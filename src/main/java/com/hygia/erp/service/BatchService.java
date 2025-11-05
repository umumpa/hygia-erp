package com.hygia.erp.service;


import com.hygia.erp.domain.Item;
import com.hygia.erp.domain.ItemBatch;
import com.hygia.erp.dto.ItemDtos.BatchReceiveRequest;
import com.hygia.erp.dto.ItemDtos.BatchResponse;
import com.hygia.erp.repository.ItemBatchRepository;
import com.hygia.erp.repository.ItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.hygia.erp.dto.PageResponse;

@Service
public class BatchService {

    private final ItemRepository itemRepo;
    private final ItemBatchRepository batchRepo;

    public BatchService(ItemRepository itemRepo, ItemBatchRepository batchRepo) {
        this.itemRepo = itemRepo;
        this.batchRepo = batchRepo;
    }

    /**
     * 入库（支持方案B）：
     *  A) 传 itemId；
     *  B) 不传 itemId，但传 sku/barcode/qbItemId 任一。若查无此物且提供 name（可选 unit），则自动创建 Item。
     *  幂等键：(item, expirationDate, batchCode) 合并数量。
     */
    @Transactional
    public BatchResponse receive(BatchReceiveRequest req) {
        // 基础校验
        if (req.expirationDate() == null) {
            throw new IllegalArgumentException("expirationDate 为必填");
        }
        if (req.quantity() == null) {
            throw new IllegalArgumentException("quantity 为必填");
        }
        if (req.quantity() < 0) {
            throw new IllegalArgumentException("quantity 不能为负数");
        }

        // 解析或创建 Item
        Item item = resolveOrCreateItem(req);

        // 幂等合并：同 (item, expiration, batchCode) 则累加数量
        ItemBatch batch = batchRepo.findOneByUniqueKey(item, req.expirationDate(), req.batchCode())
                .orElseGet(() -> {
                    ItemBatch b = new ItemBatch();
                    b.setItem(item);
                    b.setExpirationDate(req.expirationDate());
                    b.setBatchCode(req.batchCode());
                    b.setLocation(req.location());
                    b.setNote(req.note());
                    return b;
                });

        int oldQty = batch.getQuantity() == null ? 0 : batch.getQuantity();
        batch.setQuantity(oldQty + req.quantity());
        if (req.location() != null) batch.setLocation(req.location());
        if (req.note() != null) batch.setNote(req.note());

        ItemBatch saved = batchRepo.save(batch);
        return new BatchResponse(
                saved.getId(),
                saved.getItem().getId(),
                saved.getItem().getName(),
                saved.getBatchCode(),
                saved.getExpirationDate(),
                saved.getQuantity(),
                saved.getLocation()
        );
    }

    private Item resolveOrCreateItem(BatchReceiveRequest req) {
        // 1) 优先用 itemId
        if (req.itemId() != null) {
            return itemRepo.findById(req.itemId())
                    .orElseThrow(() -> new IllegalArgumentException("itemId 不存在: " + req.itemId()));
        }

        // 2) 外部标识：barcode > sku > qbItemId
        Optional<Item> byBarcode = (req.barcode() == null || req.barcode().isBlank())
                ? Optional.empty()
                : itemRepo.findByBarcode(req.barcode().trim());
        if (byBarcode.isPresent()) return byBarcode.get();

        Optional<Item> bySku = (req.sku() == null || req.sku().isBlank())
                ? Optional.empty()
                : itemRepo.findBySku(req.sku().trim());
        if (bySku.isPresent()) return bySku.get();

        Optional<Item> byQb = (req.qbItemId() == null || req.qbItemId().isBlank())
                ? Optional.empty()
                : itemRepo.findByQbItemId(req.qbItemId().trim());
        if (byQb.isPresent()) return byQb.get();

        // 3) 查不到：若给了 name（可选 unit），自动建档；否则报错
        boolean hasAnyExternalKey = notBlank(req.barcode()) || notBlank(req.sku()) || notBlank(req.qbItemId());
        if (!hasAnyExternalKey && isBlank(req.name())) {
            throw new IllegalArgumentException("请提供 itemId，或至少提供 sku/barcode/qbItemId 其一；如需自动建档，还需提供 name");
        }
        if (isBlank(req.name())) {
            throw new IllegalArgumentException("自动创建商品时，name 为必填");
        }

        Item toCreate = new Item();
        toCreate.setName(req.name().trim());
        if (notBlank(req.qbItemId())) toCreate.setQbItemId(req.qbItemId().trim());
        if (notBlank(req.sku())) toCreate.setSku(req.sku().trim());
        if (notBlank(req.barcode())) toCreate.setBarcode(req.barcode().trim());
        if (notBlank(req.unit())) toCreate.setUnit(req.unit().trim());
        toCreate.setActive(true);
        return itemRepo.save(toCreate);
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    private static boolean notBlank(String s) { return !isBlank(s); }

    public java.util.List<BatchResponse> expiringSoon(int days) {
        LocalDate until = LocalDate.now().plusDays(days <= 0 ? 30 : days);
        return batchRepo.findExpiringBefore(until).stream()
                .map(b -> new BatchResponse(
                        b.getId(),
                        b.getItem().getId(),
                        b.getItem().getName(),
                        b.getBatchCode(),
                        b.getExpirationDate(),
                        b.getQuantity(),
                        b.getLocation()
                ))
                .toList();
    }
    
    public PageResponse<BatchResponse> listBatchesOfItem(Long itemId, int page, int size) {
        if (itemId == null) {
            throw new IllegalArgumentException("itemId 为必填");
        }
        if (page < 0) page = 0;
        if (size <= 0) size = 20;

        Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.ASC, "expirationDate").and(Sort.by("id"))
        );

        Page<ItemBatch> p = batchRepo.findPageByItemIdWithItem(itemId, pageable);

        return PageResponse.of(
            p.map(b -> new BatchResponse(
                b.getId(),
                b.getItem().getId(),
                b.getItem().getName(),
                b.getBatchCode(),
                b.getExpirationDate(),
                b.getQuantity(),
                b.getLocation()
            ))
        );
    }
}