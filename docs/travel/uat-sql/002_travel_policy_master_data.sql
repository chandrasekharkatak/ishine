-- =============================================================================
-- UAT: ApMoSys Travel Policy v0.1 (25-Jan-2023) — Travel Desk master data
-- Database: db_emp_backup_new @ 192.168.21.195
-- Idempotent: safe to re-run (INSERT ... WHERE NOT EXISTS)
--
-- Policy mapping:
--   • Travel reasons  → business purpose (Annexure I #1–3, guidelines §2)
--   • Hotel categories → per-night limits (Annexure I #2, Annexure II city tiers)
--   • Travel modes    → Flight, Train, Bus, Cab (Annexure I #1, #5)
--   • Travel classes  → Economy default; cab = Uber Go / Ola Mini per policy
-- =============================================================================

USE db_emp_backup_new;

SET @created_by := 1;
SET @created_by_name := 'Travel Policy Seed';

START TRANSACTION;

-- -----------------------------------------------------------------------------
-- 1) Travel reasons (add policy items; keep existing)
-- -----------------------------------------------------------------------------
INSERT INTO travel_reason (travel_reason_name, description, is_active, created_by, created_by_name)
SELECT v.travel_reason_name, v.description, 'Y', @created_by, @created_by_name
FROM (
    SELECT 'Hotel & Lodging' AS travel_reason_name,
           'Admin-booked guest house/hotel or self-arranged stay per Travel Policy Annexure I §2 (limits by city tier)' AS description
    UNION ALL SELECT 'Business Meeting',
           'Client/prospect meetings — reimbursable reasonable costs; Director pre-approval for limits (Annexure I #3)'
    UNION ALL SELECT 'Intercity Travel',
           'Flight/Train/Bus — travel plan approved 7 days in advance; Admin books tickets (Annexure I #1)'
    UNION ALL SELECT 'Local Travel',
           'Cab/Auto/Bus/Metro for intermittent business locations; not home-to-base (Annexure I #5)'
) v
WHERE NOT EXISTS (
    SELECT 1 FROM travel_reason tr WHERE tr.travel_reason_name = v.travel_reason_name
);

-- -----------------------------------------------------------------------------
-- 2) Travel modes — policy transport types
-- -----------------------------------------------------------------------------
INSERT INTO travel_mode (travel_reason_id, mode_type, description, is_active, created_by, created_by_name)
SELECT tr.id, v.mode_type, v.description, 'Y', @created_by, @created_by_name
FROM (
    SELECT 'Client Meeting' AS reason_name, 'Bus' AS mode_type, 'Inter-city bus where applicable (Annexure I #1)' AS description
    UNION ALL SELECT 'Project Deployment', 'Bus', 'Bus for team movement to project site'
    UNION ALL SELECT 'Conference / Seminar', 'Bus', 'Bus travel to conference location'
    UNION ALL SELECT 'Intercity Travel', 'Flight', 'Air travel — Admin provides 2 options then books'
    UNION ALL SELECT 'Intercity Travel', 'Train', 'Rail — preferred if duration under 12 hours per policy'
    UNION ALL SELECT 'Intercity Travel', 'Bus', 'Bus for intercity travel'
    UNION ALL SELECT 'Intercity Travel', 'Cab / Taxi', 'Cab for airport/station transfers (PM+ within 300 km per policy)'
    UNION ALL SELECT 'Local Travel', 'Cab / Taxi', 'Uber Go / Ola Mini/Micro only where possible (Annexure I #5)'
    UNION ALL SELECT 'Local Travel', 'Metro', 'Metro / local train for business commute'
    UNION ALL SELECT 'Local Travel', 'Bus', 'Local bus for intermittent business travel'
    UNION ALL SELECT 'Business Meeting', 'Cab / Taxi', 'Local travel to meeting venue'
    UNION ALL SELECT 'Hotel & Lodging', 'Hotel Booking', 'Lodging arranged by Admin or employee per policy limits'
) v
INNER JOIN travel_reason tr ON tr.travel_reason_name = v.reason_name
WHERE NOT EXISTS (
    SELECT 1 FROM travel_mode tm
    WHERE tm.travel_reason_id = tr.id AND tm.mode_type = v.mode_type
);

-- -----------------------------------------------------------------------------
-- 3) Travel classes — align with policy defaults
-- -----------------------------------------------------------------------------
INSERT INTO travel_class (travel_reason_id, travel_mode_id, travel_class, description, is_active, created_by, created_by_name)
SELECT tr.id, tm.travel_mode_id, v.travel_class, v.description, 'Y', @created_by, @created_by_name
FROM (
    SELECT 'Intercity Travel' AS reason_name, 'Flight' AS mode_type, 'Economy' AS travel_class,
           'Default class unless PM+ / Director approval for higher' AS description
    UNION ALL SELECT 'Intercity Travel', 'Train', 'AC 2 Tier', 'Preferred for journeys under 12 hours'
    UNION ALL SELECT 'Intercity Travel', 'Train', 'AC 3 Tier', 'Cost-effective rail option'
    UNION ALL SELECT 'Intercity Travel', 'Cab / Taxi', 'Sedan', 'Airport / station transfers'
    UNION ALL SELECT 'Local Travel', 'Cab / Taxi', 'Uber Go / Ola Mini',
           'Policy: Uber Go or Ola Mini/Micro only'
    UNION ALL SELECT 'Local Travel', 'Cab / Taxi', 'Sedan', 'Approved cab class for Above TL'
    UNION ALL SELECT 'Business Meeting', 'Cab / Taxi', 'Uber Go / Ola Mini', 'Local meeting travel'
    UNION ALL SELECT 'Client Meeting', 'Cab / Taxi', 'Uber Go / Ola Mini', 'Policy cab tier'
    UNION ALL SELECT 'Hotel & Lodging', 'Hotel Booking', 'Standard', 'Per night limit per city tier category'
) v
INNER JOIN travel_reason tr ON tr.travel_reason_name = v.reason_name
INNER JOIN travel_mode tm ON tm.travel_reason_id = tr.id AND tm.mode_type = v.mode_type
WHERE NOT EXISTS (
    SELECT 1 FROM travel_class tc
    WHERE tc.travel_reason_id = tr.id AND tc.travel_mode_id = tm.travel_mode_id AND tc.travel_class = v.travel_class
);

-- -----------------------------------------------------------------------------
-- 4) Hotel categories — Annexure I per-night limits (policy v0.1)
-- Deactivate legacy star-rating categories (optional cleanup)
-- -----------------------------------------------------------------------------
UPDATE hotel_category
SET is_active = 'N', updated_on = NOW()
WHERE hotel_category IN ('Budget (3 Star)', 'Standard (4 Star)', 'Premium (5 Star)', 'Extended Stay')
  AND is_active = 'Y';

INSERT INTO hotel_category (hotel_category, description, is_active, created_by, created_by_name)
SELECT v.hotel_category, v.description, 'Y', @created_by, @created_by_name
FROM (
    SELECT 'Metro — TL and Below' AS hotel_category,
           'Max INR 2,000 per night (Metros). Admin books per tie-ups; guest house first.' AS description
    UNION ALL SELECT 'Metro — Above TL',
           'Max INR 4,000 per night (Metros). Equivalent senior designations.'
    UNION ALL SELECT 'Tier A Cities',
           'Max INR 1,800 per night (Annexure II column A cities)'
    UNION ALL SELECT 'Tier B Cities',
           'Max INR 1,500 per night (Annexure II column B cities)'
    UNION ALL SELECT 'Guest House / Company Facility',
           'Company guest house where available; Admin books'
    UNION ALL SELECT 'Friends / Relatives Stay',
           '50% of applicable limit; no bills required for reimbursement (Annexure I #2)'
) v
WHERE NOT EXISTS (SELECT 1 FROM hotel_category hc WHERE hc.hotel_category = v.hotel_category);

-- -----------------------------------------------------------------------------
-- 5) Hotel sub-categories
-- -----------------------------------------------------------------------------
INSERT INTO hotel_sub_category (hotel_category_id, hotel_sub_category_name, description, is_active, created_by, created_by_name)
SELECT hc.id, v.sub_name, v.description, 'Y', @created_by, @created_by_name
FROM (
    SELECT 'Metro — TL and Below' AS cat, 'Single Occupancy' AS sub_name, 'Default single room' AS description
    UNION ALL SELECT 'Metro — TL and Below', 'Twin Sharing', 'Two colleagues same trip'
    UNION ALL SELECT 'Metro — Above TL', 'Single Occupancy', 'Default single room'
    UNION ALL SELECT 'Metro — Above TL', 'Twin Sharing', 'Twin room'
    UNION ALL SELECT 'Metro — Above TL', 'Deluxe Room', 'Extended stay over 5 nights'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Tier A limit applies'
    UNION ALL SELECT 'Tier A Cities', 'Twin Sharing', 'Shared room Tier A'
    UNION ALL SELECT 'Tier B Cities', 'Single Occupancy', 'Tier B limit applies'
    UNION ALL SELECT 'Tier B Cities', 'Twin Sharing', 'Shared room Tier B'
    UNION ALL SELECT 'Guest House / Company Facility', 'Company Guest House', 'Stay at company guest house'
    UNION ALL SELECT 'Friends / Relatives Stay', 'Stay with Friends/Relatives', '50% limit reimbursement'
    UNION ALL SELECT 'Metro — TL and Below', 'Self-Booked (Admin Unavailable)', 'Employee books; max limit applies; submit bills'
    UNION ALL SELECT 'Metro — Above TL', 'Self-Booked (Admin Unavailable)', 'Employee books; max limit applies; submit bills'
    UNION ALL SELECT 'Tier A Cities', 'Self-Booked (Admin Unavailable)', 'Employee books when Admin/guest house unavailable'
    UNION ALL SELECT 'Tier B Cities', 'Self-Booked (Admin Unavailable)', 'Employee books when Admin/guest house unavailable'
) v
INNER JOIN hotel_category hc ON hc.hotel_category = v.cat AND hc.is_active = 'Y'
WHERE NOT EXISTS (
    SELECT 1 FROM hotel_sub_category hsc
    WHERE hsc.hotel_category_id = hc.id AND hsc.hotel_sub_category_name = v.sub_name
);

-- -----------------------------------------------------------------------------
-- 6) Cities — Annexure II (Metro / Tier A / Tier B)
-- -----------------------------------------------------------------------------
INSERT INTO city (hotel_category_id, hotel_sub_category_id, city_name, description, is_active, created_by, created_by_name)
SELECT hc.id, hsc.id, v.city_name, v.description, 'Y', @created_by, @created_by_name
FROM (
    -- Metro cities (both TL tiers get same city list; limit chosen via category)
    SELECT 'Metro — TL and Below' AS cat, 'Single Occupancy' AS sub, 'Mumbai' AS city_name, 'Metro — Annexure II' AS description
    UNION ALL SELECT 'Metro — TL and Below', 'Single Occupancy', 'Delhi', 'Metro'
    UNION ALL SELECT 'Metro — TL and Below', 'Single Occupancy', 'Chennai', 'Metro'
    UNION ALL SELECT 'Metro — TL and Below', 'Single Occupancy', 'Kolkata', 'Metro'
    UNION ALL SELECT 'Metro — TL and Below', 'Single Occupancy', 'Bengaluru', 'Metro'
    UNION ALL SELECT 'Metro — TL and Below', 'Single Occupancy', 'Hyderabad', 'Metro'
    UNION ALL SELECT 'Metro — Above TL', 'Single Occupancy', 'Mumbai', 'Metro'
    UNION ALL SELECT 'Metro — Above TL', 'Single Occupancy', 'Delhi', 'Metro'
    UNION ALL SELECT 'Metro — Above TL', 'Single Occupancy', 'Chennai', 'Metro'
    UNION ALL SELECT 'Metro — Above TL', 'Single Occupancy', 'Kolkata', 'Metro'
    UNION ALL SELECT 'Metro — Above TL', 'Single Occupancy', 'Bengaluru', 'Metro'
    UNION ALL SELECT 'Metro — Above TL', 'Single Occupancy', 'Hyderabad', 'Metro'
    -- Tier A
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Ahmedabad', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Baroda', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Rajkot', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Indore', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Bhopal', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Pune', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Nagpur', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Ludhiana', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Jaipur', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Jammu', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Srinagar', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Varanasi', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Amritsar', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Ghaziabad', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Chandigarh', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Kanpur', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Guwahati', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Ranchi', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Patna', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Jamshedpur', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Lucknow', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Siliguri', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Bhubaneswar', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Cuttack', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Panaji', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Vizag', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Trivendrum', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Vijaywada', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Kochi', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Madurai', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Gangtok', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Mangalore', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Udaipur', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Jodhpur', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Agra', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Mohali', 'Tier A'
    UNION ALL SELECT 'Tier A Cities', 'Single Occupancy', 'Coimbatore', 'Tier A'
    -- Tier B
    UNION ALL SELECT 'Tier B Cities', 'Single Occupancy', 'Surat', 'Tier B'
) v
INNER JOIN hotel_category hc ON hc.hotel_category = v.cat AND hc.is_active = 'Y'
INNER JOIN hotel_sub_category hsc ON hsc.hotel_category_id = hc.id AND hsc.hotel_sub_category_name = v.sub
WHERE NOT EXISTS (
    SELECT 1 FROM city c
    WHERE c.hotel_category_id = hc.id AND c.hotel_sub_category_id = hsc.id AND c.city_name = v.city_name
);

COMMIT;

-- -----------------------------------------------------------------------------
-- Verify
-- -----------------------------------------------------------------------------
SELECT 'travel_reason' AS entity, COUNT(*) AS cnt,
       SUM(CASE WHEN is_active = 'Y' THEN 1 ELSE 0 END) AS active_cnt FROM travel_reason
UNION ALL SELECT 'travel_mode', COUNT(*), SUM(CASE WHEN is_active = 'Y' THEN 1 ELSE 0 END) FROM travel_mode
UNION ALL SELECT 'travel_class', COUNT(*), SUM(CASE WHEN is_active = 'Y' THEN 1 ELSE 0 END) FROM travel_class
UNION ALL SELECT 'hotel_category', COUNT(*), SUM(CASE WHEN is_active = 'Y' THEN 1 ELSE 0 END) FROM hotel_category
UNION ALL SELECT 'city', COUNT(*), SUM(CASE WHEN is_active = 'Y' THEN 1 ELSE 0 END) FROM city;

SELECT travel_reason_name, is_active FROM travel_reason ORDER BY travel_reason_name;

SELECT hotel_category, is_active, LEFT(description, 60) AS policy_hint
FROM hotel_category ORDER BY is_active DESC, hotel_category;
