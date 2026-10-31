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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "source_citations", columnDefinition = "jsonb")
    private String sourceCitations; // JSONB storage

    @Column(name = "gap_flag")
    @Builder.Default
    private Boolean gapFlag = false;

    @Column(name = "reviewed_answer", columnDefinition = "TEXT")
    private String reviewedAnswer;

    @Column(name = "is_approved")
    @Builder.Default
    private Boolean isApproved = false;

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
