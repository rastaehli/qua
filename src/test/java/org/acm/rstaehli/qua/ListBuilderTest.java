package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;
import org.acm.rstaehli.qua.tools.Serializer;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;

public class ListBuilderTest {

    Repository repo;

    @Before
    public void setUp() throws NoImplementationFound {
        repo = new FileBasedRepository("src/main/test/resources/");
    }

    private Description readDescFromFile( String filename, String ... directory) throws Exception {
        return new Serializer().descriptionFromJsonFile(filename, directory);
    }

    @Test
    public void test_list_oneMap() throws Exception {
        Description pipeDesc = readDescFromFile("sharedDescription-oneStringMap", "src/test/resources/", "src/main/resources");
        List<Description> list = (List) pipeDesc.service(repo);
        assertTrue(list.size() == 1);
        Description stringMapDesc = list.get(0);
        Map<String, String> map = (Map)stringMapDesc.properties;
        assertTrue(map.get("TARGET_HOST").equals("test1.host.com"));
        assertTrue(map.get("TARGET_USER").equals("userAbc"));
        assertTrue(map.get("USER_PASSWORD").equals("${SECRET}"));
        assertTrue(map.get("CONFIG_FILE").equals("application.properties"));
    }

    @Test
    public void test_sharedDescription_multipleMaps() throws Exception {
        Description pipeDesc = readDescFromFile("sharedDescription-multipleMaps", "src/test/resources/", "src/main/resources");
        List<Description> list = (List) pipeDesc.service(repo);
        List<String> itemIds = Arrays.asList("first", "second", "third");
        for (Description desc: list) { // all list items have properties from sharedDescription
            Map<String,Object> map = desc.properties();
            assertTrue(map.get("prop1").equals("1"));
            assertTrue(map.get("prop2").equals("2"));
            assertTrue(itemIds.contains(map.get("listName")));
        }
    }

    @Test
    public void test_sharedDescription_nestedLists() throws Exception {
        Description pipeDesc = readDescFromFile("sharedDescription-nestedLists", "src/test/resources/", "src/main/resources");
        List<Description> list = (List) pipeDesc.service(repo);
        assertTrue(list.size() == 4);
        for (Description desc: list) { // all list items have properties from sharedDescription
            assertTrue( desc.properties().size() == 6);
            // all inherit the two level 0 property settings as these don't override local values
            assertTrue( desc.stringProperty("level0prop1").equals("Level0Val1"));
            assertTrue( desc.stringProperty("level0prop2").equals("Level0Val2"));
            // all inherit the first level 1 property, but not the second because lower levels already set that
            assertTrue( desc.stringProperty("level1prop1").equals("Level1List1Val1"));
            switch(desc.stringProperty("itemName")) {
            case "level2List1Item1":
            case "level2List1Item2":
                assertTrue( desc.stringProperty("level2prop1").equals("Level2List1Val1"));
                assertTrue( desc.stringProperty("level2prop2").equals("Level2List1Val2"));
                break;
            case "level2List2Item1":
            case "level2List2Item2":
                assertTrue( desc.stringProperty("level2prop1").equals("Level2List2Val1"));
                assertTrue( desc.stringProperty("level2prop2").equals("Level2List2Val2"));
                break;
            }
        }
    }

}
