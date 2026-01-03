# Phase 1 - Task 2: JPQL Entity Correction

## Summary
Corrected all JPQL queries to use new entities (`EmployeeTimesheetsNew`, `DayTypeMasterNew`, `StatusMasterNew`, `TimesheetDocumentDetailsNew`, `ProjectTimesheetStatusNew`, `EmployeeClientSideIdMappingNew`, `ClientStatusMasterNew`) instead of old entities (`Timesheet`, `DayTypeMaster`, `StatusMaster`, `TimesheetDocumentDetails`, `ProjectTimesheetStatus`, `EmployeeClientSideIdMapping`).

**Date:** 2025-01-30  
**Status:** Completed

---

## Issue Identified

The user correctly identified that JPQL queries were still using the old `Timesheet` entity (mapped to `EmployeeTimesheets` table) instead of the new `EmployeeTimesheetsNew` entity (mapped to `employee_timesheets_new` table).

### Old Entity Structure:
- `@Table(name = "EmployeeTimesheets")` → `Timesheet` entity
- Fields: `dayType` (String), `status` (String), `description` (String), `commonProperty.createdOn`, `commonProperty.updatedOn`, `timesheetStatusUpdatedBy`

### New Entity Structure:
- `@Table(name = "employee_timesheets_new")` → `EmployeeTimesheetsNew` entity
- Fields: `dayTypeId` (Integer), `status` (Integer), `createdOn`, `updatedOn`, `updatedBy`
- No `description` field (now in `EmployeeTimesheetActivitiesMappingNew`)
- No `timesheetStatusUpdatedBy` field (using `updatedBy` instead)

---

## Queries Corrected

### 1. `findTimesheetsForRejection`
**Before:**
```java
@Query("SELECT t FROM Timesheet t WHERE t.empId = :empId AND t.date >= :startDate AND t.date <= :endDate")
List<Timesheet> findTimesheetsForRejection(...)
```

**After:**
```java
@Query("SELECT t FROM EmployeeTimesheetsNew t WHERE t.empId = :empId AND t.date >= :startDate AND t.date <= :endDate")
List<EmployeeTimesheetsNew> findTimesheetsForRejection(...)
```

**Changes:**
- Entity: `Timesheet` → `EmployeeTimesheetsNew`
- Return type: `List<Timesheet>` → `List<EmployeeTimesheetsNew>`

---

### 2. `findAllLeaveTimesheetsWithoutLeaveApplication`
**Before:**
```java
@Query("SELECT new com.apmosys.employeeportal.dto.TimesheetDTO(...)
FROM Timesheet t
JOIN Employee e ON t.empId = e.empId
LEFT JOIN Employee s ON t.timesheetStatusUpdatedBy = s.empId
...
LEFT JOIN DayTypeMaster dtm ON dtm.dayTypeId = t.dayTypeId
...
WHERE dtm.dayType <> 'Week Off'
AND (:status IS NULL OR EXISTS (SELECT 1 FROM StatusMaster sm WHERE sm.statusId = t.status ...))
...
t.commonProperty.createdOn, t.commonProperty.updatedOn, ...")
```

**After:**
```java
@Query("SELECT new com.apmosys.employeeportal.dto.TimesheetDTO(...)
FROM EmployeeTimesheetsNew t
JOIN Employee e ON t.empId = e.empId
LEFT JOIN Employee s ON t.updatedBy = s.empId
...
LEFT JOIN DayTypeMasterNew dtm ON dtm.dayTypeId = t.dayTypeId
LEFT JOIN StatusMasterNew sm ON sm.statusId = t.status
LEFT JOIN EmployeeTimesheetActivitiesMappingNew etam ON etam.id.timesheetId = t.timesheetId
...
WHERE dtm.dayType <> 'Week Off'
AND (:status IS NULL OR LOWER(sm.status) LIKE ...)
...
dtm.dayType, COALESCE(etam.description, ''), sm.status, ...
t.createdOn, t.updatedOn, t.updatedBy, ...")
```

**Changes:**
- Entity: `Timesheet` → `EmployeeTimesheetsNew`
- `DayTypeMaster` → `DayTypeMasterNew`
- `StatusMaster` → `StatusMasterNew` (direct JOIN instead of EXISTS subquery)
- `t.timesheetStatusUpdatedBy` → `t.updatedBy`
- `t.commonProperty.createdOn` → `t.createdOn`
- `t.commonProperty.updatedOn` → `t.updatedOn`
- `t.dayType` → `dtm.dayType` (from JOIN)
- `t.status` → `sm.status` (from JOIN)
- `t.description` → `COALESCE(etam.description, '')` (from `EmployeeTimesheetActivitiesMappingNew`)
- Composite key access: `etam.id.timesheetId` (since `EmployeeTimesheetActivitiesMappingNew` uses `@EmbeddedId`)

---

### 3. `getAllLeaveTimesheetsWithoutLeaveApplicationDepartmentsWise`
**Changes:** Same as #2, with additional filter: `d.deptId IN :deptIds`

---

### 4. `getAllLeaveTimesheetsWithoutLeaveApplicationDepartmentWise`
**Changes:** Same as #2, with additional filter: `d.deptId = :deptId`

---

### 5. `findDatesByEmpIdAndProjectId`
**Before:**
```java
@Query("SELECT et.date FROM Timesheet et
INNER JOIN TimesheetDocumentDetails tdd ON et.timesheetId = tdd.timesheetId
WHERE et.empId = :empId
AND et.projectId = :projectId
AND et.clientSideId IS NOT NULL
AND tdd.active IS TRUE
AND tdd.finalFlag IS TRUE
AND (tdd.rmApprovalStatus = 'Approved' OR tdd.rmApprovalStatus = 'Pending' OR tdd.hrApprovalStatus != 'Rejected')")
```

**After:**
```java
@Query("SELECT et.date FROM EmployeeTimesheetsNew et
INNER JOIN TimesheetDocumentDetailsNew tdd ON et.timesheetId = tdd.timesheetId
WHERE et.empId = :empId
AND EXISTS (SELECT 1 FROM ProjectTimesheetStatusNew pts WHERE pts.id.timesheetId = et.timesheetId AND pts.id.projectId = :projectId)
AND EXISTS (SELECT 1 FROM EmployeeClientSideIdMappingNew ecsm WHERE ecsm.id.empId = et.empId AND ecsm.id.projectId = :projectId AND ecsm.active = 1)
AND tdd.active IS TRUE
AND tdd.finalFlag IS TRUE
AND EXISTS (SELECT 1 FROM ClientStatusMasterNew csm WHERE csm.statusId = tdd.clientApprovalStatusId AND (csm.status = 'Approved' OR csm.status = 'Pending'))
AND NOT EXISTS (SELECT 1 FROM ClientStatusMasterNew csm2 WHERE csm2.statusId = tdd.clientApprovalStatusId AND csm2.status = 'Rejected')")
```

**Changes:**
- Entity: `Timesheet` → `EmployeeTimesheetsNew`
- `TimesheetDocumentDetails` → `TimesheetDocumentDetailsNew`
- `et.projectId` → `EXISTS` subquery with `ProjectTimesheetStatusNew` (composite key: `pts.id.timesheetId`, `pts.id.projectId`)
- `et.clientSideId IS NOT NULL` → `EXISTS` subquery with `EmployeeClientSideIdMappingNew` (composite key: `ecsm.id.empId`, `ecsm.id.projectId`)
- `tdd.rmApprovalStatus`, `tdd.hrApprovalStatus` → `EXISTS` subquery with `ClientStatusMasterNew` checking `tdd.clientApprovalStatusId`

---

### 6. `getRejectedTimesheetIdByEmpAndDateRange`
**Before:**
```java
@Query("SELECT et FROM Timesheet et
LEFT JOIN TimesheetDocumentDetails tdd on tdd.timesheetId = et.timesheetId
WHERE et.empId = :empId
AND et.date BETWEEN :fromDate AND :toDate
AND et.status='Rejected'")
List<Timesheet> getRejectedTimesheetIdByEmpAndDateRange(...)
```

**After:**
```java
@Query("SELECT et FROM EmployeeTimesheetsNew et
LEFT JOIN TimesheetDocumentDetailsNew tdd on tdd.timesheetId = et.timesheetId
WHERE et.empId = :empId
AND et.date BETWEEN :fromDate AND :toDate
AND EXISTS (SELECT 1 FROM StatusMasterNew sm WHERE sm.statusId = et.status AND sm.status = 'Rejected')")
List<EmployeeTimesheetsNew> getRejectedTimesheetIdByEmpAndDateRange(...)
```

**Changes:**
- Entity: `Timesheet` → `EmployeeTimesheetsNew`
- `TimesheetDocumentDetails` → `TimesheetDocumentDetailsNew`
- `et.status='Rejected'` → `EXISTS` subquery with `StatusMasterNew`
- Return type: `List<Timesheet>` → `List<EmployeeTimesheetsNew>`

---

## Key Entity Mappings

### Old → New Entity Mappings:
1. `Timesheet` → `EmployeeTimesheetsNew`
2. `DayTypeMaster` → `DayTypeMasterNew`
3. `StatusMaster` → `StatusMasterNew`
4. `TimesheetDocumentDetails` → `TimesheetDocumentDetailsNew`
5. `ProjectTimesheetStatus` → `ProjectTimesheetStatusNew`
6. `EmployeeClientSideIdMapping` → `EmployeeClientSideIdMappingNew`
7. `ClientStatusMaster` → `ClientStatusMasterNew` (for client approval status)

### Composite Key Access Patterns:
- `ProjectTimesheetStatusNew`: `pts.id.timesheetId`, `pts.id.projectId`
- `EmployeeClientSideIdMappingNew`: `ecsm.id.empId`, `ecsm.id.projectId`, `ecsm.id.clientSideId`
- `EmployeeTimesheetActivitiesMappingNew`: `etam.id.timesheetId`, `etam.id.activityId`, `etam.id.projectId`

### Field Mapping Changes:
- `t.dayType` (String) → `dtm.dayType` (from `DayTypeMasterNew`)
- `t.status` (String) → `sm.status` (from `StatusMasterNew`)
- `t.description` (String) → `etam.description` (from `EmployeeTimesheetActivitiesMappingNew`)
- `t.commonProperty.createdOn` → `t.createdOn`
- `t.commonProperty.updatedOn` → `t.updatedOn`
- `t.timesheetStatusUpdatedBy` → `t.updatedBy`
- `et.projectId` → `EXISTS` subquery with `ProjectTimesheetStatusNew`
- `et.clientSideId` → `EXISTS` subquery with `EmployeeClientSideIdMappingNew`
- `tdd.rmApprovalStatus`, `tdd.hrApprovalStatus` → `EXISTS` subquery with `ClientStatusMasterNew` checking `tdd.clientApprovalStatusId`

---

## Testing Recommendations

1. **Verify Entity Mappings**: Ensure all new entities are properly configured in JPA/Hibernate.

2. **Verify Composite Keys**: Test that composite key access patterns (`id.timesheetId`, `id.projectId`, etc.) work correctly in JPQL.

3. **Verify Master Table JOINs**: Test that JOINs with `DayTypeMasterNew`, `StatusMasterNew`, and `ClientStatusMasterNew` return correct values.

4. **Verify Activity Mapping**: Test that `EmployeeTimesheetActivitiesMappingNew` JOIN returns correct description values.

5. **Verify Status Filtering**: Test that status filtering using `EXISTS` subqueries works correctly.

6. **Verify Return Types**: Ensure that methods returning `List<EmployeeTimesheetsNew>` are compatible with calling code.

---

## Notes

1. **Composite Keys**: All new entities with composite keys use `@EmbeddedId`, so JPQL must access fields via `entity.id.fieldName`.

2. **Master Tables**: Status and day type values are now stored in master tables, so JPQL must JOIN with these tables to get string values.

3. **Activity-Level Data**: Description is now at the activity level (`EmployeeTimesheetActivitiesMappingNew`), not at the timesheet level.

4. **Project-Level Data**: Project-related data is now in `ProjectTimesheetStatusNew`, not directly in `EmployeeTimesheetsNew`.

5. **Client-Side ID**: Client-side ID mapping is now in `EmployeeClientSideIdMappingNew` with composite key.

---

**Document Version:** 1.0  
**Status:** Completed

