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
        System.out.println("AI Framework options: spring-ai, spring-ai-alibaba, langchain4j, both");
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

        // 9. Database
        System.out.println();
        System.out.println("Database types: mysql, postgresql, h2 (embedded)");
        cmd.dbType(prompt(console, "Database type", cmd.dbType() != null ? cmd.dbType() : "h2"));
        if (cmd.dbType() != null && !"h2".equalsIgnoreCase(cmd.dbType())) {
            cmd.dbHost(prompt(console, "Database host", cmd.dbHost() != null ? cmd.dbHost() : "localhost"));
            String dbPortStr = prompt(console, "Database port", cmd.dbPort() != null ? String.valueOf(cmd.dbPort()) : "");
            cmd.dbPort(dbPortStr.isEmpty() ? null : Integer.parseInt(dbPortStr));
            cmd.dbName(prompt(console, "Database name", cmd.dbName()));
            cmd.dbUsername(prompt(console, "Database username", cmd.dbUsername()));
            cmd.dbPassword(prompt(console, "Database password", cmd.dbPassword()));
        }
        System.out.println();
        System.out.println("ORM frameworks: mybatis-plus, jpa");
        cmd.orm(prompt(console, "ORM framework", cmd.orm() != null ? cmd.orm() : "mybatis-plus"));

        // 10. Cache
        System.out.println();
        System.out.println("Cache types: redis, caffeine, none");
        cmd.cacheType(prompt(console, "Cache type", cmd.cacheType() != null ? cmd.cacheType() : "none"));
        if ("redis".equalsIgnoreCase(cmd.cacheType())) {
            cmd.redisHost(prompt(console, "Redis host", cmd.redisHost() != null ? cmd.redisHost() : "localhost"));
            String redisPortStr = prompt(console, "Redis port", cmd.redisPort() != null ? String.valueOf(cmd.redisPort()) : "6379");
            cmd.redisPort(Integer.parseInt(redisPortStr));
            cmd.redisPassword(prompt(console, "Redis password", cmd.redisPassword()));
            String redisDbStr = prompt(console, "Redis database", cmd.redisDatabase() != null ? String.valueOf(cmd.redisDatabase()) : "0");
            cmd.redisDatabase(Integer.parseInt(redisDbStr));
        }

        // 11. Message Queue
        System.out.println();
        System.out.println("MQ types: rabbitmq, rocketmq, kafka, none");
        cmd.mqType(prompt(console, "MQ type", cmd.mqType() != null ? cmd.mqType() : "none"));
        if (cmd.mqType() != null && !"none".equalsIgnoreCase(cmd.mqType())) {
            cmd.mqHost(prompt(console, "MQ host", cmd.mqHost() != null ? cmd.mqHost() : "localhost"));
            String mqPortStr = prompt(console, "MQ port", cmd.mqPort() != null ? String.valueOf(cmd.mqPort()) : "");
            cmd.mqPort(mqPortStr.isEmpty() ? null : Integer.parseInt(mqPortStr));
            cmd.mqUsername(prompt(console, "MQ username", cmd.mqUsername() != null ? cmd.mqUsername() : "guest"));
            cmd.mqPassword(prompt(console, "MQ password", cmd.mqPassword() != null ? cmd.mqPassword() : "guest"));
            if ("rabbitmq".equalsIgnoreCase(cmd.mqType())) {
                cmd.mqVirtualHost(prompt(console, "RabbitMQ virtual host", cmd.mqVirtualHost() != null ? cmd.mqVirtualHost() : "/"));
            }
            cmd.mqGroup(prompt(console, "Consumer group", cmd.mqGroup() != null ? cmd.mqGroup() : "scaffold4j-consumer"));
        }

        // 12. Output
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
