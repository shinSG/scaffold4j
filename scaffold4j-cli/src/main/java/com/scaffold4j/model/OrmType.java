package com.scaffold4j.model;

/**
 * Supported ORM frameworks.
 */
public enum OrmType {

    MYBATIS_PLUS("mybatis-plus", "MyBatis-Plus",
            "Flexible SQL + auto CRUD with code generator, pagination plugin"),
    JPA("jpa", "Spring Data JPA",
            "Declarative query methods + auto DDL with Hibernate");

    private final String id;
    private final String displayName;
    private final String description;

    OrmType(String id, String displayName, String description) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public String description() { return description; }

    public static OrmType fromId(String id) {
        for (OrmType t : values()) {
            if (t.id.equalsIgnoreCase(id)) return t;
        }
        throw new IllegalArgumentException("Unknown ORM type: " + id
                + ". Valid values: mybatis-plus, jpa");
    }
}
