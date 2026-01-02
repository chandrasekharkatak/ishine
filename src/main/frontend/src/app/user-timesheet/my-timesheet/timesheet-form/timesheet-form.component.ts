import { Component, OnInit, TemplateRef, ViewChild, EventEmitter, Output, Input } from '@angular/core';
import { ActivityNew } from 'src/app/models/activityNew';
import { ProjectEntry } from 'src/app/models/projectEntry';
import { Timesheet } from 'src/app/models/timesheet';
import { DateTimePickerComponent } from "src/app/helpers/date-time-picker/date-time-picker.component";
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { User } from 'src/app/models/user';
import { Employee } from 'src/app/models/employee';
import { TeamViewService } from 'src/app/services/team-view.service';
import { first } from 'rxjs';
import { TimesheetService } from 'src/app/services/timesheet.service';
import { ValidationService } from 'src/app/services/validation.service';
import { InputValidationService } from 'src/app/services/input-validation.service';
import { Router } from '@angular/router';
import * as moment from 'moment';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { AppComponent } from 'src/app/app.component';

@Component({
  standalone: false,
  selector: 'app-timesheet-form',
  templateUrl: './timesheet-form.component.html',
  styleUrl: './timesheet-form.component.css'
})
export class TimesheetFormComponent implements OnInit {
  
  @Input() isCreation: boolean = true;
  @Input() isUpdation: boolean = false;
  @Output() timesheetCreated = new EventEmitter<any>();
  @Output() timesheetUpdated = new EventEmitter<any>();
  @Output() cancel = new EventEmitter<void>();

  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;
  
  @ViewChild("night_shift_template")
  night_shift_template: TemplateRef<any>;

  modalRef: NgbModalRef;
  timesheetProjects: ProjectEntry[] = [];
  timesheetObj: Timesheet = new Timesheet();
  allTimesheetActivities: any[] = [];
  allProjectsList: any[] = [];
  allActivityList: any[] = [];
  allDayTypes: any[] = [];
  activeProjectList: any[] = [];
  clientList: any[] = [];
  filteredClients: any[] = [];
  
  allMyTimesheets: any[] = [];
  timesheetActivities: any[] = [];
  expandedProjectIndex: number | null = 0;
  fromDate: string;
  toDate: string | null = null;
  currentUser: User;
  teamMemberList: any[] = [];
  errorMsg: string;
  employeeList: any[] = [];
  alertMessage: any;

  // Time picker arrays
  hours: string[] = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0'));
  minutes: string[] = Array.from({ length: 60 }, (_, i) => String(i).padStart(2, '0'));
  periods: string[] = ['AM', 'PM'];

  // Document handling
  selectedFile: File | null = null;
  selectedFile2: File | null = null;
  previewUrl1: SafeResourceUrl | null = null;
  previewUrl2: SafeResourceUrl | null = null;
  fileError1: string = '';
  fileError2: string = '';
  fileName1: any = null;
  fileName2: any = null;

  // Flags
  timesheetFillable = true;
  clientSideIdNotMandatory: Boolean = false;
  shadowForSelf: Boolean = false;
  syncTimes: boolean = false; // Per-project sync flag
  projectRequiresClientId: Boolean = false;
  clientSideIdMandetoryFromBackend: boolean = false;
  docRequiredForShadow: boolean = true;
  maxToDate: Date | null = null;
  disableCreateUpdateTimesheet: boolean = false;

  // Timesheet level client approval status (per-timesheet, not per-project)
  timesheetClientApprovalStatus: string = '';

  // Day Type Constants (using IDs)
  readonly DAY_TYPE_IDS = {
    WORKING: 1,
    HOLIDAY: 2,
    NON_WORKING: 3,
    WEEK_OFF: 4,
    LEAVE: 5,
    APMOSYS_HOLIDAY: 6,
    CLIENT_HOLIDAY: 7,
    HALF_DAY_WORKING: 8
  };

  // Working hours requirements
  readonly MIN_WORKING_HOURS = 9;
  readonly MIN_HALF_DAY_HOURS = 4;

  constructor(
    private modalService: NgbModal,
    private authenticationService: AuthenticationService,
    private teamViewService: TeamViewService,
    private timesheetService: TimesheetService,
    private validationService: ValidationService,
    private inputValidationService: InputValidationService,
    private router: Router,
    private sanitizer: DomSanitizer
  ) {
    // Subscribe to currentUser to keep it updated
    this.authenticationService.currentUser.subscribe(x => {
      this.currentUser = x;
      // Initialize timesheetObj with current user's empId if not already set
      if (this.currentUser && this.currentUser.empId && !this.timesheetObj.empId) {
        this.timesheetObj.empId = this.currentUser.empId;
      }
    });
  }

  ngOnInit(): void {
    // Set default application type if not set
    if (!this.timesheetObj.timesheetAppliedFor) {
      this.timesheetObj.timesheetAppliedFor = 'self';
    }
    
    this.addProject();
    this.getAllDayTypes();
    this.getEmployeeBasicInfo();
    
    // Wait for currentUser to be available, then load projects
    this.authenticationService.currentUser.pipe(first()).subscribe(user => {
      if (user && user.empId) {
        this.currentUser = user;
        if (!this.timesheetObj.empId) {
          this.timesheetObj.empId = user.empId;
        }
        // Load projects after a small delay to ensure component is fully initialized
        setTimeout(() => {
          this.getActiveProjectsAndClientSideIdByEmpId();
        }, 200);
      } else {
        console.warn('Current user not available in ngOnInit');
      }
    });
  }

  // ========== PROJECT MANAGEMENT ==========

  createActivity(): ActivityNew {
    return {
      clientId: null,
      clientLocationId: null,
      teamId: null,
      activityId: null,
      description: '',
      completionTime: null,
      clientLocationList: [],
      projectList: [],
      projectActivities: []
    };
  }

  createProject(): ProjectEntry {
    return {
      projectId: null,
      projectName: '',
      clientSideId: '',
      hasClientSideId: false,
      inTime: '',
      outTime: '',
      officeInTime: '',
      officeOutTime: '',
      clientInTime: '',
      clientOutTime: '',
      totalWorkingHours: '0',
      totalClientWorkingHours: '0',
      shadowEmpId: null,
      isShadowTimesheet: false,
      activities: [this.createActivity()],
      availableActivities: [],
      clientList: [],
      clientLocationList: [],
      projectList: []
    };
  }

  toggleProject(index: number) {
    this.expandedProjectIndex = this.expandedProjectIndex === index ? null : index;
  }

  calculateProjectHours(project: ProjectEntry) {
    const total = project.activities.reduce(
      (sum: number, a: any) => sum + (Number(a.completionTime) || 0),
      0
    );
    project.totalWorkingHours = total.toFixed(2);
  }

  onActivityHourChange(project: ProjectEntry) {
    this.calculateProjectHours(project);
  }

  addProject(): void {
    if (this.timesheetProjects.length > 0) {
      const lastProject = this.timesheetProjects[this.timesheetProjects.length - 1];
      if (!this.isProjectValid(lastProject)) {
        this.alertMessage = 'Please complete at least one activity in the current project before adding a new project.';
        this.openAlertMod(this.alertTemplate, this.alertMessage);
        return;
      }
    }
    this.timesheetProjects.push(this.createProject());
    this.expandedProjectIndex = this.timesheetProjects.length - 1;
  }

  removeProject(index: number): void {
    if (this.timesheetProjects.length <= 1) {
      this.alertMessage = 'At least one project is required.';
      this.openAlertMod(this.alertTemplate, this.alertMessage);
      return;
    }
    this.timesheetProjects.splice(index, 1);
    if (this.expandedProjectIndex === index) {
      this.expandedProjectIndex = null;
    } else if (this.expandedProjectIndex > index) {
      this.expandedProjectIndex--;
    }
  }

  addActivity(project: ProjectEntry): void {
    project.activities.push(this.createActivity());
  }

  removeActivity(project: ProjectEntry, index: number): void {
    if (project.activities.length <= 1) {
      this.alertMessage = 'At least one activity is required per project.';
      this.openAlertMod(this.alertTemplate, this.alertMessage);
      return;
    }
    project.activities.splice(index, 1);
    this.calculateProjectHours(project);
  }

  // ========== VALIDATION ==========

  isActivityComplete(a: ActivityNew): boolean {
    return !!(
      a.clientId &&
      a.clientLocationId &&
      a.teamId &&
      a.activityId &&
      a.description?.trim() &&
      a.completionTime &&
      a.completionTime > 0
    );
  }

  isProjectValid(p: ProjectEntry): boolean {
    return p.activities.some(a => this.isActivityComplete(a));
  }

  /**
   * Check if day type is non-working using dayTypeId
   * Non-working days: Holiday (2), Non-Working (3), Week Off (4), Leave (5), ApMoSys Holiday (6), Client Holiday (7)
   */
  isNonWorkingDay(dayTypeId: number): boolean {
    return [
      this.DAY_TYPE_IDS.HOLIDAY,
      this.DAY_TYPE_IDS.NON_WORKING,
      this.DAY_TYPE_IDS.WEEK_OFF,
      this.DAY_TYPE_IDS.LEAVE,
      this.DAY_TYPE_IDS.APMOSYS_HOLIDAY,
      this.DAY_TYPE_IDS.CLIENT_HOLIDAY
    ].includes(dayTypeId);
  }

  /**
   * Check if day type requires time/project/activity inputs
   * Only Working (1) and Half-day Working (8) require these inputs
   */
  requiresTimeAndProjectInputs(dayTypeId: number): boolean {
    return dayTypeId === this.DAY_TYPE_IDS.WORKING || dayTypeId === this.DAY_TYPE_IDS.HALF_DAY_WORKING;
  }

  /**
   * Get minimum required working hours based on day type
   */
  getMinRequiredHours(dayTypeId: number): number {
    if (dayTypeId === this.DAY_TYPE_IDS.HALF_DAY_WORKING) {
      return this.MIN_HALF_DAY_HOURS;
    }
    if (dayTypeId === this.DAY_TYPE_IDS.WORKING) {
      return this.MIN_WORKING_HOURS;
    }
    return 0;
  }

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

    // Get dayTypeId for validation
    const dayTypeIdValue = this.getDayTypeId(this.timesheetObj.dayType);
    if (!dayTypeIdValue) {
      this.alertMessage = "Invalid Day Type selected !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    // 2. For non-working days
    if (this.isNonWorkingDay(dayTypeIdValue)) {
      if (!this.validationService.validateNullUndefinedEmptyString(this.timesheetObj.description)) {
        this.alertMessage = "Please enter Timesheet Description !!";
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      return true;
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

        if (activity.description && activity.description.trim()) {
          if (!this.validationService.validateActivityTimesheetDiscription(activity.description)) {
            this.alertMessage = `Please enter valid Activity Description for Activity ${j + 1} in Project ${i + 1} !!`;
            this.openAlertMod(template, this.alertMessage);
            return false;
          }
        }

        if (!activity.completionTime || activity.completionTime <= 0) {
          this.alertMessage = `Please enter valid Activity Completion Time for Activity ${j + 1} in Project ${i + 1} !!`;
          this.openAlertMod(template, this.alertMessage);
          return false;
        }

        if (!this.validationService.validateCompletionTime(activity.completionTime?.toString())) {
          this.alertMessage = `Please enter valid Activity Completion Time (0-24 hrs) for Activity ${j + 1} in Project ${i + 1} !!`;
          this.openAlertMod(template, this.alertMessage);
          return false;
        }

        projectTotalActivityTime += activity.completionTime;
      }

      // 4.7 Validate activity time vs project working hours
      const projectWorkingHours = this.calculateProjectWorkingHours(project);
      if (projectTotalActivityTime > projectWorkingHours) {
        this.alertMessage = `Total Activity Time (${projectTotalActivityTime} hrs) cannot exceed Project Working Hours (${projectWorkingHours.toFixed(2)} hrs) for Project ${i + 1} !!`;
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (projectTotalActivityTime <= 0 || projectTotalActivityTime > 24) {
        this.alertMessage = `Total Activity Time must be between 0 and 24 hours for Project ${i + 1} !!`;
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    // 4.8 Validate minimum working hours requirement
    const dayTypeIdForValidation = this.getDayTypeId(this.timesheetObj.dayType);
    if (this.requiresTimeAndProjectInputs(dayTypeIdForValidation)) {
      const totalWorkingHours = this.calculateTimesheetTotalWorkingHours();
      const minRequiredHours = this.getMinRequiredHours(dayTypeIdForValidation);
      
      if (totalWorkingHours < minRequiredHours) {
        this.alertMessage = `Total Working Hours (${totalWorkingHours.toFixed(2)} hrs) must be at least ${minRequiredHours} hours for ${dayTypeIdForValidation === this.DAY_TYPE_IDS.HALF_DAY_WORKING ? 'Half-day Working' : 'Working'} day !!`;
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    // 5. Complex time sync validation (3 approaches)
    const validationResult = this.validateTimeSyncApproach();
    if (!validationResult.valid) {
      this.alertMessage = validationResult.message;
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    // 6. Document validation (per-timesheet, not per-project)
    if (this.timesheetClientApprovalStatus === 'pending' && !this.selectedFile) {
      this.alertMessage = "Please upload Filled Attendance Proof document !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    
    if (this.timesheetClientApprovalStatus === 'approved' && (!this.selectedFile || !this.selectedFile2)) {
      this.alertMessage = "Please upload both Filled and Approved Attendance Proof documents !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    return true;
  }

  // ========== TIME SYNC VALIDATION (3 APPROACHES) ==========

  validateTimeSyncApproach(): { valid: boolean; message: string } {
    const projectsWithClientId = this.timesheetProjects.filter(p => p.hasClientSideId && !this.clientSideIdNotMandatory);
    const projectsWithoutClientId = this.timesheetProjects.filter(p => !p.hasClientSideId || this.clientSideIdNotMandatory);
    
    // Approach 1: All projects don't have Client Side ID
    if (projectsWithClientId.length === 0) {
      const totalActivityHours = this.timesheetProjects.reduce((sum, p) => {
        return sum + p.activities.reduce((s, a) => s + (a.completionTime || 0), 0);
      }, 0);
      
      const timesheetTotalWorkingHours = this.calculateTimesheetTotalWorkingHours();
      
      if (totalActivityHours > timesheetTotalWorkingHours) {
        return {
          valid: false,
          message: `Total Activity Hours (${totalActivityHours.toFixed(2)} hrs) cannot exceed Timesheet Total Working Hours (${timesheetTotalWorkingHours.toFixed(2)} hrs) !!`
        };
      }
      return { valid: true, message: '' };
    }
    
    // Approach 2: Some projects have Client Side ID
    if (projectsWithClientId.length > 0 && projectsWithoutClientId.length > 0) {
      // Validate projects with client ID
      for (const project of projectsWithClientId) {
        if (!project.clientInTime || !project.clientOutTime) {
          return {
            valid: false,
            message: `Please enter Client In/Out Time for Project: ${project.projectName || project.projectId} !!`
          };
        }
        
        const clientWorkingHours = this.calculateClientWorkingHours(project);
        const projectActivityHours = project.activities.reduce((sum, a) => sum + (a.completionTime || 0), 0);
        
        if (projectActivityHours > clientWorkingHours) {
          return {
            valid: false,
            message: `Activity Hours (${projectActivityHours.toFixed(2)} hrs) cannot exceed Client Working Hours (${clientWorkingHours.toFixed(2)} hrs) for Project: ${project.projectName || project.projectId} !!`
          };
        }
      }
      
      // Validate timesheet level
      const timesheetTotalWorkingHours = this.calculateTimesheetTotalWorkingHours();
      const allProjectsWorkingHours = this.timesheetProjects.reduce((sum, p) => {
        return sum + this.calculateProjectWorkingHours(p);
      }, 0);
      
      if (allProjectsWorkingHours > timesheetTotalWorkingHours) {
        return {
          valid: false,
          message: `Total Projects Working Hours (${allProjectsWorkingHours.toFixed(2)} hrs) cannot exceed Timesheet Total Working Hours (${timesheetTotalWorkingHours.toFixed(2)} hrs) !!`
        };
      }
      
      return { valid: true, message: '' };
    }
    
    // Approach 3: Only one project with Client Side ID
    if (projectsWithClientId.length === 1 && projectsWithoutClientId.length === 0) {
      const project = projectsWithClientId[0];
      const clientWorkingHours = this.calculateClientWorkingHours(project);
      const projectActivityHours = project.activities.reduce((sum, a) => sum + (a.completionTime || 0), 0);
      
      if (projectActivityHours > clientWorkingHours) {
        return {
          valid: false,
          message: `Total Activity Hours (${projectActivityHours.toFixed(2)} hrs) cannot exceed Client Working Hours (${clientWorkingHours.toFixed(2)} hrs) !!`
        };
      }
      
      return { valid: true, message: '' };
    }
    
    return { valid: true, message: '' };
  }

  calculateProjectWorkingHours(project: ProjectEntry): number {
    if (!project.officeInTime || !project.officeOutTime) return 0;
    const inTime = moment(project.officeInTime);
    const outTime = moment(project.officeOutTime);
    const diffHours = outTime.diff(inTime, 'hours', true);
    project.totalWorkingHours = diffHours.toFixed(2);
    return diffHours;
  }

  calculateClientWorkingHours(project: ProjectEntry): number {
    if (!project.clientInTime || !project.clientOutTime || !this.fromDate) {
      project.totalClientWorkingHours = '0';
      return 0;
    }
    
    try {
      let inTime: moment.Moment;
      let outTime: moment.Moment;
      
      // Handle different time formats
      const parseTime = (timeValue: any, dateValue: any): moment.Moment => {
        // If it's already a Date object or ISO string
        if (timeValue instanceof Date || (typeof timeValue === 'string' && timeValue.includes('T'))) {
          return moment(timeValue);
        }
        
        // If it's a time string like "14:06" or "14:06:00"
        if (typeof timeValue === 'string') {
          const dateStr = moment(dateValue).format('YYYY-MM-DD');
          // Handle both "HH:mm" and "HH:mm:ss" formats
          const timeStr = timeValue.length <= 5 ? timeValue : timeValue.substring(0, 5);
          return moment(`${dateStr} ${timeStr}`, 'YYYY-MM-DD HH:mm');
        }
        
        // Fallback
        return moment(timeValue);
      };
      
      inTime = parseTime(project.clientInTime, this.fromDate);
      outTime = parseTime(project.clientOutTime, this.fromDate);
      
      // Validate parsed times
      if (!inTime.isValid() || !outTime.isValid()) {
        console.warn('Invalid time format:', { inTime: project.clientInTime, outTime: project.clientOutTime });
        project.totalClientWorkingHours = '0';
        return 0;
      }
      
      // If out time is earlier than in time, assume next day (for night shifts)
      if (outTime.isBefore(inTime) || outTime.isSame(inTime)) {
        outTime.add(1, 'day');
      }
      
      const diffHours = outTime.diff(inTime, 'hours', true);
      
      // Ensure non-negative hours
      if (diffHours < 0) {
        project.totalClientWorkingHours = '0';
        return 0;
      }
      
      project.totalClientWorkingHours = diffHours.toFixed(2);
      
      // Also update totalWorkingHours if client times are available
      if (diffHours > 0) {
        project.totalWorkingHours = diffHours.toFixed(2);
      }
      return diffHours;
    } catch (error) {
      console.error('Error calculating client working hours:', error, {
        clientInTime: project.clientInTime,
        clientOutTime: project.clientOutTime,
        fromDate: this.fromDate
      });
      project.totalClientWorkingHours = '0';
      return 0;
    }
  }

  onClientInTimeChange(project: ProjectEntry, time: string): void {
    if (time) {
      project.clientInTime = time;
      if (project.clientOutTime) {
        this.calculateClientWorkingHours(project);
      }
    } else {
      project.clientInTime = null;
      project.totalClientWorkingHours = '0';
    }
  }

  onClientOutTimeChange(project: ProjectEntry, time: string): void {
    if (time) {
      project.clientOutTime = time;
      if (project.clientInTime) {
        this.calculateClientWorkingHours(project);
      }
    } else {
      project.clientOutTime = null;
      project.totalClientWorkingHours = '0';
    }
  }

  shouldShowFileUpload(): boolean {
    // Show file upload if any project has client side ID
    return this.timesheetProjects.some(p => p.clientSideId && p.clientSideId.trim() !== '');
  }

  shouldShowApprovedDoc(): boolean {
    // Show approved doc if client approval status is 'approved'
    return this.timesheetObj.clientApprovalStatus === 'approved';
  }

  openPreviewModal(docType: 'doc1' | 'doc2'): void {
    const file = docType === 'doc1' ? this.selectedFile : this.selectedFile2;
    if (!file) return;
    
    // Create object URL for preview
    const objectUrl = URL.createObjectURL(file);
    window.open(objectUrl, '_blank');
  }

  calculateTimesheetTotalWorkingHours(): number {
    // Sum of all project working hours
    return this.timesheetProjects.reduce((sum, p) => {
      return sum + this.calculateProjectWorkingHours(p);
    }, 0);
  }

  // ========== TIME MANAGEMENT ==========

  getFullDateTime(date: Date | string, hour: string, minute: string, period: string): Date {
    let h = parseInt(hour, 10);
    const m = parseInt(minute, 10);

    if (period === 'PM' && h < 12) h += 12;
    if (period === 'AM' && h === 12) h = 0;

    const dateObj = typeof date === 'string' ? new Date(date) : date;
    const newDate = new Date(dateObj);
    newDate.setHours(h, m, 0, 0);

    return newDate;
  }

  makeProjectOfficeInTime(project: ProjectEntry): void {
    try {
      if (this.fromDate && project.selectedInHour && project.selectedInMinute && project.selectedInPeriod) {
        const officeInTime = this.getFullDateTime(
          this.fromDate,
          project.selectedInHour,
          project.selectedInMinute,
          project.selectedInPeriod
        );
        project.officeInTime = officeInTime.toISOString();
        this.calculateProjectWorkingHours(project);
      } else {
        project.officeInTime = null;
      }

      // Per-project sync
      if (project['syncTimes']) {
        project.selectedClientInHour = project.selectedInHour;
        project.selectedClientInMinute = project.selectedInMinute;
        project.selectedClientInPeriod = project.selectedInPeriod;
        this.makeProjectClientInTime(project);
      }
    } catch (error) {
      console.error(error);
      alert('Selected time cannot be greater than the current time for today.');
      project.selectedInHour = null;
      project.selectedInMinute = null;
      project.selectedInPeriod = null;
      project.officeInTime = null;
    }
  }

  makeProjectOfficeOutTime(project: ProjectEntry): void {
    try {
      const outDate = this.toDate || this.fromDate;
      if (outDate && project.selectedOutHour && project.selectedOutMinute && project.selectedOutPeriod) {
        const officeOutTime = this.getFullDateTime(
          outDate,
          project.selectedOutHour,
          project.selectedOutMinute,
          project.selectedOutPeriod
        );
        project.officeOutTime = officeOutTime.toISOString();
        this.calculateProjectWorkingHours(project);
      } else {
        project.officeOutTime = null;
      }

      // Per-project sync
      if (project['syncTimes']) {
        project.selectedClientOutHour = project.selectedOutHour;
        project.selectedClientOutMinute = project.selectedOutMinute;
        project.selectedClientOutPeriod = project.selectedOutPeriod;
        this.makeProjectClientOutTime(project);
      }
    } catch (error) {
      console.error(error);
      alert('Selected time cannot be greater than the current time for today.');
      project.selectedOutHour = null;
      project.selectedOutMinute = null;
      project.selectedOutPeriod = null;
      project.officeOutTime = null;
    }
  }

  makeProjectClientInTime(project: ProjectEntry): void {
    try {
      if (this.fromDate && project.selectedClientInHour && project.selectedClientInMinute && project.selectedClientInPeriod) {
        const clientInTime = this.getFullDateTime(
          this.fromDate,
          project.selectedClientInHour,
          project.selectedClientInMinute,
          project.selectedClientInPeriod
        );
        project.clientInTime = clientInTime.toISOString();
        this.calculateClientWorkingHours(project);
      } else {
        project.clientInTime = null;
      }
    } catch (error) {
      console.error(error);
      alert('Selected time cannot be greater than the current time for today.');
      project.selectedClientInHour = null;
      project.selectedClientInMinute = null;
      project.selectedClientInPeriod = null;
      project.clientInTime = null;
    }
  }

  makeProjectClientOutTime(project: ProjectEntry): void {
    try {
      const outDate = this.toDate || this.fromDate;
      if (outDate && project.selectedClientOutHour && project.selectedClientOutMinute && project.selectedClientOutPeriod) {
        const clientOutTime = this.getFullDateTime(
          outDate,
          project.selectedClientOutHour,
          project.selectedClientOutMinute,
          project.selectedClientOutPeriod
        );
        project.clientOutTime = clientOutTime.toISOString();
        this.calculateClientWorkingHours(project);
      } else {
        project.clientOutTime = null;
      }
    } catch (error) {
      console.error(error);
      alert('Selected time cannot be greater than the current time for today.');
      project.selectedClientOutHour = null;
      project.selectedClientOutMinute = null;
      project.selectedClientOutPeriod = null;
      project.clientOutTime = null;
    }
  }

  onProjectSyncToggle(project: ProjectEntry, event: any): void {
    project['syncTimes'] = event.target.checked;
    if (project['syncTimes']) {
      project.selectedClientInHour = project.selectedInHour;
      project.selectedClientInMinute = project.selectedInMinute;
      project.selectedClientInPeriod = project.selectedInPeriod;
      project.selectedClientOutHour = project.selectedOutHour;
      project.selectedClientOutMinute = project.selectedOutMinute;
      project.selectedClientOutPeriod = project.selectedOutPeriod;
      this.makeProjectClientInTime(project);
      this.makeProjectClientOutTime(project);
    }
  }

  // ========== PROJECT SELECTION ==========

  onProjectSelect(project: ProjectEntry, projectId: number): void {
    project.projectId = projectId;
    
    const selectedProject = this.activeProjectList.find(p => p.projectId === projectId);
    if (selectedProject) {
      project.projectName = selectedProject.projectName;
      
      // Determine which employee ID to use for fetching client details
      const empIdToUse = project.isShadowTimesheet && project.shadowEmpId 
        ? project.shadowEmpId 
        : (this.timesheetObj.empId || this.currentUser?.empId);
      
      // Set client side ID from project list if available
      if (selectedProject.clientSideId) {
        project.clientSideId = selectedProject.clientSideId;
        project.hasClientSideId = true;
      } else {
        // Try to get from API if not in list - use appropriate empId
        this.getClientSideIdByProjectIdAndEmpId(projectId, empIdToUse)
          .then(clientSideId => {
            project.clientSideId = clientSideId;
            project.hasClientSideId = !!clientSideId;
          });
      }
    }
    
    // Load activities for this project
    this.loadActivitiesForProject(project);
    
    // Load clients for this project
    this.loadClientsForProject(project);
    
    // Load employee list if shadow is enabled
    if (project.isShadowTimesheet && project.projectId) {
      this.loadEmployeeListForProject(project);
    }
  }

  onShadowToggleChange(project: ProjectEntry): void {
    if (project.isShadowTimesheet) {
      // When shadow is enabled, load employee list for the project
      if (project.projectId) {
        this.loadEmployeeListForProject(project);
      } else {
        // If project not selected, show warning
        alert('Please select a project first before enabling shadow timesheet');
        project.isShadowTimesheet = false;
      }
    } else {
      // When shadow is disabled, clear shadow employee ID
      project.shadowEmpId = null;
    }
  }

  loadEmployeeListForProject(project: ProjectEntry): void {
    if (!project.projectId) return;
    
    this.timesheetService.getEmployeeListByProjectId(project.projectId, this.currentUser?.empId)
      .pipe(first())
      .subscribe((response: any) => {
        if (response.serviceStatus === "Success") {
          this.employeeList = response.serviceResponse || [];
        } else {
          console.error('Failed to load employee list:', response.serviceResponse);
          this.employeeList = [];
        }
      }, (error) => {
        console.error('Error loading employee list:', error);
        this.employeeList = [];
      });
  }

  async getClientSideIdByProjectIdAndEmpId(projectId: number, empId: number): Promise<string> {
    return new Promise((resolve) => {
      this.timesheetService.getClientSideIdByProjectIdAndEmpId(projectId, empId)
        .pipe(first())
        .subscribe((response: any) => {
          if (response.serviceStatus === "Success") {
            resolve(response.serviceResponse?.clientSideId || '');
          } else {
            resolve('');
          }
        });
    });
  }

  loadActivitiesForProject(project: ProjectEntry): void {
    if (!project.projectId) return;
    
    const timesheetObj = new Timesheet();
    timesheetObj.projectId = project.projectId;
    timesheetObj.empId = this.timesheetObj.empId || this.currentUser.empId;
    timesheetObj.shadowEmpId = project.shadowEmpId || this.timesheetObj.shadowEmpId;
    
    this.timesheetService.getAllActivitiesByProjectIdandEmpId(timesheetObj)
      .pipe(first())
      .subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          let allActivityList = response.serviceResponse || [];
          allActivityList = allActivityList.sort((a, b) => a.activity.localeCompare(b.activity));
          
          // Filter by department
          if (this.timesheetObj.timesheetAppliedFor == "team") {
            const teamMember = this.teamMemberList.find(emp => emp.empId == this.timesheetObj.empId);
            if (teamMember) {
              allActivityList = allActivityList.filter(x => 
                x.departmentList?.map(d => +d).includes(teamMember.departmentId)
              );
            }
          } else {
            allActivityList = allActivityList.filter(x => 
              x.departmentList?.map(d => +d).includes(this.currentUser.departmentId)
            );
          }
          
          project.availableActivities = allActivityList;
          
          // Set activities for each activity in project
          project.activities.forEach(activity => {
            activity.projectActivities = allActivityList;
          });
        }
      });
  }

  loadClientsForProject(project: ProjectEntry): void {
    if (!project.projectId) return;
    
    // Filter clients by project
    project.clientList = this.clientList.filter(c => c.projectId === project.projectId);
  }

  // ========== ACTIVITY MANAGEMENT ==========

  onClientSelect(project: ProjectEntry, activity: ActivityNew, clientId: number): void {
    activity.clientId = clientId;
    this.getClientLocationListForProject(project, activity, clientId);
  }

  getClientLocationListForProject(project: ProjectEntry, activity: ActivityNew, clientId: number): void {
    const client = project.clientList?.find(c => c.clientId === clientId);
    if (client && client.clientLocations) {
      activity.clientLocationList = client.clientLocations;
    } else {
      activity.clientLocationList = [];
    }
  }

  onClientLocationSelect(project: ProjectEntry, activity: ActivityNew, clientLocationId: number): void {
    activity.clientLocationId = clientLocationId;
    this.getProjectListForActivity(project, activity);
  }

  getProjectListForActivity(project: ProjectEntry, activity: ActivityNew): void {
    // Filter projects/teams by client and client location
    activity.projectList = this.allProjectsList
      .filter((p: any) => p.clientId === activity.clientId && p.clientLocationId === activity.clientLocationId)
      .map((p: any) => ({
        teamId: p.teamId,
        teamName: p.teamName,
        projectName: p.projectName,
        displayTeam: `${p.projectName} | ${p.teamName}`
      }));
  }

  onTeamSelect(project: ProjectEntry, activity: ActivityNew, teamId: number): void {
    activity.teamId = teamId;
    this.getAllActivitiesByProjectIdandEmpId(project, activity, teamId);
  }

  getAllActivitiesByProjectIdandEmpId(project: ProjectEntry, activity: ActivityNew, teamId?: number): void {
    const timesheetObj = new Timesheet();
    timesheetObj.empId = this.timesheetObj.empId || this.currentUser.empId;
    timesheetObj.teamId = teamId || activity.teamId;
    
    const projectTimesheet = this.allProjectsList.find((p: any) => p.teamId == timesheetObj.teamId);
    if (projectTimesheet) {
      timesheetObj.projectId = projectTimesheet.projectId;
    }
    
    this.timesheetService.getAllActivitiesByProjectIdandEmpId(timesheetObj)
      .pipe(first())
      .subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          let allActivityList = response.serviceResponse || [];
          allActivityList = allActivityList.sort((a, b) => a.activity.localeCompare(b.activity));
          
          // Filter by department
          if (this.timesheetObj.timesheetAppliedFor == "team") {
            const teamMember = this.teamMemberList.find(emp => emp.empId == this.timesheetObj.empId);
            if (teamMember) {
              allActivityList = allActivityList.filter(x => 
                x.departmentList?.map(d => +d).includes(teamMember.departmentId)
              );
            }
          } else {
            allActivityList = allActivityList.filter(x => 
              x.departmentList?.map(d => +d).includes(this.currentUser.departmentId)
            );
          }
          
          activity.projectActivities = allActivityList;
        }
      });
  }

  // ========== CREATE TIMESHEET ==========

  onCreateTimesheet(template: TemplateRef<any>): void {
    const dateFormat = 'YYYY-MM-DD';
    const dateTimeFormat = 'YYYY-MM-DD HH:mm:ss';

    this.timesheetObj.description = this.timesheetObj.description?.trim();

    // Validate
    let inputValidated: boolean = this.validateMultiProjectTimesheet(template);
    if (!inputValidated) return;

    // Prepare request DTO
    const createRequest: any = {
      empId: this.timesheetObj.empId || this.currentUser.empId,
      date: moment(this.fromDate || this.timesheetObj.date).format(dateFormat),
      dayTypeId: this.getDayTypeId(this.timesheetObj.dayType),
      dayType: this.timesheetObj.dayType,
      status: this.getPendingStatusId(),
      description: this.timesheetObj.description,
      leaveTypeMasterId: this.timesheetObj.leaveTypeMasterId,
      timesheetAppliedFor: this.timesheetObj.timesheetAppliedFor,
      currentManagerId: this.getCurrentManagerId(),
      isNightShift: this.timesheetObj.isNightShift,
      createdBy: this.currentUser.empId,
      createdByName: this.currentUser.name,
      clientApprovalStatus: this.timesheetClientApprovalStatus, // Per-timesheet
      
      // Transform project entries
      projectEntries: this.timesheetProjects.map(project => ({
        projectId: project.projectId,
        clientSideId: project.clientSideId,
        officeInTime: project.officeInTime ? moment(project.officeInTime).format(dateTimeFormat) : null,
        officeOutTime: project.officeOutTime ? moment(project.officeOutTime).format(dateTimeFormat) : null,
        clientInTime: project.clientInTime ? moment(project.clientInTime).format(dateTimeFormat) : null,
        clientOutTime: project.clientOutTime ? moment(project.clientOutTime).format(dateTimeFormat) : null,
        clientApprovalStatus: this.timesheetClientApprovalStatus, // Same as timesheet level
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

    // Call API
    this.timesheetService.addTimesheetWithClientNew(createRequest, this.selectedFile, this.selectedFile2)
      .pipe(first())
      .subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.resetTimesheetForm();
          this.openAlertMod(template, response.serviceResponse);
          this.timesheetCreated.emit(response);
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }
      });
  }

  // ========== HELPER FUNCTIONS ==========

  hoursToMinutes(hours: number): number {
    return Math.round(hours * 60);
  }

  getDayTypeId(dayType: string): number {
    const dayTypeObj = this.allDayTypes.find(dt => dt.dayType === dayType);
    return dayTypeObj ? dayTypeObj.dayTypeId : null;
  }

  getPendingStatusId(): number {
    // Get from status master - placeholder
    return 1;
  }

  getCurrentManagerId(): number {
    if (this.currentUser.approvalsTo == 'Reporting Manager') {
      return this.currentUser.reportingManagerId;
    } else if (this.currentUser.approvalsTo == 'Manager') {
      return this.currentUser.managerId;
    } else {
      return this.currentUser.managerId;
    }
  }

  // ========== API CALLS ==========

  getAllDayTypes() {
    this.timesheetService.getAllDayTypes().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDayTypes = response.serviceResponse;
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  getEmployeeBasicInfo() {
    // Get employee basic info if needed
  }

  getAllTeamMemberList() {
    this.resetTimesheetFormForAutoFill();
    this.teamMemberList = [];
    
    if (this.timesheetObj.timesheetAppliedFor == "team") {
      this.errorMsg = '';
      let employeeObj = new Employee();
      employeeObj.empId = this.currentUser.empId;
      this.teamViewService.getAllTeamMemberView(employeeObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.teamMemberList = response.serviceResponse;
        } else {
          console.error(response.serviceResponse);
        }
      });
    }
  }

  getActiveProjectsAndClientSideIdByEmpId() {
    const empId = this.timesheetObj.empId || (this.currentUser && this.currentUser.empId);
    
    if (!empId) {
      console.error('Employee ID is not available');
      return;
    }
    
    this.timesheetService.getActiveProjectsAndClientSideIdByEmpId(empId)
      .pipe(first())
      .subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          // The API returns a list of ProjectClientSideIdDTO directly
          this.activeProjectList = response.serviceResponse || [];
          // Extract unique clients from projects if needed
          if (this.activeProjectList && this.activeProjectList.length > 0) {
            // If we need clientList, we can extract it from projects
            // For now, set allProjectsList to the same as activeProjectList
            this.allProjectsList = this.activeProjectList;
          }
          console.log('Projects loaded:', this.activeProjectList);
          console.log('Response structure:', response);
        } else {
          console.error('Failed to load projects:', response.serviceResponse);
        }
      }, (error) => {
        console.error('Error loading projects:', error);
      });
  }

  getTimesheetMetadata(event?: any) {
    if (event && event.target) {
      this.timesheetObj.empId = event.target.value;
    }
    this.getActiveProjectsAndClientSideIdByEmpId();
  }

  onTimesheetDescriptionChange() {
    // Handle description change
  }

  // ========== FORM FLOW HANDLERS ==========

  onDayTypeChange(): void {
    // Load projects when day type is selected
    if (this.timesheetObj.dayType) {
      this.getActiveProjectsAndClientSideIdByEmpId();
    }
    // Reset projects if day type changes
    if (this.requiresTimeAndProjectInputs(this.timesheetObj.dayType)) {
      // Keep projects if it's a working day
    } else {
      // Clear projects for non-working days
      this.timesheetProjects = [];
    }
  }


  onNightShiftChange(): void {
    if (this.timesheetObj.isNightShift) {
      // Open confirmation modal
      this.modalRef = this.modalService.open(this.night_shift_template, { centered: true });
    } else {
      // Clear toDate when night shift is disabled
      this.toDate = null;
      this.recalculateAllTimes();
    }
  }

  confirmNightShift(confirmed: boolean): void {
    if (this.modalRef) {
      this.modalRef.close();
    }
    if (!confirmed) {
      this.timesheetObj.isNightShift = false;
      this.toDate = null;
    }
    this.recalculateAllTimes();
  }

  onOfficeInTimeChange(time: string): void {
    this.timesheetObj.inTime = time;
    this.calculateOfficeWorkingHours();
  }

  onOfficeOutTimeChange(time: string): void {
    this.timesheetObj.outTime = time;
    this.calculateOfficeWorkingHours();
  }

  calculateOfficeWorkingHours(): void {
    if (!this.timesheetObj.inTime || !this.timesheetObj.outTime || !this.fromDate) {
      this.timesheetObj.totalWorkingOfficeHours = '0';
      return;
    }

    try {
      const dateStr = moment(this.fromDate).format('YYYY-MM-DD');
      const inTimeStr = `${dateStr} ${this.timesheetObj.inTime}`;
      const outTimeStr = `${dateStr} ${this.timesheetObj.outTime}`;
      
      let inTime = moment(inTimeStr, 'YYYY-MM-DD HH:mm');
      let outTime = moment(outTimeStr, 'YYYY-MM-DD HH:mm');
      
      // Handle night shift (out time on next day)
      if (this.timesheetObj.isNightShift && this.toDate) {
        const toDateStr = moment(this.toDate).format('YYYY-MM-DD');
        outTime = moment(`${toDateStr} ${this.timesheetObj.outTime}`, 'YYYY-MM-DD HH:mm');
      }
      
      // If out time is before in time and not night shift, assume next day
      if (outTime.isBefore(inTime) && !this.timesheetObj.isNightShift) {
        outTime.add(1, 'day');
      }
      
      const diffHours = outTime.diff(inTime, 'hours', true);
      this.timesheetObj.totalWorkingOfficeHours = diffHours > 0 ? diffHours.toFixed(2) : '0';
    } catch (error) {
      console.error('Error calculating office working hours:', error);
      this.timesheetObj.totalWorkingOfficeHours = '0';
    }
  }

  recalculateAllTimes(): void {
    this.calculateOfficeWorkingHours();
    this.timesheetProjects.forEach(project => {
      if (project.clientInTime && project.clientOutTime) {
        this.calculateClientWorkingHours(project);
      }
    });
  }

  onShadowEmployeeChange(project: ProjectEntry): void {
    if (project.shadowEmpId && project.projectId) {
      this.getClientSideIdByProjectIdAndEmpId(project.projectId, project.shadowEmpId)
        .then(clientSideId => {
          project.clientSideId = clientSideId;
          project.hasClientSideId = !!clientSideId;
        });
    }
  }

  shouldShowClientApprovalSection(): boolean {
    // Show if any project has client side ID
    return this.timesheetProjects.some(p => p.clientSideId && p.clientSideId.trim() !== '');
  }

  get clientApprovalOptions(): any[] {
    return [
      { value: 'no', label: 'Not Filled' },
      { value: 'pending', label: 'Filled' },
      { value: 'approved', label: 'Approved' }
    ];
  }

  onClientApprovalStatusChange(): void {
    // Clear documents when status changes
    if (this.timesheetObj.clientApprovalStatus === 'no') {
      this.selectedFile = null;
      this.selectedFile2 = null;
      this.fileName1 = null;
      this.fileName2 = null;
      this.previewUrl1 = null;
      this.previewUrl2 = null;
    } else if (this.timesheetObj.clientApprovalStatus === 'pending') {
      // Clear approved doc when status is only filled
      this.selectedFile2 = null;
      this.fileName2 = null;
      this.previewUrl2 = null;
    }
  }

  showWorkingHoursError(): boolean {
    if (!this.timesheetObj.dayType || !this.requiresTimeAndProjectInputs(this.timesheetObj.dayType)) {
      return false;
    }
    const totalHours = parseFloat(this.timesheetObj.totalWorkingOfficeHours || '0');
    const minHours = this.getMinRequiredHours(this.timesheetObj.dayType);
    return totalHours > 0 && totalHours < minHours;
  }

  canSubmitTimesheet(): boolean {
    // Basic validations
    if (!this.timesheetObj.dayType || !this.fromDate) {
      return false;
    }

    const dayTypeId = this.timesheetObj.dayType;
    
    // For non-working days, only need description
    if (this.isNonWorkingDay(dayTypeId)) {
      return !!this.timesheetObj.description;
    }

    // For working days
    if (!this.requiresTimeAndProjectInputs(dayTypeId)) {
      return false;
    }

    // Check minimum working hours
    const totalHours = parseFloat(this.timesheetObj.totalWorkingOfficeHours || '0');
    const minHours = this.getMinRequiredHours(dayTypeId);
    if (totalHours < minHours) {
      return false;
    }

    // Check projects
    if (this.timesheetProjects.length === 0) {
      return false;
    }

    // Validate all projects have required fields
    for (const project of this.timesheetProjects) {
      if (!project.projectId) return false;
      if (project.isShadowTimesheet && !project.shadowEmpId) return false;
      
      // Check activities
      if (project.activities.length === 0) return false;
      for (const activity of project.activities) {
        if (!activity.activityId || !activity.completionTime || activity.completionTime <= 0) {
          return false;
        }
      }
    }

    // Check document requirements
    if (this.shouldShowClientApprovalSection()) {
      if (!this.timesheetObj.clientApprovalStatus || this.timesheetObj.clientApprovalStatus === 'no') {
        return false;
      }
      
      if (this.timesheetObj.clientApprovalStatus === 'pending' && !this.selectedFile) {
        return false;
      }
      
      if (this.timesheetObj.clientApprovalStatus === 'approved' && (!this.selectedFile || !this.selectedFile2)) {
        return false;
      }
    }

    return true;
  }

  onCancel(): void {
    this.cancel.emit();
  }

  // ========== FORM RESET ==========

  resetTimesheetFormForAutoFill() {
    this.timeReset();
    this.fromDate = null;
    this.toDate = null;
    this.timesheetProjects = [];
    this.addProject();
    this.timesheetObj.clientApprovalStatus = '';
    this.timesheetClientApprovalStatus = '';
    this.timesheetObj.dayType = '';
    this.timesheetObj.date = '';
    this.timesheetObj.description = '';
    this.selectedFile = null;
    this.selectedFile2 = null;
  }

  timeReset() {
    // Reset all time pickers for all projects
    this.timesheetProjects.forEach(project => {
      project.selectedInHour = null;
      project.selectedInMinute = null;
      project.selectedInPeriod = null;
      project.selectedOutHour = null;
      project.selectedOutMinute = null;
      project.selectedOutPeriod = null;
      project.selectedClientInHour = null;
      project.selectedClientInMinute = null;
      project.selectedClientInPeriod = null;
      project.selectedClientOutHour = null;
      project.selectedClientOutMinute = null;
      project.selectedClientOutPeriod = null;
      project.officeInTime = null;
      project.officeOutTime = null;
      project.clientInTime = null;
      project.clientOutTime = null;
    });
  }

  resetTimesheetForm() {
    this.resetTimesheetFormForAutoFill();
    this.timesheetObj = new Timesheet();
    this.timesheetObj.timesheetAppliedFor = 'self';
    this.timesheetObj.empId = this.currentUser.empId;
  }

  // ========== MODAL & ALERT ==========

  openAlertMod(template: TemplateRef<any>, message: string) {
    this.alertMessage = message;
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
  }

  cancelRequest() {
    if (this.modalRef) {
      this.modalRef.close();
    }
    this.cancel.emit();
  }

  onDateChange(val: string) {
    this.fromDate = val;
    this.timesheetObj.date = val;
    this.onFromDateChange();
  }

  onFromDateChange(date?: any): void {
    if (date) {
      this.fromDate = date;
    }
    this.timesheetObj.date = this.fromDate;
    this.toDate = null;
    if (this.fromDate && this.timesheetObj.isNightShift) {
      const nextDate = new Date(this.fromDate);
      nextDate.setDate(nextDate.getDate() + 1);
      this.maxToDate = nextDate;
      this.toDate = nextDate.toISOString().split('T')[0];
    } else if (!this.timesheetObj.isNightShift) {
      this.maxToDate = null;
      this.toDate = null;
    }
    
    // Recalculate all times
    this.recalculateAllTimes();
  }

  openNightShiftTemplate(template: TemplateRef<any>, event: any) {
    if (event.target.checked) {
      this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    } else {
      this.toDate = null;
      this.maxToDate = null;
      this.onFromDateChange();
    }
  }

  onTimesheetAppliedForChange(value: string): void {
    this.timesheetObj.timesheetAppliedFor = value;

    if (value === 'self') {
      this.getAllTeamMemberList();
      this.timesheetObj.empId = this.currentUser?.empId;
      this.getActiveProjectsAndClientSideIdByEmpId();
    } else if (value === 'team') {
      this.resetTimesheetFormForAutoFill();
      this.getAllTeamMemberList();
      // Projects will be loaded when team member is selected via getTimesheetMetadata
    }
    // Note: Shadow timesheet is now handled per-project via toggle in project section
  }

  // ========== VALIDATION HELPERS ==========

  validateDescription(event: any, activity: ActivityNew): void {
    const input = event.target.value;
    const sanitizedValue = this.inputValidationService.validateInput(input, 'Description');
    activity.description = sanitizedValue;
    event.target.value = sanitizedValue;
  }

  validateTime(event: any, activity: ActivityNew, project: ProjectEntry): void {
    const data = activity.completionTime;
    let errorMsg = '';

    if (!this.validationService.validateTimesheetCompletionTime(data?.toString())) {
      errorMsg = "Please enter Time !!";
    } else if (!this.validationService.validateExperiencedNumber(data?.toString())) {
      errorMsg = "Please enter Valid Time !!";
    } else if (data <= 0 || data > 24) {
      errorMsg = "Total Time Must be greater than 0 hrs and maximum upto 24 hrs!! ";
    } else {
      const projectWorkingHours = this.calculateProjectWorkingHours(project);
      if (data > projectWorkingHours) {
        errorMsg = `Activity time cannot exceed project working hours (${projectWorkingHours.toFixed(2)} hrs) !!`;
      }
    }

    if (errorMsg) {
      event.target.nextElementSibling.textContent = errorMsg;
    } else {
      event.target.nextElementSibling.textContent = '';
      this.calculateProjectHours(project);
    }
  }

  omit_special_char(event: any): boolean {
    const k = event.charCode;
    if ((k == 43) || (k == 45) || (k == 69) || (k == 101)) {
      return false;
    }
    return true;
  }

  preventScroll(event: WheelEvent): void {
    event.preventDefault();
  }

  // ========== DOCUMENT HANDLING ==========

  onFileSelected(event: any, type: 'doc1' | 'doc2'): void {
    const file = event.target.files[0];
    if (!file) return;

    // Validate file type
    const allowedTypes = ['application/pdf', 'image/jpeg', 'image/jpg', 'image/png'];
    if (!allowedTypes.includes(file.type)) {
      if (type === 'doc1') {
        this.fileError1 = 'Please upload PDF, JPG, JPEG, or PNG file only.';
      } else {
        this.fileError2 = 'Please upload PDF, JPG, JPEG, or PNG file only.';
      }
      return;
    }

    // Validate file size (5MB max)
    const maxSize = 5 * 1024 * 1024; // 5MB
    if (file.size > maxSize) {
      if (type === 'doc1') {
        this.fileError1 = 'File size should not exceed 5MB.';
      } else {
        this.fileError2 = 'File size should not exceed 5MB.';
      }
      return;
    }

    if (type === 'doc1') {
      this.selectedFile = file;
      this.fileName1 = file.name;
      this.fileError1 = '';
      this.createPreview(file, 'doc1');
    } else {
      this.selectedFile2 = file;
      this.fileName2 = file.name;
      this.fileError2 = '';
      this.createPreview(file, 'doc2');
    }
  }

  createPreview(file: File, type: 'doc1' | 'doc2'): void {
    const reader = new FileReader();
    reader.onload = (e: any) => {
      const url = e.target.result;
      if (file.type === 'application/pdf') {
        if (type === 'doc1') {
          this.previewUrl1 = this.sanitizer.bypassSecurityTrustResourceUrl(url);
        } else {
          this.previewUrl2 = this.sanitizer.bypassSecurityTrustResourceUrl(url);
        }
      } else {
        if (type === 'doc1') {
          this.previewUrl1 = this.sanitizer.bypassSecurityTrustResourceUrl(url);
        } else {
          this.previewUrl2 = this.sanitizer.bypassSecurityTrustResourceUrl(url);
        }
      }
    };
    reader.readAsDataURL(file);
  }
}
