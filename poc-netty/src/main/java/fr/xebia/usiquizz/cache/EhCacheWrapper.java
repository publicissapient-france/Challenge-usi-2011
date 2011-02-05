package fr.xebia.usiquizz.cache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

public class EhCacheWrapper<K, V> implements CacheWrapper<K,V> {
    private final String cacheName;
    private final CacheManager cacheManager;

    public EhCacheWrapper(final String cacheName, final CacheManager cacheManager)
    {
        this.cacheName = cacheName;
        this.cacheManager = cacheManager;
    }

    @Override
    public void put(final K key, final V value)
    {
        getCache().put(new Element(key, value));
    }

    @Override
    public V get(final K key)
    {
        Element element = getCache().get(key);
        if (element != null) {
            return (V) element.getValue();
        }
        return null;
    }

    public Ehcache getCache()
    {
        return cacheManager.getEhcache(cacheName);
    }
}
