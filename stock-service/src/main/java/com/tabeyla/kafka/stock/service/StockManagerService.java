package com.tabeyla.kafka.stock.service;

import com.tabeyla.kafka.domain.Order;
import com.tabeyla.kafka.stock.domain.Product;
import com.tabeyla.kafka.stock.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class StockManagerService {

    private static final String SOURCE = "stock";
    private static final Logger LOG = LoggerFactory.getLogger(StockManagerService.class);
    private ProductRepository repository;
    private KafkaTemplate<Long, Order> template;


    public StockManagerService(ProductRepository repository, KafkaTemplate<Long, Order> template) {
        this.repository = repository;
        this.template = template;
    }

    public void reserve(Order order){
        Product product = repository.findById(order.getProductId()).orElseThrow();
        LOG.info("Found {}", product);

        if(order.getPrice() < product.getAvailableItems()) {
            order.setStatus("ACCEPT");
            product.setReservedItems(product.getReservedItems() + order.getProductCount()); ;
            product.setAvailableItems(product.getAvailableItems() - order.getProductCount());
        }
        else {
            order.setStatus("REJECT");
        }
        order.setSource(SOURCE);
        repository.save(product);
        template.send("stock", order.getId(), order);
        LOG.info("Sent: {}", order);

    }

    public void confirm(Order order){

        Product product = repository.findById(order.getCustomerId()).orElseThrow();
        LOG.info("Found {}", product);

        if (order.getStatus().equals("CONFIRMED")) {
            product.setReservedItems(product.getReservedItems() - order.getProductCount());
            repository.save(product);
        }
        else if(order.getStatus().equals("ROLLBACK") && !order.getSource().equals(SOURCE)){
            product.setReservedItems(product.getReservedItems() - order.getProductCount());
            product.setAvailableItems(product.getAvailableItems() + order.getProductCount());

            repository.save(product);
        }


    }



}
