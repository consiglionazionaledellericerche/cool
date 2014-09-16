#! /bin/bash

while read p; do

  echo $p
  curl -s http://localhost:8280/cool-jconon/page/view/$p?guest=true | jsonlint | grep -v '"action"'  > a.json
  curl -s http://localhost:8180/cool-jconon/rest/bulkInfo/view/$p?guest=true  | jsonlint | grep -v '"action"' > b.json
  diff a.json b.json
  rm a.json b.json
done < bulkinfo.txt
