package dev.user.producer;

import dev.user.configuration.RabbitMQConstants;
import dev.user.domain.UserModel;
import dev.user.dto.EmailDto;
import dev.user.enums.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private UserProducer userProducer;

    @Test
    void publishEmailMessage_shouldSendWelcomeEmailToExchange() {
        UUID userId = UUID.randomUUID();
        UserModel user = UserModel.builder()
                .id(userId)
                .username("Joao")
                .email("joao@email.com")
                .role(Role.USER)
                .build();

        userProducer.publishEmailMessage(user);

        ArgumentCaptor<EmailDto> emailCaptor = ArgumentCaptor.forClass(EmailDto.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConstants.EXCHANGE_NAME),
                eq(RabbitMQConstants.ROUTING_KEY_WELCOME),
                emailCaptor.capture()
        );

        EmailDto emailDto = emailCaptor.getValue();
        assertEquals(userId, emailDto.userId());
        assertNull(emailDto.emailFrom());
        assertEquals("joao@email.com", emailDto.emailTo());
        assertEquals("Welcome to our service!", emailDto.emailSubject());
        assertTrue(emailDto.emailBody().contains("Dear Joao"));
    }

    @Test
    void publishEmailMessage_shouldWrapFailuresInRuntimeException() {
        UserModel user = UserModel.builder()
                .id(UUID.randomUUID())
                .username("Joao")
                .email("joao@email.com")
                .role(Role.USER)
                .build();

        doThrow(new RuntimeException("broker down"))
                .when(rabbitTemplate)
                .convertAndSend(eq(RabbitMQConstants.EXCHANGE_NAME), eq(RabbitMQConstants.ROUTING_KEY_WELCOME), any(EmailDto.class));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userProducer.publishEmailMessage(user)
        );

        assertEquals("Failed email sender", exception.getMessage());
    }
}
