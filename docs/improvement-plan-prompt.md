# scaffold4j 改进计划生成 Prompt

```text
你是一个资深 Java / Spring Boot / Spring AI / LangChain4j 架构师，同时也是代码生成工具的重构专家。

当前项目是 scaffold4j，一个用于生成 Java AI 应用项目的 CLI 脚手架。它本身不使用 Spring Boot 运行，而是生成 Spring Boot AI 应用代码。请根据 docs/architecture-analysis.md 中的分析，制定一个可执行的改进计划，并按优先级拆解为阶段性任务。

项目背景：
- CLI 入口：Scaffold4jMain
- 核心生成流程：
  1. GenerateCommand 解析命令行参数
  2. ProjectConfig 保存配置并校验
  3. ProjectGenerator 编排生成流程
  4. PomGenerator / ModuleGenerator / ConfigGenerator / DockerGenerator / GitignoreGenerator 输出具体文件
- 当前生成项目结构：
  common <- domain <- infra <- app <- api <- bootstrap
- 主要问题包括：
  - LLM Adapter 为空实现，生成项目无法实际调用模型
  - application.yml 缩进和结构错误
  - LLMProviderConfig 没有注册 Adapter
  - AgentOrchestrator 存在编译错误
  - Tool 没有使用 Spring AI / LangChain4j 的框架注解
  - Agent 实现过于简化
  - 缺少测试代码生成
  - RAG 文件生成不完整
  - Memory 仅支持内存实现
  - 缺少全局异常处理器
  - 缺少可观测性
  - ModuleGenerator 过于庞大，维护困难

请输出一个详细的改进计划，要求如下：

1. 按优先级分阶段规划
   - 第一阶段：让生成项目可以编译并运行
   - 第二阶段：提升生成代码质量
   - 第三阶段：完善 AI 能力
   - 第四阶段：生产就绪与脚手架自身重构

2. 每个阶段必须包含：
   - 阶段目标
   - 要解决的问题
   - 涉及的源码文件
   - 具体修改点
   - 验收标准
   - 推荐测试方式

3. 第一阶段必须优先处理：
   - 修复 ConfigGenerator 生成的 application.yml 结构
   - 生成可运行的 LLM Adapter 实现骨架
   - 修复 LLMProviderConfig 自动注册 Adapter
   - 修复 AgentOrchestrator 编译错误
   - 生成 GlobalExceptionHandler

4. 第二阶段必须包含：
   - Tool 增加 Spring AI / LangChain4j 对应注解
   - AgentService 改为框架原生工具调用模式
   - Domain DTO / Entity 使用 Lombok
   - 生成基础测试代码
   - 生成 application-test.yml

5. 第三阶段必须包含：
   - 修复 RAG 相关文件生成条件
   - 生成完整 RAG 摄入与检索管道
   - Memory 支持 Redis / JDBC 持久化
   - Spring AI 使用 Advisor Chain
   - LangChain4j 使用 AiServices
   - 增加基础 Guardrails

6. 第四阶段必须包含：
   - Micrometer / Prometheus 可观测性
   - OpenTelemetry Tracing
   - 健康检查增强
   - OpenAPI 文档
   - 拆分 ModuleGenerator
   - 逐步迁移字符串模板到 Mustache

7. 输出格式要求：
   - 使用 Markdown
   - 先给总览表
   - 再按阶段展开
   - 每个任务使用可勾选 checklist
   - 每个任务尽量拆成可以单独提交的 commit 粒度
   - 对 P0 / P1 阻塞问题标记为“必须优先完成”
   - 最后给出推荐执行顺序和里程碑

8. 不要直接修改代码，只生成计划。
9. 计划要足够具体，后续可以直接交给代码代理逐项执行。
```
