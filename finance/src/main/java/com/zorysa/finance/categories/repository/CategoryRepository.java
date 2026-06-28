package com.zorysa.finance.categories.repository;

import com.zorysa.finance.categories.entity.Category;
import com.zorysa.finance.categories.entity.CategoryType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    @Query(
            value = """
                    select category
                    from Category category
                    where category.userId = :userId
                       or category.defaultCategory = true
                    order by category.defaultCategory desc, category.name asc
                    """,
            countQuery = """
                    select count(category)
                    from Category category
                    where category.userId = :userId
                       or category.defaultCategory = true
                    """
    )
    Page<Category> findAllByUserIdOrDefault(@Param("userId") UUID userId, Pageable pageable);

    @Query(
            value = """
                    select category
                    from Category category
                    where (category.userId = :userId or category.defaultCategory = true)
                      and category.type = :type
                    order by category.defaultCategory desc, category.name asc
                    """,
            countQuery = """
                    select count(category)
                    from Category category
                    where (category.userId = :userId or category.defaultCategory = true)
                      and category.type = :type
                    """
    )
    Page<Category> findAllByUserIdOrDefaultAndType(
            @Param("userId") UUID userId,
            @Param("type") CategoryType type,
            Pageable pageable
    );

    @Query("""
            select category
            from Category category
            where category.id = :id
              and (category.userId = :userId or category.defaultCategory = true)
            """)
    Optional<Category> findByIdAndUserIdOrDefault(@Param("id") UUID id, @Param("userId") UUID userId);

    boolean existsByUserIdAndNameAndType(UUID userId, String name, CategoryType type);

    long countByUserIdAndDefaultCategoryFalse(UUID userId);

    @Query("""
            select count(category)
            from Category category
            where (category.userId = :userId or category.defaultCategory = true)
              and category.type = :type
            """)
    long countApplicableByUserIdAndType(@Param("userId") UUID userId, @Param("type") CategoryType type);
}

