# Held reimbursement tickets — automatic release

## You do **not** need a monthly backend restart

Queued tickets (`workflow_stage = HELD_FOR_CYCLE`) are released automatically when their **processing cycle month** starts. The month is already stored in the database:

- Column: `reimbursement_ticket.processing_cycle_year_month` (e.g. `2026-06` for June 2026 cycle)
- Set at submit time when the employee submits after the configured deadline day

## How release runs (application)

| When | What happens |
|------|----------------|
| **Every day 00:10** (Asia/Kolkata) | `ReimbursementHeldTicketReleaseScheduler` runs `releaseDueHeldTickets()` |
| **On application startup** | Same job runs once (catch-up if the server was down on the 1st) |

For each held ticket, if `today >= first day of processing_cycle_year_month`, the app:

1. Starts the approval workflow (`PENDING_LEVEL` / `PENDING_HOD`)
2. Sets assignees from the approval matrix
3. Writes audit `TICKET_RELEASED_FOR_APPROVAL`
4. Sends approver notification emails (if mail is configured)

As long as the iShine backend is running in production/UAT, **June tickets queued in May release on 1 June without any manual step**.

## Manual POST API (UAT only)

`POST /api/releaseHeldReimbursementTickets` is optional for testing. It is **not** required every month in production.

## Why not a MySQL EVENT only?

A database job could flip `workflow_stage` with an `UPDATE`, but it cannot:

- Resolve the approval matrix and `current_assignee_emp_id`
- Send notification emails
- Write consistent audit rows

That logic lives in Java. The DB column `processing_cycle_year_month` is the schedule; the app is the executor.

## Verify held tickets (SQL)

```sql
SELECT ticket_no, workflow_stage, processing_cycle_year_month, submitted_on
FROM reimbursement_ticket
WHERE workflow_stage = 'HELD_FOR_CYCLE' AND is_active = 1;
```

After the cycle month opens and the job has run, those rows should no longer be `HELD_FOR_CYCLE`.
