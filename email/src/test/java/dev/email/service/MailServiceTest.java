package dev.email.service;

import dev.email.domain.EmailModel;
import dev.email.enums.EmailStatus;
import dev.email.repository.EmailRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailRepository emailRepository;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private MailService mailService;

    @BeforeEach
    void setUp() {
        mailService.emailFrom = "noreply@service.com";
    }

    @Test
    void sendEmail_shouldMarkAsSentWhenDeliverySucceeds() {
        EmailModel emailModel = new EmailModel(
                null,
                UUID.randomUUID(),
                null,
                "joao@email.com",
                "Welcome",
                "<p>Hello</p>",
                null
        );

        when(emailRepository.save(any(EmailModel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        EmailModel result = mailService.sendEmail(emailModel);

        assertEquals(EmailStatus.SENT, result.getEmailStatus());
        assertEquals("noreply@service.com", result.getEmailFrom());
        verify(mailSender).send(mimeMessage);
        verify(emailRepository, times(2)).save(any(EmailModel.class));
    }

    @Test
    void sendEmail_shouldMarkAsFailedWhenEmailToIsEmpty() {
        EmailModel emailModel = new EmailModel(
                null,
                UUID.randomUUID(),
                null,
                "",
                "Welcome",
                "<p>Hello</p>",
                null
        );

        when(emailRepository.save(any(EmailModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmailModel result = mailService.sendEmail(emailModel);

        assertEquals(EmailStatus.FAILED, result.getEmailStatus());
        verify(emailRepository, times(2)).save(any(EmailModel.class));
    }

    @Test
    void sendEmail_shouldMarkAsFailedWhenSmtpUsernameIsMissing() {
        mailService.emailFrom = "";
        EmailModel emailModel = new EmailModel(
                null,
                UUID.randomUUID(),
                null,
                "joao@email.com",
                "Welcome",
                "<p>Hello</p>",
                null
        );

        when(emailRepository.save(any(EmailModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmailModel result = mailService.sendEmail(emailModel);

        assertEquals(EmailStatus.FAILED, result.getEmailStatus());
        verify(emailRepository, times(2)).save(any(EmailModel.class));
    }

    @Test
    void sendEmail_shouldMarkAsFailedWhenMailSenderThrows() {
        EmailModel emailModel = new EmailModel(
                null,
                UUID.randomUUID(),
                null,
                "joao@email.com",
                "Welcome",
                "<p>Hello</p>",
                null
        );

        when(emailRepository.save(any(EmailModel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("SMTP unavailable")).when(mailSender).send(mimeMessage);

        EmailModel result = mailService.sendEmail(emailModel);

        assertEquals(EmailStatus.FAILED, result.getEmailStatus());
        verify(emailRepository, times(2)).save(any(EmailModel.class));
    }
}
