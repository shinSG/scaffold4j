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
                }
                """.formatted(pkg);
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

    // ==================== utility ====================

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
