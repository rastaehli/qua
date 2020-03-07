package org.acm.rstaehli.qua;

import java.util.HashMap;
import java.util.Map;

import static org.acm.rstaehli.qua.Plan.SERVICE_INTERFACE;

/**
 * Description is a meta object to reflect on and manage the implementation of a service.
 *
 * - type: names the logical behavior of the service
 * - properties: may contain additional attributes that restrict the type:
 * - plan: describes the implementation of the service.  It may also be executed to build the service.
 *
 * A service conforms to this description only if the type matches and it has all the listed properties.
 * When a service is built, it can be accessed by its "service" property.
 */
public class Description {

    public String name;
    public String type;  // name of the behavior of the service
    public Map<String, Object> properties;  // type variables (guaranteed by the builder)
    public Description builder = null; // service to build type from dependencies
    public Map<String, Object> dependencies = new HashMap<>();  // services needed by the builder
    public String serviceId;  // reference for the service implementation
    public int status = UNKNOWN;

    public static final int UNKNOWN = 0;
    public static final int TYPED = 1;
    public static final int PLANNED = 2;
    public static final int PROVISIONED = 3;
    public static final int ASSEMBLED = 4;
    public static final int ACTIVE = 5;

    public Description(Map<String, Object> jsonObject) {
        this.name = getField(jsonObject, "name");
        this.type = getField(jsonObject, "type");
        this.properties = getField(jsonObject, "properties", new HashMap<>());
        this.builder = getField(jsonObject, "builder");
        this.dependencies = getField(jsonObject, "dependencies", new HashMap<>());
        this.serviceId = getField(jsonObject, "serviceId");
        computeStatus();
    }

    private void computeStatus() {
        status = PLANNED;  // duh!  this IS the plan
        if (serviceId != null) {
            status = ACTIVE;  // if service has been built
            return;
        }
        int leastDependencyStatus = PROVISIONED;
        for (Object o: dependencies.values()) {
            if (o instanceof Description) {
                leastDependencyStatus = Integer.min(((Description) o).status, leastDependencyStatus);
            }
        }
        status = Integer.min(status, leastDependencyStatus);
    }


    public Description(String type) {
        this.type = type;
    }

    public <T> T getField(Map<String,Object> o, String fieldName) {
        return getField(o, fieldName, null);
    }

    /**
     * Get concrete type from JsonObject map
     * @param jsonObject
     * @param fieldName within jsonObject
     * @param defaultValue to return of field is not set
     * @param <T> return type of jsonObject value
     * @return
     */
    public <T> T getField(Map<String,Object> jsonObject, String fieldName, T defaultValue) {
        if (jsonObject.containsKey(fieldName)) {
            Object value = jsonObject.get(fieldName);
            if (value instanceof Map) {
                value = new Description((Map<String, Object>)value);
            }
            return (T)value;
        } else {
            return defaultValue;
        }
    }

    public static void copyMissingProperties(Description parent, Description desc) {
        if (desc.name == null & parent.name != null) {
            desc.name = parent.name;
        }
        if (desc.type == null & parent.type != null) {
            desc.type = parent.type;
        }
        if (desc.builder == null & parent.builder != null) {
            desc.builder = parent.builder;
        }
        if (desc.serviceId == null & parent.serviceId != null) {
            desc.serviceId = parent.serviceId;
        }
        for (String name: parent.properties.keySet()) {
            if (!desc.properties.containsKey(name)) {
                desc.properties.put(name, parent.properties.get(name));
            }
        }
        for (String name: parent.dependencies.keySet()) {
            if (!desc.dependencies.containsKey(name)) {
                desc.dependencies.put(name, parent.dependencies.get(name));
            }
        }
        desc.computeStatus();
    }

    // operations to change implementation status

    public Description plan() {
        if (builder == null) {
            Description plan = (new Repository()).implementationFor(type);
            copyMissingProperties(plan, this);
        }
        // ensure all dependencies are fully planned
        if (!plan.planned()) {
            plan.plan();
        }
        return this;
    }


    public void plan() {
        if (planned()) {
            return;
        }
        for (Object o: dependencies.values()) {
            if (o instanceof Description) {
                Description d = (Description)o;
                if (!d.planned()) {
                    d.plan();
                }
            }
        }
        status = PROVISIONED;
    }

    /**
     * discover, provide and/or build all required dependencies
     */
    public void provision() {
        if (provisioned()) {
            return;
        }
        if (!planned()) {
            plan();
        }
        for (Object o: dependencies.values()) {
            if (o instanceof Description) {
                Description d = (Description)o;
                if (!d.provisioned()) {
                    d.provision();
                }
            }
        }
        status = PROVISIONED;
    }

    public Description assemble() {
        return build();
    }

    private Description build(Repository r) {
        Builder realBuilder = r.realObjectFor(builder.serviceId);
        return this;
    }

    public void activate(Description d) {
        execute(d);  // current implementation does not distinguish assembled and active states
    }

    public void execute(Description d) {
        if (active()) {
            return;  // already assembled and active
        }
        if (!provisioned()) {
            provision();
        }
        Object service = ((Builder)builder.getService()).execute(blueprint, dependencies);
        addProperty(SERVICE_INTERFACE, service);
        status = ACTIVE;

    }

    public boolean active() {
        return status >= ACTIVE;
    }

    public boolean assembled() {
        return status >= ASSEMBLED;
    }

    public boolean provisioned() {
        return status >= PROVISIONED;
    }

    public boolean planned() {
        return status >= PLANNED;
    }

    public boolean typed() {
        return status >= TYPED;
    }
    public boolean unknown() {
        return status >= UNKNOWN;
    }

    public int getStatus() {
        return status;
    }
}
