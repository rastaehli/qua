package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

public class AbstractPassiveServiceBuilder implements Builder {
    @Override
    public void assemble(Description impl) {
        throw new IllegalStateException("subclass should override assemble method.");
    }

    @Override
    public void start(Description impl) {
        try {
            impl.assemble();  // passive service need only ensure assembly
        } catch (NoImplementationFound noImplementationFound) {
            throw new IllegalStateException("AbstractPassiveServiceBuilder called to start an unimplemented Description.");
        }
    }

    @Override
    public void stop(Description impl) {

    }

    @Override
    public void recycle(Description impl) {

    }
}
