package org.acm.rstaehli.qua.tools;

import org.acm.rstaehli.qua.Description;

import java.util.HashMap;
import java.util.Map;

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

    public Description typedPlan(String type, Map<String,Object> properties,
                                 Description builder, Map<String,Object> dependencies) {
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
}
