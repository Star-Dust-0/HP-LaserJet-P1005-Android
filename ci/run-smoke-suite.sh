#!/usr/bin/env bash
set -euo pipefail
if [ "${3:-full}" = printflow ]; then
  adb install -r "$1"
  adb push ci/flow.pdf ci/flow4.pdf ci/flow8.pdf /data/local/tmp/
  python3 ci/print-flow-smoke.py
else
  bash ci/ui-smoke.sh "$1" "$2" "${3:-full}"
fi
