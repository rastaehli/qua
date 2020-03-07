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

    @Before
    public void setUp() throws IOException {
    }

    private Description fromCase(String caseName) {
        try {
            return Serializer.descriptionFromJsonFile("src/test/resources/descriptionCases/" + caseName + ".json");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Test(expected = NoImplementationFound.class)
    public void test_plan_noImplementation() {
        desc = fromCase("noImplementation");
        assertTrue(desc.typed());
        Description planned = Builder.plan(desc);
    }
}
