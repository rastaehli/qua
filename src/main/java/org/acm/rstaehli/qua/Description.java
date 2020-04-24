package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;
import org.apache.log4j.Logger;

import java.util.*;

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
public class Description implements Behavior, Plan, Access, Construction {

    private static final Logger logger = Logger.getLogger(Description.class);

    private static final String UNKNOWN_TYPE = "UNKNOWN_TYPE";
    public static final Object MATCH_ANY = "http://org.acm.rstaehli.qua/model/build/MATCH_ANY";
    public static final Map<String, Object> ALL_PROPERTIES = new HashMap();  // signal to match any properties map
    {
        ALL_PROPERTIES.put("*","*");  // even when this map is copied/translated, these values signal ALL_PROPERTIES
    }
    protected String type;  // name of the behavior of the service
    protected Map<String, Object> properties;  // type variables (guaranteed by the builder)
    protected Description builderDescription = null; // service to build type from dependencies
    protected Map<String, Object> dependencies = new HashMap<>();  // services needed by the builder
    protected Object serviceObject;  // the primary object interface of this description
    protected Map<String, String> interfaces;  // repositiory names of all interfaces
    protected int status = UNKNOWN;

    public static final int UNKNOWN = 0;
    public static final int TYPED = 1;
    public static final int PLANNED = 2;
    public static final int PROVISIONED = 3;
    public static final int ASSEMBLED = 4;
    public static final int ACTIVE = 5;

    public Description(Map<String, Object> jsonObject) {
        this.type = getField(jsonObject, "type", UNKNOWN_TYPE);
        this.properties = getField(jsonObject, "properties", new HashMap<>());
        this.builderDescription = getField(jsonObject, "builderDescription");
        this.dependencies = getField(jsonObject, "dependencies", new HashMap<>());
        this.serviceObject = getField(jsonObject, "serviceObject");
        this.interfaces = getField(jsonObject, "interfaces", new HashMap<>());
        computeStatus();
    }

    public Description() {
        this.properties = new HashMap<>();
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
        if (type.equals(UNKNOWN)) {
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
        this.type = type;
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

    @Override
    public Description setName(String n) {
        properties.put("name",n);
        return this;
    }

    // behavior
    @Override
    public Description setType(String t) {
        type = t;
        return this;
    }

    @Override
    public Description setProperties(Map<String, Object> p) {
        properties = p;
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

    @Override
    public Description setServiceObject(Object o) {
        serviceObject = o;
        return this;
    }

    @Override
    public Description setInterfaces(Map<String, String> i) {
        interfaces = i;
        return this;
    }

    @Override
    public Description setStatus(int s) {
        status = s;
        return this;
    }

    @Override
    public Description setProperty(String key, Object value) {
        return null;
    }

    public String name() {
        return stringProperty("name");
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

    public String stringProperty(String key) {
        return (String)properties.get(key);
    }

    public long longProperty(String key) {
        return (long)properties.get(key);
    }

    public double doubleProperty(String key) {
        return (double)properties.get(key);
    }

    public List<Description> listDescriptionProperty(String key) {
        return (List<Description>)properties.get(key);
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
            logger.error("no implementation for type: " + type);
            throw new NoImplementationFound("for type: " + type);
        }

        copyFrom(impl);

        if (builderDescription != null && !builderDescription.isPlanned()) {
            builderDescription.plan(repo);
        }
        planAll(properties.values(), repo);
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
    public Description copyFrom(Description impl) {
        if (this.name() == null && impl.name() != null) {
            this.setName(impl.name());
        }
        if ((this.type == null || this.type.equals(UNKNOWN_TYPE)) && impl.type != null) {
            this.type = impl.type;
        }
        if (this.builderDescription == null && impl.builderDescription != null) {
            this.builderDescription = impl.builderDescription;
        }
        if (this.serviceObject == null && impl.serviceObject != null) {
            this.serviceObject = impl.serviceObject;
        }
        if (this.properties == null && impl.properties != null) {
            this.properties = impl.properties;
        }
        if (this.dependencies == null && impl.dependencies != null) {
            this.dependencies = impl.dependencies;
        }
        copyMappings(impl.properties, this.properties);
        copyMappings(impl.dependencies, this.dependencies);

        return this;
    }

    public void copyMappings(Map<String,Object> source, Map<String, Object> target) {
        if (source == null) {
            return;
        }
        for (String key: source.keySet()) {
            if (!target.containsKey(key)) {
                target.put(key,source.get(key));
            }
        }
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
        // type must match
        if (!type.equals(goal.type())) {
            return null;
        }
        Description copy = new Description().copyFrom(this);
        if (copy.properties.equals(ALL_PROPERTIES)) { // builder promises to match all properties
            copy.properties = goal.properties;  // so copy the properties for the builder
            return copy;
        }
        // must have all goal properties
        for (String name: goal.properties().keySet()) {
            if (copy.hasProperty(name) && copy.properties.get(name) == MATCH_ANY ) {
                // MATCH_ANY is a promise from the implementation to build with required property value
                copy.properties.put(name, goal.properties().get(name));
            } else {
                Object match = match(copy.properties.get(name), goal.properties().get(name));
                if (match == null) {
                    logger.debug("property " + name +
                            " value: " + copy.properties.get(name) +
                            " does not match goal: " + goal.properties().get(name) + " for type: "+ type );
                    return null;
                }
                copy.properties.put(name, match); // match may be mutation that conforms to goal
            }
        }
        removeObsoleteWildcards(copy.properties);  // unmatched MATCH_ANY values
        return copy;
    }

    private void removeObsoleteWildcards(Map<String, Object> map) {
        List<String> obsolete = new ArrayList();
        for (String key: map.keySet()) {
            if (map.get(key) == MATCH_ANY) {
                obsolete.add(key);
            }
        }
        for (String key: obsolete) {
            map.remove(key);
        }
    }

    protected Object match(Object value1, Object value2) {
        if (value1 == null ) {
            return null;
        }
        if (value1 instanceof String && value1.equals(value2)) {
            return value1;
        }
        if (value1 instanceof Number && value1.equals(value2)) {
            return value1;
        }
        if (!(value1 instanceof Description)) {
            return null;  // we don't support any other types for a property
        }
        Description propertyDescription = (Description)value1;
        Description requiredDescription = (Description)value2;
        Description matched = propertyDescription.matchFor(requiredDescription);
        if (matched != null){
            return matched;
        }
        return null;
    }

}
