
import { LocationStrategy } from '@angular/common';
import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import * as Highcharts from 'highcharts';
import * as moment from 'moment';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Performance } from 'src/app/models/performance';
import { Query } from 'src/app/models/query';
import { SortPipe } from 'src/app/sort.pipe';
import { AppreciationAndRewardsCount } from 'src/app/models/appreciationAndRewardCount';
import { Employee } from 'src/app/models/employee';
import { Feature } from 'src/app/models/feature';
import { HrHodMangerApiForPerformnace } from 'src/app/models/hrHodMangerApiForPerformnace';
import { Log } from 'src/app/models/log';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { DepartmentService } from 'src/app/services/department.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { Employee360Service } from 'src/app/services/employee360.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { LogService } from 'src/app/services/log.service';
import { PerformanceService } from 'src/app/services/performance.service';
import { UtilityService } from 'src/app/services/utility.service';
import { ValidationService } from 'src/app/services/validation.service';
import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { DomainService } from 'src/app/services/domain.service';
import { ActivatedRoute, Params, Router } from '@angular/router';


class FilterData {
  title: any;
  columns: any;
  queryList: any;
}


@Component({
  standalone: false,
  selector: 'performance-management-system',
  templateUrl: './performance-management-system.component.html',
  styleUrls: ['./performance-management-system.component.css']
})
export class  PerformanceManagementSystemComponent implements OnInit {

  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;
  page = 1;
  filters: any = {};
  filterData: any = new FilterData();
  alertMessage: any;
  modalRef:NgbModalRef;
  submitPerformance: Performance = new Performance();
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;
  queryList: any[] = [];
  storedDataList: any[] = [];
  data: string;
  employeeDataForExcel: any[] = [];
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

  eligibleEmployeesColumns: any[] = ['employeementId', 'name', 'designationName', 'departmentName','totalExperience', 'employmentstatus', 'dateOfJoining','completionStatus'];
  finalRating: number;
  hodRemarks: any;
  quarterId: any;
  rewardsCount:any;
  appreciationCount:any;

  appreciationAndRewardsCount:AppreciationAndRewardsCount=new AppreciationAndRewardsCount();
  feature = "Performance";
  currentUser: User;
  userMapping: any = {};
  log: Log;
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
  selectedGoalData: any[];
  goalTemplates: any[];
  // http: any;
  multiGoalTemplate: string | TemplateRef<any> | (new (...args: any[]) => any);
viewTeamMember: any;
isEmployeeDashboard: any;
isfileUpload: any;
  file: any;

  constructor(private logService: LogService,
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
    private departmentService:DepartmentService,
    private domainService : DomainService,
    private http: HttpClient,
    private router: Router,
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
      await this.getALLdepartmentByEmployee1();


      // Then call other methods

      this.getAllReviveType();
      this.getAllQauterCycle();

      // Continue with user mapping logic
      let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
      featureMap.subFeatures?.forEach(sub => {
        this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
      });

      this.isperformanceDsah = true;
      console.log("usermappinghodhr",this.userMapping);
      // console.log("hodddddd", this.userMapping.performance_action_by_hod);
      // console.log("hrrrrrrrr", this.userMapping.performance_action_by_hr);
       this.getAllDepartments();
       this.getAllEmployeesCurrentStatus();
        this.getAllEmployee1();

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


   openFilterModal1(template: TemplateRef<any>, columns:any[], title:any) {
        //console.log("columns : ", columns);
        this.queryList = [];
        let dateFormat = 'DD-MM-YYYY';

        this.filterData.title  = title;
        this.filterData.columns = columns;

        if(this.filterData.title == 'Filter Timesheet Summary' || this.filterData.title == 'Filter Leave Trend Chart'){

          let fromDate = moment().subtract(8, 'd').format(dateFormat);
          let toDate = moment().format(dateFormat);

          this.queryList = [
            { column: "From Date", operator: ">=", value: fromDate, conjunction: "AND" },
            { column: "To Date", operator: "<=", value: toDate, conjunction: "" }
          ];
        }

        this.storedDataList.forEach((data) => {
          if(data.filterName == title){
            data.queryList.forEach((queryObj) => {
              if(queryObj.column == "Employee Id" && !queryObj.value.includes("A-")){
                queryObj.value = "A-".concat(queryObj.value);
              }
              // if(queryObj.column == "Employee Id" && !queryObj.value.includes("A-CS-") && (data.isConsultant == 'true')){
              //   queryObj.value = "A-CS-".concat(queryObj.value);
              // }
              // queryObj.employeeType =(queryObj.isConsultant === 'true') ? "Consultant" :((queryObj.isApprenticeship === 'true') ? "Apprentice" : "Regular");
              if(queryObj.column == "Employee Id" && !queryObj.value.includes("A-")){
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

        //console.log("filterData : ", this.filterData);
        this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
      }

      onFilterSubmit1(emittedArray:any , template:TemplateRef<any>){
        if (emittedArray[0].length != 0) {
          //console.log("queryList : ", emittedArray[0]);
          this.queryList = JSON.parse(JSON.stringify(emittedArray[0]));
          this.cancelRequest();

          emittedArray[1].forEach((object) => {
            if (Object.keys(object).length !== 0) {
              this.storedDataList.push(object);
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

          //console.log("updated queryList : ", emittedArray[0]);

            this.getCustomEmployeesList(emittedArray[0], template);


        }
      }

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


  getAllEmployee1() {
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
                completionStatus: staticData ? staticData.completionStatus : null
            };
        });
          console.log("allEmp", mergedData);

          const currentDate = new Date();
          const oneYearAgo = new Date(currentDate.getFullYear() - 1, 11, 31);

          // this.allEmployee = this.allEmployee.filter(employee => employee.empId !== this.currentUser.empId);
          this.eligibleEmployees = mergedData.filter(employee => {
            // Append employee ID using utility service
            employee.employeementId = this.utilityService.appendEmployeementid(employee.isConsultant, employee.employeementId);

            const joiningDate = new Date(employee.dateOfJoining);
   return joiningDate <= oneYearAgo && employee.employmentstatus === 'Confirmed';

            // if (this.currentUser.employeeRole !== 'HR') {
            //   alert('You are not authorized..!!');
            // }


            return false;
          });
          this.eligibleEmployees.forEach(eligibleEmp => {

            eligibleEmp.emp360 = eligibleEmp.empId;

          });


        } else {
          console.error(response.serviceResponse);
        }
      }

      )
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

  cancelRequest() {
    this.modalRef?.close();
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


  async getALLdepartmentByEmployee1() {
    try {
      let payload = null;
      const response: any = await this.performanceSerive.getALLdepartmentByEmployee(payload).toPromise();

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

     this.getALLdepartmentByEmployee1();
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


    onBillableFileSelect(event: any, template: TemplateRef<any>){
      const uploadedFiles = event.target.files;
      console.log("uploadedFiles ", uploadedFiles);
      this.file = uploadedFiles[0];
      const formData = new FormData();
      formData.append('file', this.file);

      this.domainService.billableFile(formData).pipe(first()).subscribe(
        (response: any) => {
          if (response.serviceStatus == "Success") {
            this.openAlertMod(template, response.serviceResponse);
          } else {
            this.openAlertMod(template, response.serviceResponse);
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



    this.getAllEmployee1();
  }


  handlePageChange(event) {
    this.page = event;
  }

  sortData(sort: Sort) {

    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

  name = 'EmployeeSheet.xlsx';
  exportToExcel(): void {
    this.userDetailsForPerformanceView.empId = this.currentUser.empId;
  this.userDetailsForPerformanceView.hrvalidate = this.userMapping.performance_action_by_hr;
    this.performanceService.getAllEmployeesForPerformanceExcell(this.userDetailsForPerformanceView).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        //  this.employeeDataForExcel = response.serviceResponse;

        this.allEmployee1 = response.serviceResponse;

        console.log("allEmp", this.allEmployee);

        const currentDate = new Date();
        const oneYearAgo = new Date(currentDate.getFullYear() - 1, 11, 31);

        this.eligibleEmployees1 = this.allEmployee1.filter(employee => {

          const joiningDate = new Date(employee.dateOfJoining);
          return joiningDate <= oneYearAgo && employee.employmentstatus === 'Confirmed';
        });
        this.employeeDataForExcel = this.eligibleEmployees1;

      }
      const onlySpecificDataArr = this.employeeDataForExcel.map(
        x => ({
          // "EmployeeId":"A-".concat(x.employeementId),
          "EmployeeId": (x.isConsultant === 'true' ? 'A-CS-' : 'A-') + x.employeementId,
          "Full Name": x.name,
          "EmailId": x.email,
          "Employment Status": x.employmentstatus,
          // "Date of Joining": (x.dateOfJoining) ? moment(x.dateOfJoining).format(AppComponent.DATE_FORMAT) : null,
          // "Date of Relieving": (x.dateOfRelieving) ? moment(x.dateOfRelieving).format(AppComponent.DATE_FORMAT) : null,
          "Department Name": x.departmentName,
          "Billable Type": x.billableType,
          "Experience" :x.totalExperience,
          "quarter Cycle":x.quarterycle || 'NULL',
          "financial Year": x.financialYear || 'NULL' ,
          "Current Status":x.completionStatus,
          "hod Name":x.hodName,
          "Final Rating":x.finalRating || 'NULL',
          "Manger Remark":x.hodRemarks || 'NULL',
          "Hod Remarks":x.hrRemarks || 'NULL',
          "Hod Review Status":x.hrReviewStatus || 'NULL'



        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.name)
    });
  }

  getCustomEmployeesList(queryObjList: any, template: TemplateRef<any>) {
    this.eligibleEmployees = [];

    let queryObj = new Query();
    queryObj.queryList = queryObjList;

    if (queryObjList == '') {
      this.getAllEmployee1();
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
          });
          console.log("allEmployeeList : ", this.eligibleEmployees)
        } else {
          this.openAlertMod(template, response.serviceResponse)
        }
      });
    }
  }


  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  onSearch(searchData) {
    this.filters = searchData;
  }

  myList: { reviewLabel: any; silde: any, performanceRatingId: any }[] = [];
  myRateList: { reviewLabel: any; rate: any, performanceRatingId: any }[] = [];

  allQauterCycle2: any;

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

  }

  back() {
    this.getAllEmployee1();
    this.getAllEmployeesCurrentStatus();
    this.isperformanceDsah = true;
    this.isreviewPage = false;
    setTimeout(() => {
      this.renderPlaceholderChart("Rating", "performanceId", this.departmentData);
    }, 100);

  }

  calculateFinalRating() {
    let totalRatings = 0;
    let count = 0;


    this.myList.forEach(item => {
      if (item.silde !== undefined) {
        totalRatings += item.silde;
        count++;
      }
    });


    this.myRateList.forEach(item => {
      if (item.rate !== undefined) {
        totalRatings += item.rate;
        count++;
      }
    });


    this.finalRating = count > 0 ? totalRatings / count : null;
  }











  toggleData(event) {


    if (event.target.checked) {

      this.renderPlaceholderChart("Pending", "performanceId", this.departmentData);

    } else {

      this.renderPlaceholderChart("Rating", "performanceId", this.departmentData);
    }
  }




  renderPlaceholderChart(chartName: string, chartId: string, departmentData: any) {
    // Ensure departmentData is available and has the expected structure
    if (!departmentData || departmentData.length === 0) {
      console.error('Department data is empty or undefined.');
      return;
    }

    // Categories for the chart (department names)
    const categories = departmentData.map(dep => dep.department);

    // Rating and Pending Rating Percentages arrays
    let ratingPercentages = [];
    let pendingratingPercentages = [];

    // Handle chart rendering based on chartName ('Rating' or 'Pending')
    if (chartName === 'Rating') {
      // Calculate Rating Percentages
      ratingPercentages = departmentData.map(dep => {
        return (dep.ratinggivenbymanager / dep.TotalNumberofemp) * 100;
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
          title: { text: 'Percentage of Employees Given Rating' },
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
            const TotalEmpcount = departmentData[this.index].TotalNumberofemp;
            return `<b>Rating Percentage: ${this.y}%</b><br><b>HOD: ${managerName}</b><br><b>Total Emp Count: ${TotalEmpcount}</b>`;
          }
        },
        series: [
          {
            name: 'Rating Percentage',
            type: 'column',
            data: ratingPercentages.map((percentage, index) => ({
              y: parseFloat(percentage.toFixed(2)),  // Ensure to limit decimal points
              managerName: departmentData[index].managerName // Accessing manager name directly from departmentData
            }))
          },
          {
            name: 'Bell Curve Line',
            type: 'spline',
            data: ratingPercentages,
            color: 'black',
            marker: { enabled: false }
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









  submitReviewEmployee(quarter: any, template: TemplateRef<any>, index: any) {
    this.submitPerformance.empId = this.selectedEmployee.empId;
    this.submitPerformance.quarterId = quarter.quarterId;
    this.submitPerformance.hodId = this.currentUser.empId;
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
        this.openAlertMod(template, response.serviceResponse);
        let collapseElement = document.getElementById('collapse' + index);
        if (collapseElement) {
          collapseElement.classList.remove('show'); // Remove 'show' class
        }

      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });

  }



  getRatingList(limit: number): number[] {
    return Array.from({ length: limit }, (_, i) => i + 1);
  }

  isClicked = false;
  enableDisableSubmit: boolean = false;
  performnace1: any = new Performance();
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


  currentStatus:any;
  rejectStatus:any;
  HrAndHodView(performance:any){
    this.performanceSerive.hrAndHodEmpoyeePerformanceView(performance).pipe(first()).subscribe((response: any) => {
      this.enableDisableSubmit=false;
      if (response.serviceStatus == "Success") {
        this.performnace1 = response.serviceResponse;

        console.log("given by hod",this.performnace1);


        this.currentStatus = this.performnace1[0].completionStatus;
        this.enableDisableSubmit = !this.enableDisableSubmit;
        this.filterRatingCriteria = this.performnace1.filter(item => item.deptId == this.selectedEmployee.departmentId && item.reviewFieldType === 'Rating' && item.empId == this.selectedEmployee.empId && item.quarterId == this.performnace.quarterId);
        this.filterCriteria = this.performnace1.filter(item => item.deptId == this.selectedEmployee.departmentId && item.reviewFieldType === 'Slider' && item.empId == this.selectedEmployee.empId && item.quarterId == this.performnace.quarterId);
        this.filterCriteria.forEach(value => {
          this.myList.push({ reviewLabel: value.reviewLabel, silde: value.ratingValue,performanceRatingId:value.performanceRatingId });
          this.finalRating=value.finalRating;
          this.hodRemarks = value.hodRemarks;
          this.hrReviewStatus=value.hrReviewStatus;
          this.acceptReason = value.hrRemark;
          this.rejectStatus =value.rejectStatus;
        });
        this.filterRatingCriteria.forEach(value => {
          this.myRateList.push({ reviewLabel: value.reviewLabel, rate: value.ratingValue,performanceRatingId:value.performanceRatingId });
          this.finalRating=value.finalRating;
          this.hodRemarks = value.hodRemarks;
          this.hrReviewStatus=value.hrReviewStatus;
           this.acceptReason = value.hrRemark;
           this.rejectStatus =value.rejectStatus;

        });


      } else {
        this.currentStatus = 'Not Started';
        this.enableDisableSubmit = false;
        this.filterCriteriaQuarter = this.allReviewType.filter(item => item.quarterId === this.quarterId);
        this.filterCriteria = this.filterCriteriaQuarter.filter(item => item.departmentName == this.selectedEmployee.departmentName && item.reviewFieldType === 'Slider');
        this.filterRatingCriteria = this.filterCriteriaQuarter.filter(item => item.departmentName == this.selectedEmployee.departmentName && item.reviewFieldType === 'Rating');
        this.filterCriteria.forEach(value => {
          this.myList.push({ reviewLabel: value.reviewLabel, silde: 0 ,performanceRatingId:null});
          this.finalRating = null;
          this.hodRemarks = null;
        });
        this.filterRatingCriteria.forEach(value => {
          this.myRateList.push({ reviewLabel: value.reviewLabel, rate: 0,performanceRatingId:null });
          this.finalRating = null;
          this.hodRemarks = null;
        });

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
      default:
        return '#A8A8A8';
    }
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
  submitRemarksByHR(quarter: any,template: TemplateRef<any>, index: any) {

    if (this.isAcceptSelected && !this.validationService.validateNullUndefinedEmptyString(this.acceptReason)) {
      this.alertMessage = "Please enter Comments!";
      this.openAlertMod(template, this.alertMessage);
      return;
    }

    if (this.isRejectSelected && !this.validationService.validateNullUndefinedEmptyString(this.rejectReason)) {
      this.alertMessage = "Please enter Reject Reason!";
      this.openAlertMod(template, this.alertMessage);
      return;
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
    this.submitRemarkHr.hrReviewStatus = this.isAcceptSelected ? "Accepted" : "Rejected";
    this.submitRemarkHr.hrRemark = this.isAcceptSelected ? this.acceptReason : this.rejectReason;
    this.submitRemarkHr.hrId = this.currentUser.empId;

    console.log("submithrrrrrr", this.submitRemarkHr);
    this.performanceService.submitEmployeePerformanceHR(this.submitRemarkHr).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        let collapseElement = document.getElementById('collapse' + index);
        if (collapseElement) {
          collapseElement.classList.remove('show'); // Remove 'show' class
        }

      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }







  updateReviewEmployee(quarter: any, template: TemplateRef<any>, index: any) {
    this.submitPerformance.empId = this.selectedEmployee.empId;
    this.submitPerformance.quarterId = quarter.quarterId;
    this.submitPerformance.hodId = this.currentUser.empId;
    this.submitPerformance.employeePerformanceId = null;
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
        this.openAlertMod(template, response.serviceResponse);
        let collapseElement = document.getElementById('collapse' + index);
        if (collapseElement) {
          collapseElement.classList.remove('show'); // Remove 'show' class
        }

      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });

  }

  getCountOfRewardsAndAppreciation(){
    this.appreciationCount='';
    this.rewardsCount='';
    console.log("this.projectDetails ", this.rewardsCount);
     this.appreciationAndRewardsCount.empId=this.selectedEmployee.empId;
    this.performanceSerive.getRewardsAndAppreciationCount(this.appreciationAndRewardsCount).pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus == "Success") {
            this.rewardsCount = response.serviceResponse[0].rewardsCount;
            this.appreciationCount = response.serviceResponse[0].appreciationCount;

            console.log("this.projectDetails ",  this.appreciationCount);

          }
        });
  }



static:any[] = [];
  getAllEmployeesCurrentStatus(){
    this.performanceService.getAllEmployeesCurrentStatus().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
         this.static = response.serviceResponse

      } else {
        console.error(response.serviceResponse);
      }
    });

    console.log("testing",this.static);
    console.log("testing2",this.eligibleEmployees);
  }
  openMultiGoalModal(employee: any) {
    console.log('Setting up to assign multiple goals to employee:', employee);
    this.selectedEmployee = employee;
    this.selectedGoalData = [];

    this.loadGoalTemplates();
    this.addNewGoalSelection();

    this.modalRef = this.modalService.open(this.multiGoalTemplate, {
      modalDialogClass: 'modal-lg',
    });
  }
    loadGoalTemplates() {
      console.log('Loading goal templates...');
      this.http.get(`${environment.baseUrl}api/goal-templates`).subscribe(
        (response: any) => {
          console.log('Goal templates response:', response);
          if (
            response &&
            response.serviceStatus &&
            response.serviceStatus.toUpperCase() === 'SUCCESS'
          ) {
            this.goalTemplates = response.serviceResponse || [];
            console.log('Processed goal templates:', this.goalTemplates);
          } else {
            console.error('Error loading goal templates:', response);
            this.goalTemplates = [];
          }
        },
        (error) => {
          console.error('Error fetching goal templates:', error);
          this.goalTemplates = [];
        }
      );
    }

    addNewGoalSelection() {
      this.selectedGoalData.push({
        templateId: null,
        expectedCompletionDate: '',
        quarterId: null,
      });
    }

    getEmployeePerformance(eligiemployee: any) {
      this.performanceService.setPreviousRoute(this.router.url);
      this.router.navigate(['/user-performance/view-performance', eligiemployee.empId]);
    }
}
