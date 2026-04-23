package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.ApiResponse;
import com.jippy.foodandmart.dto.BulkUploadResultDTO;
import com.jippy.foodandmart.dto.MerchantRequestDTO;
import com.jippy.foodandmart.entity.Merchant;
import com.jippy.foodandmart.service.interfaces.IMerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/merchants")
@RequiredArgsConstructor
@Slf4j
public class MerchantController {

    private final IMerchantService merchantService;

    /** POST /api/merchants — create a single merchant */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Merchant>> createMerchant(
            @Valid @RequestBody MerchantRequestDTO dto) {

        log.info("[MERCHANT] POST /api/merchants email={}, phone={}", dto.getEmail(), dto.getPhone());
        Merchant saved = merchantService.createMerchant(dto);
        log.info("[MERCHANT] Created: merchantId={}", saved.getMerchantId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Merchant registered successfully", saved));
    }

    /** GET /api/merchants — list all */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Merchant>>> getAllMerchants() {
        log.info("[MERCHANT] GET /api/merchants");
        return ResponseEntity.ok(ApiResponse.success("Merchants fetched", merchantService.getAllMerchants()));
    }

    /** GET /api/merchants/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Merchant>> getMerchantById(@PathVariable Integer id) {
        log.info("[MERCHANT] GET /api/merchants/{}", id);
        return ResponseEntity.ok(ApiResponse.success("Merchant fetched", merchantService.getMerchantById(id)));
    }

    /** GET /api/merchants/count */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getCount() {
        return ResponseEntity.ok(ApiResponse.success("Count fetched", merchantService.countMerchants()));
    }

    /** POST /api/merchants/upload — bulk upload via .xlsx or .csv */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BulkUploadResultDTO>> uploadFile(
            @RequestParam("file") MultipartFile file) {

        log.info("[BULK] POST /api/merchants/upload file={}, size={} bytes",
                file.getOriginalFilename(), file.getSize());

        if (file.isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Uploaded file is empty"));

        BulkUploadResultDTO result = merchantService.bulkUpload(file);
        String message = String.format("Upload complete: %d success, %d failed out of %d rows",
                result.getSuccessCount(), result.getFailureCount(), result.getTotalRows());

        HttpStatus status = result.getFailureCount() == 0 ? HttpStatus.OK
                : (result.getSuccessCount() == 0 ? HttpStatus.BAD_REQUEST : HttpStatus.MULTI_STATUS);

        return ResponseEntity.status(status).body(ApiResponse.success(message, result));
    }
}
