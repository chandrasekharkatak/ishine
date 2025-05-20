import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { Employee } from 'src/app/models/employee';
import { MyReimbursement } from 'src/app/models/reimbursement';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { ReimbursementService } from 'src/app/services/reimbursement.service';
import { TravelDeskService } from 'src/app/services/travel-desk.service';

@Component({
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
  constructor(
    private modalService: BsModalService,
     private sanitizer: DomSanitizer,
     private employeeService : EmployeeService,
     private authenticationService: AuthenticationService,
     private exportExcelService: ExportExcelService,
     private reimbursementService:ReimbursementService,
    private  travelDesk :TravelDeskService
  ) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x)}

  ngOnInit(): void {
    this.onGetReimbursementInfo();
    this.fetchAllInvoice();

  }

  async onGetReimbursementInfo() {
    if (this.isValidForm()) {

     this.reimbursementInfo = new MyReimbursement();
      let reimbursementData = new MyReimbursement();

      console.log('currentEmployeeInfo ::::::::::::::::',this.currentEmployeeInfo);
      reimbursementData.empId = this.currentUser.empId;  

      console.log('reimbursementData Data  ::::::::::::::::', reimbursementData);

      const response: any = await this.reimbursementService.fetchTotalReimbursementData(reimbursementData).toPromise();
      
      if (response.serviceStatus === "Success") {
        this.reimbursementRequests = response.serviceResponse;  
        console.log('Fetched Reimbursement Requests:', this.reimbursementRequests);
        this.selectedReimbursementRequest = this.reimbursementRequests;
      } else {
        console.error('Error fetching data:', response.serviceResponse);
      }
    }
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
  modalRef: BsModalRef = new BsModalRef();
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
    //this.onGetReimbursementInfo();
    location.reload();
  }


openEditModal(template: TemplateRef<any> ,row :any) {

  this.selectedReimbursementRequest = { ...row };

  console.log('editpain asichi re ::::::::::::::::::::',this.selectedReimbursementRequest);

  this.openAlertMod(template, "njvhv");

}

closeModal() {
  this.modalRef.hide();
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
      "Final Status": x.finalStatus
    })
  )
  this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.name)
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

objectKeys = Object.keys;
groupedInvoiceData: any = {};
invoiceDetails:boolean = false;
fetchAllInvoice() {
  this.invoiceDetails=!this.invoiceDetails;
  this.reimbursementService.fetchAllInvoice().pipe(first()).subscribe((response: any) => {
    if (response.serviceStatus === "Success") {
      const rawData = response.serviceResponse;

      // Group data by travelId
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
          docIdTrevel: item.docIdTrevel
        });
      });

      this.groupedInvoiceData = grouped;
      console.log("Grouped Invoice Data", this.groupedInvoiceData);
    }
  });
}


cancelRequestDocument() {
  this.modalRef.hide();

}

docUrl: string | null = null;
preview(template: TemplateRef<any>, id: any) {
  console.log(template,"template");

  this.docUrl = null; 
  this.modalRef = this.modalService.show(template, {
    class: 'modal-xl'
  });

  const payload = { docId: id }; 

  this.travelDesk.previewDocument(payload).pipe(first()).subscribe((response: any) => {
    if (response.serviceStatus === 'Success' && response.serviceResponse?.documentBytes) {
      this.docUrl = 'data:image/png;base64,' + response.serviceResponse.documentBytes;
    } else {
      // this.modalRef?.hide();
      // this.openAlertMod(template, 'Image not present');
    }
  });
}
}
