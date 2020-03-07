package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.Description;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


class Builder {

    private Repository repo;

    public Builder(Repository r) {
        repo = r;
    }

    public Description plan(Description desc) {
        return null;
    }

    public Object execute(Object blueprint, Map<String, Object> dependencies) {
        return null;  // no service created yet
    }
}
