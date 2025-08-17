#!/bin/bash

# Script to run the simple-split-api-client.jar
# Checks if jar exists, assembles if needed, then runs with all passed arguments
# Use --rebuild to force rebuilding the jar file

JAR_NAME="simple-split-api-client.jar"
TARGET_DIR="api-client/target"

# Function to find the jar file
find_jar() {
    find "$TARGET_DIR" -name "$JAR_NAME" -type f 2>/dev/null | head -1
}

# Function to build the jar
build_jar() {
    echo "Building jar file..."
    sbt apiClient/assembly -warn
}

# Function to run the jar with arguments
run_jar() {
    local jar_path="$1"
    shift
    echo "Running jar file at: $jar_path"
    java -jar "$jar_path" "$@"
}

# Check for --rebuild option
REBUILD=false
ARGS=()

for arg in "$@"; do
    if [ "$arg" = "--rebuild" ]; then
        REBUILD=true
    else
        ARGS+=("$arg")
    fi
done

# If rebuild is requested, force rebuild
if [ "$REBUILD" = true ]; then
    echo "Rebuild requested. Forcing jar rebuild..."
    build_jar
    
    JAR_PATH=$(find_jar)
    if [ -n "$JAR_PATH" ]; then
        run_jar "$JAR_PATH" "${ARGS[@]}"
    else
        echo "Error: Could not find jar file after rebuild"
        exit 1
    fi
else
    # Normal flow: check if jar exists
    JAR_PATH=$(find_jar)
    
    if [ -n "$JAR_PATH" ]; then
        run_jar "$JAR_PATH" "${ARGS[@]}"
    else
        echo "Jar file not found. Compiling..."
        build_jar
        
        # Check again after assembly
        JAR_PATH=$(find_jar)
        
        if [ -n "$JAR_PATH" ]; then
            echo "Assembly completed."
            run_jar "$JAR_PATH" "${ARGS[@]}"
        else
            echo "Error: Could not find jar file even after assembly"
            exit 1
        fi
    fi
fi 