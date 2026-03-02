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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Gregory Brown (sysdevone)
 */
public class CmdLineNegativeTest {

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
        CmdLine.clear();
    }

    @Test
    public void testDefineNoCommand() {

        try {

            CmdLine.defineCommand("!fileName, ?fileName1, :file\\d.txt, #Load files into the system");

            Assertions.fail();
        } catch (MissingException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testDefineCommand() {

        try {

            CmdLine.defineCommand("file, !fileName, ?fileName, :file\\d.txt, #Load files into the system");

            Assertions.fail();
        } catch (DuplicateException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testDefineCommand2() {

        try {

            CmdLine.defineCommand("file, file, !fileName, :file\\d.txt, #Load files into the system");

            Assertions.fail();
        } catch (DuplicateException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testDefineCommand3() {

        try {

            CmdLine.defineCommand("file, !fileName, :file\\d.txt, :file\\d.txt, #Load files into the system");

            Assertions.fail();
        } catch (DuplicateException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testDefineCommand4() {

        try {

            CmdLine.defineCommand(
                    "file, !fileName, :file\\d.txt, #Load files into the system, #Load files into the system");

            Assertions.fail();
        } catch (DuplicateException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testDefineCommand5() {

        try {

            CmdLine.defineCommand("file !fileName :file\\d.txt #Load files into the system");

            Assertions.fail();
        } catch (UnsupportedException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testDefineCommand6() {

        try {

            CmdLine.defineCommand("file, !fileNames..., ?fileNames...");

            Assertions.fail();
        } catch (UnsupportedException e) {
            Assertions.assertTrue(true);
        }

    }

    @Test
    public void testDefineCommand7() {

        try {

            CmdLine.defineCommand("file, ?fileNames..., !fileNames2...");

            Assertions.fail();
        } catch (UnsupportedException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testDefineCommand8() {

        CmdLine.defineCommand("file, !file1, !file2");

        final String[] args = new String[3];
        args[0] = "file";
        args[1] = "=";
        args[2] = "file1.txt";

        try {
            CmdLine.parse(args);

            Assertions.fail();
        } catch (MissingException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testDefineCommand9() {

        CmdLine.defineCommand("file, !file, !files...");

        final String[] args = new String[3];
        args[0] = "file";
        args[1] = "=";
        args[2] = "file1.txt";

        try {
            CmdLine.parse(args);

            Assertions.fail();
        } catch (MissingException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testDefineCommand10() {

        CmdLine.defineCommand("file, !file");

        final String[] args = new String[3];
        args[0] = "install";
        args[1] = "=";
        args[2] = "file1.txt";

        try {
            CmdLine.parse(args);

            Assertions.fail();
        } catch (UnsupportedException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testDefineCommand11() {

        try {

            CmdLine.defineCommand("file, ?fileName1, !fileNames2");

            Assertions.fail();
        } catch (UnsupportedException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testDefineCommand12() {

        try {

            CmdLine.defineCommand("");

            Assertions.fail();
        } catch (IllegalArgumentException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testDefineCommand13() {

        try {

            CmdLine.defineCommand();

            Assertions.fail();
        } catch (IllegalArgumentException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testDefineCommand14() {

        try {

            CmdLine.defineCommand("    ");

            Assertions.fail();
        } catch (UnsupportedException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testDefineCommandNullStringThrowsIllegalArgumentException() {

        try {
            CmdLine.defineCommand((String) null);
            Assertions.fail();
        } catch (IllegalArgumentException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testParseNullArgsThrowsIllegalArgumentException() {

        try {
            CmdLine.parse(null);
            Assertions.fail();
        } catch (IllegalArgumentException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testParseEmptyArgsThrowsIllegalArgumentException() {

        try {
            CmdLine.parse(new String[0]);
            Assertions.fail();
        } catch (IllegalArgumentException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testSetApplicationNameNullThrowsIllegalArgumentException() {

        try {
            CmdLine.setApplicationName(null);
            Assertions.fail();
        } catch (IllegalArgumentException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testSetApplicationNameEmptyThrowsIllegalArgumentException() {

        try {
            CmdLine.setApplicationName("");
            Assertions.fail();
        } catch (IllegalArgumentException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testSetVersionNullThrowsIllegalArgumentException() {

        try {
            CmdLine.setVersion(null);
            Assertions.fail();
        } catch (IllegalArgumentException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testSetVersionEmptyThrowsIllegalArgumentException() {

        try {
            CmdLine.setVersion("");
            Assertions.fail();
        } catch (IllegalArgumentException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testParseValueDoesNotMatchRegexThrowsMatchException() {

        CmdLine.defineCommand("file, !fileName, :file\\d.txt");

        final String[] args = { "file=badvalue.txt" };
        try {
            CmdLine.parse(args);
            Assertions.fail();
        } catch (MatchException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testSystemPropertyDisabledByDefault() {

        final String[] args = { "-Dcom.example.debug=true" };
        try {
            CmdLine.parse(args);
            Assertions.fail();
        } catch (UnsupportedException e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void testSystemPropertyBlockedJvmPrefixThrowsUnsupportedException() {

        CmdLine.setSystemPropertiesEnabled(true);

        final String[] args = { "-Djava.home=/usr/lib/jvm" };
        try {
            CmdLine.parse(args);
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
