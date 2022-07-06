import { Component, OnInit, TemplateRef } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { Activity } from 'src/app/models/activity';
import { Timesheet } from 'src/app/models/timesheet';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { ValidationService } from 'src/app/services/validation.service';

@Component({
  selector: 'app-my-timesheet',
  templateUrl: './my-timesheet.component.html',
  styleUrls: ['./my-timesheet.component.css']
})
export class MyTimesheetComponent implements OnInit {
  
  feature="My Timesheets";
  currentUser:User;
  userMapping:any = {};

  //flags 
  isCreation:boolean = false;
  isUpdation: boolean = false;

  isTimesheetForm:boolean = false;
  isTimesheetTable:boolean = false;

  //modal 
  alertMessage:any;
  modalRef: BsModalRef = new BsModalRef();

  //Obj
  timesheetObj:Timesheet = new Timesheet();
  allTimesheetActivities:any[] = []

  constructor(
    private validationService:ValidationService,
    private modalService: BsModalService,
    private authenticationService : AuthenticationService,
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
    this.showCreateTimesheetForm();
  }

  showCreateTimesheetForm(){
    this.isTimesheetForm = true;
    this.isCreation = true;
    
    this.isTimesheetTable = false;
    this.isUpdation = false;

    this.reset();
  }

  showViewMyTimesheets(){
    this.isTimesheetTable = true;
    
    this.isTimesheetForm = false;
    this.isCreation = false;
    this.isUpdation = false;

  }

  showUpdateTimesheetForm(){
    this.isTimesheetForm = true;
    this.isUpdation = true;
    
    this.isTimesheetTable = false;
    this.isCreation = false;
  }

  reset(){
    this.timesheetObj = new Timesheet();
    this.timesheetObj.dayType = '';

    this.allTimesheetActivities = [];
    this.addInputActivityField()
  }


  // Manage Activity
  addInputActivityField() {
    let newActivityObj = new Activity();
    this.allTimesheetActivities.push(newActivityObj);
  }

  removeInputActivityField(activityObj) {
    this.allTimesheetActivities.forEach((value, index) => {
      if (value == activityObj) this.allTimesheetActivities.splice(index, 1);
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
