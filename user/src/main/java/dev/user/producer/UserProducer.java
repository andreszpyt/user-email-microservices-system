package dev.user.producer;

import dev.user.domain.UserModel;
import dev.user.dto.EmailDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserProducer {
    private final RabbitTemplate rabbitTemplate;

    public UserProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Value("${spring.rabbitmq.template.exchange}")
    private String exchangeName;
    @Value("${spring.rabbitmq.template.routing-key}")
    private String routingKey;

    public void publishEmailMessage(UserModel user) {
        try {
            EmailDto emailDto = new EmailDto(user.getId(), "Olá " + user.getUsername(), user.getEmail(), "Sucessful Registration", "Welcome to our platform! Your registration was successful.");
            rabbitTemplate.convertAndSend(exchangeName, routingKey, emailDto);
        }catch (Exception e){
            throw new RuntimeException("Email sending failed");
        }
    }
}
