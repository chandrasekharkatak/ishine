import { Component, EventEmitter, OnInit, Output, TemplateRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { certification } from 'src/app/models/certification';
import { Employee } from 'src/app/models/employee';
import { PreviousEmployer } from 'src/app/models/previousEmployer';
import { User } from 'src/app/models/user';
import { EmployeeService } from 'src/app/services/employee.service';
import { UpdateUserInfoService } from 'src/app/services/updateUserInfo.service';
import { ValidationService } from 'src/app/services/validation.service';

@Component({
  selector: 'app-employee-info',
  templateUrl: './employee-info.component.html',
  styleUrls: ['./employee-info.component.css']
})
export class EmployeeInfoComponent implements OnInit {

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
  ) { }

  ngOnInit(): void {
    this.employeeObj = this.updateUserInfoService.getUserInfoObj();
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
      this.employeeObj.dateOfBirth = this.reset;
      return false;
    }
  }

  validateEmployeeObj(employeeObj:Employee, template: TemplateRef<any>){

    
    // if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.viewsOnOrganisation)){
    //   this.alertMessage = "Please enter your view on organisation !!";
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // } else if (!this.validationService.validateDiscriptionUserProfile(employeeObj.viewsOnOrganisation)){
    //   this.alertMessage = "Only string character will be valid in  your view on organisation !!";
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

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

    // if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.aboutMe)){
    //   this.alertMessage = "Please enter About me !!";
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // } else if (!this.validationService.validateAboutMeUserProfile(employeeObj.aboutMe)){
    //   this.alertMessage = "Only string character will be valid in About me  !!";
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.aboutMe)){
      this.alertMessage = "Please enter About me !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
     } else if (this.validationService.validateNullUndefinedEmptyString(employeeObj.bloodGroup) && !this.validationService.validateBloodGroup(employeeObj.bloodGroup)) {
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

    // if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.experience)){
    //   this.alertMessage = "Please Select Experience !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

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


  onSave(){
    // this.router.navigate(['../document-upload'], {relativeTo:this.route});
    this.updateUserInfoService.setUserInfoObj(this.employeeObj);
    this.loadDocumentUpload.emit();
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
