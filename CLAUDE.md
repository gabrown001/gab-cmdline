# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Build the project
mvn package

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=CmdLineTest

# Run a single test method
mvn test -Dtest=CmdLineTest#testMethodName

# Run with code coverage report (output in target/site/jacoco/)
mvn test jacoco:report

# Format code (runs automatically on build via formatter-maven-plugin)
mvn formatter:format

# Run SpotBugs static analysis
mvn spotbugs:check

# Skip tests during build
mvn package -DskipTests
```

Requirements: Java 17, Maven 3.9.0+

## Architecture Overview

This is a lightweight Java command-line argument parser library (`com.gabstudios.cmdline`). The library uses a static-singleton pattern — `CmdLine` is a utility class with only static methods and a singleton `INSTANCE` for method chaining.

### Parse Flow

1. **Define**: Call `CmdLine.defineCommand("...")` to register command definitions. Each definition string is tokenized by `CommandDefinitionTokenizer` into `Token` objects. The resulting `CommandDefinition` is stored in a static `HashMap<String, CommandDefinition>`.

2. **Parse**: Call `CmdLine.parse(args)` or `CmdLine.parse(args, listener)`. The raw `String[]` args are split on `=` and `,` into a flat token list. Tokens are consumed recursively — the first token is matched against the command definition map, and subsequent tokens are consumed as variable values.

3. **Output**: Returns a `List<Command>`, each `Command` holding its name and a `Map<String, List<String>>` of named variable values.

4. **Clear**: Call `CmdLine.clear()` to reset all static state before reuse.

### Definition Token Syntax (processed by `CommandDefinitionTokenizer`)

| Prefix | Token Type | Notes |
|--------|-----------|-------|
| *(none)* | `COMMAND` | Command name (e.g., `-help`, `--file`) |
| `#` | `DESCRIPTION` | Human-readable description; max one per command |
| `!` | `REQUIRED_VALUE` | Required variable; must precede optional variables |
| `?` | `OPTIONAL_VALUE` | Optional variable |
| `!name...` | `REQUIRED_LIST_VALUE` | Required variable that consumes remaining tokens |
| `?name...` | `OPTIONAL_LIST_VALUE` | Optional variable that consumes remaining tokens |
| `:` | `REGEX_VALUE` | Regex pattern to validate variable values; max one per command |

### Key Classes

- **`CmdLine`** — Static entry point. Holds all state in static fields (definition map, variable name set, command list, trie for suggestions). Manages `-D<key>=<value>` system property syntax automatically.
- **`CommandDefinitionTokenizer`** — Converts `defineCommand()` string args into `Token` objects using prefix-based switching.
- **`CommandDefinition`** — POJO storing names, required/optional variable lists, optional regex, and description for one logical command.
- **`Command`** — POJO returned after parsing; holds the matched command name and a map of variable name → `List<String>` values.
- **`CommandListener`** — Single-method interface (`handle(Command)`) for callback-based processing during parse.
- **`Token` / `Token.Type`** — Internal enum-typed value object used during definition tokenization.
- **Exceptions** — All extend `RuntimeException`: `DuplicateException` (duplicate command/variable), `MissingException` (required variable absent), `MatchException` (regex mismatch), `UnsupportedException` (unknown command; includes `getSuggestionList()` for typo suggestions).

### `com.gabstudios.collection` package

Contains a general-purpose trie used internally for word suggestion when an undefined command is encountered:
- **`Trie`** — Interface with `add`, `contains`, `getWords(prefix)`, `getWords()`, `clear`.
- **`LinkedHashMapTree`** — Generic tree backed by `LinkedHashMap` for ordered children.
- **`LinkedHashMapTrie`** — Implements `Trie` on top of `LinkedHashMapTree<Character>`. Each character is a tree node; `TrieNode.isWord()` marks word terminals.

### Static State & Thread Safety

`CmdLine` is **not thread-safe** — all state (definition map, variable name set, parsed command list, command listener) lives in static fields. Always call `CmdLine.clear()` between test cases (tests use `@AfterEach` for this). The class is non-instantiable (private constructor).

### Variable Name Uniqueness

Variable names (from `!` and `?` tokens) are **globally unique across all commands** — tracked in a static `HashSet<String>`. Reusing the same variable name in different `defineCommand()` calls throws `DuplicateException`.
