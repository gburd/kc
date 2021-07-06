package com.example.crud.entities;

import com.google.common.collect.ImmutableSet;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.datanucleus.api.jpa.annotations.DatastoreId;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Definition of an Inventory of products.
 */
@DatastoreId
@NamedEntityGraph(name = "allProps",
        attributeNodes = { @NamedAttributeNode("name"), @NamedAttributeNode("products") })
@ToString
@EqualsAndHashCode(callSuper=false)
@Getter @Setter
@IdClass(Inventory.ID.class)
public class Inventory extends AbstractAuditableEntity<String> {

    @Id
    private String name=null;

    @Id
    private String region=null;

    @Basic
    private String description;

    public Inventory() {
    }

    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH }, fetch = FetchType.EAGER)
    private Set<Product> products = new HashSet<Product>();

    public Inventory(String name) {
        this.name = name;
    }

    public void addProduct(Product product) {
        products.add(product);
    }

    public ImmutableSet<Product> getProducts() {
        return ImmutableSet.copyOf(products);
    }

    public void clearProducts() {
        products.clear();
    }

    public void setDescription(String description) {
        this.description = description;
    }

    class ID implements Serializable {
        String name;
        String region;
    }
}
