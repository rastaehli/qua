import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * maven test goal runs this unit test.
 * The Cucumber runner looks for .feature files
 *  finds test/resources/cucumber/....feature
 * then looks for StepDefinitions class to map "Gherkin" language test expressions to java functions.
 * You can debug the java code normally with break points to see how the tests execute.
 * In intellij, right click on a test case title in the .feature file for the option to run one test.
 */
@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty"})
public class RunCucumberTest {

}
