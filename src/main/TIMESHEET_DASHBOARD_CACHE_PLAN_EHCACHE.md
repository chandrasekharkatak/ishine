# Timesheet Dashboard API – Spring Cache + Ehcache Plan & Memory

## 1. Objective

Cache the **getTimesheetDashboardCountForEmployee** API response using the **existing Spring Cache + Ehcache** setup (no Caffeine or new cache framework).

---

## 2. Current State

- **API:** `POST /getTimesheetDashboardCountForEmployee` (in `TimesheetController`).
- **Flow:** `TimesheetController` → `TimesheetService` → `TimesheetDashboardService.getTimesheetDashboardCountForEmployee()` → `TimesheetsNewRepository` (DB).
- **Request:** `GetTimesheetDashboardCountForEmployeeDTO`: `month`, `year`, `empId`, `isClientDashboard`, `selectedBillableTypes`, `selectedEmployeeStatus`, `clientSideFilter`, `multiPOs`.
- **Response:** `ServiceResponse` containing `TimesheetDashboardResponseDTO` (summary counts + department-wise map).
- **Existing cache:** Ehcache (3.10.1) + hibernate-ehcache in `pom.xml`. One Spring `@Cacheable(value = "Employee")` on `EmployeeRepository.getAllEmployees()`. No `@EnableCaching` or explicit Spring CacheManager found; Spring may be using a default in-memory cache for `Employee`.

---

## 3. Plan (Spring Cache + Ehcache Only)

### 3.1 Dependencies and enablement

- Add **spring-boot-starter-cache** if not already on the classpath (required for `@EnableCaching` and cache abstraction).
- Use **Ehcache 3** already in the project. For Spring Cache integration with Ehcache 3, use **JSR-107 (javax.cache)**. Add **javax.cache:cache-api** if not present; Ehcache 3’s JSR-107 support is usually provided by the same `org.ehcache:ehcache` dependency.
- Enable caching: add **@EnableCaching** (e.g. on `EmployeeportalApplication` or a dedicated `CacheConfig`).

### 3.2 Cache manager and cache definition

- Add a **CacheManager** bean that backs Spring’s cache abstraction with Ehcache 3:
  - Use **JCache (JSR-107)**: `javax.cache.CacheManager` from `Caching.getCachingProvider("org.ehcache.jsr107.EhcacheCachingProvider").getCacheManager(...)`.
  - Wrap it with Spring’s **JCacheCacheManager** and expose as the Spring `CacheManager` bean.
- Define a **named cache** for this API, e.g. **`timesheetDashboardCountForEmployee`** (or a name that matches the cache name used in `@Cacheable`).
- **Do not** introduce Caffeine or any other cache provider; use only Ehcache for this cache.

### 3.3 Where to apply caching

- Apply **@Cacheable** in the **service layer**: `TimesheetDashboardService.getTimesheetDashboardCountForEmployee(...)`.
- This way all callers (e.g. `TimesheetController`, any other controller or service) share the same cache.

### 3.4 Cache key

- Key must include every input that affects the result:  
  `month`, `year`, `empId`, `isClientDashboard`, `billableTypes`, `employeeActive`, `clientSideFilter`, `multiPOs`.
- **billableTypes** is a `List<String>`: normalize for a stable key (e.g. sort and join, or a key DTO with `equals`/`hashCode`) so that `["A","B"]` and `["B","A"]` map to the same entry.
- Options:
  - **Custom key generator** (e.g. implement `KeyGenerator` and use `@Cacheable(keyGenerator = "timesheetDashboardKeyGenerator")`) that builds a string or object from the 8 parameters (with list normalized).
  - Or a **key DTO** with correct `equals`/`hashCode` and use `@Cacheable(key = "#keyDto")` (method would need to receive a key DTO or build it internally and use SpEL only if possible; a key generator is usually simpler).

### 3.5 Eviction and TTL

- **Time-to-live:** `expireAfterWrite` (e.g. **5 minutes**). Dashboard data can change on timesheet submit/approval; short TTL keeps data reasonably fresh.
- **Max size:** Cap entries (e.g. **500**) to avoid unbounded growth (key space ≈ emp × month × year × filter combinations).
- Configure both in the **Ehcache 3** definition for the cache `timesheetDashboardCountForEmployee` (see memory section below).

### 3.6 Optional: invalidation on events

- Later, if needed: on timesheet submit/approval (or other relevant events), call **Cache.evict(key)** or **Cache.clear()** for the cache (or a key pattern) so the dashboard reflects latest data without waiting for TTL. This can be done from the service that updates timesheets, by injecting the Spring `CacheManager`, resolving the cache `timesheetDashboardCountForEmployee`, and evicting.

### 3.7 What to cache

- Cache the **full** `ServiceResponse` returned by `TimesheetDashboardService.getTimesheetDashboardCountForEmployee` (same reference the controller returns). Ensure the response (and nested DTOs) are not mutated after being put in the cache; otherwise consider returning a defensive copy when reading from cache.

### 3.8 Testing

- Unit/integration: two identical requests (same 8 params) → second hit returns cached value (e.g. verify repository called once).
- Different params (e.g. different month or filter) → cache miss, repository called again.
- Verify key normalization for `billableTypes` (order-independent).

---

## 4. Ehcache Memory Details (for this cache)

### 4.1 Cache name

- **timesheetDashboardCountForEmployee**

### 4.2 Resource pool (heap)

- **Type:** Heap (on-heap only for simplicity and to use existing Ehcache setup).
- **Size:** Either by **entries** or by **MB**.
  - **By entries (recommended):**  
    `heap(500, EntryUnit.ENTRIES)`  
    - Keeps at most 500 dashboard responses (one per distinct key).
  - **By MB (alternative):**  
    `heap(10, MemoryUnit.MB)`  
    - Caps heap usage for this cache at 10 MB; number of entries then depends on average entry size.

### 4.3 Approximate memory per entry

- **Key:** Composite string (e.g. `month|year|empId|...|billableTypes|...`) ≈ **100–300 bytes**.
- **Value:** `ServiceResponse` containing:
  - `TimesheetDashboardResponseDTO`: `TimesheetDashboardCountDTO` (5 integers) + `Map<String, DepartmentWiseStatusDTO>` (4 entries: All, Approved, ClientSidePending, Defaulter). Currently department lists are empty (`initDepartmentBuckets()`), so each value is small.
- **Rough total per entry:** ~**0.5–2 KB** per entry (object headers, references, and typical JVM overhead).
- **500 entries:** ~**250 KB–1 MB** heap for this cache. With 10 MB cap, you could hold thousands of entries if keys vary a lot.

### 4.4 Expiration

- **expireAfterWrite:** 5 minutes (300 seconds) so entries are refreshed periodically and dashboard stays reasonably up to date after timesheet/approval changes.

### 4.5 Ehcache 3 configuration (conceptual)

Configure the cache **timesheetDashboardCountForEmployee** with:

- **Heap:**  
  `ResourcePoolsBuilder.newResourcePoolsBuilder().heap(500, EntryUnit.ENTRIES).build()`  
  or  
  `.heap(10, MemoryUnit.MB).build()`
- **Expiry:**  
  `ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(5))`
- **Key/Value types:**  
  `Object.class, Object.class` (or `String.class, ServiceResponse.class` if you want typed API when wiring JCache).

When using JSR-107 with `JCacheCacheManager`, the same limits (heap size and TTL) are applied in the Ehcache 3 `CacheConfiguration` for this cache name.

### 4.6 Summary table

| Item              | Value / Setting                          |
|-------------------|------------------------------------------|
| Cache name        | `timesheetDashboardCountForEmployee`    |
| Heap size         | 500 entries **or** 10 MB                 |
| Expiry            | expireAfterWrite 5 minutes               |
| Estimated memory  | ~250 KB–1 MB for 500 entries             |
| Key               | Composite of 8 params (list normalized)  |
| Value             | `ServiceResponse` (full API response)   |

---

## 5. No Caffeine / New Framework

- **Do not** add Caffeine or any other new cache implementation.
- Use **only** the existing **Spring Cache abstraction** with **Ehcache** as the backend for this API, as above.

---

## 6. Checklist (implementation)

- [ ] Add `spring-boot-starter-cache` (if missing).
- [ ] Add `javax.cache:cache-api` (if missing for JSR-107).
- [ ] Add `@EnableCaching` (e.g. on main application or `CacheConfig`).
- [ ] Create `CacheConfig` (or equivalent) with:
  - [ ] Ehcache 3–backed `javax.cache.CacheManager` (e.g. via `EhcacheCachingProvider`).
  - [ ] Bean for Spring’s `CacheManager` = `JCacheCacheManager(cacheManager)`.
  - [ ] Cache `timesheetDashboardCountForEmployee` with heap(500, ENTRIES) or heap(10, MB) and expireAfterWrite 5 min.
- [ ] Implement a **key generator** (or key DTO) that includes all 8 parameters with normalized `billableTypes`.
- [ ] Add `@Cacheable(cacheNames = "timesheetDashboardCountForEmployee", keyGenerator = "…")` on `TimesheetDashboardService.getTimesheetDashboardCountForEmployee`.
- [ ] (Optional) Invalidate cache on timesheet/approval events if required.
- [ ] Add tests for cache hit/miss and key normalization.
