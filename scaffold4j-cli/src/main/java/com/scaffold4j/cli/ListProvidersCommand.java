package com.scaffold4j.cli;

import com.scaffold4j.model.LLMProvider;
import com.scaffold4j.model.Protocol;
import com.scaffold4j.model.VectorStore;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(
    name = "list-providers",
    description = "List all supported LLM providers, vector stores, and protocols.",
    mixinStandardHelpOptions = true
)
public class ListProvidersCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        System.out.println();
        System.out.println("=== LLM Providers ===");
        System.out.printf("%-18s %-30s %s%n", "ID", "Name", "Spring AI Starter");
        System.out.println("-".repeat(90));
        for (LLMProvider p : LLMProvider.values()) {
            System.out.printf("%-18s %-30s %s%n",
                    p.id(),
                    p.displayName(),
                    p.springAiStarter() != null ? p.springAiStarter() : "(via generic adapter)");
        }

        System.out.println();
        System.out.println("=== Vector Stores ===");
        System.out.printf("%-18s %-25s %s%n", "ID", "Name", "Spring AI Starter");
        System.out.println("-".repeat(90));
        for (VectorStore vs : VectorStore.values()) {
            System.out.printf("%-18s %-25s %s%n",
                    vs.id(),
                    vs.displayName(),
                    vs.springAiStarter());
        }

        System.out.println();
        System.out.println("=== Protocols ===");
        System.out.printf("%-10s %-30s %s%n", "ID", "Name", "Description");
        System.out.println("-".repeat(80));
        for (Protocol p : Protocol.values()) {
            System.out.printf("%-10s %-30s %s%n",
                    p.id(),
                    p.displayName(),
                    p.description());
        }

        System.out.println();
        return 0;
    }
}
