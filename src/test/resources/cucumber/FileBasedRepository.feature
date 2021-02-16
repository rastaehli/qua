Feature: can read Descriptions, JobModels and other types from json files

    Scenario Outline: directory has json implementation files
        Given qua has file based <resultType> repository with <directory>
        When service <name> is retrieved
        Then service instance type is <type>

        Examples:
        | directory | resultType | name | type |
        | "src/test/resources/descriptionCases" | "Description" | "minimalPlan" | "gitlabBuildAutomation" |
        | "src/test/resources/descriptionCases" | "Description" | "planWithDependencies" | "gitlabBuildAutomation" |
        | "src/test/resources/repositoryTest" | "TestService" | "testSvc1" | "TestService" |

