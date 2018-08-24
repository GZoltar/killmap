#!/usr/bin/env bash
#
# Copyright (C) 2018 Spencer Pearson, José Campos and killmap contributors.
# 
# This file is part of killmap.
# 
# killmap is free software: you can redistribute it and/or modify it under the terms of the GNU
# Lesser General Public License as published by the Free Software Foundation, either version 3 of
# the License, or (at your option) any later version.
# 
# killmap is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
# even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public License along with killmap.
# If not, see <https://www.gnu.org/licenses/>.
#
#
# Defines a bunch of little helper functions to glue together D4J and
# Killmap. You probably will not have to use this yourself -- it is
# mostly just scaffolding for `generate-matrix.sh`.
#
# If you do use this yourself, source it, do not just run it, so you
# have access to the functions it defines.
#
# Usage:
#   source utils.sh
#

KILLMAP_SCRIPTS_HOME=$(dirname "${BASH_SOURCE[0]}")
KILLMAP_SCRIPTS_HOME=$(readlink --canonicalize "$KILLMAP_SCRIPTS_HOME")

KILLMAP_HOME=$(readlink --canonicalize "$KILLMAP_SCRIPTS_HOME/..")
KILLMAP_JAR="$KILLMAP_HOME/bin/killmap-0.0.1-SNAPSHOT.jar"

if [ ! "$DEFECTS4J_HOME" ]; then
  echo 'Error: DEFECTS4J_HOME not set' >&2
  return 1
fi
if [ ! -f "$KILLMAP_JAR" ]; then
  echo "Error: $KILLMAP_JAR does not exist" >&2
  return 1
fi

export PATH="$PATH:$DEFECTS4J_HOME/framework/bin"
export PATH="$PATH:$DEFECTS4J_HOME/framework/util"
export PATH="$DEFECTS4J_HOME/major/bin:$PATH"

##
# Exits with status 0 if any argument is -h or --help, else 1.
##
user_is_asking_for_help() {
  for arg in "$@"; do
    if [ "$arg" = "-h" -o "$arg" = "--help" ]; then
      return 0;
    fi
  done
  return 1
}

##
# Creates the MML file telling Major how to mutate the given project/bug.
##
d4j_create_mml() {
  local USAGE='d4j_create_mml PROJECT BUG DEST'
  if user_is_asking_for_help "$@"; then
    echo "Usage: $USAGE"
    echo 'Prints the name of the newly created compiled MML file (e.g. 1.mml.bin).'
    return 0
  fi
  if [ "$#" != 3 ]; then
    echo "Usage: $USAGE"
    return 1
  fi

  local PROJECT=$1
  local BUG=$2
  local DEST=$3

  local TEMPDIR
  TEMPDIR=$(mktemp -d "/tmp/mml-$PROJECT-XXXX") &&
  "$DEFECTS4J_HOME/framework/util/create_mml.pl" -p "$PROJECT" -b "$BUG" -o "$TEMPDIR" -c "$DEFECTS4J_HOME/framework/projects/$PROJECT/loaded_classes/$BUG.src" &&
  mv "$TEMPDIR/$BUG.mml.bin" "$DEST" &&
  rm -rf "$TEMPDIR"
}

##
# Compiles the D4J project in the named directory, with mutants.
# At the moment, the mutated classes live in ".classes_mutated"
# underneath the given directory.
##
d4j_mutate() {
  local USAGE='d4j_mutate PROJECT BUG DIR'
  if user_is_asking_for_help "$@"; then
    echo "Usage: $USAGE"
    echo 'Compiles the Defects4J project in the current directory with mutants.'
    return 0
  fi
  if [ "$#" != 3 ]; then
    echo "Usage: $USAGE" >&2
    return 1
  fi

  local PROJECT=$1
  local BUG=$2
  local DIR=$(readlink --canonicalize "$3")

  local MML
  export MML=$(mktemp "$DIR/.mml.bin.XXXX") &&
  d4j_create_mml "$PROJECT" "$BUG" "$MML" &&
  (cd "$DIR" && # ant sometimes has trouble finding things unless we cd in
   PATH="$PATH:$DEFECTS4J_HOME/major/bin" \
    ant -Dd4j.home="$DEFECTS4J_HOME" \
        -Dbasedir="$(pwd)" \
        -f "$DEFECTS4J_HOME/framework/projects/defects4j.build.xml" \
        mutate) &&
  rm -rf "$MML"
}

##
# Does all necessary setup to get a freshly-checked-out D4J project ready
# for matrix-generation.
##
d4j_prepare() {
  local USAGE='d4j_prepare PROJECT BUG DIR'
  if user_is_asking_for_help "$@"; then
    echo "Usage: $USAGE"
    echo 'Compiles the Defects4J project in the current directory with mutants.'
    return 0
  fi
  if [ "$#" != 3 ]; then
    echo "Usage: $USAGE" >&2
    return 1
  fi

  local PROJECT=$1
  local BUG=$2
  local DIR=$3

  (cd "$DIR" &&
   defects4j export -p cp.test -o d4j-cp.test.txt &&
   cat "$DEFECTS4J_HOME/framework/projects/$PROJECT/trigger_tests/$BUG" > triggering-tests.txt &&
   cat "$DEFECTS4J_HOME/framework/projects/$PROJECT/relevant_tests/$BUG" > relevant-test-classes.txt) \
  || return 1
  d4j_mutate "$PROJECT" "$BUG" "$DIR"
  (cd "$DIR" && defects4j compile) || return 1
}

##
# Once in a while, when the matrix-generator runs a test, something gets
# printed to stdout somehow. This function filters out any lines that
# don't look like matrix-generator output.
##
filter_dumb_lines_printed_by_tests() {
  egrep -a '^[a-zA-Z0-9_$.]+#[a-zA-Z0-9_]+,[0-9]+,[0-9]+,'
}

##
# Check out a D4J project and get it ready for matrix-generation.
##
d4j_checkout_and_prepare() {
  local USAGE='d4j_checkout_and_prepare PROJECT BUG DIR'
  if user_is_asking_for_help "$@"; then
    echo "Usage: $USAGE"
    echo 'Checks out the given project version, then prepares it for matrix generation.'
    return 0
  fi
  if [ "$#" != 3 ]; then
    echo "Usage: $USAGE" >&2
    return 1
  fi
  local PROJECT=$1
  local BUG=$2
  local DIR=$3

  rm -rf "$DIR"
  export GRADLE_USER_HOME="$DIR/.gradle-local-home"
  defects4j checkout -p "$PROJECT" -v "${BUG}b" -w "$DIR" &&
    d4j_prepare "$PROJECT" "$BUG" "$DIR"
}

##
# Run Killmap on the D4J project in the current directory, prints the
# matrix to stdout and debugging/progress/timing information to stderr.
##
d4j_generate_matrix_here() {
  local USAGE="d4j_generate_matrix_here [--help] KILLMAP_ARGS"
  if user_is_asking_for_help "$@"; then
    echo 'Generates the test-outcome matrix for the current Defects4J project,'
    echo 'using PARTIAL as a cache to avoid re-running tests from previous iterations.'
    return 0
  fi

  export TZ='America/Los_Angeles'
  export KILLMAP_CLASSPATH=".classes_mutated:$DEFECTS4J_HOME/framework/projects/lib/junit-4.11.jar:$(cat d4j-cp.test.txt | tr -d '\n'):$KILLMAP_JAR"
  time java -cp "$KILLMAP_CLASSPATH" killmap.Main triggering-tests.txt relevant-test-classes.txt "$@" | filter_dumb_lines_printed_by_tests
}

# EOF

