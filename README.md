# scaffold4j

**Java AI Application Scaffold CLI Tool** — 快速生成基于 Java 的 AI 应用项目骨架。

## 功能特性

- **多协议支持**: RESTful API / MCP / A2A / ACP，通过 `--protocols` 参数自由组合
- **多 LLM Provider**: OpenAI、Ollama、Anthropic、DeepSeek、智谱、通义千问、豆包 等 11 种
- **双 AI 框架**: Spring AI + LangChain4j，可单独或同时使用
- **Agent 编排**: LangGraph4j 工作流 + ReAct Agent 模式
- **RAG 管道**: 文档加载 → 分块 → 嵌入 → 检索增强生成
- **Chat Memory**: Token 窗口 / 消息窗口两种记忆策略
- **SSE 流式输出**: 基于 Spring WebFlux 的 Server-Sent Events
- **WebSocket**: 双向实时 AI 对话
- **Nacos**: 服务注册与配置管理
- **向量数据库**: PGVector、Milvus、Chroma、Pinecone、Elasticsearch、Redis、Qdrant、Weaviate
- **配置化管理**: 统一的 `scaffold4j.*` 配置命名空间，所有参数支持环境变量覆盖

## 快速开始

### 安装

```bash
# 从源码构建
git clone https://github.com/scaffold4j/scaffold4j.git
cd scaffold4j
mvn clean package -DskipTests

# 创建别名（可选）
alias scaffold4j="java -jar $(pwd)/scaffold4j-cli/target/scaffold4j-cli-1.0.0-SNAPSHOT.jar"
```

### 使用

#### 最小化生成

```bash
scaffold4j generate \
  --name=my-ai-app \
  --package=com.example.ai \
  --protocols=rest,mcp \
  --llm-providers=openai,ollama
```

#### 完整功能生成

```bash
scaffold4j generate \
  --name=my-ai-app \
  --package=com.example.ai \
  --protocols=rest,mcp,a2a \
  --ai-framework=both \
  --llm-providers=openai,anthropic,ollama \
  --vector-store=pgvector \
  --features=memory,rag,sse,websocket \
  --nacos=true \
  --output-dir=./output
```

#### 交互式模式

```bash
scaffold4j generate --interactive
```

#### 查看支持的供应商

```bash
scaffold4j list-providers
```

### 启动生成的项目

```bash
cd my-ai-app

# 设置 LLM API Key
export OPENAI_API_KEY=sk-...
export ANTHROPIC_API_KEY=sk-ant-...

# 启动
./mvnw -pl my-ai-app-bootstrap spring-boot:run
```

访问:
- REST API: `http://localhost:8080/api/v1/chat`
- SSE 流: `http://localhost:8080/api/v1/stream/chat`
- WebSocket: `ws://localhost:8080/ws/chat`
- A2A Agent Card: `http://localhost:8080/.well-known/agent.json`

## CLI 参数

| 参数 | 默认值 | 说明 |
|-----|--------|------|
| `--name` | 必填 | 项目名称 |
| `--package` | 必填 | 基础包名 (com.example.ai) |
| `--group-id` | 从 package 推导 | Maven groupId |
| `--artifact-id` | = name | Maven artifactId |
| `--version` | 1.0.0-SNAPSHOT | 项目版本 |
| `--java-version` | 17 | Java 版本 (17+) |
| `--spring-boot-version` | 3.5.0 | Spring Boot 版本 |
| `--protocols` | rest | rest,mcp,a2a,acp 组合 |
| `--ai-framework` | spring-ai | spring-ai / langchain4j / both |
| `--llm-providers` | openai | 逗号分隔的 LLM 供应商 |
| `--vector-store` | pgvector | 向量数据库 |
| `--features` | 无 | memory,rag,sse,websocket 组合 |
| `--nacos` | false | 是否启用 Nacos |
| `--nacos-addr` | localhost:8848 | Nacos 地址 |
| `--output-dir` | ./ | 输出目录 |

## 生成的项目结构

```
my-ai-app/
├── pom.xml                          # 根 POM (聚合 + 依赖管理)
├── .gitignore
├── .editorconfig
├── README.md
├── docker/                          # Docker 配置
│   ├── Dockerfile
│   └── docker-compose.yml
│
├── my-ai-app-common/                # 公共模块: 常量、异常、统一响应
├── my-ai-app-domain/                # 领域模块: 实体、DTO、枚举
├── my-ai-app-infra/                 # 基础设施: LLM适配、向量存储、记忆、RAG
├── my-ai-app-app/                   # 应用层: 服务、Agent、工具、Prompt模板
├── my-ai-app-api/                   # API层: REST/MCP/A2A/ACP/WebSocket
│   ├── rest/                        # RESTful 控制器
│   ├── mcp/                         # MCP Server + Tools (conditional)
│   ├── a2a/                         # A2A Agent (conditional)
│   ├── acp/                         # ACP Endpoint (conditional)
│   └── ws/                          # WebSocket Handler (conditional)
├── my-ai-app-bootstrap/             # 启动模块: Application + 配置
│   └── src/main/resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-prod.yml
│       └── logback-spring.xml
└── my-ai-app-test/                  # 测试模块 (optional)
```

## 支持矩阵

### LLM Providers

| Provider | Spring AI | LangChain4j | 需要 API Key |
|----------|-----------|-------------|-------------|
| OpenAI | `spring-ai-starter-model-openai` | `langchain4j-open-ai` | OPENAI_API_KEY |
| Ollama | `spring-ai-starter-model-ollama` | `langchain4j-ollama` | - |
| Anthropic | `spring-ai-starter-model-anthropic` | `langchain4j-anthropic` | ANTHROPIC_API_KEY |
| DeepSeek | `spring-ai-starter-model-deepseek` | `langchain4j-deepseek` | DEEPSEEK_API_KEY |
| 智谱 GLM | `spring-ai-starter-model-zhipuai` | - | ZHIPUAI_API_KEY |
| Vertex AI | `spring-ai-starter-model-vertex-ai-gemini` | `langchain4j-vertex-ai-gemini` | GOOGLE_APPLICATION_CREDENTIALS |
| Azure OpenAI | `spring-ai-starter-model-azure-openai` | `langchain4j-azure-open-ai` | AZURE_OPENAI_API_KEY |
| AWS Bedrock | `spring-ai-starter-model-bedrock-ai` | `langchain4j-bedrock` | AWS_ACCESS_KEY_ID |
| 通义千问 | `spring-ai-starter-model-dashscope` | - | DASHSCOPE_API_KEY |
| Moonshot | - | - | MOONSHOT_API_KEY |
| 豆包 | - | - | DOUBAO_API_KEY |

### Vector Stores

| Store | 类型 | 适用场景 |
|-------|------|---------|
| PGVector | PostgreSQL 扩展 | 已有 PG 的生产环境 |
| Milvus | 独立向量数据库 | 大规模向量检索 |
| Chroma | 轻量嵌入数据库 | 开发/小规模部署 |
| Pinecone | 云服务 | 免运维生产环境 |
| Redis Stack | 内存存储 | 低延迟实时检索 |
| Simple | 内存 | 开发测试 |

## 技术栈

- **Java**: 17+ (LTS)
- **Spring Boot**: 3.5.0
- **Spring AI**: 1.0.1
- **LangChain4j**: 1.0.0-beta1
- **LangGraph4j**: 1.2.3
- **Nacos**: 2.4.x
- **Maven**: 3.9+
- **Docker**: Compose v3.9

## 开发

```bash
# 编译
mvn clean compile

# 测试
mvn test

# 打包
mvn clean package -DskipTests
```

## License

Apache 2.0
