/*****************************************************************************************
 *
 * Copyright 2015 Gregory Brown. All Rights Reserved.
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
public class CmdLineNegativeTest {

    private CmdLine cmdLine;

    @BeforeEach
    public void setUp() {
        this.cmdLine = new CmdLine();
    }

    @AfterEach
    public void tearDown() {
        this.cmdLine.clear();
    }

    @Test
    public void testDefineNoCommand() {

        try {

            this.cmdLine.defineCommand("!fileName, ?fileName1, :file\\d.txt, #Load files into the system");

            Assertions.fail();
        } catch (MissingException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testDefineCommand() {

        try {

            this.cmdLine.defineCommand("file, !fileName, ?fileName, :file\\d.txt, #Load files into the system");

            Assertions.fail();
        } catch (DuplicateException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testDefineCommand2() {

        try {

            this.cmdLine.defineCommand("file, file, !fileName, :file\\d.txt, #Load files into the system");

            Assertions.fail();
        } catch (DuplicateException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testDefineCommand3() {

        try {

            this.cmdLine.defineCommand("file, !fileName, :file\\d.txt, :file\\d.txt, #Load files into the system");

            Assertions.fail();
        } catch (DuplicateException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testDefineCommand4() {

        try {

            // Passed as separate tokens: within a single comma-delimited string
            // the first '#' now begins a description that runs to the end, so a
            // later '#' is part of that prose rather than a second description.
            this.cmdLine.defineCommand("file", "!fileName", ":file\\d.txt", "#Load files into the system",
                    "#Load files into the system");

            Assertions.fail();
        } catch (DuplicateException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testDefineCommand5() {

        try {

            this.cmdLine.defineCommand("file !fileName :file\\d.txt #Load files into the system");

            Assertions.fail();
        } catch (UnsupportedException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testDefineCommand6() {

        try {

            this.cmdLine.defineCommand("file, !fileNames..., ?fileNames...");

            Assertions.fail();
        } catch (UnsupportedException e) {
            Assertions.assertTrue(true);
        }

    }

    @Test
    public void testDefineCommand7() {

        try {

            this.cmdLine.defineCommand("file, ?fileNames..., !fileNames2...");

            Assertions.fail();
        } catch (UnsupportedException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testDefineCommand8() {

        this.cmdLine.defineCommand("file, !file1, !file2");

        final String[] args = new String[3];
        args[0] = "file";
        args[1] = "=";
        args[2] = "file1.txt";

        try {
            this.cmdLine.parse(args);

            Assertions.fail();
        } catch (MissingException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testDefineCommand9() {

        this.cmdLine.defineCommand("file, !file, !files...");

        final String[] args = new String[3];
        args[0] = "file";
        args[1] = "=";
        args[2] = "file1.txt";

        try {
            this.cmdLine.parse(args);

            Assertions.fail();
        } catch (MissingException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testDefineCommand10() {

        this.cmdLine.defineCommand("file, !file");

        final String[] args = new String[3];
        args[0] = "install";
        args[1] = "=";
        args[2] = "file1.txt";

        try {
            this.cmdLine.parse(args);

            Assertions.fail();
        } catch (UnsupportedException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testDefineCommand11() {

        try {

            this.cmdLine.defineCommand("file, ?fileName1, !fileNames2");

            Assertions.fail();
        } catch (UnsupportedException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testDefineCommand12() {

        try {

            this.cmdLine.defineCommand("");

            Assertions.fail();
        } catch (IllegalArgumentException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testDefineCommand13() {

        try {

            this.cmdLine.defineCommand();

            Assertions.fail();
        } catch (IllegalArgumentException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testDefineCommand14() {

        try {

            this.cmdLine.defineCommand("    ");

            Assertions.fail();
        } catch (UnsupportedException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testDefineCommandNullStringThrowsIllegalArgumentException() {

        try {
            this.cmdLine.defineCommand((String) null);
            Assertions.fail();
        } catch (IllegalArgumentException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testParseEmptyArgsReturnsNoCommands() {

        // Running a tool with no arguments is its most common invocation, so it
        // yields no commands rather than an exception. An application that wants
        // to show help then does not have to special-case it before calling.
        final List<Command> commands = this.cmdLine.parse(new String[0]);
        Assertions.assertNotNull(commands);
        Assertions.assertTrue(commands.isEmpty());
    }

    @Test
    public void testParseNullArgsThrowsIllegalArgumentException() {

        try {
            this.cmdLine.parse(null);
            Assertions.fail();
        } catch (IllegalArgumentException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testSetApplicationNameNullThrowsIllegalArgumentException() {

        try {
            this.cmdLine.setApplicationName(null);
            Assertions.fail();
        } catch (IllegalArgumentException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testSetApplicationNameEmptyThrowsIllegalArgumentException() {

        try {
            this.cmdLine.setApplicationName("");
            Assertions.fail();
        } catch (IllegalArgumentException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testSetVersionNullThrowsIllegalArgumentException() {

        try {
            this.cmdLine.setVersion(null);
            Assertions.fail();
        } catch (IllegalArgumentException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testSetVersionEmptyThrowsIllegalArgumentException() {

        try {
            this.cmdLine.setVersion("");
            Assertions.fail();
        } catch (IllegalArgumentException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testParseValueDoesNotMatchRegexThrowsMatchException() {

        this.cmdLine.defineCommand("file, !fileName, :file\\d.txt");

        final String[] args = { "file=badvalue.txt" };
        try {
            this.cmdLine.parse(args);
            Assertions.fail();
        } catch (MatchException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testSystemPropertyDisabledByDefault() {

        final String[] args = { "-Dcom.example.debug=true" };
        try {
            this.cmdLine.parse(args);
            Assertions.fail();
        } catch (UnsupportedException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testSystemPropertyBlockedJvmPrefixThrowsUnsupportedException() {

        this.cmdLine.setSystemPropertiesEnabled(true);

        final String[] args = { "-Djava.home=/usr/lib/jvm" };
        try {
            this.cmdLine.parse(args);
            Assertions.fail();
        } catch (UnsupportedException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testUnsupportedExceptionGetSuggestionListReturnsNullWithNoSuggestions() {
        final UnsupportedException ex = new UnsupportedException("error");
        Assertions.assertNull(ex.getSuggestionList());
    }

}
