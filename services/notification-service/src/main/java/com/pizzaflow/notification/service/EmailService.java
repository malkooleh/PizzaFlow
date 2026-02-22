package com.pizzaflow.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${notification.channels.email.from-address:noreply@pizzaflow.com}")
    private String fromAddress;

    @Value("${notification.channels.email.from-name:PizzaFlow}")
    private String fromName;

    @Value("${notification.channels.email.enabled:true}")
    private boolean emailEnabled;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Async
    public CompletableFuture<Boolean> sendEmail(String to, String subject, String htmlBody) {
        if (!emailEnabled) {
            log.warn("Email sending is disabled. Would have sent to: {}", to);
            return CompletableFuture.completedFuture(false);
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
            return CompletableFuture.completedFuture(true);

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }

    public String renderTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        if (variables != null) {
            variables.forEach(context::setVariable);
        }
        return templateEngine.process(templateName, context);
    }

    /**
     * Send templated email
     */
    @Async
    public CompletableFuture<Boolean> sendTemplatedEmail(
        String to,
        String subject,
        String templateName,
        Map<String, Object> variables
    ) {
        String htmlBody = renderTemplate(templateName, variables);
        return sendEmail(to, subject, htmlBody);
    }
}
