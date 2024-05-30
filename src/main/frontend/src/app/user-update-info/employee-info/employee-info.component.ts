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
    //console.log("employeeObj :: ", this.employeeObj);

    //console.log(this.allChildList, " allChildList");

    // this.currentEmployeeInfo = this.updateUserInfoService.getUserInfoObj();
    // //console.log("Employee info IN PREVIEW ==> ", this.currentEmployeeInfo);
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

    if(this.employeeObj.child1 == null && this.employeeObj.child2 == null && this.employeeObj.child3 == null ){
      this.addInputChildField();
    }else{
      let childList = [this.employeeObj.child1, this.employeeObj.child2 , this.employeeObj.child3];
      childList.forEach(child => {
        if(child != null){
          this.allChildList.push(new Child(child));
        }
        // if(child != null){
        //   this.allChildList.push(new Child(child));
        // }else{
        //   this.addInputChildField();
        //   return;
        // }
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

  preventDecimalOnNumberInput(event:any){
    //console.log(" check entered key",event.key)
    if(event.key==='.'){
      event.preventDefault();
    }
  }

  // added by anurag

  fieldRestictCharacterForNumber(event){
    var k;
    k = event.charCode;

    if(k== 69 || k == 101){
      return false;
    }else{
      return true;
    }

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
    //console.log(`Get Experience : ${fromDate} - ${toDate} ==>  ${diffDuration.years()} years ${diffDuration.months()} months ===>  ${diffDuration.years()}.${diffDuration.months()} for ID : YOE-${yearsOfExperienceId}`);

    // //console.log(diffDuration.years()); // years
    // //console.log(diffDuration.months()); // months
    // //console.log(diffDuration.days()); // days
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

  // calculate Experience in Years from DOJ and DOR for prev. employers
  setExperienceForPrevEmployer(previousEmployer:PreviousEmployer){
    if(previousEmployer.dateOfJoining != null && previousEmployer.dateOfRelieving){
      const fromDate = moment(new Date(previousEmployer.dateOfJoining));
      const toDate = moment(new Date(previousEmployer.dateOfRelieving));
  
      const diffDuration = moment.duration(toDate.diff(fromDate));
      //console.log(`Get Experience : ${fromDate} - ${toDate} ==>  ${diffDuration.years()} years ${diffDuration.months()} months ===>  ${diffDuration.years()}.${diffDuration.months()} for ID :`);
      const experience  = `${diffDuration.years()}.${diffDuration.months()}`;
      previousEmployer.yearsOfExperience = experience;
    }
  }

  resetExperienceOnDOJChange(previousEmployer:PreviousEmployer){
    previousEmployer.dateOfRelieving = "";
    previousEmployer.yearsOfExperience = "";
  }

  // Validations
  checkEmployeeAadharNumber(template: TemplateRef<any>) {
    if(this.employeeObj.aadhar != null){
      this.employeeObj.aadhar = String(this.employeeObj.aadhar)?.trim();
      this.employeeObj.empId = this.currentUser.empId;

      this.employeeService.checkEmployeeAadharNumber(this.employeeObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Fail") {
          this.openAlertMod(template, response.serviceResponse);
          this.employeeObj.aadhar = '';
        }
      });
    }
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



  validateEmployeeObj(employeeObj:Employee, template: TemplateRef<any>) {

    employeeObj.certifications = (Object.keys(this.allCertificationList[0]).length === 0) ? null : this.allCertificationList;
    employeeObj.previousEmploymentList = (Object.keys(this.allPreviousEmployment[0]).length === 0) ? null : this.allPreviousEmployment;

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.viewsOnOrganisation)){
      this.alertMessage = "Please enter your view on organisation !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateViewsOnOrganisation(employeeObj.viewsOnOrganisation)){
      this.alertMessage = `Please enter valid view on organisation. Alphabets, Numbers and allowed Special Character are +-()'"?,&.!`;
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.aboutMe)){
      this.alertMessage = "Please enter About me !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateViewsOnOrganisation(employeeObj.aboutMe)){
      this.alertMessage = `Please enter valid in  About me. Alphabets, Numbers and allowed Special Character are +-()'"?,&.!`;
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.gender)){
      this.alertMessage = "Please select gender !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.bloodGroup)){
      this.alertMessage = "Please enter Blood Group !! !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
     } else if (this.validationService.validateBloodGroup(employeeObj.bloodGroup) && !this.validationService.validateBloodGroup(employeeObj.bloodGroup)) {
      this.alertMessage = "Please enter Valid Blood Group !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.maritalStatus)){
      this.alertMessage = "Please enter select marital status !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if(employeeObj.maritalStatus == 'married'){
      if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.spouse)){
        this.alertMessage = "Please enter spouse name !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      if(!this.validationService.validateAlphaWithSpace(employeeObj.spouse)){
        this.alertMessage = "Please enter valid spouse name !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      // //console.log("  employeeObj.child1 ",employeeObj.child1);
      // if(!this.validationService.validateAlphaWithSpace(employeeObj.child1)){
      //   this.alertMessage = "Please enter valid child1 anurag name !!"
      //   this.openAlertMod(template, this.alertMessage);
      //   return false;
      // }
      // if(!this.validationService.validateAlphaWithSpace(employeeObj.child2)){
      //   this.alertMessage = "Please enter valid child2 name !!"
      //   this.openAlertMod(template, this.alertMessage);
      //   return false;
      // }
      // if(!this.validationService.validateAlphaWithSpace(employeeObj.child3)){
      //   this.alertMessage = "Please enter valid child3 name !!"
      //   this.openAlertMod(template, this.alertMessage);
      //   return false;
      // }

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
    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.mothersName)){
      this.alertMessage = "Please enter mother name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if(!this.validationService.validateAlphaWithSpace(employeeObj.mothersName)){
      this.alertMessage = "Please enter Valid mother Name !!";
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

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.motherTongue)){
      this.alertMessage = "Please enter Mother tongue !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if(!this.validationService.validateAlphaWithSpace(employeeObj.motherTongue)){
      this.alertMessage = "Please enter Valid  Mother tongue !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(this.validationService.validateNullUndefinedEmptyString(employeeObj.passportNumber)){
      if(!this.validationService.validateAlphaNumeric(employeeObj.passportNumber)){
        this.alertMessage = "Please enter Valid Passport number !!";
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.aadhar)){
      this.alertMessage = "Please enter aadhar card number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if (employeeObj.aadhar.toString().length != 12) {
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

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.address)){
      this.alertMessage = "Please enter Current Address !!"
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

    if(this.validationService.validateNullUndefinedEmptyString(employeeObj.alternateMobileNo)){
      if(!this.validationService.validateMobileNumber(employeeObj.alternateMobileNo)){
        this.alertMessage = "Please enter Valid Alternate Number !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
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
      this.alertMessage = "Please enter Emergency Contact Person Number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else  if(!this.validationService.validateMobileNumber(employeeObj.emergencyContactMobile)){
      this.alertMessage = "Please enter Valid Emergency Contact Person Number !!"
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
    }else if(!this.validationService.validatePassingGrade(employeeObj.passingGrade)){
      this.alertMessage = "Please Enter Valid Passing Grade !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }



    // if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.experience)){
    //   this.alertMessage = "Please Select Experience !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }
    let certFlag = true;
    if(employeeObj.certifications && employeeObj.certifications.length !== 0){
      employeeObj.certifications.forEach((certification:certification, index) =>{
        if(!this.validationService.validateNullUndefinedEmptyString(certification.certificationName)){
          this.alertMessage = `Please Enter Certification Name - ${index+1}!!`;
          this.openAlertMod(template, this.alertMessage);
          certFlag = false;
          return;
        }
        // if(!this.validationService.validateAlphabetAtLeastTwoCharacter(certification.certificationName)){
        //   this.alertMessage = `Please Enter valid Certification Name - ${index+1}!!`;
        //   this.openAlertMod(template, this.alertMessage);
        //   certFlag = false;
        //   return;
        // }

        if(!this.validationService.validateNullUndefinedEmptyString(certification.duration)){
          this.alertMessage = `Please Select Duration - ${index+1}!!`;
          this.openAlertMod(template, this.alertMessage);
          certFlag = false;
          return;
        }

        if(!this.validationService.validateNullUndefinedEmptyString(certification.modeOfCourse)){
          this.alertMessage = `Please Select Mode Of Course - ${index+1}!!`;
          this.openAlertMod(template, this.alertMessage);
          certFlag = false;
          return;
        }

        if(!this.validationService.validateNullUndefinedEmptyString(certification.dateOfCompletion)){
          this.alertMessage = `Please Select Date of Completion - ${index+1}!!`;
          this.openAlertMod(template, this.alertMessage);
          certFlag = false;
          return;
        }

        if(!this.validationService.validateNullUndefinedEmptyString(certification.certificationNumber)){
          this.alertMessage = `Please Enter Certification Number - ${index+1}!!`;
          this.openAlertMod(template, this.alertMessage);
          certFlag = false;
          return;
        }
        if(!this.validationService.validateAlphaNumeric(certification.certificationNumber)){
          this.alertMessage = `Please Enter valid Certification Number - ${index+1}!!`;
          this.openAlertMod(template, this.alertMessage);
          certFlag = false;
          return;
        }
      });
    }

    if(!certFlag) return false;
    //console.log("cert flag ",certFlag);

    let prevFlag = true;
    if(employeeObj.experience == 'Experienced'){
      if(employeeObj.previousEmploymentList && employeeObj.previousEmploymentList.length === 0){
        this.alertMessage = `Please Enter Previous Employment Details !!`;
        this.openAlertMod(template, this.alertMessage);
        return false;
      }else{
        if (employeeObj.previousEmploymentList == null) {
          this.alertMessage = `Please Enter Previous Employment Details !!`;
          this.openAlertMod(template, this.alertMessage);
          return false;
        }else{
          employeeObj.previousEmploymentList.forEach((previousEmployer:PreviousEmployer, index) =>{
            if(!this.validationService.validateNullUndefinedEmptyString(previousEmployer.employerName)){
              this.alertMessage = `Please Enter Employer Name - ${index+1}!!`;
              this.openAlertMod(template, this.alertMessage);
              prevFlag = false;
              return;
            }else   if(!this.validationService.validateEmployerName(previousEmployer.employerName)){
              this.alertMessage = `Please Enter valid Employer Name - ${index+1}!!`;
              this.openAlertMod(template, this.alertMessage);
              prevFlag = false;
              return;
            }

            if(!this.validationService.validateNullUndefinedEmptyString(previousEmployer.dateOfJoining)){
              this.alertMessage = `Please Enter Date of Joining - ${index+1}!!`
              this.openAlertMod(template, this.alertMessage);
              prevFlag = false;
              return;
            }

            if(!this.validationService.validateNullUndefinedEmptyString(previousEmployer.dateOfRelieving)){
              this.alertMessage = `Please Enter Date of Relieving - ${index+1}!!`
              this.openAlertMod(template, this.alertMessage);
              prevFlag = false;
              return;
            }

            if(!this.validationService.validateNullUndefinedEmptyString(previousEmployer.yearsOfExperience)){
              this.alertMessage = `Please Enter Years of Experience - ${index+1}!!`
              this.openAlertMod(template, this.alertMessage);
              prevFlag = false;
              return;
            }else if(!this.validationService.validateExperiencedNumber(previousEmployer.yearsOfExperience)){
				this.alertMessage = `Please Enter valid Years of Experience - ${index+1}!!`
              	this.openAlertMod(template, this.alertMessage);
              	prevFlag = false;
              	return;
            }if (previousEmployer.yearsOfExperience > 60) {
              this.alertMessage = `Please Enter value 1 to 60(yrs) in Years of Experience - ${index+1}!!`
              this.openAlertMod(template, this.alertMessage);
              prevFlag = false;
              return;
            }

            if(!this.validationService.validateNullUndefinedEmptyString(previousEmployer.designation)){
              this.alertMessage = `Please Enter Designation - ${index+1}!!`
              this.openAlertMod(template, this.alertMessage);
              prevFlag = false;
              return;
            }else if(!this.validationService.validateAlphaWithSpace(previousEmployer.designation)){
              this.alertMessage = `Please Enter valid Designation - ${index+1}!!`
              this.openAlertMod(template, this.alertMessage);
              prevFlag = false;
              return;
            }

            if(!this.validationService.validateNullUndefinedEmptyString(previousEmployer.managerName)){
              this.alertMessage = `Please Enter Manager Name - ${index+1}!!`
              this.openAlertMod(template, this.alertMessage);
              prevFlag = false;
              return;
            }else   if(!this.validationService.validateAlphabeticCharacters(previousEmployer.managerName)){
              this.alertMessage = `Please Enter Manager Name - ${index+1}!!`
              this.openAlertMod(template, this.alertMessage);
              prevFlag = false;
              return;
            }

            if(!this.validationService.validateNullUndefinedEmptyString(previousEmployer.managerContactNumber)){
              this.alertMessage = `Please Enter Manager Contact Number - ${index+1}!!`
              this.openAlertMod(template, this.alertMessage);
              prevFlag = false;
              return;
            }else  if(!this.validationService.validateMobileNumber(previousEmployer.managerContactNumber)){
              this.alertMessage = `Please Enter valid Manager Contact Number - ${index+1}!!`
              this.openAlertMod(template, this.alertMessage);
              prevFlag = false;
              return;
            }

            if(!this.validationService.validateNullUndefinedEmptyString(previousEmployer.hrName)){
              this.alertMessage = `Please Enter HR Name - ${index+1}!!`
              this.openAlertMod(template, this.alertMessage);
              prevFlag = false;
              return;
            }else  if(!this.validationService.validateAlphabeticCharacters(previousEmployer.hrName)){
              this.alertMessage = `Please Enter valid HR Name - ${index+1}!!`
              this.openAlertMod(template, this.alertMessage);
              prevFlag = false;
              return;
            }

            if(!this.validationService.validateNullUndefinedEmptyString(previousEmployer.hrContactNumber)){
              this.alertMessage = `Please Enter HR Contact Number - ${index+1}!!`
              this.openAlertMod(template, this.alertMessage);
              prevFlag = false;
              return;
            }else if(!this.validationService.validateMobileNumber(previousEmployer.hrContactNumber)){
              this.alertMessage = `Please Enter valid HR Contact Number - ${index+1}!!`
              this.openAlertMod(template, this.alertMessage);
              prevFlag = false;
              return;
            }
          });
          if(!prevFlag) return false;
        }
      }
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.bankName)){
      this.alertMessage = "Please enter Bank Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }if(!this.validationService.validateOrgName(employeeObj.bankName)){
      this.alertMessage = "Please enter Valid Bank Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.bankAccountNo)){
      this.alertMessage = "Please enter Bank Account Number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }if(!this.validationService.validateAccountNumber(employeeObj.bankAccountNo)){
      this.alertMessage = " Bank Account Number is valid in between 9 to 17 digit !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.bankIFSCCode)){
      this.alertMessage = "Please enter Bank IFSC Code !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if(!this.validationService.validateIFSCCodeRegex(employeeObj.bankIFSCCode)){
      this.openAlertMod(template , 'Please Enter Valid IFSC Code !!');
      return false;
    }


    if(this.validationService.validateNullUndefinedEmptyString(employeeObj.pfAccountNumber)){
      if(!this.validationService.validatePfAccountNumber(employeeObj.pfAccountNumber)) {
        this.openAlertMod(template, 'Please Enter Valid PF Account Number !!')
        return false;
      }
    }
    
    if(this.validationService.validateNullUndefinedEmptyString(employeeObj.previousPfAccountNumber)){
      if(!this.validationService.validatePfAccountNumber(employeeObj.previousPfAccountNumber)) {
        this.openAlertMod(template, 'Please Enter Valid Previous PF Account Number !!')
        return false;
      }
    }
    
    if(this.validationService.validateNullUndefinedEmptyString(employeeObj.uan)){
      if(!this.validationService.validateUAN(employeeObj.uan)) {
        this.openAlertMod(template, 'Please Enter Valid UAN Number !!')
        return false;
      }
    }
   
    if(this.validationService.validateNullUndefinedEmptyString(employeeObj.esicNumber)){
      if(!this.validationService.validateESICNumber(employeeObj.esicNumber)) {
        this.openAlertMod(template, 'Please Enter Valid ESIC Number !!')
        return false;
      }
    }

    return true;
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

    let inputValidated: boolean = this.validateEmployeeObj(this.employeeObj, template)
    if (!inputValidated) return;

    // this.router.navigate(['../document-upload'], {relativeTo:this.route});
    const dateFormat = 'YYYY-MM-DD';
    // transform date formats to YYYY-MM-DD
    this.employeeObj.dateOfBirth = moment(this.employeeObj.dateOfBirth).format(dateFormat);
    // this.employeeObj.dateOfJoining = moment(this.employeeObj.dateOfJoining).format(dateFormat);

    this.allCertificationList?.forEach(certificaiton => {
      //console.log("All certificaiton : ", this.allCertificationList);
      if(certificaiton.dateOfCompletion == 'Invalid date' || certificaiton.dateOfCompletion == ''){
        certificaiton.dateOfCompletion = null;
      }
      //console.log(certificaiton.dateOfCompletion, " : certificaiton.dateOfCompletion after")
      if ((certificaiton != undefined && Object.keys(certificaiton).length !== 0) && (certificaiton.employeeCertificateId == undefined || certificaiton.employeeCertificateId == null)) {
        if(certificaiton.dateOfCompletion != null){
          certificaiton.dateOfCompletion = moment(certificaiton.dateOfCompletion).format(dateFormat);
        }
        //console.log("New certificaiton : ", certificaiton);
        this.updatedCertificationList.push(certificaiton);
      }
    });

    this.allPreviousEmployment?.forEach(prevEmployer => {
      //console.log("All Prev Employer : ", this.allPreviousEmployment);
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
        //console.log("New Prev Employer : ", prevEmployer);
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

    //console.log("onSave --> employeeObj : ", this.employeeObj);

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

  validateIfscCode(template: TemplateRef<any>, ifscCode:any){
    this.employeeObj.bankName = this.employeeObj.bankName.trim();
    this.employeeObj.bankIFSCCode = this.employeeObj.bankIFSCCode.trim();

    if(this.employeeObj.bankIFSCCode){
      fetch('https://ifsc.razorpay.com/'+ifscCode).then(res => res.json()).then(data => {
        
        if(data == "Not Found"){
          this.openAlertMod(template, 'Please enter valid IFSC code');
          this.employeeObj.bankIFSCCode = "";
        }else if(data && this.employeeObj.bankName){
          let name = data.BANK;
          
          if(this.employeeObj.bankName.localeCompare(name, undefined, { sensitivity: 'accent' }) !== 0){
            this.openAlertMod(template,  `Please enter valid IFSC code, entered IFSC Code belongs to ${name}`);
            this.employeeObj.bankIFSCCode = "";
          }
        }
      });
    }
  }

  // Validations

  validateBloodGroup(event, data:any){
    this.employeeObj.bloodGroup = this.employeeObj.bloodGroup?.trim();
    //console.log("Element :", event.target);
    //console.log("Sibling : ", event.target.nextElementSibling);
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
 else if (!this.validationService.validateTeamName(data)) {
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
 else if (!this.validationService.validateTeamName(data)) {
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
  else if (!this.validationService.validateTeamName(data)) {
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
      this.errorMsg = "Please enter mother tongue !!"
  }
   else if (!this.validationService.validateAlphaWithSpace(data)) {
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
    if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please enter aadhar card number !!"
    }
    else if (data.toString().length != 12) {
      this.errorMsg = "Please enter Valid aadhar card number !!";
    }
    else {
      this.errorMsg = ""
    }
    if (this.errorMsg === "") {
      event.target.nextElementSibling.textContent = '';
    } else {
      event.target.nextElementSibling.textContent = this.errorMsg
    }
  }

  validatePan(event, data:any){
    data = data?.trim();
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
      this.errorMsg = "Please enter pincode !!"
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

  validateViewOnOrganisation(event, data: any) {
    this.employeeObj.viewsOnOrganisation = this.employeeObj.viewsOnOrganisation?.trim();
    if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please enter views On Organisation!";
    }
    else {
      this.errorMsg = ""
    }

    if (this.errorMsg == "") {
      event.target.nextElementSibling.textContent = ""
    } else {
      event.target.nextElementSibling.textContent = this.errorMsg
    }
    //   else  if (!this.validationService.validateAlphabeticCharacters(data)) {
    //     this.errorMsg = "Please enter valid views On Organisation !!"
    // }
  }

  validateAboutMe(event , data:any){
    this.employeeObj.aboutMe = this.employeeObj.aboutMe?.trim();
    if (!this.validationService.validateNullUndefinedEmptyString(data)) {
      this.errorMsg = "Please enter About me !!"
    }
    else  if (!this.validationService.validateViewsOnOrganisation(data)) {
      this.errorMsg = `Please enter valid About me. Alphabets, Number and allowed Special Character are +-()'"?,&.!`
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

  validateValidEmptyNullUndefinedalternateMobileNo(event, data:any)
  {
    if (this.validationService.validateNullUndefinedEmptyString(data)) {
      if (!this.validationService.validateMobileNumber(data)) {
        this.errorMsg = "Please enter valid Alternate Mobile Number !!"
      }else{
        this.errorMsg = ""
      }
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
   else if (!this.validationService.validateAlphaWithSpace(data)) {
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
 else  if (!this.validationService.validateAlphaWithSpace(data)) {
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
    }else if (!this.validationService.validateMobileNumber(data)) {
        this.errorMsg = "Please enter valid emergency contact number !!"
    }else{
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
 else  if (!this.validationService.validateOrgName(data)) {
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
 else  if(!this.validationService.validateIFSCCodeRegex(data)) {

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

  validatePfAccountNumber(event, data: any) {
    if(data != null && data != '') {
      this.employeeObj.pfAccountNumber = this.employeeObj.pfAccountNumber?.trim();
      if (!this.validationService.validateNullUndefinedEmptyString(data)) {
        this.errorMsg = "Please enter PF Account Number!"
      }
      if (!this.validationService.validatePfAccountNumber(data)) {
        this.errorMsg = "Please enter valid PF Account Number!"
      } else {
        this.errorMsg = ""
      }

      if (this.errorMsg == "") {
        event.target.nextElementSibling.textContent = ""
      } else {
        event.target.nextElementSibling.textContent = this.errorMsg
      }
    }
    else{
      event.target.nextElementSibling.textContent = ""
    }
  }

  validatePreviousPfAccountNumber(event, data: any) {
    if(data != null && data != '') {
      this.employeeObj.previousPfAccountNumber = this.employeeObj.previousPfAccountNumber?.trim();
      if (!this.validationService.validateNullUndefinedEmptyString(data)) {
        this.errorMsg = "Please enter Pervious PF Account Number!"
      }
      if (!this.validationService.validatePfAccountNumber(data)) {
        this.errorMsg = "Please enter valid Pervious PF Account Number!"
      } else {
        this.errorMsg = ""
      }

      if (this.errorMsg == "") {
        event.target.nextElementSibling.textContent = ""
      } else {
        event.target.nextElementSibling.textContent = this.errorMsg
      }
    }
    else{
      event.target.nextElementSibling.textContent = ""
    }
  }

  validateUAN(event, data: any) {
    if(data != null && data != '')
    {
      this.employeeObj.uan = this.employeeObj.uan?.trim();
      if (!this.validationService.validateNullUndefinedEmptyString(data)) {
        this.errorMsg = "Please enter UAN Number!"
      }
      if (!this.validationService.validateUAN(data)) {
        this.errorMsg = "Please enter valid UAN Number !!"
      } else {
        this.errorMsg = ""
      }

      if (this.errorMsg == "") {
        event.target.nextElementSibling.textContent = ""
      } else {
        event.target.nextElementSibling.textContent = this.errorMsg
      }
    }
    else{
      event.target.nextElementSibling.textContent = ""
    }
  }

  validateEsicNumber(event, data: any) {
    if (data != null && data  != '') {
      this.employeeObj.esicNumber = this.employeeObj.esicNumber?.trim();
      if (!this.validationService.validateESICNumber(data)) {
        this.errorMsg = "Please enter valid  Esic number !!"
      }
      else {
        this.errorMsg = ""
      }
      if (this.errorMsg == "") {
        event.target.nextElementSibling.textContent = ""
      } else {
        event.target.nextElementSibling.textContent = this.errorMsg
      }
    }
    else{
      event.target.nextElementSibling.textContent = ""
    }
  }

  validateemployerName(event, data:any){
    if(!this.validationService.validateNullUndefinedEmptyString(data)){
      this.errorMsg = "Please enter Employer name !!"
    }
 else  if (!this.validationService.validateEmployerName(data)) {
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
else  if (!this.validationService.validateAlphaWithSpace(data)) {
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
else  if (!this.validationService.validateAlphaWithSpace(data)) {
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

  validateHrName(event, data:any){
    if(!this.validationService.validateNullUndefinedEmptyString(data)){
      this.errorMsg = "Please enter HR name !!"
    }
else  if (!this.validationService.validateAlphaWithSpace(data)) {
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


  // modals
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  restrictNumbersIn(event){
    var k ;
    k = event.charCode;
//console.log("  charcode   ",k);
    if((k == 33) || (k == 34) || (k == 35) || (k == 36 ) || (k == 37) ||
    (k == 38) || (k == 39) || (k == 40) || (k == 41) || (k == 42 ) ||
    (k == 43) || (k == 44) || (k == 46 ) || (k == 47) ||
      (k==48) || (k==49) || (k==50) || (k==51) || (k==52) || 
      (k==53)|| (k==54)|| (k==55)|| (k==56)|| (k==57) || 
      (k==58) || (k == 59) || (k == 60) || (k == 61) || (k == 62 ) 
      || (k == 63) || (k == 64)){
      return (false);
    }
    return (true);

  }

}


