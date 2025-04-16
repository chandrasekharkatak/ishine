import { Component, OnInit } from '@angular/core';
import { Employee } from 'src/app/models/employee';
import { MyReimbursement } from 'src/app/models/reimbursement';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { ReimbursementService } from 'src/app/services/reimbursement.service';
import { SecurityContext, TemplateRef, ViewChild } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/internal/operators/first';



@Component({
  selector: 'app-my-reimbursement',
  templateUrl: './my-reimbursement.component.html',
  styleUrls: ['./my-reimbursement.component.css']
})
export class MyReimbursementComponent implements OnInit {
  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;
selectedReason:any;
isTravel:boolean = false;
alertMessage:any;
modalRef: BsModalRef = new BsModalRef();

 reimbursementInfo:MyReimbursement = new MyReimbursement();

currentEmployeeInfo:Employee = new Employee();
fromDate: string;
toDate: string;
currencyType:any;
currentUser:any;
amount:any;
travelMode:any;
distance:any;
fromDateInput:any;
toDateInput:any;
purpose:any;
fileInput:any;
expenditureType:any;

reimbursementObj: any = {
  currencyType:'',
  currentUser:'',
  amount:0,
  distance:'',
  travelMode: '',
  travelClass: '',
  expenditureType:'',
  fromDate: null,
  toDate: null,
  dateOfFood: null,
  purpose: '',
  fromLocation:'',
  toLocation:'',
  supportingDocument: null,
  kilometers: null,
  foodAllowanceType:null
};



  constructor(private empService : EmployeeService, 
    private authenticationService: AuthenticationService,
    private reimbursementService : ReimbursementService,
    private modalService: BsModalService,
  ) { 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {

    const today = new Date();
    this.toDate = today.toISOString().split('T')[0];
    today.setDate(today.getDate() - 60);
    this.fromDate = today.toISOString().split('T')[0];
    this.onGetEmployeeInfo();
  }

  onReasonSelect(){
    console.log(this.reimbursementObj.expenditureType);
    if(this.reimbursementObj.expenditureType === 'Travel'){
      this.isTravel = true;
      console.log(this.isTravel);
    }
    else{
      this.isTravel = false;
    }
  }

  async onGetEmployeeInfo(){
      this.currentEmployeeInfo = new Employee();
      let currentEmp = new Employee();
      currentEmp.empId = this.currentUser.empId;
      currentEmp.isDraft = false;
      //console.log("currentEmp : ", currentEmp);
      
      const response: any = await this.empService.getEmployeeByEmpId(currentEmp).toPromise();
      if (response.serviceStatus == "Success") {
        this.currentEmployeeInfo = response.serviceResponse;
      
        console.log("currentEmployeeInfo : ", this.currentEmployeeInfo);
  
      } else {
        console.error(response.serviceResponse);
      }
  
      
  
      
     
    }

    onKilometersChange() {
      if (this.reimbursementObj.vehicleType === 'Car' && this.reimbursementObj.distance > 0) {
        this.reimbursementObj.amount = this.reimbursementObj.distance * 12;
      } else if (this.reimbursementObj.vehicleType === 'Bike' && this.reimbursementObj.distance > 0){
        this.reimbursementObj.amount = this.reimbursementObj.distance * 6;
        
      }else{
        this.reimbursementObj.amount = 0;
      }
    }

    async resetForm() {
      this.reimbursementObj = {
        currencyType:'',
        currentUser:'',
        amount:0,
        distance:'',
        travelMode: '',
        travelClass: '',
        expenditureType:'',
        fromDate: null,
        toDate: null,
        dateOfFood: null,
        purpose: '',
        fromLocation:'',
        toLocation:'',
        supportingDocument: null,
        kilometers: null,
        foodAllowanceType:null
      };
    }

   async submitForm(template: TemplateRef<any>) {
 
     // Logic to handle form submission
      if (this.isValidForm()) {

         // First upload the file
    const fileFormData = new FormData();
    fileFormData.append('file', this.reimbursementObj.supportingDocument);
    fileFormData.append("displayName", this.reimbursementObj.supportingDocument.name);
    fileFormData.append("uploadedBy", this.currentEmployeeInfo.empId);

    try {
      const uploadResponse: any = await this.reimbursementObj.uploadFile(fileFormData).pipe(first()).toPromise();

      if (uploadResponse.serviceStatus === "Fail") {
        this.openAlertMod(template, `Error found: ${uploadResponse.serviceResponse}`);
        return;
      } else if (uploadResponse.serviceStatus !== "Success") {
        this.openAlertMod(template, uploadResponse.serviceResponse || "Unexpected file upload response.");
        return;
      }

 
     this.reimbursementInfo = new MyReimbursement();
     let reimbursementData = new MyReimbursement();

     reimbursementData.empId = this.currentEmployeeInfo.employeementId;
     reimbursementData.name = this.currentEmployeeInfo.name;
     reimbursementData.email = this.currentEmployeeInfo.email;
     reimbursementData.departmentName=this.currentEmployeeInfo.departmentName;
     reimbursementData.designationName=this.currentEmployeeInfo.designationName;
     reimbursementData.mobileNo=this.currentEmployeeInfo.mobileNo;
     reimbursementData.managerName=this.currentEmployeeInfo.managerName;
     reimbursementData.amount =this.reimbursementObj.amount;        
     reimbursementData.travelMode =this.reimbursementObj.travelMode;      
     reimbursementData.distance=this.reimbursementObj.distance;     
     reimbursementData.fromDate =this.reimbursementObj.fromDate;     
     reimbursementData.toDate =this.reimbursementObj.toDate;       
     reimbursementData.purpose=this.reimbursementObj.purpose;        
     reimbursementData.fileData =this.reimbursementObj.fileData; 
     reimbursementData.selectedCurrency =this.reimbursementObj.currencyType;
     reimbursementData.expenditureType = this.reimbursementObj.expenditureType;
     reimbursementData.vehicleType = this.reimbursementObj.vehicleType;
     reimbursementData.foodAllowanceType = this.reimbursementObj.foodAllowanceType;
     reimbursementData.dateOfFood = this.reimbursementObj.dateOfFood ; 

    console.log('reimbursementData Data::::::::::::::::::::::::::::', reimbursementData);
 
       this.onGetEmployeeInfo();
     
       const response: any = await this.reimbursementService.saveReimbursementData(reimbursementData).toPromise();
       if (response.serviceStatus == "Success") {
         alert("Success! Your request was processed successfully.");
         window.location.reload();
   
       } else {
         console.error(response.serviceResponse);
       }

      }catch (error) {
        console.error("Error during submit:", error);
        this.openAlertMod(template, "An unexpected error occurred while submitting the request.");
      }
    }
  }

       isValidForm() {
        return true; 
      }
      
      openAlertMod(template: TemplateRef<any>, message: any) {
        this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
        this.alertMessage = message;
      }
      
      cancelRequest() {
        this.modalRef.hide();
      }












     }
 




 


