-- Allow multiple submissions across cycles/years
-- Existing DDL uses uq_sm_submission_employee_cycle UNIQUE (employee_id, assessment_cycle),
-- which blocks new drafts after a submission is approved/submitted.

-- Drop the old unique constraint if present.
ALTER TABLE skillmatrix_assessment_submission
  DROP INDEX uq_sm_submission_employee_cycle;

