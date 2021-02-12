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

/**
 * Read named implementations from file.
 * Configure with <directory> for files, and logical repo name <prefix>.
 * Then, an objects with name of the form <prefix>/<filename> is expected to be
 * found in the file <directory>/<filename>.json.
 * Use in-memory repository for implementations already read, and for
 * implementations not from file.
 */
public class FileBasedRepository extends AbstractRepository {

    private static final Logger logger = Logger.getLogger(Description.class);

    private AbstractRepository cacheRepository;
    private String fileDirectoryPath;
    private String repositoryName;  // prefix for names found in this repository
    private Description builderDesc;
    private JsonSerializer jsonSerializer;
    private final DescriptionSerializer descriptionSerializer;
    protected Qua qua;

    public FileBasedRepository(String dir, Qua q) {
        this(dir, "", q.namedService("HashMapBuilder", new HashMapBuilder()), q);
    }

    public FileBasedRepository(String directory, String prefix, Description builderDesc, Qua qua) {
        super();
        this.cacheRepository = new InMemoryRepository();
        this.fileDirectoryPath = directory;
        this.repositoryName = prefix;
        this.builderDesc = builderDesc;
        this.jsonSerializer = new JsonSerializer();
        this.descriptionSerializer = new DescriptionSerializer();
        this.qua = qua;
        this.qua.addRepository(this);
    }

    public void advertise(Description impl) {
        cacheRepository.advertise(impl);  // should consider persisting in file??
    }

    public Map<String,Object>  mapFromFileWithInheritance(String name, String ... directoryPath) throws Exception {
        String json = jsonFromFile(name, directoryPath);
        Map<String,Object> jsonMap = jsonSerializer.deserializeMap(json);

        if (jsonMap.containsKey("parents")) {
            List<String> parentNames = (List<String>)jsonMap.get("parents");
            for (String parent: parentNames) {
                Map<String,Object> mapParent = mapFromFileWithInheritance(parent, directoryPath);
                inheritFrom(mapParent, jsonMap);
            }
        }

        // Json file syntax allows "namespaceAliases" field to define aliases used to shorten names
        // Since these can be inherited, must first get parent JSON maps and collect alias definitions
        // so we can translate them wherever they occur.
        Map<String,String> namespaces = getNamespaces(jsonMap);
        (new Namespace(namespaces)).translate(jsonMap);
        return jsonMap;
    }

    private void inheritFrom(Map<String,Object> parent, Map<String, Object> child) {
        List<String> fields = Arrays.asList("name", "type", "builderDescription", "serviceObject");
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

    public String jsonFromFile(String name, String ... directoryPath) throws Exception {
        String fileName = null;
        String json = null;
        for (int i=0; i<directoryPath.length; i++) {
            String path = directoryPath[i];
            if (path.length() > 1) {
                path += path.endsWith("/") ? "" : "/";  // add trailing separator if missing
            }
            fileName = path + name + ".json";
            try {
                byte[] bytes = Files.readAllBytes(Paths.get(fileName));
                json = new String(bytes, Charset.defaultCharset());
            } catch (FileNotFoundException e) {
                if (i == directoryPath.length) {
                    logger.error("exception: " + e.getMessage() + " opening file: " + fileName);
                    throw e;
                } else {
                    logger.debug("exception: " + e.getMessage() + " opening file: " + fileName);
                }
            }
        }
        return json;
    }

    // use builder to assemble and cast to expected class
    public Description buildByName(String name) throws NoImplementationFound {
        try {
            return cacheRepository.implementationByName(name);
        } catch(NoImplementationFound e) {
            logger.debug("no cached implementation for name: " + name);
        }
        Map<String,Object> map = null;
        String fileName = Name.keyPart(name, repositoryName);
        try {
            String json = jsonFromFile(fileName, fileDirectoryPath);
            map = jsonSerializer.deserializeMap(json);
        } catch(Exception e) {
            logger.info("exception reading description from file: " + fileDirectoryPath + Name.keyPart(name));
            throw new NoImplementationFound(fileName + " in directory " + fileDirectoryPath);
        }
        Description objDesc = qua.typeAndPlan("qua:Builder", builderDesc);
        objDesc.setProperty("map", map);
        Description result = new Description()
                .setServiceObject(objDesc.service(qua));
        return result;
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
        String fileName = Name.keyPart(name, repositoryName);
        try {
            Map<String,Object> map = mapFromFileWithInheritance(name, fileDirectoryPath);
            impl = descriptionSerializer.descriptionFromMap( map );
        } catch (Exception e2) {
            logger.info("exception reading description from file: " + fileDirectoryPath + Name.keyPart(name));
            throw new NoImplementationFound(fileName + " in directory " + fileDirectoryPath);
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
