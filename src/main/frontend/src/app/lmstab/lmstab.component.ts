import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { EmployeeService } from '../services/employee.service';

@Component({
  selector: 'app-lmstab',
  templateUrl: './lmstab.component.html',
  styleUrls: ['./lmstab.component.css']
})
export class LmstabComponent implements OnInit {

  private lmsbaseurl:any = '';
  lmsauthentication:any;
  currentUserName = "";
   currentUser: User;
  constructor(private employeeService: EmployeeService,
    private authenticationService: AuthenticationService,
     private router: Router
  ) {  this.authenticationService.currentUser.subscribe(x => {
    this.currentUser = x;
    this.currentUserName = this.currentUser.name.split(" ")[0];
    this.currentUserName = this.currentUserName[0].toUpperCase() + this.currentUserName.slice(1).toLowerCase();
  });}

  ngOnInit(): void {
    this.LmsRedirection();
  }

  LmsRedirection(){
    let obj = new Object();
  obj = { email: this.currentUser.email,token:sessionStorage.getItem('token')};
  
     
  //obj = { email: "mohamed.owais@apmosys.com"};
  //obj = { email: "mohamed2.owais@apmosys.com"};
   this.employeeService.IsValidateLMSPORTAL(obj).subscribe((response:any)=>{
     //this.lmsauthentication = response.serviceResponse;
     this.lmsauthentication = response.serviceResponse;
     
      if(response.serviceStatus=="success"){
       window.open(response.serviceResponse, '_blank');
       this.router.navigate(['/home']);
     }
    else{
       window.open(`${this.lmsbaseurl}home/sign_up`,'_blank');
       this.router.navigate(['/home']);
        
     }
   })
  
  }

}
