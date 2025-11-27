import { Component, ElementRef, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Domain } from 'src/app/models/domain';
import { Employee } from 'src/app/models/employee';
import { Feature } from 'src/app/models/feature';
import { CustomQueryDetails } from 'src/app/models/customQueryDetails';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { DomainService } from 'src/app/services/domain.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { ValidationService } from 'src/app/services/validation.service';
import * as XLSX from 'xlsx';

@Component({
  selector: 'app-others',
  templateUrl: './others.component.html',
  styleUrls: ['./others.component.css']
})
export class OthersComponent implements OnInit {

  @ViewChild('queryModal') queryModal: TemplateRef<any>;
  queryName: string = '';
  savedFormats: CustomQueryDetails[] = [];

  @ViewChild("alert_message") alertTemplate: TemplateRef<any>;
    @ViewChild("errorModal") errorTemplate: TemplateRef<any>; 
    errorModalRef: BsModalRef = new BsModalRef();

  feature = 'Domain Config';
   
  @ViewChild('fileInput', { static: false }) fileInput!: ElementRef<HTMLInputElement>;

  //modal
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();
  all:any;
  domainToBeDeleted:any;
  userMapping: any = {};

  currentUser: User;
  employeeObj: Employee = new Employee();
  domainObj: Domain = new Domain();

  isDomain: boolean = false;
  isDomainTable: boolean = false;
  isDomainCreation: boolean = false;
  isDomainUpdation: boolean = false;
  isDomainForm: boolean = false;
  isfileUpload : boolean = false;
  file:any;

  allDomainList:any[] = [];
  specializationList:any[] = [];
  allSpecializationList:any[] = [];

  



availableColumns: string[] = ['Gender', 'Manager Name', 'Designation Name','Manager Id'];
selectedColumns: string[] = ['Employee Id','Employee Name'];  // 'Employee Id' is selected by default
selectedColumn: string = '';
availableColumn: string = '';


  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;

  filters:any = {};
  isSearchEnabled:boolean = false;
  domainColumns:any[] = ['blank','domainName','createdByName','createdOn']

  SpecializationInput: any =document.getElementById('input1');
  //@ViewChild('myInput', { static: false }) myInput: ElementRef<HTMLInputElement>;

  //excel
  domainDataForExcel: any[];
  name = 'Domain.xlsx';

  errorMessages: string[] = [];
  private selectedFileEvent: any;
  private pendingFileEvent: any;
  private pendingAlertTemplate!: TemplateRef<any>;

  constructor(
    private domainService:DomainService,
    public validationService: ValidationService,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private exportExcelService: ExportExcelService,
  ) {this.authenticationService.currentUser.subscribe(x => this.currentUser = x);}


  ngOnInit(): void {
     // Dynamic Subfeature Flags
     let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
     featureMap.subFeatures?.forEach(sub => {
       this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
     });
     //console.log(this.feature, this.userMapping);

     this.isfileUpload = true;
    this.fetchSavedFormats();
  }

  fetchSavedFormats(): void {
    this.domainService.getCustomQueries().subscribe(response => {
      if (response.serviceStatus === 'Success') {
        this.savedFormats = response.serviceResponse;
      }
    });
  }

  onFormatSelect(event: Event): void {
    const selectElement = event.target as HTMLSelectElement;
    const queryName = selectElement.value;
  
    const selectedFormat = this.savedFormats.find(format => format.queryName === queryName);
    if (selectedFormat) {
      this.availableColumns = selectedFormat.availableColumns;
      this.selectedColumns = selectedFormat.selectedColumns;
    }
  }
  

 

 

  showFileUploadForm(){

    this.isfileUpload = true;
    this.isDomainTable = false;
    
  
    this.isDomain = false;
    this.isDomainForm = false;
  
  
  }
  openQueryModal(): void {
    this.modalRef = this.modalService.show(this.queryModal);
  }

  saveQuery(template: TemplateRef<any>): void {
    if (this.queryName && this.selectedColumns.length > 0) {
      let customQueryObj = {
        queryName: this.queryName.trim(),
        createdBy: this.currentUser.empId,
        availableColumns: this.availableColumns, 
        selectedColumns: this.selectedColumns 
      };
  
      this.domainService.saveCustomQueryDetails(customQueryObj).subscribe(
        (response: any) => {
          if (response.serviceStatus === 'Success') {
            this.openAlertMod(template, response.serviceResponse);
            this.fetchSavedFormats(); 
            this.closeModal();
          } else if (response.serviceStatus === 'Fail') {
            this.openAlertMod(template, response.serviceResponse);
          } else {
            this.openAlertMod(template, 'Error occurred while saving the query.');
          }
        },
        error => {
          this.openAlertMod(template, 'Error occurred ');
        }
      );
    } else {
      alert('Query Name and Selected Columns are required.');
    }
  }

  
  
  

  

  resetForm() {
    this.queryName = '';
  }
  

  closeModal() {
    console.log("in closemodal.......");
    
    if (this.modalRef) {
      this.modalRef.hide();
      this.resetForm();
    }
  }


selectColumn(column: string, listType: string): void {
  if (listType === 'available') {
    this.availableColumn = column;
  } else {
    this.selectedColumn = column;
  }
}


moveColumn(direction: string): void {
  if (direction === 'right' && this.availableColumn) {
    this.selectedColumns.push(this.availableColumn);
    this.availableColumns = this.availableColumns.filter(col => col !== this.availableColumn);
    this.availableColumn = '';  
  } else if (direction === 'left' && this.selectedColumn) {
    this.availableColumns.push(this.selectedColumn);
    this.selectedColumns = this.selectedColumns.filter(col => col !== this.selectedColumn);
    this.selectedColumn = '';  
  }
}



downloadExcel(): void {
  const headers = {};
  this.selectedColumns.forEach(column => {
    headers[column] = '';  
  });

  
  const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet([headers], { skipHeader: false });
  const wb: XLSX.WorkBook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, 'Sheet1');
  XLSX.writeFile(wb, 'Selected_Columns_Excel.xlsx');
}


onBillableFileSelect(event: any, template: TemplateRef<any>){
  const uploadedFiles = event.target.files;
  console.log("uploadedFiles ", uploadedFiles);
  this.file = uploadedFiles[0];
  const formData = new FormData();
  formData.append('file', this.file);

  

  this.domainService.billableFile(formData).pipe(first()).subscribe(
    (response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
}

// onDesignationUpload(event: any, template: TemplateRef<any>){
//   const uploadedFiles = event.target.files;
//   console.log("uploadedFiles ", uploadedFiles);
//   this.file = uploadedFiles[0];
//   const formData = new FormData();
//   formData.append('file', this.file);

//   this.domainService.designationBulkUpload(formData).pipe(first()).subscribe(
//     (response: any) => {
//       if (response.serviceStatus == "Success") {
//         this.openAlertMod(template, response.serviceResponse);
//       } else {
//         this.openAlertMod(template, response.serviceResponse);
//       }
//     });
// }

onDesignationUpload(event: any, template: TemplateRef<any>) {
  const uploadedFiles = event.target.files;
  this.file = uploadedFiles[0];
  const formData = new FormData();
  formData.append('file', this.file);

  this.domainService.designationBulkUpload(formData).pipe(first()).subscribe(
    (response: any) => {
      if (response.serviceStatus === "Success") {
        // Show success message using openAlertMod
        this.openAlertMod(template, response.serviceResponse);
      } else if (response.serviceStatus === "Fail") {
        // Show alert with the inactive employees' IDs or row errors
        this.openAlertMod(template, `Error found: ${response.serviceResponse}`);
        // Optionally clear the file input for correction
        event.target.value = '';  // Clear file input so user can upload a corrected file
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
}

onEmployeeUpload(event: any, template: TemplateRef<any>) {
  const uploadedFiles = event.target.files;
  this.file = uploadedFiles[0];
  
  if (!this.file) {
    this.openAlertMod(template, "Please upload a file.");
    return;
  }

  const formData = new FormData();
  formData.append('file', this.file);
  formData.append('uploadedBy', this.currentUser.empId.toString());


  this.domainService.employeeBulkUpload(formData).pipe(first()).subscribe(
    (response: any) => {
      if (response.serviceStatus === "Success") {
        this.resetFileInput();
        this.openAlertMod(template, response.serviceResponse);
      } else if (response.serviceStatus === "Fail") {
        this.errorMessages = response.serviceResponse;
        this.openerrorModalTempTemp();
        this.resetFileInput();
        event.target.value = '';
      } else {
        this.openAlertMod(template, response.serviceResponse);
        this.resetFileInput();
      }
    },
    (error) => {
      this.resetFileInput();
      this.openAlertMod(template, "An error occurred while processing the upload.");
      console.error(error);
    }
  );
}



 openerrorModalTempTemp() {
  this.errorModalRef = this.modalService.show(this.errorTemplate, { class: 'modal-lg' });
}

closeErrorModal(){
  this.errorModalRef.hide();
}



   




headers = [
  { 'Employee Id': '', 'Billable': '', 'Billable Type': '', 'Gender': '', 'Manager Name': '' }
];
downloadFileTemplate(): void {
  const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(this.headers, { skipHeader: false });
  const wb: XLSX.WorkBook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, 'Template');
  XLSX.writeFile(wb, 'Manager_Mapping_Data_Template.xlsx');
}

headersBilliable = [
  { 'Employee Id': '' , 'Billable': '', 'Billable Type': '', 'Gender': ''}
];
downloadBilliableFileTemplate():void{
  const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(this.headersBilliable, { skipHeader: false });
  const wb: XLSX.WorkBook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, 'Template');
  XLSX.writeFile(wb, 'Billable_Related_Data_Template.xlsx');
}

headersDesignation = [
  { 'Employee Id': '', 'Designation Name': '' }
];
downloadDeginationUploadFileTemplate(): void {
  console.log("Designatin Template is downloaded");
  const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(this.headersDesignation, { skipHeader: false });
  const wb: XLSX.WorkBook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, 'Template');
  XLSX.writeFile(wb, 'Bulk_Designation_Upload_Template.xlsx');
}

  

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  

  //pagination
  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  
 



  //sort & searching
  sortData(sort: Sort){
    //console.log(sort);
    if(sort.active){
      let sortParams:any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

  toggleSearch(){
      this.isSearchEnabled = !this.isSearchEnabled;
      if(!this.isSearchEnabled){
        this.filters = {};
      }
  }

  onSearch(searchData){
    if(this.isSearchEnabled == true){
      this.filters = searchData;
      //console.log("Updated Filter : ", this.filters);
    }
  }


  openConfirmModal(confirmTemplate: TemplateRef<any>, event: any, alertTemplate: TemplateRef<any>) {
    this.pendingFileEvent = event;
    this.pendingAlertTemplate = alertTemplate;
    this.modalRef = this.modalService.show(confirmTemplate, { class: 'modal-sm' });
  }

  proceedUpload() {
    if (this.modalRef) this.modalRef.hide();
    if (this.pendingFileEvent) {
      this.onEmployeeUpload(this.pendingFileEvent, this.pendingAlertTemplate);
      this.pendingFileEvent = null;
    }
  }

  cancelUpload() {
    if (this.modalRef) this.modalRef.hide();
    this.resetFileInput();
    this.pendingFileEvent = null;
    this.file = null;
  }

  private resetFileInput(): void {
  if (this.fileInput && this.fileInput.nativeElement) {
    this.fileInput.nativeElement.value = ''; 
  }
  this.file = null;
  this.pendingFileEvent = null;
}

}
