package com.scaffold4j.generator;

import com.scaffold4j.model.LLMProvider;
import com.scaffold4j.model.ProjectConfig;

/**
 * Generates Java source files for each module.
 * Each method returns the file content as a String.
 */
public class ModuleGenerator {

    private final ProjectConfig config;

    public ModuleGenerator(ProjectConfig config) {
        this.config = config;
    }

    private String pkg(String subPackage) {
        return config.basePackage() + "." + subPackage;
    }

    // ==================== common module ====================

    public String generateErrorCode(String pkg) {
        return """
                package %s.common.constant;

                public enum ErrorCode {
                    SUCCESS(0, "Success"),
                    BAD_REQUEST(400, "Bad Request"),
                    UNAUTHORIZED(401, "Unauthorized"),
                    FORBIDDEN(403, "Forbidden"),
                    NOT_FOUND(404, "Not Found"),
                    INTERNAL_ERROR(500, "Internal Server Error"),
                    AI_SERVICE_ERROR(1001, "AI Service Error"),
                    LLM_PROVIDER_ERROR(1002, "LLM Provider Error"),
                    VECTOR_STORE_ERROR(1003, "Vector Store Error"),
                    RAG_PIPELINE_ERROR(1004, "RAG Pipeline Error");

                    private final int code;
                    private final String message;

                    ErrorCode(int code, String message) {
                        this.code = code;
                        this.message = message;
                    }

                    public int code() { return code; }
                    public String message() { return message; }
                }
                """.formatted(pkg);
    }

    public String generateCommonConstant(String pkg) {
        return """
                package %s.common.constant;

                public final class CommonConstant {
                    private CommonConstant() {}

                    public static final String TRACE_ID_HEADER = "X-Trace-Id";
                    public static final String DEFAULT_CHARSET = "UTF-8";
                    public static final int DEFAULT_PAGE_SIZE = 20;
                    public static final int MAX_PAGE_SIZE = 100;

                    // Redis key prefixes
                    public static final String REDIS_KEY_PREFIX = "%s:";
                    public static final String CACHE_AI_RESPONSE = "ai:response:";
                    public static final String CACHE_USER = "user:";
                    public static final String CACHE_SESSION = "session:";
                }
                """.formatted(pkg, config.effectiveArtifactId());
    }

    public String generateBaseException(String pkg) {
        return """
                package %s.common.exception;

                import %s.common.constant.ErrorCode;

                public class BaseException extends RuntimeException {
                    private final int code;

                    public BaseException(ErrorCode errorCode) {
                        super(errorCode.message());
                        this.code = errorCode.code();
                    }

                    public BaseException(ErrorCode errorCode, String message) {
                        super(message);
                        this.code = errorCode.code();
                    }

                    public BaseException(int code, String message) {
                        super(message);
                        this.code = code;
                    }

                    public int code() { return code; }
                }
                """.formatted(pkg, pkg);
    }

    public String generateAIException(String pkg) {
        return """
                package %s.common.exception;

                import %s.common.constant.ErrorCode;

                public class AIException extends BaseException {
                    public AIException(ErrorCode errorCode, String detail) {
                        super(errorCode, detail);
                    }

                    public AIException(String message) {
                        super(ErrorCode.AI_SERVICE_ERROR, message);
                    }

                    public AIException(String message, Throwable cause) {
                        super(ErrorCode.AI_SERVICE_ERROR, message);
                        initCause(cause);
                    }
                }
                """.formatted(pkg, pkg);
    }

    public String generateErrorResponse(String pkg) {
        return """
                package %s.common.exception;

                public class ErrorResponse {
                    private int code;
                    private String message;
                    private String traceId;
                    private long timestamp;

                    public ErrorResponse() {
                        this.timestamp = System.currentTimeMillis();
                    }

                    public static ErrorResponse of(int code, String message) {
                        ErrorResponse r = new ErrorResponse();
                        r.code = code;
                        r.message = message;
                        return r;
                    }

                    public int getCode() { return code; }
                    public void setCode(int code) { this.code = code; }
                    public String getMessage() { return message; }
                    public void setMessage(String message) { this.message = message; }
                    public String getTraceId() { return traceId; }
                    public void setTraceId(String traceId) { this.traceId = traceId; }
                    public long getTimestamp() { return timestamp; }
                }
                """.formatted(pkg);
    }

    public String generateResult(String pkg) {
        return """
                package %s.common.result;

                public class Result<T> {
                    private int code;
                    private String message;
                    private T data;
                    private long timestamp;

                    private Result() {
                        this.timestamp = System.currentTimeMillis();
                    }

                    public static <T> Result<T> success(T data) {
                        Result<T> r = new Result<>();
                        r.code = 0;
                        r.message = "success";
                        r.data = data;
                        return r;
                    }

                    public static <T> Result<T> error(int code, String message) {
                        Result<T> r = new Result<>();
                        r.code = code;
                        r.message = message;
                        return r;
                    }

                    public int getCode() { return code; }
                    public String getMessage() { return message; }
                    public T getData() { return data; }
                    public long getTimestamp() { return timestamp; }
                }
                """.formatted(pkg);
    }

    public String generatePageResult(String pkg) {
        return """
                package %s.common.result;

                import java.util.List;

                public class PageResult<T> {
                    private List<T> records;
                    private long total;
                    private int page;
                    private int size;

                    public PageResult() {}

                    public PageResult(List<T> records, long total, int page, int size) {
                        this.records = records;
                        this.total = total;
                        this.page = page;
                        this.size = size;
                    }

                    public List<T> getRecords() { return records; }
                    public void setRecords(List<T> records) { this.records = records; }
                    public long getTotal() { return total; }
                    public void setTotal(long total) { this.total = total; }
                    public int getPage() { return page; }
                    public void setPage(int page) { this.page = page; }
                    public int getSize() { return size; }
                    public void setSize(int size) { this.size = size; }
                }
                """.formatted(pkg);
    }

    public String generateJsonUtils(String pkg) {
        return """
                package %s.common.util;

                import com.fasterxml.jackson.core.JsonProcessingException;
                import com.fasterxml.jackson.databind.ObjectMapper;
                import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

                public final class JsonUtils {
                    private static final ObjectMapper MAPPER = new ObjectMapper()
                            .registerModule(new JavaTimeModule());

                    private JsonUtils() {}

                    public static String toJson(Object obj) {
                        try {
                            return MAPPER.writeValueAsString(obj);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException("JSON serialization failed", e);
                        }
                    }

                    public static <T> T fromJson(String json, Class<T> clazz) {
                        try {
                            return MAPPER.readValue(json, clazz);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException("JSON deserialization failed", e);
                        }
                    }
                }
                """.formatted(pkg);
    }

    // ==================== domain module ====================

    public String generateChatMessage(String pkg) {
        return """
                package %s.domain.model;

                import %s.domain.enums.MessageRole;
                import java.time.Instant;

                public class ChatMessage {
                    private String id;
                    private MessageRole role;
                    private String content;
                    private String conversationId;
                    private Instant createdAt;

                    public ChatMessage() {}

                    public ChatMessage(MessageRole role, String content) {
                        this.role = role;
                        this.content = content;
                        this.createdAt = Instant.now();
                    }

                    public static ChatMessage user(String content) {
                        return new ChatMessage(MessageRole.USER, content);
                    }

                    public static ChatMessage assistant(String content) {
                        return new ChatMessage(MessageRole.ASSISTANT, content);
                    }

                    public static ChatMessage system(String content) {
                        return new ChatMessage(MessageRole.SYSTEM, content);
                    }

                    // getters & setters
                    public String getId() { return id; }
                    public void setId(String id) { this.id = id; }
                    public MessageRole getRole() { return role; }
                    public void setRole(MessageRole role) { this.role = role; }
                    public String getContent() { return content; }
                    public void setContent(String content) { this.content = content; }
                    public String getConversationId() { return conversationId; }
                    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
                    public Instant getCreatedAt() { return createdAt; }
                    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
                }
                """.formatted(pkg, pkg);
    }

    public String generateConversation(String pkg) {
        return """
                package %s.domain.model;

                import java.time.Instant;
                import java.util.ArrayList;
                import java.util.List;

                public class Conversation {
                    private String id;
                    private String title;
                    private List<ChatMessage> messages = new ArrayList<>();
                    private Instant createdAt;
                    private Instant updatedAt;

                    public Conversation() {
                        this.createdAt = Instant.now();
                        this.updatedAt = Instant.now();
                    }

                    public void addMessage(ChatMessage message) {
                        messages.add(message);
                        updatedAt = Instant.now();
                    }

                    // getters & setters
                    public String getId() { return id; }
                    public void setId(String id) { this.id = id; }
                    public String getTitle() { return title; }
                    public void setTitle(String title) { this.title = title; }
                    public List<ChatMessage> getMessages() { return messages; }
                    public void setMessages(List<ChatMessage> messages) { this.messages = messages; }
                    public Instant getCreatedAt() { return createdAt; }
                    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
                    public Instant getUpdatedAt() { return updatedAt; }
                    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
                }
                """.formatted(pkg);
    }

    public String generateChatRequest(String pkg) {
        return """
                package %s.domain.dto;

                import jakarta.validation.constraints.NotBlank;

                public class ChatRequest {
                    @NotBlank(message = "Message cannot be blank")
                    private String message;

                    private String conversationId;
                    private String provider;

                    public String getMessage() { return message; }
                    public void setMessage(String message) { this.message = message; }
                    public String getConversationId() { return conversationId; }
                    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
                    public String getProvider() { return provider; }
                    public void setProvider(String provider) { this.provider = provider; }
                }
                """.formatted(pkg);
    }

    public String generateChatResponse(String pkg) {
        return """
                package %s.domain.dto;

                public class ChatResponse {
                    private String id;
                    private String content;
                    private String conversationId;
                    private String model;
                    private int tokensUsed;

                    public String getId() { return id; }
                    public void setId(String id) { this.id = id; }
                    public String getContent() { return content; }
                    public void setContent(String content) { this.content = content; }
                    public String getConversationId() { return conversationId; }
                    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
                    public String getModel() { return model; }
                    public void setModel(String model) { this.model = model; }
                    public int getTokensUsed() { return tokensUsed; }
                    public void setTokensUsed(int tokensUsed) { this.tokensUsed = tokensUsed; }
                }
                """.formatted(pkg);
    }

    public String generateStreamData(String pkg) {
        return """
                package %s.domain.dto;

                public class StreamData {
                    private String id;
                    private String content;
                    private String event;
                    private boolean done;

                    public StreamData() {}

                    public StreamData(String content, String event) {
                        this.content = content;
                        this.event = event;
                    }

                    public String getId() { return id; }
                    public void setId(String id) { this.id = id; }
                    public String getContent() { return content; }
                    public void setContent(String content) { this.content = content; }
                    public String getEvent() { return event; }
                    public void setEvent(String event) { this.event = event; }
                    public boolean isDone() { return done; }
                    public void setDone(boolean done) { this.done = done; }
                }
                """.formatted(pkg);
    }

    public String generateMessageRole(String pkg) {
        return """
                package %s.domain.enums;

                public enum MessageRole {
                    SYSTEM, USER, ASSISTANT, TOOL
                }
                """.formatted(pkg);
    }

    public String generateLLMProviderType(String pkg) {
        var ids = config.llmProviders().stream()
                .map(p -> p.name())
                .collect(java.util.stream.Collectors.joining(", "));
        return """
                package %s.domain.enums;

                public enum LLMProviderType {
                    %s
                }
                """.formatted(pkg, ids);
    }

    // ==================== infra module ====================

    public String generateAppConfig(String pkg) {
        return """
                package %s.infra.config;

                import org.springframework.context.annotation.Configuration;
                import org.springframework.scheduling.annotation.EnableAsync;

                @Configuration
                @EnableAsync
                public class AppConfig {
                }
                """.formatted(pkg);
    }

    public String generateLLMProviderConfig(String pkg) {
        return """
                package %s.infra.config;

                import %s.infra.llm.LLMProviderFactory;
                import org.springframework.context.annotation.Bean;
                import org.springframework.context.annotation.Configuration;

                @Configuration
                public class LLMProviderConfig {

                    @Bean
                    public LLMProviderFactory llmProviderFactory() {
                        return new LLMProviderFactory();
                    }
                }
                """.formatted(pkg, pkg);
    }

    public String generateVectorStoreConfig(String pkg) {
        return """
                package %s.infra.config;

                import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
                import org.springframework.context.annotation.Configuration;

                @Configuration
                @ConditionalOnProperty(prefix = "scaffold4j.ai.rag", name = "enabled", havingValue = "true")
                public class VectorStoreConfig {
                }
                """.formatted(pkg);
    }

    public String generateLLMProviderAdapter(String pkg) {
        return """
                package %s.infra.llm;

                import %s.domain.model.ChatMessage;
                import reactor.core.publisher.Flux;

                import java.util.List;

                /**
                 * Unified adapter interface for all LLM providers.
                 * Each provider implements this to normalize access across
                 * Spring AI, LangChain4j, and direct API calls.
                 */
                public interface LLMProviderAdapter {

                    /** Synchronous chat completion. */
                    String chat(String systemPrompt, String userMessage);

                    /** Streaming chat completion via reactive streams. */
                    Flux<String> chatStream(String systemPrompt, String userMessage);

                    /** Chat with full conversation history. */
                    String chatWithHistory(String systemPrompt, List<ChatMessage> history, String userMessage);

                    /** Provider identifier. */
                    String providerName();

                    /** Whether this provider supports streaming. */
                    default boolean supportsStreaming() { return true; }

                    /** Whether this provider supports function calling. */
                    default boolean supportsFunctionCalling() { return true; }
                }
                """.formatted(pkg, pkg);
    }

    public String generateLLMProviderFactory(String pkg) {
        var aiFramework = config.aiFramework().name();
        return """
                package %s.infra.llm;

                import %s.domain.enums.LLMProviderType;
                import java.util.Map;
                import java.util.concurrent.ConcurrentHashMap;

                /**
                 * Factory that maintains a registry of LLMProviderAdapter instances,
                 * keyed by provider type. Supports runtime provider switching.
                 *
                 * AI Framework: %s
                 */
                public class LLMProviderFactory {

                    private final Map<LLMProviderType, LLMProviderAdapter> adapters = new ConcurrentHashMap<>();

                    public void register(LLMProviderType type, LLMProviderAdapter adapter) {
                        adapters.put(type, adapter);
                    }

                    public LLMProviderAdapter getAdapter(LLMProviderType type) {
                        LLMProviderAdapter adapter = adapters.get(type);
                        if (adapter == null) {
                            throw new IllegalArgumentException("No LLM adapter registered for: " + type);
                        }
                        return adapter;
                    }

                    public LLMProviderAdapter getDefaultAdapter() {
                        return adapters.values().iterator().next();
                    }
                }
                """.formatted(pkg, pkg, aiFramework);
    }

    public String generateProviderAdapter(String pkg, LLMProvider provider) {
        String className = capitalize(provider.id()) + "Adapter";
        return """
                package %s.infra.llm;

                import %s.infra.llm.LLMProviderAdapter;
                import %s.domain.model.ChatMessage;
                import org.springframework.stereotype.Component;
                import reactor.core.publisher.Flux;

                import java.util.List;

                @Component
                public class %s implements LLMProviderAdapter {

                    @Override
                    public String chat(String systemPrompt, String userMessage) {
                        // TODO: Implement %s chat using Spring AI ChatClient or LangChain4j ChatLanguageModel
                        throw new UnsupportedOperationException("%s adapter not yet configured. Set env: %s");
                    }

                    @Override
                    public Flux<String> chatStream(String systemPrompt, String userMessage) {
                        // TODO: Implement streaming chat
                        throw new UnsupportedOperationException("Streaming not yet configured for %s");
                    }

                    @Override
                    public String chatWithHistory(String systemPrompt, List<ChatMessage> history, String userMessage) {
                        // TODO: Implement chat with conversation history
                        throw new UnsupportedOperationException("History chat not yet configured for %s");
                    }

                    @Override
                    public String providerName() {
                        return "%s";
                    }
                }
                """.formatted(pkg, pkg, pkg, className,
                        provider.displayName(), provider.displayName(),
                        provider.envVar() != null ? provider.envVar() : "N/A",
                        provider.id(), provider.id(), provider.id());
    }

    public String generateChatMemoryStore(String pkg) {
        return """
                package %s.infra.memory;

                import %s.domain.model.ChatMessage;
                import %s.domain.model.Conversation;
                import org.springframework.stereotype.Component;

                import java.util.List;
                import java.util.Map;
                import java.util.concurrent.ConcurrentHashMap;

                /**
                 * In-memory chat history store.
                 * Replace with Redis/DB-backed implementation for production.
                 */
                @Component
                public class ChatMemoryStore {

                    private final Map<String, Conversation> store = new ConcurrentHashMap<>();

                    public Conversation getOrCreate(String conversationId) {
                        return store.computeIfAbsent(conversationId, k -> new Conversation());
                    }

                    public void appendMessage(String conversationId, ChatMessage message) {
                        Conversation conv = getOrCreate(conversationId);
                        conv.addMessage(message);
                    }

                    public List<ChatMessage> getHistory(String conversationId, int maxMessages) {
                        Conversation conv = store.get(conversationId);
                        if (conv == null) return List.of();
                        List<ChatMessage> msgs = conv.getMessages();
                        int size = msgs.size();
                        int from = Math.max(0, size - maxMessages);
                        return msgs.subList(from, size);
                    }

                    public void delete(String conversationId) {
                        store.remove(conversationId);
                    }
                }
                """.formatted(pkg, pkg, pkg);
    }

    public String generateConversationRepository(String pkg) {
        return """
                package %s.infra.memory;

                import %s.domain.model.Conversation;
                import org.springframework.stereotype.Repository;

                import java.util.Optional;

                /**
                 * Repository interface for conversation persistence.
                 * Implement with JPA/MyBatis/Redis for production use.
                 */
                @Repository
                public interface ConversationRepository {
                    Optional<Conversation> findById(String id);
                    Conversation save(Conversation conversation);
                    void deleteById(String id);
                }
                """.formatted(pkg, pkg);
    }

    public String generateDocumentLoader(String pkg) {
        return """
                package %s.infra.rag;

                import org.springframework.stereotype.Component;
                import java.io.IOException;
                import java.nio.file.Files;
                import java.nio.file.Path;
                import java.util.List;

                /**
                 * Loads documents from various sources (files, URLs, etc.) for RAG ingestion.
                 */
                @Component
                public class DocumentLoader {

                    public String loadTextFile(Path filePath) throws IOException {
                        return Files.readString(filePath);
                    }

                    public List<String> loadDirectory(Path dirPath) throws IOException {
                        try (var stream = Files.list(dirPath)) {
                            return stream
                                    .filter(Files::isRegularFile)
                                    .filter(p -> p.toString().endsWith(".txt") || p.toString().endsWith(".md"))
                                    .map(p -> {
                                        try { return Files.readString(p); }
                                        catch (IOException e) { throw new RuntimeException(e); }
                                    })
                                    .toList();
                        }
                    }

                    public String loadUrl(String url) {
                        // TODO: Implement URL content fetching
                        throw new UnsupportedOperationException("URL loading not yet implemented");
                    }
                }
                """.formatted(pkg);
    }

    public String generateTextSplitter(String pkg) {
        return """
                package %s.infra.rag;

                import org.springframework.stereotype.Component;
                import java.util.ArrayList;
                import java.util.List;

                /**
                 * Splits documents into chunks for embedding.
                 */
                @Component
                public class TextSplitter {

                    private final int maxChunkSize;
                    private final int overlap;

                    public TextSplitter() {
                        this.maxChunkSize = 1000;
                        this.overlap = 200;
                    }

                    public TextSplitter(int maxChunkSize, int overlap) {
                        this.maxChunkSize = maxChunkSize;
                        this.overlap = overlap;
                    }

                    public List<String> split(String text) {
                        List<String> chunks = new ArrayList<>();
                        if (text.length() <= maxChunkSize) {
                            chunks.add(text);
                            return chunks;
                        }

                        int start = 0;
                        while (start < text.length()) {
                            int end = Math.min(start + maxChunkSize, text.length());
                            chunks.add(text.substring(start, end));
                            start = end - overlap;
                            if (start >= text.length()) break;
                        }
                        return chunks;
                    }
                }
                """.formatted(pkg);
    }

    public String generateEmbeddingService(String pkg) {
        return """
                package %s.infra.rag;

                import org.springframework.stereotype.Service;
                import java.util.List;

                /**
                 * Embedding service — converts text to vector embeddings.
                 * Uses the configured embedding model (e.g., OpenAI text-embedding-3-small).
                 */
                @Service
                public class EmbeddingService {

                    public List<Float> embed(String text) {
                        // TODO: Integrate with Spring AI EmbeddingModel or LangChain4j EmbeddingModel
                        throw new UnsupportedOperationException("Embedding service not yet configured");
                    }

                    public List<List<Float>> embedBatch(List<String> texts) {
                        return texts.stream().map(this::embed).toList();
                    }
                }
                """.formatted(pkg);
    }

    public String generateWebSocketConfig(String pkg) {
        return """
                package %s.infra.config;

                import org.springframework.context.annotation.Configuration;
                import org.springframework.web.socket.config.annotation.EnableWebSocket;
                import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
                import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
                import %s.api.ws.ChatWebSocketHandler;

                @Configuration
                @EnableWebSocket
                public class WebSocketConfig implements WebSocketConfigurer {

                    private final ChatWebSocketHandler chatWebSocketHandler;

                    public WebSocketConfig(ChatWebSocketHandler chatWebSocketHandler) {
                        this.chatWebSocketHandler = chatWebSocketHandler;
                    }

                    @Override
                    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
                        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                                .setAllowedOrigins("*");
                    }
                }
                """.formatted(pkg, pkg);
    }

    public String generateNacosDiscoveryConfig(String pkg) {
        return """
                package %s.infra.config;

                import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
                import org.springframework.context.annotation.Configuration;

                @Configuration
                @EnableDiscoveryClient
                public class NacosDiscoveryConfig {
                }
                """.formatted(pkg);
    }

    // ==================== app module ====================

    public String generateChatService(String pkg) {
        return """
                package %s.app.service;

                import %s.infra.llm.LLMProviderAdapter;
                import %s.infra.llm.LLMProviderFactory;
                import %s.domain.dto.ChatRequest;
                import %s.domain.dto.ChatResponse;
                import %s.domain.enums.LLMProviderType;
                import org.springframework.stereotype.Service;
                import reactor.core.publisher.Flux;

                import java.util.UUID;

                @Service
                public class ChatService {

                    private final LLMProviderFactory factory;

                    public ChatService(LLMProviderFactory factory) {
                        this.factory = factory;
                    }

                    public ChatResponse chat(ChatRequest request) {
                        LLMProviderAdapter adapter = resolveAdapter(request.getProvider());
                        String response = adapter.chat(
                                "You are a helpful AI assistant.",
                                request.getMessage());
                        ChatResponse cr = new ChatResponse();
                        cr.setId(UUID.randomUUID().toString());
                        cr.setContent(response);
                        cr.setConversationId(request.getConversationId());
                        return cr;
                    }

                    public Flux<String> streamChat(ChatRequest request) {
                        LLMProviderAdapter adapter = resolveAdapter(request.getProvider());
                        return adapter.chatStream(
                                "You are a helpful AI assistant.",
                                request.getMessage());
                    }

                    private LLMProviderAdapter resolveAdapter(String provider) {
                        if (provider != null && !provider.isBlank()) {
                            return factory.getAdapter(LLMProviderType.valueOf(provider.toUpperCase()));
                        }
                        return factory.getDefaultAdapter();
                    }
                }
                """.formatted(pkg, pkg, pkg, pkg, pkg, pkg);
    }

    public String generateAgentService(String pkg) {
        return """
                package %s.app.service;

                import %s.domain.dto.ChatRequest;
                import %s.domain.dto.ChatResponse;
                import org.springframework.stereotype.Service;

                /**
                 * Agent service — orchestrates AI Agent interactions with tool calling.
                 */
                @Service
                public class AgentService {

                    private final ChatService chatService;

                    public AgentService(ChatService chatService) {
                        this.chatService = chatService;
                    }

                    public ChatResponse execute(ChatRequest request) {
                        // TODO: Integrate with AgentOrchestrator for ReAct/Function Calling flow
                        return chatService.chat(request);
                    }
                }
                """.formatted(pkg, pkg, pkg);
    }

    public String generateWeatherTool(String pkg) {
        return """
                package %s.app.tool;

                import org.springframework.stereotype.Component;
                import java.util.Map;

                @Component
                public class WeatherTool {

                    public String getWeather(String city, String unit) {
                        // TODO: Integrate with a real weather API
                        return Map.of(
                            "city", city,
                            "temperature", "22",
                            "unit", unit != null ? unit : "celsius",
                            "condition", "sunny"
                        ).toString();
                    }
                }
                """.formatted(pkg);
    }

    public String generateSearchTool(String pkg) {
        return """
                package %s.app.tool;

                import org.springframework.stereotype.Component;
                import java.util.List;

                @Component
                public class SearchTool {

                    public List<String> search(String query, int maxResults) {
                        // TODO: Integrate with a search API (Tavily, SerpAPI, etc.)
                        return List.of(
                            "Result 1 for: " + query,
                            "Result 2 for: " + query
                        );
                    }
                }
                """.formatted(pkg);
    }

    public String generatePromptTemplate(String pkg) {
        return """
                package %s.app.prompt;

                import java.util.Map;
                import java.util.regex.Pattern;

                /**
                 * Simple string-template based prompt manager.
                 * Replace with StringTemplate or Mustache for production use.
                 */
                public class PromptTemplate {

                    private final String template;

                    public PromptTemplate(String template) {
                        this.template = template;
                    }

                    public String render(Map<String, String> variables) {
                        String result = template;
                        for (var entry : variables.entrySet()) {
                            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
                        }
                        return result;
                    }
                }
                """.formatted(pkg);
    }

    public String generateRagService(String pkg) {
        return """
                package %s.app.service;

                import %s.infra.rag.EmbeddingService;
                import %s.infra.rag.TextSplitter;
                import %s.infra.rag.DocumentLoader;
                import org.springframework.stereotype.Service;

                import java.nio.file.Path;
                import java.util.List;

                @Service
                public class RagService {

                    private final DocumentLoader documentLoader;
                    private final TextSplitter textSplitter;
                    private final EmbeddingService embeddingService;

                    public RagService(DocumentLoader documentLoader,
                                      TextSplitter textSplitter,
                                      EmbeddingService embeddingService) {
                        this.documentLoader = documentLoader;
                        this.textSplitter = textSplitter;
                        this.embeddingService = embeddingService;
                    }

                    public void ingestDocument(Path filePath) {
                        try {
                            String content = documentLoader.loadTextFile(filePath);
                            List<String> chunks = textSplitter.split(content);
                            List<List<Float>> embeddings = embeddingService.embedBatch(chunks);
                            // TODO: Store chunks + embeddings in vector store
                        } catch (Exception e) {
                            throw new RuntimeException("Document ingestion failed", e);
                        }
                    }

                    public List<String> retrieve(String query, int topK) {
                        // TODO: Query vector store for similar chunks
                        return List.of("Retrieved context for: " + query);
                    }
                }
                """.formatted(pkg, pkg, pkg, pkg);
    }

    public String generateIngestionPipeline(String pkg) {
        return """
                package %s.app.rag;

                import %s.infra.rag.DocumentLoader;
                import %s.infra.rag.TextSplitter;
                import %s.infra.rag.EmbeddingService;
                import org.springframework.stereotype.Component;

                import java.nio.file.Path;
                import java.util.List;

                @Component
                public class IngestionPipeline {

                    private final DocumentLoader loader;
                    private final TextSplitter splitter;
                    private final EmbeddingService embedder;

                    public IngestionPipeline(DocumentLoader loader,
                                             TextSplitter splitter,
                                             EmbeddingService embedder) {
                        this.loader = loader;
                        this.splitter = splitter;
                        this.embedder = embedder;
                    }

                    public void ingest(Path filePath) {
                        try {
                            String content = loader.loadTextFile(filePath);
                            List<String> chunks = splitter.split(content);
                            embedder.embedBatch(chunks);
                            // TODO: Persist to vector store
                        } catch (Exception e) {
                            throw new RuntimeException("Ingestion failed", e);
                        }
                    }
                }
                """.formatted(pkg, pkg, pkg, pkg);
    }

    public String generateRetrievalPipeline(String pkg) {
        return """
                package %s.app.rag;

                import org.springframework.stereotype.Component;
                import java.util.List;

                @Component
                public class RetrievalPipeline {

                    public List<String> retrieve(String query, int topK) {
                        // TODO: Query vector store with embedding similarity
                        return List.of("Retrieved context chunk for: " + query);
                    }
                }
                """.formatted(pkg);
    }

    public String generateRetrievalAugmentor(String pkg) {
        return """
                package %s.app.rag;

                import org.springframework.stereotype.Component;
                import java.util.List;

                @Component
                public class RetrievalAugmentor {

                    private final RetrievalPipeline retrieval;

                    public RetrievalAugmentor(RetrievalPipeline retrieval) {
                        this.retrieval = retrieval;
                    }

                    public String augment(String query) {
                        List<String> contexts = retrieval.retrieve(query, 5);
                        StringBuilder sb = new StringBuilder();
                        sb.append("Context:\\n");
                        for (int i = 0; i < contexts.size(); i++) {
                            sb.append("[").append(i + 1).append("] ")
                              .append(contexts.get(i)).append("\\n");
                        }
                        sb.append("\\nQuestion: ").append(query);
                        return sb.toString();
                    }
                }
                """.formatted(pkg);
    }

    public String generateStreamService(String pkg) {
        return """
                package %s.app.service;

                import %s.domain.dto.ChatRequest;
                import %s.domain.dto.StreamData;
                import reactor.core.publisher.Flux;

                /**
                 * SSE streaming service — wraps ChatService streaming in a
                 * standard stream event format.
                 */
                public class StreamService {

                    private final ChatService chatService;

                    public StreamService(ChatService chatService) {
                        this.chatService = chatService;
                    }

                    public Flux<StreamData> stream(ChatRequest request) {
                        return chatService.streamChat(request)
                                .map(chunk -> {
                                    StreamData data = new StreamData(chunk, "message");
                                    return data;
                                })
                                .concatWithValues(createDoneEvent());
                    }

                    private StreamData createDoneEvent() {
                        StreamData data = new StreamData("", "done");
                        data.setDone(true);
                        return data;
                    }
                }
                """.formatted(pkg, pkg, pkg);
    }

    public String generateReactAgent(String pkg) {
        return """
                package %s.app.agent;

                import %s.infra.llm.LLMProviderFactory;
                import %s.domain.enums.LLMProviderType;
                import java.util.ArrayList;
                import java.util.List;

                /**
                 * ReAct (Reasoning + Acting) Agent pattern implementation.
                 * Uses LangChain4j or Spring AI for tool-calling loop.
                 */
                public class ReactAgent {

                    private final LLMProviderFactory factory;
                    private final List<Object> tools = new ArrayList<>();

                    public ReactAgent(LLMProviderFactory factory) {
                        this.factory = factory;
                    }

                    public void registerTool(Object tool) {
                        tools.add(tool);
                    }

                    public String execute(String task) {
                        // TODO: Implement ReAct loop:
                        // 1. Thought: reason about the task
                        // 2. Action: call a tool
                        // 3. Observation: process tool result
                        // 4. Repeat until final answer
                        var adapter = factory.getDefaultAdapter();
                        return adapter.chat("You are a helpful agent with access to tools.", task);
                    }
                }
                """.formatted(pkg, pkg, pkg);
    }

    public String generateChatWorkflow(String pkg) {
        return """
                package %s.app.agent.workflow;

                import java.util.Map;

                /**
                 * Chat workflow using LangGraph4j StateGraph pattern.
                 * Defines a simple agent -> tools -> agent loop.
                 */
                public class ChatWorkflow {

                    public Map<String, Object> execute(Map<String, Object> initialState) {
                        // TODO: Define StateGraph with nodes:
                        // - agent: LLM call with tool binding
                        // - tools: execute tool calls
                        // - conditional edge: if no more tool calls -> END else -> tools
                        return initialState;
                    }
                }
                """.formatted(pkg);
    }

    public String generateStateGraphConfig(String pkg) {
        return """
                package %s.app.agent.workflow;

                import org.springframework.context.annotation.Configuration;

                @Configuration
                public class StateGraphConfig {

                    // TODO: Define LangGraph4j StateGraph beans here.
                    // Example:
                    // @Bean
                    // public StateGraph<AgentState> chatStateGraph() { ... }
                }
                """.formatted(pkg);
    }

    public String generateAgentOrchestrator(String pkg) {
        return """
                package %s.app.agent;

                import %s.app.service.ChatService;
                import org.springframework.stereotype.Component;

                /**
                 * Central agent orchestrator — routes requests to
                 * the appropriate agent strategy (ReAct, Chain, Custom).
                 */
                @Component
                public class AgentOrchestrator {

                    private final ChatService chatService;

                    public AgentOrchestrator(ChatService chatService) {
                        this.chatService = chatService;
                    }

                    public String orchestrate(String task) {
                        // TODO: Select agent strategy based on task complexity
                        return chatService.chat(
                                new %s.domain.dto.ChatRequest()).getContent();
                    }
                }
                """.formatted(pkg, pkg, pkg);
    }

    // ==================== api module ====================

    public String generateChatController(String pkg) {
        return """
                package %s.api.rest;

                import %s.domain.dto.ChatRequest;
                import %s.domain.dto.ChatResponse;
                import %s.app.service.ChatService;
                import %s.common.result.Result;
                import jakarta.validation.Valid;
                import org.springframework.web.bind.annotation.*;

                @RestController
                @RequestMapping("/api/v1/chat")
                public class ChatController {

                    private final ChatService chatService;

                    public ChatController(ChatService chatService) {
                        this.chatService = chatService;
                    }

                    @PostMapping
                    public Result<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
                        ChatResponse response = chatService.chat(request);
                        return Result.success(response);
                    }
                }
                """.formatted(pkg, pkg, pkg, pkg, pkg);
    }

    public String generateAgentController(String pkg) {
        return """
                package %s.api.rest;

                import %s.domain.dto.ChatRequest;
                import %s.domain.dto.ChatResponse;
                import %s.app.service.AgentService;
                import %s.common.result.Result;
                import jakarta.validation.Valid;
                import org.springframework.web.bind.annotation.*;

                @RestController
                @RequestMapping("/api/v1/agent")
                public class AgentController {

                    private final AgentService agentService;

                    public AgentController(AgentService agentService) {
                        this.agentService = agentService;
                    }

                    @PostMapping
                    public Result<ChatResponse> execute(@Valid @RequestBody ChatRequest request) {
                        ChatResponse response = agentService.execute(request);
                        return Result.success(response);
                    }
                }
                """.formatted(pkg, pkg, pkg, pkg, pkg);
    }

    public String generateSseController(String pkg) {
        return """
                package %s.api.rest;

                import %s.domain.dto.ChatRequest;
                import %s.domain.dto.StreamData;
                import %s.app.service.StreamService;
                import org.springframework.http.MediaType;
                import org.springframework.web.bind.annotation.*;
                import reactor.core.publisher.Flux;

                @RestController
                @RequestMapping("/api/v1/stream")
                public class SseController {

                    private final StreamService streamService;

                    public SseController(StreamService streamService) {
                        this.streamService = streamService;
                    }

                    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
                    public Flux<StreamData> streamChat(@RequestBody ChatRequest request) {
                        return streamService.stream(request);
                    }

                    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
                    public Flux<StreamData> streamChatGet(@RequestParam String message) {
                        ChatRequest request = new ChatRequest();
                        request.setMessage(message);
                        return streamService.stream(request);
                    }
                }
                """.formatted(pkg, pkg, pkg, pkg);
    }

    public String generateMcpServerConfig(String pkg) {
        return """
                package %s.api.mcp;

                import org.springframework.ai.mcp.server.McpAsyncServer;
                import org.springframework.context.annotation.Bean;
                import org.springframework.context.annotation.Configuration;

                @Configuration
                public class McpServerConfig {

                    // MCP Server auto-configured by spring-ai-starter-mcp-server-webmvc
                    // Add @McpTool-annotated methods to expose tools to AI clients.

                    @Bean
                    public McpAsyncServer mcpAsyncServer(McpAsyncServer server) {
                        return server;
                    }
                }
                """.formatted(pkg);
    }

    public String generateWeatherMcpTool(String pkg) {
        return """
                package %s.api.mcp.tool;

                import org.springframework.ai.mcp.annotation.McpTool;
                import org.springframework.ai.mcp.annotation.McpToolParam;
                import org.springframework.stereotype.Component;

                @Component
                public class WeatherMcpTool {

                    @McpTool(description = "Get weather information for a given city")
                    public String getWeather(
                            @McpToolParam(description = "City name, e.g. Beijing") String city,
                            @McpToolParam(description = "Temperature unit: celsius or fahrenheit") String unit) {
                        // TODO: Integrate with real weather API
                        return String.format("Weather in %%s: 22°%%s, sunny", city,
                                "fahrenheit".equalsIgnoreCase(unit) ? "F" : "C");
                    }
                }
                """.formatted(pkg);
    }

    public String generateSearchMcpTool(String pkg) {
        return """
                package %s.api.mcp.tool;

                import org.springframework.ai.mcp.annotation.McpTool;
                import org.springframework.ai.mcp.annotation.McpToolParam;
                import org.springframework.stereotype.Component;

                @Component
                public class SearchMcpTool {

                    @McpTool(description = "Search the web for information")
                    public String search(
                            @McpToolParam(description = "Search query string") String query,
                            @McpToolParam(description = "Maximum number of results") int maxResults) {
                        // TODO: Integrate with real search API
                        return String.format("Search results for '%%s' (%%d results)", query, maxResults);
                    }
                }
                """.formatted(pkg);
    }

    public String generateMcpResourceProvider(String pkg) {
        return """
                package %s.api.mcp.resource;

                import org.springframework.stereotype.Component;

                /**
                 * MCP Resource provider — exposes data resources to AI models.
                 */
                @Component
                public class McpResourceProvider {

                    public String getKnowledgeBase() {
                        // TODO: Return relevant knowledge base content
                        return "Knowledge base content goes here.";
                    }
                }
                """.formatted(pkg);
    }

    public String generateA2AAgentCard(String pkg) {
        return """
                package %s.api.a2a;

                import org.springframework.web.bind.annotation.GetMapping;
                import org.springframework.web.bind.annotation.RestController;
                import java.util.Map;

                /**
                 * A2A Agent Card endpoint — publishes agent metadata at
                 * /.well-known/agent.json per the A2A specification.
                 */
                @RestController
                public class A2AAgentCard {

                    @GetMapping("/.well-known/agent.json")
                    public Map<String, Object> getAgentCard() {
                        return Map.of(
                            "name", "%s",
                            "description", "AI Agent powered by scaffold4j",
                            "version", "1.0.0",
                            "capabilities", Map.of(
                                "streaming", true,
                                "pushNotifications", false
                            ),
                            "defaultInputModes", new String[]{"text"},
                            "defaultOutputModes", new String[]{"text"},
                            "skills", new Object[]{
                                Map.of("id", "chat", "name", "Chat",
                                       "description", "General conversation")
                            }
                        );
                    }
                }
                """.formatted(pkg, config.name());
    }

    public String generateA2ATaskHandler(String pkg) {
        return """
                package %s.api.a2a;

                import %s.app.service.ChatService;
                import %s.domain.dto.ChatRequest;
                import org.springframework.stereotype.Component;
                import java.util.Map;
                import java.util.UUID;
                import java.util.concurrent.ConcurrentHashMap;

                /**
                 * A2A Task handler — manages A2A task lifecycle and state transitions.
                 */
                @Component
                public class A2ATaskHandler {

                    private final ChatService chatService;
                    private final Map<String, String> taskStates = new ConcurrentHashMap<>();

                    public A2ATaskHandler(ChatService chatService) {
                        this.chatService = chatService;
                    }

                    public Map<String, Object> processMessage(Map<String, Object> message) {
                        String taskId = UUID.randomUUID().toString();
                        taskStates.put(taskId, "working");

                        String userMessage = (String) ((Map) message.get("parts")).get("text");

                        ChatRequest request = new ChatRequest();
                        request.setMessage(userMessage);
                        var response = chatService.chat(request);

                        taskStates.put(taskId, "completed");
                        return Map.of(
                            "taskId", taskId,
                            "state", "completed",
                            "artifacts", new Object[]{
                                Map.of("type", "text", "content", response.getContent())
                            }
                        );
                    }

                    public String getTaskState(String taskId) {
                        return taskStates.getOrDefault(taskId, "unknown");
                    }
                }
                """.formatted(pkg, pkg, pkg);
    }

    public String generateA2AServerConfig(String pkg) {
        return """
                package %s.api.a2a;

                import org.springframework.context.annotation.Bean;
                import org.springframework.context.annotation.Configuration;
                import org.springframework.web.servlet.function.RouterFunction;
                import org.springframework.web.servlet.function.ServerResponse;
                import static org.springframework.web.servlet.function.RouterFunctions.route;

                @Configuration
                public class A2AServerConfig {

                    @Bean
                    public RouterFunction<ServerResponse> a2aRoutes(A2ATaskHandler handler) {
                        return route()
                            .POST("/a2a/message/send", request -> {
                                var body = request.body(Map.class);
                                var result = handler.processMessage(body);
                                return ServerResponse.ok().body(result);
                            })
                            .GET("/a2a/tasks/{taskId}", request -> {
                                String taskId = request.pathVariable("taskId");
                                String state = handler.getTaskState(taskId);
                                return ServerResponse.ok().body(
                                    java.util.Map.of("taskId", taskId, "state", state));
                            })
                            .build();
                    }
                }
                """.formatted(pkg);
    }

    public String generateAcpAgentEndpoint(String pkg) {
        return """
                package %s.api.acp;

                import %s.app.service.ChatService;
                import %s.domain.dto.ChatRequest;
                import org.springframework.web.bind.annotation.*;
                import java.util.Map;
                import java.util.UUID;
                import java.util.concurrent.ConcurrentHashMap;

                /**
                 * ACP Agent endpoint — implements the Agent Communication Protocol.
                 * Note: ACP is merging into A2A under the Linux Foundation.
                 * This endpoint provides backward compatibility during the transition.
                 */
                @RestController
                @RequestMapping("/acp")
                public class AcpAgentEndpoint {

                    private final ChatService chatService;
                    private final Map<String, String> sessions = new ConcurrentHashMap<>();

                    public AcpAgentEndpoint(ChatService chatService) {
                        this.chatService = chatService;
                    }

                    @PostMapping("/message")
                    public Map<String, Object> sendMessage(@RequestBody Map<String, Object> body) {
                        String sessionId = (String) body.getOrDefault("sessionId",
                                UUID.randomUUID().toString());
                        sessions.putIfAbsent(sessionId, "active");

                        String text = (String) body.getOrDefault("text", "");
                        ChatRequest request = new ChatRequest();
                        request.setMessage(text);
                        var response = chatService.chat(request);

                        return Map.of(
                            "sessionId", sessionId,
                            "status", "completed",
                            "content", response.getContent()
                        );
                    }

                    @GetMapping("/agent.json")
                    public Map<String, Object> getAgentManifest() {
                        return Map.of(
                            "protocol", "ACP",
                            "version", "1.0",
                            "note", "ACP is merging into A2A. A2A endpoint also available at /.well-known/agent.json"
                        );
                    }
                }
                """.formatted(pkg, pkg, pkg);
    }

    public String generateAcpSessionManager(String pkg) {
        return """
                package %s.api.acp;

                import org.springframework.stereotype.Component;
                import java.util.Map;
                import java.util.concurrent.ConcurrentHashMap;

                @Component
                public class AcpSessionManager {

                    private final Map<String, AcpSession> sessions = new ConcurrentHashMap<>();

                    public AcpSession createSession() {
                        AcpSession session = new AcpSession();
                        sessions.put(session.id(), session);
                        return session;
                    }

                    public AcpSession getSession(String id) {
                        return sessions.get(id);
                    }

                    public void closeSession(String id) {
                        sessions.remove(id);
                    }

                    public record AcpSession(String id, String status, long createdAt) {
                        public AcpSession() {
                            this(java.util.UUID.randomUUID().toString(), "active", System.currentTimeMillis());
                        }
                    }
                }
                """.formatted(pkg);
    }

    public String generateChatWebSocketHandler(String pkg) {
        return """
                package %s.api.ws;

                import %s.app.service.ChatService;
                import %s.domain.dto.ChatRequest;
                import com.fasterxml.jackson.databind.ObjectMapper;
                import org.springframework.stereotype.Component;
                import org.springframework.web.socket.CloseStatus;
                import org.springframework.web.socket.TextMessage;
                import org.springframework.web.socket.WebSocketSession;
                import org.springframework.web.socket.handler.TextWebSocketHandler;

                @Component
                public class ChatWebSocketHandler extends TextWebSocketHandler {

                    private final ChatService chatService;
                    private final ObjectMapper objectMapper = new ObjectMapper();

                    public ChatWebSocketHandler(ChatService chatService) {
                        this.chatService = chatService;
                    }

                    @Override
                    protected void handleTextMessage(WebSocketSession session, TextMessage message)
                            throws Exception {
                        String payload = message.getPayload();
                        ChatRequest request = objectMapper.readValue(payload, ChatRequest.class);

                        chatService.streamChat(request)
                                .subscribe(
                                    chunk -> {
                                        try {
                                            session.sendMessage(new TextMessage(chunk));
                                        } catch (Exception e) {
                                            // session closed
                                        }
                                    },
                                    error -> {
                                        try {
                                            session.sendMessage(new TextMessage(
                                                "{\\"error\\":\\"" + error.getMessage() + "\\"}"));
                                        } catch (Exception ignored) {}
                                    }
                                );
                    }

                    @Override
                    public void afterConnectionEstablished(WebSocketSession session) {
                        // Connection opened
                    }

                    @Override
                    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
                        // Connection closed
                    }
                }
                """.formatted(pkg, pkg, pkg);
    }

    // ==================== bootstrap module ====================

    public String generateApplication(String pkg) {
        return """
                package %s;

                import org.springframework.boot.SpringApplication;
                import org.springframework.boot.autoconfigure.SpringBootApplication;

                @SpringBootApplication(scanBasePackages = "%s")
                public class Application {

                    public static void main(String[] args) {
                        SpringApplication.run(Application.class, args);
                    }
                }
                """.formatted(pkg, config.basePackage());
    }

    // ==================== infra: database config ====================

    public String generateDataSourceConfig(String pkg) {
        return """
                package %s.infra.config;

                import com.zaxxer.hikari.HikariDataSource;
                import org.springframework.boot.context.properties.ConfigurationProperties;
                import org.springframework.boot.jdbc.DataSourceBuilder;
                import org.springframework.context.annotation.Bean;
                import org.springframework.context.annotation.Configuration;

                import javax.sql.DataSource;

                @Configuration
                public class DataSourceConfig {

                    @Bean
                    @ConfigurationProperties(prefix = "spring.datasource")
                    public DataSource dataSource() {
                        return DataSourceBuilder.create()
                                .type(HikariDataSource.class)
                                .build();
                    }
                }
                """.formatted(pkg);
    }

    public String generateMybatisPlusConfig(String pkg) {
        return """
                package %s.infra.config;

                import com.baomidou.mybatisplus.annotation.DbType;
                import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
                import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
                import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
                import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
                import org.mybatis.spring.annotation.MapperScan;
                import org.springframework.context.annotation.Bean;
                import org.springframework.context.annotation.Configuration;

                @Configuration
                @MapperScan("%s.infra.mapper")
                public class MybatisPlusConfig {

                    @Bean
                    public MybatisPlusInterceptor mybatisPlusInterceptor() {
                        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
                        // Pagination plugin
                        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.%s));
                        // Optimistic locker
                        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
                        // Block full-table update/delete
                        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
                        return interceptor;
                    }
                }
                """.formatted(pkg, pkg, config.dbType().name());
    }

    public String generateJpaConfig(String pkg) {
        return """
                package %s.infra.config;

                import org.springframework.context.annotation.Configuration;
                import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
                import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

                @Configuration
                @EnableJpaRepositories(basePackages = "%s.infra.repository")
                @EnableJpaAuditing
                public class JpaConfig {
                }
                """.formatted(pkg, pkg);
    }

    // ==================== infra: cache config ====================

    public String generateRedisCacheConfig(String pkg) {
        return """
                package %s.infra.config;

                import com.fasterxml.jackson.annotation.JsonTypeInfo;
                import com.fasterxml.jackson.databind.ObjectMapper;
                import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
                import org.springframework.cache.annotation.EnableCaching;
                import org.springframework.context.annotation.Bean;
                import org.springframework.context.annotation.Configuration;
                import org.springframework.data.redis.connection.RedisConnectionFactory;
                import org.springframework.data.redis.core.RedisTemplate;
                import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
                import org.springframework.data.redis.serializer.StringRedisSerializer;
                import org.springframework.data.redis.cache.RedisCacheConfiguration;
                import org.springframework.data.redis.cache.RedisCacheManager;
                import org.springframework.data.redis.serializer.RedisSerializationContext;

                import java.time.Duration;

                @Configuration
                @EnableCaching
                public class RedisCacheConfig {

                    @Bean
                    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
                        RedisTemplate<String, Object> template = new RedisTemplate<>();
                        template.setConnectionFactory(factory);

                        ObjectMapper mapper = new ObjectMapper();
                        mapper.registerModule(new JavaTimeModule());
                        mapper.activateDefaultTyping(
                                mapper.getPolymorphicTypeValidator(),
                                ObjectMapper.DefaultTyping.NON_FINAL,
                                JsonTypeInfo.As.PROPERTY);

                        GenericJackson2JsonRedisSerializer serializer =
                                new GenericJackson2JsonRedisSerializer(mapper);

                        template.setKeySerializer(new StringRedisSerializer());
                        template.setValueSerializer(serializer);
                        template.setHashKeySerializer(new StringRedisSerializer());
                        template.setHashValueSerializer(serializer);
                        template.afterPropertiesSet();
                        return template;
                    }

                    @Bean
                    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
                        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofHours(1))
                                .serializeKeysWith(RedisSerializationContext.SerializationPair
                                        .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                                .disableCachingNullValues();

                        return RedisCacheManager.builder(factory)
                                .cacheDefaults(config)
                                .build();
                    }
                }
                """.formatted(pkg);
    }

    public String generateCaffeineCacheConfig(String pkg) {
        return """
                package %s.infra.config;

                import com.github.benmanes.caffeine.cache.Caffeine;
                import org.springframework.cache.CacheManager;
                import org.springframework.cache.annotation.EnableCaching;
                import org.springframework.cache.caffeine.CaffeineCacheManager;
                import org.springframework.context.annotation.Bean;
                import org.springframework.context.annotation.Configuration;

                import java.util.concurrent.TimeUnit;

                @Configuration
                @EnableCaching
                public class CaffeineCacheConfig {

                    @Bean
                    public CacheManager cacheManager() {
                        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
                        cacheManager.setCaffeine(Caffeine.newBuilder()
                                .maximumSize(500)
                                .expireAfterAccess(10, TimeUnit.MINUTES)
                                .recordStats());
                        return cacheManager;
                    }
                }
                """.formatted(pkg);
    }

    // ==================== infra: Nacos config refresh ====================

    public String generateNacosConfigRefresh(String pkg) {
        return """
                package %s.infra.config;

                import org.springframework.cloud.context.config.annotation.RefreshScope;
                import org.springframework.context.annotation.Configuration;

                /**
                 * Enables @RefreshScope for Nacos config-driven hot reload.
                 * Beans annotated with @RefreshScope will be re-initialized
                 * when configuration changes in Nacos.
                 */
                @Configuration
                @RefreshScope
                public class NacosConfigRefresh {
                }
                """.formatted(pkg);
    }

    // ==================== domain: database entities ====================

    public String generateUserStatus(String pkg) {
        return """
                package %s.domain.enums;

                public enum UserStatus {
                    ACTIVE, INACTIVE, BANNED
                }
                """.formatted(pkg);
    }

    public String generateUserEntity(String pkg) {
        if (config.usesMyBatisPlus()) {
            return """
                    package %s.domain.entity;

                    import com.baomidou.mybatisplus.annotation.*;
                    import %s.domain.enums.UserStatus;
                    import java.time.LocalDateTime;

                    @TableName("t_user")
                    public class User {
                        @TableId(type = IdType.ASSIGN_ID)
                        private Long id;
                        private String username;
                        private String email;
                        private UserStatus status;

                        @TableField(fill = FieldFill.INSERT)
                        private LocalDateTime createTime;

                        @TableField(fill = FieldFill.INSERT_UPDATE)
                        private LocalDateTime updateTime;

                        @Version
                        private Integer version;

                        @TableLogic
                        private Integer deleted;

                        public Long getId() { return id; }
                        public void setId(Long id) { this.id = id; }
                        public String getUsername() { return username; }
                        public void setUsername(String username) { this.username = username; }
                        public String getEmail() { return email; }
                        public void setEmail(String email) { this.email = email; }
                        public UserStatus getStatus() { return status; }
                        public void setStatus(UserStatus status) { this.status = status; }
                        public LocalDateTime getCreateTime() { return createTime; }
                        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
                        public LocalDateTime getUpdateTime() { return updateTime; }
                        public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
                        public Integer getVersion() { return version; }
                        public void setVersion(Integer version) { this.version = version; }
                        public Integer getDeleted() { return deleted; }
                        public void setDeleted(Integer deleted) { this.deleted = deleted; }
                    }
                    """.formatted(pkg, pkg);
        } else {
            return """
                    package %s.domain.entity;

                    import %s.domain.enums.UserStatus;
                    import jakarta.persistence.*;
                    import org.springframework.data.annotation.CreatedDate;
                    import org.springframework.data.annotation.LastModifiedDate;
                    import org.springframework.data.jpa.domain.support.AuditingEntityListener;
                    import java.time.LocalDateTime;

                    @Entity
                    @Table(name = "t_user")
                    @EntityListeners(AuditingEntityListener.class)
                    public class User {
                        @Id
                        @GeneratedValue(strategy = GenerationType.IDENTITY)
                        private Long id;

                        @Column(nullable = false, length = 50)
                        private String username;

                        @Column(length = 100)
                        private String email;

                        @Enumerated(EnumType.STRING)
                        private UserStatus status;

                        @CreatedDate
                        private LocalDateTime createTime;

                        @LastModifiedDate
                        private LocalDateTime updateTime;

                        @Version
                        private Integer version;

                        public Long getId() { return id; }
                        public void setId(Long id) { this.id = id; }
                        public String getUsername() { return username; }
                        public void setUsername(String username) { this.username = username; }
                        public String getEmail() { return email; }
                        public void setEmail(String email) { this.email = email; }
                        public UserStatus getStatus() { return status; }
                        public void setStatus(UserStatus status) { this.status = status; }
                        public LocalDateTime getCreateTime() { return createTime; }
                        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
                        public LocalDateTime getUpdateTime() { return updateTime; }
                        public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
                        public Integer getVersion() { return version; }
                        public void setVersion(Integer version) { this.version = version; }
                    }
                    """.formatted(pkg, pkg);
        }
    }

    public String generateUserMapper(String pkg) {
        return """
                package %s.infra.mapper;

                import com.baomidou.mybatisplus.core.mapper.BaseMapper;
                import %s.domain.entity.User;
                import org.apache.ibatis.annotations.Mapper;

                @Mapper
                public interface UserMapper extends BaseMapper<User> {
                }
                """.formatted(pkg, pkg);
    }

    public String generateUserRepository(String pkg) {
        return """
                package %s.infra.repository;

                import %s.domain.entity.User;
                import org.springframework.data.jpa.repository.JpaRepository;
                import org.springframework.stereotype.Repository;

                import java.util.Optional;

                @Repository
                public interface UserRepository extends JpaRepository<User, Long> {
                    Optional<User> findByUsername(String username);
                    boolean existsByUsername(String username);
                }
                """.formatted(pkg, pkg);
    }

    // ==================== app: services ====================

    public String generateUserService(String pkg) {
        if (config.usesMyBatisPlus()) {
            return """
                    package %s.app.service;

                    import com.baomidou.mybatisplus.core.metadata.IPage;
                    import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
                    import %s.domain.entity.User;
                    import %s.domain.enums.UserStatus;
                    import %s.infra.mapper.UserMapper;
                    import org.springframework.stereotype.Service;

                    import java.util.List;

                    @Service
                    public class UserService {
                        private final UserMapper userMapper;

                        public UserService(UserMapper userMapper) {
                            this.userMapper = userMapper;
                        }

                        public User create(User user) {
                            user.setStatus(UserStatus.ACTIVE);
                            userMapper.insert(user);
                            return user;
                        }

                        public User findById(Long id) {
                            return userMapper.selectById(id);
                        }

                        public List<User> findAll() {
                            return userMapper.selectList(null);
                        }

                        public IPage<User> page(int page, int size) {
                            return userMapper.selectPage(new Page<>(page, size), null);
                        }

                        public User update(User user) {
                            userMapper.updateById(user);
                            return userMapper.selectById(user.getId());
                        }

                        public void delete(Long id) {
                            userMapper.deleteById(id);
                        }
                    }
                    """.formatted(pkg, pkg, pkg, pkg);
        } else {
            return """
                    package %s.app.service;

                    import %s.domain.entity.User;
                    import %s.domain.enums.UserStatus;
                    import %s.infra.repository.UserRepository;
                    import org.springframework.data.domain.Page;
                    import org.springframework.data.domain.Pageable;
                    import org.springframework.stereotype.Service;

                    import java.util.List;

                    @Service
                    public class UserService {
                        private final UserRepository userRepository;

                        public UserService(UserRepository userRepository) {
                            this.userRepository = userRepository;
                        }

                        public User create(User user) {
                            user.setStatus(UserStatus.ACTIVE);
                            return userRepository.save(user);
                        }

                        public User findById(Long id) {
                            return userRepository.findById(id)
                                    .orElseThrow(() -> new RuntimeException("User not found: " + id));
                        }

                        public List<User> findAll() {
                            return userRepository.findAll();
                        }

                        public Page<User> page(Pageable pageable) {
                            return userRepository.findAll(pageable);
                        }

                        public User update(Long id, User user) {
                            User existing = findById(id);
                            existing.setUsername(user.getUsername());
                            existing.setEmail(user.getEmail());
                            existing.setStatus(user.getStatus());
                            return userRepository.save(existing);
                        }

                        public void delete(Long id) {
                            userRepository.deleteById(id);
                        }
                    }
                    """.formatted(pkg, pkg, pkg, pkg);
        }
    }

    public String generateCacheService(String pkg) {
        return """
                package %s.app.service;

                import org.springframework.cache.annotation.CacheEvict;
                import org.springframework.cache.annotation.CachePut;
                import org.springframework.cache.annotation.Cacheable;
                import org.springframework.stereotype.Service;

                import java.util.concurrent.TimeUnit;

                /**
                 * Generic cache service — wraps Spring Cache annotations.
                 * Use for caching expensive operations like AI call results.
                 */
                @Service
                public class CacheService {

                    @Cacheable(value = "ai-responses", key = "#key", unless = "#result == null")
                    public String getCachedResponse(String key) {
                        // Cache miss — caller should populate via putCachedResponse
                        return null;
                    }

                    @CachePut(value = "ai-responses", key = "#key")
                    public String putCachedResponse(String key, String response) {
                        return response;
                    }

                    @CacheEvict(value = "ai-responses", key = "#key")
                    public void evictResponse(String key) {
                    }

                    @CacheEvict(value = "ai-responses", allEntries = true)
                    public void evictAllResponses() {
                    }
                }
                """.formatted(pkg);
    }

    // ==================== app: enhanced LangChain4j ====================

    public String generateAIAgentService(String pkg) {
        return """
                package %s.app.agent;

                import %s.domain.dto.ChatRequest;
                import %s.domain.dto.ChatResponse;
                import %s.app.service.ChatService;
                import %s.app.tool.WeatherTool;
                import %s.app.tool.SearchTool;
                import org.springframework.stereotype.Service;

                /**
                 * LangChain4j AI Service — declarative AI agent.
                 * Uses AI Services pattern for tool-calling agents.
                 * Fallback: delegates to ChatService when LangChain4j not on classpath.
                 */
                @Service
                public class AIAgentService {

                    private final ChatService chatService;
                    private final WeatherTool weatherTool;
                    private final SearchTool searchTool;

                    public AIAgentService(ChatService chatService,
                                          WeatherTool weatherTool,
                                          SearchTool searchTool) {
                        this.chatService = chatService;
                        this.weatherTool = weatherTool;
                        this.searchTool = searchTool;
                    }

                    public ChatResponse chat(ChatRequest request) {
                        // Route to appropriate handler based on intent
                        String message = request.getMessage().toLowerCase();
                        if (message.contains("weather") || message.contains("天气")) {
                            request.setMessage(weatherTool.getWeather(message.contains("beijing") || message.contains("北京")
                                    ? "Beijing" : "Shanghai"));
                        } else if (message.contains("search") || message.contains("搜索")) {
                            request.setMessage(searchTool.search(message));
                        }
                        return chatService.chat(request);
                    }
                }
                """.formatted(pkg, pkg, pkg, pkg, pkg, pkg);
    }

    public String generateLangGraphWorkflow(String pkg) {
        return """
                package %s.app.agent.workflow;

                import %s.app.service.ChatService;
                import %s.app.service.UserService;
                import %s.app.tool.SearchTool;
                import %s.app.tool.WeatherTool;
                import %s.domain.dto.ChatRequest;
                import %s.domain.dto.ChatResponse;
                import org.springframework.stereotype.Component;

                import java.util.Map;

                /**
                 * LangGraph4j StateGraph workflow — orchestrates multi-step AI tasks.
                 * Routes user requests through classify -> tool execution -> response.
                 */
                @Component
                public class LangGraphWorkflow {

                    private final ChatService chatService;
                    private final SearchTool searchTool;
                    private final WeatherTool weatherTool;
                    private final UserService userService;

                    public LangGraphWorkflow(ChatService chatService,
                                             SearchTool searchTool,
                                             WeatherTool weatherTool,
                                             UserService userService) {
                        this.chatService = chatService;
                        this.searchTool = searchTool;
                        this.weatherTool = weatherTool;
                        this.userService = userService;
                    }

                    /**
                     * Classify user intent from message content.
                     */
                    public String classifyIntent(String message) {
                        String lower = message.toLowerCase();
                        if (lower.contains("weather") || lower.contains("天气")) return "WEATHER";
                        if (lower.contains("search") || lower.contains("搜索") || lower.contains("查找")) return "SEARCH";
                        if (lower.contains("user") || lower.contains("用户")) return "DATABASE";
                        return "GENERAL";
                    }

                    /**
                     * Execute tool based on classified intent and return result.
                     */
                    public String executeTool(String intent, String message) {
                        return switch (intent) {
                            case "WEATHER" -> weatherTool.getWeather("Shanghai");
                            case "SEARCH" -> searchTool.search(message);
                            case "DATABASE" -> "Found users: " + userService.findAll().size();
                            default -> message;
                        };
                    }

                    /**
                     * Full workflow: classify -> execute -> respond.
                     */
                    public ChatResponse run(String message) {
                        String intent = classifyIntent(message);
                        String toolResult = executeTool(intent, message);
                        ChatRequest request = new ChatRequest();
                        request.setMessage("Intent: " + intent + ". Data: " + toolResult
                                + ". User message: " + message);
                        return chatService.chat(request);
                    }
                }
                """.formatted(pkg, pkg, pkg, pkg, pkg, pkg, pkg);
    }

    // ==================== api: controllers ====================

    public String generateUserController(String pkg) {
        return """
                package %s.api.rest;

                import %s.app.service.UserService;
                import %s.common.result.Result;
                import %s.domain.entity.User;
                import org.springframework.web.bind.annotation.*;

                import java.util.List;

                @RestController
                @RequestMapping("/api/users")
                public class UserController {
                    private final UserService userService;

                    public UserController(UserService userService) {
                        this.userService = userService;
                    }

                    @PostMapping
                    public Result<User> create(@RequestBody User user) {
                        return Result.success(userService.create(user));
                    }

                    @GetMapping("/{id}")
                    public Result<User> getById(@PathVariable Long id) {
                        return Result.success(userService.findById(id));
                    }

                    @GetMapping
                    public Result<List<User>> list() {
                        return Result.success(userService.findAll());
                    }

                    @PutMapping("/{id}")
                    public Result<User> update(@PathVariable Long id, @RequestBody User user) {
                        return Result.success(userService.update(user));
                    }

                    @DeleteMapping("/{id}")
                    public Result<Void> delete(@PathVariable Long id) {
                        userService.delete(id);
                        return Result.success();
                    }
                }
                """.formatted(pkg, pkg, pkg, pkg);
    }

    public String generateHealthController(String pkg) {
        return """
                package %s.api.rest;

                import org.springframework.web.bind.annotation.GetMapping;
                import org.springframework.web.bind.annotation.RestController;

                import java.util.Map;

                @RestController
                public class HealthController {

                    @GetMapping({"/health", "/actuator/health/lite"})
                    public Map<String, Object> health() {
                        return Map.of(
                            "status", "UP",
                            "app", "%s",
                            "timestamp", System.currentTimeMillis()
                        );
                    }

                    @GetMapping("/ready")
                    public Map<String, Object> readiness() {
                        return Map.of("status", "READY");
                    }
                }
                """.formatted(pkg, config.effectiveArtifactId());
    }

    // ==================== utility ====================

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
