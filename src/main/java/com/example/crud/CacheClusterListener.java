package com.example.crud;

import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.annotation.TopologyChanged;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;
import org.infinispan.notifications.cachelistener.event.TopologyChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Listener(clustered = true)
public class CacheClusterListener {
    private Logger log = LoggerFactory.getLogger(getClass().getName());

    @CacheEntryCreated
    public void observeAdd(CacheEntryCreatedEvent<String, String> event) {
        if (event.isPre())
            return;

        log.info("Cache entry %s added in cache %s", event.getKey(), event.getCache());
    }

    @CacheEntryModified
    public void observeUpdate(CacheEntryModifiedEvent<String, String> event) {
        if (event.isPre())
            return;

        log.info("Cache entry %s = %s modified in cache %s", event.getKey(), event.getValue(), event.getCache());
    }

    @CacheEntryRemoved
    public void observeRemove(CacheEntryRemovedEvent<String, String> event) {
        if (event.isPre())
            return;

        log.info("Cache entry %s removed in cache %s", event.getKey(), event.getCache());
    }

    @TopologyChanged
    public void observeTopologyChange(TopologyChangedEvent<String, String> event) {
        if (event.isPre())
            return;

        log.info("Cache %s topology changed, new membership is %s", event.getCache().getName(), event.getConsistentHashAtEnd().getMembers());
    }

}
