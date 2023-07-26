import { LocationStrategy } from '@angular/common';
import { Component, OnInit, TemplateRef } from '@angular/core';
import { Sort } from '@angular/material/sort';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Feature } from 'src/app/models/feature';
import { Newsletter } from 'src/app/models/newsletter';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { NewsletterService } from 'src/app/services/newsletter.service';
import { ValidationService } from 'src/app/services/validation.service';
import { saveAs } from "file-saver";

@Component({
  selector: 'app-newsletter-config',
  templateUrl: './newsletter-config.component.html',
  styleUrls: ['./newsletter-config.component.css']
})
export class NewsletterConfigComponent implements OnInit {

  feature:any = "Newsletter Config";
  currentUser: User;
  userMapping: any = {};

  // Sorting 
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;
  
  isUploadForm: boolean = false;
  isTable: boolean = false;

  file:any;
  newsletterName:any;
  newsletters:any[] = [];
  newsletterObj:Newsletter = new Newsletter();

  // Preview 
  fileName: any;
  src:any

  // application properties value
  maxFileSize:any;
	maxRequestSize:any;
  fileSize: number = 0;

  // modal 
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();
  

  // Filter 
  filters:any = {};
  isSearchEnabled:boolean = false;
  documentsColumns:any[] = ['blank','displayName','fileName','createdByName','createdOn'];


  constructor(
    private authenticationService: AuthenticationService,
    private locationStrategy: LocationStrategy,
    private validationService: ValidationService,
    private modalService: BsModalService,
    private newsletterService : NewsletterService,
  ){
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
    this.showTable();
  }

  showDocumentForm() {
    this.isUploadForm = true;
    this.maxFileSize = parseInt(sessionStorage.maxFileSize);
    this.maxRequestSize = parseInt(sessionStorage.maxRequestSize);
    
    this.isTable = false;
    
    this.reset();
  }


  showTable() {
    this.isTable = true;

    this.isUploadForm = false;
    this.filters = {};
    this.isSearchEnabled = false;

    this.getAllNewsletters();
  }

  reset() {
    this.newsletterName = null;
    this.file=null;
  }

  onFileSelect(event:any){
    this.file = {};
    let totalSize: number = 0;

    this.fileSize = 0;
    const uploadedFiles = event.target.files;
    console.log("Max File Size: ", this.maxFileSize );

    let document = uploadedFiles[0];
    let fileName = document.name;
    this.fileSize = this.fileSize + document.size / 1024 /1024;

    console.log("file size : ", this.fileSize);
        
    this.file = {document : document,fileName : fileName};
  }

  onUploadFiles(template: TemplateRef<any>){
    this.newsletterName = this.newsletterName?.trim();

    if(!this.validationService.validateNullUndefinedEmptyString(this.newsletterName)){
      this.alertMessage = "Please enter Newsletter Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if(!this.validationService.validateAlphaNumericWithSpace(this.newsletterName)){
      this.alertMessage = "Please enter Valid Newsletter Name, Alphabets, Numbers & space allowed !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    console.log("this.file : ", this.file);
    
    if (!this.file){
      this.alertMessage = "Kindly Select Newsletter !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    let totalSize = parseFloat(this.fileSize.toFixed(2));
    if(totalSize>this.maxFileSize && totalSize>this.maxRequestSize){
      this.alertMessage ="File exceeds the size limit";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    const formData = new FormData();
    formData.append(`file`, this.file.document , this.file.fileName);
    formData.append("displayName", this.newsletterName);
    formData.append("uploadedBy", this.currentUser.empId);

    console.log("Upload newsletter : ", formData);
    this.newsletterService.uploadNewsletter(formData).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == 'Success') {
        this.openAlertMod(template, response.serviceResponse);
        this.reset();
        this.showTable()
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  
  }

  spaceTrimNewsletterName(){
    if(this.newsletterName != null || this.newsletterName != ''){
      this.newsletterName = this.newsletterName?.trim();
    }
  }

  getAllNewsletters(){
    this.newsletters = [];
    
    this.newsletterService.getAllNewsletters().pipe(first()).subscribe((response:any) => {
      if (response.serviceStatus == "Success") {
        this.newsletters =  response.serviceResponse;
        this.newsletters.forEach(doc => {
          doc.createdOn = (doc.createdOn)? moment(doc.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        });
        console.log("Newsletters List : ", this.newsletters);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  openDeleteDocument(template: TemplateRef<any>, newsletterObj: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.newsletterObj = newsletterObj;
    console.log("On Delete Obj : ", this.newsletterObj);
  }

  onDeleteDocument(template: TemplateRef<any>) {
    this.cancelRequest();
    this.newsletterService.deleteNewsletter(this.newsletterObj.documentId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  previewDocument(template: TemplateRef<any>,doc: any) {
    this.src = null;
    this.fileName = doc.displayName;

   this.newsletterService.downloadDocument(doc.documentId).pipe(first()).subscribe((response:any) => {
      const blob = new Blob([response], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;

      this.src =  a.href;

      if(this.src != null){
        this.openPreviewDocument(template);
      }
    });
  }

   
  downloadFile(doc: any) {
    this.newsletterService.downloadDocument(doc.documentId).subscribe(blob => saveAs(blob,doc.fileName));
  }


  // Modals

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  openPreviewDocument(template: TemplateRef<any>){
    this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  // Pagination
  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  // Sorting 
  sortData(sort: Sort){	
    console.log(sort);
    if(sort.active){
      let sortParams:any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;      
    }
  }


  // Filter 
  toggleSearch(){
    this.isSearchEnabled = !this.isSearchEnabled;
    if(!this.isSearchEnabled){
      this.filters = {};
    }
  }

  onSearch(searchData){
    this.filters = searchData;
    console.log("Updated Filter : ", this.filters);
  }
}
