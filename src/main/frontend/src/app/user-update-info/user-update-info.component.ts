import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-user-update-info',
  templateUrl: './user-update-info.component.html',
  styleUrls: ['./user-update-info.component.css']
})
export class UserUpdateInfoComponent implements OnInit {

  isEmployeeInfo:boolean = true;
  isDocumentUpload:boolean = false;
  isInfoPreview:boolean =  false;

  constructor() { }

  ngOnInit(): void {
    
    // this.router.navigate(['/employee-info'])
  }

  showEmployeeInfoForm(){
    this.isEmployeeInfo = true;

    this.isDocumentUpload = false;
    this.isInfoPreview =  false;

    document.querySelector('.breadcrumb-item.active')?.classList.remove('active');
    document.querySelector('#employee-info')?.classList.toggle('active');
  }

  showDocumentUploadForm(){
    this.isDocumentUpload = true;

    this.isEmployeeInfo = false;
    this.isInfoPreview =  false;

    document.querySelector('.breadcrumb-item.active')?.classList.remove('active');
    document.querySelector('#document-upload')?.classList.toggle('active');
  }

  showInfoPreviewForm(){
    this.isInfoPreview =  true;

    this.isEmployeeInfo = false;
    this.isDocumentUpload = false;

    document.querySelector('.breadcrumb-item.active')?.classList.remove('active');
    document.querySelector('#info-preview')?.classList.toggle('active');
  }


}
