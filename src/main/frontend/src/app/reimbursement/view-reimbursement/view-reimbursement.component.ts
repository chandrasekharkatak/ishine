import { Component, OnInit, SecurityContext, TemplateRef, ViewChild } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Sort } from '@angular/material/sort';
import { first } from 'rxjs/operators';
import { Employee } from 'src/app/models/employee';
import { MyReimbursement } from 'src/app/models/reimbursement';
import { MyTravelDesk } from 'src/app/models/travelDesk';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { ReimbursementService } from 'src/app/services/reimbursement.service';
import { ReimbursementTicketModalComponent } from '../reimbursement-ticket-modal/reimbursement-ticket-modal.component';
import {
  approvalLevelCell,
  buildTicketFilterColumns,
  deriveTableLevelColumns,
  RmbApprovalLevelColumn
} from '../rmb-approval-levels.helper';

@Component({
  standalone: false,
  selector: 'app-view-reimbursement',
  templateUrl: './view-reimbursement.component.html',
  styleUrls: ['./view-reimbursement.component.css']
})
export class ViewReimbursementComponent implements OnInit {


  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;
  @ViewChild('editTravelRequestModal') modalElement: any;
  travelDeskInfo: MyTravelDesk;
  reimbursementInfo: MyReimbursement;
  currentEmployeeInfo: Employee = new Employee();
  travelRequests: any = [];
  reimbursementRequests: any = [];
  myReimbursementTickets: any[] = [];
  selectedTicket: any = null;
  ticketModalRef: NgbModalRef | null = null;
  selectedReimbursementRequest: any = [];
  selectedTravelRequest: any = [];
  selectedReimbursementdataforDelete: any = [];
  domainSpecializationList: any[];
  currentUser: any;
  alertMessage: any;

  constructor(
    private modalService: NgbModal,
    private sanitizer: DomSanitizer,
    private employeeService: EmployeeService,
    private authenticationService: AuthenticationService,
    private exportExcelService: ExportExcelService,
    private reimbursementService: ReimbursementService,
  ) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x) }

  // Table UX: filter + sort + paginate (Timesheet-style)
  isSearchEnabledTickets = false;
  ticketFilters: any = {};
  private readonly ticketStaticFilterColumns = [
    'ticketNo',
    'displayStatus',
    'workflowStage',
    'totalClaimAmount',
    'paidClaimAmount',
    'submittedOn',
    'rejectionSummaryText',
    'blank',
    'blank'
  ];
  ticketActiveColumns: string[] = [...this.ticketStaticFilterColumns];
  tableLevelColumns: RmbApprovalLevelColumn[] = [];
  readonly approvalLevelCell = approvalLevelCell;
  sortColumn = '';
  sortColumnType = '';
  sortDirection = '';
  pageTickets = 1;

  ngOnInit(): void {
    this.onGetEmployeeInfo();
    this.onGetReimbursementInfo();
  }

  async onGetReimbursementInfo() {
    if (this.isValidForm()) {

      this.reimbursementInfo = new MyReimbursement();
      let reimbursementData = new MyReimbursement();

      console.log('currentEmployeeInfo ::::::::::::::::', this.currentEmployeeInfo);
      reimbursementData.empId = this.currentUser.empId;

      console.log('reimbursementData Data  ::::::::::::::::', reimbursementData);

      const response: any = await this.reimbursementService.fetchReimbursementData(reimbursementData).toPromise();

      if (response.serviceStatus === "Success") {
        this.reimbursementRequests = response.serviceResponse;
        console.log('Fetched Reimbursement Requests:', this.reimbursementRequests);
        this.selectedReimbursementRequest = this.reimbursementRequests;
      } else {
        console.error('Error fetching data:', response.serviceResponse);
      }

      const tr: any = await this.reimbursementService.fetchMyReimbursementTickets({ empId: this.currentUser.empId })
        .pipe(first()).toPromise();
      if (tr.serviceStatus === 'Success') {
        this.myReimbursementTickets = tr.serviceResponse || [];
      } else {
        this.myReimbursementTickets = [];
      }
      await this.refreshTableLevelColumns();
    }
  }

  get ticketTableColspan(): number {
    return 6 + this.tableLevelColumns.length * 2 + 3;
  }

  private async refreshTableLevelColumns(): Promise<void> {
    let fallback: RmbApprovalLevelColumn[] = [];
    try {
      const res: any = await this.reimbursementService
        .resolveReimbursementApprovalMatrixForEmployee(Number(this.currentUser.empId))
        .pipe(first())
        .toPromise();
      if (res?.serviceStatus === 'Success' && res.serviceResponse?.levelColumns) {
        fallback = res.serviceResponse.levelColumns;
      }
    } catch {
      fallback = [];
    }
    this.tableLevelColumns = deriveTableLevelColumns(this.myReimbursementTickets, fallback);
    this.ticketActiveColumns = buildTicketFilterColumns(this.ticketStaticFilterColumns, this.tableLevelColumns.length);
  }

  openTicketDetailsModal(template: TemplateRef<any>, t: any) {
    this.selectedTicket = t;
    this.ticketModalRef = this.modalService.open(template, { size: 'lg', backdrop: 'static' });
  }

  openTicketModal(t: any) {
    const ref = this.modalService.open(ReimbursementTicketModalComponent, {
      size: 'xl',
      backdrop: 'static',
      windowClass: 'rmb-ticket-modal'
    });
    const copy = JSON.parse(JSON.stringify(t));
    ref.componentInstance.ticket = copy;
    ref.componentInstance.actor = { empId: this.currentUser?.empId, email: this.currentUser?.email };
    ref.result
      .then((r: any) => {
        if (r?.refreshed) {
          this.onGetReimbursementInfo();
        }
      })
      .catch(() => {});
  }

  closeTicketDetailsModal() {
    this.ticketModalRef?.close();
    this.ticketModalRef = null;
    this.selectedTicket = null;
  }

  sortTicketData(sort: Sort) {
    if (sort.active) {
      const sortParams: any[] = sort.active?.split('|');
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

  toggleTicketSearch() {
    this.isSearchEnabledTickets = !this.isSearchEnabledTickets;
    if (!this.isSearchEnabledTickets) {
      this.ticketFilters = {};
    }
  }

  onTicketSearch(searchData: any) {
    if (this.isSearchEnabledTickets) {
      this.ticketFilters = searchData;
    }
  }

  handleTicketPageChange(event: any) {
    this.pageTickets = event;
  }

  /** Amount paid (sum of PAID claims); shown once Finance completes payment. */
  ticketPaidAmountDisplay(t: any): number | null {
    if (!t) return null;
    const n = Number(t.paidClaimAmount);
    if (!Number.isFinite(n)) return null;
    if (t.workflowStage === 'PAID') {
      return n;
    }
    return n > 0 ? n : null;
  }

  rejectedClaimLines(t: any): any[] {
    const rows = t?.rejectedClaimLines;
    return Array.isArray(rows) ? rows : [];
  }

  isValidForm() {
    return true;
  }
  //pagination

  page = 1;
  handlePageChange(event) {
    this.page = event;
  }
  //alertMessage: any;
  modalRef:NgbModalRef;
  modalRef2:NgbModalRef;
  openAlertMod1(template: TemplateRef<any>) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-lg' });
  }

  openAlertMod2(template: TemplateRef<any>, message: any) {
    this.modalRef2 = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  openEditModal(template: TemplateRef<any>, row: any) {

    this.selectedReimbursementRequest = { ...row };

    this.selectedReimbursementRequest.fromDate = this.formatDate(this.selectedReimbursementRequest.fromDate);
    this.selectedReimbursementRequest.toDate = this.formatDate(this.selectedReimbursementRequest.toDate);
    this.selectedReimbursementRequest.appliedOn = this.formatDate(this.selectedReimbursementRequest.appliedOn);
    this.selectedReimbursementRequest.dateOfFood = this.formatDate(this.selectedReimbursementRequest.dateOfFood);
    //  console.log('editpain asichi re ::::::::::::::::::::',this.selectedTravelRequest);

    console.log('editpain asichi re ::::::::::::::::::::', this.selectedReimbursementRequest);

    this.openAlertMod1(template);

  }

  formatDate(date: string | Date | null): string | null {
    if (!date) return null;
    const d = new Date(date);

    if (isNaN(d.getTime())) {
      console.error("Invalid date:", date);
      return null;
    }

    const month = ('0' + (d.getMonth() + 1)).slice(-2);
    const day = ('0' + d.getDate()).slice(-2);
    const year = d.getFullYear();

    return `${year}-${month}-${day}`;
  }



  async updateRequest(template: TemplateRef<any>) {

    console.log("Update pain asichi  ::::::::::::::::");

    this.reimbursementInfo = new MyReimbursement();
    let newreimbursementData = new MyReimbursement();
    newreimbursementData.requestId = this.selectedReimbursementRequest.requestId;
    newreimbursementData.empId = this.selectedReimbursementRequest.empId;
    newreimbursementData.name = this.selectedReimbursementRequest.name;
    newreimbursementData.email = this.selectedReimbursementRequest.email;
    newreimbursementData.departmentName = this.selectedReimbursementRequest.department;
    newreimbursementData.designationName = this.selectedReimbursementRequest.designationName;
    newreimbursementData.mobileNo = this.selectedReimbursementRequest.mobileNo;
    newreimbursementData.managerName = this.selectedReimbursementRequest.managerName;
    newreimbursementData.travelMode = this.selectedReimbursementRequest.travelMode;
    newreimbursementData.expenditureType = this.selectedReimbursementRequest.expenditureType;
    newreimbursementData.distance = this.selectedReimbursementRequest.distance;
    newreimbursementData.currency = this.selectedReimbursementRequest.currency;
    newreimbursementData.amount = this.selectedReimbursementRequest.amount;
    newreimbursementData.appliedBy = this.selectedReimbursementRequest.appliedBy;
    newreimbursementData.appliedOn = this.selectedReimbursementRequest.appliedOn;
    newreimbursementData.fromDate = this.selectedReimbursementRequest.fromDate;
    newreimbursementData.toDate = this.selectedReimbursementRequest.toDate;
    newreimbursementData.purpose = this.selectedReimbursementRequest.purpose;
    newreimbursementData.status = this.selectedReimbursementRequest.status;
    newreimbursementData.vehicleType = this.selectedReimbursementRequest.vehicleType;
    newreimbursementData.foodAllowanceType = this.selectedReimbursementRequest.foodAllowanceType;
    newreimbursementData.dateOfFood = this.selectedReimbursementRequest.dateOfFood;
    //  newreimbursementData.supportingDocument = this.selectedReimbursementRequest.supportingDocument;


    console.log('selectedReimbursementRequest Data ::::::::::::::::', this.selectedReimbursementRequest);
    const index = this.reimbursementRequests.findIndex(request => request.requestId === this.selectedReimbursementRequest.requestId);

    if (index !== -1) {
      // Update the travel request at the found index with the new data
      this.reimbursementRequests[index] = { ...this.selectedReimbursementRequest };

      try {
        console.log('Try Bhitare Data ::::::::::::::::');

        const response: any = await this.reimbursementService.updateReimbursementData(newreimbursementData).toPromise();

        if (response.serviceStatus === "Success") {
          this.openAlertMod2(template, "Success! Your data is updated successfully. !!");

          //alert("Success! Your data was updated successfully.");
          console.log('Updated Travel Request:', this.selectedReimbursementRequest);
          this.onGetReimbursementInfo();
          this.modalRef?.close();

        } else {
          console.error('Error updating reimbursement request:', response.serviceResponse);
          this.openAlertMod2(template, "There was an issue updating the data.. !!");

        }
      } catch (error) {
        console.error('Error during API call:', error);
        this.openAlertMod2(template, "An error occurred while updating the data. Please try again later. !!");
      }

    }
  }

  async deleteReimbursement(row: any, template: TemplateRef<any>) {

    if (this.isValidForm()) {
      this.reimbursementInfo = new MyReimbursement();
      let reimbursementData = new MyReimbursement();

      this.selectedReimbursementdataforDelete = { ...row };

      console.log('delete pain asichi reee :::::::::::::::::', this.selectedReimbursementdataforDelete)

      console.log('currentEmployeeInfo ::::::::::::::::', this.currentEmployeeInfo);
      reimbursementData.requestId = this.selectedReimbursementdataforDelete.requestId;

      // console.log('Form data for delete :::::::::::::::::::::', this.travelRequests.requestId);

      const response: any = await this.reimbursementService.revokeReimbursement(reimbursementData).toPromise();

      if (response.serviceStatus === "Success") {
        this.openAlertMod2(template, "Success! Your Data is deleted successfully .. !!");
        //alert("Success! Your Data is deleted successfully.");
        this.onGetReimbursementInfo();

      } else {
        console.error('Error fetching data:', response.serviceResponse);
      }
    }
  }



  async onGetEmployeeInfo() {
    this.domainSpecializationList = [];
    this.currentEmployeeInfo = new Employee();
    let currentEmp = new Employee();
    currentEmp.empId = this.currentUser.empId;
    currentEmp.isDraft = false;
    console.log("currentEmp :::::::::::::::::::::::: ", currentEmp);

    const response: any = await this.employeeService.getEmployeeByEmpId(currentEmp).toPromise();
    if (response.serviceStatus == "Success") {
      this.currentEmployeeInfo = response.serviceResponse;

      //console.log("currentEmployeeInfo : ", this.currentEmployeeInfo);
      //this.loadProfileImage(this.currentEmployeeInfo.imageBytes)

    } else {
      console.error(response.serviceResponse);
    }
    setTimeout(() => {
      this.currentEmployeeInfo.documentList && this.currentEmployeeInfo.documentList.forEach((doc, index) => {
        if (doc.documentBytes) {
          let preview = document.getElementById(`docPreview${index + 1}`);
          let objectURL = 'data:image/*;base64,' + doc.documentBytes;
          let src: string = this.sanitizer.sanitize(SecurityContext.RESOURCE_URL, this.sanitizer.bypassSecurityTrustResourceUrl(objectURL));
          preview.setAttribute('src', src);
        }
      });
    }, 500);
  }


  closeModal() {
    this.modalRef?.close();
  }

  cancelRequest() {
    this.modalRef2.close();
  }
  isSearchEnabled: boolean = false;
  filters: any = {};

  reimbursementActiveColumns: any[] = ['requestId', 'fullName', 'fromDate', 'toDate', 'dateOfFood', 'expenditureType', 'travelMode', 'distance', 'currency', 'amount', 'appliedBy', 'appliedOn', 'purpose'];
  onSearch(searchData) {
    if (this.isSearchEnabled == true) {
      this.filters = searchData;
      console.log("Updated Filter : ", this.filters);
    }
  }
  toggleSearch() {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
    }
  }

  name = 'ReimbursementReport.xlsx';
  exportToExcel(): void {

    const onlySpecificDataArr = this.reimbursementRequests.map(
      x => ({
        "Request Id": x.requestId,
        "Name": x.fullName,
        "From Date": x.fromDate,
        "To Date": x.toDate,
        "Fooding Date": x.dateOfFood,
        "Expenditure Type": x.expenditureType,
        "Travel Mode": x.travelMode,
        "Distance(in Kms)": x.distance,
        "Currency": x.currency,
        "Amount": x.amount,
        "Applied By": x.appliedBy,
        "Applied On": x.appliedOn,
        "Purpose Of Travel": x.purpose,
        "Current Approval Level": x.level,
        "Level 1 Approver Name": x.hodName,
        "Level 1 Approver Status": x.status,
        "Level 1 Approver Remarks": x.level1approverRemarks,
        "Level 2 Approver Name": x.level2approverName,
        "Level 2 Approver Status": x.level2approverStatus,
        "Level 2 Approver Remarks": x.level2approverRemarks,
        "Level 3 Approver Name": x.level3approverName,
        "Level 3 Approver Status": x.level3approverStatus,
        "Level 3 Approver Remarks": x.level3approverRemarks,
        "Final Status": x.finalStatus
      })
    )
    this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.name)
  }
}
