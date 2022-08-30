import { Component, OnInit, SecurityContext, TemplateRef, ViewChild } from '@angular/core';
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
import { ImageService } from '../services/image.service';
import { DomSanitizer } from '@angular/platform-browser';
import { Timesheet } from '../models/timesheet';
import { TimesheetService } from '../services/timesheet.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

    data:string;
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
  allTeamTimesheetRequests:any[] = [];
  timesheetObj:Timesheet = new Timesheet();

  //export excel
  excelName:any = '';

  birthdayList:any[] =[];
  eventImages:any[] = [];
  isImagesLoaded:boolean = false;
  
  leaveBalanceList:any[] = [];
  approvedLeavesList:any[] = [];
  pendingLeavesList:any[] = [];

  constructor(
    private modalService: BsModalService,
    private authenticationService : AuthenticationService,
    private leaveService : LeaveService,
    private exportExcelService: ExportExcelService,
    private router: Router,
    private employeeService: EmployeeService,
    private timesheetService : TimesheetService,
    private imageService: ImageService,
    private sanitizer: DomSanitizer,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
   }

  ngOnInit(): void {
    this.countAllMyTeamsPendingLeaveApplicationsByManagerId();
    this.countPendingCompOffRequestsByManagerId();
    this.countMyReporteesTimesheetRequests();

    this.renderPieChart();
    this.getAllEventPhotos();
    this.getAllEmployeesBirthDayToday();

    console.log("================= MY LEAVE DETAILS APIs =====================");
    
    this.getMyLeaveBalancesByEmpId();
    this.countMyApprovedLeaveApplicationsByLeaveType();
    this.countMyPendingLeaveApplicationsByLeaveType();

    // $('.carousel').carousel({
    //   interval: 2000,
    //   keyboard: true, 
    //   pause: "hover",
    //   ride: true,
    //   wrap: false,
    // }); 
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
    this.allTeamTimesheetRequests = []

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

  getMyReporteesTimesheetRequests(){
    this.allTeamTimesheetRequests = [];

    let timesheetObj = new Timesheet();
    timesheetObj.managerId = this.currentUser.empId;
    timesheetObj.status = "Pending";
    this.timesheetService.getMyReporteesTimesheetRequests(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allTeamTimesheetRequests = response.serviceResponse;
        console.log("allTeamTimesheetRequests :", this.allTeamTimesheetRequests);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  /* Approve / Reject Timesheet requests */
  updateTimesheetRequestById(template: TemplateRef<any>, timesheet:Timesheet, status:any){
    let timesheetObj = new Timesheet();
    timesheetObj.timesheetId = timesheet.timesheetId;
    timesheetObj.status = status;
    this.timesheetService.updateTimesheetRequestById(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.countMyReporteesTimesheetRequests();
        this.getMyReporteesTimesheetRequests();
        this.openAlertMod(template, response.serviceResponse);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  /* View TImesheet details */ 
  getAllMyActivitiesByTimesheetId(timesheet:any){
    this.timesheetObj.allTimesheetActivities = [];

    let timesheetObj = new Timesheet();
    timesheetObj.timesheetId = timesheet.timesheetId;
    this.timesheetService.getAllMyActivitiesByTimesheetId(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.timesheetObj.allTimesheetActivities = response.serviceResponse;
        console.log("timesheetObj.allTimesheetActivities :", this.timesheetObj.allTimesheetActivities);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  /* My Leave Details */
  getMyLeaveBalancesByEmpId(){
    this.leaveBalanceList = [];

    let leaveObj = new Leave();
    leaveObj.employeementId = this.currentUser.employeementId;
    this.leaveService.getMyLeaveBalancesByEmpId(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveBalanceList = response.serviceResponse;
        console.log("leaveBalanceList : ", this.leaveBalanceList);

        let balanceChartData = this.leaveBalanceList.map(leaveType => {
          if(leaveType.leaveTypeMasterId != null){
            let data = {
              name : leaveType.leaveTypeCode,
              y : leaveType.balance
            }

            return data;
          }
        }).filter(data => data != undefined);
        console.log("balanceChartData : ", balanceChartData);

        let checkData = balanceChartData.filter(data => data.y != 0);
        console.log("checkData :", checkData);
        
        if(checkData){
          this.renderLeaveChart('Leave Bucket', 'leaveBucketChart', balanceChartData, 'Leaves');
        }else{
          this.renderPlaceholderChart('Leave Bucket', 'leaveBucketChart', 'zero Leave Balance');
        }
      } else {
        console.error(response.serviceResponse);
        this.renderPlaceholderChart('Leave Bucket', 'leaveBucketChart', 'No Data to Display');
      }
    });
  }

  countMyApprovedLeaveApplicationsByLeaveType(){
    this.approvedLeavesList = [];

    let leaveObj = new Leave();
    leaveObj.empId = this.currentUser.empId;
    this.leaveService.countMyApprovedLeaveApplicationsByLeaveType(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.approvedLeavesList =  response.serviceResponse;
        console.log("approvedLeaves : ", this.approvedLeavesList);

        let approvedChartData = this.approvedLeavesList.map(leaveType => {
          let data = {
            name : leaveType.leaveTypeCode,
            y : leaveType.applicationCount
          }

          return data;
        }).filter(data => data != undefined);
        console.log("approvedChartData : ", approvedChartData);
        let checkData = approvedChartData.filter(data => data.y != 0);
        console.log("checkData :", checkData);
        
        if(checkData){
          this.renderLeaveChart('Leave Approved', 'leaveApprovedChart', approvedChartData, 'Leave Applications');
        }else{
          this.renderPlaceholderChart('Leave Approved', 'leaveApprovedChart', 'zero Leave Applications');
        }
      } else {
        console.error(response.serviceResponse);
        this.renderPlaceholderChart('Leave Approved', 'leaveApprovedChart', 'No Data to Display');
      }
    });
  }

  countMyPendingLeaveApplicationsByLeaveType(){
    this.pendingLeavesList = [];

    let leaveObj = new Leave();
    leaveObj.empId = this.currentUser.empId;
    this.leaveService.countMyPendingLeaveApplicationsByLeaveType(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.pendingLeavesList =  response.serviceResponse;
        console.log("pendingLeavesList : ", this.pendingLeavesList);

        let pendingChartData = this.pendingLeavesList.map(leaveType => {
          let data = {
            name : leaveType.leaveTypeCode,
            y : leaveType.applicationCount
          }

          return data;
        }).filter(data => data != undefined);
        console.log("pendingChartData : ", pendingChartData);
        let checkData = pendingChartData.filter(data => data.y != 0);
        console.log("checkData :", checkData);
        
        if(checkData){
          this.renderLeaveChart('Pending Leave Request', 'leaveRequestChart', pendingChartData, 'Leaves Applications');
        }else{
          this.renderPlaceholderChart('Pending Leave Request', 'leaveRequestChart', 'zero Leave Applications');
        }

      } else {
        console.error(response.serviceResponse);
        this.renderPlaceholderChart('Pending Leave Request', 'leaveRequestChart', 'No Data to Display');
      }
    });
  }

  renderLeaveChart(chartName:any, chartId:any, chartData:any, labelName:any){
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
        pointFormat: '{series.name}: <b>{point.y:.1f}</b>'
      },
      plotOptions: {
        pie: {
          borderWidth: 0,
          innerSize: '50%',
          allowPointSelect: true,
          cursor: 'pointer',
          dataLabels: {
            enabled: false,
            format: '<b>{point.name}</b>: {point.y:.1f}'
          }
        }
      },
      series: [{
        name: labelName,
        colorByPoint: true,
        type: undefined,
        data: chartData
      }],
      colors:
      ['#63b598', '#008eff','#f1a0ff', '#7260d8', '#fce877','#84a3ff', '#b5e0d3', '#e535fc','#7d9ff7', '#513d98', 
       '#ffe2f8', '#00a0da', '#ffb380','#f697c1', '#4ca2f9', '#ffa2bc', '#96e591', '#f1ae16', '#2f7b99',
        '#b259ab', '#ff8473', '#0086b3', '#00861f', '#00696c', '#d36647',  '#c6f5e4', '#e7dbce', '#ccfeff','#f5f3e9', '#f0f7f7'
      ],
    });
  }

  renderPlaceholderChart(chartName:any, chartId:any, errorMsg:any){
    HighCharts.chart(chartId, {
      credits: {
        enabled: false
      },
      chart: {
        plotBackgroundColor: null,
        plotBorderWidth: null,
        plotShadow: false,
        type: 'pie',
        events: {
          render() {
            let chart:any = this,
              x,
              y;
    
    
            //check if label exist after window resize
            if (chart.label) {
              chart.label.destroy();
            };
    
            y = (chart.clipBox.height*1.3);
            x = (chart.clipBox.width/2.5);
            chart.label = chart.renderer.text(errorMsg, x, y)
              .css({
                color: '#b0b0b0',
                fontSize: '10px'
              })
              .add();
          }
        }
      },
      title: {
        text: chartName
      },
      plotOptions: {
        series: {
          enableMouseTracking: false
        },
        pie: {
          borderWidth: 0,
          innerSize: '50%',
          allowPointSelect: true,
          cursor: 'pointer',
          dataLabels: {
            enabled: false,
            format: '<b>{point.name}</b>: {point.y:.1f}'
          }
        }
      },
      series: [{
        name: "error",
        colorByPoint: true,
        type: undefined,
        data: [{name: 'data', y: 1}]}],
      colors:
      ['#b0b0b0'],
    });
  }

  // Graphs 
  renderPieChart() {
    // Leave Bucket
    // HighCharts.chart('leaveBucketChart', {
    //   credits: {
    //     enabled: false
    //   },
    //   chart: {
    //     plotBackgroundColor: null,
    //     plotBorderWidth: null,
    //     plotShadow: false,
    //     type: 'pie'
    //   },
    //   title: {
    //     text: 'Leave Bucket'
    //   },
    //   tooltip: {
    //     pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
    //   },
    //   plotOptions: {
    //     pie: {
    //       borderWidth: 0,
    //       innerSize: '50%',
    //       allowPointSelect: true,
    //       cursor: 'pointer',
    //       dataLabels: {
    //         enabled: false,
    //         format: '<b>{point.name}</b>: {point.percentage:.1f} %'
    //       }
    //     }
    //   },
    //   series: [{
    //     name: 'Brands',
    //     colorByPoint: true,
    //     type: undefined,
    //     data: [{
    //       name: 'PL',
    //       y: 61.41,
    //     }, {
    //       name: 'CL',
    //       y: 27.74
    //     }, {
    //       name: 'LWP',
    //       y: 10.85
    //     }]
    //   }],
    //   colors:
    //   ['#63b598', '#008eff','#f1a0ff', '#7260d8', '#fce877','#84a3ff', '#b5e0d3', '#e535fc','#7d9ff7', '#513d98', 
    //    '#ffe2f8', '#00a0da', '#ffb380','#f697c1', '#4ca2f9', '#ffa2bc', '#96e591', '#f1ae16', '#2f7b99',
    //     '#b259ab', '#ff8473', '#0086b3', '#00861f', '#00696c', '#d36647',  '#c6f5e4', '#e7dbce', '#ccfeff','#f5f3e9', '#f0f7f7'
    //   ],
    // });

    // // Leave Approved 
    // HighCharts.chart('leaveApprovedChart', {
    //   credits: {
    //     enabled: false
    //   },
    //   chart: {
    //     plotBackgroundColor: null,
    //     plotBorderWidth: null,
    //     plotShadow: false,
    //     type: 'pie'
    //   },
    //   title: {
    //     text: 'Leave Approved'
    //   },
    //   tooltip: {
    //     pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
    //   },
    //   plotOptions: {
    //     pie: {
    //       borderWidth: 0,
    //       innerSize: '50%',
    //       allowPointSelect: true,
    //       cursor: 'pointer',
    //       dataLabels: {
    //         enabled: false,
    //         format: '<b>{point.name}</b>: {point.percentage:.1f} %'
    //       }
    //     }
    //   },
    //   series: [{
    //     name: 'Brands',
    //     colorByPoint: true,
    //     type: undefined,
    //     data: [{
    //       name: 'PL',
    //       y: 61.41,
    //     }, {
    //       name: 'CL',
    //       y: 27.74
    //     }, {
    //       name: 'LWP',
    //       y: 10.85
    //     }]
    //   }],
    //   colors:
    //   ['#63b598', '#008eff','#f1a0ff', '#7260d8', '#fce877','#84a3ff', '#b5e0d3', '#e535fc','#7d9ff7', '#513d98', 
    //    '#ffe2f8', '#00a0da', '#ffb380','#f697c1', '#4ca2f9', '#ffa2bc', '#96e591', '#f1ae16', '#2f7b99',
    //     '#b259ab', '#ff8473', '#0086b3', '#00861f', '#00696c', '#d36647',  '#c6f5e4', '#e7dbce', '#ccfeff','#f5f3e9', '#f0f7f7'
    //   ],
    // });

    // // Leave Request
    // HighCharts.chart('leaveRequestChart', {
    //   credits: {
    //     enabled: false
    //   },
    //   chart: {
    //     plotBackgroundColor: null,
    //     plotBorderWidth: null,
    //     plotShadow: false,
    //     type: 'pie'
    //   },
    //   title: {
    //     text: 'Leave Requests'
    //   },
    //   tooltip: {
    //     pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
    //   },
    //   plotOptions: {
    //     pie: {
    //       borderWidth: 0,
    //       innerSize: '50%',
    //       allowPointSelect: true,
    //       cursor: 'pointer',
    //       dataLabels: {
    //         enabled: false,
    //         format: '<b>{point.name}</b>: {point.percentage:.1f} %'
    //       }
    //     }
    //   },
    //   series: [{
    //     name: 'Brands',
    //     colorByPoint: true,
    //     type: undefined,
    //     data: [{
    //       name: 'PL',
    //       y: 61.41,
    //     }, {
    //       name: 'CL',
    //       y: 27.74
    //     }, {
    //       name: 'LWP',
    //       y: 10.85
    //     }]
    //   }],
    //   colors:
    //   ['#63b598', '#008eff','#f1a0ff', '#7260d8', '#fce877','#84a3ff', '#b5e0d3', '#e535fc','#7d9ff7', '#513d98', 
    //    '#ffe2f8', '#00a0da', '#ffb380','#f697c1', '#4ca2f9', '#ffa2bc', '#96e591', '#f1ae16', '#2f7b99',
    //     '#b259ab', '#ff8473', '#0086b3', '#00861f', '#00696c', '#d36647',  '#c6f5e4', '#e7dbce', '#ccfeff','#f5f3e9', '#f0f7f7'
    //   ],
    // });

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

  /* carousal Images */
  getAllEventPhotos(){
    this.eventImages = [];
    this.isImagesLoaded = false;
    document.getElementById('eventPhotosCarousel').style.display = 'none';

    this.imageService.getAllEventPhotos().pipe(first()).subscribe((response:any) => {
      if (response.serviceStatus == "Success") {
        this.eventImages =  response.serviceResponse;
        console.log("eventImages : ", this.eventImages);
        setTimeout(()=>{this.loadImages(this.eventImages);}, 1000)
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  loadImages(eventImages){
    eventImages.forEach((photo, index) =>{
      if(photo.imageBytes){
        let objectURL = 'data:image/*;base64,' + photo.imageBytes;
        let src: string = this.sanitizer.sanitize(SecurityContext.RESOURCE_URL, this.sanitizer.bypassSecurityTrustResourceUrl(objectURL));
        let carouselImg = document.getElementById(`carouselImg${index}`);
        carouselImg.setAttribute('src', src);
      }
    });
    this.isImagesLoaded = true;
    document.getElementById('eventPhotosCarousel').style.display = 'block';
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

  exportToExcelForTimesheet() {
    this.excelName = 'AllTeamTimeSheetRequest.xlsx';

    const onlySpecificDataArr: Partial<Timesheet>[] = this.allTeamTimesheetRequests.map(
      x => ({
        date: x.date,
        dayType: x.dayType,
        description: x.description,
        status: x.status
      })
    )
    this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
  }

  //pagination 

  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  // modals
  openTimesheetDetailsModal(template: TemplateRef<any>, timesheetObj:Timesheet){
    this.cancelRequest();
    
    this.timesheetObj = new Timesheet();
    this.timesheetObj = timesheetObj;
    this.getAllMyActivitiesByTimesheetId(this.timesheetObj);
    this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
 }

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
