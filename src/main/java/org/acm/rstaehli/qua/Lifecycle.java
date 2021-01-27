package org.acm.rstaehli.qua;

public enum Lifecycle {
    UNKNOWN,  // don't know type yet
    TYPED, // know type behavior should conform to
    PLANNED, // know how to construct implementation
    PROVISIONED, // know we have all dependencies for construction
    ASSEMBLED, // know interfaces to access service
    ACTIVE; // know interfaces are ready

    public static Lifecycle min(Lifecycle one, Lifecycle other) {
        if (one.ordinal() <= other.ordinal()) {
            return one;
        } else {
            return other;
        }
    }
}
