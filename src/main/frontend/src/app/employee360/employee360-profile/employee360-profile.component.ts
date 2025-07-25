



import { DatePipe, LocationStrategy } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, SecurityContext, TemplateRef, ViewChild } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';

import { AppComponent } from 'src/app/app.component';

import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { Asset } from 'src/app/models/asset';
import { Breadcrumb } from 'src/app/models/breadcrumd';

import { certification } from 'src/app/models/certification';
import { Designation } from 'src/app/models/designation';
import { Domain } from 'src/app/models/domain';
import { Employee } from 'src/app/models/employee';
import { Feature } from 'src/app/models/feature';
import { PreviousEmployer } from 'src/app/models/previousEmployer';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { BreadcrumbService } from 'src/app/services/breadcrumb.service';
import { DestinationService } from 'src/app/services/destination.service';
import { DomainService } from 'src/app/services/domain.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { ImageService } from 'src/app/services/image.service';
import { JobRoleService } from 'src/app/services/job-role.service';
import { OnBoardingService } from 'src/app/services/on-boarding.service';
import { UtilityService } from 'src/app/services/utility.service';
import { ValidationService } from 'src/app/services/validation.service';

import { DepartmentService } from 'src/app/services/department.service';
@Component({
  selector: 'app-employee360-profile',
  templateUrl: './employee360-profile.component.html',
  styleUrls: ['./employee360-profile.component.css']
})
export class Employee360ProfileComponent implements OnInit {
  employeeId!: string;
  //flags 
  isUpdateProfile: boolean = false;

  currentUser: any;
  currentEmployeeInfo: Employee = new Employee();
  UpdateEmployeeInfo: Employee = new Employee();

  profileImage: File;
  previewImage: any;
  profileImageName: any;

  feature = "Profile";
  userMapping: any = {};

  // <-->
  allDomainList: any[] = [];
  specializationList: any[] = [];
  filteredJobRoleList: any[] = [];
  allJobRoleList: any[] = [];
  allDesignationList: any[] = [];
  allDeptList: any[] = [];
  @ViewChild('change_manager_template')
  changeManagerTemplate: TemplateRef<any>;
  managerList: any = [];
  filters: any = {};
  isForm: boolean = false;
  isTable: boolean = false;
  isDraft: boolean = false;
  isUpdation: boolean = false;
  employeeObj: Employee = new Employee();
  onselectYes: boolean = false;
  managerFlag: boolean = false;
  isSearchEnabled: boolean = false;
  listOfReporties: any;
  // <-->
  //modal 
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();
  currentBreadcrumbList: any[] = [];
  referedTypeStatus: boolean = false;
  allCertificationList: any[] = [];
  allPreviousEmployment: any[] = [];
  updatedCertificationList: any[] = [];
  updatedPreviousEmployment: any[] = [];
  yearOfPassingList: any[] = [];
  domainSpecializationList: any[] = [];
  allAssetList: any[] = [];

  employeeData: any;


  @ViewChild('updateInfo')
  private updateInfoTempRef: TemplateRef<any>;

  constructor(
    private employeeService: EmployeeService,
    private validationService: ValidationService,
    private authenticationService: AuthenticationService,
    private datePipe: DatePipe,
    private modalService: BsModalService,
    private sanitizer: DomSanitizer,
    private imageService: ImageService,
    private locationStrategy: LocationStrategy,
    private domainService: DomainService,
    private onBoardingService: OnBoardingService,
    public utilityService: UtilityService,
    private breadcrumbService: BreadcrumbService,
    private jobRoleService: JobRoleService,
    private destinationService: DestinationService,
    private departmentService: DepartmentService,
    private router: Router,
    private route1: ActivatedRoute,
    private cdr: ChangeDetectorRef

  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    this.breadcrumbService.currentBreadcrumb.subscribe(x => this.currentBreadcrumbList = x);



    // const navigation = this.router.getCurrentNavigation();
    // this.employeeData = navigation?.extras.state.data;

  }

  ngOnInit(): void {
   
    const storedData = sessionStorage.getItem('employee360Data');
  
    const parsedData = storedData ? JSON.parse(storedData) : null;
    if (parsedData != null || parsedData != undefined) {
      this.employeeData = parsedData;
    } else {
      this.employeeData = history.state.data;
    }
    
    this.getAllDepartmentList();
   this.getAllDomain();
   this.getAllJobRoleList();
   this.getManagersList1();
   
    
  
    
    let findbreadcrumbObject = this.currentBreadcrumbList.findIndex(x => x.title == "Employee-360-Profile");

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
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    //console.log(this.feature, this.userMapping);
    this.setYearOfPassingList();
    this.preventBackButton();
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
  // <-------->


  // <-->
  stringToNumber(year: any) {
    this.UpdateEmployeeInfo.yearOfPassing = Number.parseInt(year);
  }

  setCalenderMaxDate() {
    const today = this.datePipe.transform(new Date(), 'yyyy-MM-dd');
    let DOB = document.getElementById('DOB');
    DOB?.setAttribute('max', today);
  }

  showUpdateProfile() {
    this.isUpdation = true;

    // this.openUpdateInfo(this.updateInfoTempRef);
  }

  showViewProfile() {
    this.isUpdation = false;

    this.onGetEmployeeInfo();
  }


  getEmpIdPrefix(employeeType: string): string {
  switch (employeeType) {
    case 'Consultant':
      return 'CS-';
    case 'Apmosys Product':
      return 'AP-';
    default:
      return 'A-';
  }
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

  // validateEmployeeObj(employeeObj: Employee, template: TemplateRef<any>) {


  //   if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.viewsOnOrganisation)) {
  //     this.alertMessage = "Please enter your view on organisation !!";
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   } else if (!this.validationService.validateDiscriptionUserProfile(employeeObj.viewsOnOrganisation)) {
  //     this.alertMessage = "Only string character will be valid in  your view on organisation !!";
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }

  //   if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.gender)) {
  //     this.alertMessage = "Please select gender !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }

  //   if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.dateOfBirth)) {
  //     this.alertMessage = "Please enter date of birth !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }

  //   if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.aboutMe)) {
  //     this.alertMessage = "Please enter About me !!";
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   } else if (!this.validationService.validateAboutMeUserProfile(employeeObj.aboutMe)) {
  //     this.alertMessage = "Only string character will be valid in About me  !!";
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }

  //   if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.bloodGroup)) {
  //     this.alertMessage = "Please enter Blood group";
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   } else if (!this.validationService.validateBloodGroup(employeeObj.bloodGroup)) {
  //     this.alertMessage = "Please enter Valid Blood Group !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }

  //   if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.fatherName)) {
  //     this.alertMessage = "Please enter father name !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   } else if (!this.validationService.validateAlphaWithSpace(employeeObj.fatherName)) {
  //     this.alertMessage = "Please enter Valid father Name !!";
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }

  //   if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.placeOfBirth)) {
  //     this.alertMessage = "Please enter place of birth !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   } else if (!this.validationService.validateAlphaWithSpace(employeeObj.placeOfBirth)) {
  //     this.alertMessage = "Please enter Valid Place of birth !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }

  //   if (this.validationService.validateNullUndefinedEmptyString(employeeObj.motherTongue) && !this.validationService.validateAlphaWithSpace(employeeObj.motherTongue)) {
  //     this.alertMessage = "Please enter Valid Mother tongue number !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }

  //   if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.maritalStatus)) {
  //     this.alertMessage = "Please enter select Marital status !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }

  //   if (this.validationService.validateNullUndefinedEmptyString(employeeObj.passportNumber) && !this.validationService.validatePassportNumber(employeeObj.passportNumber)) {
  //     this.alertMessage = "Please enter Valid Passport number !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }

  //   // if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.aadhar)){
  //   //   this.alertMessage = "Please enter aadhar card number !!"
  //   //   this.openAlertMod(template, this.alertMessage);
  //   //   return false;
  //   // }

  //   // if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.panNumber)){
  //   //   this.alertMessage = "Please enter PAN card number !!"
  //   //   this.openAlertMod(template, this.alertMessage);
  //   //   return false;
  //   // }else if(!this.validationService.validatePancardNumber(employeeObj.panNumber)){
  //   //   this.alertMessage = "Please enter Valid PAN card number !!";
  //   //   this.openAlertMod(template, this.alertMessage);
  //   //   return false;
  //   // }

  //   if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.mobileNo)) {
  //     this.alertMessage = "Please enter mobile number !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   } else if (!this.validationService.validateMobileNumber(employeeObj.mobileNo)) {
  //     this.alertMessage = "Please enter valid mobile number !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }

  //   if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.address)) {
  //     this.alertMessage = "Please enter Current Address !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }

  //   if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.state)) {
  //     this.alertMessage = "Please enter state !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   } else if (!this.validationService.validateAlphaWithSpace(employeeObj.state)) {
  //     this.alertMessage = "Please enter Valid state !!";
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }

  //   if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.city)) {
  //     this.alertMessage = "Please enter city !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   } else if (!this.validationService.validateAlphaWithSpace(employeeObj.city)) {
  //     this.alertMessage = "Please enter Valid city !!";
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }

  //   if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.country)) {
  //     this.alertMessage = "Please enter country !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   } else if (!this.validationService.validateAlphaWithSpace(employeeObj.country)) {
  //     this.alertMessage = "Please enter Valid country !!";
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }

  //   if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.pincode)) {
  //     this.alertMessage = "Please enter pincode !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   } else if (!this.validationService.validatePincodeNumber(employeeObj.pincode)) {
  //     this.alertMessage = "Please enter Valid pincode !!";
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }

  //   if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.alternateMobileNo)) {
  //     this.alertMessage = "Please enter alternate mobile number !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   } else if (!this.validationService.validateMobileNumber(employeeObj.alternateMobileNo)) {
  //     this.alertMessage = "Please enter valid alternate mobile number !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }

  //   if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.permanentAddress)) {
  //     this.alertMessage = "Please enter permanent address !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }

  //   if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.emergencyContactPerson)) {
  //     this.alertMessage = "Please enter Emergency Contact Person Name !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   } else if (!this.validationService.validateAlphaWithSpace(employeeObj.emergencyContactPerson)) {
  //     this.alertMessage = "Please enter Valid Emergency Contact Person Name !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }

  //   if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.relation)) {
  //     this.alertMessage = "Please enter Emergency Contact Person Relation !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   } else if (!this.validationService.validateAlphaWithSpace(employeeObj.relation)) {
  //     this.alertMessage = "Please enter Valid Emergency Contact Person Relation !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }

  //   if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.emergencyContactMobile)) {
  //     this.alertMessage = "Please enter Emergency Contact Person Mobile Number !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   } else if (!this.validationService.validateMobileNumber(employeeObj.emergencyContactMobile)) {
  //     this.alertMessage = "Please enter Valid Emergency Contact Person Mobile Number !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }

  //   if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.graduationType)) {
  //     this.alertMessage = "Please Select Graduation !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }

  //   if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.pursuing)) {
  //     this.alertMessage = "Please Select Pursuing !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }
  //   if (employeeObj.pursuing == 'No') {
  //     if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.yearOfPassing)) {
  //       this.alertMessage = "Please Enter Year of Passing !!"
  //       this.openAlertMod(template, this.alertMessage);
  //       return false;
  //     }
  //   }


  //   if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.passingGrade)) {
  //     this.alertMessage = "Please Enter Passing Grade !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }

  //   if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.experience)) {
  //     this.alertMessage = "Please Select Experience !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }

  //   if (employeeObj.experience == 'Experienced') {
  //     this.allPreviousEmployment.forEach((previousEmployer, index) => {
  //       if (!this.validationService.validateNullUndefinedEmptyString(previousEmployer.employerName)) {
  //         this.alertMessage = `Please Enter Employer Name - ${index}!!`;
  //         this.openAlertMod(template, this.alertMessage);
  //         return false;
  //       }

  //       if (!this.validationService.validateNullUndefinedEmptyString(previousEmployer.dateOfJoining)) {
  //         this.alertMessage = `Please Enter Date of Joining - ${index}!!`
  //         this.openAlertMod(template, this.alertMessage);
  //         return false;
  //       }

  //       if (!this.validationService.validateNullUndefinedEmptyString(previousEmployer.dateOfRelieving)) {
  //         this.alertMessage = `Please Enter Date of Relieving - ${index}!!`
  //         this.openAlertMod(template, this.alertMessage);
  //         return false;
  //       }

  //       if (!this.validationService.validateNullUndefinedEmptyString(previousEmployer.yearsOfExperience)) {
  //         this.alertMessage = `Please Enter Years of Experience - ${index}!!`
  //         this.openAlertMod(template, this.alertMessage);
  //         return false;
  //       }

  //       if (!this.validationService.validateNullUndefinedEmptyString(previousEmployer.managerName)) {
  //         this.alertMessage = `Please Enter Manager Name - ${index}!!`
  //         this.openAlertMod(template, this.alertMessage);
  //         return false;
  //       }

  //       if (!this.validationService.validateNullUndefinedEmptyString(previousEmployer.managerContactNumber)) {
  //         this.alertMessage = `Please Enter Manager Contact Number - ${index}!!`
  //         this.openAlertMod(template, this.alertMessage);
  //         return false;
  //       }

  //       if (!this.validationService.validateNullUndefinedEmptyString(previousEmployer.hrName)) {
  //         this.alertMessage = `Please Enter HR Name - ${index}!!`
  //         this.openAlertMod(template, this.alertMessage);
  //         return false;
  //       }

  //       if (!this.validationService.validateNullUndefinedEmptyString(previousEmployer.hrContactNumber)) {
  //         this.alertMessage = `Please Enter HR Contact Number - ${index}!!`
  //         this.openAlertMod(template, this.alertMessage);
  //         return false;
  //       }
  //     });
  //   }

  //   if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.bankName)) {
  //     this.alertMessage = "Please enter Bank Name !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   } if (!this.validationService.validateAlphaWithSpace(employeeObj.bankName)) {
  //     this.alertMessage = "Please enter Valid Bank Name !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }

  //   if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.bankAccountNo)) {
  //     this.alertMessage = "Please enter Bank Account Number !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   } if (!this.validationService.validateAlphaNumeric(employeeObj.bankAccountNo)) {
  //     this.alertMessage = "Please enter Valid Bank Account Number !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }

  //   if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.bankIFSCCode)) {
  //     this.alertMessage = "Please enter Bank IFSC Code !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   } if (!this.validationService.validateCapitalAlphaNumeric(employeeObj.bankIFSCCode)) {
  //     this.alertMessage = "Please enter Valid Bank IFSC Code !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }

  //   if (this.validationService.validateNullUndefinedEmptyString(employeeObj.pfAccountNumber) && !this.validationService.validateAlphaNumeric(employeeObj.pfAccountNumber)) {
  //     this.alertMessage = "Please enter Valid PF Account Number !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }

  //   if (this.validationService.validateNullUndefinedEmptyString(employeeObj.previousPfAccountNumber) && !this.validationService.validateAlphaNumeric(employeeObj.previousPfAccountNumber)) {
  //     this.alertMessage = "Please enter Valid Previous PF Account Number !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }
  //   if (this.validationService.validateNullUndefinedEmptyString(employeeObj.uan) && !this.validationService.validateAlphaNumeric(employeeObj.uan)) {
  //     this.alertMessage = "Please enter Valid UAN Number !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }
  //   if (this.validationService.validateNullUndefinedEmptyString(employeeObj.esicNumber) && !this.validationService.validateAlphaNumeric(employeeObj.esicNumber)) {
  //     this.alertMessage = "Please enter Valid ESIC Number !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }
  //   if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.totalExperience)) {
  //     this.alertMessage = "Please enter Total Experience !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   } else if (!this.validationService.validateNumber(employeeObj.totalExperience)) {
  //     this.alertMessage = "Please enter digit !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }

  //   return true;
  // }




  async onGetEmployeeInfo() {


    console.log("inner fuction");

    this.domainSpecializationList = [];
    this.currentEmployeeInfo = new Employee();
    let currentEmp = new Employee();

    currentEmp.empId = this.employeeData.empId;
    currentEmp.isDraft = false;
    //console.log("currentEmp : ", currentEmp);

    const response: any = await this.employeeService.getEmployeeByEmpId(currentEmp).toPromise();
    sessionStorage.setItem('empId', currentEmp.empId);
    sessionStorage.setItem('empIdA', response.serviceResponse.employeementId);
    sessionStorage.setItem('eId', response.serviceResponse.empId);
    if (response.serviceStatus == "Success") {
      this.currentEmployeeInfo = response.serviceResponse;
      this.employeeObj = response.serviceResponse;
      //console.log("currentEmployeeInfo : ", this.currentEmployeeInfo);
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
    domainObj.empId = this.employeeData.empId;
    const domainResponse: any = await this.domainService.getDomainSpecializationByEmpId(domainObj).toPromise();

    console.log("yessss", domainResponse)
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

  // onUpdateEmployeeInfo(template: TemplateRef<any>) {
  //   const dateFormat = 'YYYY-MM-DD';
  //   let inputValidated: boolean = this.validateEmployeeObj(this.UpdateEmployeeInfo, template)
  //   if (!inputValidated) return;

  //   // transform date formats to YYYY-MM-DD
  //   this.UpdateEmployeeInfo.dateOfBirth = moment(this.UpdateEmployeeInfo.dateOfBirth).format(dateFormat);
  //   this.UpdateEmployeeInfo.dateOfJoining = moment(this.UpdateEmployeeInfo.dateOfJoining).format(dateFormat);

  //   this.allCertificationList.forEach(certificaiton => {
  //     //console.log("All certificaiton : ", this.allCertificationList);
  //     certificaiton.dateOfCompletion = moment(certificaiton.dateOfCompletion).format(dateFormat);
  //     if ((certificaiton != undefined && Object.keys(certificaiton).length !== 0) && (certificaiton.employeeCertificateId == undefined || certificaiton.employeeCertificateId == null)) {
  //       //console.log("New certificaiton : ", certificaiton);
  //       this.updatedCertificationList.push(certificaiton);
  //     }
  //   });

  //   this.allPreviousEmployment.forEach(prevEmployer => {
  //     //console.log("All Prev Employer : ", this.allPreviousEmployment);
  //     if ((prevEmployer != undefined && Object.keys(prevEmployer).length !== 0) && (prevEmployer.previousEmploymentId == undefined || prevEmployer.previousEmploymentId == null)) {
  //       //console.log("New Prev Employer : ", prevEmployer);
  //       this.updatedPreviousEmployment.push(prevEmployer);
  //     }
  //   });

  //   this.UpdateEmployeeInfo.certifications = (Object.keys(this.allCertificationList[0]).length === 0) ? null : this.allCertificationList;
  //   this.UpdateEmployeeInfo.previousEmploymentList = (Object.keys(this.allPreviousEmployment[0]).length === 0) ? null : this.allPreviousEmployment;
  //   this.UpdateEmployeeInfo.updatedCertifications = (this.updatedCertificationList.length === 0) ? null : this.updatedCertificationList;
  //   this.UpdateEmployeeInfo.updatedPreviousEmploymentList = (this.updatedPreviousEmployment.length === 0) ? null : this.updatedPreviousEmployment;

  //   this.UpdateEmployeeInfo.updatedBy = this.employeeData.empId;;
  //   //console.log("Update Profile : ", this.UpdateEmployeeInfo);
  //   this.employeeService.updateEmployeeProfile(this.UpdateEmployeeInfo).pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus == "Success") {
  //       this.openAlertMod(template, response.serviceResponse);
  //       this.showViewProfile();
  //     } else {
  //       this.openAlertMod(template, response.serviceResponse);
  //     }
  //   });
  // }


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

  getMyAssetList() {

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
    this.modalRef = this.modalService.show(template, { class: 'modal-xl', backdrop: 'static', keyboard: false });
  }

  onDocSubmit() {
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
















  showUpdateForm(employee: any) {
    this.isForm = true;
    this.referedTypeStatus = true;
    this.isTable = false;
    this.isUpdation = true;
    this.isDraft = false;
   console.log("bshvhsfvbjhsdbvh",employee);
      this.getManagerList(employee);
     this.getAllDepartmentList();
     this.getAllDomain();
    this.allCertificationList = [];
    this.allPreviousEmployment = [];
    this.updatedCertificationList = [];
    this.updatedPreviousEmployment = [];

    // employee.employeementId = this.utilityService.substringEmployeementid(employee.isConsultant,employee.employeementId);
    employee.employeementId = this.employeeData.employeementId?.substring(2);

    this.employeeService.getEmployeeByEmpId(employee).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {

        this.employeeObj = Object.assign({}, response.serviceResponse);

        if (this.employeeObj.domainList != null) {
          this.getDomainSpecialization();
        }
        // employee.employeementId = this.utilityService.appendEmployeementid(this.employeeObj.employeementId)

        // if (this.employeeObj.isConsultant == 'true'){
        //   this.employeeObj.employeementId = "A-CS-".concat(this.employeeObj.employeementId);
        // }else{
        //   this.employeeObj.employeementId = "A-".concat(this.employeeObj.employeementId);
        // }
        // this.employeeObj.employeementId = "A-".concat(this.employeeObj.employeementId);
        if (this.employeeObj.isConsultant == 'true')
          this.employeeObj.employeeType = 'Consultant';
        else if (this.employeeObj.isApprenticeship == 'true')
          this.employeeObj.employeeType = 'Apprentice';
        else if (this.employeeObj.isApmosysProduct == 'true')
          this.employeeObj.employeeType = 'Apmosys Product'
        else
          this.employeeObj.employeeType = 'On roll';

        console.log("employee :", this.employeeObj);
        // employee.employeementId = this.utilityService.appendEmployeementid(employee.employeementId);
        // Job Role
        if (this.employeeObj.departmentId) {
          this.getJobRolesByDept(this.employeeObj.departmentId, this.employeeObj.jobRoleId);
          this.getDesignationByDeptId(this.employeeObj.departmentId);
        }
      } else {
        console.error(response.serviceResponse)
      }
    });

    setTimeout(this.setCalenderMaxDate, 1000);
  }

  onEmployeeTypeChange(selectedType: string): void {
    switch (selectedType) {
      case 'Regular':
        this.employeeObj.isConsultant = 'false';
        this.employeeObj.isApprenticeship = 'false';
        break;
      case 'Consultant':
        this.employeeObj.isConsultant = 'true';
        this.employeeObj.isApprenticeship = 'false';
        break;
      case 'Apprentice':
        this.employeeObj.isConsultant = 'false';
        this.employeeObj.isApprenticeship = 'true';
        break;
      default:
        this.employeeObj.isConsultant = null;
        this.employeeObj.isApprenticeship = null;
    }
  }



  fieldRestictCharacter(event) {
    var k;
    k = event.charCode;
    if ((k == 33) || (k == 34) || (k == 35) || (k == 36) || (k == 37) ||
      (k == 38) || (k == 39) || (k == 40) || (k == 41) || (k == 42) ||
      (k == 43) || (k == 44) || (k == 46) || (k == 47) || (k == 58) ||
      (k == 59) || (k == 60) || (k == 61) || (k == 62) || (k == 63) ||
      (k == 64) || (k == 66) || (k == 67) || (k == 68) || (k == 69) ||
      (k == 70) || (k == 71) || (k == 72) || (k == 73) || (k == 74) ||
      (k == 75) || (k == 76) || (k == 77) || (k == 78) || (k == 79) ||
      (k == 80) || (k == 81) || (k == 82) || (k == 83) || (k == 84) ||
      (k == 85) || (k == 86) || (k == 87) || (k == 88) || (k == 89) ||
      (k == 90) || (k == 91) || (k == 92) || (k == 93) || (k == 94) ||
      (k == 95) || (k == 96) || (k == 97) || (k == 98) || (k == 99) ||
      (k == 99) || (k == 100) || (k == 101) || (k == 102) || (k == 103) ||
      (k == 104) || (k == 105) || (k == 106) || (k == 107) || (k == 108) ||
      (k == 109) || (k == 110) || (k == 111) || (k == 112) || (k == 113) ||
      (k == 114) || (k == 115) || (k == 116) || (k == 117) || (k == 118) ||
      (k == 119) || (k == 120) || (k == 121) || (k == 122) || (k == 123) ||
      (k == 124) || (k == 125) || (k == 126)) {
      return (false);
    }
    return (true);

  }



  checkEmployeementId(template: TemplateRef<any>) {
    let employee = new Employee();
    employee.empId = this.employeeObj.empId;
    employee.email = this.employeeObj.email;

    console.log("employeeid with space", this.employeeObj.employeementId);

    if (this.employeeObj.employeementId.startsWith('A-')) {
      this.employeeObj.employeementId = this.employeeObj.employeementId.substring(2);
      if (!this.validationService.validateNullUndefinedEmptyString(this.employeeObj.employeementId)) {
        this.alertMessage = "Please enter Employee ID !!"
        this.openAlertMod(template, this.alertMessage);
        // this.employeeObj.employeementId = 'A-'+this.employeeObj.employeementId;
        return false;
      }
      // employee.employeementId  = this.employeeObj.employeementId.substring(2);
      console.log("Employee :", this.employeeObj);
    } else if (this.employeeObj.employeementId.startsWith('A-')) {
      if (!this.validationService.validateNullUndefinedEmptyString(this.employeeObj.employeementId)) {
        this.alertMessage = "Please enter Employee ID !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      employee.employeementId = this.employeeObj.employeementId.substring(2);
      console.log("Employee :", this.employeeObj);
    }
    else {
      employee.employeementId = this.employeeObj.employeementId
      if (!this.validationService.validateNullUndefinedEmptyString(employee.employeementId)) {
        this.alertMessage = "Please enter Employment ID !!";
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (!this.validationService.validateEmployeementId(employee.employeementId)) {
        console.log("employeementid please enter valid employmentid", employee.employeementId, this.employeeObj.employeementId);
        this.alertMessage = "Please enter valid Employment ID !!";
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }
    this.employeeService.checkEmployeementId(employee).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Fail") {
        this.openAlertMod(template, response.serviceResponse);
        employee.employeementId = '';
      }
      this.employeeObj.employeementId = 'A-' + this.employeeObj.employeementId;
      console.log("checkEmployeementId response: ", response);
    });
  }

   checkEmployeementIdWithDifferentPrefix(template: TemplateRef<any>) {
    let employee = new Employee();
    employee.empId = this.employeeObj.empId;
    employee.email = this.employeeObj.email;
  
    const prefix = this.getEmpIdPrefix(this.employeeObj.employeeType);
    const enteredId = this.employeeObj.employeementId;
  
    if (!this.validationService.validateNullUndefinedEmptyString(enteredId)) {
      this.alertMessage = "Please enter Employment ID !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
  
    if (!this.validationService.validateEmployeementId(enteredId)) {
      this.alertMessage = "Please enter valid Employment ID !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
  
    // Final ID with prefix
    employee.employeementId = enteredId;
    employee.employeeType = this.employeeObj.employeeType;
  
    this.employeeService.checkEmployeementId(employee).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Fail") {
        this.openAlertMod(template, response.serviceResponse);
        employee.employeementId = '';
         this.employeeObj.employeementId = '';
      }
      console.log("checkEmployeementId response: ", response);
    });
  }



  ValidateName(template: TemplateRef<any>) {
    this.employeeObj.name = this.employeeObj.name?.trim();
    if (!this.validationService.validateNullUndefinedEmptyString(this.employeeObj.name)) {
      this.alertMessage = "Please enter Full Name !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateAlphaWithSpace(this.employeeObj.name)) {
      this.alertMessage = "Please enter Valid Full Name !!";
      this.openAlertMod(template, this.alertMessage);
      this.employeeObj.name = '';
      return false;
    }
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





  checkEmail(template: TemplateRef<any>) {

    let employee = new Employee();
    // employee.employeementId = this.utilityService.getEmployeeIdSubstring(this.employeeObj);
    employee.employeementId = this.employeeObj.employeementId.substring(2);
    console.error("employee.employeementId ", employee.employeementId);
    employee.email = this.employeeObj.email;
    employee.empId = this.employeeObj.empId;
    console.log("checkEmail() triggered with email:", employee.email);
    if (!this.validationService.validateNullUndefinedEmptyString(employee.email)) {
      this.alertMessage = "Please Enter Email ID !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateApmosysEmail(this.employeeObj.email,this.employeeObj.employeeType)) {
      this.alertMessage = "Please Enter Valid Email ID !!"
      this.openAlertMod(template, this.alertMessage);
      this.employeeObj.email = '';
      return false;
    }
    this.employeeService.checkEmployeeEmail(employee).pipe(first()).subscribe((response: any) => {
      console.log("EMAIL-response.serviceResponse: ", response.serviceResponse);
      if (response.serviceStatus == "Fail") {
        this.openAlertMod(template, response.serviceResponse);
        this.employeeObj.email = '';
      }
    });
  }






  checkSecondaryEmail(template: TemplateRef<any>) {

    const regex = /^(?:[0-9]+[a-z_.]|[a-z_.])[a-z0-9_.]+@apmosys\.com$/i;
    // const regex = /^[A-Za-z0-9._%+-]+@apmosys\.com$/;
    if (this.validationService.validateNullUndefinedEmptyString(this.employeeObj.secondaryEmail)) {
      if (regex.test(this.employeeObj.secondaryEmail)) {

        this.openAlertMod(template, "Apmosys mail Id is not valid in secondary mail !!");
        this.employeeObj.secondaryEmail = '';
      }
    }
  }



  disableMannualDateInput() {
    return false;
  }



  validateBirthDate(template: TemplateRef<any>) {
    let birthdate = new Date(this.employeeObj.dateOfBirth);
    let dtCurrent = new Date();
    let flag = true;
    let dobInput: any = document.getElementById('DOB');

    if (dtCurrent.getFullYear() - birthdate.getFullYear() < 18) {
      this.openAlertMod(template, 'Employee age cannot be less than 18 years.');
      flag = false;
    }
    else if (dtCurrent.getFullYear() - birthdate.getFullYear() == 18) {

      //CD: 11/06/2018 and DB: 15/07/2000. Will turned 18 on 15/07/2018.
      if (dtCurrent.getMonth() < birthdate.getMonth()) {
        this.openAlertMod(template, 'Employee age cannot be less than 18 years.');
        flag = false;
      }

      if (dtCurrent.getMonth() == birthdate.getMonth()) {
        //CD: 11/06/2018 and DB: 15/06/2000. Will turned 18 on 15/06/2018.
        if (dtCurrent.getDate() < birthdate.getDate()) {
          this.openAlertMod(template, 'Employee age cannot be less than 18 years.');
          flag = false;
        }
      }
    }
    if (!flag) {
      setTimeout(() => {
        dobInput.value = '';
        this.employeeObj.dateOfBirth = '';
      }, 10)
    }
  }



  fieldRestictCharacterForNumber(event) {
    var k;
    k = event.charCode;
    if ((k == 32) || (k == 33) || (k == 34) || (k == 35) || (k == 36) || (k == 37) || (k == 38) || (k == 39) || (k == 40) || (k == 41) || (k == 42) || (k == 43) ||
      (k == 44) || (k == 45) || (k == 46) || (k == 47) || (k == 97) || (k == 98) || (k == 99) ||
      (k == 99) || (k == 100) || (k == 101) || (k == 102) || (k == 103) ||
      (k == 104) || (k == 105) || (k == 106) || (k == 107) || (k == 108) ||
      (k == 109) || (k == 110) || (k == 111) || (k == 112) || (k == 113) ||
      (k == 114) || (k == 115) || (k == 116) || (k == 117) || (k == 118) ||
      (k == 119) || (k == 120) || (k == 121) || (k == 122) || (k == 46) || (k == 65) || (k == 66) || (k == 67) || (k == 68) || (k == 69) ||
      (k == 70) || (k == 71) || (k == 72) || (k == 73) || (k == 74) ||
      (k == 75) || (k == 76) || (k == 77) || (k == 78) || (k == 79) ||
      (k == 80) || (k == 81) || (k == 82) || (k == 83) || (k == 84) ||
      (k == 85) || (k == 86) || (k == 87) || (k == 88) || (k == 89) ||
      (k == 90)) {
      return (false);
    }
    return true;
  }



  checkEmployeeMobileNo(template: TemplateRef<any>) {
    let employee = new Employee();
    employee.employeementId = this.utilityService.getEmployeeIdSubstring(this.employeeObj.isConsultant);
    employee.mobileNo = this.employeeObj.mobileNo;

    if (!this.validationService.validateMobileNumber(employee.mobileNo)) {
      this.alertMessage = "Enter Valid Mobile Number !!";
      this.openAlertMod(template, this.alertMessage);
      this.employeeObj.mobileNo = '';
    }

    else if (!this.validationService.validateNullUndefinedEmptyString(employee.mobileNo)) {
      this.alertMessage = "Please enter Mobile Number !!";
      this.openAlertMod(template, this.alertMessage);
      this.employeeObj.mobileNo = '';
    }

    else {

      this.employeeService.checkEmployeeMobileNo(employee).pipe(first()).subscribe((response: any) => {
        console.log("MOB No- response.serviceResponse: ", response.serviceResponse);
        if (response.serviceStatus == "Fail") {
          this.openAlertMod(template, response.serviceResponse);
          this.employeeObj.mobileNo = '';
        }
      });
    }
  }



  refferedChange() {
    if (this.employeeObj.referedType == "InOffice") {
      this.referedTypeStatus = true;
    }
    if (this.employeeObj.referedType == "OutOffice") {
      this.referedTypeStatus = true;
    }
  }


  onUpadateReportees(event: any) {
    console.log('data printed ----', event.target.value);
    if (event.target.value == 'Yes') {
      this.employeeObj.updateType = '';
      this.onselectYes = true;
    } else if (event.target.value == 'No') {
      this.onselectYes = false;
      this.employeeObj.updateType = '';
    } else {
      console.log('data printed ----', event.target.value);
      this.onselectYes = false;
      this.employeeObj.updateType = '';
    }
  }


  openModalForManagerChange(template: TemplateRef<any>, event: Event, employeeId: string) {
    this.filters = {};
    const eventValue = (event.target as HTMLSelectElement).value;
    // Open modal only if the updateType is 'manual'
    if (this.employeeObj.updateType === 'manual') {
      this.employeeService.getTotalNoOfreporties(employeeId).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus === 'Success') {
          this.listOfReporties = response.serviceResponse;
        }
      });
      this.isSearchEnabled = false;
      this.modalRef = this.modalService.show(template, { class: 'modal-sm', backdrop: 'static', keyboard: false });
    }
  }

  retainStatus() {

    if (this.employeeObj.employmentstatus == "Retain") {
      this.employeeObj.isRetain = "Yes";
    } else {
      this.employeeObj.isRetain = "No";
    }


    console.log(this.employeeObj.employmentstatus)
    console.log(this.employeeObj.isRetain)
  }

  resetField(updateType) {
    // updateType.employmentReleaseStatus = "";
    updateType.newManagerId = "";
    this.employeeObj.newManagerId = '';
  }

  omit_character(event) {
    var k;
    k = event.charCode;
    if ((k == 45) || (k == 69) || (k == 101)) {
      return (false);
    }
    return (true);
  }

  estimateDateOfReleiving(employeeObj: Employee) {
    const dateFormat = 'YYYY-MM-DD';

    if (this.employeeObj.dateOfResign) {
      let estimateDateOfRelieving = new Date(this.employeeObj.dateOfResign);
      this.employeeObj.dateOfRelieving = moment(estimateDateOfRelieving).add(this.employeeObj.noticePeriod, "days").format(dateFormat);
      console.log(this.employeeObj.dateOfRelieving, "this.employeeObj.dateOfRelieving")
    }
  }

  DateFilterForDOR = (d: Date) => {
    const dateFormat = 'YYYY-MM-DD';
    const currentDate = new Date();
    let dateOfJoining = this.employeeObj.dateOfJoining != (null || undefined) ? this.employeeObj.dateOfJoining : new Date();
    return (moment(d).format(dateFormat) >= moment(dateOfJoining).format(dateFormat) && moment(d).format(dateFormat) <= moment(currentDate).format(dateFormat));
  }

  estimateDateOfRetain(employeeObj: Employee) {
    const dateFormat = 'YYYY-MM-DD';

    if (this.employeeObj.dateOfRetain) {
      let estimateDateOfRetain = new Date(this.employeeObj.dateOfRetain);
      this.employeeObj.dateOfRetain = moment(estimateDateOfRetain).format(dateFormat);
      console.log(this.employeeObj.dateOfRetain, "this.employeeObj.dateOfRetain")
    }

    console.log(this.employeeObj.dateOfRetain);
  }



  DateFilterForDofRetain = (d: Date) => {
    const dateFormat = 'YYYY-MM-DD';
    const currentDate = new Date();

    // Use default date values in case the date fields are not defined
    const resignDate = this.employeeObj.dateOfResign ? this.employeeObj.dateOfResign : new Date();
    const relievingDate = this.employeeObj.dateOfRelieving ? this.employeeObj.dateOfRelieving : currentDate;

    return (
      moment(d).format(dateFormat) >= moment(resignDate).format(dateFormat) &&
      moment(d).format(dateFormat) <= moment(relievingDate).format(dateFormat)
    );
  };


  updateNoticePeriod(employeeObj: Employee) {
    const dateFormat = 'YYYY-MM-DD';
    console.log(employeeObj, "employeeObj");
    this.employeeObj.dateOfRelieving = moment(this.employeeObj.dateOfRelieving).format(dateFormat);
    console.log(this.employeeObj.dateOfRelieving);
    const diff = Math.abs(Math.floor((new Date(this.employeeObj.dateOfRelieving).getTime() - new Date(employeeObj.dateOfResign).getTime()) / (1000 * 60 * 60 * 24)));
    this.employeeObj.noticePeriod = diff;
    console.log(diff, "diffDaysdiffDays")
  }




  relievingDateFilter = (d: Date) => {
    const dateFormat = 'YYYY-MM-DD';
    const currentDate = new Date();

    let resignDate = this.employeeObj.dateOfResign;

    if (resignDate) {
      return (moment(d).format(dateFormat) >= moment(resignDate).format(dateFormat));
    } else {
      return false;
    }
  }


  getJobRolesByDept(departmentId: any, jobRoleId?: any) {
    this.filteredJobRoleList = [];
    this.filteredJobRoleList = this.allJobRoleList.filter(jobRole => jobRole.departmentId == departmentId);
    jobRoleId ? this.employeeObj.jobRoleId = jobRoleId : this.employeeObj.jobRoleId = '';
  }

  getAllJobRoleList() {
    this.allJobRoleList = [];

    this.jobRoleService.getAllJobRole().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allJobRoleList = response.serviceResponse;
        console.log("allJobRoleList : ", this.allJobRoleList);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }


  getDesignationByDeptId(departmentId: any) {
    let designationObj = new Designation();
    designationObj.deptId = departmentId;

    this.destinationService.getDesignationByDeptId(designationObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDesignationList = response.serviceResponse;
        console.log(this.allDesignationList, " : allDesignationList");
      } else {
        console.error(response.serviceResponse)
      }
    });
  }


  isDesignationDisabled() {
    return !this.employeeObj.departmentId || this.employeeObj.designationId;
  }

  clearAfterChange(field, fieldname) {
    let element: any = document.getElementById(field);
    if (element) element.value = '';

    console.log("value : ", element.value);

    this.employeeObj[fieldname] = '';
  }

  updateDesignationDropdown() {
    this.employeeObj.designationId = ''; // Set designation to 'Select' option
  }

  allStates: any[] = [
    "Andaman & Nicobar Islands",
    "Andhra Pradesh",
    "Arunachal Pradesh",
    "Assam",
    "Bihar",
    "Chandigarh",
    "Chhattisgarh",
    "Dadra and Nagar Haveli and  Daman & Diu",
    "Delhi",
    "Goa",
    "Gujarat",
    "Haryana",
    "Himachal Pradesh",
    "Jammu & Kashmir",
    "Jharkhand",
    "Karnataka",
    "Kerala",
    "Ladakh",
    "Lakshadweep",
    "Madhya Pradesh",
    "Maharashtra",
    "Manipur",
    "Meghalaya",
    "Mizoram",
    "Nagaland",
    "Odisha",
    "Puducherry",
    "Punjab",
    "Rajasthan",
    "Sikkim",
    "Tamil Nadu",
    "Telangana",
    "Tripura",
    "Uttar Pradesh",
    "Uttarakhand",
    "West Bengal",
  ];

  getCurrentFormattedDate(): string {
    const now = new Date(); // Get the current date and time

    // Manually format the date
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0'); // Months are 0-based
    const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    const seconds = String(now.getSeconds()).padStart(2, '0');

    // Format the date as a string
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
  }


  billableBenchDate: any

  onBillablechange() {

    if (this.employeeObj.billableType == 'Bench') {

      this.billableBenchDate = this.getCurrentFormattedDate();

    } else {
      this.billableBenchDate = "No";
    }

  }

  getDomainSpecialization() {
    this.specializationList = [];

    let domainObj = new Domain();
    domainObj.domainIdList = this.employeeObj.domainList;

    console.log(domainObj, " : domainObj selected");
    this.domainService.getDomainSpecialization(domainObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.specializationList = response.serviceResponse;
        this.specializationList = this.specializationList.sort((a, b) => a.specializationName.localeCompare(b.specializationName));
      } else {
        console.error(response.serviceResponse);
      }
    });
  }


  validateEmployeeObj(employeeObj: Employee, template: TemplateRef<any>) {

   
    employeeObj.name = this.employeeObj.name?.trim();
    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.name)) {
      this.alertMessage = "Please enter Full Name !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateAlphaWithSpace(employeeObj.name)) {
      this.alertMessage = "Please enter Valid Full Name !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.email)) {
      this.alertMessage = "Please enter email id !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateEmail(employeeObj.email)) {
      this.alertMessage = "Please enter valid email id !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.secondaryEmail)) {
      this.alertMessage = "Please enter Secondary email id !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateEmail(employeeObj.secondaryEmail)) {
      this.alertMessage = "Please enter valid Secondary email id !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

 

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.dateOfBirth)) {
      this.alertMessage = "Please enter date of birth !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

  

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.mobileNo)) {
      this.alertMessage = "Please enter mobile number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateMobileNumber(employeeObj.mobileNo)) {
      this.alertMessage = "Please enter valid mobile number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

 

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.dateOfJoining)) {
      this.alertMessage = "Please enter date of joining !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.reportiesFlag) && this.isUpdation) {
      this.alertMessage = "Please enter 'Do you want to change the reporting of your reportees ?' "
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(this.employeeObj.billable)) {
      this.alertMessage = "Please select a value for 'Billable'";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (this.employeeObj.billable === 'No' &&
      !this.validationService.validateNullUndefinedEmptyString(this.employeeObj.billableType)) {
      this.alertMessage = "Please select a value for 'Billable Type' when Billable is set to 'No'";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (this.employeeObj.billable === 'Yes' &&
      !this.validationService.validateNullUndefinedEmptyString(this.employeeObj.billableType)) {
      this.alertMessage = "Please select a value for 'Billable Type' when Billable is set to 'Yes'";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.employmentstatus)) {
      this.alertMessage = "Please enter employment status !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.probationPeriod)) {
      this.alertMessage = "Please enter Probation period !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } if (employeeObj.probationPeriod > 365 || employeeObj.probationPeriod < 0) {
      this.alertMessage = "Please enter value 0 to 365 in probation period field !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } if (!this.validationService.validateNumber(employeeObj.probationPeriod)) {
      this.alertMessage = "Please enter valid probation period !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.noticePeriod)) {
      this.alertMessage = "Please enter notice period !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } if (employeeObj.noticePeriod > 365 || employeeObj.noticePeriod < 0) {
      this.alertMessage = "Please enter value 0 to 365 in notice period field !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } if (!this.validationService.validateNoticePeriod(employeeObj.noticePeriod)) {
      this.alertMessage = "Please enter valid notice period !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (employeeObj.employmentstatus == "Resigned") {
      if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.dateOfResign)) {
        this.alertMessage = "Please enter date of Resign !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    } else if (employeeObj.employmentstatus == "InActive") {
      if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.dateOfResign)) {
        this.alertMessage = "Please enter date of Resign !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.dateOfRelieving)) {
        this.alertMessage = "Please enter date of Relieving !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.managerId)) {
      this.alertMessage = "Please select manager !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (employeeObj.reportingManagerId) {
      if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.approvalsTo)) {
        this.alertMessage = "Please select Approvals To !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.departmentId)) {
      this.alertMessage = "Please select Department !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.designationId)) {
      this.alertMessage = "Please select Designation !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.jobRoleId)) {
      this.alertMessage = "Please select Job Role !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.experience)) {
      this.alertMessage = "Please select experience !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if (employeeObj.experience == 'Experienced') {
      if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.totalExperience)) {
        this.alertMessage = "Please enter total experience !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      if (employeeObj.totalExperience === 0) {
        this.alertMessage = "Please enter more than 0 number !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      if (!this.validationService.validateExperiencedNumber(employeeObj.totalExperience)) {
        this.alertMessage = "Please enter valid experience in Format (Years.Months)  !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      } if (employeeObj.totalExperience > 60) {
        this.alertMessage = "Please enter value 1 to 60(yrs) in total experience field !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.workLocation)) {
      this.alertMessage = "Please select employee Work Location !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.billable)) {
      this.alertMessage = "Please select billable !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if (employeeObj.employmentstatus == 'Resigned') {
      if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.dateOfResign)) {
        this.alertMessage = "Please Enter Resign Date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

   
    return true;
  }
  onUpdateEmployee(template: TemplateRef<any>) {
    const dateFormat = 'YYYY-MM-DD';
    let inputValidated: boolean = this.validateEmployeeObj(this.employeeObj, template)
    if (!inputValidated) return;
    this.employeeObj.imageBytes = null;
    this.employeeObj.isDraft = false;
    // transform date formats to YYYY-MM-DD
    if (this.employeeObj.dateOfBirth) this.employeeObj.dateOfBirth = moment(this.employeeObj.dateOfBirth).format(dateFormat)
    if (this.employeeObj.dateOfJoining) this.employeeObj.dateOfJoining = moment(this.employeeObj.dateOfJoining).format(dateFormat)
    if (this.employeeObj.employeeConfirmationDate) this.employeeObj.employeeConfirmationDate = moment(this.employeeObj.employeeConfirmationDate).format(dateFormat)
    if (this.employeeObj.dateOfResign) this.employeeObj.dateOfResign = moment(this.employeeObj.dateOfResign).format(dateFormat)
    if (this.employeeObj.dateOfRetain) this.employeeObj.dateOfRetain = moment(this.employeeObj.dateOfRetain).format(dateFormat)

    if (this.employeeObj.employmentstatus == "Confirmed" || this.employeeObj.employmentstatus == "Probation") {
      this.employeeObj.dateOfResign = null;
      this.employeeObj.dateOfRelieving = null;
    }

    this.employeeObj.updatedBy = this.currentUser.empId;;
    console.log("Update Employe : ", this.employeeObj);

    let employee = Object.assign({}, this.employeeObj);
    employee.updatedBy = this.currentUser.empId;

    // if(this.employeeObj.employeementId.startsWith('A-CS-')){
    //   employee.employeementId  = this.employeeObj.employeementId.substring(5);
    //   console.log("Employee :", this.employeeObj);
    // }else if(this.employeeObj.employeementId.startsWith('A-')){
    //   employee.employeementId  = this.employeeObj.employeementId.substring(2);
    //   console.log("Employee :", this.employeeObj);
    // }else {
    //   employee.employeementId  = this.employeeObj.employeementId
    // }

    // if (this.employeeObj.employeementId.startsWith('A-')) {
    //   employee.employeementId = this.employeeObj.employeementId.substring(2);
    //   console.log("Employee :", this.employeeObj);
    // } else {
    //   employee.employeementId = this.employeeObj.employeementId
    // }

    employee.specializationList = this.employeeObj.specializationList;
    if (this.employeeObj.reportingManagerId == null || this.employeeObj.reportingManagerId == "") {
      employee.reportingManagerId = null;
      employee.approvalsTo = null;
    }

    if (this.employeeObj.reportingManagerId == null || this.employeeObj.reportingManagerId == "") {
      employee.reportingManagerId = null;
      employee.approvalsTo = null;
    }
    employee.newManagerId = this.employeeObj.newManagerId;
    employee.reportiesFlag = this.employeeObj.reportiesFlag;
    employee.referedType == this.employeeObj.referedType;
    employee.referedName == this.employeeObj.referedName;

    console.log("employee update before call ", employee);

    if (this.employeeObj.employeeType === 'Consultant') {
      employee.isConsultant = 'true';
      employee.isApprenticeship = 'false';
       employee.isApmosysProduct = 'false';
    } else if (this.employeeObj.employeeType === 'Apprentice') {
      employee.isConsultant = 'false';
      employee.isApprenticeship = 'true';
       employee.isApmosysProduct = 'false';
    }else if (this.employeeObj.employeeType === 'Apmosys Product') {
      employee.isConsultant = 'false';
      employee.isApprenticeship = 'false';
       employee.isApmosysProduct = 'true';
    } else {
      employee.isConsultant = 'false';
      employee.isApprenticeship = 'false';
        employee.isApmosysProduct = 'false';
    }

    employee.onbenchDate = this.billableBenchDate;

    this.employeeService.updateEmployee(employee).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
         this.showViewProfile();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  listOfDepartment: any[] = [];
  reporteeList2: any[] = [];
  reporteeList: any[] = [];
  managerAndAbove: any = [];
  newEmp = new Employee();
  getManagersList() {
    // this.managerId = "";
    // this.managerAndAbove = [];
    this.managerAndAbove = this.managerAndAbove.forEach(t => t.managerId == "");
    //console.log(" managers call ");
    this.employeeService.getManagerList().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.managerAndAbove = response.serviceResponse
        this.managerAndAbove = this.managerAndAbove.filter(empId => empId.managerId != this.employeeObj.empId);
        //console.log(" managersAndAbove list   ",this.managerAndAbove);
      }
    });
  }
  getReporteesListByManagerId() {
    console.log(" empId in manager UI change ", this.employeeObj.name);

    if (this.employeeObj.employeementId.startsWith("A-")) {
      this.employeeObj.employeementId = this.employeeObj.employeementId.substring(2);
    }

    console.log("employment id", this.employeeObj.employeementId);
    this.employeeService.getReporteesListByManagerId(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.reporteeList = response.serviceResponse;
        this.getManagersList();
        // this.employeeObj.managerId = '';
        //console.log(" teamMember list   ",this.TeamMemberList)
      }
    })
  }


  getReporteesListByReportingManagerId() {
    console.log(" empId in manager UI change ", this.employeeObj.name);

    if (this.employeeObj.employeementId.startsWith("A-")) {
      this.employeeObj.employeementId = this.employeeObj.employeementId.substring(2);
    }

    console.log("employment id", this.employeeObj.employeementId);
    this.employeeService.getReporteesListByReportingManagerId(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.reporteeList2 = response.serviceResponse;
        this.getManagersList();
        // this.employeeObj.managerId = '';
        //console.log(" teamMember list   ",this.TeamMemberList)
      }
    })
  }

 getManagersList1() {
    // this.managerId = "";
    // this.managerAndAbove = [];
    this.managerAndAbove = this.managerAndAbove.forEach(t => t.managerId == "");
    //console.log(" managers call ");
    this.employeeService.getManagerList().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.managerAndAbove = response.serviceResponse
        this.managerAndAbove = this.managerAndAbove.filter(empId => empId.managerId != this.employeeObj.empId);
        //console.log(" managersAndAbove list   ",this.managerAndAbove);
      }
    });
  }
  openManagerDetailsModal(template: TemplateRef<any>, employeeId) {
    this.filters = {};
    this.listOfDepartment = [];
    this.isSearchEnabled = false;
    this.employeeObj.departmentId = "";
    this.employeeObj.projectName = "";
    this.employeeObj.teamName = "";
    console.log(" empId in manager UI change ", this.employeeObj.name);

    this.getReporteesListByManagerId();
    this.getReporteesListByReportingManagerId();

    this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
  }
  reportingManagerUpdate(reportee, template: TemplateRef<any>) {
    this.newEmp = reportee;
    console.log("newEmployee", this.newEmp.reportingManagerId);

    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
  }



  updateEmployeesReportingManager(template: TemplateRef<any>) {

    let emp = new Employee();
    emp.empId = this.newEmp.empId;
    emp.reportingManagerId = this.newEmp.reportingManagerId

    this.employeeService.setReportingManagerToNewManager(emp).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        //console.log(" teamName after manager changes done ",this.employeeObj.teamName)
        this.getReporteesListByManagerId();
        this.getReporteesListByReportingManagerId();
        this.openAlertMod(template, " Employee's Reporting Manager has changed !!");
        //console.log(" Manager update ")
      }
    })

    this.modalRef.hide();
  }


  newEmployee = new Employee();

  managerUpdate(reportee, template: TemplateRef<any>) {
    this.newEmployee = reportee;
    console.log("newEmployee", this.newEmployee.managerId);

    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
  }


  updateEmployeesManager(template: TemplateRef<any>) {

    let emp = new Employee();
    emp.empId = this.newEmployee.empId;
    emp.managerId = this.newEmployee.managerId

    this.employeeService.setManagerToNewManager(emp).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        //console.log(" teamName after manager changes done ",this.employeeObj.teamName)
        this.getReporteesListByManagerId();
        this.getReporteesListByReportingManagerId();
        this.openAlertMod(template, " Employee's Manager has changed !!");
        //console.log(" Manager update ")
      }
    })

    this.modalRef.hide();

    //console.log(" managerUpdate method call and employee id of reporties    :   ",employee.empId);

    //console.log(" managerUpdate method call  employee name  :   ",employee.name);
    //console.log(" managerId   ::   ",employee.managerId);
  }

  getManagerList(employee?: Employee) {
    this.managerList = [];
    let employeeList = [];

    console.log("Skip manager : ", employee)
    this.employeeObj.role = "Manager";
    // if(this.employeeObj.isConsultant == 'true'){
    //   this.employeeObj.employeementId = this.employeeObj.employeementId?.substring(5);
    // }else{
    //   this.employeeObj.employeementId = this.employeeObj.employeementId?.substring(2);
    // }

    // employee.employeementId = this.employeeData.employeementId?.substring(2);
    this.employeeObj.employeementId = this.employeeData.employeementId?.substring(2);
    this.employeeService.getAllEmployeesByRole(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        employeeList = response.serviceResponse;

        console.log("employeeList By Role : ", employeeList)
        if (this.isUpdation) {
          this.managerList = employeeList.filter((manager: Employee) => manager.empId !== employee.empId);
        } else {
          this.managerList = employeeList;
        }
        console.log("managerList : ", this.managerList)
      } else {
        console.error(response.serviceResponse)
      }
    });
  }


  getAllDepartmentList() {
    this.allDeptList = [];

    this.departmentService.getAllDepartments().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDeptList = response.serviceResponse;
        console.log("allDeptList : ", this.allDeptList);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }


  getAllDomain(template?: TemplateRef<any>) {
    this.domainService.getAllDomain().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDomainList = response.serviceResponse;

        this.allDomainList.forEach((domain) => {
          domain.createdOn = (domain.createdOn) ? moment(domain.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        });

        console.log(this.allDomainList, " : this.allDomainList");
      } else {
        // this.openAlertMod(template, response.serviceResponse);
        console.error(response.serviceResponse);
      }
    });
  }
  
}
