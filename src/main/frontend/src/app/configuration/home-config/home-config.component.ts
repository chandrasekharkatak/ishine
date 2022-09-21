import { Component, OnInit, SecurityContext, TemplateRef } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { EventPhoto } from 'src/app/models/EventPhoto';
import { Feature } from 'src/app/models/feature';
import { NotificationMessage } from 'src/app/models/notification';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { ImageService } from 'src/app/services/image.service';
import { NotificationService } from 'src/app/services/notification.service';
import { ValidationService } from 'src/app/services/validation.service';

@Component({
  selector: 'app-home-config',
  templateUrl: './home-config.component.html',
  styleUrls: ['./home-config.component.css']
})
export class HomeConfigComponent implements OnInit {

  feature = "Home Config";
  currentUser: User;
  userMapping: any = {};

  //flags 
  isPhotoForm: boolean = false;
  isNotificationForm: boolean = false;
  isTable: boolean = false;

  //modal 
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();

  imageObj:EventPhoto = new EventPhoto();  
  files:any[] = [];
  eventName:any;

  eventImages:any[] = [];
  isPreviewLoaded:boolean = false;

  notificationObj: NotificationMessage = new NotificationMessage();

  constructor(
    private validationService: ValidationService,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private employeeService:EmployeeService,
    private imageService: ImageService,
    private sanitizer: DomSanitizer,
    private notificationService: NotificationService,
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
  }

  sectionViewInit() {
    if(this.userMapping.upload_event_photos){
      this.showUploadPhotosForm();
    }else if(this.userMapping.view_all_event_photos || this.userMapping.delete_event_photos){
      this.showTable();
    }else if(this.userMapping.set_notifications){
      this.showNotificationForm();
    }

  }

  showUploadPhotosForm() {
    this.isPhotoForm = true;

    this.isTable = false;
    this.isNotificationForm = false;
    this.reset();
  }

  showNotificationForm() {
    this.isNotificationForm = true;
    
    this.isPhotoForm = false;
    this.isTable = false;
    this.reset();

    this.getAllNotifications();
  }

  showTable() {
    this.isTable = true;

    this.isPhotoForm = false;
    this.isNotificationForm = false;
    this.getAllEventPhotos();
  }

  reset() {
    this.eventName = null;
    this.files = [];

    this.notificationObj = new NotificationMessage();
  }

  onImageSelect(event:any){
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

    console.log("Upload Images : ", formData);
    this.imageService.uploadMultipleImages(formData).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == 'Success') {
        this.openAlertMod(template, response.serviceResponse);
        this.reset();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onDeleteEventPhoto(template: TemplateRef<any>) {
    this.cancelRequest();
   
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
    }else if(this.notificationObj.notificationMessage.length > 5000){
      this.alertMessage = "Please enter Valid Notification Message, Use under 5000 characters !!"
      this.openAlertMod(template, this.alertMessage);
    }

    this.notificationObj.updatedBy = this.currentUser.empId;
    this.notificationService.updateNotification(this.notificationObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.reset();
        setTimeout(() => {this.getAllNotifications()}, 2000);
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
        let notificationList:any[] =  response.serviceResponse;
        console.log("notificationList : ", notificationList);
        if(notificationList) this.notificationObj = notificationList[0]; 
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  //modals
  openDeleteEventPhoto(template: TemplateRef<any>, imageObj: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.imageObj = imageObj;
    console.log(this.imageObj);
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

}
