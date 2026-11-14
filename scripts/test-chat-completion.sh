#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
test_dir=$(mktemp -d)
trap 'rm -rf "$test_dir"' EXIT
javac -source 8 -target 8 -d "$test_dir" src/main/java/xyz/kisde/kisdetweaks/util/{ChatCompletion,SuggestionMatching,NbtSuggestions,NbtSchemas}.java tests/ChatCompletionTest.java tests/NbtSuggestionsTest.java tests/SuggestionMatchingTest.java
java -ea -cp "$test_dir" ChatCompletionTest
java -ea -cp "$test_dir" SuggestionMatchingTest
java -ea -cp "$test_dir" NbtSuggestionsTest
