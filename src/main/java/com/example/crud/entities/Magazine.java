package com.example.crud.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * Definition of a Book. Extends basic Product class.
 **/
@Data @Entity
@EqualsAndHashCode(callSuper=false)
@ToString
@AllArgsConstructor
public class Magazine extends Product
{
    /** Author of the Book. */
    @Basic
    private String author = null;

    /** ISBN number of the book. */
    @Basic
    private String isbn = null;

    /** Publisher of the Book. */
    @Basic
    private String publisher = null;

    /**
     * Default Constructor.
     **/
    protected Magazine() {
        super();
    }

    /**
     * Constructor.
     * @param name name of product
     * @param description description of product
     * @param price Price
     * @param author Author of the book
     * @param isbn ISBN number of the book
     * @param publisher Name of publisher of the book 
     **/
    public Magazine(String name,
                String description,
                double price,
                String author,
                String isbn,
                String publisher) {
        super(name,description,price);
        this.author = author;
        this.isbn = isbn;
        this.publisher = publisher;
    }

}
