package com.scaffold4j.model;

/**
 * Supported vector store backends.
 */
public enum VectorStore {

    PGVECTOR("pgvector", "PGVector", "PostgreSQL with pgvector extension",
            "spring-ai-starter-vector-store-pgvector", "langchain4j-pgvector"),
    MILVUS("milvus", "Milvus", "High-performance vector database for large-scale similarity search",
            "spring-ai-starter-vector-store-milvus", "langchain4j-milvus"),
    CHROMA("chroma", "Chroma", "Lightweight open-source embedding database",
            "spring-ai-starter-vector-store-chroma", "langchain4j-chroma"),
    PINECONE("pinecone", "Pinecone", "Managed vector database (cloud)",
            "spring-ai-starter-vector-store-pinecone", "langchain4j-pinecone"),
    ELASTICSEARCH("elasticsearch", "Elasticsearch", "Distributed search engine with vector support",
            "spring-ai-starter-vector-store-elasticsearch", "langchain4j-elasticsearch"),
    REDIS("redis", "Redis Stack", "In-memory data store with vector search",
            "spring-ai-starter-vector-store-redis", "langchain4j-redis"),
    WEAVIATE("weaviate", "Weaviate", "Open-source vector database with built-in ML",
            "spring-ai-starter-vector-store-weaviate", "langchain4j-weaviate"),
    QDRANT("qdrant", "Qdrant", "Vector similarity search engine written in Rust",
            "spring-ai-starter-vector-store-qdrant", "langchain4j-qdrant"),
    SIMPLE("simple", "Simple (In-Memory)", "In-memory vector store for development/testing",
            "spring-ai-starter-vector-store-simple", null);

    private final String id;
    private final String displayName;
    private final String description;
    private final String springAiStarter;
    private final String langchain4jModule;

    VectorStore(String id, String displayName, String description,
                String springAiStarter, String langchain4jModule) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.springAiStarter = springAiStarter;
        this.langchain4jModule = langchain4jModule;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public String description() { return description; }
    public String springAiStarter() { return springAiStarter; }
    public String langchain4jModule() { return langchain4jModule; }

    public static VectorStore fromId(String id) {
        for (VectorStore vs : values()) {
            if (vs.id.equalsIgnoreCase(id)) return vs;
        }
        throw new IllegalArgumentException("Unknown vector store: " + id
                + ". Use 'scaffold4j list-providers' to see available vector stores.");
    }
}
