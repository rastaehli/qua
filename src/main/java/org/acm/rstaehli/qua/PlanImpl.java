package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlanImpl implements Plan {

    private Description builderDescription;
    private Map<String, Object> dependencies;

    public PlanImpl(Description builderDescription, Map<String, Object> dependencies) {
        this.builderDescription = builderDescription;
        this.dependencies = dependencies;
    }

    @Override
    public Plan setBuilderDescription(Description builder) {
        this.builderDescription = builder;
        return this;
    }

    @Override
    public Description getBuilderDescription() {
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
    public Plan setDependencies(Map<String, Object> d) {
        this.dependencies = d;
        return this;
    }

    @Override
    public Plan setDependency(String key, Object value) {
        if (dependencies == null) {
            dependencies = new HashMap<>();
        }
        dependencies.put(key, value);
        return this;
    }

    @Override
    public Map<String, Object> getDependencies() {
        return dependencies;
    }

    @Override
    public boolean equals(Plan other) {
        if (!(other instanceof PlanImpl)) {
            return false;
        }
        PlanImpl otherConstructionImpl = (PlanImpl)other;
        if (!this.builderDescription.equals(otherConstructionImpl.builderDescription)) {
            return false;
        }
        if (this.dependencies == null || ((PlanImpl) other).dependencies == null) {
            return false;
        }
        if (!this.dependencies.equals(otherConstructionImpl.dependencies)) {
            return false;
        }
        return true;
    }

    @Override
    public void mergePlan(Plan goal) {
        if (this.builderDescription == null && goal.getBuilderDescription() != null) {
            this.builderDescription = goal.getBuilderDescription();
        }
        if (this.dependencies == null && goal.getDependencies() != null) {
            this.dependencies = goal.getDependencies();
        } else {
            Mappings.merge(goal.getDependencies(), this.dependencies);
        }
    }


    @Override
    public List<Description> descriptions() {
        List<Description> descriptions = new ArrayList<>();
        if (dependencies != null) {
            for (Object o: dependencies.values()) {
                if (o instanceof Description) {
                    descriptions.add((Description) o);
                }
            }
        }
        if (builderDescription != null) {
            descriptions.add(builderDescription);
        }
        return descriptions;
    }

    @Override
    public Plan copy() {
        if (dependencies == null) {
            return new PlanImpl(builderDescription, null);
        }
        Map<String, Object> dependenciesCopy = new HashMap<>();
        for (String key: dependencies.keySet()) {
            dependenciesCopy.put(key, dependencies.get(key));
        }
        return new PlanImpl(builderDescription, dependenciesCopy);
    }

}
