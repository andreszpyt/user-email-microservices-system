package dev.email.dto;

import dev.email.domain.EmailModel;
import org.springframework.stereotype.Component;

@Component
public class EmailMapper {

    public EmailDto toDto(EmailModel emailModel) {
        return new EmailDto(
                emailModel.getUserId(),
                emailModel.getEmailFrom(),
                emailModel.getEmailTo(),
                emailModel.getEmailSubject(),
                emailModel.getEmailBody(),
                emailModel.getEmailStatus()
        );
    }

    public EmailModel toDomain(EmailDto emailDto) {
        return new EmailModel(
                null,
                emailDto.userId(),
                emailDto.emailFrom(),
                emailDto.emailTo(),
                emailDto.emailSubject(),
                emailDto.emailBody(),
                emailDto.emailStatus()
        );
    }
}