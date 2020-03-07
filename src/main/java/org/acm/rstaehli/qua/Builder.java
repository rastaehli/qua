package org.acm.rstaehli.qua;

public interface Builder {
    void assemble(ImplementationPlan impl);  // build from dependencies

    void start(ImplementationPlan impl);  // enable service activity

    void stop(ImplementationPlan impl);  // disable further service activity

    void recycle(ImplementationPlan impl);  // release resources
}
