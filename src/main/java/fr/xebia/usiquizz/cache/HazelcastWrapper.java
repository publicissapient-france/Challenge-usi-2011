package fr.xebia.usiquizz.cache;

import com.hazelcast.client.HazelcastClient;

import java.util.Map;

public class HazelcastWrapper<K, V> implements CacheWrapper<K, V> {

    private HazelcastClient client;
    private Map<K, V> mapCache;

    public HazelcastWrapper() {
        client = HazelcastClient.newHazelcastClient("usi", "usi-pass", "usi");
        mapCache = client.getMap("cache");
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
