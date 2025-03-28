import { Component, OnInit } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { EmployeeService } from 'src/app/services/employee.service';
import { HomeComponent } from '../home.component';
import {  ExpiredEmailData } from 'src/app/models/expiredEmailmodel';
import { PoObject } from 'src/app/models/poObbjectData';

@Component({
  selector: 'app-expied-po-and-project',
  templateUrl: './expied-po-and-project.component.html',
  styleUrls: ['./expied-po-and-project.component.css']
})
export class ExpiedPoAndProjectComponent implements OnInit {

  
  employee:any[] = [];
  modalRef: BsModalRef = new BsModalRef();
  popUpMessege: any;
  constructor(private employeeService: EmployeeService, private homeComponent:HomeComponent) { }

  ngOnInit(): void {
    this.geExpiredtPoData();
  }

  geExpiredtPoData(){
    this.employeeService.getExpiredPo().pipe(first()).subscribe((response: any) => {
    if(response.serviceStatus == "Success"){
     this.employee =  response.serviceResponse
      console.log(response,":::::::::::::::::::Response,geExpiredtPoData")
    }
    });
  }

  closeModal(){
    this.homeComponent.cancelRequest();
  }

  alterModal(msg:any,){
    this.homeComponent.emailSentPopUp(msg);
  }

  sendEmail(poEndDate:any,projectName:any,poNo:any,poProjectType:any){

    console.log(poEndDate,projectName,poNo,poProjectType)
    let expiredEmailData: ExpiredEmailData = new ExpiredEmailData();
    let poObject: PoObject = new PoObject();
    poObject.poNo = poNo;
    poObject.projectName = projectName;
    poObject.endDate = poEndDate;
    poObject.poType = poProjectType;
    expiredEmailData.expiredData = poObject;
    let user = JSON.parse(sessionStorage.getItem('currentUser'));
    expiredEmailData.userEmail = user.email;
    console.log(expiredEmailData,"expiredEmailData",)
    console.log(expiredEmailData.expiredData,"expiredEmailData")
    console.log(expiredEmailData,"expiredEmailData")
    this.employeeService.sendExpiredPoEmail(expiredEmailData).pipe(first()).subscribe((response: any) => {
      if(response.serviceStatus == "Success"){
        this.popUpMessege =  response.serviceMessage;
         console.log(response,":::::::::::::::::::Response,geExpiredtPoData");
         this.closeModal();
        this.alterModal(this.popUpMessege);
       }
    });
  }
}
