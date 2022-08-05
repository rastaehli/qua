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

    /**
     * Convenience Functions for Creating Descriptions using repository default namespace
     */

    Description namedService(String name, Object obj);

    Description type(String type);

    Description typeAndProperties(String type, Map<String, Object> properties);

    Description typedPlan(String type, Description builder);

    Description typedPlan(String type, Description builder, Map<String, Object> dependencies);

    Description typedPlan(String type, Map<String, Object> properties,
                          Description builder, Map<String, Object> dependencies);

    Description typedService(String typeName, Object impl);
}
