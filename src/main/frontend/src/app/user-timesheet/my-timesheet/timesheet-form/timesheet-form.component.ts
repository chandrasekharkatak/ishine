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
import { TimesheetDocumentDataI } from './types';
import { v4 as uuidv4 } from 'uuid';

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
  @ViewChild("clientSideIdUpdateOrAddModal")
  clientSideIdUpdateOrAddModal: TemplateRef<any>;
  clientSideIdUpdateOrAddModalRef: NgbModalRef;
  alertWithResetModRef: NgbModalRef;
  @Input() isCreation: boolean = false;
  @Input() isUpdation: boolean = false;
  @Input() isView: boolean = false;
  @Input() selectedDate: Date | null = null;
  @Input() isAutoFilled: boolean = false;
  isTimesheetLockCheckEnable: any = "true";
  // timesheetObj: Timesheet = new Timesheet();
  timesheetFilledForUser: User = new User();
  dayType: number;
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
  // selectedFile2: File | null = null;
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
  workLocationList: any[];
  isNightShift: boolean = false;
  documentList: TimesheetDocument[] = [];
  // documentData: {
  //   docId: number;
  //   projectId: number;
  //   docType: 'Filled' | 'Approved';
  //   // file: File;
  //   previewUrl: SafeResourceUrl;
  //   rawObjectUrl: string;
  //   fileError: string;
  //   fileType: '' | 'pdf' | 'image' | null;
  //   fileName: string;
  //   fileSize: number;
  // }[] = [];

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
  constructor(private teamViewService: TeamViewService,
    private timesheetService: TimesheetService,
    private timesheetNewService: TimesheetNewService,
    private modalService: NgbModal,
    private authenticationService: AuthenticationService,
    private sanitizer: DomSanitizer) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x); }

  ngOnInit(): void {
    // Initialize with one location
    this.getAllWorkLocationFromLocationMaster();
    this.addLocation(null);
    this.getAllDayTypes();

    this.getAllDSRApprovalStatusFromMaster();

  }


  isDayTypeFillable(): boolean {
    if (this.dayType == 1 || this.dayType == 3 || this.dayType == 8) return true;
    else return false;

  }

  formatDateDDMMYYYY(date: Date): string {
    return `${String(date.getDate()).padStart(2, '0')}-${String(date.getMonth() + 1).padStart(2, '0')}-${date.getFullYear()}`;
  }
  /**
   * Create a new ActivityNew object
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
   * Create a new LocationEntry object
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
        workLocationTypeId: 4,
        locationInTime: null,
        locationOutTime: null,
        totalWorkingHours: null,
        projects: [this.createProject(null, timesheetId)],
      };
    }
  }

  /**
   * Create a new ProjectEntry object
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

  /**
   * Add a new location to the timesheet
   */
  applyApmosysTiming(): void {

    if (this.useApmosysTiming) {
      this.disableAdd = true
      this.timesheetLocations[0].locationInTime = this.apmosysInTime;
      this.timesheetLocations[0].locationOutTime = this.apmosysOutTime;
    } else {
      // OPTION 1: Clear timings when unchecked
      this.disableAdd = false
      this.timesheetLocations[0].locationInTime = null;
      this.timesheetLocations[0].locationOutTime = null;

      this.onHoursChange();
    }

  }

  addLocation(timesheetId: number): void {
    console.log(this.timesheetLocations);
    this.timesheetLocations.push(this.createLocation(timesheetId));
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
    this.empHasClientSideId = false;
    this.getListToRenderUpload();
    console.log(this.documentData, "this.documentData after removing project")
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
  addProject(location: LocationEntry, timesheetId: number): void {
    location.projects.push(this.createProject(location, timesheetId));
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
    console.log(this.documentData, "this.documentData after removing project")
    // Adjust expanded project index if needed
    const locationIndex = this.timesheetLocations.indexOf(location);
    const currentExpanded = this.expandedProjectIndexMap[locationIndex];
    if (currentExpanded === projectIndex) {
      this.expandedProjectIndexMap[locationIndex] = location.projects.length > 0 ? 0 : null;
    } else if (currentExpanded !== null && currentExpanded > projectIndex) {
      this.expandedProjectIndexMap[locationIndex] = currentExpanded - 1;
    }
  }

  get formattedEmployeeId(): string {
    let placeholder = "";
    if (this.currentUser.isApmosysProduct) {
      placeholder = `NA (AP-${this.currentUser.employeementId})`;
    }
    else {
      placeholder = `NA (A-${this.currentUser.employeementId})`
    }
    return placeholder;
  }

  openClientSideTemplate(project: ProjectEntry) {

    this.empClientSideObj.clientSideId = '';
    this.clientSideIdUpdateOrAddModalRef = this.modalService.open(this.clientSideIdUpdateOrAddModal, { modalDialogClass: 'modal-lg' });

  }
  hideClientSideIdForm() {
    this.clientSideIdUpdateOrAddModalRef.close();
  }

  updateClientSideIdMapping(template: TemplateRef<any>) {
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

  onCancelClientSideId() {
    this.hideClientSideIdForm();
    this.getClientSideIdByProjectIdAndEmpId(this.empClientSideObj.projectId, this.empClientSideObj.empId);
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
          const newActivity = this.createActivity(null, project.projectId);
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
          if (newActivity.clientTeamList.length == 1) {
            newActivity.teamId = newActivity.clientTeamList[0].teamId;
            this.onProjectTeamSelect(newActivity.teamId, proj);
          }
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
    console.log(this.expandedLocationIndex);
    console.log(this.timesheetLocations)
    const duplicateCount = this.timesheetLocations.filter(
      loc => loc !== location &&
        loc.workLocationTypeId === location.workLocationTypeId
    ).length;
    console.log(this.timesheetLocations)
    if (duplicateCount > 1) {
      this.openAlertMod(this.alertTemplate, 'You are entering duplicate location');

      // Reset ONLY the selected location
      location.workLocationTypeId = null;
      location.workLocationType = null;
      return;
    }

    // ✅ Only update THIS location
    if (!this.isDayTypeFillable()) {
      location.locationMappingId = 4;
    }
    console.log(this.timesheetLocations)
    this.empHasClientSideId = false;

    if (this.timesheetAppliedFor.toLowerCase() === 'self') {
      this.getAllProjectsByEmpId(this.currentUser.empId, location);
    } else {
      const teamMember = this.teamMemberList.find(
        e => e.empId === this.timesheetFilledForUser.empId
      );
      this.getAllProjectsByEmpId(teamMember?.empId, location);
    }

    this.getListToRenderUpload();
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

    this.timesheetLocations.forEach(loc => {
      if (!this.isDayTypeFillable()) {
        loc.workLocationTypeId = 4;
        this.disableAdd = true;
        this.onLocationSelect(loc);
      } else {
        loc.workLocationTypeId = null;
        this.disableAdd = false;
      }
    });


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
                  projectName: p.projectName,
                  hasClientSideId: p.hasClientSideId,
                  hasClientFlag: p.hasClientFlag
                }
              ])
            ).values()
          );

          console.log("uniqueProjects", uniqueProjects);
          console.log(this.allProjectsList, "this.allProjectsList")
          console.log(this.timesheetLocations)
          this.timesheetLocations.forEach(loc => {
            if (location.workLocationTypeId === loc.workLocationTypeId) {
              loc.projects = [this.createProject(location, null)]; // Reset to one project
              loc.projects.forEach(proj => {
                proj.projectList = uniqueProjects;
                if (proj.projectList.length == 1) {
                  proj.projectId = proj.projectList[0].projectId
                }
                this.onProjectSelect(proj.projectId);
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
          const matchedProject = proj.projectList.find(
            p => p.projectId === proj.projectId
          );
          proj.hasClientSideId = matchedProject.hasClientSideId;
          proj.hasClientFlag = matchedProject.hasClientFlag;
          proj.projectName = matchedProject.projectName;
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
          if (proj.clientList.length == 1) {
            proj.clientId = proj.clientList[0].clientId;
            this.onProjectClientSelect(proj.clientId);
          }
          console.log('Project selected:', proj);

        }
      });
    });

    // Fetch client side id (side effect)
    this.getClientSideIdByProjectIdAndEmpId(
      projectId,
      this.currentUser.empId
    );
    this.empHasClientSideId = false;
    this.getListToRenderUpload();
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
        if (proj.clientLocationList.length == 1) {
          proj.clientLocationId = proj.clientLocationList[0].clientLocationId
        }
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
          if (activity.clientTeamList.length == 1) {
            activity.teamId = activity.clientTeamList[0].teamId;
            this.onProjectTeamSelect(activity.teamId, proj);
          }
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
        console.error('Failed to parse time:', { inTime: this.apmosysInTime, outTime: this.apmosysOutTime });
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

      this.totalPresence = Math.round(finalHours * 100) / 100; // Set totalPresence for display
      console.log('Total Working Hours calculated:', this.totalPresence);
    } catch (error) {
      console.error('Error calculating total working hours:', error);
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

    // @TODO: This data will come form backend
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

    const uniqueFile: File = this.renameFile(file, projectId, docType)
    const uniqueIdentifier = uniqueFile.name

    console.log("Unique File: ", uniqueFile);


    // ✅ Update entry
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

  containsFile(fileName: string): boolean {
    return this.documentData.some(
      f => f.uniqueIdentifier === fileName
    );
  }

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
    link.href = this.activeRawObjectUrl;
    link.download = fileName;
    link.click();
  }



  /**
   * Open preview modal for uploaded file
   */
  openPreviewModalForTwo(file: any): void {
    if (!file?.previewUrl || !file?.fileType) return;

    this.activePreviewUrl = file.previewUrl;
    this.activeFileType = file.fileType;
    this.activeRawObjectUrl = file.rawObjectUrl;

    this.resetTransformations();

    this.modalRef = this.modalService.open(this.previewModal, {
      modalDialogClass: 'modal-lg',
      scrollable: true
    });
  }

  get imageTransform(): string {
    return `
    translate(-50%, -50%)
    translate(${this.translateX}px, ${this.translateY}px)
    scale(${this.imageZoom})
    rotate(${this.rotation}deg)
  `;
  }


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

  /* ---------- PAN (DRAG) ---------- */
  startDrag(event: MouseEvent): void {
    this.isDragging = true;
    this.startX = event.clientX - this.translateX;
    this.startY = event.clientY - this.translateY;
  }

  onDrag(event: MouseEvent): void {
    if (!this.isDragging) return;

    this.translateX = event.clientX - this.startX;
    this.translateY = event.clientY - this.startY;
  }

  stopDrag(): void {
    this.isDragging = false;
  }

  /* ---------- FULLSCREEN ---------- */
  toggleFullscreen(): void {
    this.isFullscreen = !this.isFullscreen;
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
    return this.documentData?.some(
      file => file.projectId === projectId && file.docType === 'Filled'
    );
  }
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

  resetForm(){
    this.dayType = null;
    this.apmosysInTime = null;
    this.apmosysOutTime = null;
    this.totalPresence = null;
    this.timesheetLocations = [];
    this.addLocation(null);

  }

  createTimesheet() {

    this.highlightLocationList = [];
    const convertToYYYYMMDD = (dateStr: string): string => {
      if (!dateStr) return '';
      const [day, month, year] = dateStr.split('-');
      return `${year}-${month}-${day}`;  // Convert "08-01-2026" to "2026-01-08"
    };
    let hasValidationError = false;

    this.timesheetLocations.forEach((location, index) => {

      const selectedLocationData = this.workLocationList.find(
        l => l.workLocationTypeId === location.workLocationTypeId
      );

      // ✅ IN TIME VALIDATION
      const inTimeVal = this.validateLocationInTime(
        location.locationInTime,
        this.fromDate,
        this.apmosysInTime
      );

      if (!inTimeVal) {
        this.openAlertMod(
          this.alertTemplate,
          `The Log-In time for the location ${selectedLocationData?.code} is less than Work Check In time`
        );

        this.highlightLocationList.push(location.workLocationTypeId);
        hasValidationError = true;
        return;
      }

      // ✅ OUT TIME VALIDATION
      const outTimeVal = this.validateLocationOutTime(
        location.locationOutTime,
        this.fromDate,
        this.apmosysOutTime,
        this.isNightShift,
        this.toDate
      );

      if (!outTimeVal) {
        this.openAlertMod(
          this.alertTemplate,
          `The Log-Out time for the location ${selectedLocationData?.code} is more than Work Check Out time`
        );

        this.highlightLocationList.push(location.workLocationTypeId);
        hasValidationError = true;
        return;
      }
    });

    // STOP & HIGHLIGHT UI
    if (hasValidationError) {
      this.applyHighlightAndExpand();
      return;
    }
    const dataSet = this.timesheetLocations
    this.createOrUpdateObj = {
      createdBy: this.currentUser.empId,
      dayTypeId: this.dayType,
      empId: this.currentUser.empId,
      isApmosysProduct: this.currentUser.isApmosysProduct,
      isNightShift: this.isNightShift,
      timesheetId: null,
      // date: this.formatDDMMYYYY(new Date(this.fromDate as string)),
      date: convertToYYYYMMDD(this.fromDate),
      workCheckIn: [4,6,7].includes(this.dayType) ? null : this.formatDateTimeForBackend(this.apmosysInTime, convertToYYYYMMDD(this.fromDate)),
      workCheckOut: [4,6,7].includes(this.dayType) ? null : this.formatDateTimeForBackend(this.apmosysOutTime, convertToYYYYMMDD(this.isNightShift ? this.toDate : this.fromDate)),
      currentManagerId: this.currentUser.managerId,
      totalWorkingMinutes: this.totalPresence * 60,
      locationSessions: dataSet,
      documentData: this.documentData
    }

    this.createOrUpdateObj.locationSessions.forEach((location: LocationEntry) => {
      location.locationInTime = [4,6,7].includes(this.dayType) ? null : this.formatDateTimeForBackend(location.locationInTime, convertToYYYYMMDD(this.fromDate));
      location.locationOutTime = [4,6,7].includes(this.dayType) ? null : this.formatDateTimeForBackend(location.locationOutTime, convertToYYYYMMDD(this.isNightShift ? this.toDate : this.fromDate));
      location.projects.forEach((project: ProjectEntry) => {
        if(![4,6,7].includes(this.dayType)) {
          project?.activities?.forEach((activity: ActivityNew) => {
          activity.durationMinutes = activity.durationMinutes * 60;
        });
        } else {
          project.activities = null;
        }
      });
    });

    this.generateTotalMinutes(this.createOrUpdateObj);

    this.timesheetNewService.createTimesheet(this.createOrUpdateObj, this.selectedFile)
      .subscribe({
        next: (response: any) => {
          if (response.serviceStatus === "Success") {
            this.openAlertMod(this.alertTemplate, "Timesheet created successfully.");
            // Reset form or navigate as needed
          } else {
            console.error("Failed to create timesheet:", response.serviceResponse);
            this.openAlertMod(this.alertTemplate, "Failed to create timesheet. Please try again.");
          }
        },
        error: (error) => {
          console.error("Error creating timesheet:", error);
          this.openAlertMod(this.alertTemplate, "An error occurred while creating the timesheet.");
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

  validationService() {

  }

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

  validateLocationInTime(
    locationInTime: string,
    fromDate: string,
    apmosysInTime: string
  ): boolean {

    if (!locationInTime || !fromDate || !apmosysInTime) {
      return false;
    }

    const parseDate = (dateStr: string) => {
      const [day, month, year] = dateStr.split('-').map(Number);
      return new Date(year, month - 1, day);
    };

    const parseTime = (timeStr: string) => {
      const [time, meridian] = timeStr.trim().split(' ');
      let [hh, mm] = time.split(':').map(Number);

      if (meridian === 'PM' && hh !== 12) hh += 12;
      if (meridian === 'AM' && hh === 12) hh = 0;

      return { hh, mm };
    };

    const date = parseDate(fromDate);
    const inTime = parseTime(locationInTime);
    const apmIn = parseTime(apmosysInTime);

    const inDateTime = new Date(
      date.getFullYear(),
      date.getMonth(),
      date.getDate(),
      inTime.hh,
      inTime.mm
    );

    const apmosysInDateTime = new Date(
      date.getFullYear(),
      date.getMonth(),
      date.getDate(),
      apmIn.hh,
      apmIn.mm
    );

    // In-time before allowed Apmosys time
    return inDateTime.getTime() >= apmosysInDateTime.getTime();
  }

  validateLocationOutTime(
    locationOutTime: string,
    fromDate: string,
    apmosysOutTime: string,
    isNightShift: boolean,
    toDate?: string
  ): boolean {

    if (!locationOutTime || !fromDate || !apmosysOutTime) {
      return false;
    }

    if (isNightShift && !toDate) {
      return false;
    }

    const parseDate = (dateStr: string) => {
      const [day, month, year] = dateStr.split('-').map(Number);
      return new Date(year, month - 1, day);
    };

    const parseTime = (timeStr: string) => {
      const [time, meridian] = timeStr.trim().split(' ');
      let [hh, mm] = time.split(':').map(Number);

      if (meridian === 'PM' && hh !== 12) hh += 12;
      if (meridian === 'AM' && hh === 12) hh = 0;

      return { hh, mm };
    };

    const startDate = parseDate(fromDate);
    const endDate = isNightShift && toDate ? parseDate(toDate) : startDate;

    const outTime = parseTime(locationOutTime);
    const apmOut = parseTime(apmosysOutTime);

    const outDateTime = new Date(
      endDate.getFullYear(),
      endDate.getMonth(),
      endDate.getDate(),
      outTime.hh,
      outTime.mm
    );

    const apmosysOutDateTime = new Date(
      endDate.getFullYear(),
      endDate.getMonth(),
      endDate.getDate(),
      apmOut.hh,
      apmOut.mm
    );

    // ❌ Out-time after allowed Apmosys time
    return outDateTime.getTime() <= apmosysOutDateTime.getTime();
  }


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

    // ❌ Night shift but no toDate
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
      // Invalid interval
      if (outDateTime.getTime() < inDateTime.getTime()) {
        this.openAlertMod(this.alertTemplate, "The Log-Out time for the location:" + selectedLocationData.code + " " + "is greater than Log-Out time");
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

        for (const activity of proj.activities) {
          projHours += Number(activity.durationMinutes) || 0;
        }

        proj.totalWorkingHours = projHours;
        projectHoursForLocation += projHours;
      }

      // ❌ Validation 1: Project > Location
      if (projectHoursForLocation > location.totalWorkingHours) {
        this.openAlertMod(
          this.alertTemplate,
          'Total working hours for location cannot be less than sum of project working hours.'
        );

        this.resetProjectHoursForLocation(location);
        return; // ⛔ STOP further processing
      }

      totalLocationHours += location.totalWorkingHours;

      // ❌ Validation 2: All locations > Presence
      if (totalLocationHours > this.totalPresence) {
        this.openAlertMod(
          this.alertTemplate,
          'Total working hours for all locations cannot be more than total presence hours.'
        );

        this.resetLocation(location);
        return; // ⛔ STOP further processing
      }
    }
  }


  resetProjectHoursForLocation(location: LocationEntry): void {
    location.projects.forEach(proj => {
      proj.totalWorkingHours = 0;
      proj.activities.forEach(activity => {
        activity.durationMinutes = null;
      });
    });
  }

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
    this.documentData = [];
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
            console.log("entering pending status for project:", project);
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
    console.log("Upload file list generated:", this.documentData);
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
    this.fromDate = selectedDate1;
    let timesheet: Partial<Timesheet> = { empId: this.currentUser.empId };
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

  getProjectListForDateAndEmpId() {
    //employeeTeamMapping has startDate and endDate as localDateTime
    const d = new Date(this.fromDate);
    const localDateTime = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}T00:00:00`;

    // this.currentUser.empId needs to be changed to a centralized object.empId means we need to declare a centralized object which will be 
    // sended to create timesheet, because for team the empId will be the id for the selected team member.
    const payload = {
      empId: this.currentUser.empId,
      date: localDateTime
    };

    this.timesheetService.getProjectListForDateAndEmpId(payload).pipe(first()).subscribe(async (response: any) => {
      if (response.serviceStatus == "Success") {
        this.activeProjectList = response.serviceResponse;
        console.log("Active Project List :::::::::", this.activeProjectList);

      } else {
        console.error("Service Response for this.activeProjectList :::::::", response.serviceResponse);
      }
    });
  }

  generateTotalMinutes(timesheetDTO: EmployeeTimesheetDTO) {
    let totalMinutes = 0;
    timesheetDTO.locationSessions.forEach(location => {
      location.projects.forEach(project => {
        project.activities.forEach(activity => {
          totalMinutes += activity.durationMinutes * 60;
        });
      });
    });
    return totalMinutes;
  }

}