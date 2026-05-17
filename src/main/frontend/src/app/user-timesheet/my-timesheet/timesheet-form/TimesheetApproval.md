Basic take away:
1. Approval/Rejection is at project level.
2. Documents view will be in:
		a. Project level- Pending doc, Approved doc
		b. Global level- Project wise dropdown, after selecting project total working minuites(in hrs) will be displayed with two 			   document tabs, on clicking will switch document
3. Bulk reject/approve:
		a. All the project will be rejected/approved based on timesheet_id.
		b. reason id will be mapped for each project of the timesheet.
4. HOD view for update status of timesheet both project level and timesheet level.
		
Doubts:
1. Are we keeping audit for project_timesheet_status_new:
		If yes then we can reduce one table timesheet_rejection_details_new and move rejection_id and remarks to project level.
2. Is document priview part made as a reusable component:
		If yes, it can be used direcly, else will need to implement it as a helper component for reusability.
		
Pending from existing work:
1. Data issue while fetching all employee data for RM.

More required APIs for Approval flow:
1. getMyReporteesPendingTimesheetRequests(Actionable)
2. countMyReporteesTimesheetRequestsByStatus
3. getAllMyActivitiesByTimesheetId (Needs changes to fetch, based on project id)
4. getApprovedDocumentDataByDocId
5. getPendingDocumentDataByDocId
6. apporveOrRejectProjectLevelTimesheet
7. bulkApproveOrRejectTimesheetRequest
<!-- 7. bulkRejectTimesheetRequest -->
8. getRejectionReasons
9. getMyReporteesStatusBasedTimesheets(Read only)
10. updateTimesheetStatusForHOD
<!-- 10. getAllEmployeeDSROfRM-not being used can be removed -->

# Timesheet Approval and View Flow – Functional Specification Document

---

## 1. Introduction

### 1.1 Purpose

This document defines the functional requirements, user flows, validation rules, and test scenarios for the **Timesheet Approval and View System**. The system supports project-level and timesheet-level approvals with a single approval layer (RM/M) and an override authority (HOD).

### 1.2 Scope

The scope includes:

* Timesheet creation review
* Project-level approval/rejection
* Timesheet-level approval/rejection
* Bulk approval/rejection
* Document viewing at project and global level
* HOD override mechanism

### 1.3 Actors

* **Employee** (Timesheet creator – out of scope for approval)
* **RM / Manager (RM/M)** – Primary approver
* **HOD** – Override authority

---

## 2. Approval Hierarchy & Rules

1. Approval is **single-level** at RM/M level.
2. HOD has the right to **override** RM/M decisions.
3. Approval can happen at:

   * Project level
   * Timesheet level
4. Bulk actions are supported for both RM/M and HOD.
5. Timesheet status is **derived logically**, not manually updated.
6. If any project is Rejected then the entire timesheet is rejected.
7. If all the projects are approved, then the entire timesheet is approved.
---

## 3. Functional Flow – RM / Manager (RM/M)

### 3.1 View Timesheets

RM/M can view all timesheets where:

* `employee_timesheets_new.current_manager_id = RM_ID`

Displayed information:

* Employee Name / ID
* Date
* Total Working Minutes
* Calculated Status

---

### 3.2 Project-Level View & Action

#### View

* Projects fetched from `project_timesheet_status_new`
* Display:

  * Project
  * Client Working Minutes
  * Project Status
  * Approved doc
  * Pending doc

#### Actions

* Approve Project
* Reject Project (Reason mandatory)

On action:

* Update `project_timesheet_status_new.status`
* Insert audit record
* On rejection, insert record into `timesheet_rejection_details_new`

---

### 3.3 Document View

#### 3.3.1 Project-Level Document View

* Documents fetched from `timesheet_document_details_new`
* Filtered by:

  * Timesheet ID
  * Project ID

Tabs:

* Pending Documents
* Approved Documents

---

#### 3.3.2 Global Document View

* Project dropdown populated from `project_timesheet_status_new`
* On project selection:

  * Display total working minutes (hours)
  * Show two document tabs:

    * Pending Documents
    * Approved Documents

---

### 3.4 Timesheet-Level Approval / Rejection

#### Approve Timesheet

* All projects under the timesheet are approved
* No rejection entries required

#### Reject Timesheet

* All projects rejected
* Rejection reason mapped **per project**

---

### 3.5 Bulk Approval / Rejection (RM/M)

#### Bulk Approve

* All selected timesheets approved
* All projects under each timesheet approved

#### Bulk Reject

* Single rejection reason selected
* Reason mapped to:

  * Each timesheet
  * All projects under the timesheet

---

## 4. Functional Flow – HOD

### 4.1 View Timesheets

* HOD can view all employee timesheets under their hierarchy
* RM/M decision is visible

---

### 4.2 Override Capabilities

#### Project-Level Override

* HOD can change project status even after RM/M action
* Override reason mandatory
* Audit entry marked as override

#### Timesheet-Level Override

* HOD can approve or reject entire timesheet
* Overrides all project decisions

---

### 4.3 Bulk Override

* Bulk update of multiple timesheets
* Applies to all projects under selected timesheets
* Audit logged as HOD override

---

## 5. User Stories

### US-01: View Assigned Timesheets (RM/M)

As an RM/M, I want to view timesheets assigned to me so that I can approve or reject them.

### US-02: Project-Level Approval (RM/M)

As an RM/M, I want to approve or reject individual projects within a timesheet.

### US-03: Timesheet-Level Approval (RM/M)

As an RM/M, I want to approve or reject the entire timesheet in one action.

### US-04: Bulk Approval (RM/M)

As an RM/M, I want to bulk approve or reject multiple timesheets.

### US-05: Override Approval (HOD)

As an HOD, I want to override RM/M decisions at project or timesheet level.

---

## 6. Test Cases

### 6.1 Project-Level Approval

**TC-01: Approve Project**

* Expected: Project status updated, audit entry created

**TC-02: Reject Project**

* Expected: Rejection record created, audit logged

---

### 6.2 Timesheet-Level Actions

**TC-03: Approve Timesheet**

* Expected: All projects approved, timesheet status = APPROVED

**TC-04: Reject Timesheet**

* Expected: All projects rejected, rejection records created

---

### 6.3 Bulk Actions

**TC-05: Bulk Approve Timesheets**

* Expected: All selected timesheets and projects approved

**TC-06: Bulk Reject Timesheets**

* Expected: Rejection reason applied to all timesheets and projects

---

### 6.4 HOD Override

**TC-07: Override Project Decision**

* Expected: Project status updated, override audit logged

**TC-08: Override Timesheet Decision**

* Expected: Timesheet and all projects overridden

---

## 7. Data Validation & Business Rules

This section defines **strict, table-wise and flow-wise data validation rules** that must be enforced at API, service, and database layers to maintain consistency, auditability, and correctness of the timesheet approval process.

---

### 7.1 employee_timesheets_new

**Purpose**: Represents a single day timesheet for an employee. Acts as the parent entity.

#### Mandatory Validations

* `timesheet_id` must be unique and non-null.
* `emp_id`, `date`, `day_type_id`, `current_manager_id` must be present.
* Only one timesheet per `emp_id + date` combination.
* `total_working_minutes` must equal sum of:

  * `employee_timesheet_activities_mapping_new.duration_minutes`
  * Grouped by `timesheet_id`.

#### Status Derivation Rules (DO NOT UPDATE DIRECTLY)

Status is derived from `project_timesheet_status_new.status`:

* **APPROVED** → All associated projects are APPROVED
* **REJECTED** → At least one project is REJECTED
* **PENDING** → All projects are PENDING or some projects are APPROVED and other are pending.

#### Action-Level Validation

* Timesheet-level approval/rejection is allowed only if at least one project exists.
* HOD override must not delete or overwrite RM/M decision data.

---

### 7.2 project_timesheet_status_new

**Purpose**: Maintains project-wise approval status under a timesheet.

#### Key Constraints

* Unique combination: `timesheet_id + project_id + location_mapping_id`
* `status` must always have a valid enum value.

#### Approval Validation

* Project can be APPROVED only if:

  * No active rejection record exists for the same project.
  * All mapped activities exist in `employee_timesheet_activities_mapping_new`.

#### Rejection Validation

* On REJECT:

  * Entry in `timesheet_rejection_details_new` is mandatory.
  * `rejection_id` and `remarks` are mandatory.
  * `rejected_by` must match logged-in user.

#### Override Validation (HOD)

* Override must:

  * Update `status`
  * Insert new audit entry
  * Retain historical RM/M audit data

---

### 7.3 employee_timesheet_activities_mapping_new

**Purpose**: Activity-level work breakup under project and location.

#### Validations

* `timesheet_id`, `project_id`, `location_mapping_id` must exist.
* `duration_minutes` must be > 0.
* Sum of `duration_minutes` per:

  * project + location = `project_timesheet_status_new.total_client_working_minutes`

---

### 7.4 employee_timesheet_location_mapping

**Purpose**: Tracks work location and in/out times.

#### Validations

* `location_in_time` < `location_out_time`.
* Location time range must fall within timesheet work-in/work-out.
* One active location mapping per location per timesheet.

---

### 7.5 timesheet_document_details_new

**Purpose**: Stores documents attached to timesheet projects.

#### Upload Validations

* `timesheet_id`, `project_id`, `file_url`, `mime_type_id` mandatory.
* Only allowed mime types based on configuration.
* Max file size validation at API layer.

#### Approval Validations

* `final_flag = 1`:

  * Document cannot be edited or deleted.
* `active = 0`:

  * Document must not appear in UI.
* `client_approval_status_id` must sync with:

  * Project approval status.

#### Bulk Approval Validation

* `bulk_approved_doc_id` must be populated for bulk-approved documents.

---

### 7.6 final_document_new

**Purpose**: Stores finalized documents post approval.

#### Validations

* Only one final document per `project_id + location_mapping_id`.
* Created only after project approval.
* File must originate from `timesheet_document_details_new`.

---

### 7.7 timesheet_rejection_details_new

**Purpose**: Captures structured rejection reasons.

#### Mandatory Rules

* Mandatory for every REJECT action.
* One rejection record per:

  * timesheet + project + location.
* `remarks` mandatory and length validated.

#### Mapping Rules

* Timesheet-level rejection must create rejection records for:

  * All projects under the timesheet.

---

### 7.8 timesheet_action_audit

**Purpose**: Immutable audit trail.

#### Mandatory Audit Rules

* Audit entry required for:

  * Approve
  * Reject
  * Override
  * Bulk actions

#### Action Types

* APPROVE_PROJECT
* REJECT_PROJECT
* APPROVE_TIMESHEET
* REJECT_TIMESHEET
* BULK_APPROVE
* BULK_REJECT
* OVERRIDE_BY_HOD

#### Integrity Rules

* Audit data must never be updated or deleted.
* `action_by` must match logged-in user.

---

### 7.9 Cross-Table Transactional Validations

* Bulk operations must be fully transactional (ALL or NONE).
* Timesheet approval must fail if:
  * Mandatory approved document is not uploaded.
* HOD override must not break referential integrity.

---

### 7.10 Error & Edge Case Handling

* Duplicate approvals must be idempotent.
* Partial failures must rollback entire transaction.
* Concurrent approval attempts must be prevented using optimistic locking.

---

* Audit trail must be immutable
* Bulk actions must be transactional
* HOD override must not delete RM/M history
* Performance optimized for bulk operations
