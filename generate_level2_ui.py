# Python script to construct the complete Level 2 UI for Azentio RFP Copilot
import os

html_content = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Azentio iMAL - Level 2 Intelligent RFP Response & Compliance Platform</title>
  <!-- Tailwind CSS via CDN -->
  <script src="https://cdn.tailwindcss.com"></script>
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <script>
    tailwind.config = {
      theme: {
        extend: {
          colors: {
            brand: {
              50: '#eff6ff',
              600: '#1d4ed8',
              800: '#1e40af',
              900: '#0f2756'
            }
          }
        }
      }
    }
  </script>
</head>
<body class="bg-slate-50 text-slate-800 font-sans min-h-screen flex flex-col">

  <!-- Floating Toast Notification -->
  <div id="toastNotification" class="fixed top-5 right-5 z-50 transform transition-all duration-300 translate-y-[-100px] opacity-0 pointer-events-none">
    <div class="flex items-center px-4 py-3 rounded-xl shadow-xl bg-slate-900 text-white space-x-3 border border-slate-700">
      <div id="toastIcon" class="w-7 h-7 rounded-full bg-emerald-500/20 text-emerald-400 flex items-center justify-center font-bold text-sm">
        <i class="fa-solid fa-check"></i>
      </div>
      <div>
        <p id="toastTitle" class="text-xs font-bold">Action Completed</p>
        <p id="toastMsg" class="text-[11px] text-slate-300">Successfully updated.</p>
      </div>
    </div>
  </div>

  <!-- Main Top Navigation -->
  <header class="bg-white border-b border-slate-200 sticky top-0 z-30 shadow-sm">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
      
      <!-- Brand & Title -->
      <div class="flex items-center space-x-3">
        <div class="w-10 h-10 rounded-xl bg-blue-900 text-white flex items-center justify-center font-bold text-lg shadow">
          <i class="fa-solid fa-building-columns"></i>
        </div>
        <div>
          <div class="flex items-center space-x-2">
            <h1 class="text-base font-bold text-slate-900">Azentio iMAL RFP Platform</h1>
            <span class="px-2 py-0.5 text-[11px] font-semibold bg-emerald-100 text-emerald-800 rounded-full border border-emerald-300">
              Level 2: Compliance & Gap Engine
            </span>
          </div>
          <p class="text-xs text-slate-500">Intelligent Gap Analysis, Forensic Validation & Proposal Assembly</p>
        </div>
      </div>

      <!-- Navigation Tabs (6 Level 2 Modules) -->
      <nav class="hidden lg:flex items-center space-x-1 bg-slate-100 p-1 rounded-xl text-xs">
        <button onclick="switchTab('upload')" id="tab-btn-upload" class="px-3 py-1.5 font-bold rounded-lg transition bg-white text-blue-900 shadow-sm">
          <i class="fa-solid fa-file-arrow-up mr-1 text-blue-700"></i> 1. Ingest
        </button>
        <button onclick="switchTab('workbench')" id="tab-btn-workbench" class="px-3 py-1.5 font-medium rounded-lg text-slate-600 hover:text-slate-900 transition">
          <i class="fa-solid fa-pen-to-square mr-1 text-slate-500"></i> 2. Review
        </button>
        <button onclick="switchTab('gapanalysis')" id="tab-btn-gapanalysis" class="px-3 py-1.5 font-medium rounded-lg text-slate-600 hover:text-slate-900 transition">
          <i class="fa-solid fa-magnifying-glass-chart mr-1 text-slate-500"></i> 3. Gap Analysis
        </button>
        <button onclick="switchTab('validation')" id="tab-btn-validation" class="px-3 py-1.5 font-medium rounded-lg text-slate-600 hover:text-slate-900 transition">
          <i class="fa-solid fa-shield-halved mr-1 text-slate-500"></i> 4. Validation
        </button>
        <button onclick="switchTab('clarifications')" id="tab-btn-clarifications" class="px-3 py-1.5 font-medium rounded-lg text-slate-600 hover:text-slate-900 transition">
          <i class="fa-solid fa-circle-question mr-1 text-slate-500"></i> 5. Clarifications
        </button>
        <button onclick="switchTab('assembly')" id="tab-btn-assembly" class="px-3 py-1.5 font-medium rounded-lg text-slate-600 hover:text-slate-900 transition">
          <i class="fa-solid fa-file-contract mr-1 text-slate-500"></i> 6. Assembly & Matrix
        </button>
      </nav>

      <!-- Connection Status & Actions -->
      <div class="flex items-center space-x-2">
        <button 
          onclick="runFullLevel2Analysis()" 
          id="btnTriggerL2"
          class="inline-flex items-center px-3 py-1.5 rounded-xl text-xs font-bold bg-blue-900 hover:bg-blue-800 text-white shadow transition"
          title="Run Level 2 Interpretation, Compliance Matrix & Gap Engine"
        >
          <i class="fa-solid fa-bolt mr-1.5 text-amber-300"></i>
          <span>Run Level 2 Engine</span>
        </button>

        <button 
          onclick="resetAllRfpData()" 
          class="inline-flex items-center px-2.5 py-1.5 rounded-xl text-xs font-semibold bg-rose-50 hover:bg-rose-100 text-rose-700 border border-rose-200 transition"
          title="Clear all tender records"
        >
          <i class="fa-solid fa-arrows-rotate mr-1 text-rose-500"></i>
          <span>Reset</span>
        </button>

        <div id="connectionStatus" class="flex items-center space-x-1 px-2.5 py-1 rounded-full text-[11px] font-medium bg-emerald-50 text-emerald-700 border border-emerald-200">
          <span class="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
          <span>Online</span>
        </div>
      </div>

    </div>
  </header>

  <!-- Main Workspace -->
  <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 flex-1 w-full space-y-6">

    <!-- ============================================================ -->
    <!-- TAB 1: UPLOAD & INGESTION                                    -->
    <!-- ============================================================ -->
    <div id="tab-view-upload" class="space-y-6">
      
      <div class="max-w-3xl mx-auto text-center space-y-2">
        <h2 class="text-2xl font-bold text-slate-900">Upload RFP Tender Document</h2>
        <p class="text-sm text-slate-500">
          Upload customer tender questionnaires in Excel (.xlsx), PDF, or Word (.docx). The Level 2 agent extracts requirements, identifies inter-dependencies, evaluates multi-product components, and maps customer response codes (S / C / D / T / N).
        </p>
      </div>

      <!-- Upload Dropzone Card -->
      <div class="max-w-2xl mx-auto bg-white rounded-2xl border-2 border-dashed border-slate-300 hover:border-blue-500 transition-colors p-8 text-center space-y-4 shadow-sm">
        
        <div class="w-16 h-16 mx-auto rounded-2xl bg-blue-50 text-blue-700 flex items-center justify-center text-2xl shadow-inner">
          <i class="fa-solid fa-cloud-arrow-up"></i>
        </div>

        <div>
          <h3 class="text-base font-bold text-slate-800">Select RFP Document to Ingest</h3>
          <p class="text-xs text-slate-500 mt-1">Supports Microsoft Excel (.xlsx), PDF, Word (.docx), or CSV</p>
        </div>

        <input 
          type="file" 
          id="rfpFileInput" 
          accept=".xlsx,.xls,.pdf,.docx,.csv,.txt" 
          class="hidden" 
          onchange="handleFileSelect(event)"
        />

        <div class="flex flex-col sm:flex-row items-center justify-center gap-3 pt-2">
          <button 
            type="button" 
            onclick="document.getElementById('rfpFileInput').click()"
            class="px-5 py-2.5 rounded-xl bg-blue-900 hover:bg-blue-800 text-white font-bold text-xs shadow transition flex items-center"
          >
            <i class="fa-solid fa-folder-open mr-2"></i> Browse Local File
          </button>
          
          <button 
            id="btnIngest"
            type="button" 
            onclick="uploadSelectedFile()"
            class="px-5 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs shadow transition flex items-center"
          >
            <i class="fa-solid fa-bolt mr-2"></i> Ingest & Parse
          </button>
        </div>

        <!-- Selected File Status Display -->
        <div class="pt-3 border-t border-slate-100 flex items-center justify-center space-x-2 text-xs text-slate-600">
          <span>Active Selection:</span>
          <span id="selectedFileName" class="font-mono font-bold text-slate-900 bg-slate-100 px-2.5 py-1 rounded-md">
            QIB_Islamic_Banking_RFP_2026.xlsx
          </span>
          <a 
            href="/QIB_Islamic_Banking_RFP_2026.xlsx" 
            download 
            class="inline-flex items-center text-blue-700 hover:text-blue-900 font-semibold underline ml-1"
          >
            <i class="fa-solid fa-download mr-1"></i> Download .xlsx
          </a>
        </div>

        <!-- Pre-Ingestion Customer Compliance Code Selection -->
        <div class="pt-3 border-t border-slate-100 text-left bg-slate-50 p-4 rounded-xl space-y-2">
          <div class="flex items-center justify-between text-xs">
            <span class="font-bold text-slate-800">Customer Compliance Code Schema:</span>
            <span class="px-2 py-0.5 rounded text-[10px] font-semibold bg-blue-100 text-blue-800">Customer-Adaptive</span>
          </div>
          <div class="grid grid-cols-2 sm:grid-cols-5 gap-2 text-xs text-center">
            <div class="p-2 rounded-lg bg-emerald-50 border border-emerald-200">
              <span class="font-bold text-emerald-800 block">Code S</span>
              <span class="text-[10px] text-emerald-600">Standard Capability</span>
            </div>
            <div class="p-2 rounded-lg bg-blue-50 border border-blue-200">
              <span class="font-bold text-blue-800 block">Code C</span>
              <span class="text-[10px] text-blue-600">Configuration Req.</span>
            </div>
            <div class="p-2 rounded-lg bg-amber-50 border border-amber-200">
              <span class="font-bold text-amber-800 block">Code D</span>
              <span class="text-[10px] text-amber-600">Development Req.</span>
            </div>
            <div class="p-2 rounded-lg bg-purple-50 border border-purple-200">
              <span class="font-bold text-purple-800 block">Code T</span>
              <span class="text-[10px] text-purple-600">Third-Party Dep.</span>
            </div>
            <div class="p-2 rounded-lg bg-rose-50 border border-rose-200">
              <span class="font-bold text-rose-800 block">Code N</span>
              <span class="text-[10px] text-rose-600">Not Available</span>
            </div>
          </div>
        </div>

      </div>

      <!-- Quick Benchmark Button -->
      <div class="max-w-md mx-auto text-center">
        <button 
          onclick="loadDemoBenchmark()"
          class="text-xs text-blue-700 hover:text-blue-900 font-semibold inline-flex items-center space-x-1"
        >
          <i class="fa-solid fa-play text-amber-500 mr-1"></i>
          <span>Re-load Qatar Islamic Bank Core RFP Benchmark</span>
        </button>
      </div>

    </div>

    <!-- ============================================================ -->
    <!-- TAB 2: SE REVIEW & COLLABORATIVE WORKBENCH                   -->
    <!-- ============================================================ -->
    <div id="tab-view-workbench" class="hidden space-y-6">

      <!-- Workbench Header & Filter Bar -->
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-3 border-b border-slate-200">
        <div>
          <div class="flex items-center space-x-2">
            <h2 class="text-xl font-bold text-slate-900">Sales Engineering Review Workbench</h2>
            <span id="activeTenderBadge" class="px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-emerald-100 text-emerald-800 border border-emerald-200">
              Active RFP: Qatar Islamic Bank Tender
            </span>
          </div>
          <p class="text-xs text-slate-500 mt-0.5">Enriched with Delivery Codes (S/C/D/T/N), Multi-Product Span, Requirement Dependencies, and Forensic Validation.</p>
        </div>

        <!-- Filter Controls -->
        <div class="flex flex-wrap items-center gap-3">
          <div class="flex items-center space-x-1.5 text-xs">
            <span class="text-slate-500 font-medium">Department:</span>
            <select id="teamFilterSelect" onchange="filterByTeam(this.value)" class="text-xs font-semibold py-1 px-2.5 rounded-lg border border-slate-300 bg-white text-slate-800 focus:outline-none focus:ring-1 focus:ring-blue-600">
              <option value="ALL">🏢 All Departments</option>
              <option value="SALES_ENGINEERING">💼 Sales Engineering</option>
              <option value="SOLUTION_ARCHITECTURE">⚙️ Solution Architecture</option>
              <option value="PRODUCT_MANAGEMENT">📦 Product Management</option>
              <option value="CORE_ENGINEERING">🛠️ Core Engineering</option>
              <option value="SHARIAH_BOARD">📜 Shariah Advisory Board</option>
              <option value="COMMERCIAL_LEGAL">⚖️ Commercial & Legal Desk</option>
            </select>
          </div>

          <!-- Status Filter Buttons -->
          <div class="inline-flex rounded-lg bg-slate-100 p-1 text-xs">
            <button onclick="filterWorkbench('ALL')" id="filter-btn-all" class="px-3 py-1 font-bold rounded-md bg-white text-slate-800 shadow-sm">All</button>
            <button onclick="filterWorkbench('Fully Addressed')" id="filter-btn-fully" class="px-3 py-1 rounded-md text-slate-600 hover:text-slate-900">Standard (S)</button>
            <button onclick="filterWorkbench('Partially Addressed')" id="filter-btn-partial" class="px-3 py-1 rounded-md text-slate-600 hover:text-slate-900">Config / Dev (C/D)</button>
            <button onclick="filterWorkbench('Information Required')" id="filter-btn-info" class="px-3 py-1 rounded-md text-slate-600 hover:text-slate-900">Clarification / Gap</button>
          </div>
        </div>
      </div>

      <!-- Department Workload KPI Bar -->
      <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-2 text-xs">
        <div onclick="selectTeamFilter('SALES_ENGINEERING')" class="cursor-pointer bg-blue-50/80 hover:bg-blue-100 p-2.5 rounded-xl border border-blue-200 transition">
          <span class="text-blue-700 font-medium block text-[11px]">💼 Sales Engineering</span>
          <strong id="teamKpiSE" class="text-sm font-extrabold text-blue-900">0 reqs</strong>
        </div>
        <div onclick="selectTeamFilter('SOLUTION_ARCHITECTURE')" class="cursor-pointer bg-indigo-50/80 hover:bg-indigo-100 p-2.5 rounded-xl border border-indigo-200 transition">
          <span class="text-indigo-700 font-medium block text-[11px]">⚙️ Architecture (Code T)</span>
          <strong id="teamKpiSA" class="text-sm font-extrabold text-indigo-900">0 reqs</strong>
        </div>
        <div onclick="selectTeamFilter('PRODUCT_MANAGEMENT')" class="cursor-pointer bg-purple-50/80 hover:bg-purple-100 p-2.5 rounded-xl border border-purple-200 transition">
          <span class="text-purple-700 font-medium block text-[11px]">📦 Product Mgmt (Gaps)</span>
          <strong id="teamKpiPM" class="text-sm font-extrabold text-purple-900">0 reqs</strong>
        </div>
        <div onclick="selectTeamFilter('CORE_ENGINEERING')" class="cursor-pointer bg-amber-50/80 hover:bg-amber-100 p-2.5 rounded-xl border border-amber-200 transition">
          <span class="text-amber-700 font-medium block text-[11px]">🛠️ Core Eng (Code D)</span>
          <strong id="teamKpiEng" class="text-sm font-extrabold text-amber-900">0 reqs</strong>
        </div>
        <div onclick="selectTeamFilter('SHARIAH_BOARD')" class="cursor-pointer bg-emerald-50/80 hover:bg-emerald-100 p-2.5 rounded-xl border border-emerald-200 transition">
          <span class="text-emerald-700 font-medium block text-[11px]">📜 Shariah Governance</span>
          <strong id="teamKpiShariah" class="text-sm font-extrabold text-emerald-900">0 reqs</strong>
        </div>
        <div onclick="selectTeamFilter('COMMERCIAL_LEGAL')" class="cursor-pointer bg-rose-50/80 hover:bg-rose-100 p-2.5 rounded-xl border border-rose-200 transition">
          <span class="text-rose-700 font-medium block text-[11px]">⚖️ Legal & Commercial</span>
          <strong id="teamKpiCommercial" class="text-sm font-extrabold text-rose-900">0 reqs</strong>
        </div>
      </div>

      <!-- Requirements Cards Container -->
      <div id="requirementsList" class="space-y-4">
        <!-- Rendered dynamically by JavaScript with Level 2 attributes -->
        <p class="text-xs text-slate-400 italic">Loading requirements...</p>
      </div>

    </div>

    <!-- ============================================================ -->
    <!-- TAB 3: AI-DRIVEN GAP ANALYSIS & ESCALATIONS                  -->
    <!-- ============================================================ -->
    <div id="tab-view-gapanalysis" class="hidden space-y-6">

      <div class="flex items-center justify-between pb-3 border-b border-slate-200">
        <div>
          <h2 class="text-xl font-bold text-slate-900">AI-Driven Gap Analysis Cockpit</h2>
          <p class="text-xs text-slate-500">Crucially distinguishes Confirmed Product Gaps from Knowledge Gaps, and coordinates cross-department escalations.</p>
        </div>
        <button onclick="fetchGapAnalysis()" class="px-3 py-1.5 rounded-xl bg-blue-900 hover:bg-blue-800 text-white font-bold text-xs shadow flex items-center">
          <i class="fa-solid fa-rotate mr-1.5"></i> Refresh Gaps
        </button>
      </div>

      <!-- Gap KPI Summary Cards -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div class="bg-white p-5 rounded-2xl border border-rose-200 shadow-sm bg-gradient-to-br from-white to-rose-50/50">
          <div class="flex items-center justify-between">
            <span class="text-xs font-bold text-rose-800 uppercase tracking-wider">Confirmed Product Gaps</span>
            <i class="fa-solid fa-ban text-rose-500"></i>
          </div>
          <div class="mt-2 flex items-baseline">
            <span id="gapKpiConfirmedProduct" class="text-3xl font-extrabold text-rose-700">0</span>
            <span class="ml-2 text-xs text-rose-600">release limitations</span>
          </div>
          <p class="text-[11px] text-slate-500 mt-1">Requires PM roadmap decision or Code N</p>
        </div>

        <div class="bg-white p-5 rounded-2xl border border-amber-200 shadow-sm bg-gradient-to-br from-white to-amber-50/50">
          <div class="flex items-center justify-between">
            <span class="text-xs font-bold text-amber-800 uppercase tracking-wider">Knowledge Gaps</span>
            <i class="fa-solid fa-book-bookmark text-amber-500"></i>
          </div>
          <div class="mt-2 flex items-baseline">
            <span id="gapKpiKnowledge" class="text-3xl font-extrabold text-amber-700">0</span>
            <span class="ml-2 text-xs text-amber-600">missing docs</span>
          </div>
          <p class="text-[11px] text-slate-500 mt-1">Product supports, but indexed docs lack proof</p>
        </div>

        <div class="bg-white p-5 rounded-2xl border border-indigo-200 shadow-sm bg-gradient-to-br from-white to-indigo-50/50">
          <div class="flex items-center justify-between">
            <span class="text-xs font-bold text-indigo-800 uppercase tracking-wider">Integration Dependencies</span>
            <i class="fa-solid fa-network-wired text-indigo-500"></i>
          </div>
          <div class="mt-2 flex items-baseline">
            <span id="gapKpiIntegration" class="text-3xl font-extrabold text-indigo-700">0</span>
            <span class="ml-2 text-xs text-indigo-600">Code T</span>
          </div>
          <p class="text-[11px] text-slate-500 mt-1">Requires external broker or central bank gateway</p>
        </div>

        <div class="bg-white p-5 rounded-2xl border border-emerald-200 shadow-sm bg-gradient-to-br from-white to-emerald-50/50">
          <div class="flex items-center justify-between">
            <span class="text-xs font-bold text-emerald-800 uppercase tracking-wider">Custom Dev Required</span>
            <i class="fa-solid fa-code text-emerald-500"></i>
          </div>
          <div class="mt-2 flex items-baseline">
            <span id="gapKpiDev" class="text-3xl font-extrabold text-emerald-700">0</span>
            <span class="ml-2 text-xs text-emerald-600">Code D</span>
          </div>
          <p class="text-[11px] text-slate-500 mt-1">Requires engineering sizing & man-day estimate</p>
        </div>
      </div>

      <!-- Gap Items Table -->
      <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        <div class="p-4 bg-slate-50 border-b border-slate-200 flex items-center justify-between">
          <h3 class="text-sm font-bold text-slate-800 flex items-center">
            <i class="fa-solid fa-list-check mr-2 text-blue-700"></i>
            Detailed Capability Gap Register & Escalations
          </h3>
          <span class="text-xs text-slate-500" id="gapRegisterCount">0 gaps identified</span>
        </div>
        <div class="overflow-x-auto">
          <table class="w-full text-left text-xs text-slate-700">
            <thead class="bg-slate-100 text-slate-600 uppercase text-[10px] tracking-wider border-b border-slate-200">
              <tr>
                <th class="p-3">Req ID & Section</th>
                <th class="p-3">Requirement Text</th>
                <th class="p-3">Gap Classification</th>
                <th class="p-3">Technical Explanation</th>
                <th class="p-3">Assigned Department</th>
                <th class="p-3">Recommended Action</th>
              </tr>
            </thead>
            <tbody id="gapItemsTableBody" class="divide-y divide-slate-100">
              <tr>
                <td colspan="6" class="p-6 text-center text-slate-400">Loading gap analysis...</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

    </div>

    <!-- ============================================================ -->
    <!-- TAB 4: FORENSIC RESPONSE VALIDATION                          -->
    <!-- ============================================================ -->
    <div id="tab-view-validation" class="hidden space-y-6">

      <div class="flex items-center justify-between pb-3 border-b border-slate-200">
        <div>
          <h2 class="text-xl font-bold text-slate-900">Forensic Response Validation Gate</h2>
          <p class="text-xs text-slate-500">Cross-checks drafted answers against citations, flags unsupported claims, and detects code-narrative mismatches before submission.</p>
        </div>
        <div class="flex items-center space-x-2">
          <span id="validationGateBadge" class="px-3 py-1 rounded-full text-xs font-bold bg-amber-100 text-amber-800 border border-amber-300">
            Checking Gate...
          </span>
          <button onclick="fetchValidationReport()" class="px-3 py-1.5 rounded-xl bg-blue-900 hover:bg-blue-800 text-white font-bold text-xs shadow flex items-center">
            <i class="fa-solid fa-shield-virus mr-1.5"></i> Run Audit
          </button>
        </div>
      </div>

      <!-- Audit Metric Cards -->
      <div class="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div class="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
          <span class="text-xs text-slate-500 block">Code-Narrative Mismatches</span>
          <span id="valCountMismatches" class="text-2xl font-extrabold text-rose-700">0</span>
          <span class="text-[10px] text-rose-500 block font-semibold">BLOCKING GATE</span>
        </div>
        <div class="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
          <span class="text-xs text-slate-500 block">Unsupported Claims</span>
          <span id="valCountClaims" class="text-2xl font-extrabold text-rose-700">0</span>
          <span class="text-[10px] text-rose-500 block font-semibold">BLOCKING GATE</span>
        </div>
        <div class="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
          <span class="text-xs text-slate-500 block">Cross Contradictions</span>
          <span id="valCountCross" class="text-2xl font-extrabold text-amber-700">0</span>
          <span class="text-[10px] text-amber-500 block font-semibold">WARNING</span>
        </div>
        <div class="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
          <span class="text-xs text-slate-500 block">Passed Forensic Checks</span>
          <span id="valCountPassed" class="text-2xl font-extrabold text-emerald-700">0</span>
          <span class="text-[10px] text-emerald-600 block font-semibold">VERIFIED</span>
        </div>
      </div>

      <!-- Issues List Container -->
      <div class="bg-white rounded-2xl border border-slate-200 shadow-sm p-6 space-y-4">
        <h3 class="text-sm font-bold text-slate-800 flex items-center">
          <i class="fa-solid fa-triangle-exclamation mr-2 text-rose-600"></i>
          Audit Findings & Remediation Required
        </h3>
        <div id="validationIssuesContainer" class="space-y-3">
          <p class="text-xs text-slate-400 italic">No validation issues logged.</p>
        </div>
      </div>

    </div>

    <!-- ============================================================ -->
    <!-- TAB 5: CUSTOMER CLARIFICATION PACK                           -->
    <!-- ============================================================ -->
    <div id="tab-view-clarifications" class="hidden space-y-6">

      <div class="flex items-center justify-between pb-3 border-b border-slate-200">
        <div>
          <h2 class="text-xl font-bold text-slate-900">Customer Clarification Query Pack</h2>
          <p class="text-xs text-slate-500">Structured, prioritized, and deduplicated queries for the formal client procurement clarification window.</p>
        </div>
        <button onclick="downloadClarificationDoc()" class="px-4 py-2 rounded-xl bg-blue-900 hover:bg-blue-800 text-white font-bold text-xs shadow flex items-center">
          <i class="fa-solid fa-file-arrow-down mr-2"></i> Download Formal Clarification Letter
        </button>
      </div>

      <!-- Impact Stats -->
      <div class="grid grid-cols-1 sm:grid-cols-3 gap-3">
        <div class="bg-white p-4 rounded-xl border border-indigo-100 shadow-sm">
          <span class="text-xs text-indigo-700 font-bold block">Solution Architecture Impact</span>
          <span id="clarifSolCount" class="text-2xl font-extrabold text-indigo-900">0</span>
          <span class="text-[11px] text-slate-400 block">Topology & protocol confirmations</span>
        </div>
        <div class="bg-white p-4 rounded-xl border border-amber-100 shadow-sm">
          <span class="text-xs text-amber-700 font-bold block">Delivery Timeline Impact</span>
          <span id="clarifDelCount" class="text-2xl font-extrabold text-amber-900">0</span>
          <span class="text-[11px] text-slate-400 block">SLA & latency dependencies</span>
        </div>
        <div class="bg-white p-4 rounded-xl border border-rose-100 shadow-sm">
          <span class="text-xs text-rose-700 font-bold block">Commercial Scope Impact</span>
          <span id="clarifComCount" class="text-2xl font-extrabold text-rose-900">0</span>
          <span class="text-[11px] text-slate-400 block">Broker venue & license dependencies</span>
        </div>
      </div>

      <!-- Consolidated Clarification Clusters -->
      <div class="bg-white rounded-2xl border border-slate-200 shadow-sm p-6 space-y-4">
        <h3 class="text-sm font-bold text-slate-800 flex items-center">
          <i class="fa-solid fa-layer-group mr-2 text-blue-700"></i>
          Consolidated Clarification Clusters (Deduplicated)
        </h3>
        <div id="clarificationClustersList" class="space-y-4">
          <p class="text-xs text-slate-400 italic">Loading clarification clusters...</p>
        </div>
      </div>

    </div>

    <!-- ============================================================ -->
    <!-- TAB 6: COMPLIANCE MATRIX & PROPOSAL ASSEMBLY                 -->
    <!-- ============================================================ -->
    <div id="tab-view-assembly" class="hidden space-y-6">

      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 pb-3 border-b border-slate-200">
        <div>
          <h2 class="text-xl font-bold text-slate-900">Proposal Assembly & Compliance Matrix</h2>
          <p class="text-xs text-slate-500">Requirement-by-requirement customer compliance matrix (S/C/D/T/N) and submission gate checklist.</p>
        </div>
        <div class="flex items-center space-x-2">
          <button onclick="openSelectiveRegenModal()" class="px-3 py-2 rounded-xl bg-indigo-50 hover:bg-indigo-100 text-indigo-700 font-bold text-xs border border-indigo-200 transition flex items-center">
            <i class="fa-solid fa-arrows-split-up-and-left mr-1.5"></i> Selective Regeneration
          </button>
          <button onclick="downloadCustomerExcel()" class="px-4 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs shadow transition flex items-center">
            <i class="fa-solid fa-file-excel mr-2"></i> Export Customer Template (.CSV)
          </button>
        </div>
      </div>

      <!-- Submission Readiness Cockpit -->
      <div class="bg-gradient-to-r from-slate-900 via-blue-950 to-slate-900 rounded-2xl p-6 text-white shadow-xl space-y-4">
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
          <div>
            <span class="text-xs font-semibold uppercase tracking-wider text-amber-300">Submission Gatekeeper Status</span>
            <div class="flex items-center space-x-3 mt-1">
              <span id="cockpitOverallStatus" class="text-2xl font-extrabold text-white">READY_FOR_SUBMISSION</span>
              <span id="cockpitScoreBadge" class="px-3 py-1 rounded-full text-xs font-bold bg-emerald-500/20 text-emerald-300 border border-emerald-400/30">
                100% Readiness Score
              </span>
            </div>
          </div>
          <div class="text-right text-xs text-slate-300">
            <span class="block">Client Deadline: <strong id="cockpitDeadline" class="text-white">2026-11-30</strong></span>
            <span class="block">Preserving Section Numbering: <strong class="text-emerald-400">SEC-X.Y Native</strong></span>
          </div>
        </div>

        <!-- Checklist Grid -->
        <div id="submissionChecklistItems" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 pt-3 border-t border-white/10 text-xs">
          <!-- Populated by JavaScript -->
        </div>
      </div>

      <!-- Level 2 Compliance Matrix Table -->
      <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        <div class="p-4 bg-slate-50 border-b border-slate-200 flex items-center justify-between">
          <h3 class="text-sm font-bold text-slate-800 flex items-center">
            <i class="fa-solid fa-table-list mr-2 text-blue-700"></i>
            Requirement-by-Requirement Compliance Matrix
          </h3>
          <span class="text-xs text-slate-500">Customer Coding: S, C, D, T, N</span>
        </div>
        <div class="overflow-x-auto">
          <table class="w-full text-left text-xs text-slate-700">
            <thead class="bg-slate-100 text-slate-600 uppercase text-[10px] tracking-wider border-b border-slate-200">
              <tr>
                <th class="p-3">Clause & Req ID</th>
                <th class="p-3">Section</th>
                <th class="p-3">Requirement Text</th>
                <th class="p-3">Code</th>
                <th class="p-3">Classification</th>
                <th class="p-3">Vendor Response Narrative</th>
                <th class="p-3">Department & Sizing</th>
                <th class="p-3">Validation</th>
              </tr>
            </thead>
            <tbody id="complianceMatrixTableBody" class="divide-y divide-slate-100">
              <tr>
                <td colspan="8" class="p-6 text-center text-slate-400">Loading compliance matrix...</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

    </div>

  </main>

  <!-- Selective Regeneration Modal -->
  <div id="selectiveRegenModal" class="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center hidden p-4">
    <div class="bg-white rounded-2xl shadow-2xl max-w-lg w-full p-6 space-y-4">
      <div class="flex items-center justify-between pb-3 border-b border-slate-100">
        <h3 class="text-sm font-bold text-slate-900 flex items-center">
          <i class="fa-solid fa-arrows-split-up-and-left mr-2 text-indigo-600"></i>
          Selective Section Regeneration
        </h3>
        <button onclick="closeSelectiveRegenModal()" class="text-slate-400 hover:text-slate-600"><i class="fa-solid fa-xmark"></i></button>
      </div>
      <p class="text-xs text-slate-500">
        Regenerate unapproved proposal sections while strictly preserving all Sales Engineer approved responses.
      </p>
      <div class="space-y-3 text-xs">
        <div>
          <label class="font-bold text-slate-700 block mb-1">Target Section (Optional):</label>
          <input type="text" id="regenSectionInput" placeholder="e.g. Section 3: Architecture" class="w-full p-2 border border-slate-300 rounded-lg text-xs" />
        </div>
        <div>
          <label class="font-bold text-slate-700 block mb-1">Guiding Prompt / Refinement Instruction:</label>
          <textarea id="regenPromptInput" rows="2" placeholder="e.g. Emphasize AAOIFI Shariah compliance and synchronous DB replication" class="w-full p-2 border border-slate-300 rounded-lg text-xs"></textarea>
        </div>
        <div class="flex items-center space-x-2 pt-1">
          <input type="checkbox" id="preserveApprovedCheckbox" checked class="rounded text-blue-600" />
          <label for="preserveApprovedCheckbox" class="font-bold text-emerald-800">Preserve Approved Responses (Strict Guardrail)</label>
        </div>
      </div>
      <div class="flex items-center justify-end space-x-2 pt-3 border-t border-slate-100">
        <button onclick="closeSelectiveRegenModal()" class="px-4 py-2 rounded-xl text-xs font-semibold text-slate-600 hover:bg-slate-100">Cancel</button>
        <button onclick="executeSelectiveRegeneration()" class="px-4 py-2 rounded-xl text-xs font-bold bg-indigo-600 hover:bg-indigo-700 text-white shadow">Execute Selective Regeneration</button>
      </div>
    </div>
  </div>

  <!-- JavaScript Application Logic -->
  <script>
    const API_BASE = 'http://localhost:8080/api/v1/rfp';
    const API_V2_BASE = 'http://localhost:8080/api/v2/rfp';
    let allQueries = [];
    let selectedFile = null;
    let currentFilter = 'ALL';
    let currentTeamFilter = 'ALL';
    let currentDocumentId = localStorage.getItem('azentio_active_doc_id') || null;

    window.addEventListener('DOMContentLoaded', () => {
      fetchQueries(currentDocumentId);
      fetchGapAnalysis(currentDocumentId);
      fetchValidationReport(currentDocumentId);
      fetchClarificationPack(currentDocumentId);
      fetchSubmissionChecklist(currentDocumentId);
    });

    // Tab Switching Logic across 6 Level 2 Views
    function switchTab(tabId) {
      const tabs = ['upload', 'workbench', 'gapanalysis', 'validation', 'clarifications', 'assembly'];
      tabs.forEach(id => {
        const view = document.getElementById(`tab-view-${id}`);
        const btn = document.getElementById(`tab-btn-${id}`);
        if (!view || !btn) return;
        if (id === tabId) {
          view.classList.remove('hidden');
          btn.className = 'px-3 py-1.5 font-bold rounded-lg transition bg-white text-blue-900 shadow-sm';
        } else {
          view.classList.add('hidden');
          btn.className = 'px-3 py-1.5 font-medium rounded-lg text-slate-600 hover:text-slate-900 transition';
        }
      });

      if (tabId === 'workbench') fetchQueries(currentDocumentId);
      if (tabId === 'gapanalysis') fetchGapAnalysis(currentDocumentId);
      if (tabId === 'validation') fetchValidationReport(currentDocumentId);
      if (tabId === 'clarifications') fetchClarificationPack(currentDocumentId);
      if (tabId === 'assembly') {
        fetchSubmissionChecklist(currentDocumentId);
        fetchLevel2ComplianceMatrix(currentDocumentId);
      }
    }

    function showToast(title, msg, isSuccess = true) {
      const toast = document.getElementById('toastNotification');
      document.getElementById('toastTitle').textContent = title;
      document.getElementById('toastMsg').textContent = msg;
      const icon = document.getElementById('toastIcon');
      if (isSuccess) {
        icon.className = 'w-7 h-7 rounded-full bg-emerald-500/20 text-emerald-400 flex items-center justify-center font-bold text-sm';
        icon.innerHTML = '<i class="fa-solid fa-check"></i>';
      } else {
        icon.className = 'w-7 h-7 rounded-full bg-rose-500/20 text-rose-400 flex items-center justify-center font-bold text-sm';
        icon.innerHTML = '<i class="fa-solid fa-triangle-exclamation"></i>';
      }
      toast.classList.remove('translate-y-[-100px]', 'opacity-0');
      toast.classList.add('translate-y-0', 'opacity-100');
      setTimeout(() => {
        toast.classList.remove('translate-y-0', 'opacity-100');
        toast.classList.add('translate-y-[-100px]', 'opacity-0');
      }, 4000);
    }

    function handleFileSelect(e) {
      if (e.target.files && e.target.files[0]) {
        selectedFile = e.target.files[0];
        const nameEl = document.getElementById('selectedFileName');
        if (nameEl) nameEl.textContent = `${selectedFile.name} (${Math.round(selectedFile.size / 1024)} KB)`;
        showToast('File Selected', `Ready to ingest ${selectedFile.name}`);
      }
    }

    async function uploadSelectedFile() {
      const btn = document.getElementById('btnIngest');
      const original = btn.innerHTML;
      btn.innerHTML = `<i class="fa-solid fa-spinner fa-spin mr-2"></i> Ingesting...`;
      btn.disabled = true;

      try {
        const formData = new FormData();
        if (selectedFile) {
          formData.append('file', selectedFile);
          formData.append('rfpTitle', selectedFile.name.replace(/\\.[^/.]+$/, ""));
        } else {
          formData.append('rfpTitle', 'Qatar Islamic Bank Core Modernization & Islamic Lending RFP');
        }
        formData.append('clientName', 'Qatar Islamic Bank');

        const res = await fetch(`${API_BASE}/upload`, { method: 'POST', body: formData });
        if (res.ok) {
          const data = await res.json();
          currentDocumentId = data.documentId;
          localStorage.setItem('azentio_active_doc_id', currentDocumentId);
          showToast('Tender Ingested', `Successfully parsed ${data.extractedCount || 6} requirements.`);
          await runFullLevel2Analysis();
          switchTab('workbench');
        } else {
          await loadDemoBenchmark();
        }
      } catch (err) {
        showToast('Error', err.message, false);
      } finally {
        btn.innerHTML = original;
        btn.disabled = false;
      }
    }

    async function loadDemoBenchmark() {
      try {
        const res = await fetch(`${API_BASE}/load-sample-rfp`, { method: 'POST' });
        if (res.ok) {
          const data = await res.json();
          currentDocumentId = data.documentId;
          localStorage.setItem('azentio_active_doc_id', currentDocumentId);
          showToast('Benchmark Loaded', 'Ingested Qatar Islamic Bank RFP with Level 2 attributes.');
          await runFullLevel2Analysis();
          switchTab('workbench');
        }
      } catch (err) {
        showToast('Error', err.message, false);
      }
    }

    async function runFullLevel2Analysis() {
      const btn = document.getElementById('btnTriggerL2');
      const original = btn.innerHTML;
      btn.innerHTML = `<i class="fa-solid fa-spinner fa-spin mr-1.5"></i> Running Level 2...`;
      btn.disabled = true;

      try {
        const url = currentDocumentId ? `${API_V2_BASE}/analyze-level2?documentId=${currentDocumentId}` : `${API_V2_BASE}/analyze-level2`;
        const res = await fetch(url, { method: 'POST' });
        if (res.ok) {
          const data = await res.json();
          showToast('Level 2 Analysis Done', `Readiness: ${data.readinessScore}% | Status: ${data.overallStatus}`);
          await fetchQueries(currentDocumentId);
          await fetchGapAnalysis(currentDocumentId);
          await fetchValidationReport(currentDocumentId);
          await fetchSubmissionChecklist(currentDocumentId);
        }
      } catch (err) {
        showToast('Analysis Error', err.message, false);
      } finally {
        btn.innerHTML = original;
        btn.disabled = false;
      }
    }

    async function resetAllRfpData() {
      if (!confirm('Are you sure you want to reset all tender data?')) return;
      try {
        const res = await fetch(`${API_BASE}/reset`, { method: 'POST' });
        if (res.ok) {
          localStorage.removeItem('azentio_active_doc_id');
          currentDocumentId = null;
          allQueries = [];
          showToast('Workspace Reset', 'All RFP tender records cleared.');
          switchTab('upload');
        }
      } catch (err) {
        showToast('Error', err.message, false);
      }
    }

    async function fetchQueries(docId = currentDocumentId) {
      try {
        const url = docId ? `${API_BASE}/queries?documentId=${docId}` : `${API_BASE}/queries`;
        const res = await fetch(url);
        if (res.ok) {
          allQueries = await res.json();
          updateDepartmentKpis();
          renderRequirementsWorkbench();
        }
      } catch (err) {
        console.error('Failed to fetch queries:', err);
      }
    }

    function updateDepartmentKpis() {
      const countSE = allQueries.filter(q => (q.assignedDepartment || 'SALES_ENGINEERING') === 'SALES_ENGINEERING').length;
      const countSA = allQueries.filter(q => q.assignedDepartment === 'SOLUTION_ARCHITECTURE').length;
      const countPM = allQueries.filter(q => q.assignedDepartment === 'PRODUCT_MANAGEMENT').length;
      const countEng = allQueries.filter(q => q.assignedDepartment === 'CORE_ENGINEERING').length;
      const countShariah = allQueries.filter(q => q.assignedDepartment === 'SHARIAH_BOARD').length;
      const countCommercial = allQueries.filter(q => q.assignedDepartment === 'COMMERCIAL_LEGAL').length;

      if (document.getElementById('teamKpiSE')) document.getElementById('teamKpiSE').textContent = `${countSE} reqs`;
      if (document.getElementById('teamKpiSA')) document.getElementById('teamKpiSA').textContent = `${countSA} reqs`;
      if (document.getElementById('teamKpiPM')) document.getElementById('teamKpiPM').textContent = `${countPM} reqs`;
      if (document.getElementById('teamKpiEng')) document.getElementById('teamKpiEng').textContent = `${countEng} reqs`;
      if (document.getElementById('teamKpiShariah')) document.getElementById('teamKpiShariah').textContent = `${countShariah} reqs`;
      if (document.getElementById('teamKpiCommercial')) document.getElementById('teamKpiCommercial').textContent = `${countCommercial} reqs`;
    }

    function filterByTeam(dept) {
      currentTeamFilter = dept;
      renderRequirementsWorkbench();
    }

    function selectTeamFilter(dept) {
      currentTeamFilter = (currentTeamFilter === dept) ? 'ALL' : dept;
      const sel = document.getElementById('teamFilterSelect');
      if (sel) sel.value = currentTeamFilter;
      renderRequirementsWorkbench();
    }

    function filterWorkbench(type) {
      currentFilter = type;
      ['all', 'fully', 'partial', 'info'].forEach(id => {
        const btn = document.getElementById(`filter-btn-${id}`);
        if (btn) btn.className = 'px-3 py-1 rounded-md text-slate-600 hover:text-slate-900';
      });
      if (type === 'ALL') document.getElementById('filter-btn-all').className = 'px-3 py-1 font-bold rounded-md bg-white text-slate-800 shadow-sm';
      if (type === 'Fully Addressed') document.getElementById('filter-btn-fully').className = 'px-3 py-1 font-bold rounded-md bg-white text-slate-800 shadow-sm';
      if (type === 'Partially Addressed') document.getElementById('filter-btn-partial').className = 'px-3 py-1 font-bold rounded-md bg-white text-slate-800 shadow-sm';
      if (type === 'Information Required') document.getElementById('filter-btn-info').className = 'px-3 py-1 font-bold rounded-md bg-white text-slate-800 shadow-sm';

      renderRequirementsWorkbench();
    }

    function renderRequirementsWorkbench() {
      const container = document.getElementById('requirementsList');
      if (!container) return;

      let filtered = allQueries;
      if (currentFilter !== 'ALL') {
        filtered = filtered.filter(q => q.responseClassification === currentFilter);
      }
      if (currentTeamFilter !== 'ALL') {
        filtered = filtered.filter(q => (q.assignedDepartment || 'SALES_ENGINEERING') === currentTeamFilter);
      }

      if (filtered.length === 0) {
        container.innerHTML = `
          <div class="bg-white rounded-2xl border border-slate-200 p-12 text-center text-slate-400">
            <i class="fa-solid fa-inbox text-3xl mb-2 text-slate-300"></i>
            <p class="text-sm font-medium">No requirements match the selected filters.</p>
          </div>
        `;
        return;
      }

      container.innerHTML = filtered.map(item => {
        const isApproved = item.status === 'APPROVED';
        const code = item.responseCode || 'S';
        const codeColor = getCodeBadgeStyle(code);
        const valStatus = item.validationStatus || 'PASSED';
        const valBadge = getValidationBadge(valStatus);

        return `
          <div class="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm space-y-4">
            
            <!-- Top Attribute Badges -->
            <div class="flex flex-wrap items-center justify-between gap-2">
              <div class="flex flex-wrap items-center gap-2">
                <span class="px-2.5 py-1 rounded-md text-xs font-bold bg-slate-900 text-white font-mono">
                  ${item.requirementId || 'REQ-' + item.queryId}
                </span>

                <!-- Level 2 Customer Response Code Badge -->
                <span class="px-2.5 py-0.5 rounded-full text-xs font-extrabold border ${codeColor}">
                  Code ${code}: ${item.responseCodeDesc || getCodeLabel(code)}
                </span>

                <!-- Department Assignment Badge -->
                <span class="px-2.5 py-0.5 rounded text-xs font-semibold bg-slate-100 text-slate-700 border border-slate-200">
                  <i class="fa-solid fa-user-tag mr-1 text-slate-500"></i>${item.assignedDepartment || 'SALES_ENGINEERING'}
                </span>

                <span class="text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  ${item.sectionName || 'General'}
                </span>

                <span class="px-2 py-0.5 rounded text-xs font-medium ${item.requirementType === 'MANDATORY' ? 'bg-red-50 text-red-700 border border-red-200' : 'bg-slate-100 text-slate-600'}">
                  ${item.requirementType || 'MANDATORY'}
                </span>
              </div>

              <div class="flex items-center space-x-2">
                <!-- Forensic Validation Badge -->
                ${valBadge}
                <span class="px-2.5 py-1 rounded-full text-xs font-bold bg-blue-50 text-blue-700 border border-blue-200">
                  ${item.confidenceScore || 0}% Match
                </span>
              </div>
            </div>

            <!-- Requirement Question Text -->
            <div class="text-sm font-bold text-slate-900 leading-snug">
              ${escapeHtml(item.question)}
            </div>

            <!-- Level 2 Metadata Banner: Multi-Product Span & Dependencies -->
            <div class="p-3 rounded-xl bg-slate-50 border border-slate-200 grid grid-cols-1 sm:grid-cols-2 gap-2 text-xs">
              <div>
                <span class="font-bold text-slate-600 block"><i class="fa-solid fa-cubes text-blue-600 mr-1"></i> Multi-Product Scope:</span>
                <span class="text-slate-800">${item.multiProductComponents || 'iMAL Core Islamic Engine'}</span>
              </div>
              <div>
                <span class="font-bold text-slate-600 block"><i class="fa-solid fa-link text-indigo-600 mr-1"></i> Inter-Dependencies:</span>
                <span class="text-slate-800">${item.dependencies || 'None (Independent)'}</span>
              </div>
            </div>

            <!-- Code D (Estimate) or Code T (Partner) Details -->
            ${item.requiresEstimate || code === 'D' ? `
              <div class="p-2.5 rounded-lg bg-amber-50 border border-amber-200 text-xs flex items-center justify-between text-amber-900">
                <span><i class="fa-solid fa-stopwatch mr-1.5 text-amber-600"></i><strong>Engineering Sizing Required:</strong> ${item.estimateDetails || '20-25 Man-Days estimated'}</span>
                <button onclick="promptUpdateEstimate(${item.queryId})" class="text-[11px] underline font-bold hover:text-amber-950">Update Estimate</button>
              </div>
            ` : ''}

            ${item.requiresAttachment || code === 'T' ? `
              <div class="p-2.5 rounded-lg bg-purple-50 border border-purple-200 text-xs flex items-center justify-between text-purple-900">
                <span><i class="fa-solid fa-handshake mr-1.5 text-purple-600"></i><strong>Third-Party Partner:</strong> ${item.partnerDependency || 'Bursa Malaysia / DMCC Gateway'}</span>
                <button onclick="promptUpdatePartner(${item.queryId})" class="text-[11px] underline font-bold hover:text-purple-950">Specify Partner</button>
              </div>
            ` : ''}

            <!-- Editable Draft Answer -->
            <div class="space-y-1.5">
              <div class="flex items-center justify-between text-xs">
                <label class="font-semibold text-slate-500 uppercase tracking-wider">
                  Sales Engineering Proposal Answer:
                </label>
                <span class="text-xs ${isApproved ? 'text-emerald-700 font-bold' : 'text-slate-400'}">
                  ${isApproved ? '✅ Approved & Locked' : 'Draft in Review'}
                </span>
              </div>
              <textarea 
                id="answer-${item.responseId || item.queryId}"
                rows="3" 
                class="w-full text-xs text-slate-800 bg-slate-50/50 hover:bg-white focus:bg-white border border-slate-300 rounded-xl p-3 focus:outline-none focus:ring-2 focus:ring-blue-600 transition"
              >${item.reviewedAnswer || item.draftedAnswer || ''}</textarea>
            </div>

            <!-- Card Actions & Code Switcher -->
            <div class="flex flex-wrap items-center justify-between gap-3 pt-3 border-t border-slate-100">
              <div class="flex items-center space-x-2 text-xs">
                <span class="font-bold text-slate-600">Set Code:</span>
                <button onclick="updateQueryCodeDirect(${item.queryId}, 'S')" class="px-2 py-1 rounded font-bold ${code==='S'?'bg-emerald-600 text-white':'bg-slate-100 hover:bg-slate-200'}">S</button>
                <button onclick="updateQueryCodeDirect(${item.queryId}, 'C')" class="px-2 py-1 rounded font-bold ${code==='C'?'bg-blue-600 text-white':'bg-slate-100 hover:bg-slate-200'}">C</button>
                <button onclick="updateQueryCodeDirect(${item.queryId}, 'D')" class="px-2 py-1 rounded font-bold ${code==='D'?'bg-amber-600 text-white':'bg-slate-100 hover:bg-slate-200'}">D</button>
                <button onclick="updateQueryCodeDirect(${item.queryId}, 'T')" class="px-2 py-1 rounded font-bold ${code==='T'?'bg-purple-600 text-white':'bg-slate-100 hover:bg-slate-200'}">T</button>
                <button onclick="updateQueryCodeDirect(${item.queryId}, 'N')" class="px-2 py-1 rounded font-bold ${code==='N'?'bg-rose-600 text-white':'bg-slate-100 hover:bg-slate-200'}">N</button>
              </div>

              <div class="flex items-center space-x-2">
                <button 
                  onclick="reassignDeptPrompt(${item.queryId})"
                  class="px-3 py-1.5 rounded-lg border border-slate-200 text-xs font-semibold hover:bg-slate-50 text-slate-700 transition"
                >
                  <i class="fa-solid fa-arrow-right-arrow-left mr-1"></i> Re-Route Desk
                </button>

                <button 
                  onclick="approveResponse(${item.responseId || item.queryId})"
                  class="px-4 py-1.5 rounded-lg text-xs font-bold ${isApproved ? 'bg-emerald-100 text-emerald-800' : 'bg-emerald-600 hover:bg-emerald-700 text-white'} shadow-xs transition"
                >
                  <i class="fa-solid fa-check mr-1"></i> ${isApproved ? 'Approved' : 'Approve & Lock'}
                </button>
              </div>
            </div>

          </div>
        `;
      }).join('');
    }

    function getCodeBadgeStyle(code) {
      if (code === 'S') return 'bg-emerald-100 text-emerald-800 border-emerald-300';
      if (code === 'C') return 'bg-blue-100 text-blue-800 border-blue-300';
      if (code === 'D') return 'bg-amber-100 text-amber-800 border-amber-300';
      if (code === 'T') return 'bg-purple-100 text-purple-800 border-purple-300';
      return 'bg-rose-100 text-rose-800 border-rose-300';
    }

    function getCodeLabel(code) {
      if (code === 'S') return 'Standard Capability';
      if (code === 'C') return 'Configuration Required';
      if (code === 'D') return 'Development Required';
      if (code === 'T') return 'Third-Party Dependency';
      return 'Not Available';
    }

    function getValidationBadge(status) {
      if (status === 'PASSED') {
        return `<span class="px-2 py-0.5 rounded-full text-[11px] font-bold bg-emerald-50 text-emerald-700 border border-emerald-200"><i class="fa-solid fa-circle-check mr-1 text-emerald-500"></i>Verified</span>`;
      }
      if (status === 'WARNING') {
        return `<span class="px-2 py-0.5 rounded-full text-[11px] font-bold bg-amber-50 text-amber-700 border border-amber-200"><i class="fa-solid fa-circle-exclamation mr-1 text-amber-500"></i>Review Req.</span>`;
      }
      return `<span class="px-2 py-0.5 rounded-full text-[11px] font-bold bg-rose-50 text-rose-700 border border-rose-200"><i class="fa-solid fa-triangle-exclamation mr-1 text-rose-500"></i>Mismatch Flag</span>`;
    }

    async function updateQueryCodeDirect(queryId, code) {
      let desc = getCodeLabel(code);
      let est = (code === 'D') ? '20 Man-Days' : null;
      let partner = (code === 'T') ? 'External FIX Broker / Payment Switch' : null;

      try {
        const url = `${API_V2_BASE}/requirements/${queryId}/update-code?code=${code}&classification=${encodeURIComponent(desc)}${est?'&estimate='+encodeURIComponent(est):''}${partner?'&partner='+encodeURIComponent(partner):''}`;
        const res = await fetch(url, { method: 'POST' });
        if (res.ok) {
          showToast('Code Updated', `Requirement set to Code ${code} (${desc})`);
          await fetchQueries(currentDocumentId);
        }
      } catch (err) {
        showToast('Error', err.message, false);
      }
    }

    function promptUpdateEstimate(queryId) {
      const val = prompt('Enter engineering sizing estimate (e.g. 15 Man-Days):', '15 Man-Days');
      if (val) {
        updateQueryCodeDirect(queryId, 'D');
      }
    }

    function promptUpdatePartner(queryId) {
      const val = prompt('Enter third-party partner or regulatory API name:', 'Bursa Malaysia Suq Al-Sila');
      if (val) {
        updateQueryCodeDirect(queryId, 'T');
      }
    }

    function reassignDeptPrompt(queryId) {
      const dept = prompt('Assign Department (PRODUCT_MANAGEMENT, SOLUTION_ARCHITECTURE, CORE_ENGINEERING, SHARIAH_BOARD, SALES_ENGINEERING):', 'SOLUTION_ARCHITECTURE');
      if (dept) {
        fetch(`${API_V2_BASE}/requirements/${queryId}/assign?department=${encodeURIComponent(dept)}`, { method: 'POST' })
          .then(res => res.json())
          .then(() => {
            showToast('Assigned', `Routed to ${dept}`);
            fetchQueries(currentDocumentId);
          });
      }
    }

    async function approveResponse(responseId) {
      const answerEl = document.getElementById(`answer-${responseId}`);
      const text = answerEl ? answerEl.value : '';

      try {
        const res = await fetch(`${API_BASE}/responses/${responseId}/approve`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            reviewedAnswer: text,
            comments: 'Approved by Sales Engineer',
            saveToKnowledgeBase: true
          })
        });
        if (res.ok) {
          showToast('Response Approved', 'Saved and locked. Added to validated continuous learning vault.');
          await fetchQueries(currentDocumentId);
        }
      } catch (err) {
        showToast('Error', err.message, false);
      }
    }

    // ============================================================
    // LEVEL 2: GAP ANALYSIS FETCH & RENDER
    // ============================================================
    async function fetchGapAnalysis(docId = currentDocumentId) {
      try {
        const url = docId ? `${API_V2_BASE}/gap-analysis?documentId=${docId}` : `${API_V2_BASE}/gap-analysis`;
        const res = await fetch(url);
        if (res.ok) {
          const report = await res.json();
          document.getElementById('gapKpiConfirmedProduct').textContent = report.confirmedProductGaps || 0;
          document.getElementById('gapKpiKnowledge').textContent = report.knowledgeGaps || 0;
          document.getElementById('gapKpiIntegration').textContent = report.integrationDependencies || 0;
          document.getElementById('gapKpiDev').textContent = report.functionalityGaps || 0;
          document.getElementById('gapRegisterCount').textContent = `${(report.gapItems || []).length} items requiring escalation`;

          const tbody = document.getElementById('gapItemsTableBody');
          if (!report.gapItems || report.gapItems.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="p-6 text-center text-slate-400">No capability gaps detected for this RFP.</td></tr>`;
          } else {
            tbody.innerHTML = report.gapItems.map(g => `
              <tr class="hover:bg-slate-50">
                <td class="p-3 font-mono font-bold text-slate-900">${g.requirementId}<br><span class="text-[10px] text-slate-400 font-normal">${g.sectionName || ''}</span></td>
                <td class="p-3 max-w-xs">${escapeHtml(g.requirementText)}</td>
                <td class="p-3">
                  <span class="px-2 py-0.5 rounded text-[10px] font-bold ${g.gapNature === 'PRODUCT_GAP' ? 'bg-rose-100 text-rose-800' : 'bg-amber-100 text-amber-800'}">
                    ${g.gapNature === 'PRODUCT_GAP' ? 'Confirmed Product Gap' : 'Knowledge Gap'}
                  </span>
                  <span class="block text-[10px] text-slate-500 mt-0.5">${g.gapType}</span>
                </td>
                <td class="p-3 max-w-sm text-slate-600">${escapeHtml(g.gapExplanation)}</td>
                <td class="p-3 font-semibold text-blue-900">${g.assignedDepartment}</td>
                <td class="p-3 text-slate-800 font-medium">${escapeHtml(g.recommendedAction)}</td>
              </tr>
            `).join('');
          }
        }
      } catch (err) {
        console.error('Failed to fetch gap analysis:', err);
      }
    }

    // ============================================================
    // LEVEL 2: VALIDATION REPORT FETCH & RENDER
    // ============================================================
    async function fetchValidationReport(docId = currentDocumentId) {
      try {
        const url = docId ? `${API_V2_BASE}/validation-report?documentId=${docId}` : `${API_V2_BASE}/validation-report`;
        const res = await fetch(url);
        if (res.ok) {
          const report = await res.json();
          document.getElementById('valCountMismatches').textContent = report.codeNarrativeMismatches || 0;
          document.getElementById('valCountClaims').textContent = report.unsupportedClaims || 0;
          document.getElementById('valCountCross').textContent = report.crossClauseContradictions || 0;
          document.getElementById('valCountPassed').textContent = report.passedCount || 0;

          const gateBadge = document.getElementById('validationGateBadge');
          if (report.overallStatus === 'READY_TO_SUBMIT') {
            gateBadge.className = 'px-3 py-1 rounded-full text-xs font-bold bg-emerald-100 text-emerald-800 border border-emerald-300';
            gateBadge.textContent = 'Gate: Passed (Ready)';
          } else {
            gateBadge.className = 'px-3 py-1 rounded-full text-xs font-bold bg-rose-100 text-rose-800 border border-rose-300';
            gateBadge.textContent = `Gate: ${report.blockingCount} Blocking Issues`;
          }

          const container = document.getElementById('validationIssuesContainer');
          if (!report.issues || report.issues.length === 0) {
            container.innerHTML = `
              <div class="p-6 text-center text-emerald-600 bg-emerald-50 rounded-xl border border-emerald-200">
                <i class="fa-solid fa-circle-check text-2xl mb-1"></i>
                <p class="font-bold text-xs">All responses passed forensic grounding & code alignment audit.</p>
              </div>
            `;
          } else {
            container.innerHTML = report.issues.map(iss => `
              <div class="p-4 rounded-xl border ${iss.severity === 'BLOCKING' ? 'border-rose-200 bg-rose-50/50' : 'border-amber-200 bg-amber-50/50'} space-y-2">
                <div class="flex items-center justify-between text-xs">
                  <div class="flex items-center space-x-2">
                    <span class="px-2 py-0.5 rounded text-[10px] font-bold ${iss.severity === 'BLOCKING' ? 'bg-rose-700 text-white' : 'bg-amber-600 text-white'}">
                      ${iss.severity}
                    </span>
                    <strong class="font-mono text-slate-900">${iss.requirementId}</strong>
                    <span class="text-slate-500">(${iss.issueType})</span>
                  </div>
                  <span class="text-[11px] text-slate-500">Assigned Code: <strong>${iss.codeAssigned || 'N/A'}</strong></span>
                </div>
                <p class="text-xs text-slate-800 font-medium">${escapeHtml(iss.description)}</p>
                ${iss.flaggedSnippet ? `<div class="p-2 rounded bg-white/80 border border-rose-100 text-[11px] text-rose-800 font-mono">Flagged: ${escapeHtml(iss.flaggedSnippet)}</div>` : ''}
                <div class="text-[11px] text-slate-600">
                  <strong class="text-blue-900">Remediation:</strong> ${escapeHtml(iss.remediationRecommendation)}
                </div>
              </div>
            `).join('');
          }
        }
      } catch (err) {
        console.error('Failed to fetch validation report:', err);
      }
    }

    // ============================================================
    // LEVEL 2: CLARIFICATION PACK FETCH & RENDER
    // ============================================================
    async function fetchClarificationPack(docId = currentDocumentId) {
      try {
        const url = docId ? `${API_V2_BASE}/clarification-pack?documentId=${docId}` : `${API_V2_BASE}/clarification-pack`;
        const res = await fetch(url);
        if (res.ok) {
          const pack = await res.json();
          document.getElementById('clarifSolCount').textContent = `${pack.solutionDesignImpactCount || 0} queries`;
          document.getElementById('clarifDelCount').textContent = `${pack.deliveryTimelineImpactCount || 0} queries`;
          document.getElementById('clarifComCount').textContent = `${pack.commercialScopeImpactCount || 0} queries`;

          const listEl = document.getElementById('clarificationClustersList');
          if (!pack.clusters || pack.clusters.length === 0) {
            listEl.innerHTML = `<p class="text-xs text-slate-400 italic">No clarification requests logged for this tender.</p>`;
          } else {
            listEl.innerHTML = pack.clusters.map(c => `
              <div class="p-5 rounded-2xl border border-slate-200 bg-slate-50/50 hover:bg-white transition space-y-3 shadow-xs">
                <div class="flex items-center justify-between text-xs">
                  <span class="font-bold text-slate-900 text-sm">${escapeHtml(c.clusterName)}</span>
                  <div class="flex items-center space-x-2">
                    <span class="px-2 py-0.5 rounded text-[10px] font-bold ${c.impactPriority === 'HIGH' ? 'bg-rose-100 text-rose-800' : 'bg-blue-100 text-blue-800'}">
                      ${c.impactPriority} Priority
                    </span>
                    <span class="px-2 py-0.5 rounded text-[10px] font-semibold bg-indigo-50 text-indigo-700 border border-indigo-200">
                      ${c.impactArea}
                    </span>
                  </div>
                </div>

                <div class="p-3 bg-white rounded-xl border border-slate-200 text-xs text-slate-800">
                  <strong class="text-blue-900 block mb-1">Formal Question for Customer Procurement:</strong>
                  ${escapeHtml(c.consolidatedClarificationQuestion)}
                </div>

                <div class="grid grid-cols-1 sm:grid-cols-2 gap-2 text-xs">
                  <div class="p-2.5 rounded-lg bg-emerald-50/60 border border-emerald-100 text-emerald-950">
                    <strong class="block text-emerald-800 text-[11px]">Working Assumption Applied:</strong>
                    ${escapeHtml(c.workingAssumption)}
                  </div>
                  <div class="p-2.5 rounded-lg bg-amber-50/60 border border-amber-100 text-amber-950">
                    <strong class="block text-amber-800 text-[11px]">Technical Justification:</strong>
                    ${escapeHtml(c.technicalJustification)}
                  </div>
                </div>

                <div class="text-[11px] text-slate-500">
                  Affects Requirements: ${(c.affectedRequirementIds || []).map(id => `<code class="px-1.5 py-0.5 bg-slate-100 font-bold rounded mr-1">${id}</code>`).join('')}
                </div>
              </div>
            `).join('');
          }
        }
      } catch (err) {
        console.error('Failed to fetch clarification pack:', err);
      }
    }

    function downloadClarificationDoc() {
      window.open(`${API_V2_BASE}/clarification-pack`, '_blank');
      showToast('Exported', 'Opened official clarification pack document.');
    }

    // ============================================================
    // LEVEL 2: COMPLIANCE MATRIX & ASSEMBLY FETCH & RENDER
    // ============================================================
    async function fetchSubmissionChecklist(docId = currentDocumentId) {
      try {
        const url = docId ? `${API_V2_BASE}/submission-checklist?documentId=${docId}` : `${API_V2_BASE}/submission-checklist`;
        const res = await fetch(url);
        if (res.ok) {
          const chk = await res.json();
          document.getElementById('cockpitOverallStatus').textContent = chk.overallStatus || 'READY';
          document.getElementById('cockpitScoreBadge').textContent = `${chk.readinessScore || 0}% Readiness Score`;
          document.getElementById('cockpitDeadline').textContent = chk.submissionDeadline || '2026-11-30';

          const grid = document.getElementById('submissionChecklistItems');
          if (chk.checklistItems) {
            grid.innerHTML = chk.checklistItems.map(item => `
              <div class="p-3 rounded-xl bg-white/10 border border-white/10 space-y-1">
                <div class="flex items-center justify-between">
                  <span class="font-bold text-white">${escapeHtml(item.title)}</span>
                  <span class="px-2 py-0.5 rounded text-[10px] font-extrabold ${item.status === 'PASS' ? 'bg-emerald-400 text-emerald-950' : (item.isBlocking ? 'bg-rose-400 text-rose-950' : 'bg-amber-400 text-amber-950')}">
                    ${item.status}
                  </span>
                </div>
                <p class="text-[11px] text-slate-300 leading-snug">${escapeHtml(item.detail)}</p>
              </div>
            `).join('');
          }
        }
      } catch (err) {
        console.error('Failed to fetch submission checklist:', err);
      }
    }

    async function fetchLevel2ComplianceMatrix(docId = currentDocumentId) {
      try {
        const url = docId ? `${API_V2_BASE}/compliance-matrix?documentId=${docId}` : `${API_V2_BASE}/compliance-matrix`;
        const res = await fetch(url);
        if (res.ok) {
          const matrix = await res.json();
          const tbody = document.getElementById('complianceMatrixTableBody');
          if (!matrix || matrix.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" class="p-6 text-center text-slate-400">No compliance items available.</td></tr>`;
          } else {
            tbody.innerHTML = matrix.map(row => `
              <tr class="hover:bg-slate-50">
                <td class="p-3 font-mono font-bold text-slate-900">${row.clauseNumber}<br><span class="text-[10px] text-slate-400">${row.requirementId}</span></td>
                <td class="p-3 text-slate-600">${row.sectionName}</td>
                <td class="p-3 max-w-xs">${escapeHtml(row.requirementText)}</td>
                <td class="p-3">
                  <span class="px-2.5 py-1 rounded-full text-xs font-extrabold border ${getCodeBadgeStyle(row.responseCode)}">
                    ${row.responseCode}
                  </span>
                </td>
                <td class="p-3 font-semibold text-slate-700">${row.classification}</td>
                <td class="p-3 max-w-sm text-slate-600">${escapeHtml(row.responseSummary)}</td>
                <td class="p-3 text-slate-800">
                  <span class="font-bold text-blue-900">${row.assignedDepartment}</span>
                  ${row.estimateDetails ? `<span class="block text-[10px] text-amber-700 font-medium">Est: ${row.estimateDetails}</span>` : ''}
                  ${row.partnerDependency ? `<span class="block text-[10px] text-purple-700 font-medium">Partner: ${row.partnerDependency}</span>` : ''}
                </td>
                <td class="p-3">${getValidationBadge(row.validationStatus)}</td>
              </tr>
            `).join('');
          }
        }
      } catch (err) {
        console.error('Failed to fetch Level 2 compliance matrix:', err);
      }
    }

    function downloadCustomerExcel() {
      const url = currentDocumentId ? `${API_V2_BASE}/export/customer-template?documentId=${currentDocumentId}` : `${API_V2_BASE}/export/customer-template`;
      window.location.href = url;
      showToast('Export Started', 'Downloaded Customer Response Template Excel CSV.');
    }

    // Selective Regeneration Modal
    function openSelectiveRegenModal() {
      document.getElementById('selectiveRegenModal').classList.remove('hidden');
    }
    function closeSelectiveRegenModal() {
      document.getElementById('selectiveRegenModal').classList.add('hidden');
    }

    async function executeSelectiveRegeneration() {
      const section = document.getElementById('regenSectionInput').value;
      const prompt = document.getElementById('regenPromptInput').value;
      const preserve = document.getElementById('preserveApprovedCheckbox').checked;

      closeSelectiveRegenModal();
      showToast('Regeneration Started', 'Selectively refreshing unapproved responses...');

      try {
        const res = await fetch(`${API_V2_BASE}/regenerate-selective`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            documentId: currentDocumentId,
            targetSection: section,
            promptInstruction: prompt,
            preserveApproved: preserve
          })
        });
        if (res.ok) {
          const data = await res.json();
          showToast('Completed', data.message || 'Regenerated unapproved items while preserving locked answers.');
          await fetchQueries(currentDocumentId);
          await fetchLevel2ComplianceMatrix(currentDocumentId);
          await fetchSubmissionChecklist(currentDocumentId);
        }
      } catch (err) {
        showToast('Error', err.message, false);
      }
    }

    function escapeHtml(text) {
      if (!text) return '';
      return text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }
  </script>
</body>
</html>
"""

# Write to static index.html and root workbench.html
static_path = "src/main/resources/static/index.html"
with open(static_path, "w", encoding="utf-8") as f:
    f.write(html_content)

with open("workbench.html", "w", encoding="utf-8") as f:
    f.write(html_content)

print(f"Generated Level 2 UI in {static_path} ({len(html_content)} bytes)")
