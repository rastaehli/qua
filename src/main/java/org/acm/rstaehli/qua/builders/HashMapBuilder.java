package org.acm.rstaehli.qua.builders;

import com.google.gson.Gson;
import org.acm.rstaehli.qua.AbstractPassiveServiceBuilder;
import org.acm.rstaehli.qua.Description;
import java.util.Map;

/**
 * Given a json string, parse json and
 * assemble into a HashMap.
 */
public class HashMapBuilder extends AbstractPassiveServiceBuilder {
        @Override
        public void assemble(Description impl) {
            if (impl.hasProperty("json")) {
                Map<String,Object> jsonMap = new Gson().fromJson(impl.stringProperty("json"), Map.class);
                impl.setServiceObject(jsonMap);
            };
        }

    @Override
    public String resultType() {
        return "Map";
    }
    }
