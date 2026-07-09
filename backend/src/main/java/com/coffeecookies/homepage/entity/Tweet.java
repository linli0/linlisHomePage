package com.coffeecookies.homepage.entity;

import com.coffeecookies.homepage.entity.enums.Platform;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tweets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Tweet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 255)
    private String tweetId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Platform platform;

    @Column(length = 100)
    private String username;

    private LocalDateTime createdAt;

    @CreatedDate
    private LocalDateTime recordedAt;

    private Integer likesCount;

    private Integer retweetsCount;

    private Integer repliesCount;

    @Column(columnDefinition = "JSON")
    private String hashtags;

    @Column(columnDefinition = "JSON")
    private String mentions;

    @Column(columnDefinition = "JSON")
    private String mediaUrls;
}