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

    public Description typedPlan(String type, Map<String,Object> properties,
                                 Description builder, Map<String,Object> dependencies, Object service) {
        if (builder != null && dependencies == null) {
            dependencies = new HashMap<>();  // ensure builder and dependencies initialized together
        }
        return new Description()
                .setType(ns.translate(type))
                .setProperties(ns.translate(properties))
                .setBuilderDescription(builder)
                .setDependencies(ns.translate(dependencies))
                .setServiceObject(service)
                .computeStatus();
    }

    public Description type(String type) {
        return typedPlan(type, new HashMap<>(), null, null, null);
    }

    public Description typeAndProperties(String type, Map<String,Object> properties) {
        return typedPlan(type, properties, null, null, null);
    }

    public Description typeAndPlan(String type, Description builder, Map<String,Object> dependencies) {
        return typedPlan(type, new HashMap<>(), builder, dependencies, null);
    }

    public Description typeAndPlan(String type, Description builder) {
        return typedPlan(type, new HashMap<>(), builder, new HashMap(), null);
    }

    public Description typedService(String type, Object service) {
        return typedPlan(type, new HashMap<>(), null, new HashMap(), service);
    }

    public String translate(String alias) {
        return ns.translate(alias);
    }
}
