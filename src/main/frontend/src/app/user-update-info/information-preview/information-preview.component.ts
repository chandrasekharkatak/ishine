import { Component, EventEmitter, OnInit, Output, TemplateRef } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { Employee } from 'src/app/models/employee';
import { UpdateUserInfoService } from 'src/app/services/updateUserInfo.service';
import { ValidationService } from 'src/app/services/validation.service';

@Component({
  selector: 'app-information-preview',
  templateUrl: './information-preview.component.html',
  styleUrls: ['./information-preview.component.css']
})
export class InformationPreviewComponent implements OnInit {

  currentEmployeeInfo:Employee = new Employee();
  @Output() previewSubmit:EventEmitter<any> = new EventEmitter<any>();

  //modal 
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();

  constructor(
    private updateUserInfoService: UpdateUserInfoService,
    private validationService: ValidationService,
    private modalService: BsModalService,
  ) { }

  ngOnInit(): void {
    this.currentEmployeeInfo = this.updateUserInfoService.getUserInfoObj();
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
      employeeObj.previousEmploymentList.forEach((previousEmployer, index) =>{
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

  async onSubmit() {
    const response:any = await this.updateUserInfoService.updateEmployeeInfo();
    console.log("onUpdate --> Preview : ", response);
    
    if (response.serviceStatus == "Success") {
      console.log(response.serviceResponse);
      this.previewSubmit.emit();
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
