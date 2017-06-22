package com.example.crud.entities;

import com.google.common.collect.ImmutableSet;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Definition of an Inventory of products.
 */
@Data @Entity
@ToString
@NamedEntityGraph(name = "allProps",
        attributeNodes = { @NamedAttributeNode("name"), @NamedAttributeNode("products") })
public class Inventory extends AbstractAuditableEntity<String> {

    @Id
    private String name=null;

    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH }, fetch = FetchType.EAGER)
    private Set<Product> products = new HashSet<Product>();

    public Inventory() {
    }

    public Inventory(String name) {
        this.name = name;
    }

    public void addProduct(Product product) {
        products.add(product);
    }

    public Iterable<Product> getProducts() {
        return ImmutableSet.copyOf(products);
    }

    public void clearProducts() {
        products.clear();
    }

}
