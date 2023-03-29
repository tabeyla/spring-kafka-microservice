package com.tabeyla.kafka.payment.service;


import com.tabeyla.kafka.domain.Order;
import com.tabeyla.kafka.payment.domain.Customer;
import com.tabeyla.kafka.payment.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentManagerService {

    private static final String SOURCE = "payment";
    private static final Logger LOG = LoggerFactory.getLogger(PaymentManagerService.class);
    private CustomerRepository repository;
    private KafkaTemplate<Long, Order> template;

    public PaymentManagerService(CustomerRepository repository, KafkaTemplate<Long, Order> template) {
        this.repository = repository;
        this.template = template;
    }

    public void reserve(Order order){
        Customer customer = repository.findById(order.getCustomerId()).orElseThrow();
        LOG.info("Found {}", customer);

        if(order.getPrice() < customer.getAmountAvailable()) {
            order.setStatus("ACCEPT");
            customer.setAmountReserved(customer.getAmountReserved() + order.getPrice()) ;
            customer.setAmountAvailable(customer.getAmountAvailable() - order.getPrice());
        }
        else {
            order.setStatus("REJECT");
        }
        order.setSource(SOURCE);
        repository.save(customer);
        template.send("payment", order.getId(), order);
        LOG.info("Sent: {}", order);

    }

    public void confirm(Order order){

        Customer customer = repository.findById(order.getCustomerId()).orElseThrow();
        LOG.info("Found {}", customer);

        if (order.getStatus().equals("CONFIRMED")) {
            customer.setAmountReserved(customer.getAmountReserved() - order.getPrice());
            repository.save(customer);
        }
        else if(order.getStatus().equals("ROLLBACK") && !order.getSource().equals(SOURCE)){
            customer.setAmountReserved(customer.getAmountReserved() - order.getPrice());
            customer.setAmountAvailable(customer.getAmountAvailable() + order.getPrice());

            repository.save(customer);
        }


    }
}
