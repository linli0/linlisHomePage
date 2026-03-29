package com.homepage.repository;

import com.homepage.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    Page<Article> findByIsPublishedTrue(Pageable pageable);

    Page<Article> findByCategoryIdAndIsPublishedTrue(Long categoryId, Pageable pageable);

    @Query("SELECT a FROM Article a JOIN a.tags t WHERE t.id = :tagId AND a.isPublished = true")
    Page<Article> findByTagIdAndIsPublishedTrue(@Param("tagId") Long tagId, Pageable pageable);

    @Query("SELECT a FROM Article a WHERE a.isPublished = true AND (a.title LIKE %:keyword% OR a.summary LIKE %:keyword% OR a.content LIKE %:keyword%)")
    Page<Article> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    List<Article> findTop5ByIsPublishedTrueOrderByIsTopDescCreatedAtDesc();

    Optional<Article> findByIdAndIsPublishedTrue(Long id);

    long countByIsPublishedTrue();

    @Query("SELECT a FROM Article a WHERE a.isPublished = true ORDER BY a.viewCount DESC")
    List<Article> findTop5ByOrderByViewCountDesc(Pageable pageable);
}
