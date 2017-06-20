package com.example.crud.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Basic;
import javax.persistence.Entity;

/**
 * Definition of a Book. Extends basic Product class.
 **/
@Data @Entity
@EqualsAndHashCode(callSuper=false)
@ToString
public class Book extends Product
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
    protected Book() {
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
    public Book(String name,
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
