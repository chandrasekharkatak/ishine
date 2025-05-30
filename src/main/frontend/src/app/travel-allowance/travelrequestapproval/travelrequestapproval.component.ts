import { Component, OnInit, SecurityContext, TemplateRef, ViewChild } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { Employee } from 'src/app/models/employee';
import { MyTravelDesk } from 'src/app/models/travelDesk';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { TravelDeskService } from 'src/app/services/travel-desk.service';

@Component({
  selector: 'app-travelrequestapproval',
  templateUrl: './travelrequestapproval.component.html',
  styleUrls: ['./travelrequestapproval.component.css']
})
export class TravelrequestapprovalComponent implements OnInit {
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

  constructor(private travelDesk: TravelDeskService,
    private modalService: BsModalService,
    private sanitizer: DomSanitizer,
    private employeeService : EmployeeService,
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

      console.log('currentEmployeeInfo ::::::::::::::::',this.currentEmployeeInfo);
      travelData.employeeId = this.currentUser.empId;  

      console.log('Form Data:', travelData);

      const response: any = await this.travelDesk.fetchTravelDataForApproval(travelData).toPromise();
      
      if (response.serviceStatus === "Success") {
        this.travelRequests = response.serviceResponse.sort((a, b) => b.requestId - a.requestId);

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
  modalRef1: BsModalRef = new BsModalRef();

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }
  openAlertMod1(template: TemplateRef<any>, message: any) {
    this.modalRef1 = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }
  openEditModal(template: TemplateRef<any> ,row :any) {

    this.selectedTravelRequest = { ...row };

    console.log('editpain asichi re ::::::::::::::::::::',this.selectedTravelRequest);

    this.openAlertMod(template, "njvhv");

  }

  

  async updateTravelRequest() {

      this.travelDeskInfo = new MyTravelDesk();
      let travelData = new MyTravelDesk();

      this.travelDeskInfo = new MyTravelDesk();
      let newtravelData = new MyTravelDesk();
      newtravelData.requestId=this.selectedTravelRequest.requestId;
      newtravelData.employeeId = this.selectedTravelRequest.empId;
      newtravelData.fullName = this.selectedTravelRequest.name;
      newtravelData.email =this.selectedTravelRequest.email;
      newtravelData.departmentName=this.selectedTravelRequest.department;
      newtravelData.designationName=this.selectedTravelRequest.designationName;
      newtravelData.mobileNo=this.selectedTravelRequest.mobileNo;
      newtravelData.managerName=this.selectedTravelRequest.managerName;
      newtravelData.associatedTravelRequest = this.selectedTravelRequest.requestType;
      newtravelData.travelMode= this.selectedTravelRequest.travelMode ;
      newtravelData.travelClass = this.selectedTravelRequest.travelClass;
      newtravelData.fromDate =this.selectedTravelRequest.fromDate;
      newtravelData.toDate = this.selectedTravelRequest.toDate;
      newtravelData.fromLocation = this.selectedTravelRequest.fromLocation;
      newtravelData.toLocation =this.selectedTravelRequest.toLocation;
      newtravelData.purposeOfTravel = this.selectedTravelRequest.purpose;
      newtravelData.supportingDocument = this.selectedTravelRequest.supportingDocument;


      console.log('Form Data:', travelData);
       const index = this.travelRequests.findIndex(request => request.requestId === this.selectedTravelRequest.requestId);

    if (index !== -1) {
      // Update the travel request at the found index with the new data
      this.travelRequests[index] = { ...this.selectedTravelRequest };

      try {
        const response: any = await this.travelDesk.updateTravelData(newtravelData).toPromise();
  
        if (response.serviceStatus === "Success") {
          alert("Success! Your data was updated successfully.");
          console.log('Updated Travel Request:', this.selectedTravelRequest);
          this.modalRef.hide();
        } else {
          console.error('Error updating travel request:', response.serviceResponse);
          alert('There was an issue updating the data.');
        }
      } catch (error) {
        console.error('Error during API call:', error);
        alert('An error occurred while updating the data. Please try again later.');
      }
 
    }
  }

   //pagination

   page = 1;
   handlePageChange(event) {
       this.page = event;
   }

  cancelRequest() {
    this.modalRef.hide();
    this.onGetTravelInfo();
  }

  cancelRequest2() {
    this.modalRef1.hide();
  }

  closeModal() {
    this.modalRef.hide();
  }

 async onGetEmployeeInfo(){
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
    setTimeout(()=>{
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


  async actionRequest(template: TemplateRef<any>,template1: TemplateRef<any>) {

    this.travelDeskInfo = new MyTravelDesk();
    let travelData = new MyTravelDesk();

    this.travelDeskInfo = new MyTravelDesk();
    let newtravelData = new MyTravelDesk();
    newtravelData.requestId=this.selectedTravelRequest.requestId;
    newtravelData.employeeId = this.selectedTravelRequest.empId;
    newtravelData.status = this.selectedTravelRequest.approverStatus;
      if (!this.selectedTravelRequest.approverStatus) {
        this.openAlertMod1(template1, "Please select Approver Status");
        return;
      }

    if(this.selectedTravelRequest.level == 1){
      newtravelData.level1approverRemarks = this.selectedTravelRequest.approverRemarks;

    }
    else{
      newtravelData.level2approverRemarks = this.selectedTravelRequest.approverRemarks;

    }


    console.log('Approver Status ', newtravelData.approverStatus);

    console.log('newtravelData :::::::::::::::::::::::::::::', newtravelData);
    

    try {
      const response: any = await this.travelDesk.approveOrRejectTraveldesk(newtravelData).toPromise();

      console.log('AResponse Data :::::::::::::::::', response);
   

      if (response.serviceStatus === "Success") {
        
        if(response.serviceResponse.finalstatus === "Rejected"){
          this.modalRef.hide();
          this.alertMessage = `Success! Your request was Rejected successfully ..!!!!`;
          this.openAlertMod(template, this.alertMessage);
        }else{
          this.modalRef.hide();
        this.alertMessage = `Success! Your request was approved successfully ..!!!!`;
        this.openAlertMod(template, this.alertMessage);
        console.log('Updated Travel Request:', this.selectedTravelRequest);
        }
        
      } else {
        console.error('Error updating travel request:', response.serviceResponse);
        alert('There was an issue updating the data.');
      }
      this.modalRef.hide();
      
    } catch (error) {
      console.error('Error during API call:', error);
      alert('An error occurred while updating the data. Please try again later.');
    }
  



 // }
}


 

}
