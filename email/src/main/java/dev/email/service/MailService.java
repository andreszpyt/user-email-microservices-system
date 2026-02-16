package dev.email.service;

import dev.email.domain.EmailModel;
import dev.email.enums.EmailStatus;
import dev.email.repository.EmailRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    private final EmailRepository emailRepository;

    @Autowired
    public MailService(JavaMailSender mailSender, EmailRepository emailRepository) {
        this.mailSender = mailSender;
        this.emailRepository = emailRepository;
    }

    @Value("${spring.mail.username}")
    String emailFrom;

    public EmailModel sendEmail(EmailModel emailModel) {

        emailModel.setEmailStatus(EmailStatus.PENDING);
        emailModel = emailRepository.save(emailModel);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailFrom);
            helper.setTo(emailModel.getEmailTo());
            helper.setSubject(emailModel.getEmailSubject());
            helper.setText(emailModel.getEmailBody(), true);

            mailSender.send(message);

            emailModel.setEmailStatus(EmailStatus.SENT);
            emailRepository.save(emailModel);
            logger.info("Email sent to {}", emailModel.getEmailTo());
        } catch (MessagingException | RuntimeException ex) {
            emailModel.setEmailStatus(EmailStatus.FAILED);
            emailRepository.save(emailModel);
            logger.error("Failed to send email to {}: {}", emailModel.getEmailTo(), ex.getMessage(), ex);
        }

        return emailModel;
    }
}
