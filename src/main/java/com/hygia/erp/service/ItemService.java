package com.hygia.erp.service;

import com.hygia.erp.domain.Item;
import com.hygia.erp.dto.ItemDtos.ItemResponse;
import com.hygia.erp.dto.PageResponse;
import com.hygia.erp.repository.ItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ItemService {

    private final ItemRepository itemRepo;

    public ItemService(ItemRepository itemRepo) {
        this.itemRepo = itemRepo;
    }

    public PageResponse<ItemResponse> searchItems(String keyword, Boolean active, int page, int size) {
        String k = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        if (page < 0) page = 0;
        if (size <= 0) size = 20;

        Pageable pageable = PageRequest.of(
                page,
                size,
                // 先按创建时间降序，再按 id 降序稳定
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"))
        );

        Page<Item> p = itemRepo.search(k, active, pageable);

        return PageResponse.of(p.map(i -> new ItemResponse(
            i.getId(),
            i.getName(),
            i.getSku(),
            i.getBarcode(),
            i.getUnit(),
            i.isActive(),
            i.getQbItemId()
        )));
    }
}