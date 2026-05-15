import { LocationStrategy } from '@angular/common';
import { Component, OnInit, TemplateRef, ViewChild,AfterViewInit ,ElementRef} from '@angular/core';
import { Sort } from '@angular/material/sort';
import { ActivatedRoute, Router } from '@angular/router';
import * as moment from 'moment';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Employee } from 'src/app/models/employee';
import { Feature } from 'src/app/models/feature';
import { HierarchyUser } from 'src/app/models/hierarchyUser';
import { Leave } from 'src/app/models/leave';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { Employee360Service } from 'src/app/services/employee360.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { LeaveService } from 'src/app/services/leave.service';
import { TeamViewService } from 'src/app/services/team-view.service';
import { UtilityService } from 'src/app/services/utility.service';
import { ValidationService } from 'src/app/services/validation.service';
import OrgChart from '@balkangraph/orgchart.js';


@Component({
  standalone: false,
  selector: 'app-my-team',
  templateUrl: './my-team.component.html',
  styleUrls: ['./my-team.component.css']
})
export class MyTeamComponent implements OnInit {

  data:string;
  feature = "My Team";
  currentUser: User;
  userMapping: any = {};
  dateToday: any = new Date();

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;

  // modal
  alertMessage: any
  modalRef:NgbModalRef;
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
  isLeaveRevokeRequest:boolean = false;

  // Obj
  leaveObj: Leave = new Leave();
  leaveObj2: Leave = new Leave();
  employeeObj: Employee = new Employee();
  teamViewList:  Employee[] = [];
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

  revokeLeaveHistoryInfo:any;

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
  chart: any;

  isLeaveHistoryOfDepartment:boolean = false;
  departmentLeaveHistoryList:any[] = [];

  selectedDataIndex:any=0;
  showReporteeLeaveBalance:boolean = false;
  leaveBalanceList:any[] = [];

  reporteeLeaveRevokeApplicationList:any[] = [];

  isActionEnabled:boolean = false;

  filters:any = {};
  isSearchEnabled:boolean = false;
  isSearchLeaveHistoryEnabled : boolean = false;
  teamViewColumns:any[] = ['blank','employmentIdAcToET','name','email','jobRoleName','mobileNo','managerName', 'timesheetStatus','blank'];
  teamLeaveHistoryColumns:any[] = ['blank','createdByName','fromDate','toDate','createdOn','noOfDays','status','leaveStatusUpdatedByName','reason','leaveType',,'currentApprovalLevel','approverName','managerApprovalStatus','level2ApproverName','level2ApprovalStatus','level3ApproverName','level3ApprovalStatus','remark','blank'];
  teamCompOffHistoryColumns:any[] = ['blank','createdByName','fromDate','toDate','createdOn','noOfDays','status','leaveStatusUpdatedByName','reason'];
  teamLeaveAppColumns:any[] = ['blank','blank','employeeName','leaveType','fromDate','toDate','noOfDays','status','createdByName','createdOn','reason','currentApprovalLevel','approverName','managerApprovalStatus','level2ApproverName','level2ApprovalStatus','level3ApproverName','level3ApprovalStatus','blank'];
  teamCompOffAppColumns:any[] = ['blank', 'createdByName','compOffReasons','fromDate','toDate','noOfDays','description','status','blank'];
  leaveRevokeAppColumns:any[] = ['blank','leaveType','fromDate','toDate','noOfDays','status','createdByName','createdOn','revokeReason','blank'];

  // added by anurag
  leaveHistory : any [] = [];
  leaveHistoryObj: Leave = new Leave();
  leaveUser : any;
  isLeavesHistory : boolean = false;
  leaveHistoryColumns : any[] = ['blank','createdByName','leaveType','fromDate','toDate','noOfDays','createdOn','approverName','managerApprovalStatus','reason','status']
  leaveFilters:any = {};

  isPipGenerate : boolean = false;
  pipReasons : any =[];
  isPipFlag : boolean = false;

  isToggle : boolean = false;
  tempValue : any;

  startDate :any;
  endDate : any;

  // today = new Date().toISOString().split('T')[0];
  today : Date;
  maxDate : Date;
  minDate : Date;
  maxDateForExtend : Date;
  minDateForExtend : Date;
  isDateChanged : boolean = false;
  employeeData2: any;
  isManagerFlag: any;
  isApprover: boolean = false;
  allEmployeeList360: any[] = [];
  // employeesFor360: any[] = [];
  tableName: String;

  selectedNode: HierarchyUser | null = null;

  @ViewChild('orgChartContainer', { static: false }) orgChartContainer!: ElementRef;
  nodeLookup:any={}
  selectedNodeId: string | null = null;



  constructor(
    private authenticationService: AuthenticationService,
    private modalService: NgbModal,
    private teamViewService: TeamViewService,
    private leaveService: LeaveService,
    private employeeService: EmployeeService,
    private exportExcelService: ExportExcelService,
    public validationService:ValidationService,
    private locationStrategy: LocationStrategy,
    private router: Router,
    public utilityService: UtilityService,
    private route: ActivatedRoute,
    private employee360Service: Employee360Service,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  // ngOnInit(): void {
  //   // Dynamic Subfeature Flags
  //   let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
  //   featureMap.subFeatures?.forEach(sub => {
  //     this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
  //   });
  //   // this.sectionViewInit();
  //   // console.log("my team feature mapping ",this.feature, this.userMapping);
  //   // console.log("this.utilityService.getEmployee360ViewAccess()",this.utilityService.getEmployee360ViewAccess())
  //   // this.route.queryParams.subscribe(params => {
  //   //   // console.log("Activating View Team Pending Request");
  //   //   // console.log("Query Params received:", params);
  //   //   const status = params['status'];
  //   //   // console.log("Status from queryParams:", status);
  //   //   if (params['action'] === 'view-pending-request') {
  //   //     console.log("route")
  //   //     this.sectionViewInit();
  //   //   }
  //   // });
  //   this.preventBackButton();
  //   this.getAllEmployeeFor360View();
  //   //console.log('userMapping--', this.userMapping);

  //   // this.employee360Service.employeesFor360$.subscribe((employees) => {
  //   //   this.employeesFor360 = employees;
  //   //   console.log("Employee Data fetched by Shared service ",this.employeesFor360);
  //   // });
  // }

  async ngOnInit(): Promise<void> {
    // Dynamic Subfeature Flags
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });

    this.preventBackButton();

    this.sectionViewInit();
  }


ngOnDestroy(): void {
  if (this.chart) {
    this.chart.destroy();
  }
}

  onNameClick(teamView: any): void {
    console.log('Name clicked:', teamView);
  }

  onIconClick(teamView: any): void {
    console.log('Icon clicked:', teamView);
    if (teamView.isHierarchy) {
      this.myTeamHierarchy(teamView);
    }
  }

  preventBackButton(){
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(()=>{
      history.pushState(null, null, location.href);
    })
  }

  sectionViewInit() {
    if(this.userMapping.view_my_team){
      this.viewTeam();}
    else if(this.userMapping.view_team_leave_history){
      this.viewTeamLeaveHistory();
    }
    else if (this.userMapping.view_team_all_requests || this.userMapping.update_pending_req){
      this.viewTeamRequest();
    }else{
      console.log("Something went wrong");
    }
  }

  viewTeam() {
    this.isViewTeam = true;
    this.sortColumn=[];
    this.sortColumnType=[];
    this.sortDirection='';
    this.isTeamLeaveHistory = false;
    this.isLeaveRequest = false;
    this.isCompOffRequest = false;
    this.isLeaveRevokeRequest = false;
    this.isTeamRequest = false;
    this.page=1;
    this.isHierarchyTable = true;
    this.isHierarchyChart = false;
    this.filters = {};
    this.isSearchEnabled = false;

    // this.getAllManagers();
    // this.getAllTeamView();
    this.breadCrumbs = [];
    this.breadCrumbs.push(this.breadCrumbs.push({'empId':this.currentUser.empId,'name': this.currentUser.name.concat(" > ")}));
      this.loadManagersThenTeam();

  }

  async loadManagersThenTeam() {
  await this.getAllManagersAsync();
  this.getAllTeamView(); // runs only after managers are loaded
}



  viewTeamLeaveHistory() {
    if(this.userMapping.employee_360_leave_view && this.employeeData2.status === 'comp-off-applications'){
      this.sortColumn=[];
      this.sortColumnType=[];
      this.sortDirection='';
      this.isTeamLeaveHistory = true;
      this.isLeaveHistory = false;
      this.isCompOffHistory = true;

      this.isLeaveRequest = false;
      this.isCompOffRequest = false;
      this.isLeaveRevokeRequest = false;
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
      this.filters = {};
      this.isSearchEnabled = false;
    }else{
      this.sortColumn=[];
      this.sortColumnType=[];
      this.sortDirection='';
      this.isTeamLeaveHistory = true;
      this.isLeaveHistory = true;
      this.isCompOffHistory = false;

      this.isLeaveRequest = false;
      this.isCompOffRequest = false;
      this.isLeaveRevokeRequest = false;
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
      this.filters = {};
      this.isSearchEnabled = false;
    }

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
    this.filters = {};
    this.isSearchEnabled = false;
    this.isHierarchyForLeaveHistory = false;
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
    this.filters = {};
    this.isSearchEnabled = false;
    this.isHierarchyForLeaveHistory =false;
  }

  viewTeamRequest() {
    console.log("View Team Pending Request triggered");
    console.log("Employee data viewTeamRequest ",this.employeeData2)
    console.log("this.userMapping.employee_360_leave_view ",this.userMapping.employee_360_leave_view)
    if(this.userMapping.employee_360_leave_view){
      this.sortColumn=[];
      this.sortColumnType=[];
      this.sortDirection='';
      this.isTeamRequest = true;
      // this.isLeaveRequest = true;
      // this.isCompOffRequest = false;
      // this.isLeaveRevokeRequest = false;
      console.log("Employee data ",this.employeeData2)
      if (this.employeeData2.status === 'comp-off-requests') {
        this.isCompOffRequest = true;
        this.isLeaveRevokeRequest = false;
        this.isLeaveRequest = false;
        // this.viewTeamCompOffRequest();
        console.log("Pri Comp off ");
      }
      else if(this.employeeData2.status === 'Pending' || this.employeeData2.status === 'Revoked' || this.employeeData2.status ==='Approved' || this.employeeData2.status ==='Rejected'){
        this.isCompOffRequest = false;
        this.isLeaveRevokeRequest = false;
        this.isLeaveRequest = true;
        // this.viewTeamLeaveRequest();
        console.log("Pri Leave Request ");
      }else if(this.employeeData2.status === 'Applied For Revoke'){
        this.isCompOffRequest = false;
        this.isLeaveRevokeRequest = true;
        this.isLeaveRequest = false;
        // this.viewTeamLeaveRevokeRequest();
        console.log("Pri Leave Revoke Request ");
      }else if(this.employeeData2.status === 'TeamLeave'){
        this.isCompOffRequest = false;
        this.isLeaveRevokeRequest = false;
        this.isLeaveRequest = true;
        // this.viewTeamLeaveRequest();
      console.log("View Team Request my wala")
      }
      this.isLeaveHistoryOfDepartment = false;

      this.isLeaveHistory = false;
      this.isCompOffHistory = false;
      this.isTeamLeaveHistory = false;
      this.isViewTeam = false;
      this.page=1;
      this.data='';
      this.isHierarchyChart = false;
      this.isHierarchyTable = false;
      this.filters = {};
      this.isSearchEnabled = false;
    } else {
      this.sortColumn=[];
      this.sortColumnType=[];
      this.sortDirection='';
      this.isTeamRequest = true;
      this.isLeaveRequest = true;
      this.isCompOffRequest = false;
      this.isLeaveRevokeRequest = false;
      this.isLeaveHistoryOfDepartment = false;
      console.log("else triggered viewTeamRequest ")
      this.isLeaveHistory = false;
      this.isCompOffHistory = false;
      this.isTeamLeaveHistory = false;
      this.isViewTeam = false;
      this.page=1;
      this.data='';
      this.isHierarchyChart = false;
      this.isHierarchyTable = false;
      this.filters = {};
      this.isSearchEnabled = false;
    }

    this.getAllMyTeamsPendingLeaveApplicationsByManagerId();
    // console.log(" this.employeeData2.empId; ", this.employeeData2.empId);
    this.getPendingCompOffRequestsByManagerId();
    this.getAllMyTeamsPendingLeaveRevokeApplicationsByManagerId();

  }


  viewTeamLeaveRequest() {
    this.sortColumn=[];
    this.sortColumnType=[];
    this.sortDirection='';
    this.isLeaveRequest = true;
    this.isCompOffRequest = false;
    this.isLeaveRevokeRequest = false;
    this.page=1;
    this.data='';
    this.filters = {};
    this.isSearchEnabled = false;
    this.isHierarchyForPendingRequest = false;
  }

  viewTeamCompOffRequest() {
    console.log("This is called")
    this.sortColumn=[];
    this.sortColumnType=[];
    this.sortDirection='';
    this.isLeaveRequest = false;
    this.isCompOffRequest = true;
    this.isLeaveRevokeRequest = false;
    this.page=1;
    this.data='';
    this.filters = {};
    this.isSearchEnabled = false;
    this.isHierarchyForPendingRequest = false;
  }

  viewTeamLeaveRevokeRequest() {
    this.isHierarchyForPendingRequest = false;
    this.sortColumn=[];
    this.sortColumnType=[];
    this.sortDirection='';
    this.isLeaveRequest = false;
    this.isCompOffRequest = false;
    this.isLeaveRevokeRequest = true;
    this.page=1;
    this.data='';
    this.filters = {};
    this.isSearchEnabled = false;
    console.log("viewTeamLeaveRevokeRequest");
  }

  getAllManagers() {
    this.employeeService.getAllManagers().pipe(first()).subscribe((response : any)=>{
      if (response.serviceStatus == "Success") {
        this.managerList = response.serviceResponse;
        //console.log("managerList : ", this.managerList);
      }
      else {
        console.error(response.serviceResponse);
      }

    });
  }

  getAllTeamView1() {
    this.teamViewList = []

    //  let managerList = [{managerId:10},{managerId:11},{managerId:16},{managerId:12}];

    let employeeObj = new Employee();
    employeeObj.empId = this.currentUser.empId;
    // employeeObj.isHierarchy = this.isHierarchy;
    this.teamViewService.getAllTeamView(employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.teamViewList = response.serviceResponse;

        // const empData = sessionStorage.getItem('AllEmployees');
        //   if (empData) {
          //       this.employeeList = JSON.parse(empData);
          //   } else {
        //       this.employeeList = []; // Handle case where no data is found
        //   }

        for(let y of this.teamViewList){
          y.employmentIdAcToET = (y.employmentIdAcToET);
          y.isHierarchy = false;
          let temp = this.managerList?.find(manager => manager.managerId == y.empId);
          if(temp != undefined) y.isHierarchy = true;
          //console.log('matches++',matchingEmployee);
          y.emp360 = y.empId;
           y.emp360Mng = this.currentUser.empId;
        }
        this.teamViewList = [...this.teamViewList];
        console.log("teamViewList : ", this.teamViewList);

      } else {
        console.error(response.serviceResponse);
      }

    });

  }

  // Wrap getAllManagers in a promise
  getAllManagersAsync(): Promise<void> {
    return new Promise((resolve, reject) => {
      this.employeeService.getAllManagers().pipe(first()).subscribe({
        next: (response: any) => {
          if (response.serviceStatus === "Success") {
            this.managerList = response.serviceResponse;
            resolve();
          } else {
            console.error(response.serviceResponse);
            reject();
          }
        },
        error: (err) => reject(err)
      });
    });
  }

  getAllTeamView() {
  this.teamViewList = [];

  const employeeObj = new Employee();
  employeeObj.empId = this.currentUser.empId;
  employeeObj.isHierarchy = this.isHierarchy;

  this.teamViewService.getAllTeamView(employeeObj).pipe(first()).subscribe((response: any) => {
    if (response.serviceStatus === "Success") {
      this.teamViewList = response.serviceResponse.map((y: any) => {
        let temp = this.managerList?.find(manager => manager.managerId === y.empId);
          console.log("Checking employee", y.empId, "found in managerList?", temp);

        return {
          ...y,
          employmentIdAcToET: y.employmentIdAcToET,
          isHierarchy: temp !== undefined,
          emp360: y.empId,
          emp360Mng: this.currentUser.empId
        };
      });

      console.log("teamViewList : ", this.teamViewList);

    } else {
      console.error(response.serviceResponse);
    }
  });
}


  unlockAllTimesheet(template: TemplateRef<any>){
    this.cancelRequest();
    let employeeObj = new Employee();

    employeeObj.unlockTimesheetFor = "MyTeam";
    employeeObj.updatedBy = this.currentUser.empId;
    employeeObj.managerId = this.currentUser.empId;
    employeeObj.isTimesheetLockCheckEnable = "false";

    this.employeeService.unlockAllTimesheet(employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.viewTeam();
      } else {
        this.openAlertMod(template, response.serviceResponse);
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
      leaveObj.isHierarchyView = this.isHierarchyForLeaveHistory;
      this.teamViewService.getAllTeamLeaveHistoryView(leaveObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          //console.log(response.serviceResponse);
          this.teamViewLeaveHistoryList = response.serviceResponse;
          this.teamViewLeaveHistoryList.forEach(leaveHistory => {

            let revokeExpireDate = moment(leaveHistory.fromDate, "YYYY-MM-DD").add(this.currentUser.revokeReporteeLeaveValidity, 'days')?.format("YYYY-MM-DD");
            let dateToday = moment(this.dateToday).format("YYYY-MM-DD");
            if (revokeExpireDate > dateToday) {
              leaveHistory.isExpire = "true";
            }
            leaveHistory.employeementId = 'A-'.concat(leaveHistory.employeementId);
            leaveHistory.fromDate = (leaveHistory.fromDate)? moment(leaveHistory.fromDate).format(AppComponent.DATE_FORMAT) : null,
            leaveHistory.toDate = (leaveHistory.toDate)? moment(leaveHistory.toDate).format(AppComponent.DATE_FORMAT) : null,
            leaveHistory.createdOn = (leaveHistory.createdOn)? moment(leaveHistory.createdOn).format(AppComponent.DATE_FORMAT) : null
            // let matchingEmployee = this.allEmployeeList360.find(emp => emp.employeementId === leaveHistory.employeementId);
            // console.log('matches++))',matchingEmployee);
            // leaveHistory.emp360 = matchingEmployee ? matchingEmployee : {};
          });
          this.teamViewLeaveHistoryList.forEach(leaveHistory => {
          //console.log('matches++))',matchingEmployee);
            leaveHistory.emp360 =leaveHistory.empId;
            leaveHistory.emp360AppLev1 = this.currentUser.empId;
            leaveHistory.emp360AppLev2 = leaveHistory.level2ApproverId;
            leaveHistory.emp360AppLev3 = leaveHistory.level3ApproverId;
            leaveHistory.emp360leaveStatusUpdatedBy = leaveHistory.leaveStatusUpdatedBy;

            //leaveStatusUpdatedBy
          });

          //console.log("teamViewLeaveHistory : ", this.teamViewLeaveHistoryList);
        } else {
          console.error(response.serviceResponse);
        }
      });
    }
  }

  getAllTeamCompOffHistoryView(template?: TemplateRef<any>) {
    console.log("THis is called");
    this.teamViewCompOffHistoryList = []

    //console.log("alertTemplate : ", this.alertTemplate);

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

    if (this.userMapping.employee_360_leave_view){
      leaveObj.empId = this.employeeData2.empId;
      //console.log("leaveObj: ", leaveObj)
      this.teamViewService.getAllTeamCompOffHistoryViewByEmpId(leaveObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.teamViewCompOffHistoryList = response.serviceResponse;
          this.teamViewCompOffHistoryList.forEach(compOffHistory => {
            compOffHistory.fromDate = (compOffHistory.fromDate)? moment(compOffHistory.fromDate).format(AppComponent.DATE_FORMAT) : null,
            compOffHistory.toDate = (compOffHistory.toDate)? moment(compOffHistory.toDate).format(AppComponent.DATE_FORMAT) : null,
            compOffHistory.createdOn = (compOffHistory.createdOn)? moment(compOffHistory.createdOn).format(AppComponent.DATE_FORMAT) : null
            compOffHistory.emp360 = compOffHistory.empId;
            compOffHistory.emp360ApprovedBy = compOffHistory.leaveStatusUpdatedBy;

          });
        } else {
          console.error(response.serviceResponse);
        }
      });
    }else{
      leaveObj.empId = this.currentUser.empId;
      leaveObj.isHierarchyView = this.isHierarchyForLeaveHistory;
      //console.log("leaveObj: ", leaveObj)
      this.teamViewService.getAllTeamCompOffHistoryView(leaveObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.teamViewCompOffHistoryList = response.serviceResponse;
          this.teamViewCompOffHistoryList.forEach(compOffHistory => {
            compOffHistory.fromDate = (compOffHistory.fromDate)? moment(compOffHistory.fromDate).format(AppComponent.DATE_FORMAT) : null,
            compOffHistory.toDate = (compOffHistory.toDate)? moment(compOffHistory.toDate).format(AppComponent.DATE_FORMAT) : null,
            compOffHistory.createdOn = (compOffHistory.createdOn)? moment(compOffHistory.createdOn).format(AppComponent.DATE_FORMAT) : null
            compOffHistory.emp360 = compOffHistory.empId;
            compOffHistory.emp360ApprovedBy = compOffHistory.leaveStatusUpdatedBy;
          });
        } else {
          console.error(response.serviceResponse);
        }
      });
    }
        //console.log("teamViewCompOffHistory : ", this.teamViewCompOffHistoryList);

  }


  // getAllMyTeamsPendingLeaveApplicationsByManagerId() {
  //   console.log("Fetching pending leave applications...");
  //   this.leaveApplicationList = []
  //   this.bulkTeamLeaveApprove = []
  //   this.bulkTeamLeaveReject = []
  //   this.isSelectAll = false

  //   let leaveObj = new Leave();
  //   if (this.userMapping.employee_360_leave_view){

  //     console.log("New API calleddd ....");
  //     console.log(this.employeeData2.empId);
  //     leaveObj.empId = this.employeeData2.empId;
  //     console.log("New API calleddd ....");

  //     this.leaveService.getAllLeaveApplicationsByEmpId(leaveObj).pipe(first()).subscribe((response: any) => {
  //       if (response.serviceStatus == "Success") {
  //         this.leaveApplicationList = response.serviceResponse.filter((leaveApp: any) => {
  //           return leaveApp.status === this.employeeData2.status;
  //       });

  //       this.leaveApplicationList.forEach((leaveApp, index) => {
  //           leaveApp.checkId = "leave" + index;
  //           leaveApp.fromDate = leaveApp.fromDate ? moment(leaveApp.fromDate).format(AppComponent.DATE_FORMAT) : null;
  //           leaveApp.toDate = leaveApp.toDate ? moment(leaveApp.toDate).format(AppComponent.DATE_FORMAT) : null;
  //           leaveApp.createdOn = leaveApp.createdOn ? moment(leaveApp.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
  //           if (!leaveApp.currentApprovalLevel && !leaveApp.finalApprovalLevel) {
  //               leaveApp.currentApprovalLevel = 1;
  //               leaveApp.finalApprovalLevel = 1;
  //           }
  //       });
  //         //console.log("leaveApplicationList : ", this.leaveApplicationList);
  //       } else if(!this.userMapping.employee_360_leave_view){
  //         leaveObj.managerId = this.currentUser.empId;
  //         this.leaveService.getAllMyTeamsPendingLeaveApplicationsByManagerId(leaveObj).pipe(first()).subscribe((response: any) => {
  //         if (response.serviceStatus == "Success") {
  //           this.leaveApplicationList = response.serviceResponse;
  //           this.leaveApplicationList.forEach((leaveApp, index) => {
  //             leaveApp.checkId = "leave"+index;
  //             leaveApp.fromDate = (leaveApp.fromDate)? moment(leaveApp.fromDate).format(AppComponent.DATE_FORMAT) : null;
  //             leaveApp.toDate = (leaveApp.toDate)? moment(leaveApp.toDate).format(AppComponent.DATE_FORMAT) : null;
  //             leaveApp.createdOn = (leaveApp.createdOn)? moment(leaveApp.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
  //             if(!leaveApp.currentApprovalLevel && !leaveApp.finalApprovalLevel){
  //               leaveApp.currentApprovalLevel = 1;
  //               leaveApp.finalApprovalLevel = 1;
  //             }
  //           });
  //           //console.log("leaveApplicationList : ", this.leaveApplicationList);
  //         } else {
  //         console.error(response.serviceResponse);
  //       }
  //     });
  //         console.error(response.serviceResponse);
  //       }
  //     });
  //   }
  // }

  getAllMyTeamsPendingLeaveApplicationsByManagerId() {
    console.log("Fetching pending leave applications...");
    this.leaveApplicationList = [];
    this.bulkTeamLeaveApprove = [];
    this.bulkTeamLeaveReject = [];
    this.isSelectAll = false;

    let leaveObj = new Leave();

    const processLeaveApplications = (leaveApplications: any[]) => {
        return leaveApplications.map((leaveApp, index) => ({
            ...leaveApp,
            checkId: `leave${index}`,
            fromDate: leaveApp.fromDate ? moment(leaveApp.fromDate).format(AppComponent.DATE_FORMAT) : null,
            toDate: leaveApp.toDate ? moment(leaveApp.toDate).format(AppComponent.DATE_FORMAT) : null,
            createdOn: leaveApp.createdOn ? moment(leaveApp.createdOn).format(AppComponent.DATETIME_FORMAT) : null,
            currentApprovalLevel: leaveApp.currentApprovalLevel || 1,
            finalApprovalLevel: leaveApp.finalApprovalLevel || 1,
        }));
    };

    if (this.userMapping.employee_360_leave_view) {
        if(this.employeeData2.status === 'Pending' || this.employeeData2.status === 'Revoked' || this.employeeData2.status ==='Approved' || this.employeeData2.status ==='Rejected'){
          console.log("Using employee_360_leave_view API...");
          leaveObj.empId = this.employeeData2.empId;

          this.leaveService.getAllLeaveApplicationsByEmpId(leaveObj).pipe(first()).subscribe(
              (response: any) => {
                  if (response.serviceStatus === "Success") {
                      this.leaveApplicationList = processLeaveApplications(
                          response.serviceResponse.filter((leaveApp: any) => leaveApp.status === this.employeeData2.status)
                      );
                      this.leaveApplicationList.forEach((leaveApplication) => {
                        this.leaveObj2.leaveId = leaveApplication.leaveId;
                        this.leaveObj2.currentUserEmpId = this.currentUser.empId
                        leaveApplication.isApprover = ((leaveApplication.managerId === this.currentUser.empId && leaveApplication.managerApprovalStatus === 'Pending') || (leaveApplication.level2ApproverId === this.currentUser.empId && leaveApplication.level2ApprovalStatus === 'Pending') || (leaveApplication.level3ApproverId === this.currentUser.empId && leaveApplication.level3ApprovalStatus === 'Pending'))? true : false;
                       leaveApplication.emp360 = leaveApplication.empId;
                       leaveApplication.emp360AppLev1 = leaveApplication.level1ApproverId;
                        leaveApplication.emp360AppLev2 = leaveApplication.level2ApproverId;
                        leaveApplication.emp360AppLev3 = leaveApplication.level3ApproverId;


                        this.leaveService.isManager(this.leaveObj2).subscribe((response: any) => {
                          if (response.serviceStatus === "Success") {
                            leaveApplication.isManagerFlag = response.serviceResponse;
                          } else {
                            console.error("Error in isManager API:", response.serviceResponse);
                            leaveApplication.isManagerFlag = false;
                          }
                        });
                      });
                      console.log('chk dta - ',this.leaveApplicationList);

                  } else {
                      console.error("Error fetching leave applications:", response.serviceResponse);
                  }
              },
              (error) => console.error("API error:", error)
          );
        }else if(this.employeeData2.status === 'TeamLeave'){
          console.log("Using employee_360_leave_view API for TeamLeave...");
          leaveObj.empId = this.employeeData2.empId;

          this.leaveService.getAllLeaveApplicationsByTeamId(leaveObj).pipe(first()).subscribe(
              (response: any) => {
                  if (response.serviceStatus === "Success") {
                      this.leaveApplicationList = processLeaveApplications(response.serviceResponse);
                      this.leaveApplicationList.forEach((leaveApplication) => {
                        this.leaveObj2.leaveId = leaveApplication.leaveId;
                        this.leaveObj2.currentUserEmpId = this.currentUser.empId
                        let matchingEmployee = this.allEmployeeList360.find(emp => emp.employeementId === ('A-' + leaveApplication.employeementId));
                        //console.log('matches++',matchingEmployee);
                        leaveApplication.emp360 = leaveApplication.empId;
                        leaveApplication.emp360AppLev1 = leaveApplication.level1ApproverId;
                        leaveApplication.emp360AppLev2 = leaveApplication.level2ApproverId;
                        leaveApplication.emp360AppLev3 = leaveApplication.level3ApproverId;

                        leaveApplication.isApprover = ((leaveApplication.managerId === this.currentUser.empId && leaveApplication.managerApprovalStatus === 'Pending') || (leaveApplication.level2ApproverId === this.currentUser.empId && leaveApplication.level2ApprovalStatus === 'Pending') || (leaveApplication.level3ApproverId === this.currentUser.empId && leaveApplication.level3ApprovalStatus === 'Pending'))? true : false;
                        //console.log("Leave id : ",leaveApplication.leaveId," isApprover: ",leaveApplication.isApprover);
                        this.leaveService.isManager(this.leaveObj2).subscribe((response: any) => {
                          if (response.serviceStatus === "Success") {
                            leaveApplication.isManagerFlag = response.serviceResponse;
                          } else {
                            console.error("Error in isManager API:", response.serviceResponse);
                            leaveApplication.isManagerFlag = false;
                          }
                        });
                      });
                      console.log('chk dta - ',this.leaveApplicationList);

                  } else {
                      console.error("Error fetching leave applications:", response.serviceResponse);
                  }
              },
              (error) => console.error("API error:", error)
          );
        }
    }
     else {
        console.log("Using manager-specific API...");
        leaveObj.managerId = this.currentUser.empId;
        leaveObj.isHierarchyView = this.isHierarchyForPendingRequest;


        this.leaveService.getAllMyTeamsPendingLeaveApplicationsByManagerId(leaveObj).pipe(first()).subscribe(
            (response: any) => {
                if (response.serviceStatus === "Success") {
                    this.leaveApplicationList = processLeaveApplications(response.serviceResponse);
                    console.log("Leave applicationList",this.leaveApplicationList);
                    this.leaveApplicationList.forEach((leaveApplication) => {
                      this.leaveObj2.leaveId = leaveApplication.leaveId;
                      this.leaveObj2.currentUserEmpId = this.currentUser.empId
                      this.leaveService.isManager(this.leaveObj2).subscribe((response: any) => {
                        if (response.serviceStatus === "Success") {
                          leaveApplication.isManagerFlag = response.serviceResponse;
                          this.leaveApplicationList.forEach((leaveApplication) => {
                            // this.leaveObj2.leaveId = leaveApplication.leaveId;
                            // this.leaveObj2.currentUserEmpId = this.currentUser.empId
                            let matchingEmployee = this.allEmployeeList360.find(emp => emp.employeementId === ('A-' + leaveApplication.employeementId));
                            //console.log('matches++',matchingEmployee);
                            leaveApplication.emp360 = matchingEmployee ? matchingEmployee : {};
                            leaveApplication.emp360CreateBy = leaveApplication.empId;
                             leaveApplication.emp360AppLev1 = leaveApplication.level1ApproverId;
                            leaveApplication.emp360AppLev2 = leaveApplication.level2ApproverId;
                            leaveApplication.emp360AppLev3 = leaveApplication.level3ApproverId;

                            leaveApplication.isApprover = ((leaveApplication.managerId === this.currentUser.empId && leaveApplication.managerApprovalStatus === 'Pending') || (leaveApplication.level2ApproverId === this.currentUser.empId && leaveApplication.level2ApprovalStatus === 'Pending') || (leaveApplication.level3ApproverId === this.currentUser.empId && leaveApplication.level3ApprovalStatus === 'Pending'))? true : false;
                            console.log("Leave id : ",leaveApplication.leaveId," isApprover: ",leaveApplication.isApprover,"PRI",leaveApplication.managerId," ",this.currentUser.empId," ",leaveApplication.managerApprovalStatus);

                          });
                        } else {
                          console.error("Error in isManager API:", response.serviceResponse);
                          leaveApplication.isManagerFlag = false;
                        }
                      });
                    });
                    console.log("leave obj pri ",this.leaveApplicationList)
                } else {
                    console.error("Error fetching team leave applications:", response.serviceResponse);
                }
            },
            (error) => console.error("API error:", error)
        );
    }
}

  onUpdateLeaveStatus(template: TemplateRef<any>, leaveApplication, updatedLeaveStatusId) {
    // 1 = pending , 2 = Approved , 3= Rejected
    leaveApplication.leaveStatusId = updatedLeaveStatusId;
    leaveApplication.leaveStatusUpdatedBy = this.currentUser.empId;
    leaveApplication.rejectReason = leaveApplication.rejectReason?.trim()

    //console.log("leaveApplication : ", leaveApplication);

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
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-lg' });
  }


  //comOff Applications
  getPendingCompOffRequestsByManagerId() {
    if (this.userMapping.employee_360_leave_view){
      // console.log("Fetching pending comp-off requests...");
      this.allCompOffApplications = []

      let compOff = new Leave();
      compOff.empId = this.employeeData2.empId;
      this.leaveService.getPendingCompOffRequestsByEmpId(compOff).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {

          this.allCompOffApplications = response.serviceResponse;
          this.allCompOffApplications.forEach(compOffApp => {
            compOffApp.fromDate = (compOffApp.fromDate)? moment(compOffApp.fromDate).format(AppComponent.DATE_FORMAT) : null,
            compOffApp.toDate = (compOffApp.toDate)? moment(compOffApp.toDate).format(AppComponent.DATE_FORMAT) : null
            compOffApp.emp360 = compOffApp.empId;
          });
          //console.log("allCompOffApplications : ", this.allCompOffApplications);
        } else {
          console.error(response.serviceResponse);
        }
      });
    }else if(!this.userMapping.employee_360_leave_view){
      // console.log("Fetching pending comp-off requests...");
      this.allCompOffApplications = []

      let compOff = new Leave();
      compOff.managerId = this.currentUser.empId;
      compOff.isHierarchyView = this.isHierarchyForPendingRequest;
      this.leaveService.getPendingCompOffRequestsByManagerId(compOff).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {

          this.allCompOffApplications = response.serviceResponse;
          this.allCompOffApplications.forEach(compOffApp => {
            compOffApp.fromDate = (compOffApp.fromDate)? moment(compOffApp.fromDate).format(AppComponent.DATE_FORMAT) : null,
            compOffApp.toDate = (compOffApp.toDate)? moment(compOffApp.toDate).format(AppComponent.DATE_FORMAT) : null
            compOffApp.emp360 = compOffApp.empId;
          });
          //console.log("allCompOffApplications : ", this.allCompOffApplications);
        } else {
          console.error(response.serviceResponse);
        }
      });
    } else {
      console.error("Something went wrong");
    }
  }

  onUpdateCompOffStatus(template: TemplateRef<any>, compOffObj, updatedCompOffStatusId) {
    // 1 = pending , 2 = Approved , 3= Rejected

    //console.log("template: ", this.alertTemplate );

    compOffObj.leaveStatusId = updatedCompOffStatusId;
    compOffObj.leaveStatusUpdatedBy = this.currentUser.empId;
    compOffObj.hodEmail = this.currentUser.email;
    compOffObj.hodName = this.currentUser.name;
    compOffObj.employeeName = compOffObj.createdByName;
    compOffObj.rejectCompOffReason = compOffObj.rejectReason;
    //console.log("Update Comp off : ", compOffObj);
    this.leaveService.updateCompOffById(compOffObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.getPendingCompOffRequestsByManagerId();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  // Leave Revoke
  getAllMyTeamsPendingLeaveRevokeApplicationsByManagerId(){
    console.log("Fetching pending leave revoke applications...");
    this.reporteeLeaveRevokeApplicationList = [];

    if(this.userMapping.employee_360_leave_view){
      this.leaveObj.empId = this.employeeData2.empId;
      this.leaveService.getAllMyTeamsPendingLeaveRevokeApplicationsByEmpId(this.leaveObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.reporteeLeaveRevokeApplicationList = response.serviceResponse;
          this.reporteeLeaveRevokeApplicationList.forEach(leave => {
            leave.fromDate = (leave.fromDate)? moment(leave.fromDate).format(AppComponent.DATE_FORMAT) : null;
            leave.toDate = (leave.toDate)? moment(leave.toDate).format(AppComponent.DATE_FORMAT) : null;
            leave.createdOn = (leave.createdOn)? moment(leave.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
            leave.emp360 = leave.empId;
          });
          //console.log(this.reporteeLeaveRevokeApplicationList, " : reporteeLeaveRevokeApplicationList");
        } else {
          console.error(response.serviceResponse);
        }
      });
    }else{
      this.leaveObj.empId = this.currentUser.empId;
      this.leaveObj.isHierarchyView = this.isHierarchyForPendingRequest;
      this.leaveService.getAllMyTeamsPendingLeaveRevokeApplicationsByManagerId(this.leaveObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.reporteeLeaveRevokeApplicationList = response.serviceResponse;
          this.reporteeLeaveRevokeApplicationList.forEach(leave => {
            leave.fromDate = (leave.fromDate)? moment(leave.fromDate).format(AppComponent.DATE_FORMAT) : null;
            leave.toDate = (leave.toDate)? moment(leave.toDate).format(AppComponent.DATE_FORMAT) : null;
            leave.createdOn = (leave.createdOn)? moment(leave.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
           leave.emp360 = leave.empId;
          });
          //console.log(this.reporteeLeaveRevokeApplicationList, " : reporteeLeaveRevokeApplicationList");
        } else {
          console.error(response.serviceResponse);
        }
      });
    }
  }

  onUpdateRevokeLeaveStatus(template: TemplateRef<any>, leave, updatedLeaveStatusId){
    this.cancelRequest();

    // if(this.userMapping.employee_360_leave_view){
    //   leave.leaveRevokeStatusUpdatedBy = this.currentUser.empId;
    //   leave.leaveRevokeStatusId = updatedLeaveStatusId;
    //   leave.approverEmail = this.currentUser.email;
    //   //console.log(leave, " : RevokeLeaveObj");

    //   this.leaveService.updateRevokeLeaveStatus(leave).pipe(first()).subscribe((response: any) => {
    //     if (response.serviceStatus == "Success") {
    //       this.openAlertMod(template, response.serviceResponse);
    //     } else {
    //       this.openAlertMod(template, response.serviceResponse);
    //     }
    //     this.getAllMyTeamsPendingLeaveRevokeApplicationsByManagerId();
    //   });
    // }else{
      leave.leaveRevokeStatusUpdatedBy = this.currentUser.empId;
      leave.leaveRevokeStatusId = updatedLeaveStatusId;
      leave.approverEmail = this.currentUser.email;
      //console.log(leave, " : RevokeLeaveObj");

      this.leaveService.updateRevokeLeaveStatus(leave).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.openAlertMod(template, response.serviceResponse);
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }
        this.getAllMyTeamsPendingLeaveRevokeApplicationsByManagerId();
      });
      // }
  }

  navigateToAssetConsent(teamView:any){

    if (teamView.employeementId.startsWith('A-')) {
      let employmentId = teamView.employeementId?.substring(2);
      this.router.navigate(['/user-exit/my-resignation', employmentId]);
    }else if(teamView.employeementId.startsWith('AP-')){
      let employmentId = teamView.employeementId?.substring(3);
      this.router.navigate(['/user-exit/my-resignation', employmentId]);
    }
     else {
      let employmentId = teamView.employeementId;
     this.router.navigate(['/user-exit/my-resignation', employmentId]);
    }
  }


  // // download excel
  // exportToExcel(id:any): void {
  //   const tableId = id;
  //   this.tableName= 'My Team';
  //     if (this.isViewTeam == true) {
  //     this.excelName = 'MyTeam.xlsx';
  //     this.exportExcelService.exportTableFormat(tableId,this.excelName,this.tableName);
  //   }

  //     if (this.isLeaveHistory == true) {
  //     this.excelName = 'MyTeamLeaveHistory.xlsx';

  //     this.exportExcelService.exportTableFormat(tableId,this.excelName,this.tableName);


  //   }

  //     if (this.isCompOffHistory == true) {
  //     this.excelName = 'MyTeamCompOffHistory.xlsx';

  //     this.exportExcelService.exportTableFormat(tableId,this.excelName,this.tableName);


  //   }

  //   if (this.isLeaveRequest == true) {
  //       this.excelName = 'MyTeamLeaveRequests.xlsx';

  //       this.exportExcelService.exportTableFormat(tableId,this.excelName,this.tableName);

  //     }

  //     if (this.isCompOffRequest == true) {
  //         this.excelName = 'MyTeamCompOffRequests.xlsx';

  //         this.exportExcelService.exportTableFormat(tableId,this.excelName,this.tableName);

  //       }

  //       if(this.isLeaveRevokeRequest == true){
  //         this.excelName = 'ReporteeLeaveApplication.xlsx';

  //         this.exportExcelService.exportTableFormat(tableId,this.excelName,this.tableName);

  //       }
  // }
  exportToExcel(): void {

    if (this.isViewTeam == true) {
    this.excelName = 'MyTeam.xlsx';

    const onlySpecificDataArr = this.teamViewList.map(
      x => ({
        "Employee Id": x.employeementId,
        "Name": x.name,
        "Email": x.email,
        "Job Role": x.jobRoleName,
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

      if(this.isLeaveRevokeRequest == true){
        this.excelName = 'ReporteeLeaveApplication.xlsx';

        const onlySpecificDataArr: Partial<Leave>[] = this.reporteeLeaveRevokeApplicationList.map(
          x => ({
            "leave Type": x.leaveType,
            "From Date": (x.fromDate)? x.fromDate : null,
            "To Date": (x.toDate)? x.toDate : null,
            "No Of Days": x.noOfDays,
            "status": x.status,
            "Created By Name": x.createdByName,
            "Created On": (x.createdOn)? moment(x.createdOn).format(AppComponent.DATETIME_FORMAT) : null,
            "Reason": x.reason
          })
        )
        this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr,this.excelName)
      }
}

  hierarchyBreadCrumb(index){
    this.breadCrumbs.splice(index + 1);
    this.employeeObj.name = this.breadCrumbs[this.breadCrumbs.length - 1];
    this.myTeamHierarchy(this.employeeObj.name);
  }

  //myTeam-hierarchy
  myTeamHierarchy(employeeObj:Employee) {
    this.leaveBalanceList = [];
    this.showReporteeLeaveBalance = false;
    this.selectedDataIndex = 0;

    let employee = Object.assign({}, employeeObj);
   if (employee.employeementId &&
    typeof employee.employeementId === 'string' &&
    employee.employeementId.startsWith("A-") ) {
    employee.employeementId = employee.employeementId.substring(2);
}
else if(employee.employeementId &&
    typeof employee.employeementId === 'string' && employee.employeementId.startsWith('AP-')){
    employee.employeementId = employee.employeementId.substring(3);
}

    this.employeeService.getHierarchyByEmpId(employee).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.teamViewList = response.serviceResponse;
        for(let teamMember of this.teamViewList){
          teamMember.employeementId = "A-".concat(teamMember.employeementId);
          teamMember.emp360 = teamMember.empId;
    teamMember.emp360Mng = teamMember.managerId;
    // teamMember.isHierarchy = false;
        }

        for(let x of this.teamViewList){
          x.isHierarchy = false;
         let temp = this.managerList.find(manager => manager.managerId == x.empId);

          if(temp != undefined) x.isHierarchy = true;
       }

        if(!employeeObj.name.includes(">")){
          this.breadCrumbs.push({'empId':employeeObj.empId,'name': employeeObj.name.concat(" > ")});
        }
        //console.log("teamViewList : ", this.teamViewList);
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
    employee.employeementId = employee.employeementId;

    this.employeeService.getHierarchyChartByEmpId(employee).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.teamViewList = response.serviceResponse;
        for(let teamMember of this.teamViewList){
          teamMember.employeementId = "A-".concat(teamMember.employeementId);
        }
        //console.log("teamViewList : ", this.teamViewList);

        let manager = this.teamViewList.find(employee => employee.hierarchyType == "Manager");
        let coworkers = this.teamViewList.filter(employee => employee.hierarchyType == "Co-Worker");
        let self = this.teamViewList.find(employee => employee.hierarchyType == "Self");
        let reportees = this.teamViewList.filter(employee => employee.hierarchyType == "Reportee");

        //console.log("Manager : ", manager);
        //console.log("coworkers : ("+ coworkers.length+")", coworkers);
        //console.log("self : ", self);
        //console.log("reportees :  ("+ reportees.length+")", reportees);



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
            user.id = "self-node";
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
        this.renderBalkanChart();
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  createHierarchyNodes(event){
    let employeeObj = new Employee();
    employeeObj.empId =  event.empId;
    employeeObj.managerId =  event.managerId;
    employeeObj.employeementId =  event.empId;
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
    this.isHierarchyForLeaveHistory =false;
    this.page = 1;
  }

  getDepartmentLeaveHistory(){
    this.teamViewLeaveHistoryList = [];
    this.departmentLeaveHistoryList = [];

    if(this.isLeaveHistoryOfDepartment == true && this.fromDate != null && this.toDate != null){
      let leaveObj = new Leave();
      leaveObj.fromDate = this.fromDate;
      leaveObj.toDate = this.toDate;
      leaveObj.deptId = this.currentUser.departmentId;
      leaveObj.employeeRole=this.currentUser.employeeRole;
      leaveObj.empId = this.currentUser.empId
      this.teamViewService.getDepartmentLeaveHistory(leaveObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.departmentLeaveHistoryList = response.serviceResponse;
          this.teamViewLeaveHistoryList = response.serviceResponse;
          for(let y of this.teamViewLeaveHistoryList){
           y.emp360 = y.empId;
          }
          for(let y of this.departmentLeaveHistoryList){
            let matchingEmployee = this.allEmployeeList360.find(emp => emp.employeementId === ('A-' + y.employeementId));
            console.log('matches++',matchingEmployee);
            y.emp360 = matchingEmployee ? matchingEmployee : {};
            y.emp360AppLev1 =  y.approverId;
            y.emp360AppLev2 = y.level2ApproverId;
            y.emp360AppLev3 = y.level3ApproverId;
            y.emp360leaveStatusUpdatedBy = y.leaveStatusUpdatedBy;
          }
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
    }else if(employee.employeementId.startsWith('AP-'))
    {
      employee.employeementId  = employee.employeementId.substring(3);
    }
    else {
      employee.employeementId  = employee.employeementId
    }

    //console.log("updateTimesheetLockCheck : ", employee);
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

 revokeMyReporteeLeave(template: TemplateRef<any>) {
  this.cancelRequest();

  let leaveHistory = { ...this.revokeLeaveHistoryInfo };
  leaveHistory.leaveStatusUpdatedBy = this.currentUser.empId;
  leaveHistory.revokeReason = this.leaveObj.revokeReason;

  if (leaveHistory.employeementId && typeof leaveHistory.employeementId === 'string') {
    leaveHistory.employeementId = parseInt(leaveHistory.employeementId.replace(/\D/g, ''), 10);
  }

  const fromDate = this.fromDate;
  const toDate = this.toDate;

  this.teamViewService.revokeReporteeLeave(leaveHistory)
    .pipe(first())
    .subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.viewLeaveHistory();
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.getAllTeamLeaveHistoryView();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
}


canShowFilterBar(): boolean {
    return this.isViewTeam && this.isSearchEnabled && this.teamViewColumns && this.teamViewColumns.length > 0;
}

  onGetEmpLeaveBalance(template: TemplateRef<any>, teamMember, recordIndex) {
    this.leaveBalanceList = [];
    this.showReporteeLeaveBalance = false;
    this.selectedDataIndex = 0;

    let leaveObj: Leave = new Leave();
    if(teamMember){
      leaveObj.employeementId = teamMember.employeementId;

      if (leaveObj.employeementId) {
    const empIdStr = String(leaveObj.employeementId);
    if (empIdStr.startsWith('A-') ) {
        leaveObj.employeementId = empIdStr.substring(2);}
    else if(empIdStr.startsWith('AP-'))
    {
      leaveObj.employeementId = empIdStr.substring(3);
    }

}
      this.leaveService.getMyLeaveBalancesByEmpId(leaveObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.leaveBalanceList = response.serviceResponse;
          //console.log("leaveBalanceList : ", this.leaveBalanceList);
          this.showReporteeLeaveBalance = true;
          this.selectedDataIndex = recordIndex;
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }
      });
    }else{
      console.error("Reportee Not Found.");
    }
  }

  onHideEmpLeaveBalance(){
    this.leaveBalanceList = [];
    this.showReporteeLeaveBalance = false;
    this.selectedDataIndex = 0;
  }

  // Modals

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef?.close();
  }

  openRevokeReporteeLeaveModal(template: TemplateRef<any>, leaveHistory: any){
    this.modalRef = this.modalService.open(template);
    this.revokeLeaveHistoryInfo = leaveHistory;
  }

  openRevokeLeaveRejectModal(template: TemplateRef<any>, leave: any){
    this.leaveObj.rejectReason = '';
    this.cancelRequest();
    this.leaveObj = leave;
    this.modalRef = this.modalService.open(template);
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
    //console.log(sort);
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
      let checkedLeave = this.leaveApplicationList.find((_leave, index) => _leave.checkId == checkboxIndex);
      console.log("Leave id : ",checkedLeave.id," isApprover: ",checkedLeave.isApprover);
      if(checkedLeave.isManagerFlag && checkedLeave.isApprover){
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
      }
    });
  }

  select(leaveObj, event) {
  //console.log("clicked on : ", leaveObj);
    if(event.target.checked){
      event.target.classList.add('checked');
      this.bulkTeamLeaveApprove.push(leaveObj)
      this.bulkTeamLeaveReject.push(leaveObj)
    }else {
      event.target.classList.remove('checked');
      const checkboxes = document.querySelectorAll('.leaveApplication-req-checkbox.checked');
      if(checkboxes.length !== this.items) this.isSelectAll = false
      //console.log("length ",checkboxes.length);
      //console.log("items ",this.items);
      this.bulkTeamLeaveApprove.forEach((leave, index) => {
        if (leave == leaveObj) this.bulkTeamLeaveApprove.splice(index, 1);
      });
      this.bulkTeamLeaveReject.forEach((leave , index)=> {
        if(leave == leaveObj) this.bulkTeamLeaveReject.splice(index , 1);
      })
    }
    //console.log("Updated Bulk List : ",  this.bulkTeamLeaveApprove);

  }

  onBulkTeamLeaveApproval(template:TemplateRef<any>){
    //console.log("Updated Bulk List : ",  this.bulkTeamLeaveApprove);
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

    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-lg' });
  }

  OnBulkTeamLeaveReject(template: TemplateRef<any>){
    let leaveObj = new Leave();
    leaveObj.bulkLeaveRejectList =  this.bulkTeamLeaveReject;
    leaveObj.leaveStatusUpdatedBy = this.currentUser.empId;
    leaveObj.leaveStatusId = 3
    leaveObj.rejectReason = this.leaveObj.rejectReason
    leaveObj.approverEmail = this.currentUser.email;

    //console.log(" .. ",leaveObj)
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
    //console.log("template", template);
    //console.log("alertMessage", this.alertMessage);
  //  if(confirm("Are you sure you want to Enable Account?")){
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.employeeObj = employee;
    // this.onRevokeAccount(template);
  //  }


  }

  onRevokeAccount(template :TemplateRef<any>) {
    this.cancelRequest();

   if (this.employeeObj.employeementId &&
    typeof this.employeeObj.employeementId === 'string' &&
    this.employeeObj.employeementId.startsWith("A-") ) {
    this.employeeObj.employeementId = this.employeeObj.employeementId.substring(2);
}
else if(this.employeeObj.employeementId &&
    typeof this.employeeObj.employeementId === 'string' && this.employeeObj.employeementId.startsWith('AP-')){
    this.employeeObj.employeementId = this.employeeObj.employeementId.substring(3);
}
    this.employeeService.revokeAccount(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
       this.openAlertMod(template, response.serviceResponse);
      // alert(response.serviceResponse);
        this.viewTeam();
      } else {
       this.openAlertMod(template, response.serviceResponse);
      //console.log("error")
      }
    });
  }


  toggleLeaveHistoryViewOfRequestPage(event){
    this.page = 1;
    if(event.target.checked){
      this.isLeaveHistoryOfDepartment = true;
      this.getDepartmentPendingLeaveHistory();
    }else{
      this.items=10;
      this.isLeaveHistoryOfDepartment = false;
      this.getAllMyTeamsPendingLeaveApplicationsByManagerId();
    }
    this.isHierarchyForPendingRequest=false;
  }

  getDepartmentPendingLeaveHistory(){
    let leaveObj = new Leave();

    leaveObj.deptId = this.currentUser.departmentId;
    leaveObj.employeeRole = this.currentUser.employeeRole
    this.teamViewService.getDepartmentPendingLeaveHistory(leaveObj).pipe(first()).subscribe((response : any)=>{
      if(response.serviceStatus == "Success"){
        if(this.isLeaveRequest){
          this.leaveApplicationList = response.serviceResponse;
          this.leaveApplicationList = this.leaveApplicationList.filter(x=> x.leaveType != 'Compensatory Off' );
        }


      console.log("this.leaveApplicationList   ",this.leaveApplicationList);
      }else{
        console.log(response.serviceResponse);
      }
    });

  }

  toggleSearch(){
    this.isSearchEnabled = !this.isSearchEnabled;
    if(!this.isSearchEnabled){
      this.filters = {};
    }
  }

  isHierarchy : boolean = false;
  isHierarchyForLeaveHistory : boolean = false;
  isHierarchyForPendingRequest : boolean = false;

  toggleIsHierarchy(event){
    this.isHierarchy = event.target.checked;
    this.getAllTeamView();

  }
  toggleIsHierarchyForLeaveHistory(event){
    this.isHierarchyForLeaveHistory = event.target.checked;
    if(this.isLeaveHistory){
      this.getAllTeamLeaveHistoryView();
    }
    if(this.isCompOffHistory){
      this.getAllTeamCompOffHistoryView();
    }
    this.handlePageChange(1);
  }

  toggleIsHierarchyPendingRequest(event){
    console.log("This is called")
    this.isHierarchyForPendingRequest = event.target.checked;
    if(this.isLeaveRequest){
      this.getAllMyTeamsPendingLeaveApplicationsByManagerId();
    }
    if(this.isCompOffRequest){
      this.getPendingCompOffRequestsByManagerId();
    }
    if(this.isLeaveRevokeRequest){
      this.getAllMyTeamsPendingLeaveRevokeApplicationsByManagerId();
    }
    this.handlePageChange(1);

  }

  toggleLeaveHistorySearch(){
    this.isSearchLeaveHistoryEnabled = !this.isSearchLeaveHistoryEnabled;
    if(!this.isSearchLeaveHistoryEnabled){
      this.leaveFilters = {};
    }
  }

  onSearch(searchData){
    this.filters = searchData;
    //console.log("Updated Filter : ", this.filters);
  }

  onLeaveSearch(searchData){
    this.leaveFilters = searchData;
    //console.log("Updated Filter : ", this.leaveFilters);
  }


  // added by anurag

  pageNo = 1;
  handlePageChanges(event) {
    this.pageNo = event;
  }


  getAllLeavesByEmpId(teamObj){
    //console.log("method call",teamObj)

    let team = new Leave();
    team.empId = teamObj.empId;
    this.leaveService.getAllMyLeaveApplicationsByEmpId(team).pipe(first()).subscribe((response : any)=>{
      if(response.serviceStatus == "Success"){
        //console.log("getAllLeavesByEmpId  ",response.serviceResponse);
        this.leaveHistory = response.serviceResponse;
        this.leaveHistory.forEach((data)=>{
          data.fromDate = moment(data.fromDate).format(AppComponent.DATE_FORMAT);
          data.toDate = moment(data.toDate).format(AppComponent.DATE_FORMAT);
          this.leaveHistoryObj.name = data.createdByName;
          this.leaveUser = data.createdByName;
          // let matchingleavecreatedByEmpId = this.allEmployeeList360.find(emp => emp.empId === data.empId);

          // data.emp360leavecreatedByEmpId = matchingleavecreatedByEmpId ? matchingleavecreatedByEmpId : {};
          data.emp360leavecreatedByEmpId = data.empId;
          data.emp360approverempId =  data.approverEmpId;
          // let matchingapproverempId = this.allEmployeeList360.find(emp => emp.empId === data.approverEmpId);
          // data.emp360approverempId = matchingapproverempId ? matchingapproverempId : {};
        })
      }
    })
  }

  openHistoryTable(template:TemplateRef<any>,teamObj){
    this.leaveHistory = [];
    this.leaveUser ='';
    this.isLeavesHistory = true;
  this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
  this.leaveObj = teamObj;
  }

//  added by anuarg

PIP_generate(template:TemplateRef<any>, team){
  this.startDate = '';
  this.endDate = '';
  this.leaveObj.pipReason = ''
this.isPipGenerate = true;
this.modalRef=this.modalService.open(template , { modalDialogClass : 'modal-md'});
this.leaveObj = team;
}


PIP_reverse_modal(template:TemplateRef<any> , team){
  this.isToggle = false;
  this.isPipGenerate = false;
this.modalRef=this.modalService.open(template , { modalDialogClass : 'modal-md'});
this.leaveObj = team;
this.getPipDetailsByEmpId(team);
}

getPipDetailsByEmpId(employee : any){
  // console.log(" emp details  ",employee);
  let leaveObj = new Leave();
  leaveObj.empId = employee.empId;

  this.leaveService.getPipDetailsByEmpId(leaveObj).pipe(first()).subscribe((response : any)=>{
    if(response.serviceStatus == "Success"){

      this.leaveObj = Object.assign({}, response.serviceResponse);

      this.startDate = this.leaveObj.startDate;
      this.endDate = this.leaveObj.endDate;


      this.endDate = (this.endDate)? moment(this.endDate, "DD-MM-YYYY").toDate() : '';
      this.startDate = (this.startDate)? moment(this.startDate, "DD-MM-YYYY").toDate() : '';

      if (this.endDate) {
        this.tempValue = new Date(this.endDate);
        this.minDateForExtend = new Date(this.endDate);
        this.maxDateForExtend = new Date(this.endDate);
        this.maxDateForExtend.setMonth(this.maxDateForExtend.getMonth() + 2);
      }

      console.log(" startDate   ",this.endDate);

    }
  })

}

PIP_reverse(template:TemplateRef<any>,teamObj,flag){
  this.isPipGenerate = false;
  console.log(teamObj);
  let leave = new Leave();
  leave.pipFlag = flag;
  leave.pipId = teamObj.pipId;
  leave.empId = teamObj.empId;
  leave.updatedBy = this.currentUser.empId;
  leave.updatedByName = this.currentUser.name;
  leave.revReason = teamObj.revReason;

  leave.startDate = moment(this.startDate).format(AppComponent.DATE_FORMAT);
  if(this.isToggle){
    leave.endDate = moment(this.endDate).format(AppComponent.DATE_FORMAT);
  }else{
    let endDate : any = new Date();
    endDate = moment(endDate).format(AppComponent.DATE_FORMAT);
      console.log("endDate   ",endDate);
    leave.endDate = endDate;
  }

console.log(" leaves ",leave)

  this.leaveService.pipReturnFromUser(leave).pipe(first()).subscribe((response : any)=>{
    if(response.serviceStatus == "Success"){
      this.openAlertMod(template,response.serviceResponse);
      this.getAllTeamView();
    }else{
      this.openAlertMod(template,response.serviceResponse);
    }
  })
}

  togglePipView(event){
    this.employeeObj.revReason = '';
    this.isDateChanged = false;
    if(event.target.checked){
      this.isToggle = true;
      this.employeeObj.extendReason = '';

    }else{
      this.isToggle = false;
      // this.leaveObj.endDate = this.tempValue;
      this.endDate = this.tempValue;
    }
  }


PipGenerateToUser(template: TemplateRef<any>,leaveObj,flag){
  console.log(" leaveObj   ",leaveObj);

    this.cancelRequest();
    let leave = new Leave();
    leave.empId = leaveObj.empId;
    leave.pipReason = leaveObj.pipReason;
    leave.startDate = moment(this.startDate).format(AppComponent.DATE_FORMAT);
    leave.endDate = moment(this.endDate).format(AppComponent.DATE_FORMAT);
    leave.pipFlag = flag;
    leave.createdBy = this.currentUser.empId;
    leave.createdByName = this.currentUser.name;



    console.log(leave);
    this.leaveService.pipGenerateToUser(leave).pipe(first()).subscribe((response : any)=>{
      if(response.serviceStatus == "Success"){
        this.openAlertMod(template,response.serviceResponse);
        this.getAllTeamView();
      }else{
        this.openAlertMod(template,response.serviceResponse);
      }
    })
  // }

}
 openRevokeLeaveRejectModalCompOff(template: TemplateRef<any>, leave: any){
      this.cancelRequest();
      this.leaveObj = leave;
      this.modalRef = this.modalService.open(template);
    }
pipReason(teamObj){
  this.pageNo=1
  this.pipReasons = []

  let team = new Leave();

  team.empId = teamObj.empId;
  team.pipId = teamObj.pipId;
  team.pipFlag = teamObj.pipFlag;


this.leaveService.getPipReasons(team).pipe(first()).subscribe((response : any)=>{
  if(response.serviceStatus == "Success"){
    this.pipReasons = response.serviceResponse;
    if(this.pipReasons.length == 0){
      if(this.leaveObj.pipFlag == "true") this.isPipFlag=true;
      else this.isPipFlag=false;
    }
    this.pipReasons.forEach(d=>{
      d.createdOn = moment(d.createdOn).format(AppComponent.DATE_FORMAT);
      d.updatedOn = moment(d.updatedOn).format(AppComponent.DATE_FORMAT);
      console.log(" d ki value ",d)
      if(d.pipFlag == "true"){
        console.log("i am in true flag")
        this.isPipFlag = true;
      }else{
        console.log(" I'm in false flag")
        this.isPipFlag = false;
      }
      if(d.updatedOn == 'Invalid date') d.updatedOn = '';
      if(d.createdOn == 'Invalid date') d.createdOn = '';


    })
    console.log("this.pipReasons  ",this.pipReasons);
  }
})
  console.log(" team ",team);
  console.log(team,"teamteamteamteam")
}

checkDateChange(){
  this.isDateChanged = true;
  }

  pipReasonModal(template:TemplateRef<any>,teamObj){
  this.modalRef=this.modalService.open(template , { modalDialogClass : 'modal-lg'});
  this.pageNo=1
  this.leaveObj = teamObj;
  this.pipReason(this.leaveObj);
  }



extendPipModal(template : TemplateRef<any> , teamObj){
  this.modalRef=this.modalService.open(template , { modalDialogClass : 'modal-sm'});
  this.leaveObj = teamObj;
}

setPipExtendsDays(template:TemplateRef<any>){

  if(!this.isDateChanged){
    this.alertMessage="End date must be change for extend PIP";
    this.openAlertMod(template , this.alertMessage);
    return;
  }
  this.cancelRequest();

    let leave = new Leave();
    leave.pipId = this.leaveObj.pipId;
    leave.empId = this.leaveObj.empId;
    leave.updatedByName = this.currentUser.name;
    // leave.extendDays = this.leaveObj.extendDays;
    leave.extendReason = this.leaveObj.extendReason;
    leave.startDate = moment(this.startDate).format(AppComponent.DATE_FORMAT);
    leave.endDate = moment(this.endDate).format(AppComponent.DATE_FORMAT);
    console.log("team in set extend modal",leave)
    this.leaveService.setExtendPeriodByPipId(leave).pipe(first()).subscribe((response : any)=>{
      if(response.serviceStatus == "Success"){
        this.openAlertMod(template,response.serviceResponse);

      }else{
        this.openAlertMod(template,response.serviceResponse);
      }
    })

  }

  estimateEndDate() {
    if (this.startDate) {
      const startDate = new Date(this.startDate);
      const endDate = new Date(startDate.getTime() + (90 * 24 * 60 * 60 * 1000)); // Adding 90 days
      this.endDate = endDate.toISOString().split('T')[0];
    }
  }

renderBalkanChart(): void {
  if (!this.orgChartContainer) {
    return;
  }

  if (this.chart) {
    this.chart.destroy();
  }

  if (this.nodes.length === 0) {
    return;
  }

  this.nodeLookup = {};
  var balkanData = this.convertToBalkanFormat(this.nodes[0]);
  balkanData.forEach(node => {
    this.nodeLookup[node.id] = node;
  });

  OrgChart.templates.myTemplate = Object.assign({}, OrgChart.templates.ana);
  OrgChart.templates.myTemplate.size = [220, 120];

  OrgChart.templates.myTemplate.node =
    '<rect x="0" y="0" height="{h}" width="{w}" fill="url(#gradientBlue)" stroke-width="2" stroke="#64B5F6" rx="8" ry="8"></rect>' +
    '<defs>' +
    '<linearGradient id="gradientBlue" x1="0%" y1="0%" x2="0%" y2="100%">' +
    '<stop offset="0%" style="stop-color:#E3F2FD;stop-opacity:1" />' +
    '<stop offset="100%" style="stop-color:#BBDEFB;stop-opacity:1" />' +
    '</linearGradient>' +
    '</defs>';

  // Highlight Selected node
  OrgChart.templates.myTemplate.nodeMenuButton =
    '<rect x="0" y="0" height="{h}" width="{w}" fill="url(#gradientBlueActive)" stroke-width="3" stroke="#1976D2" rx="8" ry="8"></rect>' +
    '<defs>' +
    '<linearGradient id="gradientBlueActive" x1="0%" y1="0%" x2="0%" y2="100%">' +
    '<stop offset="0%" style="stop-color:#BBDEFB;stop-opacity:1" />' +
    '<stop offset="100%" style="stop-color:#90CAF9;stop-opacity:1" />' +
    '</linearGradient>' +
    '</defs>';

  OrgChart.templates.myTemplate.field_0 =
    '<foreignObject x="10" y="20" width="200" height="45">' +
    '<div xmlns="http://www.w3.org/1999/xhtml" style="font-size: 16px; font-weight: 700; color: #0D47A1; text-align: center; overflow: hidden; line-height: 1.3; text-shadow: 0 1px 2px rgba(255,255,255,0.8);">{val}</div>' +
    '</foreignObject>';

  OrgChart.templates.myTemplate.field_1 =
    '<foreignObject x="10" y="65" width="200" height="45">' +
    '<div xmlns="http://www.w3.org/1999/xhtml" style="font-size: 13px; font-weight: 500; color: #424242; text-align: center; overflow: hidden; line-height: 1.3;">{val}</div>' +
    '</foreignObject>';

  // Self-node
  OrgChart.templates.selfNode = Object.assign({}, OrgChart.templates.myTemplate);
  OrgChart.templates.selfNode.node =
    '<rect x="0" y="0" height="{h}" width="{w}" fill="url(#gradientGreen)" stroke-width="3" stroke="#66BB6A" rx="8" ry="8"></rect>' +
    '<defs>' +
    '<linearGradient id="gradientGreen" x1="0%" y1="0%" x2="0%" y2="100%">' +
    '<stop offset="0%" style="stop-color:#E8F5E9;stop-opacity:1" />' +
    '<stop offset="100%" style="stop-color:#C8E6C9;stop-opacity:1" />' +
    '</linearGradient>' +
    '</defs>';

  OrgChart.templates.selfNode.field_0 =
    '<foreignObject x="10" y="20" width="200" height="45">' +
    '<div xmlns="http://www.w3.org/1999/xhtml" style="font-size: 16px; font-weight: 700; color: #1B5E20; text-align: center; overflow: hidden; line-height: 1.3; text-shadow: 0 1px 2px rgba(255,255,255,0.8);">{val}</div>' +
    '</foreignObject>';

  OrgChart.templates.selfNode.field_1 =
    '<foreignObject x="10" y="65" width="200" height="45">' +
    '<div xmlns="http://www.w3.org/1999/xhtml" style="font-size: 13px; font-weight: 500; color: #2E7D32; text-align: center; overflow: hidden; line-height: 1.3;">{val}</div>' +
    '</foreignObject>';

  OrgChart.templates.managerNode = Object.assign({}, OrgChart.templates.myTemplate);
  OrgChart.templates.managerNode.node =
  '<rect x="0" y="0" height="{h}" width="{w}" fill="url(#gradientPastelBlue)" stroke-width="2" stroke="#8AB6F9" rx="8" ry="8"></rect>' +
  '<defs>' +
    '<linearGradient id="gradientPastelBlue" x1="0%" y1="0%" x2="0%" y2="100%">' +
      '<stop offset="0%" style="stop-color:#DCEBFF;stop-opacity:1" />' +
      '<stop offset="100%" style="stop-color:#A8C8FF;stop-opacity:1" />' +
    '</linearGradient>' +
  '</defs>';

  OrgChart.templates.managerNode.field_0 =
    '<foreignObject x="10" y="20" width="200" height="45">' +
    '<div xmlns="http://www.w3.org/1999/xhtml" style="font-size: 16px; font-weight: 700; color: #4A148C; text-align: center; overflow: hidden; line-height: 1.3; text-shadow: 0 1px 2px rgba(255,255,255,0.8);">{val}</div>' +
    '</foreignObject>';

  OrgChart.templates.managerNode.field_1 =
    '<foreignObject x="10" y="65" width="200" height="45">' +
    '<div xmlns="http://www.w3.org/1999/xhtml" style="font-size: 13px; font-weight: 500; color: #6A1B9A; text-align: center; overflow: hidden; line-height: 1.3;">{val}</div>' +
    '</foreignObject>';

  this.chart = new OrgChart(this.orgChartContainer.nativeElement, {
    nodes: balkanData,
    nodeBinding: {
      field_0: 'name',
      field_1: 'title'
    },
    tags: {
      'self-node': {
        template: 'selfNode'
      },
      'manager': {
        template: 'managerNode'
      }
    },
    layout: OrgChart.normal,
    enableSearch: false,
    orientation: OrgChart.orientation.top,
    template: 'myTemplate',
    collapse: {
      level: 3
    },
    mouseScrool: OrgChart.action.scroll,
    nodeMouseClick: OrgChart.action.details,
    keyNavigation: true,
    scaleInitial: 0.85,
    padding: 50,
    siblingSeparation: 80,
    subtreeSeparation: 100
  });

  // Handle click events
  this.chart.on('click', (sender: any, args: any) => {
    if (args.node) {
      const fullNodeData = this.nodeLookup[args.node.id];
      console.log('Clicked node full data:', fullNodeData);

      this.createHierarchyNodes({
        empId: fullNodeData.empId,
        managerId: fullNodeData.managerId,
      });
    }
    return false;
  });

  console.log("balkanData:", balkanData);
  balkanData = balkanData.filter(n => !n.pid || balkanData.some(p => p.id === n.pid));
  let centeredOnce = false;

  this.chart.on('render', () => {
  if (centeredOnce) return;
  const selfNode = balkanData.find((n: any) => n.tags?.includes('self-node'));
  if (selfNode) {
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        this.chart?.center(selfNode.id);
        centeredOnce = true;
      });
    });
  }
});

}

convertToBalkanFormat(rootNode: HierarchyUser, parentId: string | null = null): any[] {
  const result: any[] = [];

  const addNode = (node: HierarchyUser, pid: string | null) => {
    const balkanNode: any = {
      id: node.empId,
      pid: pid,
      name: node.name,
      title: node.title || '',
      empId: node.empId,
      managerId: node.managerId,
      tags: [],
    };

    if (node.cssClass) {
      balkanNode.tags.push(node.cssClass.toLowerCase());
    }

    if (node.id === 'self-node' || node.cssClass?.toLowerCase() === 'self-node') {
      balkanNode.tags.push('self-node');
    }

    if (pid === null && node.id !== 'self-node') {
      balkanNode.tags.push('manager');
    }

    result.push(balkanNode);

    if (node.childs && node.childs.length > 0) {
      node.childs.forEach(child => {
        addNode(child, node.empId);
      });
    }
  };

  addNode(rootNode, parentId);
  return result;
}



}

function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
}
