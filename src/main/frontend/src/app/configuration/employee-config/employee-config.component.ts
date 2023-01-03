import { Component, OnDestroy, OnInit, Pipe, SecurityContext, TemplateRef, ViewChild } from '@angular/core';
import { Employee } from 'src/app/models/employee';
import { EmployeeService } from 'src/app/services/employee.service';
import { ValidationService } from 'src/app/services/validation.service';
import { first } from 'rxjs/operators';
import { DatePipe, LocationStrategy } from '@angular/common';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { User } from 'src/app/models/user';
import { Feature } from 'src/app/models/feature';
import { JobRoleService } from 'src/app/services/job-role.service';
import { certification } from 'src/app/models/certification';
import { PreviousEmployer } from 'src/app/models/previousEmployer';
import { DepartmentService } from 'src/app/services/department.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import * as moment from 'moment';
import { Sort } from '@angular/material/sort';
import { ImageService } from 'src/app/services/image.service';
import { DomSanitizer } from '@angular/platform-browser';
import { Document } from 'src/app/models/document';
import { PortalService } from 'src/app/services/portal.service';
import { UtilityService } from 'src/app/services/utility.service';
import { AppComponent } from 'src/app/app.component';

@Component({
  selector: 'app-employee-config',
  templateUrl: './employee-config.component.html',
  styleUrls: ['./employee-config.component.css']
})
export class EmployeeConfigComponent implements OnInit {

  feature = 'Employee Config';
  data:string;
  items = 10;
  datas:string;

  //flags 
  isCreation: boolean = false;
  isUpdation: boolean = false;
  isDeletion: boolean = false
  isForm: boolean = false;
  isTable: boolean = false;

  isDraft: boolean = false;
  isDraftTable: boolean = false;
  dateOfReleivingshow: boolean = false;

  //modal 
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();
  previewModalRef: BsModalRef = new BsModalRef();
  all:any;
  //Obj 
  currentUser: User;
  employeeObj: Employee = new Employee();
  allEmployeeList: any;
  _allEmployeeList: any;	
  managerList: any = [];
  userMapping: any = {};
  allJobRoleList: any[] = [];
  allDeptList: any[] = [];
  filteredJobRoleList: any[] = [];
  employeeDataForExcel: any[] = [];
  portalConfigList:any[] = [];

  employeeWorkingHistory:[]
  allCertificationList: any[] = [];
  allPreviousEmployment: any[] = [];
  updatedCertificationList: any[] = [];
  updatedPreviousEmployment: any[] = [];
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
  currDate:any;
  yearOfPassingList:any[] = [];
  revoke_template: any;

  previewObj:Employee = new Employee();
  previewEmployeeObj:Employee = new Employee();
  
  constructor(
    private employeeService: EmployeeService,
    public validationService: ValidationService,
    private datePipe: DatePipe,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private jobRoleService: JobRoleService,
    private departmentService: DepartmentService,
    private exportExcelService: ExportExcelService,
    private imageService : ImageService,
    private sanitizer: DomSanitizer,
    private portalService:PortalService,
    private utilityService:UtilityService,
    private locationStrategy:LocationStrategy) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    this.currDate = this.datePipe.transform(new Date(), 'YYYY-MM-dd');
    this.getAllJobRoleList();

    // Dynamic Subfeature Flags 
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    console.log(this.feature, this.userMapping);

    this.sectionViewInit();

    //Deafult values for dropdown
    this.employeeObj.gender = '';
    this.employeeObj.maritalStatus = '';
    // this.employeeObj.managerId = '';
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

  }

  sectionViewInit() {
    if(this.userMapping.create_employee){
      this.showCreateForm()
    }else if (this.userMapping.view_all_employee || this.userMapping.update_employee || this.userMapping.delete_employee || this.revoke_template) {
      //for employee table data 
      this.showTable();
    } else if (this.userMapping.update_draft) {
      //for employee draft table data 
      this.showDraftTable();
    }
    // this.addDemographiscInfo();
  }

  addDemographiscInfo(){
    let path;

    let data = []
    
    // const pincodeData = data.map(empObj => {
    //     let pincode;
    //     const matches:any = empObj.address.match(/[1-9]{1}\d{2}\s?\d{3}/gm);
    //     if(matches){
    //       pincode = matches[0];
    //     }
    //     return {
    //       employeeId : empObj.employeementId,
    //       pincode : pincode,
    //       employeeName : empObj.name
    //     }
    // });

    // console.log("pincode data : ", pincodeData);

    data.forEach(empData => {
      let pincode = empData.pincode;
      let empId = empData.employeeId;

      if(pincode != null){
        fetch('https://api.postalpincode.in/pincode/' + pincode).then(r => r.json()).then(j => {
        path = j[0].PostOffice[0];
        console.log(path, " : path");


        let empObj = new Employee();
        empObj.state = path.State;
        empObj.city = path.Name;
        empObj.pincode = path.Pincode;
        empObj.country = path.Country;
        empObj.employeementId = empId;

        console.log(empObj, " empObj");

        this.employeeService.addDemographicsInfo(empObj).pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus == "Success") {
            console.log("Employee demographics updated");
          } else {
            console.log("Employee demographics updation failed");
          }
        });
      });
      }
    });
  }

  disableMannualDateInput() {
    return false;
  }

  setCalenderMaxDate() {
    const dateFormat = 'YYYY-MM-DD';
    const today = moment(new Date()).format(dateFormat);

    let DOB = document.getElementById('DOB');
    let DOJ = document.getElementById('DOJ');
    let DOC = document.getElementById('DOC');

    DOB?.setAttribute('max', today);
    DOJ?.setAttribute('max', today);
    DOC?.setAttribute('max', today);
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
    this.employeeObj.yearOfPassing = Number.parseInt(year);
  }

  showCreateForm() {
    this.isForm = true;
    this.isCreation = true;

    this.isTable = false;
    this.isUpdation = false;
    this.isDraft = false;
    this.isDraftTable = false;
    this.page=1;

    this.reset();
    this.getManagerList();
    this.getAllDepartmentList();
    this.getAllPortalConfigData();
    this.employeeObj.employmentstatus = "Probation";
    setTimeout(this.setCalenderMaxDate, 1000);
  }

  showTable() {
    this.isTable = true;

    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.isDraft = false;
    this.isDraftTable = false;
    this.isDeletion = false;
    this.page=1;
    this.data='';

    this.managerList = [];
    this.getAllEmployeeList();
  }

  showDraftTable() {
    this.isDraftTable = true;

    this.isTable = false;
    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.isDraft = false;
    this.isDeletion = false;
    this.page=1;
    this.data='';

    this.getAllDraftEmployees();
  }

  reset() {
    this.employeeObj = new Employee();
    //Deafult values for dropdown
    this.employeeObj.gender = '';
    this.employeeObj.maritalStatus = '';
    this.employeeObj.managerId = '';
    this.employeeObj.departmentId = '';
    this.employeeObj.jobRoleId = '';
    this.employeeObj.graduationType = '';
    this.employeeObj.pursuing = '';
    this.employeeObj.experience = '';
    this.employeeObj.workLocation = '';
    this.employeeObj.dateOfBirth = '';
    this.employeeObj.billable = '';

    this.allEmployeeList = [];
    this.filteredJobRoleList = [];
    this.allCertificationList = [];
    this.allPreviousEmployment = [];
    this.addInputCertificationField();
    this.addInputPreviousEmployerField();
  }

  showUpdateForm(employee: Employee) {
    this.isForm = true;
    this.isTable = false;
    this.isUpdation = true;
    this.isCreation = false;
    this.isDraft = false;
    this.isDraftTable = false;
    this.isDeletion = false;

    this.getManagerList(employee);
    this.getAllDepartmentList();
    this.allCertificationList = [];
    this.allPreviousEmployment = [];
    this.updatedCertificationList = [];
    this.updatedPreviousEmployment = [];
    
    employee.employeementId = this.utilityService.substringEmployeementid(employee.employeementId);
    // employee.employeementId = employee.employeementId?.substring(2)

    this.employeeService.getEmployeeByEmpId(employee).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
 
        this.employeeObj = Object.assign({}, response.serviceResponse);
        // employee.employeementId = this.utilityService.appendEmployeementid(this.employeeObj.employeementId)
        
        this.employeeObj.employeementId = "A-".concat(this.employeeObj.employeementId);

        console.log("employee :", this.employeeObj);
        // employee.employeementId = this.utilityService.appendEmployeementid(employee.employeementId);
        // Job Role
        if (this.employeeObj.departmentId) {
          this.getJobRolesByDept(this.employeeObj.departmentId, this.employeeObj.jobRoleId);
        }

        // Certifications
        if (this.employeeObj.certifications == undefined || this.employeeObj.certifications.length == 0) {
          this.addInputCertificationField();
        } else {
          this.allCertificationList = this.employeeObj.certifications;
        }

        // Prev. Employment
        if (this.employeeObj.previousEmploymentList == undefined || this.employeeObj.previousEmploymentList.length == 0) {
          this.addInputPreviousEmployerField();
        } else {
          this.allPreviousEmployment = this.employeeObj.previousEmploymentList;
        }
      } else {
        console.error(response.serviceResponse)
      }
    });

    setTimeout(this.setCalenderMaxDate, 1000);
  }

  showUpdateDraftForm(employee: Employee) {
    this.isDraft = true;
    this.isForm = false;
    this.isTable = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.isDraftTable = false;

    this.getManagerList();
    this.getAllDepartmentList();    
    
    employee.employeementId = this.utilityService.substringEmployeementid(employee.employeementId);
    this.employeeService.getDraftEmployeeByEmpId(employee).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.employeeObj = Object.assign({}, response.serviceResponse);
        console.log("employee :", this.employeeObj);

        // Job Role
        if (this.employeeObj.departmentId) {
          this.getJobRolesByDept(this.employeeObj.departmentId, this.employeeObj.jobRoleId);
        }

        // Certifications
        if (this.employeeObj.certifications == undefined || this.employeeObj.certifications.length == 0) {
          this.addInputCertificationField();
        } else {
          this.allCertificationList = this.employeeObj.certifications;
        }

        // Prev. Employment
        if (this.employeeObj.previousEmploymentList == undefined || this.employeeObj.previousEmploymentList.length == 0) {
          this.addInputPreviousEmployerField();
        } else {
          this.allPreviousEmployment = this.employeeObj.previousEmploymentList;
        }
      } else {
        console.error(response.serviceResponse)
      }
    });
    
    setTimeout(this.setCalenderMaxDate, 1000);
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
    // newCertificationObj.certificationId = "";
    // newCertificationObj.duration = "";
    // newCertificationObj.modeOfCourse = "";

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

    // if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.employeementId)) {
    //   this.alertMessage = "Please enter Employment Id !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // } else if (!this.validationService.validateEmployeementId(employeeObj.employeementId)) {
    //   this.alertMessage = "Please enter valid Employment ID !!";
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

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

    // if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.gender)) {
    //   this.alertMessage = "Please select gender !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.dateOfBirth)) {
      this.alertMessage = "Please enter date of birth !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    // if (this.validationService.validateNullUndefinedEmptyString(employeeObj.bloodGroup) && !this.validationService.validateBloodGroup(employeeObj.bloodGroup)) {
    //   this.alertMessage = "Please enter Valid Blood Group !!"	
    //   this.openAlertMod(template, this.alertMessage);	
    //   return false;	
    // }

    // if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.fatherName)) {
    //   this.alertMessage = "Please enter father name !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // } else if (!this.validationService.validateAlphaWithSpace(employeeObj.fatherName)) {
    //   this.alertMessage = "Please enter Valid father Name !!";
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (this.validationService.validateNullUndefinedEmptyString(employeeObj.placeOfBirth) && !this.validationService.validateAlphaWithSpace(employeeObj.placeOfBirth)) {
    //   this.alertMessage = "Please enter Valid Place of birth !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (this.validationService.validateNullUndefinedEmptyString(employeeObj.motherTongue) && !this.validationService.validateAlphaWithSpace(employeeObj.motherTongue)) {
    //   this.alertMessage = "Please enter Valid Mother tongue number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (this.validationService.validateNullUndefinedEmptyString(employeeObj.passportNumber) && !this.validationService.validatePassportNumber(employeeObj.passportNumber)) {
    //   this.alertMessage = "Please enter Valid Passport number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.aadhar)) {
    //   this.alertMessage = "Please enter aadhar card number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // } else if (employeeObj.aadhar.toString().length != 12) {
    //   this.alertMessage = "Please enter Valid aadhar card number !!";
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.panNumber)) {
    //   this.alertMessage = "Please enter PAN card number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // } else if (!this.validationService.validatePancardNumber(employeeObj.panNumber)) {
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

    // if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.address)) {
    //   this.alertMessage = "Please enter Current Address !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.state)) {
    //   this.alertMessage = "Please enter state !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // } else if (!this.validationService.validateAlphaWithSpace(employeeObj.state)) {
    //   this.alertMessage = "Please enter Valid state !!";
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.city)) {
    //   this.alertMessage = "Please enter city !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // } else if (!this.validationService.validateAlphaWithSpace(employeeObj.city)) {
    //   this.alertMessage = "Please enter Valid city !!";
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.country)) {
    //   this.alertMessage = "Please enter country !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // } else if (!this.validationService.validateAlphaWithSpace(employeeObj.country)) {
    //   this.alertMessage = "Please enter Valid country !!";
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.pincode)) {
    //   this.alertMessage = "Please enter pincode !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // } else if (!this.validationService.validatePincodeNumber(employeeObj.pincode)) {
    //   this.alertMessage = "Please enter Valid pincode !!";
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (this.validationService.validateNullUndefinedEmptyString(employeeObj.alternateMobileNo) && !this.validationService.validateMobileNumber(employeeObj.alternateMobileNo)) {
    //   this.alertMessage = "Please enter valid alternate mobile number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.permanentAddress)) {
    //   this.alertMessage = "Please enter permanent address !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (this.validationService.validateNullUndefinedEmptyString(employeeObj.emergencyContactPerson) && !this.validationService.validateAlphaWithSpace(employeeObj.emergencyContactPerson)) {
    //   this.alertMessage = "Please enter Valid Emergency Contact Person Name !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (this.validationService.validateNullUndefinedEmptyString(employeeObj.relation) && !this.validationService.validateAlphaWithSpace(employeeObj.relation)) {
    //   this.alertMessage = "Please enter Valid Emergency Contact Person Relation !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (this.validationService.validateNullUndefinedEmptyString(employeeObj.emergencyContactMobile) && !this.validationService.validateMobileNumber(employeeObj.emergencyContactMobile)) {
    //   this.alertMessage = "Please enter Valid Emergency Contact Person Mobile Number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.dateOfJoining)) {
      this.alertMessage = "Please enter date of joining !!"
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
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.noticePeriod)) {
      this.alertMessage = "Please enter notice period !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.managerId)) {
      this.alertMessage = "Please select manager !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.departmentId)) {
      this.alertMessage = "Please select Department !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.jobRoleId)) {
      this.alertMessage = "Please select Designation !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.experience)) {
      this.alertMessage = "Please select experience !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if(employeeObj.experience =='Experienced'){
      if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.totalExperience)) {	
        this.alertMessage = "Please enter total experience !!"	
        this.openAlertMod(template, this.alertMessage);	
        return false;	
      }	
      if(employeeObj.totalExperience === 0){	
        this.alertMessage = "Please enter more than 0 number !!"	
        this.openAlertMod(template, this.alertMessage);	
        return false;	
      }	
        if (!this.validationService.validateExperiencedNumber(employeeObj.totalExperience)) {	
          this.alertMessage = "Please enter valid experience in Format (Years.Months)  !!"	
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
    if(employeeObj.employmentstatus == 'Resigned'){
      if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.dateOfResign)) {
        this.alertMessage = "Please Enter Resign Date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    // if (this.validationService.validateNullUndefinedEmptyString(employeeObj.bankName) && !this.validationService.validateAlphaWithSpace(employeeObj.bankName)) {
    //   this.alertMessage = "Please enter Valid Bank Name !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (this.validationService.validateNullUndefinedEmptyString(employeeObj.bankAccountNo) && !this.validationService.validateAlphaNumeric(employeeObj.bankAccountNo)) {
    //   this.alertMessage = "Please enter Valid Bank Account Number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (this.validationService.validateNullUndefinedEmptyString(employeeObj.bankIFSCCode) && !this.validationService.validateAlphaNumeric(employeeObj.bankIFSCCode)) {
    //   this.alertMessage = "Please enter Valid Bank IFSC Code !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (this.validationService.validateNullUndefinedEmptyString(employeeObj.pfAccountNumber) && !this.validationService.validateAlphaNumeric(employeeObj.pfAccountNumber)) {
    //   this.alertMessage = "Please enter Valid PF Account Number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (this.validationService.validateNullUndefinedEmptyString(employeeObj.previousPfAccountNumber) && !this.validationService.validateAlphaNumeric(employeeObj.previousPfAccountNumber)) {
    //   this.alertMessage = "Please enter Valid Previous PF Account Number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }
    // if (this.validationService.validateNullUndefinedEmptyString(employeeObj.uan) && !this.validationService.validateAlphaNumeric(employeeObj.uan)) {
    //   this.alertMessage = "Please enter Valid UAN Number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }
    // if (this.validationService.validateNullUndefinedEmptyString(employeeObj.esicNumber) && !this.validationService.validateAlphaNumeric(employeeObj.esicNumber)) {
    //   this.alertMessage = "Please enter Valid ESIC Number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }
    return true;
  }

  validateEmployeeDraftObj(employeeObj: Employee, template: TemplateRef<any>) {

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.employeementId)) {
      this.alertMessage = "Please enter Emp Id !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateEmployeementId(employeeObj.employeementId)) {
      this.alertMessage = "Please enter valid Employeement ID !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.dateOfBirth)) {
      this.alertMessage = "Please enter date of birth !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

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

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.dateOfJoining)) {
      this.alertMessage = "Please enter date of joining !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.employmentstatus)) {
      this.alertMessage = "Please enter employment status !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.managerId)) {
      this.alertMessage = "Please select manager !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.departmentId)) {
      this.alertMessage = "Please select Department !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.jobRoleId)) {
      this.alertMessage = "Please select Job Role !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.billable)) {
      this.alertMessage = "Please select billable !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    return true;
  }


  fieldRestictCharacter(event){
    var k;
    k = event.charCode;
    if((k == 33) || (k == 34) || (k == 35) || (k == 36 ) || (k == 37) || 
    (k == 38) || (k == 39) || (k == 40) || (k == 41) || (k == 42 ) || 
    (k == 43) || (k == 44) || (k == 46 ) || (k == 47) || (k == 58) ||
     (k == 59) || (k == 60) || (k == 61) || (k == 62 ) || (k == 63) || 
     (k == 64) || (k == 66 ) || (k == 67) || (k == 68) || (k == 69 ) ||
      (k == 70) || (k == 71) || (k == 72 ) || (k == 73) || (k == 74) || 
      (k == 75 ) || (k == 76) || (k == 77) || (k == 78) || (k == 79) || 
      (k == 80) || (k == 81 ) || (k == 82) || (k == 83) || (k == 84) ||
       (k == 85) ||  (k == 86) || (k == 87 ) || (k == 88) || (k == 89) ||
      (k == 90) || (k == 91) || (k == 92) || (k == 93) || (k == 94) ||
      (k == 95) || (k == 96) || (k == 97) || (k == 98 ) || (k == 99) || 
    (k == 99) || (k == 100) || (k == 101) || (k == 102) || (k == 103 ) || 
    (k == 104) || (k == 105) || (k == 106 ) || (k == 107) || (k == 108) ||
     (k == 109) || (k == 110) || (k == 111) || (k == 112 ) || (k == 113) || 
     (k == 114) || (k == 115 ) || (k == 116) || (k == 117) || (k == 118 ) ||
       (k == 119) || (k == 120) || (k == 121) || (k == 122) || (k == 123) ||
        (k == 124) || (k == 125) || (k == 126))
    {
      return (false);
    }
    return (true);

  }
  // 97 to 122

  // CRUD

  onCreateEmployee(template: TemplateRef<any>) {
    const dateFormat = 'YYYY-MM-DD';
    console.log("allCertificationList : ", this.allCertificationList);
    console.log("allPreviousEmployment : ", this.allPreviousEmployment);


    let inputValidated: boolean = this.validateEmployeeObj(this.employeeObj, template)
    if (!inputValidated) return;

    this.employeeObj.isDraft = false;
    // transform date formats to YYYY-MM-DD
    this.employeeObj.dateOfBirth = moment(this.employeeObj.dateOfBirth ).format(dateFormat);
    this.employeeObj.dateOfJoining = moment(this.employeeObj.dateOfJoining).format(dateFormat);
    

    this.employeeObj.certifications = (Object.keys(this.allCertificationList[0]).length === 0) ? null : this.allCertificationList;
    if(this.employeeObj.certifications){
      this.employeeObj.certifications.forEach(certificate => {
        certificate.dateOfCompletion = moment(certificate.dateOfCompletion).format(dateFormat);
      });
    }
    this.employeeObj.previousEmploymentList = (Object.keys(this.allPreviousEmployment[0]).length === 0) ? null : this.allPreviousEmployment;
    this.employeeObj.createdBy = this.currentUser.empId;
    console.log("Create Employe : ", this.employeeObj);
   let employee = Object.assign({},this.employeeObj)
    employee.employeementId = this.utilityService.substringEmployeementid(this.employeeObj.employeementId);
    this.employeeService.createEmployee(employee).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.employeeService.deleteDraftEmployee(this.employeeObj); // deleting draft once employee is created
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }

    });
  }

  checkEmail(template: TemplateRef<any>) {
    let employee = new Employee();
    employee.employeementId = this.utilityService.substringEmployeementid(this.employeeObj.employeementId)
    const regex = /^(?:[0-9]+[a-z_.]|[a-z_.])[a-z0-9_.]+@apmosys\.com$/i;
    // this.employeeObj.employeementId = this.utilityService.substringEmployeementid(this.employeeObj.employeementId);
   // const regex = /^[A-Za-z0-9._%+-]+@apmosys\.com$/;
    if (this.employeeObj.email != null){
      if (regex.test(this.employeeObj.email)) {
        this.employeeService.checkEmployeeEmail(employee).pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus == "Fail") {
            this.openAlertMod(template, response.serviceResponse);
            this.employeeObj.email = '';
          }
        });
      }
      else {
        this.openAlertMod(template, "Please enter valid email id !!");
        this.employeeObj.email = '';
      }
    } 

    
  }

  checkSecondaryEmail(template: TemplateRef<any>) {

    const regex = /^(?:[0-9]+[a-z_.]|[a-z_.])[a-z0-9_.]+@apmosys\.com$/i;
   // const regex = /^[A-Za-z0-9._%+-]+@apmosys\.com$/;
    if (this.validationService.validateNullUndefinedEmptyString(this.employeeObj.secondaryEmail)){
      if (regex.test(this.employeeObj.secondaryEmail)) {
       
        this.openAlertMod(template, "Apmosys mail Id is not valid in secondary mail !!");
        this.employeeObj.secondaryEmail = '';
      }
    }     
  }

  checkEmployeementId(template: TemplateRef<any>) {
    let employee = new Employee();
    employee.employeementId = this.employeeObj.employeementId?.substring(2)
    if (!this.validationService.validateNullUndefinedEmptyString(employee.employeementId)) {
      this.alertMessage = "Please enter Employment ID !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if (!this.validationService.validateEmployeementId(employee.employeementId)) {
      this.alertMessage = "Please enter valid Employment ID !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
     
    this.employeeService.checkEmployeementId(employee).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Fail") {
        this.openAlertMod(template, response.serviceResponse);
        employee.employeementId = '';
      }
    });
  }


  checkEmployeeMobileNo(template: TemplateRef<any>) {
    let employee = new Employee();
    employee.employeementId = this.utilityService.substringEmployeementid(this.employeeObj.employeementId)
    this.employeeService.checkEmployeeMobileNo(employee).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Fail") {
        this.openAlertMod(template, response.serviceResponse);
        this.employeeObj.mobileNo = '';
      }
    });
  }

  checkEmployeeAadharNumber(template: TemplateRef<any>) {
    this.employeeService.checkEmployeeAadharNumber(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Fail") {
        this.openAlertMod(template, response.serviceResponse);
        this.employeeObj.aadhar = '';
      }
    });
  }

  checkEmployeePanNumber(template: TemplateRef<any>) {
    this.employeeService.checkEmployeePanNumber(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Fail") {
        this.openAlertMod(template, response.serviceResponse);
        this.employeeObj.panNumber = '';
      }
    });
  }

  clearAfterChange(){
    this.employeeObj.totalExperience = ''
  }
  onUpdateEmployee(template: TemplateRef<any>) {
    const dateFormat = 'YYYY-MM-DD';
    let inputValidated: boolean = this.validateEmployeeObj(this.employeeObj, template)
    if (!inputValidated) return;

    this.employeeObj.isDraft = false;
    // transform date formats to YYYY-MM-DD
    this.employeeObj.dateOfBirth = moment(this.employeeObj.dateOfBirth ).format(dateFormat)
    this.employeeObj.dateOfJoining = moment(this.employeeObj.dateOfJoining).format(dateFormat)
    this.employeeObj.dateOfResign = moment(this.employeeObj.dateOfResign).format(dateFormat)
   

    this.allCertificationList.forEach(certificaiton => {
      console.log("All certificaiton : ", this.allCertificationList);
      if ((certificaiton != undefined && Object.keys(certificaiton).length !== 0) && (certificaiton.employeeCertificateId == undefined || certificaiton.employeeCertificateId == null)) {
        certificaiton.dateOfCompletion = moment(certificaiton.dateOfCompletion).format(dateFormat);
        console.log("New certificaiton : ", certificaiton);
        this.updatedCertificationList.push(certificaiton);
      }
    });

    this.allPreviousEmployment.forEach(prevEmployer => {
      console.log("All Prev Employer : ", this.allPreviousEmployment);
      if ((prevEmployer != undefined && Object.keys(prevEmployer).length !== 0) && (prevEmployer.previousEmploymentId == undefined || prevEmployer.previousEmploymentId == null)) {
        console.log("New Prev Employer : ", prevEmployer);
        this.updatedPreviousEmployment.push(prevEmployer);
      }
    });

    this.employeeObj.certifications = (Object.keys(this.allCertificationList[0]).length === 0) ? null : this.allCertificationList;
    this.employeeObj.previousEmploymentList = (Object.keys(this.allPreviousEmployment[0]).length === 0) ? null : this.allPreviousEmployment;
    this.employeeObj.updatedCertifications = (this.updatedCertificationList.length === 0) ? null : this.updatedCertificationList;
    this.employeeObj.updatedPreviousEmploymentList = (this.updatedPreviousEmployment.length === 0) ? null : this.updatedPreviousEmployment;

    this.employeeObj.updatedBy = this.currentUser.empId;;
    console.log("Update Employe : ", this.employeeObj);

    let employee = Object.assign({}, this.employeeObj);
    employee.employeementId = this.utilityService.substringEmployeementid(this.employeeObj.employeementId)

    this.employeeService.updateEmployee(employee).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onDeleteEmployee(updatetemplate: TemplateRef<any>, template: TemplateRef<any>) {
    this.cancelRequest();
    this.employeeObj.employeementId = this.employeeObj.employeementId.substring(2);
    this.employeeService.deleteEmployee(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.isDeletion = true;
        this.employeeObj.oldManagerId = this.employeeObj.empId;
        this.employeeObj.newManagerId = '';
        this.getManagerList(this.employeeObj);
        this.modalRef = this.modalService.show(updatetemplate);
      }
    });
  }

  onChangeManagerMapping(template: TemplateRef<any>) {
    this.cancelRequest();
    this.isDeletion = false;

    if (!this.validationService.validateNullUndefinedEmptyString(this.employeeObj.newManagerId)) {
      this.alertMessage = "Please select a Manager !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    let employee: Employee = new Employee();
    employee.managerId = this.employeeObj.newManagerId;
    employee.oldManagerId = this.employeeObj.oldManagerId;

    console.log("changeManagerMapping : ", employee);
    
    this.employeeService.changeManagerMapping(employee).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {

        employee.empId = this.employeeObj.oldManagerId;
        console.log("deleteEmployee : ", employee);
        this.employeeService.deleteEmployee(employee).pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus == "Success") {
            this.openAlertMod(template, response.serviceResponse);
            this.showTable();
            this.page=1;
          } else {
            this.openAlertMod(template, response.serviceResponse);
          }
        });
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  getAllEmployeeList() {
    this.allEmployeeList = [];
    this.employeeService.getAllEmployees().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allEmployeeList = response.serviceResponse;
        console.log(this.allEmployeeList.dateOfRelieving,"dateofREleiving");
        console.log("allEmployeeList : ", this.allEmployeeList)
        this.allEmployeeList.forEach(employeeObj => {
          employeeObj.employeementId = this.utilityService.appendEmployeementid(employeeObj.employeementId);
          employeeObj.dateOfJoining = (employeeObj.dateOfJoining)? moment(employeeObj.dateOfJoining).format(AppComponent.DATE_FORMAT) : null;
          employeeObj.dateOfRelieving = (employeeObj.dateOfRelieving)? moment(employeeObj.dateOfRelieving).format(AppComponent.DATE_FORMAT) : null;
        });
        this._allEmployeeList = this.allEmployeeList;
        this.changeEvent("Active");
        // this.createEmployeeList(this.allEmployeeList)
      } else {
        alert(response.serviceResponse);
      }
    });
  }
  changeEvent(value:string){	
    if(value=="Active"){	
        this.allEmployeeList = this._allEmployeeList.filter(x => x.employmentstatus != 'InActive');	
        this.dateOfReleivingshow= false;
    }else if(value=="InActive"){	
      this.allEmployeeList = this._allEmployeeList.filter(x => x.employmentstatus == 'InActive');  	
      this.dateOfReleivingshow= true;
    }	
    this.page=1;
  }
  createEmployeeList(allEmployeeList: any) {
    this.managerList = allEmployeeList.map(employee => {
      let emp = { name: employee.name, empId: employee.empId.toString() };
      return emp;
    });
    console.log("managerList : ", this.managerList);
  }

  name = 'EmployeeSheet.xlsx';
  exportToExcel(): void {
    this.employeeService.getAllEmployees().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.employeeDataForExcel = response.serviceResponse;
      }
      const onlySpecificDataArr = this.employeeDataForExcel.map(
        x => ({
          "EmployeeId": "A-".concat(x.employeementId),		
          "Full Name": x.name,		
          "EmailId": x.email,		
          "Employment Status": x.employmentstatus,		
          "Date of Joining": (x.dateOfJoining)? moment(x.dateOfJoining).format(AppComponent.DATE_FORMAT) : null,
          "Date of Relieving" : (x.dateOfRelieving)? moment(x.dateOfRelieving).format(AppComponent.DATE_FORMAT) : null,
          "Department Name": x.departmentName,
          "Aadhar":x.aadhar,
          "About Me":x.aboutMe,
          "Address":x.address,
          "Permanent Address":x.permanentAddress,
          "City":x.city,
          "Blood Group":x.bloodGroup,
          "Date Of Birth":(x.dateOfBirth)? moment(x.dateOfBirth).format(AppComponent.DATE_FORMAT) : null,
          "Gender":x.gender,
          "Father Name":x.fatherName,
          "Mobile No":x.mobileNo,
          "Pan Number":x.panNumber,
          "Place Of Birth":x.placeOfBirth,
          "Work Location":x.workLocation,
          "Probation Period":x.probationPeriod,
          "Notice Period":x.noticePeriod,
          "Country":x.country,
          "Emergency Contact Mobile":x.emergencyContactMobile,
          "Emergency Contact Person":x.emergencyContactPerson,
          "Landline":x.landline,
          "Marital Status":x.maritalStatus,
          "Mother Tongue":x.motherTongue,
          "Alternate Mobile No":x.alternateMobileNo,
          "Pincode":x.pincode,
          "Relation":x.relation,
          "State":x.state,
          "Views On Organisation":x.viewsOnOrganisation,
          "Passport Number":x.passportNumber,
          "Bank Account No":x.bankAccountNo,
          "Bank IFSC Code":x.bankIFSCCode,
          "Bank Name":x.bankName,
          "PF Account Number":x.pfAccountNumber,
          "Previous PF AccountNumber":x.previousPfAccountNumber,
          "UAN":x.uan,
          "ESIC Number":x.esicNumber,
          "Graduation Type":x.graduationType,
          "Pursuing":x.pursuing,
          "Passing Grade":x.passingGrade,
          "Year Of Passing":x.yearOfPassing,
          "Created By":x.createdBy,
          "Created On":(x.createdOn)? moment(x.createdOn).format(AppComponent.DATETIME_FORMAT) : null,
          "Manager Name": x.managerName,
          "Designation":x.jobRoleName,

        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.name)
    });
  }

  getManagerList(employee?:Employee) {
    this.managerList = [];	
    let employeeList = [];	

    console.log("Skip manager : ", employee)
    this.employeeObj.role = "Manager";	
    this.employeeObj.employeementId = this.employeeObj.employeementId?.substring(2)
    this.employeeService.getAllEmployeesByRole(this.employeeObj).pipe(first()).subscribe((response: any) => {	
      if (response.serviceStatus == "Success") {	
        employeeList = response.serviceResponse;	
      
        console.log("employeeList By Role : ", employeeList)
        if(this.isUpdation || this.isDeletion){
          this.managerList = employeeList.filter((manager:Employee) => manager.empId !== employee.empId);
        }else{
          this.managerList = employeeList;
        }	
        console.log("managerList : ", this.managerList)	
      } else {	
        console.error(response.serviceResponse)	
      }	
    });	
  }	


  /* Employee Draft */
  onSaveDraftEmployee(template: TemplateRef<any>) {
    const dateFormat = 'YYYY-MM-DD';
    let inputValidated: boolean = this.validateEmployeeDraftObj(this.employeeObj, template)
    if (!inputValidated) return;

    this.employeeObj.isDraft = true;
    // // transform date formats to dd-MM-yyyy
    this.employeeObj.dateOfBirth = moment(this.employeeObj.dateOfBirth ).format(dateFormat);
    this.employeeObj.dateOfJoining = moment(this.employeeObj.dateOfJoining).format(dateFormat);

    this.employeeObj.certifications = (Object.keys(this.allCertificationList[0]).length === 0) ? null : this.allCertificationList;
    this.employeeObj.previousEmploymentList = (Object.keys(this.allPreviousEmployment[0]).length === 0) ? null : this.allPreviousEmployment;
    this.employeeObj.createdBy = this.currentUser.empId;
    console.log("Create Employe Draft : ", this.employeeObj);
    this.employeeService.createDraftEmployee(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showDraftTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onUpdateDraftEmployee(template: TemplateRef<any>) {
    const dateFormat = 'YYYY-MM-DD';
    this.employeeObj.isDraft = true;
    // // transform date formats to dd-MM-yyyy
    this.employeeObj.dateOfBirth = moment(this.employeeObj.dateOfBirth ).format(dateFormat);
    this.employeeObj.dateOfJoining = moment(this.employeeObj.dateOfJoining).format(dateFormat);

    this.employeeObj.certifications = (Object.keys(this.allCertificationList[0]).length === 0) ? null : this.allCertificationList;
    this.employeeObj.previousEmploymentList = (Object.keys(this.allPreviousEmployment[0]).length === 0) ? null : this.allPreviousEmployment;
    this.employeeObj.updatedBy = this.currentUser.empId;
    console.log("Update Employe Draft : ", this.employeeObj);
    this.employeeService.updateDraftEmployee(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showDraftTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onDeleteDraftEmployee(template: TemplateRef<any>) {
    this.cancelRequest();

    this.employeeObj.draftEmpId = this.employeeObj.empId;
    this.employeeService.deleteDraftEmployee(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showDraftTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  getAllDraftEmployees() {
    this.allEmployeeList = [];
    let employeeObj = new Employee();
    employeeObj.updateApplicationStatus = 'Pending For Approval';
    this.employeeService.getAllDraftEmployees(employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allEmployeeList = response.serviceResponse;
        for(let x of this.allEmployeeList){
          x.employeementId = "A-".concat(x.employeementId)
          x.dateOfJoining = (x.dateOfJoining)? moment(x.dateOfJoining).format(AppComponent.DATE_FORMAT) : null;
          x.dateOfRelieving = (x.dateOfRelieving)? moment(x.dateOfRelieving).format(AppComponent.DATE_FORMAT) : null;
        }
        console.log("allDraftEmployeeList : ", this.allEmployeeList)
      } else {
        alert(response.serviceResponse)
      }
    });
  }

  // Job Role
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

  getJobRolesByDept(departmentId: any, jobRoleId?:any) {
    this.filteredJobRoleList = [];
    this.filteredJobRoleList = this.allJobRoleList.filter(jobRole => jobRole.departmentId == departmentId);
    jobRoleId? this.employeeObj.jobRoleId = jobRoleId : this.employeeObj.jobRoleId = '';
  }

  validateBirthDate(template: TemplateRef<any>){   
    let birthdate = new Date(this.employeeObj.dateOfBirth);
    let dtCurrent = new Date();
    let flag = true;
    let dobInput:any = document.getElementById('DOB');

    if (dtCurrent.getFullYear() - birthdate.getFullYear() > 60) {
      this.openAlertMod(template, 'Employee age cannot be more than 60 years.');
      flag = false;
    }
    else if (dtCurrent.getFullYear() - birthdate.getFullYear() < 18) {
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

    if(!flag) {
      this.employeeObj.dateOfBirth = '';
      dobInput.value = '';
    }
  }

  rejectDraftEmployeeApplication(template: TemplateRef<any>){
    if(!this.validationService.validateNullUndefinedEmptyString(this.employeeObj.remarks)){
      this.alertMessage = "Please enter Reason for Rejecting !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } if(!this.validationService.validateTeamName(this.employeeObj.remarks.trim())){
      this.alertMessage = "Enter Valid Reason for Rejecting !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    this.cancelRequest();
    this.cancelApplication();
    this.employeeObj.updateApplicationStatus = 'In-Progress';
    this.employeeObj.updatedBy = this.currentUser.empId ;
    if(this.employeeObj.documentList){
      this.employeeObj.documentList.forEach((doc:Document) => doc.documentBytes = null);
    }
    this.employeeObj.remarks = this.employeeObj.remarks?.trim();
    this.employeeService.rejectDraftEmployeeApplication(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allEmployeeList = response.serviceResponse;
        this.openAlertMod(template, response.serviceResponse);
        this.showDraftTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  approveDraftEmployeeApplication(template: TemplateRef<any>){
    this.cancelRequest();
    this.cancelApplication();
    this.employeeObj.updateApplicationStatus = 'Approved';
    this.employeeObj.updatedBy = this.currentUser.empId ;
    if(this.employeeObj.documentList){
      this.employeeObj.documentList.forEach((doc:Document) => doc.documentBytes = null);
    }  

    this.employeeService.approveDraftEmployeeApplication(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allEmployeeList = response.serviceResponse;
        this.openAlertMod(template, response.serviceResponse);
        this.showDraftTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }
  
  // Employee Info Preview 
  async openEmployeeInfoPreview(template: TemplateRef<any>, employeeObj: Employee) {

    console.log("employeeObj : ", employeeObj);
    
    let currentEmp = new Employee();
    currentEmp.employeementId = employeeObj.employeementId.substring(2);
    currentEmp.empId = employeeObj.empId;
    currentEmp.isDraft = true;

    const infoResponse: any = await this.employeeService.getDraftEmployeeByEmpId(currentEmp).toPromise();
    if (infoResponse.serviceStatus == "Success") {
      this.previewObj = infoResponse.serviceResponse;
      console.log("this.previewObj : ", this.previewObj);
    } else {
      console.error(infoResponse.serviceResponse)
    }

    const docResponse:any = await this.imageService.getEmployeeDocuments(currentEmp).toPromise();
    if (docResponse.serviceStatus == 'Success') {
      this.previewObj.documentList = docResponse.serviceResponse;
      console.log("this.previewObj.documentList : ", this.previewObj.documentList);
    } else {
      console.log(docResponse.serviceResponse);
    }
    this.previewModalRef = this.modalService.show(template, { class: 'modal-xl'});
    setTimeout(()=>{
      this.previewObj.documentList.forEach((doc, index) => {
        if (doc.documentBytes) {
          let preview = document.getElementById(`docPreview${index + 1}`);
            let objectURL = 'data:image/*;base64,' + doc.documentBytes;
            let src: string = this.sanitizer.sanitize(SecurityContext.RESOURCE_URL, this.sanitizer.bypassSecurityTrustResourceUrl(objectURL));
            preview.setAttribute('src', src);
        }
      });
    }, 500)
  }

  // Employee Info preview View all tab
  async openViewEmployeeInfoPreview(template: TemplateRef<any>, employeeObj: Employee) {

    console.log("employeeObj : ", employeeObj);
    
    let currentEmp = new Employee();
    currentEmp.employeementId = currentEmp.employeementId?.substring(2);
    currentEmp.empId = employeeObj.empId;
    currentEmp.isDraft = false;


    const infoResponse: any = await this.employeeService.getEmployeeByEmpId(currentEmp).toPromise();
    if (infoResponse.serviceStatus == "Success") {
      this.previewEmployeeObj = infoResponse.serviceResponse;
      
      console.log("this.previewObj : ", this.previewEmployeeObj);
    } else {
      console.error(infoResponse.serviceResponse)
    }

    const docResponse:any = await this.imageService.getEmployeeDocuments(currentEmp).toPromise();
    if (docResponse.serviceStatus == 'Success') {
      this.previewEmployeeObj.documentList = docResponse.serviceResponse;
      console.log("this.previewObj.documentList : ", this.previewEmployeeObj.documentList);
    } else {
      console.log(docResponse.serviceResponse);
    }
    this.previewModalRef = this.modalService.show(template, { class: 'modal-xl'});
    setTimeout(()=>{
      this.previewEmployeeObj.documentList && this.previewEmployeeObj.documentList.forEach((doc, index) => {
        if (doc.documentBytes) {
          let preview = document.getElementById(`docPreview${index + 1}`);
            let objectURL = 'data:image/*;base64,' + doc.documentBytes;
            let src: string = this.sanitizer.sanitize(SecurityContext.RESOURCE_URL, this.sanitizer.bypassSecurityTrustResourceUrl(objectURL));
            preview.setAttribute('src', src);
        }
      });
    }, 500)
  }


  getAllPortalConfigData() {
    this.portalService.getPortalConfig().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.portalConfigList = response.serviceResponse;
        for(let portal of this.portalConfigList){
          if(portal.configName == 'Probation Period'){
            this.employeeObj.probationPeriod = portal.configPeriod;
          }
          if(portal.configName == 'Notice Period'){
            this.employeeObj.noticePeriod = portal.configPeriod;
          }
        }
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  // modals
  openDeleteEmployee(template: TemplateRef<any>, employee: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.employeeObj = employee;
  }

  openDeleteDraftEmployee(template: TemplateRef<any>, employee: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.employeeObj = employee;
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  openApplicationRejectionMod(template: TemplateRef<any> , employee: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-md' });
    this.employeeObj = employee;
  }

  openApplicationApprovalMod(template: TemplateRef<any> , employee: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-md' });
    this.employeeObj = employee;    
  }

  cancelApplication(){
    this.previewModalRef.hide();
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  //pagination 	
  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  pageNo = 1;
  handlePageChanges(event) {
    this.pageNo = event;
  }
  handleItemChanges(event) {
    this.pageNo = event;
  }

  onPage(){
    this.page=1;
  }

  // implementing sorting functionality by anurag
  
  sortData(sort: Sort){	
    console.log(sort);  	
       let data=this.allEmployeeList;	
       console.log("anurag :" , this.allEmployeeList );	
         
         if(!sort.active || sort.direction ===''){	
          this.allEmployeeList=data;	
         return;	
        }	
         else {	
          this.allEmployeeList=data.sort(	
             (a , b )=>{	
               const isAsc=sort.direction==='asc';	
               switch(sort.active){	
                  case 'employeementId':	
                    return compare(a.employeementId, b.employeementId, isAsc); 	
        
                   case 'name':	
                     return compare(a.name.toLowerCase() , b.name.toLowerCase() , isAsc);	
                   
                    case 'email':	
                      return compare(a.email , b.email , isAsc);	
                      case 'dateOfJoining':	
                          
                        return  compare(new Date(a.dateOfJoining).getTime() ,  new Date(b.dateOfJoining).getTime(), isAsc);	
                   default:	
                     return 0; 	
                 }	
             }	
           )	
         }	
       }	
       // implement Enable Account facilities by anurag

      forEnableAccount(template: TemplateRef<any>, employee: any) {
        this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
        this.employeeObj = employee;
      }

      

      findEmployeeWorkingHistory(template: TemplateRef<any>, employee: any){
        this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
       this.employeeObj = employee;
     }

     onRevokeAccount(template: TemplateRef<any>) {
      this.cancelRequest();
      this.employeeObj.employeementId = this.employeeObj.employeementId.substring(2)
      this.employeeService.revokeAccount(this.employeeObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.openAlertMod(template, response.serviceResponse);
          this.showTable();
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }
      });
    }
    

   onFindHistoryWorking(employee:Employee) {
    this.employeeWorkingHistory = [];
    let empObj = new Employee();
    empObj.empId = employee.empId;
    
     this.cancelRequest();
     this.employeeService.findEmployeeWorkingHistory(empObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.employeeWorkingHistory=response.serviceResponse;
      } else {
        console.error(response.serviceResponse);
      }
     });
   }        
}
function compare(a: number | string, b: number | string, isAsc: boolean) {	
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);	
}
