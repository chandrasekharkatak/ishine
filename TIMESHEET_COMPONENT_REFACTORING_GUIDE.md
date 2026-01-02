# Timesheet Component Refactoring Guide

## Overview
This document outlines the refactoring of the large `my-timesheet` component into smaller, focused child components for better maintainability and reusability.

## Created Child Components

### 1. **Time Picker Component** (`time-picker/`)
**Purpose**: Reusable time picker for selecting hour, minute, and AM/PM period.

**Files**:
- `time-picker.component.ts`
- `time-picker.component.html`
- `time-picker.component.css`
- `time-picker.component.spec.ts`

**Usage**:
```html
<app-time-picker
  [label]="'In Time'"
  [required]="true"
  [selectedHour]="selectedInHour"
  [selectedMinute]="selectedInMinute"
  [selectedPeriod]="selectedInPeriod"
  [disabled]="false"
  (timeChange)="onTimeChange($event)">
</app-time-picker>
```

**Replaces**: Lines 372-398, 406-439, 474-509, 512-547 in `my-timesheet.component.html`

---

### 2. **Activity Row Component** (`activity-row/`)
**Purpose**: Individual activity entry row with client, location, team, activity, description, and time fields.

**Files**:
- `activity-row.component.ts`
- `activity-row.component.html`
- `activity-row.component.css`
- `activity-row.component.spec.ts`

**Usage**:
```html
<app-activity-row
  *ngFor="let activityObj of allTimesheetActivities; let i = index"
  [activityObj]="activityObj"
  [index]="i"
  [isLast]="i === allTimesheetActivities.length - 1"
  [isFirst]="i === 0"
  [canAdd]="allTimesheetActivities.length === 1 || i === allTimesheetActivities.length - 1"
  [filteredClients]="filteredClients"
  [isUpdation]="isUpdation"
  [timesheetFillable]="timesheetFillable"
  (clientChange)="getClientLocationList($event.activity, $event.clientId)"
  (clientLocationChange)="getProjectList($event)"
  (projectChange)="getAllActivitiesByProjectIdandEmpId($event, $event)"
  (activityChange)="setActivity($event)"
  (descriptionChange)="validateDescription($event, $event)"
  (timeChange)="validateTime($event, $event.completionTime)"
  (addActivity)="addInputActivityField($event)"
  (removeActivity)="removeInputActivityField($event)"
  (validateClientName)="validateClientName($event.event, $event.clientId)"
  (validateClientLocation)="validateClientLocation($event.event, $event.clientLocationId)"
  (validateActivity)="validateActivity($event.event, $event.activityId)"
  (validateDescription)="validateDescription($event, $event)"
  (validateTime)="validateTime($event.event, $event.completionTime)">
</app-activity-row>
```

**Replaces**: Lines 605-766 in `my-timesheet.component.html`

---

### 3. **Document Upload Component** (`document-upload/`)
**Purpose**: File upload with preview functionality for attendance documents.

**Files**:
- `document-upload.component.ts`
- `document-upload.component.html`
- `document-upload.component.css`
- `document-upload.component.spec.ts`

**Usage**:
```html
<app-document-upload
  [label]="'Filled Attendance Proof'"
  [required]="true"
  [fileName]="fileName1"
  [fileError]="fileError1"
  [previewUrl]="previewUrl1"
  [showPreview]="true"
  (fileSelected)="onFileSelected($event, 'doc1')"
  (previewClicked)="openPreviewModalForTwo('doc1')">
</app-document-upload>
```

**Replaces**: Lines 302-321, 335-354 in `my-timesheet.component.html`

---

## Integration Steps

### Step 1: Update Module Declarations

Add the new components to your Angular module (likely `app.module.ts` or a feature module):

```typescript
import { TimePickerComponent } from './user-timesheet/my-timesheet/time-picker/time-picker.component';
import { ActivityRowComponent } from './user-timesheet/my-timesheet/activity-row/activity-row.component';
import { DocumentUploadComponent } from './user-timesheet/my-timesheet/document-upload/document-upload.component';

@NgModule({
  declarations: [
    // ... existing declarations
    TimePickerComponent,
    ActivityRowComponent,
    DocumentUploadComponent,
  ],
  // ...
})
```

### Step 2: Update Parent Component HTML

#### Replace Time Pickers

**Before** (Lines 372-398):
```html
<div class="col-sm-6 col-md-4 col-lg-3 mb-1">
  <div class="info-wrapper">
    <label class="info-label" style="margin-bottom: 1px;">
      <span class="text-danger">*</span>In Time
    </label>
    <div class="d-flex flex-nowrap gap-2 align-items-center">
      <!-- Hour, minute, period selects -->
    </div>
  </div>
</div>
```

**After**:
```html
<div class="col-sm-6 col-md-4 col-lg-3 mb-1">
  <app-time-picker
    [label]="'In Time'"
    [required]="true"
    [selectedHour]="selectedInHour"
    [selectedMinute]="selectedInMinute"
    [selectedPeriod]="selectedInPeriod"
    (timeChange)="onApmosysInTimeChange($event)">
  </app-time-picker>
</div>
```

#### Replace Activity Rows

**Before** (Lines 605-766):
```html
<div *ngFor="let activityObj of allTimesheetActivities; let i = index" class="row">
  <!-- Large activity form HTML -->
</div>
```

**After**:
```html
<app-activity-row
  *ngFor="let activityObj of allTimesheetActivities; let i = index"
  [activityObj]="activityObj"
  [index]="i"
  [isLast]="i === allTimesheetActivities.length - 1"
  [isFirst]="i === 0"
  [canAdd]="allTimesheetActivities.length === 1 || i === allTimesheetActivities.length - 1"
  [filteredClients]="filteredClients"
  [isUpdation]="isUpdation"
  [timesheetFillable]="timesheetFillable"
  (clientChange)="getClientLocationList($event.activity, $event.clientId)"
  (clientLocationChange)="getProjectList($event)"
  (projectChange)="getAllActivitiesByProjectIdandEmpId($event, $event)"
  (activityChange)="setActivity($event)"
  (addActivity)="addInputActivityField($event)"
  (removeActivity)="removeInputActivityField($event)"
  (validateClientName)="validateClientName($event.event, $event.clientId)"
  (validateClientLocation)="validateClientLocation($event.event, $event.clientLocationId)"
  (validateActivity)="validateActivity($event.event, $event.activityId)"
  (validateDescription)="validateDescription($event, $event)"
  (validateTime)="validateTime($event.event, $event.completionTime)">
</app-activity-row>
```

#### Replace Document Uploads

**Before** (Lines 302-321):
```html
<div class="col-md-4 mb-0">
  <div class="info-wrapper">
    <div class="form-group">
      <label class="info-label">
        <span class="text-danger">*</span>Filled Attendance Proof
      </label>
      <div class="d-flex align-items-center">
        <input type="file" ...>
        <button ...>Preview</button>
      </div>
    </div>
  </div>
</div>
```

**After**:
```html
<div class="col-md-4 mb-0">
  <app-document-upload
    [label]="'Filled Attendance Proof'"
    [required]="true"
    [fileName]="fileName1"
    [fileError]="fileError1"
    [previewUrl]="previewUrl1"
    (fileSelected)="onFileSelected($event, 'doc1')"
    (previewClicked)="openPreviewModalForTwo('doc1')">
  </app-document-upload>
</div>
```

### Step 3: Update Parent Component TypeScript

Add handler methods for the new component events:

```typescript
// Time picker handlers
onApmosysInTimeChange(time: {hour: string, minute: string, period: string}): void {
  this.selectedInHour = time.hour;
  this.selectedInMinute = time.minute;
  this.selectedInPeriod = time.period;
  this.makeApmosysInTime();
}

onApmosysOutTimeChange(time: {hour: string, minute: string, period: string}): void {
  this.selectedOutHour = time.hour;
  this.selectedOutMinute = time.minute;
  this.selectedOutPeriod = time.period;
  this.makeApmosysOutTime();
}

onClientInTimeChange(time: {hour: string, minute: string, period: string}): void {
  this.selectedClientInHour = time.hour;
  this.selectedClientInMinute = time.minute;
  this.selectedClientInPeriod = time.period;
  this.makeClientInTime();
}

onClientOutTimeChange(time: {hour: string, minute: string, period: string}): void {
  this.selectedClientOutHour = time.hour;
  this.selectedClientOutMinute = time.minute;
  this.selectedClientOutPeriod = time.period;
  this.makeClientOutTime();
}
```

## Additional Recommended Components (Future Work)

### 4. **Timesheet Form Component** (`timesheet-form/`)
**Purpose**: Main form section for creating/editing timesheets
- Project selection
- Date/Day type selection  
- Time entry sections
- Activities section
- Document upload section

### 5. **Timesheet List Component** (`timesheet-list/`)
**Purpose**: Table view for listing timesheets
- Filter/search functionality
- Table display
- Pagination
- Export functionality

### 6. **Timesheet Bulk Upload Component** (`timesheet-bulk-upload/`)
**Purpose**: Bulk document upload form
- Date range selection
- File upload
- Upload validation

## Benefits of This Refactoring

1. **Maintainability**: Smaller, focused components are easier to understand and modify
2. **Reusability**: Components like `time-picker` and `document-upload` can be reused elsewhere
3. **Testability**: Smaller components are easier to unit test
4. **Performance**: OnPush change detection can be applied to child components
5. **Code Organization**: Clear separation of concerns

## Migration Checklist

- [ ] Add new components to module declarations
- [ ] Replace time picker HTML with `<app-time-picker>`
- [ ] Replace activity rows with `<app-activity-row>`
- [ ] Replace document uploads with `<app-document-upload>`
- [ ] Add event handler methods in parent component
- [ ] Test create timesheet flow
- [ ] Test update timesheet flow
- [ ] Test all validation scenarios
- [ ] Test file upload and preview
- [ ] Verify all modes (self, asShadow, team) work correctly

## Notes

- All existing functionality should remain unchanged
- The parent component still manages the overall state and business logic
- Child components emit events for parent to handle
- Two-way binding is maintained through `[(ngModel)]` where needed




