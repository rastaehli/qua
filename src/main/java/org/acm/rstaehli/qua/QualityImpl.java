package org.acm.rstaehli.qua;

import java.util.List;
import java.util.Map;

public class QualityImpl implements Quality {

    public List<String> errorDimensions;
    public Map<String, Object> allowances;
    public Map<String, Object> utilityFunctions;
    public Float requiredUtility;

    @Override
    public Quality setErrorDimensions(List<String> errorDimensions) {
        this.errorDimensions = errorDimensions;
        return this;
    }

    @Override
    public Quality setAllowances(Map<String, Object> allowances) {
        this.allowances = allowances;
        return this;
    }

    @Override
    public Quality setUtility(Map<String, Object> utilityFunctions) {
        this.utilityFunctions = utilityFunctions;
        return this;
    }

    @Override
    public Quality setRequiredUtility(Float requiredUtility) {
        this.requiredUtility = requiredUtility;
        return this;
    }

    @Override
    public Float requiredUtility() {
        return requiredUtility;
    }

    @Override
    public boolean equals(Quality other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof QualityImpl)) {
            return false;
        }
        QualityImpl otherQualityImpl = (QualityImpl)other;
        if (!same(this.errorDimensions, otherQualityImpl.errorDimensions))  {
            return false;
        }
        if (!same(this.allowances, otherQualityImpl.allowances))  {
            return false;
        }
        if (!same(this.utilityFunctions, otherQualityImpl.utilityFunctions))  {
            return false;
        }
        if (!same(this.requiredUtility, otherQualityImpl.requiredUtility))  {
            return false;
        }
        return true;
    }

    private boolean same(Object one, Object two) {
        if (one == null || two == null) {
            return false; //  must not be null when comparing
        }
        return one.equals(two);
    }
}
