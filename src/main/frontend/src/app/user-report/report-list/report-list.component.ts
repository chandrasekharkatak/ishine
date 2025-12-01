import { CdkDragDrop, CdkDragRelease, moveItemInArray } from "@angular/cdk/drag-drop";
import { formatDate, Location, LocationStrategy } from '@angular/common';
import { Component, OnInit, Renderer2, TemplateRef, ViewChild } from '@angular/core';
import { FormControl } from "@angular/forms";
import { Sort } from '@angular/material/sort';
import { Router } from "@angular/router";
import * as Highcharts from "highcharts";
import * as moment from 'moment';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first, map, startWith } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Employee } from 'src/app/models/employee';
import { employeeReport } from "src/app/models/employeeReport";
import { Feature } from 'src/app/models/feature';
import { FilteredTimesheet } from "src/app/models/filteredTimesheet";
import { JobRole } from 'src/app/models/jobRole';
import { Query } from 'src/app/models/query';
import { Timesheet } from 'src/app/models/timesheet';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { DepartmentService } from 'src/app/services/department.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { JobRoleService } from 'src/app/services/job-role.service';
import { LeaveService } from 'src/app/services/leave.service';
import { ProjectService } from "src/app/services/project.service";
import { ResourceManagementService } from "src/app/services/resource-management.service";
import { TimesheetService } from 'src/app/services/timesheet.service';
import { UtilityService } from 'src/app/services/utility.service';
import { ValidationService } from 'src/app/services/validation.service';
import * as XLSX from 'xlsx';

class FilterData {
  title: any;
  columns: any;
  queryList: any;
}
interface Project {
  projectId: string;
  projectName: string;
}
@Component({
  standalone: false,
  selector: 'app-report-list',
  templateUrl: './report-list.component.html',
  styleUrls: ['./report-list.component.css']
})
export class ReportListComponent implements OnInit {

  @ViewChild("alert_message")
  alertModal: TemplateRef<any>;

  @ViewChild("alert_message_sync")
  alertModalSync: TemplateRef<any>;


@ViewChild('projectDetailsModal') 
projectDetailsModal!: TemplateRef<any>;

@ViewChild('employeeCountModal')
employeeCountModal!: TemplateRef<any>;

@ViewChild('alert_message_timesheet_leave_report') alert_message_timesheet_leave_report: TemplateRef<any>;
alert_message_timesheet_leave_reportModalRef: NgbModalRef;

bsModalRef?: NgbModalRef; 
selectedClientProjectViewOption: string = 'default';



  feature = 'Reports';
  currentUser: User;
  userMapping: any = {};

  data: string; //Search Data
  designationData: string;   //Search Designation

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;

  //modal 
  alertMessage: any;
  modalRef:NgbModalRef;
  modalRef1:NgbModalRef;

  employeeObj: Employee = new Employee();

  isLeaveReportTable: boolean = false;
  isTimesheetReportTable: boolean = false;
  isEmployeeReportTable: boolean = false;
  isAccessControlListTable: boolean = false;
  isLeaveTimesheetReportTable: boolean = false
  isCustomQueryForm: boolean = false;
  isAccessFeatureMapping: boolean = false;
  isDefaultFeatureMapping: boolean = false;

  allEmployeeList: any[] = [];
  deptWiseConsolidated: any[] = [];

  allLeaveApplicationsList: any[] = [];
  leaveApplicationsDataForExcel: any[] = [];

  allTimesheetApplicationsList: any[] = [];
  timesheetApplicationsDataForExcel: any[] = [];

  allJobRoleList: any[] = [];
  personaWiseJobRole: any[] = [];
  accessControlList: any[] = [];
  mappedSubFeatureList: any[] = [];
  subfeatureList: any[] = [];
  defaultMappingList: any[] = [];
  defaultMappingListFilter: any[] = [];
  updateDefaultMapping: any[] = [];

  updatedRoleSubFeature: any[] = [];
  hiddenColumnObj: any[] = [];
  showColumnList: any[] = [];
  filteringTimesheet: FilteredTimesheet = new FilteredTimesheet();

  insideCols: any[] = [];

  storedDataList: any[] = [];
  clientAndProjectReportList:any[]=[];
  departmentList: string[] = [];
  excelName: any;
  jobRoleName: any;
  departmentId: any;
  selectedProjectId: any;
  employeeRole: any;
  selectedColumnToShow: any;
  noOfDays: number | null = null;
  extendEmployee:boolean=false;
  totalItems:number = 0;
  modalMessage: string = '';
  exportAll:boolean=false;
  activeQueryList: any[] = [];
  isFilterApplied: boolean = false;
  activeQueryListForFilter: any[] = [];


  leaveColumns: any[] = ['Employee Id', 'Full Name', 'Employment Status', 'Leave Type', 'Team Name', 'Project Name', 'Client Name', 'Department', 'From Date', 'To Date', 'No. of Days', 'Reason', 'Status', 'Manager Name', 'Created On', 'Updated On', 'Updated By'];
  employeeColumns: any[] = ['Employee Id', 'Full Name', 'Department', 'Designation', 'Job Role', 'Manager', 'Team Name', 'Project Name', 'Po No', 'Po Start Date', 'Po End Date', 'Po Project Type', 'Client Name', 'Employment Status', 'Date Of Joining', 'Domain', 'Specialization', 'City', 'Blood Group', 'Gender', 'Work Location', 'Probation Period', 'Notice Period', 'Marital Status', 'Bank Name', 'Created By', 'State', 'Created On', 'Profile Completion'];
  timesheetColumns: any[] = ['Employee Id', 'Full Name', 'Employment Status', 'Department', 'Date', 'Day Type', 'Status', 'Total Working Hour', 'Team Name', 'Project Name', 'Client Name', 'From Date', 'To Date', 'Created On', 'Updated On', 'Updated By', 'Leave Type'];
  filteredTimesheetReportColumns: any[] = [
    'employmentIdAcToET',
    'employeeType',
    'employeeName',
    'departmentName',
    'date',
    'dayType',
    'projectName',
    'teamName',
    'clientName',
    'clientLocation',
    'activity',
    'description',
    'managerName',
    'status',
    'totalTime',
    'officeInTime',
    'officeOutTime',
    'leaveType',
    'createdByName',
    'createdOn'
  ];

  queryList: any[] = [];
  filterData: any = new FilterData();

  columns: any[] = [];
  paginateData: any[] = [];
  pos: any;
  release: boolean = true;
  finalColumns: any[] = [];
  clientAndProjectReportDataList:any[]=[];
  allLeaveTimesheets: any[] = [];
  endDate: any;
  startDate: any;
  employeeReportObj: employeeReport = new employeeReport();
  customQuery: any;
  leaveReportFlag: boolean = false;
  timesheetReportFlag: boolean = false;
  showDetails: boolean = false;
  showDetailsTimesheet: boolean = false;
  changeTable: boolean = true;
  showTable: boolean = false;
  hideMaternityLeaveEmps: boolean = true;

  filters: any = {};
  isSearchEnabled: boolean = false;

  employeeReportColumnForDetailedProjectViewClub: any[] = ['blank', 'employeementIdAccToET', 'name', 'departmentName', 'jobRoleName', 'managerName', 'mobileNo', 'email', 'employmentstatus', 'billable', 'billableType', 'teamName', 'projectName', 'poNo', 'poProjectType', 'poStartDate', 'poEndDate', 'effectiveStartDate', 'effectiveEndDate', 'clientName', 'clientLocation', 'workLocation', 'experience', 'primaryProjectName'];
  employeeReportColumnForDetailedProjectView: any[] = ['blank', 'employeementIdAccToET', 'name', 'departmentName', 'jobRoleName', 'managerName', 'mobileNo', 'email', 'employmentstatus', 'billable', 'billableType', 'teamName', 'projectName', 'poNo', 'poProjectType', 'poStartDate', 'poEndDate', 'effectiveStartDate', 'effectiveEndDate', 'clientName', 'clientLocation', 'workLocation', 'experience'];
  leaveReportColumns: any[] = ['employmentIdAcToET', 'employeeType', 'employeeName', 'leaveType', 'fromDate', 'toDate', 'fromDateDayType', 'toDateDayType', 'noOfDays', 'reason', 'status', 'managerName', 'departmentName', 'createdOn', 'updatedOn', 'leaveStatusUpdatedByName'];
  timesheetReportColumns: any[] = ['employeementId', 'employeeType', 'employeeName', 'date', 'dayType', 'description', 'status', 'totalWorkingHours', 'officeInTime', 'officeOutTime', 'totalWorkingOfficeHours', 'leaveType', 'createdOn', 'updatedOn', 'timesheetStatusUpdatedByName'];
  employeeReportColumn: any[] = ['blank', 'employeementId', 'employeeType', 'name', 'departmentName', 'jobRoleName', 'managerName', 'mobileNo', 'email', 'employmentstatus', 'projectName', 'poNo', 'poStartDate', 'poEndDate', 'poProjectType', 'clientName', 'billable', 'billableType', 'updatedOn', 'updatedByName', 'createdByName', 'createdOn'];
  leaveTimesheetReportColumn: any[] = ['employmentIdAcToET', 'employeeType', 'employeeName', 'date', 'dayType', 'description', 'status', 'managerName', 'departmentName', 'createdOn', 'updatedOn', 'timesheetStatusUpdatedByName'];
  defaultMappingColumns: any[] = ['tabName', 'featureName', 'subFeatureName'];
  employeeReportColumnForDetailedProjecttttView: any[] = ['blank',
    'projectName', 'projectManager', 'apmosysRM', 'clientRM',
    'poStartDate', 'poEndDate', 'poNo', 'poProjectType', 'teamName',
    'employeeName', 'jobRole', 'deptName', 'mobileNo', 'email',
    'billable', 'billableType', 'effectiveStartDate'
  ];
  employeesFor360: any[] = [];
  departments: any[] = [];
  allEmployee: any[] = [];
  allInactivePOListOfEmployee: any[] = [];
    allactivePOListOfEmployee: any[] = [];
  filteredEmployees: any[] = [];
  filteredEmployees2: any[] = [];
  allProjectPOInternal: any[] = [];
  tnmPoExpiredCount = 0;
  tnmPOValidCount = 0;
  fixedCostPoExpiredCount = 0;
  fixedCostPoValidCount = 0;
  internalCount = 0;
  tnmProjectCount = 0;
  fixedCostProjectCount = 0;
  tnmPoProjectExpiredCount = 0;
  fixedCostPoProjectExpiredCount = 0;
  tnmPOProjectActiveCount = 0;
  fixedCostPoProjectActiveCount = 0;
  internalProjectCount = 0;
  selectedDepartment: string = 'All';
  flatProjectList: any[] = [];
  groupedClientProjects: any[] = [];
  activeBox: string | null = null;
  isHovering: string | null = null;

  tnmPoExpiredCountList: any[] = [];
  tnmPOValidCountList: any[] = [];
  fixedCostPoExpiredCountList: any[] = [];
  fixedCostPoValidCountList: any[] = [];
  internalCountList: any[] = [];
  newemployeeObj: any;
  updatedEmpObj: any;
  show: number = -1;
  filteredTimesheets: any;
  toastr: any;
  isAccounts: boolean = false;
  isDeptFilter: boolean = false;
  dept: any;
  returnUrl: string | null = null;
  employeeCtrl = new FormControl();
  selectedEmpId: any = 0;
  summaryModalRef: NgbModalRef;
  projectSummaryData: any[] = [];
  searchText: string = '';
  visibleInfo: boolean = false;
  inActiveBoxinfo: any;
  activeBoxinfo: any;
  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;

  private allDepartments: any[] = [];
  showBillableOnly: boolean = false;
  totalEmployeeInActivePoCount: any;
  total7Days: string = '';
  total90Days: string = '';
  total180Days: string = '';
  total1Year: string = '';
  totalAll: string = '';
  total30Days: string = '';
  //InActivePoCounts: { type: string; count: string; }[];
  InActivePoCounts: {
dateRange: string; type: string; count: string;
}[] = [];
ActivePoCounts: {
dateRange: string; type: string; count: string;
}[] = [];
  visibleInfo1: boolean = false;
  isLoadingModalData: boolean;
  countData: { [key: string]: any } = {
    'TNM': {},
    'Fixed Cost': {},
    'Monitoring': {},
    'Internal': {}
  };
  inActivePEmployeeColumns:any[] = ['employeementIdAccToET','name','projectName','poNo','poProjectType','poStartDate','poEndDate','clientName','clientLocation'];

  constructor(
    private authenticationService: AuthenticationService,
    private modalService: NgbModal,
    private employeeService: EmployeeService,
    private exportExcelService: ExportExcelService,
    private timesheetService: TimesheetService,
    private leaveService: LeaveService,
    private jobRoleService: JobRoleService,
    private validationService: ValidationService,
    private renderer2: Renderer2,
    private locationStrategy: LocationStrategy,
    private utilityService: UtilityService,
    private departmentService: DepartmentService,
    private location: Location, private router: Router,
    private resourceManagementService: ResourceManagementService,
    private projectService: ProjectService
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    const navigation = this.router.getCurrentNavigation();
    this.returnUrl = navigation?.extras?.state?.['returnUrl'] || null;
  }

  async ngOnInit(): Promise<void> {
    this.hideMaternityLeaveEmps = true;

    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    console.log("userMapping", this.userMapping);
    await this.getAllDepartments();

    this.preventBackButton();

    const deptName = String(this.currentUser.departmentName).trim();
    if (deptName === "Accounts") {
      this.isAccounts = true;
    }
    const empRole = String(this.currentUser.employeeRole).trim();
    if (!deptName.includes("Admin") && !deptName.includes("Resource Management Group") && !deptName.includes("Director") && !deptName.includes("Super Admin") && !empRole.includes("SuperAdmin") && !empRole.includes("Accounts") && !deptName.includes("Accounts") && !deptName.includes("HR")) {
      await this.getAllDepartmentsFromId();
      this.isDeptFilter = true;
      console.log("I am here")
    } else {
      await this.getAllDepartments();
    }

    this.setDepartmentView(true);
    this.sectionViewInit();
    this.getTotalActiveEmployeeCount();
    this.getEmployeeByNameAndEmpld();
    // this.getClientAndProjectReport();
    this.employeeCtrl.valueChanges
      .pipe(
        startWith(''),
        map(value => typeof value === 'string' ? value : value?.name || ''),
        map(name => this.filterEmployees2(name))
      )
      .subscribe(filtered => {
        this.filteredEmployees2 = filtered;
      });

    console.log("On ngOnInIt Toggle ",this.employeeReportObj);
  }

  private refreshReportData(): void {
    console.log("Refreshing data with dept IDs:", this.employeeReportObj);
    this.getEmployeeReportData();
    ['TNM', 'Fixed Cost', 'Monitoring', 'Internal'].forEach(box => {
     
      this.selectedTab[box] = 'Employee';
      this.selectedFlag[box] = null;
      this.selectedBillable[box] = null;
      const payload = this.buildPayload(box);
      console.log("The payload is",payload);
      this.getEmployeeProjectCount(box, payload);
    });
    this.getTotalActiveEmployeeCountInDepartments();
    this.projectLessEmployeesDepartmentWise();
    this.employeesMappedProjectsDepartmentWise();
  }

  setDepartmentView(showBillable: boolean) {
    this.showBillableOnly = showBillable;
    
    if (this.showBillableOnly) {
      console.log("Billable Departments",this.departmentHistory);
      this.departments = [...this.departmentHistory.filter(dept => dept.isBillable == 'Yes' || dept.isBillable == true)];
      console.log("Billable departments",this.departments)
      this.filteredDepartments = [...this.departmentHistory.filter(dept => dept.isBillable)];
    } else {
      this.departments = [...this.allDepartments];
      this.filteredDepartments = [...this.allDepartments];

    }

    this.employeeReportObj.deptId = this.departments.map(dept => dept.deptId);

    this.filterDepartments();
    this.updateSelectAllState();
    this.refreshReportDataWithModifiedCount();
  }

  updateSelectAllState(): void {
    const selectedCount = this.employeeReportObj.deptId?.length - 1 || 0;
    const totalCount = this.departments.length;


    this.isAllSelected = totalCount > 0 && selectedCount === totalCount;
  }


  isAllSelected = true;
  showSearchInput = true;



  toggleSelectAll(): void {
    //  event.stopPropagation();
    if (this.isAllSelected) {
      this.employeeReportObj.deptId = [];
      this.clearSelection();
      console.log("All departments deselected 0", this.employeeReportObj.deptId);

    } else {
      this.isAllSelected = true;
      console.log("All departments deselected 1", this.employeeReportObj.deptId);
      this.employeeReportObj.deptId = this.departments.map(dept => dept.deptId);

    }
    this.refreshReportDataWithModifiedCount();
    // this.isAllSelected = !this.isAllSelected;
    // this.updateSelectAllState();
    // this.refreshReportData();
  }

  filteredDepartments: any[] = [];
  filterDepartments() {
    const lowerText = this.searchText.toLowerCase();
    this.filteredDepartments = this.departments.filter(dept =>
      dept && dept.name && dept.name.toLowerCase().includes(lowerText)
    );
  }

  clearSelection() {

    this.employeeReportObj.deptId = [];
    this.isAllSelected = false;
    this.searchText = '';
    this.filterDepartments();
    this.updateSelectAllState();
    // this.refreshReportData();
    this.refreshReportDataWithModifiedCount();
  }

  onDepartmentSelectionChange() {
    this.updateSelectAllState();
    // this.refreshReportData();
    this.refreshReportDataWithModifiedCount();
  }

  getSlicedProjects(projectList: Project[], count: number): Project[] {
    return projectList.slice(0, count);
  }

  preventBackButton() {
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(() => {
      history.pushState(null, null, location.href);
    })
  }
  totalInshineEmployeeCount: any;
  getTotalActiveEmployeeCount() {
    this.employeeService.getTotalActiveEmployeeCount().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        this.totalInshineEmployeeCount = response.serviceResponse;
      } else {
        console.error("API Error: ", response.serviceError || "Unknown error");
      }
    });
  }
  totalDepartmentWiseEmployeeCount: any;
  getTotalActiveEmployeeCountInDepartments() {
    console.log("After Toggle ",this.employeeReportObj);
    this.employeeService.getTotalActiveEmployeeCountInDepartments(this.employeeReportObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        this.totalDepartmentWiseEmployeeCount = response.serviceResponse;
      } else {
        console.error("API Error: ", response.serviceError || "Unknown error");
      }
    });
  }

  projectLessEmployeesDepartwise: any[] = [];
  projectLessEmployeesDepartwiseCount: any;
  billableCountsNotMApped: { type: any; count: any }[] = [];
  projectLessEmployeesDepartmentWise() {
    this.employeeService.projectLessEmployeesDepartmentWise(this.employeeReportObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        this.projectLessEmployeesDepartwise = response.serviceResponse;
        this.projectLessEmployeesDepartwiseCount = this.projectLessEmployeesDepartwise.length;
        const billableTypeMap = new Map<string, number>();

        this.projectLessEmployeesDepartwise.forEach(emp => {
          const type = emp.billableType;
          if (type) {
            billableTypeMap.set(type, (billableTypeMap.get(type) || 0) + 1);
          }
        });

        this.billableCountsNotMApped = Array.from(billableTypeMap.entries())
          .filter(([_, count]) => count > 0)
          .map(([type, count]) => ({ type, count }))
          .sort((a, b) => a.type.length - b.type.length);
      } else {
        console.error("API Error: ", response.serviceError || "Unknown error");
      }
    });
  }


  employeesWithProjectDeptWise: any[] = [];
  employeesWithProjectDeptWiseCount: any;
  mappedBillableCounts: { type: any; count: any }[] = [];
  employeesMappedProjectsDepartmentWise() {
    this.employeeService.employeesMappedProjectsDepartmentWise(this.employeeReportObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        this.employeesWithProjectDeptWise = response.serviceResponse;
        this.employeesWithProjectDeptWiseCount = this.employeesWithProjectDeptWise.length;

        const billableTypeMap = new Map<string, number>();

        this.employeesWithProjectDeptWise.forEach(emp => {
          const type = emp.billableType;
          if (type) {
            billableTypeMap.set(type, (billableTypeMap.get(type) || 0) + 1);
          }
        });

        this.mappedBillableCounts = Array.from(billableTypeMap.entries())
          .filter(([_, count]) => count > 0)
          .map(([type, count]) => ({ type, count }))
          .sort((a, b) => a.type.length - b.type.length);
      } else {
        console.error("API Error: ", response.serviceError || "Unknown error");
      }
    });
  }

  employeeData: any[] = [];
  catagory: any
  openModalProjectLess(template: TemplateRef<any>, catagory: string) {

    this.employeeData = this.projectLessEmployeesDepartwise;
    this.catagory = catagory;

    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
  }

  openModalEmployeesWithProjects(template: TemplateRef<any>, catagory: string) {
    this.employeeData = this.employeesWithProjectDeptWise;
    this.catagory = catagory;

    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });

  }

  activeInfoPopup: any;

  closeModal() {
    this.modalRef.close();
  }

  closeModal1() {
    this.modalRef1.close();
  }



  toggleInfoPopup(target: string): void {
    this.activeInfoPopup = this.activeInfoPopup === target ? null : target;
  }

  closeInfoPopup(event: MouseEvent) {
    event.stopPropagation();
    this.activeInfoPopup = null;
  }

  flattenProjectList() {
    this.flatProjectList = [];
    for (const project of this.projectList) {
      for (const team of project.teamDetails) {
        for (const emp of team.mappedEmployeeDetails) {
          this.flatProjectList.push({
            ...emp, 
            projectName: project.projectName,
            projectManager: project.projectManager,
            apmosysRM: project.apmosysRM,
            clientRM: project.clientRM,
            poStartDate: project.poStartDate,
            poEndDate: project.poEndDate,
            poNo: project.poNo,
            poProjectType: project.poProjectType,
            teamName: team.teamName,
          });
        }
      }
    }
    console.log(this.flatProjectList,"this.flatProjectList");
  }

  sectionViewInit() {
    if (this.userMapping.employee_report) {
      if (!this.showDetails) {
        this.isEmployeeReportTable = true;
        this.toggleView();

      }
      else
        this.showEmployeeReportTable();
    } else if (this.userMapping.timesheet_report) {
      this.showTimesheetReportTable();
    } else if (this.userMapping.leave_report) {
      this.showLeaveReportTable();
    }
  }

  toggleViewTimesheet() {
    if (this.showDetailsTimesheet === true) {
      this.showDetailsTimesheet = false;
    }
    else {
      this.showDetailsTimesheet = true;
      this.selectedDepartment = 'all';
      const today = new Date();
      const oneMonthAgo = new Date();
      oneMonthAgo.setMonth(today.getMonth() - 1);

      this.filteringTimesheet.startDate = oneMonthAgo.toISOString().split('T')[0];
      this.filteringTimesheet.endDate = today.toISOString().split('T')[0];
      this.filteringTimesheet.deptId = this.selectedDepartment;
      this.getAllDepartments();
      this.getAllOrDeptWiseEmployeeTimesheetReport();

    }
  }


  activeBoxx: string = '';
  selectedTab: any = {};
  selectedFlag: any = {};
  selectedBillable: any = {};

  selectTab(box: string, tab: string) {
    console.log("This is selected");
    this.activeBox = box;
    console.log("Active box is",this.activeBox);
    console.log("The selected tab is",this.selectedTab);
    Object.keys(this.selectedTab).forEach(key => {
      if (key !== box) {
        this.selectedTab[key] = null;
        this.selectedFlag[key] = null;
        this.selectedBillable[key] = null;
      }
    });

    this.selectedTab[box] = tab;
    this.selectedFlag[box] = null;
    this.selectedBillable[box] = null;

    this.employeeReportObj.poProjectType = box;
    this.employeeReportObj.category = tab;
    this.employeeReportObj.flag = null;
    this.employeeReportObj.billableType = null;
    this.employeeReportObj.hideMaternityLeaveEmps = this.hideMaternityLeaveEmps;
    if (this.employeeReportObj.category === 'Project') {
      this.employeeReportObj.report = 'P';
    } else if (this.employeeReportObj.category === 'Employee' && this.changeTable === true) {
      this.employeeReportObj.report = 'EC';
    } else {
      this.employeeReportObj.report = 'E';
    }
    const payload = this.buildPayload(box);
    this.getEmployeeProjectCount(box, payload);
    this.getEmployeeReportData();
  }

  selectFlag(box: string, flag: string, template: TemplateRef<any>) {
    this.activeBox = box;
    const category = this.selectedTab[box];
    if (!category) {
      this.openAlertMod(this.alertModal, "Please select a category (Employee / Project) before selecting Flag.");
      return;
    }

    Object.keys(this.selectedTab).forEach(key => {
      if (key !== box) {
        this.selectedTab[key] = null;
        this.selectedFlag[key] = null;
        this.selectedBillable[key] = null;
      }
    });
    this.selectedFlag[box] = flag;
    this.selectedBillable[box] = null;

    this.employeeReportObj.poProjectType = box;
    this.employeeReportObj.category = this.selectedTab[box];
    this.employeeReportObj.flag = flag;
    this.employeeReportObj.billableType = null;
    this.employeeReportObj.hideMaternityLeaveEmps = this.hideMaternityLeaveEmps;

    this.getEmployeeReportData();
    const payload = this.buildPayload(box);
    this.getEmployeeProjectCount(box, payload);
    if(flag === 'Inactive') {this.getInActivePoCount(this.employeeReportObj);}
    else if(flag === 'Active') {this.getActivePoCount(this.employeeReportObj);}
    
    
  }

 selectBillable(box: string, type: string, template: TemplateRef<any>) {
  const previousBox = this.activeBox;
  const prevCategory = this.selectedTab[previousBox];
  const prevFlag = this.selectedFlag[previousBox];
  const prevBillable = this.selectedBillable[previousBox];

  if (prevCategory) {
    this.selectedTab[box] = prevCategory;
  } else {
    this.selectedTab[box] = 'Employee';
  }

  if (previousBox && previousBox !== box && prevCategory && prevBillable) {
    Object.keys(this.selectedTab).forEach(key => {
      if (key !== box) {
        this.selectedTab[key] = null;
        this.selectedFlag[key] = null;
        this.selectedBillable[key] = null;
      }
    });

    this.selectedFlag[box] = box === 'Internal' ? null : (prevFlag || null);
    this.selectedBillable[box] = type; 
  } else {
    Object.keys(this.selectedTab).forEach(key => {
      if (key !== box) {
        this.selectedTab[key] = null;
        this.selectedFlag[key] = null;
        this.selectedBillable[key] = null;
      }
    });
    this.selectedBillable[box] = type;
  }

  this.activeBox = box;

  this.employeeReportObj.poProjectType = box;
  this.employeeReportObj.category = this.selectedTab[box] || '';
  this.employeeReportObj.flag = this.selectedFlag[box];
  this.employeeReportObj.billableType = [this.selectedBillable[box]];
  this.employeeReportObj.hideMaternityLeaveEmps = this.hideMaternityLeaveEmps;

  this.getEmployeeReportData();
    const payload = this.buildPayload(box);
    this.getEmployeeProjectCount(box, payload);

  if (box === 'Internal') { 
    if (this.selectedFlag[box] === 'Inactive') {
      this.getInActivePoCount(this.employeeReportObj);
    } else if (this.selectedFlag[box] === 'Active') {
      this.getActivePoCount(this.employeeReportObj);
    }
  }
}

  getRowspanForProject(project: any): number {
    if (!project.teamDetails) return 0;
    return project.teamDetails.reduce((acc: number, team: any) => {
      return acc + (team.mappedEmployeeDetails?.length || 0);
    }, 0);
  }

  poProjectSync(template: TemplateRef<any>) {
    this.employeeService.getPoProjectSync().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        this.openAlertMod(this.alertModalSync, response.serviceResponse);
      }
    });
  }

  editIndex: number = -1;
  billableTypes: string[] = ['Bench', 'Fixed Cost', 'Shadow', 'InternalRNDProducts', 'TNM'];
  updateBillableType(employee: any, template: TemplateRef<any>) {
    const updatedBillable = employee.billableType === 'TNM' ? 'Yes' : 'No';

    const payload = {
      empId: employee.empId,
      billableType: employee.billableType,
      billable: updatedBillable,
      updatedBy: this.currentUser.empId
    };

    console.log(payload);

    this.employeeService.updateEmployeeReportBillableType(payload).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        this.openAlertMod(this.alertModalSync, response.serviceResponse);
      }
    });

  }

  selectedSingleEmployee: any;
  selectedBillableTypeForSingle: any;


  showSingleUpdateModal(employee: any, template: TemplateRef<any>) {
    this.selectedBillableTypeForSingle = employee.billableType;
    this.selectedSingleEmployee = {
      ...employee,
      oldBillableType: employee.originalBillableType || employee.billableTypeBeforeChange || ''
    };
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl modal-dialog-centered' });
  }


  onConfirmSingleBillableUpdate() {
    const updatedBillable = this.selectedBillableTypeForSingle === 'TNM' ? 'Yes' : 'No';

    const payload = {
      empId: this.selectedSingleEmployee.empId,
      billableType: this.selectedBillableTypeForSingle,
      billable: updatedBillable,
      updatedBy: this.currentUser.empId
    };

    this.employeeService.updateEmployeeReportBillableType(payload).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === 'Success') {
        this.openAlertMod(this.alertModalSync, response.serviceResponse);
      }
      this.modalRef?.close();
    });
  }


  employeeList: any[] = [];
  projectList: any[] = [];
  projectSummary: any = {};
  getEmployeeReportData() {
    this.page = 1;
    this.employeeList = [];
    this.projectList = [];
    this.projectSummary = {};

    this.employeeReportObj.hideMaternityLeaveEmps = !this.employeeReportObj.hideMaternityLeaveEmps;
    // console.log("updated", this.employeeReportObj);
    this.employeeService.getEmployeeProjectReport(this.employeeReportObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        const res = response.serviceResponse;
        this.employeeList = res.getEmployeeProjectReportForEmployeeDTO || [];
        // console.log("employeeList before", this.employeeList);
        this.employeeList.forEach(employee => {
          employee.emp360EmpId = employee.empId;
          employee.emp360ManagerId = employee.managerId;

        });
        this.projectList = res.getProjectToEmployeeReportForProjectDTO || [];
        this.projectList.forEach(project => {
          project.teamDetails.forEach(team => {
            team.mappedEmployeeDetails.forEach(employee => {
              employee.emp360EmpId = employee.empId;
              employee.emp360ManagerId = project.projectManagerId;
            });
          });
        });
        // console.log("employeeList", this.employeeList); 
        // console.log("projectList", this.projectList);
        this.flattenProjectList();
        this.editIndex = -1
        // console.log("this.page", this.page);
      } else {
        console.error("API Error: ", response.serviceError || "Unknown error");
      }
    });


  }

  getTotalCount(box: string): number {
    const selectedMainFlag = this.selectedFlag[this.activeBox] || 'Default';
    const key = `${box}.${selectedMainFlag}`;
    const isActiveBox = box === this.activeBox;
    const category = this.selectedTab[this.activeBox];

    // console.log("BOx Type ", box);
    // console.log(" key ::::::::::::", key);
    // console.log("category :::::::::::::", category);
    // console.log("Is Active box :::::::::::::::::::", isActiveBox);

    if (category === 'Project' && isActiveBox) {
      return this.projectSummary[key]?.Project?.total_projects_per_po_project || 0;
    }

    return this.projectSummary[key]?.Employee?.totalEmpPerProjectType || 0;
  }

  getCount(box: string, billableType: string): number {
    const selectedMainFlag = this.selectedFlag[this.activeBox] || 'Default';
    const key = `${box}.${selectedMainFlag}`;
    const isActiveBox = box === this.activeBox;
    const category = this.selectedTab[this.activeBox];

    if (category === 'Project' && isActiveBox) {
      return this.projectSummary[key]?.Project?.[billableType] || 0;
    }

    return this.projectSummary[key]?.Employee?.[billableType] || 0;
  }

  masterSelected: boolean = false;
  selectedEmployees: any[] = [];

  selectAllEmployees() {
    for (let emp of this.employeeList) {
      emp.isSelected = this.masterSelected;
    }
    this.updateSelectedEmployees();
  }

  checkIfAllSelected() {
    this.masterSelected = this.employeeList.every(emp => emp.isSelected);
    this.updateSelectedEmployees();
  }

  updateSelectedEmployees() {
    this.selectedEmployees = this.employeeList.filter(emp => emp.isSelected);
  }

  openBulkUpdateModal(template: TemplateRef<any>) {
    if (this.selectedEmployees.length > 0 && this.selectedBillableTypeForBulk) {
      this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl modal-dialog-centered' });
    }
  }

  selectedBillableTypeForBulk: any;
  onConfirmBulkBillableUpdate(template: TemplateRef<any>) {
    if (!this.selectedBillableTypeForBulk || this.selectedEmployees.length === 0) return;
    const updatedBillable = this.selectedBillableTypeForBulk === 'TNM' ? 'Yes' : 'No';
    const empIds = this.selectedEmployees.map(emp => emp.empId);
    const payload = {
      empIds: empIds,
      billableType: this.selectedBillableTypeForBulk,
      billable: updatedBillable,
      updatedBy: this.currentUser.empId
    };
    console.log(payload);
    this.employeeService.updateBulkBillableEmployeeReport(payload).subscribe(
      (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.openAlertMod(this.alertModalSync, response.serviceResponse);
          this.selectedEmployees = [];
          this.selectedBillableTypeForBulk = '';
          this.masterSelected = false;
        } else {
          this.openAlertMod(this.alertModalSync, response.serviceResponse);
        }
      },
      (error) => {
        this.openAlertMod(this.alertModalSync, "Something went wrong while updating");
      }
    );
  }

  getAllOrDeptWiseEmployeeTimesheetReport() {
    this.filteringTimesheet.deptId = this.selectedDepartment;
    this.timesheetService.getAllOrDeptWiseEmployeeTimesheetReport(this.filteringTimesheet).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        this.filteredTimesheets = response.serviceResponse;
        this.filteredTimesheets.forEach(timesheet => {
          timesheet.employeementId = "A-".concat(timesheet.employeementId);
          timesheet.employeeType = ((timesheet.isApprenticeship === 'true') ? 'Apprentice' : ((timesheet.isConsultant === 'true') ? 'Consultant' : 'Regular')),
            timesheet.date = (timesheet.date) ? moment(timesheet.date).format(AppComponent.DATE_FORMAT) : null;
          timesheet.officeInTime = (timesheet.officeInTime) ? moment(timesheet.officeInTime).format(AppComponent.DATETIME_FORMAT) : null;
          timesheet.officeOutTime = (timesheet.officeOutTime) ? moment(timesheet.officeOutTime).format(AppComponent.DATETIME_FORMAT) : null;
          timesheet.createdOn = (timesheet.createdOn) ? moment(timesheet.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        });
        console.log("Timesheets fetched successfully:", this.filteredTimesheets);
      } else {
        console.error("Error fetching timesheets:", response.serviceResponse);
      }
    });
  }

  toggleView() {
    console.log("This is clicked")
    this.showDetails = !this.showDetails;
    this.show = -1;
    if (this.showDetails === false) {
      this.showEmployeeReportTable()
    }
    else {
      this.changeTable = true;
      this.selectedDepartment = 'all';
      this.employeeReportObj.deptId = this.departments.map(dept => dept.deptId);
      this.employeeReportObj.poProjectType = null;
      this.employeeReportObj.category = 'Employee';
      this.employeeReportObj.flag = null;
      this.employeeReportObj.billableType = null;
      this.employeeReportObj.report = 'EC';
      this.employeeReportObj.hideMaternityLeaveEmps = !this.hideMaternityLeaveEmps;

      this.isAllSelected = true;
      // ['TNM', 'Fixed Cost', 'Monitoring', 'Internal'].forEach(box => {
      //   this.selectedTab[box] = 'Employee';
      //   this.selectedFlag[box] = null;
      //   this.selectedBillable[box] = null;
      //   const payload = this.buildPayload(box);
      //   console.log("THe payload is",payload);
      //   this.getEmployeeProjectCount(box, payload);
      // });

      // this.activeBox = '';
      // this.getEmployeeReportData();
      // this.getTotalActiveEmployeeCountInDepartments();
      // this.projectLessEmployeesDepartmentWise();
      // this.employeesMappedProjectsDepartmentWise();
      // this.getClientAndProjectReport();
      this.refreshReportDataWithModifiedCount();
      this.allEmployee = [];
      this.tnmPOValidCountList = [];
      this.tnmPoExpiredCountList = [];
      this.fixedCostPoExpiredCountList = [];
      this.fixedCostPoValidCountList = [];
      this.internalCountList = [];
      this.activeBox = '';
    }
  }
  toggleTableView() {
    // this.activeBox = "";
    if (this.changeTable === true) {
      this.changeTable = false;
    }
    else {
      this.changeTable = true;
    }
    if (this.employeeReportObj.category === 'Project') {
      this.employeeReportObj.report = 'P';
    } else if (this.employeeReportObj.category === 'Employee' && this.changeTable === true) {
      this.employeeReportObj.report = 'EC';
    } else {
      this.employeeReportObj.report = 'E';
    }
    // this.getEmployeeReportData();
    // ['TNM', 'Fixed Cost', 'Monitoring', 'Internal'].forEach(box => {
    //   const payload = this.buildPayload(box);
    //   console.log("THe payload is",payload);
    //   this.getEmployeeProjectCount(box, payload);
    // });
    // this.getTotalActiveEmployeeCountInDepartments();
    // this.projectLessEmployeesDepartmentWise();
    // this.employeesMappedProjectsDepartmentWise();
    this.refreshReportDataWithModifiedCount();
  }

  // toggleTableViewForClient(){
  //  this.activeBox = "";
  //   if (this.showTable === true) {
  //     this.showTable = false;
  //   }
  //   else {
  //     this.showTable = true;
  //   }
  //   if (this.employeeReportObj.category === 'Project') {
  //     this.employeeReportObj.report = 'P';
  //   } else if (this.employeeReportObj.category === 'Employee' && this.showTable === true) {
  //     this.employeeReportObj.report = 'EC';
  //   } else {
  //     this.employeeReportObj.report = 'E';
  //   }
  // //  this.openClientProjectViewModal();
  //  this.getClientAndProjectReport();

  // }
toggleTableViewForClient() {
  this.activeBox = "";
  
  // Reset client/project specific search and sort when toggling
  this.clientProjectFilters = {};
  this.isClientProjectSearchEnabled = false;
  
  if (this.showTable === true) {
    this.showTable = false;
  } else {
    this.showTable = true;
  }
  
  if (this.employeeReportObj.category === 'Project') {
    this.employeeReportObj.report = 'P';
  } else if (this.employeeReportObj.category === 'Employee' && this.showTable === true) {
    this.employeeReportObj.report = 'EC';
  } else {
    this.employeeReportObj.report = 'E';
  }
  
  this.getClientAndProjectReport();
}

toggleClientProjectSearch() {
  this.sortColumn = [];
  this.sortColumnType = [];
  this.sortDirection = '';
  this.isClientProjectSearchEnabled = !this.isClientProjectSearchEnabled;
  if (!this.isClientProjectSearchEnabled) {
    this.clientProjectFilters = {};
  }
}

onSearchClientProject(searchData: any) {
  this.clientProjectFilters = searchData;
}
 toggleClientRow(clientName: string): void {
    this.expandedClients[clientName] = !this.expandedClients[clientName];
  }

// openClientProjectViewModal() {
//     this.getClientAndProjectReport();
//     this.bsModalRef = this.modalService.open(this.clientProjectViewModal, { modalDialogClass: 'modal-lg' });
//   }

  closeClientProjectViewModal() {
    this.bsModalRef?.close();
  }

  applyClientProjectViewOption() {
    console.log('Selected Client/Project View Option:', this.selectedClientProjectViewOption);
    if (this.selectedClientProjectViewOption === 'client') {
     
      this.changeTable = true;
    } else if (this.selectedClientProjectViewOption === 'project') {
      
      this.changeTable = true; 
    } else {
      this.changeTable = false; 
    }
    this.closeClientProjectViewModal();
  }


  departmentChange() {
    this.getEmployeeReportData();
    ['TNM', 'Fixed Cost', 'Monitoring', 'Internal'].forEach(box => {
      const payload = this.buildPayload(box);
      console.log("THe payload is",payload);
      this.getEmployeeProjectCount(box, payload);
    });
  }

  onMouseOver(box: string): void {
    this.isHovering = box;
  }

  onMouseLeave(): void {
    this.isHovering = null;
  }
  getAllEmployeesReportByProjectTypeInConsolidated() {
    this.allEmployee = [];
    this.tnmPOValidCountList = [];
    this.tnmPoExpiredCountList = [];
    this.fixedCostPoValidCountList = [];
    this.fixedCostPoExpiredCountList = [];
    this.internalCountList = [];

    this.employeeService.getAllEmployeesReportByProjectTypeInConsolidated().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allEmployee = response.serviceResponse;
        console.log('emps -- ', this.allEmployee);
        this.filterEmployees();


        this.allEmployee.forEach((emp) => {
          if (!emp.poEndDate) {
            this.internalCountList.push(emp);
            return;
          }

          const currentDate = new Date();
          const poEndDate = new Date(emp.poEndDate);

          const projectTypes = emp.poProjectType.toLowerCase().split(',');

          if (poEndDate < currentDate) {
            projectTypes.forEach((type) => {
              type = type.trim();
              if (type === 'tnm') {
                this.tnmPoExpiredCountList.push(emp);
              } else if (type === 'fixed cost') {
                this.fixedCostPoExpiredCountList.push(emp);
              }
            });

          } else {
            projectTypes.forEach((type) => {
              type = type.trim();
              if (type === 'tnm') {
                this.tnmPOValidCountList.push(emp);
              } else if (type === 'fixed cost') {
                this.fixedCostPoValidCountList.push(emp);
              }
            });
          }
        });

      } else {
        alert(response.serviceResponse);
      }
    });
  }


  deptWiseCount() {
    this.tnmPoExpiredCount = 0;
    this.tnmPOValidCount = 0;
    this.fixedCostPoExpiredCount = 0;
    this.fixedCostPoValidCount = 0;
    this.internalCount = 0;
    this.allEmployee = [];
    this.tnmPOValidCountList = [];
    this.tnmPoExpiredCountList = [];
    this.fixedCostPoValidCountList = [];
    this.fixedCostPoExpiredCountList = [];
    this.internalCountList = [];
    this.filteredEmployees = [];

    const departmentFiltered = this.selectedDepartment === 'all'
      ? this.deptWiseConsolidated
      : this.deptWiseConsolidated.filter(emp => emp.departmentId == this.selectedDepartment);
    this.filteredEmployees = departmentFiltered;
    departmentFiltered.forEach((emp) => {
      if (!emp.poEndDate) {
        this.internalCount++;
        this.internalCountList.push(emp);
        return;
      }

      const currentDate = new Date();
      const poEndDate = new Date(emp.poEndDate);

      const projectTypes = emp.poProjectType.toLowerCase().split(',');

      if (poEndDate < currentDate) {
        projectTypes.forEach((type) => {
          type = type.trim();
          if (type === 'tnm') {
            this.tnmPoExpiredCount++;
            this.tnmPoExpiredCountList.push(emp);
          } else if (type === 'fixed cost') {
            this.fixedCostPoExpiredCount++;
            this.fixedCostPoExpiredCountList.push(emp);
          }
        });

      } else {
        projectTypes.forEach((type) => {
          type = type.trim();
          if (type === 'tnm') {
            this.tnmPOValidCount++;
            this.tnmPOValidCountList.push(emp);
          } else if (type === 'fixed cost') {
            this.fixedCostPoValidCount++;
            this.fixedCostPoValidCountList.push(emp);
          }
        });
      }
    });
  }

  onBoxClickDataChange(boxName) {
    this.filteredEmployees = [];
    this.activeBox = boxName;

    if (boxName === 'TNM-Active') {
      this.filteredEmployees = this.tnmPOValidCountList;
    } else if (boxName === 'TNM-Expired') {
      this.filteredEmployees = this.tnmPoExpiredCountList;

    } else if (boxName === 'FC-Active') {
      this.filteredEmployees = this.fixedCostPoValidCountList;
    } else if (boxName === 'FC-Expired') {
      this.filteredEmployees = this.fixedCostPoExpiredCountList;

    } else {
      this.filteredEmployees = this.internalCountList;
    }
    console.log('Box click data-- :::::::::::::::::', this.filteredEmployees);
  }

  onDepartmentChangeTimesheet(event: any) {
    this.selectedDepartment = event.target.value;
    this.getAllOrDeptWiseEmployeeTimesheetReport();
  }

  onDepartmentChange(event: any) {
    this.activeBox = "";
    this.filteredEmployees = [];
    this.selectedDepartment = event.target.value;
    console.log('Selected Department:', this.selectedDepartment);
    this.deptWiseCount();
  }
  departmentHistory: any[] = [];
  getAllDepartmentsFromId(): Promise<any> {
    return new Promise((resolve, reject) => {
      this.departmentService.getAllDepartmentsFromId(this.currentUser.empId).pipe(first()).subscribe({
        next: (response: any) => {
          if (response.serviceStatus === "Success") {
            // this.departments = response.serviceResponse;
            // this.filteredDepartments = [...this.departments];
            this.departmentHistory = response.serviceResponse;
            console.log("departments",this.departments);

            resolve(response.serviceResponse);
          } else {
            reject("Failed to fetch departments");
          }
        },
        error: (error) => {
          reject(error);
        }
      });
    });
  }

  getAllDepartments(): Promise<any> {
    return new Promise((resolve, reject) => {
      this.departmentService.getAllDepartments().pipe(first()).subscribe({
        next: (response: any) => {
          if (response.serviceStatus === "Success") {
            console.log("ALldepartment list is",response.serviceResponse);
            this.allDepartments = response.serviceResponse;
            // this.departments = response.serviceResponse;
            // this.filteredDepartments = [...this.departments];
            this.departmentHistory = response.serviceResponse;
            resolve(response.serviceResponse);
          } else {
            reject("Failed to fetch departments");
          }
        },
        error: (error) => {
          reject(error);
        }
      });
    });
  }


  filterEmployees() {
    this.filteredEmployees = [];
    if (this.selectedDepartment) {
      if (this.selectedDepartment === 'all') {
        this.filteredEmployees = [...this.allEmployee];
      } else {
        this.filteredEmployees = this.allEmployee.filter(emp => emp.departmentId == this.selectedDepartment);
        this.filteredEmployees = this.filteredEmployees.map(employee => ({
          ...employee,
          showFullTeamName: false
        }));
      }
    }
  }

  updateDefaultProject(employee: any) {
    this.updatedEmpObj = new Employee();
    console.log('emp data :::::::::::', employee);
    this.getProjectId(employee);

    const selectedProjectId = this.getProjectId(employee);
    console.log("Selected Project ID:", selectedProjectId);
    this.selectedProjectId = selectedProjectId;

    let newemployeeObj: Employee = new Employee();
    newemployeeObj.selectedProjectId = this.selectedProjectId;
    newemployeeObj.projectName = employee.projectName;
    newemployeeObj.empId = employee.empId;
    newemployeeObj.updatedBy = this.currentUser.empId;

    console.log('new Employee OBJ :::::::::::::', newemployeeObj);

    this.employeeService.updateDefaultProject(newemployeeObj).pipe(first()).subscribe((response: any) => {
      console.log('response ::::::::::::::::::::::', response);
      if (response.serviceStatus === "Success") {
        this.updatedEmpObj = response.serviceResponse;
        this.openAlertMod(this.alertModal, "Default Project Updated Successfully  !! ")
        this.getEmployeeReportData();
        ['TNM', 'Fixed Cost', 'Monitoring', 'Internal'].forEach(box => {
          const payload = this.buildPayload(box);
          console.log("THe payload is",payload);
          this.getEmployeeProjectCount(box, payload);
        });
      }
    });
  }

  getProjectId(employee: any): string | null {
    if (employee.projectName && employee.projectIds) {
      const projectNames = employee.projectName.split(',');
      const projectIds = employee.projectIds.split(',');

      if (projectNames.length > 0 && projectIds.length > 0) {
        const selectedIndex = projectNames.findIndex(project => project.trim() === employee.selectedProject.trim());

        if (selectedIndex !== -1 && selectedIndex < projectIds.length) {
          return projectIds[selectedIndex].trim();
        }
      }
    } else {
      return null;
    }
  }
  showLeaveReportTable() {
    this.leaveReportFlag = true;
    this.timesheetReportFlag = false;
    this.storedDataList = [];
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.page = 1;
    this.isLeaveReportTable = true;

    this.isTimesheetReportTable = false;
    this.isEmployeeReportTable = false;
    this.isAccessControlListTable = false;
    this.isCustomQueryForm = false;
    this.isLeaveTimesheetReportTable = false;

    this.filters = {};
    this.isSearchEnabled = false;

    const today = new Date();
    const oneMonthBefore = new Date();
    oneMonthBefore.setMonth(today.getMonth() - 1);

    const formatDate = (date: Date): string => {
      const yyyy = date.getFullYear();
      const mm = String(date.getMonth() + 1).padStart(2, '0');
      const dd = String(date.getDate()).padStart(2, '0');
      return `${yyyy}-${mm}-${dd}`;
    };

    const currentDate = formatDate(today);
    const oneMonthBeforeDate = formatDate(oneMonthBefore);


    this.storedDataList.forEach((object) => {
      if (object.filterName == 'Filter Leave Report') {
        this.getCustomLeaveApplicationsList(object.queryList, this.alertModal);
      }
    });

    if (this.storedDataList.length == 0 || !this.storedDataList.find(x => x.filterName == 'Filter Leave Report')) {
      let inActiveQuery = [
        { column: "Employment Status", operator: "!=", value: "InActive", conjunction: "AND" },
        { column: "From Date", operator: ">=", value: oneMonthBeforeDate, conjunction: "AND" },
        { column: "To Date", operator: "<=", value: currentDate, conjunction: "" }


      ];
      this.getCustomLeaveApplicationsList(inActiveQuery, this.alertModal);
    }
    this.data = ''
  }

  showTimesheetReportTable() {
    this.leaveReportFlag = false;
    this.timesheetReportFlag = true;
    this.storedDataList = [];
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.page = 1;
    this.isTimesheetReportTable = true;

    this.isLeaveReportTable = false;
    this.isEmployeeReportTable = false;
    this.isAccessControlListTable = false;
    this.isCustomQueryForm = false;
    this.isLeaveTimesheetReportTable = false;
    this.filters = {};
    this.isSearchEnabled = false;

    const today = new Date();
    const oneMonthBefore = new Date();
    oneMonthBefore.setMonth(today.getMonth() - 1);

    const formatDate = (date: Date): string => {
      const yyyy = date.getFullYear();
      const mm = String(date.getMonth() + 1).padStart(2, '0');
      const dd = String(date.getDate()).padStart(2, '0');
      return `${yyyy}-${mm}-${dd}`;
    };

    const currentDate = formatDate(today);
    const oneMonthBeforeDate = formatDate(oneMonthBefore);
    this.storedDataList.forEach((object) => {
      if (object.filterName == 'Filter Timesheet Report') {
        this.getCustomTimesheetApplicationsList(object.queryList, this.alertModal);
      }
    });

    if (this.storedDataList.length == 0 || !this.storedDataList.find(x => x.filterName == 'Filter Timesheet Report')) {
      this.activeQueryList = [
        { column: "Employment Status", operator: "!=", value: "InActive", conjunction: "AND" },
        { column: "Date", operator: ">=", value: oneMonthBeforeDate, conjunction: "AND" },
        { column: "Date", operator: "<=", value: currentDate, conjunction: "" }
      ];


      
      this.getCustomTimesheetApplicationsList(this.activeQueryList, this.alertModal);
    }

    this.data = ''
  }

  showEmployeeReportTable() {
    this.leaveReportFlag = false;
    this.timesheetReportFlag = false;
    this.storedDataList = [];
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.page = 1;
    this.isEmployeeReportTable = true;

    this.isLeaveReportTable = false;
    this.isTimesheetReportTable = false;
    this.isAccessControlListTable = false;
    this.isCustomQueryForm = false;
    this.isLeaveTimesheetReportTable = false;
    this.filters = {};
    this.isSearchEnabled = false;


    this.storedDataList.forEach((object) => {
      if (object.filterName == 'Filter Employee Report') {
        this.getCustomEmployeesList(object.queryList, this.alertModal);
      }
    });

    if (this.storedDataList.length == 0 || !this.storedDataList.find(x => x.filterName == 'Filter Employee Report')) {
      let inActiveQuery = [
        { column: "Employment Status", operator: "!=", value: "InActive", conjunction: "" }
      ];

      this.getCustomEmployeesList(inActiveQuery, this.alertModal);
    }
    this.data = ''
  }

  showAccessControlListTable() {
    this.leaveReportFlag = false;
    this.timesheetReportFlag = false;
    this.isAccessControlListTable = true;

    this.isEmployeeReportTable = false;
    this.isLeaveReportTable = false;
    this.isTimesheetReportTable = false;
    this.isCustomQueryForm = false;
    this.isLeaveTimesheetReportTable = false;
    this.isAccessFeatureMapping = false;
    this.isDefaultFeatureMapping = false;
    this.data = '';
    this.columns = [];
    this.paginateData = [];
    this.finalColumns = [];
    this.toggleAccessList();
  }

  showLeaveTimesheetReportTable() {
    this.leaveReportFlag = false;
    this.timesheetReportFlag = false;
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.isLeaveTimesheetReportTable = true;
    this.startDate = null;
    this.endDate = null;

    this.isCustomQueryForm = false;
    this.isAccessControlListTable = false;
    this.isEmployeeReportTable = false;
    this.isLeaveReportTable = false;
    this.isTimesheetReportTable = false;
    this.filters = {};
    this.isSearchEnabled = false;

    this.allLeaveTimesheets = [];
  }

  showCustomQueryForm() {
    this.leaveReportFlag = false;
    this.timesheetReportFlag = false;
    this.isCustomQueryForm = true;
    this.customQuery = null;

    this.isAccessControlListTable = false;
    this.isEmployeeReportTable = false;
    this.isLeaveReportTable = false;
    this.isTimesheetReportTable = false;
    this.isLeaveTimesheetReportTable = false;
  }

  showDefaultMappingTable() {
    this.getDefaultMapping(this.alertModal);
  }

  disableMannualDateInput() {
    return false;
  }

  getAllLeaveApplicationsList() {
    this.queryList = [];
    this.allLeaveApplicationsList = [];

    this.leaveService.leaveReport().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allLeaveApplicationsList = response.serviceResponse;
        this.allLeaveApplicationsList.forEach(leave => {
          leave.employeementId = "A-".concat(leave.employeementId);
          leave.employeeType = (leave.isApmosysProduct === 'true') 
  ? 'Apmosys Product' 
  : ((leave.isApprenticeship === 'true') 
    ? 'Apprentice' 
    : ((leave.isConsultant === 'true') 
      ? 'Consultant' 
      : 'On roll')),
            leave.fromDate = (leave.fromDate) ? moment(leave.fromDate).format(AppComponent.DATE_FORMAT) : null;
          leave.toDate = (leave.toDate) ? moment(leave.toDate).format(AppComponent.DATE_FORMAT) : null;
          leave.createdOn = (leave.createdOn) ? moment(leave.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          leave.updatedOn = (leave.updatedOn) ? moment(leave.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;

          if (leave.fromDateDayType != null) {
            leave.fromDateDayType = leave.fromDateDayType === 0 ? "Full Day" : "Half Day";
          }
          if (leave.toDateDayType != null) {
            leave.toDateDayType = leave.toDateDayType === 0 ? "Full Day" : "Half Day";
          }
        });
        this.allLeaveApplicationsList.forEach(leave => {
          let matchingEmployee = this.employeesFor360.find(emp => emp.empId === leave.empId);
          leave.emp360 = matchingEmployee ? matchingEmployee : {};
          let matchingEmployee2 = this.employeesFor360.find(emp => emp.empId === leave.managerId);
          leave.emp360Manager = matchingEmployee2 ? matchingEmployee2 : {};
          leave.emp360 = matchingEmployee ? matchingEmployee : {};
          let matchingEmployee3 = this.employeesFor360.find(emp => emp.empId === leave.timesheetStatusUpdatedBy);
          leave.emp360UpdatedBy = matchingEmployee3 ? matchingEmployee3 : {};
        });
      } else {
        alert(response.serviceResponse)
      }
    });
  }

  getCustomLeaveApplicationsList(queryObjList: any, template: TemplateRef<any>) {
    this.allLeaveApplicationsList = [];

    let queryObj = new Query();
    queryObj.queryList = queryObjList;
    queryObj.empId = this.currentUser.empId;
    if (queryObjList.length == 0) {
      this.getAllLeaveApplicationsList();
    } else {
      this.leaveService.customQueryForLeaveReport(queryObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allLeaveApplicationsList = response.serviceResponse;

          if (this.allLeaveApplicationsList.length == 0) {
            this.openAlertMod(this.alertModal, "No Leave Application Report found ")
          }
          this.allLeaveApplicationsList.forEach(leave => {
            leave.employmentIdAcToET = (leave.employmentIdAcToET);
            // leave.employeeType = ((leave.isApprenticeship === 'true') ? 'Apprentice' : ((leave.isConsultant === 'true') ? 'Consultant' : 'Regular')),
             leave.employeeType = (leave.isApmosysProduct === 'true') 
  ? 'Apmosys Product' 
  : ((leave.isApprenticeship === 'true') 
    ? 'Apprentice' 
    : ((leave.isConsultant === 'true') 
      ? 'Consultant' 
      : 'On roll')),

              leave.fromDate = (leave.fromDate) ? moment(leave.fromDate).format(AppComponent.DATE_FORMAT) : null;
            leave.toDate = (leave.toDate) ? moment(leave.toDate).format(AppComponent.DATE_FORMAT) : null;
            leave.createdOn = (leave.createdOn) ? moment(leave.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
            leave.updatedOn = (leave.updatedOn) ? moment(leave.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;

            if (leave.fromDateDayType != null) {
              leave.fromDateDayType = leave.fromDateDayType === 0 ? "Full Day" : "Half Day";
            }
            if (leave.toDateDayType != null) {
              leave.toDateDayType = leave.toDateDayType === 0 ? "Full Day" : "Half Day";
            }
            leave.emp360 = leave.empId;
            leave.emp360Manager = leave.managerId;
            leave.emp360UpdatedBy = leave.leaveStatusUpdatedBy;
          });

        } else {
          this.openAlertMod(template, response.serviceResponse)
        }
      });
    }
  }

  getAllTimesheetApplicationsList() {
    this.allTimesheetApplicationsList = [];
    console.log(this.allTimesheetApplicationsList , "*********************");

    this.timesheetService.timesheetReport().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allTimesheetApplicationsList = response.serviceResponse;
        this.allTimesheetApplicationsList.forEach(timesheet => {
          timesheet.employmentIdAcToET =(timesheet.employmentIdAcToET);
          timesheet.employeeType = ((timesheet.isApprenticeship === 'true') ? 'Apprentice' : ((timesheet.isConsultant === 'true') ? 'Consultant' : 'Regular')),
            timesheet.description = timesheet.description?.replaceAll('<br>', '')
          timesheet.date = (timesheet.date) ? moment(timesheet.date).format(AppComponent.DATE_FORMAT) : null;
          timesheet.officeInTime = (timesheet.officeInTime) ? moment(timesheet.officeInTime).format(AppComponent.DATETIME_FORMAT) : null;
          timesheet.officeOutTime = (timesheet.officeOutTime) ? moment(timesheet.officeOutTime).format(AppComponent.DATETIME_FORMAT) : null;
          timesheet.createdOn = (timesheet.createdOn) ? moment(timesheet.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          timesheet.updatedOn = (timesheet.updatedOn) ? moment(timesheet.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
          timesheet.emp360 = timesheet.empId;
          timesheet.emp360UpdatedBy = timesheet.timesheetStatusUpdatedBy;

        });

      } else {
        alert(response.serviceResponse)
      }
    });
  }

  
  getCustomTimesheetApplicationsList(queryObjList: any, template: TemplateRef<any>,exportAll?) {
    this.allTimesheetApplicationsList = [];
    const finalQueryList = this.isFilterApplied
    ? this.activeQueryListForFilter
    : queryObjList;

    const queryObj: any = {
    queryList: finalQueryList,
    empId: this.currentUser.empId,
    page: this.page - 1,
    size: this.itemsPerPage,
    sortColumn: this.sortColumn,
    sortDirection: this.sortDirection,
    exportAll: exportAll || false
  };

    if (!queryObj) {
      this.openAlertMod(this.alertModal, "Enter filter to featch view timesheet data");

    } else {
      this.timesheetService.customTimesheetApplicationReport(queryObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allTimesheetApplicationsList = response.serviceResponse.content;
             this.totalItems = response.serviceResponse.totalElements;
             this.itemsPerPage = queryObj.size;

          if (this.allTimesheetApplicationsList.length == 0) {
            this.openAlertMod(this.alertModal, "No Timesheet Application Report found ");
          }
          this.allTimesheetApplicationsList.forEach(timesheet => {
            timesheet.employeementId = (timesheet.employmentIdAcToET);
           timesheet.employeeType = (timesheet.isApmosysProduct === 'true') 
  ? 'Apmosys Product' 
  : ((timesheet.isApprenticeship === 'true') 
    ? 'Apprentice' 
    : ((timesheet.isConsultant === 'true') 
      ? 'Consultant' 
      : 'Regular')),
              timesheet.description = timesheet.description?.replaceAll('<br>', '')
            timesheet.date = (timesheet.date) ? moment(timesheet.date).format(AppComponent.DATE_FORMAT) : null;
            timesheet.officeInTime = (timesheet.officeInTime) ? moment(timesheet.officeInTime).format(AppComponent.DATETIME_FORMAT) : null;
            timesheet.officeOutTime = (timesheet.officeOutTime) ? moment(timesheet.officeOutTime).format(AppComponent.DATETIME_FORMAT) : null;
            timesheet.createdOn = (timesheet.createdOn) ? moment(timesheet.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
            timesheet.updatedOn = (timesheet.updatedOn) ? moment(timesheet.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
            timesheet.emp360 = timesheet.empId;
            timesheet.emp360UpdatedBy = timesheet.timesheetStatusUpdatedBy;

          });
          if (exportAll) {
            this.isTimesheetReportTable = true;
            this.exportToExcel();
          }

        } else {
          this.openAlertMod(template, response.serviceResponse);
          this.totalItems = 0;
          this.itemsPerPage = 0;
        }
      });
    }
  }

  // getAllEmployeeList() {
  //   this.queryList = [];
  //   this.allEmployeeList = [];

  //   this.employeeService.getAllEmployees().pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus == "Success") {
  //       this.allEmployeeList = response.serviceResponse;
  //       this.allEmployeeList.forEach(employee => {
  //         employee.employeementId = "A-".concat(employee.employeementId);
  //         employee.employeeType = ((employee.isApprenticeship === 'true') ? 'Apprentice' : ((employee.isConsultant === 'true') ? 'Consultant' : 'Regular')),
  //           employee.profileCompletedPercent = employee.profileCompletedPercent + "%";
  //         employee.dateOfBirth = (employee.dateOfBirth) ? moment(employee.dateOfBirth).format(AppComponent.DATE_FORMAT) : null;
  //         employee.dateOfJoining = (employee.dateOfJoining) ? moment(employee.dateOfJoining).format(AppComponent.DATE_FORMAT) : null;
  //         employee.createdOn = (employee.createdOn) ? moment(employee.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
  //         employee.updatedOn = (employee.updatedOn) ? moment(employee.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
  //         employee.emp360 = employee.empId;
  //         employee.emp360Manager = employee.managerId;
  //         employee.emp360CreatedBy = employee.createdBy;
  //         employee.emp360UpdatedBy = employee.updatedBy;
  //       });
  //     } else {
  //       alert(response.serviceResponse)
  //     }
  //   });
  // }

  getCustomEmployeesList(queryObjList: any, template: TemplateRef<any>) {
    this.allEmployeeList = [];

    let queryObj = new Query();
    queryObj.queryList = queryObjList;
    queryObj.empId = this.currentUser.empId;

    if (queryObjList == '') {
      queryObjList= [{ column: "Employment Status", operator: "!=", value: "InActive", conjunction: "" }];
    } else {
      this.employeeService.customQueryForEmployeeReport(queryObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allEmployeeList = response.serviceResponse;

          if (this.allEmployeeList.length == 0) {
            this.openAlertMod(this.alertModal, "No Data found")
          }
          this.allEmployeeList.forEach(employee => {
            employee.employeementId = "A-".concat(employee.employeementId);
            employee.employeeType = ((employee.isApprenticeship === 'true') ? 'Apprentice' : ((employee.isConsultant === 'true') ? 'Consultant' : 'Regular')),
            employee.profileCompletedPercent = employee.profileCompletedPercent + "%";
            employee.dateOfBirth = (employee.dateOfBirth) ? moment(employee.dateOfBirth).format(AppComponent.DATE_FORMAT) : null;
            employee.dateOfJoining = (employee.dateOfJoining) ? moment(employee.dateOfJoining).format(AppComponent.DATE_FORMAT) : null;
            employee.createdOn = (employee.createdOn) ? moment(employee.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
            employee.updatedOn = (employee.updatedOn) ? moment(employee.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
            employee.emp360 = employee.empId;
            employee.emp360Manager = employee.managerId;
            employee.emp360CreatedBy = employee.createdBy;
            employee.emp360UpdatedBy = employee.updatedBy;
          }); 
        } else {
          this.openAlertMod(template, response.serviceResponse)
        }
      });
    }
  }

  selectPersona(event) {
    this.employeeRole = event.target.value;
    this.showColumnList = [];
    this.hiddenColumnObj = [];
    this.getAllJobRoleList(this.employeeRole);
  }

  getAllJobRoleList(persona: any) {
    this.personaWiseJobRole = [];
    this.columns = [];
    this.mappedSubFeatureList = [];
    this.subfeatureList = [];
    this.paginateData = [];
    this.finalColumns = [];


    this.jobRoleService.getAllSubFeatureList().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.subfeatureList = response.serviceResponse;
        this.columns.push({ "field": "subFeature", "header": "Sub-Feature" });

        this.jobRoleService.getAllJobRole().pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus == "Success") {
            this.allJobRoleList = response.serviceResponse;
            this.personaWiseJobRole = this.allJobRoleList.filter((x) => x.employeeRole == persona);
            this.personaWiseJobRole.forEach(role => { this.columns.push({ "field": role.jobRoleId, "header": role.name, "department": role.departmentName }) })

            var final = [];
            this.columns.forEach(function (e) {
              var match = false;
              final.forEach(function (i) {
                if (e.department == i.department[0].department) {
                  match = true;
                }
              });
              if (!match) {
                var obj = {
                  "header": e.department,
                  "department": [e]
                }
                final.push(obj);
              } else {
                final.forEach(function (i) {
                  if (e.department == i.department[0].department) {
                    i.department.push(e);
                  }
                });
              }
            });

            this.finalColumns = final;
            this.subfeatureList.forEach(subfeature => {
              let paginateDataItem = {}

              this.columns.forEach((column, index) => {
                if (index === 0) {
                  paginateDataItem[column.field] = subfeature.subFeatureName;
                  paginateDataItem['subfeatureId'] = subfeature.subFeatureId;
                } else {
                  paginateDataItem[column.field] = false;
                }
              });
              this.paginateData.push(paginateDataItem)
            });

            this.personaWiseJobRole.forEach((role) => {
              this.employeeObj.jobRoleId = role.jobRoleId;
              this.jobRoleService.getMappedSubFeatureList(this.employeeObj).pipe(first()).subscribe((response: any) => {
                if (response.serviceStatus == "Success") {
                  const mappedSubFeatures = response.serviceResponse;
                  mappedSubFeatures.forEach(subFeature => {
                    let mappedSubFeatureData = this.paginateData.find(data => {
                      const subFeatureName = data.subFeature;
                      if (subFeatureName == subFeature.subFeatureName)
                        return data;
                    });
                    if (mappedSubFeatureData)
                      mappedSubFeatureData[role.jobRoleId] = true;
                  });
                } else {
                  console.error(response.serviceResponse);
                }
              });
            });
          } else {
            console.error(response.serviceResponse)
          }
        });
      }
    });
  }

  dropRow(event: CdkDragDrop<string[]>) {
    moveItemInArray(this.paginateData, event.previousIndex, event.currentIndex);
  }

  dropCol(event: CdkDragDrop<string[]>) {
    if (event.previousIndex != 0 && event.currentIndex !== 0) {
      moveItemInArray(this.finalColumns, event.previousIndex, event.currentIndex);
    }
  }

  dropInsideCol(event: CdkDragDrop<string[]>) {
    if (event.previousIndex != 0 && event.currentIndex !== 0) {

      this.insideCols = [];
      this.finalColumns.forEach((x) => {
        this.insideCols.push(...x.department);
      });

      moveItemInArray(this.insideCols, event.previousIndex, event.currentIndex);
    }
  }

  mouseDown(event, el: any = null) {
    el = el || event.target
    this.pos = {
      x: el.getBoundingClientRect().left - event.clientX + 'px',
      y: el.getBoundingClientRect().top - event.clientY + 'px',
      width: el.getBoundingClientRect().width + 'px'
    }
  }

  onDragRelease(event: CdkDragRelease) {
    this.renderer2.setStyle(event.source.element.nativeElement, 'margin-left', '0px')
  }

  selectCellCheckbox(element: any, isAssigned: any, subFeatureId: any) {
    const alreadyUpdatedMapping = this.updatedRoleSubFeature.findIndex((x) => x.subFeatureId == subFeatureId && x.jobRoleId == element);
    if (alreadyUpdatedMapping >= 0) {
      this.updatedRoleSubFeature.splice(alreadyUpdatedMapping, 1);
    } else {
      this.updatedRoleSubFeature.push({
        "jobRoleId": element,
        "isAssigned": isAssigned,
        "subFeatureId": subFeatureId
      });
    }
  }

  updateJobRoleSubFeatureMapping(template: TemplateRef<any>) {
    let jobRoleObj = new JobRole();
    jobRoleObj.updatedJobRoleFeatureMapping = this.updatedRoleSubFeature;

    this.jobRoleService.updateJobRoleSubFeatureMapping(jobRoleObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.updatedRoleSubFeature = [];
      } else {
        this.openAlertMod(template, response.serviceResponse);
        this.updatedRoleSubFeature = [];
      }
    });
  }

  hideColumn(column: any) {
    this.hiddenColumnObj = this.finalColumns.find(x => x.header == column);
    this.finalColumns = this.finalColumns.filter(x => x.header != column);
    this.showColumnList.push(this.hiddenColumnObj);
  }

  showColumn(selectedColumn: string) {
    let hiddenFound = this.showColumnList.find(x => x.header === selectedColumn);
    this.selectedColumnToShow = "select";

    if (hiddenFound) {
      this.finalColumns.push(hiddenFound);
      this.showColumnList = this.showColumnList.filter(x => x !== hiddenFound);
    }
  }

  toggleAccessList(event?: any) {
    if (this.isAccessFeatureMapping == false && this.isDefaultFeatureMapping == false) {
      this.isAccessFeatureMapping = true;
    }

    if (event.target.checked) {
      this.isAccessFeatureMapping = false;
      this.isDefaultFeatureMapping = true;
      this.showDefaultMappingTable();
    } else {
      this.isAccessFeatureMapping = true;
      this.isDefaultFeatureMapping = false;
    }
  }

  getDefaultMapping(template: TemplateRef<any>) {
    this.jobRoleService.getDefaultMapping().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.defaultMappingList = response.serviceResponse;
        this.defaultMappingList.forEach((object) => {
          const formattedPermissions = object.permissionList.reduce((permissions, permission) => {

            if (permission.permission == "N") {
              permission.permission = false;
            } else if (permission.permission == "Y") {
              permission.permission = true;
            }
            permissions[permission.employeeRole] = permission.permission;
            return permissions;
          }, {});

          object.permissionList = formattedPermissions;
        });

        this.processData();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  selectDefaultMapCellCheckbox(subFeatureId: any, isAssigned: any, roleName: any, subFeatureName: any) {
    const alreadyUpdatedMapping = this.updateDefaultMapping.findIndex((x) => x.subFeatureId == subFeatureId && x.employeeRole == roleName);
    if (alreadyUpdatedMapping >= 0) {
      this.updateDefaultMapping.splice(alreadyUpdatedMapping, 1);
    } else {
      this.updateDefaultMapping.push({
        "subFeatureId": subFeatureId,
        "employeeRole": roleName,
        "permission": isAssigned,
        "subFeatureName": subFeatureName
      });
    }
  }

  updateDefaultFeatureMapping(template: TemplateRef<any>) {
    let jobRoleObj = new JobRole();
    jobRoleObj.updateDefaultFeatureMapping = this.updateDefaultMapping;
    jobRoleObj.updatedBy = this.currentUser.empId;

    this.jobRoleService.updateDefaultFeatureMapping(jobRoleObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  processData() {
    const tabSeen = {};
    const featureSeen = {};

    this.defaultMappingListFilter = this.defaultMappingList.sort((a, b) => {
      const aTabName = a.tabName || '';
      const bTabName = b.tabName || '';

      const tabComp = aTabName.localeCompare(bTabName);

      if (tabComp === 0) {
        const aFeatureName = a.featureName || '';
        const bFeatureName = b.featureName || '';
        return aFeatureName.localeCompare(bFeatureName);
      }

      return tabComp;
    }).map(x => {
      const tabSpan = tabSeen[x.tabName] ? 0 :
        this.defaultMappingList.filter(y => y.tabName === x.tabName).length;

      tabSeen[x.tabName] = true;

      const featureSpan = featureSeen[x.tabName] && featureSeen[x.tabName][x.featureName] ? 0 :
        this.defaultMappingList.filter(y => y.tabName === x.tabName && y.featureName === x.featureName).length;

      featureSeen[x.tabName] = featureSeen[x.featureName] || {};
      featureSeen[x.tabName][x.featureName] = true;

      return { ...x, tabSpan, featureSpan };
    });
  }

  openFilterModal(template: TemplateRef<any>, columns: any[], title: any) {
    this.queryList = [];

    this.filterData.title = title;
    this.filterData.columns = columns;
    const today = new Date();
    const yesterday = new Date();
    yesterday.setDate(today.getDate() - 1);
    const formattedToday = formatDate(today, 'dd-MM-yyyy', 'en-US');
    const formattedYesterday = formatDate(yesterday, 'dd-MM-yyyy', 'en-US');
    if (this.leaveReportFlag == true) {
      this.queryList = [
        { column: "Employment Status", operator: "!=", value: "InActive", conjunction: "" },
        { column: "From Date", operator: ">=", value: formattedYesterday, conjunction: "" },
        { column: "To Date", operator: "<=", value: formattedToday, conjunction: "" }
      ];
    }
    else if (this.timesheetReportFlag == true) {
      this.queryList = [
        { column: "Employment Status", operator: "!=", value: "InActive", conjunction: "" },
        { column: "Date", operator: ">=", value: formattedYesterday, conjunction: "" },
        { column: "Date", operator: "<=", value: formattedToday, conjunction: "" }
      ];
    } else {
      this.queryList = [
        { column: "Employment Status", operator: "!=", value: "InActive", conjunction: "" }
      ];
    }
    this.storedDataList.forEach((data) => {
      if (data.filterName == title) {
        data.queryList.forEach((queryObj) => {
          // if (queryObj.column == "Employee Id" && !queryObj.value.includes("A-")) {
          //   queryObj.value = "A-".concat(queryObj.value);
          // }

          if (queryObj.column == 'From Date' || queryObj.column == 'To Date' || queryObj.column == 'Date' || queryObj.column == 'Date Of Joining') {
            queryObj.value = (queryObj.value) ? moment(queryObj.value).format("DD-MM-YYYY") : '';
          } else if (queryObj.column == 'Created On' || queryObj.column == 'Updated On') {
            queryObj.value = (queryObj.value) ? moment(queryObj.value).format('DD-MM-YYYY HH:mm:ss') : '';
          }
        });
        this.queryList = data.queryList;
      }
    });

    this.filterData.queryList = JSON.stringify(this.queryList);

    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
  }

  onFilterSubmit(emittedArray: any, template: TemplateRef<any>) {
    if (emittedArray[0].length != 0) {
      this.queryList = JSON.parse(JSON.stringify(emittedArray[0]));
      this.cancelRequest();

      emittedArray[1].forEach((object) => {
        if (Object.keys(object).length !== 0) {
          if (this.storedDataList.find((x) => x.filterName == object.filterName)) {
            this.storedDataList = this.storedDataList.map(arr1 => emittedArray[1].find(arr2 => arr2.filterName === arr1.filterName) || arr1);
          } else {
            this.storedDataList.push(object);
          }
        }
      });


      emittedArray[0].forEach(query => {
        if (query.column == 'From Date' || query.column == 'To Date' || query.column == 'Date' || query.column == 'Date Of Joining') {
          query.value = (query.value) ? moment(query.value, "DD-MM-YYYY").format('YYYY-MM-DD') : '';
        } else if (query.column == 'Created On' || query.column == 'Updated On') {
          query.value = (query.value) ? moment(query.value, "DD-MM-YYYY").format('YYYY-MM-DD HH:mm:ss') : '';
        }

        // if (query.column == 'Employee Id') {
        //   query.value = query.value.split("-")[1];
        // }
      });

      if (this.filterData.title == 'Filter Leave Report') {
        this.getCustomLeaveApplicationsList(emittedArray[0], template);
      }
      if (this.filterData.title == 'Filter Employee Report') {
        this.getCustomEmployeesList(emittedArray[0], template);
      }
      if (this.filterData.title == 'Filter Timesheet Report') {
        this.isFilterApplied=true;
        this.activeQueryListForFilter=emittedArray[0];
        this.getCustomTimesheetApplicationsList(emittedArray[0], template);
      }
    } else {
      let clearedFilter = this.storedDataList.find((filter) => filter.filterName == emittedArray[1]);
      this.storedDataList.splice(clearedFilter);

      if (emittedArray[1] == 'Filter Leave Report') {
        this.showLeaveReportTable();
      }
      if (emittedArray[1] == 'Filter Timesheet Report') {
        this.showTimesheetReportTable();
      }
      if (emittedArray[1] == 'Filter Employee Report') {
        this.showEmployeeReportTable();
      }
    }
  }

  onLeaveTimesheetReportClick(): void {
    const currentDate = new Date();
    const firstDateOfMonth = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1);
    this.startDate = moment(firstDateOfMonth).format('YYYY-MM-DD');
    this.endDate = moment(currentDate).format('YYYY-MM-DD');
    this.getAllLeaveTimesheets(this.alertTemplate);

  }
  
  
  

  getAllLeaveTimesheets(template?: TemplateRef<any>) {
    this.allLeaveTimesheets = [];

    if (this.endDate) {
      if (!this.validationService.validateNullUndefinedEmptyString(this.startDate)) {
        this.alertMessage = "Please enter Start Date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (!this.validationService.validateNullUndefinedEmptyString(this.endDate)) {
        this.alertMessage = "Please enter End Date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    } else {
      return;
    }

    let timesheetObj = new Timesheet();
    timesheetObj.startDate = this.startDate;
    timesheetObj.endDate = this.endDate;
    timesheetObj.currentUser = this.currentUser.empId;
    timesheetObj.page = this.page - 1;
    timesheetObj.size = this.itemsPerPage;
    timesheetObj.sortByForTimesheetLeaveReport = this.sortColumn.length > 0 ? this.sortColumn : ['date'];
    timesheetObj.sortDirection = (this.sortDirection === 'asc' || this.sortDirection === 'desc') ? this.sortDirection as 'asc' | 'desc' : 'desc';
    timesheetObj.filters = this.filters || {};
    if (this.exportAll) {
      timesheetObj.exportAll = true;
    }


    this.timesheetService.getAllLeaveTimesheetsWithoutLeaveApplication(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        const timesheetList = response.serviceResponse?.content || [];

        this.allLeaveTimesheets = timesheetList;

        if (this.exportAll) {
          this.isLeaveTimesheetReportTable = true;
          this.exportToExcel();
          this.exportAll = false;
          return;
        }

        this.totalItems = response.serviceResponse.totalElements;

        for (let x of this.allLeaveTimesheets) {
          // Determine readable employee type
          x.employeeType = (x.isApmosysProduct === 'true')
            ? 'Apmosys Product'
            : ((x.isApprenticeship === 'true')
              ? 'Apprentice'
              : ((x.isConsultant === 'true')
                ? 'Consultant'
                : 'Regular'));

          const prefix = this.getEmpIdPrefix(x.employeeType);
          x.employmentIdAcToET = prefix.concat(String(x.employeementId));

          x.date = (x.date) ? moment(x.date).format(AppComponent.DATE_FORMAT) : null;
          x.createdOn = (x.createdOn) ? moment(x.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          x.updatedOn = (x.updatedOn) ? moment(x.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
          x.emp360 = x.empId;
          x.emp360Manager = x.managerId;
          x.emp360UpdatedBy = x.timesheetStatusUpdatedBy;
        }

      } else {
        console.error(response.serviceResponse);
        this.openAlertForTimesheetLeaveReport(response.serviceResponse);
        this.totalItems = 0;


      }
    });
  }

  getEmpIdPrefix(employeeType: string): string {
    switch (employeeType) {
      case 'Consultant':
        return 'CS-';
      case 'Apmosys Product':
        return 'AP-';
      default:
        return 'A-';
    }
  }


  getCustomQueryData(template: TemplateRef<any>) {
    this.customQuery = this.customQuery?.trim().replace(/\s{2,}/g, ' ');
    if (!this.validationService.validateNullUndefinedEmptyString(this.customQuery)) {
      this.alertMessage = "Please enter custom query !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    let queryObj = new Query();
    queryObj.customQuery = this.customQuery;

    this.utilityService.getCustomQueryData(queryObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        let responseData = response.serviceResponse;

        if (responseData) {
          let exportData = responseData.map((dataArr) => {
            let dataObj = {};
            dataArr.forEach((data, index) => {
              dataObj[index] = data;
            });

            return dataObj
          });

          const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(exportData, { skipHeader: true });
          const book: XLSX.WorkBook = XLSX.utils.book_new();
          XLSX.utils.book_append_sheet(book, worksheet, 'Sheet1');
          XLSX.writeFile(book, "CustomQueryData.xlsx");
        } else {
          this.alertMessage = "Please Enter Valid Query !!";
          this.openAlertMod(template, this.alertMessage);
        }

      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  itemsPerPageForClientProject = 10; 

  page = 1;
  itemsPerPage = 5;
  page1 = 1;
  handlePageChange(event) {
    this.page = event;
  }

handlePageChange1(event) {
    this.page1 = event;
  }

  handlePageChangeForTimesheetLeaveReport(event: number) {
    this.page = event;
    this.getAllLeaveTimesheets();
  }
 
  
  handlePageChangeForViweTimesheetReport(event: number) {
    this.page = event;
    this.getCustomTimesheetApplicationsList(this.activeQueryList,this.alertTemplate);
  }

  sortDataForTimesheetLeaveReport(sort: Sort) {
    if (sort.active) {
      let sortParams = sort.active.split("|");
      const frontendSortKey = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction || 'asc';

      const sortFieldMap: { [key: string]: string } = {
        employmentIdAcToET: "e.employeementId",
        employeeName: "e.name",
        employeeType: "e.id",
        managerName: "mgr.name",
        departmentName: "d.name",
        createdOn: "commonProperty.createdOn",
        updatedOn: "commonProperty.updatedOn",
        timesheetStatusUpdatedByName: "s.name",
        status: "status",
        dayType: "dayType",
        description: "description",
        date: "date"
      };

      const mappedSortField = sortFieldMap[frontendSortKey] || "t.date";

      this.sortColumn = [mappedSortField];

      this.getAllLeaveTimesheets();
    }
  }


  onSearchForTimesheetLeaveReport(searchData: any) {
    this.filters = searchData;
    this.page = 1;
    this.getAllLeaveTimesheets();
  }



  get paginatedProjectList(): any[] {
    return this.projectList;
  }

  page2 = 1;	
  handlePageChange2(event: number) {	
    this.page2 = event;	
  }

  getHierarchicalSrNo(pIndex: number, tIndex: number, eIndex: number): string {
    const globalProjectIndex = this.getGlobalProjectIndex(pIndex) + 1;
    return `${globalProjectIndex}.${tIndex + 1}.${eIndex + 1}`;
  }

  getGlobalProjectIndex(localPIndex: number): number {
    const itemsPerPage = 5; // Match with HTML
    return (this.page - 1) * itemsPerPage + localPIndex;
  }

  exportToExcel(): void {


    if (this.isLeaveReportTable == true) {
      this.excelName = 'leaveReport.xlsx';

      const onlySpecificDataArr = this.allLeaveApplicationsList.map(
        x => ({
          "Employee Id": x.employmentIdAcToET,
          "Employee Type": x.employeeType,
          "Employee Name": x.employeeName,
          "Leave Type": x.leaveType,
          "From Date": x.fromDate,
          "To Date": x.toDate,
          "From Date Day Type": x.fromDateDayType,
          "To Date Day Type": x.toDateDayType,
          "No Of Days": x.noOfDays,
          "Reason": x.reason,
          "Status": x.status,
          "Manager Name": x.managerName,
          "Department Name": x.departmentName,
          "Created On": x.createdOn,
          "Updated On": x.updatedOn,
          "Updated By": x.leaveStatusUpdatedByName
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
    }

    if (this.isTimesheetReportTable == true) {
      this.excelName = 'timesheetReport.xlsx';

      const onlySpecificDataArr = this.allTimesheetApplicationsList.map(
        x => ({
          "Employeement Id": x.employeementId,
          "Employee Type": x.employeeType,
          "Employee Name": x.employeeName,
          "date": x.date,
          "dayType": x.dayType,
          "description": x.description?.replaceAll('<br>', ' \n'),
          "status": x.status,
          "totalWorkingHours": x.totalWorkingHours,
          "Leave Type": x.leaveType,
          "createdOn": x.createdOn,
          "updatedOn": x.updatedOn,
          "timesheetStatusUpdatedByName": x.timesheetStatusUpdatedByName
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
    }

    if (this.isEmployeeReportTable == true) {

      if (!this.showDetails) {
        this.excelName = 'EmployeeReport.xlsx';

        const onlySpecificDataArr = this.allEmployeeList.map(
          x => ({
            "Employee Id": x.employeementId,
            "Employee Type": x.employeeType,
            "Full Name": x.name,
            "Email Id": x.email,
            "Employment Status": x.employmentstatus,
            "Date Of Joining": x.dateOfJoining,
            "Department": x.departmentName,
            "Billable": x.billable,
            "Billable Type": x.billableType,
            "Client Name": x.clientName,
            "Team Name": x.teamName,
            "Project Name": x.projectName,
            "Po No": x.poNo,
            "Po Start Date": x.poStartDate,
            "Po End Date": x.poEndDate,
            "Po Project Type": x.poProjectType,
            "createdBy": x.createdBy,
            "createdOn": x.createdOn,
            "Proile Completion Perecentage": x.profileCompletedPercent,
          })
        )
        this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName);
      } else {
        if (this.employeeReportObj.category === 'Employee') {
          this.excelName = 'EmployeeDetailedReport.xlsx';
          if (this.changeTable) {
            console.log(this.employeeList);
            const onlySpecificDataArr = this.employeeList.map(
              x => ({
                "Employee Id": x.employeementId,
                "Full Name": x.name,
                "Department": x.departmentName,
                "Job Role": x.jobRole,
                "Manager": x.managerName,
                "Mobile Number": x.mobileNo,
                "Email Id": x.email,
                "Employment Status": x.employmentstatus,
                "Billable": x.billable,
                "Billable Type": x.billableType,
                "Team Name": x.teamName,
                "Project Name": x.projectName,
                "Po No": x.poNo,
                "Po Type": x.poProjectType,
                "Po Start Date": x.poStartDate,
                "Po End Date": x.poEndDate,
                "Effective Start Date": x.effectiveStartDate,
                "Effective End Date": x.effectiveEndDate,
                "Client Name": x.clientName,
                "Client Location": x.clientLocation,
                "Work Location": x.workLocation,
                "Experience": x.totalExperience,
                "Default Project Assigned": x.primaryProjectName,
              })
            )
            this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName);
          } else {
            const onlySpecificDataArr = this.employeeList.map(
              x => ({
                "Employee Id": x.employeementId,
                "Full Name": x.name,
                "Department": x.departmentName,
                "Job Role": x.jobRole,
                "Manager": x.managerName,
                "Mobile Number": x.mobileNo,
                "Email Id": x.email,
                "Employment Status": x.employmentstatus,
                "Billable": x.billable,
                "Billable Type": x.billableType,
                "Team Name": x.teamName,
                "Project Name": x.projectName,
                "Po No": x.poNo,
                "Po Type": x.poType,
                "Po Start Date": x.poStartDate,
                "Po End Date": x.poEndDate,
                "Effective Start Date": x.effectiveStartDate,
                "Effective End Date": x.effectiveEndDate,
                "Client Name": x.clientName,
                "Client Location": x.clientLocation,
                "Work Location": x.workLocation,
                "Experience": x.totalExperience,

              })
            )
            this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName);
          }
        } else {
          this.excelName = 'ProjectDetailedReport.xlsx';

          const flatList = [];
          this.projectList.forEach(project => {
            project.teamDetails.forEach(team => {
              team.mappedEmployeeDetails.forEach(emp => {
                flatList.push({
                  "Project Name": project.projectName,
                  "Project Manager": project.projectManager,
                  "Apmosys RM": project.apmosysRM || '—',
                  "Client RM": project.clientRM || '—',
                  "PO Start Date": project.poStartDate,
                  "PO End Date": project.poEndDate,
                  "PO No": project.poNo,
                  "PO Type": project.poProjectType,
                  "Team Name": team.teamName,
                  "Employee Name": emp.employeeName,
                  "Job Role": emp.jobRole,
                  "Department": emp.deptName,
                  "Mobile No": emp.mobileNo,
                  "Email": emp.email,
                  "Billable": emp.billable,
                  "Billable Type": emp.billableType,
                  "Effective Start Date": emp.effectiveStartDate,
                });
              });
            });
          });

          this.exportExcelService.exportTableDataToExcel(flatList, this.excelName);
        }
      }

    }

    if (this.isAccessControlListTable == true) {
      this.excelName = `${this.employeeRole}-ACLReport.xlsx`;

      let columnsData = [];
      let fieldData = [];

      this.finalColumns.map(column => {
        let headers = column.department.map(field => field);
        columnsData.push(...headers);
      });

      let columns = columnsData.map(column => column.field);
      let departments = {};
      let designations = {};

      columns.forEach(column => {
        let data = columnsData.find(cd => cd.field == column);
        if (data.department) {
          if (Object.values(departments).includes(data.department)) {
            departments[column] = "";
          } else {
            departments[column] = data.department;
          }
        } else {
          departments[column] = "";
        }

        if (data) {
          designations[column] = data.header;
        }
      });

      fieldData.push(departments, designations, ...this.paginateData);

      const onlySpecificDataArr = fieldData.map(response => {
        let data = {};
        columns.forEach((header, index) => {
          data[index] = "" + response[header]
        });
        return data;
      });

      const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(onlySpecificDataArr, { skipHeader: true });
      const book: XLSX.WorkBook = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(book, worksheet, 'Sheet1');

      XLSX.writeFile(book, this.excelName);
    }

    if (this.isLeaveTimesheetReportTable == true) {
      this.excelName = 'LeaveTimesheetReport.xlsx';

      const onlySpecificDataArr = this.allLeaveTimesheets.map(
        x => ({
          "Employeement Id": x.employeementId,
          "Employee Type": x.employeeType,
          "Employee Name": x.employeeName,
          "date": x.date,
          "dayType": x.dayType,
          "description": x.description?.replaceAll('<br>', ' \n'),
          "status": x.status,
          "Manager Name": x.managerName,
          "Department Name": x.departmentName,
          "createdOn": x.createdOn,
          "updatedOn": x.updatedOn,
          "timesheetStatusUpdatedByName": x.timesheetStatusUpdatedByName
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
    }

  }
  exportToExcelForViewTimesheetReport():void{
  this.getCustomTimesheetApplicationsList(this.activeQueryList,this.alertTemplate,true);
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.close();
  }

  sortData(sort: Sort) {
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

  sortDataForViewTimesheet(sort: Sort) {
  if (sort.active) {
    const [column, type] = sort.active.split('|');
    this.sortColumn = [column];
    this.sortDirection = sort.direction || 'asc';
    this.page = 1;
    this.getCustomTimesheetApplicationsList(this.activeQueryList, this.alertTemplate);
  }
}


  toggleSearch() {
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
    }
  }

  onSearch(searchData) {
    this.filters = searchData;
  }


  // Add this helper method in your component
private mapFieldToBackendColumn(field: string): string {
  const mapping: { [key: string]: string } = {
    employeementId: "Employee Id",
    employeeType: "Employee Type",
    employeeName: "Full Name",
    date: "Date",
    dayType: "Day Type",
    description: "Description",
    status: "Status",
    totalWorkingHours: "Total Working Hours",
    officeInTime: "Office In Time",
    officeOutTime: "Office Out Time",
    totalWorkingOfficeHours: "Total Office Working Hours",
    leaveType: "Leave Type",
    createdOn: "Created On",
    updatedOn: "Updated On",
    timesheetStatusUpdatedByName: "Updated By",
    department: "Department",
    // You can keep adding more fields as needed
  };

  return mapping[field] || field; // fallback to same name if not mapped
}

onSearchForViewTimesheet(searchData: any) {
  this.filters = searchData;
  this.page = 1;

  const currentDate = new Date().toISOString().split('T')[0];
  const oneMonthBeforeDate = new Date();
  oneMonthBeforeDate.setMonth(oneMonthBeforeDate.getMonth() - 1);
  const formattedOneMonthBeforeDate = oneMonthBeforeDate.toISOString().split('T')[0];

  // Base filters
  this.activeQueryList = [
    { column: "Employment Status", operator: "!=", value: "InActive", conjunction: "AND" },
    { column: "Date", operator: ">=", value: formattedOneMonthBeforeDate, conjunction: "AND" },
    { column: "Date", operator: "<=", value: currentDate, conjunction: "" }
  ];

  // Dynamic user filters
  const dynamicConditions = Object.keys(this.filters)
    .filter(key => this.filters[key])
    .map((key, index, arr) => {
      let value = this.filters[key];
      const column = this.mapFieldToBackendColumn(key);

      // ✅ Handle date fields flexibly
      if (["date", "createdOn", "updatedOn"].includes(key)) {
        value = this.normalizeDate(value);
      }

      return {
        column,
        value,
        operator: 'LIKE',
        conjunction: index === arr.length - 1 ? '' : 'AND'
      };
    });

  if (dynamicConditions.length > 0) {
    this.activeQueryList[this.activeQueryList.length - 1].conjunction = "AND";
    this.activeQueryList.push(...dynamicConditions);
  }

  this.getCustomTimesheetApplicationsList(this.activeQueryList, this.alertTemplate);
}
private normalizeDate(value: string): string {
  if (!value) return value;

  // If already in yyyy-MM-dd format (ISO)
  if (/^\d{4}-\d{2}-\d{2}$/.test(value)) {
    return value;
  }

  // If in dd-MM-yyyy format, convert it
  if (/^\d{2}-\d{2}-\d{4}$/.test(value)) {
    const [day, month, year] = value.split('-');
    return `${year}-${month}-${day}`;
  }

  // Try parsing via Date object for any other case
  const parsed = new Date(value);
  if (!isNaN(parsed.getTime())) {
    return parsed.toISOString().split('T')[0];
  }

  // Fallback if invalid
  return value;
}




  onSearchh(updatedFilters: any) {
    this.filters = updatedFilters;
    this.page = 1;
  }

  get filteredProjectList() {
    if (!this.filters || Object.keys(this.filters).length === 0) {
      return this.projectList;
    }

    const filtered = this.projectList.map(project => {
      const filteredTeams = project.teamDetails.map(team => {
        const filteredEmployees = team.mappedEmployeeDetails.filter(emp =>
          Object.keys(this.filters).every(key => {
            const searchValue = this.filters[key]?.toLowerCase() || '';
            const empValue = emp[key]?.toString()?.toLowerCase() || '';
            const teamValue = team[key]?.toString()?.toLowerCase() || '';
            const projValue = project[key]?.toString()?.toLowerCase() || '';

            return empValue.includes(searchValue) ||
              teamValue.includes(searchValue) ||
              projValue.includes(searchValue);
          })
        );

        return {
          ...team,
          mappedEmployeeDetails: filteredEmployees
        };
      }).filter(team => team.mappedEmployeeDetails.length > 0);

      return {
        ...project,
        teamDetails: filteredTeams
      };
    }).filter(project => project.teamDetails.length > 0);

    return filtered;
  }

  getAllEmployeesReportByProjectType() {
    this.allEmployee = [];

    this.employeeService.getAllEmployeesReportByProjectType().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allEmployee = response.serviceResponse;
        this.filterEmployees();
        console.log("d nbdfh", this.allEmployee);
      } else {

      }
    });
  }

  goBack() {
    if (this.returnUrl) {
      this.router.navigateByUrl(this.returnUrl);
    } else {
      this.location.back(); // fallback
    }
  }

  onEmployeeSelected(event: any) {
    const selectedEmp = event.option.value;
    if (selectedEmp) {
      this.selectedEmpId = selectedEmp.empId;
      console.log('Selected Employee ID:', this.selectedEmpId);
    }
  }

  displayEmployee(emp: any): string {
    if (typeof emp === 'string') {
      return emp;
    }
    return emp && emp.name ? emp.name : '';
  }

  isEmployeeInList(list: any[]): boolean {
    return list?.some(emp => emp.empId === this.selectedEmpId);
  }

  getEmployeeByNameAndEmpld() {
    this.employeeService.getEmployeeByNameAndEmpld().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === 'Success') {
        this.filteredEmployees2 = response.serviceResponse;
        // console.log("employee list", this.employeeList);
        // this.filteredEmployees2 = this.employeeList;
        this.employeeCtrl.setValue('');
      } else {
      }
    });
  }

  filterEmployees2(searchText: string) {
    const filterValue = searchText.toLowerCase();

    return this.employeeList.filter(emp => {
      if (!emp) {
        return false;
      }

      const nameMatch = emp.name && emp.name.toLowerCase().includes(filterValue);
      const idMatch = emp.empId && emp.empId.toString().toLowerCase().includes(filterValue);
      return nameMatch || idMatch;
    });
  }

  openSummaryModal(template: TemplateRef<any>, selectedEmpId: any) {
    this.summaryModalRef = this.modalService.open(template, { modalDialogClass: 'modal-lg' });
    this.selectedEmpId = selectedEmpId;

    this.getProjectTimesheetSummaryData();
  }

  closeSummaryModal() {
    this.summaryModalRef.close();
  }

  getProjectTimesheetSummaryData() {
    if (!this.selectedEmpId) {
      this.openAlertMod(this.alertTemplate, "Cannot fetch summary. User information is missing.");
      console.error("Current user or empId is not available.");
      if (this.summaryModalRef) {
        document.getElementById('projectTimesheetSummaryChart').innerHTML = '<p class="text-center text-danger">Could not load data: User not identified.</p>';
      }
      return;
    }

    const resourceManagementDTO = {
      empId: this.selectedEmpId
    };

    console.log("================================", resourceManagementDTO);
    this.resourceManagementService.getProjectTimesheetSummary(resourceManagementDTO).pipe(first()).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === "Success") {
          this.projectSummaryData = response.serviceResponse;

          if (this.projectSummaryData && this.projectSummaryData.length > 0) {
            this.processProjectSummaryData(this.projectSummaryData);
          } else {
            document.getElementById('projectTimesheetSummaryChart').innerHTML = '<p class="text-center">No timesheet summary data found for your projects.</p>';
          }
        } else {
          console.error(response.serviceResponse);
          document.getElementById('projectTimesheetSummaryChart').innerHTML = '<p class="text-center text-danger">Error:No timesheets filled till date</p>';
        }
      },
      error: (err) => {
        this.openAlertMod(this.alertTemplate, "An error occurred while fetching summary data.");
        console.error(err);
        document.getElementById('projectTimesheetSummaryChart').innerHTML = '<p class="text-center text-danger">A server error occurred. Please try again later.</p>';
      }
    });
  }

  processProjectSummaryData(summaryData: any[]) {
    const categories = [];
    const seriesData = [];

    const sortedData = summaryData
      .sort((a, b) => b.totalTimesheetsFilled - a.totalTimesheetsFilled)
      .slice(0, 20);

    sortedData.forEach(item => {
      categories.push(item.projectName);
      seriesData.push(item.totalTimesheetsFilled);
    });

    const chartData = [{
      name: 'Timesheets Filled',
      data: seriesData,
      color: '#0275d8'
    }];

    this.renderColumnChart(
      'Top 20 Projects by Timesheets Filled',
      'projectTimesheetSummaryChart',
      chartData,
      categories
    );
  }

  renderColumnChart(chartName: any, chartId: any, chartData: any, categories: any) {
    Highcharts.chart(chartId, {
      chart: {
        type: 'column',
      },
      title: {
        text: chartName,
        style: {
          fontWeight: 'bold',
          color: '#000000'
        }
      },
      xAxis: {
        categories: categories,
        title: {
          text: 'Projects'
        },
        labels: {
          rotation: -45,
          style: {
            fontSize: '11px',
            fontFamily: 'Verdana, sans-serif'
          }
        }
      },
      yAxis: {
        min: 0,
        title: {
          text: 'Total Timesheets Filled',
          align: 'high'
        },
        labels: {
          overflow: 'justify'
        }
      },
      tooltip: {
        valueSuffix: ' timesheets'
      },
      plotOptions: {
        column: {
          dataLabels: {
            enabled: true,
            format: '{y}',
            style: {
              fontSize: '10px',
            }
          }
        }
      },
      credits: {
        enabled: false,
      },
      legend: {
        enabled: false
      },
      series: chartData
    });
  }

  // InActivePoCounts: { type: string; count: String }[] = [
  //   { type: '07 Days', count: this.total7Days },
  //   { type: '30 Days', count: this.total30Days },
  //   { type: '90 Days', count: this.total90Days },
  //   { type: '180 Days', count: this.total180Days },
  //   { type: '365 Days', count: this.total1Year },
  //   { type: 'Overall', count: this.totalAll },
  // ];

  // onInfoClickModel(template: TemplateRef<any>, details:any): void {
  //    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
  //   console.log('TNM stands for Time and Materials billing model.');
  // }

//   onInfoClickModel(box: any,template: TemplateRef<any>, noOfDays: number | null): void {
//     this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
//     const tabName = this.employeeReportObj.category;
//     const selectedMainFlag = this.selectedFlag[this.activeBox] || 'Default';
//     const key = `${box}.${selectedMainFlag}`;
//     const isActiveBox = box === this.activeBox;
//     const category = this.selectedTab[this.activeBox];
//     this.employeeReportObj.billableType = null;
//     this.employeeReportObj.days = noOfDays;
//     this.employeeReportObj.tabName = tabName ;
    
    
//     console.log("Fetching with noOfDays:", this.employeeReportObj);
  
//     this.employeeService.fetchInactivePOListOfEmployee(this.employeeReportObj)
//         .pipe(first())
//         .subscribe((response: any) => {
//           this.allInactivePOListOfEmployee = response.serviceResponse;
//           console.log("Service Response ", this.allInactivePOListOfEmployee);

//         if (response.serviceStatus == "Success") {
//           this.allInactivePOListOfEmployee = response.serviceResponse;
//           console.log("Response for", noOfDays, "days:", response);
//         } else {
  
//         }
  
    
//   })
// }

onInfoClickModel(box: any, defaultTemplate: TemplateRef<any>, dateRange: string | null, projectTemplate: TemplateRef<any>): void {
  const category = this.selectedTab[this.activeBox];

  if (category === 'Project') {
    this.modalRef = this.modalService.open(projectTemplate, { modalDialogClass: 'modal-xl' });
  } else {
    this.modalRef = this.modalService.open(defaultTemplate, { modalDialogClass: 'modal-xl' });
  }

  this.employeeReportObj.billableType = null;
   this.employeeReportObj.dateRange = dateRange;
  this.employeeReportObj.tabName = this.employeeReportObj.category;
  this.employeeReportObj.hideMaternityLeaveEmps = this.hideMaternityLeaveEmps;

  this.employeeService.fetchInactivePOListOfEmployee(this.employeeReportObj)
    .pipe(first())
    .subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        this.allInactivePOListOfEmployee = response.serviceResponse;
      } else {
        this.allInactivePOListOfEmployee = [];
      }
    });
}

onInfoClickModel1(box: any, defaultTemplate: TemplateRef<any>, dateRange: string | null, projectTemplate: TemplateRef<any>): void {
  const category = this.selectedTab[this.activeBox];

  if (category === 'Project') {
    this.modalRef = this.modalService.open(projectTemplate, { modalDialogClass: 'modal-xl' });
  } else {
    this.modalRef = this.modalService.open(defaultTemplate, { modalDialogClass: 'modal-xl' });
  }

  this.employeeReportObj.billableType = null;
   this.employeeReportObj.dateRange = dateRange;
  this.employeeReportObj.tabName = this.employeeReportObj.category;
  this.employeeReportObj.hideMaternityLeaveEmps = this.hideMaternityLeaveEmps;

  this.employeeService.fetchactivePOListOfEmployee(this.employeeReportObj)
    .pipe(first())
    .subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        this.allactivePOListOfEmployee = response.serviceResponse;
      } else {
        this.allactivePOListOfEmployee = [];
      }
    });
}


  

  toggleInfo1(box: any) {
    this.visibleInfo =  !this.visibleInfo ;
    this.inActiveBoxinfo = box;

  }

  toggleInfo2(box: any) {
    this.visibleInfo1 = !this.visibleInfo1;
    this.activeBoxinfo = box;
  }

  // getInActivePoCount(box: any): void {
  //   const selectedMainFlag = this.selectedFlag[this.activeBox] || 'Default';
  //   const key = `${box}.${selectedMainFlag}`;
  //   const isActiveBox = box === this.activeBox;
  //   const category = this.selectedTab[this.activeBox];
  
  //   console.log("Box Type:", box);
  //   console.log("Key:", key);
  //   console.log("Category:", category);
  //   console.log("Is Active Box:", isActiveBox);
  
  //   this.employeeService.fetchInactivePOCounts(this.employeeReportObj)
  //     .pipe(first())
  //     .subscribe((response: any) => {
  //       if (response.serviceStatus === "Success" && response.serviceResponse) {
  //         const res = response.serviceResponse;
  
  //         this.totalEmployeeInActivePoCount = res;
  
  //         this.total7Days = res.totalEmpPerProjectTypeLast7days || '0';
  //         this.total30Days = res.totalEmpPerProjectTypeLast30days || '0';
  //         this.total90Days = res.totalEmpPerProjectTypeLast90days || '0';
  //         this.total180Days = res.totalEmpPerProjectTypeLast180days || '0';
  //         this.total1Year = res.totalEmpPerProjectTypeLast1Year || '0';
  //         this.totalAll = res.totalEmpPerProjectTypeTotal || '0';
  
  //         console.log("Inactive PO Count:", this.totalEmployeeInActivePoCount);
  //       } else {
  //         console.error("API Error:", response.serviceError || "Unknown error");
  
          
  //         this.totalEmployeeInActivePoCount = {};
  //         this.total7Days = '0';
  //         this.total30Days = '0';
  //         this.total90Days = '0';
  //         this.total180Days = '0';
  //         this.total1Year = '0';
  //         this.totalAll = '0';
  //       }
  //     });
  // }
  
  // getInActivePoCount(box: any): void {
  //   const selectedMainFlag = this.selectedFlag[this.activeBox] || 'Default';
  //   const key = `${box}.${selectedMainFlag}`;
  //   const isActiveBox = box === this.activeBox;
  //   const category = this.selectedTab[this.activeBox];
  //   const tabName = this.employeeReportObj.category;
  
  //   console.log("Box Type:", box);
  //   console.log("Key:", key);
  //   console.log("Category:", category);
  //   console.log("Is Active Box:", isActiveBox);
  //   this.employeeReportObj.tabName = tabName ;
  
  //   this.employeeService.fetchInactivePOCounts(this.employeeReportObj)
  //     .pipe(first())
  //     .subscribe((response: any) => {
  //       if (response.serviceStatus === "Success" && response.serviceResponse) {
  //         const res = response.serviceResponse;
  
  //         this.totalEmployeeInActivePoCount = res;

  //         console.log("COunt  :::::::::",this.totalEmployeeInActivePoCount);
  
  //         const counts = this.totalEmployeeInActivePoCount[0];

  //         // this.InActivePoCounts = [
  //         //   { type: 'Expired in last 7 Days', count: counts.totalEmpPerProjectTypeLast7days ?? '0' },
  //         //   { type: 'Expired in last 30 Days', count: counts.totalEmpPerProjectTypeLast30days ?? '0' },
  //         //   { type: 'Expired in last 90 Days', count: counts.totalEmpPerProjectTypeLast90days ?? '0' },
  //         //   { type: 'Expired in last 180 Days', count: counts.totalEmpPerProjectTypeLast180days ?? '0' },
  //         //   { type: 'Expired in last 1 Year', count: counts.totalEmpPerProjectTypeLast1Year ?? '0' },
  //         //   { type: 'Overall Expired', count: counts.totalEmpPerProjectTypeTotal ?? '0' }
  //         // ];

  //         this.InActivePoCounts = [
  //           { type: 'Expired in last 7 Days', count: counts.inactiveCountWithin1Month ?? '0', noOfDays: 7 },
  //           { type: 'Expired in last 30 Days', count: counts.totalEmpPerProjectTypeLast30days ?? '0', noOfDays: 30 },
  //           { type: 'Expired in last 90 Days', count: counts.totalEmpPerProjectTypeLast90days ?? '0', noOfDays: 90 },
  //           { type: 'Expired in last 180 Days', count: counts.totalEmpPerProjectTypeLast180days ?? '0', noOfDays: 180 },
  //           { type: 'Expired in last 1 Year', count: counts.totalEmpPerProjectTypeLast1Year ?? '0', noOfDays: 365 },
  //           { type: 'Overall Expired', count: counts.totalEmpPerProjectTypeTotal ?? '0', noOfDays: null }
  //         ] as { type: string; count: string; noOfDays: number | null }[];
          

          
  
  //         console.log("Inactive PO Count:", counts.totalEmpPerProjectTypeTotal);
  //       } else {
  //         console.error("API Error:", response.serviceError || "Unknown error");
  
  //         this.totalEmployeeInActivePoCount = {};
  //         this.total7Days = '0';
  //         this.total30Days = '0';
  //         this.total90Days = '0';
  //         this.total180Days = '0';
  //         this.total1Year = '0';
  //         this.totalAll = '0';
  
  //         // this.InActivePoCounts = [
  //         //   { type: 'Expired in last 7 Days', count: '0' },
  //         //   { type: 'Expired in last 30 Days', count: '0' },
  //         //   { type: 'Expired in last 90 Days', count: '0' },
  //         //   { type: 'Expired in last 180 Days', count: '0' },
  //         //   { type: 'Expired in last 1 Year', count: '0' },
  //         //   { type: 'Overall Expired', count: '0' },
  //         // ];
  //       }
  //     });
  // }

  // getInActivePoCount(box: any): void {
  //   const selectedMainFlag = this.selectedFlag[this.activeBox] || 'Default';
  //   const key = `${box}.${selectedMainFlag}`;
  //   const isActiveBox = box === this.activeBox;
  //   const category = this.selectedTab[this.activeBox];
  //   const tabName = this.employeeReportObj.category;
  
  //   console.log("Box Type:", box);
  //   console.log("Key:", key);
  //   console.log("Category:", category);
  //   console.log("Is Active Box:", isActiveBox);
  //   this.employeeReportObj.tabName = tabName ;
  
  //   this.employeeService.fetchInactivePOCounts(this.employeeReportObj)
  //     .pipe(first())
  //     .subscribe((response: any) => {
  //       if (response.serviceStatus === "Success" && response.serviceResponse) {
  //         const res = response.serviceResponse;
  
  //         this.totalEmployeeInActivePoCount = res;

  //         console.log("COunt  :::::::::",this.totalEmployeeInActivePoCount);
  
  //         const counts = this.totalEmployeeInActivePoCount[0];

  //         // this.InActivePoCounts = [
  //         //   { type: 'Expired in last 7 Days', count: counts.totalEmpPerProjectTypeLast7days ?? '0' },
  //         //   { type: 'Expired in last 30 Days', count: counts.totalEmpPerProjectTypeLast30days ?? '0' },
  //         //   { type: 'Expired in last 90 Days', count: counts.totalEmpPerProjectTypeLast90days ?? '0' },
  //         //   { type: 'Expired in last 180 Days', count: counts.totalEmpPerProjectTypeLast180days ?? '0' },
  //         //   { type: 'Expired in last 1 Year', count: counts.totalEmpPerProjectTypeLast1Year ?? '0' },
  //         //   { type: 'Overall Expired', count: counts.totalEmpPerProjectTypeTotal ?? '0' }
  //         // ];

  //         this.InActivePoCounts = [
  //           { type: 'Expired in last 7 Days', count: counts.totalEmpPerProjectTypeLast7days ?? '0', noOfDays: 7 },
  //           { type: 'Expired in last 30 Days', count: counts.totalEmpPerProjectTypeLast30days ?? '0', noOfDays: 30 },
  //           { type: 'Expired in last 90 Days', count: counts.totalEmpPerProjectTypeLast90days ?? '0', noOfDays: 90 },
  //           { type: 'Expired in last 180 Days', count: counts.totalEmpPerProjectTypeLast180days ?? '0', noOfDays: 180 },
  //           { type: 'Expired in last 1 Year', count: counts.totalEmpPerProjectTypeLast1Year ?? '0', noOfDays: 365 },
  //           { type: 'Overall Expired', count: counts.totalEmpPerProjectTypeTotal ?? '0', noOfDays: null }
  //         ] as { type: string; count: string; noOfDays: number | null }[];
          

          
  
  //         console.log("Inactive PO Count:", counts.totalEmpPerProjectTypeTotal);
  //       } else {
  //         console.error("API Error:", response.serviceError || "Unknown error");
  
  //         this.totalEmployeeInActivePoCount = {};
  //         this.total7Days = '0';
  //         this.total30Days = '0';
  //         this.total90Days = '0';
  //         this.total180Days = '0';
  //         this.total1Year = '0';
  //         this.totalAll = '0';
  
  //         // this.InActivePoCounts = [
  //         //   { type: 'Expired in last 7 Days', count: '0' },
  //         //   { type: 'Expired in last 30 Days', count: '0' },
  //         //   { type: 'Expired in last 90 Days', count: '0' },
  //         //   { type: 'Expired in last 180 Days', count: '0' },
  //         //   { type: 'Expired in last 1 Year', count: '0' },
  //         //   { type: 'Overall Expired', count: '0' },
  //         // ];
  //       }
  //     });
  // }

 getInActivePoCount(box: any): void {
  const selectedMainFlag = this.selectedFlag[this.activeBox] || 'Default';
  const key = `${box}.${selectedMainFlag}`;
  const isActiveBox = box === this.activeBox;
  const category = this.selectedTab[this.activeBox];
  const tabName = this.employeeReportObj.category;

  this.employeeReportObj.tabName = tabName;

  this.employeeService.fetchInactivePOCounts(this.employeeReportObj)
    .pipe(first())
    .subscribe((response: any) => {
      if (response.serviceStatus === "Success" && response.serviceResponse) {
        const res = response.serviceResponse;

        // Updated to use dateRange instead of noOfDays and include totalInactivePOCount
        this.InActivePoCounts = [
          { type: 'Within 1 Month', count: res.inactiveCountWithin1Month?.toString() ?? '0', dateRange: 'within1month' },
          { type: '1 to 2 Months', count: res.inactiveCount1To2Months?.toString() ?? '0', dateRange: '1to2months' },
          { type: '2 to 3 Months', count: res.inactiveCount2To3Months?.toString() ?? '0', dateRange: '2to3months' },
          { type: '3 to 6 Months', count: res.inactiveCount3To6Months?.toString() ?? '0', dateRange: '3to6months' },
          { type: '6 to 9 Months', count: res.inactiveCount6To9Months?.toString() ?? '0', dateRange: '6to9months' },
          { type: '9 to 12 Months', count: res.inactiveCount9To12Months?.toString() ?? '0', dateRange: '9to12months' },
          { type: 'Above 12 Months', count: res.inactiveCountAbove12Months?.toString() ?? '0', dateRange: 'above12months' },
          { type: 'Total Inactive', count: res.totalInactivePOCount?.toString() ?? '0', dateRange: 'total' }
        ] as { type: string; count: string; dateRange: string }[];

        console.log("Successfully mapped Inactive PO Counts:", this.InActivePoCounts);
               
      } else {
        console.error("API Error:", response.serviceError || "Unknown error");

        this.InActivePoCounts = [
          { type: 'Within 1 Month', count: '0', dateRange: 'within1month' },
          { type: '1 to 2 Months', count: '0', dateRange: '1to2months' },
          { type: '2 to 3 Months', count: '0', dateRange: '2to3months' },
          { type: '3 to 6 Months', count: '0', dateRange: '3to6months' },
          { type: '6 to 9 Months', count: '0', dateRange: '6to9months' },
          { type: '9 to 12 Months', count: '0', dateRange: '9to12months' },
          { type: 'Above 12 Months', count: '0', dateRange: 'above12months' },
          { type: 'Total Inactive', count: '0', dateRange: 'total' }
        ];
      }
    });
}
getActivePoCount(box: any): void {
  const selectedMainFlag = this.selectedFlag[this.activeBox] || 'Default';
  const key = `${box}.${selectedMainFlag}`;
  const isActiveBox = box === this.activeBox;
  const category = this.selectedTab[this.activeBox];
  const tabName = this.employeeReportObj.category;

  this.employeeReportObj.tabName = tabName;

  this.employeeService.fetchActivePOCounts(this.employeeReportObj)
    .pipe(first())
    .subscribe((response: any) => {
      if (response.serviceStatus === "Success" && response.serviceResponse) {
        const res = response.serviceResponse;
        this.ActivePoCounts = [
          { type: 'Within 1 Month', count: res.activeCountWithin1Month?.toString() ?? '0', dateRange: 'within1month' },
          { type: '1 to 2 Months', count: res.activeCount1To2Months?.toString() ?? '0', dateRange: '1to2months' },
          { type: '2 to 3 Months', count: res.activeCount2To3Months?.toString() ?? '0', dateRange: '2to3months' },
          { type: '3 to 6 Months', count: res.activeCount3To6Months?.toString() ?? '0', dateRange: '3to6months' },
          { type: '6 to 9 Months', count: res.activeCount6To9Months?.toString() ?? '0', dateRange: '6to9months' },
          { type: '9 to 12 Months', count: res.activeCount9To12Months?.toString() ?? '0', dateRange: '9to12months' },
          { type: 'Above 12 Months', count: res.activeCountAbove12Months?.toString() ?? '0', dateRange: 'above12months' },
          { type: 'Total Active', count: res.totalactivePOCount?.toString() ?? '0', dateRange: 'total' }
        ] as { type: string; count: string; dateRange: string }[];

        console.log("Successfully mapped Inactive PO Counts:", this.ActivePoCounts);
               
      } else {
        console.error("API Error:", response.serviceError || "Unknown error");

        this.ActivePoCounts = [
          { type: 'Within 1 Month', count: '0', dateRange: 'within1month' },
          { type: '1 to 2 Months', count: '0', dateRange: '1to2months' },
          { type: '2 to 3 Months', count: '0', dateRange: '2to3months' },
          { type: '3 to 6 Months', count: '0', dateRange: '3to6months' },
          { type: '6 to 9 Months', count: '0', dateRange: '6to9months' },
          { type: '9 to 12 Months', count: '0', dateRange: '9to12months' },
          { type: 'Above 12 Months', count: '0', dateRange: 'above12months' },
          { type: 'Total Active', count: '0', dateRange: 'total' }
        ];
      }
    });
}




  exportToExcelActivePoDetails(): void {
    if (!this.allactivePOListOfEmployee || this.allactivePOListOfEmployee.length === 0) {
      return;
    }
    // if (this.isLeaveReportTable == true) {
      this.excelName = 'Active_PO_List.xlsx';

      const onlySpecificDataArr = this.allactivePOListOfEmployee.map(
        x => ({
      'Employee ID': `A-${x.employeementId}`,
      'Name' : x.name,
      'Project Name': x.projectName || 'N/A',
      'PO No': x.poNo || 'N/A',
      'PO Type': x.poProjectType || 'N/A',
      'PO Start': x.poStartDate || 'N/A',
      'PO End': x.poEndDate || 'N/A',
      'Client': x.clientName || 'N/A',
      'Location': x.clientLocation || 'N/A',
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
  }
  exportToExcelInactivePoDetails(): void {
    if (!this.allInactivePOListOfEmployee || this.allInactivePOListOfEmployee.length === 0) {
      return;
    }
    // if (this.isLeaveReportTable == true) {
      this.excelName = 'Inactive_PO_List.xlsx';

      const onlySpecificDataArr = this.allInactivePOListOfEmployee.map(
        x => ({
      'Employee ID': `${x.employeementIdAccToET}`,
      'Name' : x.name,
      'Project Name': x.projectName || 'N/A',
      'PO No': x.poNo || 'N/A',
      'PO Type': x.poProjectType || 'N/A',
      'PO Start': x.poStartDate || 'N/A',
      'PO End': x.poEndDate || 'N/A',
      'Client': x.clientName || 'N/A',
      'Location': x.clientLocation || 'N/A',
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
    



    // const exportData = this.allInactivePOListOfEmployee.map(emp => ({
    //   'Employee ID': `A-${emp.employeementId}`,
    //   'Name': emp.name,
    //   'Project Name': emp.projectName || 'N/A',
    //   'PO No': emp.poNo || 'N/A',
    //   'PO Type': emp.poProjectType || 'N/A',
    //   'PO Start': emp.poStartDate || 'N/A',
    //   'PO End': emp.poEndDate || 'N/A',
    //   'Client': emp.clientName || 'N/A',
    //   'Location': emp.clientLocation || 'N/A',
    // }));

    // const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(exportData);
    // const workbook: XLSX.WorkBook = {
    //   Sheets: { 'Inactive PO List': worksheet },
    //   SheetNames: ['Inactive PO List'],
    // };
    // const excelBuffer: any = XLSX.write(workbook, {
    //   bookType: 'xlsx',
    //   type: 'array',
    // });

    // const blobData = new Blob([excelBuffer], { type: 'application/octet-stream' });
    // saveAs(blobData, 'Inactive_PO_List.xlsx');
  }

  exportToExcelInactivePoDetailsProject(): void {
    if (!this.allInactivePOListOfEmployee || this.allInactivePOListOfEmployee.length === 0) {
      return;
    }
    // if (this.isLeaveReportTable == true) {
      this.excelName = 'Inactive_PO_List_ProjectList.xlsx';

      const onlySpecificDataArr = this.allInactivePOListOfEmployee.map(
        x => ({
      'Project Name': x.projectName || 'N/A',
      'PO No': x.poNo || 'N/A',
      'PO Type': x.poProjectType || 'N/A',
      'PO Start': x.poStartDate || 'N/A',
      'PO End': x.poEndDate || 'N/A',
      'Client': x.clientName || 'N/A',
      'Location': x.clientLocation || 'N/A',
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
    
  }

    exportToExcelActivePoDetailsProject(): void {
    if (!this.allactivePOListOfEmployee || this.allactivePOListOfEmployee.length === 0) {
      return;
    }
    // if (this.isLeaveReportTable == true) {
      this.excelName = 'Active_PO_List_ProjectList.xlsx';

      const onlySpecificDataArr = this.allactivePOListOfEmployee.map(
        x => ({
      'Project Name': x.projectName || 'N/A',
      'PO No': x.poNo || 'N/A',
      'PO Type': x.poProjectType || 'N/A',
      'PO Start': x.poStartDate || 'N/A',
      'PO End': x.poEndDate || 'N/A',
      'Client': x.clientName || 'N/A',
      'Location': x.clientLocation || 'N/A',
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
  }

//   getClientAndProjectReport() {

//   const payload = {
//     deptIds: this.employeeReportObj.deptId.join(',')
//   };

//   this.projectService.getClientAndProjectReport(payload).subscribe(
//     (response: any) => {
//       if (response.serviceStatus === 'Success') {
//         this.clientAndProjectReportList = response.serviceResponse;

        
//         this.departmentList = Array.from(
//           new Set(
//             this.clientAndProjectReportList
//               .map(item => item.departmentName?.trim())
//               .filter(Boolean)
//           )
//         ).sort();

       
//         const groupedMap = new Map<string, any>();

//         this.clientAndProjectReportList.forEach(item => {
//           const clientName = item.clientName || 'NA';
//           const departmentName = item.departmentName?.trim();

//           if (!groupedMap.has(clientName)) {
//             groupedMap.set(clientName, {
//               clientName,
//               departmentProjects: {},
//               totalActiveProjects: 0,
//               totalInactiveProjects: 0,
//               totalProjects: 0
//             });
//           }

//           const clientGroup = groupedMap.get(clientName);

         
//           if (departmentName) {
//             clientGroup.departmentProjects[departmentName] =
//               (clientGroup.departmentProjects[departmentName] || 0) + (item.totalProjects || 0);
//           }

          
//           clientGroup.totalActiveProjects += item.totalActiveProjects || 0;
//           clientGroup.totalInactiveProjects += item.totalInactiveProjects || 0;
//           clientGroup.totalProjects += item.totalProjects || 0;
//         });

//         this.groupedClientProjects = Array.from(groupedMap.values());

//       } else {
//         this.openAlertMod(this.alertModalSync, response.serviceResponse);
//       }
//     },
//     error => {
//       this.openAlertMod(this.alertModalSync, 'Something went wrong');
//     }
//   );
// }

clientProjectList: any[] = [];
flatClientProjectList: any[] = [];
clientProjectFilters: any = {};
isClientProjectSearchEnabled: boolean = false;
clientProjectReportColumns: any[] = [];

getClientAndProjectReport() {
  this.page = 1;
  this.clientProjectList = [];
  this.flatClientProjectList = [];
  this.clientProjectFilters = {}; 

  const payload = {
    deptIds: this.employeeReportObj.deptId.join(',')
  };

  this.projectService.getClientAndProjectReport(payload).subscribe(
    (response: any) => {
      if (response.serviceStatus === 'Success') {
        this.clientAndProjectReportList = response.serviceResponse;

        
        this.departmentList = Array.from(
          new Set(
            this.clientAndProjectReportList
              .map(item => item.departmentName?.trim())
              .filter(Boolean)
          )
        ).sort();

       
        const groupedMap = new Map<string, any>();

        this.clientAndProjectReportList.forEach(item => {
          const clientName = item.clientName || 'NA';
          const departmentName = item.departmentName?.trim();

          if (!groupedMap.has(clientName)) {
            groupedMap.set(clientName, {
              clientName,
              departmentProjects: {}, 
              totalActiveProjects: 0,
              totalInactiveProjects: 0,
              totalProjects: 0
            });
          }

          const clientGroup = groupedMap.get(clientName);

          if (departmentName) {
           
            if (!clientGroup.departmentProjects[departmentName]) {
              clientGroup.departmentProjects[departmentName] = {
                total: 0,
                active: 0,
                inactive: 0,
                activeEmployee:0,
                deptId:0,
                clientId:0
              };
            }

            const dept = clientGroup.departmentProjects[departmentName];
            dept.total += item.totalProjects || 0;
            dept.active += item.totalActiveProjects || 0;
            dept.inactive += item.totalInactiveProjects || 0;
            dept.activeEmployee += item.totalActiveResources || 0;
            dept.clientId = item.clientId;
            dept.deptId = item.deptId;
          }

          
          clientGroup.totalActiveProjects += item.totalActiveProjects || 0;
          clientGroup.totalInactiveProjects += item.totalInactiveProjects || 0;
          clientGroup.totalProjects += item.totalProjects || 0;
        });

        this.groupedClientProjects = Array.from(groupedMap.values());
       
        this.setupClientProjectSearchColumns();
        this.editIndex = -1;
      } else {
        this.openAlertMod(this.alertModalSync, response.serviceResponse);
      }
    },
    error => {
      this.openAlertMod(this.alertModalSync, 'Something went wrong');
    }
  );
}


setupClientProjectSearchColumns() {
   const departmentProjectTotalKeys = this.departmentList.map(
    deptName => `${deptName}.total`
  );
  this.clientProjectReportColumns = ['blank','clientName'];
}

flattenClientProjectList() {
  this.flatClientProjectList = [];
  for (const clientGroup of this.groupedClientProjects) {
    for (const [departmentName, projectCount] of Object.entries(clientGroup.departmentProjects)) {
      this.flatClientProjectList.push({
        clientName: clientGroup.clientName,
        departmentName: departmentName,
        departmentProjectCount: projectCount,
        totalActiveProjects: clientGroup.totalActiveProjects,
        totalInactiveProjects: clientGroup.totalInactiveProjects,
        totalProjects: clientGroup.totalProjects,
      });
    }
  }
  console.log("flatClientProjectList", this.flatClientProjectList);
}


// onSearchClientProject(filters: any) {
//   this.clientProjectFilters = filters;
// }

sortClientProjectData(sort: any) {
  this.sortColumn = sort.active;
  this.sortDirection = sort.direction;
  this.sortColumnType = sort.active.includes('total') || sort.active.includes('departmentProjects') ? 'number' : 'string';
}

exportClientProjectToExcel(): void {
  this.excelName = 'ClientProjectReport.xlsx';
  const exportData = this.groupedClientProjects.map((clientGroup, index) => {
    const row: any = {
      'Sr No.': index + 1,
      'Client Name': clientGroup.clientName,
      'Total Active Projects': clientGroup.totalActiveProjects,
      'Total Inactive Projects': clientGroup.totalInactiveProjects,
      'Total Projects': clientGroup.totalProjects
    };
    this.departmentList.forEach(dept => {
      row[dept] = clientGroup.departmentProjects[dept]?.total || 0;
    });
    return row;
  });
  console.log('Exporting client/project data:', exportData);
  this.exportExcelService.exportTableDataToExcel(exportData, this.excelName);
}

selectedStatus: string = 'all';  
modalProjectData: any[] = [];
selectedClientName: string = '';
 modalCurrentPage: number = 1;
  modalItemsPerPage: number = 10;
  isModalSearchEnabled: boolean = false;
  modalFilters: any = {};
  modalSortColumn: string = '';
  modalSortColumnType: string = '';
  modalSortDirection: string = 'asc';
 modalProjectColumns: any[] = [
    { key: 'projectName', label: 'Project Name', type: 'string' },
    { key: 'poNo', label: 'PO Number', type: 'string' },
    { key: 'projectType', label: 'Project Type', type: 'string' },
    { key: 'clientName', label: 'Client', type: 'string' },
    { key: 'apmosysRM', label: 'Apmosys RM', type: 'string' },
    { key: 'clientRM', label: 'Client RM', type: 'string' }
  ];
  modalProjectColumns1 :any[]=['projectName','poNo','projectType','clientName','apmosysRM','clientRM','poStartDate','poEndDate','createdOn'];

openClientProjectModal(template: TemplateRef<any>, clientName: string, department: string,deptId:any,clientId:any,projectType:any) {
  this.selectedClientName = clientName;
  if (department.includes('_active')) {
    this.selectedDepartment = department.replace('_active', '');
    this.selectedStatus = 'active';
  } else if (department.includes('_inactive')) {
    this.selectedDepartment = department.replace('_inactive', '');
    this.selectedStatus = 'inactive';
  } else {
    this.selectedDepartment = department;
    this.selectedStatus = 'all';
  }
   this.modalCurrentPage = 1;
    this.isModalSearchEnabled = false;
    this.resetModalFilters();
    this.resetModalSorting();
  this.getClientAndProjectReportDataList(clientId, deptId,projectType);
  this.modalRef1 = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
}
toggleModalSearch(): void {
    this.isModalSearchEnabled = !this.isModalSearchEnabled;
    if (!this.isModalSearchEnabled) {
      this.resetModalFilters();
      this.modalCurrentPage = 1;
    }
  }
 resetModalFilters(): void {
    this.modalFilters = {};
  }
   resetModalSorting(): void {
    this.modalSortColumn = '';
    this.modalSortColumnType = '';
    this.modalSortDirection = 'asc';
  }
   sortModalData(event: any): void {
    this.modalSortColumn = event.active;
    this.modalSortDirection = event.direction;
    this.modalSortColumnType = this.getColumnType(event.active);
    this.modalCurrentPage = 1;
  }
  getColumnType(columnKey: string): string {
    const column = this.modalProjectColumns.find(col => col.key === columnKey);
    return column ? column.type : 'string';
  }
  
  onModalSearch(filters: any): void {
    this.modalFilters = filters;
    this.modalCurrentPage = 1;
  }
handleModalPageChange(page: number): void {
    this.modalCurrentPage = page;
    
  }

  exportModalDataToExcel(): void {
    const dataToExport = this.clientAndProjectReportDataList;
    
    if (dataToExport.length === 0) {
      this.openAlertMod(this.alertModal, "No projects to convert to Excel.");
      return;
    }
    
    this.excelName = 'ProjectReport.xlsx';
    const exportData = dataToExport.map((project, index) => ({
      'Sr No.': index + 1,
      'Project Name': project.projectName || 'NA',
      'PO Number': project.poNo || 'NA',
      'Project Type': project.projectType ? 
        (project.projectType.charAt(0).toUpperCase() + project.projectType.slice(1)) : '',
      'Client': project.clientName || 'NA',
      'Apmosys RM': project.apmosysRM || 'NA',
      'Client RM': project.clientRM || 'NA',
      'Start Date': project.poStartDate ? 
        new Date(project.poStartDate).toLocaleDateString('en-GB') : 'NA',
      'End Date': project.poEndDate ? 
        new Date(project.poEndDate).toLocaleDateString('en-GB') : 'NA',
      'Created On': project.createdOn ? 
        new Date(project.createdOn).toLocaleDateString('en-GB') : 'NA'
    }));
    
   
    
    try {
      this.exportExcelService.exportTableDataToExcel(exportData, this.excelName);
     
    } catch (error) {
    
      this.openAlertMod(this.alertModal, "Error occurred while exporting to Excel.");
    }
  }

expandedClients: { [clientName: string]: boolean } = {};

toggleClientDetails(clientName: string) {
  this.expandedClients[clientName] = !this.expandedClients[clientName];
}

expandedDepartments: { [key: string]: boolean } = {};

toggleDepartment(clientName: string, department: string) {
  const key = `${clientName}_${department}`;
  this.expandedDepartments[key] = !this.expandedDepartments[key];
}


getTotalActiveEmployees(clientGroup: any): number {
  let total = 0;
  for (const dept of Object.values(clientGroup.departmentProjects)) {
    total += dept['activeEmployee'] || 0;
  }
  return total;
}
getFilteredData() {
  if (!this.groupedClientProjects || this.groupedClientProjects.length === 0) {
    return [];
  }
  if (this.clientProjectFilters && Object.keys(this.clientProjectFilters).length > 0) {
    return this.groupedClientProjects.filter(item => {
      return true;
    });
  }
  
  return this.groupedClientProjects;
}

getPaginatedData() {
  const filteredData = this.getFilteredData();
  const startIndex = (this.page - 1) * this.itemsPerPage;
  const endIndex = startIndex + this.itemsPerPage;
  return filteredData.slice(startIndex, endIndex);
}

getSerialNumber(index: number): number {
  return (this.page - 1) * this.itemsPerPage + index + 1;
}

activeEmployeePopup: string | null = null;
currentEmployeeCount: number = 0;

showEmployeeCount(event: Event, clientName: string, department: string, status: string) {
  event.stopPropagation();
  this.currentEmployeeCount = Math.floor(Math.random() * 13) + 3;
  const popupId = `${clientName}_${department}_${status}`;
  this.activeEmployeePopup = this.activeEmployeePopup === popupId ? null : popupId;
}

closeEmployeePopup(event?: MouseEvent) {
  if (event) {
    event.stopPropagation();
  }
  this.activeEmployeePopup = null;
}
isEmployeePopupActive(clientName: string, department: string, status: string): boolean {
  const popupId = `${clientName}_${department}_${status}`;
  return this.activeEmployeePopup === popupId;
}

getClientAndProjectReportDataList(clientId:any,deptId:any,projectType:any){
  this.clientAndProjectReportDataList =[];
  const payload = {
      projectType:projectType,
      clientIds: String(clientId),
      deptIds: String(deptId) ,  
    };
  this.projectService.getClientAndProjectReportDataList(payload).subscribe(
    (response: any) => {
      if (response.serviceStatus === 'Success') {
          this.clientAndProjectReportDataList = response.serviceResponse;
      }
    },
    error => {
      this.openAlertMod(this.alertModalSync, 'Something went wrong');
    })
}

  excludeMaternityLeaveEmployees() {
    this.activeBox = "";
    this.hideMaternityLeaveEmps = !this.hideMaternityLeaveEmps;
    this.employeeReportObj.hideMaternityLeaveEmps = !this.hideMaternityLeaveEmps;
    if (this.employeeReportObj.category === 'Project') {
      this.employeeReportObj.report = 'P';
    } else if (this.employeeReportObj.category === 'Employee' && this.changeTable === true) {
      this.employeeReportObj.report = 'EC';
    } else {
      this.employeeReportObj.report = 'E';
    }
    // this.getEmployeeReportData();
    // ['TNM', 'Fixed Cost', 'Monitoring', 'Internal'].forEach(box => {
    //   this.selectedTab[box] = 'Employee';
    //   this.selectedFlag[box] = null;
    //   this.selectedBillable[box] = null;
    //   const payload = this.buildPayload(box);
    //   console.log("The payload is",payload);
    //   this.getEmployeeProjectCount(box, payload);
    // });
   
    // this.getTotalActiveEmployeeCountInDepartments();
    // this.projectLessEmployeesDepartmentWise();
    // this.employeesMappedProjectsDepartmentWise();
    this.activeBox = this.employeeReportObj.poProjectType;
    this.refreshReportDataWithModifiedCount();
  }

  private getReportType(box: string): string {
    const category = this.selectedTab[box];
    if (category === 'Project') return 'P';
    if (category === 'Employee' && this.changeTable) return 'EC';
    return 'E';
  }

  private buildPayload(box: string): any {
    console.log("Category",this.selectedTab);
    console.log("Flag",this.selectedFlag);
    console.log("Billable Type",this.selectedBillable);
    console.log("Hide Maternity Leave",this.hideMaternityLeaveEmps);
    console.log("Selected departments",this.employeeReportObj.deptId);
    return {
      poProjectType: box,
      category: this.selectedTab[box] || '',
      flag: this.selectedFlag[box] || '',
      billableType: this.selectedBillable[box] ? [this.selectedBillable[box]] : [],
      hideMaternityLeaveEmps: !this.hideMaternityLeaveEmps,
      report: this.getReportType(box),
      deptId: this.employeeReportObj.deptId.map(item=>+item)
    };
  }

  getEmployeeProjectCount(box: string, payload: any) {
    this.page = 1;
    // console.log("getEmployeeProjectCount payload ", payload);
    // console.log("The payload is",payload);

    this.projectService.getEmployeeProjectCount(payload).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        console.log("response ::::::::::::::::::::::", response.serviceResponse);
        this.countData[box] = response.serviceResponse;
      }
      else{
        console.log("API Error: ", response.serviceError || "Unknown error");
      }
    })
  }

  expandedTeams = new Set<string>(); 
  extendEmployees(pIndex: number, tIndex: number) {
    const key = `${pIndex}-${tIndex}`;
    if (this.expandedTeams.has(key)) {
      this.expandedTeams.delete(key);
    } else {
      this.expandedTeams.add(key);
    }
  }

  private modifiedEmployeeCountAccordingTOSelectedCards(): void {
    // console.log("Refreshing data with dept IDs:", this.employeeReportObj);
    // this.getEmployeeReportData();
    
    ['TNM', 'Fixed Cost', 'Monitoring', 'Internal'].forEach(box => {
     if(this.activeBox == box){
       this.selectedTab[box] = this.employeeReportObj.category;
       this.selectedFlag[box] = this.employeeReportObj.flag;
       console.log("Billable Type",this.selectedBillable);
       console.log("Billable Type By Employee",this.employeeReportObj.billableType);
       this.selectedBillable[box] = this.employeeReportObj?.billableType?.[0];
     }
     else{

       this.selectedTab[box] = 'Employee';
       this.selectedFlag[box] = null;
       this.selectedBillable[box] = null;
      }
       const payload = this.buildPayload(box);
      this.getEmployeeProjectCount(box, payload);
    });
    // this.getTotalActiveEmployeeCountInDepartments();
    // this.projectLessEmployeesDepartmentWise();
    // this.employeesMappedProjectsDepartmentWise();
  }

   private refreshReportDataWithModifiedCount(): void {
    // console.log("Refreshing data with dept IDs:", this.employeeReportObj);
    this.modifiedEmployeeCountAccordingTOSelectedCards();
    this.getTotalActiveEmployeeCountInDepartments();
    this.projectLessEmployeesDepartmentWise();
    this.employeesMappedProjectsDepartmentWise();
    this.employeeReportObj.hideMaternityLeaveEmps = !this.employeeReportObj.hideMaternityLeaveEmps;
    this.getEmployeeReportData();
     if(this.employeeReportObj.flag === 'Inactive') {this.getInActivePoCount(this.employeeReportObj);}
    else if(this.employeeReportObj.flag === 'Active') {this.getActivePoCount(this.employeeReportObj);}

  }

  openAlertForTimesheetLeaveReport(message: string): void {
    this.modalMessage = message;
    this.alert_message_timesheet_leave_reportModalRef = this.modalService.open(this.alert_message_timesheet_leave_report, {
      modalDialogClass: 'modal-dialog-centered  modal-sm'
    });
  }

  closeAlertForTimesheetLeaveReport(): void {
    if (this.alert_message_timesheet_leave_reportModalRef) {
      this.alert_message_timesheet_leave_reportModalRef.close();
    }
  }

  toggeleTimesheetLeaveReport(): void {
    this.exportAll = true;
    this.getAllLeaveTimesheets(this.alert_message_timesheet_leave_report);
  }


}

function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
}

function saveAs(blobData: Blob, arg1: string) {
  throw new Error("Function not implemented.");
}