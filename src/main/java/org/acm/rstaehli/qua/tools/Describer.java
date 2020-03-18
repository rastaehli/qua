package org.acm.rstaehli.qua.tools;

import org.acm.rstaehli.qua.Description;

import java.util.HashMap;
import java.util.Map;

public class Describer {
    public Description namedService(String name, Object obj) {
        return new Description()
                .setName(name)
                .setServiceObject(obj)
                .computeStatus();
    }

    public Description typedPlan(String type, Map<String,Object> properties,
                                 Description builder, Map<String,Object> dependencies) {
        return new Description()
                .setType(type)
                .setProperties(properties)
                .setBuilderDescriptions(builder)
                .setDependencies(dependencies)
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
