#!/bin/sh

set -eu

usage() {
  cat <<'EOF'
Usage: scripts/list-ios-devices.sh

List iOS devices known to CoreDevice. Use this to find identifiers for devices
that are not recorded in docs/devices.md.

Options:
  -h, --help  Show this help
EOF
}

case ${1:-} in
  -h|--help)
    usage
    exit 0
    ;;
  '') ;;
  *)
    echo "Unknown option: $1" >&2
    usage >&2
    exit 2
    ;;
esac

exec xcrun devicectl list devices
