package org.example.link.domain.category.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.example.link.domain.category.entity.CategoryEntity;

public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {
}
