package com.example.crud.entities;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import lombok.extern.java.Log;
import org.junit.*;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class CRUDTest.
 */
public class CRUDTest
{
    private Log log = LogFactory.getLog(getClass().getName());

    /** The Constant PU. */
    private static final String PU = "cassandra_pu";

    /** The emf. */
    private static EntityManagerFactory emf;

    /** The em. */
    private EntityManager em;

    /**
     * Sets the up before class.
     *
     * @throws Exception
     *             the exception
     */
    @BeforeClass
    public static void SetUpBeforeClass() throws Exception
    {
        Map propertyMap = new HashMap();
        //propertyMap.put(PersistenceProperties.KUNDERA_DDL_AUTO_PREPARE, "create");
        propertyMap.put("kundera.batch.size", "5");
        propertyMap.put(CassandraConstants.CQL_VERSION, CassandraConstants.CQL_VERSION_3_0);
        emf = Persistence.createEntityManagerFactory(PU, propertyMap);
    }

    /**
     * Sets the up.
     *
     * @throws Exception
     *             the exception
     */
    @Before
    public void setUp() throws Exception
    {
        em = emf.createEntityManager();
    }

    /**
     * Test crud operations.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testCRUDOperations() throws Exception
    {
        testInsert();
        testMerge();
        testUpdate();
        testCache();
        testTransaction();
        testBatch();
        testQueryBuilder();
        testRemove();
    }

    /**
     * Test insert.
     *
     * @throws Exception
     *             the exception
     */
    private void testInsert() throws Exception
    {
        Person p = new Person();
        p.setPersonId("101");
        p.setPersonFirstName("James");
        p.setPersonLastName("Bond");
        p.setAge(24);
        //p.addEmail("007@mi6.gov");
        em.persist(p);
        em.flush();

        Person person = em.find(Person.class, "101");
        Assert.assertNotNull(person);
        Assert.assertEquals("101", person.getPersonId());
        Assert.assertEquals("James Bond", person.getPersonName());
    }

    /**
     * Test merge.
     */
    private void testMerge()
    {
        Person person = em.find(Person.class, "101");
        person.setPersonLastName("Blond");
        //person.addEmail("jamesbond@gmail.com");
        person = em.merge(person);
        em.flush();

        Person p2 = em.find(Person.class, "101");
        Assert.assertEquals("Blond", p2.getPersonLastName());
    }

    private void testCache() {
        Cache cache = emf.getCache();
        cache.evictAll();
        log.info("Person in Cache: " + cache.contains(Person.class, "101"));
        Person person = em.find(Person.class, "101");
        log.info("Person in Cache: " + cache.contains(Person.class, person.getPersonId()));
        cache.evictAll();
        log.info("Person in Cache: " + cache.contains(Person.class, person.getPersonId()));
    }

    private void testUpdate()
    {
        Person person = em.find(Person.class, "101");
        /*
        // In Query set Paramater.
        queryString = "Update PersonCassandra p SET p.personName = 'Kuldeep' WHERE p.personId IN :idList";

        List<String> id = new ArrayList<String>();
        id.add("1");
        id.add("2");
        id.add("3");

        cqlQuery = parseAndCreateUpdateQuery(kunderaQuery, emf, em, pu, PersonCassandra.class, Integer.MAX_VALUE);
        KunderaQuery kunderaQuery = getQueryObject(queryString, emf);
        kunderaQuery.setParameter("idList", id);

        PersistenceDelegator pd = getPersistenceDelegator(em, getpd);
        EntityManagerFactoryImpl.KunderaMetadata kunderaMetadata = ((EntityManagerFactoryImpl) emf).getKunderaMetadataInstance();

        CassQuery query = new CassQuery(kunderaQuery, pd, kunderaMetadata);
        query.setMaxResults(maxResult);
        if(ttl != null)
        {
            query.applyTTL(ttl);
        }

        String cqlQuery = query.createUpdateQuery(kunderaQuery);
        return cqlQuery;
                */
        person.setPersonFirstName("Jim");
        em.flush();
    }

    private void testTransaction()
    {
        EntityTransaction txn = em.getTransaction();
        txn.begin();
        Person person = new Person();
        person.setPersonFirstName("Fred");
        person.setPersonLastName("Johnson");
        person.setAge(22);
        em.persist(person);
        txn.commit();
    }

    private void testBatch()
    {
    }

    private void testQueryBuilder()
    {
        String table = em.getMetamodel().entity(Person.class).getName();
        Select q = QueryBuilder.select().all().from(table);
        Query query = em.createQuery(q.getQueryString());
        query.getResultList();
    }

    /**
     * Test remove.
     */
    private void testRemove()
    {
        Person p = em.find(Person.class, "101");
        em.remove(p);

        Person p1 = em.find(Person.class, "101");
        Assert.assertNull(p1);
    }

    /**
     * Tear down.
     *
     * @throws Exception
     *             the exception
     */
    @After
    public void tearDown() throws Exception
    {
        em.close();
    }

    /**
     * Tear down after class.
     *
     * @throws Exception
     *             the exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
        if (emf != null)
        {
            emf.close();
            emf = null;
        }
    }
}
