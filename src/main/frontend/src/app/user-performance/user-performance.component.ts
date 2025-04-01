
import { LocationStrategy } from '@angular/common';
import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import * as Highcharts from 'highcharts';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Performance } from 'src/app/models/performance';
import { Query } from 'src/app/models/query';
import { Employee } from '../models/employee';
import { Feature } from '../models/feature';
import { Log } from '../models/log';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { EmployeeService } from '../services/employee.service';
import { ExportExcelService } from '../services/export-excel.service';
import { LogService } from '../services/log.service';
import { PerformanceService } from '../services/performance.service';
import { UtilityService } from '../services/utility.service';
import { ValidationService } from '../services/validation.service';
import { SortPipe } from 'src/app/sort.pipe';


class FilterData {
  title: any;
  columns: any;
  queryList: any;
}


@Component({
  selector: 'app-user-performance',
  templateUrl: './user-performance.component.html',
  styleUrls: ['./user-performance.component.css']
})
export class UserPerformanceComponent implements OnInit {

  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;
  page = 1;
  filters: any = {};
  filterData: any = new FilterData();
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();
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
  allReviewType: any[] = [];
  allQauterCycle: any[] = [];
  eligibleEmployees: any[] = [];
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

  eligibleEmployeesColumns: any[] = ['employeementId', 'name', 'designationName', 'departmentName', 'billableType', 'totalExperience', 'employmentstatus', 'dateOfJoining'];
  finalRating: number;
  hodRemarks: any;
  quarterId: any;

  feature = "Performance";
  currentUser: User;
  userMapping: any = {};
  log: Log;
  allEmployeeList360: any[] = [];
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

  constructor(private logService: LogService,
    private employeeService: EmployeeService,
    private validationService: ValidationService,
    private utilityService: UtilityService,
    private authenticationService: AuthenticationService,
    private modalService: BsModalService,
    private locationStrategy: LocationStrategy,
    private performanceSerive: PerformanceService,
    private exportExcelService: ExportExcelService,
    private performanceService: PerformanceService
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
      await this.getALLdepartmentByEmployee();


      // Then call other methods
      this.getAllEmployee();
      this.getAllReviveType();
      this.getAllQauterCycle();

      // Continue with user mapping logic
      let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
      featureMap.subFeatures?.forEach(sub => {
        this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
      });

      this.isperformanceDsah = true;

      // console.log("hodddddd", this.userMapping.performance_action_by_hod);
      // console.log("hrrrrrrrr", this.userMapping.performance_action_by_hr);
      console.log('usermapping -- ', this.userMapping);
      this.getAllEmployeeFor360View();

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

  getAllEmployeeFor360View() {
    this.allEmployeeList360 = [];
    this.employeeService.getAllEmployeesFor360View().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allEmployeeList360 = response.serviceResponse;
        this.allEmployeeList360.forEach(employeeObj => {
          employeeObj.employeementId = this.utilityService.appendEmployeementid(employeeObj.isConsultant, employeeObj.employeementId);
          employeeObj.dateOfJoining = (employeeObj.dateOfJoining) ? moment(employeeObj.dateOfJoining).format(AppComponent.DATE_FORMAT) : null;
          employeeObj.dateOfRelieving = (employeeObj.dateOfRelieving) ? moment(employeeObj.dateOfRelieving).format(AppComponent.DATE_FORMAT) : null;
          employeeObj.updatedOn = (employeeObj.updatedOn) ? moment(employeeObj.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
          employeeObj.createdOn = (employeeObj.createdOn) ? moment(employeeObj.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          if (employeeObj.isConsultant == 'true')
            employeeObj.employeeType = 'Consultant';
          else if (employeeObj.isApprenticeship == 'true')
            employeeObj.employeeType = 'Apprentice';
          else
            employeeObj.employeeType = 'Regular';
        });
        this.allEmployeeList360 = this.allEmployeeList360;
        this.allEmployeeList360 = new SortPipe().transform(this.allEmployeeList360, ['name', 'string', 'asc']);
      } else {
        alert(response.serviceResponse);
      }
    });
  }




  RatingData: any[] = [];



  getAllEmployee() {

    this.performanceSerive.getAllEmployeesForPerformance(this.currentUser.empId).subscribe
      ((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allEmployee = response.serviceResponse;

          console.log("allEmp", this.allEmployee);

          const currentDate = new Date();
          const oneYearAgo = new Date(currentDate.getFullYear() - 1, 11, 31);

          // this.allEmployee = this.allEmployee.filter(employee => employee.empId !== this.currentUser.empId);
          this.eligibleEmployees = this.allEmployee.filter(employee => {
            // Append employee ID using utility service
            employee.employeementId = this.utilityService.appendEmployeementid(employee.isConsultant, employee.employeementId);

            const joiningDate = new Date(employee.dateOfJoining);

            // If the current user's role is HR or the HOD ID matches the current user
            if (employee.hodId === this.currentUser.empId) {
              // Check if the employee is confirmed and joined more than one year ago
              return joiningDate <= oneYearAgo && employee.employmentstatus === 'Confirmed';
            }else{

              if(employee.managerId!=null){
                if(employee.managerId===this.currentUser.managerId){
                  return joiningDate <= oneYearAgo && employee.employmentstatus === 'Confirmed';
          
                }
              }
             
            }
            if (this.userMapping.performance_action_by_hr === true) {
              // Check if the employee is confirmed and joined more than one year ago
              return joiningDate <= oneYearAgo && employee.employmentstatus === 'Confirmed';
            }


            // if (this.currentUser.employeeRole !== 'HR') {
            //   alert('You are not authorized..!!');
            // }


            return false;
          });
          this.eligibleEmployees.forEach(eligibleEmp => {
            let matchingEmployee = this.allEmployeeList360.find(emp => emp.employeementId === eligibleEmp.employeementId);

            eligibleEmp.emp360 = matchingEmployee ? matchingEmployee : {};

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
    this.modalRef.hide();
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
      const response: any = await this.performanceSerive.getALLdepartmentByEmployee().toPromise();

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
    this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
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

    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

  name = 'EmployeeSheet.xlsx';
  exportToExcel(): void {
    this.performanceService.getAllEmployeesForPerformance(this.currentUser.empId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        //  this.employeeDataForExcel = response.serviceResponse;

        this.allEmployee = response.serviceResponse;

        console.log("allEmp", this.allEmployee);

        const currentDate = new Date();
        const oneYearAgo = new Date();
        oneYearAgo.setFullYear(currentDate.getFullYear() - 1);  // Get the date one year ago

        this.eligibleEmployees = this.allEmployee.filter(employee => {

          const joiningDate = new Date(employee.dateOfJoining);
          return joiningDate <= oneYearAgo && employee.employmentstatus === 'Confirmed';
        });
        this.employeeDataForExcel = this.eligibleEmployees;

      }
      const onlySpecificDataArr = this.employeeDataForExcel.map(
        x => ({
          // "EmployeeId":"A-".concat(x.employeementId),
          "EmployeeId": (x.isConsultant === 'true' ? 'A-CS-' : 'A-') + x.employeementId,
          "Full Name": x.name,
          "EmailId": x.email,
          "Employment Status": x.employmentstatus,
          "Date of Joining": (x.dateOfJoining) ? moment(x.dateOfJoining).format(AppComponent.DATE_FORMAT) : null,
          "Date of Relieving": (x.dateOfRelieving) ? moment(x.dateOfRelieving).format(AppComponent.DATE_FORMAT) : null,
          "Department Name": x.departmentName,
          "Aadhar": x.aadhar,
          "About Me": x.aboutMe,
          "Address": x.address,
          "Permanent Address": x.permanentAddress,
          "City": x.city,
          "Blood Group": x.bloodGroup,
          "Date Of Birth": (x.dateOfBirth) ? moment(x.dateOfBirth).format(AppComponent.DATE_FORMAT) : null,
          "Gender": x.gender,
          "Father Name": x.fatherName,
          "Mobile No": x.mobileNo,
          "Pan Number": x.panNumber,
          "Place Of Birth": x.placeOfBirth,
          "Work Location": x.workLocation,
          "Probation Period": x.probationPeriod,
          "Notice Period": x.noticePeriod,
          "Country": x.country,
          "Emergency Contact Mobile": x.emergencyContactMobile,
          "Emergency Contact Person": x.emergencyContactPerson,
          "Landline": x.landline,
          "Marital Status": x.maritalStatus,
          "Mother Tongue": x.motherTongue,
          "Alternate Mobile No": x.alternateMobileNo,
          "Pincode": x.pincode,
          "Relation": x.relation,
          "State": x.state,
          "Views On Organisation": x.viewsOnOrganisation,
          "Passport Number": x.passportNumber,
          "Bank Account No": x.bankAccountNo,
          "Bank IFSC Code": x.bankIFSCCode,
          "Bank Name": x.bankName,
          "PF Account Number": x.pfAccountNumber,
          "Previous PF AccountNumber": x.previousPfAccountNumber,
          "UAN": x.uan,
          "ESIC Number": x.esicNumber,
          "Graduation Type": x.graduationType,
          "Pursuing": x.pursuing,
          "Passing Grade": x.passingGrade,
          "Year Of Passing": x.yearOfPassing,
          "Created By": x.createdBy,
          "Created On": (x.createdOn) ? moment(x.createdOn).format(AppComponent.DATETIME_FORMAT) : null,
          "Manager Name": x.managerName,
          "Job Role": x.jobRoleName,
          "Designation Name": x.designationName

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
          });
          console.log("allEmployeeList : ", this.eligibleEmployees)
        } else {
          this.openAlertMod(template, response.serviceResponse)
        }
      });
    }
  }


  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
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



  }

  back() {
    this.getAllEmployee();
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

  currentStatus: any;
  HrAndHodView(performance: any) {
    this.performanceSerive.hrAndHodEmpoyeePerformanceView(performance).pipe(first()).subscribe((response: any) => {
      this.enableDisableSubmit = false;
      if (response.serviceStatus == "Success") {
        this.performnace1 = response.serviceResponse;

        console.log("given by hod", this.performnace1);


        this.currentStatus = this.performnace1[0].completionStatus;
        this.enableDisableSubmit = !this.enableDisableSubmit;
        this.filterRatingCriteria = this.performnace1.filter(item => item.deptId == this.selectedEmployee.departmentId && item.reviewFieldType === 'Rating' && item.empId == this.selectedEmployee.empId && item.quarterId == this.performnace.quarterId);
        this.filterCriteria = this.performnace1.filter(item => item.deptId == this.selectedEmployee.departmentId && item.reviewFieldType === 'Slider' && item.empId == this.selectedEmployee.empId && item.quarterId == this.performnace.quarterId);
        this.filterCriteria.forEach(value => {
          this.myList.push({ reviewLabel: value.reviewLabel, silde: value.ratingValue, performanceRatingId: value.performanceRatingId });
          this.finalRating = value.finalRating;
          this.hodRemarks = value.hodRemarks;

        });
        this.filterRatingCriteria.forEach(value => {
          this.myRateList.push({ reviewLabel: value.reviewLabel, rate: value.ratingValue, performanceRatingId: value.performanceRatingId });
          this.finalRating = value.finalRating;
          this.hodRemarks = value.hodRemarks;

        });


      } else {
        this.currentStatus = 'Not Started';
        this.enableDisableSubmit = false;
        this.filterCriteriaQuarter = this.allReviewType.filter(item => item.quarterId === this.quarterId);
        this.filterCriteria = this.filterCriteriaQuarter.filter(item => item.departmentName == this.selectedEmployee.departmentName && item.reviewFieldType === 'Slider');
        this.filterRatingCriteria = this.filterCriteriaQuarter.filter(item => item.departmentName == this.selectedEmployee.departmentName && item.reviewFieldType === 'Rating');
        this.filterCriteria.forEach(value => {
          this.myList.push({ reviewLabel: value.reviewLabel, silde: 0, performanceRatingId: null });
          this.finalRating = null;
          this.hodRemarks = null;
        });
        this.filterRatingCriteria.forEach(value => {
          this.myRateList.push({ reviewLabel: value.reviewLabel, rate: 0, performanceRatingId: null });
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
  submitRemarksByHR(template: TemplateRef<any>, index: any) {
     
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


}
