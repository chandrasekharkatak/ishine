import { AfterContentInit, Component, EventEmitter, OnInit, Output, TemplateRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { certification } from 'src/app/models/certification';
import { Child } from 'src/app/models/Child';
import { Employee } from 'src/app/models/employee';
import { PreviousEmployer } from 'src/app/models/previousEmployer';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { UpdateUserInfoService } from 'src/app/services/updateUserInfo.service';
import { ValidationService } from 'src/app/services/validation.service';

@Component({
  selector: 'app-employee-info',
  templateUrl: './employee-info.component.html',
  styleUrls: ['./employee-info.component.css']
})
export class EmployeeInfoComponent implements OnInit{

  //modal 
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();
  errorMsg:any;
  errorMsg1:any;


  //Obj 
  currentUser: User;
  employeeObj: Employee = new Employee();
  
  allCertificationList: any[] = [];
  allPreviousEmployment: any[] = [];
  updatedCertificationList: any[] = [];
  updatedPreviousEmployment: any[] = [];
  currDate:any;
  yearOfPassingList:any[] = [];

  allChildList:any [] = [];


  @Output() loadDocumentUpload: EventEmitter<any> = new EventEmitter<any>();

  constructor(
    private employeeService: EmployeeService,
    private validationService: ValidationService,
    private modalService: BsModalService,
    private updateUserInfoService: UpdateUserInfoService,
    private authenticationService: AuthenticationService,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    this.updateUserInfoService.updateduserInfoObj.subscribe((employee:Employee)=>{
      this.sectionViewInit(employee);
    });
  }

  ngOnInit(): void {
    
    const employee:Employee = this.updateUserInfoService.getUserInfoObj();
    this.sectionViewInit(employee);
    this.setYearOfPassingList();
    console.log("employeeObj :: ", this.employeeObj);

    console.log(this.allChildList, " allChildList");
  }

  sectionViewInit(employee:Employee){
    this.allCertificationList = [];
    this.allPreviousEmployment = [];
    this.allChildList = [];
    this.employeeObj = employee;

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
    
    if(this.employeeObj.child1 == null && this.employeeObj.child2 == null && this.employeeObj.child3 ){
      this.addInputChildField();
    }else{
      let childList = [this.employeeObj.child1, this.employeeObj.child2 , this.employeeObj.child3];
      childList.forEach(child => {
        if(child != null){
          this.allChildList.push(new Child(child));
        }else{
          this.addInputChildField();
          return;
        }
      });
    }

    setTimeout(this.setCalenderMaxDate, 1000);
  }


  addInputChildField(){
    let child = new Child();
    this.allChildList.push(child);
  }

  removeInputChildField(childObj){
    this.allChildList.forEach((child, index) => {
      if (child == childObj) {
        this.allChildList.splice(index, 1);
      }
    });

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

    this.allCertificationList = [];
    this.allPreviousEmployment = [];
    this.allChildList = [];
    this.addInputCertificationField();
    this.addInputPreviousEmployerField();
    this.addInputChildField();

  }


  disableMannualDateInput() {
    return false;
  }

  setCalenderMaxDate() {
    const dateFormat = 'YYYY-MM-DD';
    const today = moment(new Date()).format(dateFormat);

    let date_inputs = document.querySelectorAll('.date-input');
    
    date_inputs.forEach(element => {
      element?.setAttribute('max', today);  
    });
  }

  currentDateFilter = (d: Date)=>{
    const dateFormat = 'YYYY-MM-DD';
    const currentDate = new Date();
    
    return (moment(d).format(dateFormat) <= moment(currentDate).format(dateFormat));
  }

  setYearOfPassingList(){
    const currentYear = new Date().getFullYear();
    for (let start = 1990; start <= currentYear; start++) {
      this.yearOfPassingList.push(start);
    }   
  }

  stringToNumber(year:any){
    this.employeeObj.yearOfPassing = Number.parseInt(year);
  }

  setExperience(dateOfJoining:any, dateOfRelieving:any, yearsOfExperienceId:any) {
    const fromDate = moment(new Date(dateOfJoining));
    const toDate = moment(new Date(dateOfRelieving));

    const diffDuration = moment.duration(toDate.diff(fromDate));
    console.log(`Get Experience : ${fromDate} - ${toDate} ==>  ${diffDuration.years()} years ${diffDuration.months()} months ===>  ${diffDuration.years()}.${diffDuration.months()} for ID : YOE-${yearsOfExperienceId}`);

    // console.log(diffDuration.years()); // years
    // console.log(diffDuration.months()); // months
    // console.log(diffDuration.days()); // days
  }

  // Manage employer
  addInputPreviousEmployerField() {
    let newPrevEmployerObj = new PreviousEmployer();
    this.allPreviousEmployment.push(newPrevEmployerObj);
    setTimeout(this.setCalenderMaxDate, 1000);
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
    this.allCertificationList.push(newCertificationObj);
    setTimeout(this.setCalenderMaxDate, 1000);
  }

  removeInputCertificationField(certificationObj) {
    this.allCertificationList.forEach((value, index) => {
      if (value == certificationObj) {
        this.updatedCertificationList.push(value);
        this.allCertificationList.splice(index, 1);
      }
    });
  }

  // Validations 
  checkEmployeeAadharNumber(template: TemplateRef<any>) {
    this.employeeObj.aadhar = this.employeeObj.aadhar?.trim();
    this.employeeService.checkEmployeeAadharNumber(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Fail") {
        this.openAlertMod(template, response.serviceResponse);
        this.employeeObj.aadhar = '';
      }
    });
  }

  checkEmployeePanNumber(template: TemplateRef<any>) {
    this.employeeObj.panNumber = this.employeeObj.panNumber?.trim();
    this.employeeService.checkEmployeePanNumber(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Fail") {
        this.openAlertMod(template, response.serviceResponse);
        this.employeeObj.panNumber = '';
      }
    });
  }


  validateBirthDate(template: TemplateRef<any>){   

    let birthdate = new Date(this.employeeObj.dateOfBirth);
    let dtCurrent = new Date();

    if (dtCurrent.getFullYear() - birthdate.getFullYear() < 18) {
      this.employeeObj.dateOfBirth = undefined;
      this.openAlertMod(template, 'Employee age cannot be less than 18 years.');
      return false;
     }
    if (dtCurrent.getFullYear() - birthdate.getFullYear() == 18) {

      //CD: 11/06/2018 and DB: 15/07/2000. Will turned 18 on 15/07/2018.
      if (dtCurrent.getMonth() < birthdate.getMonth()) {
        this.employeeObj.dateOfBirth = undefined;
        this.openAlertMod(template, 'Employee age cannot be less than 18 years.');      
        return false;
      }
      if (dtCurrent.getMonth() == birthdate.getMonth()) {
          //CD: 11/06/2018 and DB: 15/06/2000. Will turned 18 on 15/06/2018.
          if (dtCurrent.getDate() < birthdate.getDate()) {
            this.employeeObj.dateOfBirth = undefined;
            this.openAlertMod(template, 'Employee age cannot be less than 18 years.');  
            return false;
          }
      }
   }
    if (dtCurrent.getFullYear() - birthdate.getFullYear() > 60) {
      this.employeeObj.dateOfBirth = undefined;
      this.openAlertMod(template, 'Employee age cannot be more than 60 years.');
      this.employeeObj.dateOfBirth = '';
      return false;
    }
  }

  dataClean(value:string){
    if(value == 'No'){
      this.employeeObj.yearOfPassing = ''
    }
  }
  

  async onSave(template : TemplateRef<any>){

    this.employeeObj.viewsOnOrganisation = this.employeeObj.viewsOnOrganisation?.trim();
    this.employeeObj.aboutMe = this.employeeObj.aboutMe?.trim();
    this.employeeObj.bloodGroup = this.employeeObj.bloodGroup?.trim();
    this.employeeObj.fatherName = this.employeeObj.fatherName?.trim();
    this.employeeObj.mothersName = this.employeeObj.mothersName?.trim();
    this.employeeObj.placeOfBirth = this.employeeObj.placeOfBirth?.trim();
    this.employeeObj.motherTongue = this.employeeObj.motherTongue?.trim();
    this.employeeObj.passportNumber = this.employeeObj.passportNumber?.trim();
    
    this.employeeObj.panNumber = this.employeeObj.panNumber?.trim();
    this.employeeObj.spouse = this.employeeObj.spouse?.trim();
    this.employeeObj.child1 = this.employeeObj.child1?.trim();
    this.employeeObj.child2 = this.employeeObj.child2?.trim();
    this.employeeObj.child3 = this.employeeObj.child3?.trim();
    this.employeeObj.address = this.employeeObj.address?.trim();
    this.employeeObj.permanentAddress = this.employeeObj.permanentAddress?.trim();
    this.employeeObj.emergencyContactPerson = this.employeeObj.emergencyContactPerson?.trim();
    this.employeeObj.relation = this.employeeObj.relation?.trim();
    this.employeeObj.passingGrade = this.employeeObj.passingGrade?.trim();
    this.employeeObj.bankName = this.employeeObj.bankName?.trim();
    this.employeeObj.bankAccountNo = this.employeeObj.bankAccountNo?.trim();
    this.employeeObj.bankIFSCCode = this.employeeObj.bankIFSCCode?.trim();
    this.employeeObj.esicNumber = this.employeeObj.esicNumber?.trim();


    this.employeeObj.previousEmploymentList?.forEach((x)=>{
      x.employerName = x.employerName?.trim();
      x.designation = x.designation?.trim();
      x.managerName = x.managerName?.trim();
      x.hrName = x.hrName?.trim();
    })

    this.employeeObj.certifications?.forEach((y)=>{
      y.certificationName = y.certificationName?.trim();
      y.certificationNumber = y.certificationNumber?.trim();
    })


    // this.router.navigate(['../document-upload'], {relativeTo:this.route});
    const dateFormat = 'YYYY-MM-DD';
    // transform date formats to YYYY-MM-DD
    this.employeeObj.dateOfBirth = moment(this.employeeObj.dateOfBirth).format(dateFormat);
    // this.employeeObj.dateOfJoining = moment(this.employeeObj.dateOfJoining).format(dateFormat);

    this.allCertificationList?.forEach(certificaiton => {
      console.log("All certificaiton : ", this.allCertificationList);
      if(certificaiton.dateOfCompletion == 'Invalid date' || certificaiton.dateOfCompletion == ''){
        certificaiton.dateOfCompletion = null;
      }
      console.log(certificaiton.dateOfCompletion, " : certificaiton.dateOfCompletion after")
      if ((certificaiton != undefined && Object.keys(certificaiton).length !== 0) && (certificaiton.employeeCertificateId == undefined || certificaiton.employeeCertificateId == null)) {
        if(certificaiton.dateOfCompletion != null){
          certificaiton.dateOfCompletion = moment(certificaiton.dateOfCompletion).format(dateFormat);
        }
        console.log("New certificaiton : ", certificaiton);
        this.updatedCertificationList.push(certificaiton);
      }
    });

    this.allPreviousEmployment?.forEach(prevEmployer => {
      console.log("All Prev Employer : ", this.allPreviousEmployment);
      if(prevEmployer.dateOfJoining == 'Invalid date' || prevEmployer.dateOfJoining == ''){
        prevEmployer.dateOfJoining = null;
      }
      if(prevEmployer.dateOfRelieving == 'Invalid date' || prevEmployer.dateOfRelieving == ''){
        prevEmployer.dateOfRelieving = null;
      }
      if ((prevEmployer != undefined && Object.keys(prevEmployer).length !== 0) && (prevEmployer.previousEmploymentId == undefined || prevEmployer.previousEmploymentId == null)) {
        if(prevEmployer.dateOfJoining != null || prevEmployer.dateOfRelieving != null){
          prevEmployer.dateOfJoining = moment(prevEmployer.dateOfJoining).format(dateFormat);
          prevEmployer.dateOfRelieving = moment(prevEmployer.dateOfRelieving).format(dateFormat);
        }
        console.log("New Prev Employer : ", prevEmployer);
        this.updatedPreviousEmployment.push(prevEmployer);
      }
    });

  
    
    this.employeeObj.certifications = (Object.keys(this.allCertificationList[0]).length === 0) ? null : this.allCertificationList;
    this.employeeObj.previousEmploymentList = (Object.keys(this.allPreviousEmployment[0]).length === 0) ? null : this.allPreviousEmployment;
   
    this.employeeObj.updatedCertifications = (this.updatedCertificationList.length === 0) ? null : this.updatedCertificationList;
    this.employeeObj.updatedPreviousEmploymentList = (this.updatedPreviousEmployment.length === 0) ? null : this.updatedPreviousEmployment;

    
    for (let index = 0; index < 3; index++) {
      if(this.allChildList[index] && this.allChildList[index]?.childName !== ""){
        this.employeeObj[`child${index+1}`] = this.allChildList[index].childName;
      }else{
        this.employeeObj[`child${index+1}`] = null;
      }
    }
 
    this.employeeObj.createdBy = this.currentUser.empId;

    console.log("onSave --> employeeObj : ", this.employeeObj);
    
    this.updateUserInfoService.setUserInfoObj(this.employeeObj);
    const response = await this.updateUserInfoService.saveEmployeeInfo();
    if (response.serviceStatus == "Success") {
      this.employeeObj = await this.updateUserInfoService.getDraftByEmpId();
      this.employeeObj.empId = this.currentUser.empId;
      this.updateUserInfoService.setUserInfoObj(this.employeeObj);
      this.loadDocumentUpload.emit();
    } else {
      console.error(response.serviceResponse);
    }
  }

  addDemographiscInfo(template: TemplateRef<any>, pincode:any){
    let path;

    fetch('https://api.postalpincode.in/pincode/'+pincode).then(res => res.json()).then(data => {
      if(data[0].Status == "Success"){
        path = data[0].PostOffice[0];

        this.employeeObj.state = path.State;
        this.employeeObj.city = path.Name;
        this.employeeObj.country = path.Country;
      }else{
        this.employeeObj.state = "";
        this.employeeObj.city = "";
        this.employeeObj.country = "";
        this.openAlertMod(template, 'Please enter valid pincode');
      }
    });
  }

  // modals
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  // Validations 

  validateBloodGroup(event, data:any){
    this.employeeObj.bloodGroup = this.employeeObj.bloodGroup?.trim();
    console.log("Element :", event.target);
    console.log("Sibling : ", event.target.nextElementSibling);
    if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please enter Blood Group !!"   
  }
     else if (!this.validationService.validateBloodGroup(data)) {
        this.errorMsg = "Please enter valid Blood Group !!"   
    }
    else{
      this.errorMsg = ""
    }
    if(this.errorMsg == ""){
      event.target.nextElementSibling.textContent = ""
    }else{
      event.target.nextElementSibling.textContent =  this.errorMsg
    }
    
    
  }
  validateFathersName(event, data:any){
    this.employeeObj.fatherName = this.employeeObj.fatherName?.trim();
    if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please enter father name !!"   
  }
 else if (!this.validationService.validateStringWithNoSpaceAtBeginAndNoSingleCharacter(data)) {
    this.errorMsg = "Please enter valid father name !!"   
}
  else{
  this.errorMsg = ""
}
if(this.errorMsg == ""){
  event.target.nextElementSibling.textContent = ""
}else{
  event.target.nextElementSibling.textContent =  this.errorMsg
}
  }

  validateMothersName(event, data:any){
    this.employeeObj.mothersName = this.employeeObj.mothersName?.trim();
    if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please enter mother name !!"   
  }
 else if (!this.validationService.validateStringWithNoSpaceAtBeginAndNoSingleCharacter(data)) {
  this.errorMsg = "Please enter valid mother name !!"   
}
else{
this.errorMsg = ""
}
if(this.errorMsg == ""){
event.target.nextElementSibling.textContent = ""
}else{
event.target.nextElementSibling.textContent =  this.errorMsg
}
  }

  validatePlaceOfBirth(event, data:any){
    this.employeeObj.placeOfBirth = this.employeeObj.placeOfBirth?.trim();
    if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please enter Place of Birth !!"   
  }
  else if (!this.validationService.validateStringWithNoSpaceAtBeginAndNoSingleCharacter(data)) {
    this.errorMsg = "Please enter valid Place of birth !!"   
}
  else{
  this.errorMsg = ""
}
if(this.errorMsg == ""){
  event.target.nextElementSibling.textContent = ""
}else{
  event.target.nextElementSibling.textContent =  this.errorMsg
}
  }


  validateMotherTongue(event, data:any){
    this.employeeObj.motherTongue = this.employeeObj.motherTongue?.trim();
    if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please enter valid mother tongue !!"   
  }
   else if (!this.validationService.validateStringWithNoSpaceAtBeginAndNoSingleCharacter(data)) {
    this.errorMsg = "Please enter valid mother tongue !!"   
}
  else{
  this.errorMsg = ""
}
if(this.errorMsg == ""){
  event.target.nextElementSibling.textContent = ""
}else{
  event.target.nextElementSibling.textContent =  this.errorMsg
}
  }

//   validatePassportNumber(event, data:any){
//     if(data === null){
//       this.errorMsg = ""
//     }
//    else if (!this.validationService.validatePassportNumber(data)) {
//     this.errorMsg = "Please enter valid passport number !!"   
// }
//   else{
//   this.errorMsg = ""
// }
// if(this.errorMsg == ""){
//   event.target.nextElementSibling.textContent = ""
// }else{
//   event.target.nextElementSibling.textContent =  this.errorMsg
// }
//   }

  validateAadhar(event, data:any){
    this.employeeObj.aadhar = this.employeeObj.aadhar?.trim();
    if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please enter aadhar card number !!"   
  }
   else if (data.toString().length != 12) {
      this.errorMsg = "Please enter Valid aadhar card number !!";
  }
  else{
  this.errorMsg = ""
}
if(this.errorMsg == ""){
  event.target.nextElementSibling.textContent = ""
}else{
  event.target.nextElementSibling.textContent =  this.errorMsg
}
  }
  validatePan(event, data:any){
    if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please enter pan number !!"   
  }
   else if (!this.validationService.validatePancardNumber(data)) {
    this.errorMsg = "Please enter valid pan number !!"   
}
  else{
  this.errorMsg = ""
}
if(this.errorMsg == ""){
  event.target.nextElementSibling.textContent = ""
}else{
  event.target.nextElementSibling.textContent =  this.errorMsg
}
  }


  validatePincode(event, data:any){
    if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please enter valid father name !!"   
  }
   else if (!this.validationService.validatePincodeNumber(data)) {
    this.errorMsg = "Please enter valid pincode !!"   
}
  else{
  this.errorMsg = ""
}
if(this.errorMsg == ""){
  event.target.nextElementSibling.textContent = ""
}else{
  event.target.nextElementSibling.textContent =  this.errorMsg
}
  }

  validateViewOnOrganisation(event , data:any){
    this.employeeObj.viewsOnOrganisation = this.employeeObj.viewsOnOrganisation?.trim();
    if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please enter views On Organisation !!"   
  }
  else  if (!this.validationService.validateAlphabeticCharacters(data)) {
    this.errorMsg = "Please enter valid views On Organisation !!"   
}
  else{
  this.errorMsg = ""
}
if(this.errorMsg == ""){
  event.target.nextElementSibling.textContent = ""
}else{
  event.target.nextElementSibling.textContent =  this.errorMsg
}
  }

  validateAboutMe(event , data:any){
    this.employeeObj.aboutMe = this.employeeObj.aboutMe?.trim();
    if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please enter About me !!"   
  }
  else  if (!this.validationService.validateAlphabeticCharacters(data)) {
    this.errorMsg = "Please enter valid About me !!"   
}
  else{
  this.errorMsg = ""
}
if(this.errorMsg == ""){
  event.target.nextElementSibling.textContent = ""
}else{
  event.target.nextElementSibling.textContent =  this.errorMsg
}
  }

  validateValidEmptyNullUndefinedalternateMobileNo(event, data:any){
    this.employeeObj.emergencyContactMobile = this.employeeObj.emergencyContactMobile?.trim();
    if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please enter Mobile number !!"   
  }
  else  if (!this.validationService.validateMobileNumber(data)) {
    this.errorMsg = "Please enter valid  Mobile Number !!"   
}
  else{
  this.errorMsg = ""
}
if(this.errorMsg == ""){
  event.target.nextElementSibling.textContent = ""
}else{
  event.target.nextElementSibling.textContent =  this.errorMsg
}

  }

  validateValidEmptyNullUndefinedPermanentAddress(event , data:any){
    this.employeeObj.permanentAddress = this.employeeObj.permanentAddress?.trim();
    if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please enter Permanent Address !!"   
  }
  else  if (!this.validationService.validateActivityTimesheetDiscription(data)) {
    this.errorMsg = "Please enter valid  Permanent Address !!"   
}
  else{
  this.errorMsg = ""
}
if(this.errorMsg == ""){
  event.target.nextElementSibling.textContent = ""
}else{
  event.target.nextElementSibling.textContent =  this.errorMsg
}
  }

  validateValidEmptyNullUndefinedCurrentDate(event , data:any){
    this.employeeObj.address = this.employeeObj.address?.trim();
    if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please enter Current Address !!"   
  }
  else  if (!this.validationService.validateActivityTimesheetDiscription(data)) {
    this.errorMsg = "Please enter valid  Current Address !!"   
}
  else{
  this.errorMsg = ""
}
if(this.errorMsg == ""){
  event.target.nextElementSibling.textContent = ""
}else{
  event.target.nextElementSibling.textContent =  this.errorMsg
}
  }

  validateContactPerson(event, data:any){
    this.employeeObj.emergencyContactPerson = this.employeeObj.emergencyContactPerson?.trim();
    if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please enter Emergency Contact person !!"   
  }
   else if (!this.validationService.validateStringWithNoSpaceAtBeginAndNoSingleCharacter(data)) {
    this.errorMsg = "Please enter valid Emergency Contact person !!"   
}
  else{
  this.errorMsg = ""
}
if(this.errorMsg == ""){
  event.target.nextElementSibling.textContent = ""
}else{
  event.target.nextElementSibling.textContent =  this.errorMsg
}
  }
  validateRelation(event, data:any){
    this.employeeObj.relation = this.employeeObj.relation?.trim();
    if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please enter relation !!"   
  }
 else  if (!this.validationService.validateStringWithNoSpaceAtBeginAndNoSingleCharacter(data)) {
    this.errorMsg = "Please enter valid relation !!"   
}
  else{
  this.errorMsg = ""
}
if(this.errorMsg == ""){
  event.target.nextElementSibling.textContent = ""
}else{
  event.target.nextElementSibling.textContent =  this.errorMsg
}
  }
  validateValidEmptyNullUndefinedemergencyContactMobile(event, data:any){
    if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please enter emergency contact number !!"   
  }
 else  if (!this.validationService.validateMobileNumber(data)) {
    this.errorMsg = "Please enter valid emergency contact number !!"   
}
  else{
  this.errorMsg = ""
}
if(this.errorMsg == ""){
  event.target.nextElementSibling.textContent = ""
}else{
  event.target.nextElementSibling.textContent =  this.errorMsg
} 
  }
  validatebankName(event, data:any){
    this.employeeObj.bankName = this.employeeObj.bankName?.trim();
    if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please enter bank name !!"   
  }
 else  if (!this.validationService.validateStringWithNoSpaceAtBeginAndNoSingleCharacter(data)) {
    this.errorMsg = "Please enter valid bank name !!"   
}
  else{
  this.errorMsg = ""
}
if(this.errorMsg == ""){
  event.target.nextElementSibling.textContent = ""
}else{
  event.target.nextElementSibling.textContent =  this.errorMsg
} 
  }

  validatebankAccountNo(event, data:any){
    this.employeeObj.bankAccountNo = this.employeeObj.bankAccountNo?.trim();
    if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please enter bank account number !!"   
  }
 else  if (!this.validationService.validateAccountNumber(data)) {
    this.errorMsg = "Please enter valid  bank account number !!"   
}
  else{
  this.errorMsg = ""
}
if(this.errorMsg == ""){
  event.target.nextElementSibling.textContent = ""
}else{
  event.target.nextElementSibling.textContent =  this.errorMsg
} 
  }
  validatebankIFSCCode(event, data:any){
    this.employeeObj.bankIFSCCode = this.employeeObj.bankIFSCCode?.trim();
    if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please enter bank IFSC code !!"   
  }
 else  if (!this.validationService.validateIFSCCodeRegex(data)) {
    this.errorMsg = "Please enter valid  bank IFSC code !!"   
}
  else{
  this.errorMsg = ""
}
if(this.errorMsg == ""){
  event.target.nextElementSibling.textContent = ""
}else{
  event.target.nextElementSibling.textContent =  this.errorMsg
} 
  }
//   validatepfAccountNumber(event, data:any){
//     if(data === null){
//       this.errorMsg = ""
//     }
//  else  if (!this.validationService.validateAlphaNumeric(data)) {
//     this.errorMsg = "Please enter valid  PF account number !!"   
// }
//   else{
//   this.errorMsg = ""
// }
// if(this.errorMsg == ""){
//   event.target.nextElementSibling.textContent = ""
// }else{
//   event.target.nextElementSibling.textContent =  this.errorMsg
// } 
//   }
//   validatePreviouspfAccountNumber(event, data:any){
//     if(data === null){
//       this.errorMsg = ""
//     }
//  else  if (!this.validationService.validateAlphaNumeric(data)) {
//     this.errorMsg = "Please enter valid  PF account number !!"   
// }
//   else{
//   this.errorMsg = ""
// }
// if(this.errorMsg == ""){
//   event.target.nextElementSibling.textContent = ""
// }else{
//   event.target.nextElementSibling.textContent =  this.errorMsg
// } 
//   }
//   validateUan(event, data:any){
//     if(data === null){
//       this.errorMsg = ""
//     }
//  else  if (!this.validationService.validateAlphaNumeric(data)) {
//     this.errorMsg = "Please enter valid  UAN !!"   
// }
//   else{
//   this.errorMsg = ""
// }
// if(this.errorMsg == ""){
//   event.target.nextElementSibling.textContent = ""
// }else{
//   event.target.nextElementSibling.textContent =  this.errorMsg
// } 
//   }
  validateEsicNumber(event, data:any){
    this.employeeObj.esicNumber = this.employeeObj.esicNumber?.trim();
    if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please enter Esic number !!"   
  }
 else  if (!this.validationService.validateAlphaNumeric(data)) {
    this.errorMsg = "Please enter valid  Esic number !!"   
}
  else{
  this.errorMsg = ""
}
if(this.errorMsg == ""){
  event.target.nextElementSibling.textContent = ""
}else{
  event.target.nextElementSibling.textContent =  this.errorMsg
} 
  }
  validateemployerName(event, data:any){
    if(!this.validationService.validateNullUndefinedEmptyString(data)){
      this.errorMsg = "Please enter Employer name !!"  
    }
 else  if (!this.validationService.validateStringWithNoSpaceAtBeginAndNoSingleCharacter(data)) {
    this.errorMsg = "Please enter valid Employer name !!"   
}
  else{
  this.errorMsg = ""
}
if(this.errorMsg == ""){
  event.target.nextElementSibling.textContent = ""
}else{
  event.target.nextElementSibling.textContent =  this.errorMsg
} 
  }
  validatYearsOfExperience(event, data:any){
    if(!this.validationService.validateNullUndefinedEmptyString(data)){
      this.errorMsg = "Please enter year of experience !!"  
    }
    else if(data === 0){		
      this.errorMsg = "Please enter more than 0 number !!"	  	
  }		
      else if (!this.validationService.validateExperiencedNumber(data)) {		
      this.errorMsg = "Please enter valid experience in Format (Years.Months)  !!"		
  }else{
  this.errorMsg = ""
}
if(this.errorMsg == ""){
  event.target.nextElementSibling.textContent = ""
}else{
  event.target.nextElementSibling.textContent =  this.errorMsg
} 
  }

  validaeTotalExperience(event, data:any){
    if(!this.validationService.validateNullUndefinedEmptyString(data)){
      this.errorMsg = "Please enter Total  year of experience !!"  
    }
 else  if (!this.validationService.validateNumber(data)) {
    this.errorMsg = "Please enter valid Total  year of experience !!"   
}
  else{
  this.errorMsg = ""
}
if(this.errorMsg == ""){
  event.target.nextElementSibling.textContent = ""
}else{
  event.target.nextElementSibling.textContent =  this.errorMsg
} 
  }
  validaeDesignation(event, data:any){ 
    if(!this.validationService.validateNullUndefinedEmptyString(data)){
      this.errorMsg = "Please enter Designation !!"  
    }
else  if (!this.validationService.validateStringWithNoSpaceAtBeginAndNoSingleCharacter(data)) {
  this.errorMsg = "Please enter valid Designation !!"   
}
else{
this.errorMsg = ""
}
if(this.errorMsg == ""){
event.target.nextElementSibling.textContent = ""
}else{
event.target.nextElementSibling.textContent =  this.errorMsg
} 
  }
  validaeManagerName(event, data:any){ 
    if(!this.validationService.validateNullUndefinedEmptyString(data)){
      this.errorMsg = "Please enter manager name !!"  
    }
else  if (!this.validationService.validateStringWithNoSpaceAtBeginAndNoSingleCharacter(data)) {
  this.errorMsg = "Please enter valid manager name !!"   
}
else{
this.errorMsg = ""
}
if(this.errorMsg == ""){
event.target.nextElementSibling.textContent = ""
}else{
event.target.nextElementSibling.textContent =  this.errorMsg
} 
  }

  validaeManagerContactNumber(event, data:any){ 
    if(!this.validationService.validateNullUndefinedEmptyString(data)){
      this.errorMsg = "Please enter manager contact number !!"  
    }
else  if (!this.validationService.validateMobileNumber(data)) {
  this.errorMsg = "Please enter valid manager contact number !!"   
}
else{
this.errorMsg = ""
}
if(this.errorMsg == ""){
event.target.nextElementSibling.textContent = ""
}else{
event.target.nextElementSibling.textContent =  this.errorMsg
} 
  }

  validaeHrName(event, data:any){ 
    if(!this.validationService.validateNullUndefinedEmptyString(data)){
      this.errorMsg = "Please enter HR name !!"  
    }
else  if (!this.validationService.validateStringWithNoSpaceAtBeginAndNoSingleCharacter(data)) {
  this.errorMsg = "Please enter valid HR name !!"   
}
else{
this.errorMsg = ""
}
if(this.errorMsg == ""){
event.target.nextElementSibling.textContent = ""
}else{
event.target.nextElementSibling.textContent =  this.errorMsg
} 
  }

  validaeHrContactNumber(event, data:any){ 
    if(!this.validationService.validateNullUndefinedEmptyString(data)){
      this.errorMsg = "Please enter HR contact number !!"  
    }
else  if (!this.validationService.validateMobileNumber(data)) {
  this.errorMsg = "Please enter valid HR contact number !!"   
}
else{
this.errorMsg = ""
}
if(this.errorMsg == ""){
event.target.nextElementSibling.textContent = ""
}else{
event.target.nextElementSibling.textContent =  this.errorMsg
} 
  }
}
