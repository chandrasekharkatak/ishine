-- UAT: stop mandatory LinkedIn page modal on login.
-- Login shows the modal when notifications.notification_type = 'NewLinkedIn page'
-- AND is_active = 'true' AND no row in employee_notification_consent for that emp + notification_id.

UPDATE notifications
SET is_active = 'false', updated_on = NOW()
WHERE notification_type = 'NewLinkedIn page'
  AND LOWER(TRIM(COALESCE(is_active, ''))) IN ('true', 'y', 'yes', '1');

-- Optional: mark consent for one employee without disabling globally
-- INSERT INTO employee_notification_consent (emp_id, notification_id)
-- SELECT :empId, :notificationId FROM DUAL
-- WHERE NOT EXISTS (
--   SELECT 1 FROM employee_notification_consent
--   WHERE emp_id = :empId AND notification_id = :notificationId
-- );
