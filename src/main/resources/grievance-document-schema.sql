-- Proof / attachment documents for grievance tickets (multiple files per ticket).
CREATE TABLE IF NOT EXISTS grievance_document (
  document_id BIGINT NOT NULL AUTO_INCREMENT,
  ticket_id BIGINT NOT NULL,
  original_file_name VARCHAR(255) NOT NULL,
  stored_file_path VARCHAR(1000) NOT NULL,
  file_size_bytes BIGINT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_on DATETIME NULL,
  is_active INT NOT NULL DEFAULT 1,
  PRIMARY KEY (document_id),
  KEY idx_grievance_document_ticket (ticket_id),
  CONSTRAINT fk_grievance_document_ticket FOREIGN KEY (ticket_id) REFERENCES grievance_ticket (ticket_id)
);

-- Optional: backfill from legacy single proof columns on grievance_ticket
-- INSERT INTO grievance_document (ticket_id, original_file_name, stored_file_path, file_size_bytes, sort_order, created_on, is_active)
-- SELECT ticket_id, proof_file_name, proof_file_path, NULL, 0, created_on, 1
-- FROM grievance_ticket
-- WHERE proof_file_path IS NOT NULL AND proof_file_path <> ''
--   AND NOT EXISTS (SELECT 1 FROM grievance_document d WHERE d.ticket_id = grievance_ticket.ticket_id);
