package com.azentio.rfp.repository;

public interface KnowledgeMatchProjection {
    Long getId();
    String getCategory();
    String getQuestion();
    String getApprovedAnswer();
    String getSourceDocument();
    String getSectionReference();
    Double getSimilarityScore();
}
