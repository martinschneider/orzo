#!/bin/bash

# Measures Orzo's compliance with Java by running the self-contained javac tests of the OpenJDK
# test suite (test/langtools/tools/javac, see select.py) with jtreg: once with javac (the
# reference: only the tests that pass with javac count) and once with Orzo. Writes the result to
# compliance/result.properties (shown in README.md by update_metrics.sh).
#
#   JTREG_HOME=/path/to/jtreg compliance/jtreg.sh
#
# JTREG_HOME is a jtreg image, e.g. build/images/jtreg of https://github.com/openjdk/jtreg built
# with "make/build.sh --jdk <jdk>" (JT Harness needs java.applet, so build it with JDK 25 or older
# on the PATH). The tests run on the JDK of JAVA_HOME (or the "java" on the PATH). This takes a
# while (about 45 minutes for each compiler); the javac results are kept for the same OpenJDK
# commit.

set -e

OPENJDK_COMMIT=78bf06f425a99c26dbbc39b81aecf0912a50190b
CONCURRENCY=${CONCURRENCY:-4}

dir=$(cd "$(dirname "$0")" && pwd)
work=$dir/work
orzo=$(cd "$dir/.." && pwd)

if [ -z "$JTREG_HOME" ] || [ ! -x "$JTREG_HOME/bin/jtreg" ]; then
  echo "Set JTREG_HOME to a jtreg image (see the comment at the top of $0)" >&2
  exit 2
fi
jdk=${JAVA_HOME:-$(java -XshowSettings:properties -version 2>&1 | awk '/java.home/ { print $3 }')}
mkdir -p "$work"

# 1. the tests (a sparse checkout of the OpenJDK repository at a fixed commit)
tests=$work/jdk/test/langtools/tools/javac
if [ ! -d "$tests" ]; then
  echo "Fetching the javac tests of OpenJDK $OPENJDK_COMMIT"
  git init -q "$work/jdk"
  git -C "$work/jdk" remote add origin https://github.com/openjdk/jdk.git
  git -C "$work/jdk" fetch -q --depth 1 --filter=blob:none origin "$OPENJDK_COMMIT"
  git -C "$work/jdk" sparse-checkout set --skip-checks test/langtools/tools/javac test/langtools/TEST.ROOT
  git -C "$work/jdk" checkout -q FETCH_HEAD
  # (its groups refer to tests that aren't checked out)
  : > "$work/jdk/test/langtools/TEST.groups"
fi
python3 "$dir/select.py" "$tests" "$work/tests.txt"

# 2. a JDK image whose javac is Orzo
(cd "$orzo" && mvn -q clean package -DskipTests)
fake=$work/orzo-jdk
rm -rf "$fake"
mkdir -p "$fake/bin"
for f in "$jdk"/*; do
  [ "$(basename "$f")" = bin ] || ln -s "$f" "$fake/"
done
for f in "$jdk"/bin/*; do
  [ "$(basename "$f")" = javac ] || ln -s "$f" "$fake/bin/"
done
cp "$dir/javac" "$fake/bin/javac"
cp "$orzo/target/orzo.jar" "$fake/orzo.jar"

jtreg() {
  JAVA_HOME=$jdk "$JTREG_HOME/bin/jtreg" -jdk:"$jdk" -othervm -conc:"$CONCURRENCY" \
    -timeoutFactor:0.5 "$@" $(cat "$work/tests.txt") > /dev/null 2>&1 || true
}

# 3. the reference (javac) and Orzo
javac_work=$work/javac-$OPENJDK_COMMIT
if [ ! -f "$javac_work/done" ]; then
  echo "Running the tests with javac"
  rm -rf "$javac_work"
  jtreg -w:"$javac_work" -r:"$work/javac-report"
  touch "$javac_work/done"
fi
echo "Running the tests with Orzo"
rm -rf "$work/orzo"
jtreg -compilejdk:"$fake" -w:"$work/orzo" -r:"$work/orzo-report"

python3 "$dir/analyze.py" "$work/tests.txt" "$javac_work" "$work/orzo" \
  "$work/jdk/test/langtools" "$dir/result.properties" "$OPENJDK_COMMIT"
