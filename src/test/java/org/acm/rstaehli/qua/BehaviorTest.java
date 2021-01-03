package org.acm.rstaehli.qua;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.acm.rstaehli.qua.Behavior.MATCH_ANY;
import static org.acm.rstaehli.qua.Behavior.UNKNOWN_TYPE;

public class BehaviorTest {
    private Behavior behavior;

    @Before
    public void setUp() throws IOException {
    }

    @Test
    public void test_type() {
        behavior = new BehaviorImpl();
        assertTrue(behavior.type().equals(UNKNOWN_TYPE));
        assertTrue(!behavior.isTyped());
        behavior.setType("foo");
        assertTrue(behavior.isTyped());
        assertTrue(behavior.type().equals("foo"));
    }

    @Test
    public void test_properties() {
        behavior = new BehaviorImpl();
        assertTrue(behavior.type().equals(UNKNOWN_TYPE));
        assertTrue(behavior.properties() == null);
        assertTrue(!behavior.hasProperty("bar"));
        behavior.setType("foo");
        assertTrue(behavior.isTyped());
        assertTrue(behavior.properties() == null);
        assertTrue(!behavior.hasProperty("bar"));
        behavior.setProperty("bar", "baz");
        assertTrue(behavior.properties().size() == 1);
        assertTrue(behavior.hasProperty("bar"));
        assertTrue(behavior.getProperty("bar").equals("baz"));
        behavior.setProperties(new HashMap());
        assertTrue(behavior.properties().size() == 0);
        assertTrue(!behavior.hasProperty("bar"));
    }

    @Test
    public void test_constructors() {
        behavior = new BehaviorImpl();
        behavior.setType("testType");
        assertTrue(behavior.equals(new BehaviorImpl("testType")));
        behavior = new BehaviorImpl();
        behavior.setType("testType");
        behavior.setProperty("color", "red");
        behavior.setProperty("emotion", "embarassed");
        Map<String, Object> props = new HashMap<>();
        props.put("color", "red");
        props.put("emotion", "embarassed");
        assertTrue(behavior.equals(new BehaviorImpl("testType", props)));
    }

    @Test
    public void test_mergeBehavior() {
        // mergeBehavior method copies only behavior attributes not already set
        behavior = new BehaviorImpl();
        behavior.mergeBehavior(new BehaviorImpl("testType"));
        assertTrue(behavior.equals(new BehaviorImpl("testType")));

        Map<String, Object> props = new HashMap<>();
        props.put("color", "red");
        props.put("emotion", "embarassed");
        Behavior withProps = new BehaviorImpl("otherType", props);

        behavior = new BehaviorImpl("testType");
        behavior.mergeBehavior(withProps);
        // check that it copied properties, but not type because that was already set
        assertTrue(behavior.equals(new BehaviorImpl("testType", props)));

        behavior = new BehaviorImpl("testType");
        behavior.setProperty("color", "blue");
        behavior.mergeBehavior(withProps);
        // check that it copied only the property not already set
        assertTrue(behavior.properties().size() == 2);
        assertTrue(behavior.stringProperty("color").equals("blue"));
        assertTrue(behavior.stringProperty("emotion").equals("embarassed"));
    }

    @Test
    public void test_specializeFor() {
        Map<String, Object> props = new HashMap<>();
        props.put("color", "red");
        props.put("emotion", "embarassed");
        Behavior goal = new BehaviorImpl("testType", props);

        // should replace ANY_PROPERTIES with goal properties
        behavior = new BehaviorImpl("testType", BehaviorImpl.ANY_PROPERTIES);
        assertTrue(behavior.specializeFor(goal).equals(goal));

        // should return null if behavior does not conform to goal
        Map<String, Object> otherProps = new HashMap<>();
        otherProps.put("color", "blue");
        otherProps.put("emotion", "sad");
        behavior.setProperties(otherProps);
        Behavior specialized = behavior.specializeFor(goal);
        assertTrue(specialized == null);

        // should replace properties with wild card MATCH_ANY value
        behavior = new BehaviorImpl("testType");
        behavior.setProperty("color", MATCH_ANY);
        behavior.setProperty("emotion", "embarassed");
        specialized = behavior.specializeFor(goal);
        assertTrue(specialized.properties().size() == 2);
        assertTrue(specialized.equals(goal));
        assertTrue(specialized.stringProperty("color").equals("red"));
        assertTrue(specialized.stringProperty("emotion").equals("embarassed"));
    }

}
