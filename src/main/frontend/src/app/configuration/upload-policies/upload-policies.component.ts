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
import { saveAs } from "file-saver";
import { Sort } from '@angular/material/sort';
import { LocationStrategy } from '@angular/common';
import * as moment from 'moment';
import { AppComponent } from 'src/app/app.component';




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

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;

  //flags 
  isDocumentForm: boolean = false;
  isTable: boolean = false;

  //modal 
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();
  document:any[] = [];

  //application properties value
  maxFileSize:any;
	maxRequestSize:any;
  // fileObj1: any = {};
  
  fileSize: number = 0;
  data:any;
  responseList:any[] = [];
  isreadEnabled: boolean = false;
  responsedata:any;


  constructor(private uploadPoliciesService : UploadPoliciesService,
  private validationService: ValidationService,
  private modalService: BsModalService,
  private authenticationService: AuthenticationService,
  private notificationService: NotificationService,
  private locationStrategy: LocationStrategy
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
    this.preventBackButton();
  }
  preventBackButton(){
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(()=>{
      history.pushState(null, null, location.href);
    })
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
    this.maxFileSize = parseInt(sessionStorage.maxFileSize);
    this.maxRequestSize = parseInt(sessionStorage.maxRequestSize);
    this.isTable = false;
    this.isreadEnabled = false;
    this.reset();
   
  }
  showTable() {
    this.isTable = true;
    this.isDocumentForm = false;
    this.isreadEnabled = false;
    this.getAllDocuments();
  }

  onFileSelect(event:any){
    this.files = [];
    let totalSize: number = 0;
    this.fileSize = 0;
    const uploadedFiles = event.target.files;
     console.log("maxfilesize: "+ this.maxFileSize );
    if (uploadedFiles.length != 0) {
      for (let i = 0; i < uploadedFiles.length; i++) { 
        let document = uploadedFiles[i];
        let fileName = document.name;
        this.fileSize =this.fileSize +  uploadedFiles[i].size / 1024 /1024;
        console.log(this.fileSize);
        let fileObj1 = {document : document,fileName : fileName}
        this.files.push(fileObj1);
        console.log("Files : ", this.files);
      }
    };
  }
  reset() {
    this.policyName = null;
    this.files = [];
  }
  onUploadFiles(template: TemplateRef<any>){
    this.policyName = this.policyName?.trim();
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

    let totalSize = parseFloat(this.fileSize.toFixed(2));
    if(totalSize>this.maxFileSize && totalSize>this.maxRequestSize){
      this.alertMessage ="File exceeds the size limit";
      this.openAlertMod(template, this.alertMessage);
      //this.fileSize = 0;
      return false;
    }

    const formData = new FormData();
    this.files.forEach((file) =>{
      formData.append(`file`, file.document , file.fileName);
    });
    formData.append("policyName", this.policyName);
    formData.append("uploadedBy", this.currentUser.empId);
    formData.append("readEnabled","false")

    console.log("Upload files : ", formData);
    this.uploadPoliciesService.uploadMultipleFiles(formData).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == 'Success') {
        this.openAlertMod(template, response.serviceResponse);
        this.reset();
        this.showTable()
        
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  
  }

  spaceTrimInpolicyName(){
    if(this.policyName != null || this.policyName != ''){
      this.policyName = this.policyName?.trim();
    }
  }

  getAllDocuments(){
    this.data='';
    this.document = [];
    this.uploadPoliciesService.getAllDocument().pipe(first()).subscribe((response:any) => {
      if (response.serviceStatus == "Success") {
        this.document =  response.serviceResponse;
        this.document.forEach(doc => {
          doc.createdOn = (doc.createdOn)? moment(doc.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        });
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

  onReadEnabled(template: TemplateRef<any>){
    this.cancelRequest();
    let fileObj = new UploadPolicy();
    fileObj.policyID = this.fileObj.policyID;
    fileObj.updatedBy = this.currentUser.empId;
    fileObj.readEnabled = true;
    console.log("Activate Survey : ", fileObj);
    this.uploadPoliciesService.changepolicyEnabledMode(fileObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      }else{
        this.openAlertMod(template, response.serviceResponse);
      }
    });

  }

  onReadDisabled(template: TemplateRef<any>){
    this.cancelRequest();
    let fileObj = new UploadPolicy();
    fileObj.policyID = this.fileObj.policyID;
    fileObj.updatedBy = this.currentUser.empId;
    fileObj.readEnabled = false;
    console.log("Activate Survey : ", fileObj);
    this.uploadPoliciesService.changepolicyEnabledMode(fileObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      }else{
        this.openAlertMod(template, response.serviceResponse);
      }
    });

  }
  openReadEnabledMod(template: TemplateRef<any>, fileObj:UploadPolicy) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.fileObj = fileObj;
  }
  onReadDisabledMod(template: TemplateRef<any>, fileObj:UploadPolicy) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.fileObj = fileObj;
  }
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }
  cancelRequest() {
    this.modalRef.hide();
  }

  policyReadResponseById(fileObj){
    this.isreadEnabled = true;
    this.isTable = false;
    this.showPolicyReadResponse(fileObj);

  }
  showPolicyReadResponse(fileObj){
    this.responsedata='';
    this.responseList = [];
    this.uploadPoliciesService.showPolicyReadResponse(fileObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.responseList = response.serviceResponse;
        for(let x of this.responseList){
          x.empId = "A-".concat(x.empId);
        }
        console.log(this.responseList);      
      }
      else{
          console.error(response.serviceResponse);
        }
    });
  }

  
  downloadFile(doc: any) {
    this.uploadPoliciesService.downloadDocument( doc.policyID).subscribe(blob => saveAs(blob,doc.fileName));
  }

  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  sortData(sort: Sort){	
    console.log(sort);
    if(sort.active){
      let sortParams:any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;      
    }
  }
}
  function compare(a: number | string, b: number | string, isAsc: boolean) {	
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  
  }