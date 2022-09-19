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

  onUpdate(){
    alert("USER INFO Updated !!")
  }

}
