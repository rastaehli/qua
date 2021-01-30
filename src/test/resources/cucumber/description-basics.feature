Feature: Does Description class support all basic ability to reflect on component state?
    - state transitions from unknown > typed > planned > provisioned > assembled > active
    - equality, conformance, and specialization to another Description

    Scenario Outline: Description constructors yield expected state
        Given description with <type> <prop1Val> <prop2Val> <plan> <service>
        When description is constructed
        Then status is <status>

        Examples:
            | type    | prop1Val | prop2Val | plan                                | service | status        |
            | "typeA" | "null"   | "null"   | "null"                              | "null"  | "typed"       |
            | "typeA" | "v1"     | "null"   | "null"                              | "null"  | "typed"       |
            | "typeA" | "v1"     | "v2"     | "null"                              | "null"  | "typed"       |
            | "typeA" | "null"   | "null"   | "planWithNoDependencies"            | "null"  | "provisioned" |
            | "typeA" | "null"   | "null"   | "planWithUnprovisionedDependencies" | "null"  | "planned"     |
            | "typeA" | "v1"     | "v2"     | "planWithNoDependencies"            | "svc1"  | "active"      |
            | "typeA" | "v1"     | "v2"     | "null"                              | "svc1"  | "active"      |

    Scenario Outline: Description can manipulate component state
        Given repository with implementations X and Y
        And description for type Z
        And Z implementation with dependencies Y and X
        When description is asked to <action>
        Then status is <status>

        Examples:
            | action | status        |
            | "null" | "typed"        |
            | "plan" | "planned"        |
            | "provision" | "provisioned"        |
            | "assemble" | "assembled"        |
            | "activate" | "active"        |
            | "disAssemble" | "provisioned"        |

