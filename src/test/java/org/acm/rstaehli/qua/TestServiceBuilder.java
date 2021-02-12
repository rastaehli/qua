package org.acm.rstaehli.qua;

import com.google.gson.Gson;
import org.acm.rstaehli.qua.AbstractPassiveServiceBuilder;
import org.acm.rstaehli.qua.Builder;
import org.acm.rstaehli.qua.Description;
import org.acm.rstaehli.qua.TestService;

import java.util.Map;

/**
 * Given a json string, parse json and
 * assemble into a HashMap.
 */
public class TestServiceBuilder extends AbstractPassiveServiceBuilder {
    @Override
    public void assemble(Description impl) {
        if (!impl.hasProperty("map")) {
            throw new IllegalArgumentException("TestServiceBuilder cannot find map property in description.");
        }
        TestService result = buildFromMap(impl.property("map",Map.class));
        impl.setServiceObject(result);
    }

    private TestService buildFromMap(Map map) {
        String name = (String) map.get("name");
        TestService child = map.get("child") != null
                ? buildFromMap((Map) map.get("child"))
                : null;
        return new TestService(name, child);
    }
}
