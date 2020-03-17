package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

import java.util.HashMap;
import java.util.Map;

/**
 * Description is a meta object to reflect on and manage the implementation of a service.
 *
 * - type: names the logical behavior of the service
 * - properties: may contain additional attributes that restrict the type:
 * - plan: describes the implementation of the service.  It may also be executed to build the service.
 *
 * A service conforms to this description only if the type matches and it hasMatchingValue all the listed properties.
 * When a service is built, it can be accessed by its "service" property.
 */
public class Description implements Behavior, Plan, Access {

    private static final String UNKNOWN_TYPE = "UNKNOWN_TYPE";
    protected String name;
    public String type;  // name of the behavior of the service
    protected Map<String, Object> properties;  // type variables (guaranteed by the builder)
    protected Description builderDescription = null; // service to build type from dependencies
    public Map<String, Object> dependencies = new HashMap<>();  // services needed by the builder
    public Object serviceObject;  // the primary object interface of this description
    protected Map<String, String> interfaces;  // repositiory names of all interfaces
    protected int status = UNKNOWN;

    public static final int UNKNOWN = 0;
    public static final int TYPED = 1;
    public static final int PLANNED = 2;
    public static final int PROVISIONED = 3;
    public static final int ASSEMBLED = 4;
    public static final int ACTIVE = 5;

    public Description(Map<String, Object> jsonObject) {
        this.name = getField(jsonObject, "name");
        this.type = getField(jsonObject, "type", UNKNOWN_TYPE);
        this.properties = getField(jsonObject, "properties", new HashMap<>());
        this.builderDescription = getField(jsonObject, "builder");
        this.dependencies = getField(jsonObject, "dependencies", new HashMap<>());
        this.serviceObject = getField(jsonObject, "serviceObject");
        this.interfaces = getField(jsonObject, "interfaces", new HashMap<>());
        computeStatus();
    }

    public Description() {

    }

    protected void computeStatus() {
        if (serviceObject != null) {
            status = ACTIVE;  // if service hasMatchingValue been built
            return;  // don't care if typed or planned
        }
        if (!interfaces.isEmpty()) {
            status = ASSEMBLED;  // built and interfaces identified
            return;  // don't care if typed or planned
        }
        if (type.equals(UNKNOWN)) {
            status = UNKNOWN;
            return;  // can't plan without type
        } else {
            status = TYPED;  // still need to check plan status
        }
        if (builderDescription != null) {
            status = PLANNED;
        }
        int leastDependencyStatus = PROVISIONED;  // default value if no dependencies
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

    protected  <T> T getField(Map<String,Object> o, String fieldName) {
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
    protected <T> T getField(Map<String,Object> jsonObject, String fieldName, T defaultValue) {
        if (jsonObject.containsKey(fieldName)) {
            Object value = jsonObject.get(fieldName);
            if (value instanceof Map) {
                if (fieldName.equals("properties") || fieldName.equals("dependencies")) {
                    value = translateDescriptions((Map<String, Object>)value);
                } else {
                    value = new Description((Map<String, Object>)value);
                }
            }
            return (T)value;
        } else {
            return defaultValue;
        }
    }

    private Map<String, Object> translateDescriptions(Map<String, Object> map) {
        for (String key: map.keySet()) {
            Object value = map.get(key);
            if (value instanceof Map) {  // serialization assumes all maps are Description objects
                Map<String, Object> translatedValue = translateDescriptions((Map<String, Object>)value);
                Description desc = new Description(translatedValue);
                map.put(key, desc);
            }
        }
        return map;
    }

    public void inheritFrom(Description parent) {
        if (name == null & parent.name != null) {
            name = parent.name;
        }
        if (type == null || type.equals(UNKNOWN_TYPE) & parent.type != null) {
            type = parent.type;
        }
        if (builderDescription == null & parent.builderDescription != null) {
            builderDescription = parent.builderDescription;
        }
        if (serviceObject == null & parent.serviceObject != null) {
            serviceObject = parent.serviceObject;
        }
        for (String name: parent.properties.keySet()) {
            if (!properties.containsKey(name)) {
                properties.put(name, parent.properties.get(name));
            }
        }
        for (String name: parent.dependencies.keySet()) {
            if (!dependencies.containsKey(name)) {
                dependencies.put(name, parent.dependencies.get(name));
            }
        }
        computeStatus();
    }

    // behavior
    @Override
    public Description setType(String name) {
        return null;
    }

    @Override
    public Description setProperty(String key, Object value) {
        return null;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public Map<String, Object> properties() {
        return properties;
    }

    public Map<String, Object> dependencies() {
        return dependencies;
    }

    @Override
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    @Override
    public boolean satisfies(Behavior required) {
        // type must match
        if (!type.equals(required.type())) {
            return false;
        }
        // must have all goal properties
        for (String name: required.properties().keySet()) {
            if (!satisfies(properties.get(name), required.properties().get(name))) {
                return false;
            }
        }
        return true;
    }

    protected boolean satisfies(Object value1, Object value2) {
        if (value1 == null ) {
            return false;
        }
        if (value1 instanceof String && value1.equals(value2)) {
            return true;
        }
        if (value1 instanceof Number && value1.equals(value2)) {
            return true;
        }
        if (!(value1 instanceof Description)) {
            return false;  // we don't support any other types for a property
        }
        Description propertyDescription = (Description)value1;
        Description requiredDescription = (Description)value2;
        return propertyDescription.satisfies(requiredDescription);
    }


    // queries about implementation status


    public boolean isTyped() {
        return status >= TYPED;
    }

    public boolean isPlanned() {
        return status >= PLANNED;
    }

    @Override
    public boolean isProvisioned() {
        return status >= PROVISIONED;
    }

    @Override
    public boolean isAssembled() {
        return status >= ASSEMBLED;
    }

    @Override
    public boolean isActive() {
        return status >= ACTIVE;
    }

    // operations to change implementation status

    public Description plan() throws NoImplementationFound {
        return plan(null);
    }

    public Description plan(Repository repo) throws NoImplementationFound {
        if (isPlanned()) {
            return this;
        }
        Description impl = repo.implementationByType(type, properties);
        // copy impl state to this description
        builderDescription = impl.builderDescription;
        dependencies = impl.dependencies;
        status = impl.status;
        for (Object o: dependencies.values()) {
            if (o instanceof Description) {
                Description d = (Description)o;
                if (!d.isPlanned()) {
                    d.plan(repo);
                }
            }
        }
        status = PLANNED;
        return this;
    }

    public Description provision() throws NoImplementationFound {
        return provision(null);
    }

    /**
     * discover, provide and/or build all required dependencies
     */
    @Override
    public Description provision(Repository repo) throws NoImplementationFound {
        if (isProvisioned()) {
            return this;
        }
        if (!isPlanned()) {
            plan(repo);
        }
        for (Object o: dependencies.values()) {
            if (o instanceof Description) {
                Description d = (Description)o;
                if (!d.isProvisioned()) {
                    d.provision(repo);
                }
            }
        }
        status = PROVISIONED;
        return this;
    }

    @Override
    public Description assemble() throws NoImplementationFound {
        return assemble(null);
    }

    @Override
    public Description assemble(Repository repo) throws NoImplementationFound {
        if (isAssembled()) {
            return this;
        }
        if (!isProvisioned()) {
            provision(repo);
        }
        builder().assemble(this);
        status = ASSEMBLED;
        return this;
    }

    @Override
    public Description activate() throws NoImplementationFound {
        return activate(null);
    }

    @Override
    public Description activate(Repository repo) throws NoImplementationFound {
        if (isActive()) {
            return this;  // already assembled and active
        }
        if (!isAssembled()) {
            assemble(repo);
        }
        builder().start(this);
        status = ACTIVE;
        return this;
    }

    @Override
    public Object service() {
        return serviceObject;
    }

    @Override
    public Map<String, String> interfaces() {
        return interfaces;
    }

    public Builder builder() {
        return (Builder)builderDescription.service();
    }
}
