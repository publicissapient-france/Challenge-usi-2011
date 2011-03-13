package fr.xebia.usiquizz.cache;

public interface CacheWrapper<K, V> {
    void put(K key, V value);

    V get(K key);
}
