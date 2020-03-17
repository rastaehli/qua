package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

import java.util.List;
import java.util.Map;

public interface Repository {
    public void advertise(Description impl);
    Description implementationByName(String name) throws NoImplementationFound;
    Description implementationByType(String type) throws NoImplementationFound;
    Description implementationByType(String type, Map<String,Object> requiredProperties) throws NoImplementationFound;
    Description implementationMatching(Description desc) throws NoImplementationFound;
}
