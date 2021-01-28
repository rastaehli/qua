package org.acm.rstaehli.qua;

import sun.security.krb5.internal.crypto.Des;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Behavior is the description of externally visible behavior of a service in the ideal.
 *
 * A service is anything that does work for a client, not only clients that
 * call on a service interface, but a client that starts the service with the
 * expectation that the service will work autonomously via interfaces with other
 * services.
 *
 * The ideal is the assumption that the service can react instantly without
 * cost or loss of accuracy and precision.
 *
 * All allowance for limited
 * precision, cost, and delay are part of the @Quality description.  All
 * provision for implementing this behavior with the required quality are part
 * of the @Construction description.  All access to the service is via
 * the @Interfaces description.
 */
public interface Behavior {

    public static final String UNKNOWN_TYPE = "UNKNOWN_TYPE";

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

    List<Description> descriptions();  // Allow planning to access nested Description objects
}
