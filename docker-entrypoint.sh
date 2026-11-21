#!/bin/sh
set -eu

# Supports VAR and VAR_FILE (Docker secrets) and exports the resolved value.
file_env() {
  var="$1"
  file_var="${var}_FILE"

  eval "val=\${$var:-}"
  eval "file_val=\${$file_var:-}"

  if [ -n "$val" ] && [ -n "$file_val" ]; then
    echo "error: both $var and $file_var are set" >&2
    exit 1
  fi

  if [ -n "$file_val" ]; then
    if [ ! -f "$file_val" ]; then
      echo "error: secret file '$file_val' not found for $file_var" >&2
      exit 1
    fi
    val="$(cat "$file_val")"
  fi

  if [ -n "$val" ]; then
    export "$var=$val"
  fi

  unset "$file_var"
}

file_env DB_PWD
file_env JWT_SECRET
file_env MAIL_PWD
file_env SYSTEM_PWD
file_env ZOHO_CLIENT_ID
file_env ZOHO_CLIENT_SECRET
file_env ZOHO_REFRESH_TOKEN
file_env OUTBOX_ENCRYPTION_KEY

exec java -jar /app/app.jar

