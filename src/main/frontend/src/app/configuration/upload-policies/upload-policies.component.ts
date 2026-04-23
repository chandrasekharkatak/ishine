import { LocationStrategy } from '@angular/common';
import { Component, OnInit, TemplateRef } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { saveAs } from "file-saver";
import * as moment from 'moment';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Feature } from 'src/app/models/feature';
import { UploadPolicy } from 'src/app/models/UploadPolicy';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { NotificationService } from 'src/app/services/notification.service';
import { UploadPoliciesService } from 'src/app/services/upload-policies.service';
import { UtilityService } from 'src/app/services/utility.service';
import { ValidationService } from 'src/app/services/validation.service';




@Component({
  standalone: false,
  selector: 'app-upload-policies',
  templateUrl: './upload-policies.component.html',
  styleUrls: ['./upload-policies.component.css']
})
export class UploadPoliciesComponent implements OnInit {

  feature: any = "Policy Config";

  fileObj: UploadPolicy = new UploadPolicy();
  files: any[] = [];
  policyName: any;
  currentUser: User;
  userMapping: any = {};

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;

  //flags
  isDocumentForm: boolean = false;
  isTable: boolean = false;

  //modal
  alertMessage: any;
  modalRef:NgbModalRef;
  document: any[] = [];

  //application properties value
  maxFileSize: any;
  maxRequestSize: any;
  // fileObj1: any = {};

  fileSize: number = 0;
  data: any;
  responseList: any[] = [];
  isreadEnabled: boolean = false;
  responsedata: any;
  fileName: any;
  src: any;

  filters: any = {};
  isSearchEnabled: boolean = false;
  documentsColumns: any[] = ['blank', 'fileName', 'policyName', 'createdByName', 'createdOn','blank','blank'];
  readResponseColumns: any[] = ['blank', 'name', 'empId','departmentName' ,'policyName', 'readEnabled'];



  constructor(private uploadPoliciesService: UploadPoliciesService,
    private validationService: ValidationService,
    private modalService: NgbModal,
    private authenticationService: AuthenticationService,
    private exportExcelService: ExportExcelService,
    private notificationService: NotificationService,
    private locationStrategy: LocationStrategy,
    private utilityService: UtilityService,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);

  }

  async ngOnInit(): Promise<void> {

    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    console.log('----------------------------', this.userMapping);
    this.sectionViewInit();
    this.preventBackButton();
  }
  preventBackButton() {
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(() => {
      history.pushState(null, null, location.href);
    })
  }


  sectionViewInit() {
    // if(this.userMapping.upload_policy){
    //   this.showDocumentForm();
    // }else if(this.userMapping.view_all_documents){
    //   this.showTable();
    // }

    if (this.userMapping.view_all_documents) {
      this.showTable();
    }
  }
  showDocumentForm() {
    this.isDocumentForm = true;
    this.maxFileSize = parseInt(sessionStorage.maxFileSize);
    this.maxRequestSize = parseInt(sessionStorage.maxRequestSize);
    this.isTable = false;
    this.isreadEnabled = false;
    this.reset();

  }
  showTable() {
    this.isTable = true;
    this.isDocumentForm = false;
    this.isreadEnabled = false;
    this.filters = {};
    this.isSearchEnabled = false;

    this.getAllDocuments();
  }

  onFileSelect(event: any, template: TemplateRef<any>) {
    this.files = [];
    let totalSize: number = 0;
    let isSizeInRange: boolean = false;
    const allowedTypes = ['application/pdf', 'application/doc'];
    const maxSizeInBytes = 20 * 1024 * 1024; // 20MB
    this.fileSize = 0;
    const uploadedFiles = event.target.files;
    //console.log("maxfilesize: "+ this.maxFileSize );

    if (uploadedFiles[0] && allowedTypes.indexOf(uploadedFiles[0].type) === -1) {
      this.openAlertMod(template, 'Please select a valid file (.pdf or .doc).');
      event.target.value = ''; // Clear the input
      return;
    }

    if (event.target.files[0].size > maxSizeInBytes) {
      this.openAlertMod(template, "File size is more than 20MB");
      event.target.value = null;
      isSizeInRange = false;
    } else {
      isSizeInRange = true;
    }

    if (isSizeInRange) {
      this.files = [];

      if (uploadedFiles.length != 0) {
        for (let i = 0; i < uploadedFiles.length; i++) {
          let document = uploadedFiles[i];
          let fileName = document.name;
          this.fileSize = this.fileSize + uploadedFiles[i].size / 1024 / 1024;
          //console.log(this.fileSize);
          let fileObj1 = { document: document, fileName: fileName }
          this.files.push(fileObj1);
          //console.log("Files : ", this.files);
        }
      };
    }
  }
  reset() {
    this.policyName = null;
    this.files = [];
  }


  RestrictFullName(event) {
    var k;
    k = event.charCode;
    if ((k == 33) || (k == 34) || (k == 35) || (k == 36) || (k == 37) ||
      (k == 38) || (k == 39) || (k == 40) || (k == 41) || (k == 42) ||
      (k == 43) || (k == 44) || (k == 45) || (k == 46) || (k == 47) || (k == 48) ||
      (k == 49) || (k == 50) || (k == 51) || (k == 52) || (k == 53) || (k == 54) || (k == 55) ||
      (k == 56) || (k == 57) || (k == 58) ||
      (k == 59) || (k == 60) || (k == 61) || (k == 62) || (k == 63) ||
      (k == 64) || (k == 91) || (k == 92) || (k == 93) || (k == 94) ||
      (k == 95) || (k == 96) || (k == 123) ||
      (k == 124) || (k == 125) || (k == 126) || (k == 127)) {
      return (false);
    }
    return (true);


  }
  onUploadFiles(template: TemplateRef<any>) {
    this.policyName = this.policyName?.trim();
    if (!this.validationService.validateNullUndefinedEmptyString(this.policyName)) {
      this.alertMessage = "Please enter Policy Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateAlphaNumericWithSpace(this.policyName)) {
      this.alertMessage = "Please enter Valid Policy Name, Alphabets, Numbers & space allowed !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (this.files.length == 0) {
      this.alertMessage = "Kindly Select Document !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    let totalSize = parseFloat(this.fileSize.toFixed(2));
    if (totalSize > this.maxFileSize && totalSize > this.maxRequestSize) {
      this.alertMessage = "File exceeds the size limit";
      this.openAlertMod(template, this.alertMessage);
      //this.fileSize = 0;
      return false;
    }

    const formData = new FormData();
    this.files.forEach((file) => {
      formData.append(`file`, file.document, file.fileName);
    });
    formData.append("policyName", this.policyName);
    formData.append("uploadedBy", this.currentUser.empId);
    formData.append("readEnabled", "false")

    //console.log("Upload files : ", formData);
    this.uploadPoliciesService.uploadMultipleFiles(formData).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == 'Success') {
        this.openAlertMod(template, response.serviceResponse);
        this.reset();
        this.showTable()

      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });

  }

  spaceTrimInpolicyName() {
    if (this.policyName != null || this.policyName != '') {
      this.policyName = this.policyName?.trim();
    }
  }

  getAllDocuments() {
    this.data = '';
    this.document = [];
    this.uploadPoliciesService.getAllDocument().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.document = response.serviceResponse;
        this.document.forEach(doc => {
          doc.createdOn = (doc.createdOn) ? moment(doc.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          doc.emp360CreatedBy = doc.createdBy;

        });

      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  previewPolicyDocument(template: TemplateRef<any>, doc: any) {
    this.src = null;
    this.fileName = doc.policyName;

    this.uploadPoliciesService.downloadDocument(doc.policyID).pipe(first()).subscribe((response: any) => {
      const blob = new Blob([response], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;

      this.src = a.href;

      if (this.src != null) {
        this.openPreviewDocument(template);
      }
    });
  }

  //modals
  openDeleteDocument(template: TemplateRef<any>, fileObj: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.fileObj = fileObj;
    //console.log(this.fileObj);
  }

  onDeleteDocument(template: TemplateRef<any>) {
    this.cancelRequest();
    this.uploadPoliciesService.deleteDocument(this.fileObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onReadEnabled(template: TemplateRef<any>) {
    this.cancelRequest();
    let fileObj = new UploadPolicy();
    fileObj.policyID = this.fileObj.policyID;
    fileObj.updatedBy = this.currentUser.empId;
    fileObj.readEnabled = true;
    //console.log("Activate Survey : ", fileObj);
    this.uploadPoliciesService.changepolicyEnabledMode(fileObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });

  }

  onReadDisabled(template: TemplateRef<any>) {
    this.cancelRequest();
    let fileObj = new UploadPolicy();
    fileObj.policyID = this.fileObj.policyID;
    fileObj.updatedBy = this.currentUser.empId;
    fileObj.readEnabled = false;
    //console.log("Activate Survey : ", fileObj);
    this.uploadPoliciesService.changepolicyEnabledMode(fileObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });

  }
  openReadEnabledMod(template: TemplateRef<any>, fileObj: UploadPolicy) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.fileObj = fileObj;
  }
  onReadDisabledMod(template: TemplateRef<any>, fileObj: UploadPolicy) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.fileObj = fileObj;
  }
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }
  cancelRequest() {
    this.modalRef?.close();
  }

  // openPreviewDocument(template: TemplateRef<any>) {
  //   this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
  // }

  openPreviewDocument(template: TemplateRef<any>) {
  this.modalRef = this.modalService.open(template, {
    windowClass: 'a4-modal',
    backdrop: true,
    centered: true
  });
}


  fileObjj :any;
  policyReadResponseById(fileObj) {
    this.fileObjj = fileObj;
    this.isreadEnabled = true;
    this.isTable = false;
    this.filters = {};
    this.isSearchEnabled = false;



    this.showPolicyReadResponse(fileObj);

  }
  showPolicyReadResponse(fileObj) {
    this.responsedata = '';
    this.responseList = [];
    this.uploadPoliciesService.showPolicyReadResponse(fileObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.responseList = response.serviceResponse;
        for (let x of this.responseList) {
          x.empId = "A-".concat(x.empId);
        }
        //console.log(this.responseList);
      }
      else {
        console.error(response.serviceResponse);
      }
    });
  }


  name = 'DocumentReadResponse.xlsx';
  policyDataForExcel :any[];

  exportToExcel(): void {



    this.uploadPoliciesService.showPolicyReadResponse(this.fileObjj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.policyDataForExcel = response.serviceResponse;
        //console.log("response.serviceResponse: ",response.serviceResponse);
      }

      const onlySpecificDataArr = this.policyDataForExcel.map(
        x => ({
          "Employee Name": x.name,
          "Employment Id": x.empId,
          "Department Name": x.departmentName,
          "Policy Name": x.policyName,
          "Read On": (x.readEnabled) ? moment(x.readEnabled).format(AppComponent.DATETIME_FORMAT) : ' - ',
        })
      )
      //console.log("Excel Array: ",onlySpecificDataArr);
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.name)
    });
  }


  downloadFile(doc: any) {
    this.uploadPoliciesService.downloadDocument(doc.policyID).subscribe(blob => saveAs(blob, doc.fileName));
  }

  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  sortData(sort: Sort) {
    //console.log(sort);
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

  toggleSearch() {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
    }
  }

  onSearch(searchData) {
    this.filters = searchData;
    //console.log("Updated Filter : ", this.filters);
  }
}
function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);

}
