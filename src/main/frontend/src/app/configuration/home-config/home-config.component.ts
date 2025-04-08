import { LocationStrategy } from '@angular/common';
import { Component, HostListener, OnInit, SecurityContext, TemplateRef, ViewChild } from '@angular/core';
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
import { AngularEditorComponent, AngularEditorConfig } from '@kolkov/angular-editor';
import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { UtilityService } from 'src/app/services/utility.service';

@Component({
  selector: 'app-home-config',
  templateUrl: './home-config.component.html',
  styleUrls: ['./home-config.component.css']
})
export class HomeConfigComponent implements OnInit {

  @ViewChild('editor') editor: AngularEditorComponent;
  @ViewChild('alert_message') alertTemplate: TemplateRef<any>;

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
  isPhotosOrderPage:boolean = false;
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
  eventCaption:any;
  isExternalLink:any = "false";
  externalLink:any;

  eventImages:any[] = [];
  isPreviewLoaded:boolean = false;

  allNotification:any[] = [];
  consentNotificationResponse:any[] = [];
  notificationToBeDeleted:any;
  notificationResponseView: any;

  filters:any = {};
  isSearchEnabled:boolean = false;
  notificationsColumns:any[] = ['blank', 'notificationMessage','notificationType','isActive','createdByName','createdOn','updatedByName','updatedOn'];
  eventTableColumns:any[] = ['eventName','eventCaption','createdByName','createdOn'];
  notificationObj: NotificationMessage = new NotificationMessage();

  consentFilters:any = {};
  isConsentSearchEnabled:boolean = false;
  consentNotificationResponseColumns:any[] = ['blank', 'employeementId', 'name','consentOn'];
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
      fonts: [{class: 'arial', name: 'Arial'},
      {class: 'calibri', name: 'Calibri'}],
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
    private utilityService: UtilityService,
    ) {
      this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
     }

     async ngOnInit(): Promise<void> {
    
    // Dynamic Subfeature Flags
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    //console.log(this.feature, this.userMapping);

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

  @HostListener('paste', ['$event'])
  onPaste(event: ClipboardEvent) {
    const clipboardData = event.clipboardData || (window as any).clipboardData;
    const pastedData = clipboardData.getData('text/plain');
    const isImage = clipboardData.types.includes('Files');

    if (isImage) {
      event.preventDefault();
      this.alertMessage = "You cannot paste images into the editor, please enter text !!"
      this.openAlertMod(this.alertTemplate, this.alertMessage);
      return false;
    }
  }


  showUploadPhotosForm() {
    this.isPhotoForm = true;

    this.isTable = false;
    this.isPhotosOrderPage=false;
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
    this.isPhotosOrderPage=false;
    this.isPhotoForm = false;
    this.isTable = false;
    this.reset();
  }

  showNotificationTable(){
    this.sortColumn=[];
    this.sortColumnType=[];
    this.sortDirection='';
    this.isNotificationTable = true;
    this.isNotificationForm = false;

    this.isPhotoForm = false;
    this.isPhotosOrderPage=false;
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
    this.isPhotosOrderPage=false;
    this.isPhotoForm = false;
    this.isTable = false;

    this.notificationObj = Object.assign({}, notificationObj);
  }

  showTable() {
    this.sortColumn=[];
    this.sortColumnType=[];
    this.sortDirection='';
    this.isTable = true;

    this.isPhotoForm = false;
    this.isPhotosOrderPage=false;
    this.isNotificationForm = false;
    this.isNotificationTable = false;
    this.isConsentNotificationResponseTable = false;
    this.getAllEventPhotos();
  }

  showPhotosOrderpage() {
    this.isPhotosOrderPage=true;
    
    this.isTable = false;
    this.isPhotoForm = false;
    this.isNotificationForm = false;
    this.isNotificationTable = false;
    this.isConsentNotificationResponseTable = false;
    this.getAllEventPhotosInOrder();
  }

  reset() {
    this.eventName = null;
    this.eventCaption = null;
    this.isExternalLink = "false";
    this.externalLink = null;
    this.files = [];

    this.notificationObj = new NotificationMessage();
  }

  isValidHttpUrl(string:string) {
    let url;
    
    try {
      url = new URL(string);
    } catch (_) {
      return false;  
    }
  
    return url.protocol === "http:" || url.protocol === "https:";
  }

  drop(event: CdkDragDrop<EventPhoto[]>) {
    moveItemInArray(this.eventImages, event.previousIndex, event.currentIndex);
  }

  onUpdatePhotoOrder(template: TemplateRef<any>){
    this.eventImages.forEach((image, index) => {
      image.photoOrder = index+1;
    });

    //console.log("Updated Order : ", this.eventImages);

    let updatedPhotoList = new EventPhoto();
    updatedPhotoList.eventPhotoList = this.eventImages;
    updatedPhotoList.updatedBy = this.currentUser.empId;

    this.imageService.updatePhotoOrder(updatedPhotoList).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == 'Success') {
        this.openAlertMod(template, response.serviceResponse);
        this.reset();
        this.showPhotosOrderpage();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onUpdateEventDetails(template: TemplateRef<any>){
    this.cancelRequest();

    if(this.imageObj.eventName) this.imageObj.eventName = this.imageObj.eventName.trim();
    if(this.imageObj.eventCaption) this.imageObj.eventCaption = this.imageObj.eventCaption.trim();
    if(this.imageObj.isExternalLink == "true" && this.imageObj.externalLink){
      this.imageObj.externalLink = this.imageObj.externalLink.trim();
    }

    if(!this.validationService.validateNullUndefinedEmptyString(this.imageObj.eventName)){
      this.alertMessage = "Please enter Event Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    else if(!this.validationService.validateViewsOnOrganisation(this.imageObj.eventName)){
      this.alertMessage = `Please enter Valid Event Name, Alphabets, Numbers, space & Allowed special characters are +-()'"?.,&!`
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(this.validationService.validateNullUndefinedEmptyString(this.imageObj.eventCaption)){
      if(!this.validationService.validateViewsOnOrganisation(this.imageObj.eventCaption)){
        this.alertMessage = `Please enter Valid Caption, Alphabets, Numbers, space & Allowed special characters are +-()'"?.,&!`;
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    if(!this.validationService.validateNullUndefinedEmptyString(this.imageObj.isExternalLink)){
      this.alertMessage = "Please select Add External Link !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(this.imageObj.isExternalLink == "true"){
      if(!this.validationService.validateNullUndefinedEmptyString(this.imageObj.externalLink)){
        this.alertMessage = "Please enter external Link !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }else if(!this.isValidHttpUrl(this.imageObj.externalLink)){
        this.alertMessage = "Please enter valid external Link !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    let updatedPhotoDetails = new EventPhoto();
    updatedPhotoDetails = Object.assign({}, this.imageObj);
    updatedPhotoDetails.updatedBy = this.currentUser.empId;

    //console.log("onUpdateEventDetails : ", updatedPhotoDetails);
    
    this.imageService.updatePhotoDetails(updatedPhotoDetails).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == 'Success') {
        this.openAlertMod(template, response.serviceResponse);
        this.reset();
        this.showTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }
  

  onImageSelect(event:any,template: TemplateRef<any>){
    const extensionRE = /(?:\.([^.]+))?$/;
    let isSizeInRange:boolean = false;
    const allowedTypes = ['image/png', 'image/jpeg', 'image/jpg'];
    const maxSizeInBytes = 10 * 1024 * 1024; // 10MB

    const uploadedFiles = event.target.files;
    //console.log(" file type ::  ",uploadedFiles," file type :: ",uploadedFiles.type)

    //Bits in  10mb : 10485760
// Check file type
if (uploadedFiles[0] && allowedTypes.indexOf(uploadedFiles[0].type) === -1) {
  this.openAlertMod(template,'Please select a valid image file (png, jpeg, or jpg).');
  event.target.value = ''; // Clear the input
  return;
}
    
    if(event.target.files[0].size > maxSizeInBytes){
      this.openAlertMod(template, "File size is more than 10MB");
      event.target.value = null;
      isSizeInRange = false;
   }else{
    isSizeInRange = true;
   }

   if(isSizeInRange){
    this.files = [];

   
    //console.log("uploadedFiles : ", uploadedFiles);

    if (uploadedFiles.length != 0) {
      for (let i = 0; i < uploadedFiles.length; i++) {
        //console.log(" upload method call ");
        let image = uploadedFiles[i];
        let imageName = "EventPhoto_"+moment(new Date()).format("DD-MM-YYYY-hh-mm-ss")+"."+extensionRE.exec(image.name)[1];

        let imgObj = {image : image,imageName : imageName}
        this.files.push(imgObj);
      };
    }
    //console.log("Files : ", this.files);
   }
  }

  onUploadImages(template: TemplateRef<any>){
    if(this.eventName) this.eventName = this.eventName.trim();
    if(this.eventCaption) this.eventCaption = this.eventCaption.trim();
    if(this.isExternalLink == "true" && this.externalLink){
      this.externalLink = this.externalLink.trim();
    }

    if(!this.validationService.validateNullUndefinedEmptyString(this.eventName)){
      this.alertMessage = "Please enter Event Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    else if(!this.validationService.validateViewsOnOrganisation(this.eventName)){
      this.alertMessage = `Please enter Valid Event Name, Alphabets, Numbers, space & Allowed special characters are +-()'"?.,&!`
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(this.validationService.validateNullUndefinedEmptyString(this.eventCaption)){
      if(!this.validationService.validateViewsOnOrganisation(this.eventCaption)){
        this.alertMessage = `Please enter Valid Caption, Alphabets, Numbers, space & Allowed special characters are +-()'"?.,&!`;
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    if(!this.validationService.validateNullUndefinedEmptyString(this.isExternalLink)){
      this.alertMessage = "Please select Add External Link !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(this.isExternalLink == "true"){
      if(!this.validationService.validateNullUndefinedEmptyString(this.externalLink)){
        this.alertMessage = "Please enter external Link !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }else if(!this.isValidHttpUrl(this.externalLink)){
        this.alertMessage = "Please enter valid external Link !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    if (this.files.length == 0) {
      this.alertMessage = "Kindly Select Image !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    const formData = new FormData();
    this.files.forEach((file) =>{
      formData.append(`image`, file.image, file.imageName);
    });
    formData.append("eventName", this.eventName);
    formData.append("eventCaption", (this.eventCaption == null) ? "" : this.eventCaption);
    formData.append("isExternalLink", this.isExternalLink);
    formData.append("externalLink", (this.externalLink == null) ? "" : this.externalLink);
    formData.append("uploadedBy", this.currentUser.empId);
    formData.append("employeementId", this.currentUser.employeementId);
    formData.append("empId", this.currentUser.empId);

    //console.log("Upload Images : ", formData);
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
   //console.log("Updated by .. ",this.imageObj.updatedBy)
    this.imageService.deleteEventPhoto(this.imageObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  getAllEventPhotosInOrder(){
    this.eventImages = [];
    this.imageService.getAllEventPhotos().pipe(first()).subscribe((response:any) => {
      if (response.serviceStatus == "Success") {
        this.eventImages =  response.serviceResponse;
        this.eventImages.forEach(img => {
          img.createdOn = (img.createdOn)? moment(img.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        });
        this.eventImages.sort((a,b) => a.photoOrder - b.photoOrder);
        // console.log("eventImages : ", this.eventImages);
      } else {
        console.error(response.serviceResponse);
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
          img.emp360CreatedBy = img.createdBy;
         
        })
       
        // console.log("eventImages : ", this.eventImages);
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

  notificationId: number=0;

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
        if (this.notificationSelect == true && this.file.fileName != null) {
          this.notificationId = +response.serviceMessage;
          this.notificationService.addReleaseNotesVideo(this.file, this.notificationId).subscribe(
            (response: any) => {
              if (response.serviceStatus === 'Success') {
                console.log('File saved successfully:', response.serviceResponse);
                alert(response.serviceMessage);
              } else {
                console.error('Failed to save file:', response.serviceError);
                alert(response.serviceMessage);
              }
            },
            (error) => {
              console.error('Error during API call:', error);
              alert('An error occurred while saving the file.');
            }
          );

        }
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
      this.file={};
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

  onInactivateNotification(template: TemplateRef<any>){
    this.cancelRequest();

    this.notificationToBeDeleted.updatedBy = this.currentUser.empId;
    this.notificationService.onInActivateNotification(this.notificationToBeDeleted).pipe(first()).subscribe((response: any) => {
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
          notification.emp360CreatedBy = notification.createdBy;
          notification.emp360UpdatedBy = notification.updatedBy;
        
        });
        
        //console.log("notificationList : ", this.allNotification);
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
        //console.log("consentNotificationResponse : ", this.consentNotificationResponse);
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
    //console.log(this.imageObj);
  }

  openDeleteNotificationModal(notificationObj:any, template: TemplateRef<any>) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.notificationToBeDeleted = notificationObj;
    //console.log(this.notificationObj);
  }

  openInactivateNotificationModal(notificationObj:any, template: TemplateRef<any>) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.notificationToBeDeleted = notificationObj;
    //console.log(this.notificationObj);
  }

  openPreviewEventPhoto(template: TemplateRef<any>, imageObj: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
    //console.log(imageObj);
    this.imageObj = imageObj;
    this.isPreviewLoaded = false;
    document.getElementById(`photoPreview`).style.display = 'none';
    setTimeout(()=>{this.loadPreviewImage(imageObj);}, 1000);
  }

  openUpdateEventPhotoDetails(template: TemplateRef<any>, imageObj: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
    //console.log(imageObj);
    this.imageObj = imageObj;
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
    //console.log(sort);
    if(sort.active){
      let sortParams:any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

  toggleSearch(){
    this.isSearchEnabled = !this.isSearchEnabled;
    if(!this.isSearchEnabled){
      this.filters = {};
    }
  }

  resetSearch(){
    this.isSearchEnabled = false;
    this.filters = {};
  }

  onSearch(searchData){
    this.filters = searchData;
    //console.log("Updated Filter : ", this.filters);
  }

  toggleConsentResponseSearch(){
    this.isConsentSearchEnabled = !this.isConsentSearchEnabled;
    if(!this.isConsentSearchEnabled){
      this.consentFilters = {};
    }
  }

  onConsentSearch(searchData){
    this.consentFilters = searchData;
  }


// Video Upload st
notificationSelect: boolean = false;
onNotificationTypeChange() {
  if (this.notificationObj.notificationType === 'releaseNotes') {
    this.notificationSelect = true;
  } else {
    this.notificationSelect = false;
  }
}

file: any = {};
fileSize: number = 0;

onFileSelect(event: any, template: TemplateRef<any>) {
  this.file = {};
  const maxSizeInBytes = 100 * 1024 * 1024; // 100MB
  const uploadedFile = event.target.files[0];

  if (!uploadedFile) {
    return;
  }

  if (!['video/mp4', 'video/webm'].includes(uploadedFile.type)) {
    this.openAlertMod(template, 'Please select a valid file (.mp4 or .webm).');
    event.target.value = ''; 
    return;
  }

  if (uploadedFile.size > maxSizeInBytes) {
    this.openAlertMod(template, 'File size is more than 100MB');
    event.target.value = null;
    return;
  }

  this.file = { document: uploadedFile, fileName: uploadedFile.name };
  this.fileSize = uploadedFile.size / 1024 / 1024;
}


// Call any other function or perform actions based on the selected value

// Video Upload end


}  function compare(a: number | string, b: number | string, isAsc: boolean) {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  }


