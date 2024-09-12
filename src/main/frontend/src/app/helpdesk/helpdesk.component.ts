import { AfterViewInit, Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { HelpService } from '../services/help.service';
import * as moment from 'moment';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { Appreciation } from 'src/app/models/appreciation';
import { User } from 'src/app/models/user';
import { UtilityService } from 'src/app/services/utility.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { saveAs } from "file-saver";
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { ActivatedRoute, Params } from '@angular/router';
import { Help } from '../models/help';

@Component({
  selector: 'app-helpdesk',
  templateUrl: './helpdesk.component.html',
  styleUrls: ['./helpdesk.component.css']
})
export class HelpdeskComponent implements OnInit, AfterViewInit {

  @ViewChild("preview_document")
  previewDocument: TemplateRef<any>;

  modalRef: BsModalRef = new BsModalRef();
  alertMessage: any;

  document:any[] = [];

  currentUser: User;

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;
  data: any;
  src:any;
  fileName:any
  myAppreciationexportExcel: any[] = [];
  currentDocument:any
  isMyAppreciation:boolean = false;
  isTeamAppreciation:boolean=false;
  isReceived = true;
  isGiven = false;
  appreciatedByHeader = 'Appreciated By';
  filters:any = {};
  isSearchEnabled:boolean = false;
  documentsColumns:any[] = ['blank','fileName','helpDocumentName','createdByName','createdOn'];
  startDate: any;
  endDate: any;
  myAppreciationList: any[] = [];
  _myAppreciationList : any;
  teamAppreciationList: any[] = [];
  receivedAppreciations: any[] = [];
  givenAppreciations: any[] = [];
  receivedColumns:any[] = ['blank' , 'appreciationDate','appreciationEventName','appreciationBy','appreciationByName','appreciateType','comment'];
  givenColumns:any[] = ['blank','appreciationDate','appreciationEventName','appreciationTo','appreciationToName','appreciateType','comment'];
  teamColumns: any[] = ['blank','appreciationDate','appreciationEventName','appreciationBy','appreciationByName','appreciationTo','appreciationToName','appreciateType','comment'];

  constructor(
    private helpService: HelpService,
    private authenticationService: AuthenticationService,
    private sanitizer: DomSanitizer,
    private modalService: BsModalService,
    private exportExcelService: ExportExcelService,
    private utilityService:UtilityService,
    private route: ActivatedRoute,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
   }


  ngAfterViewInit(): void {
    //  this.sectionViewInit();
  }


  ngOnInit(): void {
    this.getAllHelpDocument();
    this.myAppreciationTab();
    // this.route.params.subscribe((params:Params) => {
    //   this.currentDocument = params['id'];
    // });

  }

  // sectionViewInit(){
  //   if(this.currentDocument != undefined && this.currentDocument != null){
  //     let helpObj = new Help();
  //     helpObj.helpDocId = this.currentDocument;
  //     this.previewHelpDocument(this.previewDocument, helpObj);
  //   }
  // }

  myAppreciationTab(){
    this.isMyAppreciation = true;
    this.isTeamAppreciation = false;
    this.receivedAppreciation(); // Default to Received tab when "My Appreciation" is active
  }
  teamAppreciationTab(){
    this.isMyAppreciation = false;
    this.isTeamAppreciation = true; 
  }
  receivedAppreciation() {
    this.isReceived = true;
    this.isGiven = false;
    this.appreciatedByHeader = 'Appreciated By';
    this.filterAppreciations();
  }
  givenAppreciation() {
    this.isReceived = false;
    this.isGiven = true;
    this.appreciatedByHeader = 'Appreciated To';
    this.filterAppreciations();
  }

  // filterAppreciations() {
  //   this.receivedAppreciations = this._myAppreciationList.filter(appreciation =>
  //     appreciation.appreciationTo.replace('A-', '') === this.currentUser.employeementId
  //   );

  //   this.givenAppreciations = this._myAppreciationList.filter(appreciation =>
  //     appreciation.appreciationBy.replace('A-', '') === this.currentUser.employeementId
  //   );
  // }

  filterAppreciations() {
    const formattedEmpId = `A-${this.currentUser.employeementId}`;
  
    this.receivedAppreciations = this._myAppreciationList.filter(appreciation =>
      appreciation.appreciationTo === formattedEmpId
    );
  
    this.givenAppreciations = this._myAppreciationList.filter(appreciation =>
      appreciation.appreciationBy === formattedEmpId
    );
  }
  

  getMyAppreciationDetails(){
    this.sortColumn=[];
    this.sortColumnType=[];
    this.sortDirection='';
    let appreciationObj = new Appreciation();
    appreciationObj.employeementId = this.currentUser.employeementId;
    appreciationObj.startDate =this.startDate;
    appreciationObj.endDate = this.endDate;
    this.helpService.getMyAppreciationDetails(appreciationObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.myAppreciationList= response.serviceResponse;
        this.myAppreciationList.forEach(appObj => {
          appObj.appreciationDate = (appObj.appreciationDate) 
              ? moment(appObj.appreciationDate).format(AppComponent.DATETIME_FORMAT) 
              : null;
          appObj.appreciationBy = this.utilityService.appendEmployeementid(appObj.appreciationBy);
          appObj.appreciationTo = this.utilityService.appendEmployeementid(appObj.appreciationTo);
        });
        this._myAppreciationList = this.myAppreciationList;
        this.filterAppreciations();
        console.log("myAppreciationList : ", this.myAppreciationList);
      } else {
        console.error(response.serviceResponse);
      }
    });

  }

  name = 'ReceivedAppreciations.xlsx';
  exportReceivedAppreciationsToExcel(): void {
    const filteredData = this.receivedAppreciations.map(x => ({
      "Date": x.appreciationDate ? moment(x.appreciationDate).format('DD-MM-YYYY') : null,
      "Appreciation Event": x.appreciationEventName,
      "Employee Id": "A-".concat(x.appreciationBy),
      "Appreciated By": x.appreciationByName,
      "Appreciation Type": x.appreciateType,
      "Comment": x.comment
    }));

    this.exportExcelService.exportTableDataToExcel(filteredData, this.name);
  }
   

  namee = 'GivenAppreciations.xlsx';
  exportGivenAppreciationsToExcel(): void {
    const filteredData = this.givenAppreciations.map(x => ({
      "Date": x.appreciationDate ? moment(x.appreciationDate).format('DD-MM-YYYY') : null,
      "Appreciation Event": x.appreciationEventName,
      "Employee Id": "A-".concat(x.appreciationTo),
      "Appreciated To": x.appreciationToName,
      "Appreciation Type": x.appreciateType,
      "Comment": x.comment
    }));

    this.exportExcelService.exportTableDataToExcel(filteredData, this.namee);
  }

  nameee = 'MyTeamAppreciations.xlsx';
  exportMyTeamAppreciationsToExcel(): void {
    const filteredData = this.teamAppreciationList.map(x => ({
      "Date": x.appreciationDate ? moment(x.appreciationDate).format('DD-MM-YYYY') : null,
      "Appreciation Event": x.appreciationEventName,
      "Employee Id (By)": x.appreciationBy ? "A-".concat(x.appreciationBy) : null,
      "Appreciated By": x.appreciationByName,
      "Employee Id (To)": x.appreciationTo ? "A-".concat(x.appreciationTo) : null,
      "Appreciated To": x.appreciationToName,
      "Appreciation Type": x.appreciateType,
      "Comment": x.comment
    }));

    this.exportExcelService.exportTableDataToExcel(filteredData, this.nameee);
  }

  getTeamAppreciationDetails(){
    this.sortColumn=[];
    this.sortColumnType=[];
    this.sortDirection='';
    let appreciationObj = new Appreciation();
    appreciationObj.empId = this.currentUser.empId;
    appreciationObj.startDate =this.startDate;
    appreciationObj.endDate = this.endDate;
    this.helpService.getTeamAppreciationDetails(appreciationObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.teamAppreciationList= response.serviceResponse;
        console.log("teamAppreciationList : ", this.teamAppreciationList);

        this.teamAppreciationList.forEach(appObj => {
          appObj.appreciationDate = (appObj.appreciationDate) 
              ? moment(appObj.appreciationDate).format(AppComponent.DATETIME_FORMAT) 
              : null;
          appObj.appreciationBy = this.utilityService.appendEmployeementid(appObj.appreciationBy);
          appObj.appreciationTo = this.utilityService.appendEmployeementid(appObj.appreciationTo);
        });
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  formatEmployeeId(employeeId: number): string {
    // Ensure employeeId is numeric and add 'A-' prefix
    return `A-${employeeId}`;
  }

  

  getAllHelpDocument(){
    this.sortColumn=[];
    this.sortColumnType=[];
    this.sortDirection='';
    this.data='';
    this.document = [];
    this.helpService.getAllHelpDocument().pipe(first()).subscribe((response:any) => {
      if (response.serviceStatus == "Success") {
        this.document =  response.serviceResponse;
        this.document.forEach(doc => {
          doc.createdOn = (doc.createdOn)? moment(doc.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        });
        //console.log("DocumentList : ", this.document);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  downloadFile(doc: any) {
    this.helpService.downloadHelpDocument(doc.helpDocId).subscribe(blob => saveAs(blob,doc.fileName));
  }

  previewHelpDocument(template: TemplateRef<any>,doc: any) {
    this.src = null;
    this.fileName = "HelpDocument";

    this.helpService.downloadHelpDocument(doc.helpDocId).pipe(first()).subscribe((response:any) => {
      const blob = new Blob([response], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;

      this.src =  a.href;

      if(this.src != null){
        this.openPreviewDocument(template);
      }
    });
  }

  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  sortData(sort: Sort){	
    //console.log(sort);
    if(sort.active){
      let sortParams:any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;      
    }
  }
  // sortData(sort: Sort){	
  //   if(sort.active){
  //     let sortParams: string[] = sort.active.split("|");
  //     this.sortColumn = sortParams[0];
  //     this.sortColumnType = sortParams[1];
  //     this.sortDirection = sort.direction;
  
  //     // Check if sorting by date
  //     if(this.sortColumnType === 'date') {
  //       this.receivedAppreciations.sort((a, b) => {
  //         const dateA = new Date(a[this.sortColumn]);
  //         const dateB = new Date(b[this.sortColumn]);
  //         return (this.sortDirection === 'asc') ? dateA.getTime() - dateB.getTime() : dateB.getTime() - dateA.getTime();
  //       });
  //     }
  //     // Check if sorting by string
  //     else if(this.sortColumnType === 'string') {
  //       this.receivedAppreciations.sort((a, b) => {
  //         const valueA = a[this.sortColumn].toLowerCase();
  //         const valueB = b[this.sortColumn].toLowerCase();
  //         if (this.sortDirection === 'asc') {
  //           return valueA < valueB ? -1 : valueA > valueB ? 1 : 0;
  //         } else {
  //           return valueA > valueB ? -1 : valueA < valueB ? 1 : 0;
  //         }
  //       });
  //     }
  //   }
  // }
  
  
  toggleSearch(){
    this.sortColumn=[];
    this.sortColumnType=[];
    this.sortDirection='';
    this.isSearchEnabled = !this.isSearchEnabled;
    if(!this.isSearchEnabled){
      this.filters = {};
    }
  }

  onSearch(searchData){
    this.filters = searchData;
    //console.log("Updated Filter : ", this.filters);
  }

  // Modal

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  openPreviewDocument(template: TemplateRef<any>){
    this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
  }

  cancelRequest() {
    this.modalRef.hide();
  }
}
