package com.azentio.rfp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rfp_knowledge_base")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfpKnowledgeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String category; // MURABAHA, IJARA, MUDARABA, SUKUK, SHARIAH_COMPLIANCE, ARCHITECTURE, SECURITY

    @Column(name = "source_type", nullable = false, length = 30)
    @Builder.Default
    private String sourceType = "PRODUCT_DOC"; // PRODUCT_DOC, PAST_SOW, PAST_RFP

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "approved_answer", nullable = false, columnDefinition = "TEXT")
    private String approvedAnswer;

    @Column(name = "source_document", nullable = false, length = 100)
    private String sourceDocument;

    @Column(name = "section_reference", length = 50)
    private String sectionReference;

    @Column(name = "product_version", length = 20)
    @Builder.Default
    private String productVersion = "iMAL v15.0";

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
