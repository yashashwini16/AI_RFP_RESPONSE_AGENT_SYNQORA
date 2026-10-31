package com.azentio.rfp.service;

import com.azentio.rfp.dto.HistoricalMatchDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class RfpGuardrailService {

    private static final Pattern PRICING_PATTERN = Pattern.compile(
            "\\b(price|pricing|cost|fee|license fee|rate card|discount|payment milestone|total cost of ownership|tco)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern LEGAL_PATTERN = Pattern.compile(
            "\\b(liability|indemnity|indemnification|liquidated damages|sla penalty|contractual commitment|warranty period|governing law|jurisdiction)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SLA_PATTERN = Pattern.compile(
            "\\b(uptime penalty|guaranteed 99\\.99|financial penalty|remedy for breach|default termination)\\b",
            Pattern.CASE_INSENSITIVE
    );

    public record GuardrailResult(
            boolean isTriggered,
            String guardrailFlag,     // TECHNICAL, PRICING_DESK, LEGAL_REVIEW, SLA_COMMITMENT
            String assignedOwner,     // SE_TECHNICAL, COMMERCIAL_DESK, LEGAL_COUNSEL
            String reasonExplanation
    ) {}

    /**
     * Guardrail 1: Commercial & Legal Content Triage
     * Pricing, SLAs, liability and contractual commitments are NEVER auto-answered.
     */
    public GuardrailResult evaluateGuardrails(String questionText) {
        if (questionText == null || questionText.isBlank()) {
            return new GuardrailResult(false, "TECHNICAL", "SE_TECHNICAL", null);
        }

        String lower = questionText.toLowerCase(Locale.ROOT);

        if (PRICING_PATTERN.matcher(lower).find()) {
            return new GuardrailResult(
                    true,
                    "PRICING_DESK",
                    "COMMERCIAL_DESK",
                    "⚠️ COMMERCIAL GUARDRAIL: Pricing, license fees, and financial terms cannot be auto-generated. This requirement has been routed to the Commercial & Pricing Desk for tailored proposal quotation."
            );
        }

        if (LEGAL_PATTERN.matcher(lower).find()) {
            return new GuardrailResult(
                    true,
                    "LEGAL_REVIEW",
                    "LEGAL_COUNSEL",
                    "⚠️ LEGAL GUARDRAIL: Contractual liabilities, warranties, and indemnification clauses require mandatory legal review. Flagged for Azentio Legal & Compliance Counsel."
            );
        }

        if (SLA_PATTERN.matcher(lower).find()) {
            return new GuardrailResult(
                    true,
                    "SLA_COMMITMENT",
                    "LEGAL_COUNSEL",
                    "⚠️ SLA PENALTY GUARDRAIL: Financial penalty terms and liquidated damages require executive approval prior to submission."
            );
        }

        return new GuardrailResult(false, "TECHNICAL", "SE_TECHNICAL", null);
    }

    /**
     * Guardrail 2: Source Quality & Obsolescence Warning
     * Detects when a past RFP or doc comes from an older product release (e.g. iMAL < v14).
     */
    public String evaluateSourceQuality(List<HistoricalMatchDTO> citations) {
        if (citations == null || citations.isEmpty()) return null;

        for (HistoricalMatchDTO citation : citations) {
            String version = citation.getProductVersion();
            if (version != null && (version.contains("v12") || version.contains("v13") || version.contains("2018") || version.contains("2019"))) {
                return "⚠️ SOURCE QUALITY WARNING: Grounded in legacy reference (" + version + " - " + citation.getSourceDocument() +
                        "). Current standard is Azentio iMAL v15.0. Recommended SE verification.";
            }
        }
        return null;
    }

    /**
     * Guardrail 3: Confidentiality & Client Redaction
     * Ensures customer names in past proposals are sanitized before presentation.
     */
    public String sanitizeConfidentialDetails(String text) {
        if (text == null) return null;
        return text.replaceAll("(?i)\\b(Dubai Islamic Bank|Emirates Islamic|Qatar Islamic Bank|Bank Al-Jazira|Kuwait Finance House)\\b", "[REDACTED_TIER1_ISLAMIC_BANK]");
    }
}
