import { Component, OnInit } from '@angular/core';
import { Employee } from 'src/app/models/employee';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';

@Component({
  selector: 'app-my-reimbursement',
  templateUrl: './my-reimbursement.component.html',
  styleUrls: ['./my-reimbursement.component.css']
})
export class MyReimbursementComponent implements OnInit {
selectedReason:any;
isTravel:boolean = false;
currencyType:any;
currentUser:any;
amount:any;
travelMode:any;
distance:any;
fromDateInput:any;
toDateInput:any;
purpose:any;
fileInput:any;
currentEmployeeInfo:Employee = new Employee();


  constructor(private empService : EmployeeService, private authenticationService: AuthenticationService) { 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    this.onGetEmployeeInfo();
  }

  onReasonSelect(){
    console.log(this.selectedReason);
    if(this.selectedReason === 'Travel'){
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
}
