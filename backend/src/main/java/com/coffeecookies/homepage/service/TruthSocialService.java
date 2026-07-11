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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "features.tweets", name = "enabled", havingValue = "true")
public class TruthSocialService {

    private final TweetRepository tweetRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final WebClient.Builder webClientBuilder;
    
    @Value("${truth-social.enabled}")
    private boolean truthSocialEnabled;
    
    @Value("${truth-social.api.enabled}")
    private boolean truthSocialApiEnabled;
    
    @Value("${truth-social.api.url:https://api.scrapecreators.com/v1/truthsocial}")
    private String truthSocialApiUrl;
    
    @Value("${truth-social.api.key:}")
    private String truthSocialApiKey;
    
    // Cache for rate limiting - expires after 5 minutes (300 seconds)
    private final Cache<String, LocalDateTime> lastFetchCache = Caffeine.newBuilder()
            .expireAfterWrite(300, TimeUnit.SECONDS)
            .build();
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Fetch tweets from Truth Social API for a given username
     * @param username The Truth Social username to fetch tweets for
     */
    @Transactional
    public void fetchTruthSocialTweets(String username) {
        // Skip if Truth Social is disabled globally
        if (!truthSocialEnabled) {
            log.debug("Truth Social is disabled globally, skipping fetch for username: {}", username);
            return;
        }
        
        // Skip if Truth Social API is disabled
        if (!truthSocialApiEnabled) {
            log.debug("Truth Social API is disabled, skipping fetch for username: {}", username);
            return;
        }
        
        // Check rate limiting - skip if fetched within last 5 minutes
        LocalDateTime lastFetch = lastFetchCache.getIfPresent(username);
        if (lastFetch != null) {
            log.debug("Rate limit: Skipping fetch for {} - last fetch was at {}", username, lastFetch);
            return;
        }
        
        // Validate API key
        if (truthSocialApiKey == null || truthSocialApiKey.isEmpty()) {
            log.warn("Truth Social API key is not configured. Please set TRUTH_SOCIAL_API_KEY environment variable");
            return;
        }
        
        try {
            WebClient webClient = webClientBuilder.build();
            
            // Call Truth Social API
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("api.scrapecreators.com")
                            .path("/v1/truthsocial/user/posts")
                            .queryParam("handle", username)
                            .build())
                    .header("x-api-key", truthSocialApiKey)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            if (response == null) {
                log.warn("Truth Social API returned null response for username: {}", username);
                return;
            }
            
            // Parse JSON response
            JsonNode rootNode = objectMapper.readTree(response);
            
            // Check if response has posts array
            if (!rootNode.has("posts") || !rootNode.get("posts").isArray()) {
                log.warn("Truth Social API response missing posts array for username: {}", username);
                return;
            }
            
            List<Tweet> newTweets = new ArrayList<>();
            JsonNode postsArray = rootNode.get("posts");
            
            for (JsonNode post : postsArray) {
                try {
                    Tweet tweet = parseTruthSocialPost(post);
                    if (tweet != null && tweet.getTweetId() != null) {
                        // Check if tweet already exists in database
                        var existingTweet = tweetRepository.findByTweetIdAndPlatform(
                                tweet.getTweetId(), Platform.TRUTH_SOCIAL.name());
                        
                        if (existingTweet.isEmpty()) {
                            newTweets.add(tweet);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse Truth Social post: {}", e.getMessage());
                }
            }
            
            // Save new tweets to database
            if (!newTweets.isEmpty()) {
                List<Tweet> savedTweets = tweetRepository.saveAll(newTweets);
                log.info("Saved {} new Truth Social tweets for username: {}", savedTweets.size(), username);
                
                // Broadcast new tweets to WebSocket clients
                for (Tweet tweet : savedTweets) {
                    try {
                        messagingTemplate.convertAndSend("/topic/tweets/new", tweet);
                    } catch (Exception e) {
                        log.warn("Failed to broadcast tweet via WebSocket: {}", e.getMessage());
                    }
                }
            } else {
                log.debug("No new Truth Social tweets found for username: {}", username);
            }
            
            // Update last fetch timestamp
            lastFetchCache.put(username, LocalDateTime.now());
            
        } catch (WebClientResponseException e) {
            handleWebClientException(e, username);
        } catch (Exception e) {
            log.error("Unexpected error fetching Truth Social tweets for {}: {}", username, e.getMessage(), e);
        }
    }
    
    /**
     * Parse a Truth Social post JSON into a Tweet entity
     * @param postJson The JSON node representing a Truth Social post
     * @return Tweet entity or null if parsing fails
     */
    private Tweet parseTruthSocialPost(JsonNode postJson) {
        try {
            if (postJson == null || !postJson.isObject()) {
                return null;
            }
            
            // Extract required fields
            String id = postJson.has("id") ? postJson.get("id").asText() : null;
            String text = postJson.has("text") ? postJson.get("text").asText() : "";
            String createdAtStr = postJson.has("created_at") ? postJson.get("created_at").asText() : null;
            
            if (id == null) {
                return null;
            }
            
            // Parse created_at timestamp
            LocalDateTime createdAt = null;
            if (createdAtStr != null) {
                try {
                    // Try to parse ISO 8601 format
                    createdAt = LocalDateTime.parse(createdAtStr.replace("Z", "+00:00"));
                } catch (Exception e) {
                    log.debug("Failed to parse created_at timestamp: {}", createdAtStr);
                }
            }
            
            // Extract engagement metrics
            Integer likesCount = postJson.has("likes_count") ? 
                    postJson.get("likes_count").asInt() : 0;
            Integer sharesCount = postJson.has("shares_count") ? 
                    postJson.get("shares_count").asInt() : 0;
            
            // Extract media URLs from images array
            List<String> mediaUrls = new ArrayList<>();
            if (postJson.has("images") && postJson.get("images").isArray()) {
                for (JsonNode image : postJson.get("images")) {
                    if (image.has("url")) {
                        mediaUrls.add(image.get("url").asText());
                    }
                }
            }
            
            // Create Tweet entity
            Tweet tweet = Tweet.builder()
                    .tweetId(id)
                    .content(text)
                    .platform(Platform.TRUTH_SOCIAL)
                    .username("realDonaldTrump") // Default username for now
                    .createdAt(createdAt)
                    .likesCount(likesCount)
                    .retweetsCount(sharesCount)
                    .build();
            
            // Set media URLs as JSON string if any
            if (!mediaUrls.isEmpty()) {
                try {
                    tweet.setMediaUrls(objectMapper.writeValueAsString(mediaUrls));
                } catch (Exception e) {
                    log.warn("Failed to serialize media URLs: {}", e.getMessage());
                }
            }
            
            return tweet;
            
        } catch (Exception e) {
            log.warn("Error parsing Truth Social post: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Scheduled task to fetch Truth Social tweets every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes = 300,000 milliseconds
    public void scheduledFetchTruthSocialTweets() {
        fetchTruthSocialTweets("realDonaldTrump");
    }
    
    /**
     * Handle WebClient response exceptions
     */
    private void handleWebClientException(WebClientResponseException e, String username) {
        int statusCode = e.getStatusCode().value();
        
        switch (statusCode) {
            case 401:
            case 403:
                log.warn("Truth Social API authentication failed ({}): Invalid API key for username: {}", 
                        statusCode, username);
                break;
            case 429:
                log.warn("Truth Social API rate limit exceeded (429) for username: {}", username);
                // Don't update cache on 429 to allow retry after backoff
                break;
            default:
                log.error("Truth Social API error ({}): {} for username: {}", 
                        statusCode, e.getMessage(), username);
                break;
        }
    }
}