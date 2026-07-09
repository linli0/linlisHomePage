package com.coffeecookies.homepage.dto;

import lombok.Data;

@Data
public class TweetSearchRequest {

    private String keyword;
    private String platform;
    private String username;
    private String startDate;
    private String endDate;
    private Integer limit = 20;
}