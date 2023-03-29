package com.tabeyla.kafka.payment;

import com.tabeyla.kafka.domain.Order;
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
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.scheduling.annotation.EnableAsync;

import java.time.Duration;

@SpringBootApplication
@EnableAsync

public class PaymentApp {
    private static final Logger LOG = LoggerFactory.getLogger(PaymentApp.class);

    public static void main(String[] args) {
        SpringApplication.run(PaymentApp.class, args);

    }

    @Bean
    public NewTopic orders() {
       return TopicBuilder.name("orders").partitions(1).compact().build();
    }

    @Bean
    public NewTopic payment() {
        return TopicBuilder.name("payment").partitions(1).compact().build();
    }
    @Bean
    public NewTopic stock() {
        return TopicBuilder.name("stock").partitions(1).compact().build();
    }





}
