-- Enterprise grievance audit trail (immutable logs; use is_deleted for soft-archive only).
-- Run after grievance_ticket exists.

-- Version numbers live only in grievance_audit_log.version_no (no column required on grievance_ticket).

CREATE TABLE IF NOT EXISTS grievance_audit_log (
  audit_id BIGINT NOT NULL AUTO_INCREMENT,
  ticket_id BIGINT NOT NULL,
  event_type VARCHAR(50) NOT NULL,
  summary VARCHAR(2000),
  version_no INT NOT NULL,
  changed_by BIGINT NULL,
  changed_by_name VARCHAR(200),
  changed_at DATETIME(3) NOT NULL,
  old_snapshot LONGTEXT NULL,
  new_snapshot LONGTEXT NOT NULL,
  reason VARCHAR(1000) NULL,
  is_deleted TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (audit_id),
  KEY idx_griev_audit_ticket_time (ticket_id, changed_at),
  KEY idx_griev_audit_ticket_version (ticket_id, version_no),
  CONSTRAINT fk_griev_audit_ticket FOREIGN KEY (ticket_id) REFERENCES grievance_ticket (ticket_id)
);

CREATE TABLE IF NOT EXISTS grievance_audit_log_detail (
  detail_id BIGINT NOT NULL AUTO_INCREMENT,
  audit_id BIGINT NOT NULL,
  field_name VARCHAR(128) NOT NULL,
  old_value LONGTEXT NULL,
  new_value LONGTEXT NULL,
  PRIMARY KEY (detail_id),
  KEY idx_griev_audit_detail_audit (audit_id),
  KEY idx_griev_audit_detail_field (field_name),
  CONSTRAINT fk_griev_audit_detail_log FOREIGN KEY (audit_id) REFERENCES grievance_audit_log (audit_id)
);

-- Partitioning note: for very large deployments, consider RANGE partitioning grievance_audit_log by YEAR(changed_at)
-- or archiving rows older than N years to a cold store (application job sets is_deleted=1 after export).
