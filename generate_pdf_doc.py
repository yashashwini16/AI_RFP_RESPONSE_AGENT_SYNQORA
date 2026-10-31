import os
import sys
from reportlab.lib.pagesizes import letter
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether, HRFlowable
)
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib import colors
from reportlab.pdfgen import canvas

class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        super(NumberedCanvas, self).__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_page_number(num_pages)
            canvas.Canvas.showPage(self)
        canvas.Canvas.save(self)

    def draw_page_number(self, page_count):
        self.saveState()
        self.setFont("Helvetica", 8)
        self.setFillColor(colors.HexColor("#64748B"))
        
        # Header (on pages after cover)
        if self._pageNumber > 1:
            self.drawString(54, letter[1] - 36, "Azentio iMAL AI RFP Copilot — Project Documentation (Level 1 & Level 2)")
            self.setStrokeColor(colors.HexColor("#CBD5E1"))
            self.setLineWidth(0.5)
            self.line(54, letter[1] - 42, letter[0] - 54, letter[1] - 42)

        # Footer
        page_text = f"Page {self._pageNumber} of {page_count}"
        self.drawRightString(letter[0] - 54, 36, page_text)
        self.drawString(54, 36, "Azentio GTM AI Hackathon 2026 — Challenge B (Team Synqora)")
        self.setStrokeColor(colors.HexColor("#CBD5E1"))
        self.setLineWidth(0.5)
        self.line(54, 48, letter[0] - 54, 48)
        self.restoreState()

def build_pdf(filename):
    doc = SimpleDocTemplate(
        filename,
        pagesize=letter,
        leftMargin=54,
        rightMargin=54,
        topMargin=54,
        bottomMargin=54
    )
    
    styles = getSampleStyleSheet()
    
    # Custom styles
    primary = colors.HexColor("#1E3A8A")   # Navy
    secondary = colors.HexColor("#0284C7") # Sky blue
    accent = colors.HexColor("#0F766E")    # Teal
    dark_text = colors.HexColor("#1E293B")
    
    title_style = ParagraphStyle(
        'DocTitle',
        parent=styles['Heading1'],
        fontName='Helvetica-Bold',
        fontSize=24,
        leading=28,
        textColor=primary,
        spaceAfter=6
    )
    
    subtitle_style = ParagraphStyle(
        'DocSubtitle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=12,
        leading=16,
        textColor=secondary,
        spaceAfter=15
    )
    
    meta_style = ParagraphStyle(
        'DocMeta',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9,
        leading=13,
        textColor=colors.HexColor("#475569")
    )
    
    h1_style = ParagraphStyle(
        'SectionH1',
        parent=styles['Heading1'],
        fontName='Helvetica-Bold',
        fontSize=15,
        leading=19,
        textColor=primary,
        spaceBefore=14,
        spaceAfter=8,
        keepWithNext=True
    )
    
    h2_style = ParagraphStyle(
        'SectionH2',
        parent=styles['Heading2'],
        fontName='Helvetica-Bold',
        fontSize=12,
        leading=16,
        textColor=accent,
        spaceBefore=10,
        spaceAfter=4,
        keepWithNext=True
    )
    
    body_style = ParagraphStyle(
        'DocBody',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9.5,
        leading=14,
        textColor=dark_text,
        spaceAfter=6
    )
    
    bullet_style = ParagraphStyle(
        'DocBullet',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9,
        leading=13.5,
        textColor=dark_text,
        leftIndent=14,
        spaceAfter=4
    )
    
    code_style = ParagraphStyle(
        'DocCode',
        parent=styles['Code'],
        fontName='Courier',
        fontSize=8,
        leading=10.5,
        textColor=colors.HexColor("#0F172A")
    )
    
    table_cell = ParagraphStyle(
        'TableCell',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=8,
        leading=10.5,
        textColor=dark_text
    )
    
    table_cell_bold = ParagraphStyle(
        'TableCellBold',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=8,
        leading=10.5,
        textColor=primary
    )
    
    story = []
    
    # ----------------- TITLE BLOCK -----------------
    story.append(Paragraph("Azentio iMAL AI RFP Copilot", title_style))
    story.append(Paragraph("Enterprise Islamic Banking RFP Response & Intelligent Compliance Platform", subtitle_style))
    
    meta_text = """
    <b>Hackathon</b>: Azentio GTM AI Hackathon — Challenge B Solution (Level 1 & Level 2)<br/>
    <b>Target Product Suite</b>: Azentio iMAL Core Banking & Islamic Lending Suite (v15.0)<br/>
    <b>Team</b>: Synqora | <b>Architecture</b>: Java 21 LTS, Spring Boot 3.3.4, PostgreSQL 16 + PgVector 0.8.7 (HNSW)<br/>
    <b>GitHub Repo</b>: https://github.com/AzentioHackathon-2026/AI-RFP-Response-Agent-synqora.git
    """
    story.append(Paragraph(meta_text, meta_style))
    story.append(Spacer(1, 10))
    story.append(HRFlowable(width="100%", thickness=1.5, color=primary, spaceAfter=14))
    
    # ----------------- 1. EXECUTIVE SUMMARY -----------------
    story.append(Paragraph("1. Executive Summary & Purpose", h1_style))
    story.append(Paragraph(
        "Responding to Tier-1 Islamic Banking Request for Proposals (RFPs) is an extraordinarily complex, "
        "multi-week endeavor. Tenders regularly feature 100 to 300 technical and regulatory questions spanning Murabaha commodity broker "
        "integrations, Ijara asset depreciation schedules, Mudaraba profit distribution smoothing (PER/IRR), immutable AAOIFI Shariah audit trails, "
        "and stringent disaster recovery SLAs (RPO &lt; 5 mins, RTO &lt; 15 mins).",
        body_style
    ))
    story.append(Paragraph(
        "The <b>Azentio iMAL AI RFP Copilot</b> transforms this manual bottleneck into an autonomous, forensically audited enterprise workflow. "
        "Built specifically for Sales Engineers (SEs) and cross-functional bid teams, the platform parses bank tenders from Word (.docx), Excel (.xlsx), CSV, "
        "or PDF, performs deep semantic retrieval across Azentio's verified Islamic Banking knowledge vault, classifies requirements into customer-adaptive "
        "compliance codes (<b>S, C, D, T, N</b>), detects inter-requirement dependencies and multi-product components, distinguishes confirmed product gaps from knowledge gaps, "
        "and compiles formatted submission matrices with 100% citation grounding.",
        body_style
    ))
    
    # ----------------- 2. SYSTEM ARCHITECTURE -----------------
    story.append(Paragraph("2. System Architecture & Tech Stack", h1_style))
    story.append(Paragraph(
        "The system is built on an enterprise-grade, microservice-ready Spring Boot backend with full vector database integration:",
        body_style
    ))
    
    tech_data = [
        [Paragraph("<b>Component</b>", table_cell_bold), Paragraph("<b>Technology</b>", table_cell_bold), Paragraph("<b>Specification / Purpose</b>", table_cell_bold)],
        [Paragraph("Backend Framework", table_cell), Paragraph("Spring Boot 3.3.4 (Java 21 LTS)", table_cell), Paragraph("REST API, JPA Hibernate ORM, multipart file processing, background daemon executors", table_cell)],
        [Paragraph("Vector Database", table_cell), Paragraph("PostgreSQL 16 + PgVector 0.8.7", table_cell), Paragraph("1536-dimensional HNSW cosine index (<=>) for high-speed similarity search across millions of tokens", table_cell)],
        [Paragraph("Semantic Embedding", table_cell), Paragraph("Deterministic 1536-dim Vectorizer / OpenAI", table_cell), Paragraph("Built-in offline semantic n-gram vectorizer (100% offline ready) with optional text-embedding-3-small", table_cell)],
        [Paragraph("User Interface", table_cell), Paragraph("Tailwind CSS + Modular Vanilla JS", table_cell), Paragraph("Single Page Application (SPA) with 6 dedicated tabs, live KPI meters, and responsive modals", table_cell)],
        [Paragraph("Tender Ingestion", table_cell), Paragraph("Apache POI, Native DOCX Zip, OpenPDF", table_cell), Paragraph("Binary multi-format parser for Word (.docx tables), Excel (.xlsx sheets), CSV, and PDF questionnaires", table_cell)],
        [Paragraph("Continuous Learning", table_cell), Paragraph("JPA Transactional Vector Vault", table_cell), Paragraph("Approved SE responses dynamically vectorized and added to the past proposal corpus in real time", table_cell)]
    ]
    t_tech = Table(tech_data, colWidths=[110, 150, 244])
    t_tech.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor("#F1F5F9")),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor("#CBD5E1")),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
    ]))
    story.append(t_tech)
    story.append(Spacer(1, 10))
    
    # ----------------- 3. LEVEL 1 CORE CAPABILITIES -----------------
    story.append(Paragraph("3. Level 1: Core Grounded Proposal Copilot", h1_style))
    story.append(Paragraph(
        "Level 1 establishes the bedrock proposal copilot with high-precision grounded synthesis and human-in-the-loop governance:",
        body_style
    ))
    story.append(Paragraph("• <b>Multi-Format Ingestion & Non-RFP Rejection</b>: Native parsers extract questionnaires and clause metadata. Non-RFP documents are automatically rejected with defensive validation.", bullet_style))
    story.append(Paragraph("• <b>Three-Tier Islamic Banking Knowledge Vault</b>: Segregated index across Product Manuals (iMAL v15.0), Past Scopes of Work (Tier-1 transformations), and Certified Past RFPs.", bullet_style))
    story.append(Paragraph("• <b>Confidence Scoring & Citation Traceability</b>: Every drafted sentence includes explicit source file, clause reference, and similarity percentage (0–100%).", bullet_style))
    story.append(Paragraph("• <b>Quality & Compliance Guardrails</b>: Detects duplicates, warns on outdated product manuals (e.g. legacy v12/v13 citations), and routes commercial pricing to the Commercial Desk.", bullet_style))
    story.append(Paragraph("• <b>SE Review Workbench & Continuous Learning Loop</b>: When a Lead SE clicks 'Approve & Learn', the vetted answer is automatically vectorized into the knowledge base.", bullet_style))
    
    story.append(Spacer(1, 8))
    
    # ----------------- 4. LEVEL 2 ADVANCED INTELLIGENCE -----------------
    story.append(Paragraph("4. Level 2: Intelligent Gap Analysis, Compliance & Proposal Assembly", h1_style))
    story.append(Paragraph(
        "Level 2 extends Level 1 into an autonomous decision-making engine capable of complex requirement analysis, gap categorization, forensic validation, and customer-adaptive assembly:",
        body_style
    ))
    
    l2_caps = [
        ("4.1 Advanced Requirement Interpretation", 
         "Detects inter-requirement dependency chains (e.g., REQ-FN-01 Murabaha requires REQ-SEC-01 HSM integration). "
         "Maps solution spanning across iMAL Core, Azentio Digital Banking, TradeGate Gateway, and Payment Hub. Flags ambiguous or conflicting SLA requirements."),
        
        ("4.2 Intelligent Compliance Matrix (Adaptive S/C/D/T/N Codes)",
         "Dynamically supports client-defined compliance codes without hardcoding: "
         "Code S (Standard Capability), Code C (Configuration Required), Code D (Development Required — mandates engineering man-day estimate), "
         "Code T (Third-Party Dependency — mandates partner disclosure), Code N (Not Available / Commercial Confidential). "
         "Requires human validation when confidence is below 80%."),
         
        ("4.3 AI-Driven Gap Analysis (Product Gaps vs. Knowledge Gaps)",
         "Crucially separates Confirmed Product Gaps (features requiring code modifications in iMAL) from Knowledge Gaps (supported features lacking bank-specific dark-fiber/latency specs). "
         "Classifies into 4 types: Functionality Gap, Integration Dependency, Security Concern, and Missing Documentation. "
         "Automatically routes escalation queues to Product Management, Core Engineering, Solution Architecture, and Shariah Board."),
         
        ("4.4 Forensic Response Validation Gate",
         "Gatekeeper that validates answers against source citations before proposal export. "
         "Detects Code-to-Narrative mismatches (e.g., marking Code S while the text discusses bespoke engineering sprints) and flags them as BLOCKING errors. "
         "Identifies unsupported technological claims and cross-clause contradictions."),
         
        ("4.5 Clarification Query Generation & Prioritization",
         "Generates structured clarification registers (Requirement ID, Ambiguity, Working Assumption, Proposed Clarification). "
         "Clusters and deduplicates queries to avoid redundant questions to the client. "
         "Prioritizes queries by business impact: Solution Design, Delivery Timeline, or Commercial Scope."),
         
        ("4.6 Proposal Assembly & Readiness Scoring Cockpit",
         "Calculates real-time 0–100% Submission Readiness Score. Enforces customer formatting rules (preserving SEC-X.Y numbering and 250-word answer length caps). "
         "One-click export of populated customer response template in .csv (Excel-compatible)."),
         
        ("4.7 Collaborative Review Workflow & Selective Regeneration",
         "Assigns clause ownership across 6 department desks. Supports selective section regeneration that strictly preserves already-approved answers.")
    ]
    
    for heading, desc in l2_caps:
        story.append(Paragraph(heading, h2_style))
        story.append(Paragraph(desc, body_style))
        
    story.append(Spacer(1, 8))
    
    # ----------------- 5. DEPARTMENT WORKFLOW TABLE -----------------
    story.append(Paragraph("5. Cross-Department Routing & Escalation Matrix", h1_style))
    story.append(Paragraph(
        "Requirements requiring escalation or sizing are routed to dedicated department queues in the SE Workbench:",
        body_style
    ))
    
    dept_data = [
        [Paragraph("<b>Department Desk</b>", table_cell_bold), Paragraph("<b>Trigger Criteria & Role</b>", table_cell_bold), Paragraph("<b>Deliverables / Output</b>", table_cell_bold)],
        [Paragraph("CORE_ENGINEERING", table_cell), Paragraph("Code D (Development Required), bespoke API connectors, QCB instant payment adapter", table_cell), Paragraph("Man-day sizing estimates (e.g. 20 man-days), sprint delivery timeline", table_cell)],
        [Paragraph("SOLUTION_ARCHITECTURE", table_cell), Paragraph("Knowledge Gaps, hybrid disaster recovery, dark-fiber latency, Oracle Data Guard replication", table_cell), Paragraph("Infrastructure design diagrams, technical clarification queries", table_cell)],
        [Paragraph("PRODUCT_MANAGEMENT", table_cell), Paragraph("Confirmed Product Gaps, future roadmap commitments, quantum Sukuk features", table_cell), Paragraph("Roadmap alignment sign-off, release version scheduling (v15.2)", table_cell)],
        [Paragraph("SHARIAH_BOARD", table_cell), Paragraph("AAOIFI governance, non-halal income purification accounting, automated Murabaha asset sequence", table_cell), Paragraph("Shariah compliance certificate, FATWA advisory validation note", table_cell)],
        [Paragraph("COMMERCIAL_LEGAL", table_cell), Paragraph("Code N, pricing matrices, license fees, SLA penalty clauses, third-party broker fees", table_cell), Paragraph("Bespoke commercial pricing schedule, standard non-disclosure terms", table_cell)],
        [Paragraph("SALES_ENGINEERING", table_cell), Paragraph("Code S & C standard proposal drafting, customer relationship coordination", table_cell), Paragraph("Final proposal sign-off, approved RFP response submission pack", table_cell)]
    ]
    t_dept = Table(dept_data, colWidths=[120, 210, 174])
    t_dept.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor("#F1F5F9")),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor("#CBD5E1")),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
    ]))
    story.append(t_dept)
    story.append(Spacer(1, 10))
    
    # ----------------- 6. REST API REFERENCE -----------------
    story.append(Paragraph("6. Complete REST API Reference", h1_style))
    story.append(Paragraph(
        "The backend exposes complete Level 1 (`/api/v1/rfp`) and Level 2 (`/api/v2/rfp`) endpoints:",
        body_style
    ))
    
    api_data = [
        [Paragraph("<b>Method</b>", table_cell_bold), Paragraph("<b>Endpoint</b>", table_cell_bold), Paragraph("<b>Function & Return</b>", table_cell_bold)],
        [Paragraph("POST", table_cell), Paragraph("/api/v1/rfp/upload", table_cell), Paragraph("Multipart file upload (.docx, .xlsx, .csv, .pdf) with automatic table parsing", table_cell)],
        [Paragraph("POST", table_cell), Paragraph("/api/v1/rfp/load-sample-rfp", table_cell), Paragraph("Instant benchmark loader: ingests Qatar Islamic Bank RFP", table_cell)],
        [Paragraph("GET", table_cell), Paragraph("/api/v1/rfp/queries?documentId={id}", table_cell), Paragraph("Returns triaged requirements with confidence scores, citations & status", table_cell)],
        [Paragraph("POST", table_cell), Paragraph("/api/v1/rfp/responses/{id}/approve", table_cell), Paragraph("Approves response and dynamically indexes answer into PgVector knowledge vault", table_cell)],
        [Paragraph("POST", table_cell), Paragraph("/api/v1/rfp/reset", table_cell), Paragraph("Wipes all active tender records and resets workspace to clean slate", table_cell)],
        [Paragraph("POST", table_cell), Paragraph("/api/v2/rfp/analyze-level2?documentId={id}", table_cell), Paragraph("Executes Level 2 engine: S/C/D/T/N coding, gap analysis, and forensic validation audit", table_cell)],
        [Paragraph("GET", table_cell), Paragraph("/api/v2/rfp/compliance-matrix?documentId={id}", table_cell), Paragraph("Returns requirement-by-requirement compliance matrix with estimates & disclosures", table_cell)],
        [Paragraph("GET", table_cell), Paragraph("/api/v2/rfp/gap-analysis?documentId={id}", table_cell), Paragraph("Distinguishes Product Gaps from Knowledge Gaps with PM/SA/Eng escalation queues", table_cell)],
        [Paragraph("GET", table_cell), Paragraph("/api/v2/rfp/validation-report?documentId={id}", table_cell), Paragraph("Forensic audit report (code-to-narrative mismatches, unsupported claims, version alerts)", table_cell)],
        [Paragraph("GET", table_cell), Paragraph("/api/v2/rfp/clarification-pack?documentId={id}", table_cell), Paragraph("Clustered and deduplicated clarification pack with impact ratings and assumptions", table_cell)],
        [Paragraph("GET", table_cell), Paragraph("/api/v2/rfp/submission-checklist?documentId={id}", table_cell), Paragraph("Real-time submission readiness cockpit (0–100%) and clause compliance checklist", table_cell)],
        [Paragraph("GET", table_cell), Paragraph("/api/v2/rfp/export/customer-template?documentId={id}", table_cell), Paragraph("Downloads populated customer response template (.csv) preserving clause numbering", table_cell)],
        [Paragraph("POST", table_cell), Paragraph("/api/v2/rfp/requirements/{id}/assign", table_cell), Paragraph("Re-routes requirement ownership to internal desk (PM, Eng, SA, Shariah, Legal)", table_cell)],
        [Paragraph("POST", table_cell), Paragraph("/api/v2/rfp/regenerate-selective", table_cell), Paragraph("Regenerates unapproved sections while strictly preserving approved responses", table_cell)]
    ]
    t_api = Table(api_data, colWidths=[55, 200, 249])
    t_api.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor("#F1F5F9")),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor("#CBD5E1")),
        ('TOPPADDING', (0,0), (-1,-1), 3.5),
        ('BOTTOMPADDING', (0,0), (-1,-1), 3.5),
    ]))
    story.append(t_api)
    story.append(Spacer(1, 10))
    
    # ----------------- 7. VERIFICATION & DEMO GUIDE -----------------
    story.append(Paragraph("7. Evaluator & Judge Demo Verification Guide", h1_style))
    story.append(Paragraph(
        "To verify all Level 1 and Level 2 features in the live UI (http://localhost:8080/index.html):",
        body_style
    ))
    story.append(Paragraph("<b>Step 1: Clean Slate</b> — Click the red 'Reset' button in the top navbar to wipe previous test data.", bullet_style))
    story.append(Paragraph("<b>Step 2: Tender Ingest</b> — In Tab 1, browse and select 'Amanah_Crescent_Bank_Islamic_Banking_RFP.docx' and click 'Ingest & Parse' (or click 'Load Demo QIB Tender'). Parses all 33 requirements in seconds.", bullet_style))
    story.append(Paragraph("<b>Step 3: Review Workbench (Tab 2)</b> — Inspect customer response codes (S, C, D, T, N), multi-product spans, dependencies, and citations. Test 'Approve & Learn' on Clause 1 and 'Re-Route Desk' to transfer clause ownership to Core Engineering.", bullet_style))
    story.append(Paragraph("<b>Step 4: Gap Analysis (Tab 3)</b> — View the breakdown distinguishing Confirmed Product Gaps from Knowledge Gaps, with actionable escalation recommendations for Product Management and Engineering.", bullet_style))
    story.append(Paragraph("<b>Step 5: Validation Audit (Tab 4)</b> — Inspect the forensic auditor flagging Code-to-Narrative mismatches and unsupported claims before allowing submission.", bullet_style))
    story.append(Paragraph("<b>Step 6: Clarifications Register (Tab 5)</b> — Inspect clustered queries categorized by Solution Design, Delivery Timeline, and Commercial Scope.", bullet_style))
    story.append(Paragraph("<b>Step 7: Assembly & Matrix (Tab 6)</b> — Review the 0–100% Submission Readiness Cockpit and click 'Download Customer Template (.csv)' to export the finalized proposal.", bullet_style))
    
    # Build Document
    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"Successfully generated PDF: {filename}")

if __name__ == '__main__':
    target_path = "Azentio_iMAL_AI_RFP_Copilot_Project_Documentation.pdf"
    build_pdf(target_path)
