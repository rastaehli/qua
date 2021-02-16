package org.acm.rstaehli.qua;

public interface Builder {
    void assemble(Description impl);  // build from dependencies

    void start(Description impl);  // enable service activity

    void stop(Description impl);  // disable further service activity

    void recycle(Description impl);  // release resources

    String resultType();
}
