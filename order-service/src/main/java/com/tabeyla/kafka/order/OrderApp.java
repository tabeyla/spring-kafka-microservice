package com.tabeyla.kafka.order;

import com.tabeyla.kafka.domain.Order;
import com.tabeyla.kafka.order.service.OrderManagerService;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

@SpringBootApplication
@EnableKafka
@EnableKafkaStreams
@EnableAsync
public class OrderApp {
    private static final Logger LOG = LoggerFactory.getLogger(OrderApp.class);

    public static void main(String[] args) {

        SpringApplication.run(OrderApp.class, args);

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

    @Autowired
    OrderManagerService orderManagerService;

    @Bean
    public KStream<Long, Order> stream(StreamsBuilder builder) {
        JsonSerde<Order> orderSerde = new JsonSerde<>(Order.class);

        var stream = builder.stream("payment", Consumed.with(Serdes.Long(), orderSerde));
        stream.join(
          builder.stream("stock"),
          orderManagerService::confirm,
                JoinWindows.of(Duration.ofSeconds(10)),
                StreamJoined.with(Serdes.Long(), orderSerde, orderSerde)

        ).peek((k,o) -> LOG.info("Output: {}", o)).to("orders");

        return stream;
    }

    @Bean
    public KTable<Long, Order> table(StreamsBuilder builder) {
        KeyValueBytesStoreSupplier store =
                Stores.persistentKeyValueStore("orders");
        JsonSerde<Order> orderSerde = new JsonSerde<>(Order.class);
        KStream<Long, Order> stream = builder
                .stream("orders", Consumed.with(Serdes.Long(), orderSerde));
        return stream.toTable(Materialized.<Long, Order>as(store)
                .withKeySerde(Serdes.Long())
                .withValueSerde(orderSerde));
    }

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(5);
        executor.setThreadNamePrefix("kafkaSender-");
        executor.initialize();
        return executor;
    }

}
