package ru.t1.taskmanager.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Component
@AllArgsConstructor
public class NotificationService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Async
    public void send(String to, String subject, String templateName, Context context) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            mimeMessageHelper.setFrom("***");
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            String htmlContent = templateEngine.process(templateName, context);
            mimeMessageHelper.setText(htmlContent, true);
            javaMailSender.send(mimeMessage);
        }
        catch (MessagingException messagingException) {
            throw new IllegalStateException("Failed to send email");
        }
    }
}
