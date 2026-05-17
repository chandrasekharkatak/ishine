-- UAT emp_portal_db: Reimbursement "Client" on the claim form comes from the project option row.
-- Team picker rows use clients.client_name via join; the primary-project merge path uses the
-- denormalized projects.client_name column (see ReimbursementTicketService.mergePrimaryMappedProjectIfAbsent).
-- If client_id is set but client_name on projects is NULL, the UI can show "—" for that project.
--
-- Project DEV-TNM-wcdbat-dev@test (project_id 9846): align denormalized name with clients master.

-- Preview:
-- SELECT p.project_id, p.project_name, p.client_id, p.client_name AS projects_client_name_denorm,
--        c.client_name AS clients_master_name
-- FROM projects p
-- LEFT JOIN clients c ON c.client_id = p.client_id
-- WHERE p.project_id = 9846;

UPDATE projects p
INNER JOIN clients c ON c.client_id = p.client_id
SET p.client_name = c.client_name
WHERE p.project_id = 9846;

-- Optional bulk repair (review counts first):
-- SELECT COUNT(*) FROM projects p
-- INNER JOIN clients c ON c.client_id = p.client_id
-- WHERE (p.client_name IS NULL OR TRIM(p.client_name) = '') AND p.client_id IS NOT NULL;
