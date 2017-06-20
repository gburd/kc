package com.example.crud.entities;

import lombok.Data;
import lombok.ToString;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

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
    private long id;

    /** Name of the Product. */
    @Basic
    private String name=null;

    /** Description of the Product. */
    @Basic
    private String description=null;

    /** Price of the Product. */
    @Basic
    @Column (name="THE_PRICE")
    private double price=0.0;

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
