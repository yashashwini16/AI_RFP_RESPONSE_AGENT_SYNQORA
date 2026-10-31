package com.azentio.rfp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "rfp_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfpDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String filename;

    @Column(name = "rfp_title", nullable = false, length = 150)
    private String rfpTitle;

    @Column(name = "client_name", length = 100)
    private String clientName;

    @Column(name = "submission_deadline", length = 50)
    private String submissionDeadline;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "formatting_rules", columnDefinition = "jsonb")
    private String formattingRules;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
