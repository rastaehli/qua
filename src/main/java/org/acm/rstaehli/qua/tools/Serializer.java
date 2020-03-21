package org.acm.rstaehli.qua.tools;

import com.google.gson.Gson;
import org.acm.rstaehli.qua.Description;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.*;

/**
 * Description is a meta object to reflect on and manage the implementation of a service.
 *
 * - type: names the logical behavior of the service
 * - properties: may contain additional attributes that restrict the type:
 * - plan: describes the implementation of the service.  It may also be executed to build the service.
 *
 * A service conforms to this description only if the type matches and it has all the listed properties.
 * When a service is built, it can be accessed by its "service" property.
 */
public class Serializer {

    public Description descriptionFromJsonFile(String directoryPath, String name) throws FileNotFoundException {
        Map<String,Object> jsonMap = mapFromJsonFile(directoryPath, name);
        return new Description(jsonMap);
    }

    public Map<String,Object> mapFromJsonFile(String directoryPath, String name) throws FileNotFoundException {
        Map<String,Object> jsonMap = new Gson().fromJson(new FileReader(Paths.get(directoryPath + name + ".json").toFile()), Map.class);

        if (jsonMap.containsKey("parents")) {
            List<String> parentNames = (List<String>)jsonMap.get("parents");
            for (String parent: parentNames) {
                Map<String,Object> mapParent = mapFromJsonFile(directoryPath, parent);
                inheritFrom(mapParent, jsonMap);
            }
        }

        // Json file syntax allows "namespaceAliases" field to define aliases used to shorten names
        // Since these can be inherited, must first get parent JSON maps and collect alias definitions
        // so we can translateNames them wherever they occur.
        Map<String,String> namespaces = getNamespaces(jsonMap);
        translateNames(jsonMap, namespaces);
        return jsonMap;
    }

    private void inheritFrom(Map<String,Object> parent, Map<String, Object> child) {
        List<String> fields = Arrays.asList("name", "type", "builderDescription", "serviceObject");
        for (String fieldName: fields) {
            inherit(fieldName, parent, child);
        }
        List<String> mapFields = Arrays.asList("properties", "dependencies", "namespaces");
        for (String mapName: mapFields) {
            inheritMappings(mapName, parent, child);
        }
    }

    public void inherit(String key, Map<String,Object> parent, Map<String, Object> child) {
        if (!child.containsKey(key) && parent.containsKey(key)) {
            child.put(key, parent.get(key));
        }
    }

    public void inheritMappings(String mapName, Map<String,Object> parent, Map<String, Object> child) {
            if (parent.containsKey(mapName)) {
                Map<String,Object> parentMap = (Map<String,Object>)parent.get(mapName);
                if (!child.containsKey(mapName)) {
                    child.put(mapName, new HashMap<String, Object>());
                }
                Map<String,Object> childMap = (Map<String,Object>)child.get(mapName);
                for (String key: parentMap.keySet()) {
                    if (!childMap.containsKey(key)) {
                        childMap.put(key,parentMap.get(key));
                    }
                }
            }
    }

    private void translateNames(Map<String,Object> map, Map<String, String> namespaces) {
        for (String key: map.keySet()) {
            Object o = map.get(key);
            if (o instanceof String) {
                String t = translateNames((String)o, namespaces);
                if (t != null) {
                    map.put(key, t);
                }
            } else if (o instanceof Map<?,?>) {
                translateNames((Map<String,Object>)o, namespaces);
            } else if (o instanceof ArrayList<?>) {
                translateNames((ArrayList<Object>)o, namespaces);
            }
        }
    }

    private String translateNames(String value, Map<String, String> namespaces) {
        int aliasEnd = value.indexOf(':');
        if (aliasEnd > 0) {
            String alias = value.substring(0, aliasEnd);
            String translation = namespaces.get(alias);
            if (translation != null) {
                return translation + value.substring(aliasEnd + 1, value.length());
            }
        }
        return null;
    }

    private void translateNames(ArrayList<Object> list, Map<String, String> namespaces) {
        for (int i=0; i<list.size(); i++) {
            Object o = list.get(i);
            if (o instanceof String) {
                String t = translateNames((String)o, namespaces);
                if (t != null) {
                    list.set(i, t);
                }
            } else if (o instanceof Map<?,?>) {
                translateNames((Map<String,Object>)o, namespaces);
            } else if (o instanceof ArrayList<?>) {
                translateNames((ArrayList<Object>)o, namespaces);
            }
        }
    }

    private Map<String,String> getNamespaces(Map<String,Object> jsonObject) {
        if (jsonObject.containsKey("namespaces")) {
            Object value = jsonObject.get("namespaces");
            if (value instanceof Map) {
                return (Map<String,String>)value;
            }
        }
        return new HashMap<>();
    }
}
