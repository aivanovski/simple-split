#!/bin/bash

# Script to run the simple-split-api-client.jar
# Automatically detects source changes using SHA256 checksums and rebuilds if necessary

JAR_NAME="simple-split-api-client.jar"
TARGET_DIR="api-client/target"
SOURCE_DIR="api-client/src/main/scala"
CHECKSUM_FILE="$TARGET_DIR/source.sha256"

# Function to find the jar file
find_jar() {
    find "$TARGET_DIR" -name "$JAR_NAME" -type f 2>/dev/null | head -1
}

# Function to calculate SHA256 of all source files
calculate_source_checksum() {
    if [ ! -d "$SOURCE_DIR" ]; then
        echo "Error: Source directory $SOURCE_DIR does not exist"
        exit 1
    fi
    
    # Find all .scala files and calculate their combined SHA256
    find "$SOURCE_DIR" -name "*.scala" -type f -exec sha256sum {} \; | sort | sha256sum | cut -d' ' -f1
}

# Function to get stored checksum
get_stored_checksum() {
    if [ -f "$CHECKSUM_FILE" ]; then
        cat "$CHECKSUM_FILE"
    else
        echo ""
    fi
}

# Function to store checksum
store_checksum() {
    local checksum="$1"
    mkdir -p "$(dirname "$CHECKSUM_FILE")"
    echo "$checksum" > "$CHECKSUM_FILE"
}

# Function to check if source has changed
source_has_changed() {
    local current_checksum=$(calculate_source_checksum)
    local stored_checksum=$(get_stored_checksum)
    
    if [ "$current_checksum" != "$stored_checksum" ]; then
        return 0  # true - source has changed
    else
        return 1  # false - source hasn't changed
    fi
}

# Function to build the jar
build_jar() {
    echo "Building jar file..."
    sbt apiClient/assembly -warn
    
    # Store the new checksum after successful build
    if [ $? -eq 0 ]; then
        local current_checksum=$(calculate_source_checksum)
        store_checksum "$current_checksum"
        echo "Source checksum updated."
    fi
}

# Function to run the jar with arguments
run_jar() {
    local jar_path="$1"
    shift
    echo "Running jar file at: $jar_path"
    java -jar "$jar_path" "$@"
}

# Main logic
JAR_PATH=$(find_jar)

if [ -n "$JAR_PATH" ]; then
    # Jar exists, check if source has changed
    if source_has_changed; then
        echo "Source files have changed. Rebuilding jar..."
        build_jar
        
        # Get the jar path again after rebuild
        JAR_PATH=$(find_jar)
        if [ -n "$JAR_PATH" ]; then
            run_jar "$JAR_PATH" "$@"
        else
            echo "Error: Could not find jar file after rebuild"
            exit 1
        fi
    else
        echo "Source files unchanged. Using existing jar."
        run_jar "$JAR_PATH" "$@"
    fi
else
    # Jar doesn't exist, build it
    echo "Jar file not found. Compiling..."
    build_jar
    
    # Check again after assembly
    JAR_PATH=$(find_jar)
    
    if [ -n "$JAR_PATH" ]; then
        echo "Assembly completed."
        run_jar "$JAR_PATH" "$@"
    else
        echo "Error: Could not find jar file even after assembly"
        exit 1
    fi
fi 