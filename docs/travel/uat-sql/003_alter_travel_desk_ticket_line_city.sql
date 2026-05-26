-- Add hotel city to ticket lines (entity TravelDeskTicketLine.city).
-- Safe to re-run: skips if column already exists.

SET @db := DATABASE();

SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db
    AND TABLE_NAME = 'travel_desk_ticket_line'
    AND COLUMN_NAME = 'city'
);

SET @ddl := IF(
  @col_exists = 0,
  'ALTER TABLE travel_desk_ticket_line ADD COLUMN city VARCHAR(256) NULL AFTER hotel_sub_category',
  'SELECT ''city column already exists'' AS info'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
