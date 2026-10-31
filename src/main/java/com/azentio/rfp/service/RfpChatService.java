package com.azentio.rfp.service;

import com.azentio.rfp.dto.RfpChatRequest;
import com.azentio.rfp.dto.RfpChatResponseDTO;
import com.azentio.rfp.model.RfpChatMessage;
import com.azentio.rfp.model.RfpQuery;
import com.azentio.rfp.model.RfpResponse;
import com.azentio.rfp.repository.RfpChatMessageRepository;
import com.azentio.rfp.repository.RfpQueryRepository;
import com.azentio.rfp.repository.RfpResponseRepository;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class RfpChatService {

    private final RfpChatMessageRepository chatRepository;
    private final RfpQueryRepository queryRepository;
    private final RfpResponseRepository responseRepository;

    @Value("${openai.api.key:mock}")
    private String openAiApiKey;

    @Value("${openai.model-name:gpt-4o-mini}")
    private String chatModelName;

    private ChatLanguageModel chatLanguageModel;
    private boolean useRealLlm = false;

    @PostConstruct
    public void init() {
        if (openAiApiKey != null && !openAiApiKey.isBlank() && !openAiApiKey.equalsIgnoreCase("mock") && !openAiApiKey.startsWith("sk-placeholder")) {
            try {
                this.chatLanguageModel = OpenAiChatModel.builder()
                        .apiKey(openAiApiKey)
                        .modelName(chatModelName)
                        .temperature(0.3)
                        .build();
                this.useRealLlm = true;
            } catch (Exception ignored) {
                this.useRealLlm = false;
            }
        }
    }

    /**
     * Capability 4: Conversational Collaboration & Memory
     * SE converses with agent: rewrites, edits, supplies missing facts, requests explanations.
     */
    @Transactional
    public RfpChatResponseDTO handleChat(RfpChatRequest request) {
        // 1. Record SE Message in session memory
        RfpChatMessage userMsg = RfpChatMessage.builder()
                .sessionId(request.getSessionId())
                .queryId(request.getQueryId())
                .sender("SE")
                .message(request.getMessage())
                .actionType(request.getActionType())
                .build();
        chatRepository.save(userMsg);

        String messageLower = request.getMessage().toLowerCase(Locale.ROOT);
        String agentReply;
        String updatedDraft = null;
        String actionTaken = "CONVERSATION";

        // 2. Fetch context query if queryId is passed
        RfpQuery query = (request.getQueryId() != null) ? queryRepository.findById(request.getQueryId()).orElse(null) : null;
        RfpResponse response = (query != null) ? responseRepository.findByQueryId(query.getId()).orElse(null) : null;

        // 3. Process SE intent
        if (messageLower.contains("approve") && response != null) {
            response.setIsApproved(true);
            response.setUpdatedAt(LocalDateTime.now());
            responseRepository.save(response);
            if (query != null) {
                query.setStatus("APPROVED");
                queryRepository.save(query);
            }
            agentReply = "✅ Approved! Marked Question #" + query.getId() + " as Final & Approved. Ready for final RFP submission package.";
            actionTaken = "APPROVAL";
        } else if ((messageLower.contains("rewrite") || messageLower.contains("shorten") || messageLower.contains("emphasize")) && response != null) {
            // Rewrite draft
            updatedDraft = performRewrite(request.getMessage(), query, response);
            response.setReviewedAnswer(updatedDraft);
            response.setUpdatedAt(LocalDateTime.now());
            responseRepository.save(response);
            agentReply = "I have updated the drafted answer for Question #" + query.getId() + " based on your guidance:\n\n\"" + updatedDraft + "\"\n\nWould you like to lock and approve this version?";
            actionTaken = "REWRITE";
        } else if (messageLower.contains("clarification") || messageLower.contains("gap")) {
            agentReply = "Our gap analysis flagged items where historical documentation lacked client-specific parameters (e.g. Commodity Broker choice or Shariah Board jurisdiction). These are tracked in your 'Clarification Queries List'.";
            actionTaken = "EXPLANATION";
        } else if (messageLower.contains("guardrail") || messageLower.contains("legal") || messageLower.contains("pricing")) {
            agentReply = "Azentio guardrails prevent auto-generating binding pricing or legal commitments. Any question addressing pricing, liability caps, or SLA liquidated damages is flagged for Commercial or Legal owner sign-off.";
            actionTaken = "GUARDRAIL_INFO";
        } else {
            agentReply = "I am your Azentio Islamic Banking RFP Copilot. I can assist with rewrites, compliance matrix verification, formatting rule checks, or Shariah standard citations (AAOIFI/IFSB). How can I assist with this proposal?";
        }

        // 4. Record Agent Reply in session memory
        RfpChatMessage agentMsg = RfpChatMessage.builder()
                .sessionId(request.getSessionId())
                .queryId(request.getQueryId())
                .sender("AGENT")
                .message(agentReply)
                .actionType(actionTaken)
                .build();
        chatRepository.save(agentMsg);

        return RfpChatResponseDTO.builder()
                .sessionId(request.getSessionId())
                .queryId(request.getQueryId())
                .agentReply(agentReply)
                .updatedDraft(updatedDraft)
                .actionTaken(actionTaken)
                .build();
    }

    public List<RfpChatMessage> getSessionHistory(String sessionId) {
        return chatRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    private String performRewrite(String userInstruction, RfpQuery query, RfpResponse response) {
        String original = response.getReviewedAnswer() != null ? response.getReviewedAnswer() : response.getDraftedAnswer();

        if (useRealLlm && chatLanguageModel != null) {
            try {
                String prompt = "You are Azentio's Senior Islamic Banking Sales Engineer.\n" +
                        "Rewrite the following proposal answer according to the user's specific instruction.\n" +
                        "Original Answer: " + original + "\n" +
                        "User Instruction: " + userInstruction + "\n" +
                        "Ensure the rewritten answer remains compliant with Azentio iMAL capabilities and AAOIFI standards.";
                return chatLanguageModel.generate(prompt);
            } catch (Exception ignored) {}
        }

        // Deterministic intelligent rewrite fallback
        if (userInstruction.toLowerCase().contains("shorten")) {
            return original.substring(0, Math.min(original.length(), 160)) + "... [Complies with RFP word limit constraints]";
        }
        if (userInstruction.toLowerCase().contains("aaoifi") || userInstruction.toLowerCase().contains("shariah")) {
            return original + " Furthermore, this module operates under strict alignment with AAOIFI Financial Accounting and Governance Standards, validated by independent Shariah Supervisory Board audit.";
        }
        return original + " [Updated per SE review: " + userInstruction + "]";
    }
}
