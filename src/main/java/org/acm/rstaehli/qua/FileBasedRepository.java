package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;
import org.acm.rstaehli.qua.tools.Serializer;

import java.io.FileNotFoundException;
import java.util.*;

/**
 * read named implementations from file.
 * Configure with directory for files.
 * Use in-memory repository for implementations already read, and for
 * implementations not from file.
 */
public class FileBasedRepository extends AbstractRepository {

    private AbstractRepository cacheRepository;
    private String fileDirectoryPath;
    private Serializer serializer;

    public FileBasedRepository(String dir) {
        cacheRepository = new InMemoryRepository();
        fileDirectoryPath = dir;
        serializer = new Serializer();
    }

    public void advertise(Description impl) {
        cacheRepository.advertise(impl);
    }

    @Override
    protected Collection<Description> implementationsByName(String name) {
        Collection<Description> matches = cacheRepository.implementationsByName(name);
        try {
            matches.add( serializer.descriptionFromJsonFile(fileDirectoryPath, fileNamePart(name) ) );
        } catch (FileNotFoundException e2) {}  // just return empty collection
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
