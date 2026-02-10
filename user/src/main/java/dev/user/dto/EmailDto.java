package dev.user.dto;

public record EmailDto(
        String subject,
        String body,
        String emailTo
) {
}
