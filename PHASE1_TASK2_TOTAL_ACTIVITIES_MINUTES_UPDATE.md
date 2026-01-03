# Phase 1 - Task 2: Update for `total_activities_minutes` Column

## Summary
Updated queries to use the new `total_activities_minutes` column for `total_time` calculation and updated `total_working_hours` format.

**Date:** 2025-01-30  
**Status:** Completed

---

## Column Addition
A new column `total_activities_minutes` has been added to `employee_timesheets_new` table.

---

## Changes Applied

### Calculation Updates:

1. **`total_time` calculation:**
   - **Old:** `CAST(et.total_working_minutes AS DECIMAL(10,2))/60 AS total_time`
   - **New:** `ROUND(et.total_activities_minutes / 60, 2) AS total_time`
   - **Reason:** `total_time` should now come from `total_activities_minutes` (sum of all activity durations)

2. **`total_working_hours` calculation:**
   - **Old:** `CAST(et.total_working_minutes AS DECIMAL(10,2))/60 AS total_working_hours`
   - **New:** `TIME_FORMAT(SEC_TO_TIME(et.total_working_minutes * 60), '%H:%i') AS total_working_hours`
   - **Reason:** Format as time string (HH:MM) instead of decimal hours

---

## Queries Updated (5 queries)

### 1. `getMyReporteesTimesheetRequests`
- **File:** `jpa-named-queries.properties`
- **Line:** ~1899
- **Changes:**
  - `total_time`: Now uses `ROUND(et.total_activities_minutes / 60, 2)`
  - `total_working_hours`: Now uses `TIME_FORMAT(SEC_TO_TIME(et.total_working_minutes * 60), '%H:%i')`

### 2. `getMyReporteesApprovedTimesheetRequests2`
- **File:** `jpa-named-queries.properties`
- **Line:** ~1929
- **Changes:**
  - `total_time`: Now uses `ROUND(et.total_activities_minutes / 60, 2)`
  - `total_working_hours`: Now uses `TIME_FORMAT(SEC_TO_TIME(et.total_working_minutes * 60), '%H:%i')`

### 3. `getMyReporteesApprovedTimesheets`
- **File:** `jpa-named-queries.properties`
- **Line:** ~1963
- **Changes:**
  - `total_time`: Now uses `ROUND(et.total_activities_minutes / 60, 2)`
  - `total_working_hours`: Now uses `TIME_FORMAT(SEC_TO_TIME(et.total_working_minutes * 60), '%H:%i')`

### 4. `getAllTimesheetData`
- **File:** `jpa-named-queries.properties`
- **Line:** ~2021
- **Changes:**
  - `total_time`: Now uses `ROUND(et.total_activities_minutes / 60, 2)`
  - `total_working_hours`: Now uses `TIME_FORMAT(SEC_TO_TIME(et.total_working_minutes * 60), '%H:%i')`

### 5. `getLast9DaysFilledTimesheetReport`
- **File:** `jpa-named-queries.properties`
- **Line:** ~2047
- **Changes:**
  - `working_hours`: Now uses `ROUND(et.total_activities_minutes / 60, 2)` (this query uses `working_hours` instead of `total_time`)

---

## Queries NOT Updated (5 queries)

These queries don't use `total_time` or `total_working_hours`:

1. `getTimesheetDataByEmpIdAndDate` - Only returns `emp_id`, `date`, `status`
2. `countMyReporteesTimesheetRequests` - Only returns count
3. `getLast7DaysTimesheetsByEmpId` - Only returns `timesheet_id`, `date`
4. `getTimesheetsForHomePageByEmpId` - Uses `working_hours` calculated from activities mapping (different calculation)
5. `getLast9DaysPendingTimesheetReport` - Only returns count

---

## Notes

1. **`total_activities_minutes`**: Represents the sum of all activity durations for the timesheet
2. **`total_working_minutes`**: Represents the office working time (in-out time difference)
3. **Format Change**: `total_working_hours` now returns formatted time string (e.g., "08:30") instead of decimal (e.g., 8.5)
4. **MySQL Functions Used**:
   - `ROUND(value, 2)` - Rounds to 2 decimal places
   - `TIME_FORMAT(SEC_TO_TIME(seconds), '%H:%i')` - Formats seconds as HH:MM time string

---

## Testing Recommendations

1. Verify `total_time` values match sum of activity durations
2. Verify `total_working_hours` displays correctly as HH:MM format
3. Test edge cases (null values, zero minutes, etc.)
4. Verify calculations match frontend expectations

---

**Document Version:** 1.0  
**Queries Updated:** 5/10 (from first 10 queries)  
**Completion:** 100% of applicable queries

