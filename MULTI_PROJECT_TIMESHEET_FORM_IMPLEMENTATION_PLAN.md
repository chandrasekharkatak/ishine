# Multi-Project Timesheet Form - Implementation Plan

## Overview
This document outlines the plan to adapt the existing timesheet creation functionality to support **multiple projects per timesheet**, where each project has its own in/out times and activities.

## Current vs New Structure

### Current Structure (Single Project)
```typescript
timesheetObj: {
  projectId: number,
  officeInTime: string,
  officeOutTime: string,
  clientInTime: string,
  clientOutTime: string,
  allTimesheetActivities: Activity[]  // All activities for single project
}
```

### New Structure (Multiple Projects)
```typescript
timesheetObj: {
  // Common fields
  date: string,
  dayType: string,
  timesheetAppliedFor: string,
  empId: number,
  
  // Multiple project entries
  projectEntries: ProjectEntry[] = [
    {
      projectId: number,
      clientSideId: string,
      officeInTime: string,
      officeOutTime: string,
      clientInTime: string,
      clientOutTime: string,
      totalWorkingHours: string,
      clientApprovalStatus: string,
      activities: ActivityNew[]
    }
  ]
}
```

---

## Functions to Copy & Adapt

### 1. **Core Creation Functions**

#### `onCreateTimesheet(template: TemplateRef<any>)`
**Current Location**: `my-timesheet.component.ts:1577`

**Changes Required:**
- Transform single project structure to multi-project structure
- Loop through `projectEntries` array
- For each project entry:
  - Format times (officeInTime, officeOutTime, clientInTime, clientOutTime)
  - Calculate total working hours per project
  - Map activities to project entry
- Aggregate total working minutes across all projects
- Handle documents (filled + approved) - still 2 max per timesheet (not per project)

**New Implementation:**
```typescript
onCreateTimesheet(template: TemplateRef<any>) {
  const dateFormat = 'YYYY-MM-DD';
  const dateTimeFormat = 'YYYY-MM-DD HH:mm:ss';
  
  // Validate all project entries
  let inputValidated: boolean = this.validateMultiProjectTimesheet(template);
  if (!inputValidated) return;
  
  // Prepare request DTO
  const createRequest: CreateTimesheetRequestDTO = {
    empId: this.timesheetObj.empId || this.currentUser.empId,
    date: moment(this.fromDate || this.timesheetObj.date).format(dateFormat),
    dayTypeId: this.getDayTypeId(this.timesheetObj.dayType),
    dayType: this.timesheetObj.dayType,
    status: this.getPendingStatusId(),
    description: this.timesheetObj.description?.trim(),
    leaveTypeMasterId: this.timesheetObj.leaveTypeMasterId,
    timesheetAppliedFor: this.timesheetObj.timesheetAppliedFor,
    currentManagerId: this.getCurrentManagerId(),
    isNightShift: this.timesheetObj.isNightShift,
    createdBy: this.currentUser.empId,
    createdByName: this.currentUser.name,
    
    // Transform project entries
    projectEntries: this.timesheetProjects.map(project => ({
      projectId: project.projectId,
      clientSideId: project.clientSideId,
      officeInTime: this.formatDateTime(project.officeInTime, dateTimeFormat),
      officeOutTime: this.formatDateTime(project.officeOutTime, dateTimeFormat),
      clientInTime: this.formatDateTime(project.clientInTime, dateTimeFormat),
      clientOutTime: this.formatDateTime(project.clientOutTime, dateTimeFormat),
      clientApprovalStatus: project.clientApprovalStatus,
      hasClientSideId: project.hasClientSideId,
      shadowEmpId: project.shadowEmpId,
      isShadowTimesheet: project.isShadowTimesheet,
      activities: project.activities.map(activity => ({
        activityId: activity.activityId,
        clientId: activity.clientId,
        clientLocationId: activity.clientLocationId,
        teamId: activity.teamId,
        description: activity.description,
        durationMinutes: this.hoursToMinutes(activity.completionTime),
        durationHours: activity.completionTime
      }))
    }))
  };
  
  // Call new API
  this.timesheetService.addTimesheetWithClientNew(
    createRequest, 
    this.selectedFile, 
    this.selectedFile2
  ).pipe(first()).subscribe((response: any) => {
    if (response.serviceStatus == "Success") {
      this.resetTimesheetForm();
      this.openAlertMod(template, response.serviceResponse);
      this.showViewMyTimesheets();
      // Refresh timesheet list
      this.refreshTimesheetList();
    } else {
      this.openAlertMod(template, response.serviceResponse);
    }
  });
}
```

---

### 2. **Validation Functions**

#### `validateTimesheetObj()` → `validateMultiProjectTimesheet()`
**Current Location**: `my-timesheet.component.ts:1401`

**New Validation Logic:**

```typescript
validateMultiProjectTimesheet(template: TemplateRef<any>): boolean {
  // 1. Common validations
  if (!this.validationService.validateNullUndefinedEmptyString(this.fromDate || this.timesheetObj.date)) {
    this.alertMessage = "Please enter Date !!";
    this.openAlertMod(template, this.alertMessage);
    return false;
  }

  if (!this.validationService.validateNullUndefinedEmptyString(this.timesheetObj.dayType)) {
    this.alertMessage = "Please select Day Type !!";
    this.openAlertMod(template, this.alertMessage);
    return false;
  }

  // 2. For non-working days (Public Holiday, Week Off, etc.)
  if (this.isNonWorkingDay(this.timesheetObj.dayType)) {
    if (!this.validationService.validateNullUndefinedEmptyString(this.timesheetObj.description)) {
      this.alertMessage = "Please enter Timesheet Description !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    return true; // No project validation needed
  }

  // 3. For working days - validate projects
  if (this.timesheetProjects.length === 0) {
    this.alertMessage = "Please add at least one project !!";
    this.openAlertMod(template, this.alertMessage);
    return false;
  }

  // 4. Validate each project entry
  for (let i = 0; i < this.timesheetProjects.length; i++) {
    const project = this.timesheetProjects[i];
    
    // 4.1 Project selection
    if (!project.projectId) {
      this.alertMessage = `Please select Project for Project Entry ${i + 1} !!`;
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    // 4.2 Client Side ID validation (if required)
    if (!this.clientSideIdNotMandatory && !project.hasClientSideId) {
      if (!project.clientSideId) {
        this.alertMessage = `Please enter Client Side ID for Project ${i + 1} !!`;
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    // 4.3 Office In/Out Time validation
    if (!this.validationService.validateNullUndefinedEmptyString(project.officeInTime)) {
      this.alertMessage = `Please enter Office In Time for Project ${i + 1} !!`;
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(project.officeOutTime)) {
      this.alertMessage = `Please enter Office Out Time for Project ${i + 1} !!`;
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    // 4.4 Validate time range
    const officeIn = moment(project.officeInTime);
    const officeOut = moment(project.officeOutTime);
    if (officeOut.isBefore(officeIn) || officeOut.isSame(officeIn)) {
      this.alertMessage = `Office Out Time must be after In Time for Project ${i + 1} !!`;
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    // 4.5 Client In/Out Time validation (if client side ID exists)
    if (project.hasClientSideId && !this.clientSideIdNotMandatory) {
      if (!this.validationService.validateNullUndefinedEmptyString(project.clientInTime)) {
        this.alertMessage = `Please enter Client In Time for Project ${i + 1} !!`;
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (!this.validationService.validateNullUndefinedEmptyString(project.clientOutTime)) {
        this.alertMessage = `Please enter Client Out Time for Project ${i + 1} !!`;
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      // Validate client time range
      const clientIn = moment(project.clientInTime);
      const clientOut = moment(project.clientOutTime);
      if (clientOut.isBefore(clientIn) || clientOut.isSame(clientIn)) {
        this.alertMessage = `Client Out Time must be after In Time for Project ${i + 1} !!`;
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    // 4.6 Activities validation
    if (project.activities.length === 0) {
      this.alertMessage = `Please add at least one activity for Project ${i + 1} !!`;
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    let projectTotalActivityTime = 0;
    for (let j = 0; j < project.activities.length; j++) {
      const activity = project.activities[j];
      
      // Validate required fields
      if (!activity.clientId) {
        this.alertMessage = `Please select Client for Activity ${j + 1} in Project ${i + 1} !!`;
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (!activity.clientLocationId) {
        this.alertMessage = `Please select Client Location for Activity ${j + 1} in Project ${i + 1} !!`;
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (!activity.teamId) {
        this.alertMessage = `Please select Team for Activity ${j + 1} in Project ${i + 1} !!`;
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (!activity.activityId) {
        this.alertMessage = `Please select Activity for Activity ${j + 1} in Project ${i + 1} !!`;
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      // Validate description
      if (activity.description && activity.description.trim()) {
        if (!this.validationService.validateActivityTimesheetDiscription(activity.description)) {
          this.alertMessage = `Please enter valid Activity Description for Activity ${j + 1} in Project ${i + 1} !!`;
          this.openAlertMod(template, this.alertMessage);
          return false;
        }
      }

      // Validate completion time
      if (!activity.completionTime || activity.completionTime <= 0) {
        this.alertMessage = `Please enter valid Activity Completion Time for Activity ${j + 1} in Project ${i + 1} !!`;
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (!this.validationService.validateCompletionTime(activity.completionTime)) {
        this.alertMessage = `Please enter valid Activity Completion Time (0-24 hrs) for Activity ${j + 1} in Project ${i + 1} !!`;
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      projectTotalActivityTime += activity.completionTime;
    }

    // 4.7 Validate activity time vs project working hours
    const projectWorkingHours = this.calculateProjectWorkingHours(project);
    if (projectTotalActivityTime > projectWorkingHours) {
      this.alertMessage = `Total Activity Time (${projectTotalActivityTime} hrs) cannot exceed Project Working Hours (${projectWorkingHours} hrs) for Project ${i + 1} !!`;
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    // 4.8 Validate total activity time (0-24 hrs)
    if (projectTotalActivityTime <= 0 || projectTotalActivityTime > 24) {
      this.alertMessage = `Total Activity Time must be between 0 and 24 hours for Project ${i + 1} !!`;
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
  }

  // 5. Document validation (if client approval status requires it)
  const requiresDocuments = this.timesheetProjects.some(p => 
    p.clientApprovalStatus === 'pending' || p.clientApprovalStatus === 'approved'
  );
  
  if (requiresDocuments) {
    const hasPendingStatus = this.timesheetProjects.some(p => p.clientApprovalStatus === 'pending');
    const hasApprovedStatus = this.timesheetProjects.some(p => p.clientApprovalStatus === 'approved');
    
    if (hasPendingStatus && !this.selectedFile) {
      this.alertMessage = "Please upload Filled Attendance Proof document !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    
    if (hasApprovedStatus && (!this.selectedFile || !this.selectedFile2)) {
      this.alertMessage = "Please upload both Filled and Approved Attendance Proof documents !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
  }

  return true;
}
```

---

### 3. **Project Management Functions**

#### `addProject()`
**Current Location**: `timesheet-form.component.ts:129`

**Adaptation:**
```typescript
addProject(): void {
  // Validate last project before adding new one
  if (this.timesheetProjects.length > 0) {
    const lastProject = this.timesheetProjects[this.timesheetProjects.length - 1];
    if (!this.isProjectValid(lastProject)) {
      this.alertMessage = 'Please complete at least one activity in the current project before adding a new project.';
      this.openAlertMod(this.alertTemplate, this.alertMessage);
      return;
    }
  }
  
  const newProject = this.createProject();
  this.timesheetProjects.push(newProject);
  this.expandedProjectIndex = this.timesheetProjects.length - 1; // Expand new project
}
```

#### `removeProject(index: number)`
**Current Location**: `timesheet-form.component.ts:140`

**Adaptation:**
```typescript
removeProject(index: number): void {
  if (this.timesheetProjects.length <= 1) {
    this.alertMessage = 'At least one project is required.';
    this.openAlertMod(this.alertTemplate, this.alertMessage);
    return;
  }
  
  this.timesheetProjects.splice(index, 1);
  
  // Adjust expanded index
  if (this.expandedProjectIndex === index) {
    this.expandedProjectIndex = null;
  } else if (this.expandedProjectIndex > index) {
    this.expandedProjectIndex--;
  }
}
```

#### `isProjectValid(project: ProjectEntry): boolean`
**Current Location**: `timesheet-form.component.ts:123`

**Enhanced Validation:**
```typescript
isProjectValid(project: ProjectEntry): boolean {
  // Check if project has required fields
  if (!project.projectId) return false;
  if (!project.officeInTime || !project.officeOutTime) return false;
  
  // Check if at least one activity is complete
  return project.activities.some(activity => this.isActivityComplete(activity));
}
```

---

### 4. **Activity Management Functions**

#### `addActivity(project: ProjectEntry)`
**Current Location**: `timesheet-form.component.ts:146`

**No changes needed** - already works per project

#### `removeActivity(project: ProjectEntry, index: number)`
**Current Location**: `timesheet-form.component.ts:150`

**Enhancement:**
```typescript
removeActivity(project: ProjectEntry, index: number): void {
  if (project.activities.length <= 1) {
    this.alertMessage = 'At least one activity is required per project.';
    this.openAlertMod(this.alertTemplate, this.alertMessage);
    return;
  }
  
  project.activities.splice(index, 1);
  this.calculateProjectHours(project); // Recalculate
}
```

#### `isActivityComplete(activity: ActivityNew): boolean`
**Current Location**: `timesheet-form.component.ts:111`

**No changes needed**

---

### 5. **Time Management Functions**

#### `makeApmosysInTime()` → `makeProjectOfficeInTime(project: ProjectEntry)`
**Current Location**: `my-timesheet.component.ts` (needs to be found)

**New Implementation:**
```typescript
makeProjectOfficeInTime(project: ProjectEntry): void {
  if (!project.selectedInHour || !project.selectedInMinute || !project.selectedInPeriod) {
    project.officeInTime = null;
    return;
  }
  
  const date = moment(this.fromDate || this.timesheetObj.date);
  const timeString = `${project.selectedInHour}:${project.selectedInMinute} ${project.selectedInPeriod}`;
  project.officeInTime = date.format('YYYY-MM-DD') + ' ' + moment(timeString, 'hh:mm A').format('HH:mm:ss');
  
  // Recalculate working hours
  this.calculateProjectWorkingHours(project);
}
```

#### `makeApmosysOutTime()` → `makeProjectOfficeOutTime(project: ProjectEntry)`
Similar adaptation

#### `makeClientInTime()` → `makeProjectClientInTime(project: ProjectEntry)`
Similar adaptation

#### `makeClientOutTime()` → `makeProjectClientOutTime(project: ProjectEntry)`
Similar adaptation

---

### 6. **Project Selection Functions**

#### `onProjectSelect(projectId: number)` → `onProjectSelect(project: ProjectEntry, projectId: number)`
**Current Location**: `my-timesheet.component.ts` (needs to be found)

**New Implementation:**
```typescript
onProjectSelect(project: ProjectEntry, projectId: number): void {
  project.projectId = projectId;
  
  // Get project details
  const selectedProject = this.activeProjectList.find(p => p.projectId === projectId);
  if (selectedProject) {
    project.projectName = selectedProject.projectName;
  }
  
  // Get client side ID for this project
  this.getClientSideIdByProjectIdAndEmpId(projectId, this.timesheetObj.empId || this.currentUser.empId)
    .then(clientSideId => {
      project.clientSideId = clientSideId;
      project.hasClientSideId = !!clientSideId;
    });
  
  // Load activities for this project
  this.loadActivitiesForProject(project);
}
```

---

### 7. **Activity Loading Functions**

#### `getAllActivitiesByProjectIdandEmpId()` → `loadActivitiesForProject(project: ProjectEntry)`
**Current Location**: `my-timesheet.component.ts:2035`

**New Implementation:**
```typescript
loadActivitiesForProject(project: ProjectEntry): void {
  if (!project.projectId) return;
  
  const timesheetObj = {
    projectId: project.projectId,
    empId: this.timesheetObj.empId || this.currentUser.empId,
    shadowEmpId: project.shadowEmpId || this.timesheetObj.shadowEmpId
  };
  
  this.timesheetService.getAllActivitiesByProjectIdandEmpId(timesheetObj)
    .pipe(first())
    .subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        project.availableActivities = response.serviceResponse;
      }
    });
}
```

---

### 8. **Client/Location Functions**

#### `getClientLocationList()` → `getClientLocationListForProject(project: ProjectEntry, activity: ActivityNew)`
**Current Location**: `my-timesheet.component.ts` (needs to be found)

**Adaptation:**
```typescript
getClientLocationListForProject(project: ProjectEntry, activity: ActivityNew, clientId: number): void {
  // Filter clients by project
  const projectClients = this.clientList.filter(c => c.projectId === project.projectId);
  activity.clientLocationList = projectClients
    .find(c => c.clientId === clientId)?.clientLocations || [];
}
```

---

### 9. **Helper Functions**

#### `calculateProjectWorkingHours(project: ProjectEntry): number`
**New Function:**
```typescript
calculateProjectWorkingHours(project: ProjectEntry): number {
  if (!project.officeInTime || !project.officeOutTime) return 0;
  
  const inTime = moment(project.officeInTime);
  const outTime = moment(project.officeOutTime);
  const diffHours = outTime.diff(inTime, 'hours', true);
  
  project.totalWorkingHours = diffHours.toFixed(2);
  return diffHours;
}
```

#### `hoursToMinutes(hours: number): number`
**New Function:**
```typescript
hoursToMinutes(hours: number): number {
  return Math.round(hours * 60);
}
```

#### `formatDateTime(dateTime: string, format: string): string`
**New Function:**
```typescript
formatDateTime(dateTime: string, format: string): string {
  if (!dateTime) return null;
  return moment(dateTime).isValid() ? moment(dateTime).format(format) : null;
}
```

#### `isNonWorkingDay(dayType: string): boolean`
**New Function:**
```typescript
isNonWorkingDay(dayType: string): boolean {
  return ['Public Holiday', 'Week Off', 'Leave', 'Client Holiday'].includes(dayType);
}
```

---

## Data Model Updates

### ProjectEntry Model Enhancement
```typescript
export class ProjectEntry {
  projectId: number | null;
  projectName?: string;
  clientSideId?: string;
  hasClientSideId?: boolean;
  
  // Office times
  officeInTime: string;
  officeOutTime: string;
  selectedInHour?: string;
  selectedInMinute?: string;
  selectedInPeriod?: string;
  selectedOutHour?: string;
  selectedOutMinute?: string;
  selectedOutPeriod?: string;
  
  // Client times
  clientInTime?: string;
  clientOutTime?: string;
  selectedClientInHour?: string;
  selectedClientInMinute?: string;
  selectedClientInPeriod?: string;
  selectedClientOutHour?: string;
  selectedClientOutMinute?: string;
  selectedClientOutPeriod?: string;
  
  totalWorkingHours: string;
  totalClientWorkingHours?: string;
  
  // Client approval
  clientApprovalStatus?: string;
  
  // Shadow
  shadowEmpId?: number;
  isShadowTimesheet?: boolean;
  
  // Activities
  activities: ActivityNew[];
  availableActivities?: ActivityNew[]; // Loaded activities for dropdown
}
```

---

## Validation Summary

### Per-Project Validations:
1. ✅ Project selection required
2. ✅ Client Side ID (if required)
3. ✅ Office In/Out Time required and valid
4. ✅ Client In/Out Time (if client side ID exists)
5. ✅ At least one activity required
6. ✅ Activity time ≤ Project working hours
7. ✅ Total activity time: 0-24 hours

### Cross-Project Validations:
1. ✅ At least one project required (for working days)
2. ✅ Documents: Filled doc for "pending", Both docs for "approved"
3. ✅ Date and Day Type required
4. ✅ Description required for non-working days

---

## Files to Modify

1. **`timesheet-form.component.ts`**
   - Add all validation functions
   - Add project/activity management
   - Add time calculation functions
   - Add `onCreateTimesheet()` method

2. **`timesheet-form.component.html`**
   - Already has multi-project structure
   - May need minor adjustments for validation messages

3. **`projectEntry.ts`** (Model)
   - Enhance with new fields (time pickers, client approval, etc.)

4. **`timesheet.service.ts`**
   - Add `addTimesheetWithClientNew()` method

---

## Implementation Steps

1. ✅ Review this plan
2. ⏳ Get approval
3. ⏳ Update `ProjectEntry` model
4. ⏳ Implement validation functions
5. ⏳ Implement project/activity management
6. ⏳ Implement time management functions
7. ⏳ Implement `onCreateTimesheet()` with API call
8. ⏳ Test with single project
9. ⏳ Test with multiple projects
10. ⏳ Test validations

---

## Questions for Review

1. **Documents**: Should documents be per-project or per-timesheet? (Current plan: per-timesheet, max 2)
2. **Client Approval Status**: Should it be per-project or global? (Current plan: per-project)
3. **Shadow Timesheet**: Should shadow settings be per-project or global? (Current plan: per-project)
4. **Time Sync**: Should "Same as ApMoSys" sync apply to all projects or per-project? (Current plan: per-project)

---

**Ready for Review - Please provide feedback before implementation**

