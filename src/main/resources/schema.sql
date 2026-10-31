-- ========================================================
-- AI RFP RESPONSE AGENT - DATABASE SCHEMA (Azentio Enterprise)
-- ========================================================

-- Enable vector extension
CREATE EXTENSION IF NOT EXISTS vector;

DROP TABLE IF EXISTS rfp_audit_log CASCADE;
DROP TABLE IF EXISTS rfp_responses CASCADE;
DROP TABLE IF EXISTS rfp_queries CASCADE;
DROP TABLE IF EXISTS rfp_knowledge_base CASCADE;

-- 1. Historical RFP Knowledge Base (The company's past answers)
CREATE TABLE rfp_knowledge_base (
    id SERIAL PRIMARY KEY,
    category VARCHAR(50) NOT NULL,            -- SECURITY, COMPLIANCE, ARCHITECTURE, PRICING, DATABASE, INTEGRATION
    question TEXT NOT NULL,
    approved_answer TEXT NOT NULL,
    source_document VARCHAR(100),              -- e.g. "Bank_Doha_RFP_2024.pdf"
    section_reference VARCHAR(50),             -- e.g. "Section 3.4.1"
    embedding vector(1536),                    -- Embedding vector for semantic search
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Incoming RFP Queries (The new questions to answer)
CREATE TABLE rfp_queries (
    id SERIAL PRIMARY KEY,
    rfp_title VARCHAR(100) NOT NULL,           -- e.g. "Qatar National Bank Core Upgrade"
    client_name VARCHAR(100),
    question_text TEXT NOT NULL,
    category VARCHAR(50),
    status VARCHAR(20) DEFAULT 'PENDING'       -- PENDING, DRAFTED, REVIEWED, APPROVED, NEEDS_REVIEW
);

-- 3. AI Generated Responses
CREATE TABLE rfp_responses (
    id SERIAL PRIMARY KEY,
    query_id INT REFERENCES rfp_queries(id) ON DELETE CASCADE,
    drafted_answer TEXT NOT NULL,
    confidence_score INT NOT NULL,             -- 0 to 100
    source_citations JSONB,                    -- List of historical RFPs used
    gap_flag BOOLEAN DEFAULT FALSE,            -- TRUE if knowledge was insufficient
    reviewed_answer TEXT,                      -- Edited by human engineer
    is_approved BOOLEAN DEFAULT FALSE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Audit & Latency Log
CREATE TABLE rfp_audit_log (
    id SERIAL PRIMARY KEY,
    query_id INT,
    model_used VARCHAR(50),
    prompt_tokens INT,
    completion_tokens INT,
    latency_ms INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for Vector Search Cosine Distance
CREATE INDEX IF NOT EXISTS idx_rfp_kb_embedding ON rfp_knowledge_base USING hnsw (embedding vector_cosine_ops);

-- ========================================================
-- SEED DATA (Azentio Enterprise Banking Sample Q&As)
-- ========================================================
INSERT INTO rfp_knowledge_base (category, question, approved_answer, source_document, section_reference) VALUES
('SECURITY', 'How is data encrypted at rest and in transit?', 
 'All data at rest is encrypted using AES-256 GCM encryption across all storage tiers including tablespaces, redo logs, and database backups with HSM integration. Data in transit is secured using TLS 1.3 with strict forward secrecy and mutual certificate authentication (mTLS).', 
 'Azentio_Security_Whitepaper_2025.pdf', 'Sec 4.2'),

('ARCHITECTURE', 'Does the platform support high availability and multi-region failover?', 
 'Yes, the Azentio Core Banking system architecture supports active-passive and active-active clustering across multi-availability zones with an RPO of under 5 minutes and an RTO of under 15 minutes, orchestrated using Kubernetes and automated DNS failover.', 
 'iMAL_Cloud_Architecture_Guide.pdf', 'Sec 2.1'),

('COMPLIANCE', 'Is the software compliant with ISO 27001 and SOC 2 Type II?', 
 'Yes, Azentio platforms maintain annual SOC 2 Type II certifications across all five trust service criteria (Security, Availability, Processing Integrity, Confidentiality, and Privacy) and comply fully with ISO/IEC 27001:2022 standards.', 
 'Compliance_Summary_Report_2024.pdf', 'Sec 1.0'),

('DATABASE', 'Does Azentio support Oracle Database and PL/SQL stored procedures?', 
 'Yes, Azentio iMAL and Core platforms natively support Oracle Database 19c and 23ai enterprise editions, including Oracle Real Application Clusters (RAC), Exadata engineered systems, and heavily optimized PL/SQL package architectures.', 
 'Azentio_Database_Compatibility_Matrix.pdf', 'Sec 3.1'),

('PERFORMANCE', 'What is the benchmarked TPS (Transactions Per Second) and concurrent user capacity?', 
 'The platform is benchmarked to sustain over 12,000 online banking TPS and 35,000 batch EOD transactions per minute with sub-80 millisecond 99th percentile response times, scaling to 100,000 concurrent sessions.', 
 'Core_Banking_Performance_Benchmark_2025.pdf', 'Sec 5.4'),

('INTEGRATION', 'How does the platform integrate with core banking APIs and legacy switch networks?', 
 'Azentio provides over 400+ pre-built, ISO 20022 and BIAN-compliant REST/JSON APIs, as well as native connectors for SWIFT MT/MX messaging, ISO 8583 payment card switches, and Kafka-based event streaming.', 
 'Azentio_Open_Banking_Integration_Spec.pdf', 'Sec 2.3');
