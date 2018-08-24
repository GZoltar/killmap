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

