import { Component, OnInit, TemplateRef } from '@angular/core';
import { Portal } from 'src/app/models/portal';
import { first } from 'rxjs/operators';
import { PortalService } from 'src/app/services/portal.service';
import { ValidationService } from 'src/app/services/validation.service';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { User } from 'src/app/models/user';

@Component({
  selector: 'app-portal-config',
  templateUrl: './portal-config.component.html',
  styleUrls: ['./portal-config.component.css']
})
export class PortalConfigComponent implements OnInit {

  alertMessage:any;
  currentUser: User;
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
            this.portalObj.noticeMailTrigger = portal.mailTrigger;
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
