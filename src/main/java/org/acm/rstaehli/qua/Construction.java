package org.acm.rstaehli.qua;

import java.util.HashMap;
import java.util.Map;

public interface Construction {
    // all setters return the modified Description
    Description setName(String n);  // name this Description
    Description setType(String name);  // name the type of behavior required of serviceObject
    Description setProperties(Map<String, Object> p);  // required properties of this serviceObject
    Description setBuilderDescriptions(Description d);  // describe how serviceObject is built
    Description setDependencies(Map<String, Object> d);  // objects needed to build
    Description setServiceObject(Object o);  // set reference for the serviceObject
    Description setInterfaces(Map<String, String> i);  // service may have multiple interfaces for binding
    Description setStatus(int s);  // status of service build/implementation

    Description setProperty(String key, Object value);  // set type property value
}
