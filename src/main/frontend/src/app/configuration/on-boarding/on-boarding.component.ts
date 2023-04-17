import { Component, OnInit, TemplateRef } from '@angular/core';
import { first } from 'rxjs/internal/operators/first';
import { Asset } from 'src/app/models/asset';
import { OnBoardingService } from 'src/app/services/on-boarding.service';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { User } from 'src/app/models/user';
import { ValidationService } from 'src/app/services/validation.service';

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

  constructor(
    private onBoardingService : OnBoardingService,
    private authenticationService : AuthenticationService,
    private validationService:ValidationService,
    private modalService: BsModalService,
  ) {
     this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    }

  ngOnInit(): void {   
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

  getEmployeeOnBoardingDetailByEmployeementId(template: TemplateRef<any>){
    this.cancelRequest();

    let assetObj = {...this.assetObj};

    if(!this.validationService.validateNullUndefinedEmptyString(assetObj.employeementId)){
      this.alertMessage = "Please enter Employee ID !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (assetObj.employeementId.startsWith('A-')) {
      assetObj.employeementId = assetObj.employeementId.substring(2);
    } else {
      assetObj.employeementId  = assetObj.employeementId;
    }

    if (!this.validationService.validateEmployeementId(assetObj.employeementId)) {
      this.alertMessage = "Please enter valid Employment ID !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    this.onBoardingService.getEmployeeOnBoardingDetailByEmployeementId(assetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.employeeOnBoardingDetailList = response.serviceResponse;
        this.employeeDetailList = response.serviceResponse1;
        console.log(this.employeeOnBoardingDetailList, ' : employeeOnBoardingDetailList');

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

        console.log("Assets according to departments : ", this.departmentList);
        
      } else {
        this.openAlertMod(template, response.serviceResponse);
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
      console.log(this.updatedAssetList);
  }

  updateOnBoardingCheckList(template: TemplateRef<any>){

    // this.departmentList.forEach(asset => {
    //   this.assetObj.departmentWiseAssetList.push(...asset.assetList);
    // });
    // console.log(this.assetObj.departmentWiseAssetList, " list");
    
    this.assetObj.departmentWiseAssetList = this.updatedAssetList;
    this.assetObj.updatedBy = this.currentUser.empId;
    this.onBoardingService.updateOnBoardingCheckList(this.assetObj).pipe(first()).subscribe((response: any) => {
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
