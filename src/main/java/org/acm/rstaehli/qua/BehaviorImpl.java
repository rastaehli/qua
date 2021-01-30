package org.acm.rstaehli.qua;

import org.apache.log4j.Logger;

import java.util.*;

/**
 * BehaviorImpl is a simple implementation of @Behavior interface.
 */
public class BehaviorImpl implements Behavior {

    public static final String MATCH_ANY = "http://org.acm.rstaehli.qua/model/build/MATCH_ANY";
    public static final Map<String, Object> ALL_PROPERTIES = new HashMap();  // signal to match any properties map
    {
        ALL_PROPERTIES.put("*","*");  // even when this map is copied/translated, these values signal ALL_PROPERTIES
    }

    private static final Logger logger = Logger.getLogger(BehaviorImpl.class);

    public static final Map<String, Object> ANY_PROPERTIES = new HashMap();  // signal to match any properties map
    {
        ANY_PROPERTIES.put("*","*");  // even when this map is copied/translated, these values signal ANY_PROPERTIES
    }

    protected String type;  // name of the behavior of the service
    protected Map<String, Object> properties;  // type variables (guaranteed by the builder)

    public BehaviorImpl(String type, Map<String, Object> properties) {
        this.type = type;
        this.properties = properties;
    }

    public BehaviorImpl() {
        this(UNKNOWN_TYPE, new HashMap<>());
    }

    public BehaviorImpl(String type) {
        this(type, new HashMap<>());
    }

    public BehaviorImpl setName(String n) {
        properties.put("name",n);
        return this;
    }

    @Override
    public BehaviorImpl setType(String t) {
        type = t;
        return this;
    }

    @Override
    public BehaviorImpl setProperties(Map<String, Object> p) {
        properties = p;
        return this;
    }

    @Override
    public BehaviorImpl setProperty(String key, Object value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(key, value);
        return this;
    }

    public String name() {
        return stringProperty("name");
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public Map<String, Object> properties() {
        return properties;
    }

    @Override
    public boolean hasProperty(String key) {
        if (properties == null) {
            return false;
        }
        return properties.containsKey(key);
    }

    @Override
    public Object getProperty(String key) {
        if (properties == null) {
            return null;
        }
        return properties.get(key);
    }

    @Override
    public String stringProperty(String key) {
        return (String)getProperty(key);
    }

    @Override
    public long longProperty(String key) {
        return (long)getProperty(key);
    }

    @Override
    public double doubleProperty(String key) {
        return (double)getProperty(key);
    }

    @Override
    public Description descriptionProperty(String key) {
        return null;
    }

    public boolean isTyped() {
        return type != UNKNOWN_TYPE;
    }

    @Override
    public BehaviorImpl mergeBehavior(Behavior behavior) {
        if ((this.type == null || this.type.equals(Behavior.UNKNOWN_TYPE)) && behavior.type() != null) {
            this.type = behavior.type();
        }
        if (behavior.properties() != null) {
            if (this.properties == null || this.properties == ANY_PROPERTIES) {
                this.properties = new HashMap<>();
            }
            Mappings.merge(behavior.properties(), this.properties);
        }
        return this;
    }

    @Override
    public Behavior specializeFor(Behavior goal) {
        // type must match
        if (!type.equals(goal.type())) {
            return null;
        }
        if (this.properties.equals(ANY_PROPERTIES)) { // builder promises to match all properties
            this.properties = goal.properties();  // so mergeBehavior the properties for the builder
            return this;
        }
        // must have all goal properties
        for (String name: goal.properties().keySet()) {
            if (this.hasProperty(name) && this.getProperty(name).equals(MATCH_ANY )) {
                // MATCH_ANY is a promise from an implementation to build with required property value
                this.properties.put(name, goal.properties().get(name));
            } else {
                Object match = match(this.getProperty(name), goal.properties().get(name));
                if (match == null) {
                    logger.debug("property " + name +
                            " value: " + this.getProperty(name) +
                            " does not match goal: " + goal.properties().get(name) + " for type: "+ type );
                    return null;
                }
                this.properties.put(name, match); // match may be mutation that conforms to goal
            }
        }
        removeObsoleteWildcards(this.properties);  // unmatched MATCH_ANY values
        return this;
    }

    private void removeObsoleteWildcards(Map<String, Object> map) {
        List<String> obsolete = new ArrayList();
        for (String key: map.keySet()) {
            if (map.get(key) == MATCH_ANY) {
                obsolete.add(key);
            }
        }
        for (String key: obsolete) {
            map.remove(key);
        }
    }

    protected Object match(Object property, Object requiredValue) {
        if (property == null ) {
            return null;
        }
        if (property instanceof String && property.equals(requiredValue)) {
            return property;
        }
        if (property instanceof Number && property.equals(requiredValue)) {
            return property;
        }
        if (property instanceof Description && requiredValue instanceof Description) {
            Description propertyDescription = (Description)property;
            Description requiredDescription = (Description)requiredValue;
            Description matched = propertyDescription.specializedFor(requiredDescription);
            if (matched != null){
                return matched;
            }
        }
        return null;
    }

    public boolean equals(Behavior other) {
        if (!(other instanceof BehaviorImpl)) {
            return false;
        }
        BehaviorImpl otherBehaviorImpl = (BehaviorImpl)other;
        if (!type.equals(otherBehaviorImpl.type())) {
            return false;
        }
        if (properties == null || otherBehaviorImpl.properties == null) {
            return this.properties == otherBehaviorImpl.properties;
        }
        if (!properties.equals(otherBehaviorImpl.properties)) {
            return false;
        }
        return true;
    }

    @Override
    public List<Description> descriptions() {
        List<Description> descriptions = new ArrayList<>();
        for (Object o: properties.values()) {
            if (o instanceof Description) {
                descriptions.add((Description) o);
            }
        }
        return descriptions;
    }

    public BehaviorImpl mutableCopy() {
        BehaviorImpl copy = new BehaviorImpl(type); // type is never mutable, so okay to copy
        copy.properties = new HashMap<>();
        Mappings.merge(properties, copy.properties());
        return copy;
    }
}
