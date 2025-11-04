package com.hygia.erp.controller;

import com.hygia.erp.dto.ItemDtos.BatchReceiveRequest;
import com.hygia.erp.dto.ItemDtos.BatchResponse;
import com.hygia.erp.dto.PageResponse;
import com.hygia.erp.service.BatchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/batches")
public class BatchController {

    private final BatchService batchService;

    public BatchController(BatchService batchService) {
        this.batchService = batchService;
    }

    // 入库（同 item+expiration+batchCode 合并数量）
    @PostMapping("/receive")
    public BatchResponse receive(@RequestBody BatchReceiveRequest req) {
        return batchService.receive(req);
    }

    // 近效期列表（默认 30 天）
    @GetMapping("/expiring-soon")
    public List<BatchResponse> expiringSoon(@RequestParam(defaultValue = "30") int days) {
        return batchService.expiringSoon(days);
    }
}