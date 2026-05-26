#!/usr/bin/env bash
# UAT ONLY — seed master data into db_emp_backup_new
set -euo pipefail

HOST="${MYSQL_HOST:-192.168.21.195}"
PORT="${MYSQL_PORT:-3306}"
USER="${MYSQL_USER:-dbuser_emp_portal}"
PASS="${MYSQL_PASS:-Apmosys@Emp123}"
DB="${MYSQL_DB:-db_emp_backup_new}"
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"

mysql_run() {
  echo "========== $(basename "$1") =========="
  { echo "USE \`${DB}\`;"; cat "$1"; } | mysql -h "$HOST" -P "$PORT" -u "$USER" -p"$PASS"
}

echo "Master data seed → ${DB} @ ${HOST}"

# Reimbursement configuration masters (full reload)
mysql_run "${ROOT}/docs/reimbursement/uat-sql/012_seed_expenditure_travel_vehicle_masters.sql"
mysql_run "${ROOT}/docs/reimbursement/uat-sql/016_seed_food_allowance_type.sql"

# Grievance scenarios (full reload for timesheet + leave lists)
mysql_run "${ROOT}/src/main/resources/grievance-issue-scenario-seed.sql"

# Skill matrix + copy from emp_portal_db
mysql_run "${ROOT}/scripts/db/uat-db_emp_backup_new-master-data-seed.sql"

echo "Done."
