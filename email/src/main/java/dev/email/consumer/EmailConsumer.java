package dev.email.consumer;

import dev.email.configuration.RabbitMQConstants;
import dev.email.dto.EmailDto;
import dev.email.dto.EmailMapper;
import dev.email.service.MailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {

    private final MailService mailService;
    private final EmailMapper emailMapper;

    public EmailConsumer(MailService mailService, EmailMapper emailMapper) {
        this.mailService = mailService;
        this.emailMapper = emailMapper;
    }

    @RabbitListener(queues = RabbitMQConstants.QUEUE_EMAIL)
    public void listenEmailQueue(@Payload EmailDto emailDto){
        mailService.sendEmail(emailMapper.toDomain(emailDto));
    }

}
