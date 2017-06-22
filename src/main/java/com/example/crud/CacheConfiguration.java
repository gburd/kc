package com.example.crud;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
//@ComponentScan("com.example.crud")
//@PropertySource("application.properties")
@EnableCaching
public class CacheConfiguration extends CachingConfigurerSupport {

    //private final CacheProperties cacheProperties;

    //@Autowired
    //public CacheConfiguration(CacheProperties cacheProperties) {
//        this.cacheProperties = cacheProperties;
//    }

    @Qualifier("ehCacheCacheManager")
    @Autowired(required = false)
    private CacheManager ehCacheCacheManager;

    /*
    @Qualifier("redisCacheManager")
    @Autowired(required = false)
    private CacheManager redisCacheManager;
    */

    @Bean
    @Override
    public CacheManager cacheManager() {
//        if (cacheProperties.isEnabled()) {

            List<CacheManager> cacheManagers = Lists.newArrayList();

            if (this.ehCacheCacheManager != null) {
                cacheManagers.add(this.ehCacheCacheManager);
            }

            /*
            if (this.redisCacheManager != null) {
                cacheManagers.add(this.redisCacheManager);
            }
            */

            CompositeCacheManager cacheManager = new CompositeCacheManager();

            cacheManager.setCacheManagers(cacheManagers);
            cacheManager.setFallbackToNoOpCache(false);

            return cacheManager;
/*
        } else {
            SimpleCacheManager cacheManager = new SimpleCacheManager();
            List<ConcurrentMapCache> caches = cacheProperties.getCacheNameList()
                    .stream()
                    .map(cacheName -> new ConcurrentMapCache(cacheName))
                    .collect(Collectors.toList());
            cacheManager.setCaches(caches);
            return cacheManager;
        }
*/
    }

    @Bean
    @Override
    public CacheResolver cacheResolver() {
        return null;
    }

    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        /* Simplistic KeyGenerator example:
        return new KeyGenerator() {
            @Override
            public Object generate(Object o, Method method, Object... params) {
                StringBuilder sb = new StringBuilder();
                sb.append(o.getClass().getName());
                sb.append(method.getName());
                for (Object param : params) {
                    sb.append(param.toString());
                }
                return sb.toString();
            }
        };
        */
        // Same logic as the DefaultKeyGenerator
        return new KeyGenerator() {

            public static final int NO_PARAM_KEY = 0;
            public static final int NULL_PARAM_KEY = 53;

            public Object generate(Object target, Method method, Object... params) {
                if (params.length == 1) {
                    return (params[0] == null ? NULL_PARAM_KEY : params[0]);
                }
                if (params.length == 0) {
                    return NO_PARAM_KEY;
                }
                int hashCode = 17;
                for (Object object : params) {
                    hashCode = 31 * hashCode + (object == null ? NULL_PARAM_KEY : object.hashCode());
                }
                return Integer.valueOf(hashCode);
            }
        };
    }

    @Bean
    @Override
    public CacheErrorHandler errorHandler () {
        return null;
    }

}
