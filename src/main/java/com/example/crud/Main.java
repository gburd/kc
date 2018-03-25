package com.example.crud;

import com.codahale.metrics.ehcache.InstrumentedEhcache;
import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.example.crud.entities.*;
import com.example.crud.repositories.InventoryRepository;
import com.example.crud.repositories.PersonRepository;
import org.datanucleus.api.jpa.JPAEntityManager;
import org.datanucleus.state.ObjectProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// TODO:
// * LOCAL_QUORUM
// * compound primary keys
// * pillar for DDL
// * metrics
// * com.datastax.driver.core.Cluster.builder().withQueryOptions(‌​new QueryOptions().setConsistencyLevel(ConsistencyLevel.QUORUM))
// * https://github.com/brndnmtthws/metrics-cassandra (c* as a sink for metrics)
// * https://github.com/addthis/metrics-reporter-config


/**
 * Controlling application for the DataNucleus Tutorial using JPA.
 * Uses the "persistence-unit" called "Tutorial".
 */
public class Main {
    Logger log = LoggerFactory.getLogger(getClass().getName());

    EntityManagerFactory cassandraEntityManagerFactory;
    EntityManagerFactory mongoEntityManagerFactory;
    Cluster cluster;

    String personId;

    public static void main(String args[]) {
        Main main = new Main();
    }

    public Main() {
        System.setProperty("DEBUG.MONGO", "true"); // Enable MongoDB logging in general
        System.setProperty("DB.TRACE", "true"); // Enable DB operation tracing

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.scan("com.example");
        ctx.register(ApplicationConfiguration.class);
        ctx.refresh();

        // Create an EntityManagerFactory for this "persistence-unit"
        // See the file "META-INF/persistence.xml"
        cassandraEntityManagerFactory = Persistence.createEntityManagerFactory("crud");
        mongoEntityManagerFactory = Persistence.createEntityManagerFactory("mongo");

        cluster = ctx.getBean(Cluster.class);
        /*
        Configuration configuration = cluster.getConfiguration();
        Metadata metadata = cluster.getMetadata();
        log.debug(configuration);
        log.debug(metadata); */
        /*
        Session session = ctx.getBean(Session.class);
        Select s = QueryBuilder.select().all().from("kc", "inventory");
        s.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        ResultSet rs = session.execute(s);
        log.debug(rs.toString());
        */
        /*
       //org.datanucleus.api.jpa.JPAEntityTransaction tx = (org.datanucleus.api.jpa.JPAEntityTransaction)pm.currentTransaction();
        //tx.setOption("transaction.isolation", 2);
         */

        this.a().b().c().e().f().g().z();
        cassandraEntityManagerFactory.close();
    }

    public Main a() {
        EntityManager em;
        EntityTransaction tx;

        em = cassandraEntityManagerFactory.createEntityManager();
        JpaRepositoryFactory factory = new JpaRepositoryFactory(em);
        tx = em.getTransaction();
        try {
            tx.begin();
            InventoryRepository repository = factory.getRepository(InventoryRepository.class);
            Inventory inventory = repository.findByName("My Inventory");
            if (inventory == null) {
                inventory = new Inventory("My Inventory");
            }
            log.debug("SpringData/JPA: " + inventory.toString());
            inventory.setDescription("This is my updated description.");
            tx.rollback();
        }
        catch (Exception e) {
            log.error(">> Exception in bulk delete of data", e);
            System.err.println("Error in bulk delete of data : " + e.getMessage());
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return this;
    }

    public Main b() {
        EntityManager em;
        EntityTransaction tx;

        // Add a person to MongoDB
        em = mongoEntityManagerFactory.createEntityManager();
        tx = em.getTransaction();
        try {
            tx.begin();
            Person person = new Person();
            person.setPersonFirstName("James");
            person.setPersonLastName("Bond");
            person.setAge(42);
            personId = person.getPersonId();
            em.merge(person);

            List<ObjectProvider> objs = ((JPAEntityManager) em).getExecutionContext().getObjectsToBeFlushed();
            for (Object o : objs) {
                log.debug("to be flushed: " + o.toString());
            }
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close(); // This will detach all current managed objects
        }
        return this;
    }

    public Main c() {
        EntityManager em;
        EntityTransaction tx;

        // Persistence of a Product and a Book.
        em = cassandraEntityManagerFactory.createEntityManager();
        tx = em.getTransaction();
        try {
            tx.begin();

            Inventory inv = em.merge(new Inventory("My Inventory"));
            inv.setDescription("This is my initial description.");
            Product product = new Product("Sony Discman", "A standard discman from Sony", 200.00);
            inv.addProduct(product);
            Book book = new Book("Lord of the Rings by Tolkien", "The classic story", 49.99, "JRR Tolkien",
                    "12345678", "MyBooks Factory");
            Magazine magazine = new Magazine("Field and Stream", "A hunter's guide to the outdoors.", 3.29, "F&S, Inc.",
                    "23984729347", "F&S, Inc.");

            //product.setSeller(person);
            //book.setSeller(person);
            //magazine.setSeller(person);
            inv.addProduct(book);
            inv.addProduct(magazine);
            em.persist(inv);

            tx.commit();
        }
        catch (Exception e) {
            log.error(">> Exception persisting data", e);
            System.err.println("Error persisting data : " + e.getMessage());
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        cassandraEntityManagerFactory.getCache().evictAll();
        log.debug("");
        return this;
    }

    public Main d() {
        EntityManager em;
        EntityTransaction tx;

        // Perform a retrieve of the Inventory and detach it (by closing the EM)
        em = cassandraEntityManagerFactory.createEntityManager();
        tx = em.getTransaction();
        Inventory inv = null;
        try {
            tx.begin();

            // Do a find() of the Inventory
            // Set the EntityGraph as something pulling in all Products
            // Note : you could achieve the same by either
            // 1). access the Inventory.products field before commit
            // 2). set fetch=EAGER for the Inventory.products field
            log.debug("Executing find() on Inventory");
            EntityGraph allGraph = em.getEntityGraph("allProps");
            Map hints = new HashMap();
            hints.put("javax.persistence.loadgraph", allGraph); // <- not yet supported, ignore this
            inv = em.find(Inventory.class, "My Inventory", hints);
            log.debug("Retrieved Inventory as " + inv);

            tx.commit();
        }
        catch (Exception e) {
            log.error(">> Exception performing find() on data", e);
            System.err.println("Error performing find() on data : " + e.getMessage());
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close(); // This will detach all current managed objects
        }
        for (Product prod : inv.getProducts()) {
            log.debug(">> After Detach : Inventory has a product=" + prod);
        }
        log.debug("");
        return this;
    }

    public Main e() {
        EntityManager em;
        EntityTransaction tx;

        // Update a person to MongoDB
        em = mongoEntityManagerFactory.createEntityManager();
        tx = em.getTransaction();
        try {
            tx.begin();
            if (personId == null) {
                tx.rollback();
            } else {
                Person person = em.find(Person.class, personId);
                person.setPersonLastName("Blunder");
                person.setAge(43);
                tx.commit();
            }
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close(); // This will detach all current managed objects
        }
        return this;
    }

    public Main f() {
        EntityManager em;
        EntityTransaction tx;

        // Perform some query operations
        em = cassandraEntityManagerFactory.createEntityManager();
        tx = em.getTransaction();
        try {
            tx.begin();
            log.debug("Executing Query for Products with price below 150.00");
            Query q = em.createQuery("SELECT p FROM Product p WHERE p.price < 150.00 ORDER BY p.price");
            List results = q.getResultList();
            Iterator iter = results.iterator();
            while (iter.hasNext()) {
                Object obj = iter.next();
                log.debug(">  " + obj);
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
            log.error(">> Exception querying data", e);
            System.err.println("Error querying data : " + e.getMessage());
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        log.debug("");
        return this;
    }

    public Main g() {
        EntityManager em;
        EntityTransaction tx;

        em = cassandraEntityManagerFactory.createEntityManager();
        JpaRepositoryFactory factory = new JpaRepositoryFactory(em);
        tx = em.getTransaction();
        try {
            tx.begin();
            InventoryRepository repository = factory.getRepository(InventoryRepository.class);
            Inventory inventory = repository.findByName("My Inventory");
            inventory.setDescription("This is the final description.");
            repository.save(inventory);
            tx.commit();
        }
        catch (Exception e) {
            log.error(">> Exception in bulk delete of data", e);
            System.err.println("Error in bulk delete of data : " + e.getMessage());
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return this;
    }

    public Main z() {
        EntityManager em;
        EntityTransaction tx;

        // Clean out the database
        cassandraEntityManagerFactory.getCache().evictAll();
        em = cassandraEntityManagerFactory.createEntityManager();
        tx = em.getTransaction();
        try {
            tx.begin();

            log.debug("Deleting all products from persistence");
            Inventory inv = (Inventory) em.find(Inventory.class, "My Inventory");

            log.debug("Clearing out Inventory");
            inv.clearProducts();
            em.flush();

            log.debug("Deleting Inventory");
            em.remove(inv);

            log.debug("Deleting all products from persistence");
            Query q = em.createQuery("SELECT p FROM Product p");
            List<Product> products = q.getResultList();
            int numDeleted = 0;
            for (Product prod : products) {
                em.remove(prod);
                numDeleted++;
            }
            log.debug("Deleted " + numDeleted + " products");

            tx.commit();
        }
        catch (Exception e) {
            log.error(">> Exception in bulk delete of data", e);
            System.err.println("Error in bulk delete of data : " + e.getMessage());
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return this;
    }
}
