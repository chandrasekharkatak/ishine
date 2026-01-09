# Timesheet Module - Domain-Specific Evaluation Questions

**Purpose**: Evaluate a developer's deep understanding of the Timesheet domain, business logic, and architectural decisions—not just code syntax.

**Context**: Production HRMS/Timesheet system with hierarchical structure (Employee → Project → Activity), multi-project support, approval workflows, and role-based access control.

---

## 1️⃣ CORE TIMESHEET DOMAIN UNDERSTANDING

### Q1.1: Business Entity Hierarchy
**Question**: Explain the relationship between `EmployeeTimesheetDTO`, `ProjectTimesheetDTO`, and `ActivityTimesheetDTO` in business terms. Why is this hierarchical structure necessary instead of a flat "one row per activity" design? What business problem does this solve?

**What to Look For**:
- ✅ Understands that one employee can work on multiple projects in a single day
- ✅ Recognizes that each project can have multiple activities
- ✅ Explains how this structure enables partial approvals (approve Project A but reject Project B)
- ✅ Mentions aggregation benefits (total hours per day, per project, per activity)
- ❌ Just describes the structure without business rationale
- ❌ Doesn't understand why it's not just a flat table

### Q1.2: Time Tracking Semantics
**Question**: The system has three types of time tracking: `officeInTime`/`officeOutTime` at employee level, `clientInTime`/`clientOutTime` at project level, and `durationMinutes` at activity level. Explain when each should be used and what business scenarios they represent. What happens if an employee provides office times but no client times?

**What to Look For**:
- ✅ Understands office time = physical presence/attendance
- ✅ Understands client time = billable time to specific client project
- ✅ Understands activity time = granular work breakdown
- ✅ Recognizes that office time ≠ sum of client times (breaks, meetings, etc.)
- ✅ Explains validation rules (office time should encompass client times)
- ❌ Treats all times as equivalent
- ❌ Doesn't understand the business distinction

### Q1.3: Day Type vs Status
**Question**: What's the difference between `dayTypeId` (Working, Week Off, Public Holiday, Leave) and `status` (Pending, Approved, Rejected, Partial)? Can a timesheet have `dayTypeId = "Leave"` and `status = "Approved"`? Explain the business logic.

**What to Look For**:
- ✅ Understands dayType = what happened that day (nature of day)
- ✅ Understands status = approval state (workflow state)
- ✅ Recognizes that Leave can be approved/rejected by manager
- ✅ Explains that Week Off/Public Holiday might not need approval
- ❌ Confuses dayType with status
- ❌ Doesn't understand approval workflow for non-working days

### Q1.4: Timesheet Validity
**Question**: From a domain perspective, what makes a timesheet "valid" vs "invalid"? Consider: missing activities, time overlaps, future dates, locked periods, and approval states. Which validations are business-critical (affect payroll) vs nice-to-have (UX improvements)?

**What to Look For**:
- ✅ Distinguishes between data integrity (required fields) and business rules (time logic)
- ✅ Understands payroll impact (approved timesheets affect salary calculation)
- ✅ Recognizes timesheet lock periods (can't edit past certain date)
- ✅ Mentions validation hierarchy (employee → project → activity)
- ❌ Lists validations without understanding business impact
- ❌ Doesn't prioritize critical vs non-critical rules

---

## 2️⃣ MULTI-PROJECT & MULTI-ACTIVITY SCENARIOS

### Q2.1: Multi-Project Time Allocation
**Question**: An employee works 9 AM - 6 PM (9 hours) and logs:
- Project A: 4 hours (Development activities)
- Project B: 3 hours (Code Review activities)
- Project C: 2 hours (Meeting activities)

How should the system validate this? What if the employee logs 5 hours for Project A, 4 hours for Project B, and 3 hours for Project C (total 12 hours)? What business rules should apply?

**What to Look For**:
- ✅ Recognizes sum of project times should ≤ office time (with tolerance for breaks)
- ✅ Understands that activities within a project should sum to project time
- ✅ Mentions configurable tolerance (e.g., 30 min break allowed)
- ✅ Explains validation at different levels (activity → project → day)
- ❌ Doesn't see the validation problem
- ❌ Suggests hard equality without considering breaks

### Q2.2: Partial Approval Scenario
**Question**: A timesheet has 3 projects, all initially "Pending". Manager approves Project A, rejects Project B, and leaves Project C pending. What should the overall timesheet status be? How should this affect:
- Payroll calculation
- Reporting dashboards
- Employee visibility
- Client billing

**What to Look For**:
- ✅ Understands overall status = "Partial" (mixed states)
- ✅ Recognizes payroll should only count approved projects
- ✅ Explains that reporting needs to handle partial states
- ✅ Mentions that employee can see which projects are approved/rejected
- ✅ Understands client billing depends on project approval
- ❌ Suggests binary approved/rejected
- ❌ Doesn't consider downstream impact

### Q2.3: Time Overlap Detection
**Question**: An employee logs:
- Activity 1 (Project A): 9:00 AM - 1:00 PM
- Activity 2 (Project B): 12:00 PM - 4:00 PM
- Activity 3 (Project A): 3:00 PM - 6:00 PM

Should this be allowed? What business scenarios justify overlapping times? How would you detect and handle this?

**What to Look For**:
- ✅ Recognizes overlap between Activity 1 and 2 (12:00-1:00 PM)
- ✅ Understands that some scenarios might allow overlap (context switching, multi-tasking)
- ✅ Suggests validation rules (warn vs block)
- ✅ Mentions business configuration (strict vs flexible)
- ❌ Doesn't detect the overlap
- ❌ Suggests hard blocking without business context

### Q2.4: Missing Time Gaps
**Question**: Office time is 9 AM - 6 PM (9 hours). Employee logs activities totaling 7 hours with a 2-hour gap. Should the system:
- Reject the timesheet?
- Warn but allow?
- Auto-fill with "Break" or "Other" activity?
- Ignore the gap?

What business factors influence this decision?

**What to Look For**:
- ✅ Recognizes gap detection (9 hours office - 7 hours activities = 2 hours)
- ✅ Understands business rules vary (some companies require all time accounted for)
- ✅ Mentions configurable policies
- ✅ Considers different day types (working vs leave)
- ❌ Doesn't see the gap
- ❌ Suggests one-size-fits-all solution

---

## 3️⃣ APPROVAL & STATUS LIFECYCLE

### Q3.1: Status Calculation Logic
**Question**: The `EmployeeTimesheetDTO.status` is described as "calculated logically from project statuses." Walk through the algorithm: Given a timesheet with 3 projects having statuses [Approved, Rejected, Pending], what should the overall status be? What about [Approved, Approved, Rejected]? What about [Approved, Partial, Pending]?

**What to Look For**:
- ✅ Provides clear algorithm (e.g., if all Approved → Approved, if all Rejected → Rejected, if mixed → Partial)
- ✅ Handles edge cases (empty projects, all Pending)
- ✅ Understands Partial state meaning
- ❌ Doesn't have a clear algorithm
- ❌ Doesn't handle edge cases

### Q3.2: Re-approval After Rejection
**Question**: A timesheet is rejected by Manager. Employee updates it and resubmits. What should happen to:
- The previous rejection record
- The approval history
- The status transitions
- Any payroll calculations that already ran

**What to Look For**:
- ✅ Understands need for audit trail (keep rejection history)
- ✅ Recognizes status should transition: Rejected → Pending → Approved
- ✅ Mentions impact on payroll (if already processed, needs correction)
- ✅ Considers notification to approver
- ❌ Suggests deleting rejection history
- ❌ Doesn't consider payroll impact

### Q3.3: Cascading Approval Impact
**Question**: If a Project is rejected, should all its Activities automatically be marked as rejected? Or can some Activities be approved while others are rejected? What's the business rationale for your answer?

**What to Look For**:
- ✅ Understands parent-child relationship (project → activities)
- ✅ Recognizes business need (might approve some activities, reject others)
- ✅ Explains impact on aggregation (rejected activities don't count toward totals)
- ❌ Suggests automatic cascading without business justification
- ❌ Doesn't understand hierarchical approval

### Q3.4: Approval Deadline & Lock Periods
**Question**: The system has "timesheet lock periods" where employees can't edit past timesheets. How should this interact with approval deadlines? What happens if:
- Employee submits timesheet on Day 5 of next month
- Manager tries to approve on Day 10 (after lock period)
- Payroll runs on Day 15

**What to Look For**:
- ✅ Understands lock period prevents edits but not approvals
- ✅ Recognizes approval can happen after lock
- ✅ Explains payroll impact (can't process unapproved timesheets)
- ✅ Mentions escalation/escalation workflows
- ❌ Confuses lock period with approval deadline
- ❌ Doesn't consider payroll timing

---

## 4️⃣ ROLE & ACCESS CONTROL (DOMAIN VIEW)

### Q4.1: Role-Based Visibility
**Question**: Explain what different roles (Employee, Manager, HOD, Finance, Admin) should see in the timesheet module. Why can't an Employee see all timesheets? Why can't a Manager approve timesheets from a different department? What business risks exist if access control is bypassed?

**What to Look For**:
- ✅ Understands Employee sees only their own timesheets
- ✅ Recognizes Manager sees direct reports + their own
- ✅ Explains HOD sees department-wide
- ✅ Mentions Finance needs aggregated data for payroll
- ✅ Identifies risks (data privacy, unauthorized approvals, payroll fraud)
- ❌ Doesn't understand role hierarchy
- ❌ Doesn't see business risks

### Q4.2: Shadow Timesheet Authorization
**Question**: What is a "shadow timesheet" and when is it used? Who can create shadow timesheets? What authorization checks must happen? What happens if a Manager creates a shadow timesheet for an employee they don't manage?

**What to Look For**:
- ✅ Understands shadow = manager creates timesheet on behalf of employee
- ✅ Recognizes need for manager-employee relationship validation
- ✅ Explains use cases (employee on leave, forgot to submit)
- ✅ Mentions audit trail (who created, when, why)
- ❌ Doesn't understand shadow concept
- ❌ Doesn't see authorization requirements

### Q4.3: Department + Project + Location Access
**Question**: An employee works on Project X (Client: Acme Corp, Location: New York). Which of these should affect what the employee can see/edit:
- Their department
- The project's department
- The client's department
- The location

Explain the business logic behind each.

**What to Look For**:
- ✅ Understands employee can only work on projects they're assigned to
- ✅ Recognizes department boundaries (can't see other dept projects)
- ✅ Explains location-based access (might be required for compliance)
- ✅ Mentions project assignment validation
- ❌ Doesn't see multi-dimensional access control
- ❌ Suggests simple role-based without context

### Q4.4: Centralized Authorization
**Question**: Why must authorization logic be centralized in a service layer rather than scattered across controllers? What happens if authorization is duplicated in 5 different places and one place gets updated but others don't? Give a concrete example from the timesheet module.

**What to Look For**:
- ✅ Understands DRY principle (Don't Repeat Yourself)
- ✅ Recognizes maintenance nightmare (update 5 places)
- ✅ Explains security risk (one place might miss a check)
- ✅ Gives concrete example (e.g., approval endpoint vs query endpoint)
- ❌ Doesn't see the problem
- ❌ Doesn't understand security implications

---

## 5️⃣ DATA MODEL & TABLE DESIGN AWARENESS

### Q5.1: Why `_new` Tables?
**Question**: The codebase has both old tables (`EmployeeTimesheets`, `EmployeeTimesheetActivitiesMapping`) and new tables (`employee_timesheets_new`, `employee_timesheet_activities_mapping_new`). Why weren't the old tables simply altered? What risks exist in running both in parallel? How should migration work?

**What to Look For**:
- ✅ Understands zero-downtime migration strategy
- ✅ Recognizes backward compatibility needs
- ✅ Explains risk of breaking existing integrations
- ✅ Mentions gradual migration approach
- ✅ Understands data consistency challenges (two sources of truth)
- ❌ Suggests just altering old tables
- ❌ Doesn't see migration complexity

### Q5.2: Composite Keys in Activity Mapping
**Question**: The `employee_timesheet_activities_mapping_new` table uses a composite key (`timesheetId`, `activityId`, `projectId`). Why is this necessary? What business rule does this enforce? What would happen if we used a simple auto-increment ID instead?

**What to Look For**:
- ✅ Understands composite key prevents duplicate activity entries for same project
- ✅ Recognizes business rule: one activity can appear once per project per timesheet
- ✅ Explains that auto-increment ID would allow duplicates
- ✅ Mentions performance implications (index on composite key)
- ❌ Doesn't understand composite key purpose
- ❌ Suggests simple ID without business justification

### Q5.3: Transactional vs Reporting Tables
**Question**: Some timesheet data is stored in normalized transactional tables, while some is denormalized in reporting tables. Explain the trade-offs. When would you query `employee_timesheets_new` vs a reporting table? What happens if they get out of sync?

**What to Look For**:
- ✅ Understands transactional = normalized, write-optimized
- ✅ Recognizes reporting = denormalized, read-optimized
- ✅ Explains sync mechanisms (ETL, triggers, eventual consistency)
- ✅ Mentions when to use each (real-time vs batch reporting)
- ❌ Doesn't understand the distinction
- ❌ Doesn't see sync challenges

### Q5.4: Calculated vs Stored Fields
**Question**: `totalWorkingMinutes` in `EmployeeTimesheetDTO` is described as "calculated from officeInTime and officeOutTime." Why store it instead of calculating on-the-fly? What about `totalActivitiesMinutes`? When should calculated fields be stored vs computed?

**What to Look For**:
- ✅ Understands performance trade-off (calculation cost vs storage)
- ✅ Recognizes consistency (stored value might differ from calculated if data changes)
- ✅ Explains when to recalculate (on update, on approval)
- ✅ Mentions audit trail (stored value shows what was approved)
- ❌ Always calculates on-the-fly
- ❌ Always stores without considering consistency

---

## 6️⃣ VALIDATION & BUSINESS RULES

### Q6.1: Time Validation Rules
**Question**: List all the time-related validation rules you can think of for timesheets. Consider: future dates, past lock periods, office time vs activity time, overlaps, maximum hours per day, minimum hours per day, weekend/holiday rules. Which are hard rules (must reject) vs soft rules (warn but allow)?

**What to Look For**:
- ✅ Comprehensive list (future dates, lock periods, time ranges, overlaps, max/min hours)
- ✅ Distinguishes hard vs soft rules
- ✅ Mentions business configuration (different clients might have different rules)
- ✅ Considers edge cases (night shifts, time zones)
- ❌ Incomplete list
- ❌ Doesn't distinguish rule types

### Q6.2: Client-Specific Rules
**Question**: Some clients require attendance proof documents for timesheet approval, while others don't. Some clients have maximum billable hours per day. How should the system handle client-specific validation rules? Where should this logic live?

**What to Look For**:
- ✅ Understands need for configurable rules per client/project
- ✅ Suggests rule engine or configuration table
- ✅ Recognizes validation should be in service layer, not controller
- ✅ Mentions rule inheritance (client → project → activity)
- ❌ Suggests hard-coded rules
- ❌ Doesn't see need for configurability

### Q6.3: Location-Specific Rules
**Question**: The system tracks `clientLocationId` at the activity level. What business rules might be location-specific? Examples: different working hours for different locations, location-based approval workflows, location-based compliance requirements.

**What to Look For**:
- ✅ Understands location can affect working hours (time zones, local laws)
- ✅ Recognizes approval workflows might vary by location
- ✅ Mentions compliance (labor laws differ by location)
- ✅ Explains how to implement location-based rules
- ❌ Doesn't see location impact
- ❌ Doesn't understand compliance requirements

### Q6.4: Validation Bypass Scenarios
**Question**: What happens if validation is bypassed (e.g., direct database insert, API without validation)? What are the business consequences? How should the system prevent this? What monitoring/alerting is needed?

**What to Look For**:
- ✅ Understands business risks (incorrect payroll, compliance violations)
- ✅ Recognizes need for database constraints (not just application-level)
- ✅ Mentions audit logging for bypass attempts
- ✅ Explains monitoring (alert on unusual patterns)
- ❌ Doesn't see the risk
- ❌ Relies only on application validation

---

## 7️⃣ FAILURE & EDGE CASE THINKING

### Q7.1: Partial Approval Failure
**Question**: During bulk approval of 100 timesheets, the system fails after approving 50. What should happen? Should the 50 approvals be rolled back? Should they be kept? How do you ensure data consistency? What's the recovery process?

**What to Look For**:
- ✅ Understands transaction boundaries (all-or-nothing vs partial)
- ✅ Recognizes business impact (50 employees might have been notified)
- ✅ Suggests idempotent operations (can retry safely)
- ✅ Mentions compensation transactions (rollback mechanism)
- ❌ Suggests always rollback (might lose valid approvals)
- ❌ Doesn't consider business impact

### Q7.2: Data Corruption Recovery
**Question**: A timesheet has inconsistent data: `totalWorkingMinutes = 480` (8 hours) but sum of activities = 600 minutes (10 hours). How should the system detect and handle this? Should it auto-correct? Should it flag for manual review? What's the business impact?

**What to Look For**:
- ✅ Understands data integrity checks (reconciliation)
- ✅ Recognizes auto-correction risks (might hide real issues)
- ✅ Suggests flagging for review
- ✅ Explains business impact (payroll errors, billing disputes)
- ❌ Doesn't see the inconsistency
- ❌ Suggests always auto-correct

### Q7.3: Duplicate Submission
**Question**: An employee submits a timesheet twice (network retry, UI bug, etc.). How should the system handle this? Should it reject the duplicate? Should it update the existing one? How do you detect duplicates? What's the user experience?

**What to Look For**:
- ✅ Understands idempotency (same request = same result)
- ✅ Recognizes duplicate detection (same employee + date + timestamp)
- ✅ Suggests update existing vs reject duplicate
- ✅ Mentions user feedback (show "already submitted" message)
- ❌ Creates duplicate records
- ❌ Doesn't handle retries

### Q7.4: Concurrent Approval
**Question**: Two managers try to approve the same timesheet simultaneously. What should happen? Should the first win? Should both be rejected? How do you prevent race conditions? What's the business impact of each approach?

**What to Look For**:
- ✅ Understands race condition (both read "Pending", both try to update)
- ✅ Recognizes need for optimistic locking (version field)
- ✅ Suggests first-wins or conflict resolution
- ✅ Explains business impact (double approval, notification spam)
- ❌ Doesn't see the race condition
- ❌ Doesn't consider concurrency

---

## 8️⃣ PERFORMANCE & SCALABILITY (DOMAIN-AWARE)

### Q8.1: Query Service Separation
**Question**: Why was `TimesheetQueryService` separated from `TimesheetService`? What types of queries belong in QueryService vs main Service? How does this affect performance for 10,000+ employees?

**What to Look For**:
- ✅ Understands separation of concerns (read vs write)
- ✅ Recognizes read optimization (caching, denormalization)
- ✅ Explains write optimization (transactional, normalized)
- ✅ Mentions scalability (read queries can be distributed)
- ❌ Doesn't see the benefit
- ❌ Suggests everything in one service

### Q8.2: Index-Killing Conditions
**Question**: A query filters timesheets by `status = 'Pending'` and `date BETWEEN startDate AND endDate`. What indexes should exist? What happens if you add `employee.name LIKE '%John%'` to the WHERE clause? How does this affect performance at scale?

**What to Look For**:
- ✅ Understands index usage (composite index on status + date)
- ✅ Recognizes LIKE with leading wildcard kills index
- ✅ Suggests alternative (full-text search, separate name lookup)
- ✅ Explains performance impact (full table scan)
- ❌ Doesn't understand indexing
- ❌ Doesn't see query performance impact

### Q8.3: Large-Scale Usage Impact
**Question**: The system needs to handle 10,000 employees submitting timesheets daily. What are the bottlenecks? How would you optimize:
- Timesheet submission (writes)
- Approval workflows (reads + writes)
- Reporting dashboards (reads)
- Payroll integration (batch reads)

**What to Look For**:
- ✅ Identifies bottlenecks (database writes, approval notifications, reporting queries)
- ✅ Suggests optimizations (async processing, caching, batch operations)
- ✅ Recognizes different optimization strategies for different operations
- ✅ Mentions horizontal scaling (read replicas, sharding)
- ❌ Doesn't see scalability challenges
- ❌ Suggests one-size-fits-all optimization

### Q8.4: Read vs Write Optimization
**Question**: Timesheet data is written once (on submission) but read many times (dashboards, reports, approvals). How should the data model differ for writes vs reads? What caching strategies make sense? When is eventual consistency acceptable?

**What to Look For**:
- ✅ Understands write-optimized = normalized, transactional
- ✅ Recognizes read-optimized = denormalized, cached
- ✅ Suggests caching strategies (Redis, materialized views)
- ✅ Explains eventual consistency (reporting can be slightly stale)
- ❌ Doesn't see read/write trade-offs
- ❌ Suggests same model for both

---

## 9️⃣ FRONTEND ↔ BACKEND CONTRACT AWARENESS

### Q9.1: API Contract Stability
**Question**: The frontend expects `EmployeeTimesheetDTO` with nested `ProjectTimesheetDTO` and `ActivityTimesheetDTO`. What happens if the backend changes the DTO structure? How should API versioning work? What's the impact of breaking changes?

**What to Look For**:
- ✅ Understands API contract = contract between frontend and backend
- ✅ Recognizes breaking changes break frontend
- ✅ Suggests versioning (v1, v2) or backward compatibility
- ✅ Mentions deprecation strategy (old API supported for X months)
- ❌ Doesn't see the contract
- ❌ Suggests always breaking changes

### Q9.2: Draft vs Submitted States
**Question**: How should the system handle draft timesheets (employee started filling but didn't submit)? Should drafts be saved in the same table? Should they be in a separate table? How does this affect the API contract?

**What to Look For**:
- ✅ Understands draft = work in progress, not yet submitted
- ✅ Recognizes drafts might have incomplete data
- ✅ Suggests separate table or status field
- ✅ Explains API impact (different endpoints for draft vs submitted)
- ❌ Doesn't distinguish draft from submitted
- ❌ Doesn't see API implications

### Q9.3: UI Componentization
**Question**: Why is the timesheet UI componentized (separate components for project entry, activity entry, document upload)? How does this relate to the backend API design? What happens if frontend and backend get out of sync?

**What to Look For**:
- ✅ Understands componentization = separation of concerns
- ✅ Recognizes backend should match frontend structure (hierarchical DTOs)
- ✅ Explains sync challenges (frontend expects fields backend doesn't provide)
- ✅ Mentions contract testing (ensure API matches frontend expectations)
- ❌ Doesn't see the relationship
- ❌ Doesn't understand sync challenges

### Q9.4: Preventing UI-Side Inconsistencies
**Question**: The frontend calculates `totalWorkingHours` from `officeInTime` and `officeOutTime`. The backend also calculates this. What happens if they differ? How do you ensure consistency? Should calculation be frontend-only, backend-only, or both?

**What to Look For**:
- ✅ Understands risk of inconsistency (frontend shows one value, backend stores another)
- ✅ Recognizes backend is source of truth
- ✅ Suggests frontend calculation for UX, backend validation
- ✅ Mentions reconciliation (backend recalculates on save)
- ❌ Doesn't see inconsistency risk
- ❌ Suggests only frontend or only backend

---

## 🔟 ARCHITECTURAL MATURITY QUESTIONS

### Q10.1: DDD Fit for Timesheet Module
**Question**: Why does Domain-Driven Design (DDD) fit the Timesheet module? Identify:
- **Aggregates**: What are the aggregate roots? (Hint: `EmployeeTimesheet` is likely one)
- **Entities**: What are the entities within aggregates?
- **Value Objects**: What are value objects? (Hint: time ranges, status values)
- **Domain Services**: What business logic belongs in domain services vs application services?

**What to Look For**:
- ✅ Identifies `EmployeeTimesheet` as aggregate root
- ✅ Recognizes `ProjectTimesheet` and `ActivityTimesheet` as entities within aggregate
- ✅ Understands value objects (TimeRange, Status, DayType)
- ✅ Distinguishes domain services (business rules) from application services (orchestration)
- ❌ Doesn't understand DDD concepts
- ❌ Can't identify aggregates/entities/value objects

### Q10.2: Timesheet-Payroll Integration
**Question**: How should the Timesheet module integrate with the Payroll module? Should they be tightly coupled or loosely coupled? What data should be shared? How do you handle:
- Payroll running before timesheet approval
- Timesheet corrections after payroll processing
- Different approval workflows for different payroll periods

**What to Look For**:
- ✅ Understands loose coupling (events, message queue, API)
- ✅ Recognizes shared data (approved timesheets, hours, dates)
- ✅ Explains compensation transactions (payroll corrections)
- ✅ Mentions event-driven architecture (timesheet approved → notify payroll)
- ❌ Suggests tight coupling (direct database access)
- ❌ Doesn't see integration challenges

### Q10.3: Eventual Consistency Boundaries
**Question**: In the Timesheet module, where is eventual consistency acceptable vs where must it be strongly consistent? Consider:
- Timesheet submission (employee creates)
- Approval workflow (manager approves)
- Reporting dashboards (showing timesheet counts)
- Payroll integration (calculating salary)

**What to Look For**:
- ✅ Understands strong consistency needed for writes (submission, approval)
- ✅ Recognizes eventual consistency acceptable for reads (reporting, dashboards)
- ✅ Explains why (writes affect business logic, reads are informational)
- ✅ Mentions trade-offs (performance vs consistency)
- ❌ Suggests always strong consistency
- ❌ Doesn't understand consistency levels

### Q10.4: Bounded Context Boundaries
**Question**: The Timesheet module interacts with Employee, Project, Client, and Payroll modules. Where are the bounded context boundaries? What data should be shared vs duplicated? How do you handle data that exists in multiple contexts?

**What to Look For**:
- ✅ Understands bounded context = domain boundary
- ✅ Recognizes shared data (employee ID, project ID) vs context-specific data
- ✅ Explains anti-corruption layer (translate between contexts)
- ✅ Mentions shared kernel (common data model)
- ❌ Doesn't understand bounded contexts
- ❌ Suggests sharing all data

---

## 📊 EVALUATION RUBRIC

### ❌ **Novice Level** (Red Flags)
- Can't explain business purpose of timesheet structure
- Doesn't understand multi-project scenarios
- Suggests hard-coded rules without business justification
- Doesn't see edge cases or failure scenarios
- Focuses only on code syntax, not domain logic

### ⚠️ **Intermediate Level** (Yellow Flags)
- Understands structure but not business rationale
- Can implement features but doesn't question design
- Recognizes some edge cases but not all
- Knows validation rules but not why they exist
- Can code but doesn't think about scalability

### ✅ **Senior/Expert Level** (Green Flags)
- Explains business purpose behind every design decision
- Questions assumptions and suggests improvements
- Thinks about edge cases, failures, and recovery
- Understands scalability and performance implications
- Sees the bigger picture (integration, bounded contexts, DDD)

---

## 🎯 HOW TO USE THESE QUESTIONS

1. **Start with Section 1** (Core Domain) - If they fail here, they don't understand the domain
2. **Progress to Sections 2-4** (Scenarios, Approval, Access) - Tests practical understanding
3. **Sections 5-6** (Data Model, Validation) - Tests technical depth
4. **Sections 7-8** (Edge Cases, Performance) - Tests senior-level thinking
5. **Sections 9-10** (Architecture) - Tests architectural maturity

**Scoring Guide**:
- **0-3 correct answers per section**: Needs significant training
- **4-6 correct answers per section**: Can work with guidance
- **7-8 correct answers per section**: Can work independently
- **9-10 correct answers per section**: Can lead and mentor

---

**Document Version**: 1.0  
**Created**: 2026-01-04  
**Purpose**: Domain-specific evaluation for Timesheet module developers

