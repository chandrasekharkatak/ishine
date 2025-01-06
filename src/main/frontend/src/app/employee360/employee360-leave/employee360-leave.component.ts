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
import { ActivatedRoute, Router } from '@angular/router';
import { UtilityService } from 'src/app/services/utility.service';
import { Employee360Service } from 'src/app/services/employee360.service';
import * as Highcharts from 'highcharts';
import { BreadcrumbService } from 'src/app/services/breadcrumb.service';

declare module 'highcharts' {
  interface Series {
    status?: string; 
    leaveType?: string; 
  }
  interface SeriesOptions {
    status?: string; 
  }
}

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
  employeeData2: any;
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
  breadcrumbUrl:any[] = [];

  chartOptions: Highcharts.Options = {
    chart: {
        type: "column"
    },
    title: {
        text: "Leave Data Per Month"
    },
    xAxis: {
        categories: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"],
        title: {
            text: "Month"
        }
    },
    yAxis: {
        min: 0,
        title: {
            text: "Total Days"
        },
        stackLabels: {
            enabled: true,
            style: {
                fontWeight: "bold",
                color: "gray"
            }
        }
    },
    tooltip: {
        shared: true,
        valueSuffix: " days"
    },
    plotOptions: {
        column: {
            stacking: "normal"
        }
    },
    series: [
        {
            type: "column",
            name: "Revoked",
            data: [10, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 17],
            color: "#1b263b",
            stack: "CO"
        },
        {
            type: "column",
            name: "Rejected",
            data: [10, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 14],
            color: "#1b263b",
            stack: "CO"
        },
        {
            type: "column",
            name: "Approved",
            data: [10, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 11],
            color: "#1b263b",
            stack: "CO"
        },
        {
            type: "column",
            name: "Pending",
            data: [10, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 13],
            color: "#1b263b",
            stack: "CO"
        },
        {
            type: "column",
            name: "Applied For Revoke",
            data: [10, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 12],
            color: "#1b263b",
            stack: "CO"
        },
        {
            type: "column",
            name: "Revoked",
            data: [10, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 17],
            color: "#1b263b",
            stack: "PL"
        },
        {
            type: "column",
            name: "Rejected",
            data: [10, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 14],
            color: "#1b263b",
            stack: "PL"
        },
        {
            type: "column",
            name: "Approved",
            data: [10, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 11],
            color: "#1b263b",
            stack: "PL"
        },
        {
            type: "column",
            name: "Pending",
            data: [10, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 13],
            color: "#1b263b",
            stack: "PL"
        },
        {
            type: "column",
            name: "Applied For Revoke",
            data: [10, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 12],
            color: "#1b263b",
            stack: "PL"
        }
    ]
};

  constructor(
    private authenticationService: AuthenticationService,
    private leaveService: LeaveService,
    private logService: LogService,
    private router: Router,
    private utilityService: UtilityService,
    private employee360Service: Employee360Service,
    // private route: ActivatedRoute,
    private breadcrumbService: BreadcrumbService,
  ) {
    this.breadcrumbService.currentBreadcrumb.subscribe(x => this.breadcrumbUrl = x);

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
    this.employeeData2 = this.employeeData;
   }

  ngOnInit(): void {  
    this.employeeData2 = history.state.data;
    console.log("Priyadarshini  Leave    ",this.employeeData);
    const employeeName = this.employeeData?.name || "Employee";

    const breadcrumbObject = { title: `${employeeName} - Leave`, url: "/employee-360/leave" };
    this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);

    this.getAllLeaveTypesByLeavePolicies();
    this.generateLeaveChart();
    this.getLeaveDataPerMonthByEmpId(2024);
  }

  setActiveButton(button: string): void {
    this.activeButton = button;
    if (button === 'CompOff') {
      this.activeCompOffButton = 'Requests';
    }
  }

  setActiveCompOffButton(button: string): void {
    this.activeCompOffButton = button;
    if (button === 'Requests') {
      const dataToSend = { ...this.employeeData, status: 'comp-off-requests' };
      this.employee360Service.changeEmployeeData(dataToSend);
      this.redirectToMyTeam('comp-off-requests');
    }
    if (button === 'Applications') {
      const dataToSend = { ...this.employeeData, status: 'comp-off-requests' };
      this.employee360Service.changeEmployeeData(dataToSend);
      this.redirectToMyTeam('comp-off-applications');
    }
  }

  redirectToMyTeam(status: string): void {
    console.log(" redirectToMyTeam ", this.employeeData);
    const dataToSend = { ...this.employeeData, status };
    this.utilityService.setEmployee360ViewAccess(true);
    this.employee360Service.changeEmployeeData(dataToSend);

    const employeeName = this.employeeData?.name || "Employee";

    let breadcrumbObject = { title: `${employeeName} - ${status}`, url: "/user-team" };
    this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);

    this.router.navigate(['/user-team'], { 
      queryParams: { tab: 'my-team', action: 'view-pending-request', status }
    });

    console.log("Redirecting with queryParams:", { tab: 'my-team', action: 'view-pending-request', status });
    console.log(" redirectToMyTeam end ", this.employeeData);
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
    this.employee360Service.getLeaveDataPerMonthByEmpId(this.employeeData.empId).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus === "Success") {
            const leaveData = response.serviceResponse;
            const dataByLeaveType: any = {
                "PL": { "Revoked": Array(12).fill(0), "Approved": Array(12).fill(0), "Rejected": Array(12).fill(0), "Pending": Array(12).fill(0), "Applied for revoke": Array(12).fill(0) },
                "CL": { "Revoked": Array(12).fill(0), "Approved": Array(12).fill(0), "Rejected": Array(12).fill(0), "Pending": Array(12).fill(0), "Applied for revoke": Array(12).fill(0) },
                "CO": { "Revoked": Array(12).fill(0), "Approved": Array(12).fill(0), "Rejected": Array(12).fill(0), "Pending": Array(12).fill(0), "Applied for revoke": Array(12).fill(0) },
                "LWP": { "Revoked": Array(12).fill(0), "Approved": Array(12).fill(0), "Rejected": Array(12).fill(0), "Pending": Array(12).fill(0), "Applied for revoke": Array(12).fill(0) },
                "ML": { "Revoked": Array(12).fill(0), "Approved": Array(12).fill(0), "Rejected": Array(12).fill(0), "Pending": Array(12).fill(0), "Applied for revoke": Array(12).fill(0) }
            };

            leaveData.forEach(leave => {
                const leaveType = leave.leaveType;
                const leaveStatus = leave.leaveStatus;
                const leaveMonth = parseInt(leave.leaveMonth, 10) - 1;
                const totalDays = parseFloat(leave.totalDays);

                if (dataByLeaveType[leaveType] && dataByLeaveType[leaveType][leaveStatus]) {
                    dataByLeaveType[leaveType][leaveStatus][leaveMonth] += totalDays;
                }
            });

            const seriesData = [];
            const uniqueStatuses: Set<string> = new Set();
            for (let leaveType in dataByLeaveType) {
                for (let leaveStatus in dataByLeaveType[leaveType]) {
                    seriesData.push({
                        name: `${leaveType} - ${leaveStatus}`,
                        data: dataByLeaveType[leaveType][leaveStatus],
                        color: this.getLeaveStatusColor(leaveStatus),
                        stack: leaveType,
                        status: leaveStatus,
                        leaveType: leaveType
                    });
                    uniqueStatuses.add(leaveStatus);
                }
            }

            const legendItems = Array.from(uniqueStatuses).map(status => ({
                name: status,
                status: status,
                color: this.getLeaveStatusColor(status) 
            }));

            Highcharts.chart('leaveByMonthContainer', {
                chart: {
                    type: 'column',
                },
                title: {
                    text: 'Leave Data for 2024',
                },
                xAxis: {
                    categories: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"],
                    title: {
                        text: "Month",
                    },
                },
                yAxis: {
                    min: 0,
                    title: {
                        text: "Leave Count",
                    },
                },
                tooltip: {
                    shared: false,
                    formatter: function (this: Highcharts.TooltipFormatterContextObject) {
                        const seriesOptions = this.series.options as any; 
                        return `<b>Month:</b> ${this.x}<br/>` +
                            `<b>Status:</b> ${seriesOptions.status}<br/>` +
                            `<b>Days:</b> ${this.y}`;
                    },
                },
                legend: {
                    labelFormatter: function () {
                        const seriesOptions = this.options as any; 
                        return seriesOptions.status;
                    },
                    useHTML: true,
                },
                plotOptions: {
                    column: {
                        stacking: 'normal',
                    },
                    series: {
                        events: {
                            legendItemClick: function () {
                                const clickedStatus = (this.options as any).status;
                                this.chart.series.forEach(series => {
                                    if ((series.options as any).status === clickedStatus) {
                                        series.visible ? series.hide() : series.show();
                                    }
                                });

                                return false;
                            },
                        },
                        showInLegend: true, 
                    },
                },
                series: seriesData,
            },
            function(chart) {
              console.log('Highcharts Chart Object:', chart);
          });      
        } else {
            console.error(response.serviceResponse);
        }
    });
}

getLeaveStatusColor(status: string): string {
    switch (status) {
        case "Approved":
            return "#c5d86d";
        case "Rejected":
            return "#e63946";
        case "Pending":
            return "#fde74c";
        case "Revoked":
            return "#4e878c";
        case "Applied for revoke":
            return "#f2bac9";
        default:
            return "#122f97";
    }
  }
}
