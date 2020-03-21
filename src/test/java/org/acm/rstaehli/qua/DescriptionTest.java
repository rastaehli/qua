package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;
import org.acm.rstaehli.qua.tools.Serializer;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertTrue;

public class DescriptionTest {
    private Description desc;
    private Repository repo;
    private Serializer serializer;

    @Before
    public void setUp() throws IOException {
        repo = new FileBasedRepository("src/test/resources/descriptionCases/");
//        repo.advertise(JvmObjectBuilder(Class cls, List<Class> constructorArgTypes, List<Object> ));
        serializer = new Serializer();
    }

    @Test
    public void test_serviceObject_string() throws NoImplementationFound {
        desc = repo.implementationByName("serviceObjectOnly");
        assertTrue(desc.isActive());
        assertTrue(desc.serviceObject instanceof String);
        assertTrue(desc.serviceObject.equals("12345"));
    }
}
