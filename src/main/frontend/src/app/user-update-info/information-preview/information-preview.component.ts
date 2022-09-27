import { Component, OnInit } from '@angular/core';
import { Employee } from 'src/app/models/employee';
import { UpdateUserInfoService } from 'src/app/services/updateUserInfo.service';

@Component({
  selector: 'app-information-preview',
  templateUrl: './information-preview.component.html',
  styleUrls: ['./information-preview.component.css']
})
export class InformationPreviewComponent implements OnInit {

  currentEmployeeInfo:Employee = new Employee();

  constructor(
    private updateUserInfoService: UpdateUserInfoService,
  ) { }

  ngOnInit(): void {
    this.currentEmployeeInfo = this.updateUserInfoService.getUserInfoObj();
  }

  async onSubmit() {
    const response:any = this.updateUserInfoService.updateEmployeeInfo();
    console.log("onUpdate --> Preview : ", response);
    
    if (response.serviceStatus == "Success") {
      console.log(response.serviceResponse);
    } else {
      console.error(response.serviceResponse);
    }
  }

}
