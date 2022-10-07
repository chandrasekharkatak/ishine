import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import * as HighCharts from 'highcharts';
import HC_exportData from "highcharts/modules/export-data";
import { first, groupBy } from 'rxjs/operators';
import { EmployeeService } from 'src/app/services/employee.service';
import { LeaveService } from 'src/app/services/leave.service';
import { TimesheetService } from 'src/app/services/timesheet.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
HC_exportData(HighCharts);

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
  employeeStatusTemplate: TemplateRef<any>;

  modalRef: BsModalRef = new BsModalRef();

  isleaveTimesheetDashboard:boolean = false;
  isEmployeeDashboard:boolean = false;

  data:any;
  leaveSumarryList:any[] = [];
  timsheetSummaryList:any[] = [];
  allEmployeeList: any[] = [];

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

  countOfAllEmployees:any;

  constructor(
    private leaveService : LeaveService,
    private timesheetService : TimesheetService,
    private modalService: BsModalService,
    private employeeService: EmployeeService,
    private exportExcelService: ExportExcelService
  ) { }

  ngOnInit(): void {
    //this.renderLeaveSummaryChart('Leave Summary','leaveSummaryChart',this.data,'Leave Summary Chart');
  //  this.renderTimesheetStatusSummaryChart('EOD Status Summary','EODStatusSummary',this.data,'EOD Status Chart');
    // this.renderEmployeeSummaryChart('Employee Summary','employeeSummary',this.data,'Employee Summary Chart');
    // this.renderProjectStatus('Project Summary','projectStatus',this.data,'Project Status Chart');
  
    this.sectionViewInit();
  }

  sectionViewInit(){
    this.leaveTimesheetDashboard();
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
  }

  get8DaysLeaveReport(){
    this.leaveSumarryList = [];

    this.leaveService.getLast8DaysLeaveReport().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveSumarryList = response.serviceResponse;
        console.log("leaveSumarryList : ", this.leaveSumarryList);

        let pendingCount = 0;
        let approvedCount = 0;
        let rejectedCount = 0;

        this.leaveSumarryList.forEach(leaveStatus => {

          if (leaveStatus.status == "Pending") pendingCount++;
          else if (leaveStatus.status == "Approved") approvedCount++;
          else if (leaveStatus.status == "Rejected") rejectedCount++;
        });

        let leaveStatusData  = [{
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
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  get9DayTimesheetReport(){
    this.timsheetSummaryList = [];

    this.timesheetService.getLast9DaysTimesheetReport().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.timsheetSummaryList = response.serviceResponse;
        console.log("timsheetSummaryList : ", this.timsheetSummaryList);

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
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  getAllEmployeeList() {
    this.allEmployeeList = [];
      
    this.employeeService.getAllEmployees().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allEmployeeList = response.serviceResponse;
          for(let x of this.allEmployeeList){
            x.employeementId = "A-".concat(x.employeementId);
          }
            console.log("allEmployeeList : ", this.allEmployeeList)
            this.extractData();
      } else {
        alert(response.serviceResponse)
      } 
    });
  }

  extractData() {
   
    //Total Count
    this.countOfAllEmployees = this.allEmployeeList.filter(x => x.employmentstatus != 'InActive').length;
    console.log("Count of all employees : ",this.countOfAllEmployees);    

    //Fresher / Lateral Graph (Fresher/Experienced)
    //Graph Based Employment Status(Probation/Confirmed/Resigned/In-Active)
    //Male / Female Graph
    //Age Wise Graph
    let fresherCount = 0;
    let experienceCount = 0;

    let probationCount = 0;
    let confirmedCount = 0;
    let resignedCount = 0;
    let inActiveCount = 0;

    let maleCount = 0;
    let femaleCount = 0;

    let countBetween18and25 = 0;
    let countBetween25and35 = 0;
    let countBetween35and45 = 0;
    let countAbove45 = 0;

    let experienceCountBetween0and1 = 0;
    let experienceCountBetween1and2 = 0;
    let experienceCountBetween2and5 = 0;
    let experienceCountBetween5and10 = 0;
    let experienceCountAbove10 = 0;

    this.allEmployeeList.forEach((employee)=>{

      if(employee.experience == 'Fresher') fresherCount++;
      else if (employee.experience == 'Experienced') experienceCount++;

      if (employee.employmentstatus == "Probation") probationCount++;
          else if (employee.employmentstatus == "Confirmed") confirmedCount++;
          else if (employee.employmentstatus == "Resigned") resignedCount++;
          else if (employee.employmentstatus == "InActive") inActiveCount++;
      
      if(employee.gender == 'male') maleCount++;
      else if(employee.gender == 'female') femaleCount++;

      if(employee.dateOfBirth != null){
       let age = this.getAge(employee.dateOfBirth);       
       employee.age = age;
       console.log(age);
       if(age>= 18 && age <=25)countBetween18and25++;
       else if (age>25 && age<= 35) countBetween25and35++;
       else if (age>35 && age<= 45) countBetween35and45++;
       else if (age>45) countAbove45++;       
      }

      if(employee.dateOfJoining != null && employee.totalExperience != null){
        let empTotalExperience = this.totalExperience(employee.dateOfJoining, employee.totalExperience);
        employee.totalExperience = empTotalExperience.toFixed(1);
        console.log(empTotalExperience);
        if(empTotalExperience >= 0 && empTotalExperience <= 1)experienceCountBetween0and1++;
        else if(empTotalExperience > 1 && empTotalExperience <= 2)experienceCountBetween1and2++;
        else if(empTotalExperience > 2 && empTotalExperience <= 5)experienceCountBetween2and5++;
        else if(empTotalExperience > 5 && empTotalExperience <= 10)experienceCountBetween5and10++;
        else if(empTotalExperience > 10)experienceCountAbove10++;
      }
      
    });
  
    //Department wise Employee Count
    let departmentList = this.groupBy(this.allEmployeeList,'departmentName');
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
        }];

        console.log("genderData : ", genderData);
        this.renderPieSummaryChart('Male / Female Summary', 'genderSummary', genderData, 'Employee Summary', this.openGenderSummaryModalTable.bind(this));

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
        this.renderColumnBarSummaryChart('Employee Experience','employeeExperienceSummary',totalExperienceData,'Experience', this.openEmployeeExperienceModalTable.bind(this));
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
      colors = ['#88D2B8','#D288A2'];
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
        text: chartName
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
      },
      // subtitle: {
      //   text: 'Data visualisation for analysing employee as per department',
      // },
      xAxis: {
        categories: chartData
      },
      yAxis: {
        min: 0,
        title: {
          text: 'No. Of Employees',
          align: 'high',
        },
        labels: {
          overflow: 'justify',
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

  // export excel

  exportToExcelLeaveSummary(): void {
    const onlySpecificDataArr = this.modalSummaryList.map(
      x => ({
        "Employment ID": x.employeementId,
        "Name":x.name,
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
      this.modalSummaryList = modalTableList.filter(x => x.totalWorkingHours > 0 && x.totalWorkingHours <= 5);
      this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
    }
    if(titleName == "Employee Worked Between 5 to 8 hour"){
      this.page=1;
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.totalWorkingHours > 5 && x.totalWorkingHours <= 8);
      this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
    }
    if(titleName == "Employee Worked Between 8 to 9 hour"){
      this.page=1;
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.totalWorkingHours > 8 && x.totalWorkingHours <= 9);
      this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
    }
    if(titleName == "Employee Worked Between 9 to 10 hour"){
      this.page=1;
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.totalWorkingHours > 9 && x.totalWorkingHours <= 10);
      this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
    }
    if(titleName == "Employee Worked More than 10 hour"){
      this.page=1;
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.totalWorkingHours > 10);
      this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
    }
    if(titleName == "No Timesheet Submitted"){
      this.page=1;
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.legend == "Pending By User");
      this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
    }
    if(titleName == "Holiday"){
      this.page=1;
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.dayType == "Holiday");
      this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
    }
    if(titleName == "Working On Holiday"){
      this.page=1;
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.dayType == "Non-working");
      this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
    }
  }

  openLeaveSummaryTableModel(statusName:any) {
    let modalTableList = this.leaveSumarryList;
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

  openTotalCountModal(){
    let modalTableList = this.allEmployeeList;
    this.page = 1;
    this.modalTitle = "All Active Employee Data";
    this.modalSummaryList = modalTableList.filter(x => x.employmentstatus != "InActive");
    this.modalRef = this.modalService.show(this.employeeStatusTemplate, { class: 'modal-xl' });
  }

  openEmployeeStatusTableModal(status:any){
    let modalTableList = this.allEmployeeList;
    if(status == "Probation"){
      this.page=1;
      this.modalTitle = "Employee In Probation";
      this.modalSummaryList = modalTableList.filter(x => x.employmentstatus == "Probation");
      this.modalRef = this.modalService.show(this.employeeStatusTemplate, { class: 'modal-xl' });
    }
    if(status == "Confirmed"){
      this.page=1;
      this.modalTitle = "Confirmed Employee";
      this.modalSummaryList = modalTableList.filter(x => x.employmentstatus == "Confirmed");
      this.modalRef = this.modalService.show(this.employeeStatusTemplate, { class: 'modal-xl' });
    }
    if(status == "Resigned"){
      this.page=1;
      this.modalTitle = "Resigned Employee";
      this.modalSummaryList = modalTableList.filter(x => x.employmentstatus == "Resigned");
      this.modalRef = this.modalService.show(this.employeeStatusTemplate, { class: 'modal-xl' });
    }
    if(status == "In-Active"){
      this.page=1;
      this.modalTitle = "In-Active Employee";
      this.modalSummaryList = modalTableList.filter(x => x.employmentstatus == "InActive");
      this.modalRef = this.modalService.show(this.employeeStatusTemplate, { class: 'modal-xl' });
    }
  }

  openGenderSummaryModalTable(gender:any){
    let modalTableList = this.allEmployeeList;
    if(gender == "male"){
      this.page=1;
      this.modalTitle = "Male Employee Data";
      this.modalSummaryList = modalTableList.filter(x => x.gender == "male");
      this.modalRef = this.modalService.show(this.employeeStatusTemplate, { class: 'modal-xl' });
    }
    if(gender == "female"){
      this.page=1;
      this.modalTitle = "Female Employee Data";
      this.modalSummaryList = modalTableList.filter(x => x.gender == "female");
      this.modalRef = this.modalService.show(this.employeeStatusTemplate, { class: 'modal-xl' });
    }
  }    

  openAgeSummayModalTable(age:any){
    let modalTableList = this.allEmployeeList;
    if(age == "18 to 25"){
      this.page=1;
      this.modalTitle = "Employee Age Between 18 to 25";
      this.modalSummaryList = modalTableList.filter(x => x.age>= 18 && x.age <=25);
      this.modalRef = this.modalService.show(this.employeeStatusTemplate, { class: 'modal-xl' });
    }
    if(age == "25 to 35"){
      this.page=1;
      this.modalTitle = "Employee Age Between 25 to 35";
      this.modalSummaryList = modalTableList.filter(x => x.age>25 && x.age<= 35);
      this.modalRef = this.modalService.show(this.employeeStatusTemplate, { class: 'modal-xl' });
    }
    if(age == "35 to 45"){
      this.page=1;
      this.modalTitle = "Employee Age Between 35 to 45";
      this.modalSummaryList = modalTableList.filter(x => x.age>35 && x.age<= 45);
      this.modalRef = this.modalService.show(this.employeeStatusTemplate, { class: 'modal-xl' });
    }
    if(age == "45+"){
      this.page=1;
      this.modalTitle = "Employee Age Above 45";
      this.modalSummaryList = modalTableList.filter(x => x.age > 45);
      this.modalRef = this.modalService.show(this.employeeStatusTemplate, { class: 'modal-xl' });
    }
  }

  openDepartmentWiseEmployeeModalTable(pointName:any){
    let modalTableList = this.allEmployeeList;
      this.page=1;
      this.modalTitle = "Employee(s) in "+pointName;
      this.modalSummaryList = modalTableList.filter(x => x.departmentName == pointName);
      this.modalRef = this.modalService.show(this.employeeStatusTemplate, { class: 'modal-xl' });
  }

  openEmployeeExperienceModalTable(pointName:any){
    let modalTableList = this.allEmployeeList;
    if(pointName == "0 to 1"){
      this.page=1;
      this.modalTitle = "Employee(s) with 0 to 1 YOE";
      this.modalSummaryList = modalTableList.filter(x => x.totalExperience != null && x.totalExperience >= 0 && x.totalExperience <=1);
      this.modalRef = this.modalService.show(this.employeeStatusTemplate, { class: 'modal-xl' });
    }
    if(pointName == "1 to 2"){
      this.page=1;
      this.modalTitle = "Employee(s) with 1 to 2 YOE";
      this.modalSummaryList = modalTableList.filter(x => x.totalExperience > 1 && x.totalExperience <=2);
      this.modalRef = this.modalService.show(this.employeeStatusTemplate, { class: 'modal-xl' });
    }
    if(pointName == "2 to 5"){
      this.page=1;
      this.modalTitle = "Employee(s) with 2 to 5 YOE";
      this.modalSummaryList = modalTableList.filter(x => x.totalExperience > 2 && x.totalExperience <=5);
      this.modalRef = this.modalService.show(this.employeeStatusTemplate, { class: 'modal-xl' });
    }
    if(pointName == "5 to 10"){
      this.page=1;
      this.modalTitle = "Employee(s) with 5 to 10 YOE";
      this.modalSummaryList = modalTableList.filter(x => x.totalExperience > 5 && x.totalExperience <=10);
      this.modalRef = this.modalService.show(this.employeeStatusTemplate, { class: 'modal-xl' });
    }
    if(pointName == "10+"){
      this.page=1;
      this.modalTitle = "Employee(s) with 10+ YOE";
      this.modalSummaryList = modalTableList.filter(x => x.totalExperience > 10);
      this.modalRef = this.modalService.show(this.employeeStatusTemplate, { class: 'modal-xl' });
    }
  }

  cancelRequest() {
    this.modalRef.hide();
  }

}