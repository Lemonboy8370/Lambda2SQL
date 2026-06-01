$ErrorActionPreference = "Stop"

mvn -DskipTests package

jpackage `
  --type exe `
  --name Lambda2SQL `
  --input target `
  --main-jar lambda2sql.jar `
  --main-class io.github.lambda2sql.App `
  --dest target/package `
  --app-version 0.1.0
