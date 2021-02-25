import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.acm.rstaehli.qua.*;
import org.acm.rstaehli.qua.builders.HashMapBuilder;
import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;

public class StepDefinitions {

    Description d;
    Qua qua = new Qua();
    Construction plan1;
    Builder builder1;
    Repository repo = new InMemoryRepository("");
    Repository fileBasedRepo;
    Description goal;
    Description specialization;
    Description testServiceBuilder;
    Description testServiceMapBuilder;
    TestService service1;
    Description resultDesc;

    @Given("description with {string} {string} {string} {string} {string}")
    public void description_with(String type, String value1, String value2, String plan, String service) {
        d = qua.type(type);
        setProperty(d,"property1", value1);
        setProperty(d,"property1", value2);
        switch (plan) {
            case "planWithNoDependencies": d.setConstruction(getPlan1()); break;
            case "planWithUnprovisionedDependencies": d.setConstruction(getPlan2()); break;
        }
        switch (service) {
            case "svc1": d.setServiceObject("service1");
        }
    }

    private Construction getPlan1() {
        plan1 = new ConstructionImpl(
                qua.namedService("plan1", builder1),
                new HashMap<>());
        return plan1;
    }

    private Construction getPlan2() {
        plan1 = new ConstructionImpl(
                qua.namedService("plan1", builder1),
                new HashMap<>());
        plan1.dependencies().put("dependency1", qua.type("unplanned"));
        return plan1;
    }

    private void setProperty(Description d, String key, String value) {
        if (value != null) {
            d.setProperty(key, value);
        }
    }

    @When("description is constructed")
    public void description_is_constructed() {
        assert(d != null);
    }

    @Then("status is {string}")
    public void status_is(String expectedStatus) {
        assertTrue(d.status().equalsIgnoreCase(expectedStatus));
    }

    @Given("qua has in memory repository")
    public void qua_has_in_memory_repository() {
        repo = new InMemoryRepository("");
        qua.addRepository(repo);
    }

    @Given("repository with implementations X and Y")
    public void repository_with_implementations_x_and_y() {
        // technically, these are implementations for types X and Y
        repo.advertise(qua.typedService("X", new TestSvc("XName")));
        repo.advertise(qua.typedService("Y", new TestSvc("YName")));
    }

    class TestBuilder extends AbstractPassiveServiceBuilder {
        @Override
        public void assemble(Description impl) {
            TestSvc svc = new TestSvc(
                    ((TestSvc)d.dependency("X")).name() +
                            ((TestSvc)d.dependency("Y")).name());
            d.setInterface("repositoryName", svc);
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
    class TestSvc {
        private final String name;

        public TestSvc(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }
    }

    @Given("description for type Z")
    public void description_for_type_z() {
        d = qua.type("Z");
    }

    @Given("Z implementation with dependencies Y and X")
    public void z_implementation_with_dependencies_y_and_x() {
        Description zBuilder = qua.typedService("Z", new TestBuilder());
        Description z = qua.type("Z")
                .setBuilder(zBuilder)
                .setDependency("X", qua.type("X"))
                .setDependency("Y", qua.type("Y"));
        repo.advertise(z);
    }

    @When("description is asked to {string}")
    public void description_is_asked_to(String action) throws NoImplementationFound {
        switch(action) {
            case "plan": d.plan(qua); break;
            case "provision": d.provision(qua); break;
            case "assemble": d.assemble(qua); break;
            case "activate": d.activate(qua); break;
            case "disAssemble": d.activate(qua); d.disAssemble(); break;
        }
    }

    @Given("two descriptions with {string} and {string} with only {string}")
    public void two_descriptions_with_and_with_only(String property, String dependency, String difference) {
        d = descriptionWith(property, dependency);
        goal = descriptionWith(property, dependency);
        switch(difference) {
            case "no diff": break;
            case "first only typed": d.disAssemble().setConstruction(null); break;
            case "first only provisioned": d.disAssemble(); break;
            case "first type different": d.setType("different"); break;
            case "first missing prop": d.properties().remove("prop"); break;
            case "first prop type different": d.descriptionProperty("prop").setType("different"); break;
            case "first prop only typed": d.descriptionProperty("prop").disAssemble().setConstruction(null); break;
            case "first prop only provisioned": d.descriptionProperty("prop").disAssemble(); break;
            case "first dependency type different": d.descriptionDependency("dep").setType("different"); break;
            case "first dependency only typed": d.descriptionDependency("dep").disAssemble().setConstruction(null); break;
            case "first dependency only provisioned": d.descriptionDependency("dep").disAssemble(); break;
        }
    }

    private Description descriptionWith(String property, String dependency) {
        Object prop = null;
        switch(property) {
            case "aString": prop = "aString"; break;
            case "aNumber": prop = new Integer(99); break;
            case "aDescription": prop = qua.typedService("typeB", "aString");
        }
        Object dep = null;
        switch(dependency) {
            case "aString": dep = "aString"; break;
            case "aNumber": dep = new Integer(99); break;
            case "aDescription": dep = qua.typedService("typeC", "aString");
        }
        return qua.type("typeA")
                .setProperty("prop", prop)
                .setBuilder(qua.typedService("Z", new TestBuilder()))
                .setDependency("dep", dep);
    }

    @When("first is specialized for second")
    public void first_is_specialized_for_second() {
        specialization = d.specializedFor(goal);
    }

    @Then("specialization is {string}")
    public void specialization_is(String expectedResult) {
        switch(expectedResult) {
            case "null": assertTrue(specialization == null); break;
            case "match": assertTrue( specialization != null); break;
        }
        assertTrue(specialization != d && specialization != goal);
    }

    @Given("repository with TestService impl in active state")
    public void repository_with_testservice_impl_in_active_state() {
        repo.advertise(qua.typedService("TestService", new TestService("TestService", null)));
    }

    @Given("repository with test service builder")
    public void repository_with_test_service_builder() {
        testServiceBuilder = qua.typedService("TestServiceBuilder", new TestServiceBuilder());
        repo.advertise(testServiceBuilder);
    }

    @Given("Planned impl in planned state")
    public void planned_impl_in_planned_state() {
        // planned but not provisioned.  Has at lease one dependency that is only typed.
        Description childService = qua.type("TestService");  // we know impl in repo, but not provisioned here yet
        Description planned = qua.type("Planned")
                .setProperty("repositoryName", "Planned")
                .setBuilder(testServiceBuilder)
                .setDependency("child", childService);
        repo.advertise(planned);
    }

    @Given("Provisioned impl in provisioned state")
    public void provisioned_impl_in_provisioned_state() throws NoImplementationFound {
        // planned and all dependencies provisioned.
        Description childService = qua.type("TestService").provision(qua);
        Description planned = qua.type("Provisioned")
                .setProperty("repositoryName", "Provisioned")
                .setBuilder(testServiceBuilder)
                .setDependency("child", childService);
        repo.advertise(planned);
    }

    @Given("Assembled impl in assembled state")
    public void assembled_impl_in_assembled_state() throws NoImplementationFound {
        Description childService = qua.type("TestService");
        Description planned = qua.type("Assembled")
                .setProperty("repositoryName", "Assembled")
                .setBuilder(testServiceBuilder)
                .setDependency("child", childService)
                .assemble(qua);
        repo.advertise(planned);
    }

    @Given("ActiveNoPlan impl in active state")
    public void active_no_plan_impl_in_active_state() {
        TestService svc = new TestService("ActiveNoPlan", null);
        Description activeNoPlan = qua.type("ActiveNoPlan")
                .setServiceObject(svc);
        repo.advertise(activeNoPlan);
    }

    @Given("Active impl in active state")
    public void active_impl_in_active_state() throws NoImplementationFound {
        Description childService = qua.type("TestService");
        Description planned = qua.type("Active")
                .setProperty("repositoryName", "Active")
                .setBuilder(testServiceBuilder)
                .setDependency("child", childService)
                .activate(qua);
        repo.advertise(planned);
    }

    @Given("repository with active TestService named {string}")
    public void repository_with_active_test_service_named(String name) {
        repo.advertise(qua.namedService(name, new TestService(name, null)));
    }
    @Given("repository with active TestService named null")
    public void repository_with_active_test_service_named_null() {
        // this is guaranteed by planned TestService
    }

    @Given("repository with planned TestService impl")
    public void repository_with_planned_test_service_impl() {
        Description impl = qua.typeAndPlan("TestService", testServiceMapBuilder);
        Map<String,Object> map = new HashMap<>();
        map.put("repositoryName","in-mem-testObject");
        impl.setProperty("map", map);
        repo.advertise(impl);
    }

    @When("{string} activated service is requested")
    public void activated_service_is_requested(String type) throws NoImplementationFound {
        service1 = qua.type(type).service(qua, TestService.class);
    }

    @Then("service works for {string}")
    public void service_works_for(String name) {
        assertTrue(service1.name().startsWith(name));
    }

    @When("{string} and {string} activated service is requested")
    public void and_activated_service_is_requested(String name, String type) {
        Description d = new Description();
        if (type != null) {
            d.setType(type);
        }
        if (name != null) {
            d.setName(name);
        }
        try {
            service1 = (TestService) d.service(qua);
        } catch (NoImplementationFound noImplementationFound) {
            throw new IllegalStateException(noImplementationFound);
        }
    }

    @Then("service works for null")
    public void service_works_for_null() {
        assertTrue(service1.name().equals("in-mem-testObject()"));
    }


    @Given("repository with test service map builder")
    public void repository_with_test_service_map_builder() {
        testServiceMapBuilder = qua.typedService("TestServiceMapBuilder", new TestServiceMapBuilder());
        repo.advertise(testServiceMapBuilder);
    }

    @When("null and {string} activated service is requested")
    public void null_and_activated_service_is_requested(String type) {
        Description d = new Description();
        d.setType(type);
        try {
            service1 = (TestService) d.service(qua);
        } catch (NoImplementationFound noImplementationFound) {
            throw new IllegalStateException(noImplementationFound);
        }
    }

    @When("{string} and null activated service is requested")
    public void and_null_activated_service_is_requested(String name) {
        Description d = new Description();
        d.setName(name);
        try {
            service1 = (TestService) d.service(qua);
        } catch (NoImplementationFound noImplementationFound) {
            throw new IllegalStateException(noImplementationFound);
        }
    }

    @Given("qua has file based {string} repository with {string}")
    public void qua_has_file_based_repository_with(String resultType, String directory) {
        Description bDesc = null;
        switch (resultType) {
            case "Map":
                bDesc = qua.namedService("mapBuilder", new HashMapBuilder());
                fileBasedRepo = new FileBasedRepository(directory, "", bDesc, qua);
                break;
            case "Description":
                fileBasedRepo = new FileBasedDescriptionRepository(directory, "", qua);
                break;
            case "TestService":
                bDesc = qua.namedService("testServiceMapBuilder", new TestServiceMapBuilder());
                fileBasedRepo = new FileBasedRepository(directory, "", bDesc, qua);
                break;
            default: throw new IllegalArgumentException("unknown resultType: " + resultType);
        }
    }

    @When("service {string} is retrieved")
    public void service_is_retrieved(String name) throws NoImplementationFound {
        resultDesc = fileBasedRepo.implementationByName(name);
    }

    @Then("service instance type is {string}")
    public void service_instance_type_is(String type) {
        assertTrue(resultDesc.type().equals(type));
    }

}
