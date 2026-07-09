package com.homepage.service;

import com.homepage.dto.*;
import com.homepage.entity.Article;
import com.homepage.entity.Category;
import com.homepage.entity.Tag;
import com.homepage.repository.ArticleRepository;
import com.homepage.repository.CategoryRepository;
import com.homepage.repository.CommentRepository;
import com.homepage.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final CommentRepository commentRepository;

    public PageResult<ArticleDTO> getArticles(int page, int size, Long categoryId, Long tagId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("isTop").descending().and(Sort.by("createdAt").descending()));
        Page<Article> articlePage;

        if (categoryId != null) {
            articlePage = articleRepository.findByCategoryIdAndIsPublishedTrue(categoryId, pageable);
        } else if (tagId != null) {
            articlePage = articleRepository.findByTagIdAndIsPublishedTrue(tagId, pageable);
        } else {
            articlePage = articleRepository.findByIsPublishedTrue(pageable);
        }

        return PageResult.of(articlePage.map(this::convertToDTO));
    }

    public ArticleDTO getArticleById(Long id) {
        Article article = articleRepository.findByIdAndIsPublishedTrue(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        article.setViewCount(article.getViewCount() + 1);
        articleRepository.save(article);
        return convertToDTO(article);
    }

    public PageResult<ArticleDTO> searchArticles(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Article> articlePage = articleRepository.searchByKeyword(keyword, pageable);
        return PageResult.of(articlePage.map(this::convertToDTO));
    }

    public List<ArticleDTO> getLatestArticles(int limit) {
        return articleRepository.findTop5ByIsPublishedTrueOrderByIsTopDescCreatedAtDesc()
                .stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ArticleDTO> getPopularArticles(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return articleRepository.findTop5ByOrderByViewCountDesc(pageable)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ArticleDTO createArticle(ArticleDTO dto) {
        Article article = new Article();
        updateArticleFromDTO(article, dto);
        Article saved = articleRepository.save(article);
        return convertToDTO(saved);
    }

    @Transactional
    public ArticleDTO updateArticle(Long id, ArticleDTO dto) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        updateArticleFromDTO(article, dto);
        Article saved = articleRepository.save(article);
        return convertToDTO(saved);
    }

    @Transactional
    public void deleteArticle(Long id) {
        articleRepository.deleteById(id);
    }

    private void updateArticleFromDTO(Article article, ArticleDTO dto) {
        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setContent(dto.getContent());
        article.setCoverImage(dto.getCoverImage());
        article.setIsPublished(dto.getIsPublished());
        article.setIsTop(dto.getIsTop());

        if (dto.getCategory() != null && dto.getCategory().getId() != null) {
            Category category = categoryRepository.findById(dto.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            article.setCategory(category);
        }

        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            Set<Long> tagIds = dto.getTags().stream()
                    .map(TagDTO::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(tagIds));
            article.setTags(tags);
        }
    }

    private ArticleDTO convertToDTO(Article article) {
        return ArticleDTO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .summary(article.getSummary())
                .content(article.getContent())
                .coverImage(article.getCoverImage())
                .viewCount(article.getViewCount())
                .isPublished(article.getIsPublished())
                .isTop(article.getIsTop())
                .category(article.getCategory() != null ? CategoryDTO.builder()
                        .id(article.getCategory().getId())
                        .name(article.getCategory().getName())
                        .build() : null)
                .tags(article.getTags().stream()
                        .map(tag -> TagDTO.builder()
                                .id(tag.getId())
                                .name(tag.getName())
                                .color(tag.getColor())
                                .build())
                        .collect(Collectors.toSet()))
                .commentCount(commentRepository.countByArticleIdAndIsApprovedTrue(article.getId()))
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build();
    }
}
