package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.acm.rstaehli.qua.Behavior.UNKNOWN_TYPE;

/**
 * Read a String from file.
 * Configure with <directory> for files, and logical repo name <prefix>.
 * Use in-memory repository to cache implementations already read.
 */
public class FileBasedStringRepository extends AbstractRepository {

    private static final Logger logger = Logger.getLogger(FileBasedStringRepository.class);
    protected final Qua qua;

    protected AbstractRepository cacheRepository;
    protected String fileDirectoryPath;
    protected String repositoryName;  // prefix for names found in this repository

    public FileBasedStringRepository(String dir, Qua qua) {
        this(dir, "", qua);
    }

    public FileBasedStringRepository(String directory, String prefix, Qua qua) {
        super();
        this.cacheRepository = new InMemoryRepository();
        this.fileDirectoryPath = directory;
        this.repositoryName = prefix;
        this.qua = qua;
    }

    public void advertise(Description impl) {
        cacheRepository.advertise(impl);  // should consider persisting in file??
    }

    public String stringFromFile(String name, String ... directoryPath) throws Exception {
        String fileName = null;
        String contents = null;
        for (int i=0; i<directoryPath.length; i++) {
            String path = directoryPath[i];
            if (path.length() > 1) {
                path += path.endsWith("/") ? "" : "/";  // add trailing separator if missing
            }
            fileName = path + name + ".json";
            try {
                byte[] bytes = Files.readAllBytes(Paths.get(fileName));
                contents = new String(bytes, Charset.defaultCharset());
            } catch (FileNotFoundException e) {
                if (i == directoryPath.length) {
                    logger.error("exception: " + e.getMessage() + " opening file: " + fileName);
                    throw e;
                } else {
                    logger.debug("exception: " + e.getMessage() + " opening file: " + fileName);
                }
            }
        }
        return contents;
    }

    @Override
    public Description implementationByName(String name) throws NoImplementationFound {
        String fullName = qua.translate(name);
        String contents = null;
        try {
            return cacheRepository.implementationByName(fullName);
        } catch(NoImplementationFound e) {
            logger.debug("no cached implementation for name: " + fullName);
        }
        // next look in file system
        String fileName = Name.keyPart(fullName, repositoryName);
        try {
            contents = stringFromFile(fileName, fileDirectoryPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Description desc = qua.typedService(fullName, contents);
        cacheRepository.advertise( desc );  // cache named instance to avoid rereading the file
        return desc;
    }

    private String resultType() {
        return "qua:String";
    }

    @Override
    protected Collection<Description> implementationsByType(String type) {
        return cacheRepository.implementationsByType(type);
    }

}
