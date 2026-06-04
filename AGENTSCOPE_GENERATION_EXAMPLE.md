# AgentScope 代码生成示例

## 概述

本文档提供在 scaffold4j 中生成 AgentScope 多智能体项目的示例代码。

## 生成命令

```bash
java -jar scaffold4j-cli/target/scaffold4j-cli-1.0.0-SNAPSHOT.jar generate \
  --name=ai-agents \
  --package=com.example.ai \
  --framework=agentscope \
  --protocols=rest,mcp \
  --features=memory,rag
```

## 生成的项目结构

```
ai-agents/
├── pom.xml
├── ai-agents-common/
│   └── src/main/java/com/example/ai/common/
│       ├── constant/
│       │   ├── ErrorCode.java
│       │   └── CommonConstant.java
│       ├── exception/
│       │   └── BaseException.java
│       └── util/
│           └── JsonUtils.java
├── ai-agents-domain/
│   └── src/main/java/com/example/ai/domain/
│       ├── dto/
│       │   ├── AgentTask.java
│       │   └── TaskResult.java
│       └── entity/
│           └── AgentMessage.java
├── ai-agents-infra/
│   └── src/main/java/com/example/ai/infra/
│       ├── llm/
│       │   └── LlmProviderAdapter.java
│       └── vector/
│           └── VectorStoreAdapter.java
├── ai-agents-app/
│   └── src/main/java/com/example/ai/app/
│       ├── service/
│       │   └── AgentOrchestrationService.java
│       └── agent/
│           └── BaseAgent.java
├── ai-agents-agents/                    # ⭐ AgentScope 模块
│   └── src/main/java/com/example/ai/agents/
│       ├── coordinator/
│       │   └── TaskCoordinatorAgent.java
│       ├── worker/
│       │   ├── ResearcherAgent.java
│       │   ├── CoderAgent.java
│       │   └── ReviewerAgent.java
│       ├── tools/
│       │   ├── SearchTool.java
│       │   └── CodeAnalysisTool.java
│       └── config/
│           └── AgentScopeConfig.java
├── ai-agents-api/
│   └── src/main/java/com/example/ai/api/
│       └── AgentController.java
├── ai-agents-bootstrap/
│   └── src/main/java/com/example/ai/
│       └── AiAgentsApplication.java
└── docker/
    ├── Dockerfile
    └── docker-compose.yml
```

## 生成的代码示例

### 1. AgentScope 配置类

```java
package com.example.ai.agents.config;

import com.agentscope.agent.AgentConfig;
import com.agentscope.agent.AgentRegistry;
import com.agentscope.orchestration.Coordinator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Configuration
public class AgentScopeConfig {

    @Value("${agentscope.agents.coordinator.max-workers:10}")
    private int maxWorkers;

    @Value("${agentscope.orchestration.timeout:60}")
    private int timeout;

    @Bean
    public AgentRegistry agentRegistry() {
        return AgentRegistry.create();
    }

    @Bean
    public Coordinator agentCoordinator(AgentRegistry registry) {
        List<Agent> agents = registry.getAllAgents();
        return new Coordinator(agents, maxWorkers, timeout);
    }
}
```

### 2. 协调器 Agent

```java
package com.example.ai.agents.coordinator;

import com.agentscope.agent.Agent;
import com.agentscope.agent.AgentConfig;
import com.agentscope.message.Message;
import com.agentscope.message.MessageType;
import com.example.ai.agents.BaseAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 任务协调器 - 负责任务分解和分配
 */
public class TaskCoordinatorAgent extends BaseAgent {
    
    private static final Logger log = LoggerFactory.getLogger(TaskCoordinatorAgent.class);
    
    private final List<Agent> workerAgents;
    
    public TaskCoordinatorAgent(AgentConfig config, List<Agent> workerAgents) {
        super("task-coordinator", config);
        this.workerAgents = workerAgents;
    }
    
    @Override
    public Message onMessage(Message message) {
        log.info("Coordinator received task: {}", message.getContent());
        
        // 分解任务
        List<String> subTasks = decomposeTask(message.getContent());
        
        // 分发给 worker agents
        List<CompletableFuture<String>> results = subTasks.stream()
            .map(this::dispatchToWorker)
            .toList();
        
        // 等待所有任务完成
        CompletableFuture.allOf(results.toArray(new CompletableFuture[0])).join();
        
        // 汇总结果
        String finalResult = aggregateResults(results);
        
        return Message.builder()
            .type(MessageType.RESPONSE)
            .sender(getName())
            .recipient(message.getSender())
            .content(finalResult)
            .build();
    }
    
    private List<String> decomposeTask(String task) {
        // 根据任务类型进行分解
        return List.of("Research: " + task, "Implement: " + task, "Review: " + task);
    }
    
    private CompletableFuture<String> dispatchToWorker(String subTask) {
        return CompletableFuture.supplyAsync(() -> {
            // 根据子任务类型选择合适的 worker
            Agent worker = selectWorker(subTask);
            
            Message taskMessage = Message.builder()
                .type(MessageType.TASK)
                .sender(getName())
                .recipient(worker.getName())
                .content(subTask)
                .build();
            
            Message response = worker.onMessage(taskMessage);
            return response.getContent();
        });
    }
    
    private Agent selectWorker(String subTask) {
        if (subTask.contains("Research")) {
            return workerAgents.stream()
                .filter(w -> w.getName().equals("researcher"))
                .findFirst()
                .orElseThrow();
        } else if (subTask.contains("Implement")) {
            return workerAgents.stream()
                .filter(w -> w.getName().equals("coder"))
                .findFirst()
                .orElseThrow();
        } else {
            return workerAgents.stream()
                .filter(w -> w.getName().equals("reviewer"))
                .findFirst()
                .orElseThrow();
        }
    }
    
    private String aggregateResults(List<CompletableFuture<String>> results) {
        return results.stream()
            .map(CompletableFuture::join)
            .reduce((a, b) -> a + "\n---\n" + b)
            .orElse("No results");
    }
}
```

### 3. 研究员 Agent

```java
package com.example.ai.agents.worker;

import com.agentscope.agent.AgentConfig;
import com.agentscope.message.Message;
import com.example.ai.agents.BaseAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 研究员 Agent - 负责信息搜索和分析
 */
public class ResearcherAgent extends BaseAgent {
    
    private static final Logger log = LoggerFactory.getLogger(ResearcherAgent.class);
    
    public ResearcherAgent(AgentConfig config) {
        super("researcher", config);
    }
    
    @Override
    public Message onMessage(Message message) {
        String query = message.getContent();
        log.info("Researcher processing: {}", query);
        
        // 执行搜索
        String searchResult = performSearch(query);
        
        // 分析结果
        String analysis = analyzeResults(searchResult);
        
        return Message.builder()
            .type(MessageType.RESPONSE)
            .sender(getName())
            .recipient(message.getSender())
            .content(analysis)
            .build();
    }
    
    private String performSearch(String query) {
        // 集成搜索引擎或知识库
        return "Search results for: " + query;
    }
    
    private String analyzeResults(String results) {
        // 分析搜索结果
        return "Analysis: " + results;
    }
}
```

### 4. 工具定义

```java
package com.example.ai.agents.tools;

import com.agentscope.tool.Tool;
import com.agentscope.tool.ToolParameter;
import com.agentscope.tool.ToolResult;
import java.util.List;

/**
 * 代码分析工具
 */
public class CodeAnalysisTool implements Tool {
    
    @Override
    public String getName() {
        return "code-analysis";
    }
    
    @Override
    public String getDescription() {
        return "Analyze code for quality, complexity, and potential issues";
    }
    
    @Override
    public ToolResult execute(@ToolParameter("code") String code) {
        // 分析代码
        String analysis = analyzeCode(code);
        return ToolResult.success(analysis);
    }
    
    @Override
    public List<ToolParameter> getParameters() {
        return List.of(
            new ToolParameter("code", "string", "The source code to analyze", true)
        );
    }
    
    private String analyzeCode(String code) {
        int lines = code.split("\n").length;
        int complexity = calculateComplexity(code);
        
        return String.format(
            "Code Analysis:\n- Lines: %d\n- Complexity: %d\n- Rating: %s",
            lines, complexity, complexity < 10 ? "Good" : "Needs Review"
        );
    }
    
    private int calculateComplexity(String code) {
        // 简化的复杂度计算
        return (int) code.chars().filter(c -> c == '{' || c == '}').count() / 2;
    }
}
```

### 5. Agent 编排服务

```java
package com.example.ai.app.service;

import com.agentscope.orchestration.Coordinator;
import com.agentscope.message.Message;
import com.agentscope.message.MessageType;
import com.example.ai.agents.config.AgentScopeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AgentOrchestrationService {
    
    private static final Logger log = LoggerFactory.getLogger(AgentOrchestrationService.class);
    
    private final Coordinator coordinator;
    
    public AgentOrchestrationService(Coordinator coordinator) {
        this.coordinator = coordinator;
    }
    
    /**
     * 执行任务
     */
    public String executeTask(String taskDescription) {
        log.info("Executing task: {}", taskDescription);
        
        Message task = Message.builder()
            .type(MessageType.TASK)
            .sender("user")
            .content(taskDescription)
            .build();
        
        Message result = coordinator.execute(task).join();
        
        log.info("Task completed: {}", result.getContent());
        return result.getContent();
    }
    
    /**
     * 异步执行任务
     */
    public CompletableFuture<String> executeTaskAsync(String taskDescription) {
        Message task = Message.builder()
            .type(MessageType.TASK)
            .sender("user")
            .content(taskDescription)
            .build();
        
        return coordinator.execute(task)
            .thenApply(Message::getContent);
    }
    
    /**
     * 执行代码审查任务
     */
    public String reviewCode(String code) {
        String task = "Please review the following code for quality, "
            + "complexity, and potential issues:\n\n" + code;
        return executeTask(task);
    }
    
    /**
     * 执行代码生成任务
     */
    public String generateCode(String description) {
        String task = "Please generate code based on this description: " + description;
        return executeTask(task);
    }
}
```

### 6. REST 控制器

```java
package com.example.ai.api;

import com.example.ai.app.service.AgentOrchestrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/agents")
public class AgentController {
    
    private static final Logger log = LoggerFactory.getLogger(AgentController.class);
    
    private final AgentOrchestrationService orchestrationService;
    
    public AgentController(AgentOrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }
    
    /**
     * 执行通用任务
     */
    @PostMapping("/execute")
    public ResponseEntity<Map<String, String>> executeTask(
            @RequestBody Map<String, String> request) {
        
        String task = request.get("task");
        log.info("Received task request: {}", task);
        
        String result = orchestrationService.executeTask(task);
        
        return ResponseEntity.ok(Map.of(
            "status", "completed",
            "result", result
        ));
    }
    
    /**
     * 异步执行任务
     */
    @PostMapping("/execute-async")
    public ResponseEntity<Map<String, String>> executeTaskAsync(
            @RequestBody Map<String, String> request) {
        
        String task = request.get("task");
        log.info("Received async task request: {}", task);
        
        orchestrationService.executeTaskAsync(task)
            .thenAccept(result -> log.info("Async task completed: {}", result));
        
        return ResponseEntity.accepted().body(Map.of(
            "status", "accepted",
            "message", "Task is being processed asynchronously"
        ));
    }
    
    /**
     * 代码审查
     */
    @PostMapping("/review-code")
    public ResponseEntity<Map<String, String>> reviewCode(
            @RequestBody Map<String, String> request) {
        
        String code = request.get("code");
        log.info("Received code review request");
        
        String result = orchestrationService.reviewCode(code);
        
        return ResponseEntity.ok(Map.of(
            "status", "completed",
            "review", result
        ));
    }
    
    /**
     * 代码生成
     */
    @PostMapping("/generate-code")
    public ResponseEntity<Map<String, String>> generateCode(
            @RequestBody Map<String, String> request) {
        
        String description = request.get("description");
        log.info("Received code generation request: {}", description);
        
        String result = orchestrationService.generateCode(description);
        
        return ResponseEntity.ok(Map.of(
            "status", "completed",
            "code", result
        ));
    }
}
```

### 7. POM 文件片段（AgentScope 模块）

```xml
<!-- ai-agents-agents/pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>com.example.ai</groupId>
        <artifactId>ai-agents</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    
    <artifactId>ai-agents-agents</artifactId>
    <name>ai-agents-agents</name>
    <description>AgentScope Multi-Agent Module</description>
    
    <dependencies>
        <!-- AgentScope Core -->
        <dependency>
            <groupId>com.agentscope</groupId>
            <artifactId>agentscope-core</artifactId>
        </dependency>
        
        <!-- AgentScope Tools (可选) -->
        <dependency>
            <groupId>com.agentscope</groupId>
            <artifactId>agentscope-tools</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Spring Boot Starter -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        
        <!-- Internal modules -->
        <dependency>
            <groupId>com.example.ai</groupId>
            <artifactId>ai-agents-common</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.example.ai</groupId>
            <artifactId>ai-agents-domain</artifactId>
            <version>${project.version}</version>
        </dependency>
    </dependencies>
</project>
```

## 配置文件示例

### application.yml

```yaml
spring:
  application:
    name: ai-agents

# AgentScope 配置
agentscope:
  agents:
    coordinator:
      name: task-coordinator
      max-workers: 10
      timeout: 60
    worker:
      researcher:
        name: researcher
        capabilities:
          - search
          - analysis
          - summarization
      coder:
        name: coder
        capabilities:
          - code-generation
          - refactoring
          - optimization
      reviewer:
        name: reviewer
        capabilities:
          - code-review
          - quality-check
          - best-practices
  
  orchestration:
    strategy: round-robin
    max-retries: 3
    timeout: 120
  
  logging:
    enabled: true
    level: INFO
    format: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

# LLM 配置 (可选，与 Spring AI 集成使用)
ai:
  provider: dashscope
  model: qwen-turbo
  api-key: ${DASHSCOPE_API_KEY:}
```

## 测试示例

```java
package com.example.ai.agents;

import com.example.ai.app.service.AgentOrchestrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AgentScopeIntegrationTest {
    
    @Autowired
    private AgentOrchestrationService orchestrationService;
    
    @Test
    void testTaskExecution() {
        String result = orchestrationService.executeTask(
            "Research the latest AI trends"
        );
        
        assertNotNull(result);
        assertTrue(result.contains("Research"));
    }
    
    @Test
    void testCodeReview() {
        String code = """
            public class UserService {
                public User getUser(Long id) {
                    return userRepository.findById(id).orElse(null);
                }
            }
            """;
        
        String review = orchestrationService.reviewCode(code);
        
        assertNotNull(review);
        assertTrue(review.contains("Analysis"));
    }
}
```

## 运行项目

```bash
# 编译
mvn clean compile

# 运行测试
mvn test

# 打包
mvn clean package -DskipTests

# 启动应用
java -jar ai-agents-bootstrap/target/ai-agents-bootstrap-1.0.0-SNAPSHOT.jar

# 测试 API
curl -X POST http://localhost:8080/api/v1/agents/execute \
  -H "Content-Type: application/json" \
  -d '{"task": "Research the latest AI frameworks"}'
```

## 扩展建议

1. **添加新的 Agent 类型**：根据业务需求创建更多 specialized agents
2. **集成外部工具**：添加搜索引擎、代码分析器等工具
3. **实现持久化**：将 Agent 状态和消息历史存储到数据库
4. **添加监控**：集成 Micrometer 进行性能监控
5. **实现容错**：添加重试、熔断等机制
