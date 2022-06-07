import { DatePipe } from '@angular/common';
import { Component, OnInit, SecurityContext, TemplateRef } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { Employee } from '../models/employee';
import { Feature } from '../models/feature';
import { AuthenticationService } from '../services/authentication.service';
import { EmployeeService } from '../services/employee.service';
import { ValidationService } from '../services/validation.service';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css']
})
export class UserProfileComponent implements OnInit {

  //flags 
  isUpdateProfile:boolean = false;

  currentUser:any;
  currentEmployeeInfo:Employee = new Employee();
  UpdateEmployeeInfo:Employee = new Employee();

  profileImage:File;
  previewImage:any;
  profileImageName:any;

  feature="Profile";
  userMapping:any = {};

   //modal 
   alertMessage:any;
   modalRef: BsModalRef = new BsModalRef();

  constructor(
    private employeeService:EmployeeService,
    private validationService:ValidationService,
    private authenticationService: AuthenticationService, 
    private datePipe: DatePipe,
    private modalService: BsModalService,
    private sanitizer: DomSanitizer,) {
      this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    }

  ngOnInit(): void {
    this.onGetEmployeeInfo();

    // Dynamic Subfeature Flags 
    let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    update_profile: true
upload_profile_image: true
view_profile: true
    console.log(this.feature, this.userMapping);    
  }

  ngAfterViewInit() {
    this.setCalenderMaxDate();
  }

  setCalenderMaxDate(){
    const today = this.datePipe.transform(new Date(), 'yyyy-MM-dd');
    let DOB = document.getElementById('DOB');
    DOB?.setAttribute('max', today);
  }

  showUpdateProfile(){
    this.isUpdateProfile = true;

    this.UpdateEmployeeInfo = Object.assign(this.currentEmployeeInfo,{});
  }

  showViewProfile(){
    this.isUpdateProfile = false;

    this.onGetEmployeeInfo();
  }

  validateEmployeeObj(employeeObj:Employee, template: TemplateRef<any>){

    // Non-Mandatory fields : views , about me , passportNumber, landline, emergencyContactPerson, relation, emergencyContactMobile, Educational & Banking info
    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.name)){
      this.alertMessage = "Please enter Full Name !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    
    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.email)){
      this.alertMessage = "Please enter email id !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if(!this.validationService.validateEmail(employeeObj.email)){
      this.alertMessage = "Please enter valid email id !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.gender)){
      this.alertMessage = "Please select gender !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.dateOfBirth)){
      this.alertMessage = "Please enter date of birth !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.bloodGroup)){
      this.alertMessage = "Please enter blood group !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.maritalStatus)){
      this.alertMessage = "Please enter select martial status !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.fatherName)){
      this.alertMessage = "Please enter father name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.placeOfBirth)){
      this.alertMessage = "Please enter place of birth !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.motherTongue)){
      this.alertMessage = "Please enter mother tongue !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.aadhar)){
      this.alertMessage = "Please enter aadhar card number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.panNumber)){
      this.alertMessage = "Please enter PAN card number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.mobileNo)){
      this.alertMessage = "Please enter mobile number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if(!this.validationService.validateMobileNumber(employeeObj.mobileNo)){
      this.alertMessage = "Please enter valid mobile number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } 

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.address)){
      this.alertMessage = "Please enter address !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.state)){
      this.alertMessage = "Please enter state !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.city)){
      this.alertMessage = "Please enter city !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.country)){
      this.alertMessage = "Please enter country !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.pincode)){
      this.alertMessage = "Please enter pincode !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.officialMobileNo)){
      this.alertMessage = "Please enter official mobile number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if(!this.validationService.validateMobileNumber(employeeObj.officialMobileNo)){
      this.alertMessage = "Please enter valid official mobile number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } 

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.permanentAddress)){
      this.alertMessage = "Please enter permanent address !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    return true;
  }

  onGetEmployeeInfo(){
    this.currentEmployeeInfo = new Employee();
    let currentEmp = new Employee();
    currentEmp.empId = this.currentUser.empId;
    console.log("currentEmp : ", currentEmp);
    
    this.employeeService.getEmployeeByEmpId(currentEmp).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.currentEmployeeInfo = response.serviceResponse;
        console.log("currentEmployeeInfo : ", this.currentEmployeeInfo);
        this.loadProfileImage(this.currentEmployeeInfo.imageBytes)
        
      } else {
        alert(response.serviceResponse);
      }
    });
  }

  loadProfileImage(imageByte:any){
    let imageElement = document.getElementById('user-avatar');
    if(imageByte){
      let objectURL = 'data:image/*;base64,' + imageByte;
        let src: string = this.sanitizer.sanitize(SecurityContext.RESOURCE_URL, this.sanitizer.bypassSecurityTrustResourceUrl(objectURL));
        imageElement.setAttribute("src", src);
    }else{
      imageElement.setAttribute("src", "../../assets/Images/default-user-image.jpeg");
    }
  }

  onUpdateEmployeeInfo(template: TemplateRef<any>){
    // transform date formats to dd-MM-yyyy
    this.UpdateEmployeeInfo.dateOfBirth = this.datePipe.transform(this.UpdateEmployeeInfo.dateOfBirth, 'dd-MM-yyyy');
    this.UpdateEmployeeInfo.dateOfJoining = this.datePipe.transform(this.UpdateEmployeeInfo.dateOfJoining, 'dd-MM-yyyy')

    let inputValidated:boolean  = this.validateEmployeeObj(this.UpdateEmployeeInfo, template)
    if(!inputValidated) return;
    
    this.UpdateEmployeeInfo.updatedBy = 1;
    console.log("Update Profile : ", this.UpdateEmployeeInfo);
    this.employeeService.updateEmployee(this.UpdateEmployeeInfo).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showViewProfile();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }


  onProfileImageSelect(event:any){
    let uploadLabel = document.getElementById("profileImgLabel");
    let label = `Upload Image <i class="fa-solid fa-angles-right"></i>`;

    this.profileImage = null;
    this.profileImageName = null;

    if (event.target.files[0] != null) {
      this.profileImage = event.target.files[0];
      this.profileImageName = this.profileImage.name;
      console.log(this.profileImage);

      // uploadLabel.innerHTML = label;
      this.showPreviewImage(this.profileImage);
    }
  }

  showPreviewImage(image: File) {
    this.previewImage = null;
    let byteFile;

    const formData = new FormData();
    formData.append("image", image);

    this.employeeService.previewImage(formData).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == 'Success') {
        byteFile = response.serviceResponse;
        this.loadProfileImage(byteFile);
      } else {
        alert(response.serviceResponse);
      }
    });
  }

  onUploadImage(template: TemplateRef<any>){
    if (this.profileImage == null) {
      this.openAlertMod(template, "Kindly Select Image.");
      return;
    }

    const formData = new FormData();
    formData.append("image", this.profileImage);
    formData.append("uploadedBy", this.currentUser.empId);

    this.employeeService.uploadImage(formData).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == 'Success') {
        this.openAlertMod(template, response.serviceResponse);
        this.showViewProfile();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }



  // Modal
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }
}
