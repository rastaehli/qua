package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.acm.rstaehli.qua.Lifecycle.*;

public class ConstructionImpl implements Construction {

    private Description builderDescription;
    private Map<String, Object> dependencies;
    private Map<String, Object> interfaces;
    private static final String PRIMARY_SERVICE_NAME = "PRIMARY"; // unique key for primary service interface

    public ConstructionImpl(Description builderDescription, Map<String, Object> dependencies) {
        this.builderDescription = builderDescription;
        this.dependencies = dependencies;
        this.interfaces = null;
    }

    @Override
    public Construction setBuilder(Description builder) {
        this.builderDescription = builder;
        return this;
    }

    @Override
    public Description builderDescription() {
        return builderDescription;
    }

    @Override
    public Builder builder() throws NoImplementationFound {
        if (builderDescription == null) {
            throw new NoImplementationFound("for builderDescription");
        }
        return (Builder)builderDescription.service();
    }

    @Override
    public Construction setDependencies(Map<String, Object> d) {
        this.dependencies = d;
        return this;
    }

    @Override
    public Construction setDependency(String key, Object value) {
        if (dependencies == null) {
            dependencies = new HashMap<>();
        }
        dependencies.put(key, value);
        return this;
    }

    @Override
    public Map<String, Object> dependencies() {
        return dependencies;
    }

    @Override
    public Map<String, Object> interfaces() {
        return this.interfaces;
    }

    @Override
    public Object getInterface(String name) {
        return this.interfaces.get(name);
    }

    @Override
    public Construction setService(Object obj) {
        this.interfaces.put(PRIMARY_SERVICE_NAME, obj);
        return this;
    }

    @Override
    public Construction setInterface(String name, Object value) {
        this.interfaces.put(name, value);
        return this;
    }

    @Override
    public Object service() {
        return getInterface(PRIMARY_SERVICE_NAME);
    }

    @Override
    public boolean equals(Construction other) {
        if (!(other instanceof ConstructionImpl)) {
            return false;
        }
        ConstructionImpl otherConstructionImpl = (ConstructionImpl)other;
        if (!this.builderDescription.equals(otherConstructionImpl.builderDescription)) {
            return false;
        }
        if (this.dependencies == null || ((ConstructionImpl) other).dependencies == null) {
            return false;
        }
        if (!this.dependencies.equals(otherConstructionImpl.dependencies)) {
            return false;
        }
        return true;
    }

    @Override
    public void mergeConstruction(Construction goal) {
        if (this.builderDescription == null && goal.builderDescription() != null) {
            this.builderDescription = goal.builderDescription();
        }
        if (this.service() == null && goal.service() != null) {
            this.setService( goal.service() );
        }
        if (this.dependencies == null && goal.dependencies() != null) {
            this.dependencies = goal.dependencies();
        }
        Mappings.merge(goal.dependencies(), this.dependencies);
    }


    @Override
    public List<Description> descriptions() {
        List<Description> descriptions = new ArrayList<>();
        for (Object o: dependencies.values()) {
            if (o instanceof Description) {
                descriptions.add((Description) o);
            }
        }
        if (builderDescription != null) {
            descriptions.add(builderDescription);
        }
        return descriptions;
    }

}
