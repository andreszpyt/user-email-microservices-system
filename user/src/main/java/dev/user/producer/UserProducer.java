package dev.user.producer;

import dev.user.configuration.RabbitMQConstants;
import dev.user.domain.UserModel;
import dev.user.dto.EmailDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(UserProducer.class);
    
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
            logger.info("📤 Publishing email message to exchange: {} with routing key: {} for user: {}", 
                    RabbitMQConstants.EXCHANGE_NAME, 
                    RabbitMQConstants.ROUTING_KEY_WELCOME,
                    user.getEmail());
                    
            rabbitTemplate.convertAndSend(
                    RabbitMQConstants.EXCHANGE_NAME,
                    RabbitMQConstants.ROUTING_KEY_WELCOME,
                    emailDto
            );
            
            logger.info("✅ Email message published successfully for user: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("❌ Failed to publish email message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed email sender", e);
        }
    }
}