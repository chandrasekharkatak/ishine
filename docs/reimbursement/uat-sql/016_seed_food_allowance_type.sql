-- Food allowance master (food_type). Use when Reimbursement config → Food Allowance Type is empty.
-- Does not modify expenditure_type or reimbursement_travel_mode.
-- Safe to re-run: skips rows that already exist with the same food_type_name.

SET NAMES utf8mb4;

INSERT INTO food_type (food_type_name, description, is_active, created_by, created_by_name)
SELECT v.food_type_name, v.description, 'Y', NULL, 'UAT seed'
FROM (
  SELECT 'Standard meal (official)' AS food_type_name, 'Meal during official work or client visit' AS description
  UNION ALL SELECT 'Overtime / late-night meal', 'Meal claim for extended work hours'
  UNION ALL SELECT 'Breakfast allowance', 'Morning shift or early travel breakfast'
  UNION ALL SELECT 'Lunch allowance', 'Midday meal allowance'
  UNION ALL SELECT 'Dinner allowance', 'Evening meal after official travel or late work'
  UNION ALL SELECT 'Team meal / working lunch', 'Team collaboration meal during project work'
  UNION ALL SELECT 'Client entertainment (meal)', 'Business meal with client as per policy'
  UNION ALL SELECT 'WFH meal support', 'Occasional meal allowance while working from home (if policy allows)'
) AS v
WHERE NOT EXISTS (
  SELECT 1 FROM food_type f WHERE f.food_type_name = v.food_type_name
);
