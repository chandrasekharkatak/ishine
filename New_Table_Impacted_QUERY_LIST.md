# Phase 1 - Task 1: Complete List of JPQL/Native Queries Requiring Changes

## Overview
This document lists all JPQL and Native SQL queries that reference old timesheet tables and need to be migrated to `_new` tables.

---

## REPOSITORY QUERIES

### 1. TimesheetsRepository

#### Native Queries:
1. `getMyReporteesTimesheetRequests`
2. `getMyReporteesApprovedTimesheetRequests2`
3. `countMyReporteesTimesheetRequests`
4. `getMyReporteesApprovedTimesheets`
5. `getLast7DaysTimesheetsByEmpId`
6. `getTimesheetsForHomePageByEmpId`
7. `getTimesheetDataByEmpIdAndDate`
8. `getAllTimesheetData`
9. `getLast9DaysPendingTimesheetReport`
10. `getLast9DaysFilledTimesheetReport`
11. `getAllMyTeamTimesheets`
12. `getAllMyTimesheets`
13. `findTimesheetOnLeaveDate`
14. `getAllEmployeeTimesheetsBetweenDates`
15. `getLastFilledTimesheetByEmp`
16. `getEmployeeByNameAndEmpidForTimesheet`
17. `getInactiveActivitiesByTimesheetId`
18. `getMyTeamsFilledEodCountByManagerId`
19. `getTimesheetFilledByMember`
20. `getAllProjectsByEmpId`
21. `checkIfProjectRequiresClientId`
22. `getActiveProjectsAndClientSideIdByEmpId`
23. `getClientSideIdByProjectIdAndEmpId`
24. `getEmployeesWithClientId`
25. `totalIshineFilledCount`
26. `totalvmsNotFilled`
27. `totalIshineNotFilledCount`
28. `getVmsDocumentApprovalStatusWiseCount`
29. `getEmployeeTimesheetAsCalender`
30. `getEmployeeTimesheetAsCalenderByProjectId`
31. `getRejectedTimesheetIdByEmpAndDateRange`
32. `getMyReporteesTimesheetRequests` (with dateOfJoining)
33. `getMyReporteesApprovedTimesheetRequests2` (with empIds)
34. `getProjectViewForClientAttendanceStatus` (700+ line CTE)
35. `getEmployeeViewForClientAttendanceStatus` (700+ line CTE)
36. `getAllEmployeeDSROfRM`
37. `getEmployeeSummaryOnExport`
38. `getEmployeeSummaryOnExportAccordingToStatus`
39. `getProjectByMonthRangeAndEmpId`
40. `allTimesheetFilledDatesForDateRange`
41. `findDatesByEmpIdAndProjectId`

#### JPQL Queries:
42. `findTimesheetsForRejection`
43. `getTimesheetDTOWithFilters` (3 variations - lines 90, 152, 210)
44. `getTimesheetDatesWithDocuments`

---

### 2. TimesheetActivityMapRepository

#### Native Queries:
45. `activitiesByTimesheetId`
46. `activitiesByTimesheetIdforBiomax`
47. `getTimesheetActivityByTimesheetId`

---

### 3. TimesheetDocumentDetailsRepository

#### JPQL Queries:
48. `findByTimesheetId`
49. `findDocIdByTimesheetId`
50. `findDocIdsByTimesheetId`
51. `getDocsByEmpAndDateRange`
52. `findAllDocIdByTimesheetId`
53. `findDocumentsByEmpIdAndDate`

---

### 4. EmployeeRepository

#### Native Queries:
54. `getTimesheetData`
55. `getActivityData`

---

### 5. ProjectRepository

#### Native Queries:
56. `poProjectTimesheetSync`
57. `getAllEmployeeDSROfRM`

---

## NAMED QUERIES (jpa-named-queries.properties)

### TimesheetActivityMap Named Queries:
58. `TimesheetActivityMap.activitiesByTimesheetId`
59. `TimesheetActivityMap.activitiesByTimesheetIdforBiomax`
60. `TimesheetActivityMap.getTimesheetActivityByTimesheetId`

### Timesheet Named Queries:
61. `Timesheet.getMyReporteesTimesheetRequests`
62. `Timesheet.getMyReporteesApprovedTimesheetRequests2`
63. `Timesheet.getMyReporteesApprovedTimesheets`
64. `Timesheet.getLast7DaysTimesheetsByEmpId`
65. `Timesheet.countMyReporteesTimesheetRequests`
66. `Timesheet.getTimesheetsForHomePageByEmpId`
67. `Timesheet.getAllTimesheetData`
68. `Timesheet.getLast9DaysPendingTimesheetReport`
69. `Timesheet.getLast9DaysFilledTimesheetReport`
70. `Timesheet.getAllMyTeamTimesheets`
71. `Timesheet.getAllMyTimesheets`
72. `Timesheet.getInactiveActivitiesByTimesheetId`
73. `Timesheet.getMyTeamsFilledEodCountByManagerId`
74. `Timesheet.getTimesheetFilledByMember`
75. `Timesheet.getLastFilledTimesheetByEmp`
76. `Timesheet.getEmployeeByNameAndEmpidForTimesheet`
77. `Timesheet.getAllProjectsByEmpId`
78. `Timesheet.checkIfProjectRequiresClientId`
79. `Timesheet.getActiveProjectsAndClientSideIdByEmpId`
80. `Timesheet.getClientSideIdByProjectIdAndEmpId`
81. `Timesheet.getEmployeesWithClientId`
82. `Timesheet.totalIshineFilledCount`
83. `Timesheet.totalvmsNotFilled`
84. `Timesheet.totalIshineNotFilledCount`
85. `Timesheet.getVmsDocumentApprovalStatusWiseCount`
86. `Timesheet.getEmployeeTimesheetAsCalender`
87. `Timesheet.getEmployeeTimesheetAsCalenderByProjectId`
88. `Timesheet.getRejectedTimesheetIdByEmpAndDateRange`
89. `Timesheet.getProjectViewForClientAttendanceStatus`
90. `Timesheet.getEmployeeViewForClientAttendanceStatus`
91. `Timesheet.getAllEmployeeDSROfRM`
92. `Timesheet.getEmployeeSummaryOnExport`
93. `Timesheet.getEmployeeSummaryOnExportAccordingToStatus`
94. `Timesheet.getProjectByMonthRangeAndEmpId`
95. `Timesheet.allTimesheetFilledDatesForDateRange`
96. `Timesheet.findDatesByEmpIdAndProjectId`

### Project Named Queries:
97. `Project.poProjectTimesheetSync`

### Employee Named Queries (Timesheet Related):
98. `Employee.getSegregatedDeptEodDefaulter` (references employee_timesheets)

---

## SUMMARY STATISTICS

- **Total Repository Queries**: 57
- **Total Named Queries**: 41
- **Grand Total**: **98 Queries**

### Breakdown by Type:
- **Native SQL Queries**: ~85
- **JPQL Queries**: ~13

### Breakdown by Repository:
- **TimesheetsRepository**: 44 queries
- **TimesheetActivityMapRepository**: 3 queries
- **TimesheetDocumentDetailsRepository**: 6 queries
- **EmployeeRepository**: 2 queries
- **ProjectRepository**: 2 queries
- **Named Queries (Properties File)**: 41 queries

---

## CRITICAL QUERIES (High Priority)

These queries are most critical and should be migrated first:

1. `getMyReporteesTimesheetRequests` - Used for approval workflow
2. `getAllMyTimesheets` - Core employee timesheet listing
3. `getAllMyTeamTimesheets` - Manager view
4. `getProjectViewForClientAttendanceStatus` - Complex 700+ line CTE
5. `getEmployeeViewForClientAttendanceStatus` - Complex 700+ line CTE
6. `getTimesheetDataByEmpIdAndDate` - Used for validation
7. `activitiesByTimesheetId` - Core activity retrieval
8. `getDocsByEmpAndDateRange` - Document operations
9. `getAllEmployeeTimesheetsBetweenDates` - Reporting
10. `getTimesheetsForHomePageByEmpId` - Dashboard

---

## NOTES

1. Some queries appear in both repository methods and named queries file - these are duplicates using the same query name
2. Queries marked with `#` in properties file are commented out but listed for completeness
3. Some queries may have multiple variations (e.g., with/without parameters) - each variation counts as separate
4. Complex CTE queries (700+ lines) require complete rewrite, not just table name changes
5. All queries referencing `employee_timesheets`, `employee_timesheet_activities_mapping`, or `timesheet_document_details` need updates

---

**Document Version:** 1.0  
**Date:** 2025-01-30  
**Phase:** Phase 1 - Task 1  
**Status:** Complete Query Identification

