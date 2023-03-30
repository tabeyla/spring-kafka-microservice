package com.tabeyla.kafka.order.controller;


import com.tabeyla.kafka.domain.Order;
import com.tabeyla.kafka.order.service.OrderGeneratorService;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private static final Logger LOG = LoggerFactory.getLogger(OrderController.class);
    private final OrderGeneratorService orderGeneratorService;
    private final StreamsBuilderFactoryBean kafkaStreamsFactory;
    private AtomicLong id = new AtomicLong();
    private KafkaTemplate<Long, Order> template;

    public OrderController(KafkaTemplate<Long, Order> template,
                           StreamsBuilderFactoryBean kafkaStreamsFactory,
                           OrderGeneratorService orderGeneratorService) {
        this.template = template;
        this.kafkaStreamsFactory = kafkaStreamsFactory;
        this.orderGeneratorService = orderGeneratorService;
    }

    @PostMapping
    public Order create(@RequestBody Order order) {

        order.setId(id.incrementAndGet());
        template.send("orders", order.getId(), order);
        LOG.info("Sent: {}", order);
        return order;
    }


    @GetMapping
    public List<Order> all() {
        var orders = new ArrayList<Order>();
        ReadOnlyKeyValueStore<Long, Order> store = kafkaStreamsFactory
                .getKafkaStreams()
                .store(StoreQueryParameters.fromNameAndType(
                        "orders",
                        QueryableStoreTypes.keyValueStore()
                ));
                var it = store.all();
                it.forEachRemaining(kv -> orders.add(kv.value));
        return orders;
    }
}
