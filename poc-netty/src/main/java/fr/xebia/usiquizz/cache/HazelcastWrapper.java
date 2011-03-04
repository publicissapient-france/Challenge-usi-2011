package fr.xebia.usiquizz.cache;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;

import java.util.Map;

public class HazelcastWrapper<K, V> implements CacheWrapper<K, V> {

    private Hazelcast client;
    private Map<K, V> mapCache;

    public HazelcastWrapper() {

        mapCache = Hazelcast.getMap("cache");
    }

    @Override
    public void put(K key, V value) {
        mapCache.put(key, value);
    }

    @Override
    public V get(K key) {
        return mapCache.get(key);
    }
}
