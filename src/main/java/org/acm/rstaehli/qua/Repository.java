package org.acm.rstaehli.qua;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Repository {

    private Map<String, List<Description>> typeMap;  // support lookup by type
    private Map<String, List<Description>> nameMap;  // lookup by name

    public Repository() {
        typeMap = new HashMap<>();
        nameMap = new HashMap<>();
        this.initializeRepository();
    }

    /**
     * bootstrap typeMap with initial set of implementations.
     */
    public void initializeRepository() {
        Description desc = new Description("type");
        advertise(desc);
    }

    /**
     * make an implementation available for service planning by putting in the typeMap.
     * @param impl : the description of the implementation.
     */
    public void advertise(Description impl) {
        addMapping(impl.type, impl, typeMap);
        addMapping(impl.name, impl, nameMap);
    }

    public void addMapping(String key, Description value, Map<String,List<Description>> map) {
        if (key == null) {
            return;
        }
        // support lookup of implementations by type
        List<Description> list = map.get(key);
        if (list == null) {
            list = new ArrayList();
            map.put(key, list);
        }
        for (Description d: list) {
            if (d.equals(value)) {
                throw new IllegalStateException("key is already in repository: " + key);
            }
        }
        list.add(value);  // TODO: make this modification thread safe
    }

    public Description lookupByName(String name) throws FileNotFoundException {
        List<Description> matches = nameMap.get(name);
        return bestOf(matches);
    }

    public Description implementationFor(String type) {
        List<Description> matches = typeMap.get(type);
        return bestOf(matches);
    }

    public Description implementationFor(String type, Map<String,Object> requiredProperties) {
        List<Description> matches = typeMap.get(type);
        if (requiredProperties.isEmpty()) {
            return bestOf(matches);
        }
        List<Description> matchesProperties = new ArrayList<>();
        for (Description m: matches) {
            if (hasAll(m.properties, requiredProperties)) {
                matchesProperties.add(m);
            }
        }
        return bestOf(matchesProperties);
    }

    private boolean hasAll( Map<String, Object> properties, Map<String, Object> requiredProperties) {
        List<String> propertyNameMatches = new ArrayList<>();
        for (String name: requiredProperties.keySet()) {
            if (has(properties, name, requiredProperties.get(name))) {
                propertyNameMatches.add(name);
            }
        }
        return propertyNameMatches.size() == properties.keySet().size();
    }

    private boolean has(Map<String, Object> properties, String propertyName, Object requiredValue) {
        Object property = properties.get(propertyName);
        if (property == null ) {
            return false;
        }
        if (property instanceof String && property.equals(requiredValue)) {
            return true;
        }
        // if not a String, property must be a Description of another component
        Description propertyDescription = (Description)property;
        Description requiredDescription = (Description)requiredValue;
        return conforms(propertyDescription, requiredDescription);
    }

    private boolean conforms(Description description, Description goal) {
        // type must match
        if (!description.type.equals(goal.type)) {
            return false;
        }
        // must have all goal properties
        if (!hasAll(description.properties, goal.properties)) {
            return false;
        }
        return true;
    }

    /**
     * return first of list since we have no better criteria
     * @param matches
     * @return
     */
    private Description bestOf(List<Description> matches) {
        if (matches.isEmpty()) {
            return null;
        }
        return matches.get(0);
    }

    public Builder realObjectFor(String serviceId) {
        return null; // TODO: lookup and instantiate implementation class from serviceId name
    }
}
