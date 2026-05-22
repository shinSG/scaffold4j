package com.scaffold4j.model;

/**
 * Supported AI development frameworks.
 */
public enum AIFramework {

    SPRING_AI("spring-ai", "Spring AI", "Spring ecosystem native AI framework, deep Boot integration"),
    SPRING_AI_ALIBABA("spring-ai-alibaba", "Spring AI Alibaba", "Alibaba Cloud Spring AI, DashScope models + Nacos integration"),
    LANGCHAIN4J("langchain4j", "LangChain4j", "Java port of LangChain, rich provider ecosystem and AI Services"),
    BOTH("both", "Spring AI + LangChain4j", "Combined: Spring AI for Boot integration + LangChain4j for advanced AI patterns");

    private final String id;
    private final String displayName;
    private final String description;

    AIFramework(String id, String displayName, String description) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public String description() { return description; }

    public static AIFramework fromId(String id) {
        for (AIFramework f : values()) {
            if (f.id.equalsIgnoreCase(id)) return f;
        }
        throw new IllegalArgumentException("Unknown AI framework: " + id
                + ". Valid values: spring-ai, spring-ai-alibaba, langchain4j, both");
    }
}
