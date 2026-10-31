-- =========================================================================
-- AZENTIO AI RFP RESPONSE AGENT (AGENT 2) - DATABASE SCHEMA
-- Specialized for Islamic Banking & Lending Proposals (Azentio iMAL Suite)
-- =========================================================================

CREATE EXTENSION IF NOT EXISTS vector;

-- 1. Historical Knowledge Vault (3 Grounded Sources: PRODUCT_DOC, PAST_SOW, PAST_RFP)
CREATE TABLE IF NOT EXISTS rfp_knowledge_base (
    id SERIAL PRIMARY KEY,
    category VARCHAR(50) NOT NULL,            -- SHARIAH_COMPLIANCE, MURABAHA, IJARA, MUDARABA, SUKUK, ARCHITECTURE, SECURITY
    source_type VARCHAR(30) NOT NULL,         -- PRODUCT_DOC, PAST_SOW, PAST_RFP
    question TEXT NOT NULL,
    approved_answer TEXT NOT NULL,
    source_document VARCHAR(100) NOT NULL,     -- e.g. "iMAL_Murabaha_Product_Guide_v15.pdf"
    section_reference VARCHAR(50),             -- e.g. "Sec 4.2.1"
    product_version VARCHAR(20) DEFAULT 'iMAL v15.0',
    embedding vector(1536),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Incoming RFP Documents & Formatting Guidelines
CREATE TABLE IF NOT EXISTS rfp_documents (
    id SERIAL PRIMARY KEY,
    filename VARCHAR(150) NOT NULL,
    rfp_title VARCHAR(150) NOT NULL,
    client_name VARCHAR(100) NOT NULL,
    submission_deadline VARCHAR(50),
    formatting_rules JSONB,                   -- {"font": "Arial", "max_words": 200, "numbering": "section_based"}
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Incoming RFP Questions / Requirements
CREATE TABLE IF NOT EXISTS rfp_queries (
    id SERIAL PRIMARY KEY,
    document_id INT REFERENCES rfp_documents(id) ON DELETE CASCADE,
    rfp_title VARCHAR(100) NOT NULL,
    client_name VARCHAR(100),
    section_name VARCHAR(100) DEFAULT 'General Requirements',
    question_text TEXT NOT NULL,
    category VARCHAR(50),
    requirement_type VARCHAR(20) DEFAULT 'MANDATORY', -- MANDATORY, OPTIONAL, INFO
    compliance_status VARCHAR(20) DEFAULT 'COMPLIANT', -- COMPLIANT, PARTIALLY_COMPLIANT, NON_COMPLIANT, CLARIFICATION
    guardrail_flag VARCHAR(50) DEFAULT 'TECHNICAL',   -- TECHNICAL, LEGAL_REVIEW, PRICING_DESK, SLA_COMMITMENT
    assigned_owner VARCHAR(50) DEFAULT 'SE_TECHNICAL',
    status VARCHAR(20) DEFAULT 'PENDING'              -- PENDING, DRAFTED, REVIEWED, APPROVED, NEEDS_REVIEW
);

-- 4. AI Generated Proposal Responses & Citations
CREATE TABLE IF NOT EXISTS rfp_responses (
    id SERIAL PRIMARY KEY,
    query_id INT REFERENCES rfp_queries(id) ON DELETE CASCADE,
    drafted_answer TEXT NOT NULL,
    confidence_score INT NOT NULL,                    -- 0 to 100
    source_citations JSONB,                           -- List of sources with source_type & version
    gap_flag BOOLEAN DEFAULT FALSE,
    guardrail_reason TEXT,                            -- Explanation if legal/pricing flagged
    reviewed_answer TEXT,
    is_approved BOOLEAN DEFAULT FALSE,
    outdated_warning TEXT,                            -- If source version is old
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Customer Clarification Queries List (Consolidated & Deduplicated)
CREATE TABLE IF NOT EXISTS rfp_clarifications (
    id SERIAL PRIMARY KEY,
    query_id INT REFERENCES rfp_queries(id) ON DELETE CASCADE,
    question_text TEXT NOT NULL,
    clarification_needed TEXT NOT NULL,
    reason_justification TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. Conversational Memory (SE Chat Collaboration & Rewrites)
CREATE TABLE IF NOT EXISTS rfp_chat_messages (
    id SERIAL PRIMARY KEY,
    session_id VARCHAR(50) NOT NULL,
    query_id INT,
    sender VARCHAR(20) NOT NULL,                      -- SE, AGENT
    message TEXT NOT NULL,
    action_type VARCHAR(50),                          -- REWRITE, APPROVAL, CLARIFICATION, EXPLANATION
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. Audit & Latency Log
CREATE TABLE IF NOT EXISTS rfp_audit_log (
    id SERIAL PRIMARY KEY,
    query_id INT,
    model_used VARCHAR(50),
    prompt_tokens INT,
    completion_tokens INT,
    latency_ms INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for HNSW Vector Cosine Distance
CREATE INDEX IF NOT EXISTS idx_rfp_kb_embedding ON rfp_knowledge_base USING hnsw (embedding vector_cosine_ops);

-- =========================================================================
-- LEVEL 2: COMPLIANCE, GAP ANALYSIS & COLLABORATION EXTENSIONS
-- =========================================================================

ALTER TABLE rfp_queries ADD COLUMN IF NOT EXISTS response_code VARCHAR(10);
ALTER TABLE rfp_queries ADD COLUMN IF NOT EXISTS response_code_desc VARCHAR(150);
ALTER TABLE rfp_queries ADD COLUMN IF NOT EXISTS multi_product_components TEXT;
ALTER TABLE rfp_queries ADD COLUMN IF NOT EXISTS dependencies TEXT;
ALTER TABLE rfp_queries ADD COLUMN IF NOT EXISTS ambiguity_flag BOOLEAN DEFAULT FALSE;
ALTER TABLE rfp_queries ADD COLUMN IF NOT EXISTS ambiguity_notes TEXT;
ALTER TABLE rfp_queries ADD COLUMN IF NOT EXISTS gap_type VARCHAR(50) DEFAULT 'NONE';
ALTER TABLE rfp_queries ADD COLUMN IF NOT EXISTS gap_nature VARCHAR(50) DEFAULT 'NONE';
ALTER TABLE rfp_queries ADD COLUMN IF NOT EXISTS assigned_department VARCHAR(50) DEFAULT 'SALES_ENGINEERING';
ALTER TABLE rfp_queries ADD COLUMN IF NOT EXISTS requires_estimate BOOLEAN DEFAULT FALSE;
ALTER TABLE rfp_queries ADD COLUMN IF NOT EXISTS estimate_details VARCHAR(100);
ALTER TABLE rfp_queries ADD COLUMN IF NOT EXISTS requires_attachment BOOLEAN DEFAULT FALSE;
ALTER TABLE rfp_queries ADD COLUMN IF NOT EXISTS partner_dependency VARCHAR(150);
ALTER TABLE rfp_queries ADD COLUMN IF NOT EXISTS validation_status VARCHAR(50) DEFAULT 'PASSED';
ALTER TABLE rfp_queries ADD COLUMN IF NOT EXISTS validation_issues TEXT;
ALTER TABLE rfp_queries ADD COLUMN IF NOT EXISTS human_validation_required BOOLEAN DEFAULT FALSE;

ALTER TABLE rfp_clarifications ADD COLUMN IF NOT EXISTS working_assumption TEXT;
ALTER TABLE rfp_clarifications ADD COLUMN IF NOT EXISTS impact_priority VARCHAR(20) DEFAULT 'MEDIUM';
ALTER TABLE rfp_clarifications ADD COLUMN IF NOT EXISTS impact_area VARCHAR(50) DEFAULT 'SOLUTION_DESIGN';
ALTER TABLE rfp_clarifications ADD COLUMN IF NOT EXISTS cluster_group VARCHAR(100);

-- Customer Compliance Code Schemes (S/C/D/T/N or custom)
CREATE TABLE IF NOT EXISTS rfp_compliance_code_schemes (
    id SERIAL PRIMARY KEY,
    scheme_key VARCHAR(50) NOT NULL,
    code VARCHAR(10) NOT NULL,
    classification VARCHAR(100) NOT NULL,
    description TEXT,
    requires_explanation BOOLEAN DEFAULT FALSE,
    requires_estimate BOOLEAN DEFAULT FALSE,
    requires_attachment BOOLEAN DEFAULT FALSE
);

-- Requirement Inter-Dependencies
CREATE TABLE IF NOT EXISTS rfp_requirement_dependencies (
    id SERIAL PRIMARY KEY,
    document_id INT,
    source_requirement_id VARCHAR(50) NOT NULL,
    target_requirement_id VARCHAR(50) NOT NULL,
    dependency_type VARCHAR(50) NOT NULL,
    description TEXT
);

