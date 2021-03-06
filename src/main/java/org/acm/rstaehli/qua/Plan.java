package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

import java.util.Map;

public interface Plan {
    boolean isProvisioned();
    boolean isAssembled();
    boolean isActive();

    Description setBuilderDescriptions(Description d);  // describe how serviceObject is built
    Description setDependencies(Map<String, Object> d);  // objects needed to build

    Description provision(Repository repo) throws NoImplementationFound;  // satisfy dependencies from advertised descriptions in repo
    Description assemble() throws NoImplementationFound;  // build with dependencies.  Fails if not provisioned
    Description assemble(Repository repo) throws NoImplementationFound;  // provision first, then assemble
    Description activate() throws NoImplementationFound;  // enable service for all components.  Fails if not assembled.
    Description activate(Repository repo) throws NoImplementationFound;  // assemble first, then enable all
    Description matchFor(Description goal);  // returns a mergeBehavior with required properties if possible, else null
}
