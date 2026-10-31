package com.azentio.rfp.service;

import com.azentio.rfp.dto.ClarificationItemDTO;
import com.azentio.rfp.model.RfpClarification;
import com.azentio.rfp.model.RfpQuery;
import com.azentio.rfp.repository.RfpClarificationRepository;
import com.azentio.rfp.repository.RfpQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RfpClarificationService {

    private final RfpClarificationRepository clarificationRepository;
    private final RfpQueryRepository queryRepository;

    /**
     * Capability 5: Clarification Query Generation
     * When knowledge is missing, instead of assuming, the agent compiles a formal clarification query.
     */
    @Transactional
    public void registerClarificationQuery(Long queryId, String questionText, String sectionName) {
        // Formulate professional clarification query with justification
        String needed = "Detailed technical specification regarding " + extractTopic(questionText) + " architecture and integration standards.";
        String reason = "Azentio iMAL supports multiple market-standard implementations (e.g. Bursa Suq Al-Sila vs. DMCC, direct API vs. SWIFT). Clarification ensures accurate scope and Shariah alignment.";

        RfpClarification c = RfpClarification.builder()
                .queryId(queryId)
                .questionText(questionText)
                .clarificationNeeded(needed)
                .reasonJustification(reason)
                .build();
        clarificationRepository.save(c);
        log.info("Registered customer clarification query for Query #{}", queryId);
    }

    public List<ClarificationItemDTO> getAllClarifications() {
        List<RfpClarification> list = clarificationRepository.findAllByOrderByIdAsc();
        List<ClarificationItemDTO> dtos = new ArrayList<>();

        for (RfpClarification c : list) {
            String section = "Islamic Banking Specifications";
            if (c.getQueryId() != null) {
                RfpQuery q = queryRepository.findById(c.getQueryId()).orElse(null);
                if (q != null && q.getSectionName() != null) {
                    section = q.getSectionName();
                }
            }

            dtos.add(ClarificationItemDTO.builder()
                    .id(c.getId())
                    .queryId(c.getQueryId())
                    .questionText(c.getQuestionText())
                    .clarificationNeeded(c.getClarificationNeeded())
                    .reasonJustification(c.getReasonJustification())
                    .sectionName(section)
                    .build());
        }
        return dtos;
    }

    private String extractTopic(String text) {
        if (text == null) return "feature";
        String lower = text.toLowerCase();
        if (lower.contains("murabaha")) return "Murabaha commodity trade";
        if (lower.contains("ijara")) return "Ijara lease asset management";
        if (lower.contains("sukuk")) return "Sukuk issuance & registry";
        if (lower.contains("shariah")) return "Shariah board audit workflow";
        if (lower.contains("mudaraba")) return "Mudaraba profit distribution pooling";
        return "system requirement";
    }
}
