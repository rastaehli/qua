package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

import java.util.List;
import java.util.Map;

/**
 * Construction is the description of how a service is built.
 *
 * A service is anything that does work for a client, not only clients that
 * call on a service interface, but a client that starts the service with the
 * expectation that the service will work autonomously via interfaces with other
 * services.
 *
 * The ideal is the assumption that the service can react instantly without
 * cost or loss of accuracy and precision.
 *
 * All service interface types and ideal interaction behavior is implied
 * in the @Behavior description.
 * All allowance for limited
 * precision, cost, and delay are part of the @Quality description.
 * All access to the service is via
 * the @Interfaces description.
 */
public interface Construction {

    Construction setBuilder(Description builder);
    Description builderDescription();
    Builder builder() throws NoImplementationFound;
    Construction setDependencies(Map<String, Object> d);  // required dependencies
    Construction setDependency(String key, Object value);  // set a dependency value or Description
    Map<String, Object> dependencies();  // null or required dependencies
    boolean equals(Construction other);
    void mergeConstruction(Construction goal);
    List<Description> descriptions();
}
