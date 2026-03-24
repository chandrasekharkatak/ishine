# User Training Journey - Implementation Plan

## 📊 Current Status Analysis

### ✅ **What's Already Implemented:**

1. **Login Notification:**
   - ✅ Lock status checked on login (`getEmployeeInfoOnLogin`)
   - ✅ Lock status stored in user object
   - ✅ Training component checks lock on init

2. **Lock Screen Modal:**
   - ✅ Shows pending training details
   - ✅ Shows deadline information
   - ✅ Timer functionality
   - ✅ Consent submission
   - ✅ Skip functionality (when deadline not crossed)

3. **Backend APIs:**
   - ✅ `getPendingTraining` - Gets ONE pending training
   - ✅ `getLockStatus` - Gets lock status
   - ✅ `submitConsent` - Submit consent
   - ✅ `skipTraining` - Skip training
   - ✅ `downloadContent` - Download content

4. **Business Logic:**
   - ✅ Deadline calculation
   - ✅ Deadline crossed check
   - ✅ Cycle calculation
   - ✅ Frequency tracking

---

### ❌ **What's Missing:**

1. **API to Get All Trainings for User:**
   - ❌ No API to get ALL trainings with status
   - ❌ Only `getPendingTraining` exists (returns single training)

2. **Separate User Training Page:**
   - ❌ No dedicated user training component
   - ❌ Currently only modal-based (lock screen)
   - ❌ No training listing view

3. **Deadline-Crossed Routing:**
   - ❌ No routing to training page when deadline crossed
   - ❌ Modal shows even when deadline crossed

4. **Content Viewing:**
   - ❌ Content opens in new tab (no inline preview)
   - ❌ Timer doesn't track actual content viewing
   - ❌ No content preview similar to HR config

5. **Training Status Management:**
   - ❌ No clear status indicators (Pending, In Progress, Completed, Skipped)
   - ❌ No progress tracking
   - ❌ No training list view

---

## 🎯 Required Implementation

### **Phase 1: Backend API Development**

#### **1.1 Create `getUserTrainings` API**

**Endpoint:** `POST /api/training/getUserTrainings`

**Purpose:** Get all trainings for a user with status information

**Request:**
```json
{
  "empId": 100
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
      "trainingType": "POSH",
      "status": "PENDING", // PENDING, IN_PROGRESS, COMPLETED, SKIPPED
      "currentCycleNumber": 1,
      "deadline": "2024-06-30",
      "isDeadlineCrossed": false,
      "lockEnabled": true,
      "mandatoryFlag": "true",
      "content": {
        "contentId": 1,
        "contentName": "POSH Video 2024 Q1",
        "contentType": "VIDEO",
        "contentPath": "/1/video_1.mp4"
      },
      "completionCount": 0,
      "requiredFrequency": 2,
      "minViewTimeMinutes": 30,
      "consentRequired": "true",
      "skipAllowed": "true"
    }
  ]
}
```

**Logic:**
1. Get all active mandatory trainings
2. For each training:
   - Calculate completion count (last 12 months)
   - Calculate current cycle
   - Check if consent exists for current active content in current cycle
   - Check if skip exists for current cycle
   - Calculate deadline (if enabled)
   - Determine status: PENDING, COMPLETED, SKIPPED
   - Get current active content
3. Return list sorted by priority (deadline crossed first, then deadline date)

---

### **Phase 2: Frontend Component Development**

#### **2.1 Create User Training Component**

**File:** `frontend/src/app/user-training/user-training.component.ts`

**Features:**
- List all trainings for user
- Show training status
- Filter by status
- Sort by deadline/priority
- Navigate to training detail

**File:** `frontend/src/app/user-training/user-training.component.html`

**UI:**
- Training cards/list view
- Status badges
- Deadline indicators
- Progress bars
- Action buttons

#### **2.2 Create Training Detail Component**

**File:** `frontend/src/app/user-training/training-detail/training-detail.component.ts`

**Features:**
- Show training details
- Inline content viewer (reuse from HR config)
- Timer integration
- Consent submission
- Skip option (if allowed)

**File:** `frontend/src/app/user-training/training-detail/training-detail.component.html`

**UI:**
- Training information card
- Content preview area
- Timer display
- Consent/Skip buttons

---

### **Phase 3: Routing & Navigation**

#### **3.1 Add Routes**

**File:** `frontend/src/app/app-routing.module.ts`

```typescript
{ path: 'user-training', component: UserTrainingComponent, canActivate: [AuthGuard] },
{ path: 'user-training/:trainingId', component: TrainingDetailComponent, canActivate: [AuthGuard] }
```

#### **3.2 Update Lock Logic**

**File:** `frontend/src/app/training/training.component.ts`

**Update `checkLockStatus()` method:**

```typescript
checkLockStatus() {
  // ... existing code ...
  
  if (this.isLocked) {
    // Check if deadline crossed
    if (this.lockStatus.deadlineCrossed) {
      // Route to training page
      this.router.navigate(['/user-training']);
    } else {
      // Show modal (current behavior)
      this.getPendingTraining();
      this.openLockScreen();
    }
  }
}
```

---

### **Phase 4: Content Preview Integration**

#### **4.1 Reuse Content Preview**

- Copy content preview logic from `training-config.component.ts`
- Integrate PPTX parser
- Integrate PDF viewer
- Integrate VIDEO/AUDIO players
- Integrate LINK handler

#### **4.2 Timer Integration**

- Start timer when content is viewed
- Track actual viewing time (not just page time)
- Pause timer when content not visible
- Resume timer when content visible

---

## 📋 Detailed Implementation Steps

### **Step 1: Create DTO for User Training**

**File:** `java/com/apmosys/employeeportal/dto/UserTrainingDTO.java`

```java
public class UserTrainingDTO {
    private Integer trainingId;
    private String trainingName;
    private String trainingType;
    private String status; // PENDING, IN_PROGRESS, COMPLETED, SKIPPED
    private Integer currentCycleNumber;
    private Date deadline;
    private Boolean isDeadlineCrossed;
    private Boolean lockEnabled;
    private String mandatoryFlag;
    private TrainingContentDTO content;
    private Integer completionCount;
    private Integer requiredFrequency;
    private Integer minViewTimeMinutes;
    private String consentRequired;
    private String skipAllowed;
    // ... getters/setters
}
```

---

### **Step 2: Implement `getUserTrainings` Service Method**

**File:** `java/com/apmosys/employeeportal/service/TrainingServiceImpl.java`

**Method:**
```java
public ServiceResponse getUserTrainings(Long empId) {
    // 1. Get all active mandatory trainings
    // 2. For each training:
    //    - Calculate status
    //    - Get active content
    //    - Calculate deadline
    //    - Build UserTrainingDTO
    // 3. Sort by priority
    // 4. Return list
}
```

---

### **Step 3: Create Controller Endpoint**

**File:** `java/com/apmosys/employeeportal/controller/TrainingController.java`

```java
@PostMapping(value = "/getUserTrainings")
public ServiceResponse getUserTrainings(@RequestBody TrainingRequestDTO request) {
    return trainingService.getUserTrainings(request.getEmpId());
}
```

---

### **Step 4: Create Frontend Service Method**

**File:** `frontend/src/app/services/training.service.ts`

```typescript
getUserTrainings(empId: number) {
  return this.http.post(`${this.baseUrl}api/training/getUserTrainings`, { empId: empId });
}
```

---

### **Step 5: Create User Training Component**

**Files:**
- `frontend/src/app/user-training/user-training.component.ts`
- `frontend/src/app/user-training/user-training.component.html`
- `frontend/src/app/user-training/user-training.component.css`

**Features:**
- Load all trainings on init
- Display training cards/list
- Show status badges
- Show deadline indicators
- Navigate to detail page

---

### **Step 6: Create Training Detail Component**

**Files:**
- `frontend/src/app/user-training/training-detail/training-detail.component.ts`
- `frontend/src/app/user-training/training-detail/training-detail.component.html`
- `frontend/src/app/user-training/training-detail/training-detail.component.css`

**Features:**
- Load training details
- Show content preview (reuse from HR config)
- Timer integration
- Consent submission
- Skip option

---

### **Step 7: Update Routing**

**File:** `frontend/src/app/app-routing.module.ts`

Add routes for user training pages.

---

### **Step 8: Update Lock Logic**

**File:** `frontend/src/app/training/training.component.ts`

Update to route when deadline crossed.

---

## 🎨 UI/UX Design Recommendations

### **User Training Page (Listing):**

```
┌─────────────────────────────────────────────────┐
│  My Trainings                                   │
├─────────────────────────────────────────────────┤
│  [Filter: All | Pending | Completed | Skipped] │
│  [Sort: Deadline | Name | Type]                 │
├─────────────────────────────────────────────────┤
│  ┌───────────────────────────────────────────┐ │
│  │ 🚨 POSH Training 2024                    │ │
│  │ Type: POSH | Status: PENDING              │ │
│  │ Deadline: 30-Jun-2024 ⚠️ Deadline Crossed│ │
│  │ Progress: 0/2 completions                │ │
│  │ [View Training] [Start Now]              │ │
│  └───────────────────────────────────────────┘ │
│  ┌───────────────────────────────────────────┐ │
│  │ ✅ Cyber Security Training                 │ │
│  │ Type: Cyber Security | Status: COMPLETED  │ │
│  │ Last Completed: 15-Jan-2024               │ │
│  │ Progress: 2/2 completions ✓               │ │
│  └───────────────────────────────────────────┘ │
└─────────────────────────────────────────────────┘
```

### **Training Detail Page:**

```
┌─────────────────────────────────────────────────┐
│  POSH Training 2024                             │
├─────────────────────────────────────────────────┤
│  Type: POSH | Cycle: 1 | Deadline: 30-Jun-2024  │
│  ⚠️ Deadline Crossed - Must Complete Now         │
├─────────────────────────────────────────────────┤
│  Content: POSH Video 2024 Q1                    │
│  [Video Player with Timer]                      │
│  Viewing Time: 15:30 / 30:00 minutes           │
│  [████████░░░░░░░░░░░░░░░░] 50%                │
├─────────────────────────────────────────────────┤
│  [Submit Consent] (disabled until timer done)   │
└─────────────────────────────────────────────────┘
```

---

## ✅ Implementation Checklist

### **Backend:**
- [ ] Create `UserTrainingDTO`
- [ ] Implement `getUserTrainings()` service method
- [ ] Add controller endpoint
- [ ] Add status calculation logic
- [ ] Test API

### **Frontend:**
- [ ] Add `getUserTrainings()` to training service
- [ ] Create `UserTrainingComponent`
- [ ] Create `TrainingDetailComponent`
- [ ] Add routes
- [ ] Update lock logic
- [ ] Integrate content preview
- [ ] Add status badges
- [ ] Add deadline indicators
- [ ] Add filter/sort functionality
- [ ] Add navigation menu item

### **Testing:**
- [ ] Test with deadline crossed
- [ ] Test with deadline not crossed
- [ ] Test with multiple trainings
- [ ] Test content viewing
- [ ] Test timer functionality
- [ ] Test consent submission
- [ ] Test skip functionality

---

## 🚀 Priority Order

1. **High Priority:**
   - Create `getUserTrainings` API
   - Create user training component
   - Update lock logic to route when deadline crossed

2. **Medium Priority:**
   - Integrate content preview
   - Add status indicators
   - Add filter/sort

3. **Low Priority:**
   - Enhance timer accuracy
   - Add progress tracking
   - Add training history view

---

## 📝 Notes

- **Separate Design:** HR Training Config and User Training should have separate components and designs
- **Content Preview:** Reuse preview logic from HR config but adapt for user view
- **Timer:** Consider tracking actual content viewing time (not just page time)
- **Status:** Clear status indicators help users understand their training status
- **Navigation:** Easy access to training page from main menu
