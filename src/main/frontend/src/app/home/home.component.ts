import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { Leave } from '../models/leave';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { LeaveService } from '../services/leave.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import * as HighCharts from 'highcharts';
import { Router } from '@angular/router';
import { EmployeeService } from '../services/employee.service';
import { Timesheet } from '../models/timesheet';
import { TimesheetService } from '../services/timesheet.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  //modal 
  alertMessage:any;
  modalRef: BsModalRef = new BsModalRef();
  

  feature="Home";
  currentUser:User;
  userMapping:any = {};

  isReqPending:boolean = true;
  leaveApplicationCount:any = 0;
  leaveApplicationList:any[] = [];

  compOffApplicationCount:any = 0;
  allCompOffApplications:any[] = [];

  timesheetApplicationCount:any = 0;

  //export excel
  excelName:any = '';

  birthdayList:any[] =[];
  eventImages:any[] = ['event1', 'event2', 'event3', 'event4', 'event5'];
  
  constructor(
    private modalService: BsModalService,
    private authenticationService : AuthenticationService,
    private leaveService : LeaveService,
    private exportExcelService: ExportExcelService,
    private router: Router,
    private employeeService: EmployeeService,
    private timesheetService : TimesheetService
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
   }

  ngOnInit(): void {
    this.countAllMyTeamsPendingLeaveApplicationsByManagerId();
    this.countPendingCompOffRequestsByManagerId();
    this.countMyReporteesTimesheetRequests();

    this.renderPieChart();
    this.getAllEmployeesBirthDayToday();
  }

  // Leave Applications
  getAllMyTeamsPendingLeaveApplicationsByManagerId(){
    this.leaveApplicationList = []

    let leaveObj = new Leave();
    leaveObj.managerId = this.currentUser.empId;
    this.leaveService.getAllMyTeamsPendingLeaveApplicationsByManagerId(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveApplicationList = response.serviceResponse;
        console.log("leaveApplicationList : ", this.leaveApplicationList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  /* Leave Applications Count
  *  Added by suraj 07/08/2022
  */
  countAllMyTeamsPendingLeaveApplicationsByManagerId(){
    this.leaveApplicationList = []

    let leaveObj = new Leave();
    leaveObj.managerId = this.currentUser.empId;
    this.leaveService.countAllMyTeamsPendingLeaveApplicationsByManagerId(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveApplicationCount = response.serviceResponse.applicationCount;
        console.log("leaveApplicationCount : ", this.leaveApplicationCount);
      } else {
        this.leaveApplicationCount = 0;
        console.error(response.serviceResponse);
      }
    });
  }

  onUpdateLeaveStatus(template: TemplateRef<any>, leaveApplication, updatedLeaveStatusId){
    // 1 = pending , 2 = Approved , 3= Rejected
    leaveApplication.leaveStatusId = updatedLeaveStatusId;
    leaveApplication.leaveStatusUpdatedBy = this.currentUser.empId
    console.log("leaveApplication : ", leaveApplication);
    
    this.leaveService.updateLeaveStatus(leaveApplication).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {        
        this.countAllMyTeamsPendingLeaveApplicationsByManagerId();
        this.getAllMyTeamsPendingLeaveApplicationsByManagerId();
        this.openAlertMod(template, response.serviceResponse);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }


  //comOff Applications
  getPendingCompOffRequestsByManagerId(){
    this.allCompOffApplications = []

    let compOff = new Leave();
    compOff.managerId = this.currentUser.empId;
    this.leaveService.getPendingCompOffRequestsByManagerId(compOff).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allCompOffApplications = response.serviceResponse;
        console.log("allCompOffApplications : ", this.allCompOffApplications);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  /* Leave Applications Count
  *  Added by suraj 07/08/2022
  */
  countPendingCompOffRequestsByManagerId(){
    this.allCompOffApplications = []

    let compOff = new Leave();
    compOff.managerId = this.currentUser.empId;
    this.leaveService.countPendingCompOffRequestsByManagerId(compOff).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.compOffApplicationCount =  response.serviceResponse.applicationCount;
        console.log("CompOffApplicationCount : ", this.compOffApplicationCount);
      } else {
        this.compOffApplicationCount =  0;
        console.error(response.serviceResponse);
      }
    });
  }

  onUpdateCompOffStatus(template: TemplateRef<any>, compOffObj, updatedCompOffStatusId){
    // 1 = pending , 2 = Approved , 3= Rejected
    compOffObj.leaveStatusId = updatedCompOffStatusId;
    compOffObj.leaveStatusUpdatedBy = this.currentUser.empId
    console.log("Update Comp off : ", compOffObj);
    this.leaveService.updateCompOffById(compOffObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {        
        this.countPendingCompOffRequestsByManagerId();
        this.getPendingCompOffRequestsByManagerId();
        this.openAlertMod(template, response.serviceResponse);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  /* Timesheets Applications Count
  *  Added by suraj 07/08/2022
  */
  countMyReporteesTimesheetRequests(){
    this.allCompOffApplications = []

    let timesheet = new Timesheet();
    timesheet.managerId = this.currentUser.empId;
    this.timesheetService.countMyReporteesTimesheetRequests(timesheet).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.timesheetApplicationCount =  response.serviceResponse.applicationCount;
        console.log("TimesheetApplicationCount : ", this.timesheetApplicationCount);
      } else {
        this.timesheetApplicationCount =  0;
        console.error(response.serviceResponse);
      }
    });
  }



  // Graphs 
  renderPieChart() {
    // Leave Bucket
    HighCharts.chart('leaveBucketChart', {
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
        text: 'Leave Bucket'
      },
      tooltip: {
        pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
      },
      plotOptions: {
        pie: {
          borderWidth: 0,
          innerSize: '50%',
          allowPointSelect: true,
          cursor: 'pointer',
          dataLabels: {
            enabled: false,
            format: '<b>{point.name}</b>: {point.percentage:.1f} %'
          }
        }
      },
      series: [{
        name: 'Brands',
        colorByPoint: true,
        type: undefined,
        data: [{
          name: 'PL',
          y: 61.41,
        }, {
          name: 'CL',
          y: 27.74
        }, {
          name: 'LWP',
          y: 10.85
        }]
      }],
      colors:
      ['#63b598', '#008eff','#f1a0ff', '#7260d8', '#fce877','#84a3ff', '#b5e0d3', '#e535fc','#7d9ff7', '#513d98', 
       '#ffe2f8', '#00a0da', '#ffb380','#f697c1', '#4ca2f9', '#ffa2bc', '#96e591', '#f1ae16', '#2f7b99',
        '#b259ab', '#ff8473', '#0086b3', '#00861f', '#00696c', '#d36647',  '#c6f5e4', '#e7dbce', '#ccfeff','#f5f3e9', '#f0f7f7'
      ],
    });

    // Leave Approved 
    HighCharts.chart('leaveApprovedChart', {
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
        text: 'Leave Approved'
      },
      tooltip: {
        pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
      },
      plotOptions: {
        pie: {
          borderWidth: 0,
          innerSize: '50%',
          allowPointSelect: true,
          cursor: 'pointer',
          dataLabels: {
            enabled: false,
            format: '<b>{point.name}</b>: {point.percentage:.1f} %'
          }
        }
      },
      series: [{
        name: 'Brands',
        colorByPoint: true,
        type: undefined,
        data: [{
          name: 'PL',
          y: 61.41,
        }, {
          name: 'CL',
          y: 27.74
        }, {
          name: 'LWP',
          y: 10.85
        }]
      }],
      colors:
      ['#63b598', '#008eff','#f1a0ff', '#7260d8', '#fce877','#84a3ff', '#b5e0d3', '#e535fc','#7d9ff7', '#513d98', 
       '#ffe2f8', '#00a0da', '#ffb380','#f697c1', '#4ca2f9', '#ffa2bc', '#96e591', '#f1ae16', '#2f7b99',
        '#b259ab', '#ff8473', '#0086b3', '#00861f', '#00696c', '#d36647',  '#c6f5e4', '#e7dbce', '#ccfeff','#f5f3e9', '#f0f7f7'
      ],
    });

    // Leave Request
    HighCharts.chart('leaveRequestChart', {
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
        text: 'Leave Requests'
      },
      tooltip: {
        pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
      },
      plotOptions: {
        pie: {
          borderWidth: 0,
          innerSize: '50%',
          allowPointSelect: true,
          cursor: 'pointer',
          dataLabels: {
            enabled: false,
            format: '<b>{point.name}</b>: {point.percentage:.1f} %'
          }
        }
      },
      series: [{
        name: 'Brands',
        colorByPoint: true,
        type: undefined,
        data: [{
          name: 'PL',
          y: 61.41,
        }, {
          name: 'CL',
          y: 27.74
        }, {
          name: 'LWP',
          y: 10.85
        }]
      }],
      colors:
      ['#63b598', '#008eff','#f1a0ff', '#7260d8', '#fce877','#84a3ff', '#b5e0d3', '#e535fc','#7d9ff7', '#513d98', 
       '#ffe2f8', '#00a0da', '#ffb380','#f697c1', '#4ca2f9', '#ffa2bc', '#96e591', '#f1ae16', '#2f7b99',
        '#b259ab', '#ff8473', '#0086b3', '#00861f', '#00696c', '#d36647',  '#c6f5e4', '#e7dbce', '#ccfeff','#f5f3e9', '#f0f7f7'
      ],
    });

    // Total EOD 
    HighCharts.chart('totalEODChart', {
      credits: {
        enabled: false
      },
      chart: {
        type: 'pie',
        borderWidth: 0,
        borderRadius: 0,
        plotBackgroundColor: null,
        plotShadow: false,
        plotBorderWidth: 0,
      },
      title: {
        text: '',
        floating: true
      },
      tooltip: {
        pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
      },
      plotOptions: {
        pie: {
          borderWidth: 0,
          innerSize: '50%',
          allowPointSelect: true,
          cursor: 'pointer',
          dataLabels: {
            enabled: false,
            format: '<b>{point.name}</b>: {point.percentage:.1f} %'
          }
        }
      },
      series: [{
        name: 'Working Hours',
        colorByPoint: true,
        type: undefined,
        data: [{
          name: '19-08-2022',
          y: 40,
        }, {
          name: '18-08-2022',
          y: 30
        }, {
          name: '17-08-2022',
          y: 20
        }, {
          name: '16-08-2022',
          y: 10
        }, {
          name: '15-08-2022',
          y: 0
        }]
      }],
      colors:
      ['#63b598', '#008eff','#f1a0ff', '#7260d8', '#fce877','#84a3ff', '#b5e0d3', '#e535fc','#7d9ff7', '#513d98', 
       '#ffe2f8', '#00a0da', '#ffb380','#f697c1', '#4ca2f9', '#ffa2bc', '#96e591', '#f1ae16', '#2f7b99',
        '#b259ab', '#ff8473', '#0086b3', '#00861f', '#00696c', '#d36647',  '#c6f5e4', '#e7dbce', '#ccfeff','#f5f3e9', '#f0f7f7'
      ],
    });
  }

  /* Today's Birthday List */
  getAllEmployeesBirthDayToday(){
    this.employeeService.getAllEmployeesBirthDayToday().pipe(first()).subscribe((response:any) => {
      if (response.serviceStatus == "Success") {
        this.birthdayList =  response.serviceResponse;
        console.log("birthdayList : ", this.birthdayList);
      } else {
        this.compOffApplicationCount =  0;
        console.error(response.serviceResponse);
      }
    });
  }


  /* Quick Links */
  showApplyLeaveForm(){
    this.router.navigate(['/user-leaves'],
    { queryParams: {tabName: 'leave-tab'}, queryParamsHandling: ''});
  }

  showApplyCompOffForm(){
    this.router.navigate(['/user-leaves'],
    { queryParams: {tabName: 'compOff-tab'}, queryParamsHandling: ''});
  }

  showApplyTimesheetForm(){
    this.router.navigate(['/user-timesheet'],
    { queryParams: {tabName: 'my-timesheet-tab'}, queryParamsHandling: ''});
  }

  showHolidayList(){
    this.router.navigate(['/user-leaves'],
    { queryParams: {tabName: 'holidays-tab'}, queryParamsHandling: ''});
  }

  //export to excel

  exportToExcelForLeave() {
    this.excelName = 'leaveApplication.xlsx';
    	
        const onlySpecificDataArr: Partial<Leave>[] = this.leaveApplicationList.map(	
          x => ({	
              leaveType: x.leaveType,
              fromDate: x.fromDate,
              toDate: x.toDate,
              noOfDays: x.noOfDays,
              status: x.status,
              createdByName: x.createdByName,
              createdOn: x.createdOn,
              reason: x.reason	
          })	
        )	
        this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr,this.excelName)	
      }

  exportToExcelForCompOff() {
    this.excelName = 'leaveApplication.xlsx';
    	
        const onlySpecificDataArr: Partial<Leave>[] = this.allCompOffApplications.map(	
          x => ({	
            createdByName: x.createdByName,
            compOffReasons: x.compOffReasons,
            fromDate: x.fromDate,
            toDate: x.toDate,
            noOfDays: x.noOfDays,
            description: x.description,
            status: x.status
          })	
        )	
        this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr,this.excelName)	
      }

  //pagination 

  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  // modals
  openReqMod(template: TemplateRef<any>) {
    this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }
}
