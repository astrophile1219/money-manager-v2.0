package in.dipak.money_manager.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setText(body);
            message.setSubject(subject);
            mailSender.send(message);
        }catch (Exception e) {
            System.out.println("hey there! I am here again.");
//            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
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

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false); // false = plain text

            helper.addAttachment(
                    filename,
                    new ByteArrayResource(attachment)
            );

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email with attachment", e);
        }
    }


}
