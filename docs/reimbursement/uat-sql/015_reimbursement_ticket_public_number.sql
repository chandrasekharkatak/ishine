-- Public ticket numbers APM-RMB-YYYYMMDD-#### (ReimbursementTicket.ticketNo + reimbursement_ticket_day_seq).
-- For databases that already ran 001 without ticket_no: run each block once; skip statements that error if already applied.

CREATE TABLE IF NOT EXISTS reimbursement_ticket_day_seq (
  day_key CHAR(8) NOT NULL,
  last_seq INT NOT NULL DEFAULT 0,
  PRIMARY KEY (day_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add column if missing (MySQL 5.7 / 8: run manually and ignore duplicate-column error if present)
ALTER TABLE reimbursement_ticket
  ADD COLUMN ticket_no VARCHAR(32) NULL COMMENT 'Public id e.g. APM-RMB-20260513-0001' AFTER ticket_id;

-- Backfill existing rows (requires MySQL 8+ for window functions)
UPDATE reimbursement_ticket t
JOIN (
  SELECT ticket_id,
         DATE_FORMAT(COALESCE(submitted_on, CURRENT_TIMESTAMP(3)), '%Y%m%d') AS dk,
         ROW_NUMBER() OVER (
           PARTITION BY DATE_FORMAT(COALESCE(submitted_on, CURRENT_TIMESTAMP(3)), '%Y%m%d')
           ORDER BY ticket_id) AS rn
  FROM reimbursement_ticket
) x ON t.ticket_id = x.ticket_id
SET t.ticket_no = CONCAT('APM-RMB-', x.dk, '-', LPAD(x.rn, 4, '0'))
WHERE t.ticket_no IS NULL OR t.ticket_no = '';

-- Seed day counters from existing public numbers
INSERT INTO reimbursement_ticket_day_seq (day_key, last_seq)
SELECT SUBSTRING(ticket_no, 9, 8) AS d,
       MAX(CAST(SUBSTRING_INDEX(ticket_no, '-', -1) AS UNSIGNED)) AS mx
FROM reimbursement_ticket
WHERE ticket_no REGEXP '^APM-RMB-[0-9]{8}-[0-9]{4}$'
GROUP BY SUBSTRING(ticket_no, 9, 8)
ON DUPLICATE KEY UPDATE last_seq = GREATEST(reimbursement_ticket_day_seq.last_seq, VALUES(last_seq));

-- Unique public id (fails until all rows have ticket_no — run backfill first)
CREATE UNIQUE INDEX uq_reimbursement_ticket_no ON reimbursement_ticket (ticket_no);
