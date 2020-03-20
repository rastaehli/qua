package org.acm.rstaehli.qua.tools;

import org.acm.rstaehli.qua.FileBasedRepository;
import org.acm.rstaehli.qua.Repository;
import org.junit.Before;
import org.junit.Test;
import org.acm.rstaehli.qua.Description;
import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

import java.io.IOException;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

public class SerializerTest {
    private Description desc;
    private Repository repo;
    private Serializer serializer;

    @Before
    public void setUp() throws IOException {
        repo = new FileBasedRepository("src/test/resources/descriptionCases/");
        serializer = new Serializer();
        serializer.setParentRepo(repo);
    }

    @Test
    public void test_json_noType() throws NoImplementationFound {
        desc = repo.implementationByName("noType");
    }

    @Test
    public void test_json_typeOnly() throws NoImplementationFound {
        desc = repo.implementationByName("typeOnly");
        assertTrue(desc.type() != null);
        assertTrue(!desc.isPlanned());
    }

    @Test
    public void test_json_minimalPlan() throws NoImplementationFound {
        desc = repo.implementationByName("minimalPlan");
        assertTrue(desc.isPlanned());
    }

    @Test
    public void test_json_planWithDependencies() throws NoImplementationFound {
        desc = repo.implementationByName("planWithDependencies");
        assertTrue(desc.isTyped());
        assertTrue(desc.dependencies().size() > 1);
    }

    @Test
    public void test_json_extendedProperties() throws NoImplementationFound {
        repo.advertise(repo.implementationByName("namedDescription"));  // parent for extendedProperties
        desc = repo.implementationByName("extendedProperties");
        assertTrue(desc.isTyped());
        assertTrue(desc.properties().get("newProperty1").equals("value1"));
        assertTrue((Double)(desc.properties().get("numberProp")) == 1.3 );
    }

    @Test
    public void test_json_multiLevelInheritance() throws NoImplementationFound {
        repo.advertise(repo.implementationByName("namedDescription"));  // parent for extendedProperties
        repo.advertise(repo.implementationByName("extendedProperties")); // parent for multilevelInheritance
        desc = repo.implementationByName("multiLevelInheritance");
        assertTrue(desc.isTyped());
        assertTrue(desc.properties().get("newProperty2").equals("value2"));
        assertTrue(desc.properties().get("newProperty1").equals("value99"));  // child overrode value
        assertTrue((Double)(desc.properties().get("numberProp")) == 1.3 );
    }

    @Test(expected = NoImplementationFound.class)
    public void test_plan_noImplementation() throws NoImplementationFound {
        desc = repo.implementationByName("noImplementation");
        assertTrue(desc.isTyped());
        desc.plan(repo);
    }

    @Test
    public void test_namespaces() throws Exception {
        desc = repo.implementationByName("namespace1AliasNs1");
        assertTrue(desc.type().equals("namespace1exampleType"));
    }

    @Test
    public void test_json_arrayProperties() throws Exception {
        desc = repo.implementationByName("arrayProperties");
        assertTrue(desc.isTyped());
        Object o = desc.properties().get("listOfStrings");
        assertTrue(o instanceof List);
        List<String> strings = (List<String>)o;
        assertTrue(strings.size() == 2);
        assertTrue(strings.get(0).equals("one"));
    }

    @Test
    public void test_json_multiParentInheritance() throws Exception {
        desc = repo.implementationByName("multiParentInheritance");
        assertTrue(desc.isTyped());
        assertTrue(desc.type().equals("qua:exampleType"));
        assertTrue(desc.properties().get("childProperty2").equals("value2"));
        assertTrue(desc.properties().get("childProperty1").equals("value99"));
        assertTrue(desc.properties().get("stringProp").equals("value"));
        assertTrue(desc.properties().get("descriptionProp") instanceof Description);
    }

}
