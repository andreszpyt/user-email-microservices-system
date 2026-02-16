package dev.email.dto;

import dev.email.domain.EmailModel;
import org.springframework.stereotype.Component;

@Component
public record EmailMapper (){
        public static EmailDto toEmailDto(EmailModel emailModel) {
            return new EmailDto(
                    emailModel.getUserId(),
                    emailModel.getEmailFrom(),
                    emailModel.getEmailTo(),
                    emailModel.getEmailSubject(),
                    emailModel.getEmailBody(),
                    emailModel.getEmailStatus()
            );
        }

        public static EmailModel toEmailModel(EmailDto emailDto) {
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
