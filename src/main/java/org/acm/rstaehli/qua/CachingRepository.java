package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class CachingRepository extends AbstractRepository {

    protected Repository cacheRepository = new InMemoryRepository("");

    public CachingRepository(String prefix) {
        super(prefix);
    }

    public void advertise(Description impl) {
        cacheRepository.advertise(impl);
    }

    @Override
    public List<Description> implementationsByType(String type) {
        return cacheRepository.implementationsByType(type);
    }

}
