#!/bin/sh

set -eu

script_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
project_root=$(CDPATH= cd -- "$script_dir/.." && pwd)

usage() {
  cat <<'EOF'
Usage: scripts/test-shared.sh [Gradle options]

Run all tests for the shared Kotlin Multiplatform module. Any arguments are
forwarded to Gradle after the test task.

Options:
  -h, --help  Show this help
EOF
}

case ${1:-} in
  -h|--help)
    usage
    exit 0
    ;;
esac

cd "$project_root"
exec ./gradlew :shared:allTests "$@"
