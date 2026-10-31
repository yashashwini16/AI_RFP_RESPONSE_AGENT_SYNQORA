-- =========================================================================
-- AZENTIO ENTERPRISE RFP KNOWLEDGE VAULT - 15 REALISTIC BANKING Q&As
-- Seed data for Teammate 2: Run directly in PostgreSQL or use Ingest API
-- =========================================================================

INSERT INTO rfp_knowledge_base (category, question, approved_answer, source_document, section_reference) VALUES
-- 1. Security & Key Management
('SECURITY', 'How are cryptographic keys and master database encryption keys managed?',
 'Azentio integrates natively with FIPS 140-2 Level 3 certified Hardware Security Modules (HSM) such as Thales Luna and AWS CloudHSM. Master keys undergo automated 90-day envelope rotation with dual-custody authorization and zero plaintext key leakage.',
 'Azentio_KMS_Integration_Guide.pdf', 'Sec 3.2.1'),

-- 2. Audit Trails & Non-repudiation
('SECURITY', 'What audit trail logging mechanisms exist for privileged database administrators and banking operators?',
 'Every user interaction, transaction approval, and schema access is captured in an append-only, tamper-evident audit ledger with WORM (Write Once Read Many) storage support, recording timestamp (UTC), IP, user certificate DN, and before-and-after values.',
 'Security_Compliance_Audit_Whitepaper.pdf', 'Sec 8.1'),

-- 3. Business Continuity & RPO/RTO
('ARCHITECTURE', 'What are the guaranteed RPO (Recovery Point Objective) and RTO (Recovery Time Objective) during disaster recovery?',
 'For multi-region hybrid cloud deployments, Azentio guarantees a Recovery Point Objective (RPO) of < 5 minutes via synchronous storage replication and a Recovery Time Objective (RTO) of < 15 minutes utilizing automated DNS failover.',
 'Disaster_Recovery_SLA_2025.pdf', 'Sec 2.4'),

-- 4. Cloud Portability & Containerization
('ARCHITECTURE', 'Is the banking suite containerized and certified on Red Hat OpenShift and AWS EKS?',
 'Yes, all Azentio core microservices are packaged as OCI-compliant containers and certified on Red Hat OpenShift 4.14+, AWS EKS, and Microsoft Azure AKS with native Helm chart auto-scaling.',
 'Cloud_Native_Deployment_Architecture.pdf', 'Sec 1.2'),

-- 5. Database & Oracle PL/SQL Optimization
('DATABASE', 'How does Azentio maximize transaction throughput on Oracle Enterprise Databases?',
 'Azentio utilizes native Oracle 19c/23ai features including Advanced Compression, Real Application Clusters (RAC), In-Memory Option, and heavily optimized PL/SQL bulk collection packages reducing network round-trips by up to 60%.',
 'Azentio_Oracle_Optimization_Benchmark.pdf', 'Sec 4.3'),

-- 6. Disaster Recovery Testing
('ARCHITECTURE', 'How frequently are disaster recovery drill simulations conducted and verified without production downtime?',
 'Disaster recovery failover tests are performed on a quarterly schedule using read-only standby snapshots and isolated network VPCs, ensuring 100% operational readiness without disrupting production transactions.',
 'Business_Continuity_Plan_2025.pdf', 'Sec 6.0'),

-- 7. Anti-Money Laundering & Sanctions Screening
('COMPLIANCE', 'Does the platform provide real-time transaction screening against OFAC and UN sanction lists?',
 'Yes, the embedded Azentio Sentinel AML module executes real-time fuzzy matching across OFAC, EU, UN, and PEP watchlists with sub-30 millisecond latency per transaction, featuring automated true-positive alert escalation.',
 'Sentinel_AML_Product_Specification.pdf', 'Sec 3.1'),

-- 8. Open Banking & ISO 20022 Compliance
('INTEGRATION', 'Does the solution support ISO 20022 MX messaging standards and SWIFT CBPR+ migration?',
 'Azentio provides native bi-directional transformation between legacy SWIFT MT and modern ISO 20022 MX message formats (pacs.008, pacs.009, camt.053), fully compliant with SWIFT CBPR+ guidelines.',
 'SWIFT_ISO20022_Migration_Guide.pdf', 'Sec 5.2'),

-- 9. Scalability & End-of-Day Batch
('PERFORMANCE', 'How long does the End-of-Day (EOD) batch processing take for 5 million active accounts?',
 'Through multi-threaded parallel partitioning and in-memory ledger batching, the core EOD batch for 5 million accounts completes within 42 minutes, well within the standard 2-hour banking maintenance window.',
 'Core_Banking_Performance_Benchmark_2025.pdf', 'Sec 7.1'),

-- 10. Multi-Tenancy & Data Isolation
('ARCHITECTURE', 'How does Azentio ensure logical and physical data isolation for multi-entity regional banks?',
 'Azentio supports both shared-database multi-schema isolation and dedicated database instances per legal entity, enforced via Row-Level Security (RLS) and cryptographic tenant boundary tags.',
 'Multi_Entity_Banking_Architecture.pdf', 'Sec 2.0');
