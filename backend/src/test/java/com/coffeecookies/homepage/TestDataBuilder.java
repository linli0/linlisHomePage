package com.coffeecookies.homepage;

import com.coffeecookies.homepage.entity.User;
import com.coffeecookies.homepage.entity.GoldPrice;
import com.coffeecookies.homepage.entity.Article;
import com.coffeecookies.homepage.entity.Category;
import com.coffeecookies.homepage.entity.Tag;
import com.coffeecookies.homepage.entity.ExchangeRate;
import com.coffeecookies.homepage.entity.Role;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 测试数据构建器
 * 提供构建测试实体的便捷方法
 */
public class TestDataBuilder {

    // User builders
    public static User.UserBuilder user() {
        return User.builder()
                .username("testuser")
                .password("testpassword")
                .email("test@example.com")
                .displayName("Test User")
                .avatar("https://example.com/avatar.jpg")
                .role(Role.USER)
                .enabled(true);
    }

    public static User adminUser() {
        return user()
                .username("admin")
                .role(Role.ADMIN)
                .build();
    }

    // GoldPrice builders
    public static GoldPrice.GoldPriceBuilder goldPrice() {
        return GoldPrice.builder()
                .priceUsd(new BigDecimal("2500.00"))
                .changeAmount(new BigDecimal("10.50"))
                .changePercent(new BigDecimal("0.42"))
                .currency("USD")
                .recordedAt(LocalDateTime.now());
    }

    // Article builders
    public static Article.ArticleBuilder article() {
        return Article.builder()
                .title("Test Article")
                .content("# Test Content\nThis is a test article.")
                .summary("This is a test article summary")
                .coverImage("https://example.com/cover.jpg")
                .published(true)
                .viewCount(0L);
    }

    // Category builders
    public static Category.CategoryBuilder category() {
        return Category.builder()
                .name("Test Category")
                .description("This is a test category")
                .icon("fas fa-test")
                .sortOrder(1);
    }

    // Tag builders
    public static Tag.TagBuilder tag() {
        return Tag.builder()
                .name("test-tag")
                .color("#0ea5e9");
    }

    // ExchangeRate builders
    public static ExchangeRate.ExchangeRateBuilder exchangeRate() {
        return ExchangeRate.builder()
                .fromCurrency("USD")
                .toCurrency("CNY")
                .rate(new BigDecimal("7.25"));
    }

    // Lists of test data
    public static List<User> testUsers() {
        return Arrays.asList(
            user().username("user1").build(),
            user().username("user2").build(),
            adminUser()
        );
    }

    public static List<GoldPrice> testGoldPrices() {
        return Arrays.asList(
            goldPrice().priceUsd(new BigDecimal("2500.00")).build(),
            goldPrice().priceUsd(new BigDecimal("2510.50")).build(),
            goldPrice().priceUsd(new BigDecimal("2495.75")).build()
        );
    }

    public static List<Article> testArticles() {
        return Arrays.asList(
            article().title("Article 1").build(),
            article().title("Article 2").build(),
            article().title("Article 3").published(false).build()
        );
    }
}