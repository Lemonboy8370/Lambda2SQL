# Lambda2SQL

Lambda2SQL is a small desktop tool that converts MyBatis-Plus style lambda wrapper snippets into formatted SQL.

## Current Status

This repository is a Java 17 + Swing desktop application scaffold. The core parser/generator already supports the first slice of behavior:

- MySQL and PostgreSQL identifier quoting
- `eq`, `gt`, `orderByAsc`, `orderByDesc`
- `limit(n)` and `limit(offset, n)`
- Java getter references such as `User::getCreateTime` to `create_time`
- basic string escaping for SQL literals

## Requirements

- JDK 17+
- Maven 3.6+

## Run Locally

```bash
mvn test
mvn package
java -jar target/lambda2sql.jar
```

## Project Layout

```text
src/main/java/io/github/lambda2sql/
├── App.java
├── core/
│   ├── ColumnMapper.java
│   ├── Dialect.java
│   ├── LambdaParser.java
│   ├── SqlGenerator.java
│   └── ValueFormatter.java
└── model/
    ├── Condition.java
    ├── Ordering.java
    └── SqlResult.java
```

## Release Packaging

The Maven build creates a runnable jar at `target/lambda2sql.jar`.

Desktop packages are intended to be built with `jpackage`:

- macOS Apple Silicon: run `scripts/package-macos.sh`
- Windows x64: run `scripts/package-windows.ps1`

GitHub Actions currently builds and uploads the jar artifact on every push and pull request. Native desktop packages should be produced on their target OS runners before publishing a public release.
