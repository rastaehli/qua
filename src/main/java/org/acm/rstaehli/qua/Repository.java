package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

import java.util.List;
import java.util.Map;

public interface Repository {
    public void advertise(Description impl);
    List<Description> implementationsMatching(Description desc);
    Description implementationByName(String name) throws NoImplementationFound;
    Description bestMatch(Description desc) throws NoImplementationFound;
    List<Description> implementationsByType(String type);

    String name();  // repo repositoryName is prefix for all names defined within it
}
