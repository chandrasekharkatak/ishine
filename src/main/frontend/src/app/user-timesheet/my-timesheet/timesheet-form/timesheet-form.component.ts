import { Component, Input, OnInit, Output, EventEmitter, TemplateRef, ViewChild } from '@angular/core';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import * as moment from 'moment';
import { first } from 'rxjs';
import { AppComponent } from 'src/app/app.component';
import { Employee } from 'src/app/models/employee';
import { Timesheet } from 'src/app/models/timesheet';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { TeamViewService } from 'src/app/services/team-view.service';
import { TimesheetService } from 'src/app/services/timesheet.service';
import { ProjectEntry } from 'src/app/models/projectEntry';
import { ActivityNew } from 'src/app/models/activityNew';
import { EmployeeClientSideIdMapping } from 'src/app/models/employeeClientSideIdMapping';


import { EmployeeTimesheetDTO } from 'src/app/models/EmployeeTimesheetDTO';
import { ProjectTimesheetDTO } from 'src/app/models/ProjectTimesheetDTO';
import { ActivityTimesheetDTO } from 'src/app/models/ActivityTimesheetDTO';
import { TimesheetNewService } from 'src/app/services/timesheet-new.service';
import { TimesheetDocument } from 'src/app/models/timesheetDocument';
import { LocationEntry } from 'src/app/models/locationEntry';
import { TimesheetDocumentDataI } from './types';
import { TimesheetValidationService } from 'src/app/services/TimesheetValidationService/timesheet-validation.service';
import { TimesheetConfigService } from 'src/app/services/TimesheetValidationService/timesheet-config.service';

@Component({
  standalone: false,
  selector: 'app-timesheet-form',
  templateUrl: './timesheet-form.component.html',
  styleUrl: './timesheet-form.component.css'
})
export class TimesheetFormComponent implements OnInit {

  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;
  @ViewChild("previewTemplate")
  previewModal: TemplateRef<any>;
  @ViewChild("clientSideIdUpdateOrAddModal")
  clientSideIdUpdateOrAddModal: TemplateRef<any>;
  clientSideIdUpdateOrAddModalRef: NgbModalRef;
  alertWithResetModRef: NgbModalRef;
  modalRef: NgbModalRef;
  @Input() isCreation: boolean = false;
  @Input() isUpdation: boolean = false;
  @Input() isView: boolean = false;
  @Input() selectedDate: Date | null = null;
  @Input() timesheetId: number | null = null; // ID of timesheet to update
  @Input() existingTimesheetData: EmployeeTimesheetDTO | null = null; // Pre-loaded data (optional)
  // @Input() isAutoFilled: boolean = false;
  isTimesheetLockCheckEnable: any = "true";
  
  // Output events for parent component communication
  @Output() timesheetUpdated = new EventEmitter<number>();
  @Output() updateCancelled = new EventEmitter<void>();
  
  // Loading state for update mode
  isLoadingTimesheet: boolean = false;
  // timesheetObj: Timesheet = new Timesheet();
  selectedTeamMember: any;
  timesheetFilledForUser: User = new User();
  dayType: number;
  timesheetAppliedFor: string ;
  selectedLocationId: any = null;
  teamMemberList: any[] = [];
  currentUser: User = new User();
  alertMessage: string;
  disableCreateUpdateTimesheet: boolean = false;
  fromDate: any = null;
  toDate: any = null;
  disableAdd: boolean = false;
  clientLocationList: any[] = [];
  projectList: any[] = [];
  availableTimesheets: any[] = [];
  serverDate: any; // Server's current date for date range calculation
  minDateForPicker: string | null = null; // Minimum selectable date (dd-MM-yyyy format)
  maxDateForPicker: string | null = null; // Maximum selectable date (dd-MM-yyyy format)
  disabledDatesForPicker: string[] = []; // Dates to disable (dd-MM-yyyy format)
  allDayTypes: any[] = [];
  empClientSideObj: EmployeeClientSideIdMapping = new EmployeeClientSideIdMapping();
  apmosysInTime: any = null;
  apmosysOutTime: any = null;
  clientInTime: any = null;
  clientOutTime: any = null;
  totalPresence: number = 0;
  activeProjectList: any[] = []; // Unique projects for dropdown
  activeLocationList: any[] = []; // Unique locations for dropdown
  // Multi-location support (Location -> Project -> Activity)
  uniqueProjectsList: ProjectEntry[] = [];
  timesheetLocations: LocationEntry[] = [];
  expandedLocationIndex: number | null = null;
  expandedProjectIndexMap: { [locationIndex: number]: number | null } = {}; // Track expanded project per location
  empHasClientSideId: boolean = false;
  projectActivityHoursError: { [projectIndex: number]: string } = {}; // Store validation errors per project

  // File upload properties
  selectedFile: File[] = [];
  activePreviewUrl: SafeResourceUrl | null = null;
  activeFileType: string | null = null;
  createOrUpdateObj: EmployeeTimesheetDTO = new EmployeeTimesheetDTO();
  clientApprovalStatusList: any[];
  workLocationList: any[];
  isNightShift: boolean = false;
  documentData: TimesheetDocumentDataI[] = [];
  @Input() autoFillTimesheet: boolean = false;
  activeRawObjectUrl: any;
  useApmosysTiming = false;

  imageZoom = 1;
  rotation = 0;
  isFullscreen = false;

  isDragging = false;
  startX = 0;
  startY = 0;
  translateX = 0;
  translateY = 0;
  highlightLocationList: number[] = [];
  highlightLocationIdSet = new Set<number>();
  activeProjectListByEmpId: any[] = []
  clientIdEntryBulletPoints: string[] = ["Mandatory field for all resources while filling the timesheet.",
    "Enter the client-side ID if already available.",
    "If the client-side ID is not yet assigned, enter “NA (ApMoSys Employee ID)”.",
    "Once the client-side ID is received, update the ID while filling subsequent timesheets."]
  constructor(private teamViewService: TeamViewService,
    private timesheetService: TimesheetService,
    private timesheetNewService: TimesheetNewService,
    private timesheetValidator: TimesheetValidationService,
    private configService:TimesheetConfigService,
    private modalService: NgbModal,
    private authenticationService: AuthenticationService,
    private sanitizer: DomSanitizer) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x); }

  ngOnInit(): void {
    // Initialize form with default values
    this.timesheetAppliedFor = 'self';
    this.onTimesheetAppliedForChange();
    this.getAllWorkLocationFromLocationMaster();
    this.getAllDayTypes();
    this.getAllDSRApprovalStatusFromMaster();
    
    // Load server date and available timesheets for date filtering
    this.loadServerDate();
    
    // Handle update mode
    if (this.isUpdation && this.timesheetId) {
      this.loadTimesheetForUpdate(this.timesheetId);
    } else if(this.isUpdation && this.existingTimesheetData) {
      this.populateFormFromTimesheetData(this.existingTimesheetData);
    } else {
      // For creation mode, load available timesheets after determining employee
      // This will be called in onTimesheetAppliedForChange()
    }
  }


  // ============================================
  // HELPER METHODS - Day Type & Date Utilities
  // ============================================

  /**
   * Checks if employee should be disabled in team member selection
   */
  disableEmployee = (employee: any): boolean => {
    return employee?.empId === this.currentUser?.empId;
  };

  /**
   * Checks if the current day type is fillable (requires time entries)
   * Uses TimesheetConfigService for centralized configuration
   */
  isDayTypeFillable(): boolean {
    return this.configService.isDayTypeFillable(this.dayType);
  }

  /**
   * Format date to DD-MM-YYYY format
   */
  formatDateDDMMYYYY(date: Date): string {
    return `${String(date.getDate()).padStart(2, '0')}-${String(date.getMonth() + 1).padStart(2, '0')}-${date.getFullYear()}`;
  }

  // ============================================
  // METHODS - Entity Creation (Location, Project, Activity)
  // ============================================
  /**
   * Create a new ActivityNew object with default values
   */
  createActivity(timesheetId: number = null, projectId: number): ActivityNew {
    return {
      activityId: null,
      description: '',
      durationMinutes: null,
      projectId: projectId,
      teamId: null,
      timesheetId: timesheetId,
      clientTeamList: [],
      allActivitiesForProject: []
    };
  }

  /**
   * Create a new LocationEntry object with default values
   * Sets default work location type ID for non-fillable day types
   */
  createLocation(timesheetId?: number): LocationEntry {
    if (this.isDayTypeFillable() == true) {
      return {
        locationMappingId: null,
        workLocationType: null,
        workLocationTypeId: null,
        locationInTime: null,
        locationOutTime: null,
        totalWorkingHours: null,
        projects: [this.createProject(null, timesheetId)],
      };
    }
    else {
      return {
        locationMappingId: null,
        workLocationType: null,
        workLocationTypeId: this.configService.getDefaultWorkLocationTypeId(),
        locationInTime: null,
        locationOutTime: null,
        totalWorkingHours: null,
        projects: [this.createProject(null, timesheetId)],
      };
    }
  }

  /**
   * Create a new ProjectEntry object with default values
   */
  createProject(location: LocationEntry, timesheetId: number): ProjectEntry {
    return {
      projectId: null,
      projectName: '',
      clientSideId: null,
      hasClientSideId: false,
      hasClientFlag: false,
      shadowEmpId: null,
      isShadowTimesheet: false,
      isShadowForSelf: false,
      // Client, Team, Location at project level
      clientId: null,
      clientLocationId: null,
      clientApprovalStatus: null,
      activities: [this.createActivity(null, null)],
      projectActivities: [], // Activities loaded for the project
      clientList: [],
      clientLocationList: [],
      projectList: [],
      shadowForList: [],
      poId: null,
      poNo: '',
      status: 1,
      locationMappingId: !location?.locationMappingId ? null : location.locationMappingId,
      projectHoursMinutes: null,
      timesheetId: timesheetId,
      totalClientWorkingMinutes: null,
      totalWorkingHours: null,
      description: null
    };
  }

  // ============================================
  // METHODS - Location Management
  // ============================================

  /**
   * Apply ApMoSys timing to the first location
   */
  applyApmosysTiming(workLocatioId: number): void {

    if (this.useApmosysTiming) {
      this.timesheetLocations = [this.createLocation()];
      this.disableAdd = true
      this.timesheetLocations[0].workLocationTypeId = workLocatioId;
      this.timesheetLocations[0].locationInTime = this.apmosysInTime;
      this.timesheetLocations[0].locationOutTime = this.apmosysOutTime;
      this.onLocationSelect(this.timesheetLocations[0]);
    } else {
      // OPTION 1: Clear timings when unchecked
      this.disableAdd = false
      this.timesheetLocations[0].locationInTime = null;
      this.timesheetLocations[0].locationOutTime = null;

      this.onHoursChange();
    }

  }

  addLocation(timesheetId: number): void {
    this.timesheetLocations.push(this.createLocation(timesheetId));
    // Expand the newly added location
    this.expandedLocationIndex = this.timesheetLocations.length - 1;
    // Initialize expanded project index for this location
    this.expandedProjectIndexMap[this.timesheetLocations.length - 1] = 0;
  }

  /**
   * Remove a location from the timesheet
   * Ensures at least one location remains
   * Cleans up associated document data and file references
   */
  removeLocation(index: number): void {
    if (this.timesheetLocations.length <= 1) {
      this.openAlertMod(this.alertTemplate, 'At least one location is required.');
      return;
    }
    
    // Cleanup document data for projects in this location
    const location = this.timesheetLocations[index];
    if (location && location.projects) {
      location.projects.forEach(project => {
        if (project.projectId) {
          this.cleanupProjectDocuments(project.projectId);
        }
      });
    }
    
    this.timesheetLocations.splice(index, 1);
    // ✅ Don't set empHasClientSideId = false here - let getListToRenderUpload() handle it
    // It will check ALL remaining projects and set the correct value
    this.getListToRenderUpload();
    
    // Adjust expanded index if needed
    if (this.expandedLocationIndex === index) {
      this.expandedLocationIndex = this.timesheetLocations.length > 0 ? 0 : null;
    } else if (this.expandedLocationIndex > index) {
      this.expandedLocationIndex--;
    }
    
    // Clean up expanded project index map
    delete this.expandedProjectIndexMap[index];
    // Reindex the map
    const newMap: { [key: number]: number | null } = {};
    Object.keys(this.expandedProjectIndexMap).forEach(key => {
      const oldIndex = parseInt(key);
      if (oldIndex < index) {
        newMap[oldIndex] = this.expandedProjectIndexMap[oldIndex];
      } else if (oldIndex > index) {
        newMap[oldIndex - 1] = this.expandedProjectIndexMap[oldIndex];
      }
    });
    this.expandedProjectIndexMap = newMap;
  }

  /**
   * Cleanup document data for a specific project
   * Revokes object URLs to prevent memory leaks
   */
  private cleanupProjectDocuments(projectId: number): void {
    const docsToRemove = this.documentData.filter(
      doc => doc.projectId === projectId
    );
    docsToRemove.forEach(doc => {
      if (doc.rawObjectUrl) {
        URL.revokeObjectURL(doc.rawObjectUrl);
      }
    });
    this.documentData = this.documentData.filter(
      doc => doc.projectId !== projectId
    );
    
    // Also remove from selectedFile array
    this.selectedFile = this.selectedFile.filter((file) => {
      if (!file) return true;
      const doc = docsToRemove.find(d => d.uniqueIdentifier === file.name);
      return !doc;
    });
  }

  /**
   * Toggle location expansion/collapse state
   */
  toggleLocation(index: number): void {
    this.expandedLocationIndex = this.expandedLocationIndex === index ? null : index;
  }

  /**
   * Add a new project to a location
   * Validates that projects are available before adding
   */
  addProject(location: LocationEntry, timesheetId: number): void {
    if (!this.activeProjectList || this.activeProjectList.length === 0) {
      this.openAlertMod(
        this.alertTemplate,
        'No projects available. Please ensure projects are loaded before adding.'
      );
      return;
    }
    
    location.projects.push(this.createProject(location, timesheetId));
    // Expand the newly added project
    const locationIndex = this.timesheetLocations.indexOf(location);
    this.expandedProjectIndexMap[locationIndex] = location.projects.length - 1;
    const uniqueProjects = Array.from(
      new Map(
        this.activeProjectList.map(p => [
          p.projectId,
          {
            projectId: p.projectId,
            projectName: p.projectName
          }
        ])
      ).values()
    );

    this.timesheetLocations.forEach(loc => {
      loc.projects.forEach(proj => {
        proj.projectList = uniqueProjects;
      });
    });
  }

  /**
   * Remove a project from a location
   * Ensures at least one project remains per location
   * Cleans up associated document data
   */
  removeProject(location: LocationEntry, projectIndex: number): void {
    if (location.projects.length <= 1) {
      this.openAlertMod(this.alertTemplate, 'At least one project is required per location.');
      return;
    }
    
    // Cleanup document data for this project
    const project = location.projects[projectIndex];
    if (project && project.projectId) {
      this.cleanupProjectDocuments(project.projectId);
    }
    
    location.projects.splice(projectIndex, 1);
    this.getListToRenderUpload();
    
    // Adjust expanded project index if needed
    const locationIndex = this.timesheetLocations.indexOf(location);
    const currentExpanded = this.expandedProjectIndexMap[locationIndex];
    if (currentExpanded === projectIndex) {
      this.expandedProjectIndexMap[locationIndex] = location.projects.length > 0 ? 0 : null;
    } else if (currentExpanded !== null && currentExpanded > projectIndex) {
      this.expandedProjectIndexMap[locationIndex] = currentExpanded - 1;
    }
  }

  // ============================================
  // METHODS - Client Side ID Management
  // ============================================

  /**
   * Get formatted employee ID placeholder for client side ID input
   */
  get formattedEmployeeId(): string {
    if (this.currentUser.isApmosysProduct) {
      return `NA (AP-${this.currentUser.employeementId})`;
    }
    return `NA (A-${this.currentUser.employeementId})`;
  }

  /**
   * Open client side ID update/add modal
   */
  openClientSideTemplate(project: ProjectEntry): void {
    this.empClientSideObj.clientSideId = '';
    this.clientSideIdUpdateOrAddModalRef = this.modalService.open(
      this.clientSideIdUpdateOrAddModal,
      { modalDialogClass: 'modal-lg' }
    );
  }

  /**
   * Hide client side ID form modal
   */
  hideClientSideIdForm(): void {
    this.clientSideIdUpdateOrAddModalRef.close();
  }

  /**
   * Update client side ID mapping
   * Validates client side ID before updating
   */
  updateClientSideIdMapping(template: TemplateRef<any>): void {
    if (!this.empClientSideObj.clientSideId || this.empClientSideObj.clientSideId.trim() === '') {
      this.openAlertMod(template, 'Please enter a valid Client Side ID.');
      return;
    }

    if (this.empClientSideObj.hasClientSideFlag) {
      if (this.empClientSideObj.clientSideId.toLowerCase().startsWith("na")) {
        this.modalRef?.close();
        this.empClientSideObj.clientSideId = '';
        this.openAlertMod(template, 'As per the configuration defined by your project manager, Client IDs for this project cannot begin with “NA”. Kindly provide the valid Client ID assigned to you. For additional assistance, please reach out to your project manager.');
        return;
      }
    }
    this.timesheetService.updateClientSideIdMapping(this.empClientSideObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.getClientSideIdByProjectIdAndEmpId(this.empClientSideObj.projectId, this.empClientSideObj.empId);
      } else {
        this.openAlertMod(template, response.serviceResponse)
      }
    });
  }

  /**
   * Cancel client side ID update and refresh data
   */
  onCancelClientSideId(): void {
    this.hideClientSideIdForm();
    this.getClientSideIdByProjectIdAndEmpId(this.empClientSideObj.projectId, this.empClientSideObj.empId);
  }

  

  /**
   * Toggle project expansion/collapse state within a location
   */
  toggleProject(locationIndex: number, projectIndex: number): void {
    const currentExpanded = this.expandedProjectIndexMap[locationIndex];
    this.expandedProjectIndexMap[locationIndex] = currentExpanded === projectIndex ? null : projectIndex;
  }

  /**
   * Add a new activity to a project
   * Validates that client and client location are selected before adding
   */
  addActivity(project: ProjectEntry): void {
    // Validate that client and client location are selected
    if (!project.clientId || !project.clientLocationId) {
      this.openAlertMod(
        this.alertTemplate,
        'Please select Client and Client Location before adding activities.'
      );
      return;
    }
    
    // ✅ Validate that clientDetails is available
    if (!project.clientDetails || !project.clientDetails.project) {
      this.openAlertMod(
        this.alertTemplate,
        'Client details not loaded. Please reselect the project.'
      );
      return;
    }
    
    this.timesheetLocations.forEach(loc => {
      loc.projects.forEach(proj => {
        if (proj === project) {
          const newActivity = this.createActivity(null, project.projectId);
          
          // ✅ Use teams from clientDetails API response instead of filtering activeProjectList
          if (proj.clientDetails && proj.clientDetails.project) {
            newActivity.clientTeamList = proj.clientDetails.project.teams.map(team => ({
              teamId: team.teamId,
              teamName: team.teamName
            }));
            
            if (newActivity.clientTeamList.length == 1) {
              newActivity.teamId = newActivity.clientTeamList[0].teamId;
              this.onProjectTeamSelect(newActivity.teamId, proj);
            }
          }
          
          proj.activities.push(newActivity);
        }
      });
    });
  }

  /**
   * Remove an activity from a project
   * Ensures at least one activity remains per project
   * Recalculates project total hours after removal
   */
  removeActivity(project: ProjectEntry, index: number): void {
    if (project.activities.length <= 1) {
      this.openAlertMod(this.alertTemplate, 'At least one activity is required per project.');
      return;
    }
    project.activities.splice(index, 1);
    
    // Recalculate project hours
    let projHours = 0;
    if (project.activities && Array.isArray(project.activities)) {
      project.activities.forEach(activity => {
        projHours += Number(activity.durationMinutes) || 0;
      });
    }
    project.totalWorkingHours = projHours;
    
    // Validate totals to ensure consistency
    this.onHoursChange();
  }

  // ============================================
  // METHODS - Selection Handlers (Location, Project, Client, etc.)
  // ============================================

  /**
   * Handle location selection - populate projects for the selected location
   * Validates for duplicate locations
   */
  onLocationSelect(location: LocationEntry): void {
    const duplicateCount = this.timesheetLocations.filter(
      loc => loc !== location &&
        loc.workLocationTypeId === location.workLocationTypeId
    ).length;
    if (duplicateCount > 1) {
      this.openAlertMod(this.alertTemplate, 'You are entering duplicate location');

      // Reset ONLY the selected location
      location.workLocationTypeId = null;
      location.workLocationType = null;
      return;
    }

    // Check if date is available (projects should already be fetched)
    if (!this.fromDate) {
      this.openAlertMod(this.alertTemplate, 'Please select Date first before selecting Location.');
      location.workLocationTypeId = null;
      location.workLocationType = null;
      return;
    }

    // ✅ Only update THIS location
    if (!this.isDayTypeFillable()) {
      location.locationMappingId = 4;
    }
    // ✅ Don't set empHasClientSideId = false here - let getListToRenderUpload() handle it
    // It will check ALL projects and set the correct value based on all projects

    // Check if projects are already loaded (from date selection)
    if (!this.activeProjectList || this.activeProjectList.length === 0) {
      // Projects should have been loaded when date was selected
      // If not, fetch them now (fallback scenario)
      let empId: number;
      if (this.timesheetAppliedFor.toLowerCase() === 'self') {
        empId = this.currentUser.empId;
      } else {
        const teamMember = this.teamMemberList.find(
          e => e.empId === this.timesheetFilledForUser.empId
        );
        empId = teamMember?.empId;
      }
      
      if (empId) {
        this.getProjectListForDateAndEmpId(empId);
        // Wait for projects to load, then populate location
        setTimeout(() => {
          this.populateProjectsForLocation(location);
        }, 500);
        this.getListToRenderUpload();
        return;
      }
    }

    // Use already-fetched projects from activeProjectList
    this.populateProjectsForLocation(location);
    this.getListToRenderUpload();
  }

  // ============================================
  // METHODS - Master Data Loading
  // ============================================

  /**
   * Get all day types from master data
   */
  getAllDayTypes(): void {
    this.timesheetNewService.getAllDayTypes().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDayTypes = response.serviceResponse;
        if (this.selectedDate && this.allDayTypes.length > 0) {
          this.loadAutofillData(this.formatDateDDMMYYYY(this.selectedDate));
        }
      }
    });
  }
  /**
   * Handle day type change
   * Updates location work location type ID based on fillable status
   */
  onDayTypeChange(event: any): void {
    this.timesheetLocations.forEach(loc => {
      if (!this.isDayTypeFillable()) {
        loc.workLocationTypeId = this.configService.getDefaultWorkLocationTypeId();
        this.disableAdd = true;
        this.onLocationSelect(loc);
      } else {
        loc.workLocationTypeId = null;
        this.disableAdd = false;
      }
    });
  }
  
  /**
   * Handle timesheet application target change (self/team)
   * Prevents reset in update mode
   */
  onTimesheetAppliedForChange(): void {
    // Don't reset form if in update mode
    if (this.isUpdation) {
      return;
    }
    
    this.resetForm();
    if (this.timesheetAppliedFor.toLocaleLowerCase() === 'self') {
      this.getTimesheetMetadata();
      // getTimesheetMetadata will call getAllAvailableTimesheetByEmpId
    } else {
      this.getAllTeamMemberList();
    }
  }
  // ============================================
  // METHODS - Modal & Alert Management
  // ============================================

  /**
   * Open alert modal with message
   */
  openAlertMod(template: TemplateRef<any>, message: any): void {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  /**
   * Get timesheet metadata for current user or selected team member
   */
  getTimesheetMetadata(): void {
    let userObj: User = new User();
    if (this.timesheetAppliedFor.toLocaleLowerCase() == 'self') {
      userObj.empId = this.currentUser.empId;
      userObj.isTimesheetLockCheckEnable = this.currentUser.isTimesheetLockCheckEnable;
    } else {
      const teamMember = this.teamMemberList.find(employee => employee.empId == this.timesheetFilledForUser.empId);
      userObj.empId = teamMember?.empId;
      userObj.isTimesheetLockCheckEnable = teamMember.isTimesheetLockCheckEnable;
     
    }
    this.timesheetFilledForUser = userObj;
    console.log("onTimesheetAppliedForChange obj= ",userObj)
    // Load available timesheets for date filtering after setting user
    if (userObj.empId && this.serverDate) {
      this.getAllAvailableTimesheetByEmpId(userObj);
    }
  }

  /**
   * Get all team members for the current user
   */
  getAllTeamMemberList(): void {
    this.resetForm();
    let employeeObj = new Employee();
    employeeObj.empId = this.currentUser.empId;
    this.teamViewService.getAllTeamMemberView(employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.teamMemberList = response.serviceResponse;
      }
    });
  }

  /**
   * Handle team member selection
   * Load available timesheets for selected team member
   */
  onTeamMemberSelect(teamMember: any): void {
    if (teamMember && teamMember.empId) {
      this.timesheetFilledForUser.empId = teamMember.empId;
      this.timesheetFilledForUser.name = teamMember.name;
      this.selectedTeamMember = teamMember;
      this.getTimesheetMetadata();
      // Load available timesheets for date filtering
      if (this.serverDate) {
        this.getAllAvailableTimesheetByEmpId(this.timesheetFilledForUser);
      }
    }
  }

  /**
   * Populate projects for a specific location from activeProjectList
   * Projects should already be fetched via getProjectListForDateAndEmpId() when date was selected
   */
  populateProjectsForLocation(location: LocationEntry): void {
    if (!this.activeProjectList || this.activeProjectList.length === 0) {
      if (this.timesheetAppliedFor.toLocaleLowerCase() == 'self') {
        this.disableAdd = true;
        this.openAlertMod(this.alertTemplate, 'No Projects assigned for the selected date. Please contact RMG.');
      } else if (this.timesheetAppliedFor.toLocaleLowerCase() == 'team') {
        this.disableAdd = true;
        let teamMember = this.teamMemberList.find(employee => employee.empId === this.timesheetFilledForUser.empId);
        this.openAlertMod(this.alertTemplate, 'No Projects assigned to ' + teamMember?.empName + ' for the selected date.');
      }
      return;
    }
    
    // Create unique projects list
    // Note: getProjectListForDateAndEmpId() only returns projectId and projectName
    // hasClientSideId and hasClientFlag may not be available, so use optional chaining
    const uniqueProjects = Array.from(
      new Map(
        this.activeProjectList.map(p => [
          p.projectId,
          {
            projectId: p.projectId,
            projectName: p.projectName,
            hasClientSideId: p.hasClientSideId || false,
            hasClientFlag: p.hasClientFlag || false
          }
        ])
      ).values()
    );
    
    // Populate projects for the selected location
    this.timesheetLocations.forEach(loc => {
      if (location.workLocationTypeId === loc.workLocationTypeId) {
        loc.projects = [this.createProject(location, null)]; // Reset to one project
        loc.projects.forEach(proj => {
          proj.projectList = uniqueProjects;
          if (proj.projectList.length == 1) {
            proj.projectId = proj.projectList[0].projectId;
          }
          if (proj.projectId) {
            this.onProjectSelect(proj.projectId);
          }
        });
      }
    });
  }

  /**
   * Handle project selection - populate clients and reset project-specific fields
   */
  onProjectSelect(projectId: number): void {
    this.timesheetLocations.forEach(loc => {
      loc.projects.forEach(proj => {
        if (proj.projectId === projectId) {
          const matchedProject = proj.projectList.find(
            p => p.projectId === proj.projectId
          );
          proj.hasClientSideId = matchedProject?.hasClientSideId || false;
          proj.hasClientFlag = matchedProject?.hasClientFlag || false;
          proj.projectName = matchedProject?.projectName || '';
          
          // Reset project-specific fields
          proj.shadowEmpId = null;
          proj.isShadowTimesheet = false;
          proj.isShadowForSelf = false;
          proj.clientSideId = null;
          proj.clientId = null;
          proj.clientLocationId = null;
          proj.clientApprovalStatus = null;
          proj.totalWorkingHours = null;
          proj.activities = [this.createActivity(null, projectId)];
          proj.projectActivities = [];
          proj.clientList = [];
          proj.clientLocationList = [];
          proj.clientDetails = null; // Reset client details
          
          // ✅ Fetch client details for this project (replaces incorrect filtering from activeProjectList)
          this.getClientDetailsByProjectIdAndEmpId(proj);
          
          // Fetch client side id (side effect)
          const targetEmpId = this.timesheetAppliedFor.toLowerCase() === 'self' 
            ? this.currentUser.empId 
            : this.timesheetFilledForUser.empId;
          this.getClientSideIdByProjectIdAndEmpId(projectId, targetEmpId);
          // ✅ Don't set empHasClientSideId = false here - let getListToRenderUpload() handle it
          // It will check ALL projects and set the correct value
          this.getListToRenderUpload();
        }
      });
    });
  }

  /**
   * Fetch client details for a specific project and employee
   * This replaces the old form's getClientDetailsByProjectIdAndEmpId()
   * Stores client details in project.clientDetails for use in dropdowns
   * @param project - Project entry to fetch client details for
   */
  getClientDetailsByProjectIdAndEmpId(project: ProjectEntry): void {
    if (!project.projectId) {
      console.error('Project ID is required to fetch client details');
      return;
    }
    
    // Determine empId (self or team member)
    const targetEmpId = this.timesheetAppliedFor.toLowerCase() === 'self' 
      ? this.currentUser.empId 
      : this.timesheetFilledForUser.empId;
    
    if (!targetEmpId) {
      console.error('Employee ID not available');
      return;
    }
    
    const payload = {
      empId: targetEmpId,
      projectId: project.projectId
    };
    
    this.timesheetService.getClientDetailsByProjectIdAndEmpId(payload)
      .pipe(first())
      .subscribe((response: any) => {
        if (response.serviceStatus === "Success") {
          // Store client details in the project object
          project.clientDetails = response.serviceResponse;
          
          // Populate client list (should be single client from API)
          if (project.clientDetails && project.clientDetails.clientId) {
            project.clientList = [{
              clientId: project.clientDetails.clientId,
              clientName: project.clientDetails.clientName
            }];
            
            // Auto-select client if only one (which is always the case from API)
            if (project.clientList.length === 1) {
              project.clientId = project.clientList[0].clientId;
              // Populate client locations immediately
              this.onProjectClientSelect(project.clientId, project);
            }
          } else {
            console.error('Client details response missing clientId');
            this.openAlertMod(
              this.alertTemplate,
              'Failed to load client information for this project.'
            );
          }
        } else {
          console.error('Failed to fetch client details:', response.serviceResponse);
          this.openAlertMod(
            this.alertTemplate,
            'Failed to load client details: ' + (response.serviceResponse || 'Unknown error')
          );
        }
      }, error => {
        console.error('Error fetching client details:', error);
        this.openAlertMod(
          this.alertTemplate,
          'Error loading client details. Please try again.'
        );
      });
  }

 /**
   * Get client side ID by project ID and employee ID
   * Opens modal if client side ID is required but not found
   */
  async getClientSideIdByProjectIdAndEmpId(projectId: any, empId: any) {
    await this.timesheetService.getClientSideIdByProjectIdAndEmpId(projectId, empId)
      .pipe(first())
      .subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        let clientSideId = response.serviceResponse;
        // ✅ Don't set empHasClientSideId here - let getListToRenderUpload() handle it
        // This method only sets clientSideId for the specific project
        // getListToRenderUpload() will check ALL projects and set empHasClientSideId correctly
        if (clientSideId) {
          this.empClientSideObj.clientSideId = clientSideId;
          this.empClientSideObj.projectId = projectId;
          this.empClientSideObj.empId = empId;
          this.timesheetLocations.forEach(location => {
            location.projects.forEach(project => {
              if (project.projectId == projectId) {
                project.clientSideId = clientSideId;
                this.empClientSideObj.projectName = project.projectName;
                this.empClientSideObj.hasClientSideId = project.hasClientSideId;
                this.empClientSideObj.hasClientSideFlag = project.hasClientFlag;
              }
            });
          });
        }
      } else {
        this.timesheetLocations.forEach(location => {
          location.projects.forEach(project => {
            if (project.projectId == projectId && project.hasClientSideId) {
              this.empClientSideObj.projectId = projectId;
              this.empClientSideObj.empId = empId;
              this.empClientSideObj.projectName = project.projectName;
              this.empClientSideObj.hasClientSideId = project.hasClientSideId;
              this.empClientSideObj.hasClientSideFlag = project.hasClientFlag;
              this.openClientSideTemplate(project);
            }
          });
        });
      }
    });
  }
  /**
   * Handle client selection at project level - populate client locations
   * @param clientId - Selected client ID
   * @param project - Project entry where client was selected
   */
  onProjectClientSelect(clientId: number, project: ProjectEntry): void {
    // Find the project where client was selected
    this.timesheetLocations.forEach(loc => {
      loc.projects.forEach(proj => {
        if (proj === project && proj.clientDetails) {
          // ✅ Use clientLocations from clientDetails API response
          proj.clientLocationList = proj.clientDetails.clientLocations.map(loc => ({
            clientLocationId: loc.clientLocationId,
            clientLocation: loc.clientLocation
          }));
          
          // Auto-select if only one location
          if (proj.clientLocationList.length === 1) {
            proj.clientLocationId = proj.clientLocationList[0].clientLocationId;
            this.onProjectClientLocationSelect(proj.clientLocationId, proj);
          }
        }
      });
    });
  }

  /**
   * Handle client location selection at project level - populate teams
   * Validates for duplicate client locations
   */
  /**
   * Handle client location selection at project level - populate teams
   * @param clientLocationId - Selected client location ID
   * @param project - Project entry where location was selected
   */
  onProjectClientLocationSelect(clientLocationId: number, project: ProjectEntry): void {
    // Validate that clientLocationId is not null
    if (!clientLocationId) {
      return;
    }
    
    const processedProjects = new Set<string>();
    this.timesheetLocations.forEach(loc => {
      loc.projects.forEach(proj => {
        const projectKey = `${proj.projectId}_${proj.clientId}_${proj.clientLocationId}`;

        // ❌ skip if already processed
        if (processedProjects.has(projectKey)) {
          proj.clientLocationId = null;
          this.openAlertMod(this.alertTemplate, 'You cannot add two projects with same client location')
          return;
        }

        // ✅ mark as processed
        processedProjects.add(projectKey);
        
        // ✅ Use teams from clientDetails API response instead of filtering activeProjectList
        if (proj === project && proj.clientDetails && proj.clientDetails.project) {
          const teams = proj.clientDetails.project.teams.map(team => ({
            teamId: team.teamId,
            teamName: team.teamName
          }));
          
          // Populate teams for all activities in this project
          proj.activities.forEach(activity => {
            activity.clientTeamList = teams;
            if (activity.clientTeamList.length == 1) {
              activity.teamId = activity.clientTeamList[0].teamId;
              this.onProjectTeamSelect(activity.teamId, proj);
            }
          });
        }
      }
      );
    });
  }

  /**
   * Handle team selection at project level - load activities for the project
   */
  onProjectTeamSelect(teamId: number, project: ProjectEntry): void {
    // Load activities for the project
    this.loadActivitiesForProject(project, teamId);
  }

  onClientApprovalStatusSelect(location: LocationEntry, project: ProjectEntry, status: any) {
    if (!status) {
      project.clientApprovalStatus = null;
      project.projectActivities = [];
      project.activities.forEach(activity => {
        activity.activityId = null;
      });
      // ✅ Update document list when approval status is cleared
      this.getListToRenderUpload();
      return;
    }
    project.clientApprovalStatus = status;
    // ✅ Update document list when approval status changes (affects which documents to show)
    this.getListToRenderUpload();
  }

  /**
   * Load activities for a project filtered by department
   */
  loadActivitiesForProject(project: ProjectEntry, teamId: number): void {
    if (!project.clientId || !project.clientLocationId) {
      console.error('Client and Client Location must be selected first');
      return;
    }

    const timesheetObj = new Timesheet();
    timesheetObj.projectId = project.projectId;
    timesheetObj.empId = project.isShadowTimesheet && project.shadowEmpId
      ? project.shadowEmpId
      : (this.timesheetFilledForUser.empId || this.currentUser?.empId);
    timesheetObj.teamId = teamId;
    timesheetObj.clientId = project.clientId;
    timesheetObj.clientLocationId = project.clientLocationId;

    if (!timesheetObj.empId) {
      return;
    }
    this.timesheetService.getAllActivitiesByProjectIdandEmpId(timesheetObj)
      .pipe(first())
      .subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          let allActivityList = response.serviceResponse;
          if (allActivityList.length === 0) {
            this.openAlertMod(this.alertTemplate, "No activity found for your department!");
            this.disableAdd = true;
          }
          else {
            allActivityList = allActivityList.sort((a, b) => a.activity.localeCompare(b.activity));
            // Filter by department
            if (this.timesheetAppliedFor == "team") {
              const teamMember = this.teamMemberList.find(emp => emp.empId == this.timesheetFilledForUser.empId);
              if (teamMember) {
                allActivityList = allActivityList.filter(x =>
                  x.departmentList?.map(d => +d).includes(teamMember.departmentId)
                );
              }
            } else if (this.timesheetAppliedFor == "self") {
              allActivityList = allActivityList.filter(x =>
                x.departmentList?.map(d => +d).includes(this.currentUser?.departmentId)
              );
            }
          }

          // Store activities at project level for all activities to use
          this.timesheetLocations.forEach(loc => {
            loc.projects.forEach(proj => {
              if (proj === project) {
                proj.activities.forEach(activity => {
                  activity.allActivitiesForProject = allActivityList;
                })
              }
              });
            });
          } else {
            project.projectActivities = [];
          }
        }, (error) => {
          project.projectActivities = [];
        });
  }

  /**
   * Load server date for date range calculation
   */
  loadServerDate(): void {
    this.timesheetService.getServerDate().pipe(first()).subscribe((response: any) => {
      this.serverDate = response;
      // After server date is loaded, calculate constraints if employee is known
      if (this.timesheetFilledForUser?.empId || this.currentUser?.empId) {
        const empId = this.timesheetFilledForUser?.empId || this.currentUser.empId;
        this.getAllAvailableTimesheetByEmpId({ empId: empId } as User);
      }
    });
  }

  /**
   * Get all available timesheets for an employee within date range
   * This populates availableTimesheets[] which contains dates to disable
   */
  getAllAvailableTimesheetByEmpId(employeeObj: User): void {
    if (!employeeObj?.empId) return;
    
    this.availableTimesheets = [];
    const DAY_IN_MS = 24 * 60 * 60 * 1000;
    let currentDate = new Date();
    let endDate: any;
    let startDate: any;
    let OPEN_BACKDATED_DAYS = 30;

    if (this.currentUser.timesheetBackDatedDays) {
      OPEN_BACKDATED_DAYS = this.currentUser.timesheetBackDatedDays;
    }

    if (this.isTimesheetLockCheckEnable == 'false') {
      endDate = currentDate;
      startDate = new Date(endDate.getTime() - ((OPEN_BACKDATED_DAYS + 1) * DAY_IN_MS));
    } else {
      endDate = currentDate;
      startDate = new Date(endDate.getTime() - ((this.currentUser.timesheetLockDays + 1) * DAY_IN_MS));
    }

    let timesheetObj = new Timesheet();
    timesheetObj.empId = employeeObj.empId;
    timesheetObj.startDate = moment(startDate).format(AppComponent.DB_DATE_FORMAT);
    timesheetObj.endDate = moment(endDate).format(AppComponent.DB_DATE_FORMAT);
    timesheetObj.createdBy = this.currentUser.empId;
    
    this.timesheetNewService.getAllMyTimesheetsByEmpId(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.availableTimesheets = response.serviceResponse;
        // Calculate date picker constraints after loading timesheets
        this.calculateDatePickerConstraints();
      } else {
        console.error(response.serviceResponse);
        // Still calculate constraints even if no timesheets found
        this.calculateDatePickerConstraints();
      }
    });
  }

  /**
   * Calculate date picker constraints (minDate, maxDate, disabledDates)
   * Based on timesheetDateFilter logic from old component
   */
  calculateDatePickerConstraints(): void {
    if (!this.serverDate) {
      // Wait for server date to be loaded
      return;
    }

    const DAY_IN_MS = 24 * 60 * 60 * 1000;
    const CURRENT_DAY = 1;
    const dateFormat = 'YYYY-MM-DD';
    let OPEN_BACKDATED_DAYS = 30;

    // Calculate days difference from date of joining
    let currentDate = new Date();
    let daysDifference = 365; // Default to 365 days if dateOfJoining not available
    console.log("this.currentUser.dateOfJoining ==> ",this.currentUser.dateOfJoining)
    if (this.currentUser.dateOfJoining) {
      let dateOfJoining = moment(this.currentUser.dateOfJoining, dateFormat);
      daysDifference = moment(currentDate, dateFormat).diff(dateOfJoining, 'days');
    }

    if (this.currentUser.timesheetBackDatedDays > daysDifference) {
      OPEN_BACKDATED_DAYS = daysDifference;
    } else {
      OPEN_BACKDATED_DAYS = this.currentUser.timesheetBackDatedDays || 30;
    }

    // Calculate end date (server date)
    const dateObj = new Date(this.serverDate + 'T23:59:59');
    let serverDate = dateObj;

    // Calculate start date based on lock check enable flag
    let startDate: Date;
    if (this.isTimesheetLockCheckEnable == "false") {
      startDate = new Date(serverDate.getTime() - ((OPEN_BACKDATED_DAYS + CURRENT_DAY) * DAY_IN_MS));
    } else {
      const lockDays = this.currentUser.timesheetLockDays || 30; // Default to 30 if not set
      startDate = new Date(serverDate.getTime() - ((lockDays + CURRENT_DAY) * DAY_IN_MS));
    }

    // Set min/max dates for picker (convert to dd-MM-yyyy format)
    this.minDateForPicker = moment(startDate).format('DD-MM-YYYY');
    this.maxDateForPicker = moment(serverDate).format('DD-MM-YYYY');

    console.log("minDateForPicker===> ",this.minDateForPicker);
    console.log("maxDateForPicker===> ",this.maxDateForPicker);
    console.log("disabledDatesForPicker===> ",this.disabledDatesForPicker);
    console.log("availableTimesheets count===> ",this.availableTimesheets.length);

    // Extract dates from availableTimesheets to disable
    // Handle update mode: exclude current timesheet date
    this.disabledDatesForPicker = this.availableTimesheets
      .filter((ts: any) => {
        // If updating, exclude current timesheet date
        if (this.isUpdation && this.timesheetId && ts.timesheetId === this.timesheetId) {
          return false;
        }
        return true;
      })
      .map((ts: any) => {
        // Convert date to dd-MM-yyyy format for date picker component
        // Backend returns date as LocalDate (yyyy-MM-dd format) or Date object
        let dateStr: string;
        if (typeof ts.date === 'string') {
          // Backend returns date as "yyyy-MM-dd" string (LocalDate)
          dateStr = moment(ts.date, 'YYYY-MM-DD').format('DD-MM-YYYY');
        } else if (ts.date instanceof Date) {
          // If date is Date object
          dateStr = moment(ts.date).format('DD-MM-YYYY');
        } else {
          // Try to parse as-is (moment handles various formats)
          dateStr = moment(ts.date).format('DD-MM-YYYY');
        }
        return dateStr;
      })
      .filter((date: string) => {
        // Remove invalid dates and ensure format is correct
        return date !== 'Invalid date' && 
               date !== 'Invalid Date' && 
               moment(date, 'DD-MM-YYYY', true).isValid();
      });
  }

  /**
   * Handle night shift toggle change
   * Auto-sets to date if night shift is enabled
   */
  onNightShiftChange(): void {
    if (this.isNightShift) {
      if (!this.fromDate) {
        this.openAlertMod(
          this.alertTemplate,
          'Please select From Date first before enabling Night Shift.'
        );
        this.isNightShift = false;
        return;
      }

      const fromDate = this.parseDDMMYYYY(this.fromDate);
      if (!fromDate) return;

      this.toDate = this.formatDDMMYYYY(this.addDays(fromDate, 1));
    } else {
      this.toDate = null;
    }

    // Recalculate working hours when night shift changes
    this.calculateTotalWorkingHours();
  }


  onFromDateChange() {
    if (!this.fromDate) {
      this.totalPresence = 0;
      return;
    }


    if (this.isNightShift) {
      const fromDate = this.parseDDMMYYYY(this.fromDate);
      if (!fromDate) return;

      this.toDate = this.formatDDMMYYYY(this.addDays(fromDate, 1));
    }

    // Recalculate working hours when date changes
    this.calculateTotalWorkingHours();
    this.getProjectListForDateAndEmpId();

    // 
  }


  /**
   * Handle to date change
   * Validates to date is exactly one day after from date for night shift
   */
  onToDateChange(): void {

    if (!this.fromDate) {
      this.openAlertMod(this.alertTemplate, 'Please select From Date first.');
      this.toDate = null;
      this.calculateTotalWorkingHours();
      return;
    }

    const fromDate = this.parseDDMMYYYY(this.fromDate);
    const toDate = this.parseDDMMYYYY(this.toDate);

    if (!fromDate || !toDate) {
      this.openAlertMod(this.alertTemplate, 'Invalid date format.');
      this.toDate = null;
      this.calculateTotalWorkingHours();
      return;
    }

    const diffDays =
      (toDate.getTime() - fromDate.getTime()) / (1000 * 60 * 60 * 24);

    if (diffDays !== 1) {
      this.openAlertMod(
        this.alertTemplate,
        'To Date must be exactly one day after From Date for Night Shift.'
      );
      this.isNightShift = false;
      this.onNightShiftChange();
    } else {
      // Recalculate working hours when toDate changes
      this.calculateTotalWorkingHours();
    }
  }


  /**
   * Parse DD-MM-YYYY date string to Date object
   */
  parseDDMMYYYY(dateStr: string): Date | null {
    if (!dateStr) return null;

    const parts = dateStr.split('-');
    if (parts.length !== 3) return null;

    const dd = Number(parts[0]);
    const mm = Number(parts[1]);
    const yyyy = Number(parts[2]);

    if (!dd || !mm || !yyyy) return null;

    return new Date(yyyy, mm - 1, dd);
  }

  /**
   * Format Date object to DD-MM-YYYY string
   */
  formatDDMMYYYY(date: Date): string {
    const dd = String(date.getDate()).padStart(2, '0');
    const mm = String(date.getMonth() + 1).padStart(2, '0');
    const yyyy = date.getFullYear();

    return `${dd}-${mm}-${yyyy}`;
  }

  /**
   * Add days to a date
   */
  addDays(date: Date, days: number): Date {
    const d = new Date(date);
    d.setDate(d.getDate() + days);
    return d;
  }

  /**
   * Parse time string to 24-hour format
   * Handles both "HH:mm" (24-hour) and "HH:mm AM/PM" (12-hour) formats
   */
  private parseTimeTo24Hour(timeStr: string): { hour: number; minute: number } | null {
    if (!timeStr) return null;

    try {
      // Remove extra spaces and convert to uppercase for consistent parsing
      timeStr = timeStr.trim().toUpperCase();

      // Check if it's 12-hour format (contains AM or PM)
      const hasAMPM = timeStr.includes('AM') || timeStr.includes('PM');

      if (hasAMPM) {
        // Parse 12-hour format: "09:20 AM" or "07:20 PM"
        const timePart = timeStr.replace(/\s*(AM|PM)\s*/i, '');
        const parts = timePart.split(':');

        if (parts.length !== 2) return null;

        let hour = parseInt(parts[0], 10);
        const minute = parseInt(parts[1], 10);

        if (isNaN(hour) || isNaN(minute)) return null;

        // Validate ranges
        if (hour < 1 || hour > 12 || minute < 0 || minute > 59) return null;

        // Convert to 24-hour format
        const isPM = timeStr.includes('PM');
        if (isPM && hour !== 12) {
          hour += 12;
        } else if (!isPM && hour === 12) {
          hour = 0;
        }

        return { hour, minute };
      } else {
        // Parse 24-hour format: "09:20"
        const parts = timeStr.split(':');

        if (parts.length !== 2) return null;

        const hour = parseInt(parts[0], 10);
        const minute = parseInt(parts[1], 10);

        if (isNaN(hour) || isNaN(minute)) return null;

        // Validate ranges
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) return null;

        return { hour, minute };
      }
    } catch (error) {
      console.error('Error parsing time:', timeStr, error);
      return null;
    }
  }

  // ============================================
  // METHODS - Time & Hours Calculation
  // ============================================

  /**
   * Calculate total working hours from ApMoSys In Time and Out Time
   * Handles both normal shift and night shift scenarios
   */
  calculateTotalWorkingHours(): void {
    // Reset to 0 if required fields are missing
    if (!this.fromDate || !this.apmosysInTime || !this.apmosysOutTime) {
      this.totalPresence = 0;

      return;
    }

    try {
      // Parse fromDate (DD-MM-YYYY format)
      const fromDateParsed = this.parseDDMMYYYY(this.fromDate);
      if (!fromDateParsed) {
        this.totalPresence = 0;
        return;
      }

      // Determine which date to use for out time
      let outDate: Date;
      if (this.isNightShift && this.toDate) {
        // Use toDate for night shift
        const toDateParsed = this.parseDDMMYYYY(this.toDate);
        if (!toDateParsed) {
          this.totalPresence = 0;
          return;
        }
        outDate = toDateParsed;
      } else {
        // Use fromDate for normal shift
        outDate = fromDateParsed;
      }

      // Parse time strings (handles both HH:mm and HH:mm AM/PM formats)
      const inTime24 = this.parseTimeTo24Hour(this.apmosysInTime);
      const outTime24 = this.parseTimeTo24Hour(this.apmosysOutTime);

      if (!inTime24 || !outTime24) {
        this.totalPresence = 0;
        return;
      }

      const inHour = inTime24.hour;
      const inMinute = inTime24.minute;
      const outHour = outTime24.hour;
      const outMinute = outTime24.minute;

      // Create Date objects with date and time
      const inDateTime = new Date(fromDateParsed);
      inDateTime.setHours(inHour, inMinute, 0, 0);

      const outDateTime = new Date(outDate);
      outDateTime.setHours(outHour, outMinute, 0, 0);

      // Calculate difference in milliseconds
      const diffMs = outDateTime.getTime() - inDateTime.getTime();

      // Convert to hours (decimal)
      const calculatedHours = diffMs / (1000 * 60 * 60);

      // Ensure non-negative
      const finalHours = calculatedHours < 0 ? 0 : calculatedHours;

      // Round to 2 decimal places
      this.totalPresence = Math.round(finalHours * 100) / 100;
    } catch (error) {
      this.totalPresence = 0;
    }
  }

  /**
   * Handle ApMoSys In Time change
   * Updates location in-time if using ApMoSys timing
   */
  onApMoSysInTimeChange(time: string): void {
    this.apmosysInTime = time;
    if (this.useApmosysTiming) {
      this.timesheetLocations[0].locationInTime = this.apmosysInTime;
    }
    this.calculateTotalWorkingHours();
  }

  /**
   * Handle ApMoSys Out Time change
   * Updates location out-time if using ApMoSys timing
   */
  onApMoSysOutTimeChange(time: string): void {
    this.apmosysOutTime = time;
    if (this.useApmosysTiming) {
      this.timesheetLocations[0].locationOutTime = this.apmosysOutTime;
    }
    this.calculateTotalWorkingHours();
  }

  // ============================================
  // METHODS - Document Upload & File Management
  // ============================================

  /**
   * Handle file selection for document upload
   * Validates file type and size
   */
  onFileSelected(
    event: any,
    docType: 'Filled' | 'Approved',
    projectId: number
  ): void {

    const file: File = event.target.files?.[0];
    if (!file) return;

    const allowedTypes = ['application/pdf', 'image/jpeg', 'image/png', 'image/jpg'];
    const maxSize = 500 * 1024; // 500KB

    // ❌ Invalid type
    if (!allowedTypes.includes(file.type)) {
      this.handleFileError(
        projectId,
        docType,
        'Only PDF, JPG, JPEG, PNG files allowed.'
      );
      event.target.value = '';
      return;
    }

    // ❌ Size check
    if (file.size > maxSize) {
      this.handleFileError(
        projectId,
        docType,
        'File size must be 500KB or less.'
      );
      event.target.value = '';
      return;
    }

    // ♻️ Cleanup old object URL
    const previous = this.documentData.find(
      f => f.projectId === projectId && f.docType === docType
    );

    if (previous?.rawObjectUrl) {
      URL.revokeObjectURL(previous.rawObjectUrl);
    }

    // ✅ Detect file type ONCE
    const fileType: 'pdf' | 'image' =
      file.type === 'application/pdf' ? 'pdf' : 'image';

    // ✅ Create object URL
    const objectUrl = URL.createObjectURL(file);

    // ✅ IMPORTANT: Use correct sanitizer
    const previewUrl =
      fileType === 'pdf'
        ? this.sanitizer.bypassSecurityTrustResourceUrl(objectUrl)
        : this.sanitizer.bypassSecurityTrustUrl(objectUrl);

    const uniqueFile: File = this.renameFile(file, projectId, docType);
    const uniqueIdentifier = uniqueFile.name;

    // Update entry
    this.updateUploadFile({
      docId: null,
      projectId,
      docName: file.name,
      finalFlag: false,
      bulkApprovedDocId: null,
      uniqueIdentifier: uniqueIdentifier,
      docType,
      previewUrl,
      rawObjectUrl: objectUrl,
      fileType,
      fileSize: uniqueFile.size,
      fileError: null
    }, uniqueFile);
  }

  /**
   * Check if file exists in document data by unique identifier
   */
  containsFile(fileName: string): boolean {
    return this.documentData.some(
      f => f.uniqueIdentifier === fileName
    );
  }

  /**
   * Handle file upload error
   */
  private handleFileError(
    projectId: number,
    docType: 'Filled' | 'Approved',
    message: string
  ): void {
    this.updateUploadFile({
      docId: null,
      projectId,
      docName: null,
      finalFlag: false,
      bulkApprovedDocId: null,
      uniqueIdentifier: null,
      docType,
      previewUrl: null,
      rawObjectUrl: null,
      fileError: message,
      fileType: null,
      fileSize: null,
    });

    this.openAlertMod(this.alertTemplate, message);
  }


  /**
   * Update upload file data in document data array
   */
  updateUploadFile(
    data: TimesheetDocumentDataI,
    file?: File
  ): void {

    const index = this.documentData.findIndex(
      f => f.projectId === data.projectId && f.docType === data.docType
    );

    if (index === -1) return;

    this.documentData[index] = {
      ...this.documentData[index],
      ...data
    };

    if (this.selectedFile.length > 0 && this.selectedFile.some(f => f.name === data.uniqueIdentifier)) {
      this.selectedFile[index] = file;
    } else {
      this.selectedFile.push(file);
    }

  }

  // ============================================
  // METHODS - Image Preview & Manipulation
  // ============================================

  /**
   * Zoom in on image preview
   */
  zoomIn(): void {
    this.imageZoom = Math.min(this.imageZoom + 0.2, 3);
  }

  /**
   * Zoom out on image preview
   */
  zoomOut(): void {
    this.imageZoom = Math.max(this.imageZoom - 0.2, 0.5);
  }

  /**
   * Reset zoom to default (1x)
   */
  resetZoom(): void {
    this.imageZoom = 1;
  }

  /**
   * Download image from preview
   */
  downloadImage(fileName = 'image-preview'): void {
    // Ensure we have a raw object URL
    if (!this.activeRawObjectUrl || this.activeFileType !== 'image') return;

    const link = document.createElement('a');
    link.href = this.activeRawObjectUrl;
    link.download = fileName;
    link.click();
  }



  /**
   * Open preview modal for uploaded file
   * Handles both existing documents (from server) and new uploads
   */
  openPreviewModalForTwo(file: any): void {
    // Check if it's an existing document (has docId)
    if (file?.docId && this.isExistingDocument(file)) {
      // Load existing document from server
      this.previewExistingDocument(file.docId);
      return;
    }

    // Handle new upload (has previewUrl and fileType)
    if (!file?.previewUrl || !file?.fileType) {
      this.openAlertMod(
        this.alertTemplate,
        'Document preview is not available. Please upload the document first.'
      );
      return;
    }

    this.activePreviewUrl = file.previewUrl;
    this.activeFileType = file.fileType;
    this.activeRawObjectUrl = file.rawObjectUrl;

    this.resetTransformations();

    this.modalRef = this.modalService.open(this.previewModal, {
      modalDialogClass: 'modal-lg',
      scrollable: true
    });
  }

  /**
   * Get CSS transform string for image preview
   */
  get imageTransform(): string {
    return `
    translate(-50%, -50%)
    translate(${this.translateX}px, ${this.translateY}px)
    scale(${this.imageZoom})
    rotate(${this.rotation}deg)
  `;
  }


  /**
   * Reset all image transformations (zoom, rotation, pan)
   */
  resetTransformations(): void {
    this.imageZoom = 1;
    this.rotation = 0;
    this.translateX = 0;
    this.translateY = 0;
  }
  /* ---------- ROTATE ---------- */
  rotate(): void {
    this.rotation = (this.rotation + 90) % 360;
  }

  /**
   * Start dragging image preview
   */
  startDrag(event: MouseEvent): void {
    this.isDragging = true;
    this.startX = event.clientX - this.translateX;
    this.startY = event.clientY - this.translateY;
  }

  /**
   * Handle drag movement for image preview
   */
  onDrag(event: MouseEvent): void {
    if (!this.isDragging) return;

    this.translateX = event.clientX - this.startX;
    this.translateY = event.clientY - this.startY;
  }

  /**
   * Stop dragging image preview
   */
  stopDrag(): void {
    this.isDragging = false;
  }

  /* ---------- FULLSCREEN ---------- */
  toggleFullscreen(): void {
    this.isFullscreen = !this.isFullscreen;
  }

  /**
   * Show preview for base64 data (for existing documents from server)
   */
  showPreview(base64Data: string, mimeType: string): void {
    const dataUrl = `data:${mimeType};base64,${base64Data}`;
    this.activePreviewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(dataUrl);

    if (mimeType === 'application/pdf') {
      this.activeFileType = 'pdf';
    } else if (mimeType.startsWith('image/')) {
      this.activeFileType = 'image';
    } else {
      this.activeFileType = '';
    }

    // Open modal
    this.modalRef = this.modalService.open(this.previewModal, { modalDialogClass: 'modal-lg' });
  }
  /**
   * Rename file with project ID and document type prefix
   */
  renameFile(file: File, projectId: number, docType: 'Filled' | 'Approved'): File {
    const ext = file.name.substring(file.name.lastIndexOf('.'));
    const safeDocType = docType.toLowerCase(); // optional
    const newFileName = `${projectId}_${safeDocType}_${file.name}`;

    return new File([file], newFileName, { type: file.type });
  }
  /**
   * Check if project has filled document
   */
  hasFilledDocument(projectId: number): boolean {
    return this.documentData?.some(
      file => file.projectId === projectId && file.docType === 'Filled'
    );
  }
  /**
   * Check if project has approved document
   */
  hasApprovedDocument(projectId: number): boolean {
    return this.documentData?.some(
      file => file.projectId === projectId && file.docType === 'Approved'
    );
  }


  formatDateTimeForBackend(timeStr: string, dateStr: string): string {
    const parsed = this.parseTimeTo24Hour(timeStr);
    if (!parsed) return '';

    const hour = parsed.hour.toString().padStart(2, '0');  // "09" or "19"
    const minute = parsed.minute.toString().padStart(2, '0'); // "30"

    return `${dateStr} ${hour}:${minute}:00`;  // Returns "2026-01-08 09:30:00" or "2026-01-08 19:30:00"
  }

  formatTimeForBackend(timeStr: string): string {
    const parsed = this.parseTimeTo24Hour(timeStr);
    if (!parsed) return '';

    const hour = parsed.hour.toString().padStart(2, '0');  // "09" or "19"
    const minute = parsed.minute.toString().padStart(2, '0'); // "30"

    return `${hour}:${minute}:00`;  // Returns "2026-01-08 09:30:00" or "2026-01-08 19:30:00"
  }

  /**
   * Reset form to initial state
   * Cleans up all form data, document references, and UI state
   */
  resetForm(): void {
    // Basic fields
    this.dayType = null;
    this.fromDate = null;
    this.toDate = null;
    this.isNightShift = false;
    this.apmosysInTime = null;
    this.apmosysOutTime = null;
    this.totalPresence = 0;
    this.useApmosysTiming = false;
    
    // Locations
    this.timesheetLocations = [];
    this.addLocation(null);
    
    // Document data cleanup
    this.cleanupDocumentData();
    this.documentData = [];
    this.selectedFile = [];
    this.uniqueProjectsList = [];
    this.empHasClientSideId = false;
    
    // UI state
    this.highlightLocationList = [];
    this.highlightLocationIdSet = new Set();
    this.expandedLocationIndex = null;
    this.expandedProjectIndexMap = {};
    
    // Preview state
    this.resetPreviewState();
    
    // Flags
    this.disableAdd = false;
  }

  /**
   * Cleanup document data and revoke object URLs to prevent memory leaks
   */
  private cleanupDocumentData(): void {
    this.documentData.forEach(doc => {
      if (doc.rawObjectUrl) {
        URL.revokeObjectURL(doc.rawObjectUrl);
      }
    });
  }

  /**
   * Reset preview modal state
   */
  private resetPreviewState(): void {
    this.activePreviewUrl = null;
    this.activeFileType = null;
    this.activeRawObjectUrl = null;
    this.imageZoom = 1;
    this.rotation = 0;
    this.translateX = 0;
    this.translateY = 0;
    this.isFullscreen = false;
  }

  createTimesheet() {

    this.highlightLocationList = [];
    const convertToYYYYMMDD = (dateStr: string): string => {
      if (!dateStr) return '';
      const [day, month, year] = dateStr.split('-');
      return `${year}-${month}-${day}`;  // Convert "08-01-2026" to "2026-01-08"
    };

    // Prepare validation context
    const validationContext = {
      dayType: this.dayType,
      fromDate: this.fromDate,
      toDate: this.toDate,
      isNightShift: this.isNightShift,
      apmosysInTime: this.apmosysInTime,
      apmosysOutTime: this.apmosysOutTime,
      totalPresence: this.totalPresence,
      timesheetAppliedFor: this.timesheetAppliedFor,
      timesheetFilledForUser: this.timesheetFilledForUser,
      currentUser: this.currentUser,
      timesheetLocations: this.timesheetLocations,
      workLocationList: this.workLocationList,
      documentData: this.documentData,
      uniqueProjectsList: this.uniqueProjectsList,
      empHasClientSideId: this.empHasClientSideId
    };

    // Validate using the validation service
    const validationResult = this.timesheetValidator.validateCreate(validationContext);

    if (!validationResult.isValid) {
      // Format and display errors
      const errorMessage = this.timesheetValidator.formatErrorsForDisplay(validationResult);
      this.openAlertMod(this.alertTemplate, errorMessage);
      
      // Highlight locations with errors
      if (validationResult.highlightedLocationIds && validationResult.highlightedLocationIds.length > 0) {
        this.highlightLocationList = validationResult.highlightedLocationIds;
        this.applyHighlightAndExpand();
      }
      return;
    }

    // Display warnings if any (non-blocking - show after submission)
    // Warnings are informational and should not block timesheet creation
    if (validationResult.warnings && validationResult.warnings.length > 0) {
      const warningMessage = this.timesheetValidator.formatWarningsForDisplay(validationResult);
      // Show warning but don't block - use setTimeout to show after creation starts
      setTimeout(() => {
        this.openAlertMod(this.alertTemplate, warningMessage);
      }, 100);
    }
    
    const dataSet: LocationEntry[] = structuredClone(this.timesheetLocations);
    
    // Determine correct empId based on timesheet application target
    const targetEmpId = this.timesheetAppliedFor.toLowerCase() === 'self' 
      ? this.currentUser.empId 
      : (this.timesheetFilledForUser?.empId || this.currentUser.empId);
    
    this.createOrUpdateObj = {
      createdBy: this.currentUser.empId,
      dayTypeId: this.dayType,
      empId: targetEmpId,
      isApmosysProduct: this.currentUser.isApmosysProduct,
      isNightShift: this.isNightShift,
      timesheetId: null,
      // date: this.formatDDMMYYYY(new Date(this.fromDate as string)),
      date: convertToYYYYMMDD(this.fromDate),
      workCheckIn: [4, 6, 7].includes(this.dayType) ? null : this.formatDateTimeForBackend(this.apmosysInTime, convertToYYYYMMDD(this.fromDate)),
      workCheckOut: [4, 6, 7].includes(this.dayType) ? null : this.formatDateTimeForBackend(this.apmosysOutTime, convertToYYYYMMDD(this.isNightShift ? this.toDate : this.fromDate)),
      currentManagerId: this.currentUser.managerId,
      totalWorkingMinutes: this.totalPresence * 60,
      locationSessions: dataSet,
      documentData: this.documentData
    }

    this.createOrUpdateObj.locationSessions.forEach((location: LocationEntry) => {
      location.locationInTime = [4, 6, 7].includes(this.dayType) ? null : this.formatDateTimeForBackend(location.locationInTime, convertToYYYYMMDD(this.fromDate));
      location.locationOutTime = [4, 6, 7].includes(this.dayType) ? null : this.formatDateTimeForBackend(location.locationOutTime, convertToYYYYMMDD(this.isNightShift ? this.toDate : this.fromDate));
      location.projects.forEach((project: ProjectEntry) => {
        // if (![4, 6, 7].includes(this.dayType)) {
        //   project?.activities?.forEach((activity: ActivityNew) => {
        //     activity.durationMinutes = activity.durationMinutes * 60;
        //   });
        // } else {
        //   project.activities = null;
        // }
        if([4,6,7].includes(this.dayType)){
          project.activities = null;
        }
      });
    });

    // Note: generateTotalMinutes is calculated but not used in current implementation
    // Keeping for potential future use or backend validation
    // this.generateTotalMinutes(this.createOrUpdateObj);

    this.timesheetNewService.createTimesheet(this.createOrUpdateObj, this.selectedFile)
      .subscribe({
        next: (response: any) => {
          if (response.serviceStatus === "Success") {
            this.openAlertMod(this.alertTemplate, "Timesheet created successfully.");
            this.resetForm()
            // Reset form or navigate as needed
          } else {
            console.error('Timesheet create error:', response);

                 const backendError =
                   response?.serviceError ||
                   response?.serviceResponse ||
                   '"Failed to create timesheet. Please try again.".';
       
                 this.openAlertMod(this.alertTemplate, backendError);
        }
          
        },
        error: (error) => {
          console.error('Timesheet create error:', error);

          const backendError =
            error?.error?.serviceError ||
            error?.error?.serviceResponse ||
            'An unexpected error occurred while creating the timesheet.';

          this.openAlertMod(this.alertTemplate, backendError);
        }

      });
  }

  applyHighlightAndExpand(): void {
    this.highlightLocationIdSet = new Set(this.highlightLocationList);

    const index = this.timesheetLocations.findIndex(loc =>
      this.highlightLocationIdSet.has(loc.workLocationTypeId)
    );

    if (index !== -1) {
      this.expandedLocationIndex = index;
    }
  }
  isLocationHighlighted(location: any): boolean {
    return this.highlightLocationIdSet.has(location.workLocationTypeId);
  }

  // ============================================
  // METHODS - Update Timesheet Functionality
  // ============================================

  /**
   * Load existing timesheet data for update
   * @param timesheetId - ID of timesheet to load
   */
  loadTimesheetForUpdate(timesheetId: number): void {
    this.isLoadingTimesheet = true;
    this.timesheetNewService.getTimesheetById(timesheetId)
      .pipe(first())
      .subscribe({
        next: (response: any) => {
          this.isLoadingTimesheet = false;
          if (response.serviceStatus === "Success") {
            const timesheetData: EmployeeTimesheetDTO = response.serviceResponse;
            this.populateFormFromTimesheetData(timesheetData);
          } else {
            this.openAlertMod(
              this.alertTemplate,
              response.serviceResponse || 'Failed to load timesheet data. Please try again.'
            );
          }
        },
        error: (error) => {
          this.isLoadingTimesheet = false;
          console.error('Error loading timesheet:', error);
          this.openAlertMod(
            this.alertTemplate,
            'An error occurred while loading timesheet data. Please try again.'
          );
        }
      });
  }

  /**
   * Populate form with existing timesheet data
   * @param timesheetData - Timesheet data from server
   */
  populateFormFromTimesheetData(timesheetData: EmployeeTimesheetDTO): void {
    if (!timesheetData) {
      this.openAlertMod(
        this.alertTemplate,
        'Timesheet data not found. Please refresh and try again.'
      );
      return;
    }

    if (!timesheetData.timesheetId) {
      this.openAlertMod(
        this.alertTemplate,
        'Invalid timesheet data. Missing timesheet ID.'
      );
      return;
    }

    // Store timesheet ID
    this.timesheetId = timesheetData.timesheetId;

    // 1. Basic Fields
    this.dayType = timesheetData.dayTypeId;
    if (timesheetData.date) {
      this.fromDate = this.convertYYYYMMDDToDDMMYYYY(timesheetData.date);
    }
    this.isNightShift = timesheetData.isNightShift || false;

    // Handle night shift toDate
    if (this.isNightShift && timesheetData.date) {
      const fromDate = this.parseYYYYMMDD(timesheetData.date);
      if (fromDate) {
        this.toDate = this.formatDDMMYYYY(this.addDays(fromDate, 1));
      }
    }

    // 2. ApMoSys Times
    if (timesheetData.workCheckIn) {
      this.apmosysInTime = this.extractTimeFromDateTime(timesheetData.workCheckIn);
    }
    if (timesheetData.workCheckOut) {
      this.apmosysOutTime = this.extractTimeFromDateTime(timesheetData.workCheckOut);
    }

    // 3. Calculate Total Presence
    this.totalPresence = timesheetData.totalWorkingMinutes
      ? timesheetData.totalWorkingMinutes / 60
      : 0;

    // 4. Timesheet Applied For
    // Determine from timesheetData.empId vs currentUser.empId
    if (timesheetData.empId === this.currentUser.empId) {
      this.timesheetAppliedFor = 'self';
      this.timesheetFilledForUser.empId = this.currentUser.empId;
      this.getTimesheetMetadata();
    } else {
      this.timesheetAppliedFor = 'team';
      // Load team member and set timesheetFilledForUser
      this.loadTeamMemberForUpdate(timesheetData.empId);
    }

    // 5. Load Projects for Employee (needed for dropdowns) then populate locations
    // Store location sessions temporarily for population after projects load
    const locationSessionsToPopulate = timesheetData.locationSessions;
    
    // Note: this.fromDate is already set from timesheetData.date at line 2106
    // Call getProjectListForDateAndEmpId with empId from timesheet data
    // The method will use this.fromDate which is already populated from timesheetData.date
    this.getProjectListForDateAndEmpId(timesheetData.empId);
    
    // Wait for projects to load, then populate locations
    // Use a small delay to ensure activeProjectList is populated
    setTimeout(() => {
      // 6. Populate Locations (after projects are loaded)
      this.populateLocations(locationSessionsToPopulate);

      // 7. Populate Documents
      if (timesheetData.documentData) {
        this.populateDocuments(timesheetData.documentData);
      }

      // 8. Expand first location for better UX
      if (locationSessionsToPopulate && locationSessionsToPopulate.length > 0) {
        this.expandedLocationIndex = 0;
        if (locationSessionsToPopulate[0].projects && locationSessionsToPopulate[0].projects.length > 0) {
          this.expandedProjectIndexMap[0] = 0;
        }
      }
    }, 800);
  }

  /**
   * Populate locations from server data
   * @param locationSessions - Location sessions from server
   */
  populateLocations(locationSessions: LocationEntry[]): void {
    this.timesheetLocations = [];

    if (!locationSessions || locationSessions.length === 0) {
      this.addLocation(null);
      return;
    }

    locationSessions.forEach((locationData, index) => {
      const location: LocationEntry = {
        locationMappingId: locationData.locationMappingId,
        workLocationType: locationData.workLocationType,
        workLocationTypeId: locationData.workLocationTypeId,
        locationInTime: locationData.locationInTime ? this.extractTimeFromDateTime(locationData.locationInTime) : null,
        locationOutTime: locationData.locationOutTime ? this.extractTimeFromDateTime(locationData.locationOutTime) : null,
        totalWorkingHours: locationData.totalWorkingHours,
        projects: []
      };

      // Populate projects for this location
      if (locationData.projects && locationData.projects.length > 0) {
        location.projects = this.populateProjects(locationData.projects, location);
      } else {
        // Ensure at least one project
        location.projects = [this.createProject(location, null)];
      }

      this.timesheetLocations.push(location);
    });
  }

  /**
   * Populate projects from server data
   * @param projectsData - Projects data from server
   * @param location - Parent location entry
   */
  populateProjects(projectsData: ProjectEntry[], location: LocationEntry): ProjectEntry[] {
    const projects: ProjectEntry[] = [];

    projectsData.forEach((projectData) => {
      const project: ProjectEntry = {
        projectId: projectData.projectId,
        projectName: projectData.projectName,
        clientSideId: projectData.clientSideId,
        hasClientSideId: projectData.hasClientSideId || false,
        hasClientFlag: projectData.hasClientFlag || false,
        shadowEmpId: projectData.shadowEmpId,
        isShadowTimesheet: projectData.isShadowTimesheet || false,
        isShadowForSelf: projectData.isShadowForSelf || false,
        clientId: projectData.clientId,
        clientLocationId: projectData.clientLocationId,
        clientApprovalStatus: projectData.clientApprovalStatus,
        activities: [],
        projectActivities: [],
        clientList: [],
        clientLocationList: [],
        projectList: this.activeProjectList ? this.activeProjectList.map(p => ({
          projectId: p.projectId,
          projectName: p.projectName
        })) : [],
        shadowForList: [],
        poId: projectData.poId,
        poNo: projectData.poNo,
        status: projectData.status || 1,
        locationMappingId: location.locationMappingId,
        projectHoursMinutes: projectData.projectHoursMinutes,
        timesheetId: projectData.timesheetId,
        totalClientWorkingMinutes: projectData.totalClientWorkingMinutes,
        totalWorkingHours: projectData.totalWorkingHours,
        description: projectData.description
      };

      // Populate activities
      if (projectData.activities && projectData.activities.length > 0) {
        project.activities = this.populateActivities(projectData.activities, project);
      } else {
        // Ensure at least one activity for fillable day types
        if (this.isDayTypeFillable()) {
          project.activities = [this.createActivity(null, project.projectId)];
        } else {
          project.activities = [];
        }
      }

      // Populate client and location lists if project is selected
      if (project.projectId) {
        this.populateProjectDropdowns(project);
      }

      projects.push(project);
    });

    return projects;
  }

  /**
   * Populate activities from server data
   * @param activitiesData - Activities data from server
   * @param project - Parent project entry
   */
  populateActivities(activitiesData: ActivityNew[], project: ProjectEntry): ActivityNew[] {
    const activities: ActivityNew[] = [];

    activitiesData.forEach((activityData) => {
      const activity: ActivityNew = {
        activityId: activityData.activityId,
        description: activityData.description,
        durationMinutes: activityData.durationMinutes, // Already in hours format
        projectId: project.projectId,
        teamId: activityData.teamId,
        timesheetId: activityData.timesheetId,
        clientTeamList: [],
        allActivitiesForProject: []
      };

      // ✅ Populate team list from clientDetails if available
      if (project.clientId && project.clientLocationId && project.clientDetails && project.clientDetails.project) {
        activity.clientTeamList = project.clientDetails.project.teams.map(team => ({
          teamId: team.teamId,
          teamName: team.teamName
        }));

        // Load activities for project if team is selected
        if (activity.teamId) {
          this.onProjectTeamSelect(activity.teamId, project);
        }
      }

      activities.push(activity);
    });

    return activities;
  }

  /**
   * Populate documents from server data
   * @param documentData - Document data from server
   */
  populateDocuments(documentData: TimesheetDocumentDataI[]): void {
    if (!documentData || documentData.length === 0) {
      this.documentData = [];
      return;
    }

    this.documentData = documentData.map(doc => ({
      docId: doc.docId,
      projectId: doc.projectId,
      docType: doc.docType,
      docName: doc.docName,
      previewUrl: doc.previewUrl || null, // Base64 or URL from server
      rawObjectUrl: null, // Will be set if new file uploaded
      fileError: null,
      fileType: doc.fileType || null,
      uniqueIdentifier: doc.uniqueIdentifier || doc.docName || null,
      fileSize: doc.fileSize || null,
      bulkApprovedDocId: doc.bulkApprovedDocId || null,
      finalFlag: doc.finalFlag || false
    }));

    // Update uniqueProjectsList for document upload UI
    this.getListToRenderUpload();
  }

  /**
   * Populate project dropdowns (client, location, team lists)
   * Fetches client details if not already loaded
   * @param project - Project entry to populate
   */
  populateProjectDropdowns(project: ProjectEntry): void {
    // ✅ If clientDetails not loaded, fetch it
    if (!project.clientDetails && project.projectId) {
      this.getClientDetailsByProjectIdAndEmpId(project);
    } else if (project.clientDetails) {
      // ✅ Use clientDetails if already loaded
      if (project.clientDetails.clientId) {
        project.clientList = [{
          clientId: project.clientDetails.clientId,
          clientName: project.clientDetails.clientName
        }];
        
        // Populate client location list
        if (project.clientDetails.clientLocations) {
          project.clientLocationList = project.clientDetails.clientLocations.map(loc => ({
            clientLocationId: loc.clientLocationId,
            clientLocation: loc.clientLocation
          }));
        }
        
        // Populate teams if location is selected
        if (project.clientLocationId && project.clientDetails.project) {
          project.projectList = project.clientDetails.project.teams.map(team => ({
            teamId: team.teamId,
            teamName: team.teamName
          }));
        }
      }
    }
  }

  /**
   * Load team member data for update mode
   * @param empId - Employee ID to find in team members
   */
  loadTeamMemberForUpdate(empId: number): void {
    this.getAllTeamMemberList();
    // After team members load, find and set the one matching empId
    setTimeout(() => {
      const teamMember = this.teamMemberList.find(m => m.empId === empId);
      if (teamMember) {
        this.timesheetFilledForUser.empId = teamMember.empId;
        this.timesheetFilledForUser.name = teamMember.name;
        this.selectedTeamMember = teamMember;
        this.getTimesheetMetadata();
        // Load available timesheets for date filtering
        if (this.serverDate) {
          this.getAllAvailableTimesheetByEmpId(this.timesheetFilledForUser);
        }
      }
    }, 300);
  }

  /**
   * Update existing timesheet
   * Similar to createTimesheet but includes timesheetId and handles existing documents
   */
  updateTimesheet(): void {
    // Clear previous highlights
    this.highlightLocationList = [];

    const convertToYYYYMMDD = (dateStr: string): string => {
      if (!dateStr) return '';
      const [day, month, year] = dateStr.split('-');
      return `${year}-${month}-${day}`;
    };

    // Validate timesheetId exists
    if (!this.timesheetId) {
      this.openAlertMod(
        this.alertTemplate,
        'Timesheet ID is missing. Cannot update timesheet.'
      );
      return;
    }

    // Prepare validation context (same as create)
    const validationContext = {
      dayType: this.dayType,
      fromDate: this.fromDate,
      toDate: this.toDate,
      isNightShift: this.isNightShift,
      apmosysInTime: this.apmosysInTime,
      apmosysOutTime: this.apmosysOutTime,
      totalPresence: this.totalPresence,
      timesheetAppliedFor: this.timesheetAppliedFor,
      timesheetFilledForUser: this.timesheetFilledForUser,
      currentUser: this.currentUser,
      timesheetLocations: this.timesheetLocations,
      workLocationList: this.workLocationList,
      documentData: this.documentData,
      uniqueProjectsList: this.uniqueProjectsList,
      empHasClientSideId: this.empHasClientSideId
    };

    // Validate using the validation service
    const validationResult = this.timesheetValidator.validateCreate(validationContext);

    if (!validationResult.isValid) {
      const errorMessage = this.timesheetValidator.formatErrorsForDisplay(validationResult);
      this.openAlertMod(this.alertTemplate, errorMessage);

      if (validationResult.highlightedLocationIds && validationResult.highlightedLocationIds.length > 0) {
        this.highlightLocationList = validationResult.highlightedLocationIds;
        this.applyHighlightAndExpand();
      }
      return;
    }

    // Display warnings if any (non-blocking)
    if (validationResult.warnings && validationResult.warnings.length > 0) {
      const warningMessage = this.timesheetValidator.formatWarningsForDisplay(validationResult);
      setTimeout(() => {
        this.openAlertMod(this.alertTemplate, warningMessage);
      }, 100);
    }

    const dataSet: LocationEntry[] = structuredClone(this.timesheetLocations);

    // Determine correct empId
    const targetEmpId = this.timesheetAppliedFor.toLowerCase() === 'self'
      ? this.currentUser.empId
      : (this.timesheetFilledForUser?.empId || this.currentUser.empId);

    // Prepare update object
    this.createOrUpdateObj = {
      timesheetId: this.timesheetId, // ✅ CRITICAL: Include timesheetId for update
      createdBy: this.currentUser.empId,
      updatedBy: this.currentUser.empId, // ✅ Add updatedBy
      dayTypeId: this.dayType,
      empId: targetEmpId,
      isApmosysProduct: this.currentUser.isApmosysProduct,
      isNightShift: this.isNightShift,
      date: convertToYYYYMMDD(this.fromDate),
      workCheckIn: [4, 6, 7].includes(this.dayType) ? null : this.formatDateTimeForBackend(this.apmosysInTime, convertToYYYYMMDD(this.fromDate)),
      workCheckOut: [4, 6, 7].includes(this.dayType) ? null : this.formatDateTimeForBackend(this.apmosysOutTime, convertToYYYYMMDD(this.isNightShift ? this.toDate : this.fromDate)),
      currentManagerId: this.currentUser.managerId,
      totalWorkingMinutes: this.totalPresence * 60,
      locationSessions: dataSet,
      documentData: this.documentData
    };

    // Format location and project data (same as create)
    this.createOrUpdateObj.locationSessions.forEach((location: LocationEntry) => {
      location.locationInTime = [4, 6, 7].includes(this.dayType) ? null : this.formatDateTimeForBackend(location.locationInTime, convertToYYYYMMDD(this.fromDate));
      location.locationOutTime = [4, 6, 7].includes(this.dayType) ? null : this.formatDateTimeForBackend(location.locationOutTime, convertToYYYYMMDD(this.isNightShift ? this.toDate : this.fromDate));
      location.projects.forEach((project: ProjectEntry) => {
        if ([4, 6, 7].includes(this.dayType)) {
          project.activities = null;
        }
      });
    });

    // Call update API
    this.timesheetNewService.updateTimesheet(this.createOrUpdateObj, this.selectedFile)
      .subscribe({
        next: (response: any) => {
          if (response.serviceStatus === "Success") {
            this.openAlertMod(this.alertTemplate, "Timesheet updated successfully.");
            // Emit event to parent to refresh list
            this.timesheetUpdated.emit(this.timesheetId);
            // Option: Reload updated data
            // this.loadTimesheetForUpdate(this.timesheetId);
          } else {
            console.error('Timesheet update error:', response);
            const backendError =
              response?.serviceError ||
              response?.serviceResponse ||
              'Failed to update timesheet. Please try again.';
            this.openAlertMod(this.alertTemplate, backendError);
          }
        },
        error: (error) => {
          console.error('Timesheet update error:', error);
          const backendError =
            error?.error?.serviceError ||
            error?.error?.serviceResponse ||
            'An unexpected error occurred while updating the timesheet.';
          this.openAlertMod(this.alertTemplate, backendError);
        }
      });
  }

  /**
   * Cancel update and reset form
   */
  onCancelUpdate(): void {
    this.updateCancelled.emit();
    this.resetForm();
  }

  /**
   * Check if document is existing (has docId) or new upload
   */
  isExistingDocument(doc: TimesheetDocumentDataI): boolean {
    return !!doc.docId;
  }

  /**
   * Load and preview existing document from server
   * @param docId - Document ID from server
   */
  previewExistingDocument(docId: number): void {
    this.timesheetNewService.getDocumentById(docId)
      .pipe(first())
      .subscribe({
        next: (response: any) => {
          if (response.serviceStatus === "Success") {
            const docData = response.serviceResponse;
            // docData should contain base64Data and mimeType
            if (docData.base64Data && docData.mimeType) {
              this.showPreview(docData.base64Data, docData.mimeType);
            } else {
              this.openAlertMod(
                this.alertTemplate,
                'Document data format is invalid.'
              );
            }
          } else {
            this.openAlertMod(
              this.alertTemplate,
              response.serviceResponse || 'Failed to load document for preview.'
            );
          }
        },
        error: (error) => {
          console.error('Error loading document:', error);
          this.openAlertMod(
            this.alertTemplate,
            'Failed to load document for preview.'
          );
        }
      });
  }

  // ============================================
  // HELPER METHODS - Date/Time Conversion for Update
  // ============================================

  /**
   * Convert YYYY-MM-DD to DD-MM-YYYY
   */
  convertYYYYMMDDToDDMMYYYY(dateStr: string): string {
    if (!dateStr) return null;
    const [year, month, day] = dateStr.split('-');
    return `${day}-${month}-${year}`;
  }

  /**
   * Parse YYYY-MM-DD date string
   */
  parseYYYYMMDD(dateStr: string): Date | null {
    if (!dateStr) return null;
    const parts = dateStr.split('-');
    if (parts.length !== 3) return null;
    const year = Number(parts[0]);
    const month = Number(parts[1]);
    const day = Number(parts[2]);
    if (!year || !month || !day) return null;
    return new Date(year, month - 1, day);
  }

  /**
   * Extract time from datetime string (YYYY-MM-DD HH:mm:ss or similar formats)
   */
  extractTimeFromDateTime(dateTimeStr: string): string | null {
    if (!dateTimeStr) return null;
    
    // Handle different datetime formats
    // Format 1: "YYYY-MM-DD HH:mm:ss"
    // Format 2: "YYYY-MM-DDTHH:mm:ss"
    // Format 3: ISO string
    
    let timePart: string;
    
    if (dateTimeStr.includes('T')) {
      // ISO format or similar
      const parts = dateTimeStr.split('T');
      timePart = parts[1] || '';
      // Remove timezone if present
      timePart = timePart.split('+')[0].split('-')[0].split('Z')[0];
    } else if (dateTimeStr.includes(' ')) {
      // Space-separated format
      const parts = dateTimeStr.split(' ');
      timePart = parts[1] || '';
    } else {
      return null;
    }
    
    // Extract HH:mm from HH:mm:ss
    if (timePart) {
      const timeParts = timePart.split(':');
      if (timeParts.length >= 2) {
        return `${timeParts[0]}:${timeParts[1]}`;
      }
    }
    
    return null;
  }

  // NOTE: All validation logic has been moved to TimesheetValidationService
  // The old validationService, validateDocumentUploads, and failValidation methods
  // have been removed. Use timesheetValidator.validateCreate() instead.

  // convertMMDDYYYToDDMMYYY

  getFormattedDateRange(): string {
    if (!this.fromDate) return '';

    const from = this.parseDDMMYYYY(this.fromDate);
    if (!from) return '';

    if (!this.toDate) {
      return moment(from).format('MMM DD, YYYY');
    }

    const to = this.parseDDMMYYYY(this.toDate);
    if (!to) return moment(from).format('MMM DD, YYYY');

    // Same month & year → Jan 01-02, 2026
    if (
      moment(from).isSame(to, 'month') &&
      moment(from).isSame(to, 'year')
    ) {
      return `${moment(from).format('MMM DD')}-${moment(to).format('DD, YYYY')}`;
    }

    // Different month/year → Jan 30 – Feb 02, 2026
    return `${moment(from).format('MMM DD, YYYY')} - ${moment(to).format('MMM DD, YYYY')}`;
  }


  getDayRange(): string {
    if (!this.fromDate) return '';

    const from = this.parseDDMMYYYY(this.fromDate);
    if (!from) return '';

    if (!this.toDate) {
      return moment(from).format('dddd');
    }

    const to = this.parseDDMMYYYY(this.toDate);
    if (!to) return moment(from).format('dddd');

    // Same day
    if (moment(from).isSame(to, 'day')) {
      return moment(from).format('dddd');
    }

    return `${moment(from).format('dddd')}-${moment(to).format('dddd')}`;
  }


  /**
   * Format total presence as "HH:MM hrs"
   */
  getFormattedPresence(): string {
    // Check if totalPresence is valid and greater than 0
    if (!this.totalPresence || isNaN(this.totalPresence) || this.totalPresence <= 0) {
      return '00:00 hrs';
    }

    const hours = Math.floor(this.totalPresence);
    const minutes = Math.round((this.totalPresence - hours) * 60);

    // Handle case where minutes round up to 60
    const finalHours = minutes >= 60 ? hours + 1 : hours;
    const finalMinutes = minutes >= 60 ? 0 : minutes;

    const formattedHours = String(finalHours).padStart(2, '0');
    const formattedMinutes = String(finalMinutes).padStart(2, '0');
    return `${formattedHours}:${formattedMinutes} hrs`;
  }

  /**
   * Calculate total working hours for a specific location
   * Handles both normal shift and night shift scenarios
   */
  calculateTotalWorkingHoursForLocation(
    location: LocationEntry,
    fromDate: string,          // dd-MM-yyyy (required)
    toDate?: string            // dd-MM-yyyy (required ONLY for night shift)
  ): number {

    if (
      !location?.locationInTime ||
      !location?.locationOutTime ||
      !fromDate ||
      location.locationInTime.trim() === '' ||
      location.locationOutTime.trim() === ''
    ) {
      return null;
    }

    //  Night shift but no toDate
    if (this.isNightShift && !toDate) {
      console.warn('toDate is required for night shift');
      return null;
    }

    try {
      // ---------- Helpers ----------
      const parseDate = (dateStr: string) => {
        const [day, month, year] = dateStr.split('-').map(Number);
        return { day, month, year };
      };

      const parseTime = (timeStr: string) => {
        const [time, meridian] = timeStr.trim().split(' ');
        const [hh, mm] = time.split(':').map(Number);

        let hours = hh;
        if (meridian === 'PM' && hh !== 12) hours += 12;
        if (meridian === 'AM' && hh === 12) hours = 0;

        return { hours, minutes: mm };
      };

      // ---------- Dates ----------
      const startDate = parseDate(fromDate);
      const endDate = this.isNightShift && toDate
        ? parseDate(toDate)
        : parseDate(fromDate);

      // ---------- Times ----------
      const inTime = parseTime(location.locationInTime);
      const outTime = parseTime(location.locationOutTime);

      // ---------- DateTime Objects ----------
      const inDateTime = new Date(
        startDate.year,
        startDate.month - 1,
        startDate.day,
        inTime.hours,
        inTime.minutes,
        0,
        0
      );

      const outDateTime = new Date(
        endDate.year,
        endDate.month - 1,
        endDate.day,
        outTime.hours,
        outTime.minutes,
        0,
        0
      );

      // ---------- VALIDATIONS ----------

      const selectedLocationData = this.workLocationList.find(
        l => l.workLocationTypeId === location.workLocationTypeId);
      // Invalid interval - out time before in time
      if (outDateTime.getTime() < inDateTime.getTime()) {
        this.openAlertMod(
          this.alertTemplate,
          `The Log-Out time for location ${selectedLocationData?.code || ''} cannot be before Log-In time`
        );
        return 0;
      }


      // ---------- CALCULATION ----------
      const diffMs = outDateTime.getTime() - inDateTime.getTime();
      const totalHours = diffMs / (1000 * 60 * 60);

      return Math.round(totalHours * 100) / 100;

    } catch (error) {
      console.error('Error calculating working hours', error);
      return 0;
    }
  }


  onHoursChange(): void {

    let totalLocationHours = 0;

    for (const location of this.timesheetLocations) {

      // ---------- Calculate location hours ----------
      location.totalWorkingHours =
        this.calculateTotalWorkingHoursForLocation(
          location,
          this.fromDate!,
          this.toDate
        ) || 0;

      // ---------- Calculate project hours for THIS location ----------
      let projectHoursForLocation = 0;

      for (const proj of location.projects) {
        let projHours = 0;

        if (proj.activities) {
          for (const activity of proj.activities) {
            projHours += Number(activity.durationMinutes) || 0;
          }
        }

        proj.totalWorkingHours = projHours;
        projectHoursForLocation += projHours;
      }

      // Validation 1: Project hours cannot exceed location hours
      if (projectHoursForLocation > (location.totalWorkingHours || 0)) {
        this.openAlertMod(
          this.alertTemplate,
          'Total working hours for location cannot be less than sum of project working hours.'
        );

        this.resetProjectHoursForLocation(location);
        return;
      }

      totalLocationHours += location.totalWorkingHours || 0;

      // Validation 2: Total location hours cannot exceed total presence
      if (totalLocationHours > this.totalPresence) {
        this.openAlertMod(
          this.alertTemplate,
          'Total working hours for all locations cannot be more than total presence hours.'
        );

        this.resetLocation(location);
        return;
      }
    }
  }


  /**
   * Reset project hours for a location
   */
  resetProjectHoursForLocation(location: LocationEntry): void {
    location.projects.forEach(proj => {
      proj.totalWorkingHours = 0;
      proj.activities.forEach(activity => {
        activity.durationMinutes = null;
      });
    });
  }

  /**
   * Reset location hours and related project hours
   */
  resetLocation(location: LocationEntry): void {
    location.totalWorkingHours = null;
    location.locationInTime = null;
    location.locationOutTime = null;

    location.projects.forEach(proj => {
      proj.totalWorkingHours = 0;
      proj.activities.forEach(activity => {
        activity.durationMinutes = null;
      });
    });
  }


  /**
   * Handle shadow timesheet toggle change
   * Validates shadow timesheet is only available for self timesheet
   */
  onShadowTimesheetChange(project: ProjectEntry): void {
    if (this.timesheetAppliedFor == 'self') {
      this.getEmployeeListByProjectId(project, this.currentUser.empId);
      // ✅ Update document list when shadow status changes (affects isShadowForSelf)
      this.getListToRenderUpload();
    } else {
      this.openAlertMod(this.alertTemplate, "Shadow timesheet is only available when timesheet is filled for self.");
      project.isShadowTimesheet = false;
      // ✅ Update document list when shadow is disabled
      this.getListToRenderUpload();
      return;
    }
  }
  /**
   * Generate list of documents to render for upload
   * Based on projects with client side IDs and approval status
   * Conditions: project.clientSideId exists, !project.isShadowForSelf, isDayTypeFillable()
   * 
   * ✅ IMPORTANT: empHasClientSideId is reset at start and set to true only if ANY project qualifies
   * This ensures the class-level variable correctly reflects if ANY project needs document upload
   */
  getListToRenderUpload(): void {
    this.documentData = [];
    const dataList: Map<number, ProjectEntry> = new Map<number, ProjectEntry>();
    
    // ✅ Reset at start - will be set to true only if we find at least one qualifying project
    this.empHasClientSideId = false;
    
    this.timesheetLocations.forEach((location) => {
      location.projects.forEach((project) => {
        // ✅ Check all three conditions: clientSideId exists, not shadow for self, and day type is fillable
        if (project.clientSideId && 
            !project.isShadowForSelf && 
            this.isDayTypeFillable()) {
          
          // ✅ Set to true if ANY project qualifies (not just the first one)
          this.empHasClientSideId = true;
          dataList.set(project.projectId, project);
          
          // Create document entries based on approval status
          if (project.clientApprovalStatus == 2) {
            // Approved: Show both Filled and Approved documents
            this.documentData.push({
              projectId: project.projectId,
              docType: 'Filled',
              previewUrl: null,
              rawObjectUrl: null,
              fileError: null,
              fileType: null,
              uniqueIdentifier: null,
              fileSize: null,
              bulkApprovedDocId: null,
              docId: null,
              docName: null,
              finalFlag: false
            });
            this.documentData.push({
              projectId: project.projectId,
              docType: 'Approved',
              previewUrl: null,
              rawObjectUrl: null,
              fileError: null,
              fileType: null,
              uniqueIdentifier: null,
              fileSize: null,
              bulkApprovedDocId: null,
              docId: null,
              docName: null,
              finalFlag: false
            });
          }
          else if (project.clientApprovalStatus == 1) {
            // Pending: Show only Filled document
            this.documentData.push({
              projectId: project.projectId,
              docType: 'Filled',
              previewUrl: null,
              rawObjectUrl: null,
              fileError: null,
              fileType: null,
              uniqueIdentifier: null,
              fileSize: null,
              bulkApprovedDocId: null,
              docId: null,
              docName: null,
              finalFlag: false
            });
          }
        }
      });
    });
    
    this.uniqueProjectsList = Array.from(dataList.values());
  }

  /**
   * Get employee list for shadow timesheet by project ID
   */
  getEmployeeListByProjectId(project: ProjectEntry, empId: number): void {
    this.timesheetService.getEmployeeListByProjectId(project.projectId, empId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        const shadowEmpList = response.serviceResponse;
        this.timesheetLocations.forEach(location => {
          location.projects.forEach(proj => {
            if (proj === project) {
              if (!proj.isShadowTimesheet) {
                proj.shadowEmpId = null;
                proj.shadowForList = [];
              }
              else if (proj.isShadowTimesheet) {
                proj.shadowEmpId = null;
                proj.shadowForList = shadowEmpList;
              }
            }
          });
        });
      }
    });
  }
  /**
   * Get all work locations from master data
   */
  getAllWorkLocationFromLocationMaster(): void {
    this.timesheetNewService.getAllWorkLocation().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.workLocationList = response.serviceResponse;
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getAllDSRApprovalStatusFromMaster() {
    this.timesheetNewService.getAllDSRApprovalStatus().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.clientApprovalStatusList = response.serviceResponse;
      }
    });
  }

  /**
   * Load autofill data for a selected date
   */
  /**
   * Load autofill data for a selected date
   */
  loadAutofillData(selectedDate1: string): void {
    this.isUpdation = false;
    this.timesheetAppliedFor = 'self';
    this.dayType = 1;
    this.fromDate = selectedDate1;
  }
  /**
   * Open alert modal with reset option
   */
  openAlertWithResetMod(template: TemplateRef<any>, message: any): void {
    this.alertWithResetModRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  /**
   * Cancel request and reset form
   */
  cancelRequest2(): void {
    this.alertWithResetModRef?.close();
    this.resetTimesheetForm();
  }
  resetTimesheetForm() {
    this.fromDate = null;
    this.toDate = null;
  }

  /**
   * Get project list for a specific date and employee ID
   */
  getProjectListForDateAndEmpId(empId?: number): void {
    // Determine empId (for self or team member)
    const targetEmpId = empId || 
      (this.timesheetAppliedFor.toLowerCase() === 'self' 
        ? this.currentUser.empId 
        : this.timesheetFilledForUser.empId);
    
    if (!targetEmpId) {
      console.error('Employee ID not available');
      return;
    }
    
    // Use fromDate (must be set before calling this method)
    if (!this.fromDate) {
      console.error('Date (fromDate) not available for fetching projects');
      this.disableAdd = true;
      this.openAlertMod(this.alertTemplate, 'Date is required to load projects. Please select a date first.');
      return;
    }
    
    // Convert date format: DD-MM-YYYY to YYYY-MM-DDTHH:mm:ss (LocalDateTime)
    const dateParsed = this.parseDDMMYYYY(this.fromDate);
    if (!dateParsed) {
      console.error('Invalid date format:', this.fromDate);
      return;
    }
    
    const localDateTime = `${dateParsed.getFullYear()}-${String(dateParsed.getMonth() + 1).padStart(2, '0')}-${String(dateParsed.getDate()).padStart(2, '0')}T00:00:00`;

    const payload = {
      empId: targetEmpId,
      date: localDateTime
    };

    this.timesheetService.getProjectListForDateAndEmpId(payload).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        // Populate activeProjectList
        // Note: Response only contains projectId and projectName, not client/location info
        this.activeProjectList = response.serviceResponse || [];
        
        if (this.activeProjectList.length === 0) {
          this.disableAdd = true;
          if (this.timesheetAppliedFor.toLocaleLowerCase() == 'self') {
            this.openAlertMod(this.alertTemplate, 'No Projects assigned for the selected date. Please contact RMG.');
          } else if (this.timesheetAppliedFor.toLocaleLowerCase() == 'team') {
            let teamMember = this.teamMemberList.find(employee => employee.empId === this.timesheetFilledForUser.empId);
            this.openAlertMod(this.alertTemplate, 'No Projects assigned to ' + teamMember?.empName + ' for the selected date.');
          }
        } else {
          this.disableAdd = false;
        }
      } else {
        console.error('Failed to fetch projects:', response.serviceResponse);
        this.disableAdd = true;
        this.openAlertMod(this.alertTemplate, 'Failed to load projects: ' + response.serviceResponse);
      }
    }, error => {
      console.error('Error fetching projects:', error);
      this.disableAdd = true;
      this.openAlertMod(this.alertTemplate, 'Error loading projects. Please try again.');
    });
  }

  /**
   * Generate total minutes from all activities in timesheet
   * Handles null/undefined activities safely for non-fillable day types
   */
  generateTotalMinutes(timesheetDTO: EmployeeTimesheetDTO): number {
    let totalMinutes = 0;
    timesheetDTO.locationSessions.forEach(location => {
      location.projects.forEach(project => {
        // Handle null activities for non-fillable day types
        if (project.activities && Array.isArray(project.activities)) {
          project.activities.forEach(activity => {
            if (activity?.durationMinutes) {
              totalMinutes += activity.durationMinutes * 60;
            }
          });
        }
      });
    });
    return totalMinutes;
  }

}