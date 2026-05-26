#!/usr/bin/env bash
# UAT ONLY — db_emp_backup_new @ 192.168.21.195
# Creates Grievance + Reimbursement (ticket flow) + Skill Matrix tables, then feature/subfeature seed.
# DO NOT run on production.

set -euo pipefail

HOST="${MYSQL_HOST:-192.168.21.195}"
PORT="${MYSQL_PORT:-3306}"
USER="${MYSQL_USER:-dbuser_emp_portal}"
PASS="${MYSQL_PASS:-Apmosys@Emp123}"
DB="${MYSQL_DB:-db_emp_backup_new}"

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"

mysql_run() {
  local file="$1"
  local optional="${2:-0}"
  echo ""
  echo "========== $(basename "$file") =========="
  if [[ "$optional" == "1" ]]; then
    { echo "USE \`${DB}\`;"; cat "$file"; } | mysql -h "$HOST" -P "$PORT" -u "$USER" -p"$PASS" || {
      echo "WARN: $(basename "$file") — skipped or already applied."
    }
  else
    { echo "USE \`${DB}\`;"; cat "$file"; } | mysql -h "$HOST" -P "$PORT" -u "$USER" -p"$PASS"
  fi
}

echo "Bootstrap target: ${DB} @ ${HOST}:${PORT}"

# --- Grievance ---
mysql_run "${ROOT}/scripts/db/uat-grievance-ticket-schema.sql"
mysql_run "${ROOT}/src/main/resources/grievance-document-schema.sql"
mysql_run "${ROOT}/src/main/resources/grievance-audit-schema.sql"
mysql_run "${ROOT}/src/main/resources/grievance-issue-scenario-seed.sql"

# --- Reimbursement (new ticket workflow) ---
mysql_run "${ROOT}/docs/reimbursement/uat-sql/001_create_reimbursement_ticket_tables.sql"
mysql_run "${ROOT}/docs/reimbursement/uat-sql/022_create_reimbursement_approval_matrix.sql"
mysql_run "${ROOT}/docs/reimbursement/uat-sql/024_reimbursement_submission_settings.sql"
mysql_run "${ROOT}/docs/reimbursement/uat-sql/011_add_client_to_reimbursement_claim.sql" 1
mysql_run "${ROOT}/docs/reimbursement/uat-sql/023_reimbursement_ticket_approval_matrix_flow.sql" 1
mysql_run "${ROOT}/docs/reimbursement/uat-sql/015_reimbursement_ticket_public_number.sql" 1

# --- Skill Matrix ---
mysql_run "${ROOT}/scripts/db/uat-skill-matrix-prerequisites.sql" 1
mysql_run "${ROOT}/src/main/resources/db/skill_matrix_domain_stack_all_tables.sql"
mysql_run "${ROOT}/docs/skillmatrix/uat-sql/001_create_skillmatrix_tables.sql"
mysql_run "${ROOT}/docs/skillmatrix/uat-sql/002_alter_assessment_project_skill_domain.sql" 1
mysql_run "${ROOT}/docs/skillmatrix/uat-sql/004_alter_approval_skill_manager_decision.sql" 1
mysql_run "${ROOT}/docs/skillmatrix/uat-sql/005_alter_approval_skill_hod_decision.sql" 1
mysql_run "${ROOT}/docs/skillmatrix/uat-sql/007_drop_uq_sm_submission_employee_cycle.sql" 1

# --- RBAC: tabs / features / sub-features ---
mysql_run "${ROOT}/scripts/db/uat-db_emp_backup_new-feature-subfeature-seed.sql"

# --- Envers sequence (UAT pattern: next_val > max revision id) ---
mysql_run "${ROOT}/scripts/db/uat-fix-custom-revision-entity-auto-increment.sql"

# --- Master / configuration data ---
echo ""
echo "========== Master data (reimbursement, grievance scenarios, skill matrix) =========="
"${ROOT}/scripts/db/uat-db_emp_backup_new-master-data-seed.sh"

echo ""
echo "========== Verify tables =========="
mysql -h "$HOST" -P "$PORT" -u "$USER" -p"$PASS" "$DB" -e "
SELECT table_name FROM information_schema.tables
WHERE table_schema = '${DB}'
  AND (table_name LIKE 'grievance%' OR table_name LIKE 'reimbursement_ticket%' OR table_name LIKE 'skillmatrix%')
ORDER BY 1;
"

echo ""
echo "Done."
