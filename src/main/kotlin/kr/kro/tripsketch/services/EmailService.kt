package kr.kro.tripsketch.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val resourceLoader: ResourceLoader,
    @Autowired private val emailSender: JavaMailSender
) {

    fun sendDeletionWarningEmail(to: String, deletionDate: String) {
        val bodyText = getHtmlContentWithDeletionDate(deletionDate)
        sendEmail(to, "[트립스케치] 장기 미사용 계정 삭제 안내", bodyText)
    }

    private fun sendEmail(to: String, subject: String, body: String) {
        val message = SimpleMailMessage()
        message.setTo(to)
        message.setSubject(subject)
        message.setText(body)
        emailSender.send(message)
    }

    private fun getHtmlContentWithDeletionDate(deletionDate: String): String {
        val resource = resourceLoader.getResource("classpath:/static/email.html")
        val htmlTemplate = resource.inputStream.reader().use { it.readText() }
        // {{deletionDate}} 부분을 실제 삭제 예정 날짜로 대체합니다.
        return htmlTemplate.replace("{{deletionDate}}", deletionDate)
    }
}
