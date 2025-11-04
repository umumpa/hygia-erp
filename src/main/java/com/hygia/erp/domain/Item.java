package com.hygia.erp.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "item",
       indexes = {
         @Index(name="idx_item_name", columnList="name"),
         @Index(name="idx_item_qb_item_id", columnList="qb_item_id")
       },
       uniqueConstraints = {
         @UniqueConstraint(name="item_qb_item_id_key", columnNames = {"qb_item_id"}),
         @UniqueConstraint(name="item_barcode_key", columnNames = {"barcode"})
       })
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="qb_item_id")
    private String qbItemId;

    @Column(nullable = false)
    private String name;

    private String sku;

    private String barcode;

    private String unit;

    @Column(name="is_active", nullable = false)
    private boolean isActive = true;

    @Column(name="created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name="updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = false)
    private List<ItemBatch> batches;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQbItemId() {
        return qbItemId;
    }

    public void setQbItemId(String qbItemId) {
        this.qbItemId = qbItemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public java.util.List<ItemBatch> getBatches() {
        return batches;
    }

    public void setBatches(java.util.List<ItemBatch> batches) {
        this.batches = batches;
    }
}