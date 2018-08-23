#!/usr/bin/env bash
#
# Combines a partial killmap with the result of an incremental run
# (presumably the first run timed out), to produce a single, full
# killmap.
#
# Usage:
#   killmap-combiner.sh first-half.killmap.csv.gz second-half.killmap.csv.gz full.killmap.csv.gz
#
# The inputs should both be gzipped files; the output will also be
# gzipped. Exits with status 1 if it can't figure out how to glue the
# two halves together.
#

die() {
  echo "$@" >&2
  exit 1
}

USAGE="$0 BASE EXTENSION OUTPUT"
[ "$#" = 3 ] || die "Usage: $USAGE"

KILLMAP_BASE="$1"
KILLMAP_EXTENSION="$2"
KILLMAP_FULL="$3"

[ -f "$KILLMAP_BASE" ] || die "$KILLMAP_BASE does not exist"
[ -f "$KILLMAP_EXTENSION" ] || die "$KILLMAP_EXTENSION does not exist"

FIRST_EXTENSION_TEST_AND_MUTANT=$(zcat "$KILLMAP_EXTENSION" | head -n 1 | cut -d , -f 1,2)
(zcat "$KILLMAP_BASE" | sed -e "/^$FIRST_EXTENSION_TEST_AND_MUTANT,/q" | head -n -1; zcat "$KILLMAP_EXTENSION") | gzip > "$KILLMAP_FULL"

# EOF

