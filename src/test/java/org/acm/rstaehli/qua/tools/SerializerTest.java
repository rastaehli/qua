package org.acm.rstaehli.qua.tools;

import org.acm.rstaehli.qua.FileBasedRepository;
import org.acm.rstaehli.qua.InMemoryRepository;
import org.acm.rstaehli.qua.Repository;
import org.junit.Before;
import org.junit.Test;
import org.acm.rstaehli.qua.Description;
import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

import java.io.FileNotFoundException;
import java.io.IOException;

import static junit.framework.TestCase.assertTrue;

public class SerializerTest {
    private Description desc;
    private Repository repo;
    private Serializer serializer;

    @Before
    public void setUp() throws IOException {
        repo = new FileBasedRepository("src/test/resources/descriptionCases/");
        serializer = new Serializer();
        serializer.setRepo(repo);
    }

    @Test
    public void test_json_noType() throws NoImplementationFound {
        desc = repo.lookupByName("noType");
    }

    @Test
    public void test_json_typeOnly() throws NoImplementationFound {
        desc = repo.lookupByName("typeOnly");
        assertTrue(desc.type() != null);
        assertTrue(!desc.isPlanned());
    }

    @Test
    public void test_json_minimalPlan() throws NoImplementationFound {
        desc = repo.lookupByName("minimalPlan");
        assertTrue(desc.isPlanned());
    }

    @Test
    public void test_json_planWithDependencies() throws NoImplementationFound {
        desc = repo.lookupByName("planWithDependencies");
        assertTrue(desc.isTyped());
        assertTrue(desc.dependencies().size() > 1);
    }

    @Test
    public void test_json_extendedProperties() throws NoImplementationFound {
        repo.advertise(repo.lookupByName("namedDescription"));  // parent for extendedProperties
        desc = repo.lookupByName("extendedProperties");
        assertTrue(desc.isTyped());
        assertTrue(desc.properties().get("newProperty1").equals("value1"));
        assertTrue((Double)(desc.properties().get("numberProp")) == 1.3 );
    }

    @Test
    public void test_json_multiLevelInheritance() throws NoImplementationFound {
        repo.advertise(repo.lookupByName("namedDescription"));  // parent for extendedProperties
        repo.advertise(repo.lookupByName("extendedProperties")); // parent for multilevelInheritance
        desc = repo.lookupByName("multiLevelInheritance");
        assertTrue(desc.isTyped());
        assertTrue(desc.properties().get("newProperty2").equals("value2"));
        assertTrue(desc.properties().get("newProperty1").equals("value99"));  // child overrode value
        assertTrue((Double)(desc.properties().get("numberProp")) == 1.3 );
    }

    @Test(expected = NoImplementationFound.class)
    public void test_plan_noImplementation() throws NoImplementationFound {
        desc = repo.lookupByName("noImplementation");
        assertTrue(desc.isTyped());
        desc.plan(repo);
    }
}
