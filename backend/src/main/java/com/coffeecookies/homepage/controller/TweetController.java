package com.coffeecookies.homepage.controller;

import com.coffeecookies.homepage.dto.AccountStats;
import com.coffeecookies.homepage.dto.Result;
import com.coffeecookies.homepage.dto.TweetDTO;
import com.coffeecookies.homepage.dto.TweetSearchRequest;
import com.coffeecookies.homepage.dto.TweetStatsDTO;
import com.coffeecookies.homepage.entity.Tweet;
import com.coffeecookies.homepage.repository.TweetRepository;
import com.coffeecookies.homepage.service.TweetWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tweets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@ConditionalOnProperty(prefix = "features.tweets", name = "enabled", havingValue = "true")
public class TweetController {

    private final TweetRepository tweetRepository;
    private final TweetWebSocketService tweetWebSocketService;

    @GetMapping("/latest")
    public ResponseEntity<Result<List<TweetDTO>>> getLatestTweets(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) String username) {

        // Validate limit
        if (limit < 1) limit = 1;
        if (limit > 100) limit = 100;

        List<Tweet> tweets;

        // Apply filters
        if (platform != null && username != null) {
            tweets = tweetRepository.findByUsernameAndPlatformOrderByCreatedAtDesc(username, platform);
        } else if (platform != null) {
            tweets = tweetRepository.findByPlatformOrderByCreatedAtDesc(platform);
        } else {
            tweets = tweetRepository.findAll();
        }

        // Sort by createdAt descending and limit
        List<TweetDTO> tweetDTOs = tweets.stream()
                .sorted(Comparator.comparing(Tweet::getCreatedAt).reversed())
                .limit(limit)
                .map(tweetWebSocketService::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Result.success(tweetDTOs));
    }

    @PostMapping("/search")
    public ResponseEntity<Result<List<TweetDTO>>> searchTweets(@RequestBody TweetSearchRequest request) {
        // Validate limit
        if (request.getLimit() == null || request.getLimit() < 1) {
            request.setLimit(20);
        }
        if (request.getLimit() > 100) {
            request.setLimit(100);
        }

        List<Tweet> tweets;

        // Start with keyword search if provided
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            tweets = tweetRepository.searchByKeyword(request.getKeyword());
        } else {
            tweets = tweetRepository.findAll();
        }

        // Apply additional filters
        if (request.getPlatform() != null && !request.getPlatform().isEmpty()) {
            tweets = tweets.stream()
                    .filter(t -> t.getPlatform() != null && t.getPlatform().name().equalsIgnoreCase(request.getPlatform()))
                    .collect(Collectors.toList());
        }

        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            tweets = tweets.stream()
                    .filter(t -> t.getUsername() != null && t.getUsername().equalsIgnoreCase(request.getUsername()))
                    .collect(Collectors.toList());
        }

        // Apply date range filters
        if (request.getStartDate() != null && !request.getStartDate().isEmpty()) {
            LocalDateTime startDate = LocalDateTime.parse(request.getStartDate());
            tweets = tweets.stream()
                    .filter(t -> t.getCreatedAt() != null && !t.getCreatedAt().isBefore(startDate))
                    .collect(Collectors.toList());
        }

        if (request.getEndDate() != null && !request.getEndDate().isEmpty()) {
            LocalDateTime endDate = LocalDateTime.parse(request.getEndDate());
            tweets = tweets.stream()
                    .filter(t -> t.getCreatedAt() != null && !t.getCreatedAt().isAfter(endDate))
                    .collect(Collectors.toList());
        }

        // Sort by createdAt descending and limit
        List<TweetDTO> tweetDTOs = tweets.stream()
                .sorted(Comparator.comparing(Tweet::getCreatedAt).reversed())
                .limit(request.getLimit())
                .map(tweetWebSocketService::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Result.success(tweetDTOs));
    }

    @GetMapping("/stats")
    public ResponseEntity<Result<TweetStatsDTO>> getTweetStats() {
        List<Tweet> allTweets = tweetRepository.findAll();

        // Total tweets
        long totalTweets = allTweets.size();

        // Platform breakdown
        long twitterTweets = allTweets.stream()
                .filter(t -> t.getPlatform() != null && t.getPlatform().name().equalsIgnoreCase("TWITTER"))
                .count();

        long truthSocialTweets = allTweets.stream()
                .filter(t -> t.getPlatform() != null && t.getPlatform().name().equalsIgnoreCase("TRUTH_SOCIAL"))
                .count();

        // Recent activity
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime weekStart = now.minusDays(7).toLocalDate().atStartOfDay();

        long tweetsToday = allTweets.stream()
                .filter(t -> t.getCreatedAt() != null && !t.getCreatedAt().isBefore(todayStart))
                .count();

        long tweetsThisWeek = allTweets.stream()
                .filter(t -> t.getCreatedAt() != null && !t.getCreatedAt().isBefore(weekStart))
                .count();

        // Top accounts by tweet count
        Map<String, List<Tweet>> tweetsByUsername = allTweets.stream()
                .collect(Collectors.groupingBy(Tweet::getUsername));

        List<AccountStats> topAccounts = tweetsByUsername.entrySet().stream()
                .map(entry -> {
                    String username = entry.getKey();
                    List<Tweet> userTweets = entry.getValue();
                    double avgLikes = userTweets.stream()
                            .mapToInt(Tweet::getLikesCount)
                            .average()
                            .orElse(0.0);
                    String platform = userTweets.get(0).getPlatform() != null ?
                            userTweets.get(0).getPlatform().name() : null;
                    return AccountStats.builder()
                            .username(username)
                            .displayName(username) // Could be enhanced with actual display name
                            .platform(platform)
                            .tweetCount((long) userTweets.size())
                            .averageLikes(avgLikes)
                            .build();
                })
                .sorted(Comparator.comparing(AccountStats::getTweetCount).reversed())
                .limit(10)
                .collect(Collectors.toList());

        // Average engagement (likes + retweets + replies)
        double averageEngagement = allTweets.stream()
                .mapToLong(t -> (long) t.getLikesCount() + t.getRetweetsCount() + t.getRepliesCount())
                .average()
                .orElse(0.0);

        TweetStatsDTO stats = TweetStatsDTO.builder()
                .totalTweets(totalTweets)
                .twitterTweets(twitterTweets)
                .truthSocialTweets(truthSocialTweets)
                .tweetsToday(tweetsToday)
                .tweetsThisWeek(tweetsThisWeek)
                .topAccounts(topAccounts)
                .averageEngagement(averageEngagement)
                .build();

        return ResponseEntity.ok(Result.success(stats));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<TweetDTO>> getTweetById(@PathVariable Long id) {
        return tweetRepository.findById(id)
                .map(tweet -> ResponseEntity.ok(Result.success(tweetWebSocketService.convertToDTO(tweet))))
                .orElse(ResponseEntity.status(404).body(Result.error(404, "Tweet not found")));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<Void>> deleteTweet(@PathVariable Long id) {
        if (!tweetRepository.existsById(id)) {
            return ResponseEntity.status(404).body(Result.error(404, "Tweet not found"));
        }

        tweetRepository.deleteById(id);
        return ResponseEntity.ok(Result.success());
    }
}