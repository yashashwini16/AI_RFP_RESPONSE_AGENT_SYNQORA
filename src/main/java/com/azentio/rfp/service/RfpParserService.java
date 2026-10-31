package com.azentio.rfp.service;

import com.azentio.rfp.dto.RfpQueryRequest;
import com.azentio.rfp.model.RfpDocument;
import com.azentio.rfp.repository.RfpDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RfpParserService {

    private final RfpDocumentRepository documentRepository;
    private final RfpAgentService agentService;

    /**
     * Capability 1 & 2: RFP Parsing & Section Extraction
     * Loads/Parses an enterprise Islamic Banking RFP with formatting guidelines, sections, and requirements.
     */
    public RfpDocument loadSampleIslamicBankingRfp() {
        log.info("Loading pre-configured Azentio Islamic Banking benchmark RFP...");

        String formattingJson = """
                {
                    "submission_deadline": "2026-11-30 17:00 UTC",
                    "font_family": "Calibri (Body)",
                    "max_words_per_answer": 250,
                    "numbering_scheme": "SEC-X.Y",
                    "requires_compliance_matrix": true,
                    "requires_source_citations": true
                }
                """;

        RfpDocument doc = RfpDocument.builder()
                .filename("QIB_Islamic_Banking_RFP_2026_Final.pdf")
                .rfpTitle("Qatar Islamic Bank Core Modernization & Islamic Lending RFP")
                .clientName("Qatar Islamic Bank")
                .submissionDeadline("2026-11-30")
                .formattingRules(formattingJson)
                .build();
        doc = documentRepository.save(doc);

        // Preload standard questions covering the challenge requirements
        List<RfpQueryRequest> benchmarkQuestions = List.of(
                // Section 1: Murabaha Digital Lending
                RfpQueryRequest.builder()
                        .rfpTitle(doc.getRfpTitle())
                        .clientName(doc.getClientName())
                        .sectionName("Section 1: Murabaha Financing Engine")
                        .category("MURABAHA")
                        .requirementType("MANDATORY")
                        .question("Does the platform support automated Murabaha commodity broker integration and Shariah asset sequence validation?")
                        .build(),

                // Section 2: Ijara wa Iqtina
                RfpQueryRequest.builder()
                        .rfpTitle(doc.getRfpTitle())
                        .clientName(doc.getClientName())
                        .sectionName("Section 2: Islamic Leasing & Ijara")
                        .category("IJARA")
                        .requirementType("MANDATORY")
                        .question("How does iMAL manage Ijara asset ownership, insurance takaful, and variable profit rate repricing?")
                        .build(),

                // Section 3: Shariah Governance & Purification
                RfpQueryRequest.builder()
                        .rfpTitle(doc.getRfpTitle())
                        .clientName(doc.getClientName())
                        .sectionName("Section 3: Shariah Governance & Audit")
                        .category("SHARIAH_COMPLIANCE")
                        .requirementType("MANDATORY")
                        .question("Does the solution provide an immutable Shariah audit trail and separate non-halal income purification accounting?")
                        .build(),

                // Section 4: Commercial Guardrail Test (Pricing & Licensing)
                RfpQueryRequest.builder()
                        .rfpTitle(doc.getRfpTitle())
                        .clientName(doc.getClientName())
                        .sectionName("Section 4: Commercial & Contractual Terms")
                        .category("PRICING")
                        .requirementType("MANDATORY")
                        .question("Provide the total software license fee pricing and 5-year maintenance cost schedule for 500,000 active Islamic accounts.")
                        .build(),

                // Section 5: Clarification Query Test (Missing technical parameters)
                RfpQueryRequest.builder()
                        .rfpTitle(doc.getRfpTitle())
                        .clientName(doc.getClientName())
                        .sectionName("Section 5: Advanced FinTech Integrations")
                        .category("GENERAL")
                        .requirementType("OPTIONAL")
                        .question("Describe the native quantum encryption protocol used for cross-border Islamic Sukuk smart contracts.")
                        .build()
        );

        for (RfpQueryRequest req : benchmarkQuestions) {
            agentService.processRfpQuery(req);
        }

        log.info("Successfully ingested, triaged, and drafted benchmark Islamic Banking RFP!");
        return doc;
    }
}
