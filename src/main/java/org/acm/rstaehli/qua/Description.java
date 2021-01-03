package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;
import org.apache.log4j.Logger;

import java.util.*;

import static org.acm.rstaehli.qua.Behavior.UNKNOWN_TYPE;

/**
 * Description is a meta object to reflect on and manage the implementation of a service.
 *
 * - type: names the logical behavior of the service
 * - properties: any additional attributes that restrict the type
 *      TODO: explain why properties are not simply part of the plan.  Is it
 *      that property names are an extension of the type and do not have to
 *      match plan dependency names?
 * - plan: how to build an implementation of the service.
 *
 * A service conforms to this description only if the type matches and it hasMatchingValue all the listed properties.
 * When a service is built, it can be accessed by its "service" property.
 */
public class Description implements Plan, Access {

    private static final Logger logger = Logger.getLogger(Description.class);

    protected Behavior behavior;
    protected Description builderDescription = null; // service to build type from dependencies
    protected Map<String, Object> dependencies = new HashMap<>();  // services needed by the builder
    protected Object serviceObject;  // the primary object interface of this description
    protected Map<String, String> interfaces;  // repository names of all interfaces
    protected int status = UNKNOWN;

    public static final int UNKNOWN = 0;  // don't know type yet
    public static final int TYPED = 1;  // know type behavior should conform to
    public static final int PLANNED = 2;  // know plan for how to build implementation
    public static final int PROVISIONED = 3; // know all dependencies needed for blueprint, recursively for all plans.
    public static final int ASSEMBLED = 4; // know interfaces for built component
    public static final int ACTIVE = 5; // know interfaces will behavior according to type

    public Description(Map<String, Object> jsonObject) {
        String type = getField(jsonObject, "type", UNKNOWN_TYPE);
        Map<String, Object> properties = getField(jsonObject, "properties", new HashMap<>());
        this.behavior = new BehaviorImpl(type, properties);
        this.builderDescription = getField(jsonObject, "builderDescription");
        this.dependencies = getField(jsonObject, "dependencies", new HashMap<>());
        this.serviceObject = getField(jsonObject, "serviceObject");
        this.interfaces = getField(jsonObject, "interfaces", new HashMap<>());
        computeStatus();
    }

    public Description() {
        this.behavior = new BehaviorImpl();
        this.dependencies = new HashMap<>();
    }

    public Description computeStatus() {
        if (serviceObject != null) {
            status = ACTIVE;  // if service hasMatchingValue been built
            return this;  // don't care if typed or planned
        }
        if (interfaces != null && !interfaces.isEmpty()) {
            status = ASSEMBLED;  // built and interfaces identified
            return this;  // don't care if typed or planned
        }
        if (behavior.type().equals(UNKNOWN_TYPE)) {
            status = UNKNOWN;
            return this;  // can't plan without type
        } else {
            status = TYPED;  // still need to check plan status
        }
        if (builderDescription != null && dependencies != null) {
            status = PLANNED;
            int leastDependencyStatus = PROVISIONED;  // default value if no dependencies
            for (Object o: dependencies.values()) {
                if (o instanceof Description) {
                    leastDependencyStatus = Integer.min(((Description) o).status, leastDependencyStatus);
                }
            }
            status = Integer.min(status, leastDependencyStatus);  // lower status if dependencies not provisioned
        }
        return this;
    }


    public Description(String type) {
        this.behavior.setType(type);
    }

    protected  <T> T getField(Map<String,Object> o, String fieldName) {
        return getField(o, fieldName, null);
    }

    /**
     * Get concrete Description field type from JsonObject map
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
                    value = mapSupportingNestedDescriptions((Map<String, Object>)value);
                } else {
                    value = new Description((Map<String, Object>)value);
                }
            }
            return (T)value;
        } else {
            return defaultValue;
        }
    }

    /**
     * return a properties or dependencies map, but inspect all key/value pairs to correctly deserialize nested Descriptions
     * @param map
     * @return
     */
    private Map<String, Object> mapSupportingNestedDescriptions(Map<String, Object> map) {
        for (String key: map.keySet()) {
            Object value = map.get(key);
            if (value instanceof Map) {  // restriction: all map values in properties or dependencies are Descriptions
                Description desc = new Description((Map<String,Object>)value);
                map.put(key, desc);
            } else if (value instanceof List) {
                List<Object> newList = new ArrayList();
                for (Object o: (List)value) {
                    if (o instanceof Map) {
                        newList.add( new Description((Map<String, Object>)o));
                    } else {
                        newList.add(o);
                    }
                }
                map.put(key, newList);  // replace old list with Descriptions from Maps
            }
        }
        return map;
    }

    public Description setName(String n) {
        this.behavior.setProperty("name",n);
        return this;
    }

    public Description setType(String t) {
        this.behavior.setType(t);
        return this;
    }

    public Behavior behavior() {
        return this.behavior;
    }

    public Description setProperties(Map<String, Object> p) {
        this.behavior.setProperties(p);
        return this;
    }

    @Override
    public Description setBuilderDescriptions(Description d) {
        builderDescription = d;
        return this;
    }

    @Override
    public Description setDependencies(Map<String, Object> d) {
        dependencies = d;
        return this;
    }

    public Description setProperty(String key, Object value) {
        this.behavior.setProperty(key, value);
        return this;
    }

    public Description setServiceObject(Object o) {
        serviceObject = o;
        return this;
    }

    public Description setInterfaces(Map<String, String> i) {
        interfaces = i;
        return this;
    }

    public Description setStatus(int s) {
        status = s;
        return this;
    }

    public String name() {
        return stringProperty("name");
    }

    public String type() {
        return this.behavior.type();
    }

    public Map<String, Object> properties() {
        return this.behavior.properties();
    }

    public Map<String, Object> dependencies() {
        return dependencies;
    }

    public boolean hasProperty(String key) {
        return this.behavior.hasProperty(key);
    }

    public String stringProperty(String key) {
        return (String)properties().get(key);
    }

    public long longProperty(String key) {
        return (long)properties().get(key);
    }

    public double doubleProperty(String key) {
        return (double)properties().get(key);
    }

    public List<Description> listDescriptionProperty(String key) {
        return (List<Description>)properties().get(key);
    }

    @Override
    public Object service() throws NoImplementationFound {
        return service(null);
    }

    @Override
    public Object service(Repository repo) throws NoImplementationFound {
        if (!isActive()) {
            this.activate(repo);
        }
        return serviceObject;
    }

    @Override
    public Map<String, String> interfaces() {
        return interfaces;
    }

    public Builder builder() throws NoImplementationFound {
        if (builderDescription == null) {
            throw new NoImplementationFound("for builderDescription");
        }
        return (Builder)builderDescription.service();
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

    /**
     * find implementations matching name or type/properties and set these attributes
     * on this description.
     * @param repo
     * @return
     * @throws NoImplementationFound
     */
    public Description plan(Repository repo) throws NoImplementationFound {
        if (isPlanned()) {
            return this;
        }
        Description impl = repo.bestMatch(this);
        if (impl == null) {
            logger.error("no implementation for type: " + this.behavior.type());
            throw new NoImplementationFound("for type: " + this.behavior.type());
        }

        copy(impl);

        if (builderDescription != null && !builderDescription.isPlanned()) {
            builderDescription.plan(repo);
        }
        planAll(behavior.properties().values(), repo);
        planAll(dependencies.values(), repo);

        computeStatus();
        return this;
    }

    public void planAll(Collection<Object> children, Repository repo) throws NoImplementationFound {
        for (Object o: children) {
            if (o instanceof Description) {
                Description d = (Description)o;
                if (!d.isPlanned()) {
                    d.plan(repo);
                }
            }
        }

    }

    // replace unknowns with values from given Description
    public Description copy(Description goal) {
        this.behavior.mergeBehavior(goal.behavior);
        if (this.builderDescription == null && goal.builderDescription != null) {
            this.builderDescription = goal.builderDescription;
        }
        if (this.serviceObject == null && goal.serviceObject != null) {
            this.serviceObject = goal.serviceObject;
        }
        if (this.dependencies == null && goal.dependencies != null) {
            this.dependencies = goal.dependencies;
        }
        Mappings.merge(goal.dependencies, this.dependencies);

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
            return plan(repo).provision(repo);
        }
        if (builderDescription != null && !builderDescription.isProvisioned()) {
            builderDescription.provision(repo);
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
            return provision(repo).assemble(repo);
        }
        if (builderDescription != null && !builderDescription.isAssembled()) {
            builderDescription.assemble(repo);
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
            return assemble(repo).activate(repo);
        }
        builder().start(this);
        status = ACTIVE;
        return this;
    }

    @Override
    public Description matchFor(Description goal) {
        Behavior behaviorMatch = this.behavior.specializeFor(goal.behavior());
        if (behaviorMatch == null) {
            return null;
        }
        Description copy = new Description().copy(this);
        copy.behavior = behaviorMatch;
        copy.computeStatus();   // may have changed from copied values
        return copy;
    }

}
