package com.example.crud.repositories;

import com.example.crud.entities.Inventory;
import com.example.crud.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, String> {

  Inventory findByName(String name);

  @Query(value        = "select * from inventory where product_id_eid contains :productId allow filtering",
          nativeQuery = true)
  List<Inventory> findByProduct(@Param("productId") String productId);

}
