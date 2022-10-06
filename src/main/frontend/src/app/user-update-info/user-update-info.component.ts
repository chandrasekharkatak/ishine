import { AfterViewInit, Component, ElementRef, EventEmitter, OnInit, Output, SecurityContext, ViewChild, } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { Employee } from '../models/employee';
import { ImageService } from '../services/image.service';
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

  isPreview:boolean = false;

  @ViewChild("draftTable") 
  private myDraftTable: EmployeeUpdateListComponent;

  draftObj:Employee = new Employee();
  previewObj:Employee = new Employee();

  @Output() docSubmit:EventEmitter<any> = new EventEmitter<any>();

  constructor(
    private updateUserInfoService: UpdateUserInfoService,
    private imageService : ImageService,
    private sanitizer: DomSanitizer,
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

  async onShowPreview(){
    console.log("draftObj : ", this.draftObj);
    
    let currentEmp = new Employee();
    currentEmp.employeementId = this.draftObj.employeementId;
    currentEmp.empId = this.draftObj.empId;
    currentEmp.isDraft = true;

    this.previewObj = this.draftObj;

    const docResponse:any = await this.imageService.getEmployeeDocuments(currentEmp).toPromise();
    if (docResponse.serviceStatus == 'Success') {
      this.previewObj.documentList = docResponse.serviceResponse;
      console.log("this.previewObj.documentList : ", this.previewObj.documentList);
    } else {
      console.log(docResponse.serviceResponse);
    }
  
    this.isPreview = true;

    setTimeout(()=>{
      this.previewObj.documentList.forEach((doc, index) => {
        if (doc.documentBytes) {
          let preview = document.getElementById(`docPreview${index + 1}`);
            let objectURL = 'data:image/*;base64,' + doc.documentBytes;
            let src: string = this.sanitizer.sanitize(SecurityContext.RESOURCE_URL, this.sanitizer.bypassSecurityTrustResourceUrl(objectURL));
            preview.setAttribute('src', src);
        }
      });
    }, 500);
  }

  onBack(){
    this.isPreview = false;
    this.isDraftAvailable = true;

    setTimeout(()=> {
      this.myDraftTable.myDraftList.push(this.draftObj);
    }, 500)
  }


  onDraftEdit(isDraft: boolean) {
    this.isDraftAvailable = isDraft;
    this.updateUserInfoService.setUserInfoObj(this.draftObj)
    this.showEmployeeInfoForm();
  }

  onPreviewSubmit(){
    this.docSubmit.emit();
  }

}
