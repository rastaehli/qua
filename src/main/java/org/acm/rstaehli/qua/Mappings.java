package org.acm.rstaehli.qua;

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

}
