package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;
import org.acm.rstaehli.qua.tools.Serializer;

import java.io.FileNotFoundException;
import java.util.Map;

/**
 * read named implementations from file.
 * Configure with directory for files.
 * Use in-memory repository for implementations already read, and for
 * implementations not from file.
 */
public class FileBasedRepository extends AbstractRepository {

    private Repository cacheRepository;
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

    public Description implementationByName(String name) throws NoImplementationFound {
        try {
            return cacheRepository.implementationByName(name);
        } catch (NoImplementationFound e) {}
        try {
            return serializer.descriptionFromJsonFile(fileDirectoryPath, name);
        } catch (FileNotFoundException e2) {
            throw new NoImplementationFound("for name: " + name);
        }
    }

    public Description implementationByType(String type) throws NoImplementationFound {
        return cacheRepository.implementationByType(type);
    }

    public Description implementationByType(String type, Map<String,Object> requiredProperties) throws NoImplementationFound {
        return cacheRepository.implementationByType(type, requiredProperties);
    }
}