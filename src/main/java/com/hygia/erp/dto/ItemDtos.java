package com.hygia.erp.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.*;

public class ItemDtos {

    public record ItemResponse(
            Long id,
            String name,
            String sku,
            String barcode,
            String unit,
            boolean isActive,
            String qbItemId
    ) {}

    public record BatchReceiveRequest(
        Long itemId,

        @Size(max = 128)
        String qbItemId,

        @Size(max = 128)
        String sku,

        @Size(max = 128)
        String barcode,

        @Size(min = 1, max = 200)
        String name,

        @Size(max = 32)
        String unit,

        @NotNull
        LocalDate expirationDate,

        @NotNull
        @Positive
        Integer quantity,

        @Size(max = 100)
        String location,

        @Size(max = 100)
        String batchCode,

        @Size(max = 500)
        String note
) {}

 
    public record BatchResponse(
            Long id,
            Long itemId,
            String itemName,
            String batchCode,
            LocalDate expirationDate,
            Integer quantity,
            String location
    ) {}
}