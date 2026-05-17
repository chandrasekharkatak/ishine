#!/usr/bin/env bash
# Apply leave policy seed to UAT emp_portal_db
set -euo pipefail

HOST="${MYSQL_HOST:-192.168.21.195}"
PORT="${MYSQL_PORT:-3306}"
USER="${MYSQL_USER:-dbuser_emp_portal}"
PASS="${MYSQL_PASSWORD:-Apmosys@Emp123}"
DB="${MYSQL_DATABASE:-emp_portal_db}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

mysql -h "$HOST" -P "$PORT" -u "$USER" -p"$PASS" "$DB" < "$SCRIPT_DIR/uat-seed-leave-policies.sql"
echo "Done. Leave policies seeded on $DB @ $HOST:$PORT"
