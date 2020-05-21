package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;
import org.acm.rstaehli.qua.tools.Serializer;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;

public class StringMapBuilderTest {

    Repository repo;

    @Before
    public void setUp() throws NoImplementationFound {
        repo = new FileBasedRepository("src/main/test/resources/");
    }

    private Description readDescFromFile( String filename, String ... directory) throws FileNotFoundException {
        return new Serializer().descriptionFromJsonFile(filename, directory);
    }

    @Test
    public void test_sharedDescription_deployJobVariables() throws Exception {
        Description pipeDesc = readDescFromFile("stringMapBuilder-simple", "src/test/resources/", "src/main/resources");
        Map<String,String> map = (Map)pipeDesc.service(repo);
        assertTrue(map.get("TARGET_HOST").equals("test1.host.com"));
        assertTrue(map.get("TARGET_USER").equals("userAbc"));
        assertTrue(map.get("USER_PASSWORD").equals("${SECRET}"));
        assertTrue(map.get("CONFIG_FILE").equals("application.properties"));
    }
}
