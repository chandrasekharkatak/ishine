# User Training Journey - Current Status & Gap Analysis

## ✅ Currently Implemented

### **1. Login Notification**
- ✅ Lock status checked on login (`getEmployeeInfoOnLogin`)
- ✅ Lock status stored in user object (`trainingLockStatus`)
- ✅ Training component checks lock status on init

### **2. Lock Screen Modal**
- ✅ Modal shows pending training details
- ✅ Shows training name, type, cycle, deadline
- ✅ Shows content type and name
- ✅ Timer functionality for minimum view time
- ✅ Consent submission button
- ✅ Skip button (if allowed and deadline not crossed)

### **3. Deadline Checking**
- ✅ `isDeadlineCrossed` flag in `PendingTrainingDTO`
- ✅ Deadline calculated per cycle
- ✅ Skip disabled when deadline crossed

### **4. Timer & Consent**
- ✅ Timer tracks viewing time
- ✅ Consent button enabled after minimum time
- ✅ Consent submission works
- ✅ Portal unlocks after consent

---

## ❌ Missing / Needs Improvement

### **1. Separate Training Listing Page**
**Current:** Only ONE pending training shown in modal  
**Required:** Separate page showing ALL trainings for user

**Gaps:**
- ❌ No API to get all trainings for a user (only `getPendingTraining` exists)
- ❌ No separate route for user training page (`/user-training` or `/my-trainings`)
- ❌ No component to list all trainings with status (Pending, Completed, Skipped)
- ❌ No UI to view training content from listing page

### **2. Deadline-Crossed Routing**
**Current:** Modal shows training even when deadline crossed  
**Required:** Route to training page when deadline crossed

**Gaps:**
- ❌ No routing logic when deadline crossed
- ❌ Modal doesn't differentiate between deadline crossed vs not crossed
- ❌ Need to force user to training page when deadline crossed

### **3. Training Content Viewing**
**Current:** Content opens in new tab/window  
**Required:** Inline content viewing with timer

**Gaps:**
- ❌ No inline content viewer (PPT, PDF, VIDEO preview)
- ❌ Timer doesn't track actual content viewing (only counts time on page)
- ❌ Need to integrate content preview similar to HR config page

### **4. User Training Page Design**
**Current:** HR config and user training share same component structure  
**Required:** Separate, user-friendly design

**Gaps:**
- ❌ No dedicated user training component
- ❌ No training card/list view for users
- ❌ No status indicators (Pending, In Progress, Completed, Skipped)
- ❌ No progress tracking per training

---

## 📋 Required Implementation Plan

### **Phase 1: API Development**

#### **1.1 Get All Trainings for User**
```java
@PostMapping(value = "/getUserTrainings")
public ServiceResponse getUserTrainings(@RequestBody TrainingRequestDTO request) {
    return trainingService.getUserTrainings(request.getEmpId());
}
```

**Response Structure:**
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
      "content": {
        "contentId": 1,
        "contentName": "POSH Video 2024 Q1",
        "contentType": "VIDEO"
      },
      "completionCount": 0,
      "requiredFrequency": 2,
      "lastCompletedOn": null
    }
  ]
}
```

**Logic:**
- Get all active mandatory trainings
- For each training, check user's completion status
- Calculate current cycle
- Check deadline status
- Return trainings with status

---

### **Phase 2: Frontend Component Development**

#### **2.1 Create User Training Component**
**File:** `frontend/src/app/user-training/user-training.component.ts`

**Features:**
- List all trainings for user
- Show training status (Pending, In Progress, Completed, Skipped)
- Show deadline information
- Filter by status
- Sort by deadline/priority

#### **2.2 Create User Training HTML**
**File:** `frontend/src/app/user-training/user-training.component.html`

**UI Elements:**
- Training cards/list view
- Status badges
- Deadline indicators
- Progress bars
- Action buttons (View, Start, Complete)

#### **2.3 Create Training Detail/View Component**
**File:** `frontend/src/app/user-training/training-detail/training-detail.component.ts`

**Features:**
- Show training details
- Inline content viewer (PPT, PDF, VIDEO, AUDIO)
- Timer integration
- Consent submission
- Skip option (if allowed)

---

### **Phase 3: Routing & Navigation**

#### **3.1 Add Route**
**File:** `frontend/src/app/app-routing.module.ts`

```typescript
{ path: 'user-training', component: UserTrainingComponent, canActivate: [AuthGuard] },
{ path: 'user-training/:trainingId', component: TrainingDetailComponent, canActivate: [AuthGuard] }
```

#### **3.2 Update Lock Logic**
**File:** `frontend/src/app/training/training.component.ts`

**Logic:**
```typescript
if (isLocked) {
  if (lockStatus.deadlineCrossed) {
    // Route to training page
    this.router.navigate(['/user-training']);
  } else {
    // Show modal (current behavior)
    this.openLockScreen();
  }
}
```

---

### **Phase 4: Business Logic Updates**

#### **4.1 Deadline-Crossed Handling**
- When deadline crossed: Route to training page (no skip option)
- When deadline not crossed: Show modal with skip option

#### **4.2 Training Status Calculation**
- **PENDING:** No consent for current cycle
- **IN_PROGRESS:** Content viewed but consent not submitted
- **COMPLETED:** Consent submitted for current cycle
- **SKIPPED:** Skip record exists for current cycle

#### **4.3 Content Viewing Integration**
- Reuse content preview from HR config
- Integrate timer with content viewing
- Track actual viewing time (not just page time)

---

## 🎯 Implementation Checklist

### **Backend:**
- [ ] Create `getUserTrainings()` API
- [ ] Create `UserTrainingDTO` with status information
- [ ] Update `getPendingTraining()` to include all status info
- [ ] Add training status calculation logic

### **Frontend:**
- [ ] Create `UserTrainingComponent` (listing page)
- [ ] Create `TrainingDetailComponent` (detail/view page)
- [ ] Add routes for user training pages
- [ ] Update lock logic to route when deadline crossed
- [ ] Integrate content preview (reuse from HR config)
- [ ] Add training status badges and indicators
- [ ] Add filter/sort functionality
- [ ] Add navigation menu item for "My Trainings"

### **UI/UX:**
- [ ] Design training card/list view
- [ ] Add status color coding
- [ ] Add deadline countdown/indicators
- [ ] Add progress tracking
- [ ] Make responsive design
- [ ] Add loading states
- [ ] Add error handling

---

## 🔄 User Flow (After Implementation)

### **Scenario 1: Deadline Not Crossed**
```
1. User logs in
2. Lock status checked → Locked
3. Deadline not crossed
4. Modal opens with pending training
5. User can:
   - View content
   - Complete training (after timer)
   - Skip temporarily
```

### **Scenario 2: Deadline Crossed**
```
1. User logs in
2. Lock status checked → Locked
3. Deadline crossed
4. User routed to /user-training page
5. Training page shows:
   - All pending trainings
   - Deadline crossed indicator
   - No skip option
6. User must complete training to unlock portal
```

### **Scenario 3: Multiple Trainings**
```
1. User logs in
2. Multiple pending trainings exist
3. User routed to /user-training page
4. Page shows:
   - List of all trainings
   - Status of each training
   - Priority (deadline-based)
5. User can:
   - View any training
   - Complete trainings one by one
   - See progress
```

---

## 📝 Recommendations

### **1. Separate Components**
- ✅ Keep HR Training Config separate (`training-config.component`)
- ✅ Create new User Training component (`user-training.component`)
- ✅ Different UI/UX for each (HR vs Employee)

### **2. Content Preview**
- Reuse preview logic from HR config
- Integrate timer with content viewing
- Support all content types (PPT, PDF, VIDEO, AUDIO, LINK)

### **3. Status Management**
- Clear status indicators
- Progress tracking
- Deadline warnings
- Completion tracking

### **4. Navigation**
- Add "My Trainings" to user menu
- Show pending count badge
- Quick access to training page

---

## ⚠️ Current Limitations

1. **Single Training Focus:** Only shows one pending training
2. **Modal-Based:** No dedicated training page
3. **No Training List:** Can't see all trainings at once
4. **Limited Status Info:** No clear status indicators
5. **Content Viewing:** Opens in new tab, no inline preview
6. **Timer Accuracy:** Timer runs on page, not content viewing

---

## ✅ Next Steps

1. **Create API:** `getUserTrainings()` to get all trainings with status
2. **Create Component:** User training listing page
3. **Update Routing:** Add route and update lock logic
4. **Integrate Preview:** Reuse content preview from HR config
5. **Enhance Timer:** Track actual content viewing time
6. **Add Navigation:** Menu item for "My Trainings"
