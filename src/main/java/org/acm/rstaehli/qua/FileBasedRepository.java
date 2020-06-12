package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;
import org.acm.rstaehli.qua.tools.Serializer;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.util.*;

/**
 * read named implementations from file.
 * Configure with directory for files.
 * Use in-memory repository for implementations already read, and for
 * implementations not from file.
 */
public class FileBasedRepository extends AbstractRepository {

    private static final Logger logger = Logger.getLogger(Description.class);

    private AbstractRepository cacheRepository;
    private String fileDirectoryPath;
    private Serializer serializer;

    public FileBasedRepository(String dir) {
        super();
        cacheRepository = new InMemoryRepository();
        fileDirectoryPath = dir;
        serializer = new Serializer();
    }

    public void advertise(Description impl) {
        cacheRepository.advertise(impl);
    }

    @Override
    public void advertiseByName(String name, Description impl) {
        cacheRepository.advertiseByName(name, impl);
    }

    @Override
    public Description implementationByName(String name) throws NoImplementationFound {
        try {
            return cacheRepository.implementationByName(name);
        } catch(NoImplementationFound e) {
            logger.debug("no cached implementation for name: " + name);
        }
        Description impl = null;
        try {
            impl = serializer.descriptionFromJsonFile( Name.keyPart(name), fileDirectoryPath );
        } catch (Exception e2) {
            logger.info("exception reading description from file: " + fileDirectoryPath + Name.keyPart(name));
        }
        try {
            impl.plan(this);  // don't bother to return unless implementation is planned
            cacheRepository.advertiseByName(name, impl);  // cache named instance to avoid rereading the file
            return impl;
        } catch (NoImplementationFound e) {
            logger.error("could not plan file-based description: " + name + ".  Exception: " + e);
            throw new NoImplementationFound("could not plan implementation for: " + name);
        }
    }

    @Override
    protected Collection<Description> implementationsByType(String type) {
        return cacheRepository.implementationsByType(type);
    }
}
