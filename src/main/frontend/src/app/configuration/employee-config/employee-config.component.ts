import { Component, OnDestroy, OnInit, Pipe, TemplateRef, ViewChild } from '@angular/core';
import { Employee } from 'src/app/models/employee';
import { EmployeeService } from 'src/app/services/employee.service';
import { ValidationService } from 'src/app/services/validation.service';
import { first } from 'rxjs/operators';
import { DatePipe } from '@angular/common';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { User } from 'src/app/models/user';
import { Feature } from 'src/app/models/feature';
import { JobRoleService } from 'src/app/services/job-role.service';
import { certification } from 'src/app/models/certification';
import { PreviousEmployer } from 'src/app/models/previousEmployer';
import { DepartmentService } from 'src/app/services/department.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';

@Component({
  selector: 'app-employee-config',
  templateUrl: './employee-config.component.html',
  styleUrls: ['./employee-config.component.css']
})
export class EmployeeConfigComponent implements OnInit {

  feature = 'Employee Config';

  //flags 
  isCreation:boolean = false;
  isUpdation: boolean = false;
  isForm: boolean = false;
  isTable: boolean = false;

  isDraft: boolean = false;
  isDraftTable: boolean = false;

  //modal 
  alertMessage:any;
  modalRef: BsModalRef = new BsModalRef();

  //Obj 
  currentUser:User;
  employeeObj:Employee = new Employee();
  allEmployeeList:any;
  managerList:any = [];
  userMapping:any = {};
  allJobRoleList:any[] = [];
  allDeptList:any[] = [];
  filteredJobRoleList:any[] = [];
  employeeDataForExcel:any[] = [];


  allCertificationList:any[] = [];
  allPreviousEmployment:any[] = [];
  updatedCertificationList:any[] = [];
  updatedPreviousEmployment:any[] = [];
  allStates:any[] = [
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
  ]

  constructor(
    private employeeService:EmployeeService,
    private validationService:ValidationService, 
    private datePipe: DatePipe,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private jobRoleService: JobRoleService,
    private departmentService: DepartmentService,
    private exportExcelService: ExportExcelService,) { 
      this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    }
 
  ngOnInit(): void {
    this.getAllJobRoleList();
    
    // Dynamic Subfeature Flags 
    let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    console.log(this.feature, this.userMapping);

    this.sectionViewInit();

    //Deafult values for dropdown
    this.employeeObj.gender = '';
    this.employeeObj.maritalStatus = '';
    // this.employeeObj.managerId = '';
  }

  ngAfterViewInit() {
    this.setCalenderMaxDate();
  }

  sectionViewInit(){
    if(this.userMapping.view_all_employee || this.userMapping.update_employee || this.userMapping.delete_employee){
      //for employee table data 
      this.showTable();
    }else if(this.userMapping.update_draft){
       //for employee draft table data 
       this.showDraftTable();
    }
  }

  setCalenderMaxDate(){
    const today = this.datePipe.transform(new Date(), 'yyyy-MM-dd');
    let DOB = document.getElementById('DOB');
    let DOJ = document.getElementById('DOJ');
    DOB?.setAttribute('max', today);
    DOJ?.setAttribute('max', today);
  }

  showCreateForm(){
    this.isForm = true;
    this.isCreation = true;

    this.isTable = false;
    this.isUpdation = false;
    this.isDraft=false;
    this.isDraftTable = false;

    this.reset();
    this.getManagerList();
    this.getAllDepartmentList();
  }

  showTable(){
    this.isTable = true;
    
    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.isDraft = false;
    this.isDraftTable = false;

    this.managerList = [];
    this.getAllEmployeeList();
  }

  showDraftTable(){
    this.isDraftTable = true;

    this.isTable = false;
    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.isDraft = false;
    
    this.getAllDraftEmployees();
  }

  reset(){
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
    
    this.allEmployeeList = [];
    this.filteredJobRoleList = [];
    this.allCertificationList= [];
    this.allPreviousEmployment= [];
    this.addInputCertificationField();
    this.addInputPreviousEmployerField();
  }

  showUpdateForm(employee:Employee){
    this.isForm = true;
    this.isTable = false;
    this.isUpdation = true;
    this.isCreation = false;
    this.isDraft= false;
    this.isDraftTable = false;

    this.getManagerList();
    this.getAllDepartmentList();
    this.allCertificationList= [];
    this.allPreviousEmployment= [];
    this.updatedCertificationList= [];
    this.updatedPreviousEmployment= [];

    this.employeeService.getEmployeeByEmpId(employee).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.employeeObj = Object.assign({}, response.serviceResponse);
        console.log("employee :", this.employeeObj);

        // Job Role
        if(this.employeeObj.departmentId){
          this.getJobRolesByDept(this.employeeObj.departmentId);
        }

        // Certifications
        if(this.employeeObj.certifications == undefined || this.employeeObj.certifications.length == 0){
          this.addInputCertificationField();
        }else{
          this.allCertificationList = this.employeeObj.certifications;
        }

        // Prev. Employment
        if(this.employeeObj.previousEmploymentList == undefined || this.employeeObj.previousEmploymentList.length == 0){
          this.addInputPreviousEmployerField();
        }else{
          this.allPreviousEmployment = this.employeeObj.previousEmploymentList;
        }
      } else {
        console.error(response.serviceResponse)
      }
    }); 
  }

  showUpdateDraftForm(employee:Employee){
    this.isDraft= true;
    this.isForm = false;
    this.isTable = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.isDraftTable = false;
    
    this.employeeObj = Object.assign({}, employee);
    // this.employeeObj.dateOfBirth = this.datePipe.transform(employee.dateOfBirth, 'yyyy-MM-dd');
    // this.employeeObj.dateOfJoining = this.datePipe.transform(employee.dateOfJoining, 'yyyy-MM-dd')
    if(this.employeeObj.departmentId){
      this.getJobRolesByDept(this.employeeObj.departmentId);
    }
    this.getManagerList();
    this.getAllDepartmentList();
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
    // newCertificationObj.certificationId = "";
    // newCertificationObj.duration = "";
    // newCertificationObj.modeOfCourse = "";

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

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.name)){
      this.alertMessage = "Please enter Full Name !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if(!this.validationService.validateAlphaWithSpace(employeeObj.name)){
      this.alertMessage = "Please enter Valid Full Name !!";
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


    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.fatherName)){
      this.alertMessage = "Please enter father name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if(!this.validationService.validateAlphaWithSpace(employeeObj.fatherName)){
      this.alertMessage = "Please enter Valid father Name !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(this.validationService.validateNullUndefinedEmptyString(employeeObj.placeOfBirth) && !this.validationService.validateAlphaWithSpace(employeeObj.placeOfBirth)){
      this.alertMessage = "Please enter Valid Place of birth !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(this.validationService.validateNullUndefinedEmptyString(employeeObj.motherTongue) && !this.validationService.validateAlphaWithSpace(employeeObj.motherTongue)){
      this.alertMessage = "Please enter Valid Mother tongue number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(this.validationService.validateNullUndefinedEmptyString(employeeObj.passportNumber) && !this.validationService.validatePassportNumber(employeeObj.passportNumber)){
      this.alertMessage = "Please enter Valid Passport number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.aadhar)){
      this.alertMessage = "Please enter aadhar card number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if(employeeObj.aadhar.toString().length != 12){
      this.alertMessage = "Please enter Valid aadhar card number !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.panNumber)){
      this.alertMessage = "Please enter PAN card number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if(!this.validationService.validatePancardNumber(employeeObj.panNumber)){
      this.alertMessage = "Please enter Valid PAN card number !!";
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

    if(this.validationService.validateNullUndefinedEmptyString(employeeObj.alternateMobileNo) && !this.validationService.validateMobileNumber(employeeObj.alternateMobileNo)){
      this.alertMessage = "Please enter valid alternate mobile number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } 

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.permanentAddress)){
      this.alertMessage = "Please enter permanent address !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(this.validationService.validateNullUndefinedEmptyString(employeeObj.emergencyContactPerson) && !this.validationService.validateAlphaWithSpace(employeeObj.emergencyContactPerson)){
      this.alertMessage = "Please enter Valid Emergency Contact Person Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(this.validationService.validateNullUndefinedEmptyString(employeeObj.relation) && !this.validationService.validateAlphaWithSpace(employeeObj.relation)){
      this.alertMessage = "Please enter Valid Emergency Contact Person Relation !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(this.validationService.validateNullUndefinedEmptyString(employeeObj.emergencyContactMobile) && !this.validationService.validateMobileNumber(employeeObj.emergencyContactMobile)){
      this.alertMessage = "Please enter Valid Emergency Contact Person Mobile Number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.dateOfJoining)){
      this.alertMessage = "Please enter date of joining !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    
    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.employmentstatus)){
      this.alertMessage = "Please enter employment status !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.noticePeriod)){
      this.alertMessage = "Please enter notice period !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.managerId)){
      this.alertMessage = "Please select manager !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.departmentId)){
      this.alertMessage = "Please select Department !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.jobRoleId)){
      this.alertMessage = "Please select Job Role !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.role)){
      this.alertMessage = "Please select employee Role !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.experience)){
      this.alertMessage = "Please select experience !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.workLocation)){
      this.alertMessage = "Please select employee Work Location !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(this.validationService.validateNullUndefinedEmptyString(employeeObj.bankName) && !this.validationService.validateAlphaWithSpace(employeeObj.bankName)){
      this.alertMessage = "Please enter Valid Bank Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(this.validationService.validateNullUndefinedEmptyString(employeeObj.bankAccountNo) && !this.validationService.validateAlphaNumeric(employeeObj.bankAccountNo)){
      this.alertMessage = "Please enter Valid Bank Account Number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    
    if(this.validationService.validateNullUndefinedEmptyString(employeeObj.bankIFSCCode) && !this.validationService.validateAlphaNumeric(employeeObj.bankIFSCCode)){
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

  // CRUD 
  onCreateEmployee(template: TemplateRef<any>){
    console.log("allCertificationList : ", this.allCertificationList);
    console.log("allPreviousEmployment : ", this.allPreviousEmployment);
   
    let inputValidated:boolean  = this.validateEmployeeObj(this.employeeObj, template)
    if(!inputValidated) return;
   
    this.employeeObj.isDraft = false;
    // // transform date formats to dd-MM-yyyy
    // this.employeeObj.dateOfBirth = this.datePipe.transform(this.employeeObj.dateOfBirth, 'dd-MM-yyyy');
    // this.employeeObj.dateOfJoining = this.datePipe.transform(this.employeeObj.dateOfJoining, 'dd-MM-yyyy')

    this.employeeObj.certifications = (Object.keys(this.allCertificationList[0]).length === 0) ? null : this.allCertificationList;
    this.employeeObj.previousEmploymentList = (Object.keys(this.allPreviousEmployment[0]).length === 0) ? null : this.allPreviousEmployment;
    this.employeeObj.createdBy = this.currentUser.empId;
    console.log("Create Employe : ", this.employeeObj);
    this.employeeService.createEmployee(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.employeeService.deleteDraftEmployee(this.employeeObj); // deleting draft once employee is created
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }

    });
  }

  checkEmail(template: TemplateRef<any>){
    this.employeeService.checkEmployeeEmail(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Fail") {
              this.openAlertMod(template, response.serviceResponse);
            }
    });
  }

  onUpdateEmployee(template: TemplateRef<any>){

    let inputValidated:boolean  = this.validateEmployeeObj(this.employeeObj, template)
    if(!inputValidated) return;
    
    this.employeeObj.isDraft = false;
    // // transform date formats to dd-MM-yyyy
    // this.employeeObj.dateOfBirth = this.datePipe.transform(this.employeeObj.dateOfBirth, 'dd-MM-yyyy');
    // this.employeeObj.dateOfJoining = this.datePipe.transform(this.employeeObj.dateOfJoining, 'dd-MM-yyyy');

    this.allCertificationList.forEach(certificaiton => {
      console.log("All certificaiton : ", this.allCertificationList);
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

    this.employeeObj.certifications = (Object.keys(this.allCertificationList[0]).length === 0) ? null : this.allCertificationList;
    this.employeeObj.previousEmploymentList = (Object.keys(this.allPreviousEmployment[0]).length === 0) ? null : this.allPreviousEmployment;
    this.employeeObj.updatedCertifications = (this.updatedCertificationList.length === 0) ? null : this.updatedCertificationList;
    this.employeeObj.updatedPreviousEmploymentList = (this.updatedPreviousEmployment.length === 0) ? null : this.updatedPreviousEmployment;

    this.employeeObj.updatedBy = this.currentUser.empId;;
    console.log("Update Employe : ", this.employeeObj);
    this.employeeService.updateEmployee(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onDeleteEmployee(template: TemplateRef<any>){
    this.cancelRequest();
  
    this.employeeService.deleteEmployee(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  getAllEmployeeList(){
    this.allEmployeeList = [];

    this.employeeService.getAllEmployees().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allEmployeeList = response.serviceResponse;
        console.log("allEmployeeList : ", this.allEmployeeList)
       // this.createEmployeeList(this.allEmployeeList)
      } else {
        alert(response.serviceResponse)
      }
    });
  }

  createEmployeeList(allEmployeeList:any){
    this.managerList = allEmployeeList.map(employee => {
      let emp = {name : employee.name, empId: employee.empId.toString()};
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
       const onlySpecificDataArr: Partial<Employee>[] = this.employeeDataForExcel.map(	
          x => ({	
            empId: x.empId,	
            name: x.name,	
            email: x.email,	
            employmentstatus: x.employmentstatus,	
            dateOfJoining: x.dateOfJoining	
          })	
        )	
        this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr,this.name)	
    });	
  }

  // For Manager List
  getManagerList(){
    this.managerList = [];
    let employeeList = [];

    this.employeeService.getAllEmployeesByRole().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        employeeList = response.serviceResponse;
        console.log("employeeList By Role : ", employeeList)
        this.managerList = employeeList.filter(emp => emp.role == "Manager");
        console.log("managerList : ", this.managerList)
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  /* Employee Draft */
  onSaveDraftEmployee(template: TemplateRef<any>){
    this.employeeObj.isDraft = true;
    // // transform date formats to dd-MM-yyyy
    // this.employeeObj.dateOfBirth = this.datePipe.transform(this.employeeObj.dateOfBirth, 'dd-MM-yyyy');
    // this.employeeObj.dateOfJoining = this.datePipe.transform(this.employeeObj.dateOfJoining, 'dd-MM-yyyy')

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

  onUpdateDraftEmployee(template:TemplateRef<any>){
    this.employeeObj.isDraft = true;
    // // transform date formats to dd-MM-yyyy
    // this.employeeObj.dateOfBirth = this.datePipe.transform(this.employeeObj.dateOfBirth, 'dd-MM-yyyy');
    // this.employeeObj.dateOfJoining = this.datePipe.transform(this.employeeObj.dateOfJoining, 'dd-MM-yyyy')

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

  onDeleteDraftEmployee(template: TemplateRef<any>){
    this.cancelRequest();
  
    this.employeeService.deleteDraftEmployee(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showDraftTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  getAllDraftEmployees(){
    this.allEmployeeList = [];

    this.employeeService.getAllDraftEmployees().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allEmployeeList = response.serviceResponse;
        console.log("allDraftEmployeeList : ", this.allEmployeeList)
      } else {
        alert(response.serviceResponse)
      }
    });
  }

  // Job Role
  getAllJobRoleList(){
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

  getAllDepartmentList(){
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

  getJobRolesByDept(departmentId:any){
    console.log("departmentId : ", departmentId);
    console.log("this.allJobRoleList : ", this.allJobRoleList);
    
    
    this.filteredJobRoleList = [];
    this.filteredJobRoleList = this.allJobRoleList.filter(jobRole => jobRole.departmentId == departmentId);
    console.log("filteredJobRoleList : ", this.filteredJobRoleList);
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

  cancelRequest() {
    this.modalRef.hide();
  }

  //pagination 	
  page = 1;	
  handlePageChange(event) {	
    this.page = event;	
  }

}
