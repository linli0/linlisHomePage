package com.coffeecookies.homepage.service;

import com.coffeecookies.homepage.dto.TweetDTO;
import com.coffeecookies.homepage.dto.TweetStatsDTO;
import com.coffeecookies.homepage.entity.Tweet;
import com.coffeecookies.homepage.repository.TweetRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "features.tweets", name = "enabled", havingValue = "true")
public class TweetWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final TweetRepository tweetRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    /**
     * Broadcast a new tweet to all connected clients
     */
    public void broadcastNewTweet(Tweet tweet, TweetDTO tweetDTO) {
        TweetDTO dto = tweetDTO != null ? tweetDTO : convertToDTO(tweet);

        log.info("Broadcasting new tweet: platform={}, username={}, tweetId={}",
                dto.getPlatform(), dto.getUsername(), dto.getTweetId());

        messagingTemplate.convertAndSend("/topic/tweets/new", dto);
    }

    /**
     * Broadcast tweet update (e.g., metrics changes)
     */
    public void broadcastTweetUpdate(Long tweetId, TweetDTO tweetDTO) {
        log.info("Broadcasting tweet update: tweetId={}, likes={}, retweets={}",
                tweetId, tweetDTO.getLikesCount(), tweetDTO.getRetweetsCount());

        messagingTemplate.convertAndSend("/topic/tweets/update", tweetDTO);
    }

    /**
     * Broadcast connection status for streaming services
     */
    public void broadcastConnectionStatus(String status) {
        log.info("Broadcasting connection status: {}", status);

        ConnectionStatusMessage message = new ConnectionStatusMessage(
                status,
                System.currentTimeMillis()
        );

        messagingTemplate.convertAndSend("/topic/tweets/status", message);
    }

    /**
     * Broadcast connection status for a specific platform
     */
    public void broadcastConnectionStatus(String platform, String status) {
        log.info("Broadcasting connection status: platform={}, status={}", platform, status);

        ConnectionStatusMessage message = new ConnectionStatusMessage(
                platform,
                status,
                System.currentTimeMillis()
        );

        messagingTemplate.convertAndSend("/topic/tweets/status", message);
    }

    /**
     * Broadcast statistics update
     */
    public void broadcastStatsUpdate(TweetStatsDTO stats) {
        log.info("Broadcasting stats update: totalTweets={}, twitter={}, truthSocial={}",
                stats.getTotalTweets(), stats.getTwitterTweets(), stats.getTruthSocialTweets());

        messagingTemplate.convertAndSend("/topic/tweets/stats", stats);
    }

    /**
     * Convert Tweet entity to TweetDTO
     */
    public TweetDTO convertToDTO(Tweet tweet) {
        TweetDTO.TweetDTOBuilder builder = TweetDTO.builder()
                .id(tweet.getId())
                .tweetId(tweet.getTweetId())
                .content(tweet.getContent())
                .platform(tweet.getPlatform() != null ? tweet.getPlatform().name() : null)
                .username(tweet.getUsername())
                .createdAt(tweet.getCreatedAt() != null ? tweet.getCreatedAt().format(ISO_FORMATTER) : null)
                .likesCount(tweet.getLikesCount())
                .retweetsCount(tweet.getRetweetsCount())
                .repliesCount(tweet.getRepliesCount());

        // Parse JSON columns
        try {
            if (tweet.getHashtags() != null && !tweet.getHashtags().isEmpty()) {
                List<String> hashtags = objectMapper.readValue(tweet.getHashtags(), new TypeReference<List<String>>() {});
                builder.hashtags(hashtags);
            }
        } catch (Exception e) {
            log.warn("Failed to parse hashtags for tweet {}: {}", tweet.getTweetId(), e.getMessage());
        }

        try {
            if (tweet.getMentions() != null && !tweet.getMentions().isEmpty()) {
                List<String> mentions = objectMapper.readValue(tweet.getMentions(), new TypeReference<List<String>>() {});
                builder.mentions(mentions);
            }
        } catch (Exception e) {
            log.warn("Failed to parse mentions for tweet {}: {}", tweet.getTweetId(), e.getMessage());
        }

        try {
            if (tweet.getMediaUrls() != null && !tweet.getMediaUrls().isEmpty()) {
                List<String> mediaUrls = objectMapper.readValue(tweet.getMediaUrls(), new TypeReference<List<String>>() {});
                builder.mediaUrls(mediaUrls);
            }
        } catch (Exception e) {
            log.warn("Failed to parse mediaUrls for tweet {}: {}", tweet.getTweetId(), e.getMessage());
        }

        return builder.build();
    }

    /**
     * Internal class for connection status messages
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ConnectionStatusMessage {
        private String platform;
        private String status;
        private Long timestamp;

        public ConnectionStatusMessage(String status, Long timestamp) {
            this.status = status;
            this.timestamp = timestamp;
        }
    }
}