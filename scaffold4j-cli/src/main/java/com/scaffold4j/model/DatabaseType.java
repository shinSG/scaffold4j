package com.scaffold4j.model;

/**
 * Supported relational database types.
 */
public enum DatabaseType {

    MYSQL("mysql", "MySQL", "MySQL 8.0+",
            "com.mysql.cj.jdbc.Driver",
            "jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf-8",
            3306),
    POSTGRESQL("postgresql", "PostgreSQL", "PostgreSQL 16+",
            "org.postgresql.Driver",
            "jdbc:postgresql://%s:%d/%s",
            5432),
    H2("h2", "H2 (Embedded)", "Local in-memory/file database, no install required",
            "org.h2.Driver",
            "jdbc:h2:file:./data/%s;DB_CLOSE_DELAY=-1;MODE=MySQL",
            0);

    private final String id;
    private final String displayName;
    private final String description;
    private final String driverClassName;
    private final String jdbcUrlTemplate;
    private final int defaultPort;

    DatabaseType(String id, String displayName, String description,
                 String driverClassName, String jdbcUrlTemplate, int defaultPort) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.driverClassName = driverClassName;
        this.jdbcUrlTemplate = jdbcUrlTemplate;
        this.defaultPort = defaultPort;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public String description() { return description; }
    public String driverClassName() { return driverClassName; }
    public int defaultPort() { return defaultPort; }

    /** Build a JDBC URL from host, port, and database name. */
    public String jdbcUrl(String host, int port, String database) {
        return String.format(jdbcUrlTemplate, host, port, database);
    }

    public static DatabaseType fromId(String id) {
        for (DatabaseType t : values()) {
            if (t.id.equalsIgnoreCase(id)) return t;
        }
        throw new IllegalArgumentException("Unknown database type: " + id
                + ". Valid values: mysql, postgresql, h2");
    }
}
