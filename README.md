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

- To test GET request go to following url in your browser. It accepts 2 path variables and 1 request parameter.
    - 'http://localhost:8080/demo/hello/democracy/developer?salutation=Amazing'
    - Structure of this URL
      1. baseURL:  'http://localhost:8080/demo/hello'
      2. first Path varible: democracy
      3. second Path variable: developer
      4. requestParameter: salutation

your output should look like 
```
{
    "salutation": "Amazing",
    "firstName": "democracy",
    "lastName": "developer"
}
```

- To test post request please run following curl command. The post request expects a JSON request body and produces a JSON output
```curl --location 'localhost:8080/demo/hello' \
--header 'Content-Type: application/json' \
--data '{
    "salutation": "Amazing",
    "firstName": "democracy",
    "lastName": "developer"
}'
```
Expected output for this request should be following
```
{
    "salutation": "AMAZING",
    "firstName": "DEMOCRACY",
    "lastName": "DEVELOPER"
}
```