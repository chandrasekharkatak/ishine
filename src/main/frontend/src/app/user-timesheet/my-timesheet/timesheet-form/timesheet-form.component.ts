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
import { TimesheetNewService } from 'src/app/services/timesheet-new.service';
import { TimesheetDocument } from 'src/app/models/timesheetDocument';
import { LocationEntry } from 'src/app/models/locationEntry';

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
  dayType:any;
  timesheetAppliedFor:any='self';
  selectedLocationId: any = null;
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
  totalPresence: number = 0;
  totalWorkingHours: number = 0; // Total working hours in decimal format
  activeProjectList: any[] = []; // Unique projects for dropdown
  activeLocationList: any[] = []; // Unique locations for dropdown
  // Multi-location support (Location -> Project -> Activity)
  uniqueProjectsList: ProjectEntry[] = [];
  timesheetLocations: LocationEntry[] = [];
  expandedLocationIndex: number | null = 0;
  expandedProjectIndexMap: { [locationIndex: number]: number | null } = {}; // Track expanded project per location
  empHasClientSideId: boolean = true;
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
  workLocationList: string[];
  isNightShift: boolean = false;
  documentList: TimesheetDocument[] = [];
  uploadFileList: {
    projectId: number;
    docType: 'Filled' | 'Approved';
    file: File;
    previewUrl: SafeResourceUrl;
    rawObjectUrl: string;
    fileError: string;
    fileType: 'pdf' | 'image';
    fileName: string;
    fileSize: number;
  }[] = [];

  constructor(private teamViewService: TeamViewService,
    private timesheetService: TimesheetService,
    private timesheetNewService: TimesheetNewService,
    private modalService: NgbModal,
    private authenticationService: AuthenticationService,
    private sanitizer: DomSanitizer) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x); }

  ngOnInit(): void {
    // Initialize with one location
    this.addLocation();
    this.getListToRenderUpload();
    this.getAllDayTypes();
    this.getAllWorkLocationFromLocationMaster();
    this.getAllDSRApprovalStatusFromMaster();
    

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
      projectActivities: [] // Will use project.projectActivities instead
    };
  }

  /**
   * Create a new LocationEntry object
   */
  createLocation(): LocationEntry {
    return {
      locationId: null,
      locationName: '',
      clientLocationId: null,
      logInTime: '',
      logOutTime: '',
      totalClientWorkingHours: '0',
      projects: [this.createProject()],
      availableProjects: []
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
      logInTime: '',
      logOutTime: '',
      shadowEmpId: null,
      isShadowTimesheet: false,
      isShadowForSelf: false,
      // Client, Team, Location at project level
      clientId: null,
      clientLocationId: null,
      teamId: null,
      clientApprovalStatus: null,
      activities: [this.createActivity()],
      availableActivities: [],
      projectActivities: [], // Activities loaded for the project
      clientList: [],
      clientLocationList: [],
      projectList: []
    };
  }

  /**
   * Add a new location to the timesheet
   */
  addLocation(): void {
    this.timesheetLocations.push(this.createLocation());
    // Expand the newly added location
    this.expandedLocationIndex = this.timesheetLocations.length - 1;
    // Initialize expanded project index for this location
    this.expandedProjectIndexMap[this.timesheetLocations.length - 1] = 0;
    this.getListToRenderUpload();
  }

  /**
   * Remove a location from the timesheet
   */
  removeLocation(index: number): void {
    if (this.timesheetLocations.length <= 1) {
      this.openAlertMod(this.alertTemplate, 'At least one location is required.');
      return;
    }
    this.timesheetLocations.splice(index, 1);
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
   * Toggle location expansion/collapse
   */
  toggleLocation(index: number): void {
    this.expandedLocationIndex = this.expandedLocationIndex === index ? null : index;
  }

  /**
   * Add a new project to a location
   */
  addProject(location: LocationEntry): void {
    location.projects.push(this.createProject());
    // Expand the newly added project
    const locationIndex = this.timesheetLocations.indexOf(location);
    this.expandedProjectIndexMap[locationIndex] = location.projects.length - 1;
    this.getListToRenderUpload();
  }

  /**
   * Remove a project from a location
   */
  removeProject(location: LocationEntry, projectIndex: number): void {
    if (location.projects.length <= 1) {
      this.openAlertMod(this.alertTemplate, 'At least one project is required per location.');
      return;
    }
    location.projects.splice(projectIndex, 1);
    // Adjust expanded project index if needed
    const locationIndex = this.timesheetLocations.indexOf(location);
    const currentExpanded = this.expandedProjectIndexMap[locationIndex];
    if (currentExpanded === projectIndex) {
      this.expandedProjectIndexMap[locationIndex] = location.projects.length > 0 ? 0 : null;
    } else if (currentExpanded !== null && currentExpanded > projectIndex) {
      this.expandedProjectIndexMap[locationIndex] = currentExpanded - 1;
    }
  }

  /**
   * Toggle project expansion/collapse within a location
   */
  toggleProject(locationIndex: number, projectIndex: number): void {
    const currentExpanded = this.expandedProjectIndexMap[locationIndex];
    this.expandedProjectIndexMap[locationIndex] = currentExpanded === projectIndex ? null : projectIndex;
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

  /**
   * Handle location selection - populate projects for the selected location
   */
  onLocationSelect(location: LocationEntry, locationId: any): void {
    if (!locationId) {
      location.locationId = null;
      location.availableProjects = [];
      return;
    }

    location.locationId = locationId;

    // Find selected location details from allProjectsList
    const locationData = this.allProjectsList.find((p: any) => p.clientLocationId === locationId);
    if (locationData) {
      location.locationName = locationData.clientLocation || '';
      location.clientLocationId = locationData.clientLocationId;
    }

    // Filter projects for this location from allProjectsList
    const locationProjects = this.allProjectsList
      .filter((p: any) => p.clientLocationId === locationId)
      .map((p: any) => ({
        projectId: p.projectId,
        projectName: p.projectName,
        clientSideId: p.clientSideId
      }));

    // Remove duplicates based on projectId
    location.availableProjects = [...new Map(locationProjects.map((p: any) => [p.projectId, p])).values()];

    // Clear dependent fields in all projects
    location.projects.forEach(project => {
      project.projectId = null;
      project.activities.forEach(activity => {
        activity.clientId = null;
        activity.clientLocationId = null;
        activity.teamId = null;
        activity.activityId = null;
        activity.clientLocationList = [];
        activity.projectList = [];
        activity.projectActivities = [];
      });
    });
  }
  getAllDayTypes() {
    this.timesheetNewService.getAllDayTypes().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDayTypes = response.serviceResponse;
      } else {
        console.error(response.serviceResponse)
      }
    }); 
  }
  onDayTypeChange(event: any) {
    this.dayType = event;
    console.log("Day type changed", this.timesheetObj.dayType);
  }
  onTimesheetAppliedForChange(value: string): void {

    this.timesheetAppliedFor = value;
// <<<<<<< Updated upstream

 

//   if (value === 'self') {

// =======
    this.timesheetObj.timesheetAppliedFor = value;
  
  
    if (value.toLocaleLowerCase() === 'self') {
  
// >>>>>>> Stashed changes
      // this.getAllTeamMemberList();

      this.getTimesheetMetadata();

      this.timesheetObj.isShadowTimesheet = false;



    }
    // else if (value === 'asShadow') {

    //   this.resetTimesheetFormForAutoFill();

    //   this.timesheetObj.isShadowTimesheet = true;



    // }
    else {

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
    if (this.timesheetObj.timesheetAppliedFor.toLocaleLowerCase() == 'self') {
      userObj.empId = this.currentUser.empId;
      userObj.isTimesheetLockCheckEnable = this.currentUser.isTimesheetLockCheckEnable;
      this.timesheetObj.empId = this.currentUser.empId;
      this.isTimesheetLockCheckEnable = this.currentUser.isTimesheetLockCheckEnable;


    } else {
      console.log("Team member list is", this.teamMemberList);
      let teamMember = this.teamMemberList.find(employee => employee.empId == this.timesheetObj.empId)
      console.log("Team member is ", teamMember);
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
    // this.fromDate = null;
    // this.toDate = null;
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
    console.log("Timesheet object is", this.timesheetObj)
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


    this.errorMsg = '';
    let employeeObj = new Employee();
    employeeObj.empId = this.currentUser.empId;
    this.teamViewService.getAllTeamMemberView(employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.teamMemberList = response.serviceResponse;
      } else {
        console.error(response.serviceResponse);
        this.errorMsg = response.serviceResponse;
      }
    });

    console.log("Timesheet object is", this.timesheetObj)
  }

  getAllProjectsByEmpId(empId: any) {
    this.allProjectsList = [];
    this.clientList = [];
    this.clientLocationList = [];
    this.projectList = [];
    this.activeProjectList = [];
    this.activeLocationList = [];

    let timesheetObj = new Timesheet();
    timesheetObj.empId = empId;
    this.timesheetNewService.getAllProjectsByEmpId(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allProjectsList = response.serviceResponse;
        console.log(this.allProjectsList, "this.allProjectsList")
        if (this.allProjectsList.length == 0) {
        }
        else {
          // Extract unique locations for location dropdown
          const locationKey = "clientLocationId";
          this.activeLocationList = [...new Map(this.allProjectsList
            .filter((p: any) => p.clientLocationId) // Only include entries with location
            .map((project: any) => [project[locationKey], project])).values()]
            .map((project: any) => {
              return {
                locationId: project.clientLocationId,
                locationName: project.clientLocation
              };
            });

          // Extract unique projects for project dropdown (for reference)
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
   * Handle project selection - populate clients for the selected project within a location
   */
  onProjectSelect(location: LocationEntry, project: ProjectEntry, projectId: any): void {
    if (!projectId) {
      project.projectId = null;
      project.clientList = [];
      return;
    }

    project.projectId = projectId;

    // Find selected project details from location's available projects or allProjectsList
    const selectedProject = location.availableProjects?.find(p => p.projectId === projectId) ||
      this.activeProjectList.find(p => p.projectId === projectId);
    if (selectedProject) {
      project.projectName = selectedProject.projectName;
      project.clientSideId = selectedProject.clientSideId || '';
      project.hasClientSideId = !!selectedProject.clientSideId;
    }

    // Filter clients for this project and location from allProjectsList
    const projectClients = this.allProjectsList
      .filter((p: any) => p.projectId === projectId && p.clientLocationId === location.clientLocationId)
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
    this.getClientSideIdByProjectIdAndEmpId(projectId, this.currentUser.empId);
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
          this.timesheetLocations.forEach(location => {
            location.projects.forEach(project => {
              if (project.projectId == projectId) {
                project.clientSideId = this.timesheetObj.clientSideId;
                project.hasClientSideId = true;
              }
            });
          });
        }
      } else {
        console.error(response.serviceResponse);
      }
    });
  }
  /**
   * Handle client selection at project level - populate client locations and teams
   */
  onProjectClientSelect(location: LocationEntry, project: ProjectEntry, clientId: number): void {
    if (!clientId) {
      project.clientId = null;
      project.clientLocationList = [];
      project.projectList = [];
      project.projectActivities = [];
      // Clear activities
      project.activities.forEach(activity => {
        activity.activityId = null;
      });
      return;
    }

    project.clientId = clientId;

    // Filter client locations for this project, location, and client from allProjectsList
    const clientLocations = this.allProjectsList
      .filter((p: any) =>
        p.projectId === project.projectId &&
        p.clientLocationId === location.clientLocationId &&
        p.clientId === clientId &&
        p.clientLocationId
      )
      .map((p: any) => ({
        clientLocationId: p.clientLocationId,
        clientLocation: p.clientLocation
      }));

    // Remove duplicates
    project.clientLocationList = [...new Map(clientLocations.map((cl: any) => [cl.clientLocationId, cl])).values()];

    // Filter teams for this project, location, and client
    const teams = this.allProjectsList
      .filter((p: any) =>
        p.projectId === project.projectId &&
        p.clientLocationId === location.clientLocationId &&
        p.clientId === clientId &&
        p.teamId
      )
      .map((p: any) => ({
        teamId: p.teamId,
        teamName: p.teamName,
        projectName: p.projectName
      }));

    // Remove duplicates
    project.projectList = [...new Map(teams.map((t: any) => [t.teamId, t])).values()];

    // Clear dependent fields
    project.clientLocationId = null;
    project.teamId = null;
    project.projectActivities = [];
    project.activities.forEach(activity => {
      activity.activityId = null;
    });
  }

  /**
   * Handle client location selection at project level - populate teams
   */
  onProjectClientLocationSelect(location: LocationEntry, project: ProjectEntry, clientLocationId: number): void {
    if (!clientLocationId) {
      project.clientLocationId = null;
      project.projectList = [];
      project.projectActivities = [];
      project.activities.forEach(activity => {
        activity.activityId = null;
      });
      return;
    }

    project.clientLocationId = clientLocationId;

    // Filter teams for this project, location, client, and client location
    if (project.clientId) {
      const teams = this.allProjectsList
        .filter((p: any) =>
          p.projectId === project.projectId &&
          p.clientLocationId === clientLocationId &&
          p.clientId === project.clientId &&
          p.teamId
        )
        .map((p: any) => ({
          teamId: p.teamId,
          teamName: p.teamName,
          projectName: p.projectName
        }));

      // Remove duplicates
      project.projectList = [...new Map(teams.map((t: any) => [t.teamId, t])).values()];
    }

    // Clear dependent fields
    project.teamId = null;
    project.projectActivities = [];
    project.activities.forEach(activity => {
      activity.activityId = null;
    });
  }

  /**
   * Handle team selection at project level - load activities for all activities in the project
   */
  onProjectTeamSelect(location: LocationEntry, project: ProjectEntry, teamId: number): void {
    if (!teamId) {
      project.teamId = null;
      project.projectActivities = [];
      project.activities.forEach(activity => {
        activity.activityId = null;
      });
      return;
    }

    project.teamId = teamId;

    // Load activities for the project
    this.loadActivitiesForProject(location, project, teamId);
  }

  onClientApprovalStatusSelect(location: LocationEntry, project: ProjectEntry, status: any) {
    if (!status) {
      project.clientApprovalStatus = null;
      project.projectActivities = [];
      project.activities.forEach(activity => {
        activity.activityId = null;
      });
      return;
    }
    project.clientApprovalStatus = status;
  }

  /**
   * Load activities for a project (at project level)
   */
  loadActivitiesForProject(location: LocationEntry, project: ProjectEntry, teamId: number): void {
    if (!project.clientId || !project.clientLocationId) {
      console.error('Client and Client Location must be selected first');
      return;
    }

    const timesheetObj = new Timesheet();
    timesheetObj.projectId = project.projectId;
    timesheetObj.empId = project.isShadowTimesheet && project.shadowEmpId
      ? project.shadowEmpId
      : (this.timesheetObj.empId || this.currentUser?.empId);
    timesheetObj.teamId = teamId;
    timesheetObj.clientId = project.clientId;
    timesheetObj.clientLocationId = project.clientLocationId;

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

          // Store activities at project level for all activities to use
          project.projectActivities = allActivityList;
        } else {
          console.error('Failed to load activities:', response.serviceResponse);
          project.projectActivities = [];
        }
      }, (error) => {
        console.error('Error loading activities:', error);
        project.projectActivities = [];
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
    this.timesheetNewService.getAllMyTimesheetsByEmpId(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.availableTimesheets = response.serviceResponse;
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  onNightShiftChange() {
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
      this.totalWorkingHours = 0;
      this.totalPresence = 0;
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
   * Parse time string to 24-hour format
   * Handles both "HH:mm" (24-hour) and "HH:mm AM/PM" (12-hour) formats
   */
  parseTimeTo24Hour(timeStr: string): { hour: number; minute: number } | null {
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

  /**
   * Calculate total working hours from ApMoSys In Time and Out Time
   */
  calculateTotalWorkingHours(): void {
    // Reset to 0 if required fields are missing
    if (!this.fromDate || !this.apmosysInTime || !this.apmosysOutTime) {
      this.totalWorkingHours = 0;
      this.totalPresence = 0;

      return;
    }

    try {
      // Parse fromDate (DD-MM-YYYY format)
      const fromDateParsed = this.parseDDMMYYYY(this.fromDate);
      if (!fromDateParsed) {
        this.totalWorkingHours = 0;
        this.totalPresence = 0;
        return;
      }

      // Determine which date to use for out time
      let outDate: Date;
      if (this.timesheetObj.isNightShift && this.toDate) {
        // Use toDate for night shift
        const toDateParsed = this.parseDDMMYYYY(this.toDate);
        if (!toDateParsed) {
          this.totalWorkingHours = 0;
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
        console.error('Failed to parse time:', { inTime: this.apmosysInTime, outTime: this.apmosysOutTime });
        this.totalWorkingHours = 0;
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
      this.totalWorkingHours = Math.round(finalHours * 100) / 100;
      this.totalPresence = this.totalWorkingHours; // Set totalPresence for display
      console.log('Total Working Hours calculated:', this.totalWorkingHours);
    } catch (error) {
      console.error('Error calculating total working hours:', error);
      this.totalWorkingHours = 0;
      this.totalPresence = 0;
    }
  }

  /**
   * Handle ApMoSys In Time change
   */
  onApMoSysInTimeChange(time: string): void {
    this.apmosysInTime = time;
    console.log("In time changed to", time);
    this.calculateTotalWorkingHours();
  }

  /**
   * Handle ApMoSys Out Time change
   */
  onApMoSysOutTimeChange(time: string): void {
    this.apmosysOutTime = time;
    console.log("Out time changed to", time);
    this.calculateTotalWorkingHours();
  }

  /**
   * Handle file selection for document upload
   */
  onFileSelected(event: any, docType: 'Filled' | 'Approved', projectId: number): void {
    const file: File = event.target.files[0];
    if (!file) return;

    const allowedTypes = ['application/pdf', 'image/jpeg', 'image/png'];
    const maxSize = 300 * 1024; // 300KB

    if (!allowedTypes.includes(file.type)) {
      if (docType === 'Filled') {
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
      if (docType === 'Filled') {
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
    if (docType === 'Filled' && this.rawObjectUrl1) {
      URL.revokeObjectURL(this.rawObjectUrl1);
    }
    if (docType === 'Approved' && this.rawObjectUrl2) {
      URL.revokeObjectURL(this.rawObjectUrl2);
    }

    // Create object URL and sanitize it
    const objectUrl = URL.createObjectURL(file);
    const previewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(objectUrl);

    if (docType === 'Filled') {
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
  openPreviewModalForTwo(docType: 'Filled' | 'Approved'): void {
    this.activePreviewUrl = docType === 'Filled' ? this.previewUrl1 : this.previewUrl2;
    this.activeFileType = docType === 'Filled'
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
  renameFile(file: File, projectId: number, docType: 'Filled' | 'Approved'
  ): File {
    const ext = file.name.substring(file.name.lastIndexOf('.'));
    const safeDocType = docType.toLowerCase(); // optional
    const newFileName = `${projectId}_${safeDocType}_${file.name}`;

    return new File([file], newFileName, { type: file.type });
  }
  hasFilledDocument(projectId: number): boolean {
    return this.uploadFileList?.some(
      file => file.projectId === projectId && file.docType === 'Filled'
    );
  }
  hasApprovedDocument(projectId: number): boolean {
    return this.uploadFileList?.some(
      file => file.projectId === projectId && file.docType === 'Approved'
    );
  }
  createTimesheet() {
    console.log("Create Timesheet clicked");
    let projectDataList: ProjectTimesheetDTO[] = [];
    let projectData: ProjectTimesheetDTO;
    this.timesheetLocations.forEach((location) => {
      location.projects.forEach((project) => {
        projectData = new ProjectTimesheetDTO();
        projectData.projectId = project.projectId;
        // Use location's client times if available, otherwise project's
        projectData.logInTime = location.logInTime || project.logInTime;
        projectData.logOutTime = location.logOutTime || project.logOutTime;
        projectData.clientApprovalStatus = this.timesheetObj.clientApprovalStatus;
        projectData.shadowEmpId = project.isShadowTimesheet ? project.shadowEmpId : null;
        // projectData.isShadowTimesheet = project.isShadowTimesheet;
        let activityDataList: ActivityTimesheetDTO[] = [];
        let activityData: ActivityTimesheetDTO;
        project.activities.forEach((activity) => {
          activityData = new ActivityTimesheetDTO();
          // activityData.clientId = activity.clientId;
          activityData.clientLocationId = location.clientLocationId || activity.clientLocationId;
          // activityData.teamId = activity.teamId;
          activityData.activityId = activity.activityId;
          activityData.description = activity.description;
          activityData.durationMinutes = activity.completionTime * 60;
          activityDataList.push(activityData);
        });
        projectData.activities = activityDataList;
        projectDataList.push(projectData);
      });
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

    this.timesheetNewService.addTimesheetWithClientUpdated(this.createOrUpdateObj, this.selectedFile, this.selectedFile2)
      .pipe(first())
      .subscribe((response: any) => {
        if (response.serviceStatus === "Success") {
          this.openAlertMod(this.alertTemplate, "Timesheet created successfully.");
          // Reset form or navigate as needed
        } else {
          console.error("Failed to create timesheet:", response.serviceResponse);
          this.openAlertMod(this.alertTemplate, "Failed to create timesheet. Please try again.");
        }
      }, (error) => {
        console.error("Error creating timesheet:", error);
        this.openAlertMod(this.alertTemplate, "An error occurred while creating the timesheet.");
      });
  }

  // Format date for display (e.g., "Oct 24, 2023")
  // getFormattedDate(): string {
  //   if (!this.fromDate) return '';
  //   const date = this.parseDDMMYYYY(this.fromDate);
  //   if (!date) return '';
  //   return moment(date).format('MMM DD, YYYY');
  // }

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
  

  // Get day name (e.g., "Thursday")
  // getDayName(): string {
  //   if (!this.fromDate) return '';
  //   const date = this.parseDDMMYYYY(this.fromDate);
  //   if (!date) return '';
  //   return moment(date).format('dddd');
  // }

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
  

  // Format total presence as "08:45 hrs"
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
 
  // getListToRenderUpload(){
  //   let dataList : Map<number, ProjectEntry> = new Map<number, ProjectEntry>();

  getListToRenderUpload() {
    let dataList: Map<number, ProjectEntry> = new Map<number, ProjectEntry>();
    this.timesheetLocations.forEach((location) => {
      location.projects.forEach((project) => {
        if (project.clientSideId && (project.clientApprovalStatus == 'Approved' || project.clientApprovalStatus == 'Pending')) {
          dataList.set(project.projectId, project);
        }
      });
    });
    this.uniqueProjectsList = Array.from(dataList.values());

}

  getAllWorkLocationFromLocationMaster(){
    this.timesheetNewService.getAllWorkLocation().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.workLocationList = response.serviceResponse;
        console.log("Work locaiton list",this.workLocationList);
        } else {
          console.error(response.serviceResponse)
        }
      });
  }

  getAllDSRApprovalStatusFromMaster(){
    this.timesheetNewService.getAllDSRApprovalStatus().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.clientApprovalStatusList = response.serviceResponse;
        console.log("DSR approval status list",this.clientApprovalStatusList);
        } else {
          console.error(response.serviceResponse)
        }
      });
  }
}