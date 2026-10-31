import React, { useState, useEffect } from 'react';

// API BASE URL
const API_BASE = 'http://localhost:8080/api/v1/rfp';

export interface Citation {
  id: number;
  category: string;
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
  question: string;
  category: string;
  status: 'PENDING' | 'DRAFTED' | 'NEEDS_REVIEW' | 'APPROVED';
  draftedAnswer?: string;
  confidenceScore: number;
  gapFlag: boolean;
  citations: Citation[];
  reviewedAnswer?: string;
  isApproved: boolean;
  latencyMs?: number;
  modelUsed?: string;
}

export default function RfpWorkbench() {
  const [items, setItems] = useState<RfpItem[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [newQuestion, setNewQuestion] = useState('');
  const [clientName, setClientName] = useState('Qatar National Bank');
  const [rfpTitle, setRfpTitle] = useState('Core Banking Upgrade 2026');
  const [loading, setLoading] = useState(false);
  const [editedAnswer, setEditedAnswer] = useState('');
  const [exportModal, setExportModal] = useState<string | null>(null);

  // Fetch all queries on load
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
      console.error('Failed to load queries', e);
    }
  };

  useEffect(() => {
    loadQueries();
  }, []);

  const selectedItem = items.find((i) => i.queryId === selectedId);

  // Submit New Question
  const handleAskQuestion = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newQuestion.trim()) return;
    setLoading(true);
    try {
      const res = await fetch(`${API_BASE}/query`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ question: newQuestion, clientName, rfpTitle }),
      });
      const data: RfpItem = await res.json();
      setItems((prev) => [data, ...prev]);
      setSelectedId(data.queryId);
      setEditedAnswer(data.reviewedAnswer || data.draftedAnswer || '');
      setNewQuestion('');
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  // Milestone 3: Approve & Continuous Learning
  const handleApprove = async () => {
    if (!selectedItem || !selectedItem.responseId) return;
    try {
      const res = await fetch(`${API_BASE}/responses/${selectedItem.responseId}/approve`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ reviewedAnswer: editedAnswer, saveToKnowledgeBase: true }),
      });
      if (res.ok) {
        const updated: RfpItem = await res.json();
        setItems((prev) => prev.map((item) => (item.queryId === updated.queryId ? updated : item)));
        alert('✅ Proposal answer approved and indexed into Knowledge Vault!');
      }
    } catch (e) {
      console.error(e);
    }
  };

  // Milestone 3: One-Click Export
  const handleExport = async () => {
    try {
      const res = await fetch(`${API_BASE}/export/all`);
      if (res.ok) {
        const data = await res.json();
        setExportModal(data.markdownReport);
      }
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <div className="flex h-screen bg-slate-950 text-slate-100 font-sans">
      {/* LEFT COLUMN: RFP Question Queue */}
      <div className="w-1/3 border-r border-slate-800 flex flex-col bg-slate-900/50">
        <div className="p-4 border-b border-slate-800 flex justify-between items-center bg-slate-900">
          <div>
            <h1 className="text-lg font-bold tracking-tight text-white flex items-center gap-2">
              <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse"></span>
              Azentio RFP Copilot
            </h1>
            <p className="text-xs text-slate-400">PgVector Retrieval & Agentic Drafter</p>
          </div>
          <button
            onClick={handleExport}
            className="px-3 py-1.5 text-xs font-semibold bg-emerald-600 hover:bg-emerald-500 text-white rounded-md transition shadow"
          >
            Export All
          </button>
        </div>

        {/* Input Form */}
        <form onSubmit={handleAskQuestion} className="p-4 border-b border-slate-800 bg-slate-900/40">
          <label className="block text-xs font-semibold text-slate-300 mb-1">New Client RFP Question:</label>
          <textarea
            value={newQuestion}
            onChange={(e) => setNewQuestion(e.target.value)}
            placeholder="e.g. Does your platform support multi-region failover and active-active clustering?"
            rows={2}
            className="w-full text-xs p-2 rounded bg-slate-800 border border-slate-700 text-white placeholder-slate-500 focus:outline-none focus:ring-1 focus:ring-emerald-500"
          />
          <button
            type="submit"
            disabled={loading}
            className="mt-2 w-full py-1.5 text-xs font-semibold bg-blue-600 hover:bg-blue-500 text-white rounded transition flex justify-center items-center"
          >
            {loading ? 'Semantic Searching & Drafting...' : '+ Process Question'}
          </button>
        </form>

        {/* Question List */}
        <div className="flex-1 overflow-y-auto divide-y divide-slate-800/60">
          {items.map((item) => (
            <div
              key={item.queryId}
              onClick={() => {
                setSelectedId(item.queryId);
                setEditedAnswer(item.reviewedAnswer || item.draftedAnswer || '');
              }}
              className={`p-3.5 cursor-pointer transition ${
                selectedId === item.queryId ? 'bg-slate-800/90 border-l-4 border-emerald-500' : 'hover:bg-slate-800/40'
              }`}
            >
              <div className="flex items-center justify-between text-xs mb-1">
                <span className="font-semibold text-slate-400 uppercase tracking-wider text-[10px]">
                  #{item.queryId} • {item.category || 'GENERAL'}
                </span>
                <span
                  className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                    item.status === 'APPROVED'
                      ? 'bg-emerald-950 text-emerald-400 border border-emerald-800'
                      : item.gapFlag
                      ? 'bg-rose-950 text-rose-400 border border-rose-800'
                      : 'bg-blue-950 text-blue-400 border border-blue-800'
                  }`}
                >
                  {item.gapFlag ? 'GAP ALERT' : item.status}
                </span>
              </div>
              <p className="text-xs font-medium text-slate-200 line-clamp-2">{item.question}</p>
              <div className="mt-2 flex items-center justify-between text-[11px] text-slate-400">
                <span>Confidence:</span>
                <span
                  className={`font-mono font-bold ${
                    item.confidenceScore >= 80
                      ? 'text-emerald-400'
                      : item.confidenceScore >= 50
                      ? 'text-amber-400'
                      : 'text-rose-400'
                  }`}
                >
                  {item.confidenceScore}%
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* RIGHT COLUMN: AI Response, Confidence & Citations */}
      <div className="flex-1 flex flex-col overflow-y-auto bg-slate-950 p-6">
        {selectedItem ? (
          <div className="max-w-4xl w-full mx-auto space-y-6">
            {/* Header */}
            <div className="bg-slate-900 border border-slate-800 rounded-lg p-5">
              <div className="flex items-center justify-between mb-2">
                <span className="text-xs text-slate-400">
                  Client: <strong className="text-slate-200">{selectedItem.clientName}</strong> | Proposal:{' '}
                  <strong className="text-slate-200">{selectedItem.rfpTitle}</strong>
                </span>
                {selectedItem.latencyMs && (
                  <span className="text-[11px] font-mono text-slate-500">Latency: {selectedItem.latencyMs}ms</span>
                )}
              </div>
              <h2 className="text-base font-semibold text-white">{selectedItem.question}</h2>

              {/* Confidence Score Gauge */}
              <div className="mt-4 pt-4 border-t border-slate-800">
                <div className="flex justify-between items-center text-xs mb-1.5">
                  <span className="font-semibold text-slate-300">PgVector Retrieval Confidence</span>
                  <span
                    className={`font-bold font-mono text-sm ${
                      selectedItem.confidenceScore >= 80
                        ? 'text-emerald-400'
                        : selectedItem.confidenceScore >= 50
                        ? 'text-amber-400'
                        : 'text-rose-400'
                    }`}
                  >
                    {selectedItem.confidenceScore}%
                  </span>
                </div>
                <div className="w-full bg-slate-800 rounded-full h-2.5 overflow-hidden">
                  <div
                    className={`h-2.5 transition-all duration-500 rounded-full ${
                      selectedItem.confidenceScore >= 80
                        ? 'bg-emerald-500'
                        : selectedItem.confidenceScore >= 50
                        ? 'bg-amber-500'
                        : 'bg-rose-500'
                    }`}
                    style={{ width: `${selectedItem.confidenceScore}%` }}
                  ></div>
                </div>
              </div>
            </div>

            {/* Gap Warning Banner if confidence < 50% */}
            {selectedItem.gapFlag && (
              <div className="p-4 rounded-lg bg-rose-950/60 border border-rose-800 text-rose-200 text-xs flex items-start gap-3">
                <span className="text-lg">⚠️</span>
                <div>
                  <h4 className="font-bold text-rose-300 text-sm">Gap Analysis Alert</h4>
                  <p className="mt-0.5 text-rose-200/90 leading-relaxed">
                    No approved proposal answers match this query with &ge; 50% semantic similarity. This response requires manual Subject Matter Expert (SME) authorization before submission.
                  </p>
                </div>
              </div>
            )}

            {/* Drafted & Editable Response */}
            <div className="bg-slate-900 border border-slate-800 rounded-lg p-5">
              <div className="flex justify-between items-center mb-2">
                <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400">
                  Synthesized Proposal Answer
                </h3>
                {selectedItem.isApproved && (
                  <span className="text-xs font-semibold text-emerald-400 bg-emerald-950 border border-emerald-800 px-2.5 py-0.5 rounded">
                    ✓ Approved & Locked
                  </span>
                )}
              </div>
              <textarea
                rows={6}
                value={editedAnswer}
                onChange={(e) => setEditedAnswer(e.target.value)}
                className="w-full text-xs leading-relaxed p-3 rounded bg-slate-950 border border-slate-800 text-slate-100 focus:outline-none focus:ring-1 focus:ring-emerald-500"
              />
              <div className="mt-4 flex justify-between items-center">
                <span className="text-[11px] text-slate-500">
                  Continuous Learning: Approving saves edits back to the Knowledge Vault.
                </span>
                <button
                  onClick={handleApprove}
                  className="px-4 py-2 text-xs font-semibold bg-emerald-600 hover:bg-emerald-500 text-white rounded-md transition shadow flex items-center gap-1.5"
                >
                  <span>✓</span> Approve & Save to Knowledge Base
                </button>
              </div>
            </div>

            {/* Source Citations */}
            <div className="bg-slate-900 border border-slate-800 rounded-lg p-5">
              <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3">
                Verified Source Citations ({selectedItem.citations?.length || 0})
              </h3>
              <div className="space-y-3">
                {selectedItem.citations?.map((cit) => (
                  <div key={cit.id} className="p-3 bg-slate-950 rounded border border-slate-800/80 text-xs">
                    <div className="flex justify-between items-center mb-1">
                      <span className="font-semibold text-blue-400">
                        📄 {cit.sourceDocument} • {cit.sectionReference}
                      </span>
                      <span className="text-[11px] font-mono text-emerald-400">
                        {cit.similarityPercentage}% Match
                      </span>
                    </div>
                    <p className="text-slate-300 italic text-[11px] mt-1">"{cit.approvedAnswer}"</p>
                  </div>
                ))}
              </div>
            </div>
          </div>
        ) : (
          <div className="flex-1 flex items-center justify-center text-slate-500 text-xs">
            Select a question on the left or add a new one to view the AI response.
          </div>
        )}
      </div>

      {/* Export Modal */}
      {exportModal && (
        <div className="fixed inset-0 bg-black/80 flex items-center justify-center p-6 z-50">
          <div className="bg-slate-900 border border-slate-800 rounded-xl max-w-2xl w-full p-6 space-y-4 max-h-[85vh] flex flex-col">
            <div className="flex justify-between items-center border-b border-slate-800 pb-3">
              <h3 className="text-sm font-bold text-white">Full RFP Markdown Export</h3>
              <button onClick={() => setExportModal(null)} className="text-slate-400 hover:text-white text-sm">
                ✕
              </button>
            </div>
            <pre className="flex-1 overflow-y-auto bg-slate-950 p-4 rounded text-[11px] font-mono text-slate-300 border border-slate-800 whitespace-pre-wrap">
              {exportModal}
            </pre>
            <div className="flex justify-end gap-2 pt-2">
              <button
                onClick={() => {
                  navigator.clipboard.writeText(exportModal);
                  alert('Copied Markdown to clipboard!');
                }}
                className="px-4 py-2 text-xs font-semibold bg-emerald-600 hover:bg-emerald-500 text-white rounded transition"
              >
                Copy Markdown
              </button>
              <button
                onClick={() => setExportModal(null)}
                className="px-4 py-2 text-xs font-semibold bg-slate-800 hover:bg-slate-700 text-slate-300 rounded transition"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
