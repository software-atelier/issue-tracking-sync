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
            text = ulPattern.matcher(text).replaceAll{
                    mr: MatchResult -> addBulletpoints(mr.group().toString())
            }.toString()
            // Add numbers into <ol> lists
            text = olPattern.matcher(text).replaceAll{
                    mr: MatchResult -> addNumbers(mr.group().toString())
            }.toString()
            // New line before each list item
            text = text.replace("<li>".toRegex(), "<li>\n")
            // New line after each list
            text = text.replace("</ul>".toRegex(), "</ul>\n")
            text = text.replace("</ol>".toRegex(), "</ol>\n")
            // New line after each <br/>
            text = text.replace("<br/>".toRegex(), "<br/>\n")
            // Two new lines (but not more) after each paragraph
            // This one needs to be first in case of back to back paragraphs (since the newlines of <p> are hidden behind the tag)
            text = text.replace("\n*</p>\n*".toRegex(), "</p>\n\n")
            // Two new lines (but not more) at the start of each paragraph
            text = text.replace("\n*<p>\n*".toRegex(), "<p>\n\n")
            // Cleanup unnecessary new lines
            text = text.trim()
            // Convert html to text while keeping previously inserted new lines
            return Jsoup.parse(text).wholeText()
        }

        private fun addBulletpoints(value: String): String {
            return value.replace("<li>".toRegex(), "<li>• ")
        }

        private fun addNumbers(value: String): String {
            val counter = Counter()
            return liPattern.matcher(value).replaceAll{
                "<li>" + counter.getAndIncrement() + ". "
            }.toString()
        }

        internal class Counter {
            private var count: Int = 1
            fun getAndIncrement(): Int { return count++ }
        }

    }

}