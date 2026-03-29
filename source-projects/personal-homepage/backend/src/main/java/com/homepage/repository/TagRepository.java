package com.homepage.repository;

import com.homepage.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    Set<Tag> findByNameIn(Set<String> names);

    @Query("SELECT t.name, COUNT(a) FROM Tag t JOIN t.articles a WHERE a.isPublished = true GROUP BY t.id, t.name")
    List<Object[]> findAllWithArticleCount();

    boolean existsByName(String name);
}
