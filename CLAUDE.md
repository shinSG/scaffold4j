# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Compile
mvn clean compile

# Run all tests
mvn test

# Run a single test class
mvn test -pl scaffold4j-cli -Dtest=ProjectConfigTest

# Package (skip tests)
mvn clean package -DskipTests

# Run the CLI
java -jar scaffold4j-cli/target/scaffold4j-cli-1.0.0-SNAPSHOT.jar generate --name=my-app --package=com.example.ai
```

## Architecture

scaffold4j is a **code generation CLI** — it does not use Spring itself. It produces Spring Boot projects.

**Entry point:** `Scaffold4jMain` (picocli `@Command`) with two subcommands:
- `generate` → `GenerateCommand` → `ProjectGenerator`
- `list-providers` → `ListProvidersCommand`

**Generation pipeline:**
1. `GenerateCommand` parses CLI args into a `ProjectConfig` (or `InteractiveWizard` fills them interactively)
2. `ProjectConfig.validate()` checks required fields and consistency
3. `ProjectGenerator.generate()` orchestrates file output by calling specialized generators:
   - `PomGenerator` — root POM (aggregator) + per-module POMs with conditional dependencies based on framework/protocol/feature selections
   - `ModuleGenerator` — all Java source files as string templates (embedded as text blocks, not Mustache)
   - `ConfigGenerator` — `application.yml` and env-specific configs
   - `DockerGenerator` — Dockerfile + docker-compose.yml (conditionally includes services like ollama, postgres/pgvector, nacos)
   - `GitignoreGenerator` — `.gitignore` + `.editorconfig`

**Model layer** (`com.scaffold4j.model`): Five enums drive conditional generation:
- `AIFramework` — spring-ai / langchain4j / both
- `LLMProvider` — 11 providers, each carrying Maven artifact coordinates and env var names
- `Protocol` — rest / mcp / a2a / acp
- `Feature` — memory / rag / sse / websocket
- `VectorStore` — 9 backends with artifact coordinates

**`ProjectConfig`** is the central model object passed to every generator. It exposes boolean checks (`hasFeature()`, `hasProtocol()`, `usesSpringAI()`, `usesLangChain4j()`) that generators use to conditionally emit files and dependencies.

**`TemplateEngine`** wraps Mustache but is currently unused — all Java code generation uses string concatenation / text blocks in `ModuleGenerator`. The Mustache dependency is available for future migration of templates to `.mustache` files.

**Generated project structure** (output, not in this repo):
```
<name>/
├── pom.xml                    # aggregator, depends on spring-boot-starter-parent
├── <name>-common/             # ErrorCode, BaseException, Result<T>, JsonUtils
├── <name>-domain/             # ChatMessage, Conversation, DTOs, enums
├── <name>-infra/              # LLM adapters, vector store, memory, RAG, WebSocket config
├── <name>-app/                # Services, agents, tools, prompt templates
├── <name>-api/                # REST/MCP/A2A/ACP/WebSocket controllers
├── <name>-bootstrap/          # @SpringBootApplication + config files
└── docker/
```

Module dependency chain: `common ← domain ← infra ← app ← api ← bootstrap` (each depends on the previous).

**Key dependencies of the CLI itself:** picocli (CLI parsing), Mustache (templating, not currently used), Jackson (JSON/YAML), SLF4J/Logback (logging), JUnit 5 (testing). The CLI produces a fat JAR via maven-shade-plugin.
