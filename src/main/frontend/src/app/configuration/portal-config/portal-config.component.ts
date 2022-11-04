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

  isPortalConfiguration: boolean = true;
  isAppreciationConfiguration: boolean = false;

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

  showPortalConfiguration(){
    this.isPortalConfiguration = true;
    this.isAppreciationConfiguration = false;
  }

  showAppreciationConfiguration(){
    this.isPortalConfiguration = false;
    this.isAppreciationConfiguration = true;
  }

  getAllPortalConfigData() {
    this.portalService.getPortalConfig().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.portalObj = Object.assign({}, response.serviceResponse);
        this.portalConfigList = response.serviceResponse;

        console.log(this.portalConfigList,  "   :  portalConfigList");

        for(let portal of this.portalConfigList){
          if(portal.configName == 'Probation Period'){
            this.portalObj.probationPeriod = portal.configPeriod;
            this.portalObj.probationMailTrigger = portal.mailTrigger;
          }
          if(portal.configName == 'Notice Period'){
            this.portalObj.noticePeriod = portal.configPeriod;
            this.portalObj.noticeMailTrigger = portal.mailTrigger;
          }
          if(portal.configName == 'OTRS Link'){
            this.portalObj.otrsLink = portal.configValue;
          }
          if(portal.configName == 'SNIPIT Link'){
            this.portalObj.snipitLink = portal.configValue;
          }
        }
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  updatePortalGlobalConfiguration(portalObj,template: TemplateRef<any>){

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
    if (!this.validationService.validateNullUndefinedEmptyString(portalObj.noticePeriod)) {
      this.alertMessage = "Please enter Notice Period !!"
      this.openAlertMod(template, this.alertMessage);
      return;
    }
    if (!this.validationService.validateNullUndefinedEmptyString(portalObj.noticeMailTrigger)) {
      this.alertMessage = "Please enter Notice Period Mail Trigger !!"
      this.openAlertMod(template, this.alertMessage);
      return;
    }
    if (!this.validationService.validateNullUndefinedEmptyString(portalObj.otrsLink)) {
      this.alertMessage = "Please enter OTRS Link !!"
      this.openAlertMod(template, this.alertMessage);
      return;
    }else if(this.validationService.validateUrl(portalObj.otrsLink) == false){
      this.alertMessage = "Please valid OTRS Link !!"
      this.openAlertMod(template, this.alertMessage);
      return;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(portalObj.snipitLink)) {
      this.alertMessage = "Please enter SNIPIT Link !!"
      this.openAlertMod(template, this.alertMessage);
      return;
    }else if(this.validationService.validateUrl(portalObj.snipitLink) == false){
      this.alertMessage = "Please valid SNIPIT Link !!"
      this.openAlertMod(template, this.alertMessage);
      return;
    }

    let tempArray = this.portalConfigList;    

    tempArray.forEach((portalConfig,index)=> {
      
      if(index == 0)
      {       
        this.portalConfigList[0].configPeriod = this.portalObj.probationPeriod;
        this.portalConfigList[0].mailTrigger = this.portalObj.probationMailTrigger;
      }
      else if(index == 1)
      {
        this.portalConfigList[1].configPeriod = this.portalObj.noticePeriod;
        this.portalConfigList[1].mailTrigger = this.portalObj.noticeMailTrigger;
      }else if(index == 2)
      {
        this.portalConfigList[2].configValue = this.portalObj.otrsLink;
      }else if(index == 3)
      {
        this.portalConfigList[3].configValue = this.portalObj.snipitLink;
      }
      
    })
        this.portalObj.allPortalConfigData = this.portalConfigList;

         this.portalService.updatePortalConfig(portalObj).pipe(first()).subscribe((response: any) => {
           if (response.serviceStatus == "Success") {
             this.openAlertMod(template, response.serviceResponse);
           }else{
             this.openAlertMod(template, response.serviceResponse);
           }
         });
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
