# AgentScope 集成说明

## 概述

本文档说明如何在 scaffold4j 中集成和使用 AgentScope Java 多智能体框架。

## 文件列表

| 文件 | 说明 |
|------|------|
| `AGENTSCOPE_INTEGRATION.md` | AgentScope 集成指南，包含特性说明、依赖配置和使用示例 |
| `AGENTSCOPE_GENERATION_EXAMPLE.md` | 代码生成示例，展示生成的项目结构和完整代码 |
| `AGENTSCOPE_IMPLEMENTATION_PLAN.md` | 实现计划，说明如何在 scaffold4j 中正式集成 AgentScope |
| `AGENTSCOPE_README.md` | 本文件，提供快速导航和使用说明 |

## 快速开始

### 1. 查看集成指南

首先阅读 `AGENTSCOPE_INTEGRATION.md`，了解：
- AgentScope 的特性
- 依赖配置
- 项目结构
- 示例代码

### 2. 查看生成示例

阅读 `AGENTSCOPE_GENERATION_EXAMPLE.md`，了解：
- 生成的项目结构
- 完整的代码示例
- 配置文件示例
- 测试示例

### 3. 查看实现计划

阅读 `AGENTSCOPE_IMPLEMENTATION_PLAN.md`，了解：
- 需要修改的文件
- 实现步骤
- 依赖管理
- 风险与注意事项

## 使用示例

### 生成 AgentScope 项目

```bash
# 生成包含 AgentScope 的项目
java -jar scaffold4j-cli/target/scaffold4j-cli-1.0.0-SNAPSHOT.jar generate \
  --name=ai-agents \
  --package=com.example.ai \
  --framework=agentscope \
  --protocols=rest,mcp \
  --features=memory,rag
```

### 运行生成的项目

```bash
# 进入项目目录
cd ai-agents

# 编译项目
mvn clean compile

# 运行测试
mvn test

# 启动应用
java -jar ai-agents-bootstrap/target/ai-agents-bootstrap-1.0.0-SNAPSHOT.jar

# 测试 API
curl -X POST http://localhost:8080/api/v1/agents/execute \
  -H "Content-Type: application/json" \
  -d '{"task": "Research the latest AI trends"}'
```

## 核心概念

### AgentScope 核心组件

1. **Agent（智能体）**：具有自主决策能力的实体
2. **Coordinator（协调器）**：负责任务分解和分配
3. **Worker（工作者）**：执行具体任务的 Agent
4. **Tool（工具）**：Agent 可以使用的外部能力
5. **Message（消息）**：Agent 间通信的载体

### 多智能体协作模式

1. **集中式协调**：由协调器统一分配任务
2. **分布式协作**：Agent 间直接通信协作
3. **层级式管理**：多层协调器管理不同层次的任务

## 技术栈

- **AgentScope Java**：多智能体框架
- **Spring Boot**：应用框架
- **Spring AI**：AI 集成（可选）
- **Java 17+**：运行环境

## 示例场景

### 1. 代码审查

```java
// 用户提交代码审查请求
String reviewResult = orchestrationService.reviewCode(code);

// 系统自动：
// 1. 协调器接收任务
// 2. 分发给研究员分析代码
// 3. 研究员搜索相关文档和最佳实践
// 4. 审查员执行代码审查
// 5. 汇总结果返回给用户
```

### 2. 代码生成

```java
// 用户描述需求
String code = orchestrationService.generateCode("Create a REST API for user management");

// 系统自动：
// 1. 协调器接收任务
// 2. 分发给编码员生成代码
// 3. 审查员检查代码质量
// 4. 返回优化后的代码
```

### 3. 研究分析

```java
// 用户提出研究问题
String result = orchestrationService.executeTask("Research the latest AI frameworks");

// 系统自动：
// 1. 协调器接收任务
// 2. 分发给研究员搜索信息
// 3. 研究员汇总分析结果
// 4. 返回研究报告
```

## 常见问题

### Q1: AgentScope 与 Spring AI 的区别？

- **AgentScope**：专注于多智能体协作和任务编排
- **Spring AI**：专注于 AI 模型集成和单次推理
- 可以结合使用：Spring AI 提供 LLM 能力，AgentScope 提供多智能体协作

### Q2: 如何扩展新的 Agent 类型？

1. 继承 `BaseAgent` 基类
2. 实现 `onMessage` 方法
3. 在 `AgentScopeConfig` 中注册

### Q3: 如何添加自定义工具？

1. 实现 `Tool` 接口
2. 定义工具名称、描述和参数
3. 在 Agent 中注册和使用

### Q4: 如何调试多智能体系统？

1. 启用详细日志：`agentscope.logging.level=DEBUG`
2. 使用 AgentScope 提供的可视化工具
3. 添加自定义监控指标

## 参考资源

- [AgentScope Java 官方文档](https://github.com/agentscope-ai/agentscope-java)
- [AgentScope 多智能体示例](https://github.com/agentscope-ai/agentscope-java/tree/main/examples)
- [Spring AI 官方文档](https://docs.spring.io/spring-ai/reference/)
- [scaffold4j 项目文档](https://github.com/your-repo/scaffold4j)

