-- Persist trip type (one-way / round / multi-city / hotel) on ticket lines.

SET @db := DATABASE();

SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db
    AND TABLE_NAME = 'travel_desk_ticket_line'
    AND COLUMN_NAME = 'trip_type'
);

SET @ddl := IF(
  @col_exists = 0,
  'ALTER TABLE travel_desk_ticket_line ADD COLUMN trip_type VARCHAR(32) NULL AFTER travel_class',
  'SELECT ''trip_type column already exists'' AS info'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
