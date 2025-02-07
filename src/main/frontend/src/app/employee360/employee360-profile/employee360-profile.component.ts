



import { DatePipe, LocationStrategy } from '@angular/common';
import { Component, OnInit, SecurityContext, TemplateRef, ViewChild } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { Router } from '@angular/router';



import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { Asset } from 'src/app/models/asset';
import { Breadcrumb } from 'src/app/models/breadcrumd';

import { certification } from 'src/app/models/certification';
import { Domain } from 'src/app/models/domain';
import { Employee } from 'src/app/models/employee';
import { Feature } from 'src/app/models/feature';
import { PreviousEmployer } from 'src/app/models/previousEmployer';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { BreadcrumbService } from 'src/app/services/breadcrumb.service';
import { DomainService } from 'src/app/services/domain.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { Employee360Service } from 'src/app/services/employee360.service';
import { ImageService } from 'src/app/services/image.service';
import { OnBoardingService } from 'src/app/services/on-boarding.service';
import { UtilityService } from 'src/app/services/utility.service';
import { ValidationService } from 'src/app/services/validation.service';


@Component({
  selector: 'app-employee360-profile',
  templateUrl: './employee360-profile.component.html',
  styleUrls: ['./employee360-profile.component.css']
})
export class Employee360ProfileComponent implements OnInit {

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
    currentBreadcrumbList: any[] = [];
  
    allCertificationList:any[] = [];
    allPreviousEmployment:any[] = [];
    updatedCertificationList:any[] = [];
    updatedPreviousEmployment:any[] = [];
    yearOfPassingList:any[] = [];
    domainSpecializationList:any[] = [];
    allAssetList:any[] = [];

    employeeData:any;
  
  
    @ViewChild('updateInfo')
    private updateInfoTempRef:TemplateRef<any>;
  
    constructor(
      private employeeService:EmployeeService,
      private validationService:ValidationService,
      private authenticationService: AuthenticationService, 
      private datePipe: DatePipe,
      private modalService: BsModalService,
      private sanitizer: DomSanitizer,
      private imageService : ImageService,
      private locationStrategy: LocationStrategy,
      private domainService:DomainService,
      private onBoardingService : OnBoardingService,
      public utilityService : UtilityService,
       private breadcrumbService: BreadcrumbService,
      private router:Router,
      
      ) {
       this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
       this.breadcrumbService.currentBreadcrumb.subscribe(x => this.currentBreadcrumbList = x);
      


        // const navigation = this.router.getCurrentNavigation();
        // this.employeeData = navigation?.extras.state.data;
        
      }
  
    ngOnInit(): void {

  
      const storedData = localStorage.getItem('employee360Data');
      const parsedData = storedData ? JSON.parse(storedData) : null;
      if(parsedData != null || parsedData != undefined ){
        this.employeeData =  parsedData;
      }else{
        this.employeeData = history.state.data;
      }

      console.log("ckekkkkk",this.employeeData);


       let findbreadcrumbObject = this.currentBreadcrumbList.findIndex(x => x.title == "Employee-360-Profile");
       console.log("ckecked breadcrums   ",findbreadcrumbObject)
          if (findbreadcrumbObject >= 0) {
            this.currentBreadcrumbList.splice(findbreadcrumbObject + 1);
            this.breadcrumbService.setBreadcrumbSubject(this.currentBreadcrumbList);
          } else {
            let breadcrumbObject = new Breadcrumb();
            breadcrumbObject.title = "Employee-360-Profile";
            breadcrumbObject.url = "/employee-360/profile";
            this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);
          }


        
      
      this.onGetEmployeeInfo();
      this.getMyAssetList();
  
      // Dynamic Subfeature Flags 
      let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
      featureMap.subFeatures?.forEach(sub => {
        this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
      });
      //console.log(this.feature, this.userMapping);
      this.setYearOfPassingList();    
      this.preventBackButton();
    }

    
    preventBackButton(){
      history.pushState(null, null, location.href);
      this.locationStrategy.onPopState(()=>{
        history.pushState(null, null, location.href);
      })
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
        this.alertMessage = "Please enter select Marital status !!"
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
      if(employeeObj.pursuing == 'No'){
        if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.yearOfPassing)){
          this.alertMessage = "Please Enter Year of Passing !!"
          this.openAlertMod(template, this.alertMessage);
          return false;
        }
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
  
      if(employeeObj.experience == 'Experienced'){
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
      }if(!this.validationService.validateCapitalAlphaNumeric(employeeObj.bankIFSCCode)){
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
      if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.totalExperience)){
        this.alertMessage = "Please enter Total Experience !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }else if(!this.validationService.validateNumber(employeeObj.totalExperience)){
        this.alertMessage = "Please enter digit !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
  
      return true;
    }
  

   

    async onGetEmployeeInfo(){


      console.log("inner fuction");

      this.domainSpecializationList = [];
      this.currentEmployeeInfo = new Employee();
      let currentEmp = new Employee();

      currentEmp.empId = this.employeeData.empId;
      currentEmp.isDraft = false;
      //console.log("currentEmp : ", currentEmp);
      
      const response: any = await this.employeeService.getEmployeeByEmpId(currentEmp).toPromise();
      sessionStorage.setItem('empId',response.serviceResponse.employeementId);
      sessionStorage.setItem('eId',response.serviceResponse.empId);
      if (response.serviceStatus == "Success") {
        this.currentEmployeeInfo = response.serviceResponse;

        //console.log("currentEmployeeInfo : ", this.currentEmployeeInfo);
        this.loadProfileImage(this.currentEmployeeInfo.imageBytes)
  
      } else {
        console.error(response.serviceResponse);
      }
  
      const docResponse:any = await this.imageService.getEmployeeDocuments(currentEmp).toPromise();
      if (docResponse.serviceStatus == 'Success') {
        this.currentEmployeeInfo.documentList = docResponse.serviceResponse;
        
        //console.log("this.previewObj.documentList : ", this.currentEmployeeInfo.documentList);
      } else {
        //console.log(docResponse.serviceResponse);
      }
  
      let domainObj = new Domain();
       domainObj.empId = this.employeeData.empId;
      const domainResponse:any = await this.domainService.getDomainSpecializationByEmpId(domainObj).toPromise();

      console.log("yessss",domainResponse)
        if (domainResponse.serviceStatus == "Success") {
          this.domainSpecializationList = domainResponse.serviceResponse;
  
          this.domainSpecializationList.forEach((object:Domain) =>{
  
            var letters = 'BCDEF'.split('');
            var color = '#';
            for (var i = 0; i < 6; i++) {
              color += letters[Math.floor(Math.random() * letters.length)];
            }
  
            object.colorCode = color;
          });
  
          //console.log(this.domainSpecializationList, " : this.domainSpecializationList");
        } else {
          console.error(domainResponse.serviceResponse);
        }
  
      setTimeout(()=>{
        this.currentEmployeeInfo.documentList && this.currentEmployeeInfo.documentList.forEach((doc, index) => {
          if (doc.documentBytes) {
            let preview = document.getElementById(`docPreview${index + 1}`);
              let objectURL = 'data:image/*;base64,' + doc.documentBytes;
              let src: string = this.sanitizer.sanitize(SecurityContext.RESOURCE_URL, this.sanitizer.bypassSecurityTrustResourceUrl(objectURL));
              preview.setAttribute('src', src);
          }
        });
      }, 500);
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
        //console.log("All certificaiton : ", this.allCertificationList);
        certificaiton.dateOfCompletion = moment(certificaiton.dateOfCompletion).format(dateFormat);
          if((certificaiton != undefined && Object.keys(certificaiton).length !== 0)&& (certificaiton.employeeCertificateId == undefined || certificaiton.employeeCertificateId == null)){
            //console.log("New certificaiton : ", certificaiton);
            this.updatedCertificationList.push(certificaiton);
          }
      });
  
      this.allPreviousEmployment.forEach(prevEmployer => {
        //console.log("All Prev Employer : ", this.allPreviousEmployment);
        if((prevEmployer != undefined && Object.keys(prevEmployer).length !== 0)&&(prevEmployer.previousEmploymentId == undefined || prevEmployer.previousEmploymentId == null)){
          //console.log("New Prev Employer : ", prevEmployer);
          this.updatedPreviousEmployment.push(prevEmployer);
        }
      });
  
      this.UpdateEmployeeInfo.certifications = (Object.keys(this.allCertificationList[0]).length === 0) ? null : this.allCertificationList;
      this.UpdateEmployeeInfo.previousEmploymentList = (Object.keys(this.allPreviousEmployment[0]).length === 0) ? null : this.allPreviousEmployment;
      this.UpdateEmployeeInfo.updatedCertifications = (this.updatedCertificationList.length === 0) ? null : this.updatedCertificationList;
      this.UpdateEmployeeInfo.updatedPreviousEmploymentList = (this.updatedPreviousEmployment.length === 0) ? null : this.updatedPreviousEmployment;
  
      this.UpdateEmployeeInfo.updatedBy =  this.employeeData.empId;;
      //console.log("Update Profile : ", this.UpdateEmployeeInfo);
      this.employeeService.updateEmployeeProfile(this.UpdateEmployeeInfo).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.openAlertMod(template, response.serviceResponse);
          this.showViewProfile();
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }
      });
    }
  
  
    onProfileImageSelect(event:any, template:TemplateRef<any>){
      let uploadLabel = document.getElementById("profileImgLabel");
      let label = `Upload Image <i class="fa-solid fa-angles-right"></i>`;
      const allowedTypes = ['image/png', 'image/jpeg', 'image/jpg'];
      const uploadedFiles = event.target.files;
      if (uploadedFiles[0] && allowedTypes.indexOf(uploadedFiles[0].type) === -1) {
        this.openAlertMod(template,'Please select a valid image file (png, jpeg, or jpg).');
        event.target.value = ''; // Clear the input
        return;
      }
  
  
      this.profileImage = null;
      this.profileImageName = null;
  
      if (uploadedFiles[0] != null) {
        this.profileImage = uploadedFiles[0];
        this.profileImageName = this.profileImage.name;
        //console.log(this.profileImage);
  
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
      formData.append("uploadedBy", this.employeeData.empId);
  
      this.employeeService.uploadImage(formData).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == 'Success') {
          this.openAlertMod(template, response.serviceResponse);
          this.showViewProfile();
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }
      });
    }
  
    getMyAssetList(){
  
      let assetObj = new Asset();

      let employementid = Number(this.utilityService.getEmployeeIdSubstring2(this.employeeData))
      assetObj.employeementId = employementid;
      this.onBoardingService.getEmployeeOnBoardingDetailByEmployeementId(assetObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allAssetList = response.serviceResponse;
          this.allAssetList = this.allAssetList.filter(x => x.assetType == 'both');
          //console.log("this.allAssetList :", this.allAssetList);
        } else {
          console.error(response.serviceResponse);
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
