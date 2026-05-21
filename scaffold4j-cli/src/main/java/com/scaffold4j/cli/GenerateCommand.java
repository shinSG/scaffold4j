package com.scaffold4j.cli;

import com.scaffold4j.generator.ProjectGenerator;
import com.scaffold4j.model.AIFramework;
import com.scaffold4j.model.Feature;
import com.scaffold4j.model.LLMProvider;
import com.scaffold4j.model.ProjectConfig;
import com.scaffold4j.model.Protocol;
import com.scaffold4j.model.VectorStore;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * The {@code generate} subcommand — the core of scaffold4j.
 * Parses CLI arguments into a {@link ProjectConfig} and triggers project generation.
 */
@Command(
    name = "generate",
    description = "Generate a new Java AI application project skeleton.",
    mixinStandardHelpOptions = true
)
public class GenerateCommand implements Callable<Integer> {

    // ---- Required ----

    @Option(names = {"-n", "--name"}, required = true,
            description = "Project name (used as directory and artifactId).")
    private String name;

    @Option(names = {"-p", "--package"}, required = true,
            description = "Base Java package (e.g., com.example.ai).")
    private String basePackage;

    // ---- Maven coordinates ----

    @Option(names = {"--group-id"},
            description = "Maven groupId. Defaults to base package.")
    private String groupId;

    @Option(names = {"--artifact-id"},
            description = "Maven artifactId. Defaults to project name.")
    private String artifactId;

    @Option(names = {"--version"},
            description = "Project version. Default: 1.0.0-SNAPSHOT")
    private String version = "1.0.0-SNAPSHOT";

    // ---- Build ----

    @Option(names = {"--java-version"},
            description = "Java version. Valid values: 17, 21. Default: 17")
    private int javaVersion = 17;

    @Option(names = {"--spring-boot-version"},
            description = "Spring Boot version. Default: 3.5.0")
    private String springBootVersion = "3.5.0";

    // ---- AI Framework ----

    @Option(names = {"--ai-framework"},
            description = "AI framework. Valid values: spring-ai, langchain4j, both. Default: spring-ai")
    private String aiFramework = "spring-ai";

    // ---- LLM Providers ----

    @Option(names = {"--llm-providers"},
            description = "LLM providers (comma-separated). Valid values: openai, ollama, anthropic, deepseek, zhipuai, vertex-ai, azure-openai, bedrock, qwen, moonshot, doubao. Default: openai")
    private String llmProviders = "openai";

    // ---- Vector Store ----

    @Option(names = {"--vector-store"},
            description = "Vector store backend. Valid values: pgvector, milvus, chroma, pinecone, elasticsearch, redis, weaviate, qdrant, simple. Default: pgvector")
    private String vectorStore = "pgvector";

    // ---- Protocols ----

    @Option(names = {"--protocols"},
            description = "Protocols to support (comma-separated). Valid values: rest, mcp, a2a, acp. Default: rest")
    private String protocols = "rest";

    // ---- Features ----

    @Option(names = {"--features"},
            description = "Features to include (comma-separated). Valid values: memory, rag, sse, websocket.")
    private String features = "";

    // ---- Nacos ----

    @Option(names = {"--nacos"},
            description = "Enable Nacos service registration. Default: false")
    private boolean nacos = false;

    @Option(names = {"--nacos-addr"},
            description = "Nacos server address. Default: localhost:8848")
    private String nacosAddr = "localhost:8848";

    @Option(names = {"--nacos-namespace"},
            description = "Nacos namespace.")
    private String nacosNamespace = "";

    // ---- Output ----

    @Option(names = {"-o", "--output-dir"},
            description = "Output directory for the generated project. Default: ./")
    private String outputDir = "./";

    // ---- Interactive mode ----

    @Option(names = {"-i", "--interactive"},
            description = "Run in interactive (wizard) mode.")
    private boolean interactive = false;

    @Override
    public Integer call() throws Exception {
        if (interactive) {
            InteractiveWizard.run(this);
        }

        ProjectConfig config = buildConfig();
        config.validate();

        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║          scaffold4j — Generating Project...          ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("  Project:     " + config.effectiveArtifactId());
        System.out.println("  Package:     " + config.basePackage());
        System.out.println("  Java:        " + config.javaVersion());
        System.out.println("  Boot:        " + config.springBootVersion());
        System.out.println("  Framework:   " + config.aiFramework().displayName());
        System.out.println("  Protocols:   " + config.protocols());
        System.out.println("  Providers:   " + config.llmProviders().stream().map(LLMProvider::id).toList());
        System.out.println("  Features:    " + (config.features().isEmpty() ? "(none)" : config.features()));
        System.out.println("  Nacos:       " + (config.nacosEnabled() ? "enabled (" + config.nacosAddr() + ")" : "disabled"));
        System.out.println();

        new ProjectGenerator(config).generate();

        System.out.println();
        System.out.println("Done! Project generated at: " +
                config.outputDir() + "/" + config.effectiveArtifactId());
        System.out.println();
        System.out.println("Next steps:");
        System.out.println("  cd " + config.effectiveArtifactId());
        System.out.println("  ./mvnw spring-boot:run");
        System.out.println();

        return 0;
    }

    /**
     * Converts parsed CLI arguments into a fully populated ProjectConfig.
     */
    ProjectConfig buildConfig() {
        ProjectConfig cfg = new ProjectConfig()
                .name(name)
                .basePackage(basePackage)
                .groupId(groupId)
                .artifactId(artifactId)
                .version(version)
                .javaVersion(javaVersion)
                .springBootVersion(springBootVersion)
                .aiFramework(AIFramework.fromId(aiFramework))
                .vectorStore(VectorStore.fromId(vectorStore))
                .nacosEnabled(nacos)
                .nacosAddr(nacosAddr)
                .nacosNamespace(nacosNamespace)
                .outputDir(outputDir);

        // Parse comma-separated values into typed sets
        cfg.protocols(parseSet(protocols, Protocol::fromId));
        cfg.llmProviders(parseSet(llmProviders, LLMProvider::fromId));
        cfg.features(parseSet(features, Feature::fromId));

        return cfg;
    }

    @FunctionalInterface
    private interface Parser<T> {
        T parse(String s);
    }

    private <T> Set<T> parseSet(String csv, Parser<T> parser) {
        Set<T> set = new LinkedHashSet<>();
        if (csv == null || csv.isBlank()) return set;
        for (String part : csv.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                set.add(parser.parse(trimmed));
            }
        }
        return set;
    }

    // ---- Field accessors for InteractiveWizard ----

    public String name() { return name; }
    public void name(String v) { this.name = v; }
    public String basePackage() { return basePackage; }
    public void basePackage(String v) { this.basePackage = v; }
    public void groupId(String v) { this.groupId = v; }
    public void artifactId(String v) { this.artifactId = v; }
    public void version(String v) { this.version = v; }
    public void javaVersion(int v) { this.javaVersion = v; }
    public void springBootVersion(String v) { this.springBootVersion = v; }
    public void aiFramework(String v) { this.aiFramework = v; }
    public void llmProviders(String v) { this.llmProviders = v; }
    public void vectorStore(String v) { this.vectorStore = v; }
    public void protocols(String v) { this.protocols = v; }
    public void features(String v) { this.features = v; }
    public void nacos(boolean v) { this.nacos = v; }
    public void nacosAddr(String v) { this.nacosAddr = v; }
    public void nacosNamespace(String v) { this.nacosNamespace = v; }
    public void outputDir(String v) { this.outputDir = v; }
    public boolean interactive() { return interactive; }
    public String groupId() { return groupId; }
    public String artifactId() { return artifactId; }
    public String version() { return version; }
    public int javaVersion() { return javaVersion; }
    public String springBootVersion() { return springBootVersion; }
    public String aiFramework() { return aiFramework; }
    public String llmProviders() { return llmProviders; }
    public String vectorStore() { return vectorStore; }
    public String protocols() { return protocols; }
    public String features() { return features; }
    public boolean nacos() { return nacos; }
    public String nacosAddr() { return nacosAddr; }
    public String nacosNamespace() { return nacosNamespace; }
    public String outputDir() { return outputDir; }
}
