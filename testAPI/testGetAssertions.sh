#!/bin/bash

for f in *.assertionRequest.json; do
   echo "File: $f"
   curl --location 'localhost:8080/raire/get-assertions' --header 'Content-Type: application/json' --data "$(cat $f)"
   echo ""
done

 




