package com.scaffold4j.cli;

import com.scaffold4j.model.AIFramework;
import com.scaffold4j.model.LLMProvider;
import com.scaffold4j.model.MqType;
import com.scaffold4j.model.ProjectConfig;
import com.scaffold4j.model.Protocol;
import com.scaffold4j.model.VectorStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenerateCommandTest {

    @Test
    @DisplayName("Should use defaults when optional CLI values are blank")
    void blankOptionalValuesUseDefaults() {
        GenerateCommand command = new GenerateCommand();
        command.name("blank-defaults-app");
        command.basePackage("com.example.blank");
        command.aiFramework("");
        command.vectorStore("");
        command.protocols("");
        command.llmProviders("");
        command.features("");

        ProjectConfig config = command.buildConfig();

        assertEquals(AIFramework.SPRING_AI, config.aiFramework());
        assertEquals(VectorStore.PGVECTOR, config.vectorStore());
        assertTrue(config.hasProtocol(Protocol.REST));
        assertTrue(config.hasLLMProvider(LLMProvider.OPENAI));
    }

    @Test
    @DisplayName("Should ignore MQ connection options when MQ type is none")
    void mqNoneDoesNotRequireConnectionOptions() {
        GenerateCommand command = new GenerateCommand();
        command.name("no-mq-app");
        command.basePackage("com.example.nomq");
        command.mqType("none");
        command.mqHost("");
        command.mqPort(null);
        command.mqUsername("");
        command.mqPassword("");
        command.mqVirtualHost("");
        command.mqGroup("");

        ProjectConfig config = command.buildConfig();

        assertEquals(MqType.NONE, config.mqType());
        assertTrue(!config.hasMq());
    }
}