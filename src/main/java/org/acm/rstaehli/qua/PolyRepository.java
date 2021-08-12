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
        boolean advertised = false;
        if (impl.name() != null) {
            for (Repository repo: reposForName(impl.name())) {
                    repo.advertise(impl);
                    advertised = true;
            }
        }
        if (!advertised) {
            cacheRepository.advertise(impl);
        }
    }

    private List<Repository> reposForName(String name) {
        List<Repository> repos = new ArrayList<>();
        for (String prefix: repositories.keySet()) {
            if (name.toUpperCase().startsWith(prefix.toUpperCase())) {
                repos.add( repositories.get(prefix) );
            }
        }
        return repos;
    }

    @Override
    public Description implementationByName(String name) throws NoImplementationFound {
        String fullName = qua.translate(name);
        try {
            return cacheRepository.implementationByName(fullName);
        } catch(NoImplementationFound e) {
            logger.debug("no cached implementation for repositoryName: " + fullName);
        }
        for (Repository repo: reposForName(fullName)) {
            try {
                return repo.implementationByName(fullName);
            } catch (NoImplementationFound noImplementationFound) {
                // ignore until we've checked all repos
            }
        }
        throw new NoImplementationFound("for name: " + fullName);
    }

    @Override
    public List<Description> implementationsMatching(Description d) {
        List<Description> found = new ArrayList<>();
        for (Repository repo: reposForName(d.type())) {
            found.addAll( repo.implementationsMatching(d) );
        }
        found.addAll( cacheRepository.implementationsMatching(d) );
        return found;
    }

    @Override
    public List<Description> implementationsByType(String type) {
        List<Description> found = new ArrayList<>();
        String fullName = qua.translate(repositoryName);
        for (Repository repo: reposForName(fullName)) {
            found.addAll( repo.implementationsByType(fullName) );
        }
        found.addAll( cacheRepository.implementationsByType(fullName) );
        return found;
    }

}
