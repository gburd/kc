package com.example.crud.repositories;

import com.example.crud.entities.Inventory;
import org.datanucleus.api.jpa.annotations.ReadOnly;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.util.List;

//@Metrics(registry = "${this.registry}")
public interface InventoryRepository extends JpaRepository<Inventory, String>, JpaSpecificationExecutor {

    //@Metered(name = "${this.id}")
    @Transactional
    @Cacheable(value = "inventory", key = "#name")
    Inventory findByName(String name);

    @ReadOnly
    @Query(value = "select * from inventory where product_id_eid contains :productId allow filtering",
            nativeQuery = true)
    List<Inventory> findByProduct(@Param("productId") String productId);

    @Transactional
    @CacheEvict(value = "inventory", key = "#name")
    void deleteInventoryBy(String name);

    @Transactional
        //CacheEvict(value = "inventory", key = "#name")
    <S extends String> S save(S s);
}
