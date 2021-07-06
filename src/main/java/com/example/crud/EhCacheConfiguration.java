package com.example.crud;

import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.cache.ehcache.EhCacheCacheManager;

@Configuration
public class EhCacheConfiguration {

    @Bean
    public EhCacheCacheManager ehCacheCacheManager() {
        return new EhCacheCacheManager(ehCacheManagerFactoryBean().getObject());
    }


    @Bean
    public EhCacheManagerFactoryBean ehCacheManagerFactoryBean() {
	EhCacheManagerFactoryBean cacheManagerFactoryBean = new EhCacheManagerFactoryBean();
        cacheManagerFactoryBean.setShared(true);
        return cacheManagerFactoryBean;
    }

}
