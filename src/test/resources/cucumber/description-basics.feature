Feature: Does Description class support all basic ability to reflect on component state?
    - state transitions from unknown > typed > planned > provisioned > assembled > active
    - equality, conformance, and specialization to another Description

    Scenario Outline: Description constructors yield expected state
        Given qua has in memory repository
        And description with <type> <prop1Val> <prop2Val> <plan> <service>
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
        Given qua has in memory repository
        And repository with implementations X and Y
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

    Scenario Outline: Description activates from any state
        Given qua has in memory repository
        And repository with test service builder
        And repository with TestService impl in active state
        And Planned impl in planned state
        And Provisioned impl in provisioned state
        And Assembled impl in assembled state
        And Active impl in active state
        And ActiveNoPlan impl in active state
        When <type> activated service is requested
        Then service works for <type>

        Examples:
            | type |
            | "Planned" |
            | "Provisioned" |
            | "Assembled" |
            | "Active" |
            | "ActiveNoPlan" |

    Scenario Outline: Description supports specialization to match another
        Given qua has in memory repository
        And two descriptions with <prop> and <dependency> with only <difference>
        When first is specialized for second
        Then specialization is <result>

        Examples:
            | prop      | dependency | difference                          | result  |
            | "aString" | "aDescription" | "no diff"                           | "match" |
            | "aString" | "aDescription" | "first only typed"                  | "match" |
            | "aString" | "aDescription" | "first only provisioned"            | "match" |
            | "aString" | "aDescription" | "first type different"              | "null"  |
            | "aString" | "aDescription" | "first missing prop"                | "null"  |
            | "aDescription" | "aDescription" | "first prop type different"         | "null"  |
            | "aDescription" | "aDescription" | "first prop only typed"             | "match" |
            | "aDescription" | "aDescription" | "first prop only provisioned"       | "match" |
            | "aString" | "aDescription" | "first dependency type different"   | "match"  |
            | "aString" | "aDescription" | "first dependency only typed"       | "match" |
            | "aString" | "aDescription" | "first dependency only provisioned" | "match" |

    Scenario Outline: Description activates by name or type
        Given qua has in memory repository
        And repository with test service map builder
        And repository with active TestService named <name>
        And repository with planned TestService impl
        When <name> and <type> activated service is requested
        Then service works for <name>

        Examples:
            | name | type |
            | "anyName" | "TestService" |
            | null | "TestService" |
            | "anyName" | null |
