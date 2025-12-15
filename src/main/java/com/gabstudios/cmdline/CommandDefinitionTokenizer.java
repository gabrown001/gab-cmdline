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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tokenizes command definitions into {@link Token} objects based on prefixes. Supports the following prefixes: -
 * {@code #} : Description - {@code !} : Required value (or {@code !...} for required list) - {@code ?} : Optional value
 * (or {@code ?...} for optional list) - {@code :} : Regex validation - No prefix: Command name Splits on {@code =} and
 * {@code ,} delimiters, prioritizing equals over commas.
 *
 * @author Gregory Brown (sysdevone)
 */
public class CommandDefinitionTokenizer {

    private static final int MAX_TOKEN_LENGTH = 1000; // Configurable max length for security

    /**
     * Protected constructor to prevent instantiation.
     */
    protected CommandDefinitionTokenizer() {
        // Do nothing
    }

    /**
     * Creates a {@link Token} based on the input string's prefix.
     *
     * @param inputString
     *            the string to tokenize (must not be null or empty)
     *
     * @return the corresponding {@link Token}
     *
     * @throws IllegalArgumentException
     *             if input is invalid or exceeds max length
     */
    private static Token createToken(String inputString) {
        if (inputString == null || inputString.isEmpty()) {
            throw new IllegalArgumentException("Input string must not be null or empty");
        }
        if (inputString.length() > MAX_TOKEN_LENGTH) {
            throw new IllegalArgumentException("Input string exceeds maximum length of " + MAX_TOKEN_LENGTH);
        }

        return switch (inputString.charAt(0)) {
            case '#' -> new Token(Token.Type.DESCRIPTION, inputString.substring(1));
            case '!' -> createValueToken(inputString, true);
            case '?' -> createValueToken(inputString, false);
            case ':' -> new Token(Token.Type.REGEX_VALUE, inputString.substring(1));
            default -> new Token(Token.Type.COMMAND, inputString);
        };
    }

    /**
     * Helper to create value or list tokens for required/optional.
     */
    private static Token createValueToken(String inputString, boolean required) {
        boolean isList = inputString.endsWith("...");
        String value = isList ? inputString.substring(1, inputString.length() - 3) : inputString.substring(1);

        Token.Type type;
        switch (required ? 1 : 0) {
            case 1 -> type = isList ? Token.Type.REQUIRED_LIST_VALUE : Token.Type.REQUIRED_VALUE;
            case 0 -> type = isList ? Token.Type.OPTIONAL_LIST_VALUE : Token.Type.OPTIONAL_VALUE;
            default -> throw new IllegalStateException("Unexpected value");
        }

        return new Token(type, value);
    }

    /**
     * Tokenizes the command definition arguments into a list of {@link Token}s.
     *
     * @param args
     *            array of strings to tokenize (may be null or empty)
     *
     * @return a list of tokens (empty if no valid input)
     */
    protected List<Token> tokenize(String[] args) {
        if (args == null || args.length == 0) {
            return new ArrayList<>();
        }

        return Arrays.stream(args).flatMap(arg -> Arrays.stream(arg.split("="))) // Split on '=', flatten
                .flatMap(eqPart -> Arrays.stream(eqPart.split(","))) // Split on ',', flatten
                .filter(s -> !s.isEmpty()) // Ignore empty strings
                .map(s -> createToken(s)) // Create tokens (lambda for explicit type inference)
                .collect(Collectors.toList());
    }
}
