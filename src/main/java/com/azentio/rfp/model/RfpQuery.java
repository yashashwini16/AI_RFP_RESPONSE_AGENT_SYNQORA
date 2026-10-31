package com.azentio.rfp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rfp_queries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfpQuery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rfp_title", nullable = false, length = 100)
    private String rfpTitle;

    @Column(name = "client_name", length = 100)
    private String clientName;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(length = 50)
    private String category;

    @Column(length = 20)
    @Builder.Default
    private String status = "PENDING"; // PENDING, DRAFTED, REVIEWED, APPROVED, NEEDS_REVIEW
}
