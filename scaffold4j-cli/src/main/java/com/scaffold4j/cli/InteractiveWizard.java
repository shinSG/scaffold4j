package com.scaffold4j.cli;

import java.io.Console;

/**
 * Interactive wizard mode — prompts the user step-by-step to fill in
 * project configuration, then updates the command fields in-place.
 * <p>
 * Falls back gracefully if no console is available (e.g., piped input).
 */
public final class InteractiveWizard {

    private InteractiveWizard() {}

    public static void run(GenerateCommand cmd) {
        Console console = System.console();
        if (console == null) {
            System.out.println("[scaffold4j] No interactive console available. "
                    + "Using CLI arguments directly.");
            return;
        }

        System.out.println();
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║   scaffold4j — Interactive Project Wizard    ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Press Enter to accept defaults (shown in brackets).");
        System.out.println();

        // 1. Project basics
        cmd.name(prompt(console, "Project name", cmd.name()));
        cmd.basePackage(prompt(console, "Base package", cmd.basePackage()));
        cmd.groupId(prompt(console, "Maven groupId", cmd.groupId() != null ? cmd.groupId() : cmd.basePackage()));
        cmd.artifactId(prompt(console, "Maven artifactId", cmd.artifactId() != null ? cmd.artifactId() : cmd.name()));
        cmd.version(prompt(console, "Version", cmd.version()));

        // 2. Build
        String jv = prompt(console, "Java version (17+)", String.valueOf(cmd.javaVersion()));
        cmd.javaVersion(Integer.parseInt(jv));
        cmd.springBootVersion(prompt(console, "Spring Boot version", cmd.springBootVersion()));

        // 3. AI framework
        System.out.println();
        System.out.println("AI Framework options: spring-ai, langchain4j, both");
        cmd.aiFramework(prompt(console, "AI framework", cmd.aiFramework()));

        // 4. LLM providers
        System.out.println();
        System.out.println("LLM Providers (comma-separated): openai, ollama, anthropic, deepseek, zhipuai, vertex-ai, azure-openai, bedrock, qwen, moonshot, doubao");
        cmd.llmProviders(prompt(console, "LLM providers", cmd.llmProviders()));

        // 5. Protocols
        System.out.println();
        System.out.println("Protocols (comma-separated): rest, mcp, a2a, acp");
        cmd.protocols(prompt(console, "Protocols", cmd.protocols()));

        // 6. Features
        System.out.println();
        System.out.println("Features (comma-separated): memory, rag, sse, websocket");
        cmd.features(prompt(console, "Features", cmd.features() != null ? cmd.features() : ""));

        // 7. Vector store
        System.out.println();
        System.out.println("Vector stores: pgvector, milvus, chroma, pinecone, elasticsearch, redis, weaviate, qdrant, simple");
        cmd.vectorStore(prompt(console, "Vector store", cmd.vectorStore()));

        // 8. Nacos
        String useNacos = prompt(console, "Enable Nacos? (true/false)", String.valueOf(cmd.nacos()));
        cmd.nacos(Boolean.parseBoolean(useNacos));
        if (cmd.nacos()) {
            cmd.nacosAddr(prompt(console, "Nacos server address", cmd.nacosAddr()));
            cmd.nacosNamespace(prompt(console, "Nacos namespace", cmd.nacosNamespace()));
        }

        // 9. Output
        cmd.outputDir(prompt(console, "Output directory", cmd.outputDir()));

        System.out.println();
        System.out.println("Configuration complete. Generating project...");
    }

    private static String prompt(Console console, String label, String defaultValue) {
        String dflt = (defaultValue != null && !defaultValue.isBlank()) ? defaultValue : "";
        String display = dflt.isEmpty() ? label + ": " : label + " [" + dflt + "]: ";
        String input = console.readLine(display);
        return (input == null || input.trim().isEmpty()) ? dflt : input.trim();
    }
}
