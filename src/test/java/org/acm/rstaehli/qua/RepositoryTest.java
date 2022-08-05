package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;
import org.acm.rstaehli.qua.tools.Serializer;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * a Repository is a meta object for storing and retrieving Descriptions.
 * The basic operations are
 *  -  advertise a Description
 *  -  lookup a Description by its type, name, or properties
 *
 */
public class RepositoryTest {
    private Repository repo;
    private Serializer serializer;

    @Before
    public void setUp() throws IOException {
        repo = new InMemoryRepository();
    }

    @Test
    public void testAdvertise() throws NoImplementationFound {
        Description testDesc = repo.namedService("testName", this);
        repo.advertise(testDesc);
        Description resultDesc = repo.implementationByName("testName");
        assertTrue(resultDesc != null);
        assertTrue(resultDesc.isActive());
        assertTrue(resultDesc.service() == this);
    }

    @Test
    public void testAdvertise_by_type() throws NoImplementationFound {
        Description testDesc = repo.typedService("testType", this);
        repo.advertise(testDesc);
        Description requirements = repo.type("testType");
        List<Description> results = repo.implementationsMatching(requirements);
        assertTrue(results.size() == 1);
        assertTrue(results.get(0).isActive());
        assertTrue(results.get(0).service() == this);
    }

    @Test
    public void testAdvertise_alternateImplementations() throws NoImplementationFound {
        String testType = "altImplsTestType";
        List<String> expected = Arrays.asList("impl1", "impl2", "impl3");
        advertiseImpls(testType, expected);
        advertiseImpls("anotherType", Arrays.asList("other1", "other2", "other3"));
        Description requirements = repo.type(testType);
        List<Description> results = repo.implementationsMatching(requirements);
        assertTrue(results.size() == 3);
        List<String> actual = results.stream().map(d -> (String)d.service()).collect(Collectors.toList());
        assertTrue(actual.containsAll(expected));
    }

    private void advertiseImpls(String type, List<String> impls) {
        for (String s: impls) {
            Description desc = repo.typedService(type,s);
            repo.advertise(desc);
        }
    }

    @Test
    public void testImplementationsMatching_noMatch() {
        String testType = "testType";
        Description requirements = repo.type(testType);
        List<Description> results = repo.implementationsMatching(requirements);
        assertTrue(results.size() == 0);
    }

    @Test
    public void testImplementationsMatching_oneMatch() {
        String testType = "testType";
        advertiseImpls(testType, Arrays.asList("only1"));
        Description requirements = repo.type(testType);
        List<Description> results = repo.implementationsMatching(requirements);
        assertTrue(results.size() == 1);
    }

    @Test
    public void testImplementationsMatching_twoMatch() {
        String testType = "testType";
        advertiseImpls(testType, Arrays.asList("first1", "second1"));
        Description requirements = repo.type(testType);
        List<Description> results = repo.implementationsMatching(requirements);
        assertTrue(results.size() == 2);
    }

    @Test
    public void testImplementationsMatching_stringPropertyMatch() {
        String testType = "testType";
        advertiseWithProperties(testType);
        Description requirements = repo.typeAndProperties(testType, mapProps(
                Arrays.asList("StringProperty"), Arrays.asList("stringProp2")));
        List<Description> results = repo.implementationsMatching(requirements);
        assertTrue(results.size() == 1);
    }

    @Test
    public void testImplementationsMatching_stringPropertyMatch_notFound() {
        String testType = "testType";
        advertiseWithProperties(testType);
        Description requirements = repo.typeAndProperties(testType, mapProps(
                Arrays.asList("StringProperty"), Arrays.asList("noMatch")));
        List<Description> results = repo.implementationsMatching(requirements);
        assertTrue(results.size() == 0);
    }

    @Test
    public void testImplementationsMatching_intProperty_Match() {
        String testType = "testType";
        advertiseWithProperties(testType);
        Description requirements = repo.typeAndProperties(testType, mapProps(
                Arrays.asList("IntProperty"), Arrays.asList(123)));
        List<Description> results = repo.implementationsMatching(requirements);
        assertTrue(results.size() == 1);
    }

    @Test
    public void testImplementationsMatching_intProperty_noMatch() {
        String testType = "testType";
        advertiseWithProperties(testType);
        Description requirements = repo.typeAndProperties(testType, mapProps(
                Arrays.asList("IntProperty"), Arrays.asList(3)));
        List<Description> results = repo.implementationsMatching(requirements);
        assertTrue(results.size() == 0);
    }

    @Test
    public void testImplementationsMatching_DescriptionProperty_noMatch() {
        String testType = "testType";
        advertiseWithProperties(testType);
        Description requirements = repo.typeAndProperties(testType, mapProps(
                Arrays.asList("DescriptionProperty"), Arrays.asList(repo.type("noMatchingType"))));
        List<Description> results = repo.implementationsMatching(requirements);
        assertTrue(results.size() == 0);
    }

    @Test
    public void testImplementationsMatching_DescriptionProperty_Match() {
        String testType = "testType";
        advertiseWithProperties(testType);
        Description requirements = repo.typeAndProperties(testType, mapProps(
                Arrays.asList("DescriptionProperty"), Arrays.asList(repo.type("propertyType2"))));
        List<Description> results = repo.implementationsMatching(requirements);
        assertTrue(results.size() == 1);
    }

    private void advertiseWithProperties(String testType) {
        Map<String, Object> props1 = mapProps(
                Arrays.asList("StringProperty", "IntProperty", "DescriptionProperty"),
                Arrays.asList("stringProp", 123, repo.type("propertyType")));
        Description desc = repo.typeAndProperties(testType, props1);
        repo.advertise(desc);
        Map<String, Object> props2 = mapProps(
                Arrays.asList("StringProperty", "IntProperty", "DescriptionProperty"),
                Arrays.asList("stringProp2", 2, repo.type("propertyType2")));
        desc = repo.typeAndProperties(testType, props2);
        repo.advertise(desc);

    }
    private Map<String, Object> mapProps(List<String> keys, List<Object> values) {
        Map<String,Object> map = new HashMap<>();
        for (int i = 0; i< Math.min(keys.size(), values.size()); i++ ) {
            map.put(keys.get(i), values.get(i));
        }
        return map;
    }

    public void testImplementationByName() {
    }

    public void testBestMatch() {
    }

    public void testNamedService() {
    }

    public void testNamedOnly() {
    }

    public void testTypedPlan() {
    }

    public void testType() {
    }

    public void testTypeAndProperties() {
    }

    public void testTypeAndPlan() {
    }

    public void testTestTypeAndPlan() {
    }

    @Test
    public void test_namedService() throws NoImplementationFound {
        Map<String,Object> map = new HashMap<String,Object>();
        Description desc = repo.namedService("noType", map);
        assertTrue(desc.isActive());
        Object obj = desc.service();
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
        Description builder = repo.namedService("concatenator", new Concatenator());

        Description desc = repo.typedPlan(type, properties, builder, properties);

        assertTrue(!desc.isActive());  // no active yet
        assertTrue(desc.isPlanned());
        desc.activate();  // but can be activated to get service
        assertTrue(desc.service().equals("value1value2"));
    }
//    public void test_json_minimalPlan() throws NoImplementationFound {
//        description = describer.lookupByName("minimalPlan");
//        assertTrue(description.isPlanned());
//    }
//
//    @Test
//    public void test_json_planWithDependencies() throws NoImplementationFound {
//        description = describer.lookupByName("planWithDependencies");
//        assertTrue(description.isTyped());
//        assertTrue(description.dependencies().size() > 1);
//    }
//
//    @Test
//    public void test_json_extendedProperties() throws NoImplementationFound {
//        desc.advertise(description.lookupByName("namedDescription"));  // parent for extendedProperties
//        description = describer.lookupByName("extendedProperties");
//        assertTrue(description.isTyped());
//        assertTrue(description.properties().get("newProperty1").equals("value1"));
//        assertTrue((Double)(description.properties().get("numberProp")) == 1.3 );
//    }
//
//    @Test
//    public void test_json_multiLevelInheritance() throws NoImplementationFound {
//        desc.advertise(description.lookupByName("namedDescription"));  // parent for extendedProperties
//        desc.advertise(description.lookupByName("extendedProperties")); // parent for multilevelInheritance
//        description = describer.lookupByName("multiLevelInheritance");
//        assertTrue(description.isTyped());
//        assertTrue(description.properties().get("newProperty2").equals("value2"));
//        assertTrue(description.properties().get("newProperty1").equals("value99"));  // child overrode value
//        assertTrue((Double)(description.properties().get("numberProp")) == 1.3 );
//    }
//
//    @Test(expected = NoImplementationFound.class)
//    public void test_plan_noImplementation() throws NoImplementationFound {
//        description = describer.lookupByName("noImplementation");
//        assertTrue(description.isTyped());
//        desc.plan(repo);
//    }
//
//    @Test
//    public void test_namespaces() throws Exception {
//        description = describer.lookupByName("namespace1AliasNs1");
//        assertTrue(description.type().equals("namespace1exampleType"));
//    }
//
//    @Test
//    public void test_json_arrayProperties() throws Exception {
//        description = describer.lookupByName("arrayProperties");
//        assertTrue(description.isTyped());
//        Object o = describer.properties().get("listOfStrings");
//        assertTrue(o instanceof List);
//        List<String> strings = (List<String>)o;
//        assertTrue(strings.size() == 2);
//        assertTrue(strings.get(0).equals("one"));
//    }
//
//    @Test
//    public void test_json_multiParentInheritance() throws Exception {
//        description = describer.lookupByName("multiParentInheritance");
//        assertTrue(description.isTyped());
//        assertTrue(description.type().equals("qua:exampleType"));
//        assertTrue(description.properties().get("childProperty2").equals("value2"));
//        assertTrue(description.properties().get("childProperty1").equals("value99"));
//        assertTrue(description.properties().get("stringProp").equals("value"));
//        assertTrue(description.properties().get("descriptionProp") instanceof Description);
//    }

}
