import { DatePipe, LocationStrategy } from '@angular/common';
import { Component, OnInit, TemplateRef,ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Feature } from 'src/app/models/feature';
import { Holiday } from 'src/app/models/holiday';
import { Leave } from 'src/app/models/leave';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { HolidayService } from 'src/app/services/holiday.service';
import { LeaveService } from 'src/app/services/leave.service';
import { UtilityService } from 'src/app/services/utility.service';
import { ValidationService } from 'src/app/services/validation.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { DepartmentService } from 'src/app/services/department.service';
import { Department } from 'src/app/models/department';
import { Employee } from 'src/app/models/employee';
import { LeaveExcludeInclude } from 'src/app/models/LeaveExcludeInclude';

@Component({
  selector: 'app-leave-config',
  templateUrl: './leave-config.component.html',
  styleUrls: ['./leave-config.component.css']
})
export class LeaveConfigComponent implements OnInit {
  data: string;

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;
  _holidayList:any;

  //flags 
  isCreation: boolean = false;
  isUpdation: boolean = false;

  isHolidayForm: boolean = false;
  isLeaveTypeForm: boolean = false;
  isLeaveBalanceForm: boolean = false;
  isHolidayTable: boolean = false;
  isLeaveRuleTable: boolean = false;
  isLeavePolicyForm: boolean = false;
  isLeavePolicyTable: boolean = false;


  //modal 
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();

  //excel
  excelName = '';
  holidayDataForExcel: any[];
  leaveDataForExcel: any[];
  leavePolicyDataForExcel: any[];

  //obj
  feature = "Leave Config";
  currentUser: User;
  userMapping: any = {};

  holidayObj: Holiday = new Holiday();
  holidayList: any[] = [];
  holidayListFilter: any[] = [];

  leaveTypeObj: Leave = new Leave();
  leaveTypes: any[] = [];
  filterLeaveType: any[] = [];
  newLeaveType: any;
  oldLeaveType: any;
  selectedYear: any;

  leaveBalanceObj: Leave = new Leave();
  leaveBalanceList: any[] = [];
  employeeData: any[] = [];

  leavePolicyObj: Leave = new Leave();
  leavePolicyList: any[] = [];

  years: any[] = [];
  selectedYearholidayList: any[] = [];

  allStates: any[] = [
    "Andaman & Nicobar Islands",
    "Andhra Pradesh",
    "Arunachal Pradesh",
    "Assam",
    "Bihar",
    "Chandigarh",
    "Chhattisgarh",
    "Dadra and Nagar Haveli and  Daman & Diu",
    "Delhi",
    "Goa",
    "Gujarat",
    "Haryana",
    "Himachal Pradesh",
    "Jammu & Kashmir",
    "Jharkhand",
    "Karnataka",
    "Kerala",
    "Ladakh",
    "Lakshadweep",
    "Madhya Pradesh",
    "Maharashtra",
    "Manipur",
    "Meghalaya",
    "Mizoram",
    "Nagaland",
    "Odisha",
    "Puducherry",
    "Punjab",
    "Rajasthan",
    "Sikkim",
    "Tamil Nadu",
    "Telangana",
    "Tripura",
    "Uttar Pradesh",
    "Uttarakhand",
    "West Bengal",
  ]

  // for View Holidays by State 
  selectedState: any = '';
  selectedHolidayType: any = '';

  filters:any = {};
  isSearchEnabled:boolean = false;
  holidayColumns:any[] = ['blank', 'occasion','dayOfTheWeek','dateOfHoliday','state','createdOn', 'createdbyName', 'updatedOn', 'updatedByName'];
  leaveTypeColumns:any[] = ['leaveType', 'leaveTypeCode', 'gender', 'noOfDays','rules', 'updatedOn', 'updatedByName', 'description'];
  leavePolicyColumns:any[] = ['blank','leavePolicyName','leaveType','description','createdByName','createdOn','updatedOn','updatedByName'];
  empExcludeColumns = [
  { column: 'blank', value: '' },
  { column: 'name', value: '' },
  { column: 'empId', value: '' },
  { column: 'jobRoleId', value: '' },
  { column: 'deptId', value: '' },
  { column: 'projectName', value: '' },
  { column: 'billableType', value: '' },
  { column: 'blank', value: '' },
  { column: 'blank', value: '' },
  { column: 'blank', value: '' }
];
  employeesFor360: any[] = [];
  tableName: string;

  //leave exclusion
  isEmpLeaveExclusion: boolean=false;
  departmentList: any[] = [];
  selectedDepartments: number[] = [];
  originalDepartmentList: any[] = [];
  deptSearch: string = '';
  isAllSelected: boolean = false;
  employeeListByDept: any[] = [];
  selectedEmployees: any[] = [];
  employeeSearch: string = '';
  showEmployeeDropdown: boolean=false;
  isInclude: boolean=false;
  isExclude: boolean=false;
  @ViewChild("leaveIncludeExclude_Consent")
  leaveIncludeExcludeConsent:TemplateRef<any>
  selectedAction: String = '';
  @ViewChild("alert_message")alertTemplate:TemplateRef<any>
  show: number = -1;
  searchFilters: any = {};
  excludeApiFlag:boolean=false;

  //pagination
  page1: number = 1;
  totalItems:number = 0;
  pageSize:number = 10;
  sortColumn1 = '';
    
  constructor(
    private validationService: ValidationService,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private holidayService: HolidayService,
    private datePipe: DatePipe,
    private leaveService: LeaveService,
    private exportExcelService: ExportExcelService,
    private locationStrategy: LocationStrategy,
    private utilityService: UtilityService,
    private employeeService: EmployeeService, 
    private departmentService: DepartmentService,   
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  async ngOnInit(): Promise<void> {
   

    // Dynamic Subfeature Flags 
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    console.log(this.feature, this.userMapping);

    this.sectionViewInit();
    this.preventBackButton();
    this.dynamicYearForDropdown();
    this.showHoliaysTable();
  }
  preventBackButton() {
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(() => {
      history.pushState(null, null, location.href);
    })
  }

  sectionViewInit() {
    // if (this.userMapping.add_holidays) {
    //   this.showAddHolidayForm();
    // } else if (this.userMapping.view_holidays || this.userMapping.update_holidays || this.userMapping.delete_holiday) {
    //   this.showHoliaysTable();
    // } else if (this.userMapping.add_leave_type) {
    //   this.showAddLeaveTypeForm();
    // } else if (this.userMapping.view_leave_types || this.userMapping.update_leave_type || this.userMapping.delete_leave_type) {
    //   this.showLeaveTypesTable();
    // } else if (this.userMapping.manage_employee_leave_balance) {
    //   this.showLeaveBalanceForm();
    // } else if (this.userMapping.add_leave_policy) {
    //   this.showAddLeavePolicyForm();
    // } else if (this.userMapping.view_leave_policies || this.userMapping.update_leave_policy || this.userMapping.delete_leave_policy) {
    //   this.showLeavePoliciesTable();
    // }

    if (this.userMapping.view_holidays || this.userMapping.update_holidays || this.userMapping.delete_holiday) {
      this.showHoliaysTable();
    } else if (this.userMapping.view_leave_types || this.userMapping.update_leave_type || this.userMapping.delete_leave_type) {
      this.showLeaveTypesTable();
    } else if (this.userMapping.manage_employee_leave_balance) {
      this.showLeaveBalanceForm();
    } else if (this.userMapping.view_leave_policies || this.userMapping.update_leave_policy || this.userMapping.delete_leave_policy) {
      this.showLeavePoliciesTable();
    }else if(this.userMapping.view_emp_leave_exclusion){
      this.showEmpLeaveExclusion();
    }


  }

  disableMannualDateInput() {
    return false;
  }

  showAddHolidayForm() {
    this.employeeData = []
    this.isHolidayForm = true;
    this.isCreation = true;

    this.isLeaveTypeForm = false;
    this.isLeaveBalanceForm = false;
    this.isHolidayTable = false;
    this.isLeaveRuleTable = false;
    this.isLeavePolicyTable = false;
    this.isLeavePolicyForm = false;
    this.isUpdation = false;

    this.reset();
    setTimeout(this.setCurrentYearLimit, 500);
  }

  async showHoliaysTable() {
    
    this.employeeData = []
    this.isHolidayTable = true;

    this.isLeaveRuleTable = false;
    this.isHolidayForm = false;
    this.isLeaveTypeForm = false;
    this.isLeaveBalanceForm = false;
    this.isLeavePolicyForm = false;
    this.isLeavePolicyTable = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.page = 1;
    this.data = ''
    this.filters = {};
    this.isSearchEnabled = false;
    this.isEmpLeaveExclusion=false;  

    this.getAllHolidays();
    
  }

  showLeaveTypesTable() {
    this.employeeData = []
    this.isLeaveRuleTable = true;

    this.isHolidayTable = false;
    this.isHolidayForm = false;
    this.isLeaveTypeForm = false;
    this.isLeaveBalanceForm = false;
    this.isLeavePolicyForm = false;
    this.isLeavePolicyTable = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.page = 1;
    this.data = ''
    this.filters = {};
    this.isSearchEnabled = false;
    this.isEmpLeaveExclusion=false;  

    this.getAllLeaveTypes();
  }

  showAddLeaveTypeForm() {
    this.employeeData = []
    this.isLeaveTypeForm = true;
    this.isCreation = true;

    this.isHolidayForm = false;
    this.isLeaveBalanceForm = false;
    this.isHolidayTable = false;
    this.isLeaveRuleTable = false;
    this.isLeavePolicyForm = false;
    this.isLeavePolicyTable = false;
    this.isUpdation = false;
    this.isEmpLeaveExclusion=false;  
    this.reset();
  }


  showUpdateLeaveTypeForm(leaveType: Leave) {
    this.isLeaveTypeForm = true;
    this.isUpdation = true;

    this.isHolidayForm = false;
    this.isLeaveBalanceForm = false;
    this.isHolidayTable = false;
    this.isLeaveRuleTable = false;
    this.isLeavePolicyForm = false;
    this.isLeavePolicyTable = false;
    this.isCreation = false;
    this.isEmpLeaveExclusion=false;  
    this.leaveTypeObj = Object.assign({}, leaveType);
  }

  showUpdateHolidayForm(holiday: Holiday) {
    this.isHolidayForm = true;
    this.isUpdation = true;

    this.isLeaveTypeForm = false;
    this.isLeaveBalanceForm = false;
    this.isHolidayTable = false;
    this.isLeaveRuleTable = false;
    this.isLeavePolicyForm = false;
    this.isLeavePolicyTable = false;
    this.isCreation = false;
    this.isEmpLeaveExclusion=false;  
    this.holidayObj = Object.assign({}, holiday);
    this.holidayObj.optionalHoliday = (holiday.optionalHoliday != null) ? JSON.parse(holiday.optionalHoliday) : false;
    this.holidayObj.customHoliday = (holiday.customHoliday != null) ? JSON.parse(holiday.customHoliday) : false;
    this.holidayObj.dateOfHoliday = (this.holidayObj.dateOfHoliday)? moment(this.holidayObj.dateOfHoliday, AppComponent.DATE_FORMAT).format(AppComponent.DB_DATE_FORMAT) : '';

    setTimeout(this.setCurrentYearLimit, 500);
  }

  showLeaveBalanceForm() {
    this.isLeaveBalanceForm = true;

    this.isLeaveTypeForm = false;
    this.isHolidayForm = false;
    this.isHolidayTable = false;
    this.isLeaveRuleTable = false;
    this.isLeavePolicyForm = false;
    this.isLeavePolicyTable = false;
    this.isCreation = false;
    this.isUpdation = false;
    this.isEmpLeaveExclusion=false;  
    this.reset();
  }

  showAddLeavePolicyForm() {
    this.employeeData = []
    this.isLeavePolicyForm = true;
    this.isCreation = true;

    this.isHolidayForm = false;
    this.isLeaveTypeForm = false;
    this.isLeaveBalanceForm = false;
    this.isHolidayTable = false;
    this.isLeaveRuleTable = false;
    this.isLeavePolicyTable = false;
    this.isUpdation = false;
    this.isEmpLeaveExclusion=false;  
    this.reset();
    this.getAllLeaveTypes();
  }

  showUpdateLeavePolicyForm(leavePolicyObj: Leave) {
    this.isLeavePolicyForm = true;
    this.isUpdation = true;

    this.isHolidayForm = false;
    this.isLeaveTypeForm = false;
    this.isLeaveBalanceForm = false;
    this.isHolidayTable = false;
    this.isLeaveRuleTable = false;
    this.isLeavePolicyTable = false;
    this.isCreation = false;
    this.isEmpLeaveExclusion=false;  
    this.leavePolicyObj = Object.assign({}, leavePolicyObj);
    //console.log("this.leavePolicyObj ",this.leavePolicyObj);
    
    this.getAllLeaveTypes();
  }


  showLeavePoliciesTable() {
    this.employeeData = []
    this.isLeavePolicyTable = true;

    this.isHolidayTable = false;
    this.isHolidayForm = false;
    this.isLeaveTypeForm = false;
    this.isLeaveRuleTable = false;
    this.isLeaveBalanceForm = false;
    this.isLeavePolicyForm = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.page = 1;
    this.data = '';
    this.filters = {};
    this.isSearchEnabled = false;
    this.isEmpLeaveExclusion=false;  
    this.getAllLeavePolicies();
  }

   showEmpLeaveExclusion() {
    this.isEmpLeaveExclusion=true;  
    this.sortColumn=[];
    this.sortColumnType=[];
    this.sortDirection='';
    this.isLeavePolicyForm = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.page=1;
    this.data='';
    this.filters = {};
    this.isLeavePolicyTable = false;
    this.isHolidayTable = false;
    this.isHolidayForm = false;
    this.isLeaveTypeForm = false;
    this.isLeaveRuleTable = false;
    this.isLeaveBalanceForm = false;
    this.isLeavePolicyForm = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.isSearchEnabled = false;
    this.resetExcludeFlags();
    this.getAllDepartmentList();
  }

  getAllDepartmentList() {
  this.departmentService.getAllDepartments().pipe(first()).subscribe({
    next: (response: any) => {
      if (response.serviceStatus === 'Success') {
        this.originalDepartmentList = response.serviceResponse || [];
      this.departmentList = this.originalDepartmentList.filter(
          (dept: any) => ![4, 10, 12, 14].includes(dept.deptId) );      
      } else {
        console.error('Failed to fetch departments:', response.serviceResponse);
      }
    },
    error: (err) => console.error('Error fetching department list:', err)
  });
}

filterDepartments() {
  const value = this.deptSearch.toLowerCase().trim();
  this.departmentList = value
    ? this.originalDepartmentList.filter(d => d.name.toLowerCase().includes(value))
    : [...this.originalDepartmentList]; 
}

toggleSelectAllDept() {
  if (this.isAllSelected) {
    this.selectedDepartments = [];
    this.isAllSelected = false;
  } else {
    this.selectedDepartments = this.departmentList.map(d => d.deptId);
    this.isAllSelected = true;
  }
}

clearSearch(event: Event): void {
  event.stopPropagation();
  this.deptSearch = '';
  this.filterDepartments();
  this.selectedDepartments = [];
  this.isAllSelected = false;

}

clearSelection(event: Event) {
  event.stopPropagation();
  this.selectedDepartments = [];
  this.isAllSelected = false;
}

onDepartmentSelectionChange() {
  this.showEmployeeDropdown=false;
  this.isAllSelected = this.departmentList.every(d => this.selectedDepartments.includes(d.deptId));
}

onEmpSelectionChange() {
  this.isAllSelected = this.employeeListByDept.every(e => this.selectedEmployees.includes(e.empId));
}


  getAllEmployeesByDepartmentIds(departmentSelect?:any) {
    this.employeeListByDept = [];
    let empObj = new Employee();
    empObj.departmentList = this.selectedDepartments?.map(deptId => {
      let dept = new Department();
      dept.deptId = deptId;
      return deptId;
    });
    empObj.isEmpLeaveExclusion=this.isExclude;
    empObj.isEmpLeaveInclusion=this.isInclude;
    empObj.page=this.page1 - 1
    empObj.size=this.pageSize
    empObj.sortColumn = (this.sortColumn1 && this.sortColumn1.trim() !== '') ? this.sortColumn1 : 'name';
    empObj.sortDirection = (this.sortDirection && this.sortDirection.trim() !== '') ? this.sortDirection : 'asc';
    empObj.filters=this.searchFilters 

    this.employeeService.getAllEmployeesByDepartmentIds(empObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.employeeListByDept = response.serviceResponse;
        this.employeeListByDept = this.employeeListByDept.sort((a, b) => a.name.localeCompare(b.name));
          this.showEmployeeDropdown=true;
          if(this.isExclude){
          this.totalItems = response.totalEle || 0;
          }else{this.totalItems =this.employeeListByDept.length}
          departmentSelect.close()
      } else {
        if(this.isInclude && !this.excludeApiFlag){
          this.openAlertMod(this.alertTemplate, "No Employee(s) is Excluded from Leave Week-off / Holiday!!");
        }else if(!this.excludeApiFlag){
          this.showEmployeeDropdown=false;
          this.openAlertMod(this.alertTemplate, response.serviceResponse);
        }
        this.excludeApiFlag=false;
        departmentSelect.close()
      }
    });
    this.searchFilters={};
  }

  filterEmployees() {
  if (this.employeeSearch && this.employeeSearch.trim() !== '') {
    this.employeeListByDept = this.employeeListByDept.filter(emp =>
      emp.empName.toLowerCase().includes(this.employeeSearch.toLowerCase())
    );
  } else {
    this.getAllEmployeesByDepartmentIds();
  }
}

getEmpIdToExcludeFromLeave() {
  this.modalRef.hide();
  let leaveObj = new LeaveExcludeInclude();
  leaveObj.createdBy=this.currentUser.empId;
  leaveObj.isExclude=this.isExclude
  leaveObj.isInclude=this.isInclude
  leaveObj.empIds=this.selectedEmployees
  this.leaveService.getEmpIdToExcludeIncludeFromLeave(leaveObj).pipe(first()).subscribe({
    next: (response: any) => {
      if (response.serviceStatus === 'Success') {
       this.selectedEmployees=[];
       this.excludeApiFlag=true;
       this.getAllEmployeesByDepartmentIds();
       this.openAlertMod(this.alertTemplate, response.serviceResponse);
      } else {
        console.error('Failed to fetch departments:', response.serviceResponse);
      }
    },
    error: (err) =>       
       this.openAlertMod(this.alertTemplate,"Error Excluding the Employees. Try After Sometime!!")
  });
}

resetExcludeFlags(){
  this.showEmployeeDropdown=false;
  this.isInclude=false;
  this.isExclude=false;
  this.selectedEmployees=[];
  this.selectedDepartments=[];
  this.searchFilters = {};
  this.page1= 1;
  this.totalItems = 0;
  this.pageSize = 10;
  this.sortColumn1 = '';
}

userSelection(action:String,consent? :any){
  this.selectedAction = action;
  if(action=='Include'){
    this.isInclude=true;
    this.isExclude=false;
    this.selectedDepartments=[];
    this.selectedEmployees=[];
    this.searchFilters = {};
    this.showEmployeeDropdown=false;
    this.page1= 1;
    this.totalItems = 0;
    this.pageSize = 10;
    this.sortColumn1 = '';
  }else if(action=='Exclude'){
    this.selectedDepartments=[];
    this.selectedEmployees=[];
    this.showEmployeeDropdown=false;
    this.searchFilters = {};
    this.isInclude=false;
    this.isExclude=true;
    this.page1= 1;
    this.totalItems = 0;
    this.pageSize = 10;
    this.sortColumn1 = '';

  }

  if(consent){
    this.openAlertMod(this.leaveIncludeExcludeConsent,"")  
  }
}

onCheckboxChange(emp: any) {
  if (emp.selected) {
    if (!this.selectedEmployees.includes(emp.empId)) {
      this.selectedEmployees.push(emp.empId);
    }
  } else {
    this.selectedEmployees = this.selectedEmployees.filter(id => id !== emp.empId);
  }

  this.onEmpSelectionChange();
}

toggleSelectAll(event: any) {
  const checked = event.target.checked;
  this.employeeListByDept.forEach(emp => emp.selected = checked);
  this.selectedEmployees = checked
    ? this.employeeListByDept.map(emp => emp.empId)
    : [];

  this.onEmpSelectionChange();
}


  reset() {
    this.holidayObj = new Holiday();
    this.holidayObj.optionalHoliday = false;
    this.holidayObj.customHoliday = false;
    this.holidayObj.state = '';
    this.selectedState = '';
    this.holidayList = [];

    this.leaveTypeObj = new Leave();
    this.leaveTypes = [];

    this.leaveBalanceObj = new Leave();
    this.leaveBalanceList = [];

    this.leavePolicyObj = new Leave();
    this.leavePolicyObj.employmentStatus = '';
    this.leavePolicyObj.leaveTypeMasterId = '';
    this.leavePolicyObj.leaveApplication = '';
    this.leavePolicyObj.increment = '';
    this.leavePolicyObj.oneTimeLeave = '';
    this.leavePolicyObj.carryForward = '';
    this.leavePolicyObj.expirationPeriod = '';
    this.leavePolicyObj.lockingPeriod = '';
    this.leavePolicyObj.probation = '';

    this.leavePolicyList = [];
  }

  dynamicYearForDropdown() {
    let currentYear = new Date().getFullYear();
    this.years = [];
    this.years.push(currentYear);
    for (var i = 1; i < 2; i++) {
      this.years.push(currentYear - i);
    }
    //console.log(this.years, "dynamic year");

  }

  checkLeaveType(leaveTypeObj:Leave, template: TemplateRef<any>){
    let leaveCheck = Object.assign({}, leaveTypeObj);
    //console.log("Leave Check : ", leaveCheck);
    this.leaveService.checkLeaveType(leaveCheck).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        // Valid Leave Type
      } else {
        this.openAlertMod(template, response.serviceResponse);
        this.leaveTypeObj.leaveType = "";
      }
    });
  }

  // Modals
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  openDeleteHoliday(template: TemplateRef<any>, holiday: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.holidayObj = holiday;
  }

  openDeleteLeavePolicy(template: TemplateRef<any>, leavePolicy: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.leavePolicyObj = leavePolicy;
  }

  openDeleteLeaveType(template: TemplateRef<any>, leaveType: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.leaveTypeObj = leaveType;
    //console.log(this.leaveTypeObj);
  }

  // Holiday
  onHolidayTypeSelected(){
    if(this.selectedHolidayType == ""){
      this.holidayListFilter = this.holidayList.filter((holiday:Holiday)=> moment(holiday.dateOfHoliday, "DD-MM-YYYY").year() == this.selectedYear);
    }else {
      this.holidayListFilter = this.holidayList.filter((holiday:Holiday)=> moment(holiday.dateOfHoliday, "DD-MM-YYYY").year() == this.selectedYear && holiday.holidayType == this.selectedHolidayType);
    }
    
    this.page = 1;
  }
  setCurrentYearLimit() {
    let currentYear = new Date().getFullYear();
    let currentMonth = new Date().getMonth();
    let nextYear;

    if (currentMonth == 10 || currentMonth == 11) {
      nextYear = currentYear + 1;
    } else {
      nextYear = currentYear;
    }

    let occasionDate = document.getElementById('occasionDate');
    occasionDate?.setAttribute('min', `${currentYear}-01-01`);
    occasionDate?.setAttribute('max', `${nextYear}-12-31`);
  }
  validateHolidayObj(holidayObj: Holiday, template: TemplateRef<any>) {

    if (holidayObj.holidayType == "Festival" || holidayObj.holidayType == "nonWorking") {

      if (!this.validationService.validateNullUndefinedEmptyString(holidayObj.holidayType)) {
        this.alertMessage = "Please select Holiday Type !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (!this.validationService.validateNullUndefinedEmptyString(holidayObj.occasion)) {
        this.alertMessage = "Please enter occasion Name !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      } else if (!this.validationService.validateAlphabeticCharacters(holidayObj.occasion)) {
        this.alertMessage = "Please enter valid occasion Name !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (!this.validationService.validateNullUndefinedEmptyString(holidayObj.dateOfHoliday)) {
        this.alertMessage = "Please select Date of Holiday !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (!this.validationService.validateNullUndefinedEmptyString(holidayObj.state)) {
        this.alertMessage = "Please select State !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      return true;

    } else if (holidayObj.holidayType == "WeekOff") {

      if (!this.validationService.validateNullUndefinedEmptyString(holidayObj.holidayType)) {
        this.alertMessage = "Please select Holiday Type !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (!this.validationService.validateNullUndefinedEmptyString(holidayObj.dateOfHoliday)) {
        this.alertMessage = "Please select Date of Holiday !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      return true;
    }
  }

  setHolidayWeekDay() {
    let weekDay = '';
    let day = new Date(this.holidayObj.dateOfHoliday).getDay();

    switch (day) {
      case 0: {
        weekDay = 'Sunday';
        break;
      }
      case 1: {
        weekDay = 'Monday';
        break;
      }
      case 2: {
        weekDay = 'Tuesday';
        break;
      }
      case 3: {
        weekDay = 'Wednesday';
        break;
      }
      case 4: {
        weekDay = 'Thursday';
        break;
      }
      case 5: {
        weekDay = 'Friday';
        break;
      }
      case 6: {
        weekDay = 'Saturday';
        break;
      }

      default: {
        weekDay = '';
        break;
      }
    }
    this.holidayObj.dayOfTheWeek = weekDay;
  }

  onAddHoliday(template: TemplateRef<any>) {

    let inputValidated: boolean = this.validateHolidayObj(this.holidayObj, template)
    if (!inputValidated) return;

    // this.holidayObj.dateOfHoliday = this.datePipe.transform(this.holidayObj.dateOfHoliday, 'dd-MM-yyyy');
    this.holidayObj.createdBy = this.currentUser.empId;
    //console.log("Add Holiday : ", this.holidayObj);
    this.holidayService.addHoliday(this.holidayObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
         this.openAlertMod(template, response.serviceResponse);
        this.showHoliaysTable();
        this.getAllHolidays();
        console.log("last in" ,this.holidayListFilter)
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onUpdateHoliday(template: TemplateRef<any>) {

    let inputValidated: boolean = this.validateHolidayObj(this.holidayObj, template)
    if (!inputValidated) return;

    // this.holidayObj.dateOfHoliday = this.datePipe.transform(this.holidayObj.dateOfHoliday, 'dd-MM-yyyy');
    this.holidayObj.updatedBy = this.currentUser.empId;
    //console.log("update Holiday : ", this.holidayObj);

    this.holidayService.updateHoliday(this.holidayObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showHoliaysTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }


  onDeleteHoliday(template: TemplateRef<any>) {
    this.cancelRequest();

    this.holidayService.deleteHoliday(this.holidayObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.getAllHolidays();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }


  onSelect() {
   // this.selectedHolidayType = "";
    if(this.selectedYear != null){
      if (this.selectedState == 'all state') {
        this.holidayListFilter = this.holidayList.filter((holiday:Holiday)=> moment(holiday.dateOfHoliday, "DD-MM-YYYY").year() == this.selectedYear);
        this.page = 1;
      } else {
        this.holidayListFilter = this.holidayList.filter((holiday:Holiday)=> (moment(holiday.dateOfHoliday, "DD-MM-YYYY").year() == this.selectedYear) && holiday.state == this.selectedState);
        this.page = 1;
      }
    }else{
      if (this.selectedState == 'all state') {
        this.holidayListFilter = this.holidayList;
        this.page = 1;
      } else {
        this.holidayListFilter = this.holidayList.filter(x => x.state == this.selectedState);
        this.page = 1;
      }
    }
  }

 

   getAllHolidays() {

    this.sortColumn=[];
    this.sortColumnType=[];
    this.sortDirection='';
    this.holidayList = [];
    this.holidayListFilter = [];
    this.data = ''

    // this.holidayObj.state=this.currentUser.workLocation;
    this.holidayService.getAllHoliday().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.holidayListFilter = response.serviceResponse;
        this.holidayList = response.serviceResponse;
        
        

        this.holidayListFilter.forEach(holiday => {
          holiday.dateOfHoliday = (holiday.dateOfHoliday) ? moment(holiday.dateOfHoliday).format(AppComponent.DATE_FORMAT) : null;
          holiday.createdOn = (holiday.createdOn) ? moment(holiday.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          holiday.updatedOn = (holiday.updatedOn) ? moment(holiday.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
          holiday.emp360CreatedBy =  holiday.createdBy;
          holiday.emp360UpdatedBy = holiday.updatedBy;
         
        });
        this._holidayList=this.holidayList;
        // this.changeEvent(this.currentUser.workLocation);
        this.selectedState='all';
        this.filterHolidayListByYear(new Date().getFullYear());
        this.selectedHolidayType = "Festival";
        this.onHolidayTypeSelected();
        
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  changeEvent(value:string){
    this.selectedHolidayType = "";
    if(value == 'all'){
      this.holidayList = this._holidayList.filter(x=> x.state == 'all');
    }else{
      this.holidayList = this._holidayList.filter((holiday:Holiday)=> (moment(holiday.dateOfHoliday, "DD-MM-YYYY").year() == this.selectedYear) && holiday.state == this.selectedState);
    }
  }


  filterHolidayListByYear(value: any) {
    this.selectedHolidayType = "";
    this.selectedYear = value;
    this.holidayListFilter = this.holidayList.filter((holiday:Holiday)=> moment(holiday.dateOfHoliday, "DD-MM-YYYY").year() == value);
    console.log(this.holidayListFilter, "this.holidayListFilter")
  }

  checkOccasion(template: TemplateRef<any>) {
    this.holidayObj.currentYear = new Date().getFullYear();
    
    this.holidayService.checkOccasionIfAlreadyExist(this.holidayObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Fail") {
        this.holidayObj.occasion = null;
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  // download excel
  exportToExcel(): void {

    if (this.isHolidayTable == true) {
      this.excelName = 'HolidaySheet.xlsx';

      this.holidayService.getAllHolidays(this.holidayObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.holidayDataForExcel = response.serviceResponse;
        }

        const onlySpecificDataArr = this.holidayDataForExcel.map(
          x => ({
            "Occasion": x.occasion,
            "Day": x.dayOfTheWeek,
            "Date": (x.dateOfHoliday) ? moment(x.dateOfHoliday).format(AppComponent.DATETIME_FORMAT) : null,
            "State": x.state
          })
        )
        this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
      });
    }
    if (this.isLeaveRuleTable == true) {
      this.excelName = 'LeaveSheet.xlsx';

      this.leaveService.getAllLeaveTypes().pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.leaveDataForExcel = response.serviceResponse;
        }

        const onlySpecificDataArr = this.leaveDataForExcel.map(
          x => ({
            "Leave Type": x.leaveType,
            "Leave Code": x.leaveTypeCode,
            "Employee Gender": x.gender,
            "Default Leaves": x.noOfDays,
            "Rules": x.rules,
            "Description": x.description
          })
        )
        this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
      });

    }
    if (this.isLeavePolicyTable == true) {
      this.excelName = 'LeavePolicySheet.xlsx';

      this.leaveService.getAllLeavePolicy().pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.leavePolicyDataForExcel = response.serviceResponse;
        }

        const onlySpecificDataArr = this.leavePolicyDataForExcel.map(
          x => ({
            "Leave Policy Name": x.leavePolicyName,
            "Leave Type": x.leaveType,
            "Description": x.description,
            "Created By": x.createdByName,
            "Created On": (x.createdOn) ? moment(x.createdOn).format(AppComponent.DATETIME_FORMAT) : null
          })
        )
        this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
      });

    }

  }
  // exportToExcel(id:any): void {
  //   if (this.isHolidayTable == true) {
  //     this.excelName = 'HolidaySheet.xlsx';
  //     this.tableName = 'Holiday Table';
  //     this.exportExcelService.exportTableFormat(id, this.excelName, this.tableName);
  //   }
  //   else if (this.isLeaveRuleTable == true) {
  //     this.excelName = 'LeaveSheet.xlsx';
  //     this.tableName = 'Leave Rules Table';
  //     this.exportExcelService.exportTableFormat(id, this.excelName, this.tableName);
  //   }
  //   else if (this.isLeavePolicyTable == true) {
  //     this.excelName = 'LeavePolicySheet.xlsx';
  //     this.tableName = 'Leave Policy Table';
  //     this.exportExcelService.exportTableFormat(id, this.excelName, this.tableName);
  //   }
  // }


  // Leave Type
  validateLeaveTypeObj(leaveTypeObj: Leave, template: TemplateRef<any>) {

    if (!this.validationService.validateNullUndefinedEmptyString(leaveTypeObj.leaveType)) {
      this.alertMessage = "Please enter Leave Type !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateAlphaWithSpace(leaveTypeObj.leaveType)) {
      this.alertMessage = "Please enter Valid Leave Type !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(leaveTypeObj.leaveTypeCode)) {
      this.alertMessage = "Please enter Leave Type Code !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateUppercaseAlpha(leaveTypeObj.leaveTypeCode)) {
      this.alertMessage = "Please enter Valid Leave Type Code, Only Uppercase Alphabets Allowed !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (leaveTypeObj.leaveTypeCode.length > 4) {
      this.alertMessage = "Please enter Valid Leave Type Code, Only upto 4 Characters Allowed !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }


    if (!this.validationService.validateNullUndefinedEmptyString(leaveTypeObj.gender)) {
      this.alertMessage = "Please Select Employee Gender !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(leaveTypeObj.noOfDays)) {
      this.alertMessage = "Please enter Default Leave Days !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    return true;
  }

  onAddLeaveType(template: TemplateRef<any>) {
    this.leaveTypeObj.leaveType = this.leaveTypeObj.leaveType?.trim();
    this.leaveTypeObj.leaveTypeCode = this.leaveTypeObj.leaveTypeCode?.trim();
    this.leaveTypeObj.description = this.leaveTypeObj.description?.trim();
    this.leaveTypeObj.rules = this.leaveTypeObj.rules?.trim();
    this.leaveTypeObj.createdBy = this.currentUser.empId;

    let inputValidated: boolean = this.validateLeaveTypeObj(this.leaveTypeObj, template)
    if (!inputValidated) return;

    let existingLeaveType = this.leaveTypes.find(leaveType => leaveType.leaveTypeCode == this.leaveTypeObj.leaveTypeCode)
    if (existingLeaveType) {
      this.openAlertMod(template, `Leave type against ${existingLeaveType.leaveTypeCode} Already Exist.`);
      return;
    }

    //console.log("Add Leave Type : ", this.leaveTypeObj);

    this.leaveService.createLeaveType(this.leaveTypeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showLeaveTypesTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onUpdateLeaveType(template: TemplateRef<any>) {
    //console.log("update Leave Type : ", this.leaveTypeObj);
    this.leaveTypeObj.updatedBy = this.currentUser.empId;

    this.leaveService.updateLeaveType(this.leaveTypeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showLeaveTypesTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  async getAllLeaveTypes() {
    
    this.sortColumn=[];
    this.sortColumnType=[];
    this.sortDirection='';
    this.leaveTypes = [];
    this.data = ''
    this.leaveService.getAllLeaveTypes().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveTypes = response.serviceResponse;
        //console.log("leaveTypes : ", this.leaveTypes);
        this.leaveTypes.forEach((leaveObj)=>{
          leaveObj.updatedOn = (leaveObj.updatedOn) ? moment(leaveObj.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
          leaveObj.createdOn = (leaveObj.createdOn) ? moment(leaveObj.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          leaveObj.emp360CreatedBy = leaveObj.createdBy;
          leaveObj.emp360UpdatedBy = leaveObj.updatedBy;
         
        })
        
      } else {
        console.error(response.serviceResponse);
      }
    });
  }
  
  
  onDeleteLeaveType(template: TemplateRef<any>, alertTemplate: TemplateRef<any>) {
    this.cancelRequest();

    this.filterLeaveType = this.leaveTypes.filter(x => x.leaveTypeMasterId !== this.leaveTypeObj.leaveTypeMasterId)
    this.oldLeaveType = this.leaveTypeObj.leaveTypeMasterId;

    this.leaveService.deleteLeaveType(this.leaveTypeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(alertTemplate, response.serviceResponse);
        this.showLeaveTypesTable();
      } else {
        this.leaveTypeObj.newLeaveTypeMasterId = '';
        this.modalRef = this.modalService.show(template);
      }
    });
  }

  validateLeaveTypeMappingObj(leaveTypeObj: Leave, template: TemplateRef<any>) {
    if (!this.validationService.validateNullUndefinedEmptyString(leaveTypeObj.newLeaveTypeMasterId)) {
      this.alertMessage = "Please select Leave Type !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    return true;
  }

  onChangeLeaveTypeMapping(template: TemplateRef<any>) {
    this.cancelRequest();
    let inputValidated: boolean = this.validateLeaveTypeMappingObj(this.leaveTypeObj, template)
    if (!inputValidated) return;

    this.leaveTypeObj.leaveTypeMasterId = this.leaveTypeObj.newLeaveTypeMasterId;
    this.leaveTypeObj.oldLeaveTypeMasterId = this.oldLeaveType;

    this.leaveService.changeLeaveTypeMapping(this.leaveTypeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {

        this.leaveTypeObj.leaveTypeMasterId = this.oldLeaveType;
        this.leaveService.deleteLeaveType(this.leaveTypeObj).pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus == "Success") {

            this.openAlertMod(template, response.serviceResponse);
            this.showLeaveTypesTable();
          } else {
            this.openAlertMod(template, response.serviceResponse);
          }
        });
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  fieldRestictCharacter(event) {
    var k;
    k = event.charCode;
    if ((k == 33) || (k == 34) || (k == 35) || (k == 36) || (k == 37) ||
      (k == 38) || (k == 39) || (k == 40) || (k == 41) || (k == 42) ||
      (k == 43) || (k == 44) || (k == 46) || (k == 47) || (k == 58) ||
      (k == 59) || (k == 60) || (k == 61) || (k == 62) || (k == 63) ||
      (k == 64) || (k == 66) || (k == 67) || (k == 68) || (k == 69) ||
      (k == 70) || (k == 71) || (k == 72) || (k == 73) || (k == 74) ||
      (k == 75) || (k == 76) || (k == 77) || (k == 78) || (k == 79) ||
      (k == 80) || (k == 81) || (k == 82) || (k == 83) || (k == 84) ||
      (k == 85) || (k == 86) || (k == 87) || (k == 88) || (k == 89) ||
      (k == 90) || (k == 91) || (k == 92) || (k == 93) || (k == 94) ||
      (k == 95) || (k == 96) || (k == 97) || (k == 98) || (k == 99) ||
      (k == 99) || (k == 100) || (k == 101) || (k == 102) || (k == 103) ||
      (k == 104) || (k == 105) || (k == 106) || (k == 107) || (k == 108) ||
      (k == 109) || (k == 110) || (k == 111) || (k == 112) || (k == 113) ||
      (k == 114) || (k == 115) || (k == 116) || (k == 117) || (k == 118) ||
      (k == 119) || (k == 120) || (k == 121) || (k == 122) || (k == 123) ||
      (k == 124) || (k == 125) || (k == 126)) {
      return (false);
    }
    return (true);
  }




  fieldRestictCharacterForLeave(event) {
    var k;
    var inputField = event.target; // Get the input field where the event is triggered

    // Restrict length to a maximum of 8 characters
    if (inputField.value.length >= 8) {
        // If the length is already 8 or more, prevent further input
        event.preventDefault();
        return false;
    }

    k = event.charCode;

    // Check if the character code matches any of the restricted characters
    if (
      (k == 33) || (k == 34) || (k == 35) || (k == 36) || (k == 37) ||
      (k == 38) || (k == 39) || (k == 40) || (k == 41) || (k == 42) ||
      (k == 43) || (k == 44) || (k == 46) || (k == 47) || (k == 58) ||
      (k == 59) || (k == 60) || (k == 61) || (k == 62) || (k == 63) ||
      (k == 64) || (k == 66) || (k == 67) || (k == 68) || (k == 69) ||
      (k == 70) || (k == 71) || (k == 72) || (k == 73) || (k == 74) ||
      (k == 75) || (k == 76) || (k == 77) || (k == 78) || (k == 79) ||
      (k == 80) || (k == 81) || (k == 82) || (k == 83) || (k == 84) ||
      (k == 85) || (k == 86) || (k == 87) || (k == 88) || (k == 89) ||
      (k == 90) || (k == 91) || (k == 92) || (k == 93) || (k == 94) ||
      (k == 95) || (k == 96) || (k == 97) || (k == 98) || (k == 99) ||
      (k == 100) || (k == 101) || (k == 102) || (k == 103) || (k == 104) ||
      (k == 105) || (k == 106) || (k == 107) || (k == 108) || (k == 109) ||
      (k == 110) || (k == 111) || (k == 112) || (k == 113) || (k == 114) ||
      (k == 115) || (k == 116) || (k == 117) || (k == 118) || (k == 119) ||
      (k == 120) || (k == 121) || (k == 122) || (k == 123) || (k == 124) ||
      (k == 125) || (k == 126)
    ) {
        return false; // Disallow the character if it's a restricted character
    }

    return true; // Allow the character if it's not restricted
}


fieldRestictCharacterForLeaveAccToDifferntEmployeeType(event) {
  const inputField = event.target;
  const value = inputField.value;
  const k = event.charCode;

  
  if (event.key === 'Backspace' || event.key === 'Delete' || event.key === 'ArrowLeft' || event.key === 'ArrowRight') {
    return true;
  }

  
  const prefixMatch = value.match(/^(A-|CS-|AP-)/);
  const digitsPart = prefixMatch ? value.replace(prefixMatch[0], '') : value;

  
  if (digitsPart.length >= 6 && /\d/.test(String.fromCharCode(k))) {
    event.preventDefault();
    return false;
  }

  return true;
}


fieldRestrictCharacterForEmployeeId(event: KeyboardEvent) {
  const input = (event.target as HTMLInputElement);
  const value = input.value;
  const key = event.key;

 
  // if (['Backspace', 'Delete', 'ArrowLeft', 'ArrowRight', 'Tab'].includes(key)) return;

 
  const validPrefix = value.startsWith('A-') || value.startsWith('AP-');
  const digitsOnly = value.replace(/^A-|^AP-/, '');

  if (!validPrefix && value.length < 3) {
  
    if (value === '' && key === 'A') return;
    if (value === 'A' && key === 'P') return;
    if (value === 'A' && key === '-') return;
    if (value === 'AP' && key === '-') return;
    event.preventDefault();
    return;
  }

 
  if (validPrefix) {
   
    if (!/^\d$/.test(key) || digitsOnly.length >= 6) {
      event.preventDefault();
    }
  } else {
    event.preventDefault();
  }
}


  // Manage Leave Balance
  onGetEmpLeaveBalance(template: TemplateRef<any>) {
    this.leaveBalanceList = [];
    this.employeeData = [];
    let leaveObj: Leave = new Leave();

     let empIdInput = this.leaveBalanceObj.employeementId;
    // if (this.leaveBalanceObj.employeementId.startsWith('A-')) {
    //   if (!this.validationService.validateNullUndefinedEmptyString(this.leaveBalanceObj.employeementId)) {
    //     this.alertMessage = "Please enter Employee ID !!"
    //     this.openAlertMod(template, this.alertMessage);
    //     return false;
    //   }
    //   leaveObj.employeementId = this.leaveBalanceObj.employeementId.substring(2);
    //   //console.log("Employee :", this.leaveBalanceObj);
    // } else {
    //   leaveObj.employeementId = this.leaveBalanceObj.employeementId
    // }

     if (!this.validationService.validateNullUndefinedEmptyString(empIdInput)) {
    this.alertMessage = "Please enter Employee ID !!";
    this.openAlertMod(template, this.alertMessage);
    return false;
  }

 
 if (empIdInput.startsWith('AP-')) {
    leaveObj.employeeType = "Apmosys Product";
    leaveObj.employeementId = empIdInput.substring(3);
  } else if (empIdInput.startsWith('A-')) {
    leaveObj.employeeType = "Other";
    leaveObj.employeementId = empIdInput.substring(2);
  } else {
    this.alertMessage = "Please enter valid Employee ID !!";
    this.openAlertMod(template, this.alertMessage);
    return false;
  }


    if (!this.validationService.validateEmployeementId(leaveObj.employeementId)) {
      this.alertMessage = "Please enter valid Employee ID !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    this.leaveService.getMyLeaveBalancesByEmpId(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveBalanceList = response.serviceResponse;
        this.leaveBalanceObj.empId = response.serviceResponse1;
        this.employeeData = response.serviceResponse2;
        this.employeeData.forEach((employee) => {
         employee.emp360 = employee.empId;
         employee.emp360ManagerId = employee.managerId;
        });
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });

  }




  leaveBalanceInputValidation(balance: any, template: TemplateRef<any>) {
    if (!this.validationService.validateNullUndefinedEmptyString(balance)) {
      this.alertMessage = "Please enter Valid Balance !!"
      this.openAlertMod(template, this.alertMessage);
    }
  }

  onUpdateLeaveBalance(template: TemplateRef<any>) {

    let inputValidated = true;
    this.leaveBalanceList.forEach(leave => {
      if (!this.validationService.validateNullUndefinedEmptyString(leave.balance)) {
        this.alertMessage = "Please enter Valid Balance !!"
        inputValidated = false;
        return;
      }
    });

    if (!inputValidated) {
      this.openAlertMod(template, this.alertMessage)
      return false;
    };

    this.leaveBalanceObj.employeeLeaveList = this.leaveBalanceList;
    // this.leaveBalanceObj.employeementId = this.leaveBalanceObj.employeementId?.substring(2)
    //console.log("manage Leave Balance :", this.leaveBalanceObj);

    // let empIdInput = this.leaveBalanceObj.employeementId;
 this.leaveBalanceObj.employeementId = this.leaveBalanceObj.employeementId?.replace(/^(A-|CS-|AP-)/, '');
  // this.leaveBalanceObj.employeementId = empIdInput;
    this.leaveService.updateLeavesByEmpId(this.leaveBalanceObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showLeaveBalanceForm();
        this.employeeData = []
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  // Leave Policies
  validateLeavepolicyObj(leavePolicyObj: Leave, template: TemplateRef<any>) {

    if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.leavePolicyName)) {
      this.alertMessage = "Please enter Leave Policy Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.leaveTypeMasterId)) {
      this.alertMessage = "Please Select Leave Type!!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.employmentStatus)) {
      this.alertMessage = "Please Select Employment Status !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

   if(leavePolicyObj.leaveTypeMasterId == 18){
    if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.maritalStatus)) {
      this.alertMessage = "Please Select Marital Status !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if(leavePolicyObj.maritalStatus != null){
      if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.maternityType)) {
        this.alertMessage = "Please Select Maternity Type !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }
    if(leavePolicyObj.maternityType != null){
      if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.maternityLeaveDays)) {
        this.alertMessage = "Please Enter allowed Maternity Leave days for "+leavePolicyObj.maternityType+" !!";
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      if(!this.validationService.validateNumber(leavePolicyObj.maternityLeaveDays)){
        this.alertMessage = "Please Enter valid allowed Maternity Leave days for "+leavePolicyObj.maternityType+" !!";
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }
   }
    // Policy Checks

    if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.leaveApplication)) {
      this.alertMessage = "Please Select Allow Leave Application !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.increment)) {
      this.alertMessage = "Please Select Monthly Increment !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (leavePolicyObj.increment == 'Yes') {
      if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.incrementValue)) {
        this.alertMessage = "Please Select Monthly Increment Value !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.oneTimeLeave)) {
      this.alertMessage = "Please Select One time Leave Limit !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (leavePolicyObj.oneTimeLeave == 'Yes') {
      if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.oneTimeLeaveMinCount)) {
        this.alertMessage = "Please Select One time Leave Minimum Limit Value !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.oneTimeLeaveCount)) {
        this.alertMessage = "Please Select One time Leave Maximum Limit Value !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.carryForward)) {
      this.alertMessage = "Please Select Carry Forward !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (leavePolicyObj.carryForward == 'Yes') {
      if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.carryForwardValue)) {
        this.alertMessage = "Please Select Carry Forward Value !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.expirationPeriod)) {
      this.alertMessage = "Please Select Leave Validity Expiration !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (leavePolicyObj.expirationPeriod == 'Yes') {
      if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.expirationPeriodValue)) {
        this.alertMessage = "Please Select Leave Validity Expiration Value !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.lockingPeriod)) {
      this.alertMessage = "Please Select Leave Application Count Locking !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (leavePolicyObj.lockingPeriod == 'Yes') {
      if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.lockingPeriodValue)) {
        this.alertMessage = "Please Select Leave Application Count Locking Period !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.lockingValue)) {
        this.alertMessage = "Please Select Leave Application Count Locking Value !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.probation)) {
      this.alertMessage = "Please Select Leave Application Probation !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (leavePolicyObj.probation == 'Yes') {
      if (!this.validationService.validateNullUndefinedEmptyString(leavePolicyObj.probationPeriod)) {
        this.alertMessage = "Please Enter Leave Application Probation Period !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }
    return true;
  }

  onAddLeavePolicy(template: TemplateRef<any>) {
    let inputValidated: boolean = this.validateLeavepolicyObj(this.leavePolicyObj, template)
    if (!inputValidated) return;

    this.leavePolicyObj.createdBy = this.currentUser.empId;
    //console.log("Add Leave Policy : ", this.leavePolicyObj);

    this.leaveService.addLeavePolicy(this.leavePolicyObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showLeavePoliciesTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onUpdateLeavePolicy(template: TemplateRef<any>) {
    let inputValidated: boolean = this.validateLeavepolicyObj(this.leavePolicyObj, template)
    if (!inputValidated) return;

    this.leavePolicyObj.updatedBy = this.currentUser.empId;
    //console.log("update Leave Policy : ", this.leavePolicyObj);

    this.leaveService.updateLeavePolicy(this.leavePolicyObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showLeavePoliciesTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  ondeleteLeavePolicy(template: TemplateRef<any>) {
    //console.log("Delete Leave Policy : ", this.leavePolicyObj);

    this.leaveService.deleteLeavePolicyByLeavePolicyMasterId(this.leavePolicyObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showLeavePoliciesTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  getAllLeavePolicies() {
    this.sortColumn=[];
    this.sortColumnType=[];
    this.sortDirection='';
    this.data = ''
    this.leavePolicyList = [];

    this.leaveService.getAllLeavePolicy().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leavePolicyList = response.serviceResponse;
        this.leavePolicyList.forEach(leavePolicy => {
          leavePolicy.createdOn = (leavePolicy.createdOn) ? moment(leavePolicy.createdOn).format(AppComponent.DATE_FORMAT) : null;
          leavePolicy.updatedOn = (leavePolicy.updatedOn) ? moment(leavePolicy.updatedOn).format(AppComponent.DATE_FORMAT) : null;
          leavePolicy.emp360CreatedBy =  leavePolicy.createdBy;
          leavePolicy.emp360UpdatedBy = leavePolicy.updatedBy;
     
        });
       
        //console.log("leavePolicyList : ", this.leavePolicyList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  getLeavePolicyByEmploymentStatusAndLeaveType() {
    //console.log("get Leave Policy : ", this.leavePolicyObj);
    this.leaveService.getLeavePolicyByEmployentStatusAndLeaveTypeMasterId(this.leavePolicyObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        //console.log("Leave Policy : ", response.serviceResponse);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  //pagination 

  page = 1;
  handlePageChange(event) {
    this.page = event;
    if(this.isInclude || this.isExclude){
    this.page1 = event.pageIndex+1;
    this.pageSize = event.pageSize;  
    this.getAllEmployeesByDepartmentIds();}

  }

  sortData(sort: Sort){	
    //console.log(sort);
    if(sort.active){
      let sortParams:any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;  
      this.sortColumn1 = sortParams[0];    
    }
    if(this.isInclude || this.isExclude){
    this.getAllEmployeesByDepartmentIds();}
  }

  toggleSearch(){
    this.isSearchEnabled = !this.isSearchEnabled;
    if(!this.isSearchEnabled){
      this.filters = {};
    }
  }

  onColumnSearch() {
   this.searchFilters = {};

  this.empExcludeColumns.forEach(c => {
    if (c.column !== 'blank' && c.value?.trim()) {
      this.searchFilters[c.column] = c.value.trim();
    }
  });
  this.getAllEmployeesByDepartmentIds();
}


  onSearch(searchData){
    this.filters = searchData;
    //console.log("Updated Filter : ", this.filters);
  }

  createForResetOtherField(){
    this.leavePolicyObj.maritalStatus = '';
    this.leavePolicyObj.maternityLeaveDays = '';
    this.leavePolicyObj.maternityType = '';
  }

  fieldResetOnChangeMaternityType(leavePolicy:Leave){
    leavePolicy.maternityLeaveDays = '';

  }


}

function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
}
