package org.acm.rstaehli.qua.tools;

import com.google.gson.Gson;
import org.acm.rstaehli.qua.Description;
import org.acm.rstaehli.qua.Repository;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Paths;
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

    private static Repository repo;

    public Description descriptionFromJsonFile(String path) throws FileNotFoundException {
        return descriptionFromJsonFile(path, getDefaultRepo());
    }

    public void setRepo(Repository r) {
        repo = r;
    }

    private Repository getDefaultRepo() {
        if (repo == null) {
            repo = new Repository();
        }
        return repo;
    }

    public Description descriptionFromJsonFile(String path, Repository repo) throws FileNotFoundException {
        Map map = new Gson().fromJson(new FileReader(Paths.get(path).toFile()), Map.class);
        Description desc = new Description(map);
        if (map.containsKey("parent")) {
            Description parent = repo.lookupByName((String) map.get("parent"));
            desc.inheritFrom(parent);
        }
        return desc;
    }
}
