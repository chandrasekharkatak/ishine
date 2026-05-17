-- Fix timesheet "No valid client details found" on UAT (emp_portal_db)
-- Root causes: projects.client_id NULL, clients missing names, client_locations empty

USE emp_portal_db;

-- 1) Ecoe12 / friendly* TNM test projects -> Eco Bank (client 273)
UPDATE projects
SET client_id = 273, client_name = 'Eco Bank', po_client_id = 862
WHERE project_name LIKE 'DEV - TNM - Ecoe12 - friendly%'
  AND (client_id IS NULL OR client_id = 0);

INSERT INTO clients (client_id, client_name, po_client_id, created_on)
SELECT 273, 'Eco Bank', 862, NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM clients WHERE client_id = 273);

UPDATE clients SET client_name = 'Eco Bank', po_client_id = 862
WHERE client_id = 273 AND (client_name IS NULL OR client_name = '');

-- 2) Client locations from backup for clients that have none
INSERT INTO client_locations (client_id, client_location, created_on, client_state, active_in_po, client_address_id)
SELECT b.client_id, b.client_location, b.created_on, b.client_state, b.active_in_po, b.client_address_id
FROM db_emp_backup_new.client_locations b
WHERE b.client_id IN (
  SELECT DISTINCT p.client_id FROM projects p
  WHERE p.client_id IS NOT NULL
    AND NOT EXISTS (SELECT 1 FROM client_locations cl WHERE cl.client_id = p.client_id)
)
AND NOT EXISTS (
  SELECT 1 FROM client_locations cl2
  WHERE cl2.client_id = b.client_id AND cl2.client_location = b.client_location
);
