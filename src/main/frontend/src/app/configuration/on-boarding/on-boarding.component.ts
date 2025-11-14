import { Component, OnInit, TemplateRef } from '@angular/core';
import { first } from 'rxjs/internal/operators/first';
import { Asset } from 'src/app/models/asset';
import { OnBoardingService } from 'src/app/services/on-boarding.service';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { User } from 'src/app/models/user';
import { ValidationService } from 'src/app/services/validation.service';
import { UtilityService } from 'src/app/services/utility.service';
import { Feature } from 'src/app/models/feature';

@Component({
  selector: 'app-on-boarding',
  templateUrl: './on-boarding.component.html',
  styleUrls: ['./on-boarding.component.css']
})
export class OnBoardingComponent implements OnInit {

  currentUser:User;
  alertMessage:any;
  assetObj:Asset = new Asset();

  employeeOnBoardingDetailList:any[] = [];
  employeeDetailList:any[] = [];
  departmentList:any[] = [];
  updatedAssetList:any[] = [];

  modalRef: BsModalRef = new BsModalRef();

  

  userMapping: any = {};
  feature = "Onboarding Config";
  
  constructor(
    private onBoardingService : OnBoardingService,
    private authenticationService : AuthenticationService,
    private validationService:ValidationService,
    private modalService: BsModalService,
    private utilityService: UtilityService,
  ) {
     this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    }

    async ngOnInit(): Promise<void> {
      

      // Dynamic Subfeature Flags
      let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
      featureMap.subFeatures?.forEach(sub => {
        this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
      });
    }

  // getAssetDataFromSnipitPortal(template: TemplateRef<any>){
  //   this.cancelRequest();

  //   let assetObj = {...this.assetObj};

  //   if (assetObj.employeementId.startsWith('A-')) {
  //     if(!this.validationService.validateNullUndefinedEmptyString(assetObj.employeementId)){
  //       this.alertMessage = "Please enter EmployeementId !!"
  //       this.openAlertMod(template, this.alertMessage);
  //       return false;
  //     }
  //     assetObj.employeementId = assetObj.employeementId.substring(2);
  //   } else {
  //     assetObj.employeementId  = assetObj.employeementId;
  //   }

  //   this.onBoardingService.getAssetDataFromSnipitPortal(assetObj).pipe(first()).subscribe((response:any) => {
  //     if(response.serviceStatus == "Success"){
  //       this.openAlertMod(template, response.serviceResponse);
  //       this.getEmployeeOnBoardingDetailByEmployeementId(template);
  //     }else{
  //       this.openAlertMod(template, response.serviceResponse);
  //       this.getEmployeeOnBoardingDetailByEmployeementId(template);
  //     }
  //   });
  // }

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

fieldRestrictCharacterForEmployeeId(event: KeyboardEvent) {
  const input = event.target as HTMLInputElement;
  const value = input.value;
  const key = event.key;

  // Allow navigation keys
  // if (['Backspace', 'Delete', 'ArrowLeft', 'ArrowRight', 'Tab'].includes(key)) return;

  // Handle prefix typing logic
  if (value === '') {
    if (key === 'A') return; // allow first 'A'
    event.preventDefault();
    return;
  }

  if (value === 'A') {
    if (key === '-') return;        // allow 'A-'
    if (key === 'P') return;        // allow 'AP' (for APR)
    event.preventDefault();
    return;
  }

  if (value === 'AP') {
    if (key === 'R') return;        // allow 'APR'
    event.preventDefault();         // block AP- (no '-')
    return;
  }

  if (value === 'APR') {
    if (key === '-') return;        // allow 'APR-'
    event.preventDefault();
    return;
  }

  // Now check valid prefixes (A- or APR-)
  const validPrefix = value.startsWith('A-') || value.startsWith('APR-');
  const digitsOnly = value.replace(/^A-|^APR-/, '');

  if (validPrefix) {
    // allow digits only, up to 6 digits
    if (!/^\d$/.test(key) || digitsOnly.length >= 6) {
      event.preventDefault();
    }
  } else {
    event.preventDefault();
  }
}




getEmpIdPrefixFromFlags(employee: any): string {
  if (employee.isApmosysProduct === 'true') {
    return 'AP-';
  } else if (employee.isConsultant === 'true') {
    return 'CS-';
  } else {
    return 'A-';
  }
}

  getEmployeeOnBoardingDetailByEmployeementId(template: TemplateRef<any>){
    this.cancelRequest();

    let assetObj = {...this.assetObj};
     let empIdInput = this.assetObj.employeementId;

    if(!this.validationService.validateNullUndefinedEmptyString(assetObj.employeementId)){
      this.alertMessage = "Please enter Employee ID !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    // if (assetObj.employeementId.startsWith('A-')) {
    //   assetObj.employeementId = assetObj.employeementId.substring(2);
    // } else {
    //   assetObj.employeementId  = assetObj.employeementId;
    // }


    if (empIdInput.startsWith('APR-')) {
    assetObj.employeeType = "Apmosys Product";
    assetObj.employeementId = empIdInput.substring(4);
  } else if (empIdInput.startsWith('A-')) {
    assetObj.employeeType = "Other";
    assetObj.employeementId = empIdInput.substring(2);
  } else {
    this.alertMessage = "Please enter valid Employee ID !!";
    this.openAlertMod(template, this.alertMessage);
    return false;
  }

  // assetObj.employeementId = empIdInput;

    // if (!this.validationService.validateEmployeementId(assetObj.employeementId)) {
    //   this.alertMessage = "Please enter valid Employee ID !!";
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    this.onBoardingService.getEmployeeOnBoardingDetailByEmployeementId(assetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.employeeOnBoardingDetailList = response.serviceResponse;
        this.employeeDetailList = response.serviceResponse1;
        this.employeeDetailList.forEach((employee) => {
           employee.emp360 = employee.empId;
           employee.emp360ManagerId = employee.managerId;
        });
        const key = "deptId";
        this.departmentList = [...new Map(this.employeeOnBoardingDetailList.map((employee:Asset) => [employee[key], employee])).values()].map((employee:Asset) => {
          return { departmentName: employee.departmentName, deptId: employee.deptId}
        });

        this.departmentList.forEach(dept => {
          dept["assetList"] = this.employeeOnBoardingDetailList.filter((employee:Asset) => employee.departmentName == dept.departmentName);

          dept.assetList.forEach((asset:Asset) => {
            asset.isAssigned = (asset.isAssigned != null) ? JSON.parse(asset.isAssigned) : false;
          });

          if(dept.deptId == null){
            dept.deptId = this.currentUser.departmentId;
          }
        });

        // department name check is static
        if(this.currentUser.departmentName != 'HR' && this.currentUser.departmentName != 'Director'){
          this.departmentList = this.departmentList.filter(x => x.deptId == this.currentUser.departmentId);
        }

        //console.log("Assets according to departments : ", this.departmentList);

      } else {
        this.openAlertMod(template, response.serviceResponse);

        this.employeeDetailList = null;
        this.employeeOnBoardingDetailList = null;
      }
    });
  }

  selectAssetCheckbox(updatedAsset){
    const alreadyUpdatedAsset = this.updatedAssetList.find((x) => x.assetId == updatedAsset.assetId);
      if(alreadyUpdatedAsset){
        this.updatedAssetList.splice(alreadyUpdatedAsset,1);
      }else{
        this.updatedAssetList.push(updatedAsset);
      }
      //console.log(this.updatedAssetList);
  }

  updateOnBoardingCheckList(template: TemplateRef<any>){
     let empIdInput = this.assetObj.employeementId;
  if (empIdInput?.startsWith('A-')) {
    empIdInput = empIdInput.substring(2);
  } else if (empIdInput?.startsWith('CS-')) {
    empIdInput = empIdInput.substring(3);
  } else if (empIdInput?.startsWith('AP-')) {
    empIdInput = empIdInput.substring(3);
  }
  this.assetObj.employeementId = empIdInput;
    // this.departmentList.forEach(asset => {
    //   this.assetObj.departmentWiseAssetList.push(...asset.assetList);
    // });
    // //console.log(this.assetObj.departmentWiseAssetList, " list");

    let assetObj = new Asset();
    assetObj.employeementId = empIdInput;
    assetObj.departmentWiseAssetList = this.updatedAssetList;
    assetObj.updatedBy = this.currentUser.empId;
    assetObj.empId = this.assetObj.empId;

    // if (assetObj.employeementId.startsWith('A-')) {
    //   assetObj.employeementId = assetObj.employeementId.substring(2);
    // } else {
    //   assetObj.employeementId  = assetObj.employeementId;
    // }

  

    this.onBoardingService.updateOnBoardingCheckList(assetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }

}
