package org.acm.rstaehli.qua.tools;

import org.acm.rstaehli.qua.FileBasedRepository;
import org.acm.rstaehli.qua.Qua;
import org.junit.Before;
import org.junit.Test;
import org.acm.rstaehli.qua.Description;
import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

public class SerializerTest {
    private Qua qua;
    private Description desc;
    private String dir;
    private Serializer serializer;

    @Before
    public void setUp() throws IOException {
        qua = new Qua(null);
        dir = "src/test/resources/descriptionCases/";
        new FileBasedRepository(dir,qua);
        serializer = new Serializer();
    }

    public void test_json_noType() throws FileNotFoundException {
        desc = serializer.descriptionFromJsonFile(dir, "noType");
    }

    @Test
    public void test_json_typeOnly() throws FileNotFoundException {
        desc = serializer.descriptionFromJsonFile(dir, "typeOnly");
        assertTrue(desc.type() != null);
        assertTrue(!desc.isPlanned());
    }

    @Test
    public void test_json_minimalPlan() throws FileNotFoundException {
        desc = serializer.descriptionFromJsonFile(dir, "minimalPlan");
        assertTrue(desc.isPlanned());
    }

    @Test
    public void test_json_planWithDependencies() throws FileNotFoundException {
        desc = serializer.descriptionFromJsonFile(dir, "planWithDependencies");
        assertTrue(desc.isTyped());
        assertTrue(desc.dependencies().size() > 1);
    }

    @Test
    public void test_json_extendedProperties() throws FileNotFoundException  {
        serializer.descriptionFromJsonFile(dir, "namedDescription");  // parent for extendedProperties
        desc = serializer.descriptionFromJsonFile(dir, "extendedProperties");
        assertTrue(desc.isTyped());
        assertTrue(desc.properties().get("newProperty1").equals("value1"));
        assertTrue((Double)(desc.properties().get("numberProp")) == 1.3 );
    }

    @Test
    public void test_json_multiLevelInheritance() throws FileNotFoundException  {
        serializer.descriptionFromJsonFile(dir, "namedDescription");  // parent for extendedProperties
        serializer.descriptionFromJsonFile(dir, "extendedProperties"); // parent for multilevelInheritance
        desc = serializer.descriptionFromJsonFile(dir, "multiLevelInheritance");
        assertTrue(desc.isTyped());
        assertTrue(desc.properties().get("newProperty2").equals("value2"));
        assertTrue(desc.properties().get("newProperty1").equals("value99"));  // child overrode value
        assertTrue((Double)(desc.properties().get("numberProp")) == 1.3 );
    }

    @Test(expected = NoImplementationFound .class)
    public void test_plan_noImplementation() throws FileNotFoundException, NoImplementationFound {
        desc = serializer.descriptionFromJsonFile(dir, "noImplementation");
        assertTrue(desc.isTyped());
        desc.plan(qua);
    }

    @Test
    public void test_namespaces() throws Exception {
        desc = serializer.descriptionFromJsonFile(dir, "namespace1AliasNs1");
        assertTrue(desc.type().equals("namespace1exampleType"));
    }

    @Test
    public void test_json_arrayProperties() throws Exception {
        desc = serializer.descriptionFromJsonFile(dir, "arrayProperties");
        assertTrue(desc.isTyped());
        Object o = desc.properties().get("listOfStrings");
        assertTrue(o instanceof List);
        List<String> strings = (List<String>)o;
        assertTrue(strings.size() == 2);
        assertTrue(strings.get(0).equals("one"));
    }

    @Test
    public void test_json_multiParentInheritance() throws Exception {
        desc = serializer.descriptionFromJsonFile(dir, "multiParentInheritance");
        assertTrue(desc.isTyped());
        assertTrue(desc.type().equals("qua:exampleType"));
        assertTrue(desc.properties().get("childProperty2").equals("value2"));
        assertTrue(desc.properties().get("childProperty1").equals("value99"));
        assertTrue(desc.properties().get("stringProp").equals("value"));
        assertTrue(desc.properties().get("descriptionProp") instanceof Description);
    }

}
