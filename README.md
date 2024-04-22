# raire-service
Java implementation of RAIRE

This is a work in progress. If you're interested in running it with colorado-rla, please try
[the prototype](https://github.com/DemocracyDevelopers/raire-service/tree/prototype) of this service and our [prototype updates to colorado-rla](https://github.com/DemocracyDevelopers/colorado-rla/tree/prototype). They are designed to work together.

If you are interested in running and testing this raire-service as a standalone assertion-generation
service, follow the instructions below.

PreReq: Java, Maven. This requires Java at least version 21.

The Springboot tests in RAIREMicroserviceAPITests.java additionally require libtcnative.

## Compiling 

This project depends on another project, _raire-java_, as a library. Therefore, you
first have to compile _raire-java_.

```bash
git clone https://github.com/DemocracyDevelopers/raire-java.git
cd raire-java
mvn install
cd ..
```

This will make raire-java available to other maven projects on your computer.

Now you can compile raire-service:
```bash
git clone https://github.com/DemocracyDevelopers/raire-service.git
cd raire-service
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

This will run the application on port 8080.  (If you want to change the port, reset `server` in `application.yml`.)

## Running the tests
The tests in src/test/java require Docker. You will need to [Install Docker](https://docs.docker.com/get-docker/) if you haven't got it already. 

On Linux platforms, you need to [add yourself to the docker group](https://docs.docker.com/engine/install/linux-postinstall/). This effectively grants root-level privileges to the user. If that's not what you want (and it's probably not) you can [run docker without root privileges](https://docs.docker.com/engine/security/rootless/).
