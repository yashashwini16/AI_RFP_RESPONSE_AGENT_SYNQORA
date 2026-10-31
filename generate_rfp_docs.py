import os
import openpyxl
from openpyxl.styles import Font, PatternFill, Alignment, Border, Side
from reportlab.lib.pagesizes import letter
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib import colors

os.makedirs("knowledge-vault/sample_incoming_rfp", exist_ok=True)
excel_path = "knowledge-vault/sample_incoming_rfp/QIB_Islamic_Banking_RFP_2026.xlsx"
pdf_path = "knowledge-vault/sample_incoming_rfp/QIB_Islamic_Banking_RFP_2026.pdf"

# ----------------- 1. GENERATE EXCEL RFP -----------------
wb = openpyxl.Workbook()
ws = wb.active
ws.title = "RFP Requirements Matrix"

# Colors & Fonts
header_fill = PatternFill(start_color="1E3A8A", end_color="1E3A8A", fill_type="solid") # Dark Blue
header_font = Font(name="Segoe UI", size=11, bold=True, color="FFFFFF")
title_font = Font(name="Segoe UI", size=15, bold=True, color="1E3A8A")
subtitle_font = Font(name="Segoe UI", size=10, italic=True, color="4B5563")
cell_font = Font(name="Segoe UI", size=10)
bold_font = Font(name="Segoe UI", size=10, bold=True)
thin_border = Border(
    left=Side(style='thin', color='E5E7EB'),
    right=Side(style='thin', color='E5E7EB'),
    top=Side(style='thin', color='E5E7EB'),
    bottom=Side(style='thin', color='E5E7EB')
)

# Title Block
ws.merge_cells("A1:G1")
ws["A1"] = "QATAR ISLAMIC BANK (QIB) - REQUEST FOR PROPOSAL"
ws["A1"].font = title_font
ws["A1"].alignment = Alignment(vertical="center")

ws.merge_cells("A2:G2")
ws["A2"] = "Tender No: QIB-IT-2026-RFP-09 | Scope: Core Islamic Banking Modernization | Deadline: Nov 30, 2026"
ws["A2"].font = subtitle_font
ws["A2"].alignment = Alignment(vertical="center")

ws.row_dimensions[1].height = 26
ws.row_dimensions[2].height = 20
ws.row_dimensions[4].height = 26

headers = [
    "Req ID", 
    "Section", 
    "Category", 
    "Requirement Specification", 
    "Type", 
    "Question Format", 
    "Vendor Compliance (Agent Draft)"
]

ws.append([]) # Row 3 blank

for col_idx, header in enumerate(headers, 1):
    cell = ws.cell(row=4, column=col_idx, value=header)
    cell.fill = header_fill
    cell.font = header_font
    cell.alignment = Alignment(horizontal="center", vertical="center", wrap_text=True)

rows = [
    (
        "REQ-FN-01",
        "Section 1: Murabaha Engine",
        "FUNCTIONAL",
        "Does the platform support automated Murabaha commodity broker integration and Shariah asset sequence validation?",
        "MANDATORY",
        "CONFIRMATION",
        "Fully Addressed (Azentio iMAL v15 native broker integration & AAOIFI validation)"
    ),
    (
        "REQ-FN-02",
        "Section 1: Islamic Financing",
        "FUNCTIONAL",
        "How does iMAL manage Ijara asset ownership, insurance takaful, and variable profit rate repricing?",
        "MANDATORY",
        "EXPLANATION",
        "Fully Addressed (Lessor balance sheet retention, automated Takaful apportionment)"
    ),
    (
        "REQ-SEC-01",
        "Section 2: Security & Governance",
        "SECURITY",
        "Does the solution provide an immutable Shariah audit trail and separate non-halal income purification accounting?",
        "MANDATORY",
        "CONFIRMATION",
        "Fully Addressed (Immutable Shariah Audit Trail & Charity Suspense ledger)"
    ),
    (
        "REQ-TECH-01",
        "Section 3: Architecture & DR",
        "TECHNICAL",
        "What are the guaranteed RPO and RTO during multi-region hybrid disaster recovery?",
        "MANDATORY",
        "FACTUAL",
        "Partially Addressed (RPO < 5 min, RTO < 15 min; verified against SLA audit)"
    ),
    (
        "REQ-COM-01",
        "Section 4: Commercial Terms",
        "COMMERCIAL",
        "Provide the total software license fee pricing and 5-year maintenance cost schedule for 500,000 active Islamic accounts.",
        "MANDATORY",
        "EVIDENCE",
        "Partially Addressed [GUARDRAIL TRIGGERED - Routed to Commercial Desk]"
    ),
    (
        "REQ-INT-01",
        "Section 5: FinTech Integrations",
        "INTEGRATION",
        "Describe the native quantum encryption protocol used for cross-border Islamic Sukuk smart contracts.",
        "OPTIONAL",
        "EVIDENCE",
        "Information Required [CLARIFICATION QUERY - Logged for QIB submission]"
    )
]

for r_idx, row in enumerate(rows, 5):
    ws.row_dimensions[r_idx].height = 36
    for c_idx, val in enumerate(row, 1):
        cell = ws.cell(row=r_idx, column=c_idx, value=val)
        cell.font = cell_font
        cell.border = thin_border
        if c_idx in (1, 3, 5, 6):
            cell.alignment = Alignment(horizontal="center", vertical="center")
        else:
            cell.alignment = Alignment(horizontal="left", vertical="center", wrap_text=True)
        if c_idx == 5 and val == "MANDATORY":
            cell.font = bold_font
            cell.fill = PatternFill(start_color="FEE2E2", end_color="FEE2E2", fill_type="solid")
        elif c_idx == 7 and "GUARDRAIL" in val:
            cell.fill = PatternFill(start_color="FEF3C7", end_color="FEF3C7", fill_type="solid")

col_widths = {1: 14, 2: 24, 3: 16, 4: 50, 5: 14, 6: 18, 7: 42}
for col_idx, width in col_widths.items():
    ws.column_dimensions[openpyxl.utils.get_column_letter(col_idx)].width = width

wb.save(excel_path)
print(f"Excel created: {excel_path}")

# ----------------- 2. GENERATE PDF RFP -----------------
doc = SimpleDocTemplate(pdf_path, pagesize=letter, rightMargin=36, leftMargin=36, topMargin=36, bottomMargin=36)
styles = getSampleStyleSheet()

title_style = ParagraphStyle(
    'DocTitle',
    parent=styles['Heading1'],
    fontName='Helvetica-Bold',
    fontSize=18,
    leading=22,
    textColor=colors.HexColor('#1E3A8A'),
    alignment=1
)

subtitle_style = ParagraphStyle(
    'DocSubTitle',
    parent=styles['Normal'],
    fontName='Helvetica',
    fontSize=10,
    leading=14,
    textColor=colors.HexColor('#4B5563'),
    alignment=1
)

heading2_style = ParagraphStyle(
    'SecHeading',
    parent=styles['Heading2'],
    fontName='Helvetica-Bold',
    fontSize=12,
    leading=16,
    textColor=colors.HexColor('#1E3A8A'),
    spaceBefore=10,
    spaceAfter=4
)

body_style = ParagraphStyle(
    'DocBody',
    parent=styles['Normal'],
    fontName='Helvetica',
    fontSize=9,
    leading=12
)

table_header_style = ParagraphStyle(
    'TableHeader',
    parent=styles['Normal'],
    fontName='Helvetica-Bold',
    fontSize=9,
    leading=11,
    textColor=colors.white,
    alignment=1
)

elements = []
elements.append(Paragraph("QATAR ISLAMIC BANK (QIB)", title_style))
elements.append(Paragraph("REQUEST FOR PROPOSAL (RFP) - TENDER NO: QIB-IT-2026-RFP-09", subtitle_style))
elements.append(Paragraph("Core Islamic Banking & Lending Modernization Platform", subtitle_style))
elements.append(Spacer(1, 14))

elements.append(Paragraph("1. Executive Overview & Submission Instructions", heading2_style))
inst_text = (
    "Qatar Islamic Bank (QIB) invites qualified enterprise core banking vendors to submit technical proposals "
    "for its next-generation Islamic financing, lending, and Shariah governance transformation. "
    "All responses must adhere strictly to AAOIFI guidelines, provide detailed source references, "
    "and classify requirements into standard compliance levels. "
    "Commercial proposals must be kept segregated from technical questionnaires."
)
elements.append(Paragraph(inst_text, body_style))
elements.append(Spacer(1, 12))

elements.append(Paragraph("2. Technical & Functional Questionnaire", heading2_style))

table_data = [
    [
        Paragraph("Req ID", table_header_style),
        Paragraph("Section / Category", table_header_style),
        Paragraph("Requirement Specification", table_header_style),
        Paragraph("Type", table_header_style),
        Paragraph("Format", table_header_style)
    ]
]

for row in rows:
    req_id, sec, cat, req_text, req_type, q_fmt, _ = row
    table_data.append([
        Paragraph(f"<b>{req_id}</b>", body_style),
        Paragraph(f"<b>{cat}</b><br/>{sec}", body_style),
        Paragraph(req_text, body_style),
        Paragraph(f"<b>{req_type}</b>", body_style),
        Paragraph(q_fmt, body_style)
    ])

t = Table(table_data, colWidths=[65, 110, 245, 60, 60])
t.setStyle(TableStyle([
    ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#1E3A8A')),
    ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
    ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
    ('TEXTCOLOR', (0, 0), (-1, 0), colors.white),
    ('GRID', (0, 0), (-1, -1), 0.5, colors.HexColor('#D1D5DB')),
    ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.HexColor('#FFFFFF'), colors.HexColor('#F9FAFB')]),
    ('TOPPADDING', (0, 0), (-1, -1), 6),
    ('BOTTOMPADDING', (0, 0), (-1, -1), 6),
]))

elements.append(t)
elements.append(Spacer(1, 14))
elements.append(Paragraph("3. Evaluation Criteria", heading2_style))
eval_text = (
    "Responses will be evaluated based on: (a) Grounding in certified product documentation (40%), "
    "(b) Historical delivery track record in Tier-1 Islamic banks (30%), (c) Full Shariah governance compliance (20%), "
    "and (d) Clarification rigor (10%)."
)
elements.append(Paragraph(eval_text, body_style))

doc.build(elements)
print(f"PDF created: {pdf_path}")
