package com.coffeecookies.homepage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TweetDTO {

    private Long id;
    private String tweetId;
    private String content;
    private String platform;
    private String username;
    private String displayName;
    private String createdAt;
    private Integer likesCount;
    private Integer retweetsCount;
    private Integer repliesCount;
    private List<String> hashtags;
    private List<String> mentions;
    private List<String> mediaUrls;
}