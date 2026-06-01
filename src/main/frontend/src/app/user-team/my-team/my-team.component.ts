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

  leaveRejectionReasonList: any[] = [];
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
  /** Expand/collapse keys (uses node.nodeUid so duplicate empIds do not share state). */
  expandedNodeKeys = new Set<string>();
  private hierarchyNodeUidSeq = 0;
  private childrenCache = new Map<any, HierarchyUser[]>(); // empId -> direct reportees
  private loadingChildren = new Set<any>();
  private readonly childrenBatchSize = 10;
  private childrenVisibleCount = new Map<any, number>(); // empId -> number of children currently rendered

  // Pan + zoom + focus
  @ViewChild('orgChartViewport', { static: false }) orgChartViewport?: ElementRef<HTMLElement>;
  chartScale = 1;
  private isChartDragging = false;
  private dragStartX = 0;
  private dragStartY = 0;
  private dragStartScrollLeft = 0;
  private dragStartScrollTop = 0;

  hierarchySearchText = '';
  hierarchySearchResults: HierarchyUser[] = [];
  selectedHierarchyEmpId: any = null;
  selectedHierarchyPath: HierarchyUser[] = [];

  isLeaveHistoryOfDepartment:boolean = false;
  departmentLeaveHistoryList:any[] = [];

  selectedDataIndex:any=0;
  showReporteeLeaveBalance:boolean = false;
  leaveBalanceList:any[] = [];

  reporteeLeaveRevokeApplicationList:any[] = [];

  isActionEnabled:boolean = false;

showOtherRemarks = false;
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

selectedRejectionIds: number[] = [];
  tableName: String;

  selectedNode: HierarchyUser | null = null;

  // Custom hierarchy chart (no paid dependency)



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

    this.getLeaveRejectionReasons();
    this.sectionViewInit();
  }


ngOnDestroy(): void {
  // no-op (custom hierarchy uses Angular template)
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
  onSingleRejectNew(template: TemplateRef<any>,) {
      this.leaveObj.rejectReason = this.leaveObj.rejectReason?.trim();
      if (!this.validationService.validateActivityTimesheetDiscription(this.leaveObj.rejectReason)) {
        this.alertMessage = "Please enter valid reason !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      this.onUpdateLeaveStatusNew(template, this.leaveObj, 3);
    }

     onUpdateLeaveStatusNew(
      template: TemplateRef<any>,
      leaveApplication: any,
      updatedLeaveStatusId: number
    ) {
      this.cancelRequest();
    
      // 1 = Pending, 2 = Approved, 3 = Rejected
      leaveApplication.leaveStatusId = updatedLeaveStatusId;
      leaveApplication.leaveStatusUpdatedBy = this.currentUser.empId;
      leaveApplication.rejectReason = leaveApplication.rejectReason?.trim();
    
      // New fields for rejection flow
      leaveApplication.rejectionIds = this.selectedRejectionIds || [];
    
      this.leaveService.updateLeaveStatusNew(leaveApplication)
        .pipe(first())
        .subscribe({
          next: (response: any) => {
            if (response.serviceStatus === "Success") {
              this.getAllMyTeamsPendingLeaveApplicationsByManagerId();
            }
    
            this.openAlertMod(template, response.serviceResponse);
            this.resetRejectModalData();
          },
          error: (error: any) => {
            console.error(error);
            this.openAlertMod(template, "Something went wrong.");
          }
        });
    }

    resetRejectModalData() {
      this.selectedRejectionIds = [];
      this.showOtherRemarks = false;
      this.leaveObj.rejectReason = '';
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
    this.expandedNodeKeys.clear();
    this.hierarchyNodeUidSeq = 0;
    this.childrenVisibleCount.clear();
    this.childrenCache.clear();
    this.loadingChildren.clear();
    this.selectedHierarchyEmpId = null;
    this.selectedHierarchyPath = [];
    this.hierarchySearchResults = [];
    this.hierarchySearchText = '';
    this.resetView();
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
          if (this.isSelfReportEmployeeRow(manager)) {
            managerNode.disabled = true;
          }
          this.ensureHierarchyNodeUid(managerNode);
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
              const u = new HierarchyUser(name, cssClass, title, empId, managerId);
              if (this.isSelfReportEmployeeRow(employee)) {
                u.disabled = true;
              }
              this.ensureHierarchyNodeUid(u);
              return u;
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
            if (this.isSelfReportEmployeeRow(self)) {
              user.disabled = true;
            }
            this.ensureHierarchyNodeUid(user);
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
              const u = new HierarchyUser(name, cssClass, title, empId, managerId);
              if (this.isSelfReportEmployeeRow(employee)) {
                u.disabled = true;
              }
              this.ensureHierarchyNodeUid(u);
              return u;
            });
          }else{
            console.error("Reportees Not found.");
          }

          user.childs.push(...reporteeList);
          coWorkerList.splice(MID_COUNTER,0,user)
          managerNode.childs.push(...coWorkerList);

        this.nodes.push(managerNode);

        const finishChartLayout = () => {
          this.applyDuplicateChildDisabled(this.nodes?.[0]);
          this.applyDefaultHierarchyExpansion(this.nodes?.[0], user);
          const focusId = user?.empId ?? managerNode?.empId;
          if (focusId != null) {
            this.setFocusNode(focusId);
          }
        };

        this.employeeService.getManagementSpineForHierarchy(employee).pipe(first()).subscribe({
          next: (spineRes: any) => {
            const spineRaw = spineRes?.serviceResponse as any[] | undefined;
            const spine = this.dedupeManagementSpineByEmpId(spineRaw);
            if (
              spineRes?.serviceStatus === 'Success' &&
              spine?.length > 1 &&
              manager?.empId != null &&
              managerNode?.empId != null
            ) {
              const last = spine[spine.length - 1];
              if (last != null && String(last.empId) === String(managerNode.empId)) {
                // Org-wide: one level = all direct reportees of each parent, not a single vertical line.
                let level = spine.length - 2;
                const stepUp = (cur: HierarchyUser) => {
                  if (level < 0) {
                    this.nodes = [cur];
                    finishChartLayout();
                    return;
                  }
                  const row = spine[level];
                  level -= 1;
                  this.wrapParentWithAllReporteesForOrgChart(row, cur).then((parent) => stepUp(parent));
                };
                stepUp(managerNode);
                return;
              }
            }
            finishChartLayout();
          },
          error: () => finishChartLayout()
        });
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  /**
   * Expand every node on the path from root to the logged-in user so multi-branch levels (e.g. all
   * reportees of a director) stay open for the path to "self".
   */
  private applyDefaultHierarchyExpansion(root: HierarchyUser | undefined, selfNode: HierarchyUser | null): void {
    const selfId = selfNode?.empId;
    if (root && selfId != null) {
      const path = this.findPathToEmpId(root, selfId);
      if (path?.length) {
        path.forEach((n) => {
          const k = this.expandNodeKey(n);
          if (k) {
            this.expandedNodeKeys.add(k);
          }
        });
        return;
      }
    }
    let n: HierarchyUser | undefined = root;
    while (n && (n.childs?.length === 1)) {
      const k = this.expandNodeKey(n);
      if (k) {
        this.expandedNodeKeys.add(k);
      }
      n = n.childs[0];
    }
    const nk = this.expandNodeKey(n);
    if (nk) {
      this.expandedNodeKeys.add(nk);
    }
    const sk = this.expandNodeKey(selfNode ?? undefined);
    if (sk) {
      this.expandedNodeKeys.add(sk);
    }
  }

  /** DB anomaly manager_id = emp_id — show node but keep non-interactive. */
  private isSelfReportEmployeeRow(row: any): boolean {
    return row?.empId != null && row?.managerId != null && String(row.empId) === String(row.managerId);
  }

  /** Same person as immediate parent (e.g. manager listed again under own reportees) — show but not clickable. */
  private applyDuplicateChildDisabled(root: HierarchyUser | undefined): void {
    if (!root?.childs?.length) return;
    const pid = root.empId;
    for (const c of root.childs) {
      if (c?.empId != null && pid != null && String(c.empId) === String(pid)) {
        c.disabled = true;
      }
      this.applyDuplicateChildDisabled(c);
    }
  }

  private ensureHierarchyNodeUid(node: HierarchyUser | undefined | null): void {
    if (node && !node.nodeUid) {
      node.nodeUid = 'h' + (++this.hierarchyNodeUidSeq);
    }
  }

  /** Expand/collapse map key (distinct per rendered card even when empId repeats). */
  private expandNodeKey(node: HierarchyUser | undefined | null): string {
    if (!node) {
      return '';
    }
    this.ensureHierarchyNodeUid(node);
    return node.nodeUid as string;
  }

  /** Removes duplicate spine rows (same empId) — prevents stacked duplicate parents in org-wide chart. */
  private dedupeManagementSpineByEmpId(spine: any[] | undefined): any[] {
    if (!spine?.length) {
      return [];
    }
    const seen = new Set<string>();
    const out: any[] = [];
    for (const row of spine) {
      const id = row?.empId != null ? String(row.empId) : '';
      if (!id || seen.has(id)) {
        continue;
      }
      seen.add(id);
      out.push(row);
    }
    return out;
  }

  /** Breadcrumb path: one entry per empId (tree can repeat same person if data is inconsistent). */
  private dedupeHierarchyPathByEmpId(path: HierarchyUser[]): HierarchyUser[] {
    const seen = new Set<string>();
    const out: HierarchyUser[] = [];
    for (const n of path) {
      const id = n?.empId != null ? String(n.empId) : '';
      if (!id) {
        out.push(n);
        continue;
      }
      if (seen.has(id)) {
        continue;
      }
      seen.add(id);
      out.push(n);
    }
    return out;
  }

  private wrapParentWithAllReporteesForOrgChart(parentMeta: any, childSubtree: HierarchyUser): Promise<HierarchyUser> {
    return new Promise((resolve) => {
      const emp = new Employee();
      emp.empId = parentMeta.empId;
      emp.managerId = parentMeta.managerId;
      emp.employeementId = parentMeta.empId;
      const fallbackSingleChild = () => {
        const parent = new HierarchyUser();
        parent.name = parentMeta.name;
        parent.cssClass = 'Manager';
        parent.title = `${parentMeta.jobRoleName || ''}, ${parentMeta.departmentName || ''}`;
        parent.empId = parentMeta.empId;
        parent.managerId = parentMeta.managerId;
        parent.childs = [childSubtree];
        this.ensureHierarchyNodeUid(parent);
        this.ensureHierarchyNodeUid(childSubtree);
        resolve(parent);
      };
      this.employeeService.getHierarchyChartByEmpId(emp).pipe(first()).subscribe({
        next: (res: any) => {
          const parent = new HierarchyUser();
          parent.name = parentMeta.name;
          parent.cssClass = 'Manager';
          const rc = parentMeta.reporteeCount;
          parent.title = `${parentMeta.jobRoleName || ''}, ${parentMeta.departmentName || ''}` +
            (rc != null && rc !== 0 ? `, ${rc} reportee(s)` : '');
          parent.empId = parentMeta.empId;
          parent.managerId = parentMeta.managerId;
          if (this.isSelfReportEmployeeRow(parentMeta)) {
            parent.disabled = true;
          }

          if (res?.serviceStatus !== 'Success') {
            parent.childs = [childSubtree];
            this.ensureHierarchyNodeUid(parent);
            this.ensureHierarchyNodeUid(childSubtree);
            resolve(parent);
            return;
          }
          const list = (res.serviceResponse ?? []) as any[];
          const reportees = list.filter((x) => (x?.hierarchyType ?? '').toLowerCase() === 'reportee');
          const childKey = String(childSubtree.empId);
          const sorted = reportees.slice().sort((a, b) =>
            String(a.name ?? '').localeCompare(String(b.name ?? ''))
          );
          const nodes: HierarchyUser[] = sorted.map((r) => {
            if (String(r.empId) === childKey) {
              this.ensureHierarchyNodeUid(childSubtree);
              return childSubtree;
            }
            const title =
              `${r.jobRoleName ?? ''}, ${r.departmentName ?? ''}` +
              (r.reporteeCount != null && r.reporteeCount !== 0 ? `, ${r.reporteeCount} reportee(s)` : '');
            const u = new HierarchyUser(r.name, 'Reportee', title, r.empId, r.managerId);
            if (this.isSelfReportEmployeeRow(r)) {
              u.disabled = true;
            }
            this.ensureHierarchyNodeUid(u);
            return u;
          });
          if (!nodes.some((n) => String(n.empId) === childKey)) {
            this.ensureHierarchyNodeUid(childSubtree);
            nodes.push(childSubtree);
            nodes.sort((a, b) => String(a.name ?? '').localeCompare(String(b.name ?? '')));
          }
          parent.childs = nodes;
          this.ensureHierarchyNodeUid(parent);
          resolve(parent);
        },
        error: () => fallbackSingleChild()
      });
    });
  }

  createHierarchyNodes(event){
    let employeeObj = new Employee();
    employeeObj.empId =  event.empId;
    employeeObj.managerId =  event.managerId;
    employeeObj.employeementId =  event.empId;
    this.myTeamHierarchyChart(employeeObj);
  }

  private fetchDirectReportees(empId: any, managerId: any): Promise<HierarchyUser[]> {
    if (!empId) return Promise.resolve([]);
    const cached = this.childrenCache.get(empId);
    if (cached) return Promise.resolve(cached);

    if (this.loadingChildren.has(empId)) return Promise.resolve([]);
    this.loadingChildren.add(empId);

    const employeeObj = new Employee();
    employeeObj.empId = empId;
    employeeObj.managerId = managerId;
    employeeObj.employeementId = empId;

    return new Promise((resolve) => {
      this.employeeService.getHierarchyChartByEmpId(employeeObj).pipe(first()).subscribe((response: any) => {
        this.loadingChildren.delete(empId);
        if (response?.serviceStatus !== 'Success') {
          this.childrenCache.set(empId, []);
          return resolve([]);
        }

        const list = (response.serviceResponse ?? []) as any[];
        const reportees = list.filter((x) => (x?.hierarchyType ?? '').toLowerCase() === 'reportee');
        const mapped = reportees.map(employee => {
          const name = employee.name;
          const cssClass = employee.hierarchyType;
          const title = `${employee.jobRoleName}, ${employee.departmentName} ${(employee.reporteeCount !== 0) ? `, ${employee.reporteeCount} reportee(s)` : ``}`;
          const childEmpId = employee.empId;
          const childManagerId = employee.managerId;
          const u = new HierarchyUser(name, cssClass, title, childEmpId, childManagerId);
          if (this.isSelfReportEmployeeRow(employee)) {
            u.disabled = true;
          }
          this.ensureHierarchyNodeUid(u);
          return u;
        });

        this.childrenCache.set(empId, mapped);
        resolve(mapped);
      }, _err => {
        this.loadingChildren.delete(empId);
        this.childrenCache.set(empId, []);
        resolve([]);
      });
    });
  }

  isExpanded(node: HierarchyUser): boolean {
    const k = this.expandNodeKey(node);
    return !!k && this.expandedNodeKeys.has(k);
  }

  toggleExpand(node: HierarchyUser, event?: MouseEvent): void {
    if (event) {
      event.stopPropagation();
      event.preventDefault();
    }
    if (!node?.empId || node.disabled) return;
    const key = this.expandNodeKey(node);
    if (!key) return;
    if (this.expandedNodeKeys.has(key)) {
      this.expandedNodeKeys.delete(key);
      this.childrenVisibleCount.delete(key);
    } else {
      this.expandedNodeKeys.add(key);
      if (node?.childs?.length) {
        this.childrenVisibleCount.set(key, Math.min(this.childrenBatchSize, node.childs.length));
      }
    }
  }

  onHierarchyNodeClick(node: HierarchyUser): void {
    if (!node?.empId || node.disabled) return;

    // Always keep parent/child context: expand in-place.
    // If we already have children, just expand.
    if (node?.childs?.length > 0) {
      const key = this.expandNodeKey(node);
      if (key) {
        this.expandedNodeKeys.add(key);
        this.childrenVisibleCount.set(key, Math.min(this.childrenBatchSize, node.childs.length));
      }
      this.setFocusNode(node.empId);
      return;
    }

    // Lazy-load this node's direct reportees and attach under it.
    this.fetchDirectReportees(node.empId, node.managerId).then(children => {
      node.childs = children ?? [];
      this.applyDuplicateChildDisabled(node);
      if (node.childs.length > 0) {
        const key = this.expandNodeKey(node);
        if (key) {
          this.expandedNodeKeys.add(key);
          this.childrenVisibleCount.set(key, Math.min(this.childrenBatchSize, node.childs.length));
        }
      }
      this.setFocusNode(node.empId);
    });
  }

  getVisibleChildren(node: HierarchyUser): HierarchyUser[] {
    if (!node?.childs?.length) return [];
    const nk = this.expandNodeKey(node);
    const count = this.childrenVisibleCount.get(nk) ?? Math.min(this.childrenBatchSize, node.childs.length);
    return node.childs.slice(0, count);
  }

  canShowMoreChildren(node: HierarchyUser): boolean {
    if (!node?.childs?.length) return false;
    const nk = this.expandNodeKey(node);
    const count = this.childrenVisibleCount.get(nk) ?? Math.min(this.childrenBatchSize, node.childs.length);
    return count < node.childs.length;
  }

  showMoreChildren(node: HierarchyUser, event?: MouseEvent): void {
    if (event) {
      event.stopPropagation();
      event.preventDefault();
    }
    if (!node?.childs?.length) return;
    const nk = this.expandNodeKey(node);
    const current = this.childrenVisibleCount.get(nk) ?? Math.min(this.childrenBatchSize, node.childs.length);
    const next = Math.min(node.childs.length, current + this.childrenBatchSize);
    this.childrenVisibleCount.set(nk, next);
  }

  showLessChildren(node: HierarchyUser, event?: MouseEvent): void {
    if (event) {
      event.stopPropagation();
      event.preventDefault();
    }
    if (!node?.childs?.length) return;
    const nk = this.expandNodeKey(node);
    this.childrenVisibleCount.set(nk, Math.min(this.childrenBatchSize, node.childs.length));
  }

  // ---------- Pan / zoom ----------
  onChartWheel(event: WheelEvent): void {
    event.preventDefault();
    const delta = event.deltaY;
    const factor = delta > 0 ? 0.9 : 1.1;
    const next = Math.min(1.8, Math.max(0.6, this.chartScale * factor));
    this.chartScale = Number(next.toFixed(3));
  }

  onChartMouseDown(event: MouseEvent): void {
    // only left button
    if (event.button !== 0) return;
    const viewport = this.orgChartViewport?.nativeElement;
    if (!viewport) return;
    this.isChartDragging = true;
    this.dragStartX = event.clientX;
    this.dragStartY = event.clientY;
    this.dragStartScrollLeft = viewport.scrollLeft;
    this.dragStartScrollTop = viewport.scrollTop;
  }

  onChartMouseMove(event: MouseEvent): void {
    if (!this.isChartDragging) return;
    const viewport = this.orgChartViewport?.nativeElement;
    if (!viewport) return;
    const dx = event.clientX - this.dragStartX;
    const dy = event.clientY - this.dragStartY;
    viewport.scrollLeft = this.dragStartScrollLeft - dx;
    viewport.scrollTop = this.dragStartScrollTop - dy;
  }

  onChartMouseUp(): void {
    this.isChartDragging = false;
  }

  zoomIn(): void {
    this.chartScale = Math.min(1.8, Number((this.chartScale * 1.15).toFixed(3)));
  }

  zoomOut(): void {
    this.chartScale = Math.max(0.6, Number((this.chartScale / 1.15).toFixed(3)));
  }

  resetView(): void {
    this.chartScale = 1;
    const viewport = this.orgChartViewport?.nativeElement;
    if (viewport) {
      viewport.scrollLeft = 0;
      viewport.scrollTop = 0;
    }

    // After reset, center back to self (preferred) or root.
    const root = this.nodes?.[0] as HierarchyUser | undefined;
    const selfNode = this.findFirstNodeByPredicate(root, (n) => n?.id === 'self-node');
    const anchorId = selfNode?.empId ?? root?.empId ?? this.selectedHierarchyEmpId;
    if (anchorId != null) {
      setTimeout(() => this.centerOnEmpId(anchorId), 0);
    }
  }

  // ---------- Jump navigation (start/end) ----------
  goToStart(): void {
    const viewport = this.orgChartViewport?.nativeElement;
    if (!viewport) return;
    viewport.scrollLeft = 0;
    viewport.scrollTop = 0;
  }

  goToEnd(): void {
    const viewport = this.orgChartViewport?.nativeElement;
    if (!viewport) return;
    viewport.scrollLeft = viewport.scrollWidth;
    viewport.scrollTop = viewport.scrollHeight;
  }

  private findFirstNodeByPredicate(
    node: HierarchyUser | undefined,
    predicate: (n: HierarchyUser) => boolean
  ): HierarchyUser | null {
    if (!node) return null;
    if (predicate(node)) return node;
    const kids = node.childs ?? [];
    for (const child of kids) {
      const found = this.findFirstNodeByPredicate(child, predicate);
      if (found) return found;
    }
    return null;
  }

  // ---------- Search + focus ----------
  onHierarchySearchChange(): void {
    const q = (this.hierarchySearchText || '').trim().toLowerCase();
    if (!q) {
      this.hierarchySearchResults = [];
      return;
    }
    const results: HierarchyUser[] = [];
    const seen = new Set<any>();
    this.walkLoadedHierarchy(this.nodes?.[0], (n) => {
      if (!n?.empId || seen.has(n.empId)) return;
      const hay = `${n.name ?? ''} ${n.title ?? ''} ${n.empId ?? ''}`.toLowerCase();
      if (hay.includes(q)) {
        results.push(n);
        seen.add(n.empId);
      }
    });
    this.hierarchySearchResults = results.slice(0, 20);
  }

  focusHierarchyResult(node: HierarchyUser): void {
    if (!node?.empId) return;
    this.hierarchySearchResults = [];
    this.hierarchySearchText = node.name;
    this.setFocusNode(node.empId);
  }

  private setFocusNode(empId: any): void {
    this.selectedHierarchyEmpId = empId;
    const path = this.findPathToEmpId(this.nodes?.[0], empId);
    this.selectedHierarchyPath = this.dedupeHierarchyPathByEmpId(path ?? []);
    // expand ancestors
    this.selectedHierarchyPath.forEach((p) => {
      const ek = this.expandNodeKey(p);
      if (ek) {
        this.expandedNodeKeys.add(ek);
      }
    });

    // If the target is inside a "Show more" slice, widen the slice so it becomes visible.
    for (let i = 0; i < this.selectedHierarchyPath.length - 1; i++) {
      const parent = this.selectedHierarchyPath[i];
      const child = this.selectedHierarchyPath[i + 1];
      if (!parent?.empId || !parent?.childs?.length || !child?.empId) continue;
      const idx = parent.childs.findIndex((c: any) => String(c?.empId) === String(child.empId));
      if (idx >= 0) {
        const pk = this.expandNodeKey(parent);
        const current = this.childrenVisibleCount.get(pk) ?? Math.min(this.childrenBatchSize, parent.childs.length);
        this.childrenVisibleCount.set(pk, Math.max(current, idx + 1));
      }
    }
    // center on node
    setTimeout(() => this.centerOnEmpId(empId), 0);
  }

  onBreadcrumbClick(node: HierarchyUser): void {
    if (!node?.empId) return;
    this.setFocusNode(node.empId);
  }

  isFocusedNode(node: HierarchyUser): boolean {
    if (this.selectedHierarchyEmpId == null || !node?.empId) return false;
    return String(this.selectedHierarchyEmpId) === String(node.empId);
  }

  private centerOnEmpId(empId: any): void {
    const viewport = this.orgChartViewport?.nativeElement;
    if (!viewport) return;
    const target = viewport.querySelector(`[data-emp-id="${String(empId)}"]`) as HTMLElement | null;
    if (!target) return;

    const vRect = viewport.getBoundingClientRect();
    const tRect = target.getBoundingClientRect();
    const vCx = vRect.left + vRect.width / 2;
    const vCy = vRect.top + vRect.height / 2;
    const tCx = tRect.left + tRect.width / 2;
    const tCy = tRect.top + tRect.height / 2;

    viewport.scrollLeft += (tCx - vCx);
    viewport.scrollTop += (tCy - vCy);
  }

  private walkLoadedHierarchy(root: HierarchyUser | undefined, visit: (n: HierarchyUser) => void): void {
    if (!root) return;
    visit(root);
    if (root.childs && root.childs.length > 0) {
      root.childs.forEach((c: any) => this.walkLoadedHierarchy(c, visit));
    }
  }

  private findPathToEmpId(root: HierarchyUser | undefined, empId: any): HierarchyUser[] | null {
    return this.findPathToEmpIdImpl(root, empId, new Set<string>());
  }

  /**
   * Path to target empId; pathStack tracks current chain (add/remove on backtrack) so shared
   * empIds in the tree do not block searching sibling branches, but true cycles on one path stop.
   */
  private findPathToEmpIdImpl(
    root: HierarchyUser | undefined,
    empId: any,
    pathStack: Set<string>
  ): HierarchyUser[] | null {
    if (!root) return null;
    const k = root.empId != null ? String(root.empId) : '';
    if (k) {
      if (pathStack.has(k)) {
        return null;
      }
      pathStack.add(k);
    }
    if (String(root.empId) === String(empId)) {
      if (k) {
        pathStack.delete(k);
      }
      return [root];
    }
    const kids = root.childs ?? [];
    for (const child of kids) {
      const found = this.findPathToEmpIdImpl(child, empId, pathStack);
      if (found) {
        if (k) {
          pathStack.delete(k);
        }
        return [root, ...found];
      }
    }
    if (k) {
      pathStack.delete(k);
    }
    return null;
  }

  // ---------- Export ----------
  exportVisibleHierarchyToExcel(): void {
    if (!this.nodes?.length) return;
    const rows: any[] = [];
    const walk = (node: HierarchyUser, parentEmpId: any, level: number, pathNames: string[]) => {
      const role = (node?.cssClass ?? '').toString();
      const currentPath = [...pathNames, (node?.name ?? '').toString()].filter(Boolean);
      rows.push({
        'Employee Id': node.empId ?? '',
        'Employee Name': node.name ?? '',
        'Role (Hierarchy)': role || '',
        'Title / Dept': node.title ?? '',
        'Manager Id': node.managerId ?? '',
        'Parent Employee Id': parentEmpId ?? '',
        'Level': level,
        'Path': currentPath.join(' > '),
      });

      // "Visible" = what is currently expanded on screen
      if (node.childs?.length && this.expandedNodeKeys.has(this.expandNodeKey(node))) {
        node.childs.forEach((c: any) => walk(c, node.empId, level + 1, currentPath));
      }
    };

    walk(this.nodes[0], '', 0, []);
    this.exportExcelService.exportTableDataToExcel(rows, 'MyTeam_Hierarchy_Visible.xlsx');
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

// Balkan OrgChart removed (license expired). Custom hierarchy chart is rendered in the template.

  onRejectionReasonChange() {
    const other = this.leaveRejectionReasonList.find(
      x => x.reason.toLowerCase() === 'other'
    );

    this.showOtherRemarks =
      other && this.selectedRejectionIds.includes(other.rejectionReasonId);

    if (!this.showOtherRemarks) {
      this.leaveObj.rejectReason = '';
    }
  }
  isRejectDisabled(): boolean {
    if (this.selectedRejectionIds.length === 0) {
      return true;
    }

    if (!this.leaveObj.rejectReason ||
      !this.leaveObj.rejectReason.trim()) {
      return true;
    }

    return false;
  }
  getLeaveRejectionReasons() {
  this.leaveService.getLeaveRejectionReasons()
    .subscribe({
      next: (response: any) => {
        this.leaveRejectionReasonList = response;
        console.log('Rejection Reasons:', this.leaveRejectionReasonList);
      },
      error: (error: any) => {
        console.error(error);
      }
    });
}

}

function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
}
