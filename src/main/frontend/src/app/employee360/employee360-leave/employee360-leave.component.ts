import { ScrollStrategy } from '@angular/cdk/overlay';
import { DatePipe } from '@angular/common';
import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import * as HighCharts from 'highcharts';
import * as Highcharts from 'highcharts';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Breadcrumb } from 'src/app/models/breadcrumd';
import { Leave } from 'src/app/models/leave';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { BreadcrumbService } from 'src/app/services/breadcrumb.service';
import { Employee360Service } from 'src/app/services/employee360.service';
import { LogService } from 'src/app/services/log.service';
import { UtilityService } from 'src/app/services/utility.service';
import { Log } from '../../models/log';
import { LeaveService } from '../../services/leave.service';

import { ValidationService } from 'src/app/services/validation.service';

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
  leaveObj2: Leave = new Leave();
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
  formatedEmploymentID: any;
  leaveByMonth: Leave[] = [];
  Highcharts = Highcharts;
  leaveData: any = [];
  years: number[] = [];
  selectedYear: number;
  breadcrumbUrl:any[] = [];
  currentBreadcrumbList: any[] = [];
  currentUserr:any;
  managerIds:any[]=[];

  //Active Buttons
  activeButton: string = "Leave-Charts";
  activeCompOffButton: string = 'Requests'; 

  //boolean
  showActiveButton:boolean=false;
  showTable:boolean=false;
  back:boolean=false;
  approveReject:boolean=false;
  isLeaveRevokeRequest:boolean=false;
  showDropDown:boolean=false;

  //Leave 
  empId:any=0;
  managerId:any=0;
  teamViewLeaveHistoryList: any[] = [];
  leaveList: any[] = [];
  leaveApplicationList: any[] = [];

  //Comp-off
  allCompOffApplications: any[] = [];
  CompOffData: any[] = [];
  isCompOff: boolean = false;
  isCompOffRequest: boolean = true;
  isCompOffApplication: boolean = false;
  compOffManagerId:any[]=[];
  compOffStatus:string="";

  //Applied for Revoke
  reporteeLeaveRevokeApplicationList:any[] = [];


  //date set up 
  selectedOption:any = 1;
  startDate: any;
  endDate:any; 
  dateTimeRange: any = null;
  todayDate: Date = new Date();
  scrollStrategy: ScrollStrategy;
  formattedStartDate:any = '';
  formattedEndDate:any = '';
  
  //Modal
  alertMessage: any
  modalRef: BsModalRef = new BsModalRef();
  @ViewChild("alert_message") alertTemplate: TemplateRef<any>;

  //Dropdown filter
  year:any;
  
   
  constructor(
    private authenticationService: AuthenticationService,
    private leaveService: LeaveService,
    private logService: LogService,
    private router: Router,
    private utilityService: UtilityService,
    private employee360Service: Employee360Service,
    private breadcrumbService: BreadcrumbService,
    private modalService: BsModalService,
    private datePipe: DatePipe,
    public validationService:ValidationService,
  ) {
    this.breadcrumbService.currentBreadcrumb.subscribe(x => this.currentBreadcrumbList = x);
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
   }

  ngOnInit(): void {  
    const storedData = localStorage.getItem('employee360Data');
    const parsedData = storedData ? JSON.parse(storedData) : null;
      if(parsedData != null || parsedData != undefined ){
        this.employeeData =  parsedData;
      }else{
        this.employeeData = history.state.data;
      }
    let findbreadcrumbObject = this.currentBreadcrumbList.findIndex(x => x.title =="Leave");
        if (findbreadcrumbObject >= 0) {
          this.currentBreadcrumbList.splice(findbreadcrumbObject + 1);
          this.breadcrumbService.setBreadcrumbSubject(this.currentBreadcrumbList);
        }else{
          let breadcrumbObject = new Breadcrumb();
          breadcrumbObject.title = "Leave";
          breadcrumbObject.url = "/employee-360/leave";
          this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);
        }
    console.log("activeButton===>"+this.activeButton);
    
    this.currentUserr=sessionStorage.getItem('currentUser');
    if (this.currentUserr) {
      const currentUserData = JSON.parse(this.currentUserr);
      this.managerId = currentUserData.empId;
      console.log(this.managerId); 
    }
    this.empId=sessionStorage.getItem('empId');
    this.getAllLeaveTypesByLeavePolicies();
    this.generateLeaveChart();
    this.getLeaveDataPerMonthByEmpId();
    this.countMyApprovedLeaveApplicationsByLeaveType();
  }

  setActiveCompOffButton(button: string): void {
    this.activeCompOffButton = button;
    if (button === 'Requests') {
      const dataToSend = { ...this.employeeData, status: 'comp-off-requests' };
      this.employee360Service.changeEmployeeData(dataToSend);
      this.isCompOffApplication=false;
      this.isCompOffRequest=true;
      this.getPendingCompOffRequestsByManagerId();}
    if (button === 'Applications') {
      const dataToSend = { ...this.employeeData, status: 'comp-off-requests' };
      this.employee360Service.changeEmployeeData(dataToSend);
      this.isCompOffRequest=false;
      this.isCompOffApplication=true;
      this.getPendingCompOffRequestsByManagerId();}
    }

    setCompOffStatus(){
      this.CompOffData=[];
      if(this.isCompOffRequest){
        this.compOffStatus="Compensatory Off Request";
      }else if(this.isCompOffApplication){
        this.compOffStatus="Compensatory Off";
      }
    }

  goBack(){
    this.back=false;
    this.showTable=false;
    this.showDropDown=false;
    this.isCompOff=false;
    this.isLeaveRevokeRequest=false;
    this.selectedOption = 1;
    this.dateTimeRange = null;
    this.generateLeaveChart();
    this.getLeaveDataPerMonthByEmpId();
    this.activeButton="Leave-Charts";
  }

  setActiveButton(button: string): void {
    this.activeButton = button;
    console.log("button=====>",button);
    if(["Revoked","Pending", "Approved", "Rejected"].includes(this.activeButton)){ 
        this.showTable=true;
        this.isCompOff=false;
        this.isCompOffApplication=false;
        this.isLeaveRevokeRequest=false;
        this.getAllLeaveApplicationsByEmpId();}
    else if(["Requests","Applications", "CompOff"].includes(this.activeButton)){
        this.showTable=false;
        this.isCompOffRequest=true;
        this.isCompOffApplication=false;
        this.isLeaveRevokeRequest=false;
        this.isCompOffApplication=false;
        this.isCompOff=true;
        this.getPendingCompOffRequestsByManagerId();}
    else if(["Applied For Revoke"].includes(this.activeButton)){
      this.showTable=false;
      this.isCompOff=false;
      this.isCompOffApplication=false;
      this.isLeaveRevokeRequest=true;
      this.getAllMyTeamsPendingLeaveRevokeApplicationsByManagerId();}
    if(this.activeButton!=="Leave-Charts"){this.back=true;}
    this.setButtons();
    // this.empIdd=240065;
  }

  setButtons(){
    if(this.activeButton=="Pending"){
      this.approveReject=true;}
    else{ this.approveReject=false;}

    if(["Revoked","Pending", "Approved", "Rejected"].includes(this.activeButton)){
      this.showDropDown=true;}
    else{this.showDropDown=false;}
  }

  getAllLeaveApplicationsByEmpId(){
    let leaveObj = new Leave();
    leaveObj.empId = this.empId;
    leaveObj.fromDate = this.formattedStartDate;
    leaveObj.toDate = this.formattedEndDate;

    // leaveObj.managerApprovalStatus=this.activeButton;
    this.employee360Service.getAll360LeaveApplicationsByEmpId(leaveObj).pipe(first()).subscribe(
      (response: any) => {
          if (response.serviceStatus === "Success") {
            this.teamViewLeaveHistoryList=response.serviceResponse;
            this.LeaveListOnStatus(this.teamViewLeaveHistoryList);
            console.log("this.teamViewLeaveHistoryList====>",this.teamViewLeaveHistoryList);
              this.teamViewLeaveHistoryList.forEach((leaveApplication) => {
                this.leaveObj2.leaveId = leaveApplication.leaveId;
                this.leaveObj2.currentUserEmpId = this.currentUser.empId
                leaveApplication.isApprover = ((leaveApplication.managerId === this.currentUser.empId && leaveApplication.managerApprovalStatus === 'Pending') || (leaveApplication.level2ApproverId === this.currentUser.empId && leaveApplication.level2ApprovalStatus === 'Pending') || (leaveApplication.level3ApproverId === this.currentUser.empId && leaveApplication.level3ApprovalStatus === 'Pending'))? true : false;
                console.log("Leave id : ",leaveApplication.leaveId," isApprover: ",leaveApplication.isApprover);
                this.leaveService.isManager(this.leaveObj2).subscribe((response: any) => {
                  if (response.serviceStatus === "Success") {
                    leaveApplication.isManagerFlag = response.serviceResponse;
                  } else {
                    console.error("Error in isManager API:", response.serviceResponse);
                    leaveApplication.isManagerFlag = false; 
                  }
                });
              });      
          } else {
              console.error("Error fetching leave applications:", response.serviceResponse);
          }
      },
      (error) => console.error("API error:", error)
  );
  console.log("leaveApplicationList=>>>>",this.leaveApplicationList);
  }

  LeaveListOnStatus(teamViewLeaveHistoryList: any) {
    this.leaveList = [];
    this.currentUserr=sessionStorage.getItem('currentUser');
    if (this.currentUserr) {
      const currentUserData = JSON.parse(this.currentUserr);
      this.managerId = currentUserData.empId;
      console.log(this.managerId); 
    }
    for (const emp of this.teamViewLeaveHistoryList){
      if(emp.status == this.activeButton){
        if(emp.managerId == this.managerId || emp.level2ApproverId == this.managerId || emp.level3ApproverId == this.managerId){
          emp.isSelected = true;
        }else{
          emp.isSelected = false;
        }
          this.leaveList.push(emp);

      }

    }
    

    console.log("this.leavelist = >", this.leaveList)
    }

  onUpdateLeaveStatus(template: TemplateRef<any>, leaveApplication, updatedLeaveStatusId) {
    // 1 = pending , 2 = Approved , 3= Rejected
    
    leaveApplication.leaveStatusId = updatedLeaveStatusId;
    leaveApplication.leaveStatusUpdatedBy = this.currentUser.empId;
    leaveApplication.rejectReason = leaveApplication.rejectReason?.trim()	
    // leaveApplication.leaveTypeMasterId = 2;

    this.leaveService.updateCompOffById(leaveApplication).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.getPendingCompOffRequestsByManagerId();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }
   //ComOff Applications
   getPendingCompOffRequestsByManagerId() {
    this.allCompOffApplications = []
    let compOff = new Leave();
    compOff.empId = this.empId;
    this.employee360Service.get360PendingCompOffRequestsByEmpId(compOff).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allCompOffApplications = response.serviceResponse;
        this.allCompOffApplications.forEach(compOffApp => {
          compOffApp.fromDate = (compOffApp.fromDate)? moment(compOffApp.fromDate).format(AppComponent.DATE_FORMAT) : null,
          compOffApp.toDate = (compOffApp.toDate)? moment(compOffApp.toDate).format(AppComponent.DATE_FORMAT) : null
        });
        this.setCompOffStatus();
        this.compOffManagerId = response.serviceResponse.flatMap(item => [
          item.managerApprovalStatus === 'Pending' && item.managerId != null ? item.managerId : null,
          item.level2ApprovalStatus === 'Pending' && item.level2ApproverId != null ? item.level2ApproverId : null,
          item.level3ApprovalStatus === 'Pending' && item.level3ApproverId != null ? item.level3ApproverId : null
       ]).filter(id => id != null);
       if(this.compOffManagerId.includes(this.managerId)){this.showActiveButton=true;}
       else{this.showActiveButton=false;}
      this.CompOffData=[];
      if(this.isCompOffRequest){
        this.allCompOffApplications.forEach(compOffApp => {
          if(compOffApp.status==='Pending') 
          this.CompOffData.push(compOffApp)});
      }else if(this.isCompOffApplication){
        this.allCompOffApplications.forEach(compOffApp => {
          if(compOffApp.status==='Approved') 
          this.CompOffData.push(compOffApp)});
      }else{this.CompOffData=[];}
      }
    }); 
}


    onUpdateCompOffStatus(template: TemplateRef<any>, compOffObj, updatedCompOffStatusId) {
      // 1 = pending , 2 = Approved , 3= Rejected
      compOffObj.leaveStatusId = updatedCompOffStatusId;
      compOffObj.leaveStatusUpdatedBy = this.currentUser.empId;
      compOffObj.hodEmail = this.currentUser.email;
      compOffObj.hodName = this.currentUser.name;
      compOffObj.employeeName = compOffObj.createdByName;
  
      this.leaveService.updateCompOffById(compOffObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.openAlertMod(template, response.serviceResponse);
          this.getPendingCompOffRequestsByManagerId();
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }
      });
    }

  // Leave Revoke 
    getAllMyTeamsPendingLeaveRevokeApplicationsByManagerId(){
        console.log("Fetching pending leave revoke applications...");
        this.reporteeLeaveRevokeApplicationList = [];
    
        if(this.userMapping.employee_360_leave_view){
          this.leaveObj.empId = this.employeeData2.empId;
          this.leaveService.getAllMyTeamsPendingLeaveRevokeApplicationsByEmpId(this.leaveObj).pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus == "Success") {
              this.reporteeLeaveRevokeApplicationList = response.serviceResponse;
              this.reporteeLeaveRevokeApplicationList.forEach(leave => {
                leave.fromDate = (leave.fromDate)? moment(leave.fromDate).format(AppComponent.DATE_FORMAT) : null;
                leave.toDate = (leave.toDate)? moment(leave.toDate).format(AppComponent.DATE_FORMAT) : null;
                leave.createdOn = (leave.createdOn)? moment(leave.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
              });
            } else {
              console.error(response.serviceResponse);
            }
          });
        }else{
          this.leaveObj.empId = this.currentUser.empId;
          this.leaveService.getAllMyTeamsPendingLeaveRevokeApplicationsByManagerId(this.leaveObj).pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus == "Success") {
              this.reporteeLeaveRevokeApplicationList = response.serviceResponse;
              this.reporteeLeaveRevokeApplicationList.forEach(leave => {
                leave.fromDate = (leave.fromDate)? moment(leave.fromDate).format(AppComponent.DATE_FORMAT) : null;
                leave.toDate = (leave.toDate)? moment(leave.toDate).format(AppComponent.DATE_FORMAT) : null;
                leave.createdOn = (leave.createdOn)? moment(leave.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
              });
            } else {
              console.error(response.serviceResponse);
            }
          });
        }
      }

  onUpdateRevokeLeaveStatus(template: TemplateRef<any>, leave, updatedLeaveStatusId){
        this.cancelRequest();
          leave.leaveRevokeStatusUpdatedBy = this.currentUser.empId;
          leave.leaveRevokeStatusId = updatedLeaveStatusId;
          leave.approverEmail = this.currentUser.email;	
    
          this.leaveService.updateRevokeLeaveStatus(leave).pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus == "Success") {
              this.openAlertMod(template, response.serviceResponse);
            } else {
              this.openAlertMod(template, response.serviceResponse);
            }
            this.getAllMyTeamsPendingLeaveRevokeApplicationsByManagerId();
          });
      }

  // Modals
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  // openLeaveRejectModal
  openLeaveRejectModal(template: TemplateRef<any>, leave: any){
    this.cancelRequest();
    this.leaveObj = leave
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }

  // single leave reject modal
  onSingleReject(template: TemplateRef<any> , ){
    this.onUpdateLeaveStatus(template, this.leaveObj,3);
  }

  openRevokeLeaveRejectModal(template: TemplateRef<any>, leave: any){
    this.leaveObj.rejectReason = '';
    this.cancelRequest();
    this.leaveObj = leave;
    this.modalRef = this.modalService.show(template);
  }

  cancelRequest() {
      this.modalRef.hide();}

  getAllLeaveTypesByLeavePolicies() {
    this.leaveTypes = [];
    let leaveObj = new Leave();
    leaveObj.employmentStatus = this.employeeData.employmentstatus;
    leaveObj.gender =this.employeeData.gender;
    leaveObj.maritalStatus = this.employeeData.maritalStatus;

    this.leaveService.getAllLeaveTypesByLeavePolicies(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveTypes = response.serviceResponse;
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
      this.leaveBucketDetails.forEach(data => {
        let leaveDetail = this.leaveBalanceList.find((leave: Leave) => leave.leaveTypeCode == data.leaveTypeCode);
        if (leaveDetail) {
          data.balance = (leaveDetail.balance) ? leaveDetail.balance : 0;
        }
      });
    } else {
      console.error(leaveBalanceResponse.serviceResponse);
    }

    let approvedLeaveResponse:any = await this.leaveService.countMyApprovedLeaveApplicationsByLeaveType(leaveObj).pipe(first()).toPromise();
    if (approvedLeaveResponse.serviceStatus == "Success") {
      this.approvedLeavesList = approvedLeaveResponse.serviceResponse;
      this.leaveBucketDetails.forEach(data => {
        let leaveDetail = this.approvedLeavesList.find((leave: Leave) => leave.leaveTypeCode == data.leaveTypeCode);
        if (leaveDetail) {
          data.approvedApplicationsCount = (leaveDetail.applicationCount) ? leaveDetail.applicationCount : 0;
        }
      });
    } else {
      console.error(approvedLeaveResponse.serviceResponse);
    }

    let rejectedLeaveResponse:any = await this.leaveService.countMyRejectedLeaveApplicationsByLeaveType(leaveObj).pipe(first()).toPromise();
    if (rejectedLeaveResponse.serviceStatus == 'Success') {
      this.rejectedLeavesList = rejectedLeaveResponse.serviceResponse;
      this.leaveBucketDetails.forEach(data => {
        let leaveDetail = this.rejectedLeavesList.find((leave: Leave) => leave.leaveTypeCode == data.leaveTypeCode);
        if (leaveDetail) {
          data.rejectedApplicationsCount = (leaveDetail.applicationCount) ? leaveDetail.applicationCount : 0;
        }
      });
    } else {
      console.error(rejectedLeaveResponse.serviceResponse);
    }

    let pendingLeaveResponse:any = await this.leaveService.countMyPendingLeaveApplicationsByLeaveType(leaveObj).pipe(first()).toPromise();
    if (pendingLeaveResponse.serviceStatus == "Success") {
      this.pendingLeavesList = pendingLeaveResponse.serviceResponse;
      this.leaveBucketDetails.forEach(data => {
        let leaveDetail = this.pendingLeavesList.find((leave: Leave) => leave.leaveTypeCode == data.leaveTypeCode);
        if (leaveDetail) {
          data.pendingApplicationsCount = (leaveDetail.applicationCount) ? leaveDetail.applicationCount : 0;
        }
      });
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
    leaveObj.empId = this.empId;
    this.leaveService.countMyApprovedLeaveApplicationsByLeaveType(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.approvedLeavesList = response.serviceResponse;
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
    leaveObj.empId = this.empId;
    this.leaveService.countMyPendingLeaveApplicationsByLeaveType(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.pendingLeavesList = response.serviceResponse;
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
            click: function (event) {},
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

  getLeaveDataPerMonthByEmpId(): void {
    this.employee360Service.getLeaveDataPerMonthByEmpId(this.empId).pipe(first()).subscribe((response: any) => {
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
                let leaveMonth = parseInt(leave.leaveMonth, 10) - 1;
                if (leaveMonth < 3) {
                  leaveMonth = leaveMonth + 9; // Jan (0)
              } else {
                  leaveMonth = leaveMonth - 3; // Apr (3)
              }
                const totalDays = parseFloat(leave.totalDays);
                this.year=leave.year.split('-')[0];;
                console.log("year=====>",this.year);
                
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
            Highcharts.chart('leaveByMonthContainer', {
              chart: {
                  type: 'column',
              },
              title: {
                  text: `Leave Data for ${this.year-1} - ${this.year}`,
                  style: {
                    fontWeight: 'bold', },
              },
              xAxis: {
                  categories: ["Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec","Jan", "Feb", "Mar"],
                  title: {
                      text: "Month",
                      style: {
                        fontWeight: 'bold', },
                  },
              },
              yAxis: {
                  min: 0,
                  title: {
                      text: "Leave Count",
                      style: {
                        fontWeight: 'bold', },
                  },
              },
              tooltip: {
                  shared: false,
                  formatter: function (this: Highcharts.TooltipFormatterContextObject) {
                      const seriesOptions = this.series.options as any; 
                      return `<b>Month:</b> ${this.x}<br/>` +
                          `<b>Status:</b> ${seriesOptions.status}<br/>` +
                          `<b>Leave Type:</b> ${seriesOptions.leaveType}<br/>`+
                          `<b>Days:</b> ${this.y}`;
                  },
              },
              legend: {
                  enabled: false, 
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
                      showInLegend: false, 
                  },
              },
              series: seriesData, 
          },
          function (chart) {
              const uniqueStatuses = Array.from(new Set(seriesData.map(series => series.status)));
              const legendContainer = document.createElement('div');
              legendContainer.id = 'custom-legend';
              legendContainer.style.textAlign = 'center';
              legendContainer.style.marginTop = '10px';
          
              uniqueStatuses.forEach(status => {
                  const color = seriesData.find(series => series.status === status)?.color || '#000';
                  const legendItem = document.createElement('div');
                  legendItem.style.display = 'inline-block';
                  legendItem.style.margin = '0 15px';
                  legendItem.style.cursor = 'pointer';
          
                  legendItem.innerHTML = `
                      <span style="background-color: ${color}; width: 10px; height: 10px; display: inline-block; margin-right: 5px; border-radius: 2px;"></span>
                      ${status}
                  `;
          
                  legendItem.addEventListener('click', () => {
                      chart.series.forEach(series => {
                          if ((series.options as any).status === status) {
                              series.visible ? series.hide() : series.show();
                          }
                      });
                  });
          
                  legendContainer.appendChild(legendItem);
              });
          
              const container = document.getElementById('leaveByMonthContainer');
              if (container) {
                  container.parentElement?.appendChild(legendContainer);
              }
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

  onChangeOption(arg: any) {
    if (arg == 1) {
      // Handle "All"
      this.startDate = null;
      this.endDate = null;
      this.setDate();
    } else if (arg == 4) {
      // start and end date will be handled by the owl-datepicker input fields
    }
    console.log("startDate" , this.startDate);
    console.log("endDate" , this.endDate);

  }

  getDateRange() {
    if (this.dateTimeRange && this.dateTimeRange.length === 2) {
      const fromDate = this.dateTimeRange[0];
      const toDate = this.dateTimeRange[1];
  
      console.log('From Date:', fromDate);
      console.log('To Date:', toDate);
      this.startDate = fromDate;
      this.endDate = toDate;
      this.setDate();
    }
  }

  resetDateRange() {
    this.dateTimeRange = null;
    this.startDate = null;
    this.endDate = null;
    this.setDate();
  }

  setDate(){
    if (this.startDate && this.endDate) {
       this.formattedStartDate = this.datePipe.transform(this.startDate, 'dd-MM-yyyy');
      this.formattedEndDate = this.datePipe.transform(this.endDate, 'dd-MM-yyyy');
      this.getAllLeaveApplicationsByEmpId();
    }else{
      this.formattedStartDate='';
      this.formattedEndDate='';
      this.getAllLeaveApplicationsByEmpId();

    }
  }
  
}
