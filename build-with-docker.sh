#!/bin/bash
set -e

echo "Building SonarQube Lua plugin using Docker..."

# Build the Docker image
docker build -f Dockerfile.build -t sonar-lua-build .

# Run the build and copy artifacts to local target directory
# Mount current directory so build artifacts are accessible
docker run --rm -v "$(pwd)":/build sonar-lua-build mvn clean package -Dmaven.test.skip=true -Dlicense.skip=true

echo "Build complete. Plugin JAR: sonar-lua-plugin/target/sonar-lua-plugin-1.1.jar"
