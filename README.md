SonarQube Lua Plugin
====================

[![Release](https://img.shields.io/github/v/release/gudge25/sonar-lua)](https://github.com/gudge25/sonar-lua/releases)

## Description

This plugin enables analysis of Lua projects within SonarQube.

It was originally written for SonarQube 5.6 and has been modernized to work with SonarQube 9+ / 10+ / 26.x using the modern `org.sonarsource.api.plugin:sonar-plugin-api`.

## What Works

- Language detection for `.lua` files
- Core metrics: `ncloc`, `lines`, `comment_lines`, `functions`, `statements`, `complexity`
- 16 rules registered in the `lua` repository
- 14 rules active by default in the `Sonar way` quality profile
- Issue reporting for activated rules
- Lua long comments (`--[=[ ]=]`) support
- Syntax highlighting and CPD token registration
- Dockerized Maven build environment
- AssertJ-based unit tests (all passing)

## Build Requirements

- Docker (the build environment is fully containerized)
- A running SonarQube instance to deploy the plugin

## Build

A helper script and a Makefile are provided:

```bash
./build-with-docker.sh
# or
make build
```

This builds the plugin JAR:

```
sonar-lua-plugin/target/sonar-lua-plugin-2.1.jar
```

You can also build manually with Docker:

```bash
docker build -f Dockerfile.build -t sonar-lua-build .
docker run --rm -v "$(pwd)":/build sonar-lua-build \
  mvn clean package -Dmaven.test.skip=true -Dlicense.skip=true
```

## Install from Release

Download the latest JAR from the [Releases](https://github.com/gudge25/sonar-lua/releases) page and copy it into SonarQube:

```bash
wget https://github.com/gudge25/sonar-lua/releases/download/v2.1/sonar-lua-plugin-2.1.jar \
  -O /opt/sonarqube/extensions/plugins/sonar-lua-plugin-2.0.jar
# or for Docker:
docker cp sonar-lua-plugin-2.1.jar sonarqube:/opt/sonarqube/extensions/plugins/
docker restart sonarqube
```

## Deploy from Source

Copy the built JAR into the SonarQube extensions directory and restart SonarQube:

```bash
docker cp sonar-lua-plugin/target/sonar-lua-plugin-2.1.jar \
  sonarqube:/opt/sonarqube/extensions/plugins/
docker restart sonarqube
```

Wait for SonarQube to fully start, then verify the plugin appears in the logs:

```bash
docker logs sonarqube --tail=100 | grep -i lua
```

## Analyze a Lua Project

Create a `sonar-project.properties` file at the root of your Lua project:

```properties
sonar.projectKey=my-lua-project
sonar.projectName=My Lua Project
sonar.sources=.
sonar.host.url=http://your-sonarqube:9000
sonar.token=YOUR_SONARQUBE_TOKEN
sonar.lua.file.suffixes=lua
```

Run the scanner:

```bash
sonar-scanner
```

Or with Docker:

```bash
docker run --rm --network=your_sonarqube_network -v "$(pwd)":/usr/src \
  sonarsource/sonar-scanner-cli
```

## Update / Rebuild

After changing plugin code:

1. Rebuild with `./build-with-docker.sh` or `make build`
2. Redeploy the JAR to SonarQube
3. Restart SonarQube
4. Re-run analysis on your project

### Makefile Targets

A `Makefile` with common tasks is included:

```bash
make build        # Build the plugin JAR
make test         # Run Maven tests
make clean        # Clean build artifacts
make deploy       # Build and deploy to SonarQube
make deploy-logs  # Show SonarQube logs filtered for Lua-related messages
make scan         # Run sonar-scanner on the test project
make all          # Build, deploy, and show logs
make help         # Show all available targets
```

## Modernization Notes

Major changes compared to the original plugin:

- Migrated from the legacy `sonar-plugin-api` 5.6 to `org.sonarsource.api.plugin:sonar-plugin-api` 10.7.0.2191
- Replaced `Settings` with `Configuration`
- Replaced `ProfileDefinition` with `BuiltInQualityProfilesDefinition`
- Replaced `AnnotationBasedRulesDefinition` with a custom annotation-based rule registration
- Removed Cobertura coverage support (relied on removed APIs)
- Removed `FileLinesVisitor` (relied on removed `FileLinesContextFactory`)
- Removed old complexity distribution metrics (`FUNCTION_COMPLEXITY_DISTRIBUTION`, `FILE_COMPLEXITY_DISTRIBUTION`)
- Removed deprecated `RuleStatus.READY` and `Sqale*` annotations
- Fixed Lua comment lexing (`--` and `--[[ ]]`) and long-comment parsing (`--[=[ ]=]`)
- Fixed `FunctionCallComplexityCheck` runtime cast bug
- Fixed duplicate `sslr-core` dependency conflict
- Replaced `fest-assert` with AssertJ
- Updated dependencies: `commons-io` 2.16.1, `logback` 1.5.6, `commons-lang3` 3.14.0
- Rewrote `LuaSquidSensor` to manually instantiate checks and save issues via the modern `SensorContext` API

## Remaining Work

- Re-implement coverage support using the modern coverage API
- Expand grammar coverage for newer Lua 5.x syntax (e.g., `goto`, bitwise operators, integer division)
- Add more comprehensive unit tests for checks and metrics

## Metrics

The plugin computes the following metrics:

- `LINES_OF_CODE` (NCLOC)
- `LINES`
- `FILES`
- `COMMENT_LINES`
- `FUNCTIONS`
- `STATEMENTS`
- `COMPLEXITY`

## Rules

- `CommentRegularExpressionCheck`
- `FileComplexityCheck`
- `FunctionCallComplexityCheck`
- `FunctionComplexityCheck`
- `FunctionWithTooManyParametersCheck`
- `LineLengthCheck`
- `LocalFunctionComplexityCheck`
- `LocalFunctionNameCheck`
- `MethodComplexityCheck`
- `NestedControlFlowDepthCheck`
- `NestedFunctionsDepthCheck`
- `NestedTablesDepthCheck`
- `TableComplexityCheck`
- `TableWithTooManyFieldsCheck`
- `TooManyLinesInFileCheck`
- `XPathCheck`

## License

GNU LGPL 3
