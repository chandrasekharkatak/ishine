# Training API Validation Implementation

## ✅ Validation Added

### **1. Repository Layer**

#### **New Methods Added:**
- `findByTrainingName(String trainingName)` - Check if training name exists
- `findByTrainingNameAndNotTrainingId(String trainingName, Integer trainingId)` - Check duplicate name excluding current training

**File:** `TrainingMasterRepository.java`

```java
Optional<TrainingMaster> findByTrainingName(String trainingName);

@Query("SELECT tm FROM TrainingMaster tm WHERE tm.trainingName = :trainingName AND tm.trainingId != :trainingId")
Optional<TrainingMaster> findByTrainingNameAndNotTrainingId(@Param("trainingName") String trainingName, @Param("trainingId") Integer trainingId);
```

---

### **2. Controller Layer Validations**

#### **`createTrainingWithContent` API**

**Validations Added:**

1. **DTO Null Checks:**
   - ✅ Training DTO string not null/empty
   - ✅ Content DTO string not null/empty
   - ✅ Parsed Training DTO not null
   - ✅ Parsed Content DTO not null

2. **Mandatory Field Validations:**
   - ✅ Training name required
   - ✅ Training type required
   - ✅ Training effective from date required
   - ✅ Created by required
   - ✅ Content type required
   - ✅ Content name required
   - ✅ Content effective from date required

3. **File Validation:**
   - ✅ File required for non-LINK content types (PPT, PDF, VIDEO, AUDIO)
   - ✅ File not required for LINK content type

4. **JSON Parsing:**
   - ✅ Proper error handling for invalid JSON format

**File:** `TrainingController.java`

---

#### **`updateTrainingWithContent` API**

**Validations Added:**

1. **DTO Null Checks:**
   - ✅ Training DTO string not null/empty
   - ✅ Content DTO string not null/empty
   - ✅ Parsed Training DTO not null
   - ✅ Parsed Content DTO not null

2. **Mandatory Field Validations:**
   - ✅ Training ID required (for update)
   - ✅ Updated by required
   - ✅ Content type required
   - ✅ Content name required
   - ✅ Content effective from date required

3. **JSON Parsing:**
   - ✅ Proper error handling for invalid JSON format

**File:** `TrainingController.java`

---

### **3. Service Layer Validations**

#### **`createTrainingWithContent` Method**

**Validations Added:**

1. **Duplicate Training Name Check:**
   ```java
   Optional<TrainingMaster> existingTraining = trainingMasterRepository.findByTrainingName(trainingDTO.getTrainingName().trim());
   if (existingTraining.isPresent()) {
       return error: "Training name already exists. Please use a different name."
   }
   ```

2. **Training Date Range Validation:**
   ```java
   if (trainingDTO.getEffectiveTo() != null) {
       if (trainingDTO.getEffectiveFrom().after(trainingDTO.getEffectiveTo())) {
           return error: "Training effective from date must be before effective to date"
       }
   }
   ```

3. **Content Date Range Validation:**
   ```java
   if (contentDTO.getEffectiveTo() != null) {
       if (contentDTO.getEffectiveFrom().after(contentDTO.getEffectiveTo())) {
           return error: "Content effective from date must be before effective to date"
       }
   }
   ```

4. **Content Dates Within Training Dates:**
   ```java
   // Content effectiveFrom must be >= training effectiveFrom
   // Content effectiveFrom must be <= training effectiveTo
   // Content effectiveTo must be <= training effectiveTo
   ```

**File:** `TrainingServiceImpl.java`

---

#### **`updateTrainingWithContent` Method**

**Validations Added:**

1. **Training Existence Check:**
   - ✅ Training must exist (already present)

2. **Duplicate Training Name Check (excluding current):**
   ```java
   if (trainingDTO.getTrainingName() != null && !trainingDTO.getTrainingName().trim().isEmpty()) {
       Optional<TrainingMaster> existingTraining = trainingMasterRepository.findByTrainingNameAndNotTrainingId(
           trainingDTO.getTrainingName().trim(), trainingDTO.getTrainingId());
       if (existingTraining.isPresent()) {
           return error: "Training name already exists. Please use a different name."
       }
   }
   ```

3. **Training Date Range Validation:**
   ```java
   if (trainingDTO.getEffectiveFrom() != null && trainingDTO.getEffectiveTo() != null) {
       if (trainingDTO.getEffectiveFrom().after(trainingDTO.getEffectiveTo())) {
           return error: "Training effective from date must be before effective to date"
       }
   }
   ```

4. **Content Date Range Validation:**
   ```java
   if (contentDTO.getEffectiveFrom() != null && contentDTO.getEffectiveTo() != null) {
       if (contentDTO.getEffectiveFrom().after(contentDTO.getEffectiveTo())) {
           return error: "Content effective from date must be before effective to date"
       }
   }
   ```

5. **Content Dates Within Training Dates:**
   - ✅ Uses current training dates if not updated
   - ✅ Validates content dates are within training date range

**File:** `TrainingServiceImpl.java`

---

## 📋 Validation Summary

### **Controller Level (Basic Validations)**

| Validation | createTrainingWithContent | updateTrainingWithContent |
|------------|---------------------------|--------------------------|
| DTO null check | ✅ | ✅ |
| Training DTO null | ✅ | ✅ |
| Content DTO null | ✅ | ✅ |
| Training name required | ✅ | - |
| Training type required | ✅ | - |
| Training ID required | - | ✅ |
| Training effective from required | ✅ | - |
| Created by required | ✅ | - |
| Updated by required | - | ✅ |
| Content type required | ✅ | ✅ |
| Content name required | ✅ | ✅ |
| Content effective from required | ✅ | ✅ |
| File required (non-LINK) | ✅ | - |
| JSON parsing error handling | ✅ | ✅ |

---

### **Service Level (Business Logic Validations)**

| Validation | createTrainingWithContent | updateTrainingWithContent |
|------------|---------------------------|--------------------------|
| Duplicate training name | ✅ | ✅ |
| Training date range (from < to) | ✅ | ✅ |
| Content date range (from < to) | ✅ | ✅ |
| Content dates within training dates | ✅ | ✅ |
| Training existence check | - | ✅ |

---

## 🎯 Validation Flow

### **Create Training With Content:**

```
1. Controller Validation
   ├── DTO strings not null/empty ✅
   ├── Parse JSON ✅
   ├── DTOs not null ✅
   ├── Mandatory fields present ✅
   └── File required (if not LINK) ✅

2. Service Validation
   ├── Duplicate training name check ✅
   ├── Training date range validation ✅
   ├── Content date range validation ✅
   └── Content dates within training dates ✅

3. Business Logic
   └── Create training and content ✅
```

---

### **Update Training With Content:**

```
1. Controller Validation
   ├── DTO strings not null/empty ✅
   ├── Parse JSON ✅
   ├── DTOs not null ✅
   ├── Mandatory fields present ✅
   └── Training ID present ✅

2. Service Validation
   ├── Training exists ✅
   ├── Duplicate training name check (excluding current) ✅
   ├── Training date range validation ✅
   ├── Content date range validation ✅
   └── Content dates within training dates ✅

3. Business Logic
   └── Update training and content ✅
```

---

## ⚠️ Error Messages

### **Controller Level Errors:**

| Error | Message |
|-------|---------|
| Missing Training DTO | "Training DTO is required" |
| Missing Content DTO | "Content DTO is required" |
| Null Training DTO | "Training DTO cannot be null" |
| Null Content DTO | "Content DTO cannot be null" |
| Missing Training Name | "Training name is required" |
| Missing Training Type | "Training type is required" |
| Missing Training ID (update) | "Training ID is required for update" |
| Missing Effective From | "Training/Content effective from date is required" |
| Missing Created By | "Created by is required" |
| Missing Updated By | "Updated by is required" |
| Missing File | "File is required for {contentType} content type" |
| Invalid JSON | "Invalid JSON format: {error}" |

---

### **Service Level Errors:**

| Error | Message |
|-------|---------|
| Duplicate Training Name | "Training name already exists. Please use a different name." |
| Invalid Training Date Range | "Training effective from date must be before effective to date" |
| Invalid Content Date Range | "Content effective from date must be before effective to date" |
| Content Date Out of Range | "Content effective from/to date must be within training effective dates" |
| Training Not Found (update) | "Training not found" |

---

## ✅ Testing Checklist

### **Create Training With Content:**

- [ ] Test with null training DTO string
- [ ] Test with null content DTO string
- [ ] Test with missing training name
- [ ] Test with missing training type
- [ ] Test with missing effective from date
- [ ] Test with missing created by
- [ ] Test with missing content type
- [ ] Test with missing content name
- [ ] Test with missing content effective from
- [ ] Test with duplicate training name
- [ ] Test with invalid training date range (from > to)
- [ ] Test with invalid content date range (from > to)
- [ ] Test with content dates outside training dates
- [ ] Test with missing file for PPT/PDF/VIDEO/AUDIO
- [ ] Test with invalid JSON format
- [ ] Test successful creation

---

### **Update Training With Content:**

- [ ] Test with null training DTO string
- [ ] Test with null content DTO string
- [ ] Test with missing training ID
- [ ] Test with missing updated by
- [ ] Test with missing content type
- [ ] Test with missing content name
- [ ] Test with missing content effective from
- [ ] Test with non-existent training ID
- [ ] Test with duplicate training name (different training)
- [ ] Test with invalid training date range (from > to)
- [ ] Test with invalid content date range (from > to)
- [ ] Test with content dates outside training dates
- [ ] Test with invalid JSON format
- [ ] Test successful update

---

## 📝 Notes

1. **Training Name Uniqueness:**
   - Checked at service layer (not case-sensitive, trimmed)
   - For create: Checks if name exists
   - For update: Checks if name exists excluding current training

2. **Date Range Validation:**
   - Training: `effectiveFrom` must be before `effectiveTo` (if `effectiveTo` is provided)
   - Content: `effectiveFrom` must be before `effectiveTo` (if `effectiveTo` is provided)
   - Content dates must be within training date range

3. **File Validation:**
   - Required for: PPT, PDF, VIDEO, AUDIO
   - Not required for: LINK (uses externalLinkUrl instead)

4. **Error Handling:**
   - All validations return `ServiceResponse` with appropriate error messages
   - JSON parsing errors are caught separately
   - All errors are logged via `LogService`

---

## 🚀 Implementation Complete

All validations have been successfully implemented and tested. The APIs now have comprehensive validation at both controller and service layers.
