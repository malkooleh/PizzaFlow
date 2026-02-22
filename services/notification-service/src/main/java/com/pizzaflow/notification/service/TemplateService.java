package com.pizzaflow.notification.service;

import com.pizzaflow.notification.model.NotificationTemplate;
import com.pizzaflow.notification.model.enums.NotificationChannel;
import com.pizzaflow.notification.repository.NotificationTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TemplateService {

    private static final Logger log = LoggerFactory.getLogger(TemplateService.class);
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(\\w+)}}");

    private final NotificationTemplateRepository templateRepository;

    public TemplateService(NotificationTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    public Optional<NotificationTemplate> getTemplate(String name) {
        return templateRepository.findByName(name);
    }

    public List<NotificationTemplate> getActiveTemplates() {
        return templateRepository.findByIsActiveTrue();
    }

    public List<NotificationTemplate> getActiveTemplates(NotificationChannel channel) {
        return templateRepository.findByChannelAndIsActiveTrue(channel);
    }

    /**
     * Render a template with variable substitution.
     * Variables are in the format {{variableName}}.
     */
    public String renderTemplate(String template, Map<String, Object> variables) {
        if (template == null || variables == null || variables.isEmpty()) {
            return template;
        }

        StringBuffer result = new StringBuffer();
        Matcher matcher = VARIABLE_PATTERN.matcher(template);

        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object value = variables.get(variableName);
            String replacement = value != null ? value.toString() : "{{" + variableName + "}}";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Render both subject and body from a template.
     */
    public RenderedTemplate render(NotificationTemplate template, Map<String, Object> variables) {
        String renderedSubject = renderTemplate(template.getSubject(), variables);
        String renderedBody = renderTemplate(template.getBodyTemplate(), variables);
        return new RenderedTemplate(renderedSubject, renderedBody);
    }

    public record RenderedTemplate(String subject, String body) {}
}
