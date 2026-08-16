#!/bin/sh

set -eu

script_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
project_root=$(CDPATH= cd -- "$script_dir/.." && pwd)
devices_file="$project_root/docs/devices.md"
configuration=Debug
derived_data_path=/tmp/math-flip-derived-data
device_id=
app_path=

usage() {
  cat <<'EOF'
Usage: scripts/deploy-ios-device.sh [options]

Install a built Math Flip app on a physical iOS device. By default, the script
uses the Primary device in docs/devices.md.

Options:
  --device ID           Override the device identifier
  --app PATH            Override the path to MathFlip.app
  --configuration NAME  Xcode build configuration (default: Debug)
  --derived-data PATH   Derived-data directory (default: /tmp/math-flip-derived-data)
  -h, --help            Show this help
EOF
}

while [ "$#" -gt 0 ]; do
  case "$1" in
    --device)
      [ "$#" -ge 2 ] || { echo "Missing value for --device" >&2; exit 2; }
      device_id=$2
      shift 2
      ;;
    --app)
      [ "$#" -ge 2 ] || { echo "Missing value for --app" >&2; exit 2; }
      app_path=$2
      shift 2
      ;;
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

if [ -z "$app_path" ]; then
  app_path="$derived_data_path/Build/Products/$configuration-iphoneos/MathFlip.app"
fi

if [ -z "$device_id" ] && [ -f "$devices_file" ]; then
  device_id=$(awk -F '|' '
    $2 ~ /^[[:space:]]*Primary[[:space:]]*$/ {
      value = $4
      gsub(/^[[:space:]]+|[[:space:]]+$/, "", value)
      print value
      exit
    }
  ' "$devices_file")
fi

if [ -z "$device_id" ]; then
  echo "No iOS device ID found." >&2
  echo "Copy docs/devices.example.md to docs/devices.md and fill in its Primary row," >&2
  echo "or pass --device ID." >&2
  exit 1
fi

if [ ! -d "$app_path" ]; then
  echo "Built app not found at: $app_path" >&2
  echo "Run scripts/build-ios-device.sh first." >&2
  exit 1
fi

exec xcrun devicectl device install app \
  --device "$device_id" \
  "$app_path"
