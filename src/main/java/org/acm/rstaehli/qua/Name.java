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

    // return just the part after the repositoryName prefix
    public static String keyPart(String name, String repositoryName) {
        if (name.startsWith(repositoryName)) {
            return name.substring(repositoryName.length());
        }
        return name;
    }

    public static String prefix(String name) {
        if (name == null) {
            return "";
        }
        return name.substring(0, name.length() - keyPart(name).length());
    }
}
