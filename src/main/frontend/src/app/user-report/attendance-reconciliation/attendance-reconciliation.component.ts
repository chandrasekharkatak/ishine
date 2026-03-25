import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import * as moment from 'moment';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { User } from 'src/app/models/user';
import { AttendanceReconciliationService } from 'src/app/services/attendance-reconciliation.service';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { DatePipe } from '@angular/common';
import { Query } from 'src/app/models/query';
import { LeaveService } from 'src/app/services/leave.service';
import { UtilityService } from 'src/app/services/utility.service';
import { AppComponent } from 'src/app/app.component';
import { first } from 'rxjs/operators';
import { Biomax } from 'src/app/models/biomax';
import * as Highcharts from 'highcharts';
import { EmployeeService } from 'src/app/services/employee.service';
import { SortPipe } from 'src/app/sort.pipe';
import { Feature } from 'src/app/models/feature';


class FilterData {
  title: any;
  columns: any;
  queryList: any;
}
@Component({
  standalone: false,
  selector: 'app-attendance-reconciliation',
  templateUrl: './attendance-reconciliation.component.html',
  styleUrls: ['./attendance-reconciliation.component.css']
})

export class AttendanceReconciliationComponent implements OnInit {

  @ViewChild("alert_message")
  alertModal: TemplateRef<any>;

  leaveReportColumns: any;
  filterData: any = new FilterData();
  alertMessage: any;
  modalRef:NgbModalRef;
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;
  isSearchEnabled: boolean = false;
  isLeaveReportTable: boolean = false;
  punchData: any;
  currentUser: User;
  attendanceReconciliationList: any[] = [];
  attendanceReconciliationOriginaldata: any[] = [];
  filteredData: any[] = [];
  viewMoreList: any[] = [];
  queryList: any[] = [];
  storedDataList: any[] = [];
  excelName: any;
  date: string;
  startDate: string;
  endDate: string;
  name: string;
  page = 1;
  totalRecords: number = 0;
  pageSize: number = 20;
  formattedDate: string;
  startformattedDate: string;
  endformattedDate: string;
  maxTodayDate: any;
  AttendancereConciliation: any[] = ['employeeCode', 'employeeName', 'inTime', 'outTime', 'totalDuration', 'shiftDuration', 'logDate', 'departmentName', 'reportingManagerName'];
  timesheetColumns: any[] = ['Employee Id', 'Full Name', 'Employment Status', 'Department', 'Date', 'Day Type', 'Status', 'Total Working Hour', 'Team Name', 'Project Name', 'Client Name', 'From Date', 'To Date', 'Created On', 'Updated On', 'Updated By', 'Leave Type'];

  filters: any = {};
  fromDate: string = '';
  toDate: string = '';
  currentDate: string;


  feature = 'Reports';
  userMapping: any = {};

  constructor(
    private modalService: NgbModal,
    private exportExcelService: ExportExcelService,
    private attendanceReconciliationService: AttendanceReconciliationService,
    private authenticationService: AuthenticationService,
    private datePipe: DatePipe,
    private leaveService: LeaveService,
    private utilityService: UtilityService,
    private employeeService: EmployeeService
  ) {
    this.maxTodayDate = new Date().toISOString().split('T')[0];
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x)
    const today = new Date();
    this.currentDate = today.toISOString().split('T')[0];
    this.startDate = this.currentDate;
    this.endDate = this.endDate;
    // this.formattedDate = this.formatDate(this.date);
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return this.datePipe.transform(date, 'dd-MMM-yyyy')!;
  }

  ngOnInit(): void {
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
        featureMap.subFeatures?.forEach(sub => {
          this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
        });
    this.date = moment().format("YYYY-MM-DD");
    this.startDate = moment().format("YYYY-MM-DD");
    this.endDate = moment().format("YYYY-MM-DD");

    this.formattedDate = this.formatDate(this.date);
    this.startformattedDate = this.formatDate(this.startDate);
    this.endformattedDate = this.formatDate(this.endDate);;
    //  this.date=this.formattedDate;
    // console.log("ckeck date =======", this.startDate);
    // console.log("ckeck date =======", this.endDate);
    this.getBioMatricData(this.startformattedDate, this.endformattedDate);

  }

  onSearch(searchData) {
    this.filters = searchData;
  }

  //pagination
  handlePageChange(event) {
    this.page = event;
    this.getBioMatricData(this.startDate, this.endDate);
  }

  sortData(sort: Sort) {
    //console.log(sort);
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

  getBioMatricData(startDate: string, endDate: string) {
    const startdateformat = this.formatDate(startDate);
    const enddateformat = this.formatDate(endDate);

    this.attendanceReconciliationService.getBiomatricData(startdateformat, enddateformat, this.page, this.pageSize).subscribe((response: any) => {
      this.attendanceReconciliationList = response.serviceResponse.data;
      this.totalRecords = response.serviceResponse.totalRecords;
      this.attendanceReconciliationList.forEach(employee => {

        employee.emp360 = employee.empId;

        employee.employeementId = String(employee.employeeCode);
        if (employee.employeementId.startsWith('A'))
          employee.employeementId = employee.employeementId.substring(1);
        employee.employeementId = "A-".concat(employee.employeementId);
      });

      // console.log("this.attendanceReconciliationList" , this.attendanceReconciliationList);
      this.attendanceReconciliationOriginaldata = [... this.attendanceReconciliationList];
      this.modalRef?.close();
    });
  }

  getviewMoreData(template: TemplateRef<any>, punchrecords: any, name: any) {

    this.name = name;
    this.viewMoreList.push(punchrecords);
    const recordsString = this.viewMoreList[0];
    const recordsArray = recordsString.split(',').filter(record => record);
    this.punchData = [];

    for (let i = 0; i < recordsArray.length; i += 2) {
      if (i + 1 < recordsArray.length) {
        this.punchData.push({
          in: recordsArray[i],
          out: recordsArray[i + 1]
        });
      }
    }

    // console.log("Parsed Punch Data: ", this.punchData);
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
  }

  // openFilterModal(template: TemplateRef<any>, columns: any[], title: any) {
  //   this.filterData.title = title;
  //   this.modalRef = this.modalService.open(template, { modalDialogClass: '' });

  // }

  openFilterModal(template: TemplateRef<any>, columns: any[], title: any) {
    //console.log("columns : ", columns);
    this.queryList = [];

    this.filterData.title = title;
    this.filterData.columns = columns;
    const startdateformat = this.formatDate(this.startDate);
    const enddateformat = this.formatDate(this.endDate);

    this.queryList = [
      { column: "Employment Status", operator: "!=", value: "InActive", conjunction: "",startDate : startdateformat,endDate : enddateformat},
    ];

    this.storedDataList.forEach((data) => {
      if (data.filterName == title) {
        data.queryList.forEach((queryObj) => {
          if (queryObj.column == "Employee Id" && !queryObj.value.includes("A-")) {
            queryObj.value = "A-".concat(queryObj.value);
          }
          if (queryObj.column == "Employee Id" && !queryObj.value.includes("A-CS-") && (data.isConsultant == 'true')) {
            queryObj.value = "A-CS-".concat(queryObj.value);
          }
          if (queryObj.column == "Employee Id" && !queryObj.value.includes("AP-") && (data.IsApprenticeship == 'true')) {
            queryObj.value = "AP-".concat(queryObj.value);
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

  exportToExcel(): void {
    this.excelName = 'attendanceReconciliation.xlsx';

    const convertMinutesToHours = (value: number): string => {
      if (value === null || value === undefined) {
        return 'NA'; // Handle null or undefined value
      }
      const hours = Math.floor(value / 60);
      const mins = value % 60;
      const convertedValue = `${hours} hour${hours !== 1 ? 's' : ''} ${mins} min${mins !== 1 ? 's' : ''}`;
      // console.log(`Converted value for ${value} minutes: ${convertedValue}`);  // Debugging log
      return convertedValue;
    };

    const onlySpecificDataArr = this.attendanceReconciliationList.map(
      x => ({
        "Employee Id": x.employeeCode,
        "Employee Name": x.employeeName,
        "Log IN": x.inTime,
        "Log Out": x.outTime,
        "Total Working Hours": convertMinutesToHours(x.totalDuration),
        "Shift Duration": convertMinutesToHours(x.shiftDuration),
        "Shift Name": x.shiftName,
        "Begin Time": x.beginTime,
        "endTime": x.endTime,
        "Log Date": x.logDate,
        "Early By": convertMinutesToHours(x.earlyBy),
        "Late By": convertMinutesToHours(x.lateBy),
        "Status": x.status
      })
    );

    this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName);
  }

  exportToExcelviewMore(): void {
    this.excelName = 'ViewMoreData.xlsx';
    const onlySpecificDataArr = this.punchData.map(
      x => ({
        "IN": x.in,
        "Employee Name": this.name,
        "OUT": x.out,
      })
    )
    this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
  }

  searchRecords() {
    console.log('Searching records from:', this.startDate, 'to:', this.endDate);
    if (this.startDate && this.endDate) {
      this.page = 1;
      this.getBioMatricData(this.startDate, this.endDate);
    } else {
      console.error('Start date or end date is missing');
      // Optionally, show an alert or error message to the user
    }
  }

  clickFilter:boolean=false;
  onFilterSubmit(emittedArray: any, template: TemplateRef<any>) {
    this.clickFilter = true;
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

      if (this.filterData.title == 'Filter Attendance Reconciliation') {
        this.getCustomLAttendanceApplicationsList(emittedArray[0], template);
      }
      // if (this.filterData.title == 'Filter Employee Report') {
      //   this.getCustomEmployeesList(emittedArray[0], template);
      // }
      // if (this.filterData.title == 'Filter Timesheet Report') {
      //   this.getCustomTimesheetApplicationsList(emittedArray[0], template);
      // }
    } else {
      let clearedFilter = this.storedDataList.find((filter) => filter.filterName == emittedArray[1]);
      this.storedDataList.splice(clearedFilter);

      if (emittedArray[1] == 'Filter Attendance Reconciliation') {
        // this.showLeaveReportTable();
      }
      // if (emittedArray[1] == 'Filter Timesheet Report') {
      //   this.showTimesheetReportTable();
      // }
      // if (emittedArray[1] == 'Filter Employee Report') {
      //   this.showEmployeeReportTable();
      // }
    }
  }

  allAttendanceRepostList: any[] = [];

  getCustomLAttendanceApplicationsList(queryObjList: any, template: TemplateRef<any>) {
    // this.allLeaveApplicationsList = [];

    let queryObj = new Query();
    queryObj.queryList = queryObjList;
    if (queryObjList.length == 0) {
      // this.getAllLeaveApplicationsList();
    } else {
      this.leaveService.getCustomLAttendanceApplicationsList(queryObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allAttendanceRepostList = response.serviceResponse;
          console.log("this.allAttendanceRepostList" , this.allAttendanceRepostList);

          // this.allLeaveApplicationsList = this.allLeaveApplicationsList.filter((value, index, self) =>
          //   index === self.findIndex((t) => (
          //     t.employeementId === value.employeementId && t.fromDate === value.fromDate
          //   ))
          // )

          if (this.allAttendanceRepostList.length == 0) {
            this.openAlertMod(this.alertModal, "No Leave Application Report found ")
          }
          this.allAttendanceRepostList.forEach(leave => {
            // leave.employeementId = "A-".concat(leave.employeementId);
            // leave.employeementId = (leave.isConsultant === 'true' ? "A-CS-" : "A-").concat(leave.employeementId);
            leave.employeementId = this.utilityService.getFormattedEmployeeId(leave);
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
          //console.log("allLeaveApplicationsList : ", this.allLeaveApplicationsList)
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


  // biomaxList:Biomax[]=[];
  // chartdata={
  //   workinghours:0,
  //   lessthenworkinghours:0,
  //   hovertime:0
  // }

  // getBiomatrixFilter() {

  //   let workinghours2 = 0;
  //   let lessthenworkinghours = 0;
  //   let hovertime = 0;

  //   const startdateformat = this.formatDate(this.startDate);
  //   const enddateformat = this.formatDate(this.endDate);

  //   this.attendanceReconciliationService.getBiomatricData(startdateformat, enddateformat).subscribe((response: any) => {
  //       this.biomaxList = response.serviceResponse;
  //       console.log("Check ============>" , this.biomaxList);

  //       this.biomaxList.forEach((filter5) => {
  //         let workinghours: number = parseInt(filter5.totalDuration);
  //         if (workinghours !== 0) {
  //           workinghours2 += 9; // Baseline working hours
  //           if (workinghours > 9) {
  //             hovertime += (workinghours - 9); // Overtime calculation
  //           }
  //           if (workinghours < 9) {
  //             lessthenworkinghours += workinghours; // Less than working hours calculation
  //           }
  //         }
  //       });


  //       if (this.biomaxList.length > 0) {
  //         // Set the chart data
  //         this.chartdata.hovertime = hovertime;
  //         this.chartdata.workinghours = workinghours2;
  //         this.chartdata.lessthenworkinghours = lessthenworkinghours;
  //         // Update the chart with new data
  //         this.updateChartData(this.chartdata);
  //       }
  //     });
  // }

  // private updateChartData(data: any): void {
  //   this.chartOptions = {
  //     chart: {
  //       type: 'pie'
  //     },
  //     title: {
  //       text: 'Work Hours Distribution'
  //     },
  //     credits: {
  //       enabled: false
  //     },
  //     colors: ['#FF5733', '#33FF57', '#3357FF'],
  //     series: [
  //       {
  //         type: 'pie',
  //         name: 'Work Hours',
  //         data: [
  //           { name: 'Working Hours', y: data.workinghours },
  //           { name: 'Less Than Working Hours', y: data.lessthenworkinghours },
  //           { name: 'Overtime', y: data.hovertime }
  //         ]
  //       }
  //     ]
  //   };

  //   // Update chart with new options
  //   Highcharts.chart('biomaxfiterContainer', this.chartOptions);
  // }

  biomaxList: Biomax[] = [];
chartdata = {
  completed9Hours: 0,
  above9Hours: 0,
  lessThan9Hours: 0
};

getBiomatrixFilter() {
  let completed9HoursCount = 0;
  let above9HoursCount = 0;
  let lessThan9HoursCount = 0;

  const startdateformat = this.formatDate(this.startDate);
  const enddateformat = this.formatDate(this.endDate);

  this.attendanceReconciliationService.getBiomatricData(startdateformat, enddateformat).subscribe((response: any) => {
    this.biomaxList = response.serviceResponse;
    console.log("Fetched Data ============>", this.biomaxList);

    // Iterate through all records and classify by work hours
    this.biomaxList.forEach((entry) => {
      const workingHours = parseInt(entry.totalDuration, 9);

      if (workingHours === 9) {
        completed9HoursCount++; // Count employees with exactly 9 hours
      } else if (workingHours > 9) {
        above9HoursCount++; // Count employees with more than 9 hours
      } else if (workingHours < 9) {
        lessThan9HoursCount++; // Count employees with less than 9 hours
      }
    });

    if (this.biomaxList.length > 0) {
      // Set the chart data with counts
      this.chartdata.completed9Hours = completed9HoursCount;
      this.chartdata.above9Hours = above9HoursCount;
      this.chartdata.lessThan9Hours = lessThan9HoursCount;

      // Update the chart with new data
      this.updateChartData(this.chartdata);
    }
  });
}

private updateChartData(data: any): void {
  this.chartOptions = {
    chart: {
      type: 'pie'
    },
    title: {
      text: 'Work Hours Distribution (All Employees)'
    },
    credits: {
      enabled: false
    },
    colors: ['#33FF57','#FF5733', '#3357FF'],
    series: [
      {
        type: 'pie',
        name: 'Employee Count',
        data: [
          { name: 'Completed 9 Hours', y: data.completed9Hours },
          { name: 'Above 9 Hours', y: data.above9Hours },
          { name: 'Less Than 9 Hours', y: data.lessThan9Hours }
        ]
      }
    ]
  };

  // Update chart with new options
  Highcharts.chart('biomaxfiterContainer', this.chartOptions);
}



  chartOptions: Highcharts.Options = {
    chart: {
      type: 'pie'
    },
    title: {
      text: 'Work Hours Distribution'
    },
    series: [
      {
        type: 'pie',
        name: 'Work Hours',
        data: []
      }
    ]
  };

  isAttendanceVisible = false; // Default: hide the attendance dashboard

  // Toggles the visibility of the attendance dashboard
  toggleAttendanceDashboard(event: any): void {
    this.isAttendanceVisible = event.target.checked;

    if (this.isAttendanceVisible) {
      this.getBiomatrixFilter();
    } else {
      // Optionally clear the chart when hidden
      Highcharts.chart('biomaxfiterContainer', {});
    }
  }


}
