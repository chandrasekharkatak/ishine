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
isFood:boolean = false;
 currentUser:any;
  currentEmployeeInfo:Employee = new Employee();
  todayDate: string;
  constructor(private empService : EmployeeService, private authenticationService: AuthenticationService) { 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    const today = new Date();
    this.todayDate = today.toISOString().split('T')[0];
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
        //this.loadProfileImage(this.currentEmployeeInfo.imageBytes)
  
      } else {
        console.error(response.serviceResponse);
      }
  
      
  
      
     
    }
}
