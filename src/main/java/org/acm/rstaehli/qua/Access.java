package org.acm.rstaehli.qua;

import java.util.Map;

public interface Access {
    Object service();  // primary service interface (local object reference)
    Map<String, String> interfaces();  // interface id lookup by role name
}
