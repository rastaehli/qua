package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;
import java.util.Map;

public interface Repository {
    public void advertise(Description impl);
    Description lookupByName(String name) throws NoImplementationFound;
    Description implementationFor(String type) throws NoImplementationFound;
    Description implementationFor(String type, Map<String,Object> requiredProperties) throws NoImplementationFound;
}
