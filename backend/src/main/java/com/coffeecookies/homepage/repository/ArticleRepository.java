package com.coffeecookies.homepage.repository;

import com.coffeecookies.homepage.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    
    Page<Article> findByPublishedTrue(Pageable pageable);
    
    Page<Article> findByCategoryIdAndPublishedTrue(Long categoryId, Pageable pageable);
    
    @Query("SELECT a FROM Article a JOIN a.tags t WHERE t.id = :tagId AND a.published = true")
    Page<Article> findByTagIdAndPublishedTrue(@Param("tagId") Long tagId, Pageable pageable);
    
    @Query("SELECT a FROM Article a WHERE a.published = true AND (a.title LIKE %:keyword% OR a.content LIKE %:keyword%)")
    Page<Article> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    List<Article> findTop5ByPublishedTrueOrderByCreatedAtDesc();
    
    List<Article> findTop5ByPublishedTrueOrderByViewCountDesc();
}
