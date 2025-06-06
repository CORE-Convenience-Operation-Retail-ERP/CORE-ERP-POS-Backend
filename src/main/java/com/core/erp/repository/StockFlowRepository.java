package com.core.erp.repository;

import com.core.erp.domain.StockFlowEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockFlowRepository extends JpaRepository<StockFlowEntity, Long> {

    // 통합 검색 조건 (storeId null 허용)
    @Query("""
        SELECT f FROM StockFlowEntity f
        WHERE (:storeId IS NULL OR f.store.storeId = :storeId)
          AND (:productId IS NULL OR f.product.productId = :productId)
          AND (:productName IS NULL OR f.product.proName LIKE %:productName%)
          AND (:flowType IS NULL OR f.flowType = :flowType)
          AND (:startDate IS NULL OR f.flowDate >= :startDate)
          AND (:endDate IS NULL OR f.flowDate <= :endDate)
        ORDER BY f.flowDate DESC
    """)
    Page<StockFlowEntity> searchStockFlows(
            @Param("storeId") Integer storeId,
            @Param("productId") Long productId,
            @Param("productName") String productName,
            @Param("flowType") Integer flowType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
