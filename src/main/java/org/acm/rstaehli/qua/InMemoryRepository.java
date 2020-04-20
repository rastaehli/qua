package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

import java.io.FileNotFoundException;
import java.util.*;

public class InMemoryRepository extends AbstractRepository {

    private Map<String, List<Description>> typeMap;  // support lookup by type
    private Map<String, List<Description>> nameMap;  // lookup by name

    public InMemoryRepository() {
        typeMap = new HashMap<>();
        nameMap = new HashMap<>();
    }

    /**
     * make an implementation available for service planning by putting in the typeMap.
     * @param impl : the description of the implementation.
     */
    public void advertise(Description impl) {
        addMapping(impl.type, impl, typeMap);
        addMapping(impl.name(), impl, nameMap);
    }

    public void addMapping(String key, Description value, Map<String,List<Description>> map) {
        if (key == null) {
            return;
        }
        // support lookup of implementations by type
        List<Description> list = map.get(key);
        if (list == null) {
            list = new ArrayList();
            map.put(key, list);
        }
        for (Description d: list) {
            if (d.equals(value)) {
                // TODO: add logging to warn value is already advertised
            }
        }
        list.add(value);  // TODO: make this modification thread safe
    }

    @Override
    protected Collection<Description> implementationsByName(String name) {
        List<Description> matches = nameMap.get(name);
        if (matches == null || matches.size() < 1) {
            return new ArrayList<>();
        }
        return matches;
    }

    @Override
    protected Collection<Description> implementationsByType(String type) {
        List<Description> matches = typeMap.get(type);
        if (matches == null || matches.size() < 1) {
            return new ArrayList<>();
        }
        return matches;
    }
}
