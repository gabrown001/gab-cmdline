

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

In order to parse the command line, you need to define what the commands are by calling `Cmdline.defineCommand("xxx");`

```java
CmdLine.defineCommand("-help, #print this message")
```

The string used in the defineCommand() method, contains tokens that must use one of these symbols in order for it to be recognized as that type:

\# = The description of the command. There may be zero to one defined.

! = A required value for the command name. There can be zero to many defined.

? = An optional value for the command name. There can be zero to many defined.

: = The regex value to match on for any values that are defined. There can be zero to one defined.

... = A value ends with ... and is a list for the command name. There can be zero to one defined. This can be used with the ! and ? symbols

If a token does not start with one of these tokens, then it is considered a command name.  There can be one to many  names that represent a single command, such as: 'f', 'file', 'filename' or '-f', '--file', '--filename'.

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
CmdLine.defineCommand("-help, #print this message")
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
final List<command> = CmdLine.parse( args, listener );
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
