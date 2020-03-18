package com.expd.tools;

import sun.misc.JavaIOFileDescriptorAccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * find implementation plans by searching repository for
 * advertised implementations with conforming type and properties.
 */
public class ServicePlanner {
    private Map<String, List<Description>> repository;

    public ServicePlanner() {
        repository = new HashMap<>();
    }

    /**
     * make an implementation available for service planning by putting in the repository.
     * @param impl : the description of the implementation.
     */
    public void advertise(Description impl) {
        // support lookup of implementations by type
        List<Description> list = repository.get(impl.getType());
        if (list == null) {
            list = new ArrayList();
            repository.put(impl.getType(), list);
        }
        for (Description d: list) {
            if (d.equals(impl)) {
                return;  // already in repository
            }
        }
        list.add(impl);  // TODO: make this modification thread safe
    }

    /**
     * bootstrap repository with initial set of implementations.
     */
    public void initializeRepository() {
        Plan p = new Plan();
        Description desc = new Description("type", new Plan());
        advertise(desc);
    }
    private Object build(String typeName) {
        return "http://com.expd.model/v0/build/" + typeName;
    }

    public Description implementationFor(String type) {
        List<Description> matches = repository.get(type);
        return bestOf(matches);
    }

    public Description implementationFor(String type, Map<String,Object> requiredProperties) {
        List<Description> matches = repository.get(type);
        if (requiredProperties.isEmpty()) {
            return bestOf(matches);
        }
        List<Description> matchesProperties = new ArrayList<>();
        for (Description m: matches) {
            if (hasAll(m.getProperties(), requiredProperties)) {
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
        if (!description.getType().equals(goal.getType())) {
            return false;
        }
        // must have all goal properties
        if (!hasAll(description.getProperties(), goal.getProperties())) {
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

}
