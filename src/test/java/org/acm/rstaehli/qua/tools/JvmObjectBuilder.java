package org.acm.rstaehli.qua.tools;

import org.acm.rstaehli.qua.AbstractPassiveServiceBuilder;
import org.acm.rstaehli.qua.Builder;
import org.acm.rstaehli.qua.Description;
import org.acm.rstaehli.qua.Plan;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Builds a local JVM object of type aClass with the given constructorArgs.
 * For example, a Point with arguments Integer 5 and Integer -8.
 */
class JvmObjectBuilder extends AbstractPassiveServiceBuilder {

    public static String BUILD_NAMESPACE = "http://org.acm.rstaehli/ns/build/";

    public static JvmObjectBuilder singleton = null;

    public static JvmObjectBuilder getInstance() {
        if (singleton == null) {
            singleton = new JvmObjectBuilder();
        }
        return singleton;
    }

    public static Description forConstructor(Class aClass, List<Class> constructorArgTypes, List<Object> constructorArgValues) {
        Description d = new Description();
//        d.type = aClass.getName();
//        d.serviceObject = getInstance();
//        d.dependencies = new HashMap<>();
//        d.dependencies.put("constructorArgTypes", constructorArgTypes);
//        d.dependencies.put("constructorArgValues", constructorArgValues);
        return d;
    }

    /**
     * Return first constructor with the expected argument signature
     * @param className
     * @param constructorArgTypes
     * @return
     * @throws ClassNotFoundException
     */
    public Constructor sift(String className, List<Class> constructorArgTypes) {
        try {
            Class<?> c = Class.forName(className);
            Constructor[] allConstructors = c.getDeclaredConstructors();
            for (Constructor ctor : allConstructors) {
                Class<?>[] pType = ctor.getParameterTypes();
                if (pType.length == constructorArgTypes.size()) {
                    ArrayList<Class> types = new ArrayList<>(constructorArgTypes);
                    boolean allMatch = true;
                    for (int i = 0; i < pType.length; i++) {
                        if (!pType[i].equals(types.get(i).getName())) {
                            allMatch = false;
                            break;
                        }
                    }
                    if (allMatch) {
                        return ctor;
                    }
                }
            }
            // production code should handle this exception more gracefully
        } catch(ClassNotFoundException x){
            return null;
        }
        return null;
    }

    public void assemble(Description impl) {
        Constructor ctor = sift(impl.type(), (List<Class>)impl.dependencies().get("constructorArgTypes"));
        try {
            impl.setServiceObject( ctor.newInstance(impl.dependencies().get("constructorArgValues")) );
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
