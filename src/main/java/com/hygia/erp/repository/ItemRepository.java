package com.hygia.erp.repository;

import com.hygia.erp.domain.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("""
        SELECT i FROM Item i
        WHERE (:active IS NULL OR i.isActive = :active)
          AND (
                :keyword IS NULL
             OR LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(i.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(i.barcode) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(i.qbItemId) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
        """)
    Page<Item> search(@Param("keyword") String keyword,
                      @Param("active") Boolean active,
                      Pageable pageable);
                      
    Optional<Item> findByBarcode(String barcode);
    Optional<Item> findBySku(String sku);
    Optional<Item> findByQbItemId(String qbItemId);
    Optional<Item> findByNameIgnoreCase(String name);
}
