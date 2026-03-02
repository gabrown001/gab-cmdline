/*****************************************************************************************
 *
 * Copyright 2016 Gregory Brown. All Rights Reserved.
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

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Gregory Brown (sysdevone)
 */
public class CommandDefinitionTokenizerTest {

    CommandDefinitionTokenizer _tokenizer;

    @BeforeEach
    public void setUp() {
        this._tokenizer = new CommandDefinitionTokenizer();
    }

    @AfterEach
    public void tearDown() {
        this._tokenizer = null;
    }

    @Test
    public void testTokenizer() {
        final String inputString = "file , !fileName1,:file\\d.txt, #Load a files into the system";
        final String[] inputTokens = inputString.split("\\s*,\\s*");

        List<Token> tokens = this._tokenizer.tokenize(inputTokens);

        Assertions.assertTrue(tokens.size() == 4);
        Assertions.assertEquals(Token.Type.COMMAND, tokens.get(0).getType());
        Assertions.assertTrue(tokens.get(0).getValue().equals("file"));
        Assertions.assertEquals(Token.Type.REQUIRED_VALUE, tokens.get(1).getType());
        Assertions.assertTrue(tokens.get(1).getValue().equals("fileName1"));
        Assertions.assertEquals(Token.Type.REGEX_VALUE, tokens.get(2).getType());
        Assertions.assertTrue(tokens.get(2).getValue().equals("file\\d.txt"));
        Assertions.assertEquals(Token.Type.DESCRIPTION, tokens.get(3).getType());
        Assertions.assertTrue(tokens.get(3).getValue().equals("Load a files into the system"));
    }

    @Test
    public void testTokenizerOptionalAndListTokenTypes() {
        final String inputString = "cmd, ?optName, ?optNames..., !reqNames...";
        final String[] inputTokens = inputString.split("\\s*,\\s*");

        List<Token> tokens = this._tokenizer.tokenize(inputTokens);

        Assertions.assertEquals(4, tokens.size());
        Assertions.assertEquals(Token.Type.COMMAND, tokens.get(0).getType());
        Assertions.assertEquals("cmd", tokens.get(0).getValue());
        Assertions.assertEquals(Token.Type.OPTIONAL_VALUE, tokens.get(1).getType());
        Assertions.assertEquals("optName", tokens.get(1).getValue());
        Assertions.assertEquals(Token.Type.OPTIONAL_LIST_VALUE, tokens.get(2).getType());
        Assertions.assertEquals("optNames", tokens.get(2).getValue());
        Assertions.assertEquals(Token.Type.REQUIRED_LIST_VALUE, tokens.get(3).getType());
        Assertions.assertEquals("reqNames", tokens.get(3).getValue());
    }
}
