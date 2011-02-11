package fr.xebia.usiquizz.cache;


public class NoCacheWrapper<K, V> implements CacheWrapper<K, V> {

    @Override
    public void put(K key, V value) {

    }

    @Override
    public V get(K key) {
        return null;
        
    }
}
