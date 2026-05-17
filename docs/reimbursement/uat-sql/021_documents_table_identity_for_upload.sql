-- UAT emp_portal_db: /api/uploadFileReimbursement saves rows into `documents` via entity
-- com.apmosys.employeeportal.model.Newsletter (@Table(name = "documents")) with
-- @GeneratedValue(strategy = GenerationType.IDENTITY) on documentId.
--
-- If document_id is NOT AUTO_INCREMENT (and has no PRIMARY KEY), MySQL does not generate
-- an identity on INSERT and Hibernate throws:
--   "The database returned no natively generated identity value"
--
-- Fix: primary key + AUTO_INCREMENT on document_id. InnoDB will set the next value from
-- MAX(document_id)+1 after ALTER.

-- Preview (optional):
-- SHOW CREATE TABLE documents;

ALTER TABLE documents
  MODIFY COLUMN document_id BIGINT NOT NULL AUTO_INCREMENT,
  ADD PRIMARY KEY (document_id);
