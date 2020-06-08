package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;
import org.acm.rstaehli.qua.tools.Describer;

import java.util.*;

import static org.acm.rstaehli.qua.Description.ALL_PROPERTIES;

public abstract class AbstractRepository implements Repository {

    public Map<String,String> namespaces;
    public Describer describe;

    public AbstractRepository() {

        // component descriptions require RDF names but we can use short aliases for long prefixes to make
        // serializations more readable.
        namespaces = new HashMap<>();
        namespaces.put("qua", "http://org.acm.rstaehli/qua/");
        describe = new Describer(namespaces);
    }

    Description firstOf(List<Description> list) throws NoImplementationFound {
        if (list == null || list.size() < 1) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<Description> implementationsMatching(Description desc) {
        if (desc.isActive()) {
            return Arrays.asList(desc);    // why would you even call if you already have the implementation?
        }
        List<Description> matches = new ArrayList<>();
        if (desc.isTyped()) {
            addAllMatches(matches, implementationsByType(desc.type), desc);
        }
        return matches;
    }

    private void addAllMatches(List<Description> matches, Collection<Description> candidates, Description goal) {
        for (Description d: candidates) {
            Description match = d.matchFor(goal);
            if (match != null) {
                matches.add(match);
            }
        }

    }
    @Override
    public Description bestMatch(Description desc) throws NoImplementationFound {
        return firstOf(implementationsMatching(desc));  // no other criteria for best
    }

    protected abstract Collection<Description> implementationsByType(String type);

    @Override
    public Description description(String type, Map<String, Object> properties) {
        return describe.typeAndProperties(type, properties);
    }

    public String name(String aliasedName) {
        return describe.namedOnly(aliasedName).name();
    }

}
