package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

import java.io.FileNotFoundException;
import java.util.*;

import static org.acm.rstaehli.qua.Description.ALL_PROPERTIES;

public class InMemoryRepository extends AbstractRepository {

    private Map<String, List<Description>> typeMap;  // support lookup by type
    private Map<String, List<Description>> nameMap;  // lookup by name

    public InMemoryRepository() {
        super();
        typeMap = new HashMap<>();
        nameMap = new HashMap<>();

        advertise(describe.typedPlan(
                "qua:list", ListBuilder.matchProperties(),
                describe.typedService("qua:builder", new ListBuilder(this)),
                null, null));

        advertise(describe.typedPlan(
                "qua:stringMap", ALL_PROPERTIES,
                describe.typedService("qua:builder", new StringMapBuilder()),
                null, null));
    }

    /**
     * make an implementation available for service planning by putting in the typeMap.
     * @param impl : the description of the implementation.
     */
    public void advertise(Description impl) {
        if (!impl.isPlanned()) {
            throw new IllegalStateException("attempt to advertise description with no implementation");
        }
        addMapping(impl.type, impl, typeMap);
    }

    public void advertiseByName(String name, Description impl) {
        addMapping(name, impl, nameMap);
        advertise(impl);    // also advertise by type
    }

    private boolean isNamed(Description impl) {
        return impl.name() != null && !impl.name().equals(Description.MATCH_ANY);
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
    public Description implementationByName(String name) throws NoImplementationFound {
        List<Description> matches = nameMap.get(name);
        if (matches == null || matches.size() < 1) {
            throw new NoImplementationFound("for name: " + name);
        }
        return matches.get(0);
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
