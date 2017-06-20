package com.example.crud.entities;

import lombok.Data;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedAttributeNode;
import javax.persistence.OneToMany;

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

    @OneToMany(cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH})
    public Set<Product> products = new HashSet<Product>();

    public Inventory(String name)
    {
        this.name = name;
    }
}
