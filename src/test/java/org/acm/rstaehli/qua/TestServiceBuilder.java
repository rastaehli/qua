package org.acm.rstaehli.qua;

import com.google.gson.Gson;
import org.acm.rstaehli.qua.AbstractPassiveServiceBuilder;
import org.acm.rstaehli.qua.Builder;
import org.acm.rstaehli.qua.Description;
import org.acm.rstaehli.qua.TestService;

import java.util.Map;

/**
 * Given a name property and optional child dependency
 * assemble a TestService.
 */
public class TestServiceBuilder extends AbstractPassiveServiceBuilder {

    public static final String RESULT_TYPE = TestService.class.getSimpleName();

    @Override
    public void assemble(Description impl) {
        if (!impl.hasProperty("name")) {
            throw new IllegalArgumentException("TestServiceBuilder cannot find name property in description.");
        }
        TestService child = impl.dependencies().containsKey("child")
                ? (TestService) impl.dependency("child")
                : null;
        TestService result = new TestService(impl.stringProperty("name"), child);
        impl.setServiceObject(result);
    }

    private TestService buildFromMap(Map map) {
        String name = (String) map.get("name");
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
