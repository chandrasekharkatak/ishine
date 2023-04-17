import { LocationStrategy } from '@angular/common';
import { Component, OnInit, SecurityContext, TemplateRef } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { DomSanitizer } from '@angular/platform-browser';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { EventPhoto } from 'src/app/models/EventPhoto';
import { Feature } from 'src/app/models/feature';
import { NotificationMessage } from 'src/app/models/notification';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { ImageService } from 'src/app/services/image.service';
import { NotificationService } from 'src/app/services/notification.service';
import { ValidationService } from 'src/app/services/validation.service';
import { AngularEditorConfig } from '@kolkov/angular-editor';

@Component({
  selector: 'app-home-config',
  templateUrl: './home-config.component.html',
  styleUrls: ['./home-config.component.css']
})
export class HomeConfigComponent implements OnInit {

  feature = "Home Config";
  currentUser: User;
  userMapping: any = {};

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;

  //flags 
  isPhotoForm: boolean = false;
  isNotificationForm: boolean = false;
  isTable: boolean = false;
  isNotificationTable: boolean = false;
  isNotificationCreate: boolean = false;
  isNotificationUpdate: boolean = false;
  isConsentNotificationResponseTable: boolean = false;

  //modal 
  alertMessage: any;
  excelName: any;
  modalRef: BsModalRef = new BsModalRef();

  imageObj:EventPhoto = new EventPhoto();  
  files:any[] = [];
  eventName:any;

  eventImages:any[] = [];
  isPreviewLoaded:boolean = false;

  allNotification:any[] = [];
  consentNotificationResponse:any[] = [];
  notificationToBeDeleted:any;
  notificationResponseView: any;

  filters:any = {};
  isSearchEnabled:boolean = false;
  consentNotificationResponseColumns:any[] = ['blank', 'employeementId', 'name', 'notificationMessage', 'consentDate'];

  notificationObj: NotificationMessage = new NotificationMessage();

  //Angular Editor

  editorConfig: AngularEditorConfig = {
    editable: true,
      spellcheck: true,
      height: 'auto',
      minHeight: '100px',
      maxHeight: '300px',
      width: 'auto',
      minWidth: '100px',
      translate: 'yes',
      enableToolbar: true,
      showToolbar: true,
      defaultParagraphSeparator: '',
      defaultFontSize: '',
      fonts: [{class: 'arial', name: 'Arial'},],
    uploadWithCredentials: false,
    sanitize: true,
    toolbarPosition: 'top'
};

  constructor(
    private validationService: ValidationService,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private employeeService:EmployeeService,
    private imageService: ImageService,
    private sanitizer: DomSanitizer,
    private notificationService: NotificationService,
    private locationStrategy: LocationStrategy,
    private exportExcelService: ExportExcelService,
    ) {
      this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
     }

  ngOnInit(): void {
    // Dynamic Subfeature Flags 
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
    // if(this.userMapping.upload_event_photos){
    //   this.showUploadPhotosForm();
    // }else if(this.userMapping.view_all_event_photos || this.userMapping.delete_event_photos){
    //   this.showTable();
    // }else if(this.userMapping.set_notifications){
    //   this.showNotificationForm();
    // }

    if(this.userMapping.view_all_event_photos || this.userMapping.delete_event_photos){
      this.showTable();
    }else if(this.userMapping.view_all_notification){
      this.showNotificationTable();
    }

  }

  showUploadPhotosForm() {
    this.isPhotoForm = true;

    this.isTable = false;
    this.isNotificationForm = false;
    this.isNotificationTable = false;
    this.isConsentNotificationResponseTable = false;
    this.reset();
  }

  showNotificationForm() {
    this.isNotificationCreate = true;
    this.isNotificationForm = true;

    this.isNotificationUpdate = false;
    this.isNotificationTable = false;
    this.isConsentNotificationResponseTable = false;
    this.isPhotoForm = false;
    this.isTable = false;
    this.reset();
  }

  showNotificationTable(){
    this.isNotificationTable = true;
    this.isNotificationForm = false;
    
    this.isPhotoForm = false;
    this.isConsentNotificationResponseTable = false;
    this.isTable = false;

    this.getAllNotifications();
  }

  showUpdateNotificationForm(notificationObj:any){
    this.isNotificationUpdate = true;
    this.isNotificationForm = true;

    this.isNotificationCreate = false;
    this.isNotificationTable = false;
    this.isConsentNotificationResponseTable = false;
    this.isPhotoForm = false;
    this.isTable = false;

    this.notificationObj = Object.assign({}, notificationObj);
  }

  showTable() {
    this.isTable = true;

    this.isPhotoForm = false;
    this.isNotificationForm = false;
    this.isNotificationTable = false;
    this.isConsentNotificationResponseTable = false;
    this.getAllEventPhotos();
  }

  reset() {
    this.eventName = null;
    this.files = [];

    this.notificationObj = new NotificationMessage();
  }

  onImageSelect(event:any,template: TemplateRef<any>){
    let isSizeInRange:boolean = false;

    //Bits in  10mb : 10485760
    if(event.target.files[0].size > 10485760){
      this.openAlertMod(template, "File size is more than 10MB");
      event.target.value = null;
      isSizeInRange = false;
   }else{
    isSizeInRange = true;
   }

   if(isSizeInRange){
    this.files = [];

    const uploadedFiles = event.target.files;
    console.log("uploadedFiles : ", uploadedFiles);
    
    if (uploadedFiles.length != 0) {
      for (let i = 0; i < uploadedFiles.length; i++) {
        let image = uploadedFiles[i];
        let imageName = image.name;

        let imgObj = {image : image,imageName : imageName}
        this.files.push(imgObj);
      };
    }
    console.log("Files : ", this.files);
   }
  }

  onUploadImages(template: TemplateRef<any>){

    if(!this.validationService.validateNullUndefinedEmptyString(this.eventName)){
      this.alertMessage = "Please enter Event Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if(!this.validationService.validateAlphaNumericWithSpace(this.eventName)){
      this.alertMessage = "Please enter Valid Event Name, Alphabets, Numericals & space allowed !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (this.files.length == 0) {
      this.alertMessage = "Kindly Select Images !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    
    const formData = new FormData();
    this.files.forEach((file) =>{
      formData.append(`image`, file.image, file.imageName);
    });
    formData.append("eventName", this.eventName);
    formData.append("uploadedBy", this.currentUser.empId);
    formData.append("employeementId", this.currentUser.employeementId);
    formData.append("empId", this.currentUser.empId);

    console.log("Upload Images : ", formData);
    this.imageService.uploadMultipleImages(formData).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == 'Success') {
        this.openAlertMod(template, response.serviceResponse);
        this.reset();
        this.showTable()
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onDeleteEventPhoto(template: TemplateRef<any>) {
    let imageObj = new EventPhoto();
    this.cancelRequest();
   this.imageObj.updatedBy = this.currentUser.empId;
   console.log("Updated by .. ",this.imageObj.updatedBy)
    this.imageService.deleteEventPhoto(this.imageObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  getAllEventPhotos(){
    this.eventImages = [];
    this.imageService.getAllEventPhotos().pipe(first()).subscribe((response:any) => {
      if (response.serviceStatus == "Success") {
        this.eventImages =  response.serviceResponse;
        this.eventImages.forEach(img => {
          img.createdOn = (img.createdOn)? moment(img.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        })
        console.log("eventImages : ", this.eventImages);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  loadPreviewImage(imageObj){
    if(imageObj.imageBytes){
      let objectURL = 'data:image/*;base64,' + imageObj.imageBytes;
      let src: string = this.sanitizer.sanitize(SecurityContext.RESOURCE_URL, this.sanitizer.bypassSecurityTrustResourceUrl(objectURL));
      let previeImage = document.getElementById('photoPreview');
      previeImage.setAttribute('src', src);
      this.isPreviewLoaded = true;
      previeImage.style.display = 'block';
    }
  }

  onSetNotification(template: TemplateRef<any>) {
    if(!this.validationService.validateNullUndefinedEmptyString(this.notificationObj.notificationMessage)){
      this.alertMessage = "Please enter Notification Message !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if(this.notificationObj.notificationMessage.length > 5000){
      this.alertMessage = "Please enter Valid Notification Message, Use under 5000 characters !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(this.notificationObj.notificationType)){
      this.alertMessage = "Please select Notification Type !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    this.notificationObj.createdBy = this.currentUser.empId;
    
    this.notificationService.addNotification(this.notificationObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showNotificationTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onUpdateNotification(template: TemplateRef<any>) {
    if(!this.validationService.validateNullUndefinedEmptyString(this.notificationObj.notificationMessage)){
      this.alertMessage = "Please enter Notification Message !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if(this.notificationObj.notificationMessage.length > 5000){
      this.alertMessage = "Please enter Valid Notification Message, Use under 5000 characters !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    this.notificationObj.updatedBy = this.currentUser.empId;
    this.notificationService.updateNotification(this.notificationObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showNotificationTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onDeleteNotification(template: TemplateRef<any>){
    this.cancelRequest();

    this.notificationToBeDeleted.updatedBy = this.currentUser.empId;
    this.notificationService.onDeleteNotification(this.notificationToBeDeleted).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showNotificationTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }


  /* Notifications */
  getAllNotifications(){
    this.notificationObj = new NotificationMessage();

    this.notificationService.getAllNotifications().pipe(first()).subscribe((response:any) => {
      if (response.serviceStatus == "Success") {
        this.allNotification =  response.serviceResponse;

        this.allNotification.forEach((notification) => {
          notification.createdOn = (notification.createdOn)? moment(notification.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          notification.updatedOn = (notification.updatedOn)? moment(notification.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
        });

        console.log("notificationList : ", this.allNotification);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  getConsentNotificationResponse(notificationObj, consentNotificationTemplate: TemplateRef<any>, template: TemplateRef<any>){
    this.notificationResponseView = notificationObj;

    this.notificationService.getConsentNotificationResponse(notificationObj).pipe(first()).subscribe((response:any) => {
      if (response.serviceStatus == "Success") {
        this.isConsentNotificationResponseTable = true;
        this.consentNotificationResponse = response.serviceResponse;

        this.consentNotificationResponse.forEach((object) => {
          object.employeementId = ("A-").concat(object.employeementId);
          object.consentOn = (object.consentOn)? moment(object.consentOn).format(AppComponent.DATETIME_FORMAT) : null;
        });

        this.modalRef = this.modalService.show(consentNotificationTemplate, { class: 'modal-lg' });
        console.log("consentNotificationResponse : ", this.consentNotificationResponse);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  //Export Excel
  exportToExcel(){

    if(this.isConsentNotificationResponseTable){
      this.excelName = "consentNotificationResponse.xlsx";
      
      const temp = document.createElement('div');
      temp.innerHTML = this.notificationResponseView.notificationMessage;
      let notificationMessage = temp.textContent;

      const dataArr = [["Notification Message : ", notificationMessage, "", ""],
      ["Employment Id", "Employee Name", "Consent Date"],
      ...this.consentNotificationResponse.map(x => [x.employeementId, x.name, (x.consentOn) ? moment(x.consentOn).format(AppComponent.DATETIME_FORMAT) : null])
      ];

      this.exportExcelService.exportTableDataToExcelWithDescription(dataArr, this.excelName);
    }
  }

  //modals
  openDeleteEventPhoto(template: TemplateRef<any>, imageObj: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.imageObj = imageObj;
    console.log(this.imageObj);
  }

  openDeleteNotificationModal(notificationObj:any, template: TemplateRef<any>) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.notificationToBeDeleted = notificationObj;
    console.log(this.notificationObj);
  }

  openPreviewEventPhoto(template: TemplateRef<any>, imageObj: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
    console.log(imageObj);
    this.imageObj = imageObj;
    this.isPreviewLoaded = false;
    document.getElementById(`photoPreview`).style.display = 'none';
    setTimeout(()=>{this.loadPreviewImage(imageObj);}, 1000);
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

  sortData(sort: Sort){	
    console.log(sort);
    if(sort.active){
      let sortParams:any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;      
    }
  }	

  toggleSearch(){
    this.isSearchEnabled = !this.isSearchEnabled;
  }

  onSearch(searchData){
    this.filters = searchData;
    console.log("Updated Filter : ", this.filters);
  }
}

  function compare(a: number | string, b: number | string, isAsc: boolean) {	
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);	
  }
  

