import { AfterViewInit, Component, ElementRef, OnInit, ViewChild, } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Employee } from '../models/employee';
import { UpdateUserInfoService } from '../services/updateUserInfo.service';
import { EmployeeUpdateListComponent } from './employee-update-list/employee-update-list.component';

@Component({
  selector: 'app-user-update-info',
  templateUrl: './user-update-info.component.html',
  styleUrls: ['./user-update-info.component.css']
})
export class UserUpdateInfoComponent implements OnInit, AfterViewInit {

  isDraftAvailable: boolean = false;

  isEmployeeInfo: boolean = false;
  isDocumentUpload: boolean = false;
  isInfoPreview: boolean = false;

  @ViewChild("draftTable") 
  private myDraftTable: EmployeeUpdateListComponent;

  draftObj:Employee = new Employee();

  constructor(
    private updateUserInfoService: UpdateUserInfoService,
  ) { }

  ngOnInit(): void {
  }
  
  ngAfterViewInit(): void {
    this.sectionViewInit(); 
  }

  async sectionViewInit() {
    this.draftObj = await this.updateUserInfoService.getDraftByEmpId();
    
    if (this.draftObj) {
      this.isDraftAvailable = true;
      setTimeout(()=> {
        this.myDraftTable.myDraftList.push(this.draftObj);
      }, 500)
    } else {
      this.showEmployeeInfoForm();
    }
  }

  showEmployeeInfoForm() {
    this.isEmployeeInfo = true;

    this.isDocumentUpload = false;
    this.isInfoPreview = false;

    document.querySelector('.breadcrumb-item.active')?.classList.remove('active');
    document.querySelector('#employee-info')?.classList.toggle('active');
  }

  showDocumentUploadForm() {
    this.isDocumentUpload = true;

    this.isEmployeeInfo = false;
    this.isInfoPreview = false;

    document.querySelector('.breadcrumb-item.active')?.classList.remove('active');
    document.querySelector('#document-upload')?.classList.toggle('active');
  }

  showInfoPreviewForm() {
    this.isInfoPreview = true;

    this.isEmployeeInfo = false;
    this.isDocumentUpload = false;

    document.querySelector('.breadcrumb-item.active')?.classList.remove('active');
    document.querySelector('#info-preview')?.classList.toggle('active');
  }


  onDraftEdit(isDraft: boolean) {
    this.isDraftAvailable = isDraft;
    this.updateUserInfoService.setUserInfoObj(this.draftObj)
    this.showEmployeeInfoForm();
  }

}
