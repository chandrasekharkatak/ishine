import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import * as HighCharts from 'highcharts';
import HC_exportData from "highcharts/modules/export-data";
import { first, groupBy } from 'rxjs/operators';
import { EmployeeService } from 'src/app/services/employee.service';
import { LeaveService } from 'src/app/services/leave.service';
import { TimesheetService } from 'src/app/services/timesheet.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { Query } from 'src/app/models/query';
import * as moment from 'moment';
import { Leave } from 'src/app/models/leave';
HC_exportData(HighCharts);

class FilterData{
  title:any;
  columns:any;
  queryList:any;
}

@Component({
  selector: 'app-report-dashboard',
  templateUrl: './report-dashboard.component.html',
  styleUrls: ['./report-dashboard.component.css']
})
export class ReportDashboardComponent implements OnInit {

  @ViewChild("leave_summary_template")
  leaveSummaryTemplate: TemplateRef<any>;

  @ViewChild("timesheet_summary_template")
  timesheetSummaryTemplate: TemplateRef<any>;

  @ViewChild("employee_summary_template")
  employeeSummaryTemplate: TemplateRef<any>;

  modalRef: BsModalRef = new BsModalRef();

  isleaveTimesheetDashboard:boolean = false;
  isEmployeeDashboard:boolean = false;

  data:any;
  leaveSumarryList:any[] = [];
  uniqueLeaveSumarryList:any[] = [];
  timsheetSummaryList:any[] = [];
  allEmployeeList: any[] = [];
  leaveTrendAnalysisList:any[] = [];
  allLeaveTypes:any[] = [];

  modalTitle:any;
  modalSummaryList:any[] = [];

  zeroToFive:any;
  fiveToEight:any;
  eightToNine:any;
  nineToTen:any;
  tenAndAbove:any;
  noEODSubmitted:any;
  holiday:any;
  workingOnHoliday:any;

  dateFormat:any = 'YYYY-MM-DD';

  alertMessage:any;

  countOfAllEmployees:any;
  employeeInProbationAfter6MonthsCount = 0;

  filterData:any = new FilterData();
  queryList:any[] = [];
  leaveSummaryColumns:any[] = ['Employee Id', 'Full Name', 'Leave Type','Department','Team Name','Project Name','Client Name', 'From Date', 'To Date', 'No. of Days', 'Reason', 'Status', 'Manager Name', 'Created On', 'Updated On', 'Updated By'];
  timesheetSummaryColumns:any[] = ['Employee Id','Full Name','Date','Day Type','Status','Total Working Hour','Team Name','Project Name','Client Name','From Date','To Date','Created On','Updated On','Updated By'];
  employeeColumns:any[] = ['Employee Id', 'Full Name', 'Department', 'Job Role', 'Manager', 'Employment Status', 'Date Of Joining', 'City', 'Blood Group', 'Gender', 'Work Location', 'Probation Period', 'Notice Period', 'Marital Status', 'Bank Name', 'Created By', 'State', 'Created On', 'Experience'];

  constructor(
    private leaveService : LeaveService,
    private timesheetService : TimesheetService,
    private modalService: BsModalService,
    private employeeService: EmployeeService,
    private exportExcelService: ExportExcelService
  ) { }

  ngOnInit(): void {
    this.sectionViewInit();
  }

  sectionViewInit(){
    this.leaveTimesheetDashboard();
    this.getAllLeaveTypes();
  }

  leaveTimesheetDashboard(){
    this.isleaveTimesheetDashboard = true;
    this.isEmployeeDashboard = false;

    this.get8DaysLeaveReport();
    this.get9DayTimesheetReport();
  }

  employeeDashboard(){
    this.isEmployeeDashboard = true;
    this.isleaveTimesheetDashboard = false;

    this.getAllEmployeeList();
    this.getLeaveTrendAnalysisReport();
  }

  get8DaysLeaveReport(){
    this.leaveSumarryList = [];
    this.queryList=[];
    let leaveObj= new Leave();

     leaveObj.startDate = moment().subtract(8, 'd').format(this.dateFormat);
     leaveObj.endDate = moment().format(this.dateFormat);

    this.leaveService.getLast8DaysLeaveReport(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveSumarryList = response.serviceResponse;
        console.log("leaveSumarryList : ", this.leaveSumarryList);
        this.extractLeaveReportData();
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  extractLeaveReportData(){
    let pendingCount = 0;
    let approvedCount = 0;
    let rejectedCount = 0;

    this.uniqueLeaveSumarryList = this.leaveSumarryList.filter((value, index, self) =>
      index === self.findIndex((t) => (
        t.employeementId === value.employeementId && t.fromDate === value.fromDate
      ))
    )
    console.log(this.uniqueLeaveSumarryList, " uniqueIds ");

    this.uniqueLeaveSumarryList.forEach(leaveStatus => {
      if (leaveStatus.status == "Pending") pendingCount++;
      else if (leaveStatus.status == "Approved") approvedCount++;
      else if (leaveStatus.status == "Rejected") rejectedCount++;
    });

    let leaveStatusData = [{
      name: "Pending",
      y: pendingCount
    },
    {
      name: "Approved",
      y: approvedCount
    },
    {
      name: "Rejected",
      y: rejectedCount
    }];

    console.log("leaveStatusData : ", leaveStatusData);
    this.renderPieSummaryChart('Leave Summary Chart', 'leaveSummaryChart', leaveStatusData, 'Leaves', this.openLeaveSummaryTableModel.bind(this));
        
  }

  getCustomLeaveReport(queryObjList:any , template:TemplateRef<any>) {
    this.leaveSumarryList = [];

    let queryObj = new Query();
    queryObj.queryList = queryObjList;
    if(queryObjList == ''){
      this.get8DaysLeaveReport();
    }else {
      this.leaveService.customQueryForLeaveReport(queryObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.leaveSumarryList = response.serviceResponse;
          this.extractLeaveReportData();
        } else {
          this.openAlertMod(template,response.serviceResponse);
        }
      });  
    }
  }

  getDatesInRange(startDate, endDate) {
    const date = startDate;
    const dates = [];

    while (date <= endDate) {
      dates.push(new Date(date));
      date.setDate(date.getDate() + 1);
    }
    return dates;
  }

  getAllLeaveTypes() {
    this.leaveService.getAllLeaveTypes().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allLeaveTypes = response.serviceResponse;
        console.log("leaveTypes : ", this.allLeaveTypes);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  getLeaveTrendAnalysisReport() {
    this.leaveSumarryList = [];
    let leaveObj = new Leave();

    leaveObj.startDate = moment().subtract(8, 'd').format(this.dateFormat);
    leaveObj.endDate = moment().format(this.dateFormat);

    this.leaveService.getLeaveTrendAnalysisReport(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveTrendAnalysisList = response.serviceResponse;
        this.extractLeaveTrendAnalysisData(leaveObj);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  extractLeaveTrendAnalysisData(leaveObject:any){
    this.leaveSumarryList = [];

    const d1 = new Date(leaveObject.startDate);
    const d2 = new Date(leaveObject.endDate);

    let dateRange = this.getDatesInRange(d1, d2);
    let formattedDateRange = [];

    dateRange.forEach((date) => {
      formattedDateRange.push(date.toISOString().split('T')[0]);
    })

    this.leaveTrendAnalysisList = this.leaveTrendAnalysisList.filter((value, index, self) =>
      index === self.findIndex((t) => (
        t.employeementId === value.employeementId && t.fromDate === value.fromDate
      ))
    )
    console.log(this.leaveTrendAnalysisList, " uniqueTrendAnalysisList ");

        let chartData = [];
        let leaveTypes = [];

        let leaveObj = this.allLeaveTypes.forEach(obj => {
          leaveTypes.push(obj.leaveType);
        });
        
        let data = leaveTypes.map(leaveType => {
          chartData.push({ type: 'line', name: leaveType, data: [] });
          let leaveData = this.leaveTrendAnalysisList.filter(leaveTrendAnalysis => leaveTrendAnalysis.leaveType == leaveType)
          return leaveData;
        });

        console.log("data :", data);
        console.log("ChartData : ", chartData);
        console.log("dateRange : ", dateRange);

        data.forEach(leaveDataArr => {
          dateRange.forEach(date => {
            let leaveType: any;
            let dataByDate = leaveDataArr.filter(leaveApplication => {
              leaveType = leaveApplication.leaveType;
              if (leaveApplication.fromDate == moment(date).format(this.dateFormat)) return leaveApplication;
            });
            chartData.find(chartDataObj => chartDataObj.name == leaveType)?.data.push(dataByDate.length)
          });
        });
        console.log("Final ChartData : ", chartData);
        this.renderLineGraphChart('Leave Trend Analysis Graph', 'leaveTrendAnalysis', chartData, 'Leave Trend', formattedDateRange, this.openLeaveAnalysisTableModel.bind(this));
  }

  getCustomLeaveTrendAnalysisReport(queryObjList:any , template:TemplateRef<any>) {
    this.leaveTrendAnalysisList = [];

    let queryObj = new Query();
    queryObj.queryList = queryObjList;
    if(queryObjList == ''){
      this.getLeaveTrendAnalysisReport();
    }else {
      let tempFrom = "";
      let tempTo = "";
      queryObj.queryList.forEach((query)=> {
        if(query.column == 'From Date')
        {
          tempFrom = query.value;
        }else{
          tempFrom = moment().subtract(8, 'd').format(this.dateFormat);
        }
        if(query.column == 'To Date')
        {
          tempTo = query.value;
        }else{
          tempTo = moment().format(this.dateFormat);
        }
      });

      this.leaveService.customQueryForLeaveTrendAnalysisReport(queryObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.leaveTrendAnalysisList = response.serviceResponse;
          let leaveObj = new Leave();
          leaveObj.startDate = tempFrom;
          leaveObj.endDate = tempTo;
          console.log()
          this.extractLeaveTrendAnalysisData(leaveObj);
        } else {
          this.openAlertMod(template,response.serviceResponse);
        }
      });  
    }
  }

  get9DayTimesheetReport(){
    this.timsheetSummaryList = [];

    this.timesheetService.getLast9DaysTimesheetReport().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.timsheetSummaryList = response.serviceResponse;
        console.log("timsheetSummaryList : ", this.timsheetSummaryList);
        this.extractTimesheetReportData();
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  extractTimesheetReportData(){

    let pendingByUserCount = 0;
        let pendingCount = 0;
        let approvedCount = 0;
        let rejectedCount = 0;

        let zeroToFiveCount = 0;
        let fiveToEightCount = 0;
        let eightToNineCount = 0;
        let nineToTenCount = 0;
        let tenAndAboveCount = 0;
        let noEODSubmittedCount = 0;
        let holidayCount = 0;
        let workingOnHolidayCount = 0;

        let totalListCount = 0;

        this.timsheetSummaryList = this.timsheetSummaryList.filter((value, index, self) =>
          index === self.findIndex((t) => (
            t.employeementId === value.employeementId && t.date === value.date
          ))
        )
        
        this.timsheetSummaryList.forEach(timesheet => {

          if (timesheet.legend == "Pending") pendingCount++;
          else if (timesheet.legend == "Approved") approvedCount++;
          else if (timesheet.legend == "Rejected") rejectedCount++;
          // else if (timesheet.legend == "Pending By User") pendingByUserCount++;

          if(timesheet.dayType == 'Working' && timesheet.totalWorkingHours > 0 && timesheet.totalWorkingHours <= 5) zeroToFiveCount++;
          else if(timesheet.dayType == 'Working' && timesheet.totalWorkingHours > 5 && timesheet.totalWorkingHours <= 8) fiveToEightCount++;
          else if(timesheet.dayType == 'Working' && timesheet.totalWorkingHours > 8 && timesheet.totalWorkingHours <= 9) eightToNineCount++;
          else if(timesheet.dayType == 'Working' && timesheet.totalWorkingHours > 9 && timesheet.totalWorkingHours <= 10) nineToTenCount++;
          else if(timesheet.dayType == 'Working' && timesheet.totalWorkingHours > 10) tenAndAboveCount++;
          else if(timesheet.dayType == 'Holiday') holidayCount++;
          else if(timesheet.dayType == 'Non-working') workingOnHolidayCount++;

          if(timesheet.legend == "Pending By User"){
            totalListCount = totalListCount + timesheet.pendingEodCount;
            pendingByUserCount = pendingByUserCount + timesheet.pendingEodCount;
          }else{
            totalListCount++;
          }
        });

        this.zeroToFive = ((zeroToFiveCount / totalListCount) * 100).toFixed(2) + "%";
        this.fiveToEight = ((fiveToEightCount / totalListCount) * 100).toFixed(2) + "%";
        this.eightToNine = ((eightToNineCount / totalListCount) * 100).toFixed(2) + "%";
        this.nineToTen = ((nineToTenCount / totalListCount) * 100).toFixed(2) + "%";
        this.tenAndAbove = ((tenAndAboveCount / totalListCount) * 100).toFixed(2) + "%";
        this.noEODSubmitted = ((pendingByUserCount / totalListCount) * 100).toFixed(2) + "%";
        this.holiday = ((holidayCount / totalListCount) * 100).toFixed(2) + "%";
        this.workingOnHoliday = ((workingOnHolidayCount / totalListCount) * 100).toFixed(2) + "%";

        let timesheetData  = [{
          name: "Pending",
          y: pendingCount
        },
        {
          name: "Approved",
          y: approvedCount
        },
        {
          name: "Rejected",
          y: rejectedCount
        },
        {
          name: "Pending By User",
          y: pendingByUserCount
        }];

        console.log("timesheetData : ", timesheetData);
        this.renderPieSummaryChart('Timesheet Status Summary Chart', 'timesheetStatusSummary', timesheetData, 'Timesheet',this.openTimesheetSummaryTableModel.bind(this));
  }

  getCustomTimesheetReport(queryObjList:any , template:TemplateRef<any>) {
    this.timsheetSummaryList = [];

    let queryObj = new Query();
    queryObj.queryList = queryObjList;
    if(queryObjList == ''){
      this.get9DayTimesheetReport();
    }else {
      this.timesheetService.customQueryForTimesheetSummaryChart(queryObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.timsheetSummaryList = response.serviceResponse;
          console.log(this.timsheetSummaryList, " timsheetSummaryList");
          this.extractTimesheetReportData();
        } else {
          this.openAlertMod(template,response.serviceResponse);
        }
      });  
    }
  }

  getAllEmployeeList() {
    this.allEmployeeList = [];
    this.employeeInProbationAfter6MonthsCount = 0;
      
    this.employeeService.getAllEmployees().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allEmployeeList = response.serviceResponse;
          for(let x of this.allEmployeeList){
            x.employeementId = "A-".concat(x.employeementId);
            x.dateOfRelieving = moment(x.dateOfResign).add(x.noticePeriod, 'days').format(this.dateFormat);
            x.relievingMonth = moment(x.dateOfRelieving).format('MMMM');
            x.joiningMonth = moment(x.dateOfJoining).format('MMMM');
          }
            console.log("allEmployeeList : ", this.allEmployeeList)
            this.extractData();
      } else {
        alert(response.serviceResponse)
      } 
    });
  }

  getCustomEmployeesList(queryObjList:any , template : TemplateRef<any>) {
    this.allEmployeeList = [];
    this.employeeInProbationAfter6MonthsCount = 0;

    let queryObj = new Query();
    queryObj.queryList = queryObjList;

    if(queryObjList == ''){
      this.getAllEmployeeList();
    }else{
      this.employeeService.customQueryForEmployeeReport(queryObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allEmployeeList = response.serviceResponse;
          if(this.allEmployeeList.length == 0){
            this.openAlertMod(template, "No Data Found");
          }
          this.allEmployeeList.forEach(employee => {
            employee.employeementId = "A-".concat(employee.employeementId);
            employee.dateOfRelieving = moment(employee.dateOfResign).add(employee.noticePeriod, 'days').format(this.dateFormat);
            employee.relievingMonth = moment(employee.dateOfRelieving).format('MMMM');
            employee.joiningMonth = moment(employee.dateOfJoining).format('MMMM');
          });
          this.extractData();
          console.log("allEmployeeList : ", this.allEmployeeList)
        } else {
          this.openAlertMod(template,response.serviceResponse)
        }
      });
    }
  }

  extractData() {
    //Total Count
    this.countOfAllEmployees = this.allEmployeeList.filter(x => x.employmentstatus != 'InActive').length;
    console.log("Count of all employees : ",this.countOfAllEmployees);    

    //Fresher / Lateral Graph (Fresher/Experienced)
    //Graph Based Employment Status(Probation/Confirmed/Resigned/In-Active)
    //Male / Female Graph
    //Age Wise Graph
    // Total Experience Graph
    // Join vs Resign Graph
    let fresherCount = 0;
    let experienceCount = 0;

    let probationCount = 0;
    let confirmedCount = 0;
    let resignedCount = 0;
    let inActiveCount = 0;

    let maleCount = 0;
    let femaleCount = 0;
    let otherCount = 0;

    let countBetween18and25 = 0;
    let countBetween25and35 = 0;
    let countBetween35and45 = 0;
    let countAbove45 = 0;

    let experienceCountBetween0and1 = 0;
    let experienceCountBetween1and2 = 0;
    let experienceCountBetween2and5 = 0;
    let experienceCountBetween5and10 = 0;
    let experienceCountAbove10 = 0;

    let joiningJanCount = 0;
    let joiningFebCount = 0;
    let joiningMarCount = 0;
    let joiningAprilCount = 0;
    let joiningMayCount = 0;
    let joiningJuneCount = 0;
    let joiningJulyCount = 0;
    let joiningAugCount = 0;
    let joiningSepCount = 0;
    let joiningOctoberCount = 0;
    let joiningNovCount = 0;
    let joiningDecCount = 0;

    let resignJanCount = 0;
    let resignFebCount = 0;
    let resignMarCount = 0;
    let resignAprilCount = 0;
    let resignMayCount = 0;
    let resignJuneCount = 0;
    let resignJulyCount = 0;
    let resignAugCount = 0;
    let resignSepCount = 0;
    let resignOctoberCount = 0;
    let resignNovCount = 0;
    let resignDecCount = 0;

    this.allEmployeeList.forEach((employee)=>{
      let currentYear = moment().year();
      let dateToday = moment().format(this.dateFormat);

      if(employee.experience == 'Fresher' && employee.employmentstatus != 'InActive') fresherCount++;
      else if (employee.experience == 'Experienced' && employee.employmentstatus != 'InActive') experienceCount++;

      if (employee.employmentstatus == "Probation") probationCount++;
          else if (employee.employmentstatus == "Confirmed") confirmedCount++;
          else if (employee.employmentstatus == "Resigned") resignedCount++;
          else if (employee.employmentstatus == "InActive") inActiveCount++;
      
      if(employee.gender == 'male' && employee.employmentstatus != 'InActive') maleCount++;
      else if(employee.gender == 'female' && employee.employmentstatus != 'InActive') femaleCount++;
      else if(employee.gender == 'other' && employee.employmentstatus != 'InActive') otherCount++;
 
      if(employee.dateOfBirth != null && employee.employmentstatus != 'InActive'){
       let age = this.getAge(employee.dateOfBirth);       
       employee.age = age;
       console.log(age);
       if(age>= 18 && age <=25)countBetween18and25++;
       else if (age>25 && age<= 35) countBetween25and35++;
       else if (age>35 && age<= 45) countBetween35and45++;
       else if (age>45) countAbove45++;
      }

      if(employee.dateOfJoining != null && employee.totalExperience != null && employee.employmentstatus != 'InActive'){
        let empTotalExperience = this.totalExperience(employee.dateOfJoining, employee.totalExperience);
        employee.totalExperience = empTotalExperience.toFixed(1);
        console.log(empTotalExperience);
        if(empTotalExperience >= 0 && empTotalExperience <= 1)experienceCountBetween0and1++;
        else if(empTotalExperience > 1 && empTotalExperience <= 2)experienceCountBetween1and2++;
        else if(empTotalExperience > 2 && empTotalExperience <= 5)experienceCountBetween2and5++;
        else if(empTotalExperience > 5 && empTotalExperience <= 10)experienceCountBetween5and10++;
        else if(empTotalExperience > 10)experienceCountAbove10++;
      }
      
      if(employee.joiningMonth == 'January' && moment(employee.dateOfJoining).year() == currentYear)joiningJanCount++;
      else if(employee.joiningMonth == 'February' && moment(employee.dateOfJoining).year() == currentYear)joiningFebCount++;
      else if(employee.joiningMonth == 'March' && moment(employee.dateOfJoining).year() == currentYear)joiningMarCount++;
      else if(employee.joiningMonth == 'April' && moment(employee.dateOfJoining).year() == currentYear)joiningAprilCount++;
      else if(employee.joiningMonth == 'May' && moment(employee.dateOfJoining).year() == currentYear)joiningMayCount++;
      else if(employee.joiningMonth == 'June' && moment(employee.dateOfJoining).year() == currentYear)joiningJuneCount++;
      else if(employee.joiningMonth == 'July' && moment(employee.dateOfJoining).year() == currentYear)joiningJulyCount++;
      else if(employee.joiningMonth == 'August' && moment(employee.dateOfJoining).year() == currentYear)joiningAugCount++;
      else if(employee.joiningMonth == 'September' && moment(employee.dateOfJoining).year() == currentYear)joiningSepCount++;
      else if(employee.joiningMonth == 'October' && moment(employee.dateOfJoining).year() == currentYear)joiningOctoberCount++;
      else if(employee.joiningMonth == 'November' && moment(employee.dateOfJoining).year() == currentYear)joiningNovCount++;
      else if(employee.joiningMonth == 'December' && moment(employee.dateOfJoining).year() == currentYear)joiningDecCount++;

      if(employee.relievingMonth == 'January' && moment(employee.dateOfRelieving).year() == currentYear)resignJanCount++;
      else if(employee.relievingMonth == 'February' && moment(employee.dateOfRelieving).year() == currentYear)resignFebCount++;
      else if(employee.relievingMonth == 'March' && moment(employee.dateOfRelieving).year() == currentYear)resignMarCount++;
      else if(employee.relievingMonth == 'April' && moment(employee.dateOfRelieving).year() == currentYear)resignAprilCount++;
      else if(employee.relievingMonth == 'May' && moment(employee.dateOfRelieving).year() == currentYear)resignMayCount++;
      else if(employee.relievingMonth == 'June' && moment(employee.dateOfRelieving).year() == currentYear)resignJuneCount++;
      else if(employee.relievingMonth == 'July' && moment(employee.dateOfRelieving).year() == currentYear)resignJulyCount++;
      else if(employee.relievingMonth == 'August' && moment(employee.dateOfRelieving).year() == currentYear)resignAugCount++;
      else if(employee.relievingMonth == 'September' && moment(employee.dateOfRelieving).year() == currentYear)resignSepCount++;
      else if(employee.relievingMonth == 'October' && moment(employee.dateOfRelieving).year() == currentYear)resignOctoberCount++;
      else if(employee.relievingMonth == 'November' && moment(employee.dateOfRelieving).year() == currentYear)resignNovCount++;
      else if(employee.relievingMonth == 'December' && moment(employee.dateOfRelieving).year() == currentYear)resignDecCount++;

      if(employee.dateOfJoining != null && employee.employmentstatus != 'InActive'){
        if(moment(dateToday).diff(moment(employee.dateOfJoining), 'months') > 6 && employee.employmentstatus == 'Probation')this.employeeInProbationAfter6MonthsCount++;
      }
    });
  
    //Department wise Employee Count
    let departmentList = this.groupBy(this.allEmployeeList.filter(x => x.employmentstatus != 'InActive'),'departmentName');
    let employeeByDepartment = [];
      for (let department in departmentList) {        
         employeeByDepartment.push({departmentName:department , employeeCount: departmentList[department].length})
      }

    //Age Wise Graph

    console.log("Freshers count: ",fresherCount);
    console.log("Experience count: ",experienceCount);
    console.log("----------------------------------------------------");
    console.log("Probation employees: ",probationCount);
    console.log("Confirmed employees: ",confirmedCount);
    console.log("Resigned employees: ",resignedCount);
    console.log("In-Active employees: ",inActiveCount);
    console.log("----------------------------------------------------");
    console.log("Male employees: ",maleCount);
    console.log("Female employees: ",femaleCount);
    console.log("Other employees: ", otherCount);
    console.log("----------------------------------------------------") 
    console.log(employeeByDepartment);
    console.log("----------------------------------------------------") 
    console.log("Age 18-25: ",countBetween18and25);
    console.log("Age 26-35: ",countBetween25and35);
    console.log("Age 36-45: ",countBetween35and45);
    console.log("Age above 45: ",countAbove45);
    console.log("----------------------------------------------------")
    console.log("experience 0-1: ",experienceCountBetween0and1);
    console.log("experience 1-2: ",experienceCountBetween1and2);
    console.log("experience 2-5: ",experienceCountBetween2and5);
    console.log("experience 5-10: ",experienceCountBetween5and10);
    console.log("experience Above 10: ",experienceCountAbove10);
    console.log("----------------------------------------------------")
    console.log("joiningJanCount ", joiningJanCount);

    /*
    Chart Data for - Employee Status Graph.
    */
    let employeeStatusData  = [{
          name: "Probation",
          y: probationCount
        },
        {
          name: "Confirmed",
          y: confirmedCount
        },
        {
          name: "Resigned",
          y: resignedCount
        },
        {
          name: "In-Active",
          y: inActiveCount
        }];

        console.log("leaveStatusData : ", employeeStatusData);
        this.renderPieSummaryChart('Employee Status Summary', 'employeeStatus', employeeStatusData, 'Employee Status', this.openEmployeeStatusTableModal.bind(this));


        /*
        Chart Data for - Department Wise Employee Summary Graph.
       */

        let departmentWiseEmployeeData = employeeByDepartment.map(dept => {
          return [dept.departmentName, dept.employeeCount]
        })
        console.log("departmentWiseEmployeeData : ", departmentWiseEmployeeData);
        this.renderColumnBarSummaryChart('Department Wise Employee','departmentWiseEmployee',departmentWiseEmployeeData,'Department', this.openDepartmentWiseEmployeeModalTable.bind(this));

        /*
        Chart Data for - Male / Female - Gender Summary Graph.
       */

        let genderData  = [{
          name: "male",
          y: maleCount
        },
        {
          name: "female",
          y: femaleCount
        },
        {	
          name: "other",	
            y: otherCount	
          }];

        console.log("genderData : ", genderData);
        this.renderPieSummaryChart('Gender Summary', 'genderSummary', genderData, 'Employee Summary', this.openGenderSummaryModalTable.bind(this));

         /*
        Chart Data for - Age Summary Graph.
       */

        let employeeAgeData  = [{
          name: "18 to 25",
          y: countBetween18and25
        },
        {
          name: "25 to 35",
          y: countBetween25and35
        },
        {
          name: "35 to 45",
          y: countBetween35and45
        },
        {
          name: "45+",
          y: countAbove45
        }];

        console.log("employeeAgeData : ", employeeAgeData);
        this.renderPieSummaryChart('Age Summary', 'employeeAgeSummary', employeeAgeData, 'Employee Summary', this.openAgeSummayModalTable.bind(this));

        /*
        Chart Data for - Employee Experience Graph Data.
       */

        let experienceData = [{
          name: "0 to 1",
          y: experienceCountBetween0and1
        },{
          name: "1 to 2",
          y: experienceCountBetween1and2
        },
        {
          name: "2 to 5",
          y: experienceCountBetween2and5
        },
        {
          name: "5 to 10",
          y: experienceCountBetween5and10
        },
        {
          name: "10+",
          y: experienceCountAbove10
        }];

        let totalExperienceData = experienceData.map(exp => {
          return [exp.name, exp.y]
        })
        console.log(" totalExperienceData :", totalExperienceData);
        this.renderColumnBarSummaryChart('Employee Experience','employeeExperienceSummary',totalExperienceData,'Experience', this.openEmployeeExperienceModalTable.bind(this));

        /*
        Chart Data for - Employee Fresher - Lateral Graph Data.
       */

        let fresherLateralData  = [{
          name: "Fresher",
          y: fresherCount
        },
        {
          name: "Lateral",
          y: experienceCount
        }];

        console.log("genderData : ", genderData);
        this.renderPieSummaryChart('Fresher - Lateral Summary', 'fresherLateralChart', fresherLateralData, 'Employee Summary', this.openFresherLateralModalTable.bind(this));

        /*
        Chart Data for - Employee Join VS Resign
       */

        let empJoinResignData = [{
          name: "Joined",
          y: [joiningJanCount,joiningFebCount,joiningMarCount,joiningAprilCount,joiningMayCount,joiningJuneCount,joiningJulyCount,joiningAugCount,joiningSepCount,joiningOctoberCount,joiningNovCount,joiningDecCount]
        },
        {
          name: "Resigned",
          y: [resignJanCount,resignFebCount,resignMarCount,resignAprilCount,resignMayCount,resignJuneCount,resignJulyCount,resignAugCount,resignSepCount,resignOctoberCount,resignNovCount,resignDecCount]
        }]

        let finalEmpJoinResignData = empJoinResignData.map(x => {
          return {name : x.name, data : x.y}
        })
        console.log("finalEmpJoinResignData :", finalEmpJoinResignData);
        this.renderMultiBarChart('Employee Join VS Resign','employeeJoinAndResign',finalEmpJoinResignData,'Employee', this.openEmployeeJoinResignModalTable.bind(this));
  }

  groupBy(objectArray, property) {
    return objectArray.reduce((acc, obj) => {
       const key = obj[property];
       if (!acc[key]) {
          acc[key] = [];
       }
       // Add object to list for given key's value
       acc[key].push(obj);
       return acc;
    }, {});
 }

 multipleGroupByArray(dataArray, groupPropertyArray) {
  const groups = {};
  dataArray.forEach(item => {
      const group = JSON.stringify(groupPropertyArray(item));
      groups[group] = groups[group] || [];
      groups[group].push(item);
  });
  return Object.keys(groups).map(function(group) {
      return groups[group];
  });
}

  getAge(dateString) {
    var today = new Date();
    var birthDate = new Date(dateString);
    var age = today.getFullYear() - birthDate.getFullYear();
    var m = today.getMonth() - birthDate.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) {
      age--;
    }
    return age;
  }

  totalExperience(dateOfJoining, workExperience) {
    var today = new Date().getTime();
    var joiningDate = new Date(dateOfJoining).getTime();

    let difference = (today - joiningDate);
    var experienceInApmosys = difference / (1000 * 60 * 60 * 24 * 365);
    var employeeTotalExperience = experienceInApmosys + workExperience;
    return employeeTotalExperience;
  }

  // leave / Timesheet Summary Dashboard Chart

  renderPieSummaryChart(chartName:any, chartId:any, chartData:any, labelName:any, openMod:any){
    let colors = ['#DDDF00', '#64E572', '#ED561B', '#FFBF00'];

    if(chartId == "genderSummary"){
      colors = ['#88D2B8','#D288A2','#F33323'];
    }

    HighCharts.chart(chartId, {
      credits: {
        enabled: false
      },
      chart: {
        plotBackgroundColor: null,
        plotBorderWidth: null,
        plotShadow: false,
        type: 'pie'
      },
      title: {
        text: chartName,
        style:{	
          fontWeight: 'bold',	
          color: '#000000'	
        }
        
      },
      tooltip: {
        pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
      },
      accessibility: {
        point: {
          valueSuffix: '%'
        }
      },
      exporting: {
        enabled: true,
        buttons: {
          contextButton: {
            menuItems: [//"printChart",
              "viewFullscreen",
              "downloadPNG",
              "downloadJPEG",
              "downloadCSV",
              "downloadXLS",
              "downloadPDFDocument"
            ]
          }
        }
      },
      plotOptions: {
        pie: {
          borderWidth: 0,
          allowPointSelect: true,
          cursor: 'pointer',
          events: {
            click: function (event) {
              if(chartId == 'leaveSummaryChart'){
                openMod(event.point.name);
              }
              if(chartId == 'timesheetStatusSummary'){
                openMod(event.point.name);
              }
              if(chartId == 'employeeStatus'){
                openMod(event.point.name);
              }
              if(chartId == 'genderSummary'){
                openMod(event.point.name);
              }
              if(chartId == 'employeeAgeSummary'){
                openMod(event.point.name);
              }if(chartId == 'fresherLateralChart'){
                openMod(event.point.name);
              }
            },
          },
          dataLabels: {
            enabled: true,
            format: '<b>{point.name}</b>: {point.y:.1f}'
          },
          showInLegend: true
        }
      },
      legend: {
        enabled: true,
        labelFormatter: function () {
          const point:any = this;
          return this.name + ` : ${point.y}`;
      }
      },
      series: [{
        name: labelName,
        colorByPoint: true,
        type: undefined,
        data: chartData
      }],
      colors: colors
    });
  }

  // Employee Summary Dashboard

  renderColumnBarSummaryChart(chartName:any, chartId:any, chartData:any, labelName:any, openMod:any){

    HighCharts.chart(chartId, {
      chart: {
        type: 'column',
      },
      title: {
        text: chartName,
        style:{	
          fontWeight: 'bold',	
          color:'#000000'	
        }
      },
      xAxis: {
        categories: chartData,
        labels: {	
          overflow: 'justify',	
          style:{	
            fontWeight: 'bold',	
            color:'#000000',	
          }	
        },
      },
      yAxis: {
        min: 0,
        title: {
          text: 'No. Of Employees',
          align: 'high',
          style:{	
            fontWeight: 'bold',	
            color:'#000000',	
          }
        },
        labels: {
          overflow: 'justify',
          style:{	
            fontWeight: 'bold',	
            color:'#000000',	
          }
        },
      },
      tooltip: {
        valuePrefix: 'No. ',
      },
      plotOptions: {
        series: {
          cursor: 'pointer',
          point: {
            events: {
              click: function(event) {
                if(chartId == 'employeeExperienceSummary'){
                  openMod(event.point.name);
                }
                if(chartId == 'departmentWiseEmployee'){
                  openMod(event.point.name);
                }
              }
            },
          },
        },
        bar: {
          dataLabels: {
            enabled: true,
          },
          showInLegend: true
        },
      },
      credits: {
        enabled: false,
      },
      legend: {
        enabled: false
      },
      series: [
        {
          type: 'column',
          name: labelName,
          data: chartData
        },
      ],
    });
  }

  renderMultiBarChart(chartName:any, chartId:any, chartData:any, labelName:any, openMod:any){

    HighCharts.chart(chartId, {
      chart: {
        type: 'column',
      },
      title: {
        text: chartName,
        style:{	
          fontWeight: 'bold',	
          color: '#000000'	
        }
      },
      xAxis: {
        categories: [
          "January",
          "February",
          "March",
          "April",
          "May",
          "June",
          "July",
          "August",
          "September",
          "October",
          "November",
          "December"
        ],
        labels:{	
          style:{	
            fontWeight: 'bold',	
            color: '#000000'	
          }	
        }
        
      },
      yAxis: {
        min: 0,
        title: {
          text: 'No. Of Employees',
          align: 'high',
          style:{	
            fontWeight: 'bold',	
            color: '#000000'	
          }
        },
        labels: {
          overflow: 'justify',
          style:{
            fontWeight: 'bold',
            color: '#000000'
          }
        },
      },
      tooltip: {
        valuePrefix: 'No. ',
      },
      plotOptions: {
        series: {
          cursor: 'pointer',
          point: {
            events: {
              click: function(event) {
                if(chartId == 'employeeJoinAndResign'){
                  openMod(event.point.category, event.point.series.name);
                }
              }
            },
          },
        },
        bar: {
          dataLabels: {
            enabled: true,
          },
          showInLegend: true
        },
      },
      credits: {
        enabled: false,
      },
      legend: {
        enabled: true
      },
      series: chartData
    });
  }

  renderLineGraphChart(chartName:any, chartId:any, chartData:any, labelName:any, category:any, openMod:any){
    HighCharts.chart(chartId, {
      title: {
          text: chartName,
          style:{
            color: '#000000',
            fontWeight: 'bold'
          }
      },
      yAxis: {
          title: {
              text: 'Number of Leaves',
              style:{
                color:'#000000',
                fontWeight: 'bold'
              }
          },
          labels:{
            overflow: 'justify',
            style:{
              color: '#000000',
              fontWeight: 'bold'
            }
          }
      },
      xAxis: {
        categories: category,
        labels:{
          overflow: 'justify',
          style:{
            color: '#000000',
            fontWeight: 'bold'
          }
        }
      },
      plotOptions: {
          series: {
              label: {
                  connectorAllowed: false
              },
              point: {
                events: {
                  click: function(event) {
                    if(chartId == 'leaveTrendAnalysis'){
                      openMod(event.point.series.name,this.category);
                    }
                  }
                },
              },
          }
      },
      credits: {
        enabled: false,
      },
      series: chartData
  });
  }

    /* Filter */
    openFilterModal(template: TemplateRef<any>, columns:any[], title:any) {
      console.log("columns : ", columns);
      
      this.filterData.title  = title;
      this.filterData.columns = columns;

      if(this.filterData.title == 'Filter Timesheet Summary'){

        let fromDate = moment().subtract(8, 'd').format(this.dateFormat);
        let toDate = moment().format(this.dateFormat);

        this.queryList = [
          { column: "From Date", operator: ">", value: fromDate, conjunction: "AND" },
          { column: "To Date", operator: "<", value: toDate, conjunction: "" }
        ];
      }

      this.filterData.queryList = JSON.stringify(this.queryList);
  
      console.log("filterData : ", this.filterData);
      this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
    }
  
    onFilterSubmit(queryList:any , template:TemplateRef<any>){
      console.log("queryList : ", queryList);
      this.queryList = queryList;
      this.cancelRequest();

      if(this.filterData.title == 'Filter Employee Report'){
        this.getCustomEmployeesList(queryList,template);
      }
      if(this.filterData.title == 'Filter Leave Summary'){
        this.getCustomLeaveReport(queryList,template);
      }
      if(this.filterData.title == 'Filter Leave Trend Chart'){
        this.getCustomLeaveTrendAnalysisReport(queryList,template);
      }
      if(this.filterData.title == 'Filter Timesheet Summary'){
        this.getCustomTimesheetReport(queryList,template);
      }
    }

  // export excel

  exportToExcelLeaveSummary(): void {
    const onlySpecificDataArr = this.modalSummaryList.map(
      x => ({
        "Employment ID": x.employeementId,
        "Name":x.employeeName,
        "Department Name": x.departmentName,
        "From Date": x.fromDate,
        "To Date": x.toDate,
        "Status": x.status
      })
    )
    this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.modalTitle.concat(".xlsx"))
  }

  exportToExcelTimesheetSummary(): void {
    const onlySpecificDataArr = this.modalSummaryList.map(
      x => ({
        "Employment ID": x.employeementId,
        "Name":x.employeeName,
        "Department Name": x.departmentName,
        "Email": x.email,
        "Manager Name": x.managerName,
        "Mobile No.": x.mobileNo,
        "Pending EOD Count": x.pendingEodCount,
        "Type": x.legend
      })
    )
    this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.modalTitle.concat(".xlsx"));
  }

  exportToExcelEODSegregation(): void {
    const onlySpecificDataArr = this.modalSummaryList.map(
      x => ({
        "Employment ID": x.employeementId,
        "Name":x.employeeName,
        "Department Name": x.departmentName,
        "Email": x.email,
        "Manager Name": x.managerName,
        "Mobile No.": x.mobileNo,
        "Day Type": x.legend,
        "Total Working Hours": x.totalWorkingHours
      })
    )
    this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.modalTitle.concat(".xlsx"));
  }

  exportToExcelEmployeeSummary():void {
    const onlySpecificDataArr = this.modalSummaryList.map(
      x => ({
        "Employment ID": x.employeementId,
        "Name":x.employeeName,
        "Department Name": x.departmentName,
        "Email": x.email,
        "Manager Name": x.managerName,
        "Mobile No.": x.mobileNo,
        "Status": x.employmentstatus,
        "Total Experience": x.totalExperience,
        "Gender": x.gender,
        "Age": x.age
      })
    )
    this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.modalTitle.concat(".xlsx"))
  }

  //Pagination

  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  // Models

  openEodSegregation(template: TemplateRef<any>, titleName: any){
    let modalTableList = this.timsheetSummaryList;
    this.modalSummaryList = [];
    if(titleName == "Employee Worked Between 0 to 5 hour"){
      this.page=1;
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.dayType == 'Working' && x.totalWorkingHours >= 0 && x.totalWorkingHours <= 5);
      this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
    }
    if(titleName == "Employee Worked Between 5 to 8 hour"){
      this.page=1;
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.totalWorkingHours > 5 && x.totalWorkingHours <= 8);
      this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
    }
    if(titleName == "Employee Worked Between 8 to 9 hour"){
      this.page=1;
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.totalWorkingHours > 8 && x.totalWorkingHours <= 9);
      this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
    }
    if(titleName == "Employee Worked Between 9 to 10 hour"){
      this.page=1;
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.totalWorkingHours > 9 && x.totalWorkingHours <= 10);
      this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
    }
    if(titleName == "Employee Worked More than 10 hour"){
      this.page=1;
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.totalWorkingHours > 10);
      this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
    }
    if(titleName == "No Timesheet Submitted"){
      this.page=1;
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.legend == "Pending By User");
      this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
    }
    if(titleName == "Holiday"){
      this.page=1;
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.dayType == "Holiday");
      this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
    }
    if(titleName == "Working On Holiday"){
      this.page=1;
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.dayType == "Non-working");
      this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
    }
  }

  openLeaveSummaryTableModel(statusName:any) {
    let modalTableList = this.uniqueLeaveSumarryList;
    this.modalSummaryList = [];
    if(statusName == "Pending"){
      this.page=1;
      this.modalTitle = "Pending Leave Summary";
      this.modalSummaryList = modalTableList.filter(x => x.status == "Pending");
      this.modalRef = this.modalService.show(this.leaveSummaryTemplate, { class: 'modal-lg' });
    }
    if(statusName == "Rejected"){
      this.page=1;
      this.modalTitle = "Rejected Leave Summary";
      this.modalSummaryList = modalTableList.filter(x => x.status == "Rejected");
      this.modalRef = this.modalService.show(this.leaveSummaryTemplate, { class: 'modal-lg' });
    }
    if(statusName == "Approved"){
      this.page=1;
      this.modalTitle = "Approved Leave Summary";
      this.modalSummaryList = modalTableList.filter(x => x.status == "Approved");
      this.modalRef = this.modalService.show(this.leaveSummaryTemplate, { class: 'modal-lg' });
    }
  }

  openTimesheetSummaryTableModel(legendName:any){
    let modalTableList = this.timsheetSummaryList;
    this.modalSummaryList = [];
    if(legendName == "Pending"){
      this.page=1;
      this.modalTitle = "Pending Timesheet Summary";
      this.modalSummaryList = modalTableList.filter(x => x.legend == "Pending");
      this.modalRef = this.modalService.show(this.timesheetSummaryTemplate, { class: 'modal-xl' });
    }
    if(legendName == "Rejected"){
      this.page=1;
      this.modalTitle = "Rejected Timesheet Summary";
      this.modalSummaryList = modalTableList.filter(x => x.legend == "Rejected");
      this.modalRef = this.modalService.show(this.timesheetSummaryTemplate, { class: 'modal-xl' });
    }
    if(legendName == "Approved"){
      this.page=1;
      this.modalTitle = "Approved Timesheet Summary";
      this.modalSummaryList = modalTableList.filter(x => x.legend == "Approved");
      this.modalRef = this.modalService.show(this.timesheetSummaryTemplate, { class: 'modal-xl' });
    }
    if(legendName == "Pending By User"){
      this.page=1;
      this.modalTitle = "Pending By User Timesheet Summary";
      this.modalSummaryList = modalTableList.filter(x => x.legend == "Pending By User");
      this.modalRef = this.modalService.show(this.timesheetSummaryTemplate, { class: 'modal-xl' });
    }
  }

  openTotalCountModal(title:any){
    this.modalSummaryList = [];
    let dateToday = moment().format(this.dateFormat);
    let modalTableList = this.allEmployeeList;
    this.page = 1;
    this.modalTitle = title;
    if(title == 'All Active Employee'){
      this.modalSummaryList = modalTableList.filter(x => x.employmentstatus != "InActive");
    }else{
      this.modalSummaryList = modalTableList.filter(x => moment(dateToday).diff(moment(x.dateOfJoining), 'months') > 6 && x.employmentstatus == 'Probation');
    }
    this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
  }

  openEmployeeStatusTableModal(status:any){
    this.modalSummaryList = [];
    let modalTableList = this.allEmployeeList;
    if(status == "Probation"){
      this.page=1;
      this.modalTitle = "Employee In Probation";
      this.modalSummaryList = modalTableList.filter(x => x.employmentstatus == "Probation");
      this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
    }
    if(status == "Confirmed"){
      this.page=1;
      this.modalTitle = "Confirmed Employee";
      this.modalSummaryList = modalTableList.filter(x => x.employmentstatus == "Confirmed");
      this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
    }
    if(status == "Resigned"){
      this.page=1;
      this.modalTitle = "Resigned Employee";
      this.modalSummaryList = modalTableList.filter(x => x.employmentstatus == "Resigned");
      this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
    }
    if(status == "In-Active"){
      this.page=1;
      this.modalTitle = "In-Active Employee";
      this.modalSummaryList = modalTableList.filter(x => x.employmentstatus == "InActive");
      this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
    }
  }

  openGenderSummaryModalTable(gender:any){
    this.modalSummaryList = [];
    let modalTableList = this.allEmployeeList.filter(x => x.employmentstatus != 'InActive');
    if(gender == "male"){
      this.page=1;
      this.modalTitle = "Male Employee Data";
      this.modalSummaryList = modalTableList.filter(x => x.gender == "male");
      this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
    }
    if(gender == "female"){
      this.page=1;
      this.modalTitle = "Female Employee Data";
      this.modalSummaryList = modalTableList.filter(x => x.gender == "female");
      this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
    }
    if(gender == "other"){	
      this.page=1;	
      this.modalTitle = "Other Employee Data";	
      this.modalSummaryList = modalTableList.filter(x => x.gender == "other");	
      this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
    }
  }    

  openAgeSummayModalTable(age:any){
    this.modalSummaryList = [];
    let modalTableList = this.allEmployeeList.filter(x => x.employmentstatus != 'InActive');
    if(age == "18 to 25"){
      this.page=1;
      this.modalTitle = "Employee Age Between 18 to 25";
      this.modalSummaryList = modalTableList.filter(x => x.age>= 18 && x.age <=25);
      this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
    }
    if(age == "25 to 35"){
      this.page=1;
      this.modalTitle = "Employee Age Between 25 to 35";
      this.modalSummaryList = modalTableList.filter(x => x.age>25 && x.age<= 35);
      this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
    }
    if(age == "35 to 45"){
      this.page=1;
      this.modalTitle = "Employee Age Between 35 to 45";
      this.modalSummaryList = modalTableList.filter(x => x.age>35 && x.age<= 45);
      this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
    }
    if(age == "45+"){
      this.page=1;
      this.modalTitle = "Employee Age Above 45";
      this.modalSummaryList = modalTableList.filter(x => x.age > 45);
      this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
    }
  }

  openDepartmentWiseEmployeeModalTable(pointName:any){
    this.modalSummaryList = [];
    let modalTableList = this.allEmployeeList.filter(x => x.employmentstatus != 'InActive');
      this.page=1;
      this.modalTitle = "Employee(s) in "+pointName;
      this.modalSummaryList = modalTableList.filter(x => x.departmentName == pointName);
      this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
  }

  openEmployeeExperienceModalTable(pointName:any){
    this.modalSummaryList = [];
    let modalTableList = this.allEmployeeList.filter(x => x.employmentstatus != 'InActive');
    if(pointName == "0 to 1"){
      this.page=1;
      this.modalTitle = "Employee(s) with 0 to 1 YOE";
      this.modalSummaryList = modalTableList.filter(x => x.totalExperience != null && x.totalExperience >= 0 && x.totalExperience <=1);
      this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
    }
    if(pointName == "1 to 2"){
      this.page=1;
      this.modalTitle = "Employee(s) with 1 to 2 YOE";
      this.modalSummaryList = modalTableList.filter(x => x.totalExperience > 1 && x.totalExperience <=2);
      this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
    }
    if(pointName == "2 to 5"){
      this.page=1;
      this.modalTitle = "Employee(s) with 2 to 5 YOE";
      this.modalSummaryList = modalTableList.filter(x => x.totalExperience > 2 && x.totalExperience <=5);
      this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
    }
    if(pointName == "5 to 10"){
      this.page=1;
      this.modalTitle = "Employee(s) with 5 to 10 YOE";
      this.modalSummaryList = modalTableList.filter(x => x.totalExperience > 5 && x.totalExperience <=10);
      this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
    }
    if(pointName == "10+"){
      this.page=1;
      this.modalTitle = "Employee(s) with 10+ YOE";
      this.modalSummaryList = modalTableList.filter(x => x.totalExperience > 10);
      this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
    }
  }

  openFresherLateralModalTable(pointName:any){
    this.modalSummaryList = [];
    let modalTableList = this.allEmployeeList.filter(x => x.employmentstatus != 'InActive');
      this.page=1;
      this.modalTitle = "Employee(s) "+pointName;
      if(pointName == 'Lateral'){
        this.modalSummaryList = modalTableList.filter(x => x.experience == 'Experienced');
      }else{
        this.modalSummaryList = modalTableList.filter(x => x.experience == 'Fresher');
      }
      this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
  }

  openEmployeeJoinResignModalTable(category:any, name:any){
    this.modalSummaryList = [];
    let modalTableList = this.allEmployeeList;
    let dateToday = moment().year();

    if(name == 'Joined'){
      this.page=1;
      this.modalTitle = "Employee(s) "+name+" in "+category;
      this.modalSummaryList = modalTableList.filter(x => x.joiningMonth == category && moment(x.dateOfJoining).year() == dateToday);
      this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
    }
    if(name == 'Resigned'){
      this.page=1;
      this.modalTitle = "Employee(s) "+name+" in "+category;
      this.modalSummaryList = modalTableList.filter(x => x.relievingMonth == category && moment(x.dateOfRelieving).year() == dateToday);
      this.modalRef = this.modalService.show(this.employeeSummaryTemplate, { class: 'modal-xl' });
    }
  }

  openLeaveAnalysisTableModel(pointName:any, category:any){
    this.modalSummaryList = [];
    let modalTableList = this.leaveTrendAnalysisList;
      this.page=1;
      this.modalTitle = pointName + " taken on " + category;
      this.modalSummaryList = modalTableList.filter(x => x.leaveType == pointName && x.fromDate == category);
      this.modalRef = this.modalService.show(this.leaveSummaryTemplate, { class: 'modal-xl' });
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }

}