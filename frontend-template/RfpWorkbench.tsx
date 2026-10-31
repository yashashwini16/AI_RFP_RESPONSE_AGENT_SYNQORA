import React, { useState, useEffect } from 'react';

const API_BASE = 'http://localhost:8080/api/v1/rfp';

export interface Citation {
  id: number;
  category: string;
  sourceType: string; // PRODUCT_DOC, PAST_SOW, PAST_RFP
  productVersion: string;
  isOutdated: boolean;
  question: string;
  approvedAnswer: string;
  sourceDocument: string;
  sectionReference: string;
  similarityPercentage: number;
}

export interface RfpItem {
  queryId: number;
  responseId: number;
  requirementId: string; // Level 1 Traceability: REQ-FN-01
  rfpTitle: string;
  clientName: string;
  sectionName: string;
  question: string;
  category: string; // FUNCTIONAL, TECHNICAL, SECURITY, COMPLIANCE, INTEGRATION, COMMERCIAL
  requirementType: 'MANDATORY' | 'OPTIONAL';
  questionType: 'FACTUAL' | 'EXPLANATION' | 'EVIDENCE' | 'CONFIRMATION';
  responseClassification: 'Fully Addressed' | 'Partially Addressed' | 'Information Required' | 'Not Supported';
  unansweredExplanation?: string;
  isDuplicate?: boolean;
  guardrailFlag: 'TECHNICAL' | 'LEGAL_REVIEW' | 'PRICING_DESK' | 'SLA_COMMITMENT';
  guardrailReason?: string;
  assignedOwner: 'SE_TECHNICAL' | 'LEGAL_COUNSEL' | 'COMMERCIAL_DESK';
  outdatedWarning?: string;
  status: 'PENDING' | 'DRAFTED' | 'REVIEWED' | 'APPROVED';
  draftedAnswer?: string;
  confidenceScore: number;
  gapFlag: boolean;
  citations: Citation[];
  reviewedAnswer?: string;
  isApproved: boolean;
  latencyMs?: number;
}

export interface ComplianceRow {
  queryId: number;
  clauseNumber: string;
  sectionName: string;
  requirementText: string;
  requirementType: string;
  complianceStatus: string;
  responseSummary: string;
  assignedOwner: string;
}

export interface ClarificationItem {
  id: number;
  queryId: number;
  questionText: string;
  clarificationNeeded: string;
  reasonJustification: string;
  sectionName: string;
}

export default function RfpWorkbench() {
  const [items, setItems] = useState<RfpItem[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [activeTab, setActiveTab] = useState<'WORKSPACE' | 'MATRIX' | 'CLARIFICATIONS'>('WORKSPACE');
  const [editedAnswer, setEditedAnswer] = useState('');
  const [loading, setLoading] = useState(false);
  const [matrixRows, setMatrixRows] = useState<ComplianceRow[]>([]);
  const [clarifications, setClarifications] = useState<ClarificationItem[]>([]);
  const [exportModal, setExportModal] = useState<string | null>(null);

  const loadData = async () => {
    try {
      const qRes = await fetch(`${API_BASE}/queries`);
      if (qRes.ok) {
        const data = await qRes.json();
        setItems(data);
        if (data.length > 0 && selectedId === null) {
          setSelectedId(data[0].queryId);
          setEditedAnswer(data[0].reviewedAnswer || data[0].draftedAnswer || '');
        }
      }
      const mRes = await fetch(`${API_BASE}/compliance-matrix`);
      if (mRes.ok) setMatrixRows(await mRes.json());

      const cRes = await fetch(`${API_BASE}/clarifications`);
      if (cRes.ok) setClarifications(await cRes.json());
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const selectedItem = items.find((i) => i.queryId === selectedId);

  // Ingest Level 1 Benchmark RFP
  const handleLoadSampleRfp = async () => {
    setLoading(true);
    try {
      const res = await fetch(`${API_BASE}/load-sample-rfp`, { method: 'POST' });
      if (res.ok) {
        await loadData();
        alert('✅ Level 1 RFP Ingested with Requirement-Level Traceability!');
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  // Review & Approve
  const handleApprove = async () => {
    if (!selectedItem || !selectedItem.responseId) return;
    try {
      const res = await fetch(`${API_BASE}/responses/${selectedItem.responseId}/approve`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ reviewedAnswer: editedAnswer, saveToKnowledgeBase: true }),
      });
      if (res.ok) {
        const updated = await res.json();
        setItems((prev) => prev.map((item) => (item.queryId === updated.queryId ? updated : item)));
        loadData();
        alert('✅ Requirement response approved and marked final!');
      }
    } catch (e) {}
  };

  // Export Word / Markdown
  const handleExportWord = async () => {
    try {
      const res = await fetch(`${API_BASE}/export/all`);
      if (res.ok) {
        const data = await res.json();
        setExportModal(data.markdownReport);
      }
    } catch (e) {}
  };

  // Export Excel CSV
  const handleExportExcel = () => {
    window.open(`${API_BASE}/export/excel`, '_blank');
  };

  return (
    <div className="flex h-screen bg-slate-950 text-slate-100 font-sans">
      {/* LEFT COLUMN: Extracted Requirements Queue */}
      <div className="w-1/3 border-r border-slate-800 flex flex-col bg-slate-900/40">
        <div className="p-4 border-b border-slate-800 bg-slate-900 flex justify-between items-center">
          <div>
            <h1 className="text-sm font-bold text-white flex items-center gap-2">
              <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse"></span>
              Level 1: RFP Intelligence Agent
            </h1>
            <p className="text-[11px] text-slate-400">Automated Analysis & Requirement Traceability</p>
          </div>
          <div className="flex gap-1.5">
            <button
              onClick={handleExportWord}
              className="px-2 py-1 text-[11px] font-semibold bg-emerald-700 hover:bg-emerald-600 rounded text-white shadow"
              title="Export consolidated draft to Word/Markdown"
            >
              Export Word
            </button>
            <button
              onClick={handleExportExcel}
              className="px-2 py-1 text-[11px] font-semibold bg-blue-700 hover:bg-blue-600 rounded text-white shadow"
              title="Export consolidated draft to Excel Spreadsheet"
            >
              Export Excel
            </button>
          </div>
        </div>

        {/* Level 1 Benchmark Quick-Loader Banner */}
        <div className="p-3 bg-slate-800/40 border-b border-slate-800 flex justify-between items-center">
          <div>
            <span className="text-[11px] font-semibold text-slate-200 block">QIB Core Banking RFP 2026</span>
            <span className="text-[10px] text-slate-400">PDF • 5 Sections • Functional, Tech, Security, Commercial</span>
          </div>
          <button
            onClick={handleLoadSampleRfp}
            disabled={loading}
            className="px-2.5 py-1 text-[11px] font-bold bg-blue-600 hover:bg-blue-500 rounded text-white shadow"
          >
            {loading ? 'Ingesting...' : '⚡ Ingest & Parse RFP'}
          </button>
        </div>

        {/* View Tabs */}
        <div className="flex border-b border-slate-800 text-xs font-semibold bg-slate-900/60">
          <button
            onClick={() => setActiveTab('WORKSPACE')}
            className={`flex-1 py-2 text-center border-b-2 ${
              activeTab === 'WORKSPACE' ? 'border-emerald-500 text-emerald-400 bg-slate-800/50' : 'border-transparent text-slate-400 hover:text-white'
            }`}
          >
            Requirements ({items.length})
          </button>
          <button
            onClick={() => setActiveTab('MATRIX')}
            className={`flex-1 py-2 text-center border-b-2 ${
              activeTab === 'MATRIX' ? 'border-emerald-500 text-emerald-400 bg-slate-800/50' : 'border-transparent text-slate-400 hover:text-white'
            }`}
          >
            Compliance Matrix
          </button>
          <button
            onClick={() => setActiveTab('CLARIFICATIONS')}
            className={`flex-1 py-2 text-center border-b-2 ${
              activeTab === 'CLARIFICATIONS' ? 'border-amber-500 text-amber-400 bg-slate-800/50' : 'border-transparent text-slate-400 hover:text-white'
            }`}
          >
            Clarifications ({clarifications.length})
          </button>
        </div>

        {/* Requirements Queue */}
        <div className="flex-1 overflow-y-auto divide-y divide-slate-800/60">
          {items.map((item) => (
            <div
              key={item.queryId}
              onClick={() => {
                setSelectedId(item.queryId);
                setEditedAnswer(item.reviewedAnswer || item.draftedAnswer || '');
                if (activeTab !== 'WORKSPACE') setActiveTab('WORKSPACE');
              }}
              className={`p-3.5 cursor-pointer transition ${
                selectedId === item.queryId ? 'bg-slate-800/90 border-l-4 border-emerald-500' : 'hover:bg-slate-800/40'
              }`}
            >
              <div className="flex items-center justify-between text-[10px] mb-1">
                <span className="font-mono font-bold text-blue-400">
                  {item.requirementId || `REQ-${item.queryId}`}
                </span>
                <span
                  className={`px-1.5 py-0.5 rounded text-[9px] font-bold ${
                    item.responseClassification === 'Fully Addressed'
                      ? 'bg-emerald-950 text-emerald-400 border border-emerald-800'
                      : item.responseClassification === 'Partially Addressed'
                      ? 'bg-amber-950 text-amber-400 border border-amber-800'
                      : 'bg-rose-950 text-rose-400 border border-rose-800'
                  }`}
                >
                  {item.responseClassification || 'Pending'}
                </span>
              </div>
              <p className="text-xs font-medium text-slate-200 line-clamp-2">{item.question}</p>
              <div className="mt-2 flex items-center justify-between text-[10px] text-slate-400">
                <div className="flex gap-1.5">
                  <span className="bg-slate-800 px-1.5 py-0.5 rounded text-slate-300 font-mono text-[9px]">{item.category}</span>
                  <span className="bg-slate-800 px-1.5 py-0.5 rounded text-slate-400 text-[9px]">{item.requirementType}</span>
                </div>
                <span className="font-mono text-emerald-400 font-bold text-[11px]">{item.confidenceScore}%</span>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* RIGHT COLUMN: RFP Response Workspace */}
      <div className="flex-1 flex flex-col bg-slate-950">
        {activeTab === 'WORKSPACE' && selectedItem && (
          <div className="flex-1 overflow-y-auto p-6 space-y-5 max-w-4xl mx-auto w-full">
            {/* Requirement Header */}
            <div className="bg-slate-900 border border-slate-800 rounded-lg p-5">
              <div className="flex items-center justify-between text-xs text-slate-400 mb-1.5">
                <div className="flex items-center gap-2">
                  <span className="px-2 py-0.5 rounded bg-blue-950 text-blue-300 border border-blue-800 font-mono font-bold text-xs">
                    {selectedItem.requirementId || `REQ-${selectedItem.queryId}`}
                  </span>
                  <span>{selectedItem.sectionName}</span>
                </div>
                <span className="font-mono text-emerald-400 font-bold text-sm">
                  {selectedItem.confidenceScore}% Confidence
                </span>
              </div>
              <h2 className="text-sm font-semibold text-white mt-2 leading-relaxed">{selectedItem.question}</h2>

              {/* Classification & Metadata Badges */}
              <div className="mt-4 pt-3 border-t border-slate-800 flex flex-wrap gap-2 text-[11px]">
                <span className="px-2 py-1 rounded bg-slate-800 text-slate-300">
                  Domain: <strong>{selectedItem.category}</strong>
                </span>
                <span className="px-2 py-1 rounded bg-slate-800 text-slate-300">
                  Instruction: <strong>{selectedItem.requirementType}</strong>
                </span>
                <span className="px-2 py-1 rounded bg-slate-800 text-slate-300">
                  Question Type: <strong>{selectedItem.questionType}</strong>
                </span>
                <span
                  className={`px-2 py-1 rounded font-bold ${
                    selectedItem.responseClassification === 'Fully Addressed'
                      ? 'bg-emerald-950 text-emerald-300 border border-emerald-800'
                      : selectedItem.responseClassification === 'Partially Addressed'
                      ? 'bg-amber-950 text-amber-300 border border-amber-800'
                      : 'bg-rose-950 text-rose-300 border border-rose-800'
                  }`}
                >
                  Status: {selectedItem.responseClassification}
                </span>
              </div>

              {/* Missing Information / Guardrail Explanation */}
              {selectedItem.unansweredExplanation && (
                <div className="mt-3 p-3 rounded bg-amber-950/60 border border-amber-800 text-amber-200 text-xs">
                  <strong>⚠️ Requirement Analysis Note:</strong> {selectedItem.unansweredExplanation}
                </div>
              )}
            </div>

            {/* AI Generated & Editable Answer */}
            <div className="bg-slate-900 border border-slate-800 rounded-lg p-5">
              <div className="flex justify-between items-center mb-2">
                <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400">
                  Generated Proposal Response
                </h3>
                {selectedItem.isApproved && (
                  <span className="text-xs text-emerald-400 bg-emerald-950 border border-emerald-800 px-2.5 py-0.5 rounded font-semibold">
                    ✓ Final & Approved by SE
                  </span>
                )}
              </div>
              <textarea
                rows={7}
                value={editedAnswer}
                onChange={(e) => setEditedAnswer(e.target.value)}
                className="w-full text-xs leading-relaxed p-3 rounded bg-slate-950 border border-slate-800 text-slate-100 focus:outline-none focus:ring-1 focus:ring-emerald-500"
              />
              <div className="mt-4 flex justify-between items-center">
                <span className="text-[11px] text-slate-500">
                  Human-in-the-loop: Review, edit, and approve before final proposal compilation.
                </span>
                <button
                  onClick={handleApprove}
                  className="px-4 py-2 text-xs font-semibold bg-emerald-600 hover:bg-emerald-500 text-white rounded-md transition shadow"
                >
                  ✓ Approve & Mark Final
                </button>
              </div>
            </div>

            {/* Evidential Grounded Sources */}
            <div className="bg-slate-900 border border-slate-800 rounded-lg p-5">
              <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3">
                Supporting Organizational Knowledge ({selectedItem.citations?.length || 0})
              </h3>
              <div className="space-y-3">
                {selectedItem.citations?.map((cit) => (
                  <div key={cit.id} className="p-3 bg-slate-950 rounded border border-slate-800 text-xs">
                    <div className="flex justify-between items-center mb-1">
                      <span className="font-semibold text-blue-400">
                        [{cit.sourceType}] {cit.sourceDocument} • {cit.sectionReference} ({cit.productVersion})
                      </span>
                      <span className="text-emerald-400 font-mono font-bold">{cit.similarityPercentage}% Match</span>
                    </div>
                    <p className="text-slate-300 italic text-[11px] mt-1">"{cit.approvedAnswer}"</p>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* TAB 2: COMPLIANCE MATRIX VIEW */}
        {activeTab === 'MATRIX' && (
          <div className="flex-1 overflow-y-auto p-6 max-w-5xl mx-auto w-full">
            <div className="flex justify-between items-center mb-4">
              <div>
                <h2 className="text-base font-bold text-white">Consolidated Compliance Matrix</h2>
                <p className="text-xs text-slate-400">Requirement-level traceability and response status</p>
              </div>
              <button
                onClick={handleExportExcel}
                className="px-3 py-1.5 text-xs font-semibold bg-blue-600 hover:bg-blue-500 text-white rounded shadow"
              >
                📥 Download Excel (.csv)
              </button>
            </div>
            <div className="bg-slate-900 border border-slate-800 rounded-lg overflow-hidden">
              <table className="w-full text-left text-xs">
                <thead className="bg-slate-800 text-slate-300 text-[11px] uppercase tracking-wider">
                  <tr>
                    <th className="p-3">Clause ID</th>
                    <th className="p-3">Requirement</th>
                    <th className="p-3">Type</th>
                    <th className="p-3">Classification</th>
                    <th className="p-3">Owner</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800">
                  {matrixRows.map((row) => (
                    <tr key={row.queryId} className="hover:bg-slate-800/40">
                      <td className="p-3 font-mono text-blue-400 font-bold">{row.clauseNumber}</td>
                      <td className="p-3 font-medium text-slate-200 max-w-xs">{row.requirementText}</td>
                      <td className="p-3 text-[11px] font-mono">{row.requirementType}</td>
                      <td className="p-3">
                        <span
                          className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                            row.complianceStatus === 'Fully Addressed' || row.complianceStatus === 'COMPLIANT'
                              ? 'bg-emerald-950 text-emerald-400 border border-emerald-800'
                              : row.complianceStatus === 'Partially Addressed' || row.complianceStatus === 'PARTIALLY_COMPLIANT'
                              ? 'bg-amber-950 text-amber-400 border border-amber-800'
                              : 'bg-rose-950 text-rose-400 border border-rose-800'
                          }`}
                        >
                          {row.complianceStatus}
                        </span>
                      </td>
                      <td className="p-3 font-mono text-[11px] text-slate-400">{row.assignedOwner}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* TAB 3: CLARIFICATIONS TAB */}
        {activeTab === 'CLARIFICATIONS' && (
          <div className="flex-1 overflow-y-auto p-6 max-w-4xl mx-auto w-full">
            <div className="mb-4">
              <h2 className="text-base font-bold text-white">Clarification Queries List (Information Required)</h2>
              <p className="text-xs text-slate-400">Formal queries generated for client clarification instead of inventing assumptions.</p>
            </div>
            <div className="space-y-3">
              {clarifications.map((c) => (
                <div key={c.id} className="p-4 bg-slate-900 border border-slate-800 rounded-lg text-xs space-y-2">
                  <div className="flex justify-between items-center text-slate-400 text-[11px]">
                    <span>Section: {c.sectionName}</span>
                    <span className="text-amber-400 font-bold">Clarification #{c.id}</span>
                  </div>
                  <h4 className="font-semibold text-white">Client Requirement: "{c.questionText}"</h4>
                  <div className="p-2.5 bg-slate-950 rounded border border-slate-800 text-amber-200">
                    <strong>Information Required:</strong> {c.clarificationNeeded}
                  </div>
                  <div className="text-[11px] text-slate-400">
                    <strong>Reason Needed:</strong> {c.reasonJustification}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* Export Word Modal */}
      {exportModal && (
        <div className="fixed inset-0 bg-black/80 flex items-center justify-center p-6 z-50">
          <div className="bg-slate-900 border border-slate-800 rounded-xl max-w-3xl w-full p-6 space-y-4 max-h-[85vh] flex flex-col">
            <div className="flex justify-between items-center border-b border-slate-800 pb-3">
              <h3 className="text-sm font-bold text-white">Consolidated Draft Proposal (Word / Markdown)</h3>
              <button onClick={() => setExportModal(null)} className="text-slate-400 hover:text-white text-sm">✕</button>
            </div>
            <pre className="flex-1 overflow-y-auto bg-slate-950 p-4 rounded text-[11px] font-mono text-slate-300 border border-slate-800 whitespace-pre-wrap">
              {exportModal}
            </pre>
            <div className="flex justify-end gap-2 pt-2">
              <button
                onClick={() => {
                  navigator.clipboard.writeText(exportModal);
                  alert('Copied Markdown Proposal to clipboard!');
                }}
                className="px-4 py-2 text-xs font-semibold bg-emerald-600 hover:bg-emerald-500 text-white rounded transition"
              >
                Copy Markdown
              </button>
              <button onClick={() => setExportModal(null)} className="px-4 py-2 text-xs font-semibold bg-slate-800 text-slate-300 rounded">
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
