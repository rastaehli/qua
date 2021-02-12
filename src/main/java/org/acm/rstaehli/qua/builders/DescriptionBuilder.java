package org.acm.rstaehli.qua.builders;

import com.google.gson.Gson;
import org.acm.rstaehli.qua.AbstractPassiveServiceBuilder;
import org.acm.rstaehli.qua.Description;
import org.acm.rstaehli.qua.tools.DescriptionSerializer;

import java.util.Map;

/**
 * Given a json string, parse json and
 * assemble into a HashMap.
 */
public class DescriptionBuilder extends AbstractPassiveServiceBuilder {
        @Override
        public void assemble(Description impl) {
            if (impl.hasProperty("map")) {
                Map<String,Object> jsonMap = impl.property("map", Map.class);
                Description d = (new DescriptionSerializer()).descriptionFromMap(jsonMap);
                impl.setServiceObject(d);
            };
        }
    }
