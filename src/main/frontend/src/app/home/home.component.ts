import { AfterViewInit, Component, OnInit, SecurityContext, TemplateRef, ViewChild } from '@angular/core';
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
import { NotificationMessage } from '../models/notification';
import { NotificationService } from '../services/notification.service';
import * as moment from 'moment';
import { CalendarComponent } from '../helpers/calendar/calendar.component';
import { BodyComponent } from '../body/body.component';
import { Feature } from '../models/feature';
import { ValidationService } from '../services/validation.service';
import { LogService } from '../services/log.service';
import { Log } from '../models/log';
import * as CryptoJS from 'crypto-js';
import { Employee } from '../models/employee';
@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit, AfterViewInit {

    data:string;
  //modal 
  alertMessage:any;
  modalRef: BsModalRef = new BsModalRef();
  

  feature="Home";
  currentUser:User;
  userMapping:any = {};
  log:Log;

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
  rejectedLeavesList:any[] = [];
  approvedLeavesList:any[] = [];
  pendingLeavesList:any[] = [];

  notificationObj: NotificationMessage = new NotificationMessage();
  timesheetDetails: any[] = [];

  items = 10;
  bulkApprove:any =[];
  bulkReject:any = [];
  isSelectAll:boolean = false;
  bulkLeaveApprove:any =[];
  bulkLeaveReject:any = [];

  leaveObj = new Leave();
  
  @ViewChild("thisMonthCal") 
  private thisMonthCalendar:CalendarComponent;
  @ViewChild("lastMonthCal")
  private lastMonthCalendar:CalendarComponent;

  @ViewChild('updateInfo')
  private updateInfoTempRef:TemplateRef<any>;

  // TOP BAR
  @ViewChild("change_password")
  changePasswordTemplate: TemplateRef<any>;

  fieldTextType: boolean = false;
  fieldTextTypePassword: boolean = false;
  fieldTextTypeOldPass: boolean = false;
  isError:boolean=false;
  oldPasswordValid:boolean = false;

  password:any;
  userNewPass:any;
  newpassword:any;
  errorMsg:any;
  empId:any;
  user:User = new User();
  
  leaveTypes:Leave[] = [];
  leaveBucketDetails : any[] = [];

  // stop modal to close
  config = {
    backdrop: true,
    ignoreBackdropClick: true,
    keyboard  : false
  };
  
  profileCompletedPercentage:any = 0;

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
    private notificationService: NotificationService,
    private bodyComponent: BodyComponent,
    public validationService: ValidationService,
    private logService:LogService
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    this.logService.log.subscribe(x => {
      this.log = x;
      this.log.tabName = this.feature;
      this.log.featureName = this.feature;
    });
   }

  ngOnInit(): void {
    // this.getEmployeeProfileCompletion();
    this.logService.updateLogInfo(this.log);
    // Dynamic Subfeature Flags 
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    console.log(this.feature, " : ", this.userMapping);
    
    this.getAllNotifications();
    this.getAllLeaveTypesByLeavePolicies(this.currentUser);
    if(this.userMapping.view_event_photos) this.getAllEventPhotos();
    if(this.userMapping.view_birthday_list) this.getAllEmployeesBirthDayToday();
    if(this.userMapping.view_all_team_requests){
      this.countAllMyTeamsPendingLeaveApplicationsByManagerId();
      this.countPendingCompOffRequestsByManagerId();
      this.countMyReporteesTimesheetRequests();
    }
    if(this.userMapping.view_my_leave_details){
      this.getMyLeaveBalancesByEmpId();
      this.countMyApprovedLeaveApplicationsByLeaveType();
      this.countMyPendingLeaveApplicationsByLeaveType();
      this.countMyRejectedLeaveApplicationsByLeaveType();
    }
    if(this.userMapping.view_timesheet_display) this.getTimesheetsForHomePageByEmpId('Last 7 Days');


    if(this.currentUser.isNew == "true"){
      this.bodyComponent.openChangePasswordOnFirstTimeLoggin();
    }
  }

  ngAfterViewInit(): void {    
    if(this.currentUser.isNew == "false" && this.currentUser.isUserInfoUpdated == false && this.currentUser.updateFormCounter == 0){
      this.openUpdateInfo(this.updateInfoTempRef);
      this.currentUser.updateFormCounter = 1;
    }

  }

  reset(){
    let leaveObj = new Leave();
    leaveObj.isSelected = false
    this.isSelectAll = false;
    
  }

  // Leave Applications
  getAllMyTeamsPendingLeaveApplicationsByManagerId(){
    this.data = ''
    this.leaveApplicationList = []
    this.bulkLeaveApprove = []
    this.bulkLeaveReject = []
    this.isSelectAll = false

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
    this.cancelRequest();
    let leaveObj = new Leave();
    // 1 = pending , 2 = Approved , 3= Rejected
    leaveApplication.leaveStatusId = updatedLeaveStatusId;
    leaveApplication.leaveStatusUpdatedBy = this.currentUser.empId
    leaveObj.email = leaveApplication.email
    leaveObj.rejectReason = leaveApplication.rejectReason?.trim();
    console.log("   leaveObj.email   ",leaveObj.email);
    
    
    
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

   // single leave reject modal
   onSingleReject(template: TemplateRef<any> , ){
    this.leaveObj.rejectReason = this.leaveObj.rejectReason?.trim();
    if(!this.validationService.validateActivityTimesheetDiscription(this.leaveObj.rejectReason)){
      this.alertMessage = "Please enter valid reason !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    this.onUpdateLeaveStatus(template, this.leaveObj,3);
  }

  // openLeaveRejectModal
  openLeaveRejectModal(template: TemplateRef<any>, leave: any){
    this.cancelRequest();
    this.leaveObj = leave
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }



  //comOff Applications
  getPendingCompOffRequestsByManagerId(){
    this.data = ''
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
    this.cancelRequest();
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
    this.data = ''
    this.bulkApprove = []	
    this.bulkReject = []	
    this.allTeamTimesheetRequests = [];	
   this.isSelectAll = false

    let timesheetObj = new Timesheet();
    timesheetObj.managerId = this.currentUser.empId;
    timesheetObj.status = "Pending";
    this.timesheetService.getMyReporteesTimesheetRequests(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allTeamTimesheetRequests = response.serviceResponse;
        for(let x of this.allTeamTimesheetRequests){
          x.employeementId = "A-".concat(x.employeementId);
        }
        console.log("allTeamTimesheetRequests :", this.allTeamTimesheetRequests);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  /* Approve / Reject Timesheet requests */
  updateTimesheetRequestById(template: TemplateRef<any>, timesheet:Timesheet, status:any){
    this.cancelRequest();
    let timesheetObj = new Timesheet();
    timesheetObj.timesheetId = timesheet.timesheetId;
    timesheetObj.email = timesheet.email;
    timesheetObj.rejectReason = timesheet.rejectReason?.trim();
    timesheetObj.employeementId = timesheet.employeementId.substring(2);
    timesheetObj.employeeName = timesheet.employeeName;
    timesheetObj.status = status;
    timesheetObj.timesheetStatusUpdatedBy = this.currentUser.empId;

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

  
  rejectTimesheetRequest(template: TemplateRef<any> , ){
    this.timesheetObj.rejectReason = this.timesheetObj.rejectReason?.trim()
    if(!this.validationService.validateActivityTimesheetDiscription(this.timesheetObj.rejectReason)){
      this.alertMessage = "Please enter valid reason !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    this.updateTimesheetRequestById(template, this.timesheetObj,'Rejected');
  }

  opnenRejectTimesheet(template: TemplateRef<any>, timesheet: any){
    this.cancelRequest();
    this.timesheetObj = timesheet;
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
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
  getAllLeaveTypesByLeavePolicies(userObj:User){
    this.leaveTypes = [];

    let leaveObj = new Leave();
    leaveObj.employmentStatus = userObj.employmentstatus;
    leaveObj.gender = userObj.gender;

    this.leaveService.getAllLeaveTypesByLeavePolicies(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveTypes = response.serviceResponse;
        console.log("leaveTypes : ", this.leaveTypes);
        this.leaveBucketDetails = this.leaveTypes.map((leave:Leave) => {
          let leaveObj = new Leave();
          leaveObj.leaveTypeMasterId = leave.leaveTypeMasterId;
          leaveObj.leaveType = leave.leaveType;
          leaveObj.leaveTypeCode = leave.leaveTypeCode;

          return leaveObj;
        });

        console.log("leaveBucketDetails : ", this.leaveBucketDetails);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

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
        
        if(balanceChartData && balanceChartData.length != 0){
          balanceChartData.forEach(data => {
            let leaveDetail =  this.leaveBucketDetails.find((leave:Leave) => leave.leaveTypeCode == data.name);
             
            if(leaveDetail){ 
               leaveDetail.balance = (data.y) ? data.y : 0;
             }
         }); 
        }
        
        // if(checkData && checkData.length != 0){
        //   this.renderLeaveChart('Leave Bucket', 'leaveBucketChart', balanceChartData, 'Leaves');
        // }else{
        //   this.renderPlaceholderChart('Leave Bucket', 'leaveBucketChart', 'zero Leave Balance');
        // }
      } else {
        console.error(response.serviceResponse);
        // this.renderPlaceholderChart('Leave Bucket', 'leaveBucketChart', 'No Data to Display');
      }
    });
  }

  countMyRejectedLeaveApplicationsByLeaveType(){
    this.rejectedLeavesList = [];

    let leaveObj = new Leave();
    leaveObj.empId = this.currentUser.empId;
    this.leaveService.countMyRejectedLeaveApplicationsByLeaveType(leaveObj).pipe(first()).subscribe((response : any) =>{

      if(response.serviceStatus == 'Success') {
        this.rejectedLeavesList = response.serviceResponse;
        console.log("Rejected Leaves : ", this.rejectedLeavesList);
        let rejecetdChartData = this.rejectedLeavesList.map(leaveType =>{
          let data = {
            name : leaveType.leaveTypeCode,
            y : leaveType.applicationCount
          }
          return data;
        }).filter(data => data != undefined);
        console.log(" RejecetdChartData : ", rejecetdChartData);
        let checkData = rejecetdChartData.filter(data => data.y !=0);
        console.log(" chcekData  : ",checkData)
        if(checkData && checkData.length !=0){
          checkData.forEach(data => {
            let leaveDetail = this.leaveBucketDetails.find((leave:Leave)=> leave.leaveTypeCode == data.name);
            if(leaveDetail){
              leaveDetail.rejectedApplicationsCount = data.y ;
              console.log("    ::   ",data.y);
              console.log(leaveDetail.rejectedApplicationsCount )
            }
          });
        } 
      }else {
        console.error(response.serviceResponse);
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
        
        if(checkData && checkData.length != 0){
          checkData.forEach(data => {
            let leaveDetail =  this.leaveBucketDetails.find((leave:Leave) => leave.leaveTypeCode == data.name);
             if(leaveDetail){
               leaveDetail.approvedApplicationsCount = data.y
             }
         }); 
        }

        // if(checkData && checkData.length != 0){
        //   this.renderLeaveChart('Leave Approved', 'leaveApprovedChart', approvedChartData, 'Leave Applications');
        // }else{
        //   this.renderPlaceholderChart('Leave Approved', 'leaveApprovedChart', 'zero Leave Applications');
        // }
      } else {
        console.error(response.serviceResponse);
        // this.renderPlaceholderChart('Leave Approved', 'leaveApprovedChart', 'No Data to Display');
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
        
        if(checkData && checkData.length != 0){
          checkData.forEach(data => {
            let leaveDetail =  this.leaveBucketDetails.find((leave:Leave) => leave.leaveTypeCode == data.name);
             if(leaveDetail){
               leaveDetail.pendingApplicationsCount = data.y
             }
         }); 
        }

        // if(checkData && checkData.length != 0){
        //   this.renderLeaveChart('Pending Leave', 'leaveRequestChart', pendingChartData, 'Leaves Applications');
        // }else{
        //   this.renderPlaceholderChart('Pending Leave', 'leaveRequestChart', 'zero Leave Applications');
        // }

      } else {
        console.error(response.serviceResponse);
        // this.renderPlaceholderChart('Pending Leave', 'leaveRequestChart', 'No Data to Display');
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
  renderTimesheetChart(chartName:any, chartId:any, chartData:any, labelName:any) {
    let chartTitle = document.getElementById('timesheet-title');
    chartTitle.innerText = chartName;

    HighCharts.chart(chartId, {
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
      colors:
      ['#63b598', '#008eff','#f1a0ff', '#7260d8', '#fce877','#84a3ff', '#b5e0d3', '#e535fc','#7d9ff7', '#513d98', 
       '#ffe2f8', '#00a0da', '#ffb380','#f697c1', '#4ca2f9', '#ffa2bc', '#96e591', '#f1ae16', '#2f7b99',
        '#b259ab', '#ff8473', '#0086b3', '#00861f', '#00696c', '#d36647',  '#c6f5e4', '#e7dbce', '#ccfeff','#f5f3e9', '#f0f7f7'
      ],
    });
  }

  /* Notification */
  getAllNotifications(){
    this.eventImages = [];
    this.notificationService.getAllNotifications().pipe(first()).subscribe((response:any) => {
      if (response.serviceStatus == "Success") {
        let notificationList:any[] =  response.serviceResponse;
        console.log("notificationList : ", notificationList);
        if(notificationList) this.notificationObj = notificationList[0]; 
      } else {
        console.error(response.serviceResponse);
      }
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
    // document.getElementById('eventPhotosCarousel').style.display = 'none';

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

  /* Timesheet Details*/
  dateCompare(a, b){
    const dateFormat = 'YYYY-MM-DD';
    return (moment(new Date(a.date)).format(dateFormat) < moment(new Date(b.date)).format(dateFormat))? -1 : 1;
  }

  getWeekDay(date:any): string{
    let weekDay = '';
    let day = new Date(date).getDay();

    switch (day) {
      case 0: {
        weekDay = 'Sunday';
        break;
      }
      case 1: {
        weekDay = 'Monday';
        break;
      }
      case 2: {
        weekDay = 'Tuesday'; 
        break;
      }
      case 3: {
        weekDay = 'Wednesday';
        break;
      }
      case 4: {
        weekDay = 'Thursday'; 
        break;
      }
      case 5: {
        weekDay = 'Friday';
        break;
      }
      case 6: {
        weekDay = 'Saturday'; 
        break;
      }

      default: {
        weekDay = '';
        break;
      }
    } 
    return weekDay;
  }

  getTimesheetsForHomePageByEmpId(dateRange:any){
    this.timesheetDetails = [];
    const TOTAL_WORKING_HOURS_IN_DAY = 8;
    const currentDate = new Date();
    const dateFormat = 'YYYY-MM-DD';
    let fromDate:any;
    let toDate:any;
    let totaltimesheetDaysCount = 0;
    let filledTimesheetDetails = []

    let timesheetObj = new Timesheet();
    timesheetObj.empId = this.currentUser.empId;
   
    if(dateRange == 'Last 7 Days'){
      totaltimesheetDaysCount = 7;
      const DAY_IN_MS = 24 * 60 * 60 * 1000;
      fromDate = new Date(currentDate.getTime() - (1 * DAY_IN_MS));
      toDate = new Date(currentDate.getTime() - (7 * DAY_IN_MS));

      console.log(`Last 7 Days : ${moment(fromDate).format(dateFormat)} -- ${moment(toDate).format(dateFormat)}`);
      timesheetObj.startDate = moment(toDate).format(dateFormat);
      timesheetObj.endDate = moment(fromDate).format(dateFormat);
    }else if(dateRange == 'This Month'){
      totaltimesheetDaysCount = moment(`${currentDate.getFullYear()}-${currentDate.getMonth()}`, "YYYY-MM").daysInMonth()
      fromDate = new Date(currentDate.getFullYear() , currentDate.getMonth(), 1);
      toDate = new Date(currentDate.getFullYear() , currentDate.getMonth() +1, 0);

      console.log(`This Month : ${moment(fromDate).format(dateFormat)} -- ${moment(toDate).format(dateFormat)}`);
      timesheetObj.startDate = moment(fromDate).format(dateFormat);
      timesheetObj.endDate = moment(toDate).format(dateFormat);
    }else if(dateRange == 'Last Month'){
      totaltimesheetDaysCount = moment(`${currentDate.getFullYear()}-${currentDate.getMonth()-1}`, "YYYY-MM").daysInMonth()

      fromDate = new Date(currentDate.getFullYear() , currentDate.getMonth() -1, 1);
      toDate = new Date(currentDate.getFullYear() , currentDate.getMonth(), 0);
    
      console.log(`Last Month : ${moment(fromDate).format(dateFormat)} -- ${moment(toDate).format(dateFormat)}`);
      timesheetObj.startDate = moment(fromDate).format(dateFormat);
      timesheetObj.endDate = moment(toDate).format(dateFormat);
    }

    this.timesheetService.getTimesheetsForHomePageByEmpId(timesheetObj).pipe(first()).subscribe((response:any) => {
      if (response.serviceStatus == "Success") {
        filledTimesheetDetails =  response.serviceResponse;
        console.log("filledTimesheetDetails : ", filledTimesheetDetails);
      } else {
        console.error(response.serviceResponse);
      }

      let pendingCount = 0;
      let approvedCount = 0;
      let rejectedCount = 0;
      let notFilledCount = 0;

      for (let date = moment(timesheetObj.startDate); date.isSameOrBefore(timesheetObj.endDate); date.add(1, 'days')) {
        let newTimesheetObj = new Timesheet();
        newTimesheetObj.date = moment(date).format(dateFormat);

        if (newTimesheetObj.date) {
          let checkedTimesheet = filledTimesheetDetails.find(timesheet => timesheet.date == newTimesheetObj.date);

          if (checkedTimesheet) {
            newTimesheetObj = checkedTimesheet;
            newTimesheetObj.totalWorkingHoursPercentage = (newTimesheetObj.totalWorkingHours / TOTAL_WORKING_HOURS_IN_DAY) * 100 + "%";

            // For Chart Data 
            if (newTimesheetObj.status == "Pending") pendingCount++;
            else if (newTimesheetObj.status == "Approved") approvedCount++;
            else if (newTimesheetObj.status == "Rejected") rejectedCount++;
          } else {
            newTimesheetObj.totalWorkingHoursPercentage = "0%";
            newTimesheetObj.status = "Not Filled";
            newTimesheetObj.dayType = "Not Filled";
            newTimesheetObj.weekDayName = this.getWeekDay(newTimesheetObj.date);
            notFilledCount++;
          }
        }
        this.timesheetDetails.push(newTimesheetObj);
      }

      console.log("timesheetDetails : ", this.timesheetDetails);
      this.timesheetDetails.sort(this.dateCompare);

      let timesheetChartData = [{
        name: "Pending",
        y: pendingCount
      },
      {
        name: "Approved",
        y: approvedCount
      },
      {
        name: "Not Filled",
        y: notFilledCount
      },
      {
        name: "Rejected",
        y: rejectedCount
      }];

      this.renderTimesheetChart(`${dateRange} Timesheet`, 'totalEODChart', timesheetChartData, 'Timesheet(s)');
      if (dateRange == 'This Month')
        this.thisMonthCalendar.addTimesheetDetails();
      else if (dateRange == 'Last Month')
        this.lastMonthCalendar.addTimesheetDetails();
    });
  }


  //Employee Info Update
  openUpdateInfo(template: TemplateRef<any>) {
    this.modalRef = this.modalService.show(template, { class: 'modal-xl'});
  }

  onDocSubmit(){
    this.cancelRequest();
  } 

  // Employee Proile Completed Percentage 
  getEmployeeProfileCompletion(){
    this.profileCompletedPercentage = 0;

    let employee = new Employee();
    employee.empId = this.currentUser.empId;
    this.employeeService.getEmployeeProfileCompletion(employee).pipe(first()).subscribe((response:any) => {
      if (response.serviceStatus == "Success") {
        let employeeObj = response.serviceResponse;
        this.profileCompletedPercentage = Math.ceil(employeeObj.profileCompletedPercent)+ "%" ;
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  //export to excel

  exportToExcelForLeave() {
    this.excelName = 'leaveApplication.xlsx';
    	
        const onlySpecificDataArr:any = this.leaveApplicationList.map(	
          x => ({	
              "Name": x.employeeName,
              "Leave Type": x.leaveType,
              "From Date": x.fromDate,
              "To Date": x.toDate,
              "Duration": x.noOfDays,
              "Status": x.status,
              "Applied By": x.createdByName,
              "Applied On": x.createdOn,
              "Reason": x.reason	
          })	
        )	
        this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr,this.excelName)	
      }

  exportToExcelForCompOff() {
    this.excelName = 'compOffApplication.xlsx';
    	
        const onlySpecificDataArr: any = this.allCompOffApplications.map(	
          x => ({	
            "Applied By": x.createdByName,
            "Applied For": x.compOffReasons,
            "From Date": x.fromDate,
            "To Date": x.toDate,
            "Duration": x.noOfDays,
            "Description": x.description,
            "Status": x.status
          })	
        )	
        this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr,this.excelName)	
      }

  exportToExcelForTimesheet() {
    this.excelName = 'AllTeamTimeSheetRequest.xlsx';

    const onlySpecificDataArr:any = this.allTeamTimesheetRequests.map(
      x => ({
        "Employee Id": x.employeementId,
        "Name": x.employeeName,
        "Date": x.date,
        "Day Type": x.dayType,
        "Timesheet Details": x.description,
        "Status": x.status
      })
    )
    this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
  }

  //pagination 

  page = 1;
  handlePageChange(event) {
    this.page = event;
    this.bulkApprove = []
    this.bulkReject = []
    this.isSelectAll = false
    this.bulkLeaveApprove = []
    this.bulkLeaveReject = []
  }

  // modals
  openTimesheetDetailsModal(template: TemplateRef<any>, timesheetObj:Timesheet){
    this.cancelRequest();
    
    this.timesheetObj = new Timesheet();
    this.timesheetObj = timesheetObj;
    this.getAllMyActivitiesByTimesheetId(this.timesheetObj);
    this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
 }

  openNotificationMod(template: TemplateRef<any>) {
    this.modalRef = this.modalService.show(template, { class: 'modal-md' });
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

  selectAll(event){
    this.bulkApprove = [];
    this.bulkReject = [];
    
    const checkboxes = document.querySelectorAll('.timesheet-req-checkbox');
   
    checkboxes.forEach((checkbox:any) =>{
      console.log("checkbox : ", checkbox);
      let checkboxIndex = checkbox.getAttribute('id');
      let checkedTimesheet = this.allTeamTimesheetRequests.find((_timesheet, index) => index == checkboxIndex);

      if (event.target.checked) {
        checkbox.checked = true;
        checkbox.classList.add('checked');
        this.bulkApprove.push(checkedTimesheet);
        this.bulkReject.push(checkedTimesheet);
      } else {
        checkbox.checked = false;
        checkbox.classList.remove('checked');
        this.bulkApprove.forEach((timesheet, index) => {
          if (timesheet == checkedTimesheet) this.bulkApprove.splice(index, 1);
        });
        this.bulkReject.forEach((timesheet, index) => {
          if (timesheet == checkedTimesheet) this.bulkReject.splice(index, 1);
        });
      }
    });
    const leaveCheckboxes = document.querySelectorAll('.homeLeave-req-checkbox');
    leaveCheckboxes.forEach((leaveCheck :any)=>{
      console.log("Check in leave home ",leaveCheck);
      let leaveCheckboxIndex = leaveCheck.getAttribute('id');
      let checkedLeaveApplication = this.leaveApplicationList.find((_leave , index)=> index == leaveCheckboxIndex);

      if(event.target.checked){
        leaveCheck.checked = true;
        leaveCheck.classList.add('checked');
        this.bulkLeaveApprove.push(checkedLeaveApplication);
        this.bulkLeaveReject.push(checkedLeaveApplication);
      }else {
        leaveCheck.checked = false;
        leaveCheck.classList.remove('checked');
        this.bulkLeaveApprove.forEach((leave , index)=>{
          if(leave == checkedLeaveApplication) this.bulkLeaveApprove.splice(index,1);
        });
        this.bulkLeaveReject.forEach((leave , index)=>{
          if(leave == checkedLeaveApplication) this.bulkLeaveReject.splice(index,1);
        });
      }
      
    })
  }

  select(timesheetObj, event) {
    
    console.log("clicked on : ", timesheetObj);

    if (event.target.checked) {
      event.target.classList.add('checked');
      this.bulkApprove.push(timesheetObj);
      this.bulkReject.push(timesheetObj);
    } else {
      event.target.classList.remove('checked');
      const checkboxes = document.querySelectorAll('.timesheet-req-checkbox.checked');
      if(checkboxes.length !== this.items) this.isSelectAll = false; 
      console.log("Checkboxes.length ",checkboxes.length)
      console.log(" items ",this.items)
      this.bulkApprove.forEach((timesheet, index) => {
        if (timesheet == timesheetObj) this.bulkApprove.splice(index, 1);
      });
      this.bulkReject.forEach((timesheet, index) => {
        if (timesheet == timesheetObj) this.bulkReject.splice(index, 1);
      });
    }
    console.log("Updated Bulk List : ",  this.bulkApprove);
  }




  onSelect(leaveObj, event) {
    
    console.log("clicked on : ", leaveObj);

    if (event.target.checked) {
      event.target.classList.add('checked');
      this.bulkLeaveApprove.push(leaveObj);
      this.bulkLeaveReject.push(leaveObj);
    } else {
      event.target.classList.remove('checked');
      const leaveCheckboxes = document.querySelectorAll('.homeLeave-req-checkbox.checked');
      if(leaveCheckboxes.length !== this.items) this.isSelectAll = false; 
      console.log("checkbox.length ",leaveCheckboxes.length)
      this.bulkLeaveApprove.forEach((timesheet, index) => {
        if (timesheet == leaveObj) this.bulkLeaveApprove.splice(index, 1);
      });
      this.bulkLeaveReject.forEach((timesheet, index) => {
        if (timesheet == leaveObj) this.bulkLeaveReject.splice(index, 1);
      });
    }
    console.log("Updated Bulk List : ",  this.bulkLeaveApprove);
  }
  
onBulkApproval(template:TemplateRef<any>){
  console.log("Updated Bulk List : ",  this.bulkApprove);
  let timesheetObj = new Timesheet();
  timesheetObj.bulkApprovedList =  this.bulkApprove;
  timesheetObj.updatedBy = this.currentUser.empId;
  timesheetObj.status = "Approved"
  console.log("For Bulk Update : ", timesheetObj);
  timesheetObj.bulkApprovedList.forEach((x)=>{
    x.employeementId = x.employeementId.substring(2);
  })
  this.timesheetService.bulkApproveTimesheetRequest(timesheetObj).pipe(first()).subscribe((response: any) => {
    if (response.serviceStatus == "Success") {
      this.openAlertMod(template , "All Selected Timesheets Approved Successfully ");
      this.countMyReporteesTimesheetRequests();
      this.getMyReporteesTimesheetRequests();
      this.bulkApprove = [];
      this.bulkReject = [];
    } else {
    console.error(response.serviceResponse)
    }
  });
  
}

onBulkRejectTimesheet(template: TemplateRef<any>){
  this.timesheetObj.rejectReason = this.timesheetObj.rejectReason?.trim();

  if(!this.validationService.validateActivityTimesheetDiscription(this.timesheetObj.rejectReason)){
    this.alertMessage = "Please enter Valid Reason !!"
    this.openAlertMod(template, this.alertMessage);
    return false;
  }
  console.log("Updated Bulk List : ",  this.bulkReject);
  let timesheetObj = new Timesheet();
  timesheetObj.bulkRejectList =  this.bulkReject;
  timesheetObj.updatedBy = this.currentUser.empId;
  timesheetObj.rejectReason = this.timesheetObj.rejectReason?.trim();
  console.log(" timesheet reason :  ", timesheetObj.rejectReason);
  timesheetObj.status = "Rejected"
  console.log("For Bulk Update : ", timesheetObj);
  timesheetObj.bulkRejectList.forEach((item)=>{
    item.employeementId = item.employeementId.substring(2);
  })
  this.timesheetService.bulkRejectTimesheetRequest(timesheetObj).pipe(first()).subscribe((response: any) => {
    if (response.serviceStatus == "Success") {
      this.openAlertMod(template , "All Selected Timesheets Rejected Successfully "); 
      this.bulkApprove = [];
      this.bulkReject = [];
      this.timesheetApplicationCount ;
      this.countMyReporteesTimesheetRequests();
      this.getMyReporteesTimesheetRequests();
    } else {
    console.error(response.serviceResponse)
    }
  });
  
}


// homeLeave-req-checkbox




onBulkLeaveApproval(template:TemplateRef<any>){
  console.log("Updated Bulk List : ",  this.bulkLeaveApprove);
  let leaveObj = new Leave();
  leaveObj.bulkLeaveApprovedList =  this.bulkLeaveApprove;
  leaveObj.leaveStatusUpdatedBy = this.currentUser.empId;
  leaveObj.leaveStatusId = 2;
  
  this.leaveService.bulkApproveLeaveRequest(leaveObj).pipe(first()).subscribe((response: any) => {
    if (response.serviceStatus == "Success") {
      this.openAlertMod(template , "All Selected Leaves Approved Successfully ");
  
       this.getAllMyTeamsPendingLeaveApplicationsByManagerId()
      this.countAllMyTeamsPendingLeaveApplicationsByManagerId()
     
      this.bulkLeaveApprove = [];
      this.bulkLeaveReject = [];
    } else {
    console.error(response.serviceResponse)
    }
  });
  
}

bulkRejectLeave(template: TemplateRef<any>){
  this.leaveObj.rejectReason = this.leaveObj.rejectReason?.trim();
  if(!this.validationService.validateActivityTimesheetDiscription(this.leaveObj.rejectReason)){
    this.alertMessage = "please enter valid reason !!"
    this.openAlertMod(template, this.alertMessage);
    return false;
  }
      
  let leaveObj = new Leave();
  leaveObj.bulkLeaveRejectList =  this.bulkLeaveReject;
  console.log(" ............................ ",leaveObj.bulkLeaveRejectList)
  leaveObj.leaveStatusUpdatedBy = this.currentUser.empId;
  leaveObj.leaveStatusId = 3
  leaveObj.rejectReason = this.leaveObj.rejectReason?.trim();
  leaveObj.bulkLeaveRejectList.forEach((y)=>{
    y.employeementId = y.employeementId;
  })
  
  this.leaveService.bulkRejectLeaveRequest(leaveObj).pipe(first()).subscribe((response: any) => {
    if (response.serviceStatus == "Success") {
      this.openAlertMod(template , "All Selected Leaves Rejected Successfully ");
      this.getAllMyTeamsPendingLeaveApplicationsByManagerId()
      this.countAllMyTeamsPendingLeaveApplicationsByManagerId()
      this.bulkLeaveApprove = [];
      this.bulkLeaveReject = [];

    } else {
    console.error(response.serviceResponse)
    }
  });
  
}


OnBulkReject(template: TemplateRef<any>){
  let timesheet = new Timesheet();
  this.cancelRequest();
  this.timesheetObj = timesheet;
  this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
}


OnBulkLeaveReject(template: TemplateRef<any>, leave){
  this.leaveObj.rejectReason = ''
  this.cancelRequest();
 this.leaveObj = leave
  this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
}


// TOP BAR
  userLogout() {

    let user = new User();
    user.empId = this.currentUser.empId;
    this.authenticationService.logoutUser(user).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.authenticationService.stopUserSessionCheck();
        console.log(response.serviceResponse);
        sessionStorage.removeItem('currentUser');
        // delete method call for cookies
        this.authenticationService.deleteCookies();
        this.authenticationService.setcurrentUserSubject(null);
        this.router.navigate(['/login']);
        location.reload();
      } else {
        if( response.serviceResponse == "Session already destroyed"){
          this.router.navigate(['/login']);
          setTimeout(location.reload, 1000);
        }
        console.error(response.serviceResponse);
      }
    });


  }

  toggleFieldTextType() {
    this.fieldTextType = !this.fieldTextType;
  }

  toggleFieldChangePassword() {
    this.fieldTextTypePassword = !this.fieldTextTypePassword;
  }

  toggleFieldTextTypeOldPass() {
    this.fieldTextTypeOldPass = !this.fieldTextTypeOldPass;
  }

  setEncryption(keys, value) {

    var key = CryptoJS.enc.Utf8.parse(keys);
    var iv = CryptoJS.enc.Utf8.parse(keys);

    var encrypted = CryptoJS.AES.encrypt(CryptoJS.enc.Utf8.parse(value.toString()), key,
      {
        keySize: 128 / 8,
        iv: iv,
        mode: CryptoJS.mode.CBC,
        padding: CryptoJS.pad.Pkcs7
      });

    return encrypted.toString();
  }

  passreset() {

    this.password = '';
    this.userNewPass = '';
    this.newpassword = '';
    this.errorMsg = '';
  }


  checkEmployeeOldPassword() {

    this.isError = false;
    this.errorMsg = '';

    this.user.empId = this.currentUser.empId;
    this.user.password = this.setEncryption("PkdtRsJidheGitvS", this.password);

    this.employeeService.checkEmployeeOldPassword(this.user).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.oldPasswordValid = true;
      } else {
        this.isError = true;
        this.errorMsg = response.serviceResponse;
        this.oldPasswordValid = false;
      }
    });
  }

  openChangePassword(changePasswordTemplate) {
    console.log(this.currentUser.isNew)
    if (this.currentUser.isNew == 'true') {
      this.modalRef = this.modalService.show(changePasswordTemplate, this.config);
    } else {
      this.modalRef = this.modalService.show(changePasswordTemplate);
    }
  }

  openChangePasswordOnFirstTimeLoggin() {
    this.openChangePassword(this.changePasswordTemplate);
  }


  updateEmployeePassword(template: TemplateRef<any>) {
    this.isError = false;
    this.errorMsg = '';

    if (!this.validationService.validateNullUndefinedEmptyString(this.password)) {
      this.isError = true;
      this.errorMsg = 'Please enter old Password!!';
      return;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(this.userNewPass)) {
      this.isError = true;
      this.errorMsg = 'Please enter new Password!!';
      return;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(this.newpassword)) {
      this.isError = true;
      this.errorMsg = 'Please enter Confirm password !!';
      return;
    }

    if (this.userNewPass != this.newpassword) {
      this.isError = true;
      this.errorMsg = 'Password did not match. Please try again... !!';
      this.userNewPass = '';
      this.newpassword = '';
      return;
    }

    if (!this.validationService.validateAlphaNumericSpecialCharacters(this.userNewPass) &&
      !this.validationService.validateAlphaNumericSpecialCharacters(this.newpassword)) {
      this.isError = true;
      this.errorMsg = 'Password should not be set  less than 8 characters. Only alphanumeric and @#$%!+*÷=/_-\'":;,()^{}~[] are allowed !!';
      return;
    }

    if (this.userNewPass == this.newpassword) {
      this.user.email = this.currentUser.email;
      this.user.password = this.setEncryption("PkdtRsJidheGitvS", this.password);
      this.user.newPassword = this.setEncryption("PkdtRsJidheGitvS", this.newpassword);

      this.employeeService.updateEmployeePassword(this.user).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.userLogout();
          this.openAlertMod(template, response.serviceResponse);
          if (this.currentUser.isNew == "true") {
            this.userLogout();
          }
          this.passreset();
        } else {
          this.isError = true;
          this.errorMsg = response.serviceResponse;
        }
      });
    } else {
      this.isError = true;
      this.errorMsg = 'Password and Confirm Password do not match !!';
      return;
    }
  }


}
