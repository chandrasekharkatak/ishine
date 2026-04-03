import { Component, Input, OnInit, OnChanges, OnDestroy, Output, EventEmitter, TemplateRef, ViewChild, SimpleChanges, ChangeDetectorRef } from '@angular/core';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import * as moment from 'moment';
import { Subject, first, firstValueFrom, forkJoin, of, takeUntil } from 'rxjs';
import { catchError, finalize, map } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Employee } from 'src/app/models/employee';
import { Timesheet } from 'src/app/models/timesheet';
import { User } from 'src/app/models/user';
import { Holiday } from 'src/app/models/holiday';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { TeamViewService } from 'src/app/services/team-view.service';
import { TimesheetService } from 'src/app/services/timesheet.service';
import { HolidayService } from 'src/app/services/holiday.service';
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
import { DatePipe } from '@angular/common';
import { EmployeeService } from 'src/app/services/employee.service';
import { Router } from '@angular/router';
import { ExcelDownloadService } from 'src/app/services/excel-download-service';
// import { map } from 'highcharts';

@Component({
  standalone: false,
  selector: 'app-timesheet-form',
  templateUrl: './timesheet-form.component.html',
  styleUrl: './timesheet-form.component.css'
})
export class TimesheetFormComponent implements OnInit, OnChanges, OnDestroy {
  
  // ✅ CRITICAL FIX: Subject for unsubscribing all subscriptions
  private destroy$ = new Subject<void>();
  
  // ✅ MODERATE FIX: Constants for magic numbers
  private static readonly NON_FILLABLE_DAY_TYPES = [4, 6, 7]; // Public Holiday, Client Holiday, Week Off
  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;
  @ViewChild("alert_message_for_holiday_create")
  alertMessageForHolidayCreateTemplate: TemplateRef<any>;
  @ViewChild("night_shift_template")
  night_shift_template: TemplateRef<any>;
  @ViewChild("previewTemplate")
  previewModal: TemplateRef<any>;
  @ViewChild("clientSideIdUpdateOrAddModal")
  clientSideIdUpdateOrAddModal: TemplateRef<any>;
  @ViewChild("removeLocationConfirmModal")
  removeLocationConfirmModal: TemplateRef<any>;
  clientSideIdUpdateOrAddModalRef: NgbModalRef;
  alertWithResetModRef: NgbModalRef;
  /** Dedicated ref for the simple alert modal (e.g. "Failed to load projects") so OK closes it reliably */
  alertModalRef: NgbModalRef;
  modalRef: NgbModalRef;
  removeLocationConfirmModalRef: NgbModalRef;
  alertMessageForHolidayCreateModalRef: NgbModalRef;
  pendingLocationIndexToRemove: number | null = null;
  pendingLocationToRemove: LocationEntry | null = null;
  @Input() isCreation: boolean = false;
  @Input() isUpdation: boolean = false;
  @Input() selectedDate: Date | null = null;
  @Input() autoFillEmpId: number | null = null; // Employee ID for whom timesheet should be auto-filled (used when manager creates timesheet on behalf of team member)
  @Input() timesheetId: number | null = null; // ID of timesheet to update
  // @Input() isAutoFilled: boolean = false;
  // Output events for parent component communication
  @Output() timesheetCreated = new EventEmitter<void>();
  @Output() timesheetUpdated = new EventEmitter<number>();
  @Output() updateCancelled = new EventEmitter<void>();
  
  // Loading state for update mode
  isLoadingTimesheet: boolean = false;
  /** Internal flag: when true, populateFormFromTimesheetData is being used for autofill (create template), not hard update */
  private isAutofillMode = false;
  // timesheetObj: Timesheet = new Timesheet();
  selectedTeamMember: any;
  timesheetFilledForUser: User = new User();
  dayType: number;
  timesheetAppliedFor: string ;
  teamMemberList: any[] = [];
  currentUser: User = new User();
  alertMessage: string;
  fromDate: any = null;
  toDate: any = null;
  disableAdd: boolean = false;
  availableTimesheets: any[] = [];
  serverDate: any; // Server's current date for date range calculation
  minDateForPicker: any; // Minimum selectable date (dd-MM-yyyy format)
  maxDateForPicker: any; // Maximum selectable date (dd-MM-yyyy format)
  disabledDatesForPicker: string[] = []; // Dates to disable (dd-MM-yyyy format)
  /** Base disabled dates independent of day type (already-filled timesheets, etc.) */
  private disabledDatesBase: string[] = [];
  /** 
   * Map of dates (dd-MM-YYYY) to their existing timesheet's dayTypeId.
   * Used to identify cron-filled timesheets (holiday/week-off) which should remain selectable.
   */
  private existingTimesheetDayTypes: Map<string, number> = new Map();
  /** Day type IDs for cron-filled timesheets (holiday, week-off) that can be overwritten */
  private readonly CRON_FILLED_DAY_TYPES: number[] = [2,4, 6]; // 4=Public Holiday, 6=Week Off
  /** Allowed dates for Non-working day type (holidays + week-offs within min/max window) */
  private nonWorkingAllowedDates: string[] = [];
  /** Cache the range for which nonWorkingAllowedDates was last loaded */
  private nonWorkingRangeCache: { min: string | null; max: string | null } | null = null;
  /** Track current holiday API request to prevent race conditions */
  private holidayLoadRequestId: number = 0;
  /** Last selected day type ID (used to revert invalid changes) */
  private lastDayTypeId: number | null = null;
  allDayTypes: any[] = [];
  empClientSideObj: EmployeeClientSideIdMapping = new EmployeeClientSideIdMapping();
  apmosysInTime: any = null;
  apmosysOutTime: any = null;
  totalPresence: number = 0;
  activeProjectList: any[] = []; // Unique projects for dropdown
  // Multi-location support (Location -> Project -> Activity)
  uniqueProjectsList: ProjectEntry[] = [];
  timesheetLocations: LocationEntry[] = [];
  expandedLocationIndex: number | null = null;
  expandedProjectIndexMap: { [locationIndex: number]: number | null } = {}; // Track expanded project per location
  empHasClientSideId: boolean = false;
  projectActivityHoursError: { [projectIndex: number]: string } = {}; // Store validation errors per project
  /** When true, only the first client-details error is shown (avoids N popups when loading edit for multiple projects) */
  private clientDetailsErrorShownThisPopulate = false;

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
  clientIdEntryBulletPoints: string[] = ["Mandatory field for all resources while filling the timesheet.",
    "Enter the client-side ID if already available.",
    "If the client-side ID is not yet assigned, enter “NA (ApMoSys Employee ID)”.",
    "Once the client-side ID is received, update the ID while filling subsequent timesheets."]
  appelectMember: any;
  dayTypeToBeExcluded = ["Leave","Holiday"];
  holidayDescription: any;
  noProjectEmployee: any = false;
  employeeObjForDateFetching: User = new User();  
  constructor(private teamViewService: TeamViewService,
    private timesheetService: TimesheetService,
    private timesheetNewService: TimesheetNewService,
    private timesheetValidator: TimesheetValidationService,
    private configService:TimesheetConfigService,
    private modalService: NgbModal,
    private authenticationService: AuthenticationService,
    private holidayService: HolidayService,
    private sanitizer: DomSanitizer,
    private cdr: ChangeDetectorRef,
    private datePipe: DatePipe,
    private router: Router,
    private employeeService: EmployeeService,
    private excelDownloadService: ExcelDownloadService) { 
      // ✅ CRITICAL FIX: Properly unsubscribe on destroy
      this.authenticationService.currentUser
        .pipe(takeUntil(this.destroy$))
        .subscribe(x => this.currentUser = x); 
    }

  ngOnInit(): void {
    // Initialize form with default values
    this.resetForm()
    this.timesheetAppliedFor = 'self';
    this.getAllWorkLocationFromLocationMaster();
    this.getAllDayTypes();
    this.getAllDSRApprovalStatusFromMaster();
    console.log(this.selectedDate,"[ngOnInit] Initial selectedDate:", this.selectedDate);
    // if(this.selectedDate){
    //   this.loadServerDateThenInitCreate();
    // }
    console.log('[ngOnInit] Form initialized with:', {
      isUpdation: this.isUpdation,
      timesheetId: this.timesheetId,
      selectedDate: this.selectedDate
    });

    if (this.isUpdation && this.timesheetId) {
      console.log('[ngOnInit] Update mode detected, loading timesheet...');
      this.loadServerDate();
      this.loadTimesheetForUpdate(this.timesheetId);
    } else {
      // Create mode: load server date first, then run init (resetForm + getTimesheetMetadata / team list)
      // so that getTimesheetMetadata() can call getAllAvailableTimesheetByEmpId() with serverDate set
      console.log('[ngOnInit] Create mode, initializing...',this.selectedDate);
      this.loadServerDateThenInitCreate();
    }
  }

  /**
   * Handle input changes (especially when parent sets timesheetId/isUpdation after ngOnInit)
   */
  ngOnChanges(changes: SimpleChanges): void {
   console.log('[ngOnChange] Create mode, initializing...',this.selectedDate);

    // Switch from Update to Create (user clicked Create Timesheet tab after editing): reset form and init create
    const isCreationChange = changes['isCreation'];
    const isUpdationChange = changes['isUpdation'];
    const timesheetIdChange = changes['timesheetId'];
    const switchedToCreate =
      (this.isCreation && !this.isUpdation && (this.timesheetId == null || this.timesheetId === undefined)) &&
      (
        (isUpdationChange && isUpdationChange.previousValue === true && isUpdationChange.currentValue === false) ||
        (timesheetIdChange && timesheetIdChange.previousValue != null && (timesheetIdChange.currentValue == null || timesheetIdChange.currentValue === undefined)) ||
        (isCreationChange && isCreationChange.previousValue === false && isCreationChange.currentValue === true)
      );
    if (switchedToCreate) {
      this.resetForm();
      this.timesheetAppliedFor = 'self';
      if (!this.serverDate) {
        this.loadServerDateThenInitCreate();
      } else if (this.autoFillEmpId) {
        this.applyAutoFillFrom360();
      } else {
        this.onTimesheetAppliedForChange();
      }
      return;
    }

    // When timesheetId or isUpdation changes after initial load, trigger update load
    if (timesheetIdChange || isUpdationChange) {
      const timesheetIdChanged = timesheetIdChange &&
        timesheetIdChange.currentValue !== timesheetIdChange.previousValue &&
        timesheetIdChange.currentValue != null;
      const isUpdationChanged = isUpdationChange &&
        isUpdationChange.currentValue !== isUpdationChange.previousValue &&
        isUpdationChange.currentValue === true;

      // Only load if we're in update mode and have a timesheetId, and haven't already loaded
      if ((timesheetIdChanged || isUpdationChanged) && this.isUpdation && this.timesheetId && !this.isLoadingTimesheet) {
        console.log('[ngOnChanges] Detected timesheetId/isUpdation change, loading timesheet for update:', {
          timesheetId: this.timesheetId,
          isUpdation: this.isUpdation,
          timesheetIdChanged,
          isUpdationChanged,
          previousTimesheetId: timesheetIdChange?.previousValue,
          previousIsUpdation: isUpdationChange?.previousValue
        });
        // if (!this.serverDate) {
        //   this.loadServerDate();
        // }
        // this.loadTimesheetForUpdate(this.timesheetId);
      }
    }

    // Create-from-home / create-from-360: parent set selectedDate or autoFillEmpId after form created
    const selectedDateChange = changes['selectedDate'];
    const autoFillEmpIdChange = changes['autoFillEmpId'];
    const dateOrEmpChanged =
      (selectedDateChange && !selectedDateChange.firstChange && selectedDateChange.currentValue !== selectedDateChange.previousValue) ||
      (autoFillEmpIdChange && !autoFillEmpIdChange.firstChange && autoFillEmpIdChange.currentValue !== autoFillEmpIdChange.previousValue);
    if (
      dateOrEmpChanged &&
      !this.isUpdation &&
      this.isCreation &&
      this.timesheetAppliedFor?.toLowerCase() === 'self'
    ) {
      if (!this.serverDate) {
        this.loadServerDateThenInitCreate();
      } else if (this.autoFillEmpId) {
        this.applyAutoFillFrom360();
      } else {
        this.onTimesheetAppliedForChange();
      }
    }
  }

  /**
   * Create mode only: load server date, then run onTimesheetAppliedForChange() so that
   * getTimesheetMetadata() → getAllAvailableTimesheetByEmpId() runs with serverDate already set,
   * avoiding race where date constraints were applied only when loadServerDate() completed later.
   */
  private loadServerDateThenInitCreate(): void {
    this.timesheetService.getServerDate()
      .pipe(first(), takeUntil(this.destroy$))
      .subscribe((response: any) => {
        this.serverDate = response;
        if (this.autoFillEmpId) {
          this.applyAutoFillFrom360();
        } else {
        this.onTimesheetAppliedForChange();
        }
      });
  }

  /**
   * When opened from Employee 360 with date + empId: switch to team mode, pre-select that employee,
   * then run autofill for the selected date (same as "create from home" but for team member).
   */
  private applyAutoFillFrom360(): void {
    if (!this.autoFillEmpId || this.isUpdation) {
      this.onTimesheetAppliedForChange();
      return;
    }
    this.timesheetAppliedFor = 'team';
    this.getAllTeamMemberList()
      .then(() => {
        const teamMember = this.teamMemberList?.find(
          m => m.empId === this.autoFillEmpId || Number(m.empId) === Number(this.autoFillEmpId)
        );
        if (teamMember) {
          this.timesheetFilledForUser.empId = teamMember.empId;
          this.timesheetFilledForUser.name = teamMember.name;
          this.selectedTeamMember = teamMember;
          this.appelectMember = teamMember;
          this.getTimesheetMetadata();
          if (this.serverDate) {
            this.getAllAvailableTimesheetByEmpId(this.timesheetFilledForUser);
          }
          if (this.selectedDate) {
            this.loadAutofillData(this.formatDateDDMMYYYY(this.selectedDate));
          }
          this.cdr.detectChanges();
        } else {
          console.warn('[applyAutoFillFrom360] Team member not found for empId:', this.autoFillEmpId);
          this.onTimesheetAppliedForChange();
        }
      })
      .catch(() => this.onTimesheetAppliedForChange());
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
   * Location options for dropdown: NA only for non-fillable day type, all except NA for fillable.
   */
  get locationOptionsForDropdown(): any[] {
    if (!this.workLocationList?.length) return [];
    const naId = this.configService.getDefaultWorkLocationTypeId();
    if (this.isDayTypeFillable()) {
      return this.workLocationList.filter((loc: any) => loc.workLocationTypeId !== naId);
    }
    return this.workLocationList.filter((loc: any) => loc.workLocationTypeId === naId);
  }

  /**
   * Format date to DD-MM-YYYY format
   */
  formatDateDDMMYYYY(date: Date): string {
    return `${String(date.getDate()).padStart(2, '0')}-${String(date.getMonth() + 1).padStart(2, '0')}-${date.getFullYear()}`;
  }

  // ✅ MODERATE FIX: Centralized error handling
  /**
   * Handles errors consistently across the component
   * @param error - Error object or message
   * @param context - Context where error occurred (for logging)
   * @param showToUser - Whether to show error to user via alert modal
   * @param userMessage - Optional custom message to show to user
   */
  private handleError(error: any, context: string, showToUser: boolean = false, userMessage?: string): void {
    const errorMessage = error?.message || error?.toString() || 'An unexpected error occurred';
    console.error(`[${context}]`, error);
    
    if (showToUser) {
      const message = userMessage || `Error: ${errorMessage}. Please try again.`;
      this.openAlertMod(this.alertTemplate, message);
    }
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
   * Apply ApMoSys timing to the first location.
   * Only updates location in/out times; does not replace locations or projects.
   * Call only when timesheetLocations.length === 1 (checkbox is hidden when multiple locations).
   */
  applyApmosysTiming(workLocatioId: number): void {
    if (this.timesheetLocations.length !== 1) {
      return;
    }
    const loc = this.timesheetLocations[0];
    if (this.useApmosysTiming) {
      this.disableAdd = true;
      loc.locationInTime = this.apmosysInTime;
      loc.locationOutTime = this.apmosysOutTime;
      this.onHoursChange();
    } else {
      this.disableAdd = false;
      // Leave location in/out times as-is; user can edit manually. No need to clear.
      this.onHoursChange();
    }
  }

  addLocation(timesheetId): void {
  // Prevent adding locations when timesheet is not fillable (e.g. view-only mode)
    if (!this.isDayTypeFillable()) {
      console.warn('Attempt to add location when timesheet is not fillable.');
      return;
    }
    // ✅ BUSINESS RULE: Maximum 5 locations allowed
    const MAX_LOCATIONS = 5;
    if (this.timesheetLocations.length >= MAX_LOCATIONS) {
      this.handleError(
        new Error(`Maximum ${MAX_LOCATIONS} locations allowed`),
        'addLocation',
        true,
        `You can add a maximum of ${MAX_LOCATIONS} locations. Please remove an existing location before adding a new one.`
      );
      return;
    }
    
    this.timesheetLocations.push(this.createLocation(timesheetId));
    // Expand the newly added location
    this.expandedLocationIndex = this.timesheetLocations.length - 1;
    // Initialize expanded project index for this location
    this.expandedProjectIndexMap[this.timesheetLocations.length - 1] = 0;
  }

  /**
   * Remove a location from the timesheet
   * Shows confirmation dialog before removal
   * Ensures at least one location remains
   * Cleans up associated document data and file references
   * Recalculates total hours after removal
   */
  removeLocation(index: number): void {
    // Prevent removing locations when timesheet is not fillable
    if (!this.isDayTypeFillable()) {
      console.warn('Attempt to remove location when timesheet is not fillable.');
      return;
    }
    if (this.timesheetLocations.length <= 1) {
      this.openAlertMod(this.alertTemplate, 'At least one location is required.');
      return;
    }
    
    // ✅ Show confirmation dialog before removal
    this.pendingLocationIndexToRemove = index;
    this.pendingLocationToRemove = this.timesheetLocations[index] || null;
    this.removeLocationConfirmModalRef = this.modalService.open(
      this.removeLocationConfirmModal,
      { 
        modalDialogClass: 'remove-location-confirm-modal', 
        centered: false,
        backdrop: 'static',
        keyboard: true,
        windowClass: 'remove-location-modal-window'
      }
    );
  }

  /**
   * Confirm location removal after user confirms in dialog
   */
  confirmRemoveLocation(): void {
    if (this.pendingLocationIndexToRemove === null && !this.pendingLocationToRemove) {
      return;
    }
    
    // Resolve the actual index at the time of confirmation to avoid stale index issues
    let index = -1;
    if (this.pendingLocationToRemove) {
      index = this.timesheetLocations.indexOf(this.pendingLocationToRemove);
    }
    // Fallback to stored numeric index if reference is not found (should be rare)
    if (index === -1 && this.pendingLocationIndexToRemove !== null) {
      index = this.pendingLocationIndexToRemove;
    }

    // Reset pending state
    this.pendingLocationIndexToRemove = null;
    this.pendingLocationToRemove = null;

    if (index < 0 || index >= this.timesheetLocations.length) {
      console.warn('Location to remove no longer exists or index is out of range.');
      if (this.removeLocationConfirmModalRef) {
        this.removeLocationConfirmModalRef.close();
      }
      return;
    }
    
    // Close confirmation modal
    if (this.removeLocationConfirmModalRef) {
      this.removeLocationConfirmModalRef.close();
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
    
    // ✅ Recalculate total hours after removal
    this.onHoursChange();
    
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
   * Cancel location removal
   */
  cancelRemoveLocation(): void {
    this.pendingLocationIndexToRemove = null;
    this.pendingLocationToRemove = null;
    if (this.removeLocationConfirmModalRef) {
      this.removeLocationConfirmModalRef.close();
    }
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
     this.fileTracker.forEach((value,key)=>{
      const doc = docsToRemove.find(d => d.uniqueIdentifier === value);
      if(doc){
        this.fileTracker.delete(key);
      }
    })

    console.log("Selected File",this.selectedFile)
    console.log("File tracker",this.fileTracker);
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
   * Checks for duplicate projects in the same location
   * Fetches client details automatically if only one project
   */
  addProject(location: LocationEntry, timesheetId: any): void {

    // ✅ MODERATE FIX: Validate date is selected (required for project fetching)
    if (!this.fromDate) {
      this.handleError(
        new Error('Date must be selected before adding project'),
        'addProject',
        true,
        'Please select a date first before adding a project.'
      );
      return;
    }
    
    if (!this.activeProjectList || this.activeProjectList.length === 0) {
      this.handleError(
        new Error('No projects available'),
        'addProject',
        true,
        'No projects available. Please ensure projects are loaded before adding.'
      );
      return;
    }

    // Max projects per location = number of available projects (e.g. 2 available → max 2 project rows)
    if (location.projects.length >= this.activeProjectList.length) {
      this.openAlertMod(
        this.alertTemplate,
        `Maximum ${this.activeProjectList.length} project(s) allowed at this location. You have ${this.activeProjectList.length} available.`
      );
      return;
    }
    
    // ✅ MODERATE FIX: Check for duplicate projects in the same location
    // Same project can't be added twice to the same location
    // But can be added to different locations
    const existingProjectIdsInLocation = location.projects
      .map(p => p.projectId)
      .filter(id => id !== null && id !== undefined);
    
    // Create unique projects list for dropdown
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
    
    // ✅ BUG FIX: Check for duplicate before auto-selecting (when only 1 project exists)
    if (uniqueProjects.length === 1) {
      const singleProject = uniqueProjects[0];
      // Check if this project already exists in this location
      if (existingProjectIdsInLocation.includes(singleProject.projectId)) {
        this.handleError(
          new Error('Duplicate project in same location'),
          'addProject',
          true,
          'This project is already added to this location. Please select a different project.'
        );
        return;
      }
    }
    
    // Create new project
    const newProject = this.createProject(location, timesheetId);
    newProject.projectList = uniqueProjects;
    
    // ✅ MODERATE FIX: Auto-select if only 1 project and set all fields properly
    if (uniqueProjects.length === 1) {
      const singleProject = uniqueProjects[0];
      newProject.projectId = singleProject.projectId;
      newProject.projectName = singleProject.projectName;
      newProject.hasClientSideId = singleProject.hasClientSideId;
      newProject.hasClientFlag = singleProject.hasClientFlag;
      
      // ✅ Fetch client details automatically
      this.onProjectSelect(newProject.projectId!, newProject);
    }
    
    location.projects.push(newProject);
    
    // Expand the newly added project
    const locationIndex = this.timesheetLocations.indexOf(location);
    this.expandedProjectIndexMap[locationIndex] = location.projects.length - 1;
    
    // ✅ MODERATE FIX: Update projectList for all projects in all locations
    // (This ensures dropdown consistency across all locations)
    this.timesheetLocations.forEach(loc => {
      loc.projects.forEach(proj => {
        proj.projectList = uniqueProjects;
      });
    });
    
    // ✅ MODERATE FIX: Update document list after adding project
    this.getListToRenderUpload();
  }

  /**
   * Tooltip for Add Project button: explains why disabled or invites to add another.
   */
  getAddProjectButtonTitle(location: LocationEntry): string {
    if (!this.activeProjectList?.length) {
      return 'No projects available.';
    }
    if (location.projects.length >= this.activeProjectList.length) {
      return `Maximum ${this.activeProjectList.length} project(s) at this location (all available added).`;
    }
    return 'Add another project';
  }

  /**
   * Remove a project from a location
   * Ensures at least one project remains per location
   * Cleans up associated document data
   * Recalculates location and overall total hours after removal
   *
   * Uses project object reference instead of index to avoid index-swapping issues.
   */
  removeProject(location: LocationEntry, projectToRemove: ProjectEntry): void {
    // Prevent removing projects when timesheet is not fillable
    if (location.projects.length <= 1) {
      this.openAlertMod(this.alertTemplate, 'At least one project is required per location.');
      return;
    }

    const projectIndex = location.projects.indexOf(projectToRemove);
    if (projectIndex === -1) {
      console.warn('Attempted to remove a project that is no longer present in the location.');
      return;
    }
    
    // Cleanup document data for this project
    const project = location.projects[projectIndex];
    if (project && project.projectId) {
      this.cleanupProjectDocuments(project.projectId);
    }
    
    location.projects.splice(projectIndex, 1);
    this.getListToRenderUpload();
    
    // ✅ MODERATE FIX: Recalculate location and overall total hours after removal
    this.onHoursChange();
    
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
    this.empClientSideObj.clientSideId = project?.clientSideId ?? '';
    this.empClientSideObj.projectId = project.projectId!;
    this.empClientSideObj.projectName = project.projectName;
    this.empClientSideObj.empId = this.timesheetFilledForUser.empId;
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
    // Prevent adding activities when timesheet is not fillable
    if (!this.isDayTypeFillable()) {
      console.warn('Attempt to add activity when timesheet is not fillable.');
      return;
    }
    // ✅ MODERATE FIX: Add input validation
    if (!project) {
      this.handleError(new Error('Project is required'), 'addActivity', true);
      return;
    }
    
    if (!project.projectId) {
      this.handleError(new Error('Project ID is required'), 'addActivity', true, 'Please select a project first.');
      return;
    }
    
    // Validate that client and client location are selected
    if (!project.clientId || !project.clientLocationId) {
      this.handleError(
        new Error('Client and location required'),
        'addActivity',
        true,
        'Please select Client and Client Location before adding activities.'
      );
      return;
    }
    
    // ✅ Validate that clientDetails is available
    if (!project.clientDetails || !project.clientDetails.project) {
      this.handleError(
        new Error('Client details not loaded'),
        'addActivity',
        true,
        'Client details not loaded. Please reselect the project.'
      );
      return;
    }
    
    this.timesheetLocations.forEach(loc => {
      loc.projects.forEach(proj => {
        if (proj === project) {
          const newActivity = this.createActivity(null, project.projectId!);
          
          // ✅ Use teams from clientDetails API response instead of filtering activeProjectList
          if (proj.clientDetails?.project?.teams && Array.isArray(proj.clientDetails.project.teams)) {
            newActivity.clientTeamList = proj.clientDetails.project.teams.map(team => ({
              teamId: team.teamId,
              teamName: team.teamName
            }));
            
            if (newActivity.clientTeamList.length == 1) {
              newActivity.teamId = newActivity.clientTeamList[0].teamId;
              this.onProjectTeamSelect(newActivity.teamId, proj);
            }
          } else {
            newActivity.clientTeamList = [];
            console.warn('Teams not available in clientDetails');
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
    // Prevent removing activities when timesheet is not fillable
    if (!this.isDayTypeFillable()) {
      console.warn('Attempt to remove activity when timesheet is not fillable.');
      return;
    }
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
      // ✅ CRITICAL FIX: Add null checks
      let empId: number | undefined;
      if (this.timesheetAppliedFor?.toLowerCase() === 'self') {
        empId = this.currentUser?.empId;
      } else {
        const teamMember = this.teamMemberList?.find(
          e => e.empId === this.timesheetFilledForUser?.empId
        );
        empId = teamMember?.empId;
      }
      
      if (empId) {
        // ✅ CRITICAL FIX: Use proper async handling instead of setTimeout
        this.getProjectListForDateAndEmpId(empId).then(() => {
          this.populateProjectsForLocation(location);
          this.getListToRenderUpload();
        }).catch(error => {
          console.error('Error loading projects:', error);
          this.openAlertMod(this.alertTemplate, 'Failed to load projects. Please try again.');
        });
        return;
      } else {
        this.openAlertMod(this.alertTemplate, 'Unable to determine employee ID. Please select date and team member first.');
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
  dayTypesToShow: any[] = [];
  getAllDayTypes(): void {
    this.timesheetNewService.getAllDayTypes()
      .pipe(first(), takeUntil(this.destroy$))
      .subscribe({
        next: async(response: any) => {
          if (response.serviceStatus == "Success") {
            this.allDayTypes = response.serviceResponse || [];
            // Autofill moved to onTimesheetAppliedForChange to avoid race with resetForm
            console.log("Day type changes", this.allDayTypes);

            let excludedIds = [...this.dayTypeToBeExcluded];
            // if(this.timesheetAppliedFor.toLocaleLowerCase() === 'self') {
            //   const empId = this.currentUser.empId;
            //   let response:any = await firstValueFrom(this.employeeService.getEmployeeBillableType(this.currentUser?.empId));
            //   let billableType = "";
            //   if (response.serviceStatus === "Success") {
            //   billableType = response.serviceResponse;
            //   }
            //   if (billableType?.toLowerCase() === 'tnm') {
            //     excludedIds.push("ApMoSys Holiday");
            //   }
            // }
            this.allDayTypes = this.allDayTypes.filter(f =>{
              return !excludedIds.includes(f.dayType);
            })
            console.log(this.allDayTypes);
            this.dayTypesToShow = this.allDayTypes.map(dayType => {
              return {
                ...dayType,  
                dayType: dayType.dayTypeId === 3 
                  ? 'Working on non-working day' 
                  : dayType.dayType
              };
            });
                  
                const highPriorityList = [
                    "Working", 
                    "Working on non-working day",
                    "Half-day Working"
                  ];

                const highPriorityItems = this.dayTypesToShow.filter(dt => 
                  highPriorityList.includes(dt.dayType)
                );

                const lowPriorityList = this.dayTypesToShow.filter(dt => 
                  !highPriorityList.includes(dt.dayType)
                );
                this.dayTypesToShow = [...highPriorityItems, ...lowPriorityList];
          } else {
            // ✅ MODERATE FIX: Use centralized error handling
            this.handleError(
              new Error(response.serviceResponse || 'Failed to load day types'),
              'getAllDayTypes',
              false // Don't show to user, just log
            );
            this.allDayTypes = [];
            this.dayTypesToShow = [];
          }
        },
        error: (error) => {
          this.handleError(error, 'getAllDayTypes', false);
          this.allDayTypes = [];
          this.dayTypesToShow = [];
        }
      });
  }
  /**
   * Handle day type change
   * When switching to non-fillable: clear all user-filled data and re-init with default location (NA)
   * and one project row; only description is user-fillable at project level.
   * When switching to fillable: allow user to choose location type freely.
   */
  onDayTypeChange(event: any): void {
    const newDayTypeId = this.dayType;
  
    // If current date is a Holiday/Week-off and user tries to switch to Working / Half-day Working,
    // prevent change on UI itself (backend will also enforce).
    const isHolidayOrWeekOffDate =
      !!this.fromDate &&
      this.nonWorkingAllowedDates.length > 0 &&
      this.nonWorkingAllowedDates.includes(this.fromDate);

    if (isHolidayOrWeekOffDate && newDayTypeId != null) {
      const dt = this.allDayTypes?.find((d: any) => d.dayTypeId === newDayTypeId);
      const name = dt?.dayType ? String(dt.dayType).toLowerCase() : '';
      const isWorkingLike =
        name === 'working' ||
        name === 'half-day working';

      if (isWorkingLike) {
        // Revert change
        // if (this.lastDayTypeId != null) {
        //   this.dayType = this.lastDayTypeId;
        // }
        // this.openAlertMod(
        //   this.alertTemplate,
        //   'Date is configured as Holiday/Week Off. Only Non-working timesheet is allowed on this date.'
        // );
        // Do not proceed with rest of change handling
        // return;
        // this.fromDate = null;
        this.resetForm('dayType');
        return;
      }
    } 
      if(this.halfDayValidation()){
      return;
    }
this.isNightShift = false;
        this.toDate = null;
    const dayTypeFillable = this.isDayTypeFillable();
    this.clearAndInitOnDayTypeChange(dayTypeFillable);
    // if (!dayTypeFillable) {
    //   // Switching to non-fillable: clear previous filled data and set up default location + one project
    //   this.clearAndInitForNonFillableDayType();
    // } else {
    //   // Switching to fillable: allow user to choose location type freely
    //   // this.timesheetLocations.forEach(loc => {
    //   //   loc.workLocationTypeId = null;
    //   //   this.disableAdd = false;
    //   // });
    //   this.resetForm();
    // }

    this.getListToRenderUpload();  

    // Update lastDayTypeId after successful change
    this.lastDayTypeId = this.dayType;

    // Recompute date picker disabled dates based on updated day type
    this.recalculateDisabledDatesForPicker();
  }

  /**
   * Helper: check if current day type is \"Non-working\" (fillable non-working day).
   * Uses allDayTypes metadata to match by name.
   */
  private isNonWorkingDayType(): boolean {
    if (!this.dayType || !this.allDayTypes?.length) return false;
    const dt = this.allDayTypes.find((d: any) => d.dayTypeId === this.dayType);
    if (!dt || typeof dt.dayType !== 'string') return false;
    return dt.dayType.toLowerCase() === 'non-working';
  }

  /**
   * Load all holidays (including week-offs) for the user's location,
   * then derive allowed Non-working dates within [minDateForPicker..maxDateForPicker].
   * 
   * Race condition protection:
   * - Uses requestId to ignore stale responses
   * - Captures min/max at request start and validates in response handler
   */
  private loadNonWorkingSelectableDates(): void {
    if (!this.minDateForPicker || !this.maxDateForPicker) {
      this.nonWorkingAllowedDates = [];
      return;
    }

    // Avoid refetch when range unchanged
    if (
      this.nonWorkingRangeCache &&
      this.nonWorkingRangeCache.min === this.minDateForPicker &&
      this.nonWorkingRangeCache.max === this.maxDateForPicker &&
      this.nonWorkingAllowedDates.length > 0
    ) {
      this.applyNonWorkingDisabledDates();
      return;
    }

    // Increment request ID to invalidate any in-flight requests
    const currentRequestId = ++this.holidayLoadRequestId;
    
    // Capture min/max at request start to validate in response handler
    const requestMin = this.minDateForPicker;
    const requestMax = this.maxDateForPicker;

    const holidayObj = new Holiday();
    // Use employee work location; backend will merge state-specific + 'All'
    (holidayObj as any).state = this.currentUser?.workLocation || null;

    this.holidayService.getAllHolidays(holidayObj)
      .pipe(first(), takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          // Ignore stale responses: check if this request is still current
          if (currentRequestId !== this.holidayLoadRequestId) {
            console.log(`[loadNonWorkingSelectableDates] Ignoring stale response (requestId ${currentRequestId} vs current ${this.holidayLoadRequestId})`);
            return;
          }

          // Validate that min/max haven't changed during the async call
          if (this.minDateForPicker !== requestMin || this.maxDateForPicker !== requestMax) {
            console.log(`[loadNonWorkingSelectableDates] Min/max changed during request, ignoring response`);
            // Retry with new range
            this.recalculateDisabledDatesForPicker();
            return;
          }

          if (response?.serviceStatus === 'Success' && Array.isArray(response.serviceResponse)) {
            const min = moment(requestMin, 'DD-MM-YYYY').startOf('day');
            const max = moment(requestMax, 'DD-MM-YYYY').endOf('day');

            this.nonWorkingAllowedDates = response.serviceResponse
              .map((h: any) => h.dateOfHoliday as string | undefined)
              .filter((d: string | undefined) => !!d)
              .map((d: string) => moment(d, 'YYYY-MM-DD'))
              .filter((m) => m.isValid() && !m.isBefore(min, 'day') && !m.isAfter(max, 'day'))
              .map((m) => m.format('DD-MM-YYYY'));

            this.nonWorkingRangeCache = {
              min: requestMin,
              max: requestMax
            };

            this.applyNonWorkingDisabledDates();
    } else {
            // On failure, fall back to base behavior
            this.nonWorkingAllowedDates = [];
            this.nonWorkingRangeCache = null;
            this.disabledDatesForPicker = [...this.disabledDatesBase];
          }
        },
        error: () => {
          // Ignore errors from stale requests
          if (currentRequestId !== this.holidayLoadRequestId) {
            return;
          }
          this.nonWorkingAllowedDates = [];
          this.nonWorkingRangeCache = null;
          this.disabledDatesForPicker = [...this.disabledDatesBase];
        }
      });
  }

  /**
   * Build full date range [minDateForPicker..maxDateForPicker] in dd-MM-YYYY format.
   */
  private buildDateRangeDDMMYYYY(): string[] {
    if (!this.minDateForPicker || !this.maxDateForPicker) return [];
    const start = moment(this.minDateForPicker, 'DD-MM-YYYY');
    const end = moment(this.maxDateForPicker, 'DD-MM-YYYY');
    if (!start.isValid() || !end.isValid()) return [];

    const result: string[] = [];
    for (let m = start.clone(); !m.isAfter(end, 'day'); m.add(1, 'day')) {
      result.push(m.format('DD-MM-YYYY'));
    }
    return result;
  }

  /**
   * Apply Non-working rules to disabledDatesForPicker:
   * - Start from disabledDatesBase (already-filled, etc.)
   * - For Non-working: additionally disable all dates in range that are NOT in nonWorkingAllowedDates.
   * - EXCEPTION: Cron-filled timesheets (holiday/week-off dayTypes 4,6) should remain ENABLED
   *   so users can overwrite them (backend already supports this).
   */
  private applyNonWorkingDisabledDates(): void {
    if (!this.isNonWorkingDayType()) {
      this.disabledDatesForPicker = [...this.disabledDatesBase];
      return;
    }

    if (!this.minDateForPicker || !this.maxDateForPicker) {
      this.disabledDatesForPicker = [...this.disabledDatesBase];
      return;
    }

    const allDates = this.buildDateRangeDDMMYYYY();
    const allowedSet = new Set(this.nonWorkingAllowedDates);

    // For Non-working day type: disable dates that are NOT holidays/week-offs
    const toDisableExtra = allDates.filter(d => !allowedSet.has(d));
    
    // Start with disabledDatesBase but EXCLUDE cron-filled dates (holiday/week-off)
    // Cron service auto-fills timesheets for holidays/week-offs, but users should be able to overwrite them
    const filteredBase = this.disabledDatesBase.filter(d => !this.isCronFilledTimesheet(d));
    
    const merged = new Set(filteredBase);
    toDisableExtra.forEach(d => merged.add(d));

    this.disabledDatesForPicker = Array.from(merged);
    
    console.log('[applyNonWorkingDisabledDates] Cron-filled dates kept enabled:', 
      this.disabledDatesBase.filter(d => this.isCronFilledTimesheet(d)));
  }

  /**
   * Check if the timesheet for a given date is cron-filled (holiday or week-off).
   * Cron-filled timesheets can be overwritten by users, so they should remain selectable.
   * @param dateStr Date in dd-MM-YYYY format
   * @returns true if the existing timesheet for this date was cron-filled (dayTypeId 4 or 6)
   */
  private isCronFilledTimesheet(dateStr: string): boolean {
    const dayTypeId = this.existingTimesheetDayTypes.get(dateStr);
    if (dayTypeId == null) {
      return false;
    }
    return this.CRON_FILLED_DAY_TYPES.includes(dayTypeId);
  }

  /**
   * Recalculate disabled dates for date picker based on:
   * - Base disabled dates (existing timesheets)
   * - Day type (Non-working → only holiday/week-off dates enabled)
   * 
   * Race condition protection:
   * - Invalidates any in-flight holiday requests when called
   * - Ensures only the latest request's response is applied
   */
  private recalculateDisabledDatesForPicker(): void {
    if (!this.minDateForPicker || !this.maxDateForPicker) {
      this.disabledDatesForPicker = [...this.disabledDatesBase];
      // Clear cache if min/max are invalid
      this.nonWorkingRangeCache = null;
      this.nonWorkingAllowedDates = [];
      return;
    }

    if (!this.isNonWorkingDayType()) {
      // For all other day types, use base rules only
      this.disabledDatesForPicker = [...this.disabledDatesBase];
      // Clear Non-working cache when switching away from Non-working
      this.nonWorkingRangeCache = null;
      this.nonWorkingAllowedDates = [];
      return;
    }

    // For Non-working, ensure we have holiday/week-off dates loaded
    // Check cache validity: range must match exactly
    if (
      this.nonWorkingRangeCache &&
      this.nonWorkingRangeCache.min === this.minDateForPicker &&
      this.nonWorkingRangeCache.max === this.maxDateForPicker &&
      this.nonWorkingAllowedDates.length > 0
    ) {
      // Cache hit: apply immediately
      this.applyNonWorkingDisabledDates();
    } else {
      // Cache miss or invalid: load holidays (will increment requestId, invalidating any in-flight requests)
      this.loadNonWorkingSelectableDates();
    }
  }

  /**
   * Clear all user-filled data and initialize for non-fillable day type:
   * - Clear presence / check-in / check-out
   * - Clean document upload data
   * - Replace locations with one location with default work location (e.g. 4 -> NA)
   * - One project per location with project list from activeProjectList; user only fills description
   */
  private clearAndInitOnDayTypeChange(isDayTypeFillable:any): void {
    // Clear presence and attendance time (not used for non-fillable)
    this.apmosysInTime = null;
    this.apmosysOutTime = null;
    this.totalPresence = 0;
    this.useApmosysTiming = false;

    // Clean document upload (non-fillable does not show document upload)
    this.cleanupDocumentData();
    this.documentData = [];
    this.selectedFile = [];
    this.uniqueProjectsList = [];
    this.empHasClientSideId = false;
    this.fileTracker.clear();

    // Clear highlight/validation state
    this.highlightLocationList = [];
    this.highlightLocationIdSet = new Set();

    // Replace with single location: default work location (4 -> NA), one project
    this.timesheetLocations = [this.createLocation()];
    const location = this.timesheetLocations[0];

    // Populate project list for the single project so user can select Project, Client, Client Location
    const uniqueProjects = Array.from(
      new Map(
        (this.activeProjectList || []).map(p => [
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
    location.projects.forEach(proj => {
      proj.projectList = uniqueProjects;
    });

    if(isDayTypeFillable){
     this.disableAdd = false;
    }else{
    this.disableAdd = true;
    }
    
    this.expandedLocationIndex = 0;
    this.expandedProjectIndexMap = { 0: 0 };
    
    // Auto-select project if only one is available (after projects are loaded)
    if(isDayTypeFillable){
      this.autoSelectProjectIfSingle();
    }
  }

  /**
   * Auto-select project for non-fillable day types when only one project is available.
   * Called after projects are loaded or after initializing non-fillable day type.
   * Also updates project lists for all locations when projects are loaded.
   */
  private autoSelectProjectIfSingle(): void {
    // Only for non-fillable day types
    if (this.isDayTypeFillable()) {
      return;
    }

    // Update project lists for all locations (in case projects were loaded after initialization)
    if (this.activeProjectList && this.activeProjectList.length > 0) {
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
      
      this.timesheetLocations.forEach(location => {
        location.projects.forEach(proj => {
          proj.projectList = uniqueProjects;
        });
      });
    }

    // Check if exactly one project is available for auto-selection
    if (!this.activeProjectList || this.activeProjectList.length !== 1) {
      return;
    }

    // Find location with unselected project
    if (!this.timesheetLocations || this.timesheetLocations.length === 0) {
      return;
    }

    const location = this.timesheetLocations[0];
    if (!location || !location.projects || location.projects.length === 0) {
      return;
    }

    const project = location.projects[0];
    
    // Skip if project is already selected
    if (project.projectId) {
      return;
    }

    // Auto-select the single project (reuses shared helper)
    const singleProject = this.activeProjectList[0];
    this.applySingleProjectSelection(project, singleProject);
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
      // Autofill only for self (uses currentUser.empId); run after resetForm to avoid race
      if (this.selectedDate && !this.isUpdation) {
        this.loadAutofillData(this.formatDateDDMMYYYY(this.selectedDate));
      }
    } else if(this.timesheetAppliedFor.toLocaleLowerCase() === 'team'){
      this.getAllTeamMemberList();
    }else {
      if (this.selectedDate) {
        this.loadAutofillData(this.formatDateDDMMYYYY(this.selectedDate));
      }else{
      console.error("Invalid selection")
      }      
    }
  }
  // ============================================
  // METHODS - Modal & Alert Management
  // ============================================

  /**
   * Open alert modal with message
   */
  openAlertMod(template: TemplateRef<any>, message: any): void {
    // Close any existing alert so OK always closes the current one (avoids stale ref)
    this.closeAlertModal();
    this.alertMessage = message;
    this.alertModalRef = this.modalService.open(template, {
      modalDialogClass: 'ts-alert-modal',
      backdrop: 'static'
    });
  }

  /**
   * Close the simple alert modal (called by OK button in #alert_message template)
   */
  closeAlertModal(): void {
    if (this.alertModalRef) {
      try {
        this.alertModalRef.close();
      } finally {
        this.alertModalRef = null;
      }
    }
  }

  openAlertModMessageForHolidayCreate(template: TemplateRef<any>, message: any): void {
    // Close any existing alert so OK always closes the current one (avoids stale ref)
    this.closeAlertModal();
    this.alertMessage = message;
    this.alertMessageForHolidayCreateModalRef = this.modalService.open(template, {
      modalDialogClass: 'ts-alert-modal',
      backdrop: 'static'
    });
  }

  closeAlertMessageForHolidayCreate(){

    this.alertMessageForHolidayCreateModalRef.close();
    this.getDeptBaseProjectData();

  }

  /**
   * Open a simple OK-only alert without a backdrop (click-through background)
   * Used for non-blocking consistency warnings such as client approval status across locations.
   */
  openAlertNoBackdrop(template: TemplateRef<any>, message: any, onOk?: () => void): void {
    // Close any existing alert of this type
    this.closeAlertModal();
    this.alertMessage = message;
    this.alertModalRef = this.modalService.open(template, {
      modalDialogClass: 'ts-alert-modal',
      backdrop: false
    });

    if (onOk && this.alertModalRef) {
      this.alertModalRef.result
        .then(() => {
          onOk();
        })
        .catch(() => {
          // Dismissed (e.g. ESC) – ignore, as this alert is informational
        });
    }
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
      if(teamMember?.isTimesheetFilledByMember.toLocaleLowerCase() == 'true' && this.isCreation){
        this.appelectMember = null;
        this.selectedTeamMember = null;
        userObj = new User();
        this.handleError(
          new Error('Timesheet cannot be filled for team member more than 2 days.'),
          'onProjectSelect',
          true,
          'Timesheet cannot be filled for team member more than 2 days.'
        );
        
        return;
      }
    }
    this.timesheetFilledForUser = userObj;
    console.log("onTimesheetAppliedForChange obj= ",userObj)
    
    // Load available timesheets for date filtering after setting user
    if (userObj.empId && this.serverDate) {
      this.getAllAvailableTimesheetByEmpId(userObj);
      this.getHalfDayLeaves(userObj.empId);
    }
  }

  /**
   * Get all team members for the current user
   * @returns Promise that resolves when team members are loaded
   */
  getAllTeamMemberList(): Promise<void> {
    // resetForm() already called in onTimesheetAppliedForChange() before this method
    if (!this.currentUser?.empId) {
      return Promise.reject(new Error('Current user empId not available'));
    }

    let employeeObj = new Employee();
      employeeObj.empId = this.currentUser.empId;

    return new Promise<void>((resolve, reject) => {
      this.teamViewService.getAllTeamMemberView(employeeObj)
        .pipe(first(), takeUntil(this.destroy$))
        .subscribe({
          next: (response: any) => {
            if (response.serviceStatus == "Success") {
              this.teamMemberList = response.serviceResponse || [];
              resolve();
            } else {
              const errorMsg = response.serviceResponse || 'Failed to load team members';
              console.error(errorMsg);
              this.teamMemberList = [];
              reject(new Error(errorMsg));
            }
          },
          error: (error) => {
            console.error('Error loading team members:', error);
            this.teamMemberList = [];
            reject(error);
          }
        });
    });
  }

  /**
   * Handle team member selection
   * When user selects a different team member, form is reset so previous member's
   * projects/activities/locations are not shown (different member may have different assignments).
   */
  onTeamMemberSelect(teamMemberOrId: any): void {
    // app-my-select emits only the value (empId), not the full object
    let teamMember: any = null;

    if (teamMemberOrId && typeof teamMemberOrId === 'object' && teamMemberOrId.empId) {
      teamMember = teamMemberOrId;
    } else if (teamMemberOrId != null && this.teamMemberList && this.teamMemberList.length > 0) {
      teamMember = this.teamMemberList.find(emp => emp.empId === teamMemberOrId || emp.empId === Number(teamMemberOrId));
    }

    if (!teamMember || !teamMember.empId) {
      return;
    }

    const previousEmpId = this.timesheetFilledForUser?.empId ?? null;
    const newEmpId = teamMember.empId;
    const isDifferentMember = previousEmpId != null && Number(previousEmpId) !== Number(newEmpId);

    if (isDifferentMember && !this.isUpdation) {
      // Reset form so we don't show previous member's projects/activities/locations
      this.resetForm();
    }

      this.timesheetFilledForUser.empId = teamMember.empId;
      this.timesheetFilledForUser.name = teamMember.name;
      this.selectedTeamMember = teamMember;
      this.getTimesheetMetadata();
      if (this.serverDate) {
        this.getAllAvailableTimesheetByEmpId(this.timesheetFilledForUser);
    }
  }

  /**
   * Helper: Apply single project selection to a project entry.
   * Sets project fields and triggers onProjectSelect to load client/location data.
   * Used by both populateProjectsForLocation (fillable) and autoSelectProjectIfSingle (non-fillable).
   */
  private applySingleProjectSelection(project: ProjectEntry, singleProjectData: any): void {
    if (!project || !singleProjectData) {
      return;
    }
    
    project.projectId = singleProjectData.projectId;
    project.projectName = singleProjectData.projectName;
    project.hasClientSideId = singleProjectData.hasClientSideId || false;
    project.hasClientFlag = singleProjectData.hasClientFlag || false;
    
    // Trigger onProjectSelect to fetch client details automatically
    if (project.projectId) {
      this.onProjectSelect(project.projectId, project);
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
    
    // Populate projects for the selected location ONLY
    // Do not touch other locations to avoid cross-row data loss
    location.projects = [this.createProject(location, null)]; // Reset to one project for this location
    location.projects.forEach(proj => {
      proj.projectList = uniqueProjects;
      
      // Auto-select if only 1 project available (reuses shared helper)
      if (proj.projectList.length === 1) {
        this.applySingleProjectSelection(proj, proj.projectList[0]);
      }
    });
  }
 
  /**
   * Handle project selection - populate clients and reset project-specific fields
   * Validates for duplicate projects in the same location
   * @param projectId - selected project ID
   * @param changedProject - project entry where selection happened
   */
  onProjectSelect(
    projectId: number | null | undefined,
    changedProject: ProjectEntry
  ): void {

  console.log("******executed-1****");
  console.log('projectId value:', projectId, typeof projectId);

    if (!changedProject) {
      console.warn('onProjectSelect called without project context');
      return;
    }

  // 🔹 Find parent location
    const parentLocation = this.timesheetLocations.find(loc =>
      loc.projects.includes(changedProject)
    );

    if (!parentLocation) {
      console.warn('Parent location not found for project selection');
      return;
    }

  const projectIndex = parentLocation.projects.indexOf(changedProject);

  // =========================================================
  // CASE 1: Placeholder selected → RESET
  // =========================================================
  if (projectId === null || projectId === undefined) {

    const resetObject: ProjectEntry = {
      ...changedProject,
      projectId: null,
      projectName: '',
      hasClientSideId: false,
      hasClientFlag: false,
      shadowEmpId: null,
      isShadowTimesheet: false,
      isShadowForSelf: false,
      clientSideId: null,
      clientId: null,
      clientLocationId: null,
      clientApprovalStatus: null,
      totalWorkingHours: null,
      activities: [this.createActivity(null, null)],
      projectActivities: [],
      clientList: [],
      clientLocationList: [],
      clientDetails: null,
      _lastValidProjectId: null,
      _lastValidProjectName: ''
    };

    parentLocation.projects[projectIndex] = resetObject;

    this.getListToRenderUpload();
    this.onHoursChange();

    return;
  }

  // 🔹 Convert to number safely
  projectId = Number(projectId);

  if (isNaN(projectId) || projectId <= 0) {
    console.warn('Invalid projectId:', projectId);
    return;
  }

  // =========================================================
  // CASE 2: Duplicate detection
  // =========================================================
    const hasDuplicateInLocation = parentLocation.projects.some(
      p => p !== changedProject && p.projectId === projectId
    );

    if (hasDuplicateInLocation) {

    const revertObject: ProjectEntry = {
      ...changedProject,
      projectId: null,
      projectName: '',
      hasClientSideId: false,
      hasClientFlag: false,
      shadowEmpId: null,
      isShadowTimesheet: false,
      isShadowForSelf: false,
      clientSideId: null,
      clientId: null,
      clientLocationId: null,
      clientApprovalStatus: null,
      totalWorkingHours: null,
      activities: [this.createActivity(null, null)],
      projectActivities: [],
      clientList: [],
      clientLocationList: [],
      clientDetails: null,
      _lastValidProjectId: null,
      _lastValidProjectName: ''
    };

    parentLocation.projects[projectIndex] = revertObject;

      this.getListToRenderUpload();
      this.onHoursChange();

      this.handleError(
        new Error('Duplicate project in same location'),
        'onProjectSelect',
        true,
        'This project is already added to this location. Please select a different project.'
      );

      return;
    }

  // =========================================================
  // CASE 3: Valid Selection
  // =========================================================

    const matchedProject = changedProject.projectList?.find(
      p => p.projectId === projectId
    );

    //  TNM validation
// if (matchedProject?.poProjectType?.toLowerCase() === 'tnm') {

//   const revertObject: ProjectEntry = {
//     ...changedProject,
//     projectId: null,
//     projectName: '',
//     hasClientSideId: false,
//     hasClientFlag: false,
//     shadowEmpId: null,
//     isShadowTimesheet: false,
//     isShadowForSelf: false,
//     clientSideId: null,
//     clientId: null,
//     clientLocationId: null,
//     clientApprovalStatus: null,
//     totalWorkingHours: null,
//     activities: [this.createActivity(null, null)],
//     projectActivities: [],
//     clientList: [],
//     clientLocationList: [],
//     clientDetails: null,
//     _lastValidProjectId: null,
//     _lastValidProjectName: ''
//   };

//   parentLocation.projects[projectIndex] = revertObject;

//   this.handleError(
//     new Error('TNM project not allowed'),
//     'onProjectSelect',
//     true,
//     'ApMoSys Holiday cannot be applied because the project type is TNM'
//   );

//   this.getListToRenderUpload();
//   this.onHoursChange();

//   return;
// }

  const updatedProject: ProjectEntry = {
    ...changedProject,
    projectId: projectId,
    projectName: matchedProject?.projectName || '',
    hasClientSideId: matchedProject?.hasClientSideId || false,
    hasClientFlag: matchedProject?.hasClientFlag || false,
    shadowEmpId: null,
    isShadowTimesheet: false,
    isShadowForSelf: false,
    clientSideId: null,
    clientId: null,
    clientLocationId: null,
    clientApprovalStatus: null,
    totalWorkingHours: null,
    activities: [this.createActivity(null, projectId)],
    projectActivities: [],
    clientList: [],
    clientLocationList: [],
    clientDetails: null,
    _lastValidProjectId: projectId,
    _lastValidProjectName: matchedProject?.projectName || ''
  };

  parentLocation.projects[projectIndex] = updatedProject;

  // 🔹 Fetch client details
  this.getClientDetailsByProjectIdAndEmpId(updatedProject);

  const targetEmpId =
    this.timesheetAppliedFor?.toLowerCase() === 'self'
      ? this.currentUser?.empId 
      : this.timesheetFilledForUser?.empId;
    
  if (targetEmpId && updatedProject.hasClientSideId) {
      this.getClientSideIdByProjectIdAndEmpId(projectId, targetEmpId);
    } else {
      console.warn('Employee ID not available for project selection');
    }
    
    this.getListToRenderUpload();
  }



  /**
   * Fetch client details for a specific project and employee
   * Stores client details in project.clientDetails for use in dropdowns
   * @param project - Project entry to fetch client details for
   * @param empIdOverride - When provided (e.g. from edit populate), use this empId instead of deriving
   * @param isPopulateMode - When true, show at most one error popup (avoids duplicate popups for multiple projects)
   */
  getClientDetailsByProjectIdAndEmpId(project: ProjectEntry, empIdOverride?: number, isPopulateMode?: boolean): void {
    if (!project.projectId) {
      console.error('Project ID is required to fetch client details');
      return;
    }

    const targetEmpId = empIdOverride != null
      ? empIdOverride
      : (this.timesheetAppliedFor?.toLowerCase() === 'self' ? this.currentUser?.empId : this.timesheetFilledForUser?.empId);

    if (targetEmpId == null || targetEmpId === undefined) {
      this.handleError(new Error('Employee ID not available'), 'getClientDetailsByProjectIdAndEmpId', false);
      return;
    }

    if (!project || !project.projectId) {
      this.handleError(new Error('Invalid project: projectId is required'), 'getClientDetailsByProjectIdAndEmpId', true);
      return;
    }

    const payload: any = {
      empId: Number(targetEmpId),
      projectId: project.projectId
    };

    // Add date filter to get only teams active on the selected date
    if (this.fromDate) {
      const dateParsed = this.parseDDMMYYYY(this.fromDate);
      if (dateParsed) {
        payload.date = `${dateParsed.getFullYear()}-${String(dateParsed.getMonth() + 1).padStart(2, '0')}-${String(dateParsed.getDate()).padStart(2, '0')}T00:00:00`;
      }
    }

    const showClientDetailsError = (userMessage: string) => {
      if (isPopulateMode && this.clientDetailsErrorShownThisPopulate) return;
      if (isPopulateMode) this.clientDetailsErrorShownThisPopulate = true;
      this.handleError(
        new Error(userMessage),
        'getClientDetailsByProjectIdAndEmpId',
        true,
        userMessage
      );
    };

    this.timesheetService.getClientDetailsByProjectIdAndEmpId(payload)
      .pipe(first(), takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          if (response.serviceStatus === "Success") {
            if (!response.serviceResponse || !response.serviceResponse.clientId) {
              showClientDetailsError('Failed to load client information for this project. Invalid response format.');
              return;
            }
            project.clientDetails = response.serviceResponse;
            project.clientList = [{
              clientId: project.clientDetails.clientId != null ? Number(project.clientDetails.clientId) : null,
              clientName: project.clientDetails.clientName
            }];

            // Ensure clientId is set (prefer existing value from timesheet, fallback to response)
            if (project.clientId == null && project.clientDetails.clientId != null) {
              project.clientId = Number(project.clientDetails.clientId);
            }

            // Always populate client locations using the current clientId.
            // In update mode this makes sure Client dropdown, Client Location dropdown and
            // their dependent data (teams/activities) are hydrated even when there are
            // multiple possible locations.
            // Use setTimeout to ensure Angular change detection picks up the clientDetails
            if (project.clientId) {
              setTimeout(() => {
                this.onProjectClientSelect(project.clientId!, project);
              }, 0);
            }
          } else {
            const msg = response.serviceResponse || 'No valid client details found.123';
            showClientDetailsError('Failed to load client details: ' + msg);
          }
        },
        error: (error) => {
          showClientDetailsError('Error loading client details. Please try again.');
        }
      });
  }

 /**
   * Get client side ID by project ID and employee ID
   * Opens modal if client side ID is required but not found
   */
  async getClientSideIdByProjectIdAndEmpId(projectId: any, empId: any): Promise<void> {
    // ✅ MODERATE FIX: Add input validation
    if (!projectId || projectId <= 0) {
      this.handleError(new Error('Invalid projectId'), 'getClientSideIdByProjectIdAndEmpId', false);
      return;
    }
    
    if (!empId || empId <= 0) {
      this.handleError(new Error('Invalid empId'), 'getClientSideIdByProjectIdAndEmpId', false);
      return;
    }
    
    await this.timesheetService.getClientSideIdByProjectIdAndEmpId(projectId, empId)
      .pipe(first(), takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
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
        },
        error: (error) => {
          this.handleError(error, 'getClientSideIdByProjectIdAndEmpId', false);
        }
      });
  }
  /**
   * Handle client selection at project level - populate client locations
   * @param clientId - Selected client ID
   * @param project - Project entry where client was selected
   */
  onProjectClientSelect(clientId: number | null | undefined, project: ProjectEntry): void {
    // ✅ MODERATE FIX: Add input validation
    if (!clientId || clientId <= 0) {
      console.warn('Invalid clientId:', clientId);
      return;
    }
    
    if (!project) {
      console.warn('Project is required for client selection');
      return;
    }
    
    // Find the project where client was selected (use projectId for reliable matching)
    const targetProjectId = project.projectId;
    if (!targetProjectId) {
      console.warn('[onProjectClientSelect] Project ID is required');
      return;
    }

    this.timesheetLocations.forEach(loc => {
      loc.projects.forEach(proj => {
        // Match by projectId instead of reference to ensure we find the right project
        if (proj.projectId === targetProjectId && proj.clientDetails) {
          // ✅ Use clientLocations from clientDetails API response
          if (proj.clientDetails.clientLocations && Array.isArray(proj.clientDetails.clientLocations)) {
            proj.clientLocationList = proj.clientDetails.clientLocations.map(loc => ({
              clientLocationId: loc.clientLocationId != null ? Number(loc.clientLocationId) : null,
              clientLocation: loc.clientLocation
            }));
            
            // In update mode we may already have a clientLocationId from the saved timesheet.
            // Ensure the ID matches type and exists in the list, then select it.
            console.log(`[onProjectClientSelect] Checking clientLocationId for project ${proj.projectId}:`, {
              clientLocationId: proj.clientLocationId,
              clientLocationIdType: typeof proj.clientLocationId,
              clientLocationListLength: proj.clientLocationList.length,
              clientLocationList: proj.clientLocationList.map(l => ({ id: l.clientLocationId, idType: typeof l.clientLocationId, name: l.clientLocation }))
            });
            
            if (proj.clientLocationId != null && proj.clientLocationId !== undefined) {
              // Normalize ID to number for matching (handle both string and number from backend)
              const clientLocationIdNum = Number(proj.clientLocationId);
              if (!isNaN(clientLocationIdNum)) {
                const matchedLocation = proj.clientLocationList.find(loc => {
                  const locIdNum = Number(loc.clientLocationId);
                  return !isNaN(locIdNum) && locIdNum === clientLocationIdNum;
                });
                
                if (matchedLocation) {
                  // Ensure ID is set as number for Angular binding
                  proj.clientLocationId = clientLocationIdNum;
                  console.log(`[onProjectClientSelect] ✅ Matched clientLocationId ${proj.clientLocationId} for project ${proj.projectId}`);
                  // Use setTimeout to ensure Angular change detection picks up the change
                  setTimeout(() => {
                    this.onProjectClientLocationSelect(proj.clientLocationId!, proj);
                  }, 0);
                } else {
                  console.warn(`[onProjectClientSelect] ❌ clientLocationId ${proj.clientLocationId} (normalized: ${clientLocationIdNum}) not found in clientLocationList for project ${proj.projectId}. Available IDs:`, proj.clientLocationList.map(l => Number(l.clientLocationId)));
                }
              } else {
                console.warn(`[onProjectClientSelect] ❌ Invalid clientLocationId: ${proj.clientLocationId} (cannot convert to number)`);
              }
            } else if (proj.clientLocationList.length === 1) {
              // Auto-select if only one location
              proj.clientLocationId = Number(proj.clientLocationList[0].clientLocationId);
              console.log(`[onProjectClientSelect] Auto-selected single clientLocationId ${proj.clientLocationId} for project ${proj.projectId}`);
              setTimeout(() => {
                this.onProjectClientLocationSelect(proj.clientLocationId!, proj);
              }, 0);
            } else if (proj.clientLocationList.length > 1) {
              console.warn(`[onProjectClientSelect] ⚠️ Multiple client locations available (${proj.clientLocationList.length}) but no clientLocationId set for project ${proj.projectId}. This is expected in create mode but should be set in update mode.`);
            }
          } else {
            console.warn('[onProjectClientSelect] Client locations not available in clientDetails');
            proj.clientLocationList = [];
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
    // Validate that clientLocationId and project are not null
    if (!clientLocationId) {
      return;
    }

    if (!project) {
      console.warn('Project is required for client location selection');
      return;
    }

    // Same project cannot be added with different client location across locations.
    // If another row has same projectId+clientId but different clientLocationId, reject this selection.
    const hasSameProjectDifferentLocation = this.timesheetLocations.some(loc =>
      loc.projects.some(p =>
        p !== project &&
        p.projectId === project.projectId &&
        p.clientId === project.clientId &&
        p.clientLocationId != null &&
        Number(p.clientLocationId) !== Number(clientLocationId)
      )
    );

    if (hasSameProjectDifferentLocation) {
      // Defer reset so ngModel and the select's ControlValueAccessor get the new value in the next tick.
      // Otherwise the dropdown keeps showing the invalid selection because we're updating in the same
      // tick as ngModelChange.
      setTimeout(() => {
      project.clientLocationId = null;
      if (project.activities && Array.isArray(project.activities)) {
        project.activities.forEach(activity => {
          activity.clientTeamList = [];
          activity.teamId = null;
        });
      }
        this.cdr.detectChanges();
      }, 0);

      this.openAlertMod(
        this.alertTemplate,
        'You cannot add same projects with different client location'
      );
      return;
    }

    // ✅ Use teams from clientDetails API response instead of filtering activeProjectList
    if (project.clientDetails && project.clientDetails.project) {
      const teams = project.clientDetails.project.teams.map(team => ({
        teamId: team.teamId != null ? Number(team.teamId) : null,
        teamName: team.teamName
      }));

      // Populate teams for all activities in this project
      project.activities.forEach(activity => {
        activity.clientTeamList = teams;

        // In update mode, if activity already has a teamId from backend, ensure it matches type.
        if (activity.teamId) {
          // Normalize teamId to number and ensure it exists in the list
          const teamIdNum = Number(activity.teamId);
          const matchedTeam = teams.find(t => Number(t.teamId) === teamIdNum);
          if (matchedTeam) {
            activity.teamId = teamIdNum;
          } else {
            console.warn(`[onProjectClientLocationSelect] teamId ${activity.teamId} not found in teams for project ${project.projectId}`);
            activity.teamId = null; // Clear invalid teamId
          }
        } else if (activity.clientTeamList.length === 1) {
          // Auto-select if only one team
          activity.teamId = Number(activity.clientTeamList[0].teamId);
        }
      });

      // Load activities for all unique teamIds (avoid duplicate API calls)
      const uniqueTeamIds = new Set<number>();
      project.activities.forEach(activity => {
        if (activity.teamId) {
          uniqueTeamIds.add(activity.teamId);
        }
      });

      // If no activities have teamId but there's only one team, use that team
      if (uniqueTeamIds.size === 0 && teams.length === 1) {
        const singleTeamId = Number(teams[0].teamId);
        project.activities.forEach(activity => {
          activity.teamId = singleTeamId;
        });
        uniqueTeamIds.add(singleTeamId);
      }

      // Load activities for each unique teamId
      uniqueTeamIds.forEach(teamId => {
        this.onProjectTeamSelect(teamId, project);
      });
    } else {
      console.warn('Client details not available for project when selecting client location');
    }
  }

  /**
   * Handle team selection at project level - load activities for the project
   */
  onProjectTeamSelect(teamId: number | null | undefined, project: ProjectEntry): void {
    // ✅ MODERATE FIX: Add input validation
    if (!teamId || teamId <= 0) {
      console.warn('Invalid teamId:', teamId);
      return;
    }
    
    if (!project || !project.projectId) {
      console.warn('Invalid project for team selection');
      return;
    }
    
    // Load activities for the project
    this.loadActivitiesForProject(project, teamId);
  }

  onClientApprovalStatusSelect(location: LocationEntry, project: ProjectEntry, status: any): void {
    // ✅ MODERATE FIX: Add input validation
    if (!project) {
      console.warn('Project is required for approval status selection');
      return;
    }
    
    // Normalise status value (statusId from dropdown)
    const newStatus = status != null ? Number(status) : null;

    if (!newStatus) {
      project.clientApprovalStatus = null;
      project.projectActivities = [];
      if (project.activities && Array.isArray(project.activities)) {
        project.activities.forEach(activity => {
          activity.activityId = null;
        });
      }
      // ✅ Update document list when approval status is cleared
      this.getListToRenderUpload();
      return;
    }
    
    // Apply the selected status to the current project row
    project.clientApprovalStatus = newStatus;

    // ✅ Enforce consistency: same project (projectId) must have same clientApprovalStatus across all locations
    // Applies only when:
    // - Day type is fillable (working / half-day working etc.)
    // - Project has clientSideId
    // - Not shadow for self
    if (this.isDayTypeFillable() && project.hasClientSideId && project.projectId != null) {
      const targetProjectId = Number(project.projectId);
      let hasConflict = false;

      if (Array.isArray(this.timesheetLocations)) {
        for (const loc of this.timesheetLocations) {
          if (!loc || !Array.isArray(loc.projects)) {
            continue;
          }
          for (const p of loc.projects) {
            if (!p || p === project) {
              continue;
            }
            if (
              p.projectId != null &&
              Number(p.projectId) === targetProjectId &&
              (p.hasClientSideId ?? false) &&
              p.clientApprovalStatus != null &&
              Number(p.clientApprovalStatus) !== newStatus
            ) {
              hasConflict = true;
              break;
            }
          }
          if (hasConflict) {
            break;
          }
        }
      }
      console.log(this.timesheetLocations,"ClientApprovalStatusChange")
      if (hasConflict) {
        const message =
          'For the same project, client approval status must be consistent across all locations. ' +
          'The latest selected status will be applied to all locations for this project.';

        this.openAlertNoBackdrop(this.alertTemplate, message, () => {
          // On OK, apply the latest status to all occurrences of this project across locations
          if (!Array.isArray(this.timesheetLocations)) {
            return;
          }
          for (const loc of this.timesheetLocations) {
            if (!loc || !Array.isArray(loc.projects)) {
              continue;
            }
            for (const p of loc.projects) {
              if (
                p &&
                p.projectId != null &&
                Number(p.projectId) === targetProjectId &&
                (p.hasClientSideId ?? false)
              ) {
                p.clientApprovalStatus = newStatus;
              }
            }
          }
          // Recompute document upload list after status harmonisation
          this.getListToRenderUpload();
        });
      }
    }

    // ✅ Update document list when approval status changes (affects which documents to show)
    this.getListToRenderUpload();
  }

  /**
   * Load activities for a project filtered by department
   */
   loadActivitiesForProject(project: ProjectEntry, teamId: number): void {
    // ✅ MODERATE FIX: Add input validation
    if (!project) {
      this.handleError(new Error('Project is required'), 'loadActivitiesForProject', false);
      return;
    }
    
    if (!project.projectId) {
      this.handleError(new Error('Project ID is required'), 'loadActivitiesForProject', false);
      return;
    }
    
    if (!teamId || teamId <= 0) {
      this.handleError(new Error('Valid team ID is required'), 'loadActivitiesForProject', false);
      return;
    }
    
    if (!project.clientId || !project.clientLocationId) {
      this.handleError(
        new Error('Client and location required'),
        'loadActivitiesForProject',
        false
      );
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
      .pipe(first(), takeUntil(this.destroy$))
      .subscribe({
        next: async(response: any) => {
          if (response.serviceStatus == "Success") {
            let allActivityList = response.serviceResponse || [];
            if (allActivityList.length === 0) {
              this.handleError(
                new Error('No activities found'),
                'loadActivitiesForProject',
                true,
                "No activity found for your department!"
              );
              this.disableAdd = true;
            } else {
              allActivityList = allActivityList.sort((a: any, b: any) => a.activity?.localeCompare(b.activity) || 0);
              let employeeTeamDeptId =[];
              const empId = this.timesheetFilledForUser?.empId ?? this.currentUser?.empId;
              const date = this.convertDdMmYyyyToIso( this.fromDate);
              const formattedDate = this.datePipe.transform(
                date,
                'yyyy-MM-dd'
              );
              // this.timesheetService.getEmployeeTeamDepartmentId(timesheetObj.teamId,empId,formattedDate).subscribe({

              //   next: (response: any) => {
              //     if (response.serviceStatus == "Success") {
              //       employeeTeamDeptId = response.serviceResponse || [];
              //     }
              //   },
              //   error: (error) => {
              //     this.handleError(error, 'loadEmployeeTeamDeptId', true, 'Error loading Department Id.');
              //      allActivityList = [];
              //   }
              // });
              try{
                const deptResponse: any = await firstValueFrom(
                  this.timesheetService.getEmployeeTeamDepartmentId(
                    timesheetObj.teamId,
                    empId,
                    formattedDate
                  )
                );

                if (deptResponse.serviceStatus === "Success") {
                  employeeTeamDeptId = deptResponse.serviceResponse || [];
                }
                if(employeeTeamDeptId?.length == 0){

                  this.handleError(
                    new Error('In employee team mapping table we can not find any department id mapped to you.'),
                    'getEmployeeTeamDepartmentId',
                    true,
                    "You are not mapped to a specific department in the team!"
                  );
                  this.disableAdd = true;
                }
              
              }catch(error){
                this.handleError(error, 'loadEmployeeTeamDeptId', true, 'Error loading Department Id.');
                allActivityList = [];
              }

              if(employeeTeamDeptId == null || employeeTeamDeptId?.length == 0 ){
                allActivityList = [];
              }
              // Filter by department
              if (this.timesheetAppliedFor == "team") {
                // const teamMember = this.teamMemberList?.find(emp => emp.empId == this.timesheetFilledForUser?.empId);

                if (employeeTeamDeptId?.length > 0) {
                  allActivityList = allActivityList.filter((x: any) =>
                    x.departmentList?.some((d: any) => employeeTeamDeptId.includes(+d))
                  );
                }
                // if (teamMember?.departmentId) {
                //   allActivityList = allActivityList.filter((x: any) =>
                //     x.departmentList?.map((d: any) => +d).includes(teamMember.departmentId)
                //   );
                // }
               
              } else if (this.timesheetAppliedFor == "self") {

                // if (this.currentUser?.departmentId) {
                //   allActivityList = allActivityList.filter((x: any) =>
                //     x.departmentList?.map((d: any) => +d).includes(this.currentUser.departmentId)
                //   );
                // }

                if (employeeTeamDeptId?.length > 0) {
                    allActivityList = allActivityList.filter((x: any) =>
                      x.departmentList?.some((d: any) => employeeTeamDeptId.includes(+d))
                    );
                  }
              }
            }

            // Store activities at project level for all activities to use
            this.timesheetLocations.forEach(loc => {
              loc.projects.forEach(proj => {
                if (proj === project) {
                  if (proj.activities && Array.isArray(proj.activities)) {
                    proj.activities.forEach(activity => {
                      activity.allActivitiesForProject = allActivityList;
                      // Ensure activityId matches type after list loads (for Angular binding)
                      if (activity.activityId != null && allActivityList.length > 0) {
                        const activityIdNum = Number(activity.activityId);
                        const matchedActivity = allActivityList.find(a => {
                          const aIdNum = Number(a.activityId);
                          return aIdNum === activityIdNum;
                        });
                        if (matchedActivity) {
                          activity.activityId = activityIdNum;
                          console.log(`[loadActivitiesForProject] Matched activityId ${activity.activityId} for project ${project.projectId}, team ${teamId}`);
                        } else {
                          console.warn(`[loadActivitiesForProject] activityId ${activity.activityId} (normalized: ${activityIdNum}) not found in allActivitiesForProject for project ${project.projectId}, team ${teamId}. Available IDs:`, allActivityList.map(a => Number(a.activityId)));
                        }
                      } else if (activity.activityId != null) {
                        console.warn(`[loadActivitiesForProject] activityId ${activity.activityId} set but allActivitiesForProject is empty for project ${project.projectId}, team ${teamId}`);
                      }
                    });
                  }
                }
              });
            });
            project.projectActivities = allActivityList;
          } else {
            this.handleError(
              new Error(response.serviceResponse || 'Failed to load activities'),
              'loadActivitiesForProject',
              true,
              'Failed to load activities. Please try again.'
            );
            project.projectActivities = [];
          }
        },
        error: (error) => {
          this.handleError(error, 'loadActivitiesForProject', true, 'Error loading activities. Please try again.');
          project.projectActivities = [];
        }
      });
  }

  /**
   * Load server date for date range calculation.
   * Used in update mode; in create mode use loadServerDateThenInitCreate() so init runs after serverDate is set.
   */
  loadServerDate(): void {
    this.timesheetService.getServerDate()
      .pipe(first(), takeUntil(this.destroy$))
      .subscribe((response: any) => {
        this.serverDate = response;
        // After server date is loaded, calculate constraints if employee is known
        if (this.timesheetFilledForUser?.empId || this.currentUser?.empId) {
          const empId = this.timesheetFilledForUser?.empId || this.currentUser.empId;
          let employeeObj = new User();
          employeeObj = this.teamMemberList?.find(m => m.empId === empId || Number(m.empId) === Number(empId))
          // this.getTimesheetMetadata();
          this.getAllAvailableTimesheetByEmpId(employeeObj);
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
// this.selectedTeamMember
    if ((this.timesheetAppliedFor == "self" && this.currentUser.isTimesheetLockCheckEnable == 'false') ||
    (this.timesheetAppliedFor == "team" && this.selectedTeamMember?.isTimesheetLockCheckEnable == 'false')) {
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

    // Use lightweight metadata API (no locations/projects/activities) for date picker constraints
    this.timesheetNewService.getTimesheetMetadataByEmpId(timesheetObj)
      .pipe(first(), takeUntil(this.destroy$))
      .subscribe((response: any) => {
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
      let dateOfJoining = moment(this.timesheetAppliedFor=='self' ? this.currentUser.dateOfJoining : this.selectedTeamMember.dateOfJoining, dateFormat);

      daysDifference = moment(currentDate, dateFormat).diff(dateOfJoining, 'days');
    }

    if (this.currentUser.timesheetBackDatedDays > daysDifference){
      OPEN_BACKDATED_DAYS = daysDifference;
    } else {
      OPEN_BACKDATED_DAYS = this.currentUser.timesheetBackDatedDays || 30;
    }

    // Calculate end date (server date)
    const dateObj = new Date(this.serverDate + 'T23:59:59');
    let serverDate = dateObj;

    // Calculate start date based on lock check enable flag
    let startDate: Date;
    if ((this.timesheetAppliedFor == "self" && this.currentUser.isTimesheetLockCheckEnable == 'false') ||
    (this.timesheetAppliedFor == "team" && this.selectedTeamMember?.isTimesheetLockCheckEnable == 'false')) {
      startDate = new Date(serverDate.getTime() - ((OPEN_BACKDATED_DAYS + CURRENT_DAY) * DAY_IN_MS));
    } else {
       const lockDays = this.currentUser.timesheetLockDays || 7; // Default to 30 if not set
      startDate = new Date(serverDate.getTime() - ((lockDays + CURRENT_DAY) * DAY_IN_MS));
    }

    // Set min/max dates for picker (convert to dd-MM-yyyy format)
    this.minDateForPicker = moment(startDate).format('DD-MM-YYYY');
    this.maxDateForPicker = moment(serverDate).format('DD-MM-YYYY');

    console.log("minDateForPicker===> ",this.minDateForPicker);
    console.log("maxDateForPicker===> ",this.maxDateForPicker);
    console.log("disabledDatesForPicker===> ",this.disabledDatesForPicker);
    console.log("availableTimesheets count===> ",this.availableTimesheets.length);

    // Clear and rebuild the map of existing timesheet dayTypes
    this.existingTimesheetDayTypes.clear();

    // Extract dates from availableTimesheets to disable
    // Handle update mode: exclude current timesheet date
    this.disabledDatesBase = this.availableTimesheets
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
        
        // Store the dayTypeId for this date (used to identify cron-filled timesheets)
        if (dateStr && dateStr !== 'Invalid date' && dateStr !== 'Invalid Date' && ts.dayTypeId != null) {
          this.existingTimesheetDayTypes.set(dateStr, ts.dayTypeId);
        }
        
        return dateStr;
      })
      .filter((date: string) => {
        // Remove invalid dates and ensure format is correct
        return date !== 'Invalid date' && 
               date !== 'Invalid Date' && 
               moment(date, 'DD-MM-YYYY', true).isValid();
      });

    // Apply day-type-specific rules (e.g. Non-working → only holidays/week-offs)
    this.recalculateDisabledDatesForPicker();
  }

  /**
   * Handle night shift toggle change
   * For night shift: toDate can be same as fromDate OR exactly one day after fromDate
   * Auto-sets toDate to next day as default, but user can change it to same date
   */
  onNightShiftChange(): void {

    if (this.isNightShift) {

    this.openAlertMod(this.night_shift_template,
      'Do you have required permission for night shift from your manager?');
    return;

  }

  this.toDate = null;
  this.resetFormForNightShift();
  setTimeout(() => {
    this.calculateTotalWorkingHours();
    this.onHoursChange();
  }, 0);
}
  confirmNightShift(): void {
    this.closeAlertModal();
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

      // Auto-set toDate to next day as default (user can change to same date if needed)
      if (!this.toDate) {
        this.toDate = this.formatDDMMYYYY(this.addDays(fromDate, 1));
      } else {
        // Validate existing toDate: must be same as fromDate OR exactly one day after
        const toDateParsed = this.parseDDMMYYYY(this.toDate);
        if (toDateParsed) {
          const diffDays = Math.round((toDateParsed.getTime() - fromDate.getTime()) / (1000 * 60 * 60 * 24));
          if (diffDays !== 0 && diffDays !== 1) {
            // Invalid: reset to next day
            this.toDate = this.formatDDMMYYYY(this.addDays(fromDate, 1));
          }
        }
      }
      this.resetFormForNightShift();

    // Recalculate total presence and location hours when night shift changes
    // (toDate affects both: presence uses toDate for out-time; location hours use toDate for end date).
    // Defer so ngModel has updated isNightShift before we read it.
    setTimeout(() => {
      this.calculateTotalWorkingHours();
      this.onHoursChange();
    }, 0);
  }
  
  cancelNightShift() {

  this.isNightShift = false;

  this.closeAlertModal(); // close modal
  }


  /** Date changed: reset date-dependent form state and reload for new date. */
  onFromDateChange(): void {
    if (!this.fromDate) {
      this.totalPresence = 0;
      return;
    }
    if (this.isNightShift) {
      const fromDate = this.parseDDMMYYYY(this.fromDate);
      if (!fromDate) return;
      this.toDate = this.formatDDMMYYYY(this.addDays(fromDate, 1));
    }
    if(this.halfDayValidation()){
      return;
    };
    this.applyChanges();
  }

  /** Apply date change: clear date-dependent form state, recalc hours, load projects. */
  applyChanges(): void {
    this.resetDateDependentFormState();
    this.clearAndInitOnDayTypeChange(this.isDayTypeFillable());
    this.calculateTotalWorkingHours();
    this.getProjectListForDateAndEmpId().catch(error => {
      console.error('Error loading projects:', error);
    });
  }

  /**
   * Handle to date change
   * For night shift: validates toDate is same as fromDate OR exactly one day after fromDate
   * In-time points to fromDate, out-time points to toDate
   */
  onToDateChange(): void {
    if (!this.fromDate) {
      this.openAlertMod(this.alertTemplate, 'Please select From Date first.');
      this.toDate = null;
      this.calculateTotalWorkingHours();
      this.onHoursChange();
      return;
    }

    // If not night shift, clear toDate and return
    if (!this.isNightShift) {
      this.toDate = null;
      this.calculateTotalWorkingHours();
      this.onHoursChange();
      return;
    }

    const fromDate = this.parseDDMMYYYY(this.fromDate);
    const toDate = this.parseDDMMYYYY(this.toDate);

    if (!fromDate || !toDate) {
      this.openAlertMod(this.alertTemplate, 'Invalid date format.');
      this.toDate = null;
      this.calculateTotalWorkingHours();
      this.onHoursChange();
      return;
    }

    // Calculate difference in days (rounded to handle timezone/rounding issues)
    const diffDays = Math.round((toDate.getTime() - fromDate.getTime()) / (1000 * 60 * 60 * 24));

    // For night shift: toDate must be same (0) OR exactly one day after (1)
    if (diffDays !== 0 && diffDays !== 1) {
      this.openAlertMod(
        this.alertTemplate,
        'For Night Shift, To Date must be the same as From Date OR exactly one day after From Date.'
      );
      // Reset to next day as default
      this.toDate = this.formatDDMMYYYY(this.addDays(fromDate, 1));
      this.calculateTotalWorkingHours();
      this.onHoursChange();
      return;
    }

    // Valid date: validate work check times (especially when dates become same)
    // If fromDate === toDate, out-time must be greater than in-time. No reset on failure.
    if (!this.validateWorkCheckTimes()) {
      this.calculateTotalWorkingHours();
      this.onHoursChange();
      return;
    }

    // Valid: recalculate presence and location hours (toDate affects both)
    this.calculateTotalWorkingHours();
    this.onHoursChange();
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
   * Max date for To Date picker (night shift): exactly one day after fromDate.
   * Used so To Date can only be fromDate or fromDate+1.
   */
  get maxDateForToDatePicker(): string | null {
    if (!this.fromDate) return null;
    const from = this.parseDDMMYYYY(this.fromDate);
    if (!from) return null;
    return this.formatDDMMYYYY(this.addDays(from, 1));
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

      // NIGHT SHIFT LOGIC:
      // 1. In-time always points to fromDate
      // 2. Out-time points to toDate (if toDate exists and is night shift)
      // 3. toDate can be same as fromDate OR exactly one day after fromDate
      // NORMAL SHIFT LOGIC:
      // 1. Both in-time and out-time always point to fromDate (no +1 day logic)
      const isNightShift = this.isNightShift;
      let outDateForCalculation: Date;

      if (isNightShift && this.toDate) {
        // Night shift with toDate: out-time points to toDate
        const toDateParsed = this.parseDDMMYYYY(this.toDate);
        if (!toDateParsed) {
          this.totalPresence = 0;
          return;
        }
        outDateForCalculation = toDateParsed;
      } else {
        // Normal shift OR night shift without toDate: both times on fromDate
        outDateForCalculation = fromDateParsed;
      }

      // Create Date objects with date and time (24-hour clock: 00:00–23:59)
      // In-time always on fromDate
      const inDateTime = new Date(fromDateParsed);
      inDateTime.setHours(inHour, inMinute, 0, 0);

      // Out-time: on toDate for night shift (if toDate exists), otherwise on fromDate
      const outDateTime = new Date(outDateForCalculation);
      outDateTime.setHours(outHour, outMinute, 0, 0);

      // Calculate difference in milliseconds
      const diffMs = outDateTime.getTime() - inDateTime.getTime();
      const calculatedHours = diffMs / (1000 * 60 * 60);

      // Do NOT cap at 24: night shift (fromDate to toDate+1) can yield the same value as
      // location.totalWorkingHours (e.g. 33). Capping totalPresence at 24 while location
      // hours stay uncapped causes false "location hours exceed presence" validation errors.
      const finalHours = Math.max(0, calculatedHours);

      // Round to 2 decimal places
      this.totalPresence = Math.round(finalHours * 100) / 100;
      console.log('total presence',this.totalPresence);
      this.onHoursChange();
    } catch (error) {
      this.totalPresence = 0;
    }
  }

  /**
   * Validate that work-check-out time is greater than work-check-in time.
   * Applies for: normal days AND night shift when fromDate === toDate.
   * Runs on every in/out time change (including hour, minute, AM/PM).
   * Does NOT reset in/out times on failure so user can correct (e.g. change AM to PM) without re-entering.
   */
  private validateWorkCheckTimes(): boolean {
    if (!this.apmosysInTime || !this.apmosysOutTime) {
      return true; // Skip validation if either time is not set
    }

    // For night shift: only validate if fromDate === toDate (same date)
    // For normal shift: always validate (both times on same date)
    let shouldValidate = true;
    if (this.isNightShift && this.toDate && this.fromDate) {
      const fromDateParsed = this.parseDDMMYYYY(this.fromDate);
      const toDateParsed = this.parseDDMMYYYY(this.toDate);
      if (fromDateParsed && toDateParsed) {
        const diffDays = Math.round((toDateParsed.getTime() - fromDateParsed.getTime()) / (1000 * 60 * 60 * 24));
        if (diffDays !== 0) {
          // Night shift with different dates: skip validation (out time is on next day)
          shouldValidate = false;
        }
      }
    }

    if (!shouldValidate) {
      return true; // Skip validation for night shift with different dates
    }

    // Parse times to 24-hour format for comparison
    const inTime24 = this.parseTimeTo24Hour(this.apmosysInTime);
    const outTime24 = this.parseTimeTo24Hour(this.apmosysOutTime);

    if (!inTime24 || !outTime24) {
      return true; // Skip if parsing fails
    }

    // Compare times: out must be greater than in
    const inTotalMinutes = inTime24.hour * 60 + inTime24.minute;
    const outTotalMinutes = outTime24.hour * 60 + outTime24.minute;

    if (outTotalMinutes <= inTotalMinutes) {
      this.openAlertMod(
        this.alertTemplate,
        'Work Check-Out time must be greater than Work Check-In time.'
      );
      // Do not reset in/out times - keep values so user can correct (e.g. change AM to PM)
      return false;
    }

    return true;
  }

  /**
   * Validate that location out-time is greater than location in-time
   * Applies for: normal days AND night shift when fromDate === toDate
   * Does NOT reset times - only shows alert
   */
  private validateLocationTimes(location: LocationEntry): boolean {
    if (!location?.locationInTime || !location?.locationOutTime) {
      return true; // Skip validation if either time is not set
    }

    // For night shift: only validate if fromDate === toDate (same date)
    // For normal shift: always validate (both times on same date)
    let shouldValidate = true;
    if (this.isNightShift && this.toDate && this.fromDate) {
      const fromDateParsed = this.parseDDMMYYYY(this.fromDate);
      const toDateParsed = this.parseDDMMYYYY(this.toDate);
      if (fromDateParsed && toDateParsed) {
        const diffDays = Math.round((toDateParsed.getTime() - fromDateParsed.getTime()) / (1000 * 60 * 60 * 24));
        if (diffDays !== 0) {
          // Night shift with different dates: skip validation (out time is on next day)
          shouldValidate = false;
        }
      }
    }

    if (!shouldValidate) {
      return true; // Skip validation for night shift with different dates
    }

    // Parse times to 24-hour format for comparison
    const inTime24 = this.parseTimeTo24Hour(location.locationInTime);
    const outTime24 = this.parseTimeTo24Hour(location.locationOutTime);

    if (!inTime24 || !outTime24) {
      return true; // Skip if parsing fails
    }

    // Compare times: out must be greater than in
    const inTotalMinutes = inTime24.hour * 60 + inTime24.minute;
    const outTotalMinutes = outTime24.hour * 60 + outTime24.minute;

    if (outTotalMinutes <= inTotalMinutes) {
      const locationCode = this.workLocationList.find(
        l => l.workLocationTypeId === location.workLocationTypeId
      )?.code || 'this location';
      
      this.openAlertMod(
        this.alertTemplate,
        `Log-Out time for ${locationCode} must be greater than Log-In time.`
      );
      
      return false;
    }

    return true;
  }

  /**
   * Handle ApMoSys In Time change
   * Updates location in-time if using ApMoSys timing
   */
  onApMoSysInTimeChange(time: string): void {
    this.apmosysInTime = time;
    console.log('apmosysInTime ===> ',this.apmosysInTime)
    if (this.useApmosysTiming) {
      this.timesheetLocations[0].locationInTime = this.apmosysInTime;
    }
    
    // Validate: out time must be greater than in time (on every change including AM/PM)
    // On failure we show alert only; in/out times are kept so user can correct without re-entering
    if (!this.validateWorkCheckTimes()) {
      this.calculateTotalWorkingHours();
      return;
    }
    
    this.calculateTotalWorkingHours();
  }

  /**
   * Handle ApMoSys Out Time change (value updated from picker).
   * Updates location out-time if using ApMoSys timing. Validation popup runs only on AM/PM change (see onOutTimePartChange).
   */
  onApMoSysOutTimeChange(time: string): void {
    this.apmosysOutTime = time;
    console.log('apmosysOutTime ===> ',this.apmosysOutTime)
    if (this.useApmosysTiming) {
      this.timesheetLocations[0].locationOutTime = this.apmosysOutTime;
    }
    this.calculateTotalWorkingHours();
  }

  /**
   * Called when user changes a part of Work Check-Out time (hour, minute, or AM/PM).
   * Show "out must be greater than in" popup only when AM/PM is changed, so we don't show it
   * as soon as HH/MM are selected (default AM would trigger the error before user picks PM).
   */
  onOutTimePartChange(part: 'hour' | 'minute' | 'ampm'): void {
    if (part === 'ampm') {
      if (!this.validateWorkCheckTimes()) {
        this.calculateTotalWorkingHours();
        return;
      }
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
  fileTracker = new Map<string, string>(); 

  async onFileSelected(
    event: any,
    docType: 'Filled' | 'Approved',
    projectId: number
  ): Promise<void> {

    const file: File = event.target.files?.[0];
    if (!file) return;

    const allowedTypes = ['application/pdf', 
    'image/jpeg', 'image/png', 'image/jpg',
    'application/vnd.ms-excel',                                    
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'];
    const maxSize = 500 * 1024; // 500KB

    // ❌ Invalid type
    if (!allowedTypes.includes(file.type)) {
      this.handleFileError(
        projectId,
        docType,
        'Only PDF, JPG, JPEG, PNG, XLS, XLSX files allowed.'
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

    console.log("Document data",this.documentData);

    // ♻️ Cleanup old object URL
    const previous = this.documentData.find(
      f => f.projectId === projectId && f.docType === docType
    );

    if (previous?.rawObjectUrl) {
      URL.revokeObjectURL(previous.rawObjectUrl);
    }
    const excelTypes = [
      'application/vnd.ms-excel',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    ];
    // ✅ Detect file type ONCE
    const fileType: 'pdf' | 'image' | 'excel' =
      file.type === 'application/pdf' ? 'pdf' :
      excelTypes.includes(file.type) ? 'excel' : 'image';

    // ✅ Create object URL
    const objectUrl = URL.createObjectURL(file);

    // ✅ IMPORTANT: Use correct sanitizer
    const previewUrl =
      fileType === 'pdf'
        ? this.sanitizer.bypassSecurityTrustResourceUrl(objectUrl)
        : fileType === 'excel'
        ? null   // No browser preview for Excel
        : this.sanitizer.bypassSecurityTrustUrl(objectUrl);

    const uniqueFile: File = await this.renameFile(file, projectId, docType);
    if(uniqueFile == null ){
      return;
    }
    const uniqueIdentifier = uniqueFile.name;
    
    


    // Update entry – preserve docId when replacing so backend updates existing row instead of creating new
    this.updateUploadFile({
      docId: previous?.docId ?? null,
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
   
    let key = this.generateFileKey(data.projectId,this.fromDate,this.dayType,data.docType);

    let oldUniqueIdentifier = this.fileTracker.get(key);

    const index = this.documentData.findIndex(
      f => f.projectId === data.projectId && f.docType === data.docType
    );

    if (index === -1) return;

    this.documentData[index] = {
      ...this.documentData[index],
      ...data
    };

    const getBaseName = (fileName: string) => {
      return fileName.substring(0, fileName.lastIndexOf('.'));
    };

    // ✅ Keep selectedFile array in sync without introducing undefined entries
    // Only update selectedFile when we actually have a File object
    if (file) {

      if(oldUniqueIdentifier){
        const existingIndex = this.selectedFile.findIndex(
          // f => f && getBaseName(f.name) === getBaseName(data.uniqueIdentifier)
          f => f && getBaseName(f.name) === getBaseName(oldUniqueIdentifier)
        );
        if (existingIndex !== -1) {
          // Replace existing file for this document
          this.selectedFile[existingIndex] = file;
          this.fileTracker.set(key,data.uniqueIdentifier);
        }
      }
      else {
        // Add new file entry
        this.selectedFile.push(file);
        this.fileTracker.set(key,data.uniqueIdentifier);

      }
    }

    console.log("File tracker",this.fileTracker);
    console.log("Selected File",this.selectedFile);

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
   * Handles: new uploads, replaced files (local preview), and existing docs (fetch by docId)
   * Prefer local preview when available so that after user replaces a file we show the new file, not the old one from backend.
   */
  openPreviewModalForTwo(file: any): void {
    // Prefer local file: new upload or replaced file (user picked a new file → we have previewUrl/rawObjectUrl)
    if (file?.previewUrl && file?.fileType) {
    this.activePreviewUrl = file.previewUrl;
    this.activeFileType = file.fileType;
    this.activeRawObjectUrl = file.rawObjectUrl;
    this.resetTransformations();
    this.modalRef = this.modalService.open(this.previewModal, {
      modalDialogClass: 'modal-lg',
      scrollable: true
    });
      return;
    }

    // No local file: existing document only (or not yet uploaded) → fetch from backend by docId
    if (file?.docId && this.isExistingDocument(file)) {
      this.previewExistingDocument(file);
      return;
    }

    this.openAlertMod(
      this.alertTemplate,
      'Document preview is not available. Please upload the document first.'
    );
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
  async renameFile(file: File, projectId: number, docType: 'Filled' | 'Approved'): Promise<File> {
  
    try{
    const ext = file.name.includes('.') ?file.name.substring(file.name.lastIndexOf('.')): '';
    // const safeDocType = docType.toLowerCase(); // optional
    // // const newFileName = `${this.currentUser.empId}_${projectId}_${}_${safeDocType}${ext}`;
    // const newFileName = `${projectId}_${this.fromDate}_${this.dayType}_${safeDocType}${ext}`;
    

    const response: any = await firstValueFrom(this.timesheetService.generateFileName({
      projectId: projectId,
      extension: ext,
      docType: docType
    }));
    const newFileName = response.fileName;

    return new File([file], newFileName, { type: file.type });
  }
   catch(error){
    this.handleError(error,"Generating unique file name",true,"Unable to generate unique file name")
    return null;
  }

  }

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
  resetForm(label ?: any): void {
    // Basic fields
    if(label != 'dayType'){
      this.dayType = null;
    }
    if(!this.isUpdation){
      this.fromDate = null;
      this.toDate = null;
    }
    this.appelectMember = null;
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
    this.fileTracker.clear();
    // UI state
    this.highlightLocationList = [];
    this.highlightLocationIdSet = new Set();
    this.expandedLocationIndex = null;
    this.expandedProjectIndexMap = {};
    
    // Preview state
    this.resetPreviewState();
    
    // Flags
    this.disableAdd = false;
    this.fileTracker.clear();
  }

  resetFormForNightShift(): void {
    // Basic fields
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
    this.fileTracker.clear();
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
   * Reset only date-dependent form state (locations, projects, activities, documents, in/out times).
   * Used when user changes the date so that project selections valid for the previous date
   * are not submitted for the new date. Does not clear dayType, fromDate, or toDate.
   */
  private resetDateDependentFormState(): void {
    // Locations and their projects/activities
    this.timesheetLocations = [];
    
    // Only add location if day type is fillable (addLocation returns early for non-fillable)
    if (this.isDayTypeFillable()) {
      this.addLocation(null);
    }

    // Document data cleanup and revoke object URLs
    this.cleanupDocumentData();
    this.documentData = [];
    this.selectedFile = [];
    this.uniqueProjectsList = [];
    this.empHasClientSideId = false;
    this.fileTracker.clear();
    // In/out times and presence (clean slate for new date)
    this.apmosysInTime = null;
    this.apmosysOutTime = null;
    this.totalPresence = 0;

    // Location-related UI state
    this.highlightLocationList = [];
    this.highlightLocationIdSet = new Set();
    this.expandedLocationIndex = null;
    this.expandedProjectIndexMap = {};

    // Preview state
    this.resetPreviewState();

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

  getLastFilledLocationIdForProjectAndEmp(projectId: number, empId: number) {
  return this.timesheetNewService
    .getLastFilledLocationIdForProjectAndEmp(projectId, empId)
    .pipe(
      map((response: any) => {
        if (response && response.serviceResponse) {
          return response.serviceResponse as number;
        }
        return null;
      }),
      catchError((error) => {
        console.error('Error fetching last filled location ID:', error);
        return of(null);
      })
    );
}
 getDeptBaseProjectData(): void {

  const empId = this.timesheetAppliedFor?.toLowerCase() === 'self'
    ? this.currentUser?.empId
    : this.timesheetFilledForUser?.empId;

  this.timesheetNewService
    .fetchDeptBaseProjectAndClientRelatedDataForEmployee(empId)
    .subscribe({
      next: (response: any) => {

        if (response?.serviceStatus === "Success" && response?.serviceResponse) {

          const data = response.serviceResponse;

          this.timesheetLocations.forEach(loc => {
           const project = this.createProject(null, null);
            project.projectId = data.projectId;
            project.clientId = data.clientId;
            project.clientLocationId = data.clientLocationId;
            project.description = this.holidayDescription;
            loc.projects.push(project);
          });
          this.noProjectEmployee = true;
          if(this.isUpdation){
            this.updateTimesheet();
          }else{
            this.createTimesheet();
          }
          
        } else {
          this.openAlertMod(this.alertTemplate, 'Cannot create timesheet as we could not find any Bench project for you. Please contact your Reporting Manager immediately.');
        }
      },

      error: (error) => {
        console.error('Error fetching dept base project data:', error);
        this.openAlertMod(this.alertTemplate, 'An error occurred while fetching project data. Please try again later or contact support if the issue persists.');
      }
    });
}
async prepareDataForNonWorkingDay(): Promise<boolean> {

  if (this.holidayDescription == '' || this.holidayDescription == null) {
    this.openAlertMod(this.alertTemplate, 'Description is mandatory for non-working day types.');
    return false;
  }

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

  if (uniqueProjects.length === 0) {
    this.openAlertModMessageForHolidayCreate(
      this.alertMessageForHolidayCreateTemplate,
      'No projects available. Your timesheet will be filled for Bench Project of your Department.'
    );
    return false;
  }

  const empId = this.timesheetAppliedFor?.toLowerCase() === 'self'
    ? this.currentUser?.empId
    : this.timesheetFilledForUser?.empId;
  await Promise.all(
    this.timesheetLocations.map(async (loc) => {

      const projectObservables = uniqueProjects.map(p => {

        const project = this.createProject(null, null);
        project.projectId = p.projectId;
        project.projectName = p.projectName;
        project.hasClientSideId = p.hasClientSideId;
        project.hasClientFlag = p.hasClientFlag;

        this.populateProjectDropdowns(project);

        // ✅ correct mapping
        project.clientId = project.clientDetails?.clientId ?? null;

        return this.getLastFilledLocationIdForProjectAndEmp(project.projectId, empId).pipe(
          map(locationId => {
            project.clientLocationId =
              locationId ?? project.clientDetails.clientLocations?.[0]?.clientLocationId;

            project.description = this.holidayDescription;

            return project;
          }),
          catchError(() => {
            project.clientLocationId = project.clientLocationList?.[0]?.clientLocationId;
            project.description = this.holidayDescription;
            return of(project);
          })
        );
      });

      // 🔥 HARD BLOCK here (no subscribe anywhere)
      loc.projects = await firstValueFrom(forkJoin(projectObservables));
      loc.projects = loc.projects?.filter(p => (p.clientId !== null && p.clientId !== undefined) && (p.clientLocationId !== null && p.clientLocationId !== undefined));
      if(this.dayType == 7){
        loc.projects = loc.projects?.filter(p => p.hasClientSideId == true)
      }else if (this.dayType == 6){
        loc.projects = loc.projects?.filter(p => p.hasClientSideId != true)
      }
    })
  );
  
  console.log('✅ All locations fully populated:', this.timesheetLocations);
  return true;
}

  async createTimesheet() {
    if(!this.isDayTypeFillable() && !this.noProjectEmployee){
     const successFlag =  await this.prepareDataForNonWorkingDay();
     if(successFlag){
       if(this.timesheetLocations[0].projects.length ==0){
        if(this.dayType == 7){
          this.openAlertMod(this.alertTemplate, 'Cannot create timesheet as there are no valid Client projects to assign for this day. Please contact your Reporting Manager immediately.');
        }else if(this.dayType == 6){
          this.openAlertMod(this.alertTemplate, 'Cannot create timesheet as there are no valid Internal/Bench projects to assign for this day. Please contact your Reporting Manager immediately.');
        }else{
          this.openAlertMod(this.alertTemplate, 'Cannot create timesheet as there are no valid projects to assign for this day. Please contact your Reporting Manager immediately.');
        }
        return 
       }
      }else{
        return; 
      }
    }
    this.highlightLocationList = [];
    
    // ✅ MODERATE FIX: Use centralized date conversion method
    const convertToYYYYMMDD = this.convertDDMMYYYYToYYYYMMDD.bind(this);

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
      // ✅ CRITICAL FIX: Use requestAnimationFrame instead of setTimeout for better timing
      requestAnimationFrame(() => {
        this.openAlertMod(this.alertTemplate, warningMessage);
      });
    }
    
    const dataSet: LocationEntry[] = structuredClone(this.timesheetLocations);
    // Ensure Shadow for self projects do not send client approval status (not required, dropdown hidden)
    // dataSet.forEach((loc) => loc.projects?.forEach((p) => { if (p.isShadowForSelf) p.clientApprovalStatus = null; }));
    
    // ✅ CRITICAL FIX: Add null checks for empId
    const targetEmpId = this.timesheetAppliedFor?.toLowerCase() === 'self' 
      ? this.currentUser?.empId 
      : (this.timesheetFilledForUser?.empId || this.currentUser?.empId);
    
    if (!targetEmpId) {
      this.openAlertMod(
        this.alertTemplate,
        'Employee ID not available. Please refresh and try again.'
      );
      return;
    }
    
    this.createOrUpdateObj = {
      createdBy: this.currentUser.empId,
      dayTypeId: this.dayType,
      empId: targetEmpId,
      isApmosysProduct: this.currentUser.isApmosysProduct,
      isNightShift: this.isNightShift,
      timesheetId: null,
      // date: this.formatDDMMYYYY(new Date(this.fromDate as string)),
      date: convertToYYYYMMDD(this.fromDate),
      workCheckIn: TimesheetFormComponent.NON_FILLABLE_DAY_TYPES.includes(this.dayType) ? null : this.formatDateTimeForBackend(this.apmosysInTime, convertToYYYYMMDD(this.fromDate)),
      workCheckOut: TimesheetFormComponent.NON_FILLABLE_DAY_TYPES.includes(this.dayType) ? null : this.formatDateTimeForBackend(this.apmosysOutTime, convertToYYYYMMDD(this.isNightShift ? this.toDate : this.fromDate)),
      currentManagerId: this.currentUser.managerId,
      totalWorkingMinutes: this.totalPresence * 60,
      locationSessions: dataSet,
      documentData: this.documentData
    }
    console.log('[createTimesheet] Prepared createOrUpdateObj:',this.createOrUpdateObj )
    // API expects durationMinutes in minutes; form stores hours
    this.convertActivityDurationsToMinutesForApi(this.createOrUpdateObj.locationSessions);
    const isNonFillable = !this.isDayTypeFillable();
    this.createOrUpdateObj.locationSessions.forEach((location: LocationEntry) => {
      location.locationInTime = isNonFillable ? null : this.formatDateTimeForBackend(location.locationInTime, convertToYYYYMMDD(this.fromDate));
      location.locationOutTime = isNonFillable ? null : this.formatDateTimeForBackend(location.locationOutTime, convertToYYYYMMDD(this.isNightShift ? this.toDate : this.fromDate));
      location.projects.forEach((project: ProjectEntry) => {
        if (isNonFillable) {
          project.activities = [];
        }
      });
    });

    // Note: generateTotalMinutes is calculated but not used in current implementation
    // Keeping for potential future use or backend validation
    // this.generateTotalMinutes(this.createOrUpdateObj);

    this.isLoadingTimesheet = true;
    this.timesheetNewService.createTimesheet(this.createOrUpdateObj, this.selectedFile)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.isLoadingTimesheet = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (response: any) => {
          if (response.serviceStatus === "Success") {
            
            // After successful create, refresh disabled dates so just-filled date becomes non-selectable
            if (targetEmpId) {
              this.getAllAvailableTimesheetByEmpId({ empId: targetEmpId } as User);
            }
            this.appelectMember = null;
            this.resetForm();
            this.onTimesheetAppliedForChange();
            setTimeout(() => {
              this.openAlertMod(this.alertTemplate, "Timesheet created successfully.");
            });
            console.log('this.isAutofillMode:', this.isAutofillMode);
            if(this.isAutofillMode){
              this.router.navigate(['/home']);
            }else{
              this.timesheetCreated.emit();
            }
            // Reset form or navigate as needed
          } else {
            // ✅ MODERATE FIX: Use centralized error handling
            const backendError =
              response?.serviceError ||
              response?.serviceResponse ||
              'Failed to create timesheet. Please try again.';
            this.handleError(
              new Error(backendError),
              'createTimesheet',
              true,
              backendError
            );
          }
        },
        error: (error) => {
          console.log("error ==> ",error);
          // Loader is stopped in finalize(); handle error message
          const backendError =
            error?.error?.serviceError ||
            error?.error?.serviceResponse ||
            error?.error?.message ||
            error?.error?.serviceStatus || 
            'An unexpected error occurred while creating the timesheet.';
          this.handleError(
            error,
            'createTimesheet',
            true,
            backendError
          );
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
      .pipe(
        first(),
        takeUntil(this.destroy$),
        finalize(() => (this.isLoadingTimesheet = false))
      )
      .subscribe({
        next: (response: any) => {
          if (response.serviceStatus === "Success") {
            const timesheetData: EmployeeTimesheetDTO = response.serviceResponse;
            console.log('[loadTimesheetForUpdate] Received timesheet data:', {
              timesheetId: timesheetData.timesheetId,
              date: timesheetData.date,
              locationSessionsCount: timesheetData.locationSessions?.length || 0,
              locationSessions: timesheetData.locationSessions
            });
            this.populateFormFromTimesheetData(timesheetData);
          } else {
            this.handleError(
              new Error(response.serviceResponse || 'Failed to load timesheet'),
              'loadTimesheetForUpdate',
              true,
              response.serviceResponse || 'Failed to load timesheet data. Please try again.'
            );
          }
        },
        error: (error) => {
          this.handleError(
            error,
            'loadTimesheetForUpdate',
            true,
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

    // For normal update flow we require a valid timesheetId.
    // In autofill mode we intentionally pass a template without ID.
    if (!timesheetData.timesheetId && !this.isAutofillMode) {
      this.openAlertMod(
        this.alertTemplate,
        'Invalid timesheet data. Missing timesheet ID.'
      );
      return;
    }

    // Store timesheet ID only for update mode (not for autofill templates)
    if (!this.isAutofillMode) {
    this.timesheetId = timesheetData.timesheetId;
    }

    // 1. Basic Fields
    this.dayType = timesheetData.dayTypeId;
    // Initialize lastDayTypeId for later revert-on-invalid-change behavior
    this.lastDayTypeId = this.dayType;
    if (timesheetData.date) {
      this.fromDate = this.convertYYYYMMDDToDDMMYYYY(timesheetData.date);
    }
    this.isNightShift = !!timesheetData.isNightShift;

    // Handle night shift toDate dynamically based on stored in/out datetimes
    this.toDate = null;
    if (this.isNightShift && timesheetData.date) {
      const fromDate = this.parseYYYYMMDD(timesheetData.date);
      if (fromDate) {
        const workOutRaw: string | null = timesheetData.workCheckOut || null;
        if (workOutRaw) {
          const outDatePart = workOutRaw.split(' ')[0]; // yyyy-MM-dd
          const outDate = this.parseYYYYMMDD(outDatePart);
          if (outDate) {
            const diffDays = Math.round(
              (outDate.getTime() - fromDate.getTime()) / (1000 * 60 * 60 * 24)
            );
            const effectiveToDate =
              diffDays >= 1 ? this.addDays(fromDate, 1) : fromDate;
            this.toDate = this.formatDDMMYYYY(effectiveToDate);
          } else {
            // Fallback: preserve existing behavior (fromDate + 1)
        this.toDate = this.formatDDMMYYYY(this.addDays(fromDate, 1));
      }
        } else {
          // No stored out-time; fallback to previous behavior
          this.toDate = this.formatDDMMYYYY(this.addDays(fromDate, 1));
        }
      }
    }

    // 2. ApMoSys Times (defer to next tick so time picker is rendered and receives value)
    if (timesheetData.workCheckIn || timesheetData.workCheckOut) {
      const inTime = timesheetData.workCheckIn ? this.extractTimeFromDateTime(timesheetData.workCheckIn) : null;
      const outTime = timesheetData.workCheckOut ? this.extractTimeFromDateTime(timesheetData.workCheckOut) : null;
      this.apmosysInTime = inTime;
      this.apmosysOutTime = outTime;
      setTimeout(() => {
        this.apmosysInTime = inTime;
        this.apmosysOutTime = outTime;
        this.cdr.detectChanges();
      }, 0);
    }

    // 3. Calculate Total Presence
    this.totalPresence = timesheetData.totalWorkingMinutes
      ? timesheetData.totalWorkingMinutes / 60
      : 0;
    // 4. Timesheet Applied For (use Number() so string/number empId from API matches)
    const timesheetEmpId = timesheetData.empId != null ? Number(timesheetData.empId) : null;
    const currentEmpId = this.currentUser?.empId != null ? Number(this.currentUser.empId) : null;

    if (timesheetEmpId !== null && currentEmpId !== null && timesheetEmpId === currentEmpId) {
      this.timesheetAppliedFor = 'self';
      this.timesheetFilledForUser.empId = this.currentUser.empId;
      this.selectedTeamMember = null;
      this.appelectMember = null;
    } else {
      this.timesheetAppliedFor = 'team';
      this.timesheetFilledForUser.empId = timesheetData.empId;
      this.timesheetFilledForUser.name = (timesheetData as any).employeeName ?? this.timesheetFilledForUser.name;
      // Show shadow/team selection immediately (dropdown binds to appelectMember)
      this.selectedTeamMember = { empId: timesheetData.empId, name: this.timesheetFilledForUser.name };
      this.appelectMember = this.selectedTeamMember;
    }

    // 5. SIMPLE, DIRECT MAPPING: map backend locationSessions -> timesheetLocations in one pass
    const locationSessionsToPopulate = timesheetData.locationSessions || [];
    this.timesheetLocations = [];

    locationSessionsToPopulate.forEach((locationData, lIndex) => {
      const location: LocationEntry = {
        locationMappingId: locationData.locationMappingId,
        workLocationType: locationData.workLocationType,
        workLocationTypeId: locationData.workLocationTypeId != null ? Number(locationData.workLocationTypeId) : null,
        locationInTime: locationData.locationInTime ? this.extractTimeFromDateTime(locationData.locationInTime) : null,
        locationOutTime: locationData.locationOutTime ? this.extractTimeFromDateTime(locationData.locationOutTime) : null,
        totalWorkingHours: locationData.totalWorkingHours,
        projects: []
      };
      if (locationData.projects && locationData.projects.length > 0) {
        location.projects = locationData.projects.map((projectData) => {
          console.log("clientLocationId ==> ",projectData.clientLocationId)
          console.log("projectData ==> ",projectData)
          const project: ProjectEntry = {
            projectId: projectData.projectId != null ? Number(projectData.projectId) : null,
            projectName: projectData.projectName,
            clientSideId: projectData.clientSideId,
            hasClientFlag: projectData.hasClientFlag || !!projectData.clientId,
            shadowEmpId: projectData.shadowEmpId,
            isShadowTimesheet: projectData.isShadowTimesheet || false,
            isShadowForSelf: projectData.isShadowForSelf || false,
            clientId: projectData.clientId != null ? Number(projectData.clientId) : null,
            clientLocationId: projectData.clientLocationId != null ? Number(projectData.clientLocationId) : null,
            clientApprovalStatus: projectData.clientApprovalStatus,
            totalWorkingHours: projectData.totalWorkingHours,
            shadowForList: [],
            activities: [],
            projectActivities: [],
            clientList: [],
            clientLocationList: [],
            projectList: [],
            timesheetId: projectData.timesheetId,
            poNo: projectData.poNo,
            poId: projectData.poId,
            status: (projectData.status as any) || 1,
            locationMappingId: locationData.locationMappingId,
            totalClientWorkingMinutes: projectData.totalClientWorkingMinutes,
            projectHoursMinutes: projectData.projectHoursMinutes,
            description: projectData.description,
            _lastValidProjectId: projectData.projectId != null ? Number(projectData.projectId) : null,
            _lastValidProjectName: projectData.projectName ?? ''
          };
          console.log("project => ", project);
          // Populate activities (convert minutes -> hours for display, same as create flow)
          if (projectData.activities && projectData.activities.length > 0) {
            project.activities = this.populateActivities(projectData.activities as any, project);
          } else if (this.isDayTypeFillable()) {
            project.activities = [this.createActivity(null, project.projectId)];
          } else {
            project.activities = [];
          }

          // After activities are populated, compute project total hours from activities
          if (project.activities && project.activities.length > 0) {
            let projHours = 0;
            project.activities.forEach(a => {
              projHours += Number(a.durationMinutes) || 0; // durationMinutes is in hours for UI
            });
            project.totalWorkingHours = projHours;
          }

          // Load client dropdowns for this project (uses existing API, one project at a time)
          if (project.projectId) {
            this.populateProjectDropdowns(project, timesheetEmpId ?? undefined);
          }

          return project;
        });
      } else {
        // Ensure at least one project row per location
        location.projects = [this.createProject(location, null)];
      }

      this.timesheetLocations.push(location);
    });
    console.log(this.timesheetLocations,"timesheetLocations");
    // 6. Populate Documents (if any)
    

    // 7. Expand first location/project for better UX
    if (this.timesheetLocations.length > 0) {
      this.expandedLocationIndex = 0;
      if (this.timesheetLocations[0].projects && this.timesheetLocations[0].projects.length > 0) {
        this.expandedProjectIndexMap[0] = 0;
      }
    }

    // 8. Load project list for dropdowns (needed for Project dropdown to show selected value)
    // This must happen before getTimesheetMetadata/loadTeamMemberForUpdate so projectList is ready
    if (this.fromDate && timesheetEmpId) {
      this.getProjectListForDateAndEmpId(timesheetEmpId).then(() => {
            // . Build document upload list so upload option is visible when project has clientSideId + clientApprovalStatus
            // (autofill does not include document data, but upload UI should show for qualifying projects)
            this.getListToRenderUpload();
          }).catch(error => {
            console.error('Error loading project list for update:', error);
          });
    }
    if (timesheetData.documentData) {
      this.populateDocuments(timesheetData.documentData);
    }

    // 9. Load metadata / team members (does not affect already-populated locations)
    if (timesheetEmpId !== null && currentEmpId !== null && timesheetEmpId === currentEmpId) {
      this.getTimesheetMetadata();
    } else {
      this.loadTeamMemberForUpdate(timesheetData.empId);
    }

    // 10. Recalculate location/project totals so "Total Hours" reflects populated activities
    this.onHoursChange();



    // 12. In update mode, load shadowForList for projects that have isShadowTimesheet so Shadow For dropdown shows options and selected value
    if (this.isUpdation && this.fromDate && timesheetEmpId != null) {
      this.timesheetLocations.forEach(loc => loc.projects?.forEach(proj => {
        if (proj.isShadowTimesheet && proj.projectId) this.getEmployeeListByProjectId(proj, timesheetEmpId);
      }));
    }
  }

  /**
   * Populate locations from server data
   * @param locationSessions - Location sessions from server
   * @param timesheetEmpId - Optional empId for client-details API (used when loading for edit)
   */
  populateLocations(locationSessions: LocationEntry[], timesheetEmpId?: number): void {
    console.log('[populateLocations] Called with', locationSessions?.length || 0, 'location sessions');
    console.log('[populateLocations] Current timesheetLocations before clear:', this.timesheetLocations?.length || 0);
    console.log('[populateLocations] isUpdation:', this.isUpdation);
    console.log('[populateLocations] Full locationSessions data:', JSON.stringify(locationSessions, null, 2));
    
    // Always clear and repopulate when we have data (this is the populate method, so we're replacing existing data)
    this.timesheetLocations = [];
    console.log('[populateLocations] Cleared timesheetLocations, starting population...');

    if (!locationSessions || locationSessions.length === 0) {
      console.log('[populateLocations] No location sessions, adding default location');
      this.addLocation(null);
      return;
    }

    locationSessions.forEach((locationData, index) => {
      console.log(`[populateLocations] Processing location ${index}:`, JSON.stringify(locationData, null, 2));
      
      // Resolve workLocationType from workLocationTypeId if not provided
      let workLocationType = locationData.workLocationType;
      if (!workLocationType && locationData.workLocationTypeId) {
        if (this.workLocationList?.length > 0) {
          const workLoc = this.workLocationList.find((wl: any) => 
            Number(wl.workLocationTypeId) === Number(locationData.workLocationTypeId)
          );
          workLocationType = workLoc?.workLocationType || null;
          console.log(`[populateLocations] Resolved workLocationType from ID ${locationData.workLocationTypeId}: ${workLocationType}`, workLoc);
        } else {
          console.warn(`[populateLocations] workLocationList not ready (${this.workLocationList?.length || 0} items), cannot resolve workLocationType for ID ${locationData.workLocationTypeId}`);
        }
      }
      
      const location: LocationEntry = {
        locationMappingId: locationData.locationMappingId,
        workLocationType: workLocationType || `Location ${index + 1}`, // Fallback if can't resolve
        workLocationTypeId: locationData.workLocationTypeId != null ? Number(locationData.workLocationTypeId) : null,
        locationInTime: locationData.locationInTime ? this.extractTimeFromDateTime(locationData.locationInTime) : null,
        locationOutTime: locationData.locationOutTime ? this.extractTimeFromDateTime(locationData.locationOutTime) : null,
        totalWorkingHours: locationData.totalWorkingHours,
        projects: []
      };

      if (locationData.projects && locationData.projects.length > 0) {
        console.log(`[populateLocations] Location ${index} has ${locationData.projects.length} projects to populate`);
        location.projects = this.populateProjects(locationData.projects, location, timesheetEmpId);
      } else {
        console.log(`[populateLocations] Location ${index} has no projects, creating default`);
        location.projects = [this.createProject(location, null)];
      }

      this.timesheetLocations.push(location);
      console.log(`[populateLocations] Added location ${index}: ${location.workLocationType} (ID: ${location.workLocationTypeId}), ${location.projects?.length || 0} projects`);
      location.projects?.forEach((p, pIdx) => {
        console.log(`  Project ${pIdx}: ${p.projectName || 'null'} (ID: ${p.projectId}), ${p.activities?.length || 0} activities`);
      });
    });
    console.log('[populateLocations] Completed. Total locations:', this.timesheetLocations.length);
    console.log('[populateLocations] Final timesheetLocations:', JSON.stringify(this.timesheetLocations.map(l => ({
      workLocationType: l.workLocationType,
      workLocationTypeId: l.workLocationTypeId,
      projectsCount: l.projects?.length || 0,
      projects: l.projects?.map(p => ({ projectId: p.projectId, projectName: p.projectName, activitiesCount: p.activities?.length || 0 }))
    })), null, 2));
  }

  /**
   * Populate projects from server data
   * @param projectsData - Projects data from server
   * @param location - Parent location entry
   * @param timesheetEmpId - Optional empId for client-details API (used when loading for edit)
   */
  populateProjects(projectsData: ProjectEntry[], location: LocationEntry, timesheetEmpId?: number): ProjectEntry[] {
    console.log(`[populateProjects] Called with ${projectsData?.length || 0} projects for location ${location.workLocationType}`);
    const projects: ProjectEntry[] = [];

    projectsData.forEach((projectData) => {
      console.log(`[populateProjects] Processing project: ${projectData.projectName || 'null'} (ID: ${projectData.projectId})`);
      
      // Resolve projectName from projectId if not provided (backend might not return it)
      let projectName = projectData.projectName;
      if (!projectName && projectData.projectId) {
        if (this.activeProjectList?.length > 0) {
          // Try both number and string comparison (API might return different types)
          const proj = this.activeProjectList.find((p: any) => 
            Number(p.projectId) === Number(projectData.projectId) || 
            p.projectId === projectData.projectId
          );
          projectName = proj?.projectName || null;
          console.log(`[populateProjects] Resolved projectName from ID ${projectData.projectId}: ${projectName}`, proj);
        } else {
          console.warn(`[populateProjects] activeProjectList not ready (${this.activeProjectList?.length || 0} items), cannot resolve projectName for ID ${projectData.projectId}`);
        }
      }
      
      const project: ProjectEntry = {
        projectId: projectData.projectId != null ? Number(projectData.projectId) : null,
        projectName: projectName,
        clientSideId: projectData.clientSideId,
        hasClientSideId: projectData.hasClientSideId || false,
        hasClientFlag: projectData.hasClientFlag || false,
        shadowEmpId: projectData.shadowEmpId,
        isShadowTimesheet: projectData.isShadowTimesheet || false,
        isShadowForSelf: projectData.isShadowForSelf || false,
        clientId: projectData.clientId != null ? Number(projectData.clientId) : null,
        clientLocationId: projectData.clientLocationId != null ? Number(projectData.clientLocationId) : null,
        clientApprovalStatus: projectData.clientApprovalStatus,
        activities: [],
        projectActivities: [],
        clientList: [],
        clientLocationList: [],
        // projectList will be updated after getProjectListForDateAndEmpId completes (in update mode)
        // For now, set empty array - it will be populated in getProjectListForDateAndEmpId success handler
        projectList: [],
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

      if (project.projectId) {
        this.populateProjectDropdowns(project, timesheetEmpId);
      }

      projects.push(project);
      console.log(`[populateProjects] Added project ${project.projectName} with ${project.activities?.length || 0} activities`);
    });

    console.log(`[populateProjects] Completed. Total projects: ${projects.length}`);
    return projects;
  }

  /**
   * Populate activities from server data
   * @param activitiesData - Activities data from server
   * @param project - Parent project entry
   */
  populateActivities(activitiesData: ActivityNew[], project: ProjectEntry): ActivityNew[] {
    console.log(`[populateActivities] Called with ${activitiesData?.length || 0} activities for project ${project.projectName || project.projectId}`);
    const activities: ActivityNew[] = [];

    activitiesData.forEach((activityData, index) => {
      // Backend returns duration_minutes (minutes); form displays Hours, so convert to hours for display
      const durationHours = activityData.durationMinutes != null
        ? Math.round((Number(activityData.durationMinutes) / 60) * 100) / 100
        : null;
      console.log(`[populateActivities] Processing activity ${index}: activityId=${activityData.activityId}, description="${activityData.description}", durationMinutes=${activityData.durationMinutes} -> ${durationHours} hours`);
      const activity: ActivityNew = {
        activityId: activityData.activityId != null ? Number(activityData.activityId) : null,
        description: activityData.description,
        durationMinutes: durationHours,
        projectId: project.projectId,
        teamId: activityData.teamId != null ? Number(activityData.teamId) : null,
        timesheetId: activityData.timesheetId,
        clientTeamList: [],
        allActivitiesForProject: []
      };

      // ✅ Populate team list from clientDetails if available
      if (project.clientId && project.clientLocationId && project.clientDetails && project.clientDetails.project) {
        activity.clientTeamList = project.clientDetails.project.teams.map(team => ({
          teamId: team.teamId != null ? Number(team.teamId) : null,
          teamName: team.teamName
        }));

        // Ensure teamId matches type for Angular binding
        if (activity.teamId) {
          const teamIdNum = Number(activity.teamId);
          const matchedTeam = activity.clientTeamList.find(t => Number(t.teamId) === teamIdNum);
          if (matchedTeam) {
            activity.teamId = teamIdNum;
          }
        }

        // Load activities for project if team is selected
        if (activity.teamId) {
          this.onProjectTeamSelect(activity.teamId, project);
        }
      }

      activities.push(activity);
      console.log(`[populateActivities] Added activity ${index}: activityId=${activity.activityId}, description="${activity.description}", durationMinutes=${activity.durationMinutes}`);
    });

    console.log(`[populateActivities] Completed. Total activities: ${activities.length}`);
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

    // uniqueProjectsList / empHasClientSideId are updated by populateFormFromTimesheetData step 11 (getListToRenderUpload)
    // Avoid duplicate call - caller (populateFormFromTimesheetData) calls getListToRenderUpload at the end
  }

  /**
   * Populate project dropdowns (client, location, team lists)
   * Fetches client details if not already loaded
   * @param project - Project entry to populate
   * @param timesheetEmpIdOverride - When provided (e.g. from edit load), use this empId for client-details API
   */
  populateProjectDropdowns(project: ProjectEntry, timesheetEmpIdOverride?: number): void {
    if (!project.clientDetails && project.projectId) {
      this.getClientDetailsByProjectIdAndEmpId(project, timesheetEmpIdOverride, !!timesheetEmpIdOverride);
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
    // ✅ CRITICAL FIX: Use proper async handling instead of setTimeout
    this.getAllTeamMemberList()
      .then(() => {
        const teamMember = this.teamMemberList?.find(m => m.empId === empId || Number(m.empId) === Number(empId));
        if (teamMember) {
          this.timesheetFilledForUser.empId = teamMember.empId;
          this.timesheetFilledForUser.name = teamMember.name;
          this.selectedTeamMember = teamMember;
          this.appelectMember = teamMember; // So Team Member dropdown shows selected value
          this.getTimesheetMetadata();
          // Load available timesheets for date filtering
          if (this.serverDate) {
            this.getAllAvailableTimesheetByEmpId(this.timesheetFilledForUser);
          }
          this.cdr.detectChanges();
        } else {
          console.warn(`Team member with empId ${empId} not found`);
          this.openAlertMod(
            this.alertTemplate,
            `Team member not found. Please refresh and try again.`
          );
        }
      })
      .catch(error => {
        console.error('Error loading team members:', error);
        this.openAlertMod(
          this.alertTemplate,
          'Failed to load team members. Please try again.'
        );
      });
  }

  /**
   * Update existing timesheet
   * Similar to createTimesheet but includes timesheetId and handles existing documents
   */
  async updateTimesheet() {
    // Clear previous highlights
    if(!this.isDayTypeFillable() && !this.noProjectEmployee){
     const successFlag =  await this.prepareDataForNonWorkingDay();
     if(successFlag){
       if(this.timesheetLocations[0].projects.length ==0){
        if(this.dayType == 7){
          this.openAlertMod(this.alertTemplate, 'Cannot create timesheet as there are no valid Client projects to assign for this day. Please contact your Reporting Manager immediately.');
        }else if(this.dayType == 6){
          this.openAlertMod(this.alertTemplate, 'Cannot create timesheet as there are no valid Internal/Bench projects to assign for this day. Please contact your Reporting Manager immediately.');
        }else{
          this.openAlertMod(this.alertTemplate, 'Cannot create timesheet as there are no valid projects to assign for this day. Please contact your Reporting Manager immediately.');
        }
        return 
       }
      }else{
        return; 
      }
    }
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
      // ✅ CRITICAL FIX: Use requestAnimationFrame instead of setTimeout for better timing
      requestAnimationFrame(() => {
        this.openAlertMod(this.alertTemplate, warningMessage);
      });
    }

    console.log(this.timesheetLocations,'timesheetLocations before API formatting');
    const dataSet: LocationEntry[] = structuredClone(this.timesheetLocations);

    // Ensure Shadow for self projects do not send client approval status (not required, dropdown hidden)
    dataSet.forEach((loc) => loc.projects?.forEach((p) => { if (p.isShadowForSelf) p.clientApprovalStatus = null; }));

    // ✅ MODERATE FIX: Add null checks for empId
    const targetEmpId = this.timesheetAppliedFor?.toLowerCase() === 'self'
      ? this.currentUser?.empId
      : (this.timesheetFilledForUser?.empId || this.currentUser?.empId);
    
    if (!targetEmpId) {
      this.handleError(
        new Error('Employee ID not available'),
        'updateTimesheet',
        true,
        'Employee ID not available. Please refresh and try again.'
      );
      return;
    }

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
      workCheckIn: TimesheetFormComponent.NON_FILLABLE_DAY_TYPES.includes(this.dayType) ? null : this.formatDateTimeForBackend(this.apmosysInTime, convertToYYYYMMDD(this.fromDate)),
      workCheckOut: TimesheetFormComponent.NON_FILLABLE_DAY_TYPES.includes(this.dayType) ? null : this.formatDateTimeForBackend(this.apmosysOutTime, convertToYYYYMMDD(this.isNightShift ? this.toDate : this.fromDate)),
      currentManagerId: this.currentUser.managerId,
      totalWorkingMinutes: this.totalPresence * 60,
      locationSessions: dataSet,
      documentData: this.documentData
    };
    console.log(this.createOrUpdateObj,"createOrUpdateObj");
    // API expects durationMinutes in minutes; form stores hours
    this.convertActivityDurationsToMinutesForApi(this.createOrUpdateObj.locationSessions);
    const isNonFillable = !this.isDayTypeFillable();
    this.createOrUpdateObj.locationSessions.forEach((location: LocationEntry) => {
      location.locationInTime = isNonFillable ? null : this.formatDateTimeForBackend(location.locationInTime, convertToYYYYMMDD(this.fromDate));
      location.locationOutTime = isNonFillable ? null : this.formatDateTimeForBackend(location.locationOutTime, convertToYYYYMMDD(this.isNightShift ? this.toDate : this.fromDate));
      location.projects.forEach((project: ProjectEntry) => {
        if (isNonFillable) {
          project.activities = [];
        }
      });
    });

    // Call update API
    this.timesheetNewService.updateTimesheet(this.createOrUpdateObj, this.selectedFile)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          if (response.serviceStatus === "Success") {
            // Parent shows success message and refreshes list
            this.timesheetUpdated.emit(this.timesheetId);
            // Option: Reload updated data
            // this.loadTimesheetForUpdate(this.timesheetId);
          } else {
            // ✅ MODERATE FIX: Use centralized error handling
            const backendError =
              response?.serviceError ||
              response?.serviceResponse ||
              'Failed to update timesheet. Please try again.';
            this.handleError(
              new Error(backendError),
              'updateTimesheet',
              true,
              backendError
            );
          }
        },
        error: (error) => {
          const backendError =
            error?.error?.serviceError ||
            error?.error?.serviceResponse ||
            error?.error?.message ||
            'An unexpected error occurred while updating the timesheet.';
          this.handleError(
            error,
            'updateTimesheet',
            true,
            backendError
          );
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
   * @param file - Document object with docId and docType ('Filled' | 'Approved')
   */
  previewExistingDocument(file: { docId: number; docType?: string }): void {
    const approvedDocType = file?.docType === 'Approved';
    this.timesheetNewService.getDocumentById(file.docId, approvedDocType)
      .pipe(first(), takeUntil(this.destroy$))
      .subscribe({
        next: (blob: Blob) => {
          const mimeType = blob.type || 'application/octet-stream';
          if (this.isExcelMimeType(mimeType)) {
            const fileName = `document.${mimeType.includes('openxml') ? 'xlsx' : 'xls'}`;
            // this.downloadBlobAsFile(blob, fileName);
            this.excelDownloadService.openConfirmAndDownload(blob,fileName);
          }
          else{
            const blobUrl = URL.createObjectURL(blob);
            this.showPreviewFromBlobUrl(blobUrl, mimeType);
          }
          },
        error: (error) => {
          this.handleError(
            error,
            'previewExistingDocument',
            true,
            'Failed to load document for preview. Please try again.'
          );
        }
      });
  }

  /**
   * Show preview from blob URL (for server-loaded documents)
   */
  private showPreviewFromBlobUrl(blobUrl: string, mimeType: string): void {
    this.activePreviewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(blobUrl);
    this.activeRawObjectUrl = blobUrl;
    if (mimeType === 'application/pdf') {
      this.activeFileType = 'pdf';
    } else if (mimeType.startsWith('image/')) {
      this.activeFileType = 'image';
    } else {
      this.activeFileType = 'image';
    }
    this.resetTransformations();
    this.modalRef = this.modalService.open(this.previewModal, { modalDialogClass: 'modal-lg' });
    this.modalRef.result.then(
      () => { if (blobUrl?.startsWith('blob:')) URL.revokeObjectURL(blobUrl); },
      () => { if (blobUrl?.startsWith('blob:')) URL.revokeObjectURL(blobUrl); }
    );
  }

  // ============================================
  // HELPER METHODS - Date/Time Conversion for Update
  // ============================================

  /**
   * Convert activity durations from hours (form) to minutes (API).
   * Form stores duration in hours in activity.durationMinutes; backend expects minutes.
   * Call this before create/update so payload always sends durationMinutes in minutes.
   */
  private convertActivityDurationsToMinutesForApi(locationSessions: LocationEntry[]): void {
    if (!locationSessions) return;
    const isNonFillable = !this.isDayTypeFillable();
    locationSessions.forEach((location) => {
      if (isNonFillable || !location.projects) return;
      location.projects.forEach((project) => {
        if (!project.activities) return;
        project.activities.forEach((activity) => {
          const hours = Number(activity.durationMinutes);
          if (hours != null && !Number.isNaN(hours) && hours >= 0) {
            const minutes = Math.round(hours * 60);
            activity.durationMinutes = minutes <= 0 ? null : minutes;
          } else {
            activity.durationMinutes = null;
          }
        });
      });
    });
  }

  /**
   * Convert YYYY-MM-DD to DD-MM-YYYY
   */
  /**
   * ✅ MODERATE FIX: Centralized date conversion methods
   * Convert DD-MM-YYYY to YYYY-MM-DD format
   */
  convertDDMMYYYYToYYYYMMDD(dateStr: string): string {
    if (!dateStr) return '';
    const [day, month, year] = dateStr.split('-');
    return `${year}-${month}-${day}`;  // Convert "08-01-2026" to "2026-01-08"
  }

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
   * Also handles time-only strings like "10:00" or "HH:mm:ss"
   */
  extractTimeFromDateTime(dateTimeStr: string): string | null {
    if (!dateTimeStr) return null;
    
    // Handle different datetime formats
    // Format 1: "YYYY-MM-DD HH:mm:ss"
    // Format 2: "YYYY-MM-DDTHH:mm:ss"
    // Format 3: ISO string
    // Format 4: Time-only string like "10:00" or "HH:mm:ss" (backend returns this for location times)
    
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
    } else if (dateTimeStr.match(/^\d{1,2}:\d{2}(:\d{2})?$/)) {
      // Time-only format like "10:00" or "10:00:00" - backend returns this for locationInTime/locationOutTime
      timePart = dateTimeStr;
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
      // NIGHT SHIFT LOGIC:
      // 1. In-time always points to fromDate
      // 2. Out-time points to toDate (if toDate exists and is night shift)
      // 3. If night shift but no toDate, treat as same date
      const startDate = parseDate(fromDate);
      let endDate;
      if (this.isNightShift && toDate) {
        // Night shift with toDate: out-time points to toDate
        endDate = parseDate(toDate);
      } else {
        // Normal shift OR night shift without toDate: both on fromDate
        endDate = parseDate(fromDate);
      }

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
      // Invalid interval - out time before in time (don't show popup here; validateLocationTimes shows it on AM/PM change)
      if (outDateTime.getTime() < inDateTime.getTime()) {
        return 0;
      }


      // ---------- CALCULATION ----------
      const diffMs = outDateTime.getTime() - inDateTime.getTime();
      const totalHours = diffMs / (1000 * 60 * 60);
      console.log('totalHoursForLocation', totalHours);
      return Math.round(totalHours * 100) / 100;

    } catch (error) {
      console.error('Error calculating working hours', error);
      return 0;
    }
  }


  /**
   * Called when user changes a part of location Log In or Log Out time (hour, minute, or AM/PM).
   * Show "Log-Out must be greater than Log-In" popup only when AM/PM is changed (same as work check-in/out),
   * so we don't show it as soon as HH/MM are selected (default AM would trigger error before user picks PM).
   * Hours recalculation is already triggered by ngModelChange on the picker.
   */
  onLocationTimePartChange(location: LocationEntry, part: 'hour' | 'minute' | 'ampm'): void {
    if (part === 'ampm') {
      this.validateLocationTimes(location);
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
        // Show error but DO NOT auto-reset user-entered hours
        // Let the user manually correct inconsistent values to avoid unexpected data loss
        this.openAlertMod(
          this.alertTemplate,
          'Total working hours for location cannot be less than sum of project working hours. Please adjust your project hours or location hours.'
        );
        return;
      }

      totalLocationHours += location.totalWorkingHours || 0;

      // Validation 2: Total location hours cannot exceed total presence
      // Round both to 2 decimals to avoid false errors when they are equal (e.g. 33 vs 33 after night-shift toggle)
      const locRounded = Math.round(totalLocationHours * 100) / 100;
      const presRounded = Math.round((this.totalPresence || 0) * 100) / 100;

      if (locRounded > presRounded) {
        // Show error but DO NOT auto-reset all hours for the location
        // This prevents all entered durations from being wiped out unexpectedly
        this.openAlertMod(
          this.alertTemplate,
          'Total working hours for all locations cannot be more than total presence hours. Please adjust your location/project hours.'
        );
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
   * Sync shadow options (isShadowTimesheet, isShadowForSelf, shadowEmpId, shadowForList) for the same project
   * across all locations so the user cannot have different shadow choice for the same project in different locations.
   */
  syncShadowForProjectAcrossLocations(sourceProject: ProjectEntry): void {
    if (!sourceProject?.projectId || !this.timesheetLocations?.length) return;
    const projectId = sourceProject.projectId;
    this.timesheetLocations.forEach((location) => {
      location.projects?.forEach((proj) => {
        if (proj !== sourceProject && Number(proj.projectId) === Number(projectId)) {
          proj.isShadowTimesheet = sourceProject.isShadowTimesheet;
          proj.isShadowForSelf = sourceProject.isShadowForSelf;
          proj.shadowEmpId = sourceProject.shadowEmpId;
          if (sourceProject.shadowForList?.length) {
            proj.shadowForList = sourceProject.shadowForList;
          }
        }
      });
    });
  }

  /**
   * Handle shadow timesheet toggle change
   * Validates shadow timesheet is only available for self timesheet and syncs same project across locations.
   */
  onShadowTimesheetChange(project: ProjectEntry): void {
    if (this.timesheetAppliedFor == 'self') {
      this.syncShadowForProjectAcrossLocations(project);
      this.getEmployeeListByProjectId(project, this.currentUser.empId);
      this.getListToRenderUpload();
    } else {
      // this.openAlertMod(this.alertTemplate, "Shadow timesheet is only available when timesheet is filled for self.");
      // project.isShadowTimesheet = false;
      this.syncShadowForProjectAcrossLocations(project);
      this.getEmployeeListByProjectId(project, this.timesheetFilledForUser.empId);
      this.getListToRenderUpload();
      return;
    }
  }

  /**
   * Handle Shadow for self change – sync same project across locations and refresh document list.
   */
  onShadowForSelfChange(project: ProjectEntry): void {
    this.syncShadowForProjectAcrossLocations(project);
    this.getListToRenderUpload();
  }

  /**
   * Handle Shadow for (employee) change – sync same project across locations so shadow emp is consistent.
   */
  onShadowEmpIdChange(project: ProjectEntry): void {
    this.syncShadowForProjectAcrossLocations(project);
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
    // ✅ When Shadow for self is selected, clear client approval status (not required and dropdown is hidden)
    // this.timesheetLocations.forEach((location) => {
    //   location.projects.forEach((project) => {
    //     if (project.isShadowForSelf) {
    //       project.clientApprovalStatus = null;
    //     }
    //   });
    // });

    // ✅ Preserve existing documentData from server (update mode) - getListToRenderUpload must not wipe it
    const existingDocs = [...this.documentData];
    this.documentData = [];
    const dataList: Map<number, ProjectEntry> = new Map<number, ProjectEntry>();
    
    // ✅ Reset at start - will be set to true only if we find at least one qualifying project
    this.empHasClientSideId = false;
    
    const findExistingDoc = (projectId: number, docType: 'Filled' | 'Approved'): TimesheetDocumentDataI | undefined =>
      existingDocs.find(d => Number(d.projectId) === Number(projectId) && d.docType === docType);
    
    const makePlaceholder = (projectId: number, docType: 'Filled' | 'Approved'): TimesheetDocumentDataI => ({
      projectId,
      docType,
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
    
    this.timesheetLocations.forEach((location) => {
      location.projects.forEach((project) => {
        // ✅ Check all three conditions: clientSideId exists, not shadow for self, and day type is fillable
        if (project.hasClientSideId && 
            this.isDayTypeFillable()) {
          
          // ✅ Set to true if ANY project qualifies (not just the first one)
          this.empHasClientSideId = true;
          dataList.set(project.projectId, project);

          if (this.documentData.some(d => Number(d.projectId) === Number(project.projectId))) {
            return;
          }
          
          // Create document entries based on approval status - preserve server-loaded docs when present
          if (project.clientApprovalStatus == 2) {
            // Approved: Show both Filled and Approved documents
            const filled = findExistingDoc(project.projectId, 'Filled') || makePlaceholder(project.projectId, 'Filled');
            const approved = findExistingDoc(project.projectId, 'Approved') || makePlaceholder(project.projectId, 'Approved');
            this.documentData.push(filled);
            this.documentData.push(approved);
          }
          else if (project.clientApprovalStatus == 1) {
            // Pending: Show only Filled document
            const filled = findExistingDoc(project.projectId, 'Filled') || makePlaceholder(project.projectId, 'Filled');
            this.documentData.push(filled);
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
    // ✅ MODERATE FIX: Add input validation
    if (!project || !project.projectId) {
      this.handleError(new Error('Invalid project'), 'getEmployeeListByProjectId', false);
      return;
    }

    // Decide target employee based on applied-for context
    const appliedFor = this.timesheetAppliedFor?.toLowerCase();
    const targetEmpId =
      appliedFor === 'self'
        ? this.currentUser?.empId
        : appliedFor === 'team'
          ? this.timesheetFilledForUser?.empId
          : empId;

    if (!targetEmpId || targetEmpId <= 0) {
      this.handleError(new Error('Invalid empId'), 'getEmployeeListByProjectId', false);
      return;
    }

    // Need selected date to make employee list date-aware (similar to getProjectListForDateAndEmpId)
    if (!this.fromDate) {
      this.openAlertMod(this.alertTemplate, 'Please select date first to load employees for shadow timesheet.');
      return;
    }

    const parsedDate = this.parseDDMMYYYY(this.fromDate);
    if (!parsedDate) {
      this.openAlertMod(this.alertTemplate, 'Invalid date format. Please re-select the date.');
      return;
    }

    // Pass YYYY-MM-DD (controller will derive startOfDay/endOfDay)
    const dateStr = `${parsedDate.getFullYear()}-${String(parsedDate.getMonth() + 1).padStart(2, '0')}-${String(parsedDate.getDate()).padStart(2, '0')}`;

    this.timesheetService.getEmployeeListByProjectId(project.projectId, targetEmpId, dateStr)
      .pipe(first(), takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          if (response.serviceStatus == "Success") {
            const shadowEmpList = response.serviceResponse || [];
            const preserveShadowEmpId = this.isUpdation && project.shadowEmpId != null;
            this.timesheetLocations.forEach(location => {
              location.projects.forEach(proj => {
                if (proj === project) {
                  if (!proj.isShadowTimesheet) {
                    proj.shadowEmpId = null;
                    proj.shadowForList = [];
                  }
                  else if (proj.isShadowTimesheet) {
                    if (!preserveShadowEmpId) proj.shadowEmpId = null;
                    proj.shadowForList = shadowEmpList;
                  }
                }
              });
            });
            this.syncShadowForProjectAcrossLocations(project);
          } else {
            // ✅ MODERATE FIX: Use centralized error handling
            this.handleError(
              new Error(response.serviceResponse || 'Failed to load employee list'),
              'getEmployeeListByProjectId',
              false
            );
            // Set empty list on error
            this.timesheetLocations.forEach(location => {
              location.projects.forEach(proj => {
                if (proj === project) {
                  proj.shadowForList = [];
                }
              });
            });
          }
        },
        error: (error) => {
          this.handleError(error, 'getEmployeeListByProjectId', false);
          // Set empty list on error
          this.timesheetLocations.forEach(location => {
            location.projects.forEach(proj => {
              if (proj === project) {
                proj.shadowForList = [];
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
    this.timesheetNewService.getAllWorkLocation()
      .pipe(first(), takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          if (response.serviceStatus == "Success") {
            this.workLocationList = response.serviceResponse || [];
          } else {
            // ✅ MODERATE FIX: Use centralized error handling
            this.handleError(
              new Error(response.serviceResponse || 'Failed to load work locations'),
              'getAllWorkLocationFromLocationMaster',
              false // Don't show to user, just log
            );
            this.workLocationList = [];
          }
        },
        error: (error) => {
          this.handleError(error, 'getAllWorkLocationFromLocationMaster', false);
          this.workLocationList = [];
        }
      });
  }

  getAllDSRApprovalStatusFromMaster(): void {
    this.timesheetNewService.getAllDSRApprovalStatus()
      .pipe(first(), takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          if (response.serviceStatus == "Success") {
            this.clientApprovalStatusList = response.serviceResponse || [];
            this.clientApprovalStatusList = this.clientApprovalStatusList.filter(f => f.status != "Rejected");
            // console.log("Client approval status", this.clientApprovalStatusList);
          } else {
            // ✅ MODERATE FIX: Use centralized error handling
            this.handleError(
              new Error(response.serviceResponse || 'Failed to load approval statuses'),
              'getAllDSRApprovalStatusFromMaster',
              false // Don't show to user, just log
            );
            this.clientApprovalStatusList = [];
          }
        },
        error: (error) => {
          this.handleError(error, 'getAllDSRApprovalStatusFromMaster', false);
          this.clientApprovalStatusList = [];
        }
      });
  }

  /**
   * Load autofill data for a selected date
   */
  /**
   * Load autofill data for a selected date (create mode only)
   * Should NOT be called during update mode - update loads data via loadTimesheetForUpdate
   * Supports both 'self' and 'team' modes - uses timesheetFilledForUser.empId when in team mode
   */
  loadAutofillData(selectedDate1: string): void {
    // Guard: never run autofill while in update mode
    if (this.isUpdation) {
      console.warn('loadAutofillData called during update mode - skipping to prevent data loss');
      return;
    }

    // Preserve current appliedFor mode before autofill (team or self)
    const currentAppliedFor = this.timesheetAppliedFor;
    const isTeamMode = currentAppliedFor?.toLowerCase() === 'team';

    // Determine employee for autofill: use team member's empId if in team mode, otherwise current user
    const targetEmpId = isTeamMode && this.timesheetFilledForUser?.empId
      ? this.timesheetFilledForUser.empId
      : this.currentUser?.empId;

    if (!targetEmpId) {
      console.warn('[loadAutofillData] empId not available, skipping autofill. isTeamMode:', isTeamMode);
      return;
    }

    if (!selectedDate1) {
      console.warn('[loadAutofillData] selected date is empty, skipping autofill.');
      return;
    }

    // Convert DD-MM-YYYY -> YYYY-MM-DD for backend
    const targetDateYMD = this.convertDDMMYYYYToYYYYMMDD(selectedDate1);
    if (!targetDateYMD) {
      console.warn('[loadAutofillData] Unable to convert selected date for autofill:', selectedDate1);
      return;
    }

    const payload = {
      empId: targetEmpId,
      date: targetDateYMD
    };

    console.log('[loadAutofillData] Fetching autofill for empId:', targetEmpId, 'date:', targetDateYMD, 'isTeamMode:', isTeamMode);

    this.timesheetNewService.getAutofillTimesheet(payload)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          if (response?.serviceStatus !== 'Success') {
            // Soft failure: no blocking error for create flow
            console.warn('[loadAutofillData] Autofill request failed:', response?.serviceResponse || response?.serviceError);
            return;
          }

          const template: EmployeeTimesheetDTO | null = response?.serviceResponse || null;
          if (!template) {
            // No previous working timesheet found or projects inactive
            console.info('[loadAutofillData] No autofill template returned from backend.', response?.serviceMessage);
            return;
          }

          // Clone template so we can safely adjust identity fields without mutating original response
          const cloned: EmployeeTimesheetDTO = JSON.parse(JSON.stringify(template));

          // Ensure this is treated as a CREATE template, not an update
          cloned.timesheetId = null;
          cloned.date = targetDateYMD;
          cloned.documentData = null;

          if (cloned.locationSessions) {
            cloned.locationSessions.forEach((location: any) => {
              location.locationMappingId = null;
              if (location.projects) {
                location.projects.forEach((project: any) => {
                  project.timesheetId = null;
                  project.locationMappingId = null;
                  if (project.activities) {
                    project.activities.forEach((activity: any) => {
                      activity.timesheetId = null;
                    });
                  }
                });
              }
            });
          }

          // Use populateFormFromTimesheetData in a special "autofill" mode
          this.isAutofillMode = true;
          try {
            this.populateFormFromTimesheetData(cloned);
          } catch (error) {
            console.error('[loadAutofillData] Error populating form from autofill template:', error);
            // Don't block user from filling manually if autofill population fails
            this.isAutofillMode = false;
          }
          // finally {
          //   this.isAutofillMode = false;
          // }

          // After autofill, force create mode for the selected date
          this.timesheetId = null;
    this.isUpdation = false;
          this.isCreation = true;
    this.fromDate = selectedDate1;

          // Recompute toDate for night shift based on new fromDate
          if (this.isNightShift && this.fromDate) {
            const parsedFrom = this.parseDDMMYYYY(this.fromDate);
            if (parsedFrom) {
              this.toDate = this.formatDDMMYYYY(this.addDays(parsedFrom, 1));
            }
          } else {
            this.toDate = null;
          }

          // Preserve the appliedFor mode and empId - do NOT reset to 'self' if in team mode
          // This ensures team member selection from 360 view is preserved after autofill
          if (!isTeamMode) {
            this.timesheetAppliedFor = 'self';
            this.timesheetFilledForUser.empId = targetEmpId;
          }
          // If isTeamMode, timesheetAppliedFor and timesheetFilledForUser.empId are already set correctly
        },
        error: (error) => {
          // Log only; do not block user from filling timesheet manually
          console.error('[loadAutofillData] Error while fetching autofill template:', error);
        }
      });
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
   * @param empId - Employee ID (optional, defaults to current user or selected team member)
   * @returns Promise that resolves when projects are loaded
   */
  getProjectListForDateAndEmpId(empId?: number): Promise<void> {
    // ✅ CRITICAL FIX: Add null checks
    const targetEmpId = empId || 
      (this.timesheetAppliedFor?.toLowerCase() === 'self' 
        ? this.currentUser?.empId 
        : this.timesheetFilledForUser?.empId);
    
    if (!targetEmpId) {
      const error = new Error('Employee ID not available');
      console.error(error.message);
      return Promise.reject(error);
    }
    
    // Use fromDate (must be set before calling this method)
    if (!this.fromDate) {
      const error = new Error('Date (fromDate) not available for fetching projects');
      console.error(error.message);
      this.disableAdd = true;
      this.openAlertMod(this.alertTemplate, 'Date is required to load projects. Please select a date first.');
      return Promise.reject(error);
    }
    
    // Convert date format: DD-MM-YYYY to YYYY-MM-DDTHH:mm:ss (LocalDateTime)
    const dateParsed = this.parseDDMMYYYY(this.fromDate);
    if (!dateParsed) {
      const error = new Error(`Invalid date format: ${this.fromDate}`);
      console.error(error.message);
      return Promise.reject(error);
    }
    
    const localDateTime = `${dateParsed.getFullYear()}-${String(dateParsed.getMonth() + 1).padStart(2, '0')}-${String(dateParsed.getDate()).padStart(2, '0')}T00:00:00`;

    const payload = {
      empId: targetEmpId,
      date: localDateTime
    };

    return new Promise<void>((resolve, reject) => {
      this.timesheetNewService.getProjectListForDateAndEmpId(payload)
        .pipe(first(), takeUntil(this.destroy$))
        .subscribe({
          next: (response: any) => {
            if (response.serviceStatus == "Success") {
              // Populate activeProjectList
              // Note: Response only contains projectId and projectName, not client/location info
              this.activeProjectList = response.serviceResponse || [];
              
              if (this.activeProjectList.length === 0) {
                this.disableAdd = true;
                if (this.timesheetAppliedFor?.toLocaleLowerCase() == 'self') {
                  this.openAlertMod(this.alertTemplate, 'No Projects assigned for the selected date. Please contact RMG.');
                } else if (this.timesheetAppliedFor?.toLocaleLowerCase() == 'team') {
                  let teamMember = this.teamMemberList?.find(employee => employee.empId === this.timesheetFilledForUser?.empId);
                  this.openAlertMod(this.alertTemplate, 'No Projects assigned to ' + (teamMember?.empName || 'team member') + ' for the selected date.');
                }
              } else {
                this.disableAdd = false;
              }
              
              // Update project lists for existing locations (for fillable day types)
              // In update mode OR when already populated from autofill: only update dropdown options, don't replace projects
              const hasExistingProjectsWithIds = this.timesheetLocations.some(loc =>
                loc.projects?.some(p => p.projectId != null)
              );
              if (this.isDayTypeFillable() && !this.isUpdation && !hasExistingProjectsWithIds) {
                // Create mode with empty form: populate one project per location from activeProjectList
                this.timesheetLocations.forEach(loc => {
                  if (loc.workLocationTypeId) {
                    this.populateProjectsForLocation(loc);
                  }
                });
              } else if (this.isUpdation || hasExistingProjectsWithIds) {
                // Update mode or autofill: update projectList dropdown options and projectName for existing projects
                // Same for both fillable and non-fillable so selected project shows on edit (e.g. non-fillable update)
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
                this.timesheetLocations.forEach(loc => {
                  if (loc.projects && loc.projects.length > 0) {
                    loc.projects.forEach(proj => {
                      proj.projectList = uniqueProjects;
                      // Ensure projectId matches type and exists in the list (for Angular binding)
                      if (proj.projectId && uniqueProjects.length > 0) {
                        const projectIdNum = Number(proj.projectId);
                        const matchedProject = uniqueProjects.find(p => Number(p.projectId) === projectIdNum);
                        if (matchedProject) {
                          // Ensure ID is set as number for Angular binding
                          proj.projectId = projectIdNum;
                          proj.hasClientSideId=matchedProject.hasClientSideId;
                          if (!proj.projectName && matchedProject.projectName) {
                            proj.projectName = matchedProject.projectName;
                            console.log(`[getProjectListForDateAndEmpId] Updated projectName for projectId ${proj.projectId}: ${proj.projectName}`);
                          }
                        } else {
                          console.warn(`[getProjectListForDateAndEmpId] projectId ${proj.projectId} not found in activeProjectList`);
                        }
                      }
                    });
                  }
                });
                console.log('[getProjectListForDateAndEmpId] Updated projectList dropdowns and projectNames for existing locations in update mode');
              }
              
              // Auto-select project if non-fillable day type and only one project available
              if (!this.isUpdation) {
                this.autoSelectProjectIfSingle();
              }
              
              resolve();
            } else {
              const errorMsg = response.serviceResponse || 'Failed to fetch projects';
              console.error('Failed to fetch projects:', errorMsg);
              this.disableAdd = true;
              if(this.isDayTypeFillable())
              this.openAlertMod(this.alertTemplate, 'Failed to load projects: ' + errorMsg);
              reject(new Error(errorMsg));
            }
          },
          error: (error) => {
            console.error('Error fetching projects:', error);
            this.disableAdd = true;
            this.openAlertMod(this.alertTemplate, 'Error loading projects. Please try again.');
            reject(error);
          }
        });
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
  allowDecimalOnly(event: KeyboardEvent) {
  const charCode = event.which ? event.which : event.keyCode;

  // backspace, delete, tab, escape, enter
  if ([8, 9, 27, 13].includes(charCode)) {
    return;
  }

  const inputValue = (event.target as HTMLInputElement).value;

  // only numbers and one decimal point
  if (
    (charCode < 48 || charCode > 57) && 
    charCode !== 46
  ) {
    event.preventDefault();
  }

  // Prevent multiple decimals
  if (charCode === 46 && inputValue.includes('.')) {
    event.preventDefault();
  }
  }

  /**
   * ✅ CRITICAL FIX: Cleanup on component destroy
   * Prevents memory leaks by unsubscribing all subscriptions
   */
  ngOnDestroy(): void {
    // Unsubscribe from all subscriptions
    this.destroy$.next();
    this.destroy$.complete();
    
    // Cleanup document URLs to prevent memory leaks
    this.cleanupDocumentData();
    
    // Close any open modals
    if (this.alertModalRef) {
      this.alertModalRef.close();
    }
    if (this.modalRef) {
      this.modalRef.close();
    }
    if (this.clientSideIdUpdateOrAddModalRef) {
      this.clientSideIdUpdateOrAddModalRef.close();
    }
    if (this.alertWithResetModRef) {
      this.alertWithResetModRef.close();
    }
    if (this.removeLocationConfirmModalRef) {
      this.removeLocationConfirmModalRef.close();
    }
  }



/* For disble date we had impleemnted logic 
   saperately for wokring and non working day 
   for day other than non-working (like working,half day, leave etc)
   we disable all dates which have corresponding timesheet filled for that day
   for non-working we enable date which have holiday 
   but for disabling we have same logic like if timesheet is available for that day disable that day
   but it is not correct like for holiday, week-off timesheet is filled by cron service 
   but we have to allow employee ot fill the timesheet for that day and in backend it
   is already handle and we allow user to overwrite such timesheet 
   but in frontend we have kept such date disable so specially for non-workind day if timesheet filled
   is of type Holiday, weekoff then we have to keep date enable .

   first tell me did u understand the problem and share me possible solution 
   i suggest we fetch timesheet based on min and max date just ignore date in disble logic 
   if day type is non-wokring and timesheet filled day id oof type holiday or weekoff

   */
  convertDdMmYyyyToIso(dateStr: string): string | null {
    if (!dateStr) return null;
  
    const [day, month, year] = dateStr.split('-');
  
    return `${year}-${month}-${day}`;
}

preventSpecialCharacters(event: any ,activity : any) {
   const sanitized = event.target.value.replace(/[^a-zA-Z0-9\s]/g, '');
  activity.description = sanitized;
  event.target.value = sanitized;
}

limitDecimals(event: any ,activity : any) {
  const value = event.target.value;
  if (value.includes('.')) {
    const parts = value.split('.');
    if (parts[1]?.length > 2) {
      event.target.value = parseFloat(value).toFixed(2);
      activity.durationMinutes = parseFloat(event.target.value);
    }
  }
}

  downloadFile(file: any): void {
    if (file.rawObjectUrl) {
      // Newly selected file — use object URL
      const a = document.createElement('a');
      console.log("A",a);
      console.log("Raw object url",file);
      a.href = file.rawObjectUrl;
      a.download = file.docName || 'download';
      a.click();
    } 
  }
  isExcelMimeType(mimeType: string): boolean {
    return mimeType === 'application/vnd.ms-excel'
      || mimeType === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
  }
  
  private downloadBlobAsFile(blob: Blob, fileName: string): void {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    a.click();
    URL.revokeObjectURL(url);
  }
  halfDayLeaveList:any[]=[]
  getHalfDayLeaves(empId: number): void {
    this.timesheetService.getAllHalfDayLeaves(empId)
      .pipe(first())
      .subscribe({
        next: (dates: string[]) => {
          this.halfDayLeaveList = dates;
          console.log(this.halfDayLeaveList);
        },
        error: (err) => {
          console.error('Error fetching half day leaves:', err);
        }
      });
  }
  halfDayValidation(){

    let date = this.convertDate(this.fromDate);
    if(date == null){
      return false
    }
    let isHalfday = this.halfDayLeaveList.includes(date);
    if(this.dayType == 8 && this.fromDate != null){
      if(!isHalfday){
        this.handleError(
          ("Please apply half day leave on the selected day first"),
          'createTimesheet',
          true,
          "Please apply half day leave on the selected day first"
        );
        // this.fromDate = null;
        // this.resetForm();
        return true;
      }
    }
    return false;

  }

   convertDate(dateStr) {
    if (!dateStr) return null;
    const [day, month, year] = dateStr.split("-");
    const date = new Date(`${year}-${month}-${day}`);
    return date.toISOString().split("T")[0];
  }


  private generateFileKey(
    projectId: number,
    fromDate: string,
    dayType: number,
    docType: string
  ): string {
    return `${projectId}_${fromDate}_${dayType}_${docType}`.toLowerCase();
  }

}