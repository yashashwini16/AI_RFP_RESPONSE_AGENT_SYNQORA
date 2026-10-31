# Azentio AI RFP Response Agent

An enterprise AI copilot that ingests historical proposals, security whitepapers, and technical specifications into PostgreSQL with PgVector, semantically retrieves verified answers using cosine distance, synthesizes grounded proposals via LangChain4j, calculates confidence scores, flags knowledge gaps, and supports a continuous learning loop.

---

## 🚀 Quick Start

### 1. Database Setup (PgVector on Port 5433)
The PgVector Docker container is already running on port 5433.
To stop or restart:
```bash
docker compose up -d
```

### 2. Run Backend
```bash
mvn -s ./settings-hackathon.xml spring-boot:run
```
Backend runs at: `http://localhost:8080/api/v1/rfp`  
Health check: `http://localhost:8080/api/v1/rfp/health`

---

## 📡 API Endpoints

| Method | Endpoint | Description | Milestone |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/rfp/health` | Health & milestone readiness check | All |
| `POST` | `/api/v1/rfp/query` | Ingest new RFP question, retrieve vector matches & draft answer | M1 & M2 |
| `GET` | `/api/v1/rfp/search?q={question}` | Direct semantic retrieval of top-K matches | M1 |
| `GET` | `/api/v1/rfp/queries` | List all questions with answers & status | M2 & M3 |
| `POST` | `/api/v1/rfp/responses/{id}/approve` | Approve answer & save to Knowledge Vault (Continuous Learning) | M3 |
| `POST` | `/api/v1/rfp/knowledge/ingest` | Ingest new Q&A into Knowledge Vault | M1 |
| `GET` | `/api/v1/rfp/knowledge` | List all verified knowledge base items | All |
| `GET` | `/api/v1/rfp/export/{rfpTitle}` | One-click export of proposal to Markdown & JSON | M3 |
