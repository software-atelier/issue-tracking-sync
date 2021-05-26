package ch.loewenfels.issuetrackingsync.syncclient.rtc

import org.jsoup.Jsoup
import java.util.concurrent.atomic.AtomicInteger

/**
 * Converts HTML into plain text.
 */
class HtmlConverter {

    companion object {

        private val ulPattern = "<ul>(.*?)</ul>".toRegex()
        private val olPattern = "<ol>(.*?)</ol>".toRegex()
        private val liPattern = "<li>".toRegex()

        /**
         * Checks if the given text is HTML.
         *
         * @return true if the text contains HTML elements, false otherwise.
         */
        fun isHtml(text: String): Boolean {
            return Jsoup.parse(text).wholeText() != text
        }

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
            text = ulPattern.replace(text) { mr -> addBulletpoints(mr.groupValues[0]) }
            // Add numbers into <ol> lists
            text = olPattern.replace(text) { mr -> addNumbers(mr.groupValues[0]) }
            // New line before each list item
            text = text.replace("<li>", "<li>\n")
            // New line after each list
            text = text.replace("</ul>", "</ul>\n")
            text = text.replace("</ol>", "</ol>\n")
            // New line after each <br/>
            text = text.replace("<br/>", "<br/>\n")
            // Two new lines at the start of each paragraph
            text = text.replace("<p>", "<p>\n\n")
            // Two new lines after each paragraph
            text = text.replace("</p>", "</p>\n\n")
            // Convert html to text while keeping previously inserted new lines
            text = Jsoup.parse(text).wholeText()
            // Cleanup unnecessary new lines
            text = text.trim()
            // Cleanup occurrences of more than two new lines
            return text.replace("[ ]*\n[ ]*\n[ ]*\n[ \n]*".toRegex(), "\n\n")
        }

        private fun addBulletpoints(value: String): String {
            return value.replace("<li>", "<li>• ")
        }

        private fun addNumbers(value: String): String {
            val counter = AtomicInteger(1)
            return liPattern.replace(value) { "<li>" + counter.getAndIncrement() + ". " }
        }
    }
}