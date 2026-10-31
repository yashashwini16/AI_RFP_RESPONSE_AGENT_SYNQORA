package com.azentio.rfp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rfp_compliance_code_schemes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceCodeScheme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scheme_key", nullable = false, length = 50)
    private String schemeKey; // e.g. "SCDTN", "STANDARD_COMPLIANCE", "YES_NO"

    @Column(nullable = false, length = 10)
    private String code; // S, C, D, T, N

    @Column(nullable = false, length = 100)
    private String classification; // Standard capability, Configuration required, etc.

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "requires_explanation")
    @Builder.Default
    private Boolean requiresExplanation = false;

    @Column(name = "requires_estimate")
    @Builder.Default
    private Boolean requiresEstimate = false;

    @Column(name = "requires_attachment")
    @Builder.Default
    private Boolean requiresAttachment = false;
}
