package com.expd.tools;

import org.acm.rstaehli.qua.tools.Serializer;
import org.junit.Before;
import org.junit.Test;
import org.acm.rstaehli.qua.Description;
import tools.NoImplementationFound;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

public class SerializerTest {
    private Description desc;

    @Before
    public void setUp() throws IOException {
    }

    @Test
    public void test_json_noType() {
        desc = fromCase("noType");
    }

    private Description fromCase(String caseName) {
        try {
            return Serializer.descriptionFromJsonFile("src/test/resources/descriptionCases/" + caseName + ".json");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Test
    public void test_json_typeOnly() {
        desc = fromCase("typeOnly");
        assertTrue(desc.getType() != null);
        assertTrue(!desc.planned());
    }

    @Test
    public void test_json_minimalPlan() {
        desc = fromCase("minimalPlan");
        assertTrue(desc.planned());
    }

    @Test
    public void test_json_planWithDependencies() {
        desc = fromCase("planWithDependencies");
        assertTrue(desc.typed());
        assertTrue(desc.getPlan().getDependencies().size() > 1);
    }

    @Test
    public void test_json_extendedProperties() {
        desc = fromCase("extendedProperties");
        assertTrue(desc.typed());
        assertTrue(desc.getProperties().get("newProperty1").equals("value1"));
        assertTrue((Double)(desc.getProperties().get("numberProp")) == 1.3 );
    }

    @Test
    public void test_json_multiLevelInheritance() {
        desc = fromCase("multiLevelInheritance");
        assertTrue(desc.typed());
        assertTrue(desc.getProperties().get("newProperty2").equals("value2"));
        assertTrue(desc.getProperties().get("newProperty1").equals("value99"));  // child overrode value
        assertTrue((Double)(desc.getProperties().get("numberProp")) == 1.3 );
    }

    @Test
    public void test_json_depotExample() throws FileNotFoundException {
        desc = Serializer.descriptionFromJsonFile("src/test/resources/depotExample.json");
        assertTrue(desc.getProperties().containsKey("artifacts"));
        assertTrue(((List)(desc.getProperties().get("artifacts"))).size() > 10);
    }

    @Test(expected = NoImplementationFound.class)
    public void test_plan_noImplementation() {
        desc = fromCase("noImplementation");
        assertTrue(desc.typed());
        desc.plan();
    }
}
