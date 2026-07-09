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
public class TweetStatsDTO {

    private Long totalTweets;
    private Long twitterTweets;
    private Long truthSocialTweets;
    private Long tweetsToday;
    private Long tweetsThisWeek;
    private List<AccountStats> topAccounts;
    private Double averageEngagement;
}