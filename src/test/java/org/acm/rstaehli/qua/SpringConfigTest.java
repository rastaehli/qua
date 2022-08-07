package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.exceptions.NoImplementationFound;
import org.junit.Test;
import org.junit.Before;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static junit.framework.TestCase.assertTrue;
import static org.acm.rstaehli.qua.QuaConfig.ApplicationProperties;

/**
 * test "Spring" configuration functions like:
 * - accessing configuration properties
 * - constructing program dependencies and "injecting" into the objects that need them
 */
public class SpringConfigTest {

    QuaConfig config;

    @Before
    public void setUp() {
        config = new QuaConfig(); // advertise configuration services
    }
    @Test
    public void test_accessProperties_fromApplicationDotProperties() throws NoImplementationFound {
        writeFile("application.properties", "prop.one=value one\nprop.two=another");
        Properties appProps = (Properties) config.type(ApplicationProperties).service(config);
        assertTrue(appProps != null);
        assertTrue(appProps.size() == 2);
        assertTrue( appProps.getProperty("prop.one").equals("value one"));
        assertTrue( appProps.getProperty("prop.two").equals("another"));
    }

    private void writeFile(String filename, String contents) {
        try (
                FileOutputStream out = new FileOutputStream(filename);
                ) {
            out.write(contents.getBytes(StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
