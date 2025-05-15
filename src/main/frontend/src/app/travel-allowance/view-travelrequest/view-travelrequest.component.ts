import { Component, OnInit, SecurityContext, TemplateRef, ViewChild } from '@angular/core';
import { TravelDeskService } from 'src/app/services/travel-desk.service';
import { MyTravelDesk } from 'src/app/models/travelDesk';
import { Employee } from 'src/app/models/employee';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { DomSanitizer } from '@angular/platform-browser';
import { EmployeeService } from 'src/app/services/employee.service';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { Conditional } from '@angular/compiler';
import { formatDate } from '@angular/common';
declare var $: any; // Import jQuery if it's being used for DOM manipulation

@Component({
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
  modalRef: BsModalRef = new BsModalRef();
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
    this.alertMessage = message;
  }
  openEditModal(template: TemplateRef<any> ,row :any) {

    this.selectedTravelRequest = { ...row };

    this.selectedTravelRequest.fromDate = this.formatDate(this.selectedTravelRequest.fromDate);
    this.selectedTravelRequest.toDate = this.formatDate(this.selectedTravelRequest.toDate);
    this.selectedTravelRequest.appliedOn = this.formatDate(this.selectedTravelRequest.appliedOn);

    console.log('editpain asichi re ::::::::::::::::::::',this.selectedTravelRequest);

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
          this.modalRef.hide();

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

  async deleteTraveldata(row : any,template: TemplateRef<any>) {
    if (this.isValidForm()) {
      this.travelDeskInfo = new MyTravelDesk();
      let travelData = new MyTravelDesk();

      this.selectedTraveldataforDelete  = { ...row }; 

      console.log('delete pain asichi reee :::::::::::::::::',this.selectedTraveldataforDelete)

      console.log('currentEmployeeInfo ::::::::::::::::',this.currentEmployeeInfo);
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


  closeModal() {
    this.modalRef.hide();
  }


}
