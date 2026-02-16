package dev.user.dto;

import java.util.UUID;

public record EmailDto(
        UUID userId,
        String emailFrom,
        String emailTo,
        String emailSubject,
        String emailBody
) {
}
