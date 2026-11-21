#!/bin/bash

# Writes the metrics collected by selfcompile.sh (the "stats" file) to the table between the
# "metrics:start" and "metrics:end" markers at the bottom of README.md.

# -------- CONFIGURATION --------
bar_length=40
readme_file="README.md"
stats_file="stats"
# --------------------------------

blocks=(" " "▏" "▎" "▍" "▌" "▋" "▊" "▉" "█")

stat() {
  grep "^$1=" "$stats_file" | cut -d= -f2
}

progress_bar() {
  local percent=$1
  local filled_float filled_int partial_index bar=""
  filled_float=$(echo "$percent * $bar_length / 100" | bc -l)
  filled_int=$(echo "$filled_float" | awk '{printf("%d", int($1))}')
  partial_index=$(echo "($filled_float - $filled_int) * 8" | bc -l | awk '{printf("%d", $1 + 0.5)}')
  for ((i = 0; i < filled_int; i++)); do bar+="█"; done
  if ((filled_int < bar_length)); then
    local empty_blocks=$((bar_length - filled_int))
    if ((partial_index > 0 && partial_index < 8)); then
      bar+="${blocks[$partial_index]}"
      empty_blocks=$((empty_blocks - 1))
    fi
    for ((i = 0; i < empty_blocks; i++)); do bar+="░"; done
  fi
  echo "$bar"
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
  percent=$(stat progress)
  percent=$(echo "$percent > 100" | bc -l | grep -q 1 && echo 100 || echo "$percent")
  progress_row="$(progress_bar "$percent") $(printf "%.1f" "$percent")%"
  time_row=$(ratio "$(stat orzo_ms)" "$(stat javac_ms)" 1000 s)
  size_row=$(ratio "$(stat orzo_bytes)" "$(stat javac_bytes)" 1024 KiB)
fi

table=$(
  cat <<EOF
<!-- metrics:start -->
| Metric | Value |
|--------|-------|
| Self-compilation (share of source lines compiled by Orzo) | $progress_row |
| Compile time (compiling Orzo's sources) | $time_row |
| Class size (Orzo's class files) | $size_row |
<!-- metrics:end -->
EOF
)

awk -v table="$table" '
  /<!-- metrics:start -->/ { print table; skip = 1; next }
  /<!-- metrics:end -->/ { skip = 0; next }
  !skip { print }
' "$readme_file" > "$readme_file.tmp" && mv "$readme_file.tmp" "$readme_file"
