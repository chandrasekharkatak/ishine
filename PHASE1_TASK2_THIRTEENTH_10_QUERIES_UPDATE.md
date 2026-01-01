# Phase 1, Task 2: Thirteenth Set of Queries Update

## Summary
This document tracks the verification and status of the next set of 10 queries that were identified for update.

**Date:** 2025-01-30

---

## ✅ ALL QUERIES ALREADY UPDATED

All 10 queries in this set have already been updated with `_old` backups and are using the new `_new` table schema.

---

## Query Status Details

### 1. ✅ `getLastFilledTimesheet`
- **Status:** ✅ **ALREADY UPDATED**
- **Location:** Line 862 in `EmployeeTimesheetsNewRepository.java`
- **Backup:** `getLastFilledTimesheet_old` (line 818)
- **Changes Applied:**
  - ✅ Uses `employee_timesheets_new`
  - ✅ Uses `employee_timesheet_activities_mapping_new`
  - ✅ JOINs with `day_type_master_new`
  - ✅ JOINs with `project_timesheet_status_new`
  - ✅ Uses `total_activities_minutes` calculation
  - ✅ Uses `TIME_FORMAT(SEC_TO_TIME(ets.total_working_minutes * 60), '%H:%i')` for `total_working_hours`
  - ✅ Uses `CAST(etam.duration_minutes AS DECIMAL(10,2))/60` for `completion_time`
  - ✅ Gets `description` from `etam.description`

### 2. ✅ `checkEmployeeActiveOrNot`
- **Status:** ✅ **ALREADY UPDATED**
- **Location:** Line 928 in `EmployeeTimesheetsNewRepository.java`
- **Backup:** `checkEmployeeActiveOrNot_old` (line 898)
- **Changes Applied:**
  - ✅ Uses `employee_timesheets_new`
  - ✅ Uses `employee_timesheet_activities_mapping_new`
  - ✅ JOINs with `day_type_master_new`
  - ✅ Filters by `dtm.day_type = 'Working'`

### 3. ✅ `findByEmpIdAndDateBetween`
- **Status:** ✅ **ALREADY UPDATED**
- **Location:** Line 1028 in `EmployeeTimesheetsNewRepository.java`
- **Backup:** `findByEmpIdAndDateBetween_old` (line 989)
- **Changes Applied:**
  - ✅ Uses `employee_timesheets_new`
  - ✅ Uses `employee_timesheet_activities_mapping_new`
  - ✅ Uses `timesheet_document_details_new`
  - ✅ JOINs with `day_type_master_new` and `status_master_new`
  - ✅ Uses `ROUND(et.total_activities_minutes / 60, 2)` for `total_time`
  - ✅ Uses `GROUP_CONCAT` for activities and descriptions
  - ✅ JOINs with `project_timesheet_status_new`

### 4. ✅ `getTotalVmsFilledCount`
- **Status:** ✅ **ALREADY UPDATED**
- **Location:** Line 1140 in `EmployeeTimesheetsNewRepository.java`
- **Backup:** `getTotalVmsFilledCount_old` (line 1086)
- **Changes Applied:**
  - ✅ Uses `employee_client_side_id_mapping_new` with `active = 1` check
  - ✅ CTE structure updated for new schema

### 5. ✅ `totalIshineFilledCount`
- **Status:** ✅ **ALREADY UPDATED**
- **Location:** Line 1157 in `EmployeeTimesheetsNewRepository.java`
- **Backup:** `totalIshineFilledCount_old` (line 1148)
- **Changes Applied:**
  - ✅ Uses `employee_timesheets_new`
  - ✅ JOINs with `day_type_master_new`
  - ✅ Filters by `dtm.day_type = 'Working'`

### 6. ✅ `totalvmsNotFilled`
- **Status:** ✅ **ALREADY UPDATED**
- **Location:** Line 1282 in `EmployeeTimesheetsNewRepository.java`
- **Backup:** `totalvmsNotFilled_old` (line 1218)
- **Changes Applied:**
  - ✅ Uses `employee_timesheets_new`
  - ✅ Uses `employee_client_side_id_mapping_new` with `active = 1` check
  - ✅ CTE structure updated

### 7. ✅ `totalIshineNotFilledCount`
- **Status:** ✅ **ALREADY UPDATED**
- **Location:** Line 1517 in `EmployeeTimesheetsNewRepository.java`
- **Backup:** `totalIshineNotFilledCount_old` (line 1399)
- **Changes Applied:**
  - ✅ Uses `timesheet_document_details_new`
  - ✅ JOINs with `client_status_master_new`
  - ✅ Complex CTE structure updated for new schema

### 8. ✅ `getPendingTimesheetsByEmpAndTeam`
- **Status:** ✅ **ALREADY UPDATED**
- **Location:** Line 2057 in `EmployeeTimesheetsNewRepository.java`
- **Backup:** `getPendingTimesheetsByEmpAndTeam_old` (line 2034)
- **Changes Applied:**
  - ✅ Uses `employee_timesheets_new`
  - ✅ Uses `employee_timesheet_activities_mapping_new`
  - ✅ JOINs with `day_type_master_new` and `status_master_new`
  - ✅ JOINs with `project_timesheet_status_new`
  - ✅ JOINs with `client_status_master_new`
  - ✅ Uses `employee_client_side_id_mapping_new` with `active = 1` check
  - ✅ Uses `ROUND(et.total_activities_minutes / 60, 2)` for `total_time`
  - ✅ Uses `TIME_FORMAT(SEC_TO_TIME(et.total_working_minutes * 60), '%H:%i')` for `total_working_hours`
  - ✅ Gets `description` from `etam.description`

### 9. ✅ `getMyTimesheetRequests`
- **Status:** ✅ **ALREADY UPDATED**
- **Location:** Line 2116 in `EmployeeTimesheetsNewRepository.java`
- **Backup:** `getMyTimesheetRequests_old` (line 2084)
- **Changes Applied:**
  - ✅ Uses `employee_timesheets_new`
  - ✅ Uses `employee_timesheet_activities_mapping_new`
  - ✅ JOINs with `day_type_master_new` and `status_master_new`
  - ✅ JOINs with `project_timesheet_status_new`
  - ✅ JOINs with `client_status_master_new`
  - ✅ Uses `employee_client_side_id_mapping_new` with `active = 1` check
  - ✅ Uses `ROUND(et.total_activities_minutes / 60, 2)` for `total_time`
  - ✅ Uses `TIME_FORMAT(SEC_TO_TIME(et.total_working_minutes * 60), '%H:%i')` for `total_working_hours`
  - ✅ Gets `description` from `etam.description`
  - ✅ Updated client flag logic to use `ecsm.client_side_id IS NOT NULL`

### 10. ✅ `getMyReportees`
- **Status:** ✅ **NO UPDATE NEEDED**
- **Location:** Line 8525 in `EmployeeTimesheetsNewRepository.java`
- **Type:** JPQL query
- **Reason:** This query only references the `Employee` entity and does not use any timesheet tables. It's already correct and doesn't need updates.

---

## Summary

| Query # | Query Name | Status | Notes |
|---------|------------|--------|-------|
| 1 | `getLastFilledTimesheet` | ✅ Updated | All changes applied |
| 2 | `checkEmployeeActiveOrNot` | ✅ Updated | All changes applied |
| 3 | `findByEmpIdAndDateBetween` | ✅ Updated | All changes applied |
| 4 | `getTotalVmsFilledCount` | ✅ Updated | All changes applied |
| 5 | `totalIshineFilledCount` | ✅ Updated | All changes applied |
| 6 | `totalvmsNotFilled` | ✅ Updated | All changes applied |
| 7 | `totalIshineNotFilledCount` | ✅ Updated | All changes applied |
| 8 | `getPendingTimesheetsByEmpAndTeam` | ✅ Updated | All changes applied |
| 9 | `getMyTimesheetRequests` | ✅ Updated | All changes applied |
| 10 | `getMyReportees` | ✅ No update needed | JPQL - no timesheet tables |

---

## Next Steps

Since all queries in this set are already updated, we should:

1. ✅ **Verify remaining queries** - Check if there are any other simple queries that still need updates
2. ⚠️ **Complete partial updates** - Fix `getEmployeeSummaryReportAllEMP` CTE issue
3. 🔴 **Plan complex queries** - Begin planning for batch processing of Category C queries

---

## Files Verified
- `src/main/java/com/apmosys/employeeportal/repository/EmployeeTimesheetsNewRepository.java`

---

**Conclusion:** All 10 queries in this set are already updated and verified. No changes needed for this set.

