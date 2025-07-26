#!/bin/bash

# -------- CONFIGURATION --------
bar_length=40
readme_file="README.md"
# --------------------------------

blocks=(" " "▏" "▎" "▍" "▌" "▋" "▊" "▉" "█")

# Check if tests passed
SUMMARY="target/failsafe-reports/failsafe-summary.xml"
failures=$(xmlstarlet sel -t -v "/failsafe-summary/failures" "$SUMMARY")
errors=$(xmlstarlet sel -t -v "/failsafe-summary/errors" "$SUMMARY")
if [ "$failures" != "0" ] || [ "$errors" != "0" ]; then
  echo "Test failed: $failures failures, $errors errors"
  sed -i "\$s/.*/Self-compilation progress: n\/a/" "$readme_file"
  exit 0
fi

percent=$(cat progress | tr -d '[:space:]')
percent=$(echo "$percent > 100" | bc -l | grep -q 1 && echo 100 || echo "$percent")

# Compute bar
filled_float=$(echo "$percent * $bar_length / 100" | bc -l)
filled_int=$(echo "$filled_float" | awk '{printf("%d", int($1))}')
partial_fraction=$(echo "$filled_float - $filled_int" | bc -l)
partial_index=$(echo "$partial_fraction * 8" | bc -l | awk '{printf("%d", $1 + 0.5)}')

# Build bar
if (( $(echo "$percent == 100" | bc -l) )); then
  bar=$(printf '█%.0s' $(seq 1 $bar_length))
else
  bar=""
  for ((i=0; i<filled_int; i++)); do bar+="█"; done
  if (( filled_int < bar_length )); then
    if (( partial_index > 0 && partial_index < 8 )); then
      bar+="${blocks[$partial_index]}"
      empty_blocks=$((bar_length - filled_int - 1))
    else
      empty_blocks=$((bar_length - filled_int))
    fi
    for ((i=0; i<empty_blocks; i++)); do bar+="░"; done
  fi
fi

display_percent=$(printf "%.1f" "$percent")
progress_line="Self-compilation progress: $bar $display_percent%"

escaped_line=$(printf '%s\n' "$progress_line" | sed -e 's/[\/&]/\\&/g')
sed -i "\$s/.*/$escaped_line/" "$readme_file"
