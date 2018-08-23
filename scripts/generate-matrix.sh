#!/usr/bin/env bash

HERE=${BASH_SOURCE[0]}
source "$(dirname "$HERE")/utils.sh" || exit 1

USAGE="$0 [--partial-output FILE] PROJECT BUG DIR MUTANTS_LOG [--KILLMAP-OPTION ...]"

DOC="Usage: $USAGE

Generates the test-outcome matrix for the given Defects4J project,
printing it as a CSV to stdout. Uses the given DIR as a directory for
scratch work. Details about all generated mutants (i.e. Major's
mutants.log output file) will be written to MUTANTS_LOG.

--partial-output may indicate the output of a previous run, possibly
missing some lines, as a cache to avoid re-running tests from previous
iterations."

if user_is_asking_for_help "$@"; then
  echo "$DOC"
  exit 0
fi

while [ "${1:0:2}" = '--' ]; do
  OPTION=$1; shift
  case "$OPTION" in
    ('--partial-output') PARTIAL=$1; shift ;;
    (*) echo "Usage: $USAGE" >&2; exit 1 ;;
  esac
done

if [ "$#" -lt 4 ]; then
  echo "Usage: $USAGE" >&2
  exit 1
fi

PROJECT="$1"; shift
BUG="$1"; shift
DIR="$1"; shift
MUTANTS_LOG="$1"; shift
KILLMAP_OPTIONS=("$@")

d4j_checkout_and_prepare "$PROJECT" "$BUG" "$DIR" >&2 || exit 1
cat "$DIR/mutants.log" > "$MUTANTS_LOG" || exit 1

# Make all the paths absolute, because we may change directories
# to actually generate the matrix.

if [ -z "$PARTIAL" ]; then
  PARTIAL='/dev/null'
elif [ "${PARTIAL:0:1}" != "/" ]; then
  PARTIAL=$(readlink --canonicalize "$PARTIAL")
fi
if [[ "$PARTIAL" = *.gz ]]; then
  NEW_PARTIAL="/tmp/killmap-$$-unzipped-partial-output"
  mkfifo "$NEW_PARTIAL"
  zcat "$PARTIAL" > "$NEW_PARTIAL" &
  PARTIAL="$NEW_PARTIAL"
fi

cd "$DIR" || exit 1
d4j_generate_matrix_here "$PARTIAL" "${KILLMAP_OPTIONS[@]}"

# EOF

