package org.acm.rstaehli.qua;

import java.util.List;
import java.util.Map;

/**
 * Quality is the description of how close actual behaviour of a service comes
 * to the ideal: allowance for limited precision, cost, and delay.
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
 * in the @Behavior description.  All provision for implementing the
 * behavior with the required quality are part of the @Construction description.
 * All access to the service is via the @Interfaces description.
 */
public interface Quality {

    Quality setErrorDimensions(List<String> errorDimensions);  // dimensions for deviation from ideal

    List<String> getErrorDimensions();

    Quality setWeights(Map<String, Object> allowances);  // acceptable error values for each dimension

    Map<String, Object> getWeights();

    Quality setEstimateFunctions(Map<String, Object> estimateFunctions);

    Map<String, Object> getEstimateFunctions();

    Quality setRequiredUtility(Float requiredUtility);  // goal aggregate quality for input error estimates.

    Float getRequiredUtility();
    boolean equals(Quality other);

    Quality copy();

    Double utility(Description impl);

    boolean comparable(Quality other);
}
