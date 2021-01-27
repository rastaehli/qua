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
    private Qua qua;

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
    protected Collection<Description> implementationsByName(String name) {
        Collection<Description> matches = cacheRepository.implementationsByName(name);
        try {
            Description d = serializer.descriptionFromJsonFile( fileDirectoryPath, fileNamePart(name) );
            try {
                d.setName(name);  // ensure name is part of description so advertise does not map by type
                d.activate(qua);  // don't bother to return unless it is full implementation
                cacheRepository.advertise( d );  // cache named instance to avoid rereading the file
                matches.add( d );
            } catch (NoImplementationFound e) {
                logger.error("could not activate file-based description: " + name + ".  Exception: " + e);
            }
        } catch (FileNotFoundException e2) {
            logger.info("file implementation not found for: " + fileDirectoryPath + fileNamePart(name));
        }
        return matches;
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
