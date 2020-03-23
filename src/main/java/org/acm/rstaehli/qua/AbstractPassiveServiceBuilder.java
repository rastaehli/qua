package org.acm.rstaehli.qua;

public class AbstractPassiveServiceBuilder implements Builder {
    @Override
    public void assemble(Description impl) {
        throw new IllegalStateException("subclass should override assemble method.");
    }

    @Override
    public void start(Description impl) {
        assemble(impl);  // passive service need only ensure assembly
    }

    @Override
    public void stop(Description impl) {

    }

    @Override
    public void recycle(Description impl) {

    }
}
