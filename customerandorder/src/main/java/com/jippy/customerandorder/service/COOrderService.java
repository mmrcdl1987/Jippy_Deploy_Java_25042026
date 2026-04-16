package com.jippy.customerandorder.service;

import com.jippy.customerandorder.dto.COOrderEvent;
import com.jippy.customerandorder.mapper.COEventMapper;
import com.jippy.customerandorder.modal.COOrders;
import com.jippy.customerandorder.repository.COOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class COOrderService {

    public  static Logger logger = LoggerFactory.getLogger(COOrderService.class);

    @Autowired
    private COOrderRepository COOrderRepository;

    @Autowired
    private KafkaTemplate<String, COOrderEvent> kafkaTemplate;

    public void placeOrder() {
        // write logic to save order to database
        COOrders orders = new COOrders();
       createOrderEvent(orders);

        
    }

    private void createOrderEvent(COOrders COOrders) {

        COOrderEvent orderEvent = COEventMapper.mapToOrderEvent();
        kafkaTemplate.send("new-orders", "order: "+String.valueOf(orderEvent.getOrderId()), orderEvent);
        logger.info("sent message to kafka");
    }

}
