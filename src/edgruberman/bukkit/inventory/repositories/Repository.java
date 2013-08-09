package edgruberman.bukkit.inventory.repositories;

public interface Repository<K extends Repository.Key, V> {

    /** @return true if an value exists for key */
    public boolean contains(K key);

    /** @return value for key; null if not found */
    public V get(K key);

    /** replaces existing value */
    public void put(K key, V value);

    /** delete value associated with key */
    public void remove(K key);

    /** prepare repository for garbage collection */
    public void destroy();

    /** key factory to generate based upon command parameter */
    public K createKey(String value);



    public interface Key {

        public static class StringKey implements Key {

            protected final String value;

            public StringKey(final String value) {
                this.value = value;
            }

            @Override
            public int hashCode() {
                return this.value.hashCode();
            }

            @Override
            public boolean equals(final Object obj) {
                return this.value.equals(obj);
            }

            @Override
            public String toString() {
                return this.value.toString();
            }

        }

    }

}
