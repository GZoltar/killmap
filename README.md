# Killmap

`killmap` is an utility Java program that allows developers to perform
mutation-analysis whose output could then be used to perform mutation-based
fault localization. Although `killmap` does not depend on the
[Defects4J](https://github.com/rjust/defects4j) infrastructure and therefore
could be used by just executing `java -jar killmap.jar ...`, the
[scripts](scripts/) provided to automatise the mutation-analysis have been
designed and developed to to be executed on
[Defects4J](https://github.com/rjust/defects4j) bugs.


### How to compile it?

```
ant -f build.xml jar
```


### How to use it?

```
java -cp <bin directory with mutated classes>:<classpath of the project under test>:__killmap_directory__/bin/killmap-<version>.jar \
  <triggering-tests> \
  <relevant-tests> \
  <partial-run> 2>err.txt | gzip > matrix.csv.gz
```

where:
- `<triggering-tests>` is the path to a file that contains a list of test
methods that trigger (expose) the bug (one per row, see
[Defects4J](https://github.com/rjust/defects4j) documentation for more
information)
- `<relevant-tests>` is the path to a file that contains a list of relevant
tests classes (one per row, see
[Defects4J](https://github.com/rjust/defects4j) documentation for more
information)
- `<partial-run>` is the path to a file that contains the output of a previous
run of the `killmap` program (in case it did not finish or it was interrupt).
This allows `killmap` to reuse information from a previous run, rather than
having to re-run everything from scratch. To this end, `<partial-run>` can
contain any subset of lines from a previous run, in the order they were
produced. Some examples:
  - The first time you run the `killmap` program, i.e., if there was no
  previous run, `/dev/null` is a good choice.
  - If the program is interrupted halfway through, you can run
  `java -jar ... --partial-output <(zcat matrix.csv.gz) ... > matrix-2.csv.gz`
  which will reuse the results in `matrix.csv.gz`.
  - If you think something went wrong with a particular test-run, you can
  delete that line from the matrix and re-run the program, passing in that
  matrix; every test-result from the original run will be reused, except the
  deleted result, which will be re-calculated.
- `matrix.csv.gz` is a test-outcome matrix which is written to the stdout
(where each line represents "the outcome of running a `<test>` with a
`<mutant>` enabled") and has the following form:

```
<test case>,<mutant id>,<timeout>,<outcome>,<runtime>,<output hash>,<covered mutants>,<stack trace>
```

- The `mutant id` is either a positive integer (i.e., a real mutant id) or 0,
meaning no mutant was enabled.
- The `timeout` is the number of milliseconds allocated for the test case to
run.
- The `outcome` is PASS/FAIL/TIMEOUT/CRASH, describing the general type of
outcome.
- The `runtime` is the number of milliseconds the test case actually took to
run.
- The `output hash` is the concatenation of two SHA-1 hashes: one of whatever
the test wrote to stdout, one of whatever it wrote to stderr.
- The `covered mutants` is empty unless the "mutant id" column is 0.
- The `stack trace` is the thrown exception's stack trace, if any.
Leading/trailing whitespace is stripped, and any bunch of whitespace including
a newline is replaced by a single space (i.e., `\s*\n\s*` is replaced with
` `). Beware that the stack trace may contain commas, therefore parsing this
as a CSV may return less columns than expected.


#### Usage example

Supposing you want to perform mutation-analysis on a specific bug of
[Defects4J](https://github.com/rjust/defects4j), e.g., Chart-1, a script
[generate-matrix](scripts/generate-matrix.sh) is provided to automatise the
analysis, i.e., to checkout Chart-1, compile and mutate it, and run the
`killmap` program. Here is an example of how to run
[generate-matrix](scripts/generate-matrix.sh) script.

```
$ export DEFECTS4J_HOME=__defects4j_directory__
$ mkdir /tmp/killmap_example/
$ bash scripts/generate-matrix.sh \
  Chart 1 /tmp/killmap_example/Chart-1b \
  /tmp/killmap_example/mutants.txt 2>/tmp/killmap_example/err.txt | gzip > /tmp/killmap_example/matrix.csv.gz
```

where `matrix.csv.gz` is a test-outcome matrix which is written to the stdout
and looks like:

```
org.jfree.chart.renderer.category.junit.AbstractCategoryItemRendererTests#test2947660,0,60000,FAIL,476,da39...709,1 11 12 13 ...,junit.framework.AssertionFailedError: ...
org.jfree.chart.renderer.category.junit.AbstractCategoryItemRendererTests#test2947660,1,952,FAIL,140,da39...709,,junit.framework.AssertionFailedError:
...
```

Assuming the message "Completed successfully!" appears at the end of `err.txt`
file, it means the mutation-analysis has finished successfully and no other
step or re-execution of the script is required. Otherwise, an incomplete
execution can be extended by executing the following command:

```
$ bash scripts/generate-matrix.sh \
  --partial-output /tmp/killmap_example/matrix.csv.gz \
  Chart 1 /tmp/killmap_example/Chart-1b \
  /tmp/killmap_example/mutants.txt 2>/tmp/killmap_example/err.txt | gzip > /tmp/killmap_example/matrix-2.csv.gz
```

In order to combine both test-outcome matrices (i.e., `matrix.csv.gz` and
`matrix-2.gz`) into a single matrix, a script
[killmap-combiner](scripts/killmap-combiner.sh) is provided and can be
executed as:
```
$ bash scripts/killmap-combiner.sh \
  matrix.csv.gz matrix-2.csv.gz \
  matrix-complete.csv.gz`
```


### How does Killmap work?

`killmap.Main` determines the outcome of every test and run each one with
every (or no) mutant. It makes use of two optimisations:

- if test T does not cover mutant M, its outcome with M enabled will be the
same as its outcome with no mutants enabled;
- if no triggering test changes because of M, the outcomes of passing tests
with M enabled are irrelevant.

So, `killmap.Main` needs to run every test with every mutant that (a) the test
covers and (b) at least one triggering test changes behaviour because of. To
do this, it:

- runs each failing test once with no mutants, then once for each mutant it
covers, recording which mutants change its behaviour;
- runs each passing test once with no mutants, then once for each mutant it
covers if that mutant changed the behaviour of any failing test.

And as it goes, it prints the result of every test-run.


#### How is an unit test case executed?

Most importantly, the tests are all run in a subprocess "worker JVM" because
tests might do nasty stuff like eat all the memory. From the perspective of
the "host JVM" (the main process), running tests looks like this:

1. (If necessary) Spawn a worker subprocess, by executing
`java ... killmap.TestRunner ...`. Listen on a certain port for the worker to
connect.
2. Over the socket, give the worker a "work order," consisting of a test to
run, a mutant to enable, and a timeout.
3. Wait for the worker to respond with the test-run's outcome.

If the worker ever fails to respond in step 3, the host kills it and, next
time a test needs to be run, it will spawn a new worker. All of this logic
lives in the `RemoteTestRunner` class.

From the worker's perspective, running a test looks like this:

1. Read a work order from the socket.
2. Replace `System.out` and `System.err` with dummy streams that can easily
eat up infinite amounts of data (because some tests could print infinite
amounts of data).
3. In a new thread, replace the thread's classloader with a fresh one (to
isolate the effects of the impending test-run); enable the given mutant; then
run the given test.
4. If that thread returns before the timeout expires, take the returned
outcome; otherwise, create an outcome meaning "timed out".
5. Write that outcome to the socket.

Almost all of that logic lives in the `TestRunner`. A little bit lives in
`IsolatingClassLoader` and `DeadEndDigestOutputStream`.

There are four kinds of outcome:

- `PASS`: the test completed in time, with all assertions passing.
- `FAIL`: the test completed in time, but by raising an exception rather than
passing.
- `TIMEOUT`: the test didn't complete in time, but the worker JVM was able to
terminate it and clean up successfully.
- `CRASH`: everything else. The test must have done something nasty (e.g.,
raised an `OutOfMemoryError`; made the worker completely unresponsive; refused
to halt when the thread was interrupted).
