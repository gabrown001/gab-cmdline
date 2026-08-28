/*****************************************************************************************
 *
 * Copyright 2016-2025 Gregory Brown. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 *****************************************************************************************
 */

package com.gabstudios.cmdline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.gabstudios.cmdline.Token.Type;
import com.gabstudios.collection.LinkedHashMapTrie;
import com.gabstudios.collection.Trie;

/**
 * The command line parser.
 * <p>
 * Construct one, define the commands it should recognise, then parse. Instances share no state, so two parsers can
 * coexist and a test can build one per case:
 *
 * <pre>
 * CmdLine cmdLine = new CmdLine();
 * cmdLine.defineCommand("-l, --load, !fileName, #Load a file into the system")
 *         .defineCommand("-s, --save, #Save the application").defineCommand("-q, --quit, #Quit the application");
 *
 * // Either hand each command to a listener as it is found...
 * cmdLine.parse(args, listener);
 *
 * // ...or take the list back.
 * List&lt;Command&gt; commands = cmdLine.parse(args);
 * </pre>
 * <p>
 * A definition is split on commas, and each token is read by its first character:
 * <ul>
 * <li>{@code #} the description. Zero or one. In a single comma-delimited definition it runs to the end of the string,
 * so it may itself contain commas and equals signs.</li>
 * <li>{@code !} a required value. Zero to many, and all must precede any optional value.</li>
 * <li>{@code ?} an optional value. Zero to many.</li>
 * <li>{@code :} a regex every value of this command must match. Zero or one.</li>
 * <li>A token starting with none of these is a command name. One command may have several.</li>
 * </ul>
 * <p>
 * A value name is scoped to the command that declares it, so two commands may each take a {@code !file}, and values are
 * read back by that name: {@code command.getValues("file")}.
 * <p>
 * An empty argument array yields an empty list rather than an error — running a tool with no arguments is its most
 * common invocation. A listener passed to {@link #parse(String[], CommandListener)} applies to that call only.
 *
 * @see CmdLine#defineCommand(String)
 * @see CmdLine#parse(String[])
 * @see CmdLine#parse(String[], CommandListener)
 *
 * @author G Brown
 */
public class CmdLine {

    /*
     * A map that holds the key of a command string and a value of a command definition.
     */
    private final Map<String, CommandDefinition> commandDefinitionMap;

    /*
     * Accumulates the Command instances produced during a parse() call.
     */
    private final List<Command> parsedCommands;

    /*
     * Regex to split the define command method
     */
    private static final String DEFINED_COMMAND_REGEX_PARSE_PATTERN = "\\s*,\\s*";

    /*
     * The tokenizer that handles the defineCommand(xxxx) method.
     */
    private static final CommandDefinitionTokenizer DEFINED_COMMAND_TOKENIZER;

    /*
     * The maximum length allowed for any size - String, tokens, etc.
     */
    private static final int MAX_LENGTH = 256;

    /*
     * The maximum number of command line arguments accepted. Separate from MAX_LENGTH, which bounds the length of a
     * single string: they are different limits and one constant cannot explain both in an error message.
     */
    private static final int MAX_ARGUMENTS = 256;

    /*
     * The application name.
     */
    private String applicationName;

    /*
     * The listener that will handle commands as they are processed, if it is set. May be 0 to 1.
     */
    private CommandListener commandListener;

    /*
     * The application version.
     */
    private String version;

    /*
     * A Trie that holds the command names. This data structure is used for word suggestion if the command is not found.
     */
    private final Trie wordSuggestionTrie;

    private static final String NAME_NULL_EMPTY_ERROR = "The parameter 'name' must not be null or empty.";
    private static final String NAME_LESS_EQUAL_ERROR = "The parameter 'name' must be less than or equal to "
            + CmdLine.MAX_LENGTH;

    /*
     * Controls whether -D<key>=<value> tokens are processed as System properties. Disabled by default.
     */
    private boolean systemPropertiesEnabled;

    /*
     * Property key prefixes that may not be set via the command line.
     */
    private static final Set<String> BLOCKED_KEY_PREFIXES = Set.of("java.", "javax.", "sun.", "jdk.", "com.sun.");

    static {
        DEFINED_COMMAND_TOKENIZER = new CommandDefinitionTokenizer();
    }

    /*
     * Records a variable name within ONE command definition. Names are scoped to the command that declares them, so two
     * commands may each take a "!file"; declaring the same name twice in one definition is still a duplicate.
     */
    private static void addVariableName(final Set<String> namesInThisCommand, final String name) {
        assert ((name != null) && (name.length() > 0)) : NAME_NULL_EMPTY_ERROR;
        assert (name.length() <= CmdLine.MAX_LENGTH)
                : "The parameter 'name' must be less than or equal to " + CmdLine.MAX_LENGTH;

        if (!namesInThisCommand.add(name)) {
            throw (new DuplicateException("Error: The variable '" + name
                    + "' has already been defined for this command.  Define a new variable name."));
        }
    }

    /**
     * Clears the CmdLine and releases resources. Resets all state including command definitions, the command listener,
     * system properties support (disabled), and any previously parsed commands.
     *
     * @return this parser, so calls can be chained.
     */
    public CmdLine clear() {
        this.commandListener = null;
        this.systemPropertiesEnabled = false;
        this.commandDefinitionMap.clear();
        this.wordSuggestionTrie.clear();
        this.parsedCommands.clear();
        return (this);
    }

    /*
     * Creates the Command if a CommandDefinition exists.
     */
    private Command createCommand(final String commandName, final List<String> tokens) {

        assert ((commandName != null) && (commandName.length() > 0))
                : "The parameter 'commandName' must not be null or empty";
        assert (commandName.length() <= CmdLine.MAX_LENGTH) : NAME_LESS_EQUAL_ERROR;
        assert (tokens != null) : "The parameter 'tokens' must not be null";
        assert (tokens.size() <= CmdLine.MAX_LENGTH) : NAME_LESS_EQUAL_ERROR;

        final Command command = new Command(commandName);
        if (!tokens.isEmpty()) {
            final CommandDefinition commandDefinition = this.commandDefinitionMap.get(commandName);

            final String regex = commandDefinition.getRegexValue();
            Pattern pattern = null;
            if ((regex != null) && (regex.length() > 0)) {
                pattern = Pattern.compile(regex);
            }

            if (commandDefinition.hasRequiredVariables()) {
                final List<String> names = commandDefinition.getRequiredVariableNames();
                this.processVariable(pattern, tokens, names, command, true);
            }

            if (commandDefinition.hasRequiredVariableLists()) {
                final String name = commandDefinition.getRequiredVariableListName();
                this.processVariableList(pattern, tokens, name, command, true);
            }

            if (commandDefinition.hasOptionalVariables()) {
                final List<String> names = commandDefinition.getOptionalVariableNames();
                this.processVariable(pattern, tokens, names, command, false);
            }

            if (commandDefinition.hasOptionalVariableLists()) {
                final String name = commandDefinition.getOptionalVariableListName();
                this.processVariableList(pattern, tokens, name, command, false);
            }
        }

        return (command);
    }

    /*
     * Creates a CommandDefinition.
     */
    private CommandDefinition createCommandDefinition(final List<Token> tokens) {

        assert ((tokens != null) && (!tokens.isEmpty())) : "The parameter 'tokens' must not be null or empty";
        assert (tokens.size() <= CmdLine.MAX_LENGTH)
                : "The parameter 'name' must be less than or equal to " + CmdLine.MAX_LENGTH;

        final CommandDefinition command = new CommandDefinition();

        // Variable names are unique within one command, not across all of them.
        final Set<String> variableNames = new HashSet<>();

        // a list flag. Only one list can exist.
        boolean doesListExist = false;

        // a flag to mark if an optional var was created. If this is true and an
        // attempt to create a required var is made, then an exception will be
        // thrown.
        boolean isOptionalVarDefined = false;
        for (final Token token : tokens) {

            final Type type = token.getType();
            final String name = token.getValue();
            switch (type) {
                case COMMAND -> {
                    if (name.contains(" ")) {
                        throw (new UnsupportedException("Error: The command name '" + name
                                + "' contains spaces which is not supported.  " + "The definition may need a comma."));
                    } else {
                        command.addName(name);
                        this.wordSuggestionTrie.add(name);
                    }
                }
                case DESCRIPTION -> {
                    final String description = command.getDescription();
                    if ((description != null) && (description.length() > 0)) {
                        throw (new DuplicateException(
                                "Error: The description '" + name + "' has already been defined."));
                    } else {
                        command.setDescription(name);
                    }
                }
                case REGEX_VALUE -> {
                    final String existingRegex = command.getRegexValue();
                    if ((existingRegex != null) && (existingRegex.length() > 0)) {
                        throw (new DuplicateException("Error: The regex '" + name + "' has already been defined."));
                    } else {
                        command.setRegexValue(name);
                    }
                }
                case REQUIRED_VALUE -> {
                    if (isOptionalVarDefined) {
                        throw (new UnsupportedException(
                                "Error: An optional variable has already been defined before this required variable.  "
                                        + "Required variables must be defined before optional variables.'"));
                    } else {
                        CmdLine.addVariableName(variableNames, name);
                        command.addRequiredVariable(name);
                    }
                }
                case REQUIRED_LIST_VALUE -> {
                    if (isOptionalVarDefined) {
                        throw (new UnsupportedException(
                                "Error: An optional variable has already been defined before this required variable.  "
                                        + "Required variables must be defined before optional variables.'"));
                    } else if (doesListExist) {
                        throw (new UnsupportedException("Error: A List has already been defined for '" + name
                                + "'.  A command can only have one list defined. "));
                    } else {
                        doesListExist = true;
                        CmdLine.addVariableName(variableNames, name);
                        command.setRequiredVariableList(name);
                    }
                }
                case OPTIONAL_VALUE -> {
                    CmdLine.addVariableName(variableNames, name);
                    command.addOptionalVariable(name);
                    isOptionalVarDefined = true;
                }
                case OPTIONAL_LIST_VALUE -> {
                    if (doesListExist) {
                        throw (new UnsupportedException("Error: A List has already been defined for '" + name
                                + "'.  A command can only have one list defined. "));
                    } else {
                        doesListExist = true;
                        CmdLine.addVariableName(variableNames, name);
                        command.setOptionalVariableList(name);
                        isOptionalVarDefined = true;
                    }
                }
                default -> throw (new UnsupportedException(
                        "Error:  Unknown token '" + name + "' is an unknown type ='" + type.name() + "')."));
            }
        }

        if (command.getNames().isEmpty()) {
            throw (new MissingException("Error:  The command name was not defined and is missing."));
        }

        return (command);
    }

    /**
     * This method defines the command definitions expected in the parser. Call this method for each command that will
     * be defined. A token must use one of these symbols in order for it to be recognized as that type: # = The
     * description of the command. There may be zero to one defined. ! = A required value for the command name. There
     * can be zero to many defined. ? = An optional value for the command name. There can be zero to many defined. : =
     * The regex value to match on for any values that are defined. There can be zero to one defined. ... = A value ends
     * with ... and is a list for the command name. There can be zero to one defined. This can be used with the ! and ?
     * symbols If a token does not start with one of these tokens, then it is considered a command name. Examples:
     * "file, !fileName1, :file\\d.txt, #Load a files into the system" "-f, --file, !fileName1, ?fileName2, ?fileName3,
     * :file\\d.txt, #Load a files into the system" "-f, --file, !fileName1, ?fileNames..., #Load a files into the
     * system"
     *
     * @param nameArgs
     *            An array of String containing values.
     *
     * @return this parser, so calls can be chained.
     */
    public CmdLine defineCommand(final String... nameArgs) {
        if (!(nameArgs != null && nameArgs.length > 0 && nameArgs.length <= CmdLine.MAX_LENGTH)) {
            throw new IllegalArgumentException("Invalid command definition arguments");
        }

        final List<Token> tokens = CmdLine.DEFINED_COMMAND_TOKENIZER.tokenize(nameArgs);

        // Checked here rather than deeper down: createCommandDefinition takes a
        // non-empty token list as an invariant, and a definition made only of
        // whitespace should be reported as the caller's error rather than
        // reaching an internal assertion.
        if (tokens.isEmpty()) {
            throw (new UnsupportedException(
                    "Error: The definition is empty.  A definition must contain at least " + "one command name."));
        }

        final CommandDefinition command = this.createCommandDefinition(tokens);
        final List<String> names = command.getNames();

        for (final String name : names) {
            final CommandDefinition existingCommand = this.commandDefinitionMap.put(name, command);
            if (existingCommand != null) {
                throw (new DuplicateException(
                        "Error: The command '" + name + "' has already been defined.  Define a new command name."));
            }
        }

        return (this);
    }

    /**
     * Convenience overload of {@link #defineCommand(String...)} that accepts a single comma-delimited string. The
     * string is split on commas before processing, so {@code "-f, --file, !name, #desc"} is equivalent to calling
     * {@code defineCommand("-f", "--file", "!name", "#desc")}.
     *
     * @param nameArgs
     *            A comma-delimited string containing the command definition tokens.
     *
     * @return this parser, so calls can be chained.
     *
     * @throws IllegalArgumentException
     *             if nameArgs is null, empty, or exceeds the maximum length.
     */
    public CmdLine defineCommand(final String nameArgs) {
        if (nameArgs == null || nameArgs.isEmpty() || nameArgs.length() > CmdLine.MAX_LENGTH) {
            throw new IllegalArgumentException("Invalid command definition string");
        }

        this.defineCommand(CmdLine.splitDefinition(nameArgs));
        return (this);
    }

    /**
     * Gets the application name that was defined.
     *
     * @return A String. May be null or empty if the application name was not defined.
     */
    public String getApplicationName() {
        return (this.applicationName);
    }

    /**
     * Gets the version String that was defined.
     *
     * @return A String. May be null or empty if the version was not defined.
     */
    public String getVersion() {
        return (this.version);
    }

    /**
     * Parse the command line arguments.
     *
     * @param args
     *            The arguments from the command line.
     *
     * @return A list of {@link Command} instances created from the parsed arguments.
     *
     * @throws IllegalArgumentException
     *             if args is null or exceeds the maximum number of arguments. An empty array is not an error and yields
     *             an empty list.
     * @throws UnsupportedException
     *             if an unrecognized command token is encountered.
     * @throws MissingException
     *             if a required variable value is absent.
     * @throws MatchException
     *             if a value does not match the defined regex pattern.
     */
    public List<Command> parse(final String[] args) {
        if (args == null) {
            throw new IllegalArgumentException("The arguments array must not be null");
        }
        if (args.length > CmdLine.MAX_ARGUMENTS) {
            throw new IllegalArgumentException(
                    "The arguments array must hold at most " + CmdLine.MAX_ARGUMENTS + " entries");
        }
        // Running a tool with no arguments is its most common invocation, and
        // an application that wants to show help then should not have to
        // special-case it before calling.
        if (args.length == 0) {
            this.parsedCommands.clear();
            return (new ArrayList<>());
        }

        this.parsedCommands.clear();
        final List<String> tokens = CmdLine.tokenize(args);
        this.processCmdLineTokens(tokens);

        final List<Command> commands = new ArrayList<>(this.parsedCommands);
        return (commands);
    }

    /**
     * Parse the command line arguments, notifying the provided listener for each command found.
     *
     * @param args
     *            The arguments from the command line.
     * @param listener
     *            A {@link CommandListener} that will be called for each {@link Command} created during parsing. Applies
     *            to this call only; it is not retained for later parses.
     *
     * @return A list of {@link Command} instances created from the parsed arguments.
     *
     * @throws IllegalArgumentException
     *             if args is null or exceeds the maximum number of arguments.
     * @throws UnsupportedException
     *             if an unrecognized command token is encountered.
     * @throws MissingException
     *             if a required variable value is absent.
     * @throws MatchException
     *             if a value does not match the defined regex pattern.
     */
    public List<Command> parse(final String[] args, final CommandListener listener) {
        // Applied for THIS parse only. Leaving it set meant a later parse(args)
        // still notified a listener the caller had passed once, which is
        // surprising at a distance and hard to trace.
        final CommandListener previous = this.commandListener;
        this.commandListener = listener;
        try {
            return (this.parse(args));
        } finally {
            this.commandListener = previous;
        }
    }

    /*
     * Processes the String tokens and creates Command.
     */
    private void processCmdLineTokens(final List<String> tokens) {

        assert ((tokens != null) && (!tokens.isEmpty())) : "The parameter 'tokens' must not be null or empty";
        assert (tokens.size() <= CmdLine.MAX_LENGTH)
                : "The parameter 'tokens' must be less than or equal to " + CmdLine.MAX_LENGTH;

        final String tokenValue = tokens.remove(0);

        // check to see that a command definition exists for the current token.
        if (this.commandDefinitionMap.containsKey(tokenValue)) {
            // if defined, then create a command.
            final Command command = this.createCommand(tokenValue, tokens);

            this.parsedCommands.add(command);

            // if the listener was set, then notify the listener of the created
            // command.
            if (this.commandListener != null) {
                // TODO - thread call to remove from main thread. add timeout
                // for processing.
                this.commandListener.handle(command);
            }

            // Have all tokens been consumed?
            if (!tokens.isEmpty()) {
                // Recursive call to process the remaining tokens.
                this.processCmdLineTokens(tokens);
            }

        } else {
            // Process -D<property>=<value> if it exists.
            final boolean processForSystemProperty = this.processSystemProperty(tokenValue, tokens);

            // if not processed, then the token is not supported.
            if (!processForSystemProperty) {
                // if tokenvalue and not a system property then it is not
                // defined.
                final List<String> suggestedWords = this.wordSuggestionTrie.getWords(tokenValue);

                throw (new UnsupportedException("Error: The command name '" + tokenValue + "' is not defined.",
                        suggestedWords));
            } else if (!tokens.isEmpty()) {
                // if the token is supported, recursive call and process the
                // remaining tokens.
                this.processCmdLineTokens(tokens);
            }
        }
    }

    /*
     * Processes the -D<property>=<value> and adds it to the System property. Only runs when system property processing
     * has been explicitly enabled via setSystemPropertiesEnabled(true).
     */
    private boolean processSystemProperty(final String valueString, final List<String> tokens) {

        if (!this.systemPropertiesEnabled) {
            return false;
        }

        boolean isSystemPropertyProcessed = false;
        if ((valueString != null) && (tokens != null) && !tokens.isEmpty()) {
            final int indexOfSystemProperty = valueString.indexOf("-D");

            if (indexOfSystemProperty > -1) {
                final String systemPropertyKey = valueString.substring(indexOfSystemProperty + 2);

                if (systemPropertyKey.isEmpty() || systemPropertyKey.length() > CmdLine.MAX_LENGTH) {
                    throw new IllegalArgumentException(
                            "Error: The -D property key must not be empty and must be less than or equal to "
                                    + CmdLine.MAX_LENGTH + " characters.");
                }

                for (final String blockedPrefix : CmdLine.BLOCKED_KEY_PREFIXES) {
                    if (systemPropertyKey.startsWith(blockedPrefix)) {
                        throw new UnsupportedException(
                                "Error: The -D property key '" + systemPropertyKey + "' uses a reserved prefix '"
                                        + blockedPrefix + "' and cannot be set via the command line.");
                    }
                }

                final String systemPropertyValue = tokens.remove(0);

                if (systemPropertyValue.isEmpty() || systemPropertyValue.length() > CmdLine.MAX_LENGTH) {
                    throw new IllegalArgumentException(
                            "Error: The -D property value must not be empty and must be less than or equal to "
                                    + CmdLine.MAX_LENGTH + " characters.");
                }

                isSystemPropertyProcessed = true;
                System.setProperty(systemPropertyKey, systemPropertyValue);

                final Command command = new Command(valueString);
                command.addVariable(systemPropertyKey, systemPropertyValue);

                this.parsedCommands.add(command);

                if (this.commandListener != null) {
                    this.commandListener.handle(command);
                }
            }
        }
        return (isSystemPropertyProcessed);
    }

    /*
     * Process the required and optional variables that are associated with a command.
     */
    private void processVariable(final Pattern pattern, final List<String> tokens,
            final List<String> definedVariableNames, final Command command, final boolean required) {

        // pattern can be null.

        assert (tokens != null) : "The parameter 'tokens' must not be null.";
        assert (tokens.size() <= CmdLine.MAX_LENGTH)
                : "The parameter 'tokens' must be less than or equal to " + CmdLine.MAX_LENGTH;

        assert (definedVariableNames != null) : "The parameter 'definedVariableNames' must not be null.";
        assert (definedVariableNames.size() <= CmdLine.MAX_LENGTH)
                : "The parameter 'definedVariableNames' must be less than or equal to " + CmdLine.MAX_LENGTH;

        assert (command != null) : "The parameter 'command' must not be null.";

        for (final String varName : definedVariableNames) {
            // A varName must not start with a space, otherwise an exception is
            // thrown.
            if (varName.contains(" ")) {
                throw (new UnsupportedException("Error: The variable name '" + varName
                        + "' contains spaces which is not supported.  The definition may need a comma."));
            } else if ((tokens.isEmpty()) && !required) {
                // if there isnt any info from the command line and this
                // variable is not required then break and exit.
                break;
            } else if ((tokens.isEmpty()) && required) {
                // if there isnt any info from the command line but this
                // variable is required then throw exception.
                throw (new MissingException(
                        "Error:  The value for the required variable '" + varName + "' is missing."));
            } else {

                final String argToken = tokens.remove(0);

                // System.out.println("processVariable: " + name + " : "
                // + argToken + " = " + _variableNameSet);

                if (pattern != null && !pattern.matcher(argToken).matches()) {
                    throw (new MatchException("Error:  The value '" + argToken
                            + "' does not match the expected pattern '" + pattern.toString() + "'."));
                }

                // varName came from this command's own definition, so it is a
                // variable of this command by construction.
                command.addVariable(varName, argToken);
            }
        }
    }

    /*
     * Process the required and optional variable lists that are associated with a command.
     */
    private void processVariableList(final Pattern pattern, final List<String> tokens, final String varName,
            final Command command, final boolean required) {
        // pattern can be null.

        assert (tokens != null) : "The parameter 'tokens' must not be null.";
        assert (tokens.size() <= CmdLine.MAX_LENGTH)
                : "The parameter 'tokens' must be less than or equal to " + CmdLine.MAX_LENGTH;

        assert ((varName != null) && (varName.length() > 0)) : "The parameter 'varName' must not be null or empty.";
        assert (varName.length() <= CmdLine.MAX_LENGTH)
                : "The parameter 'varName' must be less than or equal to " + CmdLine.MAX_LENGTH;

        assert (command != null) : "The parameter 'command' must not be null.";

        if (varName.contains(" ")) {
            throw (new UnsupportedException("Error: The variable name '" + varName
                    + "' contains spaces which is not supported.  The definition may need a comma."));
        } else if ((tokens.isEmpty()) && required) {
            // if there isnt any info from the command line but this
            // variable is required then throw exception.
            throw (new MissingException("Error:  The value for the required variable '" + varName + "' is missing."));
        } else {
            while (!tokens.isEmpty() && !this.commandDefinitionMap.containsKey(tokens.get(0))) {

                final String argToken = tokens.remove(0);

                // Process -Dsystem.properties=true if on command line.
                final boolean processedSystemProperty = this.processSystemProperty(argToken, tokens);

                if (!processedSystemProperty) {

                    if (pattern != null) {
                        final Matcher matcher = pattern.matcher(argToken);
                        final boolean isMatch = matcher.matches();
                        if (!isMatch) {
                            throw (new MatchException("Error:  The value '" + argToken
                                    + "' does not match the expected pattern '" + pattern.toString() + "'."));
                        }
                    }

                    command.addVariable(varName, argToken);

                }
            }
        }
    }

    /**
     * Sets the application name in the cmdline. To be used in the help menu - (future release).
     *
     * @param name
     *            The name of the application. Must not be null or empty, and must not exceed the maximum length.
     *
     * @return this parser, so calls can be chained.
     *
     * @throws IllegalArgumentException
     *             if name is null, empty, or exceeds the maximum length.
     */
    public CmdLine setApplicationName(final String name) {
        if (name == null || name.isEmpty() || name.length() > CmdLine.MAX_LENGTH) {
            throw new IllegalArgumentException("Invalid application name");
        }

        this.applicationName = name;
        return (this);
    }

    /**
     * Enables or disables automatic processing of {@code -D<key>=<value>} tokens as System properties. Disabled by
     * default. When enabled, keys matching any prefix in {@code BLOCKED_KEY_PREFIXES} are rejected with an
     * {@link UnsupportedException}.
     *
     * @param enabled
     *            true to enable {@code -D} system property processing, false to disable.
     *
     * @return this parser, so calls can be chained.
     */
    public CmdLine setSystemPropertiesEnabled(final boolean enabled) {
        this.systemPropertiesEnabled = enabled;
        return (this);
    }

    /**
     * Sets the listener that will handle the Commands that are created by the parser.
     *
     * @param commandListener
     *            A listener that will handle the callbacks.
     *
     * @return this parser, so calls can be chained.
     */
    public CmdLine setCommandListener(final CommandListener commandListener) {
        if (commandListener == null) {
            throw new IllegalArgumentException("CommandListener cannot be null");
        }

        this.commandListener = commandListener;
        return (this);
    }

    /**
     * The version number of the application using the cmdline. To be used in the help menu - (future release).
     *
     * @param version
     *            A String value. Must not be null or empty.
     *
     * @return this parser, so calls can be chained.
     *
     * @throws IllegalArgumentException
     *             if version is null or empty.
     */
    public CmdLine setVersion(final String version) {
        if (version == null || version.isEmpty()) {
            throw new IllegalArgumentException("Version cannot be null or empty");
        }

        this.version = version;
        return (this);
    }

    /*
     * Splits a comma-delimited definition, stopping at the description. Everything from the first '#' to the end of the
     * string is one token. The description is prose, and prose contains commas: "#List imports, newest first" used to
     * split into a description and a second command named "newest first", reported as an error about spaces that named
     * neither the comma nor the description. An '=' in a description had the same effect.
     */
    private static String[] splitDefinition(final String definition) {
        final int hash = definition.indexOf('#');
        if (hash < 0) {
            return (definition.split(CmdLine.DEFINED_COMMAND_REGEX_PARSE_PATTERN));
        }

        final String beforeDescription = definition.substring(0, hash);
        final String description = definition.substring(hash).trim();

        final List<String> parts = new ArrayList<>();
        for (final String part : beforeDescription.split(CmdLine.DEFINED_COMMAND_REGEX_PARSE_PATTERN)) {
            final String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                parts.add(trimmed);
            }
        }
        parts.add(description);
        return (parts.toArray(new String[0]));
    }

    /*
     * Converts the command line args into String tokens.
     */
    protected static List<String> tokenize(final String[] args) {
        assert (args != null && args.length > 0) : "The parameter 'args' must not be null or empty";

        // TODO - add assert for max length.

        return Arrays.stream(args).flatMap(arg -> Arrays.stream(arg.split("="))) // Split each arg on '=', flatten into
                                                                                 // a single stream
                .flatMap(eqPart -> Arrays.stream(eqPart.split(","))) // Split each part on ',', flatten again
                .filter(s -> !s.isEmpty()) // Remove empty strings (handles standalone '=' or ',')
                .map(String::trim) // Trim whitespace
                .collect(Collectors.toList()); // Collect into a List<String>
    }

    /**
     * Creates an independent parser.
     * <p>
     * Instances share no state: definitions, parsed commands, the listener, the application name and version, and the
     * system-property switch all belong to the instance. Two parsers can coexist in one JVM, and a test can build one
     * per case instead of clearing a global.
     * <p>
     * The static methods on this class operate on a single shared instance and remain available for simple programs.
     * Prefer an instance when chaining: the static methods return this type for backward compatibility, so chaining off
     * them invokes a static method through an expression, which {@code -Xlint:static} reports.
     */
    public CmdLine() {
        this.wordSuggestionTrie = new LinkedHashMapTrie();
        this.commandDefinitionMap = new HashMap<>();
        this.parsedCommands = new ArrayList<>();
    }

}
