import { AfterContentInit, Component, EventEmitter, OnInit, Output, TemplateRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { certification } from 'src/app/models/certification';
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

  //Obj 
  currentUser: User;
  employeeObj: Employee = new Employee();

  allCertificationList: any[] = [];
  allPreviousEmployment: any[] = [];
  updatedCertificationList: any[] = [];
  updatedPreviousEmployment: any[] = [];
  currDate:any;
  yearOfPassingList:any[] = [];

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
  }

  sectionViewInit(employee:Employee){
    this.employeeObj = employee;

    // Certifications
    if ((this.employeeObj.certifications == undefined || this.employeeObj.certifications.length == 0) && this.allCertificationList.length == 0) {
      this.addInputCertificationField();
    } else {
      this.allCertificationList = this.employeeObj.certifications;
    }

    // Prev. Employment
    if ((this.employeeObj.previousEmploymentList == undefined || this.employeeObj.previousEmploymentList.length == 0) && this.allPreviousEmployment.length == 0) {
      this.addInputPreviousEmployerField();
    } else {
      this.allPreviousEmployment = this.employeeObj.previousEmploymentList;
    }

    setTimeout(this.setCalenderMaxDate, 1000);
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
    this.addInputCertificationField();
    this.addInputPreviousEmployerField();
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
    for (let start = 1990; start < 2051; start++) {
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

  async onSave(){
    // this.router.navigate(['../document-upload'], {relativeTo:this.route});
    const dateFormat = 'YYYY-MM-DD';

    // transform date formats to YYYY-MM-DD
    this.employeeObj.dateOfBirth = moment(this.employeeObj.dateOfBirth).format(dateFormat);
    // this.employeeObj.dateOfJoining = moment(this.employeeObj.dateOfJoining).format(dateFormat);

    this.allCertificationList.forEach(certificaiton => {
      certificaiton.dateOfCompletion = moment(certificaiton.dateOfCompletion).format(dateFormat);
      console.log("All certificaiton : ", this.allCertificationList);
      if ((certificaiton != undefined && Object.keys(certificaiton).length !== 0) && (certificaiton.employeeCertificateId == undefined || certificaiton.employeeCertificateId == null)) {
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
    this.employeeObj.createdBy = this.currentUser.empId;

    console.log("onSave --> employeeObj : ", this.employeeObj);
    this.updateUserInfoService.setUserInfoObj(this.employeeObj);
    const response = await this.updateUserInfoService.saveEmployeeInfo();
    if (response.serviceStatus == "Success") {
      console.log("Employee Info saved : ",response.serviceResponse);
      this.employeeObj.draftEmpId = response.serviceResponse.draftEmpId;
      this.updateUserInfoService.setUserInfoObj(this.employeeObj);
      this.loadDocumentUpload.emit();
    } else {
      console.error(response.serviceResponse);
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

}
