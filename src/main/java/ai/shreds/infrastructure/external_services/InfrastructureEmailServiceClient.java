package ai.shreds.infrastructure.external_services;

import ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Client for sending emails via SMTP.
 */
@Service
public class InfrastructureEmailServiceClient {

    private final JavaMailSender javaMailSender;

    @Autowired
    public InfrastructureEmailServiceClient(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = createMimeMessage(to, subject, body);
            javaMailSender.send(message);
        } catch (MessagingException | MailException e) {
            throw new InfrastructureExternalServiceException(
                "Failed to send email: " + e.getMessage(),
                "InfrastructureEmailServiceClient",
                0
            );
        }
    }

    private MimeMessage createMimeMessage(String to, String subject, String body) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);
        return message;
    }
}
