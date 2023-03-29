package com.tabeyla.kafka.payment.repository;

import com.tabeyla.kafka.payment.domain.Customer;
import org.springframework.data.repository.CrudRepository;

public interface CustomerRepository extends CrudRepository<Customer, Long> {


}
