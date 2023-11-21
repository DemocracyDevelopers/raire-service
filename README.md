# raire-java
Java implementation of RAIRE

PreReq: Java 11, Maven.


## Running the raire service

Run instructions:
- Option 1: 
  - Go to au.org.democracydevelopers.raise.RaireJavaApplication class and run the main method from an IDE
  - If there are compile issues you might need to:
    1. Right-click on `raire-java/pom.xml` (the sub-project pom.xml) and add it as a Maven project,
    2. Right-click on `raire-service/pom.xml` (the parent project pom.xml) and Maven -> reload,
    3. Then recompile.
- Option 2:
  - Go to the home directory of the service and run following command
    `./mvnw spring-boot:run`
  - If there are compile issues you might need to follow the instructions for [multi-module maven projects](https://www.baeldung.com/maven-multi-module).

This will run the application on port 8080.  (If you want to change the port, reset `server` in `application.yml`.)

## Configuring and running colorado-rla with the raire-service
The prototype `colorado-rla` edits are on the **scratch** branch.

1. Clone [https://github.com/DemocracyDevelopers/colorado-rla/tree/scratch](https://github.com/DemocracyDevelopers/colorado-rla/tree/scratch)
2. Follow the dev installation instructions at 
[https://github.com/DemocracyDevelopers/colorado-rla/blob/scratch/docs/25_developer.md](https://github.com/DemocracyDevelopers/colorado-rla/blob/scratch/docs/25_developer.md)
particularly `Install and Setup.`
4. If you are running the `raire-service` on a different computer, update the `raire_url` in
`server/eclipse-project/src/test/resources/test.properties` and 
`server/eclipse-project/src/main/resources/us/freeandfair/corla/default.properties`.
If you are running on localhost, you do not need to change anything.

## Generating assertions from corla
You can request assertions for any IRV contest already present in the colorado-rla database.

1. Log in as a stateadmin.
2. Go through the steps of defining an audit. When you reach the `Generate Assertions` page,
click the `Generate Assertions` button. This should save the assertions in the database and return a json
file describing the assertions.

## Generating assertions from the command line 
## TODO - out of date - update for vote data.
Alternatively, you can request assertions directly via the command line.

- Follow the syntax in following curl

```
curl --location 'localhost:8080/cvr/audit' \
--header 'Content-Type: application/json' \
--data '[
{
"contestName": "Denver Mayoral",
"timeProvisionForResult": 10
}
]'
```

your output should look similar to  
```
[
    {
        "contestName": "Denver Mayoral",
        "result": {
            "metadata": {
                "candidates": [
                    "CANDIDATE 1",
                    "CANDIDATE 2",
                    "CANDIDATE 3"
                ]
            },
            "solution": {
                "Ok": {
                    "winner": 1,
                    "margin": 4,
                    "difficulty": 78.0,
                    "assertions": [
                        {
                            "margin": 48,
                            "difficulty": 6.5,
                            "assertion": {
                                "type": "NEB",
                                "winner": 1,
                                "loser": 0
                            }
                        },
                        {
                            "margin": 4,
                            "difficulty": 78.0,
                            "assertion": {
                                "type": "NEB",
                                "winner": 1,
                                "loser": 2
                            }
                        },
                    ],
                    "num_candidates": 13,
                    "time_to_determine_winners": {
                        "work": 13,
                        "seconds": "0.000039899"
                    },
                    "time_to_find_assertions": {
                        "work": 25,
                        "seconds": "0.000454404"
                    },
                    "time_to_trim_assertions": {
                        "work": 24,
                        "seconds": "0.00002241999999999997"
                    }
                }
            }
        }
    }
]
```
