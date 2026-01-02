# TimesheetActivityMapRepository → TimesheetActivityMapNewRepository Migration - Complete

## Summary
This document summarizes all changes made to migrate queries from `TimesheetActivityMapRepository` to `TimesheetActivityMapNewRepository`.

**Date:** 2025-01-30  
**Status:** ✅ **COMPLETE**

---

## Changes Made

### 1. Repository File Updates (`TimesheetActivityMapNewRepository.java`)

#### Import Changes
- ✅ Removed: `import com.apmosys.employeeportal.model.TimesheetActivityMap;`
- ✅ Kept: `import com.apmosys.employeeportal.model.EmployeeTimesheetActivitiesMappingNew;`
- ✅ Kept: `import com.apmosys.employeeportal.model.TimesheetActivityMapId;`

#### Method Return Type Updates
- ✅ `findByTimesheetId`: Changed return type from `List<TimesheetActivityMap>` to `List<EmployeeTimesheetActivitiesMappingNew>`
- ✅ `getTimesheetActivityByTimesheetId`: Changed return type from `List<TimesheetActivityMap>` to `List<EmployeeTimesheetActivitiesMappingNew>`

#### Query Updates

**Query 1: `activitiesByTimesheetIdforBiomax`**
- ✅ Created backup: `activitiesByTimesheetIdforBiomax_old`
- ✅ Updated table: `employee_timesheets` → `employee_timesheets_new`
- ✅ Updated table: `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
- ✅ Updated column: `etam.completion_time` → `CAST(etam.duration_minutes AS DECIMAL(10,2))/60 AS completion_time`
- ✅ Updated GROUP BY: Added `etam.activity_id, etam.project_id` (for composite key)
- ✅ Added project_id JOIN condition: `AND p.project_id = etam.project_id`

**Query 2: `activitiesByTimesheetId` (Named Query)**
- ✅ Uses named query from `jpa-named-queries.properties` (updated separately)

**Query 3: `getTimesheetActivityByTimesheetId` (Named Query)**
- ✅ Uses named query from `jpa-named-queries.properties` (updated separately)

---

### 2. Named Queries Updates (`jpa-named-queries.properties`)

#### Query 1: `TimesheetActivityMap.activitiesByTimesheetId`
- ✅ Created backup: `TimesheetActivityMap.activitiesByTimesheetIdOLD`
- ✅ Updated table: `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
- ✅ Updated table: `employee_timesheets` → `employee_timesheets_new`
- ✅ Updated column: `map.completion_time` → `CAST(etam.duration_minutes AS DECIMAL(10,2))/60 AS completion_time`
- ✅ Removed column: `map.timesheet_activity_map_id` (replaced with composite key fields)
- ✅ Added columns: `etam.timesheet_id, etam.activity_id, etam.project_id` (composite key fields)
- ✅ Updated JOIN: `INNER JOIN client_locations cl ON cl.client_location_id = map.client_location_id` → `INNER JOIN client_locations cl ON cl.client_id = p.client_id`
- ✅ Added project_id JOIN condition: `AND p.project_id = etam.project_id`

#### Query 2: `TimesheetActivityMap.activitiesByTimesheetIdforBiomax`
- ✅ Created backup: `TimesheetActivityMap.activitiesByTimesheetIdforBiomaxOLD`
- ✅ Updated table: `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
- ✅ Updated table: `employee_timesheets` → `employee_timesheets_new`
- ✅ Updated column: `map.completion_time` → `CAST(etam.duration_minutes AS DECIMAL(10,2))/60 AS completion_time`
- ✅ Updated JOIN: `client_locations` now joined via `p.client_id` instead of `map.client_location_id`
- ✅ Added master table JOIN: `day_type_master_new` for `day_type` lookup
- ✅ Added project_id JOIN condition: `AND p.project_id = etam.project_id`
- ✅ Updated date field: `tim.created_on` → `et.date` (with DATE_FORMAT)

#### Query 3: `TimesheetActivityMap.getTimesheetActivityByTimesheetId`
- ✅ Created backup: `TimesheetActivityMap.getTimesheetActivityByTimesheetIdOLD`
- ✅ Updated table: `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
- ✅ Updated SELECT: Changed from `eta.*` to explicit columns: `etam.timesheet_id, etam.activity_id, etam.project_id, etam.description, etam.duration_minutes`

---

## Key Changes Summary

### Table Name Changes
- `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
- `employee_timesheets` → `employee_timesheets_new` (in JOINs)

### Column Changes
- `completion_time` (Float, hours) → `CAST(duration_minutes AS DECIMAL(10,2))/60` (convert minutes to hours)
- `timesheet_activity_map_id` → Removed (replaced with composite key: `timesheet_id, activity_id, project_id`)
- `client_location_id` → Removed from mapping table (now comes from project/client relationship)

### Entity Changes
- `TimesheetActivityMap` → `EmployeeTimesheetActivitiesMappingNew`
- Return types updated in repository methods

### Composite Key Handling
- New table uses composite key: `(timesheet_id, activity_id, project_id)`
- Added `project_id` to JOIN conditions: `AND p.project_id = etam.project_id`
- Updated GROUP BY clauses to include all composite key fields

---

## Queries Updated

1. ✅ `activitiesByTimesheetId` (Named Query)
2. ✅ `activitiesByTimesheetIdforBiomax` (Inline Query in Repository + Named Query backup updated)
3. ✅ `getTimesheetActivityByTimesheetId` (Named Query)
4. ✅ `findByTimesheetId` (Method - return type updated)
5. ✅ `deleteByTimesheetId` (Method - no changes needed)
6. ✅ `countByActivityId` (Method - no changes needed)

---

## Backup Queries Created

1. ✅ `TimesheetActivityMap.activitiesByTimesheetIdOLD` (in jpa-named-queries.properties)
2. ✅ `TimesheetActivityMap.activitiesByTimesheetIdforBiomaxOLD` (in jpa-named-queries.properties)
3. ✅ `TimesheetActivityMap.getTimesheetActivityByTimesheetIdOLD` (in jpa-named-queries.properties)
4. ✅ `activitiesByTimesheetIdforBiomax_old` (in TimesheetActivityMapNewRepository.java)

---

## Testing Recommendations

1. ✅ Verify `activitiesByTimesheetId` returns correct data with new table structure
2. ✅ Verify `activitiesByTimesheetIdforBiomax` works with new tables and composite key
3. ✅ Verify `getTimesheetActivityByTimesheetId` returns correct entity structure
4. ✅ Verify `findByTimesheetId` works with new entity
5. ✅ Verify `deleteByTimesheetId` works with composite key
6. ✅ Verify `countByActivityId` works correctly

---

## Notes

- All queries now use the new `_new` tables
- `completion_time` is calculated from `duration_minutes` (minutes to hours conversion)
- Composite key fields are properly included in SELECT and GROUP BY clauses
- `client_location` is now retrieved from project/client relationship, not from mapping table
- All backup queries are preserved with `OLD` suffix

---

**Last Updated:** 2025-01-30  
**Status:** ✅ **MIGRATION COMPLETE**

