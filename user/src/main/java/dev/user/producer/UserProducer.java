package dev.user.producer;

import dev.user.configuration.RabbitMQConstants;
import dev.user.domain.UserModel;
import dev.user.dto.EmailDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserProducer {
    private final RabbitTemplate rabbitTemplate;

    public UserProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishEmailMessage(UserModel user) {
        var emailDto = new EmailDto(
                user.getId(),
                null,
                user.getEmail(),
                "Welcome to our service!",
                "Dear " + user.getUsername() + ",\n\nThank you for registering with us. We're excited to have you on board!\n\nBest regards,\nThe Team"
        );

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConstants.EXCHANGE_NAME,
                    RabbitMQConstants.ROUTING_KEY_WELCOME,
                    emailDto
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed email sender", e);
        }
    }
}