package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.builders.HashMapBuilder;
import org.acm.rstaehli.qua.exceptions.NoImplementationFound;
import org.acm.rstaehli.qua.tools.DescriptionSerializer;
import org.acm.rstaehli.qua.tools.JsonSerializer;
import org.acm.rstaehli.qua.tools.Namespace;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.acm.rstaehli.qua.Behavior.UNKNOWN_TYPE;

/**
 * Build "resultType" from json representation in file.
 * Configure with <directory> for files, and logical repo name <prefix>.
 * Then, an objects with name of the form <prefix>/<filename> is expected to be
 * found in the file <directory>/<filename>.json.
 * Use in-memory repository for implementations already read, and for
 * implementations not from file.
 *
 * (extends FileBasedDescriptionRepository so it can call mapFromFileWithInheritance)
 */
public class FileBasedRepository extends FileBasedDescriptionRepository {

    private static final Logger logger = Logger.getLogger(FileBasedRepository.class);

    private Description builderDesc;  // builds from json map file
    private String resultType;  // type of builder result
    private JsonSerializer jsonSerializer;

    public FileBasedRepository(String directory, String prefix, Description builderDesc, Qua qua) {
        super(directory, prefix, qua);
        this.builderDesc = builderDesc;
        this.jsonSerializer = new JsonSerializer();
        this.qua.addRepository(this);
    }

    @Override
    public Description implementationByName(String name) throws NoImplementationFound {
        String fullName = qua.translate(name);
        try {
            return cacheRepository.implementationByName(fullName);
        } catch(NoImplementationFound e) {
            logger.debug("no cached implementation for name: " + fullName);
        }
        // next look in file system
        String fileName = Name.keyPart(fullName, repositoryName);
        Map<String,Object> map = mapFromFileWithInheritance(fileName, fileDirectoryPath);
        Description desc = qua.typeAndPlan(resultType(), builderDesc);
        desc.setDependency("jobProperties", map);

        if (desc != null) {
            desc.setName(fullName);  // ensure name is part of description so advertise does not map by type
            cacheRepository.advertise( desc );  // cache named instance to avoid rereading the file
        }
        return desc;
    }

    private String resultType() {
        if (resultType == null) {
            try {
                resultType = ((Builder) builderDesc.service(qua)).resultType();
            } catch (NoImplementationFound noImplementationFound) {
                resultType = UNKNOWN_TYPE;
            }
        }
        return resultType;
    }

}
