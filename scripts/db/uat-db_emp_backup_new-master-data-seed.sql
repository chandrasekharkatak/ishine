-- =============================================================================
-- UAT ONLY — db_emp_backup_new (skill matrix masters from emp_portal_db)
-- Run via: scripts/db/uat-db_emp_backup_new-master-data-seed.sh
-- (that shell also runs reimbursement 012/016 and grievance issue scenarios)
-- =============================================================================

USE db_emp_backup_new;

INSERT IGNORE INTO skillmatrix_aspiration_chip_master (dept_id, chip_label, sort_order, is_active) VALUES
  (0, 'Solution architect', 10, 1),
  (0, 'Tech lead', 20, 1),
  (0, 'Full stack specialist', 30, 1),
  (0, 'Cloud-native engineer', 40, 1),
  (0, 'Engineering manager', 50, 1);

SET @src_db := 'emp_portal_db';
SET @tgt_db := 'db_emp_backup_new';

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM subskills_master;
DELETE FROM skills_master;
DELETE FROM skill_category_master;

SET @sql = CONCAT('INSERT INTO ', @tgt_db, '.skill_category_master SELECT * FROM ', @src_db, '.skill_category_master');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = CONCAT('INSERT INTO ', @tgt_db, '.skills_master SELECT * FROM ', @src_db, '.skills_master');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = CONCAT('INSERT INTO ', @tgt_db, '.subskills_master SELECT * FROM ', @src_db, '.subskills_master');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

DELETE FROM skill_domain_feature_master;
DELETE FROM skill_subdomain_master;
DELETE FROM skill_domain_master;

SET @sql = CONCAT('INSERT INTO ', @tgt_db, '.skill_domain_master SELECT * FROM ', @src_db, '.skill_domain_master');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = CONCAT('INSERT INTO ', @tgt_db, '.skill_subdomain_master SELECT * FROM ', @src_db, '.skill_subdomain_master');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = CONCAT('INSERT INTO ', @tgt_db, '.skill_domain_feature_master SELECT * FROM ', @src_db, '.skill_domain_feature_master');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = CONCAT(
  'INSERT IGNORE INTO ', @tgt_db, '.skillmatrix_aspiration_chip_master ',
  '(chip_id, dept_id, chip_label, sort_order, is_active, created_at, updated_at) ',
  'SELECT chip_id, dept_id, chip_label, sort_order, is_active, created_at, updated_at FROM ', @src_db, '.skillmatrix_aspiration_chip_master'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET FOREIGN_KEY_CHECKS = 1;

SELECT 'expenditure_type' AS master_table, COUNT(*) AS row_count FROM expenditure_type
UNION ALL SELECT 'reimbursement_travel_mode', COUNT(*) FROM reimbursement_travel_mode
UNION ALL SELECT 'vehicle_type', COUNT(*) FROM vehicle_type
UNION ALL SELECT 'food_type', COUNT(*) FROM food_type
UNION ALL SELECT 'grievance_issue_scenario', COUNT(*) FROM grievance_issue_scenario
UNION ALL SELECT 'skills_master', COUNT(*) FROM skills_master
UNION ALL SELECT 'subskills_master', COUNT(*) FROM subskills_master
UNION ALL SELECT 'skill_category_master', COUNT(*) FROM skill_category_master
UNION ALL SELECT 'skillmatrix_aspiration_chip_master', COUNT(*) FROM skillmatrix_aspiration_chip_master
UNION ALL SELECT 'skill_domain_master', COUNT(*) FROM skill_domain_master;
