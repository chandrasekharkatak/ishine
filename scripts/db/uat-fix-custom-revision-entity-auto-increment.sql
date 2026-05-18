-- UAT ONLY — sync hibernate_sequence with custom_revision_entity (Envers).
USE db_emp_backup_new;

UPDATE hibernate_sequence
SET next_val = (SELECT IFNULL(MAX(id), 0) + 1 FROM custom_revision_entity);

SELECT next_val AS sequence_after, (SELECT MAX(id) FROM custom_revision_entity) AS max_revision_id
FROM hibernate_sequence;
