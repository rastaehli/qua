package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Repository {
    public void advertise(Description impl);
    public void advertiseByName(String name, Description impl);
    List<Description> implementationsMatching(Description desc);
    Description implementationByName(String name) throws NoImplementationFound;
    Description bestMatch(Description desc) throws NoImplementationFound;

    Description description(String type, Map<String, Object> properties);
}
