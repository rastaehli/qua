package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractRepository implements Repository {

    Description firstOf(List<Description> list) throws NoImplementationFound {
        if (list == null || list.size() < 1) {
            throw new NoImplementationFound("empty impls for type");
        }
        return list.get(0);
    }

   /**
     * Find impl from list with all required properties.
     * @param impls that match type name
     * @param requiredProperties to match to impl
     * @return first impl with all required properties
     * @throws NoImplementationFound
     */
    protected Description firstWithProperties(List<Description> impls, Map<String,Object> requiredProperties) throws NoImplementationFound {
        if (requiredProperties.isEmpty()) {
            return firstOf(impls);
        }
        for (Description impl: impls) {
            if (hasAll(impl.properties, requiredProperties)) {
                return impl;
            }
        }
        throw new NoImplementationFound("no impl with all required properties");
    }

    protected boolean hasAll( Map<String, Object> properties, Map<String, Object> requiredProperties) {
        List<String> propertyNameMatches = new ArrayList<>();
        for (String name: requiredProperties.keySet()) {
            if (has(properties, name, requiredProperties.get(name))) {
                propertyNameMatches.add(name);
            }
        }
        return propertyNameMatches.size() == properties.keySet().size();
    }

    protected boolean has(Map<String, Object> properties, String propertyName, Object requiredValue) {
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
        return propertyDescription.satisfies(requiredDescription);
    }
}
