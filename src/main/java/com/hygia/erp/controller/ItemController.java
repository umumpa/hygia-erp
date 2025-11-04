package com.hygia.erp.controller;

import com.hygia.erp.domain.Item;
import com.hygia.erp.dto.ItemDtos.ItemResponse;
import com.hygia.erp.dto.PageResponse;
import com.hygia.erp.repository.ItemRepository;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemRepository itemRepo;

    public ItemController(ItemRepository itemRepo) {
        this.itemRepo = itemRepo;
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
        return new PageResponse<>(mapped);
    }
}