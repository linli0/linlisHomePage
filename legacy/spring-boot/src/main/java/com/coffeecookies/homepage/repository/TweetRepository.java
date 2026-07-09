package com.coffeecookies.homepage.repository;

import com.coffeecookies.homepage.entity.Tweet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TweetRepository extends JpaRepository<Tweet, Long> {

    Optional<Tweet> findByTweetIdAndPlatform(String tweetId, String platform);

    List<Tweet> findByUsernameAndPlatformOrderByCreatedAtDesc(String username, String platform);

    List<Tweet> findByPlatformOrderByCreatedAtDesc(String platform);

    @Query("SELECT t FROM Tweet t WHERE t.createdAt >= :startTime ORDER BY t.createdAt DESC")
    List<Tweet> findRecentAfter(LocalDateTime startTime);

    @Query("SELECT t FROM Tweet t WHERE t.content LIKE %:keyword% ORDER BY t.createdAt DESC")
    List<Tweet> searchByKeyword(String keyword);

    @Query("SELECT COUNT(t) FROM Tweet t")
    Long countByPlatform(String platform);
}