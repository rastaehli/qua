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
        desc.setServiceObject(0); // set to anything, just so it's an implementation we can advertise
        repo.advertise(desc);
        Map<String, Object> props2 = mapProps(
                Arrays.asList("StringProperty", "IntProperty", "DescriptionProperty"),
                Arrays.asList("stringProp2", 2, repo.type("propertyType2")));
        desc = repo.typeAndProperties(testType, props2);
        desc.setServiceObject(0); // set to anything, just so it's an implementation we can advertise
        repo.advertise(desc);

    }
    private Map<String, Object> mapProps(List<String> keys, List<Object> values) {
        Map<String,Object> map = new HashMap<>();
        for (int i = 0; i< Math.min(keys.size(), values.size()); i++ ) {
            map.put(keys.get(i), values.get(i));
        }
        return map;
    }

    @Test
    public void testImplementationByName() throws NoImplementationFound {
        Description testDesc = repo.namedService("testName", "obj1");
        repo.advertise(testDesc);
        testDesc = repo.namedService("testName2", "obj2");
        repo.advertise(testDesc);
        Description resultDesc = repo.implementationByName("testName");
        assertTrue(resultDesc != null);
        assertTrue(resultDesc.isActive());
        assertTrue(resultDesc.service().equals("obj1"));
    }

    @Test
    public void testBestMatch_noQualitySpec() throws NoImplementationFound {
        String testType = "testType";
        Description testDesc = repo.typedService(testType, "obj1");
        repo.advertise(testDesc);
        testDesc = repo.typedService(testType, "obj2");
        repo.advertise(testDesc);
        Description resultDesc = repo.bestMatch(repo.type(testType));
        assertTrue(resultDesc != null);
        assertTrue(resultDesc.isActive());
        assertTrue(resultDesc.service().equals("obj2"));
    }

    @Test
    public void testBestMatch_withQualitySpec() throws NoImplementationFound {
        String testType = "testType";
        Description testDesc = repo.typedService(testType, "obj1");
        testDesc.setQuality(new MockQuality(0.1));
        repo.advertise(testDesc);
        testDesc = repo.typedService(testType, "obj3");
        testDesc.setQuality(new MockQuality(0.99)); // this is the best match based on utility
        repo.advertise(testDesc);
        testDesc = repo.typedService(testType, "obj2");
        testDesc.setQuality(new MockQuality(0.6));
        repo.advertise(testDesc);
        Description resultDesc = repo.bestMatch(repo.type(testType));
        assertTrue(resultDesc != null);
        assertTrue(resultDesc.isActive());
        assertTrue(resultDesc.service().equals("obj3"));
    }

    @Test
    public void testNamedService() {
        Description desc = repo.namedService("bozo", 1);
        assertTrue(desc.name().equals("bozo"));
        assertTrue(desc.isActive());
        assertTrue(desc.service().equals(1));
    }

    @Test
    public void testTypedPlan() throws NoImplementationFound {
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

    @Test
    public void testType() {
        Description desc = repo.type("bozo");
        assertTrue(desc.type().equals("bozo"));
        assertTrue(desc.name() == null);
        assertTrue(desc.isTyped());
        assertTrue(!desc.isPlanned());
        assertTrue(desc.service() == null);
    }

    @Test
    public void testTypeAndProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("a", "aValue");
        props.put("b", "bValue");
        Description desc = repo.typeAndProperties("bozo", props);
        assertTrue(desc.type().equals("bozo"));
        assertTrue(desc.properties().equals(props));
        assertTrue(desc.isTyped());
        assertTrue(!desc.isPlanned());
        assertTrue(desc.service() == null);
    }

    @Test
    public void testTypeAndPlan() {
        Description plan = repo.type("testBuilder");
        Description desc = repo.typedPlan("bozo", plan);
        assertTrue(desc.type().equals("bozo"));
        assertTrue(desc.isTyped());
        assertTrue(desc.isPlanned());
        assertTrue(desc.service() == null);
    }

    @Test
    public void testTestTypeAndPlan_withDependencies() {
        Description plan = repo.type("testBuilder");
        Map<String, Object> dependencies = new HashMap<>();
        dependencies.put("first",repo.type("depencencyType1"));
        dependencies.put("second",repo.type("depencencyType2"));
        Description desc = repo.typedPlan("bozo", plan, dependencies);
        assertTrue(desc.type().equals("bozo"));
        assertTrue(desc.isTyped());
        assertTrue(desc.isPlanned());
        assertTrue(desc.service() == null);
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

    class MockQuality implements Quality {

        public Double utility;

        public MockQuality(Double mockUtility) {
            utility = mockUtility;
        }
        @Override
        public Quality setErrorDimensions(List<String> errorDimensions) {
            return null;
        }

        @Override
        public List<String> getErrorDimensions() {
            return null;
        }

        @Override
        public Quality setWeights(Map<String, Object> allowances) {
            return null;
        }

        @Override
        public Map<String, Object> getWeights() {
            return null;
        }

        @Override
        public Quality setEstimateFunctions(Map<String, Object> estimateFunctions) {
            return null;
        }

        @Override
        public Map<String, Object> getEstimateFunctions() {
            return null;
        }

        @Override
        public Quality setRequiredUtility(Float requiredUtility) {
            return null;
        }

        @Override
        public Float getRequiredUtility() {
            return null;
        }

        @Override
        public boolean equals(Quality other) {
            return other.comparable(this) && ((MockQuality)other).utility == this.utility;
        }

        @Override
        public Quality copy() {
            return new MockQuality(utility);
        }

        @Override
        public Double utility(Description impl) {
            return utility;
        }

        @Override
        public boolean comparable(Quality other) {
            return other instanceof MockQuality;
        }
    }

}
