# raire-java
Java implementation of RAIRE

PreReq: Java 11, Maven.

Run instructions:
- Option 1: 
  - Go to au.org.democracydevelopes.raise.RaireJavaApplication class and run the main method from IDE
- Option 2:
  - Go to home directory of the service and run following command
     `./mvnw spring-boot run`

This will run the application on port 8080.
You can test it with a sample GET rest API call from your webbrowser/postman
`http://localhost:8080/demo/hello/democracy/developer?salutation=Amazing`

your output should look like 
```
{
    "salutation": "Amazing",
    "firstName": "democracy",
    "lastName": "developer"
}
```