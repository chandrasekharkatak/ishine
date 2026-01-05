import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs/operators';
import { Feature } from 'src/app/models/feature';
import { SkillCertConfig } from 'src/app/models/skillCertConfig';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { DomainService } from 'src/app/services/domain.service';
import { ValidationService } from 'src/app/services/validation.service';
import * as XLSX from 'xlsx';

@Component({
  standalone: false,
  selector: 'app-skill-certfification-config',
  templateUrl: './skill-certfification-config.component.html',
  styleUrls: ['./skill-certfification-config.component.css']
})
export class SkillCertfificationConfigComponent implements OnInit {

  constructor(private authenticationService: AuthenticationService,
    public validationService: ValidationService, private modalService: NgbModal, private domainService: DomainService
  ) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x); }

  userMapping: any = {};
  feature = 'Skills And Cert Config';

  ngOnInit(): void {
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
  }

  currentUser: User;
  isSkillCertficateUpload = true;
  file: any;
  @ViewChild("alert_message") alertTemplate: TemplateRef<any>;

  @ViewChild("errorModal") errorTemplate: TemplateRef<any>;

  @ViewChild("confirmbox") confirmTemplate: TemplateRef<any>;

  @ViewChild("invalidFileModal") invalidFileTemplate: TemplateRef<any>;



  //modal
  alertMessage: any;
  modalRef:NgbModalRef;
  errorModalRef:NgbModalRef;
  confirmModalRef:NgbModalRef;
  invalidModalRef:NgbModalRef;
  showFileUploadForm() {

    this.isSkillCertficateUpload = true;
  }

  skillCertConfigObj: SkillCertConfig = new SkillCertConfig();

  headersSkills = [
    { 'Employee Id(A-240017/AP-240017)': '', 'Skills(comma separated)': '', 'Proficiency(Beginner/Intermediate/Expert)': '' }
  ];
  downloadSkillFileTemplate(): void {
    const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(this.headersSkills, { skipHeader: false });
    const wb: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Template');
    XLSX.writeFile(wb, 'Skill_Related_Data_Template.xlsx');
  }


  headersCertficates = [
    { 'Employee Id(A-240017/AP-240017)': '', 'Certificate Name': '', 'Specialization(eg.cloud engineer)': '', 'Department': '', 'Proficiency(Beginner/Intermediate/Expert)': '', 'Issuing Authority': '', 'Valid From(yyyy-mm-dd)': '', 'Expires On(yyyy-mm-dd)': '', 'Skills(Comma separated)': '', 'Drive Link': '' }
  ];
  downloadCertficateFileTemplate(): void {
    const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(this.headersCertficates, { skipHeader: false });
    const wb: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Template');
    XLSX.writeFile(wb, 'Certificate_Related_Data_Template.xlsx');
  }

  downloadTemplate(template: TemplateRef<any>): void {
    if (this.skillCertConfigObj.importType === 'Skill') {
      this.downloadSkillFileTemplate();
    } else if (this.skillCertConfigObj.importType === 'Certification') {
      this.downloadCertficateFileTemplate();
    } else {
      this.openAlertMod(template, "Please Select Import Type ");
    }
  }

  errorMessages: string[] = [];
  onSkillCertficateFileSelect(template: TemplateRef<any>) {
    if (!this.file) {
      this.modalRef?.close();
      return;
    }


    this.skillCertConfigObj.uploadedBy = this.currentUser.empId;

    if (this.skillCertConfigObj.importType === "Skill") {
      this.domainService.skillFile(this.file, this.skillCertConfigObj).pipe(first()).subscribe(
        (response: any) => {
          if (response.serviceStatus == "Success") {
            this.openAlertMod(template, response.serviceResponse);
          } else if (response.serviceStatus == "Fail") {
            this.errorMessages = response.serviceResponse;
            this.openerrorModalTempTemp();
          }
          // event.target.value = '';
          //   this.file = null;
        });
    }
    else if (this.skillCertConfigObj.importType === "Certification") {
      this.domainService.certficateFile(this.file, this.skillCertConfigObj).pipe(first()).subscribe(
        (response: any) => {
          if (response.serviceStatus == "Success") {
            this.openAlertMod(template, response.serviceResponse);
          } else if (response.serviceStatus == "Fail") {
            this.errorMessages = response.serviceResponse;
            this.openerrorModalTempTemp();
          }
          else {
            this.openAlertMod(template, response.serviceResponse);
          }
          // event.target.value = '';
          //   this.file = null;
        });
    }
    else {
      this.openAlertMod(template, "Please select a valid Import Type (Skill/Certification).");
      // event.target.value = '';
      //     this.file = null;
    }

    this.modalRef?.close();
    this.resetFileInput();







  }

  fileInputRef!: HTMLInputElement;
  openConfirmationBox(event: any, template: TemplateRef<any>, fileInput: HTMLInputElement) {

    const uploadedFiles = event.target.files;
    if (uploadedFiles && uploadedFiles.length > 0) {
      this.file = uploadedFiles[0];
      this.fileInputRef = fileInput;


      this.validateExcelHeaders(this.file).then(isValid => {
        console.log(isValid);
        if (isValid) {

          this.confirmModalRef = this.modalService.open(this.confirmTemplate, { modalDialogClass: 'modal-sm' });
        } else {

          this.invalidModalRef = this.modalService.open(this.invalidFileTemplate, { modalDialogClass: 'modal-sm' });
          this.resetFileInput();
        }
      });
    }


  }


  resetFileInput() {
    this.file = null;
    if (this.fileInputRef) {
      this.fileInputRef.value = '';
    }
  }


  closeInvalidFileModal() {
    this.invalidModalRef?.close();
  }





  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  openerrorModalTempTemp() {
    this.errorModalRef = this.modalService.open(this.errorTemplate, { modalDialogClass: 'modal-lg' });
  }

  closeErrorModal() {
    this.errorModalRef?.close();
  }


  cancelRequest() {
    this.modalRef?.close();
  }

  cancelUpload() {
    this.confirmModalRef?.close();
    this.resetFileInput();

  }


  // async validateExcelHeaders(file: File): Promise<boolean> {
  //   try {
  //     alert("lalalal")
  //     const data = await file.arrayBuffer();
  //     const workbook = XLSX.read(data);
  //     const sheet = workbook.Sheets[workbook.SheetNames[0]];
  //     const firstRow = XLSX.utils.sheet_to_json(sheet, { header: 1 })[0] as string[];

  //     if (!firstRow || firstRow.length === 0) return false;

  //     const expectedHeaders =
  //       this.skillCertConfigObj.importType === 'Skill'
  //         ? this.headersSkills
  //         : this.skillCertConfigObj.importType === 'Certification'
  //         ? this.headersCertficates
  //         : [];

  //     if (expectedHeaders.length === 0) return false;


  //     const normalizedExpected = expectedHeaders.map(h => h.trim().toLowerCase());
  //     const normalizedActual = firstRow.map((h: string) => h.trim().toLowerCase());

  //     const isMatch =
  //       normalizedExpected.length === normalizedActual.length &&
  //       normalizedExpected.every((h, i) => h === normalizedActual[i]);

  //     return isMatch;
  //   } catch (error) {
  //     console.log('Header validation failed:', error);
  //     return false;
  //   }
  // }

  async validateExcelHeaders(file: File): Promise<boolean> {
    try {


      const data = await file.arrayBuffer();
      const workbook = XLSX.read(data);
      const sheet = workbook.Sheets[workbook.SheetNames[0]];
      const firstRow = XLSX.utils.sheet_to_json(sheet, { header: 1 })[0] as any[];

      if (!firstRow || firstRow.length === 0) return false;


      const expectedHeadersObj =
        this.skillCertConfigObj.importType === 'Skill'
          ? this.headersSkills[0]
          : this.skillCertConfigObj.importType === 'Certification'
            ? this.headersCertficates[0]
            : {};

      const expectedHeaders = Object.keys(expectedHeadersObj);

      if (expectedHeaders.length === 0) return false;


      const normalizedExpected = expectedHeaders.map(h =>
        (typeof h === 'string' ? h : String(h)).trim().toLowerCase()
      );
      const normalizedActual = firstRow.map((h: any) =>
        (typeof h === 'string' ? h : String(h || '')).trim().toLowerCase()
      );


      const isMatch =
        normalizedExpected.length === normalizedActual.length &&
        normalizedExpected.every((h, i) => h === normalizedActual[i]);

      if (!isMatch) {
        console.warn('Expected Headers:', normalizedExpected);
        console.warn('Actual Headers:', normalizedActual);
      }

      return isMatch;
    } catch (error) {
      console.error('Header validation failed:', error);
      return false;
    }
  }



}
