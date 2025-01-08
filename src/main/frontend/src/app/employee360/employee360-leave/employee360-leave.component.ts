import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import * as HighCharts from 'highcharts';
import HC_exportData from "highcharts/modules/export-data";
import { LeaveService } from '../../services/leave.service';
import { Leave } from 'src/app/models/leave';
import { first } from 'rxjs/operators';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import * as moment from 'moment';
import { AppComponent } from 'src/app/app.component';
import { Log } from '../../models/log';
import { LogService } from 'src/app/services/log.service';
import { ActivatedRoute, Router } from '@angular/router';
import { UtilityService } from 'src/app/services/utility.service';
import { Employee360Service } from 'src/app/services/employee360.service';
import * as Highcharts from 'highcharts';
import { Breadcrumb } from 'src/app/models/breadcrumd';
import { BreadcrumbService } from 'src/app/services/breadcrumb.service';
import { ScrollStrategy } from '@angular/cdk/overlay';
import { DatePipe } from '@angular/common';


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

  //Active Buttons
  activeButton: string = "Leave-Charts";
  activeCompOffButton: string = 'Requests'; 

  //boolean
  showTable:boolean=false;
  back:boolean=false;

  //Leave 
  empId:any=0;
  managerId:any=0;
  teamViewLeaveHistoryList: any[] = [];
  leaveApplicationList: any[] = [];

  //Comp-off
  allCompOffApplications: any[] = [];
  isCompOffRequest: boolean = false;

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
  year=new Date().getFullYear();
  // selectedOption:any=1;
  // startDate: any;
  // endDate:any; 

  chartOptions: Highcharts.Options = {
    chart: {
        type: "column"
    },
    title: {
        text: "Leave Data Per Month"
    },
    xAxis: {
        categories: ["Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec"],
        title: {
            text: "Month",
            style: {
              fontWeight: 'bold', },
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
    private breadcrumbService: BreadcrumbService,
    private modalService: BsModalService,
    private datePipe: DatePipe,
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

    const navigation = this.router.getCurrentNavigation();
    this.employeeData = navigation?.extras.state?.['employeeData'];
    this.employeeData2 = this.employeeData;
   }

  ngOnInit(): void {  
    this.employeeData2 = history.state.data;
    console.log("Priyadarshini  Leave    ",this.employeeData);
    // const employeeName = this.employeeData?.name || "Employee";
    // const breadcrumbObject = { title: `${employeeName} - Leave`, url: "/employee-360/leave" };
    // this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);
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
    console.log(this.year);
    
    this.getLeaveDataPerMonthByEmpId(this.year);
    this.countMyApprovedLeaveApplicationsByLeaveType();
  }

  setActiveCompOffButton(button: string): void {
    this.activeCompOffButton = button;
    if (button === 'Requests') {
      const dataToSend = { ...this.employeeData, status: 'comp-off-requests' };
      this.employee360Service.changeEmployeeData(dataToSend);
      // this.redirectToMyTeam('comp-off-requests');
    }
    if (button === 'Applications') {
      const dataToSend = { ...this.employeeData, status: 'comp-off-requests' };
      this.employee360Service.changeEmployeeData(dataToSend);
      // this.redirectToMyTeam('comp-off-applications');
    }
    }

  // redirectToMyTeam(status: string): void {
  //   console.log(" redirectToMyTeam ", this.employeeData);
  //   const dataToSend = { ...this.employeeData, status };
  //   this.utilityService.setEmployee360ViewAccess(true);
  //   this.employee360Service.changeEmployeeData(dataToSend);

  //   const employeeName = this.employeeData?.name || "Employee";

  //   let breadcrumbObject = { title: `${employeeName} - ${status}`, url: "/user-team" };
  //   this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);

  //   this.router.navigate(['/user-team'], { 
  //     queryParams: { tab: 'my-team', action: 'view-pending-request', status }
  //   });

  //   console.log("Redirecting with queryParams:", { tab: 'my-team', action: 'view-pending-request', status });
  //   console.log(" redirectToMyTeam end ", this.employeeData);
  // }  


  goBack(){
    this.back=false;
    this.showTable=false;
    this.isCompOffRequest=false;
    this.generateLeaveChart();
    this.getLeaveDataPerMonthByEmpId(this.year);
    this.activeButton="Leave-Charts";   
  }

  setActiveButton(button: string): void {
    this.activeButton = button;
    console.log("button=====>",button);
    if(["Revoked","Pending", "Approved", "Rejected", "Applied For Revoke"].includes(this.activeButton)){ 
        this.showTable=true;
        this.isCompOffRequest=false;
        this.getAllLeaveApplicationsByEmpId();}
    else if(["Requests","Applications", "CompOff"].includes(this.activeButton)){
        this.showTable=false;
        this.isCompOffRequest=true;
        this.getPendingCompOffRequestsByManagerId();}
    if(this.activeButton!=="Leave-Charts"){this.back=true;}
    // this.empIdd=240065;
  }

  getAllLeaveApplicationsByEmpId(){
    let leaveObj = new Leave();
    const processLeaveApplications = (leaveApplications: any[]) => {
            return leaveApplications.map((leaveApp, index) => ({
                ...leaveApp,
                checkId: `leave${index}`,
                fromDate: leaveApp.fromDate ? moment(leaveApp.fromDate).format(AppComponent.DATE_FORMAT) : null,
                toDate: leaveApp.toDate ? moment(leaveApp.toDate).format(AppComponent.DATE_FORMAT) : null,
                createdOn: leaveApp.createdOn ? moment(leaveApp.createdOn).format(AppComponent.DATETIME_FORMAT) : null,
                currentApprovalLevel: leaveApp.currentApprovalLevel || 1,
                finalApprovalLevel: leaveApp.finalApprovalLevel || 1,
            }));
        };
    leaveObj.empId = this.empId;
    leaveObj.fromDate = this.formattedStartDate;
    leaveObj.toDate = this.formattedEndDate;

    // leaveObj.managerApprovalStatus=this.activeButton;
    this.employee360Service.getAll360LeaveApplicationsByEmpId(leaveObj).pipe(first()).subscribe(
      (response: any) => {
          if (response.serviceStatus === "Success") {
            this.teamViewLeaveHistoryList=response.serviceResponse;
            console.log("this.teamViewLeaveHistoryList====>",this.teamViewLeaveHistoryList);
            
              this.leaveApplicationList = processLeaveApplications(
                  response.serviceResponse.filter((leaveApp: any) => leaveApp.status === this.employeeData2.status)
              );
              this.leaveApplicationList.forEach((leaveApplication) => {
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

   //ComOff Applications
  getPendingCompOffRequestsByManagerId() {
      // if (this.userMapping.employee_360_leave_view){
        // console.log("Fetching pending comp-off requests...");
        this.allCompOffApplications = []
  
        let compOff = new Leave();
        compOff.empId = this.empId;
        this.leaveService.getPendingCompOffRequestsByEmpId(compOff).pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus == "Success") {
  
            this.allCompOffApplications = response.serviceResponse;
            this.allCompOffApplications.forEach(compOffApp => {
              compOffApp.fromDate = (compOffApp.fromDate)? moment(compOffApp.fromDate).format(AppComponent.DATE_FORMAT) : null,
              compOffApp.toDate = (compOffApp.toDate)? moment(compOffApp.toDate).format(AppComponent.DATE_FORMAT) : null
            });
            //console.log("allCompOffApplications : ", this.allCompOffApplications);
          } else {
            console.error(response.serviceResponse);
          }
        });
      // }
      // else 
      // if(!this.userMapping.employee_360_leave_view){
        // console.log("Fetching pending comp-off requests...");
        this.allCompOffApplications = []
  
        // let compOff = new Leave();
        compOff.managerId = this.currentUser.empId;
        this.leaveService.getPendingCompOffRequestsByManagerId(compOff).pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus == "Success") {
  
            this.allCompOffApplications = response.serviceResponse;
            this.allCompOffApplications.forEach(compOffApp => {
              compOffApp.fromDate = (compOffApp.fromDate)? moment(compOffApp.fromDate).format(AppComponent.DATE_FORMAT) : null,
              compOffApp.toDate = (compOffApp.toDate)? moment(compOffApp.toDate).format(AppComponent.DATE_FORMAT) : null
            });
            //console.log("allCompOffApplications : ", this.allCompOffApplications);
          } else {
            console.error(response.serviceResponse);
          }
        });
      // } else {
      //   console.error("Something went wrong");
      // }    
    }

    onUpdateCompOffStatus(template: TemplateRef<any>, compOffObj, updatedCompOffStatusId) {
      // 1 = pending , 2 = Approved , 3= Rejected
  
      //console.log("template: ", this.alertTemplate );
  
      compOffObj.leaveStatusId = updatedCompOffStatusId;
      compOffObj.leaveStatusUpdatedBy = this.currentUser.empId;
      compOffObj.hodEmail = this.currentUser.email;
      compOffObj.hodName = this.currentUser.name;
      compOffObj.employeeName = compOffObj.createdByName;
  
      //console.log("Update Comp off : ", compOffObj);
      this.leaveService.updateCompOffById(compOffObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.openAlertMod(template, response.serviceResponse);
          this.getPendingCompOffRequestsByManagerId();
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }
      });
    }

    // Modals

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }
  
  cancelRequest() {
      this.modalRef.hide();
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
                  text: `Leave Data for ${this.year}`,
                  style: {
                    fontWeight: 'bold', },
              },
              xAxis: {
                  categories: ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"],
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
                          `<b>Days:</b> ${this.y}`;
                  },
              },
              legend: {
                  enabled: false, // Disable default legend
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
                      showInLegend: false, // Disable per-series legend
                  },
              },
              series: seriesData, // Your processed series data
          },
          function (chart) {
              // Build a custom legend
              const uniqueStatuses = Array.from(new Set(seriesData.map(series => series.status)));
              const legendContainer = document.createElement('div');
              legendContainer.id = 'custom-legend';
              legendContainer.style.textAlign = 'center';
              legendContainer.style.marginTop = '10px';
          
              uniqueStatuses.forEach(status => {
                  const color = seriesData.find(series => series.status === status)?.color || '#000';
          
                  // Create a legend item
                  const legendItem = document.createElement('div');
                  legendItem.style.display = 'inline-block';
                  legendItem.style.margin = '0 15px';
                  legendItem.style.cursor = 'pointer';
          
                  legendItem.innerHTML = `
                      <span style="background-color: ${color}; width: 10px; height: 10px; display: inline-block; margin-right: 5px; border-radius: 2px;"></span>
                      ${status}
                  `;
          
                  // Add click event to toggle visibility
                  legendItem.addEventListener('click', () => {
                      chart.series.forEach(series => {
                          if ((series.options as any).status === status) {
                              series.visible ? series.hide() : series.show();
                          }
                      });
                  });
          
                  legendContainer.appendChild(legendItem);
              });
          
              // Append custom legend to the chart container
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
  
      // logic to filter data based on the selected range
      this.setDate();
    }
  }

  resetDateRange() {
    this.dateTimeRange = null;
    this.startDate = null;
    this.endDate = null;
    this.setDate();
    // Optionally, call your method to fetch data after reset
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
