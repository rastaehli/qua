package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.Builder;
import org.acm.rstaehli.qua.Description;

import java.util.HashMap;
import java.util.Map;


/**
 * Abstract factory for building a service from blueprint and dependency components.
 */
public class Plan {

    public static final int UNKNOWN = 0;
    public static final int TYPED = 1;
    public static final int PLANNED = 2;
    public static final int PROVISIONED = 3;
    public static final int ASSEMBLED = 4;
    public static final int ACTIVE = 5;

    public static final String SERVICE_INTERFACE = "service";

    private Description builder = null; // service able to execute this plan
    private Description blueprint = null;  // instructions for how to build
    private Map<String, Object> dependencies = new HashMap<>();  // services needed for the plan
    private int status = UNKNOWN;
    private Map<String, Object> properties = new HashMap<>();  // created by the builder

    public Plan(Map<String, Object> jsonObject) {
        this.builder = getBuilder(jsonObject);
        this.blueprint = getBlueprint(jsonObject);
        this.dependencies = getDependencies(jsonObject);
        this.properties = getProperties(jsonObject);
        computeStatus();
    }

    private void computeStatus() {
        status = PLANNED;  // duh!  this IS the plan
        if (properties.containsKey(SERVICE_INTERFACE)) {
            status = ACTIVE;  // if service has been built
            return;
        }
        int leastDependencyStatus = PROVISIONED;
        for (Object o: dependencies.values()) {
            if (o instanceof Description) {
                leastDependencyStatus = Integer.min(((Description) o).getStatus(), leastDependencyStatus);
            }
        }
        status = Integer.min(status, leastDependencyStatus);
    }

    private static Description getBlueprint(Map<String, Object> jsonObject) {
        if (jsonObject.containsKey("blueprint")) {
            return (Description)jsonObject.get("blueprint");
        } else {
            return new Description();
        }
    }

    private static Map<String, Object> getDependencies(Map<String, Object> jsonObject) {
        if (jsonObject.containsKey("dependencies")) {
            return (Map<String,Object>)jsonObject.get("dependencies");
        } else {
            return new HashMap<>();
        }
    }

    private static Map<String, Object> getProperties(Map<String, Object> jsonObject) {
        if (jsonObject.containsKey("properties")) {
            return (Map<String,Object>)jsonObject.get("properties");
        } else {
            return new HashMap<>();
        }
    }

    public Plan() {
    }

    private static Description getBuilder(Map<String, Object> m) {
        if (m.containsKey("builder")) {
            Object o = m.get("builder");
            if (o instanceof Map) {
                return new Description((Map<String,Object>)o);
            }
            throw new IllegalStateException("builder obsolete is not a map");
        } else {
            return null;
        }
    }

    /**
     * execute instructions in blueprint, consuming dependencies and ensuring properties
     */
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

    private void addProperty(String name, Object value) {
        properties.put(name, value);
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

    public void assemble(Description d) {
        execute(d);  // current implementation does not distinguish assembled and active states
    }
    public void activate(Description d) {
        execute(d);  // current implementation does not distinguish assembled and active states
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

    public Object getService() {
        if (!properties.containsKey(SERVICE_INTERFACE)) {
            throw new IllegalStateException("plan has not implemented the service yet");
        }
        return properties.get(SERVICE_INTERFACE);
    }

    public Map<String, Object> getDependencies() {
        return dependencies;
    }
}
