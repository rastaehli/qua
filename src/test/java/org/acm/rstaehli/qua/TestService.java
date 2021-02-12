package org.acm.rstaehli.qua;

public class TestService {
    private String name;
    private TestService child;
    public TestService(String name, TestService child) {
        this.name = name;
        this.child = child;
    }
    public String name() {
        return name + "(" + (child==null?"":child.name()) + ")";
    }
}
