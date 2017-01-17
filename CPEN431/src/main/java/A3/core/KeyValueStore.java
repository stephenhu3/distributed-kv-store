package A3.core;

import java.util.concurrent.ConcurrentHashMap;

public class KeyValueStore {
    ConcurrentHashMap map = new ConcurrentHashMap();
    /*
    1. put(key, value): Puts some value into the store.
       The value can be later retrieved using the key.
       If there is already a value corresponding to the key then the value is overwritten.

    2. get(key): Returns the value that is associated with the key.
       If there is no such key in your store, the store should return error - not found.

    3. remove(key): Removes the value that is associated with the key.
       If there is no such key the store should return error.
       Otherwise the value should be removed (get calls issued later for the same key should
       return error - not found).

    4. Plus a number of management messages defined below.
    */
}
