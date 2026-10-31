package com.azentio.rfp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rfp_requirement_dependencies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfpRequirementDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id")
    private Long documentId;

    @Column(name = "source_requirement_id", nullable = false, length = 50)
    private String sourceRequirementId; // e.g. "REQ-FN-01"

    @Column(name = "target_requirement_id", nullable = false, length = 50)
    private String targetRequirementId; // e.g. "REQ-SEC-01"

    @Column(name = "dependency_type", nullable = false, length = 50)
    private String dependencyType; // FUNCTIONAL, DATA, ARCHITECTURAL, SHARIAH

    @Column(columnDefinition = "TEXT")
    private String description;
}
