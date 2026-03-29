package com.homepage.repository;

import com.homepage.entity.FileInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileInfoRepository extends JpaRepository<FileInfo, Long> {

    Optional<FileInfo> findByShareToken(String shareToken);

    List<FileInfo> findByIsPublicTrueAndExpiresAtAfterOrExpiresAtIsNull(LocalDateTime now);

    List<FileInfo> findByExpiresAtBefore(LocalDateTime now);

    boolean existsByShareToken(String shareToken);
}
