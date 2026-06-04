# AgentScope 集成实现计划

## 概述

本文档提供在 scaffold4j 中正式集成 AgentScope Java 的实现计划。

## 需要修改的文件

### 1. Model 层修改

#### 1.1 AIFramework.java

```java
// 新增 AgentScope 枚举值
AGENTSCOPE("agentscope", "AgentScope", "Multi-agent framework from ModelScope"),
AGENTSCOPE_SPRING_AI("agentscope-spring-ai", "AgentScope + Spring AI", 
    "AgentScope multi-agent with Spring Boot integration"),
```

#### 1.2 Feature.java

```java
// 新增多智能体相关特性
MULTI_AGENT("multi-agent", "Multi-Agent", "AgentScope multi-agent collaboration"),
AGENT_MESSAGE("agent-message", "Agent Message", "AgentScope agent communication protocol"),
AGENT_TOOLS("agent-tools", "Agent Tools", "AgentScope tool integration"),
```

### 2. Generator 层修改

#### 2.1 ModuleGenerator.java

新增方法：

```java
// 生成 AgentScope Agent 基类
public String generateAgentBaseClass(String pkg) {
    return """
        package %s.agents;
        
        import com.agentscope.agent.Agent;
        import com.agentscope.agent.AgentConfig;
        import com.agentscope.message.Message;
        import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;
        
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
            public abstract Message onMessage(Message message);
        }
        """.formatted(pkg);
}

// 生成协调器 Agent
public String generateCoordinatorAgent(String pkg) {
    return """
        package %s.agents.coordinator;
        
        import com.agentscope.agent.Agent;
        import com.agentscope.agent.AgentConfig;
        import com.agentscope.message.Message;
        import com.agentscope.message.MessageType;
        import com.example.agents.BaseAgent;
        import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;
        
        import java.util.List;
        import java.util.concurrent.CompletableFuture;
        
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
                // 任务分解和分配逻辑
                return Message.builder()
                    .type(MessageType.RESPONSE)
                    .sender(getName())
                    .recipient(message.getSender())
                    .content("Task processed by coordinator")
                    .build();
            }
        }
        """.formatted(pkg);
}

// 生成研究员 Agent
public String generateResearcherAgent(String pkg) {
    return """
        package %s.agents.worker;
        
        import com.agentscope.agent.AgentConfig;
        import com.agentscope.message.Message;
        import com.example.agents.BaseAgent;
        import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;
        
        public class ResearcherAgent extends BaseAgent {
            
            private static final Logger log = LoggerFactory.getLogger(ResearcherAgent.class);
            
            public ResearcherAgent(AgentConfig config) {
                super("researcher", config);
            }
            
            @Override
            public Message onMessage(Message message) {
                log.info("Researcher processing: {}", message.getContent());
                // 研究和搜索逻辑
                return Message.builder()
                    .type(MessageType.RESPONSE)
                    .sender(getName())
                    .recipient(message.getSender())
                    .content("Research results")
                    .build();
            }
        }
        """.formatted(pkg);
}

// 生成工具类
public String generateToolClass(String pkg) {
    return """
        package %s.agents.tools;
        
        import com.agentscope.tool.Tool;
        import com.agentscope.tool.ToolParameter;
        import com.agentscope.tool.ToolResult;
        import java.util.List;
        
        public class CustomTool implements Tool {
            
            @Override
            public String getName() {
                return "custom-tool";
            }
            
            @Override
            public String getDescription() {
                return "Custom tool for AgentScope";
            }
            
            @Override
            public ToolResult execute(@ToolParameter("input") String input) {
                return ToolResult.success("Processed: " + input);
            }
            
            @Override
            public List<ToolParameter> getParameters() {
                return List.of(
                    new ToolParameter("input", "string", "Input parameter", true)
                );
            }
        }
        """.formatted(pkg);
}

// 生成 Agent 配置类
public String generateAgentScopeConfig(String pkg) {
    return """
        package %s.agents.config;
        
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
        """.formatted(pkg);
}
```

#### 2.2 ProjectGenerator.java

修改 `generate()` 方法，添加 AgentScope 模块生成：

```java
// 在 generate 方法中添加
if (config.usesAgentscope()) {
    generateAgentsModule(outputDir);
}

// 新增方法
private void generateAgentsModule(Path outputDir) throws IOException {
    Path agentsDir = outputDir.resolve(config.effectiveArtifactId() + "-agents");
    Files.createDirectories(agentsDir);
    
    // 生成 Agent 基类
    writeFile(agentsDir.resolve("src/main/java/" + pkgPath("agents") + "/BaseAgent.java"),
        moduleGenerator.generateAgentBaseClass(config.basePackage()));
    
    // 生成协调器 Agent
    writeFile(agentsDir.resolve("src/main/java/" + pkgPath("agents/coordinator") + "/TaskCoordinatorAgent.java"),
        moduleGenerator.generateCoordinatorAgent(config.basePackage()));
    
    // 生成研究员 Agent
    writeFile(agentsDir.resolve("src/main/java/" + pkgPath("agents/worker") + "/ResearcherAgent.java"),
        moduleGenerator.generateResearcherAgent(config.basePackage()));
    
    // 生成工具类
    writeFile(agentsDir.resolve("src/main/java/" + pkgPath("agents/tools") + "/CustomTool.java"),
        moduleGenerator.generateToolClass(config.basePackage()));
    
    // 生成配置类
    writeFile(agentsDir.resolve("src/main/java/" + pkgPath("agents/config") + "/AgentScopeConfig.java"),
        moduleGenerator.generateAgentScopeConfig(config.basePackage()));
    
    // 生成 POM 文件
    writeFile(agentsDir.resolve("pom.xml"),
        pomGenerator.generateAgentsPom(config));
}
```

### 3. POM 生成器修改

#### 3.1 PomGenerator.java

新增方法：

```java
// 生成 agents 模块的 POM
public String generateAgentsPom(ProjectConfig config) {
    return """
        <?xml version="1.0" encoding="UTF-8"?>
        <project xmlns="http://maven.apache.org/POM/4.0.0"
                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
            <modelVersion>4.0.0</modelVersion>
            
            <parent>
                <groupId>%s</groupId>
                <artifactId>%s</artifactId>
                <version>%s</version>
            </parent>
            
            <artifactId>%s-agents</artifactId>
            <name>%s-agents</name>
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
                    <groupId>%s</groupId>
                    <artifactId>%s-common</artifactId>
                    <version>${project.version}</version>
                </dependency>
                <dependency>
                    <groupId>%s</groupId>
                    <artifactId>%s-domain</artifactId>
                    <version>${project.version}</version>
                </dependency>
            </dependencies>
        </project>
        """.formatted(
            config.groupId(),
            config.effectiveArtifactId(),
            config.version(),
            config.effectiveArtifactId(),
            config.effectiveArtifactId(),
            config.groupId(),
            config.effectiveArtifactId(),
            config.groupId(),
            config.effectiveArtifactId()
        );
}

// 修改根 POM，添加 agents 模块
public String generateRootPom(ProjectConfig config) {
    StringBuilder modules = new StringBuilder();
    modules.append("            <module>").append(config.effectiveArtifactId()).append("-common</module>\n");
    modules.append("            <module>").append(config.effectiveArtifactId()).append("-domain</module>\n");
    modules.append("            <module>").append(config.effectiveArtifactId()).append("-infra</module>\n");
    modules.append("            <module>").append(config.effectiveArtifactId()).append("-app</module>\n");
    
    if (config.usesAgentscope()) {
        modules.append("            <module>").append(config.effectiveArtifactId()).append("-agents</module>\n");
    }
    
    modules.append("            <module>").append(config.effectiveArtifactId()).append("-api</module>\n");
    modules.append("            <module>").append(config.effectiveArtifactId()).append("-bootstrap</module>\n");
    
    // ... 其余 POM 内容
}
```

### 4. ProjectConfig 修改

#### 4.1 添加 AgentScope 相关方法

```java
// 检查是否使用 AgentScope
public boolean usesAgentscope() {
    return aiFramework == AIFramework.AGENTSCOPE 
        || aiFramework == AIFramework.AGENTSCOPE_SPRING_AI;
}

// 检查是否使用多智能体特性
public boolean hasMultiAgentFeature() {
    return features.contains(Feature.MULTI_AGENT);
}

// 检查是否使用 Agent 工具
public boolean hasAgentToolsFeature() {
    return features.contains(Feature.AGENT_TOOLS);
}
```

## 实现步骤

### Phase 1: 基础框架支持

1. [ ] 修改 `AIFramework.java`，添加 AgentScope 枚举值
2. [ ] 修改 `Feature.java`，添加多智能体相关特性
3. [ ] 修改 `ProjectConfig.java`，添加 AgentScope 相关方法
4. [ ] 修改 `PomGenerator.java`，支持生成 AgentScope 依赖
5. [ ] 修改 `ModuleGenerator.java`，生成 AgentScope 基础代码
6. [ ] 修改 `ProjectGenerator.java`，集成 AgentScope 模块生成

### Phase 2: 测试与验证

1. [ ] 编写单元测试，验证代码生成正确性
2. [ ] 手动测试，生成示例项目并验证功能
3. [ ] 文档更新，更新 CLAUDE.md 和 README

### Phase 3: 扩展功能

1. [ ] 添加更多 Agent 类型（如 CoderAgent、ReviewerAgent）
2. [ ] 集成 Spring AI，实现 LLM 驱动的 Agent
3. [ ] 添加配置文件模板
4. [ ] 提供示例项目和教程

## 依赖管理

### 根 POM 添加依赖管理

```xml
<!-- AgentScope -->
<dependency>
    <groupId>com.agentscope</groupId>
    <artifactId>agentscope-core</artifactId>
    <version>2.0.0</version>
</dependency>
<dependency>
    <groupId>com.agentscope</groupId>
    <artifactId>agentscope-tools</artifactId>
    <version>2.0.0</version>
</dependency>
<dependency>
    <groupId>com.agentscope</groupId>
    <artifactId>agentscope-spring-boot-starter</artifactId>
    <version>2.0.0</version>
</dependency>
```

## 风险与注意事项

1. **依赖冲突**：AgentScope 可能与其他依赖有冲突，需要仔细管理版本
2. **学习曲线**：多智能体概念需要一定学习成本
3. **性能考虑**：异步模型需要合理配置线程池大小
4. **调试难度**：多智能体系统的调试相对复杂

## 后续计划

1. **社区反馈**：收集用户反馈，优化集成体验
2. **功能扩展**：支持更多 AgentScope 高级功能
3. **文档完善**：提供详细的使用指南和最佳实践
4. **示例项目**：创建完整的示例项目，展示各种使用场景
