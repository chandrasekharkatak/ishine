import { Component, OnInit, TemplateRef } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { Feature } from 'src/app/models/feature';
import { Timesheet } from 'src/app/models/timesheet';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { TimesheetService } from 'src/app/services/timesheet.service';
import { ValidationService } from 'src/app/services/validation.service';

@Component({
  selector: 'app-team-timesheet',
  templateUrl: './team-timesheet.component.html',
  styleUrls: ['./team-timesheet.component.css']
})
export class TeamTimesheetComponent implements OnInit {

  feature="Team Timesheets";
  currentUser:User;
  userMapping:any = {};

  //flags 
  isAllTimesheetTable:boolean = false;
  isAllTimesheetRequestTable:boolean = false;

  //modal 
  alertMessage:any;
  modalRef: BsModalRef = new BsModalRef();
  allTeamTimesheets:any[] = [];
  allTeamTimesheetRequests:any[] = [];

  timesheetObj:Timesheet = new Timesheet();
  startDate:any;
  endDate:any;

  constructor(
    private validationService:ValidationService,
    private modalService: BsModalService,
    private authenticationService : AuthenticationService,
    private timesheetService : TimesheetService,
  ) { 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {

    // Dynamic Subfeature Flags 
    let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    console.log(this.feature, this.userMapping);

    this.sectionViewInit();
  }

  sectionViewInit(){
    this.showAllTimesheetRequestsTable();
  }

  showAllTimesheetsTable(){
    this.isAllTimesheetTable = true;

    this.isAllTimesheetRequestTable = false;
    this.getAllTeamTimesheets();
  }

  showAllTimesheetRequestsTable(){
    this.isAllTimesheetRequestTable = true;

    this.isAllTimesheetTable = false;

    this.getMyReporteesTimesheetRequests();
  }

  getAllTeamTimesheets(template?: TemplateRef<any>){
    this.allTeamTimesheets = [];

    if(this.endDate){
      if(!this.validationService.validateNullUndefinedEmptyString(this.startDate)){
        this.alertMessage = "Please enter Start Date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
  
      if(!this.validationService.validateNullUndefinedEmptyString(this.endDate)){
        this.alertMessage = "Please enter End Date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }else{
      return;
    }

    let timesheetObj = new Timesheet();
    timesheetObj.managerId = this.currentUser.empId;
    timesheetObj.status = "Approved";
    timesheetObj.startDate = this.startDate;
    timesheetObj.endDate = this.endDate;
    this.timesheetService.getMyReporteesApprovedTimesheets(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allTeamTimesheets = response.serviceResponse;
        console.log("allTeamTimesheets :", this.allTeamTimesheets);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getMyReporteesTimesheetRequests(){
    this.allTeamTimesheetRequests = [];

    let timesheetObj = new Timesheet();
    timesheetObj.managerId = this.currentUser.empId;
    timesheetObj.status = "Pending";
    this.timesheetService.getMyReporteesTimesheetRequests(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allTeamTimesheetRequests = response.serviceResponse;
        console.log("allTeamTimesheetRequests :", this.allTeamTimesheetRequests);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  /* Approve / Reject Timesheet requests */
  updateTimesheetRequestById(template: TemplateRef<any>, timesheet:Timesheet, status:any){
    let timesheetObj = new Timesheet();
    timesheetObj.timesheetId = timesheet.timesheetId;
    timesheetObj.status = status;
    this.timesheetService.updateTimesheetRequestById(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showAllTimesheetRequestsTable()
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }


  /* View TImesheet details */ 
  getAllMyActivitiesByTimesheetId(timesheet:any){
    this.timesheetObj.allTimesheetActivities = [];

    let timesheetObj = new Timesheet();
    timesheetObj.timesheetId = timesheet.timesheetId;
    this.timesheetService.getAllMyActivitiesByTimesheetId(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.timesheetObj.allTimesheetActivities = response.serviceResponse;
        console.log("timesheetObj.allTimesheetActivities :", this.timesheetObj.allTimesheetActivities);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  








  //modals
  openTimesheetDetailsModal(template: TemplateRef<any>, timesheetObj:Timesheet){
    this.timesheetObj = new Timesheet();
    this.timesheetObj = timesheetObj;
    this.getAllMyActivitiesByTimesheetId(this.timesheetObj);
    this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
 }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }
}
