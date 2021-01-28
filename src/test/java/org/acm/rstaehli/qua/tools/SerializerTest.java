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

    public void test_json_noType() throws Exception {
        desc = serializer.descriptionFromJsonFile( "noType", dir );
    }

    @Test
    public void test_json_typeOnly() throws Exception {
        desc = serializer.descriptionFromJsonFile( "typeOnly", dir);
        assertTrue(desc.type() != null);
        assertTrue(!desc.isPlanned());
    }

    @Test
    public void test_json_minimalPlan() throws Exception {
        desc = serializer.descriptionFromJsonFile( "minimalPlan", dir);
        assertTrue(desc.isPlanned());
    }

    @Test
    public void test_json_planWithDependencies() throws Exception {
        desc = serializer.descriptionFromJsonFile( "planWithDependencies", dir );
        assertTrue(desc.isTyped());
        assertTrue(desc.dependencies().size() > 1);
    }

    @Test
    public void test_json_extendedProperties() throws Exception  {
        serializer.descriptionFromJsonFile( "namedDescription", dir );  // parent for extendedProperties
        desc = serializer.descriptionFromJsonFile( "extendedProperties", dir );
        assertTrue(desc.isTyped());
        assertTrue(desc.properties().get("newProperty1").equals("value1"));
        assertTrue((Double)(desc.properties().get("numberProp")) == 1.3 );
    }

    @Test
    public void test_json_multiLevelInheritance() throws Exception {
        serializer.descriptionFromJsonFile("namedDescription", dir );  // parent for extendedProperties
        serializer.descriptionFromJsonFile( "extendedProperties", dir ); // parent for multilevelInheritance
        desc = serializer.descriptionFromJsonFile( "multiLevelInheritance", dir );
        assertTrue(desc.isTyped());
        assertTrue(desc.properties().get("newProperty2").equals("value2"));
        assertTrue(desc.properties().get("newProperty1").equals("value99"));  // child overrode value
        assertTrue((Double)(desc.properties().get("numberProp")) == 1.3 );
    }

    @Test(expected = NoImplementationFound .class)
    public void test_plan_noImplementation() throws Exception, NoImplementationFound {
        desc = serializer.descriptionFromJsonFile( "noImplementation", dir);
        assertTrue(desc.isTyped());
        desc.plan(qua);
    }

    @Test
    public void test_namespaces() throws Exception {
        desc = serializer.descriptionFromJsonFile( "namespace1AliasNs1", dir);
        assertTrue(desc.type().equals("namespace1exampleType"));
    }

    @Test
    public void test_json_arrayProperties() throws Exception {
        desc = serializer.descriptionFromJsonFile( "arrayProperties", dir);
        assertTrue(desc.isTyped());
        Object o = desc.properties().get("listOfStrings");
        assertTrue(o instanceof List);
        List<String> strings = (List<String>)o;
        assertTrue(strings.size() == 2);
        assertTrue(strings.get(0).equals("one"));
    }

    @Test
    public void test_json_multiParentInheritance() throws Exception {
        desc = serializer.descriptionFromJsonFile( "multiParentInheritance", dir);
        assertTrue(desc.isTyped());
        assertTrue(desc.type().equals("qua:exampleType"));
        assertTrue(desc.properties().get("childProperty2").equals("value2"));
        assertTrue(desc.properties().get("childProperty1").equals("value99"));
        assertTrue(desc.properties().get("stringProp").equals("value"));
        assertTrue(desc.properties().get("descriptionProp") instanceof Description);
    }

}
