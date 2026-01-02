# Complex Queries Tracker (Category C - REWRITE)

## Overview
This document tracks all complex queries that require complete rewrite due to business logic changes. These queries will be processed in a separate batch at the end.

**Total Complex Queries:** 37  
**Status:** All pending - to be processed after simple queries

---

## Critical Complex Queries (Affects Billing/Compliance)

### 🔴 Status Calculation Queries (Highest Risk)

#### 1. `getProjectViewForClientAttendanceStatus`
- **Lines:** 700+
- **Risk Level:** 🔴 CRITICAL (Affects Billing)
- **Location:** `EmployeeTimesheetsNewRepository.java` (around line ~7500+)
- **Issue:** Complete status derivation rewrite needed
- **Dependencies:** Uses multiple CTEs, complex status aggregation
- **Notes:** Similar structure to `getEmployeeViewForClientAttendanceStatus` (which is already updated)

#### 2. `getEmployeeTimesheetAsCalender`
- **Lines:** 300+
- **Risk Level:** 🔴 CRITICAL (Affects Reporting)
- **Location:** `EmployeeTimesheetsNewRepository.java`
- **Issue:** Daily status calculation uses old model - needs activity-level aggregation
- **Dependencies:** Calendar view with daily status
- **Notes:** May need to aggregate multiple activities per day

#### 3. `getEmployeeTimesheetAsCalenderByProjectId`
- **Lines:** 300+
- **Risk Level:** 🔴 CRITICAL (Affects Reporting)
- **Location:** `EmployeeTimesheetsNewRepository.java`
- **Issue:** Same as above + project filtering logic
- **Dependencies:** Calendar view filtered by project
- **Notes:** Similar to `getEmployeeTimesheetAsCalender` but with project filter

#### 4. `getEmployeeTimesheetAsCalenderForAllEmp`
- **Lines:** 300+
- **Risk Level:** 🔴 CRITICAL (Affects Reporting)
- **Location:** `EmployeeTimesheetsNewRepository.java`
- **Issue:** Calendar view for all employees - status calculation rewrite needed
- **Dependencies:** Calendar view for all employees
- **Notes:** Similar structure to `getEmployeeTimesheetAsCalender`

#### 5. `getEmployeeTimesheetAsCalenderByProjectIdForAllEmp`
- **Lines:** 300+
- **Risk Level:** 🔴 CRITICAL (Affects Reporting)
- **Location:** `EmployeeTimesheetsNewRepository.java`
- **Issue:** Calendar view for all employees by project
- **Dependencies:** Calendar view for all employees by project
- **Notes:** Combination of calendar view + project filter + all employees

---

## High-Risk Aggregation Queries

### 🔴 Reporting & Dashboard Queries

#### 6. `getAllEmployeeDSROfRM`
- **Risk Level:** 🔴 HIGH (DSR Calculation)
- **Location:** `EmployeeTimesheetsNewRepository.java`
- **Issue:** Complex JOIN with aggregation - references old tables, needs activity-level aggregation
- **Dependencies:** Used in reporting - needs activity-level aggregation
- **Notes:** DSR (Daily Status Report) calculation needs rewrite

#### 7. `getEmployeeSummaryOnExport`
- **Risk Level:** 🔴 HIGH (Export Format)
- **Location:** `EmployeeTimesheetsNewRepository.java`
- **Issue:** Complex query - export format may need adjustment for new structure
- **Dependencies:** Export functionality
- **Notes:** Export format may need adjustment

#### 8. `getEmployeeSummaryOnExportAccordingToStatus`
- **Risk Level:** 🔴 HIGH (Export with Status)
- **Location:** `EmployeeTimesheetsNewRepository.java`
- **Issue:** Complex query - export with status filtering logic needs rewrite
- **Dependencies:** Export functionality with status filtering
- **Notes:** Status filtering logic needs rewrite

#### 9. `getVmsDocumentApprovalStatusWiseCount`
- **Risk Level:** 🔴 HIGH (Document Approval)
- **Location:** `EmployeeTimesheetsNewRepository.java`
- **Issue:** Document approval count query - document approval logic may need adjustment
- **Dependencies:** Document approval status counts
- **Notes:** May need adjustment for new document structure

---

## Medium-Risk Queries

### 🔴 Lookup & Retrieval Queries

#### 10. `getLastFilledTimesheetByEmp`
- **Risk Level:** 🟡 MEDIUM
- **Location:** `EmployeeTimesheetsNewRepository.java`
- **Issue:** Last timesheet query - may need activity-level logic
- **Dependencies:** May need to handle multiple activities per day
- **Notes:** May need to handle multiple activities per day

#### 11. `getEmployeeByNameAndEmpidForTimesheet`
- **Risk Level:** 🟡 MEDIUM
- **Location:** `EmployeeTimesheetsNewRepository.java`
- **Issue:** Employee lookup - may need timesheet context adjustment
- **Dependencies:** May need to adjust for activity-level context
- **Notes:** May need to adjust for activity-level context

#### 12. `getClientSideIdByProjectId`
- **Risk Level:** 🟡 MEDIUM
- **Location:** `EmployeeTimesheetsNewRepository.java`
- **Issue:** Client side ID lookup - may need adjustment if client side ID moved to activity level
- **Dependencies:** Client side ID mapping
- **Notes:** May need adjustment if client side ID moved to activity level

#### 13. `getClientSideIdByProjectIdAndEmpId`
- **Risk Level:** 🟡 MEDIUM
- **Location:** `EmployeeTimesheetsNewRepository.java`
- **Issue:** Client side ID lookup with employee - may need adjustment
- **Dependencies:** Client side ID mapping with employee
- **Notes:** Similar to above but with employee filter

#### 14. `fetchEmploymentIdByEmpId`
- **Risk Level:** 🟡 MEDIUM
- **Location:** `EmployeeTimesheetsNewRepository.java`
- **Issue:** Employment ID lookup - may need timesheet context adjustment
- **Dependencies:** Employment ID retrieval
- **Notes:** May need timesheet context adjustment

#### 15. `getEmployeesWithClientId`
- **Risk Level:** 🟡 MEDIUM
- **Location:** `EmployeeTimesheetsNewRepository.java`
- **Issue:** Employee list with client ID filter - may need activity-level filtering
- **Dependencies:** Employee list with client ID
- **Notes:** May need activity-level filtering

#### 16. `getTimesheetForEmployee`
- **Risk Level:** 🟡 MEDIUM
- **Location:** `EmployeeTimesheetsNewRepository.java`
- **Issue:** Timesheet retrieval - may need activity grouping
- **Dependencies:** Timesheet retrieval for employee
- **Notes:** May need to group by activity

#### 17. `getDocumentsByEmpAndDate`
- **Risk Level:** 🟡 MEDIUM
- **Location:** `EmployeeTimesheetsNewRepository.java`
- **Issue:** Document retrieval - may need activity context
- **Dependencies:** Document retrieval by employee and date
- **Notes:** May need activity-level document filtering

#### 18. `getEmployeeTimesheetsByProject`
- **Risk Level:** 🟡 MEDIUM
- **Location:** `EmployeeTimesheetsNewRepository.java`
- **Issue:** Timesheet list by project - needs activity aggregation
- **Dependencies:** Timesheet list by project
- **Notes:** **HIGH RISK**: Needs activity-level aggregation

---

## Common Patterns in Complex Queries

### Patterns to Watch For:

1. **Status Calculation**
   - Old: Day-level status in `employee_timesheets.status`
   - New: Project-level status in `project_timesheet_status_new` + activity-level data
   - **Action:** Rewrite status derivation logic

2. **Activity Aggregation**
   - Old: 1 timesheet = 1 project
   - New: 1 timesheet = multiple activities (multiple projects possible)
   - **Action:** Add GROUP BY activity or aggregate by activity

3. **Document Structure**
   - Old: Documents directly linked to timesheet
   - New: Documents in `timesheet_document_details_new` + `final_document_new`
   - **Action:** Update document JOINs and filtering

4. **Day Type & Status Lookups**
   - Old: String values (`day_type`, `status`)
   - New: Integer IDs with master table lookups
   - **Action:** Add JOINs to `day_type_master_new` and `status_master_new`

5. **Time Calculations**
   - Old: `total_time` (float hours)
   - New: `total_activities_minutes` (integer minutes)
   - **Action:** Use `ROUND(et.total_activities_minutes / 60, 2)` for hours

---

## Update Strategy for Complex Queries

### Phase 1: Analysis (Before Updates)
1. ✅ Identify all complex queries (DONE - 37 queries)
2. 📋 Group by similarity (calendar views, summary reports, etc.)
3. 📋 Document common patterns
4. 📋 Create test cases for each query type

### Phase 2: Batch Processing (After Simple Queries)
1. 📋 Process similar queries together (e.g., all calendar views)
2. 📋 Use patterns from already-updated complex queries as reference
3. 📋 Test each query thoroughly before moving to next
4. 📋 Document changes for each query

### Phase 3: Testing & Validation
1. 📋 Unit tests for each query
2. 📋 Integration tests with real data
3. 📋 Performance testing
4. 📋 Stakeholder review for business logic correctness

---

## Reference Queries (Already Updated - Use as Patterns)

These complex queries have been successfully updated and can serve as reference:

1. ✅ `getEmployeeViewForClientAttendanceStatus` - 700+ lines
   - **Pattern:** Multiple CTEs, status aggregation, master table JOINs
   - **Use for:** `getProjectViewForClientAttendanceStatus`

2. ✅ `getTimesheetDashboardCountForEmployee` - 400+ lines
   - **Pattern:** Dashboard aggregation, status calculation
   - **Use for:** Other dashboard queries

3. ✅ `getTimesheetDashboardCountForProject` - 200+ lines
   - **Pattern:** Project-level aggregation
   - **Use for:** Other project-level queries

4. ✅ `getEmployeeSummaryReportClientSideApplicable` - 200+ lines
   - **Pattern:** Summary report with CTEs
   - **Use for:** Other summary report queries

---

## Notes

- **Do NOT update complex queries until all simple queries are done**
- **Group similar queries for batch processing**
- **Use already-updated complex queries as reference patterns**
- **Test thoroughly - these queries affect billing and compliance**
- **Document all changes for future reference**

---

**Last Updated:** 2025-01-30  
**Status:** Tracking - Awaiting completion of simple queries

