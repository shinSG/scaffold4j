# scaffold4j 生成代码架构分析与改进方案

> 基于 Spring AI、LangChain4j、Dify 等开源 Java AI 应用项目的架构对比分析
>
> 分析日期: 2026-05-27

---

## 1. 总体评价

### 1.1 做得好的方面

| 方面 | 说明 |
|------|------|
| **多模块分层架构** | `common <- domain <- infra <- app <- api <- bootstrap` 分层链路清晰，符合 DDD 分层最佳实践 |
| **条件化生成** | 通过 `ProjectConfig` 的布尔检查门控文件和依赖输出，组合空间大 |
| **多框架支持** | 同时支持 Spring AI 和 LangChain4j，覆盖主流 Java AI 框架 |
| **多协议支持** | REST / MCP / A2A / ACP 四种协议，在同类工具中非常全面 |
| **基础设施即代码** | Docker Compose 按需生成数据库、向量存储、MQ 等服务 |

### 1.2 核心问题概览

| 编号 | 问题 | 严重程度 | 影响范围 |
|------|------|----------|----------|
| P0 | LLM Adapter 全是空实现 (throw UnsupportedOperationException) | **阻塞** | 生成的项目无法运行 |
| P1 | application.yml 缩进错误导致配置解析失败 | **阻塞** | 启动即报错 |
| P1 | LLMProviderConfig 未注册任何 Adapter 到 Factory | **阻塞** | Bean 注入失败 |
| P2 | 缺少测试代码生成 | 高 | 无法验证生成代码正确性 |
| P2 | Tool 未使用框架注解 (`@Tool`) | 高 | Agent 无法发现和调用工具 |
| P2 | Agent 实现过于简化 (关键词匹配) | 高 | 无实际 Agent 能力 |
| P3 | Memory 仅 ConcurrentHashMap，无持久化 | 中 | 生产不可用 |
| P3 | RAG 文件未生成 (条件判断问题) | 中 | RAG 功能缺失 |
| P3 | ChatMessage/Conversation 未使用 Lombok | 中 | 样板代码过多 |
| P4 | 缺少全局异常处理器 | 中 | 错误处理不规范 |
| P4 | 缺少可观测性 (Observability) | 中 | 生产排查困难 |
| P5 | ModuleGenerator 3700 行巨文件 | 低 | 脚手架自身维护困难 |

---

## 2. 问题详细分析

### 2.1 [P0] LLM Adapter 空实现 -- 生成项目无法运行

**现状**: 所有 `{Provider}Adapter.java` 的方法体都是:

```java
@Override
public String invoke(String systemPrompt, String userMessage) {
    // TODO: Implement OpenAI LLM invocation using Spring AI ChatClient or LangChain4j ChatLanguageModel
    throw new UnsupportedOperationException("OpenAI adapter not yet configured. Set env: OPENAI_API_KEY");
}
```

**对比开源项目**:

- **Spring AI** 直接使用 `ChatModel.call(Prompt)` 或 `ChatClient.prompt().user(msg).call().content()`
- **LangChain4j** 使用 `ChatLanguageModel.generate(messages)` 或 `AiServices` 声明式接口

**改进建议**:

生成的 Adapter 应包含真实实现骨架。以 Spring AI + OpenAI 为例:

```java
@Component
public class OpenaiAdapter implements LLMProviderAdapter {

    private final ChatModel chatModel;
    private final StreamingChatModel streamingChatModel;

    public OpenaiAdapter(ChatModel chatModel, StreamingChatModel streamingChatModel) {
        this.chatModel = chatModel;
        this.streamingChatModel = streamingChatModel;
    }

    @Override
    public String invoke(String systemPrompt, String userMessage) {
        return chatModel.call(
            new Prompt(List.of(
                new SystemMessage(systemPrompt),
                new UserMessage(userMessage)
            ))
        ).getResult().getOutput().getText();
    }

    @Override
    public Flux<String> invokeStream(String systemPrompt, String userMessage) {
        return streamingChatModel.stream(
            new Prompt(List.of(
                new SystemMessage(systemPrompt),
                new UserMessage(userMessage)
            ))
        ).map(response -> response.getResult().getOutput().getText());
    }
}
```

LangChain4j 版本:

```java
@Component
public class OpenaiAdapter implements LLMProviderAdapter {

    private final ChatLanguageModel chatModel;
    private final StreamingChatLanguageModel streamingChatModel;

    // constructor injection...

    @Override
    public String invoke(String systemPrompt, String userMessage) {
        return chatModel.generate(
            List.of(
                dev.langchain4j.data.message.SystemMessage.from(systemPrompt),
                dev.langchain4j.data.message.UserMessage.from(userMessage)
            )
        ).content().text();
    }
}
```

**关键点**: Spring AI 的 `ChatModel` 和 LangChain4j 的 `ChatLanguageModel` 都已通过 Starter 自动配置注入，脚手架只需生成正确的调用代码。

---

### 2.2 [P1] application.yml 缩进/结构错误

**现状**: 生成的 `application.yml` 存在严重的 YAML 缩进问题:

```yaml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    # Cache Configuration  <-- 嵌套在 mybatis-plus.configuration 下!
    spring:
      cache:
        type: redis
          data:              <-- 缩进错误
            redis:
              host: ...
    # Nacos 也嵌套在 mybatis-plus.configuration 下!
    spring:
      cloud:
        nacos: ...
```

`spring.datasource`、`spring.cache`、`spring.cloud.nacos` 都被错误地嵌套在 `mybatis-plus.configuration` 节点下。多个 `spring:` 顶层键重复定义。

**改进建议**:

在 `ConfigGenerator` 中确保每个顶层配置块（`spring:`、`mybatis-plus:`、`logging:` 等）都是正确对齐的顶级键，不存在嵌套错误。建议:

1. 使用 YAML 库 (SnakeYAML) 构建配置树，而非字符串拼接
2. 生成后做一次 YAML 解析验证
3. 按逻辑分组: `spring.*` 放一起、`mybatis-plus` 放一起、自定义 `scaffold4j.*` 放一起

---

### 2.3 [P1] LLMProviderConfig 未注册 Adapter

**现状**: `LLMProviderConfig.java` 创建了 `LLMProviderFactory` Bean，但没有将任何 Adapter 注册进去:

```java
@Configuration
public class LLMProviderConfig {
    @Bean
    public LLMProviderFactory llmProviderFactory() {
        return new LLMProviderFactory();  // 空的! adapters map 为空
    }
}
```

`ChatService` 调用 `factory.getDefaultAdapter()` 时，`adapters.values().iterator().next()` 会抛 `NoSuchElementException`。

**改进建议**:

注入所有 `LLMProviderAdapter` Bean 并自动注册:

```java
@Configuration
public class LLMProviderConfig {

    @Bean
    public LLMProviderFactory llmProviderFactory(
            List<LLMProviderAdapter> adapters,
            @Value("${scaffold4j.ai.default-provider}") String defaultProvider) {
        LLMProviderFactory factory = new LLMProviderFactory();
        for (LLMProviderAdapter adapter : adapters) {
            factory.register(LLMProviderType.valueOf(adapter.providerName().toUpperCase()), adapter);
        }
        factory.setDefaultProvider(LLMProviderType.valueOf(defaultProvider.toUpperCase()));
        return factory;
    }
}
```

---

### 2.4 [P2] Tool 未使用框架注解

**现状**: 生成的 `WeatherTool.java` 和 `SearchTool.java` 是普通 Spring `@Component`:

```java
@Component
public class WeatherTool {
    public String getWeather(String city, String unit) { ... }
}
```

**对比开源项目**:

Spring AI 和 LangChain4j 都使用注解驱动的 Tool 注册:

```java
// Spring AI 风格
@Component
public class WeatherTool {
    @Tool(description = "Get current weather for a city")
    public String getWeather(
        @ToolParam(description = "City name") String city,
        @ToolParam(description = "Temperature unit") String unit) {
        // ...
    }
}

// LangChain4j 风格
@Component
public class WeatherTool {
    @Tool("Get current weather for a city")
    String getWeather(
        @P("City name") String city,
        @P("Temperature unit") String unit) {
        // ...
    }
}
```

**改进建议**:

1. 根据选择的 AI 框架生成对应的注解 (`@Tool` / `@ToolParam` / `@P`)
2. 在 `ChatClient` 或 `AiServices` 构建时注册工具
3. Tool 方法需要有清晰的参数描述，LLM 才能正确调用

---

### 2.5 [P2] Agent 实现过于简化

**现状**:

- `AgentService` 直接委托给 `ChatService`，无 Agent 逻辑
- `AIAgentService` 使用关键词匹配做意图路由 (if message contains "weather")
- `ReactAgent` 只有 TODO 注释
- `AgentOrchestrator` 有编译错误 (`new com/example/ai.domain.dto.ChatRequest()`)

**对比开源项目**:

- **LangChain4j Agentic**: 完整的 Supervisor/Planner/Workflow 模式
- **Spring AI**: 通过 `ToolCallAdvisor` 实现自动工具调用循环
- **Dify**: Chain-of-Thought 和 Function-Calling 两种 Agent Runner

**改进建议**:

生成的 Agent 代码应包含真实的 ReAct 循环:

```java
// Spring AI 风格 -- ChatClient 自动处理工具调用
@Service
public class AgentService {

    private final ChatClient.Builder chatClientBuilder;

    public ChatResponse execute(ChatRequest request) {
        ChatClient client = chatClientBuilder
            .defaultTools(weatherTool, searchTool)  // 注册工具
            .defaultSystem("You are a helpful agent with access to tools.")
            .build();

        String response = client.prompt()
            .user(request.getMessage())
            .call()
            .content();

        return new ChatResponse(response);
    }
}

// LangChain4j 风格 -- AI Services 声明式
@Service
public class AgentService {

    private final MyAgent agent;  // AiServices 构建的接口

    public ChatResponse execute(ChatRequest request) {
        return new ChatResponse(agent.chat(request.getMessage()));
    }
}

interface MyAgent {
    @SystemMessage("You are a helpful agent with access to tools.")
    String chat(@UserMessage String message);
}

// 构建:
MyAgent agent = AiServices.builder(MyAgent.class)
    .chatLanguageModel(model)
    .tools(weatherTool, searchTool)
    .build();
```

---

### 2.6 [P2] 缺少测试代码生成

**现状**: 生成的项目 `src/test/` 目录完全为空，没有任何测试文件。

**对比开源项目**:

- Spring AI 有 `spring-ai-test` 模块
- LangChain4j 有 `langchain4j-test` 模块
- 两者都提供 LLM mock 和 Testcontainers 集成测试支持

**改进建议**:

至少生成以下测试:

1. **单元测试**: `ChatServiceTest` (mock LLM adapter)
2. **集成测试**: `ChatControllerIntegrationTest` (MockMvc)
3. **Smoke 测试**: 验证 Spring 上下文能正常启动
4. **测试配置**: `application-test.yml` 使用 H2 + SimpleVectorStore

```java
@SpringBootTest
@AutoConfigureMockMvc
class ChatControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @MockBean LLMProviderFactory factory;

    @Test
    void shouldReturnChatResponse() throws Exception {
        when(factory.getDefaultAdapter()).thenReturn(mockAdapter);
        when(mockAdapter.invoke(any(), any())).thenReturn("Hello!");

        mockMvc.perform(post("/api/v1/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"Hi\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").value("Hello!"));
    }
}
```

---

### 2.7 [P3] Memory 仅支持内存存储

**现状**: `ChatMemoryStore` 使用 `ConcurrentHashMap`，进程重启后数据丢失。`ConversationRepository` 是接口但无实现。

**对比开源项目**:

- **Spring AI**: `ChatMemoryRepository` 接口 + `InMemoryChatMemoryRepository` / `JdbcChatMemoryRepository` 实现
- **LangChain4j**: `ChatMemoryStore` 接口 + `InMemoryChatMemoryStore` 实现

**改进建议**:

1. 生成 `RedisChatMemoryStore` 实现 (当选择 Redis 缓存时)
2. 生成 `JdbcChatMemoryRepository` 实现 (当选择数据库时)
3. 使用 `@ConditionalOnProperty` 切换实现
4. 集成框架原生的 ChatMemory (Spring AI 的 `MessageWindowChatMemory` 或 LangChain4j 的 `MessageWindowChatMemory`)

---

### 2.8 [P3] RAG 文件未生成

**现状**: 尽管 `ErrorCode` 中有 `VECTOR_STORE_ERROR` 和 `RAG_PIPELINE_ERROR`，但 `DocumentLoader.java`、`TextSplitter.java`、`EmbeddingService.java` 均未生成。

**对比开源项目**:

- **Spring AI RAG**: 完整管道 -- `Query` -> `ContentRetriever` -> `RetrievalAugmentationAdvisor`
- **LangChain4j RAG**: `EmbeddingStoreIngestor` (摄入) + `ContentRetriever` (检索) + `RetrievalAugmentor` (增强)

**改进建议**:

生成完整的 RAG 管道:

```
摄入管道: DocumentLoader -> TextSplitter -> EmbeddingModel -> VectorStore
检索管道: QueryTransformer -> VectorStore.similaritySearch -> Reranker -> ContextAssembler
```

优先使用框架原生组件而非自行实现:
- Spring AI: `VectorStore` + `TokenTextSplitter` + `EmbeddingModel` + `RetrievalAugmentationAdvisor`
- LangChain4j: `EmbeddingStore` + `DocumentSplitter` + `EmbeddingModel` + `DefaultRetrievalAugmentor`

---

### 2.9 [P3] 域模型未使用 Lombok

**现状**: `ChatMessage`、`Conversation`、`ChatRequest`、`ChatResponse` 等都手写 getter/setter，每个类 50+ 行。

**改进建议**:

Domain 模块已在根 POM 中引入 Lombok 依赖，应使用:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    @NotBlank(message = "Message cannot be blank")
    private String message;
    private String conversationId;
    private String provider;
}
```

可减少约 60% 的样板代码。

---

### 2.10 [P4] 缺少全局异常处理器

**现状**: 有 `BaseException`、`AIException`、`ErrorResponse` 类，但没有 `@RestControllerAdvice` 全局异常处理器。

**改进建议**:

在 API 模块生成:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Result<Void>> handleBaseException(BaseException e) {
        return ResponseEntity.badRequest()
            .body(Result.error(e.code(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        return ResponseEntity.internalServerError()
            .body(Result.error(500, "Internal Server Error"));
    }
}
```

---

### 2.11 [P4] 缺少可观测性支持

**现状**: 仅有 Logback 日志配置，无 Metrics、Tracing、结构化日志。

**对比开源项目**:

- **Spring AI**: 内置 Micrometer Observation (`ChatModelObservationConvention`)
- **LangChain4j**: `langchain4j-observation` + `langchain4j-micrometer-metrics`
- 两者都支持: Token 使用量、延迟、成本追踪

**改进建议**:

1. 在 bootstrap POM 中添加 `micrometer-registry-prometheus` 依赖
2. 生成 `ObservabilityConfig.java` 配置 Micrometer + OpenTelemetry
3. 在 `ChatService` 中记录关键指标: 请求延迟、Token 使用量、错误率
4. 生成 `application.yml` 中的 management endpoints 配置

---

### 2.12 [P5] ModuleGenerator 过于庞大

**现状**: `ModuleGenerator.java` 有 3697 行，所有 Java 源文件的生成逻辑集中在一个文件中。

**改进建议**:

拆分为按模块的子生成器:

```
generator/
├── ProjectGenerator.java          # 编排器
├── PomGenerator.java              # POM 文件
├── ConfigGenerator.java           # 配置文件
├── DockerGenerator.java           # Docker 文件
├── GitignoreGenerator.java        # Git 忽略
├── module/
│   ├── CommonModuleGenerator.java
│   ├── DomainModuleGenerator.java
│   ├── InfraModuleGenerator.java
│   ├── AppModuleGenerator.java
│   ├── ApiModuleGenerator.java
│   └── BootstrapModuleGenerator.java
└── TemplateEngine.java            # 迁移到 Mustache 模板
```

同时考虑将字符串模板迁移到 Mustache `.mustache` 文件 (依赖已引入但未使用)，提高可维护性。

---

## 3. 架构层面的改进建议

### 3.1 引入 Advisor/Interceptor 中间件链

**背景**: Spring AI 1.0 的核心架构模式是 Advisor Chain -- 类似 Servlet Filter 的可插拔中间件链，每个 Advisor 可以在 LLM 调用前后修改请求/响应。

**当前缺失**: 生成的代码没有利用这一模式，Memory、RAG、Safety、Logging 都是硬编码在 Service 中。

**建议**: 生成 Advisor 集成代码:

```java
@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatModel chatModel,
                                  VectorStore vectorStore,
                                  ChatMemoryRepository memoryRepository) {
        return ChatClient.builder(chatModel)
            .defaultAdvisors(
                new MessageChatMemoryAdvisor(memoryRepository),
                new RetrievalAugmentationAdvisor(vectorStore),
                new SimpleLoggerAdvisor()
            )
            .build();
    }
}
```

### 3.2 引入 LangChain4j AI Services 声明式接口

**背景**: LangChain4j 的 `AiServices` 模式允许通过定义接口 + 注解自动生成 Agent 实现，非常 Java-idiomatic。

**建议**: 当选择 LangChain4j 框架时，生成 AI Services 接口:

```java
public interface ChatAssistant {
    @SystemMessage("You are a helpful AI assistant.")
    String chat(@MemoryId String conversationId, @UserMessage String message);

    @SystemMessage("You are a helpful AI assistant.")
    TokenStream streamChat(@MemoryId String conversationId, @UserMessage String message);
}
```

```java
@Configuration
public class AiServicesConfig {

    @Bean
    public ChatAssistant chatAssistant(ChatLanguageModel model,
                                        ChatMemoryStore memoryStore) {
        return AiServices.builder(ChatAssistant.class)
            .chatLanguageModel(model)
            .chatMemoryProvider(memoryId ->
                MessageWindowChatMemory.builder()
                    .id(memoryId)
                    .maxMessages(20)
                    .chatMemoryStore(memoryStore)
                    .build())
            .build();
    }
}
```

### 3.3 统一 RAG 管道抽象

**建议**: 生成清晰的两阶段 RAG 管道:

```
摄入管道 (Ingestion Pipeline):
  Document -> DocumentParser -> TextSplitter -> EmbeddingModel -> VectorStore

检索管道 (Retrieval Pipeline):
  UserQuery -> QueryTransformer -> VectorStoreSearch -> Reranker -> ContextAssembler -> AugmentedPrompt
```

使用框架原生组件:
- Spring AI: `TokenTextSplitter`, `EmbeddingModel`, `VectorStore`, `RetrievalAugmentationAdvisor`
- LangChain4j: `DocumentSplitter`, `EmbeddingModel`, `EmbeddingStore`, `DefaultRetrievalAugmentor`

### 3.4 安全防护 (Guardrails)

**现状**: 无输入/输出安全检查。

**建议**: 生成基础的安全防护:

```java
// 输入检查
@Component
public class InputGuardrail {
    public void validate(String input) {
        if (input.length() > MAX_INPUT_LENGTH) throw new AIException("Input too long");
        // 检查注入攻击模式
    }
}

// 输出检查 (使用 Spring AI SafeGuardAdvisor 或 LangChain4j Guardrails)
```

---

## 4. 改进优先级路线图

### 第一阶段: 让生成项目能跑起来 (1-2 周)

- [ ] 修复 application.yml 缩进/结构错误
- [ ] 生成真实 LLM Adapter 实现 (基于框架 Starter 自动配置)
- [ ] 修复 LLMProviderConfig 自动注册所有 Adapter
- [ ] 修复 AgentOrchestrator 编译错误
- [ ] 生成全局异常处理器

### 第二阶段: 提升生成代码质量 (2-3 周)

- [ ] Tool 使用 `@Tool` / `@ToolParam` 注解
- [ ] Agent 使用框架原生工具调用 (ChatClient / AiServices)
- [ ] Domain 模型使用 Lombok
- [ ] 生成基础测试代码 (单元测试 + 集成测试)
- [ ] 生成 `application-test.yml` 测试配置

### 第三阶段: 完善 AI 能力 (3-4 周)

- [ ] 修复 RAG 文件生成条件
- [ ] 生成完整 RAG 管道 (摄入 + 检索)
- [ ] Memory 支持 Redis/JDBC 持久化
- [ ] 引入 Advisor Chain 中间件模式 (Spring AI)
- [ ] 引入 AI Services 声明式接口 (LangChain4j)
- [ ] 生成安全防护 (Guardrails)

### 第四阶段: 生产就绪 (4-5 周)

- [ ] 添加 Micrometer Metrics + Prometheus 端点
- [ ] 添加 OpenTelemetry Tracing
- [ ] 生成健康检查增强 (Liveness / Readiness)
- [ ] 生成 API 文档 (SpringDoc / OpenAPI 3)
- [ ] 拆分 ModuleGenerator 为子生成器
- [ ] 迁移模板到 Mustache 文件

---

## 5. 对比总结表

| 维度 | scaffold4j 当前 | Spring AI / LangChain4j 最佳实践 | 差距 |
|------|----------------|--------------------------------|------|
| **LLM 调用** | 空实现 (throw) | 框架 ChatModel/ChatLanguageModel 直接调用 | 严重 |
| **Tool 注册** | 普通 @Component | @Tool 注解 + 自动发现 | 严重 |
| **Agent 模式** | 关键词匹配 | ReAct / Function-Calling / Supervisor | 严重 |
| **RAG 管道** | 文件未生成 | 多阶段管道 (摄入+检索+增强) | 严重 |
| **Memory** | ConcurrentHashMap | 可插拔 ChatMemoryStore (Redis/JDBC) | 中等 |
| **中间件链** | 无 | Advisor Chain (可组合) | 中等 |
| **声明式 API** | 无 | AiServices 接口驱动 | 中等 |
| **可观测性** | 仅日志 | Micrometer + OpenTelemetry | 中等 |
| **测试** | 无 | 框架测试模块 + Testcontainers | 中等 |
| **安全防护** | 无 | Input/Output Guardrails | 低 |
| **配置管理** | YAML 拼接有 bug | 框架 auto-configuration | 中等 |
| **多模块架构** | 与最佳实践一致 | common<-domain<-infra<-app<-api<-bootstrap | 无差距 |

---

## 附录 A: 参考项目列表

| 项目 | Stars | 语言 | 关键模式 |
|------|-------|------|---------|
| [Spring AI](https://github.com/spring-projects/spring-ai) | 12k+ | Java | ChatClient, Advisor Chain, VectorStore 抽象 |
| [LangChain4j](https://github.com/langchain4j/langchain4j) | 7k+ | Java | AiServices, Agentic Framework, Guardrails |
| [Dify](https://github.com/langgenius/dify) | 60k+ | Python | Controller-Service-Core 分层, Tool Engine, RAG Pipeline |
| [FastGPT](https://github.com/labring/FastGPT) | 20k+ | TypeScript | Workflow 编排, Knowledge Base, RAG-first |

## 附录 B: 关键文件路径

| 文件 | 路径 | 问题 |
|------|------|------|
| ModuleGenerator | `scaffold4j-cli/src/main/java/com/scaffold4j/generator/ModuleGenerator.java` | 3697 行巨文件 |
| ConfigGenerator | `scaffold4j-cli/src/main/java/com/scaffold4j/generator/ConfigGenerator.java` | YAML 缩进 bug |
| LLMProviderAdapter | `my-ai-app/.../infra/llm/LLMProviderAdapter.java` | 接口设计合理 |
| OpenaiAdapter | `my-ai-app/.../infra/llm/OpenaiAdapter.java` | 空实现 |
| LLMProviderConfig | `my-ai-app/.../infra/config/LLMProviderConfig.java` | 未注册 Adapter |
| ChatService | `my-ai-app/.../app/service/ChatService.java` | 硬编码 system prompt |
| AgentService | `my-ai-app/.../app/service/AgentService.java` | 仅委托 ChatService |
| WeatherTool | `my-ai-app/.../app/tool/WeatherTool.java` | 无 @Tool 注解 |
| AIAgentService | `my-ai-app/.../app/agent/AIAgentService.java` | 关键词匹配路由 |
| ChatMemoryStore | `my-ai-app/.../infra/memory/ChatMemoryStore.java` | 仅内存存储 |
| application.yml | `my-ai-app/.../resources/application.yml` | YAML 缩进错误 |
