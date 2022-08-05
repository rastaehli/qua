package org.acm.rstaehli.qua;

import java.util.List;
import java.util.Map;

public class QualityImpl implements Quality {

    public List<String> errorDimensions;
    public Map<String, Object> weights;
    public Map<String, Object> estimateFunctions;
    public Float requiredUtility;

    @Override
    public Quality setErrorDimensions(List<String> errorDimensions) {
        this.errorDimensions = errorDimensions;
        return this;
    }

    @Override
    public Quality setWeights(Map<String, Object> weights) {
        this.weights = weights;
        return this;
    }

    @Override
    public Map<String, Object> getWeights() {
        return weights;
    }

    @Override
    public Quality setEstimateFunctions(Map<String, Object> estimateFunctions) {
        this.estimateFunctions = estimateFunctions;
        return this;
    }

    @Override
    public Map<String, Object> getEstimateFunctions() {
        return estimateFunctions;
    }

    @Override
    public Quality setRequiredUtility(Float requiredUtility) {
        this.requiredUtility = requiredUtility;
        return this;
    }

    @Override
    public Float getRequiredUtility() {
        return requiredUtility;
    }

    @Override
    public boolean equals(Quality other) {
        if (this == other) {
            return true;
        }
        return errorDimensions.equals(other.getErrorDimensions())
                && weights.equals(other.getWeights())
                && estimateFunctions.equals(other.getEstimateFunctions())
                && requiredUtility.equals(other.getRequiredUtility());
    }

    @Override
    public boolean comparable(Quality other) {
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
        if (!same(this.weights, otherQualityImpl.weights))  {
            return false;
        }
        if (!same(this.estimateFunctions, otherQualityImpl.estimateFunctions))  {
            return false;
        }
        return true;
    }

    @Override
    public Quality copy() {
        Quality copy = new QualityImpl();
        copy.setWeights(this.weights);
        copy.setErrorDimensions(this.errorDimensions);
        copy.setEstimateFunctions((this.estimateFunctions));
        copy.setRequiredUtility(this.requiredUtility);
        return copy;
    }

    @Override
    public List<String> getErrorDimensions() {
        return errorDimensions;
    }

    @Override
    public Double utility(Description impl) {
        // exponential decay function (Math.E raised to negative weighted magnitude of error vector)
        // give utility 1.0 for perfect quality, and decays to zero as error increases.
        double weightedErrorSquareSum = 0.0;
        for (String key: estimateFunctions.keySet()) {
            EstimateFunction f = (EstimateFunction) estimateFunctions.get(key);
            weightedErrorSquareSum += Math.pow(f.estimate(impl), 2);  // square the estimate
        }
        return Math.pow(Math.E, - Math.sqrt(weightedErrorSquareSum)) ;
    }

    private boolean same(Object one, Object two) {
        if (one == null || two == null) {
            return false; //  must not be null when comparing
        }
        return one.equals(two);
    }
}
