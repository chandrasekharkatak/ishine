import { Component, OnInit, TemplateRef } from '@angular/core';
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

@Component({
  standalone: false,
  selector: 'app-timesheet-form',
  templateUrl: './timesheet-form.component.html',
  styleUrl: './timesheet-form.component.css'
})
export class TimesheetFormComponent implements OnInit {
projectId: any;
getClientSideIdByProjectIdAndEmpId(arg0: any,arg1: any) {
throw new Error('Method not implemented.');
}
timesheetAppliedFor: any;
shadowEmpId: any;
employeeList: any[];
getTimesheetMetadata($event: any) {
throw new Error('Method not implemented.');
}
onTimesheetDescriptionChange() {
throw new Error('Method not implemented.');
}
getActiveProjectsAndClientSideIdByEmpId() {
throw new Error('Method not implemented.');
}
  
  modalRef:NgbModalRef;
  timesheetProjects: ProjectEntry[] = [];
  timesheetObj: Timesheet = new Timesheet();
    allTimesheetActivities: any[] = []
    allProjectsList: any[] = [];
    allActivityList: any[] = [];
    allDayTypes: any[] = [];
  
    allMyTimesheets: any[] = [];
    timesheetActivities: any[] = [];
    expandedProjectIndex: number | null = 0;
  fromDate: string;
   isCreation: boolean = true;
  isUpdation: boolean = false;
  currentUser: User;
  teamMemberList: any[] = [];
  toDate: null;
  errorMsg: string;

  constructor(
    private modalService: NgbModal,
    private authenticationService: AuthenticationService,
        private teamViewService: TeamViewService,
        private timesheetService: TimesheetService,
  ) {
        this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
   }
  ngOnInit(): void {
    this.addProject();
    this.getAllDayTypes();
  }

  
  createActivity(): ActivityNew {
    return {
      clientId: null,
      clientLocationId: null,
      teamId: null,
      activityId: null,
      description: '',
      completionTime: null
    };
  }

  createProject(): ProjectEntry
   {
    return {
      projectId: null,
      inTime: '',
      outTime: '',
      totalWorkingHours: '',
      activities: [this.createActivity()]
    };
  }

  toggleProject(index: number) {
  this.expandedProjectIndex =
    this.expandedProjectIndex === index ? null : index;
}

calculateProjectHours(project: any) {
    project.totalWorkingHours = project.activities.reduce(
      (sum: number, a: any) => sum + (Number(a.completionTime) || 0),
      0
    );
  }

  onActivityHourChange(project: any) {
    this.calculateProjectHours(project);
  }

  /* ---------- VALIDATION ---------- */

  isActivityComplete(a: ActivityNew): boolean {
    return !!(
      a.clientId &&
      a.clientLocationId &&
      a.teamId &&
      a.activityId &&
      a.description.trim() &&
      a.completionTime &&
      a.completionTime > 0
    );
  }

  isProjectValid(p: ProjectEntry): boolean {
    return p.activities.some(a => this.isActivityComplete(a));
  }

  /* ---------- PROJECT ACTIONS ---------- */

  addProject(): void {
    if (this.timesheetProjects.length > 0) {
      const last = this.timesheetProjects[this.timesheetProjects.length - 1];
      if (!this.isProjectValid(last)) {
        alert('Complete at least one activity before adding a new project.');
        return;
      }
    }
    this.timesheetProjects.push(this.createProject());
  }

  removeProject(index: number): void {
    this.timesheetProjects.splice(index, 1);
  }

  /* ---------- ACTIVITY ACTIONS ---------- */

  addActivity(project: ProjectEntry): void {
    project.activities.push(this.createActivity());
  }

  removeActivity(project: ProjectEntry, index: number): void {
    project.activities.splice(index, 1);
  }
  onDateChange(val: string) {
    this.fromDate = val;
  }
  cancelRequest() {
    this.modalRef.close();
  }

  onFromDateChange(){
  }
  openNightShiftTemplate(template: TemplateRef<any>, event) {
    
    if (event.target.checked) {
      this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    }
    else{
      // this.toDate = null;
      // this.makeApmosysInTime();
      // this.makeApmosysOutTime();
      // if(!this.clientSideIdNotMandatory){
      //   this.makeClientInTime();
      //   this.makeClientOutTime();
      // }
    }
  }
onTimesheetAppliedForChange(value: string): void {

  this.timesheetObj.timesheetAppliedFor = value;



  if (value === 'self') {

    this.getAllTeamMemberList();

    // this.getTimesheetMetadata();

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

resetTimesheetFormForAutoFill() {
    this.timeReset();
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
  timeReset() {
    throw new Error('Method not implemented.');
  }

  // Api Calls
  getAllDayTypes() {
    this.timesheetService.getAllDayTypes().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDayTypes = response.serviceResponse;
      } else {
        console.error(response.serviceResponse);
      }});
  }
  

 getAllTeamMemberList() {
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

  }
  
}
