# scaffold4j 使用说明

## 目录

1. [简介](#简介)
2. [环境准备](#环境准备)
3. [安装](#安装)
4. [命令参考](#命令参考)
5. [参数详解](#参数详解)
6. [使用示例](#使用示例)
7. [生成项目结构](#生成项目结构)
8. [开发指南](#开发指南)
9. [常见问题](#常见问题)

---

## 简介

**scaffold4j** 是一个 Java AI 应用脚手架工具，通过一行命令快速生成生产级的多模块 Maven 项目骨架。

### 生成项目

```bash
scaffold4j generate --name=my-ai-app --package=com.example.ai --protocols=rest,mcp --llm-providers=openai,ollama
```

这条命令会生成一个**完整可编译启动**的 Java AI 项目，包含 6 个子模块、RESTful API、MCP Server、LLM 多供应商适配，以及 Docker 部署配置。

---

## 环境准备

| 依赖 | 版本要求 | 说明 |
|-----|---------|------|
| Java | 17+ | 编译和运行 CLI 工具 |
| Maven | 3.9+ | 构建和启动生成的项目 |
| Docker | 可选 | 运行 Ollama、PGVector、Nacos 等基础设施 |

### 检查环境

```bash
java --version    # 应显示 17 或更高
mvn --version     # 应显示 3.9 或更高
```

---

## 安装

### 方式一：源码构建

```bash
git clone https://github.com/scaffold4j/scaffold4j.git
cd scaffold4j
mvn clean package -DskipTests
```

构建完成后，CLI 工具 JAR 位于：

```
scaffold4j-cli/target/scaffold4j-cli-1.0.0-SNAPSHOT.jar
```

### 方式二：创建别名（推荐）

在 `~/.zshrc` 或 `~/.bashrc` 中添加：

```bash
alias scaffold4j="java -jar /path/to/scaffold4j/scaffold4j-cli/target/scaffold4j-cli-1.0.0-SNAPSHOT.jar"
```

然后：

```bash
source ~/.zshrc
scaffold4j --help
```

### 验证安装

```bash
scaffold4j --version    # 显示版本号
scaffold4j --help       # 显示帮助信息
scaffold4j list-providers  # 列出所有支持的供应商
```

---

## 命令参考

scaffold4j 提供两个子命令：

### generate — 生成项目

```bash
scaffold4j generate [OPTIONS]
```

### list-providers — 查看可用组件

```bash
scaffold4j list-providers
```

输出示例：

```
=== LLM Providers ===
ID                 Name                           Spring AI Starter
------------------------------------------------------------------------------------------
openai             OpenAI                         spring-ai-starter-model-openai
ollama             Ollama (Local)                 spring-ai-starter-model-ollama
anthropic          Anthropic Claude               spring-ai-starter-model-anthropic
deepseek           DeepSeek                       spring-ai-starter-model-deepseek
...

=== Vector Stores ===
ID                 Name                      Spring AI Starter
------------------------------------------------------------------------------------------
pgvector           PGVector                  spring-ai-starter-vector-store-pgvector
milvus             Milvus                    spring-ai-starter-vector-store-milvus
...

=== Protocols ===
ID         Name                           Description
--------------------------------------------------------------------------------
rest       RESTful API                    Standard HTTP REST endpoints
mcp        Model Context Protocol         MCP server for AI tool/resource exposure
a2a        Agent-to-Agent                 Google A2A protocol for multi-agent task delegation
acp        Agent Communication Protocol   IBM/BeeAI ACP (merging into A2A)
```

---

## 参数详解

### 必填参数

| 参数 | 短形式 | 示例 | 说明 |
|-----|--------|------|------|
| `--name` | `-n` | `--name=my-ai-app` | 项目名称，用作目录名和默认 artifactId |
| `--package` | `-p` | `--package=com.example.ai` | Java 基础包名，必须是合法的包名格式 |

### 可选参数 — Maven 坐标

| 参数 | 默认值 | 说明 |
|-----|--------|------|
| `--group-id` | 等于 `--package` | Maven groupId |
| `--artifact-id` | 等于 `--name` | Maven artifactId |
| `--version` | `1.0.0-SNAPSHOT` | 项目版本号 |

### 可选参数 — 构建配置

| 参数 | 默认值 | 说明 |
|-----|--------|------|
| `--java-version` | `21` | Java 版本，可选 `17` 或 `21` |
| `--spring-boot-version` | `3.5.0` | Spring Boot 版本 |

### 可选参数 — AI 框架

| 参数 | 默认值 | 可选值 | 说明 |
|-----|--------|--------|------|
| `--ai-framework` | `spring-ai` | `spring-ai`、`langchain4j`、`both` | AI 开发框架 |

三种模式说明：

| 值 | 效果 |
|---|------|
| `spring-ai` | 使用 Spring AI 全家桶：ChatClient、ChatModel、Advisor、VectorStore 等 |
| `langchain4j` | 使用 LangChain4j：ChatLanguageModel、AiServices、LangGraph4j 工作流 |
| `both` | 同时引入两者，可在项目中混用（推荐） |

### 可选参数 — LLM 供应商

| 参数 | 默认值 | 格式 |
|-----|--------|------|
| `--llm-providers` | `openai` | 逗号分隔，如 `openai,ollama,anthropic` |

支持的供应商：

| ID | 名称 | 需要环境变量 | Spring AI Starter |
|----|------|-------------|-------------------|
| `openai` | OpenAI | `OPENAI_API_KEY` | ✔ |
| `ollama` | Ollama（本地） | 无需 | ✔ |
| `anthropic` | Anthropic Claude | `ANTHROPIC_API_KEY` | ✔ |
| `deepseek` | DeepSeek | `DEEPSEEK_API_KEY` | ✔ |
| `zhipuai` | 智谱 GLM | `ZHIPUAI_API_KEY` | ✔ |
| `vertex-ai` | Google Vertex AI | `GOOGLE_APPLICATION_CREDENTIALS` | ✔ |
| `azure-openai` | Azure OpenAI | `AZURE_OPENAI_API_KEY` | ✔ |
| `bedrock` | AWS Bedrock | `AWS_ACCESS_KEY_ID` | ✔ |
| `qwen` | 通义千问 | `DASHSCOPE_API_KEY` | ✔ |
| `moonshot` | Moonshot（月之暗面） | `MOONSHOT_API_KEY` | ✘（通用适配） |
| `doubao` | 豆包（火山引擎） | `DOUBAO_API_KEY` | ✘（通用适配） |

### 可选参数 — 协议

| 参数 | 默认值 | 格式 |
|-----|--------|------|
| `--protocols` | `rest` | 逗号分隔，如 `rest,mcp,a2a` |

| 值 | 说明 | 生成的内容 |
|---|------|-----------|
| `rest` | RESTful API | `ChatController`、`AgentController`（GET/POST 端点） |
| `mcp` | Model Context Protocol | `McpServerConfig` + `@McpTool` 注解工具类 |
| `a2a` | Agent-to-Agent | Agent Card 端点 + A2A Task Handler + 路由配置 |
| `acp` | Agent Communication Protocol | ACP 兼容端点 + Session Manager（含 A2A 迁移提示） |

### 可选参数 — 特性

| 参数 | 默认值 | 格式 |
|-----|--------|------|
| `--features` | 无 | 逗号分隔，如 `memory,rag,sse,websocket` |

| 值 | 说明 | 生成的内容 |
|---|------|-----------|
| `memory` | 对话记忆 | `ChatMemoryStore`（内存实现）+ `ConversationRepository`（接口） |
| `rag` | 检索增强生成 | 完整 RAG 管道：`DocumentLoader` → `TextSplitter` → `EmbeddingService` → `IngestionPipeline` → `RetrievalPipeline` → `RetrievalAugmentor` |
| `sse` | SSE 流式输出 | `SseController`（GET/POST 流式端点）+ `StreamService` |
| `websocket` | WebSocket | `ChatWebSocketHandler` + `WebSocketConfig`（`/ws/chat` 端点） |

### 可选参数 — 向量数据库

| 参数 | 默认值 | 说明 |
|-----|--------|------|
| `--vector-store` | `pgvector` | 向量数据库后端 |

可选值：`pgvector`、`milvus`、`chroma`、`pinecone`、`elasticsearch`、`redis`、`weaviate`、`qdrant`、`simple`

### 可选参数 — 服务注册

| 参数 | 默认值 | 说明 |
|-----|--------|------|
| `--nacos` | `false` | 是否启用 Nacos 服务注册 |
| `--nacos-addr` | `localhost:8848` | Nacos 服务地址 |
| `--nacos-namespace` | 空 | Nacos 命名空间（如 `dev`、`prod`） |

### 可选参数 — 输出

| 参数 | 短形式 | 默认值 | 说明 |
|-----|--------|--------|------|
| `--output-dir` | `-o` | `./` | 输出目录 |

### 交互式模式

| 参数 | 说明 |
|-----|------|
| `--interactive` / `-i` | 进入问答式向导，逐步填写所有配置项 |

---

## 使用示例

### 示例 1：最简 OpenAI 项目

```bash
scaffold4j generate \
  --name=simple-chat \
  --package=com.demo.chat \
  --llm-providers=openai
```

**生成结果**：一个 RESTful API 项目，支持 `/api/v1/chat` 同步对话。

**启动**：

```bash
cd simple-chat
export OPENAI_API_KEY=sk-...
./mvnw -pl simple-chat-bootstrap spring-boot:run

# 测试
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Hello, who are you?"}'
```

### 示例 2：本地 Ollama + 流式输出

```bash
scaffold4j generate \
  --name=local-ai \
  --package=com.demo.local \
  --llm-providers=ollama \
  --features=sse
```

**生成结果**：本地 Ollama 项目，支持 SSE 流式输出。

**启动**：

```bash
# 先启动 Ollama
ollama pull llama3.1

cd local-ai
./mvnw -pl local-ai-bootstrap spring-boot:run

# 测试流式输出
curl -X POST http://localhost:8080/api/v1/stream/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"讲个笑话"}'
```

### 示例 3：MCP Server + 多 LLM 供应商

```bash
scaffold4j generate \
  --name=mcp-server \
  --package=com.demo.mcp \
  --protocols=rest,mcp \
  --llm-providers=openai,ollama,anthropic \
  --features=memory,sse
```

**生成结果**：
- REST API + SSE 流式端点
- MCP Server（`WeatherMcpTool`、`SearchMcpTool`），可被 Claude Desktop 等 MCP 客户端调用
- 3 个 LLM 适配器（OpenAI、Ollama、Anthropic）
- 对话记忆管理

**Claude Desktop 配置**（`claude_desktop_config.json`）：

```json
{
  "mcpServers": {
    "mcp-server": {
      "command": "java",
      "args": ["-jar", "mcp-server-bootstrap/target/mcp-server-bootstrap-1.0.0-SNAPSHOT.jar"]
    }
  }
}
```

### 示例 4：企业级 RAG 系统

```bash
scaffold4j generate \
  --name=enterprise-rag \
  --package=com.company.rag \
  --protocols=rest,mcp,a2a \
  --ai-framework=both \
  --llm-providers=openai,anthropic,deepseek \
  --vector-store=pgvector \
  --features=memory,rag,sse,websocket \
  --nacos=true \
  --nacos-addr=nacos.prod:8848 \
  --nacos-namespace=prod
```

**生成结果**：
- 双 AI 框架（Spring AI + LangChain4j）
- 完整 RAG 管道 + PGVector 向量库
- MCP Server + A2A Agent 多协议
- SSE 流式 + WebSocket 实时
- Nacos 服务注册与配置管理
- Docker Compose 包含 PostgreSQL+pgvector、Nacos

**启动基础设施**：

```bash
cd enterprise-rag/docker
docker-compose up -d postgres nacos
```

**启动应用**：

```bash
export OPENAI_API_KEY=sk-...
export ANTHROPIC_API_KEY=sk-ant-...
export DEEPSEEK_API_KEY=sk-...

cd enterprise-rag
./mvnw -pl enterprise-rag-bootstrap spring-boot:run
```

### 示例 5：A2A 多 Agent 协作

```bash
scaffold4j generate \
  --name=agent-hub \
  --package=com.company.agents \
  --protocols=rest,mcp,a2a \
  --ai-framework=both \
  --llm-providers=openai \
  --features=sse
```

**生成结果**：
- 对外暴露 MCP Tool（可被 Claude 等调用）
- 通过 A2A 协议与其他 Agent 协作（Agent Card 在 `/.well-known/agent.json`）
- LangGraph4j 工作流骨架

### 示例 6：交互式生成

```bash
scaffold4j generate --interactive
```

系统会逐步提问：

```
╔══════════════════════════════════════════════╗
║   scaffold4j — Interactive Project Wizard    ║
╚══════════════════════════════════════════════╝

Project name [my-ai-app]: my-agent
Base package [com.example.ai]: com.acme.agent
AI framework [spring-ai]: both
LLM providers [openai]: openai,ollama
Protocols [rest]: rest,mcp,a2a
Features []: memory,sse
Vector store [pgvector]: pgvector
Enable Nacos? (true/false) [false]: true
Nacos server address [localhost:8848]:
...
```

按 Enter 使用默认值，或输入自定义值。

---

## 生成项目结构

以 `--name=my-ai-app --package=com.example.ai --protocols=rest,mcp --features=memory,rag,sse` 为例，生成后的目录结构：

```
my-ai-app/
├── pom.xml                              # 根 POM
├── .gitignore
├── .editorconfig
├── README.md
├── docker/
│   ├── Dockerfile
│   └── docker-compose.yml               # 包含 Ollama + PGVector 服务
│
├── my-ai-app-common/                    # 公共模块
│   └── src/main/java/com/example/ai/common/
│       ├── constant/ErrorCode.java       # 错误码枚举
│       ├── constant/CommonConstant.java  # 公共常量
│       ├── exception/                    # BaseException / AIException / ErrorResponse
│       ├── result/Result.java            # 统一响应体 Result<T>
│       └── util/JsonUtils.java
│
├── my-ai-app-domain/                    # 领域模型模块
│   └── src/main/java/com/example/ai/domain/
│       ├── model/ChatMessage.java        # 消息实体
│       ├── model/Conversation.java       # 会话实体
│       ├── dto/ChatRequest.java          # 请求体
│       ├── dto/ChatResponse.java         # 响应体
│       ├── dto/StreamData.java           # SSE 流数据
│       └── enums/                        # MessageRole / LLMProviderType
│
├── my-ai-app-infra/                     # 基础设施模块
│   └── src/main/java/com/example/ai/infra/
│       ├── config/                       # AppConfig / LLMProviderConfig / VectorStoreConfig
│       ├── llm/                          # LLMProviderAdapter 接口 + 各供应商实现 + Factory
│       ├── vectorstore/                  # VectorStoreAdapter
│       ├── memory/                       # ChatMemoryStore + ConversationRepository
│       └── rag/                          # DocumentLoader + TextSplitter + EmbeddingService
│
├── my-ai-app-app/                       # 应用服务模块
│   └── src/main/java/com/example/ai/app/
│       ├── service/                      # ChatService / AgentService / RagService / StreamService
│       ├── agent/                        # AgentOrchestrator + ReactAgent + workflow/
│       ├── rag/                          # IngestionPipeline + RetrievalPipeline + RetrievalAugmentor
│       ├── tool/                         # WeatherTool / SearchTool
│       └── prompt/                       # PromptTemplate + templates/
│
├── my-ai-app-api/                       # API 接口层
│   └── src/main/java/com/example/ai/api/
│       ├── rest/                         # ChatController / AgentController / SseController
│       └── mcp/                          # McpServerConfig + WeatherMcpTool + SearchMcpTool
│
└── my-ai-app-bootstrap/                 # 启动模块
    └── src/main/
        ├── java/com/example/ai/Application.java
        └── resources/
            ├── application.yml           # 主配置（所有 AI 参数集中管理）
            ├── application-dev.yml
            ├── application-prod.yml
            └── logback-spring.xml
```

### 配置示例（application.yml）

所有 AI 相关配置集中在 `bootstrap` 模块的 `application.yml` 中，通过环境变量覆盖：

```yaml
scaffold4j:
  ai:
    default-provider: ${AI_DEFAULT_PROVIDER:openai}
    providers:
      openai:
        api-key: ${OPENAI_API_KEY}
        chat:
          model: ${OPENAI_CHAT_MODEL:gpt-4o}
          temperature: 0.7
      ollama:
        base-url: ${OLLAMA_BASE_URL:http://localhost:11434}
        chat:
          model: ${OLLAMA_CHAT_MODEL:llama3.1}
    rag:
      enabled: ${RAG_ENABLED:true}
      vector-store:
        type: pgvector
        pgvector:
          host: ${PGVECTOR_HOST:localhost}
          port: ${PGVECTOR_PORT:5432}
    memory:
      enabled: ${MEMORY_ENABLED:true}
      type: token-window
      token-window:
        max-tokens: 4000
```

---

## 开发指南

### 接入真实 LLM API

生成的项目中，各 LLM Provider Adapter 默认是骨架代码（TODO 标记）。你需要根据使用的框架补充实现：

**Spring AI 方式**（以 OpenAI 为例）：

```java
// infra/llm/OpenAIAdapter.java
@Component
public class OpenAIAdapter implements LLMProviderAdapter {

    private final ChatClient chatClient;

    public OpenAIAdapter(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public String chat(String systemPrompt, String userMessage) {
        return chatClient.prompt()
            .system(systemPrompt)
            .user(userMessage)
            .call()
            .content();
    }
}
```

**LangChain4j 方式**：

```java
// 在 Application.java 或 Config 中配置
@Bean
public ChatLanguageModel chatLanguageModel() {
    return OpenAiChatModel.builder()
        .apiKey(System.getenv("OPENAI_API_KEY"))
        .modelName("gpt-4o")
        .build();
}
```

### 接入 RAG

生成的项目已包含完整的 RAG 管道骨架。接入方式：

```java
// 1. 注入 VectorStore Bean
@Bean
public VectorStore vectorStore(JdbcTemplate jdbcTemplate) {
    return PgVectorStore.builder(jdbcTemplate, new SimpleVectorTableFactory()).build();
}

// 2. 使用生成的 IngestionPipeline 摄入文档
@Autowired
private IngestionPipeline pipeline;
pipeline.ingest(Path.of("docs/产品手册.txt"));

// 3. 使用 ChatService 查询（带 RAG 上下文增强）
// 通过 RetrievalAugmentor 自动检索相关片段并传入 LLM
```

### 启用 Nacos

1. 确保 Nacos Server 已启动（Docker 或本地）
2. 启动应用后自动注册到 Nacos
3. 配置动态刷新：在需要热更新的 Bean 上加 `@RefreshScope`

### Docker 部署

```bash
# 1. 编译
cd my-ai-app
mvn clean package -DskipTests

# 2. 构建镜像
docker build -f docker/Dockerfile -t my-ai-app .

# 3. 启动全家桶（App + Ollama + PGVector + Nacos）
cd docker
export OPENAI_API_KEY=sk-...
docker-compose up -d
```

---

## 常见问题

### Q: 生成的项目如何在不同的 LLM 供应商之间切换？

通过配置切换默认供应商：

```bash
# 环境变量
export AI_DEFAULT_PROVIDER=anthropic

# 或代码中指定
LLMProviderAdapter adapter = factory.getAdapter(LLMProviderType.ANTHROPIC);
```

### Q: ACP 和 A2A 有什么区别，应该选哪个？

- **A2A**（Google）是当前主流，150+ 组织支持，推荐使用
- **ACP**（IBM）正在被 Linux 基金会合并到 A2A
- 如果新项目直接选 A2A；如果已有 ACP 存量，可用 `--protocols=acp` 生成兼容端点
- 选择 `acp` 时，生成的项目同时包含 A2A 基础结构以方便迁移

### Q: 生成的 Memory 能在多节点共享吗？

默认生成的是内存实现（`ConcurrentHashMap`），适合开发测试。生产环境需要替换为 Redis 或数据库实现：

```java
// 实现 ConversationRepository 接口
@Repository
public class RedisConversationRepository implements ConversationRepository {
    // 用 Redis 实现
}
```

### Q: 如何添加自定义 LLM 供应商？

1. 在 `infra/llm/` 下新建 Adapter 类，实现 `LLMProviderAdapter` 接口
2. 在 `LLMProviderFactory` 中注册
3. 在 `domain/enums/LLMProviderType.java` 中添加枚举值

### Q: 编译报 "Unresolved dependency" 怎么办？

某些 LLM Provider 的 Maven artifact 可能在 Milestone/Snapshot 仓库中。生成项目的根 POM 已经配置了 `spring-milestones` 和 `sonatype-snapshots` 仓库，如仍有问题，删除本地缓存后重试：

```bash
rm -rf ~/.m2/repository/org/springframework/ai/
mvn clean compile
```

### Q: 最小生成的项目有多大？

最小配置（`rest + openai`，无 feature）生成约 **25 个 Java 文件**，编译后 JAR 约 **20MB**。完整配置（全协议+全特性）约 **50 个 Java 文件**，编译后 JAR 约 **40MB**。
