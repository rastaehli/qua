package com.expd.tools;

import java.util.ArrayList;
import java.util.List;

public class JobSpec {
    private String name;
    private String stageName;
    private List<WhenCondition> whenConditions;

    public JobSpec(String name, String stageName) {
        this.name = name;
        this.stageName = stageName;
        this.whenConditions = new ArrayList<>();
    }

    public void addExceptRef(String ref) {
        whenConditions.add(new WhenCondition(WhenCondition.EXCEPT_TYPE, ref));
    }

    public void append(StringBuilder sb) {
        sb
            .append(this.name)
                .append(":\n\tstage: ")
                .append(this.stageName)
            .append(":\n");
        boolean needsConditionPreamble = true;
        for (WhenCondition c : exceptConditions(whenConditions)) {
            if (needsConditionPreamble) {
                sb.append("\texcept\n\t\trefs\n");
            }
            sb.append("\t\t\t- ");
            sb.append(c.refName());
            sb.append("\n");
        }
        appendScript(sb);
    }

    private void appendScript(StringBuilder sb) {
        sb.append("\tscript:\n");
        sb.append("\t\t- mvn compile -B -U\n");  // this works only for compile job TODO: move to subclass
    }

    private boolean hasExcept(List<WhenCondition> whenConditions) {
        for (WhenCondition c : whenConditions) {
            if (c.isExcept()) {
                return true;
            }
        }
        return false;
    }

    private List<WhenCondition> exceptConditions(List<WhenCondition> whenConditions) {
        List<WhenCondition> excepts = new ArrayList<WhenCondition>();
        for (WhenCondition c : whenConditions) {
            if (c.isExcept()) {
                excepts.add(c);
            }
        }
        return excepts;
    }
}
