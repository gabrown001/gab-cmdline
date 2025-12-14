/*****************************************************************************************
 *
 * Copyright 2016 Gregory Brown. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *****************************************************************************************
 */
package com.gabstudios.cmdline;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.*;

/**
 * @author Gregory Brown (sysdevone)
 */
public class CmdlineNegativeWordSuggestionTest {
    private class CmdLineListener implements CommandListener {

        private final Map<String, Command> _commandMap = new HashMap<String, Command>();

        public Command getCommand(final String name) {
            return (this._commandMap.get(name));
        }

        public int getCount() {
            return (this._commandMap.size());
        }

        @Override
        public void handle(final Command command) {
            this._commandMap.put(command.getName(), command);

            // System.out.println( command );
        }
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
        CmdLine.clear();
    }

    @Test
    public void testWordSuggestion() {

        final CmdLineListener listener = new CmdLineListener();

        CmdLine.setApplicationName("myApp").setVersion("1.1.0").defineCommand("file, !file, !files..., :file\\d.txt")
                .defineCommand("help").defineCommand("quit").defineCommand("install, !installOption")
                .defineCommand("info");

        final String[] args = new String[1];
        args[0] = "inztolll";

        try {
            CmdLine.parse(args, listener);

            Assertions.fail();
        } catch (final UnsupportedException e) {
            final List<String> words = e.getSuggestionList();
            Assertions.assertEquals(2, words.size());
            Assertions.assertTrue(words.contains("install"));
            Assertions.assertTrue(words.contains("info"));
        } catch (final Exception e) {
            Assertions.fail(e.toString());
        }
    }

    @Test
    public void testWordSuggestion2() {

        final CmdLineListener listener = new CmdLineListener();

        CmdLine.setApplicationName("myApp").setVersion("1.1.0").defineCommand("file, !file, !files..., :file\\d.txt")
                .defineCommand("help").defineCommand("quit").defineCommand("install, !installOption")
                .defineCommand("info");

        final String[] args = new String[1];
        args[0] = "instolll";

        try {
            CmdLine.parse(args, listener);

            Assertions.fail();
        } catch (final UnsupportedException e) {
            final List<String> words = e.getSuggestionList();
            Assertions.assertEquals(1, words.size());
            Assertions.assertTrue(words.contains("install"));
        } catch (final Exception e) {
            Assertions.fail(e.toString());
        }
    }

    @Test
    public void testWordSuggestion3() {

        final CmdLineListener listener = new CmdLineListener();

        CmdLine.setApplicationName("myApp").setVersion("1.1.0").defineCommand("file, !file, !files..., :file\\d.txt")
                .defineCommand("help").defineCommand("quit").defineCommand("install, !installOption")
                .defineCommand("info");

        final String[] args = new String[1];
        args[0] = "znstolll";

        try {
            CmdLine.parse(args, listener);

            Assertions.fail();
        } catch (final UnsupportedException e) {
            final List<String> words = e.getSuggestionList();
            Assertions.assertEquals(5, words.size());
            Assertions.assertTrue(words.contains("file"));
            Assertions.assertTrue(words.contains("help"));
            Assertions.assertTrue(words.contains("quit"));
            Assertions.assertTrue(words.contains("install"));
            Assertions.assertTrue(words.contains("info"));
        } catch (final Exception e) {
            Assertions.fail(e.toString());
        }
    }
}
