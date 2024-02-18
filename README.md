# raire-service
Java implementation of RAIRE

PreReq: Java, Maven. This requires Java at least version 17 and has been tested with Java 18.

The Springboot tests in RAIREMicroserviceAPITests.java additionally require Java 17+ and libtcnative.

## Compiling 

This project depends on another project, _raire-java_, as a library. Therefore, you
first have to compile _raire-java_.

```bash
git clone https://github.com/DemocracyDevelopers/raire-java.git
cd raire-java
git checkout dev
# TODO remove above line once it is merged into main.
mvn install
cd ..
```

This will make raire-java available to other maven projects on your computer.

Now you can compile raire-service:
```bash
git clone https://github.com/DemocracyDevelopers/raire-service.git
cd raire-service
git checkout DatabaseAccessForAssertions
# TODO remove above line once it is merged into main.
mvn compile
cd ..
```


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

## Generating assertions from the command line (endpoint: /raire/generate-and-get-assertions)
To test that the RAIRE service is running correctly, go to the `testAPI` directory and run
```agsl
./testGenerateAndGet.sh
```
Your output should look like:
```
File: GuideToRaireEx1.voteExample.json
{"metadata":{"candidates":["Alice","Bob","Chuan","Diego"],"contest":"GuideToRAIREExample1","totalAuditableBallots":15},"solution":{"Ok":{"assertions":[{"assertion":{"type":"NEB","winner":0,"loser":1},"difficulty":5.0,"margin":3},{"assertion":{"type":"NEN","winner":3,"loser":0,"continuing":[0,3]},"difficulty":15.0,"margin":1},{"assertion":{"type":"NEN","winner":0,"loser":2,"continuing":[0,2,3]},"difficulty":7.5,"margin":2},{"assertion":{"type":"NEN","winner":3,"loser":1,"continuing":[0,1,3]},"difficulty":3.75,"margin":4},{"assertion":{"type":"NEN","winner":3,"loser":2,"continuing":[0,2,3]},"difficulty":15.0,"margin":1},{"assertion":{"type":"NEN","winner":3,"loser":1,"continuing":[0,1,2,3]},"difficulty":7.5,"margin":2}],"difficulty":15.0,"margin":1,"winner":3,"num_candidates":4,"time_to_determine_winners":{"work":4,"seconds":0.0},"time_to_find_assertions":{"work":13,"seconds":0.0},"time_to_trim_assertions":{"work":36,"seconds":0.001},"warning_trim_timed_out":false}}}
File: GuideToRAIREExample3-raire-service.voteExample.json
{"metadata":{"candidates":["A","B","C","D"],"contest":"GuideToRaireExample3","totalAuditableBallots":225},"solution":{"Ok":{"assertions":[{"assertion":{"type":"NEB","winner":2,"loser":3},"difficulty":5.0,"margin":45},{"assertion":{"type":"NEN","winner":2,"loser":0,"continuing":[0,2]},"difficulty":9.0,"margin":25},{"assertion":{"type":"NEN","winner":0,"loser":1,"continuing":[0,1,2]},"difficulty":3.75,"margin":60},{"assertion":{"type":"NEN","winner":2,"loser":1,"continuing":[0,1,2]},"difficulty":5.0,"margin":45},{"assertion":{"type":"NEN","winner":0,"loser":3,"continuing":[0,1,2,3]},"difficulty":2.25,"margin":100}],"difficulty":9.0,"margin":25,"winner":2,"num_candidates":4,"time_to_determine_winners":{"work":4,"seconds":0.0},"time_to_find_assertions":{"work":13,"seconds":0.0},"time_to_trim_assertions":{"work":32,"seconds":0.001},"warning_trim_timed_out":false}}}
File: TrivialExample.voteExample.json
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
6. You can retrieve the assertions as a zip file by clicking the `Export Assertions` button.
You might like to unzip them and visualise each one using the [Assertion Explainer](https://democracydevelopers.github.io/raire-rs/WebContent/explain_assertions.html). 

## Generating and storing assertions from the command line (endpoint: /raire/generate-assertions)
You can generate and store assertions from the command line, if the relevant CVRs are already in the database. To test 
that this is working, go to the `testAPI` directory and run
```
./testGenerateAssertions.sh
```

You need to alter the examples to match some CVRs already in your database. For example, if you already have CVRs with
a countyID of 7, a contestID of 3962159, and a contest called `City of Boulder Mayoral`, the output of
`./testGenerateAssertions.sh` should look like:
```
File: EmptyRequest.generateAssertionRequest.json
{"contestName":"City of Boulder Mayoral","response":{"Err":"PlaceholderError"}}
File: GenerateAssertionRequest.generateAssertionRequest.json
{"contestName":"City of Boulder Mayoral","response":{"Ok":"Aaron Brockett"}}
```

## Retrieving stored assertions from the command line (endpoint: /raire/get-assertions)
You can also retrieve previously-generated assertions from the database. To test that this is working, go to the `testAPI` directory
and run
```agsl
./testGetAssertions.sh
```

You need to alter the examples to match some assertions that you have already generated (from corla). For example, if you 
have already generated assertions for Boulder using the [Boulder test data](https://github.com/DemocracyDevelopers/colorado-rla/tree/scratch/test/IRV-test/Boulder2023Data)
the output of your `testGetAssertions.sh` should look like:

```
File: GetAssertionRequest.assertionRequest.json
{"metadata":{"candidates":["Paul Tweedlie","Nicole Speer","Bob Yates","Aaron Brockett"],"contest":"City of Boulder Mayoral Candidates","riskLimit":0.03,"assertionRisks":[1.00,1.00,1.00]},"solution":{"Ok":{"assertions":[{"assertion":{"type":"NEB","winner":3,"loser":1},"difficulty":46.40923196276183,"margin":2578},{"assertion":{"type":"NEB","winner":3,"loser":0},"difficulty":14.260190703218116,"margin":8390},{"assertion":{"type":"NEN","winner":3,"loser":2,"continuing":[2,3]},"difficulty":97.42915309446254,"margin":1228}],"difficulty":97.42915309446254,"margin":1228,"num_candidates":4}}}
```

If you try to retrieve assertions for a contest with no matching assertions in the database, the output should look like:
```
File: NoAssertionRequest.assertionRequest.json
{"metadata":{"candidates":["Alice","Bob","Chuan","Diego"],"contest":"NotARealContest98791841978","riskLimit":0.03},"solution":{"Err":"NoAssertionsForThisContest"}}
```

