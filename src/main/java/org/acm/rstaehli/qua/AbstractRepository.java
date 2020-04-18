package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractRepository implements Repository {

    Description firstOf(List<Description> list) throws NoImplementationFound {
        if (list == null || list.size() < 1) {
            throw new NoImplementationFound("empty impls in firstOf");
        }
        return list.get(0);
    }

    @Override
    public List<Description> implementationsMatching(Description desc) {
        if (desc.isActive()) {
            return Arrays.asList(desc);    // why would you even call if you already have the implementation?
        }
        List<Description> matches = new ArrayList<>();
        if (desc.name() != null) {
            addAllMatches(matches, implementationsByName(desc.name()), desc);
        }
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

    protected abstract Collection<Description> implementationsByName(String name);

    public Description implementationByName(String name) throws NoImplementationFound {
        Collection<Description> matches = implementationsByName(name);
        if (matches.size() < 1) {
            throw new NoImplementationFound("for name: " + name);
        }
        return firstOf((List<Description>) matches);
    }

    protected abstract Collection<Description> implementationsByType(String type);
}
