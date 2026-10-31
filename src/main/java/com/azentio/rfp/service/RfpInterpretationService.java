package com.azentio.rfp.service;

import com.azentio.rfp.model.ComplianceCodeScheme;
import com.azentio.rfp.model.RfpQuery;
import com.azentio.rfp.model.RfpRequirementDependency;
import com.azentio.rfp.repository.ComplianceCodeSchemeRepository;
import com.azentio.rfp.repository.RfpRequirementDependencyRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class RfpInterpretationService {

    private final ComplianceCodeSchemeRepository codeSchemeRepository;
    private final RfpRequirementDependencyRepository dependencyRepository;

    @PostConstruct
    public void seedStandardCodeSchemes() {
        if (codeSchemeRepository.count() == 0) {
            log.info("Seeding Level 2 Customer Compliance Code Scheme (S/C/D/T/N)...");
            List<ComplianceCodeScheme> standardScheme = List.of(
                    ComplianceCodeScheme.builder()
                            .schemeKey("SCDTN")
                            .code("S")
                            .classification("Standard capability")
                            .description("Out-of-the-box standard product feature in current iMAL release.")
                            .requiresExplanation(false)
                            .requiresEstimate(false)
                            .requiresAttachment(false)
                            .build(),
                    ComplianceCodeScheme.builder()
                            .schemeKey("SCDTN")
                            .code("C")
                            .classification("Configuration required")
                            .description("Supported through core parameterization, rule engine, or business configuration without code change.")
                            .requiresExplanation(true)
                            .requiresEstimate(false)
                            .requiresAttachment(false)
                            .build(),
                    ComplianceCodeScheme.builder()
                            .schemeKey("SCDTN")
                            .code("D")
                            .classification("Development required")
                            .description("Custom development, bespoke API adapters, or UI modifications needed. Requires man-day estimate.")
                            .requiresExplanation(true)
                            .requiresEstimate(true)
                            .requiresAttachment(false)
                            .build(),
                    ComplianceCodeScheme.builder()
                            .schemeKey("SCDTN")
                            .code("T")
                            .classification("Third-party dependency")
                            .description("Requires an external partner, certified FinTech integration, or client-provided hardware/license.")
                            .requiresExplanation(true)
                            .requiresEstimate(false)
                            .requiresAttachment(true)
                            .build(),
                    ComplianceCodeScheme.builder()
                            .schemeKey("SCDTN")
                            .code("N")
                            .classification("Not available")
                            .description("Capability is not currently supported or is strictly out of project scope.")
                            .requiresExplanation(true)
                            .requiresEstimate(false)
                            .requiresAttachment(false)
                            .build()
            );
            codeSchemeRepository.saveAll(standardScheme);
        }
    }

    /**
     * Interprets delivery model code (S/C/D/T/N), multi-product span, dependencies, and ambiguity.
     */
    public void interpretRequirement(RfpQuery query, int confidenceScore, String answerNarrative) {
        String text = (query.getQuestionText() != null ? query.getQuestionText() : "").toLowerCase(Locale.ROOT);
        String category = (query.getCategory() != null ? query.getCategory() : "").toUpperCase(Locale.ROOT);
        String narrative = (answerNarrative != null ? answerNarrative : "").toLowerCase(Locale.ROOT);

        // 1. Detect Multi-Product & Solution Components
        List<String> components = new ArrayList<>();
        components.add("iMAL Core Islamic Engine");

        if (text.contains("broker") || text.contains("trading") || text.contains("bursa") || text.contains("dmcc")) {
            components.add("Azentio TradeGate Connector");
        }
        if (text.contains("digital") || text.contains("mobile") || text.contains("onboarding") || text.contains("channel")) {
            components.add("Azentio Digital Banking Omni-Channel");
        }
        if (text.contains("audit") || text.contains("purification") || text.contains("shariah") || text.contains("fatwa")) {
            components.add("Azentio Shariah Governance Bureau");
        }
        if (text.contains("disaster") || text.contains("rpo") || text.contains("rto") || text.contains("replication")) {
            components.add("Azentio High-Availability Cluster Mesh");
        }
        if (text.contains("payment") || text.contains("switch") || text.contains("iso 20022") || text.contains("rtgs")) {
            components.add("Azentio Enterprise Payment Hub");
        }
        query.setMultiProductComponents(String.join(", ", components));

        // 2. Detect Inter-Requirement Dependencies
        List<String> deps = new ArrayList<>();
        if (text.contains("murabaha") || text.contains("broker")) {
            deps.add("REQ-SEC-01 (Shariah Audit Trail)");
        }
        if (text.contains("digital") || text.contains("onboarding")) {
            deps.add("REQ-FN-01 (Core CIF Master)");
        }
        if (text.contains("disaster recovery") || text.contains("rpo")) {
            deps.add("Infrastructure Dark-Fiber Network (Customer Dependency)");
        }
        if (!deps.isEmpty()) {
            query.setDependencies(String.join(", ", deps));
        }

        // 3. Ambiguity & Incompleteness Detection
        boolean isAmbiguous = false;
        StringBuilder ambiguityNotes = new StringBuilder();

        if (text.contains("quantum encryption") || text.contains("smart contracts")) {
            isAmbiguous = true;
            ambiguityNotes.append("Emerging tech specification lacks protocol standard and key management infrastructure specs. ");
        }
        if ((text.contains("rpo") || text.contains("rto")) && !text.contains("bandwidth") && !text.contains("latency")) {
            isAmbiguous = true;
            ambiguityNotes.append("Disaster recovery SLAs requested without network round-trip latency and bandwidth parameters between Doha DC and DR site. ");
        }
        if (text.contains("ultra-fast") || text.contains("high volume") && !text.contains("tps")) {
            isAmbiguous = true;
            ambiguityNotes.append("Vague performance adjectives used without quantifiable Transactions-Per-Second (TPS) baseline. ");
        }
        query.setAmbiguityFlag(isAmbiguous);
        query.setAmbiguityNotes(ambiguityNotes.length() > 0 ? ambiguityNotes.toString().trim() : null);

        // 4. Determine Delivery Capability Response Code (S / C / D / T / N)
        if ("COMMERCIAL".equalsIgnoreCase(category) || text.contains("license fee") || text.contains("pricing")) {
            query.setResponseCode("N");
            query.setResponseCodeDesc("Not available (Commercial Confidential)");
            query.setAssignedDepartment("COMMERCIAL_LEGAL");
            query.setRequiresEstimate(false);
            query.setRequiresAttachment(false);
            query.setHumanValidationRequired(true);
        } else if (text.contains("quantum encryption") || text.contains("smart contracts")) {
            // Confirmed product limitation
            query.setResponseCode("N");
            query.setResponseCodeDesc("Not available (Roadmap Evaluation)");
            query.setAssignedDepartment("PRODUCT_MANAGEMENT");
            query.setGapNature("PRODUCT_GAP");
            query.setGapType("FUNCTIONALITY_GAP");
            query.setRequiresEstimate(false);
            query.setRequiresAttachment(false);
            query.setHumanValidationRequired(true);
        } else if (text.contains("broker") || text.contains("bursa") || text.contains("dmcc") || text.contains("credit bureau")) {
            // Third-party dependency
            query.setResponseCode("T");
            query.setResponseCodeDesc("Third-party dependency");
            query.setAssignedDepartment("SOLUTION_ARCHITECTURE");
            query.setRequiresAttachment(true);
            query.setPartnerDependency("Bursa Malaysia Suq Al-Sila / DMCC Tradeflow API");
            query.setHumanValidationRequired(confidenceScore < 85);
        } else if (text.contains("disaster recovery") || text.contains("rpo") || text.contains("rto") || text.contains("repricing")) {
            // Configuration required
            query.setResponseCode("C");
            query.setResponseCodeDesc("Configuration required");
            query.setAssignedDepartment("SOLUTION_ARCHITECTURE");
            query.setRequiresEstimate(false);
            query.setRequiresAttachment(false);
            query.setHumanValidationRequired(confidenceScore < 80);
        } else if (text.contains("custom") || text.contains("bespoke") || text.contains("instant payment switch adapter")) {
            // Development required
            query.setResponseCode("D");
            query.setResponseCodeDesc("Development required");
            query.setAssignedDepartment("CORE_ENGINEERING");
            query.setRequiresEstimate(true);
            query.setEstimateDetails("20-25 Man-Days");
            query.setRequiresAttachment(false);
            query.setHumanValidationRequired(true);
        } else {
            // Standard product capability
            query.setResponseCode("S");
            query.setResponseCodeDesc("Standard capability");
            query.setAssignedDepartment(text.contains("shariah") ? "SHARIAH_BOARD" : "SALES_ENGINEERING");
            query.setRequiresEstimate(false);
            query.setRequiresAttachment(false);
            query.setHumanValidationRequired(confidenceScore < 75);
        }
    }
}
