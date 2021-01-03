package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

import java.util.HashMap;
import java.util.Map;

/**
 * Interface for just the description of how a service behaves.
 */
public interface Behavior {

    public static final String UNKNOWN_TYPE = "UNKNOWN_TYPE";
    public static final Object MATCH_ANY = "http://org.acm.rstaehli.qua/model/build/MATCH_ANY";

    Behavior setType(String name);  // name the type of behavior required of serviceObject
    String type();  // the name for the behavior or UNKNOWN_TYPE
    boolean isTyped(); // type is not UNKNOWN_TYPE

    Behavior setProperties(Map<String, Object> p);  // required properties of this serviceObject
    Behavior setProperty(String key, Object value);  // set type property value
    Map<String, Object> properties();  // null or required properties of this instance of the type
    boolean hasProperty(String key);

    public Object getProperty(String key);
    // when you know the property type, use these to avoid typecast
    public String stringProperty(String key);
    public long longProperty(String key);
    public double doubleProperty(String key);
    public Description descriptionProperty(String key);

    // replace unknowns with specific values from behavior
    Behavior mergeBehavior(Behavior behavior);

    // replace wildcards with specific values from goal
    Behavior specializeFor(Behavior goal);

    boolean equals(Behavior other);
}
