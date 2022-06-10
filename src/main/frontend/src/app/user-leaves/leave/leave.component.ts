import { Component, OnInit, TemplateRef } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { Feature } from 'src/app/models/feature';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { ValidationService } from 'src/app/services/validation.service';

@Component({
  selector: 'app-leave',
  templateUrl: './leave.component.html',
  styleUrls: ['./leave.component.css']
})
export class LeaveComponent implements OnInit {

  //flags 
  isCreation:boolean = false;
  isUpdation: boolean = false;
  isForm: boolean = false;
  // isTable: boolean = false;
  isLeaveHistoryTable: boolean = false;
  isLeaveBalanceTable: boolean = false;
  isLeaveApplicationsTable: boolean = false;
  isLeaveLogTable: boolean = false;

  //modal 
  alertMessage:any;
  modalRef: BsModalRef = new BsModalRef();

  //obj
  feature="Leave";
  currentUser:User;
  userMapping:any = {};

  constructor(
    private validationService:ValidationService,
    private modalService: BsModalService,
    private authenticationService : AuthenticationService,) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    console.log("this.currentUser : ", this.currentUser);
    
    // Dynamic Subfeature Flags 
    let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    console.log(this.feature, this.userMapping);

    this.sectionViewInit();
  }

  sectionViewInit(){
    this.showLeaveBalanceTable();
  }

  showCreateForm(){
    this.isForm = true;
    this.isCreation = true;
    this.isUpdation = false;

    this.isLeaveApplicationsTable = false;
    this.isLeaveHistoryTable = false;
    this.isLeaveBalanceTable = false;
    this.isLeaveLogTable = false;

    this.reset();
  }

  showLeaveHistoryTable() {
    this.isLeaveHistoryTable = true;
    this.isLeaveBalanceTable = false;
    this.isLeaveApplicationsTable = false;
    this.isLeaveLogTable = false;

    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;
  }

  showLeaveBalanceTable() {
    this.isLeaveBalanceTable = true;
    this.isLeaveHistoryTable = false;
    this.isLeaveApplicationsTable = false;
    this.isLeaveLogTable = false;

    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;
  }

  showLeaveApplicationsTable() {
    this.isLeaveApplicationsTable = true;
    this.isLeaveHistoryTable = false;
    this.isLeaveBalanceTable = false;
    this.isLeaveLogTable = false;

    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;
  }

  showLeaveLogTable() {
    this.isLeaveLogTable = true;
    this.isLeaveApplicationsTable = false;
    this.isLeaveHistoryTable = false;
    this.isLeaveBalanceTable = false;

    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;
  }

  reset(){
    
  }

  showUpdateForm(){
    this.isForm = true;
    this.isUpdation = true;
    this.isCreation = false;

    this.isLeaveApplicationsTable = false;
    this.isLeaveHistoryTable = false;
    this.isLeaveBalanceTable = false;
  }

  // Modals
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }

}
