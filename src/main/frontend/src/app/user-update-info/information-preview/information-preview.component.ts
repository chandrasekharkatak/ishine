import { Component, EventEmitter, OnInit, Output, TemplateRef } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { certification } from 'src/app/models/certification';
import { Document } from 'src/app/models/document';
import { Employee } from 'src/app/models/employee';
import { PreviousEmployer } from 'src/app/models/previousEmployer';
import { UpdateUserInfoService } from 'src/app/services/updateUserInfo.service';
import { UtilityService } from 'src/app/services/utility.service';
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
    public utilityService: UtilityService,
  ) { }

  ngOnInit(): void {
    this.currentEmployeeInfo = this.updateUserInfoService.getUserInfoObj();

    //console.log("Employee info IN PREVIEW ==> ", this.currentEmployeeInfo);
  }

  validateEmployeeObj(employeeObj:Employee, template: TemplateRef<any>){

    
    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.viewsOnOrganisation)){
      this.alertMessage = "Please enter your view on organisation !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateViewsOnOrganisation(employeeObj.viewsOnOrganisation)){
      this.alertMessage = "Please enter valid view on organisation !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.aboutMe)){
      this.alertMessage = "Please enter About me !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateViewsOnOrganisation(employeeObj.aboutMe)){
      this.alertMessage = "Please enter valid in  About me !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.gender)){
      this.alertMessage = "Please select gender !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    // if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.dateOfBirth)){
    //   this.alertMessage = "Please enter date of birth !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.aboutMe)){
    //   this.alertMessage = "Please enter About me !!";
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // } else if (!this.validationService.validateAboutMeUserProfile(employeeObj.aboutMe)){
    //   this.alertMessage = "Only string character will be valid in About me  !!";
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

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
      if(!this.validationService.validateAlphaWithSpace(employeeObj.child1)){
        this.alertMessage = "Please enter valid child1 name !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      if(!this.validationService.validateAlphaWithSpace(employeeObj.child2)){
        this.alertMessage = "Please enter valid child2 name !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      if(!this.validationService.validateAlphaWithSpace(employeeObj.child3)){
        this.alertMessage = "Please enter valid child3 name !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      
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

    // if(this.validationService.validateNullUndefinedEmptyString(employeeObj.motherTongue) && !this.validationService.validateAlphaWithSpace(employeeObj.motherTongue)){
    //   this.alertMessage = "Please enter Valid Mother tongue number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if(this.validationService.validateNullUndefinedEmptyString(employeeObj.passportNumber) && !this.validationService.validatePassportNumber(employeeObj.passportNumber)){
    //   this.alertMessage = "Please enter Valid Passport number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

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

    // if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.mobileNo)){
    //   this.alertMessage = "Please enter mobile number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }else if(!this.validationService.validateMobileNumber(employeeObj.mobileNo)){
    //   this.alertMessage = "Please enter valid mobile number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // } 

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.address)){
      this.alertMessage = "Please enter Current Address !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    // if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.state)){
    //   this.alertMessage = "Please enter state !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }else if(!this.validationService.validateAlphaWithSpace(employeeObj.state)){
    //   this.alertMessage = "Please enter Valid state !!";
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.city)){
    //   this.alertMessage = "Please enter city !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }else if(!this.validationService.validateAlphaWithSpace(employeeObj.city)){
    //   this.alertMessage = "Please enter Valid city !!";
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.country)){
    //   this.alertMessage = "Please enter country !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }else if(!this.validationService.validateAlphaWithSpace(employeeObj.country)){
    //   this.alertMessage = "Please enter Valid country !!";
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.pincode)){
      this.alertMessage = "Please enter pincode !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if(!this.validationService.validatePincodeNumber(employeeObj.pincode)){
      this.alertMessage = "Please enter Valid pincode !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    // if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.alternateMobileNo)){
    //   this.alertMessage = "Please enter alternate mobile number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }else if(!this.validationService.validateMobileNumber(employeeObj.alternateMobileNo)){
    //   this.alertMessage = "Please enter valid alternate mobile number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // } 

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
            }else   if(!this.validationService.validateOrgName(previousEmployer.employerName)){
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
    }if(!this.validationService.validateIFSCCodeRegex(employeeObj.bankIFSCCode)){
      this.openAlertMod(template , 'Please Enter Valid IFSC Code !!');
      return false;
    }

    // if(this.validationService.validateNullUndefinedEmptyString(employeeObj.pfAccountNumber) && !this.validationService.validateAlphaNumeric(employeeObj.pfAccountNumber)){
    //   this.alertMessage = "Please enter Valid PF Account Number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if(this.validationService.validateNullUndefinedEmptyString(employeeObj.previousPfAccountNumber) && !this.validationService.validateAlphaNumeric(employeeObj.previousPfAccountNumber)){
    //   this.alertMessage = "Please enter Valid Previous PF Account Number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }
    // if(this.validationService.validateNullUndefinedEmptyString(employeeObj.uan) && !this.validationService.validateAlphaNumeric(employeeObj.uan)){
    //   this.alertMessage = "Please enter Valid UAN Number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }
    // if(this.validationService.validateNullUndefinedEmptyString(employeeObj.esicNumber) && !this.validationService.validateAlphaNumeric(employeeObj.esicNumber)){
    //   this.alertMessage = "Please enter Valid ESIC Number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // Documents
    let docFlag = true;
    employeeObj.documentList.forEach((doc:Document) => {
      if(!docFlag) return;  
      if(doc.documentName == null){
        this.alertMessage = `Please Upload ${doc.documentType} !!`
        this.openAlertMod(template, this.alertMessage);
        docFlag = false;
        //console.log("doc flag ",docFlag);
      }
    });

    if(!docFlag) return false;

    return true;
  }

  async onSubmit(template: TemplateRef<any>) {

    //console.log(" this.currentEmployeeInfo : ",this.currentEmployeeInfo)
    let inputValidated: boolean = this.validateEmployeeObj(this.currentEmployeeInfo, template)
    if (!inputValidated) return;
    const response:any = await this.updateUserInfoService.updateEmployeeInfo();
    //console.log("onUpdate --> Preview : ", response);
    
    if (response.serviceStatus == "Success") {
      this.openAlertMod(template, "Profile Updated Pending For Approval");
      //console.log(response.serviceResponse);
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
