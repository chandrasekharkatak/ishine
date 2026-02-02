# Difference Between `mandatoryFlag` and `lockEnabled`

## 📋 Quick Summary

| Field | Purpose | When Used | Impact |
|-------|---------|-----------|--------|
| **`mandatoryFlag`** | Determines if training is **mandatory** | During pending training detection | Controls **WHICH** trainings are required |
| **`lockEnabled`** | Determines if portal should be **locked** | After finding pending training | Controls **WHETHER** portal gets locked |

---

## 🔍 Detailed Explanation

### **1. `mandatoryFlag` (mandatory_flag)**

#### **Purpose:**
- Determines if a training is **mandatory** for employees
- Controls **which trainings** are considered for completion tracking

#### **How It Works:**
```java
// In findPendingMandatoryTraining()
List<TrainingMaster> mandatoryTrainings = trainingMasterRepository
    .findActiveMandatoryTrainings("true", "true");
//                              ↑ mandatoryFlag = "true"
```

#### **Behavior:**
- ✅ **`mandatoryFlag = 'true'`**: 
  - Training is considered **mandatory**
  - Included in pending training detection
  - Employee **must complete** it (based on frequency)
  - Tracked for compliance

- ❌ **`mandatoryFlag = 'false'`**: 
  - Training is **optional**
  - **NOT** included in pending training detection
  - Employee can complete it voluntarily
  - Not tracked for compliance

#### **Use Cases:**
- **Mandatory Trainings:** POSH, Cyber Security, Safety Training
- **Optional Trainings:** Skill Development, Soft Skills, Leadership Training

---

### **2. `lockEnabled` (lock_enabled)**

#### **Purpose:**
- Determines if the portal should be **LOCKED** when this training is pending
- Controls **enforcement mechanism** for mandatory trainings

#### **How It Works:**
```java
// In getLockStatusInternal()
TrainingMaster pendingTraining = findPendingMandatoryTraining(empId);

if (pendingTraining == null) {
    return lockStatus; // Not locked
}

// Check if lock is enabled for this training
if (!"true".equals(pendingTraining.getLockEnabled())) {
    return lockStatus; // Not locked - even though training is mandatory
}

// Portal will be locked
lockStatus.setIsLocked(true);
```

#### **Behavior:**
- ✅ **`lockEnabled = 'true'`**: 
  - Portal **WILL BE LOCKED** if training is pending
  - Employee **cannot access** other portal features
  - Employee **must complete** training to unlock portal
  - **Strict enforcement**

- ❌ **`lockEnabled = 'false'`**: 
  - Portal **WILL NOT BE LOCKED** even if training is pending
  - Employee **can access** all portal features
  - Training is still mandatory (tracked for compliance)
  - **Soft enforcement** (reminders/reports only)

#### **Use Cases:**
- **Lock Enabled:** Critical compliance trainings (POSH, Safety) that must be completed before accessing portal
- **Lock Disabled:** Important trainings that should be completed but don't block portal access

---

## 🎯 Key Differences

### **1. Scope of Control**

| Field | Controls | Stage |
|-------|----------|-------|
| `mandatoryFlag` | **Which trainings** are required | **Selection** stage (finding pending trainings) |
| `lockEnabled` | **Whether portal locks** for pending training | **Enforcement** stage (after finding pending training) |

### **2. Impact on Employee**

| Scenario | `mandatoryFlag` | `lockEnabled` | Employee Experience |
|----------|----------------|---------------|-------------------|
| **Strict Enforcement** | `'true'` | `'true'` | Portal locked, must complete to unlock |
| **Soft Enforcement** | `'true'` | `'false'` | Portal accessible, training still required (tracked) |
| **Optional Training** | `'false'` | `'true'`/`'false'` | Not considered for completion (irrelevant) |

### **3. Code Flow**

```
Step 1: Find Pending Training
├── Filter by mandatoryFlag = 'true' ✅
├── Check completion frequency
└── Return pending training (if any)

Step 2: Check Lock Status
├── If no pending training → NOT LOCKED
├── If pending training found:
│   ├── Check lockEnabled = 'true' ✅
│   ├── Check active content exists
│   ├── Check consent not submitted
│   └── If all true → PORTAL LOCKED 🔒
└── If lockEnabled = 'false' → NOT LOCKED (even if mandatory)
```

---

## 💡 Real-World Examples

### **Example 1: POSH Training (Strict Enforcement)**
```json
{
  "trainingName": "POSH Training 2024",
  "mandatoryFlag": "true",    // ✅ Mandatory - must complete
  "lockEnabled": "true"        // ✅ Lock portal - strict enforcement
}
```
**Result:** 
- Training is mandatory
- Portal is locked until completed
- Employee cannot access other features

---

### **Example 2: Soft Skills Training (Soft Enforcement)**
```json
{
  "trainingName": "Communication Skills",
  "mandatoryFlag": "true",    // ✅ Mandatory - must complete
  "lockEnabled": "false"      // ❌ Don't lock portal - soft enforcement
}
```
**Result:**
- Training is mandatory (tracked for compliance)
- Portal remains accessible
- Employee can complete training later
- HR can track completion via reports

---

### **Example 3: Optional Training**
```json
{
  "trainingName": "Leadership Workshop",
  "mandatoryFlag": "false",   // ❌ Optional - not required
  "lockEnabled": "true"       // ⚠️ Irrelevant (not considered)
}
```
**Result:**
- Training is optional
- Not included in pending training detection
- `lockEnabled` has no effect (training won't be pending)

---

## 🔄 Logic Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    Employee Logs In                          │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
        ┌───────────────────────────────┐
        │  Check Lock Status             │
        └──────────────┬────────────────┘
                       │
                       ▼
        ┌───────────────────────────────┐
        │  Find Pending Training         │
        │  (mandatoryFlag = 'true')      │
        └──────────────┬────────────────┘
                       │
         ┌─────────────┴─────────────┐
         │                           │
         ▼                           ▼
    Found Pending              No Pending
         │                           │
         │                           └───► Portal UNLOCKED ✅
         │
         ▼
    ┌───────────────────────────────┐
    │  Check lockEnabled             │
    └──────────────┬────────────────┘
                   │
      ┌────────────┴────────────┐
      │                         │
      ▼                         ▼
lockEnabled = 'true'    lockEnabled = 'false'
      │                         │
      │                         └───► Portal UNLOCKED ✅
      │                                 (but training still
      │                                  mandatory - tracked)
      ▼
┌───────────────────────────────┐
│  Check Active Content          │
│  Check Consent Status          │
└──────────────┬────────────────┘
               │
      ┌────────┴────────┐
      │                 │
      ▼                 ▼
  No Consent      Consent Exists
      │                 │
      │                 └───► Portal UNLOCKED ✅
      │
      ▼
┌───────────────────────────────┐
│  Portal LOCKED 🔒              │
│  Employee must complete        │
│  training to unlock            │
└───────────────────────────────┘
```

---

## ⚠️ Important Notes

### **1. Dependency Relationship**
- `lockEnabled` **depends on** `mandatoryFlag`
- If `mandatoryFlag = 'false'`, `lockEnabled` has **no effect**
- Only mandatory trainings can lock the portal

### **2. Combination Matrix**

| `mandatoryFlag` | `lockEnabled` | Result |
|----------------|---------------|--------|
| `'false'` | `'false'` | Optional training, no lock |
| `'false'` | `'true'` | Optional training, no lock (lockEnabled ignored) |
| `'true'` | `'false'` | Mandatory training, **soft enforcement** (no lock) |
| `'true'` | `'true'` | Mandatory training, **strict enforcement** (lock) |

### **3. Use Case Recommendations**

#### **Use `lockEnabled = 'true'` when:**
- Training is **critical** for compliance
- Training must be completed **before** accessing portal
- Legal/regulatory requirement (e.g., POSH, Safety)
- Immediate action required

#### **Use `lockEnabled = 'false'` when:**
- Training is important but not **critical**
- Employee can complete training **later**
- You want to track compliance without blocking access
- Soft reminders/reports are sufficient

---

## 📝 Summary

| Aspect | `mandatoryFlag` | `lockEnabled` |
|--------|----------------|---------------|
| **What it controls** | Training requirement | Portal lock mechanism |
| **When it's checked** | During pending training detection | After finding pending training |
| **Impact** | Determines which trainings are required | Determines if portal locks |
| **Enforcement** | Compliance tracking | Access control |
| **Can be independent?** | Yes | No (requires mandatoryFlag = 'true') |

**In Simple Terms:**
- **`mandatoryFlag`**: "Is this training required?" 
- **`lockEnabled`**: "Should we lock the portal if this training is pending?"

**Best Practice:**
- Use `lockEnabled = 'true'` for **critical** mandatory trainings
- Use `lockEnabled = 'false'` for **important but not critical** mandatory trainings
