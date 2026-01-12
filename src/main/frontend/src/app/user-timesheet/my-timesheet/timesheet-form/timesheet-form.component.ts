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
  @ViewChild("alert_message_with_reset")
  alertModalWithoutReload: TemplateRef<any>;
  alertWithResetModRef: NgbModalRef;
  @Input() isCreation: boolean = false;
  @Input() isUpdation: boolean = false;
  @Input() isView: boolean = false;
  @Input() selectedDate: Date | null = null;
  @Input() isAutoFilled: boolean = false;
  isTimesheetLockCheckEnable: any = "true";
  // timesheetObj: Timesheet = new Timesheet();
  timesheetFilledForUser: User = new User();
  dayType: any;
  timesheetAppliedFor: any = 'self';
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
  disableAdd: boolean = false;
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
  createOrUpdateObj: EmployeeTimesheetDTO = new EmployeeTimesheetDTO();
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
    fileType: '' | 'pdf' | 'image' | null;
    fileName: string;
    fileSize: number;
  }[] = [];
  @Input() autoFillTimesheet: boolean = false;
  activeRawObjectUrl: any;

  constructor(private teamViewService: TeamViewService,
    private timesheetService: TimesheetService,
    private timesheetNewService: TimesheetNewService,
    private modalService: NgbModal,
    private authenticationService: AuthenticationService,
    private sanitizer: DomSanitizer) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x); }

  ngOnInit(): void {
    // Initialize with one location
    this.addLocation();
    this.getAllDayTypes();

    this.getAllWorkLocationFromLocationMaster();
    this.getAllDSRApprovalStatusFromMaster();

  }



  formatDateDDMMYYYY(date: Date): string {
    return `${String(date.getDate()).padStart(2, '0')}-${String(date.getMonth() + 1).padStart(2, '0')}-${date.getFullYear()}`;
  }
  /**
   * Create a new ActivityNew object
   */
  createActivity(): ActivityNew {
    return {
      activityId: null,
      description: '',
      completionTime: null,
    };
  }

  /**
   * Create a new LocationEntry object
   */
  createLocation(): LocationEntry {
    return {
      locationId: null,
      logInTime: '',
      logOutTime: '',
      projects: [this.createProject()],
    };
  }

  /**
   * Create a new ProjectEntry object
   */
  createProject(): ProjectEntry {
    return {
      projectId: null,
      projectName: '',
      clientSideId: null,
      hasClientSideId: false,
      shadowEmpId: null,
      isShadowTimesheet: false,
      isShadowForSelf: false,
      // Client, Team, Location at project level
      clientId: null,
      clientLocationId: null,
      clientApprovalStatus: null,
      activities: [this.createActivity()],
      projectActivities: [], // Activities loaded for the project
      clientList: [],
      clientLocationList: [],
      projectList: [],
      shadowForList: []
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
    console.log('Timesheet Locations after adding:', this.timesheetLocations);
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
    console.log('Timesheet Locations after removing:', this.timesheetLocations);
    this.getListToRenderUpload();
    console.log(this.uploadFileList, "this.uploadFileList after removing project")
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
    const uniqueProjects = Array.from(
      new Map(
        this.allProjectsList.map(p => [
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
   */
  removeProject(location: LocationEntry, projectIndex: number): void {
    if (location.projects.length <= 1) {
      this.openAlertMod(this.alertTemplate, 'At least one project is required per location.');
      return;
    }
    location.projects.splice(projectIndex, 1);
    this.getListToRenderUpload();
    console.log(this.uploadFileList, "this.uploadFileList after removing project")
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
    this.timesheetLocations.forEach(loc => {
      loc.projects.forEach(proj => {
        if (proj === project) {
          const newActivity = this.createActivity();
          newActivity.clientTeamList = Array.from(
            new Map(
              this.allProjectsList
                .filter((p: any) => p.clientLocationId === proj.clientLocationId && p.clientId === proj.clientId && p.projectId === proj.projectId)
                .map(p => [
                  p.teamId,
                  {
                    teamId: p.teamId,
                    teamName: p.teamName
                  }
                ])
            ).values()
          );
          proj.activities.push(newActivity);
        }
        console.log('Client Location selected:', proj);
      }
      );
    });
  }

  /**
   * Remove an activity from a project
   */
  removeActivity(project: ProjectEntry, index: number): void {
    if (project.activities.length <= 1) {
      this.openAlertMod(this.alertTemplate, 'At least one activity is required per project.');
      return;
    }
    console.log(this.timesheetLocations, "this.timesheetLocations before removing activity");
    project.activities.splice(index, 1);
  }

  /**
   * Handle location selection - populate projects for the selected location
   */
  onLocationSelect(location: LocationEntry): void {
    if (this.timesheetAppliedFor.toLocaleLowerCase() == 'self') {
      console.log(this.currentUser.empId);
      this.getAllProjectsByEmpId(this.currentUser.empId, location);
      console.log(this.allProjectsList, "this.allProjectsList for self")
    }
    else if (this.timesheetAppliedFor.toLocaleLowerCase() == 'team') {
      let teamMember = this.teamMemberList.find(employee => employee.empId == this.timesheetFilledForUser.empId);
      this.getAllProjectsByEmpId(teamMember.empId, location);
      console.log(this.allProjectsList, "this.allProjectsList for team member")
    }
  }
  getAllDayTypes() {
    this.timesheetNewService.getAllDayTypes().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDayTypes = response.serviceResponse;
        if (this.isAutoFilled && this.allDayTypes.length > 0) {
          this.loadAutofillData(this.formatDateDDMMYYYY(this.selectedDate));

        }
      } else {
        console.error(response.serviceResponse)
      }
    });
  }
  onDayTypeChange(event: any) {
    // this.dayType = event;
    console.log("Day type changed", event);
    console.log("this.dayType", this.dayType);
    console.log("this is called")

  }
  activeProjectListByEmpId: any[] = []
  onTimesheetAppliedForChange(value: string): void {

    this.timesheetAppliedFor = value;


    if (this.timesheetAppliedFor.toLocaleLowerCase() === 'self') {

      this.resetTimesheetFormForAutoFill();
      this.getTimesheetMetadata();
      // this.timesheetNewService.getActiveProjectsAndClientSideIdByEmpId(+this.currentUser.empId).pipe(first()).subscribe((response: any) => {
      //   if (response.serviceStatus == "Success") {
      //     this.activeProjectListByEmpId = response.serviceResponse;
      //     console.log("all project list",this.activeProjectListByEmpId)
      //   } else {
      //     console.error(response.serviceResponse)
      //   }
      // })


    }
    else {

      this.resetTimesheetFormForAutoFill();

      this.getAllTeamMemberList();

    }

  }
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }
  getTimesheetMetadata(eventTarget?: any) {
    //console.log("timesheet Obj For getTimesheetMetadata : ", this.timesheetObj);

    let userObj: User = new User();
    if (this.timesheetAppliedFor.toLocaleLowerCase() == 'self') {
      this.timesheetFilledForUser = new User();
      userObj.empId = this.currentUser.empId;
      userObj.isTimesheetLockCheckEnable = this.currentUser.isTimesheetLockCheckEnable;
      this.timesheetFilledForUser = userObj;

    } else {
      this.timesheetFilledForUser = new User();
      console.log("Team member list is", this.teamMemberList);
      let teamMember = this.teamMemberList.find(employee => employee.empId == this.timesheetFilledForUser.empId)
      console.log("Team member is ", teamMember);
      userObj.empId = teamMember?.empId;
      userObj.isTimesheetLockCheckEnable = teamMember.isTimesheetLockCheckEnable;
      this.timesheetFilledForUser = userObj;
    }

    // const timesheetBkp = Object.assign({}, this.timesheetObj);

    // // reset timesheet
    // this.timesheetObj = new Timesheet();
    // this.timesheetObj.dayType = '';
    // this.allTimesheetActivities = [];
    // // this.addInputActivityField()

    // // set leave AppliedFor User data to fetch activities for project & for display
    // this.timesheetObj.timesheetAppliedFor = timesheetBkp.timesheetAppliedFor;
    // this.timesheetObj.empId = timesheetBkp.empId;

    // //console.log("preset Timesheet : ", this.timesheetObj);

    // this.getAllProjectsByEmpId(userObj.empId);
    // this.getAllAvailableTimesheetByEmpId(userObj);
  }

  resetTimesheetFormForAutoFill() {

  }
  getAllTeamMemberList() {
    this.resetTimesheetFormForAutoFill();

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
  }

  getAllProjectsByEmpId(empId: any, location: LocationEntry): void {
    this.allProjectsList = [];
    this.timesheetNewService.getAllProjectsByEmpId(+empId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allProjectsList = response.serviceResponse;
        if (this.allProjectsList.length === 0) {
          if (this.timesheetAppliedFor.toLocaleLowerCase() == 'self') {
            this.disableAdd = true;
            this.openAlertMod(this.alertTemplate, 'No Projects assigned. Please contact RMG.');
          } else if (this.timesheetAppliedFor.toLocaleLowerCase() == 'team') {
            this.disableAdd = true;
            let teamMember = this.teamMemberList.find(employee => employee.empId == this.timesheetFilledForUser.empId);
            this.openAlertMod(this.alertTemplate, 'No Projects assigned to ' + teamMember?.empName);

          }
        }
        else {
          const uniqueProjects = Array.from(
            new Map(
              this.allProjectsList.map(p => [
                p.projectId,
                {
                  projectId: p.projectId,
                  projectName: p.projectName
                }
              ])
            ).values()
          );

          console.log("uniqueProjects", uniqueProjects);
          console.log(this.allProjectsList, "this.allProjectsList")
          this.timesheetLocations.forEach(loc => {
            if (location.locationId === loc.locationId) {
              loc.projects = [this.createProject()]; // Reset to one project
              loc.projects.forEach(proj => {
                proj.projectList = uniqueProjects;
              });

            }
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
  onProjectSelect(projectId: number): void {
    console.log("project id selected", projectId);
    this.timesheetLocations.forEach(loc => {
      loc.projects.forEach(proj => {
        console.log("projPrevious", proj);
        if (proj.projectId === projectId) {
          console.log("projPrevious", proj);
          proj.projectName = null;
          proj.hasClientSideId = false;
          proj.shadowEmpId = null;
          proj.isShadowTimesheet = false;
          proj.isShadowForSelf = false;
          proj.clientSideId = null;
          proj.clientId = null;
          proj.clientLocationId = null;
          proj.clientApprovalStatus = null;
          proj.totalWorkingHours = null;
          proj.activities = [this.createActivity()];
          proj.projectActivities = [];
          proj.clientList = [];
          proj.clientLocationList = [];
          console.log("proj", proj);
          proj.clientList = Array.from(
            new Map(
              this.allProjectsList
                .filter(p => p.projectId === proj.projectId)
                .map(p => [
                  p.clientId,
                  {
                    clientId: p.clientId,
                    clientName: p.clientName
                  }
                ])
            ).values()
          );
          console.log('Project selected:', proj);

        }
      });
    });

    // Fetch client side id (side effect)
    this.getClientSideIdByProjectIdAndEmpId(
      projectId,
      this.currentUser.empId
    );
  }


  async getClientSideIdByProjectIdAndEmpId(projectId: any, empId: any) {
    await this.timesheetService.getClientSideIdByProjectIdAndEmpId(projectId, empId).pipe(first()).toPromise().then((response: any) => {
      if (response.serviceStatus == "Success") {
        let clientSideId = response.serviceResponse;
        this.empHasClientSideId = clientSideId ? true : false;
        // this.getListToRenderUpload();
        if (clientSideId) {
          this.empClientSideObj.clientSideId = clientSideId;
          this.empClientSideObj.projectId = projectId;
          this.empClientSideObj.empId = empId;
          this.timesheetLocations.forEach(location => {
            location.projects.forEach(project => {
              if (project.projectId == projectId) {
                project.clientSideId = clientSideId;
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
  onProjectClientSelect(clientId: number): void {
    console.log("client id selected", clientId);
    console.log(this.timesheetLocations, "this.timesheetLocations");
    this.timesheetLocations.forEach(loc => {
      loc.projects.forEach(proj => {

        proj.clientLocationList = Array.from(
          new Map(
            this.allProjectsList
              .filter(p => p.clientId === proj.clientId && p.projectId === proj.projectId)
              .map(p => [
                p.clientLocationId,
                {
                  clientLocationId: p.clientLocationId,
                  clientLocation: p.clientLocation
                }
              ])
          ).values()
        );

      });
    });

    console.log(this.timesheetLocations, "this.timesheetLocations");
  }

  /**
   * Handle client location selection at project level - populate teams
   */
  onProjectClientLocationSelect(clientLocationId: number): void {
    console.log("client location id selected", clientLocationId);
    console.log(this.timesheetLocations, "this.timesheetLocations");
    this.timesheetLocations.forEach(loc => {
      loc.projects.forEach(proj => {
        proj.activities = [this.createActivity()];
        proj.activities.forEach(activity => {
          activity.clientTeamList = Array.from(
            new Map(
              this.allProjectsList
                .filter((p: any) => p.clientLocationId === proj.clientLocationId && p.clientId === proj.clientId && p.projectId === proj.projectId)
                .map(p => [
                  p.teamId,
                  {
                    teamId: p.teamId,
                    teamName: p.teamName
                  }
                ])
            ).values()
          );
          console.log('Client Location selected:', proj);
        });
      }
      );
    });
  }

  /**
   * Handle team selection at project level - load activities for all activities in the project
   */
  onProjectTeamSelect(teamId: number, project: ProjectEntry): void {
    console.log("team id selected", teamId);
    console.log(this.timesheetLocations, "this.timesheetLocations");
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
      return;
    }
    project.clientApprovalStatus = status;
  }

  /**
   * Load activities for a project (at project level)
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
      console.error('Employee ID is not available');
      return;
    }
    console.log("timesheetObj for activities", timesheetObj);
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
            console.log("All activities for project before department filter", allActivityList);
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
            console.log("All activities for project after department filter", allActivityList);

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
    timesheetObj.createdBy = this.currentUser.empId;
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


    if (this.isNightShift) {
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
      this.isNightShift = false;
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
      if (this.isNightShift && this.toDate) {
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
  onFileSelected(
  event: any,
  docType: 'Filled' | 'Approved',
  projectId: number
): void {

  const file: File = event.target.files?.[0];
  if (!file) return;

  const allowedTypes = ['application/pdf', 'image/jpeg', 'image/png'];
  const maxSize = 300 * 1024; // 300KB

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
      'File size must be 300KB or less.'
    );
    event.target.value = '';
    return;
  }

  // ♻️ Cleanup old object URL
  const previous = this.uploadFileList.find(
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

  // ✅ Update entry
  this.updateUploadFile(projectId, docType, {
    file,
    previewUrl,
    rawObjectUrl: objectUrl,
    fileType,
    fileName: file.name,
    fileSize: file.size,
    fileError: null
  });
}


private handleFileError(
  projectId: number,
  docType: 'Filled' | 'Approved',
  message: string
): void {
  this.updateUploadFile(projectId, docType, {
    file: null,
    previewUrl: null,
    rawObjectUrl: null,
    fileType: null,
    fileName: null,
    fileSize: null,
    fileError: message
  });

  this.openAlertMod(this.alertTemplate, message);
}


 updateUploadFile(
  projectId: number,
  docType: 'Filled' | 'Approved',
  data: Partial<{
    file: File | null;
    previewUrl: SafeResourceUrl | null;
    rawObjectUrl: string | null;
    fileName: string | null;
    fileSize: number | null;
    fileError: string | null;
    fileType: 'pdf' | 'image' | null;
  }>
): void {

  const index = this.uploadFileList.findIndex(
    f => f.projectId === projectId && f.docType === docType
  );

  if (index === -1) return;

  this.uploadFileList[index] = {
    ...this.uploadFileList[index],
    ...data
  };
}



 imageZoom = 1;

zoomIn(): void {
  this.imageZoom = Math.min(this.imageZoom + 0.2, 3);
}

zoomOut(): void {
  this.imageZoom = Math.max(this.imageZoom - 0.2, 0.5);
}

resetZoom(): void {
  this.imageZoom = 1;
}

downloadImage(fileName = 'image-preview'): void {
  // Ensure we have a raw object URL
  if (!this.activeRawObjectUrl || this.activeFileType !== 'image') return;

  const link = document.createElement('a');
  link.href = this.activeRawObjectUrl; // use the raw blob URL
  link.download = fileName;
  link.click();
}


              
  /**
   * Open preview modal for uploaded file
   */
  // openPreviewModalForTwo(file: any): void {
  //   console.log("file in preview", file);
  //   this.activePreviewUrl = null;
  //   this.activeFileType = null;
  //   this.activePreviewUrl =  file.previewUrl;
  //   this.activeFileType = file?.type === 'application/pdf' ? 'pdf' : 'image';

  //   this.modalRef = this.modalService.open(this.previewModal, { modalDialogClass: 'modal-lg' });
  // }

 openPreviewModalForTwo(
  docType: 'Filled' | 'Approved',
  file: any
): void {

  console.log("file in preview", file);
  if (!file?.previewUrl || !file?.fileType) return;

  this.activePreviewUrl = file.previewUrl;
  this.activeFileType = file.fileType;
  this.activeRawObjectUrl = file.rawObjectUrl;
  if (this.activeFileType === 'image') {
    this.imageZoom = 1;
  }

  this.modalRef = this.modalService.open(
    this.previewModal,
    { modalDialogClass: 'modal-lg' }
  );
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

    // this.timesheetNewService.addTimesheetWithClientUpdated(this.createOrUpdateObj, this.selectedFile, this.selectedFile2)
    //   .pipe(first())
    //   .subscribe((response: any) => {
    //     if (response.serviceStatus === "Success") {
    //       this.openAlertMod(this.alertTemplate, "Timesheet created successfully.");
    //       // Reset form or navigate as needed
    //     } else {
    //       console.error("Failed to create timesheet:", response.serviceResponse);
    //       this.openAlertMod(this.alertTemplate, "Failed to create timesheet. Please try again.");
    //     }
    //   }, (error) => {
    //     console.error("Error creating timesheet:", error);
    //     this.openAlertMod(this.alertTemplate, "An error occurred while creating the timesheet.");
    //   });
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

  onShadowTimesheetChange(project: ProjectEntry) {
    if (this.timesheetAppliedFor == 'self') {
      this.getEmployeeListByProjectId(project, this.currentUser.empId);
    } else {
      this.openAlertMod(this.alertTemplate, "Shadow timesheet is only available when timesheet is filled for self.");
      project.isShadowTimesheet = false;
      return;
    }
  }
  getListToRenderUpload() {
    console.log("Generating upload list based on client side IDs...");
    this.uploadFileList = [];
    let dataList: Map<number, ProjectEntry> = new Map<number, ProjectEntry>();
    this.timesheetLocations.forEach((location) => {
      location.projects.forEach((project) => {
        if (project.clientSideId) {
          console.log("Project with client side id found:", project);
          this.empHasClientSideId = true;
          dataList.set(project.projectId, project);
          console.log("dataList updated:", dataList);
          if (project.clientApprovalStatus == 2) {
            console.log("entering approved status for project:", project);
            this.uploadFileList.push({ projectId: project.projectId, docType: 'Filled', file: null, previewUrl: null, rawObjectUrl: null, fileError: null, fileType: null, fileName: null, fileSize: null });
            this.uploadFileList.push({ projectId: project.projectId, docType: 'Approved', file: null, previewUrl: null, rawObjectUrl: null, fileError: null, fileType: null, fileName: null, fileSize: null });
          }
          else if (project.clientApprovalStatus == 1) {
            console.log("entering pending status for project:", project);
            this.uploadFileList.push({ projectId: project.projectId, docType: 'Filled', file: null, previewUrl: null, rawObjectUrl: null, fileError: null, fileType: null, fileName: null, fileSize: null });
          }
        }
      });
    });
    this.uniqueProjectsList = Array.from(dataList.values());
    console.log("Upload file list generated:", this.uploadFileList);
    console.log("Unique projects list generated:", this.uniqueProjectsList);
  }

  getEmployeeListByProjectId(project: ProjectEntry, empId: number) {
    this.timesheetService.getEmployeeListByProjectId(project.projectId, empId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        let shadowEmpList = response.serviceResponse;
        console.log("Shadow emp list", shadowEmpList);
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
      } else {
        console.error(response.serviceResponse)
      }
    })
  }
  // onClientApprovalStatusChange(clientApprovalStatus: any, project: ProjectEntry) {
  //   console.log("Client approval status changed to", clientApprovalStatus);
  //   console.log("Before change:", project);
  //   project.clientApprovalStatus = clientApprovalStatus;
  //   return project;
  // }
  getAllWorkLocationFromLocationMaster() {
    this.timesheetNewService.getAllWorkLocation().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.workLocationList = response.serviceResponse;
        console.log("Work locaiton list", this.workLocationList);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getAllDSRApprovalStatusFromMaster() {
    this.timesheetNewService.getAllDSRApprovalStatus().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.clientApprovalStatusList = response.serviceResponse;
        console.log("DSR approval status list", this.clientApprovalStatusList);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  loadAutofillData(selectedDate1: string) {
    this.isUpdation = false;
    // this.isTimesheetForm = true;
    this.timesheetAppliedFor = 'self';
    console.log("allDayTypes", this.allDayTypes);
    this.allDayTypes.forEach((type) => {
      if (type.dayType === 'Working') {
        this.dayType = type.dayTypeId;
      }
    });
    console.log("Default dayType set to", this.dayType);
    // this.dayType = 'Working';
    this.fromDate = selectedDate1;
    // this.makeApmosysInTime();
    // this.makeApmosysOutTime();
    // this.makeClientInTime();
    // this.makeClientOutTime();
    // this.getAllProjectsByEmpId(this.currentUser);
    // this.getActiveProjectsByEmpId();

    let timesheet: Partial<Timesheet> = { empId: this.currentUser.empId };

    // this.timesheetService.getLastFilledTimesheetByEmp(timesheet)
    //   .pipe(first())
    //   .subscribe((response: any) => {
    //     if (response.serviceStatus === "Success") {
    //       const autoData = response.serviceResponse[0];
    //       // console.log("autoFillTimesheet: " + JSON.stringify(autoData));

    //       const lockDate = new Date(autoData.timesheetLockUpdatedOn);
    //       const selectedDate = new Date(selectedDate1!);
    //       const lockCheckEnable = autoData.istimesheetLockCheckEnable;


    //       if (lockCheckEnable === "true" && lockDate > selectedDate) {
    //          const formattedDate = lockDate.toLocaleDateString("en-GB", {
    //           day: "2-digit",
    //            month: "2-digit",
    //            year: "numeric"
    //          });
    //         this.openAlertWithResetMod(
    //           this.alertModalWithoutReload,
    //           "Timesheet is locked upto " + formattedDate
    //         );
    //         return;
    //       }



    //       if (this.activeProjectList?.some(p => p.projectId === autoData.projectId)) {
    //         this.timesheetObj.projectId = autoData.projectId;
    //         // this.checkIfProjectRequiresClientId(this.timesheetObj.projectId);
    //       }
    //       this.timesheetObj.clientApprovalStatus = autoData.clientApprovalStatus;


    //       setTimeout(() => {
    //         const defaultClient = this.clientList?.find(c => c.clientId === autoData.clientId);
    //         if (!defaultClient) {
    //           console.error("Client not found in list");
    //             this.openAlertWithResetMod(
    //           this.alertModalWithoutReload,
    //           "Client not found in list " 
    //         );
    //           return;
    //         }

    //         // this.allTimesheetActivities.forEach(activityObj => {
    //         //   activityObj.clientId = defaultClient.clientId;
    //         //   this.getClientLocationList(activityObj);
    //         // });

    //         // setTimeout(() => {
    //         //   this.allTimesheetActivities.forEach(activityObj => {
    //         //     activityObj.clientLocationId = autoData.clientLocationID;
    //         //     this.getProjectList(activityObj);
    //         //   });


    //           // setTimeout(() => {
    //           //   this.allTimesheetActivities.forEach(activityObj => {
    //           //     activityObj.projectId = autoData.projectId;

    //           //     if (activityObj.projectList?.some(t => t.teamId === autoData.teamId)) {
    //           //       activityObj.teamId = autoData.teamId;
    //           //       this.getAllActivitiesByProjectIdandEmpId(activityObj);
    //           //     }
    //           //   });


    //             // setTimeout(() => {
    //             //   this.allTimesheetActivities.forEach(activityObj => {
    //             //     if (activityObj.projectActivities?.some(a => a.activityId === autoData.activityID)) {
    //             //       activityObj.activityId = autoData.activityID;
    //             //       activityObj.activity = autoData.activity;
    //             //       activityObj.description = autoData.description || '';
    //             //       activityObj.completionTime = autoData.completionTime;
    //             //     }
    //             //   });
    //             //   this.autoFillTimesheet = true;
    //             // }, 200);

    //           }, 200);

    //       //   }, 200);

    //       // }, 200);


    //     } else if (!response.serviceResponse || response.serviceResponse.length < 1) {
    //       this.openAlertWithResetMod(
    //         this.alertModalWithoutReload,
    //         "No timesheet found"
    //       );
    //       return;
    //     }

    //      else {
    //       console.error("No autofill data found:", response.serviceResponse);
    //        this.openAlertWithResetMod(
    //           this.alertModalWithoutReload,
    //           "Something went wrong " + response.serviceMessage
    //         );
    //         return;

    //     }
    //   });
  }
  openAlertWithResetMod(template: TemplateRef<any>, message: any) {
    this.alertWithResetModRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest2() {
    this.alertWithResetModRef?.close();
    this.resetTimesheetForm();
  }
  resetTimesheetForm() {
    this.fromDate = null;
    this.toDate = null;
  }

}