# TimesheetActivityMapRepository → TimesheetActivityMapNewRepository Migration Analysis

## Overview
This document analyzes the changes required to migrate queries from `TimesheetActivityMapRepository` to `TimesheetActivityMapNewRepository`.

**Date:** 2025-01-30

---

## Table Structure Changes

### Old Table: `EmployeeTimesheetActivitiesMapping`
- **Primary Key:** `timesheet_activity_map_id` (Long, auto-generated)
- **Columns:**
  - `timesheet_id` (Long)
  - `activity_id` (Long)
  - `completion_time` (Float) - in hours
  - `description` (String)
  - `client_location_id` (Integer)

### New Table: `employee_timesheet_activities_mapping_new`
- **Primary Key:** Composite key (`timesheet_id`, `activity_id`, `project_id`)
- **Columns:**
  - `timesheet_id` (Long) - part of composite key
  - `activity_id` (Long) - part of composite key
  - `project_id` (Long) - part of composite key (NEW)
  - `duration_minutes` (Short) - in minutes (was `completion_time` in hours)
  - `description` (String)
  - **Removed:** `client_location_id` (no longer in table)

---

## Queries to Update

### 1. `activitiesByTimesheetId` (Named Query)
- **Location:** `jpa-named-queries.properties` line ~1838
- **Changes Needed:**
  - Table: `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
  - Column: `map.completion_time` → `CAST(etam.duration_minutes AS DECIMAL(10,2))/60` (convert minutes to hours)
  - Column: `map.timesheet_activity_map_id` → Remove (no longer exists, use composite key fields)
  - Column: `map.client_location_id` → Remove (no longer exists)
  - JOIN: `employee_timesheets` → `employee_timesheets_new`
  - JOIN: Add master table JOINs if needed

### 2. `activitiesByTimesheetIdforBiomax` (Inline Query)
- **Location:** `TimesheetActivityMapNewRepository.java` line ~31
- **Changes Needed:**
  - Table: `employee_timesheets` → `employee_timesheets_new`
  - Table: `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
  - Column: `etam.completion_time` → `CAST(etam.duration_minutes AS DECIMAL(10,2))/60`
  - Add master table JOINs for `day_type` if needed

### 3. `getTimesheetActivityByTimesheetId` (Named Query)
- **Location:** `jpa-named-queries.properties` line ~1875
- **Changes Needed:**
  - Table: `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
  - Return type: `TimesheetActivityMap` → `EmployeeTimesheetActivitiesMappingNew`

### 4. `findByTimesheetId` (Method)
- **Location:** `TimesheetActivityMapNewRepository.java` line ~47
- **Changes Needed:**
  - Return type: `List<TimesheetActivityMap>` → `List<EmployeeTimesheetActivitiesMappingNew>`
  - This should work automatically if the entity is correctly mapped

### 5. `deleteByTimesheetId` (Method)
- **Location:** `TimesheetActivityMapNewRepository.java` line ~45
- **Changes Needed:**
  - Should work as-is (uses composite key)

### 6. `countByActivityId` (Method)
- **Location:** `TimesheetActivityMapNewRepository.java` line ~50
- **Changes Needed:**
  - Should work as-is (uses activity_id from composite key)

---

## Key Changes Summary

1. **Table Names:**
   - `employee_timesheet_activities_mapping` → `employee_timesheet_activities_mapping_new`
   - `employee_timesheets` → `employee_timesheets_new` (in JOINs)

2. **Column Mappings:**
   - `completion_time` → `CAST(duration_minutes AS DECIMAL(10,2))/60` (convert minutes to hours)
   - `timesheet_activity_map_id` → Remove (use composite key fields instead)
   - `client_location_id` → Remove (no longer exists)

3. **Entity/Return Types:**
   - `TimesheetActivityMap` → `EmployeeTimesheetActivitiesMappingNew`

4. **Composite Key:**
   - New table uses composite key: `(timesheet_id, activity_id, project_id)`
   - Queries may need to include `project_id` in WHERE clauses if filtering by composite key

---

## Implementation Plan

1. ✅ Update named queries in `jpa-named-queries.properties`
2. ✅ Update inline queries in `TimesheetActivityMapNewRepository.java`
3. ✅ Update return types in repository methods
4. ✅ Create backup of old queries (add `OLD` suffix)
5. ✅ Test all queries

---

**Status:** Ready to implement

