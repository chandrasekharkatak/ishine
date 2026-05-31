-- Per travel mode daily cap (item_name = mode type from reimbursement_travel_mode).

SET NAMES utf8mb4;

START TRANSACTION;

ALTER TABLE reimbursement_role_policy
  DROP CHECK chk_rmb_role_policy_cat;

ALTER TABLE reimbursement_role_policy
  ADD CONSTRAINT chk_rmb_role_policy_cat CHECK (
    policy_category IN (
      'AMOUNT_LIMIT',
      'TRAVEL_MODE',
      'TRAVEL_MODE_LIMIT',
      'VEHICLE_TYPE',
      'VEHICLE_RATE',
      'FOOD_ALLOWANCE'
    )
  );

COMMIT;
