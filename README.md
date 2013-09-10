Javali Compiler with Global Type Inference
==========================================

Prerequisites
-------------

Make sure that you have installed JDK for Java 7.
The project uses the build system Maven. On Ubuntu, install it using

```bash
sudo apt-get install maven
```

Maven will fetch all dependencies, generate the parser, compile the code
and build the resulting JAR file. The dependencies include:

- [ANTLR 3](http://www.antlr.org/wiki/display/ANTLR3/ANTLR+3+Wiki+Home) (for generating the parser)
- [JUnit 4](http://junit.org/) (for unit testing)
- [SLF4J](http://www.slf4j.org/) and [Logback](http://logback.qos.ch/) (for logging)
- [Apache Commons Lang](http://commons.apache.org/proper/commons-lang/)
- [Apache Commons IO](http://commons.apache.org/proper/commons-io/)
- [Apache Commons Math](http://commons.apache.org/proper/commons-math/)
- [Apache Commons CLI](http://commons.apache.org/proper/commons-cli/)
- [Google Guava](http://code.google.com/p/guava-libraries/) (various data structures, Optional, preconditions)


Building
--------

Run the following to build the whole compiler:

```bash
mvn install
```


Running
-------

To compile a single Javali source file 'foo.javali', run

```bash
./javalic foo.javali
```

To get the list of available options (type inference mode, optimization), run

```bash
./javalic --help
```


Testing
-------

The Javali files for end-to-end tests are contained in `src/test/javali`.
We reuse the reference compiler, but in cases where the output of the
reference compiler deviates from the expected output of our compiler,
we use overrides. This also applies to Javali files that use the extended
syntax as described in the report (`var` keyword).

Overrides are represented as files with an `override` suffix. We run the
tests for all Javali files using each implementation of type inference,
and also without any type inference. To that end, we use different
suffixes depending on what type of local inference the override applies to.

- `.override` applies to any type inference implementation
- `.override.gti` applies only to global type inference
- `.override.ltiwc` applies to local type inference with constraints
- `.override.ltil` applies to the efficient local type inference

Example:

- `arrays/InvariantArrayTest.javali.semantic.ref.override`

  The first line is empty, indicating that our compiler with type inference
  should not yield a semantic error (in contrast to the reference compiler).
  The remaining lines usually contain comments explaining why the override
  exists.