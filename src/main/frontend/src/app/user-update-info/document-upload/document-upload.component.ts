import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { Employee } from 'src/app/models/employee';
import { UpdateUserInfoService } from 'src/app/services/updateUserInfo.service';

@Component({
  selector: 'app-document-upload',
  templateUrl: './document-upload.component.html',
  styleUrls: ['./document-upload.component.css']
})
export class DocumentUploadComponent implements OnInit {

  @Output() loadInfoPreview: EventEmitter<any> = new EventEmitter<any>();


  currentEmployeeInfo:Employee = new Employee();

  constructor(
    private updateUserInfoService: UpdateUserInfoService,
  ) { }

  ngOnInit(): void {
    this.currentEmployeeInfo = this.updateUserInfoService.getUserInfoObj();
  }

  onSave(){
    this.loadInfoPreview.emit();
  }

}
