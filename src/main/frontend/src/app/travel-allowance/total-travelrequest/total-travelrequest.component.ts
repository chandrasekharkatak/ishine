import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { saveAs } from 'file-saver';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { Employee } from 'src/app/models/employee';
import { MyTravelDesk } from 'src/app/models/travelDesk';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { TravelDeskService } from 'src/app/services/travel-desk.service';
import * as XLSX from 'xlsx';
@Component({
  selector: 'app-total-travelrequest',
  templateUrl: './total-travelrequest.component.html',
  styleUrls: ['./total-travelrequest.component.css']
})
export class TotalTravelrequestComponent implements OnInit {


  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;
  @ViewChild('editTravelRequestModal') modalElement: any;
  travelDeskInfo: MyTravelDesk;
  currentEmployeeInfo: Employee = new Employee();
  travelRequests: any = [];
  docList: any[] = [];
  selectedTravelRequest: any = [];
  selectedTraveldataforDelete: any = [];
  domainSpecializationList: any[];
  currentUser: any;
  selectedDocument: any;
  selectedDocumentType: any;

  constructor(private travelDesk: TravelDeskService,
    private modalService: BsModalService,
    private sanitizer: DomSanitizer,
    private employeeService: EmployeeService,
    private exportExcelService: ExportExcelService,
    private authenticationService: AuthenticationService,
  ) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x) }


  ngOnInit(): void {
    this.onGetTravelInfo();
  }



  async onGetTravelInfo() {
    if (this.isValidForm()) {
      this.travelDeskInfo = new MyTravelDesk();
      let travelData = new MyTravelDesk();

      console.log('currentEmployeeInfo ::::::::::::::::', this.currentEmployeeInfo);
      travelData.employeeId = this.currentUser.empId;

      console.log('Form Data:', travelData);

      const response: any = await this.travelDesk.totalTravelData(travelData).toPromise();

      if (response.serviceStatus === "Success") {
        this.travelRequests = response.serviceResponse;

        if (this.currentUser.departmentName === 'Admin') {
          this.travelRequests = response.serviceResponse.filter((details: any) => details.finalStatus === 'Approved')
        }

        console.log('Fetched Travel Requests:', this.travelRequests);
        this.selectedTravelRequest = this.travelRequests;
      } else {
        console.error('Error fetching data:', response.serviceResponse);
      }
    }
  }


  isValidForm() {
    return true;
  }
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  openViewDocModule(template: TemplateRef<any>, requestId: any) {
    this.alertMessage = null;
    this.getAllDocumentsThroughRequestId(requestId);
    if (this.docList != null && this.alertMessage == null) {
      this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
    }
    else {
      this.openAlertMod(this.alertTemplate, this.alertMessage)
    }

  }

  async getAllDocumentsThroughRequestId(requestId: any) {
    const response: any = await this.travelDesk.getAllDocumentsThroughRequestId(requestId).toPromise()
    if (response.serviceStatus === "Success") {
      this.docList = response.serviceResponse;
      
    }
    else {
      this.docList = null;
      this.alertMessage = response.serviceMessage;
    }
  }
  async previewDocument(docId: any) {
    const requestPayload = { docId: docId };
    const response: any = await this.travelDesk.previewDocument(requestPayload).toPromise();

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



  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  cancelRequest() {
    this.modalRef.hide();
    this.onGetTravelInfo();
  }

  closeModal() {
    this.modalRef.hide();
  }

  viewSelectedDocument(doc: any) {
    console.log(doc, "doc");
    this.previewDocument(doc);
  }
  getMimeTypeFromBase64(base64: string): string {
    const header = atob(base64.slice(0, 20));
    if (header.startsWith('%PDF')) return 'application/pdf';
    if (header.startsWith('\x89PNG')) return 'image/png';
    if (header.startsWith('\xFF\xD8\xFF')) return 'image/jpeg';
    return 'application/octet-stream';
  }


  selectedFileName: string | null = null;
  selectedFile: File | null = null;
  maxFileSizeMB = 3;

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;

    if (input.files && input.files.length > 0) {
      const file = input.files[0];


      if (file.size > this.maxFileSizeMB * 1024 * 1024) {
        alert('File size should not exceed 3 MB.');
        input.value = '';
        this.selectedFileName = null;
        this.selectedFile = null;
        return;
      }

      this.selectedFileName = file.name;
      this.selectedFile = file;
    } else {
      this.selectedFileName = null;
      this.selectedFile = null;
    }
  }

  ticketId: any;
  uploadTicket(template: TemplateRef<any>, ticketId: any) {
    this.modalRef = this.modalService.show(template, {
      class: 'modal-sm'
    });
    this.ticketId = ticketId;
    console.log("ticket", ticketId);
  }

  async uploadTicketByAdmin(fileObj: any, template: TemplateRef<any>) {
    let uploadResponse: any;

    const fileFormData = new FormData();
    fileFormData.append('file', this.selectedFile);
    fileFormData.append("displayName", this.selectedFile.name);
    fileFormData.append("uploadedBy", this.currentUser.empId);
    fileFormData.append("requestId", this.ticketId);

    uploadResponse = await this.travelDesk.uploadTicket(fileFormData)
      .pipe(first())
      .toPromise();

    if (uploadResponse.serviceStatus !== "Success") {
      this.onGetTravelInfo();
      this.openAlertMod(template, `File upload failed: ${uploadResponse.serviceResponse}`);
      return;
    } else {
      this.modalRef.hide();
      // this.openAlertMod(template, `File upload failed: ${uploadResponse.serviceResponse}`);
    }
  }




  exportToExcel(id: any): void {
    let exportToExcelTeamfile = id + ".xlsx";
    const table = document.getElementById('' + id); // Get table by ID
    if (!table) {
      console.error('Table not found');
      return;
    }

    const worksheet: XLSX.WorkSheet = XLSX.utils.table_to_sheet(table); // Convert table to worksheet
    const workbook: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Time Sheet');

    const excelBuffer: any = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    const data: Blob = new Blob([excelBuffer], { type: 'application/octet-stream' });

    saveAs(data, exportToExcelTeamfile);
  }


  isSearchEnabled: boolean = false;
  filters: any = {};

  travelActiveColumns: any[] = ['requestId', 'name', 'requestType', 'fromDate', 'toDate', 'hotelCategory', 'cityCategory', 'city', 'fromLocation', 'toLocation', 'empId', 'appliedOn', 'purpose', 'level', 'hodName', 'Status', 'level2approverName', 'level2approverStatus', ''];
  onSearch(searchData) {
    if (this.isSearchEnabled == true) {
      this.filters = searchData;
      console.log("Updated Filter : ", this.travelRequests);
    }
  }
  toggleSearch() {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
    }
  }



  name = 'TravelReport.xlsx';
  exportToExcelFullData(): void {

    const onlySpecificDataArr = this.travelRequests.map(
      x => ({

        "Request Id": x.requestId,
        "Name": x.name,
        "From Date": x.fromDate,
        "To Date": x.toDate,
        "Hotel Category": x.hotelCategory,
        "City Category": x.cityCategory,
        "City Name": x.city,
        "From Location": x.fromLocation,
        "To Location": x.toLocation,
        "Applied By": x.empId,
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
        "Ticket Status": x.ticketDocId ? 'Uploaded' : 'Pending'

      })
    )
    this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.name)
  }

}
