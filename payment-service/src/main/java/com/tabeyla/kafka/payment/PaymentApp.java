package com.tabeyla.kafka.payment;

import com.tabeyla.kafka.domain.Order;
import com.tabeyla.kafka.payment.domain.Customer;
import com.tabeyla.kafka.payment.repository.CustomerRepository;
import com.tabeyla.kafka.payment.service.PaymentManagerService;
import jakarta.annotation.PostConstruct;
import net.datafaker.Faker;

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

import java.time.Duration;
import java.util.Random;

@SpringBootApplication
@EnableKafka
public class PaymentApp {
    private static final Logger LOG = LoggerFactory.getLogger(PaymentApp.class);

    public static void main(String[] args) {
        SpringApplication.run(PaymentApp.class, args);

    }

    @Autowired
    PaymentManagerService paymentManagerService;
    @KafkaListener(id = "orders", topics = "orders", groupId = "payment")
    public void onEvent(Order o) {
        LOG.info("Received: {}", o);
        if(o.getStatus().equals("NEW")) {
            paymentManagerService.reserve(o);
        }
        else {
            paymentManagerService.confirm(o);
        }
    }


    @Autowired
    CustomerRepository customerRepository;

    @PostConstruct
    public void generateData() {
        Random r = new Random();
        Faker faker = new Faker();
        for(int i=0; i< 100; i++) {
            int count = r.nextInt(100, 1000);
            Customer c = new Customer(null, faker.name().fullName(), count,0);
            customerRepository.save(c);
        }
    }

}
