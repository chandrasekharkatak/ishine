-- =============================================================================
-- UAT — db_emp_backup_new @ 192.168.21.195
-- Travel configuration master data (idempotent INSERT only)
-- Tables: travel_reason, travel_mode, travel_class, hotel_category,
--         hotel_sub_category, city
-- =============================================================================

USE db_emp_backup_new;

SET @created_by := 1;
SET @created_by_name := 'Admin1';

START TRANSACTION;

-- -----------------------------------------------------------------------------
-- 1) Travel reasons
-- -----------------------------------------------------------------------------
INSERT INTO travel_reason (travel_reason_name, description, is_active, created_by, created_by_name)
SELECT v.travel_reason_name, v.description, 'Y', @created_by, @created_by_name
FROM (
    SELECT 'Client Meeting' AS travel_reason_name,
           'Official visit to client office for meetings, reviews, or sign-offs' AS description
    UNION ALL SELECT 'Project Deployment',
           'On-site implementation, UAT support, or production go-live'
    UNION ALL SELECT 'Training & Certification',
           'External training, vendor certification, or skill workshops'
    UNION ALL SELECT 'Conference / Seminar',
           'Industry conferences, summits, and partner events'
    UNION ALL SELECT 'Pre-Sales / Demo',
           'Client demos, RFP presentations, and solution walkthroughs'
    UNION ALL SELECT 'Internal Team Offsite',
           'Team planning, retrospectives, and internal workshops'
    UNION ALL SELECT 'Recruitment Drive',
           'Campus hiring, walk-in drives, and interview panels'
    UNION ALL SELECT 'Vendor / Partner Visit',
           'Meetings with vendors, OEMs, or alliance partners'
) v
WHERE NOT EXISTS (
    SELECT 1 FROM travel_reason tr
    WHERE tr.travel_reason_name = v.travel_reason_name
);

-- -----------------------------------------------------------------------------
-- 2) Travel modes (per reason)
-- -----------------------------------------------------------------------------
INSERT INTO travel_mode (travel_reason_id, mode_type, description, is_active, created_by, created_by_name)
SELECT tr.id, v.mode_type, v.description, 'Y', @created_by, @created_by_name
FROM (
    SELECT 'Client Meeting' AS reason_name, 'Flight' AS mode_type, 'Domestic or international air travel' AS description
    UNION ALL SELECT 'Client Meeting', 'Train', 'Rail travel for inter-city client visits'
    UNION ALL SELECT 'Client Meeting', 'Cab / Taxi', 'Local commute to client site or airport'
    UNION ALL SELECT 'Project Deployment', 'Flight', 'Air travel for deployment teams'
    UNION ALL SELECT 'Project Deployment', 'Train', 'Rail for medium-distance project locations'
    UNION ALL SELECT 'Project Deployment', 'Cab / Taxi', 'Site commute and airport transfers'
    UNION ALL SELECT 'Training & Certification', 'Flight', 'Travel to training centre or vendor location'
    UNION ALL SELECT 'Training & Certification', 'Train', 'Rail for regional training programs'
    UNION ALL SELECT 'Conference / Seminar', 'Flight', 'Travel to conference city'
    UNION ALL SELECT 'Conference / Seminar', 'Train', 'Rail when venue is well connected'
    UNION ALL SELECT 'Pre-Sales / Demo', 'Flight', 'Quick turnaround for client demos'
    UNION ALL SELECT 'Pre-Sales / Demo', 'Cab / Taxi', 'City visits for multiple client meetings'
    UNION ALL SELECT 'Internal Team Offsite', 'Bus', 'Group travel for team offsites'
    UNION ALL SELECT 'Internal Team Offsite', 'Cab / Taxi', 'Local transport at offsite venue'
    UNION ALL SELECT 'Recruitment Drive', 'Train', 'Travel to campus or hiring city'
    UNION ALL SELECT 'Recruitment Drive', 'Cab / Taxi', 'Local travel between venues'
    UNION ALL SELECT 'Vendor / Partner Visit', 'Flight', 'Partner or OEM meetings in other cities'
    UNION ALL SELECT 'Vendor / Partner Visit', 'Cab / Taxi', 'Local vendor office visits'
) v
INNER JOIN travel_reason tr ON tr.travel_reason_name = v.reason_name
WHERE NOT EXISTS (
    SELECT 1 FROM travel_mode tm
    WHERE tm.travel_reason_id = tr.id
      AND tm.mode_type = v.mode_type
);

-- -----------------------------------------------------------------------------
-- 3) Travel classes (per reason + mode)
-- -----------------------------------------------------------------------------
INSERT INTO travel_class (
    travel_reason_id, travel_mode_id, travel_class, description, is_active, created_by, created_by_name
)
SELECT tr.id, tm.travel_mode_id, v.travel_class, v.description, 'Y', @created_by, @created_by_name
FROM (
    SELECT 'Client Meeting' AS reason_name, 'Flight' AS mode_type, 'Economy' AS travel_class,
           'Standard economy class — default for domestic travel' AS description
    UNION ALL SELECT 'Client Meeting', 'Flight', 'Premium Economy',
           'Extra legroom; approved for journeys over 4 hours'
    UNION ALL SELECT 'Client Meeting', 'Flight', 'Business',
           'Business class — director approval required'
    UNION ALL SELECT 'Client Meeting', 'Train', 'AC 2 Tier',
           'AC 2-tier — default for overnight rail'
    UNION ALL SELECT 'Client Meeting', 'Train', 'AC 3 Tier',
           'AC 3-tier — cost-effective rail option'
    UNION ALL SELECT 'Client Meeting', 'Cab / Taxi', 'Sedan',
           'Standard sedan for 1–3 passengers'
    UNION ALL SELECT 'Client Meeting', 'Cab / Taxi', 'SUV',
           'SUV for team travel or extra luggage'
    UNION ALL SELECT 'Project Deployment', 'Flight', 'Economy', 'Default air class for deployment teams'
    UNION ALL SELECT 'Project Deployment', 'Train', 'AC 2 Tier', 'Preferred rail for multi-day deployment'
    UNION ALL SELECT 'Project Deployment', 'Cab / Taxi', 'Sedan', 'Airport and site transfers'
    UNION ALL SELECT 'Training & Certification', 'Flight', 'Economy', 'Training travel — economy only'
    UNION ALL SELECT 'Training & Certification', 'Train', 'AC 3 Tier', 'Regional training by rail'
    UNION ALL SELECT 'Conference / Seminar', 'Flight', 'Economy', 'Conference attendance — economy'
    UNION ALL SELECT 'Conference / Seminar', 'Flight', 'Business',
           'Business class for keynote speakers — approval required'
    UNION ALL SELECT 'Conference / Seminar', 'Train', 'AC 2 Tier', 'Rail to conference hub cities'
    UNION ALL SELECT 'Pre-Sales / Demo', 'Flight', 'Economy', 'Pre-sales domestic travel'
    UNION ALL SELECT 'Pre-Sales / Demo', 'Cab / Taxi', 'Sedan', 'Client hop meetings in same city'
    UNION ALL SELECT 'Internal Team Offsite', 'Bus', 'AC Seater', 'AC coach for team offsites'
    UNION ALL SELECT 'Internal Team Offsite', 'Cab / Taxi', 'Sedan', 'Local offsite transfers'
    UNION ALL SELECT 'Recruitment Drive', 'Train', 'AC 3 Tier', 'Campus drive — rail travel'
    UNION ALL SELECT 'Recruitment Drive', 'Cab / Taxi', 'Sedan', 'Venue-to-venue in hiring city'
    UNION ALL SELECT 'Vendor / Partner Visit', 'Flight', 'Economy', 'Vendor meetings — economy'
    UNION ALL SELECT 'Vendor / Partner Visit', 'Cab / Taxi', 'Sedan', 'Vendor office local travel'
) v
INNER JOIN travel_reason tr ON tr.travel_reason_name = v.reason_name
INNER JOIN travel_mode tm ON tm.travel_reason_id = tr.id AND tm.mode_type = v.mode_type
WHERE NOT EXISTS (
    SELECT 1 FROM travel_class tc
    WHERE tc.travel_reason_id = tr.id
      AND tc.travel_mode_id = tm.travel_mode_id
      AND tc.travel_class = v.travel_class
);

-- -----------------------------------------------------------------------------
-- 4) Hotel categories
-- -----------------------------------------------------------------------------
INSERT INTO hotel_category (hotel_category, description, is_active, created_by, created_by_name)
SELECT v.hotel_category, v.description, 'Y', @created_by, @created_by_name
FROM (
    SELECT 'Budget (3 Star)' AS hotel_category,
           'Approved budget hotels for short client visits' AS description
    UNION ALL SELECT 'Standard (4 Star)',
           'Default category for most business travel'
    UNION ALL SELECT 'Premium (5 Star)',
           'Senior management or key client engagements'
    UNION ALL SELECT 'Extended Stay',
           'Serviced apartments for long on-site assignments'
) v
WHERE NOT EXISTS (
    SELECT 1 FROM hotel_category hc WHERE hc.hotel_category = v.hotel_category
);

-- -----------------------------------------------------------------------------
-- 5) Hotel sub-categories
-- -----------------------------------------------------------------------------
INSERT INTO hotel_sub_category (
    hotel_category_id, hotel_sub_category_name, description, is_active, created_by, created_by_name
)
SELECT hc.id, v.sub_name, v.description, 'Y', @created_by, @created_by_name
FROM (
    SELECT 'Budget (3 Star)' AS cat, 'Single Occupancy' AS sub_name,
           'Single room — standard for solo travel' AS description
    UNION ALL SELECT 'Budget (3 Star)', 'Twin Sharing',
           'Twin sharing for two colleagues on same trip'
    UNION ALL SELECT 'Standard (4 Star)', 'Single Occupancy',
           'Single room — default business stay'
    UNION ALL SELECT 'Standard (4 Star)', 'Twin Sharing',
           'Twin room for team members'
    UNION ALL SELECT 'Standard (4 Star)', 'Deluxe Room',
           'Deluxe room for stays over 5 nights'
    UNION ALL SELECT 'Premium (5 Star)', 'Single Occupancy',
           'Premium single — approval required'
    UNION ALL SELECT 'Premium (5 Star)', 'Executive Suite',
           'Suite for CXO / strategic client visits'
    UNION ALL SELECT 'Extended Stay', 'Studio Apartment',
           'Studio for 2–4 week deployments'
    UNION ALL SELECT 'Extended Stay', '1 BHK Apartment',
           '1 BHK for family-accompanied long stays'
) v
INNER JOIN hotel_category hc ON hc.hotel_category = v.cat
WHERE NOT EXISTS (
    SELECT 1 FROM hotel_sub_category hsc
    WHERE hsc.hotel_category_id = hc.id
      AND hsc.hotel_sub_category_name = v.sub_name
);

-- -----------------------------------------------------------------------------
-- 6) Cities (linked to hotel category + sub-category)
-- -----------------------------------------------------------------------------
INSERT INTO city (
    hotel_category_id, hotel_sub_category_id, city_name, description, is_active, created_by, created_by_name
)
SELECT hc.id, hsc.id, v.city_name, v.description, 'Y', @created_by, @created_by_name
FROM (
    SELECT 'Standard (4 Star)' AS cat, 'Single Occupancy' AS sub, 'Mumbai' AS city_name,
           'Mumbai — BKC, Andheri, Powai delivery centres' AS description
    UNION ALL SELECT 'Standard (4 Star)', 'Single Occupancy', 'Bengaluru',
           'Bengaluru — Electronic City, Whitefield, ORR'
    UNION ALL SELECT 'Standard (4 Star)', 'Single Occupancy', 'Pune',
           'Pune — Hinjewadi, Magarpatta, Kharadi'
    UNION ALL SELECT 'Standard (4 Star)', 'Single Occupancy', 'Hyderabad',
           'Hyderabad — HITEC City, Gachibowli, Madhapur'
    UNION ALL SELECT 'Standard (4 Star)', 'Single Occupancy', 'Chennai',
           'Chennai — OMR, Guindy, Tidel Park corridor'
    UNION ALL SELECT 'Standard (4 Star)', 'Single Occupancy', 'Gurugram',
           'NCR — Gurugram Cyber City and Udyog Vihar'
    UNION ALL SELECT 'Standard (4 Star)', 'Single Occupancy', 'Noida',
           'NCR — Noida Sector 62, 135, Expressway'
    UNION ALL SELECT 'Standard (4 Star)', 'Twin Sharing', 'Mumbai', 'Twin sharing — Mumbai engagements'
    UNION ALL SELECT 'Standard (4 Star)', 'Twin Sharing', 'Bengaluru', 'Twin sharing — Bengaluru project teams'
    UNION ALL SELECT 'Budget (3 Star)', 'Single Occupancy', 'Kolkata',
           'Kolkata — Salt Lake, New Town'
    UNION ALL SELECT 'Budget (3 Star)', 'Single Occupancy', 'Ahmedabad',
           'Ahmedabad — SG Highway, GIFT City'
    UNION ALL SELECT 'Premium (5 Star)', 'Single Occupancy', 'Mumbai',
           'Premium stays — Mumbai financial district'
    UNION ALL SELECT 'Premium (5 Star)', 'Executive Suite', 'Bengaluru',
           'Executive suite — key client workshops'
    UNION ALL SELECT 'Extended Stay', 'Studio Apartment', 'Pune',
           'Long deployment — Pune Hinjewadi'
    UNION ALL SELECT 'Extended Stay', '1 BHK Apartment', 'Hyderabad',
           'Extended assignment — Hyderabad Gachibowli'
) v
INNER JOIN hotel_category hc ON hc.hotel_category = v.cat
INNER JOIN hotel_sub_category hsc
    ON hsc.hotel_category_id = hc.id AND hsc.hotel_sub_category_name = v.sub
WHERE NOT EXISTS (
    SELECT 1 FROM city c
    WHERE c.hotel_category_id = hc.id
      AND c.hotel_sub_category_id = hsc.id
      AND c.city_name = v.city_name
);

COMMIT;

-- -----------------------------------------------------------------------------
-- Verify
-- -----------------------------------------------------------------------------
SELECT 'travel_reason' AS entity, COUNT(*) AS cnt FROM travel_reason
UNION ALL SELECT 'travel_mode', COUNT(*) FROM travel_mode
UNION ALL SELECT 'travel_class', COUNT(*) FROM travel_class
UNION ALL SELECT 'hotel_category', COUNT(*) FROM hotel_category
UNION ALL SELECT 'hotel_sub_category', COUNT(*) FROM hotel_sub_category
UNION ALL SELECT 'city', COUNT(*) FROM city;

SELECT tr.travel_reason_name, tm.mode_type, tc.travel_class
FROM travel_reason tr
LEFT JOIN travel_mode tm ON tm.travel_reason_id = tr.id
LEFT JOIN travel_class tc ON tc.travel_mode_id = tm.travel_mode_id AND tc.travel_reason_id = tr.id
ORDER BY tr.travel_reason_name, tm.mode_type, tc.travel_class;

SELECT hc.hotel_category, hsc.hotel_sub_category_name, c.city_name
FROM hotel_category hc
LEFT JOIN hotel_sub_category hsc ON hsc.hotel_category_id = hc.id
LEFT JOIN city c ON c.hotel_sub_category_id = hsc.id AND c.hotel_category_id = hc.id
ORDER BY hc.hotel_category, hsc.hotel_sub_category_name, c.city_name;
