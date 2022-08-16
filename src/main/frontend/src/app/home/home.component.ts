import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { Leave } from '../models/leave';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { LeaveService } from '../services/leave.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  //modal 
  alertMessage:any;
  modalRef: BsModalRef = new BsModalRef();
  

  feature="Home";
  currentUser:User;
  userMapping:any = {};

  isReqPending:boolean = true;
  leaveApplicationCount:any = 0;
  leaveApplicationList:any[] = [];

  compOffApplicationCount:any = 0;
  allCompOffApplications:any[] = [];

  //export excel

  excelName:any = '';

  constructor(
    private modalService: BsModalService,
    private authenticationService : AuthenticationService,
    private leaveService : LeaveService,
    private exportExcelService: ExportExcelService,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
   }

  ngOnInit(): void {
    this.countAllMyTeamsPendingLeaveApplicationsByManagerId();
    this.countPendingCompOffRequestsByManagerId();
    this.authenticationService.startUserSessionCheck();
  }

  // Leave Applications
  getAllMyTeamsPendingLeaveApplicationsByManagerId(){
    this.leaveApplicationList = []

    let leaveObj = new Leave();
    leaveObj.managerId = this.currentUser.empId;
    this.leaveService.getAllMyTeamsPendingLeaveApplicationsByManagerId(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveApplicationList = response.serviceResponse;
        console.log("leaveApplicationList : ", this.leaveApplicationList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  /* Leave Applications Count
  *  Added by suraj 07/08/2022
  */
  countAllMyTeamsPendingLeaveApplicationsByManagerId(){
    this.leaveApplicationList = []

    let leaveObj = new Leave();
    leaveObj.managerId = this.currentUser.empId;
    this.leaveService.countAllMyTeamsPendingLeaveApplicationsByManagerId(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveApplicationCount = response.serviceResponse.applicationCount;
        console.log("leaveApplicationCount : ", this.leaveApplicationCount);
      } else {
        this.leaveApplicationCount = 0;
        console.error(response.serviceResponse);
      }
    });
  }

  onUpdateLeaveStatus(template: TemplateRef<any>, leaveApplication, updatedLeaveStatusId){
    // 1 = pending , 2 = Approved , 3= Rejected
    leaveApplication.leaveStatusId = updatedLeaveStatusId;
    leaveApplication.leaveStatusUpdatedBy = this.currentUser.empId
    console.log("leaveApplication : ", leaveApplication);
    
    this.leaveService.updateLeaveStatus(leaveApplication).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {        
        this.countAllMyTeamsPendingLeaveApplicationsByManagerId();
        this.getAllMyTeamsPendingLeaveApplicationsByManagerId();
        this.openAlertMod(template, response.serviceResponse);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }


  //comOff Applications
  getPendingCompOffRequestsByManagerId(){
    this.allCompOffApplications = []

    let compOff = new Leave();
    compOff.managerId = this.currentUser.empId;
    this.leaveService.getPendingCompOffRequestsByManagerId(compOff).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allCompOffApplications = response.serviceResponse;
        console.log("allCompOffApplications : ", this.allCompOffApplications);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  /* Leave Applications Count
  *  Added by suraj 07/08/2022
  */
  countPendingCompOffRequestsByManagerId(){
    this.allCompOffApplications = []

    let compOff = new Leave();
    compOff.managerId = this.currentUser.empId;
    this.leaveService.countPendingCompOffRequestsByManagerId(compOff).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.compOffApplicationCount =  response.serviceResponse.applicationCount;
        console.log("CompOffApplicationCount : ", this.compOffApplicationCount);
      } else {
        this.compOffApplicationCount =  0;
        console.error(response.serviceResponse);
      }
    });
  }

  onUpdateCompOffStatus(template: TemplateRef<any>, compOffObj, updatedCompOffStatusId){
    // 1 = pending , 2 = Approved , 3= Rejected
    compOffObj.leaveStatusId = updatedCompOffStatusId;
    compOffObj.leaveStatusUpdatedBy = this.currentUser.empId
    console.log("Update Comp off : ", compOffObj);
    this.leaveService.updateCompOffById(compOffObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {        
        this.countPendingCompOffRequestsByManagerId();
        this.getPendingCompOffRequestsByManagerId();
        this.openAlertMod(template, response.serviceResponse);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  //export to excel

  exportToExcelForLeave() {
    this.excelName = 'leaveApplication.xlsx';
    	
        const onlySpecificDataArr: Partial<Leave>[] = this.leaveApplicationList.map(	
          x => ({	
              leaveType: x.leaveType,
              fromDate: x.fromDate,
              toDate: x.toDate,
              noOfDays: x.noOfDays,
              status: x.status,
              createdByName: x.createdByName,
              createdOn: x.createdOn,
              reason: x.reason	
          })	
        )	
        this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr,this.excelName)	
      }

  exportToExcelForCompOff() {
    this.excelName = 'leaveApplication.xlsx';
    	
        const onlySpecificDataArr: Partial<Leave>[] = this.allCompOffApplications.map(	
          x => ({	
            createdByName: x.createdByName,
            compOffReasons: x.compOffReasons,
            fromDate: x.fromDate,
            toDate: x.toDate,
            noOfDays: x.noOfDays,
            description: x.description,
            status: x.status
          })	
        )	
        this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr,this.excelName)	
      }

  //pagination 

  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  // modals
  openReqMod(template: TemplateRef<any>) {
    this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }
}
