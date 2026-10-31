package com.azentio.rfp.service;

import com.azentio.rfp.dto.RfpQueryRequest;
import com.azentio.rfp.model.RfpDocument;
import com.azentio.rfp.repository.RfpDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RfpParserService {

    private final RfpDocumentRepository documentRepository;
    private final RfpAgentService agentService;

    /**
     * Level 1: RFP Document Ingestion & Structured Requirement Extraction
     * Ingests RFP covering Functional, Technical, Security, Compliance, Integration, and Commercial domains.
     */
    public RfpDocument loadSampleIslamicBankingRfp() {
        log.info("Ingesting and structuring Level 1 RFP requirements...");

        String formattingJson = """
                {
                    "submission_deadline": "2026-11-30 17:00 UTC",
                    "font_family": "Calibri",
                    "max_words_per_answer": 250,
                    "numbering_scheme": "REQ-DOMAIN-XX",
                    "export_formats": ["Word (.docx)", "Excel (.xlsx)"]
                }
                """;

        RfpDocument doc = RfpDocument.builder()
                .filename("QIB_Core_Banking_RFP_2026.pdf")
                .rfpTitle("Qatar Islamic Bank Core Modernization & Islamic Lending RFP")
                .clientName("Qatar Islamic Bank")
                .submissionDeadline("2026-11-30")
                .formattingRules(formattingJson)
                .build();
        doc = documentRepository.save(doc);

        // Clear previous test queries to ensure clean Level 1 benchmark demo
        List<RfpQueryRequest> level1Requirements = List.of(
                // 1. FUNCTIONAL REQUIREMENT
                RfpQueryRequest.builder()
                        .requirementId("REQ-FN-01")
                        .rfpTitle(doc.getRfpTitle())
                        .clientName(doc.getClientName())
                        .sectionName("Section 1: Islamic Financing Products")
                        .category("FUNCTIONAL")
                        .requirementType("MANDATORY")
                        .questionType("CONFIRMATION")
                        .question("Does the platform support automated Murabaha commodity broker integration and Shariah asset sequence validation?")
                        .build(),

                // 2. FUNCTIONAL REQUIREMENT
                RfpQueryRequest.builder()
                        .requirementId("REQ-FN-02")
                        .rfpTitle(doc.getRfpTitle())
                        .clientName(doc.getClientName())
                        .sectionName("Section 1: Islamic Financing Products")
                        .category("FUNCTIONAL")
                        .requirementType("MANDATORY")
                        .questionType("EXPLANATION")
                        .question("How does iMAL manage Ijara asset ownership, insurance takaful, and variable profit rate repricing?")
                        .build(),

                // 3. SECURITY & COMPLIANCE REQUIREMENT
                RfpQueryRequest.builder()
                        .requirementId("REQ-SEC-01")
                        .rfpTitle(doc.getRfpTitle())
                        .clientName(doc.getClientName())
                        .sectionName("Section 2: Security & Shariah Governance")
                        .category("SECURITY")
                        .requirementType("MANDATORY")
                        .questionType("EVIDENCE")
                        .question("Does the solution provide an immutable Shariah audit trail and separate non-halal income purification accounting?")
                        .build(),

                // 4. TECHNICAL / ARCHITECTURE REQUIREMENT
                RfpQueryRequest.builder()
                        .requirementId("REQ-TECH-01")
                        .rfpTitle(doc.getRfpTitle())
                        .clientName(doc.getClientName())
                        .sectionName("Section 3: High Availability & Architecture")
                        .category("TECHNICAL")
                        .requirementType("MANDATORY")
                        .questionType("FACTUAL")
                        .question("What are the guaranteed RPO and RTO during disaster recovery?")
                        .build(),

                // 5. COMMERCIAL REQUIREMENT (Triggers Commercial Guardrail)
                RfpQueryRequest.builder()
                        .requirementId("REQ-COM-01")
                        .rfpTitle(doc.getRfpTitle())
                        .clientName(doc.getClientName())
                        .sectionName("Section 4: Commercial Terms & Licensing")
                        .category("COMMERCIAL")
                        .requirementType("MANDATORY")
                        .questionType("FACTUAL")
                        .question("Provide the total software license fee pricing and 5-year maintenance cost schedule for 500,000 active Islamic accounts.")
                        .build(),

                // 6. GAP REQUIREMENT (Triggers 'Information Required' Classification)
                RfpQueryRequest.builder()
                        .requirementId("REQ-INT-01")
                        .rfpTitle(doc.getRfpTitle())
                        .clientName(doc.getClientName())
                        .sectionName("Section 5: FinTech & Ecosystem Integrations")
                        .category("INTEGRATION")
                        .requirementType("OPTIONAL")
                        .questionType("EXPLANATION")
                        .question("Describe the native quantum encryption protocol used for cross-border Islamic Sukuk smart contracts.")
                        .build()
        );

        for (RfpQueryRequest req : level1Requirements) {
            agentService.processRfpQuery(req);
        }

        log.info("Level 1 benchmark RFP loaded with full requirement-level traceability!");
        return doc;
    }
}
