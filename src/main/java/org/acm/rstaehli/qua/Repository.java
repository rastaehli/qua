package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

import java.util.List;
import java.util.Map;

public interface Repository {

    // store a Description of an implementation
    public void advertise(Description impl);

    // return all implementations satisfying the described requirements
    List<Description> implementationsMatching(Description desc);

    // return just the implementation with the given name
    Description implementationByName(String name) throws NoImplementationFound;

    // return one (or null) implementation best satisfying the described requirements
    Description bestMatch(Description desc) throws NoImplementationFound;

    // ******* Creating Descriptions *********

    // describe this object with only a name
    Description namedService(String name, Object obj);

    //
    Description namedOnly(String name);

    Description typedPlan(String type, Map<String, Object> properties,
                          Description builder, Map<String, Object> dependencies);

    Description type(String type);

    Description typeAndProperties(String type, Map<String, Object> properties);

    Description typeAndPlan(String type, Description builder, Map<String, Object> dependencies);

    Description typeAndPlan(String type, Description builder);

    Description typedService(String typeName, Object impl);
}
