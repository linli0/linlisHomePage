# Tweet and SocialAccount Database Schema

## 📋 Table of Contents

- [Tweet Entity](#tweet-entity)
- [SocialAccount Entity](#socialaccount-entity)
- [Indexes](#indexes)
- [Relationships](#relationships)
- [Query Patterns](#query-patterns)

## Tweet Entity

**Table name**: `tweets`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | Primary Key, Auto-increment | Internal database ID |
| tweetId | VARCHAR(255) | Unique | Twitter/Truth Social tweet ID |
| content | TEXT | Not Null | Tweet content |
| platform | VARCHAR(50) | Not Null, ENUM('twitter', 'truth-social') | Social media platform |
| username | VARCHAR(100) | Not Null | Account username |
| createdAt | DATETIME | Not Null | Tweet timestamp from platform |
| recordedAt | DATETIME | Not Null, @CreatedDate | Database insert timestamp |
| likesCount | INT | Default 0 | Number of likes |
| retweetsCount | INT | Default 0 | Number of retweets/reposts |
| repliesCount | INT | Default 0 | Number of replies |
| hashtags | TEXT | JSON array format | Hashtags in the tweet |
| mentions | TEXT | JSON array format | User mentions in the tweet |
| mediaUrls | TEXT | JSON array format | Media URLs attached to tweet |

### JPA Entity Annotations
```java
package com.coffeecookies.homepage.entity;

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
    
    @Column(unique = true, nullable = false)
    private String tweetId;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(nullable = false)
    private String platform;
    
    @Column(nullable = false)
    private String username;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @CreatedDate
    private LocalDateTime recordedAt;
    
    private Integer likesCount = 0;
    private Integer retweetsCount = 0;
    private Integer repliesCount = 0;
    
    @Column(columnDefinition = "TEXT")
    private String hashtags;
    
    @Column(columnDefinition = "TEXT")
    private String mentions;
    
    @Column(columnDefinition = "TEXT")
    private String mediaUrls;
}
```

## SocialAccount Entity

**Table name**: `social_accounts`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | Primary Key | Internal database ID |
| username | VARCHAR(100) | Unique | Account username |
| platform | VARCHAR(50) | Not Null, ENUM('twitter', 'truth-social') | Social media platform |
| displayName | VARCHAR(255) | | Display name of the account |
| enabled | BOOLEAN | Default true | Whether account is actively monitored |
| lastTweetId | VARCHAR(255) | | Last processed tweet ID |
| monitoredSince | DATETIME | Not Null, @CreatedDate | When monitoring started |

### JPA Entity Annotations
```java
package com.coffeecookies.homepage.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "social_accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SocialAccount {
    
    @Id
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String platform;
    
    private String displayName;
    
    private Boolean enabled = true;
    
    private String lastTweetId;
    
    @CreatedDate
    private LocalDateTime monitoredSince;
}
```

## Indexes

### Tweets Table Indexes
- **Unique Index**: `(tweetId, platform)` - Ensures no duplicate tweets from same platform
- **Composite Index**: `(username, platform, createdAt)` - Optimizes queries for user's tweets by platform and time
- **Time-based Index**: `(createdAt)` - Supports efficient date range queries

### Social Accounts Table Indexes
- **Unique Index**: `(username, platform)` - Ensures unique account per platform

### SQL Index Definitions
```sql
-- Tweets indexes
CREATE UNIQUE INDEX idx_tweets_tweetid_platform ON tweets(tweetId, platform);
CREATE INDEX idx_tweets_username_platform_created ON tweets(username, platform, createdAt);
CREATE INDEX idx_tweets_created_at ON tweets(createdAt);

-- Social accounts indexes  
CREATE UNIQUE INDEX idx_social_accounts_username_platform ON social_accounts(username, platform);
```

## Relationships

- **Logical Relationship**: `Tweet.username` → `SocialAccount.username`
- **No Foreign Keys**: Denormalized design for performance optimization
- **Data Consistency**: Maintained through application logic rather than database constraints

### Rationale for Denormalization
- **Performance**: Avoids JOIN operations during high-frequency tweet ingestion
- **Scalability**: Independent scaling of tweet storage and account management
- **Flexibility**: Allows handling of accounts that may not exist in SocialAccount table (e.g., mentioned users)

## Query Patterns

### Common Query Scenarios

#### 1. Find Latest Tweets by Username and Platform
```sql
SELECT * FROM tweets 
WHERE username = ? AND platform = ? 
ORDER BY createdAt DESC 
LIMIT ?
```

#### 2. Search Tweets by Keyword
```sql
SELECT * FROM tweets 
WHERE content LIKE '%keyword%' 
ORDER BY createdAt DESC
```

#### 3. Filter by Date Range
```sql
SELECT * FROM tweets 
WHERE createdAt BETWEEN ? AND ? 
ORDER BY createdAt DESC
```

#### 4. Get Tweet Statistics
```sql
SELECT 
    COUNT(*) as totalTweets,
    AVG(likesCount) as avgLikes,
    AVG(retweetsCount) as avgRetweets,
    AVG(repliesCount) as avgReplies
FROM tweets 
WHERE username = ? AND platform = ?
```

### Repository Method Signatures (Spring Data JPA)
```java
// TweetRepository.java
List<Tweet> findByUsernameAndPlatformOrderByCreatedAtDesc(String username, String platform, Pageable pageable);
List<Tweet> findByContentContainingIgnoreCaseOrderByCreatedAtDesc(String keyword, Pageable pageable);
List<Tweet> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end, Pageable pageable);

// SocialAccountRepository.java  
SocialAccount findByUsernameAndPlatform(String username, String platform);
List<SocialAccount> findByEnabledTrue();
```

## Database Compatibility

### H2 (Development)
- All data types supported
- JSON columns stored as TEXT
- Full index support

### MySQL (Production)
- Compatible with MySQL 8+
- JSON columns can be optimized using native JSON type in future versions
- All indexes fully supported

## Migration Considerations

- **Initial Schema**: Create both tables with specified columns and indexes
- **Future Extensions**: Additional metadata columns can be added without breaking existing functionality
- **Backward Compatibility**: New optional columns should have sensible defaults