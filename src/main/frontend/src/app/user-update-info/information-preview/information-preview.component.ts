import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { Employee } from 'src/app/models/employee';
import { UpdateUserInfoService } from 'src/app/services/updateUserInfo.service';

@Component({
  selector: 'app-information-preview',
  templateUrl: './information-preview.component.html',
  styleUrls: ['./information-preview.component.css']
})
export class InformationPreviewComponent implements OnInit {

  currentEmployeeInfo:Employee = new Employee();
  @Output() previewSubmit:EventEmitter<any> = new EventEmitter<any>();

  constructor(
    private updateUserInfoService: UpdateUserInfoService,
  ) { }

  ngOnInit(): void {
    this.currentEmployeeInfo = this.updateUserInfoService.getUserInfoObj();
  }

  async onSubmit() {
    const response:any = await this.updateUserInfoService.updateEmployeeInfo();
    console.log("onUpdate --> Preview : ", response);
    
    if (response.serviceStatus == "Success") {
      console.log(response.serviceResponse);
      this.previewSubmit.emit();
    } else {
      console.error(response.serviceResponse);
    }
  }

}
