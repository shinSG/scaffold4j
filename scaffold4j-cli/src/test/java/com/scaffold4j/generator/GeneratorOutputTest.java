package com.scaffold4j.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.scaffold4j.model.AIFramework;
import com.scaffold4j.model.CacheType;
import com.scaffold4j.model.Feature;
import com.scaffold4j.model.LLMProvider;
import com.scaffold4j.model.MqType;
import com.scaffold4j.model.ProjectConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratorOutputTest {

    @TempDir
    Path tempDir;

    @Test
    void applicationYamlHasSingleTopLevelSpringAndValidTopLevelBlocks() throws Exception {
        ProjectConfig config = new ProjectConfig()
                .name("demo-app")
                .basePackage("com.example.demo")
                .aiFramework(AIFramework.BOTH)
                .llmProviders(Set.of(LLMProvider.OPENAI, LLMProvider.OLLAMA))
                .cacheType(CacheType.REDIS)
                .nacosEnabled(true)
                .mqType(MqType.KAFKA)
                .addFeature(Feature.MEMORY)
                .addFeature(Feature.WEBSOCKET);

        String yaml = new ConfigGenerator(config).generateMainConfig();

        new ObjectMapper(new YAMLFactory()).readTree(yaml);
        assertEquals(1, countTopLevelKey(yaml, "spring"));
        assertEquals(1, countTopLevelKey(yaml, "scaffold4j"));
        assertEquals(1, countTopLevelKey(yaml, "mybatis-plus"));
        assertEquals(1, countTopLevelKey(yaml, "logging"));
        assertTrue(yaml.contains("\nspring:\n"));
        assertTrue(yaml.contains("\n  datasource:\n"));
        assertTrue(yaml.contains("\n  cache:\n"));
        assertTrue(yaml.contains("\n  cloud:\n    nacos:\n"));
        assertTrue(yaml.contains("\nmybatis-plus:\n"));
        assertFalse(section(yaml, "mybatis-plus:", "logging:").contains("\nspring:"));
        assertTrue(yaml.contains("\nscaffold4j:\n  ai:\n    default-provider:"));
        assertTrue(yaml.contains("\n  websocket:\n"));
        assertTrue(yaml.contains("\n  mq:\n"));
    }

    @Test
    void generatedProviderFactoryRegistersAdaptersAndFailsClearly() {
        ProjectConfig config = new ProjectConfig()
                .name("demo-app")
                .basePackage("com.example.demo")
                .llmProviders(Set.of(LLMProvider.OPENAI));
        String source = new ModuleGenerator(config).generateLLMProviderFactory("com.example.demo");

        assertTrue(source.contains("List<LLMProviderAdapter> adapters"));
        assertTrue(source.contains("register(LLMProviderType.fromId(adapter.providerName()), adapter)"));
        assertTrue(source.contains("No LLMProviderAdapter beans were found"));
        assertTrue(source.contains("Default LLM provider '"));
        assertFalse(source.contains("NoSuchElementException"));
    }

    @Test
    void generatedAdaptersUseFrameworkInvocationSkeletons() {
        ProjectConfig springAiConfig = new ProjectConfig()
                .name("demo-app")
                .basePackage("com.example.demo")
                .aiFramework(AIFramework.SPRING_AI);
        ModuleGenerator springAiGenerator = new ModuleGenerator(springAiConfig);
        String springAiSupport = springAiGenerator.generateSpringAiLLMProviderAdapterSupport("com.example.demo");
        String springAiAdapter = springAiGenerator.generateProviderAdapter("com.example.demo", LLMProvider.OPENAI);

        assertTrue(springAiSupport.contains("ChatClient"));
        assertTrue(springAiSupport.contains("chatClient.prompt()"));
        assertTrue(springAiSupport.contains(".stream()"));
        assertFalse(coreInvokeBlockThrowsUnsupported(springAiSupport));
        assertTrue(springAiAdapter.contains("extends SpringAiLLMProviderAdapterSupport"));

        ProjectConfig langChainConfig = new ProjectConfig()
                .name("demo-app")
                .basePackage("com.example.demo")
                .aiFramework(AIFramework.LANGCHAIN4J);
        ModuleGenerator langChainGenerator = new ModuleGenerator(langChainConfig);
        String langChainSupport = langChainGenerator.generateLangChain4jLLMProviderAdapterSupport("com.example.demo");
        String langChainAdapter = langChainGenerator.generateProviderAdapter("com.example.demo", LLMProvider.OPENAI);

        assertTrue(langChainSupport.contains("ChatLanguageModel"));
        assertTrue(langChainSupport.contains("chatModel().chat"));
        assertFalse(coreInvokeBlockThrowsUnsupported(langChainSupport));
        assertTrue(langChainAdapter.contains("extends LangChain4jLLMProviderAdapterSupport"));
    }

    @Test
    void generatedAgentOrchestratorDoesNotUseSlashPackageReferences() {
        ProjectConfig config = new ProjectConfig()
                .name("demo-app")
                .basePackage("com.example.demo");
        String source = new ModuleGenerator(config).generateAgentOrchestrator("com.example.demo");

        assertFalse(source.contains("com/example"));
        assertTrue(source.contains("com.example.demo.domain.dto.ChatRequest request"));
        assertTrue(source.contains("request.setMessage(task)"));
    }

    @Test
    void globalExceptionHandlerIsGeneratedInApiModule() throws Exception {
        ProjectConfig config = new ProjectConfig()
                .name("demo-app")
                .basePackage("com.example.demo")
                .outputDir(tempDir.toString());

        new ProjectGenerator(config).generate();

        Path handler = tempDir.resolve("demo-app/demo-app-api/src/main/java/com/example/demo/api/exception/GlobalExceptionHandler.java");
        assertTrue(Files.exists(handler));
        String source = Files.readString(handler);
        assertTrue(source.contains("@RestControllerAdvice"));
        assertTrue(source.contains("@ExceptionHandler(BaseException.class)"));
        assertTrue(source.contains("@ExceptionHandler(MethodArgumentNotValidException.class)"));
        assertTrue(source.contains("@ExceptionHandler(Exception.class)"));
        assertTrue(source.contains("Result.error(ErrorCode.INTERNAL_ERROR.code(), ErrorCode.INTERNAL_ERROR.message())"));
    }

    @Test
    void mqListenerIsGeneratedInAppModuleToAvoidInfraDependingOnApp() throws Exception {
        ProjectConfig config = new ProjectConfig()
                .name("demo-app")
                .basePackage("com.example.demo")
                .mqType(MqType.RABBITMQ)
                .outputDir(tempDir.toString());

        new ProjectGenerator(config).generate();

        Path appListener = tempDir.resolve("demo-app/demo-app-app/src/main/java/com/example/demo/app/mq/MqMessageListener.java");
        Path infraListener = tempDir.resolve("demo-app/demo-app-infra/src/main/java/com/example/demo/infra/mq/MqMessageListener.java");
        assertTrue(Files.exists(appListener));
        assertFalse(Files.exists(infraListener));

        String source = Files.readString(appListener);
        assertTrue(source.contains("package com.example.demo.app.mq;"));
        assertTrue(source.contains("import com.example.demo.infra.mq.MqConfig;"));
        assertFalse(source.contains("import com.example.demo.app.mq.MqAIProcessingService;"));

        String processingService = Files.readString(tempDir.resolve("demo-app/demo-app-app/src/main/java/com/example/demo/app/mq/MqAIProcessingService.java"));
        assertTrue(processingService.contains("new ChatRequest()"));
        assertFalse(processingService.contains("ChatRequest.builder()"));
    }

    private long countTopLevelKey(String yaml, String key) {
        return Pattern.compile("(?m)^" + Pattern.quote(key) + ":\\s*$").matcher(yaml).results().count();
    }

    private String section(String text, String from, String to) {
        int start = text.indexOf(from);
        int end = text.indexOf(to, start + from.length());
        if (start < 0) {
            return "";
        }
        return end < 0 ? text.substring(start) : text.substring(start, end);
    }

    private boolean coreInvokeBlockThrowsUnsupported(String source) {
        int invokeIndex = source.indexOf("public String invoke(String systemPrompt, String userMessage)");
        if (invokeIndex < 0) {
            return false;
        }
        int nextMethod = source.indexOf("public Flux<String> invokeStream", invokeIndex);
        String block = nextMethod < 0 ? source.substring(invokeIndex) : source.substring(invokeIndex, nextMethod);
        return block.contains("UnsupportedOperationException");
    }
}
