package com.scaffold4j.model;

/**
 * Supported cache backends.
 */
public enum CacheType {

    REDIS("redis", "Redis", "Distributed cache via Spring Data Redis + Lettuce"),
    CAFFEINE("caffeine", "Caffeine", "High-performance local in-memory cache"),
    NONE("none", "None", "No caching");

    private final String id;
    private final String displayName;
    private final String description;

    CacheType(String id, String displayName, String description) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public String description() { return description; }

    public static CacheType fromId(String id) {
        for (CacheType t : values()) {
            if (t.id.equalsIgnoreCase(id)) return t;
        }
        throw new IllegalArgumentException("Unknown cache type: " + id
                + ". Valid values: redis, caffeine, none");
    }
}
