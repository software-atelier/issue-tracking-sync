package ch.loewenfels.issuetrackingsync.syncclient.rtc

import org.jsoup.Jsoup
import java.util.regex.MatchResult
import java.util.regex.Pattern

/**
 * Converts HTML into plain text.
 */
class HtmlConverter {

    companion object {

        private val ulPattern: Pattern = Pattern.compile("<ul>(.*?)</ul>")
        private val olPattern: Pattern = Pattern.compile("<ol>(.*?)</ol>")
        private val liPattern: Pattern = Pattern.compile("<li>")

        /**
         * Converts the given html string into plain text.
         *
         * Will convert <br>, <p>, <ul> and <ol> tags into plain text representations.
         *
         * @param html The html string.
         * @return Plain text representation of the html string.
         */
        fun htmlToText(html: String): String {
            var text: String = html
            // Add bulletpoints into <ul> lists
            text = ulPattern.matcher(text).replaceAll{ mr: MatchResult -> addBulletpoints(mr.group()) }
            // Add numbers into <ol> lists
            text = olPattern.matcher(text).replaceAll{ mr: MatchResult -> addNumbers(mr.group()) }
            // New line before each list item
            text = text.replace("<li>".toRegex(), "<li>\n")
            // New line after each list
            text = text.replace("</ul>".toRegex(), "</ul>\n")
            text = text.replace("</ol>".toRegex(), "</ol>\n")
            // New line after each <br/>
            text = text.replace("<br/>".toRegex(), "<br/>\n")
            // Two new lines at the start of each paragraph
            text = text.replace("<p>".toRegex(), "<p>\n\n")
            // Two new lines after each paragraph
            text = text.replace("</p>".toRegex(), "</p>\n\n")
            // Convert html to text while keeping previously inserted new lines
            text = Jsoup.parse(text).wholeText()
            // Cleanup unnecessary new lines
            text = text.trim()
            // Cleanup occurrences of more than two new lines
            return text.replace("\n\n\n+", "\n\n")
        }

        private fun addBulletpoints(value: String): String {
            return value.replace("<li>".toRegex(), "<li>• ")
        }

        private fun addNumbers(value: String): String {
            val counter = Counter()
            return liPattern.matcher(value).replaceAll{ "<li>" + counter.getAndIncrement() + ". " }
        }

        internal class Counter {
            private var count: Int = 1
            fun getAndIncrement(): Int { return count++ }
        }

    }

}