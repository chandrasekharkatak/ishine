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
import { SortPipe } from 'src/app/sort.pipe';
import { Domain } from 'src/app/models/domain';
import { DomainService } from 'src/app/services/domain.service';
import { Query } from 'src/app/models/query';
import { DestinationService } from 'src/app/services/destination.service';
import { Designation } from 'src/app/models/designation';
class FilterData {
  title: any;
  columns: any;
  queryList: any;
}
@Component({
  selector: 'app-employee-config',
  templateUrl: './employee-config.component.html',
  styleUrls: ['./employee-config.component.css']
})
export class EmployeeConfigComponent implements OnInit {

  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;

  feature = 'Employee Config';
  data:string;
  items = 10;
  datas:string;

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;

  domainToBeDeleted:any;

  //flags 
  isCreation: boolean = false;
  isUpdation: boolean = false;
  isDeletion: boolean = false
  isForm: boolean = false;
  isTable: boolean = false;

  isDraft: boolean = false;
  isDraftTable: boolean = false;
  dateOfReleivingshow: boolean = false;

  isDomain: boolean = false;
  isDomainTable: boolean = false;
  isDomainCreation: boolean = false;
  isDomainUpdation: boolean = false;
  isDomainForm: boolean = false;

  //modal 
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();
  previewModalRef: BsModalRef = new BsModalRef();
  all:any;
  //Obj 
  currentUser: User;
  employeeObj: Employee = new Employee();
  domainObj: Domain = new Domain();
  allEmployeeList: any;
  _allEmployeeList: any;	
  managerList: any = [];
  userMapping: any = {};
  allJobRoleList: any[] = [];
  allDeptList: any[] = [];
  filteredJobRoleList: any[] = [];
  employeeDataForExcel: any[] = [];
  portalConfigList:any[] = [];
  allDomainList:any[] = [];
  specializationList:any[] = [];
  allSpecializationList:any[] = [];
  storedDataList:any[] = [];
  domainSpecializationList:any[] = [];
  allDesignationList:any[] = [];

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

  filters:any = {};
  isSearchEnabled:boolean = false;
  employeeActiveColumns:any[] = ['employeementId','name','email','employmentstatus','managerName','departmentName','dateOfJoining','createdOn','createdByName','updatedOn','updatedByName'];
  employeeInActiveColumns:any[] = ['employeementId','name','email','employmentstatus','managerName','departmentName','dateOfJoining','dateOfRelieving','createdOn','createdByName','updatedOn','updatedByName'];
  draftEmployeeColumns:any[] = ['employeementId','name','email','employmentstatus','managerName','departmentName','dateOfJoining','updateApplicationStatus']
  domainColumns:any[] = ['blank','domainName','createdByName','createdOn']
  
  workHistoryFilters:any = {};
  isworkHistorySearchEnabled:boolean = false;
  employeeWorkhistoryColumns:any[] = ['occasion','dayOfTheWeek','dateOfHoliday','state','createdOn','createdbyName','updatedOn','updatedByName'];
  

  queryList: any[] = [];
  filterData: any = new FilterData();
  employeeColumns: any[] = ['Employee Id', 'Full Name', 'Department','Designation', 'Job Role', 'Manager', 'Team Name', 'Project Name', 'Client Name', 'Employment Status', 'Date Of Joining','Domain','Specialization', 'City', 'Blood Group', 'Gender', 'Work Location', 'Probation Period', 'Notice Period', 'Marital Status', 'Bank Name', 'Created By', 'State', 'Created On'];

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
    private locationStrategy:LocationStrategy,
    private domainService:DomainService,
    private destinationService:DestinationService) {
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
    this.employeeObj.reportingManagerId = '';
    this.employeeObj.approvalsTo = '';
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

  relievingDateFilter = (d: Date)=>{
    const dateFormat = 'YYYY-MM-DD';
    const currentDate = new Date();
    
    let resignDate = this.employeeObj.dateOfResign;
    
    if(resignDate){
      return (moment(d).format(dateFormat) >= moment(resignDate).format(dateFormat));
    }else{
      return false;
    }
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
    this.isDomain = false;
    this.isDomainTable = false;
    this.isDomainCreation = false;
    this.isDomainUpdation = false;
    this.isDomainForm = false;
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
    this.filters = {};
    this.workHistoryFilters = {};
    this.isSearchEnabled = false;
    this.isworkHistorySearchEnabled = false;
    this.isDomain = false;
    this.isDomainTable = false;
    this.isDomainCreation = false;
    this.isDomainUpdation = false;
    this.isDomainForm = false;

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
    this.isDomain = false;
    this.isDomainTable = false;
    this.isDomainCreation = false;
    this.isDomainUpdation = false;
    this.isDomainForm = false;
    this.page=1;
    this.data='';
    this.filters = {};

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
    this.isDomain = false;
    this.isDomainTable = false;
    this.isDomainCreation = false;
    this.isDomainUpdation = false;
    this.isDomainForm = false;

    this.getManagerList(employee);
    this.getAllDepartmentList();
    this.getAllDomain();
    this.allCertificationList = [];
    this.allPreviousEmployment = [];
    this.updatedCertificationList = [];
    this.updatedPreviousEmployment = [];
    
    employee.employeementId = this.utilityService.substringEmployeementid(employee.employeementId);
    // employee.employeementId = employee.employeementId?.substring(2)

    this.employeeService.getEmployeeByEmpId(employee).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
 
        this.employeeObj = Object.assign({}, response.serviceResponse);

        if( this.employeeObj.domainList != null){
          this.getDomainSpecialization();
        }
        this.getDesignationByDeptId(this.employeeObj.departmentId);
        // employee.employeementId = this.utilityService.appendEmployeementid(this.employeeObj.employeementId)
        
        this.employeeObj.employeementId = "A-".concat(this.employeeObj.employeementId);

        console.log("employee :", this.employeeObj);
        // employee.employeementId = this.utilityService.appendEmployeementid(employee.employeementId);
        // Job Role
        if (this.employeeObj.departmentId) {
          this.getJobRolesByDept(this.employeeObj.departmentId, this.employeeObj.jobRoleId);
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
    this.isDomain = false;
    this.isDomainTable = false;
    this.isDomainCreation = false;
    this.isDomainUpdation = false;
    this.isDomainForm = false;

    this.getManagerList();
    this.getAllDepartmentList();
    this.getAllDomain();    
    
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

  showAllDomain(){
    this.isDomain = true;
    this.isDomainTable = true;

    this.isDomainCreation = false;
    this.isDomainUpdation = false;
    this.isDomainForm = false;
    
    this.isForm = false;
    this.isTable = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.isDraft = false;
    this.isDraftTable = false;
    this.isDeletion = false;
    this.getAllDomain(this.alertTemplate);
  }

  showCreateDomainForm(){
    this.isDomainCreation = true;
    this.isDomainForm = true;
    
    this.isDomain = false;
    this.isDomainTable = false;
    this.isDomainUpdation = false;
    
    this.isForm = false;
    this.isTable = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.isDraft = false;
    this.isDraftTable = false;
    this.isDeletion = false;

    this.domainObj = new Domain();
    this.allSpecializationList = [];

    //Template Activity
    if (this.domainObj.allSpecializationList == undefined || this.domainObj.allSpecializationList.length == 0) {
      this.addInputSpecializationField();
    } else {
      this.allSpecializationList = this.domainObj.allSpecializationList;
    }
  }

  showUpdateDomainForm(domain:any){
    this.isDomainForm = true;
    this.isDomainUpdation = true;
    
    this.isDomainCreation = false;
    this.isDomain = false;
    this.isDomainTable = false;
    
    this.isForm = false;
    this.isTable = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.isDraft = false;
    this.isDraftTable = false;
    this.isDeletion = false;

    this.domainService.getDomainSpecializationByDomainId(domain).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.domainObj = Object.assign({}, response.serviceResponse);

        this.allSpecializationList = this.domainObj.allSpecializationList;

        console.log(response.serviceResponse, " : response.serviceResponse");
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  // Manage Domain / Specialization
  addInputSpecializationField(){
    let domainObj = new Domain();
    this.allSpecializationList.push(domainObj);
    console.log(this.allSpecializationList, " : this.allSpecializationList");
  }

  removeInputSpecializationField(spec:any){
    this.allSpecializationList.forEach((value, index) => {
      if (value == spec) {
        this.allSpecializationList.splice(index, 1);
      }
    });
    console.log(this.allSpecializationList, " :this.allSpecializationList");
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
    }if (employeeObj.probationPeriod > 365 || employeeObj.probationPeriod < 0) {
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

    if(employeeObj.employmentstatus == "Resigned"){
      if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.dateOfResign)) {
        this.alertMessage = "Please enter date of Resign !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }else if(employeeObj.employmentstatus == "InActive"){
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

    if(employeeObj.reportingManagerId){
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
        }	 if (employeeObj.totalExperience > 60) {
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
    if(employeeObj.employmentstatus == 'Resigned'){
      if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.dateOfResign)) {
        this.alertMessage = "Please Enter Resign Date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    // if(this.isUpdation){
    //   if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.domainList)) {
    //     this.alertMessage = "Please select Domain !!"
    //     this.openAlertMod(template, this.alertMessage);
    //     return false;
    //   }else if(employeeObj.domainList.length == 0){
    //     this.alertMessage = "Please select Domain !!"
    //     this.openAlertMod(template, this.alertMessage);
    //     return false;
    //   }
  
    //   if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.specializationList)) {
    //     this.alertMessage = "Please select Specialization(s) !!"
    //     this.openAlertMod(template, this.alertMessage);
    //     return false;
    //   }else if(employeeObj.specializationList.length == 0){
    //     this.alertMessage = "Please select Specialization(s) !!"
    //     this.openAlertMod(template, this.alertMessage);
    //     return false;
    //   }
    // }

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

  omit_character(event){	
    var k;	
    k = event.charCode;	
    if((k == 45) || (k == 69) || (k == 101 ))	
    {	
      return (false);	
    }	
    return (true);	
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

    if(this.employeeObj.employeementId.startsWith('A-')){
      employee.employeementId  = this.employeeObj.employeementId.substring(2);
      console.log("Employee :", this.employeeObj);
    }else {
      employee.employeementId  = this.employeeObj.employeementId
    }

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
        employee.email = this.employeeObj.email;
        employee.empId = this.employeeObj.empId;
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
    employee.empId = this.employeeObj.empId;
    employee.email = this.employeeObj.email;

    if(this.employeeObj.employeementId.startsWith('A-')){
      if(!this.validationService.validateNullUndefinedEmptyString(this.employeeObj.employeementId)){
        this.alertMessage = "Please enter Employee ID !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      employee.employeementId  = this.employeeObj.employeementId.substring(2);
      console.log("Employee :", this.employeeObj);
    }else {
      employee.employeementId  = this.employeeObj.employeementId
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
    if(this.employeeObj.dateOfBirth) this.employeeObj.dateOfBirth = moment(this.employeeObj.dateOfBirth ).format(dateFormat)
    if(this.employeeObj.dateOfJoining) this.employeeObj.dateOfJoining = moment(this.employeeObj.dateOfJoining).format(dateFormat)
    if(this.employeeObj.dateOfResign) this.employeeObj.dateOfResign = moment(this.employeeObj.dateOfResign).format(dateFormat)

    if(this.employeeObj.employmentstatus == "Confirmed" || this.employeeObj.employmentstatus == "Probation" ){	
      this.employeeObj.dateOfResign = null;	
      this.employeeObj.dateOfRelieving= null; 	
    }

    this.employeeObj.updatedBy = this.currentUser.empId;;
    console.log("Update Employe : ", this.employeeObj);

    let employee = Object.assign({}, this.employeeObj);
    employee.updatedBy = this.currentUser.empId;

    if(this.employeeObj.employeementId.startsWith('A-')){
      employee.employeementId  = this.employeeObj.employeementId.substring(2);
      console.log("Employee :", this.employeeObj);
    }else {
      employee.employeementId  = this.employeeObj.employeementId
    }

    employee.specializationList = this.employeeObj.specializationList;

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
          employeeObj.updatedOn = (employeeObj.updatedOn)? moment(employeeObj.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
          employeeObj.createdOn = (employeeObj.createdOn)? moment(employeeObj.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        });
        this._allEmployeeList = this.allEmployeeList;
        this.changeEvent("Active");

        // Default Sorting
        this.allEmployeeList = new SortPipe().transform(this.allEmployeeList, ['name','string', 'asc']);
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
          "Job Role":x.jobRoleName,

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

  getDesignationByDeptId(departmentId:any){
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
    console.log(" reject KYC :  ",this.employeeObj)
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
    this.domainSpecializationList = [];
    
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

    let domainObj = new Domain();
    domainObj.empId = employeeObj.empId;
    const domainResponse:any = await this.domainService.getDomainSpecializationByEmpId(domainObj).toPromise();
      if (domainResponse.serviceStatus == "Success") {
        this.domainSpecializationList = domainResponse.serviceResponse;
        console.log(this.domainSpecializationList, " : this.domainSpecializationList");
      } else {
        console.error(domainResponse.serviceResponse);
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

  updateNoticePeriod(employeeObj: Employee){	
    const dateFormat = 'YYYY-MM-DD';	
    console.log(employeeObj,"employeeObj");	
    this.employeeObj.dateOfRelieving = moment(this.employeeObj.dateOfRelieving).format(dateFormat);	
    console.log(this.employeeObj.dateOfRelieving);	
    const diff =  Math.abs(Math.floor((new Date(this.employeeObj.dateOfRelieving).getTime() - new Date(employeeObj.dateOfResign).getTime()) / (1000 * 60 * 60 * 24)));		
    this.employeeObj.noticePeriod = diff;	
    console.log(diff, "diffDaysdiffDays")	
  }
  	
  estimateDateOfReleiving(employeeObj: Employee){	
    const dateFormat = 'YYYY-MM-DD';	
    
    if(this.employeeObj.dateOfResign){
      let estimateDateOfRelieving = new Date(this.employeeObj.dateOfResign);	
      this.employeeObj.dateOfRelieving = moment(estimateDateOfRelieving).add(this.employeeObj.noticePeriod, "days").format(dateFormat);	
      console.log(this.employeeObj.dateOfRelieving, "this.employeeObj.dateOfRelieving")	
    }
  }	

  onUpdateTimesheetLockCheck(template: TemplateRef<any>,employeeObj:Employee,status: any){
    let employee = Object.assign({}, employeeObj);
    employee.isTimesheetLockCheckEnable = status;
    employee.updatedBy = this.currentUser.empId;

    if(employee.employeementId.startsWith('A-')){
      employee.employeementId  = employee.employeementId.substring(2);
    }else {
      employee.employeementId  = employee.employeementId
    }

    console.log("updateTimesheetLockCheck : ", employee);
    this.employeeService.updateTimesheetLockCheck(employee).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.getAllEmployeeList();
        this.openAlertMod(template, response.serviceResponse);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  //Doamin & Specialization  :: start

  getAllDomain(template?: TemplateRef<any>){
    this.domainService.getAllDomain().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDomainList = response.serviceResponse;

        this.allDomainList.forEach((domain) => {
          domain.createdOn = (domain.createdOn)? moment(domain.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        });

        console.log(this.allDomainList, " : this.allDomainList");
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }
  
  getDomainSpecialization(){
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
  
  validateDomainObj(domainObj, template: TemplateRef<any>) {
    let flag = true;

    domainObj.domainName = domainObj.domainName?.trim();
    if (!this.validationService.validateNullUndefinedEmptyString(domainObj.domainName)) {
      this.alertMessage = "Please enter Domain Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if (!this.validationService.validateTeamActivity(domainObj.domainName)) {
      this.alertMessage = "Please enter Valid Domain Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    const uniqueSpecialization = new Set(this.allSpecializationList.map(x => x.specializationName));
    if (uniqueSpecialization.size < this.allSpecializationList.length) {
      this.alertMessage = "Duplicate Specialization Name are not allowed !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    this.allSpecializationList.forEach((spec, index) => {

      spec.specializationName = spec.specializationName?.trim();
      if (!this.validationService.validateNullUndefinedEmptyString(spec.specializationName)) {
        this.alertMessage = `Please enter Specialization - ${index + 1}!!`
        flag = false;
        return;
      }
      if (!this.validationService.validateTeamActivity(spec.specializationName)) {
        this.alertMessage = `Please enter valid Specialization - ${index + 1}!!`
        flag = false;
        return;
      }
    });

    if (!flag) {
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else{
      return true;
    }
  }

  createDomain(template: TemplateRef<any>){

    let inputValidated: boolean = this.validateDomainObj(this.domainObj, template)
    if (!inputValidated) return;

    this.domainObj.allSpecializationList = this.allSpecializationList;
    this.domainObj.createdBy = this.currentUser.empId;
    this.domainService.createDomain(this.domainObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showAllDomain();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  updateDomain(template: TemplateRef<any>){
    let inputValidated: boolean = this.validateDomainObj(this.domainObj, template)
    if (!inputValidated) return;

    this.domainObj.allSpecializationList = this.allSpecializationList;
    this.domainObj.updatedBy = this.currentUser.empId;
    
    this.domainService.updateDomain(this.domainObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showAllDomain();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  deleteDomain(template: TemplateRef<any>) {
    this.domainToBeDeleted.updatedBy = this.currentUser.empId;

    this.domainService.deleteDomain(this.domainToBeDeleted).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showAllDomain();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  checkDomainName(domainName:any, template: TemplateRef<any>){

    let domainObj = new Domain();
    domainObj.domainName = domainName;
    domainObj.domainId = this.domainObj.domainId;
    this.domainService.checkDomainName(this.domainObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Fail") {
        this.domainObj.domainName = '';
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  //Doamin & Specialization  :: end


  /* Filter */
  openFilterModal(template: TemplateRef<any>, columns: any[], title: any) {
    console.log("columns : ", columns);
    this.queryList = [];

    this.filterData.title = title;
    this.filterData.columns = columns;

    this.queryList = [
      { column: "Employment Status", operator: "!=", value: "InActive", conjunction: "" }
    ]; 

    this.storedDataList.forEach((data) => {
      if (data.filterName == title) {
        data.queryList.forEach((queryObj) => {
          if(queryObj.column == "Employee Id" && !queryObj.value.includes("A-")){
            queryObj.value = "A-".concat(queryObj.value);
          }

          if (queryObj.column == 'From Date' || queryObj.column == 'To Date' || queryObj.column == 'Date' || queryObj.column == 'Date Of Joining') {
            queryObj.value = (queryObj.value) ? moment(queryObj.value).format("DD-MM-YYYY") : '';
          } else if (queryObj.column == 'Created On' || queryObj.column == 'Updated On') {
            queryObj.value = (queryObj.value) ? moment(queryObj.value).format('DD-MM-YYYY HH:mm:ss') : '';
          }
        });
        this.queryList = data.queryList;
      }
    });

    this.filterData.queryList = JSON.stringify(this.queryList);

    console.log("filterData : ", this.filterData);
    this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
  }

  getCustomEmployeesList(queryObjList: any, template: TemplateRef<any>) {
    this.allEmployeeList = [];

    let queryObj = new Query();
    queryObj.queryList = queryObjList;

    if (queryObjList == '') {
      this.getAllEmployeeList();
    } else {
      this.employeeService.customQueryForEmployeeReport(queryObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allEmployeeList = response.serviceResponse;

          this.allEmployeeList = this.allEmployeeList.filter((value, index, self) =>
            index === self.findIndex((t) => (
              t.employeementId === value.employeementId
            ))
          )

          if (this.allEmployeeList.length == 0) {
            this.openAlertMod(this.alertTemplate, "No Data found")
          }
          this.allEmployeeList.forEach(employee => {
            employee.employeementId = "A-".concat(employee.employeementId);
            employee.dateOfBirth = (employee.dateOfBirth) ? moment(employee.dateOfBirth).format(AppComponent.DATE_FORMAT) : null;
            employee.dateOfJoining = (employee.dateOfJoining) ? moment(employee.dateOfJoining).format(AppComponent.DATE_FORMAT) : null;
            employee.createdOn = (employee.createdOn) ? moment(employee.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
            employee.updatedOn = (employee.updatedOn) ? moment(employee.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
          });
          console.log("allEmployeeList : ", this.allEmployeeList)
        } else {
          this.openAlertMod(template, response.serviceResponse)
        }
      });
    }
  }

  onFilterSubmit(emittedArray: any, template: TemplateRef<any>) {
    if (emittedArray[0].length != 0) {
      console.log("queryList : ", emittedArray[0]);
      this.queryList = JSON.parse(JSON.stringify(emittedArray[0]));
      this.cancelRequest();

      emittedArray[1].forEach((object) => {
        if (Object.keys(object).length !== 0) {
          if (this.storedDataList.find((x) => x.filterName == object.filterName)) {
            this.storedDataList = this.storedDataList.map(arr1 => emittedArray[1].find(arr2 => arr2.filterName === arr1.filterName) || arr1);
          } else {
            this.storedDataList.push(object);
          }
        }
      });

      emittedArray[0].forEach(query => {
        if (query.column == 'From Date' || query.column == 'To Date' || query.column == 'Date' || query.column == 'Date Of Joining') {
          query.value = (query.value) ? moment(query.value, "DD-MM-YYYY").format('YYYY-MM-DD') : '';
        } else if (query.column == 'Created On' || query.column == 'Updated On') {
          query.value = (query.value) ? moment(query.value, "DD-MM-YYYY").format('YYYY-MM-DD HH:mm:ss') : '';
        }
        
        if (query.column == 'Employee Id') {
          query.value = query.value.split("-")[1];
        }
      });

      if (this.filterData.title == 'Filter All Employee') {
        this.getCustomEmployeesList(emittedArray[0], template);
      }
    }else{
      let clearedFilter = this.storedDataList.find((filter) => filter.filterName == emittedArray[1]);
      this.storedDataList.splice(clearedFilter);

      
      if (emittedArray[1] == 'Filter All Employee') {
        this.showTable();
      }
    }
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

  openDeleteDomainMod(template: TemplateRef<any> , domain: any){
    this.domainToBeDeleted = domain;
    this.modalRef = this.modalService.show(template);
  }

  cancelApplication(){
    this.previewModalRef.hide();
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  cancelDraftRequest() {
    this.employeeObj.remarks = ''
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

  // Work History 
  toggleWorkHistorySearch(){
    this.isworkHistorySearchEnabled = !this.isworkHistorySearchEnabled;
  }

  onWorkHistorySearch(searchData){
    this.filters = searchData;
    console.log("Updated Filter : ", this.filters);
  }
}
function compare(a: number | string, b: number | string, isAsc: boolean) {	
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);	
}
