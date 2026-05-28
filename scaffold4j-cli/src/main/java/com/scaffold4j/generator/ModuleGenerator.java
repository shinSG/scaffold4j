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

                    public static Result<Void> success() {
                        Result<Void> r = new Result<>();
                        r.code = 0;
                        r.message = "success";
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
                .map(p -> p.name() + "(\"" + p.id() + "\")")
                .collect(java.util.stream.Collectors.joining(", "));
        return """
                package %s.domain.enums;

                public enum LLMProviderType {
                    %s;

                    private final String id;

                    LLMProviderType(String id) {
                        this.id = id;
                    }

                    public String id() {
                        return id;
                    }

                    public static LLMProviderType fromId(String id) {
                        if (id == null || id.isBlank()) {
                            throw new IllegalArgumentException("LLM provider id must not be blank");
                        }
                        String normalized = id.trim().replace('-', '_');
                        for (LLMProviderType type : values()) {
                            if (type.id.equalsIgnoreCase(id) || type.name().equalsIgnoreCase(normalized)) {
                                return type;
                            }
                        }
                        throw new IllegalArgumentException("Unsupported LLM provider: " + id);
                    }
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
                import %s.infra.llm.LLMProviderAdapter;
                import org.springframework.beans.factory.annotation.Value;
                import org.springframework.context.annotation.Bean;
                import org.springframework.context.annotation.Configuration;

                import java.util.List;

                @Configuration
                public class LLMProviderConfig {

                    @Bean
                    public LLMProviderFactory llmProviderFactory(
                            List<LLMProviderAdapter> adapters,
                            @Value("${scaffold4j.ai.default-provider:%s}") String defaultProvider) {
                        return new LLMProviderFactory(adapters, defaultProvider);
                    }
                }
                """.formatted(pkg, pkg, pkg, config.llmProviders().iterator().next().id());
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

                    /** Synchronous LLM invocation. */
                    String invoke(String systemPrompt, String userMessage);

                    /** Streaming LLM invocation via reactive streams. */
                    Flux<String> invokeStream(String systemPrompt, String userMessage);

                    /** LLM invocation with full conversation history. */
                    String invokeWithHistory(String systemPrompt, List<ChatMessage> history, String userMessage);

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
                import java.util.List;
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
                    private final LLMProviderType defaultProvider;

                    public LLMProviderFactory(List<LLMProviderAdapter> adapters, String defaultProvider) {
                        if (adapters == null || adapters.isEmpty()) {
                            throw new IllegalStateException("No LLMProviderAdapter beans were found. Check generated provider adapters and selected AI framework dependencies.");
                        }
                        this.defaultProvider = LLMProviderType.fromId(defaultProvider);
                        for (LLMProviderAdapter adapter : adapters) {
                            register(LLMProviderType.fromId(adapter.providerName()), adapter);
                        }
                        if (!this.adapters.containsKey(this.defaultProvider)) {
                            throw new IllegalStateException("Default LLM provider '" + defaultProvider + "' has no registered adapter. Registered providers: " + this.adapters.keySet());
                        }
                    }

                    public void register(LLMProviderType type, LLMProviderAdapter adapter) {
                        if (type == null || adapter == null) {
                            throw new IllegalArgumentException("LLM provider type and adapter must not be null");
                        }
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
                        LLMProviderAdapter adapter = adapters.get(defaultProvider);
                        if (adapter == null) {
                            throw new IllegalStateException("Default LLM provider '" + defaultProvider.id() + "' has no registered adapter. Registered providers: " + adapters.keySet());
                        }
                        return adapter;
                    }
                }
                """.formatted(pkg, pkg, aiFramework);
    }

    public String generateHttpLLMProviderAdapterSupport(String pkg) {
        return """
                package %s.infra.llm;

                import %s.domain.model.ChatMessage;
                import com.fasterxml.jackson.databind.JsonNode;
                import com.fasterxml.jackson.databind.ObjectMapper;
                import org.springframework.http.HttpHeaders;
                import org.springframework.http.HttpStatusCode;
                import org.springframework.http.MediaType;
                import org.springframework.web.reactive.function.client.WebClient;
                import reactor.core.publisher.Flux;

                import java.util.ArrayList;
                import java.util.HashMap;
                import java.util.List;
                import java.util.Map;
                import java.util.function.Consumer;

                /**
                 * Shared HTTP implementation for generated LLM providers.
                 */
                public abstract class HttpLLMProviderAdapterSupport implements LLMProviderAdapter {

                    protected final WebClient webClient;
                    protected final ObjectMapper objectMapper = new ObjectMapper();

                    protected HttpLLMProviderAdapterSupport(WebClient.Builder webClientBuilder, String baseUrl) {
                        this.webClient = webClientBuilder.baseUrl(trimTrailingSlash(baseUrl)).build();
                    }

                    protected String openAiCompatibleInvoke(String apiKey, String model, double temperature, int maxTokens,
                                                            String systemPrompt, List<ChatMessage> history, String userMessage) {
                        JsonNode json = postJson("/v1/chat/completions", bearer(apiKey), openAiBody(model, temperature, maxTokens, false, systemPrompt, history, userMessage));
                        JsonNode content = json.at("/choices/0/message/content");
                        if (content.isMissingNode()) {
                            throw new IllegalStateException("LLM response did not contain choices[0].message.content: " + json);
                        }
                        return content.asText();
                    }

                    protected Flux<String> openAiCompatibleStream(String apiKey, String model, double temperature, int maxTokens,
                                                                  String systemPrompt, List<ChatMessage> history, String userMessage) {
                        return postStream("/v1/chat/completions", bearer(apiKey), openAiBody(model, temperature, maxTokens, true, systemPrompt, history, userMessage))
                                .flatMapIterable(this::extractOpenAiStreamContent);
                    }

                    protected String azureOpenAiInvoke(String apiKey, String apiVersion, String deployment, double temperature, int maxTokens,
                                                       String systemPrompt, List<ChatMessage> history, String userMessage) {
                        String path = "/openai/deployments/" + deployment + "/chat/completions?api-version=" + apiVersion;
                        JsonNode json = postJson(path, headers -> headers.set("api-key", apiKey), openAiBody(deployment, temperature, maxTokens, false, systemPrompt, history, userMessage));
                        JsonNode content = json.at("/choices/0/message/content");
                        if (content.isMissingNode()) {
                            throw new IllegalStateException("Azure OpenAI response did not contain choices[0].message.content: " + json);
                        }
                        return content.asText();
                    }

                    protected Flux<String> azureOpenAiStream(String apiKey, String apiVersion, String deployment, double temperature, int maxTokens,
                                                             String systemPrompt, List<ChatMessage> history, String userMessage) {
                        String path = "/openai/deployments/" + deployment + "/chat/completions?api-version=" + apiVersion;
                        return postStream(path, headers -> headers.set("api-key", apiKey), openAiBody(deployment, temperature, maxTokens, true, systemPrompt, history, userMessage))
                                .flatMapIterable(this::extractOpenAiStreamContent);
                    }

                    protected String anthropicInvoke(String apiKey, String model, double temperature, int maxTokens,
                                                     String systemPrompt, List<ChatMessage> history, String userMessage) {
                        Map<String, Object> body = new HashMap<>();
                        body.put("model", model);
                        body.put("system", nullToEmpty(systemPrompt));
                        body.put("temperature", temperature);
                        body.put("max_tokens", maxTokens);
                        body.put("messages", anthropicMessages(history, userMessage));
                        JsonNode json = postJson("/v1/messages", headers -> {
                            headers.set("x-api-key", apiKey);
                            headers.set("anthropic-version", "2023-06-01");
                        }, body);
                        JsonNode content = json.at("/content/0/text");
                        if (content.isMissingNode()) {
                            throw new IllegalStateException("Anthropic response did not contain content[0].text: " + json);
                        }
                        return content.asText();
                    }

                    protected String ollamaInvoke(String model, String systemPrompt, List<ChatMessage> history, String userMessage) {
                        Map<String, Object> body = new HashMap<>();
                        body.put("model", model);
                        body.put("stream", false);
                        body.put("messages", openAiMessages(systemPrompt, history, userMessage));
                        JsonNode json = postJson("/api/chat", headers -> {}, body);
                        JsonNode content = json.at("/message/content");
                        if (content.isMissingNode()) {
                            throw new IllegalStateException("Ollama response did not contain message.content: " + json);
                        }
                        return content.asText();
                    }

                    protected Flux<String> ollamaStream(String model, String systemPrompt, List<ChatMessage> history, String userMessage) {
                        Map<String, Object> body = new HashMap<>();
                        body.put("model", model);
                        body.put("stream", true);
                        body.put("messages", openAiMessages(systemPrompt, history, userMessage));
                        return postStream("/api/chat", headers -> {}, body).map(this::extractOllamaStreamContent).filter(s -> !s.isBlank());
                    }

                    protected String geminiInvoke(String apiKey, String model, double temperature, int maxTokens,
                                                  String systemPrompt, List<ChatMessage> history, String userMessage) {
                        Map<String, Object> generationConfig = new HashMap<>();
                        generationConfig.put("temperature", temperature);
                        generationConfig.put("maxOutputTokens", maxTokens);
                        Map<String, Object> body = new HashMap<>();
                        body.put("systemInstruction", Map.of("parts", List.of(Map.of("text", nullToEmpty(systemPrompt)))));
                        body.put("contents", geminiContents(history, userMessage));
                        body.put("generationConfig", generationConfig);
                        JsonNode json = postJson("/models/" + model + ":generateContent?key=" + apiKey, headers -> {}, body);
                        JsonNode content = json.at("/candidates/0/content/parts/0/text");
                        if (content.isMissingNode()) {
                            throw new IllegalStateException("Gemini response did not contain candidates[0].content.parts[0].text: " + json);
                        }
                        return content.asText();
                    }

                    protected String bedrockConverseInvoke(String apiKey, String model, double temperature, int maxTokens,
                                                           String systemPrompt, List<ChatMessage> history, String userMessage) {
                        Map<String, Object> body = new HashMap<>();
                        body.put("system", List.of(Map.of("text", nullToEmpty(systemPrompt))));
                        body.put("messages", bedrockMessages(history, userMessage));
                        body.put("inferenceConfig", Map.of("temperature", temperature, "maxTokens", maxTokens));
                        JsonNode json = postJson("/model/" + model + "/converse", bearer(apiKey), body);
                        JsonNode content = json.at("/output/message/content/0/text");
                        if (content.isMissingNode()) {
                            throw new IllegalStateException("Bedrock Converse response did not contain output.message.content[0].text: " + json);
                        }
                        return content.asText();
                    }

                    private JsonNode postJson(String path, Consumer<HttpHeaders> headersCustomizer, Map<String, Object> body) {
                        return webClient.post()
                                .uri(path)
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(headersCustomizer)
                                .bodyValue(body)
                                .retrieve()
                                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                                        .map(error -> new IllegalStateException("LLM provider request failed: " + error)))
                                .bodyToMono(JsonNode.class)
                                .block();
                    }

                    private Flux<String> postStream(String path, Consumer<HttpHeaders> headersCustomizer, Map<String, Object> body) {
                        return webClient.post()
                                .uri(path)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.TEXT_EVENT_STREAM, MediaType.APPLICATION_NDJSON, MediaType.APPLICATION_JSON)
                                .headers(headersCustomizer)
                                .bodyValue(body)
                                .retrieve()
                                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                                        .map(error -> new IllegalStateException("LLM provider stream request failed: " + error)))
                                .bodyToFlux(String.class);
                    }

                    private Map<String, Object> openAiBody(String model, double temperature, int maxTokens, boolean stream,
                                                           String systemPrompt, List<ChatMessage> history, String userMessage) {
                        Map<String, Object> body = new HashMap<>();
                        body.put("model", model);
                        body.put("temperature", temperature);
                        body.put("max_tokens", maxTokens);
                        body.put("stream", stream);
                        body.put("messages", openAiMessages(systemPrompt, history, userMessage));
                        return body;
                    }

                    private List<Map<String, String>> openAiMessages(String systemPrompt, List<ChatMessage> history, String userMessage) {
                        List<Map<String, String>> messages = new ArrayList<>();
                        if (systemPrompt != null && !systemPrompt.isBlank()) {
                            messages.add(Map.of("role", "system", "content", systemPrompt));
                        }
                        if (history != null) {
                            for (ChatMessage message : history) {
                                if (message.getRole() != null && message.getContent() != null && !message.getContent().isBlank()) {
                                    messages.add(Map.of("role", message.getRole().name().toLowerCase(), "content", message.getContent()));
                                }
                            }
                        }
                        messages.add(Map.of("role", "user", "content", nullToEmpty(userMessage)));
                        return messages;
                    }

                    private List<Map<String, String>> anthropicMessages(List<ChatMessage> history, String userMessage) {
                        List<Map<String, String>> messages = new ArrayList<>();
                        if (history != null) {
                            for (ChatMessage message : history) {
                                if (message.getRole() != null && message.getContent() != null && !message.getContent().isBlank()) {
                                    String role = "assistant".equalsIgnoreCase(message.getRole().name()) ? "assistant" : "user";
                                    messages.add(Map.of("role", role, "content", message.getContent()));
                                }
                            }
                        }
                        messages.add(Map.of("role", "user", "content", nullToEmpty(userMessage)));
                        return messages;
                    }

                    private List<Map<String, Object>> geminiContents(List<ChatMessage> history, String userMessage) {
                        List<Map<String, Object>> contents = new ArrayList<>();
                        if (history != null) {
                            for (ChatMessage message : history) {
                                if (message.getRole() != null && message.getContent() != null && !message.getContent().isBlank()) {
                                    String role = "assistant".equalsIgnoreCase(message.getRole().name()) ? "model" : "user";
                                    contents.add(Map.of("role", role, "parts", List.of(Map.of("text", message.getContent()))));
                                }
                            }
                        }
                        contents.add(Map.of("role", "user", "parts", List.of(Map.of("text", nullToEmpty(userMessage)))));
                        return contents;
                    }

                    private List<Map<String, Object>> bedrockMessages(List<ChatMessage> history, String userMessage) {
                        List<Map<String, Object>> messages = new ArrayList<>();
                        if (history != null) {
                            for (ChatMessage message : history) {
                                if (message.getRole() != null && message.getContent() != null && !message.getContent().isBlank()) {
                                    String role = "assistant".equalsIgnoreCase(message.getRole().name()) ? "assistant" : "user";
                                    messages.add(Map.of("role", role, "content", List.of(Map.of("text", message.getContent()))));
                                }
                            }
                        }
                        messages.add(Map.of("role", "user", "content", List.of(Map.of("text", nullToEmpty(userMessage)))));
                        return messages;
                    }

                    private Consumer<HttpHeaders> bearer(String apiKey) {
                        return headers -> headers.setBearerAuth(apiKey == null ? "" : apiKey);
                    }

                    private List<String> extractOpenAiStreamContent(String chunk) {
                        List<String> result = new ArrayList<>();
                        for (String line : chunk.split("\\n")) {
                            String data = line.strip();
                            if (data.startsWith("data:")) {
                                data = data.substring(5).trim();
                            }
                            if (data.isBlank() || "[DONE]".equals(data)) {
                                continue;
                            }
                            try {
                                JsonNode json = objectMapper.readTree(data);
                                JsonNode content = json.at("/choices/0/delta/content");
                                if (!content.isMissingNode()) {
                                    result.add(content.asText());
                                }
                            } catch (Exception ignored) {
                                // Ignore keep-alive or partial stream chunks.
                            }
                        }
                        return result;
                    }

                    private String extractOllamaStreamContent(String chunk) {
                        try {
                            JsonNode json = objectMapper.readTree(chunk);
                            return json.at("/message/content").asText("");
                        } catch (Exception ignored) {
                            return "";
                        }
                    }

                    private static String nullToEmpty(String value) {
                        return value == null ? "" : value;
                    }

                    private static String trimTrailingSlash(String value) {
                        if (value == null || value.isBlank()) return "";
                        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
                    }
                }
                """.formatted(pkg, pkg);
    }

    public String generateSpringAiLLMProviderAdapterSupport(String pkg) {
        return """
                package %s.infra.llm;

                import %s.domain.model.ChatMessage;
                import org.springframework.ai.chat.client.ChatClient;
                import reactor.core.publisher.Flux;

                import java.util.List;

                /**
                 * Spring AI based implementation shared by generated provider adapters.
                 *
                 * The actual model provider is created by Spring AI auto-configuration from
                 * spring.ai.* properties and provider starters on the classpath.
                 */
                public abstract class SpringAiLLMProviderAdapterSupport implements LLMProviderAdapter {

                    private final ChatClient chatClient;

                    protected SpringAiLLMProviderAdapterSupport(ChatClient.Builder chatClientBuilder) {
                        this.chatClient = chatClientBuilder.build();
                    }

                    @Override
                    public String invoke(String systemPrompt, String userMessage) {
                        return invokeWithHistory(systemPrompt, List.of(), userMessage);
                    }

                    @Override
                    public Flux<String> invokeStream(String systemPrompt, String userMessage) {
                        return chatClient.prompt()
                                .system(nullToEmpty(systemPrompt))
                                .user(userMessage)
                                .stream()
                                .content();
                    }

                    @Override
                    public String invokeWithHistory(String systemPrompt, List<ChatMessage> history, String userMessage) {
                        return chatClient.prompt()
                                .system(nullToEmpty(systemPrompt))
                                .user(toUserPrompt(history, userMessage))
                                .call()
                                .content();
                    }

                    private String toUserPrompt(List<ChatMessage> history, String userMessage) {
                        StringBuilder prompt = new StringBuilder();
                        if (history != null && !history.isEmpty()) {
                            prompt.append("Conversation history:\\n");
                            for (ChatMessage message : history) {
                                if (message != null && message.getRole() != null && message.getContent() != null) {
                                    prompt.append(message.getRole().name()).append(": ").append(message.getContent()).append('\\n');
                                }
                            }
                            prompt.append("\\nCurrent user message:\\n");
                        }
                        prompt.append(nullToEmpty(userMessage));
                        return prompt.toString();
                    }

                    private String nullToEmpty(String value) {
                        return value == null ? "" : value;
                    }
                }
                """.formatted(pkg, pkg);
    }

    public String generateLangChain4jLLMProviderAdapterSupport(String pkg) {
        return """
                package %s.infra.llm;

                import %s.domain.model.ChatMessage;
                import dev.langchain4j.model.chat.ChatLanguageModel;
                import reactor.core.publisher.Flux;

                import java.util.List;

                /**
                 * LangChain4j based implementation shared by generated provider adapters.
                 */
                public abstract class LangChain4jLLMProviderAdapterSupport implements LLMProviderAdapter {

                    protected abstract ChatLanguageModel chatModel();

                    @Override
                    public String invoke(String systemPrompt, String userMessage) {
                        return invokeWithHistory(systemPrompt, List.of(), userMessage);
                    }

                    @Override
                    public Flux<String> invokeStream(String systemPrompt, String userMessage) {
                        // Provider streaming model APIs differ by integration; expose a safe reactive wrapper.
                        return Flux.just(invoke(systemPrompt, userMessage));
                    }

                    @Override
                    public String invokeWithHistory(String systemPrompt, List<ChatMessage> history, String userMessage) {
                        return chatModel().chat(toPrompt(systemPrompt, history, userMessage));
                    }

                    private String toPrompt(String systemPrompt, List<ChatMessage> history, String userMessage) {
                        StringBuilder prompt = new StringBuilder();
                        if (systemPrompt != null && !systemPrompt.isBlank()) {
                            prompt.append("System: ").append(systemPrompt).append("\\n\\n");
                        }
                        if (history != null && !history.isEmpty()) {
                            prompt.append("Conversation history:\\n");
                            for (ChatMessage message : history) {
                                if (message != null && message.getRole() != null && message.getContent() != null) {
                                    prompt.append(message.getRole().name()).append(": ").append(message.getContent()).append('\\n');
                                }
                            }
                            prompt.append('\\n');
                        }
                        prompt.append("User: ").append(userMessage == null ? "" : userMessage);
                        return prompt.toString();
                    }
                }
                """.formatted(pkg, pkg);
    }

    public String generateProviderAdapter(String pkg, LLMProvider provider) {
        if (config.usesLangChain4j() && !config.usesSpringAI()) {
            return generateLangChain4jProviderAdapter(pkg, provider);
        }
        return generateSpringAiProviderAdapter(pkg, provider);
    }

    private String generateSpringAiProviderAdapter(String pkg, LLMProvider provider) {
        String className = providerAdapterClassName(provider.id());
        return """
                package %s.infra.llm;

                import org.springframework.ai.chat.client.ChatClient;
                import org.springframework.stereotype.Component;

                /**
                 * Provider adapter backed by Spring AI ChatClient.
                 */
                @Component
                public class %s extends SpringAiLLMProviderAdapterSupport {

                    public %s(ChatClient.Builder chatClientBuilder) {
                        super(chatClientBuilder);
                    }

                    @Override
                    public String providerName() {
                        return "%s";
                    }
                }
                """.formatted(pkg, className, className, provider.id());
    }

    private String generateLangChain4jProviderAdapter(String pkg, LLMProvider provider) {
        String className = providerAdapterClassName(provider.id());
        String modelClass = langChain4jModelClass(provider);
        String builder = langChain4jBuilder(provider);
        return """
                package %s.infra.llm;

                import dev.langchain4j.model.chat.ChatLanguageModel;
                import %s;
                import org.springframework.beans.factory.annotation.Value;
                import org.springframework.stereotype.Component;

                /**
                 * Provider adapter backed by the native LangChain4j provider integration.
                 */
                @Component
                public class %s extends LangChain4jLLMProviderAdapterSupport {

                    private final ChatLanguageModel chatModel;

                    public %s(
                            @Value("${scaffold4j.ai.providers.%s.base-url:%s}") String baseUrl,
                            @Value("${scaffold4j.ai.providers.%s.api-key:}") String apiKey,
                            @Value("${scaffold4j.ai.providers.%s.chat.model:%s}") String model,
                            @Value("${scaffold4j.ai.providers.%s.chat.temperature:0.7}") double temperature,
                            @Value("${scaffold4j.ai.providers.%s.chat.max-tokens:4096}") int maxTokens) {
                        this.chatModel = %s;
                    }

                    @Override
                    protected ChatLanguageModel chatModel() {
                        return chatModel;
                    }

                    @Override
                    public String providerName() {
                        return "%s";
                    }
                }
                """.formatted(pkg, modelClass, className, className,
                        provider.id(), defaultBaseUrl(provider),
                        provider.id(),
                        provider.id(), defaultModel(provider),
                        provider.id(),
                        provider.id(), builder,
                        provider.id());
    }

    public String generateLegacyHttpProviderAdapter(String pkg, LLMProvider provider) {
        String className = providerAdapterClassName(provider.id());
        String defaultBaseUrl = defaultBaseUrl(provider);
        String defaultModel = defaultModel(provider);
        String invokeMethod = invokeMethod(provider);
        String streamMethod = streamMethod(provider);
        String extraFields = provider == LLMProvider.AZURE_OPENAI ? "\n    private final String apiVersion;" : "";
        String extraConstructorArg = provider == LLMProvider.AZURE_OPENAI
                ? ",\n            @Value(\"${scaffold4j.ai.providers." + provider.id() + ".api-version:2024-10-21}\") String apiVersion"
                : "";
        String extraAssignment = provider == LLMProvider.AZURE_OPENAI ? "\n        this.apiVersion = apiVersion;" : "";
        return """
                package %s.infra.llm;

                import %s.domain.model.ChatMessage;
                import org.springframework.beans.factory.annotation.Value;
                import org.springframework.stereotype.Component;
                import org.springframework.web.reactive.function.client.WebClient;
                import reactor.core.publisher.Flux;

                import java.util.List;

                @Component
                public class %s extends HttpLLMProviderAdapterSupport {

                    private final String apiKey;
                    private final String model;
                    private final double temperature;
                    private final int maxTokens;%s

                    public %s(
                            WebClient.Builder webClientBuilder,
                            @Value("${scaffold4j.ai.providers.%s.base-url:%s}") String baseUrl,
                            @Value("${scaffold4j.ai.providers.%s.api-key:}") String apiKey,
                            @Value("${scaffold4j.ai.providers.%s.chat.model:%s}") String model,
                            @Value("${scaffold4j.ai.providers.%s.chat.temperature:0.7}") double temperature,
                            @Value("${scaffold4j.ai.providers.%s.chat.max-tokens:4096}") int maxTokens%s) {
                        super(webClientBuilder, baseUrl);
                        this.apiKey = apiKey;
                        this.model = model;
                        this.temperature = temperature;
                        this.maxTokens = maxTokens;%s
                    }

                    @Override
                    public String invoke(String systemPrompt, String userMessage) {
                        return invokeWithHistory(systemPrompt, List.of(), userMessage);
                    }

                    @Override
                    public Flux<String> invokeStream(String systemPrompt, String userMessage) {
                        %s
                    }

                    @Override
                    public String invokeWithHistory(String systemPrompt, List<ChatMessage> history, String userMessage) {
                        %s
                    }

                    @Override
                    public String providerName() {
                        return "%s";
                    }
                }
                """.formatted(pkg, pkg, className, extraFields, className,
                        provider.id(), defaultBaseUrl,
                        provider.id(),
                        provider.id(), defaultModel,
                        provider.id(),
                        provider.id(), extraConstructorArg, extraAssignment,
                        streamMethod, invokeMethod, provider.id());
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
                        String response = adapter.invoke(
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
                        return adapter.invokeStream(
                                "You are a helpful AI assistant.",
                                request.getMessage());
                    }

                    private LLMProviderAdapter resolveAdapter(String provider) {
                        if (provider != null && !provider.isBlank()) {
                            return factory.getAdapter(LLMProviderType.fromId(provider));
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
                        return adapter.invoke("You are a helpful agent with access to tools.", task);
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
                        %s.domain.dto.ChatRequest request = new %s.domain.dto.ChatRequest();
                        request.setMessage(task);
                        return chatService.chat(request).getContent();
                    }
                }
                """.formatted(pkg, pkg, pkg, pkg);
    }

    // ==================== api module ====================

    public String generateGlobalExceptionHandler(String pkg) {
        return """
                package %s.api.exception;

                import %s.common.constant.ErrorCode;
                import %s.common.exception.BaseException;
                import %s.common.result.Result;
                import jakarta.validation.ConstraintViolationException;
                import org.slf4j.Logger;
                import org.slf4j.LoggerFactory;
                import org.springframework.http.HttpStatus;
                import org.springframework.web.bind.MethodArgumentNotValidException;
                import org.springframework.web.bind.annotation.ExceptionHandler;
                import org.springframework.web.bind.annotation.ResponseStatus;
                import org.springframework.web.bind.annotation.RestControllerAdvice;

                import java.util.stream.Collectors;

                @RestControllerAdvice
                public class GlobalExceptionHandler {

                    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

                    @ExceptionHandler(BaseException.class)
                    @ResponseStatus(HttpStatus.BAD_REQUEST)
                    public Result<Void> handleBaseException(BaseException ex) {
                        return Result.error(ex.code(), ex.getMessage());
                    }

                    @ExceptionHandler(MethodArgumentNotValidException.class)
                    @ResponseStatus(HttpStatus.BAD_REQUEST)
                    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
                        String message = ex.getBindingResult().getFieldErrors().stream()
                                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                .collect(Collectors.joining("; "));
                        return Result.error(ErrorCode.BAD_REQUEST.code(), message.isBlank() ? ErrorCode.BAD_REQUEST.message() : message);
                    }

                    @ExceptionHandler(ConstraintViolationException.class)
                    @ResponseStatus(HttpStatus.BAD_REQUEST)
                    public Result<Void> handleConstraintViolation(ConstraintViolationException ex) {
                        String message = ex.getConstraintViolations().stream()
                                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                                .collect(Collectors.joining("; "));
                        return Result.error(ErrorCode.BAD_REQUEST.code(), message.isBlank() ? ErrorCode.BAD_REQUEST.message() : message);
                    }

                    @ExceptionHandler(Exception.class)
                    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    public Result<Void> handleException(Exception ex) {
                        log.error("Unhandled API exception", ex);
                        return Result.error(ErrorCode.INTERNAL_ERROR.code(), ErrorCode.INTERNAL_ERROR.message());
                    }
                }
                """.formatted(pkg, pkg, pkg, pkg);
    }

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
                                    ? "Beijing" : "Shanghai", "celsius"));
                        } else if (message.contains("search") || message.contains("搜索")) {
                            request.setMessage(searchTool.search(message, 5).toString());
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
                            case "WEATHER" -> weatherTool.getWeather("Shanghai", "celsius");
                            case "SEARCH" -> searchTool.search(message, 5).toString();
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

    // ==================== MQ Domain ====================

    public String generateMqMessage(String pkg) {
        return """
                package %s.domain.mq;

                import lombok.AllArgsConstructor;
                import lombok.Builder;
                import lombok.Data;
                import lombok.NoArgsConstructor;

                import java.util.Map;
                import java.util.UUID;

                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public class MqMessage<T> {

                    @Builder.Default
                    private String messageId = UUID.randomUUID().toString();

                    private String correlationId;
                    private String replyTo;
                    private Map<String, String> headers;
                    private T payload;

                    @Builder.Default
                    private long timestamp = System.currentTimeMillis();
                }
                """.formatted(pkg);
    }

    public String generateMqAIRequest(String pkg) {
        return """
                package %s.domain.mq;

                import lombok.AllArgsConstructor;
                import lombok.Builder;
                import lombok.Data;
                import lombok.NoArgsConstructor;

                import java.util.Map;

                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public class MqAIRequest {

                    private String conversationId;
                    private String prompt;

                    @Builder.Default
                    private Map<String, Object> context = Map.of();

                    @Builder.Default
                    private int maxTokens = 4096;

                    @Builder.Default
                    private double temperature = 0.7;
                }
                """.formatted(pkg);
    }

    public String generateMqAIResponse(String pkg) {
        return """
                package %s.domain.mq;

                import lombok.AllArgsConstructor;
                import lombok.Builder;
                import lombok.Data;
                import lombok.NoArgsConstructor;

                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public class MqAIResponse {

                    private String requestId;
                    private String conversationId;
                    private String content;
                    private String model;
                    private int tokensUsed;
                    private boolean success;
                    private String errorMessage;
                    private long processingTimeMs;
                }
                """.formatted(pkg);
    }

    // ==================== MQ Infra ====================

    public String generateMqConfig(String pkg) {
        return switch (config.mqType()) {
            case RABBITMQ -> generateRabbitMqConfig(pkg);
            case ROCKETMQ -> generateRocketMqConfig(pkg);
            case KAFKA -> generateKafkaMqConfig(pkg);
            default -> "";
        };
    }

    private String generateRabbitMqConfig(String pkg) {
        return """
                package %s.infra.mq;

                import org.springframework.amqp.core.Binding;
                import org.springframework.amqp.core.BindingBuilder;
                import org.springframework.amqp.core.DirectExchange;
                import org.springframework.amqp.core.Queue;
                import org.springframework.amqp.rabbit.connection.ConnectionFactory;
                import org.springframework.amqp.rabbit.core.RabbitTemplate;
                import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
                import org.springframework.context.annotation.Bean;
                import org.springframework.context.annotation.Configuration;

                @Configuration
                public class MqConfig {

                    public static final String REQUEST_QUEUE = "ai.requests";
                    public static final String RESPONSE_QUEUE = "ai.responses";
                    public static final String EXCHANGE = "ai.exchange";
                    public static final String REQUEST_ROUTING_KEY = "ai.request";
                    public static final String RESPONSE_ROUTING_KEY = "ai.response";

                    @Bean
                    public Queue requestQueue() {
                        return new Queue(REQUEST_QUEUE, true);
                    }

                    @Bean
                    public Queue responseQueue() {
                        return new Queue(RESPONSE_QUEUE, true);
                    }

                    @Bean
                    public DirectExchange aiExchange() {
                        return new DirectExchange(EXCHANGE);
                    }

                    @Bean
                    public Binding requestBinding(Queue requestQueue, DirectExchange aiExchange) {
                        return BindingBuilder.bind(requestQueue).to(aiExchange).with(REQUEST_ROUTING_KEY);
                    }

                    @Bean
                    public Binding responseBinding(Queue responseQueue, DirectExchange aiExchange) {
                        return BindingBuilder.bind(responseQueue).to(aiExchange).with(RESPONSE_ROUTING_KEY);
                    }

                    @Bean
                    public Jackson2JsonMessageConverter messageConverter() {
                        return new Jackson2JsonMessageConverter();
                    }

                    @Bean
                    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                                          Jackson2JsonMessageConverter messageConverter) {
                        RabbitTemplate template = new RabbitTemplate(connectionFactory);
                        template.setMessageConverter(messageConverter);
                        return template;
                    }
                }
                """.formatted(pkg);
    }

    private String generateRocketMqConfig(String pkg) {
        return """
                package %s.infra.mq;

                import org.apache.rocketmq.spring.core.RocketMQTemplate;
                import org.springframework.context.annotation.Bean;
                import org.springframework.context.annotation.Configuration;

                @Configuration
                public class MqConfig {

                    public static final String REQUEST_TOPIC = "ai-requests";
                    public static final String RESPONSE_TOPIC = "ai-responses";

                    @Bean
                    public RocketMQTemplate rocketMQTemplate(org.apache.rocketmq.spring.core.RocketMQTemplate template) {
                        return template;
                    }
                }
                """.formatted(pkg);
    }

    private String generateKafkaMqConfig(String pkg) {
        return """
                package %s.infra.mq;

                import org.apache.kafka.clients.admin.NewTopic;
                import org.springframework.context.annotation.Bean;
                import org.springframework.context.annotation.Configuration;
                import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
                import org.springframework.kafka.core.ConsumerFactory;
                import org.springframework.kafka.core.KafkaTemplate;
                import org.springframework.kafka.core.ProducerFactory;

                @Configuration
                public class MqConfig {

                    public static final String REQUEST_TOPIC = "ai.requests";
                    public static final String RESPONSE_TOPIC = "ai.responses";

                    @Bean
                    public NewTopic requestTopic() {
                        return new NewTopic(REQUEST_TOPIC, 3, (short) 1);
                    }

                    @Bean
                    public NewTopic responseTopic() {
                        return new NewTopic(RESPONSE_TOPIC, 3, (short) 1);
                    }

                    @Bean
                    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
                            ConsumerFactory<String, Object> consumerFactory) {
                        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                                new ConcurrentKafkaListenerContainerFactory<>();
                        factory.setConsumerFactory(consumerFactory);
                        factory.setConcurrency(4);
                        return factory;
                    }
                }
                """.formatted(pkg);
    }

    public String generateMqMessageProducer(String pkg) {
        return switch (config.mqType()) {
            case RABBITMQ -> generateRabbitMqMessageProducer(pkg);
            case ROCKETMQ -> generateRocketMqMessageProducer(pkg);
            case KAFKA -> generateKafkaMqMessageProducer(pkg);
            default -> "";
        };
    }

    private String generateRabbitMqMessageProducer(String pkg) {
        return """
                package %s.infra.mq;

                import %s.domain.mq.MqAIResponse;
                import %s.domain.mq.MqMessage;
                import lombok.RequiredArgsConstructor;
                import lombok.extern.slf4j.Slf4j;
                import org.springframework.amqp.rabbit.core.RabbitTemplate;
                import org.springframework.stereotype.Component;

                @Slf4j
                @Component
                @RequiredArgsConstructor
                public class MqMessageProducer {

                    private final RabbitTemplate rabbitTemplate;

                    public void sendResponse(MqMessage<MqAIResponse> message) {
                        String routingKey = message.getReplyTo() != null
                                ? message.getReplyTo() : MqConfig.RESPONSE_ROUTING_KEY;
                        rabbitTemplate.convertAndSend(MqConfig.EXCHANGE, routingKey, message);
                        log.debug("Sent AI response: messageId={}, correlationId={}",
                                message.getMessageId(), message.getCorrelationId());
                    }
                }
                """.formatted(pkg, pkg, pkg);
    }

    private String generateRocketMqMessageProducer(String pkg) {
        return """
                package %s.infra.mq;

                import %s.domain.mq.MqAIResponse;
                import %s.domain.mq.MqMessage;
                import lombok.RequiredArgsConstructor;
                import lombok.extern.slf4j.Slf4j;
                import org.apache.rocketmq.spring.core.RocketMQTemplate;
                import org.springframework.messaging.support.MessageBuilder;
                import org.springframework.stereotype.Component;

                @Slf4j
                @Component
                @RequiredArgsConstructor
                public class MqMessageProducer {

                    private final RocketMQTemplate rocketMQTemplate;

                    public void sendResponse(MqMessage<MqAIResponse> message) {
                        String topic = MqConfig.RESPONSE_TOPIC;
                        org.springframework.messaging.Message<MqMessage<MqAIResponse>> msg =
                                MessageBuilder.withPayload(message).build();
                        rocketMQTemplate.send(topic, msg);
                        log.debug("Sent AI response: messageId={}, correlationId={}",
                                message.getMessageId(), message.getCorrelationId());
                    }
                }
                """.formatted(pkg, pkg, pkg);
    }

    private String generateKafkaMqMessageProducer(String pkg) {
        return """
                package %s.infra.mq;

                import %s.domain.mq.MqAIResponse;
                import %s.domain.mq.MqMessage;
                import lombok.RequiredArgsConstructor;
                import lombok.extern.slf4j.Slf4j;
                import org.springframework.kafka.core.KafkaTemplate;
                import org.springframework.kafka.support.SendResult;
                import org.springframework.stereotype.Component;

                import java.util.concurrent.CompletableFuture;

                @Slf4j
                @Component
                @RequiredArgsConstructor
                public class MqMessageProducer {

                    private final KafkaTemplate<String, MqMessage<MqAIResponse>> kafkaTemplate;

                    public void sendResponse(MqMessage<MqAIResponse> message) {
                        CompletableFuture<SendResult<String, MqMessage<MqAIResponse>>> future =
                                kafkaTemplate.send(MqConfig.RESPONSE_TOPIC,
                                        message.getCorrelationId(), message);
                        future.whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("Failed to send AI response: messageId={}", message.getMessageId(), ex);
                            } else {
                                log.debug("Sent AI response: messageId={}, offset={}",
                                        message.getMessageId(), result.getRecordMetadata().offset());
                            }
                        });
                    }
                }
                """.formatted(pkg, pkg, pkg);
    }

    public String generateMqMessageListener(String pkg) {
        return switch (config.mqType()) {
            case RABBITMQ -> generateRabbitMqMessageListener(pkg);
            case ROCKETMQ -> generateRocketMqMessageListener(pkg);
            case KAFKA -> generateKafkaMqMessageListener(pkg);
            default -> "";
        };
    }

    private String generateRabbitMqMessageListener(String pkg) {
        return """
                package %s.app.mq;

                import %s.domain.mq.MqAIRequest;
                import %s.domain.mq.MqMessage;
                import %s.infra.mq.MqConfig;
                import com.fasterxml.jackson.databind.ObjectMapper;
                import lombok.RequiredArgsConstructor;
                import lombok.extern.slf4j.Slf4j;
                import org.springframework.amqp.rabbit.annotation.RabbitListener;
                import org.springframework.stereotype.Component;

                @Slf4j
                @Component
                @RequiredArgsConstructor
                public class MqMessageListener {

                    private final MqAIProcessingService processingService;
                    private final ObjectMapper objectMapper;

                    @RabbitListener(queues = MqConfig.REQUEST_QUEUE)
                    public void handleRequest(MqMessage<MqAIRequest> message) {
                        log.info("Received AI request: messageId={}, correlationId={}",
                                message.getMessageId(), message.getCorrelationId());
                        try {
                            processingService.process(message);
                        } catch (Exception e) {
                            log.error("Failed to process AI request: messageId={}", message.getMessageId(), e);
                        }
                    }
                }
                """.formatted(pkg, pkg, pkg, pkg);
    }

    private String generateRocketMqMessageListener(String pkg) {
        return """
                package %s.app.mq;

                import %s.domain.mq.MqAIRequest;
                import %s.domain.mq.MqMessage;
                import %s.infra.mq.MqConfig;
                import com.fasterxml.jackson.databind.ObjectMapper;
                import lombok.RequiredArgsConstructor;
                import lombok.extern.slf4j.Slf4j;
                import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
                import org.apache.rocketmq.spring.core.RocketMQListener;
                import org.springframework.stereotype.Component;

                @Slf4j
                @Component
                @RequiredArgsConstructor
                @RocketMQMessageListener(
                    topic = MqConfig.REQUEST_TOPIC,
                    consumerGroup = "${rocketmq.consumer.group:scaffold4j-consumer}"
                )
                public class MqMessageListener implements RocketMQListener<MqMessage<MqAIRequest>> {

                    private final MqAIProcessingService processingService;

                    @Override
                    public void onMessage(MqMessage<MqAIRequest> message) {
                        log.info("Received AI request: messageId={}, correlationId={}",
                                message.getMessageId(), message.getCorrelationId());
                        try {
                            processingService.process(message);
                        } catch (Exception e) {
                            log.error("Failed to process AI request: messageId={}", message.getMessageId(), e);
                        }
                    }
                }
                """.formatted(pkg, pkg, pkg, pkg);
    }

    private String generateKafkaMqMessageListener(String pkg) {
        return """
                package %s.app.mq;

                import %s.domain.mq.MqAIRequest;
                import %s.domain.mq.MqMessage;
                import %s.infra.mq.MqConfig;
                import lombok.RequiredArgsConstructor;
                import lombok.extern.slf4j.Slf4j;
                import org.springframework.kafka.annotation.KafkaListener;
                import org.springframework.stereotype.Component;

                @Slf4j
                @Component
                @RequiredArgsConstructor
                public class MqMessageListener {

                    private final MqAIProcessingService processingService;

                    @KafkaListener(
                        topics = MqConfig.REQUEST_TOPIC,
                        groupId = "${spring.kafka.consumer.group-id:scaffold4j-consumer}",
                        containerFactory = "kafkaListenerContainerFactory"
                    )
                    public void handleRequest(MqMessage<MqAIRequest> message) {
                        log.info("Received AI request: messageId={}, correlationId={}",
                                message.getMessageId(), message.getCorrelationId());
                        try {
                            processingService.process(message);
                        } catch (Exception e) {
                            log.error("Failed to process AI request: messageId={}", message.getMessageId(), e);
                        }
                    }
                }
                """.formatted(pkg, pkg, pkg, pkg);
    }

    // ==================== MQ App ====================

    public String generateMqAIProcessingService(String pkg) {
        return """
                package %s.app.mq;

                import %s.domain.mq.MqAIRequest;
                import %s.domain.mq.MqAIResponse;
                import %s.domain.mq.MqMessage;
                import %s.infra.mq.MqMessageProducer;
                import %s.app.service.ChatService;
                import %s.domain.dto.ChatRequest;
                import %s.domain.dto.ChatResponse;
                import lombok.RequiredArgsConstructor;
                import lombok.extern.slf4j.Slf4j;
                import org.springframework.stereotype.Service;

                @Slf4j
                @Service
                @RequiredArgsConstructor
                public class MqAIProcessingService {

                    private final ChatService chatService;
                    private final MqMessageProducer messageProducer;

                    public void process(MqMessage<MqAIRequest> requestMessage) {
                        long startTime = System.currentTimeMillis();
                        MqAIRequest request = requestMessage.getPayload();

                        try {
                            log.info("Processing AI request: messageId={}, prompt=\\"{}\\"",
                                    requestMessage.getMessageId(), request.getPrompt());

                            ChatRequest chatRequest = new ChatRequest();
                            chatRequest.setConversationId(request.getConversationId());
                            chatRequest.setMessage(request.getPrompt());

                            ChatResponse chatResponse = chatService.chat(chatRequest);

                            MqAIResponse response = MqAIResponse.builder()
                                    .requestId(requestMessage.getMessageId())
                                    .conversationId(request.getConversationId())
                                    .content(chatResponse.getContent())
                                    .model(chatResponse.getModel())
                                    .tokensUsed(chatResponse.getTokensUsed())
                                    .success(true)
                                    .processingTimeMs(System.currentTimeMillis() - startTime)
                                    .build();

                            MqMessage<MqAIResponse> responseMessage = MqMessage.<MqAIResponse>builder()
                                    .correlationId(requestMessage.getCorrelationId())
                                    .replyTo(requestMessage.getReplyTo())
                                    .payload(response)
                                    .build();

                            messageProducer.sendResponse(responseMessage);
                            log.info("AI request processed successfully: messageId={}, elapsed={}ms",
                                    requestMessage.getMessageId(), response.getProcessingTimeMs());

                        } catch (Exception e) {
                            log.error("AI request processing failed: messageId={}", requestMessage.getMessageId(), e);

                            MqAIResponse errorResponse = MqAIResponse.builder()
                                    .requestId(requestMessage.getMessageId())
                                    .conversationId(request.getConversationId())
                                    .success(false)
                                    .errorMessage(e.getMessage())
                                    .processingTimeMs(System.currentTimeMillis() - startTime)
                                    .build();

                            MqMessage<MqAIResponse> responseMessage = MqMessage.<MqAIResponse>builder()
                                    .correlationId(requestMessage.getCorrelationId())
                                    .replyTo(requestMessage.getReplyTo())
                                    .payload(errorResponse)
                                    .build();

                            messageProducer.sendResponse(responseMessage);
                        }
                    }
                }
                """.formatted(pkg, pkg, pkg, pkg, pkg, pkg, pkg, pkg);
    }

    // ==================== utility ====================

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static String providerAdapterClassName(String id) {
        if (id == null || id.isBlank()) return "ProviderAdapter";
        StringBuilder sb = new StringBuilder();
        for (String part : id.split("[-_]+")) {
            if (part.isEmpty()) continue;
            sb.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) sb.append(part.substring(1));
        }
        return sb.append("Adapter").toString();
    }

    private static String defaultBaseUrl(LLMProvider provider) {
        return switch (provider) {
            case OPENAI -> "https://api.openai.com";
            case OLLAMA -> "http://localhost:11434";
            case ANTHROPIC -> "https://api.anthropic.com";
            case DEEPSEEK -> "https://api.deepseek.com";
            case ZHIPUAI -> "https://open.bigmodel.cn/api/paas";
            case VERTEX_AI -> "https://generativelanguage.googleapis.com/v1beta";
            case AZURE_OPENAI -> "https://YOUR_RESOURCE.openai.azure.com";
            case BEDROCK -> "https://bedrock-runtime.us-east-1.amazonaws.com";
            case QWEN -> "https://dashscope.aliyuncs.com/compatible-mode";
            case MOONSHOT -> "https://api.moonshot.cn";
            case DOUBAO -> "https://ark.cn-beijing.volces.com/api";
        };
    }

    private static String defaultModel(LLMProvider provider) {
        return switch (provider) {
            case OPENAI -> "gpt-4o";
            case ANTHROPIC -> "claude-sonnet-4-6";
            case OLLAMA -> "llama3.1";
            case DEEPSEEK -> "deepseek-chat";
            case ZHIPUAI -> "glm-4-flash";
            case VERTEX_AI -> "gemini-2.5-flash";
            case AZURE_OPENAI -> "gpt-4o";
            case BEDROCK -> "anthropic.claude-3-5-sonnet-20240620-v1:0";
            case QWEN -> "qwen-plus";
            case MOONSHOT -> "moonshot-v1-8k";
            case DOUBAO -> "doubao-lite-128k";
        };
    }

    private static String langChain4jModelClass(LLMProvider provider) {
        return switch (provider) {
            case OPENAI -> "dev.langchain4j.model.openai.OpenAiChatModel";
            case OLLAMA -> "dev.langchain4j.model.ollama.OllamaChatModel";
            case ANTHROPIC -> "dev.langchain4j.model.anthropic.AnthropicChatModel";
            case DEEPSEEK -> "dev.langchain4j.model.deepseek.DeepSeekChatModel";
            case AZURE_OPENAI -> "dev.langchain4j.model.azure.AzureOpenAiChatModel";
            case VERTEX_AI -> "dev.langchain4j.model.vertexai.gemini.VertexAiGeminiChatModel";
            case BEDROCK -> "dev.langchain4j.model.bedrock.BedrockChatModel";
            default -> throw new IllegalArgumentException("Provider " + provider.id() + " does not have LangChain4j support.");
        };
    }

    private static String langChain4jBuilder(LLMProvider provider) {
        return switch (provider) {
            case OLLAMA -> """
                    OllamaChatModel.builder()
                                            .baseUrl(baseUrl)
                                            .modelName(model)
                                            .temperature(temperature)
                                            .build()""";
            case ANTHROPIC -> """
                    AnthropicChatModel.builder()
                                            .apiKey(apiKey)
                                            .modelName(model)
                                            .temperature(temperature)
                                            .maxTokens(maxTokens)
                                            .build()""";
            case AZURE_OPENAI -> """
                    AzureOpenAiChatModel.builder()
                                            .endpoint(baseUrl)
                                            .apiKey(apiKey)
                                            .deploymentName(model)
                                            .temperature(temperature)
                                            .maxTokens(maxTokens)
                                            .build()""";
            case VERTEX_AI -> """
                    VertexAiGeminiChatModel.builder()
                                            .modelName(model)
                                            .temperature(temperature)
                                            .maxOutputTokens(maxTokens)
                                            .build()""";
            case BEDROCK -> """
                    BedrockChatModel.builder()
                                            .modelId(model)
                                            .temperature(temperature)
                                            .maxTokens(maxTokens)
                                            .build()""";
            case OPENAI, DEEPSEEK -> langChain4jOpenAiCompatibleBuilder(provider);
            default -> throw new IllegalArgumentException("Provider " + provider.id() + " does not have LangChain4j support.");
        };
    }

    private static String langChain4jOpenAiCompatibleBuilder(LLMProvider provider) {
        String className = switch (provider) {
            case OPENAI -> "OpenAiChatModel";
            case DEEPSEEK -> "DeepSeekChatModel";
            default -> throw new IllegalArgumentException("Provider " + provider.id() + " is not OpenAI-compatible in LangChain4j.");
        };
        return """
                %s.builder()
                                        .apiKey(apiKey)
                                        .baseUrl(baseUrl)
                                        .modelName(model)
                                        .temperature(temperature)
                                        .maxTokens(maxTokens)
                                        .build()""".formatted(className);
    }

    private static String invokeMethod(LLMProvider provider) {
        return switch (provider) {
            case OLLAMA -> "return ollamaInvoke(model, systemPrompt, history, userMessage);";
            case ANTHROPIC -> "return anthropicInvoke(apiKey, model, temperature, maxTokens, systemPrompt, history, userMessage);";
            case AZURE_OPENAI -> "return azureOpenAiInvoke(apiKey, apiVersion, model, temperature, maxTokens, systemPrompt, history, userMessage);";
            case VERTEX_AI -> "return geminiInvoke(apiKey, model, temperature, maxTokens, systemPrompt, history, userMessage);";
            case BEDROCK -> "return bedrockConverseInvoke(apiKey, model, temperature, maxTokens, systemPrompt, history, userMessage);";
            default -> "return openAiCompatibleInvoke(apiKey, model, temperature, maxTokens, systemPrompt, history, userMessage);";
        };
    }

    private static String streamMethod(LLMProvider provider) {
        return switch (provider) {
            case OLLAMA -> "return ollamaStream(model, systemPrompt, List.of(), userMessage);";
            case AZURE_OPENAI -> "return azureOpenAiStream(apiKey, apiVersion, model, temperature, maxTokens, systemPrompt, List.of(), userMessage);";
            case OPENAI, DEEPSEEK, ZHIPUAI, QWEN, MOONSHOT, DOUBAO -> "return openAiCompatibleStream(apiKey, model, temperature, maxTokens, systemPrompt, List.of(), userMessage);";
            default -> "return Flux.just(invoke(systemPrompt, userMessage));";
        };
    }
}
