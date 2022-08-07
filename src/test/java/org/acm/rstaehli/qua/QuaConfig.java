package org.acm.rstaehli.qua;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class QuaConfig extends InMemoryRepository {
        public static final String ApplicationProperties = "ApplicationProperties";
        private static final String PropertiesBuilder = "PropertiesBuilder";

        public QuaConfig() {
                super();
                this.advertise(this.typedPlan(
                        ApplicationProperties,
                        this.namedService(PropertiesBuilder, new PropertiesBuilder())));
        }

        class PropertiesBuilder extends DefaultBuilder {
                @Override
                public void assemble(Description impl) {
                        Properties props = new Properties();
                        try (
                                FileInputStream in = new FileInputStream("application.properties");
                                InputStreamReader x = new InputStreamReader(in, StandardCharsets.UTF_8);
                                Reader r = new BufferedReader(x, 4*1024);
                        ) {
                                props.load(r);
                        } catch (FileNotFoundException e) {
                                throw new RuntimeException(e);
                        } catch (IOException e) {
                                throw new RuntimeException(e);
                        }
                        impl.setServiceObject(props);
                }
        }

        class DefaultBuilder implements Builder {

                @Override
                public void assemble(Description impl) {

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
}
