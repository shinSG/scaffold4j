package com.scaffold4j.model;

/**
 * Supported message queue backends.
 */
public enum MqType {

    RABBITMQ("rabbitmq", "RabbitMQ", "Spring AMQP message queue", 5672),
    ROCKETMQ("rocketmq", "RocketMQ", "Apache RocketMQ via Spring Cloud Stream", 9876),
    KAFKA("kafka", "Kafka", "Apache Kafka via Spring Kafka", 9092),
    NONE("none", "None", "No message queue", 0);

    private final String id;
    private final String displayName;
    private final String description;
    private final int defaultPort;

    MqType(String id, String displayName, String description, int defaultPort) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.defaultPort = defaultPort;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public String description() { return description; }
    public int defaultPort() { return defaultPort; }

    public static MqType fromId(String id) {
        for (MqType t : values()) {
            if (t.id.equalsIgnoreCase(id)) return t;
        }
        throw new IllegalArgumentException("Unknown MQ type: " + id
                + ". Valid values: rabbitmq, rocketmq, kafka, none");
    }
}
