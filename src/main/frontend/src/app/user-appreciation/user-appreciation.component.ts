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
import { AppComponent } from '../app.component';
import * as moment from 'moment';

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
  appreciationcount:any;
  isAppreciateRecieved:boolean = false;
  isAppreciationSent:boolean = false;
  isReceievedData:boolean;
  isSentData:boolean;
  clickCount: number = 0;
  clickCount2:number = 0;
  countSent:number = 0;
  countRecieved:number=0;

  sent : boolean = false;
  recieve : boolean = false;


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
    this.appreciationEventInfo.fromDate = moment(this.appreciationEventInfo.fromDate).format(AppComponent.DATE_FORMAT);
    this.appreciationEventInfo.toDate = moment(this.appreciationEventInfo.toDate).format(AppComponent.DATE_FORMAT);
    this.getAppreciateEmployeeByCurrentUser();
    this.preventBackButton();
    this.CountMyAppreciationBYcurrentUser();
    //console.log("appreicationEventInfo :",this.currentUser.appreciationEventInfo);
  }
  preventBackButton(){
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(()=>{
      history.pushState(null, null, location.href);
    })
  }

  showMyAppreciation(){
    this.isReceievedData = true;
    this.isSentData = false;
    this.isAppreciationSent = false;
    return this.isAppreciateRecieved = true;

  }

  hideMyAppreication(){
    this.sent=false;
    this.recieve=false;
    this.isSentData=false;
    this.isAppreciationSent=false;
    this.isReceievedData = false;
    return this.isAppreciateRecieved = false;

  }
  // toggleFunctionforreceived() {
  //   this.clickCount2 = 0;
    
  //   if (this.clickCount === 0) {
  //     this.showMyAppreciation();
  //     this.recieve=true;
  //     this.sent=false;
  //     this.clickCount = 1;
  //   } else {
  //     this.hideMyAppreication();
  //     this.clickCount = 0;
  //   }
  // }
  showSentAppreciation(){
    this.isReceievedData = false;
    this.isAppreciateRecieved = false;
    this.isSentData = true;
    return this.isAppreciationSent = true;

  }
  hideSentAppreciation(){
    this.sent=false;
    this.recieve=false;
    this.isAppreciateRecieved=false;
    this.isReceievedData=false;
    this.isAppreciationSent = false;
    return this.isAppreciationSent = false;
  }
//   toggleFunctionforsent() {
// this.clickCount = 0;
   

//     if (this.clickCount2 === 0) {
//       this.showSentAppreciation();
//       this.sent=true;
//       this.recieve=false;
//       this.clickCount2 = 1;
//     } else {
//       this.hideSentAppreciation();
//       this.clickCount2 = 0;
//     }
 
//   }


toggleFunctionforsent() {
  if (!this.sent) {
    this.showSentAppreciation();
    this.sent = true;
    this.recieve = false;
    this.showDetailTable = false;  // hide table until user clicks a count
  } else {
    this.hideAllAppreciation();
  }
}

toggleFunctionforreceived() {
  if (!this.recieve) {
    this.showMyAppreciation();
    this.recieve = true;
    this.sent = false;
    this.showDetailTable = false;
  } else {
    this.hideAllAppreciation();
  }
}

hideAllAppreciation() {
  this.sent = false;
  this.recieve = false;
  this.isSentData = false;
  this.isReceievedData = false;
  this.isAppreciationSent = false;
  this.isAppreciateRecieved = false;
  this.showDetailTable = false;
}


  // appreciationSentToEmployeeDetailsByCU(){
  //   this.employeeObj.appreciationBy = this.currentUser.empId;
  //   console.log(this.employeeObj,'lalalalalal');
  //   this.appreciationService.getAppreciateEmployeeByCurrentUser(this.employeeObj).pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus == "Success") {
  //       this.appreciateEmployeeByCurrentUser = response.serviceResponse;
  //       //console.log("getAppreciateEmployeeByCurrentUser : ", this.appreciateEmployeeByCurrentUser);
  //     } else {
  //       console.error(response.serviceResponse);
  //     }

  //     this.getAllEmployees();
  //   });
  // }



  selectedAppreciationType = '';
selectedAppreciationMode: 'sent' | 'received' = 'sent';
groupedData: any[] = [];
showDetailTable = false;
  openDetailTable(appreciationType: string, mode: 'sent' | 'received') {
  this.selectedAppreciationType = appreciationType;
  this.selectedAppreciationMode = mode;
  const payload = { appreciationBy: this.currentUser.empId };

  const apiCall = mode === 'sent'
    ? this.appreciationService.appreciationSentByCurrentUser(payload)
    : this.appreciationService.appreciationReceivedByCurrentUser(payload);

  apiCall.subscribe({
    next: (res: any) => {
      if (res?.serviceStatus === 'Success') {
        const rawList = res.serviceResponse;

        // Filter by appreciation type
        const filtered = rawList.filter(
  (item: any) =>
    item.appreciateType?.toLowerCase().trim() === appreciationType.toLowerCase().trim()
);
        // Group by employeeId and count
        const groupedMap = new Map<string, any>();

        filtered.forEach((item: any) => {
          const key = item.employmentIdAccToET;
          if (groupedMap.has(key)) {
            groupedMap.get(key).count += 1;
          } else {
            groupedMap.set(key, {
              employmentId: item.employmentIdAccToET,
              name: item.name,
              department: item.departmentName,
              count: 1,
            });
          }
        });

        this.groupedData = Array.from(groupedMap.values());
        this.showDetailTable = true;
      } else {
        this.groupedData = [];
        this.showDetailTable = false;
      }
    },
    error: (err) => {
      console.error(err);
      this.groupedData = [];
      this.showDetailTable = false;
    },
  });

  console.log(this.groupedData);
}


  reset(){
   this.employeeObj = new Employee();
   this.employeeObj.empId='';
   this.employeeObj.managerName=null;
   this.employeeObj.appreciateType=null;
   this.employeeObj.reason=null;
   this.countRecieved=0;
   this.countSent=0;

  }
  getAppreciateEmployeeByCurrentUser(){
    this.employeeObj.appreciationBy = this.currentUser.empId;
    this.employeeObj.appreciationEventId = this.currentUser.appreciationEventInfo.appreciationEventId;
    //console.log(this.employeeObj.appreciationEventId, "this.employeeObj.appreciationEventId");
    //console.log(this.currentUser.appreciationEventInfo.appreciationEventId, "checking current event");
    this.appreciationService.getAppreciateEmployeeByCurrentUser(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.appreciateEmployeeByCurrentUser = response.serviceResponse;
        //console.log("getAppreciateEmployeeByCurrentUser : ", this.appreciateEmployeeByCurrentUser);
      } else {
        console.error(response.serviceResponse);
      }

      this.getAllEmployees();
    });

  }
  CountMyAppreciationBYcurrentUser(){
    this.employeeObj.appreciationTo = this.currentUser.empId;
    //console.log("CurrentEmpId :",this.employeeObj.appreciationTo);
    this.appreciationService.CountMyAppreciationBYcurrentUser(this.employeeObj).pipe(first()).subscribe((response :any) =>{
      if(response.serviceStatus == "Success"){
        this.appreciationcount = response.serviceResponse;
         this.countSent = this.appreciationcount.appreciationSent;
         this.countRecieved = this.appreciationcount.appreciationReceived;

        //console.log("appreicationcount :", this.appreciationcount);
        //console.log("sent count",this.countSent);
        //console.log("count received",this.countRecieved);

      }
      else {
        console.error(response.serviceResponse);
      }
    });
  }
  getAllEmployees() {

    this.appreciationService.getAllEmployees().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allEmployee = response.serviceResponse;

        this.allEmployee = this.allEmployee.filter(x => x.employmentstatus != 'InActive' && x.empId != this.currentUser.empId);

        this.allEmployee = this.allEmployee.sort((a, b) => a.name.toLowerCase()> b.name.toLowerCase()? 1 : -1);

        //console.log("appreciation : ", this.allEmployee);

       this.appreciateEmployeeByCurrentUser.forEach((x) => {
       //console.log(x.appreciationTo , " :   appriciation to");
       })

       this.allEmployee.forEach((employee)=>{
         const appreciatedEmployee = this.appreciateEmployeeByCurrentUser.find((apprEmployee)=> employee.empId == apprEmployee.appreciationTo);
         if(appreciatedEmployee){
          employee.isAppreciated = true;
         }else{
          employee.isAppreciated = false;
         }
      });

        //console.log(this.allAppreciateEmployee, "allAppreciateEmployee");
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
    // if(!this.validationService.validateAlphaWithSpace(employeeObj.reason)) {
    //   this.alertMessage = "Only support letters in Description !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }
    // if(!this.validationService.validateTeamName(employeeObj.reason)){
    //   this.alertMessage = "Please enter a valid comment for why you want to give appreciation?"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }
    return true;
  }

  submitAppreciation(template: TemplateRef<any>)
  {
    let inputValidated: boolean = this.validateAppreciation(this.employeeObj, template)
    if (!inputValidated) return;


    //email
    this.employeeObj.email = this.currentUser.email;
    this.employeeObj.emailAppreciated = this.employee.email;
    //console.log("current user mail" + this.employeeObj.email)
    //console.log("appreciation mail" + this.employeeObj.emailAppreciated)


    //employment id
    this.employeeObj.appreciationBy = this.currentUser.empId;
    this.employeeObj.appreciationTo = this.employee.empId;
    //console.log("currentuser employmentId" + this.employeeObj.appreciationBy)
    //console.log("appreciationTo employmentId" + this.employeeObj.appreciationTo)

    //name
    this.employeeObj.name = this.currentUser.name;
    this.employeeObj.nameAppreciate = this.employee.name;
    //console.log("currentuser name" + this.employeeObj.name)
    //console.log("appreciation name" + this.employeeObj.nameAppreciate)

    //manager mail
    //console.log("this.appreciationEventInfo",this.appreciationEventInfo);
    //console.log("this.enableAppreciation.appreciationEventID",this.appreciationEventInfo.appreciationEventId);
    this.employeeObj.appreciationEventId= this.appreciationEventInfo.appreciationEventId;
    //console.log("this.employeeObj.appreciationEventID",this.employeeObj.appreciationEventId);


    this.employeeObj.appreciateType=this.employeeObj.appreciateType;
    this.employeeObj.reason=this.employeeObj.reason;



    this.appreciationService.submitAppreciation(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.all = response.serviceResponse;
        //console.log("appreciation : ", this.all)
        this.openAlertMod(template, response.serviceResponse);
        this.ngOnInit();
        this.reset();
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getManagerByEmpId(empId: any){

    //console.log(empId);
    this.employee = this.allEmployee.find(x => x.empId == empId);
    //console.log(this.employee.managerName);
    this.employeeObj.managerName = this.employee.managerName;
    this.employeeObj.managerMail = this.employee.managerMail;
    //console.log("manager mail" + this.employeeObj.managerMail)

    this.employeeObj.empIdAppreciated=this.employee.empId;
    this.employeeObj.name=this.employee.name;
    //console.log("name" +this.employeeObj.name )
    //console.log(this.employeeObj.empIdAppreciated);
  }

  cancelRequest() {
    this.modalRef.hide();
  }
}
