# Azentio iMAL Ijara Product Architecture Guide (v15.0)
**Document Ref:** iMAL-IJARA-ARCH-2025  
**Classification:** Azentio Product Architecture Documentation  
**Standard Compliance:** AAOIFI Shariah Standard No. 9 (Ijara & Ijara Muntahia Bittamleek)

---

## 3.4 Ijara wa Iqtina (Leasing with Ownership Transfer) Architecture

### Core Mechanism:
Under the Azentio iMAL Ijara module, leasing contracts are bifurcated into legal asset acquisition and periodic usufruct tenancy.

### Key Capabilities:
1. **Asset Ownership & Balance Sheet Treatment**:
   - The leased asset (vehicle, equipment, commercial real estate) remains capitalized on the bank’s fixed assets ledger throughout the lease term.
   - Depreciation schedules adhere to AAOIFI Financial Accounting Standard (FAS) 8.
2. **Takaful Insurance & Maintenance Apportionment**:
   - Fundamental maintenance (structural repairs) and Islamic Takaful comprehensive insurance are strictly assigned to the bank as Lessor.
   - Operating/minor maintenance expenses are contractually assumed by the Lessee.
3. **Variable Profit Rate Benchmarking & Floating Schedules**:
   - Dynamic repricing linked to benchmark indexes (SOFR, EIBOR, SAIBOR) with Shariah-mandated floor and ceiling caps.
   - Automatic generation of updated rental amortization notices without compounding late payment penalties (Gharama/late fee routed exclusively to Charity).
