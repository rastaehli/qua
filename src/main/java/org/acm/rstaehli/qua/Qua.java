package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;
import org.acm.rstaehli.qua.tools.Namespace;
import sun.security.krb5.internal.crypto.Des;

import java.util.HashMap;
import java.util.Map;

import static org.acm.rstaehli.qua.Behavior.UNKNOWN_TYPE;

/**
 * Provides context for describing and building components.
 * Provides convenience methods for constructing Description objects programatically.
 * Provides repository for finding implementations that match a description.
 */
public class Qua {

    public static final String QUA_NAME = "http://github.com/rstaehli/name";
    private Namespace ns;
    private Repository repo;

    public Qua() {
        this(new HashMap<>(), null);
    }

    public Qua(Map<String,String> namespaces) {
        this(namespaces, null);
    }

    public Qua(Map<String,String> namespaces, Repository r) {
        this.ns = new Namespace(namespaces);
        // always include namespace for QuA built in names
        this.ns.addMapping("qua", "http://github.com/rstaehli/");
        this.repo = r;
    }

    public Qua addRepository(Repository r) {
        this.repo = r;
        return this;
    }

    public Namespace namespace() {
        return ns;
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
        Description d = new Description()
                .setType(ns.translate(type))
                .setProperties(ns.translate(properties));
        if (builder != null || dependencies != null) {
            d.setConstruction(new ConstructionImpl(builder, ns.translate(dependencies)));
        }
        return d;
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

    public Repository repository() {
        assert(repo != null);
        return repo;
    }

    public Description typedService(String type, Object service) {
        Description d = type(type);
        d.setServiceObject(service);
        return d;
    }

    public String translate(String alias) {
        return ns.translate(alias);
    }

    public Description mutableCopy(Description d) {
        return d.mutableCopy();
    }

    public <T> T findByName(String name, Class<T> c) throws NoImplementationFound {
        Description d = repository().implementationByName(name);
        return c.cast(d.service(this));
    }
}
