package com.coffeecookies.homepage.repository;

import com.coffeecookies.homepage.entity.GoldPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GoldPriceRepository extends JpaRepository<GoldPrice, Long> {
    
    Optional<GoldPrice> findTopByOrderByRecordedAtDesc();
    
    @Query("SELECT gp FROM GoldPrice gp WHERE gp.recordedAt >= :startTime ORDER BY gp.recordedAt ASC")
    List<GoldPrice> findByRecordedAtAfterOrderByRecordedAtAsc(LocalDateTime startTime);
    
    List<GoldPrice> findTop90ByOrderByRecordedAtAsc();
}
