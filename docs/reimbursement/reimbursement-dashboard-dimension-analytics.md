# Reimbursement dashboard — analytics (end-to-end)

This document describes the **entire reimbursement analytics dashboard**: every tab’s **charts, graphs, tables, KPIs, and non-chart visuals**, plus how **Client / Department / Project** dimension analytics tie into the same API and filters.

---

## 1. Purpose and scope

| Tab | Question it answers |
|-----|----------------------|
| **Overview** | Matrix-aware KPIs (matrix/legacy tickets, pending matrix vs HOD/HR/Finance, claim-line queues), configured approval paths panel, in-flight workflow chips + matrix funnel, then status/trend/dept/category charts. |
| **Ticket lifecycle** | Drill-down table of tickets and nested claim lines (status, stage, amounts). |
| **Approval analytics** | Pending approvals by level and a simple funnel from submitted to paid/closed. |
| **Rejection analysis** | Rejection KPIs, reasons (with progress bars), rejections by claim type (column chart), rejection log table. |
| **Client-based analysis** | For every client in the **clients** master (and current filters), how much was **raised / paid / pending / rejected** on claim lines? Who are the top clients and top employees by raised amount? |
| **Department-based analysis** | For every department in the **department** master (matched to ticket employee department), same monetary split; top departments and employees; same global KPIs and aging. |
| **Project-based analysis** | For **projects that appear on claim lines** in the filtered set, stacked **project · employee** amounts and top projects / employees (no full-project master line chart). |
| **Employee analytics** | Leaderboard table: per-employee tickets, requested, paid, rejection rate. |
| **Alerts** | Finance / workflow alerts (severity, message, optional ticket link and pending amount). |

All tabs share the **same dashboard API** and **filter bar**; metrics are scoped to **`tickets`** and, where noted, **`claimsForMetrics`** after filters.

---

## 2. Complete inventory: every chart, graph, and widget

### 2.1 Technology

| Technology | Where used |
|-------------|------------|
| **Highcharts** | All hosts use `<div id="..." class="rmb-dash__chart-host">`. Types used: **pie/donut**, **column** (grouped or single), **horizontal bar**, **multi-series line**, **stacked column**. |
| **Bootstrap / HTML** | KPI cards, **approval funnel** progress bars, **rejection reason** progress bars, **tables**, **alerts**, filter toolbar. |

### 2.2 Highcharts map: DOM id → tab → chart type → data

| `activeTab` | DOM id | Highcharts type | UI title | Primary JSON field(s) | Renderer (`*.component.ts`) |
|-------------|--------|-----------------|----------|-------------------------|------------------------------|
| `overview` | `rmbDashChartStatus` | Pie (inner ~62% donut) | Status breakdown | `ticketStatusBreakdown` | `renderOverviewCharts` |
| `overview` | `rmbDashChartTrend` | Column (2 series, grouped) | Monthly trend | `monthlyTrend` (requested + paid by month key) | `renderOverviewCharts` |
| `overview` | `rmbDashChartDept` | Horizontal bar | Department spending | `departmentSpending` (requested by ticket department) | `renderOverviewCharts` |
| `overview` | `rmbDashChartCategory` | Pie (inner ~55% donut) | Claim category distribution | `claimsByExpenditureType` (claim **counts** by type) | `renderOverviewCharts` |
| `rejection` | `rmbDashChartRejectType` | Column (single series, red) | Rejections by claim type | `rejectionsByExpenditureType` | `renderRejectionTypeChart` |
| `clientTrend` | `rmbDashClientEmployeeStack` | **Grouped column** (4 series) | Top 12 active clients + Others | `clientLineChartPack` (trimmed) | `renderInsightFourSeriesGroupedColumn` |
| `clientTrend` | `rmbDashClientTicketDonut` | Pie (inner ~66% donut) | Ticket status distribution | `ticketStatusBreakdown` | `renderTicketStatusDonut` |
| `clientTrend` | `rmbDashClientTopDim` | Vertical column | Top 10 clients by raised amount | `barTotalsByClient.rows` (top 10) | `renderTopDimensionColumnChart` |
| `clientTrend` | `rmbDashClientTopEmp` | Vertical column | Top 10 employees by raised amount | `employeeLeaderboard` (top 10) | `renderTopEmployeesColumnChart` |
| `projectTrend` | `rmbDashProjectEmployeeStack` | **Grouped column** (4 series) | Top 12 active projects + Others | `projectLineChartPack` (trimmed) | `renderInsightFourSeriesGroupedColumn` |
| `projectTrend` | `rmbDashProjectTicketDonut` | Pie donut | Ticket status distribution | `ticketStatusBreakdown` | `renderTicketStatusDonut` |
| `projectTrend` | `rmbDashProjectTopDim` | Vertical column | Top 10 projects by raised amount | `barTotalsByProject.rows` | `renderTopDimensionColumnChart` |
| `projectTrend` | `rmbDashProjectTopEmp` | Vertical column | Top 10 employees by raised amount | `employeeLeaderboard` | `renderTopEmployeesColumnChart` |
| `deptTrend` | `rmbDashDeptInsightEmployeeStack` | **Grouped column** (4 series) | Top 12 active departments + Others | `departmentLineChartPack` (trimmed) | `renderInsightFourSeriesGroupedColumn` |
| `deptTrend` | `rmbDashDeptInsightTicketDonut` | Pie donut | Ticket status distribution | `ticketStatusBreakdown` | `renderTicketStatusDonut` |
| `deptTrend` | `rmbDashDeptInsightTopDim` | Vertical column | Top 10 departments by raised amount | `barTotalsByDepartment.rows` | `renderTopDimensionColumnChart` |
| `deptTrend` | `rmbDashDeptInsightTopEmp` | Vertical column | Top 10 employees by raised amount | `employeeLeaderboard` | `renderTopEmployeesColumnChart` |

### 2.3 Four monetary series (line + stacked column + top dimension bars)

For packs / rows that split **raised / paid / pending / rejected**:

| Series | Meaning |
|--------|---------|
| **Raised** | Sum of claim `amount` for in-scope claim lines. |
| **Paid** | Claim status `PAID`. |
| **Pending** | `PENDING_HOD`, `PENDING_HR`, `PENDING_FINANCE`. |
| **Rejected** | `HOD_REJECTED`, `HR_REJECTED`, `FINANCE_REJECTED`. |

**Top dimension** vertical column charts (`renderTopDimensionColumnChart`) plot **raised (requested)** on the y-axis (₹) with **name labels** on the x-axis for the top 10 rows from `barTotalsByClient` / `barTotalsByProject` / `barTotalsByDepartment`.

**Top employees** horizontal bars use **`employeeLeaderboard[].requested`** (sorted, top N).

### 2.4 Non-chart UI (by tab)

| Tab | Content |
|-----|---------|
| **All** | Filter bar: FY preset, from/to dates, department, employee, client, project, display status, workflow stage, expenditure type, **Reset**. Header: Excel/PDF (disabled), **Refresh**. |
| **Overview** | **16 KPI cards** in 4 rows plus **Configured approval paths** (from `activeApprovalMatrices`) and **In-flight workflow** (stage chips, `pendingMatrixByLevel`, compact `approvalFunnelMatrix`). |
| **Ticket lifecycle** | **Main table** from `ticketRows` with pagination; expand row → **nested claims table**; status/stage badges; link to approvals. **No Highcharts.** |
| **Approval analytics** | **3 KPI cards** (pending HOD, HR, Finance ticket counts). **Funnel:** five Bootstrap **progress** rows from `approvalFunnel` + `funnelSteps`. |
| **Rejection analysis** | **2 KPI cards**; **rejection reasons** = list + red **progress** bars (`rejectionReasonList()` / `rejectionReasonBuckets`); **rejections by claim type** = Highcharts column (above); **rejection detail log** table (`rejectionLog`). |
| **Client / Project / Department** | Each: **6 KPI cards**; main chart; donut; two horizontal bars; **pending aging** = four compact KPI cards (`pendingAgingList()` from `pendingAmountAging` + % of `pipelinePendingAmount`). |
| **Employee analytics** | **Single leaderboard table** (`employeeLeaderboard`): #, employee, dept, tickets, requested ₹, paid ₹, rejection %. **No Highcharts.** |
| **Alerts** | List of Bootstrap **alert** boxes (`alerts`: severity, message, ticketId, optional pending ₹). |

### 2.5 Backend keys for overview & rejection charts (reference)

| JSON key | Used by |
|----------|---------|
| `ticketStatusBreakdown` | Overview status pie; all three insight donuts |
| `monthlyTrend` | Overview monthly column chart |
| `departmentSpending` | Overview department horizontal bar |
| `claimsByExpenditureType` | Overview category pie (counts) |
| `rejectionsByExpenditureType` | Rejection tab column chart |
| `rejectionReasonBuckets` | Rejection tab reason progress bars |
| `rejectionLog` | Rejection tab detail table |

### 2.6 `renderCharts()` branch (Highcharts only)

`reimbursement-dashboard.component.ts` calls `renderCharts()` after each successful load (and when switching tabs). Only the **active** tab’s chart hosts are rendered so hidden containers do not break Highcharts layout.

| `activeTab` | Method |
|-------------|--------|
| `overview` | `renderOverviewCharts()` |
| `rejection` | `renderRejectionTypeChart()` |
| `clientTrend` | `renderInsightTabCharts(data, 'client')` |
| `projectTrend` | `renderInsightTabCharts(data, 'project')` |
| `deptTrend` | `renderInsightTabCharts(data, 'department')` |
| Other values (`lifecycle`, `approval`, `employee`, `alerts`, …) | No Highcharts in this pass |

---

## 3. User entry and routing

- **Angular route:** `reimbursement/reimbursement-dashboard` (see `reimbursement-routing.module.ts`).
- **Component:** `ReimbursementDashboardComponent`  
  - `src/main/frontend/src/app/reimbursement/reimbursement-dashboard/reimbursement-dashboard.component.ts`  
  - Template: `reimbursement-dashboard.component.html`  
  - Styles: `reimbursement-dashboard.component.css`
- **Tabs (internal `activeTab`):** `clientTrend` | `projectTrend` | `deptTrend` (plus Overview, Lifecycle, etc.).

On tab change, `setTab()` runs `renderCharts()` after a short timeout so chart DOM nodes for the visible tab exist before Highcharts initializes.

---

## 4. API (single load for entire dashboard)

| Item | Detail |
|------|----------|
| **HTTP** | `POST /api/fetchReimbursementDashboard` |
| **Controller** | `ReimbursementController.fetchReimbursementDashboard` |
| **Service** | `ReimbursementTicketService.dashboard(ReimbursementDashboardFilterDTO filter)` (`@Transactional(readOnly = true)`) |
| **Frontend** | `ReimbursementService.fetchReimbursementDashboard(filter?)` → `reimbursement-dashboard.component.ts` `loadDashboard()` |

**Request body** (`ReimbursementDashboardFilterDTO` — all optional):

| Field | Type | Effect |
|-------|------|--------|
| `fromDate` / `toDate` | `String` (`yyyy-MM-dd`) | Ticket **submittedOn** must fall in range (end date inclusive through end of day). |
| `department` | `String` | Ticket `department` must equal ignore-case. |
| `employeeEmpId` | `Long` | Ticket `empId` must match. |
| `ticketStatus` | `String` | Matches **display** status from `displayTicketStatus(t)`. |
| `workflowStage` | `String` | Ticket `workflowStage` equals ignore-case. |
| `projectId` | `Long` | **Claim-level:** at least one claim on the ticket must match `projectId` (and only matching claims count in metrics). |
| `clientId` | `Integer` | **Claim-level:** same for `clientId`. |
| `expenditureType` | `String` | **Claim-level:** claim `expenditureType` equals ignore-case. |

**Response:** standard `ServiceResponse` — success payload is `serviceResponse` (the dashboard `Map`), plus `filterOptions` embedded inside that map (see below).

---

## 5. Core backend pipeline (all tabs)

### 5.1 Ticket source

1. `List<ReimbursementTicket> allActive = ticketRepository.findAllActiveWithClaims();`  
   - Active tickets with claims loaded (implementation in repository).

2. **Filtered ticket list**  
   `tickets = allActive.stream().filter(t -> ticketMatchesDashboardFilters(t, filter, fromTs, toTs))`  
   - Date / department / employee / ticket status / workflow filters apply here.  
   - If **any** of `projectId`, `clientId`, or `expenditureType` is set, a ticket is kept only if it has **at least one** claim passing `claimMatchesClaimFilters` (see `hasClaimLevelFilters`).

### 5.2 Claim stream for metrics

For every metric that sums or counts **claim lines**:

```text
claimsForMetrics(ticket, filter) = ticket.getClaims().stream()
    .filter(claim -> claimMatchesClaimFilters(claim, filter))
```

So **project / client / expenditure** filters **subset claims**, not whole tickets, for amounts and counts.

### 5.3 Amount semantics (claim status)

Across **bar totals** and **line chart packs**:

| Bucket | Claim statuses included |
|--------|-------------------------|
| **Raised (requested)** | Sum of **amount** for all in-scope claim lines. |
| **Paid** | `STATUS_PAID` |
| **Pending (pipeline)** | `PENDING_HOD`, `PENDING_HR`, `PENDING_FINANCE` |
| **Rejected** | `HOD_REJECTED`, `HR_REJECTED`, `FINANCE_REJECTED` |

Helpers: `isPipelinePendingClaimStatus`, `isRejectedClaimStatus` in `ReimbursementTicketService`.

### 5.4 Master filter dropdowns (search-select on UI)

The filter bar uses **`app-my-select`** (same component as Apply Reimbursement) for department, employee, project, client, and claim type. Placeholder text prompts search; a clear (×) control resets each master filter to “all”.

| `filterOptions` key | Source |
|---------------------|--------|
| `departments` | `departmentRepository.getAllDeptsList()` — deduped department **names** |
| `employees` | `employeeRepository.findAllActiveEmployeesObject()` — active **`employee`** rows (`employmentstatus != 'InActive'`) as `{ empId, fullName }` |
| `projects` | `projectRepository.findAllProjectIdNameAndClient()` — all **`projects`** rows as `{ projectId, projectName, clientId }`. UI shows **all** projects until a client is selected; then only projects with matching `clientId`. |
| `clients` | `clientsRepository.findAll()` — **`clients`** master as `{ clientId, clientName }` |
| `expenditureTypes` | `expenditureTypeRepository.findAll()` — **`expenditure_type`** names (claim types) |

`ticketStatuses` and `workflowStages` remain plain `<select>` lists built from tickets in the current actor scope.

---

## 6. Client-based analysis

### 6.1 Master list and filter dropdown

- **Source:** `clientsRepository.findAll()` sorted by name.  
- **Filter options:** `filterOptions.clients` — array of `{ clientId, clientName }`.  
- **Display name resolution** (`resolveDashboardClientLabel`):  
  1. `Client.clientName` from entity  
  2. Else `findAllClientIdAndName()` JPQL map (`loadClientNamesFromClientsJpql`)  
  3. Else denormalized **`reimbursement_ticket_claim.client_name`** aggregated from **`allActive`** (`collectClientNamesFromTicketClaims(allActive)`)  
  4. Else `Client #<id>`  

This avoids x-axis / dropdown showing only `Client #1981` when the claim row or JPQL list still carries a readable name (e.g. HDF).

### 6.2 Payloads used on the tab

| Key | Built by | Meaning |
|-----|----------|---------|
| **`clientLineChartPack`** | `buildClientLineChartPack(...)` | **One category per client row** in `clients` table (sorted by resolved label). Parallel arrays: `categories`, `raised`, `paid`, `pending`, `rejected` (longs, rupees). Zeros where no in-scope activity for that client. |
| **`barTotalsByClient`** | `buildClientBarTotalsPack(...)` | Rows only for **client IDs that appear** on in-scope claims: `label`, `clientId`, `requested`, `paid`, `pending`, `rejected`. Sorted by requested desc. Used for **“Top clients by raised amount”** horizontal bar. |

### 6.3 Frontend charts (tab `clientTrend`)

- **Main chart:** `renderInsightFourSeriesGroupedColumn('rmbDashClientEmployeeStack', clientLineChartPack)` — top 12 clients with activity + Others bucket; grouped columns (Raised, Paid, Pending, Rejected).  
- **Donut:** `ticketStatusBreakdown` (global ticket counts for filtered `tickets`).  
- **Top dimension bar:** `barTotalsByClient` (top N by requested).  
- **Top employees bar:** `employeeLeaderboard` (top by requested).  
- **KPI row + aging:** same dashboard totals as overview; aging from `pendingAmountAging` vs `pipelinePendingAmount`.

**Note:** Host element id remains `rmbDashClientEmployeeStack` for historical reasons; content is a **line** chart, not stacked columns.

---

## 7. Department-based analysis

### 7.1 Master list and filter dropdown

- **Source:** `departmentRepository.getAllDeptsList()`, sorted by `Department.name`, deduped by **lower-case** name for the dropdown list.  
- **Filter options:** `filterOptions.departments` — list of **strings** (department names).

### 7.2 Payloads

| Key | Built by | Meaning |
|-----|----------|---------|
| **`departmentLineChartPack`** | `buildDepartmentLineChartPack(...)` | **One point per master department** (name match **ignore-case** to `ticket.getDepartment()`). Same four series as client line chart. **Orphan** ticket department strings (not in master) are **appended** after master rows so amounts are not dropped. |
| **`barTotalsByDepartment`** | `buildDepartmentBarTotalsPack(...)` | Aggregates by **ticket** `department` string (trimmed, or `Unknown`). Rows: `label`, `requested`, `paid`, `pending`, `rejected`. |

### 7.3 Frontend (tab `deptTrend`)

- **Main chart:** `renderInsightFourSeriesGroupedColumn('rmbDashDeptInsightEmployeeStack', departmentLineChartPack)`.  
- **Donut / top dept bar / top employees / aging:** same pattern as client tab, using `barTotalsByDepartment` and shared `ticketStatusBreakdown`, `employeeLeaderboard`, `pendingAmountAging`.

---

## 8. Project-based analysis

### 8.1 Project filter options (dropdown)

- **Source:** `projectRepository.findAllProjectIdAndName()` — all rows in the **`project`** table (`{ projectId, projectName }`), sorted by name.  
- Charts still aggregate only projects that have in-scope claim activity; the filter dropdown is the full project master.

### 8.2 Payloads

| Key | Built by | Meaning |
|-----|----------|---------|
| **`projectEmployeeStack`** | `buildProjectEmployeeStackPack(tickets, filter, maxProjects, maxEmpsPerProject)` | Top projects by **raised** amount; within each, top employees by raised. Categories like `ProjectName · EmployeeName`. Series: `raised`, `paid`, `pending`, `rejected`. |
| **`barTotalsByProject`** | `buildProjectBarTotalsPack(...)` | One row per **project id** on in-scope claims: `label` (from claim `projectName` when present), `projectId`, `requested`, `paid`, `pending`, `rejected`. |

### 8.3 Frontend (tab `projectTrend`)

- **Main chart:** `renderInsightFourSeriesGroupedColumn('rmbDashProjectEmployeeStack', projectLineChartPack)` when line pack is present; legacy `renderStackedEmployeeColumn` only if pack is missing.  
- **Donut / top project bar / top employees / aging:** same shared widgets as other insight tabs.

---

## 9. Shared dashboard fields (all insight tabs)

These are computed once per request and reused:

| Key | Role |
|-----|------|
| `ticketCount`, `claimCount`, `totalSubmittedAmount`, `totalPaidAmount`, `pipelinePendingAmount`, `totalRejectedClaimsAmount`, … | KPI cards |
| `ticketStatusBreakdown` | Donut (map of display status → count) |
| `employeeLeaderboard` | Top employees table / horizontal bar |
| `pendingAmountAging` | Buckets `0-7`, `8-15`, `16-30`, `30+` (days since **ticket** `submittedOn`) for **pipeline-pending claim** rupees |
| `filterOptions` | `departments`, `employees`, `projects`, `clients`, `ticketStatuses`, `workflowStages`, `expenditureTypes` |

---

## 10. Frontend request construction

`reimbursement-dashboard.component.ts` → `buildFilterPayload()`:

- Sends only non-empty fields: `fromDate`, `toDate`, `department`, `ticketStatus`, `workflowStage`, `expenditureType`, `employeeEmpId`, `projectId`, `clientId` (numbers coerced for ids).

`filterOptions` is assigned from `dash.serviceResponse.filterOptions` after a successful load (same response object as the rest of the dashboard).

---

## 11. Chart lifecycle (important)

- `destroyCharts()` runs before each redraw and on `ngOnDestroy`.  
- `renderCharts()` branches on **`activeTab`** (see **§2.6** for the exact map):  
  - Overview / rejection / client / project / department each render **only** the charts for that tab (avoids Highcharts sizing issues on hidden divs).  
- Client, project, and department **main** charts share **`renderInsightFourSeriesGroupedColumn`** (trimmed from line chart packs).  
- Project tab uses **`renderStackedEmployeeColumn`** for the main panel.

---

## 12. Data model touchpoints

| Concept | Table / entity |
|---------|----------------|
| Clients | `clients` → `Client` |
| Departments | `department` → `Department` |
| Claims | `reimbursement_ticket_claim` → `ReimbursementTicketClaim` (`client_id`, `client_name`, `project_id`, `project_name`, `amount`, `claim_status`, …) |
| Tickets | Reimbursement ticket header (`department`, `empId`, `submittedOn`, workflow, …) |

---

## 13. File reference (quick)

| Layer | File |
|-------|------|
| API | `ReimbursementController.java` — `fetchReimbursementDashboard` |
| DTO | `ReimbursementDashboardFilterDTO.java` |
| Logic | `ReimbursementTicketService.java` — `dashboard`, `claimsForMetrics`, `ticketMatchesDashboardFilters`, `buildClient*`, `buildDepartment*`, `buildProject*`, client name helpers |
| Repos | `ClientsRepository`, `DepartmentRepository`, reimbursement ticket repository (`findAllActiveWithClaims`) |
| UI | `reimbursement-dashboard.component.{ts,html,css}` |
| HTTP | `reimbursement.service.ts` — `fetchReimbursementDashboard` |

---

## 14. Troubleshooting

| Symptom | Likely cause |
|---------|----------------|
| Axis shows `Client #id` | `clients.client_name` empty in DB **and** no `client_name` on claims **and** JPQL list empty for that id; fix master data or claim denormalization. |
| Line chart all zeros for a client | Filters exclude all claims for that client; client still listed because it exists in `clients` master. |
| Department line has extra tail categories | Ticket `department` text does not match any master row (typo / legacy); amounts appear under appended orphan category. |
| Project dropdown missing a project | Project never appears on any **active** reimbursement claim in `allActive`; not driven by a separate full project catalog in this dashboard. |

---

## 15. Version note

Behaviour described here matches the implementation in the **ishine-prod** repository: full dashboard in **§2** (charts, KPIs, tables, alerts), dimension tabs (client/department/project), `ServiceResponse` wrapper, and filter DTO as above. If you extend the API (e.g. full project master for filters), update this document and the `filterOptions.projects` builder accordingly.
