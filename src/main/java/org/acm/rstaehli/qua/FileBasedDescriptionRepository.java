package org.acm.rstaehli.qua;

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
 * Read named implementations from json-serialized Description file.
 * Configure with <directory> for files, and logical repo repositoryName <prefix>.
 * Then, an objects with repositoryName of the form <prefix>/<filename> is expected to be
 * found in the file <directory>/<filename>.json.
 * Use in-memory repository to cache implementations already read.
 */
public class FileBasedDescriptionRepository extends FileBasedStringRepository {

    private static final Logger logger = Logger.getLogger(FileBasedDescriptionRepository.class);

    private JsonSerializer jsonSerializer;
    private final DescriptionSerializer descriptionSerializer;

    public FileBasedDescriptionRepository(String directory, String prefix, Qua qua) {
        super(directory, prefix, ".json", qua);  // look for .json files
        this.jsonSerializer = new JsonSerializer();
        this.descriptionSerializer = new DescriptionSerializer();
        this.qua.addRepository(this);
    }

    public Map<String,Object>  mapFromFileWithInheritance(String fileName, String ... directoryPath) throws NoImplementationFound {
        try {
            String json = stringFromFile(fileName, directoryPath);
            Map<String, Object> jsonMap = jsonSerializer.deserializeMap(json);

            if (jsonMap.containsKey("parents")) {
                List<String> parentNames = (List<String>) jsonMap.get("parents");
                for (String parent : parentNames) {
                    Map<String, Object> mapParent = mapFromFileWithInheritance(parent, directoryPath);
                    inheritFrom(mapParent, jsonMap);
                }
            }

            // Json file syntax allows "namespaceAliases" field to define aliases used to shorten names
            // Since these can be inherited, must first get parent JSON maps and collect alias definitions
            // so we can translate them wherever they occur.
            Map<String, String> namespaces = getNamespaces(jsonMap);
            (new Namespace(namespaces)).translate(jsonMap);
            return jsonMap;
        } catch (Exception e) {
            logger.info("exception reading json from file: " + fileName);
            throw new NoImplementationFound(fileName);
        }
    }

    private void inheritFrom(Map<String,Object> parent, Map<String, Object> child) {
        List<String> fields = Arrays.asList("repositoryName", "type", "builderDescription", "serviceObject");
        for (String fieldName: fields) {
            inherit(fieldName, parent, child);
        }
        List<String> mapFields = Arrays.asList("properties", "dependencies", "namespaces");
        for (String mapName: mapFields) {
            inheritMappings(mapName, parent, child);
        }
    }

    public void inherit(String key, Map<String,Object> parent, Map<String, Object> child) {
        if (!child.containsKey(key) && parent.containsKey(key)) {
            child.put(key, parent.get(key));
        }
    }

    public void inheritMappings(String mapName, Map<String,Object> parent, Map<String, Object> child) {
        if (parent.containsKey(mapName)) {
            Map<String,Object> parentMap = (Map<String,Object>)parent.get(mapName);
            if (!child.containsKey(mapName)) {
                child.put(mapName, new HashMap<String, Object>());
            }
            Map<String,Object> childMap = (Map<String,Object>)child.get(mapName);
            for (String key: parentMap.keySet()) {
                if (!childMap.containsKey(key)) {
                    childMap.put(key,parentMap.get(key));
                }
            }
        }
    }

    private Map<String,String> getNamespaces(Map<String,Object> jsonObject) {
        if (jsonObject.containsKey("namespaces")) {
            Object value = jsonObject.get("namespaces");
            if (value instanceof Map) {
                return (Map<String,String>)value;
            }
        }
        return new HashMap<>();
    }

    @Override
    public Description implementationByName(String name) throws NoImplementationFound {
        String fullName = qua.translate(name);
        try {
            return cacheRepository.implementationByName(fullName);
        } catch(NoImplementationFound e) {
            logger.debug("no cached implementation for repositoryName: " + fullName);
        }
        // next look in file system
        String fileName = Name.keyPart(fullName, repositoryName);
        Map<String,Object> map = mapFromFileWithInheritance(fileName, fileDirectoryPath);
        Description desc = descriptionSerializer.descriptionFromMap( map );
        desc.plan(qua);  // don't bother to return unless it is fully planned
        desc.setName(fullName);  // ensure repositoryName is part of description so advertise does not map by type
        cacheRepository.advertise( desc );  // cache named instance to avoid rereading the file
        return desc;
    }

    private String resultType() {
        // type of the description is unknown in general
       return UNKNOWN_TYPE;
    }

}
