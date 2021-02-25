package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

import java.util.*;

public abstract class AbstractRepository implements Repository {

    protected String repositoryName = "";

    public AbstractRepository(String prefix) {
        this.repositoryName = prefix;
    }

    @Override
    public String name() {
        return repositoryName;
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
            addAllMatches(matches, implementationsByType(desc.type()), desc);
        }
        return matches;
    }

    private void addAllMatches(List<Description> matches, Collection<Description> candidates, Description goal) {
        for (Description d: candidates) {
            Description match = d.specializedFor(goal);
            if (match != null) {
                matches.add(match);
            }
        }

    }
    @Override
    public Description bestMatch(Description desc) throws NoImplementationFound {
        return firstOf(implementationsMatching(desc));  // no other criteria for best
    }

    public abstract Description implementationByName(String name) throws NoImplementationFound;

}
