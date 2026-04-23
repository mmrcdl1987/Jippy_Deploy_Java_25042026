package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmMerchantWithBankDto;
import com.jippy.foodandmart.service.IFmMerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchants")
@Tag(
        name = "Merchant API",
        description = "APIs for managing merchant and bank details"
)
public class FmMerchantController {
    private static final Logger logger =
            LoggerFactory.getLogger(FmMerchantController.class);
    @Autowired
    private IFmMerchantService merchantService;
//
//    get merchant details with bank details
    @Operation(
            summary = "Get Merchant Profile with Bank Details",
            description = "Fetch merchant details along with bank information using merchant ID"
    )
    @ApiResponse(responseCode = "200", description = "Merchant fetched successfully")
    @ApiResponse(responseCode = "404", description = "Merchant not found")
    @GetMapping("/getMerchantProfile")
    public ResponseEntity<FmMerchantWithBankDto> getMerchantProfile(
            @RequestParam Long merchantId) {
        logger.info("Fetching merchant profile for merchantId: {}", merchantId);
        FmMerchantWithBankDto response =
                merchantService.getMerchantWithBank(merchantId);
        logger.info("Successfully fetched merchant profile for merchantId: {}", merchantId);
        return ResponseEntity.ok(response);
    }
// update merchant with bank details
    @Operation(
            summary = "Update Merchant Profile",
            description = "Update merchant and bank details"
    )
    @ApiResponse(responseCode = "200", description = "Merchant updated successfully")
    @ApiResponse(responseCode = "404", description = "Merchant or bank not found")
    @PutMapping("/updateMerchantProfile")
    public ResponseEntity<FmMerchantWithBankDto> updateMerchantProfile(@Valid
            @RequestBody FmMerchantWithBankDto dto) {
        logger.info("Updating merchant profile with data: {}", dto);

        FmMerchantWithBankDto updated =
                merchantService.updateMerchantProfile(dto);
        logger.info("Successfully updated merchant profile for merchantId: {}", dto.getMerchantId());

        return ResponseEntity.ok(updated);
    }

}
