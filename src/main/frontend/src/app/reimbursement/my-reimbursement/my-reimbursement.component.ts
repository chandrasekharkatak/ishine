import { Component, OnInit } from '@angular/core';
import { Employee } from 'src/app/models/employee';
import { MyReimbursement } from 'src/app/models/reimbursement';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { ReimbursementService } from 'src/app/services/reimbursement.service';

@Component({
  selector: 'app-my-reimbursement',
  templateUrl: './my-reimbursement.component.html',
  styleUrls: ['./my-reimbursement.component.css']
})
export class MyReimbursementComponent implements OnInit {
selectedReason:any;
isTravel:boolean = false;

 reimbursementInfo:MyReimbursement = new MyReimbursement();

currentEmployeeInfo:Employee = new Employee();
todayDate: string;
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
  amount:'',
  distance:'',
  travelMode: '',
  travelClass: '',
  expenditureType:'',
  fromDate: null,
  toDate: null,
  purpose: '',
  fromLocation:'',
  toLocation:'',
  supportingDocument: null, // File
};



  constructor(private empService : EmployeeService, 
    private authenticationService: AuthenticationService,
    private reimbursementService : ReimbursementService,
  ) { 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    const today = new Date();
    this.todayDate = today.toISOString().split('T')[0];
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

   async submitForm() {
 
     // Logic to handle form submission
      if (this.isValidForm()) {
 
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
 
     console.log('reimbursementData Data::::::::::::::::::::::::::::', reimbursementData);
 
       this.onGetEmployeeInfo();
     
       const response: any = await this.reimbursementService.saveReimbursementData(reimbursementData).toPromise();
       if (response.serviceStatus == "Success") {
         alert("Success! Your request was processed successfully.");
         window.location.reload();
   
       } else {
         console.error(response.serviceResponse);
       }
     }
 }


 isValidForm() {
  return true; 
}
 

}
