package org.acm.rstaehli.qua.tools;

import com.google.gson.Gson;
import java.util.Map;

public class JsonSerializer {

    public Map<String,Object> deserializeMap(String json) throws Exception {
        try {
            return new Gson().fromJson(json, Map.class);
        } catch(Exception e) {
            throw new Exception("exception parsing json string: " + json);
        }
    }
}
