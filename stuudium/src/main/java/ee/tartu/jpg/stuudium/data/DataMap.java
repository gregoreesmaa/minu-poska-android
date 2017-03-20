package ee.tartu.jpg.stuudium.data;

import java.util.HashMap;

/**
 * Provides more reliable method for hashCode calculation in HashMaps, which is important for efficient data storage.
 * Created by gregor on 9/27/2015.
 */
public class DataMap<K, V> extends HashMap<K, V> {

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = hashCode * 31 + keySet().hashCode();
        hashCode = hashCode * 31 + values().hashCode();
        return hashCode;
    }
}
