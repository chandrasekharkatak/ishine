import { LocationStrategy } from '@angular/common';
import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import * as moment from 'moment';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Feature } from 'src/app/models/feature';
import { TimesheetRejectReason } from 'src/app/models/timesheetRejectionReasons';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { TimesheetService } from 'src/app/services/timesheet.service';
import { ValidationService } from 'src/app/services/validation.service';

@Component({
  standalone: false,
  selector: 'app-timesheet-config',
  templateUrl: './timesheet-config.component.html',
  styleUrls: ['./timesheet-config.component.css']
})
export class TimesheetConfigComponent implements OnInit {

  rejectReasons: boolean = false;
  isCreation: boolean = false;
  isUpdation: boolean = false;
  isForm: boolean = false;
  isTable: boolean = false;
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;
  isSearchEnabled:boolean = false;
  name = 'Timesheet Reject Reasons.xlsx';
  rejectColumns:any[] = ['blank','rejectionReason','createdByName','createdOn','updatedByName','updatedOn',''];
  excelName: string;
  tableName: string;
  filters:any = {};
  allRejectReasons: TimesheetRejectReason[] = [];
  rejectReasonObj: TimesheetRejectReason = new TimesheetRejectReason;
  rejectReasonForExcel: TimesheetRejectReason[] = [];
  feature = "Timesheets Config";
  currentUser: User;
  userMapping: any = {};
  alertMessage: any;
  modalRef:NgbModalRef;
  @ViewChild("alert_message_show_table")
  alertShowTable: TemplateRef<any>
  alertShowTableRef:NgbModalRef;
  @ViewChild("alert_message")
  alertMsg: TemplateRef<any>
  alertMsgRef:NgbModalRef;

  constructor(private timesheetService: TimesheetService,
    private exportExcelService: ExportExcelService,
    private authenticationService: AuthenticationService,
    private locationStrategy:LocationStrategy,
    private validationService: ValidationService,
    private modalService: NgbModal,
  ) { 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    // console.log(this.feature, this.userMapping);

    this.sectionViewInit();
    this.preventBackButton();
  }

  preventBackButton(){
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(()=>{
      history.pushState(null, null, location.href);
    })
  }

  sectionViewInit() {
    if (this.userMapping.view_all_reject_reason || this.userMapping.update_reject_reason || this.userMapping.delete__reject_reason) {
      this.showTable();
    }
  }

  showCreateForm() {
    this.isForm = true;
    this.isCreation = true;
    this.isTable = false;
    this.isUpdation = false;
    this.reset();
  }

  showTable() {
    this.rejectReasons = true;
    this.isTable = true;
    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.reset();
    this.getRejectionReason();
  }

  reset(){
    this.rejectReasonObj.rejectionId = '';
    this.rejectReasonObj.rejectionReason = '';
    this.rejectReasonObj.active = '';
    this.rejectReasonObj.createdBy = '';
    this.rejectReasonObj.createdOn = '';
    this.rejectReasonObj.createdByName = '';
    this.rejectReasonObj.updatedBy = '';
    this.rejectReasonObj.updatedOn = '';
    this.rejectReasonObj.updatedByName = '';
  }

  getRejectionReason(){
    this.timesheetService.getRejectionReason().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allRejectReasons = response.serviceResponse;
        this.allRejectReasons.forEach(reject => {
          reject.createdOn = (reject.createdOn)? moment(reject.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          reject.updatedOn = (reject.updatedOn)? moment(reject.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
        });
      } else {
        alert(response.serviceResponse)
      }
    });
  }

  getRejectionReasonById(rejectionId:any){
    this.timesheetService.getRejectionReasonById(rejectionId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.rejectReasonObj = response.serviceResponse;
      } else {
        alert(response.serviceResponse)
      }
    });
  }

  setTimesheetRejectReason(rejectReasonObj: TimesheetRejectReason,template: TemplateRef<any>){
    if(this.isCreation){
      this.rejectReasonObj.rejectionId='';
      this.rejectReasonObj.createdBy=this.currentUser.empId;
    }

    if(this.isUpdation){
      this.rejectReasonObj.updatedBy=this.currentUser.empId;
    }
    
    this.timesheetService.setTimesheetRejectReason(rejectReasonObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        rejectReasonObj = response.serviceResponse;
        this.openAlertModShowTable(response.serviceResponse);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }

    });
  }

  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  sortData(sort: Sort){
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
    this.filters = searchData;
  }

  exportToExcel(): void {
    this.timesheetService.getRejectionReason().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.rejectReasonForExcel = response.serviceResponse;
        //console.log("response.serviceResponse: ",response.serviceResponse);
      }

      const onlySpecificDataArr = this.rejectReasonForExcel.map(
        x => ({
          "Department Name": x.rejectionReason,
          "Created by": x.createdByName,
          "Created on": (x.createdOn)? moment(x.createdOn).format(AppComponent.DATETIME_FORMAT) : null,
          "Updaeted by": x.updatedByName ??' - ',
          "Updated On": (x.updatedOn)? moment(x.updatedOn).format(AppComponent.DATETIME_FORMAT) : ' - ',
        })
      )
      //console.log("Excel Array: ",onlySpecificDataArr);
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.name)
    });
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.close();
  }

  validateRejectReason(reason: string, template: TemplateRef<any>): boolean {
    const trimmedReason = reason?.trim();

    if (!trimmedReason) {
      this.openAlertMod(template, "Reject reason cannot be empty.");
      return false;
    }

    if (trimmedReason.length < 5 || trimmedReason.length > 250) {
      this.openAlertMod(template, "Reject reason must be between 5 and 250 characters.");
      return false;
    }

    const regex = /^[a-zA-Z0-9.,'"\-\s]+$/;
    if (!regex.test(trimmedReason)) {
      this.openAlertMod(template, "Reject reason contains invalid characters.");
      return false;
    }

    return true; 
  }

  showUpdateForm(rejectionId:any) {
    this.isForm = true;
    this.isTable = false;
    this.isUpdation = true;
    this.isCreation = false;

    this.getRejectionReasonById(rejectionId);
  }

  updateActiveByRejectIdId(rejectReasonObj:TimesheetRejectReason){
    rejectReasonObj.updatedBy=this.currentUser.empId;
    rejectReasonObj.active = !rejectReasonObj.active;
    rejectReasonObj.createdOn = '';
    rejectReasonObj.updatedOn = '';
    this.timesheetService.updateActiveByRejectIdId(rejectReasonObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertModShowTable(response.serviceResponse);
      } else {
        this.openAlertModWithoutTable(response.serviceResponse);
      }
    });
  }

  openAlertModShowTable( message: any) {
    this.alertShowTableRef = this.modalService.open(this.alertShowTable, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  closeAlertShowTable(){
    this.alertShowTableRef.close();
    this.showTable;
  }

  openAlertModWithoutTable( message: any) {
    this.alertMsgRef = this.modalService.open(this.alertMsg, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  closeAlertWithoutTable(){
    this.alertMsgRef.close();
  }

}
