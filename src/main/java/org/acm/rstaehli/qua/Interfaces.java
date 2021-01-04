package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

import java.util.Map;

/**
 * Interfaces is the description of how a service is accessed.
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
 * All support for building or tearing down the service is via
 * the @Construction description.
 */
public interface Interfaces {

    Interfaces setInterfaces(Map<String, String> interfaces);  // access URIs by interface name
    Interfaces setDependency(String uri, String value);  // set a URI value
    Map<String, String> interfaces();  // null or map of interface URIs by name
    String getURI(String name); // return URI for name
    public Object service() throws NoImplementationFound; // return primary interface
    public Object service(Repository repo) throws NoImplementationFound; // return primary interface for the given repository
}
