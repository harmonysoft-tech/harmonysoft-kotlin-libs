#!/bin/bash

#./gradlew clean build

function execute {
  COMMAND=$1
  echo "executing $COMMAND"
  $COMMAND
  RES=$?
  if [ $RES -ne 0 ]
  then
    echo "stopping the execution because the following command failed: $COMMAND"
    exit 1
  fi
}

execute "./gradlew release-paperwork"
execute "./gradlew clean build"
execute "./gradlew :internal-http-test:cucumber"
execute "./gradlew :harmonysoft-mongo-cucumber:cucumber"
execute "./gradlew :harmonysoft-kafka-cucumber:cucumber"
execute "./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository"