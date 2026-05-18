# getEmployeeViewForClientAttendanceStatus – Caching Plan (Spring Cache + Ehcache)

## 1. API Summary

- **Endpoint:** `POST /getEmployeeViewForClientAttendanceStatus` (in `TimesheetController`).
- **Flow:** `TimesheetController` → `TimesheetService.getEmployeeViewForClientAttendanceStatus(GetEmployeeSummaryOnExportDTO)` → repository calls (e.g. `getEmployeeViewForClientAttendanceStatusNew`, `getEmployeeViewForClientAttendanceStatusRepeated`, `getEmployeeSummaryReportAllEMP`, etc.) → build `List<EmployeeInfoDTO>` and set `totalDistinctEmployees`.
- **Request body:** `GetEmployeeSummaryOnExportDTO` (see §2).
- **Response:** `ServiceResponse` with `serviceResponse` = `List<EmployeeInfoDTO>`, `totalElements` = total distinct employee count, plus status/error fields.

---

## 2. Request DTO: What Affects the Result

All of the following affect either which data is queried or how it is sorted/paginated, so they must be part of the cache key.

### 2.1 GetEmployeeSummaryOnExportDTO (root)

| Field | Type | Used in service | Key? |
|-------|------|------------------|-----|
| projectId | Integer | (not used in this method) | Optional* |
| month | Integer | ✓ | ✓ |
| year | Integer | ✓ | ✓ |
| empId | Long | ✓ | ✓ |
| date | LocalDateTime | (not used in query path) | Optional* |
| allEmp | Boolean | ✓ (branch: allEmp vs client-attendance) | ✓ |
| billableType | List<String> | ✓ | ✓ (normalize order) |
| projectActive | String | (not in this method) | Optional* |
| employeeActive | String | ✓ | ✓ |
| status | String | ✓ | ✓ |
| page | Integer | ✓ (offset = (page-1)*size) | ✓ |
| size | Integer | ✓ (pageSize) | ✓ |
| sortBy | String | ✓ | ✓ |
| sortDirection | String | ✓ | ✓ |
| filters | ColumnFilterDTO | ✓ (all resolved filter strings) | ✓ |
| clientSideFilter | String | ✓ | ✓ |
| deptId | Long | ✓ | ✓ |
| isEmployeeRepeated | Boolean | ✓ (branch) | ✓ |
| multiPOs | String | ✓ | ✓ |

\* Include in key if ever used in this API path to keep key stable and correct.

### 2.2 ColumnFilterDTO (object.getFilters())

These are passed into repository calls; all must be part of the key:

- employmentId, clientSideId (clientsideId), employeeName, billableType, projectName, poNo  
- projectManagerName (or projectManagers – match actual getter used in service)  
- clientName, teamName, department, employmentStatus, projectStatus  

(Any other filter field used in the repository methods for this API should be included.)

### 2.3 Derived value

- **statusCode** – derived from `projectStatus` (Mapped→"1", Removed→"0", Approval Pending→"2"). No need to store statusCode separately if `projectStatus` is in the key.

---

## 3. Cacheability

- **Deterministic:** For the same request DTO (and thus same filters, page, size, sort), the backend returns the same result until timesheet/employee/project data changes.
- **Read-only:** No intended side effects; safe to cache the full `ServiceResponse`.
- **Staleness:** Same as dashboard API – cache should expire or be invalidated when timesheets/approvals change (TTL and/or event-based eviction).

**Conclusion:** Suitable for caching with Spring Cache + Ehcache (same setup as Timesheet Dashboard API).

---

## 4. Scope and Plan (Spring Cache + Ehcache Only)

### 4.1 Where to apply

- **Layer:** `TimesheetService.getEmployeeViewForClientAttendanceStatus(GetEmployeeSummaryOnExportDTO object)`.
- **Annotation:** `@Cacheable(cacheNames = "<cacheName>", keyGenerator = "<keyGenerator>")` so all callers (e.g. `TimesheetController`, `EmployeeTimesheetControllerNew`) share the same cache.

### 4.2 Cache name

- **Suggested:** `employeeViewForClientAttendanceStatus` (new dedicated cache in existing Ehcache config).

### 4.3 Cache key

- **Must include:** month, year, empId, allEmp, billableType (list normalized, e.g. sorted and joined), employeeActive, status, page, size, sortBy, sortDirection, clientSideFilter, deptId, isEmployeeRepeated, multiPOs, and the full **filters** (or every filter field used in the repository calls).
- **Key generator:** Implement a custom `KeyGenerator` (e.g. `EmployeeViewClientAttendanceCacheKeyGenerator`) that:
  - Takes the single argument `GetEmployeeSummaryOnExportDTO` (method has one parameter).
  - Builds a stable string (e.g. concatenation with delimiter, or sorted key-value pairs), with:
    - List fields (e.g. `billableType`) normalized (e.g. sorted then joined).
    - Filters: include all filter fields that are passed to the repository (employmentId, clientsideId, employeeName, billableType, projectName, poNo, projectManagers, clientName, teamName, department, employmentStatus, projectStatus).
  - Handles nulls consistently (e.g. empty string or "null").

### 4.4 What to cache

- **Value:** The full `ServiceResponse` returned by `TimesheetService.getEmployeeViewForClientAttendanceStatus` (includes `serviceResponse` = list of DTOs and `totalElements`). Ensure cached instances are not mutated after storage (or return a defensive copy when reading from cache if needed).

### 4.5 Ehcache configuration (new cache)

- **Cache name:** `employeeViewForClientAttendanceStatus`.
- **Heap:** Either **500 entries** or **e.g. 20–50 MB** – this API returns large payloads (list of employees with project and 31-day timesheet data), so:
  - By entries: e.g. `heap(500, EntryUnit.ENTRIES)` (cap number of request variants).
  - By size: e.g. `heap(30, MemoryUnit.MB)` to limit memory; number of entries then depends on average response size.
- **Expiry:** `expireAfterWrite` **5 minutes** (same rationale as dashboard).
- **Location:** Add this cache in the same `CacheConfig` (or Ehcache XML) used for the Timesheet Dashboard cache.

### 4.6 Eviction / invalidation

- **Phase 1:** TTL only (e.g. 5 minutes).
- **Phase 2 (optional):** On timesheet submit/approval (or other relevant events), evict this cache (e.g. by key pattern or full clear) so users see fresh data without waiting for TTL.

### 4.7 Implementation checklist

- [ ] Add cache `employeeViewForClientAttendanceStatus` in `CacheConfig` (heap + 5 min TTL).
- [ ] Implement `EmployeeViewClientAttendanceCacheKeyGenerator` (single-param key from `GetEmployeeSummaryOnExportDTO` + normalized filters and list fields).
- [ ] Add `@Cacheable(cacheNames = "employeeViewForClientAttendanceStatus", keyGenerator = "employeeViewClientAttendanceCacheKeyGenerator")` on `TimesheetService.getEmployeeViewForClientAttendanceStatus(GetEmployeeSummaryOnExportDTO)`.
- [ ] (Optional) Wire eviction on timesheet/approval events if required.
- [ ] Test: same request → cache hit; different page/size/filters/sort → cache miss.

---

## 5. Memory (Ehcache) – Approximate

| Item | Value / Setting |
|------|------------------|
| Cache name | `employeeViewForClientAttendanceStatus` |
| Heap | 500 entries **or** 20–30 MB |
| Expiry | expireAfterWrite 5 minutes |
| Per entry | Key: ~0.2–0.5 KB (many params). Value: **large** (list of employees × projects × 31 days) – e.g. 50–500 KB per response depending on page size and data. |
| 500 entries (by count) | Roughly 25–250 MB if average value 50–500 KB. Prefer **heap by MB** (e.g. 30 MB) to avoid OOM. |

Recommendation: define this cache with **heap(30, MemoryUnit.MB)** and **expireAfterWrite 5 minutes** so memory is bounded regardless of response size.

---

## 6. Summary

- **API:** `POST /getEmployeeViewForClientAttendanceStatus`; request = `GetEmployeeSummaryOnExportDTO`; response = `ServiceResponse` (list of `EmployeeInfoDTO` + totalElements).
- **Cache:** New Ehcache cache `employeeViewForClientAttendanceStatus` (Spring Cache + existing Ehcache setup; no new cache framework).
- **Key:** Full request: month, year, empId, allEmp, billableType (normalized), employeeActive, status, page, size, sortBy, sortDirection, clientSideFilter, deptId, isEmployeeRepeated, multiPOs, and all filter fields from `object.getFilters()`.
- **Where:** `TimesheetService.getEmployeeViewForClientAttendanceStatus(GetEmployeeSummaryOnExportDTO)` with custom key generator.
- **Memory:** Prefer heap by MB (e.g. 30 MB), TTL 5 minutes; optional event-based invalidation later.
