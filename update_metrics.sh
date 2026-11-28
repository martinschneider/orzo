#!/bin/bash

# Writes the metrics collected by selfcompile.sh (the "stats" file) to the table between the
# "metrics:start" and "metrics:end" markers at the bottom of README.md.

readme_file="README.md"
stats_file="stats"

stat() {
  grep "^$1=" "$stats_file" | cut -d= -f2
}

# "4.2x javac (13.1 s vs. 3.1 s)"
ratio() {
  local orzo=$1 javac=$2 scale=$3 unit=$4
  awk -v o="$orzo" -v j="$javac" -v s="$scale" -v u="$unit" \
    'BEGIN { printf("%.1fx javac (%.1f %s vs. %.1f %s)", o / j, o / s, u, j / s, u) }'
}

SUMMARY="target/failsafe-reports/failsafe-summary.xml"
failures=$(xmlstarlet sel -t -v "/failsafe-summary/failures" "$SUMMARY")
errors=$(xmlstarlet sel -t -v "/failsafe-summary/errors" "$SUMMARY")
if [ "$failures" != "0" ] || [ "$errors" != "0" ] || [ ! -f "$stats_file" ]; then
  echo "Tests failed ($failures failures, $errors errors) or no stats: metrics are n/a"
  progress_row="n/a"
  time_row="n/a"
  size_row="n/a"
else
  progress_row="$(printf "%.1f" "$(stat progress)")%"
  time_row=$(ratio "$(stat orzo_ms)" "$(stat javac_ms)" 1000 s)
  size_row=$(ratio "$(stat orzo_bytes)" "$(stat javac_bytes)" 1024 KiB)
fi

# measured separately by compliance/jtreg.sh (it takes a while)
compliance_file="compliance/result.properties"
compliance() { # "47.4% (827 of 1746)" for positive or negative
  if [ -f "$compliance_file" ]; then
    awk -F= -v kind="$1" '
      { v[$1] = $2 }
      END {
        printf("%.1f%% (%d of %d)", 100 * v[kind "_passed"] / v[kind "_total"], v[kind "_passed"],
          v[kind "_total"])
      }' "$compliance_file"
  else
    echo "n/a"
  fi
}

table=$(
  cat <<EOF
<!-- metrics:start -->
| Metric | Value |
|--------|-------|
| Self-compilation | $progress_row |
| [jtreg](https://openjdk.org/jtreg/) positive tests (must compile and run) | $(compliance positive) |
| jtreg negative tests (must be rejected) | $(compliance negative) |
| Compile time (Orzo's sources) | $time_row |
| Class size (Orzo's class files)<sup>1</sup> | $size_row |
<!-- metrics:end -->
EOF
)

awk -v table="$table" '
  /<!-- metrics:start -->/ { print table; skip = 1; next }
  /<!-- metrics:end -->/ { skip = 0; next }
  !skip { print }
' "$readme_file" > "$readme_file.tmp" && mv "$readme_file.tmp" "$readme_file"
