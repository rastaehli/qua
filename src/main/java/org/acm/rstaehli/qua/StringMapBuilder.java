package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

import java.util.HashMap;
import java.util.Map;


public class StringMapBuilder extends AbstractPassiveServiceBuilder {

    public static Map<String, String> from(Description job, String fieldName) {
        try {
            Object o = job.properties().get(fieldName);
            if (o instanceof Map) {
                return (Map<String,String>)o;
            }
            Description mapDescription = (Description)o;
            return mapDescription == null ? new HashMap<>() : (Map<String,String>)mapDescription.service();
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    public static Description newMapDescription(Repository repo) {
        try {
            return repo.bestMatch(repo.description("qua:stringMap", new HashMap<>()));
        } catch (NoImplementationFound noImplementationFound) {
            noImplementationFound.printStackTrace();  // TODO: handle or avoid this possible exception
        }
        return null;
    }

    @Override
    public void assemble(Description job) {
        Map<String, Object> props = job.properties();
        Map<String, String> map = new HashMap<>();
        for (String key: props.keySet()) {
            if (props.get(key) instanceof String) {
                map.put(key, (String)props.get(key));
            }
        }

        job.setServiceObject(map);
    }

}
