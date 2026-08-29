/*****************************************************************************************
 *
 * Copyright 2016-2025 Gregory Brown. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 *****************************************************************************************
 */

package com.gabstudios.cmdline;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Covers the behaviours introduced when {@link CmdLine} became an instance class.
 * <p>
 * Each test here pins a decision rather than an implementation detail: isolation between parsers, descriptions being
 * prose, variable names being scoped to their command, an empty argument array being ordinary, and a listener applying
 * only to the parse it was passed to.
 *
 * @author G Brown
 */
public class CmdLineInstanceTest {

    /**
     * Two parsers share nothing. This is the point of the change: isolation no longer depends on remembering to clear a
     * global before the next use.
     */
    @Test
    public void testInstancesAreIsolated() {
        final CmdLine first = new CmdLine();
        final CmdLine second = new CmdLine();

        first.defineCommand("-a, --alpha, #Only known to the first parser");

        final List<Command> parsedByFirst = first.parse(new String[] { "-a" });
        Assertions.assertEquals(1, parsedByFirst.size());

        try {
            second.parse(new String[] { "-a" });
            Assertions.fail("the second parser should not know a command defined on the first");
        } catch (final UnsupportedException e) {
            Assertions.assertTrue(true);
        }
    }

    /** State set on one parser does not leak to another. */
    @Test
    public void testInstanceStateIsNotShared() {
        final CmdLine first = new CmdLine();
        final CmdLine second = new CmdLine();

        first.setApplicationName("first").setVersion("1.0");

        Assertions.assertEquals("first", first.getApplicationName());
        Assertions.assertNull(second.getApplicationName());
        Assertions.assertNull(second.getVersion());
    }

    /** Chaining on an instance is the supported form and returns the same object. */
    @Test
    public void testChainingReturnsTheSameInstance() {
        final CmdLine cmdLine = new CmdLine();
        final CmdLine returned = cmdLine.defineCommand("-a, --alpha, #Alpha").defineCommand("-b, --beta, #Beta")
                .setApplicationName("test");

        Assertions.assertSame(cmdLine, returned);
    }

    /**
     * A description is prose and runs to the end of the definition.
     * <p>
     * It used to be split on commas, so the tail of a sentence became a second command name — reported as an error
     * about spaces that mentioned neither the comma nor the description.
     */
    @Test
    public void testDescriptionMayContainCommas() {
        final CmdLine cmdLine = new CmdLine();
        cmdLine.defineCommand("-i, --imports, #List imports, newest first");

        final List<Command> commands = cmdLine.parse(new String[] { "--imports" });
        Assertions.assertEquals(1, commands.size());
        Assertions.assertEquals("--imports", commands.get(0).getName());
    }

    /** An equals sign in a description is prose too. */
    @Test
    public void testDescriptionMayContainEquals() {
        final CmdLine cmdLine = new CmdLine();
        cmdLine.defineCommand("-p, --property, !pair, #Set a property as key=value");

        final List<Command> commands = cmdLine.parse(new String[] { "--property", "a" });
        Assertions.assertEquals(1, commands.size());
        Assertions.assertEquals("a", commands.get(0).getValues("pair").get(0));
    }

    /**
     * Variable names are scoped to the command that declares them.
     * <p>
     * Two options both taking a {@code !file} is ordinary — a value name describes that option's argument, not a
     * program-wide identifier.
     */
    @Test
    public void testVariableNamesAreScopedToTheirCommand() {
        final CmdLine cmdLine = new CmdLine();
        cmdLine.defineCommand("-u, --upload, !file, #Upload a file");
        cmdLine.defineCommand("-c, --cacert, !file, #Certificate to trust");

        final List<Command> commands = cmdLine.parse(new String[] { "--upload", "scan.sarif", "--cacert", "ca.pem" });

        Assertions.assertEquals(2, commands.size());
        Assertions.assertEquals("scan.sarif", commands.get(0).getValues("file").get(0));
        Assertions.assertEquals("ca.pem", commands.get(1).getValues("file").get(0));
    }

    /** Declaring the same variable twice in ONE command is still a duplicate. */
    @Test
    public void testDuplicateVariableWithinOneCommandIsStillRefused() {
        final CmdLine cmdLine = new CmdLine();
        try {
            cmdLine.defineCommand("-u, --upload, !file, !file, #Upload a file");
            Assertions.fail("one command may not declare the same variable twice");
        } catch (final DuplicateException e) {
            Assertions.assertTrue(true);
        }
    }

    /** No arguments is the most common invocation of a tool, and yields no commands. */
    @Test
    public void testEmptyArgumentsYieldNoCommands() {
        final CmdLine cmdLine = new CmdLine();
        cmdLine.defineCommand("-a, --alpha, #Alpha");

        final List<Command> commands = cmdLine.parse(new String[0]);
        Assertions.assertNotNull(commands);
        Assertions.assertTrue(commands.isEmpty());
    }

    /**
     * A listener applies to the parse it was given and no other.
     * <p>
     * It used to be stored globally, so a later plain parse still notified a listener the caller had passed once — hard
     * to trace, because nothing at the second call site mentions it.
     */
    @Test
    public void testListenerDoesNotPersistBeyondItsParse() {
        final CmdLine cmdLine = new CmdLine();
        cmdLine.defineCommand("-a, --alpha, #Alpha");

        final int[] handled = new int[1];
        final CommandListener listener = command -> handled[0]++;

        cmdLine.parse(new String[] { "-a" }, listener);
        Assertions.assertEquals(1, handled[0]);

        cmdLine.parse(new String[] { "-a" });
        Assertions.assertEquals(1, handled[0], "the listener should not have been notified again");
    }

    /** A definition made only of variables names no command, and is refused. */
    @Test
    public void testDefinitionMustNameACommand() {
        final CmdLine cmdLine = new CmdLine();
        try {
            cmdLine.defineCommand("!justAVariable");
            Assertions.fail("a definition with no command name should be refused");
        } catch (final MissingException e) {
            Assertions.assertTrue(true);
        }
    }

    /**
     * A definition of nothing but whitespace is refused.
     * <p>
     * It used to survive as a command name containing spaces and be refused for that reason; tokens are now trimmed, so
     * the emptiness is reported for what it is.
     */
    @Test
    public void testWhitespaceOnlyDefinitionIsRefused() {
        final CmdLine cmdLine = new CmdLine();
        try {
            cmdLine.defineCommand("    ");
            Assertions.fail("a whitespace-only definition should be refused");
        } catch (final UnsupportedException e) {
            Assertions.assertTrue(true);
        }
    }
}
