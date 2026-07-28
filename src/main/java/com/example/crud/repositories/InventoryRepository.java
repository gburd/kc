package com.example.crud.repositories;

import com.example.crud.entities.Inventory;
import org.datanucleus.api.jakarta.annotations.ReadOnly;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, String>, JpaSpecificationExecutor {

    @Transactional
    @Cacheable(value = "inventory", key = "#name")
    @Query(value="select * from inventory where firstName = :name", nativeQuery=true)
    Inventory findByName(@Param("name") String name);

    @ReadOnly
    @Query(value = "select * from inventory where product_id_eid contains :productId allow filtering",
            nativeQuery = true)
    List<Inventory> findByProduct(@Param("productId") String productId);

    @Transactional
    @CacheEvict(value = "inventory", key = "#name")
    void deleteInventoryByName(String name);

    @Transactional
    @CacheEvict(value = "inventory", key = "#name")
    <S extends String> S save(S s);
}
