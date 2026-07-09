package com.coffeecookies.homepage.controller;

import com.coffeecookies.homepage.dto.CategoryDTO;
import com.coffeecookies.homepage.dto.Result;
import com.coffeecookies.homepage.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<Result<List<CategoryDTO>>> getAllCategories() {
        return ResponseEntity.ok(Result.success(categoryService.getAllCategories()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<CategoryDTO>> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(Result.success(categoryService.getCategoryById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<CategoryDTO>> createCategory(@RequestBody CategoryDTO dto) {
        return ResponseEntity.ok(Result.success(categoryService.createCategory(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<CategoryDTO>> updateCategory(@PathVariable Long id, @RequestBody CategoryDTO dto) {
        return ResponseEntity.ok(Result.success(categoryService.updateCategory(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(Result.success());
    }
}
