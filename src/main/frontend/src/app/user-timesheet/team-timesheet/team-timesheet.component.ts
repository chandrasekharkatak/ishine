import { Component, OnInit, TemplateRef } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
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
    // let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    // featureMap.subFeatures?.forEach(sub => {
    //   this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    // });
    // console.log(this.feature, this.userMapping);

    this.sectionViewInit();
  }

  sectionViewInit(){
    this.showAllTimesheetsTable();
  }

  showAllTimesheetsTable(){
    this.isAllTimesheetTable = true;

    this.isAllTimesheetRequestTable = false;

  }

  showAllTimesheetRequestsTable(){
    this.isAllTimesheetRequestTable = true;
    
    this.isAllTimesheetTable = false;

  }

  getMyReporteesTimesheetRequests(projectId:any){
    this.allTeamTimesheets = [];

    let timesheetObj = new Timesheet();
    timesheetObj.managerId = this.currentUser.empId;
    this.timesheetService.getMyReporteesTimesheetRequests(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allTeamTimesheets = response.serviceResponse;
        console.log("allTeamTimesheets :", this.allTeamTimesheets);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }










  //modals
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }
}
