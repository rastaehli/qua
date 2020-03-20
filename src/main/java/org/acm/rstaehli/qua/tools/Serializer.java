package org.acm.rstaehli.qua.tools;

import com.google.gson.Gson;
import org.acm.rstaehli.qua.Description;
import org.acm.rstaehli.qua.Repository;
import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description is a meta object to reflect on and manage the implementation of a service.
 *
 * - type: names the logical behavior of the service
 * - properties: may contain additional attributes that restrict the type:
 * - plan: describes the implementation of the service.  It may also be executed to build the service.
 *
 * A service conforms to this description only if the type matches and it has all the listed properties.
 * When a service is built, it can be accessed by its "service" property.
 */
public class Serializer {

    private static Repository parentRepo;  // this Serializer responsible for resolving inheritance

    public Description descriptionFromJsonFile(String path) throws FileNotFoundException, NoImplementationFound {
        return descriptionFromJsonFile(path, parentRepo);
    }

    /**
     * Client may set the repo where parent descriptions are found.
     * @param r
     */
    public void setParentRepo(Repository r) {
        parentRepo = r;
    }

    public Description descriptionFromJsonFile(String path, Repository repo) throws FileNotFoundException, NoImplementationFound {
        Map<String,Object> map = new Gson().fromJson(new FileReader(Paths.get(path).toFile()), Map.class);
        Map<String,String> namespaces = getNamespaces(map);
        replaceAliasesWithNamespaceTranslations(map, namespaces);

        Description desc = new Description(map);
        if (map.containsKey("parents")) {
            List<String> parentNames = (List<String>)map.get("parents");
            for (String name: parentNames) {
                Description parent = repo.implementationByName(name);
                desc.inheritFrom(parent);
            }
        }
        return desc;
    }

    private void replaceAliasesWithNamespaceTranslations(Map<String,Object> map, Map<String, String> namespaces) {
        for (String key: map.keySet()) {
            Object o = map.get(key);
            if (o instanceof String) {
                String s = (String)o;
                int aliasEnd = s.indexOf(':');
                if (aliasEnd > 0) {
                    String alias = s.substring(0,aliasEnd);
                    String translation = namespaces.get(alias);
                    if (translation != null) {
                        // replace original value with translation substituted for alias
                        map.put(key, translation + s.substring(aliasEnd + 1,s.length()));
                    }
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
}
