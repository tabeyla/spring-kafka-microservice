package com.tabeyla.kafka.stock.repository;

import com.tabeyla.kafka.stock.domain.Product;
import org.springframework.data.repository.CrudRepository;



public interface ProductRepository extends CrudRepository<Product, Long> {


}
