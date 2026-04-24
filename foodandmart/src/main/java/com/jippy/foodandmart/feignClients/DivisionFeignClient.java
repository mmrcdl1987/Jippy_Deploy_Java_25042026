package com.jippy.foodandmart.feignClients;

import com.jippy.foodandmart.dto.DivPriceModelDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "division")
public interface DivisionFeignClient {

   @GetMapping(value = "/api/coupons/getPriceModels",consumes = "application/json")
    public ResponseEntity<List<DivPriceModelDto>> getPriceModels();
}
