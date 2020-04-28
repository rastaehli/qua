package org.acm.rstaehli.qua.tools;

import org.acm.rstaehli.qua.Description;

import java.util.HashMap;
import java.util.Map;

import static org.acm.rstaehli.qua.Description.UNKNOWN_TYPE;

/**
 * Provides convenience methods for constructing Description objects programatically.
 */
public class Describer {

    private Namespace ns;

    public Describer(Map<String,String> namespaces) {
        this.ns = new Namespace(namespaces);
    }

    public Description namedService(String name, Object obj) {
        return new Description()
                .setName(ns.translate(name))
                .setServiceObject(obj)
                .computeStatus();
    }

    public Description namedOnly(String name) {
        return new Description()
                .setName(ns.translate(name))
                .setType(UNKNOWN_TYPE)
                .computeStatus();
    }

    public Description typedPlan(String type, Map<String,Object> properties,
                                 Description builder, Map<String,Object> dependencies) {
        if (builder != null && dependencies == null) {
            dependencies = new HashMap<>();  // ensure builder and dependencies initialized together
        }
        return new Description()
                .setType(ns.translate(type))
                .setProperties(ns.translate(properties))
                .setBuilderDescriptions(builder)
                .setDependencies(ns.translate(dependencies))
                .computeStatus();
    }

    public Description type(String type) {
        return typedPlan(type, new HashMap<>(), null, null);
    }

    public Description typeAndProperties(String type, Map<String,Object> properties) {
        return typedPlan(type, properties, null, null);
    }

    public Description typeAndPlan(String type, Description builder, Map<String,Object> dependencies) {
        return typedPlan(type, new HashMap<>(), builder, dependencies);
    }

    public Description typeAndPlan(String type, Description builder) {
        return typedPlan(type, new HashMap<>(), builder, new HashMap());
    }
}
