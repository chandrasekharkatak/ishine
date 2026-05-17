-- Add HOD per-skill decision + comment.
-- decision      = FINAL (pending|approved|rejected)
-- manager_decision = Manager per-skill decision (pending|approved|adjusted|sent_back|rejected)
-- hod_decision  = HOD per-skill decision (pending|approved|rejected)

ALTER TABLE skillmatrix_assessment_approval_skill
  ADD COLUMN hod_decision VARCHAR(20) NOT NULL DEFAULT 'pending' AFTER manager_decision,
  ADD COLUMN hod_comment  VARCHAR(500) NULL AFTER hod_decision;

-- Backfill existing rows: if final already rejected, HOD decision should also be rejected.
UPDATE skillmatrix_assessment_approval_skill
SET hod_decision = CASE WHEN decision='rejected' THEN 'rejected' ELSE 'pending' END
WHERE hod_decision = 'pending';

