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
import { HierarchyUser } from 'src/app/models/hierarchyUser';
import { LocationStrategy } from '@angular/common';
import * as moment from 'moment';
import { AppComponent } from 'src/app/app.component';

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

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;

  // modal
  alertMessage: any
  modalRef: BsModalRef = new BsModalRef();
  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;
  @ViewChild("revoke_template") revokeTemplate: TemplateRef<any>;
  revoke_template: TemplateRef<any>;
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
  breadCrumbs:any[] = [];

  //excel
  leaveApplicationDataForExcel: any[];
  allCompOffApplicationsDataForExcel: any[];
  elementName = '';
  excelName = '';

  fromDate:any;
  toDate:any;
  managerList:any[];
  items = 10;
  bulkTeamLeaveReject : any = [];
  bulkTeamLeaveApprove: any = [];
  isSelectAll:boolean = false;
  isSelect:boolean = false;

  isHierarchyChart:boolean = false;
  isHierarchyTable:boolean = false;
  
  nodes: any = [];

  isLeaveHistoryOfDepartment:boolean = false;
  departmentLeaveHistoryList:any[] = [];

  isActionEnabled:boolean = false;

  constructor(
    private authenticationService: AuthenticationService,
    private modalService: BsModalService,
    private teamViewService: TeamViewService,
    private leaveService: LeaveService,
    private employeeService: EmployeeService,
    private exportExcelService: ExportExcelService,
    public validationService:ValidationService,
    private locationStrategy: LocationStrategy
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
    if(this.userMapping.view_my_team){
      this.viewTeam();
    }else if(this.userMapping.view_team_leave_history){
      this.viewTeamLeaveHistory();
    }else if (this.userMapping.view_team_all_requests || this.userMapping.update_pending_req){
      this.viewTeamRequest();
    }
  }

  viewTeam() {
    this.isViewTeam = true;

    this.isTeamLeaveHistory = false;
    this.isLeaveRequest = false;
    this.isCompOffRequest = false;
    this.isTeamRequest = false;
    this.page=1;
    this.isHierarchyTable = true;
    this.isHierarchyChart = false;

    this.getAllManagers();
    // this.getAllTeamView();    
    this.breadCrumbs = [];
    this.breadCrumbs.push(this.breadCrumbs.push({'empId':this.currentUser.empId,'name': this.currentUser.name.concat(" > ")}));
    let employeeObj = new Employee();
    employeeObj.empId = this.currentUser.empId;
    employeeObj.managerId = this.currentUser.managerId;
    this.myTeamHierarchy(employeeObj); 
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
    this.isHierarchyChart = false;
    this.isHierarchyTable = false;
    this.isLeaveHistoryOfDepartment = false;
    this.fromDate = null;
    this.toDate = null;
    this.teamViewLeaveHistoryList = [];
    this.departmentLeaveHistoryList = [];

    this.getAllMyTeamsPendingLeaveApplicationsByManagerId();
  }

  viewLeaveHistory() {
    this.isLeaveHistory = true;
    this.isCompOffHistory = false;
    this.fromDate = null;
    this.toDate = null;
    this.teamViewLeaveHistoryList = [];
    this.page=1;
    this.data='';
    this.isHierarchyChart = false;
    this.isHierarchyTable = false;
    this.isLeaveHistoryOfDepartment = false;
  }

  viewCompOffHistory() {
    this.isLeaveHistory = false;
    this.isCompOffHistory = true;
    this.fromDate = null;
    this.toDate = null;
    this.teamViewCompOffHistoryList = [];
    this.page=1;
    this.data='';
    this.isLeaveHistoryOfDepartment = false;
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
    this.isHierarchyChart = false;
    this.isHierarchyTable = false;

    this.getAllMyTeamsPendingLeaveApplicationsByManagerId();
    this.getPendingCompOffRequestsByManagerId();
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

  getAllManagers() {
    this.employeeService.getAllManagers().pipe(first()).subscribe((response : any)=>{
      if (response.serviceStatus == "Success") {
        this.managerList = response.serviceResponse;
        console.log("managerList : ", this.managerList);
      }
      else {
        console.error(response.serviceResponse);
      }

    });
  }

  getAllTeamView() {
    this.teamViewList = []

  //  let managerList = [{managerId:10},{managerId:11},{managerId:16},{managerId:12}];

    let employeeObj = new Employee();
    employeeObj.empId = this.currentUser.empId;
    this.teamViewService.getAllTeamView(employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.teamViewList = response.serviceResponse;

        for(let y of this.teamViewList){
          y.employeementId = "A-".concat(y.employeementId);
        }
        console.log("teamViewList : ", this.teamViewList);         

      } else {
        console.error(response.serviceResponse);
      }
    });

  }  

  getAllTeamLeaveHistoryView(template?: TemplateRef<any>) {
    this.teamViewLeaveHistoryList = []
    
    if(this.isLeaveHistoryOfDepartment == false){
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
          this.teamViewLeaveHistoryList.forEach(leaveHistory => {
            leaveHistory.fromDate = (leaveHistory.fromDate)? moment(leaveHistory.fromDate).format(AppComponent.DATE_FORMAT) : null,
            leaveHistory.toDate = (leaveHistory.toDate)? moment(leaveHistory.toDate).format(AppComponent.DATE_FORMAT) : null,
            leaveHistory.createdOn = (leaveHistory.createdOn)? moment(leaveHistory.createdOn).format(AppComponent.DATE_FORMAT) : null
          });
          console.log("teamViewLeaveHistory : ", this.teamViewLeaveHistoryList);
        } else {
          console.error(response.serviceResponse);
        }
      });
    }
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
        this.teamViewCompOffHistoryList.forEach(compOffHistory => {
          compOffHistory.fromDate = (compOffHistory.fromDate)? moment(compOffHistory.fromDate).format(AppComponent.DATE_FORMAT) : null,
          compOffHistory.toDate = (compOffHistory.toDate)? moment(compOffHistory.toDate).format(AppComponent.DATE_FORMAT) : null,
          compOffHistory.createdOn = (compOffHistory.createdOn)? moment(compOffHistory.createdOn).format(AppComponent.DATE_FORMAT) : null
        });
        console.log("teamViewCompOffHistory : ", this.teamViewCompOffHistoryList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }


  getAllMyTeamsPendingLeaveApplicationsByManagerId() {
    this.leaveApplicationList = []
    this.bulkTeamLeaveApprove = []
    this.bulkTeamLeaveReject = []
    this.isSelectAll = false

    let leaveObj = new Leave();
    leaveObj.managerId = this.currentUser.empId;
    this.leaveService.getAllMyTeamsPendingLeaveApplicationsByManagerId(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveApplicationList = response.serviceResponse;
        this.leaveApplicationList.forEach(leaveApp => {
          leaveApp.fromDate = (leaveApp.fromDate)? moment(leaveApp.fromDate).format(AppComponent.DATE_FORMAT) : null,
          leaveApp.toDate = (leaveApp.toDate)? moment(leaveApp.toDate).format(AppComponent.DATE_FORMAT) : null,
          leaveApp.createdOn = (leaveApp.createdOn)? moment(leaveApp.createdOn).format(AppComponent.DATETIME_FORMAT) : null
        });
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
    leaveApplication.approverEmail = this.currentUser.email;	

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

   // single leave reject modal
   onSingleReject(template: TemplateRef<any> , ){
    this.onUpdateLeaveStatus(template, this.leaveObj,3);
  }

  // openLeaveRejectModal
  openLeaveRejectModal(template: TemplateRef<any>, leave: any){
    this.cancelRequest();
    this.leaveObj = leave
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }

  
  //comOff Applications
  getPendingCompOffRequestsByManagerId() {
    this.allCompOffApplications = []

    let compOff = new Leave();
    compOff.managerId = this.currentUser.empId;
    this.leaveService.getPendingCompOffRequestsByManagerId(compOff).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allCompOffApplications = response.serviceResponse;
        this.allCompOffApplications.forEach(compOffApp => {
          compOffApp.fromDate = (compOffApp.fromDate)? moment(compOffApp.fromDate).format(AppComponent.DATE_FORMAT) : null,
          compOffApp.toDate = (compOffApp.toDate)? moment(compOffApp.toDate).format(AppComponent.DATE_FORMAT) : null
        });
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
          "Employee Id": x.employeementId,
          "Name": x.name,
          "Email": x.email,
          "Designation": x.jobRoleName,
          "Mobile No": x.mobileNo,
          "Reports To": x.managerName
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
    }

      if (this.isLeaveHistory == true) {
      this.excelName = 'MyTeamLeaveHistory.xlsx';

      const onlySpecificDataArr = this.teamViewLeaveHistoryList.map(
        x => ({
          "Employee": x.createdByName,
          "From": x.fromDate,
          "To": x.toDate,
          "Apply Date": x.createdOn,
          "Duration": x.noOfDays,
          "Status": x.status,
          "Approved/Rejected By":x.leaveStatusUpdatedByName,
          "Leave Reason": x.reason,
          "Type": x.leaveType,
          "Remarks":x.remark
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)

    }

      if (this.isCompOffHistory == true) {
      this.excelName = 'MyTeamCompOffHistory.xlsx';

      const onlySpecificDataArr = this.teamViewCompOffHistoryList.map(
        x => ({
          "Employee": x.createdByName,
          "From": x.fromDate,
          "To": x.toDate,
          "Apply Date": x.createdOn,
          "Duration": x.noOfDays,
          "Status": x.status,
          "Approved/Rejected By":x.leaveStatusUpdatedByName,
          "Comp-Off Reason": x.reason
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
      
    }

    if (this.isLeaveRequest == true) {
        this.excelName = 'MyTeamLeaveRequests.xlsx';
  
          const onlySpecificDataArr = this.leaveApplicationList.map(
            x => ({
              "Name":x.employeeName,
              "Leave Type": x.leaveType,
              "From Date": x.fromDate,
              "To Date": x.toDate,
              "Duration": x.noOfDays,
              "Status": x.status,
              "Applied By": x.createdByName,
              "Applied On": x.createdOn,
              "Reason": x.reason
            })
          )
          this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
      }

      if (this.isCompOffRequest == true) {
          this.excelName = 'MyTeamCompOffRequests.xlsx';
    
          const onlySpecificDataArr = this.allCompOffApplications.map(
            x => ({
              "Applied By": x.createdByName,
              "Applied For": x.compOffReasons,
              "From Date": x.fromDate,
              "To Date": x.toDate,
              "Duration": x.noOfDays,
              "Description": x.description,
              "Status": x.status
            })
          )
          this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
        }
  }

  hierarchyBreadCrumb(index){	
    this.breadCrumbs.splice(index + 1);
    this.employeeObj.name = this.breadCrumbs[this.breadCrumbs.length - 1];	
    this.myTeamHierarchy(this.employeeObj.name);
  }

  //myTeam-hierarchy	
  myTeamHierarchy(employeeObj:Employee) {
    let employee = Object.assign({}, employeeObj);
    employee.employeementId = employee.employeementId?.substring(2);

    this.employeeService.getHierarchyByEmpId(employee).pipe(first()).subscribe((response: any) => {	
      if (response.serviceStatus == "Success") {	
        this.teamViewList = response.serviceResponse;
        for(let teamMember of this.teamViewList){
          teamMember.employeementId = "A-".concat(teamMember.employeementId);
        }

        for(let x of this.teamViewList){
          x.isHierarchy = false;
         let temp = this.managerList.find(manager => manager.managerId == x.empId);
         
          if(temp != undefined) x.isHierarchy = true;          
       }

        if(!employeeObj.name.includes(">")){
          this.breadCrumbs.push({'empId':employeeObj.empId,'name': employeeObj.name.concat(" > ")});
        }
        console.log("teamViewList : ", this.teamViewList);	
      } else {	
        console.error(response.serviceResponse);	
      }	
    });	
  }

  toggleHierarchyView(event){
    let employeeObj = new Employee();
    employeeObj.empId = this.currentUser.empId;
    employeeObj.managerId = this.currentUser.managerId;
   
    if(event.target.checked){
      this.isHierarchyChart = true;
      this.isHierarchyTable = false;
      this.myTeamHierarchyChart(employeeObj)
    }else{
      this.isHierarchyTable = true;
      this.isHierarchyChart = false;
      this.myTeamHierarchy(employeeObj);
      this.viewTeam();
    }
  }

  myTeamHierarchyChart(employeeObj:Employee) {
    this.nodes = [];
    let employee = Object.assign({}, employeeObj);
    employee.employeementId = employee.employeementId?.substring(2);

    this.employeeService.getHierarchyChartByEmpId(employee).pipe(first()).subscribe((response: any) => {	
      if (response.serviceStatus == "Success") {
        this.teamViewList = response.serviceResponse;
        for(let teamMember of this.teamViewList){
          teamMember.employeementId = "A-".concat(teamMember.employeementId);
        }
        console.log("teamViewList : ", this.teamViewList);

        let manager = this.teamViewList.find(employee => employee.hierarchyType == "Manager");
        let coworkers = this.teamViewList.filter(employee => employee.hierarchyType == "Co-Worker");
        let self = this.teamViewList.find(employee => employee.hierarchyType == "Self");
        let reportees = this.teamViewList.filter(employee => employee.hierarchyType == "Reportee");
        
        console.log("Manager : ", manager);
        console.log("coworkers : ("+ coworkers.length+")", coworkers);
        console.log("self : ", self);
        console.log("reportees :  ("+ reportees.length+")", reportees);
        


        let managerNode = new HierarchyUser();
        let user = new HierarchyUser();
        let reporteeList: HierarchyUser[];
        let coWorkerList: HierarchyUser[];
        let MID_COUNTER = 0

        if(manager){
          managerNode.name = manager.name;
          managerNode.cssClass = manager.hierarchyType;
          managerNode.title = `${manager.jobRoleName}, ${manager.departmentName} ${(manager.reporteeCount !== 0)? `, ${manager.reporteeCount} reportee(s)`: ``}`;
          managerNode.empId = manager.empId;
          managerNode.managerId = manager.managerId;
        }else {
          console.error("Manager Not found.");
        }

        if(coworkers){
            MID_COUNTER = Math.floor(coworkers.length/2);
            coWorkerList = coworkers.map(employee => {
              const name = employee.name;
              const cssClass = employee.hierarchyType;
              const title = `${employee.jobRoleName}, ${employee.departmentName} ${(employee.reporteeCount !== 0)? `, ${employee.reporteeCount} reportee(s)`: ``}`;
              const empId = employee.empId;
              const managerId = employee.managerId;
              return new HierarchyUser(name, cssClass, title, empId, managerId);
            });
          }else{
            console.error("Co-Workers Not found.");
          }

          if(self){
            user.name = self.name;
            user.cssClass = self.hierarchyType;
            user.title = `${self.jobRoleName}, ${self.departmentName} ${(self.reporteeCount !== 0)? `, ${self.reporteeCount} reportee(s)`: ``}`;
            user.empId = self.empId;
            user.managerId = self.managerId;
          }else{
            console.error("User Not found.");
          }

          if(reportees){
            reporteeList = reportees.map(employee => {
              const name = employee.name;
              const cssClass = employee.hierarchyType;
              const title = `${employee.jobRoleName}, ${employee.departmentName} ${(employee.reporteeCount !== 0)? `, ${employee.reporteeCount} reportee(s)`: ``}`;
              const empId = employee.empId;
              const managerId = employee.managerId;
              return new HierarchyUser(name, cssClass, title, empId, managerId);
            });
          }else{
            console.error("Reportees Not found.");
          }

          user.childs.push(...reporteeList);
          coWorkerList.splice(MID_COUNTER,0,user)
          managerNode.childs.push(...coWorkerList);
        
        this.nodes.push(managerNode);
        console.log("nodes : ", this.nodes);
        setTimeout(()=>{
          let self = document.querySelector('.Self');
          console.log("self element : ", self);
          self.scrollIntoView({behavior: 'smooth', inline: 'center'});
        }, 1000);
      } else {	
        console.error(response.serviceResponse);	
      }	
    });	
  }

  createHierarchyNodes(event){
    let employeeObj = new Employee();
    employeeObj.empId =  event.empId;
    employeeObj.managerId =  event.managerId;
    this.myTeamHierarchyChart(employeeObj);
  }

  toggleLeaveHistoryView(event){
    if(event.target.checked){
      this.isLeaveHistoryOfDepartment = true;
      this.getDepartmentLeaveHistory();
    }else{
      this.isLeaveHistoryOfDepartment = false;
      this.viewTeamLeaveHistory();
    }
  }

  getDepartmentLeaveHistory(){
    this.teamViewLeaveHistoryList = [];
    this.departmentLeaveHistoryList = [];

    if(this.isLeaveHistoryOfDepartment == true && this.fromDate != null && this.toDate != null){
      let leaveObj = new Leave();
      leaveObj.fromDate = this.fromDate;
      leaveObj.toDate = this.toDate;
      leaveObj.deptId = this.currentUser.departmentId;
      this.teamViewService.getDepartmentLeaveHistory(leaveObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.departmentLeaveHistoryList = response.serviceResponse;
          this.teamViewLeaveHistoryList = response.serviceResponse;
          console.log("departmentLeaveHistoryList : ", this.departmentLeaveHistoryList);
        } else {
          console.error(response.serviceResponse);
        }
      });
    }
  }

  onUpdateTimesheetLockCheck(template: TemplateRef<any>,employeeObj:Employee,status: any){
    let employee = Object.assign({}, employeeObj);
    employee.isTimesheetLockCheckEnable = status;
    employee.updatedBy = this.currentUser.empId;

    if(employee.employeementId.startsWith('A-')){
      employee.employeementId  = employee.employeementId.substring(2);
    }else {
      employee.employeementId  = employee.employeementId
    }

    console.log("updateTimesheetLockCheck : ", employee);
    this.employeeService.updateTimesheetLockCheck(employee).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        let employeeObj = new Employee();
        employeeObj.empId = this.currentUser.empId;
        employeeObj.managerId = this.currentUser.managerId;
        this.myTeamHierarchy(employeeObj);
      } else {
        this.openAlertMod(template, response.serviceResponse);
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
    this.isSelectAll = false
    this.bulkTeamLeaveApprove = []
    this.bulkTeamLeaveReject = []
    this.leaveApplicationList.forEach(x=>{
      x.isSelected = false;
    })
  }
    
  //Sorting team view table 	
  sortData(sort: Sort){	
    console.log(sort);
    if(sort.active){
      let sortParams:any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;      
    }
  }

  
  selectAll(event){
    this.bulkTeamLeaveApprove = [];
    this.bulkTeamLeaveReject = [];
    
    const checkboxes = document.querySelectorAll('.leaveApplication-req-checkbox');
    checkboxes.forEach((checkbox:any) =>{
     
      let checkboxIndex = checkbox.getAttribute('id');
      let checkedLeave = this.leaveApplicationList.find((_leave, index) => index == checkboxIndex);

      if (event.target.checked) {
        checkbox.checked = true;
        this.bulkTeamLeaveApprove.push(checkedLeave);
        this.bulkTeamLeaveReject.push(checkedLeave);
      } else {
        checkbox.checked = false;
        this.bulkTeamLeaveApprove.forEach((leave, index) => {
          if (leave == checkedLeave) this.bulkTeamLeaveApprove.splice(index, 1);
        });
        this.bulkTeamLeaveReject.forEach((leave, index) => {
          if (leave == checkedLeave) this.bulkTeamLeaveReject.splice(index, 1);
        });
      }
    });
  }

   select(leaveObj, event) {
    console.log("clicked on : ", leaveObj);
      if(event.target.checked){
        event.target.classList.add('checked');
        this.bulkTeamLeaveApprove.push(leaveObj)
        this.bulkTeamLeaveReject.push(leaveObj)
      }else {
        event.target.classList.remove('checked');
        const checkboxes = document.querySelectorAll('.leaveApplication-req-checkbox.checked');
        if(checkboxes.length !== this.items) this.isSelectAll = false
        console.log("length ",checkboxes.length);
        console.log("items ",this.items);
        this.bulkTeamLeaveApprove.forEach((leave, index) => {
          if (leave == leaveObj) this.bulkTeamLeaveApprove.splice(index, 1);
        });
        this.bulkTeamLeaveReject.forEach((leave , index)=> {
          if(leave == leaveObj) this.bulkTeamLeaveReject.splice(index , 1);
        })
      }
      console.log("Updated Bulk List : ",  this.bulkTeamLeaveApprove);
      
    }

  onBulkTeamLeaveApproval(template:TemplateRef<any>){
    console.log("Updated Bulk List : ",  this.bulkTeamLeaveApprove);
    let leaveObj = new Leave();
    leaveObj.bulkLeaveApprovedList =  this.bulkTeamLeaveApprove;
    leaveObj.leaveStatusUpdatedBy = this.currentUser.empId;
    leaveObj.approverEmail = this.currentUser.email;	
    leaveObj.leaveStatusId = 2;
   
    this.leaveService.bulkApproveLeaveRequest(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template , "All Selected Leaves Approved Successfully ");
        this.getAllMyTeamsPendingLeaveApplicationsByManagerId()
       
        this.bulkTeamLeaveApprove = [];
        this.bulkTeamLeaveReject = [];
      } else {
      console.error(response.serviceResponse)
      }
    });
    
  }

  // openBulklLeaveReject

  openBulklLeaveReject(template: TemplateRef<any>){
    this.cancelRequest();
   
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }

  OnBulkTeamLeaveReject(template: TemplateRef<any>){
    let leaveObj = new Leave();
    leaveObj.bulkLeaveRejectList =  this.bulkTeamLeaveReject;
    leaveObj.leaveStatusUpdatedBy = this.currentUser.empId;
    leaveObj.leaveStatusId = 3
    leaveObj.rejectReason = this.leaveObj.rejectReason
    leaveObj.approverEmail = this.currentUser.email;	

    console.log(" .. ",leaveObj)
    this.leaveService.bulkRejectLeaveRequest(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template , "All Selected Leaves Approved Successfully ");
        this.getAllMyTeamsPendingLeaveApplicationsByManagerId()
        this.bulkTeamLeaveApprove = [];
        this.bulkTeamLeaveReject = [];
      } else {
      console.error(response.serviceResponse)
      }
    });
  }

  //  Enable Account  

  forEnableAccount(template: TemplateRef<any>, employee: any) {
    console.log("template", template);
    console.log("alertMessage", this.alertMessage);
   if(confirm("Are you sure you want to Enable Account?")){
    //this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.employeeObj = employee;
    this.onRevokeAccount();
   }
    
   
  }

  onRevokeAccount() {
    //this.cancelRequest();
    this.employeeObj.employeementId = this.employeeObj.employeementId.substring(2)
    this.employeeService.revokeAccount(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
     //   this.openAlertMod(template, response.serviceResponse);
      alert(response.serviceResponse);
        this.viewTeam();
      } else {
      //  this.openAlertMod(template, response.serviceResponse);
      console.log("error")
      }
    });
  }
}

function compare(a: number | string, b: number | string, isAsc: boolean) {	
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);	
}
