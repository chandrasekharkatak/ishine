# Training Management & Compliance Module - Phase 1
## Assumptions, Clarifications & Readiness Confirmation

---

## 📋 ASSUMPTIONS

### **1. Technical Stack Assumptions**

#### Backend:
- **Framework:** Spring Boot (Java) - Based on existing codebase structure
- **Database:** MySQL - Based on existing JPA repositories
- **ORM:** JPA/Hibernate - Based on existing Entity patterns
- **File Storage:** 
  - PPT/Video/Audio files stored as BLOB in database (similar to TimesheetDocumentDetails pattern)
  - OR stored in file system with path reference (to be confirmed)
- **Session Management:** Existing UserSession mechanism will be used
- **Authentication:** Existing authentication flow (OTP-based) will be used

#### Frontend:
- **Framework:** Angular - Based on existing frontend structure
- **UI Library:** Bootstrap/NgBootstrap - Based on existing modal patterns
- **State Management:** Service-based with BehaviorSubject (similar to AuthenticationService pattern)
- **Routing:** Angular Router with AuthGuard integration

---

### **2. Database Structure Assumptions**

#### New Tables Required:
1. **training_master** - Training configuration
2. **training_content** - Content items (PPT/Video/Audio/Link) with sequence
3. **training_assignment** - Employee training assignments
4. **training_view_time_log** - Server-side time tracking
5. **training_consent** - Consent submissions
6. **training_lock_status** - Current lock status per employee

#### Assumed Fields:
- Training Master: training_id, name, type, mandatory_flag, effective_from, effective_to, frequency_per_year, lock_enabled, min_view_time_minutes, consent_required, skip_allowed, mandatory_completion_deadline, active_status, created_by, created_on, updated_by, updated_on
- Training Content: content_id, training_id, content_type (PPT/Video/Audio/Link), content_data/path, sequence_order, active
- Training Assignment: assignment_id, training_id, emp_id, assigned_date, due_date, status (Pending/InProgress/Completed/Skipped), completion_count
- View Time Log: log_id, assignment_id, emp_id, session_start_time, session_end_time, total_time_seconds, is_active_session
- Training Consent: consent_id, assignment_id, emp_id, training_id, consent_timestamp, total_time_spent_seconds, completion_cycle_number
- Lock Status: lock_id, emp_id, training_id, is_locked, lock_reason, locked_since, skip_count

---

### **3. Business Logic Assumptions**

#### Frequency Calculation:
- **Rolling 12-month window:** Counts completions in last 12 months from current date
- **Completion counting:** Each consent submission = 1 completion
- **Auto-reassignment:** System checks on login if employee needs training (completion_count < 2 in last 12 months)

#### Minimum View Time:
- **Server-side tracking:** Time tracked via API heartbeats/ping mechanism
- **Continuous timer:** Timer runs only when training page is active (not in background tab)
- **Session-based:** If user closes browser, timer resets (must complete in single session OR accumulate across sessions - **NEEDS CLARIFICATION**)

#### Lock Mechanism:
- **Global lock:** When locked, employee can ONLY access:
  - Training page
  - Logout
  - Training-related APIs
- **Lock check:** Performed on every API call (via interceptor) and route navigation (via AuthGuard)
- **Lock release:** Immediate after consent submission

#### Skip/Attend Later:
- **Skip count tracking:** System tracks how many times employee skipped
- **Skip limit:** No hard limit mentioned, but mid-cycle deadline enforces completion
- **Skip behavior:** Lock lifted temporarily, restored on next login

#### Mid-Cycle Mandatory Completion:
- **Deadline configuration:** HR configures deadline (e.g., "End of 6th month", "End of mid-year")
- **Non-skippable lock:** After deadline, skip option disabled
- **Enforcement:** System checks deadline on login, applies non-skippable lock if past deadline

---

### **4. Integration Assumptions**

#### With Existing Systems:
- **Employee Management:** Uses existing Employee entity and repository
- **Role-Based Access:** Uses existing JobRoleAccess annotation for HR configuration screens
- **Feature Access:** Training module will be a new feature in FeatureMaster table
- **Audit Trail:** Uses existing LogDTO pattern for API logging
- **Session Management:** Integrates with existing UserSession mechanism

#### API Patterns:
- **REST APIs:** Following existing controller pattern (e.g., TimesheetController)
- **Service Layer:** Following existing service pattern (e.g., TimesheetService)
- **DTO Pattern:** Using DTOs for request/response (similar to TimesheetDTO pattern)
- **Response Format:** Using existing ServiceResponse wrapper

---

### **5. UI/UX Assumptions**

#### Training Page:
- **Full-screen modal/overlay:** Similar to LinkedIn notification popup pattern
- **Non-dismissible:** When locked, cannot close training page
- **Content viewer:** 
  - PPT: Embedded viewer or download option
  - Video: HTML5 video player
  - Audio: HTML5 audio player
  - Link: Opens in new tab (with tracking)
- **Timer display:** Shows elapsed time and remaining time (if applicable)
- **Consent button:** Disabled until minimum time completed, then enabled
- **Skip button:** Visible only if skip_allowed = true and before deadline

#### HR Configuration Screen:
- **CRUD operations:** Create, Read, Update, Delete trainings
- **Content management:** Upload/manage PPT/Video/Audio files, add external links
- **Content sequencing:** Drag-and-drop or up/down arrows for sequence
- **Bulk operations:** Assign training to multiple employees/departments

#### Employee Dashboard:
- **Training list:** Shows assigned trainings with status
- **Progress indicator:** Shows time completed vs minimum required
- **Due dates:** Shows deadline if configured

---

### **6. Content Handling Assumptions**

#### File Upload:
- **PPT files:** Uploaded as .ppt/.pptx, stored as BLOB or file path
- **Video files:** Common formats (MP4, WebM), size limit to be defined
- **Audio files:** Common formats (MP3, WAV), size limit to be defined
- **External Links:** Stored as URL string, opens in new tab

#### Content Display:
- **PPT:** Convert to images/slides or use embedded viewer
- **Video:** HTML5 video player with controls
- **Audio:** HTML5 audio player with controls
- **Link:** Clickable link that opens in new tab, time tracked when link is clicked

#### Content Sequence:
- **Ordered display:** Content shown in sequence_order
- **Navigation:** Next/Previous buttons between content items
- **Progress:** Shows "Content 2 of 5" indicator

---

### **7. Time Tracking Assumptions**

#### Server-Side Tracking:
- **Heartbeat mechanism:** Frontend sends periodic pings (every 30 seconds) to server
- **Session tracking:** Server maintains active session with start time
- **Accumulation:** Time accumulates across multiple sessions (if user closes and reopens)
- **Pause detection:** If no heartbeat for 5 minutes, session considered paused/inactive

#### Timer Behavior:
- **Continuous:** Timer runs only when training page is active/visible
- **Pause on tab switch:** If user switches tabs, timer pauses (detected via Page Visibility API)
- **Resume:** Timer resumes when user returns to training tab

---

### **8. Lock Enforcement Assumptions**

#### Lock Check Points:
1. **On Login:** Check if employee has pending mandatory training
2. **On Route Navigation:** AuthGuard checks lock status before allowing navigation
3. **On API Calls:** Interceptor checks lock status (except training-related APIs)
4. **On Page Load:** App component checks lock status

#### Lock Behavior:
- **Redirect:** If locked, redirect to training page
- **Disable navigation:** Sidebar/menu items disabled except logout
- **Disable API calls:** All APIs return 403 except training APIs
- **Visual indicator:** Show lock icon/banner on training page

---

### **9. Assignment Logic Assumptions**

#### Auto-Assignment:
- **On login:** System checks if employee needs training
- **Cron job:** Periodic job (daily/hourly) to assign new trainings
- **Assignment criteria:**
  - Training is mandatory
  - Training is active
  - Current date is within effective period
  - Employee completion count < frequency requirement
  - Employee is not exempted

#### Manual Assignment:
- **HR can assign:** HR can manually assign training to specific employees
- **Bulk assignment:** Assign to department, job role, or employee list

---

### **10. Audit & Reporting Assumptions**

#### Audit Data Stored:
- **Assignment records:** Who assigned, when, to whom
- **View time logs:** Detailed time tracking per session
- **Consent records:** When consent submitted, time spent
- **Skip records:** When skipped, skip count
- **Completion history:** All completion records with timestamps

#### Reporting:
- **Compliance dashboard:** HR can view compliance status
- **Employee reports:** Individual employee training history
- **Department reports:** Department-wise compliance
- **Export:** Export reports to Excel/PDF

---

## ❓ MANDATORY CLARIFICATION QUESTIONS

### **1. Time Tracking - Session Continuity**
**Question:** If an employee starts viewing training (spends 10 minutes), then closes browser and logs in again later, should the timer:
- **Option A:** Reset to 0 (must complete minimum time in single session)
- **Option B:** Accumulate (10 minutes + new session time = total)

**Impact:** Critical - Affects user experience and lock mechanism

---

### **2. Multiple Mandatory Trainings**
**Question:** If an employee has multiple mandatory trainings assigned simultaneously:
- **Option A:** Show one training at a time (complete Training A, then Training B)
- **Option B:** Show list of trainings, employee can choose order
- **Option C:** Show all trainings, must complete all before lock releases

**Impact:** Critical - Affects UI design and lock logic

---

### **3. Mid-Cycle Deadline Configuration**
**Question:** For "mandatory completion deadline" configuration:
- **Option A:** Fixed date (e.g., "2024-06-30")
- **Option B:** Relative date (e.g., "End of 6th month from assignment")
- **Option C:** Calendar-based (e.g., "End of mid-year", "End of quarter")

**Impact:** High - Affects configuration UI and deadline calculation logic

---

### **4. Content Time Tracking**
**Question:** For minimum view time, should time be tracked:
- **Option A:** Per content item (e.g., 5 min for PPT, 10 min for Video = 15 min total)
- **Option B:** Per training (e.g., 30 minutes total across all content)
- **Option C:** Per content type (different minimums for PPT vs Video)

**Impact:** High - Affects time tracking logic and configuration

---

### **5. External Link Time Tracking**
**Question:** For external links, how should time be tracked:
- **Option A:** Time from click to return (track when user clicks link and returns)
- **Option B:** Fixed time (e.g., count as 5 minutes when clicked)
- **Option C:** No time tracking (just mark as viewed when clicked)

**Impact:** Medium - Affects time tracking implementation

---

### **6. Skip Count Limit**
**Question:** Should there be a maximum skip count before forcing completion:
- **Option A:** No limit (only deadline enforces)
- **Option B:** Configurable limit (e.g., max 3 skips)
- **Option C:** Escalating restrictions (more skips = stricter lock)

**Impact:** Medium - Affects skip logic and configuration

---

### **7. Training Exemptions**
**Question:** How should training exemptions be handled:
- **Option A:** HR can mark specific employees as exempted per training
- **Option B:** Exemptions based on employee attributes (e.g., role, department)
- **Option C:** No exemptions (all employees must complete)

**Impact:** Medium - Affects assignment logic and HR configuration

---

### **8. Content File Size Limits**
**Question:** What are the file size limits for:
- PPT files: _____ MB
- Video files: _____ MB  
- Audio files: _____ MB

**Impact:** Medium - Affects file upload validation and storage

---

### **9. Training Assignment - New Employees**
**Question:** When should trainings be assigned to new employees:
- **Option A:** Immediately on employee creation
- **Option B:** After employee activation/onboarding
- **Option C:** On first login
- **Option D:** Configurable delay (e.g., 7 days after joining)

**Impact:** Low - Affects assignment logic

---

### **10. Training Deactivation**
**Question:** If a training is deactivated (active_status = false):
- **Option A:** Remove from all pending assignments
- **Option B:** Keep assignments but don't enforce
- **Option C:** Complete existing assignments, don't create new

**Impact:** Low - Affects training lifecycle management

---

## ✅ READINESS CONFIRMATION

### **Phase-1 Scope Confirmed:**
✅ Training Master Configuration (HR)  
✅ Training Content Management (PPT/Video/Audio/Link)  
✅ Mandatory Training Assignment  
✅ Minimum View Time Enforcement (Server-side)  
✅ Consent Submission  
✅ Lock Mechanism (Global portal lock)  
✅ Skip/Attend Later Option  
✅ Re-login Enforcement  
✅ Mid-Cycle Mandatory Completion  
✅ Frequency Enforcement (2 times/year)  
✅ Audit Trail  

### **Phase-1 Exclusions (Phase-2):**
❌ Quiz Configuration  
❌ Quiz Execution  
❌ Quiz Scoring  
❌ Quiz-based Completion  

---

## 🎯 DESIGN APPROACH

### **Architecture Pattern:**
Following existing codebase patterns:
- **Model:** JPA Entities (TrainingMaster, TrainingContent, TrainingAssignment, etc.)
- **Repository:** JPA Repositories with custom queries
- **Service:** Service layer with business logic
- **Controller:** REST Controllers with JobRoleAccess annotations
- **DTO:** Request/Response DTOs
- **Frontend:** Angular components with services

### **Key Design Decisions:**
1. **Lock Implementation:** Similar to LinkedIn notification popup pattern - non-dismissible modal/overlay
2. **Time Tracking:** Server-side with heartbeat mechanism (more secure than client-side)
3. **Content Storage:** BLOB in database (consistent with existing document storage pattern)
4. **Assignment:** Auto-assignment via cron job + manual assignment by HR
5. **Frequency:** Rolling 12-month window calculation

---

## 📝 NEXT STEPS

1. **Await clarifications** on the 10 mandatory questions above
2. **Database schema design** - Create ER diagram and table structures
3. **API design** - Define all REST endpoints
4. **UI/UX mockups** - Design training page and HR configuration screens
5. **Implementation plan** - Break down into development tasks

---

## ⚠️ NOTES

- All assumptions are based on existing codebase patterns
- Assumptions will be validated during design phase
- Any deviations will be documented and approved
- Phase-2 (Quiz) will be designed after Phase-1 completion

---

**Status:** ✅ **READY TO PROCEED WITH PHASE-1 DESIGN** (pending clarifications on mandatory questions)
