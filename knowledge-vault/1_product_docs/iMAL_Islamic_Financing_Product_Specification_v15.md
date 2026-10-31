# Azentio iMAL Islamic Financing Product Specification (v15.0)
**Document Ref:** iMAL-ISL-SPEC-2025-V15  
**Classification:** Azentio Product Architecture Documentation  
**Standard Compliance:** AAOIFI Shariah Standard No. 8 (Murabaha) & IFSB Guidelines

---

## 4.1 Automated Murabaha Financing Engine
### 4.1.2 Commodity Broker Integration & Shariah Asset Sequence Validation

The Azentio iMAL v15.0 Murabaha Financing module is engineered to support straight-through processing (STP) for Islamic personal and corporate financing. 

### Key Capabilities:
1. **Automated Commodity Broker Connectors**:
   - Real-time API integration with **Bursa Suq Al-Sila' (BSAS)** and **DMCC Tradeflow**.
   - Automated purchase of verified physical commodities (e.g., CPO, metals) on behalf of the financing bank.
2. **Shariah Sequence Enforcement (Tarteeb)**:
   - Enforces sequential execution to prevent 'Inah and fictitious sales:
     - Step 1: Customer submits unilateral non-binding Promise to Purchase (*Waad*).
     - Step 2: Bank executes commodity purchase from Broker A and acquires beneficial ownership and title.
     - Step 3: Bank issues Offer (*Ijab*) to customer with explicit cost-plus-profit disclosure (*Murabaha* margin).
     - Step 4: Customer executes Acceptance (*Qabul*).
     - Step 5: Bank or appointed agent sells commodity to Broker B on customer instruction for cash disbursement.
3. **Audit Evidence**:
   - Electronic certificates of commodity ownership, holding timestamps, and broker trade references are permanently cryptographically anchored in the transaction record.
