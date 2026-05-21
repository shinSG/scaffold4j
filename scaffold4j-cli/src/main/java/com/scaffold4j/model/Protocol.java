package com.scaffold4j.model;

/**
 * Supported application protocols.
 */
public enum Protocol {

    REST("rest", "RESTful API", "Standard HTTP REST endpoints with Spring Web MVC"),
    MCP("mcp", "Model Context Protocol", "MCP server for AI tool/resource exposure (Anthropic standard)"),
    A2A("a2a", "Agent-to-Agent", "Google A2A protocol for multi-agent task delegation"),
    ACP("acp", "Agent Communication Protocol", "IBM/BeeAI ACP for agent communication (merging into A2A)");

    private final String id;
    private final String displayName;
    private final String description;

    Protocol(String id, String displayName, String description) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public String description() { return description; }

    public static Protocol fromId(String id) {
        for (Protocol p : values()) {
            if (p.id.equalsIgnoreCase(id)) return p;
        }
        throw new IllegalArgumentException("Unknown protocol: " + id
                + ". Valid values: rest, mcp, a2a, acp");
    }
}
