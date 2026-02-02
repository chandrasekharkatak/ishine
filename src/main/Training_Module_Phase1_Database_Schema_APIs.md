# Training Management Module - Phase 1
## Database Schema & API Design

---

## 📌 PHASE-1 KEY DESIGN DECISION

**Key Phase-1 Design Decisions:**

1. **No Assignment Table:**
   - Phase-1 trainings are **mandatory and automatically available to all employees**
   - No explicit assignment needed - training is "assigned" dynamically by checking completion frequency

2. **Multiple Content Per Training (One Active at a Time):**
   - Multiple content items can exist for a training (e.g., Video for Q1-Q2, PPT for Q3-Q4)
   - Only **ONE content is active at a time** (determined by effective dates)
   - Active content: `active_status = 'true'` AND current date BETWEEN `effective_from` AND `effective_to`
   - No content viewing tracking needed - if consent is submitted, content is assumed to be viewed

3. **Training Status:**
   - **PENDING:** No consent for current cycle
   - **COMPLETED:** Consent exists for current cycle
   - **SKIPPED:** Skip record exists for current cycle

---

## 📊 DATABASE SCHEMA DESIGN

### **Table 1: training_master**

**Purpose:** Stores training configuration defined by HR/Admin

**Columns:**

| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| training_id | INT | PK, AUTO_INCREMENT | Unique training identifier |
| training_name | VARCHAR(255) | NOT NULL | Name of the training |
| training_type | VARCHAR(100) | NOT NULL | Type (Induction/POSH/CyberSecurity/etc.) |
| mandatory_flag | VARCHAR(10) | NOT NULL, DEFAULT 'false' | 'true'/'false' - Is training mandatory |
| effective_from | DATE | NOT NULL | Training effective start date |
| effective_to | DATE | NULL | Training effective end date (NULL = no expiry) |
| frequency_per_year | INT | NOT NULL, DEFAULT 2 | Number of times training must be completed per year |
| lock_enabled | VARCHAR(10) | NOT NULL, DEFAULT 'false' | 'true'/'false' - Enable lock for this training |
| min_view_time_minutes | INT | NULL | Minimum view time in minutes (NULL = no time requirement) |
| consent_required | VARCHAR(10) | NOT NULL, DEFAULT 'true' | 'true'/'false' - Is consent required |
| skip_allowed | VARCHAR(10) | NOT NULL, DEFAULT 'true' | 'true'/'false' - Can employee skip this training |
| mandatory_completion_deadline | DATE | NULL | Deadline for mandatory completion (NULL = no deadline) |
| active_status | VARCHAR(10) | NOT NULL, DEFAULT 'true' | 'true'/'false' - Is training active |
| created_by | BIGINT | NOT NULL | Employee ID who created |
| created_on | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updated_by | BIGINT | NULL | Employee ID who last updated |
| updated_on | TIMESTAMP | NULL | Last update timestamp |

**Indexes:**
- PRIMARY KEY (training_id)
- INDEX idx_training_active (active_status, mandatory_flag)
- INDEX idx_training_effective (effective_from, effective_to)
- INDEX idx_training_lock (lock_enabled, mandatory_flag, active_status)

---

### **Table 2: training_content**

**Purpose:** Stores content items (PPT/Video/Audio/Link) for each training - multiple content allowed but only one active at a time

**Columns:**

| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| content_id | INT | PK, AUTO_INCREMENT | Unique content identifier |
| training_id | INT | FK, NOT NULL | Reference to training_master |
| content_type | VARCHAR(50) | NOT NULL | 'PPT'/'VIDEO'/'AUDIO'/'LINK' |
| content_name | VARCHAR(255) | NOT NULL | Display name for content |
| content_path | VARCHAR(1000) | NULL | File path/URL (NULL for LINK type) |
| external_link_url | VARCHAR(1000) | NULL | External URL (only for LINK type) |
| effective_from | DATE | NOT NULL | Content effective start date |
| effective_to | DATE | NULL | Content effective end date (NULL = no expiry) |
| file_size_bytes | BIGINT | NULL | File size in bytes (for PPT/Video/Audio) |
| mime_type | VARCHAR(100) | NULL | MIME type (for PPT/Video/Audio) |
| active_status | VARCHAR(10) | NOT NULL, DEFAULT 'true' | 'true'/'false' - Is content active |
| created_by | BIGINT | NOT NULL | Employee ID who created |
| created_on | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updated_by | BIGINT | NULL | Employee ID who last updated |
| updated_on | TIMESTAMP | NULL | Last update timestamp |

**Indexes:**
- PRIMARY KEY (content_id)
- FOREIGN KEY (training_id) REFERENCES training_master(training_id) ON DELETE CASCADE
- INDEX idx_content_training_active (training_id, active_status, effective_from, effective_to)
- INDEX idx_content_effective (effective_from, effective_to, active_status)

**Constraints:**
- CHECK: (content_type = 'LINK' AND external_link_url IS NOT NULL) OR (content_type != 'LINK' AND content_path IS NOT NULL)

**Business Rules:**
- Multiple content items can exist for a training
- Only ONE content is active at a time (determined by effective dates and active_status)
- Active content: `active_status = 'true'` AND current date BETWEEN `effective_from` AND `effective_to` (or `effective_to IS NULL`)
- Example: POSH Training can have Video (Jan-Jun), PPT (Jul-Dec), another Video (next year), etc.

---

### **Table 3: training_consent**

**Purpose:** Records consent submissions per content for frequency tracking and audit

**Columns:**

| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| consent_id | BIGINT | PK, AUTO_INCREMENT | Unique consent identifier |
| training_id | INT | FK, NOT NULL | Reference to training_master (denormalized for queries) |
| content_id | INT | FK, NOT NULL | Reference to training_content - **Consent is per content** |
| emp_id | BIGINT | FK, NOT NULL | Reference to employee |
| consent_timestamp | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | When consent was submitted |
| completion_cycle_number | INT | NOT NULL | Cycle number when completed (for frequency calculation) |
| created_by | BIGINT | NOT NULL | Employee ID (same as emp_id) |

**Indexes:**
- PRIMARY KEY (consent_id)
- FOREIGN KEY (training_id) REFERENCES training_master(training_id) ON DELETE CASCADE
- FOREIGN KEY (content_id) REFERENCES training_content(content_id) ON DELETE CASCADE
- FOREIGN KEY (emp_id) REFERENCES employee(emp_id) ON DELETE CASCADE
- INDEX idx_consent_emp_training (emp_id, training_id, consent_timestamp)
- INDEX idx_consent_emp_content (emp_id, content_id)
- INDEX idx_consent_frequency (emp_id, training_id, completion_cycle_number)
- UNIQUE (training_id, content_id, emp_id, completion_cycle_number) - One consent per content per employee per cycle

**Purpose:**
- Tracks completion per content (not just per training)
- If active content changes, employee must complete new content even if previous content was completed
- Frequency calculation: Counts unique (training_id, completion_cycle_number) combinations in last 12 months
- Audit trail for compliance
- Used to calculate rolling 12-month completion count

**Business Logic:**
- Consent is tied to specific content - if content changes, new consent required
- Example: Employee completes Video-1 in January, HR adds Video-2 in February → Employee must complete Video-2
- Frequency is still per training (2 times/year), but each completion must be for the active content at that time

**Note:** 
- Phase-1: All mandatory trainings are automatically available to all employees
- No explicit assignment needed - training is "assigned" by checking if employee needs it (frequency < required)

---

### **Table 4: training_skip**

**Purpose:** Tracks training skips for employees (temporary skip tracking)

**Columns:**

| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| skip_id | BIGINT | PK, AUTO_INCREMENT | Unique skip identifier |
| training_id | INT | FK, NOT NULL | Reference to training_master |
| emp_id | BIGINT | FK, NOT NULL | Reference to employee |
| cycle_number | INT | NOT NULL | Cycle number when skipped (for tracking) |
| skip_count | INT | NOT NULL, DEFAULT 1 | Number of times skipped in this cycle (incremented on each skip) |
| first_skipped_on | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | When training was first skipped in this cycle |
| last_skipped_on | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | When training was last skipped (updated on each skip) |

**Indexes:**
- PRIMARY KEY (skip_id)
- FOREIGN KEY (training_id) REFERENCES training_master(training_id) ON DELETE CASCADE
- FOREIGN KEY (emp_id) REFERENCES employee(emp_id) ON DELETE CASCADE
- INDEX idx_skip_emp_training (emp_id, training_id, cycle_number)
- UNIQUE (training_id, emp_id, cycle_number) - One skip record per training per cycle

**Purpose:**
- Tracks skip count per cycle (incremented on each skip, not new record)
- Used to determine if training was skipped
- Skip is temporary - lock restored on next login if training still pending
- **Update behavior:** On skip, update existing record: increment `skip_count`, update `last_skipped_on`

**Business Logic:**
- If skip record exists: UPDATE `skip_count = skip_count + 1`, `last_skipped_on = CURRENT_TIMESTAMP`
- If skip record doesn't exist: INSERT new record with `skip_count = 1`, `first_skipped_on = CURRENT_TIMESTAMP`, `last_skipped_on = CURRENT_TIMESTAMP`

**Note:** Phase-1: Each training has only ONE content item. Content viewing tracking not required - if consent is submitted, content is assumed to be viewed.

---

### **ER Diagram Relationships:**

```
training_master (1) ----< (N) training_content  (Multiple content, one active at a time)
training_master (1) ----< (N) training_consent   (Denormalized - for queries)
training_content (1) ----< (N) training_consent (Consent is per content)
training_master (1) ----< (N) training_skip
employee (1) ----< (N) training_consent
employee (1) ----< (N) training_skip
```

**Note:** 
- Phase-1: No explicit assignment table - all mandatory trainings are automatically available to all employees
- Phase-1: Multiple content items per training, but only ONE active at a time (based on effective dates)
- Active content determined by: `active_status = 'true'` AND current date BETWEEN `effective_from` AND `effective_to`
- **Consent is per content:** If active content changes, employee must complete new content even if previous content was completed
- Training status determined by:
  - **PENDING:** No consent for current active content in current cycle
  - **COMPLETED:** Consent exists for current active content in current cycle
  - **SKIPPED:** Skip record exists for current cycle

---

## 🔌 API DESIGN

### **API Group 1: Training Configuration (HR/Admin)**

#### **1.1 Create Training**
**Endpoint:** `POST /api/training/createTraining`  
**Access:** `@JobRoleAccess(featureIds = {XX})` - HR/Admin only  
**Request Body:**
```json
{
  "trainingName": "POSH Training 2024",
  "trainingType": "POSH",
  "mandatoryFlag": "true",
  "effectiveFrom": "2024-01-01",
  "effectiveTo": "2024-12-31",
  "frequencyPerYear": 2,
  "lockEnabled": "true",
  "minViewTimeMinutes": 30,
  "consentRequired": "true",
  "skipAllowed": "true",
  "deadlineEnabled": "true",
  "deadlinePattern": "MID_YEAR",
  "createdBy": 123
}
```
**Response:**
```json
{
  "serviceStatus": "Success",
  "serviceResponse": {
    "trainingId": 1,
    "trainingName": "POSH Training 2024",
    "message": "Training created successfully"
  }
}
```

---

#### **1.2 Update Training**
**Endpoint:** `POST /api/training/updateTraining`  
**Access:** `@JobRoleAccess(featureIds = {XX})` - HR/Admin only  
**Request Body:**
```json
{
  "trainingId": 1,
  "trainingName": "POSH Training 2024 Updated",
  "effectiveTo": "2024-12-31",
  "minViewTimeMinutes": 45,
  "activeStatus": "true",
  "updatedBy": 123
}
```
**Response:**
```json
{
  "serviceStatus": "Success",
  "serviceResponse": "Training updated successfully"
}
```

---

#### **1.3 Get All Trainings**
**Endpoint:** `GET /api/training/getAllTrainings`  
**Access:** `@JobRoleAccess(featureIds = {XX})` - HR/Admin only  
**Query Parameters:** `activeStatus` (optional), `mandatoryFlag` (optional)  
**Response:**
```json
{
  "serviceStatus": "Success",
  "serviceResponse": [
    {
      "trainingId": 1,
      "trainingName": "POSH Training 2024",
      "trainingType": "POSH",
      "mandatoryFlag": "true",
      "effectiveFrom": "2024-01-01",
      "effectiveTo": "2024-12-31",
      "frequencyPerYear": 2,
      "lockEnabled": "true",
      "minViewTimeMinutes": 30,
      "activeStatus": "true",
      "createdByName": "John Doe",
      "createdOn": "2024-01-01 10:00:00"
    }
  ]
}
```

---

#### **1.4 Add Training Content**
**Endpoint:** `POST /api/training/addTrainingContent`  
**Access:** `@JobRoleAccess(featureIds = {XX})` - HR/Admin only  
**Request:** `multipart/form-data`
- `trainingId`: INT
- `contentType`: String (PPT/VIDEO/AUDIO/LINK)
- `contentName`: String
- `effectiveFrom`: DATE (required)
- `effectiveTo`: DATE (optional, NULL = no expiry)
- `file`: MultipartFile (for PPT/VIDEO/AUDIO)
- `externalLinkUrl`: String (for LINK type)
- `createdBy`: Long

**Response:**
```json
{
  "serviceStatus": "Success",
  "serviceResponse": {
    "contentId": 1,
    "contentPath": "/uploads/training/1/content_1.pdf",
    "message": "Content added successfully"
  }
}
```
**Logic:**
- Creates new content item for training
- Validates that new content's effective period doesn't overlap with existing active content
- **Overlap Detection:** Checks if any existing content has:
  - `active_status = 'true'`
  - Date ranges overlap: `(effective_from <= new_effective_to) AND (effective_to >= new_effective_from OR effective_to IS NULL)`
- If overlap exists and new content is set to active:
  - Deactivates overlapping content (sets `active_status = 'false'`)
  - Ensures only ONE content is active at a time
- Returns error if trying to activate content that overlaps with existing active content (unless auto-deactivation is allowed)

---

#### **1.5 Update Training Content**
**Endpoint:** `POST /api/training/updateTrainingContent`  
**Access:** `@JobRoleAccess(featureIds = {XX})` - HR/Admin only  
**Request Body:**
```json
{
  "contentId": 1,
  "contentName": "Updated POSH Video",
  "effectiveFrom": "2024-01-01",
  "effectiveTo": "2024-06-30",
  "activeStatus": "true",
  "updatedBy": 123
}
```
**Response:**
```json
{
  "serviceStatus": "Success",
  "serviceResponse": "Content updated successfully"
}
```
**Logic:**
- Updates content details
- If effective dates changed, validates no overlap with other active content
- If setting to active, deactivates other overlapping content

---

#### **1.6 Get All Content for Training**
**Endpoint:** `GET /api/training/getTrainingContent/{trainingId}`  
**Access:** `@JobRoleAccess(featureIds = {XX})` - HR/Admin only  
**Response:**
```json
{
  "serviceStatus": "Success",
  "serviceResponse": [
    {
      "contentId": 1,
      "contentType": "VIDEO",
      "contentName": "POSH Video 2024 Q1",
      "effectiveFrom": "2024-01-01",
      "effectiveTo": "2024-06-30",
      "activeStatus": "true",
      "isCurrentlyActive": true
    },
    {
      "contentId": 2,
      "contentType": "PPT",
      "contentName": "POSH PPT 2024 Q2",
      "effectiveFrom": "2024-07-01",
      "effectiveTo": "2024-12-31",
      "activeStatus": "true",
      "isCurrentlyActive": false
    }
  ]
}
```

---

**Note:** Phase-1 does not require explicit assignment API. All mandatory trainings are automatically available to all employees. Assignment is determined dynamically by checking if employee needs training (frequency < required).

---

### **API Group 2: Employee Training Operations**

#### **2.1 Get Employee's Pending Training**
**Endpoint:** `POST /api/training/getPendingTraining`  
**Access:** Authenticated employees  
**Request Body:**
```json
{
  "empId": 100
}
```
**Response:**
```json
{
  "serviceStatus": "Success",
  "serviceResponse": {
    "trainingId": 1,
    "trainingName": "POSH Training 2024",
    "trainingType": "POSH",
    "minViewTimeMinutes": 30,
    "consentRequired": "true",
    "skipAllowed": "true",
    "deadlineEnabled": "true",
    "deadlinePattern": "MID_YEAR",
    "currentCycleNumber": 1,
    "currentCycleDeadline": "2024-06-30",
    "isDeadlineCrossed": false,
    "content": {
      "contentId": 1,
      "contentType": "VIDEO",
      "contentName": "POSH Video 2024 Q1",
      "contentPath": "/uploads/training/1/video_1.mp4",
      "effectiveFrom": "2024-01-01",
      "effectiveTo": "2024-06-30"
    }
  }
}
```
**Logic:**
- Returns ONE pending mandatory training (priority: earliest deadline, then training creation date)
- Determines pending by checking:
  - Training is active and mandatory
  - Employee completion count < frequency_per_year (in last 12 months)
  - **Current active content has NOT been completed** (no consent exists for current active content in current cycle)
- Returns the **currently active content** for the training (based on effective dates)
- Active content: `active_status = 'true'` AND current date BETWEEN `effective_from` AND `effective_to`
- **Key:** Checks if employee has consent for CURRENT active content, not just any content
- If active content changes, employee must complete new content even if previous content was completed
- Returns null if no pending training or no active content
- Used to check lock status

---

**Note:** Phase-1: No "start training" API needed. Status is determined by:
- **PENDING:** No consent for current cycle
- **COMPLETED:** Consent exists for current cycle
- **SKIPPED:** Skip record exists for current cycle

---

---

**Note:** Phase-1: No "mark content viewed" API needed. Since each training has only ONE content, if consent is submitted, content is assumed to be viewed.

---
**Logic:**
- Creates/updates training_content_view record
- For LINK type, marks external_link_clicked = true
- Used for external link tracking

---

#### **2.4 Submit Training Consent**
**Endpoint:** `POST /api/training/submitConsent`  
**Access:** Authenticated employees  
**Request Body:**
```json
{
  "trainingId": 1,
  "contentId": 2,
  "empId": 100,
  "cycleNumber": 1
}
```
**Response:**
```json
{
  "serviceStatus": "Success",
  "serviceResponse": {
    "consentId": 1,
    "lockReleased": true,
    "message": "Consent submitted successfully. Lock released."
  }
}
```
**Logic:**
- Validates that `contentId` is the CURRENT active content for the training
- Validates minimum view time completed (UI-level check, backend validates consent)
- Creates training_consent record with `content_id` and `completion_cycle_number`
- **Consent is per content** - if content changes, new consent required
- Calculates completion_cycle_number based on existing consents for this training (max + 1)
- Releases lock if this was the only pending training
- Returns lock status
- Deletes any skip record for this training/cycle if exists
- **Important:** If active content changed after employee started, they must complete new content

---

#### **2.5 Skip Training**
**Endpoint:** `POST /api/training/skipTraining`  
**Access:** Authenticated employees  
**Request Body:**
```json
{
  "trainingId": 1,
  "empId": 100,
  "cycleNumber": 1
}
```
**Response:**
```json
{
  "serviceStatus": "Success",
  "serviceResponse": {
    "skipId": 1,
    "skipCount": 2,
    "lastSkippedOn": "2024-02-15 10:30:00",
    "status": "SKIPPED",
    "lockReleased": true,
    "message": "Training skipped. Lock temporarily released."
  }
}
```
**Logic:**
- Validates skip_allowed = true
- Calculates current cycle deadline dynamically
- Validates deadline not crossed (if deadline enabled and deadline crossed, skip disabled)
- **Check if skip record exists for (training_id, emp_id, cycle_number)**
- **If exists:** UPDATE record - increment `skip_count`, update `last_skipped_on = CURRENT_TIMESTAMP`
- **If not exists:** INSERT new record with `skip_count = 1`, `first_skipped_on = CURRENT_TIMESTAMP`, `last_skipped_on = CURRENT_TIMESTAMP`
- Temporarily releases lock (will be restored on next login)
- Returns lock status and updated skip count

---

#### **2.6 Get Training Lock Status**
**Endpoint:** `POST /api/training/getLockStatus`  
**Access:** Authenticated employees  
**Request Body:**
```json
{
  "empId": 100
}
```
**Response:**
```json
{
  "serviceStatus": "Success",
  "serviceResponse": {
    "isLocked": true,
    "lockedTrainingId": 1,
    "lockedTrainingName": "POSH Training 2024",
    "lockReason": "Mandatory training pending",
    "canSkip": true,
    "deadlineCrossed": false
  }
}
```
**Logic:**
- Checks if employee has any pending mandatory training with lock_enabled = true
- Returns lock status and training details
- Used by interceptor/AuthGuard to enforce lock

---

#### **2.7 Download Training Content**
**Endpoint:** `GET /api/training/downloadContent/{contentId}`  
**Access:** Authenticated employees  
**Response:** File download (PDF/Video/Audio)  
**Logic:**
- Validates employee has assignment for this training
- Returns file from file system
- Logs download for audit

---

### **API Group 3: Training Assignment & Frequency**

#### **3.1 Check Training Requirements (Cron Job)**
**Endpoint:** `POST /api/training/checkTrainingRequirements`  
**Access:** Internal/Cron only  
**Request Body:**
```json
{
  "empId": null  // null = process all employees
}
```
**Response:**
```json
{
  "serviceStatus": "Success",
  "serviceResponse": {
    "employeesChecked": 500,
    "pendingTrainingsFound": 150,
    "message": "Training requirements check completed"
  }
}
```
**Logic:**
- Finds active mandatory trainings
- For each employee, checks if training needed (completion count < frequency_per_year in last 12 months)
- No explicit assignment needed - training is "available" if needed
- Runs via scheduled cron job (optional - can be done on-demand during login)
- Used for reporting/compliance purposes

---

#### **3.2 Check Training Frequency**
**Endpoint:** `POST /api/training/checkTrainingFrequency`  
**Access:** Authenticated employees  
**Request Body:**
```json
{
  "empId": 100,
  "trainingId": 1
}
```
**Response:**
```json
{
  "serviceStatus": "Success",
  "serviceResponse": {
    "trainingId": 1,
    "completionCount": 1,
    "requiredFrequency": 2,
    "needsAssignment": true,
    "lastCompletedOn": "2024-01-15 10:30:00"
  }
}
```
**Logic:**
- Counts consent records in last 12 months
- Returns completion count vs required frequency
- Used for auto-assignment logic

---

### **API Group 4: Training Deactivation**

#### **4.1 Deactivate Training**
**Endpoint:** `POST /api/training/deactivateTraining`  
**Access:** `@JobRoleAccess(featureIds = {XX})` - HR/Admin only  
**Request Body:**
```json
{
  "trainingId": 1,
  "updatedBy": 123
}
```
**Response:**
```json
{
  "serviceStatus": "Success",
  "serviceResponse": {
    "trainingId": 1,
    "locksReleased": 45,
    "message": "Training deactivated. 45 employee locks released."
  }
}
```
**Logic:**
- Sets active_status = false
- Releases locks for affected employees (those with pending training)
- Does NOT delete historical records (consent, skip, content_view)
- Training no longer appears as pending for any employee

---

### **API Group 5: Reporting & Audit**

#### **5.1 Get Employee Training History**
**Endpoint:** `POST /api/training/getEmployeeTrainingHistory`  
**Access:** `@JobRoleAccess(featureIds = {XX})` - HR/Admin or Self  
**Request Body:**
```json
{
  "empId": 100,
  "trainingId": null  // null = all trainings
}
```
**Response:**
```json
{
  "serviceStatus": "Success",
  "serviceResponse": [
    {
      "trainingId": 1,
      "trainingName": "POSH Training 2024",
      "completedOn": "2024-01-15 10:30:00",
      "status": "COMPLETED",
      "cycleNumber": 1,
      "skipCount": 0,
      "completionCount": 1
    }
  ]
}
```
**Logic:**
- Combines data from training_consent and training_skip tables
- Status determined by: COMPLETED if consent exists, SKIPPED if skip exists, PENDING otherwise

---

#### **5.2 Get Training Compliance Report**
**Endpoint:** `POST /api/training/getComplianceReport`  
**Access:** `@JobRoleAccess(featureIds = {XX})` - HR/Admin only  
**Request Body:**
```json
{
  "trainingId": 1,
  "departmentId": null,  // null = all departments
  "status": "PENDING"  // PENDING/COMPLETED/SKIPPED
}
```
**Response:**
```json
{
  "serviceStatus": "Success",
  "serviceResponse": {
    "trainingId": 1,
    "trainingName": "POSH Training 2024",
    "totalAssigned": 500,
    "completed": 350,
    "pending": 100,
    "skipped": 50,
    "compliancePercentage": 70.0
  }
}
```

---

## 🔒 LOCK MECHANISM IMPLEMENTATION

### **Lock Calculation Logic:**

```java
public boolean isEmployeeLocked(Long empId) {
    // Find pending mandatory training with lock enabled
    TrainingMaster pendingTraining = findPendingMandatoryTraining(empId);
    
    if (pendingTraining == null) {
        return false; // No pending training, not locked
    }
    
    // Check if training is still active
    if (!"true".equals(pendingTraining.getActiveStatus())) {
        return false; // Training deactivated, lock released
    }
    
    // Check if lock is enabled for this training
    if (!"true".equals(pendingTraining.getLockEnabled())) {
        return false; // Lock not enabled
    }
    
    // Get current active content for this training
    TrainingContent activeContent = findActiveContent(pendingTraining.getTrainingId());
    if (activeContent == null) {
        return false; // No active content, not locked
    }
    
    // Determine current cycle number
    int currentCycle = calculateCurrentCycle(empId, pendingTraining.getTrainingId());
    
    // Check if consent submitted for CURRENT ACTIVE CONTENT in current cycle (lock release condition)
    TrainingConsent consent = findConsentForContentAndCycle(
        empId, 
        pendingTraining.getTrainingId(), 
        activeContent.getContentId(), 
        currentCycle
    );
    if (consent != null) {
        return false; // Consent submitted for current active content, lock released
    }
    
    return true; // Locked - employee hasn't completed current active content
}

private TrainingContent findActiveContent(Integer trainingId) {
    LocalDate today = LocalDate.now();
    return trainingContentRepository.findByTrainingIdAndActiveStatusAndEffectiveDates(
        trainingId, 
        "true", 
        today
    );
    // Query: WHERE training_id = ? AND active_status = 'true' 
    //   AND effective_from <= ? AND (effective_to >= ? OR effective_to IS NULL)
}

private TrainingMaster findPendingMandatoryTraining(Long empId) {
    // Find active mandatory trainings
    List<TrainingMaster> mandatoryTrainings = trainingMasterRepository
        .findByMandatoryFlagAndActiveStatus("true", "true");
    
    TrainingMaster pendingTraining = null;
    LocalDate earliestDeadline = null;
    
    // Filter by: completion count < frequency_per_year in last 12 months
    for (TrainingMaster training : mandatoryTrainings) {
        int completionCount = countCompletionsInLast12Months(empId, training.getTrainingId());
        if (completionCount < training.getFrequencyPerYear()) {
            // Get current active content for this training
            TrainingContent activeContent = findActiveContent(training.getTrainingId());
            if (activeContent == null) {
                continue; // No active content, skip this training
            }
            
            // Determine current cycle number
            int currentCycle = completionCount + 1;
            
            // Check if employee has completed CURRENT ACTIVE CONTENT for current cycle
            TrainingConsent consent = findConsentForContentAndCycle(
                empId, 
                training.getTrainingId(), 
                activeContent.getContentId(), 
                currentCycle
            );
            
            // If consent exists for current active content, training is not pending
            if (consent != null) {
                continue; // Employee completed current active content
            }
            
            // Calculate deadline for current cycle if deadline enabled
            LocalDate cycleDeadline = null;
            if ("true".equals(training.getDeadlineEnabled()) && training.getDeadlinePattern() != null) {
                cycleDeadline = calculateCycleDeadline(training, currentCycle);
                
                // If deadline crossed, this training must be completed (non-skippable)
                if (cycleDeadline != null && LocalDate.now().isAfter(cycleDeadline)) {
                    return training; // Deadline crossed, must complete immediately
                }
            }
            
            // Track training with earliest deadline
            if (cycleDeadline != null) {
                if (earliestDeadline == null || cycleDeadline.isBefore(earliestDeadline)) {
                    earliestDeadline = cycleDeadline;
                    pendingTraining = training;
                }
            } else if (pendingTraining == null) {
                // No deadline, use first pending training
                pendingTraining = training;
            }
        }
    }
    return pendingTraining;
}
```

### **Lock Check Points:**

1. **On Login:** `getEmployeeInfoOnLogin()` - Check and return lock status
2. **On Route Navigation:** `AuthGuard.canActivate()` - Check lock, redirect if locked
3. **On API Calls:** `EmployeePortalInterceptor.preHandle()` - Check lock, return 403 if locked (except training APIs)
4. **Training APIs:** Whitelisted in interceptor - Always allowed

**Interceptor Integration:**
- Add training APIs to `WHITELISTED_APIS` list in `EmployeePortalInterceptor.java`:
  ```java
  "/api/training/getPendingTraining",
  "/api/training/submitConsent",
  "/api/training/skipTraining",
  "/api/training/getLockStatus",
  "/api/training/downloadContent/*",
  "/api/training/checkTrainingFrequency"
  ```
- For locked employees, all other APIs return 403 (except whitelisted training APIs)
- Lock check logic calls `TrainingService.getLockStatus(empId)` before allowing API execution

---

## 📝 KEY DESIGN DECISIONS

### **1. No Time Tracking Tables**
- Time enforcement is UI-only
- Backend validates consent submission only
- No time persistence for Phase-1

### **2. No Assignment Table (Phase-1)**
- All mandatory trainings automatically available to all employees
- No explicit assignment needed
- Training "assigned" dynamically by checking completion frequency
- Status determined by: consent exists (COMPLETED), content viewed (IN_PROGRESS), skip exists (SKIPPED), or none (PENDING)

### **3. Lock is Calculated Dynamically**
- No separate lock_status table
- Lock calculated on-demand based on:
  - Active mandatory training exists (completion count < frequency)
  - Training has lock_enabled = true
  - Training is active
  - **Current active content exists**
  - **Consent not submitted for CURRENT ACTIVE CONTENT in current cycle**

### **4. Frequency Calculation**
- Uses `training_consent` table
- Counts **unique (training_id, completion_cycle_number)** combinations in rolling 12-month window
- Each consent is per content, but frequency is per training
- `completion_cycle_number` helps track cycles
- Current cycle = max(completion_cycle_number) + 1 if no consent exists for current active content
- **Important:** If active content changes, employee must complete new content even if previous content was completed

### **5. Multiple Content Per Training (One Active at a Time)**
- Phase-1: Multiple content items per training allowed
- Only ONE content is active at a time (determined by effective dates)
- Active content: `active_status = 'true'` AND current date BETWEEN `effective_from` AND `effective_to`
- Content can be scheduled for different periods (e.g., Video for Q1-Q2, PPT for Q3-Q4)
- No content viewing tracking needed - if consent is submitted, content is assumed to be viewed
- Content overlap validation: When adding new content, overlapping active content is deactivated

### **6. One Training at a Time**
- `getPendingTraining()` API returns single training
- Priority: earliest deadline, then training creation date
- Next training surfaced only after current completion

### **7. Skip Logic**
- Skip tracked in `training_skip` table - **ONE record per training/employee/cycle**
- **Update behavior:** On each skip, update existing record (increment `skip_count`, update `last_skipped_on`)
- **No new entry:** Same record is updated, not multiple records created
- Lock temporarily released
- Skip count incremented on each skip (tracks total skips in cycle)
- `first_skipped_on` tracks when first skipped, `last_skipped_on` tracks most recent skip
- Skip disabled after deadline crosses (deadline calculated dynamically per cycle)
- Skip record deleted when consent submitted

### **8. Deadline Handling (Dynamic)**
- **No single deadline column** - deadlines calculated dynamically based on pattern and cycle
- **Deadline patterns:** MID_YEAR, YEAR_END, QUARTERLY, CUSTOM
- **Per-cycle deadlines:** Each cycle has its own deadline (e.g., Cycle 1 = June 30, Cycle 2 = December 31)
- **No database updates needed** - deadlines calculated on-demand
- **Deadline enforcement:** After deadline crosses, skip option disabled, lock becomes non-dismissible

### **8. Deactivation Behavior**
- Immediate lock release
- Training no longer appears as pending
- Historical records preserved (consent, skip, content_view)

### **8. File Storage Implementation**
- **File Location Configuration:** Use `@Value("${file.location.documents.training}")` pattern
- **Storage Pattern:** 
  - Files stored in file system: `{trainingFileLocation}/{trainingId}/content_{contentId}_{timestamp}_{originalFilename}`
  - Database stores relative path: `/training/{trainingId}/content_{contentId}_{timestamp}_{originalFilename}`
  - Use `Paths.get()` and `Files.write()` for file operations
  - Use `Files.createDirectories()` to ensure directory exists
- **File Naming:** `{timestamp}_{originalFilename}` to avoid conflicts
- **File Download:** Use `FileSystemResource` or `Resource` for serving files

---

## 🎯 API SUMMARY TABLE

| API Endpoint | Method | Purpose | Access | Returns |
|-------------|--------|---------|--------|---------|
| `/api/training/createTraining` | POST | Create training | HR/Admin | Training ID |
| `/api/training/updateTraining` | POST | Update training | HR/Admin | Success message |
| `/api/training/getAllTrainings` | GET | List all trainings | HR/Admin | Training list |
| `/api/training/addTrainingContent` | POST | Add content | HR/Admin | Content ID |
| `/api/training/updateTrainingContent` | POST | Update content | HR/Admin | Success message |
| `/api/training/getTrainingContent/{id}` | GET | Get all content for training | HR/Admin | Content list |
| `/api/training/checkTrainingRequirements` | POST | Check requirements (cron) | Internal | Check results |
| `/api/training/getPendingTraining` | POST | Get employee's pending training | Employee | Training details |
| `/api/training/submitConsent` | POST | Submit consent | Employee | Lock status |
| `/api/training/skipTraining` | POST | Skip training | Employee | Lock status |
| `/api/training/getLockStatus` | POST | Check lock status | Employee | Lock details |
| `/api/training/downloadContent/{id}` | GET | Download content file | Employee | File |
| `/api/training/checkTrainingFrequency` | POST | Check frequency | Employee | Completion count |
| `/api/training/deactivateTraining` | POST | Deactivate training | HR/Admin | Locks released |
| `/api/training/getEmployeeTrainingHistory` | POST | Get history | HR/Admin/Self | History list |
| `/api/training/getComplianceReport` | POST | Compliance report | HR/Admin | Report data |

---

## ✅ PHASE-1 COMPLIANCE CHECKLIST

- ✅ No time tracking tables
- ✅ No session tracking
- ✅ No content-level time enforcement
- ✅ **No assignment table** - All mandatory trainings automatically available to all employees
- ✅ **No content_view table** - Content viewing tracking not required
- ✅ **Multiple content per training** - Multiple content items allowed, but only ONE active at a time
- ✅ **Content effective dates** - Content scheduled via `effective_from` and `effective_to` dates
- ✅ **Dynamic deadline calculation** - No single deadline column, calculated per cycle based on pattern
- ✅ Lock calculated dynamically
- ✅ Consent-based lock release
- ✅ One training at a time
- ✅ One content per training
- ✅ Skip logic implemented (via training_skip table)
- ✅ Deactivation removes lock
- ✅ File system storage (not BLOB)
- ✅ Frequency tracking via consent records
- ✅ Audit trail via consent table
- ✅ Status determined by: consent (COMPLETED), content_view (IN_PROGRESS), skip (SKIPPED), or none (PENDING)

---

**Status:** ✅ **PHASE-1 DATABASE SCHEMA & API DESIGN COMPLETE**
