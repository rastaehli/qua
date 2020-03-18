package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

import java.util.Map;

/**
 * Interface for just the description of how a service behaves.
 */
public interface Behavior {
    String type();  // the name for the behavior
    Map<String, Object> properties();  // required properties of this instance of the type
    boolean hasProperty(String key);
    boolean satisfies(Behavior required);

    boolean isTyped();
    Description plan(Repository repo) throws NoImplementationFound;  // find implementation plan in repo
}
