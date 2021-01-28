package org.acm.rstaehli.qua.tools;

import org.acm.rstaehli.qua.Behavior;

import java.util.ArrayList;
import java.util.Map;

/**
 * Support short aliases for long namespace prefixes ALA XML or Turtle RDF documents.
 * Example:
 *    declare a namespace in a JSON qua document:
 *      "namespaces": {
 *          "build": "http://org.acm.rstaehli.qua/model/build/"
 *      }
 *    reference this namespace alias in a name:
 *      "name": "build:Sort"
 *    When the JSON qua document is read, the name should be translated to:
 *      "name": "http://org.acm.rstaehli.qua/model/build/Sort"
 *
 * Use of namespaces avoids conflicts between a "Sort" defined by
 * different domains.
 */
public class Namespace {

    private Map<String, String> prefixMap;

    public Namespace(Map<String, String> map) {
        this.prefixMap = map;
    }

    public Namespace addMapping(String prefix, String fullName) {
        prefixMap.put(prefix, fullName);
        return this;
    }

    public Map<String, Object> translate(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        for (String key: map.keySet()) {
            Object o = map.get(key);
            if (o instanceof String) {
                String t = translate((String)o);
                if (t != o) {
                    map.put(key, t);
                }
                if (t.equals(Behavior.MATCH_ANY)) {
                    map.put(key, Behavior.MATCH_ANY);  // use same object ref
                }
            } else if (o instanceof Map<?,?>) {
                translate((Map<String,Object>)o);
            } else if (o instanceof ArrayList<?>) {
                translate((ArrayList<Object>)o);
            }
        }
        return map;
    }

    public String translate(String value) {
        int aliasEnd = value.indexOf(':');
        if (aliasEnd > 0) {
            String alias = value.substring(0, aliasEnd);
            String translation = prefixMap.get(alias);
            if (translation != null) {
                return translation + value.substring(aliasEnd + 1, value.length());
            }
        }
        return value;
    }

    public ArrayList<Object> translate(ArrayList<Object> list) {
        for (int i=0; i<list.size(); i++) {
            Object o = list.get(i);
            if (o instanceof String) {
                String t = translate((String)o);
                if (t != o) {
                    list.set(i, t);
                }
            } else if (o instanceof Map<?,?>) {
                translate((Map<String,Object>)o);
            } else if (o instanceof ArrayList<?>) {
                translate((ArrayList<Object>)o);
            }
        }
        return list;
    }

}
