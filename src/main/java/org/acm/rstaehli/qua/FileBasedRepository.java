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
    protected Qua qua;

    public FileBasedRepository(String dir, Qua q) {
        cacheRepository = new InMemoryRepository();
        fileDirectoryPath = dir;
        serializer = new Serializer();
        qua = q;
        qua.addRepository(this);  // treat as 1:1 relation
    }

    public void advertise(Description impl) {
        cacheRepository.advertise(impl);
    }

    @Override
    public Description implementationByName(String name) throws NoImplementationFound {
        try {
            return cacheRepository.implementationByName(name);
        } catch(NoImplementationFound e) {
            logger.debug("no cached implementation for name: " + name);
        }
        // next look in file system
        Description impl = null;
        try {
            impl = serializer.descriptionFromJsonFile( Name.keyPart(name), fileDirectoryPath );
        } catch (Exception e2) {
            logger.info("exception reading description from file: " + fileDirectoryPath + Name.keyPart(name));
        }
        try {
            impl.plan(qua);  // don't bother to return unless it is fully planned
            impl.setName(name);  // ensure name is part of description so advertise does not map by type
            cacheRepository.advertise( impl );  // cache named instance to avoid rereading the file
        } catch (NoImplementationFound e) {
            logger.error("could not plan file-based description: " + name + ".  Exception: " + e);
        }
        return impl;
    }

    private String fileNamePart(String name) {
        if (name.contains("/")) {  // assume like http://werver/path/filename
            String[] parts = name.split("/");
            return parts[parts.length-1];
        }
        if (name.contains(":")) {  // assume like alias:filename
            String[] parts = name.split(":");
            return parts[parts.length-1];
        }
        return name;
    }

    @Override
    protected Collection<Description> implementationsByType(String type) {
        return cacheRepository.implementationsByType(type);
    }
}
