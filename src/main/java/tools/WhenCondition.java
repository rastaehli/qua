package tools;

public class WhenCondition {
    public static String EXCEPT_TYPE = "except";

    private String type;
    private String refName;

    public WhenCondition(String type, String ref) {
        this.type = type;
        this.refName = ref;
    }

    public boolean isExcept() {
        return type == EXCEPT_TYPE;
    }

    public String refName() {
        return this.refName;
    }
}
