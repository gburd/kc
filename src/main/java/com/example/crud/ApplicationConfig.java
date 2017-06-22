package com.example.crud;

import com.example.crud.entities.AbstractAuditableEntity;
import com.example.crud.entities.AbstractEntity;
import com.example.crud.entities.Product;
import org.datanucleus.enhancer.DataNucleusEnhancer;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.springframework.cache.annotation.EnableCaching;
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
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.net.UnknownHostException;

@Configuration
@EnableJpaRepositories
@EnableJpaAuditing
@EnableScheduling
@EnableAspectJAutoProxy
@EnableCaching
@EnableTransactionManagement
class ApplicationConfig {

    @PostConstruct
    private void enhanceModelObjectBytecode() {
        DataNucleusEnhancer enhancer = new DataNucleusEnhancer("JPA", null);
        enhancer.setVerbose(true);
        enhancer.addClasses(AbstractEntity.class.getName());
        enhancer.addClasses(AbstractAuditableEntity.class.getName());
        enhancer.addClasses(Product.class.getName());
        enhancer.addPersistenceUnit("crud");
        enhancer.addPersistenceUnit("mongo");
        enhancer.enhance();
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

    @Bean
    public EmbeddedCacheManager cacheManager() {
        return infinispanEmbeddedDistributedCacheManager();
    }

    @Bean
    public AuditorAware<String> auditorAware() {
        return new UsernameAuditorAware();
    }

    private EmbeddedCacheManager infinispanEmbeddedDistributedCacheManager() {
        String nodeName = null;
        try {
            nodeName = java.net.InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            nodeName = "localhost";
        }
//        ConfigurationBuilder cb = new ConfigurationBuilder();	cb.addCluster("HighQCacheCluster").‌​addClusterNode("jbos‌​s1ind1", 11222).addClusterNode("udit.local.com", 11222); RemoteCacheManager rmc = new RemoteCacheManager(cb.build());

        DefaultCacheManager cacheManager = new DefaultCacheManager(
                GlobalConfigurationBuilder.defaultClusteredBuilder()
                        .transport().nodeName(nodeName).addProperty("configurationFile",
                        "jgroups-l2-cache-udp-largecluster.xml")
                        .build(),
                new ConfigurationBuilder()
                        .clustering()
                        .cacheMode(CacheMode.INVALIDATION_SYNC)
                        .build()
        );
        // The only way to get the "repl" cache to be exactly the same as the default cache is to not define it at all
        cacheManager.defineConfiguration("dist", new ConfigurationBuilder()
                .clustering()
                .cacheMode(CacheMode.DIST_SYNC)
                .hash().numOwners(2)
                .build()
        );
        return cacheManager;
    }

}
