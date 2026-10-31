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
  rfpTitle: string;
  clientName: string;
  sectionName: string;
  question: string;
  category: string;
  requirementType: 'MANDATORY' | 'OPTIONAL' | 'INFO';
  complianceStatus: 'COMPLIANT' | 'PARTIALLY_COMPLIANT' | 'NON_COMPLIANT' | 'CLARIFICATION';
  guardrailFlag: 'TECHNICAL' | 'LEGAL_REVIEW' | 'PRICING_DESK' | 'SLA_COMMITMENT';
  guardrailReason?: string;
  assignedOwner: 'SE_TECHNICAL' | 'LEGAL_COUNSEL' | 'COMMERCIAL_DESK';
  outdatedWarning?: string;
  status: 'PENDING' | 'DRAFTED' | 'NEEDS_REVIEW' | 'APPROVED';
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

export interface ChatMessage {
  id?: number;
  sender: 'SE' | 'AGENT';
  message: string;
  actionType?: string;
}

export default function RfpWorkbench() {
  const [items, setItems] = useState<RfpItem[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [activeTab, setActiveTab] = useState<'PROPOSAL' | 'MATRIX' | 'CLARIFICATIONS' | 'CHAT'>('PROPOSAL');
  const [editedAnswer, setEditedAnswer] = useState('');
  const [loading, setLoading] = useState(false);

  // Chat State
  const [chatMessages, setChatMessages] = useState<ChatMessage[]>([]);
  const [chatInput, setChatInput] = useState('');
  const [chatLoading, setChatLoading] = useState(false);

  // Matrix & Clarifications State
  const [matrixRows, setMatrixRows] = useState<ComplianceRow[]>([]);
  const [clarifications, setClarifications] = useState<ClarificationItem[]>([]);
  const [exportModal, setExportModal] = useState<string | null>(null);

  const loadQueries = async () => {
    try {
      const res = await fetch(`${API_BASE}/queries`);
      if (res.ok) {
        const data = await res.json();
        setItems(data);
        if (data.length > 0 && selectedId === null) {
          setSelectedId(data[0].queryId);
          setEditedAnswer(data[0].reviewedAnswer || data[0].draftedAnswer || '');
        }
      }
    } catch (e) {
      console.error(e);
    }
  };

  const loadMatrix = async () => {
    try {
      const res = await fetch(`${API_BASE}/compliance-matrix`);
      if (res.ok) setMatrixRows(await res.json());
    } catch (e) {}
  };

  const loadClarifications = async () => {
    try {
      const res = await fetch(`${API_BASE}/clarifications`);
      if (res.ok) setClarifications(await res.json());
    } catch (e) {}
  };

  useEffect(() => {
    loadQueries();
    loadMatrix();
    loadClarifications();
  }, []);

  const selectedItem = items.find((i) => i.queryId === selectedId);

  // Load Benchmark Demo RFP
  const handleLoadSampleRfp = async () => {
    setLoading(true);
    try {
      const res = await fetch(`${API_BASE}/load-sample-rfp`, { method: 'POST' });
      if (res.ok) {
        await loadQueries();
        await loadMatrix();
        await loadClarifications();
        alert('✅ Loaded Qatar Islamic Bank (QIB) RFP with 5 Sections & Formatting Rules!');
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  // SE Chat Message
  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!chatInput.trim()) return;
    const userText = chatInput;
    setChatMessages((prev) => [...prev, { sender: 'SE', message: userText }]);
    setChatInput('');
    setChatLoading(true);

    try {
      const res = await fetch(`${API_BASE}/chat`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          sessionId: 'se-session-01',
          queryId: selectedId,
          message: userText,
          actionType: 'CHAT',
        }),
      });
      if (res.ok) {
        const reply = await res.json();
        setChatMessages((prev) => [...prev, { sender: 'AGENT', message: reply.agentReply, actionType: reply.actionTaken }]);
        if (reply.updatedDraft) {
          setEditedAnswer(reply.updatedDraft);
          loadQueries();
        }
      }
    } catch (e) {
      console.error(e);
    } finally {
      setChatLoading(false);
    }
  };

  // Approve Answer
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
        loadMatrix();
        alert('✅ Proposal answer approved and locked for submission!');
      }
    } catch (e) {}
  };

  // Export Full Document
  const handleExport = async () => {
    try {
      const res = await fetch(`${API_BASE}/export/all`);
      if (res.ok) {
        const data = await res.json();
        setExportModal(data.markdownReport);
      }
    } catch (e) {}
  };

  return (
    <div className="flex h-screen bg-slate-950 text-slate-100 font-sans">
      {/* LEFT COLUMN: RFP Sections & Question Queue */}
      <div className="w-1/3 border-r border-slate-800 flex flex-col bg-slate-900/40">
        <div className="p-4 border-b border-slate-800 bg-slate-900 flex justify-between items-center">
          <div>
            <h1 className="text-sm font-bold text-white flex items-center gap-2">
              <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse"></span>
              Azentio Agent 2: Islamic Banking RFP Copilot
            </h1>
            <p className="text-[11px] text-slate-400">iMAL Suite • AAOIFI Compliant • 3 Grounded Sources</p>
          </div>
          <button
            onClick={handleExport}
            className="px-2.5 py-1 text-xs font-semibold bg-emerald-600 hover:bg-emerald-500 rounded text-white"
          >
            Export Proposal
          </button>
        </div>

        {/* Demo Quick-Loader Banner */}
        <div className="p-3 bg-slate-800/40 border-b border-slate-800 flex justify-between items-center">
          <span className="text-[11px] text-slate-300">Qatar Islamic Bank RFP 2026</span>
          <button
            onClick={handleLoadSampleRfp}
            disabled={loading}
            className="px-2 py-1 text-[11px] font-bold bg-blue-600 hover:bg-blue-500 rounded text-white shadow"
          >
            {loading ? 'Ingesting...' : '⚡ Ingest Benchmark RFP'}
          </button>
        </div>

        {/* Tabs for Left/Right switching */}
        <div className="flex border-b border-slate-800 text-xs font-semibold bg-slate-900/60">
          <button
            onClick={() => setActiveTab('PROPOSAL')}
            className={`flex-1 py-2 text-center border-b-2 ${
              activeTab === 'PROPOSAL' ? 'border-emerald-500 text-emerald-400 bg-slate-800/50' : 'border-transparent text-slate-400 hover:text-white'
            }`}
          >
            Questions ({items.length})
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

        {/* Questions Queue */}
        <div className="flex-1 overflow-y-auto divide-y divide-slate-800/60">
          {items.map((item) => (
            <div
              key={item.queryId}
              onClick={() => {
                setSelectedId(item.queryId);
                setEditedAnswer(item.reviewedAnswer || item.draftedAnswer || '');
                if (activeTab === 'MATRIX' || activeTab === 'CLARIFICATIONS') setActiveTab('PROPOSAL');
              }}
              className={`p-3.5 cursor-pointer transition ${
                selectedId === item.queryId ? 'bg-slate-800/90 border-l-4 border-emerald-500' : 'hover:bg-slate-800/40'
              }`}
            >
              <div className="flex items-center justify-between text-[10px] mb-1">
                <span className="font-semibold text-slate-400 truncate max-w-[180px]">
                  {item.sectionName || 'General'}
                </span>
                <div className="flex items-center gap-1.5">
                  {item.guardrailFlag && item.guardrailFlag !== 'TECHNICAL' && (
                    <span className="px-1.5 py-0.5 rounded text-[9px] font-bold bg-amber-950 text-amber-400 border border-amber-800">
                      {item.assignedOwner}
                    </span>
                  )}
                  <span
                    className={`px-1.5 py-0.5 rounded text-[9px] font-bold ${
                      item.isApproved
                        ? 'bg-emerald-950 text-emerald-400 border border-emerald-800'
                        : item.gapFlag
                        ? 'bg-rose-950 text-rose-400 border border-rose-800'
                        : 'bg-blue-950 text-blue-400 border border-blue-800'
                    }`}
                  >
                    {item.gapFlag ? 'CLARIFY' : item.status}
                  </span>
                </div>
              </div>
              <p className="text-xs font-medium text-slate-200 line-clamp-2">{item.question}</p>
              <div className="mt-2 flex items-center justify-between text-[11px] text-slate-400">
                <span className="text-[10px] text-slate-400 uppercase font-mono">{item.requirementType}</span>
                <span className="font-mono text-emerald-400 font-bold">{item.confidenceScore}%</span>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* RIGHT COLUMN: Interactive Work Area */}
      <div className="flex-1 flex flex-col bg-slate-950">
        {/* Sub-Header Tabs */}
        <div className="border-b border-slate-800 px-6 py-2.5 bg-slate-900/40 flex justify-between items-center">
          <div className="flex gap-4 text-xs">
            <button
              onClick={() => setActiveTab('PROPOSAL')}
              className={`pb-1 font-semibold ${activeTab === 'PROPOSAL' ? 'text-white border-b-2 border-emerald-500' : 'text-slate-400'}`}
            >
              Proposal Drafting & Citations
            </button>
            <button
              onClick={() => setActiveTab('CHAT')}
              className={`pb-1 font-semibold ${activeTab === 'CHAT' ? 'text-white border-b-2 border-blue-500' : 'text-slate-400'}`}
            >
              SE Collaborative Chat Assistant
            </button>
            <button
              onClick={() => setActiveTab('MATRIX')}
              className={`pb-1 font-semibold ${activeTab === 'MATRIX' ? 'text-white border-b-2 border-emerald-500' : 'text-slate-400'}`}
            >
              Compliance Matrix View
            </button>
            <button
              onClick={() => setActiveTab('CLARIFICATIONS')}
              className={`pb-1 font-semibold ${activeTab === 'CLARIFICATIONS' ? 'text-white border-b-2 border-amber-500' : 'text-slate-400'}`}
            >
              Clarification Queries List
            </button>
          </div>
          {selectedItem?.latencyMs && (
            <span className="text-[11px] text-slate-500 font-mono">Response Latency: {selectedItem.latencyMs}ms</span>
          )}
        </div>

        {/* TAB 1: PROPOSAL DRAFTING */}
        {activeTab === 'PROPOSAL' && selectedItem && (
          <div className="flex-1 overflow-y-auto p-6 space-y-5 max-w-4xl mx-auto w-full">
            {/* Question Details */}
            <div className="bg-slate-900 border border-slate-800 rounded-lg p-5">
              <div className="flex items-center justify-between text-xs text-slate-400 mb-1">
                <span>{selectedItem.sectionName} • Requirement: <strong>{selectedItem.requirementType}</strong></span>
                <span className="text-emerald-400 font-bold font-mono text-sm">{selectedItem.confidenceScore}% Confidence</span>
              </div>
              <h2 className="text-sm font-semibold text-white mt-1">{selectedItem.question}</h2>

              {/* Guardrail Warnings */}
              {selectedItem.guardrailFlag && selectedItem.guardrailFlag !== 'TECHNICAL' && (
                <div className="mt-3 p-3 rounded bg-amber-950/60 border border-amber-800 text-amber-200 text-xs">
                  <span className="font-bold">⚠️ Guardrail Active: </span>
                  {selectedItem.guardrailReason}
                  <div className="mt-1 font-semibold text-amber-300">Action: Routed to {selectedItem.assignedOwner}</div>
                </div>
              )}

              {selectedItem.outdatedWarning && (
                <div className="mt-2 p-3 rounded bg-orange-950/60 border border-orange-800 text-orange-200 text-xs">
                  {selectedItem.outdatedWarning}
                </div>
              )}
            </div>

            {/* Answer Editor */}
            <div className="bg-slate-900 border border-slate-800 rounded-lg p-5">
              <div className="flex justify-between items-center mb-2">
                <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400">
                  Grounded Islamic Banking Answer
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
                <button
                  onClick={() => setActiveTab('CHAT')}
                  className="text-xs text-blue-400 hover:text-blue-300 flex items-center gap-1 font-semibold"
                >
                  💬 Ask Agent to Rewrite or Shorten in Chat &rarr;
                </button>
                <button
                  onClick={handleApprove}
                  className="px-4 py-2 text-xs font-semibold bg-emerald-600 hover:bg-emerald-500 text-white rounded-md transition shadow"
                >
                  ✓ Approve & Lock Answer
                </button>
              </div>
            </div>

            {/* Source Citations */}
            <div className="bg-slate-900 border border-slate-800 rounded-lg p-5">
              <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3">
                Grounded Knowledge Sources ({selectedItem.citations?.length || 0})
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

        {/* TAB 2: SE CHAT ASSISTANT */}
        {activeTab === 'CHAT' && (
          <div className="flex-1 flex flex-col p-6 max-w-4xl mx-auto w-full">
            <div className="bg-slate-900 border border-slate-800 rounded-t-lg p-4">
              <h3 className="text-sm font-bold text-white flex items-center gap-2">
                <span>🤖</span> SE Collaborative Assistant
              </h3>
              <p className="text-xs text-slate-400">
                Direct the agent to rewrite answers, emphasize AAOIFI Shariah governance, verify formatting, or clarify legal guardrails.
              </p>
            </div>
            <div className="flex-1 overflow-y-auto bg-slate-900/50 border-x border-slate-800 p-4 space-y-3">
              {chatMessages.length === 0 && (
                <div className="text-center text-slate-500 text-xs py-8">
                  Try asking: <br />
                  <span className="text-slate-400 font-mono">"Rewrite this answer to emphasize AAOIFI compliance"</span> or <br />
                  <span className="text-slate-400 font-mono">"Shorten the answer to comply with the 200-word limit"</span> or <br />
                  <span className="text-slate-400 font-mono">"Approve this answer"</span>
                </div>
              )}
              {chatMessages.map((msg, i) => (
                <div key={i} className={`flex ${msg.sender === 'SE' ? 'justify-end' : 'justify-start'}`}>
                  <div
                    className={`max-w-xl text-xs p-3 rounded-lg leading-relaxed ${
                      msg.sender === 'SE' ? 'bg-blue-600 text-white' : 'bg-slate-800 text-slate-100 border border-slate-700'
                    }`}
                  >
                    <div className="text-[10px] font-bold opacity-75 mb-1">{msg.sender === 'SE' ? 'Sales Engineer' : 'RFP Agent'}</div>
                    <div className="whitespace-pre-wrap">{msg.message}</div>
                  </div>
                </div>
              ))}
              {chatLoading && <div className="text-xs text-slate-400 italic">Agent is analyzing and rewriting...</div>}
            </div>
            <form onSubmit={handleSendMessage} className="p-3 bg-slate-900 border border-slate-800 rounded-b-lg flex gap-2">
              <input
                type="text"
                value={chatInput}
                onChange={(e) => setChatInput(e.target.value)}
                placeholder="Give instructions (e.g. 'Rewrite with focus on Murabaha asset ownership')..."
                className="flex-1 text-xs p-2.5 rounded bg-slate-950 border border-slate-700 text-white focus:outline-none focus:ring-1 focus:ring-blue-500"
              />
              <button type="submit" disabled={chatLoading} className="px-4 py-2 text-xs font-semibold bg-blue-600 hover:bg-blue-500 text-white rounded">
                Send
              </button>
            </form>
          </div>
        )}

        {/* TAB 3: COMPLIANCE MATRIX VIEW */}
        {activeTab === 'MATRIX' && (
          <div className="flex-1 overflow-y-auto p-6 max-w-5xl mx-auto w-full">
            <div className="flex justify-between items-center mb-4">
              <div>
                <h2 className="text-base font-bold text-white">Full RFP Compliance Matrix</h2>
                <p className="text-xs text-slate-400">Formal compliance disposition table for submission</p>
              </div>
            </div>
            <div className="bg-slate-900 border border-slate-800 rounded-lg overflow-hidden">
              <table className="w-full text-left text-xs">
                <thead className="bg-slate-800 text-slate-300 text-[11px] uppercase tracking-wider">
                  <tr>
                    <th className="p-3">Clause</th>
                    <th className="p-3">Requirement</th>
                    <th className="p-3">Type</th>
                    <th className="p-3">Compliance</th>
                    <th className="p-3">Owner</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800">
                  {matrixRows.map((row) => (
                    <tr key={row.queryId} className="hover:bg-slate-800/40">
                      <td className="p-3 font-mono text-slate-400 font-bold">{row.clauseNumber}</td>
                      <td className="p-3 font-medium text-slate-200 max-w-xs">{row.requirementText}</td>
                      <td className="p-3 text-[11px] font-mono">{row.requirementType}</td>
                      <td className="p-3">
                        <span
                          className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                            row.complianceStatus === 'COMPLIANT'
                              ? 'bg-emerald-950 text-emerald-400 border border-emerald-800'
                              : row.complianceStatus === 'PARTIALLY_COMPLIANT'
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

        {/* TAB 4: CLARIFICATION QUERIES LIST */}
        {activeTab === 'CLARIFICATIONS' && (
          <div className="flex-1 overflow-y-auto p-6 max-w-4xl mx-auto w-full">
            <div className="mb-4">
              <h2 className="text-base font-bold text-white">Consolidated Customer Clarification Queries</h2>
              <p className="text-xs text-slate-400">Formal questions to submit to the customer rather than making assumptions.</p>
            </div>
            <div className="space-y-3">
              {clarifications.length === 0 ? (
                <div className="text-center text-slate-500 text-xs py-8">No open customer clarifications. All requirements answered confidently!</div>
              ) : (
                clarifications.map((c) => (
                  <div key={c.id} className="p-4 bg-slate-900 border border-slate-800 rounded-lg text-xs space-y-2">
                    <div className="flex justify-between items-center text-slate-400 text-[11px]">
                      <span>Section: {c.sectionName}</span>
                      <span className="text-amber-400 font-bold">Query #{c.id}</span>
                    </div>
                    <h4 className="font-semibold text-white">Client Question: "{c.questionText}"</h4>
                    <div className="p-2.5 bg-slate-950 rounded border border-slate-800 text-amber-200">
                      <strong>Clarification Requested:</strong> {c.clarificationNeeded}
                    </div>
                    <div className="text-[11px] text-slate-400">
                      <strong>Reason Needed:</strong> {c.reasonJustification}
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        )}
      </div>

      {/* Export Modal */}
      {exportModal && (
        <div className="fixed inset-0 bg-black/80 flex items-center justify-center p-6 z-50">
          <div className="bg-slate-900 border border-slate-800 rounded-xl max-w-3xl w-full p-6 space-y-4 max-h-[85vh] flex flex-col">
            <div className="flex justify-between items-center border-b border-slate-800 pb-3">
              <h3 className="text-sm font-bold text-white">Formatted Islamic Banking Proposal Export</h3>
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
