package com.scaffold4j.cli;

import com.scaffold4j.generator.ProjectGenerator;
import com.scaffold4j.model.AIFramework;
import com.scaffold4j.model.CacheType;
import com.scaffold4j.model.DatabaseType;
import com.scaffold4j.model.Feature;
import com.scaffold4j.model.LLMProvider;
import com.scaffold4j.model.MqType;
import com.scaffold4j.model.OrmType;
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
            description = "Enable Nacos service discovery + config center. Default: false")
    private boolean nacos = false;

    @Option(names = {"--nacos-discovery"},
            description = "Enable Nacos service registration only.")
    private Boolean nacosDiscovery;

    @Option(names = {"--nacos-config"},
            description = "Enable Nacos config center only.")
    private Boolean nacosConfig;

    @Option(names = {"--nacos-addr"},
            description = "Nacos server address. Default: localhost:8848")
    private String nacosAddr = "localhost:8848";

    @Option(names = {"--nacos-namespace"},
            description = "Nacos namespace.")
    private String nacosNamespace = "";

    // ---- Database ----

    @Option(names = {"--db-type"},
            description = "Database type. Valid values: mysql, postgresql, h2. Default: h2 (embedded)")
    private String dbType;

    @Option(names = {"--db-host"},
            description = "Database host. Default: localhost")
    private String dbHost = "localhost";

    @Option(names = {"--db-port"},
            description = "Database port. Default depends on db-type (mysql=3306, postgresql=5432).")
    private Integer dbPort;

    @Option(names = {"--db-name"},
            description = "Database name. Defaults to project name.")
    private String dbName;

    @Option(names = {"--db-username"},
            description = "Database username. Default: root")
    private String dbUsername = "root";

    @Option(names = {"--db-password"},
            description = "Database password. Default: root")
    private String dbPassword = "root";

    @Option(names = {"--orm"},
            description = "ORM framework. Valid values: mybatis-plus, jpa. Default: mybatis-plus")
    private String orm;

    // ---- Message Queue ----

    @Option(names = {"--mq-type"},
            description = "Message queue type. Valid values: rabbitmq, rocketmq, kafka, none. Default: none")
    private String mqType = "none";

    @Option(names = {"--mq-host"},
            description = "MQ server host. Default: localhost")
    private String mqHost = "localhost";

    @Option(names = {"--mq-port"},
            description = "MQ server port. Default depends on mq-type.")
    private Integer mqPort;

    @Option(names = {"--mq-username"},
            description = "MQ username. Default: guest")
    private String mqUsername = "guest";

    @Option(names = {"--mq-password"},
            description = "MQ password. Default: guest")
    private String mqPassword = "guest";

    @Option(names = {"--mq-virtual-host"},
            description = "RabbitMQ virtual host. Default: /")
    private String mqVirtualHost = "/";

    @Option(names = {"--mq-group"},
            description = "Consumer group name. Default: scaffold4j-consumer")
    private String mqGroup = "scaffold4j-consumer";

    // ---- Cache ----

    @Option(names = {"--cache-type"},
            description = "Cache type. Valid values: redis, caffeine, none. Default: none")
    private String cacheType;

    @Option(names = {"--redis-host"},
            description = "Redis host. Default: localhost")
    private String redisHost = "localhost";

    @Option(names = {"--redis-port"},
            description = "Redis port. Default: 6379")
    private Integer redisPort = 6379;

    @Option(names = {"--redis-password"},
            description = "Redis password.")
    private String redisPassword;

    @Option(names = {"--redis-database"},
            description = "Redis database number. Default: 0")
    private Integer redisDatabase = 0;

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
        System.out.println("  Database:    " + config.dbType().displayName());
        System.out.println("  ORM:         " + config.ormType().displayName());
        System.out.println("  Cache:       " + config.cacheType().displayName());
        System.out.println("  MQ:           " + config.mqType().displayName());
        System.out.println("  Nacos:       " + (config.hasNacos()
                ? "discovery=" + config.hasNacosDiscovery() + " config=" + config.hasNacosConfig()
                : "disabled"));
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
        // Nacos: --nacos enables both, --nacos-discovery/--nacos-config override
        boolean discovery = nacosDiscovery != null ? nacosDiscovery : nacos;
        boolean config = nacosConfig != null ? nacosConfig : nacos;

        ProjectConfig cfg = new ProjectConfig()
                .name(name)
                .basePackage(basePackage)
                .groupId(groupId)
                .artifactId(artifactId)
                .version(version)
                .javaVersion(javaVersion)
                .springBootVersion(springBootVersion)
                .aiFramework(AIFramework.fromId(defaultIfBlank(aiFramework, "spring-ai")))
                .vectorStore(VectorStore.fromId(defaultIfBlank(vectorStore, "pgvector")))
                .nacosDiscoveryEnabled(discovery)
                .nacosConfigEnabled(config)
                .nacosAddr(nacosAddr)
                .nacosNamespace(nacosNamespace)
                .outputDir(outputDir);

        // Parse comma-separated values into typed sets
        cfg.protocols(parseSet(defaultIfBlank(protocols, "rest"), Protocol::fromId));
        cfg.llmProviders(parseSet(defaultIfBlank(llmProviders, "openai"), LLMProvider::fromId));
        cfg.features(parseSet(features, Feature::fromId));

        // Database configuration
        if (dbType != null && !dbType.isBlank()) {
            cfg.dbType(DatabaseType.fromId(dbType));
        }
        cfg.dbHost(dbHost);
        if (dbPort != null) cfg.dbPort(dbPort);
        if (dbName != null && !dbName.isBlank()) cfg.dbName(dbName);
        cfg.dbUsername(dbUsername);
        cfg.dbPassword(dbPassword);
        if (orm != null && !orm.isBlank()) {
            cfg.ormType(OrmType.fromId(orm));
        }

        // Cache configuration
        if (cacheType != null && !cacheType.isBlank()) {
            cfg.cacheType(CacheType.fromId(cacheType));
        }
        cfg.redisHost(redisHost);
        if (redisPort != null) cfg.redisPort(redisPort);
        if (redisPassword != null && !redisPassword.isBlank()) cfg.redisPassword(redisPassword);
        if (redisDatabase != null) cfg.redisDatabase(redisDatabase);

                // MQ configuration. When mq-type is none, ignore all MQ connection fields so
                // callers and wizards do not need to provide MQ host/port/auth settings.
                MqType selectedMqType = MqType.fromId(defaultIfBlank(mqType, "none"));
                cfg.mqType(selectedMqType);
                if (selectedMqType != MqType.NONE) {
                        cfg.mqHost(mqHost);
                        if (mqPort != null) cfg.mqPort(mqPort);
                        if (mqUsername != null && !mqUsername.isBlank()) cfg.mqUsername(mqUsername);
                        if (mqPassword != null && !mqPassword.isBlank()) cfg.mqPassword(mqPassword);
                        if (mqVirtualHost != null && !mqVirtualHost.isBlank()) cfg.mqVirtualHost(mqVirtualHost);
                        if (mqGroup != null && !mqGroup.isBlank()) cfg.mqGroup(mqGroup);
        }

        return cfg;
    }

        private String defaultIfBlank(String value, String defaultValue) {
                return (value == null || value.isBlank()) ? defaultValue : value;
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
    public void dbType(String v) { this.dbType = v; }
    public void dbHost(String v) { this.dbHost = v; }
    public void dbPort(Integer v) { this.dbPort = v; }
    public void dbName(String v) { this.dbName = v; }
    public void dbUsername(String v) { this.dbUsername = v; }
    public void dbPassword(String v) { this.dbPassword = v; }
    public void orm(String v) { this.orm = v; }
    public void cacheType(String v) { this.cacheType = v; }
    public void redisHost(String v) { this.redisHost = v; }
    public void redisPort(Integer v) { this.redisPort = v; }
    public void redisPassword(String v) { this.redisPassword = v; }
    public void redisDatabase(Integer v) { this.redisDatabase = v; }
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
    public String dbType() { return dbType; }
    public String dbHost() { return dbHost; }
    public Integer dbPort() { return dbPort; }
    public String dbName() { return dbName; }
    public String dbUsername() { return dbUsername; }
    public String dbPassword() { return dbPassword; }
    public String orm() { return orm; }
    public String cacheType() { return cacheType; }
    public String redisHost() { return redisHost; }
    public Integer redisPort() { return redisPort; }
    public String redisPassword() { return redisPassword; }
    public Integer redisDatabase() { return redisDatabase; }
    public void mqType(String v) { this.mqType = v; }
    public void mqHost(String v) { this.mqHost = v; }
    public void mqPort(Integer v) { this.mqPort = v; }
    public void mqUsername(String v) { this.mqUsername = v; }
    public void mqPassword(String v) { this.mqPassword = v; }
    public void mqVirtualHost(String v) { this.mqVirtualHost = v; }
    public void mqGroup(String v) { this.mqGroup = v; }
    public String mqType() { return mqType; }
    public String mqHost() { return mqHost; }
    public Integer mqPort() { return mqPort; }
    public String mqUsername() { return mqUsername; }
    public String mqPassword() { return mqPassword; }
    public String mqVirtualHost() { return mqVirtualHost; }
    public String mqGroup() { return mqGroup; }
    public String outputDir() { return outputDir; }
}
