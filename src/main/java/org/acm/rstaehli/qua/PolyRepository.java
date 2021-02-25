package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Configure with multiple <prefix> named repositories.
 * When looking for implementation by repositoryName, will consult the appropriate
 * repository based on implementation repositoryName prefix.
 * Use in-memory repository to cache implementations already read.
 */
public class PolyRepository extends CachingRepository {

    private static final Logger logger = Logger.getLogger(PolyRepository.class);
    protected final Qua qua;
    protected final Map<String,Repository> repositories = new HashMap<>();

    public PolyRepository(Qua qua) {
        super("");
        this.qua = qua;
    }

    public void addRepository(Repository r) {
        repositories.put(r.name(),r);
    }

    public void advertise(Description impl) {
        if (impl.name() != null) {
            Repository repo = repoForName(impl.name());
            if (repo != null) {
                repo.advertise(impl);
                return;
            }
        }
        cacheRepository.advertise(impl);  // should consider persisting in file??
    }

    private Repository repoForName(String name) {
        for (String prefix: repositories.keySet()) {
            if (name.toUpperCase().startsWith(prefix.toUpperCase())) {
                return repositories.get(prefix);
            }
        }
        return null;
    }

    @Override
    public Description implementationByName(String name) throws NoImplementationFound {
        String fullName = qua.translate(name);
        try {
            return cacheRepository.implementationByName(fullName);
        } catch(NoImplementationFound e) {
            logger.debug("no cached implementation for repositoryName: " + fullName);
        }
        Repository repo = repoForName(fullName);
        if (repo != null) {
            return repo.implementationByName(fullName);
        }
        return null;
    }

    @Override
    public List<Description> implementationsMatching(Description d) {
        List<Description> found = new ArrayList<>();
        Repository repo = repoForName(d.type());
        if (repo != null) {
            found.addAll( repo.implementationsMatching(d) );
        }
        found.addAll( cacheRepository.implementationsMatching(d) );
        return found;
    }

    @Override
    public List<Description> implementationsByType(String type) {
        List<Description> found = new ArrayList<>();
        String fullName = qua.translate(repositoryName);
        Repository repo = repoForName(fullName);
        if (repo != null) {
            found.addAll( repo.implementationsByType(fullName) );
        }
        found.addAll( cacheRepository.implementationsByType(fullName) );
        return found;
    }

}
