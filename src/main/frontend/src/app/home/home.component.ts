import { LocationStrategy } from '@angular/common';
import { AfterViewInit, ChangeDetectorRef, Component, OnInit, SecurityContext, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { DomSanitizer } from '@angular/platform-browser';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import * as CryptoJS from 'crypto-js';
import * as HighCharts from 'highcharts';
import * as moment from 'moment';
import { first } from 'rxjs/operators';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { AppComponent } from '../app.component';
import { BodyComponent } from '../body/body.component';
import { CalendarComponent } from '../helpers/calendar/calendar.component';
import { Employee } from '../models/employee';
import { ExpiredEmailData } from '../models/expiredEmailmodel';
import { Feature } from '../models/feature';
import { Leave } from '../models/leave';
import { Log } from '../models/log';
import { NotificationMessage } from '../models/notification';
import { PoObject } from '../models/poObbjectData';
import { RewardCategory } from '../models/rewardCategory';
import { Timesheet } from '../models/timesheet';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { EmployeeService } from '../services/employee.service';
import { Employee360Service } from '../services/employee360.service';
import { ImageService } from '../services/image.service';
import { LeaveService } from '../services/leave.service';
import { LogService } from '../services/log.service';
import { NotificationService } from '../services/notification.service';
import { RewardsServiceService } from '../services/rewards-service.service';
import { TimesheetService } from '../services/timesheet.service';
import { UtilityService } from '../services/utility.service';
import { ValidationService } from '../services/validation.service';
import { ProjectService } from '../services/project.service';
import { EncryptionService } from '../services/EncryptionService';
import {TimesheetNewService} from '../services/timesheet-new.service';
import { MilestoneToBeExpired } from '../models/milestoneToBeExpired';
import { MilestoneExtendReason } from '../models/MilestoneExtendReason';
import { MilestoneUpdatedLog } from '../models/MilestoneUpdatedLog';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

interface objlms {
  email: any
}
interface LmsRediredtion {

  email: any,
  message: any,
  url: any,
  tokem: any

}

@Component({
  standalone: false,
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit, AfterViewInit {
  private lmsbaseurl: any = '';
  lines: any = [];
  probationNotifications: any[] = [];
  isLoadingNotifications = false;

    @ViewChild("previewTemplate")
    previewModal : TemplateRef<any>;

 previewUrl: any;
  fileType: '' | 'pdf' | 'image' | null = null;
  docData:any;
  mimeType:any;

  data: string;
  //modal
  alertMessage: any;
  modalRef: NgbModalRef;
  modalRef2: NgbModalRef;

  milestoneDetailModalRefView: NgbModalRef;
  detailModalRef: NgbModalRef;
  popUpModalResf: NgbModalRef;

   @ViewChild('milestoneExpireValidationPupup') milestoneExpireValidationPupup: TemplateRef<any>;
   milestoneExpireValidationPupupModalRef: NgbModalRef;

   modalMessage:String='';
    isHelpHovered = false;



  feature = "Home";
  currentUser: User;
  currentUserName = "";
  userMapping: any = {};
  log: Log;

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;

  isReqPending: boolean = true;
  leaveApplicationCount: any = 0;
  leaveApplicationList: any[] = [];

  compOffApplicationCount: any = 0;
  allCompOffApplications: any[] = [];

  timesheetApplicationCount: any = 0;
  allTeamTimesheetRequests: any[] = [];
  timesheetObj: Timesheet = new Timesheet();
  selectedRejectReason:any;
  //export excel
  excelName: any = '';

  // Timsheet Status Counts
  pendingCount = 0;
approvedCount = 0;
rejectedCount = 0;

  birthdayList: any[] = [];
  workAnniversaryList: any[] = [];
  rewardsList: any[] = [];
  eventImages: any[] = [];
  isImagesLoaded: boolean = false;
  response1: any;

  leaveBalanceList: any[] = [];
  rejectedLeavesList: any[] = [];
  approvedLeavesList: any[] = [];
  pendingLeavesList: any[] = [];
  allNotification: any[] = [];

  notificationObj: NotificationMessage = new NotificationMessage();
  timesheetDetails: any[] = [];

  items = 10;
  bulkApprove: any = [];
  bulkReject: any = [];
  isSelectAll: boolean = false;
  bulkLeaveApprove: any = [];
  bulkLeaveReject: any = [];
  bulkCompOffApprove: any = [];
  bulkCompOffReject: any = [];
  overLapsLeaveForManager: any = [];
  milestoneToBeExpired: MilestoneToBeExpired[] = [];
  milestoneExtendReason: MilestoneExtendReason[] = [];
  milestoneExpiredPage = 1;

  milestoneExpiredSortColumn = 'endDate';
  milestoneExpiredSortColumnType = 'string';
  milestoneExpiredSortDirection = 'asc';
  isOtherReasonSelected: boolean = false;
  milestoneCount: number = 0;
  selectedMilestone: MilestoneToBeExpired | null = null;


  fieldTextType: boolean = false;
  fieldTextTypePassword: boolean = false;
  fieldTextTypeOldPass: boolean = false;
  config = {
    backdrop: true,
    ignoreBackdropClick: true,
    keyboard: false
  };
  oldPasswordValid: boolean = false;
  rewardCategoryObj: RewardCategory = new RewardCategory();
  password: any;
  userNewPass: any;
  newpassword: any;
  errorMsg: any;
  empId: any;
  consentNotificationMessage: any;
  user: User = new User();
  leaveApplication: any;
  show: number = -1;
  leaveTypes: Leave[] = [];
  leaveBucketDetails: any[] = [];
  lmsauthentication: any;
  // Expired Po section
  expiredList: any[] = [];
  popUpMessege: any;
  //  notificationObj: NotificationMessage = new NotificationMessage();
  //  timesheetDetails: any[] = [];

  //  items = 10;
  //  bulkApprove: any = [];
  //  bulkReject: any = [];
  //  isSelectAll: boolean = false;
  //  bulkLeaveApprove: any = [];
  //  bulkLeaveReject: any = [];
  //  bulkCompOffApprove: any = [];
  //  bulkCompOffReject: any = [];
  //  overLapsLeaveForManager: any = [];

  //Timesheet form render
  isTimesheetFormVisible: boolean = false;
  lastTimesheetData: any = null;

  milestoneForm!: FormGroup;
  clientFilter: boolean | null = null;

  leaveObj = new Leave();
  leaveRejectionReasonList: any[] = [];
selectedRejectionIds: number[] = [];
showOtherRemarks = false;

  zoomScale = 1;
  zoomLevel = 100;

  isDragging = false;
  startX = 0;
  startY = 0;
  translateX = 0;
  translateY = 0;

  @ViewChild("thisMonthCal")
  private thisMonthCalendar: CalendarComponent;
  @ViewChild("lastMonthCal")
  private lastMonthCalendar: CalendarComponent;

  @ViewChild('updateInfo')
  private updateInfoTempRef: TemplateRef<any>;

  @ViewChild('consent_notification_template')
  private consentNotificationTemplate: TemplateRef<any>;

  @ViewChild('inactiveEmployeeTemplate') inactiveEmployeeTemplate!: TemplateRef<any>;

  @ViewChild('noTimesheetFoundEmployeeTemplate') noTimesheetFoundEmployeeTemplate!: TemplateRef<any>;

  @ViewChild('poexpire_template_fixed')
  poExpireTemplateRef: TemplateRef<any>;


  @ViewChild('milestone_expired_list_modal') milestoneExpiredListModalRef!: TemplateRef<any>;

  @ViewChild('milestoneDetailModal') milestoneDetailModalRef!: TemplateRef<any>;



  @ViewChild('mailSentPopUp')
  mailSentPopUp: TemplateRef<any>;

  consentModalConfig = {
    backdrop: true,
    ignoreBackdropClick: true,
    keyboard: false,
    modalDialogClass: 'modal-lg'
  }
  // TOP BAR
  @ViewChild("change_password")
  changePasswordTemplate: TemplateRef<any>;
  @ViewChild("LoadingLogin") LoadingLoginTemplate: TemplateRef<any>;
  selectedCategoryId: any;
  selectedCategoryName: any;
  selectedMonth: any;
  scrollingInterval: any;
  showEmptyMessage: any;

  isError: boolean = false;
  profileCompletedPercentage: any = 0;
  filters: any = {};
  isSearchEnabled: boolean = false;
  leaveApplicationColumns: any[] = ['blank', 'blank', 'employeeName', 'leaveType', 'fromDate', 'toDate', 'noOfDays', 'status', 'createdByName', 'createdOn', 'reason', 'currentApprovalLevel', 'approverName', 'managerApprovalStatus', 'level2ApproverName', 'level2ApprovalStatus', 'level3ApproverName', 'level3ApprovalStatus'];
  compOfApplicationColumns: any[] = ['blank', 'createdByName', 'compOffReasons', 'fromDate', 'toDate', 'noOfDays', 'description', 'status'];
  timesheetApplicationsColumns: any[] = ['blank', 'blank', 'blank', 'employeementId', 'employeeName', 'date', 'dayType', 'description', 'officeInTime', 'officeOutTime', 'totalWorkingOfficeHours', 'isNightShift', 'status',,'clientInTime','clientOutTime','totalClientWorkingHours', 'clientApprovalStatus', 'filledDocument','approvedDocument','createdOn'];
  isShowReleaseNote: boolean = false;
  releaseNoteText = "";
  currentIndex: any = 0;
  currentGroup: any = null;
  scrollDelay: number = 18700;
  employeesFor360: any[] = [];
  rewardCategoryList: any[] = [];
  groupedRewards: { [key: string]: any[] } = {};
  monthKeys: string[] = [];
  currentMonthIndex: number = 0;
  currentRewards: any[] = [];
  scrollInterval: any;
  selectedTab: string = 'birthday';
 rejectReasons: any;
 jobRole: string = '';
  probation:number=0;
   rmTimesheetbulletContent: string[] = [
    "Displays all timesheet entries submitted by your reportees.",
    "Review entries on a day-wise basis.",
    "Approve or Reject individual entries as required.",
    "Use bulk approval when all entries are correct.",
    "Ensure all entries are approved for the month to close successfully.",
  ]

location: any;
act: any;
project: any;
timesheet: any;

  constructor(
    private modalService: NgbModal,
    private authenticationService: AuthenticationService,
    private leaveService: LeaveService,
    private exportExcelService: ExportExcelService,
    private router: Router,
    private route: ActivatedRoute,
    private employeeService: EmployeeService,
    private timesheetService: TimesheetService,
    private imageService: ImageService,
    private sanitizer: DomSanitizer,
    private notificationService: NotificationService,
    private bodyComponent: BodyComponent,
    public validationService: ValidationService,
    private logService: LogService,
    private locationStrategy: LocationStrategy,
    private rewardsService: RewardsServiceService,
    public utilityService: UtilityService,
    private cdr: ChangeDetectorRef,
    private encryptionService: EncryptionService,
    public employee360Service: Employee360Service,
    public projectService: ProjectService,
    private fb: FormBuilder,
    private timesheetNewService : TimesheetNewService,

  ) {
    this.authenticationService.currentUser.subscribe(x => {
      this.currentUser = x;
      this.currentUserName = this.currentUser.name.split(" ")[0];
      this.currentUserName = this.currentUserName[0].toUpperCase() + this.currentUserName.slice(1).toLowerCase();
      this.jobRole = this.currentUser.employeeRole;
    });
    this.logService.log.subscribe(x => {
      this.log = x;
      this.log.tabName = this.feature;
      this.log.featureName = this.feature;
    });
  }
  //added by rahul for lms redirection
  //added by rahul for lms redirection
  LmsRedirection() {
    let obj = new Object();
    obj = { email: this.currentUser.email, token: sessionStorage.getItem('token') };


    //obj = { email: "mohamed.owais@apmosys.com"};
    //obj = { email: "mohamed2.owais@apmosys.com"};
    this.employeeService.IsValidateLMSPORTAL(obj).subscribe((response: any) => {
      //this.lmsauthentication = response.serviceResponse;
      this.lmsauthentication = response.serviceResponse;

      if (response.serviceStatus == "success") {
        window.open(response.serviceResponse, '_blank');
      }
      else {
        window.open(`${this.lmsbaseurl}home/sign_up`, '_blank');
      }
    })

  }

  async ngOnInit(): Promise<void> {
    if (this.currentUser.isNew === "true") {
      sessionStorage.setItem('isFirstTimeLogin', 'true');
      window.history.pushState(null, "", window.location.href);
      window.onpopstate = function () {
        window.history.pushState(null, "", window.location.href); // Keep pushing new states
      };
      this.bodyComponent.openChangePasswordOnFirstTimeLoggin();





    }






    if (sessionStorage.getItem('isFirstTimeLogin') === 'true') {
      this.bodyComponent.openChangePasswordOnFirstTimeLoggin();
      sessionStorage.removeItem('isFirstTimeLogin');
    }
    this.getEmployeeProfileCompletion();
    this.logService.updateLogInfo(this.log);
    // Dynamic Subfeature Flags
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    this.getAutoFillTimesheetFlag();
    this.getAllNotifications();
    this.fetchMilestones();
    this.initMilestoneForm();
    this.featchingmilestoneExtensionReason();
    this.getAllLeaveTypesByLeavePolicies(this.currentUser);
    if (this.userMapping.view_birthday_list) this.getAllEmployeesBirthDayToday();
    if (this.userMapping.view_work_anniversary_list) this.getAllEmployeesWorkAnniversaryToday();
    if (this.userMapping.view_all_team_requests) {
      this.countAllMyTeamsPendingLeaveApplicationsByManagerId();
      this.countPendingCompOffRequestsByManagerId();
      this.countMyReporteesTimesheetRequests();
    }
    if (this.userMapping.view_my_leave_details) {
      this.getMyLeaveBalancesByEmpId();
      console.log(this.currentUser,"lalalacurrentUser");
    }
    if (this.userMapping.view_timesheet_display) this.getTimesheetsForHomePageByEmpId('Last 7 Days');
    if (this.userMapping.view_event_photos) this.getAllEventPhotosForHome();
    //  if (this.userMapping.view_employee_rewards) this.fetchEmployeesForHomepageByCategoryId(1);

    if (this.userMapping.view_employee_rewards) this.fetchRewardCategoryForHomePage();

    this.preventBackButton();
    this.isEmployeeOnBench();
    this.getRejectionReason();
    //console.log('User Mapping', this.userMapping);
    this.getTimesheetStatusCountsByEmpId();
    this.getLeaveRejectionReasons();

  }

  //   openProbationNotificationModal(template: TemplateRef<any>) {
  //   if (!this.currentUser || !this.currentUser.empId) {
  //       console.error("Current user (HOD) not found. Cannot fetch notifications.");
  //       return;
  //   }

  //   this.isLoadingNotifications = true;
  //   this.probationNotifications = [];
  //   this.modalRef = this.modalService.open(template, { modalDialogClass:'modal-lg' });

  //   const payload = {
  //     hodId: this.currentUser.empId,
  //   };


  //   this.employeeService.getProbationReminders(payload).subscribe({
  //     next: (response) => {
  //       if (response && response.serviceStatus === 'SUCCESS') {
  //         this.probationNotifications = response.serviceResponse;
  //       } else {
  //       }
  //       this.isLoadingNotifications = false;
  //     },
  //     error: (err) => {
  //       console.error('Failed to load probation notifications', err);
  //       this.isLoadingNotifications = false;
  //     }
  //   });
  // }

  switchTab(tab: string) {
    this.selectedTab = tab;
  }

  preventBackButton() {
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(() => {
      history.pushState(null, null, location.href);
    })
  }

  onRejectionReasonChange() {
    const other = this.leaveRejectionReasonList.find(
      x => x.reason.toLowerCase() === 'other'
    );

    this.showOtherRemarks =
      other && this.selectedRejectionIds.includes(other.rejectionReasonId);

    if (!this.showOtherRemarks) {
      this.leaveObj.rejectReason = '';
    }
  }
  isRejectDisabled(): boolean {
    if (this.selectedRejectionIds.length === 0) {
      return true;
    }

    if (!this.leaveObj.rejectReason ||
      !this.leaveObj.rejectReason.trim()) {
      return true;
    }

    return false;
  }



  ngAfterViewInit(): void {
    if (this.currentUser.isNew == "false" && this.currentUser.isUserInfoUpdated == false && this.currentUser.updateFormCounter == 0) {
      this.openUpdateInfo(this.updateInfoTempRef);
      this.currentUser.updateFormCounter = 1;
    }

    if (this.currentUser.isNew == "false") {
      //console.log("this.currentUser : ", this.currentUser);
      this.openConsentNotificationModal();
      this.setReleaseNote();
    }

    if (this.currentUser.employeeRole == "RMG") {
      this.openPoExpiredMod();
    }

  }

  reset() {
    let leaveObj = new Leave();
    leaveObj.isSelected = false
    this.isSelectAll = false;

  }

  // Leave Applications
  getAllMyTeamsPendingLeaveApplicationsByManagerId() {
    this.data = ''
    this.leaveApplicationList = []
    this.bulkLeaveApprove = []
    this.bulkLeaveReject = []
    this.isSelectAll = false
    this.items = 10;

    let leaveObj = new Leave();
    leaveObj.managerId = this.currentUser.empId;
    leaveObj.approverEmail = this.currentUser.email;
    this.leaveService.getAllMyTeamsPendingLeaveApplicationsByManagerId(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveApplicationList = response.serviceResponse;
        this.leaveApplicationList.forEach((leave, index) => {
          leave.checkId = "leave" + index;
          leave.fromDate = (leave.fromDate) ? moment(leave.fromDate).format(AppComponent.DATE_FORMAT) : null;
          leave.toDate = (leave.toDate) ? moment(leave.toDate).format(AppComponent.DATE_FORMAT) : null;
          leave.createdOn = (leave.createdOn) ? moment(leave.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          if (!leave.currentApprovalLevel && !leave.finalApprovalLevel) {
            leave.currentApprovalLevel = 1;
            leave.finalApprovalLevel = 1;
          }
          leave.emp360 = leave.empId;
          leave.emp360AppLev1 = leave.level1ApproverId;
          leave.emp360AppLev2 = leave.level2ApproverId;
          leave.emp360AppLev3 = leave.level3ApproverId;
          leave.createdBy360 = leave.empId

        });
        // console.log("leaveApplicationList : ", this.leaveApplicationList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  /* Leave Applications Count
  *  Added by suraj 07/08/2022
  */
  countAllMyTeamsPendingLeaveApplicationsByManagerId() {
    this.leaveApplicationList = []

    let leaveObj = new Leave();
    leaveObj.managerId = this.currentUser.empId;
    this.leaveService.countAllMyTeamsPendingLeaveApplicationsByManagerId(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveApplicationCount = response.serviceResponse.applicationCount;
        //console.log("leaveApplicationCount : ", this.leaveApplicationCount);
      } else {
        this.leaveApplicationCount = 0;
        console.error(response.serviceResponse);
      }
    });
  }

  onUpdateLeaveStatus(template: TemplateRef<any>, leaveApplication, updatedLeaveStatusId) {
    this.cancelRequest();
    // 1 = pending , 2 = Approved , 3= Rejected
    leaveApplication.leaveStatusId = updatedLeaveStatusId;
    leaveApplication.leaveStatusUpdatedBy = this.currentUser.empId
    leaveApplication.rejectReason = leaveApplication.rejectReason?.trim();

    //console.log("leaveApplication : ", leaveApplication);

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
  onUpdateLeaveStatusNew(
  template: TemplateRef<any>,
  leaveApplication: any,
  updatedLeaveStatusId: number
) {
  this.cancelRequest();

  // 1 = Pending, 2 = Approved, 3 = Rejected
  leaveApplication.leaveStatusId = updatedLeaveStatusId;
  leaveApplication.leaveStatusUpdatedBy = this.currentUser.empId;
  leaveApplication.rejectReason = leaveApplication.rejectReason?.trim();

  // New fields for rejection flow
  leaveApplication.rejectionIds = this.selectedRejectionIds || [];

  this.leaveService.updateLeaveStatusNew(leaveApplication)
    .pipe(first())
    .subscribe({
      next: (response: any) => {
        if (response.serviceStatus === "Success") {
          this.countAllMyTeamsPendingLeaveApplicationsByManagerId();
          this.getAllMyTeamsPendingLeaveApplicationsByManagerId();
        }

        this.openAlertMod(template, response.serviceResponse);
        this.resetRejectModalData();
      },
      error: (error: any) => {
        console.error(error);
        this.openAlertMod(template, "Something went wrong.");
      }
    });
}
resetRejectModalData() {
  this.selectedRejectionIds = [];
  this.showOtherRemarks = false;
  this.leaveObj.rejectReason = '';
}

  onUpdateLeaveStatusCheck(template: TemplateRef<any>, updatedLeaveStatusId) {
    this.cancelRequest();
    // 1 = pending , 2 = Approved , 3= Rejected
    this.leaveApplication.leaveStatusId = updatedLeaveStatusId;
    this.leaveApplication.leaveStatusUpdatedBy = this.currentUser.empId
    this.leaveApplication.rejectReason = this.leaveApplication.rejectReason?.trim();

    //console.log("leaveApplication : ", this.leaveApplication);

    this.leaveService.updateLeaveStatus(this.leaveApplication).pipe(first()).subscribe((response: any) => {
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
  onSingleReject(template: TemplateRef<any>,) {
    this.leaveObj.rejectReason = this.leaveObj.rejectReason?.trim();
    if (!this.validationService.validateActivityTimesheetDiscription(this.leaveObj.rejectReason)) {
      this.alertMessage = "Please enter valid reason !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    this.onUpdateLeaveStatus(template, this.leaveObj, 3);
  }
  onSingleRejectNew(template: TemplateRef<any>,) {
    this.leaveObj.rejectReason = this.leaveObj.rejectReason?.trim();
    if (!this.validationService.validateActivityTimesheetDiscription(this.leaveObj.rejectReason)) {
      this.alertMessage = "Please enter valid reason !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    this.onUpdateLeaveStatusNew(template, this.leaveObj, 3);
  }

  getLeaveRejectionReasons() {
  this.leaveService.getLeaveRejectionReasons()
    .subscribe({
      next: (response: any) => {
        this.leaveRejectionReasonList = response;
        console.log('Rejection Reasons:', this.leaveRejectionReasonList);
      },
      error: (error: any) => {
        console.error(error);
      }
    });
}

  // openLeaveRejectModal
  openLeaveRejectModal(template: TemplateRef<any>, leave: any) {
    this.cancelRequest();
    this.leaveObj = leave
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-lg' });
  }



  //comOff Applications
  getPendingCompOffRequestsByManagerId() {
    this.data = ''
    this.allCompOffApplications = []
    this.isSelectAll = false
    this.bulkCompOffApprove = []
    this.bulkCompOffReject = []

    let compOff = new Leave();
    compOff.managerId = this.currentUser.empId;
    this.leaveService.getPendingCompOffRequestsByManagerId(compOff).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allCompOffApplications = response.serviceResponse;
        this.allCompOffApplications.forEach((compOff, index) => {
          compOff.checkId = "compOff" + index;
          compOff.fromDate = (compOff.fromDate) ? moment(compOff.fromDate).format(AppComponent.DATE_FORMAT) : null;
          compOff.toDate = (compOff.toDate) ? moment(compOff.toDate).format(AppComponent.DATE_FORMAT) : null;
          compOff.createdOn = (compOff.createdOn) ? moment(compOff.createdOn).format(AppComponent.DATE_FORMAT) : null;
          compOff.emp360 = compOff.empId;
          compOff.emp360Manager = compOff.managerId;
          compOff.emp360Level2Approver = compOff.level2ApproverId;
        });
        // console.log("allCompOffApplications : ", this.allCompOffApplications);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  /* Leave Applications Count
  *  Added by suraj 07/08/2022
  */
  countPendingCompOffRequestsByManagerId() {
    this.allCompOffApplications = []

    let compOff = new Leave();
    compOff.managerId = this.currentUser.empId;
    this.leaveService.countPendingCompOffRequestsByManagerId(compOff).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.compOffApplicationCount = response.serviceResponse.applicationCount;
        //console.log("CompOffApplicationCount : ", this.compOffApplicationCount);
      } else {
        this.compOffApplicationCount = 0;
        console.error(response.serviceResponse);
      }
    });
  }

  onUpdateCompOffStatus(template: TemplateRef<any>, compOffObj, updatedCompOffStatusId) {
    this.cancelRequest();
    // 1 = pending , 2 = Approved , 3= Rejected
    let compOff: Leave = new Leave();
    compOff = Object.assign({}, compOffObj);
    compOff.leaveStatusId = updatedCompOffStatusId;
    compOff.rejectCompOffReason = compOffObj.rejectReason;
    compOff.leaveStatusUpdatedBy = this.currentUser.empId

    this.leaveService.updateCompOffById(compOff).pipe(first()).subscribe((response: any) => {
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
  countMyReporteesTimesheetRequests() {
    this.allTeamTimesheetRequests = []

    let timesheet = new Timesheet();
    timesheet.managerId = this.currentUser.empId;
    this.timesheetService.countMyReporteesTimesheetRequests(timesheet).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.timesheetApplicationCount = response.serviceResponse.applicationCount;
      } else {
        this.timesheetApplicationCount = 0;
        console.error(response.serviceResponse);
      }
    });
  }

  getMyReporteesTimesheetRequests() {
    this.data = ''
    this.bulkApprove = []
    this.bulkReject = []
    this.allTeamTimesheetRequests = [];
    this.isSelectAll = false
    this.items = 10;

    let timesheetObj = new Timesheet();
    timesheetObj.managerId = this.currentUser.empId;
    timesheetObj.status = "Pending";
    timesheetObj.client=this.clientFilter;
    this.timesheetService.getMyReporteesTimesheetRequests(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allTeamTimesheetRequests = response.serviceResponse;
        console.log(this.allTeamTimesheetRequests);
        this.allTeamTimesheetRequests.forEach((timesheet, index) => {
          timesheet.checkId = "timesheet" + index;
          timesheet.employeementId = this.utilityService.getFormattedEmployeeId(timesheet);
          timesheet.date = (timesheet.date) ? moment(timesheet.date).format(AppComponent.DATE_FORMAT) : null;
          timesheet.officeInTime = (timesheet.officeInTime) ? moment(timesheet.officeInTime).format(AppComponent.DATETIME_FORMAT) : null;
          timesheet.officeOutTime = (timesheet.officeOutTime) ? moment(timesheet.officeOutTime).format(AppComponent.DATETIME_FORMAT) : null;
          timesheet.createdOn = (timesheet.createdOn) ? moment(timesheet.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          timesheet.emp360 = timesheet.empId;

        });
        //console.log("allTeamTimesheetRequests :", this.allTeamTimesheetRequests);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getDocsForPreview(docId:any){
      console.log(docId,":docId");
      this.resetPreviewState(); // added to reset all the zoom values
      this.timesheetService.getDocumentDataByDocId(docId).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          console.log(response.serviceResponse);
          this.docData = response.serviceResponse.docData;
          console.log(typeof(this.docData),":docDataType")
          this.mimeType = response.serviceResponse.docMimeType
          this.showPreview(this.docData,this.mimeType)
        }
      });
    }

    getFinalDocumentDataByDocId(timesheetId:any,docId:any){
      console.log(docId,":docId");
      this.timesheetService.getFinalDocumentDataByDocId(timesheetId,docId).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          console.log(response.serviceResponse);
          this.docData = response.serviceResponse.docData;
          console.log(typeof(this.docData),":docDataType")
          this.mimeType = response.serviceResponse.docMimeType
          this.showPreview(this.docData,this.mimeType)
        }
      });
    }
    showPreview(base64Data: string, mimeType: string) {
    const dataUrl = `data:${mimeType};base64,${base64Data}`;
      this.previewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(dataUrl);

      if (mimeType === 'application/pdf') {
        this.fileType = 'pdf';
      } else if (mimeType.startsWith('image/')) {
        this.fileType = 'image';
      } else {
        this.fileType = '';
      }


    // Open modal
    this.modalRef2 = this.modalService.open(this.previewModal, { modalDialogClass:'modal-lg' });
  }

  /* Approve / Reject Timesheet requests */
  updateTimesheetRequestById(template: TemplateRef<any>, timesheet: Timesheet, status: any) {
    this.cancelRequest();
    let timesheetObj = new Timesheet();
    timesheetObj.timesheetId = timesheet.timesheetId;
    timesheetObj.email = timesheet.email;
    timesheetObj.rejectReason = timesheet.rejectReason?.trim();
    timesheetObj.employeementId = timesheet.employeementId.substring(2);
    timesheetObj.employeeName = timesheet.employeeName;
    timesheetObj.managerId = this.currentUser.empId;
    timesheetObj.managerEmail = this.currentUser.email;
    timesheetObj.managerName = this.currentUser.name;
    timesheetObj.date = timesheet.date;
    timesheetObj.dayType = timesheet.dayType;
    timesheetObj.totalWorkingOfficeHours = timesheet.totalWorkingOfficeHours;
    timesheetObj.empId = timesheet.empId;
    //console.log("  timesheetObj.totalWorkingHours ", timesheet.totalWorkingHours)
    //console.log("  timesheetObj.totalWorkingOfficeHours ", timesheet.totalWorkingOfficeHours)
    timesheetObj.status = status;
    timesheetObj.timesheetStatusUpdatedBy = this.currentUser.empId;
    timesheetObj.rejectionId = this.selectedRejectReason;

    console.log("      :      ",timesheetObj)

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


  rejectTimesheetRequest(template: TemplateRef<any>,) {
    this.timesheetObj.rejectReason = this.timesheetObj.rejectReason?.trim()
    this.timesheetObj.empId = this.timesheetObj.empId;
     this.timesheetObj.rejectionId = this.selectedRejectReason;
    if (!this.validationService.validateActivityTimesheetDiscription(this.timesheetObj.rejectReason)) {
      this.alertMessage = "Please enter valid reason !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    this.updateTimesheetRequestById(template, this.timesheetObj, 'Rejected');
  }

  opnenRejectTimesheet(template: TemplateRef<any>, timesheet: any) {
    this.cancelRequest();
    this.timesheetObj = timesheet;
    this.modalRef = this.modalService.open(template, { modalDialogClass:'modal-lg' });
  }



  isEmployeeOnBench() {
    let obj = new Employee();
    obj.empId = this.currentUser.empId;
    obj.billableType = 'Bench';
    this.employeeService.isEmployeeOnBench(obj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        let dtoResponse = response.serviceResponse;
        this.lines = dtoResponse[0];
      }
    });
  }



  /* View TImesheet details */
  getAllMyActivitiesByTimesheetId(timesheet: any) {
    this.timesheetObj.allTimesheetActivities = [];

    let timesheetObj = new Timesheet();
    timesheetObj.timesheetId = timesheet.timesheetId;
    this.timesheetService.getAllMyActivitiesByTimesheetId(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.timesheetObj.allTimesheetActivities = response.serviceResponse;
        //console.log("timesheetObj.allTimesheetActivities :", this.timesheetObj.allTimesheetActivities);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  /* My Leave Details */
  getAllLeaveTypesByLeavePolicies(userObj: User) {
    this.leaveTypes = [];

    let leaveObj = new Leave();
    leaveObj.employmentStatus = userObj.employmentstatus;
    leaveObj.gender = userObj.gender;
    leaveObj.maritalStatus = this.currentUser.maritalStatus;

    this.leaveService.getAllLeaveTypesByLeavePolicies(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveTypes = response.serviceResponse;
        //console.log("leaveTypes : ", this.leaveTypes);
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

        //console.log("leaveBucketDetails : ", this.leaveBucketDetails);
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
    leaveObj.employeementId = this.currentUser.employeementId;
    leaveObj.empId = this.currentUser.empId;
    leaveObj.employmentStatus = this.currentUser.employmentstatus;
    if(this.currentUser.isApmosysProduct === 'true'){
      leaveObj.employeeType = 'Apmosys Product';
    }else{
       leaveObj.employeeType = 'Other';
    }

    console.log("sdnkvsvns" + leaveObj.employmentStatus);
    let leaveBalanceResponse: any = await this.leaveService.getMyLeaveBalancesByEmpId(leaveObj).pipe(first()).toPromise();
    if (leaveBalanceResponse.serviceStatus == "Success") {
      this.leaveBalanceList = leaveBalanceResponse.serviceResponse;
      //console.log("leaveBalanceList : ", this.leaveBalanceList);
      this.leaveBucketDetails.forEach(data => {
        let leaveDetail = this.leaveBalanceList.find((leave: Leave) => leave.leaveTypeCode == data.leaveTypeCode);
        if (leaveDetail) {
          data.balance = (leaveDetail.balance) ? leaveDetail.balance : 0;
        }
      });
      //console.log("leaveBucketDetails with Balance : ", this.leaveBucketDetails);
    } else {
      console.error(leaveBalanceResponse.serviceResponse);
    }

    let approvedLeaveResponse: any = await this.leaveService.countMyApprovedLeaveApplicationsByLeaveType(leaveObj).pipe(first()).toPromise();
    if (approvedLeaveResponse.serviceStatus == "Success") {
      this.approvedLeavesList = approvedLeaveResponse.serviceResponse;
      //console.log("approvedLeaves : ", this.approvedLeavesList);

      this.leaveBucketDetails.forEach(data => {
        let leaveDetail = this.approvedLeavesList.find((leave: Leave) => leave.leaveTypeCode == data.leaveTypeCode);
        if (leaveDetail) {
          data.approvedApplicationsCount = (leaveDetail.applicationCount) ? leaveDetail.applicationCount : 0;
        }
      });

      //console.log("leaveBucketDetails with Approved Leaves : ", this.leaveBucketDetails);

    } else {
      console.error(approvedLeaveResponse.serviceResponse);
    }

    let rejectedLeaveResponse: any = await this.leaveService.countMyRejectedLeaveApplicationsByLeaveType(leaveObj).pipe(first()).toPromise();
    if (rejectedLeaveResponse.serviceStatus == 'Success') {
      this.rejectedLeavesList = rejectedLeaveResponse.serviceResponse;
      //console.log("Rejected Leaves : ", this.rejectedLeavesList);

      this.leaveBucketDetails.forEach(data => {
        let leaveDetail = this.rejectedLeavesList.find((leave: Leave) => leave.leaveTypeCode == data.leaveTypeCode);
        if (leaveDetail) {
          data.rejectedApplicationsCount = (leaveDetail.applicationCount) ? leaveDetail.applicationCount : 0;
        }
      });

      //console.log("leaveBucketDetails with Rejected Leaves : ", this.leaveBucketDetails);
    } else {
      console.error(rejectedLeaveResponse.serviceResponse);
    }

    let pendingLeaveResponse: any = await this.leaveService.countMyPendingLeaveApplicationsByLeaveType(leaveObj).pipe(first()).toPromise();
    if (pendingLeaveResponse.serviceStatus == "Success") {
      this.pendingLeavesList = pendingLeaveResponse.serviceResponse;
      //console.log("pendingLeavesList : ", this.pendingLeavesList);

      this.leaveBucketDetails.forEach(data => {
        let leaveDetail = this.pendingLeavesList.find((leave: Leave) => leave.leaveTypeCode == data.leaveTypeCode);
        if (leaveDetail) {
          data.pendingApplicationsCount = (leaveDetail.applicationCount) ? leaveDetail.applicationCount : 0;
        }
      });

      //console.log("leaveBucketDetails with pending leaves : ", this.leaveBucketDetails);
    } else {
      console.error(pendingLeaveResponse.serviceResponse);
    }
  }

  countMyRejectedLeaveApplicationsByLeaveType() {
    this.rejectedLeavesList = [];

    let leaveObj = new Leave();
    leaveObj.empId = this.currentUser.empId;
    this.leaveService.countMyRejectedLeaveApplicationsByLeaveType(leaveObj).pipe(first()).subscribe((response: any) => {

      if (response.serviceStatus == 'Success') {
        this.rejectedLeavesList = response.serviceResponse;
        //console.log("Rejected Leaves : ", this.rejectedLeavesList);

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
    leaveObj.empId = this.currentUser.empId;
    this.leaveService.countMyApprovedLeaveApplicationsByLeaveType(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.approvedLeavesList = response.serviceResponse;
        //console.log("approvedLeaves : ", this.approvedLeavesList);

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
    leaveObj.empId = this.currentUser.empId;
    this.leaveService.countMyPendingLeaveApplicationsByLeaveType(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.pendingLeavesList = response.serviceResponse;
        //console.log("pendingLeavesList : ", this.pendingLeavesList);

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

  renderLeaveChart(chartName: any, chartId: any, chartData: any, labelName: any) {
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
        ['#63b598', '#008eff', '#f1a0ff', '#7260d8', '#fce877', '#84a3ff', '#b5e0d3', '#e535fc', '#7d9ff7', '#513d98',
          '#ffe2f8', '#00a0da', '#ffb380', '#f697c1', '#4ca2f9', '#ffa2bc', '#96e591', '#f1ae16', '#2f7b99',
          '#b259ab', '#ff8473', '#0086b3', '#00861f', '#00696c', '#d36647', '#c6f5e4', '#e7dbce', '#ccfeff', '#f5f3e9', '#f0f7f7'
        ],
    });
  }

  renderPlaceholderChart(chartName: any, chartId: any, errorMsg: any) {
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
            let chart: any = this,
              x,
              y;


            //check if label exist after window resize
            if (chart.label) {
              chart.label.destroy();
            };

            y = (chart.clipBox.height * 1.3);
            x = (chart.clipBox.width / 2.5);
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
        data: [{ name: 'data', y: 1 }]
      }],
      colors:
        ['#b0b0b0'],
    });
  }

  // Graphs
  renderTimesheetChart(chartName: any, chartId: any, chartData: any, labelName: any) {
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
        ['#63b598', '#008eff', '#f1a0ff', '#7260d8', '#fce877', '#84a3ff', '#b5e0d3', '#e535fc', '#7d9ff7', '#513d98',
          '#ffe2f8', '#00a0da', '#ffb380', '#f697c1', '#4ca2f9', '#ffa2bc', '#96e591', '#f1ae16', '#2f7b99',
          '#b259ab', '#ff8473', '#0086b3', '#00861f', '#00696c', '#d36647', '#c6f5e4', '#e7dbce', '#ccfeff', '#f5f3e9', '#f0f7f7'
        ],
    });
  }

  /* Today's Birthday List */
  getAllEmployeesBirthDayToday() {
    this.employeeService.getAllEmployeesBirthDayToday().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.birthdayList = response.serviceResponse;
        this.birthdayList.forEach((employee) => {
          employee.emp360 = employee.empId;
        });
        // console.log("birthdayList : ", this.birthdayList);
      } else {
        this.compOffApplicationCount = 0;
        console.error(response.serviceResponse);
      }
    });
  }

  getAllEmployeesWorkAnniversaryToday() {
    this.employeeService.getAllEmployeesWorkAnniversaryToday().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.workAnniversaryList = response.serviceResponse;
        this.workAnniversaryList.forEach((employee) => {

          // let matchingEmployee = this.employeesFor360.find(emp => emp.empId === employee.empId);

          // employee.emp360 = matchingEmployee ? matchingEmployee : {};
          employee.emp360 = employee.empId;
        });

      } else {
        this.compOffApplicationCount = 0;
        console.error(response.serviceResponse);
      }
    });
  }

  /* Quick Links */
  showApplyLeaveForm() {
    if (this.currentUser.isNew === "true") {

      this.bodyComponent.openChangePasswordOnFirstTimeLoggin();

    } else {
      this.router.navigate(['/user-leaves'],
        { queryParams: { tabName: 'leave-tab' }, queryParamsHandling: '' });
    }

  }

  showApplyCompOffForm() {
    if (this.currentUser.isNew === "true") {

      this.bodyComponent.openChangePasswordOnFirstTimeLoggin();

    } else {
      this.router.navigate(['/user-leaves'],
        { queryParams: { tabName: 'compOff-tab' }, queryParamsHandling: '' });
    }
  }

  showApplyTimesheetForm() {
    if (this.currentUser.isNew === "true") {

      this.bodyComponent.openChangePasswordOnFirstTimeLoggin();

    } else {
      this.router.navigate(['/user-timesheet'],
        { queryParams: { tabName: 'my-timesheet-tab' }, queryParamsHandling: '' });
    }
  }

  showHolidayList() {
    if (this.currentUser.isNew === "true") {

      this.bodyComponent.openChangePasswordOnFirstTimeLoggin();

    } else {
      this.router.navigate(['/user-leaves'],
        { queryParams: { tabName: 'holidays-tab' }, queryParamsHandling: '' });
    }
  }

  /* carousal Images */
  getAllEventPhotosForHome() {
    this.eventImages = [];
    this.isImagesLoaded = false;
    // document.getElementById('eventPhotosCarousel').style.display = 'none';

    this.imageService.getFirstEventPhotoForHome().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.eventImages = response.serviceResponse;
        //console.log("First image : ", this.eventImages);
        setTimeout(() => {
          this.loadImages(this.eventImages);
        }, 1000);
        this.getPhotosForHome();
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  getPhotosForHome() {
    this.imageService.getAllEventPhotosForHome().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        let images = response.serviceResponse;
        this.eventImages.push(...images);
        this.eventImages.sort((a, b) => a.photoOrder - b.photoOrder)
        //console.log("eventImages : ", this.eventImages);
        setTimeout(() => { this.loadImages(this.eventImages); }, 1000);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  loadImages(eventImages) {
    eventImages.forEach((photo, index) => {
      if (photo.imageBytes) {
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
  dateCompare(a, b) {
    const dateFormat = 'YYYY-MM-DD';
    return (moment(new Date(a.date)).format(dateFormat) < moment(new Date(b.date)).format(dateFormat)) ? -1 : 1;
  }

  getWeekDay(date: any): string {
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

  getTimesheetsForHomePageByEmpId(dateRange: any) {
    if (this.currentUser.isNew === "true") {

      this.bodyComponent.openChangePasswordOnFirstTimeLoggin();

    }

    this.timesheetDetails = [];
    const TOTAL_WORKING_HOURS_IN_DAY = 8;
    const currentDate = new Date();
    const dateFormat = 'YYYY-MM-DD';
    let fromDate: any;
    let toDate: any;
    let totaltimesheetDaysCount = 0;
    let filledTimesheetDetails = []

    let timesheetObj = new Timesheet();
    timesheetObj.empId = this.currentUser.empId;

    if (dateRange == 'Last 7 Days') {
      totaltimesheetDaysCount = 7;
      const DAY_IN_MS = 24 * 60 * 60 * 1000;
      fromDate = new Date(currentDate.getTime() - (1 * DAY_IN_MS));
      toDate = new Date(currentDate.getTime() - (7 * DAY_IN_MS));

      //console.log(`Last 7 Days : ${moment(fromDate).format(dateFormat)} -- ${moment(toDate).format(dateFormat)}`);
      timesheetObj.startDate = moment(toDate).format(dateFormat);
      timesheetObj.endDate = moment(fromDate).format(dateFormat);
    } else if (dateRange == 'This Month') {
      totaltimesheetDaysCount = moment(`${currentDate.getFullYear()}-${currentDate.getMonth()}`, "YYYY-MM").daysInMonth()
      fromDate = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1);
      toDate = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0);

      //console.log(`This Month : ${moment(fromDate).format(dateFormat)} -- ${moment(toDate).format(dateFormat)}`);
      timesheetObj.startDate = moment(fromDate).format(dateFormat);
      timesheetObj.endDate = moment(toDate).format(dateFormat);
    } else if (dateRange == 'Last Month') {
      totaltimesheetDaysCount = moment(`${currentDate.getFullYear()}-${currentDate.getMonth() - 1}`, "YYYY-MM").daysInMonth()

      fromDate = new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1);
      toDate = new Date(currentDate.getFullYear(), currentDate.getMonth(), 0);

      //console.log(`Last Month : ${moment(fromDate).format(dateFormat)} -- ${moment(toDate).format(dateFormat)}`);
      timesheetObj.startDate = moment(fromDate).format(dateFormat);
      timesheetObj.endDate = moment(toDate).format(dateFormat);
    }

    this.timesheetService.getTimesheetsForHomePageByEmpId(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        filledTimesheetDetails = response.serviceResponse;
        //console.log("filledTimesheetDetails : ", filledTimesheetDetails);
      } else {
        console.error(response.serviceResponse);
      }

      let pendingCount = 0;
      let approvedCount = 0;
      let rejectedCount = 0;
      let notFilledCount = 0;

      for (
          let date = moment(timesheetObj.startDate);
          date.isSameOrBefore(timesheetObj.endDate);
          date.add(1, 'days')
        ) {
          const currentDateStr = date.format(dateFormat);

          const rows = filledTimesheetDetails.filter(
            t => t.date === currentDateStr
          );

          // ===== LAST 7 DAYS (single row per date) =====
          if (dateRange === 'Last 7 Days') {
            const ts = rows[0]; // first is enough

            if (ts) {
              const clone = { ...ts } as Timesheet;
              // clone.totalWorkingHoursPercentage =
              //   (clone.totalWorkingHours / TOTAL_WORKING_HOURS_IN_DAY) * 100 + '%';
              clone.totalWorkingHours = this.roundToTwo(clone.totalWorkingHours || 0);

              clone.totalWorkingHoursPercentage =
                this.roundToTwo(
                  (clone.totalWorkingHours / TOTAL_WORKING_HOURS_IN_DAY) * 100
                ) + '%';
              this.timesheetDetails.push(clone);

              if (clone.status === 'Pending') pendingCount++;
              else if (clone.status === 'Approved') approvedCount++;
              else if (clone.status === 'Rejected') rejectedCount++;
            } else {
              const empty = new Timesheet();
              empty.date = currentDateStr;
              empty.status = 'Not Filled';
              empty.dayType = 'Not Filled';
              empty.weekDayName = this.getWeekDay(currentDateStr);
              empty.totalWorkingHoursPercentage = '0%';

              this.timesheetDetails.push(empty);
              notFilledCount++;
            }

            continue;
          }

          // ===== MONTH / OTHER VIEWS (multiple rows per date) =====
          if (rows.length > 0) {
            rows.forEach(ts => {
              const clone = { ...ts } as Timesheet;
              clone.totalWorkingHours = this.roundToTwo(clone.totalWorkingHours || 0);

          clone.totalWorkingHoursPercentage =
            this.roundToTwo(
              (clone.totalWorkingHours / TOTAL_WORKING_HOURS_IN_DAY) * 100
            ) + '%';

              this.timesheetDetails.push(clone);

              if (clone.status === 'Pending') pendingCount++;
              else if (clone.status === 'Approved') approvedCount++;
              else if (clone.status === 'Rejected') rejectedCount++;
            });
          } else {
            const empty = new Timesheet();
            empty.date = currentDateStr;
            empty.status = 'Not Filled';
            empty.dayType = 'Not Filled';
            empty.weekDayName = this.getWeekDay(currentDateStr);
            empty.totalWorkingHoursPercentage = '0%';

            this.timesheetDetails.push(empty);
            notFilledCount++;
          }
        }


      //console.log("timesheetDetails : ", this.timesheetDetails);
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

      // this.renderTimesheetChart(`${dateRange} Timesheet`, 'totalEODChart', timesheetChartData, 'Timesheet(s)');
      if (dateRange == 'This Month')
        this.thisMonthCalendar.addTimesheetDetails();
      else if (dateRange == 'Last Month')
        this.lastMonthCalendar.addTimesheetDetails();
    });



  }

private roundToTwo(num: number): number {
  return Math.round((num + Number.EPSILON) * 100) / 100;
}
  //Employee Info Update
  openUpdateInfo(template: TemplateRef<any>) {
    this.modalRef = this.modalService.open(template, { modalDialogClass:'modal-xl' });
  }

  onDocSubmit() {
    this.cancelRequest();
  }

  // Employee Proile Completed Percentage
  getEmployeeProfileCompletion() {
    this.profileCompletedPercentage = 0;

    let employee = new Employee();
    employee.empId = this.currentUser.empId;
    this.employeeService.getEmployeeProfileCompletion(employee).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        let employeeObj = response.serviceResponse;
        this.profileCompletedPercentage = employeeObj.profileCompletedPercent + "%";
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  //export to excel

  exportToExcelForLeave() {
    this.excelName = 'leaveApplication.xlsx';

    const onlySpecificDataArr: any = this.leaveApplicationList.map(
      x => ({
        "Name": x.employeeName,
        "Leave Type": x.leaveType,
        "From Date": x.fromDate,
        "To Date": x.toDate,
        "Duration": (x.noOfDays + " day(s)"),
        "Status": x.status,
        "Applied By": x.createdByName,
        "Applied On": x.createdOn,
        "Reason": x.reason
      })
    )
    this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
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
    this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
  }

  exportToExcelForTimesheet() {
    this.excelName = 'AllTeamTimeSheetRequest.xlsx';

    const onlySpecificDataArr: any = this.allTeamTimesheetRequests.map(
      x => ({
        "Employee Id": x.employeementId,
        "Name": x.employeeName,
        "Date": x.date,
        "Day Type": x.dayType,
        "Activity": x.description?.replaceAll('<br>', ' \n'),
        "Applied By": x.createdByName,
        "Working Hours": x.totalTime,
        "Office In Time": x.officeInTime,
        "Office Out Time": x.officeOutTime,
        "Total Office Working Hours": x.totalWorkingOfficeHours,
        "Shift Type": x.isNightShift == 'true' ? 'Night Shift' : 'Regular Shift',
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
  openTimesheetDetailsModal(template: TemplateRef<any>, timesheetObj: Timesheet) {
    this.cancelRequest();

    this.timesheetObj = new Timesheet();
    this.timesheetObj = timesheetObj;
    this.getAllMyActivitiesByTimesheetId(this.timesheetObj);
    this.modalRef = this.modalService.open(template, { modalDialogClass:'modal-xl' });
  }

  openNotificationMod(template: TemplateRef<any>) {
    this.modalRef = this.modalService.open(template, { modalDialogClass:'modal-lg' });
  }

  openPoExpiredMod() {
    this.cancelRequest();
    this.geExpiredtPoData();
    console.log(this.expiredList);
    // alert("hi");
    this.modalRef = this.modalService.open(this.poExpireTemplateRef, {
      modalDialogClass: 'modal-xl',
      backdrop: 'static',
      keyboard: false
    });

  }

  openReqMod(template: TemplateRef<any>) {
    if (this.currentUser.isNew === "true") {

      this.bodyComponent.openChangePasswordOnFirstTimeLoggin();

    } else {
      this.filters = {};
      this.isSearchEnabled = false;
      this.modalRef = this.modalService.open(template, { modalDialogClass:'modal-xl' });
    }

  }


  openRevokeLeaveRejectModalCompOff(template: TemplateRef<any>, leave: any) {
    this.cancelRequest();
    this.leaveObj = leave;
    this.modalRef = this.modalService.open(template);
  }
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass:'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef?.close();
  }
  cancelRequest1() {
    this.modalRef2.close();
  }

  openUpdateProjectCompletionModal(message: string): void {
    this.modalMessage = message;
    this.milestoneExpireValidationPupupModalRef = this.modalService.open(this.milestoneExpireValidationPupup, {
      modalDialogClass: 'modal-dialog modal-sm modal-position-top'
    });
  }

  closeUpdateProjectCompletionModal(): void {
    if (this.milestoneExpireValidationPupupModalRef) {
      this.milestoneExpireValidationPupupModalRef?.close();
    }
    if(this.isUpdated){
  window.location.reload();
    }
   this.isUpdated=false;
  }

  selectAll(event) {
    this.bulkApprove = [];
    this.bulkReject = [];

    const checkboxes = document.querySelectorAll('.timesheet-req-checkbox');

    //console.log("select Data : ", this.allTeamTimesheetRequests);
    //console.log("checkboxes    ::   ",checkboxes);

    checkboxes.forEach((checkbox: any) => {
      //console.log("checkbox : ", checkbox);
      let checkboxIndex = checkbox.getAttribute('id');
      let checkedTimesheet = this.allTeamTimesheetRequests.find((_timesheet, index) => _timesheet.checkId == checkboxIndex);

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
    //console.log("leaveCheckboxes     ::   ",leaveCheckboxes);
    leaveCheckboxes.forEach((leaveCheck: any) => {
      //console.log("Check in leave home ", leaveCheck);
      let leaveCheckboxIndex = leaveCheck.getAttribute('id');
      let checkedLeaveApplication = this.leaveApplicationList.find((_leave, index) => _leave.checkId == leaveCheckboxIndex);

      if (event.target.checked) {
        leaveCheck.checked = true;
        leaveCheck.classList.add('checked');
        this.bulkLeaveApprove.push(checkedLeaveApplication);
        this.bulkLeaveReject.push(checkedLeaveApplication);
      } else {
        leaveCheck.checked = false;
        leaveCheck.classList.remove('checked');
        this.bulkLeaveApprove.forEach((leave, index) => {
          if (leave == checkedLeaveApplication) this.bulkLeaveApprove.splice(index, 1);
        });
        this.bulkLeaveReject.forEach((leave, index) => {
          if (leave == checkedLeaveApplication) this.bulkLeaveReject.splice(index, 1);
        });
      }

    });

  }
  selectAllCompOff(event) {
    const compOffCheckBox = document.querySelectorAll('.compoff-req-checkbox');

    //console.log("select Data : ", compOffCheckBox);

    compOffCheckBox.forEach((checkbox: any) => {
      //console.log("checkbox : ", checkbox);
      let checkboxIndex = checkbox.getAttribute('id');
      let checkedCompOff = this.allCompOffApplications.find((_compoff, index) => _compoff.checkId == checkboxIndex);
      //console.log("checkedCompOff   ::   ",checkedCompOff);
      if (event.target.checked) {
        checkbox.checked = true;
        checkbox.classList.add('checked');
        this.bulkCompOffApprove.push(checkedCompOff);
        this.bulkCompOffReject.push(checkedCompOff);
      } else {
        checkbox.checked = false;
        checkbox.classList.remove('checked');
        this.bulkCompOffApprove.forEach((compOff, index) => {
          if (compOff == checkedCompOff) this.bulkCompOffApprove.splice(index, 1);
        });
        this.bulkCompOffReject.forEach((compOff, index) => {
          if (compOff == checkedCompOff) this.bulkCompOffReject.splice(index, 1);
        });
      }
    });
  }

  select(timesheetObj, event) {

    //console.log("clicked on : ", timesheetObj);

    if (event.target.checked) {
      event.target.classList.add('checked');
      this.bulkApprove.push(timesheetObj);
      this.bulkReject.push(timesheetObj);
    } else {
      event.target.classList.remove('checked');
      const checkboxes = document.querySelectorAll('.timesheet-req-checkbox.checked');
      if (checkboxes.length !== this.items) this.isSelectAll = false;
      //console.log("Checkboxes.length ", checkboxes.length)
      //console.log(" items ", this.items)
      this.bulkApprove.forEach((timesheet, index) => {
        if (timesheet == timesheetObj) this.bulkApprove.splice(index, 1);
      });
      this.bulkReject.forEach((timesheet, index) => {
        if (timesheet == timesheetObj) this.bulkReject.splice(index, 1);
      });
    }
    //console.log("Updated Bulk List : ", this.bulkApprove);
  }

  selectCompOff(compOffObj, event) {

    // compoff-req-checkbox
    //console.log("compOff bulk method call clicked on ",compOffObj);
    if (event.target.checked) {
      event.target.classList.add('checked');
      this.bulkCompOffApprove.push(compOffObj);
      this.bulkCompOffReject.push(compOffObj);
    } else {
      event.target.classList.remove('checked');
      const compOffLeaveCheckBox = document.querySelectorAll('.compoff-req-checkbox.checked');
      if (compOffLeaveCheckBox.length !== this.items) this.isSelectAll = false;
      //console.log("checkbox.length of comp off  ",compOffLeaveCheckBox.length);
      this.bulkCompOffApprove.forEach((compOff, index) => {
        if (compOff == compOffObj) this.bulkCompOffApprove.splice(index, 1);
      });
      this.bulkCompOffReject.forEach((compOff, index) => {
        if (compOff == compOffObj) this.bulkCompOffReject.splice(index, 1);
      });
      //console.log("Updated Bulk List : ", this.bulkCompOffApprove);
    }
  }


  onSelect(leaveObj, event) {

    //console.log("clicked on : ", leaveObj);

    if (event.target.checked) {
      event.target.classList.add('checked');
      this.bulkLeaveApprove.push(leaveObj);
      this.bulkLeaveReject.push(leaveObj);
    } else {
      event.target.classList.remove('checked');
      const leaveCheckboxes = document.querySelectorAll('.homeLeave-req-checkbox.checked');
      if (leaveCheckboxes.length !== this.items) this.isSelectAll = false;
      //console.log("checkbox.length ", leaveCheckboxes.length)
      this.bulkLeaveApprove.forEach((timesheet, index) => {
        if (timesheet == leaveObj) this.bulkLeaveApprove.splice(index, 1);
      });
      this.bulkLeaveReject.forEach((timesheet, index) => {
        if (timesheet == leaveObj) this.bulkLeaveReject.splice(index, 1);
      });
    }
    //console.log("Updated Bulk List : ", this.bulkLeaveApprove);
  }

  openBulkApprovalModal(nightShiftTemplate: TemplateRef<any>, alertTemplate: TemplateRef<any>) {
    const isNightShiftFound = this.bulkApprove.filter((x) => x.isNightShift == "true");
    //console.log(isNightShiftFound, " : isNightShiftFound");

    if (isNightShiftFound.length != 0) {
      this.modalRef = this.modalService.open(nightShiftTemplate, { modalDialogClass:'modal-lg' });
    } else {
      this.onBulkApproval(alertTemplate);
    }
  }

  bulkApproveWithoutNightShiftRequest(template: TemplateRef<any>) {
    this.bulkApprove = this.bulkApprove.filter((x) => x.isNightShift == "false" || x.isNightShift == null);
    if (this.bulkApprove.length !== 0) {
      this.onBulkApproval(template);
    } else {
      this.cancelRequest();
      this.getMyReporteesTimesheetRequests();
    }
  }

  openBulkRejectModal(nightShiftTemplate: TemplateRef<any>, bulkRejectTimesheet: TemplateRef<any>) {
    const isNightShiftFound = this.bulkApprove.filter((x) => x.isNightShift == "true");
    //console.log(isNightShiftFound, " : isNightShiftFound");

    if (isNightShiftFound.length != 0) {
      this.modalRef = this.modalService.open(nightShiftTemplate, { modalDialogClass:'modal-lg' });
    } else {
      this.OnBulkReject(bulkRejectTimesheet);
    }
  }

  bulkRejectWithoutNightShiftRequest(bulkRejectTimesheet: TemplateRef<any>) {
    this.timesheetObj.rejectReason = null;
    this.bulkReject = this.bulkApprove.filter((x) => x.isNightShift == "false" || x.isNightShift == null);
    if (this.bulkReject.length !== 0) {
      this.OnBulkReject(bulkRejectTimesheet);
    } else {
      this.cancelRequest();
      this.getMyReporteesTimesheetRequests();
    }
  }

  onBulkApproval(template: TemplateRef<any>) {
    this.cancelRequest();
    let timesheetObj = new Timesheet();
    timesheetObj.bulkApprovedList = this.bulkApprove;
    timesheetObj.updatedBy = this.currentUser.empId;

    timesheetObj.status = "Approved"

    timesheetObj.bulkApprovedList.forEach((x) => {
      x.employeementId = x.employeementId.substring(2);
      x.updatedBy = this.currentUser.empId;

    })
    this.timesheetService.bulkApproveTimesheetRequest(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, "All Selected Timesheets Approved Successfully ");
        this.countMyReporteesTimesheetRequests();
        this.getMyReporteesTimesheetRequests();
        this.bulkApprove = [];
        this.bulkReject = [];
      } else {
        console.error(response.serviceResponse)
      }
    });

  }

  onBulkRejectTimesheet(template: TemplateRef<any>) {
    this.timesheetObj.rejectReason = this.timesheetObj.rejectReason?.trim();
    this.timesheetObj.empId = this.timesheetObj.empId;
    if (!this.validationService.validateActivityTimesheetDiscription(this.timesheetObj.rejectReason)) {
      this.alertMessage = "Please enter Valid Reason !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    //console.log("Updated Bulk List : ", this.bulkReject);
    let timesheetObj = new Timesheet();
    timesheetObj.bulkRejectList = this.bulkReject;
    timesheetObj.updatedBy = this.currentUser.empId;
    timesheetObj.rejectReason = this.timesheetObj.rejectReason?.trim();
    //console.log(" timesheet reason :  ", timesheetObj.rejectReason);
    timesheetObj.status = "Rejected"
    timesheetObj.rejectionId = this.selectedRejectReason;
    //console.log("For Bulk Update : ", timesheetObj);
    timesheetObj.bulkRejectList.forEach((item) => {
      item.employeementId = item.employeementId.substring(2);
    })
    this.timesheetService.bulkRejectTimesheetRequest(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, "All Selected Timesheets Rejected Successfully ");
        this.bulkApprove = [];
        this.bulkReject = [];
        this.timesheetApplicationCount;
        this.countMyReporteesTimesheetRequests();
        this.getMyReporteesTimesheetRequests();
      } else {
        console.error(response.serviceResponse)
      }
    });

  }


  // homeLeave-req-checkbox




  onBulkLeaveApproval(template: TemplateRef<any>) {
    //console.log("Updated Bulk List : ", this.bulkLeaveApprove);
    let leaveObj = new Leave();
    leaveObj.bulkLeaveApprovedList = this.bulkLeaveApprove;
    leaveObj.leaveStatusUpdatedBy = this.currentUser.empId;
    leaveObj.approverEmail = this.currentUser.email;

    leaveObj.leaveStatusId = 2;

    this.leaveService.bulkApproveLeaveRequest(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, "All Selected Leaves Approved Successfully ");

        this.getAllMyTeamsPendingLeaveApplicationsByManagerId()
        this.countAllMyTeamsPendingLeaveApplicationsByManagerId()

        this.bulkLeaveApprove = [];
        this.bulkLeaveReject = [];
      } else {
        console.error(response.serviceResponse)
      }
    });

  }

  bulkRejectLeave(template: TemplateRef<any>) {

    this.leaveObj.rejectReason = this.leaveObj.rejectReason?.trim();
    if (!this.validationService.validateActivityTimesheetDiscription(this.leaveObj.rejectReason)) {
      this.alertMessage = "please enter valid reason !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    let leaveObj = new Leave();
    leaveObj.bulkLeaveRejectList = this.bulkLeaveReject;
    //console.log(" ............................ ", leaveObj.bulkLeaveRejectList)
    leaveObj.leaveStatusUpdatedBy = this.currentUser.empId;
    leaveObj.leaveStatusId = 3
    leaveObj.approverEmail = this.currentUser.email;
    leaveObj.rejectReason = this.leaveObj.rejectReason?.trim();
    leaveObj.bulkLeaveRejectList.forEach((y) => {
      y.employeementId = y.employeementId;
    })

    this.leaveService.bulkRejectLeaveRequest(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, "All Selected Leaves Rejected Successfully ");
        this.getAllMyTeamsPendingLeaveApplicationsByManagerId()
        this.countAllMyTeamsPendingLeaveApplicationsByManagerId()
        this.bulkLeaveApprove = [];
        this.bulkLeaveReject = [];

      } else {
        console.error(response.serviceResponse)
      }
    });

  }

  bulkRejectLeaveNew(template: TemplateRef<any>) {

  this.leaveObj.rejectReason = this.leaveObj.rejectReason?.trim();

  if (this.showOtherRemarks &&
      !this.leaveObj.rejectReason) {
    this.alertMessage = "Please enter remarks for Other reason.";
    this.openAlertMod(template, this.alertMessage);
    return;
  }

  let leaveObj = new Leave();

  leaveObj.bulkLeaveRejectList = this.bulkLeaveReject;
  leaveObj.leaveStatusUpdatedBy = this.currentUser.empId;
  leaveObj.leaveStatusId = 3;
  leaveObj.approverEmail = this.currentUser.email;
  leaveObj.rejectReason = this.leaveObj.rejectReason;
  leaveObj.rejectionIds = this.selectedRejectionIds;

  this.leaveService.bulkRejectLeaveRequestNew(leaveObj)
    .pipe(first())
    .subscribe({
      next: (response: any) => {
        if (response.serviceStatus == "Success") {

          this.openAlertMod(
            template,
            "All Selected Leaves Rejected Successfully"
          );

          this.getAllMyTeamsPendingLeaveApplicationsByManagerId();
          this.countAllMyTeamsPendingLeaveApplicationsByManagerId();

          this.bulkLeaveApprove = [];
          this.bulkLeaveReject = [];
          this.resetRejectModalData();
        }
      },
      error: (error: any) => {
        console.error(error);
      }
    });
}




  onBulkCompOffReject(template: TemplateRef<any>, leave) {
    this.leaveObj.rejectCompOffReason = ''
    this.cancelRequest();
    this.leaveObj = leave
    this.modalRef = this.modalService.open(template, { modalDialogClass:'modal-lg' });
  }

  onBulkCompOffApprove(template: TemplateRef<any>) {
    //console.log(" bulk approve compoff call ::  ");
    let compOffObj = new Leave();
    compOffObj.bulkLeaveApprovedList = this.bulkCompOffApprove;
    compOffObj.leaveStatusUpdatedBy = this.currentUser.empId;
    compOffObj.approverEmail = this.currentUser.email;
    compOffObj.leaveStatusId = 2;
    this.leaveService.bulkCompOffApprove(compOffObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, "All Selected CompOff Leaves Approved Successfully ");
        this.getPendingCompOffRequestsByManagerId();
        this.countPendingCompOffRequestsByManagerId();
        this.bulkCompOffApprove = [];
        this.bulkCompOffReject = [];

      } else {
        console.error(response.serviceResponse);
      }
    });
  }



  bulkCompOffRejectLeave(template: TemplateRef<any>) {
    this.leaveObj.rejectCompOffReason = this.leaveObj.rejectCompOffReason?.trim();
    if (!this.validationService.validateActivityTimesheetDiscription(this.leaveObj.rejectCompOffReason)) {
      this.alertMessage = "please enter valid reason !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    let compOffObj = new Leave();
    compOffObj.bulkLeaveRejectList = this.bulkCompOffReject;
    //console.log("-------------------------n   ",compOffObj.bulkLeaveRejectList);
    compOffObj.leaveStatusUpdatedBy = this.currentUser.empId;
    compOffObj.leaveStatusId = 3;
    compOffObj.approverEmail = this.currentUser.email;
    compOffObj.rejectCompOffReason = this.leaveObj.rejectCompOffReason?.trim();
    compOffObj.bulkLeaveRejectList.forEach((compOff) => {
      compOff.employeementId = compOff.employeementId;
    });
    this.leaveService.bulkCompOffReject(compOffObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, "All Selected CompOff Leaves Rejected Successfully ");
        this.getPendingCompOffRequestsByManagerId();
        this.countPendingCompOffRequestsByManagerId();
        this.bulkCompOffApprove = [];
        this.bulkCompOffReject = [];

      } else {
        console.error(response.serviceResponse);
      }
    });

  }

  OnBulkReject(template: TemplateRef<any>) {
    let timesheet = new Timesheet();
    this.cancelRequest();
    this.timesheetObj = timesheet;
    this.modalRef = this.modalService.open(template, { modalDialogClass:'modal-lg' });
  }


  OnBulkLeaveReject(template: TemplateRef<any>, leave) {
    this.leaveObj.rejectReason = ''
    this.cancelRequest();
    this.leaveObj = leave
    this.modalRef = this.modalService.open(template, { modalDialogClass:'modal-lg' });
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

    if (this.password) {
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
  }

  openChangePassword(changePasswordTemplate) {
    this.errorMsg = ''
    this.password = ''
    this.oldPasswordValid = false;
    this.newpassword = ''
    this.userNewPass = ''
    //console.log(this.currentUser.isNew)
    if (this.currentUser.isNew == 'true') {
      this.modalRef = this.modalService.open(changePasswordTemplate, this.config);
    } else {
      this.modalRef = this.modalService.open(changePasswordTemplate);
    }
  }

  openChangePasswordOnFirstTimeLoggin() {
    this.openChangePassword(this.changePasswordTemplate);
  }

  // setTimeout(() => {
  //   // Redirect to the desired location
  //   location.reload();
  // }, 5000);


  // TOP BAR
  userLogout(template?: TemplateRef<any>) {
    let user = new User();
    user.empId = this.currentUser.empId;
    this.authenticationService.logoutUser(user).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.authenticationService.stopUserSessionCheck();
        //console.log(response.serviceResponse);
        sessionStorage.removeItem('currentUser');
        sessionStorage.removeItem('token');
        sessionStorage.removeItem('logInfo');
        sessionStorage.removeItem('maxFileSize');
        sessionStorage.removeItem('maxRequestSize');
        sessionStorage.removeItem('sessioncheck');
        sessionStorage.removeItem('breadcrumb');
        // delete method call for cookies
        this.authenticationService.deleteCookies();
        this.authenticationService.setcurrentUserSubject(null);

        this.router.navigate(['/login']);
        setTimeout(() => { location.reload(); });

      } else {
        if (response.serviceResponse == "Session already destroyed") {
          this.authenticationService.stopUserSessionCheck();
          sessionStorage.removeItem('currentUser');
          sessionStorage.removeItem('token');
          sessionStorage.removeItem('logInfo');
          sessionStorage.removeItem('maxFileSize');
          sessionStorage.removeItem('maxRequestSize');
          sessionStorage.removeItem('sessioncheck');
          sessionStorage.removeItem('breadcrumb');
          // delete method call for cookies
          this.authenticationService.deleteCookies();
          this.authenticationService.setcurrentUserSubject(null);
          this.router.navigate(['/login']);
          setTimeout(() => { location.reload(); });
        }
        console.error(response.serviceResponse);
      }
    });

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
      this.errorMsg = 'Password should not be set less than 8 characters and at least 1 lowercase character,  1 uppercase character, 1 digit , 1 special character should be there. Allowed Special characters are !@#$%^&*';
      return;
    }

    if (this.userNewPass == this.newpassword) {
      this.user.email = this.currentUser.email;
      this.user.password = this.setEncryption("PkdtRsJidheGitvS", this.password);
      this.user.newPassword = this.setEncryption("PkdtRsJidheGitvS", this.newpassword);

      this.employeeService.updateEmployeePassword(this.user).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.cancelRequest();
          this.passreset();

          this.modalRef = this.modalService.open(this.LoadingLoginTemplate);
          setTimeout(() => {
            this.cancelRequest();
            this.userLogout();
          }, 2000);

        }
        else {
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

  //Notification Consent

  getAllNotifications() {
    this.notificationObj = new NotificationMessage();

    this.notificationService.getAllNotifications().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allNotification = response.serviceResponse;

        this.allNotification.forEach((notification) => {
          notification.createdOn = (notification.createdOn) ? moment(notification.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          notification.updatedOn = (notification.updatedOn) ? moment(notification.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
        });

        this.allNotification = this.allNotification.filter(x => x.isActive == 'true' && x.notificationType != "consentNotification" && x.notificationType != "releaseNotes");

        //console.log("notificationList : ", this.allNotification);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  openConsentNotificationModal() {
    //console.log(this.currentUser.notificationConsent, " : this.currentUser.notificationConsent");

    if (this.currentUser.notificationConsent != null || this.currentUser.notificationConsent != undefined) {
      this.consentNotificationMessage = this.currentUser.notificationConsent.notificationMessage;
      // this.modalRef = this.modalService.open(this.consentNotificationTemplate, this.consentModalConfig);
    }
  }

  submitNotificationConsent() {
    // this.cancelRequest();

    let notificationObj = new NotificationMessage();

    notificationObj.empId = this.currentUser.empId;
    notificationObj.notificationId = this.currentUser.notificationConsent.notificationId;
    notificationObj.notificationType = this.currentUser.notificationConsent.notificationType;
    this.notificationService.submitNotificationConsent(notificationObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        let dtoResponse = response.serviceResponse;
        this.currentUser.notificationConsent = dtoResponse.notificationConsent;

        this.authenticationService.setcurrentUserSubject(this.currentUser);
        this.openConsentNotificationModal();
      }
    });
  }


  // Release Note Consent
  setReleaseNote() {
    this.isShowReleaseNote = false;

    //console.log(this.currentUser.releaseNoteNotification, " : releaseNoteNotification");

    if (this.currentUser.releaseNoteNotification != null || this.currentUser.releaseNoteNotification != undefined) {
      this.releaseNoteText = this.currentUser.releaseNoteNotification.notificationMessage;
      this.isShowReleaseNote = true;
    }
  }


  submitReleaseNoteNotificationConsent() {
    let notificationObj = new NotificationMessage();

    notificationObj.empId = this.currentUser.empId;
    notificationObj.notificationId = this.currentUser.releaseNoteNotification.notificationId;
    notificationObj.notificationType = this.currentUser.releaseNoteNotification.notificationType;
    this.notificationService.submitNotificationConsent(notificationObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        let dtoResponse = response.serviceResponse;
        this.currentUser.releaseNoteNotification = dtoResponse.releaseNoteNotification;
        this.authenticationService.setcurrentUserSubject(this.currentUser);
        this.setReleaseNote()
      }
    });
  }


  sortData(sort: Sort) {
    //console.log(sort);
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

  toggleSearch() {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
    }
  }

  onSearch(searchData) {
    this.filters = searchData;

    //console.log("Updated Filter : ", this.filters);
  }

  // added by anurag
  onUpdateLeave(template: TemplateRef<any>, leaveApplication) {
    //console.log("leaveApplication ",leaveApplication);
    this.modalRef = this.modalService.open(template, { modalDialogClass:'modal-lg' });
    this.leaveApplication = leaveApplication;
    this.findOverLapsLeaveForManager();
  }

  active: boolean = false;
  findOverLapsLeaveForManager() {
    let leaveApp = new Leave();

    const dateFormat = 'YYYY-MM-DD';

    leaveApp.managerId = this.currentUser.empId;
    leaveApp.empId = this.leaveApplication.empId;
    // leaveApp.fromDate = moment(this.leaveApplication.fromDate).format(dateFormat);
    // leaveApp.toDate = moment(this.leaveApplication.toDate).format(dateFormat);

    leaveApp.fromDate = this.leaveApplication.fromDate;
    leaveApp.toDate = this.leaveApplication.toDate;
    leaveApp.status = this.leaveApplication.status;

    //console.log(leaveApp);
    this.overLapsLeaveForManager = [];

    this.leaveService.getOverLapsLeaveForManager(leaveApp).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.overLapsLeaveForManager = response.serviceResponse;
        // const empData = sessionStorage.getItem('AllEmployees');
        // if (empData) {
        //   this.employeeList = JSON.parse(empData);
        // } else {
        //   this.employeeList = [];
        // }
        // for(let y of this.overLapsLeaveForManager){
        //   let matchingEmployee = this.employeeList.find(emp => emp.employeementId === y.employeementId);
        //   y.emp360 = matchingEmployee ? matchingEmployee : {};
        // }
        console.log("this.getOverLapsLeaveForManager ", this.overLapsLeaveForManager);
      }
    })


  }

  fetchRewardCategoryForHomePage() {
    this.rewardsService.fetchRewardCategoryForHomePage().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == 'Success') {
        this.rewardCategoryList = response.serviceResponse;
         const order = ['Quarterly', 'Half Yearly', 'Annual'];


      this.rewardCategoryList = response.serviceResponse
        .filter(cat => order.includes(cat.categoryName.trim()))
        .sort((a, b) => order.indexOf(a.categoryName.trim()) - order.indexOf(b.categoryName.trim()));
        if (this.rewardCategoryList.length > 0) {
          this.onCategoryTabClick(this.rewardCategoryList[0]);
        } else {
          console.error(response.serviceResponse);
        }
      }
    })
  }

  onCategoryTabClick(category: any) {
    this.selectedCategoryId = category.rewardCategoryId;
    this.selectedCategoryName = category.categoryName;
    this.showEmptyMessage = false;
    this.rewardsList = [];
    this.fetchEmployeesForHomepageByCategoryId(this.selectedCategoryId);
  }

  fetchEmployeesForHomepageByCategoryId(categoryId: number) {
    this.rewardCategoryObj.rewardCategoryId = categoryId;
    this.rewardsService.fetchEmployeesForHomepageByCategoryId(this.rewardCategoryObj).subscribe((response: any) => {
      if (response.serviceStatus === 'Success') {
        this.rewardsList = response.serviceResponse;

        this.rewardsList.forEach((employee) => {
          employee.emp360 = employee.rewardedTo;
        });


        this.groupRewardsByMonth();
        setTimeout(() => {

          this.startScrolling();

          if (this.rewardsList.length === 0) {
            this.showEmptyMessage = true;
          }
        }, 500);

      } else {
        this.showEmptyMessage = true;
        console.error(response.serviceResponse);
      }
    });
  }


  groupRewardsByMonth() {
    this.groupedRewards = this.rewardsList.reduce((groups: any, reward: any) => {
      if (!groups[reward.ofMonthYear]) {
        groups[reward.ofMonthYear] = [];
      }
      groups[reward.ofMonthYear].push(reward);
      return groups;
    }, {});

    this.monthKeys = Object.keys(this.groupedRewards).sort();

    if (this.monthKeys.length > 0) {
      this.selectedMonth = this.monthKeys[0];
    }

    this.currentMonthIndex = 0;
    this.updateCurrentRewards();

    this.startScrolling();
  }

  updateCurrentRewards() {
    const currentMonth = this.monthKeys[this.currentMonthIndex];

    const newRewards = this.groupedRewards[currentMonth] || [];

    if (newRewards.length === 0) {
      this.showEmptyMessage = true;
    } else {
      this.showEmptyMessage = false;
    }

    if (JSON.stringify(this.currentRewards) !== JSON.stringify(newRewards)) {
      this.currentRewards = this.groupedRewards[currentMonth];
      this.selectedMonth = currentMonth;
      this.cdr.markForCheck();
    }
  }

  // startScrolling() {
  //   if (this.scrollInterval) {
  //     clearInterval(this.scrollInterval);
  //   }

  //   let scrollTime = Math.min(Math.max(this.currentRewards.length * 3 * 1000, 30000), 90000);

  //   this.updateCurrentRewards();

  //   this.scrollInterval = setInterval(() => {
  //     this.currentMonthIndex = (this.currentMonthIndex + 1) % this.monthKeys.length;

  //     // Ensure there are rewards for the next month before updating
  //     const nextMonth = this.monthKeys[this.currentMonthIndex];
  //     if (this.groupedRewards[nextMonth] && this.groupedRewards[nextMonth].length > 0) {
  //       this.updateCurrentRewards();
  //     } else {
  //       console.warn(`No rewards for the month: ${nextMonth}`);
  //     }
  //   }, scrollTime);
  // }

  startScrolling() {
    if (this.scrollInterval) {
      clearInterval(this.scrollInterval);
    }

    let scrollTime = Math.min(Math.max(this.currentRewards.length * 3 * 1000, 30000), 90000);

    this.updateCurrentRewards();

    this.scrollInterval = setInterval(() => {

      this.currentRewards = [];
      this.cdr.markForCheck();

      setTimeout(() => {
        this.currentMonthIndex = (this.currentMonthIndex + 1) % this.monthKeys.length;

        const nextMonth = this.monthKeys[this.currentMonthIndex];
        if (this.groupedRewards[nextMonth] && this.groupedRewards[nextMonth].length > 0) {
          this.updateCurrentRewards();
        } else {
          console.warn(`No rewards for the month: ${nextMonth}`);
        }
      }, 3000);
    }, scrollTime);
  }

  startRewardCycle(): void {
    this.currentGroup = this.rewardsList[this.currentIndex];

    setTimeout(() => {
      this.moveToNextGroup();
    }, this.scrollDelay);
  }

  moveToNextGroup(): void {
    this.currentIndex = (this.currentIndex + 1) % this.rewardsList.length;

    this.startRewardCycle();
  }
  emailSentPopUp(msg: any) {
    this.openAlertMod(this.mailSentPopUp, msg);
  }
  geExpiredtPoData() {
    this.employeeService.getExpiredPo().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.expiredList = response.serviceResponse
        console.log(response, ":::::::::::::::::::Response,geExpiredtPoData")
      }
    });
  }

  handleEmailRequest(eventData: { poEndDate: any, projectName: any, poNo: any, poProjectType: any }) {
    console.log('Email Request Received:', eventData);
    this.sendEmail(eventData.poEndDate, eventData.projectName, eventData.poNo, eventData.poProjectType);
    // Perform email sending logic here
  }

  sendEmail(poEndDate: any, projectName: any, poNo: any, poProjectType: any) {

    console.log(poEndDate, projectName, poNo, poProjectType)
    let expiredEmailData: ExpiredEmailData = new ExpiredEmailData();
    let poObject: PoObject = new PoObject();
    poObject.poNo = poNo;
    poObject.projectName = projectName;
    poObject.endDate = poEndDate;
    poObject.poType = poProjectType;
    expiredEmailData.expiredData = poObject;
    const decryptedData = this.encryptionService.decrypt(sessionStorage.getItem('currentUser') || '');
    let user = JSON.parse(decryptedData);
    console.log(user, "userDetails");
    expiredEmailData.userEmail = user.email;
    console.log(expiredEmailData, "expiredEmailData",)
    console.log(expiredEmailData.expiredData, "expiredEmailData")
    console.log(expiredEmailData, "expiredEmailData")
    this.employeeService.sendExpiredPoEmail(expiredEmailData).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.popUpMessege = response.serviceMessage;
        console.log(response, ":::::::::::::::::::Response,geExpiredtPoData");
        this.cancelRequest();
        this.emailSentPopUp(this.popUpMessege);
      }
    });
  }

  closeTimesheetForm() {
    this.isTimesheetFormVisible = false;


  }

  // renderTimesheet(day?: any): void {
  //     console.log("Render triggered for:", day?.displayDate);
  //   const empId = this.currentUser?.empId;
  //   if (empId) {
  //     this.timesheetService.getLastFilledTimesheetByEmpId(Number(empId)).subscribe({
  //       next: (res) => {
  //         if (res.serviceStatus === 'Success') {
  //           this.lastTimesheetData = JSON.parse(JSON.stringify(res.serviceResponse));
  //         } else {
  //           this.lastTimesheetData = null;
  //         }
  //         this.isTimesheetFormVisible = true;
  //       },
  //       error: (err) => {
  //         console.error("Failed to fetch last timesheet", err);
  //         this.lastTimesheetData = null;
  //         this.isTimesheetFormVisible = true;
  //       }
  //     });
  //   }
  // }






  renderTimesheet(day?: any): void {
    console.log("Render triggered for:", day?.displayDate);
    const empId = this.currentUser?.empId;

    if (empId) {
      this.timesheetService.getLastFilledTimesheetByEmpId(Number(empId)).subscribe({
        next: (res) => {


          if (res.serviceStatus === 'Success') {

            if (res.serviceResponse1 === 'No Timesheet') {

              this.triggerNoTimesheetPopup = true;

              this.lastTimesheetData = null;




            } if (res.serviceResponse1 === 'Not Active') {
              this.triggerNoTimesheetPopupForInactiveEmployee = true;
            }
            else {
              // this.lastTimesheetData = JSON.parse(JSON.stringify(res.serviceResponse));
              const originalData = JSON.parse(JSON.stringify(res.serviceResponse));
              const selectedDate = day?.displayDate; // e.g., '2025-07-04'


              const fixDateTime = (datetime: string): string => {
                if (!datetime || !selectedDate) return datetime;
                const timePart = datetime.split(' ')[1];
                return `${selectedDate} ${timePart}`;
              };

              originalData.officeInTime = fixDateTime(originalData.officeInTime);
              originalData.officeOutTime = fixDateTime(originalData.officeOutTime);
              originalData.date = selectedDate;

              this.lastTimesheetData = originalData;
            }
          } else {
            this.lastTimesheetData = null;
          }


          this.isTimesheetFormVisible = true;
        },
        error: (err) => {
          console.error("Failed to fetch last timesheet", err);
          this.lastTimesheetData = null;
          this.isTimesheetFormVisible = true;
        }
      });
    }
  }



  //  renderTimesheet(day?: any): void {
  //     const empId = this.currentUser?.empId;

  //     if (empId) {
  //       this.timesheetService.getLastFilledTimesheetByEmpId(Number(empId)).subscribe({
  //         next: (res) => {
  //           if (res.serviceResponse1 === 'Not Active') {
  //              this.openInactiveModal();
  //             this.isTimesheetFormVisible = false;
  //             return;
  //           }

  //            if (res.serviceResponse1 === 'No Timesheet') {
  //              this.openNoTimesheet();
  //             this.isTimesheetFormVisible = false;
  //             return;
  //           }

  //           if (res.serviceStatus === 'Success') {
  //             this.lastTimesheetData = { ...res.serviceResponse };
  //           } else {
  //             this.lastTimesheetData = null;
  //           }

  //           this.isTimesheetFormVisible = true;
  //         },
  //         error: (err) => {
  //           console.error("Error fetching timesheet:", err);
  //           this.lastTimesheetData = null;
  //           this.isTimesheetFormVisible = true;
  //         }
  //       });
  //     }
  //   }



  onTimesheetSubmissionComplete(): void {
    this.isTimesheetFormVisible = false;
    this.getTimesheetsForHomePageByEmpId('This Month');
    // Optional: navigate to the tab if needed
    // this.router.navigate(['/user-timesheet', 'my-timesheet']); // or 'team-timesheet' based on context
  }


  // fetchLastTimesheet(empId: number | string): void {
  //   this.timesheetService.getLastFilledTimesheetByEmpId(Number(empId)).subscribe({
  //     next: (res) => {
  //       if (res.serviceStatus === 'Success') {
  //         this.lastTimesheetData = JSON.parse(JSON.stringify(res.serviceResponse)); // Force reference change
  //       }
  //     },
  //     error: (err) => {
  //       console.error("Failed to fetch last timesheet", err);
  //     }
  //   });
  // }

  triggerNoTimesheetPopup: boolean = false;

  triggerNoTimesheetPopupForInactiveEmployee: boolean = false;


  onCreateTimesheet() {
    console.log("Timesheet submitted!");
  }


  shouldRenderTimesheet(): boolean {
    return this.timesheetDetails.some(
      ts => ts.status === 'Pending' || ts.status === 'Not Filled'
    );

  }

  handleTabClick(): void {

    console.log("shouldRenderTimesheet called")
    console.log('Timesheet Details:', this.timesheetDetails);

    if (this.shouldRenderTimesheet()) {
      this.renderTimesheet();
    }
  }



  //fetch all milestone to be expired by rm mail
  fetchMilestones(): void {
    const rmEmail: number = this.currentUser.empId;

    this.projectService.getAllMilestoneToBeExpired(rmEmail).subscribe({
      next: (response) => {
        console.log('Milestones to be expired fetched:', response);
        const milestoneLength = Array.isArray(response.serviceResponse) ? response.serviceResponse.length : 0;
        console.log('Number of milestones to be expired:', milestoneLength);

        if (response.serviceStatus === 'Success' && milestoneLength > 0) {
          this.milestoneToBeExpired = response.serviceResponse;
          this.milestoneCount = milestoneLength;
        }

      },
      error: (error) => {
        console.error('Error fetching milestones:', error);

      }
    });
  }

  //sort milestone expired data by mat table
  sortMilestoneExpiredData(event: any): void {
    this.milestoneExpiredSortColumn = event.active;
    this.milestoneExpiredSortColumnType = 'string';
    this.milestoneExpiredSortDirection = event.direction || 'asc';
  }
  //opne model
  openMilestoneExpiredListModal(): void {
    this.detailModalRef = this.modalService.open(this.milestoneExpiredListModalRef, {
      modalDialogClass: 'modal-xl'
    });
  }

  closeMilestoneExpiredListModal(): void {
    this.detailModalRef?.close();
  }




  //form initialization
  private initMilestoneForm(): void {
    this.milestoneForm = this.fb.group({
      projectName: [''],
      lineItemName: [''],
      name: [''],
      startDate: [''],
      endDate: [''],
      extendedDate: ['', Validators.required],
      status: [''],
      customReason: [''],
      extensionReasonId: [null, Validators.required]
    });









  }




  updateMilestoneDetails(milestone: MilestoneToBeExpired): void {

    this.selectedMilestone = milestone;
    this.milestoneForm.patchValue({
      projectName: milestone.projectName,
      lineItemName: milestone.lineItemName,
      name: milestone.name,
      startDate: this.formatDate(milestone.startDate),
      endDate: this.formatDate(milestone.endDate),
      status: milestone.status,
      extendedDate: this.formatDate(milestone.extendedDate)

    });

    this.milestoneDetailModalRefView = this.modalService.open(this.milestoneDetailModalRef, { modalDialogClass:'modal-lg' });
    this.minExtendedDate();
  }


  //fetch all milestone to be expired reason
  featchingmilestoneExtensionReason() {
    this.projectService.getAllMilestoneExtendReason().subscribe({
      next: (response) => {
        console.log('AllMilestoneExtendReason to be expired fetched:', response);



        if (response.serviceStatus === 'Success') {
          this.milestoneExtendReason = response.serviceResponse;

        }

      },
      error: (error) => {
        console.error('Error fetching milestones:', error);

      }
    });
  }


  //update milestone extended date with reason
  isLoadingmilestoneDetailModal:boolean=false;
  isUpdated:boolean=false;
  updateMilestoneExtendedDateWithReason(): void {

    if (this.milestoneForm.invalid) {

      this.openUpdateProjectCompletionModal("Please fill all required fields.");
      this.milestoneForm.markAllAsTouched();
      return;
    }

    const formValues = this.milestoneForm.value;

    console.log("selectedMilestone" + this.selectedMilestone);

    const payload: MilestoneUpdatedLog = {
      milestoneId: this.selectedMilestone.id,
      poId: this.selectedMilestone.poId,
      projectId: this.selectedMilestone.projectId,

      milestoneName: this.selectedMilestone.name,
      milestoneStartDate: this.selectedMilestone.startDate
        ? new Date(this.selectedMilestone.startDate):null,
      milestoneEndDate: this.selectedMilestone.endDate
        ? new Date(this.selectedMilestone.endDate):null,
      description: this.selectedMilestone.description,
      remarks: this.selectedMilestone.remarks,
      milestoneStatus: this.selectedMilestone.status,

      lineItemId: this.selectedMilestone.lienItemId,
      lineItemName: this.selectedMilestone.lineItemName,
      projectName: this.selectedMilestone.projectName,
      poNumber: this.selectedMilestone.poNo,


      extendedDate: formValues.extendedDate,
      updatedBy: this.currentUser.empId,

      milestoneExtensionReasonId: formValues.extensionReasonId,
      milestoneExtensionReasonText: formValues.customReason
    };

    console.log('Payload for milestone extension:', payload);
    this.isLoadingmilestoneDetailModal=true;
    this.projectService.updateMilestoneExtendedDate(payload).subscribe(
      (response: any) => {
        console.log('Milestone extension response:', response);
        if (response.serviceStatus === 'Success') {
          this.response1 = response.serviceMessage;
          this.isUpdated=true;
          this.isLoadingmilestoneDetailModal=false;
          // const initialState = {
          //   // 'message' should be a public property in your modal component's class
          //   message: this.response1
          // };
          // this.popUpModalResf = this.modalService.open(this.milestoneExpireValidationPupup, {
          //   class: 'modal-sm',
          //   initialState: initialState
          // });
           this.openUpdateProjectCompletionModal(
            "milestone extended date updated successfully."
          );



          this.fetchMilestones();
           this.milestoneForm.get('extensionReasonId')?.reset();
        } else {

          this.isLoadingmilestoneDetailModal=false
          this.response1 = response.serviceMessage || 'An unexpected error occurred.';
          this.openUpdateProjectCompletionModal(response.serviceResponse);
           this.milestoneForm.get('extensionReasonId')?.reset();










        }
      },
      (errorResponse) => {
        console.error('Error updating milestone:', errorResponse);

       this.isLoadingmilestoneDetailModal=false;
        let errorMessage = 'An unknown error occurred.';
        if (errorResponse.error && errorResponse.error.message) {

          errorMessage = errorResponse.error.message;
        }




        this.openAlertMod(this.milestoneExpireValidationPupup, errorMessage);

      }
    );

  }


  //onchamges in form
  onReasonChange(event: Event): void {
    const selectedValue = (event.target as HTMLSelectElement).value;
    const selectedReason = this.milestoneExtendReason.find(
      r => r.id === +selectedValue
    );

    this.isOtherReasonSelected = selectedReason?.milestoneExtensionReason === 'Other';

    if (this.isOtherReasonSelected) {
      this.milestoneForm.get('customReason')?.reset();
      this.milestoneForm.get('customReason')?.setValidators([Validators.required]);
    } else {
      this.isOtherReasonSelected = false;
      this.milestoneForm.get('customReason')?.clearValidators();
      this.milestoneForm.get('customReason')?.setValue('');
    }
    this.milestoneForm.get('extendedDate')?.updateValueAndValidity();

  }



  closeMilestoneDetailModal(): void {
    this.milestoneDetailModalRefView?.close();

  }


  //convert form data to date type
  private formatDate(date: Date | string): string {
    if (!date) return '';
    return new Date(date).toLocaleDateString('en-GB'); // dd/MM/yyyy
  }

  private resetUserEnteredFields(): void {

    this.milestoneForm.get('extendedDate')?.reset();
    this.milestoneForm.get('extensionReasonId')?.reset();
    this.milestoneForm.get('customReason')?.reset();
  }

  cancelRequestPopup() {

    this.popUpModalResf?.close();
    this.milestoneForm.get('extensionReasonId')?.reset();
    this.closeMilestoneDetailModal();
  }



    minExtendedDateformilestone:Date;
   public  minExtendedDate(): void {
    const endDate=this.milestoneForm.get('endDate').value;;
    this.minExtendedDateformilestone=new Date(this.convertToISO(endDate));
      this.milestoneForm.get('extensionReasonId')?.reset();
    console.log("minExtendedDateformilestone",this.minExtendedDateformilestone);

  }
  convertToISO(dateString: string): string {
  const [day, month, year] = dateString.split('/');
  return `${year}-${month}-${day}`;
}

public getDaysLeftForExpiry(endDate: string | Date): string {
    if (!endDate) {
      return 'N/A';
    }

    const today = new Date();
    const milestoneEndDate = new Date(endDate);


    today.setHours(0, 0, 0, 0);
    milestoneEndDate.setHours(0, 0, 0, 0);


    const differenceInMs = milestoneEndDate.getTime() - today.getTime();


    const daysLeft = Math.ceil(differenceInMs / (1000 * 60 * 60 * 24));

    if (daysLeft < 0) {
      return 'Expired';
    } else if (daysLeft === 0) {
      return 'Expires Today';
    } else {
     return `${daysLeft}`;
    }
  }

  onHelpButtonHover(event: MouseEvent) {
    this.isHelpHovered = true
  }

  onHelpButtonLeave() {
    this.isHelpHovered = false
  }




openUserManualPdf(): void {
  const pdfPath = 'assets/pdfFiles/RM Approval of timesheet.pdf';
  window.open(pdfPath, '_blank');
}





  getRejectionReason() {

      this.timesheetService.getRejectionReason().pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.rejectReasons = response.serviceResponse;

        } else {
          console.error(response.serviceResponse)
        }
      });

    }

    openProbationNotificationModal(template: TemplateRef<any>) {
    if (!this.currentUser || !this.currentUser.empId) {
        console.error("Current user (HOD) not found. Cannot fetch notifications.");
        return;
    }

    this.isLoadingNotifications = true;
    this.probationNotifications = [];
    this.modalRef2 = this.modalService.open(template, { modalDialogClass:'modal-lg' });

    const payload = {
      hodId: this.currentUser.empId,
    };


    this.employeeService.getProbationReminders(payload).subscribe({
      next: (response) => {
        if (response && response.serviceStatus === 'SUCCESS') {
          this.probationNotifications = response.serviceResponse;
        } else {
        }
        this.isLoadingNotifications = false;
      },
      error: (err) => {
        console.error('Failed to load probation notifications', err);
        this.isLoadingNotifications = false;
      }
    });
  }


  onDateSelected(event: { date: string, status: string }) {
  console.log("User clicked date:", event);

  if (event.status === 'Approved') {
    console.log("Skipping navigation because timesheet is approved.");
    return;
  }
  else if(event.status==='Pending'){
     console.log("Skipping navigation because timesheet is filled.");
    return;
  } else if(event.status==='Rejected'){
     console.log("Skipping navigation because timesheet is 	Rejected.");
    return;
  }

  this.router.navigate(['/user-timesheet/my-timesheet'], {
    queryParams: { date: event.date }
  });
}


autoFillTimesheet:boolean
  getAutoFillTimesheetFlag(): void {
    const flag = sessionStorage.getItem('autoFillTimesheet');
    this.autoFillTimesheet = flag === 'true';
    console.log('autoFillTimesheet received:', this.autoFillTimesheet);
    sessionStorage.removeItem('autoFillTimesheet');
  }

  applyClientFilter() {
  this.getMyReporteesTimesheetRequests();  // Call your API again
}

onTimesheetSearch(searchData) {
  // Handle night shift search terms only for timesheet
  console.log("")
  console.log(searchData, "searchData")
  if (searchData.isNightShift) {
    const searchTerm = searchData.isNightShift.toLowerCase().trim();
    console.log("Search term",searchTerm);
    const nightDisplay = 'night shift';
    const regularDisplay = 'regular shift';

    // Check if search term appears in display text
    const nightMatch = nightDisplay.includes(searchTerm);
    const regularMatch = regularDisplay.includes(searchTerm);

    if (nightMatch && !regularMatch) {
      searchData.isNightShift = 'true';
    } else if (regularMatch && !nightMatch) {
      searchData.isNightShift = 'null';
    } else {
      // If both match or neither match, use character-based fallback
      if (searchTerm.includes('n') && !searchTerm.includes('r') && !searchTerm.includes('d')) {
        searchData.isNightShift = 'true';
      } else if ((searchTerm.includes('r') || searchTerm.includes('d')) && !searchTerm.includes('n')) {
        searchData.isNightShift = 'null';
      }
    }
  }

  this.filters = searchData;
}



expandedTimesheetIndex: number | null = null;
expandedProjectIndex: number | null = null;

/* EMPLOYEE ACCORDION */
toggleAccordion(index: number): void {
  if (this.expandedTimesheetIndex === index) {
    this.expandedTimesheetIndex = null;     // close employee
    this.expandedProjectIndex = null;       // close project
  } else {
    this.expandedTimesheetIndex = index;    // open employee
    this.expandedProjectIndex = null;       // reset project
  }
}

/* PROJECT ACCORDION */
toggleProject(projectIndex: number): void {
  if (this.expandedProjectIndex === projectIndex) {
    this.expandedProjectIndex = null;       // close project
  } else {
    this.expandedProjectIndex = projectIndex; // open project
  }
}



downloadExcel(base64Data: string, mimeType: string, fileName: string) {

  const byteCharacters = atob(base64Data);
  const byteNumbers = new Array(byteCharacters.length);

  for (let i = 0; i < byteCharacters.length; i++) {
    byteNumbers[i] = byteCharacters.charCodeAt(i);
  }

  const blob = new Blob(
    [new Uint8Array(byteNumbers)],
    { type: mimeType }
  );

  const url = window.URL.createObjectURL(blob);

  const a = document.createElement('a');
  a.href = url;
  a.download = fileName;
  a.click();

  window.URL.revokeObjectURL(url);
}




onEmployeeToggle(timesheet: any) {
  timesheet.locations.forEach((loc: any) => {
    loc.projects.forEach((proj: any) => {
      proj.isSelected = timesheet.isSelected;

      proj.activities.forEach((act: any) => {
        act.isSelected = timesheet.isSelected;
      });
    });
  });
}
onProjectToggle(timesheet: any, project: any) {
  project.activities.forEach((act: any) => {
    act.isSelected = project.isSelected;
  });

  // Update employee checkbox
  timesheet.isSelected = timesheet.locations
    .flatMap((l: any) => l.projects)
    .every((p: any) => p.isSelected);
}
onActivityToggle(timesheet: any, project: any) {
  // Update project checkbox
  project.isSelected = project.activities.every(
    (act: any) => act.isSelected
  );

  // Update employee checkbox
  timesheet.isSelected = timesheet.locations
    .flatMap((l: any) => l.projects)
    .every((p: any) => p.isSelected);
}

// allTeamTimesheets: any[] = [];

// selectAllTimesheet(event){
//   this.bulkApprove = [];
//  this.bulkReject = [];

//  const checkboxes = document.querySelectorAll('.timesheet-req-checkbox');
//  checkboxes.forEach((checkbox: any) => {
//    //console.log("checkbox : ", checkbox);
//    let checkboxIndex = checkbox.getAttribute('id');
//    let checkedTimesheet = this.allTeamTimesheets.find((_timesheet, index) => _timesheet.checkId == checkboxIndex);

//    if (event.target.checked) {
//      checkbox.checked = true;
//      this.bulkApprove.push(checkedTimesheet);
//      this.bulkReject.push(checkedTimesheet);
//    } else {
//      checkbox.checked = false;
//      this.bulkApprove.forEach((timesheet, index) => {
//        if (timesheet == checkedTimesheet) this.bulkApprove.splice(index, 1);
//      });
//      this.bulkReject.forEach((timesheet, index) => {
//        if (timesheet == checkedTimesheet) this.bulkReject.splice(index, 1);
//      });
//    }
//  });
// }

zoomIn() {
  if (this.zoomScale < 2.5) {
    this.zoomScale += 0.1;
    this.zoomLevel = Math.round(this.zoomScale * 100);
  }
}

zoomOut() {
  if (this.zoomScale > 0.5) {
    this.zoomScale -= 0.1;
    this.zoomLevel = Math.round(this.zoomScale * 100);
  }
}


get transformStyle() {
  return `translate(${this.translateX}px, ${this.translateY}px) scale(${this.zoomScale})`;
}

startDrag(event: MouseEvent) {
  if (this.zoomScale <= 1) return; // drag only when zoomed

  this.isDragging = true;
  this.startX = event.clientX - this.translateX;
  this.startY = event.clientY - this.translateY;
  event.preventDefault();
}

onDrag(event: MouseEvent) {
  if (!this.isDragging) return;

  this.translateX = event.clientX - this.startX;
  this.translateY = event.clientY - this.startY;
}

endDrag() {
  this.isDragging = false;
}

resetPreviewState() {
  this.zoomScale = 1;
  this.zoomLevel = 100;
  this.translateX = 0;
  this.translateY = 0;
  this.isDragging = false;
}

redirectToViewTeamTimesheet() {
  this.router.navigate(
    ['/user-timesheet/team-timesheet'],
    { queryParams: { tab: 'team-timesheet', view: 'requests' } }
  );
}


getTimesheetStatusCountsByEmpId() {
  const payload : any = {
    managerId: this.currentUser.empId,
  };
  this.timesheetNewService
.getMyReporteesTimesheetRequestsCount(payload)
.subscribe({
  next: (res: any) => {
    console.log("Status Count Response", res);
    if (res && res.serviceResponse) {
      this.pendingCount = 0;
      this.approvedCount = 0;
      this.rejectedCount = 0;

      res.serviceResponse.forEach((item: any) => {

        const status = item[0]?.toLowerCase();
        const count = Number(item[1]) || 0;

        if (status === 'pending') {
          this.pendingCount = count;
        }
        else if (status === 'approved') {
          this.approvedCount = count;
        }
        else if (status === 'rejected') {
          this.rejectedCount = count;
        }

      });


    }
  }
});

}
}



// Move compare function outside the class
function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
}
