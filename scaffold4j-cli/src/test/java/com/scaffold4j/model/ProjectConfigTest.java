package com.scaffold4j.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

import com.scaffold4j.model.DatabaseType;
import com.scaffold4j.model.CacheType;
import com.scaffold4j.model.MqType;
import com.scaffold4j.model.OrmType;

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
        // Default database/cache
        assertTrue(cfg.hasDatabase());
        assertEquals(DatabaseType.H2, cfg.dbType());
        assertFalse(cfg.hasCache());
        // Default Nacos off
        assertFalse(cfg.hasNacos());
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
        assertTrue(cfg.hasNacos());
        assertTrue(cfg.hasNacosDiscovery());
        assertTrue(cfg.hasNacosConfig());
        assertEquals("com.custom", cfg.effectiveGroupId());
        assertEquals("nacos.prod:8848", cfg.nacosAddr());
    }

    @Test
    @DisplayName("Should support Spring AI Alibaba framework")
    void springAiAlibaba() {
        ProjectConfig cfg = new ProjectConfig()
                .name("ali-ai-app")
                .basePackage("com.example.ai")
                .aiFramework(AIFramework.SPRING_AI_ALIBABA);

        cfg.validate();

        assertTrue(cfg.usesSpringAI());
        assertTrue(cfg.usesSpringAIAlibaba());
        assertFalse(cfg.usesLangChain4j());
    }

    @Test
    @DisplayName("Should support database configuration")
    void databaseConfig() {
        ProjectConfig cfg = new ProjectConfig()
                .name("db-app")
                .basePackage("com.example.ai")
                .dbType(DatabaseType.MYSQL)
                .dbHost("192.168.1.100")
                .dbPort(3307)
                .dbName("mydb")
                .dbUsername("admin")
                .dbPassword("secret")
                .ormType(OrmType.JPA);

        assertTrue(cfg.hasDatabase());
        assertEquals(DatabaseType.MYSQL, cfg.dbType());
        assertEquals(3307, cfg.dbPort());
        assertEquals(3307, cfg.effectiveDbPort());
        assertEquals("mydb", cfg.dbName());
        assertEquals("admin", cfg.dbUsername());
        assertTrue(cfg.usesJpa());
        assertFalse(cfg.usesMyBatisPlus());
    }

    @Test
    @DisplayName("Should support Redis cache configuration")
    void redisCacheConfig() {
        ProjectConfig cfg = new ProjectConfig()
                .name("cache-app")
                .basePackage("com.example.ai")
                .cacheType(CacheType.REDIS)
                .redisHost("redis.internal")
                .redisPort(6380)
                .redisPassword("redis123")
                .redisDatabase(1);

        assertTrue(cfg.hasCache());
        assertTrue(cfg.hasRedisCache());
        assertFalse(cfg.hasCaffeineCache());
        assertEquals("redis.internal", cfg.redisHost());
        assertEquals(6380, cfg.redisPort());
        assertEquals("redis123", cfg.redisPassword());
        assertEquals(1, cfg.redisDatabase());
    }

    @Test
    @DisplayName("Should support Nacos discovery only")
    void nacosDiscoveryOnly() {
        ProjectConfig cfg = new ProjectConfig()
                .name("nacos-app")
                .basePackage("com.example.ai")
                .nacosDiscoveryEnabled(true);

        assertTrue(cfg.hasNacos());
        assertTrue(cfg.hasNacosDiscovery());
        assertFalse(cfg.hasNacosConfig());
    }

    @Test
    @DisplayName("Should support Nacos config only")
    void nacosConfigOnly() {
        ProjectConfig cfg = new ProjectConfig()
                .name("nacos-app")
                .basePackage("com.example.ai")
                .nacosConfigEnabled(true);

        assertTrue(cfg.hasNacos());
        assertFalse(cfg.hasNacosDiscovery());
        assertTrue(cfg.hasNacosConfig());
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
    @DisplayName("Should default to no MQ")
    void defaultNoMq() {
        ProjectConfig cfg = new ProjectConfig()
                .name("app")
                .basePackage("com.example.ai");

        assertFalse(cfg.hasMq());
        assertEquals(MqType.NONE, cfg.mqType());
    }

    @Test
    @DisplayName("Should support RabbitMQ configuration")
    void rabbitMqConfig() {
        ProjectConfig cfg = new ProjectConfig()
                .name("mq-app")
                .basePackage("com.example.ai")
                .mqType(MqType.RABBITMQ)
                .mqHost("rabbitmq.internal")
                .mqPort(5672)
                .mqUsername("admin")
                .mqPassword("secret")
                .mqVirtualHost("/ai")
                .mqGroup("ai-consumer");

        assertTrue(cfg.hasMq());
        assertTrue(cfg.isRabbitMq());
        assertFalse(cfg.isRocketMq());
        assertFalse(cfg.isKafka());
        assertEquals(5672, cfg.mqPort());
        assertEquals(5672, cfg.effectiveMqPort());
        assertEquals("admin", cfg.mqUsername());
        assertEquals("secret", cfg.mqPassword());
        assertEquals("/ai", cfg.mqVirtualHost());
        assertEquals("ai-consumer", cfg.mqGroup());
    }

    @Test
    @DisplayName("Should support RocketMQ configuration")
    void rocketMqConfig() {
        ProjectConfig cfg = new ProjectConfig()
                .name("mq-app")
                .basePackage("com.example.ai")
                .mqType(MqType.ROCKETMQ);

        assertTrue(cfg.hasMq());
        assertTrue(cfg.isRocketMq());
        assertFalse(cfg.isRabbitMq());
        assertFalse(cfg.isKafka());
        assertEquals(9876, cfg.effectiveMqPort());
    }

    @Test
    @DisplayName("Should support Kafka configuration")
    void kafkaMqConfig() {
        ProjectConfig cfg = new ProjectConfig()
                .name("mq-app")
                .basePackage("com.example.ai")
                .mqType(MqType.KAFKA);

        assertTrue(cfg.hasMq());
        assertTrue(cfg.isKafka());
        assertFalse(cfg.isRabbitMq());
        assertFalse(cfg.isRocketMq());
        assertEquals(9092, cfg.effectiveMqPort());
    }

    @Test
    @DisplayName("Should use default port when mqPort is 0")
    void defaultMqPort() {
        ProjectConfig cfg = new ProjectConfig()
                .name("mq-app")
                .basePackage("com.example.ai")
                .mqType(MqType.RABBITMQ)
                .mqPort(0);

        assertEquals(5672, cfg.effectiveMqPort());
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
