#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
test_dir=$(mktemp -d)
trap 'rm -rf "$test_dir"' EXIT
javac -source 8 -target 8 -d "$test_dir" src/main/java/xyz/kisde/kisdetweaks/util/BookEditor.java tests/BookEditorTest.java
java -ea -cp "$test_dir" BookEditorTest
