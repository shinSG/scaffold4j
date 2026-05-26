package com.scaffold4j.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Central configuration model that captures all user choices for project generation.
 * This object is passed to all generators to drive conditional code generation.
 */
public class ProjectConfig {

    // ---- Required fields ----
    private String name;
    private String basePackage;

    // ---- Derived/optional Maven fields ----
    private String groupId;
    private String artifactId;
    private String version = "1.0.0-SNAPSHOT";

    // ---- Build configuration ----
    private int javaVersion = 17;
    private String springBootVersion = "3.5.0";
    private String springCloudVersion = "2025.0.0";

    // ---- AI configuration ----
    private AIFramework aiFramework = AIFramework.SPRING_AI;
    private Set<LLMProvider> llmProviders = new LinkedHashSet<>();
    private VectorStore vectorStore = VectorStore.PGVECTOR;

    // ---- Protocol support ----
    private Set<Protocol> protocols = new LinkedHashSet<>();

    // ---- Feature flags ----
    private Set<Feature> features = new LinkedHashSet<>();

    // ---- Nacos (backward-compat: nacosDiscoveryEnabled + nacosConfigEnabled replace old nacosEnabled) ----
    private boolean nacosDiscoveryEnabled = false;
    private boolean nacosConfigEnabled = false;
    private String nacosAddr = "localhost:8848";
    private String nacosNamespace = "";

    // ---- Database ----
    private DatabaseType dbType = DatabaseType.H2;
    private String dbHost = "localhost";
    private int dbPort = 0; // 0 means use default for dbType
    private String dbName;
    private String dbUsername = "root";
    private String dbPassword = "root";
    private OrmType ormType = OrmType.MYBATIS_PLUS;

    // ---- Cache ----
    private CacheType cacheType = CacheType.NONE;
    private String redisHost = "localhost";
    private int redisPort = 6379;
    private String redisPassword;
    private int redisDatabase = 0;

    // ---- Message Queue ----
    private MqType mqType = MqType.NONE;
    private String mqHost = "localhost";
    private int mqPort = 0;
    private String mqUsername = "guest";
    private String mqPassword = "guest";
    private String mqVirtualHost = "/";
    private String mqGroup = "scaffold4j-consumer";

    // ---- Output ----
    private String outputDir = "./";

    public ProjectConfig() {
        protocols.add(Protocol.REST);
        llmProviders.add(LLMProvider.OPENAI);
    }

    // ---- Computed properties ----

    /** Maven groupId derived from base package if not explicitly set. */
    public String effectiveGroupId() {
        return (groupId != null && !groupId.isBlank()) ? groupId : basePackage;
    }

    /** Maven artifactId derived from project name if not explicitly set. */
    public String effectiveArtifactId() {
        return (artifactId != null && !artifactId.isBlank()) ? artifactId : name;
    }

    /** Convert base package to file path (e.g. com.example.ai -> com/example/ai). */
    public String packagePath() {
        return basePackage.replace('.', '/');
    }

    /** Module directory name for the given suffix (e.g. "common" -> "myapp-common"). */
    public String moduleName(String suffix) {
        return effectiveArtifactId() + "-" + suffix;
    }

    // ---- Feature checks ----

    public boolean hasProtocol(Protocol p) { return protocols.contains(p); }
    public boolean hasFeature(Feature f) { return features.contains(f); }
    public boolean hasLLMProvider(LLMProvider p) { return llmProviders.contains(p); }
    public boolean usesSpringAI() {
        return aiFramework == AIFramework.SPRING_AI
                || aiFramework == AIFramework.SPRING_AI_ALIBABA
                || aiFramework == AIFramework.BOTH;
    }
    public boolean usesSpringAIAlibaba() {
        return aiFramework == AIFramework.SPRING_AI_ALIBABA;
    }
    public boolean usesLangChain4j() {
        return aiFramework == AIFramework.LANGCHAIN4J || aiFramework == AIFramework.BOTH;
    }

    // ---- Database checks ----
    public boolean hasDatabase() { return dbType != null; }
    public boolean usesMyBatisPlus() { return hasDatabase() && ormType == OrmType.MYBATIS_PLUS; }
    public boolean usesJpa() { return hasDatabase() && ormType == OrmType.JPA; }
    public int effectiveDbPort() {
        return dbPort > 0 ? dbPort : dbType.defaultPort();
    }
    public String effectiveDbName() {
        return (dbName != null && !dbName.isBlank()) ? dbName : name;
    }

    // ---- Cache checks ----
    public boolean hasRedisCache() { return cacheType == CacheType.REDIS; }
    public boolean hasCaffeineCache() { return cacheType == CacheType.CAFFEINE; }
    public boolean hasCache() { return cacheType != CacheType.NONE; }

    // ---- MQ checks ----
    public boolean hasMq() { return mqType != MqType.NONE; }
    public boolean isRabbitMq() { return mqType == MqType.RABBITMQ; }
    public boolean isRocketMq() { return mqType == MqType.ROCKETMQ; }
    public boolean isKafka() { return mqType == MqType.KAFKA; }
    public int effectiveMqPort() {
        return mqPort > 0 ? mqPort : mqType.defaultPort();
    }

    // ---- Nacos checks ----
    public boolean hasNacosDiscovery() { return nacosDiscoveryEnabled; }
    public boolean hasNacosConfig() { return nacosConfigEnabled; }
    public boolean hasNacos() { return nacosDiscoveryEnabled || nacosConfigEnabled; }

    /**
     * Validate the configuration for completeness and consistency.
     * @throws IllegalArgumentException if configuration is invalid
     */
    public void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Project name (--name) is required.");
        }
        if (!name.matches("[a-zA-Z][a-zA-Z0-9_-]*")) {
            throw new IllegalArgumentException("Project name must start with a letter and contain only letters, digits, hyphens, and underscores.");
        }
        if (basePackage == null || basePackage.isBlank()) {
            throw new IllegalArgumentException("Base package (--package) is required.");
        }
        if (!basePackage.matches("^[a-z][a-z0-9]*(\\.[a-z][a-z0-9]*)+$")) {
            throw new IllegalArgumentException("Base package must be a valid Java package name (e.g., com.example.ai).");
        }
        if (protocols.isEmpty()) {
            throw new IllegalArgumentException("At least one protocol must be specified.");
        }
        if (llmProviders.isEmpty()) {
            throw new IllegalArgumentException("At least one LLM provider must be specified.");
        }
        if (!usesSpringAI() && !usesLangChain4j()) {
            throw new IllegalArgumentException("At least one AI framework must be selected.");
        }
        if (hasFeature(Feature.RAG) && !usesSpringAI() && !usesLangChain4j()) {
            throw new IllegalArgumentException("RAG requires at least one AI framework to be selected.");
        }
        for (LLMProvider provider : llmProviders) {
            if (usesSpringAI() && !usesLangChain4j() && !provider.hasSpringAiSupport()) {
                throw new IllegalArgumentException("Provider " + provider.id() + " does not have Spring AI support.");
            }
            if (usesLangChain4j() && !usesSpringAI() && !provider.hasLangchain4jSupport()) {
                throw new IllegalArgumentException("Provider " + provider.id() + " does not have LangChain4j support.");
            }
            if (usesSpringAI() && usesLangChain4j()
                    && !provider.hasSpringAiSupport() && !provider.hasLangchain4jSupport()) {
                throw new IllegalArgumentException("Provider " + provider.id() + " does not have Spring AI or LangChain4j support.");
            }
        }
    }

    // ---- Fluent setters ----

    public ProjectConfig name(String v) { this.name = v; return this; }
    public ProjectConfig basePackage(String v) { this.basePackage = v; return this; }
    public ProjectConfig groupId(String v) { this.groupId = v; return this; }
    public ProjectConfig artifactId(String v) { this.artifactId = v; return this; }
    public ProjectConfig version(String v) { this.version = v; return this; }
    public ProjectConfig javaVersion(int v) { this.javaVersion = v; return this; }
    public ProjectConfig springBootVersion(String v) { this.springBootVersion = v; return this; }
    public ProjectConfig springCloudVersion(String v) { this.springCloudVersion = v; return this; }
    public ProjectConfig aiFramework(AIFramework v) { this.aiFramework = v; return this; }
    public ProjectConfig llmProviders(Set<LLMProvider> v) { this.llmProviders = v; return this; }
    public ProjectConfig addLLMProvider(LLMProvider v) { this.llmProviders.add(v); return this; }
    public ProjectConfig vectorStore(VectorStore v) { this.vectorStore = v; return this; }
    public ProjectConfig protocols(Set<Protocol> v) { this.protocols = v; return this; }
    public ProjectConfig addProtocol(Protocol v) { this.protocols.add(v); return this; }
    public ProjectConfig features(Set<Feature> v) { this.features = v; return this; }
    public ProjectConfig addFeature(Feature v) { this.features.add(v); return this; }
    public ProjectConfig nacosEnabled(boolean v) { this.nacosDiscoveryEnabled = v; this.nacosConfigEnabled = v; return this; }
    public ProjectConfig nacosDiscoveryEnabled(boolean v) { this.nacosDiscoveryEnabled = v; return this; }
    public ProjectConfig nacosConfigEnabled(boolean v) { this.nacosConfigEnabled = v; return this; }
    public ProjectConfig nacosAddr(String v) { this.nacosAddr = v; return this; }
    public ProjectConfig nacosNamespace(String v) { this.nacosNamespace = v; return this; }
    public ProjectConfig dbType(DatabaseType v) { this.dbType = v; return this; }
    public ProjectConfig dbHost(String v) { this.dbHost = v; return this; }
    public ProjectConfig dbPort(int v) { this.dbPort = v; return this; }
    public ProjectConfig dbName(String v) { this.dbName = v; return this; }
    public ProjectConfig dbUsername(String v) { this.dbUsername = v; return this; }
    public ProjectConfig dbPassword(String v) { this.dbPassword = v; return this; }
    public ProjectConfig ormType(OrmType v) { this.ormType = v; return this; }
    public ProjectConfig cacheType(CacheType v) { this.cacheType = v; return this; }
    public ProjectConfig redisHost(String v) { this.redisHost = v; return this; }
    public ProjectConfig redisPort(int v) { this.redisPort = v; return this; }
    public ProjectConfig redisPassword(String v) { this.redisPassword = v; return this; }
    public ProjectConfig redisDatabase(int v) { this.redisDatabase = v; return this; }
    public ProjectConfig mqType(MqType v) { this.mqType = v; return this; }
    public ProjectConfig mqHost(String v) { this.mqHost = v; return this; }
    public ProjectConfig mqPort(int v) { this.mqPort = v; return this; }
    public ProjectConfig mqUsername(String v) { this.mqUsername = v; return this; }
    public ProjectConfig mqPassword(String v) { this.mqPassword = v; return this; }
    public ProjectConfig mqVirtualHost(String v) { this.mqVirtualHost = v; return this; }
    public ProjectConfig mqGroup(String v) { this.mqGroup = v; return this; }
    public ProjectConfig outputDir(String v) { this.outputDir = v; return this; }

    // ---- Getters ----

    public String name() { return name; }
    public String basePackage() { return basePackage; }
    public String groupId() { return groupId; }
    public String artifactId() { return artifactId; }
    public String version() { return version; }
    public int javaVersion() { return javaVersion; }
    public String springBootVersion() { return springBootVersion; }
    public String springCloudVersion() { return springCloudVersion; }
    public AIFramework aiFramework() { return aiFramework; }
    public Set<LLMProvider> llmProviders() { return Collections.unmodifiableSet(llmProviders); }
    public VectorStore vectorStore() { return vectorStore; }
    public Set<Protocol> protocols() { return Collections.unmodifiableSet(protocols); }
    public Set<Feature> features() { return Collections.unmodifiableSet(features); }
    public boolean nacosEnabled() { return hasNacos(); }
    public boolean nacosDiscoveryEnabled() { return nacosDiscoveryEnabled; }
    public boolean nacosConfigEnabled() { return nacosConfigEnabled; }
    public String nacosAddr() { return nacosAddr; }
    public String nacosNamespace() { return nacosNamespace; }
    public DatabaseType dbType() { return dbType; }
    public String dbHost() { return dbHost; }
    public int dbPort() { return dbPort; }
    public String dbName() { return dbName; }
    public String dbUsername() { return dbUsername; }
    public String dbPassword() { return dbPassword; }
    public OrmType ormType() { return ormType; }
    public CacheType cacheType() { return cacheType; }
    public String redisHost() { return redisHost; }
    public int redisPort() { return redisPort; }
    public String redisPassword() { return redisPassword; }
    public int redisDatabase() { return redisDatabase; }
    public MqType mqType() { return mqType; }
    public String mqHost() { return mqHost; }
    public int mqPort() { return mqPort; }
    public String mqUsername() { return mqUsername; }
    public String mqPassword() { return mqPassword; }
    public String mqVirtualHost() { return mqVirtualHost; }
    public String mqGroup() { return mqGroup; }
    public String outputDir() { return outputDir; }
}
