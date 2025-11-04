package com.hygia.erp.dto;

import java.time.LocalDate;

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
            String qbItemId,     
            String sku,          
            String barcode,      
            String name,         
            String unit,         

            // batch information
            LocalDate expirationDate, 
            Integer quantity,         
            String location,          
            String batchCode,         
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