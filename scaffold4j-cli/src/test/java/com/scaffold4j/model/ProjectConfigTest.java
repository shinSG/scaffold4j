package com.scaffold4j.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProjectConfigTest {

    @Test
    @DisplayName("Should build minimal config with defaults")
    void minimalConfig() {
        ProjectConfig cfg = new ProjectConfig()
                .name("test-app")
                .basePackage("com.example.ai");

        cfg.validate();

        assertEquals("com.example.ai", cfg.effectiveGroupId());
        assertEquals("test-app", cfg.effectiveArtifactId());
        assertEquals("com/example/ai", cfg.packagePath());
        assertTrue(cfg.hasProtocol(Protocol.REST));
        assertEquals(1, cfg.llmProviders().size());
        assertTrue(cfg.hasLLMProvider(LLMProvider.OPENAI));
    }

    @Test
    @DisplayName("Should build full-featured config")
    void fullConfig() {
        ProjectConfig cfg = new ProjectConfig()
                .name("full-app")
                .basePackage("com.example.ai")
                .groupId("com.custom")
                .version("2.0.0")
                .javaVersion(21)
                .aiFramework(AIFramework.BOTH)
                .protocols(Set.of(Protocol.REST, Protocol.MCP, Protocol.A2A))
                .addLLMProvider(LLMProvider.OPENAI)
                .addLLMProvider(LLMProvider.OLLAMA)
                .addLLMProvider(LLMProvider.ANTHROPIC)
                .vectorStore(VectorStore.MILVUS)
                .addFeature(Feature.MEMORY)
                .addFeature(Feature.RAG)
                .addFeature(Feature.SSE)
                .addFeature(Feature.WEBSOCKET)
                .nacosEnabled(true)
                .nacosAddr("nacos.prod:8848");

        cfg.validate();

        assertTrue(cfg.usesSpringAI());
        assertTrue(cfg.usesLangChain4j());
        assertTrue(cfg.hasFeature(Feature.MEMORY));
        assertTrue(cfg.hasFeature(Feature.RAG));
        assertTrue(cfg.hasProtocol(Protocol.MCP));
        assertTrue(cfg.hasProtocol(Protocol.A2A));
        assertTrue(cfg.nacosEnabled());
        assertEquals("com.custom", cfg.effectiveGroupId());
        assertEquals("nacos.prod:8848", cfg.nacosAddr());
    }

    @Test
    @DisplayName("Should derive groupId from package")
    void derivedGroupId() {
        ProjectConfig cfg = new ProjectConfig()
                .name("app")
                .basePackage("io.mycompany.ai");

        assertEquals("io.mycompany.ai", cfg.effectiveGroupId());
    }

    @Test
    @DisplayName("Should reject empty name")
    void rejectEmptyName() {
        ProjectConfig cfg = new ProjectConfig()
                .name("")
                .basePackage("com.example.ai");

        assertThrows(IllegalArgumentException.class, cfg::validate);
    }

    @Test
    @DisplayName("Should reject invalid package name")
    void rejectInvalidPackage() {
        ProjectConfig cfg = new ProjectConfig()
                .name("app")
                .basePackage("invalid");

        assertThrows(IllegalArgumentException.class, cfg::validate);
    }
}
