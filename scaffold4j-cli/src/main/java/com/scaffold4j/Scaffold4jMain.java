package com.scaffold4j;

import com.scaffold4j.cli.GenerateCommand;
import com.scaffold4j.cli.ListProvidersCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * scaffold4j — Java AI Application Scaffold CLI.
 * <p>
 * Quickly generate a production-ready, multi-module Maven project
 * for building AI applications with Spring AI, LangChain4j, and LangGraph4j.
 * </p>
 *
 * <h3>Quick Start</h3>
 * <pre>
 * scaffold4j generate --name=my-ai-app --package=com.example.ai --protocols=rest,mcp --llm-providers=openai,ollama
 * </pre>
 */
@Command(
    name = "scaffold4j",
    description = "Java AI Application Scaffold — 快速生成基于 Java 的 AI 应用项目骨架",
    version = "scaffold4j 1.0.0",
    mixinStandardHelpOptions = true,
    subcommands = {
        GenerateCommand.class,
        ListProvidersCommand.class
    }
)
public class Scaffold4jMain {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Scaffold4jMain())
                .setCaseInsensitiveEnumValuesAllowed(true)
                .execute(args);
        System.exit(exitCode);
    }
}
