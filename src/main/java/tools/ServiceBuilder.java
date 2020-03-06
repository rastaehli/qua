package tools;

import java.util.Map;

/**
 * Generic builder for a service Description.
 * Depends on local repository of service plans.
 */
public class ServiceBuilder extends Builder {
    public ServiceBuilder(Map<String, Object> o) {
        super(o);
    }
}
