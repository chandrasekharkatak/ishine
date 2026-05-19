-- Reimbursement configuration: Expenditure Type, Travel Mode, Vehicle Type (UAT / emp_portal_db).
-- Run after backup. Clears the three master tables and reloads seed data.
-- Travel modes are linked to expenditure_type_name = 'Travel' (required by saveReimbursementTravelMode API).
-- Table names verified: expenditure_type, reimbursement_travel_mode, vehicle_type.

SET NAMES utf8mb4;

START TRANSACTION;

DELETE FROM reimbursement_travel_mode;
DELETE FROM vehicle_type;
DELETE FROM expenditure_type;

-- ========== Tab 1: Expenditure Type (28 rows) ==========
INSERT INTO expenditure_type (expenditure_type_name, description, is_active, created_by, created_by_name) VALUES
('CAB', 'Need expenditure for travel', 'Y', NULL, 'UAT seed'),
('Client Location Visit', 'Travel for business purpose', 'Y', NULL, 'UAT seed'),
('Food', 'Food reimbursement', 'Y', NULL, 'UAT seed'),
('Travel', 'Travel expenses', 'Y', NULL, 'UAT seed'),
('Hotel / Accommodation', 'Stay charges during official trip', 'Y', NULL, 'UAT seed'),
('Flight', 'Airfare for domestic/international travel', 'Y', NULL, 'UAT seed'),
('Train', 'Rail travel for official purpose', 'Y', NULL, 'UAT seed'),
('Bus / Volvo', 'Intercity bus travel charges', 'Y', NULL, 'UAT seed'),
('Fuel Reimbursement', 'Petrol/diesel for personal vehicle used officially', 'Y', NULL, 'UAT seed'),
('Toll & Parking', 'Toll charges and parking fees', 'Y', NULL, 'UAT seed'),
('Daily Allowance (DA)', 'Per diem allowance for outstation travel', 'Y', NULL, 'UAT seed'),
('Mobile Bill', 'Official mobile usage reimbursement', 'Y', NULL, 'UAT seed'),
('Internet / Broadband', 'WFH internet bill reimbursement', 'Y', NULL, 'UAT seed'),
('AI Subscription', 'ChatGPT Plus, Claude Pro, Copilot, Gemini', 'Y', NULL, 'UAT seed'),
('Software Subscription', 'JIRA, Postman, Figma, Notion, Slack', 'Y', NULL, 'UAT seed'),
('Cloud Infrastructure', 'AWS, Azure, GCP usage charges', 'Y', NULL, 'UAT seed'),
('API Usage Charges', 'OpenAI API, Google Maps API, Twilio', 'Y', NULL, 'UAT seed'),
('Hardware / Equipment', 'Keyboard, mouse, headset, webcam, hub', 'Y', NULL, 'UAT seed'),
('Laptop Accessory', 'Laptop stand, cooling pad, charger', 'Y', NULL, 'UAT seed'),
('Office Supplies', 'Stationery, notebooks, printing', 'Y', NULL, 'UAT seed'),
('Training & Certification', 'AWS/Azure/PMP exam fees, Udemy, Coursera', 'Y', NULL, 'UAT seed'),
('Conference / Seminar', 'Registration fees for tech events', 'Y', NULL, 'UAT seed'),
('Client Entertainment', 'Business meals with clients', 'Y', NULL, 'UAT seed'),
('Team Meal (Overtime)', 'Meals during extended work or late night', 'Y', NULL, 'UAT seed'),
('Visa & Travel Documents', 'Visa fees, passport renewal for international travel', 'Y', NULL, 'UAT seed'),
('Courier / Shipping', 'Document or equipment courier charges', 'Y', NULL, 'UAT seed'),
('Co-working Space', 'Remote work space charges', 'Y', NULL, 'UAT seed'),
('Miscellaneous', 'Other approved project-related expenses', 'Y', NULL, 'UAT seed');

-- ========== Tab 2: Travel Mode (10 rows, FK -> Travel expenditure) ==========
SET @travel_et_id := (SELECT id FROM expenditure_type WHERE expenditure_type_name = 'Travel' LIMIT 1);

INSERT INTO reimbursement_travel_mode (expenditure_type_id, mode_type, description, is_active, requires_vehicle_type, created_by, created_by_name) VALUES
(@travel_et_id, 'Own Vehicle', 'Personal car/bike used for official travel', 'Y', 'Yes', NULL, 'UAT seed'),
(@travel_et_id, 'Cab / Taxi', 'Ola, Uber, local taxi booking', 'Y', 'Yes', NULL, 'UAT seed'),
(@travel_et_id, 'Auto Rickshaw', 'Local auto for short-distance travel', 'Y', 'No', NULL, 'UAT seed'),
(@travel_et_id, 'Company Vehicle', 'Official car/van provided by company', 'Y', 'Yes', NULL, 'UAT seed'),
(@travel_et_id, 'Flight', 'Domestic or international air travel', 'Y', 'No', NULL, 'UAT seed'),
(@travel_et_id, 'Train', 'Railway travel (sleeper/AC)', 'Y', 'No', NULL, 'UAT seed'),
(@travel_et_id, 'Bus', 'State transport or private bus', 'Y', 'No', NULL, 'UAT seed'),
(@travel_et_id, 'Metro / Local Train', 'City metro or suburban rail', 'Y', 'No', NULL, 'UAT seed'),
(@travel_et_id, 'Rental Car', 'Hired vehicle for outstation trip', 'Y', 'Yes', NULL, 'UAT seed'),
(@travel_et_id, 'Bike / Two-Wheeler', 'Two-wheeler for local conveyance', 'Y', 'Yes', NULL, 'UAT seed');

-- ========== Tab 3: Vehicle Type (10 rows) ==========
INSERT INTO vehicle_type (vehicle_type_name, description, is_active, created_by, created_by_name) VALUES
('Four Wheeler', 'Car for journey', 'Y', NULL, 'UAT seed'),
('Two Wheeler', 'Bike/scooter for local travel', 'Y', NULL, 'UAT seed'),
('Auto Rickshaw', 'Three-wheeler for short trips', 'Y', NULL, 'UAT seed'),
('Mini Cab (Hatchback)', 'Small cab — Ola Mini, Uber Go', 'Y', NULL, 'UAT seed'),
('Sedan', 'Standard cab — Ola Prime, Uber Premier', 'Y', NULL, 'UAT seed'),
('SUV / MUV', 'Large vehicle — Innova, Ertiga for group travel', 'Y', NULL, 'UAT seed'),
('Tempo Traveller', 'Group travel van for team outings', 'Y', NULL, 'UAT seed'),
('Company Bus', 'Office shuttle or hired bus', 'Y', NULL, 'UAT seed'),
('Electric Vehicle (EV)', 'EV cab or personal EV for official use', 'Y', NULL, 'UAT seed'),
('Bicycle', 'Eco-friendly short-distance commute', 'Y', NULL, 'UAT seed');

COMMIT;
