package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * BehaviorImpl is a simple implementation of @Behavior interface.
 */
public class BehaviorImpl implements Behavior {

    private static final Logger logger = Logger.getLogger(BehaviorImpl.class);
    public static final Map<String, Object> ALL_PROPERTIES = new HashMap();  // signal to match any properties map
    {
        ALL_PROPERTIES.put("*","*");  // even when this map is copied/translated, these values signal ALL_PROPERTIES
    }


    protected String type;  // name of the behavior of the service
    protected Map<String, Object> properties;  // type variables (guaranteed by the builder)

    public BehaviorImpl(String type, Map<String, Object> properties) {
        this.type = type;
        this.properties = properties;
    }

    public BehaviorImpl() {
        this(null, new HashMap<>());
    }

    public BehaviorImpl(String type) {
        this(type, new HashMap<>());
    }

    public BehaviorImpl setName(String n) {
        properties.put("name",n);
        return this;
    }

    public BehaviorImpl setType(String t) {
        type = t;
        return this;
    }

    public BehaviorImpl setProperties(Map<String, Object> p) {
        properties = p;
        return this;
    }

    public BehaviorImpl setProperty(String key, Object value) {
        return null;
    }

    public String name() {
        return stringProperty("name");
    }

    public String type() {
        return type;
    }

    public Map<String, Object> properties() {
        return properties;
    }

    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    public String stringProperty(String key) {
        return (String)properties.get(key);
    }

    public long longProperty(String key) {
        return (long)properties.get(key);
    }

    public double doubleProperty(String key) {
        return (double)properties.get(key);
    }

    public boolean isTyped() {
        return type != null;
    }

    @Override
    // return copy merging values from the goal BehaviorImpl
    public BehaviorImpl copyFrom(Behavior goal) {
            if ((this.type == null || this.type.equals(Behavior.UNKNOWN_TYPE)) && goal.type() != null) {
                this.type = goal.type();
            }
            if (this.properties == null && goal.properties() != null) {
                this.properties = goal.properties();
            }
            Mappings.copyMappings(goal.properties(), this.properties);

            return this;
    }

    @Override
    public Behavior matchFor(Behavior goal) {
        // type must match
        if (!type.equals(goal.type())) {
            return null;
        }
        BehaviorImpl copy = new BehaviorImpl().copyFrom(this);
        if (copy.properties.equals(ALL_PROPERTIES)) { // builder promises to match all properties
            copy.properties = goal.properties();  // so copy the properties for the builder
            return copy;
        }
        // must have all goal properties
        for (String name: goal.properties().keySet()) {
            if (copy.hasProperty(name) && copy.properties.get(name) == Behavior.MATCH_ANY ) {
                // MATCH_ANY is a promise from the implementation to build with required property value
                copy.properties.put(name, goal.properties().get(name));
            } else {
                Object match = match(copy.properties.get(name), goal.properties().get(name));
                if (match == null) {
                    logger.debug("property " + name +
                            " value: " + copy.properties.get(name) +
                            " does not match goal: " + goal.properties().get(name) + " for type: "+ type );
                    return null;
                }
                copy.properties.put(name, match); // match may be mutation that conforms to goal
            }
        }
        removeObsoleteWildcards(copy.properties);  // unmatched MATCH_ANY values
        return copy;
    }

    private void removeObsoleteWildcards(Map<String, Object> map) {
        List<String> obsolete = new ArrayList();
        for (String key: map.keySet()) {
            if (map.get(key) == Behavior.MATCH_ANY) {
                obsolete.add(key);
            }
        }
        for (String key: obsolete) {
            map.remove(key);
        }
    }

    protected Object match(Object value1, Object value2) {
        if (value1 == null ) {
            return null;
        }
        if (value1 instanceof String && value1.equals(value2)) {
            return value1;
        }
        if (value1 instanceof Number && value1.equals(value2)) {
            return value1;
        }
        if (!(value1 instanceof BehaviorImpl)) {
            return null;  // we don't support any other types for a property
        }
        Description propertyDescription = (Description)value1;
        Description requiredDescription = (Description)value2;
        Description matched = propertyDescription.matchFor(requiredDescription);
        if (matched != null){
            return matched;
        }
        return null;
    }

}
