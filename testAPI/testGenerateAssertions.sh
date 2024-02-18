#!/bin/bash

for f in *.generateAssertionRequest.json; do
   echo "File: $f"
   curl --location 'localhost:8080/raire/generate-assertions' --header 'Content-Type: application/json' --data "$(cat $f)"
   echo ""
done

 




