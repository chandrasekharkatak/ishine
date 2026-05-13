-- Add manager_decision column so approval_skill.decision can represent FINAL decision.
-- Manager per-skill decision will be stored in manager_decision, while decision stays pending until final approval.

ALTER TABLE skillmatrix_assessment_approval_skill
  ADD COLUMN manager_decision VARCHAR(20) NOT NULL DEFAULT 'pending' AFTER employee_rating;

-- Backfill existing rows: move current decision into manager_decision and reset final decision to pending
-- unless it is already rejected (rejected stays rejected).
UPDATE skillmatrix_assessment_approval_skill
SET manager_decision = COALESCE(NULLIF(decision,''),'pending'),
    decision = CASE WHEN decision='rejected' THEN 'rejected' ELSE 'pending' END
WHERE manager_decision = 'pending';

