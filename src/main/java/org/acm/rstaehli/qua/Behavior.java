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
    Behavior setProperties(Map<String, Object> p);  // required properties of this serviceObject
    Behavior setProperty(String key, Object value);  // set type property value

    String type();  // the name for the behavior
    Map<String, Object> properties();  // required properties of this instance of the type
    boolean hasProperty(String key);
    boolean isTyped();

    Behavior copyFrom(Behavior behavior);

    Behavior matchFor(Behavior goal);
}
