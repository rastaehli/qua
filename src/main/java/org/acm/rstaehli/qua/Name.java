package org.acm.rstaehli.qua;

/**
 * Support RDF naming, construction, operations to extract useful parts of these long names.
 */
public class Name {

    public static String keyPart(String name) {
        if (name.contains("/")) {  // assume like http://werver/path/keyPart
            String[] parts = name.split("/");
            return parts[parts.length-1];
        }
        if (name.contains(":")) {  // assume like alias:keyPart
            String[] parts = name.split(":");
            return parts[parts.length-1];
        }
        return name;
    }

}
