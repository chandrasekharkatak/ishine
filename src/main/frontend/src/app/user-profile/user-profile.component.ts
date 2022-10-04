import { DatePipe } from '@angular/common';
import { Component, OnInit, SecurityContext, TemplateRef, ViewChild } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { certification } from '../models/certification';
import { Employee } from '../models/employee';
import { Feature } from '../models/feature';
import { PreviousEmployer } from '../models/previousEmployer';
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

  allCertificationList:any[] = [];
  allPreviousEmployment:any[] = [];
  updatedCertificationList:any[] = [];
  updatedPreviousEmployment:any[] = [];
  yearOfPassingList:any[] = [];

  @ViewChild('updateInfo')
  private updateInfoTempRef:TemplateRef<any>;

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
    console.log(this.feature, this.userMapping);
    this.setYearOfPassingList();    
  }

  ngAfterViewInit() {
    this.setCalenderMaxDate();
  }

  currentDateFilter = (d: Date)=>{
    const dateFormat = 'YYYY-MM-DD';
    const currentDate = new Date();
    
    return (moment(d).format(dateFormat) <= moment(currentDate).format(dateFormat));
  }

  setYearOfPassingList(){
    for (let start = 1990; start < 2051; start++) {
      this.yearOfPassingList.push(start);
    }   
  }

  stringToNumber(year:any){
    this.UpdateEmployeeInfo.yearOfPassing = Number.parseInt(year);
  }

  setCalenderMaxDate(){
    const today = this.datePipe.transform(new Date(), 'yyyy-MM-dd');
    let DOB = document.getElementById('DOB');
    DOB?.setAttribute('max', today);
  }

  showUpdateProfile(){
    this.isUpdateProfile = true;

    this.openUpdateInfo(this.updateInfoTempRef);
    
    // this.UpdateEmployeeInfo = Object.assign(this.currentEmployeeInfo,{});
    // this.addInputCertificationField();
    // if(this.UpdateEmployeeInfo.certifications){
    //   this.allCertificationList = this.UpdateEmployeeInfo.certifications;
    // }
    // // else{
    // //   this.addInputCertificationField();
    // // }

    // this.addInputPreviousEmployerField();
    // if(this.UpdateEmployeeInfo.previousEmploymentList){
    //   this.allPreviousEmployment = this.UpdateEmployeeInfo.previousEmploymentList;
    // }
    // // else{
    // //   this.addInputPreviousEmployerField();
    // // }
  }

  showViewProfile(){
    this.isUpdateProfile = false;

    this.onGetEmployeeInfo();
  }

  // Manage employer
  addInputPreviousEmployerField() {
    let newPrevEmployerObj = new PreviousEmployer();
    this.allPreviousEmployment.push(newPrevEmployerObj);
  }

  removeInputPreviousEmployerField(prevEmployerObj) {
    this.allPreviousEmployment.forEach((value, index) => {
      if (value == prevEmployerObj){
        this.updatedPreviousEmployment.push(value);
        this.allPreviousEmployment.splice(index, 1);
      } 
    });
  }

  // Manage Certifications
  addInputCertificationField() {
    let newCertificationObj = new certification();
    newCertificationObj.certificationId = "";
    newCertificationObj.duration = "";
    newCertificationObj.modeOfCourse = "";

    this.allCertificationList.push(newCertificationObj);
  }

  removeInputCertificationField(certificationObj) {
    this.allCertificationList.forEach((value, index) => {
      if (value == certificationObj){
        this.updatedCertificationList.push(value);
        this.allCertificationList.splice(index, 1);
      } 
    });
  }

  validateEmployeeObj(employeeObj:Employee, template: TemplateRef<any>){

    
    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.viewsOnOrganisation)){
      this.alertMessage = "Please enter your view on organisation !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateDiscriptionUserProfile(employeeObj.viewsOnOrganisation)){
      this.alertMessage = "Only string character will be valid in  your view on organisation !!";
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

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.aboutMe)){
      this.alertMessage = "Please enter About me !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateAboutMeUserProfile(employeeObj.aboutMe)){
      this.alertMessage = "Only string character will be valid in About me  !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.bloodGroup)){
      this.alertMessage = "Please enter Blood group";
      this.openAlertMod(template, this.alertMessage);
      return false;
     } else if (!this.validationService.validateBloodGroup(employeeObj.bloodGroup)) {
      this.alertMessage = "Please enter Valid Blood Group !!"	
      this.openAlertMod(template, this.alertMessage);	
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.fatherName)){
      this.alertMessage = "Please enter father name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if(!this.validationService.validateAlphaWithSpace(employeeObj.fatherName)){
      this.alertMessage = "Please enter Valid father Name !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.placeOfBirth)){
      this.alertMessage = "Please enter place of birth !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if(!this.validationService.validateAlphaWithSpace(employeeObj.placeOfBirth)){
      this.alertMessage = "Please enter Valid Place of birth !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(this.validationService.validateNullUndefinedEmptyString(employeeObj.motherTongue) && !this.validationService.validateAlphaWithSpace(employeeObj.motherTongue)){
      this.alertMessage = "Please enter Valid Mother tongue number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.maritalStatus)){
      this.alertMessage = "Please enter select martial status !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(this.validationService.validateNullUndefinedEmptyString(employeeObj.passportNumber) && !this.validationService.validatePassportNumber(employeeObj.passportNumber)){
      this.alertMessage = "Please enter Valid Passport number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    // if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.aadhar)){
    //   this.alertMessage = "Please enter aadhar card number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.panNumber)){
    //   this.alertMessage = "Please enter PAN card number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }else if(!this.validationService.validatePancardNumber(employeeObj.panNumber)){
    //   this.alertMessage = "Please enter Valid PAN card number !!";
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

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
      this.alertMessage = "Please enter Current Address !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.state)){
      this.alertMessage = "Please enter state !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if(!this.validationService.validateAlphaWithSpace(employeeObj.state)){
      this.alertMessage = "Please enter Valid state !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.city)){
      this.alertMessage = "Please enter city !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if(!this.validationService.validateAlphaWithSpace(employeeObj.city)){
      this.alertMessage = "Please enter Valid city !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.country)){
      this.alertMessage = "Please enter country !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if(!this.validationService.validateAlphaWithSpace(employeeObj.country)){
      this.alertMessage = "Please enter Valid country !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.pincode)){
      this.alertMessage = "Please enter pincode !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if(!this.validationService.validatePincodeNumber(employeeObj.pincode)){
      this.alertMessage = "Please enter Valid pincode !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.alternateMobileNo)){
      this.alertMessage = "Please enter alternate mobile number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if(!this.validationService.validateMobileNumber(employeeObj.alternateMobileNo)){
      this.alertMessage = "Please enter valid alternate mobile number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } 

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.permanentAddress)){
      this.alertMessage = "Please enter permanent address !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.emergencyContactPerson)){
      this.alertMessage = "Please enter Emergency Contact Person Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if(!this.validationService.validateAlphaWithSpace(employeeObj.emergencyContactPerson)){
      this.alertMessage = "Please enter Valid Emergency Contact Person Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.relation)){
      this.alertMessage = "Please enter Emergency Contact Person Relation !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if(!this.validationService.validateAlphaWithSpace(employeeObj.relation)){
      this.alertMessage = "Please enter Valid Emergency Contact Person Relation !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.emergencyContactMobile)){
      this.alertMessage = "Please enter Emergency Contact Person Mobile Number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else  if(!this.validationService.validateMobileNumber(employeeObj.emergencyContactMobile)){
      this.alertMessage = "Please enter Valid Emergency Contact Person Mobile Number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.graduationType)){
      this.alertMessage = "Please Select Graduation !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.pursuing)){
      this.alertMessage = "Please Select Pursuing !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.yearOfPassing)){
      this.alertMessage = "Please Enter Year of Passing !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.passingGrade)){
      this.alertMessage = "Please Enter Passing Grade !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.experience)){
      this.alertMessage = "Please Select Experience !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(employeeObj.experience == 'Exprienced'){
      this.allPreviousEmployment.forEach((previousEmployer, index) =>{
        if(!this.validationService.validateNullUndefinedEmptyString(previousEmployer.employerName)){
          this.alertMessage = `Please Enter Employer Name - ${index}!!`;
          this.openAlertMod(template, this.alertMessage);
          return false;
        }
    
        if(!this.validationService.validateNullUndefinedEmptyString(previousEmployer.dateOfJoining)){
          this.alertMessage = `Please Enter Date of Joining - ${index}!!`
          this.openAlertMod(template, this.alertMessage);
          return false;
        }
    
        if(!this.validationService.validateNullUndefinedEmptyString(previousEmployer.dateOfRelieving)){
          this.alertMessage = `Please Enter Date of Relieving - ${index}!!`
          this.openAlertMod(template, this.alertMessage);
          return false;
        }
  
        if(!this.validationService.validateNullUndefinedEmptyString(previousEmployer.yearsOfExperience)){
          this.alertMessage = `Please Enter Years of Experience - ${index}!!`
          this.openAlertMod(template, this.alertMessage);
          return false;
        }
  
        if(!this.validationService.validateNullUndefinedEmptyString(previousEmployer.managerName)){
          this.alertMessage = `Please Enter Manager Name - ${index}!!`
          this.openAlertMod(template, this.alertMessage);
          return false;
        }
  
        if(!this.validationService.validateNullUndefinedEmptyString(previousEmployer.managerContactNumber)){
          this.alertMessage = `Please Enter Manager Contact Number - ${index}!!`
          this.openAlertMod(template, this.alertMessage);
          return false;
        }
  
        if(!this.validationService.validateNullUndefinedEmptyString(previousEmployer.hrName)){
          this.alertMessage = `Please Enter HR Name - ${index}!!`
          this.openAlertMod(template, this.alertMessage);
          return false;
        }
  
        if(!this.validationService.validateNullUndefinedEmptyString(previousEmployer.hrContactNumber)){
          this.alertMessage = `Please Enter HR Contact Number - ${index}!!`
          this.openAlertMod(template, this.alertMessage);
          return false;
        }
      });
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.bankName)){
      this.alertMessage = "Please enter Bank Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }if(!this.validationService.validateAlphaWithSpace(employeeObj.bankName)){
      this.alertMessage = "Please enter Valid Bank Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.bankAccountNo)){
      this.alertMessage = "Please enter Bank Account Number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }if(!this.validationService.validateAlphaNumeric(employeeObj.bankAccountNo)){
      this.alertMessage = "Please enter Valid Bank Account Number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.bankIFSCCode)){
      this.alertMessage = "Please enter Bank IFSC Code !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }if(!this.validationService.validateAlphaNumeric(employeeObj.bankIFSCCode)){
      this.alertMessage = "Please enter Valid Bank IFSC Code !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(this.validationService.validateNullUndefinedEmptyString(employeeObj.pfAccountNumber) && !this.validationService.validateAlphaNumeric(employeeObj.pfAccountNumber)){
      this.alertMessage = "Please enter Valid PF Account Number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(this.validationService.validateNullUndefinedEmptyString(employeeObj.previousPfAccountNumber) && !this.validationService.validateAlphaNumeric(employeeObj.previousPfAccountNumber)){
      this.alertMessage = "Please enter Valid Previous PF Account Number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if(this.validationService.validateNullUndefinedEmptyString(employeeObj.uan) && !this.validationService.validateAlphaNumeric(employeeObj.uan)){
      this.alertMessage = "Please enter Valid UAN Number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if(this.validationService.validateNullUndefinedEmptyString(employeeObj.esicNumber) && !this.validationService.validateAlphaNumeric(employeeObj.esicNumber)){
      this.alertMessage = "Please enter Valid ESIC Number !!"
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
      imageElement.setAttribute("src", "assets/Images/default-user-image.jpeg");
    }
  }

  onUpdateEmployeeInfo(template: TemplateRef<any>){
    const dateFormat = 'YYYY-MM-DD';
    let inputValidated:boolean  = this.validateEmployeeObj(this.UpdateEmployeeInfo, template)
    if(!inputValidated) return;

    // transform date formats to YYYY-MM-DD
    this.UpdateEmployeeInfo.dateOfBirth = moment(this.UpdateEmployeeInfo.dateOfBirth ).format(dateFormat);
    this.UpdateEmployeeInfo.dateOfJoining = moment(this.UpdateEmployeeInfo.dateOfJoining).format(dateFormat);

    this.allCertificationList.forEach(certificaiton => {
      console.log("All certificaiton : ", this.allCertificationList);
      certificaiton.dateOfCompletion = moment(certificaiton.dateOfCompletion).format(dateFormat);
        if((certificaiton != undefined && Object.keys(certificaiton).length !== 0)&& (certificaiton.employeeCertificateId == undefined || certificaiton.employeeCertificateId == null)){
          console.log("New certificaiton : ", certificaiton);
          this.updatedCertificationList.push(certificaiton);
        }
    });

    this.allPreviousEmployment.forEach(prevEmployer => {
      console.log("All Prev Employer : ", this.allPreviousEmployment);
      if((prevEmployer != undefined && Object.keys(prevEmployer).length !== 0)&&(prevEmployer.previousEmploymentId == undefined || prevEmployer.previousEmploymentId == null)){
        console.log("New Prev Employer : ", prevEmployer);
        this.updatedPreviousEmployment.push(prevEmployer);
      }
    });

    this.UpdateEmployeeInfo.certifications = (Object.keys(this.allCertificationList[0]).length === 0) ? null : this.allCertificationList;
    this.UpdateEmployeeInfo.previousEmploymentList = (Object.keys(this.allPreviousEmployment[0]).length === 0) ? null : this.allPreviousEmployment;
    this.UpdateEmployeeInfo.updatedCertifications = (this.updatedCertificationList.length === 0) ? null : this.updatedCertificationList;
    this.UpdateEmployeeInfo.updatedPreviousEmploymentList = (this.updatedPreviousEmployment.length === 0) ? null : this.updatedPreviousEmployment;

    this.UpdateEmployeeInfo.updatedBy =  this.currentUser.empId;;
    console.log("Update Profile : ", this.UpdateEmployeeInfo);
    this.employeeService.updateEmployeeProfile(this.UpdateEmployeeInfo).pipe(first()).subscribe((response: any) => {
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

  //Employee Info Update 
  openUpdateInfo(template: TemplateRef<any>) {
    this.modalRef = this.modalService.show(template, { class: 'modal-xl', backdrop: 'static', keyboard: false});
  }

  onDocSubmit(){
    this.cancelRequest();
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
