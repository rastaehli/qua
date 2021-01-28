package org.acm.rstaehli.qua;

import java.util.HashMap;
import java.util.Map;

/**
 * A utility class to support common methods for property and dependency maps.
 */
public class Mappings {

    /**
     * merge in properties from source missing in target
     * @param source
     * @param target
     */
    public static void merge(Map<String,Object> source, Map<String, Object> target) {
        if (source == null) {
            return;
        }
        for (String key: source.keySet()) {
            if (!target.containsKey(key)) {
                target.put(key,source.get(key));
            }
        }
    }

    public static Map<String, Object> mutableCopy(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        Map<String, Object> copy = new HashMap<>();
        for (String s: map.keySet()) {
            Object o = map.get(s);
            if (o instanceof Description) {
                copy.put(s, (Description)((Description) o).mutableCopy());
            } else {
                copy.put(s, o);
            }
        }
        return copy;
    }
}
