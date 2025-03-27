import { Component, OnInit } from '@angular/core';
import { first } from 'rxjs/operators';
import { EmployeeService } from 'src/app/services/employee.service';

@Component({
  selector: 'app-expied-po-and-project',
  templateUrl: './expied-po-and-project.component.html',
  styleUrls: ['./expied-po-and-project.component.css']
})
export class ExpiedPoAndProjectComponent implements OnInit {

  employee:any[] = [];
  constructor(private employeeService: EmployeeService) { }

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
}
