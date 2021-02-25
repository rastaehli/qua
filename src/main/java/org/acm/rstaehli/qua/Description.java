package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;
import org.apache.log4j.Logger;

import java.util.*;

import static org.acm.rstaehli.qua.Behavior.UNKNOWN_TYPE;
import static org.acm.rstaehli.qua.Lifecycle.*;
import static org.acm.rstaehli.qua.Qua.QUA_NAME;

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
public class Description {

    private static final Logger logger = Logger.getLogger(Description.class);

    protected BehaviorImpl behavior;
    protected QualityImpl quality;
    protected ConstructionImpl construction;
    private Map<String, Object> interfaces;
    public static final String PRIMARY_SERVICE_NAME = "serviceObject"; // unique key for primary service interface

//    protected Description builderDescription = null; // service to build type from dependencies
//    protected Map<String, Object> dependencies = new HashMap<>();  // services needed by the builder
//    protected Object serviceObject;  // the primary object interface of this description
//    protected Map<String, String> interfaces;  // repository names of all interfaces
    protected Lifecycle status = UNKNOWN;

    public Description() {
        this.behavior = new BehaviorImpl();
        this.construction = null;
    }

    public Description computeStatus() {
        if (service() != null) {
            status = ACTIVE;  // construction sets primary service only when active/ready
            return this;
        }
        if (interfaces != null && !interfaces.isEmpty()) {
            status = ASSEMBLED;  // built and interfaces identified
            return this;  // don't care if typed or planned
        }
        // next we want to know if it is constructed, but state of
        // construction is meaningless without type, so check that first
        if (behavior.type().equals(UNKNOWN_TYPE)) {
            status = UNKNOWN;
            return this;  // can't plan without type
        } else {
            status = TYPED;  // still need to check construction status
        }

        if (construction != null) {
            status = PROVISIONED;  // default value if no dependencies
            for (Description d: construction.descriptions()) {
                if (!d.isProvisioned()) {
                    status = PLANNED;   // construction plan exists but need some dependency
                }
            }
        }

        return this;
    }


    public Description(String type) {
        this.behavior = new BehaviorImpl(type);
        computeStatus();
    }

    public Description setName(String n) {
        behavior.setProperty(QUA_NAME,n);
        return this;
    }

    public Description setType(String t) {
        if (behavior == null) {
            behavior = new BehaviorImpl(t);
        } else {
            behavior.setType(t);
        }
        computeStatus();
        return this;
    }

    public Behavior behavior() {
        return this.behavior;
    }

    public Description setProperties(Map<String, Object> p) {
        this.behavior.setProperties(p);
        return this;
    }

    public Description setProperty(String key, Object value) {
        this.behavior.setProperty(key, value);
        return this;
    }

    public String name() {
        return stringProperty(QUA_NAME);
    }

    public String type() {
        return this.behavior.type();
    }

    public Map<String, Object> properties() {
        return this.behavior.properties();
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

    // return expected property type
    public <T> T property(String key, Class<T> classOfT) {
        if (!properties().containsKey(key)) {
            throw new IllegalArgumentException("properties missing: " + key);
        }
        return classOfT.cast(properties().get(key));
    }

    public Description descriptionProperty(String key) {
        return (Description)properties().get(key);
    }

    public Description setConstruction(Construction c) {
        construction = (ConstructionImpl) c;
        computeStatus();
        return this;
    }

    // assumes no repository needed, as in service already active
    public Object service() {
        return getInterface(PRIMARY_SERVICE_NAME);
    }

    public Object service(Qua qua) throws NoImplementationFound {
        if (!isActive()) {
            this.activate(qua);
        }
        return getInterface(PRIMARY_SERVICE_NAME);
    }


    // queries about implementation status

    public boolean isTyped() {
        return status.ordinal() >= TYPED.ordinal();
    }

    public boolean isPlanned() {
        return status.ordinal() >= PLANNED.ordinal();
    }

    public boolean isProvisioned() {
        return status.ordinal() >= PROVISIONED.ordinal();
    }

    public boolean isAssembled() {
        return status.ordinal() >= ASSEMBLED.ordinal();
    }

    public boolean isActive() {
        return status.ordinal() >= ACTIVE.ordinal();
    }

    // operations to change implementation status

    public Description plan() throws NoImplementationFound {
        return plan(null);
    }

    /**
     * find implementations matching repositoryName or behavior and set these attributes.
     * on this description.
     * @param qua has implementation descriptions that may provide the needed plan.
     * @return this Description updated with construction plan.
     * @throws NoImplementationFound
     */
    public Description plan(Qua qua) throws NoImplementationFound {
        if (isPlanned()) {
            return this;
        }
        Description impl = null;
        if (this.name() != null) {
            impl = repository(qua).implementationByName(this.name());
        } else {
            impl = repository(qua).bestMatch(this);
        }
        if (impl == null) {
            logger.error("no implementation for type: " + this.behavior.type());
            throw new NoImplementationFound("for type: " + this.behavior.type());
        }

        behavior.mergeBehavior(impl.behavior);
        construction = (ConstructionImpl) (new ConstructionImpl()).mergeConstruction(impl.construction);
        interfaces = impl.interfaces;

        computeStatus();
        return this;
    }

    private Repository repository(Qua qua) {
        if (qua == null) {
            throw new IllegalStateException("cannot plan with null Qua context");
        }
        if (qua.repository() == null) {
            throw new IllegalStateException("cannot plan with null repository in Qua context");
        }
        return qua.repository();
    }

    public Description provision() throws NoImplementationFound {
        return provision(null);
    }

    /**
     * discover, provide and/or build all required dependencies
     */
    public Description provision(Qua qua) throws NoImplementationFound {
        if (isProvisioned()) {
            return this;
        }
        if (!isPlanned()) {
            return plan(qua).provision(qua);
        }
        for (Description d: childDescriptions()) {
            d.provision(qua);
        };
        status = PROVISIONED;
        return this;
    }

    private List<Description> childDescriptions() {
        List<Description> descriptions = behavior.descriptions();
        descriptions.addAll(construction.descriptions());
        return descriptions;
    }

    public Description assemble() throws NoImplementationFound {
        return assemble(null);
    }

    public Description assemble(Qua qua) throws NoImplementationFound {
        if (isAssembled()) {
            return this;
        }
        if (!isProvisioned()) {
            return provision(qua).assemble(qua);
        }
        for (Description d: childDescriptions()) {
            if (!d.isAssembled()) {
                d.assemble();
            }
        }
        construction.builder().assemble(this);
        status = ASSEMBLED;
        return this;
    }

    public Description disAssemble() {
        interfaces().clear();
        computeStatus();
        return this;
    }

    public Description activate() throws NoImplementationFound {
        return activate(null);
    }

    public Description activate(Qua qua) throws NoImplementationFound {
        if (isActive()) {
            return this;  // already assembled and active
        }
        if (!isAssembled()) {
            return assemble(qua).activate(qua);
        }
        construction.builder().start(this);
        status = ACTIVE;
        return this;
    }

    /**
     * Attempt to create a copy of this specialized to match the goal.
     *
     * @param goal describes behavior iwe are trying to match
     * @return null or a specialized copy matching goal
     */
    public Description specializedFor(Description goal) {
        Description copy = mutableCopy();
        Behavior specializedBehavior = copy.behavior.specializeFor(goal.behavior());
        if (specializedBehavior == null) {
            return null;
        } else {
            copy.computeStatus();   // may have changed from copied values
            return copy;
        }
    }

    public Map<String, Object> dependencies() {
        return construction.dependencies();
    }

    public Map<String, Object> interfaces() {
        return this.interfaces;
    }

    public Object getInterface(String name) {
        if (interfaces == null) {
            interfaces = new HashMap();
            return null;
        }
        return this.interfaces.get(name);
    }

    public Description setInterface(String name, Object value) {
        if (interfaces == null) {
            interfaces = new HashMap();
        }
        this.interfaces.put(name, value);
        computeStatus();
        return this;
    }

    public Description setServiceObject(Object obj) {
        setInterface(PRIMARY_SERVICE_NAME, obj);
        return this;
    }

    public Description mutableCopy() {
        Description copy = new Description();
        copy.behavior = behavior.mutableCopy();
        copy.construction = construction == null
                ? null
                : construction.mutableCopy();
        copy.quality = quality == null
                ? null
                : quality.mutableCopy();
        if (interfaces == null) {
            copy.interfaces = null;
        } else {
            copy.interfaces = new HashMap<>();
            Mappings.merge(interfaces, copy.interfaces);
        }
        copy.computeStatus();
        return copy;
    }

    public String status() {
        return status.toString();
    }

    public Object dependency(String x) {
        return construction.dependency(x);
    }

    public Description setBuilder(Description builderDescription) {
        if (construction == null) {
            construction = new ConstructionImpl(builderDescription);
        } else {
            construction.setBuilder(builderDescription);
        }
        computeStatus();
        return this;
    }

    public Description setDependencies(Map<String,Object> map) {
        requireConstructionBuilder();
        construction.setDependencies(map);
        return this;
    }

    private void requireConstructionBuilder() {
        if (construction == null || construction.builderDescription() == null) {
            throw new IllegalStateException("cannot set dependency before builder for construction.");
        }

    }
    public Description setDependency(String key, Object value) {
        requireConstructionBuilder();
        construction.setDependency(key, value);
        computeStatus();
        return this;
    }

    public Description descriptionDependency(String dependency) {
        if (construction == null) {
            return null;
        } else {
            return (Description)construction.dependencies().get(dependency);
        }
    }

    // return expected dependency type
    public <T> T dependency(String key, Class<T> classOfT) {
        if (!dependencies().containsKey(key)) {
            throw new IllegalArgumentException("dependency missing: " + key);
        }
        return classOfT.cast(dependencies().get(key));
    }
    // return expected service type
    public <T> T service(Qua qua, Class<T> classOfT) throws NoImplementationFound {
        return classOfT.cast(service(qua));
    }
}
