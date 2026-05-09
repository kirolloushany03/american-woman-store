package com.americanwomen.store.repository;

import com.americanwomen.store.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNewArrivalTrueOrderByCreatedAtDesc();
    List<Product> findByBestSellerTrueOrderByCreatedAtDesc();
    List<Product> findByOnSaleTrueOrderByCreatedAtDesc();
    List<Product> findByCategoryOrderByCreatedAtDesc(String category);
    
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.category) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY p.createdAt DESC")
    List<Product> searchProducts(@Param("query") String query);
}

