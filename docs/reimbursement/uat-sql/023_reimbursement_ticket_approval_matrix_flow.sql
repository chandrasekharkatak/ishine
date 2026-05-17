-- Matrix-driven reimbursement ticket workflow (links ticket to configured approval matrix).
ALTER TABLE reimbursement_ticket
  ADD COLUMN approval_matrix_id BIGINT NULL AFTER workflow_stage,
  ADD COLUMN current_level_order INT NOT NULL DEFAULT 1 AFTER approval_matrix_id,
  ADD COLUMN current_assignee_emp_id BIGINT NULL AFTER current_level_order,
  ADD KEY idx_rmb_ticket_matrix (approval_matrix_id),
  ADD CONSTRAINT fk_rmb_ticket_matrix FOREIGN KEY (approval_matrix_id)
    REFERENCES reimbursement_approval_matrix (matrix_id) ON DELETE SET NULL;
