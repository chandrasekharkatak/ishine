-- Reimbursement configuration: Food Allowance Type (UAT / emp_portal_db).
-- Clears food_type and reloads seed data.

SET NAMES utf8mb4;

START TRANSACTION;

DELETE FROM food_type;

INSERT INTO food_type (food_type_name, description, is_active, created_by, created_by_name) VALUES
('Veg', 'Dal khichdi', 'Y', NULL, 'UAT seed'),
('Non-Veg', 'Non-vegetarian meal allowance', 'Y', NULL, 'UAT seed'),
('Breakfast Allowance', 'Morning meal during official travel', 'Y', NULL, 'UAT seed'),
('Lunch Allowance', 'Afternoon meal on working days', 'Y', NULL, 'UAT seed'),
('Dinner Allowance', 'Evening meal during late work / travel', 'Y', NULL, 'UAT seed'),
('Full Day Meal Allowance', 'All meals included for outstation day', 'Y', NULL, 'UAT seed'),
('Half Day Meal Allowance', 'Meals for half-day official travel', 'Y', NULL, 'UAT seed'),
('Client Meal', 'Business meal hosted for client', 'Y', NULL, 'UAT seed'),
('Team Meal (OT)', 'Meal during overtime / extended hours', 'Y', NULL, 'UAT seed'),
('Snacks / Tea Allowance', 'Tea, coffee, snacks during official meetings', 'Y', NULL, 'UAT seed'),
('Jain Meal', 'Jain food for employees with dietary preference', 'Y', NULL, 'UAT seed'),
('Special Diet Allowance', 'Diabetic, gluten-free, or medically prescribed meal', 'Y', NULL, 'UAT seed');

COMMIT;

