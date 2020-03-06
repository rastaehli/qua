package org.acm.rstaehli.qua;

import com.google.gson.Gson;
import tools.Plan;
import tools.ServicePlanner;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Description is a meta object to reflect on and manage the implementation of a service.
 *
 * - type: names the logical behavior of the service
 * - properties: may contain additional attributes that restrict the type:
 * - plan: describes the implementation of the service.  It may also be executed to build the service.
 *
 * A service conforms to this description only if the type matches and it has all the listed properties.
 * When a service is built, it can be accessed by its "service" property.
 */
public class Description {

    private String type;
    private Plan plan;
    public Map<String, Object> properties;  // attributes guaranteed by the plan

    private ServicePlanner planner;

    public Description() {

    }

    public Description(Map<String, Object> jsonObject) {
        this.type = getType(jsonObject);
        this.plan = getPlan(jsonObject);
        this.planner = getPlanner(jsonObject);
        this.properties = getProperties(jsonObject);
    }

    public Description(String type, Plan plan) {
        this.type = type;
        this.plan = plan;
    }

    public static String getType(Map<String,Object> o) {
        if (o.containsKey("type")) {
            return (String)o.get("type");
        } else {
            return null;
        }
    }

    public static Plan getPlan(Map<String,Object> o) {
        if (o.containsKey("plan")) {
            return new Plan((Map<String,Object>)o.get("plan"));
        } else {
            return null;
        }
    }

    public static ServicePlanner getPlanner(Map<String,Object> o) {
        if (o.containsKey("planner")) {
            return (ServicePlanner)o.get("planner");
        } else {
            return new ServicePlanner();
        }
    }

    public static Map<String,Object> getProperties(Map<String,Object> o) {
        if (o.containsKey("properties")) {
            return (Map<String,Object>)o.get("properties");
        } else {
            return new HashMap<>();
        }
    }

    public static void shareInheritance(Description parent, Description desc) {
        if (desc.getType() == null & parent.getType() != null) {
            desc.type = parent.getType();
        }
        if (desc.getPlan() == null & parent.getPlan() != null) {
            desc.plan = parent.getPlan();
        }
        for (String name: parent.getProperties().keySet()) {
            if (!desc.getProperties().containsKey(name)) {
                desc.getProperties().put(name, parent.getProperties().get(name));
            }
        }
        if (desc.getPlanner() == null & parent.getPlanner() != null) {
            desc.planner = parent.getPlanner();
        }
    }

    private Object getProperty(String name) {
        return properties.get(name);
    }

    public String getType() {
        return type;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public Plan getPlan() {
        return plan;
    }

    public ServicePlanner getPlanner() {
        return planner;
    }

    public int getStatus() {
        if (plan == null) {
            return typed() ? Plan.TYPED : Plan.UNKNOWN;
        }
        return plan.getStatus();
    }

    public Object getService() {
        return getPlan().getService();
    }


    // operations to change implementation status

    public Description plan() {
        if (plan == null) {
            plan = planner.implementationFor(type).plan;
            return this;
        }
        // ensure all dependencies are fully planned
        if (!plan.planned()) {
            plan.plan();
        }
        return this;
    }

    public Description provision() {
        getPlan().provision();
        return this;
    }

    public Description assemble() {
        getPlan().assemble(this);
        return this;
    }

    public Description activate() {
        getPlan().activate(this);
        return this;
    }

    public boolean active() {
        if (plan == null) {
            return false;
        }
        return plan.active();
    }

    public boolean provisioned() {
        if (plan == null) {
            return false;
        }
        return plan.provisioned();
    }

    public boolean planned() {
        if (plan == null) {
            return false;
        }
        return plan.planned();
    }

    public boolean typed() {
        return type != null;
    }
}
