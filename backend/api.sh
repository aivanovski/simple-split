#!/bin/bash

# Script to run the simple-split-api-client.jar
# Checks if jar exists, assembles if needed, then runs with all passed arguments

JAR_NAME="simple-split-api-client.jar"
TARGET_DIR="api-client/target"

# Function to find the jar file
find_jar() {
    find "$TARGET_DIR" -name "$JAR_NAME" -type f 2>/dev/null | head -1
}

# Check if jar file exists
JAR_PATH=$(find_jar)

if [ -n "$JAR_PATH" ]; then
    echo "Found jar file at: $JAR_PATH"
    java -jar "$JAR_PATH" "$@"
else
    echo "Jar file not found. Compiling..."
    sbt apiClient/assembly -warn
    
    # Check again after assembly
    JAR_PATH=$(find_jar)
    
    if [ -n "$JAR_PATH" ]; then
        echo "Assembly completed. Found jar file at: $JAR_PATH"
        java -jar "$JAR_PATH" "$@"
    else
        echo "Error: Could not find jar file even after assembly"
        exit 1
    fi
fi 