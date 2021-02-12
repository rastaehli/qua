Feature: can read Descriptions, JobModels and other types from json files

    Scenario Outline: directory has descriptions
        Given qua has file based repository with <directory> and <builder>
        When service <name> is retrieved
        Then service instance type is <type>

        Examples:
        | directory | builder | name | type |
        | "src/test/resources/descriptionCases" | "DescriptionBuilder" | "minimalPlan" | "Description" |
        | "src/test/resources/descriptionCases" | "DescriptionBuilder" | "planWithDependencies" | "Description" |
        | "src/test/resources/repositoryTest" | "TestServiceBuilder" | "testSvc1" | "TestService" |

