package com.hygia.erp.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "item_batch",
       indexes = {
         @Index(name="idx_item_batch_item_id", columnList="item_id"),
         @Index(name="idx_item_batch_expiration", columnList="expiration_date"),
         @Index(name="idx_item_batch_item_exp", columnList="item_id,expiration_date")
       },
       uniqueConstraints = {
         @UniqueConstraint(name="uq_item_batch", columnNames = {"item_id","expiration_date","batch_code"})
       })
public class ItemBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 多对一，指向 item.id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="item_id", nullable = false,
                foreignKey = @ForeignKey(name="item_batch_item_id_fkey"))
    private Item item;

    @Column(name="batch_code")
    private String batchCode;

    @Column(name="expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Column(nullable = false)
    private Integer quantity = 0;

    private String location;

    @Column(name="received_at", nullable = false)
    private OffsetDateTime receivedAt = OffsetDateTime.now();

    private String note;

    @Column(name="created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name="updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getBatchCode() {
        return batchCode;
    }

    public void setBatchCode(String batchCode) {
        this.batchCode = batchCode;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public OffsetDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(OffsetDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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
}