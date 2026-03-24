# Employee Training Journey - API & Business Logic Analysis

## ✅ API Availability Check

### **Employee Training APIs - Status: ✅ ALL PRESENT**

| API Endpoint | Method | Status | Purpose |
|-------------|--------|--------|---------|
| `/api/training/getPendingTraining` | POST | ✅ Present | Get employee's pending mandatory training |
| `/api/training/submitConsent` | POST | ✅ Present | Submit training consent/completion |
| `/api/training/skipTraining` | POST | ✅ Present | Skip training temporarily |
| `/api/training/getLockStatus` | POST | ✅ Present | Check if portal is locked due to pending training |
| `/api/training/downloadContent/{contentId}` | GET | ✅ Present | Download/view training content (PPT, PDF, VIDEO, AUDIO) |
| `/api/training/checkTrainingFrequency` | POST | ✅ Present | Check training completion frequency |

### **Frontend Components - Status: ✅ PRESENT**

| Component | File | Status | Purpose |
|-----------|------|--------|---------|
| Training Component | `frontend/src/app/training/training.component.ts` | ✅ Present | Employee training UI and logic |
| Training HTML | `frontend/src/app/training/training.component.html` | ✅ Present | Lock screen modal and training display |
| Training Service | `frontend/src/app/services/training.service.ts` | ✅ Present | API calls for training operations |

### **Backend Implementation - Status: ✅ ALL IMPLEMENTED**

| Service Method | File | Status | Purpose |
|---------------|------|--------|---------|
| `getPendingTraining()` | `TrainingServiceImpl.java` | ✅ Implemented | Find and return pending training |
| `submitConsent()` | `TrainingServiceImpl.java` | ✅ Implemented | Process consent submission |
| `skipTraining()` | `TrainingServiceImpl.java` | ✅ Implemented | Process skip request |
| `getLockStatus()` | `TrainingServiceImpl.java` | ✅ Implemented | Calculate lock status |
| `downloadContent()` | `TrainingServiceImpl.java` | ✅ Implemented | Serve training content files |
| `checkTrainingFrequency()` | `TrainingServiceImpl.java` | ✅ Implemented | Check completion frequency |

### **Helper Methods - Status: ✅ ALL IMPLEMENTED**

| Method | Purpose | Status |
|--------|---------|--------|
| `findPendingMandatoryTraining()` | Find pending training for employee | ✅ Implemented |
| `calculateCurrentCycle()` | Calculate current completion cycle | ✅ Implemented |
| `calculateCycleDeadline()` | Calculate deadline for current cycle | ✅ Implemented |
| `countCompletionsInLast12Months()` | Count completions in rolling 12 months | ✅ Implemented |
| `getLockStatusInternal()` | Internal lock status calculation | ✅ Implemented |

### **Security Integration - Status: ✅ CONFIGURED**

| Integration Point | Status | Details |
|-------------------|--------|---------|
| Training APIs Whitelisted | ✅ Done | APIs whitelisted in `EmployeePortalInterceptor` |
| Lock Check on Login | ✅ Done | Integrated in `EmployeeService.getEmployeeInfoOnLogin()` |
| Lock Check on API Calls | ✅ Done | Interceptor checks lock before API execution |

---

## 🎯 Core Business Logic - Employee Training Journey

### **1. Portal Lock Mechanism**

#### **Lock Calculation Logic:**
```
IF (Employee has pending mandatory training) AND
   (Training is active) AND
   (Training has lock_enabled = 'true') AND
   (Current active content exists) AND
   (No consent for CURRENT active content in current cycle)
THEN
   Portal is LOCKED
ELSE
   Portal is UNLOCKED
```

#### **Lock Check Points:**
1. **On Login:** `getEmployeeInfoOnLogin()` checks lock status
2. **On Route Navigation:** AuthGuard checks lock (if implemented)
3. **On API Calls:** `EmployeePortalInterceptor` checks lock before allowing API execution
4. **Training APIs:** Whitelisted - always accessible even when locked

#### **Lock Release:**
- Lock is released when employee submits consent for pending training
- Lock status is recalculated dynamically after consent submission
- No manual unlock needed - automatic based on completion

---

### **2. Pending Training Detection**

#### **Algorithm (`findPendingMandatoryTraining`):**
```
1. Get all active mandatory trainings (active_status = 'true', mandatory_flag = 'true')
2. For each training:
   a. Count employee's completions in last 12 months
   b. IF completion_count < frequency_per_year:
      - Get current active content (based on effective dates)
      - Calculate current cycle number (completion_count + 1)
      - Check if consent exists for CURRENT active content in current cycle
      - IF no consent exists:
         - Training is PENDING
         - Calculate deadline (if enabled)
         - Track training with earliest deadline
3. Return training with earliest deadline (or first pending if no deadlines)
```

#### **Key Rules:**
- **Content-Based Completion:** Consent is per content, not per training
- **Active Content Priority:** Only current active content matters
- **Cycle-Based Tracking:** Each completion cycle is tracked separately
- **Deadline Priority:** Training with earliest deadline is shown first
- **Content Change Handling:** If active content changes, employee must complete new content

---

### **3. Training Cycle Calculation**

#### **Current Cycle Logic:**
```
currentCycle = max(completion_cycle_number) + 1

Where:
- completion_cycle_number is from training_consent table
- Counts unique cycles completed in last 12 months
- If no consent exists, cycle = 1
- Each training completion increments cycle
```

#### **Cycle Deadline Calculation:**
Based on `deadlinePattern`:
- **YEARLY:** Deadline = Training effective_to date (or Dec 31 if null)
- **MID_YEAR:** Deadline = June 30 of current year
- **QUARTERLY:** Deadline = End of current quarter (Mar 31, Jun 30, Sep 30, Dec 31)
- **CUSTOM:** Deadline = Based on `customDeadlineMonths` (comma-separated months)

---

### **4. Content Active Status**

#### **Active Content Determination:**
```
Content is ACTIVE IF:
- active_status = 'true' AND
- current_date >= effective_from AND
- (effective_to IS NULL OR current_date <= effective_to)
```

#### **Content Priority:**
- Only ONE content is active at a time per training
- When new content is added with overlapping dates, old content is deactivated
- Employee must complete CURRENT active content, not previous content
- If content changes mid-cycle, employee must complete new content

---

### **5. Consent Submission Flow**

#### **Frontend Validation:**
1. **Minimum View Time Check:**
   - Timer tracks viewing time
   - Consent button disabled until minimum time reached
   - Progress bar shows viewing progress

2. **External Link Visit Check:**
   - For LINK content type, employee must click link
   - `hasVisitedLink` flag tracks link visit
   - Consent button enabled after link visit

3. **Consent Button State:**
   - Disabled initially
   - Enabled when:
     - Minimum time reached (if configured)
     - External link visited (if LINK type)
     - No time requirement (if minViewTimeMinutes = 0)

#### **Backend Validation:**
1. **Content Validation:**
   - Verifies contentId is CURRENT active content
   - Prevents submitting consent for outdated content

2. **Cycle Validation:**
   - Calculates/validates cycle number
   - Ensures consent is for correct cycle

3. **Duplicate Check:**
   - Checks if consent already exists
   - Returns success if duplicate (idempotent)

4. **Consent Creation:**
   - Creates `TrainingConsent` record
   - Links to training, content, employee, and cycle
   - Deletes skip record if exists

5. **Lock Status Update:**
   - Recalculates lock status after consent
   - Returns updated lock status to frontend

---

### **6. Skip Training Flow**

#### **Skip Conditions:**
- `skipAllowed = 'true'` for training
- Deadline not crossed (if deadline enabled)
- Can skip multiple times (skip count tracked)

#### **Skip Logic:**
1. Validates skip is allowed
2. Checks deadline (if enabled)
3. Creates/updates `TrainingSkip` record
4. Increments skip count
5. Recalculates lock status
6. Portal remains locked (skip doesn't release lock)

#### **Skip vs Consent:**
- **Skip:** Temporary, doesn't complete training, lock remains
- **Consent:** Permanent completion, releases lock, increments cycle

---

### **7. Content Viewing & Download**

#### **Content Types Supported:**
- **PPT/PPTX:** Download and view (PPTX parsed for preview)
- **PDF:** Download and view (iframe preview)
- **VIDEO:** Download and view (video player)
- **AUDIO:** Download and view (audio player)
- **LINK:** External link (must visit to enable consent)

#### **Content URL Construction:**
- File content: `/api/training/downloadContent/{contentId}`
- Link content: Direct external URL

#### **Content Access:**
- No authentication required for download (public endpoint)
- Content served with proper MIME types
- Filename preserved from original upload

---

### **8. Frequency Tracking**

#### **Completion Count Logic:**
```
Count = COUNT(DISTINCT completion_cycle_number) 
WHERE emp_id = ? 
  AND training_id = ? 
  AND consent_timestamp >= (NOW() - 12 months)
```

#### **Frequency Check:**
- Compares completion count with `frequency_per_year`
- If count < frequency: Training is pending
- If count >= frequency: Training is completed for current period

#### **Rolling Window:**
- Uses rolling 12-month window
- Older completions drop out after 12 months
- Ensures continuous compliance

---

### **9. Deadline Management**

#### **Deadline Calculation:**
- Calculated per cycle based on pattern
- Deadline shown in pending training response
- `isDeadlineCrossed` flag indicates if deadline passed

#### **Deadline Impact:**
- **Before Deadline:** Training can be skipped (if allowed)
- **After Deadline:** Training cannot be skipped, must complete
- **Deadline Priority:** Training with earliest deadline shown first

#### **Deadline Patterns:**
- **YEARLY:** End of training effective period
- **MID_YEAR:** June 30
- **QUARTERLY:** End of current quarter
- **CUSTOM:** Based on specified months

---

### **10. Employee Journey Flow**

#### **Step-by-Step Flow:**

1. **Employee Logs In**
   - `getLockStatus()` called automatically
   - If locked, lock screen modal opens
   - If unlocked, normal portal access

2. **Lock Screen Display**
   - Shows pending training details
   - Displays training name, type, cycle, deadline
   - Shows content type and name
   - Displays minimum view time requirement

3. **Content Access**
   - **LINK:** Button to visit external link
   - **File:** Button to download/view content
   - Content opens in new tab/window

4. **Viewing Time Tracking**
   - Timer starts automatically (if minViewTimeMinutes > 0)
   - Progress bar shows viewing progress
   - Consent button disabled until minimum time reached

5. **Consent Submission**
   - Employee clicks "Submit Consent" button
   - Frontend validates minimum time and link visit
   - Backend validates content and cycle
   - Consent record created
   - Lock status recalculated
   - Portal unlocked if no other pending trainings

6. **Skip Option (if allowed)**
   - Employee can skip temporarily
   - Skip record created/updated
   - Lock remains active
   - Employee can complete later

---

## 📋 Business Rules Summary

### **Training Assignment:**
- ✅ All mandatory trainings automatically assigned to all employees
- ✅ No explicit assignment needed
- ✅ Assignment determined by completion frequency

### **Training Status:**
- **PENDING:** No consent for current active content in current cycle
- **COMPLETED:** Consent exists for current active content in current cycle
- **SKIPPED:** Skip record exists for current cycle

### **Content Management:**
- ✅ Multiple content items per training allowed
- ✅ Only ONE content active at a time
- ✅ Active content determined by effective dates
- ✅ Content overlap automatically deactivates old content

### **Cycle Management:**
- ✅ Each training completion increments cycle
- ✅ Cycle tracked per training, not per content
- ✅ Current cycle = max(completed cycles) + 1

### **Lock Mechanism:**
- ✅ Lock calculated dynamically (no stored lock status)
- ✅ Lock based on pending mandatory training
- ✅ Lock released automatically on consent submission
- ✅ Training APIs always accessible (whitelisted)

### **Frequency Compliance:**
- ✅ Rolling 12-month window for completion tracking
- ✅ Frequency requirement per training
- ✅ Multiple completions per year allowed (based on frequency)

### **Deadline Management:**
- ✅ Deadline calculated per cycle
- ✅ Deadline patterns: YEARLY, MID_YEAR, QUARTERLY, CUSTOM
- ✅ Deadline crossing prevents skip
- ✅ Earliest deadline training shown first

---

## ⚠️ Potential Issues & Recommendations

### **1. Missing Validations:**
- ⚠️ Backend doesn't validate minimum view time (UI-only)
- ⚠️ Backend doesn't validate external link visit (UI-only)
- **Recommendation:** Add backend validation for security

### **2. Content Preview:**
- ✅ PDF preview works (iframe with sanitized URL)
- ✅ PPT preview works (PPTX parser)
- ✅ VIDEO/AUDIO preview works (HTML5 players)
- ⚠️ Large files may have issues (chunked upload plan exists)

### **3. Error Handling:**
- ✅ Frontend has error handling for API failures
- ✅ Backend has error handling and logging
- ⚠️ Network failures during consent submission not retried

### **4. Timer Accuracy:**
- ⚠️ Timer is client-side only (can be manipulated)
- ⚠️ No server-side time tracking
- **Recommendation:** Consider server-side time tracking for Phase 2

### **5. Skip Behavior:**
- ✅ Skip allowed before deadline
- ✅ Skip not allowed after deadline
- ⚠️ Skip count tracked but not used for any logic
- **Recommendation:** Consider skip limit in future

---

## ✅ Conclusion

**All APIs and related code are present and implemented.**

The employee training journey is fully functional with:
- ✅ Portal lock mechanism
- ✅ Pending training detection
- ✅ Content viewing and download
- ✅ Consent submission
- ✅ Skip functionality
- ✅ Deadline management
- ✅ Frequency tracking
- ✅ Cycle management

The core business logic handles all key scenarios:
- ✅ Multiple content per training (one active at a time)
- ✅ Content change handling
- ✅ Cycle-based completion tracking
- ✅ Rolling 12-month frequency window
- ✅ Deadline-based prioritization
- ✅ Lock/unlock mechanism

**Ready for testing the employee journey!**
