//package kr.kro.tripsketch.services
//
//import com.google.api.services.gmail.Gmail
//import com.google.api.services.gmail.model.Message
//import org.apache.commons.codec.binary.Base64
//import org.springframework.stereotype.Service
//import java.util.Properties
//import javax.mail.Message.RecipientType
//import javax.mail.Session
//import javax.mail.internet.InternetAddress
//import javax.mail.internet.MimeMessage
//
//@Service
//class EmailService(private val gmail: Gmail) {
//
//    fun sendDeletionWarningEmail(email: String) {
//        val emailContent = createEmail(email, "youremail@gmail.com", "Deletion Warning", "Your content will be deleted soon!")
//        sendMessage(emailContent)
//    }
//
//    private fun createEmail(to: String, from: String, subject: String, bodyText: String): MimeMessage {
//        val props = Properties()
//        val session = Session.getDefaultInstance(props, null)
//
//        val email = MimeMessage(session)
//        email.setFrom(InternetAddress(from))
//        email.addRecipient(RecipientType.TO, InternetAddress(to))
//        email.subject = subject
//        email.setText(bodyText)
//        return email
//    }
//
//    private fun sendMessage(emailContent: MimeMessage): Message {
//        val bytes = Base64.encodeBase64URLSafeString(emailContent.toByteArray())
//        val message = Message()
//        message.raw = bytes
//        return gmail.users().messages().send("me", message).execute()
//    }
//}
