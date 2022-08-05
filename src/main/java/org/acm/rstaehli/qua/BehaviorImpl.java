package org.acm.rstaehli.qua;

import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Stream;

/**
 * BehaviorImpl is a simple implementation of @Behavior interface.
 */
public class BehaviorImpl implements Behavior {

    private static final Logger logger = Logger.getLogger(BehaviorImpl.class);

    public static final Map<String, Object> ANY_PROPERTIES = new HashMap();  // signal to match any properties map
    {
        ANY_PROPERTIES.put("*","*");  // even when this map is copied/translated, these values signal ANY_PROPERTIES
    }

    protected String type;  // QuA Name of the behavior of the service
    protected String name;  // QuA Name of a particular instance
    protected Map<String, Object> properties;  // object properties guaranteed by the builder
//    protected Map<String, Object> metaProperties;  // meta object properties guaranteed by the builder

    public BehaviorImpl(String type, String name, Map<String, Object> properties) {
        this.type = type;
        this.name = name;
        this.properties = properties;
    }

    public BehaviorImpl(String type, Map<String, Object> properties) {
        this(type, null, properties);
    }

    public BehaviorImpl() {
        this(UNKNOWN_TYPE, null, null);
    }

    public BehaviorImpl(String type) {
        this(type, null, null);
    }

    public static BehaviorImpl named(String name) {
        return new BehaviorImpl(null, name, null);
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
        BehaviorImpl specialized = new BehaviorImpl().mergeBehavior(this);
        if (ANY_PROPERTIES.equals(specialized.properties)) { // builder promises to match all properties
            specialized.properties = goal.properties();  // so mergeBehavior the properties for the builder
            logger.info("match by specializing: " + this.toString());
            return specialized;
        }
        // must have all goal properties
        for (String name: goal.properties().keySet()) {
            if (specialized.hasProperty(name) && specialized.getProperty(name) == Behavior.MATCH_ANY ) {
                // MATCH_ANY is a promise from the implementation to build with required property value
                specialized.properties.put(name, goal.properties().get(name));
            } else {
                Object match = match(specialized.getProperty(name), goal.properties().get(name));
                if (match == null) {
                    logger.debug("property " + name +
                            " value: " + toString(specialized.getProperty(name)) +
                            " does not match goal: " + goal.properties().get(name).toString() + " for type: "+ type );
                    return null;
                }
                specialized.properties.put(name, match); // match may be mutation that conforms to goal
            }
        }
        removeObsoleteWildcards(specialized.properties);  // unmatched MATCH_ANY values
        logger.info("match by specializing: " + this.toString());
        return specialized;
    }

    private String toString(Object o) {
        if (o instanceof Description) {
            Description d = (Description) o;
            return d.toString();
        } else {
            return o.toString();
        }
    }

    private void removeObsoleteWildcards(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return;
        }
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
        if (!(value1 instanceof Description)) {
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

}
