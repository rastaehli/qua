import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.acm.rstaehli.qua.*;
import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

import java.util.HashMap;

import static junit.framework.TestCase.assertTrue;

public class StepDefinitions {

    Description d;
    Qua qua = new Qua();
    Construction plan1;
    Builder builder1;
    InMemoryRepository repo = new InMemoryRepository();
    Description goal;
    Description specialization;

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

    @Given("repository with implementations X and Y")
    public void repository_with_implementations_x_and_y() {
        // technically, these are implementations for types X and Y
        repo = new InMemoryRepository();
        qua.addRepository(repo);
        repo.advertise(qua.typedService("X", new TestSvc("XName")));
        repo.advertise(qua.typedService("Y", new TestSvc("YName")));
    }

    class TestBuilder implements Builder {
        @Override
        public void assemble(Description impl) {
            TestSvc svc = new TestSvc(
                    ((TestSvc)d.dependency("X")).name() +
                            ((TestSvc)d.dependency("Y")).name());
            d.setInterface("name", svc);
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


}
