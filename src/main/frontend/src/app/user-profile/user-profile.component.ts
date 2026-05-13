import { DatePipe, LocationStrategy } from '@angular/common';
import { Component, OnInit, SecurityContext, TemplateRef, ViewChild } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import * as moment from 'moment';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs/operators';
import { Asset } from '../models/asset';
import { certification } from '../models/certification';
import { Domain } from '../models/domain';
import { Employee } from '../models/employee';
import { Feature } from '../models/feature';
import { PreviousEmployer } from '../models/previousEmployer';
import { AuthenticationService } from '../services/authentication.service';
import { DomainService } from '../services/domain.service';
import { EmployeeService } from '../services/employee.service';
import { ImageService } from '../services/image.service';
import { OnBoardingService } from '../services/on-boarding.service';
import { ValidationService } from '../services/validation.service';
import { Skills } from '../models/skills';
import { Certificate } from '../models/certificate';
import { UpdateUserInfoService } from '../services/updateUserInfo.service';
@Component({
  standalone: false,
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css']
})
export class UserProfileComponent implements OnInit {

  //flags
  isUpdateProfile: boolean = false;
  showImageUpload: boolean = false;

  currentUser: any;
  currentEmployeeInfo: Employee = new Employee();
  UpdateEmployeeInfo: Employee = new Employee();

  profileImage: File;
  previewImage: any;
  profileImageName: any;

  feature = "Profile";
  userMapping: any = {};

  //modal
  alertMessage: any;
  modalRef:NgbModalRef;


  modalRef1:NgbModalRef;
  draftObj:Employee = new Employee();
  hasPendingRequest: boolean = false;
  allCertificationList: any[] = [];
  allPreviousEmployment: any[] = [];
  updatedCertificationList: any[] = [];
  updatedPreviousEmployment: any[] = [];
  yearOfPassingList: any[] = [];
  domainSpecializationList: any[] = [];
  allAssetList: any[] = [];


  @ViewChild('updateInfo')
  private updateInfoTempRef: TemplateRef<any>;

  constructor(
    private employeeService: EmployeeService,
    private validationService: ValidationService,
    private authenticationService: AuthenticationService,
    private datePipe: DatePipe,
    private modalService: NgbModal,
    private sanitizer: DomSanitizer,
    private imageService: ImageService,
    private locationStrategy: LocationStrategy,
    private domainService: DomainService,
    private onBoardingService: OnBoardingService,
    private updateUserInfoService: UpdateUserInfoService
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    this.onGetEmployeeInfo();
    this.checkExistingDraft();
    this.getMyAssetList();

    // Dynamic Subfeature Flags
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    //console.log(this.feature, this.userMapping);
    this.setYearOfPassingList();
    this.preventBackButton();
    this.getAllSkillsOfEmployee();
    this.getAllCertificatesOfEmployee();
  }
  preventBackButton() {
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(() => {
      history.pushState(null, null, location.href);
    })
  }

  ngAfterViewInit() {
    this.setCalenderMaxDate();
  }

async checkExistingDraft() {
  try {
    this.draftObj = await this.updateUserInfoService.getDraftByEmpId();

    if (this.draftObj) {
      this.hasPendingRequest = true;
    } else {
      this.hasPendingRequest = false;
    }
  } catch (error) {
    this.hasPendingRequest = false;
  }
}
  currentDateFilter = (d: Date) => {
    const dateFormat = 'YYYY-MM-DD';
    const currentDate = new Date();

    return (moment(d).format(dateFormat) <= moment(currentDate).format(dateFormat));
  }

  setYearOfPassingList() {
    for (let start = 1990; start < 2051; start++) {
      this.yearOfPassingList.push(start);
    }
  }

  stringToNumber(year: any) {
    this.UpdateEmployeeInfo.yearOfPassing = Number.parseInt(year);
  }

  setCalenderMaxDate() {
    const today = this.datePipe.transform(new Date(), 'yyyy-MM-dd');
    let DOB = document.getElementById('DOB');
    DOB?.setAttribute('max', today);
  }

  showUpdateProfile() {
    this.isUpdateProfile = true;
    this.showImageUpload = true;
    this.openUpdateInfo(this.updateInfoTempRef);
  }

  showViewProfile() {
    this.isUpdateProfile = false;
    this.showImageUpload = true;
    this.onGetEmployeeInfo();
  }

  // Manage employer
  addInputPreviousEmployerField() {
    let newPrevEmployerObj = new PreviousEmployer();
    this.allPreviousEmployment.push(newPrevEmployerObj);
  }

  removeInputPreviousEmployerField(prevEmployerObj) {
    this.allPreviousEmployment.forEach((value, index) => {
      if (value == prevEmployerObj) {
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
      if (value == certificationObj) {
        this.updatedCertificationList.push(value);
        this.allCertificationList.splice(index, 1);
      }
    });
  }

  validateEmployeeObj(employeeObj: Employee, template: TemplateRef<any>) {


    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.viewsOnOrganisation)) {
      this.alertMessage = "Please enter your view on organisation !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateDiscriptionUserProfile(employeeObj.viewsOnOrganisation)) {
      this.alertMessage = "Only string character will be valid in  your view on organisation !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.gender)) {
      this.alertMessage = "Please select gender !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.dateOfBirth)) {
      this.alertMessage = "Please enter date of birth !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.aboutMe)) {
      this.alertMessage = "Please enter About me !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateAboutMeUserProfile(employeeObj.aboutMe)) {
      this.alertMessage = "Only string character will be valid in About me  !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.bloodGroup)) {
      this.alertMessage = "Please enter Blood group";
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateBloodGroup(employeeObj.bloodGroup)) {
      this.alertMessage = "Please enter Valid Blood Group !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.fatherName)) {
      this.alertMessage = "Please enter father name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateAlphaWithSpace(employeeObj.fatherName)) {
      this.alertMessage = "Please enter Valid father Name !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.placeOfBirth)) {
      this.alertMessage = "Please enter place of birth !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateAlphaWithSpace(employeeObj.placeOfBirth)) {
      this.alertMessage = "Please enter Valid Place of birth !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (this.validationService.validateNullUndefinedEmptyString(employeeObj.motherTongue) && !this.validationService.validateAlphaWithSpace(employeeObj.motherTongue)) {
      this.alertMessage = "Please enter Valid Mother tongue number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.maritalStatus)) {
      this.alertMessage = "Please enter select Marital status !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (this.validationService.validateNullUndefinedEmptyString(employeeObj.passportNumber) && !this.validationService.validatePassportNumber(employeeObj.passportNumber)) {
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

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.mobileNo)) {
      this.alertMessage = "Please enter mobile number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateMobileNumber(employeeObj.mobileNo)) {
      this.alertMessage = "Please enter valid mobile number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.address)) {
      this.alertMessage = "Please enter Current Address !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.state)) {
      this.alertMessage = "Please enter state !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateAlphaWithSpace(employeeObj.state)) {
      this.alertMessage = "Please enter Valid state !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.city)) {
      this.alertMessage = "Please enter city !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateAlphaWithSpace(employeeObj.city)) {
      this.alertMessage = "Please enter Valid city !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.country)) {
      this.alertMessage = "Please enter country !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateAlphaWithSpace(employeeObj.country)) {
      this.alertMessage = "Please enter Valid country !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.pincode)) {
      this.alertMessage = "Please enter pincode !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validatePincodeNumber(employeeObj.pincode)) {
      this.alertMessage = "Please enter Valid pincode !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.alternateMobileNo)) {
      this.alertMessage = "Please enter alternate mobile number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateMobileNumber(employeeObj.alternateMobileNo)) {
      this.alertMessage = "Please enter valid alternate mobile number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.permanentAddress)) {
      this.alertMessage = "Please enter permanent address !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.emergencyContactPerson)) {
      this.alertMessage = "Please enter Emergency Contact Person Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateAlphaWithSpace(employeeObj.emergencyContactPerson)) {
      this.alertMessage = "Please enter Valid Emergency Contact Person Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.relation)) {
      this.alertMessage = "Please enter Emergency Contact Person Relation !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateAlphaWithSpace(employeeObj.relation)) {
      this.alertMessage = "Please enter Valid Emergency Contact Person Relation !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.emergencyContactMobile)) {
      this.alertMessage = "Please enter Emergency Contact Person Mobile Number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateMobileNumber(employeeObj.emergencyContactMobile)) {
      this.alertMessage = "Please enter Valid Emergency Contact Person Mobile Number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.graduationType)) {
      this.alertMessage = "Please Select Graduation !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.pursuing)) {
      this.alertMessage = "Please Select Pursuing !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if (employeeObj.pursuing == 'No') {
      if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.yearOfPassing)) {
        this.alertMessage = "Please Enter Year of Passing !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }


    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.passingGrade)) {
      this.alertMessage = "Please Enter Passing Grade !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.experience)) {
      this.alertMessage = "Please Select Experience !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (employeeObj.experience == 'Experienced') {
      this.allPreviousEmployment.forEach((previousEmployer, index) => {
        if (!this.validationService.validateNullUndefinedEmptyString(previousEmployer.employerName)) {
          this.alertMessage = `Please Enter Employer Name - ${index}!!`;
          this.openAlertMod(template, this.alertMessage);
          return false;
        }

        if (!this.validationService.validateNullUndefinedEmptyString(previousEmployer.dateOfJoining)) {
          this.alertMessage = `Please Enter Date of Joining - ${index}!!`
          this.openAlertMod(template, this.alertMessage);
          return false;
        }

        if (!this.validationService.validateNullUndefinedEmptyString(previousEmployer.dateOfRelieving)) {
          this.alertMessage = `Please Enter Date of Relieving - ${index}!!`
          this.openAlertMod(template, this.alertMessage);
          return false;
        }

        if (!this.validationService.validateNullUndefinedEmptyString(previousEmployer.yearsOfExperience)) {
          this.alertMessage = `Please Enter Years of Experience - ${index}!!`
          this.openAlertMod(template, this.alertMessage);
          return false;
        }

        if (!this.validationService.validateNullUndefinedEmptyString(previousEmployer.managerName)) {
          this.alertMessage = `Please Enter Manager Name - ${index}!!`
          this.openAlertMod(template, this.alertMessage);
          return false;
        }

        if (!this.validationService.validateNullUndefinedEmptyString(previousEmployer.managerContactNumber)) {
          this.alertMessage = `Please Enter Manager Contact Number - ${index}!!`
          this.openAlertMod(template, this.alertMessage);
          return false;
        }

        if (!this.validationService.validateNullUndefinedEmptyString(previousEmployer.hrName)) {
          this.alertMessage = `Please Enter HR Name - ${index}!!`
          this.openAlertMod(template, this.alertMessage);
          return false;
        }

        if (!this.validationService.validatePhoneNumber(previousEmployer.hrContactNumber)) {
          this.alertMessage = `Please Enter HR Contact Number - ${index}!!`
          this.openAlertMod(template, this.alertMessage);
          return false;
        }
      });
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.bankName)) {
      this.alertMessage = "Please enter Bank Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } if (!this.validationService.validateAlphaWithSpace(employeeObj.bankName)) {
      this.alertMessage = "Please enter Valid Bank Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.bankAccountNo)) {
      this.alertMessage = "Please enter Bank Account Number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } if (!this.validationService.validateAlphaNumeric(employeeObj.bankAccountNo)) {
      this.alertMessage = "Please enter Valid Bank Account Number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.bankIFSCCode)) {
      this.alertMessage = "Please enter Bank IFSC Code !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } if (!this.validationService.validateCapitalAlphaNumeric(employeeObj.bankIFSCCode)) {
      this.alertMessage = "Please enter Valid Bank IFSC Code !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (this.validationService.validateNullUndefinedEmptyString(employeeObj.pfAccountNumber) && !this.validationService.validateAlphaNumeric(employeeObj.pfAccountNumber)) {
      this.alertMessage = "Please enter Valid PF Account Number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (this.validationService.validateNullUndefinedEmptyString(employeeObj.previousPfAccountNumber) && !this.validationService.validateAlphaNumeric(employeeObj.previousPfAccountNumber)) {
      this.alertMessage = "Please enter Valid Previous PF Account Number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if (this.validationService.validateNullUndefinedEmptyString(employeeObj.uan) && !this.validationService.validateAlphaNumeric(employeeObj.uan)) {
      this.alertMessage = "Please enter Valid UAN Number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if (this.validationService.validateNullUndefinedEmptyString(employeeObj.esicNumber) && !this.validationService.validateAlphaNumeric(employeeObj.esicNumber)) {
      this.alertMessage = "Please enter Valid ESIC Number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.totalExperience)) {
      this.alertMessage = "Please enter Total Previous Work Experience !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateNumber(employeeObj.totalExperience)) {
      this.alertMessage = "Please enter digit !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    return true;
  }

  async onGetEmployeeInfo() {
    this.domainSpecializationList = [];
    this.currentEmployeeInfo = new Employee();
    let currentEmp = new Employee();
    currentEmp.empId = this.currentUser.empId;
    currentEmp.isDraft = false;
    //console.log("currentEmp : ", currentEmp);

    const response: any = await this.employeeService.getEmployeeByEmpId(currentEmp).toPromise();
    if (response.serviceStatus == "Success") {
      this.currentEmployeeInfo = response.serviceResponse;
      this.currentEmployeeInfo.totalCurrentExperience=this.employeeService.calculateTotalExperience(
          this.currentEmployeeInfo.totalExperience, this.currentEmployeeInfo.dateOfJoining );

      this.loadProfileImage(this.currentEmployeeInfo.imageBytes)

    } else {
      console.error(response.serviceResponse);
    }

    const docResponse: any = await this.imageService.getEmployeeDocuments(currentEmp).toPromise();
    if (docResponse.serviceStatus == 'Success') {
      this.currentEmployeeInfo.documentList = docResponse.serviceResponse;

      //console.log("this.previewObj.documentList : ", this.currentEmployeeInfo.documentList);
    } else {
      //console.log(docResponse.serviceResponse);
    }

    let domainObj = new Domain();
    domainObj.empId = this.currentUser.empId;
    const domainResponse: any = await this.domainService.getDomainSpecializationByEmpId(domainObj).toPromise();
    if (domainResponse.serviceStatus == "Success") {
      this.domainSpecializationList = domainResponse.serviceResponse;

      this.domainSpecializationList.forEach((object: Domain) => {

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

    setTimeout(() => {
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

  loadProfileImage(imageByte: any) {
    let imageElement = document.getElementById('user-avatar');
    if (imageByte) {
      let objectURL = 'data:image/*;base64,' + imageByte;
      let src: string = this.sanitizer.sanitize(SecurityContext.RESOURCE_URL, this.sanitizer.bypassSecurityTrustResourceUrl(objectURL));
      imageElement.setAttribute("src", src);
    } else {
      imageElement.setAttribute("src", "assets/Images/default-user-image.jpeg");
    }
  }

  onUpdateEmployeeInfo(template: TemplateRef<any>) {
    const dateFormat = 'YYYY-MM-DD';
    let inputValidated: boolean = this.validateEmployeeObj(this.UpdateEmployeeInfo, template)
    if (!inputValidated) return;

    // transform date formats to YYYY-MM-DD
    this.UpdateEmployeeInfo.dateOfBirth = moment(this.UpdateEmployeeInfo.dateOfBirth).format(dateFormat);
    this.UpdateEmployeeInfo.dateOfJoining = moment(this.UpdateEmployeeInfo.dateOfJoining).format(dateFormat);

    this.allCertificationList.forEach(certificaiton => {
      //console.log("All certificaiton : ", this.allCertificationList);
      certificaiton.dateOfCompletion = moment(certificaiton.dateOfCompletion).format(dateFormat);
      if ((certificaiton != undefined && Object.keys(certificaiton).length !== 0) && (certificaiton.employeeCertificateId == undefined || certificaiton.employeeCertificateId == null)) {
        //console.log("New certificaiton : ", certificaiton);
        this.updatedCertificationList.push(certificaiton);
      }
    });

    this.allPreviousEmployment.forEach(prevEmployer => {
      //console.log("All Prev Employer : ", this.allPreviousEmployment);
      if ((prevEmployer != undefined && Object.keys(prevEmployer).length !== 0) && (prevEmployer.previousEmploymentId == undefined || prevEmployer.previousEmploymentId == null)) {
        //console.log("New Prev Employer : ", prevEmployer);
        this.updatedPreviousEmployment.push(prevEmployer);
      }
    });

    this.UpdateEmployeeInfo.certifications = (Object.keys(this.allCertificationList[0]).length === 0) ? null : this.allCertificationList;
    this.UpdateEmployeeInfo.previousEmploymentList = (Object.keys(this.allPreviousEmployment[0]).length === 0) ? null : this.allPreviousEmployment;
    this.UpdateEmployeeInfo.updatedCertifications = (this.updatedCertificationList.length === 0) ? null : this.updatedCertificationList;
    this.UpdateEmployeeInfo.updatedPreviousEmploymentList = (this.updatedPreviousEmployment.length === 0) ? null : this.updatedPreviousEmployment;

    this.UpdateEmployeeInfo.updatedBy = this.currentUser.empId;;
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


  onProfileImageSelect(event: any, template: TemplateRef<any>) {
    let uploadLabel = document.getElementById("profileImgLabel");
    let label = `Upload Image <i class="fa-solid fa-angles-right"></i>`;
    const allowedTypes = ['image/png', 'image/jpeg', 'image/jpg'];
    const uploadedFiles = event.target.files;
    if (uploadedFiles[0] && allowedTypes.indexOf(uploadedFiles[0].type) === -1) {
      this.openAlertMod(template, 'Please select a valid image file (png, jpeg, or jpg).');
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

  onUploadImage(template: TemplateRef<any>) {
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




  getMyAssetList() {

    let assetObj = new Asset();
    assetObj.employeementId = this.currentUser.employeementId;
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
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl', backdrop: true, keyboard: true });

    this.modalRef.result.then(
      () => { this.resetUpdateState(); },
      () => { this.resetUpdateState(); }
    );
  }
  private resetUpdateState() {
    this.isUpdateProfile = false;
    this.onGetEmployeeInfo();
    this.checkExistingDraft();
  }

  onDocSubmit() {
    this.cancelRequest();
  }

  // Modal
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef?.close();
  }


  currentIndex = 0;
  itemsPerPage = 4;
  visibleSkills = [];


  // skills = [
  //   { name: 'JavaScript', level: 'Expert' },
  //   { name: 'TypeScript', level: 'Intermediate' },
  //   { name: 'Angular', level: 'Expert' },
  //   { name: 'React', level: 'Beginner' },
  //   { name: 'lalala', level: 'Expert' },
  //   { name: 'lala1', level: 'Expert' }
  // ];


  skills: any[] = [];

  updateVisibleSkills() {

    this.visibleSkills = this.skills.slice(this.currentIndex, this.currentIndex + this.itemsPerPage);
  }



  nextSkills() {
    if (this.currentIndex + this.itemsPerPage < this.skills.length) {
      this.currentIndex += this.itemsPerPage;
      this.updateVisibleSkills();
    }
  }

  prevSkills() {
    if (this.currentIndex - this.itemsPerPage >= 0) {
      this.currentIndex -= this.itemsPerPage;
      this.updateVisibleSkills();
    }
  }



  // Dropdown options
  skillLevels = ['Beginner', 'Intermediate', 'Expert'];

  openModal = false;
  selectedSkill: Skills = new Skills();
  isEditMode = false;
  openSkillAdditionForm() {
    this.isEditMode = false;

    this.openModal = true;
  }

  editSkill(skill: Skills) {
    this.isEditMode = true;
    this.selectedSkill = skill;
    this.openModal = true;
  }
  openConfirmModal = false;
   confirmMessage : any;
  openDeleteConfirm(skill: any) {
  this.selectedSkill = skill;
  this.confirmMessage = `Are you sure you want to delete "${skill.skillName}" from your skill set?`;
  this.openConfirmModal = true;
   this.openCertificateConfirmModal = false;
}

selectedCertificate:any;
openCertificateConfirmModal= false;
openDeleteCertificate(certificate: any){
  this.selectedCertificate = certificate;
     this.confirmMessage = `Are you sure you want to delete the certificate of  "${certificate.certificationName}" ?`;
     this.openCertificateConfirmModal = true;
     this.openConfirmModal = false;
}






  deleteSkill(skill:any){
    console.log("delet krdeli",this.selectedSkill);

     this.employeeService.deleteSkillOfEmployee(this.selectedSkill).subscribe({
    next: (res) => {
      console.log("Deleted successfully", res);
      this.openConfirmModal = false;
       this.messageText = "Skill removed successfully!";
      this.openMessageModal = true;


    },
    error: (err) => {
      console.error("Delete failed", err);
      this.openConfirmModal = false;
    }
  });
  }


    deleteCertificate(certificate:any){
    console.log("delet krdeli",this.selectedSkill);

     this.employeeService.deleteCertificateOfEmployee(this.selectedCertificate).subscribe({
    next: (res) => {
      console.log("Deleted successfully", res);
      this.openCertificateConfirmModal = false;
       this.messageText = "Certificate deleted successfully!";
      this.openMessageModal = true;


    },
    error: (err) => {
      console.error("Delete failed", err);
      this.openConfirmModal = false;
    }
  });
  }


  openMessageModal: boolean = false;
  messageText: string = '';

  saveSkill(event: any) {
    if (event?.type === 'message') {
      this.messageText = event.text;
      this.openMessageModal = true;
    } else {
      this.openMessageModal = true;
    }
  }

  handleCloseMessageModal() {
    this.openMessageModal = false;
    this.getAllSkillsOfEmployee();
      this.getAllCertificatesOfEmployee();
  }

  skillsObj: Skills = new Skills();
  getAllSkillsOfEmployee() {
    this.skillsObj.empId = this.currentUser.empId;
    this.employeeService.getSkillOfEmployee(this.skillsObj).pipe(first()).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {

          this.skills = response.serviceResponse;
          // this.skills = response.serviceResponse.map((s: any) => ({
          //   empSkillId: s.empSkillId,
          //   name: s.skillName ? s.skillName : s.additionalSkill,
          //   proficiencyLevel: s.proficiencyLevel,
          //   imageUrl: s.imageUrl,
          //   proficiencyId: s.proficiencyId
          // }));

          this.skills.forEach(s => {
            s.skillName = s.skillName ? s.skillName : s.additionalSkill;


          });
          this.updateVisibleSkills();
        } else {
          this.skills = [];
          this.updateVisibleSkills();
        }
      },
      error: (err) => {
        console.error(err);

      }
    });
  }


  isCertModalOpen = false;

  openCertModal() {
    this.isCertModalOpen = true;
  }

  certifications:any[]=[];
  certificateObj:Certificate = new Certificate();
    getAllCertificatesOfEmployee() {
    this.certificateObj.empId = this.currentUser.empId;
    this.employeeService.getCertificateOfEmployee(this.certificateObj).pipe(first()).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.certifications = response.serviceResponse;
        } else {
          this.certifications = [];
        }
      },
      error: (err) => {
        console.error(err);

      }
    });
  }


  download(docId: number): void {
  this.employeeService.downloadCertificate(docId).subscribe((response) => {
    if (response.serviceStatus === 'Success') {
      const fileData = response.serviceResponse;
      const byteCharacters = atob(fileData.docData);
      const byteNumbers = new Array(byteCharacters.length);
      for (let i = 0; i < byteCharacters.length; i++) {
        byteNumbers[i] = byteCharacters.charCodeAt(i);
      }
      const byteArray = new Uint8Array(byteNumbers);
      const blob = new Blob([byteArray], { type: fileData.docMimeType });

      // Download
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = fileData.docName;
      a.click();
      window.URL.revokeObjectURL(url);
    } else {
      alert(response.serviceResponse);
    }
  });
}









}
