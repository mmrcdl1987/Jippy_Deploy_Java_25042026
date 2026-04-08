package com.jippy.customerandorder.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class orderController {

    public  static Logger logger = LoggerFactory.getLogger(orderController.class);

    @GetMapping("/order")
     public String order(){
        logger.info("order api is called");
        return "order api is called";
     }
}
