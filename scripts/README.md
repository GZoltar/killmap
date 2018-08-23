## Utility scripts to automatise mutation-analysis on [Defects4J](https://github.com/rjust/defects4j) bugs.

* [generate-matrix](generate-matrix.sh): generates a test-outcome matrix for a
given [Defects4J](https://github.com/rjust/defects4j) bug, printing it as a
CSV to the stdout. This script is responsible for checkout a
[Defects4J](https://github.com/rjust/defects4j) bug, compile and mutate it, and
run the `killmap` program.

* [killmap-combiner](killmap-combiner.sh): combines a partial output generated
by the `killmap` program with the result of an incremental run (presumably the
first run was interrupted halfway through or ran out of time), to produce a
single, full test-outcome matrix.

* [utils](utils.sh): helper functions to glue together
[Defects4J](https://github.com/rjust/defects4j) and `killmap`.
