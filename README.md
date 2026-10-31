# Azentio Agent 2: AI RFP Response Agent (Islamic Banking & Lending)
> **Azentio GTM AI Hackathon — Challenge B Solution**  
> *Target Product Suite: Azentio iMAL Core Banking & Islamic Lending Suite*

---

## Executive Overview & Purpose

The **RFP Agent** is an enterprise copilot built specifically for **Sales Engineers (SEs)** responding to complex **Islamic Banking and Lending RFPs**. 

Instead of manually digging through 200-page proposal archives and product manuals, the SE uploads an RFP in chat, and the agent works with them question by question:
1. **Parses & Triages** the RFP sections, requirements, and formatting rules.
2. **Retrieves Grounded Knowledge** from **3 distinct sources**:
   - 📘 **Product Documentation** (Azentio iMAL product guides, Murabaha engine, Ijara, Mudaraba, AAOIFI governance)
   - 📄 **Past Scopes of Work (SOWs)** (Redacted Tier-1 banking transformation deliverables)
   - 📑 **Past RFP Responses** (Audited and certified historical proposal answers)
3. **Enforces Enterprise Guardrails**:
   -  **SE in Control**: Every answer remains a draft until explicitly reviewed and approved by the Sales Engineer.
   -  **Commercial & Legal Triage**: Pricing, license fees, SLA liquidated damages, and contractual commitments are **never** auto-generated; they are flagged for the Commercial Desk or Legal Counsel.
   -  **Confidentiality / Client Redaction**: Past customer bank names are sanitized (`[REDACTED_TIER1_ISLAMIC_BANK]`).
   -  **Source Quality Warning**: Flags when knowledge originates from outdated product versions (e.g. iMAL v12/v13 vs current v15.0).
4. **Clarification Query Compilation**: Generates a consolidated, deduplicated list of questions to send to the client with technical justification instead of inventing assumptions.
5. **Generates Full Compliance Matrix**: Compliant (`C`), Partially Compliant (`PC`), Non-Compliant (`NC`), Clarification Needed (`CL`).
6. **Conversational Collaboration & Memory**: SE instructs the agent in chat ("Rewrite to emphasize AAOIFI compliance", "Shorten answer to 200 words", "Approve answer").

---

##  Architecture & Tech Stack

```mermaid
flowchart TD
    A[Sales Engineer / Client RFP Upload] --> B[RfpParserService & Triage]
    B --> C{Guardrail Evaluation}
    C -->|Commercial / Legal| D[Route to Commercial / Legal Owner]
    C -->|Technical| E[PgVector HNSW Semantic Retrieval]
    E --> F[(PostgreSQL Knowledge Vault\n3 Grounded Sources)]
    F --> G[LangChain4j RAG Agentic Synthesizer]
    G --> H[Confidence Scoring & Gap Analysis]
    H -->|Confidence < 50%| I[Customer Clarification Queries List]
    H -->|Confidence >= 50%| J[Drafted Proposal Answer + Citations]
    J --> K[SE Chat Collaboration & Rewrites]
    K --> L[Approval & Continuous Learning Loop]
    L --> M[One-Click Export: Formatted RFP + Compliance Matrix]
```

- **Backend**: Spring Boot 3.3.4 (Java 21 LTS)
- **Vector Database**: PostgreSQL 16 + PgVector 0.8.7 (`vector(1536)` with HNSW cosine distance `<=>`)
- **Agent Orchestration**: LangChain4j 0.36.2 + OpenAI (`gpt-4o-mini`) with offline deterministic n-gram fallback
- **Frontend**: React + Tailwind CSS Sales Engineering Workbench
- **Persistence**: Spring Data JPA + JSONB citations + Session chat memory

---

## 📡 Key REST Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/rfp/load-sample-rfp` | **Instant Judge Demo:** Ingests Qatar Islamic Bank RFP with 5 sections & formatting rules |
| `GET` | `/api/v1/rfp/queries` | List all parsed RFP questions with status, citations, and owners |
| `POST` | `/api/v1/rfp/query` | Process a single RFP question through PgVector and triage |
| `POST` | `/api/v1/rfp/chat` | Conversational collaboration: instruct agent to rewrite, shorten, or explain |
| `GET` | `/api/v1/rfp/compliance-matrix` | Full compliance matrix (`C`, `PC`, `NC`, `CL`) |
| `GET` | `/api/v1/rfp/clarifications` | Consolidated customer clarification queries with reasons |
| `POST` | `/api/v1/rfp/responses/{id}/approve` | SE approval & continuous learning (saves back to Knowledge Vault) |
| `GET` | `/api/v1/rfp/export/all` | Export formatted proposal report adhering to RFP rules |

---

## 🚀 Level 2: Intelligent Gap Analysis, Compliance & Proposal Assembly

Level 2 extends the platform into an autonomous enterprise proposal intelligence engine:

### Key Level 2 Capabilities:
1. **Advanced Requirement Interpretation**:
   - Detects inter-requirement dependencies (`REQ-FN-01` $\rightarrow$ `REQ-SEC-01`).
   - Identifies multi-product scope (`iMAL Core`, `Azentio Digital Banking`, `TradeGate Broker`, `Shariah Bureau`, `Payment Hub`).
   - Classifies capabilities into customer codes: **Standard (S)**, **Configuration (C)**, **Development (D)**, **Third-Party (T)**, **Not Available (N)**.
   - Detects ambiguous, contradictory, or incomplete requirements.

2. **Intelligent Compliance Matrix**:
   - Customer-adaptive coding schema (S/C/D/T/N or custom).
   - Evidence-backed recommendations grounded in citations.
   - Human validation gating for low confidence (<80%) or incomplete evidence.
   - Mandatory disclosure flags: Engineering man-day estimates (Code D) and Third-Party partner disclosures (Code T).

3. **AI-Driven Gap Analysis**:
   - Crucially distinguishes **Confirmed Product Gaps** (limitations in current release) from **Knowledge Gaps** (insufficient documentation).
   - Categorizes into: *Functionality Gaps*, *Integration Dependencies*, *Security Concerns*, *Missing Documentation*.
   - Routes actionable escalations to: **Product Management (PM)**, **Solution Architecture (SA)**, **Core Engineering**, and **Shariah Advisory Board**.

4. **Clarification Query Generation**:
   - Compiles structured queries: Clause, Ambiguity, Working Assumption, Clarification Question.
   - Prioritizes by business impact: *Solution Design*, *Delivery Timeline*, *Commercial Scope*.
   - Clustered and deduplicated to avoid redundant client requests.

5. **Intelligent Response Validation Gate**:
   - Forensic cross-check against citations.
   - Flags **Code-to-Narrative Mismatches** (e.g. Code S claiming custom development) as blocking errors.
   - Detects unsupported claims and cross-clause contradictions.
   - Verifies all mandatory questions are answered before unlocking submission.

6. **Customer-Specific Formatting & Assembly**:
   - Preserves section numbering (`SEC-X.Y`) and requirement IDs.
   - Computes real-time Submission Readiness Score (0–100%) and Gatekeeper status.
   - Directly populates customer response template (`.csv` / `.xlsx` ready).

7. **Collaborative Review Workflow**:
   - Role assignments: PM, Solution Architecture, Core Engineering, Shariah Board, Commercial/Legal, SE.
   - **Selective Section Regeneration**: Re-runs unapproved sections while strictly preserving approved responses.

### 📡 Level 2 REST Endpoints (`/api/v2/rfp`)

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v2/rfp/analyze-level2` | Executes full interpretation, S/C/D/T/N coding, gap analysis & forensic validation |
| `GET` | `/api/v2/rfp/compliance-matrix` | Requirement-by-requirement compliance matrix with customer codes, estimates & disclosures |
| `GET` | `/api/v2/rfp/gap-analysis` | Confirmed Product Gaps vs Knowledge Gaps report with PM/SA/Eng escalations |
| `GET` | `/api/v2/rfp/validation-report` | Forensic audit report (unsupported claims, code-narrative mismatches, contradictions) |
| `GET` | `/api/v2/rfp/clarification-pack` | Deduplicated, prioritized customer clarification pack with impact ratings |
| `GET` | `/api/v2/rfp/submission-checklist` | Real-time submission readiness cockpit & checklist |
| `GET` | `/api/v2/rfp/export/customer-template` | Populates and downloads customer Excel response template (`.csv`) |
| `POST` | `/api/v2/rfp/requirements/{id}/assign` | Re-assign requirement to internal department SME |
| `POST` | `/api/v2/rfp/requirements/{id}/update-code` | Update delivery code, estimate, or partner details |
| `POST` | `/api/v2/rfp/regenerate-selective` | Selectively regenerate affected sections while preserving approved responses |

---
