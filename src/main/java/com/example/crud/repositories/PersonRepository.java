package com.example.crud.repositories;

import com.example.crud.entities.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PersonRepository extends JpaRepository<Inventory, String>, JpaSpecificationExecutor {
}
