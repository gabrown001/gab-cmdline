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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Gregory Brown (sysdevone)
 */
public class CmdLineTest {
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
    public void testDefineCommand() {

        final CmdLineListener listener = new CmdLineListener();
        CmdLine.setCommandListener(listener);

        CmdLine.defineCommand("file , !fileName1,:file\\d.txt,       #Load files into the system");

        final String[] args = new String[1];
        args[0] = "file=file1.txt";

        try {
            CmdLine.parse(args);

            Assertions.assertTrue(listener.getCount() == 1);

            final Command command = listener.getCommand("file");
            Assertions.assertTrue(command != null);

            final List<String> values = command.getValues("fileName1");
            Assertions.assertTrue(values != null);
            Assertions.assertTrue(values.size() > 0);

            final String value = values.get(0);
            Assertions.assertTrue(value != null);
            Assertions.assertTrue(value.length() > 0);
            Assertions.assertTrue(value.equals("file1.txt"));

            Assertions.assertTrue(true);
        } catch (final Exception e) {
            Assertions.fail(e.toString());
        }

    }

    @Test
    public void testDefineCommand1() {
        final CmdLineListener listener = new CmdLineListener();
        CmdLine.setCommandListener(listener);

        CmdLine.defineCommand("file, !fileName1, :file\\d.txt, #Load a files into the system");

        final String[] args = new String[1];
        args[0] = "file = file1.txt";

        try {
            CmdLine.parse(args);

            Assertions.assertTrue(listener.getCount() == 1);

            final Command command = listener.getCommand("file");
            Assertions.assertTrue(command != null);
            Assertions.assertTrue(command.hasVariables());

            final List<String> values = command.getValues("fileName1");
            Assertions.assertTrue(values != null);
            Assertions.assertTrue(values.size() > 0);

            final String value = values.get(0);
            Assertions.assertTrue(value != null);
            Assertions.assertTrue(value.length() > 0);
            Assertions.assertTrue(value.equals("file1.txt"));

            Assertions.assertTrue(true);
        } catch (final Exception e) {
            Assertions.fail(e.toString());
        }

    }

    @Test
    public void testDefineCommand1a() {
        final CmdLineListener listener = new CmdLineListener();
        CmdLine.setCommandListener(listener);

        CmdLine.defineCommand(
                "-f, --file, !fileName1, ?fileName2, ?fileName3, :file\\d.txt, #Load a files into the system");

        final String[] args = new String[1];
        args[0] = "-f=file1.txt, file2.txt, file3.txt";

        try {
            CmdLine.parse(args);

            Assertions.assertTrue(listener.getCount() == 1);

            final Command command = listener.getCommand("-f");
            Assertions.assertTrue(command != null);
            Assertions.assertTrue(command.hasVariables());

            List<String> values = command.getValues("fileName1");
            Assertions.assertTrue(values != null);
            Assertions.assertTrue(values.size() == 1);

            String value = values.get(0);
            Assertions.assertTrue(value != null);
            Assertions.assertTrue(value.length() > 0);
            Assertions.assertTrue(value.equals("file1.txt"));

            values = command.getValues("fileName2");
            Assertions.assertTrue(values != null);
            Assertions.assertTrue(values.size() == 1);

            value = values.get(0);
            Assertions.assertTrue(value != null);
            Assertions.assertTrue(value.length() > 0);
            Assertions.assertTrue(value.equals("file2.txt"));

            values = command.getValues("fileName3");
            Assertions.assertTrue(values != null);
            Assertions.assertTrue(values.size() == 1);

            value = values.get(0);
            Assertions.assertTrue(value != null);
            Assertions.assertTrue(value.length() > 0);
            Assertions.assertTrue(value.equals("file3.txt"));

            Assertions.assertTrue(true);
        } catch (final Exception e) {
            Assertions.fail(e.toString());
        }
    }

    @Test
    public void testDefineCommand1b() {
        final CmdLineListener listener = new CmdLineListener();
        CmdLine.setCommandListener(listener);

        CmdLine.defineCommand(
                "-f, --file, !fileName1, ?fileName2, ?fileName3, :file\\d.txt, #Load a files into the system");

        final String[] args = new String[3];
        args[0] = "-f";
        args[1] = "=";
        args[2] = "file1.txt";

        try {
            CmdLine.parse(args);

            Assertions.assertTrue(listener.getCount() == 1);

            final Command command = listener.getCommand("-f");
            Assertions.assertTrue(command != null);
            Assertions.assertTrue(command.hasVariables());

            final List<String> values = command.getValues("fileName1");
            Assertions.assertTrue(values != null);
            Assertions.assertTrue(values.size() > 0);

            final String value = values.get(0);
            Assertions.assertTrue(value != null);
            Assertions.assertTrue(value.length() > 0);
            Assertions.assertTrue(value.equals("file1.txt"));

            Assertions.assertTrue(true);
        } catch (final Exception e) {
            Assertions.fail(e.toString());
        }
    }

    @Test
    public void testDefineCommand1c() {
        final CmdLineListener listener = new CmdLineListener();
        CmdLine.setCommandListener(listener);

        CmdLine.defineCommand("-f, --file, !fileName1, ?fileName2, ?fileName3, #Load a files into the system");

        final String[] args = new String[3];
        args[0] = "-f";
        args[1] = "=";
        args[2] = "file1.txt";

        try {
            CmdLine.parse(args);

            Assertions.assertTrue(listener.getCount() == 1);

            final Command command = listener.getCommand("-f");
            Assertions.assertTrue(command != null);
            Assertions.assertTrue(command.hasVariables());

            final List<String> values = command.getValues("fileName1");
            Assertions.assertTrue(values != null);
            Assertions.assertTrue(values.size() == 1);

            final String value = values.get(0);
            Assertions.assertTrue(value != null);
            Assertions.assertTrue(value.length() > 0);
            Assertions.assertTrue(value.equals("file1.txt"));

            Assertions.assertTrue(true);
        } catch (final Exception e) {
            Assertions.fail(e.toString());
        }
    }

    @Test
    public void testDefineCommand1d() {
        final CmdLineListener listener = new CmdLineListener();
        CmdLine.setCommandListener(listener);

        CmdLine.defineCommand("-f, --file, !fileName1, ?fileNames..., #Load a files into the system");

        final String[] args = new String[7];
        args[0] = "-f";
        args[1] = "=";
        args[2] = "file1.txt";
        args[3] = ",";
        args[4] = "file2.txt";
        args[5] = ",";
        args[6] = "file3.txt";

        try {
            CmdLine.parse(args);

            Assertions.assertTrue(listener.getCount() == 1);

            final Command command = listener.getCommand("-f");
            Assertions.assertTrue(command != null);
            Assertions.assertTrue(command.hasVariables());

            List<String> values = command.getValues("fileName1");
            Assertions.assertTrue(values != null);
            Assertions.assertTrue(values.size() == 1);

            String value = values.get(0);
            Assertions.assertTrue(value != null);
            Assertions.assertTrue(value.length() > 0);
            Assertions.assertTrue(value.equals("file1.txt"));

            values = command.getValues("fileNames");
            Assertions.assertTrue(values != null);
            Assertions.assertTrue(values.size() == 2);

            value = values.get(0);
            Assertions.assertTrue(value != null);
            Assertions.assertTrue(value.length() > 0);
            Assertions.assertTrue(value.equals("file2.txt"));

            value = values.get(1);
            Assertions.assertTrue(value != null);
            Assertions.assertTrue(value.length() > 0);
            Assertions.assertTrue(value.equals("file3.txt"));

            Assertions.assertTrue(true);
        } catch (final Exception e) {
            Assertions.fail(e.toString());
        }
    }

    @Test
    public void testDefineCommand1e() {
        final CmdLineListener listener = new CmdLineListener();
        CmdLine.setCommandListener(listener);

        CmdLine.defineCommand("-f, --file, !fileName1, ?fileNames..., #Load a files into the system, file");

        final String[] args = new String[7];
        args[0] = "file";
        args[1] = "=";
        args[2] = "file1.txt";
        args[3] = ",";
        args[4] = "file2.txt";
        args[5] = ",";
        args[6] = "file3.txt";

        try {
            CmdLine.parse(args);

            Assertions.assertTrue(listener.getCount() == 1);

            final Command command = listener.getCommand("file");
            Assertions.assertTrue(command != null);
            Assertions.assertTrue(command.hasVariables());

            List<String> values = command.getValues("fileName1");
            Assertions.assertTrue(values != null);
            Assertions.assertTrue(values.size() == 1);

            String value = values.get(0);
            Assertions.assertTrue(value != null);
            Assertions.assertTrue(value.length() > 0);
            Assertions.assertTrue(value.equals("file1.txt"));

            values = command.getValues("fileNames");
            Assertions.assertTrue(values != null);
            Assertions.assertTrue(values.size() == 2);

            value = values.get(0);
            Assertions.assertTrue(value != null);
            Assertions.assertTrue(value.length() > 0);
            Assertions.assertTrue(value.equals("file2.txt"));

            value = values.get(1);
            Assertions.assertTrue(value != null);
            Assertions.assertTrue(value.length() > 0);
            Assertions.assertTrue(value.equals("file3.txt"));

            Assertions.assertTrue(true);
        } catch (final Exception e) {
            Assertions.fail(e.toString());
        }
    }

    @Test
    public void testDefineCommand1f() {
        final CmdLineListener listener = new CmdLineListener();
        CmdLine.setCommandListener(listener);

        CmdLine.defineCommand("file, !file");

        final String[] args = new String[3];
        args[0] = "file";
        args[1] = "=";
        args[2] = "file1.txt";

        try {
            CmdLine.parse(args);

            Assertions.assertTrue(listener.getCount() == 1);

            final Command command = listener.getCommand("file");
            Assertions.assertTrue(command != null);
            Assertions.assertTrue(command.hasVariables());

            List<String> values = command.getValues("file");
            Assertions.assertTrue(values != null);
            Assertions.assertTrue(values.size() == 1);

            String value = values.get(0);
            Assertions.assertTrue(value != null);
            Assertions.assertTrue(value.length() > 0);
            Assertions.assertTrue(value.equals("file1.txt"));

            Assertions.assertTrue(true);
        } catch (final Exception e) {
            Assertions.fail(e.toString());
        }
    }

    @Test
    public void testDefineCommand1g() {
        final CmdLineListener listener = new CmdLineListener();
        CmdLine.setCommandListener(listener);

        CmdLine.defineCommand("file, !file, !files...");

        final String[] args = new String[5];
        args[0] = "file";
        args[1] = "=";
        args[2] = "file1.txt";
        args[3] = ",";
        args[4] = "file2.txt";

        try {
            CmdLine.parse(args);

            Assertions.assertTrue(listener.getCount() == 1);

            final Command command = listener.getCommand("file");
            Assertions.assertTrue(command != null);
            Assertions.assertTrue(command.hasVariables());

            List<String> values = command.getValues("file");
            Assertions.assertTrue(values != null);
            Assertions.assertTrue(values.size() == 1);

            String value = values.get(0);
            Assertions.assertTrue(value != null);
            Assertions.assertTrue(value.length() > 0);
            Assertions.assertTrue(value.equals("file1.txt"));

            Assertions.assertTrue(true);
        } catch (final Exception e) {
            Assertions.fail(e.toString());
        }
    }

    @Test
    public void testParseWithListener() {
        final CmdLineListener listener = new CmdLineListener();

        CmdLine.defineCommand("file, !file, !files...");

        final String[] args = new String[5];
        args[0] = "file";
        args[1] = "=";
        args[2] = "file1.txt";
        args[3] = ",";
        args[4] = "file2.txt";

        try {
            CmdLine.parse(args, listener);

            Assertions.assertTrue(listener.getCount() == 1);

            final Command command = listener.getCommand("file");
            Assertions.assertTrue(command != null);
            Assertions.assertTrue(command.hasVariables());

            List<String> values = command.getValues("file");
            Assertions.assertTrue(values != null);
            Assertions.assertTrue(values.size() == 1);

            String value = values.get(0);
            Assertions.assertTrue(value != null);
            Assertions.assertTrue(value.length() > 0);
            Assertions.assertTrue(value.equals("file1.txt"));

            Assertions.assertTrue(true);
        } catch (final Exception e) {
            Assertions.fail(e.toString());
        }
    }

    @Test
    public void testChainingCommand() {
        final CmdLineListener listener = new CmdLineListener();

        final String[] args = new String[5];
        args[0] = "file";
        args[1] = "=";
        args[2] = "file1.txt";
        args[3] = ",";
        args[4] = "file2.txt";

        try {
            CmdLine.defineCommand("file, !file, !files...").parse(args, listener);

            Assertions.assertTrue(listener.getCount() == 1);

            final Command command = listener.getCommand("file");
            Assertions.assertTrue(command != null);
            Assertions.assertTrue(command.hasVariables());

            List<String> values = command.getValues("file");
            Assertions.assertTrue(values != null);
            Assertions.assertTrue(values.size() == 1);

            String value = values.get(0);
            Assertions.assertTrue(value != null);
            Assertions.assertTrue(value.length() > 0);
            Assertions.assertTrue(value.equals("file1.txt"));

            Assertions.assertTrue(true);
        } catch (final Exception e) {
            Assertions.fail(e.toString());
        }
    }

    @Test
    public void testDefineMultipleCommand1() {
        final CmdLineListener listener = new CmdLineListener();
        CmdLine.setCommandListener(listener);

        CmdLine.setVersion("1.1.0");
        CmdLine.defineCommand("-f", "--file", "!fileNames...", ":file\\d.txt", "#Load files into the system");
        CmdLine.defineCommand("-l", "--list", "#List the files loaded into the system");
        CmdLine.defineCommand("-q", "--quit", "#Quit the application");

        final String[] args = new String[4];
        args[0] = "-f";
        args[1] = "=";
        args[2] = "file1.txt";
        args[3] = "--list";

        try {
            CmdLine.parse(args);

            Assertions.assertTrue(listener.getCount() == 2);

            Command command = listener.getCommand("-f");
            Assertions.assertTrue(command != null);
            Assertions.assertTrue(command.hasVariables());

            final List<String> values = command.getValues("fileNames");
            Assertions.assertTrue(values != null);
            Assertions.assertTrue(values.size() == 1);

            final String value = values.get(0);
            Assertions.assertTrue(value != null);
            Assertions.assertTrue(value.length() > 0);
            Assertions.assertTrue(value.equals("file1.txt"));

            command = listener.getCommand("--list");
            Assertions.assertTrue(command != null);
            Assertions.assertTrue(!command.hasVariables());

            Assertions.assertTrue(true);
        } catch (final Exception e) {
            Assertions.fail(e.toString());
        }
    }

    @Test
    public void testDefineMultipleCommand2() {
        final CmdLineListener listener = new CmdLineListener();
        CmdLine.setApplicationName("myApp").setVersion("1.1.0")
                .defineCommand("-f", "--file", "!fileNames...", ":file\\d.txt", "#Load files into the system")
                .defineCommand("-l", "--list", "#List the files loaded into the system")
                .defineCommand("-q", "--quit", "#Quit the application");

        final String[] args = new String[5];
        args[0] = "-f";
        args[1] = "=";
        args[2] = "file1.txt";
        args[3] = "-Dcom.gabsocial.cmdline.debug=true";
        args[4] = "--list";

        try {
            CmdLine.parse(args, listener);

            Assertions.assertTrue(listener.getCount() == 3);

            Command command = listener.getCommand("-f");
            Assertions.assertTrue(command != null);
            Assertions.assertTrue(command.hasVariables());

            List<String> values = command.getValues("fileNames");
            Assertions.assertTrue(values != null);
            Assertions.assertTrue(values.size() == 1);

            String value = values.get(0);
            Assertions.assertTrue(value != null);
            Assertions.assertTrue(value.length() > 0);
            Assertions.assertTrue(value.equals("file1.txt"));

            command = listener.getCommand("-Dcom.gabsocial.cmdline.debug");
            Assertions.assertTrue(command != null);
            Assertions.assertTrue(command.hasVariables());

            values = command.getValues("com.gabsocial.cmdline.debug");
            Assertions.assertTrue(values != null);
            Assertions.assertTrue(values.size() == 1);

            value = values.get(0);
            Assertions.assertTrue(value != null);
            Assertions.assertTrue(value.length() > 0);
            Assertions.assertTrue(value.equals("true"));

            command = listener.getCommand("--list");
            Assertions.assertTrue(command != null);
            Assertions.assertTrue(!command.hasVariables());

            Assertions.assertTrue(true);
        } catch (final Exception e) {
            Assertions.fail(e.toString());
        }
    }

    @Test
    public void testDefineMultipleCommandWithDefaultListener() {
        CmdLine.setApplicationName("myApp").setVersion("1.1.0")
                .defineCommand("-f", "--file", "!fileNames...", ":file\\d.txt", "#Load files into the system")
                .defineCommand("-l", "--list", "#List the files loaded into the system")
                .defineCommand("-q", "--quit", "#Quit the application");

        final String[] args = new String[5];
        args[0] = "-f";
        args[1] = "=";
        args[2] = "file1.txt";
        args[3] = "-Dcom.gabsocial.cmdline.debug=true";
        args[4] = "--list";

        try {
            List<Command> commands = CmdLine.parse(args);
            Assertions.assertTrue(commands.size() == 3);

            for (Command command : commands) {
                final String name = command.getName();
                // System.out.println( "'" + command + "'");

                if (name.equals("-f")) {
                    Assertions.assertTrue(command != null);
                    Assertions.assertTrue(command.hasVariables());

                    List<String> values = command.getValues("fileNames");
                    Assertions.assertTrue(values != null);
                    Assertions.assertTrue(values.size() == 1);

                    String value = values.get(0);
                    Assertions.assertTrue(value != null);
                    Assertions.assertTrue(value.length() > 0);
                    Assertions.assertTrue(value.equals("file1.txt"));
                } else if (name.equals("-Dcom.gabsocial.cmdline.debug")) {
                    Assertions.assertTrue(command != null);
                    Assertions.assertTrue(command.hasVariables());

                    List<String> values = command.getValues("com.gabsocial.cmdline.debug");
                    Assertions.assertTrue(values != null);
                    Assertions.assertTrue(values.size() == 1);

                    String value = values.get(0);
                    Assertions.assertTrue(value != null);
                    Assertions.assertTrue(value.length() > 0);
                    Assertions.assertTrue(value.equals("true"));
                } else if (name.equals("--list")) {
                    Assertions.assertTrue(command != null);
                    Assertions.assertTrue(!command.hasVariables());
                }
            }
            Assertions.assertTrue(true);
        } catch (final Exception e) {
            e.printStackTrace();
            Assertions.fail(e.toString());
        }
    }

    @Test
    public void testDefineMultipleCommandWithDefaultListenerAndSecondListener() {
        final CmdLineListener listener = new CmdLineListener();
        CmdLine.setApplicationName("myApp").setVersion("1.1.0")
                .defineCommand("-f", "--file", "!fileNames...", ":file\\d.txt", "#Load files into the system")
                .defineCommand("-l", "--list", "#List the files loaded into the system")
                .defineCommand("-q", "--quit", "#Quit the application");

        final String[] args = new String[5];
        args[0] = "-f";
        args[1] = "=";
        args[2] = "file1.txt";
        args[3] = "-Dcom.gabsocial.cmdline.debug=true";
        args[4] = "--list";

        try {
            List<Command> commands = CmdLine.parse(args, listener);
            Assertions.assertTrue(commands.size() == 3);

            for (Command command : commands) {
                final String name = command.getName();
                // System.out.println( "'" + command + "'");

                if (name.equals("-f")) {
                    Assertions.assertTrue(command != null);
                    Assertions.assertTrue(command.hasVariables());

                    List<String> values = command.getValues("fileNames");
                    Assertions.assertTrue(values != null);
                    Assertions.assertTrue(values.size() == 1);

                    String value = values.get(0);
                    Assertions.assertTrue(value != null);
                    Assertions.assertTrue(value.length() > 0);
                    Assertions.assertTrue(value.equals("file1.txt"));
                } else if (name.equals("-Dcom.gabsocial.cmdline.debug")) {
                    Assertions.assertTrue(command != null);
                    Assertions.assertTrue(command.hasVariables());

                    List<String> values = command.getValues("com.gabsocial.cmdline.debug");
                    Assertions.assertTrue(values != null);
                    Assertions.assertTrue(values.size() == 1);

                    String value = values.get(0);
                    Assertions.assertTrue(value != null);
                    Assertions.assertTrue(value.length() > 0);
                    Assertions.assertTrue(value.equals("true"));
                } else if (name.equals("--list")) {
                    Assertions.assertTrue(command != null);
                    Assertions.assertTrue(!command.hasVariables());
                }
            }

            Assertions.assertTrue(listener.getCount() == 3);

            Command command = listener.getCommand("-f");
            Assertions.assertTrue(command != null);
            Assertions.assertTrue(command.hasVariables());

            List<String> values = command.getValues("fileNames");
            Assertions.assertTrue(values != null);
            Assertions.assertTrue(values.size() == 1);

            String value = values.get(0);
            Assertions.assertTrue(value != null);
            Assertions.assertTrue(value.length() > 0);
            Assertions.assertTrue(value.equals("file1.txt"));

            command = listener.getCommand("-Dcom.gabsocial.cmdline.debug");
            Assertions.assertTrue(command != null);
            Assertions.assertTrue(command.hasVariables());

            values = command.getValues("com.gabsocial.cmdline.debug");
            Assertions.assertTrue(values != null);
            Assertions.assertTrue(values.size() == 1);

            value = values.get(0);
            Assertions.assertTrue(value != null);
            Assertions.assertTrue(value.length() > 0);
            Assertions.assertTrue(value.equals("true"));

            command = listener.getCommand("--list");
            Assertions.assertTrue(command != null);
            Assertions.assertTrue(!command.hasVariables());

            Assertions.assertTrue(true);
        } catch (final Exception e) {
            e.printStackTrace();
            Assertions.fail(e.toString());
        }
    }

    @Test
    public void testSystemPropertyCommand1() {
        final CmdLineListener listener = new CmdLineListener();
        CmdLine.setCommandListener(listener);

        CmdLine.setVersion("1.1.0");

        final String[] args = new String[1];
        args[0] = "-Dcom.gabsocial.cmdline.debug=true";

        try {
            CmdLine.parse(args);

            final Command command = listener.getCommand("-Dcom.gabsocial.cmdline.debug");

            Assertions.assertTrue(listener.getCount() == 1);
            Assertions.assertTrue(command != null);
            Assertions.assertTrue(command.hasVariables());

            final List<String> values = command.getValues("com.gabsocial.cmdline.debug");
            Assertions.assertTrue(values != null);
            Assertions.assertTrue(values.size() == 1);

            final String value = values.get(0);
            Assertions.assertTrue(value != null);
            Assertions.assertTrue(value.length() > 0);
            Assertions.assertTrue(value.equals("true"));

            Assertions.assertTrue(true);
        } catch (final Exception e) {
            Assertions.fail(e.toString());
        }
    }

    @Test
    public void testSystemPropertyCommand2() {
        final CmdLineListener listener = new CmdLineListener();
        CmdLine.setCommandListener(listener);

        CmdLine.setVersion("1.1.0");

        final String[] args = new String[4];
        args[0] = "-Dcom.gabsocial.cmdline.debug=true";
        args[1] = "-Dcom.gabsocial.cmdline.screen=true";
        args[2] = "-Dcom.gabsocial.cmdline.gfx=true";
        args[3] = "-Dcom.gabsocial.cmdline.load=true";

        try {
            CmdLine.parse(args);

            Assertions.assertTrue(listener.getCount() == 4);

            final Command command = listener.getCommand("-Dcom.gabsocial.cmdline.debug");

            Assertions.assertTrue(command != null);
            Assertions.assertTrue(command.hasVariables());

            final List<String> values = command.getValues("com.gabsocial.cmdline.debug");
            Assertions.assertTrue(values != null);
            Assertions.assertTrue(values.size() == 1);

            final String value = values.get(0);
            Assertions.assertTrue(value != null);
            Assertions.assertTrue(value.length() > 0);
            Assertions.assertTrue(value.equals("true"));

            Assertions.assertTrue(true);
        } catch (final Exception e) {
            Assertions.fail(e.toString());
        }
    };

    @Test
    public void testSystemPropertyCommand3() {
        final CmdLineListener listener = new CmdLineListener();
        CmdLine.setCommandListener(listener);

        CmdLine.defineCommand("file, !file, !files..., :file\\d.txt");

        CmdLine.setVersion("1.1.0");

        final String[] args = new String[4];
        args[0] = "-Dcom.gabsocial.cmdline.debug=true";
        args[1] = "file";
        args[2] = "file1.txt";
        args[3] = "file2.txt";

        try {
            CmdLine.parse(args);

            Assertions.assertTrue(listener.getCount() == 2);

            Command command = listener.getCommand("-Dcom.gabsocial.cmdline.debug");

            Assertions.assertTrue(command != null);
            Assertions.assertTrue(command.hasVariables());

            List<String> values = command.getValues("com.gabsocial.cmdline.debug");
            Assertions.assertTrue(values != null);
            Assertions.assertTrue(values.size() == 1);

            String value = values.get(0);
            Assertions.assertTrue(value != null);
            Assertions.assertTrue(value.length() > 0);
            Assertions.assertTrue(value.equals("true"));

            command = listener.getCommand("file");

            Assertions.assertTrue(command != null);
            Assertions.assertTrue(command.hasVariables());

            values = command.getValues("file");
            Assertions.assertTrue(values != null);
            Assertions.assertTrue(values.size() == 1);

            value = values.get(0);
            Assertions.assertTrue(value != null);
            Assertions.assertTrue(value.length() > 0);
            Assertions.assertTrue(value.equals("file1.txt"));

            values = command.getValues("files");
            Assertions.assertTrue(values != null);
            Assertions.assertTrue(values.size() == 1);

            value = values.get(0);
            Assertions.assertTrue(value != null);
            Assertions.assertTrue(value.length() > 0);
            Assertions.assertTrue(value.equals("file2.txt"));

            Assertions.assertTrue(true);
        } catch (final Exception e) {
            Assertions.fail(e.toString());
        }
    }

    @Test
    public void testSystemPropertyCommand4() {
        final CmdLineListener listener = new CmdLineListener();

        CmdLine.defineCommand("file, !file, !files..., :file\\d.txt").setApplicationName("myApp").setVersion("1.1.0");

        final String[] args = new String[5];
        args[0] = "-Dcom.gabsocial.cmdline.debug=true";
        args[1] = "file";
        args[2] = "file1.txt";
        args[3] = "file2.txt";
        args[4] = "-Dcom.gabsocial.cmdline.load=true";

        try {
            CmdLine.parse(args, listener);

            Assertions.assertTrue(listener.getCount() == 3);

            Command command = listener.getCommand("-Dcom.gabsocial.cmdline.debug");

            Assertions.assertTrue(command != null);
            Assertions.assertTrue(command.hasVariables());

            List<String> values = command.getValues("com.gabsocial.cmdline.debug");
            Assertions.assertTrue(values != null);
            Assertions.assertTrue(values.size() == 1);

            String value = values.get(0);
            Assertions.assertTrue(value != null);
            Assertions.assertTrue(value.length() > 0);
            Assertions.assertTrue(value.equals("true"));

            command = listener.getCommand("file");

            Assertions.assertTrue(command != null);
            Assertions.assertTrue(command.hasVariables());

            values = command.getValues("file");
            Assertions.assertTrue(values != null);
            Assertions.assertTrue(values.size() == 1);

            value = values.get(0);
            Assertions.assertTrue(value != null);
            Assertions.assertTrue(value.length() > 0);
            Assertions.assertTrue(value.equals("file1.txt"));

            values = command.getValues("files");
            Assertions.assertTrue(values != null);
            Assertions.assertTrue(values.size() == 1);

            value = values.get(0);
            Assertions.assertTrue(value != null);
            Assertions.assertTrue(value.length() > 0);
            Assertions.assertTrue(value.equals("file2.txt"));

            command = listener.getCommand("-Dcom.gabsocial.cmdline.load");

            Assertions.assertTrue(command != null);
            Assertions.assertTrue(command.hasVariables());

            values = command.getValues("com.gabsocial.cmdline.load");
            Assertions.assertTrue(values != null);
            Assertions.assertTrue(values.size() == 1);

            value = values.get(0);
            Assertions.assertTrue(value != null);
            Assertions.assertTrue(value.length() > 0);
            Assertions.assertTrue(value.equals("true"));

            Assertions.assertTrue(true);
        } catch (final Exception e) {
            Assertions.fail(e.toString());
        }
    }

    @Test
    public void testTokenizer() {
        // file=file1.txt
        final String[] inputTokens = { "file=file1.txt" };

        final List<String> tokens = CmdLine.tokenize(inputTokens);

        Assertions.assertTrue(tokens.size() == 2);
        Assertions.assertTrue(tokens.get(0).equals("file"));
        Assertions.assertTrue(tokens.get(1).equals("file1.txt"));

    }

    @Test
    public void testTokenizer2() {
        // -file=file1,txt, file2.txt
        final String[] inputTokens = { "-file=file1.txt,", "file2.txt" };

        final List<String> tokens = CmdLine.tokenize(inputTokens);

        Assertions.assertTrue(tokens.size() == 3);
        Assertions.assertTrue(tokens.get(0).equals("-file"));
        Assertions.assertTrue(tokens.get(1).equals("file1.txt"));
        Assertions.assertTrue(tokens.get(2).equals("file2.txt"));

    }

    @Test
    public void testTokenizer3() {
        // -file=file1,txt, file2.txt -Dorg.gabsocial.cmdline.debug=true
        final String[] inputTokens = { "-file", "file1.txt", "file2.txt", "-Dorg.gabsocial.cmdline.debug=true" };

        final List<String> tokens = CmdLine.tokenize(inputTokens);

        Assertions.assertTrue(tokens.size() == 5);
        Assertions.assertTrue(tokens.get(0).equals("-file"));
        Assertions.assertTrue(tokens.get(1).equals("file1.txt"));
        Assertions.assertTrue(tokens.get(2).equals("file2.txt"));
        Assertions.assertTrue(tokens.get(3).equals("-Dorg.gabsocial.cmdline.debug"));
        Assertions.assertTrue(tokens.get(4).equals("true"));
    }
}
