package com.coffeecookies.homepage.service;

import com.coffeecookies.homepage.dto.ArticleDTO;
import com.coffeecookies.homepage.dto.CategoryDTO;
import com.coffeecookies.homepage.dto.TagDTO;
import com.coffeecookies.homepage.dto.UserDTO;
import com.coffeecookies.homepage.entity.Article;
import com.coffeecookies.homepage.entity.Category;
import com.coffeecookies.homepage.entity.Tag;
import com.coffeecookies.homepage.repository.ArticleRepository;
import com.coffeecookies.homepage.repository.CategoryRepository;
import com.coffeecookies.homepage.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public Page<ArticleDTO> getPublishedArticles(Pageable pageable) {
        Page<Article> articles = articleRepository.findByPublishedTrue(pageable);
        return new PageImpl<>(
                articles.getContent().stream().map(this::convertToDTO).collect(Collectors.toList()),
                pageable,
                articles.getTotalElements()
        );
    }

    @Transactional(readOnly = true)
    public ArticleDTO getArticleById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        return convertToDTO(article);
    }

    @Transactional
    public ArticleDTO createArticle(ArticleDTO dto) {
        Article article = new Article();
        updateArticleFromDTO(article, dto);
        return convertToDTO(articleRepository.save(article));
    }

    @Transactional
    public ArticleDTO updateArticle(Long id, ArticleDTO dto) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        updateArticleFromDTO(article, dto);
        return convertToDTO(articleRepository.save(article));
    }

    @Transactional
    public void deleteArticle(Long id) {
        articleRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ArticleDTO> getRecentArticles(int limit) {
        return articleRepository.findTop5ByPublishedTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ArticleDTO> getPopularArticles(int limit) {
        return articleRepository.findTop5ByPublishedTrueOrderByViewCountDesc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void incrementViewCount(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        article.setViewCount(article.getViewCount() + 1);
        articleRepository.save(article);
    }

    private ArticleDTO convertToDTO(Article article) {
        return ArticleDTO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .summary(article.getSummary())
                .coverImage(article.getCoverImage())
                .published(article.isPublished())
                .viewCount(article.getViewCount())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .category(article.getCategory() != null ? CategoryDTO.builder()
                        .id(article.getCategory().getId())
                        .name(article.getCategory().getName())
                        .icon(article.getCategory().getIcon())
                        .build() : null)
                .tags(article.getTags().stream().map(tag -> TagDTO.builder()
                        .id(tag.getId())
                        .name(tag.getName())
                        .color(tag.getColor())
                        .build()).collect(Collectors.toSet()))
                .author(article.getAuthor() != null ? UserDTO.builder()
                        .id(article.getAuthor().getId())
                        .username(article.getAuthor().getUsername())
                        .displayName(article.getAuthor().getDisplayName())
                        .avatar(article.getAuthor().getAvatar())
                        .build() : null)
                .build();
    }

    private void updateArticleFromDTO(Article article, ArticleDTO dto) {
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setSummary(dto.getSummary());
        article.setCoverImage(dto.getCoverImage());
        article.setPublished(dto.isPublished());
        
        if (dto.getCategory() != null && dto.getCategory().getId() != null) {
            Category category = categoryRepository.findById(dto.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            article.setCategory(category);
        }
        
        if (dto.getTags() != null) {
            Set<Tag> tags = new HashSet<>();
            for (TagDTO tagDTO : dto.getTags()) {
                Tag tag = tagRepository.findById(tagDTO.getId())
                        .orElseThrow(() -> new RuntimeException("Tag not found"));
                tags.add(tag);
            }
            article.setTags(tags);
        }
    }
}
