package com.coffeecookies.homepage.service;

import com.coffeecookies.homepage.entity.Tweet;
import com.coffeecookies.homepage.entity.enums.Platform;
import com.coffeecookies.homepage.repository.TweetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "features.tweets", name = "enabled", havingValue = "true")
public class TweetPollingService {

    private final TweetRepository tweetRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final WebClient.Builder webClientBuilder;

    @Value("${tweets.fallback.polling.enabled:true}")
    private boolean pollingEnabled;

    @Value("${tweets.fallback.polling.interval-seconds:300}")
    private int pollingIntervalSeconds;

    @Value("${twitter.streaming.bearer-token:}")
    private String twitterBearerToken;

    private static final String TWITTER_API_BASE = "https://api.twitter.com/2";
    private static final String TRUTH_SOCIAL_API_BASE = "https://truthsocial.com/api/v1";

    @Scheduled(fixedRateString = "${tweets.fallback.polling.interval-seconds}000")
    @Transactional
    public void pollTweets() {
        if (!pollingEnabled) {
            log.debug("Tweet polling is disabled, skipping");
            return;
        }

        log.info("Starting tweet polling cycle");

        try {
            List<Tweet> allNewTweets = new ArrayList<>();

            // Poll Twitter
            List<Tweet> twitterTweets = pollTwitterTweets();
            allNewTweets.addAll(twitterTweets);

            // Poll Truth Social
            List<Tweet> truthSocialTweets = pollTruthSocialTweets();
            allNewTweets.addAll(truthSocialTweets);

            // Save and broadcast new tweets
            if (!allNewTweets.isEmpty()) {
                saveAndBroadcastNewTweets(allNewTweets);
            } else {
                log.debug("No new tweets found in this polling cycle");
            }

        } catch (Exception e) {
            log.error("Error during tweet polling: {}", e.getMessage(), e);
        }
    }

    private List<Tweet> pollTwitterTweets() {
        List<Tweet> tweets = new ArrayList<>();

        if (twitterBearerToken == null || twitterBearerToken.isEmpty()) {
            log.warn("Twitter bearer token not configured, skipping Twitter polling");
            return tweets;
        }

        try {
            WebClient webClient = webClientBuilder.build();

            // Get user IDs for the accounts we want to track
            List<String> usernames = List.of("realDonaldTrump", "POTUS");
            Map<String, String> userIdMap = getUserIds(webClient, usernames);

            for (Map.Entry<String, String> entry : userIdMap.entrySet()) {
                String username = entry.getKey();
                String userId = entry.getValue();

                // Get the last tweet ID for this user
                String lastTweetId = getLastTweetId(username, Platform.TWITTER);

                // Fetch tweets for this user
                String url = TWITTER_API_BASE + "/users/" + userId + "/tweets";
                String response = webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path(url)
                                .queryParam("max_results", "10")
                                .queryParam("tweet.fields", "created_at,public_metrics,entities")
                                .queryParam("exclude", "retweets,replies")
                                .build())
                        .header("Authorization", "Bearer " + twitterBearerToken)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                if (response != null) {
                    List<Tweet> userTweets = parseTwitterResponse(response, username);
                    tweets.addAll(userTweets);
                }
            }

            log.info("Polled {} tweets from Twitter", tweets.size());

        } catch (Exception e) {
            log.error("Error polling Twitter: {}", e.getMessage(), e);
        }

        return tweets;
    }

    private Map<String, String> getUserIds(WebClient webClient, List<String> usernames) {
        try {
            String usernamesParam = String.join(",", usernames);
            String response = webClient.get()
                    .uri(TWITTER_API_BASE + "/users/by?usernames=" + usernamesParam)
                    .header("Authorization", "Bearer " + twitterBearerToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response != null) {
                JSONObject json = new JSONObject(response);
                JSONArray data = json.optJSONArray("data");

                if (data != null) {
                    Map<String, String> userIdMap = new java.util.HashMap<>();
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject user = data.getJSONObject(i);
                        String username = user.getString("username");
                        String id = user.getString("id");
                        userIdMap.put(username, id);
                    }
                    return userIdMap;
                }
            }
        } catch (Exception e) {
            log.error("Error getting user IDs: {}", e.getMessage(), e);
        }

        return new java.util.HashMap<>();
    }

    private List<Tweet> parseTwitterResponse(String response, String username) {
        List<Tweet> tweets = new ArrayList<>();

        try {
            JSONObject json = new JSONObject(response);
            JSONArray data = json.optJSONArray("data");

            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject tweetJson = data.getJSONObject(i);

                    Tweet tweet = Tweet.builder()
                            .tweetId(tweetJson.getString("id"))
                            .content(tweetJson.optString("text", ""))
                            .platform(Platform.TWITTER)
                            .username(username)
                            .createdAt(parseTwitterDateTime(tweetJson.optString("created_at")))
                            .likesCount(extractMetric(tweetJson, "like_count"))
                            .retweetsCount(extractMetric(tweetJson, "retweet_count"))
                            .repliesCount(extractMetric(tweetJson, "reply_count"))
                            .hashtags(extractHashtags(tweetJson))
                            .mentions(extractMentions(tweetJson))
                            .mediaUrls(extractMediaUrls(tweetJson))
                            .build();

                    tweets.add(tweet);
                }
            }
        } catch (Exception e) {
            log.error("Error parsing Twitter response: {}", e.getMessage(), e);
        }

        return tweets;
    }

    private List<Tweet> pollTruthSocialTweets() {
        List<Tweet> tweets = new ArrayList<>();

        try {
            WebClient webClient = webClientBuilder.build();

            // Get posts from Truth Social
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(TRUTH_SOCIAL_API_BASE + "/accounts/verify_credentials")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Note: Truth Social API structure may vary
            // This is a placeholder implementation
            log.debug("Truth Social polling response: {}", response);

        } catch (Exception e) {
            log.error("Error polling Truth Social: {}", e.getMessage(), e);
        }

        return tweets;
    }

    @Transactional
    public void saveAndBroadcastNewTweets(List<Tweet> tweets) {
        List<Tweet> newTweets = new ArrayList<>();

        for (Tweet tweet : tweets) {
            // Check if tweet already exists
            boolean exists = tweetRepository.findByTweetIdAndPlatform(
                    tweet.getTweetId(),
                    tweet.getPlatform().name()
            ).isPresent();

            if (!exists) {
                newTweets.add(tweet);
            }
        }

        if (!newTweets.isEmpty()) {
            // Save to database
            List<Tweet> savedTweets = tweetRepository.saveAll(newTweets);

            // Broadcast via WebSocket
            for (Tweet savedTweet : savedTweets) {
                messagingTemplate.convertAndSend("/topic/tweets", savedTweet);
            }

            log.info("Saved and broadcast {} new tweets", savedTweets.size());
        } else {
            log.debug("No new tweets to save (all duplicates)");
        }
    }

    private String getLastTweetId(String username, Platform platform) {
        return tweetRepository.findByUsernameAndPlatformOrderByCreatedAtDesc(username, platform.name())
                .stream()
                .findFirst()
                .map(Tweet::getTweetId)
                .orElse(null);
    }

    private LocalDateTime parseTwitterDateTime(String dateTimeStr) {
        try {
            // Twitter API returns format: "2023-01-01T00:00:00.000Z"
            return LocalDateTime.parse(dateTimeStr.replace("Z", ""));
        } catch (Exception e) {
            log.error("Error parsing Twitter datetime: {}", dateTimeStr, e);
            return LocalDateTime.now();
        }
    }

    private Integer extractMetric(JSONObject tweetJson, String metricName) {
        try {
            JSONObject publicMetrics = tweetJson.optJSONObject("public_metrics");
            if (publicMetrics != null) {
                return publicMetrics.optInt(metricName, 0);
            }
        } catch (Exception e) {
            log.debug("Error extracting metric {}: {}", metricName, e.getMessage());
        }
        return 0;
    }

    private String extractHashtags(JSONObject tweetJson) {
        try {
            JSONObject entities = tweetJson.optJSONObject("entities");
            if (entities != null) {
                JSONArray hashtags = entities.optJSONArray("hashtags");
                if (hashtags != null) {
                    List<String> hashtagList = new ArrayList<>();
                    for (int i = 0; i < hashtags.length(); i++) {
                        JSONObject tag = hashtags.getJSONObject(i);
                        hashtagList.add(tag.optString("tag", ""));
                    }
                    return new JSONObject(hashtagList).toString();
                }
            }
        } catch (Exception e) {
            log.debug("Error extracting hashtags: {}", e.getMessage());
        }
        return "[]";
    }

    private String extractMentions(JSONObject tweetJson) {
        try {
            JSONObject entities = tweetJson.optJSONObject("entities");
            if (entities != null) {
                JSONArray mentions = entities.optJSONArray("mentions");
                if (mentions != null) {
                    List<String> mentionList = new ArrayList<>();
                    for (int i = 0; i < mentions.length(); i++) {
                        JSONObject mention = mentions.getJSONObject(i);
                        mentionList.add(mention.optString("username", ""));
                    }
                    return new JSONObject(mentionList).toString();
                }
            }
        } catch (Exception e) {
            log.debug("Error extracting mentions: {}", e.getMessage());
        }
        return "[]";
    }

    private String extractMediaUrls(JSONObject tweetJson) {
        try {
            JSONObject entities = tweetJson.optJSONObject("entities");
            if (entities != null) {
                JSONArray media = entities.optJSONArray("media");
                if (media != null) {
                    List<String> mediaList = new ArrayList<>();
                    for (int i = 0; i < media.length(); i++) {
                        JSONObject mediaItem = media.getJSONObject(i);
                        mediaList.add(mediaItem.optString("url", ""));
                    }
                    return new JSONObject(mediaList).toString();
                }
            }
        } catch (Exception e) {
            log.debug("Error extracting media URLs: {}", e.getMessage());
        }
        return "[]";
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (pollingEnabled) {
            log.info("Tweet polling service initialized. Polling interval: {} seconds", pollingIntervalSeconds);
        } else {
            log.info("Tweet polling service is disabled");
        }
    }
}