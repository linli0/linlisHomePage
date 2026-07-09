package com.coffeecookies.homepage.repository;

import com.coffeecookies.homepage.entity.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findByUsernameAndPlatform(String username, String platform);

    List<SocialAccount> findByEnabledTrue();

    List<SocialAccount> findByPlatform(String platform);
}