package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;
import org.acm.rstaehli.qua.tools.Serializer;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.acm.rstaehli.qua.Behavior.MATCH_ANY;

public class DescriptionTestIT {
    private Description desc;
    private Repository repo;
    private Serializer serializer;

    @Before
    public void setUp() throws IOException {
        repo = new FileBasedRepository("src/test/resources/descriptionCases/");
//        repo.advertise(JvmObjectBuilder(Class cls, List<Class> constructorArgTypes, List<Object> ));
        serializer = new Serializer();
    }

    @Test
    public void test_serviceObject_string() throws NoImplementationFound {
        desc = repo.implementationByName("serviceObjectOnly");
        assertTrue(desc.isActive());
        assertTrue(desc.service() instanceof String);
        assertTrue(desc.service().equals("12345"));
    }

    @Test
    public void test_matchFor_success() {
        Description stringDescription = repo.namedService("stringService", "stringValue");
        Map<String, Object> goalProperties = new HashMap<>();
        goalProperties.put("p1", "v1");
        goalProperties.put("p2", 12345);
        goalProperties.put("p3", stringDescription);
        Description goal = repo.typeAndProperties("goaltype", goalProperties);
        Map<String, Object> conformingProperties = new HashMap<>();
        conformingProperties.put("p1", MATCH_ANY);
        conformingProperties.put("p2", MATCH_ANY);
        conformingProperties.put("p3", MATCH_ANY);
        Description conformingDesc = repo.typeAndProperties("goaltype", conformingProperties);

        Description match = conformingDesc.matchFor(goal);
        assertTrue(match != conformingDesc);
        assertTrue(match.properties() != conformingDesc.properties());
        assertEquals(match.type(), goal.type());
        assertEquals(match.properties().size(), goal.properties().size());
        for (String key: goal.properties().keySet()) {
            assertEquals(match.properties().get(key), goal.properties().get(key));
        }
    }
}
