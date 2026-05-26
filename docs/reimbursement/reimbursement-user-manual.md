# Reimbursement User Manual

**Document Type:** Business User Manual  
**Module:** Reimbursement  
**Audience:** Employee, Approver, Finance / Accounts, Configuration Admin  
**Scope:** Current implemented application behavior

This document is a business user manual for the Reimbursement module as it is currently implemented in the application. It covers configuration, employee claim submission, approval, finance processing, history pages, and analytics.

## 1. Purpose

The Reimbursement module is used to manage employee reimbursement claims inside iShine. It supports:

- reimbursement master configuration
- approval matrix configuration
- employee ticket-based reimbursement submission
- multi-claim reimbursement tickets
- document upload per claim
- approver review
- finance closure / paid processing
- reimbursement analytics dashboard
- history and admin pages

## 2. Main roles

The reimbursement flow usually involves these roles:

- `Configuration Admin`
- `Employee`
- `Approver`
- `Finance / Accounts`

## 3. Navigation

### Reimbursement module

Go to:

- `Reimbursement -> Apply Reimbursement`
- `Reimbursement -> View Reimbursements`
- `Reimbursement -> Approve Reimbursements`
- `Reimbursement -> Dashboard`
- `Reimbursement -> Total Reimbursement Request`

### Configuration side

Go to:

- `Configuration -> Reimbursment`

This screen contains:

- `Expenditure Type`
- `Travel Mode`
- `Vehicle Type`
- `Food Allowance Type`
- `Submission Window`
- `Approval Matrix`

## 4. Reimbursement configuration

Before employees use reimbursement, the configuration should be prepared correctly.

### 4.1 Expenditure Type

Use this section to maintain reimbursement categories.

Examples may include:

- Travel
- Food
- Accommodation
- Other business expense types

You can:

- add expenditure type
- edit expenditure type
- delete expenditure type

### 4.2 Travel Mode

Travel mode is linked to an expenditure type.

Examples:

- Personal Vehicle
- Cab
- Bus
- Train

The travel mode master also stores whether the mode requires vehicle details.

You can:

- add travel mode
- edit travel mode
- delete travel mode

### 4.3 Vehicle Type

Vehicle type is used mainly when the reimbursement mode requires vehicle selection.

Examples:

- Car
- Bike

You can:

- add vehicle type
- edit vehicle type
- delete vehicle type

### 4.4 Food Allowance Type

Use this section to maintain food-related claim types.

Examples:

- Breakfast
- Lunch
- Dinner
- Daily allowance categories

You can:

- add food allowance type
- edit food allowance type
- delete food allowance type

### 4.5 Submission Window

The reimbursement module includes a configurable submission window.

This is used to:

- enable or disable monthly submission restriction
- control the last day of the month by which claims can be submitted

This is useful when reimbursement must be submitted only within an allowed cycle.

### 4.6 Approval Matrix

Use the `Approval Matrix` configuration to define who approves reimbursement tickets.

The matrix supports:

- applicability by submitter department
- applicability by submitter job role
- 1 to 5 approval levels
- reporting manager routing
- HOD routing
- pool-based routing
- named approver routing
- finance department mapping
- optional finance assignee

Important implementation note:

- when no matrix matches, the system falls back to the legacy approval path
- the working fallback is effectively `HOD -> HR -> Finance`

## 5. Reimbursement module overview

The current reimbursement implementation is mainly **ticket-based**.

This means:

- one submission creates one reimbursement ticket
- one ticket can contain multiple claims
- each claim is a separate expense row inside the same ticket

The live user flow is centered on:

- `Apply Reimbursement`
- `Approve Reimbursements`
- `Dashboard`

At the same time, some older reimbursement pages are still available in:

- `Total Reimbursement Request`

So the module currently contains:

- a new ticket-based reimbursement flow
- some legacy reimbursement/admin/history behavior

## 6. Employee process: Apply Reimbursement

Go to:

- `Reimbursement -> Apply Reimbursement`

This is the main employee screen for creating a reimbursement ticket.

### 6.1 Key concept

The employee does not submit one expense directly to the backend one by one.

Instead:

- the employee prepares a claim row
- adds it to a draft ticket
- repeats the process for multiple claims if needed
- submits the full ticket once all claims are ready

### 6.2 Auto-captured employee details

The screen shows employee context from the system, such as:

- employee name
- employee ID
- designation
- department
- HOD name
- mobile number

These details are shown for reference and workflow validation.

### 6.3 Claim-level fields

Each reimbursement claim can contain:

- expenditure type
- project
- client handling
- amount
- purpose
- date fields
- travel-related details if required
- food-related details if required
- proof documents

### 6.4 Project and client behavior

The employee selects a project from the available project list.

Behavior implemented now:

- mapped projects are available for selection
- when the project is already mapped to a client, the client is shown accordingly
- if the project is `Others`, the employee must:
  - enter project name manually
  - select client manually

### 6.5 Amount

The employee enters the claim amount for each row.

Rules:

- amount must be greater than zero
- numeric input is enforced

### 6.6 Purpose

The employee must enter the reimbursement purpose.

This should explain why the claim is being raised.

### 6.7 Travel claim behavior

If the claim is travel-related, the form may require:

- travel mode
- vehicle type
- distance

Current implemented behavior:

- if mode is `Personal Vehicle`, vehicle type becomes important
- for `Car`, amount may be auto-calculated at `12/km`
- for `Bike`, amount may be auto-calculated at `6/km`

### 6.8 Food claim behavior

If the claim is food-related, the form may require:

- food allowance type
- fooding date

Food claims use food-specific fields instead of generic travel fields.

### 6.9 Date behavior

Different claim types use dates differently:

- food claims use `Fooding date`
- non-food claims generally use:
  - `From date`
  - `To date`

Implemented validations include:

- `To date` cannot be before `From date`
- required date fields must be entered before adding claim

### 6.10 Proof documents

Documents are attached per claim before the ticket is submitted.

Important current behavior:

- each claim should have at least one document
- multiple document slots are supported
- uploaded files can be previewed or removed before submission

Examples of proof documents:

- bill
- invoice
- receipt
- ticket
- hotel bill
- approval proof

### 6.11 Add claim to ticket

After entering one claim:

- click `Add claim to ticket`

The claim is added to the draft table.

The employee can then:

- add another claim
- edit an existing draft claim
- remove a draft claim
- review all claims before submission

### 6.12 Submit ticket

Once all draft claims are ready:

1. review the draft rows
2. click `Submit ticket`

After submission:

- the ticket is created in the backend
- a ticket number is generated
- the ticket enters the approval workflow

## 7. Employee validation rules

The current reimbursement flow checks several validations.

### 7.1 Mandatory validations

The system validates:

- expenditure type is required
- project is required
- amount must be greater than zero
- purpose is required
- at least one document is required

### 7.2 Others project validations

If the selected project is `Others`, then:

- project name is required
- client is required

### 7.3 Travel validations

If the claim is travel-related:

- travel mode is required
- if mode requires vehicle handling, vehicle type may be required
- distance may be required depending on mode

### 7.4 Food validations

If the claim is food-related:

- food allowance type is required
- fooding date is required

### 7.5 Date validations

For non-food claims:

- from date is required
- to date is required
- to date cannot be before from date

### 7.6 Submission window validation

The reimbursement module can restrict whether submission is allowed for the current period.

If the submission window is closed:

- employee submission is blocked

### 7.7 HOD dependency

Submission also depends on valid hierarchy configuration.

If HOD details are missing in employee profile:

- submission may be blocked

## 8. Approver process

Go to:

- `Reimbursement -> Approve Reimbursements`

This is the main screen used by approvers.

### 8.1 Main views

Approvers typically see:

- `Pending Actions`
- `All Tickets`

### 8.2 What an approver receives

A ticket reaches an approver based on:

- approval matrix match
- reporting manager setup
- HOD routing
- approval pool routing
- fallback legacy workflow

A ticket may contain:

- one claim
- multiple claims
- mixed expenditure types in one ticket

### 8.3 Ticket review modal

When an approver opens a reimbursement ticket, they can review:

- ticket summary
- claim rows
- project and client
- amount
- purpose
- dates
- proofs / supporting documents
- current stage
- audit history

### 8.4 Approval actions

Approver actions include:

- approve claim
- reject claim
- approve all pending claims
- reject all pending claims
- enter remarks

### 8.5 Approval rule

Approvers act at claim level.

This means:

- each pending claim in the ticket can be decided
- approved and rejected claims may coexist in one ticket
- the final ticket outcome can become partial if decisions are mixed

### 8.6 Remarks

Remarks are important in the current flow.

Approver remarks are generally required for:

- approval action
- rejection action

### 8.7 Stage movement

Depending on configuration, a ticket may move through:

- HOD
- matrix approval level(s)
- HR
- Finance

If no matrix matches:

- legacy flow applies

### 8.8 Result after approver decision

After action:

- approved claims move forward
- rejected claims stop in the workflow
- if all claims are rejected, the ticket closes as rejected
- if only some claims are rejected, the ticket remains partially progressed

## 9. Finance / Accounts process

Finance or Accounts completes the final stage of the ticket-based reimbursement process.

### 9.1 Finance stage

Finance receives tickets that are ready for payment stage.

These are usually in:

- `PENDING_FINANCE`

### 9.2 Finance actions

Finance can perform:

- `Mark as paid`
- `Reject ticket`

### 9.3 Finance action behavior

In the ticket-based flow:

- finance acts on all finance-pending claims together
- `Mark as paid` closes finance-pending claims as paid
- `Reject ticket` rejects finance-pending claims and closes the ticket accordingly

### 9.4 Finance remarks

Finance remarks are important and should be entered properly when processing the ticket.

### 9.5 Payment result

After `Mark as paid`:

- claim status becomes paid
- ticket reflects paid closure
- paid amount becomes visible in summary and analytics

After rejection:

- finance rejection status is recorded
- the ticket closes as rejected for those pending finance claims

## 10. View Reimbursements

Go to:

- `Reimbursement -> View Reimbursements`

This screen is mainly used by employees to see the summary of reimbursement tickets already submitted.

### 10.1 What the page shows

The page currently focuses on summary information such as:

- ticket number
- display status
- workflow stage
- total claimed
- paid amount
- submitted date
- approval-level summary columns
- rejection summary
- claim count

### 10.2 Current limitation

The current page is largely summary-based.

So users should expect:

- list-level ticket overview
- not a rich employee detail action from the visible table

## 11. Total Reimbursement Request

Go to:

- `Reimbursement -> Total Reimbursement Request`

This page is broader than the main ticket-based flow and still contains some older reimbursement/admin features.

### 11.1 What this page is used for

This screen may show:

- legacy reimbursement records
- finance/admin reimbursement processing
- ticket-level finance queue
- travel-based reimbursement handling

### 11.2 Important note

This page is not purely the new ticket workflow page.

It combines:

- new reimbursement finance work
- older reimbursement admin/history behavior
- travel-based reimbursement processing

So it should be understood as a combined operational/admin page.

## 12. Dashboard and analytics

Go to:

- `Reimbursement -> Dashboard`

This is the main analytics page for reimbursement and also includes Travel Desk analytics as a related section.

### 12.1 Global filters

The dashboard supports global filters such as:

- FY preset
- from date
- to date
- department
- employee
- client
- project
- display status
- workflow stage
- claim type / expenditure type

### 12.2 Filter behavior

Important implemented behavior:

- project narrows when client is selected
- filters apply across the dashboard tabs
- travel analytics shares most filters

### 12.3 Dashboard tabs

Current implemented tabs include:

- Overview
- Ticket lifecycle
- Approval analytics
- Rejection analysis
- Client-based analysis
- Project-based analysis
- Department-based analysis
- Travel Desk Analytics
- Employee analytics
- Alerts

### 12.4 Overview

The overview tab can show KPI-style metrics such as:

- total tickets
- total claims
- requested amount
- paid amount
- rejected amount
- pending pipeline
- approval-ready or finance-ready indicators

### 12.5 Ticket lifecycle

This area helps users understand ticket progression and claim movement through workflow stages.

### 12.6 Approval analytics

This tab shows queue and approval trend information, such as:

- pending HOD
- pending approval
- pending HR
- pending Finance
- matrix-level flow metrics

### 12.7 Rejection analysis

This tab is useful for:

- claim-type rejection review
- rejection reason analysis
- rejection channel analysis
- department-wise rejection patterns

### 12.8 Dimension analysis

The dashboard includes analysis by:

- client
- project
- department
- employee

### 12.9 Alerts

Alerts highlight important operational conditions, such as:

- overdue approvals
- high-value pending finance tickets

## 13. Common statuses and labels

The reimbursement module uses ticket-based and claim-based statuses.

### 13.1 Common business display labels

Users may see labels such as:

- `Submitted`
- `Pending approval`
- `Approved by HOD`
- `Pending finance`
- `Paid`
- `Rejected`
- `Partial`

### 13.2 Workflow stage labels

System workflow stages may include:

- `PENDING_HOD`
- `PENDING_LEVEL`
- `PENDING_HR`
- `PENDING_FINANCE`
- `PAID`
- `REJECTED`

### 13.3 Claim-level statuses

Detailed claim statuses may include:

- `PENDING_HOD`
- `PENDING_APPROVAL`
- `LEVEL_REJECTED`
- `HOD_REJECTED`
- `PENDING_HR`
- `HR_REJECTED`
- `PENDING_FINANCE`
- `FINANCE_REJECTED`
- `PAID`

### 13.4 Ticket and claim terminology

- `Ticket`: one reimbursement submission containing one or more claims
- `Claim`: one expense line inside the ticket
- `Proof`: supporting document attached to the claim

## 14. Recommended operating sequence

To use the reimbursement module successfully, follow this order:

1. Configure expenditure types.
2. Configure travel modes.
3. Configure vehicle types.
4. Configure food allowance types.
5. Configure submission window.
6. Configure approval matrix.
7. Employee creates a reimbursement ticket.
8. Approver reviews the ticket.
9. Finance marks ticket as paid or rejects it.
10. Employee tracks status in `View Reimbursements`.
11. Admin or management reviews trends in the dashboard.

## 15. Quick role-wise summary

### Configuration Admin

- maintain reimbursement masters
- maintain submission window
- maintain approval matrix

### Employee

- create one or more claims
- upload proof per claim
- submit ticket
- track history

### Approver

- review tickets
- approve or reject claims
- provide remarks

### Finance / Accounts

- process finance-pending tickets
- mark as paid
- reject if needed
- close finance stage

## 16. Employee user manual

This section is written for employees who raise reimbursement claims.

### 16.1 When to use reimbursement

Use the reimbursement module when you need to claim business expenses such as:

- travel reimbursement
- food reimbursement
- local conveyance
- approved business expenses supported by bills or proofs

### 16.2 Before raising a reimbursement ticket

Make sure:

- your project details are known
- your HOD details are available in the system
- your claim proofs are ready
- your claim falls inside the allowed submission period

### 16.3 Open the apply screen

Go to:

- `Reimbursement -> Apply Reimbursement`

### 16.4 Create a claim row

1. Select expenditure type.
2. Select project.
3. If project is `Others`, enter:
   - project name
   - client
4. Enter amount.
5. Enter purpose.
6. Enter dates based on claim type.
7. If applicable, fill travel fields or food fields.
8. Upload claim proof.
9. Click `Add claim to ticket`.

### 16.5 Add multiple claims

You can repeat the same process to add multiple claims into one ticket.

This is useful when:

- one reimbursement submission contains many related expenses
- you want one ticket for one reimbursement cycle

### 16.6 Edit or remove draft claims

Before submission, you can:

- edit a draft claim
- remove a draft claim
- review all claims in the draft list

### 16.7 Submit the ticket

Once all claims are ready:

1. review the draft list
2. click `Submit ticket`

After submission:

- the ticket is created
- it enters the approval workflow
- you can track it from history pages

### 16.8 Employee checks before submission

Verify:

- amount is correct
- project is correct
- client is selected where required
- purpose is meaningful
- claim dates are correct
- proof is uploaded

### 16.9 Track your reimbursements

Go to:

- `Reimbursement -> View Reimbursements`

You can check:

- ticket number
- status
- total claimed
- paid amount
- claim count
- approval progression summary

## 17. Approver user manual

This section is for managers, HODs, HR reviewers, and matrix-based approvers.

### 17.1 Open the approval screen

Go to:

- `Reimbursement -> Approve Reimbursements`

### 17.2 Main views

You will generally see:

- `Pending Actions`
- `All Tickets`

### 17.3 Review a ticket

1. Open the assigned ticket.
2. Review employee and ticket summary.
3. Review each claim row.
4. Check:
   - expenditure type
   - project / client
   - amount
   - purpose
   - date range
   - proof documents

### 17.4 Approve or reject

Approvers can:

- approve claim(s)
- reject claim(s)
- approve all
- reject all
- add remarks

### 17.5 Important rule

Action is claim-based, not only ticket-based.

This means:

- one ticket may contain mixed decisions
- some claims may move ahead while others get rejected
- the ticket may become partial if outcomes are mixed

### 17.6 What happens after action

- approved claims move to next workflow stage
- rejected claims stop progressing
- fully rejected tickets close as rejected
- partially approved tickets continue with the remaining valid claims

## 18. Finance / Accounts user manual

This section is for finance or accounts users processing the final stage.

### 18.1 Open the finance work queue

Go to:

- `Reimbursement -> Total Reimbursement Request`

and/or use the finance stage from the reimbursement ticket processing flow where applicable.

### 18.2 Identify finance-pending tickets

Finance works on tickets that are ready for payment stage.

These are generally tickets in:

- `Pending finance`

### 18.3 Review before payment

Before processing, confirm:

- the ticket is fully approved up to finance stage
- claim amounts are correct
- documents are available
- remarks and workflow history are reasonable

### 18.4 Finance actions

Finance can:

- `Mark as paid`
- `Reject ticket`

### 18.5 Mark as paid

Use this when payment is approved and completed.

After marking paid:

- finance-pending claims become paid
- ticket status reflects payment completion
- dashboard metrics update accordingly

### 18.6 Reject ticket

Use rejection only when finance cannot process the claim.

In this case:

- enter clear remarks
- close the applicable finance stage claims as rejected

### 18.7 Legacy / travel-based reimbursements

Finance users may also see older reimbursement pages and travel-based reimbursement processing in `Total Reimbursement Request`.

This means:

- some rows on this page belong to legacy flow
- some rows belong to current ticket-based flow
- travel-based reimbursement handling may also appear here

## 19. Configuration Admin user manual

This section is for users maintaining reimbursement configuration.

### 19.1 Open reimbursement configuration

Go to:

- `Configuration -> Reimbursment`

### 19.2 Configure expenditure types

Create and maintain expense categories that employees will use while submitting reimbursement claims.

Recommended checks:

- avoid duplicate types
- keep names business-friendly
- keep descriptions meaningful

### 19.3 Configure travel modes

Map valid travel modes under the correct expenditure type.

Recommended checks:

- assign only relevant modes
- maintain clean naming
- verify whether vehicle detail behavior is needed

### 19.4 Configure vehicle types

Keep allowed vehicle values clean and standardized.

Examples:

- Car
- Bike

### 19.5 Configure food allowance types

Maintain all valid food allowance categories used in reimbursement.

### 19.6 Configure submission window

Use this carefully to control when employees can submit reimbursement tickets.

Recommended admin action:

- confirm whether the window is open or closed
- verify cutoff day based on policy

### 19.7 Configure approval matrix

Set up approval routing using:

- submitter department
- submitter role
- one or more approval levels
- finance mapping

Recommended checks:

- test one sample employee
- verify matrix applicability
- confirm fallback behavior if no matrix matches

### 19.8 Best practice before production use

1. configure all masters
2. configure approval matrix
3. configure submission window
4. submit a sample reimbursement ticket
5. test an approval cycle
6. test finance closure
7. verify ticket visibility in history and dashboard

## 20. Important implementation notes

- The active employee apply flow is ticket-based and supports multiple claims per ticket.
- `View Reimbursements` is mainly summary-oriented at present.
- `Total Reimbursement Request` combines current ticket-finance work with older reimbursement/admin screens.
- Mixed claim outcomes are possible inside one ticket.
- Some approval-matrix screen wording may suggest full-ticket rejection, but the actual claim workflow supports partial outcomes.
- The dashboard is broad and filter-driven, and some users see org-wide data while others only see workflow-visible data.
- This manual reflects the current implemented application behavior.
