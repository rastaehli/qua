package org.acm.rstaehli.qua.tools;

import org.acm.rstaehli.qua.FileBasedRepository;
import org.acm.rstaehli.qua.Repository;
import org.junit.Before;
import org.junit.Test;
import org.acm.rstaehli.qua.Description;
import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;

public class DescriberTest {
    private Describer describer;
    private Description description;

    @Before
    public void setUp() throws IOException {
    }

    @Test
    public void test_namedService() throws NoImplementationFound {
        Map<String,Object> map = new HashMap<String,Object>();
        description = describer.namedService("noType", map);
        assertTrue(description.isActive());
        assertTrue(description.name().equals("noType"));
        Object obj = description.service();
        assertTrue(obj instanceof Map<?,?>);
    }

    @Test
    public void test_json_typeOnly() throws NoImplementationFound {
        description = describer.lookupByName("typeOnly");
        assertTrue(description.type() != null);
        assertTrue(!desc.isPlanned());
    }

    @Test
    public void test_json_minimalPlan() throws NoImplementationFound {
        description = describer.lookupByName("minimalPlan");
        assertTrue(description.isPlanned());
    }

    @Test
    public void test_json_planWithDependencies() throws NoImplementationFound {
        description = describer.lookupByName("planWithDependencies");
        assertTrue(description.isTyped());
        assertTrue(description.dependencies().size() > 1);
    }

    @Test
    public void test_json_extendedProperties() throws NoImplementationFound {
        desc.advertise(description.lookupByName("namedDescription"));  // parent for extendedProperties
        description = describer.lookupByName("extendedProperties");
        assertTrue(description.isTyped());
        assertTrue(description.properties().get("newProperty1").equals("value1"));
        assertTrue((Double)(description.properties().get("numberProp")) == 1.3 );
    }

    @Test
    public void test_json_multiLevelInheritance() throws NoImplementationFound {
        desc.advertise(description.lookupByName("namedDescription"));  // parent for extendedProperties
        desc.advertise(description.lookupByName("extendedProperties")); // parent for multilevelInheritance
        description = describer.lookupByName("multiLevelInheritance");
        assertTrue(description.isTyped());
        assertTrue(description.properties().get("newProperty2").equals("value2"));
        assertTrue(description.properties().get("newProperty1").equals("value99"));  // child overrode value
        assertTrue((Double)(description.properties().get("numberProp")) == 1.3 );
    }

    @Test(expected = NoImplementationFound.class)
    public void test_plan_noImplementation() throws NoImplementationFound {
        description = describer.lookupByName("noImplementation");
        assertTrue(description.isTyped());
        desc.plan(repo);
    }

    @Test
    public void test_namespaces() throws Exception {
        description = describer.lookupByName("namespace1AliasNs1");
        assertTrue(description.type().equals("namespace1exampleType"));
    }

    @Test
    public void test_json_arrayProperties() throws Exception {
        description = describer.lookupByName("arrayProperties");
        assertTrue(description.isTyped());
        Object o = describer.properties().get("listOfStrings");
        assertTrue(o instanceof List);
        List<String> strings = (List<String>)o;
        assertTrue(strings.size() == 2);
        assertTrue(strings.get(0).equals("one"));
    }

    @Test
    public void test_json_multiParentInheritance() throws Exception {
        description = describer.lookupByName("multiParentInheritance");
        assertTrue(description.isTyped());
        assertTrue(description.type().equals("qua:exampleType"));
        assertTrue(description.properties().get("childProperty2").equals("value2"));
        assertTrue(description.properties().get("childProperty1").equals("value99"));
        assertTrue(description.properties().get("stringProp").equals("value"));
        assertTrue(description.properties().get("descriptionProp") instanceof Description);
    }

}
