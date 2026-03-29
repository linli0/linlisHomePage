package com.homepage.repository;

import com.homepage.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByArticleIdAndIsApprovedTrueOrderByCreatedAtDesc(Long articleId);

    Page<Comment> findByIsApprovedFalse(Pageable pageable);

    long countByArticleIdAndIsApprovedTrue(Long articleId);
}
