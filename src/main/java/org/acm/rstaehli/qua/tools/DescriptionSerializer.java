package org.acm.rstaehli.qua.tools;

import com.google.gson.Gson;
import org.acm.rstaehli.qua.Description;
import org.apache.log4j.Logger;

import java.util.*;

import static org.acm.rstaehli.qua.Behavior.UNKNOWN_TYPE;
import static org.acm.rstaehli.qua.Description.PRIMARY_SERVICE_NAME;

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
public class DescriptionSerializer {
    private static final Logger logger = Logger.getLogger(DescriptionSerializer.class);

    public Description descriptionFromJson(String json) throws Exception {
        Map<String,Object> jsonMap = deserializeMap(json);
        return descriptionFromMap(jsonMap);
    }

    public Description descriptionFromMap(Map<String, Object> jsonObject) {
        String type = getField(jsonObject, "type", UNKNOWN_TYPE);
        Map<String, Object> properties = getField(jsonObject, "properties", new HashMap<>());
        Description d = new Description()
                .setType(type)
                .setProperties(properties);

        Description builderDescription = getField(jsonObject, "builderDescription");
        if (builderDescription != null) {
            Map<String, Object> dependencies = getField(jsonObject, "dependencies", new HashMap<>());
            d.setBuilder(builderDescription)
                    .setDependencies( dependencies);
        }

        Map<String, Object> interfaces = getField(jsonObject, "interfaces", new HashMap<>());
        for (String name: interfaces.keySet()) {
            d.setInterface(name, interfaces.get(name));
        }
        Object serviceObject = getField(jsonObject, PRIMARY_SERVICE_NAME);
        if (serviceObject != null) {
            d.setServiceObject(serviceObject);
        }
        d.computeStatus();
        return d;
    }


    protected  <T> T getField(Map<String,Object> o, String fieldName) {
        return getField(o, fieldName, null);
    }

    /**
     * Get concrete Description field type from Json object map
     * @param jsonObject
     * @param fieldName within jsonObject
     * @param defaultValue to return of field is not set
     * @param <T> return type of jsonObject value
     * @return
     */
    protected <T> T getField(Map<String,Object> jsonObject, String fieldName, T defaultValue) {
        if (jsonObject.containsKey(fieldName)) {
            Object value = jsonObject.get(fieldName);
            if (value instanceof Map) {
                if (fieldName.equals("properties") || fieldName.equals("dependencies")) {
                    value = mapSupportingNestedDescriptions((Map<String, Object>)value);
                } else {
                    value = descriptionFromMap((Map<String, Object>)value);
                }
            }
            return (T)value;
        } else {
            return defaultValue;
        }
    }

    /**
     * return a properties or dependencies map, but inspect all key/value pairs to correctly deserialize nested Descriptions
     * @param map
     * @return
     */
    private Map<String, Object> mapSupportingNestedDescriptions(Map<String, Object> map) {
        for (String key: map.keySet()) {
            Object value = map.get(key);
            if (value instanceof Map) {  // restriction: all map values in properties or dependencies are Descriptions
                Description desc = descriptionFromMap((Map<String,Object>)value);
                map.put(key, desc);
            } else if (value instanceof List) {
                List<Object> newList = new ArrayList();
                for (Object o: (List)value) {
                    if (o instanceof Map) {
                        newList.add( descriptionFromMap((Map<String, Object>)o));
                    } else {
                        newList.add(o);
                    }
                }
                map.put(key, newList);  // replace old list with Descriptions from Maps
            }
        }
        return map;
    }


    public Map<String,Object> deserializeMap(String json) throws Exception {
        try {
            return new Gson().fromJson(json, Map.class);
        } catch(Exception e) {
            throw new Exception("exception parsing json string: " + json);
        }
    }
}
