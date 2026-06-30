#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

if ! command -v rg >/dev/null 2>&1; then
  echo "ripgrep is required for secret scanning." >&2
  exit 1
fi

secret_pattern='(sk-[A-Za-z0-9_-]{20,}|AKIA[0-9A-Z]{16}|AIza[0-9A-Za-z_-]{35}|xox[baprs]-[A-Za-z0-9-]{10,}|-----BEGIN (RSA |EC |OPENSSH |)PRIVATE KEY-----)'

if rg -n --hidden \
  --glob '!build/**' \
  --glob '!.gradle/**' \
  --glob '!.git/**' \
  --glob '!gradle/wrapper/gradle-wrapper.jar' \
  --glob '!*.jar' \
  --glob '!*.class' \
  "$secret_pattern" .; then
  echo "Potential secret detected. Review the matches above." >&2
  exit 1
fi

echo "No obvious secrets detected."
