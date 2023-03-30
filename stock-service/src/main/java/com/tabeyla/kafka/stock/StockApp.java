package com.tabeyla.kafka.stock;

import com.tabeyla.kafka.domain.Order;
import com.tabeyla.kafka.payment.domain.Product;
import com.tabeyla.kafka.payment.repository.ProductRepository;
import com.tabeyla.kafka.stock.service.StockManagerService;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.StreamJoined;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Random;

@SpringBootApplication
@EnableKafka
public class StockApp {
    private static final Logger LOG = LoggerFactory.getLogger(StockApp.class);

    public static void main(String[] args) {
        SpringApplication.run(StockApp.class, args);

    }

    @Autowired
    StockManagerService stockManagerService;

    @KafkaListener(id="orders", topics="orders", groupId = "stock")
    public void onEvent(Order o) {
        LOG.info("Received: {}" , o);
        if (o.getStatus().equals("NEW"))
            stockManagerService.reserve(o);
        else
            stockManagerService.confirm(o);
    }


    @Autowired
    private ProductRepository repository;

    @PostConstruct
    public void generateData() {
        Random r = new Random();
        for (int i = 0; i < 1000; i++) {
            int count = r.nextInt(10, 1000);
            Product p = new Product(null, "Product" + i, count, 0);
            repository.save(p);
        }
    }





}
