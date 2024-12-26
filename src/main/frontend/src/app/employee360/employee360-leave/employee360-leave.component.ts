import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import * as HighCharts from 'highcharts';
import HC_exportData from "highcharts/modules/export-data";
import { LeaveService } from '../../services/leave.service';
import { Leave } from 'src/app/models/leave';
import { first } from 'rxjs/operators';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';

import { Log } from '../../models/log';
import { LogService } from 'src/app/services/log.service';
import { Router } from '@angular/router';
import { UtilityService } from 'src/app/services/utility.service';
import { Employee360Service } from 'src/app/services/employee360.service';
import * as Highcharts from 'highcharts';

interface CustomChartPoint {
  name: string;
  y: number;
  approvedApplicationsCount: number;
  pendingApplicationsCount: number;
  rejectedApplicationsCount: number;
}
@Component({
  selector: 'app-employee360-leave',
  templateUrl: './employee360-leave.component.html',
  styleUrls: ['./employee360-leave.component.css']
})

export class Employee360LeaveComponent implements OnInit {

  leaveObj = new Leave();
  employeeData: any;
  employeeDetails: any;
  currentUserName = "";
  currentUser: User;
  feature = "Home";
  userMapping: any = {};
  log: Log;
  leaveTypes: Leave[] = [];
  leave: Leave[] = [];
  leaveBucketDetails: any[] = [];
  leaveBalanceList: any[] = [];
  rejectedLeavesList: any[] = [];
  approvedLeavesList: any[] = [];
  pendingLeavesList: any[] = [];
  activeButton: string = 'Pending';
  activeCompOffButton: string = 'Requests'; 
  formatedEmploymentID: any;
  leaveByMonth: Leave[] = [];
  Highcharts = Highcharts;
  leaveData: any = [];
  years: number[] = [];
  selectedYear: number;
  chartOptions: Highcharts.Options = {
    chart: {
      type: 'column'
    },
    title: {
      text: 'Leave Data Per Month'
    },
    xAxis: [{
      categories: [],  // Populated dynamically by 'prepareChartData'
      title: {
        text: 'Month'
      }
    }] as Highcharts.XAxisOptions[],  // Explicitly cast to an array of XAxisOptions if multiple axes are used
    yAxis: {
      min: 0,
      title: {
        text: 'Total Days'
      },
      stackLabels: {
        enabled: true,
        style: {
          fontWeight: 'bold',
          color: 'gray'
        }
      }
    },
    tooltip: {
      shared: true,
      valueSuffix: ' days'
    },
    plotOptions: {
      column: {
        stacking: 'normal'
      }
    },
    series: []  // Populated dynamically by 'prepareChartData'
  };
  

  constructor(
    private authenticationService: AuthenticationService,
    private leaveService: LeaveService,
    private logService: LogService,
    private router: Router,
    private utilityService: UtilityService,
    private employee360Service: Employee360Service,
  ) {
    this.authenticationService.currentUser.subscribe(x => {
      this.currentUser = x;
      this.currentUserName = this.currentUser.name.split(" ")[0];
      this.currentUserName = this.currentUserName[0].toUpperCase() + this.currentUserName.slice(1).toLowerCase();
    });
    this.logService.log.subscribe(x => {
      this.log = x;
      this.log.tabName = this.feature;
      this.log.featureName = this.feature;
    });
    const navigation = this.router.getCurrentNavigation();
    this.employeeData = navigation?.extras.state?.['employeeData'];
   }

  ngOnInit(): void {  
    console.log("Priyadarshini  Leave    ",this.employeeData);
    this.getAllLeaveTypesByLeavePolicies();
    this.generateLeaveChart();
    this.getLeaveDataPerMonthByEmpId(2023);
    this.getEmployeeDetails();
    this.renderChart();
  }

  setActiveButton(button: string): void {
    this.activeButton = button;
    if (button === 'CompOff') {
      this.activeCompOffButton = 'Requests';
    }
  }

  setActiveCompOffButton(button: string): void {
    this.activeCompOffButton = button;
  }

  getAllLeaveTypesByLeavePolicies() {
    this.leaveTypes = [];
    let leaveObj = new Leave();
    leaveObj.employmentStatus = this.employeeData.employmentstatus;
    leaveObj.gender = this.employeeData.gender;
    leaveObj.maritalStatus = this.employeeData.maritalStatus;

    this.leaveService.getAllLeaveTypesByLeavePolicies(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveTypes = response.serviceResponse;
        // console.log("leaveTypes : ", this.leaveTypes);
        this.leaveBucketDetails = this.leaveTypes.map((leave: Leave) => {
          let leaveObj = new Leave();
          leaveObj.leaveTypeMasterId = leave.leaveTypeMasterId;
          leaveObj.leaveType = leave.leaveType;
          leaveObj.leaveTypeCode = leave.leaveTypeCode;
          leaveObj.balance = 0;
          leaveObj.approvedApplicationsCount = 0
          leaveObj.pendingApplicationsCount = 0;
          leaveObj.rejectedApplicationsCount = 0;

          return leaveObj;
        });
        // console.log("getAllLeaveTypesByLeavePolicies leaveBucketDetails : ", this.leaveBucketDetails);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  async getMyLeaveBalancesByEmpId() {
    this.leaveBalanceList = [];
    this.rejectedLeavesList = [];
    this.approvedLeavesList = [];
    this.pendingLeavesList = [];

    let leaveObj = new Leave();
    this.formatedEmploymentID = this.utilityService.getEmployeeIdSubstring2(this.employeeData);
    leaveObj.employeementId = this.formatedEmploymentID;
    leaveObj.empId = this.employeeData.empId;

    let leaveBalanceResponse:any = await this.leaveService.getMyLeaveBalancesByEmpId(leaveObj).pipe(first()).toPromise();
    if (leaveBalanceResponse.serviceStatus == "Success") {
      this.leaveBalanceList = leaveBalanceResponse.serviceResponse;
      // console.log("leaveBalanceList : ", this.leaveBalanceList);
      this.leaveBucketDetails.forEach(data => {
        // console.log("Priyadarshini Leave   ",data);
        // console.log('Checking leave type match:', data.leaveTypeCode);
        let leaveDetail = this.leaveBalanceList.find((leave: Leave) => leave.leaveTypeCode == data.leaveTypeCode);
        // console.log('Matching detail:', leaveDetail);
        if (leaveDetail) {
          data.balance = (leaveDetail.balance) ? leaveDetail.balance : 0;
        }
      });
      // console.log("leaveBucketDetails with Balance : ", this.leaveBucketDetails);
    } else {
      console.error(leaveBalanceResponse.serviceResponse);
    }

    let approvedLeaveResponse:any = await this.leaveService.countMyApprovedLeaveApplicationsByLeaveType(leaveObj).pipe(first()).toPromise();
    if (approvedLeaveResponse.serviceStatus == "Success") {
      this.approvedLeavesList = approvedLeaveResponse.serviceResponse;
      // console.log("approvedLeaves : ", this.approvedLeavesList);

      this.leaveBucketDetails.forEach(data => {
        let leaveDetail = this.approvedLeavesList.find((leave: Leave) => leave.leaveTypeCode == data.leaveTypeCode);
        if (leaveDetail) {
          data.approvedApplicationsCount = (leaveDetail.applicationCount) ? leaveDetail.applicationCount : 0;
        }
      });
      // console.log("leaveBucketDetails with Approved Leaves : ", this.leaveBucketDetails);
    } else {
      console.error(approvedLeaveResponse.serviceResponse);
    }

    let rejectedLeaveResponse:any = await this.leaveService.countMyRejectedLeaveApplicationsByLeaveType(leaveObj).pipe(first()).toPromise();
    if (rejectedLeaveResponse.serviceStatus == 'Success') {
      this.rejectedLeavesList = rejectedLeaveResponse.serviceResponse;
      // console.log("Rejected Leaves : ", this.rejectedLeavesList);

      this.leaveBucketDetails.forEach(data => {
        let leaveDetail = this.rejectedLeavesList.find((leave: Leave) => leave.leaveTypeCode == data.leaveTypeCode);
        if (leaveDetail) {
          data.rejectedApplicationsCount = (leaveDetail.applicationCount) ? leaveDetail.applicationCount : 0;
        }
      });
      // console.log("leaveBucketDetails with Rejected Leaves : ", this.leaveBucketDetails);
    } else {
      console.error(rejectedLeaveResponse.serviceResponse);
    }

    let pendingLeaveResponse:any = await this.leaveService.countMyPendingLeaveApplicationsByLeaveType(leaveObj).pipe(first()).toPromise();
    if (pendingLeaveResponse.serviceStatus == "Success") {
      this.pendingLeavesList = pendingLeaveResponse.serviceResponse;
      // console.log("pendingLeavesList : ", this.pendingLeavesList);

      this.leaveBucketDetails.forEach(data => {
        let leaveDetail = this.pendingLeavesList.find((leave: Leave) => leave.leaveTypeCode == data.leaveTypeCode);
        if (leaveDetail) {
          data.pendingApplicationsCount = (leaveDetail.applicationCount) ? leaveDetail.applicationCount : 0;
        }
      });

      // console.log("leaveBucketDetails with pending leaves : ", this.leaveBucketDetails);
    } else {
      console.error(pendingLeaveResponse.serviceResponse);
    }
  }

  countMyRejectedLeaveApplicationsByLeaveType() {
    this.rejectedLeavesList = [];

    let leaveObj = new Leave();
    leaveObj.empId = this.employeeData.empId;
    this.leaveService.countMyRejectedLeaveApplicationsByLeaveType(leaveObj).pipe(first()).subscribe((response: any) => {

      if (response.serviceStatus == 'Success') {
        this.rejectedLeavesList = response.serviceResponse;
        // console.log("Rejected Leaves : ", this.rejectedLeavesList);

        this.leaveBucketDetails.forEach(data => {
          let leaveDetail = this.rejectedLeavesList.find((leave: Leave) => leave.leaveTypeCode == data.leaveTypeCode);
          if (leaveDetail) {
            data.rejectedApplicationsCount = (leaveDetail.applicationCount) ? leaveDetail.applicationCount : 0;
          }
        });

      } else {
        console.error(response.serviceResponse);
      }

    });
  }

  countMyApprovedLeaveApplicationsByLeaveType() {
    this.approvedLeavesList = [];
    let leaveObj = new Leave();
    leaveObj.empId = this.employeeData.empId;
    this.leaveService.countMyApprovedLeaveApplicationsByLeaveType(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.approvedLeavesList = response.serviceResponse;
        // console.log("approvedLeaves : ", this.approvedLeavesList);
        this.leaveBucketDetails.forEach(data => {
          let leaveDetail = this.approvedLeavesList.find((leave: Leave) => leave.leaveTypeCode == data.leaveTypeCode);
          if (leaveDetail) {
            data.approvedApplicationsCount = (leaveDetail.applicationCount) ? leaveDetail.applicationCount : 0;
          }
        });
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  countMyPendingLeaveApplicationsByLeaveType() {
    this.pendingLeavesList = [];
    let leaveObj = new Leave();
    leaveObj.empId = this.employeeData.empId;
    this.leaveService.countMyPendingLeaveApplicationsByLeaveType(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.pendingLeavesList = response.serviceResponse;
        // console.log("pendingLeavesList : ", this.pendingLeavesList);

        this.leaveBucketDetails.forEach(data => {
          let leaveDetail = this.pendingLeavesList.find((leave: Leave) => leave.leaveTypeCode == data.leaveTypeCode);
          if (leaveDetail) {
            data.pendingApplicationsCount = (leaveDetail.applicationCount) ? leaveDetail.applicationCount : 0;
          }
        });

      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  async generateLeaveChart() {
    await this.getMyLeaveBalancesByEmpId();

    this.chartData = this.leaveBucketDetails.map(leave => ({
      name: leave.leaveTypeCode, 
      y: leave.balance || 0, 
      approvedApplicationsCount: leave.approvedApplicationsCount || 0, 
      pendingApplicationsCount: leave.pendingApplicationsCount || 0, 
      rejectedApplicationsCount: leave.rejectedApplicationsCount || 0 
    }));

    // console.log("Chart Data: ", this.chartData);

    this.renderPieSummaryChart(
      'Leave Balance Distribution', 
      'leaveChartContainer',       
      this.chartData,            
      'Leave Type' 
    );
  } 

  chartData: CustomChartPoint[] = this.leaveBucketDetails.map(leave => ({
    name: leave.leaveTypeCode, 
    y: leave.balance || 0, 
    approvedApplicationsCount: leave.approvedApplicationsCount || 0, 
    pendingApplicationsCount: leave.pendingApplicationsCount || 0, 
    rejectedApplicationsCount: leave.rejectedApplicationsCount || 0 
  }));

  renderPieSummaryChart(chartName: any, chartId: any, chartData: any, labelName: any) {
    const colors = {
      PL: '#006d77',
      CL: '#BBDEF0',
      CO: '#e29578',
      ML: '#ffddd2',
      LWP: '#775144',
    };

    chartData = chartData.map((dataPoint: any) => ({
      ...dataPoint,
      color: colors[dataPoint.name] || '#cccccc',
    }));

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
        style: { fontWeight: 'bold', color: '#000000' },
      },
      tooltip: {
        pointFormatter: function () {
          const point = this as unknown as CustomChartPoint;
          return `
            <b>${point.name}</b><br/>
            Balance: <b>${point.y}</b><br/>
            Approved: <b>${point.approvedApplicationsCount}</b><br/>
            Pending: <b>${point.pendingApplicationsCount}</b><br/>
            Rejected: <b>${point.rejectedApplicationsCount}</b>
          `;
        },
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
            menuItems: [
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
              // if(chartId == 'leaveSummaryChart'){
              //   openMod(event.point.name);
              // }
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
          const point: any = this;
          return this.name + ` : ${point.y}`;
        }
      },
      series: [{
        name: labelName,
        colorByPoint: true,
        type: undefined,
        data: chartData
      }],
    });
  }
  
  getLeaveDataPerMonthByEmpId(year: number): void {
    this.leaveByMonth = [];
    
    this.employee360Service.getLeaveDataPerMonthByEmpId(this.employeeData.empId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {

        this.leaveByMonth = response.serviceResponse;
        console.log("getLeaveDataPerMonthByEmpId 1: ", this.leaveByMonth); 
        this.leaveByMonth = this.leaveByMonth.filter((leave) => {
          console.log("getLeaveDataPerMonthByEmpId 2: ", this.leaveByMonth);
          
          const leaveYear = parseInt(leave.leaveYear, 10);  
          return leaveYear === year;
        });
  
        this.prepareChartData();
        console.log("getLeaveDataPerMonthByEmpId : ", this.leaveByMonth);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }
  
  prepareChartData(): void {
    let leaveTypes = {};
    let months = Array.from({ length: 12 }, (_, i) => i + 1); 
    let chartData = {
      xAxisCategories: [],
      seriesData: [] 
    };
  
    chartData.xAxisCategories = months.map(month => month.toString());

    this.leaveByMonth.forEach((leave) => {
      const leaveMonth = parseInt(leave.leaveMonth, 10);
      const leaveType = leave.leaveType; 
      const leaveStatus = leave.leaveStatus; 
  
      if (!leaveTypes[leaveMonth]) {
        leaveTypes[leaveMonth] = {}; 
      }
  
      if (!leaveTypes[leaveMonth][leaveType]) {
        leaveTypes[leaveMonth][leaveType] = { "Pending": 0, "Approved": 0, "Rejected": 0, 'Revoked': 0, 'Applied For Revoke': 0 }; 
      }
      const totalDays = parseFloat(leave.totalDays); 
      if (!isNaN(totalDays)) {
        leaveTypes[leaveMonth][leaveType][leaveStatus] += totalDays;
      }
    });

    Object.keys(leaveTypes).forEach((month) => {
      const dataForMonth = [];
      
      Object.keys(leaveTypes[month]).forEach((leaveType) => {
        const leaveStatusData = leaveTypes[month][leaveType];
        
        Object.keys(leaveStatusData).forEach((status) => {
          dataForMonth.push({
            name: status,
            data: [leaveStatusData[status]],
            color: this.getColorForLeaveStatus(status), 
            stack: leaveType, 
          });
        });
      });
  
      chartData.seriesData.push(...dataForMonth);
    });
  
    this.chartOptions = {
      chart: {
        type: 'column'
      },
      title: {
        text: 'Leave Data Per Month'
      },
      xAxis: {
        categories: chartData.xAxisCategories,
        title: {
          text: 'Month'
        }
      },
      yAxis: {
        min: 0,
        title: {
          text: 'Total Days'
        },
        stackLabels: {
          enabled: true,
          style: {
            fontWeight: 'bold',
            color: 'gray'
          }
        }
      },
      tooltip: {
        shared: true,
        valueSuffix: ' days'
      },
      plotOptions: {
        column: {
          stacking: 'normal'
        }
      },
      series: chartData.seriesData 
    };
  
    console.log("Prepared Chart Data: ", this.chartOptions);
  }
  
  getColorForLeaveStatus(status: string) {
    const colorMap = {
      'Pending': '#a2d2ff',
      'Approved': '#778da9',
      'Rejected': '#415a77',
      'Revoked': '#0d1b2a',
      'Applied For Revoke': '#1b263b'
    };
    return colorMap[status] || '#2a9d8f'; 
  }
  
  
  
  getEmployeeDetails() {
    this.employeeDetails = [];
    this.formatedEmploymentID = Number(this.utilityService.getEmployeeIdSubstring2(this.employeeData));
    console.log("formatedEmploymentID  : ", this.formatedEmploymentID);

    this.employee360Service.getEmployeeDetails(this.formatedEmploymentID).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.employeeDetails = response.serviceResponse;
        console.log("getEmployeeDetails : ", this.employeeDetails);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  renderChart() {
    Highcharts.chart('leaveByMonthContainer', this.chartOptions); 
  }
}
