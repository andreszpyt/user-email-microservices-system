package dev.email.domain;


import dev.email.enums.EmailStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "emails")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EmailModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID emailId;
    private UUID userId;
    private String emailFrom;
    private String emailTo;
    private String emailSubject;
    @Column(columnDefinition = "TEXT")
    private String emailBody;
    private EmailStatus emailStatus;
}
