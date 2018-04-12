package com.example.crud;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.example.crud.entities.AbstractAuditableEntity;
import com.example.crud.entities.AbstractEntity;
import com.example.crud.entities.Product;
import org.datanucleus.ExecutionContext;
import org.datanucleus.enhancer.DataNucleusEnhancer;
import org.datanucleus.store.StoreManager;
import org.datanucleus.store.connection.ConnectionManager;
import org.datanucleus.store.connection.ManagedConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

@Configuration
@EnableJpaRepositories
@EnableJpaAuditing
@EnableScheduling
@EnableAspectJAutoProxy
@EnableTransactionManagement
class ApplicationConfiguration {

    @PostConstruct
    private void enhanceModelObjectBytecode() {
        DataNucleusEnhancer enhancer = new DataNucleusEnhancer("JPA", null);
        enhancer.setVerbose(true)
                .addClasses(AbstractEntity.class.getName())
                .addClasses(AbstractAuditableEntity.class.getName())
                .addClasses(Product.class.getName())
                .addPersistenceUnit("crud")
                .addPersistenceUnit("mongo")
                .enhance();
    }

    @Bean
    public EntityManagerFactory entityManagerFactory() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("crud");
        return emf;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory());
        return txManager;
    }

    // Auditing
    @Bean
    public AuditorAware<String> auditorAware() {
        return new UsernameAuditorAware();
    }

    // Cassandra
    @Bean
    public EntityManager entityManager() {
        EntityManager em = entityManagerFactory().createEntityManager();
        return em;
    }

    @Bean
    public Session session() {
        StoreManager storeManager = ((ExecutionContext)entityManager().getDelegate()).getNucleusContext().getStoreManager();
        ConnectionManager connectionManager = storeManager.getConnectionManager();
        ManagedConnection connection = connectionManager.getConnection(-1);
        Session session = (Session) connection.getConnection();
        return session;
    }

    @Bean
    public Cluster cluster() {
        return session().getCluster();
    }

}
