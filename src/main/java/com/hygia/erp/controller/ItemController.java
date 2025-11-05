package com.hygia.erp.controller;

import com.hygia.erp.domain.Item;
import com.hygia.erp.dto.ItemDtos.BatchResponse;
import com.hygia.erp.dto.ItemDtos.ItemResponse;
import com.hygia.erp.dto.PageResponse;
import com.hygia.erp.repository.ItemRepository;
import com.hygia.erp.service.BatchService;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemRepository itemRepo;
    private final BatchService batchService;

    public ItemController(ItemRepository itemRepo, BatchService batchService) {
        this.itemRepo = itemRepo;
        this.batchService = batchService;
    }

    @GetMapping
    public PageResponse<ItemResponse> list(@RequestParam(value = "keyword", required = false) String keyword,
                                           @RequestParam(value = "active", required = false) Boolean active,
                                           @RequestParam(value = "page", defaultValue = "0") int page,
                                           @RequestParam(value = "size", defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Item> p = itemRepo.search((keyword == null || keyword.isBlank()) ? null : keyword, active, pageable);
        Page<ItemResponse> mapped = p.map(i -> new ItemResponse(
                i.getId(), i.getName(), i.getSku(), i.getBarcode(), i.getUnit(), i.isActive(), i.getQbItemId()
        ));
        return PageResponse.of(mapped);
    }

    @GetMapping("/{id}/batches")
    public PageResponse<BatchResponse> listBatchesOfItem(
            @PathVariable("id") Long itemId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        return batchService.listBatchesOfItem(itemId, page, size);
    }

}