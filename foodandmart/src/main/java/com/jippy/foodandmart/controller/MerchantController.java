package com.jippy.foodandmart.controller;

import org.slf4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/merchants")
public class MerchantController {

    public static final Logger logger = org.slf4j.LoggerFactory.getLogger(MerchantController.class);

    @GetMapping("/listMerchants")
    public String getMerchants() {
        logger.info("Fetching list of merchants");
        return "List of merchants";
    }
}
