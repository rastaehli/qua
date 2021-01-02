package org.acm.rstaehli.qua;

import java.util.Map;

public class Mappings {

    public static void copyMappings(Map<String,Object> source, Map<String, Object> target) {
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
