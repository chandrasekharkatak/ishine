import { Component, OnInit } from '@angular/core';
import * as HighCharts from 'highcharts';
import HC_exportData from "highcharts/modules/export-data";
import { first, groupBy } from 'rxjs/operators';
import { EmployeeService } from 'src/app/services/employee.service';
import { LeaveService } from 'src/app/services/leave.service';
import { TimesheetService } from 'src/app/services/timesheet.service';
HC_exportData(HighCharts);

@Component({
  selector: 'app-report-dashboard',
  templateUrl: './report-dashboard.component.html',
  styleUrls: ['./report-dashboard.component.css']
})
export class ReportDashboardComponent implements OnInit {

  data:any;
  leaveSumarryList:any[] = [];
  timsheetSummaryList:any[] = [];
  allEmployeeList: any[] = [];

  constructor(
    private leaveService : LeaveService,
    private timesheetService : TimesheetService,
    private employeeService: EmployeeService
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
        this.renderLeaveSummaryChart('Leave Summary Chart', 'leaveSummaryChart', leaveStatusData, 'Leaves');
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

        this.timsheetSummaryList.forEach(timesheet => {

          if (timesheet.legend == "Pending") pendingCount++;
          else if (timesheet.legend == "Approved") approvedCount++;
          else if (timesheet.legend == "Rejected") rejectedCount++;
          else if (timesheet.legend == "Pending By User") pendingByUserCount++;
        });

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
        this.renderTimesheetStatusSummaryChart('Timesheet Status Summary Chart', 'timesheetStatusSummary', timesheetData, 'Timesheet');
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

  renderLeaveSummaryChart(chartName:any, chartId:any, chartData:any, labelName:any){
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
        data: chartData
      }],
      colors: ['#DDDF00', '#64E572', '#ED561B']
    });
  }

  renderTimesheetStatusSummaryChart(chartName:any, chartId:any, chartData:any, labelName:any){
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
        data: chartData
      }],
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

}
