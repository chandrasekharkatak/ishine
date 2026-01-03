import { Component, Input, OnInit, TemplateRef, ViewChild } from '@angular/core';
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
  @Input() isCreation: boolean = false;
  @Input() isUpdation: boolean = false;
  isTimesheetLockCheckEnable: any = "true";
  timesheetObj: Timesheet = new Timesheet();
  teamMemberList: any[] = [];
  currentUser: User = new User();
  userMapping: any = {};
  serverDate: any;
  errorMsg: string;
  modalRef: NgbModalRef;
  alertMessage: string;
  disableCreateUpdateTimesheet: boolean = false;
  fromDate: any = null;
  toDate: any = null;
  allTimesheetActivities: any[] = [];
  allProjectsList: any[] = [];
  clientList: any[] = [];
  clientLocationList: any[] = [];
  projectList: any[] = [];
  availableTimesheets: any[] = [];
  allDayTypes: any[] = [];
  empClientSideObj: EmployeeClientSideIdMapping = new EmployeeClientSideIdMapping();
  apmosysInTime: any = null;
  apmosysOutTime: any = null;
  clientInTime: any = null;
  clientOutTime: any = null;
  totalWorkingHours: number = 0; // Total working hours in decimal format
  activeProjectList: any[] = []; // Unique projects for dropdown
  // Multi-project support
  timesheetProjects: ProjectEntry[] = [];
  expandedProjectIndex: number | null = 0;
  empHasClientSideId: boolean = false;
  projectActivityHoursError: { [projectIndex: number]: string } = {}; // Store validation errors per project
  
  // File upload properties
  selectedFile: File | null = null;
  selectedFile2: File | null = null;
  previewUrl1: SafeResourceUrl | null = null;
  previewUrl2: SafeResourceUrl | null = null;
  rawObjectUrl1: string | null = null;
  rawObjectUrl2: string | null = null;
  fileError1: string = '';
  fileError2: string = '';
  fileName1: string = '';
  fileName2: string = '';
  activePreviewUrl: SafeResourceUrl | null = null;
  activeFileType: string | null = null;
  createOrUpdateObj:EmployeeTimesheetDTO=new EmployeeTimesheetDTO();
clientApprovalStatusList: any[];
  
  constructor(private teamViewService: TeamViewService,
    private timesheetService: TimesheetService,private modalService: NgbModal,
    private authenticationService: AuthenticationService,
    private sanitizer: DomSanitizer) 
    {this.authenticationService.currentUser.subscribe(x => this.currentUser = x);    }

  ngOnInit(): void {
    // Initialize with one project
    this.addProject();
    this.getAllDayTypes();
    
    // Load projects when currentUser is available
    if (this.currentUser && this.currentUser.empId) {
      this.getAllProjectsByEmpId(this.currentUser.empId);
    } else {
      // Wait for currentUser to be available
      this.authenticationService.currentUser.pipe(first()).subscribe(user => {
        if (user && user.empId) {
          this.currentUser = user;
          this.getAllProjectsByEmpId(user.empId);
        }
      });
    }
  }

  /**
   * Create a new ActivityNew object
   */
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

  /**
   * Create a new ProjectEntry object
   */
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

  /**
   * Add a new project to the timesheet
   */
  addProject(): void {
    this.timesheetProjects.push(this.createProject());
    // Expand the newly added project
    this.expandedProjectIndex = this.timesheetProjects.length - 1;
  }

  /**
   * Remove a project from the timesheet
   */
  removeProject(index: number): void {
    if (this.timesheetProjects.length <= 1) {
      this.openAlertMod(this.alertTemplate, 'At least one project is required.');
      return;
    }
    this.timesheetProjects.splice(index, 1);
    // Adjust expanded index if needed
    if (this.expandedProjectIndex === index) {
      this.expandedProjectIndex = this.timesheetProjects.length > 0 ? 0 : null;
    } else if (this.expandedProjectIndex > index) {
      this.expandedProjectIndex--;
    }
  }

  /**
   * Toggle project expansion/collapse
   */
  toggleProject(index: number): void {
    this.expandedProjectIndex = this.expandedProjectIndex === index ? null : index;
  }

  /**
   * Add a new activity to a project
   */
  addActivity(project: ProjectEntry): void {
    project.activities.push(this.createActivity());
  }

  /**
   * Remove an activity from a project
   */
  removeActivity(project: ProjectEntry, index: number): void {
    if (project.activities.length <= 1) {
      this.openAlertMod(this.alertTemplate, 'At least one activity is required per project.');
      return;
    }
    project.activities.splice(index, 1);
  }
    getAllDayTypes(){
    this.timesheetService.getAllDayTypes().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDayTypes = response.serviceResponse;
        } else {
          console.error(response.serviceResponse)
        }
      });
    }
    onDayTypeChange(event: any){
      this.timesheetObj.dayType = event;
      console.log("Day type changed",this.timesheetObj.dayType);
    }
  onTimesheetAppliedForChange(value: string): void {

    this.timesheetObj.timesheetAppliedFor = value;
  
  
  
    if (value === 'self') {
  
      this.getAllTeamMemberList();
  
      this.getTimesheetMetadata();
  
      this.timesheetObj.isShadowTimesheet = false;
  
  
  
    } else if (value === 'asShadow') {
  
      this.resetTimesheetFormForAutoFill();
  
      this.timesheetObj.isShadowTimesheet = true;
  
  
  
    } else {
  
      this.resetTimesheetFormForAutoFill();
  
      this.getAllTeamMemberList();
  
      this.timesheetObj.isShadowTimesheet = false;
  
    }
  
  }
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }
  getTimesheetMetadata(eventTarget?: any) {
    //console.log("timesheet Obj For getTimesheetMetadata : ", this.timesheetObj);

    let userObj: User = new User();
    if (this.timesheetObj.timesheetAppliedFor == 'self') {
      userObj.empId = this.currentUser.empId;
      userObj.isTimesheetLockCheckEnable = this.currentUser.isTimesheetLockCheckEnable;
      this.timesheetObj.empId = this.currentUser.empId;
      this.isTimesheetLockCheckEnable = this.currentUser.isTimesheetLockCheckEnable;


    } else {
      console.log("Team member list is",this.teamMemberList);
      let teamMember = this.teamMemberList.find(employee => employee.empId == this.timesheetObj.empId)
      console.log("Team member is ",teamMember);
      userObj.empId = teamMember?.empId;
      userObj.isTimesheetLockCheckEnable = teamMember.isTimesheetLockCheckEnable;
      this.isTimesheetLockCheckEnable = teamMember.isTimesheetLockCheckEnable;
      this.timesheetObj.empId = teamMember.empId;

      if (teamMember.isTimesheetFilledByMember == "true") {
        this.openAlertMod(this.alertTemplate, "Timesheet cannot be filled for team member more than 2 days.");
        this.timesheetObj.empId = '';
        eventTarget.value = "";
        this.disableCreateUpdateTimesheet = true;
        eventTarget.value = '';



      } else {
        this.disableCreateUpdateTimesheet = false;
      }
    }

    const timesheetBkp = Object.assign({}, this.timesheetObj);

    // reset timesheet
    this.timesheetObj = new Timesheet();
    this.timesheetObj.dayType = '';
    this.allTimesheetActivities = [];
    // this.addInputActivityField()

    // set leave AppliedFor User data to fetch activities for project & for display
    this.timesheetObj.timesheetAppliedFor = timesheetBkp.timesheetAppliedFor;
    this.timesheetObj.empId = timesheetBkp.empId;

    //console.log("preset Timesheet : ", this.timesheetObj);

    this.getAllProjectsByEmpId(userObj);
    this.getAllAvailableTimesheetByEmpId(userObj);
  }

  resetTimesheetFormForAutoFill() {
    this.fromDate = null;
    this.toDate = null;
    this.timesheetObj.projectId = '';
    this.timesheetObj.clientSideId = '';
    this.timesheetObj.hasClientSideId = false;
    this.timesheetObj.shadowEmpId = '';
    // this.timesheetObj.timesheetAppliedFor = '';
    // this.timesheetObj.empId = '';
    this.timesheetObj.employmentId = '';
    this.timesheetObj.clientApprovalStatus = '';
    this.timesheetObj.dayType = '';
    this.timesheetObj.date = '';
    this.timesheetObj.description = '';
    this.timesheetObj.officeInTime = '';
    this.timesheetObj.officeOutTime = '';
    this.timesheetObj.totalWorkingOfficeHours = '';
    this.timesheetObj.isNightShift = '';
    this.timesheetObj.clientInTime = '';
    this.timesheetObj.clientOutTime = '';
    this.timesheetObj.totalClientWorkingHours = '';
    this.timesheetObj.docId = '';
    // this.allTimesheetActivities = [];
  }
  getAllTeamMemberList() {
    console.log("Timesheet object is",this.timesheetObj)
    this.resetTimesheetFormForAutoFill();
    this.teamMemberList = []
    this.timesheetObj.date = ''
    this.timesheetObj.dayType = ''
    this.timesheetObj.officeInTime = ''
    this.timesheetObj.officeOutTime = ''
    this.timesheetObj.totalWorkingOfficeHours = ''
    this.allTimesheetActivities.forEach((timesheet) => {
      timesheet.clientId = ''
      timesheet.clientLocationId = ''
      timesheet.teamId = ''
      timesheet.activityId = ''
      timesheet.description = ''
      timesheet.completionTime = ''
    })

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
    console.log("Timesheet object is",this.timesheetObj)
  }

  getAllProjectsByEmpId(empId:any) {
    this.allProjectsList = [];
    this.clientList = [];
    this.clientLocationList = [];
    this.projectList = [];
    this.activeProjectList = [];

    let timesheetObj = new Timesheet();
    timesheetObj.empId = empId;
    this.timesheetService.getAllProjectsByEmpId(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allProjectsList = response.serviceResponse;
        console.log(this.allProjectsList,"this.allProjectsList")
        if (this.allProjectsList.length == 0) {
        }
        else {
          // Extract unique projects for project dropdown
          const projectKey = "projectId";
          this.activeProjectList = [...new Map(this.allProjectsList.map((project: any) => [project[projectKey], project])).values()].map((project: any) => {
            return { 
              projectId: project.projectId, 
              projectName: project.projectName,
              clientSideId: project.clientSideId
            };
          });
        }
      } else {
        console.error(response.serviceResponse)
        // this.openAlertWithResetMod(this.alertModalWithoutReload, "Please contact the RMG team and set up your default project mapping!");
      }
    });
  }

  /**
   * Handle project selection - populate clients for the selected project
   */
  onProjectSelect(project: ProjectEntry, projectId: any): void {
    if (!projectId) {
      project.projectId = null;
      project.clientList = [];
      return;
    }

    project.projectId = projectId;
    
    // Find selected project details
    const selectedProject = this.activeProjectList.find(p => p.projectId === projectId);
    if (selectedProject) {
      project.projectName = selectedProject.projectName;
      project.clientSideId = selectedProject.clientSideId || '';
      project.hasClientSideId = !!selectedProject.clientSideId;
    }

    // Filter clients for this project from allProjectsList
    const projectClients = this.allProjectsList
      .filter((p: any) => p.projectId === projectId)
      .map((p: any) => ({
        clientId: p.clientId,
        clientName: p.clientName,
        projectId: p.projectId
      }));

    // Remove duplicates based on clientId
    project.clientList = [...new Map(projectClients.map((c: any) => [c.clientId, c])).values()];

    // Clear dependent fields
    project.activities.forEach(activity => {
      activity.clientId = null;
      activity.clientLocationId = null;
      activity.teamId = null;
      activity.activityId = null;
      activity.clientLocationList = [];
      activity.projectList = [];
      activity.projectActivities = [];
    });
    this.getClientSideIdByProjectIdAndEmpId(projectId,this.currentUser.empId);
  }

  async getClientSideIdByProjectIdAndEmpId(projectId: any, empId: any) {
    await this.timesheetService.getClientSideIdByProjectIdAndEmpId(projectId, empId).pipe(first()).toPromise().then((response: any) => {
      if (response.serviceStatus == "Success") {
        this.timesheetObj.clientSideId = response.serviceResponse;
        this.empHasClientSideId = true;
        if (this.timesheetObj.clientSideId) {
          this.empClientSideObj.clientSideId = this.timesheetObj.clientSideId;
          this.empClientSideObj.projectId = projectId;
          this.empClientSideObj.empId = empId;
          this.timesheetProjects.forEach(project => {
            if (project.projectId == projectId) {
              project.clientSideId = this.timesheetObj.clientSideId;
              project.hasClientSideId = true;
            }
          });
        }
      } else {
        console.error(response.serviceResponse);
      }
    });
  }
  /**
   * Handle client selection - populate client locations for the selected client and project
   */
  onClientSelect(project: ProjectEntry, activity: ActivityNew, clientId: number): void {
    if (!clientId) {
      activity.clientId = null;
      activity.clientLocationList = [];
      return;
    }

    activity.clientId = clientId;

    // Filter client locations for this project and client
    const locations = this.allProjectsList
      .filter((p: any) => p.projectId === project.projectId && p.clientId === clientId && p.clientLocationId)
      .map((p: any) => ({
        clientLocationId: p.clientLocationId,
        locationName: p.clientLocation
      }));

    // Remove duplicates
    activity.clientLocationList = [...new Map(locations.map((l: any) => [l.clientLocationId, l])).values()];

    // Clear dependent fields
    activity.clientLocationId = null;
    activity.teamId = null;
    activity.activityId = null;
    activity.projectList = [];
    activity.projectActivities = [];
  }

  /**
   * Handle client location selection - populate teams for the selected location
   */
  onClientLocationSelect(project: ProjectEntry, activity: ActivityNew, clientLocationId: number): void {
    if (!clientLocationId) {
      activity.clientLocationId = null;
      activity.projectList = [];
      return;
    }

    activity.clientLocationId = clientLocationId;

    // Filter teams for this project, client, and client location
    const teams = this.allProjectsList
      .filter((p: any) => 
        p.projectId === project.projectId && 
        p.clientId === activity.clientId && 
        p.clientLocationId === clientLocationId &&
        p.teamId
      )
      .map((p: any) => ({
        teamId: p.teamId,
        teamName: p.teamName,
        projectName: p.projectName,
        displayTeam: `${p.projectName} | ${p.teamName}`
      }));

    // Remove duplicates
    activity.projectList = [...new Map(teams.map((t: any) => [t.teamId, t])).values()];

    // Clear dependent fields
    activity.teamId = null;
    activity.activityId = null;
    activity.projectActivities = [];
  }

  /**
   * Handle team selection - load activities for the selected team
   */
  onTeamSelect(project: ProjectEntry, activity: ActivityNew, teamId: number): void {
    if (!teamId) {
      activity.teamId = null;
      activity.projectActivities = [];
      return;
    }

    activity.teamId = teamId;

    // Load activities - this will need to call the API
    // For now, we'll prepare the data structure
    this.loadActivitiesForTeam(project, activity, teamId);
  }

  /**
   * Load activities for a selected team
   */
  loadActivitiesForTeam(project: ProjectEntry, activity: ActivityNew, teamId: number): void {
    const timesheetObj = new Timesheet();
    timesheetObj.projectId = project.projectId;
    timesheetObj.empId = project.isShadowTimesheet && project.shadowEmpId 
      ? project.shadowEmpId 
      : (this.timesheetObj.empId || this.currentUser?.empId);
    timesheetObj.teamId = teamId;
    timesheetObj.clientId = activity.clientId;
    timesheetObj.clientLocationId = activity.clientLocationId;

    if (!timesheetObj.empId) {
      console.error('Employee ID is not available');
      return;
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
              x.departmentList?.map(d => +d).includes(this.currentUser?.departmentId)
            );
            if (allActivityList.length === 0) {
              this.openAlertMod(this.alertTemplate, "No activity found for your department!");
            }
          }
          
          activity.projectActivities = allActivityList;
        } else {
          console.error('Failed to load activities:', response.serviceResponse);
          activity.projectActivities = [];
        }
      }, (error) => {
        console.error('Error loading activities:', error);
        activity.projectActivities = [];
      });
  }
  getAllAvailableTimesheetByEmpId(employeeObj: User) {
    this.availableTimesheets = [];
    const DAY_IN_MS = 24 * 60 * 60 * 1000;
    let currentDate = new Date();
    const dateFormat = 'DD-MM-YYYY';
    let endDate: any;
    let startDate: any;
    let OPEN_BACKDATED_DAYS = 30;

    if (this.currentUser.timesheetBackDatedDays) {
      OPEN_BACKDATED_DAYS = this.currentUser.timesheetBackDatedDays;
      // console.log("OPEN_BACKDATED_DAYS",this.currentUser.timesheetBackDatedDays);
    }

    if (this.isTimesheetLockCheckEnable == 'false') {
      endDate = currentDate;
      startDate = new Date(endDate.getTime() - ((OPEN_BACKDATED_DAYS + 1) * DAY_IN_MS));
      // startDate=this.currentUser.dateOfJoining;
    } else {
      endDate = currentDate;
      startDate = new Date(endDate.getTime() - ((this.currentUser.timesheetLockDays + 1) * DAY_IN_MS));
    }

    let timesheetObj = new Timesheet();
    timesheetObj.empId = employeeObj.empId;
    timesheetObj.startDate = moment(startDate).format(AppComponent.DB_DATE_FORMAT);
    timesheetObj.endDate = moment(endDate).format(AppComponent.DB_DATE_FORMAT);

    //console.log("getAllMyTimesheetsByEmpId :", timesheetObj);
    this.timesheetService.getAllMyTimesheetsByEmpId(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.availableTimesheets = response.serviceResponse;
      } else {
        console.error(response.serviceResponse)
      }
    });
  }
  onNightShiftChange() {
    if (this.timesheetObj.isNightShift) {
      if (!this.fromDate) {
        this.openAlertMod(
          this.alertTemplate,
          'Please select From Date first before enabling Night Shift.'
        );
        this.timesheetObj.isNightShift = false;
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
      this.totalWorkingHours = 0;
      return;
    }
  
    // Always keep dd-MM-yyyy
    this.timesheetObj.date = this.fromDate;
  
    if (this.timesheetObj.isNightShift) {
      const fromDate = this.parseDDMMYYYY(this.fromDate);
      if (!fromDate) return;
  
      this.toDate = this.formatDDMMYYYY(this.addDays(fromDate, 1));
    }
    
    // Recalculate working hours when date changes
    this.calculateTotalWorkingHours();
  }
  

  onToDateChange() {
  
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
      this.timesheetObj.isNightShift = false;
      this.onNightShiftChange();
    } else {
      // Recalculate working hours when toDate changes
      this.calculateTotalWorkingHours();
    }
  }
  
  
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
  
  formatDDMMYYYY(date: Date): string {
    const dd = String(date.getDate()).padStart(2, '0');
    const mm = String(date.getMonth() + 1).padStart(2, '0');
    const yyyy = date.getFullYear();
  
    return `${dd}-${mm}-${yyyy}`;
  }
  
  addDays(date: Date, days: number): Date {
    const d = new Date(date);
    d.setDate(d.getDate() + days);
    return d;
  }

  /**
   * Calculate total working hours from ApMoSys In Time and Out Time
   */
  calculateTotalWorkingHours(): void {
    // Reset to 0 if required fields are missing
    if (!this.fromDate || !this.apmosysInTime || !this.apmosysOutTime) {
      this.totalWorkingHours = 0;
      return;
    }

    try {
      // Parse fromDate (DD-MM-YYYY format)
      const fromDateParsed = this.parseDDMMYYYY(this.fromDate);
      if (!fromDateParsed) {
        this.totalWorkingHours = 0;
        return;
      }

      // Determine which date to use for out time
      let outDate: Date;
      if (this.timesheetObj.isNightShift && this.toDate) {
        // Use toDate for night shift
        const toDateParsed = this.parseDDMMYYYY(this.toDate);
        if (!toDateParsed) {
          this.totalWorkingHours = 0;
          return;
        }
        outDate = toDateParsed;
      } else {
        // Use fromDate for normal shift
        outDate = fromDateParsed;
      }

      // Parse time strings (HH:mm format)
      const inTimeParts = this.apmosysInTime.split(':');
      const outTimeParts = this.apmosysOutTime.split(':');

      if (inTimeParts.length !== 2 || outTimeParts.length !== 2) {
        this.totalWorkingHours = 0;
        return;
      }

      const inHour = parseInt(inTimeParts[0], 10);
      const inMinute = parseInt(inTimeParts[1], 10);
      const outHour = parseInt(outTimeParts[0], 10);
      const outMinute = parseInt(outTimeParts[1], 10);

      // Create Date objects with date and time
      const inDateTime = new Date(fromDateParsed);
      inDateTime.setHours(inHour, inMinute, 0, 0);

      const outDateTime = new Date(outDate);
      outDateTime.setHours(outHour, outMinute, 0, 0);

      // Calculate difference in milliseconds
      const diffMs = outDateTime.getTime() - inDateTime.getTime();

      // Convert to hours (decimal)
      this.totalWorkingHours = diffMs / (1000 * 60 * 60);

      // Ensure non-negative
      if (this.totalWorkingHours < 0) {
        this.totalWorkingHours = 0;
      }

      // Round to 2 decimal places
      this.totalWorkingHours = Math.round(this.totalWorkingHours * 100) / 100;
    } catch (error) {
      console.error('Error calculating total working hours:', error);
      this.totalWorkingHours = 0;
    }
  }

  /**
   * Handle ApMoSys In Time change
   */
  onApMoSysInTimeChange(time: string): void {
    this.apmosysInTime = time;
    this.calculateTotalWorkingHours();
  }

  /**
   * Handle ApMoSys Out Time change
   */
  onApMoSysOutTimeChange(time: string): void {
    this.apmosysOutTime = time;
    this.calculateTotalWorkingHours();
  }

  /**
   * Handle file selection for document upload
   */
  onFileSelected(event: any, docType: 'doc1' | 'doc2'): void {
    const file: File = event.target.files[0];
    if (!file) return;

    const allowedTypes = ['application/pdf', 'image/jpeg', 'image/png'];
    const maxSize = 300 * 1024; // 300KB

    if (!allowedTypes.includes(file.type)) {
      if (docType === 'doc1') {
        this.fileError1 = 'Only PDF, JPG, JPEG, PNG files allowed.';
        this.openAlertMod(this.alertTemplate, this.fileError1);
      } else {
        this.fileError2 = 'Only PDF, JPG, JPEG, PNG files allowed.';
        this.openAlertMod(this.alertTemplate, this.fileError2);
      }
      // Reset file input
      event.target.value = '';
      return;
    }

    if (file.size > maxSize) {
      if (docType === 'doc1') {
        this.fileError1 = 'File size must be 300KB or less.';
        this.openAlertMod(this.alertTemplate, this.fileError1);
        this.selectedFile = null;
        this.fileName1 = '';
        this.previewUrl1 = null;
        if (this.rawObjectUrl1) {
          URL.revokeObjectURL(this.rawObjectUrl1);
        }
        this.rawObjectUrl1 = null;
      } else {
        this.fileError2 = 'File size must be 300KB or less.';
        this.openAlertMod(this.alertTemplate, this.fileError2);
        this.selectedFile2 = null;
        this.fileName2 = '';
        this.previewUrl2 = null;
        if (this.rawObjectUrl2) {
          URL.revokeObjectURL(this.rawObjectUrl2);
        }
        this.rawObjectUrl2 = null;
      }
      // Reset file input
      event.target.value = '';
      return;
    }

    // Clean up previous object URL if exists
    if (docType === 'doc1' && this.rawObjectUrl1) {
      URL.revokeObjectURL(this.rawObjectUrl1);
    }
    if (docType === 'doc2' && this.rawObjectUrl2) {
      URL.revokeObjectURL(this.rawObjectUrl2);
    }

    // Create object URL and sanitize it
    const objectUrl = URL.createObjectURL(file);
    const previewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(objectUrl);

    if (docType === 'doc1') {
      this.selectedFile = file;
      this.fileName1 = file.name;
      this.previewUrl1 = previewUrl;
      this.rawObjectUrl1 = objectUrl;
      this.fileError1 = '';
    } else {
      this.selectedFile2 = file;
      this.fileName2 = file.name;
      this.previewUrl2 = previewUrl;
      this.rawObjectUrl2 = objectUrl;
      this.fileError2 = '';
    }
  }

  /**
   * Open preview modal for uploaded file
   */
  openPreviewModalForTwo(docType: 'doc1' | 'doc2'): void {
    this.activePreviewUrl = docType === 'doc1' ? this.previewUrl1 : this.previewUrl2;
    this.activeFileType = docType === 'doc1'
      ? (this.selectedFile?.type === 'application/pdf' ? 'pdf' : 'image')
      : (this.selectedFile2?.type === 'application/pdf' ? 'pdf' : 'image');

    this.modalRef = this.modalService.open(this.previewModal, { modalDialogClass: 'modal-lg' });
  }

  /**
   * Show preview for base64 data (for existing documents)
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
  
  createTimesheet() {
    console.log("Create Timesheet clicked");
    let projectDataList : ProjectTimesheetDTO[] = [];
    let projectData : ProjectTimesheetDTO;
    this.timesheetProjects.forEach((project) => {
      projectData = new ProjectTimesheetDTO();
      projectData.projectId = project.projectId;
      // 
      projectData.clientInTime = project.clientInTime;
      projectData.clientOutTime = project.clientOutTime;
      projectData.clientApprovalStatus = this.timesheetObj.clientApprovalStatus;
      projectData.shadowEmpId = project.isShadowTimesheet ? project.shadowEmpId : null;
      // projectData.isShadowTimesheet = project.isShadowTimesheet;
      let activityDataList : ActivityTimesheetDTO[] = [];
      let activityData : ActivityTimesheetDTO;
      project.activities.forEach((activity) => {
        activityData = new ActivityTimesheetDTO();
        // activityData.clientId = activity.clientId;
        activityData.clientLocationId = activity.clientLocationId;
        // activityData.teamId = activity.teamId;
        activityData.activityId = activity.activityId;
        activityData.description = activity.description;
        activityData.durationMinutes = activity.completionTime * 60;
        activityDataList.push(activityData);
      });
      projectData.activities = activityDataList;
      projectDataList.push(projectData);
    });
    // Implementation for creating timesheet goes here 
    this.createOrUpdateObj.empId = this.timesheetObj.empId;
    this.createOrUpdateObj.dayTypeId = this.timesheetObj.dayType;
    this.createOrUpdateObj.date = this.timesheetObj.date;
    this.createOrUpdateObj.totalActivitiesMinutes = this.totalWorkingHours;
    this.createOrUpdateObj.isNightShift = this.timesheetObj.isNightShift;
    this.createOrUpdateObj.description = this.timesheetObj.description;
    this.createOrUpdateObj.projectTimesheets = projectDataList;
    this.createOrUpdateObj.createdBy = this.currentUser.empId;

    console.log("Timesheet to be created:", this.createOrUpdateObj);
    // Call the service to create timesheet


  }
}
