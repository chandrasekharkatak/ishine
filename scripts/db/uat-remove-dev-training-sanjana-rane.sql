-- UAT db_emp_backup_new: unblock Sanjana Rane from mandatory "Dev training" (training_id=12)
-- by marking cycle 1 as completed (training applies to all employees; per-user completion via consent).

-- Employee: Sanjana Rane (emp_id=1570, employeement_id=23145)
-- Training: Dev training (training_id=12, content_id=19, quiz_id=48)

INSERT INTO training_consent (
  training_id,
  content_id,
  emp_id,
  quiz_id,
  completion_cycle_number,
  created_by
)
SELECT 12, 19, 1570, 48, 1, 1570
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM training_consent
  WHERE emp_id = 1570 AND training_id = 12 AND content_id = 19 AND quiz_id = 48 AND completion_cycle_number = 1
);
