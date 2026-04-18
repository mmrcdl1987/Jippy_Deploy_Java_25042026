package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmOutletDetailsDto;
import com.jippy.foodandmart.exception.InvalidUserTypeException;
import com.jippy.foodandmart.service.IFmOutletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/outlets")
@Tag(
        name = "Outlet API",
        description = "APIs for fetching outlet details, menu, categories, and timings"
)
public class FmOutletController {

    private static final Logger logger =
            LoggerFactory.getLogger(FmOutletController.class);

    private static final String CUSTOMER = "CUSTOMER";
    private static final String MERCHANT = "MERCHANT";

    @Autowired
    private IFmOutletService outletService;

    @Operation(
            summary = "Get Outlet Details",
            description = "Fetch outlet details including menu, categories, product timings and outlet timings based on user type"
    )
    @ApiResponse(responseCode = "200", description = "Outlet details fetched successfully")
    @ApiResponse(responseCode = "400", description = "Invalid userType")
    @ApiResponse(responseCode = "404", description = "Outlet not found")
    @GetMapping("/getOutletDetails")
    public ResponseEntity<FmOutletDetailsDto> getOutletDetails(

            @Parameter(description = "Outlet ID", required = true)
            @RequestParam Integer outletId,

            @Parameter(description = "User Type (CUSTOMER / MERCHANT)", required = true)
            @RequestParam String userType)
    {

        logger.info("Fetching outlet details for outletId: {}, userType: {}", outletId, userType);

        // Custom validation
        if (!CUSTOMER.equalsIgnoreCase(userType) && !MERCHANT.equalsIgnoreCase(userType)) {
            throw new InvalidUserTypeException("Invalid userType. Allowed values: CUSTOMER or MERCHANT");
        }

        FmOutletDetailsDto outletDetails =
                outletService.getOutletDetails(outletId, userType);

        logger.info("Successfully fetched outlet details for outletId: {}, userType: {}", outletId, userType);

        return ResponseEntity.ok(outletDetails);
    }
}