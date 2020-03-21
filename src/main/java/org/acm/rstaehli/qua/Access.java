package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

import java.util.Map;

public interface Access {
    Object service() throws NoImplementationFound;  // primary service interface (local object reference)

    Object service(Repository repo) throws NoImplementationFound;

    Map<String, String> interfaces();  // interface id lookup by role name
}
