package dev.email.dto;

import dev.email.enums.EmailStatus;

import java.util.UUID;

public record EmailDto(
        UUID userId,
        String emailFrom,
        String emailTo,
        String emailSubject,
        String emailBody,
        EmailStatus emailStatus
) {
}
