package com.azentio.rfp.service;

import com.azentio.rfp.dto.ClarificationPackDTO;
import com.azentio.rfp.model.RfpClarification;
import com.azentio.rfp.model.RfpQuery;
import com.azentio.rfp.repository.RfpClarificationRepository;
import com.azentio.rfp.repository.RfpQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RfpClarificationManagerService {

    private final RfpClarificationRepository clarificationRepository;
    private final RfpQueryRepository queryRepository;

    /**
     * Generates a structured, prioritized, and deduplicated Customer Clarification Pack.
     */
    public ClarificationPackDTO generateClarificationPack(Long documentId) {
        List<RfpQuery> queries = (documentId != null)
                ? queryRepository.findByDocumentIdOrderByIdAsc(documentId)
                : queryRepository.findAllByOrderByIdAsc();

        List<RfpClarification> existing = (documentId != null)
                ? clarificationRepository.findByDocumentIdOrderByIdAsc(documentId)
                : clarificationRepository.findAll();

        String rfpTitle = "Qatar Islamic Bank Core Modernization & Islamic Lending RFP";
        String clientName = "Qatar Islamic Bank";

        Map<String, ClarificationPackDTO.ClarificationClusterDTO> clusterMap = new LinkedHashMap<>();

        // 1. Process existing registered clarifications
        for (RfpClarification c : existing) {
            String cluster = (c.getClusterGroup() != null && !c.getClusterGroup().isBlank())
                    ? c.getClusterGroup()
                    : deriveClusterGroup(c.getQuestionText(), c.getSectionName());

            ClarificationPackDTO.ClarificationClusterDTO item = clusterMap.computeIfAbsent(cluster, k ->
                    ClarificationPackDTO.ClarificationClusterDTO.builder()
                            .clusterName(k)
                            .impactPriority(c.getImpactPriority() != null ? c.getImpactPriority() : "HIGH")
                            .impactArea(c.getImpactArea() != null ? c.getImpactArea() : "SOLUTION_DESIGN")
                            .consolidatedClarificationQuestion(c.getClarificationNeeded())
                            .technicalJustification(c.getReasonJustification())
                            .workingAssumption(c.getWorkingAssumption() != null ? c.getWorkingAssumption() : "Azentio assumes standard dual-broker active/active connectivity.")
                            .affectedRequirementIds(new ArrayList<>())
                            .build()
            );
            if (c.getRequirementId() != null && !item.getAffectedRequirementIds().contains(c.getRequirementId())) {
                item.getAffectedRequirementIds().add(c.getRequirementId());
            }
        }

        // 2. Identify and cluster ambiguous queries from requirements
        for (RfpQuery q : queries) {
            if (q.getRfpTitle() != null) rfpTitle = q.getRfpTitle();
            if (q.getClientName() != null) clientName = q.getClientName();

            if (Boolean.TRUE.equals(q.getAmbiguityFlag()) || "INFORMATION_REQUIRED".equalsIgnoreCase(q.getComplianceStatus())
                    || "T".equals(q.getResponseCode()) && q.getQuestionText().contains("latency")) {

                String cluster = deriveClusterGroup(q.getQuestionText(), q.getSectionName());
                ClarificationPackDTO.ClarificationClusterDTO clusterDTO = clusterMap.get(cluster);

                if (clusterDTO == null) {
                    String priority = "HIGH";
                    String area = "SOLUTION_DESIGN";
                    String assumption = "Azentio standard implementation methodology applied.";
                    String question = "Please confirm operational parameters and network standards.";
                    String reason = "Exact sizing and deployment topology depend on client network specs.";

                    String qLower = q.getQuestionText().toLowerCase(Locale.ROOT);
                    if (qLower.contains("quantum") || qLower.contains("smart contracts")) {
                        priority = "HIGH";
                        area = "SOLUTION_DESIGN";
                        question = "Clarification on Sukuk Smart Contract Protocol: Specify if public permissionless blockchain or private consortium Hyperledger / R3 Corda ledger is mandated.";
                        assumption = "Azentio assumes integration via enterprise private REST APIs connecting to client-designated custody node.";
                        reason = "Public blockchain post-quantum protocols are unsupported without specific HSM key appliances.";
                    } else if (qLower.contains("rpo") || qLower.contains("rto") || qLower.contains("disaster")) {
                        priority = "HIGH";
                        area = "DELIVERY_TIMELINE";
                        question = "Disaster Recovery Network Latency: Confirm guaranteed round-trip latency (RTT in ms) and dark-fiber bandwidth between primary Doha DC and secondary DR facility.";
                        assumption = "Azentio assumes dedicated >= 10Gbps connection with < 5ms RTT for synchronous database log replication.";
                        reason = "RPO < 5 minutes SLA cannot be contractually committed over high-latency asynchronous links.";
                    } else if (qLower.contains("broker") || qLower.contains("trading")) {
                        priority = "MEDIUM";
                        area = "COMMERCIAL_SCOPE";
                        question = "Murabaha Trading Venues: Confirm whether Qatar Islamic Bank requires Bursa Malaysia Suq Al-Sila, DMCC Tradeflow, or local Qatari commodity brokers.";
                        assumption = "Azentio includes standard pre-built adapters for both Bursa Malaysia and DMCC Tradeflow in baseline scope.";
                        reason = "Additional local broker FIX gateways may require custom adapter development.";
                    }

                    clusterDTO = ClarificationPackDTO.ClarificationClusterDTO.builder()
                            .clusterName(cluster)
                            .impactPriority(priority)
                            .impactArea(area)
                            .consolidatedClarificationQuestion(question)
                            .technicalJustification(reason)
                            .workingAssumption(assumption)
                            .affectedRequirementIds(new ArrayList<>())
                            .build();

                    clusterMap.put(cluster, clusterDTO);
                }

                if (!clusterDTO.getAffectedRequirementIds().contains(q.getRequirementId())) {
                    clusterDTO.getAffectedRequirementIds().add(q.getRequirementId());
                }
            }
        }

        List<ClarificationPackDTO.ClarificationClusterDTO> clusters = new ArrayList<>(clusterMap.values());
        int highCount = (int) clusters.stream().filter(c -> "HIGH".equalsIgnoreCase(c.getImpactPriority())).count();
        int solCount = (int) clusters.stream().filter(c -> "SOLUTION_DESIGN".equalsIgnoreCase(c.getImpactArea())).count();
        int delCount = (int) clusters.stream().filter(c -> "DELIVERY_TIMELINE".equalsIgnoreCase(c.getImpactArea())).count();
        int comCount = (int) clusters.stream().filter(c -> "COMMERCIAL_SCOPE".equalsIgnoreCase(c.getImpactArea())).count();

        return ClarificationPackDTO.builder()
                .rfpTitle(rfpTitle)
                .clientName(clientName)
                .totalClarifications(clusters.size())
                .highPriorityCount(highCount)
                .solutionDesignImpactCount(solCount)
                .deliveryTimelineImpactCount(delCount)
                .commercialScopeImpactCount(comCount)
                .clusters(clusters)
                .build();
    }

    private String deriveClusterGroup(String text, String section) {
        String combined = ((text != null ? text : "") + " " + (section != null ? section : "")).toLowerCase(Locale.ROOT);
        if (combined.contains("quantum") || combined.contains("smart contracts") || combined.contains("fintech")) {
            return "Cluster 1: Advanced FinTech & Smart Contract Architecture";
        }
        if (combined.contains("rpo") || combined.contains("rto") || combined.contains("disaster") || combined.contains("network")) {
            return "Cluster 2: High Availability Infrastructure & Network Latency";
        }
        if (combined.contains("broker") || combined.contains("trading") || combined.contains("murabaha")) {
            return "Cluster 3: Islamic Commodity Trading & Exchange Protocols";
        }
        return "Cluster 4: Core Banking & Regulatory Compliance";
    }
}
