# AgentScope 2.0 Java 集成指南

## 概述

[AgentScope](https://github.com/agentscope-ai/agentscope-java) 是阿里巴巴 ModelScope 团队开发的多智能体（Multi-Agent）框架，提供了 Java SDK 支持。本指南说明如何在 scaffold4j 生成的项目中集成 AgentScope。

## 特性支持

| 特性 | 说明 | 依赖 |
|------|------|------|
| **多智能体协作** | Agent 间协调与任务分配 | `agentscope-core` |
| **工具集成** | 自定义 Agent 工具 | `agentscope-tools` |
| **消息传递** | Agent 间通信协议 | `agentscope-core` |
| **流程编排** | 可视化工作流 | `agentscope-orchestration` |

## 依赖配置

### Maven 依赖

```xml
<!-- AgentScope Core -->
<dependency>
    <groupId>com.agentscope</groupId>
    <artifactId>agentscope-core</artifactId>
    <version>2.0.0</version>
</dependency>

<!-- AgentScope Tools (可选) -->
<dependency>
    <groupId>com.agentscope</groupId>
    <artifactId>agentscope-tools</artifactId>
    <version>2.0.0</version>
</dependency>

<!-- Spring Boot Starter (可选，与 Spring AI 集成) -->
<dependency>
    <groupId>com.agentscope</groupId>
    <artifactId>agentscope-spring-boot-starter</artifactId>
    <version>2.0.0</version>
</dependency>
```

### Gradle 依赖

```groovy
implementation 'com.agentscope:agentscope-core:2.0.0'
implementation 'com.agentscope:agentscope-tools:2.0.0'
```

## 项目结构

集成 AgentScope 后的生成项目结构：

```
<name>/
├── pom.xml
├── <name>-common/           # 通用工具类
├── <name>-domain/           # 领域模型
├── <name>-infra/            # 基础设施（LLM、向量存储等）
├── <name>-app/              # 业务逻辑（Agent、Service）
├── <name>-agents/           # ⭐ AgentScope Agent 定义
│   ├── coordinator/         # 协调器 Agent
│   ├── worker/              # 工作 Agent
│   ├── tools/               # Agent 工具
│   └── config/              # Agent 配置
├── <name>-api/              # REST/MCP/A2A 控制器
├── <name>-bootstrap/        # 启动类
└── docker/
```

## 示例代码

### 1. Agent 基类定义

```java
package com.example.agents;

import com.agentscope.agent.Agent;
import com.agentscope.agent.AgentConfig;
import com.agentscope.message.Message;
import com.agentscope.message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自定义 Agent 基类
 */
public abstract class BaseAgent implements Agent {
    
    protected final Logger log = LoggerFactory.getLogger(getClass());
    
    protected final String name;
    protected final AgentConfig config;
    
    public BaseAgent(String name, AgentConfig config) {
        this.name = name;
        this.config = config;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void onMessage(Message message) {
        log.info("Agent [{}] received message from [{}]: {}", 
            name, message.getSender(), message.getContent());
        
        try {
            Message response = processMessage(message);
            if (response != null) {
                sendResponse(response);
            }
        } catch (Exception e) {
            log.error("Error processing message in agent [{}]", name, e);
            sendErrorResponse(e);
        }
    }
    
    protected abstract Message processMessage(Message message);
    
    protected Message createResponse(String content, String recipient) {
        return Message.builder()
            .type(MessageType.RESPONSE)
            .sender(name)
            .recipient(recipient)
            .content(content)
            .build();
    }
    
    protected void sendResponse(Message response) {
        // 发送响应到消息总线
        log.debug("Agent [{}] sending response to [{}]", name, response.getRecipient());
    }
    
    protected void sendErrorResponse(Exception e) {
        log.error("Agent [{}] error: {}", name, e.getMessage());
    }
}
```

### 2. 协调器 Agent 示例

```java
package com.example.agents.coordinator;

import com.agentscope.agent.Agent;
import com.agentscope.agent.AgentConfig;
import com.agentscope.orchestration.Coordinator;
import com.agentscope.message.Message;
import com.example.agents.BaseAgent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 协调器 Agent - 负责任务分配和协调
 */
public class CoordinatorAgent extends BaseAgent {
    
    private final List<Agent> workerAgents;
    private final Coordinator coordinator;
    
    public CoordinatorAgent(AgentConfig config, List<Agent> workerAgents) {
        super("coordinator", config);
        this.workerAgents = workerAgents;
        this.coordinator = new Coordinator(workerAgents);
    }
    
    @Override
    protected Message processMessage(Message message) {
        log.info("Coordinator received task: {}", message.getContent());
        
        // 根据任务类型分发到合适的 worker agent
        String taskType = extractTaskType(message.getContent());
        
        return switch (taskType) {
            case "research" -> delegateToWorker("researcher", message);
            case "code" -> delegateToWorker("coder", message);
            case "review" -> delegateToWorker("reviewer", message);
            default -> broadcastToAll(message);
        };
    }
    
    private Message delegateToWorker(String workerName, Message message) {
        Agent worker = workerAgents.stream()
            .filter(w -> w.getName().equals(workerName))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown worker: " + workerName));
        
        return createResponse(message.getContent(), workerName);
    }
    
    private Message broadcastToAll(Message message) {
        // 广播消息给所有 worker
        workerAgents.forEach(worker -> {
            worker.onMessage(createResponse(message.getContent(), worker.getName()));
        });
        return createResponse("Task broadcasted to all workers", message.getSender());
    }
    
    private String extractTaskType(String content) {
        if (content.contains("research") || content.contains("search")) {
            return "research";
        } else if (content.contains("code") || content.contains("implement")) {
            return "code";
        } else if (content.contains("review") || content.contains("check")) {
            return "review";
        }
        return "general";
    }
}
```

### 3. 工作 Agent 示例（研究员）

```java
package com.example.agents.worker;

import com.agentscope.agent.AgentConfig;
import com.agentscope.message.Message;
import com.example.agents.BaseAgent;

/**
 * 研究员 Agent - 负责信息搜索和研究
 */
public class ResearcherAgent extends BaseAgent {
    
    public ResearcherAgent(AgentConfig config) {
        super("researcher", config);
    }
    
    @Override
    protected Message processMessage(Message message) {
        String query = message.getContent();
        log.info("Researcher processing query: {}", query);
        
        // 执行搜索和研究
        String result = performResearch(query);
        
        return createResponse(result, message.getSender());
    }
    
    private String performResearch(String query) {
        // 这里可以集成搜索引擎或知识库
        return "Research result for: " + query;
    }
}
```

### 4. 工具定义示例

```java
package com.example.agents.tools;

import com.agentscope.tool.Tool;
import com.agentscope.tool.ToolParameter;
import com.agentscope.tool.ToolResult;

/**
 * 自定义工具示例
 */
public class SearchTool implements Tool {
    
    @Override
    public String getName() {
        return "search";
    }
    
    @Override
    public String getDescription() {
        return "Search for information";
    }
    
    @Override
    public ToolResult execute(@ToolParameter("query") String query) {
        // 实现搜索逻辑
        String result = "Search results for: " + query;
        return ToolResult.success(result);
    }
    
    @Override
    public List<ToolParameter> getParameters() {
        return List.of(
            new ToolParameter("query", "string", "The search query", true)
        );
    }
}
```

### 5. Agent 配置类

```java
package com.example.agents.config;

import com.agentscope.agent.AgentConfig;
import com.agentscope.agent.AgentRegistry;
import com.agentscope.orchestration.Coordinator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AgentScopeConfig {
    
    @Bean
    public AgentRegistry agentRegistry() {
        return AgentRegistry.create();
    }
    
    @Bean
    public Coordinator agentCoordinator(AgentRegistry registry) {
        List<Agent> agents = registry.getAllAgents();
        return new Coordinator(agents);
    }
}
```

### 6. Agent 服务调用

```java
package com.example.service;

import com.agentscope.orchestration.Coordinator;
import com.agentscope.message.Message;
import com.agentscope.message.MessageType;
import org.springframework.stereotype.Service;

@Service
public class AgentOrchestrationService {
    
    private final Coordinator coordinator;
    
    public AgentOrchestrationService(Coordinator coordinator) {
        this.coordinator = coordinator;
    }
    
    public String executeTask(String taskDescription) {
        Message task = Message.builder()
            .type(MessageType.TASK)
            .sender("user")
            .content(taskDescription)
            .build();
        
        return coordinator.execute(task).join();
    }
}
```

### 7. REST API 控制器

```java
package com.example.api;

import com.example.service.AgentOrchestrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agents")
public class AgentController {
    
    private final AgentOrchestrationService orchestrationService;
    
    public AgentController(AgentOrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }
    
    @PostMapping("/execute")
    public ResponseEntity<String> executeTask(@RequestBody String task) {
        String result = orchestrationService.executeTask(task);
        return ResponseEntity.ok(result);
    }
}
```

## 配置文件示例

### application.yml

```yaml
agentscope:
  agents:
    coordinator:
      name: coordinator
      max-workers: 10
      timeout: 60
    worker:
      researcher:
        name: researcher
        capabilities:
          - search
          - analysis
      coder:
        name: coder
        capabilities:
          - code-generation
          - refactoring
      reviewer:
        name: reviewer
        capabilities:
          - code-review
          - quality-check
  
  orchestration:
    strategy: round-robin  # round-robin, random, or custom
    max-retries: 3
    timeout: 120
  
  logging:
    enabled: true
    level: INFO
```

## 使用场景示例

### 代码审查流程

```java
// 用户提交代码审查请求
String reviewResult = orchestrationService.executeTask(
    "Please review the code in src/main/java/com/example/UserService.java"
);

// 系统自动：
// 1. CoordinatorAgent 接收任务
// 2. 分发给 ResearcherAgent 分析代码
// 3. ResearcherAgent 搜索相关文档和最佳实践
// 4. ReviewerAgent 执行代码审查
// 5. 汇总结果返回给用户
```

## 与 Spring AI 集成

如果选择 `spring-ai` + `agentscope` 组合，可以实现：

```java
@Service
public class AiAgentService {
    
    private final ChatModel chatModel;
    private final Coordinator agentCoordinator;
    
    public String askWithAgents(String question) {
        // 使用 AgentScope 多智能体处理复杂问题
        Message task = Message.builder()
            .type(MessageType.TASK)
            .sender("user")
            .content(question)
            .build();
        
        return agentCoordinator.execute(task).join();
    }
}
```

## 注意事项

1. **依赖版本兼容性**：确保 AgentScope 与其他依赖版本兼容
2. **线程安全**：AgentScope 使用异步模型，注意线程安全
3. **错误处理**：Agent 间的错误需要正确传播
4. **性能调优**：根据负载调整 `max-workers` 和 `timeout` 配置

## 参考资源

- [AgentScope Java 官方文档](https://github.com/agentscope-ai/agentscope-java)
- [AgentScope 多智能体示例](https://github.com/agentscope-ai/agentscope-java/tree/main/examples)
- [Spring AI 官方文档](https://docs.spring.io/spring-ai/reference/)
