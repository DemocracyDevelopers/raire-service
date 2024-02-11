#!/bin/bash

for f in *.voteExample.json; do
   echo "File: $f"
   curl --location 'localhost:8080/cvr/audit' --header 'Content-Type: application/json' --data "$(cat $f)"
   echo ""
done

 




