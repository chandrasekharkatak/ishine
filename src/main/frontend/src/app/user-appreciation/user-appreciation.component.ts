import { Component, OnInit,TemplateRef } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { ValidationService } from 'src/app/services/validation.service';
import { AppreciationService } from '../services/appreciation.service';
import { first } from 'rxjs/operators';
import { Employee } from '../models/employee';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { enableAppreciation } from '../models/enableAppreciation';
import { LocationStrategy } from '@angular/common';

//import { Appreciation } from 'src/app/models/Appreciation';


@Component({
  selector: 'app-user-appreciation',
  templateUrl: './user-appreciation.component.html',
  styleUrls: ['./user-appreciation.component.css']
})
export class UserAppreciationComponent implements OnInit {

 // jobRoleObj: Appreciation = new Appreciation();
  //modal 
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();

  allEmployee: any[];
  all:any;
  employeeObj: Employee = new Employee();
  employee:any
  managername:any
  employeementid:any
  currentUser: User;
  empid:any;
  appreciationEventInfo:enableAppreciation;
  appreciateEmployeeByCurrentUser: any[] = [];
  allAppreciateEmployee:any;
  
 
  constructor(private appreciationService : AppreciationService,
    private authenticationService: AuthenticationService,
    private modalService: BsModalService,
    private validationService: ValidationService,
    private locationStrategy: LocationStrategy
    ) { 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    this.employeeObj.empId=this.currentUser.empId;
    this.employeeObj.appreciateType = 'You are my Star';	 
    this.appreciationEventInfo = this.currentUser.appreciationEventInfo;
    this.getAppreciateEmployeeByCurrentUser();
    this.preventBackButton();
  }
  preventBackButton(){
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(()=>{
      history.pushState(null, null, location.href);
    })
  }

  reset(){
   this.employeeObj = new Employee();
   this.employeeObj.empId='';
   this.employeeObj.managerName=null;
   this.employeeObj.appreciateType=null;
   this.employeeObj.reason=null;
  }
  getAppreciateEmployeeByCurrentUser(){
    this.employeeObj.appreciationBy = this.currentUser.employeementId;
    this.employeeObj.appreciationEventId = this.currentUser.appreciationEventInfo.appreciationEventId;
    console.log(this.employeeObj.appreciationEventId, "this.employeeObj.appreciationEventId");
    console.log(this.currentUser.appreciationEventInfo.appreciationEventId, "checking current event");
    this.appreciationService.getAppreciateEmployeeByCurrentUser(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.appreciateEmployeeByCurrentUser = response.serviceResponse;
        console.log("getAppreciateEmployeeByCurrentUser : ", this.appreciateEmployeeByCurrentUser);
      } else {
        console.error(response.serviceResponse);
      }

      this.getAllEmployees();
    });

  }
  getAllEmployees() {
    
    this.appreciationService.getAllEmployees().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allEmployee = response.serviceResponse;

        this.allEmployee = this.allEmployee.filter(x => x.employmentstatus != 'InActive' && x.empId != this.currentUser.empId);

        this.allEmployee = this.allEmployee.sort((a, b) => a.name.toLowerCase()> b.name.toLowerCase()? 1 : -1);

        console.log("appreciation : ", this.allEmployee);

       this.appreciateEmployeeByCurrentUser.forEach((x) => {
       console.log(x.appreciationTo , " :   appriciation to");
       })
       
       this.allEmployee.forEach((employee)=>{
         const appreciatedEmployee = this.appreciateEmployeeByCurrentUser.find((apprEmployee)=> employee.employeementId == apprEmployee.appreciationTo);
         if(appreciatedEmployee){
          employee.isAppreciated = true;
         }else{
          employee.isAppreciated = false;
         }
      });

        console.log(this.allAppreciateEmployee, "allAppreciateEmployee");
      } else {
        console.error(response.serviceResponse)
      }
    });
  }
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  validateAppreciation(employeeObj: Employee, template: TemplateRef<any>) {

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.name)) {
      this.alertMessage = "Please Select Employee Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.appreciateType)) {
      this.alertMessage = "Please select AppreciateType !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.reason)) {
      this.alertMessage = "Why you want to give Appreciation? Should not be Empty!!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if(!this.validationService.validateAlphaWithSpace(employeeObj.reason)) {	
      this.alertMessage = "Only support letters in Description !!"	
      this.openAlertMod(template, this.alertMessage);	
      return false;	
    }
    if(!this.validationService.validateTeamName(employeeObj.reason)){
      this.alertMessage = "Please enter a valid comment for why you want to give appreciation?"
      this.openAlertMod(template, this.alertMessage);	
      return false;	
    }
    return true;
  }
 
  submitAppreciation(template: TemplateRef<any>)
  {
    let inputValidated: boolean = this.validateAppreciation(this.employeeObj, template)
    if (!inputValidated) return;
   
  
    //email
    this.employeeObj.email = this.currentUser.email;
    this.employeeObj.emailAppreciated = this.employee.email;
    console.log("current user mail" + this.employeeObj.email)
    console.log("appreciation mail" + this.employeeObj.emailAppreciated)

    
    //employment id
    this.employeeObj.appreciationBy = this.currentUser.employeementId;
    this.employeeObj.appreciationTo = this.employee.employeementId;
    console.log("currentuser employmentId" + this.employeeObj.appreciationBy)
    console.log("appreciationTo employmentId" + this.employeeObj.appreciationTo)

    //name
    this.employeeObj.name = this.currentUser.name;
    this.employeeObj.nameAppreciate = this.employee.name;
    console.log("currentuser name" + this.employeeObj.name)
    console.log("appreciation name" + this.employeeObj.nameAppreciate)

    //manager mail
    console.log("this.appreciationEventInfo",this.appreciationEventInfo);
    console.log("this.enableAppreciation.appreciationEventID",this.appreciationEventInfo.appreciationEventId);
    this.employeeObj.appreciationEventId= this.appreciationEventInfo.appreciationEventId;
    console.log("this.employeeObj.appreciationEventID",this.employeeObj.appreciationEventId);


    this.employeeObj.appreciateType=this.employeeObj.appreciateType;
    this.employeeObj.reason=this.employeeObj.reason;

   

    this.appreciationService.submitAppreciation(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.all = response.serviceResponse;
        console.log("appreciation : ", this.all)
        this.openAlertMod(template, response.serviceResponse); 
        this.getAppreciateEmployeeByCurrentUser();
        this.reset();  
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getManagerByEmpId(empId: any){
    
    console.log(empId);
    this.employee = this.allEmployee.find(x => x.empId == empId);
    console.log(this.employee.managerName);
    this.employeeObj.managerName = this.employee.managerName;
    this.employeeObj.managerMail = this.employee.managerMail;
    console.log("manager mail" + this.employeeObj.managerMail)

    this.employeeObj.empIdAppreciated=this.employee.empId;
    this.employeeObj.name=this.employee.name;
    console.log("name" +this.employeeObj.name )
    console.log(this.employeeObj.empIdAppreciated);
  }

  cancelRequest() {
    this.modalRef.hide();
  }
}