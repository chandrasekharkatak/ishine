
import { LocationStrategy } from '@angular/common';
import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { AppComponent } from 'src/app/app.component';
import { Performance } from 'src/app/models/performance';
import { Log } from '../models/log';
import { User } from '../models/user';
import { Feature } from '../models/feature';
import { LogService } from 'src/app/services/log.service';
import { AuthenticationService } from '../services/authentication.service';
import { PerformanceService } from '../services/performance.service';
import { first } from 'rxjs/operators';
import { DepartmentService } from '../services/department.service';
import { EmployeeService } from '../services/employee.service';
import { Employee360Service } from '../services/employee360.service';
import { ExportExcelService } from '../services/export-excel.service';
import { UtilityService } from '../services/utility.service';
import { ValidationService } from '../services/validation.service';
import { AppreciationAndRewardsCount } from '../models/appreciationAndRewardCount';
import { Employee } from '../models/employee';
import { HrHodMangerApiForPerformnace } from '../models/hrHodMangerApiForPerformnace';
import * as Highcharts from 'highcharts';
import * as moment from 'moment';
import { Query } from 'src/app/models/query';
import { SortPipe } from 'src/app/sort.pipe';
class FilterData {
  title: any;
  columns: any;
  queryList: any;
}

@Component({
  standalone: false,
  selector: 'app-user-performance',
  templateUrl: './user-performance.component.html',
  styleUrls: ['./user-performance.component.css']
})
export class UserPerformanceComponent implements OnInit {
  tabName:any = 'Performance ';
  feature = "Performance"
  currentUser:User;
  userMapping:any = {};
  mappTeamDashboard:boolean = false;

  log:Log;
  activeTab: string = 'performance-dashboard';
  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;
  @ViewChild("approval_details_modal")
  approvalDetailsModalTemplate: TemplateRef<any>;
  @ViewChild("status_detail_modal")
  statusDetailModalTemplate: TemplateRef<any>;
  page = 1;
  filters: any = {};
  filterData: any = new FilterData();
  alertMessage: any;
  modalRef:NgbModalRef;
  approvalDetailsModalRef: NgbModalRef;
  summaryStatusModalRef: NgbModalRef;
  /** Title for status detail popup (e.g. "Not Started - Employees") */
  summaryStatusTitle = '';
  /** Filtered list of employees for the selected status in the popup */
  summaryStatusEmployees: any[] = [];
  /** When set, main table shows only employees in this status (from performance summary click). */
  summaryStatusFilter: 'notStarted' | 'pendingHod' | 'ongoing' | 'completed' | 'rejected' | null = null;
  /** Table list source: all employees (default), eligible, or not eligible. */
  tableListMode: 'eligible' | 'all' | 'notEligible' = 'all';
  /** Popup data for Manager/HOD/HR approval details: { role, name, id, employmentId, rating, feedback } */
  approvalDetailsPopup: { role: string; name: string; id: any; employmentId: string; rating: string; feedback: string } | null = null;
  approvalDetailsLoading = false;
  submitPerformance: Performance = new Performance();
  updatePerformanceHr :Performance = new Performance();
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;
  queryList: any[] = [];
  storedDataList: any[] = [];
  data: string;
  employeeDataForExcel: any[] = [];
  selectedBulkEmpIds: Set<number> = new Set<number>();
  bulkHrRemark: string = '';
  bulkActionInProgress = false;
  performnace: Performance = new Performance();
  myMap: Map<string, string> = new Map();




  isperformanceDsah: boolean = false;
  isreviewPage: boolean = false;
  allEmployee: any[] = [];
  allEmployee1: any[] = [];
  allReviewType: any[] = [];
  allQauterCycle: any[] = [];
  eligibleEmployees: any[] = [];
  eligibleEmployees1: any[] = [];
  hrReviewStatus:any;
  // currentUser: any;
  // userMapping: any = {};
  EnabledAndActiveQuarterCycle: any[] = [];
  isSearchEnabled: boolean = false;
  selectedEmployee = new Employee();
  employeeColumns: any[] = ['Employee Id', 'Full Name', 'Department', 'Designation',
    'Job Role', 'Manager', 'Team Name', 'Project Name', 'Client Name',
    'Employment Status', 'Date Of Joining', 'Domain', 'Specialization', 'City', 'Blood Group',
    'Gender', 'Work Location', 'Probation Period', 'Notice Period', 'Marital Status',
    'Bank Name', 'Created By', 'State', 'Created On'];

  eligibleEmployeesColumns: any[] = ['employmentIdAcToET', 'name', 'designationName', 'departmentName','totalExperience', 'employmentstatus', 'dateOfJoining','completionStatus'];
  finalRating: number;
  hodRemarks: any;
  hrRemarks: any;
  recommendationsRemarks: any; 
  quarterId: any;
  rewardsCount:any;
  appreciationCount:any;
  isEditMode:boolean=false;
  appreciationAndRewardsCount:AppreciationAndRewardsCount=new AppreciationAndRewardsCount();
  departmentData: any[] = [
    // { department: 'HR', TotalNumberofemp: 10, ratinggivenbymanager: 7, pendingratinggivenbymanager: 3, managerName: 'Saxena' },
    // { department: 'Functional Testing', TotalNumberofemp: 15, ratinggivenbymanager: 10, pendingratinggivenbymanager: 5, managerName: 'Dev' },
    // { department: 'INHOUSE', TotalNumberofemp: 13, ratinggivenbymanager: 3, pendingratinggivenbymanager: 10, managerName: 'Mayur' },
    // { department: 'Devops', TotalNumberofemp: 20, ratinggivenbymanager: 10, pendingratinggivenbymanager: 10, managerName: 'vishal' },
    // { department: 'Finanace', TotalNumberofemp: 33, ratinggivenbymanager: 30, pendingratinggivenbymanager: 3, managerName: 'suresh' },
    // { department: 'Cloud Dep', TotalNumberofemp: 65, ratinggivenbymanager: 40, pendingratinggivenbymanager: 20, managerName: 'Jitendra' },
    // { department: 'New Dep', TotalNumberofemp: 87, ratinggivenbymanager: 17, pendingratinggivenbymanager: 70, managerName: 'Gopal' },
    // { department: 'New Dep', TotalNumberofemp: 87, ratinggivenbymanager: 17, pendingratinggivenbymanager: 70, managerName: 'Gopal' },
    // { department: 'New Dep', TotalNumberofemp: 87, ratinggivenbymanager: 17, pendingratinggivenbymanager: 70, managerName: 'Gopal' },
    // { department: 'New Dep', TotalNumberofemp: 87, ratinggivenbymanager: 17, pendingratinggivenbymanager: 70, managerName: 'Gopal' },
    // { department: 'New Dep', TotalNumberofemp: 87, ratinggivenbymanager: 17, pendingratinggivenbymanager: 70, managerName: 'Gopal' },
    // { department: 'New Dep', TotalNumberofemp: 87, ratinggivenbymanager: 17, pendingratinggivenbymanager: 70, managerName: 'Gopal' },

  ];

  myList: { reviewLabel: any; silde: any; performanceRatingId: any; comment?: string }[] = [];
  myRateList: { reviewLabel: any; rate: any; performanceRatingId: any; comment?: string }[] = [];

  allQauterCycle2: any;
  confirmModalRef :any;
  confirmDiscard: boolean = false;
  @ViewChild('confirm_discard_modal') confirmDiscardModal!: TemplateRef<any>;
   
  constructor(
     private router: Router,
    private route: ActivatedRoute,
    private logService: LogService,
    private employeeService: EmployeeService,
    private validationService: ValidationService,
    private utilityService: UtilityService,
    private authenticationService: AuthenticationService,
    private modalService: NgbModal,
    private locationStrategy: LocationStrategy,
    private performanceSerive: PerformanceService,
    private exportExcelService: ExportExcelService,
    private performanceService: PerformanceService,
    private employee360Service: Employee360Service,
    private departmentService:DepartmentService
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);

    this.logService.log.subscribe(x => {
      this.log = x;
      this.log.featureName = this.feature;
    });
  }
  ngAfterViewInit(): void {
    this.renderPlaceholderChart("Rating", "performanceId", this.departmentData);
  }

  async ngOnInit(): Promise<void> {
    try {
      // Call getALLdepartmentByEmployee first
      this.getAllDepartments();
      await this.getALLdepartmentByEmployee();

      this.getCurrentUserDepartment();
      // Then call other methods

      this.getAllReviveType();
      this.getAllQauterCycle();
      // this.setQuartedId(this.quarterId);
      // Continue with user mapping logic
      let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
      featureMap.subFeatures?.forEach(sub => {
        this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
      });

      this.isperformanceDsah = true;
      console.log("usermappinghodhr",this.userMapping);
      console.log("hodddddd", this.userMapping.performance_action_by_hod);
      console.log("hrrrrrrrr", this.userMapping.performance_action_by_hr);
      console.log("rmmm", this.userMapping.performance_action_by_approvals_tos);
     

       
       this.getAllEmployeesCurrentStatus();
        this.getAllEmployee();

    } catch (error) {
      console.error("Error in ngOnInit", error);
    }
  }

  performanceData = [
    { criteria: "Consistency", rating: 0 },
    { criteria: "Team Collaboration", rating: 0 },
    { criteria: "Innovation", rating: 4 },
    { criteria: "Communication Skills", rating: 5 },
    { criteria: "Stakeholder Feedback", rating: 3 },
    { criteria: "Problem Solving", rating: 4 },
    { criteria: "Decision Making", rating: 3 },
    { criteria: "Professionalism and Work Ethics", rating: 4 },
    { criteria: "Initiative Nature", rating: 4 }
  ];

  filterCriteria: any[] = [];
  filterCriteria1: any[] = [];
  filterRatingCriteria: any[] = [];
  filterCriteriaQuarter: any[] = [];

  // updateSlide(sliderIndex: number, newRating: number) {
  //   if (!this.myList[sliderIndex]) {
  //     this.myList[sliderIndex] = { reviewLabel: this.filterCriteria[sliderIndex]?.reviewLabel, silde: 0 };
  //   }
  //   this.myList[sliderIndex].silde = newRating;

  //   console.log("Slider Data:", this.myList);
  //   this.calculateFinalRating();
  // }

  // updateRating(ratingIndex: number, rating: number) {
  //   if (!this.myRateList[ratingIndex]) {
  //     this.myRateList[ratingIndex] = { reviewLabel: this.filterRatingCriteria[ratingIndex]?.reviewLabel, rate: 0 };
  //   }
  //   this.myRateList[ratingIndex].rate = rating;

  //   console.log("Rating Data:", this.myRateList);
  //   this.calculateFinalRating();
  // }

  updateSlide(index: number, newRating: number) {

    this.myList[index].silde = newRating;


    console.log("data log", this.myList);
    this.calculateFinalRating();
  }


  updateRating(index: number, rating: number) {
    this.myRateList[index].rate = rating;
    console.log("data log", this.myRateList);
    this.calculateFinalRating();
  }




  RatingData: any[] = [];

userDetailsForPerformanceView:HrHodMangerApiForPerformnace=new HrHodMangerApiForPerformnace();

  getAllEmployee() {
  this.userDetailsForPerformanceView.empId = this.currentUser.empId;
  this.userDetailsForPerformanceView.hrvalidate = this.userMapping.performance_action_by_hr;
   this.performanceSerive.getAllEmployeesForPerformance(this.userDetailsForPerformanceView).subscribe
      ((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allEmployee = response.serviceResponse;
          const mergedData = this.allEmployee.map(emp => {
            const staticData = this.static.find(item => item.empId === emp.empId);
            return {
                ...emp,
                completionStatus: staticData ? staticData.completionStatus : null,
                finalRating: (staticData && (staticData.finalRating != null && staticData.finalRating !== '')) ? staticData.finalRating : (staticData?.averageRating != null && staticData.averageRating !== '') ? staticData.averageRating : (emp.finalRating != null && emp.finalRating !== '' ? emp.finalRating : (emp.averageRating != null && emp.averageRating !== '' ? emp.averageRating : null)),
                performanceStatusPercentage: (staticData && (staticData.performanceStatusPercentage != null)) ? staticData.performanceStatusPercentage : (emp.performanceStatusPercentage != null ? emp.performanceStatusPercentage : null),
                managerReviewStatus: staticData?.managerReviewStatus ?? emp.managerReviewStatus,
                hodReviewStatus: staticData?.hodReviewStatus ?? emp.hodReviewStatus,
                hrReviewStatus: staticData?.hrReviewStatus ?? emp.hrReviewStatus
            };
        });
          console.log("allEmp", mergedData);

          // Use merged data so "All employees" view shows review status, final rating, approval status
          this.allEmployee = mergedData;

          const currentDate = new Date();
          const oneYearAgo = new Date(currentDate.getFullYear() - 1, 11, 31);

          // this.allEmployee = this.allEmployee.filter(employee => employee.empId !== this.currentUser.empId);
          this.eligibleEmployees = this.allEmployee.filter(employee => {
            // Append employee ID using utility service
            employee.employeementId = this.utilityService.appendEmployeementid(employee.isConsultant, employee.employeementId);

            // Calculate experience from date_of_joining
            if (employee.dateOfJoining) {
              employee.calculatedExperience = this.calculateExperienceFromDOJ(employee.dateOfJoining);
            }

            const joiningDate = new Date(employee.dateOfJoining);
   return joiningDate <= oneYearAgo && employee.employmentstatus === 'Confirmed';

            // if (this.currentUser.employeeRole !== 'HR') {
            //   alert('You are not authorized..!!');
            // }


            return false;
          });
          this.eligibleEmployees.forEach(eligibleEmp => {
            eligibleEmp.emp360 = eligibleEmp.empId;
            eligibleEmp.fullSearchText = [eligibleEmp.name, eligibleEmp.employmentIdAcToET, eligibleEmp.departmentName, eligibleEmp.designationName].filter(Boolean).join(' ').toLowerCase();
          });
          this.mergeStaticDataIntoEligibleEmployees();

        } else {
          console.error(response.serviceResponse);
        }
      }

      )
  }


  toggleSearch() {
    this.sortColumn = null;
    this.sortColumnType = null;
    this.sortDirection = '';
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
    }
  }

  /** When true, OK on the alert modal will close and redirect back to the main table. */
  alertRedirectToList = false;

  cancelRequest() {
    const shouldRedirect = this.alertRedirectToList;
    this.alertRedirectToList = false;
    this.modalRef?.close();
    if (shouldRedirect) {
      setTimeout(() => {
        this.isperformanceDsah = true;
        this.isreviewPage = false;
        this.getAllEmployeesCurrentStatus();
        this.getAllEmployee();
      }, 150);
    }
  }


  getAllReviveType() {
    this.performanceSerive.getReviewType().subscribe
      ((response: any) => {
        if (response.serviceStatus == "Success") {

          this.allReviewType = response.serviceResponse;
          console.log("allReviewType", this.allReviewType);

        } else {
          console.error(response.serviceResponse);
        }
      });
  }


  async getALLdepartmentByEmployee() {
    try {


      const response: any = await this.performanceSerive.getALLdepartmentByEmployee(this.userDetailsForPerformanceView).toPromise();

      if (response.serviceStatus === "Success") {






        this.departmentData = response.serviceResponse;

        console.log("departmentData", this.departmentData);
        setTimeout(() => {
          this.renderPlaceholderChart("Rating", "performanceId", this.departmentData);
        }, 100);
      } else {
        console.error(response.serviceResponse);
      }
    } catch (error) {
      console.error("Error fetching department data:", error);
    }
  }

  currentUserdepartmentName :string = 'all';
  selectedDepartment: string = 'All';
  departments:any[] =[];
  onDepartmentChange(event: any) {


    // if(this.selectedDepartment === 'all'){
    //   this.filteredEmployees=this.allEmployee;
    // }else{
      this.selectedDepartment = event.target.value;
    // }
    if(this.selectedDepartment === 'all'){
      this.userDetailsForPerformanceView.deptId = null;
    }else{
      this.userDetailsForPerformanceView.deptId = this.selectedDepartment;
    }
    console.log("hbhgsvchsgdv",this.userDetailsForPerformanceView.deptId,this.selectedDepartment)
    // this.userDetailsForPerformanceView.deptId =
    // this.selectedDepartment === 'All' ? null : this.selectedDepartment;

     this.getALLdepartmentByEmployee();
  }

  selectedQuarter:String = 'All'
  onQuarterChange(event: any){

    this.selectedQuarter = event.target.value;
    // }
    if(this.selectedQuarter === 'all'){
      this.selectedQuarter = null;
    }else{
      this.selectedQuarter = this.selectedQuarter;
    }
    console.log("Check quarter",this.selectedQuarter);

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

  getAllQauterCycle() {
    this.performanceSerive.getAllQuarterCycles().subscribe
      ((response: any) => {
        if (response.serviceStatus == "Success") {

          this.allQauterCycle = response.serviceResponse;
          this.EnabledAndActiveQuarterCycle = this.allQauterCycle.filter(quarter => quarter.isEnable && quarter.isActive);
          console.log("allQauterCycle", this.allQauterCycle);

        } else {
          console.error(response.serviceResponse);
        }
      });
  }


  openFilterModal(template: TemplateRef<any>, columns: any[], title: any) {
    console.log("columns : ", columns);
    this.queryList = [];

    this.filterData.title = title;
    this.filterData.columns = columns;

    this.queryList = [
      { column: "Employment Status", operator: "!=", value: "InActive", conjunction: "" }
    ];

    this.storedDataList.forEach((data) => {
      if (data.filterName == title) {
        data.queryList.forEach((queryObj) => {
          if (queryObj.column == "Employee Id" && !queryObj.value.includes("A-")) {
            queryObj.value = "A-".concat(queryObj.value);
          }

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

    console.log("filterData : ", this.filterData);
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
  }





  onFilterSubmit(emittedArray: any, template: TemplateRef<any>) {
    if (emittedArray[0].length != 0) {
      console.log("queryList : ", emittedArray[0]);
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

      if (this.filterData.title == 'Filter All Employee') {
        this.getCustomEmployeesList(emittedArray[0], template);
      }
    } else {
      let clearedFilter = this.storedDataList.find((filter) => filter.filterName == emittedArray[1]);
      this.storedDataList.splice(clearedFilter);


      if (emittedArray[1] == 'Filter All Employee') {
        this.showTable();
      }
    }
  }


  showTable() {

    this.page = 1;
    this.data = '';
    this.filters = {};



    this.getAllEmployee();
  }


  handlePageChange(event) {
    this.page = event;
  }

  sortData(sort: Sort) {
    if (!sort.active) return;
    const direction = sort.direction;
    if (!direction) {
      this.sortColumn = null;
      this.sortColumnType = null;
      this.sortDirection = '';
      return;
    }
    const sortParams = sort.active.split('|');
    this.sortColumn = sortParams[0] || null;
    this.sortColumnType = sortParams[1] || null;
    this.sortDirection = direction;
    this.page = 1;
  }

  name = 'EmployeeSheet.xlsx';

  /** Returns the current table list filtered and sorted (same as displayed, all pages). Used for export. */
  getExportList(): any[] {
    let list = this.getTableEmployeeList() || [];
    if (!Array.isArray(list)) return [];
    const filter = this.filters;
    if (filter && Object.keys(filter).length > 0) {
      const filterKeys = Object.keys(filter);
      const escapeRegExp = (s: string) => String(s).replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
      list = list.filter((item: any) =>
        filterKeys.every((keyName) => {
          const val = filter[keyName];
          return val == null || val === '' || new RegExp(escapeRegExp(String(val)), 'gi').test(String(item[keyName] ?? ''));
        })
      );
    }
    if (this.sortColumn != null && this.sortColumnType != null && this.sortDirection != null) {
      list = new SortPipe().transform([...list], [this.sortColumn, this.sortColumnType, this.sortDirection]);
    }
    return list;
  }

  /** Whether an employee row can be part of HR bulk action. */
  canBulkHrAction(emp: any): boolean {
    if (!this.userMapping?.performance_action_by_hr) return false;
    if (!emp?.empId) return false;
    const managerStatus = this.getApprovalStatus(emp, 'manager');
    const hodStatus = this.getApprovalStatus(emp, 'hod');
    const hrStatus = this.getApprovalStatus(emp, 'hr');
    // Tick/select only when BOTH Manager and HOD have acted, and HR is still Pending.
    return managerStatus !== 'Pending' && hodStatus !== 'Pending' && hrStatus === 'Pending';
  }

  isBulkSelected(empId: any): boolean {
    return empId != null ? this.selectedBulkEmpIds.has(Number(empId)) : false;
  }

  toggleBulkSelection(emp: any, checked: boolean): void {
    const id = Number(emp?.empId);
    if (!id || !this.canBulkHrAction(emp)) return;
    if (checked) this.selectedBulkEmpIds.add(id);
    else this.selectedBulkEmpIds.delete(id);
  }

  toggleSelectAllBulk(checked: boolean): void {
    const rows = this.getExportList().filter((e: any) => this.canBulkHrAction(e));
    if (checked) {
      rows.forEach((e: any) => this.selectedBulkEmpIds.add(Number(e.empId)));
    } else {
      this.selectedBulkEmpIds.clear();
    }
  }

  areAllBulkSelected(): boolean {
    const rows = this.getExportList().filter((e: any) => this.canBulkHrAction(e));
    return rows.length > 0 && rows.every((e: any) => this.selectedBulkEmpIds.has(Number(e.empId)));
  }

  clearBulkSelection(): void {
    this.selectedBulkEmpIds.clear();
    this.bulkHrRemark = '';
  }

  submitBulkHrAction(status: 'Accepted' | 'Rejected'): void {
    if (!this.userMapping?.performance_action_by_hr) return;
    if (this.selectedBulkEmpIds.size === 0) {
      this.openAlertMod(this.alertTemplate, 'Please select at least one employee for bulk action.');
      return;
    }
    const remark = (this.bulkHrRemark || '').trim();
    if (!remark) {
      this.openAlertMod(this.alertTemplate, 'Please enter HR remark for bulk action.');
      return;
    }
    const quarterId = this.EnabledAndActiveQuarterCycle?.[0]?.quarterId;
    if (!quarterId) {
      this.openAlertMod(this.alertTemplate, 'No active quarter found for bulk HR action.');
      return;
    }
    this.bulkActionInProgress = true;
    const payload: any = {
      empIds: Array.from(this.selectedBulkEmpIds),
      quarterId: quarterId,
      hrId: this.currentUser?.empId,
      hrReviewStatus: status,
      hrRemark: remark
    };
    this.performanceService.bulkSubmitEmployeePerformanceHR(payload).pipe(first()).subscribe((response: any) => {
      this.bulkActionInProgress = false;
      if (response?.serviceStatus === 'Success') {
        this.clearBulkSelection();
        this.getAllEmployeesCurrentStatus();
        this.getAllEmployee();
        this.openAlertMod(this.alertTemplate, `Bulk ${status.toLowerCase()} completed successfully.`);
      } else {
        this.openAlertMod(this.alertTemplate, response?.serviceResponse || 'Bulk HR action failed.');
      }
    }, () => {
      this.bulkActionInProgress = false;
      this.openAlertMod(this.alertTemplate, 'Something went wrong while processing bulk HR action.');
    });
  }

  exportToExcel(): void {
    const list = this.getExportList();
    const onlySpecificDataArr = list.map((x: any) => ({
      "EmployeeId": (x.isConsultant === 'true' ? 'A-CS-' : 'A-') + (x.employeementId ?? x.employmentIdAcToET ?? ''),
      "Full Name": x.name,
      "EmailId": x.email ?? '',
      "Employment Status": x.employmentstatus ?? '',
      "Department Name": x.departmentName ?? '',
      "Billable Type": x.billableType ?? '',
      "Experience": x.calculatedExperience ?? (x.dateOfJoining ? this.calculateExperienceFromDOJ(x.dateOfJoining) : x.totalExperience) ?? '',
      "quarter Cycle": x.quarterycle || 'NULL',
      "financial Year": x.financialYear || 'NULL',
      "Current Status": x.completionStatus ?? 'NULL',
      "hod Name": x.hodName ?? 'NULL',
      "Final Rating": x.finalRating ?? 'NULL',
      "Manger Remark": x.hodRemarks ?? 'NULL',
      "Hod Remarks": x.hrRemarks ?? 'NULL',
      "Hod Review Status": x.hrReviewStatus ?? 'NULL'
    }));
    this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.name);
  }

  getCustomEmployeesList(queryObjList: any, template: TemplateRef<any>) {
    this.eligibleEmployees = [];

    let queryObj = new Query();
    queryObj.queryList = queryObjList;

    if (queryObjList == '') {
      this.getAllEmployee();
    } else {
      this.employeeService.customQueryForEmployeeReport(queryObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {



          this.allEmployee = response.serviceResponse;

          console.log("allEmp", this.allEmployee);

          const currentDate = new Date();
          const oneYearAgo = new Date();
          oneYearAgo.setFullYear(currentDate.getFullYear() - 1);  // Get the date one year ago

          this.eligibleEmployees = this.allEmployee.filter(employee => {
            // employee.employeementId = this.utilityService.appendEmployeementid(employee.isConsultant, employee.employeementId);
            const joiningDate = new Date(employee.dateOfJoining);
            return joiningDate <= oneYearAgo && employee.employmentstatus === 'Confirmed';
          });


          this.eligibleEmployees = this.eligibleEmployees.filter((value, index, self) =>
            index === self.findIndex((t) => (
              t.employeementId === value.employeementId
            ))
          )

          if (this.eligibleEmployees.length == 0) {
            this.openAlertMod(this.alertTemplate, "No Data found")
          }
          this.eligibleEmployees.forEach(employee => {
            if (employee.isConsultant == 'true') {
              employee.employeementId = "A-".concat(employee.employeementId);
            } else {
              employee.employeementId = "A-CS-".concat(employee.employeementId);
            }
            // employee.employeementId = "A-".concat(employee.employeementId);
            employee.dateOfBirth = (employee.dateOfBirth) ? moment(employee.dateOfBirth).format(AppComponent.DATE_FORMAT) : null;
            employee.dateOfJoining = (employee.dateOfJoining) ? moment(employee.dateOfJoining).format(AppComponent.DATE_FORMAT) : null;
            employee.createdOn = (employee.createdOn) ? moment(employee.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
            employee.updatedOn = (employee.updatedOn) ? moment(employee.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
            employee.fullSearchText = [employee.name, employee.employmentIdAcToET, employee.departmentName, employee.designationName].filter(Boolean).join(' ').toLowerCase();
          });
          console.log("allEmployeeList : ", this.eligibleEmployees)
        } else {
          this.openAlertMod(template, response.serviceResponse)
        }
      });
    }
  }

  openAlertMod(template: TemplateRef<any>, message: any, redirectOnClose?: boolean) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
    this.alertRedirectToList = redirectOnClose === true;
  }

  searchTableText = '';

  onSearch(searchData) {
    this.filters = searchData;
  }

  onSearchTable() {
    this.filters = { fullSearchText: this.searchTableText || '' };
    this.page = 1;
  }


  /** Compute final rating from criteria: normalize each rating by its max (condition), average, then scale to 0–5 for Overall Rating Summary. */
  calculateFinalRating() {
    let normalizedSum = 0;
    let count = 0;
    if (this.filterCriteria && this.myList) {
      this.myList.forEach((item, i) => {
        const criterion = this.filterCriteria[i];
        const max = criterion && criterion.condition != null ? Number(criterion.condition) : 5;
        if (max > 0 && item.silde !== undefined && item.silde !== null && !isNaN(Number(item.silde))) {
          normalizedSum += Math.min(1, Math.max(0, Number(item.silde) / max));
          count++;
        }
      });
    }
    if (this.filterRatingCriteria && this.myRateList) {
      this.myRateList.forEach((item, i) => {
        const criterion = this.filterRatingCriteria[i];
        const max = criterion && criterion.condition != null ? Number(criterion.condition) : 5;
        if (max > 0 && item.rate !== undefined && item.rate !== null && !isNaN(Number(item.rate))) {
          normalizedSum += Math.min(1, Math.max(0, Number(item.rate) / max));
          count++;
        }
      });
    }
    const avgNormalized = count > 0 ? normalizedSum / count : 0;
    this.finalRating = count > 0 ? Math.round(avgNormalized * 5 * 100) / 100 : null;
  }

  renderPlaceholderChart(chartName: string, chartId: string, departmentData: any) {
    // Ensure departmentData is available and has the expected structure
    if (!departmentData || departmentData.length === 0) {
      console.error('Department data is empty or undefined.');
      return;
    }

    // Categories for the chart (department names); fix common typos for display
    const departmentNameFix: Record<string, string> = { 'Accountss': 'Accounts', 'Finanace': 'Finance' };
    const categories = departmentData.map((dep: any) => departmentNameFix[dep.department] || dep.department);

    // Rating and Pending Rating Percentages arrays
    let ratingPercentages = [];
    let pendingratingPercentages = [];

    // Handle chart rendering based on chartName ('Rating' or 'Pending')
    if (chartName === 'Rating') {
      // Single brand color for all bars (matches legend and looks consistent)
      const barColor = '#193d8a';
      const barData = departmentData.map((dep: any) => {
        const total = dep.TotalNumberofemp || 0;
        const rated = dep.ratinggivenbymanager ?? 0;
        const pct = total > 0 ? parseFloat(((rated / total) * 100).toFixed(1)) : 0;
        return {
          y: pct,
          color: barColor,
          rated,
          total,
          managerName: dep.managerName || '—'
        };
      });

      Highcharts.chart(chartId, {
        chart: {
          type: 'column',
          backgroundColor: 'transparent',
          style: { fontFamily: 'inherit' },
          spacing: [20, 16, 72, 16],
          plotBackgroundColor: 'transparent',
          plotBorderWidth: 0,
          plotShadow: false
        },
        title: { text: null },
        credits: { enabled: false },
        exporting: { enabled: false },
        legend: { enabled: false },
        xAxis: {
          categories: categories,
          title: { text: null },
          labels: {
            style: { fontSize: '11px', color: '#475569', fontWeight: '500' },
            autoRotation: [-45, -90],
            reserveSpace: true,
            x: -4,
            y: 14
          },
          lineColor: '#cbd5e1',
          tickColor: '#cbd5e1',
          tickLength: 6
        },
        yAxis: {
          title: {
            text: 'Percentage of Employees Rated',
            style: { fontSize: '12px', color: '#64748b', fontWeight: '600' }
          },
          min: 0,
          max: 100,
          tickInterval: 20,
          labels: {
            format: '{value}%',
            style: { fontSize: '11px', color: '#64748b' }
          },
          gridLineColor: '#e2e8f0',
          gridLineWidth: 1,
          lineColor: '#cbd5e1',
          tickLength: 0
        },
        plotOptions: {
          column: {
            borderRadius: 6,
            borderWidth: 0,
            pointPadding: 0.2,
            groupPadding: 0.25,
            shadow: false,
            dataLabels: {
              enabled: true,
              format: '{point.rated} / {point.total}',
              style: {
                fontSize: '11px',
                fontWeight: '700',
                textOutline: 'none',
                color: '#334155'
              },
              verticalAlign: 'top',
              y: -6
            }
          }
        },
        tooltip: {
          shared: false,
          useHTML: true,
          backgroundColor: '#ffffff',
          borderColor: '#e2e8f0',
          borderWidth: 1,
          borderRadius: 8,
          shadow: true,
          style: { fontSize: '13px' },
          padding: 12,
          formatter: function (this: any) {
            const p = this.point;
            return `<div class="pms-chart-tooltip">
              <strong>${this.x}</strong><br/>
              <span>Rated: <b>${p.rated}</b> employees</span><br/>
              <span>Total: <b>${p.total}</b> employees</span><br/>
              <span>Percentage: <b>${p.y}%</b></span><br/>
              <span>HOD: ${p.managerName}</span>
            </div>`;
          }
        },
        series: [
          {
            name: 'Rated',
            type: 'column',
            data: barData
          }
        ]
      });

    } else {
      // Calculate Pending Rating Percentages
      pendingratingPercentages = departmentData.map(dep => {
        return (dep.pendingratinggivenbymanager / dep.TotalNumberofemp) * 100;
      });

      Highcharts.chart(chartId, {
        chart: {
          type: 'column'
        },
        title: {
          text: 'Department-wise Bell Curve'
        },
        credits: { enabled: false },
        xAxis: {
          categories: categories,
          title: { text: '' },
          labels: { enabled: false }
        },
        yAxis: {
          title: { text: 'Percentage of Pending' },
          max: 100
        },
        plotOptions: {
          column: {
            borderRadius: 10,
            colorByPoint: true,
            dataLabels: {
              enabled: true,
              format: '{point.category}', // Show department name
              verticalAlign: 'bottom', // Align labels at bottom of column
              y: -10, // Move label just above the column
              style: {
                fontSize: '12px',
                fontWeight: 'bold'
              }
            }
          }
        },
        tooltip: {
          pointFormatter: function () {
            // Access managerName through the departmentData array
            const managerName = departmentData[this.index].managerName;
            return `<b>Pending Percentage: ${this.y}%</b><br><b>HOD: ${managerName}</b>`;
          }
        },
        series: [
          {
            name: 'Pending Percentage',
            type: 'column',
            data: pendingratingPercentages.map((percentage, index) => ({
              y: parseFloat(percentage.toFixed(2)),
              managerName: departmentData[index].managerName // Accessing manager name directly from departmentData
            }))
          },
          {
            name: 'Bell Curve Line',
            type: 'spline',
            data: pendingratingPercentages,
            color: 'black',
            marker: { enabled: false }
          }
        ]
      });
    }
  }

  static:any[] = [];
  getAllEmployeesCurrentStatus(){
    this.performanceService.getAllEmployeesCurrentStatus().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
         this.static = response.serviceResponse;
         this.mergeStaticDataIntoEligibleEmployees();
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  /** Merge finalRating, performanceStatusPercentage, and approval statuses from static (current status) into eligibleEmployees. */
  mergeStaticDataIntoEligibleEmployees() {
    if (!this.static?.length || !this.eligibleEmployees?.length) return;
    this.eligibleEmployees.forEach(emp => {
      const staticData = this.static.find((item: any) => item.empId === emp.empId);
      if (staticData) {
        if (staticData.finalRating != null && staticData.finalRating !== '') emp.finalRating = staticData.finalRating;
        else if (staticData.averageRating != null && staticData.averageRating !== '') emp.finalRating = staticData.averageRating;
        if (staticData.performanceStatusPercentage != null) emp.performanceStatusPercentage = staticData.performanceStatusPercentage;
        if (staticData.managerReviewStatus != null) emp.managerReviewStatus = staticData.managerReviewStatus;
        if (staticData.hodReviewStatus != null) emp.hodReviewStatus = staticData.hodReviewStatus;
        if (staticData.hrReviewStatus != null) emp.hrReviewStatus = staticData.hrReviewStatus;
      }
    });
  }

  /**
   * Calculate experience in years from date of joining to current date
   * Formula: current_date - date_of_joining
   * @param dateOfJoining - Date of joining in string format (YYYY-MM-DD)
   * @returns Experience in years (rounded to 1 decimal place)
   */
  calculateExperienceFromDOJ(dateOfJoining: string): number {
    if (!dateOfJoining) {
      return 0;
    }

    try {
      const doj = new Date(dateOfJoining);
      const today = new Date();
      
      // Calculate difference in milliseconds
      const diff = today.getTime() - doj.getTime();
      
      // Convert to years (considering leap years: 365.25 days per year)
      const experienceInYears = diff / (1000 * 60 * 60 * 24 * 365.25);
      
      // Round to 1 decimal place
      return Number(experienceInYears.toFixed(1));
    } catch (error) {
      console.error('Error calculating experience:', error);
      return 0;
    }
  }

  /** Performance summary counts for eligible employees (for CYCLE ASSIGNED card). */
  get performanceSummary(): {
    notStarted: number;
    ongoing: number;
    pendingHod: number;
    completed: number;
    rejected: number;
    rated: number;
    avgRating: number | null;
  } {
    const list = this.eligibleEmployees || [];
    let notStarted = 0, ongoing = 0, pendingHod = 0, completed = 0, rejected = 0, rated = 0;
    let ratingSum = 0;
    list.forEach(emp => {
      const s = (emp?.completionStatus || 'Not Started').toString().trim().toLowerCase();
      if (s === 'not started') notStarted++;
      else if (s === 'ongoing' || s === 'submitted') ongoing++;
      else if (s === 'pending hod') pendingHod++;
      else if (s === 'completed') completed++;
      else if (s === 'rejected') rejected++;
      else notStarted++;
      const r = this.getEmployeeFinalRatingValue(emp);
      if (r != null && !isNaN(r)) {
        rated++;
        ratingSum += r <= 5 ? r : (r / 10) * 5;
      }
    });
    const avgRating = rated > 0 ? Math.round((ratingSum / rated) * 100) / 100 : null;
    return { notStarted, ongoing, pendingHod, completed, rejected, rated, avgRating };
  }

  /** Rating category bands (0–5 scale): NI, M-, M, M+, E. */
  private static readonly RATING_CATEGORIES: { max: number; label: string }[] = [
    { max: 1.5, label: 'NI' },
    { max: 2.4, label: 'M-' },
    { max: 3.4, label: 'M' },
    { max: 4.4, label: 'M+' },
    { max: 5, label: 'E' }
  ];

  /** Maps numeric rating (0–5) to category: NI (0–1.5), M- (1.6–2.4), M (2.5–3.4), M+ (3.5–4.4), E (4.5–5). */
  getRatingCategory(rating: number | null | undefined): string {
    if (rating == null || isNaN(Number(rating))) return 'N/A';
    const n = Math.min(5, Math.max(0, Number(rating)));
    for (const band of UserPerformanceComponent.RATING_CATEGORIES) {
      if (n <= band.max) return band.label;
    }
    return 'E';
  }

  /** Resolves final rating value from employee (finalRating or averageRating), normalized to 0–5. */
  private getEmployeeFinalRatingValue(emp: any): number | null {
    const r = emp?.finalRating ?? emp?.averageRating;
    if (r == null || r === '' || isNaN(Number(r))) return null;
    const num = Number(r);
    return num <= 5 ? num : (num / 10) * 5;
  }
  /** Returns 0-5 filled stars from employee final rating. Supports scale 5 or 10 (if > 5 treated as out of 10). Table UI. */
  getFinalRatingStars(emp: any): number {
    const num = this.getEmployeeFinalRatingValue(emp);
    if (num == null) return 0;
    return Math.min(5, Math.max(0, Math.round(num)));
  }
  /** Returns label for final rating column: category (e.g. "M+") with optional numeric "3.2 (M+)" or "N/A". */
  getFinalRatingLabel(emp: any): string {
    const num = this.getEmployeeFinalRatingValue(emp);
    if (num == null) return 'N/A';
    const category = this.getRatingCategory(num);
    const formatted = (Math.round(num * 10) / 10).toFixed(1);
    return `${formatted} (${category})`;
  }
  /** Returns 0-5 filled stars from averageRating (card). */
  getAverageRatingStars(): number {
    const r = this.averageRating;
    if (r == null || r === '' || isNaN(Number(r))) return 0;
    return Math.min(5, Math.max(0, Math.round(Number(r))));
  }
  /** Returns average rating label for card: "X.X / 5.0" or "N/A". */
  getAverageRatingLabel(): string {
    const r = this.averageRating;
    if (r == null || r === '' || isNaN(Number(r))) return 'N/A';
    const n = Math.min(5, Math.max(0, Number(r)));
    return (Math.round(n * 10) / 10).toFixed(1) + ' / 5.0';
  }
  /** Numeric average rating (same source as card) for Overall Rating Summary. */
  getAverageRatingNumeric(): number | null {
    const r = this.averageRating;
    if (r == null || r === '' || isNaN(Number(r))) return null;
    return Math.min(5, Math.max(0, Number(r)));
  }
  /** Performance score % from averageRating (same source as card). For Overall Rating Summary table. */
  getAverageRatingScorePercent(): number {
    const n = this.getAverageRatingNumeric();
    if (n == null) return 0;
    return Math.min(100, Math.round((n / 5) * 1000) / 10);
  }
  /** Performance category from averageRating: NI / M- / M / M+ / E. */
  getAverageRatingCategory(): string {
    return this.getRatingCategory(this.getAverageRatingNumeric());
  }
  /** Formatted average rating number only e.g. "2.0" (same source as card). For Overall Rating Summary Final Rating value. */
  getAverageRatingFormatted(): string {
    const n = this.getAverageRatingNumeric();
    if (n == null) return '0';
    return (Math.round(n * 10) / 10).toFixed(1);
  }
  /** Returns approval status for Manager/HOD/HR. Uses emp.managerReviewStatus, emp.hodReviewStatus, emp.hrReviewStatus or 'Pending'. */
  getApprovalStatus(emp: any, role: 'manager' | 'hod' | 'hr'): string {
    const key = role === 'manager' ? 'managerReviewStatus' : role === 'hod' ? 'hodReviewStatus' : 'hrReviewStatus';
    const s = emp?.[key];
    return s && (s === 'Submitted' || s === 'Accepted' || s === 'Rejected') ? s : 'Pending';
  }

  /** Whether the approval icon is clickable (Submitted or Accepted). */
  canOpenApprovalDetails(emp: any, role: 'manager' | 'hod' | 'hr'): boolean {
    const status = this.getApprovalStatus(emp, role);
    return status === 'Submitted' || status === 'Accepted';
  }

  /** Open approval details popup for the given role (manager/hod/hr). Fetches details from API. */
  openApprovalDetails(emp: any, role: 'manager' | 'hod' | 'hr'): void {
    if (!this.canOpenApprovalDetails(emp, role)) return;
    const quarterId = this.EnabledAndActiveQuarterCycle?.[0]?.quarterId;
    if (!quarterId || !emp?.empId) return;
    const scrollY = window.scrollY || document.documentElement.scrollTop;
    this.approvalDetailsLoading = true;
    this.approvalDetailsPopup = { role: role === 'manager' ? 'Manager' : role === 'hod' ? 'HOD' : 'HR', name: '', id: '', employmentId: '', rating: '', feedback: '' };
    this.approvalDetailsModalRef = this.modalService.open(this.approvalDetailsModalTemplate, {
      size: 'md',
      centered: true,
      windowClass: 'pms-approval-modal-window'
    });
    if (this.approvalDetailsModalRef.shown) {
      this.approvalDetailsModalRef.shown.subscribe(() => window.scrollTo(0, scrollY));
    } else {
      setTimeout(() => window.scrollTo(0, scrollY), 100);
    }
    this.performanceService.getApprovalDetails(emp.empId, quarterId).pipe(first()).subscribe((response: any) => {
      this.approvalDetailsLoading = false;
      if (response?.serviceStatus === 'Success' && response?.serviceResponse) {
        const data = response.serviceResponse[role];
        if (data) {
          this.approvalDetailsPopup = {
            role: role === 'manager' ? 'Manager' : role === 'hod' ? 'HOD' : 'HR',
            name: data.name || '—',
            id: data.id != null ? data.id : '—',
            employmentId: data.employmentId || '—',
            rating: data.rating != null && data.rating !== '' ? data.rating : '—',
            feedback: data.feedback || '—'
          };
        }
      }
    }, () => { this.approvalDetailsLoading = false; });
  }

  closeApprovalDetailsModal(): void {
    this.approvalDetailsModalRef?.close();
    this.approvalDetailsPopup = null;
  }

  /** Status filter config for summary boxes and table filter. */
  private readonly summaryStatusConfig: Record<string, { title: string; matchStatus: string[] }> = {
    notStarted: { title: 'Not Started', matchStatus: ['not started'] },
    pendingHod: { title: 'Pending', matchStatus: ['pending hod'] },
    ongoing: { title: 'Ongoing', matchStatus: ['ongoing', 'submitted'] },
    completed: { title: 'Completed', matchStatus: ['completed'] },
    rejected: { title: 'Rejected', matchStatus: ['rejected'] }
  };

  /** Returns the list to display in the main table (by cycle filter and/or summary status). */
  getTableEmployeeList(): any[] {
    let list: any[];
    if (this.tableListMode === 'all') {
      list = this.allEmployee || [];
    } else if (this.tableListMode === 'notEligible') {
      list = this.getNotEligibleEmployees();
    } else {
      list = this.eligibleEmployees || [];
      if (this.summaryStatusFilter) {
        const matchStatus = this.summaryStatusConfig[this.summaryStatusFilter]?.matchStatus || [];
        list = list.filter(emp => {
          const s = (emp?.completionStatus || 'Not Started').toString().trim().toLowerCase();
          return matchStatus.includes(s);
        });
      }
    }
    return list;
  }

  /** Employees in allEmployee who are not in eligibleEmployees (by empId). */
  getNotEligibleEmployees(): any[] {
    const eligibleIds = new Set((this.eligibleEmployees || []).map((e: any) => e.empId));
    return (this.allEmployee || []).filter((a: any) => !eligibleIds.has(a.empId));
  }

  /** Scroll the page to the Employee list table. */
  scrollToEmployeeTable(): void {
    setTimeout(() => {
      const el = document.getElementById('employeeListTableSection');
      el?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }, 150);
  }

  /** Apply cycle filter: All / Eligible / Not eligible, then scroll to table. */
  applyCycleFilter(mode: 'all' | 'eligible' | 'notEligible'): void {
    this.tableListMode = mode;
    this.summaryStatusFilter = null;
    this.page = 1;
    this.scrollToEmployeeTable();
  }

  /** Apply performance summary filter: show only employees in this status, then scroll to table. */
  applySummaryStatusFilter(statusKey: 'notStarted' | 'pendingHod' | 'ongoing' | 'completed' | 'rejected'): void {
    this.tableListMode = 'eligible';
    this.summaryStatusFilter = statusKey;
    this.page = 1;
    this.scrollToEmployeeTable();
  }

  /** Clear table filter and show all employees (default view). */
  clearSummaryStatusFilter(): void {
    this.summaryStatusFilter = null;
    this.tableListMode = 'all';
    this.page = 1;
  }

  /** Whether the table has an active filter (cycle or status) to show the "Showing..." bar. */
  hasTableFilter(): boolean {
    return this.summaryStatusFilter != null || this.tableListMode !== 'all';
  }

  /** Label for current filter for the "Showing: ..." bar. */
  getTableFilterLabel(): string {
    if (this.summaryStatusFilter) return this.summaryStatusConfig[this.summaryStatusFilter]?.title || '';
    if (this.tableListMode === 'eligible') return 'Eligible employees';
    if (this.tableListMode === 'notEligible') return 'Not eligible';
    return '';
  }

  /** True if this employee is in the eligible list for the current cycle (can be evaluated and has rating). */
  isEligibleEmployee(emp: any): boolean {
    if (!emp?.empId || !this.eligibleEmployees?.length) return false;
    return this.eligibleEmployees.some((e: any) => e.empId === emp.empId);
  }

  /** Label for current summary filter (e.g. "Not Started") for the "Showing: ..." bar. */
  getSummaryStatusFilterLabel(): string {
    return this.summaryStatusFilter ? this.summaryStatusConfig[this.summaryStatusFilter]?.title || '' : '';
  }

  /** Opens popup listing employees for the given performance summary status (kept for any other use). */
  openStatusDetailPopup(statusKey: 'notStarted' | 'pendingHod' | 'ongoing' | 'completed' | 'rejected'): void {
    const config = this.summaryStatusConfig;
    const { title, matchStatus } = config[statusKey];
    const list = this.eligibleEmployees || [];
    this.summaryStatusEmployees = list.filter(emp => {
      const s = (emp?.completionStatus || 'Not Started').toString().trim().toLowerCase();
      return matchStatus.includes(s);
    });
    this.summaryStatusTitle = title + ' - Employees';
    this.summaryStatusModalRef = this.modalService.open(this.statusDetailModalTemplate, {
      size: 'xl',
      scrollable: true,
      windowClass: 'pms-status-detail-modal-window'
    });
  }

  closeStatusDetailPopup(): void {
    this.summaryStatusModalRef?.close();
    this.summaryStatusEmployees = [];
    this.summaryStatusTitle = '';
  }

  /** Numeric rating (0–5) for approval details popup star display. */
  getApprovalDetailRatingNum(): number {
    const r = this.approvalDetailsPopup?.rating;
    if (r == null || r === '' || r === '—') return 0;
    const n = Number(r);
    return isNaN(n) ? 0 : Math.min(5, Math.max(0, n));
  }

  /** Feedback section title by role: Manager Feedback | HOD Feedback | HR remark/feedback */
  getFeedbackSectionTitle(): string {
    if (this.userMapping?.performance_action_by_hr) return 'HR remark/feedback';
    if (this.userMapping?.performance_action_by_hod) return 'HOD Feedback';
    return 'Manager Feedback';
  }

  /** Feedback section label by role for the remarks field */
  getFeedbackSectionLabel(): string {
    if (this.userMapping?.performance_action_by_hr) return 'HR Remarks';
    if (this.userMapping?.performance_action_by_hod) return 'HOD Remarks';
    return 'Manager/Reporting Manager Remarks';
  }

  /** Disabled state for feedback textarea: HOD can always edit; Manager when Ongoing/Completed; HR when not in edit mode */
  getFeedbackTextareaDisabled(): boolean {
    if (this.userMapping?.performance_action_by_hr) return !this.isEditMode;
    if (this.userMapping?.performance_action_by_hod) return false;
    return this.currentStatus === 'Ongoing' || this.currentStatus === 'Completed';
  }

  onReview(eligiemployee: any) {
    this.isperformanceDsah = false;
    this.isreviewPage = true;
    this.selectedEmployee = eligiemployee;
    console.log("eligiemployee", eligiemployee);
    this.selectedEmployee.emp360 = eligiemployee.emp360;
    console.log("eligiemployee.emp360", eligiemployee.emp360);
    this.myList = [];
    this.myRateList = [];
    this.getCountOfRewardsAndAppreciation();
    if (this.EnabledAndActiveQuarterCycle?.length) {
      this.setQuartedId(this.EnabledAndActiveQuarterCycle[0].quarterId);
    }
  }

  back() {
    this.getAllEmployee();
    this.getAllEmployeesCurrentStatus();
    this.isperformanceDsah = true;
    this.isreviewPage = false;
    setTimeout(() => {
      this.renderPlaceholderChart("Rating", "performanceId", this.departmentData);
    }, 100);
  }

  toggleData(event) {
    if (event.target.checked) {
      this.renderPlaceholderChart("Pending", "performanceId", this.departmentData);
    } else {
      this.renderPlaceholderChart("Rating", "performanceId", this.departmentData);
    }
  }

  submitReviewEmployee(quarter: any, template: TemplateRef<any>, index: any) {
    this.submitPerformance.empId = this.selectedEmployee.empId;
    this.submitPerformance.currentStatus = this.currentStatus;
    this.submitPerformance.quarterId = quarter.quarterId;
    this.submitPerformance.hodId = this.currentUser.empId;
    if(this.userMapping.performance_action_by_hod){
      this.submitPerformance.actionBy = 'HOD'
    }
    else if (this.userMapping.performance_action_by_approvals_tos) {
      this.submitPerformance.actionBy = 'RM';
    }
    this.submitPerformance.performanceRatings = [];
    this.filterCriteria.forEach((item, index) => {
      if (this.myList[index] && this.myList[index].silde !== undefined) {
        this.submitPerformance.performanceRatings.push({
          reviewTypeId: item.reviewTypeId,
          rating: this.myList[index].silde,
          performanceRatingId: null
        });
      }
    });
    this.filterRatingCriteria.forEach((item, index) => {
      if (this.myRateList[index] && this.myRateList[index].rate !== undefined) {
        this.submitPerformance.performanceRatings.push({
          reviewTypeId: item.reviewTypeId,
          rating: this.myRateList[index].rate,
          performanceRatingId: null
        });
      }
    });
    this.submitPerformance.finalRating = this.finalRating;
    this.submitPerformance.hodRemarks = this.hodRemarks;
    if (!this.validationService.validateNullUndefinedEmptyString(this.submitPerformance.hodRemarks)) {
      this.alertMessage = "Please justify your rating by providing remarks!";
      this.openAlertMod(template, this.alertMessage);
      return;
    }
    console.log(this.submitPerformance, "performance");
    this.performanceService.submitEmployeePerformanceHOD(this.submitPerformance).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse, true);
        let collapseElement = document.getElementById('collapse' + index);
        if (collapseElement) {
          collapseElement.classList.remove('show'); // Remove 'show' class
        }
      } else {
        this.openAlertMod(template, response.serviceResponse, true);
      }
    });
  }

  getRatingList(limit: number): number[] {
    return Array.from({ length: limit }, (_, i) => i + 1);
  }

  isClicked = false;
  enableDisableSubmit: boolean = false;
  performnace1: any = new Performance();

  /** Selected quarter object for the current quarterId (used when details are shown directly). */
  getSelectedQuarter(): any {
    if (!this.EnabledAndActiveQuarterCycle?.length || this.quarterId == null) return null;
    return this.EnabledAndActiveQuarterCycle.find((q: any) => q.quarterId === this.quarterId)
      || this.EnabledAndActiveQuarterCycle[0];
  }
  getSelectedQuarterIndex(): number {
    const q = this.getSelectedQuarter();
    if (!q) return 0;
    const i = this.EnabledAndActiveQuarterCycle.indexOf(q);
    return i >= 0 ? i : 0;
  }
  setQuartedId(quartId: any) {
    this.quarterId = quartId;
    this.myList = [];
    this.myRateList = [];
    this.submitPerformance.finalRating = '';
    this.hodRemarks = '';
    this.isClicked = !this.isClicked;
    this.isAcceptSelected = false;
    this.isRejectSelected = false;
    this.currentStatus = '';
    this.performnace.empId = this.selectedEmployee.empId;
    this.performnace.quarterId = quartId;
    this.HrAndHodView(this.performnace);
  }

  currentStatus: any;
  rejectStatus: any;

  HrAndHodView(performance: any) {
    this.performanceSerive.hrAndHodEmpoyeePerformanceView(performance).pipe(first()).subscribe((response: any) => {
      this.enableDisableSubmit = false;
      if (response.serviceStatus == "Success") {
        console.log('inside if block');
        this.performnace1 = response.serviceResponse;
        console.log("given by hod", this.performnace1);
        this.currentStatus = this.performnace1[0].completionStatus;
        this.enableDisableSubmit = !this.enableDisableSubmit;
        this.filterRatingCriteria = this.performnace1.filter(item => item.deptId == this.selectedEmployee.departmentId && item.reviewFieldType === 'Rating' && item.empId == this.selectedEmployee.empId && item.quarterId == this.performnace.quarterId);
        this.filterCriteria = this.performnace1.filter(item => item.deptId == this.selectedEmployee.departmentId && item.reviewFieldType === 'Slider' && item.empId == this.selectedEmployee.empId && item.quarterId == this.performnace.quarterId);
        this.filterCriteria.forEach(value => {
          this.myList.push({ reviewLabel: value.reviewLabel, silde: value.ratingValue, performanceRatingId: value.performanceRatingId, comment: (value != null && value.criteriaRemark != null && value.criteriaRemark !== '') ? value.criteriaRemark : '' });
          this.finalRating = value.finalRating;
          this.hodRemarks = value.hodRemarks;
          this.hrRemarks = value.hrRemark != null ? value.hrRemark : this.hrRemarks;
          this.hrReviewStatus = value.hrReviewStatus;
          this.acceptReason = value.hrRemark;
          this.rejectStatus = value.rejectStatus;
        });
        this.filterRatingCriteria.forEach(value => {
          this.myRateList.push({ reviewLabel: value.reviewLabel, rate: value.ratingValue, performanceRatingId: value.performanceRatingId, comment: (value != null && value.criteriaRemark != null && value.criteriaRemark !== '') ? value.criteriaRemark : '' });
          this.finalRating = value.finalRating;
          this.hodRemarks = value.hodRemarks;
          this.hrRemarks = value.hrRemark != null ? value.hrRemark : this.hrRemarks;
          this.hrReviewStatus = value.hrReviewStatus;
          this.acceptReason = value.hrRemark;
          this.rejectStatus = value.rejectStatus;
        });
        this.calculateFinalRating();
      } else {
        console.log('inside else part');
        this.currentStatus = 'Not Started';
        this.enableDisableSubmit = false;
        this.filterCriteriaQuarter = this.allReviewType.filter(item => item.quarterId === this.quarterId);
        this.filterCriteria = this.filterCriteriaQuarter.filter(item => item.departmentName == this.selectedEmployee.departmentName && item.reviewFieldType === 'Slider');
        this.filterRatingCriteria = this.filterCriteriaQuarter.filter(item => item.departmentName == this.selectedEmployee.departmentName && item.reviewFieldType === 'Rating');
        console.log('==================filter',this.filterRatingCriteria);
        this.filterCriteria.forEach(value => {
          this.myList.push({ reviewLabel: value.reviewLabel, silde: 0, performanceRatingId: null, comment: '' });
          this.finalRating = null;
          this.hodRemarks = null;
        });
        this.filterRatingCriteria.forEach(value => {
          this.myRateList.push({ reviewLabel: value.reviewLabel, rate: 0, performanceRatingId: null, comment: '' });
          this.finalRating = null;
          this.hodRemarks = null;
        });
        this.calculateFinalRating();
      }
    });
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'Ongoing':
        return '#FF6C37';
      case 'Completed':
        return '#04724D';
      case 'Rejected':
        return '#931621';
      case 'Pending HOD':
        return '#E67E22';
      default:
        return '#A8A8A8';
    }
  }
  /** Performance score as percentage (0-100) from finalRating out of 5. For Overall Rating Summary. */
  getPerformanceScorePercent(): number {
    const r = this.finalRating;
    if (r == null || isNaN(Number(r))) return 0;
    return Math.min(100, Math.round((Number(r) / 5) * 1000) / 10);
  }
  /** Performance category from finalRating (criteria-based): NI / M- / M / M+ / E. */
  getPerformanceCategory(): string {
    const r = this.finalRating;
    if (r == null || isNaN(Number(r))) return 'N/A';
    return this.getRatingCategory(Number(r));
  }
  /** Average rating label for Overall Rating Summary from criteria (finalRating on 0–5 scale): "X.X / 5.0" or "N/A". */
  getCriteriaSummaryLabel(): string {
    const r = this.finalRating;
    if (r == null || isNaN(Number(r))) return 'N/A';
    const n = Math.min(5, Math.max(0, Number(r)));
    return (Math.round(n * 10) / 10).toFixed(1) + ' / 5.0';
  }
  /** Rating label for a row e.g. "4.0/5.0". */
  getCriteriaRatingLabel(rate: number, maxStars: number): string {
    if (rate == null || isNaN(Number(rate))) return '0/' + (maxStars || 5);
    const n = Math.min(maxStars || 5, Math.max(0, Number(rate)));
    return (Math.round(n * 10) / 10).toFixed(1) + '/' + (maxStars || 5) + '.0';
  }
  /** Number of filled stars (0-5) for Overall Rating Summary from finalRating. */
  getFinalRatingStarsCount(): number {
    const r = this.finalRating;
    if (r == null || isNaN(Number(r))) return 0;
    return Math.min(5, Math.max(0, Math.round(Number(r))));
  }
  /** Formatted final rating for display e.g. "3.67" or "0". */
  getFinalRatingFormatted(): string {
    const r = this.finalRating;
    if (r == null || isNaN(Number(r))) return '0';
    return (Math.round(Number(r) * 100) / 100).toFixed(2);
  }

  isAcceptSelected: boolean = false;
  isRejectSelected: boolean = false;

  selectAction(action: string) {
    if (action === 'accept') {
      this.isRejectSelected = false;
      this.isAcceptSelected = true;
    } else if (action === 'reject') {
      this.isAcceptSelected = false;
      this.isRejectSelected = true;
    }
  }

  submitRemarkHr: Performance = new Performance();
  acceptReason: any;
  rejectReason: any;
  submitRemarksByHR(quarter: any, template: TemplateRef<any>, index: any) {
    if (this.userMapping.performance_action_by_hod) {
      if ((this.isAcceptSelected || this.isRejectSelected) && !this.validationService.validateNullUndefinedEmptyString(this.hodRemarks)) {
        this.alertMessage = "Please enter HOD Remarks!";
        this.openAlertMod(template, this.alertMessage);
        return;
      }
    } else {
      // HR: use HR Remarks field for accept/reject reason (same section as manager-to-HOD flow)
      if (this.isAcceptSelected && !this.validationService.validateNullUndefinedEmptyString(this.hrRemarks)) {
        this.alertMessage = "Please enter HR Remarks!";
        this.openAlertMod(template, this.alertMessage);
        return;
      }
      if (this.isRejectSelected && !this.validationService.validateNullUndefinedEmptyString(this.hrRemarks)) {
        this.alertMessage = "Please enter HR Remarks!";
        this.openAlertMod(template, this.alertMessage);
        return;
      }
    }
    this.submitRemarkHr.empId = this.selectedEmployee.empId;
    this.submitRemarkHr.quarterId = quarter.quarterId;
    this.submitRemarkHr.performanceRatings = [];
    this.filterCriteria.forEach((item, index) => {
      this.submitRemarkHr.employeePerformanceId = item.employeePerformanceId;
      if (this.myList[index] && this.myList[index].silde !== undefined) {
        this.submitRemarkHr.performanceRatings.push({
          reviewTypeId: item.reviewTypeId,
          rating: this.myList[index].silde,
          performanceRatingId: this.myList[index].performanceRatingId,
        });
      }
    });
    this.filterRatingCriteria.forEach((item, index) => {
      this.submitRemarkHr.employeePerformanceId = item.employeePerformanceId;
      if (this.myRateList[index] && this.myRateList[index].rate !== undefined) {
        this.submitRemarkHr.employeePerformanceId = item.employeePerformanceId;
        this.submitRemarkHr.performanceRatings.push({
          reviewTypeId: item.reviewTypeId,
          rating: this.myRateList[index].rate,
          performanceRatingId: this.myRateList[index].performanceRatingId,
        });
      }
    });

    this.submitRemarkHr.finalRating = this.finalRating;
    this.submitRemarkHr.employeePerformanceId = this.performnace1[0].employeePerformanceId;

    if (this.userMapping.performance_action_by_hod) {
      this.submitRemarkHr.hodId = this.currentUser.empId;
      this.submitRemarkHr.hodRemarks = this.hodRemarks;
      this.submitRemarkHr.hodReviewStatus = this.isAcceptSelected ? 'Accepted' : 'Rejected';
      this.performanceService.submitRemarksByHOD(this.submitRemarkHr).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == 'Success') {
          this.openAlertMod(template, response.serviceResponse, true);
          const collapseElement = document.getElementById('collapse' + index);
          if (collapseElement) collapseElement.classList.remove('show');
        } else {
          this.openAlertMod(template, response.serviceResponse, true);
        }
      });
    } else {
      this.submitRemarkHr.hrReviewStatus = this.isAcceptSelected ? 'Accepted' : 'Rejected';
      this.submitRemarkHr.hrRemark = this.hrRemarks;
      this.submitRemarkHr.hrId = this.currentUser.empId;
      this.performanceService.submitEmployeePerformanceHR(this.submitRemarkHr).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == 'Success') {
          this.openAlertMod(template, response.serviceResponse, true);
          const collapseElement = document.getElementById('collapse' + index);
          if (collapseElement) collapseElement.classList.remove('show');
        } else {
          this.openAlertMod(template, response.serviceResponse, true);
        }
      });
    }
  }

  onHodSubmit(quarter: any, template: TemplateRef<any>, index: any) {
    if (this.isAcceptSelected || this.isRejectSelected) {
      this.submitRemarksByHR(quarter, template, index);
    } else {
      this.updateReviewEmployee(quarter, template, index);
    }
  }

  /** HOD: Accept in one click – submit as Accepted using HOD Remarks. */
  onHodAccept(quarter: any, template: TemplateRef<any>, index: any) {
    this.isAcceptSelected = true;
    this.isRejectSelected = false;
    this.submitRemarksByHR(quarter, template, index);
  }

  /** HOD: Reject in one click – submit as Rejected using HOD Remarks. */
  onHodReject(quarter: any, template: TemplateRef<any>, index: any) {
    this.isAcceptSelected = false;
    this.isRejectSelected = true;
    this.submitRemarksByHR(quarter, template, index);
  }

  /** HR: Accept – set hr_review_status to Accepted using HR Remarks. */
  onHrAccept(quarter: any, template: TemplateRef<any>, index: any) {
    this.isAcceptSelected = true;
    this.isRejectSelected = false;
    this.submitRemarksByHR(quarter, template, index);
  }

  /** HR: Reject – set hr_review_status to Rejected using HR Remarks. */
  onHrReject(quarter: any, template: TemplateRef<any>, index: any) {
    this.isAcceptSelected = false;
    this.isRejectSelected = true;
    this.submitRemarksByHR(quarter, template, index);
  }

  updateReviewEmployee(quarter: any, template: TemplateRef<any>, index: any) {
    this.submitPerformance.empId = this.selectedEmployee.empId;
    this.submitPerformance.quarterId = quarter.quarterId;
    this.submitPerformance.hodId = this.currentUser.empId;
    this.submitPerformance.employeePerformanceId = null;
    this.submitPerformance.actionBy = (this.currentStatus === 'Rejected' && (this.userMapping.performance_action_by_approvals_to || this.userMapping.performance_action_by_approvals_tos)) ? 'RM' : 'HOD';
    this.submitPerformance.performanceRatings = [];
    this.filterCriteria.forEach((item, index) => {
      this.submitPerformance.employeePerformanceId = item.employeePerformanceId;
      if (this.myList[index] && this.myList[index].silde !== undefined) {
        this.submitPerformance.performanceRatings.push({
          reviewTypeId: item.reviewTypeId,
          rating: this.myList[index].silde,
          performanceRatingId: this.myList[index].performanceRatingId,
        });
      }
    });
    this.filterRatingCriteria.forEach((item, index) => {
      this.submitPerformance.employeePerformanceId = item.employeePerformanceId;
      if (this.myRateList[index] && this.myRateList[index].rate !== undefined) {
        this.submitPerformance.employeePerformanceId = item.employeePerformanceId;
        this.submitPerformance.performanceRatings.push({
          reviewTypeId: item.reviewTypeId,
          rating: this.myRateList[index].rate,
          performanceRatingId: this.myRateList[index].performanceRatingId,
        });
      }
    });
    this.submitPerformance.finalRating = this.finalRating;
    this.submitPerformance.hodRemarks = this.hodRemarks;
    console.log(this.submitPerformance, "performance");
    this.performanceService.updateEmployeePerformanceHOD(this.submitPerformance).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse, true);
        let collapseElement = document.getElementById('collapse' + index);
        if (collapseElement) {
          collapseElement.classList.remove('show'); // Remove 'show' class
        }
      } else {
        this.openAlertMod(template, response.serviceResponse, true);
      }
    });
  }
  
  averageRating!:any ;
  getCountOfRewardsAndAppreciation(){
    this.appreciationCount='';
    this.rewardsCount='';
    console.log("this.projectDetails ", this.rewardsCount);
    this.appreciationAndRewardsCount.empId = this.selectedEmployee.empId;
    this.employee360Service.getRewardsAndAppreciationCount(this.appreciationAndRewardsCount).pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus == "Success") {
            this.rewardsCount = response.serviceResponse[0].rewardsCount;
            this.appreciationCount = response.serviceResponse[0].appreciationCount;
            this.averageRating = response.serviceResponse[0].averageRating;

            console.log("this.projectDetails ",  this.appreciationCount);

          }
        });
  }


  getCurrentUserDepartment() {
  this.performanceService.getCurrentUserDepartment(this.currentUser.empId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        // Find the department ID that matches the user's department name
        const userDept = this.departments.find(dept => dept.name === response.serviceResponse);
        
        if (userDept) {
          this.currentUserdepartmentName = userDept.deptId.toString();
          this.selectedDepartment = userDept.deptId.toString();
          this.userDetailsForPerformanceView.deptId = userDept.deptId;
          this.getALLdepartmentByEmployee();
        } else {
          this.currentUserdepartmentName = 'all';
        }
        
        console.log("currentUserdepartmentName", this.currentUserdepartmentName);
      } else {
        this.currentUserdepartmentName = 'all';
      }
    });
  }
  //  toggleEditMode() {
  //   this.isEditMode = !this.isEditMode;
  //   console.log("==========edit mode",this.isEditMode);
  // }
  backupMyList: any[] = [];
  backupMyRateList: any[] = [];
  
// Updated toggleEditMode method
toggleEditMode() {
  if (this.isEditMode) {
    const hasChanges = this.hasDataChanged();
    
    if (hasChanges) {
      this.confirmModalRef = this.modalService.open(this.confirmDiscardModal, {
        centered: true,
        backdrop: 'static',
        keyboard: false
      });
    } else {
      this.isEditMode = false;
      console.log("Edit mode OFF - No changes");
    }
  } else {
    
    this.createBackupData();
    this.isEditMode = true;
    console.log("Edit mode ON - Backup created");
  }
}

// Handle confirmation response
confirmDiscardChanges(discard: boolean) {
  this.confirmDiscard = discard;
  
  if (this.confirmDiscard) {
    // User chose to discard changes
    this.restoreBackupData();
    this.isEditMode = false;
    console.log("Edit mode OFF - Changes discarded");
  } else {
    // User chose to keep editing
    console.log("Edit mode still ON - User chose to keep editing");
  }
  
  // Close the confirmation modal
  this.confirmModalRef.close();
}

// Create backup of current data
createBackupData() {
  this.backupMyList = JSON.parse(JSON.stringify(this.myList));
  this.backupMyRateList = JSON.parse(JSON.stringify(this.myRateList));
  console.log("Backup created:", { backupMyList: this.backupMyList, backupMyRateList: this.backupMyRateList });
}

// Restore data from backup
restoreBackupData() {
  this.myList = JSON.parse(JSON.stringify(this.backupMyList));
  this.myRateList = JSON.parse(JSON.stringify(this.backupMyRateList));
  console.log("Data restored from backup");
}

// Check if data has changed
hasDataChanged(): boolean {
  const currentMyList = JSON.stringify(this.myList);
  const backupListStr = JSON.stringify(this.backupMyList);
  
  const currentMyRateList = JSON.stringify(this.myRateList);
  const backupRateListStr = JSON.stringify(this.backupMyRateList);
  
  const hasChanges = (currentMyList !== backupListStr) || (currentMyRateList !== backupRateListStr);
  
  console.log("Has changes:", hasChanges);
  return hasChanges;
}
  updateReviewByHr(quarter: any, template: TemplateRef<any>, index: any)
  {
    const hrFeedback = (this.hrRemarks != null && this.hrRemarks !== '') ? this.hrRemarks.trim() : '';
    if (!hrFeedback) {
      this.alertMessage = 'Please enter HR remark/feedback.';
      this.openAlertMod(template, this.alertMessage);
      return;
    }
    this.updatePerformanceHr.empId = this.selectedEmployee.empId;
    this.updatePerformanceHr.quarterId = quarter.quarterId;
    this.updatePerformanceHr.hrId = this.currentUser.empId;
    this.updatePerformanceHr.employeePerformanceId = null;
    this.updatePerformanceHr.performanceRatings = [];
    this.filterCriteria.forEach((item, index) => {
      this.updatePerformanceHr.employeePerformanceId = item.employeePerformanceId;
      if (this.myList[index] && this.myList[index].silde !== undefined) {
        this.updatePerformanceHr.performanceRatings.push({
          reviewTypeId: item.reviewTypeId,
          rating: this.myList[index].silde,
          performanceRatingId: this.myList[index].performanceRatingId,
        });
      }
    });
    this.filterRatingCriteria.forEach((item, index) => {
      this.updatePerformanceHr.employeePerformanceId = item.employeePerformanceId;
      if (this.myRateList[index] && this.myRateList[index].rate !== undefined) {
        this.updatePerformanceHr.employeePerformanceId = item.employeePerformanceId;
        this.updatePerformanceHr.performanceRatings.push({
          reviewTypeId: item.reviewTypeId,
          rating: this.myRateList[index].rate,
          performanceRatingId: this.myRateList[index].performanceRatingId,
        });
      }
    });
    this.updatePerformanceHr.finalRating = this.finalRating;
    this.updatePerformanceHr.hodRemarks = this.hodRemarks;
    this.updatePerformanceHr.hrRemark = hrFeedback;
    console.log(this.updatePerformanceHr, "performance update by hrrrr");
   
    this.performanceService.updateEmployeePerformanceHr(this.updatePerformanceHr).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse, true);
        let collapseElement = document.getElementById('collapse' + index);
        if (collapseElement) {
          collapseElement.classList.remove('show'); // Remove 'show' class
        }
      } else {
        this.openAlertMod(template, response.serviceResponse, true);
      }
    });
  }

  /** Close the review panel (Cancel button). */
  cancelReview(index: number) {
    const collapseElement = document.getElementById('collapse' + index);
    if (collapseElement) collapseElement.classList.remove('show');
  }

  /** Whether criteria ratings (slider/stars) and comments can be edited. Manager: Not Started or Rejected (resubmit flow); HOD: always; HR: when isEditMode. */
  canEditRating(): boolean {
    if (this.userMapping?.performance_action_by_hr && this.isEditMode) return true;
    if (this.userMapping?.performance_action_by_hod) return true;
    if (this.userMapping?.performance_action_by_approvals_tos && (this.currentStatus === 'Not Started' || this.currentStatus === 'Rejected')) return true;
    if (this.userMapping?.performance_action_by_approvals_to && this.currentStatus === 'Rejected') return true;
    return false;
  }
}
