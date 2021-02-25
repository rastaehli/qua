package org.acm.rstaehli.qua;

import java.util.Map;

/**
 * Given a json object map property, set TestService properties.
 */
public class TestServiceMapBuilder extends AbstractPassiveServiceBuilder {
    public static final String RESULT_TYPE = TestService.class.getSimpleName();

    @Override
    public void assemble(Description impl) {
        if (!impl.hasProperty("map")) {
            throw new IllegalArgumentException("TestServiceMapBuilder cannot find map property in description.");
        }
        TestService result = buildFromMap(impl.property("map",Map.class));
        impl.setServiceObject(result);
    }

    private TestService buildFromMap(Map map) {
        String name = (String) map.get("repositoryName");
        TestService child = map.get("child") != null
                ? buildFromMap((Map) map.get("child"))
                : null;
        return new TestService(name, child);
    }

    @Override
    public String resultType() {
        return RESULT_TYPE;
    }
}
