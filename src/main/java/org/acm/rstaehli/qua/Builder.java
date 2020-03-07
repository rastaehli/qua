package org.acm.rstaehli.qua;

public interface Builder {
    void assemble(Plan impl);  // build from dependencies

    void start(Plan impl);  // enable service activity

    void stop(Plan impl);  // disable further service activity

    void recycle(Plan impl);  // release resources
}
