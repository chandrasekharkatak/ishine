import { CdkDragDrop, CdkDragRelease, moveItemInArray } from "@angular/cdk/drag-drop";
import { formatDate, LocationStrategy } from '@angular/common';
import { Component, OnInit, Renderer2, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Employee } from 'src/app/models/employee';
import { Feature } from 'src/app/models/feature';
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
import { TimesheetService } from 'src/app/services/timesheet.service';
import { UtilityService } from 'src/app/services/utility.service';
import { ValidationService } from 'src/app/services/validation.service';
import * as XLSX from 'xlsx';

class FilterData {
  title: any;
  columns: any;
  queryList: any;
}
@Component({
  selector: 'app-report-list',
  templateUrl: './report-list.component.html',
  styleUrls: ['./report-list.component.css']
})
export class ReportListComponent implements OnInit {

  @ViewChild("alert_message")
  alertModal: TemplateRef<any>;

  feature = 'Reports';
  currentUser: User;
  userMapping: any = {};

  data: string; //Search Data
  designationData: string;   //Search Designation

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;

  //modal 
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();

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

  insideCols: any[] = [];

  storedDataList: any[] = [];

  excelName: any;
  jobRoleName: any;
  departmentId: any;
  employeeRole: any;
  selectedColumnToShow: any;

  leaveColumns: any[] = ['Employee Id', 'Full Name', 'Employment Status', 'Leave Type', 'Team Name', 'Project Name', 'Client Name', 'Department', 'From Date', 'To Date', 'No. of Days', 'Reason', 'Status', 'Manager Name', 'Created On', 'Updated On', 'Updated By'];
  employeeColumns: any[] = ['Employee Id', 'Full Name', 'Department','Designation', 'Job Role', 'Manager', 'Team Name', 'Project Name','Po No','Po Start Date','Po End Date','Po Project Type', 'Client Name', 'Employment Status', 'Date Of Joining','Domain','Specialization', 'City', 'Blood Group', 'Gender', 'Work Location', 'Probation Period', 'Notice Period', 'Marital Status', 'Bank Name', 'Created By', 'State', 'Created On','Profile Completion'];
  timesheetColumns: any[] = ['Employee Id', 'Full Name', 'Employment Status', 'Department', 'Date', 'Day Type', 'Status', 'Total Working Hour', 'Team Name', 'Project Name', 'Client Name', 'From Date', 'To Date', 'Created On', 'Updated On', 'Updated By', 'Leave Type'];
  queryList: any[] = [];
  filterData: any = new FilterData();

  columns: any[] = [];
  paginateData: any[] = [];
  pos: any;
  release: boolean = true;
  finalColumns: any[] = [];

  allLeaveTimesheets: any[] = [];
  endDate:any;
  startDate:any;

  customQuery:any;
  leaveReportFlag:boolean=false;
  timesheetReportFlag:boolean=false;
  showDetails:boolean = false;
  changeTable:boolean = true;

  filters:any = {};
  isSearchEnabled:boolean = false;

  employeeReportColumnForDetailedProjectView:any[]=['blank','employeementId','name','departmentName','jobRoleName','managerName','mobileNo','email','employmentstatus','billable','billableType','teamName','projectName','poNo','poType','poStartDate','poEndDate','effectiveStartDate','effectiveEndDate','clientName','clientLocation','workLocation','experience'];
  leaveReportColumns:any[] = ['employeementId','employeeType','employeeName','leaveType','fromDate','toDate','fromDateDayType','toDateDayType','noOfDays','reason','status','managerName','departmentName','createdOn','updatedOn','leaveStatusUpdatedByName'];
  timesheetReportColumns:any[] = ['employeementId','employeeType','employeeName','date','dayType','description','status','totalWorkingHours','officeInTime','officeOutTime','totalWorkingOfficeHours','leaveType','createdOn','updatedOn','timesheetStatusUpdatedByName'];
  employeeReportColumn:any[] = ['blank','employeementId','employeeType','name','departmentName','jobRoleName','managerName','mobileNo','email','employmentstatus','projectName','poNo','poStartDate','poEndDate','poProjectType','clientName','billable','billableType','dateOfJoining','aadhar','aboutMe','address','permanentAddress','city','bloodGroup','dateOfBirth','gender','fatherName','panNumber','placeOfBirth','workLocation','probationPeriod','noticePeriod','country','totalExperience','emergencyContactMobile','emergencyContactPerson','landline','maritalStatus','motherTongue','alternateMobileNo','pincode','relation','state','viewsOnOrganisation','passportNumber','bankAccountNo','bankIFSCCode','bankName','pfAccountNumber','previousPfAccountNumber','uan','esicNumber','graduationType','pursuing','passingGrade','yearOfPassing','updatedOn','updatedByName','createdByName','createdOn'];
  leaveTimesheetReportColumn:any[] = ['employeementId','employeeType','employeeName','date','dayType','description','status','managerName','departmentName','createdOn','updatedOn','timesheetStatusUpdatedByName'];
  defaultMappingColumns:any[] = ['tabName','featureName','subFeatureName'];
  departments:any [] = [];
  allEmployee : any[] =[];
  filteredEmployees: any []=[];
  tnmPoExpiredCount=0;
  tnmPOValidCount=0;
  fixedCostPoExpiredCount=0;
  fixedCostPoValidCount=0;
  internalCount=0;
  selectedDepartment: string = 'All';

  activeBox: string | null = null;
  isHovering: string | null = null;

  tnmPoExpiredCountList:any[]=[];
  tnmPOValidCountList:any[]=[];
  fixedCostPoExpiredCountList:any[]=[];
  fixedCostPoValidCountList:any[]=[];
  internalCountList:any[]=[];


 
  constructor(
    private authenticationService: AuthenticationService,
    private modalService: BsModalService,
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

  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    // Dynamic Subfeature Flags 
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    //console.log(this.feature, this.userMapping);
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
    if (this.userMapping.leave_report) {
      this.showLeaveReportTable();
    } else if (this.userMapping.timesheet_report) {
      this.showTimesheetReportTable();
    } else if (this.userMapping.employee_report) {
      this.showEmployeeReportTable();
    }
  }
  toggleView() {
    if(this.showDetails === true){
      this.showDetails = false;

    }
    else{
      this.showDetails = true;
      this.changeTable = true;
      this.selectedDepartment= 'all';
      this.getAllDepartments();
      this.getAllEmployeesReportByProjectTypeInConsolidated();
      this.filterEmployees();
      this.tnmPoExpiredCount=0;
    this.tnmPOValidCount=0;
    this.fixedCostPoExpiredCount=0;
    this.fixedCostPoValidCount=0;
    this.internalCount=0;
    }
  }
  toggleTableView(){
    if(this.changeTable === true){
      this.changeTable = false;
      this.getAllEmployeesReportByProjectType();
    }
    else{
      this.changeTable = true;
      this.getAllEmployeesReportByProjectTypeInConsolidated();
      this.tnmPoExpiredCount=0;
    this.tnmPOValidCount=0;
    this.fixedCostPoExpiredCount=0;
    this.fixedCostPoValidCount=0;
    this.internalCount=0;
    }
  }

 

  onMouseOver(box: string): void {
    
    this.isHovering = box;
  }

  onMouseLeave(): void {
    this.isHovering = null;
  }
  getAllEmployeesReportByProjectTypeInConsolidated(){
    this.allEmployee=[];
    this.employeeService.getAllEmployeesReportByProjectTypeInConsolidated().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        // console.log('response -- ',response.serviceResponse);
        this.allEmployee=response.serviceResponse;
        console.log('emps -- ',this.allEmployee);
        this.filterEmployees();
        
        
        this.allEmployee.forEach((emp) => {
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
          console.log("TNM Expired Count:", this.tnmPoExpiredCount);
          console.log("Fixed Cost Expired Count:", this.fixedCostPoExpiredCount);
          console.log("TNM Valid Count:", this.tnmPOValidCount);
          console.log("Fixed Cost Valid Count:", this.fixedCostPoValidCount);
        });

      }else {
        alert(response.serviceResponse);
      }
    });
  }
  // onBoxClickDataChange(boxName){
  //   this.filteredEmployees=[];
  //   this.activeBox = boxName;
  //   //TNM-Active,TNM-Expired,FC-Active,FC-Expired
  //   const currentDate = new Date();
  //   //const poEndDate = new Date(emp.poEndDate)

  //     if (boxName === 'TNM-Active') {
  //       this.filteredEmployees = this.allEmployee.filter(emp => 
  //         emp.poProjectType === "TNM" && new Date(emp.poEndDate) >= currentDate
  //       );
  //     } else if (boxName === 'TNM-Expired') {
  //       this.filteredEmployees = this.allEmployee.filter(emp => 
  //         emp.poProjectType === "TNM" && new Date(emp.poEndDate) < currentDate
  //       );
  //     } else if (boxName === 'FC-Active') {
  //       this.filteredEmployees = this.allEmployee.filter(emp => 
  //         emp.poProjectType === "Fixed Cost" && new Date(emp.poEndDate) >= currentDate
  //       );
  //     } else if (boxName === 'FC-Expired') {
  //       this.filteredEmployees = this.allEmployee.filter(emp => 
  //         emp.poProjectType === "Fixed Cost" && new Date(emp.poEndDate) < currentDate
  //       );
  //     } else {
  //       this.filteredEmployees = this.allEmployee.filter(emp => 
  //         emp.poProjectType !== "Fixed Cost" && emp.poProjectType !== "TNM"
  //       );
  //     }
  //     console.log('Box click data-- ', this.filteredEmployees);


  // }
//   onBoxClickDataChange(boxName) {
//     this.filteredEmployees = [];
//     this.activeBox = boxName;
//     const currentDate = new Date();

//     this.filteredEmployees = this.allEmployee.filter(emp => {
      
//       const projectTypes = emp.poProjectType.toLowerCase().split(',').map(type => type.trim()); 
//       if(emp.poProjectType){
        

//         if (boxName === 'TNM-Active') {
//             return projectTypes.includes('tnm') && new Date(emp.poEndDate) >= currentDate;
//         } else if (boxName === 'TNM-Expired') {
//             return projectTypes.includes('tnm') && new Date(emp.poEndDate) < currentDate;
//         } else if (boxName === 'FC-Active') {
//             return projectTypes.includes('fixed cost') && new Date(emp.poEndDate) >= currentDate;
//         } else if (boxName === 'FC-Expired') {
//             return projectTypes.includes('fixed cost') && new Date(emp.poEndDate) < currentDate;
//         } 
//       }else {
//         return !projectTypes.includes('fixed cost') && !projectTypes.includes('tnm');
//     }
        
//     });

//     console.log('Box click data-- ', this.filteredEmployees);
// }
// onBoxClickDataChange(boxName) {
//   this.filteredEmployees = [];
//   this.activeBox = boxName;
//   const currentDate = new Date();

  // this.filteredEmployees = this.allEmployee.filter(emp => {
  //     // Handle null poProjectType
  //     if (!emp.poProjectType) {
  //         return false; // Exclude employees with null or undefined poProjectType
  //     }

  //     // Normalize poProjectType: convert to lowercase and split by commas
  //     const projectTypes = emp.poProjectType.toLowerCase().split(',').map(type => type.trim());

  //     if (boxName === 'TNM-Active') {
  //         return projectTypes.includes('tnm') && new Date(emp.poEndDate) >= currentDate;
  //     } else if (boxName === 'TNM-Expired') {
  //         return projectTypes.includes('tnm') && new Date(emp.poEndDate) < currentDate;
  //     } else if (boxName === 'FC-Active') {
  //         return projectTypes.includes('fixed cost') && new Date(emp.poEndDate) >= currentDate;
  //     } else if (boxName === 'FC-Expired') {
  //         return projectTypes.includes('fixed cost') && new Date(emp.poEndDate) < currentDate;
  //     } else {
  //         return !projectTypes.includes('fixed cost') && !projectTypes.includes('tnm');
  //     }
  // });

//   console.log('Box click data-- ', this.filteredEmployees);
// }
onBoxClickDataChange(boxName) {
  this.filteredEmployees = [];
  this.activeBox = boxName;
  // const currentDate = new Date();

  // this.filteredEmployees = this.allEmployee.filter(emp => {
  //     if (!emp.poProjectType) {
  //         return false; 
  //     }

      // const projectTypes = emp.poProjectType.toLowerCase().split(',').map(type => type.trim());

      if (boxName === 'TNM-Active') {
          // return projectTypes.includes('tnm') && new Date(emp.poEndDate) >= currentDate;
          this.filteredEmployees=this.tnmPOValidCountList;
      } else if (boxName === 'TNM-Expired') {
        this.filteredEmployees=this.tnmPoExpiredCountList;

          // return projectTypes.includes('tnm') && new Date(emp.poEndDate) < currentDate;
      } else if (boxName === 'FC-Active') {
        this.filteredEmployees=this.fixedCostPoValidCountList;
          // return projectTypes.includes('fixed cost') && new Date(emp.poEndDate) >= currentDate;
      } else if (boxName === 'FC-Expired') {
        this.filteredEmployees=this.fixedCostPoExpiredCountList;

          // return projectTypes.includes('fixed cost') && new Date(emp.poEndDate) < currentDate;
      } else {
          // return !projectTypes.includes('fixed cost') && !projectTypes.includes('tnm');
          this.filteredEmployees=this.internalCountList;
      }
  // });

  console.log('Box click data-- ', this.filteredEmployees);
}

  onDepartmentChange(event: any) {
    this.filteredEmployees=[];

    // if(this.selectedDepartment === 'all'){
    //   this.filteredEmployees=this.allEmployee;
    // }else{
      this.selectedDepartment = event.target.value;
    // }
    console.log('Selected Department:', this.selectedDepartment);
    this.filterEmployees();
    
  }
  getAllDepartments(){
    this.departmentService.getAllDepartments().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        // console.log('response -- ',response.serviceResponse);
        this.departments = response.serviceResponse;
        // console.log('dept response -- ',this.departments);

      }else {
        alert(response.serviceResponse)
      }
    });
  }

  filterEmployees() {
    if (this.selectedDepartment) {
      if(this.selectedDepartment === 'all'){
        this.filteredEmployees = [...this.allEmployee];
      }else{
        this.filteredEmployees = this.allEmployee.filter(emp => emp.departmentId == this.selectedDepartment);
      }
    }
    console.log('Filtered dept data -- ', this.filteredEmployees);
    //Fixed Cost,TNM,Bench,InternalRNDProducts,Shadow
    //poEndDate 
  }
  
  showLeaveReportTable() {
    this.leaveReportFlag = true;
    this.timesheetReportFlag = false;
    this.storedDataList = [];
    this.sortColumn=[];
    this.sortColumnType=[];
    this.sortDirection='';
    this.page = 1;
    this.isLeaveReportTable = true;

    this.isTimesheetReportTable = false;
    this.isEmployeeReportTable = false;
    this.isAccessControlListTable = false;
    this.isCustomQueryForm = false;
    this.isLeaveTimesheetReportTable = false;

    this.filters = {};
    this.isSearchEnabled = false;

    //console.log(this.storedDataList, " : storeddatalist");


    this.storedDataList.forEach((object) => {
      if (object.filterName == 'Filter Leave Report') {
        this.getCustomLeaveApplicationsList(object.queryList, this.alertModal);
      }
    });

    if (this.storedDataList.length == 0 || !this.storedDataList.find(x => x.filterName == 'Filter Leave Report')) {
      let inActiveQuery = [
        { column: "Employment Status", operator: "!=", value: "InActive", conjunction: "" }
      ];
      // this.getAllLeaveApplicationsList();

      this.getCustomLeaveApplicationsList(inActiveQuery, this.alertModal);

    }

    this.data = ''
  }

  showTimesheetReportTable() {
    this.leaveReportFlag = false;
    this.timesheetReportFlag = true;
    this.storedDataList=[];
    this.sortColumn=[];
    this.sortColumnType=[];
    this.sortDirection='';
    this.page = 1;
    this.isTimesheetReportTable = true;

    this.isLeaveReportTable = false;
    this.isEmployeeReportTable = false;
    this.isAccessControlListTable = false;
    this.isCustomQueryForm = false;
    this.isLeaveTimesheetReportTable = false;
    this.filters = {};
    this.isSearchEnabled = false;

    this.storedDataList.forEach((object) => {
      if (object.filterName == 'Filter Timesheet Report') {
        this.getCustomTimesheetApplicationsList(object.queryList, this.alertModal);
      }
    });

    if (this.storedDataList.length == 0 || !this.storedDataList.find(x => x.filterName == 'Filter Timesheet Report')) {
      let inActiveQuery = [
        { column: "Employment Status", operator: "!=", value: "InActive", conjunction: "" }
      ];
      this.getCustomTimesheetApplicationsList(inActiveQuery, this.alertModal);

      //this.getAllTimesheetApplicationsList();
    }

    this.data = ''
  }

  showEmployeeReportTable() {
    this.leaveReportFlag = false;
    this.timesheetReportFlag = false;
    this.storedDataList=[];
  this.sortColumn=[];
    this.sortColumnType=[];
    this.sortDirection='';
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
      // this.getAllEmployeeList();
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

  showLeaveTimesheetReportTable(){
    this.leaveReportFlag = false;
    this.timesheetReportFlag = false;
    this.sortColumn=[];
    this.sortColumnType=[];
    this.sortDirection='';
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

  showDefaultMappingTable(){
    this.getDefaultMapping(this.alertModal);
  }

  disableMannualDateInput() {
    return false;
  }


  // Leave Report 
  getAllLeaveApplicationsList() {
    this.queryList = [];
    this.allLeaveApplicationsList = [];

    this.leaveService.leaveReport().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allLeaveApplicationsList = response.serviceResponse;
        // this.allLeaveApplicationsList = this.allLeaveApplicationsList.filter(x => x.employmentstatus != 'InActive');
        this.allLeaveApplicationsList.forEach(leave => {
          leave.employeementId = "A-".concat(leave.employeementId);
          // leave.employeementId = (leave.isConsultant === 'true' ? "A-CS-" : "A-").concat(leave.employeementId);
          leave.employeeType = ((leave.isApprenticeship === 'true') ? 'Apprentice' : ((leave.isConsultant === 'true') ? 'Consultant': 'Regular')),
          leave.fromDate = (leave.fromDate) ? moment(leave.fromDate).format(AppComponent.DATE_FORMAT) : null;
          leave.toDate = (leave.toDate) ? moment(leave.toDate).format(AppComponent.DATE_FORMAT) : null;
          leave.createdOn = (leave.createdOn) ? moment(leave.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          leave.updatedOn = (leave.updatedOn) ? moment(leave.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;

          if(leave.fromDateDayType != null){
            leave.fromDateDayType = leave.fromDateDayType === 0 ? "Full Day" : "Half Day";
          }
          if(leave.toDateDayType != null){
            leave.toDateDayType = leave.toDateDayType === 0 ? "Full Day" : "Half Day";
          }
        });
        //console.log("allLeaveApplicationsList : ", this.allLeaveApplicationsList)
      } else {
        alert(response.serviceResponse)
      }
    });
  }

  getCustomLeaveApplicationsList(queryObjList: any, template: TemplateRef<any>) {
    this.allLeaveApplicationsList = [];

    let queryObj = new Query();
    queryObj.queryList = queryObjList;
    if (queryObjList.length == 0) {
      this.getAllLeaveApplicationsList();
    } else {
      this.leaveService.customQueryForLeaveReport(queryObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allLeaveApplicationsList = response.serviceResponse;

          // this.allLeaveApplicationsList = this.allLeaveApplicationsList.filter((value, index, self) =>
          //   index === self.findIndex((t) => (
          //     t.employeementId === value.employeementId && t.fromDate === value.fromDate
          //   ))
          // )

          if (this.allLeaveApplicationsList.length == 0) {
            this.openAlertMod(this.alertModal, "No Leave Application Report found ")
          }
          this.allLeaveApplicationsList.forEach(leave => {
            leave.employeementId = "A-".concat(leave.employeementId);
            // leave.employeementId = (leave.isConsultant === 'true' ? "A-CS-" : "A-").concat(leave.employeementId);
            leave.employeeType = ((leave.isApprenticeship === 'true') ? 'Apprentice' : ((leave.isConsultant === 'true') ? 'Consultant': 'Regular')),
            leave.fromDate = (leave.fromDate) ? moment(leave.fromDate).format(AppComponent.DATE_FORMAT) : null;
            leave.toDate = (leave.toDate) ? moment(leave.toDate).format(AppComponent.DATE_FORMAT) : null;
            leave.createdOn = (leave.createdOn) ? moment(leave.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
            leave.updatedOn = (leave.updatedOn) ? moment(leave.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;

            if(leave.fromDateDayType != null){
              leave.fromDateDayType = leave.fromDateDayType === 0 ? "Full Day" : "Half Day";
            }
            if(leave.toDateDayType != null){
              leave.toDateDayType = leave.toDateDayType === 0 ? "Full Day" : "Half Day";
            }
          });
          //console.log("allLeaveApplicationsList : ", this.allLeaveApplicationsList)
        } else {
          this.openAlertMod(template, response.serviceResponse)
        }
      });
    }
  }

  // Timesheet Report
  getAllTimesheetApplicationsList() {
    this.allTimesheetApplicationsList = [];

    this.timesheetService.timesheetReport().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allTimesheetApplicationsList = response.serviceResponse;
        // this.allTimesheetApplicationsList = this.allTimesheetApplicationsList.filter(x => x.employmentstatus != 'InActive');	

        this.allTimesheetApplicationsList.forEach(timesheet => {
          timesheet.employeementId = "A-".concat(timesheet.employeementId);
          // timesheet.employeementId = (timesheet.isConsultant === 'true' ? "A-CS-" : "A-").concat(timesheet.employeementId);
          timesheet.employeeType = ((timesheet.isApprenticeship === 'true') ? 'Apprentice' : ((timesheet.isConsultant === 'true') ? 'Consultant': 'Regular')),
          timesheet.description = timesheet.description?.replaceAll('<br>', '')
          timesheet.date = (timesheet.date) ? moment(timesheet.date).format(AppComponent.DATE_FORMAT) : null;
          timesheet.officeInTime = (timesheet.officeInTime) ? moment(timesheet.officeInTime).format(AppComponent.DATETIME_FORMAT) : null;
          timesheet.officeOutTime = (timesheet.officeOutTime) ? moment(timesheet.officeOutTime).format(AppComponent.DATETIME_FORMAT) : null;
          timesheet.createdOn = (timesheet.createdOn) ? moment(timesheet.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          timesheet.updatedOn = (timesheet.updatedOn) ? moment(timesheet.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
        });
        //console.log("allTimesheetApplicationsList : ", this.allTimesheetApplicationsList)
      } else {
        alert(response.serviceResponse)
      }
    });
  }

  getCustomTimesheetApplicationsList(queryObjList: any, template: TemplateRef<any>) {
    this.allTimesheetApplicationsList = [];

    let queryObj = new Query();
    queryObj.queryList = queryObjList;
    if (queryObjList == '') {
      this.getAllTimesheetApplicationsList();
    } else {
      this.timesheetService.customTimesheetApplicationReport(queryObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allTimesheetApplicationsList = response.serviceResponse;

          if (this.allTimesheetApplicationsList.length == 0) {
            this.openAlertMod(this.alertModal, "No Timesheet Application Report found ");
          }
          this.allTimesheetApplicationsList.forEach(timesheet => {
            timesheet.employeementId = "A-".concat(timesheet.employeementId);
            // timesheet.employeementId = (timesheet.isConsultant === 'true' ? "A-CS-" : "A-").concat(timesheet.employeementId);
            timesheet.employeeType = ((timesheet.isApprenticeship === 'true') ? 'Apprentice' : ((timesheet.isConsultant === 'true') ? 'Consultant': 'Regular')),
            timesheet.description = timesheet.description?.replaceAll('<br>', '')
            timesheet.date = (timesheet.date) ? moment(timesheet.date).format(AppComponent.DATE_FORMAT) : null;
            timesheet.officeInTime = (timesheet.officeInTime) ? moment(timesheet.officeInTime).format(AppComponent.DATETIME_FORMAT) : null;
            timesheet.officeOutTime = (timesheet.officeOutTime) ? moment(timesheet.officeOutTime).format(AppComponent.DATETIME_FORMAT) : null;
            timesheet.createdOn = (timesheet.createdOn) ? moment(timesheet.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
            timesheet.updatedOn = (timesheet.updatedOn) ? moment(timesheet.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
          });
          //console.log("allTimesheetApplicationsList : ", this.allTimesheetApplicationsList)
        } else {
          this.openAlertMod(template, response.serviceResponse)
        }
      });
    }
  }

  // Employee Report 
  getAllEmployeeList() {
    this.queryList = [];
    this.allEmployeeList = [];

    this.employeeService.getAllEmployees().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allEmployeeList = response.serviceResponse;
        // this.allEmployeeList = this.allEmployeeList.filter(x => x.employmentstatus != 'InActive');	

        this.allEmployeeList.forEach(employee => {
          employee.employeementId = "A-".concat(employee.employeementId);
          // employee.employeementId = (employee.isConsultant === 'true' ? "A-CS-" : "A-").concat(employee.employeementId);
          employee.employeeType = ((employee.isApprenticeship === 'true') ? 'Apprentice' : ((employee.isConsultant === 'true') ? 'Consultant': 'Regular')),
          employee.profileCompletedPercent = employee.profileCompletedPercent + "%";
          employee.dateOfBirth = (employee.dateOfBirth) ? moment(employee.dateOfBirth).format(AppComponent.DATE_FORMAT) : null;
          employee.dateOfJoining = (employee.dateOfJoining) ? moment(employee.dateOfJoining).format(AppComponent.DATE_FORMAT) : null;
          employee.createdOn = (employee.createdOn) ? moment(employee.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          employee.updatedOn = (employee.updatedOn) ? moment(employee.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
        });
        //console.log("allEmployeeList : ", this.allEmployeeList)
      } else {
        alert(response.serviceResponse)
      }
    });
  }

  getCustomEmployeesList(queryObjList: any, template: TemplateRef<any>) {
    this.allEmployeeList = [];

    let queryObj = new Query();
    queryObj.queryList = queryObjList;

    if (queryObjList == '') {
      this.getAllEmployeeList();
    } else {
      this.employeeService.customQueryForEmployeeReport(queryObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allEmployeeList = response.serviceResponse;

          // this.allEmployeeList = this.allEmployeeList.filter((value, index, self) =>
          //   index === self.findIndex((t) => (
          //     t.employeementId === value.employeementId
          //   ))
          // )

          if (this.allEmployeeList.length == 0) {
            this.openAlertMod(this.alertModal, "No Data found")
          }
          this.allEmployeeList.forEach(employee => {
            employee.employeementId = "A-".concat(employee.employeementId);
            // employee.employeementId = (employee.isConsultant === 'true' ? "A-CS-" : "A-").concat(employee.employeementId);
            employee.employeeType = ((employee.isApprenticeship === 'true') ? 'Apprentice' : ((employee.isConsultant === 'true') ? 'Consultant': 'Regular')),
            employee.profileCompletedPercent = employee.profileCompletedPercent + "%";
            employee.dateOfBirth = (employee.dateOfBirth) ? moment(employee.dateOfBirth).format(AppComponent.DATE_FORMAT) : null;
            employee.dateOfJoining = (employee.dateOfJoining) ? moment(employee.dateOfJoining).format(AppComponent.DATE_FORMAT) : null;
            employee.createdOn = (employee.createdOn) ? moment(employee.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
            employee.updatedOn = (employee.updatedOn) ? moment(employee.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
          });
          //console.log("allEmployeeList : ", this.allEmployeeList)
        } else {
          this.openAlertMod(template, response.serviceResponse)
        }
      });
    }
  }

  // ACL Start

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

            // segregating jobroles by department
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
            //console.log(this.finalColumns, " : final arr");

            // Generate Template
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
            //console.log("paginateData : ", this.paginateData);

            this.personaWiseJobRole.forEach((role) => {
              this.employeeObj.jobRoleId = role.jobRoleId;
              this.jobRoleService.getMappedSubFeatureList(this.employeeObj).pipe(first()).subscribe((response: any) => {
                if (response.serviceStatus == "Success") {
                  const mappedSubFeatures = response.serviceResponse;
                  //console.log("mappedSubFeatures : ", mappedSubFeatures);
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
    //console.log(this.updatedRoleSubFeature, " :   updatedRoleSubFeature");
  }

  updateJobRoleSubFeatureMapping(template: TemplateRef<any>) {
    let jobRoleObj = new JobRole();
    jobRoleObj.updatedJobRoleFeatureMapping = this.updatedRoleSubFeature;

    this.jobRoleService.updateJobRoleSubFeatureMapping(jobRoleObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
      } else {
        this.openAlertMod(template, response.serviceResponse);
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

  toggleAccessList(event?:any){

    if(this.isAccessFeatureMapping == false && this.isDefaultFeatureMapping == false){
      this.isAccessFeatureMapping = true;
    }

    if(event.target.checked){
      this.isAccessFeatureMapping = false;
      this.isDefaultFeatureMapping = true;
      this.showDefaultMappingTable();
    }else{
      this.isAccessFeatureMapping = true;
      this.isDefaultFeatureMapping = false;
    }
  }

  getDefaultMapping(template: TemplateRef<any>){

    this.jobRoleService.getDefaultMapping().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.defaultMappingList = response.serviceResponse;

        this.defaultMappingList.forEach((object) => {
          const formattedPermissions = object.permissionList.reduce((permissions, permission) => {

            if(permission.permission == "N"){
              permission.permission = false;
            }else if(permission.permission == "Y"){
              permission.permission = true;
            }
            permissions[permission.employeeRole] = permission.permission;
            return permissions;
          }, {});

          object.permissionList = formattedPermissions;
        });

        this.processData();
        //console.log( this.defaultMappingList , " :  this.defaultMappingList ");
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  selectDefaultMapCellCheckbox(subFeatureId:any, isAssigned:any ,roleName:any, subFeatureName:any){
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
    //console.log(this.updateDefaultMapping, " :   updateDefaultMapping");
  }

  updateDefaultFeatureMapping(template: TemplateRef<any>){
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
      const tabComp = a.tabName.localeCompare(b.tabName);
      return tabComp ? tabComp : a.featureName.localeCompare(b.featureName);
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

    //console.log(this.defaultMappingListFilter,  " : defaultMappingListFilter");
  }

  // ACL end

  /* Filter */
  openFilterModal(template: TemplateRef<any>, columns: any[], title: any) {
    //console.log("columns : ", columns);
    this.queryList = [];

    this.filterData.title = title;
    this.filterData.columns = columns;
    const today = new Date();
    const yesterday = new Date();
    yesterday.setDate(today.getDate() - 1);
    const formattedToday = formatDate(today, 'dd-MM-yyyy', 'en-US');
    const formattedYesterday = formatDate(yesterday, 'dd-MM-yyyy', 'en-US');
    if(this.leaveReportFlag==true){
      this.queryList = [
        { column: "Employment Status", operator: "!=", value: "InActive", conjunction: "" },
        {column: "From Date", operator: ">=",value: formattedYesterday, conjunction: "" },
        {column: "To Date", operator: "<=", value: formattedToday, conjunction: "" }
      ];
    }
    else if(this.timesheetReportFlag==true){
      this.queryList = [
        { column: "Employment Status", operator: "!=", value: "InActive", conjunction: "" },
        {column: "Date", operator: "=", value: formattedYesterday, conjunction: "" },
        {column: "Date", operator: "=", value: formattedToday, conjunction: "" }
      ];
    }else{
      this.queryList = [
        { column: "Employment Status", operator: "!=", value: "InActive", conjunction: "" }
      ];
    }
    this.storedDataList.forEach((data) => {
      if (data.filterName == title) {
        data.queryList.forEach((queryObj) => {
          if(queryObj.column == "Employee Id" && !queryObj.value.includes("A-")){
            queryObj.value = "A-".concat(queryObj.value);
          }
          // if(queryObj.column == "Employee Id" && !queryObj.value.includes("A-") && (data.isConsultant == 'true')){
          //   queryObj.value = "A-CS-".concat(queryObj.value);
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

    //console.log("filterData : ", this.filterData);
    this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
  }

  onFilterSubmit(emittedArray: any, template: TemplateRef<any>) {
    if (emittedArray[0].length != 0) {
      //console.log("queryList : ", emittedArray[0]);
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
        
        if (query.column == 'Employee Id') {
          query.value = query.value.split("-")[1];
        }
      });

      if (this.filterData.title == 'Filter Leave Report') {
        this.getCustomLeaveApplicationsList(emittedArray[0], template);
      }
      if (this.filterData.title == 'Filter Employee Report') {
        this.getCustomEmployeesList(emittedArray[0], template);
      }
      if (this.filterData.title == 'Filter Timesheet Report') {
        this.getCustomTimesheetApplicationsList(emittedArray[0], template);
      }
    }else{
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

  // Leave Timesheet Report
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
    //console.log("timesheet obj  : ", timesheetObj)

    this.timesheetService.getAllLeaveTimesheetsWithoutLeaveApplication(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allLeaveTimesheets = response.serviceResponse;

        for (let x of this.allLeaveTimesheets) {
          x.employeementId = "A-".concat(x.employeementId);
          // x.employeementId = (x.isConsultant === 'true' ? "A-CS-" : "A-").concat(x.employeementId);
          x.employeeType = ((x.isApprenticeship === 'true') ? 'Apprentice' : ((x.isConsultant === 'true') ? 'Consultant': 'Regular')),
          x.date = (x.date) ? moment(x.date).format(AppComponent.DATE_FORMAT) : null;
          x.createdOn = (x.createdOn) ? moment(x.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          x.updatedOn = (x.updatedOn) ? moment(x.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
        }

        //console.log("allLeaveTimesheets :", this.allLeaveTimesheets);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }


  // Custom Query Data 
  getCustomQueryData(template: TemplateRef<any>) {    
    this.customQuery = this.customQuery?.trim().replace(/\s{2,}/g,' ');
    if(!this.validationService.validateNullUndefinedEmptyString(this.customQuery)){
      this.alertMessage = "Please enter custom query !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    let queryObj = new Query();
    queryObj.customQuery = this.customQuery;

    this.utilityService.getCustomQueryData(queryObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        let responseData = response.serviceResponse;
        //console.log("responseData : ", responseData);

        if(responseData){
          let exportData = responseData.map((dataArr) => {
            let dataObj = {};
            dataArr.forEach((data,index) => {
              dataObj[index] = data;
            });

            return dataObj
          });
          
          const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(exportData, {skipHeader: true});
          const book: XLSX.WorkBook = XLSX.utils.book_new();
          XLSX.utils.book_append_sheet(book, worksheet, 'Sheet1');
          XLSX.writeFile(book, "CustomQueryData.xlsx");
        }else{
          this.alertMessage = "Please Enter Valid Query !!";
          this.openAlertMod(template, this.alertMessage);
        }

      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  //pagination 	
  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  // Excel Export 
  exportToExcel(): void {

    if (this.isLeaveReportTable == true) {
      this.excelName = 'leaveReport.xlsx';

      const onlySpecificDataArr = this.allLeaveApplicationsList.map(
        x => ({
          "Employee Id": x.employeementId,
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
      // this.excelName = 'EmployeeReport.xlsx';

      if(!this.showDetails){
        this.excelName = 'EmployeeReport.xlsx';

        const onlySpecificDataArr = this.allEmployeeList.map(
          x => ({
            "Employee Id": x.employeementId,
            "Employee Type": x.employeeType,
            "Full Name": x.name,		
            "Email Id": x.email,		
            "Employment Status": x.employmentstatus,
            "Date Of Joining": x.dateOfJoining,
            "Department" : x.departmentName,
            "Billable" : x.billable,
            "Billable Type" : x.billableType,
            "Client Name" : x.clientName,
            "Team Name":x.teamName,
            "Project Name" : x.projectName,
            "Po No" : x.poNo,
            "Po Start Date" : x.poStartDate,
            "Po End Date" : x.poEndDate,
            "Po Project Type" : x.poProjectType,
            "Aadhar":x.aadhar,
            "About Me":x.aboutMe,
            "address":x.address,
            "permanentAddress":x.permanentAddress,
            "city":x.city,
            "Manager Name":x.managerName,
            "Blood Group":x.bloodGroup,
            "date Of Birth":x.dateOfBirth,
            "gender":x.gender,
            "fatherName":x.fatherName,
            "mobileNo":x.mobileNo,
            "panNumber":x.panNumber,
            "placeOfBirth":x.placeOfBirth,
            "workLocation":x.workLocation,
            "Probation Period":x.probationPeriod,
            "noticePeriod":x.noticePeriod,
            "country":x.country,
            "emergencyContactMobile":x.emergencyContactMobile,
            "emergencyContactPerson":x.emergencyContactPerson,
            "landline":x.landline,
            "maritalStatus":x.maritalStatus,
            "motherTongue":x.motherTongue,
            "alternateMobileNo":x.alternateMobileNo,
            "pincode":x.pincode,
            "relation":x.relation,
            "State":x.state,
            "viewsOnOrganisation":x.viewsOnOrganisation,
            "passportNumber":x.passportNumber,
            "bankAccountNo":x.bankAccountNo,
            "bankIFSCCode":x.bankIFSCCode,
            "bankName":x.bankName,
            "pfAccountNumber":x.pfAccountNumber,
            "previousPfAccountNumber":x.previousPfAccountNumber,
            "uan":x.uan,
            "esicNumber":x.esicNumber,
            "graduationType":x.graduationType,
            "pursuing":x.pursuing,
            "passingGrade":x.passingGrade,
            "yearOfPassing":x.yearOfPassing,
            "createdBy":x.createdBy,
            "createdOn":x.createdOn, 
            "Proile Completion Perecentage":x.profileCompletedPercent,
          })
        )
        this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName);
      }else{
        this.excelName = 'EmployeeDetailedReport.xlsx';

        const onlySpecificDataArr = this.filteredEmployees.map(
          x => ({
            "Employee Id":x.employeementId,
            "Full Name":x.name,
            "Department":x.departmentName,
            "Job Role":x.jobRole,
            "Manager":x.managerName,
            "Mobile Number":x.mobileNo,
            "Email Id":x.email,
            "Employment Status":x.employmentstatus,
            "Billable":x.billable,
            "Billable Type":x.billableType,
            "Team Name":x.teamName,
            "Project Name":x.projectName,
            "Po No":x.poNo,
            "Po Type":x.poType,
            "Po Start Date":x.poStartDate,
            "Po End Date":x.poEndDate,
            "Effective Start Date":x.effectiveStartDate,
            "Effective End Date":x.effectiveEndDate,
            "Client Name":x.clientName,
            "Client Location":x.clientLocation,
            "Work Location":x.workLocation,
            "Experience":x.totalExperience,

          })
        )
        this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName);
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
        if(data.department){
          if(Object.values(departments).includes(data.department)){
            departments[column] = "";
          }else{
            departments[column] = data.department;
          }
        }else{
          departments[column] = "";
        }

        if(data){
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

      const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(onlySpecificDataArr, {skipHeader:true});
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

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
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

  getAllEmployeesReportByProjectType() {
    this.allEmployee = [];

      this.employeeService.getAllEmployeesReportByProjectType().pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allEmployee = response.serviceResponse;
          this.filterEmployees();
           console.log("d nbdfh",this.allEmployee);
        } else {
          
        }
      });
    }



  }




function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
}
