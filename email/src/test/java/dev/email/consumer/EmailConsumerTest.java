package dev.email.consumer;

import dev.email.domain.EmailModel;
import dev.email.dto.EmailDto;
import dev.email.dto.EmailMapper;
import dev.email.enums.EmailStatus;
import dev.email.service.MailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailConsumerTest {

    @Mock
    private MailService mailService;

    @Mock
    private EmailMapper emailMapper;

    @InjectMocks
    private EmailConsumer emailConsumer;

    @Test
    void listenEmailQueue_shouldMapDtoAndSendEmail() {
        EmailDto emailDto = new EmailDto(
                UUID.randomUUID(),
                "noreply@service.com",
                "joao@email.com",
                "Welcome",
                "Hello",
                EmailStatus.PENDING
        );
        EmailModel emailModel = new EmailModel(
                null,
                emailDto.userId(),
                emailDto.emailFrom(),
                emailDto.emailTo(),
                emailDto.emailSubject(),
                emailDto.emailBody(),
                emailDto.emailStatus()
        );

        when(emailMapper.toDomain(emailDto)).thenReturn(emailModel);

        emailConsumer.listenEmailQueue(emailDto);

        verify(emailMapper).toDomain(emailDto);
        verify(mailService).sendEmail(emailModel);
    }

    @Test
    void listenEmailQueue_shouldSwallowExceptions() {
        EmailDto emailDto = new EmailDto(
                UUID.randomUUID(),
                "noreply@service.com",
                "joao@email.com",
                "Welcome",
                "Hello",
                EmailStatus.PENDING
        );
        EmailModel emailModel = new EmailModel(
                null,
                emailDto.userId(),
                emailDto.emailFrom(),
                emailDto.emailTo(),
                emailDto.emailSubject(),
                emailDto.emailBody(),
                emailDto.emailStatus()
        );

        when(emailMapper.toDomain(emailDto)).thenReturn(emailModel);
        doThrow(new RuntimeException("send failed")).when(mailService).sendEmail(emailModel);

        assertDoesNotThrow(() -> emailConsumer.listenEmailQueue(emailDto));
        verify(mailService).sendEmail(emailModel);
    }
}
