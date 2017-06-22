package com.example.crud;

import com.example.crud.entities.*;
import com.example.crud.repositories.InventoryRepository;
import org.datanucleus.util.NucleusLogger;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Controlling application for the DataNucleus Tutorial using JPA.
 * Uses the "persistence-unit" called "Tutorial".
 */
public class Main {

    public static void cacheTest() {
        // Construct a simple local cache manager with default configuration
        DefaultCacheManager cacheManager = new DefaultCacheManager();
        // Define local cache configuration
        cacheManager.defineConfiguration("local", new ConfigurationBuilder().build());
        // Obtain the local cache
        Cache<String, String> cache = cacheManager.getCache("local");
        // Register a listener
        cache.addListener(new CacheClusterListener());
        // Store some values
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key1", "newValue");
        // Stop the cache manager and release all resources
        cacheManager.stop();
    }

    public static void main(String args[]) {
        //cacheTest();

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(ApplicationConfig.class);
        ctx.refresh();

        // Create an EntityManagerFactory for this "persistence-unit"
        // See the file "META-INF/persistence.xml"
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("crud");
        EntityManagerFactory emf_mongo = Persistence.createEntityManagerFactory("mongo");
        //MergingPersistenceUnitmanager

        // TODO:
        // * types: int, bool, etc.
        // * Set<>
        // * L2/Caching via Infinispan (embedded, clustered)
        // * MergingPersistenceUnitmanager
        // * Draft/(Fluent)Builder Immutable Entites

        // Persistence of a Product and a Book.
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Inventory inv = em.merge(new Inventory("My Inventory"));
            Product product = new Product("Sony Discman", "A standard discman from Sony", 200.00);
            inv.addProduct(product);
            Book book = new Book("Lord of the Rings by Tolkien", "The classic story", 49.99, "JRR Tolkien",
                    "12345678", "MyBooks Factory");
            Magazine magazine = new Magazine("Field and Stream", "A hunter's guide to the outdoors.", 3.29, "F&S, Inc.", "23984729347", "F&S, Inc.");
            inv.addProduct(book);
            inv.addProduct(magazine);
            em.persist(inv);

            tx.commit();
//            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(inv);
//            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(product);
//            System.out.println("Product and Book have been persisted, inventory: " + inv.getPrimaryKey().toString() + ", product: " + product.getPrimaryKey().toString());
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

        em = emf.createEntityManager();
        JpaRepositoryFactory factory = new JpaRepositoryFactory(em);
        InventoryRepository repository = factory.getRepository(InventoryRepository.class);
        Inventory inventory = repository.findByName("My Inventory");
        System.out.println("SpringData/JPA: " + inventory.toString());
        em.close();

        // Clean out the database
        emf.getCache().evictAll();
        em = emf.createEntityManager();
        tx = em.getTransaction();
        try {
            tx.begin();

            System.out.println("Deleting all products from persistence");
            inv = (Inventory) em.find(Inventory.class, "My Inventory");

            System.out.println("Clearing out Inventory");
            inv.clearProducts();
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
