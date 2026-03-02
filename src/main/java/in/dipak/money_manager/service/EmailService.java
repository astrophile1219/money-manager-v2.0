package in.dipak.money_manager.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j  // ✅ Added
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String body) {
        try {
            // ✅ Replaced SimpleMailMessage with MimeMessage for HTML support
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // ✅ true = HTML enabled
            mailSender.send(message);
            log.info("✅ Email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("❌ Failed to send email to: {} | Error: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    public void sendEmailWithAttachment(
            String to,
            String subject,
            String body,
            byte[] attachment,
            String filename
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // ✅ Changed false → true for HTML support
            helper.addAttachment(filename, new ByteArrayResource(attachment));
            mailSender.send(message);
            log.info("✅ Email with attachment sent to: {}", to);
        } catch (MessagingException e) {
            log.error("❌ Failed to send email with attachment to: {} | Error: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email with attachment: " + e.getMessage(), e);
        }
    }
}