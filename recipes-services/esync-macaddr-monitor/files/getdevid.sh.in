#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

# Validate and read machine-id
get_machine_id() {
  local idfile=""
  if [[ -r /etc/machine-id ]]; then
    idfile="/etc/machine-id"
  elif [[ -r /var/lib/dbus/machine-id ]]; then
    idfile="/var/lib/dbus/machine-id"
  else
    echo "Error: machine-id file not found." >&2
    return 1
  fi

  local rawid
  rawid="$(grep -m1 -E '.+' "$idfile" 2>/dev/null || true)"
  if [[ -z "$rawid" ]]; then
    echo "Error: machine-id is empty." >&2
    return 1
  fi

  if [[ "$rawid" =~ ^[a-fA-F0-9]{32}$ || "$rawid" =~ ^[a-fA-F0-9]{8}-([a-fA-F0-9]{4}-){3}[a-fA-F0-9]{12}$ ]]; then
    printf '%s' "$rawid"
  else
    echo "Error: Invalid machine-id format: $rawid" >&2
    return 1
  fi
}

# Hash and extract HWID
generate_hwid() {
  local rawid
  rawid="$(get_machine_id)" || return 1

  if command -v sha256sum &>/dev/null; then
    echo -n "$rawid" | sha256sum | awk '{print substr($1,1,12)}'
  elif command -v md5sum &>/dev/null; then
    echo -n "$rawid" | md5sum | awk '{print substr($1,1,12)}'
  elif command -v openssl &>/dev/null; then
    echo -n "$rawid" | openssl dgst -sha256 | awk '{print substr($NF,1,12)}'
  else
    echo "Error: No hashing tool available (sha256sum/md5sum/openssl)." >&2
    return 1
  fi
}

# Main execution
generate_hwid
