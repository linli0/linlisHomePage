package com.coffeecookies.homepage.service;

import com.coffeecookies.homepage.entity.Tweet;
import com.coffeecookies.homepage.entity.enums.Platform;
import com.coffeecookies.homepage.repository.TweetRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "features.tweets", name = "enabled", havingValue = "true")
public class TwitterStreamService {

    private final TweetRepository tweetRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final WebClient.Builder webClientBuilder;
    
    @Value("${twitter.enabled:true}")
    private boolean twitterEnabled;
    
    @Value("${twitter.streaming.enabled:true}")
    private boolean streamingEnabled;
    
    @Value("${twitter.streaming.bearer-token}")
    private String bearerToken;
    
    @Value("${twitter.api.auth-token:}")
    private String authToken;
    
    @Value("${twitter.api.ct0:}")
    private String ct0;

    private static final String STREAM_RULES_ENDPOINT = "https://api.twitter.com/2/tweets/search/stream/rules";
    private static final String STREAM_ENDPOINT = "https://api.twitter.com/2/tweets/search/stream";
    private static final String WEBSOCKET_TOPIC = "/topic/tweets/new";

    @PostConstruct
    public void startStreaming() {
        if (!twitterEnabled || !streamingEnabled) {
            log.info("Twitter streaming is disabled. Skipping stream initialization.");
            return;
        }
        
        if (bearerToken == null || bearerToken.isEmpty()) {
            log.error("Twitter Bearer Token is not configured. Streaming cannot start.");
            return;
        }
        
        log.info("Starting Twitter filtered streaming service...");
        initializeAndStartStream();
    }

    private void initializeAndStartStream() {
        try {
            WebClient webClient = webClientBuilder.build();
            
            // Configure stream rules
            configureStreamRules(webClient);
            
            // Establish stream connection with exponential backoff
            establishStreamConnectionWithBackoff(webClient);
            
        } catch (Exception e) {
            log.error("Failed to initialize Twitter stream: {}", e.getMessage(), e);
        }
    }

    private void configureStreamRules(WebClient webClient) {
        try {
            log.info("Configuring Twitter stream rules...");
            
            // First, get existing rules to delete them
            String existingRulesResponse = webClient.get()
                .uri(STREAM_RULES_ENDPOINT)
                .header("Authorization", "Bearer " + bearerToken)
                .retrieve()
                .bodyToMono(String.class)
                .block(Duration.ofSeconds(10));
            
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode existingRules = objectMapper.readTree(existingRulesResponse);
            
            // Delete existing rules if any
            if (existingRules.has("data") && existingRules.get("data").isArray() && 
                existingRules.get("data").size() > 0) {
                
                List<String> ruleIds = new ArrayList<>();
                for (JsonNode rule : existingRules.get("data")) {
                    if (rule.has("id")) {
                        ruleIds.add(rule.get("id").asText());
                    }
                }
                
                if (!ruleIds.isEmpty()) {
                    log.info("Deleting {} existing stream rules", ruleIds.size());
                    String deletePayload = objectMapper.writeValueAsString(
                        objectMapper.createObjectNode()
                            .set("delete", objectMapper.createObjectNode()
                                .set("ids", objectMapper.valueToTree(ruleIds)))
                    );
                    
                    webClient.post()
                        .uri(STREAM_RULES_ENDPOINT)
                        .header("Authorization", "Bearer " + bearerToken)
                        .header("Content-Type", "application/json")
                        .bodyValue(deletePayload)
                        .retrieve()
                        .toBodilessEntity()
                        .block(Duration.ofSeconds(10));
                }
            }
            
            // Add new rules
            String ruleValue = "from:realDonaldTrump OR from:POTUS";
            String ruleTag = "trump_accounts";
            
            String addRulePayload = objectMapper.writeValueAsString(
                objectMapper.createObjectNode()
                    .set("add", objectMapper.createArrayNode()
                        .add(objectMapper.createObjectNode()
                            .put("value", ruleValue)
                            .put("tag", ruleTag)))
            );
            
            log.info("Adding stream rule: {}", ruleValue);
            webClient.post()
                .uri(STREAM_RULES_ENDPOINT)
                .header("Authorization", "Bearer " + bearerToken)
                .header("Content-Type", "application/json")
                .bodyValue(addRulePayload)
                .retrieve()
                .bodyToMono(String.class)
                .block(Duration.ofSeconds(10));
            
            log.info("Twitter stream rules configured successfully");
            
        } catch (Exception e) {
            log.error("Failed to configure Twitter stream rules: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to configure stream rules", e);
        }
    }

    private void establishStreamConnectionWithBackoff(WebClient webClient) {
        AtomicInteger backoffDelay = new AtomicInteger(1); // Start with 1 second
        final int MAX_BACKOFF_DELAY = 16; // Max 16 seconds
        
        Runnable streamTask = () -> {
            while (true) {
                try {
                    log.info("Establishing Twitter stream connection...");
                    establishStreamConnection(webClient);
                    // If we reach here, the stream ended normally (shouldn't happen in normal operation)
                    log.warn("Twitter stream connection ended unexpectedly");
                } catch (Exception e) {
                    log.error("Twitter stream connection failed: {}", e.getMessage(), e);
                    
                    // Check for specific error conditions
                    if (isAuthenticationError(e)) {
                        log.error("Authentication error detected. Stopping reconnection attempts.");
                        return;
                    }
                    
                    if (isRateLimitError(e)) {
                        log.warn("Rate limit error detected. Applying maximum backoff.");
                        backoffDelay.set(MAX_BACKOFF_DELAY);
                    }
                    
                    // Apply exponential backoff
                    int currentDelay = Math.min(backoffDelay.get(), MAX_BACKOFF_DELAY);
                    log.info("Reconnecting in {} seconds...", currentDelay);
                    
                    try {
                        Thread.sleep(currentDelay * 1000L);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.info("Stream reconnection interrupted");
                        return;
                    }
                    
                    // Double the backoff delay for next attempt (capped at MAX_BACKOFF_DELAY)
                    backoffDelay.set(Math.min(backoffDelay.get() * 2, MAX_BACKOFF_DELAY));
                }
            }
        };
        
        // Run stream in separate thread to avoid blocking application startup
        Thread streamThread = new Thread(streamTask, "Twitter-Stream-Thread");
        streamThread.setDaemon(true);
        streamThread.start();
    }

    private void establishStreamConnection(WebClient webClient) {
        URI streamUri = UriComponentsBuilder.fromHttpUrl(STREAM_ENDPOINT)
            .queryParam("tweet.fields", "id,text,created_at,public_metrics,author_id,entities")
            .queryParam("user.fields", "username")
            .build()
            .toUri();
        
        // Build headers
        WebClient.RequestHeadersSpec<?> request = webClient.get()
            .uri(streamUri)
            .header("Authorization", "Bearer " + bearerToken);
        
        // Add cookie headers if available
        if (authToken != null && !authToken.isEmpty()) {
            request.header("Cookie", "auth_token=" + authToken);
        }
        
        if (ct0 != null && !ct0.isEmpty()) {
            request.header("x-csrf-token", ct0);
        }
        
        // Process the stream
        Flux<ByteBuffer> byteBufferFlux = request
            .retrieve()
            .bodyToFlux(ByteBuffer.class);
        
        byteBufferFlux
            .publishOn(Schedulers.boundedElastic())
            .doOnNext(this::processTweetChunk)
            .doOnError(error -> log.error("Error in Twitter stream: {}", error.getMessage(), error))
            .doOnComplete(() -> log.info("Twitter stream completed"))
            .blockLast(Duration.ofHours(24)); // Keep stream alive for up to 24 hours
    }

    private void processTweetChunk(ByteBuffer chunk) {
        try {
            if (chunk.hasArray()) {
                String line = new String(chunk.array(), chunk.position(), chunk.remaining()).trim();
                if (!line.isEmpty() && !line.startsWith("{")) {
                    // Skip non-JSON lines (like keep-alive messages)
                    return;
                }
                
                if (!line.isEmpty()) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode tweetJson = objectMapper.readTree(line);
                    
                    // Skip errors or disconnect messages
                    if (tweetJson.has("error") || tweetJson.has("disconnect")) {
                        log.warn("Received error/disconnect message from Twitter stream: {}", line);
                        return;
                    }
                    
                    // Parse and save tweet
                    Tweet tweet = parseTweet(tweetJson);
                    if (tweet != null && tweet.getTweetId() != null) {
                        // Check if tweet already exists
                        if (!tweetRepository.findByTweetIdAndPlatform(tweet.getTweetId(), Platform.TWITTER.name()).isPresent()) {
                            Tweet savedTweet = tweetRepository.save(tweet);
                            log.info("Saved new tweet: {} from {}", savedTweet.getTweetId(), savedTweet.getUsername());
                            
                            // Broadcast to WebSocket
                            messagingTemplate.convertAndSend(WEBSOCKET_TOPIC, savedTweet);
                        } else {
                            log.debug("Skipping duplicate tweet: {}", tweet.getTweetId());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to process tweet chunk: {}", e.getMessage(), e);
        }
    }

    private Tweet parseTweet(JsonNode tweetJson) {
        try {
            if (!tweetJson.has("data")) {
                return null;
            }
            
            JsonNode data = tweetJson.get("data");
            
            Tweet.TweetBuilder builder = Tweet.builder()
                .platform(Platform.TWITTER);
            
            // Extract basic fields
            if (data.has("id")) {
                builder.tweetId(data.get("id").asText());
            }
            
            if (data.has("text")) {
                builder.content(data.get("text").asText());
            }
            
            if (data.has("created_at")) {
                try {
                    // Parse ISO 8601 date format: "2023-01-01T12:00:00.000Z"
                    String createdAtStr = data.get("created_at").asText();
                    java.time.LocalDateTime createdAt = java.time.OffsetDateTime.parse(
                        createdAtStr.replace("Z", "+00:00"),
                        java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
                    ).toLocalDateTime();
                    builder.createdAt(createdAt);
                } catch (Exception e) {
                    log.warn("Failed to parse created_at: {}", data.get("created_at").asText());
                }
            }
            
            // Extract author info
            if (data.has("author_id")) {
                String authorId = data.get("author_id").asText();
                // Try to get username from includes if available
                if (tweetJson.has("includes") && tweetJson.get("includes").has("users")) {
                    for (JsonNode user : tweetJson.get("includes").get("users")) {
                        if (user.has("id") && user.get("id").asText().equals(authorId) && user.has("username")) {
                            builder.username(user.get("username").asText());
                            break;
                        }
                    }
                }
                // Fallback: use author_id as username if no username found
                if (builder.build().getUsername() == null) {
                    builder.username(authorId);
                }
            }
            
            // Extract public metrics
            if (data.has("public_metrics")) {
                JsonNode metrics = data.get("public_metrics");
                if (metrics.has("like_count")) {
                    builder.likesCount(metrics.get("like_count").asInt());
                }
                if (metrics.has("retweet_count")) {
                    builder.retweetsCount(metrics.get("retweet_count").asInt());
                }
                if (metrics.has("reply_count")) {
                    builder.repliesCount(metrics.get("reply_count").asInt());
                }
            }
            
            // Extract entities (hashtags, mentions)
            ObjectMapper objectMapper = new ObjectMapper();
            if (data.has("entities")) {
                JsonNode entities = data.get("entities");
                
                // Hashtags
                if (entities.has("hashtags")) {
                    List<String> hashtags = new ArrayList<>();
                    for (JsonNode hashtag : entities.get("hashtags")) {
                        if (hashtag.has("tag")) {
                            hashtags.add(hashtag.get("tag").asText());
                        }
                    }
                    if (!hashtags.isEmpty()) {
                        builder.hashtags(objectMapper.writeValueAsString(hashtags));
                    }
                }
                
                // Mentions
                if (entities.has("mentions")) {
                    List<String> mentions = new ArrayList<>();
                    for (JsonNode mention : entities.get("mentions")) {
                        if (mention.has("username")) {
                            mentions.add(mention.get("username").asText());
                        }
                    }
                    if (!mentions.isEmpty()) {
                        builder.mentions(objectMapper.writeValueAsString(mentions));
                    }
                }
            }
            
            return builder.build();
            
        } catch (Exception e) {
            log.error("Failed to parse tweet JSON: {}", e.getMessage(), e);
            return null;
        }
    }

    private boolean isAuthenticationError(Exception e) {
        String message = e.getMessage();
        return message != null && 
               (message.contains("401") || message.contains("403") || 
                message.contains("Unauthorized") || message.contains("Forbidden"));
    }

    private boolean isRateLimitError(Exception e) {
        String message = e.getMessage();
        return message != null && (message.contains("429") || message.contains("rate limit"));
    }
}