import { Component, OnInit, TemplateRef } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { Feature } from 'src/app/models/feature';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { ValidationService } from 'src/app/services/validation.service';

@Component({
  selector: 'app-leave-config',
  templateUrl: './leave-config.component.html',
  styleUrls: ['./leave-config.component.css']
})
export class LeaveConfigComponent implements OnInit {

  //flags 
  isCreation:boolean = false;
  isUpdation: boolean = false;

  isHolidayForm:boolean = false;
  isLeaveRuleForm:boolean = false;
  isHolidayTable:boolean = false;
  isLeaveRuleTable:boolean = false;

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
    private authenticationService : AuthenticationService) {
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
    this.showLeaveRulesTable();
  }

  showAddHolidayForm(){
    this.isHolidayForm = true;
    this.isCreation = true;

    this.isLeaveRuleForm = false;
    this.isHolidayTable = false;
    this.isLeaveRuleTable = false;
    this.isUpdation = false;

    this.reset();
  }

  showHoliaysTable(){
    this.isHolidayTable = true;

    this.isLeaveRuleTable = false;
    this.isHolidayForm = false;
    this.isLeaveRuleForm = false;
    this.isUpdation = false;
    this.isCreation = false;

  }

  showLeaveRulesTable(){
    this.isLeaveRuleTable = true;

    this.isHolidayTable = false;
    this.isHolidayForm = false;
    this.isLeaveRuleForm = false;
    this.isUpdation = false;
    this.isCreation = false;

  }

  showUpdateLeaveRuleForm(){
    this.isHolidayForm = true;
    this.isUpdation = true;
    
    this.isLeaveRuleForm = false;
    this.isHolidayTable = false;
    this.isLeaveRuleTable = false;
    this.isCreation = false;

  }

  showUpdateHolidayForm(){
    this.isHolidayTable = true;
    this.isUpdation = true;
    
    this.isLeaveRuleForm = false;
    this.isLeaveRuleTable = false;
    this.isHolidayForm = false;
    this.isCreation = false;

  }

  reset(){

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
