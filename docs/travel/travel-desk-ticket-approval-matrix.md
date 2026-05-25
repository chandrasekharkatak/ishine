# Travel Desk — ticket + approval matrix (target design)

## Current state (legacy)

- Table **`travel_desk`**: one row = one travel/hotel request.
- Approval: fixed **Level 1 → Level 2** (manager / HOD style) in `TravelDeskService`.
- No public ticket number, no configurable matrix, no admin fulfillment/proof step.
- UI: **Apply Travel Request** (`my-travelrequest`) saves into legacy flow.

## Target state (aligned with Reimbursement)

| Reimbursement | Travel Desk |
|---------------|-------------|
| `reimbursement_ticket` | `travel_desk_ticket` |
| `reimbursement_ticket_claim` (many claims) | `travel_desk_ticket_line` (many requests per submit) |
| `APM-RMB-YYYYMMDD-####` | `APM-TRV-YYYYMMDD-####` |
| `reimbursement_approval_matrix` | `travel_approval_matrix` |
| Matrix levels → Finance (paid) | Matrix levels → **Travel Admin** (booking + proof) |
| `ReimbursementTicketService` + emails | `TravelDeskTicketService` + emails (same template style) |

## Workflow stages

```
SUBMIT (employee, N lines in one ticket)
  → PENDING_LEVEL (level 1..N from matrix)
  → … advance per level on approve …
  → PENDING_ADMIN (Travel Desk / admin team)
  → COMPLETED (admin entered booking refs + uploaded proof)
  → or REJECTED (at any level or admin)
```

### Line-level status (`travel_desk_ticket_line.line_status`)

- `PENDING_APPROVAL` — awaiting current matrix level
- `LEVEL_REJECTED` — rejected at a matrix level
- `PENDING_ADMIN` — all lines approved through matrix; waiting for admin booking
- `FULFILLED` — admin attached ticket/booking proof for this line
- `ADMIN_REJECTED` — admin could not book / cancelled

### Ticket-level (`workflow_stage`)

- `PENDING_LEVEL` — in matrix approval
- `PENDING_ADMIN` — all lines past matrix; admin queue
- `COMPLETED` — admin finished (all lines fulfilled or mixed terminal)
- `REJECTED` — closed rejected

## Approval matrix configuration

- New config screen (clone of **Reimbursement Rule Set / Approval Matrix**):
  - Path suggestion: `/configuration/travel-approval-matrix`
  - Same routing modes: `REPORTING_MANAGER`, `HOD_SUBMITTER_DEPT`, `SPECIFIC_IN_SCOPE`, `POOL_ANY_IN_SCOPE`
  - Applicability: departments + job roles (tables `travel_approval_matrix_app_dept`, `_app_role`)
- **Final step is not Finance** — after last matrix level, assign to **Travel Admin** pool (config: `admin_dept_id` / `admin_assignee_emp_id` on matrix header, or global property `travel.workflow.admin.mail`).

## Admin fulfillment (final step)

Admin team (Total Travel Request / new **Fulfill Travel** tab):

- View tickets in `PENDING_ADMIN`
- Per line or per ticket:
  - Booking reference (PNR, hotel confirmation, etc.)
  - Actual travel dates/times if different
  - **Proof documents** (`proof_doc_ids` comma-separated file ids, same pattern as reimbursement `doc_ids`)
- On submit → line `FULFILLED`, ticket `COMPLETED` when all lines terminal
- Email to **employee** (TO) with summary + proof notice; optional CC travel admin distro

## Emails (same rules as reimbursement)

On each transition:

- **TO**: next approver(s) from matrix (`notifyEmailsForCurrentLevelAll`)
- **CC**: employee who raised the ticket
- Header: `iShine Travel` | right: `Action Required`
- Body: ticket summary (Ticket ID, Status, Employee name only) + line details table
- No approval-path table in email
- Amounts: use `Rs.` not `₹` for client compatibility

Triggers:

1. Submit ticket → level 1 approver(s)
2. Each matrix approval → next level or `PENDING_ADMIN`
3. Enter admin queue → travel admin mailbox / assignees
4. Admin completes → employee status email

## API surface (planned)

| API | Purpose |
|-----|---------|
| `POST /api/saveTravelDeskTicket` | Submit ticket + lines |
| `POST /api/fetchMyTravelDeskTickets` | Employee list |
| `POST /api/fetchTravelDeskTicketsForApproval` | Approver queue |
| `POST /api/processTravelDeskTicketApproval` | Matrix level decisions |
| `POST /api/processTravelDeskTicketAdminFulfillment` | Admin booking + proof |
| `GET/POST travel approval matrix CRUD` | Config (mirror reimbursement) |

## Frontend (planned)

| Tab | Change |
|-----|--------|
| Apply Travel Request | Multi-line cart → single **Submit ticket** (like Apply Reimbursement) |
| Approve Travel Request | Matrix levels + per-line approve/reject |
| Total Travel Request | Admin fulfillment + proof upload |
| Configuration | **Travel Approval Matrix** screen |

## Database scripts

Run on UAT/prod (in order):

1. `docs/travel/uat-sql/001_travel_desk_ticket_and_matrix.sql` — creates new tables only; legacy `travel_desk` unchanged for migration period.

## Migration strategy

1. Deploy new tables + backend APIs behind feature flag.
2. New submissions use **ticket** model only.
3. Optional later: read-only view of legacy `travel_desk` rows in View Travel Request.

## Implementation phases

| Phase | Deliverable |
|-------|-------------|
| **1** | SQL + JPA entities + repositories |
| **2** | `TravelApprovalMatrixService` (copy/adapt from reimbursement) + config UI |
| **3** | `TravelDeskTicketService.submit` + public ticket no + matrix init |
| **4** | Approval APIs + frontend Approve tab |
| **5** | Email notifications (reuse reimbursement mail helpers) |
| **6** | Admin fulfillment APIs + proof upload + Total Travel tab |
| **7** | UAT SQL seed matrix + tab access |

---

**Next step:** Run SQL `001_travel_desk_ticket_and_matrix.sql` on UAT, then implement Phase 2–3 (backend submit + matrix config).
