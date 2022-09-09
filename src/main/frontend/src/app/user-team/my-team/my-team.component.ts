import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { Employee } from 'src/app/models/employee';
import { Leave } from 'src/app/models/leave';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { TeamViewService } from 'src/app/services/team-view.service';
import { LeaveService } from 'src/app/services/leave.service';
import { User } from 'src/app/models/user';
import { Feature } from 'src/app/models/feature';
import { ValidationService } from 'src/app/services/validation.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { Sort } from '@angular/material/sort';

@Component({
  selector: 'app-my-team',
  templateUrl: './my-team.component.html',
  styleUrls: ['./my-team.component.css']
})
export class MyTeamComponent implements OnInit {

  data:string;
  feature = "My Team";
  currentUser: User;
  userMapping: any = {};

  // modal
  alertMessage: any
  modalRef: BsModalRef = new BsModalRef();
  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;

  // flags
  isViewTeam: boolean = true;
  isTeamLeaveHistory: boolean = false;
  isLeaveHistory: boolean = false;
  isCompOffHistory: boolean = false;
  isTeamRequest: boolean = false;
  isLeaveRequest: boolean = false;
  isCompOffRequest: boolean = false;

  // Obj
  leaveObj: Leave = new Leave();
  employeeObj: Employee = new Employee();
  teamViewList: any[] = [];
  teamViewLeaveHistoryList: any[] = [];
  teamViewCompOffHistoryList: any[] = [];
  leaveApplicationList: any[] = [];
  allCompOffApplications: any[] = [];

  //excel
  leaveApplicationDataForExcel: any[];
  allCompOffApplicationsDataForExcel: any[];
  elementName = '';
  excelName = '';

  fromDate:any;
  toDate:any;


  constructor(
    private authenticationService: AuthenticationService,
    private modalService: BsModalService,
    private teamViewService: TeamViewService,
    private leaveService: LeaveService,
    private employeeService: EmployeeService,
    private exportExcelService: ExportExcelService,
    private validationService:ValidationService,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    // Dynamic Subfeature Flags 
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    console.log(this.feature, this.userMapping);

    this.getAllTeamView();
    this.getAllMyTeamsPendingLeaveApplicationsByManagerId();
    this.getPendingCompOffRequestsByManagerId();

    console.log("alert template : ", this.alertTemplate);
    
  }

  viewTeam() {
    this.isViewTeam = true;

    this.isTeamLeaveHistory = false;
    this.isLeaveRequest = false;
    this.isCompOffRequest = false;
    this.isTeamRequest = false;
    this.page=1;
  }

  viewTeamLeaveHistory() {
    this.isTeamLeaveHistory = true;
    this.isLeaveHistory = true;
    this.isCompOffHistory = false;

    this.isLeaveRequest = false;
    this.isCompOffRequest = false;
    this.isViewTeam = false;
    this.isTeamRequest = false;
    this.page=1;
    this.data=''
  }

  viewLeaveHistory() {
    this.isLeaveHistory = true;
    this.isCompOffHistory = false;
    this.fromDate = null;
    this.toDate = null;
    this.teamViewLeaveHistoryList = [];
    this.page=1;
    this.data='';
  }

  viewCompOffHistory() {
    this.isLeaveHistory = false;
    this.isCompOffHistory = true;
    this.fromDate = null;
    this.toDate = null;
    this.teamViewCompOffHistoryList = [];
    this.page=1;
    this.data='';
  }

  viewTeamRequest() {
    this.isTeamRequest = true;
    this.isLeaveRequest = true;
    this.isCompOffRequest = false;

    this.isLeaveHistory = false;
    this.isCompOffHistory = false;
    this.isTeamLeaveHistory = false;
    this.isViewTeam = false;
    this.page=1;
    this.data='';
  }

  viewTeamLeaveRequest() {
    this.isLeaveRequest = true;
    this.isCompOffRequest = false;
    this.page=1;
    this.data='';
  }

  viewTeamCompOffRequest() {
    this.isLeaveRequest = false;
    this.isCompOffRequest = true;
    this.page=1;
    this.data='';
  }

  getAllTeamView() {
    this.isViewTeam = true;
    this.teamViewList = []

    let employeeObj = new Employee();
    employeeObj.empId = this.currentUser.empId;
    this.teamViewService.getAllTeamView(employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.teamViewList = response.serviceResponse;
        console.log("teamViewList : ", this.teamViewList);
      } else {
        console.error(response.serviceResponse);
      }
    });

  }

  getAllTeamLeaveHistoryView(template?: TemplateRef<any>) {
    this.teamViewLeaveHistoryList = []
    
    if(this.toDate){
      if(!this.validationService.validateNullUndefinedEmptyString(this.fromDate)){
        this.alertMessage = "Please enter Start Date !!"
        alert(this.alertMessage);
        //this.openAlertMod(this.alertTemplate, this.alertMessage);
        return false;
      }
  
      if(!this.validationService.validateNullUndefinedEmptyString(this.toDate)){
        this.alertMessage = "Please enter End Date !!"
        alert(this.alertMessage);
        //this.openAlertMod(this.alertTemplate, this.alertMessage);
        return false;
      }
    }else{
      return;
    }

    let leaveObj = new Leave();
    leaveObj.fromDate = this.fromDate;
    leaveObj.toDate = this.toDate;
    leaveObj.empId = this.currentUser.empId;
    this.teamViewService.getAllTeamLeaveHistoryView(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.teamViewLeaveHistoryList = response.serviceResponse;
        console.log("teamViewLeaveHistory : ", this.teamViewLeaveHistoryList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  getAllTeamCompOffHistoryView(template?: TemplateRef<any>) {
    this.teamViewCompOffHistoryList = []

    console.log("alertTemplate : ", this.alertTemplate);

    if(this.toDate){
      if(!this.validationService.validateNullUndefinedEmptyString(this.fromDate)){
        this.alertMessage = "Please enter Start Date !!"
        alert(this.alertMessage);
      //  this.openAlertMod(this.alertTemplate, this.alertMessage);
        return false;
      }
  
      if(!this.validationService.validateNullUndefinedEmptyString(this.toDate)){
        this.alertMessage = "Please enter End Date !!"
        alert(this.alertMessage);
        //this.openAlertMod(this.alertTemplate, this.alertMessage);
        return false;
      }
    }else{
      return;
    }

    let leaveObj = new Leave();
    leaveObj.fromDate = this.fromDate;
    leaveObj.toDate = this.toDate;
    leaveObj.empId = this.currentUser.empId;
    console.log("leaveObj: ", leaveObj)
    this.teamViewService.getAllTeamCompOffHistoryView(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.teamViewCompOffHistoryList = response.serviceResponse;
        console.log("teamViewCompOffHistory : ", this.teamViewCompOffHistoryList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }


  getAllMyTeamsPendingLeaveApplicationsByManagerId() {
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

  onUpdateLeaveStatus(template: TemplateRef<any>, leaveApplication, updatedLeaveStatusId) {
    // 1 = pending , 2 = Approved , 3= Rejected
    leaveApplication.leaveStatusId = updatedLeaveStatusId;
    leaveApplication.leaveStatusUpdatedBy = this.currentUser.empId
    console.log("leaveApplication : ", leaveApplication);

    this.leaveService.updateLeaveStatus(leaveApplication).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.getAllMyTeamsPendingLeaveApplicationsByManagerId()
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  //comOff Applications
  getPendingCompOffRequestsByManagerId() {
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

  onUpdateCompOffStatus(template: TemplateRef<any>, compOffObj, updatedCompOffStatusId) {
    // 1 = pending , 2 = Approved , 3= Rejected

    console.log("template: ", this.alertTemplate );

    compOffObj.leaveStatusId = updatedCompOffStatusId;
    compOffObj.leaveStatusUpdatedBy = this.currentUser.empId

    console.log("Update Comp off : ", compOffObj);
    this.leaveService.updateCompOffById(compOffObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.getPendingCompOffRequestsByManagerId();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  // download excel
  exportToExcel(): void {

      if (this.isViewTeam == true) {
      this.excelName = 'MyTeam.xlsx';

      const onlySpecificDataArr = this.teamViewList.map(
        x => ({
          "Emp Id": x.empId,
          "Name": x.name,
          "Email": x.email,
          "Job Role Name": x.jobRoleName,
          "Mobile No": x.mobileNo,
          "Manager Name": x.managerName
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
    }

      if (this.isLeaveHistory == true) {
      this.excelName = 'MyTeamLeaveHistory.xlsx';

      const onlySpecificDataArr = this.teamViewLeaveHistoryList.map(
        x => ({
          "Created By Name": x.createdByName,
          "From Date": x.fromDate,
          "To Date": x.toDate,
          "Created On": x.createdOn,
          "No Of Days": x.noOfDays,
          "Status": x.status,
          "Reason": x.reason,
          "Leave Type": x.leaveType
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)

    }

      if (this.isCompOffHistory == true) {
      this.excelName = 'MyTeamCompOffHistory.xlsx';

      const onlySpecificDataArr = this.teamViewCompOffHistoryList.map(
        x => ({
          "Created By Name": x.createdByName,
          "From Date": x.fromDate,
          "To Date": x.toDate,
          "Created On": x.createdOn,
          "No Of Days": x.noOfDays,
          "Status": x.status,
          "Reason": x.reason
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
      
    }

    if (this.isLeaveRequest == true) {
        this.excelName = 'MyTeamLeaveRequests.xlsx';
  
          const onlySpecificDataArr = this.leaveApplicationList.map(
            x => ({
              "Leave Type": x.leaveType,
              "From Date": x.fromDate,
              "To Date": x.toDate,
              "No Of Days": x.noOfDays,
              "Status": x.status,
              "Created By Name": x.createdByName,
              "Created On": x.createdOn,
              "Reason": x.reason
            })
          )
          this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
      }

      if (this.isCompOffRequest == true) {
          this.excelName = 'MyTeamCompOffRequests.xlsx';
    
          const onlySpecificDataArr = this.allCompOffApplications.map(
            x => ({
              "Created By Name": x.createdByName,
              "Comp Off Reasons": x.compOffReasons,
              "From Date": x.fromDate,
              "To Date": x.toDate,
              "No Of Days": x.noOfDays,
              "Description": x.description,
              "Status": x.status
            })
          )
          this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
        }
  }

  //myTeam-hierarchy	
  myTeamHierarchy(employeeObj:Employee) {

    this.employeeService.getHierarchyByEmpId(employeeObj).pipe(first()).subscribe((response: any) => {	
      if (response.serviceStatus == "Success") {	
        this.teamViewList = response.serviceResponse;	
        console.log("teamViewList : ", this.teamViewList);	
      } else {	
        console.error(response.serviceResponse);	
      }	
    });	
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
    
  //Sorting team view table 	
  sortViewTeamTable(sort:Sort){	
    console.log(sort);	
    const data=this.teamViewList;	
   // console.log(data , "****************************");	
   if(!sort.active || sort.direction==='')	
   {	
    this.teamViewList=data;	
    return ;	
   }else {	
    this.teamViewList=data.sort(	
      (a , b)=>{	
        const isAsc=sort.direction==='asc';	
        switch(sort.active){	
          case 'employeementId':	
            return compare(a.employeementId , b.employeementId , isAsc)	
            case 'name':	
              return compare(a.name , b.name , isAsc)	
              case 'email':	
                return compare(a.email , b.email , isAsc)	
                case 'jobRoleName':	
                  return compare(a.jobRoleName , b.jobRoleName , isAsc)	
                  case 'mobileNo':	
                    return compare(a.mobileNo , b.mobileNo ,isAsc)	
                    case 'managerName':	
                      return compare(a.managerName , b.managerName , isAsc)	
          default :	
          return 0;	
        }	
      }	
    )	
   }	
    	
    	
  }	

  sortViewLeaveHistory(sort:Sort){	
    console.log(sort);	
    const data=this.teamViewLeaveHistoryList;	
   // console.log(data , "=====================");	
    	
   if(!sort.active || sort.direction==='')	
   {	
    this.teamViewLeaveHistoryList=data;	
    return ;	
   }else {	
    this.teamViewLeaveHistoryList=data.sort(	
      (a , b)=>{	
        const isAsc=sort.direction==='asc';	
        switch(sort.active){	
          case 'createdByName':	
            return compare(a.createdByName , b.createdByName , isAsc)	
            case 'fromDate':	
              return compare(a.fromDate , b.fromDate , isAsc)	
              case 'toDate':	
                return compare(a.toDate , b.toDate , isAsc)	
                case 'createdOn':	
                  return compare(a.createdOn , b.createdOn , isAsc)	
                  case 'noOfDays':	
                    return compare(a.noOfDays , b.noOfDays ,isAsc)	
                    case 'status':	
                      return compare(a.status , b.status , isAsc)	
                       case 'leaveStatusUpdatedByName':	
                         return compare(a.leaveStatusUpdatedByName , b.leaveStatusUpdatedByName , isAsc)	
                         case 'reason':	
                           return compare(a.reason , b.reason , isAsc)	
                            case 'leaveType':	
                             return compare(a.leaveType , b.leaveType , isAsc)	
          default :	
          return 0;	
        }	
      }	
    )	
   }	
    	
    	
  }

  sortViewCompOffLeave(sort:Sort){	
    console.log(sort);	
    const data=this.teamViewCompOffHistoryList;	
 //   console.log(data , "********************************************");	
    if(!sort.active || sort.direction==='')	
   {	
    this.teamViewCompOffHistoryList=data;	
    return ;	
   }else {	
    this.teamViewCompOffHistoryList=data.sort(	
      (a , b)=>{	
        const isAsc=sort.direction==='asc';	
        switch(sort.active){	
          case 'createdByName':	
            return compare(a.createdByName , b.createdByName , isAsc)	
            case 'fromDate':	
              return compare(a.fromDate , b.fromDate , isAsc)	
              case 'toDate':	
                return compare(a.toDate , b.toDate , isAsc)	
                case 'createdOn':	
                  return compare(a.createdOn , b.createdOn , isAsc)	
                  case 'noOfDays':	
                    return compare(a.noOfDays , b.noOfDays ,isAsc)	
                    case 'status':	
                      return compare(a.status , b.status , isAsc)	
                       case 'leaveStatusUpdatedByName':	
                         return compare(a.leaveStatusUpdatedByName , b.leaveStatusUpdatedByName , isAsc)	
                         case 'reason':	
                           return compare(a.reason , b.reason , isAsc)	
                           	
          default :	
          return 0;	
        }	
      }	
    )	
   }	
    	
    	
    	
  }

  sortLeaveRequestTable(sort:Sort){	
    console.log(sort);	
    const data =this.leaveApplicationList;	
    console.log(data , "====================");	
    	
    if(!sort.active || sort.direction==='')	
   {	
    this.leaveApplicationList=data;	
    return ;	
   }else {	
    this.leaveApplicationList=data.sort(	
      (a , b)=>{	
        const isAsc=sort.direction==='asc';	
        switch(sort.active){	
          case 'leaveType':	
            return compare(a.leaveType , b.leaveType , isAsc)	
            case 'fromDate':	
              return compare(a.fromDate , b.fromDate , isAsc)	
              case 'toDate':	
                return compare(a.toDate , b.toDate , isAsc)	
                case 'noOfDays':	
                  return compare(a.noOfDays , b.noOfDays , isAsc)	
                  case 'status':	
                    return compare(a.status , b.status ,isAsc)	
                    case 'createdByName':	
                      return compare(a.createdByName , b.createdByName , isAsc)	
                       case 'createdOn':	
                         return compare(a.createdOn , b.createdOn , isAsc)	
                         case 'reason':	
                           return compare(a.reason , b.reason , isAsc)	
                           	
          default :	
          return 0;	
        }	
      }	
    )	
   }	
    	
  }

  sortCompOffRequestTable(sort:Sort){	
    console.log(sort);	
    const data=this.allCompOffApplications;	
   // console.log(data , "++++++++++++++++++++++");	
    	
   if(!sort.active || sort.direction==='')	
   {	
    this.allCompOffApplications=data;	
    return ;	
   }else {	
    this.allCompOffApplications=data.sort(	
      (a , b)=>{	
        const isAsc=sort.direction==='asc';	
        switch(sort.active){	
          case 'createdByName':	
            return compare(a.createdByName , b.createdByName , isAsc)	
            case 'compOffReasons':	
              return compare(a.compOffReasons , b.compOffReasons , isAsc)	
            case 'fromDate':	
              return compare(a.fromDate , b.fromDate , isAsc)	
              case 'toDate':	
                return compare(a.toDate , b.toDate , isAsc)	
                case 'noOfDays':	
                  return compare(a.noOfDays , b.noOfDays , isAsc)	
                  case 'description':	
                    return compare(a.description , b.description , isAsc)	
                  case 'status':	
                    return compare(a.status , b.status ,isAsc)	
                   	
                           	
          default :	
          return 0;	
        }	
      }	
    )	
   }	
    	
  }	



  
}

function compare(a: number | string, b: number | string, isAsc: boolean) {	
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);	
}
