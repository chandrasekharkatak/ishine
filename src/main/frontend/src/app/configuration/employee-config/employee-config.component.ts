import { DatePipe, LocationStrategy } from '@angular/common';
import { Component, OnInit, SecurityContext, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { DomSanitizer } from '@angular/platform-browser';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { certification } from 'src/app/models/certification';
import { Designation } from 'src/app/models/designation';
import { Document } from 'src/app/models/document';
import { Domain } from 'src/app/models/domain';
import { Employee } from 'src/app/models/employee';
import { Feature } from 'src/app/models/feature';
import { Leave } from 'src/app/models/leave';
import { PreviousEmployer } from 'src/app/models/previousEmployer';
import { Query } from 'src/app/models/query';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { DepartmentService } from 'src/app/services/department.service';
import { DestinationService } from 'src/app/services/destination.service';
import { DomainService } from 'src/app/services/domain.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { ImageService } from 'src/app/services/image.service';
import { JobRoleService } from 'src/app/services/job-role.service';
import { LeaveService } from 'src/app/services/leave.service';
import { PortalService } from 'src/app/services/portal.service';
import { UtilityService } from 'src/app/services/utility.service';
import { ValidationService } from 'src/app/services/validation.service';


import { Subscription } from 'rxjs';
class FilterData {
  title: any;
  columns: any;
  queryList: any;


}
@Component({
  selector: 'app-employee-config',
  templateUrl: './employee-config.component.html',
  styleUrls: ['./employee-config.component.css']
})
export class EmployeeConfigComponent implements OnInit {

  actionSection: TemplateRef<any>;
  extensionReason: any;
  extensionPeriod: any;
  reasonOfExtension: any;
  reasonForDelay: any;
  selectedTab: string;
  reduceExtension: number;
  getStatusClass(arg0: any): string | string[] | Set<string> | { [klass: string]: any; } {
    throw new Error('Method not implemented.');
  }

  isExtensionFormValid() {
    throw new Error('Method not implemented.');
  }
  extendProbation(_t2394: any) {
    throw new Error('Method not implemented.');
  }


  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;
  @ViewChild('change_manager_template')
  changeManagerTemplate: TemplateRef<any>;
  feature = 'Employee Config';
  managerFlag: boolean = false;
  data: string;
  items = 10;
  datas: string;
  imployeeID: any;

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;

  teamList: any = [];
  listOfProjectsByDeptId: any[] = [];

  //flags
  isCreation: boolean = false;
  isUpdation: boolean = false;
  isDeletion: boolean = false
  isForm: boolean = false;
  isTable: boolean = false;

  isDraft: boolean = false;
  isDraftTable: boolean = false;
  dateOfReleivingshow: boolean = false;

  isFullJourneyAccordianBody: boolean = false;
  isLifeCycleAccordianBody: boolean = false;
  isKycUpdateAccordianBody: boolean = false;
  isEmployeeInfoAccordianBody: boolean = false;
  isEmployeeHistory: boolean = false;
  isTeamProjectAccordianBody: boolean = false;

  //modal
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();
  modalRef1: BsModalRef = new BsModalRef();
  previewModalRef: BsModalRef = new BsModalRef();
  all: any;
  //Obj
  currentUser: User;
  employeeObj: Employee = new Employee();
  domainObj: Domain = new Domain();
  allEmployeeList: any;
  _allEmployeeList: any;
  managerList: any = [];
  managerListOriginal: Employee[] = [];
  managerAndAbove: any = [];
  userMapping: any = {};
  allJobRoleList: any[] = [];
  allDeptList: any[] = [];
  filteredJobRoleList: any[] = [];
  employeeDataForExcel: any[] = [];
  portalConfigList: any[] = [];
  allDomainList: any[] = [];
  specializationList: any[] = [];
  // allSpecializationList:any[] = [];
  storedDataList: any[] = [];
  domainSpecializationList: any[] = [];
  allDesignationList: any[] = [];
  employeeAuditHistory: any[] = [];
  filteredEmployeeAuditHistory: any[] = [];
  lifeCycleChangeList: any[] = [];
  teamProjectChangeList: any[] = [];
  kycUpdateList: any[] = [];
  employeeInfoChangeList: any[] = [];
  userEmployeementId: any;

  employeeWorkingHistory: any[] = [];
  allCertificationList: any[] = [];
  allPreviousEmployment: any[] = [];
  updatedCertificationList: any[] = [];
  updatedPreviousEmployment: any[] = [];
  allStates: any[] = [
    "Andaman & Nicobar Islands",
    "Andhra Pradesh",
    "Arunachal Pradesh",
    "Assam",
    "Bihar",
    "Chandigarh",
    "Chhattisgarh",
    "Dadra and Nagar Haveli and  Daman & Diu",
    "Delhi",
    "Goa",
    "Gujarat",
    "Haryana",
    "Himachal Pradesh",
    "Jammu & Kashmir",
    "Jharkhand",
    "Karnataka",
    "Kerala",
    "Ladakh",
    "Lakshadweep",
    "Madhya Pradesh",
    "Maharashtra",
    "Manipur",
    "Meghalaya",
    "Mizoram",
    "Nagaland",
    "Odisha",
    "Puducherry",
    "Punjab",
    "Rajasthan",
    "Sikkim",
    "Tamil Nadu",
    "Telangana",
    "Tripura",
    "Uttar Pradesh",
    "Uttarakhand",
    "West Bengal",
  ];
  //added by rahul for refered employee addition
  referedType = [
    "In Office",
    "Out Office"
  ]
  currDate: any;
  yearOfPassingList: any[] = [];
  revoke_template: any;

  deptId: any;


  previewObj: Employee = new Employee();
  previewEmployeeObj: Employee = new Employee();
  listOfReporties: any;

  filters: any = {};
  filterOnhistory = {};
  isSearchEnabled: boolean = false;
  employeeActiveColumns: any[] = ['employeementId', 'name', 'email', 'departmentName', 'managerName', 'dateOfJoining', 'employmentstatus', 'blank', 'blank', 'employeeType', 'skillNames', 'certificateNames', 'createdOn', 'createdByName', 'updatedOn', 'updatedByName', 'referedName'];


  employeeInActiveColumns: any[] = ['employeementId', 'name', 'email', 'departmentName', 'managerName', 'dateOfJoining', 'employmentstatus', 'blank', 'blank', 'employeeType', 'skillNames', 'certificateNames', 'createdOn', 'createdByName', 'updatedOn', 'updatedByName', 'referedName'];


  draftEmployeeColumns: any[] = ['employeementId', 'name', 'email', 'employmentstatus', 'managerName', 'departmentName', 'dateOfJoining', 'updateApplicationStatus']
  domainColumns: any[] = ['blank', 'domainName', 'createdByName', 'createdOn']
  employeeRole: any[] = ['Employee', 'TeamLead', 'Manager', 'HOD', 'HR', 'SuperAdmin', 'RMG'];
  workHistoryFilters: any = {};
  isworkHistorySearchEnabled: boolean = false;
  employeeWorkhistoryColumns: any[] = ['occasion', 'dayOfTheWeek', 'dateOfHoliday', 'state', 'createdOn', 'createdbyName', 'updatedOn', 'updatedByName'];

  auditFilter: any = {};
  isAuditSearchEnabled: boolean = false;
  employeeAuditColumns: any[] = ['blank', 'date', 'field', 'value', 'bucketName', 'updatedByName'];
  employeehistoryColumns: any[] = ['blank', 'name', 'teamName', 'projectName', 'startDate', 'endDate', 'teamLeadName', 'jobRole', 'clientLocation', 'clientName'];


  queryList: any[] = [];
  filterData: any = new FilterData();

  startDate: any;
  endDate: any;
  isToggle: boolean = false;
  today: Date;
  minDate: Date;
  maxDate: Date;
  isDateChanged: boolean = false;
  tempValue: any;
  maxDateForExtend: Date;
  minDateForExtend: Date;
  isPipGenerate: boolean = false;
  pipReasons: any = [];
  isPipFlag: boolean = false;

  isActiveTable: boolean = false;
  isApmosysProductUpdate: boolean = false;
  maxDOB: Date = moment().subtract(18, 'years').toDate();

  managerId: any;
  reporteeList: any = [];
  reporteeList2: any = [];
  listOfDepartment: any[] = [];

  employeeColumns: any[] = ['Employee Id', 'Full Name', 'Department', 'Designation',
    'Job Role', 'Manager', 'Team Name', 'Project Name', 'Client Name',
    'Employment Status', 'Date Of Joining', 'Domain', 'Specialization', 'City', 'Blood Group',
    'Gender', 'Work Location', 'Probation Period', 'Notice Period', 'Marital Status',
    'Bank Name', 'Created By', 'State', 'Created On'];
  departmentName: any;
  private subscription: Subscription = new Subscription();
  referedTypeStatus: boolean = false;
  employeesFor360: any[] = [];
  excelName: string;
  tableName: string;
  selectedEmployee: any;
  isProcessing = false;
  extensionData: any;
  confirmationReason: string = '';
  isEmployeementTypeChanged: boolean = false;

  showExpandedColumns: any;
  actualNoticePeriod:number=0;
  statusFlag:boolean =false;

  constructor(

    private employeeService: EmployeeService,
    public validationService: ValidationService,
    private datePipe: DatePipe,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private jobRoleService: JobRoleService,
    private departmentService: DepartmentService,
    private exportExcelService: ExportExcelService,
    private imageService: ImageService,
    private sanitizer: DomSanitizer,
    private portalService: PortalService,
    private utilityService: UtilityService,
    private locationStrategy: LocationStrategy,
    private domainService: DomainService,
    private destinationService: DestinationService,
    private leaveService: LeaveService,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  async ngOnInit(): Promise<void> {
    this.currDate = this.datePipe.transform(new Date(), 'YYYY-MM-dd');
    this.getAllJobRoleList();

    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    // console.log(this.feature, this.userMapping);

    this.sectionViewInit();
    this.loadManagerList();
    this.employeeObj.gender = '';
    this.employeeObj.maritalStatus = '';
    this.employeeObj.reportingManagerId = '';
    this.employeeObj.approvalsTo = '';
    this.setYearOfPassingList();
    this.preventBackButton();
    // this.getAllEmployeeList();

    try {
      //this.employeesFor360 = await this.utilityService.getEmployeeDetailsFor360View();
      // console.log("Priyadarshini ", this.employeesFor360);
    } catch (error) {
      console.error("Error fetching employee details for 360 view", error);
    }
    //console.log('user -- ', this.userMapping);
  }

  //
  getEmploymentStatusClass(status: string): string {
    switch (status?.toLowerCase()) {
      case 'confirmed': return 'badge bg-success';
      case 'probation': return 'badge bg-warning';
      // case 'extended': return 'badge bg-info';
      default: return 'badge bg-secondary';
    }
  }
  getConfirmationDate(employee: any): string {
    if (employee.dateOfJoining && employee.probationPeriod) {
      const [day, month, year] = employee.dateOfJoining.split('-');
      const doj = new Date(year, month - 1, day);

      const probationDays = parseInt(employee.probationPeriod);
      const noOfDays = employee.noOfDays || 0;
      const confirmationDate = new Date(doj.getTime() + ((probationDays + noOfDays) * 24 * 60 * 60 * 1000));

      return this.datePipe.transform(confirmationDate, 'dd-MM-yyyy') || '';
    }
    return '';
  }

  openEmployeeModal(employee: any, employeeTemplate: TemplateRef<any>) {
    this.selectedEmployee = employee;

    if (this.selectedEmployee && this.selectedEmployee.dateOfJoining && this.selectedEmployee.probationPeriod) {
      const [day, month, year] = this.selectedEmployee.dateOfJoining.split('-');
      const doj = new Date(year, month - 1, day);

      const probationDays = parseInt(this.selectedEmployee.probationPeriod);
      const noOfDays = this.selectedEmployee.noOfDays || 0;
      const confirmationDate = new Date(doj.getTime() + ((probationDays + noOfDays) * 24 * 60 * 60 * 1000));

      this.selectedEmployee.confirmationDate = confirmationDate;
    }

    this.confirmationReason = '';
    this.reasonForDelay = '';
    this.extensionReason = '';
    this.reasonOfExtension = '';
    this.extensionPeriod = null;

    this.modalRef1 = this.modalService.show(employeeTemplate, {
      class: 'modal-xl'
    });
  }
  openRevokeModal(employee: any, revokeModalTemplate: TemplateRef<any>) {
    this.selectedEmployee = employee;
    this.modalRef = this.modalService.show(revokeModalTemplate, { class: 'modal-sm' });
  }

  confirmRevoke(alert_message: TemplateRef<any>) {
    if (!this.selectedEmployee) {
      console.error('No employee selected for revoke action.');
      this.modalRef?.hide();
      return;
    }

    const payload = {
      empId: this.selectedEmployee.empId,
      hodId: this.currentUser.empId
    };

    this.employeeService.revokeConfirmation(payload).subscribe({
      next: (response) => {
        if (response && response.serviceStatus === 'SUCCESS') {
          console.log('Revoke successful:', response.serviceResponse);

          this.selectedEmployee.isConfirmedClicked = 0;
          this.selectedEmployee.employmentstatus = "Probation";

          const message = "Confirmation has been revoked successfully.";
          this.openAlertMod7(alert_message, message);

        } else {
          console.error('Revoke failed:', response.serviceResponse);
          const errorMessage = response.serviceResponse || 'An unexpected error occurred while revoking confirmation.';
          this.openAlertMod7(alert_message, errorMessage);

        }

        this.modalRef?.hide();
      },
      error: (err) => {
        const errorMessage = err.error?.serviceResponse || 'An unexpected server error occurred.';

        console.error('Failed to revoke confirmation:', errorMessage);
        this.openAlertMod7(alert_message, errorMessage);

        this.modalRef?.hide();
      }
    });
  }

  handleConfirmClick(employee: any, alert_message: TemplateRef<any>) {
    if (employee.days_left_for_full_time < 0) {
      this.selectedTab = 'Confirm Employee Late';
    } else {
      this.selectedTab = ' ';
      this.confirmAndExecuteConfirmation(employee, alert_message);
    }
  }

  confirmAndExecuteConfirmation(employee: any, alert_message: TemplateRef<any>) {
    if (employee.days_left_for_full_time < 0 && !this.confirmationReason.trim()) {
      alert('A reason is required for late confirmation.');
      return;
    }

    const payload = {
      empId: employee.empId,
      hodId: this.currentUser.empId,
      reasonOfExtension: this.confirmationReason || '',
    };

    this.employeeService.confirmEmployee(payload).pipe(first()).subscribe({
      next: (response: any) => {
        this.closeAllModals();

        if (response.serviceStatus === "Success") {
          const message = "Employee has been confirmed for Full Time Employment";
          this.openAlertMod7(alert_message, message);
        } else {
          this.openAlertMod7(alert_message, response.serviceResponse);
        }


        this.resetAllForms();
      },
      error: (error) => {
        console.error("Error confirming employee", error);
        this.closeAllModals();
        this.resetAllForms();

      }
    });
  }

  openEmployeeConfimationModal(template: TemplateRef<any>, template2: TemplateRef<any>) {
    if (!this.selectedEmployee || !this.selectedEmployee.empId) {
      const message = 'Please select an employee to confirm.';
      this.openAlertMod7(template2, message);
      return;
    }

    if (this.selectedEmployee.days_left_for_full_time < 0) {
      this.selectedTab = 'Confirm Employee Late';
    } else {
      this.selectedTab = ' ';
    }

    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }

  openReduceExtensionModal(template: TemplateRef<any>, template2: TemplateRef<any>) {
    this.selectedTab = 'Reduce Extension';
    if (!this.selectedEmployee || !this.selectedEmployee.empId) {
      const message = 'Please select an employee to reduce the extension.';
      this.openAlertMod(template2, message);
      return;
    }
    if (this.selectedEmployee.extensionPeriod || this.selectedEmployee.extensionPeriod >= 0) {
      const message = 'Please enter a valid number of extension days.';
      this.openAlertMod(template2, message);
      return;
    }
    this.extensionData = {
      empId: this.selectedEmployee.empId,
      daysToReduce: this.reduceExtension,
      hodId: this.currentUser.empId
    };
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }

  reduceExtensionRequest(selectedEmployee: any, alert_message: TemplateRef<any>) {
    if (this.reduceExtension == null) {
      const message = 'Days to reduce cannot be empty. Please enter a valid number.';
      this.openAlertMod(alert_message, message);
      return;
    }

    const daysToReduce = Number(this.reduceExtension);

    if (isNaN(daysToReduce)) {
      const message = 'Please enter a valid numeric value for days to reduce.';
      this.openAlertMod(alert_message, message);
      return;
    }

    if (!Number.isInteger(daysToReduce)) {
      const message = 'Days to reduce must be a whole number (integer).';
      this.openAlertMod(alert_message, message);
      return;
    }

    if (daysToReduce <= 0) {
      const message = 'Days to reduce must be greater than 0.';
      this.openAlertMod(alert_message, message);
      return;
    }

    if (daysToReduce > 365) {
      const message = 'Days to reduce cannot exceed 365 days.';
      this.openAlertMod(alert_message, message);
      return;
    }

    this.extensionData.daystoReduce = daysToReduce;

    this.isProcessing = true;

    this.employeeService.reduceExtension(this.extensionData).pipe(first()).subscribe({
      next: (response: any) => {
        this.isProcessing = false;
        this.closeAllModals();

        if (response.serviceStatus === "Success") {
          const message = "Extension has been reduced successfully.";
          this.openAlertMod(alert_message, message);
        } else {
          this.openAlertMod(alert_message, response.serviceResponse);
        }

        this.resetAllForms();
      },
      error: (error) => {
        console.error("Error reducing extension", error);
        this.isProcessing = false;
        this.closeAllModals();

        const message = 'An error occurred while reducing the extension. Please try again.';
        this.openAlertMod(alert_message, message);

        this.resetAllForms();
      }
    });
  }


  openReasonConfirmationModal(template: TemplateRef<any>, template2: TemplateRef<any>) {
    this.selectedTab = 'Reason for Delay';
    if (!this.selectedEmployee || !this.selectedEmployee.empId) {
      const message = 'Please select an employee to provide a reason for delay.';
      this.openAlertMod(template2, message);
      return;
    }
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }

  confirmAndExecuteReasonSubmission(selectedEmployee: any, template: TemplateRef<any>) {
    if (!this.reasonForDelay || !this.reasonForDelay.trim()) {
      const message = 'A reason for the delay is required.';
      this.openAlertMod(template, message);
      return;
    }

    const payload = {
      empId: selectedEmployee.empId,
      reasonOfExtension: this.reasonForDelay,
      hodId: this.currentUser.empId
    };

    this.employeeService.submitReasonForDelay(payload).pipe(first()).subscribe({
      next: (response: any) => {
        this.closeAllModals();

        if (response.serviceStatus === "Success") {
          const message = "Reason for delay has been submitted successfully.";
          this.openAlertMod(template, message);
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }

        this.resetAllForms();
      },
      error: (error) => {
        console.error("Error submitting reason for delay", error);
        this.closeAllModals();
        this.resetAllForms();
        alert("An error occurred while submitting the reason for delay.");
      }
    });
  }

  openExtensionConfirmationModal(template: TemplateRef<any>, template2: TemplateRef<any>) {
    if (!this.extensionPeriod || this.extensionPeriod <= 0 || this.extensionPeriod > 90) {
      const message = 'Please enter a valid number of extension days.';
      this.openAlertMod(template2, message);
      return;
    }
    if (!this.reasonOfExtension || !this.reasonOfExtension.trim()) {
      const message = 'A reason for the extension is required.';
      this.openAlertMod(template2, message);
      return;
    }

    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }

  confirmAndExecuteExtension(template: TemplateRef<any>): void {
    this.modalRef?.hide();
    this.executeExtendEmployee(template);
  }

  decline(): void {
    this.closeAllModals();
    this.resetAllForms();
  }

  executeExtendEmployee(template: TemplateRef<any>) {
    const payload = {
      empId: this.selectedEmployee.empId,
      probationPeriod: this.selectedEmployee.probationPeriod,
      reasonOfExtension: this.reasonOfExtension,
      extendedPeriod: this.extensionPeriod,
      hodId: this.currentUser.empId
    };

    this.employeeService.extendemployee(payload).pipe(first()).subscribe({
      next: (response: any) => {
        this.closeAllModals();

        if (response.serviceStatus === "Success") {
          const Message = "Employee's probation period has been extended";
          this.openAlertMod(template, Message);
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }

        this.resetAllForms();
      },
      error: (error) => {
        console.error("Error extending employee", error);
        this.closeAllModals();
        this.resetAllForms();
        alert("An error occurred while extending the probation period.");
      }
    });
  }

  private closeAllModals(): void {
    if (this.modalRef) {
      this.modalRef.hide();
      this.modalRef = null;
    }
    if (this.modalRef1) {
      this.modalRef1.hide();
      this.modalRef1 = null;
    }
  }

  private resetAllForms(): void {
    this.selectedTab = ' ';
    this.confirmationReason = '';
    this.reasonForDelay = '';
    this.extensionReason = '';
    this.reasonOfExtension = '';
    this.extensionPeriod = null;
  }

  onModalClose(): void {
    this.closeAllModals();
    this.resetAllForms();
  }

  onModalBackdropClick(): void {
    this.closeAllModals();
    this.resetAllForms();
  }

  //



  toggleExpandedColumns(): void {
    this.showExpandedColumns = !this.showExpandedColumns;
  }

  getVisibleColumns(allColumns: any[]): any[] {
    if (!this.showExpandedColumns) {
      const primaryColumnKeys = [
        'employeementId', 'empId', 'name', 'email', 'departmentName',
        'managerName', 'dateOfJoining', 'employmentstatus'
      ];
      return allColumns.filter(column =>
        primaryColumnKeys.some(key => column.key === key || column.sortKey === key)
      );
    }
    return allColumns;
  }


  getColspanCount(): number {
    let baseColumns = 9;

    if (this.showExpandedColumns) {
      let expandedColumns = 0;
      if (this.isTable) {
        expandedColumns += 1;
        if (!this.isActiveTable) expandedColumns += 1;
        if (this.dateOfReleivingshow) expandedColumns += 1;
        expandedColumns += 6;
        expandedColumns += 2;
      }
      if (this.isDraftTable) {
        expandedColumns += 1;
      }

      baseColumns += expandedColumns;
    }

    return baseColumns;
  }
  preventBackButton() {
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(() => {
      history.pushState(null, null, location.href);
    })
  }
  validateBirthDate(template: TemplateRef<any>) {
    let birthdate = new Date(this.employeeObj.dateOfBirth);
    let dtCurrent = new Date();
    let flag = true;
    let dobInput: any = document.getElementById('DOB');

    if (dtCurrent.getFullYear() - birthdate.getFullYear() < 18) {
      this.openAlertMod(template, 'Employee age cannot be less than 18 years.');
      flag = false;
    }
    else if (dtCurrent.getFullYear() - birthdate.getFullYear() == 18) {

      //CD: 11/06/2018 and DB: 15/07/2000. Will turned 18 on 15/07/2018.
      if (dtCurrent.getMonth() < birthdate.getMonth()) {
        this.openAlertMod(template, 'Employee age cannot be less than 18 years.');
        flag = false;
      }

      if (dtCurrent.getMonth() == birthdate.getMonth()) {
        //CD: 11/06/2018 and DB: 15/06/2000. Will turned 18 on 15/06/2018.
        if (dtCurrent.getDate() < birthdate.getDate()) {
          this.openAlertMod(template, 'Employee age cannot be less than 18 years.');
          flag = false;
        }
      }
    }
    if (!flag) {
      setTimeout(() => {
        dobInput.value = '';
        this.employeeObj.dateOfBirth = '';
      }, 10)
    }
  }

  ngOnDestroy() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }
  ngAfterViewInit() {

  }

  sectionViewInit() {
    // if(this.userMapping.create_employee){
    //   this.showCreateForm()
    // }else if (this.userMapping.view_all_employee || this.userMapping.update_employee || this.userMapping.delete_employee || this.revoke_template) {
    //   //for employee table data
    //   this.showTable();
    // } else if (this.userMapping.update_draft) {
    //   //for employee draft table data
    //   this.showDraftTable();
    // }
    // this.addDemographiscInfo();

    if (this.userMapping.view_all_employee || this.userMapping.update_employee || this.userMapping.delete_employee || this.revoke_template) {
      //for employee table data
      this.showTable();
    } else if (this.userMapping.update_draft) {
      //for employee draft table data
      this.showDraftTable();
    }
  }
  //added by rahul for reffered
  refferedChange() {
    if (this.employeeObj.referedType == "InOffice") {
      this.referedTypeStatus = true;
    }
    if (this.employeeObj.referedType == "OutOffice") {
      this.referedTypeStatus = true;
    }
  }
  //end of the code
  // addDemographiscInfo() {
  //   let path;

  //   let data = []

  //   data.forEach(empData => {
  //     let pincode = empData.pincode;
  //     let empId = empData.employeeId;

  //     if (pincode != null) {
  //       fetch('https://api.postalpincode.in/pincode/' + pincode).then(r => r.json()).then(j => {
  //         path = j[0].PostOffice[0];
  //         console.log(path, " : path");


  //         let empObj = new Employee();
  //         empObj.state = path.State;
  //         empObj.city = path.Name;
  //         empObj.pincode = path.Pincode;
  //         empObj.country = path.Country;
  //         empObj.employeementId = empId;

  //         console.log(empObj, " empObj");

  //         this.employeeService.addDemographicsInfo(empObj).pipe(first()).subscribe((response: any) => {
  //           if (response.serviceStatus == "Success") {
  //             console.log("Employee demographics updated");
  //           } else {
  //             console.log("Employee demographics updation failed");
  //           }
  //         });
  //       });
  //     }
  //   });
  // }

  disableMannualDateInput() {
    return false;
  }



  billableBenchDate: any

  onBillablechange() {

    if (this.employeeObj.billableType == 'Bench') {

      this.billableBenchDate = this.getCurrentFormattedDate();

    } else {
      this.billableBenchDate = "No";
    }

  }

  resetSelectSearch(managerSelect: any) {
    managerSelect.searchValue = '';
    managerSelect.filteredSource = managerSelect.source;
  }


  getCurrentFormattedDate(): string {
    const now = new Date(); // Get the current date and time

    // Manually format the date
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0'); // Months are 0-based
    const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    const seconds = String(now.getSeconds()).padStart(2, '0');

    // Format the date as a string
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
  }



  setCalenderMaxDate() {
    const dateFormat = 'YYYY-MM-DD';
    const today = moment(new Date()).format(dateFormat);

    let DOB = document.getElementById('DOB');
    let DOJ = document.getElementById('DOJ');
    let DOC = document.getElementById('DOC');

    DOB?.setAttribute('max', today);
    DOJ?.setAttribute('max', today);
    DOC?.setAttribute('max', today);
  }

  currentDateFilter = (d: Date) => {
    const dateFormat = 'YYYY-MM-DD';
    const currentDate = new Date();
    return (moment(d).format(dateFormat) <= moment(currentDate).format(dateFormat));
  }

  currentDateFilterDOB = (d: Date): boolean => {
    const dateFormat = 'YYYY-MM-DD';
    const today = moment();
    const eighteenYearsAgo = today.subtract(18, 'years');
    return moment(d).isSameOrBefore(eighteenYearsAgo, 'day');
  };

  DateFilterForDOR = (d: Date) => {
    const dateFormat = 'YYYY-MM-DD';
    const currentDate = new Date();
    let dateOfJoining = this.employeeObj.dateOfJoining != null && this.employeeObj.dateOfJoining !== undefined ? this.employeeObj.dateOfJoining : new Date();
    return (moment(d).format(dateFormat) >= moment(dateOfJoining).format(dateFormat) && moment(d).format(dateFormat) <= moment(currentDate).format(dateFormat));
  }


  DateFilterForDofRetain = (d: Date) => {
    const dateFormat = 'YYYY-MM-DD';
    const currentDate = new Date();

    // Use default date values in case the date fields are not defined
    const resignDate = this.employeeObj.dateOfResign ? this.employeeObj.dateOfResign : new Date();
    const relievingDate = this.employeeObj.dateOfRelieving ? this.employeeObj.dateOfRelieving : currentDate;

    return (
      moment(d).format(dateFormat) >= moment(resignDate).format(dateFormat) &&
      moment(d).format(dateFormat) <= moment(relievingDate).format(dateFormat)
    );
  };


  relievingDateFilter = (d: Date) => {
    const dateFormat = 'YYYY-MM-DD';
    const currentDate = new Date();

    let resignDate = this.employeeObj.dateOfResign;

    if (resignDate) {
      return (moment(d).format(dateFormat) >= moment(resignDate).format(dateFormat));
    } else {
      return false;
    }
  }

  setYearOfPassingList() {
    for (let start = 1990; start < 2051; start++) {
      this.yearOfPassingList.push(start);
    }
  }

  newEmployee = new Employee();

  managerUpdate(reportee, template: TemplateRef<any>) {
    this.newEmployee = reportee;
    console.log("newEmployee", this.newEmployee.managerId);

    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
  }

  updateEmployeesManager(template: TemplateRef<any>) {

    let emp = new Employee();
    emp.empId = this.newEmployee.empId;
    emp.managerId = this.newEmployee.managerId

    this.employeeService.setManagerToNewManager(emp).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        //console.log(" teamName after manager changes done ",this.employeeObj.teamName)
        this.getReporteesListByManagerId();
        this.getReporteesListByReportingManagerId();
        this.openAlertMod(template, " Employee's Manager has changed !!");
        //console.log(" Manager update ")
      }
    })

    this.modalRef.hide();

    //console.log(" managerUpdate method call and employee id of reporties    :   ",employee.empId);

    //console.log(" managerUpdate method call  employee name  :   ",employee.name);
    //console.log(" managerId   ::   ",employee.managerId);
  }

  newEmp = new Employee();

  reportingManagerUpdate(reportee, template: TemplateRef<any>) {
    this.newEmp = reportee;
    console.log("newEmployee", this.newEmp.reportingManagerId);

    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
  }

  updateEmployeesReportingManager(template: TemplateRef<any>) {

    let emp = new Employee();
    emp.empId = this.newEmp.empId;
    emp.reportingManagerId = this.newEmp.reportingManagerId

    this.employeeService.setReportingManagerToNewManager(emp).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        //console.log(" teamName after manager changes done ",this.employeeObj.teamName)
        this.getReporteesListByManagerId();
        this.getReporteesListByReportingManagerId();
        this.openAlertMod(template, " Employee's Reporting Manager has changed !!");
        //console.log(" Manager update ")
      }
    })

    this.modalRef.hide();
  }

  getTeamMemberByTeamName(employeeObj) {
    //console.log(" teamName getTeamMemberByTeamName ",teamName);
    // this.managerAndAbove = this.managerAndAbove.forEach(t=> t.managerId == ""); 

    let empObj = new Employee();
    empObj.managerId = employeeObj.managerId;

    this.employeeService.getTeamMemberByTeamName(empObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.reporteeList = response.serviceResponse;
        this.getManagersList();
        // this.employeeObj.managerId = '';
        //console.log(" teamMember list   ",this.TeamMemberList)
      }
    })
  }

  getManagersList() {
    // this.managerId = "";
    // this.managerAndAbove = [];
    this.managerAndAbove = this.managerAndAbove.forEach(t => t.managerId == "");
    //console.log(" managers call ");
    this.employeeService.getManagerList().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.managerAndAbove = response.serviceResponse
        this.managerAndAbove = this.managerAndAbove.filter(empId => empId.managerId != this.employeeObj.empId);
        //console.log(" managersAndAbove list   ",this.managerAndAbove);
      }
    });
  }

  stringToNumber(year: any) {
    this.employeeObj.yearOfPassing = Number.parseInt(year);
  }

  retainStatus() {

    if (this.employeeObj.employmentstatus == "Retain") {
      this.employeeObj.isRetain = "Yes";
    } else {
      this.employeeObj.isRetain = "No";
    }


    console.log(this.employeeObj.employmentstatus)
    console.log(this.employeeObj.isRetain)
  }

  showCreateForm() {
    this.isForm = true;
    this.isCreation = true;

    this.isTable = false;
    this.isUpdation = false;
    this.isDraft = false;
    this.isDraftTable = false;
    this.page = 1;
    this.showResourceRequirementDropdown = false;
    this.deptSelected = false;
    this.showProjectDropdown = false;
    this.showTeamDropdown = false;
    this.showEmployeeRoleDropdown = false;
    this.reset();
    this.getManagerList();
    this.getAllDepartmentList();
    this.getAllPortalConfigData();
    this.employeeObj.employmentstatus = "Probation";
    setTimeout(this.setCalenderMaxDate, 1000);
  }

  showTable() {
    this.isTable = true;

    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.isDraft = false;
    this.isDraftTable = false;
    this.isDeletion = false;
    this.page = 1;
    this.data = '';
    this.filters = {};
    this.workHistoryFilters = {};
    this.filterOnhistory = {};
    this.isSearchEnabled = false;
    this.isworkHistorySearchEnabled = false;
    this.isAuditSearchEnabled = false;

    this.managerList = [];
    this.getAllEmployeeList();
  }

  showDraftTable() {
    this.isDraftTable = true;

    this.isTable = false;
    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.isDraft = false;
    this.isDeletion = false;
    this.page = 1;
    this.data = '';
    this.filters = {};

    this.getAllDraftEmployees();
  }

  reset() {
    this.employeeObj = new Employee();
    //Deafult values for dropdown
    this.employeeObj.gender = '';
    this.employeeObj.maritalStatus = '';
    this.employeeObj.managerId = '';
    this.employeeObj.departmentId = '';
    this.employeeObj.jobRoleId = '';
    this.employeeObj.graduationType = '';
    this.employeeObj.pursuing = '';
    this.employeeObj.experience = '';
    this.employeeObj.workLocation = '';
    this.employeeObj.dateOfBirth = '';
    this.employeeObj.billable = '';
    this.employeeObj.employeeType = '';
    this.employeeObj.defaultprojectType = '';
    this.employeeObj.defaultProjectId = '';
    this.employeeObj.defaultTeamId = '';
    this.employeeObj.isShadowResource = '';
    this.employeeObj.selectedResourceOverviewId = '';
    this.employeeObj.defaultTeamEmployeeRole = [];
    this.allEmployeeList = [];
    this.filteredJobRoleList = [];
    this.allCertificationList = [];
    this.allPreviousEmployment = [];
    this.addInputCertificationField();
    this.addInputPreviousEmployerField();
  }

  showUpdateForm(employee: Employee) {

    this.isForm = true;
    this.referedTypeStatus = true;
    this.isTable = false;
    this.isUpdation = true;
    this.isCreation = false;
    this.isDraft = false;
    this.isDraftTable = false;
    this.isDeletion = false;
    this.isApmosysProductUpdate = false;

    this.applyManagerFilter(employee);
    // this.getManagerList(employee);
    this.getAllDepartmentList();
    this.getAllDomain();
    this.allCertificationList = [];
    this.allPreviousEmployment = [];
    this.updatedCertificationList = [];
    this.updatedPreviousEmployment = [];

    // employee.employeementId = this.utilityService.substringEmployeementid(employee.isConsultant,employee.employeementId);
    employee.employeementId = employee.employeementId?.substring(2)

    this.employeeService.getEmployeeByEmpId(employee).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {

        this.employeeObj = Object.assign({}, response.serviceResponse);
        this.employeeObj.reportiesFlag = 'No';
        if (this.employeeObj.domainList != null) {
          this.getDomainSpecialization();
        }
        this.userEmployeementId = this.employeeObj.employeementId;
        this.employeeObj.totalCurrentExperience=this.employeeService.calculateTotalExperience(
          this.employeeObj.totalExperience, this.employeeObj.dateOfJoining );
        // employee.employeementId = this.utilityService.appendEmployeementid(this.employeeObj.employeementId)

        // if (this.employeeObj.isConsultant == 'true'){
        //   this.employeeObj.employeementId = "A-CS-".concat(this.employeeObj.employeementId);
        // }else{
        //   this.employeeObj.employeementId = "A-".concat(this.employeeObj.employeementId);
        // }
        // this.employeeObj.employeementId = "A-".concat(this.employeeObj.employeementId);
        if (this.employeeObj.isConsultant == 'true')
          this.employeeObj.employeeType = 'Consultant';
        else if (this.employeeObj.isApprenticeship == 'true')
          this.employeeObj.employeeType = 'Apprentice';
        else if (this.employeeObj.isApmosysProduct == 'true') {
          this.employeeObj.employeeType = 'Apmosys Product';
          this.isApmosysProductUpdate = true;
        }
        else
          this.employeeObj.employeeType = 'Regular';

        console.log("employee :", this.employeeObj);
        this.employeeObj.oldEmployeementId = this.employeeObj.employeementId;
        this.employeeObj.oldEmployeeType = this.employeeObj.employeeType;
        // employee.employeementId = this.utilityService.appendEmployeementid(employee.employeementId);
        // Job Role
        if (this.employeeObj.departmentId) {
          this.getJobRolesByDept(this.employeeObj.departmentId, this.employeeObj.jobRoleId);
          this.getDesignationByDeptId(this.employeeObj.departmentId);
        }
      } else {
        console.error(response.serviceResponse)
      }

    });

    setTimeout(this.setCalenderMaxDate, 1000);
    // this.resetEmployee();
  }

  showUpdateDraftForm(employee: Employee) {
    this.isDraft = true;
    this.isForm = false;
    this.isTable = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.isDraftTable = false;

    this.getManagerList();
    this.getAllDepartmentList();
    this.getAllDomain();

    employee.employeementId = this.utilityService.substringEmployeementid(employee.isConsultant, employee.employeementId);
    this.employeeService.getDraftEmployeeByEmpId(employee).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.employeeObj = Object.assign({}, response.serviceResponse);
        console.log("employee :", this.employeeObj);

        // Job Role
        if (this.employeeObj.departmentId) {
          this.getJobRolesByDept(this.employeeObj.departmentId, this.employeeObj.jobRoleId);
        }

        // Certifications
        if (this.employeeObj.certifications == undefined || this.employeeObj.certifications.length == 0) {
          this.addInputCertificationField();
        } else {
          this.allCertificationList = this.employeeObj.certifications;
        }

        // Prev. Employment
        if (this.employeeObj.previousEmploymentList == undefined || this.employeeObj.previousEmploymentList.length == 0) {
          this.addInputPreviousEmployerField();
        } else {
          this.allPreviousEmployment = this.employeeObj.previousEmploymentList;
        }
      } else {
        console.error(response.serviceResponse)
      }
    });

    setTimeout(this.setCalenderMaxDate, 1000);
  }

  // showAllDomain(){
  //   this.isDomain = true;
  //   this.isDomainTable = true;

  //   this.isDomainCreation = false;
  //   this.isDomainUpdation = false;
  //   this.isDomainForm = false;

  //   this.isForm = false;
  //   this.isTable = false;
  //   this.isUpdation = false;
  //   this.isCreation = false;
  //   this.isDraft = false;
  //   this.isDraftTable = false;
  //   this.isDeletion = false;
  //   this.getAllDomain(this.alertTemplate);
  // }

  // showCreateDomainForm(){
  //   this.isDomainCreation = true;
  //   this.isDomainForm = true;

  //   this.isDomain = false;
  //   this.isDomainTable = false;
  //   this.isDomainUpdation = false;

  //   this.isForm = false;
  //   this.isTable = false;
  //   this.isUpdation = false;
  //   this.isCreation = false;
  //   this.isDraft = false;
  //   this.isDraftTable = false;
  //   this.isDeletion = false;

  //   this.domainObj = new Domain();
  //   this.allSpecializationList = [];

  //   //Template Activity
  //   if (this.domainObj.allSpecializationList == undefined || this.domainObj.allSpecializationList.length == 0) {
  //     this.addInputSpecializationField();
  //   } else {
  //     this.allSpecializationList = this.domainObj.allSpecializationList;
  //   }
  // }

  // showUpdateDomainForm(domain:any){
  //   this.isDomainForm = true;
  //   this.isDomainUpdation = true;

  //   this.isDomainCreation = false;
  //   this.isDomain = false;
  //   this.isDomainTable = false;

  //   this.isForm = false;
  //   this.isTable = false;
  //   this.isUpdation = false;
  //   this.isCreation = false;
  //   this.isDraft = false;
  //   this.isDraftTable = false;
  //   this.isDeletion = false;

  //   this.domainService.getDomainSpecializationByDomainId(domain).pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus == "Success") {
  //       this.domainObj = Object.assign({}, response.serviceResponse);

  //       this.allSpecializationList = this.domainObj.allSpecializationList;

  //       console.log(response.serviceResponse, " : response.serviceResponse");
  //     } else {
  //       console.error(response.serviceResponse);
  //     }
  //   });
  // }

  // Manage Domain / Specialization
  // addInputSpecializationField(){
  //   let domainObj = new Domain();
  //   this.allSpecializationList.push(domainObj);
  //   console.log(this.allSpecializationList, " : this.allSpecializationList");
  // }

  // removeInputSpecializationField(spec:any){
  //   this.allSpecializationList.forEach((value, index) => {
  //     if (value == spec) {
  //       this.allSpecializationList.splice(index, 1);
  //     }
  //   });
  //   console.log(this.allSpecializationList, " :this.allSpecializationList");
  // }

  // Manage employer
  addInputPreviousEmployerField() {
    let newPrevEmployerObj = new PreviousEmployer();
    this.allPreviousEmployment.push(newPrevEmployerObj);
  }

  removeInputPreviousEmployerField(prevEmployerObj) {
    this.allPreviousEmployment.forEach((value, index) => {
      if (value == prevEmployerObj) {
        this.updatedPreviousEmployment.push(value);
        this.allPreviousEmployment.splice(index, 1);
      }

    });
  }

  // Manage Certifications
  addInputCertificationField() {
    let newCertificationObj = new certification();
    // newCertificationObj.certificationId = "";
    // newCertificationObj.duration = "";
    // newCertificationObj.modeOfCourse = "";

    this.allCertificationList.push(newCertificationObj);
  }

  removeInputCertificationField(certificationObj) {
    this.allCertificationList.forEach((value, index) => {
      if (value == certificationObj) {
        this.updatedCertificationList.push(value);
        this.allCertificationList.splice(index, 1);
      }
    });
  }


  // getTeamsByProjectName(projectName,template: TemplateRef<any>){
  //   //console.log(" projectName   ",projectName);
  //   this.employeeObj.teamName = "";
  //   this.employeeService.getTeamByProjectName(projectName).pipe(first()).subscribe((response : any)=>{
  //     if(response.serviceStatus == "Success"){
  //       this.teamList = response.serviceResponse;
  //       //console.log(" team list success   ",this.teamList);
  //     }else{
  //       this.openAlertMod(template , response.serviceResponse);
  //       console.log(" in fail ")
  //     }
  //   });

  // }

  validateEmployeeObj(employeeObj: Employee, template: TemplateRef<any>) {

    // if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.employeementId)) {
    //   this.alertMessage = "Please enter Employment Id !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // } else if (!this.validationService.validateEmployeementId(employeeObj.employeementId)) {
    //   this.alertMessage = "Please enter valid Employment ID !!";
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }
    employeeObj.name = this.employeeObj.name?.trim();
    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.name)) {
      this.alertMessage = "Please enter Full Name !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateAlphaWithSpace(employeeObj.name)) {
      this.alertMessage = "Please enter Valid Full Name !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.email)) {
      this.alertMessage = "Please enter email id !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateEmail(employeeObj.email)) {
      this.alertMessage = "Please enter valid email id !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.secondaryEmail)) {
      this.alertMessage = "Please enter Secondary email id !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateEmail(employeeObj.secondaryEmail)) {
      this.alertMessage = "Please enter valid Secondary email id !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    // if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.gender)) {
    //   this.alertMessage = "Please select gender !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // } 

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.dateOfBirth)) {
      this.alertMessage = "Please enter date of birth !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    // if (this.validationService.validateNullUndefinedEmptyString(employeeObj.bloodGroup) && !this.validationService.validateBloodGroup(employeeObj.bloodGroup)) {
    //   this.alertMessage = "Please enter Valid Blood Group !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.fatherName)) {
    //   this.alertMessage = "Please enter father name !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // } else if (!this.validationService.validateAlphaWithSpace(employeeObj.fatherName)) {
    //   this.alertMessage = "Please enter Valid father Name !!";
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (this.validationService.validateNullUndefinedEmptyString(employeeObj.placeOfBirth) && !this.validationService.validateAlphaWithSpace(employeeObj.placeOfBirth)) {
    //   this.alertMessage = "Please enter Valid Place of birth !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (this.validationService.validateNullUndefinedEmptyString(employeeObj.motherTongue) && !this.validationService.validateAlphaWithSpace(employeeObj.motherTongue)) {
    //   this.alertMessage = "Please enter Valid Mother tongue number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (this.validationService.validateNullUndefinedEmptyString(employeeObj.passportNumber) && !this.validationService.validatePassportNumber(employeeObj.passportNumber)) {
    //   this.alertMessage = "Please enter Valid Passport number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.aadhar)) {
    //   this.alertMessage = "Please enter aadhar card number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // } else if (employeeObj.aadhar.toString().length != 12) {
    //   this.alertMessage = "Please enter Valid aadhar card number !!";
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.panNumber)) {
    //   this.alertMessage = "Please enter PAN card number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // } else if (!this.validationService.validatePancardNumber(employeeObj.panNumber)) {
    //   this.alertMessage = "Please enter Valid PAN card number !!";
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.mobileNo)) {
      this.alertMessage = "Please enter mobile number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateMobileNumber(employeeObj.mobileNo)) {
      this.alertMessage = "Please enter valid mobile number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    // if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.address)) {
    //   this.alertMessage = "Please enter Current Address !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.state)) {
    //   this.alertMessage = "Please enter state !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // } else if (!this.validationService.validateAlphaWithSpace(employeeObj.state)) {
    //   this.alertMessage = "Please enter Valid state !!";
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.city)) {
    //   this.alertMessage = "Please enter city !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // } else if (!this.validationService.validateAlphaWithSpace(employeeObj.city)) {
    //   this.alertMessage = "Please enter Valid city !!";
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.country)) {
    //   this.alertMessage = "Please enter country !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // } else if (!this.validationService.validateAlphaWithSpace(employeeObj.country)) {
    //   this.alertMessage = "Please enter Valid country !!";
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.pincode)) {
    //   this.alertMessage = "Please enter pincode !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // } else if (!this.validationService.validatePincodeNumber(employeeObj.pincode)) {
    //   this.alertMessage = "Please enter Valid pincode !!";
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (this.validationService.validateNullUndefinedEmptyString(employeeObj.alternateMobileNo) && !this.validationService.validateMobileNumber(employeeObj.alternateMobileNo)) {
    //   this.alertMessage = "Please enter valid alternate mobile number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.permanentAddress)) {
    //   this.alertMessage = "Please enter permanent address !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (this.validationService.validateNullUndefinedEmptyString(employeeObj.emergencyContactPerson) && !this.validationService.validateAlphaWithSpace(employeeObj.emergencyContactPerson)) {
    //   this.alertMessage = "Please enter Valid Emergency Contact Person Name !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (this.validationService.validateNullUndefinedEmptyString(employeeObj.relation) && !this.validationService.validateAlphaWithSpace(employeeObj.relation)) {
    //   this.alertMessage = "Please enter Valid Emergency Contact Person Relation !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (this.validationService.validateNullUndefinedEmptyString(employeeObj.emergencyContactMobile) && !this.validationService.validateMobileNumber(employeeObj.emergencyContactMobile)) {
    //   this.alertMessage = "Please enter Valid Emergency Contact Person Mobile Number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.dateOfJoining)) {
      this.alertMessage = "Please enter date of joining !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.reportiesFlag) && this.isUpdation) {
      this.alertMessage = "Please enter 'Do you want to change the reporting of your reportees ?' "
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    // if (!this.validationService.validateNullUndefinedEmptyString(this.employeeObj.billable)) {
    //   this.alertMessage = "Please select a value for 'Billable'";
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (this.employeeObj.billable === 'No' &&
    //   !this.validationService.validateNullUndefinedEmptyString(this.employeeObj.billableType)) {
    //   this.alertMessage = "Please select a value for 'Billable Type' when Billable is set to 'No'";
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (this.employeeObj.billable === 'Yes' &&
    //   !this.validationService.validateNullUndefinedEmptyString(this.employeeObj.billableType)) {
    //   this.alertMessage = "Please select a value for 'Billable Type' when Billable is set to 'Yes'";
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }



    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.employmentstatus)) {
      this.alertMessage = "Please enter employment status !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.probationPeriod)) {
      this.alertMessage = "Please enter Probation period !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } if (employeeObj.probationPeriod > 365 || employeeObj.probationPeriod < 0) {
      this.alertMessage = "Please enter value 0 to 365 in probation period field !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } if (!this.validationService.validateNumber(employeeObj.probationPeriod)) {
      this.alertMessage = "Please enter valid probation period !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.noticePeriod)) {
      this.alertMessage = "Please enter notice period !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } if (employeeObj.noticePeriod > 365 || employeeObj.noticePeriod < 0) {
      this.alertMessage = "Please enter value 0 to 365 in notice period field !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } if (!this.validationService.validateNoticePeriod(employeeObj.noticePeriod)) {
      this.alertMessage = "Please enter valid notice period !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (employeeObj.employmentstatus == "Resigned") {
      if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.dateOfResign)) {
        this.alertMessage = "Please enter date of Resign !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    } else if (employeeObj.employmentstatus == "InActive") {
      if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.dateOfResign)) {
        this.alertMessage = "Please enter date of Resign !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.dateOfRelieving)) {
        this.alertMessage = "Please enter date of Relieving !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.managerId)) {
      this.alertMessage = "Please select manager !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (employeeObj.reportingManagerId) {
      if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.approvalsTo)) {
        this.alertMessage = "Please select Approvals To !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.departmentId)) {
      this.alertMessage = "Please select Department !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.designationId)) {
      this.alertMessage = "Please select Designation !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.defaultprojectType) && !this.isUpdation) {
      this.alertMessage = "Please select Default project Type !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.defaultProjectId) && !this.isUpdation) {
      this.alertMessage = "Please select Default project  !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.defaultTeamId) && !this.isUpdation) {
      this.alertMessage = "Please select Default Team !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if (!this.isUpdation) {
      if ((employeeObj.defaultTeamEmployeeRole.length === 0 || !employeeObj.defaultTeamEmployeeRole)) {
        this.alertMessage = "Please select Employee Role In Default Project !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }




    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.jobRoleId)) {
      this.alertMessage = "Please select Job Role !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.experience)) {
      this.alertMessage = "Please select experience !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if (employeeObj.experience == 'Experienced') {
      if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.totalExperience)) {
        this.alertMessage = "Please enter total previous work experience !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      if (employeeObj.totalExperience === 0) {
        this.alertMessage = "Please enter more than 0 number !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      if (!this.validationService.validateExperiencedNumber(employeeObj.totalExperience)) {
        this.alertMessage = "Please enter valid experience in Format (Years.Months)  !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      } if (employeeObj.totalExperience > 60) {
        this.alertMessage = "Please enter value 1 to 60(yrs) in total previous work experience field !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.workLocation)) {
      this.alertMessage = "Please select employee Work Location !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    // if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.billable)) {
    //   this.alertMessage = "Please select billable !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }
    if (employeeObj.employmentstatus == 'Resigned') {
      if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.dateOfResign)) {
        this.alertMessage = "Please Enter Resign Date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }

    // if(this.isUpdation){
    //   if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.domainList)) {
    //     this.alertMessage = "Please select Domain !!"
    //     this.openAlertMod(template, this.alertMessage);
    //     return false;
    //   }else if(employeeObj.domainList.length == 0){
    //     this.alertMessage = "Please select Domain !!"
    //     this.openAlertMod(template, this.alertMessage);
    //     return false;
    //   }

    //   if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.specializationList)) {
    //     this.alertMessage = "Please select Specialization(s) !!"
    //     this.openAlertMod(template, this.alertMessage);
    //     return false;
    //   }else if(employeeObj.specializationList.length == 0){
    //     this.alertMessage = "Please select Specialization(s) !!"
    //     this.openAlertMod(template, this.alertMessage);
    //     return false;
    //   }
    // }

    // if (this.validationService.validateNullUndefinedEmptyString(employeeObj.bankName) && !this.validationService.validateAlphaWithSpace(employeeObj.bankName)) {
    //   this.alertMessage = "Please enter Valid Bank Name !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (this.validationService.validateNullUndefinedEmptyString(employeeObj.bankAccountNo) && !this.validationService.validateAlphaNumeric(employeeObj.bankAccountNo)) {
    //   this.alertMessage = "Please enter Valid Bank Account Number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (this.validationService.validateNullUndefinedEmptyString(employeeObj.bankIFSCCode) && !this.validationService.validateAlphaNumeric(employeeObj.bankIFSCCode)) {
    //   this.alertMessage = "Please enter Valid Bank IFSC Code !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (this.validationService.validateNullUndefinedEmptyString(employeeObj.pfAccountNumber) && !this.validationService.validateAlphaNumeric(employeeObj.pfAccountNumber)) {
    //   this.alertMessage = "Please enter Valid PF Account Number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    // if (this.validationService.validateNullUndefinedEmptyString(employeeObj.previousPfAccountNumber) && !this.validationService.validateAlphaNumeric(employeeObj.previousPfAccountNumber)) {
    //   this.alertMessage = "Please enter Valid Previous PF Account Number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }
    // if (this.validationService.validateNullUndefinedEmptyString(employeeObj.uan) && !this.validationService.validateAlphaNumeric(employeeObj.uan)) {
    //   this.alertMessage = "Please enter Valid UAN Number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }
    // if (this.validationService.validateNullUndefinedEmptyString(employeeObj.esicNumber) && !this.validationService.validateAlphaNumeric(employeeObj.esicNumber)) {
    //   this.alertMessage = "Please enter Valid ESIC Number !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }
    return true;
  }

  validateEmployeeDraftObj(employeeObj: Employee, template: TemplateRef<any>) {

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.employeementId)) {
      this.alertMessage = "Please enter Emp Id !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateEmployeementId(employeeObj.employeementId)) {
      this.alertMessage = "Please enter valid Employeement ID !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.dateOfBirth)) {
      this.alertMessage = "Please enter date of birth !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.name)) {
      this.alertMessage = "Please enter Full Name !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateAlphaWithSpace(employeeObj.name)) {
      this.alertMessage = "Please enter Valid Full Name !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.email)) {
      this.alertMessage = "Please enter email id !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateEmail(employeeObj.email)) {
      this.alertMessage = "Please enter valid email id !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.dateOfJoining)) {
      this.alertMessage = "Please enter date of joining !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.employmentstatus)) {
      this.alertMessage = "Please enter employment status !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.managerId)) {
      this.alertMessage = "Please select manager !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.departmentId)) {
      this.alertMessage = "Please select Department !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.jobRoleId)) {
      this.alertMessage = "Please select Job Role !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(employeeObj.billable)) {
      this.alertMessage = "Please select billable !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    return true;
  }

  omit_character(event) {
    var k;
    k = event.charCode;
    if ((k == 45) || (k == 69) || (k == 101)) {
      return (false);
    }
    return (true);
  }

  fieldRestictCharacter(event) {
    var k;
    k = event.charCode;

    if ((event.target as HTMLInputElement).value.length >= 6) {
      event.preventDefault();
      return false;
    }
    if ((k == 33) || (k == 34) || (k == 35) || (k == 36) || (k == 37) ||
      (k == 38) || (k == 39) || (k == 40) || (k == 41) || (k == 42) ||
      (k == 43) || (k == 44) || (k == 46) || (k == 47) || (k == 58) ||
      (k == 59) || (k == 60) || (k == 61) || (k == 62) || (k == 63) ||
      (k == 64) || (k == 66) || (k == 67) || (k == 68) || (k == 69) ||
      (k == 70) || (k == 71) || (k == 72) || (k == 73) || (k == 74) ||
      (k == 75) || (k == 76) || (k == 77) || (k == 78) || (k == 79) ||
      (k == 80) || (k == 81) || (k == 82) || (k == 83) || (k == 84) ||
      (k == 85) || (k == 86) || (k == 87) || (k == 88) || (k == 89) ||
      (k == 90) || (k == 91) || (k == 92) || (k == 93) || (k == 94) ||
      (k == 95) || (k == 96) || (k == 97) || (k == 98) || (k == 99) ||
      (k == 99) || (k == 100) || (k == 101) || (k == 102) || (k == 103) ||
      (k == 104) || (k == 105) || (k == 106) || (k == 107) || (k == 108) ||
      (k == 109) || (k == 110) || (k == 111) || (k == 112) || (k == 113) ||
      (k == 114) || (k == 115) || (k == 116) || (k == 117) || (k == 118) ||
      (k == 119) || (k == 120) || (k == 121) || (k == 122) || (k == 123) ||
      (k == 124) || (k == 125) || (k == 126)) {
      return (false);
    }
    return (true);

  }

  fieldRestictCharacterCS(event) {
    var k;
    k = event.charCode;
    if ((k == 33) || (k == 34) || (k == 35) || (k == 36) || (k == 37) ||
      (k == 38) || (k == 39) || (k == 40) || (k == 41) || (k == 42) ||
      (k == 43) || (k == 44) || (k == 46) || (k == 47) || (k == 58) ||
      (k == 59) || (k == 60) || (k == 61) || (k == 62) || (k == 63) ||
      (k == 64) || (k == 66) || (k == 68) || (k == 69) ||
      (k == 70) || (k == 71) || (k == 72) || (k == 73) || (k == 74) ||
      (k == 75) || (k == 76) || (k == 77) || (k == 78) || (k == 79) ||
      (k == 80) || (k == 81) || (k == 82) || (k == 84) ||
      (k == 85) || (k == 86) || (k == 87) || (k == 88) || (k == 89) ||
      (k == 90) || (k == 91) || (k == 92) || (k == 93) || (k == 94) ||
      (k == 95) || (k == 96) || (k == 97) || (k == 98) || (k == 99) ||
      (k == 99) || (k == 100) || (k == 101) || (k == 102) || (k == 103) ||
      (k == 104) || (k == 105) || (k == 106) || (k == 107) || (k == 108) ||
      (k == 109) || (k == 110) || (k == 111) || (k == 112) || (k == 113) ||
      (k == 114) || (k == 115) || (k == 116) || (k == 117) || (k == 118) ||
      (k == 119) || (k == 120) || (k == 121) || (k == 122) || (k == 123) ||
      (k == 124) || (k == 125) || (k == 126)) {
      return (false);
    }
    return (true);

  }

  // 97 to 122

  // CRUD

  onCreateEmployee(template: TemplateRef<any>) {
    const dateFormat = 'YYYY-MM-DD';
    console.log("allCertificationList : ", this.allCertificationList);
    console.log("allPreviousEmployment : ", this.allPreviousEmployment);

    if (!this.employeeObj.employeementId || this.employeeObj.employeementId == null || this.employeeObj.employeementId.toString().trim() == "") {
      this.openAlertMod(template, "Please enter valid employeement Id");
      return;
    }

    let inputValidated: boolean = this.validateEmployeeObj(this.employeeObj, template)
    if (!inputValidated) return;

    this.employeeObj.isDraft = false;
    // transform date formats to YYYY-MM-DD
    this.employeeObj.dateOfBirth = moment(this.employeeObj.dateOfBirth).format(dateFormat);
    this.employeeObj.dateOfJoining = moment(this.employeeObj.dateOfJoining).format(dateFormat);


    this.employeeObj.certifications = (Object.keys(this.allCertificationList[0]).length === 0) ? null : this.allCertificationList;
    if (this.employeeObj.certifications) {
      this.employeeObj.certifications.forEach(certificate => {
        certificate.dateOfCompletion = moment(certificate.dateOfCompletion).format(dateFormat);
      });
    }
    this.employeeObj.previousEmploymentList = (Object.keys(this.allPreviousEmployment[0]).length === 0) ? null : this.allPreviousEmployment;
    this.employeeObj.createdBy = this.currentUser.empId;
    console.log("Create Employe : ", this.employeeObj);
    let employee = Object.assign({}, this.employeeObj)
    // employee.employeementId = this.utilityService.substringEmployeementid(this.employeeObj.isConsultant, this.employeeObj.employeementId);

    // if(this.employeeObj.employeementId.startsWith('A-CS-')){
    //   employee.employeementId  = this.employeeObj.employeementId.substring(5);
    //   console.log("Employee :", this.employeeObj);
    // }else if(this.employeeObj.employeementId.startsWith('A-')){
    //   employee.employeementId  = this.employeeObj.employeementId.substring(2);
    //   console.log("Employee :", this.employeeObj);
    // }else {
    //   employee.employeementId  = this.employeeObj.employeementId
    // }

    if (this.employeeObj.employeementId.startsWith('A-')) {
      employee.employeementId = this.employeeObj.employeementId.substring(2);
      console.log("Employee :", this.employeeObj);
    } else {
      employee.employeementId = this.employeeObj.employeementId
    }

    employee.onbenchDate = this.billableBenchDate;

    if (!this.validationService.validateEmployeementId(employee.employeementId)) {
      this.alertMessage = "Please enter a valid Employment ID !!";
      this.openAlertMod(template, this.alertMessage);
      return;
    }

    console.log("on create", employee);

    this.employeeService.createEmployee(employee).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.employeeService.deleteDraftEmployee(this.employeeObj); // deleting draft once employee is created
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }

    });
  }


  getEmpIdPrefix(employeeType: string): string {
    switch (employeeType) {
      case 'Consultant':
        return 'CS-';
      case 'Apmosys Product':
        return 'AP-';
      default:
        return 'A-';
    }
  }

  checkEmployeementId(template: TemplateRef<any>) {
    let employee = new Employee();
    employee.empId = this.employeeObj.empId;
    employee.email = this.employeeObj.email;

    console.log("employeeid with space", this.employeeObj.employeementId);

    if (this.employeeObj.employeementId.startsWith('A-')) {
      this.employeeObj.employeementId = this.employeeObj.employeementId.substring(2);
      if (!this.validationService.validateNullUndefinedEmptyString(this.employeeObj.employeementId)) {
        this.alertMessage = "Please enter Employee ID !!"
        this.openAlertMod(template, this.alertMessage);
        // this.employeeObj.employeementId = 'A-'+this.employeeObj.employeementId;
        return false;
      }
      // employee.employeementId  = this.employeeObj.employeementId.substring(2);
      console.log("Employee :", this.employeeObj);
    } else if (this.employeeObj.employeementId.startsWith('A-')) {
      if (!this.validationService.validateNullUndefinedEmptyString(this.employeeObj.employeementId)) {
        this.alertMessage = "Please enter Employee ID !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      employee.employeementId = this.employeeObj.employeementId.substring(2);
      console.log("Employee :", this.employeeObj);
    }
    else {
      employee.employeementId = this.employeeObj.employeementId
      if (!this.validationService.validateNullUndefinedEmptyString(employee.employeementId)) {
        this.alertMessage = "Please enter Employment ID !!";
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if (!this.validationService.validateEmployeementId(employee.employeementId)) {
        console.log("employeementid please enter valid employmentid", employee.employeementId, this.employeeObj.employeementId);
        this.alertMessage = "Please enter valid Employment ID !!";
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }
    this.employeeService.checkEmployeementId(employee).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Fail") {
        this.openAlertMod(template, response.serviceResponse);
        employee.employeementId = '';
      }
      this.employeeObj.employeementId = 'A-' + this.employeeObj.employeementId;
      console.log("checkEmployeementId response: ", response);
    });
  }


  checkEmployeementIdWithDifferentPrefix(template: TemplateRef<any>) {
    let employee = new Employee();
    employee.empId = this.employeeObj.empId;
    employee.email = this.employeeObj.email;

    const prefix = this.getEmpIdPrefix(this.employeeObj.employeeType);
    const enteredId = this.employeeObj.employeementId;

    if (!this.validationService.validateNullUndefinedEmptyString(enteredId)) {
      this.alertMessage = "Please enter Employment ID !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateEmployeementId(enteredId)) {
      this.alertMessage = "Please enter valid Employment ID !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    // Final ID with prefix
    employee.employeementId = enteredId;
    employee.employeeType = this.employeeObj.employeeType;
    console.log("EmployeeId ", employee.employeementId, "Employee Type ", employee.employeeType);
    this.employeeService.checkEmployeementId(employee).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Fail") {
        this.openAlertMod(template, response.serviceResponse);
        if (this.isUpdation) {
          this.employeeObj.employeementId = this.employeeObj.employeementId;
          employee.employeementId = this.employeeObj.employeementId;
        } else {
          employee.employeementId = '';
          this.employeeObj.employeementId = '';
        }
      }
      console.log("checkEmployeementId response: ", response);
    });
  }





  // checkEmployeementId(template: TemplateRef<any>) {
  //   let employee = new Employee();
  //   employee.employeementId = this.utilityService.substringEmployeementid2(this.employeeObj.employeementId);
  //   console.log("chechEmpId() employee.employeementId: ",employee.employeementId);
  //   employee.email = this.employeeObj.email;
  //   employee.empId = this.employeeObj.empId;
  //   if (!this.validationService.validateNullUndefinedEmptyString(employee.employeementId)) {
  //         this.alertMessage = "Please Enter Employment ID !!";
  //         this.openAlertMod(template, this.alertMessage);
  //         return false;
  //   }
  //   if (!this.validationService.validateEmployeementId(employee.employeementId)) {
  //     this.alertMessage = "Please Enter Valid Employment ID !!";
  //     this.openAlertMod(template, this.alertMessage);
  //     this.employeeObj.employeementId = '';
  //   }
  //   this.employeeService.checkEmployeementId(employee).pipe(first()).subscribe((response: any) => {
  //     console.log("EMPLY-ID response.serviceResponse: ",response.serviceResponse);
  //     if (response.serviceStatus == "Fail") {
  //       this.openAlertMod(template, response.serviceResponse);
  //       this.employeeObj.employeementId = '';
  //     }
  //   });
  // }

  checkEmail(template: TemplateRef<any>) {

    let employee = new Employee();
    // employee.employeementId = this.utilityService.getEmployeeIdSubstring(this.employeeObj);
    employee.employeementId = this.employeeObj.employeementId.substring(2);
    console.error("employee.employeementId ", employee.employeementId);
    employee.email = this.employeeObj.email;
    employee.empId = this.employeeObj.empId;
    console.log("checkEmail() triggered with email:", employee.email);
    if (!this.validationService.validateNullUndefinedEmptyString(employee.email)) {
      this.alertMessage = "Please Enter Email ID !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateApmosysEmail(this.employeeObj.email, this.employeeObj.employeeType)) {
      this.alertMessage = "Please Enter Valid Email ID !!"
      this.openAlertMod(template, this.alertMessage);
      this.employeeObj.email = '';
      return false;
    }
    this.employeeService.checkEmployeeEmail(employee).pipe(first()).subscribe((response: any) => {
      console.log("EMAIL-response.serviceResponse: ", response.serviceResponse);
      if (response.serviceStatus == "Fail") {
        this.openAlertMod(template, response.serviceResponse);
        this.employeeObj.email = '';
      }
    });
  }

  // checkEmail(template: TemplateRef<any>) {
  //   let employee = new Employee();
  //   employee.employeementId = this.utilityService.substringEmployeementid(this.employeeObj.employeementId)
  //   console.log("checkEmail employee.employeementId: ",employee.employeementId);
  //   const regex = /^(?:[0-9]+[a-z_.]|[a-z_.])[a-z0-9_.]+@apmosys\.com$/i;
  //   if (this.employeeObj.email != null)
  //   {

  //     if (regex.test(this.employeeObj.email)) {
  //       employee.email = this.employeeObj.email;
  //       employee.empId = this.employeeObj.empId;
  //       this.employeeService.checkEmployeeEmail(employee).pipe(first()).subscribe((response: any) => {
  //         if (response.serviceStatus == "Fail") {
  //           this.openAlertMod(template, response.serviceResponse);
  //           this.employeeObj.email = '';
  //         }
  //         console.log("checkEmployeeEmail response: ",response);
  //       });
  //     }else {
  //       this.openAlertMod(template, "Please enter valid email id... !!");
  //       this.employeeObj.email = '';

  //     }

  //     if (!this.validationService.validateEmail(this.employeeObj.email)) {
  //       this.alertMessage = "Please enter valid email id !!"
  //       this.openAlertMod(template, this.alertMessage);
  //       this.employeeObj.email = '';
  //       return false;
  //     }
  //   }
  // }

  checkSecondaryEmail(template: TemplateRef<any>) {

    const regex = /^(?:[0-9]+[a-z_.]|[a-z_.])[a-z0-9_.]+@apmosys\.com$/i;
    // const regex = /^[A-Za-z0-9._%+-]+@apmosys\.com$/;
    if (this.validationService.validateNullUndefinedEmptyString(this.employeeObj.secondaryEmail)) {
      if (regex.test(this.employeeObj.secondaryEmail)) {

        this.openAlertMod(template, "Apmosys mail Id is not valid in secondary mail !!");
        this.employeeObj.secondaryEmail = '';
      }
    }
  }

  // added by anurag on field validation

  ValidateName(template: TemplateRef<any>) {
    this.employeeObj.name = this.employeeObj.name?.trim();
    if (!this.validationService.validateNullUndefinedEmptyString(this.employeeObj.name)) {
      this.alertMessage = "Please enter Full Name !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateAlphaWithSpace(this.employeeObj.name)) {
      this.alertMessage = "Please enter Valid Full Name !!";
      this.openAlertMod(template, this.alertMessage);
      this.employeeObj.name = '';
      return false;
    }
  }

  checkEmployeeMobileNo(template: TemplateRef<any>) {
    let employee = new Employee();
    employee.employeementId = this.utilityService.getEmployeeIdSubstring(this.employeeObj.isConsultant);
    employee.mobileNo = this.employeeObj.mobileNo;

    if (!this.validationService.validateMobileNumber(employee.mobileNo)) {
      this.alertMessage = "Enter Valid Mobile Number !!";
      this.openAlertMod(template, this.alertMessage);
      this.employeeObj.mobileNo = '';
    }

    else if (!this.validationService.validateNullUndefinedEmptyString(employee.mobileNo)) {
      this.alertMessage = "Please enter Mobile Number !!";
      this.openAlertMod(template, this.alertMessage);
      this.employeeObj.mobileNo = '';
    }

    else {

      this.employeeService.checkEmployeeMobileNo(employee).pipe(first()).subscribe((response: any) => {
        console.log("MOB No- response.serviceResponse: ", response.serviceResponse);
        if (response.serviceStatus == "Fail") {
          this.openAlertMod(template, response.serviceResponse);
          this.employeeObj.mobileNo = '';
        }
      });
    }
  }

  // checkEmployeeMobileNo(template: TemplateRef<any>) {
  //   let employee = new Employee();
  //   employee.employeementId = this.utilityService.substringEmployeementid(this.employeeObj.employeementId);
  //   employee.mobileNo = this.employeeObj.mobileNo;

  //   console.log(employee, " : employee");

  //   this.employeeService.checkEmployeeMobileNo(employee).pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus == "Fail") {
  //       this.openAlertMod(template, response.serviceResponse);
  //       this.employeeObj.mobileNo = '';
  //     }changeManagerMapping
  //   });
  // }

  checkEmployeeAadharNumber(template: TemplateRef<any>) {
    this.employeeService.checkEmployeeAadharNumber(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Fail") {
        this.openAlertMod(template, response.serviceResponse);
        this.employeeObj.aadhar = '';
      }
    });
  }
  updateDesignationDropdown() {
    this.employeeObj.designationId = ''; // Set designation to 'Select' option
  }

  isDesignationDisabled() {
    return !this.employeeObj.departmentId || this.employeeObj.designationId;
  }

  checkEmployeePanNumber(template: TemplateRef<any>) {
    this.employeeService.checkEmployeePanNumber(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Fail") {
        this.openAlertMod(template, response.serviceResponse);
        this.employeeObj.panNumber = '';
      }
    });
  }

  // clearAfterChange(field, fieldname) {
  //   let element: any = document.getElementById(field);
  //   if (this.employeeObj.experience === 'Fresher') {
  //     if (element) element.value = '';
  //     this.employeeObj[fieldname] = '';
  // }
  // }

  onUpdateEmployee(template: TemplateRef<any>) {
    const dateFormat = 'YYYY-MM-DD';
    let inputValidated: boolean = this.validateEmployeeObj(this.employeeObj, template)
    if (!inputValidated) return;
    if (!this.employeeObj.employeementId || this.employeeObj.employeementId == null || this.employeeObj.employeementId.toString().trim() == "") {
      this.openAlertMod(template, "Please enter valid employeement Id");
      return;
    }
    this.employeeObj.imageBytes = null;
    this.employeeObj.isDraft = false;
    // transform date formats to YYYY-MM-DD
    if (this.employeeObj.dateOfBirth) this.employeeObj.dateOfBirth = moment(this.employeeObj.dateOfBirth).format(dateFormat)
    if (this.employeeObj.dateOfJoining) this.employeeObj.dateOfJoining = moment(this.employeeObj.dateOfJoining).format(dateFormat)
    if (this.employeeObj.employeeConfirmationDate) this.employeeObj.employeeConfirmationDate = moment(this.employeeObj.employeeConfirmationDate).format(dateFormat)
    if (this.employeeObj.dateOfResign) this.employeeObj.dateOfResign = moment(this.employeeObj.dateOfResign).format(dateFormat)
    if (this.employeeObj.dateOfRetain) this.employeeObj.dateOfRetain = moment(this.employeeObj.dateOfRetain).format(dateFormat)

    if (this.employeeObj.employmentstatus == "Confirmed" || this.employeeObj.employmentstatus == "Probation") {
      this.employeeObj.dateOfResign = null;
      this.employeeObj.dateOfRelieving = null;
    }

    this.employeeObj.updatedBy = this.currentUser.empId;;
    console.log("Update Employe : ", this.employeeObj);

    let employee = Object.assign({}, this.employeeObj);
    employee.updatedBy = this.currentUser.empId;

    // if(this.employeeObj.employeementId.startsWith('A-CS-')){
    //   employee.employeementId  = this.employeeObj.employeementId.substring(5);
    //   console.log("Employee :", this.employeeObj);
    // }else if(this.employeeObj.employeementId.startsWith('A-')){
    //   employee.employeementId  = this.employeeObj.employeementId.substring(2);
    //   console.log("Employee :", this.employeeObj);
    // }else {
    //   employee.employeementId  = this.employeeObj.employeementId
    // }

    // if (this.employeeObj.employeementId.startsWith('A-')) {
    //   employee.employeementId = this.employeeObj.employeementId.substring(2);
    //   console.log("Employee :", this.employeeObj);
    // } else {
    //   employee.employeementId = this.employeeObj.employeementId
    // }

    employee.specializationList = this.employeeObj.specializationList;
    if (this.employeeObj.reportingManagerId == null || this.employeeObj.reportingManagerId == "") {
      employee.reportingManagerId = null;
      employee.approvalsTo = null;
    }

    if (this.employeeObj.reportingManagerId == null || this.employeeObj.reportingManagerId == "") {
      employee.reportingManagerId = null;
      employee.approvalsTo = null;
    }
    employee.newManagerId = this.employeeObj.newManagerId;
    employee.reportiesFlag = this.employeeObj.reportiesFlag;
    employee.referedType == this.employeeObj.referedType;
    employee.referedName == this.employeeObj.referedName;

    console.log("employee update before call ", employee);

    if (this.employeeObj.employeeType === 'Consultant') {
      employee.isConsultant = 'true';
      employee.isApprenticeship = 'false';
      employee.isApmosysProduct = 'false';
    } else if (this.employeeObj.employeeType === 'Apprentice') {
      employee.isConsultant = 'false';
      employee.isApprenticeship = 'true';
      employee.isApmosysProduct = 'false';
    } else if (this.employeeObj.employeeType === 'Apmosys Product') {
      employee.isConsultant = 'false';
      employee.isApprenticeship = 'false';
      employee.isApmosysProduct = 'true';
    } else {
      employee.isConsultant = 'false';
      employee.isApprenticeship = 'false';
      employee.isApmosysProduct = 'false';
    }

    employee.onbenchDate = this.billableBenchDate;

    this.employeeService.updateEmployee(employee).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
        this.employeeObj = null;
        // setTimeout(() => {
        //   window.location.reload();
        // }, 4000);

      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onDeleteEmployee(updatetemplate: TemplateRef<any>, template: TemplateRef<any>) {
    this.cancelRequest();
    // if(this.employeeObj.isConsultant === true){
    //   this.employeeObj.employeementId = this.employeeObj.employeementId.substring(5);
    // }else{
    //   this.employeeObj.employeementId = this.employeeObj.employeementId.substring(2);
    // }
    this.employeeObj.employeementId = this.employeeObj.employeementId.substring(2);
    this.employeeService.deleteEmployee(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.isDeletion = true;
        this.employeeObj.oldManagerId = this.employeeObj.empId;
        this.employeeObj.newManagerId = '';
        this.getManagerList(this.employeeObj);
        this.modalRef = this.modalService.show(updatetemplate);
      }
    });
  }

  unlockAllTimesheet(template: TemplateRef<any>) {
    // this.cancelRequest();
    let employeeObj = new Employee();

    employeeObj.unlockTimesheetFor = "All";
    employeeObj.updatedBy = this.currentUser.empId;
    employeeObj.isTimesheetLockCheckEnable = "false";

    this.employeeService.unlockAllTimesheet(employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onChangeManagerMapping(template: TemplateRef<any>) {
    this.cancelRequest();
    this.isDeletion = false;

    // if (!this.validationService.validateNullUndefinedEmptyString(this.employeeObj.newManagerId)) {
    //   this.alertMessage = "Please select a Manager !!"
    //   this.openAlertMod(template, this.alertMessage);
    //   return false;
    // }

    let employee: Employee = new Employee();
    employee.newManagerId = this.employeeObj.newManagerId;
    employee.managerId = this.employeeObj.managerId;

    console.log("changeManagerMapping : ", employee);

    this.employeeService.changeManagerMapping(employee).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {

        employee.empId = this.employeeObj.oldManagerId;
        console.log("deleteEmployee : ", employee);
        this.employeeService.deleteEmployee(employee).pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus == "Success") {
            this.openAlertMod(template, response.serviceResponse);
            this.showTable();
            this.page = 1;
          } else {
            this.openAlertMod(template, response.serviceResponse);
          }
        });
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  projectList: any[] = [];
  teamListt: any[] = [];
  apiResponse: any[] = [];
  showProjectDropdown: any;
  showTeamDropdown: any;
  showEmployeeRoleDropdown: any;
  getProjectsAccToDepartmentSelected(template: TemplateRef<any>) {
    this.showProjectDropdown = true;
    this.showTeamDropdown = false;
    this.showEmployeeRoleDropdown = false;
    const payload = {
      departmentId: this.employeeObj.departmentId,
      defaultProjectType: this.employeeObj.defaultprojectType
    };
    this.employeeService.getProjectsAccToDepartmentSelected(payload).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        this.projectList = response.serviceResponse;
        if (this.projectList.length === 0) {
          this.showProjectDropdown = false;
          this.openAlertMod(template, 'No project exists for the selected department. Please contact RMG to create one.');
        } else {
          this.showProjectDropdown = true;
        }
      } else {
        this.showProjectDropdown = false;
        alert(response.serviceResponse);
      }
    });
  }

  onProjectChange(event: any) {
    const selectedProjectId = +event.target.value;
    const selectedProject = this.projectList.find(p => p.projectId === selectedProjectId);
    this.showEmployeeRoleDropdown = false;

    if (selectedProject && selectedProject.teamList) {
      this.teamList = selectedProject.teamList;
      this.showTeamDropdown = true;
    } else {
      this.teamList = [];
      this.showTeamDropdown = false;
    }

    if (selectedProject && selectedProject.resourceRequirement && selectedProject.resourceRequirement.length > 0) {
      this.resourceRequirements = selectedProject.resourceRequirement;
      this.showResourceRequirementDropdown = true;
    } else {
      this.resourceRequirements = [];
      this.showResourceRequirementDropdown = false;
      this.employeeObj.selectedResourceOverviewId = null;
    }
  }


  showResourceRequirementDropdown: boolean = false;
  resourceRequirements: any[] = [];
  onTeamChange(event: any) {
    const selectedTeamId = +event.target.value;
    const selectedTeam = this.teamList.find(t => t.teamId === selectedTeamId);

    if (selectedTeam) {
      this.showEmployeeRoleDropdown = true;

      this.employeeObj.defaultTeamEmployeeRole = [];
    } else {
      this.showEmployeeRoleDropdown = false;
    }
  }





  // getAllEmployeeList() {
  //   this.allEmployeeList = [];
  //   this.employeeService.getAllEmployees().pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus == "Success") {
  //       this.allEmployeeList = response.serviceResponse;
  //       this.allEmployeeList.forEach(employeeObj => {
  //         employeeObj.employeementId = this.utilityService.appendEmployeementid(employeeObj.isConsultant, employeeObj.employeementId);
  //         employeeObj.dateOfJoining = (employeeObj.dateOfJoining) ? moment(employeeObj.dateOfJoining).format(AppComponent.DATE_FORMAT) : null;
  //         employeeObj.dateOfRelieving = (employeeObj.dateOfRelieving) ? moment(employeeObj.dateOfRelieving).format(AppComponent.DATE_FORMAT) : null;
  //         employeeObj.updatedOn = (employeeObj.updatedOn) ? moment(employeeObj.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
  //         employeeObj.createdOn = (employeeObj.createdOn) ? moment(employeeObj.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
  //         if (employeeObj.isConsultant == 'true')
  //           employeeObj.employeeType = 'Consultant';
  //         else if (employeeObj.isApprenticeship == 'true')
  //           employeeObj.employeeType = 'Apprentice';
  //         else
  //           employeeObj.employeeType = 'Regular';
  //       });
  //       this._allEmployeeList = this.allEmployeeList;
  //       this.changeEvent("Active");
  //       this.onselectYes = false;
  //       // sessionStorage.setItem('AllEmployees', JSON.stringify(this.allEmployeeList));
  //     } else {
  //       alert(response.serviceResponse);
  //     }
  //   });
  // }

  getAllEmployeeList() {
    this.allEmployeeList = [];
    this.employeeService.getAllEmployees().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allEmployeeList = response.serviceResponse;
        this.allEmployeeList.forEach(employeeObj => {
          const skillNames = employeeObj.employeeSkills
            ?.map((skill: any) => skill.skillName || skill.additionalSkill || 'NA')
            .filter((name: string) => name.trim() !== 'NA')
            .join(', ') || 'NA';
          const certificateNames = employeeObj.employeeCertificates
            ?.map((cert: any) => cert.certificationName || 'NA')
            .filter((name: string) => name.trim() !== 'NA')
            .join(', ') || 'NA';
          employeeObj.skillNames = skillNames;
          employeeObj.certificateNames = certificateNames;

          employeeObj.employeementId = this.utilityService.appendEmployeementid(employeeObj.isConsultant, employeeObj.employeementId);
          employeeObj.dateOfJoining = (employeeObj.dateOfJoining) ? moment(employeeObj.dateOfJoining).format(AppComponent.DATE_FORMAT) : null;
          employeeObj.dateOfRelieving = (employeeObj.dateOfRelieving) ? moment(employeeObj.dateOfRelieving).format(AppComponent.DATE_FORMAT) : null;
          employeeObj.updatedOn = (employeeObj.updatedOn) ? moment(employeeObj.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
          employeeObj.createdOn = (employeeObj.createdOn) ? moment(employeeObj.createdOn).format(AppComponent.DATETIME_FORMAT) : null;

          if (employeeObj.isConsultant == 'true')
            employeeObj.employeeType = 'Consultant';
          else if (employeeObj.isApprenticeship == 'true')
            employeeObj.employeeType = 'Apprentice';
          else if (employeeObj.isApmosysProduct == 'true')
            employeeObj.employeeType = 'Apmosys Product';

          else
            employeeObj.employeeType = 'On roll';

          // Calculate days_left_for_full_time
          this.calculateDaysLeftForFullTime(employeeObj);
        });
        this._allEmployeeList = this.allEmployeeList;
        this.changeEvent("Active");
        this.onselectYes = false;
      } else {
        alert(response.serviceResponse);
      }
    });
  }

  calculateDaysLeftForFullTime(employeeObj: any) {




    if (!employeeObj.dateOfJoining || !employeeObj.probationPeriod && employeeObj.employmentstatus !== 'Inactive') {
      employeeObj.days_left_for_full_time = 'N/A';
      employeeObj.days_left_status = 'missing_data';
      return;
    }

    try {

      const joiningDate = moment(employeeObj.dateOfJoining, AppComponent.DATE_FORMAT);
      const today = moment();

      const daysPassed = today.diff(joiningDate, 'days');

      const daysLeft = employeeObj.probationPeriod - daysPassed;
      if (employeeObj.employmentstatus && employeeObj.employmentstatus.toLowerCase() === 'probation' && employeeObj.employmentstatus !== 'Inactive') {
        employeeObj.days_left_for_full_time = daysLeft;
      }
      else if (employeeObj.employmentstatus && employeeObj.employmentstatus.toLowerCase() === 'confirmed') {
        employeeObj.days_left_for_full_time = 'N/A';
      }
      else {
        employeeObj.days_left_for_full_time = 'N/A';
      }


    } catch (error) {
      console.error('Error calculating days left for full time:', error);
      employeeObj.days_left_for_full_time = 'Error';
      employeeObj.days_left_status = 'error';
    }
  }


  resetExtensionForm() {
    this.extensionData = {
      period: null,
      reason: '',
      customReason: ''
    };
  }
  changeEvent(value: string) {
    if (value == "Active") {
      this.allEmployeeList = this._allEmployeeList.filter(x => x.employmentstatus != 'InActive');
      this.dateOfReleivingshow = false;
      this.managerFlag = false;
      this.isActiveTable = true
    } else if (value == "InActive") {
      this.allEmployeeList = this._allEmployeeList.filter(x => x.employmentstatus == 'InActive');
      this.dateOfReleivingshow = true;
      this.managerFlag = true;
      this.isActiveTable = false;
    }
    this.page = 1;
  }
  createEmployeeList(allEmployeeList: any) {
    this.managerList = allEmployeeList.map(employee => {
      let emp = { name: employee.name, empId: employee.empId.toString() };
      return emp;
    });
    console.log("managerList : ", this.managerList);
  }

  name = 'EmployeeSheet.xlsx';
  exportToExcel(): void {
    const onlySpecificDataArr = this.allEmployeeList.map(
      x => ({
        "EmployeeId": x.employmentIdAcToET,
        // "EmployeeId":(x.isConsultant === 'true' ? 'A-CS-' : 'A-') + x.employeementId,
        "Employee Type":
          x.isApmosysProduct === 'true'
            ? 'Apmosys Product'
            : x.isApprenticeship === 'true'
              ? 'Apprentice'
              : x.isConsultant === 'true'
                ? 'Consultant'
                : 'On roll',
        "Full Name": x.name,
        "EmailId": x.email,
        "Employment Status": x.employmentstatus,
        // "Date of Joining": (x.dateOfJoining) ? moment(x.dateOfJoining).format(AppComponent.DATE_FORMAT) : null,
        // "Date of Confirmation": (x.employeeConfirmationDate) ? moment(x.employeeConfirmationDate).format(AppComponent.DATE_FORMAT) : null,
        // "Date of Relieving": (x.dateOfRelieving) ? moment(x.dateOfRelieving).format(AppComponent.DATE_FORMAT) : null,
        "Date of Joining": (x.dateOfJoining)
          ? moment(x.dateOfJoining, "DD-MM-YYYY").format(AppComponent.DATE_FORMAT)
          : null,

        "Date of Confirmation": (x.employeeConfirmationDate)
          ? moment(x.employeeConfirmationDate, "YYYY-MM-DD").format(AppComponent.DATE_FORMAT)
          : null,

        "Date of Relieving": (x.dateOfRelieving)
          ? moment(x.dateOfRelieving, "DD-MM-YYYY").format(AppComponent.DATE_FORMAT)
          : null,
        "Employee Release Status": x.employmentReleaseStatus,
        "Department Name": x.departmentName,
        "Aadhar": x.aadhar,
        "About Me": x.aboutMe,
        "Address": x.address,
        "Permanent Address": x.permanentAddress,
        "City": x.city,
        "Blood Group": x.bloodGroup,
        "Date Of Birth": (x.dateOfBirth) ? moment(x.dateOfBirth).format(AppComponent.DATE_FORMAT) : null,
        "Gender": x.gender,
        "Father Name": x.fatherName,
        "Mobile No": x.mobileNo,
        "Pan Number": x.panNumber,
        "Place Of Birth": x.placeOfBirth,
        "Work Location": x.workLocation,
        "Probation Period": x.probationPeriod,
        "Notice Period": x.noticePeriod,
        "Country": x.country,
        "Emergency Contact Mobile": x.emergencyContactMobile,
        "Emergency Contact Person": x.emergencyContactPerson,
        "Landline": x.landline,
        "Marital Status": x.maritalStatus,
        "Mother Tongue": x.motherTongue,
        "Alternate Mobile No": x.alternateMobileNo,
        "Pincode": x.pincode,
        "Relation": x.relation,
        "State": x.state,
        "Views On Organisation": x.viewsOnOrganisation,
        "Passport Number": x.passportNumber,
        "Bank Account No": x.bankAccountNo,
        "Bank IFSC Code": x.bankIFSCCode,
        "Bank Name": x.bankName,
        "PF Account Number": x.pfAccountNumber,
        "Previous PF AccountNumber": x.previousPfAccountNumber,
        "UAN": x.uan,
        "ESIC Number": x.esicNumber,
        "Graduation Type": x.graduationType,
        "Pursuing": x.pursuing,
        "Passing Grade": x.passingGrade,
        "Year Of Passing": x.yearOfPassing,
        "Created By": x.createdBy,
        "Created On": (x.createdOn) ? moment(x.createdOn).format(AppComponent.DATETIME_FORMAT) : null,
        "Manager Name": x.managerName,
        "Job Role": x.jobRoleName,
        "Designation Name": x.designationName,
        "Skills": x.skillNames,
        "Certifications": x.certificateNames,

      })
    )
    this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.name)
  }
  // exportToExcel(id:any): void {
  //   const tableId = id; // Replace with your actual table ID
  //   this.excelName = "EmployeeSheet.xlsx";
  //   this.tableName= "Employee Info";

  //   this.exportExcelService.exportTableFormat(tableId,this.excelName,this.tableName);
  // }

  getManagerList(employee?: Employee) {
    this.managerList = [];
    let employeeList = [];

    console.log("Skip manager : ", employee)
    this.employeeObj.role = "Manager";
    let employeeObjManager: Partial<Employee> = {
      role: "Manager"
    }

    // if(this.employeeObj.isConsultant == 'true'){
    //   this.employeeObj.employeementId = this.employeeObj.employeementId?.substring(5);
    // }else{
    //   this.employeeObj.employeementId = this.employeeObj.employeementId?.substring(2);
    // }
    this.employeeObj.employeementId = this.employeeObj.employeementId?.substring(2)
    this.employeeService.getAllEmployeesByRoleForManager(employeeObjManager).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        employeeList = response.serviceResponse;

        console.log("employeeList By Role : ", employeeList)
        if (this.isUpdation || this.isDeletion) {
          this.managerList = employeeList.filter((manager: Employee) => manager.empId !== employee.empId);
        } else {
          this.managerList = employeeList;
        }
        console.log("managerList : ", this.managerList)
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  //   onManagerChange(employeeObj: any) {

  //     this.onChangeManagerSelection(employeeObj);

  //     this.onUpdateTypeChange(employeeObj);

  // }



  //   onChangeManagerSelection(employeeObj: any) {

  //     // Handle logic when "Do you want to change manager?" field changes

  //     if (employeeObj.changeManager === 'no') {

  //         employeeObj.employmentReleaseStatus = '';

  //         employeeObj.updateType = '';

  //         employeeObj.newManagerId = '';

  //     }

  // }



  // onUpdateTypeChange(employeeObj: any) {

  //   // const selectElement = event.target as HTMLSelectElement;

  //   //   const status = selectElement.value;

  //   //   if (this.employeeObj.updateType === 'manual') {

  //   //     this.openModalForManagerChange(this.changeManagerTemplate, event, this.employeeObj.empId);

  //   // }

  //     if (this.employeeObj.updateType === 'manual') {

  //         this.openModalForManagerChange(this.changeManagerTemplate, event, this.employeeObj.empId);

  //     }

  // }

  // //added by priyadarshini

  // onMappingUpdateTypeChange(employeeObj: any) {

  //   // Handle logic when "Mapping Update Type" field changes

  //   if (employeeObj.updateType !== 'automatic') {

  //       employeeObj.newManagerId = '';

  //   }

  // }

  onManagerChange(employeeObj: any) {

    this.onChangeManagerSelection(employeeObj);

    this.onUpdateTypeChange(employeeObj);

  }



  onChangeManagerSelection(employeeObj: any) {

    // Handle logic when "Do you want to change manager?" field changes

    if (employeeObj.changeManager === 'no') {

      employeeObj.employmentReleaseStatus = '';

      employeeObj.updateType = '';

      employeeObj.newManagerId = '';

    }

  }



  onUpdateTypeChange(employeeObj: any) {

    // const selectElement = event.target as HTMLSelectElement;

    //   const status = selectElement.value;

    //   if (this.employeeObj.updateType === 'manual') {

    //     this.openModalForManagerChange(this.changeManagerTemplate, event, this.employeeObj.empId);

    // }

    if (this.employeeObj.updateType === 'manual') {

      this.openModalForManagerChange(this.changeManagerTemplate, event, this.employeeObj.empId);

    }

  }



  onMappingUpdateTypeChange(employeeObj: any) {

    // Handle logic when "Mapping Update Type" field changes

    if (employeeObj.updateType !== 'automatic') {

      employeeObj.newManagerId = '';

    }

  }



  /* Employee Draft */
  onSaveDraftEmployee(template: TemplateRef<any>) {
    const dateFormat = 'YYYY-MM-DD';
    let inputValidated: boolean = this.validateEmployeeDraftObj(this.employeeObj, template)
    if (!inputValidated) return;

    this.employeeObj.isDraft = true;
    // // transform date formats to dd-MM-yyyy
    this.employeeObj.dateOfBirth = moment(this.employeeObj.dateOfBirth).format(dateFormat);
    this.employeeObj.dateOfJoining = moment(this.employeeObj.dateOfJoining).format(dateFormat);

    this.employeeObj.certifications = (Object.keys(this.allCertificationList[0]).length === 0) ? null : this.allCertificationList;
    this.employeeObj.previousEmploymentList = (Object.keys(this.allPreviousEmployment[0]).length === 0) ? null : this.allPreviousEmployment;
    this.employeeObj.createdBy = this.currentUser.empId;
    console.log("Create Employe Draft : ", this.employeeObj);
    this.employeeService.createDraftEmployee(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showDraftTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onUpdateDraftEmployee(template: TemplateRef<any>) {
    const dateFormat = 'YYYY-MM-DD';
    this.employeeObj.isDraft = true;
    // // transform date formats to dd-MM-yyyy
    this.employeeObj.dateOfBirth = moment(this.employeeObj.dateOfBirth).format(dateFormat);
    this.employeeObj.dateOfJoining = moment(this.employeeObj.dateOfJoining).format(dateFormat);

    this.employeeObj.certifications = (Object.keys(this.allCertificationList[0]).length === 0) ? null : this.allCertificationList;
    this.employeeObj.previousEmploymentList = (Object.keys(this.allPreviousEmployment[0]).length === 0) ? null : this.allPreviousEmployment;
    this.employeeObj.updatedBy = this.currentUser.empId;
    console.log("Update Employe Draft : ", this.employeeObj);
    this.employeeService.updateDraftEmployee(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showDraftTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onDeleteDraftEmployee(template: TemplateRef<any>) {
    this.cancelRequest();

    this.employeeObj.draftEmpId = this.employeeObj.empId;
    this.employeeService.deleteDraftEmployee(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showDraftTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  getAllDraftEmployees() {
    this.allEmployeeList = [];
    let employeeObj = new Employee();
    employeeObj.updateApplicationStatus = 'Pending For Approval';
    this.employeeService.getAllDraftEmployees(employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allEmployeeList = response.serviceResponse;
        for (let x of this.allEmployeeList) {
          x.employeementId = "A-".concat(x.employeementId)
          // x.employeementId = (x.isConsultant === 'true' ? 'A-CS-' : 'A-') + x.employeementId;
          x.dateOfJoining = (x.dateOfJoining) ? moment(x.dateOfJoining).format(AppComponent.DATE_FORMAT) : null;
          x.dateOfRelieving = (x.dateOfRelieving) ? moment(x.dateOfRelieving).format(AppComponent.DATE_FORMAT) : null;
          if (x.isConsultant == 'true')
            x.employeeType = 'Consultant';
          else if (x.isApprenticeship == 'true')
            x.employeeType = 'Apprentice';
          else if (x.isApmosysProduct == 'true')
            x.employeeType = 'Apmosys Product';
          else
            x.employeeType = 'On roll';
        }
        this.allEmployeeList.forEach(draftemp => {
          draftemp.emp360 = draftemp.empId;
          draftemp.emp360Mng = draftemp.managerId;


        });
        console.log("allDraftEmployeeList : ", this.allEmployeeList)
      } else {
        alert(response.serviceResponse)
      }
    });
  }

  // Job Role
  getAllJobRoleList() {
    this.allJobRoleList = [];

    this.jobRoleService.getAllJobRole().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allJobRoleList = response.serviceResponse;
        console.log("allJobRoleList : ", this.allJobRoleList);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getDesignationByDeptId(departmentId: any) {
    let designationObj = new Designation();
    designationObj.deptId = departmentId;

    this.destinationService.getDesignationByDeptId(designationObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        console.log(response.serviceResponse)
        this.allDesignationList = response.serviceResponse;
        console.log(this.allDesignationList, " : allDesignationList");
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getAllDepartmentList() {
    this.allDeptList = [];

    this.departmentService.getAllDepartments().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDeptList = response.serviceResponse;
        console.log("allDeptList : ", this.allDeptList);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  deptSelected: any;
  getJobRolesByDept(departmentId: any, jobRoleId?: any) {
    this.deptSelected = true;
    this.filteredJobRoleList = [];
    this.filteredJobRoleList = this.allJobRoleList.filter(jobRole => jobRole.departmentId == departmentId);
    jobRoleId ? this.employeeObj.jobRoleId = jobRoleId : this.employeeObj.jobRoleId = '';
  }


  rejectDraftEmployeeApplication(template: TemplateRef<any>) {
    if (!this.validationService.validateNullUndefinedEmptyString(this.employeeObj.remarks)) {
      this.alertMessage = "Please enter Reason for Rejecting !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } if (!this.validationService.validateTeamName(this.employeeObj.remarks.trim())) {
      this.alertMessage = "Enter Valid Reason for Rejecting !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    this.cancelRequest();
    this.cancelApplication();
    this.employeeObj.updateApplicationStatus = 'Rejected';
    this.employeeObj.updatedBy = this.currentUser.empId;
    if (this.employeeObj.documentList) {
      this.employeeObj.documentList.forEach((doc: Document) => doc.documentBytes = null);
    }
    this.employeeObj.remarks = this.employeeObj.remarks?.trim();
    console.log(" reject KYC :  ", this.employeeObj)
    this.employeeService.rejectDraftEmployeeApplication(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allEmployeeList = response.serviceResponse;
        this.openAlertMod(template, response.serviceResponse);
        this.showDraftTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  // Method to convert file to Base64
  // convertFileToBase64(file: File): Promise<string> {
  //   return new Promise((resolve, reject) => {
  //     const reader = new FileReader();
  //     reader.readAsDataURL(file);
  //     reader.onload = () => resolve(reader.result as string);
  //     reader.onerror = error => reject(error);
  //   });
  // }

  // public getDocumentType(document: any): string {
  //   return document.documentType;
  // }


  // async handleFileInput(event: any) {
  //   const files: FileList = event.target.files;

  //   this.employeeObj.documentList = []; // Reset document list if needed

  //   for (let i = 0; i < files.length; i++) {
  //     const file = files[i];
  //     const base64String = await this.convertFileToBase64(file);

  //     // Assuming documentList is an array of document objects
  //     this.employeeObj.documentList.push({
  //       documentName: file.name,
  //       documentBytes: base64String.split(',')[1], // Extract Base64 data without prefix
  //       documentType: this.getDocumentType(file.name), // Get document type based on your logic
  //       isDraft: 'true', // or any value you use
  //     });
  //   }
  // }

  // approveDraftEmployeeApplication(template: TemplateRef<any>) {
  //   this.cancelRequest();
  //   this.cancelApplication();
  //   this.employeeObj.updateApplicationStatus = 'Approved';
  //   this.employeeObj.updatedBy = this.currentUser.empId;

  //   this.employeeService.approveDraftEmployeeApplication(this.employeeObj)
  //     .pipe(first())
  //     .subscribe((response: any) => {
  //       if (response.serviceStatus === "Success") {
  //         this.allEmployeeList = response.serviceResponse;
  //         this.openAlertMod(template, response.serviceResponse);
  //         this.showDraftTable();
  //       } else {
  //         this.openAlertMod(template, response.serviceResponse);
  //       }
  //     });
  // }

  approveDraftEmployeeApplication(template: TemplateRef<any>) {
    this.cancelRequest();
    this.cancelApplication();
    this.employeeObj.updateApplicationStatus = 'Approved';
    this.employeeObj.updatedBy = this.currentUser.empId;
    if (this.employeeObj.documentList) {
      this.employeeObj.documentList.forEach((doc: Document) => doc.documentBytes = null);
    }
    this.employeeService.approveDraftEmployeeApplication(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allEmployeeList = response.serviceResponse;
        this.openAlertMod(template, response.serviceResponse);
        this.showDraftTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  // Employee Info Preview
  async openEmployeeInfoPreview(template: TemplateRef<any>, employeeObj: Employee) {

    console.log("employeeObj : ", employeeObj);

    let currentEmp = new Employee();
    // if (employeeObj.isConsultant == "true"){
    //   currentEmp.employeementId = employeeObj.employeementId.substring(5);
    // }else{
    //   currentEmp.employeementId = employeeObj.employeementId.substring(2);
    // }
    currentEmp.employeementId = employeeObj.employeementId.substring(2);

    console.log("employment id : ", currentEmp);

    currentEmp.empId = employeeObj.empId;
    currentEmp.isDraft = true;

    console.log("employeeObj.isConsultant", employeeObj.isConsultant);

    currentEmp.isConsultant = employeeObj.isConsultant;

    const infoResponse: any = await this.employeeService.getDraftEmployeeByEmpId(currentEmp).toPromise();
    if (infoResponse.serviceStatus == "Success") {
      this.previewObj = infoResponse.serviceResponse;
      this.previewObj.isConsultant = employeeObj.isConsultant;
      this.previewObj.isApprenticeship = employeeObj.isApprenticeship;
      console.log("this.previewObj : ", this.previewObj);
    } else {
      console.error(infoResponse.serviceResponse)
    }

    const docResponse: any = await this.imageService.getEmployeeDocuments(currentEmp).toPromise();
    if (docResponse.serviceStatus == 'Success') {
      this.previewObj.documentList = docResponse.serviceResponse;
      console.log("this.previewObj.documentList : ", this.previewObj.documentList);
    } else {
      console.log(docResponse.serviceResponse);
    }
    this.previewModalRef = this.modalService.show(template, { class: 'modal-xl' });
    setTimeout(() => {
      this.previewObj.documentList.forEach((doc, index) => {
        if (doc.documentBytes) {
          let preview = document.getElementById(`docPreview${index + 1}`);
          let objectURL = 'data:image/*;base64,' + doc.documentBytes;
          let src: string = this.sanitizer.sanitize(SecurityContext.RESOURCE_URL, this.sanitizer.bypassSecurityTrustResourceUrl(objectURL));
          preview.setAttribute('src', src);
        }
      });
    }, 500)
  }

  // Employee Info preview View all tab
  async openViewEmployeeInfoPreview(template: TemplateRef<any>, employeeObj: Employee) {

    console.log("employeeObj : ", employeeObj);
    this.domainSpecializationList = [];

    let currentEmp = new Employee();
    // if (employeeObj.isConsultant == 'true') {
    //   currentEmp.employeementId = currentEmp.employeementId?.substring(5);
    // }else{
    // currentEmp.employeementId = currentEmp.employeementId?.substring(2);
    // }
    currentEmp.employeementId = currentEmp.employeementId?.substring(2);
    currentEmp.empId = employeeObj.empId;
    currentEmp.isDraft = false;


    const infoResponse: any = await this.employeeService.getEmployeeByEmpId(currentEmp).toPromise();
    if (infoResponse.serviceStatus == "Success") {
      this.previewEmployeeObj = infoResponse.serviceResponse;

      console.log("this.previewObj : ", this.previewEmployeeObj);
    } else {
      console.error(infoResponse.serviceResponse)
    }

    const docResponse: any = await this.imageService.getEmployeeDocuments(currentEmp).toPromise();
    if (docResponse.serviceStatus == 'Success') {
      this.previewEmployeeObj.documentList = docResponse.serviceResponse;
      console.log("this.previewObj.documentList : ", this.previewEmployeeObj.documentList);
    } else {
      console.log(docResponse.serviceResponse);
    }

    let domainObj = new Domain();
    domainObj.empId = employeeObj.empId;
    const domainResponse: any = await this.domainService.getDomainSpecializationByEmpId(domainObj).toPromise();
    if (domainResponse.serviceStatus == "Success") {
      this.domainSpecializationList = domainResponse.serviceResponse;
      console.log(this.domainSpecializationList, " : this.domainSpecializationList");
    } else {
      console.error(domainResponse.serviceResponse);
    }

    this.previewModalRef = this.modalService.show(template, { class: 'modal-xl' });
    setTimeout(() => {
      this.previewEmployeeObj.documentList && this.previewEmployeeObj.documentList.forEach((doc, index) => {
        if (doc.documentBytes) {
          let preview = document.getElementById(`docPreview${index + 1}`);
          let objectURL = 'data:image/*;base64,' + doc.documentBytes;
          let src: string = this.sanitizer.sanitize(SecurityContext.RESOURCE_URL, this.sanitizer.bypassSecurityTrustResourceUrl(objectURL));
          preview.setAttribute('src', src);
        }
      });
    }, 500)
  }

  openModalForManagerChange(template: TemplateRef<any>, event: Event, employeeId: string) {
    this.filters = {};
    const eventValue = (event.target as HTMLSelectElement).value;
    // Open modal only if the updateType is 'manual'
    if (this.employeeObj.updateType === 'manual') {
      this.employeeService.getTotalNoOfreporties(employeeId).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus === 'Success') {
          this.listOfReporties = response.serviceResponse;
        }
      });
      this.isSearchEnabled = false;
      this.modalRef = this.modalService.show(template, { class: 'modal-sm', backdrop: 'static', keyboard: false });
    }
  }


  // openModalForManagerChange(updateType,template: TemplateRef<any>, event,employeeId) {
  //   updateType.newManagerId = "";
  //   // this.employeeObj.newManagerId ='';
  //   this.filters = {};
  //   var eventValue = event.target.value;
  //   // if(eventValue == 'InActive'){
  //   if(eventValue == 'manual'){
  //     var id = employeeId

  //     this.employeeService.getTotalNoOfreporties(id).pipe(first()).subscribe((response : any)=>{
  //       if(response.serviceStatus == 'Success'){
  //         this.listOfReporties = response.serviceResponse;
  //       }
  //     })
  //     var empId = employeeId;
  //     //console.log(" empId    ",empId);
  //     this.isSearchEnabled = false;
  //     //console.log("Log    eventValue    ",eventValue);

  //     this.modalRef = this.modalService.show(template, { class: 'modal-sm' ,  backdrop: 'static', keyboard: false });
  //   }
  // }

  // openModalForManagerChange(template: TemplateRef<any>, event,employeeId) {

  //   this.filters = {};
  //   var eventValue = event.target.value;
  //   if(eventValue == 'InActive'){
  //     var id = employeeId

  //     this.employeeService.getTotalNoOfreporties(id).pipe(first()).subscribe((response : any)=>{
  //       if(response.serviceStatus == 'Success'){
  //         this.listOfReporties = response.serviceResponse;
  //       }
  //     })
  //     var empId = employeeId;
  //     console.log(" empId    ",empId);
  //     this.isSearchEnabled = false;
  //     //console.log("Log    eventValue    ",eventValue);

  //     this.modalRef = this.modalService.show(template, { class: 'modal-sm' ,  backdrop: 'static', keyboard: false });
  //   }
  // }





  RestrictFullName(event) {
    var k;
    k = event.charCode;
    if ((k == 33) || (k == 34) || (k == 35) || (k == 36) || (k == 37) ||
      (k == 38) || (k == 39) || (k == 40) || (k == 41) || (k == 42) ||
      (k == 43) || (k == 44) || (k == 45) || (k == 46) || (k == 47) || (k == 48) ||
      (k == 49) || (k == 50) || (k == 51) || (k == 52) || (k == 53) || (k == 54) || (k == 55) ||
      (k == 56) || (k == 57) || (k == 58) ||
      (k == 59) || (k == 60) || (k == 61) || (k == 62) || (k == 63) ||
      (k == 64) || (k == 91) || (k == 92) || (k == 93) || (k == 94) ||
      (k == 95) || (k == 96) || (k == 123) ||
      (k == 124) || (k == 125) || (k == 126) || (k == 127)) {
      return (false);
    }
    return (true);


  }

  fieldRestictCharacterForNumber(event) {
    var k;
    k = event.charCode;
    if ((k == 32) || (k == 33) || (k == 34) || (k == 35) || (k == 36) || (k == 37) || (k == 38) || (k == 39) || (k == 40) || (k == 41) || (k == 42) || (k == 43) ||
      (k == 44) || (k == 45) || (k == 46) || (k == 47) || (k == 97) || (k == 98) || (k == 99) ||
      (k == 99) || (k == 100) || (k == 101) || (k == 102) || (k == 103) ||
      (k == 104) || (k == 105) || (k == 106) || (k == 107) || (k == 108) ||
      (k == 109) || (k == 110) || (k == 111) || (k == 112) || (k == 113) ||
      (k == 114) || (k == 115) || (k == 116) || (k == 117) || (k == 118) ||
      (k == 119) || (k == 120) || (k == 121) || (k == 122) || (k == 46) || (k == 65) || (k == 66) || (k == 67) || (k == 68) || (k == 69) ||
      (k == 70) || (k == 71) || (k == 72) || (k == 73) || (k == 74) ||
      (k == 75) || (k == 76) || (k == 77) || (k == 78) || (k == 79) ||
      (k == 80) || (k == 81) || (k == 82) || (k == 83) || (k == 84) ||
      (k == 85) || (k == 86) || (k == 87) || (k == 88) || (k == 89) ||
      (k == 90)) {
      return (false);
    }
    return true;
  }


  getAllPortalConfigData() {
    this.portalService.getPortalConfig().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.portalConfigList = response.serviceResponse;
        for (let portal of this.portalConfigList) {
          if (portal.configName == 'Probation Period') {
            this.employeeObj.probationPeriod = portal.configPeriod;
          }
          if (portal.configName == 'Notice Period') {
            this.employeeObj.noticePeriod = portal.configPeriod;
          }
        }
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  getProjectsByDepartment(employeeObj, template: TemplateRef<any>) {
    // var newDept = employeeObj.departmentName?.trim();
    this.employeeObj.projectName = "";
    let empObj = new Employee();

    empObj.departmentName = employeeObj.departmentId;
    empObj.managerId = employeeObj.empId;

    console.log("empObj   ", empObj);

    this.employeeService.getProjectsByDepartmentName(empObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {

        this.listOfProjectsByDeptId = response.serviceResponse;
        console.log(" success ", this.listOfProjectsByDeptId);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  // added by anurag for manager changes incase of inactive

  openManagerDetailsModal(template: TemplateRef<any>, employeeId) {
    this.filters = {};
    this.listOfDepartment = [];
    this.isSearchEnabled = false;
    this.employeeObj.departmentId = "";
    this.employeeObj.projectName = "";
    this.employeeObj.teamName = "";
    console.log(" empId in manager UI change ", this.employeeObj.name);

    this.getReporteesListByManagerId();
    this.getReporteesListByReportingManagerId();

    this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
  }

  getReporteesListByManagerId() {
    console.log(" empId in manager UI change ", this.employeeObj.name);

    if (this.employeeObj.employeementId.startsWith("A-")) {
      this.employeeObj.employeementId = this.employeeObj.employeementId.substring(2);
    }

    console.log("employment id", this.employeeObj.employeementId);
    this.employeeService.getReporteesListByManagerId(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.reporteeList = response.serviceResponse;
        this.getManagersList();
        // this.employeeObj.managerId = '';
        //console.log(" teamMember list   ",this.TeamMemberList)
      }
    })
  }

  updateNoticePeriod(employeeObj: Employee) {
    const dateFormat = 'YYYY-MM-DD';
    console.log(employeeObj, "employeeObj");
    this.employeeObj.dateOfRelieving = moment(this.employeeObj.dateOfRelieving).format(dateFormat);
    console.log(this.employeeObj.dateOfRelieving);
    const diff = Math.abs(Math.floor((new Date(this.employeeObj.dateOfRelieving).getTime() - new Date(employeeObj.dateOfResign).getTime()) / (1000 * 60 * 60 * 24)));
    this.employeeObj.noticePeriod = diff;
    console.log(diff, "diffDaysdiffDays")
  }

  estimateDateOfReleiving(employeeObj: Employee) {
    const dateFormat = 'YYYY-MM-DD';

    if (this.employeeObj.dateOfResign) {
      let estimateDateOfRelieving = new Date(this.employeeObj.dateOfResign);
      this.employeeObj.dateOfRelieving = moment(estimateDateOfRelieving).add(this.employeeObj.noticePeriod, "days").format(dateFormat);
      console.log(this.employeeObj.dateOfRelieving, "this.employeeObj.dateOfRelieving")
    }
    if(!this.statusFlag){
      this.actualNoticePeriod=this.employeeObj.noticePeriod
    }
    if(['Terminated','Absconded'].includes(this.employeeObj.employmentReleaseStatus)){
      this.statusFlag=true;
      this.employeeObj.dateOfRelieving = moment(this.employeeObj.dateOfResign).format(dateFormat);
        if (this.employeeObj.dateOfResign && this.employeeObj.dateOfRelieving) {
            this.employeeObj.noticePeriod = moment(this.employeeObj.dateOfRelieving)
                .diff(moment(this.employeeObj.dateOfResign), 'days');
           }
    }
  }

  onUpdateTimesheetLockCheck(template: TemplateRef<any>, employeeObj: Employee, status: any) {
    let employee = Object.assign({}, employeeObj);
    employee.isTimesheetLockCheckEnable = status;
    employee.updatedBy = this.currentUser.empId;

    // if(employee.employeementId.startsWith('A-CS-')){
    //   employee.employeementId  = employee.employeementId.substring(5);
    // }else  if(employee.employeementId.startsWith('A-')){
    //   employee.employeementId  = employee.employeementId.substring(2);
    // }else {
    //   employee.employeementId  = employee.employeementId
    // }

    if (employee.employeementId.startsWith('A-')) {
      employee.employeementId = employee.employeementId.substring(2);
    } else {
      employee.employeementId = employee.employeementId
    }

    console.log("updateTimesheetLockCheck : ", employee);
    this.employeeService.updateTimesheetLockCheck(employee).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.getAllEmployeeList();
        this.openAlertMod(template, response.serviceResponse);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  // Employee Audit :: start

  resetAuditSearchFilter(event, title?: any) {
    this.isAuditSearchEnabled = false;
    this.auditFilter = {};
    this.filterOnhistory = {};

    if (title == 'Full Journey') {
      this.isFullJourneyAccordianBody = true;
    } else {
      this.isFullJourneyAccordianBody = false;
    }
    if (title == 'Life Cycle Change') {
      this.isLifeCycleAccordianBody = true;
    } else {
      this.isLifeCycleAccordianBody = false;
    }
    if (title == 'Kyc Update') {
      this.isKycUpdateAccordianBody = true;
    } else {
      this.isKycUpdateAccordianBody = false;
    }
    if (title == 'Employee Info Change') {
      this.isEmployeeInfoAccordianBody = true;
    } else {
      this.isEmployeeInfoAccordianBody = false;
    }
    if (title == 'Team/Project Change') {
      this.isTeamProjectAccordianBody = true;

    } else {
      this.isTeamProjectAccordianBody = false;
    }
    if (title == 'Project History') {
      this.isEmployeeHistory = true;
      this.employeehistory();
    } else {
      this.isEmployeeHistory = false;
    }

  }



  employeehistory() {
    this.employeeWorkingHistory = [];
    let empObj = new Employee();
    empObj.empId = this.imployeeID;
    this.employeeService.findEmployeeWorkingHistory(empObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.employeeWorkingHistory = response.serviceResponse;
        console.log('emoloyee hist-----', this.employeeWorkingHistory);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }


  getEmployeeAuditInfo(employee: any, auditTemplate: TemplateRef<any>, template: TemplateRef<any>) {

    this.filteredEmployeeAuditHistory = [];
    this.employeeAuditHistory = [];
    this.filters = {};
    this.filterOnhistory = {};
    this.isEmployeeInfoAccordianBody = false;
    this.isTeamProjectAccordianBody = false;
    this.isKycUpdateAccordianBody = false;
    this.isLifeCycleAccordianBody = false;
    this.isFullJourneyAccordianBody = false;
    this.isEmployeeHistory = false

    let employeeObj = new Employee();
    employeeObj.empId = employee.empId;
    this.imployeeID = employee.empId;

    this.employeeService.getEmployeeAuditInfo(employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.employeeAuditHistory = response.serviceResponse;

        Object.keys(this.employeeAuditHistory).forEach(key => {
          let obj = this.employeeAuditHistory[key];
          Object.keys(obj).forEach(innerKey => {
            if (obj[innerKey] !== null) {
              let newObj = {
                "date": key,
                "field": innerKey,
                "value": obj[innerKey],
                "bucketName": obj["bucketName"],
                "updatedByName": obj["updatedByName"] ? obj["updatedByName"] : null,
                "createdByName": obj["createdByName"] ? obj["createdByName"] : null,
                "color": '#FFFFFF',
                "updatedBy": obj["updatedBy"] ? obj["updatedBy"] : null
              };

              if (newObj.field !== 'bucketName' && newObj.field !== 'designationId' && newObj.field !== 'jobRoleId'
                && newObj.field !== 'createdBy' && newObj.field !== 'departmentId'
                && newObj.field !== 'updatedByName' && newObj.field !== 'createdOn' && newObj.field !== 'createdByName'
                && newObj.field !== 'updatedOn' && newObj.field !== 'empId' && newObj.field !== 'teamId' && newObj.field !== 'employeeTeamMapId'
                && newObj.field !== 'teamLeadId' && newObj.field !== 'reportingManagerId' && newObj.field !== 'managerId') {

                if (newObj.field == 'employmentstatus') {
                  newObj.bucketName = 'Lifecycle Changes';
                }

                if (newObj.bucketName == 'Employment Info Changes') {
                  newObj.color = '#9FE2BF';
                } else if (newObj.bucketName == 'Lifecycle Changes') {
                  newObj.color = '#40E0D0';
                } else if (newObj.bucketName == 'KYC Update') {
                  newObj.color = '#CCCCFF';
                } else if (newObj.bucketName == 'Team/Project Changes') {
                  newObj.color = '#F1948A';
                }

                if (newObj.field == 'active') {
                  newObj.value = newObj.value == '1' ? 'Yes' : 'No';
                }

                if (newObj.field == 'startDate') {
                  newObj.value = (newObj.value) ? moment(newObj.value).format(AppComponent.DATETIME_FORMAT) : null;
                }

                newObj.date = (newObj.date) ? moment(newObj.date).format(AppComponent.DATETIME_FORMAT) : null;

                if (newObj.field == 'dateOfRelieving' || newObj.field == 'dateOfResign') {
                  newObj.value = (newObj.value) ? moment(newObj.value).format(AppComponent.DATE_FORMAT) : null;
                }

                const fieldConversion = newObj.field.replace(/([A-Z])/g, " $1");
                const finalField = fieldConversion.charAt(0).toUpperCase() + fieldConversion.slice(1);

                newObj.field = finalField;

                this.filteredEmployeeAuditHistory.push(newObj);
              }
            }
          });
        });

        this.filteredEmployeeAuditHistory.sort((a, b) => (b.date > a.date) ? 1 : -1);

        this.filteredEmployeeAuditHistory.forEach((object) => {
          let date;
          if (object.bucketName == 'Team/Project Changes') {
            if (object.field == 'Active' && object.value == 'No') {
              date = object.date;
            }
            let updateField = this.filteredEmployeeAuditHistory.find(x => x.date == date && x.field == 'Start Date');
            if (updateField) {
              updateField.field = 'End Date';
            }
          }
        });

        this.filteredEmployeeAuditHistory.forEach(employee => {
          employee.emp360updatedBy = employee.updatedBy;
        });

        this.lifeCycleChangeList = this.filteredEmployeeAuditHistory.filter(x => x.bucketName == 'Lifecycle Changes');
        this.teamProjectChangeList = this.filteredEmployeeAuditHistory.filter(x => x.bucketName == 'Team/Project Changes');
        this.kycUpdateList = this.filteredEmployeeAuditHistory.filter(x => x.bucketName == 'KYC Update');
        this.employeeInfoChangeList = this.filteredEmployeeAuditHistory.filter(x => x.bucketName == 'Employment Info Changes');

        this.modalRef = this.modalService.show(auditTemplate, { class: 'modal-lg' });

        console.log(this.filteredEmployeeAuditHistory, " : this.filteredEmployeeAuditHistory ");
      } else {
        console.log(response.serviceResponse, " audit response");
      }
    });
  }

  // Employee Audit :: end

  // //Doamin & Specialization  :: start

  getAllDomain(template?: TemplateRef<any>) {
    this.domainService.getAllDomain().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDomainList = response.serviceResponse;

        this.allDomainList.forEach((domain) => {
          domain.createdOn = (domain.createdOn) ? moment(domain.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        });
      } else {
        // this.openAlertMod(template, response.serviceResponse);
        console.error(response.serviceResponse);
      }
    });
  }



  getDomainSpecialization() {
    this.specializationList = [];

    let domainObj = new Domain();
    domainObj.domainIdList = this.employeeObj.domainList;
    this.domainService.getDomainSpecialization(domainObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.specializationList = response.serviceResponse;
        this.specializationList = this.specializationList.sort((a, b) => a.specializationName.localeCompare(b.specializationName));
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  // validateDomainObj(domainObj, template: TemplateRef<any>) {
  //   let flag = true;

  //   domainObj.domainName = domainObj.domainName?.trim();
  //   if (!this.validationService.validateNullUndefinedEmptyString(domainObj.domainName)) {
  //     this.alertMessage = "Please enter Domain Name !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }
  //   if (!this.validationService.validateTeamActivity(domainObj.domainName)) {
  //     this.alertMessage = "Please enter Valid Domain Name !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }

  //   const uniqueSpecialization = new Set(this.allSpecializationList.map(x => x.specializationName));
  //   if (uniqueSpecialization.size < this.allSpecializationList.length) {
  //     this.alertMessage = "Duplicate Specialization Name are not allowed !!"
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }

  //   this.allSpecializationList.forEach((spec, index) => {

  //     spec.specializationName = spec.specializationName?.trim();
  //     if (!this.validationService.validateNullUndefinedEmptyString(spec.specializationName)) {
  //       this.alertMessage = `Please enter Specialization - ${index + 1}!!`
  //       flag = false;
  //       return;
  //     }
  //     if (!this.validationService.validateTeamActivity(spec.specializationName)) {
  //       this.alertMessage = `Please enter valid Specialization - ${index + 1}!!`
  //       flag = false;
  //       return;
  //     }
  //   });

  //   if (!flag) {
  //     this.openAlertMod(template, this.alertMessage);
  //     return false;
  //   }else{
  //     return true;
  //   }
  // }

  // createDomain(template: TemplateRef<any>){

  //   let inputValidated: boolean = this.validateDomainObj(this.domainObj, template)
  //   if (!inputValidated) return;

  //   this.domainObj.allSpecializationList = this.allSpecializationList;
  //   this.domainObj.createdBy = this.currentUser.empId;
  //   this.domainService.createDomain(this.domainObj).pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus == "Success") {
  //       this.openAlertMod(template, response.serviceResponse);
  //       this.showAllDomain();
  //     } else {
  //       this.openAlertMod(template, response.serviceResponse);
  //     }
  //   });
  // }

  // updateDomain(template: TemplateRef<any>){
  //   let inputValidated: boolean = this.validateDomainObj(this.domainObj, template)
  //   if (!inputValidated) return;

  //   this.domainObj.allSpecializationList = this.allSpecializationList;
  //   this.domainObj.updatedBy = this.currentUser.empId;

  //   this.domainService.updateDomain(this.domainObj).pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus == "Success") {
  //       this.openAlertMod(template, response.serviceResponse);
  //       this.showAllDomain();
  //     } else {
  //       this.openAlertMod(template, response.serviceResponse);
  //     }
  //   });
  // }

  // deleteDomain(template: TemplateRef<any>) {
  //   this.domainToBeDeleted.updatedBy = this.currentUser.empId;

  //   this.domainService.deleteDomain(this.domainToBeDeleted).pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus == "Success") {
  //       this.openAlertMod(template, response.serviceResponse);
  //       this.showAllDomain();
  //     } else {
  //       this.openAlertMod(template, response.serviceResponse);
  //     }
  //   });
  // }

  // checkDomainName(domainName:any, template: TemplateRef<any>){

  //   let domainObj = new Domain();
  //   domainObj.domainName = domainName;
  //   domainObj.domainId = this.domainObj.domainId;
  //   this.domainService.checkDomainName(this.domainObj).pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus == "Fail") {
  //       this.domainObj.domainName = '';
  //       this.openAlertMod(template, response.serviceResponse);
  //     }
  //   });
  // }

  // //Doamin & Specialization  :: end


  /* Filter */
  openFilterModal(template: TemplateRef<any>, columns: any[], title: any) {
    console.log("columns : ", columns);
    this.queryList = [];

    this.filterData.title = title;
    this.filterData.columns = columns;

    this.queryList = [
      { column: "Employment Status", operator: "!=", value: "InActive", conjunction: "" }
    ];

    this.storedDataList.forEach((data) => {
      if (data.filterName == title) {
        data.queryList.forEach((queryObj) => {
          // if (queryObj.column == "Employee Id" && !queryObj.value.includes("A-")) {
          //   queryObj.value = "A-".concat(queryObj.value);
          // }

          if (queryObj.column == 'From Date' || queryObj.column == 'To Date' || queryObj.column == 'Date' || queryObj.column == 'Date Of Joining') {
            queryObj.value = (queryObj.value) ? moment(queryObj.value).format("DD-MM-YYYY") : '';
          } else if (queryObj.column == 'Created On' || queryObj.column == 'Updated On') {
            queryObj.value = (queryObj.value) ? moment(queryObj.value).format('DD-MM-YYYY HH:mm:ss') : '';
          }
        });
        this.queryList = data.queryList;
      }
    });

    this.filterData.queryList = JSON.stringify(this.queryList);

    console.log("filterData : ", this.filterData);
    this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
  }

  getCustomEmployeesList(queryObjList: any, template: TemplateRef<any>) {
    this.allEmployeeList = [];

    let queryObj = new Query();
    queryObj.queryList = queryObjList;

    if (queryObjList == '') {
      this.getAllEmployeeList();
    } else {
      this.employeeService.customQueryForEmployeeReport(queryObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allEmployeeList = response.serviceResponse;

          this.allEmployeeList = this.allEmployeeList.filter((value, index, self) =>
            index === self.findIndex((t) => (
              t.employeementId === value.employeementId
            ))
          )

          if (this.allEmployeeList.length == 0) {
            this.openAlertMod(this.alertTemplate, "No Data found")
          }
          this.allEmployeeList.forEach(employee => {
            // if (employee.isConsultant == 'true'){
            //   employee.employeementId = "A-".concat(employee.employeementId);
            // }else {
            //   employee.employeementId = "A-CS-".concat(employee.employeementId);
            // }
            employee.employeementId = "A-".concat(employee.employeementId);

            employee.dateOfBirth = (employee.dateOfBirth) ? moment(employee.dateOfBirth).format(AppComponent.DATE_FORMAT) : null;
            employee.dateOfJoining = (employee.dateOfJoining) ? moment(employee.dateOfJoining).format(AppComponent.DATE_FORMAT) : null;
            employee.createdOn = (employee.createdOn) ? moment(employee.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
            employee.updatedOn = (employee.updatedOn) ? moment(employee.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
            if (employee.isConsultant == 'true')
              employee.employeeType = 'Consultant';
            else if (employee.isApprenticeship == 'true')
              employee.employeeType = 'Apprentice';
            else if (employee.isApmosysProduct == 'true')
              employee.employeeType = 'Apmosys Product';
            else
              employee.employeeType = 'On roll';
          });
          console.log("allEmployeeList : ", this.allEmployeeList)
        } else {
          this.openAlertMod(template, response.serviceResponse)
        }
      });
    }
  }

  onFilterSubmit(emittedArray: any, template: TemplateRef<any>) {
    if (emittedArray[0].length != 0) {
      console.log("queryList : ", emittedArray[0]);
      this.queryList = JSON.parse(JSON.stringify(emittedArray[0]));
      this.cancelRequest();

      emittedArray[1].forEach((object) => {
        if (Object.keys(object).length !== 0) {
          if (this.storedDataList.find((x) => x.filterName == object.filterName)) {
            this.storedDataList = this.storedDataList.map(arr1 => emittedArray[1].find(arr2 => arr2.filterName === arr1.filterName) || arr1);
          } else {
            this.storedDataList.push(object);
          }
        }
      });

      emittedArray[0].forEach(query => {
        if (query.column == 'From Date' || query.column == 'To Date' || query.column == 'Date' || query.column == 'Date Of Joining') {
          query.value = (query.value) ? moment(query.value, "DD-MM-YYYY").format('YYYY-MM-DD') : '';
        } else if (query.column == 'Created On' || query.column == 'Updated On') {
          query.value = (query.value) ? moment(query.value, "DD-MM-YYYY").format('YYYY-MM-DD HH:mm:ss') : '';
        }

        // if (query.column == 'Employee Id') {
        //   query.value = query.value.split("-")[1];
        // }
      });

      if (this.filterData.title == 'Filter All Employee') {
        this.getCustomEmployeesList(emittedArray[0], template);
      }
    } else {
      let clearedFilter = this.storedDataList.find((filter) => filter.filterName == emittedArray[1]);
      this.storedDataList.splice(clearedFilter);


      if (emittedArray[1] == 'Filter All Employee') {
        this.showTable();
      }
    }
  }

  // modals
  openDeleteEmployee(template: TemplateRef<any>, employee: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.employeeObj = employee;
  }

  openDeleteDraftEmployee(template: TemplateRef<any>, employee: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.employeeObj = employee;
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }
  openAlertMod7(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }


  openApplicationRejectionMod(template: TemplateRef<any>, employee: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-md' });
    this.employeeObj = employee;
  }

  openApplicationApprovalMod(template: TemplateRef<any>, employee: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-md' });
    this.employeeObj = employee;
  }

  // openDeleteDomainMod(template: TemplateRef<any> , domain: any){
  //   this.domainToBeDeleted = domain;
  //   this.modalRef = this.modalService.show(template);
  // }

  cancelApplication() {
    this.previewModalRef.hide();
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  pageReload() {
    window.location.reload();
  }
  cancelDraftRequest() {
    this.employeeObj.remarks = ''
    this.modalRef.hide();
  }


  //pagination
  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  pageNo = 1;
  handlePageChanges(event) {
    this.pageNo = event;
  }

  handleAuditPageChanges(event) {
    this.pageNo = event;
  }

  handleItemChanges(event) {
    this.pageNo = event;
  }

  onPage() {
    this.page = 1;
  }
  // implement Enable Account facilities by anurag

  forEnableAccount(template: TemplateRef<any>, employee: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.employeeObj = employee;
  }



  findEmployeeWorkingHistory(template: TemplateRef<any>, employee: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
    this.employeeObj = employee;
  }

  onRevokeAccount(template: TemplateRef<any>) {
    this.cancelRequest();
    // if (this.employeeObj.isConsultant == 'true'){
    //   this.employeeObj.employeementId = this.employeeObj.employeementId.substring(5);
    // }else {
    //   this.employeeObj.employeementId = this.employeeObj.employeementId.substring(2);
    // }
    this.employeeObj.employeementId = this.employeeObj.employeementId.substring(2);
    this.employeeService.revokeAccount(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }


  onFindHistoryWorking(employee: Employee) {
    this.employeeWorkingHistory = [];
    let empObj = new Employee();
    empObj.empId = employee.empId;

    this.cancelRequest();
    this.employeeService.findEmployeeWorkingHistory(empObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.employeeWorkingHistory = response.serviceResponse;
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  sortData(sort: Sort) {
    console.log(sort);
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
    if (this.isSearchEnabled == true) {
      this.filters = searchData;
      console.log("Updated Filter : ", this.filters);
    }
  }


  getCurrentVisibleColumns(): string[] {
    const primaryColumns = ['employmentIdAcToET', 'name', 'email', 'departmentName', 'managerName', 'dateOfJoining', 'employmentstatus'];

    let columnsWithBlanks = [...primaryColumns];
    if (this.userMapping.update_employee || this.userMapping.delete_employee || this.userMapping.update_draft) {
      columnsWithBlanks.push('blank');
    }

    columnsWithBlanks.push('blank');

    if (!this.showExpandedColumns) {
      return columnsWithBlanks;
    }

    if (this.isDraftTable) {
      const expandedCols = this.draftEmployeeColumns.slice(columnsWithBlanks.length);
      return [...columnsWithBlanks, ...expandedCols];
    } else if (this.isTable && this.dateOfReleivingshow) {
      const expandedCols = this.employeeInActiveColumns.slice(columnsWithBlanks.length);
      return [...columnsWithBlanks, ...expandedCols];
    } else if (this.isTable) {
      const expandedCols = this.employeeActiveColumns.slice(columnsWithBlanks.length);
      return [...columnsWithBlanks, ...expandedCols];
    }

    return columnsWithBlanks;
  }

  getPrimaryColumnsForFilter(): string[] {
    const primaryColumns = ['employeementId', 'name', 'email', 'departmentName', 'managerName', 'dateOfJoining', 'employmentstatus'];

    const columnsWithBlanks = [...primaryColumns];
    if (this.userMapping.update_employee || this.userMapping.delete_employee || this.userMapping.update_draft) {
      columnsWithBlanks.push('blank');
    }

    columnsWithBlanks.push('blank');

    return columnsWithBlanks;
  }

  getExpandedColumns(fullColumnList: string[]): string[] {
    const primaryColumnsCount = this.getPrimaryColumnsForFilter().length;
    return fullColumnList.slice(primaryColumnsCount);
  }

  toggleWorkHistorySearch() {
    this.isworkHistorySearchEnabled = !this.isworkHistorySearchEnabled;
  }

  onWorkHistorySearch(searchData) {
    this.filters = searchData;
    console.log("Updated Filter : ", this.filters);
  }


  onsearchhistory(searchData) {
    this.filterOnhistory = searchData;
  }

  toggleAuditSearch() {
    this.isAuditSearchEnabled = !this.isAuditSearchEnabled;
  }

  onAuditSearch(searchData) {
    this.auditFilter = searchData;
    console.log("Audit Updated Filter : ", this.filters);
  }



  exportData(type: string): void {
    let data: any[] = [];
    switch (type) {
      case 'fullJourney':
        data = this.filteredEmployeeAuditHistory.map(item => ({
          Date: item.date,
          Field: item.field,
          Value: item.value,
          Bucket: item.bucketName,
          UpdatedBy: item.updatedByName
        }));
        break;
      case 'lifeCycle':
        data = this.lifeCycleChangeList.map(item => ({
          Date: item.date,
          Field: item.field,
          Value: item.value,
          Bucket: item.bucketName,
          UpdatedBy: item.updatedByName
        }));
        break;
      case 'teamProject':
        data = this.teamProjectChangeList.map(item => ({
          Date: item.date,
          Field: item.field,
          Value: item.value,
          Bucket: item.bucketName,
          UpdatedBy: item.updatedByName
        }));
        break;
      case 'kycUpdate':
        data = this.kycUpdateList.map(item => ({
          Date: item.date,
          Field: item.field,
          Value: item.value,
          Bucket: item.bucketName,
          UpdatedBy: item.updatedByName
        }));
        break;
      case 'employeeInfo':
        data = this.employeeInfoChangeList.map(item => ({
          Date: item.date,
          Field: item.field,
          Value: item.value,
          Bucket: item.bucketName
        }));
        break;
      case 'employeehistoryID':
        data = this.employeeWorkingHistory.map(item => ({
          EmployeeName: item.name,
          TeamName: item.teamName,
          ProjectName: item.projectName,
          StartDate: item.startDate,
          EndDate: item.updatedOn,
          TeamLeadName: item.teamLeadName,
          JobRole: item.jobRoleName,
          ClientLocation: item.clientLocation,
          ClientName: item.clientName,
        }));

        break;
      default:
        console.error('Unknown export type');
        return;
    }
    this.exportExcelService.exportTableDataToExcel(data, `${type}.xlsx`);
  }

  // by priyadarshini
  resetField_OnChange(updateType) {
    updateType.employmentReleaseStatus = "";
    updateType.newManagerId = "";
    this.employeeObj.newManagerId = '';
  }
  resetFieldOnChange(updateType) {
    updateType.employmentstatus = "";
    updateType.newManagerId = "";
    this.employeeObj.newManagerId = '';
  }
  resetField(updateType) {
    // updateType.employmentReleaseStatus = "";
    updateType.newManagerId = "";
    this.employeeObj.newManagerId = '';
    this.employeeObj.dateOfResign='';
    this.employeeObj.dateOfRelieving='';
    if(this.statusFlag){this.employeeObj.noticePeriod=this.actualNoticePeriod;}
  }
  // added by anurag 

  // mapLeavesAndCompOffToNewManager(employee){
  //   //console.log(" employee   ",employee);
  //   //console.log(" pre employee ",this.employeeObj.managerId);
  //   let emp = new Employee();
  //   emp.empId=employee.empId;
  //   emp.managerId=employee.managerId;

  //   this.employeeService.mapLeavesAndCompOffToNewManager(emp).pipe(first()).subscribe((response:any)=>{
  //     if(response.serviceStatus == "Success"){
  //       //console.log(" method successfully call ")
  //     }
  //   })
  // }

  //  added by anuarg

  PIP_generate(template: TemplateRef<any>, employee) {
    this.isPipGenerate = true;
    this.startDate = '';
    this.endDate = '';
    this.employeeObj.pipReason = ''
    this.modalRef = this.modalService.show(template, { class: 'modal-md' });
    this.employeeObj = employee;
  }

  PIP_reverse_modal(template: TemplateRef<any>, team) {
    this.isToggle = false;
    this.isPipGenerate = false;
    this.modalRef = this.modalService.show(template, { class: 'modal-md' });
    this.employeeObj = team;
    this.getPipDetailsByEmpId(team);
  }

  getPipDetailsByEmpId(employee: any) {
    // console.log(" emp details  ",employee);
    let leaveObj = new Leave();
    leaveObj.empId = employee.empId;

    this.leaveService.getPipDetailsByEmpId(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {

        this.employeeObj = Object.assign({}, response.serviceResponse);

        this.startDate = this.employeeObj.startDate;
        this.endDate = this.employeeObj.endDate;

        this.endDate = (this.endDate) ? moment(this.endDate, AppComponent.DATE_FORMAT).toDate() : '';
        this.startDate = (this.startDate) ? moment(this.startDate, AppComponent.DATE_FORMAT).toDate() : '';

        if (this.endDate) {
          this.tempValue = new Date(this.endDate);
          this.minDateForExtend = new Date(this.endDate);
          this.maxDateForExtend = new Date(this.endDate);
          this.maxDateForExtend.setMonth(this.maxDateForExtend.getMonth() + 2);
        }


        console.log(" startDate   ", this.startDate);
        console.log(" endDate   ", this.endDate);

      }
    })

  }

  PIP_reverse(template: TemplateRef<any>, teamObj, flag) {
    this.isPipGenerate = false;
    console.log(teamObj);
    let empObj = new Employee();
    empObj.pipFlag = flag;
    empObj.pipId = teamObj.pipId;
    empObj.empId = teamObj.empId;
    empObj.updatedBy = this.currentUser.empId;
    empObj.updatedByName = this.currentUser.name;
    empObj.revReason = teamObj.revReason;
    empObj.startDate = moment(this.startDate).format(AppComponent.DATE_FORMAT);
    if (this.isToggle) {
      empObj.endDate = moment(this.endDate).format(AppComponent.DATE_FORMAT);
    } else {
      let endDate: any = new Date();
      endDate = moment(endDate).format(AppComponent.DATE_FORMAT);
      console.log("endDate   ", endDate);
      empObj.endDate = endDate;
    }

    this.employeeService.pipReturnFromUser(empObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.getAllEmployeeList();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    })
  }

  PipGenerateToUser(template: TemplateRef<any>, leaveObj, flag) {
    console.log(" leaveObj   ", leaveObj);
    let emp = new Employee();
    emp.empId = leaveObj.empId;
    emp.pipReason = leaveObj.pipReason;
    emp.startDate = moment(this.startDate).format(AppComponent.DATE_FORMAT);
    emp.endDate = moment(this.endDate).format(AppComponent.DATE_FORMAT);
    emp.pipFlag = flag;
    emp.createdBy = this.currentUser.empId;
    emp.createdByName = this.currentUser.name;

    console.log(emp);
    this.employeeService.pipGenerateToUser(emp).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.getAllEmployeeList();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    })
  }

  togglePipView(event) {
    this.employeeObj.revReason = '';
    this.isDateChanged = false;
    if (event.target.checked) {
      this.isToggle = true;
      this.employeeObj.extendReason = '';

    } else {
      this.isToggle = false;
      // this.leaveObj.endDate = this.tempValue;
      this.endDate = this.tempValue;
    }
  }


  pipReason(teamObj) {
    let empObj = new Employee();

    empObj.empId = teamObj.empId;
    empObj.pipId = teamObj.pipId;
    empObj.pipFlag = teamObj.pipFlag;


    this.employeeService.getPipReasons(empObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.pipReasons = response.serviceResponse;
        if (this.pipReasons.length == 0) {
          if (this.employeeObj.pipFlag == "true") this.isPipFlag = true;
          else this.isPipFlag = false;
        }
        this.pipReasons.forEach(d => {
          d.createdOn = moment(d.createdOn).format(AppComponent.DATE_FORMAT);
          d.updatedOn = moment(d.updatedOn).format(AppComponent.DATE_FORMAT);
          console.log(" d ki value ", d)
          if (d.pipFlag == "true") {
            console.log("i am in true flag")
            this.isPipFlag = true;
          } else {
            console.log(" I'm in false flag")
            this.isPipFlag = false;
          }
          if (d.createdOn == 'Invalid date') {
            d.createdOn = '';
          }
          if (d.updatedOn == 'Invalid date') {
            d.updatedOn = '';
          }
        })
        console.log("this.pipReasons  ", this.pipReasons);
      }
    })
    console.log(" team ", empObj);
    console.log(empObj, "teamteamteamteam")
  }

  pipReasonModal(template: TemplateRef<any>, teamObj) {
    this.pageNo = 1;
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
    this.employeeObj = teamObj;
    this.pipReason(this.employeeObj);
  }

  extendPipModal(template: TemplateRef<any>, teamObj) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.employeeObj = teamObj;
  }

  checkDateChange() {
    this.isDateChanged = true;
  }



  onUpadateReportees(event: any) {
    console.log('data printed ----', event.target.value);
    if (event.target.value == 'Yes') {
      this.employeeObj.updateType = '';
      this.onselectYes = true;
    } else if (event.target.value == 'No') {
      this.onselectYes = false;
      this.employeeObj.updateType = '';
    } else {
      console.log('data printed ----', event.target.value);
      this.onselectYes = false;
      this.employeeObj.updateType = '';
    }
  }


  setPipExtendsDays(template: TemplateRef<any>) {

    if (!this.isDateChanged) {
      this.alertMessage = "End date must be change for extend PIP";
      this.openAlertMod(template, this.alertMessage);
      return;
    }
    this.cancelRequest();
    let leave = new Employee();
    leave.pipId = this.employeeObj.pipId;
    leave.empId = this.employeeObj.empId;
    leave.updatedByName = this.currentUser.name;
    leave.extendReason = this.employeeObj.extendReason;
    // leave.extendDays = this.leaveObj.extendDays;
    leave.startDate = moment(this.startDate).format(AppComponent.DATE_FORMAT);
    leave.endDate = moment(this.endDate).format(AppComponent.DATE_FORMAT);
    console.log("team in set extend modal", leave)
    this.employeeService.setExtendPeriodByPipId(leave).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);

      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    })

  }

  estimateEndDate() {
    if (this.startDate) {
      const startDate = new Date(this.startDate);
      const endDate = new Date(startDate.getTime() + (90 * 24 * 60 * 60 * 1000)); // Adding 90 days
      this.endDate = endDate.toISOString().split('T')[0];
    }
  }

  //added by priyadarshini
  onselectYes: boolean = false;




  onEmployeeTypeChange(selectedType: string, template: TemplateRef<any>): void {

    if (!this.employeeObj?.employeementId || this.employeeObj?.employeementId?.toString().trim() === '') {
      this.employeeObj.employeementId = this?.userEmployeementId;
    }
    switch (selectedType) {
      case 'Regular':
        this.employeeObj.isConsultant = 'false';
        this.employeeObj.isApprenticeship = 'false';
        this.employeeObj.isApmosysProduct = 'false';
        break;
      case 'Consultant':
        this.employeeObj.isConsultant = 'true';
        this.employeeObj.isApprenticeship = 'false';
        this.employeeObj.isApmosysProduct = 'false';
        break;
      case 'Apprentice':
        this.employeeObj.isConsultant = 'false';
        this.employeeObj.isApprenticeship = 'true';
        this.employeeObj.isApmosysProduct = 'false';
        break;
      case 'Apmosys Product':
        this.employeeObj.isConsultant = 'false';
        this.employeeObj.isApprenticeship = 'false';
        this.employeeObj.isApmosysProduct = 'true';
        break;
      default:
        this.employeeObj.isConsultant = null;
        this.employeeObj.isApprenticeship = null;
        this.employeeObj.isApmosysProduct = null;
    }
    if (this.isCreation && (this.employeeObj?.employeementId && this.employeeObj?.employeementId?.toString().trim() != "")) {
      this.checkEmployeementIdWithDifferentPrefix(template);

    }
    else if (this.isUpdation) {
      this.isEmployeementTypeChanged=true;
      this.checkEmployeementIdWithDifferentPrefix(template);
    }

  }

  getReporteesListByReportingManagerId() {
    console.log(" empId in manager UI change ", this.employeeObj.name);

    if (this.employeeObj.employeementId.startsWith("A-")) {
      this.employeeObj.employeementId = this.employeeObj.employeementId.substring(2);
    }

    console.log("employment id", this.employeeObj.employeementId);
    this.employeeService.getReporteesListByReportingManagerId(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.reporteeList2 = response.serviceResponse;
        this.getManagersList();
        // this.employeeObj.managerId = '';
        //console.log(" teamMember list   ",this.TeamMemberList)
      }
    })
  }

  estimateDateOfRetain(employeeObj: Employee) {
    const dateFormat = 'YYYY-MM-DD';

    if (this.employeeObj.dateOfRetain) {
      let estimateDateOfRetain = new Date(this.employeeObj.dateOfRetain);
      this.employeeObj.dateOfRetain = moment(estimateDateOfRetain).format(dateFormat);
      console.log(this.employeeObj.dateOfRetain, "this.employeeObj.dateOfRetain")
    }

    console.log(this.employeeObj.dateOfRetain);
  }

  resetEmployee() {
    this.employeeObj.empId = '';
    this.employeeObj.draftEmpId = '';
    this.employeeObj.employeementId = '';
    this.employeeObj.managerId = '';
    this.employeeObj.dateOfJoining = '';
    this.employeeObj.dateOfBirth = '';
    this.employeeObj.gender = '';
    this.employeeObj.bloodGroup = '';
    this.employeeObj.maritalStatus = '';
    this.employeeObj.fatherName = '';
    this.employeeObj.placeOfBirth = '';
    this.employeeObj.motherTongue = '';
    this.employeeObj.passportNumber = '';
    this.employeeObj.aadhar = '';
    this.employeeObj.panNumber = '';
    this.employeeObj.mobileNo = '';
    this.employeeObj.landline = '';
    this.employeeObj.address = '';
    this.employeeObj.city = '';
    this.employeeObj.state = '';
    this.employeeObj.country = '';
    this.employeeObj.pincode = '';
    this.employeeObj.alternateMobileNo = '';
    this.employeeObj.permanentAddress = '';
    this.employeeObj.emergencyContactPerson = '';
    this.employeeObj.relation = '';
    this.employeeObj.emergencyContactMobile = '';
    this.employeeObj.employmentstatus = '';
    this.employeeObj.noticePeriod = '';
    this.employeeObj.isDraft = false;
    this.employeeObj.jobRoleId = '';
    this.employeeObj.bankName = '';
    this.employeeObj.bankAccountNo = '';
    this.employeeObj.bankIFSCCode = '';
    this.employeeObj.pfAccountNumber = '';
    this.employeeObj.previousPfAccountNumber = '';
    this.employeeObj.uan = '';
    this.employeeObj.esicNumber = '';
    this.employeeObj.graduationType = '';
    this.employeeObj.pursuing = '';
    this.employeeObj.yearOfPassing = '';
    this.employeeObj.passingGrade = '';
    this.employeeObj.certifications = '';
    this.employeeObj.aboutMe = '';
    this.employeeObj.viewsOnOrganisation = '';
    this.employeeObj.jobRoleName = '';
    this.employeeObj.departmentId = '';
    this.employeeObj.departmentName = '';
    this.employeeObj.managerName = '';
    this.employeeObj.experience = '';
    this.employeeObj.previousEmploymentList = '';
    this.employeeObj.updatedCertifications = '';
    this.employeeObj.updatedPreviousEmploymentList = '';
    this.employeeObj.role = '';
    this.employeeObj.departmentList = '';
    this.employeeObj.workLocation = '';
    this.employeeObj.createdOn = '';
    this.employeeObj.createdBy = '';
    this.employeeObj.updatedOn = '';
    this.employeeObj.updatedBy = '';
    this.employeeObj.imageBytes = '';
    this.employeeObj.invalidAccessAttempt = '';
    this.employeeObj.failedAttempt = '';
    this.employeeObj.secondaryEmail = '';
    this.employeeObj.updateApplicationStatus = '';
    this.employeeObj.probationPeriod = '';
    this.employeeObj.dateOfResign = '';
    this.employeeObj.remarks = '';
    this.employeeObj.documentList = '';
    this.employeeObj.isUserInfoUpdated = true;
    this.employeeObj.dateOfRelieving = '';
    this.employeeObj.relievingMonth = '';
    this.employeeObj.joiningMonth = '';
    this.employeeObj.empIdAppreciated = '';
    this.employeeObj.appreciateType = '';
    this.employeeObj.reason = '';
    this.employeeObj.name = '';
    this.employeeObj.nameAppreciate = '';
    this.employeeObj.email = '';
    this.employeeObj.emailAppreciated = '';
    this.employeeObj.updateChild = '';
    this.employeeObj.childLists = '';
    this.employeeObj.appreciationBy = '';
    this.employeeObj.appreciationTo = '';
    this.employeeObj.managerMail = '';
    this.employeeObj.spouse = '';
    this.employeeObj.child1 = '';
    this.employeeObj.child2 = '';
    this.employeeObj.child3 = '';
    this.employeeObj.billable = '';
    this.employeeObj.mothersName = '';
    this.employeeObj.totalExperience = '';
    this.employeeObj.teamId = '';
    this.employeeObj.teamName = '';
    this.employeeObj.teamLeadId = '';
    this.employeeObj.projectId = '';
    this.employeeObj.projectName = '';
    this.employeeObj.startDate = '';
    this.employeeObj.endDate = '';
    this.employeeObj.teamLeadName = '';
    this.employeeObj.employeeRole = '';
    this.employeeObj.clientName = '';
    this.employeeObj.clientLocation = '';
    this.employeeObj.isAppreciationEnable = false;
    this.employeeObj.appreciationEventId = '';
    this.employeeObj.newManagerId = '';
    this.employeeObj.oldManagerId = '';
    this.employeeObj.isSelected = false;
    this.employeeObj.columnHeader = '';
    this.employeeObj.reporteeCount = '';
    this.employeeObj.hierarchyType = '';
    this.employeeObj.deptHeadConsentList = '';
    this.employeeObj.consentMailLink = '';
    this.employeeObj.profileCompletedPercent = '';
    this.employeeObj.updatedByName = '';
    this.employeeObj.createdByName = '';
    this.employeeObj.isTimesheetLockCheckEnable = '';
    this.employeeObj.timesheetLockUpdatedOn = '';
    this.employeeObj.timesheetBackDatedDays = '';
    this.employeeObj.compOffLockDays = '';
    this.employeeObj.reportingManagerId = '';
    this.employeeObj.approvalsTo = '';
    this.employeeObj.reportingManagerName = '';
    this.employeeObj.reportingManagerEmail = '';
    this.employeeObj.specializationList = '';
    this.employeeObj.domainList = '';
    this.employeeObj.designationId = '';
    this.employeeObj.designationName = '';
    this.employeeObj.timesheetStatus = '';
    this.employeeObj.resignationStatus = '';
    this.employeeObj.unlockTimesheetFor = '';
    this.employeeObj.employmentReleaseStatus = '';
    this.employeeObj.updateType = '';
    this.employeeObj.pipReason = '';
    this.employeeObj.pipFlag = '';
    this.employeeObj.pipId = '';
    this.employeeObj.revReason = '';
    this.employeeObj.extendDays = '';
    this.employeeObj.billableType = '';
    this.employeeObj.profileKycStatus = '';
    this.employeeObj.extendReason = '';
    this.employeeObj.reportiesFlag = '';
    this.employeeObj.employeeNameForReward = '';
    this.employeeObj.employeeIdForReward = '';
    this.employeeObj.managerNameForReward = '';
    this.employeeObj.managerIdForReward = '';
    this.employeeObj.rewardId = '';
    this.employeeObj.isConsultant = '';
    this.employeeObj.onbenchDate = '';
    this.employeeObj.employeeType = '';
    this.employeeObj.isApprenticeship = '';
    this.employeeObj.isRegular = '';
    this.employeeObj.isRetain = '';
    this.employeeObj.dateOfRetain = '';
    this.employeeObj.referedType = '';
    this.employeeObj.referedName = '';
    this.employeeObj.employeeConfirmationDate = '';
    this.employeeObj.emp360 = '';
    this.employeeObj.selectedProjectId = '';
    this.employeeObj.employmentId = '';
    this.employeeObj.resourceOverviewId = '';
    this.employeeObj.defaultprojectType = '';
    this.employeeObj.defaultProjectId = '';
    this.employeeObj.defaultProjectName = '';
    this.employeeObj.defaultTeamId = '';
    this.employeeObj.isShadowResource = '';
    this.employeeObj.defaultTeamEmployeeRole = '';
    this.employeeObj.selectedResourceOverviewId = '';

  }

  //added to optimize the code featch managerlist at a time and overcome from undefied employee onject
  loadManagerList(): void {
    this.employeeObj.employeementId = this.employeeObj.employeementId?.substring(2);

    let employeeObjManager: Partial<Employee> = {
      role: "Manager"
    }
    this.employeeService
      .getAllEmployeesByRoleForManager(employeeObjManager)
      .pipe(first())
      .subscribe((response: any) => {
        if (response.serviceStatus === "Success") {
          const employeeList = response.serviceResponse;

          console.log("employeeList By Role:", employeeList);

          // Store original result for reuse
          this.managerListOriginal = employeeList;
          this.managerList = [...this.managerListOriginal];

        } else {
          console.error(response.serviceResponse);
        }
      });
  }

  applyManagerFilter(employee: Employee): void {
    if (this.isUpdation || this.isDeletion) {
      this.managerList = this.managerListOriginal.filter(
        (manager: Employee) => manager.empId !== employee.empId
      );
    } else {
      this.managerList = [...this.managerListOriginal];
    }
  }

  isEmployeeIdDisabled(): boolean {
    const prefix = this.getEmpIdPrefix(this.employeeObj.employeeType);

    if (this.isCreation === true) {
      return false;
    }
    else if (this.employeeObj.employeeType === 'Apmosys Product' && this.isApmosysProductUpdate === true) {
      return true; //block
    }
    else if (this.employeeObj.employeeType != 'Apmosys Product' && this.isApmosysProductUpdate === false) {
      return true; //block 
    } else if (this.employeeObj.employeeType === 'Apmosys Product' && this.isApmosysProductUpdate === false) {
      return false; //update
    }
    else if (this.employeeObj.employeeType != 'Apmosys Product' && this.isApmosysProductUpdate === true) {
      return true; //block
    }
    else {
      return false; //update
    }
  }
  getInputRestrictionMethod(): (event: any) => boolean {
    const prefix = this.getEmpIdPrefix(this.employeeObj.employeeType);

    if (prefix === 'CS-') {
      return this.fieldRestictCharacterCS;
    } else {
      return this.fieldRestictCharacter;
    }
  }

 calculateTotalExperience() {
    this.employeeObj.totalCurrentExperience=this.employeeService.calculateTotalExperience(
          this.employeeObj.totalExperience, this.employeeObj.dateOfJoining );
}
 

}





function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
}
