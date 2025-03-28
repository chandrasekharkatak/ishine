import { Component, OnInit } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { EmployeeService } from 'src/app/services/employee.service';
import { HomeComponent } from '../home.component';

@Component({
  selector: 'app-expied-po-and-project',
  templateUrl: './expied-po-and-project.component.html',
  styleUrls: ['./expied-po-and-project.component.css']
})
export class ExpiedPoAndProjectComponent implements OnInit {

  employee:any[] = [];
  modalRef: BsModalRef = new BsModalRef();
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

  sendEmail(poEndDate:any,projectName:any,poNo:any,poProjectType:any){

    console.log(poEndDate,projectName,poNo,poProjectType)
    
  }
}
