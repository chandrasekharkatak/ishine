# Next Set of Queries to Update

## Summary
Based on analysis of `EmployeeTimesheetsNewRepository.java` and `jpa-named-queries.properties`, here are the queries that still need updates.

**Last Updated:** 2025-01-30

---

## ✅ Already Updated (Most Simple Queries)

The following queries have already been updated with `_old` backups:

1. ✅ `getTimesheetDataByEmpIdAndDate`
2. ✅ `getAllTimesheetData`
3. ✅ `getLast9DaysPendingTimesheetReport`
4. ✅ `getLast9DaysFilledTimesheetReport`
5. ✅ `getMyReporteesTimesheetRequests`
6. ✅ `getMyReporteesApprovedTimesheetRequests2`
7. ✅ `countMyReporteesTimesheetRequests`
8. ✅ `getMyReporteesApprovedTimesheets`
9. ✅ `getLast7DaysTimesheetsByEmpId`
10. ✅ `getTimesheetsForHomePageByEmpId`
11. ✅ `getAllMyTeamTimesheets`
12. ✅ `getAllMyTimesheets`
13. ✅ `findTimesheetOnLeaveDate`
14. ✅ `getInactiveActivitiesByTimesheetId`
15. ✅ `getMyTeamsFilledEodCountByManagerId`
16. ✅ `getTimesheetFilledByMember`
17. ✅ `getAllEmployeeTimesheetsBetweenDates`
18. ✅ `getTimesheetsByDepartmentAndDateRange`

---

## 📋 Next Set of Queries to Update (10 queries)

### Set 1: Remaining Simple Named Queries

These queries still need to be checked/updated in `jpa-named-queries.properties`:

1. 📋 `getLastFilledTimesheet`
   - **Type:** Named query
   - **Location:** `jpa-named-queries.properties`
   - **Check:** Verify if updated or needs update
   - **Change:** `employee_timesheets` → `employee_timesheets_new` + `employee_timesheet_activities_mapping_new`

2. 📋 `checkEmployeeActiveOrNot`
   - **Type:** Named query
   - **Location:** `jpa-named-queries.properties`
   - **Check:** Verify if updated or needs update
   - **Change:** `employee_timesheets` → `employee_timesheets_new` + `employee_timesheet_activities_mapping_new`

3. 📋 `findByEmpIdAndDateBetween`
   - **Type:** Named query
   - **Location:** `jpa-named-queries.properties`
   - **Check:** Verify if updated or needs update
   - **Change:** All 3 tables: `employee_timesheets`, `employee_timesheet_activities_mapping`, `timesheet_document_details` → `_new` versions

4. 📋 `getTotalVmsFilledCount`
   - **Type:** Named query (CTE)
   - **Location:** `jpa-named-queries.properties`
   - **Check:** Verify if updated or needs update
   - **Change:** `employee_timesheets` → `employee_timesheets_new` in CTE

5. 📋 `totalIshineFilledCount`
   - **Type:** Named query
   - **Location:** `jpa-named-queries.properties`
   - **Check:** Verify if updated or needs update
   - **Change:** `employee_timesheets` → `employee_timesheets_new`

6. 📋 `totalvmsNotFilled`
   - **Type:** Named query (CTE)
   - **Location:** `jpa-named-queries.properties`
   - **Check:** Verify if updated or needs update
   - **Change:** `employee_timesheets` → `employee_timesheets_new` in CTE + check `employee_client_side_id_mapping_new`

7. 📋 `totalIshineNotFilledCount`
   - **Type:** Named query (Complex CTE)
   - **Location:** `jpa-named-queries.properties`
   - **Check:** Verify if updated or needs update
   - **Change:** Both `employee_timesheets` and `timesheet_document_details` → `_new` versions

8. 📋 `getPendingTimesheetsByEmpAndTeam`
   - **Type:** Named query
   - **Location:** `jpa-named-queries.properties`
   - **Check:** Verify if updated or needs update
   - **Change:** Both `employee_timesheets` and `employee_timesheet_activities_mapping` → `_new` versions + status/team filtering

9. 📋 `getMyTimesheetRequests`
   - **Type:** Named query
   - **Location:** `jpa-named-queries.properties`
   - **Check:** Verify if updated or needs update
   - **Change:** Both `employee_timesheets` and `employee_timesheet_activities_mapping` → `_new` versions + date range filtering

10. 📋 `getMyReportees` (JPQL)
    - **Type:** JPQL query
    - **Location:** `EmployeeTimesheetsNewRepository.java`
    - **Check:** Verify if updated or needs update
    - **Change:** Should work as-is (no timesheet table), but verify entity relationships

---

## ⚠️ Partially Complete Queries

1. ⚠️ `getEmployeeSummaryReportAllEMP`
   - **Status:** Partially complete
   - **Issue:** `Employee_Timesheets_With_Activities` CTE (line ~6742) still uses old tables
   - **Action:** Update CTE to use `employee_timesheets_new` and master table JOINs

---

## 🔴 Complex Queries (Tracked Separately)

All complex queries (Category C) are tracked in `COMPLEX_QUERIES_TRACKER.md` and will be processed at the end.

---

## Action Plan

### Immediate Next Steps:

1. **Check remaining named queries** in `jpa-named-queries.properties`:
   - Search for queries that still reference `employee_timesheets` (without `_new`)
   - Search for queries that still reference `employee_timesheet_activities_mapping` (without `_new`)
   - Search for queries that still reference `timesheet_document_details` (without `_new`)

2. **Update the 10 queries listed above** (if not already updated):
   - Create `OLD` backups
   - Update table names
   - Add master table JOINs where needed
   - Update column references

3. **Complete `getEmployeeSummaryReportAllEMP`**:
   - Fix the `Employee_Timesheets_With_Activities` CTE

4. **Verify all simple queries are complete** before moving to complex queries

---

## Verification Checklist

For each query update, verify:

- [ ] `_old` backup created
- [ ] Table names updated (`employee_timesheets` → `employee_timesheets_new`)
- [ ] Master table JOINs added (`day_type_master_new`, `status_master_new`)
- [ ] Column references updated (`total_time` → `total_activities_minutes` calculation)
- [ ] `description` field now comes from `EmployeeTimesheetActivitiesMappingNew`
- [ ] Status/status_id mapping correct
- [ ] Day type/day_type_id mapping correct
- [ ] Client side ID references use `employee_client_side_id_mapping_new` with `active = 1` check

---

**Next Action:** Check `jpa-named-queries.properties` for remaining queries that need updates.

