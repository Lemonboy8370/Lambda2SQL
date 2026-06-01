# Lambda2SQL Requirements

## Goal

Convert MyBatis-Plus style lambda condition chains into readable SQL for MySQL and PostgreSQL.

## MVP Scope

- Desktop UI using Java Swing.
- Table name input.
- Dialect selector for MySQL and PostgreSQL.
- Lambda snippet input.
- Read-only SQL output.
- Generate, copy, and save actions.
- Runnable jar for development.
- Native packages for macOS Apple Silicon and Windows for GitHub Releases.

## Supported Syntax

Initial implementation slice:

| Input | SQL |
| --- | --- |
| `.eq(X, val)` | `col = val` |
| `.gt(X, val)` | `col > val` |
| `.orderByAsc(X)` | `ORDER BY col ASC` |
| `.orderByDesc(X)` | `ORDER BY col DESC` |
| `.limit(n)` | `LIMIT n` |
| `.limit(offset, n)` | MySQL: `LIMIT offset, n`; PostgreSQL: `LIMIT n OFFSET offset` |

Planned v1.0 operators:

`ne`, `ge`, `lt`, `le`, `like`, `notLike`, `likeLeft`, `likeRight`, `isNull`, `isNotNull`, `in`, `notIn`, `between`, `notBetween`.

## Value Rules

- Numbers are emitted without quotes.
- Strings are emitted with single quotes.
- Single quotes inside string values are escaped as `''`.
- `null` is emitted as `NULL`.

## Field Mapping

- `User::getName` -> `name`
- `User::getCreateTime` -> `create_time`
- `User::isActive` -> `active`

## Release Targets

- macOS Apple Silicon package for direct download from GitHub Releases.
- Windows x64 package for direct download from GitHub Releases.
- Users should not need to install a JDK to run native packages.
