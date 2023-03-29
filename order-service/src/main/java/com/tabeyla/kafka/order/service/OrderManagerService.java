package com.tabeyla.kafka.order.service;

import com.tabeyla.kafka.domain.Order;
import org.springframework.stereotype.Service;

@Service
public class OrderManagerService {


    public Order confirm(Order orderPayment, Order orderStock) {
        Order o = new Order(orderPayment.getId(), orderPayment.getCustomerId(), orderPayment.getProductId(), orderPayment.getProductCount(), orderPayment.getPrice());

        if(orderPayment.getStatus().equals("ACCEPT") &&
            orderStock.getStatus().equals("ACCEPT")) {
            o.setStatus("CONFIRMED");
        }
        else if(orderPayment.getStatus().equals("REJECT") &&
                orderStock.getStatus().equals("REJECT")) {
            o.setStatus("REJECTED");
        }
        else if(orderPayment.getStatus().equals("REJECT") ||
                orderStock.getStatus().equals("REJECT")) {
            o.setStatus("ROLLBACK");
            o.setSource(orderPayment.getStatus().equals("REJECT")?"PAYMENT":"STOCK");
        }

        return o;

    }

}
