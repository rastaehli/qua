package org.acm.rstaehli.qua;

import org.acm.rstaehli.qua.tools.Serializer;

import java.io.FileNotFoundException;
import java.util.List;

public class Repository {
    public Description lookupByName(String name) throws FileNotFoundException {
        return Serializer.descriptionFromJsonFile("src/test/resources/descriptionCases/" + name + ".json", this);
    }
}
