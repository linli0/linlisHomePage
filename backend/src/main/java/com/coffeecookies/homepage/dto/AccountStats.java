package com.coffeecookies.homepage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountStats {

    private String username;
    private String displayName;
    private String platform;
    private Long tweetCount;
    private Double averageLikes;
}