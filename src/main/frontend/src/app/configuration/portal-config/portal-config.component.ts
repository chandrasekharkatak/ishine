import { Component, OnInit, TemplateRef } from '@angular/core';
import { Portal } from 'src/app/models/portal';
import { first } from 'rxjs/operators';
import { PortalService } from 'src/app/services/portal.service';
import { ValidationService } from 'src/app/services/validation.service';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { User } from 'src/app/models/user';
import { Feature } from 'src/app/models/feature';

@Component({
  selector: 'app-portal-config',
  templateUrl: './portal-config.component.html',
  styleUrls: ['./portal-config.component.css']
})
export class PortalConfigComponent implements OnInit {

  feature="Portal Config";
  currentUser:User;
  userMapping:any = {};

  alertMessage:any;
  modalRef: BsModalRef = new BsModalRef();

  portalObj:Portal = new Portal();

  portalConfigList:any[] = [];

  constructor(
    private portalService:PortalService,
    private validationService:ValidationService,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    // Dynamic Subfeature Flags 
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    console.log(this.feature, " : ", this.userMapping);

    this.getAllPortalConfigData();
  }

  getAllPortalConfigData() {
    this.portalService.getPortalConfig().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.portalObj = Object.assign({}, response.serviceResponse);
        this.portalConfigList = response.serviceResponse;
        for(let portal of this.portalConfigList){
          if(portal.configName == 'Probation Period'){
            this.portalObj.probationPeriod = portal.configPeriod;
            this.portalObj.probationMailTrigger = portal.mailTrigger;
          }
          if(portal.configName == 'Notice Period'){
            this.portalObj.noticePeriod = portal.configPeriod;
          }
        }
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  updateProbationPeriod(portalObj,template: TemplateRef<any>){

    if (!this.validationService.validateNullUndefinedEmptyString(portalObj.probationPeriod)) {
      this.alertMessage = "Please enter Probation Period !!"
      this.openAlertMod(template, this.alertMessage);
      return;
    }
    if (!this.validationService.validateNullUndefinedEmptyString(portalObj.probationMailTrigger)) {
      this.alertMessage = "Please enter Probation Period Mail Trigger !!"
      this.openAlertMod(template, this.alertMessage);
      return;
    }

    for(let portal of this.portalConfigList){
      if(portal.configName == 'Probation Period'){

        portalObj.configName = portal.configName;
        portalObj.portalConfigId = portal.portalConfigId;
        portalObj.configPeriod = portalObj.probationPeriod;
        portalObj.mailTrigger = portalObj.probationMailTrigger;
        portalObj.updatedBy = this.currentUser.empId;

        this.portalService.updatePortalConfig(portalObj).pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus == "Success") {
            this.openAlertMod(template, response.serviceResponse);
          }else{
            this.openAlertMod(template, response.serviceResponse);
          }
        });
      }
    }
  }

  updateNoticePeriod(portalObj,template: TemplateRef<any>){

    if (!this.validationService.validateNullUndefinedEmptyString(portalObj.noticePeriod)) {
      this.alertMessage = "Please enter Notice Period !!"
      this.openAlertMod(template, this.alertMessage);
      return;
    }

    for(let portal of this.portalConfigList){
      if(portal.configName == 'Notice Period'){

        portalObj.configName = portal.configName;
        portalObj.portalConfigId = portal.portalConfigId;
        portalObj.updatedBy = this.currentUser.empId;
        portalObj.configPeriod = portalObj.noticePeriod;

        this.portalService.updatePortalConfig(portalObj).pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus == "Success") {
            this.openAlertMod(template, response.serviceResponse);
          }else{
            this.openAlertMod(template, response.serviceResponse);
          }
        });
      }
    }
  }

  //modal

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }

}
