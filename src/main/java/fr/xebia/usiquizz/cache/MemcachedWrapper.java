package fr.xebia.usiquizz.cache;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.MemcachedClient;

import java.io.IOException;

public class MemcachedWrapper<K extends String, V> implements CacheWrapper<K, V> {

    private MemcachedClient client;

    public MemcachedWrapper() throws IOException {
        client = new MemcachedClient(new BinaryConnectionFactory(), AddrUtil.getAddresses("192.168.166.10:11211"));
    }

    @Override
    public void put(K key, V value) {
        client.add(key, 36000, value);
    }

    @Override
    public V get(K key) {
        return (V) client.get(key);
    }
}
