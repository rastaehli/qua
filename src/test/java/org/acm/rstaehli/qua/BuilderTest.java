package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.tools.Serializer;
import org.junit.Before;
import org.junit.Test;
import org.acm.rstaehli.qua.exceptions.NoImplementationFound;

import java.io.FileNotFoundException;
import java.io.IOException;

import static junit.framework.TestCase.assertTrue;

public class BuilderTest {
    private Description desc;
    private Repository repo;
    private Serializer serializer;

    @Before
    public void setUp() throws IOException {
        repo = new Repository();
        serializer = new Serializer();
        serializer.setRepo(repo);
    }

    private Description fromCase(String caseName) {
        try {
            return serializer.descriptionFromJsonFile("src/test/resources/descriptionCases/" + caseName + ".json");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Test(expected = NoImplementationFound.class)
    public void test_plan_noImplementation() throws NoImplementationFound {
        desc = fromCase("noImplementation");
        assertTrue(desc.isTyped());
        desc.plan(repo);
        assertTrue(!desc.isPlanned());
    }
}
