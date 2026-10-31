package com.azentio.rfp.repository;

public interface KnowledgeMatchProjection {
    Long getId();
    String getCategory();
    String getSourceType();
    String getProductVersion();
    String getQuestion();
    String getApprovedAnswer();
    String getSourceDocument();
    String getSectionReference();
    Double getSimilarityScore();
}
