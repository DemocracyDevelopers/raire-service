# raire-java
Java implementation of RAIRE

PreReq: Java, Maven. This requires Java at least version 11 and has been tested with Java 18.

The Springboot tests in RAIREMicroserviceAPITests.java additionally require Java 17+ and libtcnative.

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

### Generating assertions from the command line
To test that the RAIRE service is running correctly, go to the `testAPI` directory an run
```agsl
./testScript.sh
```
Your output should look like:
```
File: GuideToRaireEx1.json
{"metadata":{"candidates":["Alice","Bob","Chuan","Diego"],"contest":"GuideToRAIREExample1","totalAuditableBallots":15},"solution":{"Ok":{"assertions":[{"assertion":{"type":"NEB","winner":0,"loser":1},"difficulty":5.0,"margin":3},{"assertion":{"type":"NEN","winner":3,"loser":0,"continuing":[0,3]},"difficulty":15.0,"margin":1},{"assertion":{"type":"NEN","winner":0,"loser":2,"continuing":[0,2,3]},"difficulty":7.5,"margin":2},{"assertion":{"type":"NEN","winner":3,"loser":1,"continuing":[0,1,3]},"difficulty":3.75,"margin":4},{"assertion":{"type":"NEN","winner":3,"loser":2,"continuing":[0,2,3]},"difficulty":15.0,"margin":1},{"assertion":{"type":"NEN","winner":3,"loser":1,"continuing":[0,1,2,3]},"difficulty":7.5,"margin":2}],"difficulty":15.0,"margin":1,"winner":3,"num_candidates":4,"time_to_determine_winners":{"work":4,"seconds":0.0},"time_to_find_assertions":{"work":13,"seconds":0.0},"time_to_trim_assertions":{"work":36,"seconds":0.001},"warning_trim_timed_out":false}}}
File: TrivialExample.json
{"metadata":{"candidates":["Alice","Bob"],"contest":"TrivialExample1","totalAuditableBallots":15},"solution":{"Ok":{"assertions":[{"assertion":{"type":"NEB","winner":0,"loser":1},"difficulty":7.5,"margin":2}],"difficulty":7.5,"margin":2,"winner":0,"num_candidates":2,"time_to_determine_winners":{"work":2,"seconds":0.0},"time_to_find_assertions":{"work":1,"seconds":0.001},"time_to_trim_assertions":{"work":2,"seconds":0.0},"warning_trim_timed_out":false}}}
```

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

1. Log in to colorado-rla as countyadminN, for as many different N as you like. 
2. Upload some example IRV CVRs, one per county. You can use the test CSVs, manifests and sha256sums in
   [the NSW test data directory](https://github.com/DemocracyDevelopers/Utilities-and-experiments/tree/main/src/main/resources/test-data) 
   or [the Colorado-rla example data directory](https://github.com/DemocracyDevelopers/colorado-rla/tree/main/test/IRV-test) or make your own.
3. Log in to colorado-rla as a stateadmin.
4. Go through the steps of defining an audit. You may need a canonical list file - use either
[the NSW test one](https://github.com/DemocracyDevelopers/colorado-rla/tree/scratch/test/NSW2021Data) or [the Colorado-rla example one](https://github.com/DemocracyDevelopers/colorado-rla/blob/main/test/IRV-test/IRV_Test_Canonical_List.csv) or make your own. 
5. When you reach the `Generate Assertions` page,
click the `Generate Assertions` button. This should save the assertions in the database. 
It takes between 5 and 30 seconds, depending on how many votes are relevant.
6. You can retrieve the json for the assertions by visiting [http://localhost:8888/generate-assertions](http://localhost:8888/generate-assertions).
You might like to visualise each one using the [Assertion Explainer](https://democracydevelopers.github.io/raire-rs/WebContent/explain_assertions.html). (Make sure you enter each contest's raire response individually, not the whole list.)