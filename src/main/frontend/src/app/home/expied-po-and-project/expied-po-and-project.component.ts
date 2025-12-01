import { Component, OnInit, AfterViewInit, Input, Output, EventEmitter } from '@angular/core';
import { first } from 'rxjs/operators';
import { EmployeeService } from 'src/app/services/employee.service';
import { HomeComponent } from '../home.component';
import {  ExpiredEmailData } from 'src/app/models/expiredEmailmodel';
import { PoObject } from 'src/app/models/poObbjectData';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { User } from 'src/app/models/user';

@Component({
  standalone: false,
  selector: 'app-expied-po-and-project',
  templateUrl: './expied-po-and-project.component.html',
  styleUrls: ['./expied-po-and-project.component.css']
})
export class ExpiedPoAndProjectComponent implements OnInit {

  
  @Input() 
  employee:any[] = [];
  @Output() emailEvent = new EventEmitter<{ poEndDate: any, projectName: any, poNo: any, poProjectType: any }>();
  // modalRef:NgbModalRef;
  // popUpMessege: any= "Mail sent successfully...!";
  // currentUser: User;
  // userMapping: any = {};
  // feature = "Home";

  constructor(
      // private employeeService: EmployeeService, 
      private homeComponent:HomeComponent,
      // private authenticationService: AuthenticationService
      ) { 
    // this.authenticationService.currentUser.subscribe(x => {
    //   this.currentUser = x;
    // });
  }

  ngOnInit(): void {
    // console.log("HIIII")
    // this.geExpiredtPoData();
  }

  ngAfterViewInit(): void {
    // console.log("HIIII")
    // this.geExpiredtPoData();
  }

  // geExpiredtPoData(){
  //   this.employeeService.getExpiredPo().pipe(first()).subscribe((response: any) => {
  //   if(response.serviceStatus == "Success"){
  //    this.employee =  response.serviceResponse
  //     console.log(response,":::::::::::::::::::Response,geExpiredtPoData")
  //   }
  //   });
  // }

  closeModal(){
    this.homeComponent.cancelRequest();
  }

  // alterModal(msg:any,){
  //   this.homeComponent.emailSentPopUp(msg);
  // }

  sendEmail(poEndDate:any,projectName:any,poNo:any,poProjectType:any){

    const emailData = { poEndDate, projectName, poNo, poProjectType };
    this.emailEvent.emit(emailData);
    
  }
}
