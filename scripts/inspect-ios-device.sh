#!/bin/sh

set -eu

script_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
project_root=$(CDPATH= cd -- "$script_dir/.." && pwd)
devices_file="$project_root/docs/devices.md"
device_id=

usage() {
  cat <<'EOF'
Usage: scripts/inspect-ios-device.sh [options]

List installed apps and running processes on a physical iOS device. By default,
the script uses the Primary device in docs/devices.md.

Options:
  --device ID  Override the device identifier
  -h, --help   Show this help
EOF
}

while [ "$#" -gt 0 ]; do
  case "$1" in
    --device)
      [ "$#" -ge 2 ] || { echo "Missing value for --device" >&2; exit 2; }
      device_id=$2
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

xcrun devicectl device info apps --device "$device_id"
xcrun devicectl device info processes --device "$device_id"
