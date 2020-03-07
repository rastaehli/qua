package org.acm.rstaehli.qua.tools;

import org.acm.rstaehli.qua.Repository;
import org.junit.Before;
import org.junit.Test;
import org.acm.rstaehli.qua.Description;
import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

public class SerializerTest {
    private Description desc;
    private Repository repo;
    private Serializer serializer;

    @Before
    public void setUp() throws IOException {
        repo = new Repository();
        serializer = new Serializer();
        serializer.setRepo(repo);
    }

    @Test
    public void test_json_noType() {
        desc = fromCase("noType");
    }

    private Description fromCase(String caseName) {
        try {
            return serializer.descriptionFromJsonFile("src/test/resources/descriptionCases/" + caseName + ".json");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Test
    public void test_json_typeOnly() {
        desc = fromCase("typeOnly");
        assertTrue(desc.type() != null);
        assertTrue(!desc.isPlanned());
    }

    @Test
    public void test_json_minimalPlan() {
        desc = fromCase("minimalPlan");
        assertTrue(desc.isPlanned());
    }

    @Test
    public void test_json_planWithDependencies() {
        desc = fromCase("planWithDependencies");
        assertTrue(desc.isTyped());
        assertTrue(desc.dependencies().size() > 1);
    }

    @Test
    public void test_json_extendedProperties() {
        repo.advertise(fromCase("namedDescription"));  // parent for extendedProperties
        desc = fromCase("extendedProperties");
        assertTrue(desc.isTyped());
        assertTrue(desc.properties().get("newProperty1").equals("value1"));
        assertTrue((Double)(desc.properties().get("numberProp")) == 1.3 );
    }

    @Test
    public void test_json_multiLevelInheritance() {
        repo.advertise(fromCase("namedDescription"));  // parent for extendedProperties
        repo.advertise(fromCase("extendedProperties")); // parent for multilevelInheritance
        desc = fromCase("multiLevelInheritance");
        assertTrue(desc.isTyped());
        assertTrue(desc.properties().get("newProperty2").equals("value2"));
        assertTrue(desc.properties().get("newProperty1").equals("value99"));  // child overrode value
        assertTrue((Double)(desc.properties().get("numberProp")) == 1.3 );
    }

    @Test(expected = NoImplementationFound.class)
    public void test_plan_noImplementation() throws NoImplementationFound {
        desc = fromCase("noImplementation");
        assertTrue(desc.isTyped());
        desc.plan(repo);
    }
}
