package com.azentio.rfp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rfp_clarifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfpClarification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id")
    private Long documentId;

    @Column(name = "query_id")
    private Long queryId;

    @Column(name = "requirement_id")
    private String requirementId;

    @Column(name = "section_name")
    private String sectionName;

    @Column(name = "rfp_title")
    private String rfpTitle;

    @Column(name = "status")
    @Builder.Default
    private String status = "PENDING_CUSTOMER_FEEDBACK";

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "clarification_needed", nullable = false, columnDefinition = "TEXT")
    private String clarificationNeeded;

    @Column(name = "reason_justification", nullable = false, columnDefinition = "TEXT")
    private String reasonJustification;

    // Level 2 Fields
    @Column(name = "working_assumption", columnDefinition = "TEXT")
    private String workingAssumption;

    @Column(name = "impact_priority", length = 20)
    @Builder.Default
    private String impactPriority = "MEDIUM"; // HIGH, MEDIUM, LOW

    @Column(name = "impact_area", length = 50)
    @Builder.Default
    private String impactArea = "SOLUTION_DESIGN"; // SOLUTION_DESIGN, DELIVERY_TIMELINE, COMMERCIAL_SCOPE

    @Column(name = "cluster_group", length = 100)
    private String clusterGroup; // Consolidated grouping to avoid redundant requests

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
