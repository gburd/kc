package com.example.crud.entities;

import lombok.Data;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

/**
 * Definition of an Inventory of products.
 */
@Data @Entity
@ToString
@NamedEntityGraph(name="allProps",
    attributeNodes={@NamedAttributeNode("name"), @NamedAttributeNode("products")})
public class Inventory
{
    @Id
    private String name=null;

    @OneToMany(cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH}, fetch=FetchType.EAGER)
    public Set<Product> products = new HashSet<Product>();

    @Version
    long version;

    public Inventory() { }
    public Inventory(String name)
    {
        this.name = name;
    }
}
