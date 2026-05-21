package com.scaffold4j.model;

/**
 * Optional features that can be included in the generated project.
 */
public enum Feature {

    MEMORY("memory", "Chat Memory", "Multi-turn conversation history with token/message window management"),
    RAG("rag", "RAG Pipeline", "Retrieval-Augmented Generation: document loading, chunking, embedding, retrieval"),
    SSE("sse", "SSE Streaming", "Server-Sent Events for real-time streaming AI responses"),
    WEBSOCKET("websocket", "WebSocket", "WebSocket endpoints for bidirectional real-time AI chat");

    private final String id;
    private final String displayName;
    private final String description;

    Feature(String id, String displayName, String description) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public String description() { return description; }

    public static Feature fromId(String id) {
        for (Feature f : values()) {
            if (f.id.equalsIgnoreCase(id)) return f;
        }
        throw new IllegalArgumentException("Unknown feature: " + id
                + ". Valid values: memory, rag, sse, websocket");
    }
}
