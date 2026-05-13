import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Sort } from '@angular/material/sort';
import { first } from 'rxjs/operators';
import { Employee } from 'src/app/models/employee';
import { MyReimbursement } from 'src/app/models/reimbursement';
import { TravelBased } from 'src/app/models/travelBasedReimbursement';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { ReimbursementService } from 'src/app/services/reimbursement.service';
import { TravelDeskService } from 'src/app/services/travel-desk.service';
import { ReimbursementTicketModalComponent } from '../reimbursement-ticket-modal/reimbursement-ticket-modal.component';

@Component({
  standalone: false,
  selector: 'app-total-reimbursementrequest',
  templateUrl: './total-reimbursementrequest.component.html',
  styleUrls: ['./total-reimbursementrequest.component.css']
})
export class TotalReimbursementrequestComponent implements OnInit {

  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;
  @ViewChild('editTravelRequestModal') modalElement: any;
  //travelDeskInfo: MyTravelDesk;
  reimbursementInfo: MyReimbursement;
  currentEmployeeInfo: Employee = new Employee();
  travelRequests: any = [];
  reimbursementRequests: any = [];
  selectedReimbursementRequest: any = [];
  selectedTravelRequest: any = [];
  selectedReimbursementdataforDelete: any = [];
  domainSpecializationList: any[];
  currentUser: any;
  alertMessage: any;
  level: number;
  reimbursementDashboard: any = null;
  financeTickets: any[] = [];
  selectedFinanceTicket: any = null;
  financeModalRef: NgbModalRef | null = null;

  // Finance tickets table UX
  isSearchEnabledFinanceTickets = false;
  financeTicketFilters: any = {};
  financeTicketActiveColumns: any[] = [
    'ticketNo',
    'fullName',
    'displayStatus',
    'workflowStage',
    'payableApprovedAmount',
    'submittedOn',
    'level1ApproverName',
    'level2ApproverName',
    'level3ApproverName'
  ];
  sortFinanceTicketColumn = '';
  sortFinanceTicketColumnType = '';
  sortFinanceTicketDirection = '';
  pageFinanceTickets = 1;
  constructor(
    private modalService: NgbModal,
     private sanitizer: DomSanitizer,
     private employeeService : EmployeeService,
     private authenticationService: AuthenticationService,
     private exportExcelService: ExportExcelService,
     private reimbursementService:ReimbursementService,
    private  travelDesk :TravelDeskService
  ) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x)}

  ngOnInit(): void {
    this.onGetReimbursementInfo();


  }

  async onGetReimbursementInfo() {
    if (this.isValidForm()) {

     this.reimbursementInfo = new MyReimbursement();
      let reimbursementData = new MyReimbursement();

      console.log('currentEmployeeInfo ::::::::::::::::',this.currentEmployeeInfo.departmentName
      );
      reimbursementData.empId = this.currentUser.empId;

      console.log('reimbursementData Data  ::::::::::::::::', reimbursementData);

      const response: any = await this.reimbursementService.fetchTotalReimbursementData(reimbursementData).toPromise();

      if (response.serviceStatus === "Success") {
        this.reimbursementRequests = response.serviceResponse;

        console.log('All reimbursement data:', this.reimbursementRequests);

        if (this.currentUser.departmentName === 'Accounts') {
          this.reimbursementRequests = this.reimbursementRequests.filter(
            (item: any) => item.finalStatus === 'Approved'
          );
          console.log('Filtered for Development + Approved:', this.reimbursementRequests);
        }

        this.selectedReimbursementRequest = this.reimbursementRequests;
      } else {
        console.error('Error fetching data:', response.serviceResponse);
      }

      try {
        const dash: any = await this.reimbursementService.fetchReimbursementDashboard({}).pipe(first()).toPromise();
        if (dash.serviceStatus === 'Success') {
          this.reimbursementDashboard = dash.serviceResponse;
        }
      } catch {
        this.reimbursementDashboard = null;
      }
      try {
        const ap: any = await this.reimbursementService.fetchReimbursementTicketsForApproval({
          empId: this.currentUser.empId,
          email: this.currentUser.email
        }).pipe(first()).toPromise();
        if (ap.serviceStatus === 'Success') {
          this.financeTickets = (ap.serviceResponse || []).filter((t: any) => t.workflowStage === 'PENDING_FINANCE');
        } else {
          this.financeTickets = [];
        }
      } catch {
        this.financeTickets = [];
      }
    }
  }

  toggleFinanceTicketSearch() {
    this.isSearchEnabledFinanceTickets = !this.isSearchEnabledFinanceTickets;
    if (!this.isSearchEnabledFinanceTickets) {
      this.financeTicketFilters = {};
    }
  }

  onFinanceTicketSearch(searchData: any) {
    if (this.isSearchEnabledFinanceTickets) {
      this.financeTicketFilters = searchData;
    }
  }

  sortFinanceTicketData(sort: Sort) {
    if (sort.active) {
      const sortParams: any[] = sort.active?.split('|');
      this.sortFinanceTicketColumn = sortParams[0];
      this.sortFinanceTicketColumnType = sortParams[1];
      this.sortFinanceTicketDirection = sort.direction;
    }
  }

  handleFinanceTicketPageChange(event: any) {
    this.pageFinanceTickets = event;
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

  isValidForm() {
    return true;
  }

  shouldShowAction(row: any): boolean {
    console.log('row data :::::::::::::',row);
    if (row.level === 1) {
      return row.status === 'Pending';
    } else if (row.level === 2) {
      return row.status === 'Approved' && row.level2approverStatus === 'Pending';
    } else if (row.level === 3) {
      return (
        row.status === 'Approved' &&
        row.level2approverStatus === 'Approved' &&
        row.level3approverStatus === 'Pending'
      );
    }
    return false;
  }
  //pagination

  page = 1;
  handlePageChange(event) {
      this.page = event;
  }
  //alertMessage: any;
  modalRef:NgbModalRef;
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef?.close();
    //this.onGetReimbursementInfo();
    location.reload();
  }


openEditModal(template: TemplateRef<any> ,row :any) {

  this.selectedReimbursementRequest = { ...row };

  console.log('editpain asichi re ::::::::::::::::::::',this.selectedReimbursementRequest);

  this.openAlertMod(template, "njvhv");

}

closeModal() {
  this.modalRef?.close();
  this.onGetReimbursementInfo();
}

name = 'TotalReimbursementReport.xlsx';
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
      "Final Status": x.finalStatus,
      "Reimbursement Status":x.reimbursementStatus?x.reimbursementStatus:'Pending'
    })
  )
  this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.name)
}

isSearchEnabled: boolean = false;
filters: any = {};


reimbursementActiveColumns: any[] = ['requestId', 'fullName', 'fromDate', 'toDate', 'expenditureType', 'travelMode', 'distance', 'currency', 'foodAllowanceType', 'dateOfFood', 'amount', 'appliedBy','appliedOn', 'purpose','level','hodName','status','level1approverRemarks','level2approverName','level2approverStatus','level2approverRemarks','level3approverName','level3approverStatus','level3approverRemarks'];
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

objectKeys = Object.keys;
groupedInvoiceData: any = {};
invoiceDetails:boolean = false;
// fetchAllInvoice1(){
//   this.invoiceDetails=!this.invoiceDetails;
// }
fetchAllInvoice1(){
  this.invoiceDetails=false;
  this.onGetReimbursementInfo();
}

Excell:any[]=[];
fetchAllInvoice() {
  this.invoiceDetails=true;
  this.reimbursementService.fetchAllInvoice().pipe(first()).subscribe((response: any) => {
    if (response.serviceStatus === "Success") {
      const rawData = response.serviceResponse;
      this.Excell=response.serviceResponse;

  console.log("Test",response.serviceResponse.sort((a, b) => b.travelId - a.travelId))

      const grouped: any = {};
      rawData.forEach((item: any) => {
        const travelId = item.travelId || 'unknown';
        if (!grouped[travelId]) {
          grouped[travelId] = {
            commonInfo: item,
            invoices: []
          };
        }
        grouped[travelId].invoices.push({
          invoiceNo: item.invoiceNo,
          amount: item.amount,
          docIdTrevel: item.docIdTrevel,
          reimbursementStatus:item.reimbursementStatus
        });
      });

      this.groupedInvoiceData = grouped;
      console.log("Grouped Invoice Data", this.groupedInvoiceData);
    }
  });
}


cancelRequestDocument() {
  this.modalRef?.close();
}
getMimeTypeFromBase64(base64: string): string {
  const header = atob(base64.slice(0, 20));
  if (header.startsWith('%PDF')) return 'application/pdf';
  if (header.startsWith('\x89PNG')) return 'image/png';
  if (header.startsWith('\xFF\xD8\xFF')) return 'image/jpeg';
  return 'application/octet-stream';
}
docUrl: string | null = null;
selectedDocument: any;

  async preview(template: TemplateRef<any>, id: any) {
  console.log(template,"template");

  this.selectedDocument = null;


  const payload = { docId: id };



    const response: any = await this.travelDesk.previewDocument(payload).toPromise();

    if (response.serviceStatus === 'Success' && response.serviceResponse?.documentBytes) {
      const base64Data = response.serviceResponse.documentBytes;
      const mimeType = this.getMimeTypeFromBase64(base64Data);

      if (mimeType === 'application/pdf') {
        const pdfUrl = `data:application/pdf;base64,${base64Data}`;
        this.selectedDocument = this.sanitizer.bypassSecurityTrustResourceUrl(pdfUrl);
        this.modalRef = this.modalService.open(template, {
          modalDialogClass: 'modal-xl'
        });
      } else if (mimeType.startsWith('image/')) {
        const imgUrl = `data:${mimeType};base64,${base64Data}`;
        this.selectedDocument = imgUrl;
        this.modalRef = this.modalService.open(template, {
          modalDialogClass: 'modal-xl'
        });
      } else {
        // Handle other file types: Download
        const link = document.createElement('a');
        link.href = `data:application/octet-stream;base64,${base64Data}`;
        link.download = 'document';
        link.click();
      }
    }else {
      this.openAlertMod(template, "No Document to dipslay");
    }

}



docList: any[] = [];
 openViewDocModule(template: TemplateRef<any>, requestId:any){
    this.alertMessage = null;
    this.getAllDocumentsThroughRequestId(requestId);
    if(this.docList != null && this.alertMessage == null){
      this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
    }
    else{
      this.openAlertMod(this.alertTemplate,this.alertMessage)
    }

  }


  async getAllDocumentsThroughRequestId(requestId:any) {
    const response:any = await this.reimbursementService.getAllDocumentsReimbursmentThroughRequestId(requestId).toPromise()
    if (response.serviceStatus === "Success") {
      this.docList = response.serviceResponse;
      console.log(this.docList,"this.docList")
    }
    else{
      this.docList = null;
      this.alertMessage = response.serviceMessage;
    }
  }
  viewSelectedDocument(doc: any) {
    console.log(doc,"doc");
    this.previewDocument(doc);
    }

    async previewDocument(docId: any) {
      const requestPayload = { docId: docId };
      const response: any = await this.reimbursementService.previewDocumentReimbursment(requestPayload).toPromise();

      if (response.serviceStatus === 'Success' && response.serviceResponse?.documentBytes) {
        const base64Data = response.serviceResponse.documentBytes;
        const mimeType = this.getMimeTypeFromBase64(base64Data);

        if (mimeType === 'application/pdf') {
          const pdfUrl = `data:application/pdf;base64,${base64Data}`;
          this.selectedDocument = this.sanitizer.bypassSecurityTrustResourceUrl(pdfUrl);
        } else if (mimeType.startsWith('image/')) {
          const imgUrl = `data:${mimeType};base64,${base64Data}`;
          this.selectedDocument = imgUrl; // image binding is safe by default
        } else {
          // Handle other file types: Download
          const link = document.createElement('a');
          link.href = `data:application/octet-stream;base64,${base64Data}`;
          link.download = 'document';
          link.click();
        }
      }
    }


    account1:TravelBased=new TravelBased();
    rejectPopUp(template:TemplateRef<any>,details:any){
      this.modalRef = this.modalService.open(template, {
        modalDialogClass: 'modal-sm'
      });
      this.account1.invoiceNo=details.invoiceNo;
      this.account1.travelId = details.travelId;
    }


    rejectPopUpForIndividualReimbursement(template:TemplateRef<any>,details:any){
      this.modalRef = this.modalService.open(template, {
        modalDialogClass: 'modal-sm'
      });

      this.account1.requestId = details.requestId;
    }
    account:TravelBased=new TravelBased();
    accountSubmit(template:TemplateRef<any>, details:any){
      this.account.invoiceNo=details.invoiceNo;
      this.account.travelId = details.travelId;
      this.account.rejectReason = details.rejectReason;

      // this.account.rejectReason = details.rejectReason;
      // this.account.reimbursementStatus = details.reimbursementStatus;
      // this.account.isValid = details.isValid;
      console.log("details",this.account);
      this.modalRef?.close();
      this.reimbursementService.updateInvoicesDetailsByAccountsTeam(this.account).pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus == "Success") {

               this.fetchAllInvoice();

              this.openAlertMod(template, "Reimbursment Status Updated !!");
            } else {

              this.openAlertMod(template, response.serviceResponse);
            }
          });
    }


    getApprovedTotal(invoices: any[]): number {
      if (!invoices || invoices.length === 0) return 0;

      return invoices
        .filter(inv => inv.reimbursementStatus === 'Approved')
        .reduce((sum, inv) => sum + (inv.amount || 0), 0);
    }

    allInvoicesApproved(invoices: any[]): boolean {
      if (!invoices || invoices.length === 0) return false;
      return invoices.every(inv => inv.reimbursementStatus === 'Approved');
    }

    markAsPaid(travelId:any,template:TemplateRef<any>): void {
      const travelId1 = {travelId:travelId};
      this.reimbursementService.markAsPaid(travelId1).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {

          this.openAlertMod(template, response.serviceResponse);
          this.fetchAllInvoice();
        } else {

          this.openAlertMod(template, response.serviceResponse);
        }
      });

      console.log("Marking as paid for Travel ID:", travelId);
      // You can call your service here to mark as paid
    }

    updateReimbursementDetailsByAccountsTeam(template:TemplateRef<any>, details:any){

      this.account.requestId = details.requestId;
      this.account.rejectReason = details.rejectReason;

      // this.account.rejectReason = details.rejectReason;
      // this.account.reimbursementStatus = details.reimbursementStatus;
      // this.account.isValid = details.isValid;
      console.log("details",this.account);
      this.modalRef?.close();
      this.reimbursementService.updateReimbursementDetailsByAccountsTeam(this.account).pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus == "Success") {

               this.onGetReimbursementInfo();

              this.openAlertMod(template, "Reimbursment Status Updated !!");
            } else {

              this.openAlertMod(template, response.serviceResponse);
            }
          });
    }

    isSearchEnabledTravel:boolean=false;
    reimbursementActiveColumns1: any[] = ['requestId', 'fullName', 'fromDate', 'toDate', 'expenditureType', 'travelMode', 'distance', 'currency', 'foodAllowanceType', 'dateOfFood', 'amount', 'appliedBy','appliedOn', 'purpose','level','hodName','status','level1approverRemarks','level2approverName','level2approverStatus','level2approverRemarks','level3approverName','level3approverStatus','level3approverRemarks'];
    onSearchTravel(searchData) {
      if (this.isSearchEnabledTravel == true) {
        this.filters = searchData;
        console.log("Updated Filter : ", this.filters);
      }
    }

    toggleSearchTravel() {
      this.isSearchEnabledTravel = !this.isSearchEnabledTravel;
      if (!this.isSearchEnabledTravel) {
        this.filters = {};
      }
    }





  openFinanceTicketModal(template: TemplateRef<any>, t: any) {
    this.selectedFinanceTicket = JSON.parse(JSON.stringify(t));
    this.selectedFinanceTicket._financeAction = 'PAID';
    this.selectedFinanceTicket._financeRemarks = '';
    this.financeModalRef = this.modalService.open(template, { size: 'lg', backdrop: 'static' });
  }

  async submitFinanceTicket(templateOk: TemplateRef<any>) {
    const t = this.selectedFinanceTicket;
    const act = t._financeAction;
    const remarks = t._financeRemarks;
    if (act === 'REJECTED' && (!remarks || !String(remarks).trim())) {
      this.openAlertMod(templateOk, 'Remarks required when rejecting.');
      return;
    }
    const body = {
      ticketId: t.ticketId,
      actorEmpId: this.currentUser.empId,
      actorEmail: this.currentUser.email,
      action: act,
      remarks: remarks || ''
    };
    const resp: any = await this.reimbursementService.processReimbursementTicketFinance(body).pipe(first()).toPromise();
    if (resp.serviceStatus === 'Success') {
      this.financeModalRef?.close();
      this.openAlertMod(templateOk, 'Finance action recorded.');
      await this.onGetReimbursementInfo();
    } else {
      this.openAlertMod(templateOk, resp.serviceError || 'Failed');
    }
  }

   name1= 'TravelBasedRequest.xlsx'
    exportToExcelTravel(){
      const onlySpecificDataArr = this.Excell.map(
        x => ({
          "Travel Id": x.travelId,
          "From Location": x.fromLocation,
          "To Location": x.toLocation,
          "From Date": x.fromDate,
          "To Date": x.toDate,
          "Travel Mode ": x.travelMode,
          "Travel Class": x.travelClass,
          "Invoice No": x.invoiceNo,
          "Amount": x.amount,
           "Reimbursement Status":x.reimbursementStatus?x.reimbursementStatus:'Pending'
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.name1)
    }
}
