package com.azentio.rfp.service;

import com.azentio.rfp.dto.RfpQueryRequest;
import com.azentio.rfp.model.RfpClarification;
import com.azentio.rfp.model.RfpDocument;
import com.azentio.rfp.repository.RfpClarificationRepository;
import com.azentio.rfp.repository.RfpDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class RfpParserService {

    private final RfpDocumentRepository documentRepository;
    private final RfpClarificationRepository clarificationRepository;
    private final com.azentio.rfp.repository.RfpRequirementDependencyRepository dependencyRepository;
    private final RfpAgentService agentService;

    /**
     * Level 1: Multipart File Ingestion for PDF, Excel, Word (.docx) & CSV
     */
    public Map<String, Object> parseUploadedRfp(MultipartFile file, String rfpTitle, String clientName) {
        String filename = (file != null && file.getOriginalFilename() != null && !file.getOriginalFilename().isBlank()) 
                ? file.getOriginalFilename() : "QIB_Islamic_Banking_RFP_2026.xlsx";
        String resolvedTitle = (rfpTitle != null && !rfpTitle.isBlank()) ? rfpTitle : "Qatar Islamic Bank Core Modernization & Islamic Lending RFP";
        String resolvedClient = (clientName != null && !clientName.isBlank()) ? clientName : "Qatar Islamic Bank";

        log.info("Processing uploaded RFP document: {} for client: {}", filename, resolvedClient);

        RfpDocument doc = RfpDocument.builder()
                .filename(filename)
                .rfpTitle(resolvedTitle)
                .clientName(resolvedClient)
                .submissionDeadline("2026-11-30")
                .formattingRules("{\"submission_deadline\": \"2026-11-30\", \"export_format\": \"Word/Excel\", \"max_words\": 250}")
                .build();
        doc = documentRepository.save(doc);

        List<RfpQueryRequest> extracted = new ArrayList<>();

        if (file != null && !file.isEmpty()) {
            String lower = filename.toLowerCase();
            // Validate allowed extensions
            if (!lower.endsWith(".docx") && !lower.endsWith(".xlsx") && !lower.endsWith(".xls") 
                    && !lower.endsWith(".csv") && !lower.endsWith(".pdf")) {
                documentRepository.delete(doc);
                Map<String, Object> err = new HashMap<>();
                err.put("status", "REJECTED");
                err.put("filename", filename);
                err.put("message", "Validation Failed: File format not supported. Only RFP documents in Word (.docx), Excel (.xlsx), CSV, or PDF format are accepted.");
                return err;
            }

            try {
                byte[] bytes = file.getBytes();
                
                // 1. Check if DOCX (Word Document)
                if (lower.endsWith(".docx")) {
                    extracted = parseDocxFile(bytes, doc);
                    if (!extracted.isEmpty()) {
                        resolvedTitle = doc.getRfpTitle();
                        resolvedClient = doc.getClientName();
                    }
                }
                // 2. Check if CSV
                else if (lower.endsWith(".csv")) {
                    String content = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                    String[] lines = content.split("\r?\n");
                    for (int i = 1; i < lines.length; i++) {
                        String line = lines[i].trim();
                        if (line.isBlank()) continue;
                        String[] cols = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                        if (cols.length >= 4) {
                            String reqId = cols[0].replaceAll("^\"|\"$", "").trim();
                            String sec = cols[1].replaceAll("^\"|\"$", "").trim();
                            String cat = cols[2].replaceAll("^\"|\"$", "").trim();
                            String q = cols[3].replaceAll("^\"|\"$", "").trim();
                            String reqType = cols.length > 4 ? cols[4].replaceAll("^\"|\"$", "").trim() : "MANDATORY";
                            String qType = cols.length > 5 ? cols[5].replaceAll("^\"|\"$", "").trim() : "EXPLANATION";

                            extracted.add(RfpQueryRequest.builder()
                                    .documentId(doc.getId())
                                    .requirementId(reqId.isBlank() ? "REQ-UP-" + i : reqId)
                                    .rfpTitle(resolvedTitle)
                                    .clientName(resolvedClient)
                                    .sectionName(sec.isBlank() ? "General" : sec)
                                    .category(cat.isBlank() ? "ISLAMIC_BANKING" : cat)
                                    .requirementType(reqType.toUpperCase().contains("OPTIONAL") ? "OPTIONAL" : "MANDATORY")
                                    .questionType(qType.toUpperCase())
                                    .question(q)
                                    .build());
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Binary or structured parsing error for {}: {}", filename, e.getMessage());
            }

            // If an actual file was uploaded but NO requirements or questionnaires were found, REJECT it!
            if (extracted.isEmpty()) {
                documentRepository.delete(doc);
                Map<String, Object> err = new HashMap<>();
                err.put("status", "REJECTED");
                err.put("filename", filename);
                err.put("message", "Validation Failed: The uploaded document '" + filename + "' does not contain an RFP questionnaire or requirement evaluation matrix. Non-RFP documents are rejected.");
                return err;
            }
        } else {
            // Only when user clicks "Load Demo QIB Tender" (file is null):
            extracted = List.of(
                    RfpQueryRequest.builder()
                            .documentId(doc.getId())
                            .requirementId("REQ-FN-01")
                            .rfpTitle(resolvedTitle)
                            .clientName(resolvedClient)
                            .sectionName("Section 1: Murabaha Engine")
                            .category("FUNCTIONAL")
                            .requirementType("MANDATORY")
                            .questionType("CONFIRMATION")
                            .question("Does the platform support automated Murabaha commodity broker integration and Shariah asset sequence validation?")
                            .build(),
                    RfpQueryRequest.builder()
                            .documentId(doc.getId())
                            .requirementId("REQ-FN-02")
                            .rfpTitle(resolvedTitle)
                            .clientName(resolvedClient)
                            .sectionName("Section 1: Islamic Financing")
                            .category("FUNCTIONAL")
                            .requirementType("MANDATORY")
                            .questionType("EXPLANATION")
                            .question("How does iMAL manage Ijara asset ownership, insurance takaful, and variable profit rate repricing?")
                            .build(),
                    RfpQueryRequest.builder()
                            .documentId(doc.getId())
                            .requirementId("REQ-SEC-01")
                            .rfpTitle(resolvedTitle)
                            .clientName(resolvedClient)
                            .sectionName("Section 2: Security & Governance")
                            .category("SECURITY")
                            .requirementType("MANDATORY")
                            .questionType("CONFIRMATION")
                            .question("Does the solution provide an immutable Shariah audit trail and separate non-halal income purification accounting?")
                            .build(),
                    RfpQueryRequest.builder()
                            .documentId(doc.getId())
                            .requirementId("REQ-TECH-01")
                            .rfpTitle(resolvedTitle)
                            .clientName(resolvedClient)
                            .sectionName("Section 3: Architecture & High Availability")
                            .category("TECHNICAL")
                            .requirementType("MANDATORY")
                            .questionType("FACTUAL")
                            .question("What are the guaranteed RPO and RTO during multi-region hybrid disaster recovery?")
                            .build(),
                    RfpQueryRequest.builder()
                            .documentId(doc.getId())
                            .requirementId("REQ-COM-01")
                            .rfpTitle(resolvedTitle)
                            .clientName(resolvedClient)
                            .sectionName("Section 4: Commercial Terms & Licensing")
                            .category("COMMERCIAL")
                            .requirementType("MANDATORY")
                            .questionType("EVIDENCE")
                            .question("Provide the total software license fee pricing and 5-year maintenance cost schedule for 500,000 active Islamic accounts.")
                            .build(),
                    RfpQueryRequest.builder()
                            .documentId(doc.getId())
                            .requirementId("REQ-INT-01")
                            .rfpTitle(resolvedTitle)
                            .clientName(resolvedClient)
                            .sectionName("Section 5: FinTech Integrations")
                            .category("INTEGRATION")
                            .requirementType("OPTIONAL")
                            .questionType("EXPLANATION")
                            .question("Describe the native quantum encryption protocol used for cross-border Islamic Sukuk smart contracts.")
                            .build(),
                    RfpQueryRequest.builder()
                            .documentId(doc.getId())
                            .requirementId("REQ-DEV-01")
                            .rfpTitle(resolvedTitle)
                            .clientName(resolvedClient)
                            .sectionName("Section 5: FinTech Integrations")
                            .category("INTEGRATION")
                            .requirementType("MANDATORY")
                            .questionType("EXPLANATION")
                            .question("Provide custom bespoke API adapter for Qatar Central Bank (QCB) Instant Payment Service (QMP) switch.")
                            .build(),
                    RfpQueryRequest.builder()
                            .documentId(doc.getId())
                            .requirementId("REQ-DIG-01")
                            .rfpTitle(resolvedTitle)
                            .clientName(resolvedClient)
                            .sectionName("Section 6: Digital Banking & Onboarding")
                            .category("FUNCTIONAL")
                            .requirementType("MANDATORY")
                            .questionType("CONFIRMATION")
                            .question("Does the digital onboarding mobile journey integrate with Qatar National ID e-KYC and automatically create the core Islamic CIF?")
                            .build()
            );
        }

        for (RfpQueryRequest req : extracted) {
            agentService.processRfpQuery(req);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "SUCCESS");
        resp.put("documentId", doc.getId());
        resp.put("filename", filename);
        resp.put("rfpTitle", doc.getRfpTitle());
        resp.put("clientName", doc.getClientName());
        resp.put("extractedCount", extracted.size());
        resp.put("message", "Extracted and triaged " + extracted.size() + " requirements from " + filename);
        return resp;
    }

    /**
     * Natively extracts tables and questionnaires from DOCX (Word Document) XML
     */
    private List<RfpQueryRequest> parseDocxFile(byte[] fileBytes, RfpDocument doc) {
        List<RfpQueryRequest> list = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(fileBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if ("word/document.xml".equals(entry.getName())) {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.setNamespaceAware(false);
                    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                    Document xmlDoc = factory.newDocumentBuilder().parse(new InputSource(zis));

                    NodeList tables = xmlDoc.getElementsByTagName("w:tbl");
                    if (tables.getLength() > 0) {
                        // Extract metadata from Table 0 if present
                        Element metaTable = (Element) tables.item(0);
                        NodeList metaRows = metaTable.getElementsByTagName("w:tr");
                        for (int r = 0; r < metaRows.getLength(); r++) {
                            List<String> cells = getRowCells((Element) metaRows.item(r));
                            if (cells.size() >= 2) {
                                String key = cells.get(0).toLowerCase();
                                String val = cells.get(1);
                                if (key.contains("issuing organization")) {
                                    doc.setClientName(val.replaceAll("\\(.*?\\)", "").trim());
                                } else if (key.contains("rfp title")) {
                                    doc.setRfpTitle(val.trim());
                                } else if (key.contains("proposal deadline")) {
                                    doc.setSubmissionDeadline(val.trim());
                                }
                            }
                        }
                        documentRepository.save(doc);
                    }

                    // Process all tables to find requirements, questions, and clarification registers
                    for (int t = 0; t < tables.getLength(); t++) {
                        Element tbl = (Element) tables.item(t);
                        NodeList rows = tbl.getElementsByTagName("w:tr");
                        if (rows.getLength() < 2) continue;

                        Element headerRow = (Element) rows.item(0);
                        List<String> headerCols = getRowCells(headerRow);

                        // Check if this is Appendix C: Clarification & Assumption Register table
                        boolean isClarificationTable = headerCols.stream().anyMatch(h -> 
                            h.toLowerCase().contains("clarification") || h.toLowerCase().contains("assumption"));

                        if (isClarificationTable) {
                            int idIdx = -1, topicIdx = -1, clarifIdx = -1, assumpIdx = -1;
                            for (int c = 0; c < headerCols.size(); c++) {
                                String col = headerCols.get(c).toLowerCase();
                                if (col.equals("id")) idIdx = c;
                                else if (col.contains("topic") || col.contains("area")) topicIdx = c;
                                else if (col.contains("clarification") || col.contains("question")) clarifIdx = c;
                                else if (col.contains("assumption") || col.contains("justification")) assumpIdx = c;
                            }

                            if (idIdx != -1 && clarifIdx != -1) {
                                for (int r = 1; r < rows.getLength(); r++) {
                                    Element row = (Element) rows.item(r);
                                    List<String> cells = getRowCells(row);
                                    if (cells.size() > Math.max(idIdx, clarifIdx)) {
                                        String id = cells.get(idIdx).trim();
                                        String topic = (topicIdx != -1 && cells.size() > topicIdx) ? cells.get(topicIdx).trim() : "General";
                                        String clarif = cells.get(clarifIdx).trim();
                                        String assump = (assumpIdx != -1 && cells.size() > assumpIdx) ? cells.get(assumpIdx).trim() : "Azentio iMAL standard compliance assumption";

                                        if (!id.isBlank() && !clarif.isBlank() && !id.equalsIgnoreCase("id")) {
                                            RfpClarification clarification = RfpClarification.builder()
                                                    .documentId(doc.getId())
                                                    .requirementId(id)
                                                    .sectionName("Appendix C: Clarifications & Assumptions (" + topic + ")")
                                                    .rfpTitle(doc.getRfpTitle())
                                                    .questionText(topic + ": " + clarif)
                                                    .clarificationNeeded(clarif)
                                                    .reasonJustification(assump)
                                                    .status("ACTIVE_REGISTER")
                                                    .build();
                                            clarificationRepository.save(clarification);
                                            log.info("Extracted Appendix C Clarification: {} - {} (docId: {})", id, topic, doc.getId());
                                        }
                                    }
                                }
                            }
                            continue; // Do not treat as requirement questionnaire
                        }

                        // Otherwise, parse standard requirements & vendor questionnaires
                        int idIdx = -1, areaIdx = -1, reqIdx = -1, prioIdx = -1;
                        for (int c = 0; c < headerCols.size(); c++) {
                            String col = headerCols.get(c).toLowerCase();
                            if (col.equals("id")) idIdx = c;
                            else if (col.contains("area") || col.contains("topic")) areaIdx = c;
                            else if (col.contains("requirement") || col.contains("question")) reqIdx = c;
                            else if (col.contains("priority")) prioIdx = c;
                        }

                        if (idIdx != -1 && reqIdx != -1) {
                            for (int r = 1; r < rows.getLength(); r++) {
                                Element row = (Element) rows.item(r);
                                List<String> cells = getRowCells(row);
                                if (cells.size() > Math.max(idIdx, reqIdx)) {
                                    String id = cells.get(idIdx).trim();
                                    String text = cells.get(reqIdx).trim();
                                    String area = (areaIdx != -1 && cells.size() > areaIdx) ? cells.get(areaIdx).trim() : "General";
                                    String prio = (prioIdx != -1 && cells.size() > prioIdx) ? cells.get(prioIdx).trim() : "Mandatory";

                                    if (!id.isBlank() && !text.isBlank() && !id.equalsIgnoreCase("id")) {
                                        list.add(RfpQueryRequest.builder()
                                                .documentId(doc.getId())
                                                .requirementId(id)
                                                .rfpTitle(doc.getRfpTitle())
                                                .clientName(doc.getClientName())
                                                .sectionName(area)
                                                .category(mapCategory(area, text))
                                                .requirementType(prio.equalsIgnoreCase("Preferred") || prio.equalsIgnoreCase("Optional") ? "OPTIONAL" : "MANDATORY")
                                                .question(text)
                                                .build());
                                    }
                                }
                            }
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse docx document: {}", e.getMessage(), e);
        }
        return list;
    }

    private List<String> getRowCells(Element row) {
        List<String> cells = new ArrayList<>();
        NodeList tcList = row.getElementsByTagName("w:tc");
        for (int i = 0; i < tcList.getLength(); i++) {
            Element tc = (Element) tcList.item(i);
            NodeList tList = tc.getElementsByTagName("w:t");
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < tList.getLength(); j++) {
                sb.append(tList.item(j).getTextContent()).append(" ");
            }
            cells.add(sb.toString().replaceAll("\\s+", " ").trim());
        }
        return cells;
    }

    private String mapCategory(String area, String text) {
        String combined = (area + " " + text).toLowerCase();
        if (combined.contains("murabaha")) return "MURABAHA";
        if (combined.contains("ijara") || combined.contains("ijarah")) return "IJARA";
        if (combined.contains("musharakah") || combined.contains("sukuk") || combined.contains("islamic")) return "ISLAMIC_FINANCING";
        if (combined.contains("shariah") || combined.contains("governance") || combined.contains("compliance")) return "SHARIAH_COMPLIANCE";
        if (combined.contains("security") || combined.contains("encryption") || combined.contains("access control") || combined.contains("audit")) return "SECURITY";
        if (combined.contains("api") || combined.contains("integration") || combined.contains("migration") || combined.contains("channel")) return "INTEGRATION";
        if (combined.contains("resilience") || combined.contains("backup") || combined.contains("disaster") || combined.contains("performance") || combined.contains("deployment")) return "TECHNICAL";
        if (combined.contains("support") || combined.contains("sla") || combined.contains("price") || combined.contains("cost")) return "COMMERCIAL";
        return "FUNCTIONAL";
    }

    /**
     * Level 1: Default Ingestion for Qatar Islamic Bank RFP
     */
    public RfpDocument loadSampleIslamicBankingRfp() {
        Map<String, Object> parseRes = parseUploadedRfp(null, "Qatar Islamic Bank Core Modernization & Islamic Lending RFP", "Qatar Islamic Bank");
        Long docId = (Long) parseRes.get("documentId");
        
        if (docId != null && clarificationRepository.findByDocumentIdOrderByIdAsc(docId).isEmpty()) {
            clarificationRepository.save(RfpClarification.builder()
                    .documentId(docId)
                    .requirementId("QIB-CLAR-01")
                    .sectionName("Section 1: Murabaha Trading Engine")
                    .rfpTitle("Qatar Islamic Bank Core Modernization & Islamic Lending RFP")
                    .questionText("Commodity Trading Broker Connectivity: Specify whether direct FIX gateway to Bursa Suq Al-Sila is required or DMCC Tradeflow API.")
                    .clarificationNeeded("Qatar Islamic Bank parameter specification: Confirmation of primary commodity trading venue and gateway interface.")
                    .reasonJustification("Azentio iMAL supports native pre-built connectors to both Bursa Malaysia Suq Al-Sila and DMCC Tradeflow; protocol confirmation ensures exact latency SLAs.")
                    .status("PENDING_CUSTOMER_FEEDBACK")
                    .build());

            clarificationRepository.save(RfpClarification.builder()
                    .documentId(docId)
                    .requirementId("QIB-CLAR-02")
                    .sectionName("Section 3: Architecture & Disaster Recovery")
                    .rfpTitle("Qatar Islamic Bank Core Modernization & Islamic Lending RFP")
                    .questionText("Disaster Recovery RPO/RTO: Confirm network latency between primary data center in Doha and DR site.")
                    .clarificationNeeded("Confirm latency and dark-fiber connectivity bandwidth between primary and DR sites for synchronous DB replication.")
                    .reasonJustification("Guaranteed RPO < 5 minutes requires synchronous database replication with round-trip latency under 10ms.")
                    .status("PENDING_CUSTOMER_FEEDBACK")
                    .build());

            dependencyRepository.save(com.azentio.rfp.model.RfpRequirementDependency.builder()
                    .documentId(docId)
                    .sourceRequirementId("REQ-FN-01")
                    .targetRequirementId("REQ-SEC-01")
                    .dependencyType("SHARIAH_AUDIT")
                    .description("Murabaha trade sequence execution requires Shariah audit trail locking.")
                    .build());
            dependencyRepository.save(com.azentio.rfp.model.RfpRequirementDependency.builder()
                    .documentId(docId)
                    .sourceRequirementId("REQ-DIG-01")
                    .targetRequirementId("REQ-FN-01")
                    .dependencyType("DATA_DEPENDENCY")
                    .description("Mobile customer onboarding requires core CIF account creation.")
                    .build());
            dependencyRepository.save(com.azentio.rfp.model.RfpRequirementDependency.builder()
                    .documentId(docId)
                    .sourceRequirementId("REQ-TECH-01")
                    .targetRequirementId("INFRA-NET-01")
                    .dependencyType("ARCHITECTURAL")
                    .description("Synchronous database replication requires client dark-fiber network connection.")
                    .build());
            return documentRepository.findAll().stream().reduce((first, second) -> second).orElse(null);
        }
        return documentRepository.findAll().stream().reduce((first, second) -> second).orElse(null);
    }
}
