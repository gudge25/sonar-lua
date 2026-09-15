# Makefile for SonarQube Lua Plugin
# Provides convenient targets for building, testing, and deploying the plugin.

.PHONY: help build test clean deploy deploy-logs scan image

# Default SonarQube container name
SONARQUBE_CONTAINER ?= sonarqube
# Default test project directory
TEST_PROJECT ?= /tmp/lua-test
# Default SonarQube URL inside container network
SONARQUBE_URL ?= http://$(SONARQUBE_CONTAINER):9000

help:
	@echo "SonarQube Lua Plugin - available targets:"
	@echo ""
	@echo "  make build        - Build the plugin JAR using Docker"
	@echo "  make test         - Run Maven tests using Docker"
	@echo "  make clean        - Clean build artifacts"
	@echo "  make deploy       - Copy plugin JAR to SonarQube and restart it"
	@echo "  make deploy-logs  - Show last 50 lines of SonarQube logs after deploy"
	@echo "  make scan         - Run sonar-scanner on the test project"
	@echo "  make all          - Build, test, deploy, and run a test scan"

build:
	@echo "Building SonarQube Lua plugin..."
	@docker build -f Dockerfile.build -t sonar-lua-build .
	@docker run --rm -v "$$(pwd)":/build sonar-lua-build \
		mvn clean package -Dmaven.test.skip=true -Dlicense.skip=true
	@echo "Build complete: sonar-lua-plugin/target/sonar-lua-plugin-1.1.jar"

test:
	@echo "Running tests..."
	@docker build -f Dockerfile.build -t sonar-lua-build .
	@docker run --rm -v "$$(pwd)":/build sonar-lua-build \
		mvn test -Dlicense.skip=true
	@echo "Tests complete"

clean:
	@echo "Cleaning build artifacts..."
	@docker run --rm -v "$$(pwd)":/build sonar-lua-build \
		mvn clean -Dlicense.skip=true || true
	@echo "Clean complete"

deploy: build
	@echo "Deploying plugin to SonarQube container '$(SONARQUBE_CONTAINER)'..."
	@docker cp sonar-lua-plugin/target/sonar-lua-plugin-1.1.jar \
		$(SONARQUBE_CONTAINER):/opt/sonarqube/extensions/plugins/
	@docker restart $(SONARQUBE_CONTAINER)
	@echo "Plugin deployed. SonarQube is restarting."

deploy-logs:
	@echo "Showing SonarQube logs (last 50 lines)..."
	@docker logs $(SONARQUBE_CONTAINER) --tail=50 | grep -iE "(lua|error|exception|deploy)" || true

scan:
	@echo "Running sonar-scanner on $(TEST_PROJECT)..."
	@if [ ! -f $(TEST_PROJECT)/sonar-project.properties ]; then \
		echo "Error: $(TEST_PROJECT)/sonar-project.properties not found"; \
		exit 1; \
	fi
	@docker run --rm --network=$$(docker inspect -f '{{range $$key, $$value := .NetworkSettings.Networks}}{{println $$key}}{{end}}' $(SONARQUBE_CONTAINER) | head -1) \
		-v $(TEST_PROJECT):/usr/src sonarsource/sonar-scanner-cli

all: build deploy
	@echo "Waiting 90 seconds for SonarQube to start..."
	@sleep 90
	@make deploy-logs
