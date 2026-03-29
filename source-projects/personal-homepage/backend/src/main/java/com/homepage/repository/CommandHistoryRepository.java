package com.homepage.repository;

import com.homepage.entity.CommandHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommandHistoryRepository extends JpaRepository<CommandHistory, Long> {

    Page<CommandHistory> findByCommandTypeOrderByCreatedAtDesc(String commandType, Pageable pageable);

    List<CommandHistory> findTop20ByOrderByCreatedAtDesc();

    List<CommandHistory> findByCreatedAtBefore(LocalDateTime date);
}
