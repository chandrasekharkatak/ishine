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

  modalRef: BsModalRef = new BsModalRef();

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

  constructor(
    private leaveService : LeaveService,
    private timesheetService : TimesheetService,
    private modalService: BsModalService,
    private employeeService: EmployeeService,
    private exportExcelService: ExportExcelService
  ) { }

  ngOnInit(): void {
    this.get8DaysLeaveReport();
    this.get9DayTimesheetReport();
    this.getAllEmployeeList();
    //this.renderLeaveSummaryChart('Leave Summary','leaveSummaryChart',this.data,'Leave Summary Chart');
  //  this.renderTimesheetStatusSummaryChart('EOD Status Summary','EODStatusSummary',this.data,'EOD Status Chart');
    this.renderEmployeeSummaryChart('Employee Summary','employeeSummary',this.data,'Employee Summary Chart');
    this.renderProjectStatus('Project Summary','projectStatus',this.data,'Project Status Chart');
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
        this.renderLeaveSummaryChart('Leave Summary Chart', 'leaveSummaryChart', leaveStatusData, 'Leaves', this.openLeaveSummaryTableModel.bind(this));
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

        this.timsheetSummaryList.forEach(timesheet => {

          if (timesheet.legend == "Pending") pendingCount++;
          else if (timesheet.legend == "Approved") approvedCount++;
          else if (timesheet.legend == "Rejected") rejectedCount++;
          else if (timesheet.legend == "Pending By User") pendingByUserCount++;

          
          if(timesheet.totalWorkingHours > 0 && timesheet.totalWorkingHours <= 5) zeroToFiveCount++;
          else if(timesheet.totalWorkingHours > 5 && timesheet.totalWorkingHours <= 8) fiveToEightCount++;
          else if(timesheet.totalWorkingHours > 8 && timesheet.totalWorkingHours <= 9) eightToNineCount++;
          else if(timesheet.totalWorkingHours > 9 && timesheet.totalWorkingHours <= 10) nineToTenCount++;
          else if(timesheet.totalWorkingHours > 10) tenAndAboveCount++;
        });

        this.zeroToFive = ((zeroToFiveCount / this.timsheetSummaryList.length) * 100).toFixed(2) + "%";
        this.fiveToEight = ((fiveToEightCount / this.timsheetSummaryList.length) * 100).toFixed(2) + "%";
        this.eightToNine = ((eightToNineCount / this.timsheetSummaryList.length) * 100).toFixed(2) + "%";
        this.nineToTen = ((nineToTenCount / this.timsheetSummaryList.length) * 100).toFixed(2) + "%";
        this.tenAndAbove = ((tenAndAboveCount / this.timsheetSummaryList.length) * 100).toFixed(2) + "%";
        this.noEODSubmitted = ((pendingByUserCount / this.timsheetSummaryList.length) * 100).toFixed(2) + "%";

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
        this.renderTimesheetStatusSummaryChart('Timesheet Status Summary Chart', 'timesheetStatusSummary', timesheetData, 'Timesheet',this.openTimesheetSummaryTableModel.bind(this));
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
    let countOfAllEmployees = this.allEmployeeList.length;
    console.log("Count of all employees : ",countOfAllEmployees);    

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
    this.allEmployeeList.forEach((employee)=>{

      if(employee.experience == 'Fresher') fresherCount++;
      else if (employee.experience == 'Experienced') experienceCount++;

      if (employee.employmentstatus == "Probation") probationCount++;
          else if (employee.employmentstatus == "Confirmed") confirmedCount++;
          else if (employee.employmentstatus == "Resigned") resignedCount++;
          else if (employee.employmentstatus == "In-Active") inActiveCount++;
      
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

  renderLeaveSummaryChart(chartName:any, chartId:any, chartData:any, labelName:any, openLeaveMod:any){
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
              if(event.point.name == "Pending"){
                openLeaveMod("Pending");
              }if(event.point.name == "Rejected"){
                openLeaveMod("Rejected");
              }if(event.point.name == "Approved"){
                openLeaveMod("Approved");
              }
            }
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
      colors: ['#DDDF00', '#64E572', '#ED561B']
    });
  }

  renderTimesheetStatusSummaryChart(chartName:any, chartId:any, chartData:any, labelName:any, openTimesheetMod:any){
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
      plotOptions: {
        pie: {
          borderWidth: 0,
          allowPointSelect: true,
          cursor: 'pointer',
          events: {
            click: function (event) {
              if(event.point.name == "Pending"){
                openTimesheetMod("Pending");
              }
              if(event.point.name == "Approved"){
                openTimesheetMod("Approved");
              }
              if(event.point.name == "Rejected"){
                openTimesheetMod("Rejected");
              }
              if(event.point.name == "Pending By User"){
                openTimesheetMod("Pending By User");
              }
            }
          },
          dataLabels: {
            enabled: true,
            format: '<b>{point.name}</b>: {point.y:.1f}'
          },
          showInLegend: true
        }
      },
      series: [{
        name: labelName,
        colorByPoint: true,
        type: undefined,
        data: chartData
      }],
      legend: {
        enabled: true,
        labelFormatter: function () {
          const point:any = this;
          return this.name + ` : ${point.y}`;
      }
      },
      colors: ['#193d8a', '#ED561B', '#DDDF00', '#24CBE5', '#64E572', '#FF9655', '#FFF263', '#6AF9C4']
    });
  }

  renderEmployeeSummaryChart(chartName:any, chartId:any, chartData:any, labelName:any){
    HighCharts.chart(chartId, {
      credits: {
        enabled: false
      },
      chart: {
        plotBackgroundColor: null,
        plotBorderWidth: null,
        plotShadow: false
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
      plotOptions: {
          pie: {
              dataLabels: {
                  enabled: false,
              },
              startAngle: -90,
              endAngle: 90,
              center: ['50%', '75%'],
              size: '110%',
              showInLegend: true
          }
      },
      series: [{
          type: 'pie',
          name: 'Browser share',
          innerSize: '50%',
          data: [{
            name: 'Chrome',
            y: 38.41,
            selected: true
          }, {
            name: 'Internet Explorer',
            y: 11.84
          },{
            name: 'ABC Explorer',
            y: 19.84
          },{
            name: 'EFG Explorer',
            y: 15.84
          },{
            name: 'IJK Explorer',
            y: 21.84
          },{
            name: 'LMN Explorer',
            y: 11.84
          }]
      }]
  });
  }

  renderProjectStatus(chartName:any, chartId:any, chartData:any, labelName:any){
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
      plotOptions: {
        pie: {
          borderWidth: 0,
          allowPointSelect: true,
          cursor: 'pointer',
          dataLabels: {
            enabled: false,
            format: '<b>{point.name}</b>: {point.y:.1f}'
          },
          showInLegend: true
        }
      },
      series: [{
        name: labelName,
        colorByPoint: true,
        type: undefined,
        data: [{
          name: 'Completed',
          y: 10,
          selected: true
        }, {
          name: 'In-Progress',
          y: 12
        }]
      }]
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
        "Tpye": x.legend
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
      console.log("titleName");
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.totalWorkingHours > 0 && x.totalWorkingHours <= 5);
      this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
    }
    if(titleName == "Employee Worked Between 5 to 8 hour"){
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.totalWorkingHours > 5 && x.totalWorkingHours <= 8);
      this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
    }
    if(titleName == "Employee Worked Between 8 to 9 hour"){
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.totalWorkingHours > 8 && x.totalWorkingHours <= 9);
      this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
    }
    if(titleName == "Employee Worked Between 9 to 10 hour"){
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.totalWorkingHours > 9 && x.totalWorkingHours <= 10);
      this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
    }
    if(titleName == "Employee Worked More than 10 hour"){
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.totalWorkingHours > 10);
      this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
    }
    if(titleName == "No Timesheet Submitted"){
      this.modalTitle = titleName;
      this.modalSummaryList = modalTableList.filter(x => x.legend == "Pending By User");
      this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
    }
  }

  openLeaveSummaryTableModel(statusName:any) {
    let modalTableList = this.leaveSumarryList;
    this.modalSummaryList = [];
    if(statusName == "Pending"){
      this.modalTitle = "Pending Leave Summary";
      this.modalSummaryList = modalTableList.filter(x => x.status == "Pending");
      this.modalRef = this.modalService.show(this.leaveSummaryTemplate, { class: 'modal-lg' });
    }
    if(statusName == "Rejected"){
      this.modalTitle = "Rejected Leave Summary";
      this.modalSummaryList = modalTableList.filter(x => x.status == "Rejected");
      this.modalRef = this.modalService.show(this.leaveSummaryTemplate, { class: 'modal-lg' });
    }
    if(statusName == "Approved"){
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

  cancelRequest() {
    this.modalRef.hide();
  }

}