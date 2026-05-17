import { LocationStrategy } from '@angular/common';
import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { saveAs } from "file-saver";
import * as moment from 'moment';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Document } from 'src/app/models/document';
import { Feature } from 'src/app/models/feature';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { NewsletterService } from 'src/app/services/newsletter.service';
import { UtilityService } from 'src/app/services/utility.service';
import { ValidationService } from 'src/app/services/validation.service';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  standalone: false,
  selector: 'app-document',
  templateUrl: './document.component.html',
  styleUrls: ['./document.component.css']
})
export class DocumentComponent implements OnInit {

  document : boolean = false;
  upload : boolean = false;
  isForm : boolean = false;
  type : boolean = false;
  isCreation : boolean = false;
  isUpdation : boolean = false;
  isDocCheck : boolean = false;
  isSearchEnableddocument:boolean = false;
  currentUser:User;
  documentObj= new Document();

  @ViewChild('alert_message') alertTemplate: TemplateRef<any>;
  modalRef:NgbModalRef;
  alertMessage: any;
  allTypeList : any = [];
  fileSize: number = 0;
  maxFileSize:any;
  maxRequestSize:any;
  file:any;
  documentName : any;
  documents :any = [];

  isSearchEnabled:boolean= false;
   // Sorting
   sortDirection = 'asc';
   sortColumn: any;
   sortColumnType:any;
  src: any;
  userMapping: any = {};
  feature:any = "Newsletter Config";
    // Filter
    filters:any = {};
    typeNames:any;
    allTypeListColumns:any[]=['blank','typeName','createdOn','name','blank','blank','blank'];
    documentsColumns:any[]=['blank','displayName','fileName','typeName','createdOn','createdByName'];
  employeesFor360: any[] = [];

  constructor(
    private newsletterService : NewsletterService,
    private modalService: NgbModal,
    private authenticationService : AuthenticationService,
    private validationService : ValidationService,
    private locationStrategy : LocationStrategy,
    private utilityService: UtilityService,
    private router : Router
  ) { }

  async ngOnInit(): Promise<void> {
    try {
      // this.employeesFor360 = await this.utilityService.getEmployeeDetailsFor360View();

    } catch (error) {
      console.error("Error fetching employee details for 360 view", error);
    }
    this.getAllTypes();
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    this.preventBackButton();
  }

  preventBackButton() {
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(() => {
      history.pushState(null, null, location.href);
    })
  }

  getAllCustomType(){
    this.document=true;
    this.getAllTypeName();
    this.upload=false;
    this.getAllDocuments();
    this.isForm=false;
    this.type=false;
  }

  showUploadForm(){
    this.upload=true;
    this.document=false;
    this.isForm=false;
    this.type=false;
    this.maxFileSize = parseInt(sessionStorage.maxFileSize);
    this.maxRequestSize = parseInt(sessionStorage.maxRequestSize);
    this.documentObj.typeId = '';
    this.documentObj.readEnabled = '';
    this.documentName = '';
  }
  addType(){
    this.isForm=true;
    this.isCreation=true;
    this.isUpdation=false;
    this.upload=false;
    this.document=false;
    this.type=false;
    this.documentObj.typeName = '';

  }


    RestrictFullName(event) {
      var k;
      k = event.charCode;
      if ((k == 33) || (k == 34) || (k == 35) || (k == 36) || (k == 37) ||
        (k == 38) || (k == 39) || (k == 40) || (k == 41) || (k == 42) ||
        (k == 43) || (k == 44) || (k == 45) || (k == 46) || (k == 47) || (k == 48) ||
        (k == 49) || (k == 50) || (k == 51) || (k == 52) || (k == 53) || (k == 54) || (k == 55) ||
        (k == 56) || (k == 57) || (k == 58) ||
        (k == 59) || (k == 60) || (k == 61) || (k == 62) || (k == 63) ||
        (k == 64) || (k == 91) || (k == 92) || (k == 93) || (k == 94) ||
        (k == 95) || (k == 96) || (k == 123) ||
        (k == 124) || (k == 125) || (k == 126) || (k == 127)) {
        return (false);
      }
      return (true);


    }

  showTable(){
    this.document = true;
    this.upload = false;
    this.isForm= false;
    this.type=false;
    this.filters = {};
    this.getAllDocuments();
  }

  showTypeTable(){
    this.document = false;
    this.upload = false;
    this.isForm= false;
    this.type=true;
    this.filters = {};
    this.getAllTypeName();
  }

    // Sorting
    sortData(sort: Sort){
      //console.log(sort);
      if(sort.active){
        let sortParams:any[] = sort.active?.split("|");
        this.sortColumn = sortParams[0];
        this.sortColumnType = sortParams[1];
        this.sortDirection = sort.direction;
      }
    }

  addTypeMethod(document : any,template:TemplateRef<any>){
    //console.log(" add function call");
    let doc = new Document();
    doc.createdBy = this.currentUser.empId;
    doc.typeName = document.typeName;

    if(!this.validationService.validateNullUndefinedEmptyString(document.typeName)){
      this.alertMessage = "Please Enter Type !!";
      this.openAlertMod(template,this.alertMessage);
      return false;
    }



    //console.log(doc);
    this.newsletterService.addTypeDocument(doc).pipe(first()).subscribe((response : any)=>{
      if(response.serviceStatus == "Success"){
        this.openAlertMod(template,response.serviceResponse);
        this.getAllTypes();
      }else{
        this.openAlertMod(template,response.serviceResponse);
      }
    })
  }

  toggleSearchDocument() {
    this.isSearchEnableddocument = !this.isSearchEnableddocument;
    if (!this.isSearchEnableddocument) {
      this.filters = {};
    }
  }
  toggleSearch() {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
    }
  }
  onSearch(searchData) {
    this.filters = searchData;
    //console.log("Updated Filter : ", this.filters);
  }
  updateType(document : any,template:TemplateRef<any>){

    let doc = new Document();
    doc.typeId = this.documentObj.typeId;
    doc.typeName = document.typeName;
    doc.updatedBy = this.currentUser.empId;
    //console.log("Update method call   ",doc);
    this.newsletterService.updateType(doc).pipe(first()).subscribe((response : any)=>{
      if(response.serviceStatus == "Success"){
        this.openAlertMod(template,response.serviceResponse);
        this.showTypeTable();
      }else{
        this.openAlertMod(template,response.serviceResponse);
      }
    })

  }
  checkTypeName(typeName,template:TemplateRef<any>){
    //console.log("hii ",typeName);

    this.newsletterService.checkTypeName(typeName).pipe(first()).subscribe((response :any)=>{
      if(response.serviceStatus == "Fail"){
        this.openAlertMod(template,response.serviceResponse);
        this.documentObj.typeName = "";
      }
    })
  }

  getAllTypeName(){
    this.allTypeList = [];
    this.newsletterService.getAllTypeName().pipe(first()).subscribe((response : any)=>{
      if(response.serviceStatus == "Success"){
        this.allTypeList = response.serviceResponse;
        this.allTypeList.forEach(type =>{
          type.createdOn = moment(type.createdOn).format(AppComponent.DATE_FORMAT);
        })
        this.allTypeList.forEach((employee) => {
          console.log("employee.createdBy ", employee.createdBy);
          let matchingEmployee3 = this.employeesFor360.find(emp => emp.empId === employee.createdBy);
          console.log("createdby ", matchingEmployee3);
          employee.emp360CreatedBy = matchingEmployee3 ? matchingEmployee3 : {};
          console.log("employee.updatedBy ", employee.updatedBy);
          let matchingEmployee4 = this.employeesFor360.find(emp => emp.empId === employee.updatedBy);
          console.log("updatedBy ", matchingEmployee4);
          employee.emp360UpdatedBy = matchingEmployee4 ? matchingEmployee4 : {};
        });
        console.log("this.allTypeList   ::   ",this.allTypeList);
      }
    })
  }

  getAllTypes(){
    this.type=true;
    this.document=false;
    this.isForm=false;
    this.upload=false;
    this.getAllTypeName();
  }


  onFileSelect(event:any, template:TemplateRef<any>){
    this.file = {};
    let totalSize: number = 0;
    const allowedTypes = ['application/pdf', 'application/doc'];
    this.fileSize = 0;
    const maxSizeInBytes = 20 * 1024 * 1024; // 20MB
    const uploadedFiles = event.target.files;
    //console.log("Max File Size: ", this.maxFileSize );

    if (uploadedFiles[0] && allowedTypes.indexOf(uploadedFiles[0].type) === -1) {
      this.openAlertMod(template,'Please select a valid file (.pdf or .doc).');
      event.target.value = ''; // Clear the input
      return;
    }

    if(event.target.files[0].size > maxSizeInBytes){
      this.openAlertMod(template, "File size is more than 20MB");
      event.target.value = null;
   }

    let document = uploadedFiles[0];
    let fileName = document.name;
    this.fileSize = this.fileSize + document.size / 1024 /1024;

    //console.log("file size : ", this.fileSize);

    this.file = {document : document,fileName : fileName};
  }


  onUploadFiles(template: TemplateRef<any>){
    this.documentName = this.documentName?.trim();
    if(!this.validationService.validateNullUndefinedEmptyString(this.documentName)){
      this.openAlertMod(template, "Please enter Document Name !!");
      return false;
    }else if(!this.validationService.validateAlphaNumericWithSpace(this.documentName)){
      this.openAlertMod(template, "Please enter Valid Document Name, Alphabets, Numbers & space allowed !!");
      return false;
    }
    if(!this.validationService.validateNullUndefinedEmptyString(this.documentObj.typeId)){
      this.openAlertMod(template, "Please select type !!");
      return false;
    }
    if(!this.validationService.validateNullUndefinedEmptyString(this.documentObj.readEnabled)){
      this.openAlertMod(template, "Please select publish mode !!");
      return false;
    }

    //console.log(" get file ",this.file);
    if(!this.file){
      this.openAlertMod(template, "Kindly select Document !! ");
      return false;
    }
    let totalSize = parseFloat(this.fileSize.toFixed(2));
    if(totalSize > this.maxFileSize && totalSize > this.maxRequestSize){
      this.openAlertMod(template, "File exceeds the size limit ");
      return false;
    }

    const formData = new FormData();
    formData.append(`file`,this.file.document, this.file.fileName);
    formData.append("displayName",this.documentName);
    formData.append("uploadedBy",this.currentUser.empId);
    formData.append("typeId",this.documentObj.typeId);
    formData.append("readEnabled",this.documentObj.readEnabled);

    //console.log("FormData ",formData);
    this.newsletterService.uploadDocument(formData).pipe(first()).subscribe((response : any)=>{
      if(response.serviceStatus == "Success"){
        this.openAlertMod(template, response.serviceResponse);
        this.getAllCustomType()
      }else{
        this.openAlertMod(template, response.serviceResponse);
      }
    })


  }


  getAllDocuments(){
    this.documents = [];

    this.newsletterService.getAllNewsletters().pipe(first()).subscribe((response:any) => {
      if (response.serviceStatus == "Success") {
        this.documents =  response.serviceResponse;
        this.documents.forEach(doc => {
          doc.createdOn = (doc.createdOn)? moment(doc.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        });
        this.documents.forEach((employee) => {
          // console.log("employee.createdBy ", employee.createdBy);
          let matchingEmployee3 = this.employeesFor360.find(emp => emp.empId === employee.createdBy);
          // console.log("createdby ", matchingEmployee3);
          employee.emp360CreatedBy = matchingEmployee3 ? matchingEmployee3 : {};
          // console.log("employee.updatedBy ", employee.updatedBy);
          let matchingEmployee4 = this.employeesFor360.find(emp => emp.empId === employee.updatedBy);
          // console.log("updatedBy ", matchingEmployee4);
          employee.emp360UpdatedBy = matchingEmployee4 ? matchingEmployee4 : {};
        });
        //console.log("this.documents List : ", this.documents);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }


  previewDocument(template: TemplateRef<any>,doc: any) {
    this.src = null;
    this.documentName = doc.displayName;

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


  // openPreviewDocument(template: TemplateRef<any>){
  //   this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
  // }
 
openPreviewDocument(template: TemplateRef<any>) {
  this.modalRef = this.modalService.open(template, {
    size: 'lg'  
  });
}

  spaceTrimDocumentName(){
    if(this.documentName != null || this.documentName != ''){
      this.documentName = this.documentName?.trim();
    }
  }

  showUpdateForm(type){
    this.isForm=true;
    this.isCreation=false;
    this.isUpdation=true;
    this.document=false;
    this.type = false;
    this.upload=false;

    this.getTypeById(type)

  }
  getTypeById(type){
    //console.log(" getTypeBy Id method call ",type);
   let doc = new Document();
   doc.typeId = type.typeId;
    this.newsletterService.getTypeById(doc).pipe(first()).subscribe((response : any)=>{
      if(response.serviceStatus == "Success"){
        this.documentObj = response.serviceResponse;
        //console.log("this.documentObj.typeName   ",this.documentObj.typeName);
      }
    })
  }
  cancelRequest() {
    this.modalRef?.close();
  }


  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  openDeleteModal(template:TemplateRef<any> , documentObj:any){
    this.modalRef = this.modalService.open(template , { modalDialogClass : 'modal-sm'});
    this.documentObj = documentObj;
    this.typeNames = documentObj.typeName;
    //console.log("documentObj   on delete call  ",documentObj);

  }

  onDeleteType(template:TemplateRef<any>){
let doc = new Document();

doc.typeId = this.documentObj.typeId;
doc.typeName = this.documentObj.typeName;
    this.newsletterService.deleteType(doc).pipe(first()).subscribe((response : any)=>{
      if(response.serviceStatus == "Success"){
        this.openAlertMod(template, response.serviceResponse);
        this.getAllTypeName();
      }else{
        this.openAlertMod(template, response.serviceResponse);
      }
    })
    //console.log("yes delete !!   ",doc.typeId);
  }

  page = 1;
  handlePageChange(event) {
    this.page = event;
  }


  onDeleteDocument(template:TemplateRef<any>){
    this.cancelRequest();
    this.newsletterService.deleteNewsletter(this.documentObj.documentId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  deleteDoc(template:TemplateRef<any>,document){
    //console.log("deleteDoc    ",document)
    this.modalRef = this.modalService.open(template , { modalDialogClass : 'modal-sm'});
    this.documentObj = document;
    this.documentObj.displayName = document.displayName;
  }

// check document name duplicacy

checkDocumentName(documentName,template:TemplateRef<any>){

  this.documentName = documentName.trim();
  //console.log("this.documentName   ",this.documentName)
  //console.log(" Hii, document check method call",documentName);

  this.newsletterService.checkDocumentName(this.documentName).pipe(first()).subscribe((response:any)=>{
    if(response.serviceStatus == "Fail"){
      this.documentName = '';
      this.openAlertMod(template,response.serviceResponse);
      this.isDocCheck = true;
    }else{
      this.isDocCheck = true;
    }
  })






}


}
