# raire-java
Java implementation of RAIRE

PreReq: Java 11, Maven.

## Running the connector

Run instructions:
- Option 1: 
  - Go to au.org.democracydevelopers.raise.RaireJavaApplication class and run the main method from IDE
- Option 2:
  - Go to home directory of the service and run following command
     `./mvnw spring-boot:run`

This will run the application on port 8080.

## Running RAIRE

You need to have a running copy of the [Raire webserver](https://github.com/DemocracyDevelopers/raire-rs).
If you are also running the colorado-rla client at the same time, ensure that it is running on a different port - by default, they use the same port.

For example, you might choose to run raire on port 3001 by starting it with
```
raire-webserver --socket 3001
```

If you are also running the colorado-rla client at the same time, ensure that they are running on different ports - by default, they use the same port.

For example, you might choose to run raire on port 3001 by starting it with
```
./target/release/raire-webserver --socket 3001
```

If you do so, you need to change application.yml correspondingly. Set the raire line to:
```
raire:
 url: http://localhost:3001/raire
```
or whatever other non-default port number you chose.

## Running the database

The rla-raire-connector service retrieves data from the database set up by colorado-rla. This is also defined in `application.yml`. Ensure that the url, username and password match the database colorado-rla is writing to.

## Generating assertions from corla
You can request assertions for any IRV contest already present in the colorado-rla database.

1. Log in as a stateadmin.
2. From the browser where you are logged in as stateadmin, visit [http://localhost:8888/generate-assertions](http://localhost:8888/generate-assertions).


## Generating assertions from the command line 
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
