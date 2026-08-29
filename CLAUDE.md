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

This is a lightweight Java command-line argument parser library (`com.gabstudios.cmdline`). `CmdLine` is an ordinary class: construct one with `new CmdLine()` and call instance methods on it. Instances share no state, so two parsers can coexist and a test builds one per case rather than clearing a global.

### Parse Flow

1. **Define**: Call `cmdLine.defineCommand("...")` to register command definitions. Each definition string is split on commas **up to the first `#`**; everything from `#` onward is the description, kept whole. The tokens are turned into `Token` objects by `CommandDefinitionTokenizer` and the resulting `CommandDefinition` is stored in the instance's map.

2. **Parse**: Call `cmdLine.parse(args)` or `cmdLine.parse(args, listener)`. The raw `String[]` args are split on `=` and `,` into a flat token list. Tokens are consumed recursively — the first token is matched against the command definition map, and subsequent tokens are consumed as variable values. An **empty array yields an empty list**, not an exception: running a tool with no arguments is its most common invocation. A `listener` applies only to the parse it is passed to.

3. **Output**: Returns a `List<Command>`, each `Command` holding its name and a `Map<String, List<String>>` of named variable values.

4. **Clear**: Call `cmdLine.clear()` to reset one parser for reuse. Usually unnecessary — construct a new one instead.

### Definition Token Syntax (processed by `CommandDefinitionTokenizer`)

| Prefix | Token Type | Notes |
|--------|-----------|-------|
| *(none)* | `COMMAND` | Command name (e.g., `-help`, `--file`) |
| `#` | `DESCRIPTION` | Human-readable description; max one per command. In a single comma-delimited definition it runs to the end of the string, so it may contain commas and `=` |
| `!` | `REQUIRED_VALUE` | Required variable; must precede optional variables |
| `?` | `OPTIONAL_VALUE` | Optional variable |
| `!name...` | `REQUIRED_LIST_VALUE` | Required variable that consumes remaining tokens |
| `?name...` | `OPTIONAL_LIST_VALUE` | Optional variable that consumes remaining tokens |
| `:` | `REGEX_VALUE` | Regex pattern to validate variable values; max one per command |

### Key Classes

- **`CmdLine`** — The parser. Holds all state in instance fields (definition map, parsed command list, listener, application name, version, trie for suggestions). Manages `-D<key>=<value>` system property syntax automatically.
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

### State & Thread Safety

State lives on the instance, so two parsers are independent. A single instance is **not thread-safe** — its collections are unsynchronized — so do not share one across threads; give each thread its own.

Tests construct a parser per case rather than clearing a global.

### Variable Name Uniqueness

Variable names (from `!` and `?` tokens) are unique **within one command**, not across all of them. Two commands may each declare a `!file`; declaring `!file` twice in one definition throws `DuplicateException`.

Values are read back by variable name: `command.getValues("file")`.

### Chaining

`defineCommand` and the setters return the instance, so calls chain:

```java
CmdLine cmdLine = new CmdLine();
cmdLine.defineCommand("-f, --file, !name, #Load a file")
       .defineCommand("-s, --save, #Save the file")
       .setApplicationName("mytool");
```

Because these are instance methods, chaining is clean under `-Xlint:static` — a consumer building with `-Werror` no longer has to break the chain apart.
