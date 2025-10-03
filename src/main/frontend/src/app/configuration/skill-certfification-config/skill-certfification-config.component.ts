import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { SkillCertConfig } from 'src/app/models/skillCertConfig';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { DomainService } from 'src/app/services/domain.service';
import { ValidationService } from 'src/app/services/validation.service';
import * as XLSX from 'xlsx';

@Component({
  selector: 'app-skill-certfification-config',
  templateUrl: './skill-certfification-config.component.html',
  styleUrls: ['./skill-certfification-config.component.css']
})
export class SkillCertfificationConfigComponent implements OnInit {

  constructor(    private authenticationService: AuthenticationService,
     public validationService: ValidationService, private modalService: BsModalService, private domainService:DomainService
  ) {this.authenticationService.currentUser.subscribe(x => this.currentUser = x); }

  ngOnInit(): void {
  }

  currentUser: User;
 isSkillCertficateUpload= true;
  file:any;
  @ViewChild("alert_message") alertTemplate: TemplateRef<any>;

  @ViewChild("errorModal") errorTemplate: TemplateRef<any>; 
 
  
 
   //modal
   alertMessage: any;
   modalRef: BsModalRef = new BsModalRef();
   errorModalRef: BsModalRef = new BsModalRef();
  showFileUploadForm(){

    this.isSkillCertficateUpload = true;
  }

  skillCertConfigObj: SkillCertConfig = new SkillCertConfig();

  headersSkills = [
    { 'Employee Id': '' , 'Skills(comma separated)': '', 'Proficiency': ''}
  ];
  downloadSkillFileTemplate():void{
    const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(this.headersSkills, { skipHeader: false });
    const wb: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Template');
    XLSX.writeFile(wb, 'Skill_Related_Data_Template.xlsx');
  }


    headersCertficates = [
    { 'Employee Id': '' , 'Certificate Name': '', 'Specialization': '', 'Department':'', 'Proficiency':'','Issuing Authority':'','Valid From(yyyy-mm-dd)':'','Expires On(yyyy-mm-dd)':'','Skills(Comma separated)':'','Drive Link':''}
  ];
  downloadCertficateFileTemplate():void{
    const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(this.headersCertficates, { skipHeader: false });
    const wb: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Template');
    XLSX.writeFile(wb, 'Certificate_Related_Data_Template.xlsx');
  }

  downloadTemplate(): void {
  if (this.skillCertConfigObj.importType === 'Skill') {
    this.downloadSkillFileTemplate();
  } else if (this.skillCertConfigObj.importType === 'Certification') {
    this.downloadCertficateFileTemplate();
  } else {
    alert('Please select Import Type first');
  }
}

errorMessages: string[] = [];
onSkillCertficateFileSelect(event: any, template: TemplateRef<any>){
  const uploadedFiles = event.target.files;
  console.log("uploadedFiles ", uploadedFiles);
  this.file = uploadedFiles[0];
  

  this.skillCertConfigObj.uploadedBy = this.currentUser.empId;

   if (this.skillCertConfigObj.importType === "Skill") {
  this.domainService.skillFile(this.file,this.skillCertConfigObj).pipe(first()).subscribe(
    (response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
      }else if(response.serviceStatus == "Fail") {
         this.errorMessages = response.serviceResponse;
         this.openerrorModalTempTemp();
      }
      event.target.value = '';
        this.file = null;
    });
  }
  else if (this.skillCertConfigObj.importType === "Certification") {
     this.domainService.certficateFile(this.file,this.skillCertConfigObj).pipe(first()).subscribe(
    (response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
      } else if(response.serviceStatus == "Fail"){
         this.errorMessages = response.serviceResponse;
        this.openerrorModalTempTemp();
      }
      else{
        this.openAlertMod(template, response.serviceResponse);
      }
      event.target.value = '';
        this.file = null;
    });
  }
   else {
    this.openAlertMod(template, "Please select a valid Import Type (Skill/Certification).");
    event.target.value = '';
        this.file = null;
  }




}






 openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

 openerrorModalTempTemp() {
  this.errorModalRef = this.modalService.show(this.errorTemplate, { class: 'modal-lg' });
}

closeErrorModal(){
  this.errorModalRef.hide();
}


  cancelRequest() {
    this.modalRef.hide();
  }

}
