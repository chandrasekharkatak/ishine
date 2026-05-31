-- Allow VEHICLE_RATE policy rows (per-km rate for personal vehicle types) and seed self vehicle types.

SET NAMES utf8mb4;

START TRANSACTION;

ALTER TABLE reimbursement_role_policy
  DROP CHECK chk_rmb_role_policy_cat;

ALTER TABLE reimbursement_role_policy
  ADD CONSTRAINT chk_rmb_role_policy_cat CHECK (
    policy_category IN ('AMOUNT_LIMIT', 'TRAVEL_MODE', 'VEHICLE_TYPE', 'VEHICLE_RATE', 'FOOD_ALLOWANCE')
  );

INSERT INTO vehicle_type (vehicle_type_name, description, is_active, created_by, created_by_name)
SELECT 'Two Wheeler (Self)', 'Personal two-wheeler — official travel at per-km rate', 'Y', NULL, 'UAT seed'
WHERE NOT EXISTS (SELECT 1 FROM vehicle_type WHERE LOWER(TRIM(vehicle_type_name)) = 'two wheeler (self)');

INSERT INTO vehicle_type (vehicle_type_name, description, is_active, created_by, created_by_name)
SELECT 'Four Wheeler (Self)', 'Personal four-wheeler — official travel at per-km rate', 'Y', NULL, 'UAT seed'
WHERE NOT EXISTS (SELECT 1 FROM vehicle_type WHERE LOWER(TRIM(vehicle_type_name)) = 'four wheeler (self)');

COMMIT;
