package com.scaffold4j.generator;

import com.scaffold4j.model.ProjectConfig;
import com.scaffold4j.util.FileUtils;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Orchestrates the entire project generation process.
 * Delegates to specialized generators for each artifact type.
 */
public class ProjectGenerator {

    private final ProjectConfig config;
    private final Path projectDir;

    private final PomGenerator pomGenerator;
    private final ModuleGenerator moduleGenerator;
    private final ConfigGenerator configGenerator;
    private final DockerGenerator dockerGenerator;
    private final GitignoreGenerator gitignoreGenerator;

    public ProjectGenerator(ProjectConfig config) {
        this.config = config;
        this.projectDir = Path.of(config.outputDir()).resolve(config.effectiveArtifactId());

        this.pomGenerator = new PomGenerator(config);
        this.moduleGenerator = new ModuleGenerator(config);
        this.configGenerator = new ConfigGenerator(config);
        this.dockerGenerator = new DockerGenerator(config);
        this.gitignoreGenerator = new GitignoreGenerator(config);
    }

    public void generate() throws IOException {
        // 1. Root project files
        FileUtils.writeFile(projectDir.resolve("pom.xml"), pomGenerator.generateRootPom());
        FileUtils.writeFile(projectDir.resolve(".gitignore"), gitignoreGenerator.generate());
        FileUtils.writeFile(projectDir.resolve(".editorconfig"), gitignoreGenerator.generateEditorConfig());
        FileUtils.writeFile(projectDir.resolve("README.md"), generateReadme());

        // 2. Common module
        generateCommonModule();

        // 3. Domain module
        generateDomainModule();

        // 4. Infra module
        generateInfraModule();

        // 5. App module
        generateAppModule();

        // 6. API module
        generateApiModule();

        // 7. Bootstrap module
        generateBootstrapModule();

        // 8. Docker
        generateDockerFiles();

        // 9. Maven wrapper placeholder
        FileUtils.createDirectory(projectDir.resolve(".mvn"));
        FileUtils.writeFile(projectDir.resolve(".mvn/maven.config"),
                "-Dmaven.compiler.source=" + config.javaVersion() + "\n"
                + "-Dmaven.compiler.target=" + config.javaVersion() + "\n");
    }

    private void generateCommonModule() throws IOException {
        String module = config.moduleName("common");
        Path dir = projectDir.resolve(module);
        FileUtils.createDirectory(dir);

        FileUtils.writeFile(dir.resolve("pom.xml"), pomGenerator.generateModulePom(module, "common",
                null));

        String pkg = config.basePackage();
        Path src = dir.resolve("src/main/java").resolve(config.packagePath()).resolve("common");

        FileUtils.writeFile(src.resolve("constant/ErrorCode.java"),
                moduleGenerator.generateErrorCode(pkg));
        FileUtils.writeFile(src.resolve("constant/CommonConstant.java"),
                moduleGenerator.generateCommonConstant(pkg));
        FileUtils.writeFile(src.resolve("exception/BaseException.java"),
                moduleGenerator.generateBaseException(pkg));
        FileUtils.writeFile(src.resolve("exception/AIException.java"),
                moduleGenerator.generateAIException(pkg));
        FileUtils.writeFile(src.resolve("exception/ErrorResponse.java"),
                moduleGenerator.generateErrorResponse(pkg));
        FileUtils.writeFile(src.resolve("result/Result.java"),
                moduleGenerator.generateResult(pkg));
        FileUtils.writeFile(src.resolve("result/PageResult.java"),
                moduleGenerator.generatePageResult(pkg));
        FileUtils.writeFile(src.resolve("util/JsonUtils.java"),
                moduleGenerator.generateJsonUtils(pkg));
    }

    private void generateDomainModule() throws IOException {
        String module = config.moduleName("domain");
        Path dir = projectDir.resolve(module);
        FileUtils.createDirectory(dir);

        FileUtils.writeFile(dir.resolve("pom.xml"), pomGenerator.generateModulePom(module, "domain",
                config.moduleName("common")));

        String pkg = config.basePackage();
        Path src = dir.resolve("src/main/java").resolve(config.packagePath()).resolve("domain");

        FileUtils.writeFile(src.resolve("model/ChatMessage.java"),
                moduleGenerator.generateChatMessage(pkg));
        FileUtils.writeFile(src.resolve("model/Conversation.java"),
                moduleGenerator.generateConversation(pkg));
        FileUtils.writeFile(src.resolve("dto/ChatRequest.java"),
                moduleGenerator.generateChatRequest(pkg));
        FileUtils.writeFile(src.resolve("dto/ChatResponse.java"),
                moduleGenerator.generateChatResponse(pkg));
        FileUtils.writeFile(src.resolve("dto/StreamData.java"),
                moduleGenerator.generateStreamData(pkg));
        FileUtils.writeFile(src.resolve("enums/MessageRole.java"),
                moduleGenerator.generateMessageRole(pkg));
        FileUtils.writeFile(src.resolve("enums/LLMProviderType.java"),
                moduleGenerator.generateLLMProviderType(pkg));

        // Database entities
        if (config.hasDatabase()) {
            FileUtils.writeFile(src.resolve("enums/UserStatus.java"),
                    moduleGenerator.generateUserStatus(pkg));
            FileUtils.writeFile(src.resolve("entity/User.java"),
                    moduleGenerator.generateUserEntity(pkg));
        }

        // MQ domain models
        if (config.hasMq()) {
            FileUtils.writeFile(src.resolve("mq/MqMessage.java"),
                    moduleGenerator.generateMqMessage(pkg));
            FileUtils.writeFile(src.resolve("mq/MqAIRequest.java"),
                    moduleGenerator.generateMqAIRequest(pkg));
            FileUtils.writeFile(src.resolve("mq/MqAIResponse.java"),
                    moduleGenerator.generateMqAIResponse(pkg));
        }
    }

    private void generateInfraModule() throws IOException {
        String module = config.moduleName("infra");
        Path dir = projectDir.resolve(module);
        FileUtils.createDirectory(dir);

        FileUtils.writeFile(dir.resolve("pom.xml"), pomGenerator.generateModulePom(module, "infra",
                config.moduleName("domain")));

        String pkg = config.basePackage();
        Path src = dir.resolve("src/main/java").resolve(config.packagePath()).resolve("infra");

        FileUtils.writeFile(src.resolve("config/AppConfig.java"),
                moduleGenerator.generateAppConfig(pkg));
        FileUtils.writeFile(src.resolve("config/LLMProviderConfig.java"),
                moduleGenerator.generateLLMProviderConfig(pkg));
        FileUtils.writeFile(src.resolve("config/VectorStoreConfig.java"),
                moduleGenerator.generateVectorStoreConfig(pkg));
        FileUtils.writeFile(src.resolve("llm/LLMProviderAdapter.java"),
                moduleGenerator.generateLLMProviderAdapter(pkg));
        FileUtils.writeFile(src.resolve("llm/HttpLLMProviderAdapterSupport.java"),
                moduleGenerator.generateHttpLLMProviderAdapterSupport(pkg));
        FileUtils.writeFile(src.resolve("llm/LLMProviderFactory.java"),
                moduleGenerator.generateLLMProviderFactory(pkg));

        for (var provider : config.llmProviders()) {
            FileUtils.writeFile(
                    src.resolve("llm/" + providerAdapterClassName(provider.id()) + ".java"),
                    moduleGenerator.generateProviderAdapter(pkg, provider));
        }

        if (config.hasFeature(com.scaffold4j.model.Feature.MEMORY)) {
            FileUtils.writeFile(src.resolve("memory/ChatMemoryStore.java"),
                    moduleGenerator.generateChatMemoryStore(pkg));
            FileUtils.writeFile(src.resolve("memory/ConversationRepository.java"),
                    moduleGenerator.generateConversationRepository(pkg));
        }

        if (config.hasFeature(com.scaffold4j.model.Feature.RAG)) {
            FileUtils.writeFile(src.resolve("rag/DocumentLoader.java"),
                    moduleGenerator.generateDocumentLoader(pkg));
            FileUtils.writeFile(src.resolve("rag/TextSplitter.java"),
                    moduleGenerator.generateTextSplitter(pkg));
            FileUtils.writeFile(src.resolve("rag/EmbeddingService.java"),
                    moduleGenerator.generateEmbeddingService(pkg));
        }

        if (config.hasFeature(com.scaffold4j.model.Feature.WEBSOCKET)) {
            FileUtils.writeFile(src.resolve("config/WebSocketConfig.java"),
                    moduleGenerator.generateWebSocketConfig(pkg));
        }

        if (config.hasNacosDiscovery()) {
            FileUtils.writeFile(src.resolve("config/NacosDiscoveryConfig.java"),
                    moduleGenerator.generateNacosDiscoveryConfig(pkg));
        }
        if (config.hasNacosConfig()) {
            FileUtils.writeFile(src.resolve("config/NacosConfigRefresh.java"),
                    moduleGenerator.generateNacosConfigRefresh(pkg));
        }

        // Database infrastructure
        if (config.hasDatabase()) {
            FileUtils.writeFile(src.resolve("config/DataSourceConfig.java"),
                    moduleGenerator.generateDataSourceConfig(pkg));
            if (config.usesMyBatisPlus()) {
                FileUtils.writeFile(src.resolve("config/MybatisPlusConfig.java"),
                        moduleGenerator.generateMybatisPlusConfig(pkg));
                FileUtils.writeFile(src.resolve("mapper/UserMapper.java"),
                        moduleGenerator.generateUserMapper(pkg));
            }
            if (config.usesJpa()) {
                FileUtils.writeFile(src.resolve("config/JpaConfig.java"),
                        moduleGenerator.generateJpaConfig(pkg));
                FileUtils.writeFile(src.resolve("repository/UserRepository.java"),
                        moduleGenerator.generateUserRepository(pkg));
            }
        }

        // Cache infrastructure
        if (config.hasRedisCache()) {
            FileUtils.writeFile(src.resolve("config/RedisCacheConfig.java"),
                    moduleGenerator.generateRedisCacheConfig(pkg));
        }
        if (config.hasCaffeineCache()) {
            FileUtils.writeFile(src.resolve("config/CaffeineCacheConfig.java"),
                    moduleGenerator.generateCaffeineCacheConfig(pkg));
        }

        // MQ infrastructure
        if (config.hasMq()) {
            FileUtils.writeFile(src.resolve("mq/MqConfig.java"),
                    moduleGenerator.generateMqConfig(pkg));
            FileUtils.writeFile(src.resolve("mq/MqMessageProducer.java"),
                    moduleGenerator.generateMqMessageProducer(pkg));
            FileUtils.writeFile(src.resolve("mq/MqMessageListener.java"),
                    moduleGenerator.generateMqMessageListener(pkg));
        }
    }

    private void generateAppModule() throws IOException {
        String module = config.moduleName("app");
        Path dir = projectDir.resolve(module);
        FileUtils.createDirectory(dir);

        FileUtils.writeFile(dir.resolve("pom.xml"), pomGenerator.generateModulePom(module, "app",
                config.moduleName("infra")));

        String pkg = config.basePackage();
        Path src = dir.resolve("src/main/java").resolve(config.packagePath()).resolve("app");

        FileUtils.writeFile(src.resolve("service/ChatService.java"),
                moduleGenerator.generateChatService(pkg));
        FileUtils.writeFile(src.resolve("service/AgentService.java"),
                moduleGenerator.generateAgentService(pkg));
        FileUtils.writeFile(src.resolve("tool/WeatherTool.java"),
                moduleGenerator.generateWeatherTool(pkg));
        FileUtils.writeFile(src.resolve("tool/SearchTool.java"),
                moduleGenerator.generateSearchTool(pkg));
        FileUtils.writeFile(src.resolve("prompt/PromptTemplate.java"),
                moduleGenerator.generatePromptTemplate(pkg));

        // Prompt template files
        Path promptDir = src.resolve("prompt/templates");
        FileUtils.createDirectory(promptDir);
        FileUtils.writeFile(promptDir.resolve("system-prompt.st"),
                "You are a helpful AI assistant.\n");
        FileUtils.writeFile(promptDir.resolve("rag-prompt.st"),
                "Answer the question based on the following context:\n\nContext:\n{{context}}\n\nQuestion: {{question}}\n");

        if (config.hasFeature(com.scaffold4j.model.Feature.RAG)) {
            FileUtils.writeFile(src.resolve("service/RagService.java"),
                    moduleGenerator.generateRagService(pkg));
            FileUtils.writeFile(src.resolve("rag/IngestionPipeline.java"),
                    moduleGenerator.generateIngestionPipeline(pkg));
            FileUtils.writeFile(src.resolve("rag/RetrievalPipeline.java"),
                    moduleGenerator.generateRetrievalPipeline(pkg));
            FileUtils.writeFile(src.resolve("rag/RetrievalAugmentor.java"),
                    moduleGenerator.generateRetrievalAugmentor(pkg));
        }

        if (config.hasFeature(com.scaffold4j.model.Feature.SSE)) {
            FileUtils.writeFile(src.resolve("service/StreamService.java"),
                    moduleGenerator.generateStreamService(pkg));
        }

        if (config.usesLangChain4j()) {
            FileUtils.writeFile(src.resolve("agent/ReactAgent.java"),
                    moduleGenerator.generateReactAgent(pkg));
            FileUtils.writeFile(src.resolve("agent/workflow/ChatWorkflow.java"),
                    moduleGenerator.generateChatWorkflow(pkg));
            FileUtils.writeFile(src.resolve("agent/workflow/StateGraphConfig.java"),
                    moduleGenerator.generateStateGraphConfig(pkg));
            FileUtils.writeFile(src.resolve("agent/AIAgentService.java"),
                    moduleGenerator.generateAIAgentService(pkg));
            FileUtils.writeFile(src.resolve("agent/workflow/LangGraphWorkflow.java"),
                    moduleGenerator.generateLangGraphWorkflow(pkg));
        }

        // Database services
        if (config.hasDatabase()) {
            FileUtils.writeFile(src.resolve("service/UserService.java"),
                    moduleGenerator.generateUserService(pkg));
        }

        // Cache services
        if (config.hasCache()) {
            FileUtils.writeFile(src.resolve("service/CacheService.java"),
                    moduleGenerator.generateCacheService(pkg));
        }

        // MQ processing service
        if (config.hasMq()) {
            FileUtils.writeFile(src.resolve("mq/MqAIProcessingService.java"),
                    moduleGenerator.generateMqAIProcessingService(pkg));
        }

        FileUtils.writeFile(src.resolve("agent/AgentOrchestrator.java"),
                moduleGenerator.generateAgentOrchestrator(pkg));
    }

    private void generateApiModule() throws IOException {
        String module = config.moduleName("api");
        Path dir = projectDir.resolve(module);
        FileUtils.createDirectory(dir);

        FileUtils.writeFile(dir.resolve("pom.xml"), pomGenerator.generateModulePom(module, "api",
                config.moduleName("app")));

        String pkg = config.basePackage();
        Path src = dir.resolve("src/main/java").resolve(config.packagePath()).resolve("api");

        FileUtils.writeFile(src.resolve("rest/ChatController.java"),
                moduleGenerator.generateChatController(pkg));
        FileUtils.writeFile(src.resolve("rest/AgentController.java"),
                moduleGenerator.generateAgentController(pkg));

        if (config.hasFeature(com.scaffold4j.model.Feature.SSE)) {
            FileUtils.writeFile(src.resolve("rest/SseController.java"),
                    moduleGenerator.generateSseController(pkg));
        }

        if (config.hasProtocol(com.scaffold4j.model.Protocol.MCP)) {
            FileUtils.writeFile(src.resolve("mcp/McpServerConfig.java"),
                    moduleGenerator.generateMcpServerConfig(pkg));
            FileUtils.writeFile(src.resolve("mcp/tool/WeatherMcpTool.java"),
                    moduleGenerator.generateWeatherMcpTool(pkg));
            FileUtils.writeFile(src.resolve("mcp/tool/SearchMcpTool.java"),
                    moduleGenerator.generateSearchMcpTool(pkg));
            FileUtils.writeFile(src.resolve("mcp/resource/McpResourceProvider.java"),
                    moduleGenerator.generateMcpResourceProvider(pkg));
        }

        if (config.hasProtocol(com.scaffold4j.model.Protocol.A2A)) {
            FileUtils.writeFile(src.resolve("a2a/A2AAgentCard.java"),
                    moduleGenerator.generateA2AAgentCard(pkg));
            FileUtils.writeFile(src.resolve("a2a/A2ATaskHandler.java"),
                    moduleGenerator.generateA2ATaskHandler(pkg));
            FileUtils.writeFile(src.resolve("a2a/A2AServerConfig.java"),
                    moduleGenerator.generateA2AServerConfig(pkg));
        }

        if (config.hasProtocol(com.scaffold4j.model.Protocol.ACP)) {
            FileUtils.writeFile(src.resolve("acp/AcpAgentEndpoint.java"),
                    moduleGenerator.generateAcpAgentEndpoint(pkg));
            FileUtils.writeFile(src.resolve("acp/AcpSessionManager.java"),
                    moduleGenerator.generateAcpSessionManager(pkg));
        }

        if (config.hasFeature(com.scaffold4j.model.Feature.WEBSOCKET)) {
            FileUtils.writeFile(src.resolve("ws/ChatWebSocketHandler.java"),
                    moduleGenerator.generateChatWebSocketHandler(pkg));
        }

        // Database REST API
        if (config.hasDatabase()) {
            FileUtils.writeFile(src.resolve("rest/UserController.java"),
                    moduleGenerator.generateUserController(pkg));
        }

        // Health check endpoint (always generate)
        FileUtils.writeFile(src.resolve("rest/HealthController.java"),
                moduleGenerator.generateHealthController(pkg));
    }

    private void generateBootstrapModule() throws IOException {
        String module = config.moduleName("bootstrap");
        Path dir = projectDir.resolve(module);
        FileUtils.createDirectory(dir);

        FileUtils.writeFile(dir.resolve("pom.xml"), pomGenerator.generateModulePom(module, "bootstrap",
                config.moduleName("api")));

        String pkg = config.basePackage();
        Path src = dir.resolve("src/main/java").resolve(config.packagePath());
        Path res = dir.resolve("src/main/resources");

        FileUtils.writeFile(src.resolve("Application.java"),
                moduleGenerator.generateApplication(pkg));

        FileUtils.writeFile(res.resolve("application.yml"), configGenerator.generateMainConfig());
        FileUtils.writeFile(res.resolve("application-dev.yml"), configGenerator.generateDevConfig());
        FileUtils.writeFile(res.resolve("application-prod.yml"), configGenerator.generateProdConfig());
        FileUtils.writeFile(res.resolve("logback-spring.xml"), configGenerator.generateLogbackConfig());

        if (config.hasNacosConfig()) {
            FileUtils.writeFile(res.resolve("bootstrap.yml"), configGenerator.generateBootstrapConfig());
        }
    }

    private void generateDockerFiles() throws IOException {
        Path dockerDir = projectDir.resolve("docker");
        FileUtils.createDirectory(dockerDir);
        FileUtils.writeFile(dockerDir.resolve("Dockerfile"), dockerGenerator.generateDockerfile());
        FileUtils.writeFile(dockerDir.resolve("docker-compose.yml"), dockerGenerator.generateDockerCompose());
    }

    private String generateReadme() {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(config.effectiveArtifactId()).append("\n\n");
        sb.append("AI Application generated by [scaffold4j](https://github.com/scaffold4j).\n\n");
        sb.append("## Tech Stack\n\n");
        sb.append("- Java ").append(config.javaVersion()).append("\n");
        sb.append("- Spring Boot ").append(config.springBootVersion()).append("\n");
        sb.append("- AI Framework: ").append(config.aiFramework().displayName()).append("\n");
        sb.append("- LLM Providers: ").append(config.llmProviders().stream()
                .map(p -> p.displayName()).toList()).append("\n");
        sb.append("- Protocols: ").append(config.protocols().stream()
                .map(p -> p.id()).toList()).append("\n");
        if (!config.features().isEmpty()) {
            sb.append("- Features: ").append(config.features().stream()
                    .map(f -> f.id()).toList()).append("\n");
        }
        if (config.hasMq()) {
            sb.append("- Message Queue: ").append(config.mqType().displayName()).append("\n");
        }
        sb.append("\n## Quick Start\n\n");
        sb.append("```bash\n");
        sb.append("# Set environment variables for your LLM providers\n");
        for (var p : config.llmProviders()) {
            if (p.envVar() != null) {
                sb.append("export ").append(p.envVar()).append("=<your-api-key>\n");
            }
        }
        sb.append("\n# Run the application\n");
        sb.append("./mvnw -pl ").append(config.moduleName("bootstrap"))
                .append(" spring-boot:run\n");
        sb.append("```\n\n");
        sb.append("## Project Structure\n\n");
        sb.append("See the architecture documentation for details.\n");
        return sb.toString();
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

        private static String providerAdapterClassName(String id) {
                if (id == null || id.isBlank()) return "ProviderAdapter";
                StringBuilder sb = new StringBuilder();
                for (String part : id.split("[-_]+")) {
                        if (part.isEmpty()) continue;
                        sb.append(Character.toUpperCase(part.charAt(0)));
                        if (part.length() > 1) sb.append(part.substring(1));
                }
                return sb.append("Adapter").toString();
        }
}
