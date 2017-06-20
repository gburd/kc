package com.example.crud;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.EntityGraph;
import javax.persistence.Persistence;
import javax.persistence.Query;

import com.example.crud.entities.*;

import org.datanucleus.enhancer.DataNucleusEnhancer;
import org.datanucleus.util.NucleusLogger;

/**
 * Controlling application for the DataNucleus Tutorial using JPA.
 * Uses the "persistence-unit" called "Tutorial".
 */
public class Main {
    public static void main(String args[]) {
        DataNucleusEnhancer enhancer = new DataNucleusEnhancer("JPA", null);
        enhancer.setVerbose(true);
        enhancer.addPersistenceUnit("crud");
        enhancer.addPersistenceUnit("mongo");
        enhancer.enhance();

        // Create an EntityManagerFactory for this "persistence-unit"
        // See the file "META-INF/persistence.xml"
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("crud");
        EntityManagerFactory emf_mongo = Persistence.createEntityManagerFactory("mongo");

        // Persistence of a Product and a Book.
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Inventory inv = em.merge(new Inventory("My Inventory"));
            Product product = new Product("Sony Discman", "A standard discman from Sony", 200.00);
            inv.getProducts().add(product);
            Book book = new Book("Lord of the Rings by Tolkien", "The classic story", 49.99, "JRR Tolkien",
                    "12345678", "MyBooks Factory");
            Magazine magazine = new Magazine("Field and Stream", "A hunter's guide to the outdoors.", 3.29, "F&S, Inc.", "23984729347", "F&S, Inc.");
            inv.getProducts().add(book);
            inv.getProducts().add(magazine);
            em.persist(inv);

            tx.commit();
            System.out.println("Product and Book have been persisted");
        }
        catch (Exception e) {
            NucleusLogger.GENERAL.error(">> Exception persisting data", e);
            System.err.println("Error persisting data : " + e.getMessage());
            return;
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        emf.getCache().evictAll();
        System.out.println("");

        // Perform a retrieve of the Inventory and detach it (by closing the EM)
        em = emf.createEntityManager();
        tx = em.getTransaction();
        Inventory inv = null;
        try {
            tx.begin();

            // Do a find() of the Inventory
            // Set the EntityGraph as something pulling in all Products
            // Note : you could achieve the same by either
            // 1). access the Inventory.products field before commit
            // 2). set fetch=EAGER for the Inventory.products field
            System.out.println("Executing find() on Inventory");
            EntityGraph allGraph = em.getEntityGraph("allProps");
            Map hints = new HashMap();
            hints.put("javax.persistence.loadgraph", allGraph);
            inv = em.find(Inventory.class, "My Inventory", hints);
            System.out.println("Retrieved Inventory as " + inv);

            tx.commit();
        }
        catch (Exception e) {
            NucleusLogger.GENERAL.error(">> Exception performing find() on data", e);
            System.err.println("Error performing find() on data : " + e.getMessage());
            return;
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close(); // This will detach all current managed objects
        }
        for (Product prod : inv.getProducts()) {
            System.out.println(">> After Detach : Inventory has a product=" + prod);
        }
        System.out.println("");

        // Add a person to MongoDB
        em = emf_mongo.createEntityManager();
        tx = em.getTransaction();
        Person person;
        try {
            tx.begin();
            person = new Person();
            person.setPersonFirstName("James");
            person.setPersonLastName("Bond");
            person.setAge(42);
            em.merge(person);
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close(); // This will detach all current managed objects
        }

        // Perform some query operations
        em = emf.createEntityManager();
        tx = em.getTransaction();
        try {
            tx.begin();
            System.out.println("Executing Query for Products with price below 150.00");
            Query q = em.createQuery("SELECT p FROM Product p WHERE p.price < 150.00 ORDER BY p.price");
            List results = q.getResultList();
            Iterator iter = results.iterator();
            while (iter.hasNext()) {
                Object obj = iter.next();
                System.out.println(">  " + obj);
                // Give an example of an update
                if (obj instanceof Book) {
                    Book b = (Book) obj;
                    b.setDescription(b.getDescription() + " REDUCED");
                }
                if (obj instanceof Magazine) {
                    Magazine m = (Magazine) obj;
                    m.setDescription(m.getDescription() + " SPECIAL");
                }
            }

            tx.commit();
        }
        catch (Exception e) {
            NucleusLogger.GENERAL.error(">> Exception querying data", e);
            System.err.println("Error querying data : " + e.getMessage());
            return;
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        System.out.println("");

        // Clean out the database
        em = emf.createEntityManager();
        tx = em.getTransaction();
        try {
            tx.begin();

            System.out.println("Deleting all products from persistence");
            inv = (Inventory) em.find(Inventory.class, "My Inventory");

            System.out.println("Clearing out Inventory");
            inv.getProducts().clear();
            em.flush();

            System.out.println("Deleting Inventory");
            em.remove(inv);

            System.out.println("Deleting all products from persistence");
            Query q = em.createQuery("SELECT p FROM Product p");
            List<Product> products = q.getResultList();
            int numDeleted = 0;
            for (Product prod : products) {
                em.remove(prod);
                numDeleted++;
            }
            System.out.println("Deleted " + numDeleted + " products");

            tx.commit();
        }
        catch (Exception e) {
            NucleusLogger.GENERAL.error(">> Exception in bulk delete of data", e);
            System.err.println("Error in bulk delete of data : " + e.getMessage());
            return;
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

        System.out.println("");
        System.out.println("End of Tutorial");
        emf.close();
    }
}
