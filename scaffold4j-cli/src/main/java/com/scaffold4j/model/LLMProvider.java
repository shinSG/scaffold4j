package com.scaffold4j.model;

import java.util.Set;

/**
 * Supported LLM providers with their dependency coordinates.
 */
public enum LLMProvider {

    OPENAI("openai", "OpenAI", "spring-ai-starter-model-openai", "langchain4j-open-ai",
            "spring.ai.openai.api-key", "OPENAI_API_KEY"),
    OLLAMA("ollama", "Ollama (Local)", "spring-ai-starter-model-ollama", "langchain4j-ollama",
            "spring.ai.ollama.base-url", null),
    ANTHROPIC("anthropic", "Anthropic Claude", "spring-ai-starter-model-anthropic", "langchain4j-anthropic",
            "spring.ai.anthropic.api-key", "ANTHROPIC_API_KEY"),
    DEEPSEEK("deepseek", "DeepSeek", "spring-ai-starter-model-deepseek", "langchain4j-deepseek",
            "spring.ai.deepseek.api-key", "DEEPSEEK_API_KEY"),
    ZHIPUAI("zhipuai", "智谱 GLM", "spring-ai-starter-model-zhipuai", null,
            "spring.ai.zhipuai.api-key", "ZHIPUAI_API_KEY"),
    VERTEX_AI("vertex-ai", "Google Vertex AI", "spring-ai-starter-model-vertex-ai-gemini", "langchain4j-vertex-ai-gemini",
            null, "GOOGLE_APPLICATION_CREDENTIALS"),
    AZURE_OPENAI("azure-openai", "Azure OpenAI", "spring-ai-starter-model-azure-openai", "langchain4j-azure-open-ai",
            "spring.ai.azure.openai.api-key", "AZURE_OPENAI_API_KEY"),
    BEDROCK("bedrock", "AWS Bedrock", "spring-ai-starter-model-bedrock-ai", "langchain4j-bedrock",
            null, "AWS_ACCESS_KEY_ID"),
    QWEN("qwen", "通义千问 (DashScope)", "spring-ai-starter-model-dashscope", null,
            "spring.ai.dashscope.api-key", "DASHSCOPE_API_KEY"),
    MOONSHOT("moonshot", "Moonshot (月之暗面)", null, null,
            null, "MOONSHOT_API_KEY"),
    DOUBAO("doubao", "豆包 (火山引擎)", null, null,
            null, "DOUBAO_API_KEY");

    private final String id;
    private final String displayName;
    private final String springAiStarter;
    private final String langchain4jModule;
    private final String configKey;
    private final String envVar;

    LLMProvider(String id, String displayName, String springAiStarter, String langchain4jModule,
                String configKey, String envVar) {
        this.id = id;
        this.displayName = displayName;
        this.springAiStarter = springAiStarter;
        this.langchain4jModule = langchain4jModule;
        this.configKey = configKey;
        this.envVar = envVar;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public String springAiStarter() { return springAiStarter; }
    public String langchain4jModule() { return langchain4jModule; }
    public String configKey() { return configKey; }
    public String envVar() { return envVar; }

    public boolean hasSpringAiSupport() { return springAiStarter != null; }
    public boolean hasLangchain4jSupport() { return langchain4jModule != null; }

    public static LLMProvider fromId(String id) {
        for (LLMProvider p : values()) {
            if (p.id.equalsIgnoreCase(id)) return p;
        }
        throw new IllegalArgumentException("Unknown LLM provider: " + id
                + ". Use 'scaffold4j list-providers' to see available providers.");
    }

    /** Commonly recommended providers for beginners. */
    public static Set<LLMProvider> recommended() {
        return Set.of(OPENAI, OLLAMA, ANTHROPIC);
    }
}
