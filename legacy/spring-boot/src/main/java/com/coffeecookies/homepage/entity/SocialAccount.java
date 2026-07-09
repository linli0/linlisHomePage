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
@Table(name = "social_accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 100)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Platform platform;

    @Column(length = 255)
    private String displayName;

    private Boolean enabled;

    @Column(length = 255)
    private String lastTweetId;

    @CreatedDate
    private LocalDateTime monitoredSince;
}