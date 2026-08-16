#!/bin/sh

set -eu

script_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
project_root=$(CDPATH= cd -- "$script_dir/.." && pwd)
configuration=Debug
derived_data_path=/tmp/math-flip-derived-data

usage() {
  cat <<'EOF'
Usage: scripts/build-ios-device.sh [options]

Build Math Flip for a physical iOS device.

Options:
  --configuration NAME  Xcode build configuration (default: Debug)
  --derived-data PATH   Derived-data directory (default: /tmp/math-flip-derived-data)
  -h, --help            Show this help
EOF
}

while [ "$#" -gt 0 ]; do
  case "$1" in
    --configuration)
      [ "$#" -ge 2 ] || { echo "Missing value for --configuration" >&2; exit 2; }
      configuration=$2
      shift 2
      ;;
    --derived-data)
      [ "$#" -ge 2 ] || { echo "Missing value for --derived-data" >&2; exit 2; }
      derived_data_path=$2
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      usage >&2
      exit 2
      ;;
  esac
done

cd "$project_root"

exec xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme MathFlip \
  -sdk iphoneos \
  -configuration "$configuration" \
  -derivedDataPath "$derived_data_path" \
  build
