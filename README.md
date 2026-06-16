# OpenLinkHub RAG

OpenLinkHub RAG contains an upstream LightRAG runtime plus an OpenLinkHub
management layer for knowledge base operations, RBAC, model profiles, and query
testing.

## Layout

```text
OpenLinkHub-RAG/
  LightRAG/                    # Upstream LightRAG runtime
  services/rag-admin-api/      # Spring Boot management API
  apps/rag-admin-web/          # Vue management console
  docs/rag-admin-design.md     # Product and architecture design
```

## Runtime Ports

- LightRAG: `http://127.0.0.1:9621`
- RAG Admin API: `http://localhost:8091`
- RAG Admin Web: `http://localhost:5176`

## Start

Start LightRAG first using the existing workflow under `LightRAG/`.

Then start the management API:

```bash
cd services/rag-admin-api
mvn spring-boot:run
```

Then start the web console:

```bash
cd apps/rag-admin-web
npm install
npm run dev
```

Default login:

```text
username: admin
password: admin123
```

## Current MVP

- Login with a seeded administrator.
- In-memory RBAC and permission model.
- Default knowledge base bound to local LightRAG.
- LightRAG endpoint health check.
- Document list, upload, text insert, delete, and pipeline status proxy.
- Streaming query playground through LightRAG `/query/stream`.
- Model profile list and assignment with `pending_restart` status.

## Next Steps

- Replace in-memory data with database persistence.
- Add Spring Security JWT.
- Add user, role, and knowledge base member management screens.
- Add audit log persistence.
- Add managed LightRAG instance lifecycle and model profile restart workflow.
