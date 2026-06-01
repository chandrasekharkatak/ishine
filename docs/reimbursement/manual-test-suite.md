# Reimbursement Module — Professional Manual Test Suite (Employee → Approvers → Finance + Config)

This document defines a comprehensive **manual testing suite** for the Reimbursement module: employee claim creation and ticket submission, multi‑level approvals (HOD → HR → optional matrix levels), finance actions, dashboards/exports, and reimbursement configuration (masters + submission settings + approval matrix).

## Legend

### Test Type (what you are testing)

| Code | Name | When to use |
|------|------|-------------|
| **POS** | Positive | Valid inputs / happy path; system should **accept** and proceed |
| **NEG** | Negative | Invalid/missing inputs; system should **reject** with clear error |
| **BND** | Boundary | Min/max length, edge dates, decimals, limits |
| **API** | API (positive) | Valid API payload; server accepts |
| **API-NEG** | API (negative) | Invalid API payload; server must reject |
| **SEC** | Security | Authorization, tampering, injection, data isolation |
| **E2E** | End-to-end | Multi-step journey across roles |
| **REG** | Regression | Must-run before release smoke pack |

### Implementation Result (current product behavior)

| Code | Meaning |
|------|---------|
| **PASS** | Behavior is **implemented and enforced** (UI and/or API) as of this review |
| **FAIL** | **Gap** — missing, inconsistent, UI-only, or bypassable via API |

> A case can be **NEG** (negative test) and still **PASS** (the rejection works). Example: empty amount → rejected = **NEG | PASS**.

> A case can be **POS** and **FAIL** if valid business behavior is not implemented.


### Suite summary (cataloged in this document)

| Metric | Count |
|--------|------:|
| **Total test cases (Sections 3–18)** | **310** |
| Positive (POS) | 123 |
| Negative (NEG) | 118 |
| Boundary (BND) | 19 |
| API positive (API) | 5 |
| API negative (API-NEG) | 21 |
| Security (SEC) | 13 |
| End-to-end (E2E) | 18 |
| Regression (REG) | 11 |
| Implementation **PASS** (works today) | 229 |
| Implementation **FAIL** (known gaps) | 81 |

**SIT/UAT coverage:** **310** cataloged cases (target 250–300). **229** expected to pass in current build; **81** document known gaps to fix or accept as risk.


## 1) Scope & Roles

### 1.1 In-scope functional areas

- Employee: Apply reimbursement, add claim(s) to ticket draft, submit ticket, view ticket status, view rejection reasons/audit history, revoke/cancel legacy requests if present.
- Approvers: Pending tickets view, all assigned tickets view, per-claim approve/reject, bulk approve/reject, view proofs and pre‑approval proofs, view audit history.
- Finance: Mark paid, reject ticket with remarks, view payable amount logic.
- Admin/Config: Expenditure types, travel modes, vehicle types, food allowance types, submission settings, approval matrix.
- Dashboards: Ticket listing, filters, export to excel.

### 1.2 Roles

- **Employee (Submitter)**
- **HOD (Assigned HOD approver)**
- **HR mailbox user** (configured HR email)
- **Matrix approval level approver(s)** (if matrix workflow enabled)
- **Finance mailbox user** (configured finance email)
- **Admin** (managing configuration)

---

## 2) Test Data Setup (Recommended)

Create/identify:

- **Employee A** with assigned HOD and valid email.
- **Employee B** without assigned HOD (or with missing HOD email) for negative tests.
- At least **1 mapped project** allowed for reimbursement for Employee A.
- A manual project option exists (e.g., **Others/POC**) and at least **1 client source** (master client list + reimbursement clients list).
- Expenditure types include at minimum: **Travel**, **Food**, and one non-travel/non-food type (e.g., **Others**).
- Travel modes include **Personal Vehicle** and at least one other mode.
- Vehicle types include at least one value.
- Food allowance types include at least one value.
- Submission window settings configured (normal open), plus a state that queues tickets for next cycle.
- Approval matrix configured for at least one employee (to test matrix workflow), and absent for another employee (to test default HOD→HR→Finance).

---

## 0) How to Read This Suite (SIT / UAT)

### 0.1 Standard columns (Master Catalog tables)

| Column | Meaning |
|--------|---------|
| **Test ID** | Unique identifier |
| **Type** | **POS** Positive · **NEG** Negative · **BND** Boundary · **API** API positive · **API-NEG** API negative · **SEC** Security · **E2E** End-to-end · **REG** Regression |
| **Result** | **PASS** = implemented/enforced today · **FAIL** = gap / not fully enforced |
| **Test Case** | What to execute and expected outcome |

### 0.2 Execution status (filled by QA during run)

Add columns in your test run sheet: **Exec Status** (Not Run / Pass / Fail / Blocked), **Actual Result**, **Defect ID**, **Tester**, **Date**.

### 0.3 Suite size target

This catalog is structured for a full **SIT/UAT** run targeting **~250–300** test cases when you include:
- All rows in Sections **3A–16** (field-level + workflow + config)
- Section **17** (expanded combinations)
- Environment-specific cases (email delivery, browser, performance) marked optional

---

## 3) Employee — Claim Form (Apply Reimbursement) Test Cases

## 3A) Employee — Field-Level Test Suite (Claim Form)

This section is **field-by-field**. Execute these tests while creating a claim and before adding it to the ticket.

### 3A.1 Expenditure Type (`expenditureType`)


| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|
| TC-FLD-ET-001 | NEG | PASS | Expenditure Type not selected → submission blocked (required). |
| TC-FLD-ET-002 | POS | PASS | Select a valid value from configured list (e.g., Travel/Food/Others) → accepted. |
| TC-FLD-ET-003 | API-NEG | PASS | Submit via API with empty/null expenditureType → rejected. |
| TC-FLD-ET-004 | API-NEG | PASS | Submit via API with unknown expenditureType string → rejected. |

### 3A.2 Claim Mode flags (`recurringExpense`, `pocProject`)


| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|
| TC-FLD-CM-001 | POS | PASS | Neither selected → claim can still be created (optional flags). |
| TC-FLD-CM-002 | POS | PASS | Recurring selected → claim saved with recurring indicator. |
| TC-FLD-CM-003 | POS | PASS | POC selected → claim saved with POC indicator. |
| TC-FLD-CM-004 | POS | PASS | Both selected → verify UI behavior (should allow or clearly restrict); ensure correct storage. |

### 3A.3 Project (`projectId`) and Manual Project Names (`othersProjectName`, `pocProjectName`)


| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|
| TC-FLD-PROJ-001 | NEG | PASS | Project not selected → blocked (required). |
| TC-FLD-PROJ-002 | POS | PASS | Select allowed mapped project → accepted. |
| TC-FLD-PROJ-003 | API-NEG | PASS | Select project not allowed for employee (force via API) → rejected. |

Manual project variants:

| TC-FLD-PROJ-010 | NEG | PASS | Project = Others → Others Project Name empty → rejected. |
| TC-FLD-PROJ-011 | BND | PASS | Project = Others → name provided (1–500 chars) → accepted. |
| TC-FLD-PROJ-012 | NEG | PASS | Project = POC → POC name empty → rejected. |
| TC-FLD-PROJ-013 | BND | PASS | Project = POC → name provided (1–500 chars) → accepted. |
| TC-FLD-PROJ-014 | BND | FAIL | Others/POC name > 500 chars → should reject consistently; validate all paths (gap risk). |
| TC-FLD-PROJ-015 | NEG | FAIL | Names with only spaces → should reject (verify trimming behavior end-to-end). |

### 3A.4 Client selection for Manual Project (`clientPickerKey`, `clientId`, `reimbursementClientId`, `prospectiveClientName`)


| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|
When using manual project:

| TC-FLD-CL-001 | NEG | PASS | No client selection and no prospective name → rejected. |
| TC-FLD-CL-002 | POS | PASS | Select master client → accepted. |
| TC-FLD-CL-003 | POS | PASS | Select reimbursement client → accepted. |
| TC-FLD-CL-004 | NEG | PASS | Choose “Prospective New Client” but leave name empty → rejected. |
| TC-FLD-CL-005 | POS | PASS | Prospective client name valid → accepted. |
| TC-FLD-CL-006 | BND | FAIL | Prospective client name max length/charset constraints not clearly enforced server-side. |
| TC-FLD-CL-007 | API-NEG | PASS | Supply invalid clientId via API → rejected. |

### 3A.5 Amount (`amount`)


| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|
| TC-FLD-AMT-001 | NEG | PASS | Amount = null/empty → rejected. |
| TC-FLD-AMT-002 | NEG | PASS | Amount = 0 → rejected. |
| TC-FLD-AMT-003 | NEG | PASS | Amount < 0 → rejected. |
| TC-FLD-AMT-004 | BND | PASS | Amount = 0.01 (or smallest allowed by UI) → accepted. |
| TC-FLD-AMT-005 | POS | PASS | Amount with 2 decimals → accepted. |
| TC-FLD-AMT-006 | BND | FAIL | Excessive decimals (e.g., 123.4567) — expected to normalize/reject; not clearly enforced at API. |
| TC-FLD-AMT-007 | BND | FAIL | Extremely large amount (upper bound) — no explicit max rule enforced. |

### 3A.6 Currency (`currencyType`)


| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|
| TC-FLD-CUR-001 | NEG | FAIL | Currency selection rules unclear (UI/API). If app is INR-only, currency should be fixed and non-editable. |
| TC-FLD-CUR-002 | API | FAIL | Submit with unsupported currency via API — verify behavior. |

### 3A.7 Date Range (`fromDate`, `toDate`)


| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|
Required baseline:

| TC-FLD-DT-001 | NEG | PASS | From Date null → rejected. |
| TC-FLD-DT-002 | NEG | PASS | To Date null → rejected. |
| TC-FLD-DT-003 | NEG | PASS | To < From → rejected. |
| TC-FLD-DT-004 | POS | PASS | Valid From/To (same day) → accepted. |
| TC-FLD-DT-005 | POS | PASS | Valid From/To (multi-day) → accepted. |

Date window behavior:

| TC-FLD-DT-010 | POS | PASS | UI prevents selecting outside allowed window (previous month). |
| TC-FLD-DT-011 | NEG | FAIL | API does not enforce previous-month-only window (bypass possible). |

Format/timezone:

| TC-FLD-DT-020 | API-NEG | FAIL | Invalid date format submitted via API (non-ISO / invalid) — verify API error handling. |
| TC-FLD-DT-021 | BND | FAIL | Timezone boundary: submit around midnight UTC; verify stored date is correct day. |

### 3A.8 Food Allowance Type (`foodAllowanceType`) — Food claims only


| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|
| TC-FLD-FOOD-001 | NEG | PASS | Food claim with Food Allowance Type empty → rejected. |
| TC-FLD-FOOD-002 | POS | PASS | Food claim with valid allowance type selected → accepted. |
| TC-FLD-FOOD-003 | API | FAIL | API does not clearly validate allowance type value against master list → crafted value may be accepted. |

### 3A.9 Travel Mode (`travelMode`) — Travel claims only


| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|
| TC-FLD-TRV-001 | NEG | PASS | Travel claim with travelMode empty → rejected. |
| TC-FLD-TRV-002 | POS | PASS | Travel claim with travelMode selected → accepted. |
| TC-FLD-TRV-003 | NEG | FAIL | travelMode value not clearly validated against master list at API. |

### 3A.10 Vehicle Type (`vehicleType`) and Distance (`distance`) — only when Travel Mode = Personal Vehicle


| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|
| TC-FLD-PV-001 | NEG | PASS | Personal Vehicle selected, vehicleType empty → rejected. |
| TC-FLD-PV-002 | NEG | PASS | Personal Vehicle selected, distance null/0/negative → rejected. |
| TC-FLD-PV-003 | POS | PASS | Personal Vehicle selected, vehicleType set and distance > 0 → accepted. |
| TC-FLD-PV-004 | POS | FAIL | Non-personal travel mode with distance filled → should ignore or clear; not enforced. |
| TC-FLD-PV-005 | NEG | FAIL | API does not clearly validate vehicleType against master list. |

### 3A.11 Purpose (`purpose`)


| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|
| TC-FLD-PUR-001 | NEG | PASS | Empty/whitespace → rejected. |
| TC-FLD-PUR-002 | POS | PASS | Normal text (letters/numbers/spaces) → accepted. |
| TC-FLD-PUR-003 | API-NEG | FAIL | Special characters (e.g., `@#$`) blocked by UI but likely accepted via API. |
| TC-FLD-PUR-004 | BND | FAIL | Very long purpose string (length boundary) — not clearly constrained. |

### 3A.12 Business Justification (`businessJustification`)


| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|
| TC-FLD-BJ-001 | NEG | PASS | Empty/whitespace → rejected. |
| TC-FLD-BJ-002 | POS | PASS | Normal text → accepted. |
| TC-FLD-BJ-003 | BND | FAIL | Very long justification string (length boundary) — not clearly constrained. |

### 3A.13 HOD Pre‑Approval Date (`preApprovalDate`)


| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|
| TC-FLD-HODD-001 | NEG | PASS | Missing preApprovalDate → rejected. |
| TC-FLD-HODD-002 | POS | PASS | Set preApprovalDate earlier than From Date → accepted in UI. |
| TC-FLD-HODD-003 | POS | PASS | UI blocks preApprovalDate >= From Date. |
| TC-FLD-HODD-004 | NEG | FAIL | API does not clearly enforce preApprovalDate < From Date (bypass possible). |

### 3A.14 HOD Pre‑Approval Proofs (`preApprovalDocIds`)


| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|
| TC-FLD-HODP-001 | NEG | PASS | preApprovalDocIds empty → rejected. |
| TC-FLD-HODP-002 | POS | PASS | preApprovalDocIds contains 1+ doc id(s) → accepted. |
| TC-FLD-HODP-003 | NEG | FAIL | Document ownership validation (doc belongs to employee / uploadedBy) not proven. |

### 3A.15 Claim Proofs (`docIds`)

| TC-FLD-DOC-001 | NEG | PASS | docIds empty → rejected. |
| TC-FLD-DOC-002 | POS | PASS | docIds contains 1+ doc id(s) → accepted. |
| TC-FLD-DOC-003 | NEG | FAIL | Upload file type enforcement not proven server-side. |
| TC-FLD-DOC-004 | NEG | FAIL | Upload file size enforcement not proven. |
| TC-FLD-DOC-005 | NEG | FAIL | docId tampering (use someone else’s docId) not proven. |

---

### 3.1 Page load & baseline

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-E-001 | POS | PASS | Open Apply Reimbursement page → project picker loads; expenditure types load; travel/vehicle/food lists load. |
| TC-E-002 | POS | PASS | Date bounds display “previous calendar month only” and pickers enforce min/max in UI. |
| TC-E-003 | POS | PASS | If ticket draft has unsaved claims and user tries to navigate away → confirmation modal appears. |
| TC-E-004 | POS | PASS | If no draft claims exist → navigation away does not show confirmation modal. |

### 3.2 Expenditure type selection

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-E-010 | POS | PASS | Select **Travel** → travel-specific fields become required (travel mode; personal vehicle adds vehicle type + distance). |
| TC-E-011 | POS | PASS | Select non-Travel (e.g., Others) → travel-specific required fields not enforced. |
| TC-E-012 | POS | PASS | Select **Food** → **Food Allowance Type** required. |

### 3.3 Amount validations

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-E-020 | NEG | PASS | Amount = empty/0/negative → blocked with validation. |
| TC-E-021 | NEG | PASS | Amount contains non-numeric chars → blocked with validation. |
| TC-E-022 | POS | PASS | Amount with one decimal (e.g., 123.45) → accepted. |
| TC-E-023 | NEG | FAIL | Very large amount upper limit enforcement (no explicit max check observed) → may accept unrealistic values. |

### 3.4 Date validations (From/To & window)

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-E-030 | NEG | PASS | From Date empty → blocked. |
| TC-E-031 | NEG | PASS | To Date empty → blocked. |
| TC-E-032 | NEG | PASS | To Date < From Date → blocked. |
| TC-E-033 | NEG | PASS | From/To outside previous month window → blocked by UI. |
| TC-E-034 | API-NEG | FAIL | API enforcement for “previous month only” (server does not appear to validate window) → may allow bypass via crafted request. |

### 3.5 Food claim dates behavior

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-E-040 | POS | PASS | Food claim with From/To provided → accepted by API (Fooding date not required). |
| TC-E-041 | NEG | PASS | Food claim missing Food Allowance Type → blocked. |
| TC-E-042 | POS | FAIL | UI still includes legacy Fooding Date anywhere (should not be required) → verify no hidden validation remains. |

### 3.6 Travel claim validations

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-E-050 | NEG | PASS | Travel claim missing Travel Mode → blocked (API also enforces). |
| TC-E-051 | NEG | PASS | Travel mode = Personal Vehicle, missing Vehicle Type → blocked (API enforces). |
| TC-E-052 | NEG | PASS | Travel mode = Personal Vehicle, distance missing/0/negative → blocked (API enforces). |
| TC-E-053 | POS | FAIL | Travel mode ≠ Personal Vehicle but distance populated → no explicit rule to prevent; may store meaningless distance. |

### 3.7 Purpose & Business Justification

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-E-060 | NEG | PASS | Purpose empty → blocked (UI + API). |
| TC-E-061 | NEG | PASS | Business justification empty → blocked (UI + API). |
| TC-E-062 | NEG | FAIL | Purpose “special characters not allowed” enforcement is UI-only; API likely accepts → bypass possible. |
| TC-E-063 | BND | FAIL | Max length enforcement for purpose/justification not clearly enforced at API → may accept extremely long strings. |

### 3.8 HOD pre‑approval

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-E-070 | NEG | PASS | Pre‑approval date missing → blocked (API). |
| TC-E-071 | NEG | PASS | Pre‑approval email proof missing → blocked (API requires preApprovalDocIds). |
| TC-E-072 | POS | PASS | UI enforces pre‑approval date must be strictly before From Date. |
| TC-E-073 | NEG | FAIL | API enforcement for pre‑approval date < From Date not clearly enforced → bypass possible. |
| TC-E-074 | NEG | PASS | If assigned HOD missing from employee profile → ticket submission blocked (UI). |

### 3.9 Supporting documents (claim proofs)

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-E-080 | NEG | PASS | Attempt to add claim without at least one supporting document → blocked (API requires docIds). |
| TC-E-081 | NEG | FAIL | File type restriction (pdf/jpg/png) not clearly enforced server-side → may accept any file type depending on upload service. |
| TC-E-082 | NEG | FAIL | File size limit enforcement not clearly visible → may fail unpredictably. |
| TC-E-083 | NEG | PASS | Remove document from draft → count updates; cannot submit if zero docs remain. |

### 3.10 Project & client selection (allowed projects, Others/POC)

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-E-090 | POS | PASS | Normal mapped project selected → accepted. |
| TC-E-091 | API-NEG | PASS | Project not allowed for employee (not in mapping) → API rejects. |
| TC-E-092 | NEG | PASS | Manual project = Others → project name required; missing name rejected. |
| TC-E-093 | NEG | PASS | Manual project = POC → project name required; missing name rejected. |
| TC-E-094 | NEG | PASS | Manual project → must provide client OR reimbursement client OR prospective client name; missing rejected. |
| TC-E-095 | BND | FAIL | Manual project name length > 500 enforcement exists in some paths; verify consistently enforced for both Others and POC in all endpoints. |
| TC-E-096 | NEG | PASS | Prospective client name empty when selected → rejected by API (requires a client/entry). |
| TC-E-097 | NEG | FAIL | Prospective client name validation (length, allowed chars) unclear → may accept invalid. |

### 3.11 Claim draft management (add/edit/remove)

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-E-100 | POS | PASS | Add claim to ticket draft → claim appears in draft list with correct amount/type/project. |
| TC-E-101 | POS | PASS | Edit draft claim → updates reflect; validations re-run. |
| TC-E-102 | POS | PASS | Remove draft claim → removed; totals recalc. |
| TC-E-103 | NEG | PASS | Submit ticket with zero claims → blocked. |

---

## 4) Employee — Ticket Submission & Post-Submit

### 4.1 Ticket submission API contract

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-T-001 | POS | PASS | Submit ticket with 1 valid claim → success; ticket number/id returned. |
| TC-T-002 | POS | PASS | Submit ticket with multiple claims → success; each claim validated independently. |
| TC-T-003 | API-NEG | PASS | Submit ticket with missing HOD details → API rejects. |
| TC-T-004 | API-NEG | PASS | Submit ticket with invalid expenditure type → API rejects. |
| TC-T-005 | API-NEG | PASS | Submit ticket with missing projectId → API rejects. |
| TC-T-006 | API-NEG | PASS | Submit ticket with invalid client selection for manual project → API rejects. |

### 4.2 Submission window behavior (carried / held)

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-T-010 | POS | PASS | When submission window open → ticket enters active workflow stage (pending approvals). |
| TC-T-011 | POS | PASS | When configured to carry tickets to next cycle → ticket is queued/held; UI shows informational message. |
| TC-T-012 | NEG | PASS | Attempt to approve a held/queued ticket → API blocks action with “cannot be approved until cycle month starts”. |
| TC-T-013 | POS | PASS | Manual release of held tickets when cycle opens → ticket moves into approval workflow. |

### 4.3 Employee visibility after submit

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-T-020 | POS | PASS | Employee can view submitted tickets in “My tickets” list with stage/status. |
| TC-T-021 | NEG | PASS | Rejection reason shown for rejected claims/tickets (via modal info / summary). |
| TC-T-022 | POS | PASS | Audit history modal shows chronological actions. |
| TC-T-023 | NEG | FAIL | Employee editing of a submitted ticket is not defined in ticket workflow; verify no unintended update endpoints allow altering claims post-submit. |

---

## 5) Approver — Ticket Lists (Pending / All Assigned)

### 5.1 Listing views & filters

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-A-001 | POS | PASS | Pending Actions view shows only tickets awaiting current actor’s action. |
| TC-A-002 | NEG | PASS | All Tickets view shows all tickets assigned in workflow for actor (including paid/rejected where applicable). |
| TC-A-003 | POS | PASS | Sort and pagination work without breaking action buttons. |
| TC-A-004 | NEG | PASS | Rejected amount computed and displayed for partial/rejected scenarios. |

### 5.2 Authorization boundary (actor identity)

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-A-010 | POS | PASS | HOD actor can only act if actorEmpId matches ticket’s assigned HOD. |
| TC-A-011 | POS | PASS | HR stage only accessible by HR mailbox email. |
| TC-A-012 | POS | PASS | Finance stage only accessible by finance mailbox email. |
| TC-A-013 | API-NEG | PASS | Unauthorized actor attempts action → API rejects with authorization error. |

---

## 6) Approver — Ticket Modal (Per-Claim Decisions)

### 6.1 Modal content & proofs

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-M-001 | POS | PASS | Open ticket modal shows ticket meta (employee, submitted time, total). |
| TC-M-002 | POS | PASS | Claims table displays: type, project, client, amount, From/To dates, status, purpose, justification, HOD pre-approval, proofs. |
| TC-M-003 | POS | PASS | Proofs modal opens only when docIds exist; shows preview for images/PDFs. |
| TC-M-004 | POS | PASS | HOD pre-approval proofs button appears when preApprovalDocIds exist. |

### 6.2 Decision capture rules (HOD / HR / Matrix Level)

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-M-010 | NEG | PASS | Each pending claim requires exactly one decision in a single submission (approve/reject). |
| TC-M-011 | API-NEG | PASS | Approve decision requires remarks → API rejects if empty. |
| TC-M-012 | API-NEG | PASS | Reject decision requires remarks → API rejects if empty. |
| TC-M-013 | API-NEG | PASS | Provide fewer decisions than pending claims → API rejects (“exactly one per pending claim”). |
| TC-M-014 | API-NEG | PASS | Provide decision with invalid claimId → API rejects. |
| TC-M-015 | API-NEG | PASS | Submit when no pending claims → API rejects (“No pending claims to process”). |

### 6.3 Bulk decisions

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-M-020 | POS | PASS | Approve all (bulk) sets approve=true with same remarks for all pending claims. |
| TC-M-021 | NEG | PASS | Reject all (bulk) sets reject with same remarks for all pending claims. |
| TC-M-022 | NEG | PASS | Bulk dialog required remarks for approve/reject. |

### 6.4 Partial approvals and ticket stage transitions

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-M-030 | NEG | PASS | HOD approves some, rejects some → rejected claims marked rejected; approved claims move to HR pending; ticket stage advances appropriately. |
| TC-M-031 | NEG | PASS | HR approves some, rejects some → approved claims move to finance pending; rejected claims stay rejected; ticket stage advances. |
| TC-M-032 | NEG | PASS | If all claims rejected at any stage → ticket stage becomes REJECTED (terminal). |
| TC-M-033 | POS | PASS | Matrix workflow: level decision routes to next matrix level or to finance after final level. |
| TC-M-034 | NEG | FAIL | Display of “Pending finance” for matrix workflow vs “Approved by HR” for non-matrix may confuse users; ensure UI labels match business rules. |

---

## 7) Finance — Payment / Rejection

### 7.1 Finance action rules

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-F-001 | POS | PASS | Finance can only act when ticket stage = PENDING_FINANCE. |
| TC-F-002 | NEG | PASS | Action must be PAID or REJECTED; invalid action rejected. |
| TC-F-003 | NEG | PASS | Finance PAID requires remarks; missing remarks rejected. |
| TC-F-004 | NEG | PASS | Finance REJECTED requires remarks; missing remarks rejected. |
| TC-F-005 | NEG | PASS | If no claims pending finance → finance action rejected (“No claims pending finance”). |

### 7.2 Outcomes

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-F-010 | POS | PASS | PAID marks all finance-pending claims as PAID and ticket stage = PAID. |
| TC-F-011 | NEG | PASS | REJECTED marks finance-pending claims as FINANCE_REJECTED and ticket stage = REJECTED. |
| TC-F-012 | POS | PASS | After finance action, audit entry created and employee notified (email + UI status updates). |
| TC-F-013 | NEG | FAIL | Partial payment (pay subset of claims) is not supported; ensure product requirement aligns. |

---

## 8) Audit Trail & History

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-H-001 | POS | PASS | Audit log records each stage decision with actor and remarks. |
| TC-H-002 | POS | PASS | Audit modal loads via ticket id and shows actor display name, action, created time. |
| TC-H-003 | NEG | FAIL | Sensitive info exposure: verify audit log is not accessible to unauthorized users by calling API directly (authorization not clearly shown in audit fetch method). |

---

## 9) Dashboard & Export

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

### 9.1 Dashboard filtering

| TC-D-001 | POS | PASS | Dashboard loads counts/rows for tickets. |
| TC-D-002 | NEG | PASS | Date filter from/to sends only non-empty fields. |
| TC-D-003 | NEG | FAIL | Filter validation (fromDate <= toDate; date format) not clearly enforced server-side → may break results or error. |

### 9.2 Excel export

| TC-D-010 | POS | PASS | Export returns excel file with expected columns including from/to dates. |
| TC-D-011 | POS | FAIL | Large export performance / memory handling not verified; may time out on large datasets. |

---

## 10) Configuration (Masters + Submission Settings + Approval Matrix)

### 10.1 Expenditure type master

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-C-001 | POS | PASS | Add new expenditure type → appears in dropdown for employee. |
| TC-C-002 | API-NEG | PASS | Delete expenditure type → cannot submit claim with deleted type (API rejects invalid type). |
| TC-C-003 | NEG | FAIL | Prevent deletion of expenditure type in use (referential integrity) not validated here; may break existing tickets. |

### 10.2 Travel modes & vehicle types

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-C-010 | POS | PASS | Add travel mode → appears in Travel mode dropdown. |
| TC-C-011 | NEG | PASS | Delete travel mode → cannot submit Travel claim with that mode (API enforces travelMode required but may not validate against master list). |
| TC-C-012 | API | FAIL | API does not clearly validate travelMode value against configured list → crafted values may be accepted. |
| TC-C-013 | POS | PASS | Personal Vehicle requires vehicle type and distance. |
| TC-C-014 | API | FAIL | API does not validate vehicle type against list → crafted values may be accepted. |

### 10.3 Food allowance type

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-C-020 | POS | PASS | Add food allowance type → appears in Food dropdown. |
| TC-C-021 | NEG | PASS | Food claim missing allowance type rejected. |
| TC-C-022 | API | FAIL | API may not validate allowance type against master list → crafted values may be accepted. |

### 10.4 Submission settings (window / cycle)

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-C-030 | POS | PASS | Settings page can fetch/save reimbursement submission settings. |
| TC-C-031 | POS | PASS | Submission window status endpoint reflects current policy and UI displays message. |
| TC-C-032 | POS | PASS | Held ticket release job moves held tickets into workflow once cycle opens. |

### 10.5 Approval matrix

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-C-040 | POS | PASS | Resolve approval matrix for employee returns flow summary and level columns for approver screens. |
| TC-C-041 | POS | PASS | Save approval matrix updates workflow routing for future tickets. |
| TC-C-042 | NEG | PASS | In matrix workflow, decisions require remarks for approve/reject. |
| TC-C-043 | NEG | FAIL | Misconfigured matrix (missing approver for a level) handling not clearly validated → tickets may get stuck. |

---

## 11) Security, Data Integrity, and Abuse Scenarios

### 11.1 Authorization & impersonation

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-S-001 | SEC | PASS | Non-assigned HOD cannot approve ticket (server checks). |
| TC-S-002 | SEC | PASS | Non-HR email cannot approve at HR stage. |
| TC-S-003 | SEC | PASS | Non-finance email cannot mark paid/reject. |
| TC-S-004 | SEC | FAIL | Employee bypass UI constraints by directly calling ticket submission API with out-of-window dates → likely accepted (no server window check). |
| TC-S-005 | SEC | FAIL | Purpose special character restriction can be bypassed via API (server does not enforce). |

### 11.2 Data tampering

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-S-010 | SEC | FAIL | Submit ticket with manipulated docIds (documents not owned by employee) — ownership validation not clearly shown; must be tested. |
| TC-S-011 | SEC | FAIL | Approver decision payload tampering (claimId not pending) is checked; but test repeated submissions / replay to ensure idempotency. |

### 11.3 Concurrency / replay

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-S-020 | SEC | PASS | Submit decisions when ticket already moved to another stage → API rejects (“not awaiting this approval stage”). |
| TC-S-021 | SEC | PASS | Finance action when ticket not with finance → rejected. |
| TC-S-022 | SEC | FAIL | Two approvers submitting decisions simultaneously at same stage — last-write-wins risk not clearly controlled; must be validated. |

---

## 12) End-to-End Scenarios (Happy Paths + Negative Journeys)

### 12.1 Happy path

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|
 — Non-matrix workflow

| SCN-1 | E2E | PASS | Employee submits 2 claims (Travel + Food) → HOD approves all with remarks → HR approves all with remarks → Finance marks PAID with remarks → ticket stage PAID; all claim statuses PAID; audit shows full trail. |

### 12.2 Happy path — Matrix workflow

| SCN-2 | E2E | PASS | Employee submits multi-claim ticket under matrix workflow → Level 1 approver approves all with remarks → Level 2 approves → Finance marks PAID → ticket stage PAID. |

### 12.3 Partial rejection with continuation

| SCN-3 | E2E | PASS | Employee submits 3 claims → HOD rejects 1 (remarks) and approves 2 → ticket continues with remaining claims → HR approves 2 → Finance pays 2 → ticket outcome reflects partial rejected amount and payable approved amount. |

### 12.4 Full rejection terminal

| SCN-4 | E2E | PASS | Employee submits 1 claim → HOD rejects with remarks → ticket stage REJECTED; employee sees rejection reason and cannot proceed to finance. |

### 12.5 Held-for-cycle queue

| SCN-5 | E2E | PASS | Employee submits ticket after monthly deadline (carry enabled) → ticket stage HELD_FOR_CYCLE; approver attempts action and gets blocked → once cycle opens, held ticket released and becomes approvable. |

### 12.6 Negative — Missing mandatory proofs

| SCN-6 | E2E | PASS | Employee adds claim but removes all documents → submit ticket → blocked with “supporting document required”. |

### 12.7 Negative — Unauthorized approval

| SCN-7 | E2E | PASS | Different HOD tries to approve ticket not assigned to them → API rejects. |

### 12.8 Negative — Missing remarks on approval/rejection

| SCN-8 | E2E | PASS | Approver clicks approve but submits without remarks → API rejects; UI shows error. |

### 12.9 Negative — UI/API mismatch exploit (date window)

| SCN-9 | E2E | FAIL | Submit via API with From/To dates outside previous month → ticket accepted (expected to be rejected by business rule). |

---

## 13) Regression Checklist (Must Run Before Release)

| R-01 | REG | PASS | Submit ticket with each expenditure type. |
| R-02 | REG | PASS | Travel (Personal Vehicle) required fields. |
| R-03 | REG | PASS | Food requires allowance type; uses From/To dates. |
| R-04 | REG | PASS | HOD/HR decisions require remarks and full-claim coverage. |
| R-05 | REG | PASS | Finance actions require remarks and proper stage. |
| R-06 | REG | PASS | Held-for-cycle cannot be approved until released. |
| R-07 | REG | PASS | Audit history loads and shows all transitions. |
| R-08 | REG | PASS | Proof preview works for PDFs and images. |
| R-09 | REG | FAIL | API enforces date window & pre-approval-before-from-date (needs server validation). |

---

## 14) Summary of Known FAIL Areas (Gaps to Fix)

- **Server does not enforce “previous month only” date window** for claim From/To dates (UI-only enforcement).
- **Server does not clearly enforce pre-approval date < From date** (UI-only enforcement).
- **Purpose special character restriction is UI-only**; API may accept.
- **Master value validation** for travelMode/vehicleType/foodAllowanceType appears required-but-not-validated-against-master (potentially accept arbitrary strings).
- **Document ownership/type/size constraints** are not clearly enforced in the visible validation layer; must be verified and/or implemented.
- **Concurrency** (simultaneous approvals) not clearly guarded; must be tested.

---

## 15) Field-Level — Approver & Finance Inputs

### 15.1 Approver decision fields (HOD/HR/Matrix)

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

| TC-FLD-APR-001 | POS | PASS | Decision must include claimId for each pending claim → enforced. |
| TC-FLD-APR-002 | POS | PASS | `approved` must be true/false (not null) → enforced. |
| TC-FLD-APR-003 | POS | PASS | `remarks` required for approve → enforced. |
| TC-FLD-APR-004 | NEG | PASS | `remarks` required for reject → enforced. |
| TC-FLD-APR-005 | BND | FAIL | Remarks max length / allowed characters not clearly enforced; test very long remarks and HTML/script injection. |

### 15.2 Finance action fields

| TC-FLD-FIN-001 | NEG | PASS | `action` must be PAID or REJECTED → enforced. |
| TC-FLD-FIN-002 | POS | PASS | `remarks` required for PAID → enforced. |
| TC-FLD-FIN-003 | NEG | PASS | `remarks` required for REJECTED → enforced. |
| TC-FLD-FIN-004 | BND | FAIL | Finance remarks max length / encoding not clearly enforced; test long text and XSS strings. |

---

## 16) Field-Level — Reimbursement Configuration (Masters + Settings + Approval Matrix)

This section adds **field-level** coverage for configuration screens/APIs used by the reimbursement module.

### 16.1 Expenditure Type Master (Config)

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

Typical fields:
- Expenditure Type Name
- Description (if available)
- Active flag (if available)

Test cases:

| TC-CFG-ET-001 | POS | PASS | Create expenditure type with valid unique name (letters/spaces) → saved; appears in employee dropdown. |
| TC-CFG-ET-002 | NEG | PASS | Create with empty name → rejected. |
| TC-CFG-ET-003 | NEG | FAIL | Create with duplicate name differing only by case/spacing (e.g., `Travel` vs ` travel `) → expected reject; verify normalization. |
| TC-CFG-ET-004 | BND | FAIL | Create with very long name (boundary) → expected reject; max length not clearly defined. |
| TC-CFG-ET-005 | NEG | FAIL | Create with special characters / HTML (`<script>`) → expected sanitize/reject; not clearly enforced. |
| TC-CFG-ET-006 | NEG | PASS | Delete expenditure type → cannot submit claim using that type (ticket submit rejects invalid type). |
| TC-CFG-ET-007 | POS | FAIL | Delete expenditure type currently used in existing tickets/claims → expected block; referential integrity behavior not confirmed. |

### 16.2 Travel Mode Master (Config)

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

Typical fields:
- Travel Mode Name

Test cases:

| TC-CFG-TM-001 | POS | PASS | Create travel mode with valid unique name → appears in Travel claim dropdown. |
| TC-CFG-TM-002 | NEG | PASS | Empty name → rejected. |
| TC-CFG-TM-003 | NEG | FAIL | Duplicate travel mode name case/space variants → expected reject; verify. |
| TC-CFG-TM-004 | POS | FAIL | Delete travel mode used by existing claims → expected block or graceful handling; not confirmed. |
| TC-CFG-TM-005 | POS | FAIL | API should validate travelMode against configured list during ticket submission; likely not enforced. |

### 16.3 Vehicle Type Master (Config)

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

Typical fields:
- Vehicle Type Name

Test cases:

| TC-CFG-VT-001 | POS | PASS | Create vehicle type with valid unique name → appears when Travel Mode = Personal Vehicle. |
| TC-CFG-VT-002 | NEG | PASS | Empty name → rejected. |
| TC-CFG-VT-003 | NEG | FAIL | Duplicate (case/space variants) → expected reject; verify. |
| TC-CFG-VT-004 | POS | FAIL | Delete vehicle type referenced by claims → expected block or stable display; not confirmed. |
| TC-CFG-VT-005 | NEG | FAIL | API should validate `vehicleType` against list when Personal Vehicle; not clearly enforced. |

### 16.4 Food Allowance Type Master (Config)

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

Typical fields:
- Food Allowance Type Name

Test cases:

| TC-CFG-FT-001 | POS | PASS | Create food allowance type with valid unique name → appears in Food claim dropdown. |
| TC-CFG-FT-002 | NEG | PASS | Empty name → rejected. |
| TC-CFG-FT-003 | NEG | FAIL | Duplicate (case/space variants) → expected reject; verify. |
| TC-CFG-FT-004 | POS | FAIL | Delete type used by existing claims → expected block or stable display; not confirmed. |
| TC-CFG-FT-005 | NEG | FAIL | API should validate `foodAllowanceType` against list; not clearly enforced. |

### 16.5 Submission Settings (Config)

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|

Common settings that materially affect behavior:
- Whether submission is allowed / messaging returned by status endpoint
- Cut-off and “carry to next cycle” behavior
- Processing cycle month rules (held tickets)

Test cases:

| TC-CFG-SS-001 | POS | PASS | Fetch settings → page loads current values. |
| TC-CFG-SS-002 | POS | PASS | Save settings with valid values → persisted and reflected on reload. |
| TC-CFG-SS-003 | POS | PASS | Submission window status endpoint returns message; employee page shows correct banner. |
| TC-CFG-SS-004 | POS | PASS | When carry-to-next-cycle enabled, ticket submission succeeds but stage becomes held/queued. |
| TC-CFG-SS-005 | NEG | PASS | Held tickets cannot be approved; approver action returns blocking message. |
| TC-CFG-SS-006 | POS | PASS | Release held tickets job/manual trigger releases only when cycle is open. |
| TC-CFG-SS-007 | NEG | FAIL | Invalid settings (negative day values, invalid date ranges, etc.) should be rejected; validation rules not clearly specified. |

### 16.6 Approval Matrix (Config)

Typical fields:
- Employee scope / mapping (who the matrix applies to)
- One or more levels
- Approver at each level (empId/email)
- Active/inactive flags (if supported)

Test cases:

| TC-CFG-AM-001 | POS | PASS | Resolve matrix for employee returns flow summary and level columns for UI. |
| TC-CFG-AM-002 | POS | PASS | Create a matrix with N levels and valid approvers → new tickets route through PENDING_LEVEL. |
| TC-CFG-AM-003 | NEG | PASS | Approver at a matrix level can act; unauthorized actor gets “not authorized”. |
| TC-CFG-AM-004 | NEG | FAIL | Save matrix with missing approver at a level → should reject; otherwise tickets may get stuck. |
| TC-CFG-AM-005 | NEG | FAIL | Save matrix with duplicate approver across levels (same person repeated) → expected allow/deny based on business rule; not defined. |
| TC-CFG-AM-006 | POS | FAIL | Deactivate/delete matrix while tickets are mid-workflow → expected defined behavior; not specified. |
| TC-CFG-AM-007 | NEG | FAIL | Email vs empId mismatch for approver (wrong email typed) → should validate; not confirmed. |

---

## 17) Expanded SIT/UAT — Additional Test Cases (Target 250–300)

Use this section to complete a **full SIT/UAT** run. These cases complement Sections 3–16.

| Test ID | Type | Result | Test Case |
|---------|------|--------|-----------|
| TC-EXP-001 | POS | PASS | Submit ticket with exactly **1** claim → ticket created with lineNo 1. |
| TC-EXP-002 | POS | PASS | Submit ticket with **5** claims (mixed types) → all lines validated and stored. |
| TC-EXP-003 | BND | FAIL | Submit ticket with **20+** claims → verify max limit (if any); document behavior. |
| TC-EXP-004 | POS | PASS | Ticket public number format `APM-RMB-YYYYMMDD-####` displayed after submit. |
| TC-EXP-005 | POS | PASS | Employee **View Reimbursements** lists submitted tickets with correct status. |
| TC-EXP-006 | NEG | PASS | Employee without reimbursement menu access cannot open apply URL directly (role guard). |
| TC-EXP-007 | POS | PASS | **Pending Actions** tab shows only tickets where logged-in user is current approver. |
| TC-EXP-008 | POS | PASS | **All Tickets** tab includes paid/rejected tickets for same approver scope. |
| TC-EXP-009 | POS | PASS | Approver opens **Take action** → modal shows **From Date** and **To Date** per claim. |
| TC-EXP-010 | POS | PASS | Approved total in modal updates when toggling per-claim approve/reject. |
| TC-EXP-011 | NEG | PASS | Submit approval with only some claims decided → blocked until all pending decided. |
| TC-EXP-012 | E2E | PASS | HOD rejects all claims → employee notified; ticket **Rejected**; no finance queue. |
| TC-EXP-013 | E2E | PASS | HR rejects all after HOD approved → ticket **Rejected**; finance not involved. |
| TC-EXP-014 | E2E | PASS | Finance **REJECTED** with remarks → employee sees finance reason on ticket. |
| TC-EXP-015 | E2E | PASS | Finance **PAID** with remarks → ticket **Paid**; paid amount reflects approved claims only. |
| TC-EXP-016 | E2E | PASS | Matrix Level 1 approve all → routes to Level 2 (not finance yet). |
| TC-EXP-017 | E2E | PASS | Matrix final level approve all → routes to **Pending Finance**. |
| TC-EXP-018 | E2E | NEG | PASS | Matrix Level 1 reject one claim → claim **LEVEL_REJECTED**; others continue if approved. |
| TC-EXP-019 | POS | PASS | Audit history shows **TICKET_RELEASED_FOR_APPROVAL** after held ticket release. |
| TC-EXP-020 | POS | PASS | Audit history shows **HOD_CLAIM_DECISION** / **HR_CLAIM_DECISION** / **FINANCE_PAID** entries. |
| TC-EXP-021 | NEG | FAIL | Fetch audit log for another employee’s ticket without authorization → must be blocked. |
| TC-EXP-022 | POS | PASS | Upload **PDF** proof → preview in proofs modal. |
| TC-EXP-023 | POS | PASS | Upload **JPG/PNG** proof → preview in proofs modal. |
| TC-EXP-024 | NEG | FAIL | Upload **.exe** or disallowed type → rejected at upload. |
| TC-EXP-025 | BND | FAIL | Upload file **> configured max size** → rejected with clear message. |
| TC-EXP-026 | NEG | FAIL | Upload **0-byte** file → rejected. |
| TC-EXP-027 | POS | PASS | Upload multiple proofs on one claim → all listed; each preview works. |
| TC-EXP-028 | POS | PASS | Upload HOD pre-approval email screenshot/PDF → linked via preApprovalDocIds. |
| TC-EXP-029 | NEG | PASS | Remove all draft claims then submit → blocked (no claims). |
| TC-EXP-030 | POS | PASS | Edit draft claim after add → previous line updated, not duplicated. |
| TC-EXP-031 | POS | PASS | Clear project selection → dependent client fields reset. |
| TC-EXP-032 | NEG | PASS | Prospective client duplicate name warning when name exists in master list. |
| TC-EXP-033 | POS | PASS | Travel **non–Personal Vehicle** → distance/vehicle fields hidden or ignored. |
| TC-EXP-034 | POS | PASS | Food claim: From Date = To Date (single day) → accepted. |
| TC-EXP-035 | BND | PASS | From Date = first day of allowed month; To Date = last day → accepted. |
| TC-EXP-036 | NEG | PASS | From Date = last day, To Date = first day (invalid order) → rejected. |
| TC-EXP-037 | API-NEG | FAIL | API submit with From/To in **current month** → should reject (business rule). |
| TC-EXP-038 | API-NEG | FAIL | API submit with preApprovalDate **on or after** From Date → should reject. |
| TC-EXP-039 | POS | PASS | Dashboard filter by **department** → only matching tickets shown. |
| TC-EXP-040 | POS | PASS | Dashboard filter by **expenditure type** → correct subset. |
| TC-EXP-041 | POS | PASS | Dashboard filter by **ticket status** / **workflow stage** → correct subset. |
| TC-EXP-042 | NEG | FAIL | Dashboard filter fromDate **after** toDate → should error or auto-correct. |
| TC-EXP-043 | POS | PASS | Excel export with active filters → file matches on-screen data. |
| TC-EXP-044 | BND | FAIL | Excel export with **10,000+** rows → performance acceptable / no timeout. |
| TC-EXP-045 | POS | PASS | Config: edit expenditure type description → saved and visible where applicable. |
| TC-EXP-046 | POS | PASS | Config: deactivate (if supported) expenditure type → hidden from employee dropdown. |
| TC-EXP-047 | NEG | PASS | Config: save travel mode with leading/trailing spaces → trimmed or rejected. |
| TC-EXP-048 | SEC | FAIL | XSS in purpose field displayed in approver modal → must be escaped/sanitized. |
| TC-EXP-049 | SEC | FAIL | SQL/script in remarks stored and shown in audit → must be safe. |
| TC-EXP-050 | SEC | PASS | Session timeout during ticket draft → re-login; draft behavior documented. |
| TC-EXP-051 | E2E | FAIL | Email to next approver on submit → verify TO/CC content (manual mailbox check). |
| TC-EXP-052 | E2E | FAIL | Email to employee on rejection/paid → verify content includes ticket ref and remarks. |
| TC-EXP-053 | POS | PASS | Pagination on approval list: page 2 loads without losing filters. |
| TC-EXP-054 | POS | PASS | Sort by **Submitted** date ascending/descending → order correct. |
| TC-EXP-055 | POS | PASS | Sort by **Total amt applied** → numeric order correct. |
| TC-EXP-056 | NEG | PASS | Finance marks PAID with empty remarks → API rejects. |
| TC-EXP-057 | NEG | PASS | Finance action **PAID** when ticket still at HOD stage → rejected. |
| TC-EXP-058 | POS | PASS | Partial rejection tooltip on approval list shows claim line, amount, reason. |
| TC-EXP-059 | REG | PASS | Smoke: one full ticket Food-only through to Finance PAID. |
| TC-EXP-060 | REG | PASS | Smoke: one full ticket Travel (Personal Vehicle) through to Finance PAID. |

---

## 18) Quick Reference — Type × Result Matrix

|  | **PASS** (works) | **FAIL** (gap) |
|--|------------------|----------------|
| **POS** (positive) | Valid flow works | Expected feature missing |
| **NEG** (negative) | Invalid input rejected | Invalid input incorrectly accepted |
| **BND** (boundary) | Edge value handled | Edge case breaks |
| **API / API-NEG** | Server contract correct | Bypass / weak validation |
| **SEC** | Access control OK | Security hole |
| **E2E / REG** | Journey complete | Broken workflow |

**How to execute:** Run all **REG** and **E2E** first, then all **NEG** + **API-NEG**, then **POS**, then **BND** and **FAIL** rows (retest after fixes).


