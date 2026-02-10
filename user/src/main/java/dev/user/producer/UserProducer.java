package dev.user.producer;

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

    private final String EXCHANGE_NAME = "app-exchange";
    private final String ROUTING_KEY = "email.welcome";

    public void publishEmailMessage(UserModel user) {
        try {
            EmailDto emailDto = new EmailDto("Registro realizado com sucesso", "Olá " + user.getUsername(), user.getEmail());
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, emailDto);
        }catch (Exception e){
            throw new RuntimeException("Email sending failed");
        }
    }
}
