-- =========================================================================
-- AZENTIO iMAL ISLAMIC BANKING & LENDING KNOWLEDGE VAULT
-- 3 Grounded Sources: PRODUCT_DOC, PAST_SOW, PAST_RFP
-- Includes Shariah Terms, AAOIFI Compliance, Redacted Customer Names & Versioning
-- =========================================================================

DELETE FROM rfp_knowledge_base;

INSERT INTO rfp_knowledge_base (category, source_type, product_version, question, approved_answer, source_document, section_reference) VALUES

-- [SOURCE 1: PRODUCT_DOC] Murabaha Financing Engine
('MURABAHA', 'PRODUCT_DOC', 'iMAL v15.0',
 'Does the platform support automated Murabaha commodity broker integration and Shariah-compliant asset ownership verification?',
 'Azentio iMAL v15 provides native automated integration with certified commodity brokers (including Bursa Suq Al-Sila and DMCC Tradeflow) for automated Murabaha execution. The system enforces strict Shariah asset sequence validation: verification of bank legal title, possession, and non-binding promise (Waad) prior to offer and acceptance (Ijab wa Qabul).',
 'iMAL_Islamic_Financing_Product_Specification_v15.pdf', 'Sec 4.1.2'),

-- [SOURCE 1: PRODUCT_DOC] Ijara wa Iqtina (Leasing to Own)
('IJARA', 'PRODUCT_DOC', 'iMAL v15.0',
 'How does iMAL manage Ijara asset ownership, insurance takaful, and variable profit rate repricing?',
 'Under iMAL Ijara wa Iqtina module, the underlying asset remains on the bank balance sheet throughout the lease tenure. Takaful insurance premiums and major maintenance expenses are automatically apportioned to the lessor according to AAOIFI Shariah Standard No. 9. Repricing schedules support forward-looking benchmark rates (SOFR/EIBOR) with Shariah-mandated profit caps and floors.',
 'iMAL_Ijara_Product_Architecture_Guide.pdf', 'Sec 3.4'),

-- [SOURCE 1: PRODUCT_DOC] Mudaraba / Musharaka Profit-Sharing Pools
('MUDARABA', 'PRODUCT_DOC', 'iMAL v15.0',
 'How does the platform handle Islamic profit calculation, pool distribution, and Profit Equalization Reserves (PER)?',
 'iMAL features an automated Islamic Profit Calculation & Distribution engine supporting both unrestricted (Mudaraba Mutlaqa) and restricted investment pools. The engine automatically computes gross asset yields, calculates Profit Equalization Reserves (PER) to smooth customer returns, and Investment Risk Reserves (IRR) to mitigate future capital impairment, strictly complying with IFSB-15 and AAOIFI standards.',
 'iMAL_Islamic_Treasury_and_Pool_Distribution.pdf', 'Sec 6.2'),

-- [SOURCE 1: PRODUCT_DOC] AAOIFI & Shariah Supervisory Board Audit
('SHARIAH_COMPLIANCE', 'PRODUCT_DOC', 'iMAL v15.0',
 'Does the solution provide an immutable Shariah audit trail and separate non-halal income purification accounting?',
 'Yes, iMAL maintains a dedicated, immutable Shariah Audit Trail ledger. Any irregular income derived from operational non-compliance is automatically segregated into a designated Charity/Purification Suspense Account, preventing co-mingling with core bank income in accordance with AAOIFI Governance Standard No. 1.',
 'Azentio_Shariah_Governance_Framework.pdf', 'Sec 2.1'),

-- [SOURCE 1: PRODUCT_DOC] Sukuk Management & Issuance
('SUKUK', 'PRODUCT_DOC', 'iMAL v15.0',
 'What types of Sukuk instruments are natively supported in the treasury module?',
 'Azentio iMAL Treasury natively supports origination, issuance, coupon distribution, and redemption for Sukuk Al-Ijara, Sukuk Al-Murabaha, Sukuk Al-Wakalah, and Hybrid Sukuk structures with automated custody reconciliation and secondary market trading.',
 'iMAL_Sukuk_Treasury_Manual.pdf', 'Sec 7.3'),

-- [SOURCE 2: PAST_SOW] Emirates Islamic Core Transformation (Redacted Client)
('MURABAHA', 'PAST_SOW', 'iMAL v14.5',
 'What is the end-to-end processing SLA for real-time digital Islamic consumer lending disbursement?',
 'As successfully delivered in the [REDACTED_TIER1_ISLAMIC_BANK] Core Transformation Scope of Work (Phase 2), Azentio iMAL delivers automated straight-through processing (STP) for digital Murabaha personal financing from mobile submission to account disbursement within 180 seconds, including automated credit bureau (AECB) checks and digital contracts.',
 'SOW_Tier1_UAE_Islamic_Bank_Transformation.pdf', 'Sec 3.1.4 (SOW Delivery Scope)'),

-- [SOURCE 2: PAST_SOW] Trade Finance Islamic Letters of Credit (Redacted Client)
('TRADE_FINANCE', 'PAST_SOW', 'iMAL v15.0',
 'How does the system support Islamic Letters of Credit under Murabaha and Musharaka?',
 'In accordance with the [REDACTED_GCC_ISLAMIC_BANK] Trade Modernization SOW, iMAL supports Wakala-based and Murabaha-based Import Letters of Credit (LC), handling maritime bill of lading asset title transfers, custom shipping documents, and post-import financing conversion with zero interest accrual.',
 'SOW_GCC_Regional_Islamic_Trade_Finance.pdf', 'Sec 5.2 (Deliverable 4B)'),

-- [SOURCE 2: PAST_SOW] Multi-Entity Islamic Banking Setup
('ARCHITECTURE', 'PAST_SOW', 'iMAL v14.8',
 'Can the system support an Islamic window within a conventional banking group with complete ledger separation?',
 'Per [REDACTED_SOUTHEAST_ASIA_BANK] Islamic Window Implementation SOW, iMAL enforces total balance sheet and treasury segregation between conventional and Islamic operations, including segregated clearing accounts, separate chart of accounts (COA), and zero co-mingling of liquidity reserves.',
 'SOW_Islamic_Window_Segregation_Project.pdf', 'Sec 1.4'),

-- [SOURCE 3: PAST_RFP] QNB Islamic RFP Response (Redacted)
('COMPLIANCE', 'PAST_RFP', 'iMAL v15.0',
 'Are all Islamic banking transactions audited and certified by an independent Shariah Advisory Board?',
 'Azentio iMAL workflows, accounting entries, and contractual templates undergo continuous review and annual certification by internationally recognized Shariah advisory boards, verifying adherence to AAOIFI, IFSB, and Central Bank Islamic regulatory mandates.',
 'RFP_Resp_Qatar_National_Islamic_2024.pdf', 'RFP Clause 3.2.1'),

-- [SOURCE 3: PAST_RFP] Dubai Islamic RFP Response
('LENDING', 'PAST_RFP', 'iMAL v15.0',
 'Does the platform support diminishing Musharaka (Musharaka Mutanaqisa) for Islamic home finance and commercial mortgages?',
 'Yes, Azentio iMAL has full native support for Diminishing Musharaka (Musharaka Mutanaqisa) financing. The engine dynamically calculates periodic unit purchase schedules, rental recalculations, customer buyout options, and dual title deed registrations with automated amortization adjustment.',
 'RFP_Resp_Dubai_Islamic_Mortgage_2024.pdf', 'RFP Sec 4.8'),

-- [SOURCE 3: PAST_RFP - OUTDATED VERSION FOR GUARDRAIL DEMO]
('LEGACY_TECH', 'PAST_RFP', 'iMAL v12.1',
 'What underlying application server and messaging broker does the legacy core operate on?',
 'The legacy core operates on IBM WebSphere Application Server 8.5 with point-to-point MQ messaging.',
 'Legacy_Islamic_Proposal_2018.pdf', 'Sec 2.0 (Outdated)');
