package ch.loewenfels.issuetrackingsync.controller

import ch.loewenfels.issuetrackingsync.app.SyncApplicationProperties
import ch.loewenfels.issuetrackingsync.notification.CsvProtocol
import org.apache.commons.io.IOUtils
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import java.io.FileInputStream
import javax.servlet.http.HttpServletResponse

@Controller
class CsvController(
    private val properties: SyncApplicationProperties
) {

    @GetMapping("/protocolCsv")
    fun downloadCsv(response: HttpServletResponse) {
        val file = (properties.notificationChannels.first { it is CsvProtocol } as CsvProtocol).file
        response.setContentType("text/csv")
        response.setHeader("Content-Disposition", "attachment; filename=\"protocol.csv\"")
        FileInputStream(file).use { IOUtils.copy(it, response.outputStream) }
        response.outputStream.close()
    }
}
