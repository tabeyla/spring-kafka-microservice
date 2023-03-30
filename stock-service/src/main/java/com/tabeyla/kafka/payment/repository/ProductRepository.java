package com.tabeyla.kafka.payment.repository;

import com.tabeyla.kafka.payment.domain.Product;
import org.springframework.data.repository.CrudRepository;

public interface ProductRepository extends CrudRepository<Product, Long> {


}
