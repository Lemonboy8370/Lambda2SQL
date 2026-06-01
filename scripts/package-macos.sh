#!/usr/bin/env bash
set -euo pipefail

mvn -DskipTests package

jpackage \
  --type dmg \
  --name Lambda2SQL \
  --input target \
  --main-jar lambda2sql.jar \
  --main-class io.github.lambda2sql.App \
  --dest target/package \
  --app-version 1.0.0
