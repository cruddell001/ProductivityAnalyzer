#!/bin/bash

# Check that we are in the root directory
if [ ! -f build.gradle.kts ]; then
    echo "Please run this script from the root of the project.  E.g., ./scripts/run.sh"
    exit 1
fi

# Make sure jar file exists
if [ ! -f build/libs/ProductivityAnalyzer-1.0-SNAPSHOT-all.jar ]; then
    echo "Jar file not found.  Please run ./scripts/build.sh first."
    exit 1
fi

# make sure JAVA_HOME is set
if [ -z "$JAVA_HOME" ]; then
  echo "JAVA_HOME is not set"
  exit 1
fi

# get current java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
# make sure JAVA_VERSION is REQUIRED_JAVA_VERSION
if [[ $JAVA_VERSION != "$REQUIRED_JAVA_VERSION"* ]]; then
  echo " "
  echo "JAVA_VERSION is not $REQUIRED_JAVA_VERSION - you are running $JAVA_VERSION"
  echo " "
  echo "Some possibilities include:"
  # list out all the java versions installed with full path, using ~/Library/Java/JavaVirtualMachines/
  ls -d1 ~/Library/Java/JavaVirtualMachines/*/Contents/Home/bin/java | grep 11

  exit 1
fi

# Run the jar file with any arguments passed to this script
java -jar build/libs/ProductivityAnalyzer-1.0-SNAPSHOT-all.jar "$@"

