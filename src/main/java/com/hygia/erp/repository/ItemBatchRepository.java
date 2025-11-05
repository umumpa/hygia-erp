package com.hygia.erp.repository;

import com.hygia.erp.domain.ItemBatch;
import com.hygia.erp.domain.Item;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.*;

public interface ItemBatchRepository extends JpaRepository<ItemBatch, Long> {

    @Query("""
      SELECT b FROM ItemBatch b
      JOIN FETCH b.item i
      WHERE b.expirationDate <= :until
      ORDER BY b.expirationDate ASC
    """)
    List<ItemBatch> findExpiringBefore(@Param("until") LocalDate until);

    @Query("""
      SELECT b FROM ItemBatch b
      WHERE b.item = :item AND b.expirationDate = :exp AND
            ((:code IS NULL AND b.batchCode IS NULL) OR b.batchCode = :code)
    """)
    Optional<ItemBatch> findOneByUniqueKey(@Param("item") Item item,
                                           @Param("exp") LocalDate expiration,
                                           @Param("code") String batchCode);

    @Query(
    value = """
      SELECT b FROM ItemBatch b
      JOIN FETCH b.item i
      WHERE i.id = :itemId
      ORDER BY b.expirationDate ASC, b.id ASC
    """,
    countQuery = """
      SELECT COUNT(b) FROM ItemBatch b
      WHERE b.item.id = :itemId
    """
  )
  Page<ItemBatch> findPageByItemIdWithItem(@Param("itemId") Long itemId, Pageable pageable);
}
