package com.azentio.rfp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "rfp_responses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfpResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "query_id")
    private Long queryId;

    @Column(name = "drafted_answer", nullable = false, columnDefinition = "TEXT")
    private String draftedAnswer;

    @Column(name = "confidence_score", nullable = false)
    private Integer confidenceScore;

    @Column(name = "response_classification", length = 30)
    @Builder.Default
    private String responseClassification = "Fully Addressed"; // Fully Addressed, Partially Addressed, Information Required, Not Supported

    @Column(name = "unanswered_explanation", columnDefinition = "TEXT")
    private String unansweredExplanation; // Explains why requirement cannot be answered completely

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "source_citations", columnDefinition = "jsonb")
    private String sourceCitations; // JSONB storage with source_type & version

    @Column(name = "gap_flag")
    @Builder.Default
    private Boolean gapFlag = false;

    @Column(name = "guardrail_reason", columnDefinition = "TEXT")
    private String guardrailReason;

    @Column(name = "outdated_warning", columnDefinition = "TEXT")
    private String outdatedWarning;

    @Column(name = "reviewed_answer", columnDefinition = "TEXT")
    private String reviewedAnswer;

    @Column(name = "is_approved")
    @Builder.Default
    private Boolean isApproved = false;

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
