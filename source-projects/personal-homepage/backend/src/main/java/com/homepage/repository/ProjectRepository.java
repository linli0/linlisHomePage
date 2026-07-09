package com.homepage.repository;

import com.homepage.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findAllByOrderBySortOrderAsc();

    List<Project> findByIsFeaturedTrueOrderBySortOrderAsc();

    List<Project> findByCategoryOrderBySortOrderAsc(String category);
}
