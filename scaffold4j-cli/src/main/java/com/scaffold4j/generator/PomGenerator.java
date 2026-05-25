package com.scaffold4j.generator;

import com.scaffold4j.model.AIFramework;
import com.scaffold4j.model.ProjectConfig;
import com.scaffold4j.model.Protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates pom.xml files for the root project and each module.
 */
public class PomGenerator {

    private final ProjectConfig config;

    public PomGenerator(ProjectConfig config) {
        this.config = config;
    }

    /**
     * Generate the root aggregator POM.
     */
    public String generateRootPom() {
        List<String> modules = new ArrayList<>();
        modules.add(config.moduleName("common"));
        modules.add(config.moduleName("domain"));
        modules.add(config.moduleName("infra"));
        modules.add(config.moduleName("app"));
        modules.add(config.moduleName("api"));
        modules.add(config.moduleName("bootstrap"));

        String modulesXml = modules.stream()
                .map(m -> "        <module>" + m + "</module>")
                .collect(Collectors.joining("\n"));

        StringBuilder deps = new StringBuilder();

        // Spring AI BOM
        if (config.usesSpringAI()) {
            deps.append("""
                    <!-- Spring AI BOM -->
                            <dependency>
                                <groupId>org.springframework.ai</groupId>
                                <artifactId>spring-ai-bom</artifactId>
                                <version>${spring-ai.version}</version>
                                <type>pom</type>
                                <scope>import</scope>
                            </dependency>
                    """);
        }

        // LangChain4j BOM
        if (config.usesLangChain4j()) {
            deps.append("""
                    <!-- LangChain4j BOM -->
                            <dependency>
                                <groupId>dev.langchain4j</groupId>
                                <artifactId>langchain4j-bom</artifactId>
                                <version>${langchain4j.version}</version>
                                <type>pom</type>
                                <scope>import</scope>
                            </dependency>
                    """);
        }

        // Spring AI Alibaba BOM
        if (config.usesSpringAIAlibaba()) {
            deps.append("""
                    <!-- Spring AI Alibaba BOM -->
                            <dependency>
                                <groupId>com.alibaba.cloud.ai</groupId>
                                <artifactId>spring-ai-alibaba-bom</artifactId>
                                <version>${spring-ai.version}</version>
                                <type>pom</type>
                                <scope>import</scope>
                            </dependency>
                    """);
        }

        // Nacos BOM (via Spring Cloud Alibaba)
        if (config.hasNacos()) {
            deps.append("""
                    <!-- Spring Cloud Alibaba BOM -->
                            <dependency>
                                <groupId>com.alibaba.cloud</groupId>
                                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                                <version>${spring-cloud-alibaba.version}</version>
                                <type>pom</type>
                                <scope>import</scope>
                            </dependency>
                    """);
        }

        // Spring Cloud BOM (needed for Nacos)
        if (config.hasNacos()) {
            deps.append("""
                    <!-- Spring Cloud BOM -->
                            <dependency>
                                <groupId>org.springframework.cloud</groupId>
                                <artifactId>spring-cloud-dependencies</artifactId>
                                <version>${spring-cloud.version}</version>
                                <type>pom</type>
                                <scope>import</scope>
                            </dependency>
                    """);
        }

        // Database driver version management
        if (config.hasDatabase()) {
            deps.append("""
                    <!-- MyBatis-Plus -->
                            <dependency>
                                <groupId>com.baomidou</groupId>
                                <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
                                <version>${mybatis-plus.version}</version>
                            </dependency>
                            <!-- MySQL Driver -->
                            <dependency>
                                <groupId>com.mysql</groupId>
                                <artifactId>mysql-connector-j</artifactId>
                                <version>${mysql-connector.version}</version>
                            </dependency>
                            <!-- PostgreSQL Driver -->
                            <dependency>
                                <groupId>org.postgresql</groupId>
                                <artifactId>postgresql</artifactId>
                                <version>${postgresql.version}</version>
                            </dependency>
                    """);
        }

        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                         https://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>

                    <parent>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-parent</artifactId>
                        <version>${spring.boot.version}</version>
                        <relativePath/>
                    </parent>

                    <groupId>${group.id}</groupId>
                    <artifactId>${artifact.id}</artifactId>
                    <version>${version}</version>
                    <packaging>pom</packaging>

                    <name>${project.name}</name>
                    <description>AI Application generated by scaffold4j</description>

                    <modules>
                ${modules}
                    </modules>

                    <properties>
                        <java.version>${java.version}</java.version>
                        <maven.compiler.parameters>true</maven.compiler.parameters>
                        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                ${aiProps}
                    </properties>

                    <dependencyManagement>
                        <dependencies>
                ${deps}
                        </dependencies>
                    </dependencyManagement>

                    <dependencies>
                        <dependency>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <optional>true</optional>
                        </dependency>
                    </dependencies>

                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <configuration>
                                    <parameters>true</parameters>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """
                .replace("${group.id}", config.effectiveGroupId())
                .replace("${artifact.id}", config.effectiveArtifactId())
                .replace("${version}", config.version())
                .replace("${project.name}", config.name())
                .replace("${spring.boot.version}", config.springBootVersion())
                .replace("${java.version}", String.valueOf(config.javaVersion()))
                .replace("${modules}", modulesXml)
                .replace("${aiProps}", aiProps())
                .replace("${deps}", deps.toString());
    }

    private String aiProps() {
        StringBuilder sb = new StringBuilder();
        sb.append("        <spring-ai.version>1.0.1</spring-ai.version>\n");
        sb.append("        <langchain4j.version>1.0.0-beta1</langchain4j.version>\n");
        sb.append("        <langgraph4j.version>1.2.3</langgraph4j.version>\n");
        if (config.hasNacos()) {
            sb.append("        <spring-cloud.version>").append(config.springCloudVersion()).append("</spring-cloud.version>\n");
            sb.append("        <spring-cloud-alibaba.version>2025.0.0.0</spring-cloud-alibaba.version>\n");
        }
        if (config.hasDatabase()) {
            sb.append("        <mybatis-plus.version>3.5.9</mybatis-plus.version>\n");
            sb.append("        <mysql-connector.version>9.1.0</mysql-connector.version>\n");
            sb.append("        <postgresql.version>42.7.4</postgresql.version>\n");
            sb.append("        <h2.version>2.3.232</h2.version>\n");
        }
        if (config.hasCache()) {
            sb.append("        <caffeine.version>3.1.8</caffeine.version>\n");
        }
        return sb.toString();
    }

    /**
     * Generate a module-level pom.xml.
     * @param moduleDirName e.g. "myapp-common"
     * @param moduleType "common", "domain", "infra", "app", "api", "bootstrap"
     * @param parentModuleName parent dependency (null for common)
     */
    public String generateModulePom(String moduleDirName, String moduleType, String parentModuleName) {
        StringBuilder dependencies = new StringBuilder();

        // Internal module dependency
        if (parentModuleName != null) {
            dependencies.append("""
                    <!-- Internal module -->
                    <dependency>
                        <groupId>${project.groupId}</groupId>
                        <artifactId>${parent}</artifactId>
                        <version>${project.version}</version>
                    </dependency>
                    """.replace("${parent}", parentModuleName));
        }

        // Module-specific dependencies
        switch (moduleType) {
            case "common":
                dependencies.append("""
                        <!-- Jackson JSON utilities -->
                        <dependency>
                            <groupId>com.fasterxml.jackson.core</groupId>
                            <artifactId>jackson-databind</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>com.fasterxml.jackson.datatype</groupId>
                            <artifactId>jackson-datatype-jsr310</artifactId>
                        </dependency>
                        """);
                break;

            case "domain":
                dependencies.append("""
                        <!-- Jakarta Validation -->
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-validation</artifactId>
                        </dependency>
                        """);
                if (config.usesMyBatisPlus()) {
                    dependencies.append("""
                            <!-- MyBatis-Plus annotations for domain entities -->
                            <dependency>
                                <groupId>com.baomidou</groupId>
                                <artifactId>mybatis-plus-annotation</artifactId>
                                <version>${mybatis-plus.version}</version>
                            </dependency>
                            """);
                }
                break;

            case "infra":
                dependencies.append("""
                        <!-- Spring Boot Starter -->
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter</artifactId>
                        </dependency>
                        <!-- WebClient + Reactor for generated LLM provider HTTP adapters -->
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-webflux</artifactId>
                        </dependency>
                        """);

                // Generated LLM providers use direct WebClient HTTP adapters, so provider-specific
                // Spring AI model starters are not required for chat completion calls.

                // LangChain4j dependencies
                if (config.usesLangChain4j()) {
                    dependencies.append("""
                            <dependency>
                                <groupId>dev.langchain4j</groupId>
                                <artifactId>langchain4j</artifactId>
                            </dependency>
                            """);
                    for (var provider : config.llmProviders()) {
                        if (provider.hasLangchain4jSupport()) {
                            dependencies.append("""
                                    <dependency>
                                        <groupId>dev.langchain4j</groupId>
                                        <artifactId>""" + provider.langchain4jModule() + """
                                    </artifactId>
                                    </dependency>
                                    """);
                        }
                    }
                }

                // Vector store dependency
                if (config.usesSpringAI() && config.vectorStore().springAiStarter() != null) {
                    dependencies.append("""
                            <dependency>
                                <groupId>org.springframework.ai</groupId>
                                <artifactId>""" + config.vectorStore().springAiStarter() + """
                            </artifactId>
                            </dependency>
                            """);
                }

                // Nacos Discovery
                if (config.hasNacosDiscovery()) {
                    dependencies.append("""
                            <!-- Nacos Discovery -->
                            <dependency>
                                <groupId>com.alibaba.cloud</groupId>
                                <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
                            </dependency>
                            """);
                }

                // Nacos Config
                if (config.hasNacosConfig()) {
                    dependencies.append("""
                            <!-- Nacos Config -->
                            <dependency>
                                <groupId>com.alibaba.cloud</groupId>
                                <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
                            </dependency>
                            """);
                }

                // Spring AI Alibaba
                if (config.usesSpringAIAlibaba()) {
                    dependencies.append("""
                            <!-- Spring AI Alibaba (DashScope) -->
                            <dependency>
                                <groupId>com.alibaba.cloud.ai</groupId>
                                <artifactId>spring-ai-alibaba-starter</artifactId>
                            </dependency>
                            """);
                }

                // LangChain4j Spring Boot Starter
                if (config.usesLangChain4j()) {
                    dependencies.append("""
                            <!-- LangChain4j Spring Boot Starter -->
                            <dependency>
                                <groupId>dev.langchain4j</groupId>
                                <artifactId>langchain4j-spring-boot-starter</artifactId>
                            </dependency>
                            """);
                }

                // Database driver
                if (config.hasDatabase()) {
                    switch (config.dbType()) {
                        case MYSQL:
                            dependencies.append("""
                                    <!-- MySQL Driver -->
                                    <dependency>
                                        <groupId>com.mysql</groupId>
                                        <artifactId>mysql-connector-j</artifactId>
                                        <scope>runtime</scope>
                                    </dependency>
                                    """);
                            break;
                        case POSTGRESQL:
                            dependencies.append("""
                                    <!-- PostgreSQL Driver -->
                                    <dependency>
                                        <groupId>org.postgresql</groupId>
                                        <artifactId>postgresql</artifactId>
                                        <scope>runtime</scope>
                                    </dependency>
                                    """);
                            break;
                        case H2:
                            dependencies.append("""
                                    <!-- H2 Embedded Database -->
                                    <dependency>
                                        <groupId>com.h2database</groupId>
                                        <artifactId>h2</artifactId>
                                        <scope>runtime</scope>
                                    </dependency>
                                    """);
                            break;
                    }
                }

                // ORM framework
                if (config.usesMyBatisPlus()) {
                    dependencies.append("""
                            <!-- MyBatis-Plus -->
                            <dependency>
                                <groupId>com.baomidou</groupId>
                                <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
                            </dependency>
                            <dependency>
                                <groupId>com.baomidou</groupId>
                                <artifactId>mybatis-plus-jsqlparser</artifactId>
                                <version>${mybatis-plus.version}</version>
                            </dependency>
                            """);
                }
                if (config.usesJpa()) {
                    dependencies.append("""
                            <!-- Spring Data JPA -->
                            <dependency>
                                <groupId>org.springframework.boot</groupId>
                                <artifactId>spring-boot-starter-data-jpa</artifactId>
                            </dependency>
                            """);
                }

                // Redis Cache
                if (config.hasRedisCache()) {
                    dependencies.append("""
                            <!-- Spring Data Redis -->
                            <dependency>
                                <groupId>org.springframework.boot</groupId>
                                <artifactId>spring-boot-starter-data-redis</artifactId>
                            </dependency>
                            """);
                }

                // Caffeine Cache
                if (config.hasCaffeineCache()) {
                    dependencies.append("""
                            <!-- Caffeine Cache -->
                            <dependency>
                                <groupId>com.github.ben-manes.caffeine</groupId>
                                <artifactId>caffeine</artifactId>
                            </dependency>
                            """);
                }

                // Spring Cache abstraction
                if (config.hasCache()) {
                    dependencies.append("""
                            <!-- Spring Cache -->
                            <dependency>
                                <groupId>org.springframework.boot</groupId>
                                <artifactId>spring-boot-starter-cache</artifactId>
                            </dependency>
                            """);
                }

                // WebSocket
                if (config.hasFeature(com.scaffold4j.model.Feature.WEBSOCKET)) {
                    dependencies.append("""
                            <!-- WebSocket -->
                            <dependency>
                                <groupId>org.springframework.boot</groupId>
                                <artifactId>spring-boot-starter-websocket</artifactId>
                            </dependency>
                            """);
                }

                // MQ dependencies
                if (config.hasMq()) {
                    dependencies.append("""
                            <!-- Jackson for message serialization -->
                            <dependency>
                                <groupId>com.fasterxml.jackson.core</groupId>
                                <artifactId>jackson-databind</artifactId>
                            </dependency>
                            """);
                    switch (config.mqType()) {
                        case RABBITMQ:
                            dependencies.append("""
                                    <!-- Spring AMQP (RabbitMQ) -->
                                    <dependency>
                                        <groupId>org.springframework.boot</groupId>
                                        <artifactId>spring-boot-starter-amqp</artifactId>
                                    </dependency>
                                    """);
                            break;
                        case ROCKETMQ:
                            dependencies.append("""
                                    <!-- RocketMQ Spring Boot Starter -->
                                    <dependency>
                                        <groupId>org.apache.rocketmq</groupId>
                                        <artifactId>rocketmq-spring-boot-starter</artifactId>
                                        <version>2.3.0</version>
                                    </dependency>
                                    """);
                            break;
                        case KAFKA:
                            dependencies.append("""
                                    <!-- Spring Kafka -->
                                    <dependency>
                                        <groupId>org.springframework.kafka</groupId>
                                        <artifactId>spring-kafka</artifactId>
                                    </dependency>
                                    """);
                            break;
                    }
                }
                break;

            case "app":
                dependencies.append("""
                        <!-- Spring Context -->
                        <dependency>
                            <groupId>org.springframework</groupId>
                            <artifactId>spring-context</artifactId>
                        </dependency>
                        """);

                if (config.usesLangChain4j()) {
                    dependencies.append("""
                            <!-- LangGraph4j for Agent Workflow -->
                            <dependency>
                                <groupId>org.bsc.langgraph4j</groupId>
                                <artifactId>langgraph4j-core</artifactId>
                                <version>1.2.3</version>
                            </dependency>
                            """);
                }
                break;

            case "api":
                dependencies.append("""
                        <!-- Spring Web MVC -->
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-web</artifactId>
                        </dependency>
                        <!-- Spring WebFlux (for reactive streaming) -->
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-webflux</artifactId>
                        </dependency>
                        """);

                // MCP dependencies
                if (config.hasProtocol(Protocol.MCP) && config.usesSpringAI()) {
                    dependencies.append("""
                            <!-- MCP Server -->
                            <dependency>
                                <groupId>org.springframework.ai</groupId>
                                <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
                            </dependency>
                            <!-- MCP Client -->
                            <dependency>
                                <groupId>org.springframework.ai</groupId>
                                <artifactId>spring-ai-starter-mcp-client</artifactId>
                            </dependency>
                            """);
                }
                break;

            case "bootstrap":
                dependencies.append("""
                        <!-- Spring Boot Starter -->
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter</artifactId>
                        </dependency>
                        <!-- Spring Boot Actuator -->
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-actuator</artifactId>
                        </dependency>
                        <!-- Spring Boot DevTools -->
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-devtools</artifactId>
                            <optional>true</optional>
                        </dependency>
                        <!-- Test -->
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-test</artifactId>
                            <scope>test</scope>
                        </dependency>
                        """);

                if (config.hasNacosConfig()) {
                    dependencies.append("""
                            <!-- Spring Cloud Bootstrap (required for Nacos Config) -->
                            <dependency>
                                <groupId>org.springframework.cloud</groupId>
                                <artifactId>spring-cloud-starter-bootstrap</artifactId>
                            </dependency>
                            """);
                }
                break;
        }

        boolean isBootstrap = "bootstrap".equals(moduleType);
        String packaging = isBootstrap ? "jar" : "jar";
        String bootPlugin = isBootstrap ? """
                <!-- Spring Boot Maven Plugin -->
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                    <configuration>
                        <excludes>
                            <exclude>
                                <groupId>org.projectlombok</groupId>
                                <artifactId>lombok</artifactId>
                            </exclude>
                        </excludes>
                    </configuration>
                </plugin>
                """ : "";

        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                         https://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>

                    <parent>
                        <groupId>${project.groupId}</groupId>
                        <artifactId>${project.artifactId}</artifactId>
                        <version>${project.version}</version>
                    </parent>

                    <artifactId>${module.artifactId}</artifactId>
                    <packaging>${packaging}</packaging>

                    <dependencies>
                ${deps}
                    </dependencies>

                    <build>
                        <plugins>
                ${boot.plugin}
                        </plugins>
                    </build>
                </project>
                """
                .replace("${project.groupId}", config.effectiveGroupId())
                .replace("${project.artifactId}", config.effectiveArtifactId())
                .replace("${project.version}", config.version())
                .replace("${module.artifactId}", moduleDirName)
                .replace("${packaging}", packaging)
                .replace("${deps}", dependencies.toString())
                .replace("${boot.plugin}", bootPlugin);
    }
}
