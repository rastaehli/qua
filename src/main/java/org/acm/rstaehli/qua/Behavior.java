package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

import java.util.Map;

public interface Behavior {
    String type();
    Map<String, Object> properties();
    boolean hasProperty(String key);
    boolean satisfies(Behavior required);

    boolean isTyped();
    Description setType(String name);  // name the type of behavior required
    Description setProperty(String key, Object value);  // set type property value
    Description plan(Repository repo) throws NoImplementationFound;  // find implementation plan in repo
}
