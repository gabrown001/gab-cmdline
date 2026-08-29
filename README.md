

GAB-CmdLine
=======

The GAB Social Command Line Parser for Java.  The purpose of this project is to analyze and examine how I would create a command line parser for Java.  Comments are welcome.  Thank you.


Required
---------
This project requires the following: 

    * Java 17
    * Maven


Maven Dependency
---------
```java
<dependency>
   <groupId>com.gabstudios</groupId>
   <artifactId>gab-cmdline</artifactId>
   <version>1.0.0-SNAPSHOT</version>
</dependency>

```



Build
---------
Use Maven to build - `mvn package`.

Usage
---------

Construct a parser, then define what the commands are by calling `defineCommand("xxx")` on it.

```java
CmdLine cmdLine = new CmdLine();
cmdLine.defineCommand("-help, #print this message");
```

Instances share no state, so two parsers can coexist and a test can build one per case
rather than clearing a global.

The string used in the defineCommand() method, contains tokens that must use one of these symbols in order for it to be recognized as that type:

\# = The description of the command. There may be zero to one defined.

! = A required value for the command name. There can be zero to many defined.

? = An optional value for the command name. There can be zero to many defined.

: = The regex value to match on for any values that are defined. There can be zero to one defined.

... = A value ends with ... and is a list for the command name. There can be zero to one defined. This can be used with the ! and ? symbols

If a token does not start with one of these tokens, then it is considered a command name.  There can be one to many  names that represent a single command, such as: 'f', 'file', 'filename' or '-f', '--file', '--filename'.

Three things are worth knowing before you write your first definition:

* **The description runs to the end of the definition.** Everything from the first `#` is
  description, so it may contain commas and equals signs.

* **A value name is scoped to its command.** Two commands may each declare a `!file`.
  Values are read back by that name, not by the option that carried them —
  `--upload, !file` is read as `command.getValues("file")`.

* **Parsing no arguments is not an error.** `parse(new String[0])` returns an empty list,
  so an application that shows help in that case does not have to special-case it first.

Example
---------

```text
myApp [commands] [option1 [option2 [option3] ...]]
  Commands: 
  -help                  print this message
  -version               print the version information and exit
  -quiet                 be extra quiet
  -verbose               be extra verbose
  -debug                 print debugging information
  -logfile <file>        use given file for log
  -logger <classname>    the class which is to perform logging
  -listener <classname>  add an instance of class as a project listener
  -D<property>=<value>   use value for given property
  -find <file>           search for file towards the root of the
                         filesystem and use it
```

```java
// define a listener implementation of the CommandListener interface.
private class CmdLineListener implements CommandListener
{
    @Override
    public void handle(final Command command)
    {
        System.out.println( command );
    }
}
// create an instance of the listener.
final CmdLineListener listener = new CmdLineListener();

// define/declare the commands the parser should parse.
// command names can start with any character that is not reserved.  reserved are !?#:
// the commands listed below use the - (dash) to denote a command, but this is not required.
CmdLine cmdLine = new CmdLine();
cmdLine.defineCommand("-help, #print this message")
       .defineCommand("-version, #print the version information and exit")
       .defineCommand("-quiet, #be extra quiet")
       .defineCommand("-verbose, #be extra verbose")
       .defineCommand("-debug, #print debugging information")
       .defineCommand("-logfile, !logFile, #use given file for log")
       .defineCommand("-logger, !logClass, #the class which is to perform logging")
       .defineCommand("-listener, !listenerClass, #add an instance of class as a project listener")
       .defineCommand("-find, !buildFile, #search for file towards the root of the file system and use it");

Note:  The format of "-D<property>=<value>" is automatically supported and doesnt need to be defined.  
If a -D<property>=<value> is seen on the command line, it is parsed and set 
in the System properties.  In addition, a command is created and sent to the listener.

// parse the command line args and pass matching commands to the listener for processing.
final List<Command> commands = cmdLine.parse( args, listener );
```
Click for more [examples].


More Documentation
------------------
Check the project [wiki].


Copyright
-------
[Copyright 2016-2025 Gregory Brown]


License
-------
This codebase is licensed under the [Apache v2.0 License].


Feedback
---------
Comments and feedback are greatly appreciated!!!


[Copyright 2016-2025 Gregory Brown]: https://github.com/gabrown001/gab-cmdline/tree/master/COPYRIGHT.txt
[Apache v2.0 License]: https://github.com/gabrown001/gab-cmdline/tree/master/LICENSE.txt
[wiki]: https://github.com/gabrown001/gab-cmdline/wiki
[examples]: https://github.com/gabrown001/gab-cmdline/wiki/Examples
