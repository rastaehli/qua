package org.acm.rstaehli.qua.tools;

import org.acm.rstaehli.qua.*;
import org.junit.Before;
import org.junit.Test;
import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;

public class QuaTest {
    private Qua qua;
    private Description description;

    @Before
    public void setUp() throws IOException {
        qua = new Qua(new HashMap());
    }

    @Test
    public void test_namedService() throws NoImplementationFound {
        Map<String,Object> map = new HashMap<String,Object>();
        description = qua.namedService("noType", map);
        assertTrue(description.isActive());
        assertTrue(description.name().equals("noType"));
        Object obj = description.service();
        assertTrue(obj instanceof Map<?,?>);
    }

    class Concatenator implements Builder {

        @Override
        public void assemble(Description impl) {
            Map<String,Object> parts = impl.dependencies();
            impl.setServiceObject((String)parts.get("p1")+(String)parts.get("p2"));
        }
        @Override
        public void start(Description impl) {
        }
        @Override
        public void stop(Description impl) {
        }
        @Override
        public void recycle(Description impl) {
        }
    }
    @Test
    public void test_typedPlan() throws NoImplementationFound {
        String type = "aType";
        Map<String,Object> properties = new HashMap<>();
        properties.put("p1", "value1");
        properties.put("p2", "value2");
        Description builder = qua.namedService("concatenator", new Concatenator());

        description = qua.typedPlan(type, properties, builder, properties);

        assertTrue(!description.isActive());  // no active yet
        assertTrue(description.isPlanned());
        description.activate();  // but can be activated to get service
        assertTrue(description.service().equals("value1value2"));
    }
//    public void test_json_minimalPlan() throws NoImplementationFound {
//        description = qua.lookupByName("minimalPlan");
//        assertTrue(description.isPlanned());
//    }
//
//    @Test
//    public void test_json_planWithDependencies() throws NoImplementationFound {
//        description = qua.lookupByName("planWithDependencies");
//        assertTrue(description.isTyped());
//        assertTrue(description.dependencies().size() > 1);
//    }
//
//    @Test
//    public void test_json_extendedProperties() throws NoImplementationFound {
//        desc.advertise(description.lookupByName("namedDescription"));  // parent for extendedProperties
//        description = qua.lookupByName("extendedProperties");
//        assertTrue(description.isTyped());
//        assertTrue(description.properties().get("newProperty1").equals("value1"));
//        assertTrue((Double)(description.properties().get("numberProp")) == 1.3 );
//    }
//
//    @Test
//    public void test_json_multiLevelInheritance() throws NoImplementationFound {
//        desc.advertise(description.lookupByName("namedDescription"));  // parent for extendedProperties
//        desc.advertise(description.lookupByName("extendedProperties")); // parent for multilevelInheritance
//        description = qua.lookupByName("multiLevelInheritance");
//        assertTrue(description.isTyped());
//        assertTrue(description.properties().get("newProperty2").equals("value2"));
//        assertTrue(description.properties().get("newProperty1").equals("value99"));  // child overrode value
//        assertTrue((Double)(description.properties().get("numberProp")) == 1.3 );
//    }
//
//    @Test(expected = NoImplementationFound.class)
//    public void test_plan_noImplementation() throws NoImplementationFound {
//        description = qua.lookupByName("noImplementation");
//        assertTrue(description.isTyped());
//        desc.plan(qua);
//    }
//
//    @Test
//    public void test_namespaces() throws Exception {
//        description = qua.lookupByName("namespace1AliasNs1");
//        assertTrue(description.type().equals("namespace1exampleType"));
//    }
//
//    @Test
//    public void test_json_arrayProperties() throws Exception {
//        description = qua.lookupByName("arrayProperties");
//        assertTrue(description.isTyped());
//        Object o = qua.properties().get("listOfStrings");
//        assertTrue(o instanceof List);
//        List<String> strings = (List<String>)o;
//        assertTrue(strings.size() == 2);
//        assertTrue(strings.get(0).equals("one"));
//    }
//
//    @Test
//    public void test_json_multiParentInheritance() throws Exception {
//        description = qua.lookupByName("multiParentInheritance");
//        assertTrue(description.isTyped());
//        assertTrue(description.type().equals("qua:exampleType"));
//        assertTrue(description.properties().get("childProperty2").equals("value2"));
//        assertTrue(description.properties().get("childProperty1").equals("value99"));
//        assertTrue(description.properties().get("stringProp").equals("value"));
//        assertTrue(description.properties().get("descriptionProp") instanceof Description);
//    }

}
