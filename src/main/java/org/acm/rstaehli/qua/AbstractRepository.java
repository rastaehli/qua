package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;
import org.acm.rstaehli.qua.tools.Namespace;

import org.apache.log4j.Logger;

import java.util.*;

import static org.acm.rstaehli.qua.Behavior.UNKNOWN_TYPE;

public abstract class AbstractRepository implements Repository {

    protected static final Logger logger = Logger.getLogger(Description.class);

    private Namespace ns;

    public AbstractRepository(Map<String,String> namespaces) {
        this.ns = new Namespace(namespaces);
    }

    Description firstOf(List<Description> list) throws NoImplementationFound {
        if (list == null || list.size() < 1) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<Description> implementationsMatching(Description desc) {
        logger.info("searching " + this.toString() + " for matches: " + desc.toString());
        if (desc.isActive()) {
            return Arrays.asList(desc);    // why would you even call if you already have the implementation?
        }
        List<Description> matches = new ArrayList<>();
        if (desc.isTyped()) {
            addAllMatches(matches, implementationsByType(desc.type()), desc);
        }
        return matches;
    }

    public String toString() {
        return " repository: " + ns.toString();
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

    /** describing */

    @Override
    public Description namedService(String name, Object obj) {
        return new Description()
                .setName(ns.translate(name))
                .setServiceObject(obj)
                .computeStatus();
    }


    @Override
    public Description typedService(String typeName, Object impl) {
        return new Description()
                .setType(ns.translate(typeName))
                .setServiceObject(impl)
                .computeStatus();
    }

    @Override
    public Description namedOnly(String name) {
        return new Description()
                .setName(ns.translate(name))
                .setType(UNKNOWN_TYPE)
                .computeStatus();
    }

    @Override
    public Description typedPlan(String type, Map<String, Object> properties,
                                 Description builder, Map<String, Object> dependencies) {
        if (builder != null && dependencies == null) {
            dependencies = new HashMap<>();  // ensure builder and dependencies initialized together
        }
        return new Description()
                .setType(ns.translate(type))
                .setProperties(ns.translate(properties))
                .setConstruction(new ConstructionImpl(builder, ns.translate(dependencies)))
                .computeStatus();
    }

    @Override
    public Description type(String type) {
        return typedPlan(type, new HashMap<>(), null, null);
    }

    @Override
    public Description typeAndProperties(String type, Map<String, Object> properties) {
        return typedPlan(type, properties, null, null);
    }

    @Override
    public Description typeAndPlan(String type, Description builder, Map<String, Object> dependencies) {
        return typedPlan(type, new HashMap<>(), builder, dependencies);
    }

    @Override
    public Description typeAndPlan(String type, Description builder) {
        return typedPlan(type, new HashMap<>(), builder, new HashMap());
    }

}
