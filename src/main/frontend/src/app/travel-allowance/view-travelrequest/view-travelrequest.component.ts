import { Component, OnInit, SecurityContext, TemplateRef, ViewChild } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs/operators';
import { Employee } from 'src/app/models/employee';
import { MyTravelDesk } from 'src/app/models/travelDesk';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { TravelDeskService } from 'src/app/services/travel-desk.service';
declare var $: any; // Import jQuery if it's being used for DOM manipulation

@Component({
  standalone: false,
  selector: 'app-view-travelrequest',
  templateUrl: './view-travelrequest.component.html',
  styleUrls: ['./view-travelrequest.component.css']
})
export class ViewTravelrequestComponent implements OnInit {

  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;
  @ViewChild('editTravelRequestModal') modalElement: any;
  travelDeskInfo: MyTravelDesk;
  currentEmployeeInfo: Employee = new Employee();
  travelRequests: any = [];
  selectedTravelRequest: any = [];
  selectedTraveldataforDelete: any = [];
  domainSpecializationList: any[];
  currentUser: any;
  alertMessage: any;
  invoiceDetails: boolean = false;

  constructor(private travelDesk: TravelDeskService,
    private modalService: NgbModal,
    private sanitizer: DomSanitizer,
    private exportExcelService: ExportExcelService,
    private employeeService: EmployeeService,
    private authenticationService: AuthenticationService,
  ) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x) }

  ngOnInit(): void {
    this.onGetEmployeeInfo();
    this.onGetTravelInfo();
  }

  async onGetTravelInfo() {
    if (this.isValidForm()) {
      this.travelDeskInfo = new MyTravelDesk();
      let travelData = new MyTravelDesk();

      console.log('currentEmployeeInfo ::::::::::::::::', this.currentEmployeeInfo);
      travelData.employeeId = this.currentUser.empId;

      console.log('Form Data:', travelData);

      const response: any = await this.travelDesk.fetchTravelData(travelData).toPromise();

      if (response.serviceStatus === "Success") {
        this.travelRequests = response.serviceResponse;
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
  //pagination

  page = 1;
  handlePageChange(event) {
    this.page = event;
  }
  //alertMessage: any;
  modalRef:NgbModalRef;
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-lg' });
    this.alertMessage = message;
  }
  openEditModal(template: TemplateRef<any>, row: any) {

    this.selectedTravelRequest = { ...row };

    this.selectedTravelRequest.fromDate = this.formatDate(this.selectedTravelRequest.fromDate);
    this.selectedTravelRequest.toDate = this.formatDate(this.selectedTravelRequest.toDate);
    this.selectedTravelRequest.appliedOn = this.formatDate(this.selectedTravelRequest.appliedOn);

    console.log('editpain asichi re ::::::::::::::::::::', this.selectedTravelRequest);

    this.openAlertMod(template, "");

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



  async updateTravelRequest(template: TemplateRef<any>) {

    this.travelDeskInfo = new MyTravelDesk();
    let travelData = new MyTravelDesk();

    this.travelDeskInfo = new MyTravelDesk();
    let newtravelData = new MyTravelDesk();
    newtravelData.requestId = this.selectedTravelRequest.requestId;
    newtravelData.employeeId = this.selectedTravelRequest.empId;
    newtravelData.fullName = this.selectedTravelRequest.name;
    newtravelData.email = this.selectedTravelRequest.email;
    newtravelData.departmentName = this.selectedTravelRequest.department;
    newtravelData.designationName = this.selectedTravelRequest.designationName;
    newtravelData.mobileNo = this.selectedTravelRequest.mobileNo;
    newtravelData.managerName = this.selectedTravelRequest.managerName;
    newtravelData.associatedTravelRequest = this.selectedTravelRequest.requestType;
    newtravelData.travelMode = this.selectedTravelRequest.travelMode;
    newtravelData.travelClass = this.selectedTravelRequest.travelClass;
    newtravelData.fromDate = this.selectedTravelRequest.fromDate;
    newtravelData.toDate = this.selectedTravelRequest.toDate;
    newtravelData.fromLocation = this.selectedTravelRequest.fromLocation;
    newtravelData.toLocation = this.selectedTravelRequest.toLocation;
    newtravelData.purposeOfTravel = this.selectedTravelRequest.purpose;
    newtravelData.levelOneApprover = this.selectedTravelRequest.approver1;
    newtravelData.level2Approver = this.selectedTravelRequest.approver2;
    newtravelData.supportingDocument = this.selectedTravelRequest.supportingDocument;
    newtravelData.hotelCategory = this.selectedTravelRequest.hotelCategory;
    newtravelData.cityCategory = this.selectedTravelRequest.cityCategory;


    console.log('selectedTravelRequest Data ::::::::::::::::', this.selectedTravelRequest);
    const index = this.travelRequests.findIndex(request => request.requestId === this.selectedTravelRequest.requestId);

    if (index !== -1) {
      // Update the travel request at the found index with the new data
      this.travelRequests[index] = { ...this.selectedTravelRequest };

      try {
        const response: any = await this.travelDesk.updateTravelData(newtravelData).toPromise();

        if (response.serviceStatus === "Success") {
          this.modalRef.close();

          this.openAlertMod(template, "Success! Your data was updated successfully. !!");

          // alert("Success! Your data was updated successfully.");
          console.log('Updated Travel Request:', this.selectedTravelRequest);

        } else {
          console.error('Error updating travel request:', response.serviceResponse);
          this.openAlertMod(template, "There was an issue updating the data. !!");
        }
      } catch (error) {
        console.error('Error during API call:', error);
        this.openAlertMod(template, "An error occurred while updating the data. Please try again later.");
      }

    }
  }

  async deleteTraveldata(row: any, template: TemplateRef<any>) {
    if (this.isValidForm()) {
      this.travelDeskInfo = new MyTravelDesk();
      let travelData = new MyTravelDesk();

      this.selectedTraveldataforDelete = { ...row };

      console.log('delete pain asichi reee :::::::::::::::::', this.selectedTraveldataforDelete)

      console.log('currentEmployeeInfo ::::::::::::::::', this.currentEmployeeInfo);
      travelData.requestId = this.selectedTraveldataforDelete.requestId;

      // console.log('Form data for delete :::::::::::::::::::::', this.travelRequests.requestId);

      const response: any = await this.travelDesk.revokeTravel(travelData).toPromise();

      if (response.serviceStatus === "Success") {
        //this.openAlertMod( "Success! Your Data is deleted successfully. !!");

        // alert("Success! Your Data is deleted successfully.");
        this.openAlertMod(template, "Success! Your data is deleted successfully. !!");
        this.onGetTravelInfo();

      } else {
        console.error('Error fetching data:', response.serviceResponse);
      }
    }
  }
  cancelRequest() {
    this.modalRef.close();
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
    this.modalRef.close();
  }

  isSearchEnabled: boolean = false;
  filters: any = {};

  travelActiveColumns: any[] = ['requestId', 'name', 'requestType', 'fromDate', 'toDate', 'hotelCategory', 'cityCategory', 'city', 'fromLocation', 'toLocation', 'empId', 'appliedOn', 'purpose'];
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
  name = 'TravelReport.xlsx';
  exportToExcel(): void {

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
        "Final Status": x.finalStatus
      })
    )
    this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.name)
  }

  travelId: any;
  serialNo: any;
  docIdAgainstRejection: any;
  reimbursmentModel(template: TemplateRef<any>, row: any) {

    this.selectedTravelRequest = { ...row };

    this.selectedTravelRequest.fromDate = this.formatDate(this.selectedTravelRequest.fromDate);
    this.selectedTravelRequest.toDate = this.formatDate(this.selectedTravelRequest.toDate);
    this.selectedTravelRequest.appliedOn = this.formatDate(this.selectedTravelRequest.appliedOn);
    this.travelId = row.requestId;
    console.log('editpain asichi re ::::::::::::::::::::', this.selectedTravelRequest);

    this.openAlertMod(template, "");

  }
  reimbursmentModelForRejected(template: TemplateRef<any>, fromDate: any, toDate: any, travelId: any, invoice: any) {
    this.selectedTravelRequest.fromDate = this.formatDate(fromDate);
    this.selectedTravelRequest.toDate = this.formatDate(toDate);
    this.travelId = travelId;
    this.invoices[0].invoiceNo = invoice.invoiceNo;
    this.invoices[0].invoiceDate = this.formatDate(invoice.invoiceDate);
    this.invoices[0].amount = invoice.amount;
    this.docIdAgainstRejection = invoice.docIdTrevel;
    this.serialNo = invoice.serialNo;
    console.log("test2", this.formatDate(invoice.invoiceDate), invoice.invoiceDate);
    this.openAlertMod(template, "");
  }
  restrictingAlphaAndCharacter(event) {
    const k = event.charCode;
    if (k >= 48 && k <= 57) {
      return true;
    }
    return false;
  }



  invoices = [
    {
      invoiceNo: '', invoiceDate: '', amount: null, file1: null, travelId: null,
      uploadedBy: null
    }
  ];

  addInvoiceRow() {
    this.invoices.push({
      invoiceNo: '', invoiceDate: '', amount: null, file1: null, travelId: null,
      uploadedBy: null
    });
  }

  removeInvoiceRow(index: number) {
    this.invoices.splice(index, 1);
  }

  onInvoiceFileChange(event: any, index: number, template: TemplateRef<any>) {
    const file = event.target.files[0];

    if (file) {

      // if (!file.type.startsWith('image/')) {
      //   this.openAlertMod1(template, 'Only image files are allowed');
      //   event.target.value = '';
      //   return;
      // }


      const maxSizeInBytes = 1 * 1024 * 1024;
      if (file.size > maxSizeInBytes) {
        this.openAlertMod1(template, 'Image size should not exceed 1MB');
        event.target.value = '';
        return;
      }


      this.invoices[index].file1 = file;
    }
  }



  isInvoiceRowValid(invoice: any, index: number): boolean {
    const invoiceNo = invoice.invoiceNo?.trim();

    if (
      !invoiceNo ||
      !invoice.invoiceDate ||
      invoice.amount === null ||
      invoice.amount === undefined ||
      !invoice.file1
    ) {
      return false;
    }

    // Check for duplicate invoiceNo
    const isDuplicate = this.invoices.some((inv, i) => i !== index && inv.invoiceNo?.trim() === invoiceNo);

    return !isDuplicate;
  }

  getFileDetails() {
    this.invoices.forEach(async (invoice, index) => {
      if (invoice.file1) {

        try {
          const fileFormData = new FormData();
          fileFormData.append('file', invoice.file1);
          fileFormData.append("displayName", invoice.file1.name);
          fileFormData.append("uploadedBy", this.currentEmployeeInfo.empId);
          fileFormData.append("invoiceNo", invoice.invoiceNo);

          const uploadResponse: any = await this.travelDesk.uploadFileTravelBased(fileFormData).pipe(first()).toPromise();

          if (uploadResponse.serviceStatus === "Fail") {
            // this.openAlertMod(template, `Error found: ${uploadResponse.serviceResponse}`);
            return;
          } else if (uploadResponse.serviceStatus !== "Success") {
            // this.openAlertMod(template, uploadResponse.serviceResponse || "Unexpected file upload response.");
            return;
          }



        } catch (error) {
          // console.error("Upload failed for file", fileObj.file.name, error);
          // this.openAlertMod(template, `File upload failed: ${error.message || error}`);
          return;
        }

        console.log(`File details for Invoice #${index + 1}:`);
        console.log("invoice number", invoice.invoiceNo)
        console.log('File Name:', invoice.file1.name);      // Get the file name
        console.log('File Size:', invoice.file1.size);      // Get the file size in bytes
        console.log('File Type:', invoice.file1.type);      // Get the file type (MIME type)
      } else {
        console.log(`No file uploaded for Invoice #${index + 1}`);
      }
    });
  }
  modalRef1:NgbModalRef;
  openAlertMod1(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }
  submitInvoice(template: TemplateRef<any>) {
    const travelId = this.travelId;
    const uploadedBy = this.currentUser.empId;
    console.log("Testing", travelId);
    const cleanedInvoices = this.invoices.map(invoice => ({
      ...invoice,
      travelId: travelId,
      uploadedBy: uploadedBy,
      fileName: invoice.file1 || null

    }));

    const invoiceNos = cleanedInvoices.map(inv => inv.invoiceNo?.trim());
    const duplicateInvoiceNos = invoiceNos.filter((no, idx) => no && invoiceNos.indexOf(no) !== idx);

    if (duplicateInvoiceNos.length > 0) {
      this.openAlertMod1(template, "Duplicate invoice number(s) found. Please ensure all invoice numbers are unique.");
      return;
    }

    const hasHighAmountInvoice = cleanedInvoices.some(invoice => invoice.amount > 10000);

    if (hasHighAmountInvoice) {
      this.openAlertMod1(template, "The maximum amount per invoice should not exceed five digits.");
      return;

    }
    const hasInvalidInvoice = cleanedInvoices.some(invoice =>
      !invoice.invoiceNo ||
      !invoice.invoiceDate ||
      invoice.amount == null ||
      !invoice.travelId ||
      !invoice.uploadedBy ||
      !invoice.fileName
    );
    console.log("Testing", cleanedInvoices);
    if (hasInvalidInvoice) {

      this.openAlertMod1(template, "Please fill all required invoice fields before submitting.");
      return;
    }


    this.travelDesk.submitReimbursmentBasedOnTravelRequest(cleanedInvoices).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.modalRef.close();
        this.getFileDetails();
        this.invoices = [];
        this.addInvoiceRow();
        this.openAlertMod1(template, response.serviceResponse);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  checkInvoiceNumberPresentorNot(template: TemplateRef<any>, invoiceNo) {
    const invoice = {
      invoiceNo: invoiceNo,

    };
    console.log("Test", this.invoices);
    this.travelDesk.checkInvoiceNumberPresentorNot(invoice).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.invoices = [];
        this.addInvoiceRow()
        this.openAlertMod1(template, response.serviceResponse);
      }
    });
  }

  isAfterToDate(toDateStr: string): boolean {
    const toDate = new Date(toDateStr);
    const today = new Date();

    // Remove time for comparison
    toDate.setHours(0, 0, 0, 0);
    today.setHours(0, 0, 0, 0);

    return today > toDate; // Show only if today is after toDate
  }

  isBeforeOrOnFromDate(fromDateStr: string): boolean {
    const fromDate = new Date(fromDateStr);
    const today = new Date();

    // Remove time part for accurate comparison
    fromDate.setHours(0, 0, 0, 0);
    today.setHours(0, 0, 0, 0);

    return today <= fromDate; // Show button if today is before or on fromDate
  }

  downLoadTicket(template: TemplateRef<any>, ticketDocId: any) {
    const det = { docId: ticketDocId };

    this.travelDesk.previewDocument(det).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        const base64String = response.serviceResponse.documentBytes;
        const fullFileName = response.serviceResponse.ticketFileName || 'default.xlsx';
        const fileName = fullFileName.substring(fullFileName.indexOf('_') + 1);

        const contentType = response.serviceResponse.contentType || 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';

        const byteCharacters = atob(base64String);
        const byteNumbers = new Array(byteCharacters.length);
        for (let i = 0; i < byteCharacters.length; i++) {
          byteNumbers[i] = byteCharacters.charCodeAt(i);
        }

        const byteArray = new Uint8Array(byteNumbers);
        const blob = new Blob([byteArray], { type: contentType });
        const url = window.URL.createObjectURL(blob);

        const a = document.createElement('a');
        a.href = url;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);

      } else {
        this.openAlertMod(template, 'File download failed: ' + "Ticket Not Generated Yet");
      }
    });
  }

  fetchAllInvoice() {
    this.invoiceDetails = !this.invoiceDetails;
  }


  fetchAllInvoice1() {
    this.invoiceDetails = !this.invoiceDetails;
  }
  objectKeys = Object.keys;
  groupedInvoiceData: any = {};
  getAllInvoicesByEmpId(template: TemplateRef<any>, userId: any) {
    this.invoiceDetails = !this.invoiceDetails;
    const invoice = {
      uploadedBy: userId,

    };
    console.log("Test", this.invoices);
    this.travelDesk.getAllInvoicesByEmpId(invoice).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        const rawData = response.serviceResponse;

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
            invoiceDate: item.invoiceDate,
            docIdTrevel: item.docIdTrevel,
            serialNo: item.serialNo,
            reimbursementStatus: item.reimbursementStatus
          });
        });

        this.groupedInvoiceData = grouped;

      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  cancelRequestDocument() {
    this.modalRef.close();
  }
  getMimeTypeFromBase64(base64: string): string {
    const header = atob(base64.slice(0, 20));
    if (header.startsWith('%PDF')) return 'application/pdf';
    if (header.startsWith('\x89PNG')) return 'image/png';
    if (header.startsWith('\xFF\xD8\xFF')) return 'image/jpeg';
    return 'application/octet-stream';
  }
  selectedDocument: any;

  async preview(template: TemplateRef<any>, id: any) {
    console.log(template, "template");

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
    }
    else {
      this.openAlertMod(template, "No Document to dipslay");
    }

  }



  updateReimbursmentBasedOnTravelRequest(template: TemplateRef<any>) {
    const travelId = this.travelId;
    const uploadedBy = this.currentUser.empId;
    const serialNo = this.serialNo;
    const invoice = this.invoices[0];
    const docId = this.docIdAgainstRejection;
    console.log("Testing", travelId);
    const cleanedInvoice = {
      ...invoice,
      travelId: travelId,
      uploadedBy: uploadedBy,
      serialNo: serialNo,
      docId: docId
    };
    console.log("Testing", invoice);
    const trimmedInvoiceNo = cleanedInvoice.invoiceNo?.trim();

    // Duplicate check (redundant if handling only one invoice, but kept for safety)
    if (!trimmedInvoiceNo) {
      this.openAlertMod1(template, "Invoice number is required.");
      return;
    }

    let missingFields: string[] = [];

    if (!cleanedInvoice.invoiceNo) {
      missingFields.push("Invoice Number");
    }
    if (!cleanedInvoice.invoiceDate) {
      missingFields.push("Invoice Date");
    }
    if (cleanedInvoice.amount == null) {
      missingFields.push("Amount");
    }
    if (!cleanedInvoice.travelId) {
      missingFields.push("Travel ID");
    }
    if (!cleanedInvoice.uploadedBy) {
      missingFields.push("Uploaded By");
    }


    if (missingFields.length > 0) {
      const message = "Please fill the following field(s) before submitting: " + missingFields.join(", ");
      this.openAlertMod1(template, message);
      return;
    }

    this.travelDesk.updateReimbursmentBasedOnTravelRequest(cleanedInvoice).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.getFileDetails();
        this.invoices = [];
        this.addInvoiceRow();
        this.modalRef.close();
        this.updateUploadedFile();
        this.getAllInvoicesByEmpId(template, this.currentUser.EmpId);
        this.openAlertMod1(template, response.serviceResponse);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
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

  async updateUploadedFile() {
    // this.invoices.forEach(async (invoice, index) => {
    //   if (invoice.file1) {

    try {
      const fileFormData = new FormData();
      fileFormData.append('file', this.selectedFile);
      fileFormData.append("displayName", this.selectedFile.name);
      fileFormData.append("uploadedBy", this.currentUser.empId);

      fileFormData.append("docId", this.docIdAgainstRejection);

      const uploadResponse: any = await this.travelDesk.updateUploadedFile(fileFormData).pipe(first()).toPromise();

      if (uploadResponse.serviceStatus === "Fail") {
        // this.openAlertMod(template, `Error found: ${uploadResponse.serviceResponse}`);
        return;
      } else if (uploadResponse.serviceStatus !== "Success") {
        // this.openAlertMod(template, uploadResponse.serviceResponse || "Unexpected file upload response.");
        return;
      }



    } catch (error) {
      // console.error("Upload failed for file", fileObj.file.name, error);
      // this.openAlertMod(template, `File upload failed: ${error.message || error}`);
      return;
    }


  }


checkInvoiceNumberAgainstResubmit(template: TemplateRef<any>, invoiceNo) {
    const invoice = {
      invoiceNo: invoiceNo,
      serialNo:this.serialNo
    };
    console.log("Test", this.invoices);
    this.travelDesk.checkInvoiceNumberAgainstResubmit(invoice).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.invoices = [];
        this.addInvoiceRow()
        this.openAlertMod1(template, response.serviceResponse);
      }
    });
  }
}
