import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { Employee } from 'src/app/models/employee';
import { MyTravelDesk } from 'src/app/models/travelDesk';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { TravelDeskService } from 'src/app/services/travel-desk.service';

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
    private employeeService : EmployeeService,
    private authenticationService: AuthenticationService,
  ) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x) }


  ngOnInit(): void {
    this.onGetTravelInfo();
  }



  async onGetTravelInfo() {
    if (this.isValidForm()) {
      this.travelDeskInfo = new MyTravelDesk();
      let travelData = new MyTravelDesk();

      console.log('currentEmployeeInfo ::::::::::::::::',this.currentEmployeeInfo);
      travelData.employeeId = this.currentUser.empId;  

      console.log('Form Data:', travelData);

      const response: any = await this.travelDesk.totalTravelData(travelData).toPromise();
      
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
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  openViewDocModule(template: TemplateRef<any>, requestId:any){
    this.alertMessage = null;
    this.getAllDocumentsThroughRequestId(requestId);
    if(this.docList != null && this.alertMessage == null){
      this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
    }
    else{
      this.openAlertMod(this.alertTemplate,this.alertMessage)
    }
     
  }
  
  async getAllDocumentsThroughRequestId(requestId:any) {
    const response:any = await this.travelDesk.getAllDocumentsThroughRequestId(requestId).toPromise()
    if (response.serviceStatus === "Success") {
      this.docList = response.serviceResponse;
      console.log(this.docList,"this.docList")
    }
    else{
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
console.log(doc,"doc");
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
 
 ticketId:any;
 uploadTicket(template:TemplateRef<any>,ticketId:any){
  this.modalRef = this.modalService.show(template, {
    class: 'modal-sm'
  });
  this.ticketId=ticketId;
  console.log("ticket",ticketId);
 }

 async uploadTicketByAdmin(fileObj:any,template:TemplateRef<any>){
  let uploadResponse: any;

              const fileFormData = new FormData();
              fileFormData.append('file', this.selectedFile);
              fileFormData.append("displayName", this.selectedFile.name);
              fileFormData.append("uploadedBy", this.currentUser.empId);
              fileFormData.append("requestId",this.ticketId);
        
              uploadResponse = await this.travelDesk.uploadTicket(fileFormData)
                .pipe(first())
                .toPromise();
        
              if (uploadResponse.serviceStatus !== "Success") {
                this.openAlertMod(template, `File upload failed: ${uploadResponse.serviceResponse}`);
                return;
              }else{
                this.modalRef.hide();
                // this.openAlertMod(template, `File upload failed: ${uploadResponse.serviceResponse}`);
              }
 }

}
