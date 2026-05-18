-- Curated grievance issue scenarios for Timesheet and Leave modules only.
-- tab_key: use 'timesheet' or 'leave' (matched after normalizing category tab name in app).
-- NULL / empty feature_name and sub_feature_name = applies to any feature/sub-feature under that module.
-- Safe to re-run: replaces rows for timesheet/leave.

CREATE TABLE IF NOT EXISTS grievance_issue_scenario (
  scenario_id BIGINT NOT NULL AUTO_INCREMENT,
  tab_key VARCHAR(50) NOT NULL,
  feature_name VARCHAR(200) NULL,
  sub_feature_name VARCHAR(200) NULL,
  scenario_label VARCHAR(500) NOT NULL,
  sort_order INT DEFAULT 0,
  is_active TINYINT(1) DEFAULT 1,
  PRIMARY KEY (scenario_id),
  KEY idx_grievance_scenario_tab (tab_key, is_active)
);

DELETE FROM grievance_issue_scenario WHERE tab_key IN ('timesheet', 'leave');

-- ========== TIMESHEET (entire module) ==========
INSERT INTO grievance_issue_scenario (tab_key, feature_name, sub_feature_name, scenario_label, sort_order) VALUES
('timesheet', NULL, NULL, 'Cannot save or submit timesheet', 10),
('timesheet', NULL, NULL, 'Hours or entries not saving correctly', 20),
('timesheet', NULL, NULL, 'Wrong project / task / activity shown or selectable', 30),
('timesheet', NULL, NULL, 'Calendar or period view not loading / blank', 40),
('timesheet', NULL, NULL, 'My timesheet page error or timeout', 50),
('timesheet', NULL, NULL, 'Team timesheet not visible or wrong data', 60),
('timesheet', NULL, NULL, 'Approval / submit workflow stuck or failing', 70),
('timesheet', NULL, NULL, 'Biomax or external sync / integration issue', 80),
('timesheet', NULL, NULL, 'Lock period / backdated entry blocked incorrectly', 90),
('timesheet', NULL, NULL, 'Duplicate entry or validation error', 100),
('timesheet', NULL, NULL, 'Copy from previous week / bulk fill not working', 110),
('timesheet', NULL, NULL, 'HR dashboard or reports wrong / not updating', 120),
('timesheet', NULL, NULL, 'Export / download incorrect or missing', 130),
('timesheet', NULL, NULL, 'Notifications (submit / approve / reject) not received', 140),
('timesheet', NULL, NULL, 'Other timesheet issue (describe in description)', 999);

-- ========== LEAVE (entire module) ==========
INSERT INTO grievance_issue_scenario (tab_key, feature_name, sub_feature_name, scenario_label, sort_order) VALUES
('leave', NULL, NULL, 'Leave balance incorrect or not updating', 10),
('leave', NULL, NULL, 'Cannot apply for leave', 20),
('leave', NULL, NULL, 'Leave application stuck in pending / not moving', 30),
('leave', NULL, NULL, 'Leave approval / rejection not working', 40),
('leave', NULL, NULL, 'Comp-off credit / application issue', 50),
('leave', NULL, NULL, 'Holiday list wrong, missing, or not loading', 60),
('leave', NULL, NULL, 'Wrong leave type or policy shown', 70),
('leave', NULL, NULL, 'Sandwich rule / weekend / policy calculation wrong', 80),
('leave', NULL, NULL, 'Leave cancellation failed or not allowed', 90),
('leave', NULL, NULL, 'Half-day or custom duration not working', 100),
('leave', NULL, NULL, 'Encashment / LOP / carry-forward related issue', 110),
('leave', NULL, NULL, 'Manager or HR view of team leave incorrect', 120),
('leave', NULL, NULL, 'Email or portal notification missing for leave', 130),
('leave', NULL, NULL, 'Attachment or document upload on leave request failed', 140),
('leave', NULL, NULL, 'Other leave issue (describe in description)', 999);
