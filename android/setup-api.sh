#!/bin/bash

JAR_NAME="simple-split-api.jar"
TARGET_DIR="api/target"

# Function to find the api jar file
find_jar() {
    find "$TARGET_DIR" -name "$JAR_NAME" -type f 2>/dev/null | head -1
}

set -e

cd ../backend
sbt "project api" clean compile package -warn

JAR_PATH=$(find_jar)

if [[ ! -e "$JAR_PATH" ]]; then
    echo "Unable to find $JAR_NAME file"
    exit 1
fi

# Copy JAR to Android libs
mkdir -p ../android/app/libs
cp "$JAR_PATH" "../android/app/libs/$JAR_NAME"

echo "$JAR_NAME built and copied successfully"
