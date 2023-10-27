# raire-java
Java implementation of RAIRE

PreReq: Java 11, Maven.

Run instructions:
- Option 1: 
  - Go to au.org.democracydevelopes.raise.RaireJavaApplication class and run the main method from IDE
- Option 2:
  - Go to home directory of the service and run following command
     `./mvnw spring-boot:run`

This will run the application on port 8080.

You need to have a running copy of the [Raire webserver](https://github.com/DemocracyDevelopers/raire-rs).

- To run the audit please follow the syntax in following curl
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
                    "WRITE-IN"
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