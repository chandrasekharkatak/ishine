import { Component, OnInit, SecurityContext, TemplateRef, ViewChild } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
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
async previewDocument(docIt:any){
  const response:any = await this.travelDesk.previewDocument(docIt).toPromise();
  if (response.serviceStatus === 'Success' && response.serviceResponse?.documentBytes) {
      this.selectedDocument = 'data:image/png;base64,' + response.serviceResponse.documentBytes;
    }
    else{
      
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

getFileType(url: string): string {
  const extension = url.split('.').pop()?.toLowerCase();
  if (extension === 'pdf') return 'pdf';
  if (['jpg', 'jpeg', 'png', 'gif'].includes(extension)) return 'image';
  return 'other';
}

}
