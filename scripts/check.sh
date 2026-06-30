#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

./gradlew clean ktlintCheck test integrationTest build
./scripts/secret-scan.sh
