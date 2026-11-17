#!/bin/sh
# Prints the line coverage of a JaCoCo CSV report (Lab 7 guideline: domain + application packages).
# Usage: sh coverage.sh target/site/jacoco/jacoco.csv
report="$1"
if [ ! -f "$report" ]; then
  echo "No JaCoCo report at $report: add the jacoco-maven-plugin to the module pom (Lab 7)."
  exit 0
fi
awk -F, 'NR > 1 {
  all_missed += $8; all_covered += $9
  if ($2 ~ /\.(domain|application)(\.|$)/) { core_missed += $8; core_covered += $9 }
}
END {
  if (all_missed + all_covered > 0) printf "Coverage all code: %.1f%%\n", 100 * all_covered / (all_missed + all_covered)
  if (core_missed + core_covered > 0) printf "Coverage domain+application: %.1f%%\n", 100 * core_covered / (core_missed + core_covered)
}' "$report"
