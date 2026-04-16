package com.jippy.customerandorder.mapper;

import com.jippy.customerandorder.Constants.COConstants;
import com.jippy.customerandorder.dto.COOrderEvent;

public class COEventMapper {

    public static COOrderEvent mapToOrderEvent(){
        COOrderEvent COOrderEvent = new COOrderEvent();
        COOrderEvent.setOrderId(1);
        COOrderEvent.setOutletId(1);
        COOrderEvent.setCustomerId(1);
        COOrderEvent.setStatus(COConstants.newOrderPlaced);
        return COOrderEvent;
    }
}
