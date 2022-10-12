
import { Component, OnInit,TemplateRef } from '@angular/core';
import { UploadPolicy } from 'src/app/models/UploadPolicy';
import { UploadPoliciesService } from 'src/app/services/upload-policies.service';
import { ValidationService } from 'src/app/services/validation.service';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { NotificationService } from 'src/app/services/notification.service';
import { first } from 'rxjs/operators';
import { Feature } from 'src/app/models/feature';


@Component({
  selector: 'app-upload-policies',
  templateUrl: './upload-policies.component.html',
  styleUrls: ['./upload-policies.component.css']
})
export class UploadPoliciesComponent implements OnInit {

  feature:any = "Policy Config";

  fileObj:UploadPolicy = new UploadPolicy();  
  files:any[] = [];
  policyName:any;
  currentUser: User;
  userMapping: any = {};

  //flags 
  isDocumentForm: boolean = false;
  isTable: boolean = false;

  //modal 
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();
  document:any[] = [];

  


  constructor(private uploadPoliciesService : UploadPoliciesService,
  private validationService: ValidationService,
  private modalService: BsModalService,
  private authenticationService: AuthenticationService,
  private notificationService: NotificationService,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);

   }

  ngOnInit(): void {
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });

    console.log(this.feature, this.userMapping);
    
    this.sectionViewInit();
  }

  sectionViewInit() {
    if(this.userMapping.upload_policy){
      this.showDocumentForm();
    }else if(this.userMapping.view_all_documents){
      this.showTable();
    }
  }
  showDocumentForm() {
    this.isDocumentForm = true;

    this.isTable = false;
   
  }
  showTable() {
    this.isTable = true;

    this.isDocumentForm = false;
    this.getAllDocuments();
  }

  onFileSelect(event:any){
    this.files = [];

    const uploadedFiles = event.target.files;
    console.log("uploadedFiles : ", uploadedFiles);
    if (uploadedFiles.length != 0) {
      for (let i = 0; i < uploadedFiles.length; i++) {
        let document = uploadedFiles[i];
        let fileName = document.name;

        let fileObj = {document : document,fileName : fileName}
        this.files.push(fileObj);
      };
    }
    console.log("Files : ", this.files);
  }
  reset() {
    this.policyName = null;
    this.files = [];
  }
  onUploadFiles(template: TemplateRef<any>){
    if(!this.validationService.validateNullUndefinedEmptyString(this.policyName)){
      this.alertMessage = "Please enter Policy Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if(!this.validationService.validateAlphaNumericWithSpace(this.policyName)){
      this.alertMessage = "Please enter Valid Policy Name, Alphabets, Numericals & space allowed !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (this.files.length == 0) {
      this.alertMessage = "Kindly Select Document !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    const formData = new FormData();
    this.files.forEach((file) =>{
      formData.append(`file`, file.document , file.fileName);
    });
    formData.append("policyName", this.policyName);
    formData.append("uploadedBy", this.currentUser.empId);

    console.log("Upload files : ", formData);
    this.uploadPoliciesService.uploadMultipleFiles(formData).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == 'Success') {
        this.openAlertMod(template, response.serviceResponse);
        this.reset();
        
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  
  }

  getAllDocuments(){
    this.document = [];
    this.uploadPoliciesService.getAllDocument().pipe(first()).subscribe((response:any) => {
      if (response.serviceStatus == "Success") {
        this.document =  response.serviceResponse;
        console.log("DocumentList : ", this.document);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }
   //modals
   openDeleteDocument(template: TemplateRef<any>, fileObj: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.fileObj = fileObj;
    console.log(this.fileObj);
  }

  onDeleteDocument(template: TemplateRef<any>) {
    this.cancelRequest();
   
    this.uploadPoliciesService.deleteDocument(this.fileObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }
  cancelRequest() {
    this.modalRef.hide();
  }

  page = 1;
  handlePageChange(event) {
    this.page = event;
  }
  }
