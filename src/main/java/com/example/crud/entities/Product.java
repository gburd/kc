package com.example.crud.entities;

import lombok.Data;
import lombok.ToString;

import javax.annotation.Generated;
import javax.persistence.*;

/**
 * Definition of a Product
 * Represents a product, and contains the key aspects of the item.
 **/
@Data @Entity
@ToString
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public class Product
{
    /** Id for the product. */
    @Id
    private String id;

    /** Name of the Product. */
    @Basic
    private String name=null;

    /** Description of the Product. */
    @Basic
    private String description=null;

    /** Price of the Product. */
    @Basic
    private double price=0.0;

    /** Seller of this product. */
    @ManyToOne(optional = false)
    private Person seller;

    /**
     * Default constructor. 
     */
    protected Product() {
    }

    /**
     * Constructor.
     * @param name name of product
     * @param description description of product
     * @param price Price
     **/
    public Product(String name, String description, double price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }

}
