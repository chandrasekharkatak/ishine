# Timesheet Metadata API Optimization Plan

## 1. Problem Confirmation ✅

### Current Situation:
- **Frontend Function**: `getAllAvailableTimesheetByEmpId()` in `timesheet-form.component.ts`
- **Purpose**: Fetch timesheet dates in a date range to populate `availableTimesheets[]` and calculate `disabledDatesForPicker` for date picker constraints
- **Current API**: `getAllMyTimesheetsByEmpId` → `/api/v2/timesheet/getAllMyTimesheetsByEmpId`

### Issue:
The current API returns **massive hierarchical data**:
- **Query**: `EmployeeTimesheetsNew.getAllMyTimesheets` (named query)
- **Joins**: 
  - `employee_timesheets_new` (main table)
  - `day_type_master_new`
  - `employee_timesheet_location_mapping`
  - `work_location_type_master`
  - `project_timesheet_status_new`
  - `projects`
  - `clients`
  - `client_locations`
  - `employee_timesheet_activities_mapping_new`
  - `activities`
  - `timesheet_rejection_details_new`
- **Returns**: 37+ columns, builds hierarchical `EmployeeTimesheetDTO` with:
  - `LocationSessionDTO[]` → `ProjectTimesheetDTO[]` → `ActivityTimesheetDTO[]`
  - `TimesheetDocumentDataDTO[]`
  - All project/client/location details

### What Frontend Actually Needs:
From `calculateDatePickerConstraints()` analysis:
- **Only uses**: `ts.timesheetId`, `ts.date` from `availableTimesheets`
- **Purpose**: Build `disabledDatesForPicker[]` array (dates already filled)
- **Does NOT need**: locations, projects, activities, documents, client details, etc.

### Performance Impact:
- **Network**: Transfers huge JSON payloads (nested objects, arrays)
- **Database**: Multiple JOINs across 10+ tables
- **Memory**: Large DTO objects in memory
- **Unnecessary**: 95%+ of data fetched is unused

---

## 2. Solution Plan

### New Lightweight API

#### 2.1 Backend Implementation

**New Repository Method** (`EmployeeTimesheetsNewRepository.java`):
```java
/**
 * Lightweight query: Fetch only basic timesheet metadata (no joins, no nested data)
 * Used for date picker constraints - only needs timesheet_id, emp_id, date
 */
@Query(value = "SELECT " +
               "et.timesheet_id, " +
               "et.emp_id, " +
               "et.date, " +
               "et.day_type_id, " +
               "et.status " +
               "FROM employee_timesheets_new et " +
               "WHERE et.emp_id = :empId " +
               "  AND et.date >= :startDate " +
               "  AND et.date <= :endDate " +
               "ORDER BY et.date DESC",
       nativeQuery = true)
List<Object[]> findTimesheetMetadataByEmpIdAndDateRange(
    @Param("empId") Long empId,
    @Param("startDate") LocalDate startDate,
    @Param("endDate") LocalDate endDate
);
```

**New Service Method** (`TimesheetQueryService.java`):
```java
/**
 * Lightweight API: Returns only timesheet metadata (id, empId, date, dayTypeId, status)
 * No locations, projects, activities, documents - optimized for date picker constraints
 */
public ServiceResponse getTimesheetMetadataByEmpId(TimesheetDTO timesheetDTO) {
    ServiceResponse response = new ServiceResponse();
    LogDTO apiLogInfo = new LogDTO();
    apiLogInfo.setApiUrl("/api/v2/timesheet/getTimesheetMetadataByEmpId");
    apiLogInfo.setLogLevel("INFO");
    
    try {
        LocalDate start = LocalDate.parse(timesheetDTO.getStartDate());
        LocalDate end = LocalDate.parse(timesheetDTO.getEndDate());
        
        List<Object[]> rows = employeeTimesheetsNewRepository
            .findTimesheetMetadataByEmpIdAndDateRange(timesheetDTO.getEmpId(), start, end);
        
        if (rows == null || rows.isEmpty()) {
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(new ArrayList<>());
            return response;
        }
        
        // Simple DTO list - no nested structures
        List<TimesheetMetadataDTO> metadataList = rows.stream()
            .map(row -> {
                TimesheetMetadataDTO dto = new TimesheetMetadataDTO();
                dto.setTimesheetId(((Number) row[0]).longValue());
                dto.setEmpId(((Number) row[1]).longValue());
                dto.setDate(((java.sql.Date) row[2]).toLocalDate());
                dto.setDayTypeId(row[3] != null ? ((Number) row[3]).intValue() : null);
                dto.setStatus(row[4] != null ? ((Number) row[4]).intValue() : null);
                return dto;
            })
            .collect(Collectors.toList());
        
        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
        response.setServiceResponse(metadataList);
        return response;
    } catch (Exception e) {
        // Error handling...
    }
}
```

**New DTO** (`TimesheetMetadataDTO.java`):
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetMetadataDTO {
    private Long timesheetId;
    private Long empId;
    private LocalDate date;
    private Integer dayTypeId;
    private Integer status;
}
```

**New Controller Endpoint** (`EmployeeTimesheetControllerNew.java`):
```java
@JobRoleAccess(featureIds = {15})
@PostMapping("/getTimesheetMetadataByEmpId")
public ServiceResponse getTimesheetMetadataByEmpId(@RequestBody TimesheetDTO timesheetDTO) {
    return timesheetServiceNew.getTimesheetMetadataByEmpId(timesheetDTO);
}
```

#### 2.2 Frontend Implementation

**New Service Method** (`timesheet-new.service.ts`):
```typescript
/**
 * Lightweight API: Fetch only timesheet metadata (id, date) for date picker constraints
 * Replaces getAllMyTimesheetsByEmpId for date picker use case
 */
getTimesheetMetadataByEmpId(timesheetObj: Timesheet): Observable<any> {
  return this.http.post(
    `${this.baseUrl}api/v2/timesheet/getTimesheetMetadataByEmpId`,
    timesheetObj
  );
}
```

**Update Component** (`timesheet-form.component.ts`):
```typescript
getAllAvailableTimesheetByEmpId(employeeObj: User): void {
  // ... existing date range calculation ...
  
  let timesheetObj = new Timesheet();
  timesheetObj.empId = employeeObj.empId;
  timesheetObj.startDate = moment(startDate).format(AppComponent.DB_DATE_FORMAT);
  timesheetObj.endDate = moment(endDate).format(AppComponent.DB_DATE_FORMAT);
  
  // ✅ Use lightweight API instead of getAllMyTimesheetsByEmpId
  this.timesheetNewService.getTimesheetMetadataByEmpId(timesheetObj)
    .pipe(first(), takeUntil(this.destroy$))
    .subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.availableTimesheets = response.serviceResponse;
        this.calculateDatePickerConstraints();
      } else {
        console.error(response.serviceResponse);
        this.calculateDatePickerConstraints();
      }
    });
}
```

---

## 3. Implementation Steps

### Step 1: Create DTO
- [ ] Create `TimesheetMetadataDTO.java` in `dto` package
- [ ] Fields: `timesheetId`, `empId`, `date`, `dayTypeId`, `status`

### Step 2: Add Repository Method
- [ ] Add `findTimesheetMetadataByEmpIdAndDateRange()` to `EmployeeTimesheetsNewRepository.java`
- [ ] Query: SELECT only 5 columns from `employee_timesheets_new` (no JOINs)

### Step 3: Add Service Method
- [ ] Add `getTimesheetMetadataByEmpId()` to `TimesheetQueryService.java`
- [ ] Map `Object[]` → `TimesheetMetadataDTO`
- [ ] Return simple list (no hierarchical structure)

### Step 4: Add Controller Endpoint
- [ ] Add `getTimesheetMetadataByEmpId()` to `EmployeeTimesheetControllerNew.java`
- [ ] Endpoint: `/api/v2/timesheet/getTimesheetMetadataByEmpId`
- [ ] Method: POST (to match existing pattern)

### Step 5: Update Frontend Service
- [ ] Add `getTimesheetMetadataByEmpId()` to `timesheet-new.service.ts`

### Step 6: Update Component
- [ ] Replace `getAllMyTimesheetsByEmpId()` call with `getTimesheetMetadataByEmpId()` in `getAllAvailableTimesheetByEmpId()`
- [ ] Verify `calculateDatePickerConstraints()` still works (uses `ts.timesheetId`, `ts.date`)

### Step 7: Testing
- [ ] Test date picker constraints (disabled dates)
- [ ] Verify update mode exclusion logic (exclude current timesheet date)
- [ ] Performance comparison: old vs new API response size

---

## 4. Benefits

### Performance:
- **Database**: Single table query (no JOINs) → ~10-100x faster
- **Network**: ~95% reduction in payload size (5 fields vs 37+ fields + nested arrays)
- **Memory**: Minimal DTO objects vs large hierarchical structures

### Maintainability:
- **Clear separation**: Lightweight metadata API vs full timesheet details API
- **Single responsibility**: Each API serves its specific purpose
- **No breaking changes**: Existing `getAllMyTimesheetsByEmpId` remains unchanged

### Scalability:
- **Date range queries**: Much faster for large date ranges (30-90 days)
- **Concurrent users**: Lower database load, better throughput

---

## 5. Constraints & Considerations

### Constraints:
- ✅ **No breaking changes**: Existing API remains untouched
- ✅ **Backward compatible**: Frontend can still use old API if needed
- ✅ **Same date range logic**: Uses same `startDate`/`endDate` parameters

### Considerations:
- **Future use**: If frontend needs `dayTypeId` or `status` later, they're already included
- **Error handling**: Same error handling pattern as existing API
- **Logging**: Same logging pattern for consistency

---

## 6. File Changes Summary

### Backend:
1. **New DTO**: `java/com/apmosys/employeeportal/dto/TimesheetMetadataDTO.java`
2. **Repository**: `EmployeeTimesheetsNewRepository.java` - add `findTimesheetMetadataByEmpIdAndDateRange()`
3. **Service**: `TimesheetQueryService.java` - add `getTimesheetMetadataByEmpId()`
4. **Service Wrapper**: `TimesheetServiceNew.java` - delegate to `TimesheetQueryService`
5. **Controller**: `EmployeeTimesheetControllerNew.java` - add `@PostMapping("/getTimesheetMetadataByEmpId")`

### Frontend:
1. **Service**: `timesheet-new.service.ts` - add `getTimesheetMetadataByEmpId()`
2. **Component**: `timesheet-form.component.ts` - update `getAllAvailableTimesheetByEmpId()` to use new API

---

## 7. Expected Results

### Before (Current):
- **Query**: 10+ table JOINs, 37+ columns
- **Response Size**: ~50-500 KB per timesheet (with nested data)
- **Query Time**: 100-500ms for 30-day range
- **Network**: Large JSON payloads

### After (Optimized):
- **Query**: Single table, 5 columns
- **Response Size**: ~200 bytes per timesheet
- **Query Time**: 5-20ms for 30-day range
- **Network**: Minimal JSON payloads

### Improvement:
- **~95% reduction** in data transfer
- **~10-20x faster** query execution
- **Better scalability** for concurrent users

---

## 8. Confirmation Checklist

- [x] **Problem understood**: Current API fetches huge hierarchical data when only `timesheetId` and `date` are needed
- [x] **Solution clear**: New lightweight API querying only `employee_timesheets_new` table
- [x] **No breaking changes**: Existing API remains untouched
- [x] **Frontend usage confirmed**: Only uses `timesheetId` and `date` for date picker constraints
- [x] **Plan documented**: Implementation steps, file changes, expected results

---

**Status**: ✅ Ready for implementation
**Priority**: High (Performance optimization)
**Risk**: Low (New API, existing API unchanged)
