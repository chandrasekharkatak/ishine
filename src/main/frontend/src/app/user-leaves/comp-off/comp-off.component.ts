import { DatePipe } from '@angular/common';
import { Component, OnInit, TemplateRef } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { Feature } from 'src/app/models/feature';
import { Leave } from 'src/app/models/leave';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { LeaveService } from 'src/app/services/leave.service';
import { ValidationService } from 'src/app/services/validation.service';

@Component({
  selector: 'app-comp-off',
  templateUrl: './comp-off.component.html',
  styleUrls: ['./comp-off.component.css']
})
export class CompOffComponent implements OnInit {

  //flags 
  isCreation:boolean = false;
  isUpdation: boolean = false;
  isForm: boolean = false;
  isTable: boolean = false;

  //modal 
  alertMessage:any;
  modalRef: BsModalRef = new BsModalRef();

  //obj
  feature="Comp off";
  currentUser:User;
  userMapping:any = {};

  compOffObj:Leave = new Leave();
  compOffReasons:any[] = [];

  constructor(
    private validationService:ValidationService,
    private modalService: BsModalService,
    private authenticationService : AuthenticationService,
    private leaveService : LeaveService,
    private datePipe: DatePipe,) {
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
    this.getAllCompOffReasons();
    this.compOffObj.leaveType = 'Compensatory Off'
  }

  sectionViewInit(){
    this.showTable();
  }

  showCreateForm(){
    this.isForm = true;
    this.isTable = false;
    this.isCreation = true;
    this.isUpdation = false;

    this.reset();
  }

  showTable(){
    this.isTable = true;
    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;

  }

  reset(){
    this.compOffObj = new Leave();
  }

  showUpdateForm(){
    this.isForm = true;
    this.isTable = false;
    this.isUpdation = true;
    this.isCreation = false;

  }
    // Modals
    openAlertMod(template: TemplateRef<any>, message: any) {
      this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
      this.alertMessage = message;
    }
  
    cancelRequest() {
      this.modalRef.hide();
    }

    getAllCompOffReasons(){
      this.compOffReasons = [];
  
      this.leaveService.getAllCompOffReasons().pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.compOffReasons = response.serviceResponse;
          console.log("compOffReasons : ", this.compOffReasons);
        } else {
          console.error(response.serviceResponse);
        }
      });
    }

    validateLeavetObj(leaveObj:Leave, template: TemplateRef<any>){
      
      if(!this.validationService.validateNullUndefinedEmptyString(leaveObj.compOffId)){
        this.alertMessage = "Please select comp off reason !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if(!this.validationService.validateNullUndefinedEmptyString(leaveObj.description)){
        this.alertMessage = "Please enter comp off description !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      return true;
    }

    onApplyCompOff(template: TemplateRef<any>){
      let inputValidated:boolean  = this.validateLeavetObj(this.compOffObj, template)
      if(!inputValidated) return;
  
      this.compOffObj.leaveTypeMasterId = 5;
      this.compOffObj.fromDate = this.compOffObj.toDate = this.datePipe.transform(new Date(), 'dd-MM-yyyy');
      this.compOffObj.noOfDays = 1;

      this.compOffObj.empId = this.currentUser.empId;
      this.compOffObj.createdBy = this.currentUser.empId;
      this.compOffObj.managerId = 1; 
  
      console.log("Apply Comp off : ", this.compOffObj);
      this.leaveService.applyLeave(this.compOffObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.openAlertMod(template, response.serviceResponse);
          this.showTable();
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }
      });
    }

}
