package dev.email.dto;

public record EmailDto(
        String subject,
        String body,
        String emailTo
) {
}
